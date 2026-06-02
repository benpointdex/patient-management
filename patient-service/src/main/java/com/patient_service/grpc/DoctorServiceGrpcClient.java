package com.patient_service.grpc;

import com.pm.doctor_service.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

@Service
public class DoctorServiceGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(DoctorServiceGrpcClient.class);
    
    private final ManagedChannel channel;
    private final DoctorGrpcServiceGrpc.DoctorGrpcServiceBlockingStub blockingStub;

    public DoctorServiceGrpcClient(
            @Value("${doctor.grpc.host:doctor-service}") String serverAddress,
            @Value("${doctor.grpc.port:9002}") int serverPort) {

        log.info("Connecting to Doctor Service gRPC at {}:{}", serverAddress, serverPort);

        this.channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();

        this.blockingStub = DoctorGrpcServiceGrpc.newBlockingStub(channel);
    }

    public DoctorProfileResponse verifyDoctorActive(String doctorId) {
        log.info("gRPC check verifyDoctorActive for doctorId: {}", doctorId);
        try {
            DoctorIdRequest request = DoctorIdRequest.newBuilder()
                    .setDoctorId(doctorId)
                    .build();
            return blockingStub.verifyDoctorActive(request);
        } catch (Exception e) {
            log.error("gRPC error verifying doctor active status: ", e);
            return DoctorProfileResponse.newBuilder().setIsActive(false).build();
        }
    }

    public AvailabilityResponse checkAvailability(String doctorId, String dayOfWeek, String requestedTime) {
        log.info("gRPC check checkAvailability for doctorId: {}, day: {}, time: {}", doctorId, dayOfWeek, requestedTime);
        try {
            AvailabilityRequest request = AvailabilityRequest.newBuilder()
                    .setDoctorId(doctorId)
                    .setDayOfWeek(dayOfWeek)
                    .setRequestedTime(requestedTime)
                    .build();
            return blockingStub.checkAvailability(request);
        } catch (Exception e) {
            log.error("gRPC error checking doctor availability: ", e);
            return AvailabilityResponse.newBuilder().setIsAvailable(false).setSlotDurationMinutes(15).build();
        }
    }

    public UpdateStatsResponse updateDoctorStats(
            String doctorId, 
            int totalAppointmentsDelta,
            int completedAppointmentsDelta,
            int cancelledAppointmentsDelta,
            int patientsHandledDelta,
            double revenueDelta,
            double ratingScore) {
        log.info("gRPC call updateDoctorStats for doctorId: {}", doctorId);
        try {
            UpdateStatsRequest request = UpdateStatsRequest.newBuilder()
                    .setDoctorId(doctorId)
                    .setTotalAppointmentsDelta(totalAppointmentsDelta)
                    .setCompletedAppointmentsDelta(completedAppointmentsDelta)
                    .setCancelledAppointmentsDelta(cancelledAppointmentsDelta)
                    .setPatientsHandledDelta(patientsHandledDelta)
                    .setRevenueDelta(revenueDelta)
                    .setRatingScore(ratingScore)
                    .build();
            return blockingStub.updateDoctorStats(request);
        } catch (Exception e) {
            log.error("gRPC error updating doctor stats: ", e);
            return UpdateStatsResponse.newBuilder().setSuccess(false).setMessage("gRPC failed: " + e.getMessage()).build();
        }
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            log.info("Shutting down Doctor Service gRPC channel");
            channel.shutdown();
        }
    }
}
