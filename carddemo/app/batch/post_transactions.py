"""Post transactions batch — from CBTRN02C.cbl.

CBTRN02C reads daily transaction file sequentially:
  1000-PROCESS-DALYTRAN-FILE: Main loop reading DALYTRAN-FILE
  1500-VALIDATE-TRAN: Validates each transaction
    - 1500-A-LOOKUP-XREF: Look up card in XREF file
    - 1500-B-LOOKUP-ACCT: Look up account, check credit limit, check expiration
  2000-POST-TRANSACTION: Posts valid transactions
    - 2700-UPDATE-TCATBAL: Update transaction category balances
    - 2800-UPDATE-ACCOUNT-REC: Update account balances
    - 2900-WRITE-TRANSACTION-FILE: Write to transaction file
  2500-WRITE-REJECT-REC: Write rejected transactions with reason
"""

from datetime import UTC, datetime
from decimal import Decimal

from sqlalchemy.orm import Session

from app.models.account import Account
from app.models.card_xref import CardXref
from app.models.daily_transaction import DailyTransaction
from app.models.tran_category_balance import TranCategoryBalance
from app.models.transaction import Transaction


def validate_transaction(db: Session, daily_tran: DailyTransaction) -> tuple[bool, int, str]:
    """Validate a daily transaction — from CBTRN02C 1500-VALIDATE-TRAN.

    Returns (is_valid, fail_reason_code, fail_reason_desc).
    """
    # 1500-A-LOOKUP-XREF: Look up card in cross-reference
    xref = db.query(CardXref).filter_by(card_num=daily_tran.card_num).first()
    if not xref:
        return False, 100, "INVALID CARD NUMBER FOUND"

    # 1500-B-LOOKUP-ACCT: Look up account
    account = db.query(Account).filter_by(acct_id=xref.acct_id).first()
    if not account:
        return False, 101, "ACCOUNT RECORD NOT FOUND"

    # Credit limit check
    temp_bal = (
        Decimal(str(account.curr_cyc_credit)) - Decimal(str(account.curr_cyc_debit)) + Decimal(str(daily_tran.tran_amt))
    )
    if Decimal(str(account.credit_limit)) < temp_bal:
        return False, 102, "OVERLIMIT TRANSACTION"

    # Expiration date check
    orig_date = daily_tran.orig_ts[:10] if daily_tran.orig_ts else ""
    if account.expiration_date and orig_date and account.expiration_date < orig_date:
        return False, 103, "TRANSACTION RECEIVED AFTER ACCT EXPIRATION"

    return True, 0, ""


def post_transaction(db: Session, daily_tran: DailyTransaction) -> Transaction:
    """Post a validated transaction — from CBTRN02C 2000-POST-TRANSACTION."""
    now = datetime.now(UTC).strftime("%Y-%m-%d-%H.%M.%S.%f")[:26]

    transaction = Transaction(
        tran_id=daily_tran.tran_id,
        tran_type_cd=daily_tran.tran_type_cd,
        tran_cat_cd=daily_tran.tran_cat_cd,
        tran_source=daily_tran.tran_source,
        tran_desc=daily_tran.tran_desc,
        tran_amt=daily_tran.tran_amt,
        merchant_id=daily_tran.merchant_id,
        merchant_name=daily_tran.merchant_name,
        merchant_city=daily_tran.merchant_city,
        merchant_zip=daily_tran.merchant_zip,
        card_num=daily_tran.card_num,
        orig_ts=daily_tran.orig_ts,
        proc_ts=now,
    )
    db.add(transaction)

    # 2700-UPDATE-TCATBAL
    xref = db.query(CardXref).filter_by(card_num=daily_tran.card_num).first()
    if xref:
        tcat = (
            db.query(TranCategoryBalance)
            .filter_by(acct_id=xref.acct_id, type_cd=daily_tran.tran_type_cd, cat_cd=daily_tran.tran_cat_cd)
            .first()
        )
        if tcat:
            tcat.balance = Decimal(str(tcat.balance)) + Decimal(str(daily_tran.tran_amt))
        else:
            tcat = TranCategoryBalance(
                acct_id=xref.acct_id,
                type_cd=daily_tran.tran_type_cd,
                cat_cd=daily_tran.tran_cat_cd,
                balance=daily_tran.tran_amt,
            )
            db.add(tcat)

        # 2800-UPDATE-ACCOUNT-REC
        account = db.query(Account).filter_by(acct_id=xref.acct_id).first()
        if account:
            account.curr_bal = Decimal(str(account.curr_bal)) + Decimal(str(daily_tran.tran_amt))
            if Decimal(str(daily_tran.tran_amt)) >= 0:
                account.curr_cyc_credit = Decimal(str(account.curr_cyc_credit)) + Decimal(str(daily_tran.tran_amt))
            else:
                account.curr_cyc_debit = Decimal(str(account.curr_cyc_debit)) + Decimal(str(daily_tran.tran_amt))

    return transaction


def run_post_transactions(db: Session) -> dict:
    """Run the full post transactions batch — replaces CBTRN02C main program."""
    daily_trans = db.query(DailyTransaction).all()

    posted = 0
    rejected = 0
    rejects: list[dict] = []

    for dt in daily_trans:
        is_valid, fail_code, fail_desc = validate_transaction(db, dt)
        if is_valid:
            post_transaction(db, dt)
            posted += 1
        else:
            rejected += 1
            rejects.append(
                {
                    "tran_id": dt.tran_id,
                    "card_num": dt.card_num,
                    "fail_reason": fail_code,
                    "fail_desc": fail_desc,
                }
            )

    db.commit()
    return {"posted": posted, "rejected": rejected, "rejects": rejects}
