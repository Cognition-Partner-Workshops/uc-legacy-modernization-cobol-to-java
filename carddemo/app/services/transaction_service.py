"""Transaction service — replaces COTRN00C/01C/02C CICS programs.

COTRN00C: STARTBR/READNEXT/ENDBR on TRANSACT → paginated queries
COTRN01C: READ DATASET('TRANSACT') → filter_by().first()
COTRN02C: Transaction add with auto-generated ID (read last record, increment)
"""

from datetime import UTC, datetime
from decimal import Decimal

from sqlalchemy import func
from sqlalchemy.orm import Session

from app.models.account import Account
from app.models.card_xref import CardXref
from app.models.tran_category_balance import TranCategoryBalance
from app.models.transaction import Transaction
from app.schemas.transaction import TransactionCreate


def get_transaction(db: Session, tran_id: str) -> Transaction | None:
    """READ DATASET('TRANSACT') RIDFLD(TRAN-ID) → filter_by().first()"""
    return db.query(Transaction).filter_by(tran_id=tran_id).first()


def list_transactions(
    db: Session,
    card_num: str | None = None,
    acct_id: int | None = None,
    page: int = 1,
    page_size: int = 10,
) -> tuple[list[Transaction], int]:
    """STARTBR/READNEXT/ENDBR on TRANSACT → paginated query."""
    query = db.query(Transaction)
    if card_num:
        query = query.filter_by(card_num=card_num)
    if acct_id:
        xrefs = db.query(CardXref.card_num).filter_by(acct_id=acct_id).subquery()
        query = query.filter(Transaction.card_num.in_(xrefs))
    total = query.count()
    transactions = query.order_by(Transaction.tran_id.desc()).offset((page - 1) * page_size).limit(page_size).all()
    return transactions, total


def _generate_tran_id(db: Session) -> str:
    """Auto-generate transaction ID by reading last record and incrementing.

    From COTRN02C.cbl: reads the last TRANSACT record key and increments.
    Format: 16-char zero-padded string.
    """
    last = db.query(func.max(Transaction.tran_id)).scalar()
    if last:
        try:
            next_num = int(last) + 1
        except ValueError:
            next_num = 1
    else:
        next_num = 1
    return str(next_num).zfill(16)


def add_transaction(db: Session, data: TransactionCreate) -> Transaction:
    """Add a new transaction — replaces COTRN02C WRITE DATASET('TRANSACT').

    Also updates account balances (2800-UPDATE-ACCOUNT-REC) and
    transaction category balances (2700-UPDATE-TCATBAL).
    """
    now = datetime.now(UTC).strftime("%Y-%m-%d-%H.%M.%S.%f")[:26]
    tran_id = _generate_tran_id(db)

    transaction = Transaction(
        tran_id=tran_id,
        tran_type_cd=data.tran_type_cd,
        tran_cat_cd=data.tran_cat_cd,
        tran_source=data.tran_source or "Online",
        tran_desc=data.tran_desc,
        tran_amt=data.tran_amt,
        merchant_id=data.merchant_id,
        merchant_name=data.merchant_name,
        merchant_city=data.merchant_city,
        merchant_zip=data.merchant_zip,
        card_num=data.card_num,
        orig_ts=now,
        proc_ts=now,
    )
    db.add(transaction)

    # Update account balance (from CBTRN02C 2800-UPDATE-ACCOUNT-REC)
    xref = db.query(CardXref).filter_by(card_num=data.card_num).first()
    if xref:
        account = db.query(Account).filter_by(acct_id=xref.acct_id).first()
        if account:
            account.curr_bal = Decimal(str(account.curr_bal)) + data.tran_amt
            if data.tran_amt >= 0:
                account.curr_cyc_credit = Decimal(str(account.curr_cyc_credit)) + data.tran_amt
            else:
                account.curr_cyc_debit = Decimal(str(account.curr_cyc_debit)) + data.tran_amt

        # Update transaction category balance (from CBTRN02C 2700-UPDATE-TCATBAL)
        tcat = (
            db.query(TranCategoryBalance)
            .filter_by(acct_id=xref.acct_id, type_cd=data.tran_type_cd, cat_cd=data.tran_cat_cd)
            .first()
        )
        if tcat:
            tcat.balance = Decimal(str(tcat.balance)) + data.tran_amt
        else:
            tcat = TranCategoryBalance(
                acct_id=xref.acct_id,
                type_cd=data.tran_type_cd,
                cat_cd=data.tran_cat_cd,
                balance=data.tran_amt,
            )
            db.add(tcat)

    db.commit()
    db.refresh(transaction)
    return transaction
