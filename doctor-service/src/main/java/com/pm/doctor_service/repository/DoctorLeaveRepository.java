package com.pm.doctor_service.repository;

import com.pm.doctor_service.model.DoctorLeave;
import com.pm.doctor_service.model.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorLeaveRepository extends JpaRepository<DoctorLeave, Long> {
    List<DoctorLeave> findByDoctor_DoctorId(UUID doctorId);
    List<DoctorLeave> findByDoctor_DoctorIdAndStatus(UUID doctorId, LeaveStatus status);
}
