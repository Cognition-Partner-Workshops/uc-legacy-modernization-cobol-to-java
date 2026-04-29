-- Flyway migration: disclosure_groups table
-- Source: COBOL copybook CVTRA02Y.cpy (Disclosure group, RECLN 50)
CREATE TABLE IF NOT EXISTS disclosure_groups (
    acct_group_id      VARCHAR(10)   NOT NULL,
    tran_type_cd       VARCHAR(2)    NOT NULL,
    tran_cat_cd        INTEGER       NOT NULL,
    interest_rate      DECIMAL(6,2)  NOT NULL,
    PRIMARY KEY (acct_group_id, tran_type_cd, tran_cat_cd)
);
