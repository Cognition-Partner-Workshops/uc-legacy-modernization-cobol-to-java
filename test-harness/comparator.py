"""
Comparator — Comparison functions for migration testing.

Provides utilities for:
  - Golden-file comparison (expected vs actual JSON output)
  - Differential comparison (COBOL vs Java output for same input)
  - Tolerance-based numeric matching for floating-point rounding
  - Field-level diff reporting
"""

import json
import math
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Optional


@dataclass
class FieldDiff:
    """A single field-level difference between two records."""

    record_index: int
    field_name: str
    expected: Any
    actual: Any
    diff_type: str  # "missing", "extra", "value_mismatch", "type_mismatch"
    tolerance_applied: bool = False


@dataclass
class ComparisonResult:
    """Result of comparing two datasets."""

    passed: bool
    total_records_expected: int
    total_records_actual: int
    records_matched: int
    records_mismatched: int
    field_diffs: list[FieldDiff]
    summary: str

    def to_dict(self) -> dict:
        return {
            "passed": self.passed,
            "total_records_expected": self.total_records_expected,
            "total_records_actual": self.total_records_actual,
            "records_matched": self.records_matched,
            "records_mismatched": self.records_mismatched,
            "field_diffs": [
                {
                    "record_index": d.record_index,
                    "field_name": d.field_name,
                    "expected": d.expected,
                    "actual": d.actual,
                    "diff_type": d.diff_type,
                    "tolerance_applied": d.tolerance_applied,
                }
                for d in self.field_diffs
            ],
            "summary": self.summary,
        }


def compare_values(
    expected: Any, actual: Any, numeric_tolerance: float = 0.0
) -> tuple[bool, bool]:
    """
    Compare two values with optional numeric tolerance.

    Returns (match: bool, tolerance_applied: bool).
    """
    if expected == actual:
        return True, False

    # Numeric tolerance comparison
    if isinstance(expected, (int, float)) and isinstance(actual, (int, float)):
        if numeric_tolerance > 0:
            if math.isclose(expected, actual, abs_tol=numeric_tolerance):
                return True, True
        return False, False

    # String comparison — strip trailing whitespace
    if isinstance(expected, str) and isinstance(actual, str):
        if expected.rstrip() == actual.rstrip():
            return True, False

    return False, False


def compare_records(
    expected: dict[str, Any],
    actual: dict[str, Any],
    record_index: int,
    numeric_tolerance: float = 0.01,
    ignore_fields: Optional[set[str]] = None,
) -> list[FieldDiff]:
    """
    Compare two record dicts field-by-field.

    Returns a list of FieldDiff for any mismatches found.
    """
    diffs: list[FieldDiff] = []
    ignore = ignore_fields or set()

    all_keys = set(expected.keys()) | set(actual.keys())

    for key in sorted(all_keys):
        if key in ignore:
            continue

        if key not in actual:
            diffs.append(
                FieldDiff(
                    record_index=record_index,
                    field_name=key,
                    expected=expected[key],
                    actual=None,
                    diff_type="missing",
                )
            )
        elif key not in expected:
            diffs.append(
                FieldDiff(
                    record_index=record_index,
                    field_name=key,
                    expected=None,
                    actual=actual[key],
                    diff_type="extra",
                )
            )
        else:
            match, tol_applied = compare_values(
                expected[key], actual[key], numeric_tolerance
            )
            if not match:
                diffs.append(
                    FieldDiff(
                        record_index=record_index,
                        field_name=key,
                        expected=expected[key],
                        actual=actual[key],
                        diff_type="value_mismatch",
                        tolerance_applied=tol_applied,
                    )
                )

    return diffs


def compare_datasets(
    expected_records: list[dict[str, Any]],
    actual_records: list[dict[str, Any]],
    numeric_tolerance: float = 0.01,
    ignore_fields: Optional[set[str]] = None,
    max_diffs: int = 100,
) -> ComparisonResult:
    """
    Compare two lists of records (expected vs actual).

    Compares record-by-record in order. Reports count mismatches and
    field-level differences up to max_diffs.
    """
    all_diffs: list[FieldDiff] = []
    records_matched = 0
    records_mismatched = 0

    min_len = min(len(expected_records), len(actual_records))

    for i in range(min_len):
        if len(all_diffs) >= max_diffs:
            break

        diffs = compare_records(
            expected_records[i],
            actual_records[i],
            record_index=i,
            numeric_tolerance=numeric_tolerance,
            ignore_fields=ignore_fields,
        )

        if diffs:
            records_mismatched += 1
            all_diffs.extend(diffs[: max_diffs - len(all_diffs)])
        else:
            records_matched += 1

    # Count-level mismatch
    count_match = len(expected_records) == len(actual_records)
    passed = count_match and records_mismatched == 0

    summary_parts = []
    if not count_match:
        summary_parts.append(
            f"Record count mismatch: expected={len(expected_records)}, actual={len(actual_records)}"
        )
    if records_mismatched > 0:
        summary_parts.append(f"{records_mismatched} records have field differences")
    if passed:
        summary_parts.append(f"All {records_matched} records match")

    return ComparisonResult(
        passed=passed,
        total_records_expected=len(expected_records),
        total_records_actual=len(actual_records),
        records_matched=records_matched,
        records_mismatched=records_mismatched,
        field_diffs=all_diffs,
        summary="; ".join(summary_parts),
    )


def compare_golden_file(
    golden_path: Path,
    actual_records: list[dict[str, Any]],
    numeric_tolerance: float = 0.01,
    ignore_fields: Optional[set[str]] = None,
) -> ComparisonResult:
    """
    Compare actual output against a golden reference JSON file.

    Args:
        golden_path: Path to the golden .json file (generated by record_parser).
        actual_records: Records produced by the Java migration.
        numeric_tolerance: Allowed absolute difference for numeric fields.
        ignore_fields: Set of field names to skip during comparison.
    """
    with golden_path.open() as f:
        golden_data = json.load(f)

    expected_records = golden_data["records"]
    return compare_datasets(
        expected_records,
        actual_records,
        numeric_tolerance=numeric_tolerance,
        ignore_fields=ignore_fields,
    )


def compare_differential(
    cobol_output: list[dict[str, Any]],
    java_output: list[dict[str, Any]],
    numeric_tolerance: float = 0.01,
    ignore_fields: Optional[set[str]] = None,
) -> ComparisonResult:
    """
    Differential testing: compare COBOL batch output vs Java batch output.

    Both should be run against the same input; this verifies behavioral parity.
    """
    return compare_datasets(
        cobol_output,
        java_output,
        numeric_tolerance=numeric_tolerance,
        ignore_fields=ignore_fields,
    )
