package com.pm.appointment_service.kafka;

import com.pm.appointment_service.service.AppointmentService;
import doctor.events.DoctorEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class DoctorEventListener {

    private static final Logger log = LoggerFactory.getLogger(DoctorEventListener.class);

    private final AppointmentService appointmentService;

    public DoctorEventListener(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @KafkaListener(
            topics = "doctor-events",
            groupId = "appointment-service-group"
    )
    public void onDoctorEvent(byte[] message) {
        try {
            DoctorEvent event = DoctorEvent.parseFrom(message);
            log.info("Received DoctorEvent from Kafka: doctorId={}, status={}, eventType={}", 
                    event.getDoctorId(), event.getStatus(), event.getEventType());

            String status = event.getStatus();
            if ("ON_LEAVE".equalsIgnoreCase(status) || "INACTIVE".equalsIgnoreCase(status)) {
                log.warn("Doctor {} status changed to {}. Canceling all upcoming appointments.", 
                        event.getDoctorId(), status);
                
                UUID doctorId = UUID.fromString(event.getDoctorId());
                // Cancel all scheduled appointments from today up to 1 year in the future
                LocalDate startDate = LocalDate.now();
                LocalDate endDate = startDate.plusYears(1);
                
                appointmentService.handleDoctorLeaveProcess(doctorId, startDate, endDate);
            }
        } catch (Exception e) {
            log.error("Failed to process DoctorEvent from Kafka", e);
        }
    }
}
