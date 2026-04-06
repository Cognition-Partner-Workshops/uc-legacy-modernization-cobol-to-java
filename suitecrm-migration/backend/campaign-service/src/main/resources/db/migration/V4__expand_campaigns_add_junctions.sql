-- V4: Expand campaigns + add junction tables
ALTER TABLE campaign_schema.campaigns ADD COLUMN IF NOT EXISTS campaign_type VARCHAR(50);
ALTER TABLE campaign_schema.campaigns ADD COLUMN IF NOT EXISTS actual_cost DECIMAL(26,6);
ALTER TABLE campaign_schema.campaigns ADD COLUMN IF NOT EXISTS expected_cost DECIMAL(26,6);
ALTER TABLE campaign_schema.campaigns ADD COLUMN IF NOT EXISTS expected_revenue DECIMAL(26,6);
ALTER TABLE campaign_schema.campaigns ADD COLUMN IF NOT EXISTS impressions INTEGER;
ALTER TABLE campaign_schema.campaigns ADD COLUMN IF NOT EXISTS currency_id UUID;
ALTER TABLE campaign_schema.campaigns ADD COLUMN IF NOT EXISTS objective TEXT;
ALTER TABLE campaign_schema.campaigns ADD COLUMN IF NOT EXISTS content TEXT;
ALTER TABLE campaign_schema.campaigns ADD COLUMN IF NOT EXISTS tracker_text VARCHAR(255);
ALTER TABLE campaign_schema.campaigns ADD COLUMN IF NOT EXISTS tracker_key VARCHAR(255);
ALTER TABLE campaign_schema.campaigns ADD COLUMN IF NOT EXISTS tracker_count INTEGER DEFAULT 0;
ALTER TABLE campaign_schema.campaigns ADD COLUMN IF NOT EXISTS refer_url VARCHAR(500);
ALTER TABLE campaign_schema.campaigns ADD COLUMN IF NOT EXISTS frequency VARCHAR(50);
ALTER TABLE campaign_schema.campaigns ADD COLUMN IF NOT EXISTS survey_id UUID;
ALTER TABLE campaign_schema.campaigns ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

CREATE TABLE IF NOT EXISTS campaign_schema.prospect_list_campaigns (
    id UUID PRIMARY KEY,
    prospect_list_id UUID NOT NULL,
    campaign_id UUID NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_plc_campaign ON campaign_schema.prospect_list_campaigns(campaign_id);
