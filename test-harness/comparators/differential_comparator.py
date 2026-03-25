#!/usr/bin/env python3
"""
Compare legacy COBOL batch outputs against migrated Java batch outputs.

Performs field-by-field differential comparison of two result sets to detect
behavioral divergence between the legacy and modern systems.

Usage:
    python3 differential_comparator.py --legacy PATH --modern PATH [--output PATH]
        [--ignore-fields FIELD1,FIELD2] [--timestamp-tolerance SECONDS]
"""

import json
import sys
import argparse
from datetime import datetime


DEFAULT_TIMESTAMP_TOLERANCE_SECONDS = 5
TIMESTAMP_FORMATS = [
    "%Y-%m-%d %H:%M:%S.%f",
    "%Y-%m-%d %H:%M:%S",
    "%Y-%m-%d",
    "%Y%m%d%H%M%S",
]


def parse_timestamp(value: str) -> datetime:
    """Try to parse a timestamp string using known formats.

    Returns None if no format matches.
    """
    value = value.strip()
    for fmt in TIMESTAMP_FORMATS:
        try:
            return datetime.strptime(value, fmt)
        except ValueError:
            continue
    return None


def is_timestamp_field(field_name: str) -> bool:
    """Heuristic to detect timestamp fields by name."""
    ts_indicators = ["TS", "DATE", "TIME", "TIMESTAMP", "DT"]
    upper = field_name.upper().replace("-", "_")
    return any(ind in upper for ind in ts_indicators)


def compare_values(
    field_name: str,
    legacy_val: str,
    modern_val: str,
    ignore_fields: set,
    timestamp_tolerance: int,
) -> dict:
    """Compare a single field value between legacy and modern.

    Returns None if values match, or a mismatch dict if they differ.
    """
    if field_name in ignore_fields:
        return None

    legacy_str = str(legacy_val).strip() if legacy_val is not None else ""
    modern_str = str(modern_val).strip() if modern_val is not None else ""

    # Exact match
    if legacy_str == modern_str:
        return None

    # Timestamp tolerance check
    if is_timestamp_field(field_name):
        legacy_ts = parse_timestamp(legacy_str)
        modern_ts = parse_timestamp(modern_str)
        if legacy_ts and modern_ts:
            delta = abs((modern_ts - legacy_ts).total_seconds())
            if delta <= timestamp_tolerance:
                return None

    return {
        "field": field_name,
        "legacy": legacy_str,
        "modern": modern_str,
    }


def diff_record_sets(
    legacy_records: list,
    modern_records: list,
    key_field: str,
    ignore_fields: set,
    timestamp_tolerance: int,
) -> dict:
    """Diff two lists of record dicts.

    Args:
        legacy_records: Records from the legacy COBOL system.
        modern_records: Records from the migrated Java system.
        key_field: Field name to use as the record key for matching.
        ignore_fields: Set of field names to skip during comparison.
        timestamp_tolerance: Seconds of tolerance for timestamp fields.

    Returns:
        Differential report dict.
    """
    # Index by key
    legacy_by_key = {}
    for rec in legacy_records:
        key = str(rec.get(key_field, "")).strip()
        legacy_by_key[key] = rec

    modern_by_key = {}
    for rec in modern_records:
        key = str(rec.get(key_field, "")).strip()
        modern_by_key[key] = rec

    all_keys = set(legacy_by_key.keys()) | set(modern_by_key.keys())

    matches = 0
    mismatched_records = []
    legacy_only = []
    modern_only = []

    for key in sorted(all_keys):
        if key not in legacy_by_key:
            modern_only.append({"key": key, "record": modern_by_key[key]})
            continue
        if key not in modern_by_key:
            legacy_only.append({"key": key, "record": legacy_by_key[key]})
            continue

        legacy_rec = legacy_by_key[key]
        modern_rec = modern_by_key[key]

        # Compare all fields
        all_fields = set(legacy_rec.keys()) | set(modern_rec.keys())
        record_mismatches = []

        for field in sorted(all_fields):
            if field.startswith("_"):
                continue
            mismatch = compare_values(
                field,
                legacy_rec.get(field),
                modern_rec.get(field),
                ignore_fields,
                timestamp_tolerance,
            )
            if mismatch:
                record_mismatches.append(mismatch)

        if record_mismatches:
            mismatched_records.append(
                {
                    "key": key,
                    "mismatches": record_mismatches,
                }
            )
        else:
            matches += 1

    report = {
        "key_field": key_field,
        "legacy_count": len(legacy_records),
        "modern_count": len(modern_records),
        "matched": matches,
        "mismatched": len(mismatched_records),
        "legacy_only": len(legacy_only),
        "modern_only": len(modern_only),
        "mismatched_records": mismatched_records,
        "legacy_only_records": legacy_only,
        "modern_only_records": modern_only,
        "pass": len(mismatched_records) == 0
        and len(legacy_only) == 0
        and len(modern_only) == 0,
    }

    return report


