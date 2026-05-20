-- Seed data: Transaction types, categories, disclosure groups, and transactions

-- Transaction types (from COBOL CVTRA03Y.cpy)
INSERT INTO transaction.tran_types (tran_type, type_desc) VALUES
('01', 'Purchase'),
('02', 'Return/Refund'),
('03', 'Cash Advance'),
('04', 'Balance Transfer'),
('05', 'Payment'),
('06', 'Fee'),
('07', 'Interest Charge')
ON CONFLICT (tran_type) DO NOTHING;

-- Transaction categories
INSERT INTO transaction.tran_categories (type_cd, cat_cd, cat_desc) VALUES
('01', 1, 'Retail Purchase'),
('01', 2, 'Online Purchase'),
('01', 3, 'Recurring Payment'),
('02', 1, 'Merchandise Return'),
('03', 1, 'ATM Cash Advance'),
('03', 2, 'Counter Cash Advance'),
('05', 1, 'Standard Payment'),
('05', 2, 'Minimum Payment'),
('06', 1, 'Annual Fee'),
('06', 2, 'Late Payment Fee'),
('06', 3, 'Over Limit Fee'),
('07', 5, 'Monthly Interest')
ON CONFLICT (type_cd, cat_cd) DO NOTHING;

-- Disclosure groups (interest rates by account group and transaction type/category)
INSERT INTO transaction.disclosure_groups (acct_group_id, tran_type_cd, tran_cat_cd, int_rate) VALUES
('GRP001', '01', 1, 18.99),
('GRP001', '01', 2, 18.99),
('GRP001', '03', 1, 24.99),
('GRP001', '03', 2, 24.99),
('GRP002', '01', 1, 15.99),
('GRP002', '01', 2, 15.99),
('GRP002', '03', 1, 22.99),
('GRP003', '01', 1, 21.99),
('GRP003', '03', 1, 26.99)
ON CONFLICT (acct_group_id, tran_type_cd, tran_cat_cd) DO NOTHING;

-- Transaction category balances
INSERT INTO transaction.tran_cat_balances (acct_id, type_cd, cat_cd, balance) VALUES
('00000000001', '01', 1, 800.00),
('00000000001', '01', 2, 350.50),
('00000000001', '03', 1, 200.00),
('00000000002', '01', 1, 2000.00),
('00000000002', '01', 2, 800.00),
('00000000003', '01', 1, 150.00),
('00000000004', '01', 1, 4500.00),
('00000000004', '01', 2, 499.99),
('00000000007', '01', 1, 500.00),
('00000000007', '03', 1, 250.25),
('00000000008', '01', 1, 9000.00),
('00000000008', '03', 1, 800.00)
ON CONFLICT (acct_id, type_cd, cat_cd) DO NOTHING;

-- Historical transactions
INSERT INTO transaction.transactions (tran_id, type_cd, cat_cd, source, description, amount, merchant_id, merchant_name, merchant_city, merchant_zip, card_num, orig_ts, proc_ts) VALUES
('TXN0000000000001', '01', 1, 'POS', 'Grocery purchase', 125.50, 'M00000001', 'Fresh Foods Market', 'New York', '10001', '4111111111111111', '2024-12-01T10:30:00.000000', '2024-12-01T10:30:05.000000'),
('TXN0000000000002', '01', 2, 'ONLINE', 'Online electronics', 899.99, 'M00000002', 'Tech Store Online', 'San Jose', '95101', '4111111111111111', '2024-12-02T14:15:00.000000', '2024-12-02T14:15:03.000000'),
('TXN0000000000003', '03', 1, 'ATM', 'Cash advance ATM', 200.00, 'M00000003', 'National Bank ATM', 'New York', '10002', '4111111111111111', '2024-12-03T09:00:00.000000', '2024-12-03T09:00:02.000000'),
('TXN0000000000004', '05', 1, 'ONLINE', 'Monthly payment', -500.00, NULL, NULL, NULL, NULL, '4111111111111111', '2024-12-15T08:00:00.000000', '2024-12-15T08:00:01.000000'),
('TXN0000000000005', '01', 1, 'POS', 'Restaurant dinner', 85.30, 'M00000004', 'Downtown Grill', 'Los Angeles', '90001', '4222222222222222', '2024-12-01T19:30:00.000000', '2024-12-01T19:30:04.000000'),
('TXN0000000000006', '01', 2, 'ONLINE', 'Subscription service', 14.99, 'M00000005', 'StreamFlix', 'Seattle', '98101', '4222222222222222', '2024-12-01T00:01:00.000000', '2024-12-01T00:01:02.000000'),
('TXN0000000000007', '02', 1, 'POS', 'Return - defective item', -45.00, 'M00000002', 'Tech Store Online', 'San Jose', '95101', '4222222222222222', '2024-12-05T11:00:00.000000', '2024-12-05T11:00:03.000000'),
('TXN0000000000008', '01', 1, 'POS', 'Hardware store', 234.56, 'M00000006', 'Builder Supply Co', 'Dallas', '75001', '4333333333333333', '2024-12-10T15:45:00.000000', '2024-12-10T15:45:02.000000'),
('TXN0000000000009', '06', 1, 'SYSTEM', 'Annual fee', 95.00, NULL, NULL, NULL, NULL, '4444444444444444', '2024-12-01T00:00:00.000000', '2024-12-01T00:00:01.000000'),
('TXN0000000000010', '06', 2, 'SYSTEM', 'Late payment fee', 35.00, NULL, NULL, NULL, NULL, '4444444444444444', '2024-12-15T00:00:00.000000', '2024-12-15T00:00:01.000000'),
('TXN0000000000011', '01', 1, 'POS', 'Gas station', 55.20, 'M00000007', 'QuickFuel Express', 'Chicago', '60601', '4555555555555555', '2024-12-08T07:30:00.000000', '2024-12-08T07:30:03.000000'),
('TXN0000000000012', '01', 3, 'ONLINE', 'Gym membership', 49.99, 'M00000008', 'FitLife Gym', 'Chicago', '60602', '4555555555555555', '2024-12-01T06:00:00.000000', '2024-12-01T06:00:02.000000')
ON CONFLICT (tran_id) DO NOTHING;

-- Daily transactions (pending posting)
INSERT INTO transaction.daily_transactions (tran_id, type_cd, cat_cd, source, description, amount, merchant_id, merchant_name, merchant_city, merchant_zip, card_num, orig_ts) VALUES
('DLY0000000000001', '01', 1, 'POS', 'Pharmacy purchase', 32.50, 'M00000009', 'City Pharmacy', 'New York', '10001', '4111111111111111', '2024-12-20T12:00:00.000000'),
('DLY0000000000002', '01', 2, 'ONLINE', 'Book purchase', 24.99, 'M00000010', 'BookWorld Online', 'Portland', '97201', '4222222222222222', '2024-12-20T13:00:00.000000'),
('DLY0000000000003', '03', 1, 'ATM', 'Cash advance', 500.00, 'M00000003', 'National Bank ATM', 'Dallas', '75001', '4333333333333333', '2024-12-20T14:00:00.000000'),
('DLY0000000000004', '01', 1, 'POS', 'Over-limit purchase', 100.00, 'M00000011', 'Luxury Items', 'Miami', '33101', '4444444444444444', '2024-12-20T15:00:00.000000'),
('DLY0000000000005', '01', 1, 'POS', 'Expired account txn', 75.00, 'M00000012', 'Corner Store', 'Seattle', '98101', '4666666666666666', '2024-12-20T16:00:00.000000')
ON CONFLICT (tran_id) DO NOTHING;
