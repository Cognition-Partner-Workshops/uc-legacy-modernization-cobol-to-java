"""Tests for data loader — validates COBOL sign decoding and file parsing."""

from decimal import Decimal

from app.batch.data_loader import _decode_signed


def test_decode_positive_zero():
    assert _decode_signed("0000{", 2) == Decimal("0.00")


def test_decode_positive_nonzero():
    # 00000001940{ → digits "00000019400" → 194.00 (with 2 decimal places)
    assert _decode_signed("00000001940{", 2) == Decimal("194.00")


def test_decode_negative():
    # 00000001940} → digits "-00000019400" → -194.00
    assert _decode_signed("00000001940}", 2) == Decimal("-194.00")


def test_decode_positive_A():
    # 0000194A → +00001941 → 19.41
    assert _decode_signed("0000194A", 2) == Decimal("19.41")


def test_decode_negative_J():
    # 0000194J → -00001941 → -19.41
    assert _decode_signed("0000194J", 2) == Decimal("-19.41")


def test_decode_no_decimal():
    assert _decode_signed("00012{", 0) == Decimal("120")


def test_decode_empty():
    assert _decode_signed("", 0) == Decimal("0")


def test_decode_spaces():
    assert _decode_signed("     ", 0) == Decimal("0")


def test_decode_plain_digit():
    assert _decode_signed("01234", 2) == Decimal("12.34")
