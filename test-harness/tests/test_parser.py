"""Tests for the COBOL fixed-width file parser."""

from __future__ import annotations

import json
import tempfile
from pathlib import Path

import pytest

from harness.parser import (
    ACCOUNT_FIELDS,
    CARD_FIELDS,
    CARDXREF_FIELDS,
    CUSTOMER_FIELDS,
    DAILYTRAN_FIELDS,
    DISCGRP_FIELDS,
    LAYOUT_REGISTRY,
    TCATBAL_FIELDS,
    TRANCATG_FIELDS,
    TRANTYPE_FIELDS,
    decode_signed_numeric,
    parse_by_name,
    parse_field,
    parse_file,
    parse_record,
    pic_length,
)

DATA_DIR = Path(__file__).resolve().parent.parent.parent / "app" / "data" / "ASCII"


class TestPicLength:
    def test_alphanumeric_single(self):
        assert pic_length("X") == (1, False, False, 0)

    def test_alphanumeric_repeated(self):
        assert pic_length("X(16)") == (16, False, False, 0)

    def test_numeric_simple(self):
        assert pic_length("9(11)") == (11, True, False, 0)

    def test_numeric_signed_decimal(self):
        length, is_num, is_signed, dec = pic_length("S9(10)V99")
        assert length == 12
        assert is_num is True
        assert is_signed is True
        assert dec == 2

    def test_numeric_unsigned(self):
        assert pic_length("9(03)") == (3, True, False, 0)


class TestSignedNumericDecoding:
    def test_positive_zero(self):
        assert decode_signed_numeric("{") == 0.0

    def test_positive_values(self):
        # "01940{" → +19400
        assert decode_signed_numeric("01940{") == 19400.0

    def test_negative_values(self):
        # "01940}" → -19400
        assert decode_signed_numeric("01940}") == -19400.0

    def test_negative_digit(self):
        # "0000J" → -00001 → -1
        assert decode_signed_numeric("0000J") == -1.0

    def test_positive_digit_A(self):
        assert decode_signed_numeric("0000A") == 1.0

    def test_plain_digits(self):
        assert decode_signed_numeric("12345") == 12345.0


class TestFieldLayouts:
    """Verify that field layouts sum to the expected record length."""

    @pytest.mark.parametrize(
        "fields,expected_reclen",
        [
            (ACCOUNT_FIELDS, 300),
            (CARD_FIELDS, 150),
            (CARDXREF_FIELDS, 50),
            (CUSTOMER_FIELDS, 500),
            (DAILYTRAN_FIELDS, 350),
            (DISCGRP_FIELDS, 50),
            (TCATBAL_FIELDS, 50),
            (TRANCATG_FIELDS, 60),
            (TRANTYPE_FIELDS, 60),
        ],
    )
    def test_layout_total_length(self, fields, expected_reclen):
        total = sum(f.length for f in fields)
        assert total == expected_reclen, (
            f"Layout total {total} != expected {expected_reclen}"
        )


class TestParseRecord:
    def test_account_record(self):
        line = (
            "00000000001Y00000001940{00000020200{00000010200{"
            "2014-11-202025-05-202025-05-20"
            "00000000000{00000000000{A000000000"
        )
        rec = parse_record(line, ACCOUNT_FIELDS, 300)
        assert rec["ACCT-ID"] == 1
        assert rec["ACCT-ACTIVE-STATUS"] == "Y"
        assert rec["ACCT-CURR-BAL"] == 194.00
        assert rec["ACCT-CREDIT-LIMIT"] == 2020.00
        assert rec["ACCT-OPEN-DATE"] == "2014-11-20"
        # ACCT-ADDR-ZIP occupies offset 102-111; in this truncated test
        # input the GROUP-ID field at offset 112 is space-padded.
        assert rec["ACCT-ADDR-ZIP"] == "A000000000"

    def test_cardxref_record(self):
        line = "050002445376574000000005000000000050"
        rec = parse_record(line, CARDXREF_FIELDS, 50)
        assert rec["XREF-CARD-NUM"] == "0500024453765740"
        assert rec["XREF-CUST-ID"] == 50
        assert rec["XREF-ACCT-ID"] == 50

    def test_trantype_record(self):
        line = "01Purchase"
        rec = parse_record(line, TRANTYPE_FIELDS, 60)
        assert rec["TRAN-TYPE"] == "01"
        assert rec["TRAN-TYPE-DESC"].startswith("Purchase")


class TestParseFile:
    @pytest.mark.skipif(not DATA_DIR.exists(), reason="Data dir not available")
    def test_parse_acctdata(self):
        records = parse_file(DATA_DIR / "acctdata.txt", ACCOUNT_FIELDS, 300)
        assert len(records) == 50
        assert all("ACCT-ID" in r for r in records)

    @pytest.mark.skipif(not DATA_DIR.exists(), reason="Data dir not available")
    def test_parse_carddata(self):
        records = parse_file(DATA_DIR / "carddata.txt", CARD_FIELDS, 150)
        assert len(records) == 50

    @pytest.mark.skipif(not DATA_DIR.exists(), reason="Data dir not available")
    def test_parse_cardxref(self):
        records = parse_file(DATA_DIR / "cardxref.txt", CARDXREF_FIELDS, 50)
        assert len(records) == 50

    @pytest.mark.skipif(not DATA_DIR.exists(), reason="Data dir not available")
    def test_parse_custdata(self):
        records = parse_file(DATA_DIR / "custdata.txt", CUSTOMER_FIELDS, 500)
        assert len(records) == 50

    @pytest.mark.skipif(not DATA_DIR.exists(), reason="Data dir not available")
    def test_parse_dailytran(self):
        records = parse_file(DATA_DIR / "dailytran.txt", DAILYTRAN_FIELDS, 350)
        assert len(records) == 300

    @pytest.mark.skipif(not DATA_DIR.exists(), reason="Data dir not available")
    def test_parse_discgrp(self):
        records = parse_file(DATA_DIR / "discgrp.txt", DISCGRP_FIELDS, 50)
        assert len(records) == 51

    @pytest.mark.skipif(not DATA_DIR.exists(), reason="Data dir not available")
    def test_parse_tcatbal(self):
        records = parse_file(DATA_DIR / "tcatbal.txt", TCATBAL_FIELDS, 50)
        assert len(records) == 50

    @pytest.mark.skipif(not DATA_DIR.exists(), reason="Data dir not available")
    def test_parse_trancatg(self):
        records = parse_file(DATA_DIR / "trancatg.txt", TRANCATG_FIELDS, 60)
        assert len(records) == 18

    @pytest.mark.skipif(not DATA_DIR.exists(), reason="Data dir not available")
    def test_parse_trantype(self):
        records = parse_file(DATA_DIR / "trantype.txt", TRANTYPE_FIELDS, 60)
        assert len(records) == 7


class TestParseByName:
    @pytest.mark.skipif(not DATA_DIR.exists(), reason="Data dir not available")
    def test_auto_detect(self):
        records = parse_by_name(DATA_DIR / "acctdata.txt")
        assert len(records) == 50

    def test_unknown_file(self):
        with tempfile.NamedTemporaryFile(suffix=".txt", prefix="unknown") as f:
            with pytest.raises(ValueError, match="Unknown data file"):
                parse_by_name(f.name)
