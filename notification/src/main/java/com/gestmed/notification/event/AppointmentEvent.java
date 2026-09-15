package com.gestmed.notification.event;

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
}