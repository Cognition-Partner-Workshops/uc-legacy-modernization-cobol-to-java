"""
Field-level comparison engine for migration testing.

Compares two sets of parsed records (expected vs actual) and produces a
structured diff report.  Supports three comparison modes:

1. **Exact** — values must match exactly (alphanumeric after strip,
   numeric by value).
2. **Tolerance** — numeric fields may differ by up to a caller-supplied
   epsilon (useful for interest calculations with rounding differences).
3. **Subset** — only the fields listed in a whitelist are compared (useful
   when the Java target adds audit columns).
"""

from __future__ import annotations

import json
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any


@dataclass
class FieldMismatch:
    record_index: int
    field_name: str
    expected: Any
    actual: Any
    detail: str = ""


@dataclass
class ComparisonResult:
    source: str = ""
    total_expected: int = 0
    total_actual: int = 0
    matched: int = 0
    mismatched: int = 0
    missing_in_actual: int = 0
    extra_in_actual: int = 0
    field_mismatches: list[FieldMismatch] = field(default_factory=list)

    @property
    def passed(self) -> bool:
        return (
            self.mismatched == 0
            and self.missing_in_actual == 0
            and self.extra_in_actual == 0
        )

    def summary(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        lines = [
            f"[{status}] {self.source}",
            f"  Expected records : {self.total_expected}",
            f"  Actual records   : {self.total_actual}",
            f"  Matched          : {self.matched}",
            f"  Mismatched       : {self.mismatched}",
            f"  Missing in actual: {self.missing_in_actual}",
            f"  Extra in actual  : {self.extra_in_actual}",
        ]
        for fm in self.field_mismatches[:20]:
            lines.append(
                f"  Record[{fm.record_index}].{fm.field_name}: "
                f"expected={fm.expected!r}  actual={fm.actual!r}"
                + (f"  ({fm.detail})" if fm.detail else "")
            )
        if len(self.field_mismatches) > 20:
            lines.append(
                f"  ... and {len(self.field_mismatches) - 20} more mismatches"
            )
        return "\n".join(lines)


def _values_equal(
    expected: Any,
    actual: Any,
    tolerance: float = 0.0,
) -> bool:
    """Compare two field values, optionally with numeric tolerance."""
    if isinstance(expected, (int, float)) and isinstance(actual, (int, float)):
        if tolerance > 0:
            return abs(expected - actual) <= tolerance
        return expected == actual

    # String comparison — strip and case-sensitive
    return str(expected).strip() == str(actual).strip()


def compare_records(
    expected: list[dict[str, Any]],
    actual: list[dict[str, Any]],
    *,
    source: str = "",
    ignore_fields: set[str] | None = None,
    only_fields: set[str] | None = None,
    tolerance: float = 0.0,
) -> ComparisonResult:
    """Compare two lists of record dicts field-by-field.

    Parameters
    ----------
    expected, actual : lists of dicts (one dict per record)
    source : label for the comparison (e.g. file name)
    ignore_fields : field names to skip
    only_fields : if set, only these fields are compared
    tolerance : numeric tolerance for float comparisons
    """
    ignore = ignore_fields or set()
    result = ComparisonResult(
        source=source,
        total_expected=len(expected),
        total_actual=len(actual),
    )

    paired = min(len(expected), len(actual))
    result.missing_in_actual = max(0, len(expected) - len(actual))
    result.extra_in_actual = max(0, len(actual) - len(expected))

    for idx in range(paired):
        rec_exp = expected[idx]
        rec_act = actual[idx]

        fields_to_check = set(rec_exp.keys())
        if only_fields:
            fields_to_check &= only_fields
        fields_to_check -= ignore
        fields_to_check.discard("FILLER")

        rec_ok = True
        for fname in sorted(fields_to_check):
            val_exp = rec_exp.get(fname)
            val_act = rec_act.get(fname)

            if val_act is None:
                result.field_mismatches.append(
                    FieldMismatch(idx, fname, val_exp, None, "field missing in actual")
                )
                rec_ok = False
            elif not _values_equal(val_exp, val_act, tolerance):
                result.field_mismatches.append(
                    FieldMismatch(idx, fname, val_exp, val_act)
                )
                rec_ok = False

        if rec_ok:
            result.matched += 1
        else:
            result.mismatched += 1

    return result


# ---------------------------------------------------------------------------
# Golden-file comparison convenience
# ---------------------------------------------------------------------------

def compare_against_golden(
    golden_path: str | Path,
    actual_records: list[dict[str, Any]],
    **kwargs: Any,
) -> ComparisonResult:
    """Load a golden JSON file and compare against actual records."""
    with open(golden_path) as fp:
        golden = json.load(fp)

    return compare_records(
        golden["records"],
        actual_records,
        source=golden.get("source_file", str(golden_path)),
        **kwargs,
    )
