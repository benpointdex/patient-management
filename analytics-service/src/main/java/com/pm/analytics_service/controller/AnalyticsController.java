package com.pm.analytics_service.controller;

import com.pm.analytics_service.dto.AnalyticsResponse;
import com.pm.analytics_service.dto.AnalyticsSummary;
import com.pm.analytics_service.model.PatientStat;
import com.pm.analytics_service.repository.AnalyticsRepository;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsRepository analyticsRepository;

    @GetMapping("/all")
    public List<AnalyticsResponse> getAll() {
        return analyticsRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/summary")
    public AnalyticsSummary getSummary(@RequestHeader("X-User-Role") String role) {

        if(!"ADMIN".equals(role)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only admins allowed!");
        }
        BigDecimal totalRevenue = analyticsRepository.sumTotalSpent();
        BigDecimal totalBilled = analyticsRepository.sumTotalSpent();
        BigDecimal totalPaid = analyticsRepository.sumTotalPaid();

        totalBilled = (totalBilled != null) ? totalBilled : BigDecimal.ZERO;
        totalPaid = (totalPaid != null) ? totalPaid : BigDecimal.ZERO;

        return AnalyticsSummary.builder()
                .totalPatients(analyticsRepository.count())
                .activeAccounts(analyticsRepository.countByBillingStatus("ACTIVE"))
                .inactiveAccounts(analyticsRepository.countByBillingStatus("INACTIVE") + analyticsRepository.countByBillingStatus(null))
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .totalBilled(totalBilled)   // New metrics
                .cashOnHand(totalPaid)
                .accountsReceivable(totalBilled.subtract(totalPaid))
                .build();

    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<AnalyticsResponse> getByPatientId(@PathVariable String patientId) {
        return analyticsRepository.findByPatientId(patientId)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private AnalyticsResponse mapToResponse(PatientStat stat) {
        return AnalyticsResponse.builder()
                .patientId(stat.getPatientId())
                .name(stat.getName())
                .email(stat.getEmail())
                .billingAccountId(stat.getBillingAccountId())
                .billingStatus(stat.getBillingStatus())
                .totalSpent(stat.getTotalSpent())
                .gender(stat.getGender())
                .address(stat.getAddress())
                .dateOfBirth(stat.getDateOfBirth())
                .totalPaid(stat.getTotalPaid())
                .registeredAt(stat.getRegisteredAt())
                .processedAt(stat.getProcessedAt())
                .build();
    }
}
