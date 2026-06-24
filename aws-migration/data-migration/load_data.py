#!/usr/bin/env python3
"""Load CardDemo COBOL flat-file data into DynamoDB tables.

Usage:
    python load_data.py [--region us-east-1] [--prefix carddemo]

The script expects the ASCII data files to live at ``../../app/data/ASCII/``
relative to this file (the standard repo layout).
"""

import argparse
import os
import sys

import boto3

from parsers import (
    load_xref_map,
    parse_accounts,
    parse_card_xref,
    parse_cards,
    parse_customers,
    parse_transaction_categories,
    parse_transaction_types,
    parse_transactions,
)

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.normpath(os.path.join(SCRIPT_DIR, "..", "..", "app", "data", "ASCII"))


def _batch_load(table, records):
    """Write *records* to *table* using the DynamoDB batch_writer."""
    count = 0
    with table.batch_writer() as batch:
        for rec in records:
            batch.put_item(Item=rec)
            count += 1
    return count


def main():
    parser = argparse.ArgumentParser(description="Load CardDemo data into DynamoDB")
    parser.add_argument("--region", default="us-east-1", help="AWS region (default: us-east-1)")
    parser.add_argument("--prefix", default="carddemo", help="DynamoDB table name prefix (default: carddemo)")
    args = parser.parse_args()

    dynamodb = boto3.resource("dynamodb", region_name=args.region)

    def table(suffix):
        return dynamodb.Table(f"{args.prefix}-{suffix}")

    # ------------------------------------------------------------------
    # 1. Accounts
    # ------------------------------------------------------------------
    filepath = os.path.join(DATA_DIR, "acctdata.txt")
    count = _batch_load(table("accounts"), parse_accounts(filepath))
    print(f"accounts: {count} records loaded")

    # ------------------------------------------------------------------
    # 2. Customers
    # ------------------------------------------------------------------
    filepath = os.path.join(DATA_DIR, "custdata.txt")
    count = _batch_load(table("customers"), parse_customers(filepath))
    print(f"customers: {count} records loaded")

    # ------------------------------------------------------------------
    # 3. Cards
    # ------------------------------------------------------------------
    filepath = os.path.join(DATA_DIR, "carddata.txt")
    count = _batch_load(table("cards"), parse_cards(filepath))
    print(f"cards: {count} records loaded")

    # ------------------------------------------------------------------
    # 4. Card cross-reference
    # ------------------------------------------------------------------
    xref_path = os.path.join(DATA_DIR, "cardxref.txt")
    count = _batch_load(table("card-xref"), parse_card_xref(xref_path))
    print(f"card-xref: {count} records loaded")

    # ------------------------------------------------------------------
    # 5. Transactions (enrich with acct_id via xref lookup)
    # ------------------------------------------------------------------
    xref_map = load_xref_map(xref_path)
    filepath = os.path.join(DATA_DIR, "dailytran.txt")
    count = _batch_load(table("transactions"), parse_transactions(filepath, xref_map))
    print(f"transactions: {count} records loaded")

    # ------------------------------------------------------------------
    # 6. Transaction types
    # ------------------------------------------------------------------
    filepath = os.path.join(DATA_DIR, "trantype.txt")
    count = _batch_load(table("transaction-types"), parse_transaction_types(filepath))
    print(f"transaction-types: {count} records loaded")

    # ------------------------------------------------------------------
    # 7. Transaction categories
    # ------------------------------------------------------------------
    filepath = os.path.join(DATA_DIR, "trancatg.txt")
    count = _batch_load(table("transaction-categories"), parse_transaction_categories(filepath))
    print(f"transaction-categories: {count} records loaded")

    print("\nDone — all tables loaded.")


if __name__ == "__main__":
    main()
