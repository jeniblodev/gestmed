package com.gestmed.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NotificationApplication {

	private static final Logger log =
			LoggerFactory.getLogger(
					NotificationApplication.class
			);

	public static void main(String[] args) {
		SpringApplication.run(
				NotificationApplication.class,
				args
		);
	}

	@Bean
	CommandLineRunner notificationStartupLogger() {
		return args -> log.info(
				"Serviço de notificações iniciado. " +
						"Aguardando eventos de agendamento no RabbitMQ."
		);
	}
}