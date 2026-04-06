-- V3: Add attachments table for Notes & Attachments module
-- Migrated from SuiteCRM Notes (with file attachments) module

CREATE TABLE IF NOT EXISTS activity_schema.attachments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    filename VARCHAR(255),
    file_mime_type VARCHAR(100),
    file_size BIGINT,
    file_url VARCHAR(500),
    file_source VARCHAR(50),
    parent_type VARCHAR(50),
    parent_id UUID,
    note_id UUID REFERENCES activity_schema.notes(id),
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Add file_size to notes if not exists
ALTER TABLE activity_schema.notes ADD COLUMN IF NOT EXISTS file_size BIGINT;

-- Indexes
CREATE INDEX IF NOT EXISTS idx_attachments_parent ON activity_schema.attachments(parent_type, parent_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_attachments_note ON activity_schema.attachments(note_id) WHERE deleted = FALSE;
CREATE INDEX IF NOT EXISTS idx_attachments_created_by ON activity_schema.attachments(created_by) WHERE deleted = FALSE;
