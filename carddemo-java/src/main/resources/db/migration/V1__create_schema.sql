-- V1__create_schema.sql
-- CardDemo database schema - converted from COBOL VSAM file definitions

CREATE TABLE accounts (
    acct_id             BIGINT PRIMARY KEY,
    active_status       VARCHAR(1),
    current_balance     DECIMAL(12, 2),
    credit_limit        DECIMAL(12, 2),
    cash_credit_limit   DECIMAL(12, 2),
    open_date           DATE,
    expiration_date     DATE,
    reissue_date        DATE,
    current_cycle_credit DECIMAL(12, 2),
    current_cycle_debit  DECIMAL(12, 2),
    address_zip         VARCHAR(10),
    group_id            VARCHAR(10)
);

CREATE TABLE customers (
    cust_id                 BIGINT PRIMARY KEY,
    first_name              VARCHAR(25),
    middle_name             VARCHAR(25),
    last_name               VARCHAR(25),
    address_line1           VARCHAR(50),
    address_line2           VARCHAR(50),
    address_line3           VARCHAR(50),
    state_code              VARCHAR(2),
    country_code            VARCHAR(3),
    zip_code                VARCHAR(10),
    phone1                  VARCHAR(15),
    phone2                  VARCHAR(15),
    ssn                     BIGINT,
    govt_issued_id          VARCHAR(20),
    date_of_birth           DATE,
    eft_account_id          VARCHAR(10),
    primary_card_holder_ind VARCHAR(1),
    fico_credit_score       INT
);

CREATE TABLE credit_cards (
    card_num        VARCHAR(16) PRIMARY KEY,
    acct_id         BIGINT,
    cvv_code        INT,
    embossed_name   VARCHAR(50),
    expiration_date VARCHAR(10),
    active_status   VARCHAR(1)
);

CREATE TABLE card_xrefs (
    card_num VARCHAR(16) PRIMARY KEY,
    cust_id  BIGINT,
    acct_id  BIGINT
);

CREATE TABLE transactions (
    card_num       VARCHAR(16) NOT NULL,
    tran_id        VARCHAR(16) NOT NULL,
    type_cd        VARCHAR(2),
    cat_cd         INT,
    source         VARCHAR(10),
    description    VARCHAR(100),
    amount         DECIMAL(11, 2),
    merchant_id    BIGINT,
    merchant_name  VARCHAR(50),
    merchant_city  VARCHAR(50),
    merchant_zip   VARCHAR(10),
    orig_timestamp VARCHAR(26),
    proc_timestamp VARCHAR(26),
    PRIMARY KEY (card_num, tran_id)
);

CREATE TABLE daily_transactions (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_num       VARCHAR(16),
    tran_id        VARCHAR(16),
    type_cd        VARCHAR(2),
    cat_cd         INT,
    source         VARCHAR(10),
    description    VARCHAR(100),
    amount         DECIMAL(11, 2),
    merchant_id    BIGINT,
    merchant_name  VARCHAR(50),
    merchant_city  VARCHAR(50),
    merchant_zip   VARCHAR(10),
    orig_timestamp VARCHAR(26),
    proc_timestamp VARCHAR(26)
);

CREATE TABLE users (
    user_id    VARCHAR(8) PRIMARY KEY,
    password   VARCHAR(8),
    user_type  VARCHAR(1),
    first_name VARCHAR(20),
    last_name  VARCHAR(20)
);

CREATE TABLE tran_cat_balances (
    acct_id  BIGINT NOT NULL,
    type_cd  VARCHAR(2) NOT NULL,
    cat_cd   INT NOT NULL,
    balance  DECIMAL(12, 2),
    PRIMARY KEY (acct_id, type_cd, cat_cd)
);

CREATE TABLE discount_groups (
    acct_group_id VARCHAR(10) NOT NULL,
    tran_type_cd  VARCHAR(2) NOT NULL,
    tran_cat_cd   INT NOT NULL,
    interest_rate DECIMAL(10, 6),
    PRIMARY KEY (acct_group_id, tran_type_cd, tran_cat_cd)
);

-- Indexes for alternate access paths (mirrors VSAM alternate indexes)
CREATE INDEX idx_credit_cards_acct_id ON credit_cards(acct_id);
CREATE INDEX idx_card_xrefs_acct_id ON card_xrefs(acct_id);
CREATE INDEX idx_card_xrefs_cust_id ON card_xrefs(cust_id);
CREATE INDEX idx_transactions_card_num ON transactions(card_num);
CREATE INDEX idx_tran_cat_balances_acct_id ON tran_cat_balances(acct_id);
