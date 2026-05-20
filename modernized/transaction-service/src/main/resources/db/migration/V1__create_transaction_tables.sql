CREATE TABLE transaction.transactions (
    tran_id           VARCHAR(16) PRIMARY KEY,
    type_cd           CHAR(2),
    cat_cd            INTEGER,
    source            VARCHAR(10),
    description       VARCHAR(100),
    amount            NUMERIC(11,2),
    merchant_id       VARCHAR(9),
    merchant_name     VARCHAR(50),
    merchant_city     VARCHAR(50),
    merchant_zip      VARCHAR(10),
    card_num          VARCHAR(16),
    orig_ts           VARCHAR(26),
    proc_ts           VARCHAR(26),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transaction.daily_transactions (
    tran_id           VARCHAR(16) PRIMARY KEY,
    type_cd           CHAR(2),
    cat_cd            INTEGER,
    source            VARCHAR(10),
    description       VARCHAR(100),
    amount            NUMERIC(11,2),
    merchant_id       VARCHAR(9),
    merchant_name     VARCHAR(50),
    merchant_city     VARCHAR(50),
    merchant_zip      VARCHAR(10),
    card_num          VARCHAR(16),
    orig_ts           VARCHAR(26),
    proc_ts           VARCHAR(26),
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transaction.tran_types (
    tran_type     CHAR(2) PRIMARY KEY,
    type_desc     VARCHAR(50)
);

CREATE TABLE transaction.tran_categories (
    type_cd       CHAR(2) NOT NULL,
    cat_cd        INTEGER NOT NULL,
    cat_desc      VARCHAR(50),
    PRIMARY KEY (type_cd, cat_cd)
);

CREATE TABLE transaction.disclosure_groups (
    acct_group_id VARCHAR(10) NOT NULL,
    tran_type_cd  CHAR(2) NOT NULL,
    tran_cat_cd   INTEGER NOT NULL,
    int_rate      NUMERIC(6,2),
    PRIMARY KEY (acct_group_id, tran_type_cd, tran_cat_cd)
);

CREATE TABLE transaction.tran_cat_balances (
    acct_id       VARCHAR(11) NOT NULL,
    type_cd       CHAR(2) NOT NULL,
    cat_cd        INTEGER NOT NULL,
    balance       NUMERIC(11,2) DEFAULT 0,
    PRIMARY KEY (acct_id, type_cd, cat_cd)
);

CREATE TABLE transaction.rejected_transactions (
    id            SERIAL PRIMARY KEY,
    tran_id       VARCHAR(16),
    card_num      VARCHAR(16),
    amount        NUMERIC(11,2),
    reason_code   INTEGER,
    reason_desc   VARCHAR(100),
    rejected_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_txn_card_num ON transaction.transactions(card_num);
CREATE INDEX idx_daily_txn_card_num ON transaction.daily_transactions(card_num);
