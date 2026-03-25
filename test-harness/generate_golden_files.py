#!/usr/bin/env python3
"""
generate_golden_files.py -- Parse ASCII fixed-width data files using COBOL
copybook layouts and produce structured JSON golden-reference files.

Usage:
    python generate_golden_files.py [--data-dir PATH] [--output-dir PATH]

Defaults:
    --data-dir   ../app/data/ASCII
    --output-dir ../golden-files
"""

import argparse
import json
import os
import sys

from copybook_parser import FILE_LAYOUTS, compute_layout_length
from field_parser import extract_field


def parse_record(line: str, layout: list[dict]) -> dict:
    """Parse a single fixed-width record line into a dict using the layout.

    Args:
        line: Raw record string (without trailing newline).
        layout: List of field descriptors from copybook_parser.

    Returns:
        Dict mapping field names to parsed values (FILLER fields omitted).
    """
    record = {}
    offset = 0
    for field in layout:
        value, offset = extract_field(
            line, offset, field["length"], field["type"], field["decimal"]
        )
        if field["type"] != "filler":
            record[field["name"]] = value
    return record


def parse_file(filepath: str, file_key: str) -> dict:
    """Parse an entire ASCII data file into a structured golden-file dict.

    Args:
        filepath: Path to the .txt data file.
        file_key: Key into FILE_LAYOUTS (e.g., "acctdata").

    Returns:
        Dict with metadata and list of parsed records.
    """
    layout_info = FILE_LAYOUTS[file_key]
    layout = layout_info["layout"]
    expected_reclen = layout_info["reclen"]
    copybook = layout_info["copybook"]
    computed_len = compute_layout_length(layout)

    records = []
    parse_errors = []

    with open(filepath, "r", encoding="ascii", errors="replace") as fh:
        for line_num, raw_line in enumerate(fh, start=1):
            # Strip only the trailing newline, preserve spaces
            line = raw_line.rstrip("\n").rstrip("\r")

            if not line:
                continue

            actual_len = len(line)

            # Some files have slight length variations; parse what we can
            if actual_len < computed_len:
                # Pad short records with spaces
                line = line.ljust(computed_len)
            elif actual_len > computed_len:
                # Use only the bytes the layout expects
                pass

            try:
                record = parse_record(line, layout)
                record["_line_number"] = line_num
                records.append(record)
            except Exception as exc:
                parse_errors.append({
                    "line_number": line_num,
                    "error": str(exc),
                    "raw_length": actual_len,
                })

    return {
        "metadata": {
            "source_file": os.path.basename(filepath),
            "copybook": copybook,
            "expected_record_length": expected_reclen,
            "computed_layout_length": computed_len,
            "total_records": len(records),
            "parse_errors": len(parse_errors),
        },
        "records": records,
        "errors": parse_errors if parse_errors else [],
    }


def main() -> int:
    script_dir = os.path.dirname(os.path.abspath(__file__))
    default_data_dir = os.path.join(script_dir, "..", "app", "data", "ASCII")
    default_output_dir = os.path.join(script_dir, "..", "golden-files")

    parser = argparse.ArgumentParser(
        description="Generate golden-reference JSON files from ASCII COBOL data."
    )
    parser.add_argument(
        "--data-dir", default=default_data_dir,
        help="Path to ASCII data directory"
    )
    parser.add_argument(
        "--output-dir", default=default_output_dir,
        help="Path to output golden-files directory"
    )
    args = parser.parse_args()

    os.makedirs(args.output_dir, exist_ok=True)

    total_files = 0
    total_records = 0
    total_errors = 0

    for file_key, layout_info in sorted(FILE_LAYOUTS.items()):
        source_path = os.path.join(args.data_dir, f"{file_key}.txt")
        if not os.path.exists(source_path):
            print(f"  SKIP  {file_key}.txt (not found)")
            continue

        golden = parse_file(source_path, file_key)
        output_path = os.path.join(args.output_dir, f"{file_key}.golden.json")

        with open(output_path, "w", encoding="utf-8") as out:
            json.dump(golden, out, indent=2, default=str)

        rec_count = golden["metadata"]["total_records"]
        err_count = golden["metadata"]["parse_errors"]
        status = "OK" if err_count == 0 else f"WARN ({err_count} errors)"

        print(f"  {status:12s} {file_key:12s} -> {rec_count:>5d} records  [{layout_info['copybook']}]")

        total_files += 1
        total_records += rec_count
        total_errors += err_count

    print(f"\nSummary: {total_files} files, {total_records} records, {total_errors} parse errors")

    return 1 if total_errors > 0 else 0


if __name__ == "__main__":
    sys.exit(main())
