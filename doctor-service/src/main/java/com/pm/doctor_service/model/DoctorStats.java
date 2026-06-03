package com.pm.doctor_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "doctor_stats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStats {

    @Id
    private UUID doctorId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "doctor_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Doctor doctor;

    @NotNull
    @Column(nullable = false)
    private Integer totalAppointments = 0;

    @NotNull
    @Column(nullable = false)
    private Integer completedAppointments = 0;

    @NotNull
    @Column(nullable = false)
    private Integer cancelledAppointments = 0;

    @NotNull
    @Column(nullable = false)
    private Integer patientsHandled = 0;

    @NotNull
    @Column(nullable = false)
    private BigDecimal totalRevenueGenerated = BigDecimal.ZERO;

    private Double averageRating;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
