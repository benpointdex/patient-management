package com.pm.analytics_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnalyticsResponse {
    private String patientId;
    private String name;
    private String email;
    private String billingAccountId;
    private String billingStatus;
    private java.math.BigDecimal totalSpent;
    private String gender;
    private LocalDate dateOfBirth;
    private String address;
    private BigDecimal totalPaid;
    private LocalDateTime registeredAt;
    private LocalDateTime processedAt;
}
