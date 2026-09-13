package com.gestmed.scheduling.service;

import com.gestmed.scheduling.config.RabbitMQConfig;
import com.gestmed.scheduling.entity.Appointment;
import com.gestmed.scheduling.repository.AppointmentRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final RabbitTemplate rabbitTemplate;

    public AppointmentService(AppointmentRepository appointmentRepository, RabbitTemplate rabbitTemplate) {
        this.appointmentRepository = appointmentRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    public Appointment createAppointment(Appointment appointment) {
        appointment.setStatus("SCHEDULED");
        Appointment savedAppointment = appointmentRepository.save(appointment);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                savedAppointment
        );

        return savedAppointment;
    }

    public List<Appointment> getAppointmentsByPatient(String patientUsername) {
        return appointmentRepository.findByPatientUsername(patientUsername);
    }

    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    public List<Appointment> getPatientAppointments(String currentUsername) {
        return appointmentRepository.findByPatientUsername(currentUsername);
    }
}
