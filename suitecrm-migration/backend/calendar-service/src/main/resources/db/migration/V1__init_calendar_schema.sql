CREATE SCHEMA IF NOT EXISTS calendar_schema;
SET search_path TO calendar_schema;

CREATE TABLE calls (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    date_start TIMESTAMP,
    date_end TIMESTAMP,
    direction VARCHAR(100),
    status VARCHAR(100) DEFAULT 'Planned',
    description TEXT,
    duration_hours INTEGER,
    duration_minutes INTEGER,
    parent_type VARCHAR(100),
    parent_id UUID,
    reminder_time INTEGER,
    email_reminder_time INTEGER,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE meetings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    date_start TIMESTAMP,
    date_end TIMESTAMP,
    status VARCHAR(100) DEFAULT 'Planned',
    type VARCHAR(100),
    location VARCHAR(255),
    description TEXT,
    duration_hours INTEGER,
    duration_minutes INTEGER,
    parent_type VARCHAR(100),
    parent_id UUID,
    reminder_time INTEGER,
    email_reminder_time INTEGER,
    external_id VARCHAR(255),
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE meeting_invitees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meeting_id UUID NOT NULL REFERENCES meetings(id),
    invitee_id UUID NOT NULL,
    invitee_type VARCHAR(50),
    accept_status VARCHAR(50) DEFAULT 'none',
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE calls_reschedule (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    call_id UUID NOT NULL REFERENCES calls(id),
    reason VARCHAR(100),
    old_date_start TIMESTAMP,
    new_date_start TIMESTAMP,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_calls_date_start ON calls(date_start) WHERE deleted = FALSE;
CREATE INDEX idx_calls_assigned ON calls(assigned_user_id) WHERE deleted = FALSE;
CREATE INDEX idx_calls_status ON calls(status) WHERE deleted = FALSE;
CREATE INDEX idx_meetings_date_start ON meetings(date_start) WHERE deleted = FALSE;
CREATE INDEX idx_meetings_assigned ON meetings(assigned_user_id) WHERE deleted = FALSE;
CREATE INDEX idx_meetings_status ON meetings(status) WHERE deleted = FALSE;
CREATE INDEX idx_meeting_invitees_meeting ON meeting_invitees(meeting_id) WHERE deleted = FALSE;
