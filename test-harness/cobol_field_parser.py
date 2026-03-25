#!/usr/bin/env python3
"""
Copybook-aware fixed-width field parser for CardDemo ASCII data files.

Reads fixed-width records according to COBOL copybook field definitions
(PIC clauses) and produces structured Python dictionaries (serialisable to JSON).

COBOL zoned-decimal sign encoding (overpunch):
  The last byte of a signed numeric field uses a zone-punch character to
  encode the sign.  In ASCII files exported from EBCDIC:
    Positive: { 0  A 1  B 2  C 3  D 4  E 5  F 6  G 7  H 8  I 9
    Negative: } 0  J 1  K 2  L 3  M 4  N 5  O 6  P 7  Q 8  R 9
"""

from __future__ import annotations

import json
import os
import re
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

# ---------------------------------------------------------------------------
# Overpunch decoding tables
# ---------------------------------------------------------------------------
POSITIVE_OVERPUNCH = {
    "{": "0", "A": "1", "B": "2", "C": "3", "D": "4",
    "E": "5", "F": "6", "G": "7", "H": "8", "I": "9",
}
NEGATIVE_OVERPUNCH = {
    "}": "0", "J": "1", "K": "2", "L": "3", "M": "4",
    "N": "5", "O": "6", "P": "7", "Q": "8", "R": "9",
}


def decode_overpunch(raw: str) -> str:
    """Decode a zoned-decimal overpunch character in the last position."""
    if not raw:
        return "0"
    last = raw[-1]
    prefix = raw[:-1]
    if last in POSITIVE_OVERPUNCH:
        return prefix + POSITIVE_OVERPUNCH[last]
    if last in NEGATIVE_OVERPUNCH:
        return "-" + prefix + NEGATIVE_OVERPUNCH[last]
    # No overpunch -- treat as plain digits
    return raw


# ---------------------------------------------------------------------------
# Field definition
# ---------------------------------------------------------------------------
@dataclass
class FieldDef:
    """A single copybook field definition."""
    name: str
    pic: str          # e.g. "9(11)", "X(10)", "S9(10)V99"
    length: int       # byte width in the fixed-width record
    offset: int = 0   # computed byte offset from start of record
    signed: bool = False
    decimal_places: int = 0
    field_type: str = "alpha"  # "alpha", "numeric", "signed_numeric"


def parse_pic_clause(pic: str) -> tuple[str, int, bool, int]:
    """
    Parse a COBOL PIC clause and return (field_type, length, signed, decimal_places).
    
    Examples:
        PIC 9(11)          -> ("numeric", 11, False, 0)
        PIC X(10)          -> ("alpha", 10, False, 0)
        PIC S9(10)V99      -> ("signed_numeric", 12, True, 2)
        PIC S9(09)V99      -> ("signed_numeric", 11, True, 2)
        PIC X(01)          -> ("alpha", 1, False, 0)
        PIC S9(04)V99      -> ("signed_numeric", 6, True, 2)
    """
    signed = pic.startswith("S")
    clean = pic.lstrip("S")

    # Split on V (implied decimal point)
    decimal_places = 0
    if "V" in clean:
        integer_part, decimal_part = clean.split("V", 1)
        decimal_places = _count_digits(decimal_part)
    else:
        integer_part = clean
        decimal_part = ""

    integer_len = _count_digits(integer_part) if "9" in integer_part else _count_chars(integer_part)

    total_length = integer_len + decimal_places
    if "X" in integer_part:
        field_type = "alpha"
    elif signed:
        field_type = "signed_numeric"
    else:
        field_type = "numeric"

    return field_type, total_length, signed, decimal_places


def _count_digits(part: str) -> int:
    """Count the number of digit positions in a PIC part like '9(11)' or '99'."""
    m = re.match(r"9\((\d+)\)", part)
    if m:
        return int(m.group(1))
    return part.count("9")


def _count_chars(part: str) -> int:
    """Count the number of character positions in a PIC part like 'X(10)' or 'XX'."""
    m = re.match(r"X\((\d+)\)", part)
    if m:
        return int(m.group(1))
    return part.count("X")


