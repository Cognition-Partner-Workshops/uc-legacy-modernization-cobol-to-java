-- Seed data: Customers for CardDemo
INSERT INTO customer.customers (cust_id, first_name, middle_name, last_name, addr_line_1, addr_line_2, addr_state_cd, addr_country_cd, addr_zip, phone_num_1, ssn, govt_issued_id, dob, eft_account_id, pri_card_holder_ind, fico_credit_score) VALUES
('000000001', 'John', 'A', 'Smith', '123 Main Street', 'Apt 4B', 'NY', 'USA', '10001', '2125551234', '123456789', 'DL12345678', '1985-03-15', '1234567890', 'Y', 750),
('000000002', 'Jane', 'M', 'Doe', '456 Oak Avenue', NULL, 'CA', 'USA', '90210', '3105555678', '987654321', 'DL87654321', '1990-07-22', '0987654321', 'Y', 680),
('000000003', 'Robert', NULL, 'Williams', '789 Pine Road', 'Suite 100', 'TX', 'USA', '75001', '2145559012', '555123456', 'PP11223344', '1978-11-08', '5551234560', 'Y', 820),
('000000004', 'Emily', 'R', 'Brown', '321 Elm Street', NULL, 'FL', 'USA', '33101', '3055553456', '444556677', 'DL44556677', '1995-01-30', '4445566770', 'Y', 590),
('000000005', 'Michael', 'B', 'Davis', '654 Maple Drive', 'Unit 12', 'IL', 'USA', '60601', '3125557890', '111223344', 'DL11223344', '1982-09-12', '1112233440', 'Y', 710),
('000000006', 'Jessica', NULL, 'Wilson', '987 Cedar Lane', NULL, 'WA', 'USA', '98101', '2065551111', '222334455', 'DL22334455', '1988-05-25', '2223344550', 'N', 640),
('000000007', 'David', 'C', 'Taylor', '147 Birch Court', 'Floor 3', 'MA', 'USA', '02101', '6175552222', '333445566', 'PP33445566', '1975-12-01', '3334455660', 'Y', 780),
('000000008', 'Amanda', NULL, 'Martinez', '258 Walnut Blvd', NULL, 'AZ', 'USA', '85001', '6025553333', '444556678', 'DL44556678', '1992-08-18', '4445566780', 'Y', 550)
ON CONFLICT (cust_id) DO NOTHING;
