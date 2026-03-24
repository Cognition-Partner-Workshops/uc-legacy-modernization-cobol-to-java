"""Interest calculator — from CBACT04C.cbl paragraph 1300-COMPUTE-INTEREST.

COBOL formula (line 464-465):
  COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200

This preserves the exact business logic:
  monthly_interest = (tran_cat_balance * interest_rate) / 1200

The 1200 divisor converts annual rate (e.g., 18.00%) to monthly:
  18.00 / 1200 = 0.015 = 1.5% monthly
"""

from decimal import ROUND_HALF_UP, Decimal

from sqlalchemy.orm import Session

from app.models.account import Account
from app.models.card_xref import CardXref
from app.models.disclosure_group import DisclosureGroup
from app.models.tran_category_balance import TranCategoryBalance


def calculate_monthly_interest(balance: Decimal, rate: Decimal) -> Decimal:
    """Exact replica of CBACT04C 1300-COMPUTE-INTEREST.

    COMPUTE WS-MONTHLY-INT = (TRAN-CAT-BAL * DIS-INT-RATE) / 1200
    """
    if rate == 0:
        return Decimal("0.00")
    return (balance * rate / Decimal("1200")).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP)


def run_interest_calculation(db: Session) -> dict:
    """Full interest calculation batch — replaces CBACT04C main loop.

    Reads all transaction category balance records sequentially,
    looks up disclosure group for interest rate,
    computes interest, and updates account balances.
    """
    processed = 0
    total_interest = Decimal("0.00")
    last_acct_id = None
    acct_total_interest = Decimal("0.00")

    tcat_records = db.query(TranCategoryBalance).order_by(TranCategoryBalance.acct_id).all()

    for tcat in tcat_records:
        # When account changes, update the previous account (from 1050-UPDATE-ACCOUNT)
        if last_acct_id is not None and tcat.acct_id != last_acct_id:
            _update_account_balance(db, last_acct_id, acct_total_interest)
            acct_total_interest = Decimal("0.00")

        last_acct_id = tcat.acct_id

        # Look up account to get group_id (from 1100-GET-ACCT-DATA)
        account = db.query(Account).filter_by(acct_id=tcat.acct_id).first()
        if not account:
            continue

        # Look up interest rate from disclosure group (from 1200-GET-INTEREST-RATE)
        rate = _get_interest_rate(db, account.group_id, tcat.type_cd, tcat.cat_cd)

        if rate != Decimal("0"):
            monthly_int = calculate_monthly_interest(Decimal(str(tcat.balance)), rate)
            acct_total_interest += monthly_int
            total_interest += monthly_int
            processed += 1

    # Update the last account
    if last_acct_id is not None:
        _update_account_balance(db, last_acct_id, acct_total_interest)

    db.commit()
    return {"processed": processed, "total_interest": str(total_interest)}


def _get_interest_rate(db: Session, group_id: str, type_cd: str, cat_cd: int) -> Decimal:
    """Look up interest rate — from CBACT04C 1200-GET-INTEREST-RATE.

    Falls back to 'DEFAULT' group if specific group not found (status '23').
    """
    disc = db.query(DisclosureGroup).filter_by(group_id=group_id, tran_type_cd=type_cd, tran_cat_cd=cat_cd).first()
    if disc:
        return Decimal(str(disc.interest_rate))

    # Fallback to DEFAULT group (from 1200-A-GET-DEFAULT-INT-RATE)
    disc = db.query(DisclosureGroup).filter_by(group_id="DEFAULT", tran_type_cd=type_cd, tran_cat_cd=cat_cd).first()
    if disc:
        return Decimal(str(disc.interest_rate))

    return Decimal("0")


def _update_account_balance(db: Session, acct_id: int, total_interest: Decimal) -> None:
    """Update account balance with interest — from CBACT04C 1050-UPDATE-ACCOUNT.

    ADD WS-TOTAL-INT TO ACCT-CURR-BAL
    MOVE 0 TO ACCT-CURR-CYC-CREDIT
    MOVE 0 TO ACCT-CURR-CYC-DEBIT
    """
    account = db.query(Account).filter_by(acct_id=acct_id).first()
    if account:
        account.curr_bal = Decimal(str(account.curr_bal)) + total_interest
        account.curr_cyc_credit = Decimal("0")
        account.curr_cyc_debit = Decimal("0")


def get_xref_card_num(db: Session, acct_id: int) -> str:
    """Get card number from cross-reference — from CBACT04C 1110-GET-XREF-DATA."""
    xref = db.query(CardXref).filter_by(acct_id=acct_id).first()
    return xref.card_num if xref else ""
