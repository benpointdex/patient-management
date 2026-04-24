package com.pm.billing_serivce.repository;

import com.pm.billing_serivce.model.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionRepository extends  JpaRepository<PaymentTransaction, Long> {
    List<PaymentTransaction> findByInvoiceId(Long invoiceId);
}
