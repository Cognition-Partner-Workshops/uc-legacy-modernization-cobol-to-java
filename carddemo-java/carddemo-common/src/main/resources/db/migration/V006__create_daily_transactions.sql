-- Flyway migration: daily_transactions table
-- Source: COBOL copybook CVTRA06Y.cpy (Daily transaction, RECLN 350)
CREATE TABLE IF NOT EXISTS daily_transactions (
    tran_id            VARCHAR(16)   PRIMARY KEY,
    tran_type_cd       VARCHAR(2)    NOT NULL,
    tran_cat_cd        INTEGER       NOT NULL,
    tran_source        VARCHAR(10),
    tran_desc          VARCHAR(100),
    tran_amt           DECIMAL(11,2) NOT NULL,
    merchant_id        BIGINT,
    merchant_name      VARCHAR(50),
    merchant_city      VARCHAR(50),
    merchant_zip       VARCHAR(10),
    card_num           VARCHAR(16)   NOT NULL,
    orig_ts            TIMESTAMP,
    proc_ts            TIMESTAMP
);
