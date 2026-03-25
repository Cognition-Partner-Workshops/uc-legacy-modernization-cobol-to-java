#!/usr/bin/env python3
"""
Differential Comparator for CardDemo Migration Test Harness.

Compares live COBOL and Java system outputs during parallel-run periods.
Supports both synchronous (online) and asynchronous (batch) comparison modes.

Usage:
    # Compare two JSON response files (e.g., COBOL BMS capture vs Java REST response)
    python3 test-harness/differential_comparator.py \
        --cobol-output /path/to/cobol_response.json \
        --java-output /path/to/java_response.json \
        --mode synchronous \
        --entity account

    # Compare batch output files
    python3 test-harness/differential_comparator.py \
        --cobol-output /path/to/cobol_batch_output.json \
        --java-output /path/to/java_batch_output.json \
        --mode asynchronous \
        --entity transaction
"""

import argparse
import json
import sys
from dataclasses import dataclass, field
from datetime import datetime
from decimal import Decimal, InvalidOperation
from pathlib import Path
from typing import Any


# ---------------------------------------------------------------------------
# Divergence severity classification
# ---------------------------------------------------------------------------

DIVERGENCE_SEVERITY = {
    "financial_amount": "CRITICAL",
    "record_count":     "CRITICAL",
    "identifier":       "HIGH",
    "date_timestamp":   "MEDIUM",
    "text_formatting":  "LOW",
    "sort_order":       "LOW",
}

# Field name patterns for severity classification
FINANCIAL_PATTERNS = [
    "BAL", "AMT", "CREDIT", "DEBIT", "LIMIT", "RATE", "PAYMENT",
    "bal", "amt", "credit", "debit", "limit", "rate", "payment",
]
IDENTIFIER_PATTERNS = [
    "ID", "NUM", "CD", "TYPE", "KEY",
    "id", "num", "cd", "type", "key",
]
TIMESTAMP_PATTERNS = [
    "DATE", "TS", "TIME", "TIMESTAMP",
    "date", "ts", "time", "timestamp",
]


@dataclass
class Divergence:
    """A single divergence between COBOL and Java outputs."""
    field_name: str
    cobol_value: Any
    java_value: Any
    severity: str
    category: str
    record_key: str = ""
    message: str = ""


@dataclass
class DifferentialResult:
    """Complete differential comparison result."""
    mode: str  # "synchronous" or "asynchronous"
    entity: str
    timestamp: str = ""
    cobol_record_count: int = 0
    java_record_count: int = 0
    divergences: list = field(default_factory=list)

    @property
    def passed(self) -> bool:
        critical = sum(1 for d in self.divergences if d.severity == "CRITICAL")
        high = sum(1 for d in self.divergences if d.severity == "HIGH")
        return critical == 0 and high == 0

    @property
    def summary(self) -> dict:
        by_severity = {}
        for d in self.divergences:
            by_severity[d.severity] = by_severity.get(d.severity, 0) + 1
        return {
            "passed": self.passed,
            "mode": self.mode,
            "entity": self.entity,
            "cobol_records": self.cobol_record_count,
            "java_records": self.java_record_count,
            "total_divergences": len(self.divergences),
            "by_severity": by_severity,
        }


def classify_field(field_name: str) -> tuple[str, str]:
    """Classify a field name into category and severity.

    Returns (category, severity).
    """
    upper = field_name.upper()

    for pattern in FINANCIAL_PATTERNS:
        if pattern.upper() in upper:
            return "financial_amount", DIVERGENCE_SEVERITY["financial_amount"]

    for pattern in IDENTIFIER_PATTERNS:
        if pattern.upper() in upper:
            return "identifier", DIVERGENCE_SEVERITY["identifier"]

    for pattern in TIMESTAMP_PATTERNS:
        if pattern.upper() in upper:
            return "date_timestamp", DIVERGENCE_SEVERITY["date_timestamp"]

    return "text_formatting", DIVERGENCE_SEVERITY["text_formatting"]


def values_equal(cobol_val: Any, java_val: Any, category: str) -> bool:
    """Compare two values with category-appropriate logic."""
    # Normalize None
    if cobol_val is None and java_val is None:
        return True
    if cobol_val is None or java_val is None:
        return False

    cobol_str = str(cobol_val).rstrip()
    java_str = str(java_val).rstrip()

    # Direct string match
    if cobol_str == java_str:
        return True

    # Financial: exact decimal comparison
    if category == "financial_amount":
        try:
            return Decimal(cobol_str) == Decimal(java_str)
        except (InvalidOperation, ValueError):
            return False

    # Identifiers: try integer comparison (handles leading zeros)
    if category == "identifier":
        try:
            return int(cobol_str) == int(java_str)
        except (ValueError, TypeError):
            return cobol_str == java_str

    # Timestamps: normalize common format differences
    if category == "date_timestamp":
        # Strip trailing zeros/spaces from timestamps
        return cobol_str.rstrip("0").rstrip(".") == java_str.rstrip("0").rstrip(".")

    # Text: case-insensitive trimmed comparison
    return cobol_str.strip().lower() == java_str.strip().lower()


