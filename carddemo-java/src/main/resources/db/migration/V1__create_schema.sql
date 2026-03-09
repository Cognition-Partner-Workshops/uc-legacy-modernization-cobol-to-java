-- ============================================================================
-- CardDemo Database Schema - Migrated from VSAM KSDS
-- ============================================================================
-- This schema maps COBOL copybook record layouts to relational tables.
-- Original VSAM datasets used fixed-length records with EBCDIC encoding.
-- ============================================================================

-- Accounts (from CVACT01Y.cpy - ACCOUNT-RECORD, RECLN 300)
-- VSAM Dataset: AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS
-- Key: ACCT-ID PIC 9(11)
CREATE TABLE accounts (
    acct_id             BIGINT          NOT NULL PRIMARY KEY,
    acct_active_status  VARCHAR(1)      NOT NULL DEFAULT 'Y',
    acct_curr_bal       DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    acct_credit_limit   DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    acct_cash_credit_limit DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    acct_open_date      VARCHAR(10),
    acct_expiration_date VARCHAR(10),
    acct_reissue_date   VARCHAR(10),
    acct_curr_cyc_credit DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    acct_curr_cyc_debit DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    acct_addr_zip       VARCHAR(10),
    acct_group_id       VARCHAR(10)
);

-- Customers (from CVCUS01Y.cpy - CUSTOMER-RECORD, RECLN 500)
-- VSAM Dataset: AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS
-- Key: CUST-ID PIC 9(09)
CREATE TABLE customers (
    cust_id                 BIGINT      NOT NULL PRIMARY KEY,
    cust_first_name         VARCHAR(25),
    cust_middle_name        VARCHAR(25),
    cust_last_name          VARCHAR(25),
    cust_addr_line_1        VARCHAR(50),
    cust_addr_line_2        VARCHAR(50),
    cust_addr_line_3        VARCHAR(50),
    cust_addr_state_cd      VARCHAR(2),
    cust_addr_country_cd    VARCHAR(3),
    cust_addr_zip           VARCHAR(10),
    cust_phone_num_1        VARCHAR(15),
    cust_phone_num_2        VARCHAR(15),
    cust_ssn                BIGINT,
    cust_govt_issued_id     VARCHAR(20),
    cust_dob_yyyy_mm_dd     VARCHAR(10),
    cust_eft_account_id     VARCHAR(10),
    cust_pri_card_holder_ind VARCHAR(1),
    cust_fico_credit_score  INTEGER
);

-- Cards (from CVACT02Y.cpy - CARD-RECORD, RECLN 150)
-- VSAM Dataset: AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS
-- Key: CARD-NUM PIC X(16)
CREATE TABLE cards (
    card_num            VARCHAR(16)     NOT NULL PRIMARY KEY,
    card_acct_id        BIGINT          NOT NULL,
    card_cvv_cd         INTEGER         NOT NULL,
    card_embossed_name  VARCHAR(50),
    card_expiration_date VARCHAR(10),
    card_active_status  VARCHAR(1)      NOT NULL DEFAULT 'Y',
    CONSTRAINT fk_card_account FOREIGN KEY (card_acct_id) REFERENCES accounts(acct_id)
);

CREATE INDEX idx_cards_acct_id ON cards(card_acct_id);

-- Card-Account Cross Reference (from CVACT03Y.cpy - CARD-XREF-RECORD, RECLN 50)
-- VSAM Dataset: AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS
-- Key: XREF-CARD-NUM PIC X(16)
CREATE TABLE card_xref (
    xref_card_num       VARCHAR(16)     NOT NULL PRIMARY KEY,
    xref_cust_id        BIGINT          NOT NULL,
    xref_acct_id        BIGINT          NOT NULL,
    CONSTRAINT fk_xref_customer FOREIGN KEY (xref_cust_id) REFERENCES customers(cust_id),
    CONSTRAINT fk_xref_account FOREIGN KEY (xref_acct_id) REFERENCES accounts(acct_id)
);

CREATE INDEX idx_xref_cust_id ON card_xref(xref_cust_id);
CREATE INDEX idx_xref_acct_id ON card_xref(xref_acct_id);

