"""
test_reconciliation.py - Reconciliation check tests against CardDemo baseline data.

These tests validate business-rule invariants using the shipped ASCII data files.
"""

from __future__ import annotations

import sys

# Reconciliation module is bootstrapped into sys.modules by conftest.py
_recon = sys.modules["test_harness.reconciliation"]
check_card_account_linkage = _recon.check_card_account_linkage
check_output_record_count = _recon.check_output_record_count
check_record_count_balance = _recon.check_record_count_balance
check_tcatbal_consistency = _recon.check_tcatbal_consistency
check_xref_referential_integrity = _recon.check_xref_referential_integrity


class TestReferentialIntegrity:
    """Verify cross-file referential integrity in the baseline data."""

    def test_xref_references_valid_accounts_and_customers(
        self, cardxref_records, acctdata_records, custdata_records
    ):
        result = check_xref_referential_integrity(
            xref_records=cardxref_records,
            account_records=acctdata_records,
            customer_records=custdata_records,
        )
        assert result.passed, result.message

    def test_cards_reference_valid_accounts(
        self, carddata_records, acctdata_records
    ):
        result = check_card_account_linkage(
            card_records=carddata_records,
            account_records=acctdata_records,
        )
        assert result.passed, result.message


class TestBaselineRecordCounts:
    """Verify expected record counts in the baseline data files."""

    def test_acctdata_count(self, acctdata_records):
        result = check_output_record_count(
            job="DATA_LOAD",
            expected_count=50,
            actual_count=len(acctdata_records),
            dataset_name="acctdata",
        )
        assert result.passed, result.message

    def test_carddata_count(self, carddata_records):
        result = check_output_record_count(
            job="DATA_LOAD",
            expected_count=50,
            actual_count=len(carddata_records),
            dataset_name="carddata",
        )
        assert result.passed, result.message

    def test_custdata_count(self, custdata_records):
        result = check_output_record_count(
            job="DATA_LOAD",
            expected_count=50,
            actual_count=len(custdata_records),
            dataset_name="custdata",
        )
        assert result.passed, result.message

    def test_cardxref_count(self, cardxref_records):
        result = check_output_record_count(
            job="DATA_LOAD",
            expected_count=50,
            actual_count=len(cardxref_records),
            dataset_name="cardxref",
        )
        assert result.passed, result.message

    def test_dailytran_count(self, dailytran_records):
        result = check_output_record_count(
            job="DATA_LOAD",
            expected_count=300,
            actual_count=len(dailytran_records),
            dataset_name="dailytran",
        )
        assert result.passed, result.message

    def test_tcatbal_count(self, tcatbal_records):
        result = check_output_record_count(
            job="DATA_LOAD",
            expected_count=50,
            actual_count=len(tcatbal_records),
            dataset_name="tcatbal",
        )
        assert result.passed, result.message


class TestTcatbalConsistency:
    """Verify transaction-category balance records exist."""

    def test_tcatbal_records_present(self, tcatbal_records, dailytran_records):
        result = check_tcatbal_consistency(
            job="POSTTRAN",
            tcatbal_records=tcatbal_records,
            transaction_records=dailytran_records,
        )
        assert result.passed, result.message


class TestRecordCountBalance:
    """Demonstrate the record_count_balance check pattern."""

    def test_balanced_counts(self):
        """Simulated check: 300 input = 295 accepted + 5 rejected."""
        result = check_record_count_balance(
            job="POSTTRAN",
            input_count=300,
            accepted_count=295,
            rejected_count=5,
        )
        assert result.passed, result.message

    def test_imbalanced_counts_detected(self):
        """Verify that an imbalance is correctly flagged."""
        result = check_record_count_balance(
            job="POSTTRAN",
            input_count=300,
            accepted_count=290,
            rejected_count=5,
        )
        assert not result.passed
        assert "295" in result.message or "300" in result.message