# ---------------------------------------------------------------------------
# Record layout registry  (copybook -> field definitions)
# ---------------------------------------------------------------------------
def _build_layout(fields: list[tuple[str, str]]) -> list[FieldDef]:
    """Build a list of FieldDef with computed offsets from (name, pic) pairs."""
    result: list[FieldDef] = []
    offset = 0
    for name, pic in fields:
        ftype, length, signed, decimals = parse_pic_clause(pic)
        result.append(FieldDef(
            name=name, pic=pic, length=length, offset=offset,
            signed=signed, decimal_places=decimals, field_type=ftype,
        ))
        offset += length
    return result


# -- CVACT01Y  Account record (300 bytes) ----------------------------------
ACCOUNT_LAYOUT = _build_layout([
    ("ACCT-ID",                "9(11)"),
    ("ACCT-ACTIVE-STATUS",     "X(01)"),
    ("ACCT-CURR-BAL",          "S9(10)V99"),
    ("ACCT-CREDIT-LIMIT",      "S9(10)V99"),
    ("ACCT-CASH-CREDIT-LIMIT", "S9(10)V99"),
    ("ACCT-OPEN-DATE",         "X(10)"),
    ("ACCT-EXPIRAION-DATE",    "X(10)"),
    ("ACCT-REISSUE-DATE",      "X(10)"),
    ("ACCT-CURR-CYC-CREDIT",   "S9(10)V99"),
    ("ACCT-CURR-CYC-DEBIT",    "S9(10)V99"),
    ("ACCT-ADDR-ZIP",          "X(10)"),
    ("ACCT-GROUP-ID",          "X(10)"),
    ("FILLER",                 "X(178)"),
])

# -- CVACT02Y  Card record (150 bytes) -------------------------------------
CARD_LAYOUT = _build_layout([
    ("CARD-NUM",              "X(16)"),
    ("CARD-ACCT-ID",          "9(11)"),
    ("CARD-CVV-CD",           "9(03)"),
    ("CARD-EMBOSSED-NAME",    "X(50)"),
    ("CARD-EXPIRAION-DATE",   "X(10)"),
    ("CARD-ACTIVE-STATUS",    "X(01)"),
    ("FILLER",                "X(59)"),
])

# -- CVCUS01Y  Customer record (500 bytes) ---------------------------------
CUSTOMER_LAYOUT = _build_layout([
    ("CUST-ID",                   "9(09)"),
    ("CUST-FIRST-NAME",           "X(25)"),
    ("CUST-MIDDLE-NAME",          "X(25)"),
    ("CUST-LAST-NAME",            "X(25)"),
    ("CUST-ADDR-LINE-1",          "X(50)"),
    ("CUST-ADDR-LINE-2",          "X(50)"),
    ("CUST-ADDR-LINE-3",          "X(50)"),
    ("CUST-ADDR-STATE-CD",        "X(02)"),
    ("CUST-ADDR-COUNTRY-CD",      "X(03)"),
    ("CUST-ADDR-ZIP",             "X(10)"),
    ("CUST-PHONE-NUM-1",          "X(15)"),
    ("CUST-PHONE-NUM-2",          "X(15)"),
    ("CUST-SSN",                  "9(09)"),
    ("CUST-GOVT-ISSUED-ID",       "X(20)"),
    ("CUST-DOB-YYYY-MM-DD",       "X(10)"),
    ("CUST-EFT-ACCOUNT-ID",       "X(10)"),
    ("CUST-PRI-CARD-HOLDER-IND",  "X(01)"),
    ("CUST-FICO-CREDIT-SCORE",    "9(03)"),
    ("FILLER",                    "X(168)"),
])

# -- CVACT03Y  Card cross-reference (50 bytes) -----------------------------
CARD_XREF_LAYOUT = _build_layout([
    ("XREF-CARD-NUM",  "X(16)"),
    ("XREF-CUST-ID",   "9(09)"),
    ("XREF-ACCT-ID",   "9(11)"),
    ("FILLER",         "X(14)"),
])

