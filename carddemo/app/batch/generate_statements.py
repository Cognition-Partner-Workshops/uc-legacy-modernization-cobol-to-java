"""Generate statements batch — from CBSTM03A.CBL.

CBSTM03A uses ALTER/GO TO and z/OS control block addressing (PSA→TCB→TIOT)
to determine DD names. This is replaced with normal Python control flow
and config/env vars.

The 2D array (51 cards × 10 transactions) becomes a dict of lists.
Dual output (text + HTML) uses Jinja2 for HTML.
"""

from decimal import Decimal
from pathlib import Path

from jinja2 import Environment, FileSystemLoader
from sqlalchemy.orm import Session

from app.models.account import Account
from app.models.card import Card
from app.models.card_xref import CardXref
from app.models.customer import Customer
from app.models.transaction import Transaction


def generate_statement(db: Session, acct_id: int) -> dict:
    """Generate a statement for one account."""
    account = db.query(Account).filter_by(acct_id=acct_id).first()
    if not account:
        return {"error": f"Account {acct_id} not found"}

    # Get customer info via cross-reference
    xref = db.query(CardXref).filter_by(acct_id=acct_id).first()
    customer = None
    if xref:
        customer = db.query(Customer).filter_by(cust_id=xref.cust_id).first()

    # Get all cards for this account
    cards = db.query(Card).filter_by(acct_id=acct_id).all()
    card_nums = [c.card_num for c in cards]

    # Get transactions for all cards (replaces 51×10 2D array)
    transactions_by_card: dict[str, list[dict]] = {}
    for card_num in card_nums:
        trans = db.query(Transaction).filter_by(card_num=card_num).order_by(Transaction.orig_ts.desc()).limit(10).all()
        transactions_by_card[card_num] = [
            {
                "tran_id": t.tran_id,
                "tran_desc": t.tran_desc,
                "tran_amt": str(t.tran_amt),
                "orig_ts": t.orig_ts,
                "merchant_name": t.merchant_name,
            }
            for t in trans
        ]

    total_debits = Decimal("0")
    total_credits = Decimal("0")
    for card_trans in transactions_by_card.values():
        for t in card_trans:
            amt = Decimal(t["tran_amt"])
            if amt >= 0:
                total_credits += amt
            else:
                total_debits += amt

    statement = {
        "acct_id": acct_id,
        "customer_name": f"{customer.first_name} {customer.last_name}" if customer else "N/A",
        "curr_bal": str(account.curr_bal),
        "credit_limit": str(account.credit_limit),
        "open_date": account.open_date,
        "expiration_date": account.expiration_date,
        "total_debits": str(total_debits),
        "total_credits": str(total_credits),
        "transactions_by_card": transactions_by_card,
    }

    return statement


def generate_statement_html(db: Session, acct_id: int, template_dir: str | None = None) -> str:
    """Generate HTML statement using Jinja2 — replaces CBSTM03A print output."""
    statement = generate_statement(db, acct_id)
    if "error" in statement:
        return f"<html><body><p>{statement['error']}</p></body></html>"

    if template_dir is None:
        template_dir = str(Path(__file__).parent.parent.parent / "templates")

    try:
        env = Environment(loader=FileSystemLoader(template_dir), autoescape=True)
        template = env.get_template("statement.html")
        return template.render(**statement)
    except Exception:
        # Fallback to inline template if file not found
        return _inline_statement_html(statement)


def _inline_statement_html(statement: dict) -> str:
    """Fallback inline HTML statement template."""
    html = f"""<!DOCTYPE html>
<html><head><title>Account Statement - {statement["acct_id"]}</title></head>
<body>
<h1>Account Statement</h1>
<p><strong>Account:</strong> {statement["acct_id"]}</p>
<p><strong>Customer:</strong> {statement["customer_name"]}</p>
<p><strong>Balance:</strong> ${statement["curr_bal"]}</p>
<p><strong>Credit Limit:</strong> ${statement["credit_limit"]}</p>
<p><strong>Total Credits:</strong> ${statement["total_credits"]}</p>
<p><strong>Total Debits:</strong> ${statement["total_debits"]}</p>
"""
    for card_num, trans in statement["transactions_by_card"].items():
        html += f"<h2>Card: {card_num}</h2><table border='1'>"
        html += "<tr><th>ID</th><th>Description</th><th>Amount</th><th>Date</th><th>Merchant</th></tr>"
        for t in trans:
            html += f"<tr><td>{t['tran_id']}</td><td>{t['tran_desc']}</td>"
            html += f"<td>${t['tran_amt']}</td><td>{t['orig_ts']}</td><td>{t['merchant_name']}</td></tr>"
        html += "</table>"

    html += "</body></html>"
    return html


def run_generate_statements(db: Session) -> dict:
    """Generate statements for all active accounts — replaces CBSTM03A batch."""
    accounts = db.query(Account).filter_by(active_status="Y").all()
    generated = 0
    for account in accounts:
        generate_statement(db, account.acct_id)
        generated += 1

    return {"generated": generated}
