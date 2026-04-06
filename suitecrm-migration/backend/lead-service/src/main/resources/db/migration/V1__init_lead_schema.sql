CREATE SCHEMA IF NOT EXISTS lead_schema;
SET search_path TO lead_schema;

CREATE TABLE leads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    salutation VARCHAR(100),
    first_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    title VARCHAR(255),
    department VARCHAR(255),
    account_name VARCHAR(255),
    account_description TEXT,
    phone_work VARCHAR(50),
    phone_mobile VARCHAR(50),
    phone_home VARCHAR(50),
    phone_other VARCHAR(50),
    phone_fax VARCHAR(50),
    email1 VARCHAR(255),
    email2 VARCHAR(255),
    primary_address_street VARCHAR(255),
    primary_address_city VARCHAR(100),
    primary_address_state VARCHAR(100),
    primary_address_postalcode VARCHAR(20),
    primary_address_country VARCHAR(100),
    alt_address_street VARCHAR(255),
    alt_address_city VARCHAR(100),
    alt_address_state VARCHAR(100),
    alt_address_postalcode VARCHAR(20),
    alt_address_country VARCHAR(100),
    description TEXT,
    status VARCHAR(100) DEFAULT 'New',
    status_description TEXT,
    lead_source VARCHAR(100),
    lead_source_description TEXT,
    refered_by VARCHAR(255),
    website VARCHAR(255),
    do_not_call BOOLEAN DEFAULT FALSE,
    converted BOOLEAN DEFAULT FALSE,
    converted_contact_id UUID,
    converted_account_id UUID,
    converted_opportunity_id UUID,
    campaign_id UUID,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE lead_conversions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lead_id UUID NOT NULL REFERENCES leads(id),
    contact_id UUID,
    account_id UUID,
    opportunity_id UUID,
    converted_by UUID,
    conversion_date TIMESTAMP NOT NULL DEFAULT NOW(),
    notes TEXT,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_leads_status ON leads(status) WHERE deleted = FALSE;
CREATE INDEX idx_leads_lead_source ON leads(lead_source) WHERE deleted = FALSE;
CREATE INDEX idx_leads_assigned_user ON leads(assigned_user_id) WHERE deleted = FALSE;
CREATE INDEX idx_leads_converted ON leads(converted) WHERE deleted = FALSE;
CREATE INDEX idx_leads_campaign ON leads(campaign_id) WHERE deleted = FALSE;