# -- CVTRA05Y  Transaction record (350 bytes) ------------------------------
TRAN_LAYOUT = _build_layout([
    ("TRAN-ID",             "X(16)"),
    ("TRAN-TYPE-CD",        "X(02)"),
    ("TRAN-CAT-CD",         "9(04)"),
    ("TRAN-SOURCE",         "X(10)"),
    ("TRAN-DESC",           "X(100)"),
    ("TRAN-AMT",            "S9(09)V99"),
    ("TRAN-MERCHANT-ID",    "9(09)"),
    ("TRAN-MERCHANT-NAME",  "X(50)"),
    ("TRAN-MERCHANT-CITY",  "X(50)"),
    ("TRAN-MERCHANT-ZIP",   "X(10)"),
    ("TRAN-CARD-NUM",       "X(16)"),
    ("TRAN-ORIG-TS",        "X(26)"),
    ("TRAN-PROC-TS",        "X(26)"),
    ("FILLER",              "X(20)"),
])

# -- CVTRA06Y  Daily transaction record (350 bytes) ------------------------
DAILY_TRAN_LAYOUT = _build_layout([
    ("DALYTRAN-ID",             "X(16)"),
    ("DALYTRAN-TYPE-CD",        "X(02)"),
    ("DALYTRAN-CAT-CD",         "9(04)"),
    ("DALYTRAN-SOURCE",         "X(10)"),
    ("DALYTRAN-DESC",           "X(100)"),
    ("DALYTRAN-AMT",            "S9(09)V99"),
    ("DALYTRAN-MERCHANT-ID",    "9(09)"),
    ("DALYTRAN-MERCHANT-NAME",  "X(50)"),
    ("DALYTRAN-MERCHANT-CITY",  "X(50)"),
    ("DALYTRAN-MERCHANT-ZIP",   "X(10)"),
    ("DALYTRAN-CARD-NUM",       "X(16)"),
    ("DALYTRAN-ORIG-TS",        "X(26)"),
    ("DALYTRAN-PROC-TS",        "X(26)"),
    ("FILLER",                  "X(20)"),
])

# -- CVTRA03Y  Transaction type (60 bytes) ---------------------------------
TRAN_TYPE_LAYOUT = _build_layout([
    ("TRAN-TYPE",       "X(02)"),
    ("TRAN-TYPE-DESC",  "X(50)"),
    ("FILLER",          "X(08)"),
])

# -- CVTRA04Y  Transaction category (60 bytes) -----------------------------
TRAN_CAT_LAYOUT = _build_layout([
    ("TRAN-TYPE-CD",        "X(02)"),
    ("TRAN-CAT-CD",         "9(04)"),
    ("TRAN-CAT-TYPE-DESC",  "X(50)"),
    ("FILLER",              "X(04)"),
])

# -- CVTRA02Y  Disclosure group (50 bytes) ---------------------------------
DIS_GROUP_LAYOUT = _build_layout([
    ("DIS-ACCT-GROUP-ID",  "X(10)"),
    ("DIS-TRAN-TYPE-CD",   "X(02)"),
    ("DIS-TRAN-CAT-CD",    "9(04)"),
    ("DIS-INT-RATE",       "S9(04)V99"),
    ("FILLER",             "X(28)"),
])

# -- CVTRA01Y  Transaction category balance (50 bytes) ---------------------
TRAN_CAT_BAL_LAYOUT = _build_layout([
    ("TRANCAT-ACCT-ID",   "9(11)"),
    ("TRANCAT-TYPE-CD",   "X(02)"),
    ("TRANCAT-CD",        "9(04)"),
    ("TRAN-CAT-BAL",      "S9(09)V99"),
    ("FILLER",            "X(22)"),
])

# -- CSUSR01Y  Security user data (80 bytes) -------------------------------
SEC_USER_LAYOUT = _build_layout([
    ("SEC-USR-ID",     "X(08)"),
    ("SEC-USR-FNAME",  "X(20)"),
    ("SEC-USR-LNAME",  "X(20)"),
    ("SEC-USR-PWD",    "X(08)"),
    ("SEC-USR-TYPE",   "X(01)"),
    ("SEC-USR-FILLER", "X(23)"),
])


