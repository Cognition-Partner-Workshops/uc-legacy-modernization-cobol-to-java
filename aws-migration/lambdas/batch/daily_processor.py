"""Daily batch processing Lambda for CardDemo.

Replicates the mainframe post-transaction batch logic of CBTRN01C/02C/03C
(see ``app/cbl/CBTRN01C.cbl``, ``CBTRN02C.cbl``, ``CBTRN03C.cbl``) against the
DynamoDB tables described in ``aws-migration/API_CONTRACT.md`` (§3, §5).

For every transaction not yet marked ``batch_processed``:
  1. Read the owning account (by ``acct_id``).
  2. Apply the transaction to the account balances using Decimal math:
       - type "01" (Purchase): curr_bal -= amt; curr_cyc_debit += amt
       - types "02"/"03"/"05" (Payment/Credit/Refund):
             curr_bal += amt; curr_cyc_credit += amt
  3. Write the updated balances back as STRINGS (read-compute-write; additive
     DynamoDB update expressions are not used because the fields are strings).
  4. Mark the transaction ``batch_processed = true`` and set ``tran_proc_ts``.

Returns ``{transactions_processed, skipped, errors}``.
"""

from __future__ import annotations

import os
from datetime import datetime, timezone
from decimal import Decimal

import boto3

ACCOUNTS_TABLE = os.environ.get("ACCOUNTS_TABLE", "carddemo-accounts")
TRANSACTIONS_TABLE = os.environ.get("TRANSACTIONS_TABLE", "carddemo-transactions")

# Transaction types that credit the account (increase the balance).
CREDIT_TYPES = {"02", "03", "05"}
# Transaction type that debits the account (decrease the balance).
DEBIT_TYPES = {"01"}


def _money(value: object) -> Decimal:
    """Coerce a stored money attribute (string/absent) to a Decimal."""
    if value is None or value == "":
        return Decimal("0")
    return Decimal(str(value))


def _format(amount: Decimal) -> str:
    """Format a Decimal as a 2-decimal-place money string."""
    return f"{amount.quantize(Decimal('0.01'))}"


def handler(event, context):
    dynamodb = boto3.resource("dynamodb")
    accounts = dynamodb.Table(ACCOUNTS_TABLE)
    transactions = dynamodb.Table(TRANSACTIONS_TABLE)

    processed = 0
    skipped = 0
    errors = 0

    scan_kwargs = {
        "FilterExpression": "attribute_not_exists(batch_processed)",
    }
    start_key = None
    while True:
        if start_key:
            scan_kwargs["ExclusiveStartKey"] = start_key
        response = transactions.scan(**scan_kwargs)

        for tran in response.get("Items", []):
            try:
                acct_id = tran.get("acct_id")
                type_cd = tran.get("type_cd")
                amount = _money(tran.get("amt"))

                if not acct_id:
                    skipped += 1
                    continue

                acct_resp = accounts.get_item(Key={"acct_id": acct_id})
                account = acct_resp.get("Item")
                if account is None:
                    skipped += 1
                    continue

                curr_bal = _money(account.get("curr_bal"))
                curr_cyc_credit = _money(account.get("curr_cyc_credit"))
                curr_cyc_debit = _money(account.get("curr_cyc_debit"))

                if type_cd in DEBIT_TYPES:
                    curr_bal -= amount
                    curr_cyc_debit += amount
                elif type_cd in CREDIT_TYPES:
                    curr_bal += amount
                    curr_cyc_credit += amount
                else:
                    # Unknown type: nothing to post, but the transaction is
                    # still considered handled and marked processed below.
                    pass

                accounts.update_item(
                    Key={"acct_id": acct_id},
                    UpdateExpression=(
                        "SET curr_bal = :bal, "
                        "curr_cyc_credit = :cyc_credit, "
                        "curr_cyc_debit = :cyc_debit"
                    ),
                    ExpressionAttributeValues={
                        ":bal": _format(curr_bal),
                        ":cyc_credit": _format(curr_cyc_credit),
                        ":cyc_debit": _format(curr_cyc_debit),
                    },
                )

                transactions.update_item(
                    Key={
                        "tran_id": tran["tran_id"],
                        "tran_orig_ts": tran["tran_orig_ts"],
                    },
                    UpdateExpression=(
                        "SET batch_processed = :bp, tran_proc_ts = :ts"
                    ),
                    ExpressionAttributeValues={
                        ":bp": True,
                        ":ts": datetime.now(timezone.utc).isoformat(),
                    },
                )

                processed += 1
            except Exception:
                errors += 1

        start_key = response.get("LastEvaluatedKey")
        if not start_key:
            break

    return {
        "transactions_processed": processed,
        "skipped": skipped,
        "errors": errors,
    }
