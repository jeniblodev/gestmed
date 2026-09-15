package com.gestmed.notification.listener;

import com.gestmed.notification.config.RabbitMQConfig;
import com.gestmed.notification.event.AppointmentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger log =
            LoggerFactory.getLogger(
                    NotificationListener.class
            );

    @RabbitListener(
            queues = RabbitMQConfig.NOTIFICATION_QUEUE
    )
    public void processAppointmentNotification(
            AppointmentEvent event) {

        validateEvent(event);

        log.info(
                "Evento recebido para notificação: " +
                        "eventId={}, tipo={}, versão={}, agendamento={}",
                event.eventId(),
                event.eventType(),
                event.eventVersion(),
                event.appointmentId()
        );

        switch (event.eventType()) {
            case "CREATED" ->
                    processCreated(event);

            case "UPDATED" ->
                    processUpdated(event);

            case "CANCELLED" ->
                    processCancelled(event);

            default ->
                    throw new IllegalArgumentException(
                            "Tipo de evento desconhecido: "
                                    + event.eventType()
                    );
        }

        log.info(
                "Evento de notificação processado com sucesso: {}",
                event.eventId()
        );
    }

    private void processCreated(AppointmentEvent event) {
        log.info(
                "LEMBRETE CRIADO: paciente={}, médico={}, data={}",
                event.patientUsername(),
                event.doctorUsername(),
                event.appointmentDate()
        );
    }

    private void processUpdated(AppointmentEvent event) {
        log.info(
                "LEMBRETE ATUALIZADO: agendamento={}, " +
                        "paciente={}, novaData={}, status={}",
                event.appointmentId(),
                event.patientUsername(),
                event.appointmentDate(),
                event.status()
        );
    }

    private void processCancelled(AppointmentEvent event) {
        log.info(
                "LEMBRETE CANCELADO: agendamento={}, paciente={}",
                event.appointmentId(),
                event.patientUsername()
        );
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

        if (event.appointmentDate() == null) {
            throw new IllegalArgumentException(
                    "A data do agendamento é obrigatória"
            );
        }
    }
}