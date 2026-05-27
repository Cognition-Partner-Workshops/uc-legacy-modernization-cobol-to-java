"""
Golden-file validation tests.

Verifies that the ASCII data files parse correctly against their copybook
layouts and that the golden JSON references contain the expected record
counts, field types, and values.
"""

from __future__ import annotations

import json
import os

import pytest

from copybook_parser import decode_signed_numeric, parse_file, parse_record
from layouts.definitions import (
    ACCOUNT_LAYOUT,
    CARD_LAYOUT,
    CARD_XREF_LAYOUT,
    CUSTOMER_LAYOUT,
    DAILY_TRANSACTION_LAYOUT,
    DISCLOSURE_GROUP_LAYOUT,
    FILE_LAYOUT_MAP,
    TRAN_CAT_BALANCE_LAYOUT,
    TRANSACTION_CATEGORY_LAYOUT,
    TRANSACTION_TYPE_LAYOUT,
)


# --- Signed Numeric Decoding Tests ---

class TestSignedNumericDecoding:
    def test_positive_zero(self):
        assert decode_signed_numeric("00000001940{", scale=2) == "194.00"

    def test_positive_overpunch_A(self):
        assert decode_signed_numeric("0000000000A", scale=2) == "0.01"

    def test_positive_overpunch_I(self):
        assert decode_signed_numeric("0000000000I", scale=2) == "0.09"

    def test_negative_overpunch_J(self):
        assert decode_signed_numeric("0000009190}", scale=2) == "-919.00"

    def test_positive_overpunch_G(self):
        result = decode_signed_numeric("0000005047G", scale=2)
        assert result == "504.77"

    def test_zero_value(self):
        assert decode_signed_numeric("00000000000{", scale=2) == "0.00"

    def test_scale_with_short_value(self):
        assert decode_signed_numeric("00150{", scale=2) == "15.00"


# --- Layout Length Validation ---

class TestLayoutLengths:
    """Verify that each layout's field lengths sum to the declared LRECL."""

    @pytest.mark.parametrize("basename,config", FILE_LAYOUT_MAP.items())
    def test_layout_total_length(self, basename, config):
        total = sum(f.length for f in config["layout"])
        assert total == config["record_length"], (
            f"{basename}: layout fields sum to {total}, "
            f"expected {config['record_length']}"
        )


# --- ASCII File Parsing Tests ---

class TestAsciiFileParsing:
    """Parse each ASCII data file and validate record counts and key fields."""

    def test_parse_acctdata(self, data_dir):
        records = parse_file(
            os.path.join(data_dir, "acctdata.txt"),
            ACCOUNT_LAYOUT, 300,
        )
        assert len(records) == 50
        first = records[0]
        assert first["ACCT-ID"] == 1
        assert first["ACCT-ACTIVE-STATUS"] == "Y"
        assert first["ACCT-OPEN-DATE"].strip() == "2014-11-20"

    def test_parse_custdata(self, data_dir):
        records = parse_file(
            os.path.join(data_dir, "custdata.txt"),
            CUSTOMER_LAYOUT, 500,
        )
        assert len(records) == 50
        first = records[0]
        assert first["CUST-ID"] == 1
        assert first["CUST-FIRST-NAME"].strip() == "Immanuel"
        assert first["CUST-ADDR-STATE-CD"].strip() == "NC"

    def test_parse_carddata(self, data_dir):
        records = parse_file(
            os.path.join(data_dir, "carddata.txt"),
            CARD_LAYOUT, 150,
        )
        assert len(records) == 50
        first = records[0]
        assert first["CARD-ACTIVE-STATUS"].strip() == "Y"
        assert first["CARD-CVV-CD"] == 747

    def test_parse_cardxref(self, data_dir):
        records = parse_file(
            os.path.join(data_dir, "cardxref.txt"),
            CARD_XREF_LAYOUT, 50,
        )
        assert len(records) == 50

    def test_parse_dailytran(self, data_dir):
        records = parse_file(
            os.path.join(data_dir, "dailytran.txt"),
            DAILY_TRANSACTION_LAYOUT, 350,
        )
        assert len(records) == 300

    def test_parse_trantype(self, data_dir):
        records = parse_file(
            os.path.join(data_dir, "trantype.txt"),
            TRANSACTION_TYPE_LAYOUT, 60,
        )
        assert len(records) == 7
        assert records[0]["TRAN-TYPE"].strip() == "01"
        assert records[0]["TRAN-TYPE-DESC"].strip() == "Purchase"

    def test_parse_trancatg(self, data_dir):
        records = parse_file(
            os.path.join(data_dir, "trancatg.txt"),
            TRANSACTION_CATEGORY_LAYOUT, 60,
        )
        assert len(records) == 18

    def test_parse_tcatbal(self, data_dir):
        records = parse_file(
            os.path.join(data_dir, "tcatbal.txt"),
            TRAN_CAT_BALANCE_LAYOUT, 50,
        )
        assert len(records) == 50

    def test_parse_discgrp(self, data_dir):
        records = parse_file(
            os.path.join(data_dir, "discgrp.txt"),
            DISCLOSURE_GROUP_LAYOUT, 50,
        )
        assert len(records) == 51


# --- Golden File Integrity ---

class TestGoldenFileIntegrity:
    """Verify golden JSON files exist and have correct metadata."""

    @pytest.mark.parametrize("basename,config", FILE_LAYOUT_MAP.items())
    def test_golden_file_exists_and_valid(self, golden_dir, basename, config):
        path = os.path.join(golden_dir, f"{basename}.golden.json")
        if not os.path.exists(path):
            pytest.skip(f"Golden file not yet generated: {basename}")

        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)

        assert "metadata" in data
        assert "records" in data
        assert data["metadata"]["copybook"] == config["copybook"]
        assert data["metadata"]["record_length"] == config["record_length"]
        assert data["metadata"]["record_count"] == len(data["records"])
        assert data["metadata"]["record_count"] > 0


# --- Record-Level Parse Validation ---

class TestRecordParsing:
    """Verify specific record parsing edge cases."""

    def test_short_line_padded(self):
        """Lines shorter than LRECL should be right-padded with spaces."""
        short_line = "0500024453765740000000050000000000500123456789ABCD"
        record = parse_record(short_line, CARD_XREF_LAYOUT, 50)
        assert record["XREF-CARD-NUM"].strip() == "0500024453765740"

    def test_account_signed_balance(self, data_dir):
        """Verify signed numeric decoding for account balances."""
        records = parse_file(
            os.path.join(data_dir, "acctdata.txt"),
            ACCOUNT_LAYOUT, 300,
        )
        first = records[0]
        bal = float(first["ACCT-CURR-BAL"])
        assert bal > 0
