package com.gestmed.history.listener;

import com.gestmed.history.config.RabbitMQConfig;
import com.gestmed.history.dto.AppointmentMessage;
import com.gestmed.history.entity.AppointmentHistory;
import com.gestmed.history.repository.AppointmentHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class HistoryListener {

    private static final Logger log = LoggerFactory.getLogger(HistoryListener.class);
    private final AppointmentHistoryRepository repository;

    public HistoryListener(AppointmentHistoryRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitMQConfig.HISTORY_QUEUE)
    public void processHistoryRecord(AppointmentMessage message) {
        log.info("Salvando histórico de consultas do paciente: {}", message.patientUsername());

        AppointmentHistory history = new AppointmentHistory();
        history.setId(message.id());
        history.setPatientUsername(message.patientUsername());
        history.setDoctorUsername(message.doctorUsername());
        history.setAppointmentDate(message.appointmentDate());
        history.setStatus(message.status());

        repository.save(history);
    }
}
