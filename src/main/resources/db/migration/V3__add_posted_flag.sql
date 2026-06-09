-- Add posted flag to transactions for batch idempotency
ALTER TABLE transactions ADD COLUMN posted BOOLEAN NOT NULL DEFAULT FALSE;
