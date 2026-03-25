#!/usr/bin/env python3
"""
Compare Java application output against golden JSON reference files.

Loads a golden JSON file produced by generate_golden_files.py and compares
it field-by-field against a result set from the migrated Java application.
Produces a detailed report of matches, mismatches, and missing records.

Usage:
    python3 golden_file_comparator.py --golden PATH --actual PATH [--output PATH]
"""

import json
import sys
import argparse
from decimal import Decimal, InvalidOperation


def load_json(filepath: str) -> dict:
    """Load and return a JSON file."""
    with open(filepath, "r") as f:
        return json.load(f)


def normalize_value(value: str, field_def: dict) -> str:
    """Normalize a field value for comparison.

    Args:
        value: The raw string value.
        field_def: Field definition dict with 'type' and 'decimal_places'.

    Returns:
        Normalized string for comparison.
    """
    if value is None:
        return ""

    value = str(value).strip()

    if field_def.get("type") == "alpha":
        return value.rstrip()

    if field_def.get("type") in ("numeric", "signed_decimal"):
        try:
            decimal_places = field_def.get("decimal_places", 0)
            d = Decimal(value)
            if decimal_places > 0:
                fmt = f"{{:.{decimal_places}f}}"
                return fmt.format(d)
            return str(int(d))
        except (InvalidOperation, ValueError):
            return value

    return value


def compare_records(
    golden_record: dict,
    actual_record: dict,
    field_definitions: list,
    record_index: int,
) -> list:
    """Compare two records field-by-field.

    Args:
        golden_record: Expected record from golden file.
        actual_record: Actual record from Java output.
        field_definitions: List of field definition dicts.
        record_index: Index of the record for reporting.

    Returns:
        List of mismatch dictionaries.
    """
    mismatches = []

    for field_def in field_definitions:
        name = field_def["name"]
        if name == "FILLER":
            continue
        if name == "_line_number":
            continue

        golden_val = golden_record.get(name, "")
        actual_val = actual_record.get(name, "")

        golden_norm = normalize_value(golden_val, field_def)
        actual_norm = normalize_value(actual_val, field_def)

        if golden_norm != actual_norm:
            mismatches.append(
                {
                    "record": record_index,
                    "field": name,
                    "expected": golden_norm,
                    "actual": actual_norm,
                    "type": field_def.get("type", "unknown"),
                }
            )

    return mismatches


def find_key_field(field_definitions: list) -> str:
    """Identify the primary key field (first non-FILLER field)."""
    for fd in field_definitions:
        if fd["name"] != "FILLER":
            return fd["name"]
    return None


def compare_files(golden_data: dict, actual_data: dict) -> dict:
    """Compare a golden reference against actual output.

    Args:
        golden_data: Parsed golden JSON (with metadata and records).
        actual_data: Parsed actual JSON (same structure, or list of records).

    Returns:
        Comparison report dictionary.
    """
    field_defs = golden_data["metadata"]["field_definitions"]
    golden_records = golden_data["records"]

    # Support both wrapped format (with metadata) and plain list
    if isinstance(actual_data, dict) and "records" in actual_data:
        actual_records = actual_data["records"]
    elif isinstance(actual_data, list):
        actual_records = actual_data
    else:
        actual_records = []

    key_field = find_key_field(field_defs)

    # Build lookup by key for actual records
    actual_by_key = {}
    if key_field:
        for rec in actual_records:
            key = str(rec.get(key_field, "")).strip()
            actual_by_key[key] = rec

    all_mismatches = []
    missing_records = []
    extra_records = []
    matched_keys = set()

    for idx, golden_rec in enumerate(golden_records):
        golden_key = str(golden_rec.get(key_field, "")).strip() if key_field else str(idx)

        if key_field and golden_key in actual_by_key:
            actual_rec = actual_by_key[golden_key]
            matched_keys.add(golden_key)
            mismatches = compare_records(golden_rec, actual_rec, field_defs, idx)
            all_mismatches.extend(mismatches)
        elif idx < len(actual_records):
            # Fall back to positional comparison
            actual_rec = actual_records[idx]
            mismatches = compare_records(golden_rec, actual_rec, field_defs, idx)
            all_mismatches.extend(mismatches)
        else:
            missing_records.append(
                {
                    "record": idx,
                    "key_field": key_field,
                    "key_value": golden_key,
                }
            )

    # Find extra records in actual that aren't in golden
    if key_field:
        for rec in actual_records:
            key = str(rec.get(key_field, "")).strip()
            if key not in matched_keys and key not in [
                str(gr.get(key_field, "")).strip() for gr in golden_records
            ]:
                extra_records.append(
                    {
                        "key_field": key_field,
                        "key_value": key,
                    }
                )

    report = {
        "source_file": golden_data["metadata"]["source_file"],
        "copybook": golden_data["metadata"]["copybook"],
        "golden_record_count": len(golden_records),
        "actual_record_count": len(actual_records),
        "mismatches": all_mismatches,
        "missing_records": missing_records,
        "extra_records": extra_records,
        "mismatch_count": len(all_mismatches),
        "pass": len(all_mismatches) == 0
        and len(missing_records) == 0
        and len(extra_records) == 0,
    }

    return report


def main():
    parser = argparse.ArgumentParser(
        description="Compare Java output against golden JSON reference files."
    )
    parser.add_argument(
        "--golden", required=True, help="Path to golden JSON file."
    )
    parser.add_argument(
        "--actual", required=True, help="Path to actual output JSON file."
    )
    parser.add_argument(
        "--output",
        default=None,
        help="Path to write comparison report JSON (default: stdout).",
    )
    args = parser.parse_args()

    golden_data = load_json(args.golden)
    actual_data = load_json(args.actual)

    report = compare_files(golden_data, actual_data)

    report_json = json.dumps(report, indent=2)

    if args.output:
        with open(args.output, "w") as f:
            f.write(report_json)
        print(f"Report written to {args.output}")
    else:
        print(report_json)

    status = "PASS" if report["pass"] else "FAIL"
    print(f"\nResult: {status}", file=sys.stderr)
    print(
        f"  Records: {report['golden_record_count']} golden, "
        f"{report['actual_record_count']} actual",
        file=sys.stderr,
    )
    print(f"  Mismatches: {report['mismatch_count']}", file=sys.stderr)
    print(f"  Missing:    {len(report['missing_records'])}", file=sys.stderr)
    print(f"  Extra:      {len(report['extra_records'])}", file=sys.stderr)

    sys.exit(0 if report["pass"] else 1)


if __name__ == "__main__":
    main()