-- Transactions (from CVTRA05Y.cpy - TRAN-RECORD, RECLN 350)
-- VSAM Dataset: AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS
-- Key: TRAN-ID PIC X(16)
CREATE TABLE transactions (
    tran_id             VARCHAR(16)     NOT NULL PRIMARY KEY,
    tran_type_cd        VARCHAR(2),
    tran_cat_cd         INTEGER,
    tran_source         VARCHAR(10),
    tran_desc           VARCHAR(100),
    tran_amt            DECIMAL(11, 2),
    tran_merchant_id    BIGINT,
    tran_merchant_name  VARCHAR(50),
    tran_merchant_city  VARCHAR(50),
    tran_merchant_zip   VARCHAR(10),
    tran_card_num       VARCHAR(16),
    tran_orig_ts        VARCHAR(26),
    tran_proc_ts        VARCHAR(26),
    CONSTRAINT fk_tran_card FOREIGN KEY (tran_card_num) REFERENCES cards(card_num)
);

CREATE INDEX idx_tran_card_num ON transactions(tran_card_num);
CREATE INDEX idx_tran_type_cd ON transactions(tran_type_cd);

-- Daily Transactions (from CVTRA06Y.cpy - DALYTRAN-RECORD, RECLN 350)
-- Sequential file: AWS.M2.CARDDEMO.DALYTRAN.PS
CREATE TABLE daily_transactions (
    id                      BIGINT          NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    dalytran_id             VARCHAR(16)     NOT NULL,
    dalytran_type_cd        VARCHAR(2),
    dalytran_cat_cd         INTEGER,
    dalytran_source         VARCHAR(10),
    dalytran_desc           VARCHAR(100),
    dalytran_amt            DECIMAL(11, 2),
    dalytran_merchant_id    BIGINT,
    dalytran_merchant_name  VARCHAR(50),
    dalytran_merchant_city  VARCHAR(50),
    dalytran_merchant_zip   VARCHAR(10),
    dalytran_card_num       VARCHAR(16),
    dalytran_orig_ts        VARCHAR(26),
    dalytran_proc_ts        VARCHAR(26),
    processed               BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_dalytran_card_num ON daily_transactions(dalytran_card_num);
CREATE INDEX idx_dalytran_processed ON daily_transactions(processed);

-- User Security (from CSUSR01Y.cpy - SEC-USER-DATA, RECLN 80)
-- VSAM Dataset: AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS
-- Key: SEC-USR-ID PIC X(08)
CREATE TABLE users (
    usr_id              VARCHAR(8)      NOT NULL PRIMARY KEY,
    usr_first_name      VARCHAR(20),
    usr_last_name       VARCHAR(20),
    usr_pwd             VARCHAR(255),
    usr_type            VARCHAR(1)      NOT NULL DEFAULT 'U',
    CONSTRAINT chk_usr_type CHECK (usr_type IN ('A', 'U'))
);

-- Transaction Category Balance (from CVTRA01Y.cpy - TRAN-CAT-BAL-RECORD, RECLN 50)
-- VSAM Dataset: AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS
-- Composite Key: TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD
CREATE TABLE tran_cat_balances (
    trancat_acct_id     BIGINT          NOT NULL,
    trancat_type_cd     VARCHAR(2)      NOT NULL,
    trancat_cd          INTEGER         NOT NULL,
    trancat_bal         DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
    PRIMARY KEY (trancat_acct_id, trancat_type_cd, trancat_cd),
    CONSTRAINT fk_tcb_account FOREIGN KEY (trancat_acct_id) REFERENCES accounts(acct_id)
);

-- Disclosure Groups (from CVTRA02Y.cpy - DISCGRP-RECORD, RECLN 50)
-- VSAM Dataset: AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS
-- Composite Key: DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD
CREATE TABLE disclosure_groups (
    dis_acct_group_id   VARCHAR(10)     NOT NULL,
    dis_tran_type_cd    VARCHAR(2)      NOT NULL,
    dis_tran_cat_cd     INTEGER         NOT NULL,
    dis_int_rate        DECIMAL(7, 4)   NOT NULL DEFAULT 0.0000,
    dis_fee_amt         DECIMAL(9, 2)   NOT NULL DEFAULT 0.00,
    PRIMARY KEY (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd)
);

-- Transaction Types (from CVTRA03Y.cpy - TRAN-TYPE-RECORD, RECLN 60)
-- VSAM Dataset: AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS
CREATE TABLE transaction_types (
    tran_type_cd        VARCHAR(2)      NOT NULL PRIMARY KEY,
    tran_type_desc      VARCHAR(50)
);

-- Transaction Categories (from CVTRA04Y.cpy - TRAN-CAT-RECORD, RECLN 60)
-- VSAM Dataset: AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS
CREATE TABLE transaction_categories (
    tran_cat_cd         INTEGER         NOT NULL PRIMARY KEY,
    tran_cat_desc       VARCHAR(50)
);
