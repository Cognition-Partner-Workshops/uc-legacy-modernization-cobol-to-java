-- Flyway migration: transaction_categories table
-- Source: COBOL copybook CVTRA04Y.cpy (Transaction category, RECLN 60)
CREATE TABLE IF NOT EXISTS transaction_categories (
    tran_type_cd       VARCHAR(2)    NOT NULL,
    tran_cat_cd        INTEGER       NOT NULL,
    tran_cat_type_desc VARCHAR(50),
    PRIMARY KEY (tran_type_cd, tran_cat_cd)
);
