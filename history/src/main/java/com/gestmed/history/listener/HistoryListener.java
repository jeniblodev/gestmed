package com.gestmed.history.listener;

import com.gestmed.history.config.RabbitMQConfig;
import com.gestmed.history.event.AppointmentEvent;
import com.gestmed.history.service.HistoryEventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class HistoryListener {

    private static final Logger log =
            LoggerFactory.getLogger(HistoryListener.class);

    private static final int SUPPORTED_EVENT_VERSION = 1;

    private static final Set<String> SUPPORTED_EVENT_TYPES =
            Set.of(
                    "CREATED",
                    "UPDATED",
                    "CANCELLED"
            );

    private final HistoryEventService historyEventService;

    public HistoryListener(
            HistoryEventService historyEventService) {

        this.historyEventService = historyEventService;
    }

    @RabbitListener(
            queues = RabbitMQConfig.HISTORY_QUEUE
    )
    public void processHistoryRecord(
            AppointmentEvent event) {

        validateEvent(event);

        log.info(
                "Evento recebido pelo histórico: " +
                        "eventId={}, tipo={}, agendamento={}",
                event.eventId(),
                event.eventType(),
                event.appointmentId()
        );

        historyEventService.process(event);
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

        if (event.eventType() == null
                || event.eventType().isBlank()) {

            throw new IllegalArgumentException(
                    "O tipo do evento é obrigatório"
            );
        }

        if (!SUPPORTED_EVENT_TYPES.contains(
                event.eventType())) {

            throw new IllegalArgumentException(
                    "Tipo de evento não suportado: "
                            + event.eventType()
            );
        }

        if (event.eventVersion()
                != SUPPORTED_EVENT_VERSION) {

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