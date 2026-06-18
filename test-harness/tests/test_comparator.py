"""Tests for the field-by-field comparison utility."""

from __future__ import annotations

from harness.comparator import compare_by_key, compare_records
from harness.parser import ACCOUNT_FIELDS, FieldDef, _fields


SIMPLE_FIELDS = _fields(
    ("ID", "9(05)"),
    ("NAME", "X(10)"),
    ("AMT", "S9(07)V99"),
)


class TestCompareRecords:
    def test_identical(self):
        records = [
            {"ID": 1, "NAME": "Alice", "AMT": 100.50},
            {"ID": 2, "NAME": "Bob", "AMT": -200.00},
        ]
        result = compare_records(records, list(records), SIMPLE_FIELDS)
        assert result.passed
        assert len(result.mismatches) == 0

    def test_value_mismatch(self):
        expected = [{"ID": 1, "NAME": "Alice", "AMT": 100.50}]
        actual = [{"ID": 1, "NAME": "Alice", "AMT": 100.60}]
        result = compare_records(expected, actual, SIMPLE_FIELDS)
        assert not result.passed
        assert len(result.mismatches) == 1
        assert result.mismatches[0].field_name == "AMT"

    def test_within_tolerance(self):
        expected = [{"ID": 1, "NAME": "Alice", "AMT": 100.50}]
        actual = [{"ID": 1, "NAME": "Alice", "AMT": 100.505}]
        result = compare_records(expected, actual, SIMPLE_FIELDS, numeric_tolerance=0.01)
        assert result.passed

    def test_missing_records(self):
        expected = [
            {"ID": 1, "NAME": "Alice", "AMT": 100.50},
            {"ID": 2, "NAME": "Bob", "AMT": 200.00},
        ]
        actual = [{"ID": 1, "NAME": "Alice", "AMT": 100.50}]
        result = compare_records(expected, actual, SIMPLE_FIELDS)
        assert not result.passed
        assert result.missing_records_in_actual == 1

    def test_extra_records(self):
        expected = [{"ID": 1, "NAME": "Alice", "AMT": 100.50}]
        actual = [
            {"ID": 1, "NAME": "Alice", "AMT": 100.50},
            {"ID": 2, "NAME": "Bob", "AMT": 200.00},
        ]
        result = compare_records(expected, actual, SIMPLE_FIELDS)
        assert not result.passed
        assert result.extra_records_in_actual == 1


class TestCompareByKey:
    def test_reordered_match(self):
        expected = [
            {"ID": 1, "NAME": "Alice", "AMT": 100.50},
            {"ID": 2, "NAME": "Bob", "AMT": 200.00},
        ]
        actual = [
            {"ID": 2, "NAME": "Bob", "AMT": 200.00},
            {"ID": 1, "NAME": "Alice", "AMT": 100.50},
        ]
        result = compare_by_key(expected, actual, SIMPLE_FIELDS, "ID")
        assert result.passed

    def test_key_mismatch(self):
        expected = [{"ID": 1, "NAME": "Alice", "AMT": 100.50}]
        actual = [{"ID": 1, "NAME": "Bob", "AMT": 100.50}]
        result = compare_by_key(expected, actual, SIMPLE_FIELDS, "ID")
        assert not result.passed
        assert result.mismatches[0].field_name == "NAME"


class TestSummary:
    def test_summary_output(self):
        expected = [{"ID": 1, "NAME": "Alice", "AMT": 100.50}]
        actual = [{"ID": 1, "NAME": "Bob", "AMT": 999.99}]
        result = compare_records(expected, actual, SIMPLE_FIELDS)
        summary = result.summary()
        assert "Field mismatches" in summary
        assert "NAME" in summary
