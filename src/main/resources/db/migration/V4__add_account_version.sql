-- Add optimistic locking version column to accounts
ALTER TABLE accounts ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
