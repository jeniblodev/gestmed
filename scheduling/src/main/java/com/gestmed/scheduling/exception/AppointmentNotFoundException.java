package com.gestmed.scheduling.exception;

public class AppointmentNotFoundException extends RuntimeException {

    public AppointmentNotFoundException(Long id) {
        super("Agendamento não encontrado: " + id);
    }
}