# ---------------------------------------------------------------------------
# File-to-layout mapping
# ---------------------------------------------------------------------------
FILE_LAYOUTS: dict[str, list[FieldDef]] = {
    "acctdata.txt":  ACCOUNT_LAYOUT,
    "carddata.txt":  CARD_LAYOUT,
    "custdata.txt":  CUSTOMER_LAYOUT,
    "cardxref.txt":  CARD_XREF_LAYOUT,
    "dailytran.txt": DAILY_TRAN_LAYOUT,
    "discgrp.txt":   DIS_GROUP_LAYOUT,
    "tcatbal.txt":   TRAN_CAT_BAL_LAYOUT,
    "trancatg.txt":  TRAN_CAT_LAYOUT,
    "trantype.txt":  TRAN_TYPE_LAYOUT,
}


# ---------------------------------------------------------------------------
# Parsing logic
# ---------------------------------------------------------------------------
def parse_field(raw: str, fdef: FieldDef) -> Any:
    """Parse a single raw field value according to its FieldDef."""
    if fdef.name == "FILLER":
        return None  # skip filler

    if fdef.field_type == "alpha":
        return raw.rstrip()

    if fdef.field_type == "numeric":
        cleaned = raw.strip()
        if not cleaned:
            return 0
        try:
            return int(cleaned)
        except ValueError:
            return 0

    if fdef.field_type == "signed_numeric":
        decoded = decode_overpunch(raw.strip())
        if not decoded or decoded == "-":
            return 0.0
        try:
            int_val = int(decoded)
        except ValueError:
            return 0.0
        if fdef.decimal_places > 0:
            return int_val / (10 ** fdef.decimal_places)
        return int_val

    return raw.rstrip()


def parse_record(line: str, layout: list[FieldDef]) -> dict[str, Any]:
    """Parse a single fixed-width line into a dict using the given layout."""
    record: dict[str, Any] = {}
    for fdef in layout:
        raw = line[fdef.offset:fdef.offset + fdef.length]
        if fdef.name == "FILLER":
            continue
        record[fdef.name] = parse_field(raw, fdef)
    return record


def parse_file(filepath: str | Path, layout: list[FieldDef]) -> list[dict[str, Any]]:
    """Parse an entire fixed-width data file into a list of record dicts."""
    records: list[dict[str, Any]] = []
    with open(filepath, "r", encoding="utf-8") as fh:
        for line in fh:
            stripped = line.rstrip("\n\r")
            if not stripped:
                continue
            records.append(parse_record(stripped, layout))
    return records


# ---------------------------------------------------------------------------
# Golden-file generation
# ---------------------------------------------------------------------------
def generate_golden_files(
    data_dir: str | Path,
    output_dir: str | Path,
) -> dict[str, int]:
    """
    Parse all known ASCII data files and write JSON golden references.
    
    Returns a dict mapping output filename to record count.
    """
    data_dir = Path(data_dir)
    output_dir = Path(output_dir)
    output_dir.mkdir(parents=True, exist_ok=True)

    summary: dict[str, int] = {}
    for filename, layout in FILE_LAYOUTS.items():
        src = data_dir / filename
        if not src.exists():
            print(f"  SKIP  {filename} (not found)")
            continue
        records = parse_file(src, layout)
        out_name = src.stem + ".json"
        out_path = output_dir / out_name
        with open(out_path, "w", encoding="utf-8") as fh:
            json.dump(
                {"_source": filename, "_record_count": len(records), "records": records},
                fh,
                indent=2,
                ensure_ascii=False,
            )
        summary[out_name] = len(records)
        print(f"  OK    {filename} -> {out_name}  ({len(records)} records)")

    return summary


# ---------------------------------------------------------------------------
# CLI entry point
# ---------------------------------------------------------------------------
def main() -> None:
    repo_root = Path(__file__).resolve().parent.parent
    data_dir = repo_root / "app" / "data" / "ASCII"
    golden_dir = repo_root / "golden-files"

    print("Generating golden reference files ...")
    summary = generate_golden_files(data_dir, golden_dir)
    print(f"\nDone. {len(summary)} files generated, {sum(summary.values())} total records.")


if __name__ == "__main__":
    main()
