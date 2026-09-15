package com.gestmed.scheduling.event;

import com.gestmed.scheduling.config.RabbitMQConfig;
import com.gestmed.scheduling.entity.Appointment;
import com.gestmed.scheduling.entity.AppointmentStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppointmentEventPublisherTest {

    private static final Long APPOINTMENT_ID = 42L;
    private static final String PATIENT_USERNAME = "patient";
    private static final String DOCTOR_USERNAME = "doctor";
    private static final LocalDateTime APPOINTMENT_DATE =
            LocalDateTime.of(2026, 10, 20, 14, 30);

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AppointmentEventPublisher publisher;

    @Test
    void publishCreated_sendsCreatedEventWithAppointmentData() {
        assertPublishedEvent(
                publisher::publishCreated,
                RabbitMQConfig.CREATED_ROUTING_KEY,
                AppointmentEventType.CREATED
        );
    }

    @Test
    void publishUpdated_sendsUpdatedEventWithAppointmentData() {
        assertPublishedEvent(
                publisher::publishUpdated,
                RabbitMQConfig.UPDATED_ROUTING_KEY,
                AppointmentEventType.UPDATED
        );
    }

    @Test
    void publishCancelled_sendsCancelledEventWithAppointmentData() {
        assertPublishedEvent(
                publisher::publishCancelled,
                RabbitMQConfig.CANCELLED_ROUTING_KEY,
                AppointmentEventType.CANCELLED
        );
    }

    private void assertPublishedEvent(
            Consumer<Appointment> publishAction,
            String expectedRoutingKey,
            AppointmentEventType expectedEventType) {

        Appointment appointment = appointment();

        publishAction.accept(appointment);

        ArgumentCaptor<AppointmentEvent> eventCaptor =
                ArgumentCaptor.forClass(AppointmentEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE_NAME),
                eq(expectedRoutingKey),
                eventCaptor.capture()
        );

        AppointmentEvent event = eventCaptor.getValue();
        assertEquals(expectedEventType.name(), event.eventType());
        assertEquals(1, event.eventVersion());
        assertEquals(APPOINTMENT_ID, event.appointmentId());
        assertEquals(PATIENT_USERNAME, event.patientUsername());
        assertEquals(DOCTOR_USERNAME, event.doctorUsername());
        assertEquals(APPOINTMENT_DATE, event.appointmentDate());
        assertEquals(AppointmentStatus.CONFIRMED.name(), event.status());
    }

    private Appointment appointment() {
        Appointment appointment = new Appointment();
        ReflectionTestUtils.setField(appointment, "id", APPOINTMENT_ID);
        appointment.setPatientUsername(PATIENT_USERNAME);
        appointment.setDoctorUsername(DOCTOR_USERNAME);
        appointment.setAppointmentDate(APPOINTMENT_DATE);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointment;
    }
}
