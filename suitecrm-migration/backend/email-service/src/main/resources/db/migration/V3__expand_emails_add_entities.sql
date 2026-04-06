-- V3: Expand emails + add inbound/outbound/text/address entities + junction tables
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS subject VARCHAR(500);
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS uid VARCHAR(255);
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS msgno INTEGER;
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS imap_keywords VARCHAR(500);
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS is_imported BOOLEAN DEFAULT FALSE;
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS is_only_plain_text BOOLEAN DEFAULT FALSE;
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS has_attachment BOOLEAN DEFAULT FALSE;
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS orphaned BOOLEAN DEFAULT FALSE;
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS opt_in VARCHAR(50);
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS folder VARCHAR(255);
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS folder_type VARCHAR(50);
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS category_id VARCHAR(100);
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS mailbox_id UUID;
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS parent_id UUID;
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS parent_type VARCHAR(100);
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS parent_name VARCHAR(255);
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS last_synced TIMESTAMP;
ALTER TABLE email_schema.emails ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

CREATE TABLE IF NOT EXISTS email_schema.emails_text (
    email_id UUID PRIMARY KEY,
    from_addr VARCHAR(500),
    reply_to_addr VARCHAR(500),
    to_addrs TEXT,
    cc_addrs TEXT,
    bcc_addrs TEXT,
    description TEXT,
    description_html TEXT,
    raw_source TEXT,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS email_schema.inbound_email (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50),
    server_url VARCHAR(255),
    email_user VARCHAR(255),
    email_password VARCHAR(255),
    port INTEGER,
    service VARCHAR(50),
    mailbox VARCHAR(255),
    connection_string VARCHAR(500),
    auth_type VARCHAR(50),
    external_oauth_connection_id UUID,
    protocol VARCHAR(20),
    is_ssl BOOLEAN DEFAULT FALSE,
    is_personal BOOLEAN DEFAULT FALSE,
    is_default BOOLEAN DEFAULT FALSE,
    mailbox_type VARCHAR(50),
    template_id UUID,
    group_id UUID,
    stored_options TEXT,
    distribution_method VARCHAR(50),
    distribution_user_id UUID,
    create_case_template_id UUID,
    auto_reply_template_id UUID,
    auto_reply_max INTEGER DEFAULT 10,
    move_to_trash_folder BOOLEAN DEFAULT FALSE,
    mark_read BOOLEAN DEFAULT FALSE,
    only_since BOOLEAN DEFAULT FALSE,
    filter_domain VARCHAR(255),
    allow_outbound_group_usage BOOLEAN DEFAULT FALSE,
    created_by UUID,
    modified_user_id UUID,
    date_entered TIMESTAMP NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS email_schema.outbound_email (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50),
    mail_sendtype VARCHAR(50) DEFAULT 'SMTP',
    mail_smtptype VARCHAR(50),
    mail_smtpserver VARCHAR(255),
    mail_smtpport INTEGER DEFAULT 587,
    mail_smtpuser VARCHAR(255),
    mail_smtppass VARCHAR(255),
    mail_smtpauth_req BOOLEAN DEFAULT TRUE,
    mail_smtpssl BOOLEAN DEFAULT FALSE,
    mail_smtptls BOOLEAN DEFAULT TRUE,
    auth_type VARCHAR(50),
    external_oauth_connection_id UUID,
    user_id UUID,
    created_by UUID,
    modified_user_id UUID,
    date_entered TIMESTAMP NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS email_schema.email_addresses (
    id UUID PRIMARY KEY,
    email_address VARCHAR(255) NOT NULL,
    email_address_caps VARCHAR(255),
    invalid_email BOOLEAN DEFAULT FALSE,
    opt_out BOOLEAN DEFAULT FALSE,
    confirm_opt_in VARCHAR(50),
    confirm_opt_in_date TIMESTAMP,
    confirm_opt_in_sent_date TIMESTAMP,
    confirm_opt_in_fail_date TIMESTAMP,
    confirm_opt_in_token VARCHAR(255),
    date_created TIMESTAMP NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_email_addr_caps ON email_schema.email_addresses(email_address_caps);

CREATE TABLE IF NOT EXISTS email_schema.email_addr_bean_rel (
    id UUID PRIMARY KEY,
    email_address_id UUID NOT NULL,
    bean_id UUID NOT NULL,
    bean_module VARCHAR(100) NOT NULL,
    primary_address BOOLEAN DEFAULT FALSE,
    reply_to_address BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMP NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_eabr_bean ON email_schema.email_addr_bean_rel(bean_id, bean_module);

CREATE TABLE IF NOT EXISTS email_schema.emails_beans (
    id UUID PRIMARY KEY,
    email_id UUID NOT NULL,
    bean_id UUID NOT NULL,
    bean_module VARCHAR(100) NOT NULL,
    campaign_data TEXT,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_eb_email ON email_schema.emails_beans(email_id);
CREATE INDEX IF NOT EXISTS idx_eb_bean ON email_schema.emails_beans(bean_id, bean_module);
