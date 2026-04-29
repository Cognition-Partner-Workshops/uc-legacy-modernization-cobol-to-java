-- CardDemo Seed Data - Matches COBOL sample data structure

-- Users (passwords are BCrypt-hashed values of 'PASSWORD')
INSERT INTO users (user_id, first_name, last_name, password, user_type) VALUES
('ADMIN001', 'ADMIN', 'USER', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'A'),
('USER0001', 'REGULAR', 'USER', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'U');

-- Accounts
INSERT INTO accounts (account_id, active_status, current_balance, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, address_zip, group_id) VALUES
(12345678901, 'Y', 1500.00, 5000.00, 1000.00, '2020-01-15', '2025-01-15', '2023-01-15', 200.00, 350.00, '60601', 'GROUP001'),
(12345678902, 'Y', 3200.50, 10000.00, 2000.00, '2019-06-20', '2024-06-20', '2022-06-20', 500.00, 750.00, '10001', 'GROUP001'),
(12345678903, 'N', 0.00, 3000.00, 500.00, '2018-03-10', '2023-03-10', '2021-03-10', 0.00, 0.00, '90210', 'GROUP002');

-- Customers
INSERT INTO customers (customer_id, first_name, middle_name, last_name, address_line_1, address_line_2, address_line_3, state_code, country_code, zip_code, phone_number_1, phone_number_2, ssn, govt_issued_id, date_of_birth, eft_account_id, primary_card_holder_ind, fico_credit_score) VALUES
(100000001, 'JOHN', 'M', 'DOE', '123 MAIN ST', 'APT 4B', '', 'IL', 'USA', '60601', '312-555-0101', '', '123456789', 'DL12345678', '1985-03-15', 'EFT001', 'Y', 750),
(100000002, 'JANE', 'A', 'SMITH', '456 OAK AVE', '', '', 'NY', 'USA', '10001', '212-555-0202', '212-555-0303', '987654321', 'PP87654321', '1990-07-22', 'EFT002', 'Y', 680),
(100000003, 'BOB', '', 'JOHNSON', '789 PINE RD', 'SUITE 100', '', 'CA', 'USA', '90210', '310-555-0404', '', '456789123', 'DL98765432', '1978-11-30', 'EFT003', 'N', 720);

-- Cards
INSERT INTO cards (card_number, account_id, cvv_code, embossed_name, expiration_date, active_status) VALUES
('4111111111111111', 12345678901, '123', 'JOHN M DOE', '2025-01-15', 'Y'),
('4222222222222222', 12345678902, '456', 'JANE A SMITH', '2024-06-20', 'Y'),
('4333333333333333', 12345678903, '789', 'BOB JOHNSON', '2023-03-10', 'N');

-- Card Cross References
INSERT INTO card_cross_references (card_number, customer_id, account_id) VALUES
('4111111111111111', 100000001, 12345678901),
('4222222222222222', 100000002, 12345678902),
('4333333333333333', 100000003, 12345678903);

-- Transactions
INSERT INTO transactions (transaction_id, type_code, category_code, source, description, amount, merchant_id, merchant_name, merchant_city, merchant_zip, card_number, origin_timestamp, processed_timestamp) VALUES
('TRN0000000000001', 'SA', 5001, 'ONLINE', 'GROCERY PURCHASE', 125.50, 100000001, 'WHOLE FOODS MARKET', 'CHICAGO', '60601', '4111111111111111', '2024-01-15 10:30:00', '2024-01-15 10:30:05'),
('TRN0000000000002', 'SA', 5002, 'POS', 'GAS STATION', 45.00, 100000002, 'SHELL OIL', 'NEW YORK', '10001', '4222222222222222', '2024-01-16 14:22:00', '2024-01-16 14:22:03'),
('TRN0000000000003', 'CR', 5003, 'ONLINE', 'REFUND - RETURNED ITEM', -75.25, 100000003, 'AMAZON', 'SEATTLE', '98101', '4111111111111111', '2024-01-17 09:15:00', '2024-01-17 09:15:10');
