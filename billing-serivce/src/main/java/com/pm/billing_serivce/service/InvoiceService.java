package com.pm.billing_serivce.service;

import com.pm.billing_serivce.dto.InvoiceRequest;
import com.pm.billing_serivce.kafka.BillingKafkaProducer;
import com.pm.billing_serivce.model.Invoice;
import com.pm.billing_serivce.model.InvoiceItem;
import com.pm.billing_serivce.model.PaymentTransaction;
import com.pm.billing_serivce.repository.InvoiceRepository;
import com.pm.billing_serivce.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
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
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final BillingKafkaProducer billingKafkaProducer;
    private final PaymentTransactionRepository paymentTransactionRepository;

    @Transactional
    public Invoice createInvoice(InvoiceRequest request) {
        BigDecimal total = BigDecimal.ZERO;
        List<InvoiceItem> items = new ArrayList<>();

        for (InvoiceRequest.ItemRequest itemReq : request.getItems()) {
            // No more DB lookup! We take the values directly from the request.
            BigDecimal lineTotal = itemReq.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            total = total.add(lineTotal);

            items.add(InvoiceItem.builder()
                    .serviceName(itemReq.getServiceName())
                    .price(itemReq.getPrice())
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

        BigDecimal remainingAmount= invoice.getTotalAmount().subtract(invoice.getAmountPaid());
        if(amountPaid.compareTo(remainingAmount) > 0){
            throw new RuntimeException("Payment exceeds remaining balance. Remaining balance is: " + remainingAmount);
        }
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAID) {
            throw new RuntimeException("Already paid");
        }
        invoice.setAmountPaid(invoice.getAmountPaid().add(amountPaid));
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
}
