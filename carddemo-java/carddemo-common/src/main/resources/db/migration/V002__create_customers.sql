-- Flyway migration: customers table
-- Source: COBOL copybook CVCUS01Y.cpy (Customer record, RECLN 500)
CREATE TABLE IF NOT EXISTS customers (
    cust_id                BIGINT       PRIMARY KEY,
    first_name             VARCHAR(25),
    middle_name            VARCHAR(25),
    last_name              VARCHAR(25),
    addr_line_1            VARCHAR(50),
    addr_line_2            VARCHAR(50),
    addr_line_3            VARCHAR(50),
    addr_state_cd          VARCHAR(2),
    addr_country_cd        VARCHAR(3),
    addr_zip               VARCHAR(10),
    phone_num_1            VARCHAR(15),
    phone_num_2            VARCHAR(15),
    ssn                    BIGINT,
    govt_issued_id         VARCHAR(20),
    dob                    DATE,
    eft_account_id         VARCHAR(10),
    pri_card_holder_ind    VARCHAR(1),
    fico_credit_score      INTEGER
);

CREATE INDEX idx_customers_last_name ON customers(last_name);
