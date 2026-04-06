-- V2: Add report conditions, fields, and scheduled reports
-- Maps to AS-IS SuiteCRM modules: ReportConditions, ReportFields, ScheduledReports

CREATE TABLE IF NOT EXISTS report_schema.report_conditions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL REFERENCES report_schema.reports(id),
    field_name VARCHAR(255) NOT NULL,
    module_name VARCHAR(100),
    operator VARCHAR(50),
    value TEXT,
    value_type VARCHAR(50),
    order_num INTEGER,
    group_condition VARCHAR(10) DEFAULT 'AND',
    parent_condition_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS report_schema.report_fields (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL REFERENCES report_schema.reports(id),
    field_name VARCHAR(255) NOT NULL,
    module_name VARCHAR(100),
    label VARCHAR(255),
    field_type VARCHAR(50),
    display BOOLEAN DEFAULT TRUE,
    field_order INTEGER,
    sort_by VARCHAR(10),
    sort_order INTEGER,
    group_by BOOLEAN DEFAULT FALSE,
    function_name VARCHAR(50),
    format VARCHAR(100),
    width INTEGER,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS report_schema.scheduled_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID NOT NULL REFERENCES report_schema.reports(id),
    name VARCHAR(255) NOT NULL,
    schedule VARCHAR(50) NOT NULL,
    time_interval VARCHAR(50),
    date_start TIMESTAMP,
    last_run TIMESTAMP,
    next_run TIMESTAMP,
    active BOOLEAN DEFAULT TRUE,
    users TEXT,
    email_recipients TEXT,
    export_format VARCHAR(50) DEFAULT 'CSV',
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_report_conditions_report_id ON report_schema.report_conditions(report_id);
CREATE INDEX idx_report_fields_report_id ON report_schema.report_fields(report_id);
CREATE INDEX idx_scheduled_reports_report_id ON report_schema.scheduled_reports(report_id);
CREATE INDEX idx_scheduled_reports_next_run ON report_schema.scheduled_reports(next_run);
CREATE INDEX idx_scheduled_reports_active ON report_schema.scheduled_reports(active);
