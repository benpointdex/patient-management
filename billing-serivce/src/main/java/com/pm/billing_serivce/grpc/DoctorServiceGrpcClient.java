package com.pm.billing_serivce.grpc;

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

    public ConsultationResponse getDoctorConsultationDetails(String doctorId) {
        log.info("gRPC call getDoctorConsultationDetails for doctorId: {}", doctorId);
        try {
            DoctorIdRequest request = DoctorIdRequest.newBuilder()
                    .setDoctorId(doctorId)
                    .build();
            return blockingStub.getDoctorConsultationDetails(request);
        } catch (Exception e) {
            log.error("gRPC error getting doctor consultation details: ", e);
            return ConsultationResponse.newBuilder().setConsultationFee(0.0).build();
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
