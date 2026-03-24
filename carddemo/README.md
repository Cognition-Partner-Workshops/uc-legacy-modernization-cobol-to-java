# CardDemo — Python/FastAPI Migration

Credit Card Management System migrated from COBOL/CICS/VSAM to Python/FastAPI/PostgreSQL.

## Architecture

| COBOL Component | Python Replacement |
|---|---|
| CICS transactions | FastAPI REST endpoints |
| VSAM KSDS files | PostgreSQL tables (SQLAlchemy ORM) |
| COBOL copybooks | SQLAlchemy models + Pydantic schemas |
| JCL batch jobs | Celery tasks + CLI commands |
| BMS maps | JSON request/response |
| RACF security | JWT authentication (bcrypt) |
| IMS DL/I | SQLAlchemy queries |
| COMMAREA | JWT claims |

## Quick Start

```bash
# Start all services
docker compose up -d

# Run migrations
docker compose exec app alembic upgrade head

# Seed default users
curl -X POST http://localhost:8000/api/v1/auth/seed-admin

# Load sample data
docker compose exec app python -m app.batch.data_loader
```

## API Endpoints

| Endpoint | Method | COBOL Source | Description |
|---|---|---|---|
| `/api/v1/auth/login` | POST | COSGN00C | User login |
| `/api/v1/accounts` | GET | COACTVWC | List accounts |
| `/api/v1/accounts/{id}` | GET/PUT | COACTVWC/COACTUPC | View/update account |
| `/api/v1/cards` | GET | COCRDLIC | List cards |
| `/api/v1/cards/{num}` | GET/PUT | COCRDSLC/COCRDUPC | View/update card |
| `/api/v1/transactions` | GET/POST | COTRN00C/COTRN02C | List/add transactions |
| `/api/v1/transactions/{id}` | GET | COTRN01C | View transaction |
| `/api/v1/reports/transactions` | POST | CORPT00C | Transaction report |
| `/api/v1/billing/pay` | POST | COBIL00C | Bill payment |
| `/api/v1/users` | CRUD | COUSR00C-03C | User management (admin) |
| `/api/v1/admin/dashboard` | GET | COADM01C | Admin dashboard |
| `/api/v1/authorizations` | GET/POST | COPAUS0C/COPAUA0C | Pending authorizations |

## Development

```bash
# Install dependencies
pip install -e ".[dev]"

# Run tests
pytest tests/ -v --cov=app

# Lint
ruff check .
ruff format --check .

# Start dev server
uvicorn app.main:app --reload
```

## Project Structure

```
carddemo/
├── app/
│   ├── api/routes/      # FastAPI route handlers (from CICS programs)
│   ├── models/          # SQLAlchemy models (from COBOL copybooks)
│   ├── schemas/         # Pydantic v2 request/response schemas
│   ├── services/        # Business logic (preserved from COBOL)
│   ├── batch/           # Batch jobs (from JCL/COBOL batch programs)
│   └── core/            # Config, security, database setup
├── alembic/             # Database migrations
├── templates/           # Jinja2 templates for HTML statements
└── tests/               # pytest test suite
```

## Key Business Logic Preserved

- **Interest Calculation** (CBACT04C): `monthly_interest = (balance * rate) / 1200`
- **Authorization Decision** (COPAUA0C): Card/account validation, credit limit, expiration checks
- **Transaction Posting** (CBTRN02C): XREF lookup, account validation, balance updates
- **Date Validation** (CSUTLDTC): Century, month, day, leap year validation
- **Role-Based Access**: Admin ('A') = full access, User ('U') = own account only

## Data Types

All monetary fields use `decimal.Decimal` (never `float`) to match COBOL COMP-3 precision.
