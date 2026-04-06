-- V3: Add polymorphic prospect_lists_prospects junction
CREATE TABLE IF NOT EXISTS targetlist_schema.prospect_lists_prospects (
    id UUID PRIMARY KEY,
    prospect_list_id UUID NOT NULL,
    related_id UUID NOT NULL,
    related_type VARCHAR(100) NOT NULL,
    date_modified TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);
CREATE INDEX IF NOT EXISTS idx_plp_list ON targetlist_schema.prospect_lists_prospects(prospect_list_id);
CREATE INDEX IF NOT EXISTS idx_plp_related ON targetlist_schema.prospect_lists_prospects(related_id, related_type);
