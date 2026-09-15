package com.gestmed.scheduling.event;

import com.gestmed.scheduling.config.RabbitMQConfig;
import com.gestmed.scheduling.entity.Appointment;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class AppointmentEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public AppointmentEventPublisher(
            RabbitTemplate rabbitTemplate) {

        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishCreated(Appointment appointment) {
        publish(
                appointment,
                AppointmentEventType.CREATED,
                RabbitMQConfig.CREATED_ROUTING_KEY
        );
    }

    public void publishUpdated(Appointment appointment) {
        publish(
                appointment,
                AppointmentEventType.UPDATED,
                RabbitMQConfig.UPDATED_ROUTING_KEY
        );
    }

    public void publishCancelled(Appointment appointment) {
        publish(
                appointment,
                AppointmentEventType.CANCELLED,
                RabbitMQConfig.CANCELLED_ROUTING_KEY
        );
    }

    private void publish(
            Appointment appointment,
            AppointmentEventType eventType,
            String routingKey) {

        AppointmentEvent event =
                AppointmentEvent.from(
                        appointment,
                        eventType
                );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                routingKey,
                event
        );
    }
}