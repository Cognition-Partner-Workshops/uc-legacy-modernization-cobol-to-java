-- Schema derived from COBOL copybooks
-- CVACT02Y.cpy -> cards table
-- CVACT01Y.cpy -> accounts table
-- CVACT03Y.cpy -> card_xref table

CREATE TABLE IF NOT EXISTS cards (
    card_number     VARCHAR(16)   NOT NULL PRIMARY KEY,  -- CARD-NUM PIC X(16)
    account_id      VARCHAR(11)   NOT NULL,              -- CARD-ACCT-ID PIC 9(11)
    cvv_code        VARCHAR(3),                          -- CARD-CVV-CD PIC 9(03)
    embossed_name   VARCHAR(50),                         -- CARD-EMBOSSED-NAME PIC X(50)
    expiration_date VARCHAR(10),                         -- CARD-EXPIRAION-DATE PIC X(10)
    active_status   VARCHAR(1)                           -- CARD-ACTIVE-STATUS PIC X(01)
);

CREATE TABLE IF NOT EXISTS accounts (
    account_id        VARCHAR(11)    NOT NULL PRIMARY KEY,  -- ACCT-ID PIC 9(11)
    active_status     VARCHAR(1),                           -- ACCT-ACTIVE-STATUS PIC X(01)
    current_balance   DECIMAL(12,2),                        -- ACCT-CURR-BAL PIC S9(10)V99
    credit_limit      DECIMAL(12,2),                        -- ACCT-CREDIT-LIMIT PIC S9(10)V99
    cash_credit_limit DECIMAL(12,2),                        -- ACCT-CASH-CREDIT-LIMIT PIC S9(10)V99
    open_date         VARCHAR(10),                          -- ACCT-OPEN-DATE PIC X(10)
    expiration_date   VARCHAR(10),                          -- ACCT-EXPIRAION-DATE PIC X(10)
    reissue_date      VARCHAR(10)                           -- ACCT-REISSUE-DATE PIC X(10)
);

CREATE TABLE IF NOT EXISTS card_xref (
    card_number  VARCHAR(16) NOT NULL PRIMARY KEY,  -- XREF-CARD-NUM PIC X(16)
    customer_id  VARCHAR(9),                        -- XREF-CUST-ID PIC 9(09)
    account_id   VARCHAR(11)                        -- XREF-ACCT-ID PIC 9(11)
);
