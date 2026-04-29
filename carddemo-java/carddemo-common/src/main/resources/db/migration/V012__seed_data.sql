-- Flyway migration: seed data
-- TODO: Parse app/data/ASCII/*.txt flat files and generate INSERT statements
-- This migration will be populated during Phase 1 (Data Layer) implementation

-- Sample seed data for user_security (from COBOL README default credentials)
INSERT INTO user_security (usr_id, usr_fname, usr_lname, usr_pwd, usr_type)
VALUES ('ADMIN001', 'ADMIN', 'USER', 'PASSWORD', 'A')
ON CONFLICT (usr_id) DO NOTHING;

INSERT INTO user_security (usr_id, usr_fname, usr_lname, usr_pwd, usr_type)
VALUES ('USER0001', 'REGULAR', 'USER', 'PASSWORD', 'U')
ON CONFLICT (usr_id) DO NOTHING;

-- TODO: Add seed data for accounts, customers, cards, transactions from ASCII data files
