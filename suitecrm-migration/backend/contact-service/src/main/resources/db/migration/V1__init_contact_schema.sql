-- =====================================================
-- Contact Service - Aurora PostgreSQL Schema
-- Migrated from SuiteCRM contacts/leads tables
-- =====================================================

CREATE SCHEMA IF NOT EXISTS contact_schema;
SET search_path TO contact_schema;

-- Contacts table
CREATE TABLE contacts (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    salutation                  VARCHAR(25),
    first_name                  VARCHAR(100),
    last_name                   VARCHAR(100) NOT NULL,
    title                       VARCHAR(100),
    department                  VARCHAR(100),
    account_id                  UUID,
    email_primary               VARCHAR(255),
    email_secondary             VARCHAR(255),
    phone_work                  VARCHAR(50),
    phone_mobile                VARCHAR(50),
    phone_home                  VARCHAR(50),
    phone_fax                   VARCHAR(50),
    primary_address_street      VARCHAR(255),
    primary_address_city        VARCHAR(100),
    primary_address_state       VARCHAR(100),
    primary_address_postalcode  VARCHAR(20),
    primary_address_country     VARCHAR(100),
    alt_address_street          VARCHAR(255),
    alt_address_city            VARCHAR(100),
    alt_address_state           VARCHAR(100),
    alt_address_postalcode      VARCHAR(20),
    alt_address_country         VARCHAR(100),
    description                 TEXT,
    lead_source                 VARCHAR(100),
    do_not_call                 BOOLEAN DEFAULT FALSE,
    portal_user_type            VARCHAR(50),
    assigned_user_id            UUID,
    created_by                  UUID,
    date_entered                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted                     BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_contacts_name ON contacts(last_name, first_name);
CREATE INDEX idx_contacts_email ON contacts(email_primary);
CREATE INDEX idx_contacts_account ON contacts(account_id);
CREATE INDEX idx_contacts_assigned ON contacts(assigned_user_id);
CREATE INDEX idx_contacts_deleted ON contacts(deleted);

-- Leads table
CREATE TABLE leads (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    salutation                  VARCHAR(25),
    first_name                  VARCHAR(100),
    last_name                   VARCHAR(100) NOT NULL,
    title                       VARCHAR(100),
    company                     VARCHAR(255),
    department                  VARCHAR(100),
    email_primary               VARCHAR(255),
    phone_work                  VARCHAR(50),
    phone_mobile                VARCHAR(50),
    website                     VARCHAR(255),
    status                      VARCHAR(50) DEFAULT 'New',
    lead_source                 VARCHAR(100),
    lead_source_description     TEXT,
    rating                      VARCHAR(50),
    industry                    VARCHAR(100),
    annual_revenue              VARCHAR(50),
    employees                   VARCHAR(10),
    primary_address_street      VARCHAR(255),
    primary_address_city        VARCHAR(100),
    primary_address_state       VARCHAR(100),
    primary_address_postalcode  VARCHAR(20),
    primary_address_country     VARCHAR(100),
    description                 TEXT,
    converted                   BOOLEAN DEFAULT FALSE,
    converted_contact_id        UUID,
    converted_account_id        UUID,
    converted_opportunity_id    UUID,
    assigned_user_id            UUID,
    created_by                  UUID,
    date_entered                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted                     BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_lead_status CHECK (status IN ('New','Assigned','In Process','Converted','Recycled','Dead'))
);

CREATE INDEX idx_leads_name ON leads(last_name, first_name);
CREATE INDEX idx_leads_company ON leads(company);
CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_assigned ON leads(assigned_user_id);
CREATE INDEX idx_leads_converted ON leads(converted);
CREATE INDEX idx_leads_deleted ON leads(deleted);

-- Prospects table
CREATE TABLE prospects (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name                  VARCHAR(100),
    last_name                   VARCHAR(100) NOT NULL,
    title                       VARCHAR(100),
    department                  VARCHAR(100),
    email_primary               VARCHAR(255),
    phone_work                  VARCHAR(50),
    phone_mobile                VARCHAR(50),
    primary_address_street      VARCHAR(255),
    primary_address_city        VARCHAR(100),
    primary_address_state       VARCHAR(100),
    primary_address_postalcode  VARCHAR(20),
    primary_address_country     VARCHAR(100),
    description                 TEXT,
    assigned_user_id            UUID,
    date_entered                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified               TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted                     BOOLEAN NOT NULL DEFAULT FALSE
);

-- Prospect Lists table
CREATE TABLE prospect_lists (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    list_type           VARCHAR(50) DEFAULT 'default',
    description         TEXT,
    domain_name         VARCHAR(255),
    assigned_user_id    UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

-- Prospect List Members (M:N)
CREATE TABLE prospect_list_members (
    prospect_list_id    UUID NOT NULL REFERENCES prospect_lists(id) ON DELETE CASCADE,
    related_id          UUID NOT NULL,
    related_type        VARCHAR(50) NOT NULL,
    date_added          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (prospect_list_id, related_id, related_type)
);

-- Contacts Audit
CREATE TABLE contacts_audit (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    contact_id      UUID NOT NULL REFERENCES contacts(id),
    field_name      VARCHAR(100) NOT NULL,
    before_value    TEXT,
    after_value     TEXT,
    changed_by      UUID,
    changed_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contacts_audit_contact ON contacts_audit(contact_id);
