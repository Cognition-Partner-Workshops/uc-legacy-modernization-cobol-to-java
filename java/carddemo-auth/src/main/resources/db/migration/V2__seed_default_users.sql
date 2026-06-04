-- Seed the default users carried over from the legacy USRSEC dataset.
--
-- Legacy CardDemo shipped these accounts with the plaintext password
-- 'PASSWORD' in SEC-USR-PWD. As part of the Phase 1 security uplift the
-- passwords are stored as BCrypt hashes (strength 10) of 'PASSWORD' instead of
-- plaintext. Operators should rotate these credentials after first sign-on.
--
--   ADMIN001 -> user_type ADMIN  (legacy SEC-USR-TYPE 'A')
--   USER0001 -> user_type USER   (legacy SEC-USR-TYPE 'U')
INSERT INTO users (user_id, first_name, last_name, password, user_type) VALUES
    ('ADMIN001', 'Admin', 'User', '$2b$10$rpS66pTFX1GglGTSvex4NeB6avkpx/v.SO5Sb7RrPiiLurx3dgHU6', 'ADMIN'),
    ('USER0001', 'Regular', 'User', '$2b$10$/yejoNVtcISfw7Np58CRUeamLIPvwwuCV/L3tMcHTnuq1lAsZfdPW', 'USER');
