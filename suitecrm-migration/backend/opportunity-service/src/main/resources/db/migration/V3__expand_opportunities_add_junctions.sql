-- V3: Expand opportunities + add junction tables
ALTER TABLE opportunity_schema.opportunities ADD COLUMN IF NOT EXISTS amount_usdollar DECIMAL(26,6);
ALTER TABLE opportunity_schema.opportunities ADD COLUMN IF NOT EXISTS currency_id UUID;
ALTER TABLE opportunity_schema.opportunities ADD COLUMN IF NOT EXISTS probability DECIMAL(5,2);
ALTER TABLE opportunity_schema.opportunities ADD COLUMN IF NOT EXISTS account_id UUID;
ALTER TABLE opportunity_schema.opportunities ADD COLUMN IF NOT EXISTS account_name VARCHAR(255);
ALTER TABLE opportunity_schema.opportunities ADD COLUMN IF NOT EXISTS campaign_id UUID;
ALTER TABLE opportunity_schema.opportunities ADD COLUMN IF NOT EXISTS campaign_name VARCHAR(255);
ALTER TABLE opportunity_schema.opportunities ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

CREATE TABLE IF NOT EXISTS opportunity_schema.opportunities_contacts (
    id UUID PRIMARY KEY,
    opportunity_id UUID NOT NULL,
    contact_id UUID NOT NULL,
    contact_role VARCHAR(50),
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_opp_contact_opp ON opportunity_schema.opportunities_contacts(opportunity_id);
CREATE INDEX IF NOT EXISTS idx_opp_contact_contact ON opportunity_schema.opportunities_contacts(contact_id);
