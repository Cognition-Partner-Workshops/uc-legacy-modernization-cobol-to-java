-- V2: Add prospects, prospect lists, and email addresses
-- Maps to AS-IS SuiteCRM modules: Prospects, ProspectLists, EmailAddresses

CREATE TABLE IF NOT EXISTS contact_schema.prospects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    title VARCHAR(100),
    department VARCHAR(100),
    phone_work VARCHAR(50),
    phone_mobile VARCHAR(50),
    phone_fax VARCHAR(50),
    email VARCHAR(255),
    primary_address_street VARCHAR(255),
    primary_address_city VARCHAR(100),
    primary_address_state VARCHAR(100),
    primary_address_postal_code VARCHAR(20),
    primary_address_country VARCHAR(100),
    alt_address_street VARCHAR(255),
    alt_address_city VARCHAR(100),
    alt_address_state VARCHAR(100),
    alt_address_postal_code VARCHAR(20),
    alt_address_country VARCHAR(100),
    account_name VARCHAR(255),
    description TEXT,
    do_not_call BOOLEAN DEFAULT FALSE,
    converted BOOLEAN DEFAULT FALSE,
    lead_source VARCHAR(100),
    status VARCHAR(50) DEFAULT 'New',
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS contact_schema.prospect_lists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    list_type VARCHAR(50) DEFAULT 'default',
    domain_name VARCHAR(255),
    description TEXT,
    entry_count INTEGER DEFAULT 0,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS contact_schema.email_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email_address VARCHAR(255) NOT NULL,
    email_address_caps VARCHAR(255),
    invalid_email BOOLEAN DEFAULT FALSE,
    opt_out BOOLEAN DEFAULT FALSE,
    confirm_opt_in VARCHAR(50),
    confirm_opt_in_date TIMESTAMP,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS contact_schema.email_addr_bean_rel (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email_address_id UUID NOT NULL REFERENCES contact_schema.email_addresses(id),
    bean_id UUID NOT NULL,
    bean_module VARCHAR(100) NOT NULL,
    primary_address BOOLEAN DEFAULT FALSE,
    reply_to_address BOOLEAN DEFAULT FALSE,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_prospects_email ON contact_schema.prospects(email);
CREATE INDEX idx_prospects_status ON contact_schema.prospects(status);
CREATE INDEX idx_prospect_lists_type ON contact_schema.prospect_lists(list_type);
CREATE INDEX idx_email_addresses_caps ON contact_schema.email_addresses(email_address_caps);
CREATE INDEX idx_email_addr_bean_rel_bean ON contact_schema.email_addr_bean_rel(bean_id, bean_module);
