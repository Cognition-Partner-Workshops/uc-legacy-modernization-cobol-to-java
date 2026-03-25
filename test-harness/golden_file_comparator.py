#!/usr/bin/env python3
"""
Golden File Comparator for CardDemo Migration Test Harness.

Compares Java service output (actual) against golden-file references (expected)
and produces a detailed diff report with field-level comparison.

Usage:
    python3 test-harness/golden_file_comparator.py \
        --expected golden-files/acctdata.json \
        --actual /path/to/java/output/accounts.json \
        --entity account

    python3 test-harness/golden_file_comparator.py \
        --expected golden-files/acctdata.json \
        --actual /path/to/java/output/accounts.json \
        --report reports/comparison/acctdata_diff.json
"""

import argparse
import json
import sys
from dataclasses import dataclass, field
from decimal import Decimal, InvalidOperation
from pathlib import Path
from typing import Any


# ---------------------------------------------------------------------------
# Comparison severity levels
# ---------------------------------------------------------------------------

SEVERITY_CRITICAL = "CRITICAL"
SEVERITY_HIGH = "HIGH"
SEVERITY_MEDIUM = "MEDIUM"
SEVERITY_LOW = "LOW"

# Fields that trigger CRITICAL severity if mismatched
FINANCIAL_FIELD_PATTERNS = [
    "-BAL", "-AMT", "-CREDIT-LIMIT", "-CASH-CREDIT-LIMIT",
    "-INT-RATE", "-CYC-CREDIT", "-CYC-DEBIT",
    "_bal", "_amt", "_credit_limit", "_cash_credit_limit",
    "_int_rate", "_cyc_credit", "_cyc_debit",
]

# Fields that trigger HIGH severity if mismatched
IDENTIFIER_FIELD_PATTERNS = [
    "-ID", "-NUM", "-CD", "-TYPE",
    "_id", "_num", "_cd", "_type",
]


@dataclass
class FieldDiff:
    """A single field-level difference between expected and actual."""
    record_index: int
    field_name: str
    expected_value: Any
    actual_value: Any
    severity: str
    message: str


@dataclass
class ComparisonResult:
    """Complete comparison result between expected and actual datasets."""
    expected_file: str
    actual_file: str
    expected_count: int
    actual_count: int
    matched_records: int
    mismatched_records: int
    missing_records: int   # In expected but not actual
    extra_records: int     # In actual but not expected
    diffs: list = field(default_factory=list)
    passed: bool = True

    @property
    def critical_count(self) -> int:
        return sum(1 for d in self.diffs if d.severity == SEVERITY_CRITICAL)

    @property
    def high_count(self) -> int:
        return sum(1 for d in self.diffs if d.severity == SEVERITY_HIGH)

    @property
    def summary(self) -> dict:
        return {
            "passed": self.passed,
            "expected_records": self.expected_count,
            "actual_records": self.actual_count,
            "matched": self.matched_records,
            "mismatched": self.mismatched_records,
            "missing": self.missing_records,
            "extra": self.extra_records,
            "diffs_by_severity": {
                SEVERITY_CRITICAL: self.critical_count,
                SEVERITY_HIGH: self.high_count,
                SEVERITY_MEDIUM: sum(1 for d in self.diffs if d.severity == SEVERITY_MEDIUM),
                SEVERITY_LOW: sum(1 for d in self.diffs if d.severity == SEVERITY_LOW),
            },
        }


def classify_severity(field_name: str) -> str:
    """Classify the severity of a field mismatch based on field name patterns."""
    name_upper = field_name.upper()
    for pattern in FINANCIAL_FIELD_PATTERNS:
        if pattern.upper() in name_upper:
            return SEVERITY_CRITICAL
    for pattern in IDENTIFIER_FIELD_PATTERNS:
        if pattern.upper() in name_upper:
            return SEVERITY_HIGH
    if "DATE" in name_upper or "TS" in name_upper:
        return SEVERITY_MEDIUM
    return SEVERITY_LOW


def normalize_value(value: Any) -> str:
    """Normalize a value for comparison.

    - Strings: strip trailing whitespace
    - Numbers: convert to string with consistent formatting
    - None/null: convert to empty string
    """
    if value is None:
        return ""
    if isinstance(value, str):
        return value.rstrip()
    if isinstance(value, (int, float)):
        return str(value)
    return str(value)


