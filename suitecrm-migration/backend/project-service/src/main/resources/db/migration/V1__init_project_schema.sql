-- V1: Initialize project schema
-- Maps to AS-IS SuiteCRM modules: Projects, ProjectTasks, ProjectTemplates, ProjectResources

CREATE SCHEMA IF NOT EXISTS project_schema;

CREATE TABLE IF NOT EXISTS project_schema.projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'Draft',
    priority VARCHAR(50) DEFAULT 'Medium',
    estimated_start_date DATE,
    estimated_end_date DATE,
    actual_start_date DATE,
    actual_end_date DATE,
    estimated_cost DECIMAL(26,6),
    actual_cost DECIMAL(26,6),
    description TEXT,
    override_business_hours BOOLEAN DEFAULT FALSE,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS project_schema.project_tasks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES project_schema.projects(id),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'Not Started',
    priority VARCHAR(50) DEFAULT 'Medium',
    percent_complete INTEGER DEFAULT 0,
    task_number INTEGER,
    order_number INTEGER,
    estimated_effort INTEGER,
    actual_effort INTEGER,
    utilization INTEGER,
    date_start DATE,
    date_finish DATE,
    date_due DATE,
    duration INTEGER,
    duration_unit VARCHAR(20) DEFAULT 'Days',
    parent_task_id UUID REFERENCES project_schema.project_tasks(id),
    milestone_flag BOOLEAN DEFAULT FALSE,
    predecessors VARCHAR(500),
    relationship_type VARCHAR(50),
    lag INTEGER,
    description TEXT,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS project_schema.project_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'Active',
    priority VARCHAR(50) DEFAULT 'Medium',
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS project_schema.project_resources (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID NOT NULL REFERENCES project_schema.projects(id),
    user_id UUID,
    contact_id UUID,
    resource_type VARCHAR(50),
    role VARCHAR(100),
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_projects_status ON project_schema.projects(status);
CREATE INDEX idx_projects_priority ON project_schema.projects(priority);
CREATE INDEX idx_projects_assigned_user ON project_schema.projects(assigned_user_id);
CREATE INDEX idx_project_tasks_project_id ON project_schema.project_tasks(project_id);
CREATE INDEX idx_project_tasks_status ON project_schema.project_tasks(status);
CREATE INDEX idx_project_tasks_parent ON project_schema.project_tasks(parent_task_id);
CREATE INDEX idx_project_tasks_milestone ON project_schema.project_tasks(milestone_flag);
CREATE INDEX idx_project_templates_status ON project_schema.project_templates(status);
CREATE INDEX idx_project_resources_project_id ON project_schema.project_resources(project_id);
