"""CardDemo API Lambda — regex-routed HTTP API Gateway v2 handler.

Implements the 14 REST routes defined in API_CONTRACT.md (§3, §4) over the
DynamoDB tables described in §1. All monetary values are stored and returned as
strings; ``decimal.Decimal`` is used for arithmetic and serialized as strings by
``db.DecimalEncoder``.
"""

import base64
import binascii
import decimal
import json
import re
import uuid
from datetime import datetime, timezone

from boto3.dynamodb.conditions import Key

import db

CORS_HEADERS = {
    "Content-Type": "application/json",
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "GET,POST,PUT,OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type",
}

DEFAULT_LIMIT = 25

# Transaction type codes that credit the account (vs. type "01" which debits).
CREDIT_TYPE_CODES = {"02", "03", "05"}


# --------------------------------------------------------------------------- #
# Errors / responses
# --------------------------------------------------------------------------- #
class ApiError(Exception):
    """Raised to short-circuit a request with a specific HTTP status."""

    def __init__(self, status, message):
        super().__init__(message)
        self.status = status
        self.message = message


def _response(status, body):
    return {
        "statusCode": status,
        "headers": dict(CORS_HEADERS),
        "body": db.dumps(body),
    }


def _list_response(items, last_evaluated_key, status=200):
    last_key = db.dumps(last_evaluated_key) if last_evaluated_key else None
    return _response(status, {"data": items, "last_key": last_key})


def _error(status, message):
    return _response(status, {"error": message})


# --------------------------------------------------------------------------- #
# Request parsing helpers
# --------------------------------------------------------------------------- #
def _parse_body(event):
    raw = event.get("body")
    if raw is None or raw == "":
        return {}
    if event.get("isBase64Encoded"):
        try:
            raw = base64.b64decode(raw).decode("utf-8")
        except (binascii.Error, UnicodeDecodeError) as exc:
            raise ApiError(400, f"invalid base64 body: {exc}")
    try:
        parsed = json.loads(raw, parse_float=decimal.Decimal)
    except json.JSONDecodeError as exc:
        raise ApiError(400, f"invalid JSON body: {exc}")
    if not isinstance(parsed, dict):
        raise ApiError(400, "request body must be a JSON object")
    return parsed


def _parse_limit(qs):
    raw = qs.get("limit")
    if raw is None:
        return DEFAULT_LIMIT
    try:
        limit = int(raw)
    except (TypeError, ValueError):
        raise ApiError(400, "limit must be an integer")
    if limit <= 0:
        raise ApiError(400, "limit must be positive")
    return limit


def _parse_last_key(qs):
    raw = qs.get("last_key")
    if not raw:
        return None
    try:
        return json.loads(raw)
    except json.JSONDecodeError as exc:
        raise ApiError(400, f"invalid last_key: {exc}")


# --------------------------------------------------------------------------- #
# Route handlers
# --------------------------------------------------------------------------- #
def list_accounts(match, event, qs, body):
    items, last = db.paginated_scan(
        db.accounts_table(),
        limit=_parse_limit(qs),
        exclusive_start_key=_parse_last_key(qs),
    )
    return _list_response(items, last)


def get_account(match, event, qs, body):
    resp = db.accounts_table().get_item(Key={"acct_id": match.group("acct_id")})
    item = resp.get("Item")
    if not item:
        raise ApiError(404, "account not found")
    return _response(200, {"data": item, "last_key": None})


def update_account(match, event, qs, body):
    acct_id = match.group("acct_id")
    if not body:
        raise ApiError(400, "no fields to update")

    set_clauses = []
    names = {}
    values = {}
    for idx, (field, value) in enumerate(body.items()):
        if field == "acct_id":
            continue
        name_token = f"#f{idx}"
        value_token = f":v{idx}"
        names[name_token] = field
        values[value_token] = value
        set_clauses.append(f"{name_token} = {value_token}")

    if not set_clauses:
        raise ApiError(400, "no updatable fields provided")

    resp = db.accounts_table().update_item(
        Key={"acct_id": acct_id},
        UpdateExpression="SET " + ", ".join(set_clauses),
        ExpressionAttributeNames=names,
        ExpressionAttributeValues=values,
        ReturnValues="ALL_NEW",
    )
    return _response(200, {"data": resp.get("Attributes", {}), "last_key": None})


def list_customers(match, event, qs, body):
    items, last = db.paginated_scan(
        db.customers_table(),
        limit=_parse_limit(qs),
        exclusive_start_key=_parse_last_key(qs),
    )
    return _list_response(items, last)


