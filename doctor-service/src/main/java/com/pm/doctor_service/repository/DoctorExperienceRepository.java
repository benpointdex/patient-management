package com.pm.doctor_service.repository;

import com.pm.doctor_service.model.DoctorExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorExperienceRepository extends JpaRepository<DoctorExperience, Long> {
    List<DoctorExperience> findByDoctor_DoctorId(UUID doctorId);
}
