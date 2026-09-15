package com.gestmed.scheduling.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "hospital.exchange";
    public static final String CREATED_ROUTING_KEY = "appointment.created";
    public static final String UPDATED_ROUTING_KEY = "appointment.updated";
    public static final String CANCELLED_ROUTING_KEY = "appointment.cancelled";

    @Bean
    public TopicExchange hospitalExchange() {
        return new TopicExchange(
                EXCHANGE_NAME,
                true,
                false
        );
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}