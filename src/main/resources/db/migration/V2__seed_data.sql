-- Seed data migrated from VSAM PS flat files (app/data/ASCII/)

-- Users (from USRSEC VSAM - passwords are BCrypt-hashed for Spring Security)
INSERT INTO users (user_id, first_name, last_name, password, user_type) VALUES
('USER0001', 'John', 'Smith', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'U'),
('USER0002', 'Jane', 'Doe', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'U'),
('USER0003', 'Bob', 'Wilson', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'U'),
('ADMIN01', 'Admin', 'User', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'A');

-- Accounts (from ACCTDATA VSAM)
INSERT INTO accounts (acct_id, active_status, curr_bal, credit_limit, cash_credit_limit, open_date, expiration_date, reissue_date, curr_cyc_credit, curr_cyc_debit, addr_zip, group_id) VALUES
(1, 'Y', 1940.00, 20200.00, 10200.00, '2014-11-20', '2025-05-20', '2025-05-20', 0.00, 0.00, 'A000000000', ''),
(2, 'Y', 1580.00, 61300.00, 54480.00, '2013-06-19', '2024-08-11', '2024-08-11', 0.00, 0.00, 'A000000000', ''),
(3, 'Y', 1470.00, 49090.00, 5380.00, '2013-08-23', '2024-01-10', '2024-01-10', 0.00, 0.00, 'A000000000', ''),
(4, 'Y', 400.00, 35030.00, 27890.00, '2012-11-17', '2023-12-16', '2023-12-16', 0.00, 0.00, 'A000000000', ''),
(5, 'Y', 3450.00, 38190.00, 24300.00, '2012-10-03', '2025-03-09', '2025-03-09', 0.00, 0.00, 'A000000000', '');

-- Customers (from CUSTDATA VSAM)
INSERT INTO customers (cust_id, first_name, middle_name, last_name, addr_line_1, addr_line_2, addr_line_3, addr_state_cd, addr_country_cd, addr_zip, phone_num_1, phone_num_2, ssn, govt_issued_id, dob, eft_account_id, pri_card_holder_ind, fico_credit_score) VALUES
(1, 'Immanuel', 'Madeline', 'Kessler', '618 Deshaun Route', 'Apt. 802', 'Altenwerthshire', 'NC', 'USA', '12546', '(908)119-8310', '(373)693-8684', 20973888, '000000000', '1961-06-08', '0053581756', 'Y', 274),
(2, 'Enrico', 'April', 'Rosenbaum', '4917 Myrna Flats', 'Apt. 453', 'West Bernita', 'IN', 'USA', '22770', '(429)706-9510', '(744)950-5272', 587518382, '000000000', '1961-10-08', '0069194009', 'Y', 268),
(3, 'Larry', 'Cody', 'Homenick', '362 Esta Parks', 'Apt. 390', 'New Gladys', 'GA', 'USA', '19852-6716', '(950)396-9024', '(685)168-8826', 317460867, '000000000', '1987-11-30', '0006465789', 'Y', 616),
(4, 'Delbert', 'Kaia', 'Parisian', '638 Blanda Gateway', 'Apt. 076', 'Lake Virginie', 'MI', 'USA', '39035-0455', '(801)603-4121', '(156)074-6837', 660354258, '000000000', '1985-01-13', '0040802739', 'Y', 776),
(5, 'Treva', 'Manley', 'Schowalter', '5653 Legros Plaza', 'Apt. 968', 'Alvinaport', 'MI', 'USA', '02251-1698', '(978)775-4633', '(439)943-7644', 611264288, '000000000', '1971-09-29', '0006365573', 'Y', 529);

-- Card Cross References (from CARDXREF VSAM, acct_ids aligned to seeded accounts)
INSERT INTO card_xref (card_num, cust_id, acct_id) VALUES
('0500024453765740', 5, 5),
('0683586198171516', 2, 2),
('0923877193247330', 2, 3),
('0927987108636232', 2, 2),
('0982496213629795', 1, 1);

-- Transactions (from DAILYTRAN VSAM)
INSERT INTO transactions (tran_id, tran_type_cd, tran_cat_cd, tran_source, tran_desc, tran_amt, merchant_id, merchant_name, merchant_city, merchant_zip, card_num, orig_ts, proc_ts) VALUES
('0000000000683580', '01', 1, 'POS TERM', 'Purchase at Abshire-Lowe', 504.78, 800000000, 'Abshire-Lowe', 'North Enoshaven', '72112', '4859452612877065', '2022-06-10 19:27:53.000000', NULL),
('0000000001774260', '03', 1, 'OPERATOR', 'Return item at Nitzsche, Nicolas and Lowe', -919.00, 800000000, 'Nitzsche, Nicolas and Lowe', 'Fidelshire', '53378', '0927987108636232', '2022-06-10 19:27:53.000000', NULL),
('0000000006292564', '01', 1, 'POS TERM', 'Purchase at Ernser, Roob and Gleason', 67.88, 800000000, 'Ernser, Roob and Gleason', 'North Makenziemouth', '78487-7965', '6009619150674526', '2022-06-10 19:27:53.000000', NULL),
('0000000009101861', '01', 1, 'POS TERM', 'Purchase at Guann LLC', 281.78, 800000000, 'Guann LLC', 'South Lynn', '51508-9166', '8040580410348680', '2022-06-10 19:27:53.000000', NULL),
('0000000010142252', '01', 1, 'POS TERM', 'Purchase at Kertzmann-Schoen', 454.65, 800000000, 'Kertzmann-Schoen', 'East Eulahstad', '98754-1089', '5656830544981216', '2022-06-10 19:27:53.000000', NULL);
