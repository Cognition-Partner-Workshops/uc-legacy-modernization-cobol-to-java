"""
Reconciliation check tests.

Runs all referential integrity and business rule checks against the
golden-file dataset and reports any violations.
"""

from __future__ import annotations

import os

import pytest

from reconciliation import (
    CheckResult,
    check_br_003,
    check_br_004,
    check_br_005,
    check_ri_001,
    check_ri_002,
    check_ri_003,
    check_ri_004,
    check_ri_005,
    check_ri_006,
    load_golden_file,
    run_all_reconciliation_checks,
)


def _skip_if_no_golden(golden_dir: str) -> None:
    if not os.path.exists(os.path.join(golden_dir, "acctdata.golden.json")):
        pytest.skip("Golden files not yet generated")


class TestReferentialIntegrity:
    """Referential integrity checks between data files."""

    def test_ri_001_xref_card_in_carddata(self, golden_dir):
        _skip_if_no_golden(golden_dir)
        xref = load_golden_file(golden_dir, "cardxref")
        cards = load_golden_file(golden_dir, "carddata")
        result = check_ri_001(xref, cards)
        assert result.passed, (
            f"{result.summary}\n"
            + "\n".join(v.message for v in result.violations[:10])
        )

    def test_ri_002_xref_acct_in_acctdata(self, golden_dir):
        _skip_if_no_golden(golden_dir)
        xref = load_golden_file(golden_dir, "cardxref")
        accounts = load_golden_file(golden_dir, "acctdata")
        result = check_ri_002(xref, accounts)
        assert result.passed, (
            f"{result.summary}\n"
            + "\n".join(v.message for v in result.violations[:10])
        )

    def test_ri_003_xref_cust_in_custdata(self, golden_dir):
        _skip_if_no_golden(golden_dir)
        xref = load_golden_file(golden_dir, "cardxref")
        customers = load_golden_file(golden_dir, "custdata")
        result = check_ri_003(xref, customers)
        assert result.passed, (
            f"{result.summary}\n"
            + "\n".join(v.message for v in result.violations[:10])
        )

    def test_ri_004_card_acct_in_acctdata(self, golden_dir):
        _skip_if_no_golden(golden_dir)
        cards = load_golden_file(golden_dir, "carddata")
        accounts = load_golden_file(golden_dir, "acctdata")
        result = check_ri_004(cards, accounts)
        assert result.passed, (
            f"{result.summary}\n"
            + "\n".join(v.message for v in result.violations[:10])
        )

    def test_ri_005_dailytran_card_in_xref(self, golden_dir):
        _skip_if_no_golden(golden_dir)
        dailytran = load_golden_file(golden_dir, "dailytran")
        xref = load_golden_file(golden_dir, "cardxref")
        result = check_ri_005(dailytran, xref)
        assert result.passed, (
            f"{result.summary}\n"
            + "\n".join(v.message for v in result.violations[:10])
        )

    def test_ri_006_tcatbal_acct_in_acctdata(self, golden_dir):
        _skip_if_no_golden(golden_dir)
        tcatbal = load_golden_file(golden_dir, "tcatbal")
        accounts = load_golden_file(golden_dir, "acctdata")
        result = check_ri_006(tcatbal, accounts)
        assert result.passed, (
            f"{result.summary}\n"
            + "\n".join(v.message for v in result.violations[:10])
        )


class TestBusinessRules:
    """Business rule invariant checks."""

    def test_br_003_active_card_active_account(self, golden_dir):
        _skip_if_no_golden(golden_dir)
        cards = load_golden_file(golden_dir, "carddata")
        accounts = load_golden_file(golden_dir, "acctdata")
        result = check_br_003(cards, accounts)
        assert result.passed, (
            f"{result.summary}\n"
            + "\n".join(v.message for v in result.violations[:10])
        )

    def test_br_004_dailytran_type_in_trantype(self, golden_dir):
        _skip_if_no_golden(golden_dir)
        dailytran = load_golden_file(golden_dir, "dailytran")
        trantypes = load_golden_file(golden_dir, "trantype")
        result = check_br_004(dailytran, trantypes)
        assert result.passed, (
            f"{result.summary}\n"
            + "\n".join(v.message for v in result.violations[:10])
        )

    def test_br_005_dailytran_category_in_trancatg(self, golden_dir):
        _skip_if_no_golden(golden_dir)
        dailytran = load_golden_file(golden_dir, "dailytran")
        trancatg = load_golden_file(golden_dir, "trancatg")
        result = check_br_005(dailytran, trancatg)
        assert result.passed, (
            f"{result.summary}\n"
            + "\n".join(v.message for v in result.violations[:10])
        )


class TestFullReconciliation:
    """Run the complete reconciliation suite."""

    def test_all_checks(self, golden_dir):
        _skip_if_no_golden(golden_dir)
        results = run_all_reconciliation_checks(golden_dir)
        failures = [r for r in results if not r.passed]
        if failures:
            report = "\n".join(r.summary for r in failures)
            pytest.fail(f"{len(failures)} reconciliation checks failed:\n{report}")
