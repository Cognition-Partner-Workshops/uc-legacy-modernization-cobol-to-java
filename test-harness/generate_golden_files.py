#!/usr/bin/env python3
"""
Generate golden JSON reference files from CardDemo ASCII data files.

Reads each ASCII data file in app/data/ASCII/, parses it using the
corresponding COBOL copybook layout, and writes structured JSON to
the golden-files/ directory.

Usage:
    python3 generate_golden_files.py [--data-dir PATH] [--copybook-dir PATH] [--output-dir PATH]
"""

import json
import os
import sys
import argparse

from copybook_parser import parse_copybook, fields_to_dict
from ascii_parser import parse_file


# Mapping of ASCII data files to their copybook layouts
FILE_COPYBOOK_MAP = {
    "acctdata.txt": "CVACT01Y.cpy",
    "carddata.txt": "CVACT02Y.cpy",
    "cardxref.txt": "CVACT03Y.cpy",
    "custdata.txt": "CVCUS01Y.cpy",
    "dailytran.txt": "CVTRA06Y.cpy",
    "trantype.txt": "CVTRA03Y.cpy",
    "trancatg.txt": "CVTRA04Y.cpy",
    "discgrp.txt": "CVTRA02Y.cpy",
    "tcatbal.txt": "CVTRA01Y.cpy",
}


def generate_golden_file(
    data_file: str,
    copybook_file: str,
    output_file: str,
) -> dict:
    """Parse a data file and write golden JSON output.

    Args:
        data_file: Path to the ASCII data file.
        copybook_file: Path to the COBOL copybook.
        output_file: Path for the output JSON file.

    Returns:
        Dictionary with metadata about the generation.
    """
    fields = parse_copybook(copybook_file)
    records = parse_file(data_file, fields)

    output = {
        "metadata": {
            "source_file": os.path.basename(data_file),
            "copybook": os.path.basename(copybook_file),
            "record_count": len(records),
            "field_definitions": fields_to_dict(fields),
        },
        "records": records,
    }

    os.makedirs(os.path.dirname(output_file), exist_ok=True)
    with open(output_file, "w") as f:
        json.dump(output, f, indent=2)

    return {
        "file": os.path.basename(output_file),
        "records": len(records),
        "fields": len([fd for fd in fields if fd.name != "FILLER"]),
    }


def main():
    parser = argparse.ArgumentParser(
        description="Generate golden JSON files from CardDemo ASCII data."
    )
    parser.add_argument(
        "--data-dir",
        default=None,
        help="Path to ASCII data directory (default: auto-detect)",
    )
    parser.add_argument(
        "--copybook-dir",
        default=None,
        help="Path to copybook directory (default: auto-detect)",
    )
    parser.add_argument(
        "--output-dir",
        default=None,
        help="Path to golden-files output directory (default: auto-detect)",
    )
    args = parser.parse_args()

    # Auto-detect paths relative to repo root
    script_dir = os.path.dirname(os.path.abspath(__file__))
    repo_root = os.path.dirname(script_dir)

    data_dir = args.data_dir or os.path.join(repo_root, "app", "data", "ASCII")
    copybook_dir = args.copybook_dir or os.path.join(repo_root, "app", "cpy")
    output_dir = args.output_dir or os.path.join(repo_root, "golden-files")

    if not os.path.isdir(data_dir):
        print(f"ERROR: Data directory not found: {data_dir}", file=sys.stderr)
        sys.exit(1)

    if not os.path.isdir(copybook_dir):
        print(f"ERROR: Copybook directory not found: {copybook_dir}", file=sys.stderr)
        sys.exit(1)

    print(f"Data directory:     {data_dir}")
    print(f"Copybook directory: {copybook_dir}")
    print(f"Output directory:   {output_dir}")
    print()

    results = []
    errors = []

    for data_filename, copybook_filename in sorted(FILE_COPYBOOK_MAP.items()):
        data_path = os.path.join(data_dir, data_filename)
        copybook_path = os.path.join(copybook_dir, copybook_filename)
        output_filename = data_filename.replace(".txt", ".json")
        output_path = os.path.join(output_dir, output_filename)

        if not os.path.isfile(data_path):
            errors.append(f"Data file not found: {data_path}")
            continue

        if not os.path.isfile(copybook_path):
            errors.append(f"Copybook not found: {copybook_path}")
            continue

        try:
            result = generate_golden_file(data_path, copybook_path, output_path)
            results.append(result)
            print(
                f"  OK  {result['file']:20s}  "
                f"{result['records']:4d} records, "
                f"{result['fields']:2d} fields"
            )
        except Exception as e:
            errors.append(f"Failed to process {data_filename}: {e}")
            print(f"  FAIL {data_filename}: {e}")

    print()
    print(f"Generated: {len(results)} golden files")
    if errors:
        print(f"Errors:    {len(errors)}")
        for err in errors:
            print(f"  - {err}")
        sys.exit(1)

    print("All golden files generated successfully.")


if __name__ == "__main__":
    main()
