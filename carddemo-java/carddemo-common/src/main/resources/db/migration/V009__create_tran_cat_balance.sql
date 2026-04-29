-- Flyway migration: tran_cat_balance table
-- Source: COBOL copybook CVTRA01Y.cpy (Transaction category balance, RECLN 50)
CREATE TABLE IF NOT EXISTS tran_cat_balance (
    acct_id            BIGINT        NOT NULL REFERENCES accounts(acct_id),
    tran_type_cd       VARCHAR(2)    NOT NULL,
    tran_cat_cd        INTEGER       NOT NULL,
    tran_cat_bal       DECIMAL(11,2) NOT NULL DEFAULT 0,
    PRIMARY KEY (acct_id, tran_type_cd, tran_cat_cd)
);
