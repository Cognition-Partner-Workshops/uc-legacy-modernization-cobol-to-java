"""Unit tests for the field-by-field comparison utility."""

from __future__ import annotations

import json
import tempfile
from decimal import Decimal

from cobol_test_harness.comparator import (
    ComparisonResult,
    FieldMismatch,
    compare_json_files,
    compare_records,
)

# ---------------------------------------------------------------------------
# Basic comparison tests
# ---------------------------------------------------------------------------


class TestCompareRecords:
    """Tests for the compare_records function."""

    def test_identical_records_pass(self) -> None:
        records = [
            {"ACCT-ID": "1", "STATUS": "Y", "BAL": "100.00"},
            {"ACCT-ID": "2", "STATUS": "N", "BAL": "200.00"},
        ]
        result = compare_records(records, records, source_file="test.txt")
        assert result.passed
        assert result.mismatch_count == 0
        assert result.record_count_match

    def test_value_mismatch_detected(self) -> None:
        expected = [{"ACCT-ID": "1", "BAL": "100.00"}]
        actual = [{"ACCT-ID": "1", "BAL": "100.01"}]
        result = compare_records(expected, actual, source_file="test.txt")
        assert not result.passed
        assert result.mismatch_count == 1
        assert result.mismatches[0].field_name == "BAL"
        assert result.mismatches[0].mismatch_type == "VALUE_MISMATCH"

    def test_record_count_mismatch(self) -> None:
        expected = [{"A": "1"}, {"A": "2"}]
        actual = [{"A": "1"}]
        result = compare_records(expected, actual, source_file="test.txt")
        assert not result.passed
        assert not result.record_count_match
        assert result.total_records_expected == 2
        assert result.total_records_actual == 1

    def test_extra_records_in_actual(self) -> None:
        expected = [{"A": "1"}]
        actual = [{"A": "1"}, {"A": "2"}]
        result = compare_records(expected, actual, source_file="test.txt")
        assert not result.passed
        # One mismatch for the extra record
        extra = [m for m in result.mismatches if m.mismatch_type == "RECORD_EXTRA_IN_ACTUAL"]
        assert len(extra) == 1

    def test_missing_field_in_actual(self) -> None:
        expected = [{"A": "1", "B": "2"}]
        actual = [{"A": "1"}]
        result = compare_records(expected, actual, source_file="test.txt")
        assert not result.passed
        missing = [m for m in result.mismatches if m.mismatch_type == "FIELD_MISSING_IN_ACTUAL"]
        assert len(missing) == 1
        assert missing[0].field_name == "B"

    def test_extra_field_in_actual(self) -> None:
        expected = [{"A": "1"}]
        actual = [{"A": "1", "B": "2"}]
        result = compare_records(expected, actual, source_file="test.txt")
        assert not result.passed
        extra = [m for m in result.mismatches if m.mismatch_type == "FIELD_MISSING_IN_EXPECTED"]
        assert len(extra) == 1

    def test_ignore_fields(self) -> None:
        expected = [{"A": "1", "B": "2"}]
        actual = [{"A": "1", "B": "999"}]
        result = compare_records(
            expected, actual, source_file="test.txt", ignore_fields={"B"}
        )
        assert result.passed

    def test_empty_records_pass(self) -> None:
        result = compare_records([], [], source_file="test.txt")
        assert result.passed


# ---------------------------------------------------------------------------
# Numeric tolerance
# ---------------------------------------------------------------------------


class TestNumericTolerance:
    """Tests for numeric comparison with tolerance."""

    def test_within_tolerance(self) -> None:
        expected = [{"AMT": "100.00"}]
        actual = [{"AMT": "100.005"}]
        result = compare_records(
            expected, actual,
            source_file="test.txt",
            numeric_tolerance=Decimal("0.01"),
        )
        assert result.passed

    def test_outside_tolerance(self) -> None:
        expected = [{"AMT": "100.00"}]
        actual = [{"AMT": "100.02"}]
        result = compare_records(
            expected, actual,
            source_file="test.txt",
            numeric_tolerance=Decimal("0.01"),
        )
        assert not result.passed

    def test_zero_tolerance_exact_match(self) -> None:
        expected = [{"AMT": "100.00"}]
        actual = [{"AMT": "100.00"}]
        result = compare_records(
            expected, actual,
            source_file="test.txt",
            numeric_tolerance=Decimal("0"),
        )
        assert result.passed

    def test_equivalent_numeric_formats(self) -> None:
        # "100" and "100.00" should be considered equivalent after normalization
        expected = [{"AMT": "100"}]
        actual = [{"AMT": "1E+2"}]
        result = compare_records(expected, actual, source_file="test.txt")
        assert result.passed


# ---------------------------------------------------------------------------
# ComparisonResult
# ---------------------------------------------------------------------------


class TestComparisonResult:
    """Tests for ComparisonResult helper methods."""

    def test_summary_pass(self) -> None:
        result = ComparisonResult(
            source_file="test.txt",
            total_records_expected=10,
            total_records_actual=10,
            records_compared=10,
            mismatches=[],
            record_count_match=True,
        )
        summary = result.summary()
        assert "PASS" in summary

    def test_summary_fail(self) -> None:
        result = ComparisonResult(
            source_file="test.txt",
            total_records_expected=10,
            total_records_actual=10,
            records_compared=10,
            mismatches=[FieldMismatch(0, "F", "1", "2")],
            record_count_match=True,
        )
        summary = result.summary()
        assert "FAIL" in summary

    def test_to_dict(self) -> None:
        result = ComparisonResult(
            source_file="test.txt",
            total_records_expected=5,
            total_records_actual=5,
            records_compared=5,
            mismatches=[],
            record_count_match=True,
        )
        d = result.to_dict()
        assert d["status"] == "PASS"
        assert d["mismatch_count"] == 0


# ---------------------------------------------------------------------------
# JSON file comparison
# ---------------------------------------------------------------------------


class TestCompareJsonFiles:
    """Tests for comparing two JSON golden files."""

    def test_identical_files_pass(self) -> None:
        data = {
            "_metadata": {
                "fields": [
                    {"name": "A", "pic_type": "X(1)", "offset": 0, "length": 1},
                ]
            },
            "records": [{"A": "1"}, {"A": "2"}],
        }
        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".json", delete=False
        ) as f1:
            json.dump(data, f1)
            f1.flush()
            with tempfile.NamedTemporaryFile(
                mode="w", suffix=".json", delete=False
            ) as f2:
                json.dump(data, f2)
                f2.flush()
                result = compare_json_files(f1.name, f2.name)
        assert result.passed

    def test_different_files_fail(self) -> None:
        expected = {"records": [{"A": "1"}]}
        actual = {"records": [{"A": "2"}]}
        with tempfile.NamedTemporaryFile(
            mode="w", suffix=".json", delete=False
        ) as f1:
            json.dump(expected, f1)
            f1.flush()
            with tempfile.NamedTemporaryFile(
                mode="w", suffix=".json", delete=False
            ) as f2:
                json.dump(actual, f2)
                f2.flush()
                result = compare_json_files(f1.name, f2.name)
        assert not result.passed
