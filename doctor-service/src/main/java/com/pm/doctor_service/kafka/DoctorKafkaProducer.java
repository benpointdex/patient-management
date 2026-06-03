package com.pm.doctor_service.kafka;

import com.pm.doctor_service.model.Doctor;
import doctor.events.DoctorEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class DoctorKafkaProducer {


    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private static final String TOPIC = "doctor-events";


    public DoctorKafkaProducer(KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendDoctorEvent(Doctor doctor, String eventType) {
      try { DoctorEvent event = DoctorEvent.newBuilder()
              .setDoctorId(doctor.getDoctorId().toString())
              .setFullName(doctor.getFullName() != null ? doctor.getFullName() : "")
              .setDepartmentName(doctor.getDepartment() != null ? doctor.getDepartment().getDepartmentName() : "")
              .setSpecializationName(doctor.getSpecialization() != null ? doctor.getSpecialization().getName() : "")
              .setStatus(doctor.getStatus().name())
              .setAvailabilityStatus(doctor.getAvailabilityStatus().name())
              .setConsultationFee(doctor.getConsultationFee() != null ? doctor.getConsultationFee().doubleValue() : 0.0)
              .setEventType(eventType)
              .setTimestamp(Instant.now().toString())
              .build();


        log.info("Sending Doctor Event to Kafka: [ doctorId={}, eventType={}, status={} ]",
                event.getDoctorId(), event.getEventType(), event.getStatus());
        kafkaTemplate.send(TOPIC, doctor.getDoctorId().toString(), event.toByteArray());
    } catch (Exception e) {
        log.error("Failed to send Doctor Event to Kafka", e);
    }
}
}
