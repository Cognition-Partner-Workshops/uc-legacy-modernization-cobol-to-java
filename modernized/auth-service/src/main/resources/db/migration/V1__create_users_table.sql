CREATE TABLE auth.users (
    user_id       VARCHAR(8) PRIMARY KEY,
    first_name    VARCHAR(20) NOT NULL,
    last_name     VARCHAR(20) NOT NULL,
    password_hash VARCHAR(72) NOT NULL,
    user_type     CHAR(1) NOT NULL DEFAULT 'U',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
