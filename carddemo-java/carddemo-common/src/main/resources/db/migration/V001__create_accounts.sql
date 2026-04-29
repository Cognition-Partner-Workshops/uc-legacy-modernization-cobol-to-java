-- Flyway migration: accounts table
-- Source: COBOL copybook CVACT01Y.cpy (Account record, RECLN 300)
CREATE TABLE IF NOT EXISTS accounts (
    acct_id           BIGINT        PRIMARY KEY,
    active_status     VARCHAR(1)    NOT NULL,
    curr_bal          DECIMAL(12,2) NOT NULL DEFAULT 0,
    credit_limit      DECIMAL(12,2) NOT NULL DEFAULT 0,
    cash_credit_limit DECIMAL(12,2) NOT NULL DEFAULT 0,
    open_date         DATE,
    expiration_date   DATE,
    reissue_date      DATE,
    curr_cyc_credit   DECIMAL(12,2) NOT NULL DEFAULT 0,
    curr_cyc_debit    DECIMAL(12,2) NOT NULL DEFAULT 0,
    addr_zip          VARCHAR(10),
    group_id          VARCHAR(10)
);

CREATE INDEX idx_accounts_status ON accounts(active_status);
CREATE INDEX idx_accounts_group ON accounts(group_id);
