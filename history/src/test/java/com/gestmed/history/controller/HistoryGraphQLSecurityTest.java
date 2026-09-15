package com.gestmed.history.controller;

import com.gestmed.history.entity.AppointmentHistory;
import com.gestmed.history.event.AppointmentEvent;
import com.gestmed.history.service.HistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
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
class HistoryGraphQLSecurityTest {

    private static final String PATIENT_USERNAME = "patient";
    private static final String OTHER_PATIENT_USERNAME = "other-patient";
    private static final String DOCTOR_USERNAME = "doctor";
    private static final String NURSE_USERNAME = "nurse";
    private static final LocalDateTime APPOINTMENT_DATE =
            LocalDateTime.of(2026, 10, 20, 14, 30);
    private static final Instant OCCURRED_AT =
            Instant.parse("2026-09-15T12:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HistoryService historyService;

    @Test
    void graphqlRequestWithoutAuthentication_returnsUnauthorized() throws Exception {
        mockMvc.perform(graphqlPost(myHistoryQuery()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientCanGetOwnHistoryUsingAuthenticatedUsername() throws Exception {
        when(historyService.findByPatient(PATIENT_USERNAME))
                .thenReturn(List.of(history(1L, PATIENT_USERNAME)));

        mockMvc.perform(authenticatedGraphqlPost(
                        PATIENT_USERNAME,
                        "patient123",
                        myHistoryQuery()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andExpect(jsonPath("$.data.getMyHistory", hasSize(1)));

        verify(historyService).findByPatient(PATIENT_USERNAME);
    }

    @Test
    void patientCannotAccessOtherPatientOrAllHistory() throws Exception {
        mockMvc.perform(authenticatedGraphqlPost(
                        PATIENT_USERNAME,
                        "patient123",
                        patientHistoryQuery()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", not(empty())))
                .andExpect(jsonPath("$.data")
                        .value(nullValue()));

        verify(historyService, never())
                .findByPatient(OTHER_PATIENT_USERNAME);

        mockMvc.perform(authenticatedGraphqlPost(
                        PATIENT_USERNAME,
                        "patient123",
                        allHistoryQuery()
                ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.errors", not(empty())))
                .andExpect(jsonPath("$.data")
                        .value(nullValue()));

        verify(historyService, never()).findAll();
    }

    @Test
    void staffCanGetPatientHistoryAndAllHistory() throws Exception {
        when(historyService.findByPatient(OTHER_PATIENT_USERNAME))
                .thenReturn(List.of(history(2L, OTHER_PATIENT_USERNAME)));
        when(historyService.findAll())
                .thenReturn(List.of(history(3L, PATIENT_USERNAME)));

        for (String username : new String[]{DOCTOR_USERNAME, NURSE_USERNAME}) {
            mockMvc.perform(authenticatedGraphqlPost(
                            username,
                            passwordFor(username),
                            patientHistoryQuery()
                    ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.errors").doesNotExist())
                    .andExpect(jsonPath("$.data.getPatientHistory", hasSize(1)));

            mockMvc.perform(authenticatedGraphqlPost(
                            username,
                            passwordFor(username),
                            allHistoryQuery()
                    ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.errors").doesNotExist())
                    .andExpect(jsonPath("$.data.getAllHistory", hasSize(1)));
        }

        verify(historyService, times(2))
                .findByPatient(OTHER_PATIENT_USERNAME);
        verify(historyService, times(2)).findAll();
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
            String password,
            String query)
            throws Exception {

        return graphqlPost(query)
                .with(httpBasic(username, password));
    }

    private AppointmentHistory history(Long id, String patientUsername) {
        AppointmentHistory history =
                new AppointmentHistory(new AppointmentEvent(
                        UUID.randomUUID(),
                        "CREATED",
                        1,
                        OCCURRED_AT,
                        10L,
                        patientUsername,
                        DOCTOR_USERNAME,
                        APPOINTMENT_DATE,
                        "CONFIRMED"
                ));

        ReflectionTestUtils.setField(history, "id", id);

        return history;
    }

    private String passwordFor(String username) {
        return switch (username) {
            case DOCTOR_USERNAME -> "doctor123";
            case NURSE_USERNAME -> "nurse123";
            case PATIENT_USERNAME -> "patient123";
            default -> throw new IllegalArgumentException(
                    "Unknown test user: " + username
            );
        };
    }

    private String myHistoryQuery() {
        return """
                query {
                  getMyHistory {
                    id
                    eventId
                    eventType
                    eventVersion
                    occurredAt
                    appointmentId
                    patientUsername
                    doctorUsername
                    appointmentDate
                    status
                  }
                }
                """;
    }

    private String patientHistoryQuery() {
        return """
                query {
                  getPatientHistory(patientUsername: "other-patient") {
                    id
                    eventId
                    eventType
                    eventVersion
                    occurredAt
                    appointmentId
                    patientUsername
                    doctorUsername
                    appointmentDate
                    status
                  }
                }
                """;
    }

    private String allHistoryQuery() {
        return """
                query {
                  getAllHistory {
                    id
                    eventId
                    eventType
                    eventVersion
                    occurredAt
                    appointmentId
                    patientUsername
                    doctorUsername
                    appointmentDate
                    status
                  }
                }
                """;
    }
}
