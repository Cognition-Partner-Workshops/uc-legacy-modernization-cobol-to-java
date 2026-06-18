"""Tests for reconciliation check functions."""

from __future__ import annotations

from pathlib import Path

import pytest

from harness.parser import (
    ACCOUNT_FIELDS,
    CARD_FIELDS,
    CARDXREF_FIELDS,
    CUSTOMER_FIELDS,
    DAILYTRAN_FIELDS,
    TCATBAL_FIELDS,
    parse_file,
)
from harness.reconciliation import (
    check_dailytran_card_in_xref,
    check_posttran_counts,
    check_record_count,
    check_referential_integrity,
    check_sum_balance,
    check_tcatbal_acct_in_acctdata,
    check_transaction_amount_conservation,
    check_xref_acct_in_acctdata,
    check_xref_card_in_carddata,
    check_xref_cust_in_custdata,
    run_data_integrity_checks,
    sum_field,
)

DATA_DIR = Path(__file__).resolve().parent.parent.parent / "app" / "data" / "ASCII"


class TestRecordCount:
    def test_exact_match(self):
        records = [{"a": 1}, {"a": 2}, {"a": 3}]
        result = check_record_count(records, 3, "test count")
        assert result.passed

    def test_count_mismatch(self):
        records = [{"a": 1}, {"a": 2}]
        result = check_record_count(records, 3, "test count")
        assert not result.passed
        assert result.actual == 2


class TestPosttranCounts:
    def test_balanced(self):
        inp = [{}] * 10
        posted = [{}] * 7
        rejected = [{}] * 3
        result = check_posttran_counts(inp, posted, rejected)
        assert result.passed

    def test_unbalanced(self):
        inp = [{}] * 10
        posted = [{}] * 7
        rejected = [{}] * 4
        result = check_posttran_counts(inp, posted, rejected)
        assert not result.passed


class TestSumField:
    def test_basic_sum(self):
        records = [{"AMT": 10.5}, {"AMT": 20.0}, {"AMT": -5.5}]
        assert sum_field(records, "AMT") == 25.0

    def test_missing_field(self):
        records = [{"AMT": 10.0}, {"OTHER": 5.0}]
        assert sum_field(records, "AMT") == 10.0


class TestAmountConservation:
    def test_conserved(self):
        daily = [{"DALYTRAN-AMT": 100.0}, {"DALYTRAN-AMT": 50.0}]
        posted = [{"TRAN-AMT": 100.0}]
        rejected = [{"DALYTRAN-AMT": 50.0}]
        result = check_transaction_amount_conservation(daily, posted, rejected)
        assert result.passed


class TestReferentialIntegrity:
    def test_all_present(self):
        children = [{"FK": "1"}, {"FK": "2"}]
        parents = [{"PK": "1"}, {"PK": "2"}, {"PK": "3"}]
        result = check_referential_integrity(children, "FK", parents, "PK", "test")
        assert result.passed

    def test_orphans(self):
        children = [{"FK": "1"}, {"FK": "99"}]
        parents = [{"PK": "1"}, {"PK": "2"}]
        result = check_referential_integrity(children, "FK", parents, "PK", "test")
        assert not result.passed
        assert result.actual == 1


@pytest.mark.skipif(not DATA_DIR.exists(), reason="Data dir not available")
class TestLiveDataIntegrity:
    """Run reconciliation checks against the actual sample data files."""

    @pytest.fixture(autouse=True)
    def load_data(self):
        self.acct = parse_file(DATA_DIR / "acctdata.txt", ACCOUNT_FIELDS, 300)
        self.card = parse_file(DATA_DIR / "carddata.txt", CARD_FIELDS, 150)
        self.xref = parse_file(DATA_DIR / "cardxref.txt", CARDXREF_FIELDS, 50)
        self.cust = parse_file(DATA_DIR / "custdata.txt", CUSTOMER_FIELDS, 500)
        self.daily = parse_file(DATA_DIR / "dailytran.txt", DAILYTRAN_FIELDS, 350)
        self.tcatbal = parse_file(DATA_DIR / "tcatbal.txt", TCATBAL_FIELDS, 50)

    def test_record_counts(self):
        assert len(self.acct) == 50
        assert len(self.card) == 50
        assert len(self.xref) == 50
        assert len(self.cust) == 50
        assert len(self.daily) == 300
        assert len(self.tcatbal) == 50

    def test_xref_cards_in_carddata(self):
        result = check_xref_card_in_carddata(self.xref, self.card)
        assert result.passed, result

    def test_xref_accts_in_acctdata(self):
        result = check_xref_acct_in_acctdata(self.xref, self.acct)
        assert result.passed, result

    def test_xref_custs_in_custdata(self):
        result = check_xref_cust_in_custdata(self.xref, self.cust)
        assert result.passed, result

    def test_tcatbal_accts_in_acctdata(self):
        result = check_tcatbal_acct_in_acctdata(self.tcatbal, self.acct)
        assert result.passed, result

    def test_dailytran_cards_in_xref(self):
        result = check_dailytran_card_in_xref(self.daily, self.xref)
        assert result.passed, result

    def test_full_suite(self):
        report = run_data_integrity_checks(
            self.acct, self.card, self.xref, self.cust, self.daily, self.tcatbal
        )
        assert report.all_passed, report.summary()
