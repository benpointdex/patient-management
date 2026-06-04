package com.pm.appointment_service.service;

import com.pm.appointment_service.dto.AppointmentRequest;
import com.pm.appointment_service.dto.AppointmentResponse;
import com.pm.appointment_service.grpc.DoctorGrpcClient;
import com.pm.appointment_service.grpc.PatientGrpcClient;
import com.pm.appointment_service.model.Appointment;
import com.pm.appointment_service.model.AppointmentStatus;
import com.pm.appointment_service.model.OutboxEvent;
import com.pm.appointment_service.repository.AppointmentRepository;
import com.pm.appointment_service.repository.OutboxEventRepository;
import com.pm.doctor_service.grpc.AvailabilityResponse;
import com.pm.doctor_service.grpc.DoctorProfileResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);
    
    private final AppointmentRepository appointmentRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final PatientGrpcClient patientGrpcClient;
    private final DoctorGrpcClient doctorGrpcClient;
    private final com.pm.appointment_service.repository.AppointmentRequestRepository appointmentRequestRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public AppointmentResponse createAppointment(AppointmentRequest request) {
        log.info("Scheduling appointment: patient={}, doctor={}", request.getPatientId(), request.getDoctorId());

        // 1. Validate Patient existence via gRPC
        boolean patientValid = patientGrpcClient.verifyPatient(request.getPatientId());
        if (!patientValid) {
            log.warn("Patient verification failed for ID: {}", request.getPatientId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or inactive patient ID");
        }

        // 2. Validate Doctor existence and active status via gRPC
        DoctorProfileResponse doctorProfile = doctorGrpcClient.verifyDoctorActive(request.getDoctorId());
        if (doctorProfile == null || !doctorProfile.getIsActive()) {
            log.warn("Doctor verification failed for ID: {}", request.getDoctorId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is not active or does not exist");
        }

        // Parse date and start time
        LocalDate date = LocalDate.parse(request.getAppointmentDate());
        LocalTime startTime = LocalTime.parse(request.getStartTime());
        LocalTime endTime = startTime.plusMinutes(request.getDurationMinutes());

        // Validate that appointment is not in the past (previous days or past times on current day)
        LocalDateTime appointmentDateTime = LocalDateTime.of(date, startTime);
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            log.warn("Cannot book appointment in the past: {} {}", date, startTime);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot book appointments in the past");
        }

        // Validate that appointment is not more than 7 days in the future
        LocalDate maxFutureDate = LocalDate.now().plusDays(7);
        if (date.isAfter(maxFutureDate)) {
            log.warn("Cannot book appointment beyond 7 days: {}", date);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointments can only be scheduled up to 7 days in advance.");
        }

        // 3. Validate Doctor availability via gRPC
        String dayOfWeek = date.getDayOfWeek().name();
        String timeStr = startTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
        AvailabilityResponse availability = doctorGrpcClient.checkAvailability(request.getDoctorId(), dayOfWeek, timeStr);
        if (availability == null || !availability.getIsAvailable()) {
            log.warn("Doctor {} is not available on {} at {}", request.getDoctorId(), dayOfWeek, timeStr);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is not available at the requested time slot");
        }

        // 4. Strong consistency check: Conflict resolution at DB level
        long conflicts = appointmentRepository.findConflictingAppointments(
                UUID.fromString(request.getDoctorId()), date, startTime, endTime
        );
        if (conflicts > 0) {
            log.warn("Double-booking conflict detected for doctor {} on {} between {} and {}", 
                    request.getDoctorId(), date, startTime, endTime);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The requested time slot is already booked for this doctor");
        }

        // 5. Save appointment to DB
        Appointment appointment = Appointment.builder()
                .patientId(UUID.fromString(request.getPatientId()))
                .doctorId(UUID.fromString(request.getDoctorId()))
                .appointmentDate(date)
                .startTime(startTime)
                .endTime(endTime)
                .status(AppointmentStatus.SCHEDULED)
                .notes(request.getNotes())
                .build();
        appointment = appointmentRepository.save(appointment);

        // 6. Write outbox event (in same transaction)
        saveOutboxEvent("APPOINTMENT_SCHEDULED", appointment, doctorProfile.getDepartmentName());

        return AppointmentResponse.fromEntity(appointment);
    }

    @Transactional
    public com.pm.appointment_service.dto.AppointmentRequestResponse createAppointmentRequest(AppointmentRequest request) {
        log.info("Creating appointment request: patient={}, doctor={}", request.getPatientId(), request.getDoctorId());

        // 1. Validate Patient existence via gRPC
        boolean patientValid = patientGrpcClient.verifyPatient(request.getPatientId());
        if (!patientValid) {
            log.warn("Patient verification failed for ID: {}", request.getPatientId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or inactive patient ID");
        }

        // 2. Validate Doctor existence and active status via gRPC
        DoctorProfileResponse doctorProfile = doctorGrpcClient.verifyDoctorActive(request.getDoctorId());
        if (doctorProfile == null || !doctorProfile.getIsActive()) {
            log.warn("Doctor verification failed for ID: {}", request.getDoctorId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is not active or does not exist");
        }

        LocalDate date = LocalDate.parse(request.getAppointmentDate());

        // 3. Past check validation
        if (date.isBefore(LocalDate.now())) {
            log.warn("Cannot request appointment in the past: {}", date);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot request appointments in the past");
        }

        // 4. Future booking limit check (7 days)
        LocalDate maxFutureDate = LocalDate.now().plusDays(7);
        if (date.isAfter(maxFutureDate)) {
            log.warn("Cannot request appointment beyond 7 days: {}", date);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointments can only be scheduled up to 7 days in advance.");
        }

        // 5. Global Capacity Limit (40 requests/day system-wide)
        long globalCount = appointmentRequestRepository.countGlobalRequestsForDate(date);
        if (globalCount >= 40) {
            log.warn("Global request limit of 40 reached for date: {}", date);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Daily request limit of 40 has been reached for this date. Please choose another date.");
        }

        // 6. Doctor Capacity & Waitlisting Logic
        long doctorCount = appointmentRequestRepository.countActiveRequestsForDoctorAndDate(
                UUID.fromString(request.getDoctorId()), date
        );

        String priority = request.getPriority() != null ? request.getPriority().toUpperCase() : "NORMAL";
        com.pm.appointment_service.model.RequestStatus targetStatus;

        if ("URGENT".equals(priority)) {
            if (doctorCount < 20) {
                targetStatus = com.pm.appointment_service.model.RequestStatus.UNDER_REVIEW;
            } else {
                targetStatus = com.pm.appointment_service.model.RequestStatus.WAITLISTED;
            }
        } else {
            if (doctorCount < 17) {
                targetStatus = com.pm.appointment_service.model.RequestStatus.UNDER_REVIEW;
            } else {
                targetStatus = com.pm.appointment_service.model.RequestStatus.WAITLISTED;
            }
        }

        com.pm.appointment_service.model.AppointmentRequest apptRequest = com.pm.appointment_service.model.AppointmentRequest.builder()
                .patientId(UUID.fromString(request.getPatientId()))
                .doctorId(UUID.fromString(request.getDoctorId()))
                .preferredDate(date)
                .preferredTimeWindow(request.getPreferredTimeWindow() != null ? request.getPreferredTimeWindow() : "MORNING")
                .consultationType(request.getConsultationType() != null ? request.getConsultationType() : "NEW_CONSULTATION")
                .reason(request.getNotes() != null ? request.getNotes() : "")
                .priority(priority)
                .status(targetStatus)
                .build();

        apptRequest = appointmentRequestRepository.save(apptRequest);

        saveOutboxEvent("APPOINTMENT_REQUEST_CREATED", apptRequest, targetStatus.name());

        return com.pm.appointment_service.dto.AppointmentRequestResponse.fromEntity(apptRequest);
    }

    @Transactional
    public AppointmentResponse acceptAppointmentRequest(UUID requestId, AppointmentRequest acceptRequest) {
        log.info("Accepting appointment request: {}", requestId);

        com.pm.appointment_service.model.AppointmentRequest request = appointmentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment request not found"));

        if (request.getStatus() != com.pm.appointment_service.model.RequestStatus.UNDER_REVIEW) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Only requests under review can be accepted");
        }

        LocalDate date = request.getPreferredDate();
        LocalTime startTime = LocalTime.parse(acceptRequest.getStartTime());
        LocalTime endTime = startTime.plusMinutes(acceptRequest.getDurationMinutes());

        // Only check for double-booking conflicts — the doctor explicitly assigns this slot.
        // We do NOT re-check roster availability here because the doctor IS the authority on their own schedule.
        long conflicts = appointmentRepository.findConflictingAppointments(
                request.getDoctorId(), date, startTime, endTime
        );
        if (conflicts > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The requested time slot is already booked for this doctor");
        }

        request.setStatus(com.pm.appointment_service.model.RequestStatus.ACCEPTED);
        appointmentRequestRepository.save(request);

        Appointment appointment = Appointment.builder()
                .patientId(request.getPatientId())
                .doctorId(request.getDoctorId())
                .appointmentDate(date)
                .startTime(startTime)
                .endTime(endTime)
                .status(AppointmentStatus.SCHEDULED)
                .notes(acceptRequest.getNotes() != null ? acceptRequest.getNotes() : request.getReason())
                .build();
        appointment = appointmentRepository.save(appointment);

        saveOutboxEvent("APPOINTMENT_SCHEDULED", appointment, "");

        return AppointmentResponse.fromEntity(appointment);
    }

    @Transactional
    public com.pm.appointment_service.dto.AppointmentRequestResponse rejectAppointmentRequest(UUID requestId) {
        log.info("Rejecting appointment request: {}", requestId);

        com.pm.appointment_service.model.AppointmentRequest request = appointmentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment request not found"));

        if (request.getStatus() != com.pm.appointment_service.model.RequestStatus.UNDER_REVIEW &&
            request.getStatus() != com.pm.appointment_service.model.RequestStatus.WAITLISTED) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Only pending or waitlisted requests can be rejected");
        }

        request.setStatus(com.pm.appointment_service.model.RequestStatus.REJECTED);
        request = appointmentRequestRepository.save(request);

        promoteNextWaitlisted(request.getDoctorId(), request.getPreferredDate());

        return com.pm.appointment_service.dto.AppointmentRequestResponse.fromEntity(request);
    }

    @Transactional
    public com.pm.appointment_service.dto.AppointmentRequestResponse cancelAppointmentRequest(UUID requestId) {
        log.info("Patient cancelling appointment request: {}", requestId);

        com.pm.appointment_service.model.AppointmentRequest request = appointmentRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment request not found"));

        if (request.getStatus() != com.pm.appointment_service.model.RequestStatus.UNDER_REVIEW &&
            request.getStatus() != com.pm.appointment_service.model.RequestStatus.WAITLISTED) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Only pending or waitlisted requests can be cancelled");
        }

        request.setStatus(com.pm.appointment_service.model.RequestStatus.CANCELLED_BY_PATIENT);
        request = appointmentRequestRepository.save(request);

        // Free up capacity — promote next waitlisted patient
        promoteNextWaitlisted(request.getDoctorId(), request.getPreferredDate());

        return com.pm.appointment_service.dto.AppointmentRequestResponse.fromEntity(request);
    }

    private void promoteNextWaitlisted(UUID doctorId, LocalDate date) {
        List<com.pm.appointment_service.model.AppointmentRequest> waitlisted = appointmentRequestRepository.findWaitlistedRequestsOrdered(doctorId, date);
        if (!waitlisted.isEmpty()) {
            com.pm.appointment_service.model.AppointmentRequest nextRequest = waitlisted.get(0);
            log.info("Promoting waitlisted request {} to UNDER_REVIEW", nextRequest.getId());
            nextRequest.setStatus(com.pm.appointment_service.model.RequestStatus.UNDER_REVIEW);
            appointmentRequestRepository.save(nextRequest);
            saveOutboxEvent("WAITLIST_PROMOTED", nextRequest, "UNDER_REVIEW");
        }
    }

    @Transactional
    public List<com.pm.appointment_service.dto.AppointmentRequestResponse> getPatientRequests(UUID patientId) {
        return appointmentRequestRepository.findByPatientId(patientId).stream()
                .map(com.pm.appointment_service.dto.AppointmentRequestResponse::fromEntity).toList();
    }

    @Transactional
    public List<com.pm.appointment_service.dto.AppointmentRequestResponse> getDoctorRequests(UUID doctorId) {
        return appointmentRequestRepository.findByDoctorId(doctorId).stream()
                .map(com.pm.appointment_service.dto.AppointmentRequestResponse::fromEntity).toList();
    }

    @Transactional
    public AppointmentResponse completeAppointment(UUID appointmentId) {
        log.info("Completing appointment: {}", appointmentId);
        
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Only scheduled appointments can be completed");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment = appointmentRepository.save(appointment);

        // Fetch doctor profile for department details
        DoctorProfileResponse doctorProfile = doctorGrpcClient.verifyDoctorActive(appointment.getDoctorId().toString());
        String deptName = (doctorProfile != null) ? doctorProfile.getDepartmentName() : "";

        // Write outbox event (in same transaction)
        saveOutboxEvent("APPOINTMENT_COMPLETED", appointment, deptName);

        return AppointmentResponse.fromEntity(appointment);
    }

    @Transactional
    public AppointmentResponse cancelAppointment(UUID appointmentId, String canceledBy) {
        log.info("Canceling appointment: {} by {}", appointmentId, canceledBy);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Only scheduled appointments can be canceled");
        }

        appointment.setStatus(AppointmentStatus.CANCELED);
        appointment.setNotes(appointment.getNotes() + " | Canceled by: " + canceledBy);
        appointment = appointmentRepository.save(appointment);

        // Write outbox event (in same transaction)
        saveOutboxEvent("APPOINTMENT_CANCELED", appointment, "");

        // Auto-promote waitlisted patient
        promoteNextWaitlisted(appointment.getDoctorId(), appointment.getAppointmentDate());

        return AppointmentResponse.fromEntity(appointment);
    }

    @Transactional
    public AppointmentResponse reschedule(UUID appointmentId, AppointmentRequest request) {
        log.info("Rescheduling appointment: {}", appointmentId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Only scheduled appointments can be rescheduled");
        }

        LocalDate oldDate = appointment.getAppointmentDate();

        // Temporarily set status to CANCELED to avoid self-conflict when validating slot
        AppointmentStatus originalStatus = appointment.getStatus();
        appointment.setStatus(AppointmentStatus.CANCELED);
        appointmentRepository.saveAndFlush(appointment);

        // Validate Doctor existence and active status via gRPC
        DoctorProfileResponse doctorProfile = doctorGrpcClient.verifyDoctorActive(request.getDoctorId());
        if (doctorProfile == null || !doctorProfile.getIsActive()) {
            appointment.setStatus(originalStatus);
            appointmentRepository.save(appointment);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is not active or does not exist");
        }

        // Parse date and start time
        LocalDate date = LocalDate.parse(request.getAppointmentDate());
        LocalTime startTime = LocalTime.parse(request.getStartTime());
        LocalTime endTime = startTime.plusMinutes(request.getDurationMinutes());

        // Validate that appointment is not in the past
        LocalDateTime appointmentDateTime = LocalDateTime.of(date, startTime);
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            appointment.setStatus(originalStatus);
            appointmentRepository.save(appointment);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot reschedule appointments to a past date/time");
        }

        // Validate that appointment is not more than 7 days in the future
        LocalDate maxFutureDate = LocalDate.now().plusDays(7);
        if (date.isAfter(maxFutureDate)) {
            appointment.setStatus(originalStatus);
            appointmentRepository.save(appointment);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointments can only be scheduled up to 7 days in advance.");
        }

        // Validate Doctor availability via gRPC
        String dayOfWeek = date.getDayOfWeek().name();
        String timeStr = startTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
        AvailabilityResponse availability = doctorGrpcClient.checkAvailability(request.getDoctorId(), dayOfWeek, timeStr);
        if (availability == null || !availability.getIsAvailable()) {
            appointment.setStatus(originalStatus);
            appointmentRepository.save(appointment);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is not available at the requested time slot");
        }

        // Strong consistency check: Conflict resolution at DB level
        long conflicts = appointmentRepository.findConflictingAppointments(
                UUID.fromString(request.getDoctorId()), date, startTime, endTime
        );
        if (conflicts > 0) {
            appointment.setStatus(originalStatus);
            appointmentRepository.save(appointment);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "The requested time slot is already booked for this doctor");
        }

        // Update fields in-place
        appointment.setDoctorId(UUID.fromString(request.getDoctorId()));
        appointment.setAppointmentDate(date);
        appointment.setStartTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setNotes(request.getNotes() != null ? request.getNotes() : appointment.getNotes());

        appointment = appointmentRepository.save(appointment);

        // If date has changed, trigger waitlist promotion on the old date!
        if (!oldDate.equals(date)) {
            promoteNextWaitlisted(appointment.getDoctorId(), oldDate);
        }

        // Write outbox event (in same transaction)
        saveOutboxEvent("APPOINTMENT_RESCHEDULED", appointment, doctorProfile.getDepartmentName());

        return AppointmentResponse.fromEntity(appointment);
    }

    @Transactional
    public void handleDoctorLeaveProcess(UUID doctorId, LocalDate startDate, LocalDate endDate) {
        log.info("System-canceling appointments for doctor {} due to leave between {} and {}", doctorId, startDate, endDate);
        
        List<Appointment> scheduledAppointments = appointmentRepository.findScheduledAppointmentsInPeriod(
                doctorId, startDate, endDate
        );

        for (Appointment appointment : scheduledAppointments) {
            log.info("System canceling appointment {} for patient {}", appointment.getId(), appointment.getPatientId());
            appointment.setStatus(AppointmentStatus.CANCELED_SYSTEM);
            appointment.setNotes(appointment.getNotes() + " | Automatically canceled due to doctor leave");
            appointmentRepository.save(appointment);

            saveOutboxEvent("APPOINTMENT_CANCELED", appointment, "");
        }
    }

    public AppointmentResponse getAppointment(UUID id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        return AppointmentResponse.fromEntity(appointment);
    }

    public List<AppointmentResponse> getPatientAppointments(UUID patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        return appointments.stream().map(AppointmentResponse::fromEntity).toList();
    }

    public List<AppointmentResponse> getDoctorSchedule(UUID doctorId, LocalDate date) {
        List<Appointment> appointments = appointmentRepository.findByDoctorIdAndAppointmentDate(doctorId, date);
        return appointments.stream().map(AppointmentResponse::fromEntity).toList();
    }

    public List<AppointmentResponse> getDoctorAllAppointments(UUID doctorId) {
        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctorId);
        return appointments.stream().map(AppointmentResponse::fromEntity).toList();
    }

    private void saveOutboxEvent(String eventType, Appointment appointment, String departmentName) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("eventType", eventType);
            payload.put("appointmentId", appointment.getId().toString());
            payload.put("patientId", appointment.getPatientId().toString());
            payload.put("doctorId", appointment.getDoctorId().toString());
            payload.put("timestamp", LocalDateTime.now().toString());
            payload.put("departmentId", departmentName != null ? departmentName : "");

            String jsonPayload = objectMapper.writeValueAsString(payload);

            OutboxEvent event = OutboxEvent.builder()
                    .eventType(eventType)
                    .payload(jsonPayload)
                    .processed(false)
                    .build();

            outboxEventRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to save outbox event for appointment: " + appointment.getId(), e);
            throw new RuntimeException("Outbox persistence failure", e);
        }
    }

    private void saveOutboxEvent(String eventType, com.pm.appointment_service.model.AppointmentRequest request, String departmentName) {
        try {
            Map<String, String> payload = new HashMap<>();
            payload.put("eventType", eventType);
            payload.put("appointmentRequestId", request.getId().toString());
            payload.put("patientId", request.getPatientId().toString());
            payload.put("doctorId", request.getDoctorId().toString());
            payload.put("timestamp", LocalDateTime.now().toString());
            payload.put("departmentId", departmentName != null ? departmentName : "");

            String jsonPayload = objectMapper.writeValueAsString(payload);

            OutboxEvent event = OutboxEvent.builder()
                    .eventType(eventType)
                    .payload(jsonPayload)
                    .processed(false)
                    .build();

            outboxEventRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to save outbox event for appointment request: " + request.getId(), e);
            throw new RuntimeException("Outbox persistence failure", e);
        }
    }
}
