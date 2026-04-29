-- Seed data for CardDemo User Management
-- All passwords are BCrypt-hashed 'password' (matching COBOL 8-char SEC-USR-PWD constraint)
-- BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

INSERT INTO users (user_id, first_name, last_name, password, user_type)
VALUES ('ADMIN001', 'ADMIN', 'USER', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'A');

INSERT INTO users (user_id, first_name, last_name, password, user_type)
VALUES ('USER0001', 'FIRST', 'USER', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'U');

INSERT INTO users (user_id, first_name, last_name, password, user_type)
VALUES ('USER0002', 'SECOND', 'USER', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'U');
