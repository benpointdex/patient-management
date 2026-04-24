package com.pm.billing_serivce.repository;

import com.pm.billing_serivce.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByPatientId(String patientId);
    List<Invoice> findByStatus(Invoice.InvoiceStatus status);
    List<Invoice> findByStatusIn(List<Invoice.InvoiceStatus> statuses);
}
