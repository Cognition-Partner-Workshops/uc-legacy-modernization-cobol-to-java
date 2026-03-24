"""Validate transactions batch — from CBTRN01C.cbl.

CBTRN01C validates the daily transaction file without posting.
It produces a validation report showing which transactions would pass or fail.
"""

from sqlalchemy.orm import Session

from app.batch.post_transactions import validate_transaction
from app.models.daily_transaction import DailyTransaction


def run_validate_transactions(db: Session) -> dict:
    """Validate all daily transactions — replaces CBTRN01C main program."""
    daily_trans = db.query(DailyTransaction).all()

    valid = 0
    invalid = 0
    results: list[dict] = []

    for dt in daily_trans:
        is_valid, fail_code, fail_desc = validate_transaction(db, dt)
        results.append(
            {
                "tran_id": dt.tran_id,
                "card_num": dt.card_num,
                "valid": is_valid,
                "fail_reason": fail_code if not is_valid else 0,
                "fail_desc": fail_desc if not is_valid else "",
            }
        )
        if is_valid:
            valid += 1
        else:
            invalid += 1

    return {"total": len(daily_trans), "valid": valid, "invalid": invalid, "results": results}
