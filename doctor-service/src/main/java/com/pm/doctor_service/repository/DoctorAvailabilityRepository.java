package com.pm.doctor_service.repository;

import com.pm.doctor_service.model.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    List<DoctorAvailability> findByDoctor_DoctorId(UUID doctorId);
    List<DoctorAvailability> findByDoctor_DoctorIdAndDayOfWeekAndIsAvailableTrue(UUID doctorId, DayOfWeek dayOfWeek);
}
