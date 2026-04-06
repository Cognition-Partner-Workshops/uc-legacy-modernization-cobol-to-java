CREATE SCHEMA IF NOT EXISTS document_schema;
SET search_path TO document_schema;

CREATE TABLE documents (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_name       VARCHAR(255) NOT NULL,
    category_id         VARCHAR(100),
    subcategory_id      VARCHAR(100),
    status_id           VARCHAR(50) DEFAULT 'Active',
    active_date         DATE,
    exp_date            DATE,
    description         TEXT,
    template_type       VARCHAR(50),
    is_template         BOOLEAN DEFAULT FALSE,
    assigned_user_id    UUID,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_documents_name ON documents(document_name);
CREATE INDEX idx_documents_category ON documents(category_id);
CREATE INDEX idx_documents_status ON documents(status_id);
CREATE INDEX idx_documents_deleted ON documents(deleted);

CREATE TABLE document_revisions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    document_id         UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    revision            INTEGER NOT NULL,
    filename            VARCHAR(255),
    file_mime_type      VARCHAR(100),
    file_size           BIGINT,
    file_ext            VARCHAR(20),
    s3_key              VARCHAR(500),
    s3_bucket           VARCHAR(255),
    change_log          TEXT,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_doc_revisions_doc ON document_revisions(document_id);
CREATE INDEX idx_doc_revisions_rev ON document_revisions(document_id, revision);

CREATE TABLE contracts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    account_id          UUID,
    opportunity_id      UUID,
    status              VARCHAR(50) DEFAULT 'Draft',
    start_date          DATE,
    end_date            DATE,
    contract_value      NUMERIC(26,6),
    currency_id         UUID,
    company_signed_date DATE,
    customer_signed_date DATE,
    description         TEXT,
    assigned_user_id    UUID,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_contracts_account ON contracts(account_id);
CREATE INDEX idx_contracts_status ON contracts(status);
