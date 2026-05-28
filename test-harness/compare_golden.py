"""Golden-file comparison engine for migration validation.

Compares actual Java output against golden reference files with
configurable tolerance rules for numeric fields.
"""

import json
import sys
from dataclasses import dataclass, field
from decimal import Decimal
from pathlib import Path
from typing import Dict, List, Optional, Tuple


@dataclass
class FieldDifference:
    """A single field-level difference between golden and actual."""
    record_index: int
    record_key: str
    field_name: str
    golden_value: str
    actual_value: str
    within_tolerance: bool = False


@dataclass
class ComparisonResult:
    """Result of comparing golden reference against actual output."""
    golden_file: str
    actual_file: str
    total_records_golden: int = 0
    total_records_actual: int = 0
    records_compared: int = 0
    records_matched: int = 0
    differences: List[FieldDifference] = field(default_factory=list)
    missing_in_actual: List[int] = field(default_factory=list)
    extra_in_actual: List[int] = field(default_factory=list)

    @property
    def passed(self) -> bool:
        """True if all compared records match (within tolerance)."""
        non_tolerated = [d for d in self.differences if not d.within_tolerance]
        return (
            len(non_tolerated) == 0
            and len(self.missing_in_actual) == 0
            and len(self.extra_in_actual) == 0
        )

    def summary(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        lines = [
            f"Comparison Result: {status}",
            f"  Golden records:  {self.total_records_golden}",
            f"  Actual records:  {self.total_records_actual}",
            f"  Compared:        {self.records_compared}",
            f"  Matched:         {self.records_matched}",
            f"  Differences:     {len(self.differences)}",
        ]
        if self.differences:
            tolerated = sum(1 for d in self.differences if d.within_tolerance)
            lines.append(f"    Within tolerance: {tolerated}")
            lines.append(f"    Out of tolerance: {len(self.differences) - tolerated}")
        if self.missing_in_actual:
            lines.append(f"  Missing in actual: {len(self.missing_in_actual)}")
        if self.extra_in_actual:
            lines.append(f"  Extra in actual:   {len(self.extra_in_actual)}")
        return "\n".join(lines)


# Default tolerance rules by PIC type pattern
DEFAULT_TOLERANCES = {
    "signed_decimal": Decimal("0.01"),
    "numeric": Decimal("0"),
    "alphanumeric": None,  # exact match
}

# Fields to skip during comparison
SKIP_FIELDS = {"_line_number", "FILLER"}


def compare_records(
    golden: Dict,
    actual: Dict,
    record_index: int,
    key_field: str = "",
    tolerances: Optional[Dict[str, Decimal]] = None,
) -> List[FieldDifference]:
    """Compare two record dictionaries field by field.

    Args:
        golden: Golden reference record.
        actual: Actual output record.
        record_index: Index of this record in the file.
        key_field: Name of the key field for identification.
        tolerances: Optional per-field tolerance overrides.

    Returns:
        List of field differences found.
    """
    differences = []
    record_key = str(golden.get(key_field, f"record_{record_index}"))

    for field_name, golden_value in golden.items():
        if field_name in SKIP_FIELDS:
            continue

        actual_value = actual.get(field_name)

        if actual_value is None:
            differences.append(FieldDifference(
                record_index=record_index,
                record_key=record_key,
                field_name=field_name,
                golden_value=str(golden_value),
                actual_value="<missing>",
                within_tolerance=False,
            ))
            continue

        # String comparison
        g_str = str(golden_value).strip()
        a_str = str(actual_value).strip()

        if g_str == a_str:
            continue

        # Try numeric tolerance comparison
        within_tolerance = False
        try:
            g_dec = Decimal(g_str)
            a_dec = Decimal(a_str)
            tolerance = Decimal("0.01")
            if tolerances and field_name in tolerances:
                tolerance = tolerances[field_name]
            within_tolerance = abs(g_dec - a_dec) <= tolerance
        except Exception:
            pass

        differences.append(FieldDifference(
            record_index=record_index,
            record_key=record_key,
            field_name=field_name,
            golden_value=g_str,
            actual_value=a_str,
            within_tolerance=within_tolerance,
        ))

    return differences


def compare_golden_files(
    golden_path: str,
    actual_path: str,
    key_field: Optional[str] = None,
    tolerances: Optional[Dict[str, Decimal]] = None,
) -> ComparisonResult:
    """Compare a golden reference JSON file against actual output.

    Args:
        golden_path: Path to golden reference JSON.
        actual_path: Path to actual output JSON.
        key_field: Field to use as record key (for reporting).
        tolerances: Per-field numeric tolerance overrides.

    Returns:
        ComparisonResult with detailed findings.
    """
    golden_data = json.loads(Path(golden_path).read_text())
    actual_data = json.loads(Path(actual_path).read_text())

    golden_records = golden_data.get("records", golden_data)
    actual_records = actual_data.get("records", actual_data)

    if isinstance(golden_records, dict):
        golden_records = [golden_records]
    if isinstance(actual_records, dict):
        actual_records = [actual_records]

    result = ComparisonResult(
        golden_file=golden_path,
        actual_file=actual_path,
        total_records_golden=len(golden_records),
        total_records_actual=len(actual_records),
    )

    # Detect key field from first record if not specified
    if key_field is None and golden_records:
        first_keys = [k for k in golden_records[0].keys() if k not in SKIP_FIELDS]
        key_field = first_keys[0] if first_keys else ""

    # Compare records by position
    max_records = max(len(golden_records), len(actual_records))
    for i in range(max_records):
        if i >= len(golden_records):
            result.extra_in_actual.append(i)
            continue
        if i >= len(actual_records):
            result.missing_in_actual.append(i)
            continue

        result.records_compared += 1
        diffs = compare_records(
            golden_records[i], actual_records[i], i, key_field, tolerances
        )

        if diffs:
            result.differences.extend(diffs)
        else:
            result.records_matched += 1

    return result


def compare_to_json(result: ComparisonResult) -> dict:
    """Convert ComparisonResult to JSON-serializable dictionary."""
    return {
        "status": "PASS" if result.passed else "FAIL",
        "golden_file": result.golden_file,
        "actual_file": result.actual_file,
        "total_records_golden": result.total_records_golden,
        "total_records_actual": result.total_records_actual,
        "records_compared": result.records_compared,
        "records_matched": result.records_matched,
        "differences": [
            {
                "record_index": d.record_index,
                "record_key": d.record_key,
                "field_name": d.field_name,
                "golden_value": d.golden_value,
                "actual_value": d.actual_value,
                "within_tolerance": d.within_tolerance,
            }
            for d in result.differences
        ],
        "missing_in_actual": result.missing_in_actual,
        "extra_in_actual": result.extra_in_actual,
    }


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(
        description="Compare actual output against golden reference files"
    )
    parser.add_argument("--golden", required=True, help="Path to golden reference JSON")
    parser.add_argument("--actual", required=True, help="Path to actual output JSON")
    parser.add_argument("--key-field", help="Field to use as record key")
    parser.add_argument("--output", help="Write JSON report to file")

    args = parser.parse_args()

    result = compare_golden_files(args.golden, args.actual, args.key_field)

    print(result.summary())
    print()

    if args.output:
        report = compare_to_json(result)
        Path(args.output).write_text(json.dumps(report, indent=2))
        print(f"Report written to: {args.output}")

    sys.exit(0 if result.passed else 1)
