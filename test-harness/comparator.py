"""
Comparison utilities for migration validation.

Provides field-level diff functions with configurable tolerance for numeric
fields and options to ignore FILLER or timestamp fields.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Optional


@dataclass
class FieldDiff:
    """A single field-level difference between two records."""
    record_index: int
    field_name: str
    expected: object
    actual: object
    diff_type: str  # "missing_field", "value_mismatch", "type_mismatch"


@dataclass
class ComparisonResult:
    """Result of comparing two datasets."""
    match: bool
    total_records_expected: int
    total_records_actual: int
    records_compared: int
    field_diffs: list[FieldDiff] = field(default_factory=list)
    missing_records: int = 0
    extra_records: int = 0

    @property
    def summary(self) -> str:
        if self.match:
            return f"PASS: {self.records_compared} records match"
        parts = []
        if self.missing_records:
            parts.append(f"{self.missing_records} missing records")
        if self.extra_records:
            parts.append(f"{self.extra_records} extra records")
        if self.field_diffs:
            parts.append(f"{len(self.field_diffs)} field differences")
        return f"FAIL: {', '.join(parts)}"


@dataclass
class ComparisonConfig:
    """Configuration for comparison behavior."""
    numeric_tolerance: float = 0.005
    ignore_filler: bool = True
    ignore_fields: list[str] = field(default_factory=list)
    ignore_line_numbers: bool = True
    timestamp_date_only: bool = False
    timestamp_fields: list[str] = field(default_factory=lambda: [
        "TRAN-ORIG-TS", "TRAN-PROC-TS",
        "DALYTRAN-ORIG-TS", "DALYTRAN-PROC-TS",
    ])


def _is_numeric(value: object) -> bool:
    if isinstance(value, (int, float)):
        return True
    if isinstance(value, str):
        try:
            float(value)
            return True
        except ValueError:
            return False
    return False


def _numeric_match(expected: object, actual: object, tolerance: float) -> bool:
    try:
        exp_val = float(str(expected))
        act_val = float(str(actual))
        return abs(exp_val - act_val) <= tolerance
    except (ValueError, TypeError):
        return False


def _normalize_timestamp(value: str, date_only: bool) -> str:
    if date_only and len(value) >= 10:
        return value[:10]
    return value


def compare_records(
    expected: dict,
    actual: dict,
    record_index: int,
    config: Optional[ComparisonConfig] = None,
) -> list[FieldDiff]:
    """Compare two parsed records field by field.

    Args:
        expected: The golden/reference record.
        actual: The migrated/test record.
        record_index: Index of the record in the dataset (for reporting).
        config: Comparison configuration.

    Returns:
        List of field differences found.
    """
    if config is None:
        config = ComparisonConfig()

    diffs = []
    skip_fields = set(config.ignore_fields)
    if config.ignore_filler:
        skip_fields.add("FILLER")
    if config.ignore_line_numbers:
        skip_fields.add("_line_number")

    all_keys = set(expected.keys()) | set(actual.keys())

    for key in sorted(all_keys):
        if key in skip_fields:
            continue

        if key not in expected:
            continue
        if key not in actual:
            diffs.append(FieldDiff(
                record_index=record_index,
                field_name=key,
                expected=expected[key],
                actual=None,
                diff_type="missing_field",
            ))
            continue

        exp_val = expected[key]
        act_val = actual[key]

        if config.timestamp_date_only and key in config.timestamp_fields:
            exp_val = _normalize_timestamp(str(exp_val), True)
            act_val = _normalize_timestamp(str(act_val), True)

        if exp_val == act_val:
            continue

        if _is_numeric(exp_val) and _is_numeric(act_val):
            if _numeric_match(exp_val, act_val, config.numeric_tolerance):
                continue

        diffs.append(FieldDiff(
            record_index=record_index,
            field_name=key,
            expected=exp_val,
            actual=act_val,
            diff_type="value_mismatch",
        ))

    return diffs


def compare_datasets(
    expected: list[dict],
    actual: list[dict],
    config: Optional[ComparisonConfig] = None,
) -> ComparisonResult:
    """Compare two lists of parsed records.

    Args:
        expected: The golden/reference dataset.
        actual: The migrated/test dataset.
        config: Comparison configuration.

    Returns:
        ComparisonResult with details of any differences.
    """
    if config is None:
        config = ComparisonConfig()

    result = ComparisonResult(
        match=True,
        total_records_expected=len(expected),
        total_records_actual=len(actual),
        records_compared=min(len(expected), len(actual)),
    )

    if len(expected) != len(actual):
        result.match = False
        result.missing_records = max(0, len(expected) - len(actual))
        result.extra_records = max(0, len(actual) - len(expected))

    for i in range(result.records_compared):
        diffs = compare_records(expected[i], actual[i], i, config)
        if diffs:
            result.match = False
            result.field_diffs.extend(diffs)

    return result
