package com.pm.billing_serivce.kafka;

import com.pm.appointment_service.grpc.AppointmentEvent;
import com.pm.billing_serivce.grpc.DoctorServiceGrpcClient;
import com.pm.billing_serivce.model.Invoice;
import com.pm.billing_serivce.model.InvoiceItem;
import com.pm.billing_serivce.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Listens to the `appointment-events` Kafka topic.
 * When an APPOINTMENT_COMPLETED event arrives, automatically generates
 * an invoice for the patient using the doctor's consultation fee (fetched via gRPC).
 *
 * System Rules:
 *  - Rule 1: No invoice before consultation.
 *  - Rule 2: Consultation completion automatically creates invoice.
 *  - Rule 3: No manual invoice creation for consultation (this is the only path).
 *  - Rule 4: Doctor cannot edit billing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventConsumer {

    private final InvoiceRepository invoiceRepository;
    private final BillingKafkaProducer billingKafkaProducer;
    private final DoctorServiceGrpcClient doctorServiceGrpcClient;

    @KafkaListener(topics = "appointment-events", groupId = "billing-service-group")
    @Transactional
    public void handleAppointmentEvent(byte[] eventBytes) {
        try {
            AppointmentEvent event = AppointmentEvent.parseFrom(eventBytes);
            log.info("Received appointment event: type={}, appointmentId={}, patientId={}, doctorId={}",
                    event.getEventType(), event.getAppointmentId(), event.getPatientId(), event.getDoctorId());

            if ("APPOINTMENT_COMPLETED".equals(event.getEventType())) {
                handleConsultationCompleted(event);
            } else {
                log.debug("Ignoring appointment event of type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process appointment event from Kafka", e);
            // In production, this would go to a Dead Letter Queue (DLQ).
            // We swallow here so a corrupt message doesn't block the partition.
        }
    }

    private void handleConsultationCompleted(AppointmentEvent event) {
        String appointmentId = event.getAppointmentId();
        String patientId    = event.getPatientId();
        String doctorId     = event.getDoctorId();

        // Idempotency guard: do not create a duplicate invoice for the same appointment
        if (invoiceRepository.existsByAppointmentId(appointmentId)) {
            log.warn("Invoice already exists for appointmentId={}. Skipping duplicate creation.", appointmentId);
            return;
        }

        // Resolve consultation fee from doctor-service via gRPC
        BigDecimal consultationFee = resolveConsultationFee(doctorId);

        // Build invoice item
        InvoiceItem item = InvoiceItem.builder()
                .serviceName("Consultation Fee")
                .doctorId(doctorId)
                .price(consultationFee)
                .quantity(1)
                .build();

        // Build auto-generated invoice
        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .patientId(patientId)
                .appointmentId(appointmentId)
                .totalAmount(consultationFee)
                .amountPaid(BigDecimal.ZERO)
                .status(Invoice.InvoiceStatus.UNPAID)
                .createdAt(LocalDateTime.now())
                .items(List.of(item))
                .build();

        Invoice saved = invoiceRepository.save(invoice);

        log.info("Auto-generated invoice {} for patient={} after consultation completion (appointmentId={})",
                saved.getInvoiceNumber(), patientId, appointmentId);

        // Notify other services via Kafka
        billingKafkaProducer.sendInvoiceEvent(saved, "INVOICE_CREATED");
    }

    /**
     * Fetches the doctor's consultation fee via gRPC.
     * Falls back to a default fee if gRPC call fails to prevent blocking invoice creation.
     */
    private BigDecimal resolveConsultationFee(String doctorId) {
        if (doctorId == null || doctorId.isBlank()) {
            log.warn("No doctorId in completed appointment event. Using default consultation fee.");
            return BigDecimal.valueOf(100.00);
        }
        try {
            var docResponse = doctorServiceGrpcClient.getDoctorConsultationDetails(doctorId);
            if (docResponse.getConsultationFee() > 0) {
                BigDecimal fee = BigDecimal.valueOf(docResponse.getConsultationFee());
                log.info("Fetched consultation fee {} for doctorId={} via gRPC", fee, doctorId);
                return fee;
            } else {
                log.warn("gRPC returned zero consultation fee for doctorId={}. Using default.", doctorId);
                return BigDecimal.valueOf(100.00);
            }
        } catch (Exception e) {
            log.error("gRPC call failed for doctorId={}. Using default consultation fee.", doctorId, e);
            return BigDecimal.valueOf(100.00);
        }
    }
}
