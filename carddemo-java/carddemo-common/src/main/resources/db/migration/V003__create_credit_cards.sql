-- Flyway migration: credit_cards table
-- Source: COBOL copybook CVACT02Y.cpy (Card record, RECLN 150)
CREATE TABLE IF NOT EXISTS credit_cards (
    card_num           VARCHAR(16)   PRIMARY KEY,
    acct_id            BIGINT        NOT NULL REFERENCES accounts(acct_id),
    cvv_cd             INTEGER       NOT NULL,
    embossed_name      VARCHAR(50),
    expiration_date    DATE,
    active_status      VARCHAR(1)    NOT NULL
);

CREATE INDEX idx_credit_cards_acct ON credit_cards(acct_id);
