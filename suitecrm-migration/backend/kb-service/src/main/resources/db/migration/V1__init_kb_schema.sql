-- Knowledge Base Service Schema
-- Migrated from SuiteCRM AOK_KnowledgeBase, AOK_Knowledge_Base_Categories modules

CREATE SCHEMA IF NOT EXISTS kb_schema;

-- KB Categories (hierarchical)
CREATE TABLE kb_schema.kb_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_id UUID REFERENCES kb_schema.kb_categories(id),
    display_order INTEGER DEFAULT 0,
    is_external BOOLEAN DEFAULT FALSE,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- KB Content (articles)
CREATE TABLE kb_schema.kb_contents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    body TEXT,
    summary TEXT,
    status VARCHAR(50) DEFAULT 'draft',
    revision INTEGER DEFAULT 1,
    active_date TIMESTAMP,
    exp_date TIMESTAMP,
    view_count INTEGER DEFAULT 0,
    helpful_count INTEGER DEFAULT 0,
    not_helpful_count INTEGER DEFAULT 0,
    category_id UUID REFERENCES kb_schema.kb_categories(id),
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- KB Documents (attachments to articles)
CREATE TABLE kb_schema.kb_documents (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    document_type VARCHAR(50),
    filename VARCHAR(255),
    file_mime_type VARCHAR(100),
    file_url VARCHAR(500),
    kb_content_id UUID REFERENCES kb_schema.kb_contents(id),
    category_id UUID REFERENCES kb_schema.kb_categories(id),
    status VARCHAR(50) DEFAULT 'active',
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Tags
CREATE TABLE kb_schema.kb_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    tag_type VARCHAR(50),
    usage_count INTEGER DEFAULT 0,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Content-Tag relationship
CREATE TABLE kb_schema.kb_content_tags (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    kb_content_id UUID NOT NULL REFERENCES kb_schema.kb_contents(id),
    tag_id UUID NOT NULL REFERENCES kb_schema.kb_tags(id),
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE(kb_content_id, tag_id)
);

-- Article revision history
CREATE TABLE kb_schema.kb_revisions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    kb_content_id UUID NOT NULL REFERENCES kb_schema.kb_contents(id),
    revision_number INTEGER NOT NULL,
    name VARCHAR(255),
    body TEXT,
    summary TEXT,
    changed_by UUID,
    change_description TEXT,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Article feedback/comments
CREATE TABLE kb_schema.kb_feedback (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    kb_content_id UUID NOT NULL REFERENCES kb_schema.kb_contents(id),
    user_id UUID,
    feedback_type VARCHAR(50),
    comment TEXT,
    rating INTEGER,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes
CREATE INDEX idx_kb_contents_status ON kb_schema.kb_contents(status) WHERE deleted = FALSE;
CREATE INDEX idx_kb_contents_category ON kb_schema.kb_contents(category_id) WHERE deleted = FALSE;
CREATE INDEX idx_kb_contents_assigned ON kb_schema.kb_contents(assigned_user_id) WHERE deleted = FALSE;
CREATE INDEX idx_kb_contents_views ON kb_schema.kb_contents(view_count DESC) WHERE deleted = FALSE;
CREATE INDEX idx_kb_categories_parent ON kb_schema.kb_categories(parent_id) WHERE deleted = FALSE;
CREATE INDEX idx_kb_documents_content ON kb_schema.kb_documents(kb_content_id) WHERE deleted = FALSE;
CREATE INDEX idx_kb_tags_name ON kb_schema.kb_tags(name) WHERE deleted = FALSE;
CREATE INDEX idx_kb_content_tags_content ON kb_schema.kb_content_tags(kb_content_id);
CREATE INDEX idx_kb_content_tags_tag ON kb_schema.kb_content_tags(tag_id);
CREATE INDEX idx_kb_revisions_content ON kb_schema.kb_revisions(kb_content_id);
CREATE INDEX idx_kb_feedback_content ON kb_schema.kb_feedback(kb_content_id);

-- Full-text search index for articles
CREATE INDEX idx_kb_contents_fulltext ON kb_schema.kb_contents
    USING gin(to_tsvector('english', COALESCE(name, '') || ' ' || COALESCE(body, '') || ' ' || COALESCE(summary, '')));
