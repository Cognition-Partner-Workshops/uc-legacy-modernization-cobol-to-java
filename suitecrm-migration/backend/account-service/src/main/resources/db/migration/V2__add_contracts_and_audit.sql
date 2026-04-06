-- V2: Add contracts and account audit tables
-- Maps to AS-IS SuiteCRM modules: Contracts, AccountAudit

CREATE TABLE IF NOT EXISTS account_schema.contracts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    reference_code VARCHAR(100),
    status VARCHAR(50) DEFAULT 'Draft',
    start_date DATE,
    end_date DATE,
    company_signed_date DATE,
    customer_signed_date DATE,
    contract_type VARCHAR(50),
    renewal_reminder_date DATE,
    description TEXT,
    total_contract_value DECIMAL(26,6),
    currency_id UUID,
    account_id UUID REFERENCES account_schema.accounts(id),
    opportunity_id UUID,
    contact_id UUID,
    assigned_user_id UUID,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS account_schema.account_audit (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id UUID NOT NULL REFERENCES account_schema.accounts(id),
    field_name VARCHAR(100) NOT NULL,
    data_type VARCHAR(100),
    before_value_string VARCHAR(255),
    after_value_string VARCHAR(255),
    before_value_text TEXT,
    after_value_text TEXT,
    created_by UUID,
    date_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contracts_account_id ON account_schema.contracts(account_id);
CREATE INDEX idx_contracts_status ON account_schema.contracts(status);
CREATE INDEX idx_contracts_end_date ON account_schema.contracts(end_date);
CREATE INDEX idx_account_audit_parent_id ON account_schema.account_audit(parent_id);
