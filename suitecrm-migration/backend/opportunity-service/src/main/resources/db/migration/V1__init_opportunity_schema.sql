CREATE SCHEMA IF NOT EXISTS opportunity_schema;
SET search_path TO opportunity_schema;

CREATE TABLE opportunities (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    account_id          UUID,
    amount              NUMERIC(26,6),
    currency_id         UUID,
    date_closed         DATE,
    sales_stage         VARCHAR(50) DEFAULT 'Prospecting',
    probability         DOUBLE PRECISION,
    lead_source         VARCHAR(100),
    next_step           VARCHAR(255),
    opportunity_type    VARCHAR(50),
    campaign_id         UUID,
    description         TEXT,
    assigned_user_id    UUID,
    created_by          UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_sales_stage CHECK (sales_stage IN (
        'Prospecting','Qualification','Needs Analysis','Value Proposition',
        'Id. Decision Makers','Perception Analysis','Proposal/Price Quote',
        'Negotiation/Review','Closed Won','Closed Lost'))
);

CREATE INDEX idx_opp_name ON opportunities(name);
CREATE INDEX idx_opp_account ON opportunities(account_id);
CREATE INDEX idx_opp_stage ON opportunities(sales_stage);
CREATE INDEX idx_opp_date_closed ON opportunities(date_closed);
CREATE INDEX idx_opp_assigned ON opportunities(assigned_user_id);
CREATE INDEX idx_opp_deleted ON opportunities(deleted);

CREATE TABLE quotes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    opportunity_id      UUID REFERENCES opportunities(id),
    account_id          UUID,
    quote_stage         VARCHAR(50) DEFAULT 'Draft',
    purchase_order_num  VARCHAR(100),
    payment_terms       VARCHAR(100),
    valid_until         DATE,
    subtotal            NUMERIC(26,6),
    discount            NUMERIC(26,6) DEFAULT 0,
    tax                 NUMERIC(26,6) DEFAULT 0,
    shipping            NUMERIC(26,6) DEFAULT 0,
    total               NUMERIC(26,6),
    currency_id         UUID,
    billing_address_street      VARCHAR(255),
    billing_address_city        VARCHAR(100),
    billing_address_state       VARCHAR(100),
    billing_address_postalcode  VARCHAR(20),
    billing_address_country     VARCHAR(100),
    shipping_address_street     VARCHAR(255),
    shipping_address_city       VARCHAR(100),
    shipping_address_state      VARCHAR(100),
    shipping_address_postalcode VARCHAR(20),
    shipping_address_country    VARCHAR(100),
    description         TEXT,
    assigned_user_id    UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE invoices (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    quote_id            UUID REFERENCES quotes(id),
    opportunity_id      UUID REFERENCES opportunities(id),
    account_id          UUID,
    invoice_status      VARCHAR(50) DEFAULT 'Unpaid',
    due_date            DATE,
    subtotal            NUMERIC(26,6),
    discount            NUMERIC(26,6) DEFAULT 0,
    tax                 NUMERIC(26,6) DEFAULT 0,
    shipping            NUMERIC(26,6) DEFAULT 0,
    total               NUMERIC(26,6),
    amount_due          NUMERIC(26,6),
    currency_id         UUID,
    description         TEXT,
    assigned_user_id    UUID,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE products (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(255) NOT NULL,
    product_code        VARCHAR(100),
    category            VARCHAR(100),
    cost_price          NUMERIC(26,6),
    list_price          NUMERIC(26,6),
    discount_price      NUMERIC(26,6),
    currency_id         UUID,
    tax_class           VARCHAR(50),
    weight              NUMERIC(10,2),
    quantity_in_stock   INTEGER DEFAULT 0,
    description         TEXT,
    date_entered        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE line_items (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_type         VARCHAR(50) NOT NULL,
    parent_id           UUID NOT NULL,
    product_id          UUID REFERENCES products(id),
    name                VARCHAR(255) NOT NULL,
    quantity            NUMERIC(10,2) DEFAULT 1,
    unit_price          NUMERIC(26,6),
    discount_amount     NUMERIC(26,6) DEFAULT 0,
    discount_percentage NUMERIC(5,2) DEFAULT 0,
    tax_amount          NUMERIC(26,6) DEFAULT 0,
    total_amount        NUMERIC(26,6),
    position            INTEGER DEFAULT 0,
    description         TEXT,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_line_items_parent ON line_items(parent_type, parent_id);
