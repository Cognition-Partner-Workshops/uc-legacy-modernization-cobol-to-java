SET search_path TO target_list_schema;

CREATE TABLE prospect_list_leads (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    prospect_list_id UUID NOT NULL,
    lead_id UUID NOT NULL,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_prospect_list_leads_list ON prospect_list_leads(prospect_list_id) WHERE deleted = FALSE;
CREATE INDEX idx_prospect_list_leads_lead ON prospect_list_leads(lead_id) WHERE deleted = FALSE;
