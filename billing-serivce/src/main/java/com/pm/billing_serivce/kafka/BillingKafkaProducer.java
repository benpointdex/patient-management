package com.pm.billing_serivce.kafka;

import billing_event.BillingEvent;
import com.pm.billing_serivce.model.BillingAccount;
import com.pm.billing_serivce.model.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j

public class BillingKafkaProducer {


    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    BillingKafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate){
        this.kafkaTemplate=kafkaTemplate;
    }


    public void sendBillingEvent(BillingAccount billingAccount, String eventType){


        BillingEvent billingEvent = BillingEvent.newBuilder()
                .setPatientId(billingAccount.getPatientId())
                .setAccountId(billingAccount.getAccountId().toString())
                .setStatus(billingAccount.getStatus())
                .setEventType(eventType).build();


        try {
            log.info("Sending Billing Event: [ id={},status={} ]",billingEvent.getAccountId(), billingEvent.getStatus());
            kafkaTemplate.send("billing", billingAccount.getPatientId(), billingEvent.toByteArray());
        } catch (Exception e) {
            log.error("Failed to send billing event to Kafka",e);
        }

    }

    public void sendPaymentEvent(Invoice invoice, BigDecimal paymentAmount , String eventType){
        BillingEvent event= BillingEvent.newBuilder()
                .setPatientId(invoice.getPatientId())
                .setAccountId(invoice.getInvoiceNumber())
                .setStatus(invoice.getStatus().name())
                .setEventType(eventType)
                .setAmount(paymentAmount.doubleValue())
                .build();

        try {
            log.info("Sending Payment Event: [ invoiceNumber={}, amount={} ]", invoice.getInvoiceNumber(), paymentAmount);
            kafkaTemplate.send("billing",invoice.getPatientId(), event.toByteArray());
        } catch (Exception e) {
            log.error("Failed to send payment event to Kafka", e);
        }
    }

    public void sendInvoiceEvent(Invoice invoice, String eventType) {
        BillingEvent event = BillingEvent.newBuilder()
                .setPatientId(invoice.getPatientId())
                .setAccountId(invoice.getInvoiceNumber()) // Storing Invoice # in accountId field
                .setStatus(invoice.getStatus().name())
                .setEventType(eventType)
                .setAmount(invoice.getTotalAmount().doubleValue()) // The Payday!
                .build();

        try {
            log.info("Sending Invoice Event: [ invoiceNumber={},status={} ]", invoice.getInvoiceNumber(), invoice.getStatus());
            kafkaTemplate.send("billing", invoice.getPatientId(), event.toByteArray());
        } catch (Exception e) {
            log.error("Failed to send invoice event to Kafka", e);
        }
    }

}
