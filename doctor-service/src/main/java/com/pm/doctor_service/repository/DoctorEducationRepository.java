package com.pm.doctor_service.repository;

import com.pm.doctor_service.model.DoctorEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorEducationRepository extends JpaRepository<DoctorEducation, Long> {
    List<DoctorEducation> findByDoctor_DoctorId(UUID doctorId);
}
