SET search_path TO cases_schema;

CREATE TABLE bugs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    bug_number VARCHAR(50),
    status VARCHAR(100) DEFAULT 'New',
    priority VARCHAR(100) DEFAULT 'Medium',
    type VARCHAR(100),
    category VARCHAR(100),
    found_in_release VARCHAR(255),
    fixed_in_release VARCHAR(255),
    resolution VARCHAR(100),
    source VARCHAR(100),
    product_category VARCHAR(100),
    description TEXT,
    work_log TEXT,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE releases (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(100) DEFAULT 'Active',
    list_order INTEGER,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_bugs_status ON bugs(status) WHERE deleted = FALSE;
CREATE INDEX idx_bugs_priority ON bugs(priority) WHERE deleted = FALSE;
CREATE INDEX idx_bugs_assigned ON bugs(assigned_user_id) WHERE deleted = FALSE;
