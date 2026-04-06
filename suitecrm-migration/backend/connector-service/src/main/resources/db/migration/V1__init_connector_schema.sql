-- Connector Service Schema
-- Migrated from SuiteCRM Connectors, EAPM (External API Modules) modules

CREATE SCHEMA IF NOT EXISTS connector_schema;

-- Connectors (integration definitions)
CREATE TABLE connector_schema.connectors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    connector_type VARCHAR(50),
    source_module VARCHAR(100),
    connector_class VARCHAR(255),
    base_url VARCHAR(500),
    api_version VARCHAR(20),
    auth_type VARCHAR(50),
    status VARCHAR(50) DEFAULT 'active',
    is_enabled BOOLEAN DEFAULT TRUE,
    config_json TEXT,
    field_mapping TEXT,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- External accounts (EAPM - per-user external API connections)
CREATE TABLE connector_schema.external_accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    connector_id UUID REFERENCES connector_schema.connectors(id),
    user_id UUID,
    external_id VARCHAR(255),
    application VARCHAR(100),
    assigned_user_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- OAuth keys (OAuth1/OAuth2 credentials)
CREATE TABLE connector_schema.oauth_keys (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    connector_id UUID REFERENCES connector_schema.connectors(id),
    consumer_key VARCHAR(500),
    consumer_secret VARCHAR(500),
    oauth_type VARCHAR(20),
    token_url VARCHAR(500),
    authorize_url VARCHAR(500),
    scope VARCHAR(500),
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- OAuth tokens (per-user access tokens)
CREATE TABLE connector_schema.oauth_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    oauth_key_id UUID REFERENCES connector_schema.oauth_keys(id),
    user_id UUID NOT NULL,
    access_token TEXT,
    refresh_token TEXT,
    token_type VARCHAR(50),
    expires_at TIMESTAMP,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Connector logs (API call history)
CREATE TABLE connector_schema.connector_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    connector_id UUID NOT NULL REFERENCES connector_schema.connectors(id),
    action VARCHAR(100),
    status VARCHAR(50),
    request_data TEXT,
    response_data TEXT,
    error_message TEXT,
    duration_ms BIGINT,
    user_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Integration mappings (field-level mapping configuration)
CREATE TABLE connector_schema.integration_mappings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    connector_id UUID NOT NULL REFERENCES connector_schema.connectors(id),
    source_module VARCHAR(100),
    source_field VARCHAR(100),
    target_field VARCHAR(100),
    mapping_type VARCHAR(50),
    transformation VARCHAR(500),
    is_required BOOLEAN DEFAULT FALSE,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Indexes
CREATE INDEX idx_connectors_type ON connector_schema.connectors(connector_type) WHERE deleted = FALSE;
CREATE INDEX idx_connectors_status ON connector_schema.connectors(status) WHERE deleted = FALSE;
CREATE INDEX idx_ext_accounts_connector ON connector_schema.external_accounts(connector_id) WHERE deleted = FALSE;
CREATE INDEX idx_ext_accounts_user ON connector_schema.external_accounts(user_id) WHERE deleted = FALSE;
CREATE INDEX idx_oauth_keys_connector ON connector_schema.oauth_keys(connector_id) WHERE deleted = FALSE;
CREATE INDEX idx_oauth_tokens_user ON connector_schema.oauth_tokens(user_id) WHERE deleted = FALSE;
CREATE INDEX idx_connector_logs_connector ON connector_schema.connector_logs(connector_id);
CREATE INDEX idx_connector_logs_date ON connector_schema.connector_logs(date_entered);
CREATE INDEX idx_int_mappings_connector ON connector_schema.integration_mappings(connector_id) WHERE deleted = FALSE;
