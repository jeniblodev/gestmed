package com.gestmed.history.service;

import com.gestmed.history.entity.AppointmentHistory;
import com.gestmed.history.repository.AppointmentHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoryService {

    private final AppointmentHistoryRepository repository;

    public HistoryService(AppointmentHistoryRepository repository) {
        this.repository = repository;
    }

    public List<AppointmentHistory> findByPatient(
            String patientUsername) {

        return repository.findByPatientUsername(patientUsername);
    }

    public List<AppointmentHistory> findFutureByPatient(
            String patientUsername) {

        return repository
                .findByPatientUsernameAndAppointmentDateAfter(
                        patientUsername,
                        LocalDateTime.now()
                );
    }

    public List<AppointmentHistory> findAll() {
        return repository.findAll();
    }
}
