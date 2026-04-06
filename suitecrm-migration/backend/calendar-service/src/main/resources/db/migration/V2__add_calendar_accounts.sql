-- V2: Add calendar accounts table for CalDav/Exchange integration
CREATE TABLE IF NOT EXISTS calendar_schema.calendar_accounts (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    url VARCHAR(500),
    username VARCHAR(255),
    password VARCHAR(255),
    auth_type VARCHAR(50),
    oauth_connection_id UUID,
    sync_enabled BOOLEAN DEFAULT TRUE,
    sync_interval INTEGER DEFAULT 15,
    last_sync TIMESTAMP,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_cal_acct_user ON calendar_schema.calendar_accounts(assigned_user_id);
