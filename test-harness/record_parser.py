"""
Split fixed-width ASCII record lines into named fields using copybook
field definitions produced by copybook_parser.

Handles:
- Alphanumeric fields  (PIC X) — returned as stripped strings
- Unsigned numeric      (PIC 9) — returned as int or float (when scale > 0)
- Signed numeric        (PIC S9) — trailing overpunch decoded per EBCDIC
  zoned-decimal convention used in the ASCII data files
"""

from __future__ import annotations

from typing import Any

# ---------------------------------------------------------------------------
# EBCDIC zoned-decimal trailing-overpunch tables
# ---------------------------------------------------------------------------

_POSITIVE_OVERPUNCH: dict[str, int] = {
    "{": 0, "A": 1, "B": 2, "C": 3, "D": 4,
    "E": 5, "F": 6, "G": 7, "H": 8, "I": 9,
}

_NEGATIVE_OVERPUNCH: dict[str, int] = {
    "}": 0, "J": 1, "K": 2, "L": 3, "M": 4,
    "N": 5, "O": 6, "P": 7, "Q": 8, "R": 9,
}

_ALL_OVERPUNCH: dict[str, tuple[int, int]] = {}
for ch, digit in _POSITIVE_OVERPUNCH.items():
    _ALL_OVERPUNCH[ch] = (digit, 1)
for ch, digit in _NEGATIVE_OVERPUNCH.items():
    _ALL_OVERPUNCH[ch] = (digit, -1)


def decode_signed_numeric(raw: str, scale: int) -> float | int:
    """Decode a zoned-decimal signed numeric string.

    The last character may be an overpunch letter encoding both the low-order
    digit and the sign.  If the last character is a plain digit the value is
    treated as positive.
    """
    raw = raw.strip()
    if not raw:
        return 0

    last_char = raw[-1]
    if last_char in _ALL_OVERPUNCH:
        digit, sign = _ALL_OVERPUNCH[last_char]
        numeric_str = raw[:-1] + str(digit)
        value = int(numeric_str) * sign
    else:
        value = int(raw)

    if scale > 0:
        return value / (10 ** scale)
    return value


def decode_unsigned_numeric(raw: str, scale: int) -> float | int:
    """Decode an unsigned numeric display field."""
    raw = raw.strip()
    if not raw:
        return 0
    value = int(raw)
    if scale > 0:
        return value / (10 ** scale)
    return value


# ---------------------------------------------------------------------------
# Record parser
# ---------------------------------------------------------------------------

def parse_record(line: str, fields: list[dict[str, Any]]) -> dict[str, Any]:
    """Parse a single fixed-width line into a dict keyed by field name.

    ``fields`` is the list returned by ``copybook_parser.parse_copybook``.
    """
    result: dict[str, Any] = {}

    for f in fields:
        name = f["name"]
        offset = f["offset"]
        length = f["length"]
        ftype = f["type"]
        scale = f["scale"]

        raw = line[offset : offset + length]

        if ftype == "alphanumeric":
            result[name] = raw.rstrip()
        elif ftype == "numeric_signed":
            result[name] = decode_signed_numeric(raw, scale)
        elif ftype == "numeric_display":
            result[name] = decode_unsigned_numeric(raw, scale)
        else:
            result[name] = raw.rstrip()

    return result


def parse_file(
    data_path: str,
    fields: list[dict[str, Any]],
    *,
    expected_length: int | None = None,
) -> list[dict[str, Any]]:
    """Parse every line of a fixed-width data file.

    If *expected_length* is given, lines shorter than expected are right-padded
    with spaces (trailing FILLER may be stripped in ASCII exports).  Lines
    longer than expected are truncated.  Blank lines are skipped.
    """
    import sys
    from pathlib import Path

    records: list[dict[str, Any]] = []
    for lineno, line in enumerate(Path(data_path).read_text().splitlines(), 1):
        if not line.strip():
            continue
        if expected_length is not None:
            if len(line) < expected_length:
                line = line.ljust(expected_length)
            elif len(line) > expected_length:
                line = line[:expected_length]
        records.append(parse_record(line, fields))

    return records
