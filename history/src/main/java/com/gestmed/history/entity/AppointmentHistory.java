package com.gestmed.history.entity;

import com.gestmed.history.event.AppointmentEvent;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_history_events")
public class AppointmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long id;

    @Column(
            name = "event_id",
            nullable = false,
            unique = true,
            length = 36
    )
    private String eventId;

    @Column(
            name = "event_type",
            nullable = false,
            length = 30
    )
    private String eventType;

    @Column(
            name = "event_version",
            nullable = false
    )
    private int eventVersion;

    @Column(
            name = "occurred_at",
            nullable = false
    )
    private Instant occurredAt;

    @Column(
            name = "appointment_id",
            nullable = false
    )
    private Long appointmentId;

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

    protected AppointmentHistory() {
    }

    public AppointmentHistory(AppointmentEvent event) {
        this.eventId = event.eventId().toString();
        this.eventType = event.eventType();
        this.eventVersion = event.eventVersion();
        this.occurredAt = event.occurredAt();
        this.appointmentId = event.appointmentId();
        this.patientUsername = event.patientUsername();
        this.doctorUsername = event.doctorUsername();
        this.appointmentDate = event.appointmentDate();
        this.status = event.status();
    }

    public Long getId() {
        return id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getEventVersion() {
        return eventVersion;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Long getAppointmentId() {
        return appointmentId;
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
}