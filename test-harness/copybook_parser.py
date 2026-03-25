"""
copybook_parser.py - Parse COBOL fixed-width ASCII data using copybook field layouts.

Each layout is defined as a list of (field_name, pic_clause, length) tuples.
The parser slices each fixed-width record according to the layout and returns
typed Python values:
  - PIC X(n)       -> str (trailing spaces stripped)
  - PIC 9(n)       -> int
  - PIC S9(n)V99   -> Decimal (signed, two implied decimal places)
  - FILLER         -> str (kept for completeness, key prefixed with _filler_N)
"""

from __future__ import annotations

import re
from decimal import Decimal
from pathlib import Path
from typing import Any

# ---------------------------------------------------------------------------
# COBOL zoned-decimal sign encoding (last byte overpunch)
# The trailing character encodes both the last digit and the sign.
# '{' = +0, 'A'-'I' = +1..+9, '}' = -0, 'J'-'R' = -1..-9
# ---------------------------------------------------------------------------
_POSITIVE_OVERPUNCH = {
    "{": 0, "A": 1, "B": 2, "C": 3, "D": 4,
    "E": 5, "F": 6, "G": 7, "H": 8, "I": 9,
}
_NEGATIVE_OVERPUNCH = {
    "}": 0, "J": 1, "K": 2, "L": 3, "M": 4,
    "N": 5, "O": 6, "P": 7, "Q": 8, "R": 9,
}


def decode_signed_zoned(raw: str, decimal_places: int = 0) -> Decimal:
    """Decode a COBOL zoned-decimal value with trailing overpunch sign.

    Args:
        raw: The raw fixed-width string (e.g. '00000001940{').
        decimal_places: Number of implied decimal places (V99 = 2).

    Returns:
        A Decimal with the correct sign and scale.
    """
    if not raw:
        return Decimal(0)

    raw = raw.strip()
    if not raw:
        return Decimal(0)

    last_char = raw[-1]
    digits_part = raw[:-1]

    if last_char in _POSITIVE_OVERPUNCH:
        last_digit = _POSITIVE_OVERPUNCH[last_char]
        sign = 1
    elif last_char in _NEGATIVE_OVERPUNCH:
        last_digit = _NEGATIVE_OVERPUNCH[last_char]
        sign = -1
    elif last_char.isdigit():
        # No overpunch, treat as unsigned positive
        digits_part = raw
        last_digit = None
        sign = 1
    else:
        # Fallback: try to parse as-is
        digits_part = raw
        last_digit = None
        sign = 1

    if last_digit is not None:
        full_digits = digits_part + str(last_digit)
    else:
        full_digits = digits_part

    # Remove leading zeros but keep at least one digit
    full_digits = full_digits.lstrip("0") or "0"

    if decimal_places > 0:
        # Insert the decimal point
        full_digits = full_digits.zfill(decimal_places + 1)
        integer_part = full_digits[:-decimal_places]
        frac_part = full_digits[-decimal_places:]
        value = Decimal(f"{sign * int(integer_part)}.{frac_part}")
    else:
        value = Decimal(sign * int(full_digits))

    return value


# ---------------------------------------------------------------------------
# PIC clause type detection
# ---------------------------------------------------------------------------
_RE_PIC_X = re.compile(r"X\((\d+)\)")
_RE_PIC_9 = re.compile(r"^9\((\d+)\)$")
_RE_PIC_S9V = re.compile(r"S9\((\d+)\)V(9+)")


def pic_type(pic: str) -> str:
    """Return a category string for a PIC clause: 'alpha', 'unsigned', 'signed_decimal'."""
    if _RE_PIC_X.search(pic):
        return "alpha"
    if _RE_PIC_S9V.search(pic):
        return "signed_decimal"
    if _RE_PIC_9.search(pic):
        return "unsigned"
    return "alpha"  # fallback


def pic_length(pic: str) -> int:
    """Compute the storage length in bytes for a PIC clause."""
    m = _RE_PIC_X.search(pic)
    if m:
        return int(m.group(1))
    m = _RE_PIC_S9V.search(pic)
    if m:
        return int(m.group(1)) + len(m.group(2))
    m = _RE_PIC_9.search(pic)
    if m:
        return int(m.group(1))
    return 0


# ---------------------------------------------------------------------------
# Field layout definition
# ---------------------------------------------------------------------------
class FieldDef:
    """Definition of a single copybook field."""

    __slots__ = ("name", "pic", "length", "offset")

    def __init__(self, name: str, pic: str, length: int, offset: int = 0):
        self.name = name
        self.pic = pic
        self.length = length
        self.offset = offset

    def __repr__(self) -> str:
        return f"FieldDef({self.name!r}, {self.pic!r}, {self.length}, offset={self.offset})"


def build_layout(fields: list[tuple[str, str, int]]) -> list[FieldDef]:
    """Convert a list of (name, pic, length) tuples into FieldDef objects with offsets."""
    layout: list[FieldDef] = []
    offset = 0
    for name, pic, length in fields:
        layout.append(FieldDef(name, pic, length, offset))
        offset += length
    return layout


# ---------------------------------------------------------------------------
# Record parser
# ---------------------------------------------------------------------------
def parse_record(line: str, layout: list[FieldDef]) -> dict[str, Any]:
    """Parse a single fixed-width line into a dict using the given layout.

    Args:
        line: A fixed-width data line (may be shorter than expected; pads with spaces).
        layout: A list of FieldDef objects describing each field.

    Returns:
        A dict mapping field names to typed values.
    """
    # Pad line to at least cover the full record length
    total_len = layout[-1].offset + layout[-1].length if layout else 0
    padded = line.ljust(total_len)

    record: dict[str, Any] = {}
    filler_idx = 0

    for field in layout:
        raw = padded[field.offset : field.offset + field.length]
        ptype = pic_type(field.pic)

        if field.name.upper().startswith("FILLER"):
            filler_idx += 1
            key = f"_filler_{filler_idx}"
            record[key] = raw
            continue

        if ptype == "alpha":
            record[field.name] = raw.rstrip()
        elif ptype == "unsigned":
            stripped = raw.strip()
            record[field.name] = int(stripped) if stripped else 0
        elif ptype == "signed_decimal":
            m = _RE_PIC_S9V.search(field.pic)
            dec_places = len(m.group(2)) if m else 2
            record[field.name] = str(decode_signed_zoned(raw, dec_places))
        else:
            record[field.name] = raw.rstrip()

    return record


def parse_file(
    filepath: str | Path,
    layout: list[FieldDef],
    *,
    skip_blank_lines: bool = True,
) -> list[dict[str, Any]]:
    """Parse an entire fixed-width data file.

    Args:
        filepath: Path to the ASCII data file.
        layout: Field layout for the record type.
        skip_blank_lines: Whether to skip empty lines.

    Returns:
        A list of parsed record dicts.
    """
    records: list[dict[str, Any]] = []
    path = Path(filepath)
    with path.open("r", encoding="ascii", errors="replace") as f:
        for line in f:
            stripped = line.rstrip("\n").rstrip("\r")
            if skip_blank_lines and not stripped.strip():
                continue
            records.append(parse_record(stripped, layout))
    return records
