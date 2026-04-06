CREATE SCHEMA IF NOT EXISTS maps_schema;
SET search_path TO maps_schema;

CREATE TABLE geo_maps (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(255) NOT NULL,
    center_lat DOUBLE PRECISION, center_lng DOUBLE PRECISION, zoom_level INTEGER,
    map_type VARCHAR(50), unit VARCHAR(20), module_type VARCHAR(100),
    description TEXT, assigned_user_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(), date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE markers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(255) NOT NULL,
    city VARCHAR(100), state VARCHAR(100), country VARCHAR(100),
    latitude DOUBLE PRECISION, longitude DOUBLE PRECISION,
    marker_type VARCHAR(100), related_module VARCHAR(100), related_id UUID,
    description TEXT, assigned_user_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(), date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE areas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), name VARCHAR(255) NOT NULL,
    city VARCHAR(100), state VARCHAR(100), country VARCHAR(100),
    coordinates TEXT, area_type VARCHAR(100),
    description TEXT, assigned_user_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(), date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE address_cache (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), address VARCHAR(512) NOT NULL,
    latitude DOUBLE PRECISION, longitude DOUBLE PRECISION,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(), deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_markers_location ON markers(latitude, longitude) WHERE deleted = FALSE;
CREATE INDEX idx_markers_related ON markers(related_module, related_id) WHERE deleted = FALSE;
CREATE INDEX idx_address_cache_addr ON address_cache(address) WHERE deleted = FALSE;
