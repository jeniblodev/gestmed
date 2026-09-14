package com.gestmed.scheduling.repository;

import com.gestmed.scheduling.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientUsername(String patientUsername);
    List<Appointment> findByPatientUsernameAndAppointmentDateAfter(String patientUsername, LocalDateTime appointmentDate);

    boolean existsByDoctorUsernameAndAppointmentDate(
            String doctorUsername,
            LocalDateTime appointmentDate
    );

    boolean existsByPatientUsernameAndAppointmentDate(
            String patientUsername,
            LocalDateTime appointmentDate
    );

    boolean existsByDoctorUsernameAndAppointmentDateAndIdNot(
            String doctorUsername,
            LocalDateTime appointmentDate,
            Long id
    );

    boolean existsByPatientUsernameAndAppointmentDateAndIdNot(
            String patientUsername,
            LocalDateTime appointmentDate,
            Long id
    );

}
