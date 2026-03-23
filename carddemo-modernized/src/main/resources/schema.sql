-- ============================================================
-- CardDemo Modernized - Relational Schema (JDBC/ODBC Layer)
-- Mirrors the legacy DB2 tables from the mainframe application
-- ============================================================

-- Transaction Types (from COBOL copybook CVTRA03Y)
-- Legacy: TRAN-TYPE-RECORD, RECLN = 60
CREATE TABLE IF NOT EXISTS transaction_types (
    type_code       VARCHAR(2)   PRIMARY KEY,
    type_description VARCHAR(50) NOT NULL
);

-- Transaction Categories (from COBOL copybook CVTRA04Y)
-- Legacy: TRAN-CAT-RECORD, RECLN = 60
CREATE TABLE IF NOT EXISTS transaction_categories (
    type_code           VARCHAR(2)  NOT NULL,
    category_code       INT         NOT NULL,
    category_description VARCHAR(50) NOT NULL,
    PRIMARY KEY (type_code, category_code),
    FOREIGN KEY (type_code) REFERENCES transaction_types(type_code)
);

-- Disclosure Groups (from COBOL copybook CVTRA02Y)
CREATE TABLE IF NOT EXISTS disclosure_groups (
    group_id            VARCHAR(10) PRIMARY KEY,
    group_description   VARCHAR(50) NOT NULL
);

-- Transaction Category Balances (from COBOL copybook CVTRA01Y)
CREATE TABLE IF NOT EXISTS transaction_category_balances (
    type_code       VARCHAR(2)      NOT NULL,
    category_code   INT             NOT NULL,
    balance         DECIMAL(12,2)   NOT NULL DEFAULT 0,
    PRIMARY KEY (type_code, category_code)
);

-- Audit log for batch processing tracking
CREATE TABLE IF NOT EXISTS batch_job_audit (
    job_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_name        VARCHAR(8)   NOT NULL,
    job_status      VARCHAR(20)  NOT NULL,
    start_time      TIMESTAMP    NOT NULL,
    end_time        TIMESTAMP,
    records_processed BIGINT     DEFAULT 0,
    error_message   VARCHAR(500)
);

-- Insert default transaction types
MERGE INTO transaction_types (type_code, type_description) KEY(type_code) VALUES
    ('SA', 'Sale'),
    ('RT', 'Return'),
    ('CR', 'Credit'),
    ('DB', 'Debit'),
    ('PM', 'Payment'),
    ('IN', 'Interest'),
    ('FE', 'Fee'),
    ('CA', 'Cash Advance');

-- Insert default transaction categories
MERGE INTO transaction_categories (type_code, category_code, category_description) KEY(type_code, category_code) VALUES
    ('SA', 5001, 'Retail Purchase'),
    ('SA', 5002, 'Online Purchase'),
    ('SA', 5003, 'Recurring Payment'),
    ('RT', 6001, 'Merchandise Return'),
    ('CR', 7001, 'Account Credit'),
    ('PM', 8001, 'Monthly Payment'),
    ('PM', 8002, 'One-time Payment'),
    ('IN', 9001, 'Monthly Interest'),
    ('FE', 9501, 'Annual Fee'),
    ('FE', 9502, 'Late Payment Fee'),
    ('CA', 9901, 'ATM Cash Advance');
