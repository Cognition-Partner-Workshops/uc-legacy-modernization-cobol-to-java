-- Flyway migration: user_security table
-- Source: COBOL copybook CSUSR01Y.cpy (User security, RECLN 80)
CREATE TABLE IF NOT EXISTS user_security (
    usr_id             VARCHAR(8)    PRIMARY KEY,
    usr_fname          VARCHAR(20),
    usr_lname          VARCHAR(20),
    usr_pwd            VARCHAR(8)    NOT NULL,
    usr_type           VARCHAR(1)    NOT NULL
);

CREATE INDEX idx_user_security_type ON user_security(usr_type);