def get_customer(match, event, qs, body):
    resp = db.customers_table().get_item(Key={"cust_id": match.group("cust_id")})
    item = resp.get("Item")
    if not item:
        raise ApiError(404, "customer not found")
    return _response(200, {"data": item, "last_key": None})


def list_cards(match, event, qs, body):
    acct_id = qs.get("acct_id")
    limit = _parse_limit(qs)
    start_key = _parse_last_key(qs)
    if acct_id:
        items, last = db.paginated_query(
            db.cards_table(),
            limit=limit,
            exclusive_start_key=start_key,
            IndexName="acct_id-index",
            KeyConditionExpression=Key("acct_id").eq(acct_id),
        )
    else:
        items, last = db.paginated_scan(
            db.cards_table(), limit=limit, exclusive_start_key=start_key
        )
    return _list_response(items, last)


def get_card(match, event, qs, body):
    resp = db.cards_table().get_item(Key={"card_num": match.group("card_num")})
    item = resp.get("Item")
    if not item:
        raise ApiError(404, "card not found")
    return _response(200, {"data": item, "last_key": None})


def list_transactions(match, event, qs, body):
    acct_id = qs.get("acct_id")
    card_num = qs.get("card_num")
    limit = _parse_limit(qs)
    start_key = _parse_last_key(qs)
    table = db.transactions_table()
    if acct_id:
        items, last = db.paginated_query(
            table,
            limit=limit,
            exclusive_start_key=start_key,
            IndexName="acct_id-index",
            KeyConditionExpression=Key("acct_id").eq(acct_id),
        )
    elif card_num:
        items, last = db.paginated_query(
            table,
            limit=limit,
            exclusive_start_key=start_key,
            IndexName="card_num-index",
            KeyConditionExpression=Key("card_num").eq(card_num),
        )
    else:
        items, last = db.paginated_scan(
            table, limit=limit, exclusive_start_key=start_key
        )
    return _list_response(items, last)


def get_transaction(match, event, qs, body):
    tran_id = match.group("tran_id")
    items, _ = db.paginated_query(
        db.transactions_table(),
        KeyConditionExpression=Key("tran_id").eq(tran_id),
    )
    if not items:
        raise ApiError(404, "transaction not found")
    return _response(200, {"data": items, "last_key": None})


def create_transaction(match, event, qs, body):
    if not body:
        raise ApiError(400, "transaction body required")
    card_num = body.get("card_num")
    if not card_num:
        raise ApiError(400, "card_num is required")

    item = dict(body)
    item.setdefault("tran_id", uuid.uuid4().hex[:16])
    if not item.get("tran_orig_ts"):
        item["tran_orig_ts"] = datetime.now(timezone.utc).strftime(
            "%Y-%m-%d %H:%M:%S.%f"
        )

    # Enrich acct_id via the card-xref table when not supplied.
    if not item.get("acct_id"):
        xref = db.card_xref_table().get_item(Key={"card_num": card_num}).get("Item")
        if xref and xref.get("acct_id"):
            item["acct_id"] = xref["acct_id"]

    db.transactions_table().put_item(Item=item)
    return _response(201, {"data": item, "last_key": None})


def list_transaction_types(match, event, qs, body):
    items = db.scan_all(db.transaction_types_table())
    return _response(200, {"data": items, "last_key": None})


def list_transaction_categories(match, event, qs, body):
    items = db.scan_all(db.transaction_categories_table())
    return _response(200, {"data": items, "last_key": None})


def process_daily_batch(match, event, qs, body):
    summary = run_daily_batch()
    return _response(200, {"data": summary, "last_key": None})


def get_dashboard(match, event, qs, body):
    accounts_tbl = db.accounts_table()

    total_balance = decimal.Decimal("0")
    for acct in db.scan_all(accounts_tbl, ProjectionExpression="curr_bal"):
        raw = acct.get("curr_bal")
        if raw is not None:
            total_balance += decimal.Decimal(str(raw))

    recent, _ = db.paginated_scan(db.transactions_table(), limit=10)

    data = {
        "accounts": db.count_all(accounts_tbl),
        "customers": db.count_all(db.customers_table()),
        "cards": db.count_all(db.cards_table()),
        "transactions": db.count_all(db.transactions_table()),
        "total_balance": str(total_balance),
        "recent_transactions": recent,
    }
    return _response(200, {"data": data, "last_key": None})


