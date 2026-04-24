package com.pm.analytics_service.repository;

import com.pm.analytics_service.model.PatientStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.math.BigDecimal;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyticsRepository extends JpaRepository<PatientStat, Long> {
    Optional<PatientStat> findByPatientId(String patientId);

    long countByBillingStatus(String status);

    @Query("SELECT SUM(p.totalSpent) FROM PatientStat p")
    BigDecimal sumTotalSpent();
    @Query("SELECT SUM(p.totalPaid) FROM PatientStat p") // ADD THIS LINE
    BigDecimal sumTotalPaid();
}
