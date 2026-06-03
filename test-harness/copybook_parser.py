"""
Copybook Parser — Parses COBOL copybook definitions and extracts field layouts.

Reads .cpy files from app/cpy/ and produces structured field definitions
including name, offset, length, picture clause, and data type.
"""

import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Optional


@dataclass
class CopybookField:
    """A single field extracted from a COBOL copybook."""

    name: str
    level: int
    picture: str
    offset: int
    length: int
    data_type: str  # "numeric", "alphanumeric", "signed_decimal", "packed"
    decimal_places: int = 0
    is_filler: bool = False
    children: list = field(default_factory=list)


def pic_length(pic: str) -> tuple[int, int, str]:
    """
    Compute the storage length, decimal places, and type from a PIC clause.

    Returns (length, decimal_places, data_type).
    """
    pic = pic.upper().replace(" ", "")

    # Signed decimal: S9(n)V99 or S9(n)V9(n)
    m = re.match(r"S9\((\d+)\)V9+", pic)
    if m:
        int_digits = int(m.group(1))
        dec_part = pic.split("V")[1]
        dec_digits = _count_digits(dec_part)
        return int_digits + dec_digits, dec_digits, "signed_decimal"

    m = re.match(r"S9\((\d+)\)V9\((\d+)\)", pic)
    if m:
        int_digits = int(m.group(1))
        dec_digits = int(m.group(2))
        return int_digits + dec_digits, dec_digits, "signed_decimal"

    # Signed without decimal: S9(n)
    m = re.match(r"S9\((\d+)\)", pic)
    if m:
        return int(m.group(1)), 0, "signed_decimal"

    # Unsigned decimal: 9(n)V9(n)
    m = re.match(r"9\((\d+)\)V9\((\d+)\)", pic)
    if m:
        int_digits = int(m.group(1))
        dec_digits = int(m.group(2))
        return int_digits + dec_digits, dec_digits, "numeric"

    m = re.match(r"9\((\d+)\)V9+", pic)
    if m:
        int_digits = int(m.group(1))
        dec_part = pic.split("V")[1]
        dec_digits = _count_digits(dec_part)
        return int_digits + dec_digits, dec_digits, "numeric"

    # Numeric: 9(n) or 9999...
    m = re.match(r"9\((\d+)\)", pic)
    if m:
        return int(m.group(1)), 0, "numeric"
    m = re.match(r"9+$", pic)
    if m:
        return len(m.group(0)), 0, "numeric"

    # Alphanumeric: X(n) or XX...
    m = re.match(r"X\((\d+)\)", pic)
    if m:
        return int(m.group(1)), 0, "alphanumeric"
    m = re.match(r"X+$", pic)
    if m:
        return len(m.group(0)), 0, "alphanumeric"

    # Fallback
    return 0, 0, "unknown"


def _count_digits(s: str) -> int:
    """Count digit positions in a PIC fragment like '99' or '9(2)'."""
    m = re.match(r"9\((\d+)\)", s)
    if m:
        return int(m.group(1))
    return s.count("9")


def parse_copybook(filepath: Path) -> list[CopybookField]:
    """
    Parse a COBOL copybook file and return a flat list of fields with offsets.

    Skips:
      - Comment lines (column 7 = '*')
      - REDEFINES clauses (alternate views of same storage)
      - 88-level condition names
      - Group-level items without PIC (only tracks elementary items)
    """
    fields: list[CopybookField] = []
    current_offset = 0

    lines = filepath.read_text().splitlines()

    for raw_line in lines:
        # Strip sequence numbers (cols 1-6) if present
        line = _strip_sequence_numbers(raw_line)

        # Skip comments
        if not line or line.lstrip().startswith("*"):
            continue

        # Skip REDEFINES (alternate memory layout)
        if "REDEFINES" in line.upper():
            continue

        # Extract level number and field definition
        m = re.match(r"\s*(\d{2})\s+(\S+)", line)
        if not m:
            continue

        level = int(m.group(1))
        name = m.group(2).rstrip(".")

        # Skip 88-level conditions
        if level == 88:
            continue

        # Skip 01-level record name (group header)
        if level == 1:
            continue

        # Look for PIC clause
        pic_match = re.search(r"PIC\s+(\S+)", line, re.IGNORECASE)
        if not pic_match:
            # Group-level item without PIC — skip (children define storage)
            continue

        pic = pic_match.group(1).rstrip(".")
        length, decimals, dtype = pic_length(pic)

        is_filler = name.upper() == "FILLER"

        fields.append(
            CopybookField(
                name=name,
                level=level,
                picture=pic,
                offset=current_offset,
                length=length,
                data_type=dtype,
                decimal_places=decimals,
                is_filler=is_filler,
            )
        )

        current_offset += length

    return fields


def _strip_sequence_numbers(line: str) -> str:
    """Strip COBOL sequence numbers from columns 1-6 and trailing cols 73-80."""
    if len(line) < 7:
        return line
    # If cols 1-6 are all digits, strip them
    if line[:6].strip().isdigit() and len(line) >= 7:
        line = line[6:]
    # Strip trailing sequence area (cols 73-80) if line is long enough
    if len(line) > 72:
        line = line[:72]
    return line


def get_record_length(fields: list[CopybookField]) -> int:
    """Compute total record length from parsed fields."""
    if not fields:
        return 0
    last = fields[-1]
    return last.offset + last.length


# Mapping of data files to their copybook layouts
FILE_COPYBOOK_MAP = {
    "acctdata.txt": "CVACT01Y.cpy",
    "carddata.txt": "CVACT02Y.cpy",
    "cardxref.txt": "CVACT03Y.cpy",
    "custdata.txt": "CVCUS01Y.cpy",
    "dailytran.txt": "CVTRA06Y.cpy",
    "discgrp.txt": "CVTRA02Y.cpy",
    "tcatbal.txt": "CVTRA01Y.cpy",
    "trancatg.txt": "CVTRA04Y.cpy",
    "trantype.txt": "CVTRA03Y.cpy",
}
