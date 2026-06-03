package com.pm.doctor_service.model;

import com.pm.doctor_service.model.enums.AvailabilityStatus;
import com.pm.doctor_service.model.enums.EmploymentType;
import com.pm.doctor_service.model.enums.Status;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "doctor")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "UUID")
    private UUID doctorId;

    @Column(unique = true, nullable = false)
    private String employeeId;

    @NotBlank
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Column(nullable = false)
    private String lastName;

    private String fullName;

    @NotBlank
    @Email
    @Column(unique = true, nullable = false)
    private String email;

    private String phoneNumber;

    private String gender;

    private LocalDate dateOfBirth;

    private String profileImageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "specialization_id")
    private Specialization specialization;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    private String qualification;

    private Integer experienceYears;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private String languagesSpoken;

    private String medicalLicenseNumber;

    private LocalDate licenseExpiryDate;

    private BigDecimal consultationFee;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    private LocalDate joiningDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private String createdBy;

    private String updatedBy;

    // Helper hook to set fullName before persisting/updating
    @PrePersist
    @PreUpdate
    private void setFullNameFromFirstAndLastName() {
        this.fullName = (this.firstName != null ? this.firstName.trim() : "") + " " + (this.lastName != null ? this.lastName.trim() : "");
        this.fullName = this.fullName.trim();
    }
}
