-- V2: Add ACL roles, ACL actions, employees, user preferences, OAuth tokens
-- Maps to AS-IS SuiteCRM modules: ACLRoles, ACLActions, Employees, UserPreferences, OAuthTokens

CREATE TABLE IF NOT EXISTS auth_schema.acl_roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    is_admin BOOLEAN DEFAULT FALSE,
    created_by UUID,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS auth_schema.acl_actions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,
    acltype VARCHAR(100) DEFAULT 'module',
    aclaccess INTEGER,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS auth_schema.acl_roles_actions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL REFERENCES auth_schema.acl_roles(id),
    action_id UUID NOT NULL REFERENCES auth_schema.acl_actions(id),
    access_override INTEGER,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS auth_schema.acl_roles_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id UUID NOT NULL REFERENCES auth_schema.acl_roles(id),
    user_id UUID NOT NULL REFERENCES auth_schema.users(id),
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS auth_schema.employees (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth_schema.users(id),
    first_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    title VARCHAR(100),
    department VARCHAR(100),
    phone_work VARCHAR(50),
    phone_mobile VARCHAR(50),
    phone_fax VARCHAR(50),
    email VARCHAR(255),
    address_street VARCHAR(255),
    address_city VARCHAR(100),
    address_state VARCHAR(100),
    address_postal_code VARCHAR(20),
    address_country VARCHAR(100),
    reports_to_id UUID REFERENCES auth_schema.employees(id),
    employee_status VARCHAR(50) DEFAULT 'Active',
    messenger_type VARCHAR(50),
    messenger_id VARCHAR(100),
    description TEXT,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS auth_schema.user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth_schema.users(id),
    category VARCHAR(100) DEFAULT 'global',
    preference_key VARCHAR(255) NOT NULL,
    preference_value TEXT,
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS auth_schema.oauth_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES auth_schema.users(id),
    token_type VARCHAR(50),
    access_token TEXT,
    refresh_token TEXT,
    client_id VARCHAR(255),
    expires_at TIMESTAMP,
    scope VARCHAR(500),
    date_entered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS auth_schema.security_groups_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    security_group_id UUID NOT NULL REFERENCES auth_schema.security_groups(id),
    user_id UUID NOT NULL REFERENCES auth_schema.users(id),
    noninheritable BOOLEAN DEFAULT FALSE,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS auth_schema.security_groups_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    security_group_id UUID NOT NULL REFERENCES auth_schema.security_groups(id),
    record_id UUID NOT NULL,
    module VARCHAR(100) NOT NULL,
    date_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted BOOLEAN DEFAULT FALSE
);

-- Indexes
CREATE INDEX idx_acl_roles_name ON auth_schema.acl_roles(name);
CREATE INDEX idx_acl_actions_category ON auth_schema.acl_actions(category);
CREATE INDEX idx_acl_roles_actions_role ON auth_schema.acl_roles_actions(role_id);
CREATE INDEX idx_acl_roles_users_role ON auth_schema.acl_roles_users(role_id);
CREATE INDEX idx_acl_roles_users_user ON auth_schema.acl_roles_users(user_id);
CREATE INDEX idx_employees_user_id ON auth_schema.employees(user_id);
CREATE INDEX idx_employees_department ON auth_schema.employees(department);
CREATE INDEX idx_employees_reports_to ON auth_schema.employees(reports_to_id);
CREATE INDEX idx_employees_status ON auth_schema.employees(employee_status);
CREATE INDEX idx_user_preferences_user ON auth_schema.user_preferences(user_id);
CREATE INDEX idx_user_preferences_key ON auth_schema.user_preferences(preference_key);
CREATE INDEX idx_oauth_tokens_user ON auth_schema.oauth_tokens(user_id);
CREATE INDEX idx_oauth_tokens_expires ON auth_schema.oauth_tokens(expires_at);
CREATE INDEX idx_sg_users_sg ON auth_schema.security_groups_users(security_group_id);
CREATE INDEX idx_sg_users_user ON auth_schema.security_groups_users(user_id);
CREATE INDEX idx_sg_records_sg ON auth_schema.security_groups_records(security_group_id);
CREATE INDEX idx_sg_records_record ON auth_schema.security_groups_records(record_id, module);

-- Seed default ACL actions for standard modules
INSERT INTO auth_schema.acl_actions (name, category, acltype, aclaccess) VALUES
    ('access', 'Accounts', 'module', 89),
    ('view', 'Accounts', 'module', 90),
    ('list', 'Accounts', 'module', 90),
    ('edit', 'Accounts', 'module', 90),
    ('delete', 'Accounts', 'module', 90),
    ('import', 'Accounts', 'module', 90),
    ('export', 'Accounts', 'module', 90),
    ('access', 'Contacts', 'module', 89),
    ('view', 'Contacts', 'module', 90),
    ('list', 'Contacts', 'module', 90),
    ('edit', 'Contacts', 'module', 90),
    ('delete', 'Contacts', 'module', 90),
    ('import', 'Contacts', 'module', 90),
    ('export', 'Contacts', 'module', 90),
    ('access', 'Opportunities', 'module', 89),
    ('view', 'Opportunities', 'module', 90),
    ('list', 'Opportunities', 'module', 90),
    ('edit', 'Opportunities', 'module', 90),
    ('delete', 'Opportunities', 'module', 90),
    ('access', 'Cases', 'module', 89),
    ('view', 'Cases', 'module', 90),
    ('list', 'Cases', 'module', 90),
    ('edit', 'Cases', 'module', 90),
    ('delete', 'Cases', 'module', 90),
    ('access', 'Campaigns', 'module', 89),
    ('view', 'Campaigns', 'module', 90),
    ('list', 'Campaigns', 'module', 90),
    ('edit', 'Campaigns', 'module', 90),
    ('delete', 'Campaigns', 'module', 90),
    ('access', 'Activities', 'module', 89),
    ('view', 'Activities', 'module', 90),
    ('list', 'Activities', 'module', 90),
    ('edit', 'Activities', 'module', 90),
    ('delete', 'Activities', 'module', 90),
    ('access', 'Documents', 'module', 89),
    ('view', 'Documents', 'module', 90),
    ('list', 'Documents', 'module', 90),
    ('edit', 'Documents', 'module', 90),
    ('delete', 'Documents', 'module', 90),
    ('access', 'Reports', 'module', 89),
    ('view', 'Reports', 'module', 90),
    ('list', 'Reports', 'module', 90),
    ('edit', 'Reports', 'module', 90),
    ('delete', 'Reports', 'module', 90),
    ('access', 'Projects', 'module', 89),
    ('view', 'Projects', 'module', 90),
    ('list', 'Projects', 'module', 90),
    ('edit', 'Projects', 'module', 90),
    ('delete', 'Projects', 'module', 90);

-- Seed default ACL roles
INSERT INTO auth_schema.acl_roles (name, description, is_admin) VALUES
    ('Administrator', 'Full system access', TRUE),
    ('Sales Manager', 'Access to sales modules with management capabilities', FALSE),
    ('Sales Representative', 'Access to core sales modules', FALSE),
    ('Marketing Manager', 'Access to marketing and campaign modules', FALSE),
    ('Support Manager', 'Access to cases and support modules', FALSE),
    ('Support Agent', 'Limited access to cases and knowledge base', FALSE),
    ('Project Manager', 'Access to project management modules', FALSE),
    ('Read Only', 'View-only access to all modules', FALSE);
