CREATE SCHEMA IF NOT EXISTS case_schema;
SET search_path TO case_schema;

CREATE SEQUENCE case_number_seq START WITH 1000;
CREATE SEQUENCE bug_number_seq START WITH 1000;

CREATE TABLE cases (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_number         BIGINT UNIQUE NOT NULL DEFAULT nextval('case_number_seq'),
    name                VARCHAR(255) NOT NULL,
    account_id          UUID,
    contact_id          UUID,
    status              VARCHAR(50) DEFAULT 'New',
    priority            VARCHAR(50) DEFAULT 'Medium',
    type                VARCHAR(50),
    description         TEXT,
    resolution          TEXT,
    work_log            TEXT,
    assigned_user_id    UUID,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_case_status CHECK (status IN ('New','Assigned','Closed','Pending Input','Rejected','Duplicate')),
    CONSTRAINT chk_case_priority CHECK (priority IN ('High','Medium','Low'))
);

CREATE INDEX idx_cases_number ON cases(case_number);
CREATE INDEX idx_cases_status ON cases(status);
CREATE INDEX idx_cases_priority ON cases(priority);
CREATE INDEX idx_cases_account ON cases(account_id);
CREATE INDEX idx_cases_assigned ON cases(assigned_user_id);
CREATE INDEX idx_cases_deleted ON cases(deleted);

CREATE TABLE bugs (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bug_number          BIGINT UNIQUE NOT NULL DEFAULT nextval('bug_number_seq'),
    name                VARCHAR(255) NOT NULL,
    status              VARCHAR(50) DEFAULT 'New',
    priority            VARCHAR(50) DEFAULT 'Medium',
    type                VARCHAR(50),
    category            VARCHAR(100),
    product_category    VARCHAR(100),
    found_in_release    VARCHAR(50),
    fixed_in_release    VARCHAR(50),
    source              VARCHAR(100),
    resolution          TEXT,
    description         TEXT,
    work_log            TEXT,
    assigned_user_id    UUID,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_bugs_number ON bugs(bug_number);
CREATE INDEX idx_bugs_status ON bugs(status);
CREATE INDEX idx_bugs_priority ON bugs(priority);

CREATE TABLE case_updates (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    case_id         UUID NOT NULL REFERENCES cases(id),
    update_type     VARCHAR(50) NOT NULL,
    description     TEXT,
    internal        BOOLEAN DEFAULT FALSE,
    created_by      UUID,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_case_updates_case ON case_updates(case_id);

CREATE TABLE knowledge_base (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title               VARCHAR(255) NOT NULL,
    body                TEXT,
    status              VARCHAR(50) DEFAULT 'Draft',
    category            VARCHAR(100),
    revision            INTEGER DEFAULT 1,
    approved_by         UUID,
    assigned_user_id    UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_kb_title ON knowledge_base(title);
CREATE INDEX idx_kb_category ON knowledge_base(category);
CREATE INDEX idx_kb_status ON knowledge_base(status);
