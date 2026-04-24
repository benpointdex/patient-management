package com.pm.billing_serivce.repository;

import com.pm.billing_serivce.model.BillingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface BillingRepository extends JpaRepository<BillingAccount, UUID> {
}
