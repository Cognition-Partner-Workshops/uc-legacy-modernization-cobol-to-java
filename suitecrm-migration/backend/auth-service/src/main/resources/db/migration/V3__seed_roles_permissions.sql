-- =====================================================
-- Seed Default Roles and Permissions
-- Migrated from SuiteCRM ACL Roles system
-- =====================================================

SET search_path TO auth_schema;

-- =====================================================
-- Insert Default Roles
-- =====================================================
INSERT INTO roles (id, name, description) VALUES
    (gen_random_uuid(), 'ROLE_ADMIN',     'System Administrator - Full access to all modules and configuration'),
    (gen_random_uuid(), 'ROLE_MANAGER',   'Manager - CRUD on team records, approve workflows'),
    (gen_random_uuid(), 'ROLE_SALES',     'Sales Representative - Full access to Sales modules'),
    (gen_random_uuid(), 'ROLE_SUPPORT',   'Support Agent - Full access to Service modules'),
    (gen_random_uuid(), 'ROLE_MARKETING', 'Marketing User - Full access to Marketing modules'),
    (gen_random_uuid(), 'ROLE_USER',      'Regular User - Standard CRM operations on own records'),
    (gen_random_uuid(), 'ROLE_VIEWER',    'Viewer - Read-only access across modules'),
    (gen_random_uuid(), 'ROLE_PORTAL',    'Portal User - Limited access to Cases and Knowledge Base');

-- =====================================================
-- Insert Module Permissions
-- Modules: accounts, contacts, leads, opportunities, quotes,
--          invoices, cases, bugs, knowledge, campaigns,
--          calls, meetings, tasks, emails, notes, documents,
--          reports, workflows, users, roles
-- Actions: access, create, read, update, delete, import, export, list
-- Access Levels: 0 = All, 1 = Owner only, -99 = None
-- =====================================================

-- Accounts module permissions
INSERT INTO permissions (id, module_name, action_name, access_level, description) VALUES
    (gen_random_uuid(), 'accounts', 'access', 0, 'Access Accounts module'),
    (gen_random_uuid(), 'accounts', 'create', 0, 'Create accounts'),
    (gen_random_uuid(), 'accounts', 'read', 0, 'View accounts'),
    (gen_random_uuid(), 'accounts', 'update', 0, 'Edit accounts'),
    (gen_random_uuid(), 'accounts', 'delete', 0, 'Delete accounts'),
    (gen_random_uuid(), 'accounts', 'import', 0, 'Import accounts'),
    (gen_random_uuid(), 'accounts', 'export', 0, 'Export accounts'),
    (gen_random_uuid(), 'accounts', 'list', 0, 'List accounts');

-- Contacts module permissions
INSERT INTO permissions (id, module_name, action_name, access_level, description) VALUES
    (gen_random_uuid(), 'contacts', 'access', 0, 'Access Contacts module'),
    (gen_random_uuid(), 'contacts', 'create', 0, 'Create contacts'),
    (gen_random_uuid(), 'contacts', 'read', 0, 'View contacts'),
    (gen_random_uuid(), 'contacts', 'update', 0, 'Edit contacts'),
    (gen_random_uuid(), 'contacts', 'delete', 0, 'Delete contacts'),
    (gen_random_uuid(), 'contacts', 'import', 0, 'Import contacts'),
    (gen_random_uuid(), 'contacts', 'export', 0, 'Export contacts'),
    (gen_random_uuid(), 'contacts', 'list', 0, 'List contacts');

-- Leads module permissions
INSERT INTO permissions (id, module_name, action_name, access_level, description) VALUES
    (gen_random_uuid(), 'leads', 'access', 0, 'Access Leads module'),
    (gen_random_uuid(), 'leads', 'create', 0, 'Create leads'),
    (gen_random_uuid(), 'leads', 'read', 0, 'View leads'),
    (gen_random_uuid(), 'leads', 'update', 0, 'Edit leads'),
    (gen_random_uuid(), 'leads', 'delete', 0, 'Delete leads'),
    (gen_random_uuid(), 'leads', 'import', 0, 'Import leads'),
    (gen_random_uuid(), 'leads', 'export', 0, 'Export leads'),
    (gen_random_uuid(), 'leads', 'list', 0, 'List leads');

-- Opportunities module permissions
INSERT INTO permissions (id, module_name, action_name, access_level, description) VALUES
    (gen_random_uuid(), 'opportunities', 'access', 0, 'Access Opportunities module'),
    (gen_random_uuid(), 'opportunities', 'create', 0, 'Create opportunities'),
    (gen_random_uuid(), 'opportunities', 'read', 0, 'View opportunities'),
    (gen_random_uuid(), 'opportunities', 'update', 0, 'Edit opportunities'),
    (gen_random_uuid(), 'opportunities', 'delete', 0, 'Delete opportunities'),
    (gen_random_uuid(), 'opportunities', 'import', 0, 'Import opportunities'),
    (gen_random_uuid(), 'opportunities', 'export', 0, 'Export opportunities'),
    (gen_random_uuid(), 'opportunities', 'list', 0, 'List opportunities');