def compare_values(expected: Any, actual: Any, field_name: str) -> tuple[bool, str]:
    """Compare two field values with type-aware logic.

    Returns (is_equal, message).
    """
    # Normalize both values
    exp_str = normalize_value(expected)
    act_str = normalize_value(actual)

    # Direct string match
    if exp_str == act_str:
        return True, ""

    # Try decimal comparison for numeric fields
    severity = classify_severity(field_name)
    if severity == SEVERITY_CRITICAL:
        try:
            exp_dec = Decimal(str(expected))
            act_dec = Decimal(str(actual))
            if exp_dec == act_dec:
                return True, ""
            return False, f"Decimal mismatch: expected {exp_dec}, got {act_dec}"
        except (InvalidOperation, ValueError, TypeError):
            pass

    # Try integer comparison
    try:
        if int(str(expected)) == int(str(actual)):
            return True, ""
    except (ValueError, TypeError):
        pass

    return False, f"Value mismatch: expected '{exp_str}', got '{act_str}'"


def compare_records(expected: dict, actual: dict, record_index: int) -> list[FieldDiff]:
    """Compare two record dicts field by field.

    Skips internal fields (prefixed with _).
    """
    diffs = []
    all_fields = set(expected.keys()) | set(actual.keys())

    for field_name in sorted(all_fields):
        if field_name.startswith("_"):
            continue

        exp_val = expected.get(field_name)
        act_val = actual.get(field_name)

        if field_name not in actual:
            diffs.append(FieldDiff(
                record_index=record_index,
                field_name=field_name,
                expected_value=exp_val,
                actual_value="<MISSING>",
                severity=classify_severity(field_name),
                message=f"Field '{field_name}' missing in actual output",
            ))
            continue

        if field_name not in expected:
            diffs.append(FieldDiff(
                record_index=record_index,
                field_name=field_name,
                expected_value="<NOT IN GOLDEN>",
                actual_value=act_val,
                severity=SEVERITY_LOW,
                message=f"Extra field '{field_name}' in actual output",
            ))
            continue

        is_equal, message = compare_values(exp_val, act_val, field_name)
        if not is_equal:
            diffs.append(FieldDiff(
                record_index=record_index,
                field_name=field_name,
                expected_value=exp_val,
                actual_value=act_val,
                severity=classify_severity(field_name),
                message=message,
            ))

    return diffs


def find_record_key(record: dict, entity_type: str) -> str | None:
    """Extract a natural key from a record based on entity type.

    Used to match records between expected and actual by business key
    rather than position.
    """
    key_map = {
        "account": ["ACCT-ID", "acct_id"],
        "card": ["CARD-NUM", "card_num"],
        "cardxref": ["XREF-CARD-NUM", "xref_card_num", "card_num"],
        "customer": ["CUST-ID", "cust_id"],
        "transaction": ["TRAN-ID", "tran_id", "DALYTRAN-ID", "dalytran_id"],
        "dailytran": ["DALYTRAN-ID", "dalytran_id", "TRAN-ID", "tran_id"],
        "discgrp": ["DIS-ACCT-GROUP-ID", "dis_acct_group_id"],
        "tcatbal": ["TRANCAT-ACCT-ID", "trancat_acct_id"],
        "trancatg": ["TRAN-TYPE-CD", "tran_type_cd"],
        "trantype": ["TRAN-TYPE", "tran_type"],
    }

    possible_keys = key_map.get(entity_type, [])
    for key_field in possible_keys:
        if key_field in record:
            return str(record[key_field])

    # Fallback: use first non-internal field
    for k, v in record.items():
        if not k.startswith("_"):
            return str(v)
    return None


