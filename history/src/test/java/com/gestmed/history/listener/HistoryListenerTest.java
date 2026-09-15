package com.gestmed.history.listener;

import com.gestmed.history.event.AppointmentEvent;
import com.gestmed.history.service.HistoryEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class HistoryListenerTest {

    private static final UUID EVENT_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final Instant OCCURRED_AT =
            Instant.parse("2026-09-15T12:00:00Z");
    private static final LocalDateTime APPOINTMENT_DATE =
            LocalDateTime.of(2026, 10, 20, 14, 30);

    @Mock
    private HistoryEventService historyEventService;

    @InjectMocks
    private HistoryListener listener;

    @Test
    void processHistoryRecord_whenEventTypeIsValid_delegatesSameEventToService() {
        for (String eventType : new String[]{"CREATED", "UPDATED", "CANCELLED"}) {
            AppointmentEvent event = validEvent(eventType);

            listener.processHistoryRecord(event);

            verify(historyEventService).process(event);
        }
    }

    @Test
    void processHistoryRecord_whenEventTypeIsUnsupported_throwsIllegalArgumentExceptionAndDoesNotDelegate() {
        assertRejectedWithoutDelegation(
                () -> listener.processHistoryRecord(validEvent("RESCHEDULED"))
        );
    }

    @Test
    void processHistoryRecord_whenEventVersionIsUnsupported_throwsIllegalArgumentExceptionAndDoesNotDelegate() {
        assertRejectedWithoutDelegation(
                () -> listener.processHistoryRecord(validEventWithVersion(2))
        );
    }

    @Test
    void processHistoryRecord_whenEventIsNull_throwsIllegalArgumentExceptionAndDoesNotDelegate() {
        assertRejectedWithoutDelegation(
                () -> listener.processHistoryRecord(null)
        );
    }

    @Test
    void processHistoryRecord_whenRequiredPayloadIsInvalid_throwsIllegalArgumentExceptionAndDoesNotDelegate() {
        AppointmentEvent[] invalidEvents = {
                event(null, "CREATED", 1, OCCURRED_AT, 10L,
                        "patient", "doctor", APPOINTMENT_DATE, "SCHEDULED"),
                event(EVENT_ID, "CREATED", 1, null, 10L,
                        "patient", "doctor", APPOINTMENT_DATE, "SCHEDULED"),
                event(EVENT_ID, "CREATED", 1, OCCURRED_AT, 10L,
                        "patient", "doctor", null, "SCHEDULED"),
                event(EVENT_ID, "CREATED", 1, OCCURRED_AT, 10L,
                        " ", "doctor", APPOINTMENT_DATE, "SCHEDULED")
        };

        for (AppointmentEvent event : invalidEvents) {
            assertRejectedWithoutDelegation(
                    () -> listener.processHistoryRecord(event)
            );
        }
    }

    private void assertRejectedWithoutDelegation(Executable executable) {
        assertThrows(IllegalArgumentException.class, executable);
        verifyNoInteractions(historyEventService);
    }

    private static AppointmentEvent validEvent(String eventType) {
        return event(
                EVENT_ID,
                eventType,
                1,
                OCCURRED_AT,
                10L,
                "patient",
                "doctor",
                APPOINTMENT_DATE,
                "SCHEDULED"
        );
    }

    private static AppointmentEvent validEventWithVersion(
            int eventVersion) {

        return event(
                EVENT_ID,
                "CREATED",
                eventVersion,
                OCCURRED_AT,
                10L,
                "patient",
                "doctor",
                APPOINTMENT_DATE,
                "SCHEDULED"
        );
    }

    private static AppointmentEvent event(
            UUID eventId,
            String eventType,
            int eventVersion,
            Instant occurredAt,
            Long appointmentId,
            String patientUsername,
            String doctorUsername,
            LocalDateTime appointmentDate,
            String status) {

        return new AppointmentEvent(
                eventId,
                eventType,
                eventVersion,
                occurredAt,
                appointmentId,
                patientUsername,
                doctorUsername,
                appointmentDate,
                status
        );
    }
}
