-- V3: Expand accounts table with missing fields + add junction tables
ALTER TABLE account_schema.accounts ADD COLUMN IF NOT EXISTS annual_revenue_usdollar DECIMAL(26,6);
ALTER TABLE account_schema.accounts ADD COLUMN IF NOT EXISTS currency_id UUID;
ALTER TABLE account_schema.accounts ADD COLUMN IF NOT EXISTS email2 VARCHAR(255);
ALTER TABLE account_schema.accounts ADD COLUMN IF NOT EXISTS email_opt_out BOOLEAN DEFAULT FALSE;
ALTER TABLE account_schema.accounts ADD COLUMN IF NOT EXISTS invalid_email BOOLEAN DEFAULT FALSE;
ALTER TABLE account_schema.accounts ADD COLUMN IF NOT EXISTS parent_name VARCHAR(255);
ALTER TABLE account_schema.accounts ADD COLUMN IF NOT EXISTS campaign_id UUID;
ALTER TABLE account_schema.accounts ADD COLUMN IF NOT EXISTS campaign_name VARCHAR(255);
ALTER TABLE account_schema.accounts ADD COLUMN IF NOT EXISTS modified_user_id UUID;
ALTER TABLE account_schema.accounts ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

-- Junction tables
CREATE TABLE IF NOT EXISTS account_schema.accounts_contacts (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    contact_role VARCHAR(50),
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_acct_contact_acct ON account_schema.accounts_contacts(account_id);
CREATE INDEX IF NOT EXISTS idx_acct_contact_contact ON account_schema.accounts_contacts(contact_id);

CREATE TABLE IF NOT EXISTS account_schema.accounts_opportunities (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    opportunity_id UUID NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_acct_opp_acct ON account_schema.accounts_opportunities(account_id);

CREATE TABLE IF NOT EXISTS account_schema.accounts_cases (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    case_id UUID NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS account_schema.accounts_bugs (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    bug_id UUID NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
