"""FastAPI application entry point — replaces CICS DFHPCT/DFHFCT definitions.

The COBOL CardDemo application uses CICS Program Control Table (PCT) entries
to route transaction IDs to programs. This module replaces that with FastAPI
route registration.

CICS Transaction → FastAPI Route mapping:
  SGNC (COSGN00C) → /api/v1/auth/login
  CAVW (COACTVWC) → /api/v1/accounts/{id}
  CAUP (COACTUPC) → /api/v1/accounts/{id} PUT
  CCLI (COCRDLIC) → /api/v1/cards
  CCSL (COCRDSLC) → /api/v1/cards/{num}
  CCUP (COCRDUPC) → /api/v1/cards/{num} PUT
  CT00 (COTRN00C) → /api/v1/transactions
  CT01 (COTRN01C) → /api/v1/transactions/{id}
  CT02 (COTRN02C) → /api/v1/transactions POST
  CR00 (CORPT00C) → /api/v1/reports/transactions
  CBIL (COBIL00C) → /api/v1/billing/pay
  CU00-03 (COUSR)  → /api/v1/users
  ADM1 (COADM01C) → /api/v1/admin/dashboard
  PAUS (COPAUS0C) → /api/v1/authorizations
"""

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.routes import (
    accounts,
    admin,
    auth,
    authorizations,
    billing,
    cards,
    reports,
    transactions,
    users,
)

app = FastAPI(
    title="CardDemo API",
    description="Credit Card Management System — migrated from COBOL/CICS to Python/FastAPI",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Register route modules (replaces CICS PCT entries)
app.include_router(auth.router, prefix="/api/v1")
app.include_router(accounts.router, prefix="/api/v1")
app.include_router(cards.router, prefix="/api/v1")
app.include_router(transactions.router, prefix="/api/v1")
app.include_router(reports.router, prefix="/api/v1")
app.include_router(billing.router, prefix="/api/v1")
app.include_router(users.router, prefix="/api/v1")
app.include_router(admin.router, prefix="/api/v1")
app.include_router(authorizations.router, prefix="/api/v1")


@app.get("/health")
def health_check() -> dict:
    return {"status": "healthy", "application": "CardDemo", "version": "1.0.0"}
