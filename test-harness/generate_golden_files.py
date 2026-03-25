#!/usr/bin/env python3
"""
Golden File Generator for CardDemo Migration Test Harness.

Parses all ASCII data files in app/data/ASCII/ using their corresponding
copybook layouts and produces structured JSON golden references in golden-files/.

Usage:
    python3 test-harness/generate_golden_files.py [--data-dir app/data/ASCII/] [--output-dir golden-files/]
"""

import argparse
import sys
from pathlib import Path

# Ensure test-harness/ is on the path
sys.path.insert(0, str(Path(__file__).parent))

from copybook_parser import LAYOUT_REGISTRY, parse_file_to_json


# Mapping from ASCII data file stems to layout keys
DATA_FILE_MAP = {
    "acctdata":  "acctdata",
    "carddata":  "carddata",
    "cardxref":  "cardxref",
    "custdata":  "custdata",
    "dailytran": "dailytran",
    "discgrp":   "discgrp",
    "tcatbal":   "tcatbal",
    "trancatg":  "trancatg",
    "trantype":  "trantype",
}


def generate_all(data_dir: Path, output_dir: Path) -> dict:
    """Parse all data files and write golden JSON files.

    Returns a summary dict with file names, record counts, and status.
    """
    output_dir.mkdir(parents=True, exist_ok=True)
    summary = {}

    for file_stem, layout_key in sorted(DATA_FILE_MAP.items()):
        data_file = data_dir / f"{file_stem}.txt"
        output_file = output_dir / f"{file_stem}.json"

        if not data_file.exists():
            summary[file_stem] = {"status": "SKIPPED", "reason": f"{data_file} not found"}
            print(f"  SKIP  {file_stem}: {data_file} not found")
            continue

        if layout_key not in LAYOUT_REGISTRY:
            summary[file_stem] = {"status": "SKIPPED", "reason": f"No layout for {layout_key}"}
            print(f"  SKIP  {file_stem}: no layout registered for {layout_key}")
            continue

        layout = LAYOUT_REGISTRY[layout_key]

        try:
            json_str = parse_file_to_json(data_file, layout, output_file)
            # Count records from the output
            import json
            parsed = json.loads(json_str)
            record_count = parsed["_metadata"]["record_count"]
            summary[file_stem] = {
                "status": "OK",
                "records": record_count,
                "output": str(output_file),
            }
            print(f"  OK    {file_stem}: {record_count} records -> {output_file}")
        except Exception as e:
            summary[file_stem] = {"status": "ERROR", "reason": str(e)}
            print(f"  ERROR {file_stem}: {e}")

    return summary


def main():
    parser = argparse.ArgumentParser(
        description="Generate golden JSON files from CardDemo ASCII data files."
    )
    parser.add_argument(
        "--data-dir",
        type=Path,
        default=Path("app/data/ASCII"),
        help="Directory containing ASCII data files (default: app/data/ASCII/)",
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=Path("golden-files"),
        help="Directory for golden JSON output (default: golden-files/)",
    )
    args = parser.parse_args()

    print(f"Generating golden files from {args.data_dir} -> {args.output_dir}")
    print()

    summary = generate_all(args.data_dir, args.output_dir)

    print()
    ok_count = sum(1 for v in summary.values() if v["status"] == "OK")
    total_records = sum(v.get("records", 0) for v in summary.values())
    print(f"Done: {ok_count}/{len(summary)} files parsed, {total_records} total records")

    # Return non-zero if any errors
    if any(v["status"] == "ERROR" for v in summary.values()):
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
