"""
Copybook-driven fixed-width record parser for CardDemo ASCII data files.

Each copybook layout is defined as a list of field descriptors:
    (field_name, pic_type, length, decimals)

Where:
    - field_name: COBOL field name (FILLER fields are ignored in output)
    - pic_type:   'numeric'  -> PIC 9(n) or PIC S9(n)V99
                  'alpha'    -> PIC X(n)
    - length:     total character width consumed in the ASCII record
    - decimals:   number of implied decimal places (0 for integers and alpha)
"""

import json
import os
import re
from collections import OrderedDict

# ---------------------------------------------------------------------------
# COBOL sign-overpunch decoding table
# In zoned-decimal ASCII representation the trailing character encodes the sign.
# Positive: { A B C D E F G H I  -> 0 1 2 3 4 5 6 7 8
# Negative: } J K L M N O P Q R  -> 0 1 2 3 4 5 6 7 8
# ---------------------------------------------------------------------------
_POSITIVE_OVERPUNCH = {
    "{": "0", "A": "1", "B": "2", "C": "3", "D": "4",
    "E": "5", "F": "6", "G": "7", "H": "8", "I": "9",
}
_NEGATIVE_OVERPUNCH = {
    "}": "0", "J": "1", "K": "2", "L": "3", "M": "4",
    "N": "5", "O": "6", "P": "7", "Q": "8", "R": "9",
}


def decode_signed_numeric(raw, decimals=0):
    """Decode a COBOL zoned-decimal field with optional sign overpunch.

    Args:
        raw: The raw string from the fixed-width record.
        decimals: Number of implied decimal places (e.g. 2 for PIC S9(10)V99).

    Returns:
        A Python float (if decimals > 0) or int.
    """
    if not raw or raw.isspace():
        return 0

    raw = raw.strip()
    if not raw:
        return 0

    last_char = raw[-1]
    sign = 1

    if last_char in _POSITIVE_OVERPUNCH:
        raw = raw[:-1] + _POSITIVE_OVERPUNCH[last_char]
        sign = 1
    elif last_char in _NEGATIVE_OVERPUNCH:
        raw = raw[:-1] + _NEGATIVE_OVERPUNCH[last_char]
        sign = -1

    # Remove any remaining non-digit characters
    cleaned = re.sub(r"[^0-9]", "", raw)
    if not cleaned:
        return 0

    value = int(cleaned) * sign

    if decimals > 0:
        return round(value / (10 ** decimals), decimals)
    return value


def decode_unsigned_numeric(raw, decimals=0):
    """Decode an unsigned COBOL numeric field (PIC 9(n)).

    Args:
        raw: The raw string from the fixed-width record.
        decimals: Number of implied decimal places.

    Returns:
        A Python int (decimals == 0) or float.
    """
    if not raw or raw.isspace():
        return 0

    cleaned = re.sub(r"[^0-9]", "", raw.strip())
    if not cleaned:
        return 0

    value = int(cleaned)
    if decimals > 0:
        return round(value / (10 ** decimals), decimals)
    return value


def parse_record(line, layout):
    """Parse a single fixed-width record according to a copybook layout.

    Args:
        line: The raw text line (fixed-width record).
        layout: List of (field_name, pic_type, length, decimals) tuples.

    Returns:
        An OrderedDict of field_name -> parsed value.
        FILLER fields are excluded.
    """
    record = OrderedDict()
    offset = 0

    for field_name, pic_type, length, decimals in layout:
        raw = line[offset:offset + length]
        offset += length

        if field_name == "FILLER":
            continue

        if pic_type == "alpha":
            record[field_name] = raw.rstrip()
        elif pic_type == "signed_numeric":
            record[field_name] = decode_signed_numeric(raw, decimals)
        elif pic_type == "numeric":
            record[field_name] = decode_unsigned_numeric(raw, decimals)
        else:
            record[field_name] = raw.rstrip()

    return record


def parse_file(filepath, layout):
    """Parse an entire fixed-width data file.

    Args:
        filepath: Path to the ASCII data file.
        layout: Copybook layout definition.

    Returns:
        A list of OrderedDict records.
    """
    records = []
    with open(filepath, "r", encoding="ascii", errors="replace") as f:
        for line in f:
            # Skip empty lines
            stripped = line.rstrip("\n\r")
            if not stripped:
                continue
            records.append(parse_record(stripped, layout))
    return records


def write_golden_file(records, output_path):
    """Write parsed records to a JSON golden file.

    Args:
        records: List of OrderedDict records.
        output_path: Destination JSON file path.
    """
    os.makedirs(os.path.dirname(output_path) or ".", exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(
            {"record_count": len(records), "records": records},
            f,
            indent=2,
            ensure_ascii=False,
        )
