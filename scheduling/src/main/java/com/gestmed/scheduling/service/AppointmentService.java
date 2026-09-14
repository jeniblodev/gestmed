package com.gestmed.scheduling.service;

import com.gestmed.scheduling.config.RabbitMQConfig;
import com.gestmed.scheduling.entity.Appointment;
import com.gestmed.scheduling.entity.AppointmentStatus;
import com.gestmed.scheduling.entity.User;
import com.gestmed.scheduling.exception.AppointmentNotFoundException;
import com.gestmed.scheduling.exception.InvalidAppointmentException;
import com.gestmed.scheduling.exception.ScheduleConflictException;
import com.gestmed.scheduling.exception.UserNotFoundException;
import com.gestmed.scheduling.repository.AppointmentRepository;
import com.gestmed.scheduling.repository.UserRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final RabbitTemplate rabbitTemplate;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            UserRepository userRepository,
            RabbitTemplate rabbitTemplate) {

        this.appointmentRepository = appointmentRepository;
        this.userRepository = userRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public Appointment createAppointment(
            Appointment appointment) {

        validateCreation(appointment);

        appointment.setStatus(AppointmentStatus.SCHEDULED);

        Appointment savedAppointment =
                appointmentRepository.save(appointment);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                savedAppointment
        );

        return savedAppointment;
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Appointment> getPatientAppointments(
            String username) {

        return appointmentRepository
                .findByPatientUsername(username);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getFutureAppointmentByPatient(
            String username) {

        return appointmentRepository
                .findByPatientUsernameAndAppointmentDateAfter(
                        username,
                        LocalDateTime.now()
                );
    }

    @Transactional
    public Appointment updateAppointment(
            Long id,
            LocalDateTime newDate,
            AppointmentStatus newStatus) {

        Appointment appointment = appointmentRepository
                .findById(id)
                .orElseThrow(() ->
                        new AppointmentNotFoundException(id)
                );

        validateUpdate(
                appointment,
                newDate,
                newStatus
        );

        appointment.setAppointmentDate(newDate);
        appointment.setStatus(newStatus);

        Appointment updatedAppointment =
                appointmentRepository.save(appointment);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                updatedAppointment
        );

        return updatedAppointment;
    }

    private void validateCreation(Appointment appointment) {
        if (appointment.getAppointmentDate() == null) {
            throw new InvalidAppointmentException(
                    "A data da consulta é obrigatória"
            );
        }

        if (!appointment.getAppointmentDate()
                .isAfter(LocalDateTime.now())) {

            throw new InvalidAppointmentException(
                    "A consulta deve ser agendada para uma data futura"
            );
        }

        User patient = findUser(
                appointment.getPatientUsername()
        );

        if (!patient.hasRole("ROLE_PATIENT")) {
            throw new InvalidAppointmentException(
                    "O usuário informado não possui o perfil PATIENT"
            );
        }

        User doctor = findUser(
                appointment.getDoctorUsername()
        );

        if (!doctor.hasRole("ROLE_DOCTOR")) {
            throw new InvalidAppointmentException(
                    "O usuário informado não possui o perfil DOCTOR"
            );
        }

        validateConflicts(
                appointment.getPatientUsername(),
                appointment.getDoctorUsername(),
                appointment.getAppointmentDate()
        );
    }

    private void validateUpdate(
            Appointment appointment,
            LocalDateTime newDate,
            AppointmentStatus newStatus) {

        if (newDate == null) {
            throw new InvalidAppointmentException(
                    "A nova data é obrigatória"
            );
        }

        if (newStatus == null) {
            throw new InvalidAppointmentException(
                    "O novo status é obrigatório"
            );
        }

        if (newDate.isBefore(LocalDateTime.now())
                && newStatus != AppointmentStatus.COMPLETED
                && newStatus != AppointmentStatus.CANCELLED) {

            throw new InvalidAppointmentException(
                    "Uma consulta ativa não pode ser movida para o passado"
            );
        }

        validateStatusTransition(
                appointment.getStatus(),
                newStatus
        );

        boolean doctorConflict =
                appointmentRepository
                        .existsByDoctorUsernameAndAppointmentDateAndIdNot(
                                appointment.getDoctorUsername(),
                                newDate,
                                appointment.getId()
                        );

        if (doctorConflict) {
            throw new ScheduleConflictException(
                    "O médico já possui uma consulta nesse horário"
            );
        }

        boolean patientConflict =
                appointmentRepository
                        .existsByPatientUsernameAndAppointmentDateAndIdNot(
                                appointment.getPatientUsername(),
                                newDate,
                                appointment.getId()
                        );

        if (patientConflict) {
            throw new ScheduleConflictException(
                    "O paciente já possui uma consulta nesse horário"
            );
        }
    }

    private void validateConflicts(
            String patientUsername,
            String doctorUsername,
            LocalDateTime date) {

        boolean doctorConflict =
                appointmentRepository
                        .existsByDoctorUsernameAndAppointmentDate(
                                doctorUsername,
                                date
                        );

        if (doctorConflict) {
            throw new ScheduleConflictException(
                    "O médico já possui uma consulta nesse horário"
            );
        }

        boolean patientConflict =
                appointmentRepository
                        .existsByPatientUsernameAndAppointmentDate(
                                patientUsername,
                                date
                        );

        if (patientConflict) {
            throw new ScheduleConflictException(
                    "O paciente já possui uma consulta nesse horário"
            );
        }
    }

    private void validateStatusTransition(
            AppointmentStatus currentStatus,
            AppointmentStatus newStatus) {

        if (currentStatus == AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentException(
                    "Uma consulta concluída não pode ser alterada"
            );
        }

        if (currentStatus == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentException(
                    "Uma consulta cancelada não pode ser alterada"
            );
        }

        if (currentStatus == AppointmentStatus.SCHEDULED
                && newStatus == AppointmentStatus.COMPLETED) {

            throw new InvalidAppointmentException(
                    "Uma consulta agendada deve ser confirmada antes de ser concluída"
            );
        }
    }

    private User findUser(String username) {
        if (username == null || username.isBlank()) {
            throw new InvalidAppointmentException(
                    "O username é obrigatório"
            );
        }

        return userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UserNotFoundException(username)
                );
    }
}