package com.pm.appointment_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointment_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column(name = "patient_id")
    private UUID patientId;

    @NotNull
    @Column(name = "doctor_id")
    private UUID doctorId;

    @NotNull
    @Column(name = "preferred_date")
    private LocalDate preferredDate;

    @Column(name = "preferred_time_window")
    private String preferredTimeWindow; // "MORNING", "AFTERNOON", "EVENING"

    @Column(name = "consultation_type")
    private String consultationType; // "FOLLOW_UP", "NEW_CONSULTATION"

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "priority")
    private String priority; // "LOW", "NORMAL", "HIGH", "URGENT"

    @NotNull
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
