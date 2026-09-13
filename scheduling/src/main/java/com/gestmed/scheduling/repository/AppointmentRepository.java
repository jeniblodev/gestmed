package com.gestmed.scheduling.repository;

import com.gestmed.scheduling.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientUsername(String patientUsername);
}
