"""
Parse COBOL copybook (.cpy) files into structured field definitions.

Extracts field name, PIC clause, offset, length, type, and decimal places
from COBOL 01-level record definitions.
"""

import re
from dataclasses import dataclass


@dataclass
class CopybookField:
    """A single field extracted from a COBOL copybook."""

    name: str
    pic: str
    offset: int
    length: int
    field_type: str  # "alpha", "numeric", "signed_decimal"
    decimal_places: int
    level: int


# Map of EBCDIC zoned-decimal sign overpunch characters to their sign and digit.
# In ASCII representations of signed packed fields, the last byte of a positive
# number uses {A-I for digits 0-9, and negative uses }J-R for digits 0-9.
POSITIVE_OVERPUNCH = {
    "{": "0",
    "A": "1",
    "B": "2",
    "C": "3",
    "D": "4",
    "E": "5",
    "F": "6",
    "G": "7",
    "H": "8",
    "I": "9",
}

NEGATIVE_OVERPUNCH = {
    "}": "0",
    "J": "1",
    "K": "2",
    "L": "3",
    "M": "4",
    "N": "5",
    "O": "6",
    "P": "7",
    "Q": "8",
    "R": "9",
}


def decode_signed_value(raw: str, decimal_places: int) -> str:
    """Decode a zoned-decimal signed value with overpunch character.

    Args:
        raw: The raw string from the ASCII data file.
        decimal_places: Number of implied decimal places from PIC clause.

    Returns:
        String representation of the decoded numeric value (e.g., "1940.00").
    """
    if not raw:
        return "0"

    raw = raw.strip()
    if not raw:
        return "0"

    last_char = raw[-1]
    digits_part = raw[:-1]

    if last_char in POSITIVE_OVERPUNCH:
        sign = ""
        last_digit = POSITIVE_OVERPUNCH[last_char]
    elif last_char in NEGATIVE_OVERPUNCH:
        sign = "-"
        last_digit = NEGATIVE_OVERPUNCH[last_char]
    elif last_char.isdigit():
        # No overpunch; treat as unsigned
        sign = ""
        last_digit = last_char
    else:
        return raw  # Cannot decode, return as-is

    full_digits = digits_part + last_digit

    if decimal_places > 0:
        # Insert decimal point
        if len(full_digits) <= decimal_places:
            full_digits = full_digits.zfill(decimal_places + 1)
        integer_part = full_digits[:-decimal_places]
        decimal_part = full_digits[-decimal_places:]
        return f"{sign}{integer_part}.{decimal_part}"

    return f"{sign}{full_digits}"


def pic_length(pic: str) -> tuple:
    """Calculate the display length, type, and decimal places from a PIC clause.

    Args:
        pic: The PIC clause string (e.g., "9(11)", "X(16)", "S9(10)V99").

    Returns:
        Tuple of (length, field_type, decimal_places).
    """
    pic = pic.upper().strip()
    original_pic = pic

    is_signed = pic.startswith("S")
    if is_signed:
        pic = pic[1:]

    decimal_places = 0
    total_length = 0
    field_type = "alpha"

    if "V" in pic:
        # Implied decimal: e.g., 9(10)V99
        parts = pic.split("V")
        integer_part = parts[0]
        decimal_part = parts[1] if len(parts) > 1 else ""

        int_len = _count_digits(integer_part)
        dec_len = _count_digits(decimal_part)

        total_length = int_len + dec_len
        decimal_places = dec_len
        field_type = "signed_decimal" if is_signed else "numeric"
    elif "9" in pic:
        total_length = _count_digits(pic)
        field_type = "signed_decimal" if is_signed else "numeric"
    elif "X" in pic:
        total_length = _count_chars(pic)
        field_type = "alpha"
    else:
        # Fallback: try to parse as repeat notation
        total_length = _count_chars(pic)
        field_type = "alpha"

    return total_length, field_type, decimal_places


