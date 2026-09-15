package com.gestmed.history.service;

import com.gestmed.history.entity.AppointmentHistory;
import com.gestmed.history.entity.ProcessedEvent;
import com.gestmed.history.event.AppointmentEvent;
import com.gestmed.history.repository.AppointmentHistoryRepository;
import com.gestmed.history.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HistoryEventService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    HistoryEventService.class
            );

    private final AppointmentHistoryRepository historyRepository;
    private final ProcessedEventRepository processedEventRepository;

    public HistoryEventService(
            AppointmentHistoryRepository historyRepository,
            ProcessedEventRepository processedEventRepository) {

        this.historyRepository = historyRepository;
        this.processedEventRepository =
                processedEventRepository;
    }

    @Transactional
    public void process(AppointmentEvent event) {
        String eventId = event.eventId().toString();

        if (processedEventRepository.existsById(eventId)) {
            log.info(
                    "Evento já processado. Ignorando duplicata: {}",
                    eventId
            );

            return;
        }

        AppointmentHistory history =
                historyRepository
                        .findById(event.appointmentId())
                        .orElseGet(AppointmentHistory::new);

        history.setId(event.appointmentId());
        history.setPatientUsername(
                event.patientUsername()
        );
        history.setDoctorUsername(
                event.doctorUsername()
        );
        history.setAppointmentDate(
                event.appointmentDate()
        );
        history.setStatus(event.status());

        historyRepository.save(history);

        ProcessedEvent processedEvent =
                new ProcessedEvent(
                        event.eventId(),
                        event.eventType(),
                        event.appointmentId()
                );

        processedEventRepository.save(processedEvent);

        log.info(
                "Evento processado e registrado: " +
                        "eventId={}, agendamento={}, tipo={}",
                event.eventId(),
                event.appointmentId(),
                event.eventType()
        );
    }
}