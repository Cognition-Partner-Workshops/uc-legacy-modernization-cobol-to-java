"""
Fixed-width record parser using COBOL copybook layout definitions.

Handles:
- PIC X(n): Alphanumeric fields (trimmed strings)
- PIC 9(n): Unsigned numeric fields (integers)
- PIC S9(n)V99: Signed zoned decimal with implied decimal point
- FILLER: Skipped padding bytes
- Sign overpunch encoding for zoned decimals
"""

import json
import sys
from pathlib import Path
from typing import Any

from parsers.layouts import FILE_LAYOUT_MAP

# Zoned decimal sign overpunch mapping (EBCDIC convention in ASCII representation)
# Positive: { = 0, A = 1, B = 2, C = 3, D = 4, E = 5, F = 6, G = 7, H = 8, I = 9
# Negative: } = 0, J = 1, K = 2, L = 3, M = 4, N = 5, O = 6, P = 7, Q = 8, R = 9
POSITIVE_OVERPUNCH = {
    "{": 0, "A": 1, "B": 2, "C": 3, "D": 4,
    "E": 5, "F": 6, "G": 7, "H": 8, "I": 9,
}

NEGATIVE_OVERPUNCH = {
    "}": 0, "J": 1, "K": 2, "L": 3, "M": 4,
    "N": 5, "O": 6, "P": 7, "Q": 8, "R": 9,
}


def decode_zoned_decimal(raw: str, decimal_places: int) -> float:
    """Decode a zoned decimal field with sign overpunch on the last character.

    Args:
        raw: The raw string value from the fixed-width file
        decimal_places: Number of implied decimal places (e.g., V99 = 2)

    Returns:
        Decoded float value with correct sign and scale
    """
    if not raw or raw.isspace():
        return 0.0

    raw = raw.strip()
    if not raw:
        return 0.0

    last_char = raw[-1]
    sign = 1
    last_digit = last_char

    if last_char in POSITIVE_OVERPUNCH:
        sign = 1
        last_digit = str(POSITIVE_OVERPUNCH[last_char])
    elif last_char in NEGATIVE_OVERPUNCH:
        sign = -1
        last_digit = str(NEGATIVE_OVERPUNCH[last_char])
    elif last_char.isdigit():
        last_digit = last_char
    else:
        last_digit = "0"

    numeric_str = raw[:-1] + last_digit

    # Remove any non-digit characters
    cleaned = "".join(c for c in numeric_str if c.isdigit())
    if not cleaned:
        return 0.0

    integer_value = int(cleaned)
    result = integer_value / (10 ** decimal_places) if decimal_places > 0 else float(integer_value)
    return sign * result


def parse_field(raw: str, pic_type: str, options: dict) -> Any:
    """Parse a single field value based on its PIC type.

    Args:
        raw: Raw string extracted from the fixed-width record
        pic_type: The PIC type ('X', '9', 'S9', 'FILLER')
        options: Additional options (e.g., {'decimal': 2})

    Returns:
        Parsed value (str, int, float, or None for FILLER)
    """
    if pic_type == "FILLER":
        return None

    if pic_type == "X":
        return raw.rstrip()

    if pic_type == "9":
        decimal_places = options.get("decimal", 0)
        cleaned = "".join(c for c in raw if c.isdigit())
        if not cleaned:
            return 0 if decimal_places == 0 else 0.0
        value = int(cleaned)
        if decimal_places > 0:
            return value / (10 ** decimal_places)
        return value

    if pic_type == "S9":
        decimal_places = options.get("decimal", 0)
        return decode_zoned_decimal(raw, decimal_places)

    return raw.rstrip()


def parse_record(line: str, layout: dict) -> dict:
    """Parse a single fixed-width record using the given layout.

    Args:
        line: A single line from the data file
        layout: Layout definition dict with 'fields' list

    Returns:
        Dictionary of field_name -> parsed_value (excludes FILLER)
    """
    record = {}
    offset = 0

    for field_name, pic_type, length, options in layout["fields"]:
        raw = line[offset:offset + length]
        offset += length

        if pic_type == "FILLER":
            continue

        value = parse_field(raw, pic_type, options)
        record[field_name] = value

    return record


def parse_file(file_path: str, layout: dict) -> list[dict]:
    """Parse an entire data file using the given layout.

    Args:
        file_path: Path to the ASCII data file
        layout: Layout definition dict from layouts.py

    Returns:
        List of parsed records as dictionaries
    """
    records = []
    path = Path(file_path)

    with path.open("r", encoding="ascii", errors="replace") as f:
        for line_num, line in enumerate(f, 1):
            # Strip newline but preserve record content
            line = line.rstrip("\n").rstrip("\r")
            if not line:
                continue

            try:
                record = parse_record(line, layout)
                record["_meta"] = {
                    "source_file": path.name,
                    "line_number": line_num,
                    "record_length": len(line),
                    "expected_length": layout["record_length"],
                }
                records.append(record)
            except Exception as e:
                record = {
                    "_meta": {
                        "source_file": path.name,
                        "line_number": line_num,
                        "error": str(e),
                    }
                }
                records.append(record)

    return records


def generate_golden_file(data_file: str, layout_name: str, output_path: str) -> None:
    """Parse a data file and write the golden JSON reference.

    Args:
        data_file: Path to the ASCII data file
        layout_name: Key into FILE_LAYOUT_MAP
        output_path: Path to write the golden JSON file
    """
    layout = FILE_LAYOUT_MAP[layout_name]
    records = parse_file(data_file, layout)

    output = {
        "metadata": {
            "source_file": str(data_file),
            "copybook": layout["copybook"],
            "record_length": layout["record_length"],
            "total_records": len(records),
            "layout_name": layout_name,
        },
        "records": records,
    }

    output_file = Path(output_path)
    output_file.parent.mkdir(parents=True, exist_ok=True)

    with output_file.open("w", encoding="utf-8") as f:
        json.dump(output, f, indent=2, ensure_ascii=False)


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python -m test_harness.parsers.copybook_parser <data_dir> <output_dir>")
        print("  Parses all ASCII data files and generates golden JSON references.")
        sys.exit(1)

    data_dir = Path(sys.argv[1])
    output_dir = Path(sys.argv[2])

    for file_key, layout in FILE_LAYOUT_MAP.items():
        data_file = data_dir / f"{file_key}.txt"
        if data_file.exists():
            output_file = output_dir / f"{file_key}.golden.json"
            print(f"Parsing {data_file.name} -> {output_file.name}")
            generate_golden_file(str(data_file), file_key, str(output_file))
        else:
            print(f"WARNING: {data_file} not found, skipping")

    print(f"\nGolden files written to: {output_dir}")
