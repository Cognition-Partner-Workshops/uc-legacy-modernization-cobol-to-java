#!/usr/bin/env python3
"""
comparator.py -- Field-level comparison engine for migration testing.

Supports two modes:
  - golden:       Compare Java output against golden-reference JSON files.
  - differential: Compare COBOL output against Java output field-by-field.

Usage:
    python comparator.py --mode golden --golden-dir ../golden-files --java-dir PATH
    python comparator.py --mode differential --cobol-dir PATH --java-dir PATH
"""

import argparse
import json
import os
import sys
from decimal import Decimal
from typing import Any


# ---------------------------------------------------------------------------
# Tolerance configuration per field type
# ---------------------------------------------------------------------------
TOLERANCE_RULES = {
    "exact":   {"description": "Exact match required"},
    "decimal": {"description": "Numeric delta within threshold", "threshold": Decimal("0.005")},
    "trim":    {"description": "Match after whitespace trimming"},
    "timestamp": {"description": "Timestamp within 1 second tolerance", "threshold_seconds": 1},
}


def classify_field_type(value: Any) -> str:
    """Classify a field value to determine which tolerance rule applies."""
    if value is None:
        return "exact"
    if isinstance(value, (int, float)):
        return "decimal"
    if isinstance(value, str):
        # Check if it looks like a decimal string
        try:
            Decimal(value)
            return "decimal"
        except Exception:
            pass
        return "trim"
    return "exact"


def values_match(expected: Any, actual: Any) -> tuple[bool, str]:
    """Compare two field values with type-appropriate tolerance.

    Returns:
        Tuple of (matches: bool, reason: str).
    """
    if expected is None and actual is None:
        return True, "both null"

    if expected is None or actual is None:
        return False, f"expected={expected!r}, actual={actual!r}"

    field_type = classify_field_type(expected)

    if field_type == "decimal":
        try:
            exp_dec = Decimal(str(expected))
            act_dec = Decimal(str(actual))
            delta = abs(exp_dec - act_dec)
            threshold = TOLERANCE_RULES["decimal"]["threshold"]
            if delta <= threshold:
                return True, f"decimal match (delta={delta})"
            return False, f"decimal mismatch: expected={exp_dec}, actual={act_dec}, delta={delta}"
        except Exception as exc:
            return False, f"decimal parse error: {exc}"

    if field_type == "trim":
        exp_str = str(expected).strip()
        act_str = str(actual).strip()
        if exp_str == act_str:
            return True, "string match (trimmed)"
        return False, f"string mismatch: expected={exp_str!r}, actual={act_str!r}"

    # Exact match fallback
    if expected == actual:
        return True, "exact match"
    return False, f"mismatch: expected={expected!r}, actual={actual!r}"


def compare_records(expected: dict, actual: dict, record_id: str) -> list[dict]:
    """Compare two record dicts field-by-field.

    Returns:
        List of mismatch dicts (empty if all fields match).
    """
    mismatches = []
    all_keys = set(expected.keys()) | set(actual.keys())
    # Skip metadata fields
    skip_keys = {"_line_number", "_source"}

    for key in sorted(all_keys - skip_keys):
        exp_val = expected.get(key)
        act_val = actual.get(key)
        matches, reason = values_match(exp_val, act_val)
        if not matches:
            mismatches.append({
                "record_id": record_id,
                "field": key,
                "expected": exp_val,
                "actual": act_val,
                "reason": reason,
            })

    return mismatches


