"""
Field-level comparison utilities for golden-file and differential testing.

Compares two sets of parsed records (expected vs actual) and produces
a structured diff report.
"""

import json
import math
from collections import OrderedDict


class ComparisonResult:
    """Holds the result of comparing two record sets."""

    def __init__(self):
        self.total_records_expected = 0
        self.total_records_actual = 0
        self.matched_records = 0
        self.mismatched_records = 0
        self.missing_records = 0
        self.extra_records = 0
        self.field_diffs = []  # list of per-record field differences

    @property
    def passed(self):
        return (
            self.mismatched_records == 0
            and self.missing_records == 0
            and self.extra_records == 0
        )

    def summary(self):
        return {
            "passed": self.passed,
            "total_records_expected": self.total_records_expected,
            "total_records_actual": self.total_records_actual,
            "matched_records": self.matched_records,
            "mismatched_records": self.mismatched_records,
            "missing_records": self.missing_records,
            "extra_records": self.extra_records,
            "field_diffs_count": len(self.field_diffs),
        }

    def to_json(self, indent=2):
        return json.dumps(
            {
                "summary": self.summary(),
                "field_diffs": self.field_diffs[:100],  # cap at 100 for readability
            },
            indent=indent,
        )


def _values_equal(expected, actual, numeric_tolerance=0.0):
    """Compare two field values with type-aware logic.

    Args:
        expected: The expected (golden) value.
        actual: The actual (migrated) value.
        numeric_tolerance: Absolute tolerance for numeric comparisons.

    Returns:
        True if values are considered equal.
    """
    if expected == actual:
        return True

    # Both numeric
    if isinstance(expected, (int, float)) and isinstance(actual, (int, float)):
        if numeric_tolerance > 0:
            return math.isclose(expected, actual, abs_tol=numeric_tolerance)
        return expected == actual

    # String comparison: trim trailing spaces
    if isinstance(expected, str) and isinstance(actual, str):
        return expected.rstrip() == actual.rstrip()

    # Type mismatch -- try string comparison as fallback
    return str(expected).strip() == str(actual).strip()


def compare_records(
    expected_records,
    actual_records,
    key_fields=None,
    ignore_fields=None,
    numeric_tolerance=0.0,
):
    """Compare two lists of record dicts field-by-field.

    Args:
        expected_records: List of OrderedDict from golden file.
        actual_records: List of OrderedDict from Java output.
        key_fields: Optional list of field names to use as record keys for
                    matching. If None, records are compared positionally.
        ignore_fields: Set of field names to skip during comparison.
        numeric_tolerance: Absolute tolerance for numeric fields.

    Returns:
        A ComparisonResult.
    """
    result = ComparisonResult()
    result.total_records_expected = len(expected_records)
    result.total_records_actual = len(actual_records)
    ignore = set(ignore_fields or [])

    if key_fields:
        # Key-based matching
        def make_key(record):
            return tuple(record.get(k, "") for k in key_fields)

        expected_map = {}
        for i, rec in enumerate(expected_records):
            expected_map[make_key(rec)] = (i, rec)

        actual_map = {}
        for i, rec in enumerate(actual_records):
            actual_map[make_key(rec)] = (i, rec)

        all_keys = set(expected_map.keys()) | set(actual_map.keys())

        for key in sorted(all_keys, key=str):
            if key not in expected_map:
                result.extra_records += 1
                continue
            if key not in actual_map:
                result.missing_records += 1
                continue

            exp_idx, exp_rec = expected_map[key]
            act_idx, act_rec = actual_map[key]
            diffs = _diff_single_record(
                exp_rec, act_rec, exp_idx, ignore, numeric_tolerance
            )
            if diffs:
                result.mismatched_records += 1
                result.field_diffs.extend(diffs)
            else:
                result.matched_records += 1
    else:
        # Positional matching
        max_len = max(len(expected_records), len(actual_records))
        for i in range(max_len):
            if i >= len(expected_records):
                result.extra_records += 1
                continue
            if i >= len(actual_records):
                result.missing_records += 1
                continue

            diffs = _diff_single_record(
                expected_records[i],
                actual_records[i],
                i,
                ignore,
                numeric_tolerance,
            )
            if diffs:
                result.mismatched_records += 1
                result.field_diffs.extend(diffs)
            else:
                result.matched_records += 1

    return result


def _diff_single_record(expected, actual, record_index, ignore, tolerance):
    """Compare fields of a single record pair.

    Returns:
        List of diff dicts, empty if records match.
    """
    diffs = []
    all_fields = list(expected.keys())
    # Also check fields only in actual
    for k in actual.keys():
        if k not in expected:
            all_fields.append(k)

    for field in all_fields:
        if field in ignore:
            continue

        exp_val = expected.get(field)
        act_val = actual.get(field)

        if exp_val is None and act_val is not None:
            diffs.append(
                {
                    "record_index": record_index,
                    "field": field,
                    "expected": None,
                    "actual": act_val,
                    "type": "extra_field",
                }
            )
        elif exp_val is not None and act_val is None:
            diffs.append(
                {
                    "record_index": record_index,
                    "field": field,
                    "expected": exp_val,
                    "actual": None,
                    "type": "missing_field",
                }
            )
        elif not _values_equal(exp_val, act_val, tolerance):
            diffs.append(
                {
                    "record_index": record_index,
                    "field": field,
                    "expected": exp_val,
                    "actual": act_val,
                    "type": "value_mismatch",
                }
            )

    return diffs


def compare_golden_file(golden_path, actual_records, key_fields=None, **kwargs):
    """Load a golden JSON file and compare against actual records.

    Args:
        golden_path: Path to the golden JSON file.
        actual_records: List of dicts from the migrated system.
        key_fields: Optional key fields for matching.

    Returns:
        A ComparisonResult.
    """
    with open(golden_path, "r", encoding="utf-8") as f:
        golden_data = json.load(f)

    expected_records = golden_data["records"]
    # Convert to OrderedDict for consistent ordering
    expected_records = [OrderedDict(r) for r in expected_records]

    return compare_records(expected_records, actual_records, key_fields, **kwargs)
