SET search_path TO campaign_schema;

CREATE TABLE email_marketing (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    campaign_id UUID,
    template_id UUID,
    from_addr VARCHAR(255),
    from_name VARCHAR(255),
    reply_to_name VARCHAR(255),
    reply_to_addr VARCHAR(255),
    inbound_email_id UUID,
    date_start TIMESTAMP,
    status VARCHAR(100) DEFAULT 'Active',
    all_prospect_lists BOOLEAN DEFAULT FALSE,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE emailman (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id UUID,
    marketing_id UUID,
    list_id UUID,
    related_id UUID,
    related_type VARCHAR(100),
    send_date_time TIMESTAMP,
    in_queue BOOLEAN DEFAULT TRUE,
    in_queue_date TIMESTAMP DEFAULT NOW(),
    send_attempts INTEGER DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_email_marketing_campaign ON email_marketing(campaign_id) WHERE deleted = FALSE;
CREATE INDEX idx_emailman_campaign ON emailman(campaign_id) WHERE deleted = FALSE;
CREATE INDEX idx_emailman_queue ON emailman(in_queue) WHERE deleted = FALSE;
