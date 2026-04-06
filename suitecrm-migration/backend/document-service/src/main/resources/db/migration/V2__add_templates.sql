-- V2: Add email templates and PDF templates
-- Maps to AS-IS SuiteCRM modules: EmailTemplates, PDFTemplates

CREATE TABLE IF NOT EXISTS document_schema.email_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    subject VARCHAR(255),
    body TEXT,
    body_html TEXT,
    type VARCHAR(50) DEFAULT 'email',
    text_only BOOLEAN DEFAULT FALSE,
    published BOOLEAN DEFAULT FALSE,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS document_schema.pdf_templates (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(100) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    body_html TEXT,
    header TEXT,
    footer TEXT,
    page_size VARCHAR(50) DEFAULT 'A4',
    orientation VARCHAR(50) DEFAULT 'Portrait',
    margin_left INTEGER DEFAULT 15,
    margin_right INTEGER DEFAULT 15,
    margin_top INTEGER DEFAULT 16,
    margin_bottom INTEGER DEFAULT 16,
    margin_header INTEGER DEFAULT 9,
    margin_footer INTEGER DEFAULT 9,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_email_templates_type ON document_schema.email_templates(type);
CREATE INDEX idx_email_templates_published ON document_schema.email_templates(published);
CREATE INDEX idx_pdf_templates_type ON document_schema.pdf_templates(type);
CREATE INDEX idx_pdf_templates_active ON document_schema.pdf_templates(active);
