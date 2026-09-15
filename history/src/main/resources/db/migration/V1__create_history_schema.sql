CREATE TABLE IF NOT EXISTS appointment_history (
                                                   id BIGINT NOT NULL,
                                                   patient_username VARCHAR(100) NOT NULL,
                                                   doctor_username VARCHAR(100) NOT NULL,
                                                   appointment_date DATETIME(6) NOT NULL,
                                                   status VARCHAR(30) NOT NULL,

                                                   CONSTRAINT pk_appointment_history
                                                       PRIMARY KEY (id),

                                                   INDEX idx_history_patient (
                                                                              patient_username
                                                       ),

                                                   INDEX idx_history_patient_date (
                                                                                   patient_username,
                                                                                   appointment_date
                                                       ),

                                                   INDEX idx_history_doctor_date (
                                                                                  doctor_username,
                                                                                  appointment_date
                                                       )
);

CREATE TABLE IF NOT EXISTS processed_events (
                                                event_id VARCHAR(36) NOT NULL,
                                                event_type VARCHAR(50) NOT NULL,
                                                appointment_id BIGINT NOT NULL,
                                                processed_at DATETIME(6) NOT NULL,

                                                CONSTRAINT pk_processed_events
                                                    PRIMARY KEY (event_id),

                                                INDEX idx_processed_events_processed_at (
                                                                                         processed_at
                                                    ),

                                                INDEX idx_processed_events_appointment (
                                                                                        appointment_id
                                                    )
);