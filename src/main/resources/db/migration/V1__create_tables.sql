-- Migrated from VSAM KSDS files to relational tables
-- Source copybooks: CVACT01Y, CVCUS01Y, CVTRA05Y, CVACT02Y, CVACT03Y, CSUSR01Y

CREATE TABLE accounts (
    acct_id           BIGINT       PRIMARY KEY,
    active_status     VARCHAR(1)   NOT NULL DEFAULT 'Y',
    curr_bal          DECIMAL(12,2) DEFAULT 0,
    credit_limit      DECIMAL(12,2) DEFAULT 0,
    cash_credit_limit DECIMAL(12,2) DEFAULT 0,
    open_date         VARCHAR(10),
    expiration_date   VARCHAR(10),
    reissue_date      VARCHAR(10),
    curr_cyc_credit   DECIMAL(12,2) DEFAULT 0,
    curr_cyc_debit    DECIMAL(12,2) DEFAULT 0,
    addr_zip          VARCHAR(10),
    group_id          VARCHAR(10)
);

CREATE TABLE customers (
    cust_id              BIGINT      PRIMARY KEY,
    first_name           VARCHAR(25) NOT NULL,
    middle_name          VARCHAR(25),
    last_name            VARCHAR(25) NOT NULL,
    addr_line_1          VARCHAR(50),
    addr_line_2          VARCHAR(50),
    addr_line_3          VARCHAR(50),
    addr_state_cd        VARCHAR(2),
    addr_country_cd      VARCHAR(3),
    addr_zip             VARCHAR(10),
    phone_num_1          VARCHAR(15),
    phone_num_2          VARCHAR(15),
    ssn                  BIGINT,
    govt_issued_id       VARCHAR(20),
    dob                  VARCHAR(10),
    eft_account_id       VARCHAR(10),
    pri_card_holder_ind  VARCHAR(1),
    fico_credit_score    INT DEFAULT 0
);

CREATE TABLE transactions (
    tran_id        VARCHAR(16)  PRIMARY KEY,
    tran_type_cd   VARCHAR(2),
    tran_cat_cd    INT,
    tran_source    VARCHAR(10),
    tran_desc      VARCHAR(100),
    tran_amt       DECIMAL(11,2),
    merchant_id    BIGINT,
    merchant_name  VARCHAR(50),
    merchant_city  VARCHAR(50),
    merchant_zip   VARCHAR(10),
    card_num       VARCHAR(16)  NOT NULL,
    orig_ts        VARCHAR(26),
    proc_ts        VARCHAR(26)
);

CREATE TABLE card_xref (
    card_num  VARCHAR(16) PRIMARY KEY,
    cust_id   BIGINT,
    acct_id   BIGINT
);

CREATE TABLE users (
    user_id    VARCHAR(8)  PRIMARY KEY,
    first_name VARCHAR(20) NOT NULL,
    last_name  VARCHAR(20) NOT NULL,
    password   VARCHAR(60) NOT NULL,
    user_type  VARCHAR(1)  NOT NULL DEFAULT 'U'
);

-- Indexes mirroring VSAM AIX (Alternate Index) paths
CREATE INDEX idx_transactions_card_num ON transactions(card_num);
CREATE INDEX idx_card_xref_cust_id ON card_xref(cust_id);
CREATE INDEX idx_card_xref_acct_id ON card_xref(acct_id);
CREATE INDEX idx_accounts_group_id ON accounts(group_id);
CREATE INDEX idx_customers_last_name ON customers(last_name);
