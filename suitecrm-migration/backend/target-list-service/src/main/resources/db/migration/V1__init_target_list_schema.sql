-- Target List Service Schema
-- Migrated from SuiteCRM ProspectLists, Prospects modules

CREATE SCHEMA IF NOT EXISTS target_list_schema;

-- Prospect lists (target lists)
CREATE TABLE target_list_schema.prospect_lists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    list_type VARCHAR(50) DEFAULT 'default',
    domain_name VARCHAR(255),
    entry_count INTEGER DEFAULT 0,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Prospects (targets)
CREATE TABLE target_list_schema.prospects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    title VARCHAR(100),
    department VARCHAR(100),
    phone_work VARCHAR(50),
    phone_mobile VARCHAR(50),
    phone_fax VARCHAR(50),
    email_address VARCHAR(255),
    primary_address_street VARCHAR(500),
    primary_address_city VARCHAR(100),
    primary_address_state VARCHAR(100),
    primary_address_postalcode VARCHAR(20),
    primary_address_country VARCHAR(100),
    account_name VARCHAR(255),
    do_not_call BOOLEAN DEFAULT FALSE,
    tracker_key VARCHAR(100),
    lead_id UUID,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Prospect list members (many-to-many)
CREATE TABLE target_list_schema.prospect_list_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prospect_list_id UUID NOT NULL REFERENCES target_list_schema.prospect_lists(id),
    related_id UUID NOT NULL,
    related_type VARCHAR(50) NOT NULL,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Campaign prospect list relationships
CREATE TABLE target_list_schema.prospect_list_campaigns (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prospect_list_id UUID NOT NULL REFERENCES target_list_schema.prospect_lists(id),
    campaign_id UUID NOT NULL,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Indexes
CREATE INDEX idx_prospect_lists_type ON target_list_schema.prospect_lists(list_type) WHERE deleted = FALSE;
CREATE INDEX idx_prospects_email ON target_list_schema.prospects(email_address) WHERE deleted = FALSE;
CREATE INDEX idx_prospects_name ON target_list_schema.prospects(last_name, first_name) WHERE deleted = FALSE;
CREATE INDEX idx_plm_list ON target_list_schema.prospect_list_members(prospect_list_id) WHERE deleted = FALSE;
CREATE INDEX idx_plm_related ON target_list_schema.prospect_list_members(related_id, related_type) WHERE deleted = FALSE;
CREATE INDEX idx_plc_campaign ON target_list_schema.prospect_list_campaigns(campaign_id) WHERE deleted = FALSE;
