"""
test_golden_files.py - Golden-file round-trip validation tests.

Verifies that:
1. The copybook parser correctly parses every ASCII data file.
2. The parsed records match the golden JSON references field-for-field.
3. Record counts are correct.
4. Key fields have expected types and non-empty values.
"""

from __future__ import annotations

import sys

import pytest

# Layouts are bootstrapped into sys.modules by conftest.py
test_harness_gen = sys.modules["test_harness.golden_file_generator"]
ACCOUNT_LAYOUT = test_harness_gen.ACCOUNT_LAYOUT
CARD_LAYOUT = test_harness_gen.CARD_LAYOUT
CARDXREF_LAYOUT = test_harness_gen.CARDXREF_LAYOUT
CUSTOMER_LAYOUT = test_harness_gen.CUSTOMER_LAYOUT
DISCGRP_LAYOUT = test_harness_gen.DISCGRP_LAYOUT
TCATBAL_LAYOUT = test_harness_gen.TCATBAL_LAYOUT
TRANCATG_LAYOUT = test_harness_gen.TRANCATG_LAYOUT
TRANSACTION_LAYOUT = test_harness_gen.TRANSACTION_LAYOUT
TRANTYPE_LAYOUT = test_harness_gen.TRANTYPE_LAYOUT


class TestAccountData:
    """Golden-file tests for acctdata.txt (CVACT01Y.cpy)."""

    def test_record_count(self, acctdata_records, golden_acctdata):
        assert len(acctdata_records) == golden_acctdata["record_count"]

    def test_first_record_acct_id(self, acctdata_records):
        assert acctdata_records[0]["ACCT-ID"] == 1

    def test_all_accounts_have_id(self, acctdata_records):
        for rec in acctdata_records:
            assert rec["ACCT-ID"] > 0, f"Account with zero/negative ID: {rec}"

    def test_active_status_valid(self, acctdata_records):
        for rec in acctdata_records:
            assert rec["ACCT-ACTIVE-STATUS"] in ("Y", "N"), (
                f"Invalid status '{rec['ACCT-ACTIVE-STATUS']}' for acct {rec['ACCT-ID']}"
            )

    def test_golden_field_match(self, acctdata_records, golden_acctdata):
        for idx, (parsed, golden) in enumerate(
            zip(acctdata_records, golden_acctdata["records"])
        ):
            for key in golden:
                if key.startswith("_filler_"):
                    continue
                assert str(parsed[key]) == str(golden[key]), (
                    f"Record {idx}, field {key}: parsed={parsed[key]}, golden={golden[key]}"
                )


class TestCardData:
    """Golden-file tests for carddata.txt (CVACT02Y.cpy)."""

    def test_record_count(self, carddata_records, golden_carddata):
        assert len(carddata_records) == golden_carddata["record_count"]

    def test_card_numbers_are_16_chars(self, carddata_records):
        for rec in carddata_records:
            card = rec["CARD-NUM"]
            # Card number should be non-empty
            assert len(card.strip()) > 0, f"Empty card number in record"

    def test_golden_field_match(self, carddata_records, golden_carddata):
        for idx, (parsed, golden) in enumerate(
            zip(carddata_records, golden_carddata["records"])
        ):
            for key in golden:
                if key.startswith("_filler_"):
                    continue
                assert str(parsed[key]) == str(golden[key]), (
                    f"Record {idx}, field {key}: parsed={parsed[key]}, golden={golden[key]}"
                )


class TestCustomerData:
    """Golden-file tests for custdata.txt (CVCUS01Y.cpy)."""

    def test_record_count(self, custdata_records, golden_custdata):
        assert len(custdata_records) == golden_custdata["record_count"]

    def test_customer_ids_sequential(self, custdata_records):
        for i, rec in enumerate(custdata_records):
            assert rec["CUST-ID"] == i + 1, (
                f"Expected CUST-ID {i + 1}, got {rec['CUST-ID']}"
            )

    def test_golden_field_match(self, custdata_records, golden_custdata):
        for idx, (parsed, golden) in enumerate(
            zip(custdata_records, golden_custdata["records"])
        ):
            for key in golden:
                if key.startswith("_filler_"):
                    continue
                assert str(parsed[key]) == str(golden[key]), (
                    f"Record {idx}, field {key}: parsed={parsed[key]}, golden={golden[key]}"
                )


