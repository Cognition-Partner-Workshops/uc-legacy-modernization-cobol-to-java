-- V3: Expand workflows + add action/condition tables
ALTER TABLE workflow_schema.workflows ADD COLUMN IF NOT EXISTS flow_module VARCHAR(100);
ALTER TABLE workflow_schema.workflows ADD COLUMN IF NOT EXISTS status VARCHAR(50);
ALTER TABLE workflow_schema.workflows ADD COLUMN IF NOT EXISTS run_when VARCHAR(50);
ALTER TABLE workflow_schema.workflows ADD COLUMN IF NOT EXISTS run_on_import BOOLEAN DEFAULT FALSE;
ALTER TABLE workflow_schema.workflows ADD COLUMN IF NOT EXISTS multiple_runs BOOLEAN DEFAULT FALSE;
ALTER TABLE workflow_schema.workflows ADD COLUMN IF NOT EXISTS flow_run_on VARCHAR(50);
ALTER TABLE workflow_schema.workflows ADD COLUMN IF NOT EXISTS repeated_runs BOOLEAN DEFAULT FALSE;
ALTER TABLE workflow_schema.workflows ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

CREATE TABLE IF NOT EXISTS workflow_schema.workflow_actions (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    name VARCHAR(255),
    action VARCHAR(100),
    action_order INTEGER DEFAULT 0,
    parameters TEXT,
    description TEXT,
    date_entered TIMESTAMP NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (workflow_id) REFERENCES workflow_schema.workflows(id)
);

CREATE TABLE IF NOT EXISTS workflow_schema.workflow_conditions (
    id UUID PRIMARY KEY,
    workflow_id UUID NOT NULL,
    name VARCHAR(255),
    field VARCHAR(100),
    module_path VARCHAR(255),
    operator VARCHAR(50),
    value_type VARCHAR(50),
    value TEXT,
    condition_order INTEGER DEFAULT 0,
    date_entered TIMESTAMP NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (workflow_id) REFERENCES workflow_schema.workflows(id)
);
