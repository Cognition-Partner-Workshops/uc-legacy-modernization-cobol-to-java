-- V2: Add case events, case updates, and bugs
-- Maps to AS-IS SuiteCRM modules: CaseEvents, CaseUpdates, Bugs

CREATE TABLE IF NOT EXISTS case_schema.case_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id UUID NOT NULL REFERENCES case_schema.cases(id),
    event_type VARCHAR(100),
    description TEXT,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS case_schema.case_updates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id UUID NOT NULL REFERENCES case_schema.cases(id),
    name VARCHAR(255),
    description TEXT,
    internal BOOLEAN DEFAULT FALSE,
    contact_id UUID,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS case_schema.bugs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bug_number INTEGER,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'New',
    priority VARCHAR(50) DEFAULT 'Medium',
    type VARCHAR(50),
    category VARCHAR(100),
    resolution VARCHAR(50),
    description TEXT,
    work_log TEXT,
    found_in_release VARCHAR(100),
    fixed_in_release VARCHAR(100),
    source VARCHAR(100),
    product_category VARCHAR(100),
    portal_viewable BOOLEAN DEFAULT FALSE,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_case_events_case_id ON case_schema.case_events(case_id);
CREATE INDEX idx_case_updates_case_id ON case_schema.case_updates(case_id);
CREATE INDEX idx_bugs_status ON case_schema.bugs(status);
CREATE INDEX idx_bugs_priority ON case_schema.bugs(priority);
CREATE INDEX idx_bugs_bug_number ON case_schema.bugs(bug_number);