def compare_single_record(cobol_rec: dict, java_rec: dict,
                          record_key: str = "") -> list[Divergence]:
    """Compare a single COBOL record against a Java record."""
    divergences = []
    all_fields = set(cobol_rec.keys()) | set(java_rec.keys())

    for field_name in sorted(all_fields):
        if field_name.startswith("_"):
            continue

        cobol_val = cobol_rec.get(field_name)
        java_val = java_rec.get(field_name)

        category, severity = classify_field(field_name)

        if field_name not in java_rec:
            divergences.append(Divergence(
                field_name=field_name,
                cobol_value=cobol_val,
                java_value="<MISSING>",
                severity=severity,
                category=category,
                record_key=record_key,
                message=f"Field '{field_name}' present in COBOL but missing in Java",
            ))
            continue

        if field_name not in cobol_rec:
            divergences.append(Divergence(
                field_name=field_name,
                cobol_value="<NOT IN COBOL>",
                java_value=java_val,
                severity="LOW",
                category="text_formatting",
                record_key=record_key,
                message=f"Extra field '{field_name}' in Java output",
            ))
            continue

        if not values_equal(cobol_val, java_val, category):
            divergences.append(Divergence(
                field_name=field_name,
                cobol_value=cobol_val,
                java_value=java_val,
                severity=severity,
                category=category,
                record_key=record_key,
                message=f"{field_name}: COBOL='{cobol_val}' vs Java='{java_val}'",
            ))

    return divergences


def _make_composite_key(record: dict, fields: list[str]) -> str | None:
    """Build a composite key by joining multiple field values with '|'.

    Returns None if any required field is missing from the record.
    """
    parts = []
    for f in fields:
        if f not in record:
            return None
        parts.append(str(record[f]))
    return "|".join(parts)


def find_key_value(record: dict, entity: str) -> str:
    """Extract a natural key from a record for matching.

    Supports composite keys for entities whose primary key spans
    multiple fields (discgrp, tcatbal, trancatg).
    """
    # Composite keys: each entry is a list of field-name lists.
    composite_key_map: dict[str, list[list[str]]] = {
        "discgrp": [
            ["DIS-ACCT-GROUP-ID", "DIS-TRAN-TYPE-CD", "DIS-TRAN-CAT-CD"],
            ["dis_acct_group_id", "dis_tran_type_cd", "dis_tran_cat_cd"],
        ],
        "tcatbal": [
            ["TRANCAT-ACCT-ID", "TRANCAT-TYPE-CD", "TRANCAT-CD"],
            ["trancat_acct_id", "trancat_type_cd", "trancat_cd"],
        ],
        "trancatg": [
            ["TRAN-TYPE-CD", "TRAN-CAT-CD"],
            ["tran_type_cd", "tran_cat_cd"],
        ],
    }

    if entity in composite_key_map:
        for field_set in composite_key_map[entity]:
            key = _make_composite_key(record, field_set)
            if key is not None:
                return key

    # Simple (single-field) keys
    simple_key_candidates = {
        "account": ["ACCT-ID", "acct_id", "accountId"],
        "card": ["CARD-NUM", "card_num", "cardNumber"],
        "customer": ["CUST-ID", "cust_id", "customerId"],
        "transaction": ["TRAN-ID", "tran_id", "transactionId", "DALYTRAN-ID"],
        "dailytran": ["DALYTRAN-ID", "tran_id"],
        "cardxref": ["XREF-CARD-NUM", "card_num"],
        "trantype": ["TRAN-TYPE", "tran_type"],
    }

    for key_field in simple_key_candidates.get(entity, []):
        if key_field in record:
            return str(record[key_field])

    # Fallback
    for k, v in record.items():
        if not k.startswith("_"):
            return str(v)
    return ""


