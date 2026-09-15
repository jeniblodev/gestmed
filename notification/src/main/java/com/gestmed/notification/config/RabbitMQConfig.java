package com.gestmed.notification.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "hospital.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "hospital.dlx";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_DLQ = "notification.dlq";
    public static final String ROUTING_PATTERN = "appointment.*";
    public static final String DEAD_LETTER_ROUTING_KEY = "notification.failed";

    @Bean
    public TopicExchange hospitalExchange() {
        return new TopicExchange(
                EXCHANGE_NAME,
                true,
                false
        );
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(
                DEAD_LETTER_EXCHANGE,
                true,
                false
        );
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue notificationDeadLetterQueue() {
        return QueueBuilder
                .durable(NOTIFICATION_DLQ)
                .build();
    }

    @Bean
    public Binding notificationBinding(
            Queue notificationQueue,
            TopicExchange hospitalExchange) {

        return BindingBuilder
                .bind(notificationQueue)
                .to(hospitalExchange)
                .with(ROUTING_PATTERN);
    }

    @Bean
    public Binding notificationDeadLetterBinding(
            Queue notificationDeadLetterQueue,
            DirectExchange deadLetterExchange) {

        return BindingBuilder
                .bind(notificationDeadLetterQueue)
                .to(deadLetterExchange)
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}