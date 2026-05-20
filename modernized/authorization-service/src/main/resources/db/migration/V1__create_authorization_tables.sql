CREATE TABLE "authorization".pending_authorizations (
    auth_id         SERIAL PRIMARY KEY,
    card_num        VARCHAR(16) NOT NULL,
    transaction_id  VARCHAR(16),
    transaction_amt NUMERIC(11,2),
    auth_time       TIMESTAMP,
    auth_resp_code  CHAR(2),
    auth_resp_reason CHAR(4),
    approved_amt    NUMERIC(11,2),
    status          VARCHAR(20) DEFAULT 'PENDING',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pending_auth_card ON "authorization".pending_authorizations(card_num);
CREATE INDEX idx_pending_auth_status ON "authorization".pending_authorizations(status);
