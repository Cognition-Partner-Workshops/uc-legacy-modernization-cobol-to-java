"""Lambda API handler for CardDemo AWS Migration.

Single Lambda function behind HTTP API Gateway (payload format v2.0)
with routing for all CardDemo resources.
"""

import json
import re
from decimal import Decimal

from db import (
    accounts_table,
    cards_table,
    customers_table,
    scan_paginated,
    query_paginated,
    transactions_table,
    transaction_types_table,
    transaction_categories_table,
)

CORS_HEADERS = {
    "Content-Type": "application/json",
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "GET,POST,PUT,DELETE,OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type",
}

PAGE_SIZE = 20


class DecimalEncoder(json.JSONEncoder):
    """JSON encoder that converts Decimal to str."""

    def default(self, o):
        if isinstance(o, Decimal):
            return str(o)
        return super().default(o)


def _json_dumps(obj):
    return json.dumps(obj, cls=DecimalEncoder)


def _success(data, last_key=None):
    body = {"data": data}
    if last_key is not None:
        body["last_key"] = last_key
    return {"statusCode": 200, "headers": CORS_HEADERS, "body": _json_dumps(body)}


def _error(status_code, message):
    return {
        "statusCode": status_code,
        "headers": CORS_HEADERS,
        "body": _json_dumps({"error": message}),
    }


def _parse_body(event):
    body = event.get("body")
    if not body:
        return None
    return json.loads(body, parse_float=Decimal)


def _get_last_key(event):
    """Parse last_key from query params (JSON-encoded DynamoDB key)."""
    params = event.get("queryStringParameters") or {}
    lk = params.get("last_key")
    if lk:
        return json.loads(lk, parse_float=Decimal)
    return None


# --------------- Route handlers ---------------


def _get_accounts(event):
    last_key = _get_last_key(event)
    result = scan_paginated(accounts_table(), limit=PAGE_SIZE, last_key=last_key)
    return _success(result["items"], result.get("last_key"))


def _get_account(event):
    acct_id = event["pathParameters"]["acct_id"]
    resp = accounts_table().get_item(Key={"acct_id": acct_id})
    item = resp.get("Item")
    if not item:
        return _error(404, "Account not found")
    return _success(item)


def _put_account(event):
    acct_id = event["pathParameters"]["acct_id"]
    body = _parse_body(event)
    if not body:
        return _error(400, "Request body is required")

    update_expr_parts = []
    expr_values = {}
    expr_names = {}
    for key, value in body.items():
        if key == "acct_id":
            continue
        safe_key = f"#k_{key}"
        val_key = f":v_{key}"
        update_expr_parts.append(f"{safe_key} = {val_key}")
        expr_names[safe_key] = key
        expr_values[val_key] = value

    if not update_expr_parts:
        return _error(400, "No fields to update")

    accounts_table().update_item(
        Key={"acct_id": acct_id},
        UpdateExpression="SET " + ", ".join(update_expr_parts),
        ExpressionAttributeNames=expr_names,
        ExpressionAttributeValues=expr_values,
    )
    return _success({"acct_id": acct_id, "updated": True})


def _get_customers(event):
    last_key = _get_last_key(event)
    result = scan_paginated(customers_table(), limit=PAGE_SIZE, last_key=last_key)
    return _success(result["items"], result.get("last_key"))


def _get_customer(event):
    cust_id = event["pathParameters"]["cust_id"]
    resp = customers_table().get_item(Key={"cust_id": cust_id})
    item = resp.get("Item")
    if not item:
        return _error(404, "Customer not found")
    return _success(item)


def _get_cards(event):
    params = event.get("queryStringParameters") or {}
    acct_id = params.get("acct_id")
    last_key = _get_last_key(event)

    if acct_id:
        result = query_paginated(
            cards_table(),
            key_condition="acct_id = :acct_id",
            expr_values={":acct_id": acct_id},
            limit=PAGE_SIZE,
            last_key=last_key,
            index_name="acct_id-index",
        )
    else:
        result = scan_paginated(cards_table(), limit=PAGE_SIZE, last_key=last_key)
    return _success(result["items"], result.get("last_key"))


def _get_card(event):
    card_num = event["pathParameters"]["card_num"]
    resp = cards_table().get_item(Key={"card_num": card_num})
    item = resp.get("Item")
    if not item:
        return _error(404, "Card not found")
    return _success(item)


def _get_transactions(event):
    params = event.get("queryStringParameters") or {}
    acct_id = params.get("acct_id")
    card_num = params.get("card_num")
    last_key = _get_last_key(event)

    if acct_id:
        result = query_paginated(
            transactions_table(),
            key_condition="acct_id = :acct_id",
            expr_values={":acct_id": acct_id},
            limit=PAGE_SIZE,
            last_key=last_key,
            index_name="acct_id-index",
        )
    elif card_num:
        result = query_paginated(
            transactions_table(),
            key_condition="card_num = :card_num",
            expr_values={":card_num": card_num},
            limit=PAGE_SIZE,
            last_key=last_key,
            index_name="card_num-index",
        )
    else:
        result = scan_paginated(transactions_table(), limit=PAGE_SIZE, last_key=last_key)
    return _success(result["items"], result.get("last_key"))


