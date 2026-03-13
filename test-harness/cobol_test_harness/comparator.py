"""Field-by-field comparison utility for COBOL-to-Java migration testing.

Compares two sets of parsed records (expected vs actual) and reports
mismatches with field names, positions, and values.
"""

from __future__ import annotations

import json
from dataclasses import dataclass
from decimal import Decimal, InvalidOperation
from pathlib import Path
from typing import Any


@dataclass
class FieldMismatch:
    """A single field-level mismatch between expected and actual output."""

    record_index: int
    field_name: str
    expected_value: str
    actual_value: str
    offset: int = 0
    length: int = 0
    pic_type: str = ""
    mismatch_type: str = "VALUE_MISMATCH"

    def to_dict(self) -> dict[str, Any]:
        return {
            "record_index": self.record_index,
            "field": self.field_name,
            "expected": self.expected_value,
            "actual": self.actual_value,
            "offset": self.offset,
            "length": self.length,
            "pic_type": self.pic_type,
            "mismatch_type": self.mismatch_type,
        }


@dataclass
class ComparisonResult:
    """Result of comparing two record sets."""

    source_file: str
    total_records_expected: int
    total_records_actual: int
    records_compared: int
    mismatches: list[FieldMismatch]
    record_count_match: bool

    @property
    def passed(self) -> bool:
        return self.record_count_match and len(self.mismatches) == 0

    @property
    def mismatch_count(self) -> int:
        return len(self.mismatches)

    def summary(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        lines = [
            f"Comparison Result: {status}",
            f"  Source: {self.source_file}",
            f"  Expected records: {self.total_records_expected}",
            f"  Actual records:   {self.total_records_actual}",
            f"  Records compared: {self.records_compared}",
            f"  Mismatches:       {self.mismatch_count}",
        ]
        if not self.record_count_match:
            lines.append("  ** RECORD COUNT MISMATCH **")
        return "\n".join(lines)

    def to_dict(self) -> dict[str, Any]:
        return {
            "source_file": self.source_file,
            "status": "PASS" if self.passed else "FAIL",
            "total_records_expected": self.total_records_expected,
            "total_records_actual": self.total_records_actual,
            "records_compared": self.records_compared,
            "mismatch_count": self.mismatch_count,
            "mismatches": [m.to_dict() for m in self.mismatches],
        }


def _normalize_value(value: Any) -> str:
    """Normalize a field value for comparison."""
    if value is None:
        return ""
    s = str(value).strip()
    # Try to normalize numeric values
    try:
        d = Decimal(s)
        # Remove trailing zeros for consistent comparison
        return str(d.normalize())
    except (InvalidOperation, ValueError):
        return s


def _values_match(
    expected: Any,
    actual: Any,
    numeric_tolerance: Decimal = Decimal("0"),
) -> bool:
    """Compare two field values, with optional numeric tolerance."""
    exp_str = _normalize_value(expected)
    act_str = _normalize_value(actual)

    # Exact string match
    if exp_str == act_str:
        return True

    # Try numeric comparison with tolerance
    try:
        exp_dec = Decimal(exp_str)
        act_dec = Decimal(act_str)
        return abs(exp_dec - act_dec) <= numeric_tolerance
    except (InvalidOperation, ValueError):
        pass

    return False


def compare_records(
    expected_records: list[dict[str, Any]],
    actual_records: list[dict[str, Any]],
    source_file: str = "",
    field_metadata: list[dict[str, Any]] | None = None,
    numeric_tolerance: Decimal = Decimal("0"),
    ignore_fields: set[str] | None = None,
) -> ComparisonResult:
    """Compare two lists of record dicts field-by-field.

    Args:
        expected_records: The golden/reference records.
        actual_records: The records to validate.
        source_file: Name of the source file for reporting.
        field_metadata: Optional list of field metadata dicts (from parser).
        numeric_tolerance: Maximum allowed difference for numeric fields.
        ignore_fields: Set of field names to skip during comparison.

    Returns:
        A ComparisonResult with all mismatches.
    """
    ignore_fields = ignore_fields or set()
    mismatches: list[FieldMismatch] = []

    record_count_match = len(expected_records) == len(actual_records)
    records_to_compare = min(len(expected_records), len(actual_records))

    # Build a lookup for field metadata
    meta_lookup: dict[str, dict[str, Any]] = {}
    if field_metadata:
        for fm in field_metadata:
            meta_lookup[fm["name"]] = fm

    for i in range(records_to_compare):
        exp_rec = expected_records[i]
        act_rec = actual_records[i]

        # Compare all fields in expected record
        all_fields = set(exp_rec.keys()) | set(act_rec.keys())

        for field_name in sorted(all_fields):
            if field_name in ignore_fields:
                continue

            exp_val = exp_rec.get(field_name)
            act_val = act_rec.get(field_name)

            if exp_val is None and act_val is not None:
                mismatches.append(FieldMismatch(
                    record_index=i,
                    field_name=field_name,
                    expected_value="<missing>",
                    actual_value=str(act_val),
                    mismatch_type="FIELD_MISSING_IN_EXPECTED",
                ))
                continue

            if act_val is None and exp_val is not None:
                mismatches.append(FieldMismatch(
                    record_index=i,
                    field_name=field_name,
                    expected_value=str(exp_val),
                    actual_value="<missing>",
                    mismatch_type="FIELD_MISSING_IN_ACTUAL",
                ))
                continue

            if not _values_match(exp_val, act_val, numeric_tolerance):
                meta = meta_lookup.get(field_name, {})
                mismatches.append(FieldMismatch(
                    record_index=i,
                    field_name=field_name,
                    expected_value=str(exp_val),
                    actual_value=str(act_val),
                    offset=meta.get("offset", 0),
                    length=meta.get("length", 0),
                    pic_type=meta.get("pic_type", ""),
                    mismatch_type="VALUE_MISMATCH",
                ))

    # Report extra records
    if len(expected_records) > len(actual_records):
        for i in range(records_to_compare, len(expected_records)):
            mismatches.append(FieldMismatch(
                record_index=i,
                field_name="<entire record>",
                expected_value="<present>",
                actual_value="<missing>",
                mismatch_type="RECORD_MISSING_IN_ACTUAL",
            ))
    elif len(actual_records) > len(expected_records):
        for i in range(records_to_compare, len(actual_records)):
            mismatches.append(FieldMismatch(
                record_index=i,
                field_name="<entire record>",
                expected_value="<missing>",
                actual_value="<present>",
                mismatch_type="RECORD_EXTRA_IN_ACTUAL",
            ))

    return ComparisonResult(
        source_file=source_file,
        total_records_expected=len(expected_records),
        total_records_actual=len(actual_records),
        records_compared=records_to_compare,
        mismatches=mismatches,
        record_count_match=record_count_match,
    )


def compare_json_files(
    expected_path: str | Path,
    actual_path: str | Path,
    numeric_tolerance: Decimal = Decimal("0"),
    ignore_fields: set[str] | None = None,
) -> ComparisonResult:
    """Compare two golden-file JSON outputs field-by-field.

    Both files should have the structure produced by parser.parse_data_file().
    """
    expected_path = Path(expected_path)
    actual_path = Path(actual_path)

    with open(expected_path, "r", encoding="utf-8") as f:
        expected_data = json.load(f)
    with open(actual_path, "r", encoding="utf-8") as f:
        actual_data = json.load(f)

    expected_records = expected_data.get("records", [])
    actual_records = actual_data.get("records", [])

    field_metadata = None
    if "_metadata" in expected_data:
        field_metadata = expected_data["_metadata"].get("fields")

    return compare_records(
        expected_records=expected_records,
        actual_records=actual_records,
        source_file=expected_path.name,
        field_metadata=field_metadata,
        numeric_tolerance=numeric_tolerance,
        ignore_fields=ignore_fields,
    )


# ---------------------------------------------------------------------------
# CLI entry point
# ---------------------------------------------------------------------------


def main() -> None:
    """Command-line interface for comparing two JSON golden files."""
    import argparse

    parser = argparse.ArgumentParser(
        description="Compare two COBOL migration output files field-by-field."
    )
    parser.add_argument(
        "--expected", required=True, help="Path to the expected (golden) JSON file"
    )
    parser.add_argument(
        "--actual", required=True, help="Path to the actual (Java output) JSON file"
    )
    parser.add_argument(
        "--tolerance",
        type=str,
        default="0",
        help="Numeric tolerance for field comparisons (default: 0)",
    )
    parser.add_argument(
        "--output",
        required=False,
        help="Path to write the comparison report JSON (default: stdout)",
    )

    args = parser.parse_args()

    result = compare_json_files(
        expected_path=args.expected,
        actual_path=args.actual,
        numeric_tolerance=Decimal(args.tolerance),
    )

    print(result.summary())
    print()

    report = json.dumps(result.to_dict(), indent=2)

    if args.output:
        Path(args.output).write_text(report + "\n", encoding="utf-8")
        print(f"Detailed report written to {args.output}")
    else:
        if result.mismatches:
            print("Mismatches:")
            print(report)


if __name__ == "__main__":
    main()
