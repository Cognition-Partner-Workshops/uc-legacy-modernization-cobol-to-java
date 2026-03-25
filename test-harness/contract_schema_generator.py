#!/usr/bin/env python3
"""
Copybook -> JSON Schema generator.

Reads the copybook field definitions from ``cobol_field_parser.py`` and
produces JSON Schema documents that describe the expected shape of Java API
responses for each data entity.

Usage:
    python contract_schema_generator.py [--output-dir contracts/]
"""

from __future__ import annotations

import json
import sys
from pathlib import Path
from typing import Any

from cobol_field_parser import (
    ACCOUNT_LAYOUT,
    CARD_LAYOUT,
    CARD_XREF_LAYOUT,
    CUSTOMER_LAYOUT,
    DAILY_TRAN_LAYOUT,
    DIS_GROUP_LAYOUT,
    SEC_USER_LAYOUT,
    TRAN_CAT_BAL_LAYOUT,
    TRAN_CAT_LAYOUT,
    TRAN_LAYOUT,
    TRAN_TYPE_LAYOUT,
    FieldDef,
)


def _field_to_json_schema(fdef: FieldDef) -> dict[str, Any]:
    """Convert a single FieldDef into a JSON Schema property definition."""
    if fdef.field_type == "alpha":
        return {
            "type": "string",
            "maxLength": fdef.length,
            "description": f"COBOL PIC {fdef.pic} (alpha, {fdef.length} bytes)",
        }
    if fdef.field_type == "numeric":
        return {
            "type": "integer",
            "description": f"COBOL PIC {fdef.pic} (unsigned numeric, {fdef.length} digits)",
        }
    if fdef.field_type == "signed_numeric":
        if fdef.decimal_places > 0:
            return {
                "type": "number",
                "description": (
                    f"COBOL PIC {fdef.pic} "
                    f"(signed numeric, {fdef.decimal_places} decimal places)"
                ),
            }
        return {
            "type": "integer",
            "description": f"COBOL PIC {fdef.pic} (signed numeric)",
        }
    return {"type": "string"}


def layout_to_schema(
    layout: list[FieldDef],
    title: str,
    description: str,
) -> dict[str, Any]:
    """Convert a full record layout into a JSON Schema document."""
    properties: dict[str, Any] = {}
    required: list[str] = []

    for fdef in layout:
        if fdef.name == "FILLER":
            continue
        prop_name = fdef.name
        properties[prop_name] = _field_to_json_schema(fdef)
        required.append(prop_name)

    return {
        "$schema": "http://json-schema.org/draft-07/schema#",
        "title": title,
        "description": description,
        "type": "object",
        "properties": properties,
        "required": required,
        "additionalProperties": True,
    }


# ---------------------------------------------------------------------------
# Schema definitions
# ---------------------------------------------------------------------------
SCHEMAS: list[tuple[str, list[FieldDef], str, str]] = [
    (
        "account_record.schema.json",
        ACCOUNT_LAYOUT,
        "AccountRecord",
        "Account master record (CVACT01Y, 300 bytes)",
    ),
    (
        "card_record.schema.json",
        CARD_LAYOUT,
        "CardRecord",
        "Card master record (CVACT02Y, 150 bytes)",
    ),
    (
        "customer_record.schema.json",
        CUSTOMER_LAYOUT,
        "CustomerRecord",
        "Customer master record (CVCUS01Y, 500 bytes)",
    ),
    (
        "card_xref_record.schema.json",
        CARD_XREF_LAYOUT,
        "CardXrefRecord",
        "Card cross-reference record (CVACT03Y, 50 bytes)",
    ),
    (
        "tran_record.schema.json",
        TRAN_LAYOUT,
        "TransactionRecord",
        "Transaction record (CVTRA05Y, 350 bytes)",
    ),
    (
        "daily_tran_record.schema.json",
        DAILY_TRAN_LAYOUT,
        "DailyTransactionRecord",
        "Daily transaction record (CVTRA06Y, 350 bytes)",
    ),
    (
        "tran_type_record.schema.json",
        TRAN_TYPE_LAYOUT,
        "TransactionTypeRecord",
        "Transaction type record (CVTRA03Y, 60 bytes)",
    ),
    (
        "tran_cat_record.schema.json",
        TRAN_CAT_LAYOUT,
        "TransactionCategoryRecord",
        "Transaction category record (CVTRA04Y, 60 bytes)",
    ),
    (
        "dis_group_record.schema.json",
        DIS_GROUP_LAYOUT,
        "DisclosureGroupRecord",
        "Disclosure group record (CVTRA02Y, 50 bytes)",
    ),
    (
        "tran_cat_bal_record.schema.json",
        TRAN_CAT_BAL_LAYOUT,
        "TransactionCategoryBalanceRecord",
        "Transaction category balance record (CVTRA01Y, 50 bytes)",
    ),
    (
        "sec_user_data.schema.json",
        SEC_USER_LAYOUT,
        "SecurityUserData",
        "Security / user record (CSUSR01Y, 80 bytes)",
    ),
]


def generate_schemas(output_dir: str | Path) -> None:
    """Generate all JSON Schema files."""
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    for filename, layout, title, desc in SCHEMAS:
        schema = layout_to_schema(layout, title, desc)
        out_path = output_dir / filename
        with open(out_path, "w", encoding="utf-8") as fh:
            json.dump(schema, fh, indent=2, ensure_ascii=False)
        print(f"  OK  {filename}")


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------
def main() -> None:
    output_dir = Path(__file__).resolve().parent / "contracts"
    if len(sys.argv) > 1 and sys.argv[1] == "--output-dir":
        output_dir = Path(sys.argv[2])

    print("Generating contract JSON Schema files ...")
    generate_schemas(output_dir)
    print("Done.")


if __name__ == "__main__":
    main()
