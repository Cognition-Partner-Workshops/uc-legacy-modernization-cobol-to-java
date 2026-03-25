"""
Reconciliation test suite.

Validates cross-entity data integrity invariants using the golden files.
"""

import pytest

from reconciliation import ReconciliationRunner, format_reconciliation_report
from conftest import GOLDEN_DIR, EXPECTED_COUNTS


@pytest.fixture
def runner():
    """Create a reconciliation runner."""
    return ReconciliationRunner(GOLDEN_DIR)


class TestReconciliationChecks:
    """Run each reconciliation check individually."""

    def test_r01_card_references_account(self, runner):
        result = runner.check_r01_card_references_account()
        assert result.passed, (
            f"R-01 FAILED: {result.details}\n"
            f"Violations: {result.violations[:5]}"
        )

    def test_r02_xref_integrity(self, runner):
        result = runner.check_r02_xref_integrity()
        assert result.passed, (
            f"R-02 FAILED: {result.details}\n"
            f"Violations: {result.violations[:5]}"
        )

    def test_r03_transaction_card_refs(self, runner):
        result = runner.check_r03_transaction_card_refs()
        assert result.passed, (
            f"R-03 FAILED: {result.details}\n"
            f"Violations: {result.violations[:5]}"
        )

    def test_r04_category_balance_coverage(self, runner):
        result = runner.check_r04_category_balance_coverage()
        assert result.passed, (
            f"R-04 FAILED: {result.details}\n"
            f"Violations: {result.violations[:5]}"
        )

    def test_r05_disclosure_group_refs(self, runner):
        result = runner.check_r05_disclosure_group_refs()
        assert result.passed, (
            f"R-05 FAILED: {result.details}\n"
            f"Violations: {result.violations[:5]}"
        )

    def test_r06_transaction_category_codes(self, runner):
        result = runner.check_r06_transaction_category_codes()
        assert result.passed, (
            f"R-06 FAILED: {result.details}\n"
            f"Violations: {result.violations[:5]}"
        )

    def test_r07_transaction_type_codes(self, runner):
        result = runner.check_r07_transaction_type_codes()
        assert result.passed, (
            f"R-07 FAILED: {result.details}\n"
            f"Violations: {result.violations[:5]}"
        )

    def test_r08_record_counts(self, runner):
        result = runner.check_r08_record_counts(EXPECTED_COUNTS)
        assert result.passed, (
            f"R-08 FAILED: {result.details}\n"
            f"Violations: {result.violations[:5]}"
        )

    def test_r09_account_status_consistency(self, runner):
        result = runner.check_r09_account_status_consistency()
        assert result.passed, (
            f"R-09 FAILED: {result.details}\n"
            f"Violations: {result.violations[:5]}"
        )

    def test_r10_customer_card_symmetry(self, runner):
        result = runner.check_r10_customer_card_symmetry()
        assert result.passed, (
            f"R-10 FAILED: {result.details}\n"
            f"Violations: {result.violations[:5]}"
        )


class TestReconciliationRunAll:
    """Run all reconciliation checks together and produce a report."""

    def test_all_checks_pass(self, runner):
        results = runner.run_all(EXPECTED_COUNTS)
        report = format_reconciliation_report(results)

        failed = [r for r in results if not r.passed]
        assert len(failed) == 0, (
            f"{len(failed)} reconciliation checks failed:\n{report}"
        )
