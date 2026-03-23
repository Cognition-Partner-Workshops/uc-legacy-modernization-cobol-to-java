-- CardDemo Sample Data
-- Parsed from app/data/ASCII/ files

-- Transaction Types (from trantype.txt)
INSERT INTO transaction_types (type_cd, type_description) VALUES ('01', 'Purchase');
INSERT INTO transaction_types (type_cd, type_description) VALUES ('02', 'Payment');
INSERT INTO transaction_types (type_cd, type_description) VALUES ('03', 'Credit');
INSERT INTO transaction_types (type_cd, type_description) VALUES ('04', 'Authorization');
INSERT INTO transaction_types (type_cd, type_description) VALUES ('05', 'Refund');
INSERT INTO transaction_types (type_cd, type_description) VALUES ('06', 'Reversal');
INSERT INTO transaction_types (type_cd, type_description) VALUES ('07', 'Adjustment');

-- Transaction Categories (from trancatg.txt)
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('01', 1, 'Regular Sales Draft');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('01', 2, 'Regular Cash Advance');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('01', 3, 'Convenience Check Debit');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('01', 4, 'ATM Cash Advance');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('01', 5, 'Interest Amount');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('02', 1, 'Cash payment');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('02', 2, 'Electronic payment');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('02', 3, 'Check payment');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('03', 1, 'Credit to Account');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('03', 2, 'Credit to Purchase balance');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('03', 3, 'Credit to Cash balance');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('04', 1, 'Zero dollar authorization');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('04', 2, 'Online purchase authorization');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('04', 3, 'Travel booking authorization');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('05', 1, 'Refund credit');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('06', 1, 'Fraud reversal');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('06', 2, 'Non-fraud reversal');
INSERT INTO transaction_categories (type_cd, cat_cd, category_description) VALUES ('07', 1, 'Sales draft credit adjustment');

-- Accounts (from acctdata.txt - first 10 records)
INSERT INTO accounts (acct_id, active_status, current_balance, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, zip_code, group_id) VALUES ('00000000001', 'Y', 19400.00, 202000.00, 102000.00, '2014-11-20', '2025-05-20', '2025-05-20', 0.00, 0.00, 'A000000000', '');
INSERT INTO accounts (acct_id, active_status, current_balance, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, zip_code, group_id) VALUES ('00000000002', 'Y', 15800.00, 613000.00, 544800.00, '2013-06-19', '2024-08-11', '2024-08-11', 0.00, 0.00, 'A000000000', '');
INSERT INTO accounts (acct_id, active_status, current_balance, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, zip_code, group_id) VALUES ('00000000003', 'Y', 14700.00, 490900.00, 53800.00, '2013-08-23', '2024-01-10', '2024-01-10', 0.00, 0.00, 'A000000000', '');
INSERT INTO accounts (acct_id, active_status, current_balance, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, zip_code, group_id) VALUES ('00000000004', 'Y', 4000.00, 350300.00, 278900.00, '2012-11-17', '2023-12-16', '2023-12-16', 0.00, 0.00, 'A000000000', '');
INSERT INTO accounts (acct_id, active_status, current_balance, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, zip_code, group_id) VALUES ('00000000005', 'Y', 34500.00, 381900.00, 243000.00, '2012-10-03', '2025-03-09', '2025-03-09', 0.00, 0.00, 'A000000000', '');
INSERT INTO accounts (acct_id, active_status, current_balance, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, zip_code, group_id) VALUES ('00000000006', 'Y', 21800.00, 358400.00, 294800.00, '2017-12-23', '2025-10-08', '2025-10-08', 0.00, 0.00, 'A000000000', '');
INSERT INTO accounts (acct_id, active_status, current_balance, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, zip_code, group_id) VALUES ('00000000007', 'Y', 19300.00, 206500.00, 26400.00, '2012-10-12', '2024-12-13', '2024-12-13', 0.00, 0.00, 'A000000000', '');
INSERT INTO accounts (acct_id, active_status, current_balance, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, zip_code, group_id) VALUES ('00000000008', 'Y', 60500.00, 610400.00, 131800.00, '2012-01-04', '2024-05-20', '2024-05-20', 0.00, 0.00, 'A000000000', '');
INSERT INTO accounts (acct_id, active_status, current_balance, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, zip_code, group_id) VALUES ('00000000009', 'Y', 56000.00, 820100.00, 206500.00, '2016-08-27', '2024-12-27', '2024-12-27', 0.00, 0.00, 'A000000000', '');
INSERT INTO accounts (acct_id, active_status, current_balance, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, current_cycle_credit, current_cycle_debit, zip_code, group_id) VALUES ('00000000010', 'Y', 15900.00, 540100.00, 444200.00, '2015-09-13', '2023-01-27', '2023-01-27', 0.00, 0.00, 'A000000000', '');

