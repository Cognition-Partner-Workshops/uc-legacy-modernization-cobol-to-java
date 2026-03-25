#!/usr/bin/env python3
"""
Golden-file comparison engine.

Compares the output of a migrated Java component against the JSON golden
reference files produced by ``cobol_field_parser.py``.

Usage:
    python golden_file_comparator.py <golden.json> <actual.json> [--tolerance 0.01]
"""

from __future__ import annotations

import json
import math
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any


@dataclass
class FieldDiff:
    """A single field-level difference between golden and actual."""
    record_index: int
    field_name: str
    golden_value: Any
    actual_value: Any
    diff_type: str  # "value_mismatch", "missing_field", "extra_field"


@dataclass
class ComparisonResult:
    """Outcome of comparing a golden file against an actual output."""
    golden_path: str
    actual_path: str
    golden_count: int = 0
    actual_count: int = 0
    matched_records: int = 0
    mismatched_records: int = 0
    missing_records: int = 0
    extra_records: int = 0
    field_diffs: list[FieldDiff] = field(default_factory=list)

    @property
    def passed(self) -> bool:
        return (
            self.mismatched_records == 0
            and self.missing_records == 0
            and self.extra_records == 0
        )

    def summary(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        lines = [
            f"[{status}] Golden: {self.golden_path}  Actual: {self.actual_path}",
            f"  Records -- golden: {self.golden_count}  actual: {self.actual_count}",
            f"  Matched: {self.matched_records}  Mismatched: {self.mismatched_records}",
            f"  Missing: {self.missing_records}  Extra: {self.extra_records}",
        ]
        if self.field_diffs:
            lines.append(f"  Field diffs (first 20):")
            for d in self.field_diffs[:20]:
                lines.append(
                    f"    record[{d.record_index}].{d.field_name}: "
                    f"golden={d.golden_value!r}  actual={d.actual_value!r}  ({d.diff_type})"
                )
            if len(self.field_diffs) > 20:
                lines.append(f"    ... and {len(self.field_diffs) - 20} more")
        return "\n".join(lines)


def values_equal(golden: Any, actual: Any, tolerance: float = 0.01) -> bool:
    """
    Compare two field values with type-aware logic.

    - Numeric values are compared with an absolute tolerance.
    - Strings are compared after stripping trailing whitespace.
    - None / missing values are treated as equal to empty string or zero.
    """
    # Normalise None
    if golden is None and actual is None:
        return True
    if golden is None:
        golden = "" if isinstance(actual, str) else 0
    if actual is None:
        actual = "" if isinstance(golden, str) else 0

    # Both numeric
    if isinstance(golden, (int, float)) and isinstance(actual, (int, float)):
        return math.isclose(golden, actual, abs_tol=tolerance)

    # Both strings
    if isinstance(golden, str) and isinstance(actual, str):
        return golden.rstrip() == actual.rstrip()

    # Mixed types -- coerce to string
    return str(golden).rstrip() == str(actual).rstrip()


def compare_records(
    golden_rec: dict[str, Any],
    actual_rec: dict[str, Any],
    record_index: int,
    tolerance: float = 0.01,
) -> list[FieldDiff]:
    """Compare two record dicts field by field."""
    diffs: list[FieldDiff] = []
    all_keys = set(golden_rec.keys()) | set(actual_rec.keys())

    for key in sorted(all_keys):
        if key.startswith("_"):
            continue  # skip metadata keys
        g_val = golden_rec.get(key)
        a_val = actual_rec.get(key)
        if key not in actual_rec:
            diffs.append(FieldDiff(record_index, key, g_val, None, "missing_field"))
        elif key not in golden_rec:
            diffs.append(FieldDiff(record_index, key, None, a_val, "extra_field"))
        elif not values_equal(g_val, a_val, tolerance):
            diffs.append(FieldDiff(record_index, key, g_val, a_val, "value_mismatch"))

    return diffs


def compare_golden_file(
    golden_path: str | Path,
    actual_path: str | Path,
    tolerance: float = 0.01,
    key_field: str | None = None,
) -> ComparisonResult:
    """
    Compare a golden JSON file against an actual output JSON file.

    Both files are expected to have the structure:
        { "records": [ {field: value, ...}, ... ] }

    If *key_field* is provided, records are matched by that field value
    instead of by positional index.
    """
    golden_path = Path(golden_path)
    actual_path = Path(actual_path)

    with open(golden_path, "r", encoding="utf-8") as fh:
        golden_data = json.load(fh)
    with open(actual_path, "r", encoding="utf-8") as fh:
        actual_data = json.load(fh)

    golden_records = golden_data.get("records", [])
    actual_records = actual_data.get("records", [])

    result = ComparisonResult(
        golden_path=str(golden_path),
        actual_path=str(actual_path),
        golden_count=len(golden_records),
        actual_count=len(actual_records),
    )

    if key_field:
        golden_map = {r.get(key_field): r for r in golden_records}
        actual_map = {r.get(key_field): r for r in actual_records}
        all_keys = set(golden_map.keys()) | set(actual_map.keys())

        for idx, k in enumerate(sorted(all_keys, key=str)):
            if k not in actual_map:
                result.missing_records += 1
            elif k not in golden_map:
                result.extra_records += 1
            else:
                diffs = compare_records(golden_map[k], actual_map[k], idx, tolerance)
                if diffs:
                    result.mismatched_records += 1
                    result.field_diffs.extend(diffs)
                else:
                    result.matched_records += 1
    else:
        max_len = max(len(golden_records), len(actual_records))
        for i in range(max_len):
            if i >= len(actual_records):
                result.missing_records += 1
            elif i >= len(golden_records):
                result.extra_records += 1
            else:
                diffs = compare_records(golden_records[i], actual_records[i], i, tolerance)
                if diffs:
                    result.mismatched_records += 1
                    result.field_diffs.extend(diffs)
                else:
                    result.matched_records += 1

    return result


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------
def main() -> None:
    if len(sys.argv) < 3:
        print("Usage: golden_file_comparator.py <golden.json> <actual.json> [--tolerance N]")
        sys.exit(1)

    golden = sys.argv[1]
    actual = sys.argv[2]
    tolerance = 0.01
    if "--tolerance" in sys.argv:
        idx = sys.argv.index("--tolerance")
        tolerance = float(sys.argv[idx + 1])

    result = compare_golden_file(golden, actual, tolerance)
    print(result.summary())
    sys.exit(0 if result.passed else 1)


if __name__ == "__main__":
    main()
