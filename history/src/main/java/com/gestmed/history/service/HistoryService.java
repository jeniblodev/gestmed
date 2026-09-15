package com.gestmed.history.service;

import com.gestmed.history.entity.AppointmentHistory;
import com.gestmed.history.repository.AppointmentHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HistoryService {

    private final AppointmentHistoryRepository repository;

    public HistoryService(
            AppointmentHistoryRepository repository) {

        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AppointmentHistory> findByPatient(
            String patientUsername) {

        return repository
                .findByPatientUsernameOrderByOccurredAtDesc(
                        patientUsername
                );
    }

    @Transactional(readOnly = true)
    public List<AppointmentHistory> findFutureByPatient(
            String patientUsername) {

        return repository
                .findByPatientUsernameAndAppointmentDateAfterOrderByOccurredAtDesc(
                        patientUsername,
                        LocalDateTime.now()
                );
    }

    @Transactional(readOnly = true)
    public List<AppointmentHistory> findAll() {
        return repository.findAllByOrderByOccurredAtDesc();
    }
}