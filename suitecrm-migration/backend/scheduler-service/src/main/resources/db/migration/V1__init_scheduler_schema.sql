CREATE SCHEMA IF NOT EXISTS scheduler_schema;
SET search_path TO scheduler_schema;

CREATE TABLE schedulers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(255) NOT NULL,
    job VARCHAR(255), job_interval VARCHAR(100),
    time_from VARCHAR(20), time_to VARCHAR(20), last_run TIMESTAMP,
    status VARCHAR(100) DEFAULT 'Active', catch_up BOOLEAN DEFAULT TRUE,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(), date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE scheduler_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(255) NOT NULL,
    scheduler_id UUID REFERENCES schedulers(id),
    execute_time TIMESTAMP, status VARCHAR(100) DEFAULT 'queued', resolution VARCHAR(100),
    message TEXT, target VARCHAR(255), data TEXT,
    retry_count INTEGER DEFAULT 0, failure_count INTEGER DEFAULT 0,
    assigned_user_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(), date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE business_hours (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(255) NOT NULL,
    day_of_week INTEGER NOT NULL, open_time TIME, close_time TIME,
    is_open BOOLEAN DEFAULT TRUE, timezone VARCHAR(100),
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(), date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE resource_calendars (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(255) NOT NULL,
    user_id UUID, event_date DATE, event_type VARCHAR(100),
    description TEXT, is_available BOOLEAN DEFAULT TRUE,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(), date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_schedulers_status ON schedulers(status) WHERE deleted = FALSE;
CREATE INDEX idx_scheduler_jobs_status ON scheduler_jobs(status) WHERE deleted = FALSE;
CREATE INDEX idx_scheduler_jobs_scheduler ON scheduler_jobs(scheduler_id) WHERE deleted = FALSE;
CREATE INDEX idx_resource_cal_user ON resource_calendars(user_id) WHERE deleted = FALSE;
CREATE INDEX idx_resource_cal_date ON resource_calendars(event_date) WHERE deleted = FALSE;
