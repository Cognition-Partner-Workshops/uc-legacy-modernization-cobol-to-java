CREATE SCHEMA IF NOT EXISTS template_schema;
SET search_path TO template_schema;

CREATE TABLE email_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(255) NOT NULL,
    subject TEXT, body TEXT, body_html TEXT, type VARCHAR(100),
    text_only BOOLEAN DEFAULT FALSE, description TEXT,
    assigned_user_id UUID, created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(), date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE pdf_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(255) NOT NULL,
    type VARCHAR(100), module_name VARCHAR(100),
    pdfheader TEXT, pdffooter TEXT, body TEXT,
    margin_left INTEGER, margin_right INTEGER, margin_top INTEGER, margin_bottom INTEGER,
    margin_header INTEGER, margin_footer INTEGER,
    page_size VARCHAR(50) DEFAULT 'A4', orientation VARCHAR(50) DEFAULT 'Portrait',
    description TEXT, assigned_user_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(), date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_email_templates_type ON email_templates(type) WHERE deleted = FALSE;
CREATE INDEX idx_pdf_templates_module ON pdf_templates(module_name) WHERE deleted = FALSE;