def compare_datasets(expected_records: list[dict], actual_records: list[dict],
                     entity_type: str = "unknown") -> ComparisonResult:
    """Compare two lists of records with key-based matching.

    First attempts key-based matching, then falls back to positional matching.
    """
    result = ComparisonResult(
        expected_file="",
        actual_file="",
        expected_count=len(expected_records),
        actual_count=len(actual_records),
        matched_records=0,
        mismatched_records=0,
        missing_records=0,
        extra_records=0,
    )

    # Build lookup for actual records by key
    actual_by_key: dict[str, dict] = {}
    actual_used: set[int] = set()

    for i, rec in enumerate(actual_records):
        key = find_record_key(rec, entity_type)
        if key is not None:
            actual_by_key[key] = rec
            actual_by_key[f"_idx_{key}"] = i

    # Match expected records
    for exp_idx, exp_rec in enumerate(expected_records):
        exp_key = find_record_key(exp_rec, entity_type)

        act_rec = None
        if exp_key is not None and exp_key in actual_by_key:
            act_rec = actual_by_key[exp_key]
            act_idx_key = f"_idx_{exp_key}"
            if act_idx_key in actual_by_key:
                actual_used.add(actual_by_key[act_idx_key])
        elif exp_idx < len(actual_records):
            act_rec = actual_records[exp_idx]
            actual_used.add(exp_idx)

        if act_rec is None:
            result.missing_records += 1
            result.diffs.append(FieldDiff(
                record_index=exp_idx,
                field_name="<RECORD>",
                expected_value=exp_key or f"record[{exp_idx}]",
                actual_value="<MISSING>",
                severity=SEVERITY_CRITICAL,
                message=f"Expected record {exp_key or exp_idx} not found in actual output",
            ))
            continue

        record_diffs = compare_records(exp_rec, act_rec, exp_idx)
        if record_diffs:
            result.mismatched_records += 1
            result.diffs.extend(record_diffs)
        else:
            result.matched_records += 1

    # Check for extra records in actual
    for act_idx in range(len(actual_records)):
        if act_idx not in actual_used:
            result.extra_records += 1
            act_key = find_record_key(actual_records[act_idx], entity_type)
            result.diffs.append(FieldDiff(
                record_index=act_idx,
                field_name="<RECORD>",
                expected_value="<NOT IN GOLDEN>",
                actual_value=act_key or f"record[{act_idx}]",
                severity=SEVERITY_HIGH,
                message=f"Extra record {act_key or act_idx} in actual output",
            ))

    # Determine pass/fail
    result.passed = (
        result.critical_count == 0
        and result.high_count == 0
        and result.missing_records == 0
        and result.extra_records == 0
    )

    return result


def load_golden_file(path: Path) -> list[dict]:
    """Load records from a golden JSON file."""
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    return data.get("records", [])


def load_actual_file(path: Path) -> list[dict]:
    """Load records from a Java output JSON file.

    Supports both flat arrays and objects with a 'records' key.
    """
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    if isinstance(data, list):
        return data
    if isinstance(data, dict) and "records" in data:
        return data["records"]
    if isinstance(data, dict) and "data" in data:
        return data["data"]
    return [data]


def result_to_json(result: ComparisonResult) -> str:
    """Serialize a ComparisonResult to JSON."""
    output = {
        "summary": result.summary,
        "expected_file": result.expected_file,
        "actual_file": result.actual_file,
        "diffs": [
            {
                "record_index": d.record_index,
                "field": d.field_name,
                "expected": d.expected_value,
                "actual": d.actual_value,
                "severity": d.severity,
                "message": d.message,
            }
            for d in result.diffs
        ],
    }
    return json.dumps(output, indent=2, default=str)


def main():
    parser = argparse.ArgumentParser(
        description="Compare Java output against golden-file references."
    )
    parser.add_argument("--expected", type=Path, required=True, help="Path to golden JSON file")
    parser.add_argument("--actual", type=Path, required=True, help="Path to Java output JSON file")
    parser.add_argument("--entity", type=str, default="unknown", help="Entity type for key matching")
    parser.add_argument("--report", type=Path, help="Write comparison report to file")
    args = parser.parse_args()

    if not args.expected.exists():
        print(f"ERROR: Expected file not found: {args.expected}", file=sys.stderr)
        return 1
    if not args.actual.exists():
        print(f"ERROR: Actual file not found: {args.actual}", file=sys.stderr)
        return 1

    expected = load_golden_file(args.expected)
    actual = load_actual_file(args.actual)

    result = compare_datasets(expected, actual, args.entity)
    result.expected_file = str(args.expected)
    result.actual_file = str(args.actual)

    report_json = result_to_json(result)

    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        with open(args.report, "w", encoding="utf-8") as f:
            f.write(report_json)
            f.write("\n")
        print(f"Report written to {args.report}")

    # Print summary
    print()
    print(f"Comparison: {args.expected} vs {args.actual}")
    print(f"  Expected records: {result.expected_count}")
    print(f"  Actual records:   {result.actual_count}")
    print(f"  Matched:          {result.matched_records}")
    print(f"  Mismatched:       {result.mismatched_records}")
    print(f"  Missing:          {result.missing_records}")
    print(f"  Extra:            {result.extra_records}")
    print(f"  CRITICAL diffs:   {result.critical_count}")
    print(f"  HIGH diffs:       {result.high_count}")
    print()
    status = "PASSED" if result.passed else "FAILED"
    print(f"  Result: {status}")

    return 0 if result.passed else 1


if __name__ == "__main__":
    sys.exit(main())
