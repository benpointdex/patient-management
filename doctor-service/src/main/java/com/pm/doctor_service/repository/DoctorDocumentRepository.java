package com.pm.doctor_service.repository;

import com.pm.doctor_service.model.DoctorDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorDocumentRepository extends JpaRepository<DoctorDocument, Long> {
    List<DoctorDocument> findByDoctor_DoctorId(UUID doctorId);
}
