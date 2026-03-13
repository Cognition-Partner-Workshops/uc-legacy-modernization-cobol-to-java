-- =============================================================================
-- CardDemo Database Schema - V1 Initial Migration
-- Flyway migration replacing VSAM file definitions from the COBOL CardDemo app.
-- Each table maps to a COBOL copybook that defines the record layout.
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Table: accounts
-- COBOL Copybook: CVACT01Y.cpy (300-byte VSAM record)
-- VSAM File: ACCTDAT
-- ---------------------------------------------------------------------------
CREATE TABLE accounts (
    account_id          BIGINT          NOT NULL,
    active_status       VARCHAR(1),
    current_balance     DECIMAL(12, 2),
    credit_limit        DECIMAL(12, 2),
    cash_credit_limit   DECIMAL(12, 2),
    open_date           DATE,
    expiration_date     DATE,
    reissue_date        DATE,
    current_cycle_credit DECIMAL(12, 2),
    current_cycle_debit DECIMAL(12, 2),
    address_zip         VARCHAR(10),
    group_id            VARCHAR(10),
    PRIMARY KEY (account_id)
);

-- ---------------------------------------------------------------------------
-- Table: cards
-- COBOL Copybook: CVACT02Y.cpy (150-byte VSAM record)
-- VSAM File: CARDDAT
-- ---------------------------------------------------------------------------
CREATE TABLE cards (
    card_number         VARCHAR(16)     NOT NULL,
    account_id          BIGINT          NOT NULL,
    cvv_code            INTEGER,
    embossed_name       VARCHAR(50),
    expiration_date     DATE,
    active_status       VARCHAR(1),
    PRIMARY KEY (card_number)
);

-- ---------------------------------------------------------------------------
-- Table: customers
-- COBOL Copybook: CVCUS01Y.cpy (500-byte VSAM record)
-- VSAM File: CUSTDAT
-- ---------------------------------------------------------------------------
CREATE TABLE customers (
    customer_id                     BIGINT          NOT NULL,
    first_name                      VARCHAR(25),
    middle_name                     VARCHAR(25),
    last_name                       VARCHAR(25),
    address_line_1                  VARCHAR(50),
    address_line_2                  VARCHAR(50),
    address_line_3                  VARCHAR(50),
    state_code                      VARCHAR(2),
    country_code                    VARCHAR(3),
    zip_code                        VARCHAR(10),
    phone_number_1                  VARCHAR(15),
    phone_number_2                  VARCHAR(15),
    ssn                             VARCHAR(9),
    govt_issued_id                  VARCHAR(20),
    date_of_birth                   DATE,
    eft_account_id                  VARCHAR(10),
    primary_card_holder_indicator   VARCHAR(1),
    fico_credit_score               INTEGER,
    PRIMARY KEY (customer_id)
);

-- ---------------------------------------------------------------------------
-- Table: card_cross_references
-- COBOL Copybook: CVACT03Y.cpy (50-byte VSAM record)
-- VSAM File: CARDXREF
-- Alternate Index: CXACAIX (by account_id)
-- ---------------------------------------------------------------------------
CREATE TABLE card_cross_references (
    card_number         VARCHAR(16)     NOT NULL,
    customer_id         BIGINT          NOT NULL,
    account_id          BIGINT          NOT NULL,
    PRIMARY KEY (card_number)
);

CREATE INDEX idx_card_xref_account_id ON card_cross_references (account_id);

-- ---------------------------------------------------------------------------
-- Table: transactions
-- COBOL Copybook: CVTRA05Y.cpy (350-byte VSAM record)
-- VSAM File: TRANSACT
-- ---------------------------------------------------------------------------
CREATE TABLE transactions (
    transaction_id      VARCHAR(16)     NOT NULL,
    type_code           VARCHAR(2),
    category_code       VARCHAR(4),
    source              VARCHAR(10),
    description         VARCHAR(100),
    amount              DECIMAL(11, 2),
    merchant_id         VARCHAR(9),
    merchant_name       VARCHAR(50),
    merchant_city       VARCHAR(50),
    merchant_zip        VARCHAR(10),
    card_number         VARCHAR(16),
    origin_timestamp    TIMESTAMP,
    processed_timestamp TIMESTAMP,
    PRIMARY KEY (transaction_id)
);

