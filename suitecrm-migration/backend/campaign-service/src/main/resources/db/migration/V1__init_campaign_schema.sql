CREATE SCHEMA IF NOT EXISTS campaign_schema;
SET search_path TO campaign_schema;

CREATE TABLE campaigns (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    campaign_type       VARCHAR(50),
    status              VARCHAR(50) DEFAULT 'Planning',
    start_date          DATE,
    end_date            DATE,
    budget              NUMERIC(26,6),
    actual_cost         NUMERIC(26,6),
    expected_revenue    NUMERIC(26,6),
    expected_cost       NUMERIC(26,6),
    impressions         BIGINT,
    objective           TEXT,
    content             TEXT,
    description         TEXT,
    assigned_user_id    UUID,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_campaign_status CHECK (status IN ('Planning','Active','Inactive','Complete'))
);

CREATE INDEX idx_campaigns_name ON campaigns(name);
CREATE INDEX idx_campaigns_status ON campaigns(status);
CREATE INDEX idx_campaigns_type ON campaigns(campaign_type);
CREATE INDEX idx_campaigns_dates ON campaigns(start_date, end_date);
CREATE INDEX idx_campaigns_deleted ON campaigns(deleted);

CREATE TABLE email_marketing (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id         UUID NOT NULL REFERENCES campaigns(id),
    name                VARCHAR(255) NOT NULL,
    from_name           VARCHAR(255),
    from_addr           VARCHAR(255),
    reply_to_name       VARCHAR(255),
    reply_to_addr       VARCHAR(255),
    inbound_email_id    UUID,
    template_id         UUID,
    status              VARCHAR(50) DEFAULT 'active',
    all_prospect_lists  BOOLEAN DEFAULT FALSE,
    date_start          TIMESTAMP,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE email_templates (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    subject             VARCHAR(500),
    body_html           TEXT,
    body_text           TEXT,
    type                VARCHAR(50) DEFAULT 'email',
    published           BOOLEAN DEFAULT FALSE,
    description         TEXT,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE campaign_log (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    campaign_id         UUID NOT NULL REFERENCES campaigns(id),
    target_id           UUID,
    target_type         VARCHAR(50),
    activity_type       VARCHAR(50) NOT NULL,
    activity_date       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    related_id          UUID,
    related_type        VARCHAR(50),
    hits                INTEGER DEFAULT 0,
    marketing_id        UUID,
    more_information    TEXT,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_campaign_log_campaign ON campaign_log(campaign_id);
CREATE INDEX idx_campaign_log_type ON campaign_log(activity_type);
CREATE INDEX idx_campaign_log_target ON campaign_log(target_id, target_type);
