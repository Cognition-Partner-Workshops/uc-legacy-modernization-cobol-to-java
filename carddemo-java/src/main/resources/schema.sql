-- CardDemo Schema - Translated from COBOL copybook definitions

CREATE TABLE IF NOT EXISTS users (
    user_id         VARCHAR(8)   NOT NULL PRIMARY KEY,
    first_name      VARCHAR(20),
    last_name       VARCHAR(20),
    password        VARCHAR(72)  NOT NULL,
    user_type       VARCHAR(1)   NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
    account_id           BIGINT         NOT NULL PRIMARY KEY,
    active_status        VARCHAR(1),
    current_balance      DECIMAL(12,2),
    credit_limit         DECIMAL(12,2),
    cash_credit_limit    DECIMAL(12,2),
    open_date            DATE,
    expiration_date      DATE,
    reissue_date         DATE,
    current_cycle_credit DECIMAL(12,2),
    current_cycle_debit  DECIMAL(12,2),
    address_zip          VARCHAR(10),
    group_id             VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS cards (
    card_number      VARCHAR(16)  NOT NULL PRIMARY KEY,
    account_id       BIGINT       NOT NULL,
    cvv_code         VARCHAR(3),
    embossed_name    VARCHAR(50),
    expiration_date  DATE,
    active_status    VARCHAR(1)
);

CREATE TABLE IF NOT EXISTS customers (
    customer_id             BIGINT      NOT NULL PRIMARY KEY,
    first_name              VARCHAR(25),
    middle_name             VARCHAR(25),
    last_name               VARCHAR(25),
    address_line_1          VARCHAR(50),
    address_line_2          VARCHAR(50),
    address_line_3          VARCHAR(50),
    state_code              VARCHAR(2),
    country_code            VARCHAR(3),
    zip_code                VARCHAR(10),
    phone_number_1          VARCHAR(15),
    phone_number_2          VARCHAR(15),
    ssn                     VARCHAR(9),
    govt_issued_id          VARCHAR(20),
    date_of_birth           DATE,
    eft_account_id          VARCHAR(10),
    primary_card_holder_ind VARCHAR(1),
    fico_credit_score       INT
);

CREATE TABLE IF NOT EXISTS card_cross_references (
    card_number  VARCHAR(16)  NOT NULL PRIMARY KEY,
    customer_id  BIGINT       NOT NULL,
    account_id   BIGINT       NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id      VARCHAR(16)    NOT NULL PRIMARY KEY,
    type_code           VARCHAR(2),
    category_code       INT,
    source              VARCHAR(10),
    description         VARCHAR(100),
    amount              DECIMAL(11,2),
    merchant_id         BIGINT,
    merchant_name       VARCHAR(50),
    merchant_city       VARCHAR(50),
    merchant_zip        VARCHAR(10),
    card_number         VARCHAR(16),
    origin_timestamp    TIMESTAMP,
    processed_timestamp TIMESTAMP
);
