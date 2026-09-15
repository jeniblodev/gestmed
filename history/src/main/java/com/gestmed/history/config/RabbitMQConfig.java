package com.gestmed.history.config;

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
    public static final String HISTORY_QUEUE = "history.queue";
    public static final String HISTORY_DLQ = "history.dlq";
    public static final String ROUTING_PATTERN = "appointment.*";
    public static final String DEAD_LETTER_ROUTING_KEY = "history.failed";

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
    public Queue historyQueue() {
        return QueueBuilder
                .durable(HISTORY_QUEUE)
                .deadLetterExchange(DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue historyDeadLetterQueue() {
        return QueueBuilder
                .durable(HISTORY_DLQ)
                .build();
    }

    @Bean
    public Binding historyBinding(
            Queue historyQueue,
            TopicExchange hospitalExchange) {

        return BindingBuilder
                .bind(historyQueue)
                .to(hospitalExchange)
                .with(ROUTING_PATTERN);
    }

    @Bean
    public Binding historyDeadLetterBinding(
            Queue historyDeadLetterQueue,
            DirectExchange deadLetterExchange) {

        return BindingBuilder
                .bind(historyDeadLetterQueue)
                .to(deadLetterExchange)
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}