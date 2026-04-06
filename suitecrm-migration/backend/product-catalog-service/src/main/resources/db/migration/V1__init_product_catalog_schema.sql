-- Product Catalog Service Schema
-- Migrated from SuiteCRM Products, ProductCategories, ProductTypes, Manufacturers, Shippers modules

CREATE SCHEMA IF NOT EXISTS product_catalog_schema;

-- Product categories (hierarchical)
CREATE TABLE product_catalog_schema.product_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_id UUID REFERENCES product_catalog_schema.product_categories(id),
    list_order INTEGER,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Product types
CREATE TABLE product_catalog_schema.product_types (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Manufacturers
CREATE TABLE product_catalog_schema.manufacturers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50),
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Shippers
CREATE TABLE product_catalog_schema.shippers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50),
    default_cost DECIMAL(26,6),
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Products (AOS_Products_Quotes equivalent)
CREATE TABLE product_catalog_schema.products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'Available',
    part_number VARCHAR(100),
    cost_price DECIMAL(26,6),
    list_price DECIMAL(26,6),
    discount_price DECIMAL(26,6),
    currency_id UUID,
    category_id UUID REFERENCES product_catalog_schema.product_categories(id),
    product_type_id UUID REFERENCES product_catalog_schema.product_types(id),
    manufacturer_id UUID REFERENCES product_catalog_schema.manufacturers(id),
    weight DECIMAL(12,4),
    qty_in_stock INTEGER,
    date_available TIMESTAMP,
    date_cost_price TIMESTAMP,
    tax_class VARCHAR(50),
    website VARCHAR(500),
    mft_part_num VARCHAR(100),
    vendor_part_num VARCHAR(100),
    support_name VARCHAR(255),
    support_description TEXT,
    support_contact VARCHAR(255),
    support_term VARCHAR(50),
    pricing_formula VARCHAR(50),
    pricing_factor INTEGER,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Product bundles (for quotes)
CREATE TABLE product_catalog_schema.product_bundles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    bundle_stage VARCHAR(50),
    currency_id UUID,
    base_rate DECIMAL(26,6),
    shipping DECIMAL(26,6),
    tax DECIMAL(26,6),
    subtotal DECIMAL(26,6),
    total DECIMAL(26,6),
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Product bundle items
CREATE TABLE product_catalog_schema.product_bundle_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    bundle_id UUID NOT NULL REFERENCES product_catalog_schema.product_bundles(id),
    product_id UUID NOT NULL REFERENCES product_catalog_schema.products(id),
    quantity INTEGER DEFAULT 1,
    product_list_price DECIMAL(26,6),
    product_discount DECIMAL(26,6),
    product_discount_amount DECIMAL(26,6),
    product_unit_price DECIMAL(26,6),
    product_total_price DECIMAL(26,6),
    vat VARCHAR(50),
    vat_amount DECIMAL(26,6),
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted BOOLEAN DEFAULT FALSE
);

-- Indexes
CREATE INDEX idx_products_status ON product_catalog_schema.products(status) WHERE deleted = FALSE;
CREATE INDEX idx_products_category ON product_catalog_schema.products(category_id) WHERE deleted = FALSE;
CREATE INDEX idx_products_manufacturer ON product_catalog_schema.products(manufacturer_id) WHERE deleted = FALSE;
CREATE INDEX idx_products_part_number ON product_catalog_schema.products(part_number) WHERE deleted = FALSE;
CREATE INDEX idx_product_categories_parent ON product_catalog_schema.product_categories(parent_id) WHERE deleted = FALSE;
CREATE INDEX idx_bundle_items_bundle ON product_catalog_schema.product_bundle_items(bundle_id) WHERE deleted = FALSE;
