"""Transaction routes — from COTRN00C (List), COTRN01C (View), COTRN02C (Add).

COTRN00C: STARTBR/READNEXT/ENDBR on TRANSACT with pagination.
COTRN01C: READ DATASET('TRANSACT') by TRAN-ID.
COTRN02C: Generate TRAN-ID, WRITE DATASET('TRANSACT'), update ACCTDAT + TCATBALF.
"""

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.api.dependencies import get_current_user
from app.core.database import get_db
from app.models.user_security import UserSecurity
from app.schemas.transaction import TransactionCreate, TransactionListResponse, TransactionResponse
from app.services import transaction_service

router = APIRouter(prefix="/transactions", tags=["transactions"])


@router.get("", response_model=TransactionListResponse)
def list_transactions(
    card_num: str | None = None,
    acct_id: int | None = None,
    page: int = 1,
    page_size: int = 10,
    db: Session = Depends(get_db),
    _current_user: UserSecurity = Depends(get_current_user),
) -> TransactionListResponse:
    """List transactions — replaces COTRN00C STARTBR/READNEXT/ENDBR."""
    transactions, total = transaction_service.list_transactions(db, card_num, acct_id, page, page_size)
    return TransactionListResponse(
        transactions=[TransactionResponse.model_validate(t) for t in transactions],
        total=total,
        page=page,
        page_size=page_size,
    )


@router.get("/{tran_id}", response_model=TransactionResponse)
def get_transaction(
    tran_id: str,
    db: Session = Depends(get_db),
    _current_user: UserSecurity = Depends(get_current_user),
) -> TransactionResponse:
    """View transaction — replaces COTRN01C READ DATASET('TRANSACT')."""
    transaction = transaction_service.get_transaction(db, tran_id)
    if not transaction:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Transaction not found")
    return TransactionResponse.model_validate(transaction)


@router.post("", response_model=TransactionResponse, status_code=status.HTTP_201_CREATED)
def add_transaction(
    data: TransactionCreate,
    db: Session = Depends(get_db),
    _current_user: UserSecurity = Depends(get_current_user),
) -> TransactionResponse:
    """Add transaction — replaces COTRN02C WRITE DATASET('TRANSACT')."""
    transaction = transaction_service.add_transaction(db, data)
    return TransactionResponse.model_validate(transaction)
