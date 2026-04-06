-- V4: Add call/meeting junction tables for contacts, leads, users
CREATE TABLE IF NOT EXISTS activity_schema.calls_contacts (
    id UUID PRIMARY KEY,
    call_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    required BOOLEAN DEFAULT TRUE,
    accept_status VARCHAR(25) DEFAULT 'none',
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_calls_contacts_call ON activity_schema.calls_contacts(call_id);

CREATE TABLE IF NOT EXISTS activity_schema.calls_leads (
    id UUID PRIMARY KEY,
    call_id UUID NOT NULL,
    lead_id UUID NOT NULL,
    required BOOLEAN DEFAULT TRUE,
    accept_status VARCHAR(25) DEFAULT 'none',
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS activity_schema.calls_users (
    id UUID PRIMARY KEY,
    call_id UUID NOT NULL,
    user_id UUID NOT NULL,
    required BOOLEAN DEFAULT TRUE,
    accept_status VARCHAR(25) DEFAULT 'none',
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS activity_schema.meetings_contacts (
    id UUID PRIMARY KEY,
    meeting_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    required BOOLEAN DEFAULT TRUE,
    accept_status VARCHAR(25) DEFAULT 'none',
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_meetings_contacts_meeting ON activity_schema.meetings_contacts(meeting_id);

CREATE TABLE IF NOT EXISTS activity_schema.meetings_leads (
    id UUID PRIMARY KEY,
    meeting_id UUID NOT NULL,
    lead_id UUID NOT NULL,
    required BOOLEAN DEFAULT TRUE,
    accept_status VARCHAR(25) DEFAULT 'none',
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS activity_schema.meetings_users (
    id UUID PRIMARY KEY,
    meeting_id UUID NOT NULL,
    user_id UUID NOT NULL,
    required BOOLEAN DEFAULT TRUE,
    accept_status VARCHAR(25) DEFAULT 'none',
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
