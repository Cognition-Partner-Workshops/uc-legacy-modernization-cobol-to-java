#!/usr/bin/env python3
"""
Generate structured JSON golden-reference files from the CardDemo ASCII
data files by parsing each file with its corresponding COBOL copybook layout.

Usage:
    python generate_golden_files.py            # from test-harness/
    python test-harness/generate_golden_files.py  # from repo root

Output is written to ``golden-files/`` relative to the repository root.
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

# Ensure sibling modules are importable
_THIS_DIR = Path(__file__).resolve().parent
if str(_THIS_DIR) not in sys.path:
    sys.path.insert(0, str(_THIS_DIR))

from copybook_parser import parse_copybook, record_length  # noqa: E402
from record_parser import parse_file  # noqa: E402

REPO_ROOT = _THIS_DIR.parent
CPY_DIR = REPO_ROOT / "app" / "cpy"
DATA_DIR = REPO_ROOT / "app" / "data" / "ASCII"
OUT_DIR = REPO_ROOT / "golden-files"

# ---------------------------------------------------------------------------
# File ↔ Copybook mapping
# ---------------------------------------------------------------------------

FILE_MAP: list[dict] = [
    {
        "data_file": "acctdata.txt",
        "copybook": "CVACT01Y.cpy",
        "golden_file": "acctdata.json",
        "record_length": 300,
    },
    {
        "data_file": "carddata.txt",
        "copybook": "CVACT02Y.cpy",
        "golden_file": "carddata.json",
        "record_length": 150,
    },
    {
        "data_file": "custdata.txt",
        "copybook": "CVCUS01Y.cpy",
        "golden_file": "custdata.json",
        "record_length": 500,
    },
    {
        "data_file": "cardxref.txt",
        "copybook": "CVACT03Y.cpy",
        "golden_file": "cardxref.json",
        "record_length": 50,
    },
    {
        "data_file": "dailytran.txt",
        "copybook": "CVTRA06Y.cpy",
        "golden_file": "dailytran.json",
        "record_length": 350,
    },
    {
        "data_file": "trantype.txt",
        "copybook": "CVTRA03Y.cpy",
        "golden_file": "trantype.json",
        "record_length": 60,
    },
    {
        "data_file": "trancatg.txt",
        "copybook": "CVTRA04Y.cpy",
        "golden_file": "trancatg.json",
        "record_length": 60,
    },
    {
        "data_file": "tcatbal.txt",
        "copybook": "CVTRA01Y.cpy",
        "golden_file": "tcatbal.json",
        "record_length": 50,
    },
    {
        "data_file": "discgrp.txt",
        "copybook": "CVTRA02Y.cpy",
        "golden_file": "discgrp.json",
        "record_length": 50,
    },
]


def generate() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)

    all_ok = True
    for entry in FILE_MAP:
        cpy_path = CPY_DIR / entry["copybook"]
        data_path = DATA_DIR / entry["data_file"]
        out_path = OUT_DIR / entry["golden_file"]
        expected_reclen = entry["record_length"]

        if not cpy_path.exists():
            print(f"ERROR: copybook not found: {cpy_path}", file=sys.stderr)
            all_ok = False
            continue
        if not data_path.exists():
            print(f"ERROR: data file not found: {data_path}", file=sys.stderr)
            all_ok = False
            continue

        fields = parse_copybook(cpy_path)
        parsed_reclen = record_length(fields)

        print(f"{entry['data_file']:20s}  copybook fields={len(fields):3d}  "
              f"parsed_reclen={parsed_reclen:4d}  expected={expected_reclen:4d}")

        records = parse_file(
            str(data_path),
            fields,
            expected_length=expected_reclen,
        )

        # Write golden JSON
        with open(out_path, "w") as fp:
            json.dump(
                {
                    "source_file": entry["data_file"],
                    "copybook": entry["copybook"],
                    "record_length": expected_reclen,
                    "record_count": len(records),
                    "fields": [
                        {
                            "name": f["name"],
                            "type": f["type"],
                            "offset": f["offset"],
                            "length": f["length"],
                            "scale": f["scale"],
                        }
                        for f in fields
                    ],
                    "records": records,
                },
                fp,
                indent=2,
            )

        print(f"  -> {out_path.relative_to(REPO_ROOT)}  ({len(records)} records)")

    if not all_ok:
        sys.exit(1)


if __name__ == "__main__":
    generate()
