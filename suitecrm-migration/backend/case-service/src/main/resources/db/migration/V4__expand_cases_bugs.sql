-- V4: Expand cases and bugs with all AS-IS fields
ALTER TABLE case_schema.cases ADD COLUMN IF NOT EXISTS case_number INTEGER UNIQUE;
ALTER TABLE case_schema.cases ADD COLUMN IF NOT EXISTS type VARCHAR(50);
ALTER TABLE case_schema.cases ADD COLUMN IF NOT EXISTS state VARCHAR(50);
ALTER TABLE case_schema.cases ADD COLUMN IF NOT EXISTS internal BOOLEAN DEFAULT FALSE;
ALTER TABLE case_schema.cases ADD COLUMN IF NOT EXISTS suggestion_box TEXT;
ALTER TABLE case_schema.cases ADD COLUMN IF NOT EXISTS update_text TEXT;
ALTER TABLE case_schema.cases ADD COLUMN IF NOT EXISTS contact_created_by_id UUID;
ALTER TABLE case_schema.cases ADD COLUMN IF NOT EXISTS contact_created_by_name VARCHAR(255);
ALTER TABLE case_schema.cases ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;

ALTER TABLE case_schema.bugs ADD COLUMN IF NOT EXISTS bug_number INTEGER UNIQUE;
ALTER TABLE case_schema.bugs ADD COLUMN IF NOT EXISTS type VARCHAR(50);
ALTER TABLE case_schema.bugs ADD COLUMN IF NOT EXISTS work_log TEXT;
ALTER TABLE case_schema.bugs ADD COLUMN IF NOT EXISTS source VARCHAR(100);
ALTER TABLE case_schema.bugs ADD COLUMN IF NOT EXISTS product_category VARCHAR(100);
ALTER TABLE case_schema.bugs ADD COLUMN IF NOT EXISTS found_in_release VARCHAR(255);
ALTER TABLE case_schema.bugs ADD COLUMN IF NOT EXISTS found_in_release_id UUID;
ALTER TABLE case_schema.bugs ADD COLUMN IF NOT EXISTS fixed_in_release VARCHAR(255);
ALTER TABLE case_schema.bugs ADD COLUMN IF NOT EXISTS fixed_in_release_id UUID;
ALTER TABLE case_schema.bugs ADD COLUMN IF NOT EXISTS version BIGINT DEFAULT 0;