# --------------------------------------------------------------------------- #
# Batch processing (replicates CBTRN01C/02C/03C, see API_CONTRACT.md §3)
# --------------------------------------------------------------------------- #
def run_daily_batch():
    transactions_tbl = db.transactions_table()
    accounts_tbl = db.accounts_table()

    unprocessed = db.scan_all(
        transactions_tbl,
        FilterExpression="attribute_not_exists(batch_processed)",
    )

    processed = 0
    skipped = 0
    errors = 0

    for tran in unprocessed:
        acct_id = tran.get("acct_id")
        try:
            if not acct_id:
                skipped += 1
                continue

            account = accounts_tbl.get_item(Key={"acct_id": acct_id}).get("Item")
            if not account:
                skipped += 1
                continue

            amt = decimal.Decimal(str(tran.get("amt", "0")))
            curr_bal = decimal.Decimal(str(account.get("curr_bal", "0")))
            curr_cyc_credit = decimal.Decimal(str(account.get("curr_cyc_credit", "0")))
            curr_cyc_debit = decimal.Decimal(str(account.get("curr_cyc_debit", "0")))

            type_cd = tran.get("type_cd")
            if type_cd in CREDIT_TYPE_CODES:
                curr_bal += amt
                curr_cyc_credit += amt
            else:  # "01" Purchase (and any other debit type)
                curr_bal -= amt
                curr_cyc_debit += amt

            accounts_tbl.update_item(
                Key={"acct_id": acct_id},
                UpdateExpression=(
                    "SET curr_bal = :b, curr_cyc_credit = :c, curr_cyc_debit = :d"
                ),
                ExpressionAttributeValues={
                    ":b": str(curr_bal),
                    ":c": str(curr_cyc_credit),
                    ":d": str(curr_cyc_debit),
                },
            )

            transactions_tbl.update_item(
                Key={
                    "tran_id": tran["tran_id"],
                    "tran_orig_ts": tran["tran_orig_ts"],
                },
                UpdateExpression="SET batch_processed = :p, tran_proc_ts = :t",
                ExpressionAttributeValues={
                    ":p": True,
                    ":t": datetime.now(timezone.utc).isoformat(),
                },
            )
            processed += 1
        except Exception:  # noqa: BLE001 - count and continue per contract
            errors += 1

    return {
        "transactions_processed": processed,
        "skipped": skipped,
        "errors": errors,
    }


# --------------------------------------------------------------------------- #
# Router
# --------------------------------------------------------------------------- #
ROUTES = [
    ("GET", re.compile(r"^/accounts$"), list_accounts),
    ("GET", re.compile(r"^/accounts/(?P<acct_id>[^/]+)$"), get_account),
    ("PUT", re.compile(r"^/accounts/(?P<acct_id>[^/]+)$"), update_account),
    ("GET", re.compile(r"^/customers$"), list_customers),
    ("GET", re.compile(r"^/customers/(?P<cust_id>[^/]+)$"), get_customer),
    ("GET", re.compile(r"^/cards$"), list_cards),
    ("GET", re.compile(r"^/cards/(?P<card_num>[^/]+)$"), get_card),
    ("GET", re.compile(r"^/transaction-types$"), list_transaction_types),
    ("GET", re.compile(r"^/transaction-categories$"), list_transaction_categories),
    ("GET", re.compile(r"^/transactions$"), list_transactions),
    ("GET", re.compile(r"^/transactions/(?P<tran_id>[^/]+)$"), get_transaction),
    ("POST", re.compile(r"^/transactions$"), create_transaction),
    ("POST", re.compile(r"^/batch/process-daily$"), process_daily_batch),
    ("GET", re.compile(r"^/dashboard$"), get_dashboard),
]


def _strip_stage(event, raw_path):
    stage = event.get("requestContext", {}).get("stage", "")
    if stage and raw_path.startswith(f"/{stage}"):
        return raw_path[len(f"/{stage}"):] or "/"
    return raw_path


def handler(event, context):
    request_context = event.get("requestContext", {})
    http = request_context.get("http", {})
    method = http.get("method", "")
    raw_path = http.get("path", "")
    path = _strip_stage(event, raw_path)

    if method == "OPTIONS":
        return {"statusCode": 204, "headers": dict(CORS_HEADERS), "body": ""}

    qs = event.get("queryStringParameters") or {}

    try:
        body = _parse_body(event) if method in ("POST", "PUT") else {}
        for route_method, pattern, func in ROUTES:
            if route_method != method:
                continue
            match = pattern.match(path)
            if match:
                return func(match, event, qs, body)
        return _error(404, f"no route for {method} {path}")
    except ApiError as exc:
        return _error(exc.status, exc.message)
    except Exception as exc:  # noqa: BLE001 - generic 500 per contract
        return _error(500, f"internal error: {exc}")
