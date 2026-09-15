package com.gestmed.scheduling.controller;

import com.gestmed.scheduling.entity.Appointment;
import com.gestmed.scheduling.entity.AppointmentStatus;
import com.gestmed.scheduling.entity.Role;
import com.gestmed.scheduling.entity.User;
import com.gestmed.scheduling.repository.UserRepository;
import com.gestmed.scheduling.service.AppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AppointmentGraphQLSecurityTest {

    private static final String PATIENT_USERNAME = "patient";
    private static final String DOCTOR_USERNAME = "doctor";
    private static final String NURSE_USERNAME = "nurse";
    private static final String PASSWORD = "password";
    private static final LocalDateTime APPOINTMENT_DATE =
            LocalDateTime.of(2026, 10, 20, 14, 30);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    void setUpUsers() {
        givenUser(PATIENT_USERNAME, "ROLE_PATIENT");
        givenUser(DOCTOR_USERNAME, "ROLE_DOCTOR");
        givenUser(NURSE_USERNAME, "ROLE_NURSE");
    }

    @Test
    void graphqlRequestWithoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(graphqlPost(patientAppointmentsQuery()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientCanGetOwnAppointmentsUsingAuthenticatedUsername() throws Exception {
        when(appointmentService.getPatientAppointments(PATIENT_USERNAME))
                .thenReturn(List.of(appointment(1L, PATIENT_USERNAME)));

        mockMvc.perform(authenticatedGraphqlPost(
                        PATIENT_USERNAME,
                        patientAppointmentsQuery()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.getPatientAppointments", hasSize(1)));

        verify(appointmentService)
                .getPatientAppointments(PATIENT_USERNAME);
    }

    @Test
    void patientCannotAccessStaffOperations() throws Exception {
        mockMvc.perform(authenticatedGraphqlPost(
                        PATIENT_USERNAME,
                        allAppointmentsQuery()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", not(empty())))
                .andExpect(jsonPath("$.data.getAllAppointments")
                        .value(nullValue()));

        verify(appointmentService, never()).getAllAppointments();

        mockMvc.perform(authenticatedGraphqlPost(
                        PATIENT_USERNAME,
                        createAppointmentMutation()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", not(empty())))
                .andExpect(jsonPath("$.data.createAppointment")
                        .value(nullValue()));

        verify(appointmentService, never()).createAppointment(any());

        mockMvc.perform(authenticatedGraphqlPost(
                        PATIENT_USERNAME,
                        updateAppointmentMutation()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", not(empty())))
                .andExpect(jsonPath("$.data.updateAppointment")
                        .value(nullValue()));

        verify(appointmentService, never())
                .updateAppointment(any(), any(), any());
    }

    @Test
    void staffCanQueryAndCreateAppointments() throws Exception {
        when(appointmentService.getAllAppointments())
                .thenReturn(List.of(appointment(2L, PATIENT_USERNAME)));
        when(appointmentService.createAppointment(any()))
                .thenReturn(appointment(3L, PATIENT_USERNAME));
        when(appointmentService.updateAppointment(any(), any(), any()))
                .thenReturn(appointment(4L, PATIENT_USERNAME));

        for (String username : new String[]{DOCTOR_USERNAME, NURSE_USERNAME}) {
            mockMvc.perform(authenticatedGraphqlPost(
                            username,
                            allAppointmentsQuery()
                    ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.errors").doesNotExist())
                    .andExpect(jsonPath("$.data.getAllAppointments", hasSize(1)));

            mockMvc.perform(authenticatedGraphqlPost(
                            username,
                            createAppointmentMutation()
                    ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.errors").doesNotExist())
                    .andExpect(jsonPath("$.data.createAppointment.id").value("3"));

            mockMvc.perform(authenticatedGraphqlPost(
                            username,
                            updateAppointmentMutation()
                    ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.errors").doesNotExist())
                    .andExpect(jsonPath("$.data.updateAppointment.id").value("4"));
        }

        verify(appointmentService, times(2))
                .getAllAppointments();
        verify(appointmentService, times(2))
                .createAppointment(any());
        verify(appointmentService, times(2))
                .updateAppointment(any(), any(), any());
    }

    private void givenUser(String username, String roleName) {
        User user = new User(
                username,
                passwordEncoder.encode(PASSWORD),
                true
        );
        user.addRole(new Role(roleName));

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder graphqlPost(
            String query)
            throws Exception {

        return post("/graphql")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(
                        Map.of("query", query)
                ));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authenticatedGraphqlPost(
            String username,
            String query)
            throws Exception {

        return graphqlPost(query)
                .with(httpBasic(username, PASSWORD));
    }

    private Appointment appointment(Long id, String patientUsername) {
        Appointment appointment = new Appointment();
        ReflectionTestUtils.setField(appointment, "id", id);
        appointment.setPatientUsername(patientUsername);
        appointment.setDoctorUsername(DOCTOR_USERNAME);
        appointment.setAppointmentDate(APPOINTMENT_DATE);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return appointment;
    }

    private String patientAppointmentsQuery() {
        return """
                query {
                  getPatientAppointments {
                    id
                    patientUsername
                    doctorUsername
                    appointmentDate
                    status
                  }
                }
                """;
    }

    private String allAppointmentsQuery() {
        return """
                query {
                  getAllAppointments {
                    id
                    patientUsername
                    doctorUsername
                    appointmentDate
                    status
                  }
                }
                """;
    }

    private String createAppointmentMutation() {
        return """
                mutation {
                  createAppointment(input: {
                    patientUsername: "patient"
                    doctorUsername: "doctor"
                    appointmentDate: "2026-10-20T14:30:00"
                  }) {
                    id
                    patientUsername
                    doctorUsername
                    appointmentDate
                    status
                  }
                }
                """;
    }

    private String updateAppointmentMutation() {
        return """
                mutation {
                  updateAppointment(input: {
                    id: 5
                    appointmentDate: "2026-10-20T14:30:00"
                    status: CONFIRMED
                  }) {
                    id
                    patientUsername
                    doctorUsername
                    appointmentDate
                    status
                  }
                }
                """;
    }
}
