package com.pm.appointment_service.repository;

import com.pm.appointment_service.model.Appointment;
import com.pm.appointment_service.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByPatientIdAndStatus(UUID patientId, AppointmentStatus status);

    List<Appointment> findByPatientId(UUID patientId);

    List<Appointment> findByDoctorId(UUID doctorId);

    List<Appointment> findByDoctorIdAndAppointmentDate(UUID doctorId, LocalDate appointmentDate);

    @Query("SELECT count(a) FROM Appointment a WHERE a.doctorId = :docId AND a.appointmentDate = :date AND a.startTime < :reqEnd AND a.endTime > :reqStart AND a.status = 'SCHEDULED'")
    long findConflictingAppointments(
            @Param("docId") UUID docId,
            @Param("date") LocalDate date,
            @Param("reqStart") LocalTime reqStart,
            @Param("reqEnd") LocalTime reqEnd
    );

    @Query("SELECT a FROM Appointment a WHERE a.doctorId = :docId AND a.appointmentDate >= :startDate AND a.appointmentDate <= :endDate AND a.status = 'SCHEDULED'")
    List<Appointment> findScheduledAppointmentsInPeriod(
            @Param("docId") UUID docId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
