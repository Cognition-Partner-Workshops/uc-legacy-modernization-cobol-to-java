"""
comparator.py - Differential comparison engine for migration testing.

Compares two sets of parsed records (legacy vs. migrated) field-by-field
and produces a structured diff report.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from decimal import Decimal, InvalidOperation
from typing import Any


@dataclass
class FieldMismatch:
    """A single field-level mismatch between legacy and migrated records."""
    record_index: int
    field_name: str
    legacy_value: Any
    migrated_value: Any
    message: str = ""


@dataclass
class DiffReport:
    """Result of a differential comparison."""
    source: str
    total_records_legacy: int
    total_records_migrated: int
    records_compared: int
    mismatches: list[FieldMismatch] = field(default_factory=list)

    @property
    def passed(self) -> bool:
        return (
            self.total_records_legacy == self.total_records_migrated
            and len(self.mismatches) == 0
        )

    @property
    def mismatch_count(self) -> int:
        return len(self.mismatches)

    def summary(self) -> dict[str, Any]:
        return {
            "source": self.source,
            "passed": self.passed,
            "total_records_legacy": self.total_records_legacy,
            "total_records_migrated": self.total_records_migrated,
            "records_compared": self.records_compared,
            "mismatch_count": self.mismatch_count,
            "mismatches": [
                {
                    "record": m.record_index,
                    "field": m.field_name,
                    "legacy": m.legacy_value,
                    "migrated": m.migrated_value,
                    "message": m.message,
                }
                for m in self.mismatches
            ],
        }


def _normalise_alpha(value: Any) -> str:
    """Normalise an alphanumeric value for comparison."""
    return str(value).rstrip() if value is not None else ""


def _normalise_numeric(value: Any, tolerance: Decimal = Decimal("0.00")) -> Decimal:
    """Convert a value to Decimal for numeric comparison."""
    if isinstance(value, Decimal):
        return value
    try:
        return Decimal(str(value))
    except (InvalidOperation, ValueError):
        return Decimal("0")


def compare_records(
    legacy: list[dict[str, Any]],
    migrated: list[dict[str, Any]],
    *,
    source: str = "unknown",
    numeric_fields: set[str] | None = None,
    numeric_tolerance: Decimal = Decimal("0.00"),
    ignore_fields: set[str] | None = None,
    volatile_fields: set[str] | None = None,
) -> DiffReport:
    """Compare two lists of record dicts field-by-field.

    Args:
        legacy: Records from the legacy system (golden file or COBOL output).
        migrated: Records from the Java migrated system.
        source: Label for the data source being compared.
        numeric_fields: Set of field names that should be compared numerically.
        numeric_tolerance: Acceptable difference for numeric fields.
        ignore_fields: Set of field names to skip entirely (e.g., FILLER).
        volatile_fields: Set of field names that may differ legitimately
                         (e.g., timestamps set at processing time).

    Returns:
        A DiffReport with all mismatches.
    """
    if numeric_fields is None:
        numeric_fields = set()
    if ignore_fields is None:
        ignore_fields = set()
    if volatile_fields is None:
        volatile_fields = set()

    report = DiffReport(
        source=source,
        total_records_legacy=len(legacy),
        total_records_migrated=len(migrated),
        records_compared=min(len(legacy), len(migrated)),
    )

    if len(legacy) != len(migrated):
        report.mismatches.append(
            FieldMismatch(
                record_index=-1,
                field_name="_record_count",
                legacy_value=len(legacy),
                migrated_value=len(migrated),
                message=f"Record count mismatch: legacy={len(legacy)}, migrated={len(migrated)}",
            )
        )

    for idx in range(report.records_compared):
        leg_rec = legacy[idx]
        mig_rec = migrated[idx]

        all_fields = set(leg_rec.keys()) | set(mig_rec.keys())

        for fname in sorted(all_fields):
            if fname in ignore_fields:
                continue
            if fname.startswith("_filler_"):
                continue
            if fname in volatile_fields:
                continue

            leg_val = leg_rec.get(fname)
            mig_val = mig_rec.get(fname)

            if fname in numeric_fields:
                leg_num = _normalise_numeric(leg_val)
                mig_num = _normalise_numeric(mig_val)
                if abs(leg_num - mig_num) > numeric_tolerance:
                    report.mismatches.append(
                        FieldMismatch(
                            record_index=idx,
                            field_name=fname,
                            legacy_value=str(leg_val),
                            migrated_value=str(mig_val),
                            message=f"Numeric diff: {leg_num} vs {mig_num} (tolerance={numeric_tolerance})",
                        )
                    )
            else:
                leg_str = _normalise_alpha(leg_val)
                mig_str = _normalise_alpha(mig_val)
                if leg_str != mig_str:
                    report.mismatches.append(
                        FieldMismatch(
                            record_index=idx,
                            field_name=fname,
                            legacy_value=leg_str,
                            migrated_value=mig_str,
                            message=f"Alpha mismatch at record {idx}",
                        )
                    )

    return report


def compare_golden_vs_migrated(
    golden_records: list[dict[str, Any]],
    migrated_records: list[dict[str, Any]],
    *,
    source: str = "unknown",
    numeric_fields: set[str] | None = None,
) -> DiffReport:
    """Convenience wrapper for comparing golden-file records against Java output.

    Automatically ignores FILLER fields and uses zero tolerance for numerics.
    """
    return compare_records(
        legacy=golden_records,
        migrated=migrated_records,
        source=source,
        numeric_fields=numeric_fields,
        numeric_tolerance=Decimal("0.00"),
        ignore_fields=set(),
    )
