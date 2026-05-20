CREATE TABLE card.cards (
    card_num          VARCHAR(16) PRIMARY KEY,
    acct_id           VARCHAR(11) NOT NULL,
    cvv_cd            INTEGER,
    embossed_name     VARCHAR(50),
    expiration_date   VARCHAR(10),
    active_status     CHAR(1) DEFAULT 'Y',
    version           BIGINT DEFAULT 0,
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE card.card_xref (
    card_num   VARCHAR(16) PRIMARY KEY,
    cust_id    VARCHAR(9) NOT NULL,
    acct_id    VARCHAR(11) NOT NULL
);

CREATE INDEX idx_card_xref_cust ON card.card_xref(cust_id);
CREATE INDEX idx_card_xref_acct ON card.card_xref(acct_id);
CREATE INDEX idx_cards_acct_id ON card.cards(acct_id);
