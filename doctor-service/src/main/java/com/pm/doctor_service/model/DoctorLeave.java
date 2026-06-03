package com.pm.doctor_service.model;

import com.pm.doctor_service.model.enums.LeaveStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "doctor_leave")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorLeave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaveId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    private String leaveType;

    private Boolean earlyReturnRequested = false;

    private LocalDate earlyReturnDate;

    private String earlyReturnReason;

    @NotNull
    @Column(nullable = false)
    private LocalDate startDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;

    private String reason;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status = LeaveStatus.PENDING;

    private String approvedBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
