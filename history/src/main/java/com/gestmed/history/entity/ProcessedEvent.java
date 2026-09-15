package com.gestmed.history.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {

    @Id
    @Column(
            name = "event_id",
            nullable = false,
            length = 36
    )
    private String eventId;

    @Column(
            name = "event_type",
            nullable = false,
            length = 50
    )
    private String eventType;

    @Column(
            name = "appointment_id",
            nullable = false
    )
    private Long appointmentId;

    @Column(
            name = "processed_at",
            nullable = false
    )
    private Instant processedAt;

    protected ProcessedEvent() {
    }

    public ProcessedEvent(
            UUID eventId,
            String eventType,
            Long appointmentId) {

        this.eventId = eventId.toString();
        this.eventType = eventType;
        this.appointmentId = appointmentId;
        this.processedAt = Instant.now();
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}