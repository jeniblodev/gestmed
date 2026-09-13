package com.gestmed.notification.dto;

import java.time.LocalDateTime;

public record AppointmentMessage(
    Long id,
    String patientUsername,
    String doctorUsername,
    LocalDateTime appointmentDate,
    String status
){}
