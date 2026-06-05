package com.pm.billing_serivce.service;

import com.pm.billing_serivce.dto.InvoiceRequest;
import com.pm.billing_serivce.kafka.BillingKafkaProducer;
import com.pm.billing_serivce.model.Invoice;
import com.pm.billing_serivce.model.InvoiceItem;
import com.pm.billing_serivce.model.PaymentTransaction;
import com.pm.billing_serivce.repository.InvoiceRepository;
import com.pm.billing_serivce.repository.PaymentTransactionRepository;
import com.pm.billing_serivce.grpc.DoctorServiceGrpcClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BillingKafkaProducer billingKafkaProducer;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final DoctorServiceGrpcClient doctorServiceGrpcClient;

    @Transactional
    public Invoice createInvoice(InvoiceRequest request) {
        BigDecimal total = BigDecimal.ZERO;
        List<InvoiceItem> items = new ArrayList<>();

        for (InvoiceRequest.ItemRequest itemReq : request.getItems()) {
            BigDecimal price = itemReq.getPrice();
            // Fetch dynamically via gRPC if doctorId is present
            if (itemReq.getDoctorId() != null && !itemReq.getDoctorId().trim().isEmpty()) {
                try {
                    log.info("Fetching dynamic consultation fee via gRPC for doctorId: {}", itemReq.getDoctorId());
                    var docResponse = doctorServiceGrpcClient.getDoctorConsultationDetails(itemReq.getDoctorId());
                    if (docResponse.getConsultationFee() > 0) {
                        price = BigDecimal.valueOf(docResponse.getConsultationFee());
                        log.info("Successfully fetched and applied dynamic consultation fee: {} for doctorId: {}", price, itemReq.getDoctorId());
                    }
                } catch (Exception e) {
                    log.error("Failed to fetch consultation fee via gRPC, falling back to manual input price", e);
                }
            }

            BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(lineTotal);

            items.add(InvoiceItem.builder()
                    .serviceName(itemReq.getServiceName())
                    .doctorId(itemReq.getDoctorId())
                    .price(price)
                    .quantity(itemReq.getQuantity())
                    .build());
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .patientId(request.getPatientId())
                .totalAmount(total)
                .status(Invoice.InvoiceStatus.UNPAID)
                .createdAt(LocalDateTime.now())
                .items(items)
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);
        billingKafkaProducer.sendInvoiceEvent(savedInvoice, "INVOICE_CREATED");

        return savedInvoice;
    }

    @Transactional
    public Invoice payInvoice(Long invoiceId, BigDecimal amountPaid) {

        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new RuntimeException("Invoice not found"));

        BigDecimal currentPaid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal remainingAmount = invoice.getTotalAmount().subtract(currentPaid);
        if(amountPaid.compareTo(remainingAmount) > 0){
            throw new RuntimeException("Payment exceeds remaining balance. Remaining balance is: " + remainingAmount);
        }
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new RuntimeException("Already paid");
        }
        invoice.setAmountPaid(currentPaid.add(amountPaid));
        if(invoice.getAmountPaid().compareTo(invoice.getTotalAmount())>=0){
            invoice.setStatus(Invoice.InvoiceStatus.PAID);
        } else {
            invoice.setStatus(Invoice.InvoiceStatus.PARTIALLY_PAID);
        }
        PaymentTransaction paymentTransaction = PaymentTransaction.builder()
                .invoiceId(invoiceId)
                .amount(amountPaid)
                .paymentDate(new Date())
                .build();
        PaymentTransaction paymentTrn = paymentTransactionRepository.save(paymentTransaction);
        Invoice invoiceSaved = invoiceRepository.save(invoice);
        billingKafkaProducer.sendPaymentEvent(invoiceSaved, amountPaid, "PAYMENT_RECEIVED");

        // Trigger gRPC stats update if invoice is fully paid
        if (invoiceSaved.getStatus() == Invoice.InvoiceStatus.PAID) {
            for (InvoiceItem item : invoiceSaved.getItems()) {
                if (item.getDoctorId() != null && !item.getDoctorId().trim().isEmpty()) {
                    try {
                        log.info("Sending dynamic stats update to doctor-service via gRPC for doctorId: {}", item.getDoctorId());
                        doctorServiceGrpcClient.updateDoctorStats(
                                item.getDoctorId(),
                                1, // totalAppointmentsDelta
                                1, // completedAppointmentsDelta
                                0, // cancelledAppointmentsDelta
                                1, // patientsHandledDelta
                                item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())).doubleValue(), // revenueDelta
                                0.0 // ratingScore (no rating yet)
                        );
                        log.info("Successfully updated doctor stats via gRPC for doctorId: {}", item.getDoctorId());
                    } catch (Exception e) {
                        log.error("Failed to update doctor stats via gRPC for doctorId: {}", item.getDoctorId(), e);
                    }
                }
            }
        }

        return invoiceSaved;
    }

    public List<Invoice> getUnpaidInvoices() {
        return invoiceRepository.findByStatusIn(java.util.Arrays.asList(Invoice.InvoiceStatus.UNPAID, Invoice.InvoiceStatus.PARTIALLY_PAID));
    }

    public List<PaymentTransaction> getPaymentHistory(Long invoiceId) {
        return paymentTransactionRepository.findByInvoiceId(invoiceId);
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public List<Invoice> getPatientInvoices(String patientId) {
        return invoiceRepository.findByPatientId(patientId);
    }

    public java.util.Optional<Invoice> getInvoiceByAppointmentId(String appointmentId) {
        return invoiceRepository.findByAppointmentId(appointmentId);
    }
}
