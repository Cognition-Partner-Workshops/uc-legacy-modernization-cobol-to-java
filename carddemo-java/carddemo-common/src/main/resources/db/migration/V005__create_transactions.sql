-- Flyway migration: transactions table
-- Source: COBOL copybook CVTRA05Y.cpy (Transaction record, RECLN 350)
CREATE TABLE IF NOT EXISTS transactions (
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

CREATE INDEX idx_transactions_card ON transactions(card_num);
CREATE INDEX idx_transactions_type ON transactions(tran_type_cd);
CREATE INDEX idx_transactions_orig_ts ON transactions(orig_ts);
