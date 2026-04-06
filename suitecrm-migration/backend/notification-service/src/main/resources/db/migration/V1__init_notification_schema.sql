CREATE SCHEMA IF NOT EXISTS notification_schema;
SET search_path TO notification_schema;

CREATE TABLE alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(255) NOT NULL,
    alert_type VARCHAR(100), url_redirect VARCHAR(512), target_module VARCHAR(100),
    description TEXT, is_read BOOLEAN DEFAULT FALSE,
    assigned_user_id UUID, created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(), date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE activity_feeds (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(255) NOT NULL,
    feed_type VARCHAR(100), related_module VARCHAR(100), related_id UUID,
    link_url VARCHAR(512), description TEXT,
    assigned_user_id UUID, created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(), date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE trackers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL, module_name VARCHAR(100), item_id UUID,
    item_summary VARCHAR(255), action VARCHAR(100), session_id VARCHAR(255),
    visible BOOLEAN DEFAULT TRUE,
    date_modified TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE favorites (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_name VARCHAR(100) NOT NULL, record_id UUID NOT NULL,
    assigned_user_id UUID NOT NULL,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE saved_searches (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(255) NOT NULL,
    search_module VARCHAR(100) NOT NULL, contents TEXT,
    assigned_user_id UUID NOT NULL,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(), date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_alerts_user ON alerts(assigned_user_id) WHERE deleted = FALSE;
CREATE INDEX idx_alerts_unread ON alerts(assigned_user_id) WHERE is_read = FALSE AND deleted = FALSE;
CREATE INDEX idx_activity_feeds_user ON activity_feeds(assigned_user_id) WHERE deleted = FALSE;
CREATE INDEX idx_trackers_user ON trackers(user_id) WHERE deleted = FALSE;
CREATE INDEX idx_favorites_user ON favorites(assigned_user_id) WHERE deleted = FALSE;
CREATE INDEX idx_saved_searches_user ON saved_searches(assigned_user_id) WHERE deleted = FALSE;
