"""Transaction report batch — from CBTRN03C.cbl.

CBTRN03C reads the transaction file sequentially and produces a formatted
transaction report grouped by account.
"""

from decimal import Decimal

from sqlalchemy.orm import Session

from app.models.transaction import Transaction


def run_transaction_report(db: Session, start_date: str | None = None, end_date: str | None = None) -> dict:
    """Generate transaction report — replaces CBTRN03C main program."""
    query = db.query(Transaction)

    if start_date:
        query = query.filter(Transaction.orig_ts >= start_date)
    if end_date:
        query = query.filter(Transaction.orig_ts <= end_date)

    transactions = query.order_by(Transaction.card_num, Transaction.orig_ts).all()

    total_amount = Decimal("0")
    report_lines: list[dict] = []

    for t in transactions:
        report_lines.append(
            {
                "tran_id": t.tran_id,
                "card_num": t.card_num,
                "tran_type_cd": t.tran_type_cd,
                "tran_cat_cd": t.tran_cat_cd,
                "tran_desc": t.tran_desc,
                "tran_amt": str(t.tran_amt),
                "orig_ts": t.orig_ts,
                "merchant_name": t.merchant_name,
            }
        )
        total_amount += Decimal(str(t.tran_amt))

    return {
        "total_records": len(report_lines),
        "total_amount": str(total_amount),
        "report": report_lines,
    }
