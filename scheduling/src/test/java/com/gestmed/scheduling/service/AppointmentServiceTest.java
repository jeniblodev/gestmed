package com.gestmed.scheduling.service;

import com.gestmed.scheduling.entity.Appointment;
import com.gestmed.scheduling.entity.AppointmentStatus;
import com.gestmed.scheduling.entity.Role;
import com.gestmed.scheduling.entity.User;
import com.gestmed.scheduling.event.AppointmentEventPublisher;
import com.gestmed.scheduling.exception.AppointmentNotFoundException;
import com.gestmed.scheduling.exception.InvalidAppointmentException;
import com.gestmed.scheduling.exception.ScheduleConflictException;
import com.gestmed.scheduling.exception.UserNotFoundException;
import com.gestmed.scheduling.repository.AppointmentRepository;
import com.gestmed.scheduling.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.gestmed.scheduling.entity.AppointmentStatus.CANCELLED;
import static com.gestmed.scheduling.entity.AppointmentStatus.COMPLETED;
import static com.gestmed.scheduling.entity.AppointmentStatus.CONFIRMED;
import static com.gestmed.scheduling.entity.AppointmentStatus.SCHEDULED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    private static final Long APPOINTMENT_ID = 10L;
    private static final String PATIENT_USERNAME = "patient";
    private static final String DOCTOR_USERNAME = "doctor";

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AppointmentEventPublisher eventPublisher;

    @InjectMocks
    private AppointmentService appointmentService;

    @Test
    void createAppointment_whenValid_savesScheduledAppointmentAndPublishesCreated() {
        Appointment appointment = newAppointment(futureDate());
        givenPatientAndDoctorExist();
        givenNoCreationConflicts(appointment);
        givenSaveReturnsArgument();

        Appointment createdAppointment =
                appointmentService.createAppointment(appointment);

        assertSame(appointment, createdAppointment);
        assertEquals(SCHEDULED, createdAppointment.getStatus());
        verify(appointmentRepository).save(createdAppointment);
        verify(eventPublisher).publishCreated(createdAppointment);
    }

    @Test
    void createAppointment_whenDateIsInThePast_throwsInvalidAppointmentAndDoesNotPersistOrPublish() {
        Appointment appointment =
                newAppointment(LocalDateTime.now().minusMinutes(1));

        assertRejectedWithoutPersistenceOrEvents(
                InvalidAppointmentException.class,
                () -> appointmentService.createAppointment(appointment)
        );
    }

    @Test
    void createAppointment_whenUserDoesNotExist_throwsUserNotFoundAndDoesNotPersistOrPublish() {
        Appointment appointment = newAppointment(futureDate());
        when(userRepository.findByUsername(PATIENT_USERNAME))
                .thenReturn(Optional.empty());

        assertRejectedWithoutPersistenceOrEvents(
                UserNotFoundException.class,
                () -> appointmentService.createAppointment(appointment)
        );
    }

    @Test
    void createAppointment_whenUserHasInvalidRole_throwsInvalidAppointmentAndDoesNotPersistOrPublish() {
        Appointment appointment = newAppointment(futureDate());
        givenUser(PATIENT_USERNAME, "ROLE_DOCTOR");

        assertRejectedWithoutPersistenceOrEvents(
                InvalidAppointmentException.class,
                () -> appointmentService.createAppointment(appointment)
        );
    }

    @Test
    void createAppointment_whenDoctorAlreadyHasAppointmentAtSameTime_throwsScheduleConflictAndDoesNotPersistOrPublish() {
        Appointment appointment = newAppointment(futureDate());
        givenPatientAndDoctorExist();
        when(appointmentRepository.existsByDoctorUsernameAndAppointmentDate(
                DOCTOR_USERNAME,
                appointment.getAppointmentDate()
        )).thenReturn(true);

        assertRejectedWithoutPersistenceOrEvents(
                ScheduleConflictException.class,
                () -> appointmentService.createAppointment(appointment)
        );
    }

    @Test
    void createAppointment_whenPatientAlreadyHasAppointmentAtSameTime_throwsScheduleConflictAndDoesNotPersistOrPublish() {
        Appointment appointment = newAppointment(futureDate());
        givenPatientAndDoctorExist();
        when(appointmentRepository.existsByDoctorUsernameAndAppointmentDate(
                DOCTOR_USERNAME,
                appointment.getAppointmentDate()
        )).thenReturn(false);
        when(appointmentRepository.existsByPatientUsernameAndAppointmentDate(
                PATIENT_USERNAME,
                appointment.getAppointmentDate()
        )).thenReturn(true);

        assertRejectedWithoutPersistenceOrEvents(
                ScheduleConflictException.class,
                () -> appointmentService.createAppointment(appointment)
        );
    }

    @Test
    void updateAppointment_whenValid_savesNewDateAndStatusAndPublishesUpdated() {
        Appointment appointment =
                existingAppointment(APPOINTMENT_ID, SCHEDULED, futureDate());
        LocalDateTime newDate = futureDate().plusDays(1);
        givenAppointmentExists(appointment);
        givenNoUpdateConflicts(appointment, newDate);
        givenSaveReturnsArgument();

        Appointment updatedAppointment =
                appointmentService.updateAppointment(
                        APPOINTMENT_ID,
                        newDate,
                        CONFIRMED
                );

        assertSame(appointment, updatedAppointment);
        assertEquals(CONFIRMED, updatedAppointment.getStatus());
        assertEquals(newDate, updatedAppointment.getAppointmentDate());
        verify(appointmentRepository).save(updatedAppointment);
        verify(eventPublisher).publishUpdated(updatedAppointment);
    }

    @Test
    void updateAppointment_whenCancelled_savesCancelledAndPublishesCancelledOnly() {
        Appointment appointment =
                existingAppointment(APPOINTMENT_ID, SCHEDULED, futureDate());
        LocalDateTime newDate = futureDate().plusDays(1);
        givenAppointmentExists(appointment);
        givenNoUpdateConflicts(appointment, newDate);
        givenSaveReturnsArgument();

        Appointment updatedAppointment =
                appointmentService.updateAppointment(
                        APPOINTMENT_ID,
                        newDate,
                        CANCELLED
                );

        assertEquals(CANCELLED, updatedAppointment.getStatus());
        verify(appointmentRepository).save(updatedAppointment);
        verify(eventPublisher).publishCancelled(updatedAppointment);
        verify(eventPublisher, never()).publishUpdated(any());
    }

    @Test
    void updateAppointment_whenAppointmentDoesNotExist_throwsAppointmentNotFoundAndDoesNotPersistOrPublish() {
        when(appointmentRepository.findById(APPOINTMENT_ID))
                .thenReturn(Optional.empty());

        assertRejectedWithoutPersistenceOrEvents(
                AppointmentNotFoundException.class,
                () -> appointmentService.updateAppointment(
                        APPOINTMENT_ID,
                        futureDate(),
                        CONFIRMED
                )
        );
    }

    @Test
    void updateAppointment_whenCurrentStatusIsTerminal_throwsInvalidAppointmentAndDoesNotPersistOrPublish() {
        for (AppointmentStatus currentStatus : new AppointmentStatus[]{COMPLETED, CANCELLED}) {
            Appointment appointment =
                    existingAppointment(APPOINTMENT_ID, currentStatus, futureDate());
            givenAppointmentExists(appointment);

            assertRejectedWithoutPersistenceOrEvents(
                    InvalidAppointmentException.class,
                    () -> appointmentService.updateAppointment(
                            APPOINTMENT_ID,
                            futureDate().plusDays(1),
                            CONFIRMED
                    )
            );
        }
    }

    @Test
    void updateAppointment_whenScheduledAppointmentIsCompletedDirectly_throwsInvalidAppointmentAndDoesNotPersistOrPublish() {
        Appointment appointment =
                existingAppointment(APPOINTMENT_ID, SCHEDULED, futureDate());
        givenAppointmentExists(appointment);

        assertRejectedWithoutPersistenceOrEvents(
                InvalidAppointmentException.class,
                () -> appointmentService.updateAppointment(
                        APPOINTMENT_ID,
                        futureDate().plusDays(1),
                        COMPLETED
                )
        );
    }

    @Test
    void updateAppointment_whenScheduleConflicts_throwsScheduleConflictAndDoesNotPersistOrPublish() {
        Appointment appointment =
                existingAppointment(APPOINTMENT_ID, SCHEDULED, futureDate());
        LocalDateTime newDate = futureDate().plusDays(1);
        givenAppointmentExists(appointment);
        when(appointmentRepository.existsByDoctorUsernameAndAppointmentDateAndIdNot(
                DOCTOR_USERNAME,
                newDate,
                APPOINTMENT_ID
        )).thenReturn(true);

        assertRejectedWithoutPersistenceOrEvents(
                ScheduleConflictException.class,
                () -> appointmentService.updateAppointment(
                        APPOINTMENT_ID,
                        newDate,
                        CONFIRMED
                )
        );
    }

    @Test
    void updateAppointment_whenCheckingConflicts_excludesCurrentAppointmentId() {
        LocalDateTime ownDate = futureDate();
        Appointment appointment =
                existingAppointment(APPOINTMENT_ID, SCHEDULED, ownDate);
        givenAppointmentExists(appointment);
        givenNoUpdateConflicts(appointment, ownDate);
        givenSaveReturnsArgument();

        appointmentService.updateAppointment(
                APPOINTMENT_ID,
                ownDate,
                CONFIRMED
        );

        verify(appointmentRepository)
                .existsByDoctorUsernameAndAppointmentDateAndIdNot(
                        DOCTOR_USERNAME,
                        ownDate,
                        APPOINTMENT_ID
                );
        verify(appointmentRepository)
                .existsByPatientUsernameAndAppointmentDateAndIdNot(
                        PATIENT_USERNAME,
                        ownDate,
                        APPOINTMENT_ID
                );
    }

    private void givenPatientAndDoctorExist() {
        givenUser(PATIENT_USERNAME, "ROLE_PATIENT");
        givenUser(DOCTOR_USERNAME, "ROLE_DOCTOR");
    }

    private void givenUser(String username, String roleName) {
        User user = new User(
                username,
                "{noop}password",
                true
        );

        user.addRole(new Role(roleName));

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.of(user));
    }

    private void givenNoCreationConflicts(Appointment appointment) {
        when(appointmentRepository.existsByDoctorUsernameAndAppointmentDate(
                appointment.getDoctorUsername(),
                appointment.getAppointmentDate()
        )).thenReturn(false);
        when(appointmentRepository.existsByPatientUsernameAndAppointmentDate(
                appointment.getPatientUsername(),
                appointment.getAppointmentDate()
        )).thenReturn(false);
    }

    private void givenAppointmentExists(Appointment appointment) {
        when(appointmentRepository.findById(appointment.getId()))
                .thenReturn(Optional.of(appointment));
    }

    private void givenNoUpdateConflicts(
            Appointment appointment,
            LocalDateTime newDate) {

        when(appointmentRepository.existsByDoctorUsernameAndAppointmentDateAndIdNot(
                appointment.getDoctorUsername(),
                newDate,
                appointment.getId()
        )).thenReturn(false);
        when(appointmentRepository.existsByPatientUsernameAndAppointmentDateAndIdNot(
                appointment.getPatientUsername(),
                newDate,
                appointment.getId()
        )).thenReturn(false);
    }

    private void givenSaveReturnsArgument() {
        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void assertRejectedWithoutPersistenceOrEvents(
            Class<? extends Throwable> exceptionType,
            Executable executable) {

        assertThrows(exceptionType, executable);
        verify(appointmentRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    private Appointment newAppointment(LocalDateTime appointmentDate) {
        Appointment appointment = new Appointment();
        appointment.setPatientUsername(PATIENT_USERNAME);
        appointment.setDoctorUsername(DOCTOR_USERNAME);
        appointment.setAppointmentDate(appointmentDate);
        return appointment;
    }

    private Appointment existingAppointment(
            Long id,
            AppointmentStatus status,
            LocalDateTime appointmentDate) {

        Appointment appointment = newAppointment(appointmentDate);
        ReflectionTestUtils.setField(appointment, "id", id);
        appointment.setStatus(status);
        return appointment;
    }

    private LocalDateTime futureDate() {
        return LocalDateTime.now()
                .plusDays(7)
                .withSecond(0)
                .withNano(0);
    }
}
