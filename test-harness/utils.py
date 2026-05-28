"""Shared utilities for COBOL data parsing and comparison."""

import re
from decimal import Decimal, ROUND_HALF_UP


# Zoned-decimal sign overpunch mapping (EBCDIC-compatible ASCII representation)
# Positive: {ABCDEFGHI  maps to 0-9
# Negative: }JKLMNOPQR  maps to 0-9
POSITIVE_OVERPUNCH = {
    "{": "0", "A": "1", "B": "2", "C": "3", "D": "4",
    "E": "5", "F": "6", "G": "7", "H": "8", "I": "9",
}

NEGATIVE_OVERPUNCH = {
    "}": "0", "J": "1", "K": "2", "L": "3", "M": "4",
    "N": "5", "O": "6", "P": "7", "Q": "8", "R": "9",
}


def decode_signed_zoned_decimal(raw: str, decimal_places: int = 0) -> Decimal:
    """Decode a signed zoned-decimal value with overpunch character.

    In ASCII representation, the last character encodes both the digit and sign.
    Positive: {ABCDEFGHI -> +0 through +9
    Negative: }JKLMNOPQR -> -0 through -9
    Plain digits are treated as positive.
    """
    if not raw or raw.strip() == "":
        return Decimal("0")

    raw = raw.strip()
    last_char = raw[-1]
    digits_part = raw[:-1]

    if last_char in POSITIVE_OVERPUNCH:
        sign = 1
        last_digit = POSITIVE_OVERPUNCH[last_char]
    elif last_char in NEGATIVE_OVERPUNCH:
        sign = -1
        last_digit = NEGATIVE_OVERPUNCH[last_char]
    elif last_char.isdigit():
        sign = 1
        last_digit = last_char
    else:
        sign = 1
        last_digit = "0"

    full_digits = digits_part + last_digit

    if decimal_places > 0:
        integer_part = full_digits[:-decimal_places] or "0"
        decimal_part = full_digits[-decimal_places:]
        value = Decimal(f"{integer_part}.{decimal_part}")
    else:
        value = Decimal(full_digits)

    return value * sign


def parse_pic_clause(pic: str):
    """Parse a COBOL PIC clause and return (type, total_digits, decimal_places).

    Returns:
        tuple: (field_type, display_length, decimal_places)
            field_type: 'numeric', 'alphanumeric', or 'signed_decimal'
    """
    pic = pic.strip().upper()
    is_signed = pic.startswith("S")
    if is_signed:
        pic = pic[1:]

    # Check for implied decimal V
    if "V" in pic:
        parts = pic.split("V")
        integer_part = parts[0]
        decimal_part = parts[1] if len(parts) > 1 else ""

        int_len = _expand_pic_length(integer_part)
        dec_len = _expand_pic_length(decimal_part)
        total_len = int_len + dec_len
        return ("signed_decimal" if is_signed else "numeric", total_len, dec_len)

    length = _expand_pic_length(pic)

    if "X" in pic:
        return ("alphanumeric", length, 0)
    elif "9" in pic:
        if is_signed:
            return ("signed_decimal", length, 0)
        return ("numeric", length, 0)
    else:
        return ("alphanumeric", length, 0)


def _expand_pic_length(pic_fragment: str) -> int:
    """Expand PIC notation like 9(11) or X(50) to actual length."""
    total = 0
    i = 0
    while i < len(pic_fragment):
        ch = pic_fragment[i]
        if ch in ("9", "X", "A", "Z", "-", "+", "."):
            if i + 1 < len(pic_fragment) and pic_fragment[i + 1] == "(":
                close = pic_fragment.index(")", i + 2)
                count = int(pic_fragment[i + 2:close])
                total += count
                i = close + 1
            else:
                total += 1
                i += 1
        else:
            i += 1
    return total


def format_field_value(raw: str, field_type: str, decimal_places: int) -> object:
    """Convert a raw field string to appropriate Python type for JSON output."""
    if field_type == "alphanumeric":
        return raw.rstrip()
    elif field_type == "signed_decimal":
        return str(decode_signed_zoned_decimal(raw, decimal_places))
    elif field_type == "numeric":
        stripped = raw.strip()
        if not stripped:
            return "0"
        return stripped.lstrip("0") or "0"
    return raw.rstrip()
