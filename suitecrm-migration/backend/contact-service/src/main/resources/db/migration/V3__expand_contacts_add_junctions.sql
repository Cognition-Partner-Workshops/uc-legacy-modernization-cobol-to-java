-- V3: Expand contacts with all AS-IS fields + junction tables
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS email2 VARCHAR(255);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS email_opt_out BOOLEAN DEFAULT FALSE;
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS invalid_email BOOLEAN DEFAULT FALSE;
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS alt_address_street VARCHAR(255);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS alt_address_city VARCHAR(100);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS alt_address_state VARCHAR(100);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS alt_address_postalcode VARCHAR(20);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS alt_address_country VARCHAR(100);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS reports_to_id UUID;
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS reports_to_name VARCHAR(255);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS assistant VARCHAR(100);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS assistant_phone VARCHAR(50);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS birthdate DATE;
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS lead_source VARCHAR(100);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS campaign_id UUID;
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS campaign_name VARCHAR(255);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS portal_name VARCHAR(255);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS portal_active BOOLEAN DEFAULT FALSE;
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS portal_password VARCHAR(255);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS portal_app VARCHAR(255);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS portal_user_type VARCHAR(50);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS portal_account_disabled BOOLEAN DEFAULT FALSE;
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS preferred_language VARCHAR(20);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS sync_contact BOOLEAN DEFAULT FALSE;
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS photo VARCHAR(255);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS lawful_basis VARCHAR(100);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS date_reviewed DATE;
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS lawful_basis_source VARCHAR(100);
ALTER TABLE contact_schema.contacts ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Junction tables
CREATE TABLE IF NOT EXISTS contact_schema.contacts_cases (
    id UUID PRIMARY KEY,
    contact_id UUID NOT NULL,
    case_id UUID NOT NULL,
    contact_role VARCHAR(50),
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS contact_schema.contacts_bugs (
    id UUID PRIMARY KEY,
    contact_id UUID NOT NULL,
    bug_id UUID NOT NULL,
    contact_role VARCHAR(50),
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
