package com.pm.doctor_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "doctor_education")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorEducation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long educationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @NotBlank
    @Column(nullable = false)
    private String degreeName;

    @NotBlank
    @Column(nullable = false)
    private String institutionName;

    private Integer completionYear;

    private String specialization;

    private String grade;
}
