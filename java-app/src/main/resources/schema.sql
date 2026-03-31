-- CardDemo Database Schema
-- Converted from COBOL VSAM file definitions to relational tables

DROP TABLE IF EXISTS rejected_transaction;
DROP TABLE IF EXISTS daily_transaction;
DROP TABLE IF EXISTS transaction;
DROP TABLE IF EXISTS transaction_category_balance;
DROP TABLE IF EXISTS transaction_category;
DROP TABLE IF EXISTS transaction_type;
DROP TABLE IF EXISTS disclosure_group;
DROP TABLE IF EXISTS card_data;
DROP TABLE IF EXISTS card_xref;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS account;
DROP TABLE IF EXISTS user_security;

-- User Security (from CSUSR01Y copybook - replaces USRSEC VSAM file)
CREATE TABLE user_security (
    user_id VARCHAR(8) NOT NULL PRIMARY KEY,
    first_name VARCHAR(20),
    last_name VARCHAR(20),
    password VARCHAR(8) NOT NULL,
    user_type VARCHAR(1) NOT NULL DEFAULT 'U'
);

-- Account Master (from CVACT01Y copybook - replaces ACCTDAT VSAM file)
CREATE TABLE account (
    acct_id BIGINT NOT NULL PRIMARY KEY,
    active_status VARCHAR(1) DEFAULT 'Y',
    curr_bal DECIMAL(12,2) DEFAULT 0.00,
    credit_limit DECIMAL(12,2) DEFAULT 0.00,
    cash_credit_limit DECIMAL(12,2) DEFAULT 0.00,
    open_date VARCHAR(10),
    expiration_date VARCHAR(10),
    reissue_date VARCHAR(10),
    curr_cyc_credit DECIMAL(12,2) DEFAULT 0.00,
    curr_cyc_debit DECIMAL(12,2) DEFAULT 0.00,
    addr_zip VARCHAR(10),
    group_id VARCHAR(10)
);

-- Customer Master (from CVCUS01Y copybook - replaces CUSTDAT VSAM file)
CREATE TABLE customer (
    cust_id BIGINT NOT NULL PRIMARY KEY,
    first_name VARCHAR(25),
    middle_name VARCHAR(25),
    last_name VARCHAR(25),
    address_line_1 VARCHAR(50),
    address_line_2 VARCHAR(50),
    address_line_3 VARCHAR(50),
    state_code VARCHAR(2),
    country_code VARCHAR(3),
    zip_code VARCHAR(10),
    phone_1 VARCHAR(15),
    phone_2 VARCHAR(15),
    ssn VARCHAR(9),
    govt_issued_id VARCHAR(20),
    dob VARCHAR(10),
    fico_score INT DEFAULT 0,
    eft_account_id VARCHAR(10),
    pri_card_holder_ind VARCHAR(1)
);

-- Card Cross-Reference (from CVACT03Y copybook - replaces CARDXREF VSAM file)
CREATE TABLE card_xref (
    card_num VARCHAR(16) NOT NULL PRIMARY KEY,
    cust_id BIGINT NOT NULL,
    acct_id BIGINT NOT NULL
);

-- Card Data (from CVACT02Y copybook - replaces CARDDAT VSAM file)
CREATE TABLE card_data (
    card_num VARCHAR(16) NOT NULL PRIMARY KEY,
    acct_id BIGINT,
    cvv_cd VARCHAR(3),
    embossed_name VARCHAR(50),
    expiration_date VARCHAR(10),
    active_status VARCHAR(1) DEFAULT 'Y'
);

-- Transaction Type Reference
CREATE TABLE transaction_type (
    type_cd VARCHAR(2) NOT NULL PRIMARY KEY,
    type_description VARCHAR(50)
);

-- Transaction Category Reference
CREATE TABLE transaction_category (
    type_cd VARCHAR(2) NOT NULL,
    cat_cd INT NOT NULL,
    cat_description VARCHAR(50),
    PRIMARY KEY (type_cd, cat_cd)
);

-- Transaction Master (from CVTRA05Y copybook - replaces TRANSACT VSAM file)
CREATE TABLE transaction (
    tran_id VARCHAR(16) NOT NULL PRIMARY KEY,
    type_cd VARCHAR(2),
    cat_cd INT,
    source VARCHAR(10),
    description VARCHAR(100),
    amount DECIMAL(12,2),
    merchant_id BIGINT DEFAULT 0,
    merchant_name VARCHAR(50),
    merchant_city VARCHAR(50),
    merchant_zip VARCHAR(10),
    card_num VARCHAR(16),
    orig_timestamp VARCHAR(26),
    proc_timestamp VARCHAR(26)
);

-- Daily Transaction Input (from CVTRA06Y copybook - replaces DAILYTRAN sequential file)
CREATE TABLE daily_transaction (
    tran_id VARCHAR(16) NOT NULL PRIMARY KEY,
    type_cd VARCHAR(2),
    cat_cd INT,
    source VARCHAR(10),
    description VARCHAR(100),
    amount DECIMAL(12,2),
    merchant_id BIGINT DEFAULT 0,
    merchant_name VARCHAR(50),
    merchant_city VARCHAR(50),
    merchant_zip VARCHAR(10),
    card_num VARCHAR(16),
    orig_timestamp VARCHAR(26),
    proc_timestamp VARCHAR(26)
);

-- Transaction Category Balance (from CVTRA01Y copybook - replaces TCATBALF VSAM file)
CREATE TABLE transaction_category_balance (
    acct_id BIGINT NOT NULL,
    type_cd VARCHAR(2) NOT NULL,
    cat_cd INT NOT NULL,
    balance DECIMAL(12,2) DEFAULT 0.00,
    PRIMARY KEY (acct_id, type_cd, cat_cd)
);

-- Disclosure Group (from CVTRA02Y copybook - replaces DISCGRP VSAM file)
CREATE TABLE disclosure_group (
    acct_group_id VARCHAR(10) NOT NULL,
    tran_type_cd VARCHAR(2) NOT NULL,
    tran_cat_cd INT NOT NULL,
    interest_rate DECIMAL(8,2) DEFAULT 0.00,
    PRIMARY KEY (acct_group_id, tran_type_cd, tran_cat_cd)
);

-- Rejected Transactions (for batch job output)
CREATE TABLE rejected_transaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tran_id VARCHAR(16),
    type_cd VARCHAR(2),
    cat_cd INT,
    source VARCHAR(10),
    description VARCHAR(100),
    amount DECIMAL(12,2),
    card_num VARCHAR(16),
    reject_reason_cd INT,
    reject_reason VARCHAR(200)
);
