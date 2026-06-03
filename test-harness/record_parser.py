"""
Record Parser — Parses fixed-width ASCII data files using copybook field definitions.

Converts raw mainframe-format records into structured Python dicts/JSON.
Handles COBOL numeric sign conventions (trailing overpunch) and decimal scaling.
"""

import json
from pathlib import Path
from typing import Any

from copybook_parser import CopybookField, parse_copybook, FILE_COPYBOOK_MAP


# COBOL zoned-decimal trailing overpunch sign encoding (EBCDIC convention in ASCII files)
# Positive: { = +0, A = +1, B = +2, ..., I = +9
# Negative: } = -0, J = -1, K = -2, ..., R = -9
POSITIVE_OVERPUNCH = {"{": 0, "A": 1, "B": 2, "C": 3, "D": 4, "E": 5, "F": 6, "G": 7, "H": 8, "I": 9}
NEGATIVE_OVERPUNCH = {"}": 0, "J": 1, "K": 2, "L": 3, "M": 4, "N": 5, "O": 6, "P": 7, "Q": 8, "R": 9}


def decode_signed_numeric(raw: str, decimal_places: int) -> float:
    """
    Decode a COBOL signed numeric field (zoned decimal with overpunch).

    The last character encodes both the sign and the final digit.
    """
    if not raw or raw.isspace():
        return 0.0

    raw = raw.strip()
    if not raw:
        return 0.0

    last_char = raw[-1]
    prefix = raw[:-1]

    sign = 1
    last_digit = 0

    if last_char in POSITIVE_OVERPUNCH:
        last_digit = POSITIVE_OVERPUNCH[last_char]
        sign = 1
    elif last_char in NEGATIVE_OVERPUNCH:
        last_digit = NEGATIVE_OVERPUNCH[last_char]
        sign = -1
    elif last_char.isdigit():
        last_digit = int(last_char)
        sign = 1
    else:
        # Unknown encoding — treat as zero
        return 0.0

    digits_str = prefix + str(last_digit)
    try:
        int_value = int(digits_str)
    except ValueError:
        return 0.0

    if decimal_places > 0:
        return sign * int_value / (10**decimal_places)
    return float(sign * int_value)


def decode_unsigned_numeric(raw: str, decimal_places: int) -> Any:
    """Decode an unsigned numeric field."""
    raw = raw.strip()
    if not raw:
        return 0

    # Remove any non-digit characters
    digits = "".join(c for c in raw if c.isdigit())
    if not digits:
        return 0

    int_value = int(digits)
    if decimal_places > 0:
        return int_value / (10**decimal_places)
    return int_value


def parse_record(line: str, fields: list[CopybookField]) -> dict[str, Any]:
    """
    Parse a single fixed-width record line into a dict using field definitions.

    Skips FILLER fields. Applies numeric decoding for signed/unsigned fields.
    """
    record: dict[str, Any] = {}

    for f in fields:
        if f.is_filler:
            continue

        # Extract raw value from fixed-width position
        raw = line[f.offset : f.offset + f.length] if f.offset + f.length <= len(line) else line[f.offset:]

        if f.data_type == "signed_decimal":
            record[f.name] = decode_signed_numeric(raw, f.decimal_places)
        elif f.data_type == "numeric":
            record[f.name] = decode_unsigned_numeric(raw, f.decimal_places)
        elif f.data_type == "alphanumeric":
            record[f.name] = raw.rstrip()
        else:
            record[f.name] = raw.rstrip()

    return record


def parse_data_file(
    data_path: Path, copybook_path: Path, max_records: int = 0
) -> list[dict[str, Any]]:
    """
    Parse an entire ASCII data file using the given copybook layout.

    Args:
        data_path: Path to the fixed-width ASCII data file.
        copybook_path: Path to the COBOL copybook (.cpy) file.
        max_records: If > 0, only parse this many records (for sampling).

    Returns:
        List of parsed record dicts.
    """
    fields = parse_copybook(copybook_path)
    records = []

    with data_path.open("r", encoding="ascii", errors="replace") as f:
        for i, line in enumerate(f):
            if max_records > 0 and i >= max_records:
                break
            # Strip trailing newline/CR but preserve fixed-width content
            line = line.rstrip("\n").rstrip("\r")
            if not line:
                continue
            records.append(parse_record(line, fields))

    return records


def generate_golden_files(
    data_dir: Path, copybook_dir: Path, output_dir: Path
) -> dict[str, int]:
    """
    Generate golden JSON reference files for all mapped data files.

    Returns a dict of {filename: record_count} for summary reporting.
    """
    output_dir.mkdir(parents=True, exist_ok=True)
    summary: dict[str, int] = {}

    for data_file, copybook_file in FILE_COPYBOOK_MAP.items():
        data_path = data_dir / data_file
        copybook_path = copybook_dir / copybook_file

        if not data_path.exists():
            print(f"  SKIP: {data_file} (file not found)")
            continue
        if not copybook_path.exists():
            print(f"  SKIP: {data_file} (copybook {copybook_file} not found)")
            continue

        records = parse_data_file(data_path, copybook_path)

        output_file = output_dir / data_file.replace(".txt", ".json")
        with output_file.open("w") as out:
            json.dump(
                {
                    "source_file": data_file,
                    "copybook": copybook_file,
                    "record_count": len(records),
                    "records": records,
                },
                out,
                indent=2,
            )

        summary[data_file] = len(records)
        print(f"  OK: {data_file} -> {output_file.name} ({len(records)} records)")

    return summary


if __name__ == "__main__":
    import sys

    repo_root = Path(__file__).resolve().parent.parent
    data_dir = repo_root / "app" / "data" / "ASCII"
    copybook_dir = repo_root / "app" / "cpy"
    output_dir = repo_root / "golden-files"

    print("Generating golden reference files...")
    summary = generate_golden_files(data_dir, copybook_dir, output_dir)
    print(f"\nDone. Generated {len(summary)} golden files.")
    total = sum(summary.values())
    print(f"Total records: {total}")
