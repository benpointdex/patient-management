package com.pm.doctor_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "doctor_availability")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long availabilityId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Doctor doctor;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @NotNull
    @Column(nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(nullable = false)
    private LocalTime endTime;

    private LocalTime breakStartTime;

    private LocalTime breakEndTime;

    @NotNull
    @Column(nullable = false)
    private Integer slotDurationMinutes = 15;

    @NotNull
    @Column(nullable = false)
    private Boolean isAvailable = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
