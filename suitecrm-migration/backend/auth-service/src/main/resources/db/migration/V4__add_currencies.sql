SET search_path TO auth_schema;

CREATE TABLE currencies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    symbol VARCHAR(50) NOT NULL,
    iso4217 VARCHAR(3) NOT NULL UNIQUE,
    conversion_rate NUMERIC(26,6) DEFAULT 1.000000,
    status VARCHAR(100) DEFAULT 'Active',
    date_entered TIMESTAMP NOT NULL DEFAULT NOW(),
    date_modified TIMESTAMP DEFAULT NOW(),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

INSERT INTO currencies (name, symbol, iso4217, conversion_rate, status)
VALUES ('US Dollar', '$', 'USD', 1.000000, 'Active'),
       ('Euro', '€', 'EUR', 0.920000, 'Active'),
       ('British Pound', '£', 'GBP', 0.790000, 'Active'),
       ('Japanese Yen', '¥', 'JPY', 149.500000, 'Active'),
       ('Indian Rupee', '₹', 'INR', 83.200000, 'Active');

CREATE INDEX idx_currencies_iso ON currencies(iso4217) WHERE deleted = FALSE;
