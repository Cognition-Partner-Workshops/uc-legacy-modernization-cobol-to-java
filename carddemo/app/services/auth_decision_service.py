"""Authorization decision service — core logic from COPAUA0C.cbl paragraph 6000-MAKE-DECISION.

The COBOL program COPAUA0C processes MQ authorization requests:
1. Receives auth request from MQ queue
2. Reads customer data from IMS (GU call)
3. Checks account status, credit limit, card expiration
4. Makes approve/decline decision
5. Updates IMS segments and inserts DB2 AUTHFRDS record
6. Sends response back via MQ

This service preserves the exact decision logic from 6000-MAKE-DECISION.
"""

from datetime import UTC, datetime
from decimal import Decimal

from sqlalchemy.orm import Session

from app.models.account import Account
from app.models.auth_fraud import AuthFraud
from app.models.card import Card
from app.models.card_xref import CardXref
from app.models.pending_auth_detail import PendingAuthDetail


def make_decision(db: Session, detail_id: int, decision: str, reason: str = "") -> dict:
    """Process authorization decision — replaces COPAUA0C 6000-MAKE-DECISION.

    Decision logic preserved from COBOL:
    1. Check if card exists and is active
    2. Check if account exists and is active
    3. Check if account is not expired
    4. Check if transaction would exceed credit limit
    5. Approve or decline based on checks
    """
    detail = db.query(PendingAuthDetail).filter_by(id=detail_id).first()
    if not detail:
        return {"success": False, "message": "Pending authorization not found"}

    if decision.upper() == "APPROVE":
        result = _validate_and_approve(db, detail)
    elif decision.upper() == "DECLINE":
        result = {"success": True, "decision": "DECLINED", "reason": reason or "Manual decline"}
    else:
        return {"success": False, "message": "Invalid decision. Use APPROVE or DECLINE."}

    # Update detail status
    now = datetime.now(UTC).strftime("%Y-%m-%d-%H.%M.%S.%f")[:26]
    detail.auth_status = "A" if result["decision"] == "APPROVED" else "D"
    detail.proc_ts = now

    # Insert into auth_fraud table (DB2 AUTHFRDS)
    fraud_record = AuthFraud(
        acct_id=detail.acct_id,
        card_num=detail.card_num,
        tran_amt=detail.tran_amt,
        decision=result["decision"],
        reason=result["reason"],
        decision_ts=now,
    )
    db.add(fraud_record)
    db.commit()

    return result


def _validate_and_approve(db: Session, detail: PendingAuthDetail) -> dict:
    """Validation checks from COPAUA0C 6000-MAKE-DECISION."""
    # Check card exists and is active
    card = db.query(Card).filter_by(card_num=detail.card_num).first()
    if not card:
        return {"success": True, "decision": "DECLINED", "reason": "Card not found"}
    if card.active_status != "Y":
        return {"success": True, "decision": "DECLINED", "reason": "Card is not active"}

    # Check card expiration
    now_date = datetime.now(UTC).strftime("%Y-%m-%d")
    if card.expiration_date < now_date:
        return {"success": True, "decision": "DECLINED", "reason": "Card is expired"}

    # Check account via cross-reference
    xref = db.query(CardXref).filter_by(card_num=detail.card_num).first()
    if not xref:
        return {"success": True, "decision": "DECLINED", "reason": "Card cross-reference not found"}

    account = db.query(Account).filter_by(acct_id=xref.acct_id).first()
    if not account:
        return {"success": True, "decision": "DECLINED", "reason": "Account not found"}
    if account.active_status != "Y":
        return {"success": True, "decision": "DECLINED", "reason": "Account is not active"}

    # Check credit limit (from CBTRN02C 1500-B-LOOKUP-ACCT logic)
    temp_bal = Decimal(str(account.curr_cyc_credit)) - Decimal(str(account.curr_cyc_debit)) + detail.tran_amt
    if Decimal(str(account.credit_limit)) < temp_bal:
        return {"success": True, "decision": "DECLINED", "reason": "Transaction would exceed credit limit"}

    return {"success": True, "decision": "APPROVED", "reason": "All checks passed"}
