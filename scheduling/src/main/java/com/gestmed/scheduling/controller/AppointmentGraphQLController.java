package com.gestmed.scheduling.controller;

import com.gestmed.scheduling.entity.Appointment;
import com.gestmed.scheduling.exception.InvalidAppointmentException;
import com.gestmed.scheduling.service.AppointmentService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import com.gestmed.scheduling.dto.AppointmentInput;
import com.gestmed.scheduling.dto.UpdateAppointmentInput;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AppointmentGraphQLController {

    private final AppointmentService appointmentService;

    public AppointmentGraphQLController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @QueryMapping
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('NURSE')")
    public List<Appointment> getPatientAppointments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        return appointmentService.getPatientAppointments(currentUsername);
    }

    @QueryMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public List<Appointment> getAllAppointments() {
        return appointmentService.getAllAppointments();
    }

    @QueryMapping
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('NURSE')")
    public List<Appointment> getPatientFutureAppointments() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        return appointmentService.getFutureAppointmentByPatient(currentUsername);
    }

    @MutationMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public Appointment createAppointment(@Argument @Valid AppointmentInput input) {
        Appointment appointment = new Appointment();
        appointment.setPatientUsername(input.patientUsername());
        appointment.setDoctorUsername(input.doctorUsername());
        appointment.setAppointmentDate(parseAppointmentDate(input.appointmentDate()));

        return appointmentService.createAppointment(appointment);
    }

    @MutationMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public Appointment updateAppointment(@Argument @Valid UpdateAppointmentInput input) {
        return appointmentService.updateAppointment(
                input.id(),
                parseAppointmentDate(input.appointmentDate()),
                input.status());
    }

    private LocalDateTime parseAppointmentDate(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidAppointmentException(
                    "A data da consulta é obrigatória"
            );
        }

        return LocalDateTime.parse(value);
    }
}