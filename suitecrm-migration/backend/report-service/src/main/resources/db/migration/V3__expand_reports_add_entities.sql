-- V3: Expand reports + add field/condition/chart tables
ALTER TABLE report_schema.reports ADD COLUMN IF NOT EXISTS graphs_per_row INTEGER DEFAULT 2;
ALTER TABLE report_schema.reports ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

CREATE TABLE IF NOT EXISTS report_schema.report_fields (
    id UUID PRIMARY KEY,
    report_id UUID NOT NULL,
    name VARCHAR(255),
    label VARCHAR(255),
    field VARCHAR(100),
    module_path VARCHAR(255),
    field_function VARCHAR(50),
    sort_by VARCHAR(20),
    sort_order INTEGER,
    group_by BOOLEAN DEFAULT FALSE,
    group_order INTEGER,
    group_display VARCHAR(50),
    field_order INTEGER DEFAULT 0,
    display BOOLEAN DEFAULT TRUE,
    total VARCHAR(50),
    format VARCHAR(50),
    date_entered TIMESTAMP NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (report_id) REFERENCES report_schema.reports(id)
);

CREATE TABLE IF NOT EXISTS report_schema.report_conditions (
    id UUID PRIMARY KEY,
    report_id UUID NOT NULL,
    name VARCHAR(255),
    field VARCHAR(100),
    module_path VARCHAR(255),
    operator VARCHAR(50),
    value_type VARCHAR(50),
    value TEXT,
    logic_op VARCHAR(10) DEFAULT 'AND',
    parenthesis VARCHAR(10),
    parameter BOOLEAN DEFAULT FALSE,
    condition_order INTEGER DEFAULT 0,
    date_entered TIMESTAMP NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (report_id) REFERENCES report_schema.reports(id)
);

CREATE TABLE IF NOT EXISTS report_schema.report_charts (
    id UUID PRIMARY KEY,
    report_id UUID NOT NULL,
    name VARCHAR(255),
    type VARCHAR(50),
    x_field VARCHAR(100),
    y_field VARCHAR(100),
    date_entered TIMESTAMP NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (report_id) REFERENCES report_schema.reports(id)
);
