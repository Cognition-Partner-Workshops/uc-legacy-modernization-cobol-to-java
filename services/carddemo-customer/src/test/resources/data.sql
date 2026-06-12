-- Test seed data matching COBOL CUSTOMER-RECORD and CARD-XREF-RECORD structures

INSERT INTO customers (cust_id, first_name, middle_name, last_name,
    address_line_1, address_line_2, address_line_3, state_code, country_code, zip,
    phone_number_1, phone_number_2, ssn, govt_issued_id, date_of_birth,
    eft_account_id, primary_card_holder_ind, fico_credit_score)
VALUES
    (1, 'John', 'Michael', 'Smith', '123 Main St', 'Apt 4B', '', 'NY', 'USA', '10001',
     '212-555-0100', '212-555-0101', '123456789', 'DL-NY-12345678', '1985-03-15',
     'EFT0000001', 'Y', 750),
    (2, 'Jane', 'Elizabeth', 'Doe', '456 Oak Ave', '', '', 'CA', 'USA', '90210',
     '310-555-0200', '', '987654321', 'DL-CA-87654321', '1990-07-22',
     'EFT0000002', 'Y', 680),
    (3, 'Robert', '', 'Johnson', '789 Pine Rd', 'Suite 100', '', 'TX', 'USA', '75001',
     '214-555-0300', '214-555-0301', '111223333', 'PP-US-11223344', '1978-11-08',
     'EFT0000003', 'N', 720);

INSERT INTO card_xref (card_number, customer_id, account_id)
VALUES
    ('4111111111111111', 1, 10000000001),
    ('4222222222222222', 1, 10000000001),
    ('5333333333333333', 2, 20000000002),
    ('5444444444444444', 3, 30000000003),
    ('5555555555555555', 3, 30000000004);
