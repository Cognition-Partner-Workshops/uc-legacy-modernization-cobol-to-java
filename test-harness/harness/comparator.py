"""
Field-by-field comparison utility for COBOL migration output validation.

Compares two lists of parsed records and reports mismatches with field names,
positions, and values.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

from .parser import FieldDef


@dataclass
class FieldMismatch:
    """A single field-level difference between expected and actual."""

    record_index: int
    field_name: str
    field_offset: int
    field_length: int
    expected: Any
    actual: Any

    def __str__(self) -> str:
        return (
            f"Record {self.record_index}: {self.field_name} "
            f"(offset={self.field_offset}, len={self.field_length}) "
            f"expected={self.expected!r} actual={self.actual!r}"
        )


@dataclass
class ComparisonResult:
    """Aggregated result of comparing two record sets."""

    expected_count: int
    actual_count: int
    mismatches: list[FieldMismatch] = field(default_factory=list)
    extra_records_in_actual: int = 0
    missing_records_in_actual: int = 0

    @property
    def passed(self) -> bool:
        return (
            not self.mismatches
            and self.extra_records_in_actual == 0
            and self.missing_records_in_actual == 0
        )

    def summary(self) -> str:
        lines = [
            f"Expected records: {self.expected_count}",
            f"Actual records:   {self.actual_count}",
        ]
        if self.missing_records_in_actual:
            lines.append(f"Missing in actual: {self.missing_records_in_actual}")
        if self.extra_records_in_actual:
            lines.append(f"Extra in actual:   {self.extra_records_in_actual}")
        lines.append(f"Field mismatches:  {len(self.mismatches)}")
        if self.mismatches:
            lines.append("--- First 20 mismatches ---")
            for m in self.mismatches[:20]:
                lines.append(f"  {m}")
        return "\n".join(lines)


def _values_equal(
    expected: Any,
    actual: Any,
    is_numeric: bool,
    tolerance: float,
) -> bool:
    """Compare two field values with optional numeric tolerance."""
    if is_numeric and isinstance(expected, (int, float)) and isinstance(actual, (int, float)):
        return abs(expected - actual) <= tolerance
    return expected == actual


def compare_records(
    expected: list[dict[str, Any]],
    actual: list[dict[str, Any]],
    fields: list[FieldDef],
    numeric_tolerance: float = 0.01,
) -> ComparisonResult:
    """Compare two lists of parsed record dicts field-by-field.

    Parameters
    ----------
    expected : list of dicts
        Records parsed from the reference (golden) file.
    actual : list of dicts
        Records parsed from the implementation under test.
    fields : list of FieldDef
        The copybook field layout (used for offset/length metadata).
    numeric_tolerance : float
        Maximum absolute difference allowed for numeric fields.

    Returns
    -------
    ComparisonResult
    """
    result = ComparisonResult(
        expected_count=len(expected),
        actual_count=len(actual),
    )
    min_len = min(len(expected), len(actual))
    result.missing_records_in_actual = max(0, len(expected) - len(actual))
    result.extra_records_in_actual = max(0, len(actual) - len(expected))

    field_map = {f.name: f for f in fields if f.name != "FILLER"}

    for i in range(min_len):
        exp_rec = expected[i]
        act_rec = actual[i]
        for fname, fdef in field_map.items():
            exp_val = exp_rec.get(fname)
            act_val = act_rec.get(fname)
            if not _values_equal(exp_val, act_val, fdef.is_numeric, numeric_tolerance):
                result.mismatches.append(
                    FieldMismatch(
                        record_index=i,
                        field_name=fname,
                        field_offset=fdef.offset,
                        field_length=fdef.length,
                        expected=exp_val,
                        actual=act_val,
                    )
                )

    return result


def compare_by_key(
    expected: list[dict[str, Any]],
    actual: list[dict[str, Any]],
    fields: list[FieldDef],
    key_field: str,
    numeric_tolerance: float = 0.01,
) -> ComparisonResult:
    """Compare two record sets matched by a key field, not by position.

    Useful when records may be reordered between COBOL and Java output.
    """
    exp_map: dict[Any, dict[str, Any]] = {}
    for rec in expected:
        exp_map[rec.get(key_field)] = rec

    act_map: dict[Any, dict[str, Any]] = {}
    for rec in actual:
        act_map[rec.get(key_field)] = rec

    result = ComparisonResult(
        expected_count=len(expected),
        actual_count=len(actual),
    )

    all_keys = set(exp_map.keys()) | set(act_map.keys())
    result.missing_records_in_actual = len(set(exp_map.keys()) - set(act_map.keys()))
    result.extra_records_in_actual = len(set(act_map.keys()) - set(exp_map.keys()))

    field_map = {f.name: f for f in fields if f.name != "FILLER"}
    common_keys = sorted(set(exp_map.keys()) & set(act_map.keys()), key=str)

    for idx, key in enumerate(common_keys):
        exp_rec = exp_map[key]
        act_rec = act_map[key]
        for fname, fdef in field_map.items():
            exp_val = exp_rec.get(fname)
            act_val = act_rec.get(fname)
            if not _values_equal(exp_val, act_val, fdef.is_numeric, numeric_tolerance):
                result.mismatches.append(
                    FieldMismatch(
                        record_index=idx,
                        field_name=fname,
                        field_offset=fdef.offset,
                        field_length=fdef.length,
                        expected=exp_val,
                        actual=act_val,
                    )
                )

    return result
