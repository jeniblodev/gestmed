package com.gestmed.history.listener;

import com.gestmed.history.config.RabbitMQConfig;
import com.gestmed.history.entity.AppointmentHistory;
import com.gestmed.history.event.AppointmentEvent;
import com.gestmed.history.repository.AppointmentHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class HistoryListener {

    private static final Logger log =
            LoggerFactory.getLogger(HistoryListener.class);

    private final AppointmentHistoryRepository repository;

    public HistoryListener(
            AppointmentHistoryRepository repository) {

        this.repository = repository;
    }

    @RabbitListener(
            queues = RabbitMQConfig.HISTORY_QUEUE
    )
    public void processHistoryRecord(
            AppointmentEvent event) {

        validateEvent(event);

        log.info(
                "Atualizando histórico: eventId={}, tipo={}, " +
                        "agendamento={}",
                event.eventId(),
                event.eventType(),
                event.appointmentId()
        );

        AppointmentHistory history =
                repository
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

        repository.save(history);
    }

    private void validateEvent(AppointmentEvent event) {
        if (event == null) {
            throw new IllegalArgumentException(
                    "O evento recebido não pode ser nulo"
            );
        }

        if (event.eventId() == null) {
            throw new IllegalArgumentException(
                    "O eventId é obrigatório"
            );
        }

        if (event.eventVersion() != 1) {
            throw new IllegalArgumentException(
                    "Versão de evento não suportada: "
                            + event.eventVersion()
            );
        }

        if (event.appointmentId() == null) {
            throw new IllegalArgumentException(
                    "O ID do agendamento é obrigatório"
            );
        }

        if (event.patientUsername() == null
                || event.patientUsername().isBlank()) {

            throw new IllegalArgumentException(
                    "O paciente é obrigatório"
            );
        }

        if (event.doctorUsername() == null
                || event.doctorUsername().isBlank()) {

            throw new IllegalArgumentException(
                    "O médico é obrigatório"
            );
        }

        if (event.appointmentDate() == null) {
            throw new IllegalArgumentException(
                    "A data do agendamento é obrigatória"
            );
        }

        if (event.status() == null
                || event.status().isBlank()) {

            throw new IllegalArgumentException(
                    "O status é obrigatório"
            );
        }
    }
}