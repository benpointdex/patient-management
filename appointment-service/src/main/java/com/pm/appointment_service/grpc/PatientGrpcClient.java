package com.pm.appointment_service.grpc;

import com.patient_service.grpc.PatientExistsResponse;
import com.patient_service.grpc.PatientGrpcServiceGrpc;
import com.patient_service.grpc.PatientIdRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

@Service
public class PatientGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(PatientGrpcClient.class);

    private final ManagedChannel channel;
    private final PatientGrpcServiceGrpc.PatientGrpcServiceBlockingStub blockingStub;

    public PatientGrpcClient(
            @Value("${patient.grpc.host:patient-service}") String serverAddress,
            @Value("${patient.grpc.port:9005}") int serverPort) {

        log.info("Connecting to Patient Service gRPC at {}:{}", serverAddress, serverPort);

        this.channel = ManagedChannelBuilder.forAddress(serverAddress, serverPort)
                .usePlaintext()
                .build();

        this.blockingStub = PatientGrpcServiceGrpc.newBlockingStub(channel);
    }

    public boolean verifyPatient(String patientId) {
        log.info("gRPC check verifyPatient for patientId: {}", patientId);
        try {
            PatientIdRequest request = PatientIdRequest.newBuilder()
                    .setPatientId(patientId)
                    .build();
            PatientExistsResponse response = blockingStub.verifyPatientExists(request);
            return response.getIsValid();
        } catch (Exception e) {
            log.error("gRPC error verifying patient existence: ", e);
            return false;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            log.info("Shutting down Patient Service gRPC channel");
            channel.shutdown();
        }
    }
}
