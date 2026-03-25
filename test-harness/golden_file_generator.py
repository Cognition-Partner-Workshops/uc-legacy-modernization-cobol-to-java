"""
golden_file_generator.py - Generate golden-file JSON from CardDemo ASCII data.

Reads each ASCII data file under app/data/ASCII/ using the corresponding
copybook layout and writes structured JSON to golden-files/.

Usage:
    python -m test-harness.golden_file_generator [--data-dir app/data/ASCII] [--out-dir golden-files]
"""

from __future__ import annotations

import json
import sys
from decimal import Decimal
from pathlib import Path

from .copybook_parser import FieldDef, build_layout, parse_file

# ---------------------------------------------------------------------------
# Copybook layouts  (field_name, pic_clause, byte_length)
#
# These mirror the copybook definitions in app/cpy/ exactly.
# ---------------------------------------------------------------------------

# CVACT01Y.cpy - ACCOUNT-RECORD (RECLN 300)
ACCOUNT_LAYOUT = build_layout([
    ("ACCT-ID",                "9(11)",       11),
    ("ACCT-ACTIVE-STATUS",     "X(01)",        1),
    ("ACCT-CURR-BAL",          "S9(10)V99",   12),
    ("ACCT-CREDIT-LIMIT",      "S9(10)V99",   12),
    ("ACCT-CASH-CREDIT-LIMIT", "S9(10)V99",   12),
    ("ACCT-OPEN-DATE",         "X(10)",       10),
    ("ACCT-EXPIRAION-DATE",    "X(10)",       10),
    ("ACCT-REISSUE-DATE",      "X(10)",       10),
    ("ACCT-CURR-CYC-CREDIT",   "S9(10)V99",   12),
    ("ACCT-CURR-CYC-DEBIT",    "S9(10)V99",   12),
    ("ACCT-ADDR-ZIP",          "X(10)",       10),
    ("ACCT-GROUP-ID",          "X(10)",       10),
    ("FILLER",                 "X(178)",     178),
])

# CVACT02Y.cpy - CARD-RECORD (RECLN 150)
CARD_LAYOUT = build_layout([
    ("CARD-NUM",              "X(16)",  16),
    ("CARD-ACCT-ID",          "9(11)",  11),
    ("CARD-CVV-CD",           "9(03)",   3),
    ("CARD-EMBOSSED-NAME",    "X(50)",  50),
    ("CARD-EXPIRAION-DATE",   "X(10)",  10),
    ("CARD-ACTIVE-STATUS",    "X(01)",   1),
    ("FILLER",                "X(59)",  59),
])

# CVCUS01Y.cpy - CUSTOMER-RECORD (RECLN 500)
CUSTOMER_LAYOUT = build_layout([
    ("CUST-ID",                   "9(09)",   9),
    ("CUST-FIRST-NAME",           "X(25)",  25),
    ("CUST-MIDDLE-NAME",          "X(25)",  25),
    ("CUST-LAST-NAME",            "X(25)",  25),
    ("CUST-ADDR-LINE-1",          "X(50)",  50),
    ("CUST-ADDR-LINE-2",          "X(50)",  50),
    ("CUST-ADDR-LINE-3",          "X(50)",  50),
    ("CUST-ADDR-STATE-CD",        "X(02)",   2),
    ("CUST-ADDR-COUNTRY-CD",      "X(03)",   3),
    ("CUST-ADDR-ZIP",             "X(10)",  10),
    ("CUST-PHONE-NUM-1",          "X(15)",  15),
    ("CUST-PHONE-NUM-2",          "X(15)",  15),
    ("CUST-SSN",                  "9(09)",   9),
    ("CUST-GOVT-ISSUED-ID",       "X(20)",  20),
    ("CUST-DOB-YYYY-MM-DD",       "X(10)",  10),
    ("CUST-EFT-ACCOUNT-ID",       "X(10)",  10),
    ("CUST-PRI-CARD-HOLDER-IND",  "X(01)",   1),
    ("CUST-FICO-CREDIT-SCORE",    "9(03)",   3),
    ("FILLER",                    "X(168)", 168),
])

# CVACT03Y.cpy - CARD-XREF-RECORD (RECLN 50)
CARDXREF_LAYOUT = build_layout([
    ("XREF-CARD-NUM",  "X(16)",  16),
    ("XREF-CUST-ID",   "9(09)",   9),
    ("XREF-ACCT-ID",   "9(11)",  11),
    ("FILLER",         "X(14)",  14),
])

# CVTRA05Y.cpy - TRAN-RECORD (RECLN 350)
TRANSACTION_LAYOUT = build_layout([
    ("TRAN-ID",            "X(16)",   16),
    ("TRAN-TYPE-CD",       "X(02)",    2),
    ("TRAN-CAT-CD",        "9(04)",    4),
    ("TRAN-SOURCE",        "X(10)",   10),
    ("TRAN-DESC",          "X(100)", 100),
    ("TRAN-AMT",           "S9(09)V99", 11),
    ("TRAN-MERCHANT-ID",   "9(09)",    9),
    ("TRAN-MERCHANT-NAME", "X(50)",   50),
    ("TRAN-MERCHANT-CITY", "X(50)",   50),
    ("TRAN-MERCHANT-ZIP",  "X(10)",   10),
    ("TRAN-CARD-NUM",      "X(16)",   16),
    ("TRAN-ORIG-TS",       "X(26)",   26),
    ("TRAN-PROC-TS",       "X(26)",   26),
    ("FILLER",             "X(20)",   20),
])

