-- Seed data: Auth users for CardDemo
-- Passwords are bcrypt-hashed value of 'password' ($2a$10$...)
-- Admin users (user_type = 'A') and regular users (user_type = 'U')

INSERT INTO auth.users (user_id, first_name, last_name, password_hash, user_type) VALUES
('admin01', 'System', 'Admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBfjMGhI04.GElUcel5Sv6sK26S', 'A'),
('admin02', 'Sarah', 'Johnson', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBfjMGhI04.GElUcel5Sv6sK26S', 'A'),
('user0001', 'John', 'Smith', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBfjMGhI04.GElUcel5Sv6sK26S', 'U'),
('user0002', 'Jane', 'Doe', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBfjMGhI04.GElUcel5Sv6sK26S', 'U'),
('user0003', 'Robert', 'Williams', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBfjMGhI04.GElUcel5Sv6sK26S', 'U'),
('user0004', 'Emily', 'Brown', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBfjMGhI04.GElUcel5Sv6sK26S', 'U'),
('user0005', 'Michael', 'Davis', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjqQBfjMGhI04.GElUcel5Sv6sK26S', 'U')
ON CONFLICT (user_id) DO NOTHING;