def compare_datasets(cobol_records: list[dict], java_records: list[dict],
                     entity: str, mode: str) -> DifferentialResult:
    """Compare two datasets (COBOL vs Java) record-by-record."""
    result = DifferentialResult(
        mode=mode,
        entity=entity,
        timestamp=datetime.utcnow().isoformat(),
        cobol_record_count=len(cobol_records),
        java_record_count=len(java_records),
    )

    # Record count divergence
    if len(cobol_records) != len(java_records):
        result.divergences.append(Divergence(
            field_name="<RECORD_COUNT>",
            cobol_value=len(cobol_records),
            java_value=len(java_records),
            severity="CRITICAL",
            category="record_count",
            message=f"Record count mismatch: COBOL={len(cobol_records)}, Java={len(java_records)}",
        ))

    # Build Java lookup by key
    java_by_key = {}
    for rec in java_records:
        key = find_key_value(rec, entity)
        java_by_key[key] = rec

    # Compare each COBOL record
    java_matched = set()
    for cobol_rec in cobol_records:
        cobol_key = find_key_value(cobol_rec, entity)
        java_rec = java_by_key.get(cobol_key)

        if java_rec is None:
            result.divergences.append(Divergence(
                field_name="<RECORD>",
                cobol_value=cobol_key,
                java_value="<MISSING>",
                severity="CRITICAL",
                category="record_count",
                record_key=cobol_key,
                message=f"COBOL record {cobol_key} not found in Java output",
            ))
            continue

        java_matched.add(cobol_key)
        record_divs = compare_single_record(cobol_rec, java_rec, record_key=cobol_key)
        result.divergences.extend(record_divs)

    # Check for extra Java records
    for java_rec in java_records:
        java_key = find_key_value(java_rec, entity)
        if java_key not in java_matched:
            result.divergences.append(Divergence(
                field_name="<RECORD>",
                cobol_value="<NOT IN COBOL>",
                java_value=java_key,
                severity="HIGH",
                category="record_count",
                record_key=java_key,
                message=f"Extra Java record {java_key} not in COBOL output",
            ))

    return result


def check_sort_order(cobol_records: list[dict], java_records: list[dict],
                     entity: str) -> list[Divergence]:
    """Check if records are in the same order."""
    divergences = []
    min_len = min(len(cobol_records), len(java_records))

    for i in range(min_len):
        cobol_key = find_key_value(cobol_records[i], entity)
        java_key = find_key_value(java_records[i], entity)

        if cobol_key != java_key:
            divergences.append(Divergence(
                field_name="<SORT_ORDER>",
                cobol_value=f"position[{i}]={cobol_key}",
                java_value=f"position[{i}]={java_key}",
                severity="LOW",
                category="sort_order",
                message=f"Sort order differs at position {i}: COBOL={cobol_key}, Java={java_key}",
            ))
            # Only report first sort divergence
            break

    return divergences


def result_to_json(result: DifferentialResult) -> str:
    """Serialize a DifferentialResult to JSON."""
    output = {
        "summary": result.summary,
        "divergences": [
            {
                "field": d.field_name,
                "cobol_value": d.cobol_value,
                "java_value": d.java_value,
                "severity": d.severity,
                "category": d.category,
                "record_key": d.record_key,
                "message": d.message,
            }
            for d in result.divergences
        ],
    }
    return json.dumps(output, indent=2, default=str)


def load_json(path: Path) -> list[dict]:
    """Load records from a JSON file."""
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    if isinstance(data, list):
        return data
    if isinstance(data, dict):
        for key in ("records", "data", "results", "items"):
            if key in data:
                return data[key]
    return [data]


def main():
    parser = argparse.ArgumentParser(
        description="Compare COBOL and Java outputs during parallel-run."
    )
    parser.add_argument("--cobol-output", type=Path, required=True, help="COBOL output JSON file")
    parser.add_argument("--java-output", type=Path, required=True, help="Java output JSON file")
    parser.add_argument("--mode", choices=["synchronous", "asynchronous"], default="asynchronous")
    parser.add_argument("--entity", type=str, default="unknown", help="Entity type")
    parser.add_argument("--report", type=Path, help="Write report to file")
    args = parser.parse_args()

    if not args.cobol_output.exists():
        print(f"ERROR: COBOL output not found: {args.cobol_output}", file=sys.stderr)
        return 1
    if not args.java_output.exists():
        print(f"ERROR: Java output not found: {args.java_output}", file=sys.stderr)
        return 1

    cobol_records = load_json(args.cobol_output)
    java_records = load_json(args.java_output)

    result = compare_datasets(cobol_records, java_records, args.entity, args.mode)

    # Also check sort order
    sort_divs = check_sort_order(cobol_records, java_records, args.entity)
    result.divergences.extend(sort_divs)

    report_json = result_to_json(result)

    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        with open(args.report, "w", encoding="utf-8") as f:
            f.write(report_json)
            f.write("\n")
        print(f"Report written to {args.report}")

    # Print summary
    print()
    print(f"Differential Comparison ({args.mode}): {args.entity}")
    print(f"  COBOL records: {result.cobol_record_count}")
    print(f"  Java records:  {result.java_record_count}")
    print(f"  Divergences:   {len(result.divergences)}")
    for sev, count in sorted(result.summary["by_severity"].items()):
        print(f"    {sev}: {count}")
    print()
    status = "PASSED" if result.passed else "FAILED"
    print(f"  Result: {status}")

    return 0 if result.passed else 1


if __name__ == "__main__":
    sys.exit(main())