-- Cards (from carddata.txt - first 10 records)
INSERT INTO cards (card_num, acct_id, cvv_code, embossed_name, expiration_date, active_status) VALUES ('0500024453765740', '00000000050', 747, 'Aniya Von', '2023-03-09', 'Y');
INSERT INTO cards (card_num, acct_id, cvv_code, embossed_name, expiration_date, active_status) VALUES ('0683586198171516', '00000000027', 567, 'Ward Jones', '2025-07-13', 'Y');
INSERT INTO cards (card_num, acct_id, cvv_code, embossed_name, expiration_date, active_status) VALUES ('0923877193247330', '00000000002', 28, 'Enrico Rosenbaum', '2024-08-11', 'Y');
INSERT INTO cards (card_num, acct_id, cvv_code, embossed_name, expiration_date, active_status) VALUES ('0927987108636232', '00000000020', 3, 'Carter Veum', '2024-03-13', 'Y');
INSERT INTO cards (card_num, acct_id, cvv_code, embossed_name, expiration_date, active_status) VALUES ('0982496213629795', '00000000012', 75, 'Maci Robel', '2023-07-07', 'Y');
INSERT INTO cards (card_num, acct_id, cvv_code, embossed_name, expiration_date, active_status) VALUES ('1014086565224350', '00000000044', 640, 'Irving Emard', '2024-01-17', 'Y');
INSERT INTO cards (card_num, acct_id, cvv_code, embossed_name, expiration_date, active_status) VALUES ('1142167692878931', '00000000037', 625, 'Shany Walker', '2023-10-24', 'Y');
INSERT INTO cards (card_num, acct_id, cvv_code, embossed_name, expiration_date, active_status) VALUES ('1561409106491600', '00000000035', 31, 'Angelica Dach', '2025-09-23', 'Y');
INSERT INTO cards (card_num, acct_id, cvv_code, embossed_name, expiration_date, active_status) VALUES ('2745303720002090', '00000000039', 33, 'Aliyah Berge', '2025-09-08', 'Y');
INSERT INTO cards (card_num, acct_id, cvv_code, embossed_name, expiration_date, active_status) VALUES ('2760836797107565', '00000000024', 859, 'Stefanie Dickinson', '2025-02-11', 'Y');

-- Card Cross References (from cardxref.txt - first 10 records)
INSERT INTO card_xref (card_num, cust_id, acct_id) VALUES ('0500024453765740', 50, '00000000050');
INSERT INTO card_xref (card_num, cust_id, acct_id) VALUES ('0683586198171516', 27, '00000000027');
INSERT INTO card_xref (card_num, cust_id, acct_id) VALUES ('0923877193247330', 2, '00000000002');
INSERT INTO card_xref (card_num, cust_id, acct_id) VALUES ('0927987108636232', 20, '00000000020');
INSERT INTO card_xref (card_num, cust_id, acct_id) VALUES ('0982496213629795', 12, '00000000012');
INSERT INTO card_xref (card_num, cust_id, acct_id) VALUES ('1014086565224350', 44, '00000000044');
INSERT INTO card_xref (card_num, cust_id, acct_id) VALUES ('1142167692878931', 37, '00000000037');
INSERT INTO card_xref (card_num, cust_id, acct_id) VALUES ('1561409106491600', 35, '00000000035');
INSERT INTO card_xref (card_num, cust_id, acct_id) VALUES ('2745303720002090', 39, '00000000039');
INSERT INTO card_xref (card_num, cust_id, acct_id) VALUES ('2760836797107565', 24, '00000000024');

