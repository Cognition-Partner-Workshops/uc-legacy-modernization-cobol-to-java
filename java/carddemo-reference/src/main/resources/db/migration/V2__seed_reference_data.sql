-- Seed reference data taken verbatim from the legacy CardDemo data files:
--   app/data/ASCII/trantype.txt  (TR_TYPE + TR_DESCRIPTION)
--   app/data/ASCII/trancatg.txt  (TRC_TYPE_CODE + TRC_TYPE_CATEGORY + TRC_CAT_DATA)

INSERT INTO TRANSACTION_TYPE (TR_TYPE, TR_DESCRIPTION) VALUES
    ('01', 'Purchase'),
    ('02', 'Payment'),
    ('03', 'Credit'),
    ('04', 'Authorization'),
    ('05', 'Refund'),
    ('06', 'Reversal'),
    ('07', 'Adjustment');

INSERT INTO TRANSACTION_TYPE_CATEGORY (TRC_TYPE_CODE, TRC_TYPE_CATEGORY, TRC_CAT_DATA) VALUES
    ('01', '0001', 'Regular Sales Draft'),
    ('01', '0002', 'Regular Cash Advance'),
    ('01', '0003', 'Convenience Check Debit'),
    ('01', '0004', 'ATM Cash Advance'),
    ('01', '0005', 'Interest Amount'),
    ('02', '0001', 'Cash payment'),
    ('02', '0002', 'Electronic payment'),
    ('02', '0003', 'Check payment'),
    ('03', '0001', 'Credit to Account'),
    ('03', '0002', 'Credit to Purchase balance'),
    ('03', '0003', 'Credit to Cash balance'),
    ('04', '0001', 'Zero dollar authorization'),
    ('04', '0002', 'Online purchase authorization'),
    ('04', '0003', 'Travel booking authorization'),
    ('05', '0001', 'Refund credit'),
    ('06', '0001', 'Fraud reversal'),
    ('06', '0002', 'Non-fraud reversal'),
    ('07', '0001', 'Sales draft credit adjustment');
