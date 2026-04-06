CREATE SCHEMA IF NOT EXISTS account_schema;
SET search_path TO account_schema;

CREATE TABLE accounts (
    id                              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                            VARCHAR(255) NOT NULL,
    account_type                    VARCHAR(50),
    industry                        VARCHAR(100),
    annual_revenue                  NUMERIC(26,6),
    employees                       VARCHAR(10),
    rating                          VARCHAR(50),
    phone_office                    VARCHAR(50),
    phone_alternate                 VARCHAR(50),
    phone_fax                       VARCHAR(50),
    website                         VARCHAR(255),
    email                           VARCHAR(255),
    ownership                       VARCHAR(100),
    ticker_symbol                   VARCHAR(20),
    sic_code                        VARCHAR(20),
    parent_id                       UUID,
    billing_address_street          VARCHAR(255),
    billing_address_city            VARCHAR(100),
    billing_address_state           VARCHAR(100),
    billing_address_postalcode      VARCHAR(20),
    billing_address_country         VARCHAR(100),
    shipping_address_street         VARCHAR(255),
    shipping_address_city           VARCHAR(100),
    shipping_address_state          VARCHAR(100),
    shipping_address_postalcode     VARCHAR(20),
    shipping_address_country        VARCHAR(100),
    description                     TEXT,
    assigned_user_id                UUID,
    created_by                      UUID,
    date_entered                    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified                   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted                         BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_accounts_name ON accounts(name);
CREATE INDEX idx_accounts_type ON accounts(account_type);
CREATE INDEX idx_accounts_industry ON accounts(industry);
CREATE INDEX idx_accounts_assigned ON accounts(assigned_user_id);
CREATE INDEX idx_accounts_parent ON accounts(parent_id);
CREATE INDEX idx_accounts_deleted ON accounts(deleted);

CREATE TABLE employees (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name          VARCHAR(100),
    last_name           VARCHAR(100) NOT NULL,
    title               VARCHAR(100),
    department          VARCHAR(100),
    phone_work          VARCHAR(50),
    phone_mobile        VARCHAR(50),
    email               VARCHAR(255),
    employee_status     VARCHAR(50) DEFAULT 'Active',
    reports_to_id       UUID,
    account_id          UUID REFERENCES accounts(id),
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE accounts_audit (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id      UUID NOT NULL REFERENCES accounts(id),
    field_name      VARCHAR(100) NOT NULL,
    before_value    TEXT,
    after_value     TEXT,
    changed_by      UUID,
    changed_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
