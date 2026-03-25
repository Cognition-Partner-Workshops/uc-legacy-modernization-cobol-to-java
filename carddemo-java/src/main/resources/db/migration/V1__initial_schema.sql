-- CardDemo Database Schema
-- Migrated from VSAM KSDS files to relational tables

-- Users table (replaces USRSEC VSAM file / CSUSR01Y copybook)
CREATE TABLE users (
    user_id         VARCHAR(8) PRIMARY KEY,
    first_name      VARCHAR(20) NOT NULL,
    last_name       VARCHAR(20) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    user_type       VARCHAR(1) NOT NULL CHECK (user_type IN ('A', 'U')),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Accounts table (replaces ACCTDAT VSAM file / CVACT01Y copybook)
CREATE TABLE accounts (
    account_id        BIGINT PRIMARY KEY,
    active_status     VARCHAR(1) NOT NULL DEFAULT 'Y',
    current_balance   DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    credit_limit      DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    cash_credit_limit DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    open_date         DATE NOT NULL,
    expiration_date   DATE,
    reissue_date      DATE,
    cycle_credit      DECIMAL(12,2) DEFAULT 0.00,
    cycle_debit       DECIMAL(12,2) DEFAULT 0.00,
    zip_code          VARCHAR(10),
    group_id          VARCHAR(10),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customers table (replaces CUSTDAT VSAM file / CVCUS01Y copybook)
CREATE TABLE customers (
    customer_id     BIGINT PRIMARY KEY,
    first_name      VARCHAR(25) NOT NULL,
    middle_name     VARCHAR(25),
    last_name       VARCHAR(25) NOT NULL,
    addr_line_1     VARCHAR(50),
    addr_line_2     VARCHAR(50),
    addr_line_3     VARCHAR(50),
    state_code      VARCHAR(2),
    country_code    VARCHAR(3),
    zip_code        VARCHAR(10),
    phone_1         VARCHAR(15),
    phone_2         VARCHAR(15),
    ssn             VARCHAR(9),
    govt_id         VARCHAR(20),
    date_of_birth   DATE,
    eft_account_id  VARCHAR(10),
    primary_holder  VARCHAR(1),
    fico_score      INTEGER,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Cards table (replaces CARDDAT VSAM file / CVACT02Y copybook)
CREATE TABLE cards (
    card_number     VARCHAR(16) PRIMARY KEY,
    account_id      BIGINT NOT NULL REFERENCES accounts(account_id),
    cvv_code        VARCHAR(3) NOT NULL,
    embossed_name   VARCHAR(50),
    expiration_date DATE,
    active_status   VARCHAR(1) NOT NULL DEFAULT 'Y',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_cards_account_id ON cards(account_id);

-- Card cross-reference table (replaces CCXREF VSAM file / CVACT03Y copybook)
CREATE TABLE card_xref (
    card_number     VARCHAR(16) PRIMARY KEY REFERENCES cards(card_number),
    customer_id     BIGINT NOT NULL REFERENCES customers(customer_id),
    account_id      BIGINT NOT NULL REFERENCES accounts(account_id)
);

CREATE INDEX idx_card_xref_customer ON card_xref(customer_id);
CREATE INDEX idx_card_xref_account ON card_xref(account_id);

-- Transactions table (replaces TRANSACT VSAM file / CVTRA05Y copybook)
CREATE TABLE transactions (
    transaction_id  VARCHAR(16) PRIMARY KEY,
    type_code       VARCHAR(2) NOT NULL,
    category_code   INTEGER NOT NULL,
    source          VARCHAR(10),
    description     VARCHAR(100),
    amount          DECIMAL(11,2) NOT NULL,
    merchant_id     BIGINT,
    merchant_name   VARCHAR(50),
    merchant_city   VARCHAR(50),
    merchant_zip    VARCHAR(10),
    card_number     VARCHAR(16) REFERENCES cards(card_number),
    originated_ts   TIMESTAMP,
    processed_ts    TIMESTAMP,
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transactions_card ON transactions(card_number);
CREATE INDEX idx_transactions_type ON transactions(type_code, category_code);
CREATE INDEX idx_transactions_orig_ts ON transactions(originated_ts);

-- Transaction category balances (replaces TCATBAL VSAM file / CVTRA01Y copybook)
CREATE TABLE tran_cat_balances (
    account_id      BIGINT NOT NULL REFERENCES accounts(account_id),
    type_code       VARCHAR(2) NOT NULL,
    category_code   INTEGER NOT NULL,
    balance         DECIMAL(11,2) NOT NULL DEFAULT 0.00,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (account_id, type_code, category_code)
);

-- Daily transactions staging table (replaces DALYTRAN file / CVTRA06Y copybook)
CREATE TABLE daily_transactions (
    transaction_id  VARCHAR(16) PRIMARY KEY,
    type_code       VARCHAR(2) NOT NULL,
    category_code   INTEGER NOT NULL,
    source          VARCHAR(10),
    description     VARCHAR(100),
    amount          DECIMAL(11,2) NOT NULL,
    merchant_id     BIGINT,
    merchant_name   VARCHAR(50),
    merchant_city   VARCHAR(50),
    merchant_zip    VARCHAR(10),
    card_number     VARCHAR(16),
    originated_ts   TIMESTAMP,
    processed_ts    TIMESTAMP,
    status          VARCHAR(10) DEFAULT 'PENDING',
    rejection_reason VARCHAR(100),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
