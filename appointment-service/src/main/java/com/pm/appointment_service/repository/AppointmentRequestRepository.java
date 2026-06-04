package com.pm.appointment_service.repository;

import com.pm.appointment_service.model.AppointmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AppointmentRequestRepository extends JpaRepository<AppointmentRequest, UUID> {
    List<AppointmentRequest> findByPatientId(UUID patientId);
    List<AppointmentRequest> findByDoctorId(UUID doctorId);

    @Query("SELECT count(r) FROM AppointmentRequest r WHERE r.preferredDate = :date AND r.status IN ('UNDER_REVIEW', 'WAITLISTED', 'ACCEPTED')")
    long countGlobalRequestsForDate(@Param("date") LocalDate date);

    @Query("SELECT count(r) FROM AppointmentRequest r WHERE r.doctorId = :doctorId AND r.preferredDate = :date AND r.status IN ('UNDER_REVIEW', 'ACCEPTED')")
    long countActiveRequestsForDoctorAndDate(@Param("doctorId") UUID doctorId, @Param("date") LocalDate date);

    @Query("SELECT r FROM AppointmentRequest r WHERE r.doctorId = :doctorId AND r.preferredDate = :date AND r.status = 'WAITLISTED' ORDER BY r.createdAt ASC")
    List<AppointmentRequest> findWaitlistedRequestsOrdered(@Param("doctorId") UUID doctorId, @Param("date") LocalDate date);
}
