package com.pm.doctor_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "doctor_experience")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long experienceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotBlank
    @Column(nullable = false)
    private String hospitalName;

    @NotBlank
    @Column(nullable = false)
    private String designation;

    @NotNull
    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    private Double yearsWorked;

    @Column(columnDefinition = "TEXT")
    private String description;
}
