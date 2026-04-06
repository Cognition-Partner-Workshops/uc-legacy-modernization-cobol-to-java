-- V2: Add campaign log, trackers, and email marketing
-- Maps to AS-IS SuiteCRM modules: CampaignLog, CampaignTrackers, EmailMarketing

CREATE TABLE IF NOT EXISTS campaign_schema.campaign_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaign_schema.campaigns(id),
    target_tracker_key VARCHAR(36),
    target_id UUID,
    target_type VARCHAR(100),
    activity_type VARCHAR(100),
    activity_date TIMESTAMP,
    related_id UUID,
    related_type VARCHAR(100),
    archived BOOLEAN DEFAULT FALSE,
    hits INTEGER DEFAULT 0,
    list_id UUID,
    more_information TEXT,
    marketing_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS campaign_schema.campaign_trackers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaign_schema.campaigns(id),
    tracker_name VARCHAR(255) NOT NULL,
    tracker_url VARCHAR(500),
    tracker_key VARCHAR(36),
    is_optout BOOLEAN DEFAULT FALSE,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS campaign_schema.email_marketing (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID NOT NULL REFERENCES campaign_schema.campaigns(id),
    name VARCHAR(255) NOT NULL,
    from_name VARCHAR(255),
    from_addr VARCHAR(255),
    reply_to_name VARCHAR(255),
    reply_to_addr VARCHAR(255),
    inbound_email_id UUID,
    date_start TIMESTAMP,
    template_id UUID,
    status VARCHAR(50) DEFAULT 'Active',
    all_prospect_lists BOOLEAN DEFAULT FALSE,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_campaign_log_campaign_id ON campaign_schema.campaign_log(campaign_id);
CREATE INDEX idx_campaign_log_activity_type ON campaign_schema.campaign_log(activity_type);
CREATE INDEX idx_campaign_log_target ON campaign_schema.campaign_log(target_id, target_type);
CREATE INDEX idx_campaign_trackers_campaign_id ON campaign_schema.campaign_trackers(campaign_id);
CREATE INDEX idx_campaign_trackers_key ON campaign_schema.campaign_trackers(tracker_key);
CREATE INDEX idx_email_marketing_campaign_id ON campaign_schema.email_marketing(campaign_id);
CREATE INDEX idx_email_marketing_status ON campaign_schema.email_marketing(status);
