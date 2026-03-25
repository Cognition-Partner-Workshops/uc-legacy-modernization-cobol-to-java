#!/usr/bin/env python3
"""
Generate golden-file JSON references from CardDemo ASCII data files.

Reads each fixed-width data file in app/data/ASCII/ using the corresponding
copybook layout definition and writes structured JSON to golden-files/.

Usage:
    python generate_golden_files.py [--data-dir PATH] [--output-dir PATH]
"""

import argparse
import json
import sys
from pathlib import Path

from copybook_parser import LAYOUT_REGISTRY, parse_file


def generate_golden_files(data_dir: Path, output_dir: Path) -> dict[str, int]:
    """Parse all ASCII data files and write golden JSON files.

    Returns a dict mapping file basenames to record counts.
    """
    output_dir.mkdir(parents=True, exist_ok=True)
    counts: dict[str, int] = {}

    for name, fields in LAYOUT_REGISTRY.items():
        src_path = data_dir / f"{name}.txt"
        if not src_path.exists():
            print(f"WARNING: Source file not found: {src_path}", file=sys.stderr)
            continue

        print(f"Parsing {src_path.name} ...", end=" ")
        records = parse_file(str(src_path), fields, skip_filler=True)
        counts[name] = len(records)
        print(f"{len(records)} records")

        golden = {
            "source_file": f"app/data/ASCII/{name}.txt",
            "copybook_layout": _layout_name(name),
            "record_count": len(records),
            "records": records,
        }

        dst_path = output_dir / f"{name}.json"
        with open(dst_path, "w", encoding="utf-8") as f:
            json.dump(golden, f, indent=2, default=str)
        print(f"  -> {dst_path}")

    return counts


def _layout_name(name: str) -> str:
    """Map data file name to copybook name for documentation."""
    mapping = {
        "acctdata": "CVACT01Y.cpy (ACCOUNT-RECORD, 300 bytes)",
        "carddata": "CVACT02Y.cpy (CARD-RECORD, 150 bytes)",
        "custdata": "CVCUS01Y.cpy (CUSTOMER-RECORD, 500 bytes)",
        "cardxref": "CVACT03Y.cpy (CARD-XREF-RECORD, 50 bytes)",
        "dailytran": "CVTRA06Y.cpy (DALYTRAN-RECORD, 350 bytes)",
        "trantype": "CVTRA03Y.cpy (TRAN-TYPE-RECORD, 60 bytes)",
        "trancatg": "CVTRA04Y.cpy (TRAN-CAT-RECORD, 60 bytes)",
        "tcatbal": "CVTRA01Y.cpy (TRAN-CAT-BAL-RECORD, 50 bytes)",
        "discgrp": "CVTRA02Y.cpy (DIS-GROUP-RECORD, 50 bytes)",
    }
    return mapping.get(name, "unknown")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Generate golden-file JSON from CardDemo ASCII data"
    )
    # Default paths relative to the repo root
    repo_root = Path(__file__).resolve().parent.parent
    parser.add_argument(
        "--data-dir",
        type=Path,
        default=repo_root / "app" / "data" / "ASCII",
        help="Path to ASCII data directory",
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=repo_root / "golden-files",
        help="Path to output golden-files directory",
    )
    args = parser.parse_args()

    print(f"Data directory: {args.data_dir}")
    print(f"Output directory: {args.output_dir}")
    print()

    counts = generate_golden_files(args.data_dir, args.output_dir)

    print()
    print("Summary:")
    print("-" * 40)
    total = 0
    for name, count in sorted(counts.items()):
        print(f"  {name:15s} {count:6d} records")
        total += count
    print(f"  {'TOTAL':15s} {total:6d} records")


if __name__ == "__main__":
    main()
