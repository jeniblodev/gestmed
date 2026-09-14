package com.gestmed.scheduling.dto;

import com.gestmed.scheduling.entity.AppointmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateAppointmentInput(

        @NotNull(message = "O ID é obrigatório")
        @Positive(message = "O ID deve ser positivo")
        Long id,

        @NotBlank(message = "A data da consulta é obrigatória")
        String appointmentDate,

        @NotNull(message = "O status é obrigatório")
        AppointmentStatus status

) {
}