"""
Golden file comparator for migration validation.

Compares Java system output against golden reference files to detect
field-level discrepancies in migrated data.
"""

import json
import math
from dataclasses import dataclass, field
from pathlib import Path


@dataclass
class FieldDifference:
    """Represents a single field-level difference."""
    record_index: int
    field_name: str
    expected: object
    actual: object
    difference_type: str  # 'missing', 'extra', 'value_mismatch', 'type_mismatch'


@dataclass
class ComparisonResult:
    """Result of comparing actual output against a golden file."""
    golden_file: str
    actual_file: str
    passed: bool
    total_records_expected: int = 0
    total_records_actual: int = 0
    record_count_match: bool = True
    differences: list = field(default_factory=list)
    summary: str = ""

    def to_dict(self) -> dict:
        return {
            "golden_file": self.golden_file,
            "actual_file": self.actual_file,
            "passed": self.passed,
            "total_records_expected": self.total_records_expected,
            "total_records_actual": self.total_records_actual,
            "record_count_match": self.record_count_match,
            "total_differences": len(self.differences),
            "differences": [
                {
                    "record_index": d.record_index,
                    "field_name": d.field_name,
                    "expected": d.expected,
                    "actual": d.actual,
                    "difference_type": d.difference_type,
                }
                for d in self.differences[:100]  # Limit to first 100
            ],
            "summary": self.summary,
        }


def values_equal(expected, actual, tolerance: float = 0.0) -> bool:
    """Compare two values with optional numeric tolerance.

    Args:
        expected: Expected value from golden file
        actual: Actual value from system output
        tolerance: Acceptable absolute difference for numeric values

    Returns:
        True if values are considered equal
    """
    if expected is None and actual is None:
        return True
    if expected is None or actual is None:
        return False

    # String comparison (strip trailing spaces)
    if isinstance(expected, str) and isinstance(actual, str):
        return expected.rstrip() == actual.rstrip()

    # Numeric comparison with tolerance
    if isinstance(expected, (int, float)) and isinstance(actual, (int, float)):
        if tolerance > 0:
            return math.isclose(expected, actual, abs_tol=tolerance)
        return expected == actual

    return expected == actual


def compare_records(
    expected: dict, actual: dict, record_index: int, tolerance: float = 0.0
) -> list[FieldDifference]:
    """Compare two records field-by-field.

    Args:
        expected: Expected record from golden file
        actual: Actual record from system output
        record_index: Index of the record for reporting
        tolerance: Numeric tolerance for floating-point fields

    Returns:
        List of field differences found
    """
    differences = []

    # Check all expected fields
    for field_name, expected_value in expected.items():
        if field_name == "_meta":
            continue

        if field_name not in actual:
            differences.append(FieldDifference(
                record_index=record_index,
                field_name=field_name,
                expected=expected_value,
                actual=None,
                difference_type="missing",
            ))
        elif not values_equal(expected_value, actual[field_name], tolerance):
            actual_value = actual[field_name]
            diff_type = "value_mismatch"
            if type(expected_value) != type(actual_value):
                diff_type = "type_mismatch"
            differences.append(FieldDifference(
                record_index=record_index,
                field_name=field_name,
                expected=expected_value,
                actual=actual_value,
                difference_type=diff_type,
            ))

    # Check for extra fields in actual
    for field_name in actual:
        if field_name == "_meta":
            continue
        if field_name not in expected:
            differences.append(FieldDifference(
                record_index=record_index,
                field_name=field_name,
                expected=None,
                actual=actual[field_name],
                difference_type="extra",
            ))

    return differences


def compare_golden(
    golden_path: str, actual_path: str, tolerance: float = 0.0
) -> ComparisonResult:
    """Compare an actual output file against a golden reference.

    Args:
        golden_path: Path to the golden JSON file
        actual_path: Path to the actual output JSON file
        tolerance: Numeric tolerance for floating-point comparisons

    Returns:
        ComparisonResult with detailed difference information
    """
    golden_file = Path(golden_path)
    actual_file = Path(actual_path)

    with golden_file.open("r") as f:
        golden_data = json.load(f)

    with actual_file.open("r") as f:
        actual_data = json.load(f)

    golden_records = golden_data.get("records", [])
    actual_records = actual_data.get("records", [])

    result = ComparisonResult(
        golden_file=str(golden_path),
        actual_file=str(actual_path),
        passed=True,
        total_records_expected=len(golden_records),
        total_records_actual=len(actual_records),
        record_count_match=(len(golden_records) == len(actual_records)),
    )

    if not result.record_count_match:
        result.passed = False

    # Compare records up to the minimum count
    compare_count = min(len(golden_records), len(actual_records))
    for i in range(compare_count):
        diffs = compare_records(golden_records[i], actual_records[i], i, tolerance)
        result.differences.extend(diffs)

    if result.differences:
        result.passed = False

    # Generate summary
    if result.passed:
        result.summary = (
            f"PASS: All {result.total_records_expected} records match golden reference."
        )
    else:
        issues = []
        if not result.record_count_match:
            issues.append(
                f"Record count mismatch: expected {result.total_records_expected}, "
                f"got {result.total_records_actual}"
            )
        if result.differences:
            issues.append(f"{len(result.differences)} field-level differences found")
        result.summary = f"FAIL: {'; '.join(issues)}"

    return result
