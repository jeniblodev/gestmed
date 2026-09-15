package com.gestmed.history.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_history")
public class AppointmentHistory {

    @Id
    @Column(name = "id", nullable = false)
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

    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private String status;

    public AppointmentHistory() {
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

    public String getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPatientUsername(
            String patientUsername) {

        this.patientUsername = patientUsername;
    }

    public void setDoctorUsername(
            String doctorUsername) {

        this.doctorUsername = doctorUsername;
    }

    public void setAppointmentDate(
            LocalDateTime appointmentDate) {

        this.appointmentDate = appointmentDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}