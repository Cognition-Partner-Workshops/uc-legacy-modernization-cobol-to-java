"""Unit tests for the reconciliation check functions."""

from __future__ import annotations

from decimal import Decimal

from cobol_test_harness.reconciliation import (
    CheckResult,
    ReconciliationReport,
    check_field_sum,
    check_record_count,
    check_referential_integrity,
    compute_field_sums,
)

# ---------------------------------------------------------------------------
# Record count validation
# ---------------------------------------------------------------------------


class TestCheckRecordCount:
    """Tests for record count validation."""

    def test_correct_count_passes(self) -> None:
        records = [{"A": "1"}, {"A": "2"}, {"A": "3"}]
        result = check_record_count(records, 3, "test.txt")
        assert result.passed
        assert result.actual == "3"

    def test_incorrect_count_fails(self) -> None:
        records = [{"A": "1"}, {"A": "2"}]
        result = check_record_count(records, 3, "test.txt")
        assert not result.passed
        assert result.expected == "3"
        assert result.actual == "2"

    def test_empty_records_zero_expected(self) -> None:
        result = check_record_count([], 0, "test.txt")
        assert result.passed

    def test_empty_records_nonzero_expected(self) -> None:
        result = check_record_count([], 5, "test.txt")
        assert not result.passed


# ---------------------------------------------------------------------------
# Numeric field sum validation
# ---------------------------------------------------------------------------


class TestCheckFieldSum:
    """Tests for numeric field sum validation."""

    def test_correct_sum_passes(self) -> None:
        records = [
            {"AMT": "100.00"},
            {"AMT": "200.50"},
            {"AMT": "50.25"},
        ]
        result = check_field_sum(records, "AMT", expected_sum="350.75", file_name="test")
        assert result.passed

    def test_incorrect_sum_fails(self) -> None:
        records = [
            {"AMT": "100.00"},
            {"AMT": "200.00"},
        ]
        result = check_field_sum(records, "AMT", expected_sum="999.99", file_name="test")
        assert not result.passed
        assert result.expected == "999.99"
        assert result.actual == "300.00"

    def test_no_expected_sum_always_passes(self) -> None:
        records = [{"AMT": "100.00"}, {"AMT": "200.00"}]
        result = check_field_sum(records, "AMT", expected_sum=None, file_name="test")
        assert result.passed
        assert result.actual == "300.00"

    def test_negative_values(self) -> None:
        records = [
            {"AMT": "100.00"},
            {"AMT": "-50.00"},
        ]
        result = check_field_sum(records, "AMT", expected_sum="50.00", file_name="test")
        assert result.passed

    def test_missing_field_treated_as_zero(self) -> None:
        records = [{"AMT": "100.00"}, {"OTHER": "200.00"}]
        result = check_field_sum(records, "AMT", expected_sum="100.00", file_name="test")
        assert result.passed

    def test_empty_records(self) -> None:
        result = check_field_sum([], "AMT", expected_sum="0", file_name="test")
        assert result.passed


class TestComputeFieldSums:
    """Tests for computing multiple field sums."""

    def test_compute_multiple_sums(self) -> None:
        records = [
            {"A": "10", "B": "20"},
            {"A": "30", "B": "40"},
        ]
        sums = compute_field_sums(records, ["A", "B"])
        assert sums["A"] == Decimal("40")
        assert sums["B"] == Decimal("60")

    def test_missing_fields(self) -> None:
        records = [{"A": "10"}, {"A": "20"}]
        sums = compute_field_sums(records, ["A", "B"])
        assert sums["A"] == Decimal("30")
        assert sums["B"] == Decimal("0")


# ---------------------------------------------------------------------------
# Cross-reference integrity checks
# ---------------------------------------------------------------------------


class TestCheckReferentialIntegrity:
    """Tests for referential integrity checks."""

    def test_all_references_valid(self) -> None:
        children = [
            {"FK": "1"},
            {"FK": "2"},
            {"FK": "3"},
        ]
        parents = [
            {"PK": "1"},
            {"PK": "2"},
            {"PK": "3"},
            {"PK": "4"},
        ]
        result = check_referential_integrity(
            children, "FK", parents, "PK", "test_check"
        )
        assert result.passed

    def test_missing_reference_fails(self) -> None:
        children = [
            {"FK": "1"},
            {"FK": "2"},
            {"FK": "999"},  # Does not exist in parents
        ]
        parents = [
            {"PK": "1"},
            {"PK": "2"},
        ]
        result = check_referential_integrity(
            children, "FK", parents, "PK", "test_check"
        )
        assert not result.passed
        assert "999" in result.details

    def test_empty_children_passes(self) -> None:
        parents = [{"PK": "1"}]
        result = check_referential_integrity(
            [], "FK", parents, "PK", "test_check"
        )
        assert result.passed

    def test_empty_parents_with_children_fails(self) -> None:
        children = [{"FK": "1"}]
        result = check_referential_integrity(
            children, "FK", [], "PK", "test_check"
        )
        assert not result.passed

    def test_multiple_missing_references(self) -> None:
        children = [{"FK": str(i)} for i in range(20)]
        parents = [{"PK": str(i)} for i in range(5)]
        result = check_referential_integrity(
            children, "FK", parents, "PK", "test_check"
        )
        assert not result.passed
        assert "15" in result.actual  # 15 missing references

    def test_string_matching_with_leading_zeros(self) -> None:
        children = [{"FK": "00001"}]
        parents = [{"PK": "00001"}]
        result = check_referential_integrity(
            children, "FK", parents, "PK", "test_check"
        )
        assert result.passed


# ---------------------------------------------------------------------------
# ReconciliationReport
# ---------------------------------------------------------------------------


class TestReconciliationReport:
    """Tests for the ReconciliationReport class."""

    def test_all_passed(self) -> None:
        report = ReconciliationReport(checks=[
            CheckResult("check1", True, "1", "1"),
            CheckResult("check2", True, "2", "2"),
        ])
        assert report.all_passed
        assert report.pass_count == 2
        assert report.fail_count == 0

    def test_some_failed(self) -> None:
        report = ReconciliationReport(checks=[
            CheckResult("check1", True, "1", "1"),
            CheckResult("check2", False, "2", "3"),
        ])
        assert not report.all_passed
        assert report.pass_count == 1
        assert report.fail_count == 1

    def test_summary_contains_failures(self) -> None:
        report = ReconciliationReport(checks=[
            CheckResult("bad_check", False, "10", "5", "Something went wrong"),
        ])
        summary = report.summary()
        assert "FAILURES DETECTED" in summary
        assert "bad_check" in summary

    def test_to_dict(self) -> None:
        report = ReconciliationReport(checks=[
            CheckResult("check1", True, "1", "1"),
        ])
        d = report.to_dict()
        assert d["status"] == "PASS"
        assert d["total_checks"] == 1
        assert len(d["checks"]) == 1

    def test_empty_report(self) -> None:
        report = ReconciliationReport()
        assert report.all_passed  # vacuously true
        assert report.pass_count == 0
