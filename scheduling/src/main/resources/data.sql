INSERT INTO roles (id, name) VALUES (1, 'ROLE_DOCTOR');
INSERT INTO roles (id, name) VALUES (2, 'ROLE_NURSE');
INSERT INTO roles (id, name) VALUES (3, 'ROLE_PATIENT');

INSERT INTO user (id, username, password, enabled) VALUES (1, 'doctor', '$2a$12$N9/XQ7F9J3/qD1J4J9Z6O.vG9m1X9Z6O.vG9m1X9Z6O.vG9m1X9Z6O', true);
INSERT INTO user (id, username, password, enabled) VALUES (2, 'nurse', '$2a$12$N9/XQ7F9J3/qD1J4J9Z6O.vG9m1X9Z6O.vG9m1X9Z6O.vG9m1X9Z6O', true);
INSERT INTO user (id, username, password, enabled) VALUES (3, 'patient', '$2a$12$N9/XQ7F9J3/qD1J4J9Z6O.vG9m1X9Z6O.vG9m1X9Z6O.vG9m1X9Z6O', true);

INSERT INTO user_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2);
INSERT INTO user_roles (user_id, role_id) VALUES (3, 3);