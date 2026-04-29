-- Flyway migration: transaction_types table
-- Source: COBOL copybook CVTRA03Y.cpy (Transaction type, RECLN 60)
CREATE TABLE IF NOT EXISTS transaction_types (
    tran_type          VARCHAR(2)    PRIMARY KEY,
    tran_type_desc     VARCHAR(50)
);
