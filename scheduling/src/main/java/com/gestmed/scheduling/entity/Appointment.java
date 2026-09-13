package com.gestmed.scheduling.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String patientUsername;
    private String doctorUsername;
    private LocalDateTime appointmentDate;
    private String status;

    public void setStatus(String status) {
        this.status = status;
    }


    public void setPatientUsername(String patientUsername) {
        this.patientUsername = patientUsername;
    }

    public void setDoctorUsername(String doctorUsername) {
        this.doctorUsername = doctorUsername;
    }

    public void setAppointmentDate(LocalDateTime appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

}
