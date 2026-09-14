package com.gestmed.history.controller;

import com.gestmed.history.entity.AppointmentHistory;
import com.gestmed.history.service.HistoryService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class HistoryGraphQLController {

    private final HistoryService historyService;

    public HistoryGraphQLController(
            HistoryService historyService) {

        this.historyService = historyService;
    }

    @QueryMapping
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentHistory> getMyHistory(
            Authentication authentication) {

        return historyService.findByPatient(
                authentication.getName()
        );
    }

    @QueryMapping
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentHistory> getMyFutureHistory(
            Authentication authentication) {

        return historyService.findFutureByPatient(
                authentication.getName()
        );
    }

    @QueryMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public List<AppointmentHistory> getPatientHistory(
            @Argument String patientUsername) {

        return historyService.findByPatient(patientUsername);
    }

    @QueryMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public List<AppointmentHistory> getPatientFutureHistory(
            @Argument String patientUsername) {

        return historyService.findFutureByPatient(
                patientUsername
        );
    }

    @QueryMapping
    @PreAuthorize("hasRole('DOCTOR') or hasRole('NURSE')")
    public List<AppointmentHistory> getAllHistory() {
        return historyService.findAll();
    }
}