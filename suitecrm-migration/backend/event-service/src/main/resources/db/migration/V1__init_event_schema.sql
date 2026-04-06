-- Event Service Schema
-- Migrated from SuiteCRM FP_Events, FP_Event_Locations_FP_Events modules

CREATE SCHEMA IF NOT EXISTS event_schema;

-- Event locations
CREATE TABLE event_schema.fp_event_locations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    address VARCHAR(500),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),
    capacity INTEGER,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Events
CREATE TABLE event_schema.fp_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    date_start TIMESTAMP,
    date_end TIMESTAMP,
    duration_hours INTEGER,
    duration_minutes INTEGER,
    status VARCHAR(50) DEFAULT 'planned',
    budget DECIMAL(26,6),
    currency_id UUID,
    invite_templates VARCHAR(255),
    accept_redirect VARCHAR(500),
    decline_redirect VARCHAR(500),
    location_id UUID REFERENCES event_schema.fp_event_locations(id),
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Event registrations
CREATE TABLE event_schema.event_registrations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES event_schema.fp_events(id),
    contact_id UUID,
    lead_id UUID,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(50),
    company VARCHAR(255),
    status VARCHAR(50) DEFAULT 'registered',
    accept_status VARCHAR(50) DEFAULT 'none',
    registration_date TIMESTAMP DEFAULT NOW(),
    notes TEXT,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Event invitees
CREATE TABLE event_schema.event_invitees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID NOT NULL REFERENCES event_schema.fp_events(id),
    invitee_type VARCHAR(50),
    invitee_id UUID,
    email VARCHAR(255),
    accept_status VARCHAR(50) DEFAULT 'none',
    invite_sent BOOLEAN DEFAULT FALSE,
    invite_sent_date TIMESTAMP,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Indexes
CREATE INDEX idx_fp_events_status ON event_schema.fp_events(status) WHERE deleted = FALSE;
CREATE INDEX idx_fp_events_date ON event_schema.fp_events(date_start) WHERE deleted = FALSE;
CREATE INDEX idx_fp_events_location ON event_schema.fp_events(location_id) WHERE deleted = FALSE;
CREATE INDEX idx_event_reg_event ON event_schema.event_registrations(event_id) WHERE deleted = FALSE;
CREATE INDEX idx_event_reg_contact ON event_schema.event_registrations(contact_id) WHERE deleted = FALSE;
CREATE INDEX idx_event_invitees_event ON event_schema.event_invitees(event_id) WHERE deleted = FALSE;
