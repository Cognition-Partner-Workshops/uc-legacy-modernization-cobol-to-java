#!/usr/bin/env python3
"""Generate golden-file JSON references from ASCII data files.

Usage::

    python -m test-harness.generate_golden_files [--data-dir app/data/ASCII]
                                                  [--copybook-dir app/cpy]
                                                  [--output-dir golden-files]

For each registered dataset in ``COPYBOOK_REGISTRY``, this script:

1. Parses the copybook to obtain field descriptors.
2. Reads the fixed-width ASCII data file record-by-record.
3. Writes a pretty-printed JSON array to ``golden-files/<dataset>.json``.
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

_HERE = Path(__file__).resolve().parent

# Use relative imports so the script works regardless of whether the
# package directory is named ``test-harness`` (hyphen) or ``test_harness``.
sys.path.insert(0, str(_HERE))

from copybook_parser import COPYBOOK_REGISTRY, parse_copybook_file  # noqa: E402
from record_parser import parse_file  # noqa: E402


def generate(
    data_dir: Path,
    copybook_dir: Path,
    output_dir: Path,
) -> dict[str, int]:
    """Generate golden JSON for every registered dataset.

    Returns a dict mapping dataset name → record count written.
    """
    output_dir.mkdir(parents=True, exist_ok=True)
    stats: dict[str, int] = {}

    for filename, meta in COPYBOOK_REGISTRY.items():
        ds_name = filename.replace(".txt", "")
        data_path = data_dir / filename
        cpy_path = copybook_dir / meta["copybook"]

        if not data_path.exists():
            print(f"  SKIP {ds_name}: data file not found ({data_path})")
            continue
        if not cpy_path.exists():
            print(f"  SKIP {ds_name}: copybook not found ({cpy_path})")
            continue

        fields = parse_copybook_file(cpy_path)
        records = parse_file(data_path, fields, meta["recln"])

        out_path = output_dir / f"{ds_name}.json"
        out_path.write_text(
            json.dumps(records, indent=2, ensure_ascii=False, default=str),
            encoding="utf-8",
        )
        stats[ds_name] = len(records)
        print(f"  {ds_name}: {len(records)} records → {out_path}")

    return stats


def main() -> None:
    parser = argparse.ArgumentParser(description="Generate golden-file JSON references")
    parser.add_argument("--data-dir", default="app/data/ASCII", help="ASCII data directory")
    parser.add_argument("--copybook-dir", default="app/cpy", help="Copybook directory")
    parser.add_argument("--output-dir", default="golden-files", help="Output directory")
    args = parser.parse_args()

    # Resolve relative to repo root
    repo_root = _HERE.parent
    data_dir = (repo_root / args.data_dir).resolve()
    copybook_dir = (repo_root / args.copybook_dir).resolve()
    output_dir = (repo_root / args.output_dir).resolve()

    print(f"Data dir:     {data_dir}")
    print(f"Copybook dir: {copybook_dir}")
    print(f"Output dir:   {output_dir}")
    print()

    stats = generate(data_dir, copybook_dir, output_dir)

    print(f"\nDone. {sum(stats.values())} records across {len(stats)} datasets.")


if __name__ == "__main__":
    main()
