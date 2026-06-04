"""Deep field-level comparator for golden-file and differential testing.

Compares two lists of parsed records (dicts) and reports per-field
differences, respecting COBOL numeric precision and whitespace normalization.
"""

from __future__ import annotations

import math
from dataclasses import dataclass, field
from typing import Any, Dict, List, Optional, Set


@dataclass
class FieldDiff:
    """A single field-level difference."""

    record_key: str
    field_name: str
    expected: Any
    actual: Any
    diff_type: str  # "value_mismatch" | "missing_field" | "extra_field" | "type_mismatch"


@dataclass
class ComparisonResult:
    """Aggregated result of comparing two record sets."""

    total_records_expected: int = 0
    total_records_actual: int = 0
    matched_records: int = 0
    missing_records: List[str] = field(default_factory=list)
    extra_records: List[str] = field(default_factory=list)
    field_diffs: List[FieldDiff] = field(default_factory=list)

    @property
    def passed(self) -> bool:
        return (
            self.total_records_expected == self.total_records_actual
            and not self.missing_records
            and not self.extra_records
            and not self.field_diffs
        )

    def summary(self) -> str:
        lines = [
            f"Records — expected: {self.total_records_expected}, "
            f"actual: {self.total_records_actual}, matched: {self.matched_records}",
        ]
        if self.missing_records:
            lines.append(f"Missing records ({len(self.missing_records)}): "
                         f"{self.missing_records[:5]}{'...' if len(self.missing_records) > 5 else ''}")
        if self.extra_records:
            lines.append(f"Extra records ({len(self.extra_records)}): "
                         f"{self.extra_records[:5]}{'...' if len(self.extra_records) > 5 else ''}")
        if self.field_diffs:
            lines.append(f"Field differences: {len(self.field_diffs)}")
            for d in self.field_diffs[:10]:
                lines.append(f"  [{d.record_key}].{d.field_name}: "
                             f"expected={d.expected!r} actual={d.actual!r} ({d.diff_type})")
            if len(self.field_diffs) > 10:
                lines.append(f"  ... and {len(self.field_diffs) - 10} more")
        return "\n".join(lines)


# ---------------------------------------------------------------------------
# Normalization helpers
# ---------------------------------------------------------------------------

def _normalize_value(value: Any) -> Any:
    """Normalize a value for comparison."""
    if isinstance(value, str):
        return value.rstrip()
    return value


def _values_equal(
    expected: Any,
    actual: Any,
    *,
    numeric_tolerance: float = 0.0,
) -> bool:
    """Compare two field values with optional tolerance for floats."""
    e = _normalize_value(expected)
    a = _normalize_value(actual)

    if e is None and a is None:
        return True
    if e is None or a is None:
        return False

    if isinstance(e, (int, float)) and isinstance(a, (int, float)):
        if numeric_tolerance > 0:
            return math.isclose(e, a, abs_tol=numeric_tolerance)
        return e == a

    return e == a


# ---------------------------------------------------------------------------
# Public API
# ---------------------------------------------------------------------------

def compare_record_sets(
    expected: List[Dict[str, Any]],
    actual: List[Dict[str, Any]],
    key_field: str,
    *,
    ignore_fields: Optional[Set[str]] = None,
    numeric_tolerance: float = 0.0,
) -> ComparisonResult:
    """Compare two lists of record dicts keyed by *key_field*.

    Parameters
    ----------
    expected / actual:
        Lists of dicts (e.g. from ``record_parser.parse_file``).
    key_field:
        The dict key used to match records across the two sets.
    ignore_fields:
        Field names to skip during comparison (e.g. timestamps).
    numeric_tolerance:
        Absolute tolerance for float comparisons.
    """
    ignore = ignore_fields or set()

    result = ComparisonResult(
        total_records_expected=len(expected),
        total_records_actual=len(actual),
    )

    expected_by_key = {str(r.get(key_field, "")): r for r in expected}
    actual_by_key = {str(r.get(key_field, "")): r for r in actual}

    expected_keys = set(expected_by_key.keys())
    actual_keys = set(actual_by_key.keys())

    result.missing_records = sorted(expected_keys - actual_keys)
    result.extra_records = sorted(actual_keys - expected_keys)

    common_keys = expected_keys & actual_keys
    result.matched_records = 0

    for key in sorted(common_keys):
        e_rec = expected_by_key[key]
        a_rec = actual_by_key[key]
        rec_match = True

        all_fields = set(e_rec.keys()) | set(a_rec.keys())
        for fname in sorted(all_fields):
            if fname in ignore:
                continue

            if fname not in e_rec:
                result.field_diffs.append(FieldDiff(
                    record_key=key, field_name=fname,
                    expected=None, actual=a_rec[fname],
                    diff_type="extra_field",
                ))
                rec_match = False
            elif fname not in a_rec:
                result.field_diffs.append(FieldDiff(
                    record_key=key, field_name=fname,
                    expected=e_rec[fname], actual=None,
                    diff_type="missing_field",
                ))
                rec_match = False
            elif not _values_equal(
                e_rec[fname], a_rec[fname],
                numeric_tolerance=numeric_tolerance,
            ):
                result.field_diffs.append(FieldDiff(
                    record_key=key, field_name=fname,
                    expected=e_rec[fname], actual=a_rec[fname],
                    diff_type="value_mismatch",
                ))
                rec_match = False

        if rec_match:
            result.matched_records += 1

    return result
