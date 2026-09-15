package com.gestmed.scheduling.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "patient_username",
            nullable = false,
            length = 100
    )
    private String patientUsername;

    @Column(
            name = "doctor_username",
            nullable = false,
            length = 100
    )
    private String doctorUsername;

    @Column(
            name = "appointment_date",
            nullable = false
    )
    private LocalDateTime appointmentDate;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private AppointmentStatus status;

    public Appointment() {
    }

    public Long getId() {
        return id;
    }

    public String getPatientUsername() {
        return patientUsername;
    }

    public String getDoctorUsername() {
        return doctorUsername;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setPatientUsername(String patientUsername) {
        this.patientUsername = patientUsername;
    }

    public void setDoctorUsername(String doctorUsername) {
        this.doctorUsername = doctorUsername;
    }

    public void setAppointmentDate(
            LocalDateTime appointmentDate) {

        this.appointmentDate = appointmentDate;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
}
