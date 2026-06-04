-- Phase 1 (Identity & Access) schema.
--
-- Replaces the VSAM USRSEC KSDS (record layout: copybook CSUSR01Y, keyed on
-- SEC-USR-ID) with a relational table. The 8-byte SEC-USR-PWD plaintext field
-- is replaced by a BCrypt hash column (widened to 100 chars). The mainframe
-- SEC-USR-FILLER padding is dropped.
CREATE TABLE users (
    user_id    VARCHAR(8)   NOT NULL,  -- SEC-USR-ID    PIC X(08)
    first_name VARCHAR(20)  NOT NULL,  -- SEC-USR-FNAME PIC X(20)
    last_name  VARCHAR(20)  NOT NULL,  -- SEC-USR-LNAME PIC X(20)
    password   VARCHAR(100) NOT NULL,  -- SEC-USR-PWD   PIC X(08) -> BCrypt hash
    user_type  VARCHAR(5)   NOT NULL,  -- SEC-USR-TYPE  PIC X(01) -> ADMIN/USER
    CONSTRAINT pk_users PRIMARY KEY (user_id),
    CONSTRAINT ck_users_user_type CHECK (user_type IN ('ADMIN', 'USER'))
);
