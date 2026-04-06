SET search_path TO connector_schema;

CREATE TABLE oauth2_clients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    client_id VARCHAR(255) NOT NULL UNIQUE,
    client_secret VARCHAR(512),
    redirect_url VARCHAR(512),
    allowed_grant_type VARCHAR(100),
    is_confidential BOOLEAN DEFAULT TRUE,
    assigned_user_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE oauth2_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    client_id UUID NOT NULL,
    user_id UUID,
    access_token VARCHAR(512) NOT NULL,
    refresh_token VARCHAR(512),
    token_type VARCHAR(50) DEFAULT 'Bearer',
    access_token_expires TIMESTAMP,
    refresh_token_expires TIMESTAMP,
    grant_type VARCHAR(100),
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE external_oauth_providers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100),
    connector_name VARCHAR(255),
    client_id VARCHAR(255),
    client_secret VARCHAR(512),
    scope VARCHAR(512),
    url_authorize VARCHAR(512),
    url_access_token VARCHAR(512),
    url_user_info VARCHAR(512),
    extra_provider_params TEXT,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE external_oauth_connections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    provider_id UUID REFERENCES external_oauth_providers(id),
    client_id VARCHAR(255),
    client_secret VARCHAR(512),
    token_url VARCHAR(512),
    authorize_url VARCHAR(512),
    scope VARCHAR(512),
    redirect_uri VARCHAR(512),
    access_token TEXT,
    refresh_token TEXT,
    token_expiry TIMESTAMP,
    assigned_user_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_oauth2_clients_client_id ON oauth2_clients(client_id) WHERE deleted = FALSE;
CREATE INDEX idx_oauth2_tokens_client ON oauth2_tokens(client_id) WHERE deleted = FALSE;
CREATE INDEX idx_oauth2_tokens_user ON oauth2_tokens(user_id) WHERE deleted = FALSE;
CREATE INDEX idx_ext_oauth_conn_user ON external_oauth_connections(assigned_user_id) WHERE deleted = FALSE;
