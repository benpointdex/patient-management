package com.pm.doctor_service.grpc;

import com.pm.doctor_service.model.*;
import com.pm.doctor_service.model.enums.Status;
import com.pm.doctor_service.service.DoctorService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@GrpcService
public class DoctorGrpcServiceImpl extends DoctorGrpcServiceGrpc.DoctorGrpcServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(DoctorGrpcServiceImpl.class);

    @Autowired
    private DoctorService doctorService;

    @Override
    public void verifyDoctorActive(DoctorIdRequest request, StreamObserver<DoctorProfileResponse> responseObserver) {
        log.info("verifyDoctorActive gRPC call received for doctor ID: {}", request.getDoctorId());
        
        try {
            UUID doctorId = UUID.fromString(request.getDoctorId());
            Doctor doctor = doctorService.getDoctorById(doctorId);

            boolean isActive = doctor.getStatus() == Status.ACTIVE;
            
            DoctorProfileResponse response = DoctorProfileResponse.newBuilder()
                    .setIsActive(isActive)
                    .setFirstName(doctor.getFirstName() != null ? doctor.getFirstName() : "")
                    .setLastName(doctor.getLastName() != null ? doctor.getLastName() : "")
                    .setFullName(doctor.getFullName() != null ? doctor.getFullName() : "")
                    .setSpecializationName(doctor.getSpecialization() != null ? doctor.getSpecialization().getName() : "")
                    .setDepartmentName(doctor.getDepartment() != null ? doctor.getDepartment().getDepartmentName() : "")
                    .setConsultationFee(doctor.getConsultationFee() != null ? doctor.getConsultationFee().doubleValue() : 0.0)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in verifyDoctorActive gRPC call: ", e);
            DoctorProfileResponse response = DoctorProfileResponse.newBuilder()
                    .setIsActive(false)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void checkAvailability(AvailabilityRequest request, StreamObserver<AvailabilityResponse> responseObserver) {
        log.info("checkAvailability gRPC call received for doctor: {} on {} at {}", 
                request.getDoctorId(), request.getDayOfWeek(), request.getRequestedTime());

        try {
            UUID doctorId = UUID.fromString(request.getDoctorId());
            DayOfWeek day = DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase());
            LocalTime time = LocalTime.parse(request.getRequestedTime(), DateTimeFormatter.ISO_LOCAL_TIME);

            List<DoctorAvailability> availabilities = doctorService.getAvailabilityByDoctorId(doctorId);
            
            boolean isAvailable = false;
            int slotDuration = 15;

            for (DoctorAvailability avail : availabilities) {
                if (avail.getIsAvailable() && avail.getDayOfWeek() == day) {
                    LocalTime start = avail.getStartTime();
                    LocalTime end = avail.getEndTime();
                    
                    // Basic range check: start <= time <= end
                    if ((time.equals(start) || time.isAfter(start)) && (time.equals(end) || time.isBefore(end))) {
                        
                        // Check if time overlaps with break windows
                        LocalTime breakStart = avail.getBreakStartTime();
                        LocalTime breakEnd = avail.getBreakEndTime();
                        boolean inBreak = false;
                        
                        if (breakStart != null && breakEnd != null) {
                            if ((time.equals(breakStart) || time.isAfter(breakStart)) && time.isBefore(breakEnd)) {
                                inBreak = true;
                            }
                        }
                        
                        if (!inBreak) {
                            isAvailable = true;
                            slotDuration = avail.getSlotDurationMinutes();
                            break;
                        }
                    }
                }
            }

            AvailabilityResponse response = AvailabilityResponse.newBuilder()
                    .setIsAvailable(isAvailable)
                    .setSlotDurationMinutes(slotDuration)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in checkAvailability gRPC call: ", e);
            AvailabilityResponse response = AvailabilityResponse.newBuilder()
                    .setIsAvailable(false)
                    .setSlotDurationMinutes(15)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getDoctorConsultationDetails(DoctorIdRequest request, StreamObserver<ConsultationResponse> responseObserver) {
        log.info("getDoctorConsultationDetails gRPC call received for doctor ID: {}", request.getDoctorId());

        try {
            UUID doctorId = UUID.fromString(request.getDoctorId());
            Doctor doctor = doctorService.getDoctorById(doctorId);

            ConsultationResponse response = ConsultationResponse.newBuilder()
                    .setConsultationFee(doctor.getConsultationFee() != null ? doctor.getConsultationFee().doubleValue() : 0.0)
                    .setEmployeeId(doctor.getEmployeeId() != null ? doctor.getEmployeeId() : "")
                    .setMedicalLicenseNumber(doctor.getMedicalLicenseNumber() != null ? doctor.getMedicalLicenseNumber() : "")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in getDoctorConsultationDetails gRPC call: ", e);
            ConsultationResponse response = ConsultationResponse.newBuilder()
                    .setConsultationFee(0.0)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateDoctorStats(UpdateStatsRequest request, StreamObserver<UpdateStatsResponse> responseObserver) {
        log.info("updateDoctorStats gRPC call received for doctor ID: {}", request.getDoctorId());

        try {
            UUID doctorId = UUID.fromString(request.getDoctorId());
            Double rating = request.getRatingScore() > 0.0 ? request.getRatingScore() : null;

            doctorService.updateStats(
                    doctorId,
                    request.getTotalAppointmentsDelta(),
                    request.getCompletedAppointmentsDelta(),
                    request.getCancelledAppointmentsDelta(),
                    request.getPatientsHandledDelta(),
                    request.getRevenueDelta(),
                    rating
            );

            UpdateStatsResponse response = UpdateStatsResponse.newBuilder()
                    .setSuccess(true)
                    .setMessage("Doctor stats updated successfully.")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in updateDoctorStats gRPC call: ", e);
            UpdateStatsResponse response = UpdateStatsResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Failed to update stats: " + e.getMessage())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
