package com.pm.analytics_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "patient_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PatientStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientId;
    private String name;
    private String email;

    private String gender;
    private String address;
    private LocalDate dateOfBirth;
    private String billingAccountId;
    private String billingStatus;
    private BigDecimal totalSpent;
    private BigDecimal totalPaid;
    private LocalDateTime registeredAt;
    private LocalDateTime processedAt;
}
