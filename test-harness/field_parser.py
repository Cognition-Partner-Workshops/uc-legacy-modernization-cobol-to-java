"""
field_parser.py -- Low-level utilities for parsing COBOL fixed-width fields
from ASCII data files.

Handles:
  - PIC X(n)       : Alphanumeric (right-trimmed)
  - PIC 9(n)       : Unsigned zoned decimal -> int
  - PIC S9(m)V99   : Signed zoned decimal with implied decimal -> Decimal
  - FILLER         : Skipped (advance offset only)
"""

from decimal import Decimal
from typing import Any

# Trailing-sign overpunch mapping for signed zoned decimals.
# The last byte of a signed zoned field encodes both the digit and the sign.
_POSITIVE_OVERPUNCH = {
    "{": 0, "A": 1, "B": 2, "C": 3, "D": 4,
    "E": 5, "F": 6, "G": 7, "H": 8, "I": 9,
}
_NEGATIVE_OVERPUNCH = {
    "}": 0, "J": 1, "K": 2, "L": 3, "M": 4,
    "N": 5, "O": 6, "P": 7, "Q": 8, "R": 9,
}


def parse_alphanumeric(raw: str) -> str:
    """Parse PIC X(n) field: return right-trimmed string."""
    return raw.rstrip()


def parse_unsigned_numeric(raw: str) -> int:
    """Parse PIC 9(n) field: unsigned zoned decimal -> int."""
    stripped = raw.strip()
    if not stripped:
        return 0
    return int(stripped)


def parse_signed_decimal(raw: str, decimal_places: int) -> Decimal:
    """Parse PIC S9(m)V99 field with trailing overpunch sign.

    Args:
        raw: The raw fixed-width string (all digits + possible overpunch).
        decimal_places: Number of implied decimal places (e.g., 2 for V99).

    Returns:
        Decimal value with correct sign and scale.
    """
    if not raw or raw.isspace():
        return Decimal("0")

    last_char = raw[-1]
    digits_part = raw[:-1]
    sign = 1
    last_digit = 0

    if last_char in _POSITIVE_OVERPUNCH:
        last_digit = _POSITIVE_OVERPUNCH[last_char]
        sign = 1
    elif last_char in _NEGATIVE_OVERPUNCH:
        last_digit = _NEGATIVE_OVERPUNCH[last_char]
        sign = -1
    elif last_char.isdigit():
        last_digit = int(last_char)
        sign = 1
    else:
        last_digit = 0
        sign = 1

    full_digits = digits_part + str(last_digit)

    # Remove leading zeros but keep at least one digit
    full_digits = full_digits.lstrip("0") or "0"

    if decimal_places > 0:
        # Pad with leading zeros if needed
        full_digits = full_digits.zfill(decimal_places + 1)
        integer_part = full_digits[:-decimal_places]
        fractional_part = full_digits[-decimal_places:]
        numeric_str = f"{integer_part}.{fractional_part}"
    else:
        numeric_str = full_digits

    result = Decimal(numeric_str)
    if sign == -1:
        result = -result

    return result


def extract_field(record: str, offset: int, length: int, field_type: str,
                  decimal_places: int = 0) -> tuple[Any, int]:
    """Extract a single field from a fixed-width record.

    Args:
        record: The full record string.
        offset: Current byte offset into the record.
        length: Field length in bytes.
        field_type: One of 'alpha', 'unsigned', 'signed_decimal', 'filler'.
        decimal_places: For signed_decimal fields, number of implied decimals.

    Returns:
        Tuple of (parsed_value, new_offset).
    """
    raw = record[offset:offset + length]
    new_offset = offset + length

    if field_type == "filler":
        return None, new_offset
    elif field_type == "alpha":
        return parse_alphanumeric(raw), new_offset
    elif field_type == "unsigned":
        return parse_unsigned_numeric(raw), new_offset
    elif field_type == "signed_decimal":
        return str(parse_signed_decimal(raw, decimal_places)), new_offset
    else:
        raise ValueError(f"Unknown field type: {field_type}")
