CREATE TABLE account.accounts (
    acct_id               VARCHAR(11) PRIMARY KEY,
    active_status         CHAR(1) NOT NULL DEFAULT 'Y',
    curr_bal              NUMERIC(12,2) DEFAULT 0,
    credit_limit          NUMERIC(12,2) DEFAULT 0,
    cash_credit_limit     NUMERIC(12,2) DEFAULT 0,
    open_date             VARCHAR(10),
    expiration_date       VARCHAR(10),
    reissue_date          VARCHAR(10),
    curr_cyc_credit       NUMERIC(12,2) DEFAULT 0,
    curr_cyc_debit        NUMERIC(12,2) DEFAULT 0,
    addr_zip              VARCHAR(10),
    group_id              VARCHAR(10),
    version               BIGINT DEFAULT 0,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
