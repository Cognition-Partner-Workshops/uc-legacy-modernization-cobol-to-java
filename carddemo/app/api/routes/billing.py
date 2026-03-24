"""Billing route — from COBIL00C.cbl (Bill Payment).

COBIL00C: Processes bill payments by reducing account balance.
RECEIVE MAP → get payment amount
READ ACCTDAT → get current balance
Subtract payment from balance
REWRITE ACCTDAT → update balance
"""

from decimal import Decimal

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.api.dependencies import get_current_user
from app.core.database import get_db
from app.models.account import Account
from app.models.user_security import UserSecurity
from app.schemas.billing import BillPaymentRequest, BillPaymentResponse

router = APIRouter(prefix="/billing", tags=["billing"])


@router.post("/pay", response_model=BillPaymentResponse)
def pay_bill(
    request: BillPaymentRequest,
    db: Session = Depends(get_db),
    _current_user: UserSecurity = Depends(get_current_user),
) -> BillPaymentResponse:
    """Process bill payment — replaces COBIL00C payment processing."""
    account = db.query(Account).filter_by(acct_id=request.acct_id).first()
    if not account:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Account not found")

    if request.payment_amount <= 0:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail="Payment amount must be positive")

    account.curr_bal = Decimal(str(account.curr_bal)) - request.payment_amount
    db.commit()
    db.refresh(account)

    return BillPaymentResponse(
        acct_id=account.acct_id,
        payment_amount=str(request.payment_amount),
        new_balance=str(account.curr_bal),
        message="Payment processed successfully",
    )
