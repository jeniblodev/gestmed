package com.gestmed.scheduling.dto;

import jakarta.validation.constraints.NotBlank;

public record AppointmentInput(

        @NotBlank(message = "O paciente é obrigatório")
        String patientUsername,

        @NotBlank(message = "O médico é obrigatório")
        String doctorUsername,

        @NotBlank(message = "A data da consulta é obrigatória")
        String appointmentDate

) {
}