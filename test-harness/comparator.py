"""
Field-level comparison utilities for migration testing.

Provides deep-diff capabilities with configurable tolerance for numeric fields,
format-aware timestamp comparison, and structured diff reporting.
"""

from decimal import Decimal, InvalidOperation
from typing import Any


class FieldDifference:
    """Represents a single field-level difference between two records."""

    def __init__(self, field: str, expected: Any, actual: Any,
                 classification: str = "breaking", detail: str = ""):
        self.field = field
        self.expected = expected
        self.actual = actual
        self.classification = classification  # "breaking", "cosmetic", "expected"
        self.detail = detail

    def to_dict(self) -> dict[str, Any]:
        return {
            "field": self.field,
            "expected": self.expected,
            "actual": self.actual,
            "classification": self.classification,
            "detail": self.detail,
        }

    def __repr__(self) -> str:
        return (f"FieldDifference({self.field!r}: "
                f"{self.expected!r} -> {self.actual!r} "
                f"[{self.classification}])")


class ComparisonConfig:
    """Configuration for record comparison behavior."""

    def __init__(
        self,
        numeric_tolerance: float = 0.01,
        ignore_fields: list[str] | None = None,
        numeric_fields: set[str] | None = None,
        timestamp_format_only: bool = True,
        strip_strings: bool = True,
        case_sensitive: bool = True,
    ):
        self.numeric_tolerance = numeric_tolerance
        self.ignore_fields = set(ignore_fields or [])
        self.numeric_fields = numeric_fields
        self.timestamp_format_only = timestamp_format_only
        self.strip_strings = strip_strings
        self.case_sensitive = case_sensitive


class RecordComparator:
    """Compares two record dictionaries field-by-field."""

    # Fields that are typically timestamps (heuristic)
    TIMESTAMP_INDICATORS = {"TS", "DATE", "TIMESTAMP", "DT"}

    def __init__(self, config: ComparisonConfig | None = None):
        self.config = config or ComparisonConfig()

    def _is_timestamp_field(self, field_name: str) -> bool:
        """Heuristically determine if a field is a timestamp."""
        upper = field_name.upper().replace("-", "_")
        return any(ind in upper for ind in self.TIMESTAMP_INDICATORS)

    def _normalize_timestamp(self, value: str) -> str:
        """Normalize timestamp strings for comparison.

        Strips whitespace and normalizes common format variations.
        """
        normalized = str(value).strip()
        # Normalize separator variations: T vs space
        normalized = normalized.replace("T", " ")
        # Remove trailing zeros in fractional seconds
        if "." in normalized:
            normalized = normalized.rstrip("0").rstrip(".")
        return normalized

    def _compare_numeric(self, expected: Any, actual: Any,
                         field: str) -> FieldDifference | None:
        """Compare two numeric values with tolerance."""
        try:
            exp_val = Decimal(str(expected))
            act_val = Decimal(str(actual))
        except (InvalidOperation, ValueError):
            return FieldDifference(
                field=field,
                expected=expected,
                actual=actual,
                classification="breaking",
                detail="Cannot parse as numeric",
            )

        diff = abs(exp_val - act_val)
        if diff <= Decimal(str(self.config.numeric_tolerance)):
            return None

        return FieldDifference(
            field=field,
            expected=str(expected),
            actual=str(actual),
            classification="breaking",
            detail=f"Numeric difference: {diff} exceeds tolerance {self.config.numeric_tolerance}",
        )

    def _compare_string(self, expected: Any, actual: Any,
                        field: str) -> FieldDifference | None:
        """Compare two string values."""
        exp_str = str(expected)
        act_str = str(actual)

        if self.config.strip_strings:
            exp_str = exp_str.strip()
            act_str = act_str.strip()

        if not self.config.case_sensitive:
            exp_str = exp_str.lower()
            act_str = act_str.lower()

        if exp_str == act_str:
            return None

        # Check if it's just a padding/whitespace difference
        if exp_str.strip() == act_str.strip():
            return FieldDifference(
                field=field,
                expected=repr(expected),
                actual=repr(actual),
                classification="cosmetic",
                detail="Whitespace/padding difference only",
            )

        return FieldDifference(
            field=field,
            expected=expected,
            actual=actual,
            classification="breaking",
            detail="String value mismatch",
        )

    def compare_records(self, expected: dict[str, Any],
                        actual: dict[str, Any]) -> list[FieldDifference]:
        """Compare two record dictionaries field-by-field.

        Returns a list of FieldDifference objects for all mismatched fields.
        An empty list means the records are equivalent.
        """
        diffs: list[FieldDifference] = []

        all_fields = set(expected.keys()) | set(actual.keys())

        for field in sorted(all_fields):
            if field in self.config.ignore_fields:
                continue
            if field.startswith("_"):
                continue  # Skip metadata fields like _line_number

            if field not in expected:
                diffs.append(FieldDifference(
                    field=field,
                    expected="<missing>",
                    actual=actual[field],
                    classification="breaking",
                    detail="Field present in actual but not in expected",
                ))
                continue

            if field not in actual:
                diffs.append(FieldDifference(
                    field=field,
                    expected=expected[field],
                    actual="<missing>",
                    classification="breaking",
                    detail="Field present in expected but not in actual",
                ))
                continue

            exp_val = expected[field]
            act_val = actual[field]

            # Timestamp fields: normalize before comparing
            if (self.config.timestamp_format_only
                    and self._is_timestamp_field(field)):
                exp_norm = self._normalize_timestamp(exp_val)
                act_norm = self._normalize_timestamp(act_val)
                if exp_norm != act_norm:
                    diffs.append(FieldDifference(
                        field=field,
                        expected=exp_val,
                        actual=act_val,
                        classification="breaking",
                        detail="Timestamp value mismatch after normalization",
                    ))
                continue

            # Numeric fields: compare with tolerance.
            # Use explicit numeric_fields set if provided; otherwise fall back
            # to Python type check only (int/float).  We intentionally do NOT
            # use the _looks_numeric heuristic by default because all-digit
            # string fields (card numbers, government IDs, zip codes) would
            # be compared as numbers, silently stripping leading zeros.
            is_numeric = isinstance(exp_val, (int, float))
            if not is_numeric and self.config.numeric_fields is not None:
                is_numeric = field in self.config.numeric_fields
            if is_numeric:
                diff = self._compare_numeric(exp_val, act_val, field)
                if diff:
                    diffs.append(diff)
                continue

            # String fields: compare with optional stripping
            diff = self._compare_string(exp_val, act_val, field)
            if diff:
                diffs.append(diff)

        return diffs

    def compare_record_sets(
        self,
        expected: list[dict[str, Any]],
        actual: list[dict[str, Any]],
        key_field: str | None = None,
    ) -> dict[str, Any]:
        """Compare two lists of records.

        Args:
            expected: The golden/reference record set.
            actual: The migrated/test record set.
            key_field: Optional field to use as the record key for matching.
                       If None, records are compared positionally.

        Returns:
            A summary dict with counts and detailed diffs.
        """
        result: dict[str, Any] = {
            "expected_count": len(expected),
            "actual_count": len(actual),
            "count_match": len(expected) == len(actual),
            "records_compared": 0,
            "records_matched": 0,
            "records_with_diffs": 0,
            "missing_in_actual": [],
            "extra_in_actual": [],
            "diffs": [],
        }

        if key_field:
            exp_map = {str(r.get(key_field, "")): r for r in expected}
            act_map = {str(r.get(key_field, "")): r for r in actual}

            for key in sorted(exp_map.keys()):
                if key not in act_map:
                    result["missing_in_actual"].append(key)
                    continue

                result["records_compared"] += 1
                diffs = self.compare_records(exp_map[key], act_map[key])
                if diffs:
                    result["records_with_diffs"] += 1
                    result["diffs"].append({
                        "key": key,
                        "differences": [d.to_dict() for d in diffs],
                    })
                else:
                    result["records_matched"] += 1

            for key in sorted(act_map.keys()):
                if key not in exp_map:
                    result["extra_in_actual"].append(key)
        else:
            for i in range(max(len(expected), len(actual))):
                if i >= len(expected):
                    result["extra_in_actual"].append(f"record[{i}]")
                    continue
                if i >= len(actual):
                    result["missing_in_actual"].append(f"record[{i}]")
                    continue

                result["records_compared"] += 1
                diffs = self.compare_records(expected[i], actual[i])
                if diffs:
                    result["records_with_diffs"] += 1
                    result["diffs"].append({
                        "index": i,
                        "differences": [d.to_dict() for d in diffs],
                    })
                else:
                    result["records_matched"] += 1

        result["passed"] = (
            result["count_match"]
            and result["records_with_diffs"] == 0
            and len(result["missing_in_actual"]) == 0
            and len(result["extra_in_actual"]) == 0
        )

        return result


