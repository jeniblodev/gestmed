CREATE TABLE appointment_history_events (
                                            history_id BIGINT NOT NULL AUTO_INCREMENT,
                                            event_id VARCHAR(36) NOT NULL,
                                            event_type VARCHAR(30) NOT NULL,
                                            event_version INT NOT NULL,
                                            occurred_at DATETIME(6) NOT NULL,
                                            appointment_id BIGINT NOT NULL,
                                            patient_username VARCHAR(100) NOT NULL,
                                            doctor_username VARCHAR(100) NOT NULL,
                                            appointment_date DATETIME(6) NOT NULL,
                                            status VARCHAR(30) NOT NULL,

                                            CONSTRAINT pk_appointment_history_events
                                                PRIMARY KEY (history_id),

                                            CONSTRAINT uk_history_event_id
                                                UNIQUE (event_id),

                                            INDEX idx_history_appointment (
                                                                           appointment_id
                                                ),

                                            INDEX idx_history_patient (
                                                                       patient_username
                                                ),

                                            INDEX idx_history_patient_date (
                                                                            patient_username,
                                                                            appointment_date
                                                ),

                                            INDEX idx_history_occurred_at (
                                                                           occurred_at
                                                )
);