-- Cases module permissions
INSERT INTO permissions (id, module_name, action_name, access_level, description) VALUES
    (gen_random_uuid(), 'cases', 'access', 0, 'Access Cases module'),
    (gen_random_uuid(), 'cases', 'create', 0, 'Create cases'),
    (gen_random_uuid(), 'cases', 'read', 0, 'View cases'),
    (gen_random_uuid(), 'cases', 'update', 0, 'Edit cases'),
    (gen_random_uuid(), 'cases', 'delete', 0, 'Delete cases'),
    (gen_random_uuid(), 'cases', 'import', 0, 'Import cases'),
    (gen_random_uuid(), 'cases', 'export', 0, 'Export cases'),
    (gen_random_uuid(), 'cases', 'list', 0, 'List cases');

-- Campaigns module permissions
INSERT INTO permissions (id, module_name, action_name, access_level, description) VALUES
    (gen_random_uuid(), 'campaigns', 'access', 0, 'Access Campaigns module'),
    (gen_random_uuid(), 'campaigns', 'create', 0, 'Create campaigns'),
    (gen_random_uuid(), 'campaigns', 'read', 0, 'View campaigns'),
    (gen_random_uuid(), 'campaigns', 'update', 0, 'Edit campaigns'),
    (gen_random_uuid(), 'campaigns', 'delete', 0, 'Delete campaigns'),
    (gen_random_uuid(), 'campaigns', 'import', 0, 'Import campaigns'),
    (gen_random_uuid(), 'campaigns', 'export', 0, 'Export campaigns'),
    (gen_random_uuid(), 'campaigns', 'list', 0, 'List campaigns');

-- Users module permissions
INSERT INTO permissions (id, module_name, action_name, access_level, description) VALUES
    (gen_random_uuid(), 'users', 'access', 0, 'Access Users module'),
    (gen_random_uuid(), 'users', 'create', 0, 'Create users'),
    (gen_random_uuid(), 'users', 'read', 0, 'View users'),
    (gen_random_uuid(), 'users', 'update', 0, 'Edit users'),
    (gen_random_uuid(), 'users', 'delete', 0, 'Delete users'),
    (gen_random_uuid(), 'users', 'list', 0, 'List users');

-- Reports module permissions
INSERT INTO permissions (id, module_name, action_name, access_level, description) VALUES
    (gen_random_uuid(), 'reports', 'access', 0, 'Access Reports module'),
    (gen_random_uuid(), 'reports', 'create', 0, 'Create reports'),
    (gen_random_uuid(), 'reports', 'read', 0, 'View reports'),
    (gen_random_uuid(), 'reports', 'update', 0, 'Edit reports'),
    (gen_random_uuid(), 'reports', 'delete', 0, 'Delete reports'),
    (gen_random_uuid(), 'reports', 'export', 0, 'Export reports'),
    (gen_random_uuid(), 'reports', 'list', 0, 'List reports');

-- Documents module permissions
INSERT INTO permissions (id, module_name, action_name, access_level, description) VALUES
    (gen_random_uuid(), 'documents', 'access', 0, 'Access Documents module'),
    (gen_random_uuid(), 'documents', 'create', 0, 'Create documents'),
    (gen_random_uuid(), 'documents', 'read', 0, 'View documents'),
    (gen_random_uuid(), 'documents', 'update', 0, 'Edit documents'),
    (gen_random_uuid(), 'documents', 'delete', 0, 'Delete documents'),
    (gen_random_uuid(), 'documents', 'list', 0, 'List documents');

-- =====================================================
-- Assign ALL permissions to ROLE_ADMIN
-- =====================================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ROLE_ADMIN';

-- =====================================================
-- Default Admin User (password: Admin@123)
-- BCrypt hash for 'Admin@123'
-- =====================================================
INSERT INTO users (id, username, email, password_hash, first_name, last_name, status, is_admin)
VALUES (
    gen_random_uuid(),
    'admin',
    'admin@suitecrm.local',
    '$2a$12$LJ3m4ys3uz0GHi0MzlTmYeg1yD0sbcFmJb4qKDMCRpFoNjS0Yx3d.',
    'System',
    'Administrator',
    'Active',
    TRUE
);

-- Assign ROLE_ADMIN to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- =====================================================
-- Default Security Groups
-- =====================================================
INSERT INTO security_groups (id, name, description) VALUES
    (gen_random_uuid(), 'Sales Team', 'Sales department security group'),
    (gen_random_uuid(), 'Support Team', 'Customer support security group'),
    (gen_random_uuid(), 'Marketing Team', 'Marketing department security group'),
    (gen_random_uuid(), 'Management', 'Management security group'),
    (gen_random_uuid(), 'All Users', 'Default security group for all users');
