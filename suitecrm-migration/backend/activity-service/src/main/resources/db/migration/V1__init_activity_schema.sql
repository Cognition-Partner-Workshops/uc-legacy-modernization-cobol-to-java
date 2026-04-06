CREATE SCHEMA IF NOT EXISTS activity_schema;
SET search_path TO activity_schema;

CREATE TABLE meetings (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    date_start          TIMESTAMP,
    date_end            TIMESTAMP,
    duration_hours      INTEGER,
    duration_minutes    INTEGER,
    status              VARCHAR(50) DEFAULT 'Planned',
    type                VARCHAR(50) DEFAULT 'Sugar',
    location            VARCHAR(255),
    description         TEXT,
    parent_type         VARCHAR(50),
    parent_id           UUID,
    reminder_time       INTEGER,
    assigned_user_id    UUID,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_meeting_status CHECK (status IN ('Planned','Held','Not Held'))
);

CREATE INDEX idx_meetings_date ON meetings(date_start);
CREATE INDEX idx_meetings_status ON meetings(status);
CREATE INDEX idx_meetings_assigned ON meetings(assigned_user_id);
CREATE INDEX idx_meetings_parent ON meetings(parent_type, parent_id);

CREATE TABLE calls (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    date_start          TIMESTAMP,
    date_end            TIMESTAMP,
    duration_hours      INTEGER,
    duration_minutes    INTEGER,
    status              VARCHAR(50) DEFAULT 'Planned',
    direction           VARCHAR(50),
    description         TEXT,
    parent_type         VARCHAR(50),
    parent_id           UUID,
    reminder_time       INTEGER,
    assigned_user_id    UUID,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_call_status CHECK (status IN ('Planned','Held','Not Held')),
    CONSTRAINT chk_call_direction CHECK (direction IN ('Inbound','Outbound'))
);

CREATE INDEX idx_calls_date ON calls(date_start);
CREATE INDEX idx_calls_status ON calls(status);
CREATE INDEX idx_calls_assigned ON calls(assigned_user_id);

CREATE TABLE tasks (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    status              VARCHAR(50) DEFAULT 'Not Started',
    priority            VARCHAR(50) DEFAULT 'Medium',
    date_start          DATE,
    date_due            DATE,
    description         TEXT,
    parent_type         VARCHAR(50),
    parent_id           UUID,
    contact_id          UUID,
    assigned_user_id    UUID,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_task_status CHECK (status IN ('Not Started','In Progress','Completed','Pending Input','Deferred')),
    CONSTRAINT chk_task_priority CHECK (priority IN ('High','Medium','Low'))
);

CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_due ON tasks(date_due);
CREATE INDEX idx_tasks_assigned ON tasks(assigned_user_id);

CREATE TABLE notes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    parent_type         VARCHAR(50),
    parent_id           UUID,
    contact_id          UUID,
    filename            VARCHAR(255),
    file_mime_type      VARCHAR(100),
    file_url            VARCHAR(500),
    assigned_user_id    UUID,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_notes_parent ON notes(parent_type, parent_id);

CREATE TABLE emails (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(500),
    from_addr           VARCHAR(255),
    to_addrs            TEXT,
    cc_addrs            TEXT,
    bcc_addrs           TEXT,
    date_sent           TIMESTAMP,
    message_id          VARCHAR(255),
    type                VARCHAR(50),
    status              VARCHAR(50) DEFAULT 'unread',
    intent              VARCHAR(50) DEFAULT 'pick',
    parent_type         VARCHAR(50),
    parent_id           UUID,
    description         TEXT,
    description_html    TEXT,
    assigned_user_id    UUID,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_emails_date ON emails(date_sent);
CREATE INDEX idx_emails_parent ON emails(parent_type, parent_id);
CREATE INDEX idx_emails_status ON emails(status);

-- Meeting/Call invitees
CREATE TABLE meeting_users (
    meeting_id      UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL,
    accept_status   VARCHAR(50) DEFAULT 'none',
    required        BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (meeting_id, user_id)
);

CREATE TABLE call_users (
    call_id         UUID NOT NULL REFERENCES calls(id) ON DELETE CASCADE,
    user_id         UUID NOT NULL,
    accept_status   VARCHAR(50) DEFAULT 'none',
    required        BOOLEAN DEFAULT TRUE,
    PRIMARY KEY (call_id, user_id)
);
