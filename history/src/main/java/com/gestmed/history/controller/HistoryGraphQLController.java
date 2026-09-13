package com.gestmed.history.controller;

import com.gestmed.history.entity.AppointmentHistory;
import com.gestmed.history.repository.AppointmentHistoryRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class HistoryGraphQLController {

    private final AppointmentHistoryRepository repository;

    public HistoryGraphQLController(AppointmentHistoryRepository repository) {
        this.repository = repository;
    }

    @QueryMapping
    public List<AppointmentHistory> getPatientHistory(@Argument String patientUsername) {
        return repository.findByPatientUsername(patientUsername);
    }

    @QueryMapping
    public List<AppointmentHistory> getAllHistory() {
        return repository.findAll();
    }
}
