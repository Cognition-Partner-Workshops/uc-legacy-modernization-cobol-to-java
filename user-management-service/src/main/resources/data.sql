-- ==========================================================================
-- Sample seed data for the USERS table.
--
-- These records mirror the test users from the CardDemo USRSEC VSAM dataset
-- (see app/data/EBCDIC/AWS.M2.CARDDEMO.USRSEC.PS).
--
-- User types: ADMIN = system administrator, REGULAR = standard user
-- ==========================================================================
INSERT INTO users (user_id, first_name, last_name, password, user_type) VALUES
    ('ADMIN1',   'Admin',    'User',      'ADMIN1',   'ADMIN'),
    ('USER0001', 'John',     'Smith',     'PASS0001', 'REGULAR'),
    ('USER0002', 'Jane',     'Doe',       'PASS0002', 'REGULAR'),
    ('USER0003', 'Robert',   'Johnson',   'PASS0003', 'REGULAR'),
    ('USER0004', 'Maria',    'Garcia',    'PASS0004', 'REGULAR'),
    ('USER0005', 'James',    'Williams',  'PASS0005', 'REGULAR'),
    ('USER0006', 'Patricia', 'Brown',     'PASS0006', 'REGULAR'),
    ('USER0007', 'Michael',  'Jones',     'PASS0007', 'REGULAR'),
    ('USER0008', 'Linda',    'Davis',     'PASS0008', 'REGULAR'),
    ('USER0009', 'William',  'Miller',    'PASS0009', 'REGULAR'),
    ('USER0010', 'Barbara',  'Wilson',    'PASS0010', 'REGULAR'),
    ('USER0011', 'David',    'Anderson',  'PASS0011', 'REGULAR'),
    ('USER0012', 'Susan',    'Taylor',    'PASS0012', 'REGULAR');
