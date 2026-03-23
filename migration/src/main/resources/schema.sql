-- CardDemo Database Schema
-- Migrated from COBOL VSAM files to relational tables

CREATE TABLE IF NOT EXISTS customers (
    cust_id BIGINT PRIMARY KEY,
    first_name VARCHAR(25),
    middle_name VARCHAR(25),
    last_name VARCHAR(25),
    address_line1 VARCHAR(50),
    address_line2 VARCHAR(50),
    address_line3 VARCHAR(50),
    state_code VARCHAR(2),
    country_code VARCHAR(3),
    zip_code VARCHAR(10),
    phone1 VARCHAR(15),
    phone2 VARCHAR(15),
    ssn VARCHAR(9),
    govt_issued_id VARCHAR(20),
    date_of_birth DATE,
    eft_account_id VARCHAR(10),
    primary_card_holder_ind VARCHAR(1),
    fico_credit_score INT
);

CREATE TABLE IF NOT EXISTS accounts (
    acct_id VARCHAR(11) PRIMARY KEY,
    active_status VARCHAR(1),
    current_balance DECIMAL(12,2),
    credit_limit DECIMAL(12,2),
    cash_credit_limit DECIMAL(12,2),
    open_date DATE,
    expiration_date DATE,
    reissue_date DATE,
    current_cycle_credit DECIMAL(12,2),
    current_cycle_debit DECIMAL(12,2),
    zip_code VARCHAR(10),
    group_id VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS cards (
    card_num VARCHAR(16) PRIMARY KEY,
    acct_id VARCHAR(11),
    cvv_code INT,
    embossed_name VARCHAR(50),
    expiration_date DATE,
    active_status VARCHAR(1)
);

CREATE TABLE IF NOT EXISTS card_xref (
    card_num VARCHAR(16) PRIMARY KEY,
    cust_id BIGINT,
    acct_id VARCHAR(11)
);

CREATE TABLE IF NOT EXISTS transactions (
    tran_id VARCHAR(16) PRIMARY KEY,
    type_cd VARCHAR(2),
    cat_cd INT,
    source VARCHAR(10),
    description VARCHAR(100),
    amount DECIMAL(12,2),
    merchant_id BIGINT,
    merchant_name VARCHAR(50),
    merchant_city VARCHAR(50),
    merchant_zip VARCHAR(10),
    card_num VARCHAR(16),
    orig_timestamp TIMESTAMP,
    proc_timestamp TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_security (
    user_id VARCHAR(8) PRIMARY KEY,
    first_name VARCHAR(20),
    last_name VARCHAR(20),
    password VARCHAR(72),
    user_type VARCHAR(1)
);

CREATE TABLE IF NOT EXISTS tran_cat_balance (
    acct_id VARCHAR(11),
    type_cd VARCHAR(2),
    cat_cd INT,
    balance DECIMAL(12,2),
    PRIMARY KEY (acct_id, type_cd, cat_cd)
);

CREATE TABLE IF NOT EXISTS disclosure_groups (
    acct_group_id VARCHAR(10),
    tran_type_cd VARCHAR(2),
    tran_cat_cd INT,
    interest_rate DECIMAL(6,2),
    PRIMARY KEY (acct_group_id, tran_type_cd, tran_cat_cd)
);

CREATE TABLE IF NOT EXISTS transaction_types (
    type_cd VARCHAR(2) PRIMARY KEY,
    type_description VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS transaction_categories (
    type_cd VARCHAR(2),
    cat_cd INT,
    category_description VARCHAR(50),
    PRIMARY KEY (type_cd, cat_cd)
);
