-- =====================================================
-- SuiteCRM Auth Service - Aurora PostgreSQL Schema
-- Migrated from MySQL/MariaDB SuiteCRM ACL system
-- =====================================================

CREATE SCHEMA IF NOT EXISTS auth_schema;
SET search_path TO auth_schema;

-- =====================================================
-- Users Table (migrated from SuiteCRM users table)
-- =====================================================
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username        VARCHAR(60) NOT NULL UNIQUE,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    phone_work      VARCHAR(50),
    phone_mobile    VARCHAR(50),
    title           VARCHAR(100),
    department      VARCHAR(100),
    status          VARCHAR(20) NOT NULL DEFAULT 'Active',
    is_admin        BOOLEAN NOT NULL DEFAULT FALSE,
    avatar_url      VARCHAR(500),
    timezone        VARCHAR(50) DEFAULT 'UTC',
    language        VARCHAR(10) DEFAULT 'en_US',
    date_entered    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login      TIMESTAMP,
    failed_login_attempts INTEGER DEFAULT 0,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT chk_user_status CHECK (status IN ('Active', 'Inactive', 'Locked'))
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_deleted ON users(deleted);

-- =====================================================
-- Roles Table (migrated from SuiteCRM acl_roles)
-- =====================================================
CREATE TABLE roles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL UNIQUE,
    description     VARCHAR(500),
    date_entered    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

-- =====================================================
-- Permissions Table (migrated from SuiteCRM acl_actions)
-- Module-level CRUD + special operations
-- =====================================================
CREATE TABLE permissions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    module_name     VARCHAR(100) NOT NULL,
    action_name     VARCHAR(50) NOT NULL,
    access_level    INTEGER NOT NULL DEFAULT 0,
    description     VARCHAR(255),

    CONSTRAINT uq_permission_module_action UNIQUE (module_name, action_name),
    CONSTRAINT chk_action_name CHECK (action_name IN (
        'access', 'create', 'read', 'update', 'delete',
        'import', 'export', 'list', 'massupdate', 'approve'
    )),
    CONSTRAINT chk_access_level CHECK (access_level IN (-99, -1, 0, 1))
);

CREATE INDEX idx_permissions_module ON permissions(module_name);

-- =====================================================
-- Security Groups (migrated from SuiteCRM securitygroups)
-- =====================================================
CREATE TABLE security_groups (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(100) NOT NULL UNIQUE,
    description     VARCHAR(500),
    noninheritable  BOOLEAN DEFAULT FALSE,
    date_entered    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_modified   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted         BOOLEAN NOT NULL DEFAULT FALSE
);

-- =====================================================
-- Junction Tables
-- =====================================================

-- User <-> Role (M:N)
CREATE TABLE user_roles (
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id         UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    date_assigned   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_user_roles_user ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role_id);

-- Role <-> Permission (M:N)
CREATE TABLE role_permissions (
    role_id         UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id   UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX idx_role_permissions_role ON role_permissions(role_id);

-- Security Group <-> User (M:N)
CREATE TABLE security_group_users (
    security_group_id UUID NOT NULL REFERENCES security_groups(id) ON DELETE CASCADE,
    user_id           UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    noninheritable    BOOLEAN DEFAULT FALSE,
    primary_group     BOOLEAN DEFAULT FALSE,
    date_assigned     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (security_group_id, user_id)
);

CREATE INDEX idx_sg_users_sg ON security_group_users(security_group_id);
CREATE INDEX idx_sg_users_user ON security_group_users(user_id);

-- =====================================================
-- Audit Log
-- =====================================================
CREATE TABLE auth_audit_log (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID REFERENCES users(id),
    action          VARCHAR(50) NOT NULL,
    resource_type   VARCHAR(50),
    resource_id     UUID,
    details         JSONB,
    ip_address      VARCHAR(45),
    user_agent      VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_user ON auth_audit_log(user_id);
CREATE INDEX idx_audit_action ON auth_audit_log(action);
CREATE INDEX idx_audit_created ON auth_audit_log(created_at);