def _looks_numeric(value: Any) -> bool:
    """Heuristic: does this value look like a number?"""
    if isinstance(value, (int, float)):
        return True
    if isinstance(value, str):
        stripped = value.strip()
        if not stripped:
            return False
        try:
            Decimal(stripped)
            return True
        except (InvalidOperation, ValueError):
            return False
    return False


def format_diff_report(comparison: dict[str, Any]) -> str:
    """Format a comparison result dict into a human-readable report."""
    lines = []
    lines.append("=" * 72)
    lines.append("COMPARISON REPORT")
    lines.append("=" * 72)
    lines.append(f"Expected records: {comparison['expected_count']}")
    lines.append(f"Actual records:   {comparison['actual_count']}")
    lines.append(f"Count match:      {'YES' if comparison['count_match'] else 'NO'}")
    lines.append(f"Records compared: {comparison['records_compared']}")
    lines.append(f"Records matched:  {comparison['records_matched']}")
    lines.append(f"Records w/ diffs: {comparison['records_with_diffs']}")
    lines.append(f"Overall result:   {'PASS' if comparison['passed'] else 'FAIL'}")
    lines.append("")

    if comparison["missing_in_actual"]:
        lines.append(f"Missing in actual ({len(comparison['missing_in_actual'])}):")
        for key in comparison["missing_in_actual"][:20]:
            lines.append(f"  - {key}")
        if len(comparison["missing_in_actual"]) > 20:
            lines.append(f"  ... and {len(comparison['missing_in_actual']) - 20} more")
        lines.append("")

    if comparison["extra_in_actual"]:
        lines.append(f"Extra in actual ({len(comparison['extra_in_actual'])}):")
        for key in comparison["extra_in_actual"][:20]:
            lines.append(f"  - {key}")
        if len(comparison["extra_in_actual"]) > 20:
            lines.append(f"  ... and {len(comparison['extra_in_actual']) - 20} more")
        lines.append("")

    if comparison["diffs"]:
        lines.append("Field-level differences:")
        lines.append("-" * 72)
        for diff_record in comparison["diffs"][:50]:
            key = diff_record.get("key", diff_record.get("index", "?"))
            lines.append(f"  Record: {key}")
            for d in diff_record["differences"]:
                lines.append(
                    f"    {d['field']}: {d['expected']!r} -> {d['actual']!r} "
                    f"[{d['classification']}] {d['detail']}"
                )
            lines.append("")
        if len(comparison["diffs"]) > 50:
            lines.append(f"  ... and {len(comparison['diffs']) - 50} more records with diffs")

    lines.append("=" * 72)
    return "\n".join(lines)
