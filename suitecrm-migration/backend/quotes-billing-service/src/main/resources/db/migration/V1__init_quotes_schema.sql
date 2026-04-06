CREATE SCHEMA IF NOT EXISTS quotes_schema;
SET search_path TO quotes_schema;

CREATE TABLE quotes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    quote_num VARCHAR(50),
    quote_stage VARCHAR(100) DEFAULT 'Draft',
    purchase_order_num VARCHAR(100),
    payment_terms VARCHAR(100),
    valid_until DATE,
    subtotal_amount NUMERIC(26,6),
    discount_amount NUMERIC(26,6),
    tax_amount NUMERIC(26,6),
    shipping_amount NUMERIC(26,6),
    total_amount NUMERIC(26,6),
    currency_id UUID,
    billing_address_street VARCHAR(255),
    billing_address_city VARCHAR(100),
    billing_address_state VARCHAR(100),
    billing_address_postalcode VARCHAR(20),
    billing_address_country VARCHAR(100),
    shipping_address_street VARCHAR(255),
    shipping_address_city VARCHAR(100),
    shipping_address_state VARCHAR(100),
    shipping_address_postalcode VARCHAR(20),
    shipping_address_country VARCHAR(100),
    account_id UUID,
    contact_id UUID,
    opportunity_id UUID,
    assigned_user_id UUID,
    description TEXT,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    invoice_number VARCHAR(50),
    quote_id UUID REFERENCES quotes(id),
    status VARCHAR(100) DEFAULT 'Draft',
    due_date DATE,
    subtotal_amount NUMERIC(26,6),
    discount_amount NUMERIC(26,6),
    tax_amount NUMERIC(26,6),
    shipping_amount NUMERIC(26,6),
    total_amount NUMERIC(26,6),
    currency_id UUID,
    billing_address_street VARCHAR(255),
    billing_address_city VARCHAR(100),
    billing_address_state VARCHAR(100),
    billing_address_postalcode VARCHAR(20),
    billing_address_country VARCHAR(100),
    account_id UUID,
    contact_id UUID,
    assigned_user_id UUID,
    description TEXT,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE contracts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    reference_code VARCHAR(100),
    status VARCHAR(100) DEFAULT 'Not Started',
    contract_type VARCHAR(100),
    start_date DATE,
    end_date DATE,
    renewal_reminder_date DATE,
    total_contract_value NUMERIC(26,6),
    currency_id UUID,
    account_id UUID,
    contact_id UUID,
    opportunity_id UUID,
    assigned_user_id UUID,
    description TEXT,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE line_item_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    parent_type VARCHAR(100),
    parent_id UUID,
    group_number INTEGER,
    total_amount NUMERIC(26,6),
    discount_amount NUMERIC(26,6),
    tax_amount NUMERIC(26,6),
    subtotal_amount NUMERIC(26,6),
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE line_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    group_id UUID REFERENCES line_item_groups(id),
    product_id UUID,
    item_description TEXT,
    quantity NUMERIC(18,4),
    cost_price NUMERIC(26,6),
    list_price NUMERIC(26,6),
    unit_price NUMERIC(26,6),
    discount_price NUMERIC(26,6),
    discount_amount NUMERIC(26,6),
    tax_amount NUMERIC(26,6),
    total_amount NUMERIC(26,6),
    item_number INTEGER,
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_quotes_stage ON quotes(quote_stage) WHERE deleted = FALSE;
CREATE INDEX idx_quotes_account ON quotes(account_id) WHERE deleted = FALSE;
CREATE INDEX idx_invoices_status ON invoices(status) WHERE deleted = FALSE;
CREATE INDEX idx_invoices_account ON invoices(account_id) WHERE deleted = FALSE;
CREATE INDEX idx_contracts_status ON contracts(status) WHERE deleted = FALSE;
CREATE INDEX idx_contracts_account ON contracts(account_id) WHERE deleted = FALSE;