-- Customers (from custdata.txt - first 5 records)
INSERT INTO customers (cust_id, first_name, middle_name, last_name, address_line1, address_line2, address_line3, state_code, country_code, zip_code, phone1, phone2, ssn, govt_issued_id, date_of_birth, eft_account_id, primary_card_holder_ind, fico_credit_score) VALUES (1, 'Immanuel', 'Madeline', 'Kessler', '618 Deshaun Route', 'Apt. 802', 'Altenwerthshire', 'NC', 'USA', '12546', '(908)119-8310', '(373)693-8684', '020973888', '0000000000', '1961-06-08', '0053581756', 'Y', 274);
INSERT INTO customers (cust_id, first_name, middle_name, last_name, address_line1, address_line2, address_line3, state_code, country_code, zip_code, phone1, phone2, ssn, govt_issued_id, date_of_birth, eft_account_id, primary_card_holder_ind, fico_credit_score) VALUES (2, 'Enrico', 'April', 'Rosenbaum', '4917 Myrna Flats', 'Apt. 453', 'West Bernita', 'IN', 'USA', '22770', '(429)706-9510', '(744)950-5272', '587518382', '0000000000', '1961-10-08', '0069194009', 'Y', 268);
INSERT INTO customers (cust_id, first_name, middle_name, last_name, address_line1, address_line2, address_line3, state_code, country_code, zip_code, phone1, phone2, ssn, govt_issued_id, date_of_birth, eft_account_id, primary_card_holder_ind, fico_credit_score) VALUES (3, 'Larry', 'Cody', 'Homenick', '362 Esta Parks', 'Apt. 390', 'New Gladys', 'GA', 'USA', '19852-6716', '(950)396-9024', '(685)168-8826', '317460867', '0000000000', '1987-11-30', '0006465789', 'Y', 616);
INSERT INTO customers (cust_id, first_name, middle_name, last_name, address_line1, address_line2, address_line3, state_code, country_code, zip_code, phone1, phone2, ssn, govt_issued_id, date_of_birth, eft_account_id, primary_card_holder_ind, fico_credit_score) VALUES (4, 'Delbert', 'Kaia', 'Parisian', '638 Blanda Gateway', 'Apt. 076', 'Lake Virginie', 'MI', 'USA', '39035-0455', '(801)603-4121', '(156)074-6837', '660354258', '0000000000', '1985-01-13', '0040802739', 'Y', 776);
INSERT INTO customers (cust_id, first_name, middle_name, last_name, address_line1, address_line2, address_line3, state_code, country_code, zip_code, phone1, phone2, ssn, govt_issued_id, date_of_birth, eft_account_id, primary_card_holder_ind, fico_credit_score) VALUES (5, 'Treva', 'Manley', 'Schowalter', '5653 Legros Plaza', 'Apt. 968', 'Alvinaport', 'MI', 'USA', '02251-1698', '(978)775-4633', '(439)943-7644', '611264288', '0000000000', '1971-09-29', '0006365573', 'Y', 529);

-- Disclosure Groups (from discgrp.txt - DEFAULT group)
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '01', 1, 15.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '01', 2, 25.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '01', 3, 25.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '01', 4, 25.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '02', 1, 0.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '02', 2, 0.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '02', 3, 0.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '03', 1, 0.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '03', 2, 0.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '03', 3, 0.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '04', 1, 15.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '04', 2, 15.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '04', 3, 15.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '05', 1, 15.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '06', 1, 15.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '06', 2, 15.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('DEFAULT', '07', 1, 0.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('ZEROAPR', '01', 1, 0.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('ZEROAPR', '01', 2, 0.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('ZEROAPR', '01', 3, 0.00);
INSERT INTO disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, interest_rate) VALUES ('ZEROAPR', '01', 4, 0.00);

-- Transaction Category Balances (from tcatbal.txt - first 10 records)
INSERT INTO transaction_category_balances (acct_id, type_cd, cat_cd, balance) VALUES ('00000000001', '01', 1, 0.00);
INSERT INTO transaction_category_balances (acct_id, type_cd, cat_cd, balance) VALUES ('00000000002', '01', 1, 0.00);
INSERT INTO transaction_category_balances (acct_id, type_cd, cat_cd, balance) VALUES ('00000000003', '01', 1, 0.00);
INSERT INTO transaction_category_balances (acct_id, type_cd, cat_cd, balance) VALUES ('00000000004', '01', 1, 0.00);
INSERT INTO transaction_category_balances (acct_id, type_cd, cat_cd, balance) VALUES ('00000000005', '01', 1, 0.00);
INSERT INTO transaction_category_balances (acct_id, type_cd, cat_cd, balance) VALUES ('00000000006', '01', 1, 0.00);
INSERT INTO transaction_category_balances (acct_id, type_cd, cat_cd, balance) VALUES ('00000000007', '01', 1, 0.00);
INSERT INTO transaction_category_balances (acct_id, type_cd, cat_cd, balance) VALUES ('00000000008', '01', 1, 0.00);
INSERT INTO transaction_category_balances (acct_id, type_cd, cat_cd, balance) VALUES ('00000000009', '01', 1, 0.00);
INSERT INTO transaction_category_balances (acct_id, type_cd, cat_cd, balance) VALUES ('00000000010', '01', 1, 0.00);

-- User Security (default users - passwords stored as plain text for legacy compatibility)
-- DataInitializer will BCrypt-encode if these aren't present
INSERT INTO user_security (user_id, first_name, last_name, password, user_type) VALUES ('ADMIN001', 'System', 'Admin', 'ADMIN001', 'A');
INSERT INTO user_security (user_id, first_name, last_name, password, user_type) VALUES ('USER0001', 'Default', 'User', 'USER0001', 'U');