def _count_digits(pic_part: str) -> int:
    """Count the number of display positions in a PIC part like 9(10) or 99."""
    total = 0
    i = 0
    while i < len(pic_part):
        if pic_part[i] == "9":
            if i + 1 < len(pic_part) and pic_part[i + 1] == "(":
                # Find the closing paren
                close = pic_part.index(")", i + 2)
                count = int(pic_part[i + 2 : close])
                total += count
                i = close + 1
            else:
                total += 1
                i += 1
        else:
            i += 1
    return total


def _count_chars(pic_part: str) -> int:
    """Count the number of character positions in a PIC part like X(16) or XX."""
    total = 0
    i = 0
    while i < len(pic_part):
        if pic_part[i] in ("X", "A", "9"):
            if i + 1 < len(pic_part) and pic_part[i + 1] == "(":
                close = pic_part.index(")", i + 2)
                count = int(pic_part[i + 2 : close])
                total += count
                i = close + 1
            else:
                total += 1
                i += 1
        else:
            i += 1
    return total


def parse_copybook(filepath: str) -> list:
    """Parse a COBOL copybook file and extract field definitions.

    Args:
        filepath: Path to the .cpy file.

    Returns:
        List of CopybookField objects with computed offsets.
    """
    fields = []
    current_offset = 0

    with open(filepath, "r") as f:
        lines = f.readlines()

    # Join continuation lines and strip comments
    cleaned_lines = []
    buffer = ""
    for line in lines:
        # Skip pure comment lines (column 7 = *)
        stripped = line.rstrip()
        if len(stripped) >= 7 and stripped[6] == "*":
            continue
        if not stripped.strip():
            continue

        # Remove sequence numbers (columns 1-6) if present
        content = stripped
        if len(content) > 6 and content[:6].strip().isdigit():
            content = content[6:]

        # Remove inline comments
        content = content.split("*")[0] if "*" in content else content

        buffer += " " + content.strip()
        if "." in buffer:
            cleaned_lines.append(buffer.strip())
            buffer = ""

    if buffer.strip():
        cleaned_lines.append(buffer.strip())

    # Pattern to match field definitions
    field_pattern = re.compile(
        r"(\d{2})\s+([\w-]+)\s+PIC\s+([^\s.]+)", re.IGNORECASE
    )
    filler_pattern = re.compile(
        r"(\d{2})\s+FILLER\s+PIC\s+([^\s.]+)", re.IGNORECASE
    )
    redefines_pattern = re.compile(r"REDEFINES", re.IGNORECASE)
    value_pattern = re.compile(r"VALUE\s+", re.IGNORECASE)

    for line in cleaned_lines:
        # Skip REDEFINES lines (they share the same storage)
        if redefines_pattern.search(line):
            continue

        # Check for FILLER
        filler_match = filler_pattern.search(line)
        if filler_match:
            level = int(filler_match.group(1))
            pic = filler_match.group(2)
            length, ftype, decimals = pic_length(pic)
            fields.append(
                CopybookField(
                    name="FILLER",
                    pic=pic,
                    offset=current_offset,
                    length=length,
                    field_type=ftype,
                    decimal_places=decimals,
                    level=level,
                )
            )
            current_offset += length
            continue

        # Check for named field with PIC
        field_match = field_pattern.search(line)
        if field_match:
            level = int(field_match.group(1))
            name = field_match.group(2)
            pic = field_match.group(3)

            # Remove trailing period if present
            pic = pic.rstrip(".")

            length, ftype, decimals = pic_length(pic)
            fields.append(
                CopybookField(
                    name=name,
                    pic=pic,
                    offset=current_offset,
                    length=length,
                    field_type=ftype,
                    decimal_places=decimals,
                    level=level,
                )
            )
            current_offset += length

    return fields


def fields_to_dict(fields: list) -> list:
    """Convert CopybookField list to a list of plain dicts for JSON output."""
    return [
        {
            "name": f.name,
            "pic": f.pic,
            "offset": f.offset,
            "length": f.length,
            "type": f.field_type,
            "decimal_places": f.decimal_places,
            "level": f.level,
        }
        for f in fields
    ]
