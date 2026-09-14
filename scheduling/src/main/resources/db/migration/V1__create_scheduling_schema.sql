CREATE TABLE IF NOT EXISTS roles (
                                     id BIGINT NOT NULL AUTO_INCREMENT,
                                     name VARCHAR(50) NOT NULL,
                                     CONSTRAINT pk_roles PRIMARY KEY (id),
                                     CONSTRAINT uk_roles_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS app_users (
                                         id BIGINT NOT NULL AUTO_INCREMENT,
                                         username VARCHAR(100) NOT NULL,
                                         password VARCHAR(100) NOT NULL,
                                         enabled BOOLEAN NOT NULL DEFAULT TRUE,
                                         CONSTRAINT pk_app_users PRIMARY KEY (id),
                                         CONSTRAINT uk_app_users_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL,
                                          role_id BIGINT NOT NULL,
                                          CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
                                          CONSTRAINT fk_user_roles_user
                                              FOREIGN KEY (user_id)
                                                  REFERENCES app_users (id),
                                          CONSTRAINT fk_user_roles_role
                                              FOREIGN KEY (role_id)
                                                  REFERENCES roles (id)
);

CREATE TABLE IF NOT EXISTS appointment (
                                           id BIGINT NOT NULL AUTO_INCREMENT,
                                           patient_username VARCHAR(100) NOT NULL,
                                           doctor_username VARCHAR(100) NOT NULL,
                                           appointment_date DATETIME(6) NOT NULL,
                                           status VARCHAR(30) NOT NULL,
                                           CONSTRAINT pk_appointment PRIMARY KEY (id)
);

CREATE INDEX idx_appointment_patient
    ON appointment (patient_username);

CREATE INDEX idx_appointment_patient_date
    ON appointment (patient_username, appointment_date);

CREATE INDEX idx_appointment_doctor_date
    ON appointment (doctor_username, appointment_date);