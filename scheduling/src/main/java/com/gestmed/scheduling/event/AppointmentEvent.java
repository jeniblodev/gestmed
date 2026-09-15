package com.gestmed.scheduling.event;

import com.gestmed.scheduling.entity.Appointment;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        Instant occurredAt,
        Long appointmentId,
        String patientUsername,
        String doctorUsername,
        LocalDateTime appointmentDate,
        String status
) {

    private static final int CURRENT_VERSION = 1;

    public static AppointmentEvent from(
            Appointment appointment,
            AppointmentEventType eventType) {

        return new AppointmentEvent(
                UUID.randomUUID(),
                eventType.name(),
                CURRENT_VERSION,
                Instant.now(),
                appointment.getId(),
                appointment.getPatientUsername(),
                appointment.getDoctorUsername(),
                appointment.getAppointmentDate(),
                appointment.getStatus().name()
        );
    }
}
