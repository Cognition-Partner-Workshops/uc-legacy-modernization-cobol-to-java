"""Unit tests for the COBOL data file parser."""

from __future__ import annotations

import tempfile

import pytest

from cobol_test_harness.parser import (
    FieldDef,
    decode_zoned_decimal,
    get_account_layout,
    get_card_layout,
    get_card_xref_layout,
    get_customer_layout,
    get_daily_transaction_layout,
    get_disclosure_group_layout,
    get_layout_for_file,
    get_tran_cat_bal_layout,
    get_transaction_category_layout,
    get_transaction_type_layout,
    parse_data_file,
    parse_field_value,
    parse_record,
)

# ---------------------------------------------------------------------------
# Zoned decimal decoding
# ---------------------------------------------------------------------------


class TestDecodeZonedDecimal:
    """Tests for the zoned decimal decoder."""

    def test_positive_zero(self) -> None:
        assert decode_zoned_decimal("{", 0) == "0"

    def test_positive_zero_with_decimals(self) -> None:
        assert decode_zoned_decimal("0000000000{", 2) == "0.00"

    def test_positive_nonzero(self) -> None:
        # '00000001940{' -> last char '{' means +0, so digits = 000000019400
        # With 2 decimal places: 000000194.00
        result = decode_zoned_decimal("00000001940{", 2)
        assert result == "194.00"

    def test_positive_with_letter(self) -> None:
        # 'G' = +7, so '0000005047G' -> digits = 00000050477
        # With 2 decimal places: 000000504.77
        result = decode_zoned_decimal("0000005047G", 2)
        assert result == "504.77"

    def test_negative_zero(self) -> None:
        assert decode_zoned_decimal("}", 0) == "0"

    def test_negative_nonzero(self) -> None:
        # '}' = -0, 'J'=-1, 'K'=-2, etc.
        # '0000009190}' with 2 decimal places
        # digits = 00000091900, decimal: 000000919.00, sign negative
        result = decode_zoned_decimal("0000009190}", 2)
        assert result == "-919.00"

    def test_negative_with_letter(self) -> None:
        # 'P' = -7
        # '0000000567P' with 2 decimal places
        # digits = 00000005677, decimal: 000000056.77, sign negative
        result = decode_zoned_decimal("0000000567P", 2)
        assert result == "-56.77"

    def test_plain_digits(self) -> None:
        # If last char is a regular digit, treat as unsigned
        result = decode_zoned_decimal("00000012345", 2)
        assert result == "123.45"

    def test_empty_string(self) -> None:
        assert decode_zoned_decimal("", 0) == "0"

    def test_whitespace_only(self) -> None:
        assert decode_zoned_decimal("   ", 0) == "0"

    def test_large_positive(self) -> None:
        # 'I' = +9
        result = decode_zoned_decimal("999999999I", 2)
        assert result == "99999999.99"

    def test_no_decimal_positive(self) -> None:
        # '{' = +0, so '00150{' -> digits = 001500
        # With 2 decimal places: 0015.00
        result = decode_zoned_decimal("00150{", 2)
        assert result == "15.00"


# ---------------------------------------------------------------------------
# Field value parsing
# ---------------------------------------------------------------------------


class TestParseFieldValue:
    """Tests for field value parsing."""

    def test_alphanumeric_field(self) -> None:
        field = FieldDef("NAME", "X(25)", 0, 25)
        assert parse_field_value("John Smith               ", field) == "John Smith"

    def test_alphanumeric_trailing_spaces(self) -> None:
        field = FieldDef("STATUS", "X(01)", 0, 1)
        assert parse_field_value("Y", field) == "Y"

    def test_unsigned_numeric(self) -> None:
        field = FieldDef("ACCT-ID", "9(11)", 0, 11)
        assert parse_field_value("00000000001", field) == "1"

    def test_unsigned_numeric_large(self) -> None:
        field = FieldDef("ACCT-ID", "9(11)", 0, 11)
        assert parse_field_value("99999999999", field) == "99999999999"

    def test_signed_decimal(self) -> None:
        field = FieldDef(
            "BAL", "S9(10)V99", 0, 12,
            decimal_places=2, is_signed=True,
        )
        # '00000001940{' -> '{' = +0 -> digits 000000019400 -> V99 -> 194.00
        result = parse_field_value("00000001940{", field)
        assert result == "194.00"

    def test_filler_field(self) -> None:
        field = FieldDef("FILLER", "X(10)", 0, 10, is_filler=True)
        result = parse_field_value("          ", field)
        assert result == "          "

    def test_date_field(self) -> None:
        field = FieldDef("DATE", "X(10)", 0, 10)
        assert parse_field_value("2014-11-20", field) == "2014-11-20"


# ---------------------------------------------------------------------------
# Record parsing
# ---------------------------------------------------------------------------


