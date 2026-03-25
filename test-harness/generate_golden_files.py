#!/usr/bin/env python3
"""
Generate JSON golden reference files from CardDemo ASCII data files.

Usage:
    python generate_golden_files.py [--data-dir PATH] [--output-dir PATH]

Defaults:
    --data-dir   ../app/data/ASCII
    --output-dir ../golden-files
"""

import argparse
import os
import sys

from copybook_layouts import FILE_LAYOUT_MAP
from copybook_parser import parse_file, write_golden_file


def main():
    parser = argparse.ArgumentParser(
        description="Generate golden JSON files from CardDemo ASCII data."
    )
    parser.add_argument(
        "--data-dir",
        default=os.path.join(os.path.dirname(__file__), "..", "app", "data", "ASCII"),
        help="Path to the ASCII data directory.",
    )
    parser.add_argument(
        "--output-dir",
        default=os.path.join(os.path.dirname(__file__), "..", "golden-files"),
        help="Path to the golden-files output directory.",
    )
    args = parser.parse_args()

    data_dir = os.path.abspath(args.data_dir)
    output_dir = os.path.abspath(args.output_dir)

    if not os.path.isdir(data_dir):
        print(f"ERROR: Data directory not found: {data_dir}", file=sys.stderr)
        sys.exit(1)

    os.makedirs(output_dir, exist_ok=True)

    total_files = 0
    total_records = 0

    for filename, layout in sorted(FILE_LAYOUT_MAP.items()):
        filepath = os.path.join(data_dir, filename)
        if not os.path.isfile(filepath):
            print(f"WARNING: Data file not found, skipping: {filepath}")
            continue

        records = parse_file(filepath, layout)
        basename = os.path.splitext(filename)[0]
        output_path = os.path.join(output_dir, f"{basename}.json")
        write_golden_file(records, output_path)

        total_files += 1
        total_records += len(records)
        print(f"  {filename:20s} -> {basename}.json  ({len(records)} records)")

    print(f"\nGenerated {total_files} golden files with {total_records} total records.")


if __name__ == "__main__":
    main()
