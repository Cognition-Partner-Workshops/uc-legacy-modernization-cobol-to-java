"""DynamoDB helper module for CardDemo Lambda API."""

import os
import boto3

_TABLE_PREFIX = os.environ.get("TABLE_PREFIX", "carddemo")
_resource = boto3.resource("dynamodb")

TABLE_NAMES = {
    "accounts": f"{_TABLE_PREFIX}-accounts",
    "customers": f"{_TABLE_PREFIX}-customers",
    "cards": f"{_TABLE_PREFIX}-cards",
    "card_xref": f"{_TABLE_PREFIX}-card-xref",
    "transactions": f"{_TABLE_PREFIX}-transactions",
    "transaction_types": f"{_TABLE_PREFIX}-transaction-types",
    "transaction_categories": f"{_TABLE_PREFIX}-transaction-categories",
}


def _table(name):
    return _resource.Table(TABLE_NAMES[name])


def accounts_table():
    return _table("accounts")


def customers_table():
    return _table("customers")


def cards_table():
    return _table("cards")


def card_xref_table():
    return _table("card_xref")


def transactions_table():
    return _table("transactions")


def transaction_types_table():
    return _table("transaction_types")


def transaction_categories_table():
    return _table("transaction_categories")


def scan_paginated(table, limit=20, last_key=None):
    """Scan a table with pagination support."""
    params = {"Limit": limit}
    if last_key:
        params["ExclusiveStartKey"] = last_key
    response = table.scan(**params)
    result = {"items": response.get("Items", [])}
    if "LastEvaluatedKey" in response:
        result["last_key"] = response["LastEvaluatedKey"]
    return result


def query_paginated(table, key_condition, expr_values, limit=20, last_key=None, index_name=None):
    """Query a table or GSI with pagination support."""
    params = {
        "KeyConditionExpression": key_condition,
        "ExpressionAttributeValues": expr_values,
        "Limit": limit,
    }
    if index_name:
        params["IndexName"] = index_name
    if last_key:
        params["ExclusiveStartKey"] = last_key
    response = table.query(**params)
    result = {"items": response.get("Items", [])}
    if "LastEvaluatedKey" in response:
        result["last_key"] = response["LastEvaluatedKey"]
    return result