# CVTRA03Y.cpy - TRAN-TYPE-RECORD (RECLN 60)
TRANTYPE_LAYOUT = build_layout([
    ("TRAN-TYPE",       "X(02)",   2),
    ("TRAN-TYPE-DESC",  "X(50)",  50),
    ("FILLER",          "X(08)",   8),
])

# CVTRA04Y.cpy - TRAN-CAT-RECORD (RECLN 60)
TRANCATG_LAYOUT = build_layout([
    ("TRAN-TYPE-CD",       "X(02)",   2),
    ("TRAN-CAT-CD",        "9(04)",   4),
    ("TRAN-CAT-TYPE-DESC", "X(50)",  50),
    ("FILLER",             "X(04)",   4),
])

# CVTRA01Y.cpy - TRAN-CAT-BAL-RECORD (RECLN 50)
TCATBAL_LAYOUT = build_layout([
    ("TRANCAT-ACCT-ID",  "9(11)",      11),
    ("TRANCAT-TYPE-CD",  "X(02)",       2),
    ("TRANCAT-CD",       "9(04)",       4),
    ("TRAN-CAT-BAL",     "S9(09)V99",  11),
    ("FILLER",           "X(22)",      22),
])

# CVTRA02Y.cpy - DIS-GROUP-RECORD (RECLN 50)
DISCGRP_LAYOUT = build_layout([
    ("DIS-ACCT-GROUP-ID",  "X(10)",      10),
    ("DIS-TRAN-TYPE-CD",   "X(02)",       2),
    ("DIS-TRAN-CAT-CD",    "9(04)",       4),
    ("DIS-INT-RATE",       "S9(04)V99",   6),
    ("FILLER",             "X(28)",      28),
])

# ---------------------------------------------------------------------------
# File -> Layout mapping
# ---------------------------------------------------------------------------
FILE_LAYOUTS: dict[str, tuple[str, list[FieldDef]]] = {
    "acctdata.txt":  ("acctdata.json",  ACCOUNT_LAYOUT),
    "carddata.txt":  ("carddata.json",  CARD_LAYOUT),
    "custdata.txt":  ("custdata.json",  CUSTOMER_LAYOUT),
    "cardxref.txt":  ("cardxref.json",  CARDXREF_LAYOUT),
    "dailytran.txt": ("dailytran.json", TRANSACTION_LAYOUT),
    "trantype.txt":  ("trantype.json",  TRANTYPE_LAYOUT),
    "trancatg.txt":  ("trancatg.json",  TRANCATG_LAYOUT),
    "tcatbal.txt":   ("tcatbal.json",   TCATBAL_LAYOUT),
    "discgrp.txt":   ("discgrp.json",   DISCGRP_LAYOUT),
}


class _DecimalEncoder(json.JSONEncoder):
    """JSON encoder that serialises Decimal as string to preserve precision."""
    def default(self, o):
        if isinstance(o, Decimal):
            return str(o)
        return super().default(o)


def generate_golden_files(
    data_dir: str | Path = "app/data/ASCII",
    out_dir: str | Path = "golden-files",
) -> dict[str, int]:
    """Parse all ASCII data files and write golden JSON.

    Returns:
        A dict mapping output file names to record counts.
    """
    data_path = Path(data_dir)
    out_path = Path(out_dir)
    out_path.mkdir(parents=True, exist_ok=True)

    stats: dict[str, int] = {}

    for src_name, (dst_name, layout) in FILE_LAYOUTS.items():
        src_file = data_path / src_name
        if not src_file.exists():
            print(f"WARNING: {src_file} not found, skipping", file=sys.stderr)
            continue

        records = parse_file(src_file, layout)
        dst_file = out_path / dst_name

        with dst_file.open("w", encoding="utf-8") as f:
            json.dump(
                {"source": src_name, "record_count": len(records), "records": records},
                f,
                indent=2,
                cls=_DecimalEncoder,
            )

        stats[dst_name] = len(records)
        print(f"  {src_name} -> {dst_name}  ({len(records)} records)")

    return stats


# ---------------------------------------------------------------------------
# CLI entry point
# ---------------------------------------------------------------------------
if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="Generate golden-file JSON from CardDemo ASCII data")
    parser.add_argument("--data-dir", default="app/data/ASCII", help="Path to ASCII data files")
    parser.add_argument("--out-dir", default="golden-files", help="Output directory for golden JSON")
    args = parser.parse_args()

    print("Generating golden files...")
    stats = generate_golden_files(args.data_dir, args.out_dir)
    print(f"\nDone. Generated {len(stats)} golden files, {sum(stats.values())} total records.")
