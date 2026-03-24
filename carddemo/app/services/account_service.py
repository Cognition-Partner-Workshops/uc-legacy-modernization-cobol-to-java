"""Account service — replaces COACTVWC.cbl and COACTUPC.cbl CICS programs.

COACTVWC: READ DATASET('ACCTDAT') → db.query(Account).filter_by()
COACTUPC: REWRITE DATASET('ACCTDAT') → db.merge() + db.commit()
STARTBR/READNEXT/ENDBR → paginated queries
"""

from sqlalchemy.orm import Session

from app.models.account import Account
from app.models.card_xref import CardXref
from app.schemas.account import AccountUpdate


def get_account(db: Session, acct_id: int) -> Account | None:
    """READ DATASET('ACCTDAT') RIDFLD(ACCT-ID) → filter_by().first()"""
    return db.query(Account).filter_by(acct_id=acct_id).first()


def get_account_by_card(db: Session, card_num: str) -> Account | None:
    """Cross-reference lookup: XREF → ACCOUNT (CBTRN02C 1500-B-LOOKUP-ACCT)."""
    xref = db.query(CardXref).filter_by(card_num=card_num).first()
    if not xref:
        return None
    return db.query(Account).filter_by(acct_id=xref.acct_id).first()


def list_accounts(db: Session, page: int = 1, page_size: int = 10) -> tuple[list[Account], int]:
    """STARTBR/READNEXT/ENDBR → paginated query."""
    total = db.query(Account).count()
    accounts = db.query(Account).order_by(Account.acct_id).offset((page - 1) * page_size).limit(page_size).all()
    return accounts, total


def update_account(db: Session, acct_id: int, update_data: AccountUpdate) -> Account | None:
    """REWRITE DATASET('ACCTDAT') → merge + commit."""
    account = db.query(Account).filter_by(acct_id=acct_id).first()
    if not account:
        return None
    for field, value in update_data.model_dump(exclude_unset=True).items():
        setattr(account, field, value)
    db.commit()
    db.refresh(account)
    return account
