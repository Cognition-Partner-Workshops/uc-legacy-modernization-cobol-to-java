-- V2: Expand project_tasks + add task_templates
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS task_number INTEGER;
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS percent_complete INTEGER DEFAULT 0;
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS milestone_flag BOOLEAN DEFAULT FALSE;
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS date_start DATE;
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS date_finish DATE;
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS date_due DATE;
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS duration INTEGER;
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS duration_unit VARCHAR(20);
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS actual_duration INTEGER;
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS estimated_effort INTEGER;
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS actual_effort INTEGER;
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS utilization INTEGER DEFAULT 100;
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS parent_task_id UUID;
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS predecessors TEXT;
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS order_number INTEGER;
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS relationship_type VARCHAR(50);
ALTER TABLE project_schema.project_tasks ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

CREATE TABLE IF NOT EXISTS project_schema.task_templates (
    id UUID PRIMARY KEY,
    project_template_id UUID,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    task_number INTEGER,
    duration INTEGER,
    duration_unit VARCHAR(20),
    estimated_effort INTEGER,
    utilization INTEGER DEFAULT 100,
    milestone_flag BOOLEAN DEFAULT FALSE,
    predecessors TEXT,
    relationship_type VARCHAR(50),
    order_number INTEGER,
    assigned_user_id UUID,
    date_entered TIMESTAMP NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
