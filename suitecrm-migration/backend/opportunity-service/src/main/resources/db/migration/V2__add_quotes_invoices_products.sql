-- V2: Add products, product categories, quotes, invoices, line items
-- Maps to AS-IS SuiteCRM modules: Products, ProductCategories, Quotes, Invoices, LineItems

CREATE TABLE IF NOT EXISTS opportunity_schema.product_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    parent_category_id UUID REFERENCES opportunity_schema.product_categories(id),
    list_order INTEGER,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS opportunity_schema.products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) DEFAULT 'Available',
    date_available DATE,
    date_cost_price DATE,
    description TEXT,
    cost DECIMAL(26,6),
    list_price DECIMAL(26,6),
    discount_price DECIMAL(26,6),
    discount_amount DECIMAL(26,6),
    weight DECIMAL(12,2),
    quantity INTEGER DEFAULT 0,
    mft_part_num VARCHAR(100),
    vendor_part_num VARCHAR(100),
    tax_class VARCHAR(100),
    category_id UUID REFERENCES opportunity_schema.product_categories(id),
    currency_id UUID,
    website VARCHAR(500),
    support_name VARCHAR(255),
    support_description TEXT,
    support_contact VARCHAR(255),
    support_term VARCHAR(100),
    pricing_model VARCHAR(50),
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS opportunity_schema.quotes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    quote_num INTEGER,
    quote_stage VARCHAR(50) DEFAULT 'Draft',
    purchase_order_num VARCHAR(100),
    payment_terms VARCHAR(100),
    valid_until DATE,
    date_quote_expected_closed DATE,
    description TEXT,
    subtotal DECIMAL(26,6),
    subtotal_usdollar DECIMAL(26,6),
    discount_amount DECIMAL(26,6),
    tax DECIMAL(26,6),
    shipping DECIMAL(26,6),
    shipping_tax DECIMAL(26,6),
    total DECIMAL(26,6),
    total_usdollar DECIMAL(26,6),
    currency_id UUID,
    billing_address_street VARCHAR(255),
    billing_address_city VARCHAR(100),
    billing_address_state VARCHAR(100),
    billing_address_postal_code VARCHAR(20),
    billing_address_country VARCHAR(100),
    shipping_address_street VARCHAR(255),
    shipping_address_city VARCHAR(100),
    shipping_address_state VARCHAR(100),
    shipping_address_postal_code VARCHAR(20),
    shipping_address_country VARCHAR(100),
    opportunity_id UUID,
    account_id UUID,
    contact_id UUID,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS opportunity_schema.invoices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    invoice_number INTEGER,
    status VARCHAR(50) DEFAULT 'Unpaid',
    due_date DATE,
    quote_id UUID REFERENCES opportunity_schema.quotes(id),
    description TEXT,
    subtotal DECIMAL(26,6),
    subtotal_usdollar DECIMAL(26,6),
    discount_amount DECIMAL(26,6),
    tax DECIMAL(26,6),
    shipping DECIMAL(26,6),
    shipping_tax DECIMAL(26,6),
    total DECIMAL(26,6),
    total_usdollar DECIMAL(26,6),
    amount_due DECIMAL(26,6),
    currency_id UUID,
    billing_address_street VARCHAR(255),
    billing_address_city VARCHAR(100),
    billing_address_state VARCHAR(100),
    billing_address_postal_code VARCHAR(20),
    billing_address_country VARCHAR(100),
    shipping_address_street VARCHAR(255),
    shipping_address_city VARCHAR(100),
    shipping_address_state VARCHAR(100),
    shipping_address_postal_code VARCHAR(20),
    shipping_address_country VARCHAR(100),
    account_id UUID,
    contact_id UUID,
    opportunity_id UUID,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS opportunity_schema.line_item_groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id UUID NOT NULL,
    parent_type VARCHAR(50) NOT NULL,
    group_number INTEGER,
    name VARCHAR(255),
    description TEXT,
    subtotal DECIMAL(26,6),
    discount_amount DECIMAL(26,6),
    tax DECIMAL(26,6),
    total DECIMAL(26,6),
    currency_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS opportunity_schema.line_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID REFERENCES opportunity_schema.line_item_groups(id),
    product_id UUID REFERENCES opportunity_schema.products(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    part_number VARCHAR(100),
    quantity DECIMAL(12,2) DEFAULT 1,
    cost_price DECIMAL(26,6),
    list_price DECIMAL(26,6),
    unit_price DECIMAL(26,6),
    discount_price DECIMAL(26,6),
    discount_amount DECIMAL(26,6),
    discount_select BOOLEAN DEFAULT FALSE,
    tax_amount DECIMAL(26,6),
    total_amount DECIMAL(26,6),
    item_number INTEGER,
    currency_id UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE INDEX idx_products_category_id ON opportunity_schema.products(category_id);
CREATE INDEX idx_products_status ON opportunity_schema.products(status);
CREATE INDEX idx_quotes_opportunity_id ON opportunity_schema.quotes(opportunity_id);
CREATE INDEX idx_quotes_account_id ON opportunity_schema.quotes(account_id);
CREATE INDEX idx_quotes_stage ON opportunity_schema.quotes(quote_stage);
CREATE INDEX idx_invoices_account_id ON opportunity_schema.invoices(account_id);
CREATE INDEX idx_invoices_status ON opportunity_schema.invoices(status);
CREATE INDEX idx_invoices_quote_id ON opportunity_schema.invoices(quote_id);
CREATE INDEX idx_line_items_group_id ON opportunity_schema.line_items(group_id);
CREATE INDEX idx_line_item_groups_parent ON opportunity_schema.line_item_groups(parent_id, parent_type);