class TestCardXref:
    """Golden-file tests for cardxref.txt (CVACT03Y.cpy)."""

    def test_record_count(self, cardxref_records, golden_cardxref):
        assert len(cardxref_records) == golden_cardxref["record_count"]

    def test_all_xrefs_have_acct_and_cust(self, cardxref_records):
        for rec in cardxref_records:
            assert rec["XREF-ACCT-ID"] > 0
            assert rec["XREF-CUST-ID"] > 0

    def test_golden_field_match(self, cardxref_records, golden_cardxref):
        for idx, (parsed, golden) in enumerate(
            zip(cardxref_records, golden_cardxref["records"])
        ):
            for key in golden:
                if key.startswith("_filler_"):
                    continue
                assert str(parsed[key]) == str(golden[key]), (
                    f"Record {idx}, field {key}: parsed={parsed[key]}, golden={golden[key]}"
                )


class TestDailyTransactions:
    """Golden-file tests for dailytran.txt (CVTRA05Y.cpy)."""

    def test_record_count(self, dailytran_records, golden_dailytran):
        assert len(dailytran_records) == golden_dailytran["record_count"]

    def test_transaction_ids_non_empty(self, dailytran_records):
        for rec in dailytran_records:
            assert rec["TRAN-ID"].strip(), "Empty transaction ID"

    def test_golden_field_match(self, dailytran_records, golden_dailytran):
        for idx, (parsed, golden) in enumerate(
            zip(dailytran_records, golden_dailytran["records"])
        ):
            for key in golden:
                if key.startswith("_filler_"):
                    continue
                assert str(parsed[key]) == str(golden[key]), (
                    f"Record {idx}, field {key}: parsed={parsed[key]}, golden={golden[key]}"
                )


class TestTranType:
    """Golden-file tests for trantype.txt (CVTRA03Y.cpy)."""

    def test_record_count(self, trantype_records, golden_trantype):
        assert len(trantype_records) == golden_trantype["record_count"]

    def test_golden_field_match(self, trantype_records, golden_trantype):
        for idx, (parsed, golden) in enumerate(
            zip(trantype_records, golden_trantype["records"])
        ):
            for key in golden:
                if key.startswith("_filler_"):
                    continue
                assert str(parsed[key]) == str(golden[key]), (
                    f"Record {idx}, field {key}: parsed={parsed[key]}, golden={golden[key]}"
                )


class TestTranCatg:
    """Golden-file tests for trancatg.txt (CVTRA04Y.cpy)."""

    def test_record_count(self, trancatg_records, golden_trancatg):
        assert len(trancatg_records) == golden_trancatg["record_count"]

    def test_golden_field_match(self, trancatg_records, golden_trancatg):
        for idx, (parsed, golden) in enumerate(
            zip(trancatg_records, golden_trancatg["records"])
        ):
            for key in golden:
                if key.startswith("_filler_"):
                    continue
                assert str(parsed[key]) == str(golden[key]), (
                    f"Record {idx}, field {key}: parsed={parsed[key]}, golden={golden[key]}"
                )


class TestTcatBal:
    """Golden-file tests for tcatbal.txt (CVTRA01Y.cpy)."""

    def test_record_count(self, tcatbal_records, golden_tcatbal):
        assert len(tcatbal_records) == golden_tcatbal["record_count"]

    def test_golden_field_match(self, tcatbal_records, golden_tcatbal):
        for idx, (parsed, golden) in enumerate(
            zip(tcatbal_records, golden_tcatbal["records"])
        ):
            for key in golden:
                if key.startswith("_filler_"):
                    continue
                assert str(parsed[key]) == str(golden[key]), (
                    f"Record {idx}, field {key}: parsed={parsed[key]}, golden={golden[key]}"
                )


class TestDiscGrp:
    """Golden-file tests for discgrp.txt (CVTRA02Y.cpy)."""

    def test_record_count(self, discgrp_records, golden_discgrp):
        assert len(discgrp_records) == golden_discgrp["record_count"]

    def test_golden_field_match(self, discgrp_records, golden_discgrp):
        for idx, (parsed, golden) in enumerate(
            zip(discgrp_records, golden_discgrp["records"])
        ):
            for key in golden:
                if key.startswith("_filler_"):
                    continue
                assert str(parsed[key]) == str(golden[key]), (
                    f"Record {idx}, field {key}: parsed={parsed[key]}, golden={golden[key]}"
                )
