-- Flyway migration: card_xref table
-- Source: COBOL copybook CVACT03Y.cpy (Card cross-reference, RECLN 50)
CREATE TABLE IF NOT EXISTS card_xref (
    xref_card_num      VARCHAR(16)   PRIMARY KEY,
    xref_cust_id       BIGINT        NOT NULL REFERENCES customers(cust_id),
    xref_acct_id       BIGINT        NOT NULL REFERENCES accounts(acct_id)
);

CREATE INDEX idx_card_xref_cust ON card_xref(xref_cust_id);
CREATE INDEX idx_card_xref_acct ON card_xref(xref_acct_id);
