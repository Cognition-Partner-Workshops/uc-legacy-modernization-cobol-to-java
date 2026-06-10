-- Add posted flag to transactions for batch idempotency
ALTER TABLE transactions ADD COLUMN posted BOOLEAN NOT NULL DEFAULT FALSE;

-- Mark existing seed transactions as already posted (balances already reflect them)
UPDATE transactions SET posted = TRUE;
