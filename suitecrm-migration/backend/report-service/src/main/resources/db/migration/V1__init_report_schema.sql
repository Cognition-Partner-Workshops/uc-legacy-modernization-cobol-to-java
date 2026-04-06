CREATE SCHEMA IF NOT EXISTS report_schema;
SET search_path TO report_schema;

CREATE TABLE reports (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    report_type         VARCHAR(50),
    module_name         VARCHAR(100),
    content             TEXT,
    chart_type          VARCHAR(50),
    schedule_type       VARCHAR(50),
    favorite            BOOLEAN DEFAULT FALSE,
    description         TEXT,
    assigned_user_id    UUID,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_reports_name ON reports(name);
CREATE INDEX idx_reports_type ON reports(report_type);
CREATE INDEX idx_reports_module ON reports(module_name);
CREATE INDEX idx_reports_deleted ON reports(deleted);

CREATE TABLE dashboards (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    layout              JSONB,
    is_default          BOOLEAN DEFAULT FALSE,
    assigned_user_id    UUID,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE dashboard_widgets (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dashboard_id        UUID NOT NULL REFERENCES dashboards(id) ON DELETE CASCADE,
    widget_type         VARCHAR(50) NOT NULL,
    title               VARCHAR(255),
    report_id           UUID REFERENCES reports(id),
    config              JSONB,
    position_x          INTEGER DEFAULT 0,
    position_y          INTEGER DEFAULT 0,
    width               INTEGER DEFAULT 4,
    height              INTEGER DEFAULT 3,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_widgets_dashboard ON dashboard_widgets(dashboard_id);

CREATE TABLE saved_searches (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    search_module       VARCHAR(100) NOT NULL,
    contents            JSONB,
    assigned_user_id    UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);