class TestParseRecord:
    """Tests for parsing a full record line."""

    def test_parse_cardxref_record(self) -> None:
        layout = get_card_xref_layout()
        # Layout: XREF-CARD-NUM X(16), XREF-CUST-ID 9(09), XREF-ACCT-ID 9(11), FILLER X(14)
        # Total = 50 chars
        card_num = "0500024453765740"   # 16 chars
        cust_id  = "000000050"          # 9 chars -> "50"
        acct_id  = "00000000050"        # 11 chars -> "50"
        filler   = " " * 14             # 14 chars
        line = card_num + cust_id + acct_id + filler
        assert len(line) == 50
        record = parse_record(line, layout)
        assert record["XREF-CARD-NUM"] == "0500024453765740"
        assert record["XREF-CUST-ID"] == "50"
        assert record["XREF-ACCT-ID"] == "50"

    def test_parse_trantype_record(self) -> None:
        layout = get_transaction_type_layout()
        line = "01Purchase                                          00000000"
        record = parse_record(line, layout)
        assert record["TRAN-TYPE"] == "01"
        assert record["TRAN-TYPE-DESC"] == "Purchase"

    def test_parse_trancatg_record(self) -> None:
        layout = get_transaction_category_layout()
        line = "010001Regular Sales Draft                               0000"
        record = parse_record(line, layout)
        assert record["TRAN-TYPE-CD"] == "01"
        assert record["TRAN-CAT-CD"] == "1"
        assert record["TRAN-CAT-TYPE-DESC"] == "Regular Sales Draft"

    def test_filler_excluded(self) -> None:
        layout = get_card_xref_layout()
        line = "050002445376574000000005000000000050" + " " * 14
        record = parse_record(line, layout)
        assert "FILLER" not in record


# ---------------------------------------------------------------------------
# Layout registry
# ---------------------------------------------------------------------------


class TestLayoutRegistry:
    """Tests for the layout registry."""

    def test_all_registered_files(self) -> None:
        expected_files = [
            "acctdata.txt", "carddata.txt", "cardxref.txt", "custdata.txt",
            "dailytran.txt", "tcatbal.txt", "trancatg.txt", "trantype.txt",
            "discgrp.txt",
        ]
        for name in expected_files:
            layout = get_layout_for_file(name)
            assert layout is not None
            assert layout.record_length > 0
            assert len(layout.fields) > 0

    def test_unknown_file_raises(self) -> None:
        with pytest.raises(ValueError, match="No layout registered"):
            get_layout_for_file("unknown.txt")


# ---------------------------------------------------------------------------
# Layout field lengths
# ---------------------------------------------------------------------------


class TestLayoutFieldLengths:
    """Verify that field lengths sum to the declared record length."""

    def test_account_layout_length(self) -> None:
        layout = get_account_layout()
        assert layout.total_field_length() == layout.record_length

    def test_card_layout_length(self) -> None:
        layout = get_card_layout()
        assert layout.total_field_length() == layout.record_length

    def test_card_xref_layout_length(self) -> None:
        layout = get_card_xref_layout()
        assert layout.total_field_length() == layout.record_length

    def test_customer_layout_length(self) -> None:
        layout = get_customer_layout()
        assert layout.total_field_length() == layout.record_length

    def test_daily_transaction_layout_length(self) -> None:
        layout = get_daily_transaction_layout()
        assert layout.total_field_length() == layout.record_length

    def test_tran_cat_bal_layout_length(self) -> None:
        layout = get_tran_cat_bal_layout()
        assert layout.total_field_length() == layout.record_length

    def test_disclosure_group_layout_length(self) -> None:
        layout = get_disclosure_group_layout()
        assert layout.total_field_length() == layout.record_length

    def test_transaction_type_layout_length(self) -> None:
        layout = get_transaction_type_layout()
        assert layout.total_field_length() == layout.record_length

    def test_transaction_category_layout_length(self) -> None:
        layout = get_transaction_category_layout()
        assert layout.total_field_length() == layout.record_length


# ---------------------------------------------------------------------------
# Full file parsing
# ---------------------------------------------------------------------------


class TestParseDataFile:
    """Tests for full data file parsing."""

    def test_parse_small_file(self) -> None:
        layout = get_transaction_type_layout()
        content = (
            "01Purchase                                          00000000\n"
            "02Payment                                           00000000\n"
        )
        with tempfile.NamedTemporaryFile(mode="w", suffix=".txt", delete=False) as f:
            f.write(content)
            f.flush()
            result = parse_data_file(f.name, layout, include_metadata=True)

        assert result["_metadata"]["record_count"] == 2
        assert len(result["records"]) == 2
        assert result["records"][0]["TRAN-TYPE"] == "01"
        assert result["records"][0]["TRAN-TYPE-DESC"] == "Purchase"
        assert result["records"][1]["TRAN-TYPE"] == "02"
        assert result["records"][1]["TRAN-TYPE-DESC"] == "Payment"

    def test_metadata_includes_fields(self) -> None:
        layout = get_transaction_type_layout()
        content = "01Purchase                                          00000000\n"
        with tempfile.NamedTemporaryFile(mode="w", suffix=".txt", delete=False) as f:
            f.write(content)
            f.flush()
            result = parse_data_file(f.name, layout, include_metadata=True)

        fields = result["_metadata"]["fields"]
        field_names = [fd["name"] for fd in fields]
        assert "TRAN-TYPE" in field_names
        assert "TRAN-TYPE-DESC" in field_names

    def test_empty_file(self) -> None:
        layout = get_transaction_type_layout()
        with tempfile.NamedTemporaryFile(mode="w", suffix=".txt", delete=False) as f:
            f.write("")
            f.flush()
            result = parse_data_file(f.name, layout, include_metadata=True)

        assert result["_metadata"]["record_count"] == 0
        assert len(result["records"]) == 0
