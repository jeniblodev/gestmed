package com.gestmed.notification.listener;

import com.gestmed.notification.event.AppointmentEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotificationListenerTest {

    private static final UUID EVENT_ID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant OCCURRED_AT =
            Instant.parse("2026-09-15T12:00:00Z");
    private static final LocalDateTime APPOINTMENT_DATE =
            LocalDateTime.of(2026, 10, 20, 14, 30);

    private final NotificationListener listener =
            new NotificationListener();

    @Test
    void processAppointmentNotification_whenEventTypeIsValid_doesNotThrowException() {
        for (String eventType : new String[]{"CREATED", "UPDATED", "CANCELLED"}) {
            assertDoesNotThrow(
                    () -> listener.processAppointmentNotification(
                            validEvent(eventType)
                    )
            );
        }
    }

    @Test
    void processAppointmentNotification_whenEventIsNull_throwsIllegalArgumentException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> listener.processAppointmentNotification(null)
        );
    }

    @Test
    void processAppointmentNotification_whenEventTypeIsUnsupported_throwsIllegalArgumentException() {
        assertThrowsProcessing(validEvent("RESCHEDULED"));
    }

    @Test
    void processAppointmentNotification_whenEventVersionIsUnsupported_throwsIllegalArgumentException() {
        assertThrowsProcessing(validEventWithVersion(2));
    }

    @Test
    void processAppointmentNotification_whenRequiredPayloadIsInvalid_throwsIllegalArgumentException() {
        AppointmentEvent[] invalidEvents = {
                event(null, "CREATED", 1, 10L, "patient",
                        APPOINTMENT_DATE),
                event(EVENT_ID, "CREATED", 1, 10L, "patient",
                        null)
        };

        for (AppointmentEvent event : invalidEvents) {
            assertThrowsProcessing(event);
        }
    }

    private void assertThrowsProcessing(AppointmentEvent event) {
        assertThrows(
                IllegalArgumentException.class,
                () -> listener.processAppointmentNotification(event)
        );
    }

    private static AppointmentEvent validEvent(String eventType) {
        return event(
                EVENT_ID,
                eventType,
                1,
                10L,
                "patient",
                APPOINTMENT_DATE
        );
    }

    private static AppointmentEvent validEventWithVersion(
            int eventVersion) {

        return event(
                EVENT_ID,
                "CREATED",
                eventVersion,
                10L,
                "patient",
                APPOINTMENT_DATE
        );
    }

    private static AppointmentEvent event(
            UUID eventId,
            String eventType,
            int eventVersion,
            Long appointmentId,
            String patientUsername,
            LocalDateTime appointmentDate) {

        return new AppointmentEvent(
                eventId,
                eventType,
                eventVersion,
                OCCURRED_AT,
                appointmentId,
                patientUsername,
                "doctor",
                appointmentDate,
                "SCHEDULED"
        );
    }
}
