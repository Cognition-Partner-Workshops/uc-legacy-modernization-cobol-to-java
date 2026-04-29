-- Seed data for CardDemo User Management
-- All passwords are BCrypt-hashed 'password' (matching COBOL 8-char SEC-USR-PWD constraint)

INSERT INTO users (user_id, first_name, last_name, password, user_type)
VALUES ('ADMIN001', 'ADMIN', 'USER', '$2a$10$IwW845WTgNtRnCvAtTtkt.aMO8VgA9SleBo3LAdBwcSp3szhuQHhW', 'A');

INSERT INTO users (user_id, first_name, last_name, password, user_type)
VALUES ('USER0001', 'FIRST', 'USER', '$2a$10$IwW845WTgNtRnCvAtTtkt.aMO8VgA9SleBo3LAdBwcSp3szhuQHhW', 'U');

INSERT INTO users (user_id, first_name, last_name, password, user_type)
VALUES ('USER0002', 'SECOND', 'USER', '$2a$10$IwW845WTgNtRnCvAtTtkt.aMO8VgA9SleBo3LAdBwcSp3szhuQHhW', 'U');
