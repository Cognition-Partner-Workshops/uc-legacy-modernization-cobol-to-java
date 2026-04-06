-- Email Service Schema
-- Migrated from SuiteCRM Emails, InboundEmail, OutboundEmail modules

CREATE SCHEMA IF NOT EXISTS email_schema;

-- Core emails table
CREATE TABLE email_schema.emails (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    from_addr VARCHAR(255),
    from_name VARCHAR(255),
    to_addrs TEXT,
    cc_addrs TEXT,
    bcc_addrs TEXT,
    reply_to_addr VARCHAR(255),
    description TEXT,
    description_html TEXT,
    type VARCHAR(50) DEFAULT 'out',
    status VARCHAR(50) DEFAULT 'draft',
    intent VARCHAR(50),
    message_id VARCHAR(255),
    parent_type VARCHAR(100),
    parent_id UUID,
    date_sent TIMESTAMP,
    flagged BOOLEAN DEFAULT FALSE,
    reply_to_status BOOLEAN DEFAULT FALSE,
    mailbox_id UUID,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Email attachments
CREATE TABLE email_schema.email_attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email_id UUID NOT NULL REFERENCES email_schema.emails(id),
    filename VARCHAR(255),
    file_mime_type VARCHAR(100),
    file_size BIGINT,
    file_source VARCHAR(50),
    file_ext VARCHAR(50),
    storage_location VARCHAR(500),
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Email threads for conversation grouping
CREATE TABLE email_schema.email_threads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    subject VARCHAR(255) NOT NULL,
    last_message_date TIMESTAMP,
    message_count INTEGER DEFAULT 0,
    participant_ids TEXT,
    related_module VARCHAR(100),
    related_module_id UUID,
    assigned_user_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Inbound email accounts (IMAP/POP3 configuration)
CREATE TABLE email_schema.inbound_email (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'Active',
    server_url VARCHAR(500),
    email_user VARCHAR(255),
    email_password VARCHAR(500),
    port VARCHAR(10),
    protocol VARCHAR(50),
    mailbox_type VARCHAR(50),
    service VARCHAR(50),
    is_personal BOOLEAN DEFAULT FALSE,
    is_ssl BOOLEAN DEFAULT FALSE,
    delete_seen BOOLEAN DEFAULT FALSE,
    mailbox VARCHAR(255),
    group_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Outbound email accounts (SMTP configuration)
CREATE TABLE email_schema.outbound_email (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) DEFAULT 'user',
    mail_sendtype VARCHAR(50) DEFAULT 'SMTP',
    mail_smtptype VARCHAR(50),
    mail_smtpserver VARCHAR(500),
    mail_smtpport INTEGER DEFAULT 587,
    mail_smtpuser VARCHAR(255),
    mail_smtppass VARCHAR(500),
    mail_smtpauth_req BOOLEAN DEFAULT TRUE,
    mail_smtpssl BOOLEAN DEFAULT TRUE,
    user_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Email address book / cache
CREATE TABLE email_schema.email_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email_address VARCHAR(255) NOT NULL,
    display_name VARCHAR(255),
    contact_type VARCHAR(100),
    contact_id UUID,
    last_used TIMESTAMP,
    use_count INTEGER DEFAULT 0,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_emails_assigned_user ON email_schema.emails(assigned_user_id) WHERE deleted = FALSE;
CREATE INDEX idx_emails_status ON email_schema.emails(status) WHERE deleted = FALSE;
CREATE INDEX idx_emails_type ON email_schema.emails(type) WHERE deleted = FALSE;
CREATE INDEX idx_emails_mailbox ON email_schema.emails(mailbox_id) WHERE deleted = FALSE;
CREATE INDEX idx_emails_parent ON email_schema.emails(parent_type, parent_id) WHERE deleted = FALSE;
CREATE INDEX idx_emails_date_sent ON email_schema.emails(date_sent DESC) WHERE deleted = FALSE;
CREATE INDEX idx_email_attachments_email ON email_schema.email_attachments(email_id) WHERE deleted = FALSE;
CREATE INDEX idx_email_threads_assigned ON email_schema.email_threads(assigned_user_id) WHERE deleted = FALSE;
CREATE INDEX idx_inbound_email_status ON email_schema.inbound_email(status) WHERE deleted = FALSE;
CREATE INDEX idx_outbound_email_user ON email_schema.outbound_email(user_id) WHERE deleted = FALSE;
CREATE INDEX idx_email_cache_address ON email_schema.email_cache(email_address);
