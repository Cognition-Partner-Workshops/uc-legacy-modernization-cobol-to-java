"""
Parse fixed-width ASCII data files using COBOL copybook layout definitions.

Handles zoned decimal sign overpunch encoding for signed numeric fields and
implied decimal positioning for PIC S9(n)V99 fields.
"""

from __future__ import annotations

from typing import Union

from layouts.definitions import FieldDef

# Zoned decimal overpunch mappings (ASCII representation)
_POSITIVE_OVERPUNCH = {
    "{": 0, "A": 1, "B": 2, "C": 3, "D": 4,
    "E": 5, "F": 6, "G": 7, "H": 8, "I": 9,
}
_NEGATIVE_OVERPUNCH = {
    "}": 0, "J": 1, "K": 2, "L": 3, "M": 4,
    "N": 5, "O": 6, "P": 7, "Q": 8, "R": 9,
}


def decode_signed_numeric(raw: str, scale: int = 0) -> str:
    """Decode a zoned decimal signed numeric field.

    Args:
        raw: The raw string from the data file.
        scale: Number of implied decimal places (e.g., 2 for V99).

    Returns:
        String representation of the decoded numeric value.
    """
    if not raw or raw.isspace():
        return "0" if scale == 0 else f"0.{'0' * scale}"

    last_char = raw[-1]
    digits = raw[:-1]
    sign = "+"

    if last_char in _POSITIVE_OVERPUNCH:
        last_digit = str(_POSITIVE_OVERPUNCH[last_char])
        sign = "+"
    elif last_char in _NEGATIVE_OVERPUNCH:
        last_digit = str(_NEGATIVE_OVERPUNCH[last_char])
        sign = "-"
    elif last_char.isdigit():
        last_digit = last_char
        sign = "+"
    else:
        last_digit = "0"
        sign = "+"

    full_digits = digits + last_digit

    if scale > 0:
        integer_part = full_digits[:-scale] or "0"
        decimal_part = full_digits[-scale:]
        int_val = int(integer_part)
        result = f"{int_val}.{decimal_part}"
    else:
        result = str(int(full_digits))

    if sign == "-" and result not in ("0", "0.00", "0.0"):
        result = "-" + result

    return result


def parse_field(raw: str, field_def: FieldDef) -> Union[str, int, float]:
    """Parse a single field value based on its PIC type.

    Args:
        raw: The raw fixed-width string for this field.
        field_def: The field definition from the layout.

    Returns:
        The parsed value: str for alphanumeric, int for unsigned numeric,
        str (decimal representation) for signed numeric with scale.
    """
    pic_type = field_def.pic_type

    if pic_type == "X":
        return raw

    if pic_type == "9":
        stripped = raw.strip()
        if not stripped:
            return 0
        return int(stripped)

    if pic_type == "S9":
        return decode_signed_numeric(raw, scale=0)

    if pic_type == "S9V99":
        return decode_signed_numeric(raw, scale=2)

    return raw


def parse_record(line: str, layout: list[FieldDef], record_length: int) -> dict:
    """Parse a single fixed-width record using the given layout.

    The line is right-padded with spaces to the expected record length if
    it is shorter (trailing whitespace is often stripped from text files).

    Args:
        line: A single line from the data file (without newline).
        layout: List of FieldDef tuples defining the record structure.
        record_length: Expected total record length in bytes.

    Returns:
        Dictionary mapping field names to parsed values.
        FILLER fields are excluded from the output.
    """
    padded = line.ljust(record_length)
    record = {}
    offset = 0

    for field_def in layout:
        raw = padded[offset:offset + field_def.length]
        offset += field_def.length

        if field_def.name == "FILLER":
            continue

        record[field_def.name] = parse_field(raw, field_def)

    return record


def parse_file(
    file_path: str,
    layout: list[FieldDef],
    record_length: int,
) -> list[dict]:
    """Parse an entire fixed-width data file.

    Args:
        file_path: Path to the ASCII data file.
        layout: List of FieldDef tuples defining the record structure.
        record_length: Expected total record length in bytes.

    Returns:
        List of parsed record dictionaries.
    """
    records = []
    with open(file_path, "r", encoding="ascii", errors="replace") as f:
        for line_num, line in enumerate(f, start=1):
            stripped = line.rstrip("\n").rstrip("\r")
            if not stripped:
                continue
            record = parse_record(stripped, layout, record_length)
            record["_line_number"] = line_num
            records.append(record)
    return records
