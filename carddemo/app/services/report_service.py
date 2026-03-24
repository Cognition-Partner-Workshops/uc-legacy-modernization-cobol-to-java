"""Report service — replaces CORPT00C.cbl transaction report generation.

CORPT00C: Generates transaction reports filtered by account, card, or date range.
"""

from decimal import Decimal

from sqlalchemy.orm import Session

from app.models.card_xref import CardXref
from app.models.transaction import Transaction


def generate_transaction_report(
    db: Session,
    acct_id: int | None = None,
    card_num: str | None = None,
    start_date: str | None = None,
    end_date: str | None = None,
) -> dict:
    """Generate a transaction report — replaces CORPT00C report logic."""
    query = db.query(Transaction)

    if card_num:
        query = query.filter(Transaction.card_num == card_num)
    elif acct_id:
        xrefs = db.query(CardXref.card_num).filter_by(acct_id=acct_id).subquery()
        query = query.filter(Transaction.card_num.in_(xrefs))

    if start_date:
        query = query.filter(Transaction.orig_ts >= start_date)
    if end_date:
        query = query.filter(Transaction.orig_ts <= end_date)

    transactions = query.order_by(Transaction.orig_ts).all()

    total_amount = sum(Decimal(str(t.tran_amt)) for t in transactions)

    report_data = [
        {
            "tran_id": t.tran_id,
            "tran_type_cd": t.tran_type_cd,
            "tran_cat_cd": t.tran_cat_cd,
            "tran_desc": t.tran_desc,
            "tran_amt": str(t.tran_amt),
            "card_num": t.card_num,
            "merchant_name": t.merchant_name,
            "orig_ts": t.orig_ts,
        }
        for t in transactions
    ]

    return {
        "report_data": report_data,
        "total_records": len(report_data),
        "total_amount": str(total_amount),
    }
