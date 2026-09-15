package com.gestmed.history.service;

import com.gestmed.history.entity.AppointmentHistory;
import com.gestmed.history.entity.ProcessedEvent;
import com.gestmed.history.event.AppointmentEvent;
import com.gestmed.history.repository.AppointmentHistoryRepository;
import com.gestmed.history.repository.ProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HistoryEventServiceTest {

    private static final UUID EVENT_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant OCCURRED_AT =
            Instant.parse("2026-09-15T12:00:00Z");
    private static final Long APPOINTMENT_ID = 20L;
    private static final String PATIENT_USERNAME = "patient";
    private static final String DOCTOR_USERNAME = "doctor";
    private static final LocalDateTime APPOINTMENT_DATE =
            LocalDateTime.of(2026, 10, 20, 14, 30);
    private static final String STATUS = "CONFIRMED";

    @Mock
    private AppointmentHistoryRepository historyRepository;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private HistoryEventService service;

    @ParameterizedTest
    @ValueSource(strings = {"CREATED", "UPDATED", "CANCELLED"})
    void process_whenEventIsNew_savesHistoryAndProcessedEventPreservingEventData(
            String eventType) {

        AppointmentEvent event = event(eventType);
        when(processedEventRepository.existsById(EVENT_ID.toString()))
                .thenReturn(false);

        service.process(event);

        AppointmentHistory savedHistory = captureSavedHistory();
        assertEquals(EVENT_ID.toString(), savedHistory.getEventId());
        assertEquals(eventType, savedHistory.getEventType());
        assertEquals(1, savedHistory.getEventVersion());
        assertEquals(OCCURRED_AT, savedHistory.getOccurredAt());
        assertEquals(APPOINTMENT_ID, savedHistory.getAppointmentId());
        assertEquals(PATIENT_USERNAME, savedHistory.getPatientUsername());
        assertEquals(DOCTOR_USERNAME, savedHistory.getDoctorUsername());
        assertEquals(APPOINTMENT_DATE, savedHistory.getAppointmentDate());
        assertEquals(STATUS, savedHistory.getStatus());

        ProcessedEvent savedProcessedEvent =
                captureSavedProcessedEvent();
        assertEquals(EVENT_ID.toString(),
                savedProcessedEvent.getEventId());
        assertEquals(eventType, savedProcessedEvent.getEventType());
        assertEquals(APPOINTMENT_ID,
                savedProcessedEvent.getAppointmentId());
        assertNotNull(savedProcessedEvent.getProcessedAt());
    }

    @Test
    void process_whenEventWasAlreadyProcessed_doesNotSaveHistoryOrProcessedEvent() {
        AppointmentEvent event = event("UPDATED");
        when(processedEventRepository.existsById(EVENT_ID.toString()))
                .thenReturn(true);

        service.process(event);

        verify(processedEventRepository)
                .existsById(EVENT_ID.toString());
        verify(historyRepository, never())
                .save(any());
        verify(processedEventRepository, never())
                .save(any());
    }

    private AppointmentHistory captureSavedHistory() {
        ArgumentCaptor<AppointmentHistory> captor =
                ArgumentCaptor.forClass(AppointmentHistory.class);

        verify(historyRepository).save(captor.capture());

        return captor.getValue();
    }

    private ProcessedEvent captureSavedProcessedEvent() {
        ArgumentCaptor<ProcessedEvent> captor =
                ArgumentCaptor.forClass(ProcessedEvent.class);

        verify(processedEventRepository).save(captor.capture());

        return captor.getValue();
    }

    private AppointmentEvent event(String eventType) {
        return new AppointmentEvent(
                EVENT_ID,
                eventType,
                1,
                OCCURRED_AT,
                APPOINTMENT_ID,
                PATIENT_USERNAME,
                DOCTOR_USERNAME,
                APPOINTMENT_DATE,
                STATUS
        );
    }
}
