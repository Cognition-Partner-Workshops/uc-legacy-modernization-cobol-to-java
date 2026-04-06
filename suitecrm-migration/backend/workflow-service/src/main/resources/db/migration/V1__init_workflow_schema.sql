CREATE SCHEMA IF NOT EXISTS workflow_schema;
SET search_path TO workflow_schema;

CREATE TABLE workflows (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    flow_module         VARCHAR(100),
    status              VARCHAR(50) DEFAULT 'Active',
    run_when            VARCHAR(50) DEFAULT 'Always',
    run_on              VARCHAR(50) DEFAULT 'All Records',
    repeat_runs         VARCHAR(50) DEFAULT 'Always',
    description         TEXT,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_workflow_status CHECK (status IN ('Active','Inactive'))
);

CREATE INDEX idx_workflows_status ON workflows(status);
CREATE INDEX idx_workflows_module ON workflows(flow_module);
CREATE INDEX idx_workflows_deleted ON workflows(deleted);

CREATE TABLE workflow_conditions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id         UUID NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
    field               VARCHAR(100) NOT NULL,
    operator            VARCHAR(50) NOT NULL,
    value_type          VARCHAR(50),
    value               TEXT,
    module_path         VARCHAR(255),
    order_num           INTEGER DEFAULT 0,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_wf_conditions_workflow ON workflow_conditions(workflow_id);

CREATE TABLE workflow_actions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id         UUID NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
    action_type         VARCHAR(50) NOT NULL,
    action_module       VARCHAR(100),
    parameters          JSONB,
    order_num           INTEGER DEFAULT 0,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_wf_actions_workflow ON workflow_actions(workflow_id);

CREATE TABLE workflow_audit_log (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id         UUID NOT NULL REFERENCES workflows(id),
    record_id           UUID,
    record_module       VARCHAR(100),
    status              VARCHAR(50) NOT NULL,
    description         TEXT,
    error_message       TEXT,
    executed_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_wf_audit_workflow ON workflow_audit_log(workflow_id);
CREATE INDEX idx_wf_audit_date ON workflow_audit_log(executed_at);

CREATE TABLE schedulers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    job                 VARCHAR(255) NOT NULL,
    date_time_start     TIMESTAMP,
    date_time_end       TIMESTAMP,
    job_interval        VARCHAR(100),
    time_from           TIME,
    time_to             TIME,
    status              VARCHAR(50) DEFAULT 'Active',
    catch_up            BOOLEAN DEFAULT TRUE,
    last_run            TIMESTAMP,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE scheduler_log (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scheduler_id        UUID NOT NULL REFERENCES schedulers(id),
    execute_time        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status              VARCHAR(50) NOT NULL,
    message             TEXT
);
