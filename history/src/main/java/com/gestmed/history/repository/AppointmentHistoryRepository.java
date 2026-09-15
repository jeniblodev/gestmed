package com.gestmed.history.repository;

import com.gestmed.history.entity.AppointmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentHistoryRepository
        extends JpaRepository<AppointmentHistory, Long> {

    List<AppointmentHistory> findByPatientUsernameOrderByOccurredAtDesc(
            String patientUsername
    );

    List<AppointmentHistory> findByPatientUsernameAndAppointmentDateAfterOrderByOccurredAtDesc(
            String patientUsername,
            LocalDateTime appointmentDate
    );

    List<AppointmentHistory> findAllByOrderByOccurredAtDesc();
}