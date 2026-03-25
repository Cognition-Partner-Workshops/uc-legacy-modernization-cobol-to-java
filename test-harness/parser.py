"""
Copybook-aware fixed-width file parser for CardDemo ASCII data files.

Parses fixed-width records using COBOL copybook layouts defined in layouts.py
and produces structured Python dicts (serializable to JSON).

Usage:
    from parser import parse_file
    records = parse_file("app/data/ASCII/acctdata.txt", "acctdata")

    # Or from command line:
    python parser.py app/data/ASCII/acctdata.txt acctdata
"""

import json
import sys
from decimal import Decimal, InvalidOperation
from pathlib import Path

from layouts import FILE_LAYOUTS, FILE_COPYBOOKS


def parse_signed_decimal(raw: str, decimal_places: int = 2) -> float:
    """Parse a COBOL signed decimal display field (PIC S9(n)V99).

    COBOL stores signed decimals in "display" format where the last byte
    encodes both the final digit and the sign. In ASCII representation:
      - Positive: last digit is '{' (0), 'A'-'I' (1-9)
      - Negative: last digit is '}' (0), 'J'-'R' (1-9)

    The 'V' (implied decimal) means the last `decimal_places` digits
    before sign encoding are fractional.
    """
    if not raw or raw.isspace():
        return 0.0

    raw = raw.strip()
    if not raw:
        return 0.0

    # Map overpunch characters to (digit, sign)
    positive_map = {
        "{": ("0", "+"), "A": ("1", "+"), "B": ("2", "+"),
        "C": ("3", "+"), "D": ("4", "+"), "E": ("5", "+"),
        "F": ("6", "+"), "G": ("7", "+"), "H": ("8", "+"),
        "I": ("9", "+"),
    }
    negative_map = {
        "}": ("0", "-"), "J": ("1", "-"), "K": ("2", "-"),
        "L": ("3", "-"), "M": ("4", "-"), "N": ("5", "-"),
        "O": ("6", "-"), "P": ("7", "-"), "Q": ("8", "-"),
        "R": ("9", "-"),
    }

    last_char = raw[-1]
    prefix = raw[:-1]

    if last_char in positive_map:
        digit, sign = positive_map[last_char]
        numeric_str = sign + prefix + digit
    elif last_char in negative_map:
        digit, sign = negative_map[last_char]
        numeric_str = sign + prefix + digit
    elif last_char.isdigit():
        # No sign overpunch — treat as positive
        numeric_str = raw
    else:
        # Fallback: try direct conversion
        numeric_str = raw

    # Insert implied decimal point
    try:
        if decimal_places > 0 and len(numeric_str.lstrip("+-")) > decimal_places:
            int_part = numeric_str[:-decimal_places]
            dec_part = numeric_str[-decimal_places:]
            numeric_str = int_part + "." + dec_part
        result = float(Decimal(numeric_str))
        return round(result, decimal_places)
    except (InvalidOperation, ValueError):
        return 0.0


def parse_unsigned_numeric(raw: str) -> int:
    """Parse a COBOL unsigned numeric field (PIC 9(n))."""
    raw = raw.strip()
    if not raw:
        return 0
    try:
        return int(raw)
    except ValueError:
        return 0


def parse_record(line: str, layout: list) -> dict:
    """Parse a single fixed-width record using the given layout.

    Args:
        line: The raw fixed-width text line
        layout: List of (field_name, field_type, length) tuples

    Returns:
        dict with parsed field values (FILLER fields excluded)
    """
    record = {}
    offset = 0

    for field_name, field_type, length in layout:
        raw = line[offset:offset + length]
        offset += length

        if field_type == "FILLER":
            continue

        if field_type == "X":
            record[field_name] = raw.rstrip()
        elif field_type == "9":
            record[field_name] = parse_unsigned_numeric(raw)
        elif field_type == "S9V2":
            record[field_name] = parse_signed_decimal(raw, decimal_places=2)
        else:
            record[field_name] = raw.rstrip()

    return record


def parse_file(file_path: str, file_key: str) -> dict:
    """Parse an entire fixed-width data file into structured JSON.

    Args:
        file_path: Path to the ASCII data file
        file_key: Key into FILE_LAYOUTS (e.g., "acctdata")

    Returns:
        dict with metadata and list of parsed records
    """
    layout = FILE_LAYOUTS[file_key]
    copybook = FILE_COPYBOOKS[file_key]
    record_length = sum(length for _, _, length in layout)

    path = Path(file_path)
    records = []

    with open(path, "r") as f:
        for line_num, line in enumerate(f, 1):
            # Strip trailing newline but preserve fixed-width content
            line = line.rstrip("\n").rstrip("\r")

            # Pad short lines to expected record length
            if len(line) < record_length:
                line = line.ljust(record_length)

            record = parse_record(line, layout)
            record["_line_number"] = line_num
            records.append(record)

    return {
        "metadata": {
            "source_file": path.name,
            "copybook": copybook,
            "record_length": record_length,
            "record_count": len(records),
            "layout_fields": [
                name for name, ftype, _ in layout if ftype != "FILLER"
            ],
        },
        "records": records,
    }


def main():
    """CLI entry point: python parser.py <file_path> <file_key> [output_path]"""
    if len(sys.argv) < 3:
        print("Usage: python parser.py <data_file> <file_key> [output.json]")
        print(f"  file_key: {', '.join(FILE_LAYOUTS.keys())}")
        sys.exit(1)

    file_path = sys.argv[1]
    file_key = sys.argv[2]
    output_path = sys.argv[3] if len(sys.argv) > 3 else None

    if file_key not in FILE_LAYOUTS:
        print(f"Unknown file_key: {file_key}")
        print(f"  Valid keys: {', '.join(FILE_LAYOUTS.keys())}")
        sys.exit(1)

    result = parse_file(file_path, file_key)

    json_str = json.dumps(result, indent=2, default=str)

    if output_path:
        with open(output_path, "w") as f:
            f.write(json_str)
            f.write("\n")
        print(f"Wrote {result['metadata']['record_count']} records to {output_path}")
    else:
        print(json_str)


if __name__ == "__main__":
    main()
