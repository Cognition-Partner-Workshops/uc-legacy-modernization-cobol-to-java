"""Account routes — from COACTVWC.cbl (Account View) and COACTUPC.cbl (Account Update).

COACTVWC: READ ACCTDAT by ACCT-ID, SEND MAP with account details.
COACTUPC: RECEIVE MAP with changes, REWRITE ACCTDAT record.
"""

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.api.dependencies import get_current_user
from app.core.database import get_db
from app.models.user_security import UserSecurity
from app.schemas.account import AccountListResponse, AccountResponse, AccountUpdate
from app.services import account_service

router = APIRouter(prefix="/accounts", tags=["accounts"])


@router.get("", response_model=AccountListResponse)
def list_accounts(
    page: int = 1,
    page_size: int = 10,
    db: Session = Depends(get_db),
    _current_user: UserSecurity = Depends(get_current_user),
) -> AccountListResponse:
    """List accounts — replaces STARTBR/READNEXT/ENDBR on ACCTDAT."""
    accounts, total = account_service.list_accounts(db, page, page_size)
    return AccountListResponse(
        accounts=[AccountResponse.model_validate(a) for a in accounts],
        total=total,
        page=page,
        page_size=page_size,
    )


@router.get("/{acct_id}", response_model=AccountResponse)
def get_account(
    acct_id: int,
    db: Session = Depends(get_db),
    _current_user: UserSecurity = Depends(get_current_user),
) -> AccountResponse:
    """View account — replaces COACTVWC READ DATASET('ACCTDAT')."""
    account = account_service.get_account(db, acct_id)
    if not account:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Account not found")
    return AccountResponse.model_validate(account)


@router.put("/{acct_id}", response_model=AccountResponse)
def update_account(
    acct_id: int,
    update_data: AccountUpdate,
    db: Session = Depends(get_db),
    _current_user: UserSecurity = Depends(get_current_user),
) -> AccountResponse:
    """Update account — replaces COACTUPC REWRITE DATASET('ACCTDAT')."""
    account = account_service.update_account(db, acct_id, update_data)
    if not account:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Account not found")
    return AccountResponse.model_validate(account)
