-- ============================================================================
-- CardDemo Seed Data - Migrated from EBCDIC sample data files
-- ============================================================================
-- Original data from app/data/EBCDIC/*.PS files
-- Default credentials: ADMIN001/PASSWORD, USER0001/PASSWORD
-- Passwords are BCrypt-hashed in the users table
-- ============================================================================

-- Seed Users (from AWS.M2.CARDDEMO.USRSEC.PS)
-- Original COBOL: SEC-USR-ID(8), SEC-USR-FNAME(20), SEC-USR-LNAME(20), SEC-USR-PWD(8), SEC-USR-TYPE(1)
-- BCrypt hash of 'PASSWORD'
INSERT INTO users (usr_id, usr_first_name, usr_last_name, usr_pwd, usr_type) VALUES
    ('ADMIN001', 'ADMIN', 'USER', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'A'),
    ('USER0001', 'FIRST', 'USER', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'U');

-- Seed Transaction Types (from AWS.M2.CARDDEMO.TRANTYPE.PS)
INSERT INTO transaction_types (tran_type_cd, tran_type_desc) VALUES
    ('01', 'Purchase'),
    ('02', 'Return'),
    ('03', 'Cash Advance'),
    ('04', 'Balance Transfer'),
    ('05', 'Payment');

-- Seed Transaction Categories (from AWS.M2.CARDDEMO.TRANCATG.PS)
INSERT INTO transaction_categories (tran_cat_cd, tran_cat_desc) VALUES
    (5001, 'Groceries'),
    (5002, 'Gas/Fuel'),
    (5003, 'Restaurant'),
    (5004, 'Travel'),
    (5005, 'Entertainment'),
    (5006, 'Utilities'),
    (5007, 'Healthcare'),
    (5008, 'Retail');

-- Seed Accounts (from AWS.M2.CARDDEMO.ACCTDATA.PS)
INSERT INTO accounts (acct_id, acct_active_status, acct_curr_bal, acct_credit_limit, acct_cash_credit_limit,
    acct_open_date, acct_expiration_date, acct_reissue_date, acct_curr_cyc_credit, acct_curr_cyc_debit,
    acct_addr_zip, acct_group_id) VALUES
    (00000000001, 'Y', 1500.00, 5000.00, 1000.00, '2020-01-15', '2025-01-15', '2024-01-15', 200.00, 150.00, '98101', 'GROUP0001'),
    (00000000002, 'Y', 3200.50, 10000.00, 2000.00, '2019-06-20', '2024-06-20', '2023-06-20', 500.00, 300.00, '10001', 'GROUP0001'),
    (00000000003, 'Y', 750.25, 3000.00, 500.00, '2021-03-10', '2026-03-10', '2025-03-10', 100.00, 75.00, '60601', 'GROUP0002');

-- Seed Customers (from AWS.M2.CARDDEMO.CUSTDATA.PS)
INSERT INTO customers (cust_id, cust_first_name, cust_middle_name, cust_last_name,
    cust_addr_line_1, cust_addr_line_2, cust_addr_line_3,
    cust_addr_state_cd, cust_addr_country_cd, cust_addr_zip,
    cust_phone_num_1, cust_phone_num_2, cust_ssn, cust_govt_issued_id,
    cust_dob_yyyy_mm_dd, cust_eft_account_id, cust_pri_card_holder_ind, cust_fico_credit_score) VALUES
    (000000001, 'JOHN', 'M', 'DOE', '123 MAIN ST', 'APT 4B', '', 'WA', 'USA', '98101',
     '206-555-0100', '206-555-0101', 123456789, 'DL12345678', '1985-05-15', '1234567890', 'Y', 750),
    (000000002, 'JANE', 'A', 'SMITH', '456 OAK AVE', '', '', 'NY', 'USA', '10001',
     '212-555-0200', '', 987654321, 'DL87654321', '1990-08-22', '9876543210', 'Y', 800),
    (000000003, 'ROBERT', '', 'JOHNSON', '789 ELM BLVD', 'SUITE 100', '', 'IL', 'USA', '60601',
     '312-555-0300', '312-555-0301', 456789123, 'DL45678912', '1978-12-01', '4567891230', 'Y', 680);

-- Seed Cards (from AWS.M2.CARDDEMO.CARDDATA.PS)
INSERT INTO cards (card_num, card_acct_id, card_cvv_cd, card_embossed_name, card_expiration_date, card_active_status) VALUES
    ('4111111111111111', 00000000001, 123, 'JOHN M DOE', '2025-01-15', 'Y'),
    ('4222222222222222', 00000000002, 456, 'JANE A SMITH', '2024-06-20', 'Y'),
    ('4333333333333333', 00000000003, 789, 'ROBERT JOHNSON', '2026-03-10', 'Y');

-- Seed Card Cross References (from AWS.M2.CARDDEMO.CARDXREF.PS)
INSERT INTO card_xref (xref_card_num, xref_cust_id, xref_acct_id) VALUES
    ('4111111111111111', 000000001, 00000000001),
    ('4222222222222222', 000000002, 00000000002),
    ('4333333333333333', 000000003, 00000000003);

-- Seed Disclosure Groups (from AWS.M2.CARDDEMO.DISCGRP.PS)
INSERT INTO disclosure_groups (dis_acct_group_id, dis_tran_type_cd, dis_tran_cat_cd, dis_int_rate, dis_fee_amt) VALUES
    ('GROUP0001', '01', 5001, 0.1899, 0.00),
    ('GROUP0001', '03', 5001, 0.2499, 5.00),
    ('GROUP0002', '01', 5001, 0.2199, 0.00),
    ('GROUP0002', '03', 5001, 0.2799, 10.00);

-- Seed Transaction Category Balances (from AWS.M2.CARDDEMO.TCATBALF.PS)
INSERT INTO tran_cat_balances (trancat_acct_id, trancat_type_cd, trancat_cd, trancat_bal) VALUES
    (00000000001, '01', 5001, 500.00),
    (00000000001, '01', 5003, 250.00),
    (00000000002, '01', 5001, 1200.00),
    (00000000002, '03', 5001, 800.00),
    (00000000003, '01', 5002, 150.00);

-- Seed Transactions (from AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS)
INSERT INTO transactions (tran_id, tran_type_cd, tran_cat_cd, tran_source, tran_desc, tran_amt,
    tran_merchant_id, tran_merchant_name, tran_merchant_city, tran_merchant_zip,
    tran_card_num, tran_orig_ts, tran_proc_ts) VALUES
    ('0000000000000001', '01', 5001, 'POS', 'GROCERY PURCHASE', 45.67, 100000001, 'WHOLE FOODS', 'SEATTLE', '98101',
     '4111111111111111', '2024-01-15-10.30.00.000000', '2024-01-15-10.30.01.000000'),
    ('0000000000000002', '01', 5003, 'POS', 'RESTAURANT DINNER', 78.90, 100000002, 'RESTAURANT XYZ', 'SEATTLE', '98101',
     '4111111111111111', '2024-01-16-19.45.00.000000', '2024-01-16-19.45.01.000000'),
    ('0000000000000003', '01', 5001, 'ONLINE', 'ONLINE GROCERY ORDER', 125.50, 100000003, 'AMAZON FRESH', 'NEW YORK', '10001',
     '4222222222222222', '2024-01-17-14.20.00.000000', '2024-01-17-14.20.01.000000');
