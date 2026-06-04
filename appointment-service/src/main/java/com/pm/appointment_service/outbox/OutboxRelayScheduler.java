package com.pm.appointment_service.outbox;

import com.pm.appointment_service.grpc.AppointmentEvent;
import com.pm.appointment_service.model.OutboxEvent;
import com.pm.appointment_service.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelayScheduler.class);
    private static final String TOPIC = "appointment-events";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void processOutbox() {
        List<OutboxEvent> pendingEvents = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc();
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox events to publish to Kafka", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                // Deserialize JSON payload
                Map<String, String> data = objectMapper.readValue(
                        event.getPayload(), 
                        new TypeReference<Map<String, String>>() {}
                );

                String eventType = data.get("eventType");
                String appointmentId = data.get("appointmentId");
                String patientId = data.get("patientId");
                String doctorId = data.get("doctorId");
                String timestamp = data.get("timestamp");
                String departmentId = data.get("departmentId");

                // Construct Protobuf AppointmentEvent
                AppointmentEvent protoEvent = AppointmentEvent.newBuilder()
                        .setEventType(eventType != null ? eventType : "")
                        .setAppointmentId(appointmentId != null ? appointmentId : "")
                        .setPatientId(patientId != null ? patientId : "")
                        .setDoctorId(doctorId != null ? doctorId : "")
                        .setTimestamp(timestamp != null ? timestamp : "")
                        .setDepartmentId(departmentId != null ? departmentId : "")
                        .build();

                // Send to Kafka
                log.info("Relaying event {} to topic {} with key {}", eventType, TOPIC, appointmentId);
                kafkaTemplate.send(TOPIC, appointmentId, protoEvent.toByteArray());

                // Mark as processed
                event.setProcessed(true);
                outboxEventRepository.save(event);

            } catch (Exception e) {
                log.error("Failed to process and relay outbox event: " + event.getId(), e);
                // We don't throw exception here so that it doesn't block other events, 
                // but in a production setup, we might want a retry mechanism or DLQ.
            }
        }
    }
}
