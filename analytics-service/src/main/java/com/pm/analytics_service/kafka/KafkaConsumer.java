package com.pm.analytics_service.kafka;

import billing_event.BillingEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import com.pm.analytics_service.model.PatientStat;
import com.pm.analytics_service.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(
            KafkaConsumer.class);

    private final AnalyticsRepository analyticsRepository;

    @KafkaListener(topics = "patient", groupId = "analytics-service")
    public void consumeEvent(byte[] event) {
        try {
            PatientEvent patientEvent = PatientEvent.parseFrom(event);

            log.info("Received Patient Event: [PatientId={},PatientName={},PatientEmail={}]",
                    patientEvent.getPatientId(),
                    patientEvent.getName(),
                    patientEvent.getEmail());

            PatientStat stat = PatientStat.builder()
                    .patientId(patientEvent.getPatientId())
                    .name(patientEvent.getName())
                    .email(patientEvent.getEmail())
                    .gender(patientEvent.getGender())
                    .address(patientEvent.getAddress())
                    .dateOfBirth(LocalDate.parse(patientEvent.getDateOfBirth()))
                    .totalSpent(BigDecimal.ZERO)
                    .registeredAt(LocalDateTime.now())
                    .processedAt(LocalDateTime.now())
                    .build();

            analyticsRepository.save(stat);
            log.info("Persisted patient analytics for patient: {}", patientEvent.getPatientId());

        } catch (InvalidProtocolBufferException e) {
            log.error("Error deserializing event {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "billing", groupId = "analytics-service")
    public void consumeBillingEvent(byte[] event) {
        try {
            BillingEvent billingEvent = BillingEvent.parseFrom(event);
            log.info("Received billing event for patient: {}", billingEvent.getPatientId());

            analyticsRepository.findByPatientId(billingEvent.getPatientId())
                    .ifPresentOrElse(stat -> {
                        if ("INVOICE_CREATED".equals(billingEvent.getEventType())) {
                            BigDecimal currentSpent = stat.getTotalSpent() != null ? stat.getTotalSpent() : BigDecimal.ZERO;
                            stat.setTotalSpent(currentSpent.add(BigDecimal.valueOf(billingEvent.getAmount())));
                            log.info("Aggregated revenue for patient {}: +{}", billingEvent.getPatientId(), billingEvent.getAmount());
                        } else if ("INVOICE_PAID".equals(billingEvent.getEventType())) {
                            BigDecimal currentPaid = stat.getTotalPaid() != null ? stat.getTotalPaid() : BigDecimal.ZERO;
                            stat.setTotalPaid(currentPaid.add(BigDecimal.valueOf(billingEvent.getAmount())));
                        } else if ("INVOICE_PAID".equals(billingEvent.getEventType()) || "PAYMENT_RECEIVED".equals(billingEvent.getEventType())) {
                            BigDecimal currentPaid=stat.getTotalPaid()!=null? stat.getTotalPaid(): BigDecimal.ZERO;
                            stat.setTotalPaid(currentPaid.add(BigDecimal.valueOf(billingEvent.getAmount())));
                        }

                        stat.setBillingAccountId(billingEvent.getAccountId());
                        stat.setBillingStatus(billingEvent.getStatus());
                        stat.setProcessedAt(LocalDateTime.now());
                        
                        analyticsRepository.save(stat);
                        log.info("Updated billing info for patient: {}", billingEvent.getPatientId());
                    }, () -> {
                        log.warn("Received billing event for unknown patient: {}", billingEvent.getPatientId());
                    });

        } catch (Exception e) {
            log.error("Error while handling the billing event", e);
        }
    }
}
