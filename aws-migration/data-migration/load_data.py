"""Load the parsed CardDemo flat files into DynamoDB.

Parses all seven COBOL flat files via ``parsers.py`` and batch-writes each into
its corresponding ``carddemo-*`` DynamoDB table using ``table.batch_writer()``.
Transactions are enriched with ``acct_id`` (looked up from the card-xref data by
``card_num``) before being written, per API_CONTRACT.md §2.

Table names default to the ``carddemo-*`` names but can be overridden via the
environment variables documented in API_CONTRACT.md §6.
"""

from __future__ import annotations

import argparse
import os

import boto3

import parsers

# Default table names; each overridable via the matching environment variable.
TABLE_ENV = {
    "accounts": ("ACCOUNTS_TABLE", "carddemo-accounts"),
    "customers": ("CUSTOMERS_TABLE", "carddemo-customers"),
    "cards": ("CARDS_TABLE", "carddemo-cards"),
    "card_xref": ("CARD_XREF_TABLE", "carddemo-card-xref"),
    "transactions": ("TRANSACTIONS_TABLE", "carddemo-transactions"),
    "transaction_types": ("TRANSACTION_TYPES_TABLE", "carddemo-transaction-types"),
    "transaction_categories": ("TRANSACTION_CATEGORIES_TABLE",
                               "carddemo-transaction-categories"),
}


def table_name(key: str) -> str:
    env_var, default = TABLE_ENV[key]
    return os.environ.get(env_var, default)


def enrich_transactions(transactions: list[dict[str, str]],
                        card_xref: list[dict[str, str]]) -> list[dict[str, str]]:
    """Add ``acct_id`` to each transaction via card-xref lookup on ``card_num``."""
    acct_by_card = {x["card_num"]: x["acct_id"] for x in card_xref}
    for tran in transactions:
        acct_id = acct_by_card.get(tran["card_num"])
        if acct_id is not None:
            tran["acct_id"] = acct_id
    return transactions


def batch_write(dynamodb, key: str, records: list[dict[str, str]]) -> int:
    """Batch-write records into the table for ``key``; return the count written."""
    table = dynamodb.Table(table_name(key))
    with table.batch_writer() as writer:
        for record in records:
            writer.put_item(Item=record)
    return len(records)


def main() -> None:
    ap = argparse.ArgumentParser(description="Load CardDemo flat files into DynamoDB.")
    ap.add_argument("--data-dir",
                    default=os.path.join(os.path.dirname(__file__),
                                         "..", "..", "app", "data", "ASCII"),
                    help="directory containing the ASCII flat files")
    ap.add_argument("--region", default="us-east-1", help="AWS region")
    args = ap.parse_args()

    def path(name: str) -> str:
        return os.path.join(args.data_dir, name)

    accounts = parsers.parse_accounts(path("acctdata.txt"))
    customers = parsers.parse_customers(path("custdata.txt"))
    cards = parsers.parse_cards(path("carddata.txt"))
    card_xref = parsers.parse_card_xref(path("cardxref.txt"))
    transactions = parsers.parse_transactions(path("dailytran.txt"))
    transaction_types = parsers.parse_transaction_types(path("trantype.txt"))
    transaction_categories = parsers.parse_transaction_categories(path("trancatg.txt"))

    enrich_transactions(transactions, card_xref)

    dynamodb = boto3.resource("dynamodb", region_name=args.region)

    datasets = [
        ("accounts", accounts),
        ("customers", customers),
        ("cards", cards),
        ("card_xref", card_xref),
        ("transactions", transactions),
        ("transaction_types", transaction_types),
        ("transaction_categories", transaction_categories),
    ]

    print("Loading CardDemo data into DynamoDB...")
    summary: list[tuple[str, int]] = []
    for key, records in datasets:
        written = batch_write(dynamodb, key, records)
        name = table_name(key)
        print(f"  {name}: {written} items")
        summary.append((name, written))

    total = sum(count for _, count in summary)
    print(f"Done. {total} items written across {len(summary)} tables.")


if __name__ == "__main__":
    main()
