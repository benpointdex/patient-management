package com.patient_service.grpc;

import com.patient_service.repository.PatientRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@GrpcService
public class PatientGrpcServiceImpl extends PatientGrpcServiceGrpc.PatientGrpcServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(PatientGrpcServiceImpl.class);

    @Autowired
    private PatientRepository patientRepository;

    @Override
    public void verifyPatientExists(PatientIdRequest request, StreamObserver<PatientExistsResponse> responseObserver) {
        log.info("verifyPatientExists gRPC call received for patient ID: {}", request.getPatientId());

        try {
            UUID patientId = UUID.fromString(request.getPatientId());
            boolean exists = patientRepository.existsById(patientId);

            PatientExistsResponse response = PatientExistsResponse.newBuilder()
                    .setIsValid(exists)
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format for patient ID: {}", request.getPatientId());
            PatientExistsResponse response = PatientExistsResponse.newBuilder()
                    .setIsValid(false)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error in verifyPatientExists gRPC call: ", e);
            PatientExistsResponse response = PatientExistsResponse.newBuilder()
                    .setIsValid(false)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
