#!/usr/bin/env python3
"""
Generate golden-file JSON references from CardDemo ASCII data files.

Reads each ASCII data file in app/data/ASCII/, parses it using the
corresponding COBOL copybook layout, and writes structured JSON to
golden-files/.
"""

from __future__ import annotations

import json
import os
import sys

from copybook_parser import parse_file
from layouts.definitions import FILE_LAYOUT_MAP


def generate_golden_files(
    data_dir: str,
    output_dir: str,
    indent: int = 2,
) -> dict[str, int]:
    """Generate golden JSON files for all known data files.

    Args:
        data_dir: Path to the directory containing ASCII .txt files.
        output_dir: Path to the golden-files output directory.
        indent: JSON indentation level.

    Returns:
        Dictionary mapping file basenames to record counts.
    """
    os.makedirs(output_dir, exist_ok=True)
    results = {}

    for basename, config in FILE_LAYOUT_MAP.items():
        input_path = os.path.join(data_dir, f"{basename}.txt")
        if not os.path.exists(input_path):
            print(f"WARNING: {input_path} not found, skipping", file=sys.stderr)
            continue

        records = parse_file(
            input_path,
            config["layout"],
            config["record_length"],
        )

        golden = {
            "metadata": {
                "source_file": f"{basename}.txt",
                "copybook": config["copybook"],
                "record_name": config["record_name"],
                "record_length": config["record_length"],
                "record_count": len(records),
            },
            "records": records,
        }

        output_path = os.path.join(output_dir, f"{basename}.golden.json")
        with open(output_path, "w", encoding="utf-8") as f:
            json.dump(golden, f, indent=indent, ensure_ascii=False)

        results[basename] = len(records)
        print(f"Generated {output_path}: {len(records)} records")

    return results


def main() -> None:
    repo_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    data_dir = os.path.join(repo_root, "app", "data", "ASCII")
    output_dir = os.path.join(repo_root, "golden-files")

    if not os.path.isdir(data_dir):
        print(f"ERROR: Data directory not found: {data_dir}", file=sys.stderr)
        sys.exit(1)

    results = generate_golden_files(data_dir, output_dir)
    total = sum(results.values())
    print(f"\nTotal: {len(results)} files, {total} records")


if __name__ == "__main__":
    main()
