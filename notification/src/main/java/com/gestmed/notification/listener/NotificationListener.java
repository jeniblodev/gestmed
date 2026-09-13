package com.gestmed.notification.listener;

import com.gestmed.notification.config.RabbitMQConfig;
import com.gestmed.notification.dto.AppointmentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationListener.class);

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void processAppointmentNotification(AppointmentMessage message) {
        logger.info("--- Novo Evento Recebido ---");
        logger.info("Preparando para enviar lembrete ao paciente: {}", message.patientUsername());
        logger.info("Médico responsável: {}", message.doctorUsername());
        logger.info("Data do agendamento: {}", message.appointmentDate());

        logger.info("SUCESSO: lembrete enviado com sucesso para {}", message.patientUsername());
    }
}