def compare_golden(golden_dir: str, java_dir: str) -> dict:
    """Compare Java output files against golden references.

    Args:
        golden_dir: Path to golden-files/ directory.
        java_dir: Path to Java-produced output directory (same JSON structure).

    Returns:
        Comparison report dict.
    """
    report = {"mode": "golden", "files": [], "total_mismatches": 0}

    for fname in sorted(os.listdir(golden_dir)):
        if not fname.endswith(".golden.json"):
            continue

        golden_path = os.path.join(golden_dir, fname)
        java_fname = fname.replace(".golden.json", ".java-output.json")
        java_path = os.path.join(java_dir, java_fname)

        file_report = {"file": fname, "mismatches": [], "status": "PASS"}

        if not os.path.exists(java_path):
            file_report["status"] = "SKIP"
            file_report["reason"] = f"Java output not found: {java_fname}"
            report["files"].append(file_report)
            continue

        with open(golden_path) as gf:
            golden_data = json.load(gf)
        with open(java_path) as jf:
            java_data = json.load(jf)

        golden_records = golden_data.get("records", [])
        java_records = java_data.get("records", [])

        if len(golden_records) != len(java_records):
            file_report["mismatches"].append({
                "field": "_record_count",
                "expected": len(golden_records),
                "actual": len(java_records),
                "reason": "Record count mismatch",
            })

        for idx, (g_rec, j_rec) in enumerate(zip(golden_records, java_records)):
            record_id = f"{fname}:record[{idx}]"
            mismatches = compare_records(g_rec, j_rec, record_id)
            file_report["mismatches"].extend(mismatches)

        if file_report["mismatches"]:
            file_report["status"] = "FAIL"
            report["total_mismatches"] += len(file_report["mismatches"])

        report["files"].append(file_report)

    return report


def compare_differential(cobol_dir: str, java_dir: str) -> dict:
    """Compare COBOL and Java outputs side-by-side.

    Both directories should contain JSON files with the same structure
    as golden files (metadata + records).

    Returns:
        Comparison report dict.
    """
    report = {"mode": "differential", "files": [], "total_mismatches": 0}

    for fname in sorted(os.listdir(cobol_dir)):
        if not fname.endswith(".json"):
            continue

        cobol_path = os.path.join(cobol_dir, fname)
        java_path = os.path.join(java_dir, fname)

        file_report = {"file": fname, "mismatches": [], "status": "PASS"}

        if not os.path.exists(java_path):
            file_report["status"] = "SKIP"
            file_report["reason"] = f"Java output not found: {fname}"
            report["files"].append(file_report)
            continue

        with open(cobol_path) as cf:
            cobol_data = json.load(cf)
        with open(java_path) as jf:
            java_data = json.load(jf)

        cobol_records = cobol_data.get("records", [])
        java_records = java_data.get("records", [])

        if len(cobol_records) != len(java_records):
            file_report["mismatches"].append({
                "field": "_record_count",
                "expected": len(cobol_records),
                "actual": len(java_records),
                "reason": "Record count mismatch",
            })

        for idx, (c_rec, j_rec) in enumerate(zip(cobol_records, java_records)):
            record_id = f"{fname}:record[{idx}]"
            mismatches = compare_records(c_rec, j_rec, record_id)
            file_report["mismatches"].extend(mismatches)

        if file_report["mismatches"]:
            file_report["status"] = "FAIL"
            report["total_mismatches"] += len(file_report["mismatches"])

        report["files"].append(file_report)

    return report


def main() -> int:
    parser = argparse.ArgumentParser(description="Migration comparison engine")
    parser.add_argument("--mode", choices=["golden", "differential"], required=True)
    parser.add_argument("--golden-dir", default="../golden-files")
    parser.add_argument("--java-dir", help="Path to Java output directory")
    parser.add_argument("--cobol-dir", help="Path to COBOL output directory")
    parser.add_argument("--output", default="comparison-report.json",
                        help="Output report file path")
    args = parser.parse_args()

    if args.mode == "golden":
        if not args.java_dir:
            print("ERROR: --java-dir required for golden mode")
            return 1
        report = compare_golden(args.golden_dir, args.java_dir)
    else:
        if not args.cobol_dir or not args.java_dir:
            print("ERROR: --cobol-dir and --java-dir required for differential mode")
            return 1
        report = compare_differential(args.cobol_dir, args.java_dir)

    with open(args.output, "w") as f:
        json.dump(report, f, indent=2, default=str)

    print(f"\nComparison complete ({args.mode} mode)")
    print(f"  Files compared: {len(report['files'])}")
    print(f"  Total mismatches: {report['total_mismatches']}")

    for fr in report["files"]:
        status = fr["status"]
        mismatch_count = len(fr.get("mismatches", []))
        print(f"    {status:6s} {fr['file']} ({mismatch_count} mismatches)")

    return 1 if report["total_mismatches"] > 0 else 0


if __name__ == "__main__":
    sys.exit(main())