def main():
    parser = argparse.ArgumentParser(
        description="Differential comparison of legacy vs modern batch outputs."
    )
    parser.add_argument(
        "--legacy", required=True, help="Path to legacy output JSON."
    )
    parser.add_argument(
        "--modern", required=True, help="Path to modern output JSON."
    )
    parser.add_argument(
        "--key-field",
        default=None,
        help="Field name to use as record key (auto-detected if not set).",
    )
    parser.add_argument(
        "--ignore-fields",
        default="",
        help="Comma-separated field names to ignore.",
    )
    parser.add_argument(
        "--timestamp-tolerance",
        type=int,
        default=DEFAULT_TIMESTAMP_TOLERANCE_SECONDS,
        help=f"Seconds of tolerance for timestamp comparisons (default: {DEFAULT_TIMESTAMP_TOLERANCE_SECONDS}).",
    )
    parser.add_argument(
        "--output",
        default=None,
        help="Path to write report JSON (default: stdout).",
    )
    args = parser.parse_args()

    with open(args.legacy, "r") as f:
        legacy_data = json.load(f)
    with open(args.modern, "r") as f:
        modern_data = json.load(f)

    # Unwrap if in golden-file format
    legacy_records = (
        legacy_data["records"] if isinstance(legacy_data, dict) and "records" in legacy_data
        else legacy_data
    )
    modern_records = (
        modern_data["records"] if isinstance(modern_data, dict) and "records" in modern_data
        else modern_data
    )

    ignore_fields = (
        set(args.ignore_fields.split(",")) if args.ignore_fields else set()
    )
    ignore_fields.discard("")

    # Auto-detect key field from first record
    key_field = args.key_field
    if not key_field and legacy_records:
        first = legacy_records[0]
        for candidate in first:
            if not candidate.startswith("_"):
                key_field = candidate
                break

    if not key_field:
        print("ERROR: Cannot determine key field.", file=sys.stderr)
        sys.exit(1)

    report = diff_record_sets(
        legacy_records,
        modern_records,
        key_field,
        ignore_fields,
        args.timestamp_tolerance,
    )

    report_json = json.dumps(report, indent=2, default=str)

    if args.output:
        with open(args.output, "w") as f:
            f.write(report_json)
        print(f"Report written to {args.output}")
    else:
        print(report_json)

    status = "PASS" if report["pass"] else "FAIL"
    print(f"\nResult: {status}", file=sys.stderr)
    print(f"  Matched:     {report['matched']}", file=sys.stderr)
    print(f"  Mismatched:  {report['mismatched']}", file=sys.stderr)
    print(f"  Legacy only: {report['legacy_only']}", file=sys.stderr)
    print(f"  Modern only: {report['modern_only']}", file=sys.stderr)

    sys.exit(0 if report["pass"] else 1)


if __name__ == "__main__":
    main()
