#!/bin/bash
set -e

# Create multiple databases for microservices
# This script runs inside the PostgreSQL Docker container on initialization

DATABASES="suitecrm_auth suitecrm_contacts suitecrm_accounts suitecrm_opportunities suitecrm_cases suitecrm_campaigns suitecrm_activities suitecrm_reports suitecrm_documents suitecrm_workflows"

for DB in $DATABASES; do
    echo "Creating database: $DB"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
        SELECT 'CREATE DATABASE $DB' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '$DB')\gexec
        GRANT ALL PRIVILEGES ON DATABASE $DB TO $POSTGRES_USER;
EOSQL
done

echo "All databases created successfully."
