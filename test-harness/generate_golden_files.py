#!/usr/bin/env python3
"""
Generate golden-file JSON from the ASCII data files in app/data/ASCII/.

Usage:
    python generate_golden_files.py [--data-dir ../app/data/ASCII] [--output-dir ../golden-files]
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

# Allow running from the test-harness directory
sys.path.insert(0, str(Path(__file__).resolve().parent))

from harness.parser import LAYOUT_REGISTRY, parse_file


def generate(data_dir: Path, output_dir: Path) -> None:
    output_dir.mkdir(parents=True, exist_ok=True)

    for stem, (fields, reclen) in sorted(LAYOUT_REGISTRY.items()):
        source = data_dir / f"{stem}.txt"
        if not source.exists():
            print(f"  SKIP {stem}.txt (not found)")
            continue

        records = parse_file(source, fields, reclen)
        out_path = output_dir / f"{stem}.json"
        with out_path.open("w", encoding="utf-8") as fh:
            json.dump(records, fh, indent=2, ensure_ascii=False)
        print(f"  {stem}.json — {len(records)} records")


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate golden-file JSON")
    parser.add_argument(
        "--data-dir",
        type=Path,
        default=Path(__file__).resolve().parent.parent / "app" / "data" / "ASCII",
        help="Path to ASCII data files",
    )
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=Path(__file__).resolve().parent.parent / "golden-files",
        help="Output directory for golden JSON files",
    )
    args = parser.parse_args()
    print(f"Data dir:   {args.data_dir}")
    print(f"Output dir: {args.output_dir}")
    generate(args.data_dir, args.output_dir)
    print("Done.")


if __name__ == "__main__":
    main()
