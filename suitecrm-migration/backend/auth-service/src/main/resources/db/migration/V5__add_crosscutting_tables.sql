-- V5: Add cross-cutting tables: audit log, dashboards, dashlets, admin settings, import maps, OAuth2 auth codes, security group records
CREATE TABLE IF NOT EXISTS auth_schema.audit_log (
    id UUID PRIMARY KEY,
    parent_id UUID NOT NULL,
    parent_module VARCHAR(100) NOT NULL,
    field_name VARCHAR(100) NOT NULL,
    data_type VARCHAR(50),
    before_value_string TEXT,
    after_value_string TEXT,
    before_value_text TEXT,
    after_value_text TEXT,
    changed_by UUID,
    date_created TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_audit_parent ON auth_schema.audit_log(parent_id, parent_module);

CREATE TABLE IF NOT EXISTS auth_schema.dashboards (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    dashboard_module VARCHAR(100),
    dashboard_type VARCHAR(50) DEFAULT 'dashboard',
    layout TEXT,
    assigned_user_id UUID,
    date_entered TIMESTAMP NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_dashboard_user ON auth_schema.dashboards(assigned_user_id);

CREATE TABLE IF NOT EXISTS auth_schema.dashlets (
    id UUID PRIMARY KEY,
    dashboard_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    dashlet_type VARCHAR(100),
    dashlet_module VARCHAR(100),
    title VARCHAR(255),
    options TEXT,
    position INTEGER,
    column_index INTEGER DEFAULT 0,
    row_index INTEGER DEFAULT 0,
    date_entered TIMESTAMP NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (dashboard_id) REFERENCES auth_schema.dashboards(id)
);

CREATE TABLE IF NOT EXISTS auth_schema.admin_settings (
    id UUID PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    value TEXT,
    platform VARCHAR(50) DEFAULT 'default',
    date_entered TIMESTAMP NOT NULL,
    date_modified TIMESTAMP,
    UNIQUE(category, name)
);

CREATE TABLE IF NOT EXISTS auth_schema.import_maps (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    source VARCHAR(100),
    module VARCHAR(100) NOT NULL,
    content TEXT,
    has_header BOOLEAN DEFAULT TRUE,
    delimiter VARCHAR(5) DEFAULT ',',
    enclosure VARCHAR(5) DEFAULT '"',
    is_published BOOLEAN DEFAULT FALSE,
    assigned_user_id UUID,
    date_entered TIMESTAMP NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS auth_schema.oauth2_auth_codes (
    id UUID PRIMARY KEY,
    code VARCHAR(500) NOT NULL,
    client_id VARCHAR(255) NOT NULL,
    user_id UUID,
    redirect_uri VARCHAR(500),
    scopes VARCHAR(500),
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    date_entered TIMESTAMP NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_oauth2_code ON auth_schema.oauth2_auth_codes(code);

CREATE TABLE IF NOT EXISTS auth_schema.securitygroups_records (
    id UUID PRIMARY KEY,
    securitygroup_id UUID NOT NULL,
    record_id UUID NOT NULL,
    module VARCHAR(100) NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_sgr_record ON auth_schema.securitygroups_records(record_id, module);
CREATE INDEX IF NOT EXISTS idx_sgr_group ON auth_schema.securitygroups_records(securitygroup_id);
