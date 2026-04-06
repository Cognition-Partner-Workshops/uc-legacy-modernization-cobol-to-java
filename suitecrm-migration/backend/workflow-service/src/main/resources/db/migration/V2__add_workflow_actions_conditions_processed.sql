-- V2: Add workflow actions, conditions, and processed workflows
-- Maps to AS-IS SuiteCRM modules: WorkflowActions, WorkflowConditions, ProcessedWorkflows

CREATE TABLE IF NOT EXISTS workflow_schema.workflow_actions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id UUID NOT NULL REFERENCES workflow_schema.workflows(id),
    name VARCHAR(255) NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    action_module VARCHAR(100),
    field_name VARCHAR(255),
    field_value TEXT,
    parameters TEXT,
    action_order INTEGER,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS workflow_schema.workflow_conditions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id UUID NOT NULL REFERENCES workflow_schema.workflows(id),
    name VARCHAR(255),
    module_name VARCHAR(100),
    field_name VARCHAR(255) NOT NULL,
    operator VARCHAR(50),
    value TEXT,
    value_type VARCHAR(50),
    condition_order INTEGER,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS workflow_schema.processed_workflows (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    workflow_id UUID NOT NULL REFERENCES workflow_schema.workflows(id),
    parent_id UUID NOT NULL,
    parent_type VARCHAR(100) NOT NULL,
    status VARCHAR(50) DEFAULT 'Completed',
    date_processed TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_workflow_actions_workflow_id ON workflow_schema.workflow_actions(workflow_id);
CREATE INDEX idx_workflow_conditions_workflow_id ON workflow_schema.workflow_conditions(workflow_id);
CREATE INDEX idx_processed_workflows_workflow_id ON workflow_schema.processed_workflows(workflow_id);
CREATE INDEX idx_processed_workflows_parent ON workflow_schema.processed_workflows(parent_id, parent_type);
CREATE INDEX idx_processed_workflows_date ON workflow_schema.processed_workflows(date_processed);
