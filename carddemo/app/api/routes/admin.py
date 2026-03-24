"""Admin menu route — from COADM01C.cbl (Admin Menu).

COADM01C: Admin menu screen that provides navigation to:
  - User management (COUSR00C-03C)
  - Transaction type management (COTRTLIC/COTRTUPC with DB2)

In the Python version, this is a simple status/menu endpoint.
"""

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

from app.api.dependencies import require_admin
from app.core.database import get_db
from app.models.account import Account
from app.models.card import Card
from app.models.customer import Customer
from app.models.transaction import Transaction
from app.models.user_security import UserSecurity

router = APIRouter(prefix="/admin", tags=["admin"])


@router.get("/dashboard")
def admin_dashboard(
    db: Session = Depends(get_db),
    _admin: UserSecurity = Depends(require_admin),
) -> dict:
    """Admin dashboard — replaces COADM01C admin menu screen.

    Provides summary statistics instead of a menu screen.
    """
    return {
        "total_accounts": db.query(Account).count(),
        "total_cards": db.query(Card).count(),
        "total_customers": db.query(Customer).count(),
        "total_transactions": db.query(Transaction).count(),
        "total_users": db.query(UserSecurity).count(),
        "menu_options": [
            {"code": "CU00", "label": "User Management", "endpoint": "/api/v1/users"},
            {"code": "CAVW", "label": "Account View", "endpoint": "/api/v1/accounts"},
            {"code": "CCLI", "label": "Card List", "endpoint": "/api/v1/cards"},
            {"code": "CT00", "label": "Transaction List", "endpoint": "/api/v1/transactions"},
            {"code": "CR00", "label": "Reports", "endpoint": "/api/v1/reports"},
        ],
    }
