-- ==========================================================================
-- Schema for the USERS table (replaces the USRSEC VSAM dataset)
--
-- Field mapping from COBOL copybook CSUSR01Y.cpy:
--   SEC-USR-ID     PIC X(08)  -> user_id    VARCHAR(8)   PRIMARY KEY
--   SEC-USR-FNAME  PIC X(20)  -> first_name VARCHAR(20)  NOT NULL
--   SEC-USR-LNAME  PIC X(20)  -> last_name  VARCHAR(20)  NOT NULL
--   SEC-USR-PWD    PIC X(08)  -> password   VARCHAR(8)   NOT NULL
--   SEC-USR-TYPE   PIC X(01)  -> user_type  VARCHAR(10)  NOT NULL
--   SEC-USR-FILLER PIC X(23)  -> (not migrated; COBOL filler padding)
-- ==========================================================================
CREATE TABLE IF NOT EXISTS users (
    user_id    VARCHAR(8)   NOT NULL,
    first_name VARCHAR(20)  NOT NULL,
    last_name  VARCHAR(20)  NOT NULL,
    password   VARCHAR(8)   NOT NULL,
    user_type  VARCHAR(10)  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT chk_user_type CHECK (user_type IN ('REGULAR', 'ADMIN'))
);
