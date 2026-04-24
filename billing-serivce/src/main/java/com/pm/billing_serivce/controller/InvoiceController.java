package com.pm.billing_serivce.controller;

import com.pm.billing_serivce.dto.InvoiceRequest;
import com.pm.billing_serivce.model.Invoice;
import com.pm.billing_serivce.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.pm.billing_serivce.model.PaymentTransaction;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/billing/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Invoice createInvoice(@RequestBody InvoiceRequest request) {
        return invoiceService.createInvoice(request);
    }

    @GetMapping("/patient/{patientId}")
    public List<Invoice> getPatientInvoices(@PathVariable String patientId) {
        return invoiceService.getPatientInvoices(patientId);
    }

    @PutMapping("/{id}/pay")
    public Invoice payInvoice(@PathVariable Long id , @RequestBody BigDecimal amount) {
        return invoiceService.payInvoice(id, amount);
    }

    @GetMapping("/{id}/payments")
    public List<PaymentTransaction> getPaymentHistory(@PathVariable Long id) {
        return invoiceService.getPaymentHistory(id);
    }

    @GetMapping("/all")
    public List<Invoice> getAllInvoices(@RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role) && !"CASHIER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Cashiers and Admins can see the billing history.");
        }
        return invoiceService.getAllInvoices();
    }

    @GetMapping("/unpaid")
    public List<Invoice> getUnpaidInvoices(@RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role) && !"CASHIER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Cashiers and Admins can see the debt queue.");
        }
        return invoiceService.getUnpaidInvoices();
    }
}
