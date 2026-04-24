package com.pm.billing_serivce.grpc;

import billing.BillingRequest;
import billing.BillingResponse;
import billing.BillingServiceGrpc.BillingServiceImplBase;
import com.pm.billing_serivce.kafka.BillingKafkaProducer;
import com.pm.billing_serivce.model.BillingAccount;
import com.pm.billing_serivce.repository.BillingRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@GrpcService
@RequiredArgsConstructor
public class BillingGrpcService extends BillingServiceImplBase {

    private final BillingRepository billingRepository;

    private static final Logger log = LoggerFactory.getLogger(
            BillingGrpcService.class);

    private final BillingKafkaProducer billingKafkaProducer;

    @Override
    public void createBillingAccount(BillingRequest billingRequest,
                                     StreamObserver<BillingResponse> responseObserver) {

        log.info("createBillingAccount request received for patient: {}", billingRequest.getPatientId());

        BillingAccount billingAccount = BillingAccount.builder()
                .patientId(billingRequest.getPatientId())
                .name(billingRequest.getName())
                .email(billingRequest.getEmail())
                .status("ACTIVE")
                .build();

        BillingAccount savedAccount = billingRepository.save(billingAccount);
        billingKafkaProducer.sendBillingEvent(savedAccount,"BILLING_CREATED");
        BillingResponse response = BillingResponse.newBuilder()
                .setAccountId(savedAccount.getAccountId().toString())
                .setStatus(savedAccount.getStatus())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}