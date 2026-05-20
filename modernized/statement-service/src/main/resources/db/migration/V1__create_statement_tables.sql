CREATE TABLE statement.statements (
    id SERIAL PRIMARY KEY,
    acct_id VARCHAR(11) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    file_path VARCHAR(500),
    html_path VARCHAR(500),
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