def _get_transaction(event):
    tran_id = event["pathParameters"]["tran_id"]
    result = query_paginated(
        transactions_table(),
        key_condition="tran_id = :tran_id",
        expr_values={":tran_id": tran_id},
        limit=100,
    )
    return _success(result["items"])


def _post_transaction(event):
    body = _parse_body(event)
    if not body:
        return _error(400, "Request body is required")

    required = ["tran_id", "tran_orig_ts", "tran_amt", "card_num", "acct_id"]
    missing = [f for f in required if f not in body]
    if missing:
        return _error(400, f"Missing required fields: {', '.join(missing)}")

    transactions_table().put_item(Item=body)
    return _success({"tran_id": body["tran_id"], "created": True})


def _get_transaction_types(event):
    result = scan_paginated(transaction_types_table(), limit=100)
    return _success(result["items"])


def _get_transaction_categories(event):
    result = scan_paginated(transaction_categories_table(), limit=100)
    return _success(result["items"])


def _post_batch_process_daily(event):
    """Process daily batch: iterate transactions and update account balances."""
    txn_table = transactions_table()
    acct_table = accounts_table()

    balance_updates = {}
    scan_params = {}
    total_processed = 0

    while True:
        response = txn_table.scan(**scan_params)
        items = response.get("Items", [])

        for txn in items:
            acct_id = txn.get("acct_id")
            tran_amt = txn.get("tran_amt")
            if not acct_id or tran_amt is None:
                continue
            amt = Decimal(str(tran_amt))
            balance_updates[acct_id] = balance_updates.get(acct_id, Decimal("0")) + amt
            total_processed += 1

        if "LastEvaluatedKey" not in response:
            break
        scan_params["ExclusiveStartKey"] = response["LastEvaluatedKey"]

    accounts_updated = 0
    for acct_id, total_amt in balance_updates.items():
        acct_table.update_item(
            Key={"acct_id": acct_id},
            UpdateExpression="SET curr_bal = if_not_exists(curr_bal, :zero) + :amt, curr_cyc_debit = if_not_exists(curr_cyc_debit, :zero) + :amt",
            ExpressionAttributeValues={":amt": total_amt, ":zero": Decimal("0")},
        )
        accounts_updated += 1

    return _success({
        "transactions_processed": total_processed,
        "accounts_updated": accounts_updated,
    })


def _get_dashboard(event):
    """Return summary stats across all tables."""
    acct_resp = accounts_table().scan(Select="COUNT")
    cust_resp = customers_table().scan(Select="COUNT")
    txn_resp = transactions_table().scan(Select="COUNT")
    card_resp = cards_table().scan(Select="COUNT")

    return _success({
        "total_accounts": acct_resp["Count"],
        "total_customers": cust_resp["Count"],
        "total_transactions": txn_resp["Count"],
        "total_cards": card_resp["Count"],
    })


# --------------- Router ---------------

ROUTES = [
    ("GET", r"^/accounts$", _get_accounts),
    ("GET", r"^/accounts/(?P<acct_id>[^/]+)$", _get_account),
    ("PUT", r"^/accounts/(?P<acct_id>[^/]+)$", _put_account),
    ("GET", r"^/customers$", _get_customers),
    ("GET", r"^/customers/(?P<cust_id>[^/]+)$", _get_customer),
    ("GET", r"^/cards$", _get_cards),
    ("GET", r"^/cards/(?P<card_num>[^/]+)$", _get_card),
    ("GET", r"^/transactions$", _get_transactions),
    ("GET", r"^/transactions/(?P<tran_id>[^/]+)$", _get_transaction),
    ("POST", r"^/transactions$", _post_transaction),
    ("GET", r"^/transaction-types$", _get_transaction_types),
    ("GET", r"^/transaction-categories$", _get_transaction_categories),
    ("POST", r"^/batch/process-daily$", _post_batch_process_daily),
    ("GET", r"^/dashboard$", _get_dashboard),
]


def handler(event, context):
    """Main Lambda entry point."""
    http_ctx = event.get("requestContext", {}).get("http", {})
    method = http_ctx.get("method", "").upper()
    path = http_ctx.get("path", "")

    # Handle CORS preflight
    if method == "OPTIONS":
        return {"statusCode": 200, "headers": CORS_HEADERS, "body": ""}

    for route_method, pattern, route_handler in ROUTES:
        if method != route_method:
            continue
        match = re.match(pattern, path)
        if match:
            # Inject path parameters from regex groups
            if match.groupdict():
                if "pathParameters" not in event or event["pathParameters"] is None:
                    event["pathParameters"] = {}
                event["pathParameters"].update(match.groupdict())
            try:
                return route_handler(event)
            except Exception as e:
                return _error(500, f"Internal server error: {str(e)}")

    return _error(404, f"Route not found: {method} {path}")