-- ---------------------------------------------------------------------------
-- Table: daily_transactions
-- COBOL Copybook: CVTRA06Y.cpy (350-byte VSAM record)
-- VSAM File: DALYTRAN
-- Same structure as transactions but for daily batch input.
-- ---------------------------------------------------------------------------
CREATE TABLE daily_transactions (
    transaction_id      VARCHAR(16)     NOT NULL,
    type_code           VARCHAR(2),
    category_code       VARCHAR(4),
    source              VARCHAR(10),
    description         VARCHAR(100),
    amount              DECIMAL(11, 2),
    merchant_id         VARCHAR(9),
    merchant_name       VARCHAR(50),
    merchant_city       VARCHAR(50),
    merchant_zip        VARCHAR(10),
    card_number         VARCHAR(16),
    origin_timestamp    TIMESTAMP,
    processed_timestamp TIMESTAMP,
    PRIMARY KEY (transaction_id)
);

-- ---------------------------------------------------------------------------
-- Table: transaction_category_balances
-- COBOL Copybook: CVTRA01Y.cpy (50-byte VSAM record)
-- VSAM File: TCATBAL
-- Composite key: account_id + type_code + category_code
-- ---------------------------------------------------------------------------
CREATE TABLE transaction_category_balances (
    account_id          BIGINT          NOT NULL,
    type_code           VARCHAR(2)      NOT NULL,
    category_code       VARCHAR(4)      NOT NULL,
    balance             DECIMAL(11, 2),
    PRIMARY KEY (account_id, type_code, category_code)
);

-- ---------------------------------------------------------------------------
-- Table: disclosure_groups
-- COBOL Copybook: CVTRA02Y.cpy (50-byte VSAM record)
-- VSAM File: DISCGRP
-- Composite key: account_group_id + transaction_type_code + transaction_category_code
-- ---------------------------------------------------------------------------
CREATE TABLE disclosure_groups (
    account_group_id            VARCHAR(10)     NOT NULL,
    transaction_type_code       VARCHAR(2)      NOT NULL,
    transaction_category_code   VARCHAR(4)      NOT NULL,
    interest_rate               DECIMAL(6, 2),
    PRIMARY KEY (account_group_id, transaction_type_code, transaction_category_code)
);

-- ---------------------------------------------------------------------------
-- Table: transaction_types
-- COBOL Copybook: CVTRA03Y.cpy (60-byte VSAM record)
-- VSAM File: TRANTYPE
-- ---------------------------------------------------------------------------
CREATE TABLE transaction_types (
    type_code           VARCHAR(2)      NOT NULL,
    type_description    VARCHAR(50),
    PRIMARY KEY (type_code)
);

-- ---------------------------------------------------------------------------
-- Table: transaction_categories
-- COBOL Copybook: CVTRA04Y.cpy (60-byte VSAM record)
-- VSAM File: TRANCAT
-- Composite key: type_code + category_code
-- ---------------------------------------------------------------------------
CREATE TABLE transaction_categories (
    type_code               VARCHAR(2)      NOT NULL,
    category_code           VARCHAR(4)      NOT NULL,
    category_description    VARCHAR(50),
    PRIMARY KEY (type_code, category_code)
);

-- ---------------------------------------------------------------------------
-- Table: user_security
-- COBOL Copybook: CSUSR01Y.cpy (80-byte VSAM record)
-- VSAM File: USRSEC
-- User type values from COCOM01Y.cpy:
--   'A' = ADMIN (CDEMO-USRTYP-ADMIN)
--   'U' = USER  (CDEMO-USRTYP-USER)
-- ---------------------------------------------------------------------------
CREATE TABLE user_security (
    user_id             VARCHAR(8)      NOT NULL,
    first_name          VARCHAR(20),
    last_name           VARCHAR(20),
    password            VARCHAR(8),
    user_type           VARCHAR(5),
    PRIMARY KEY (user_id)
);
