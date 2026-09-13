package com.gestmed.scheduling.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/appointments")
public class TestController {

    @GetMapping("/doctor/history")
    public String getDoctorHistory() {
        return "DOCTOR: Acesso liberado para visualizar e editar historico de consultas.";
    }

    @GetMapping("/nurse/register")
    public String registerAppointment() {
        return "NURSE: Acesso liberado para registrar consultas e acessar historico.";
    }

    @GetMapping("/patient/view")
    public String getPatientAppointments() {
        return "PATIENT: Acesso liberado para visualizar apenas suas proprias consultas.";
    }
}