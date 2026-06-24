"""Daily batch transaction processor Lambda.

Replicates the mainframe CBTRN01C / CBTRN02C / CBTRN03C batch logic:
  1. Scan for unprocessed transactions (empty ``tran_proc_ts``).
  2. For each transaction, look up the card cross-reference and account,
     apply the balance update, then stamp the transaction as processed.

Environment variables
---------------------
TABLE_PREFIX   DynamoDB table name prefix (default ``carddemo``)
AWS_REGION_NAME  AWS region
"""

import os
from datetime import datetime, timezone
from decimal import Decimal

import boto3

PREFIX = os.environ.get("TABLE_PREFIX", "carddemo")
REGION = os.environ.get("AWS_REGION_NAME", os.environ.get("AWS_DEFAULT_REGION", "us-east-1"))

dynamodb = boto3.resource("dynamodb", region_name=REGION)

tbl_transactions = dynamodb.Table(f"{PREFIX}-transactions")
tbl_card_xref = dynamodb.Table(f"{PREFIX}-card-xref")
tbl_accounts = dynamodb.Table(f"{PREFIX}-accounts")

ZERO = Decimal("0")


def _scan_unprocessed():
    """Yield all transactions whose ``tran_proc_ts`` is empty."""
    params = {
        "FilterExpression": "tran_proc_ts = :empty OR begins_with(tran_proc_ts, :space)",
        "ExpressionAttributeValues": {":empty": "", ":space": " "},
    }
    while True:
        resp = tbl_transactions.scan(**params)
        yield from resp.get("Items", [])
        if "LastEvaluatedKey" not in resp:
            break
        params["ExclusiveStartKey"] = resp["LastEvaluatedKey"]


def _get_acct_id(card_num: str, tran_acct_id: str | None) -> str | None:
    """Resolve acct_id from the transaction record or card-xref table."""
    if tran_acct_id:
        return tran_acct_id
    resp = tbl_card_xref.get_item(Key={"card_num": card_num})
    xref = resp.get("Item")
    if xref:
        return xref.get("acct_id")
    return None


def _apply_transaction(tran: dict) -> str:
    """Apply a single transaction to its account. Returns a status string."""
    card_num = tran.get("card_num", "")
    acct_id = _get_acct_id(card_num, tran.get("acct_id"))
    if not acct_id:
        return "skip:no_acct"

    resp = tbl_accounts.get_item(Key={"acct_id": acct_id})
    account = resp.get("Item")
    if not account:
        return "skip:acct_not_found"

    tran_type = tran.get("tran_type_cd", "")
    amt = Decimal(tran.get("tran_amt", "0"))
    curr_bal = Decimal(account.get("curr_bal", "0"))
    cyc_credit = Decimal(account.get("curr_cyc_credit", "0"))
    cyc_debit = Decimal(account.get("curr_cyc_debit", "0"))

    if tran_type == "01":
        curr_bal -= amt
        cyc_debit += amt
    elif tran_type in ("02", "03", "05"):
        curr_bal += amt
        cyc_credit += amt
    else:
        return "skip:unsupported_type"

    tbl_accounts.update_item(
        Key={"acct_id": acct_id},
        UpdateExpression="SET curr_bal = :bal, curr_cyc_credit = :cc, curr_cyc_debit = :cd",
        ExpressionAttributeValues={
            ":bal": str(curr_bal),
            ":cc": str(cyc_credit),
            ":cd": str(cyc_debit),
        },
    )

    now_ts = datetime.now(timezone.utc).isoformat()
    tbl_transactions.update_item(
        Key={
            "tran_id": tran["tran_id"],
            "tran_orig_ts": tran["tran_orig_ts"],
        },
        UpdateExpression="SET tran_proc_ts = :ts",
        ExpressionAttributeValues={":ts": now_ts},
    )

    return "processed"


def handler(event, context):
    """Lambda entry point."""
    processed = 0
    skipped = 0
    errors = 0
    details = []

    for tran in _scan_unprocessed():
        try:
            status = _apply_transaction(tran)
            if status == "processed":
                processed += 1
            else:
                skipped += 1
                details.append({"tran_id": tran.get("tran_id"), "reason": status})
        except Exception as exc:
            errors += 1
            details.append({"tran_id": tran.get("tran_id"), "error": str(exc)})

    summary = {
        "processed": processed,
        "skipped": skipped,
        "errors": errors,
    }
    if details:
        summary["details"] = details[:50]

    print(f"Batch complete: {summary}")
    return summary
