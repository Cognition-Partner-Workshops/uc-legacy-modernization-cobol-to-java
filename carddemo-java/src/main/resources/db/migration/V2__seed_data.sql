-- V2__seed_data.sql
-- Seed data for CardDemo - parsed from app/data/ASCII/*.txt fixed-width files
-- Note: EBCDIC '{' convention means +0 (positive sign overpunch)

-- Default users (admin and regular user)
INSERT INTO users (user_id, password, user_type, first_name, last_name)
VALUES ('ADMIN001', 'ADMIN001', 'A', 'Admin', 'User');
INSERT INTO users (user_id, password, user_type, first_name, last_name)
VALUES ('USER0001', 'USER0001', 'U', 'Regular', 'User');

-- Sample accounts from acctdata.txt
-- Format: 11-char acctId, 1-char status, balance fields with { as +0, dates YYYY-MM-DD
INSERT INTO accounts (acct_id, active_status, current_balance, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, address_zip, group_id)
VALUES (1, 'Y', 19400.00, 20200.00, 10200.00, '2014-11-20', '2025-05-20', '2025-05-20', 0.00, 0.00, '', 'A000000000');
INSERT INTO accounts (acct_id, active_status, current_balance, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, address_zip, group_id)
VALUES (2, 'Y', 15800.00, 61300.00, 54480.00, '2013-06-19', '2024-08-11', '2024-08-11', 0.00, 0.00, '', 'A000000000');
INSERT INTO accounts (acct_id, active_status, current_balance, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, address_zip, group_id)
VALUES (3, 'Y', 14700.00, 49090.00, 5380.00, '2013-08-23', '2024-01-10', '2024-01-10', 0.00, 0.00, '', 'A000000000');
INSERT INTO accounts (acct_id, active_status, current_balance, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, address_zip, group_id)
VALUES (4, 'Y', 4000.00, 35030.00, 27890.00, '2012-11-17', '2023-12-16', '2023-12-16', 0.00, 0.00, '', 'A000000000');
INSERT INTO accounts (acct_id, active_status, current_balance, credit_limit, cash_credit_limit,
    open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, address_zip, group_id)
VALUES (5, 'Y', 34500.00, 38190.00, 24300.00, '2012-10-03', '2025-03-09', '2025-03-09', 0.00, 0.00, '', 'A000000000');

-- Sample customers from custdata.txt
INSERT INTO customers (cust_id, first_name, middle_name, last_name, address_line1, address_line2, address_line3,
    state_code, country_code, zip_code, phone1, phone2, ssn, govt_issued_id, date_of_birth, eft_account_id,
    primary_card_holder_ind, fico_credit_score)
VALUES (1, 'Immanuel', 'Madeline', 'Kessler', '618 Deshaun Route', 'Apt. 802', 'Altenwerthshire',
    'NC', 'USA', '12546', '(908)119-8310', '(373)693-8684', 20973888, '0000000000493684371', '1961-06-08', '0053581756', 'Y', 274);
INSERT INTO customers (cust_id, first_name, middle_name, last_name, address_line1, address_line2, address_line3,
    state_code, country_code, zip_code, phone1, phone2, ssn, govt_issued_id, date_of_birth, eft_account_id,
    primary_card_holder_ind, fico_credit_score)
VALUES (2, 'Enrico', 'April', 'Rosenbaum', '4917 Myrna Flats', 'Apt. 453', 'West Bernita',
    'IN', 'USA', '22770', '(429)706-9510', '(744)950-5272', 587518382, '0000000000506210371', '1961-10-08', '0069194009', 'Y', 268);
INSERT INTO customers (cust_id, first_name, middle_name, last_name, address_line1, address_line2, address_line3,
    state_code, country_code, zip_code, phone1, phone2, ssn, govt_issued_id, date_of_birth, eft_account_id,
    primary_card_holder_ind, fico_credit_score)
VALUES (3, 'Larry', 'Cody', 'Homenick', '362 Esta Parks', 'Apt. 390', 'New Gladys',
    'GA', 'USA', '19852-6716', '(950)396-9024', '(685)168-8826', 317460867, '0000000000052419303', '1987-11-30', '0006465789', 'Y', 616);
INSERT INTO customers (cust_id, first_name, middle_name, last_name, address_line1, address_line2, address_line3,
    state_code, country_code, zip_code, phone1, phone2, ssn, govt_issued_id, date_of_birth, eft_account_id,
    primary_card_holder_ind, fico_credit_score)
VALUES (4, 'Delbert', 'Kaia', 'Parisian', '638 Blanda Gateway', 'Apt. 076', 'Lake Virginie',
    'MI', 'USA', '39035-0455', '(801)603-4121', '(156)074-6837', 660354258, '0000000000068579249', '1985-01-13', '0040802739', 'Y', 776);
INSERT INTO customers (cust_id, first_name, middle_name, last_name, address_line1, address_line2, address_line3,
    state_code, country_code, zip_code, phone1, phone2, ssn, govt_issued_id, date_of_birth, eft_account_id,
    primary_card_holder_ind, fico_credit_score)
VALUES (5, 'Treva', 'Manley', 'Schowalter', '5653 Legros Plaza', 'Apt. 968', 'Alvinaport',
    'MI', 'USA', '02251-1698', '(978)775-4633', '(439)943-7644', 611264288, '0000000000639799754', '1971-09-29', '0006365573', 'Y', 529);

-- Sample credit cards from carddata.txt
INSERT INTO credit_cards (card_num, acct_id, cvv_code, embossed_name, expiration_date, active_status)
VALUES ('0500024453765740', 50, 747, 'Aniya Von', '2023-03-09', 'Y');
INSERT INTO credit_cards (card_num, acct_id, cvv_code, embossed_name, expiration_date, active_status)
VALUES ('0683586198171516', 27, 567, 'Ward Jones', '2025-07-13', 'Y');
INSERT INTO credit_cards (card_num, acct_id, cvv_code, embossed_name, expiration_date, active_status)
VALUES ('0923877193247330', 2, 28, 'Enrico Rosenbaum', '2024-08-11', 'Y');
INSERT INTO credit_cards (card_num, acct_id, cvv_code, embossed_name, expiration_date, active_status)
VALUES ('0927987108636232', 20, 3, 'Carter Veum', '2024-03-13', 'Y');
INSERT INTO credit_cards (card_num, acct_id, cvv_code, embossed_name, expiration_date, active_status)
VALUES ('0982496213629795', 12, 75, 'Maci Robel', '2023-07-07', 'Y');

-- Sample card xrefs from cardxref.txt
INSERT INTO card_xrefs (card_num, cust_id, acct_id) VALUES ('0500024453765740', 5, 50);
INSERT INTO card_xrefs (card_num, cust_id, acct_id) VALUES ('0683586198171516', 27, 27);
INSERT INTO card_xrefs (card_num, cust_id, acct_id) VALUES ('0923877193247330', 2, 2);
INSERT INTO card_xrefs (card_num, cust_id, acct_id) VALUES ('0927987108636232', 20, 20);
INSERT INTO card_xrefs (card_num, cust_id, acct_id) VALUES ('0982496213629795', 12, 12);

-- Sample transactions from dailytran.txt
INSERT INTO transactions (card_num, tran_id, type_cd, cat_cd, source, description, amount,
    merchant_id, merchant_name, merchant_city, merchant_zip, orig_timestamp, proc_timestamp)
VALUES ('0000000000683580', '0100010000000000', '01', 1, 'POS TERM', 'Purchase at Abshire-Lowe',
    50.47, 800000000, 'Abshire-Lowe', 'North Enoshaven', '72112', '2022-06-10 19:27:53.000000', '');
INSERT INTO transactions (card_num, tran_id, type_cd, cat_cd, source, description, amount,
    merchant_id, merchant_name, merchant_city, merchant_zip, orig_timestamp, proc_timestamp)
VALUES ('0000000001774260', '0300010000000000', '03', 1, 'OPERATOR', 'Return item at Nitzsche, Nicolas and Lowe',
    -91.90, 800000000, 'Nitzsche, Nicolas and Lowe', 'Fidelshire', '53378', '2022-06-10 19:27:53.000000', '');

-- Sample tran_cat_balances from tcatbal.txt
INSERT INTO tran_cat_balances (acct_id, type_cd, cat_cd, balance)
VALUES (1, '01', 1, 1500.00);
INSERT INTO tran_cat_balances (acct_id, type_cd, cat_cd, balance)
VALUES (1, '01', 2, 0.00);
INSERT INTO tran_cat_balances (acct_id, type_cd, cat_cd, balance)
VALUES (2, '01', 1, 0.00);

-- Sample discount groups from discgrp.txt
INSERT INTO discount_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate)
VALUES ('A000000000', '01', 1, 0.000000);
INSERT INTO discount_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate)
VALUES ('A000000000', '01', 2, 0.000000);
