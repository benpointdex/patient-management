package com.pm.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnalyticsSummary {
    private long totalPatients;
    private long activeAccounts;
    private long inactiveAccounts;
    private java.math.BigDecimal totalRevenue;
    private BigDecimal totalBilled;      // (Sum of Spent)
    private BigDecimal cashOnHand;       // (Sum of Paid)
    private BigDecimal accountsReceivable;
}
