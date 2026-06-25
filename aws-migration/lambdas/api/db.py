"""DynamoDB helpers for the CardDemo API Lambda.

Provides a shared boto3 resource, table accessors keyed off the Terraform-set
environment variables (see API_CONTRACT.md §6), reusable paginated scan/query
helpers, and a ``DecimalEncoder`` that serializes ``decimal.Decimal`` as strings
to preserve monetary fidelity (all money is stored as DynamoDB strings).
"""

import decimal
import json
import os

import boto3

REGION = os.environ.get("AWS_REGION", "us-east-1")

_dynamodb = boto3.resource("dynamodb", region_name=REGION)

# Environment variable names set on the Lambda by Terraform.
ACCOUNTS_TABLE = "ACCOUNTS_TABLE"
CUSTOMERS_TABLE = "CUSTOMERS_TABLE"
CARDS_TABLE = "CARDS_TABLE"
CARD_XREF_TABLE = "CARD_XREF_TABLE"
TRANSACTIONS_TABLE = "TRANSACTIONS_TABLE"
TRANSACTION_TYPES_TABLE = "TRANSACTION_TYPES_TABLE"
TRANSACTION_CATEGORIES_TABLE = "TRANSACTION_CATEGORIES_TABLE"


def _table(env_var):
    """Return the DynamoDB Table resource named by ``env_var``."""
    name = os.environ[env_var]
    return _dynamodb.Table(name)


def accounts_table():
    return _table(ACCOUNTS_TABLE)


def customers_table():
    return _table(CUSTOMERS_TABLE)


def cards_table():
    return _table(CARDS_TABLE)


def card_xref_table():
    return _table(CARD_XREF_TABLE)


def transactions_table():
    return _table(TRANSACTIONS_TABLE)


def transaction_types_table():
    return _table(TRANSACTION_TYPES_TABLE)


def transaction_categories_table():
    return _table(TRANSACTION_CATEGORIES_TABLE)


class DecimalEncoder(json.JSONEncoder):
    """JSON encoder that renders ``decimal.Decimal`` values as strings."""

    def default(self, o):
        if isinstance(o, decimal.Decimal):
            return str(o)
        return super().default(o)


def dumps(obj):
    """Serialize ``obj`` to JSON using ``DecimalEncoder``."""
    return json.dumps(obj, cls=DecimalEncoder)


def paginated_scan(table, limit=None, exclusive_start_key=None, **scan_kwargs):
    """Scan a single page (when ``limit`` set) or follow pages until exhausted.

    Returns ``(items, last_key)`` where ``last_key`` is the raw
    ``LastEvaluatedKey`` dict (or ``None`` when there are no more pages).
    """
    kwargs = dict(scan_kwargs)
    if limit is not None:
        kwargs["Limit"] = limit
    if exclusive_start_key:
        kwargs["ExclusiveStartKey"] = exclusive_start_key

    resp = table.scan(**kwargs)
    items = resp.get("Items", [])
    last_key = resp.get("LastEvaluatedKey")
    return items, last_key


def paginated_query(table, limit=None, exclusive_start_key=None, **query_kwargs):
    """Query a single page (when ``limit`` set) and return ``(items, last_key)``."""
    kwargs = dict(query_kwargs)
    if limit is not None:
        kwargs["Limit"] = limit
    if exclusive_start_key:
        kwargs["ExclusiveStartKey"] = exclusive_start_key

    resp = table.query(**kwargs)
    items = resp.get("Items", [])
    last_key = resp.get("LastEvaluatedKey")
    return items, last_key


def scan_all(table, **scan_kwargs):
    """Scan a table fully, following ``LastEvaluatedKey``; returns all items."""
    items = []
    start_key = None
    while True:
        kwargs = dict(scan_kwargs)
        if start_key:
            kwargs["ExclusiveStartKey"] = start_key
        resp = table.scan(**kwargs)
        items.extend(resp.get("Items", []))
        start_key = resp.get("LastEvaluatedKey")
        if not start_key:
            break
    return items


def count_all(table, **scan_kwargs):
    """Return the total item count via paginated ``Select="COUNT"`` scans."""
    total = 0
    start_key = None
    while True:
        kwargs = dict(scan_kwargs)
        kwargs["Select"] = "COUNT"
        if start_key:
            kwargs["ExclusiveStartKey"] = start_key
        resp = table.scan(**kwargs)
        total += resp.get("Count", 0)
        start_key = resp.get("LastEvaluatedKey")
        if not start_key:
            break
    return total
