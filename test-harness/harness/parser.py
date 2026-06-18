"""
Fixed-width COBOL data file parser based on copybook PIC clause definitions.

Reads ASCII data files and produces structured Python dicts (one per record)
using the field layout from the corresponding copybook.
"""

from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path
from typing import Any


@dataclass(frozen=True)
class FieldDef:
    """A single field extracted from a COBOL copybook PIC clause."""

    name: str
    pic: str
    offset: int
    length: int
    is_numeric: bool
    is_signed: bool
    decimal_places: int


# ── PIC clause helpers ──────────────────────────────────────────────────────

_PIC_RE = re.compile(
    r"PIC\s+"
    r"(S)?"                  # optional sign
    r"(9+|\d+\(\d+\))"      # integer part
    r"(?:V(9+|\d+\(\d+\)))?" # optional decimal (V99, V9(2), etc.)
    r"|"
    r"PIC\s+"
    r"(X)"                   # alphanumeric single char
    r"(?:\((\d+)\))?"        # optional repeat
    ,
    re.IGNORECASE,
)


def _expand_pic_digits(token: str) -> int:
    """Return the digit count for tokens like ``9(11)`` or ``999``."""
    m = re.match(r"(\d)\((\d+)\)", token)
    if m:
        return int(m.group(2))
    return len(token)


def pic_length(pic_clause: str) -> tuple[int, bool, bool, int]:
    """Parse a PIC clause and return (display_length, is_numeric, is_signed, decimals).

    Only handles display-format (no COMP/COMP-3).
    """
    pic = pic_clause.strip().upper()

    # Alphanumeric: PIC X, PIC X(n)
    m = re.match(r"X(?:\((\d+)\))?$", pic)
    if m:
        length = int(m.group(1)) if m.group(1) else 1
        return length, False, False, 0

    # Numeric: optional S, 9-digits, optional V + decimals
    signed = pic.startswith("S")
    rest = pic.lstrip("S")
    parts = rest.split("V", 1)
    int_digits = _expand_pic_digits(parts[0])
    dec_digits = _expand_pic_digits(parts[1]) if len(parts) > 1 else 0
    total = int_digits + dec_digits
    return total, True, signed, dec_digits


# ── Copybook layout registry ───────────────────────────────────────────────

def _fields(*specs: tuple[str, str]) -> list[FieldDef]:
    """Build a list of FieldDef from (name, pic_clause) pairs."""
    fields: list[FieldDef] = []
    offset = 0
    for name, pic in specs:
        length, is_num, is_signed, dec = pic_length(pic)
        fields.append(FieldDef(name, pic, offset, length, is_num, is_signed, dec))
        offset += length
    return fields


# CVACT01Y – Account Record (RECLN 300)
ACCOUNT_FIELDS = _fields(
    ("ACCT-ID", "9(11)"),
    ("ACCT-ACTIVE-STATUS", "X(01)"),
    ("ACCT-CURR-BAL", "S9(10)V99"),
    ("ACCT-CREDIT-LIMIT", "S9(10)V99"),
    ("ACCT-CASH-CREDIT-LIMIT", "S9(10)V99"),
    ("ACCT-OPEN-DATE", "X(10)"),
    ("ACCT-EXPIRAION-DATE", "X(10)"),
    ("ACCT-REISSUE-DATE", "X(10)"),
    ("ACCT-CURR-CYC-CREDIT", "S9(10)V99"),
    ("ACCT-CURR-CYC-DEBIT", "S9(10)V99"),
    ("ACCT-ADDR-ZIP", "X(10)"),
    ("ACCT-GROUP-ID", "X(10)"),
    ("FILLER", "X(178)"),
)

# CVACT02Y – Card Record (RECLN 150)
CARD_FIELDS = _fields(
    ("CARD-NUM", "X(16)"),
    ("CARD-ACCT-ID", "9(11)"),
    ("CARD-CVV-CD", "9(03)"),
    ("CARD-EMBOSSED-NAME", "X(50)"),
    ("CARD-EXPIRAION-DATE", "X(10)"),
    ("CARD-ACTIVE-STATUS", "X(01)"),
    ("FILLER", "X(59)"),
)

# CVACT03Y – Card Cross Reference (RECLN 50)
CARDXREF_FIELDS = _fields(
    ("XREF-CARD-NUM", "X(16)"),
    ("XREF-CUST-ID", "9(09)"),
    ("XREF-ACCT-ID", "9(11)"),
    ("FILLER", "X(14)"),
)

# CVCUS01Y – Customer Record (RECLN 500)
CUSTOMER_FIELDS = _fields(
    ("CUST-ID", "9(09)"),
    ("CUST-FIRST-NAME", "X(25)"),
    ("CUST-MIDDLE-NAME", "X(25)"),
    ("CUST-LAST-NAME", "X(25)"),
    ("CUST-ADDR-LINE-1", "X(50)"),
    ("CUST-ADDR-LINE-2", "X(50)"),
    ("CUST-ADDR-LINE-3", "X(50)"),
    ("CUST-ADDR-STATE-CD", "X(02)"),
    ("CUST-ADDR-COUNTRY-CD", "X(03)"),
    ("CUST-ADDR-ZIP", "X(10)"),
    ("CUST-PHONE-NUM-1", "X(15)"),
    ("CUST-PHONE-NUM-2", "X(15)"),
    ("CUST-SSN", "9(09)"),
    ("CUST-GOVT-ISSUED-ID", "X(20)"),
    ("CUST-DOB-YYYY-MM-DD", "X(10)"),
    ("CUST-EFT-ACCOUNT-ID", "X(10)"),
    ("CUST-PRI-CARD-HOLDER-IND", "X(01)"),
    ("CUST-FICO-CREDIT-SCORE", "9(03)"),
    ("FILLER", "X(168)"),
)

# CVTRA01Y – Transaction Category Balance (RECLN 50)
TCATBAL_FIELDS = _fields(
    ("TRANCAT-ACCT-ID", "9(11)"),
    ("TRANCAT-TYPE-CD", "X(02)"),
    ("TRANCAT-CD", "9(04)"),
    ("TRAN-CAT-BAL", "S9(09)V99"),
    ("FILLER", "X(22)"),
)

# CVTRA02Y – Disclosure Group (RECLN 50)
DISCGRP_FIELDS = _fields(
    ("DIS-ACCT-GROUP-ID", "X(10)"),
    ("DIS-TRAN-TYPE-CD", "X(02)"),
    ("DIS-TRAN-CAT-CD", "9(04)"),
    ("DIS-INT-RATE", "S9(04)V99"),
    ("FILLER", "X(28)"),
)

# CVTRA03Y – Transaction Type (RECLN 60)
TRANTYPE_FIELDS = _fields(
    ("TRAN-TYPE", "X(02)"),
    ("TRAN-TYPE-DESC", "X(50)"),
    ("FILLER", "X(08)"),
)

# CVTRA04Y – Transaction Category Type (RECLN 60)
TRANCATG_FIELDS = _fields(
    ("TRAN-TYPE-CD", "X(02)"),
    ("TRAN-CAT-CD", "9(04)"),
    ("TRAN-CAT-TYPE-DESC", "X(50)"),
    ("FILLER", "X(04)"),
)

# CVTRA05Y – Transaction Record (RECLN 350)
TRANSACTION_FIELDS = _fields(
    ("TRAN-ID", "X(16)"),
    ("TRAN-TYPE-CD", "X(02)"),
    ("TRAN-CAT-CD", "9(04)"),
    ("TRAN-SOURCE", "X(10)"),
    ("TRAN-DESC", "X(100)"),
    ("TRAN-AMT", "S9(09)V99"),
    ("TRAN-MERCHANT-ID", "9(09)"),
    ("TRAN-MERCHANT-NAME", "X(50)"),
    ("TRAN-MERCHANT-CITY", "X(50)"),
    ("TRAN-MERCHANT-ZIP", "X(10)"),
    ("TRAN-CARD-NUM", "X(16)"),
    ("TRAN-ORIG-TS", "X(26)"),
    ("TRAN-PROC-TS", "X(26)"),
    ("FILLER", "X(20)"),
)

# CVTRA06Y – Daily Transaction Record (RECLN 350)
DAILYTRAN_FIELDS = _fields(
    ("DALYTRAN-ID", "X(16)"),
    ("DALYTRAN-TYPE-CD", "X(02)"),
    ("DALYTRAN-CAT-CD", "9(04)"),
    ("DALYTRAN-SOURCE", "X(10)"),
    ("DALYTRAN-DESC", "X(100)"),
    ("DALYTRAN-AMT", "S9(09)V99"),
    ("DALYTRAN-MERCHANT-ID", "9(09)"),
    ("DALYTRAN-MERCHANT-NAME", "X(50)"),
    ("DALYTRAN-MERCHANT-CITY", "X(50)"),
    ("DALYTRAN-MERCHANT-ZIP", "X(10)"),
    ("DALYTRAN-CARD-NUM", "X(16)"),
    ("DALYTRAN-ORIG-TS", "X(26)"),
    ("DALYTRAN-PROC-TS", "X(26)"),
    ("FILLER", "X(20)"),
)

# CSUSR01Y – User Security (RECLN 80)
USRSEC_FIELDS = _fields(
    ("SEC-USR-ID", "X(08)"),
    ("SEC-USR-FNAME", "X(20)"),
    ("SEC-USR-LNAME", "X(20)"),
    ("SEC-USR-PWD", "X(08)"),
    ("SEC-USR-TYPE", "X(01)"),
    ("SEC-USR-FILLER", "X(23)"),
)


# Map from data-file base name to its field layout and expected record length.
LAYOUT_REGISTRY: dict[str, tuple[list[FieldDef], int]] = {
    "acctdata": (ACCOUNT_FIELDS, 300),
    "carddata": (CARD_FIELDS, 150),
    "cardxref": (CARDXREF_FIELDS, 50),
    "custdata": (CUSTOMER_FIELDS, 500),
    "dailytran": (DAILYTRAN_FIELDS, 350),
    "discgrp": (DISCGRP_FIELDS, 50),
    "tcatbal": (TCATBAL_FIELDS, 50),
    "trancatg": (TRANCATG_FIELDS, 60),
    "trantype": (TRANTYPE_FIELDS, 60),
}


# ── Zoned-decimal sign handling ─────────────────────────────────────────────

# In EBCDIC-to-ASCII converted files, the trailing byte of a signed numeric
# field encodes the sign.  Positive: {ABCDEFGHI → 0-9, Negative: }JKLMNOPQR → 0-9
_POS_SIGNS = "{ABCDEFGHI"  # { = +0, A = +1, … I = +9
_NEG_SIGNS = "}JKLMNOPQR"  # } = -0, J = -1, … R = -9


def decode_signed_numeric(raw: str) -> float:
    """Decode a zoned-decimal display-format signed numeric field.

    The last character encodes the sign:
      Positive: { A B C D E F G H I  →  0 1 2 3 4 5 6 7 8 9
      Negative: } J K L M N O P Q R  →  0 1 2 3 4 5 6 7 8 9
    """
    if not raw:
        return 0.0
    last = raw[-1]
    rest = raw[:-1]

    pos_idx = _POS_SIGNS.find(last)
    if pos_idx >= 0:
        return float(rest + str(pos_idx))

    neg_idx = _NEG_SIGNS.find(last)
    if neg_idx >= 0:
        return -float(rest + str(neg_idx))

    # Plain digit (unsigned treatment)
    return float(raw)


def parse_field(raw: str, field: FieldDef) -> Any:
    """Parse a single field value from its raw fixed-width string."""
    if not field.is_numeric:
        return raw.rstrip()

    if field.is_signed:
        int_value = decode_signed_numeric(raw)
    else:
        try:
            int_value = float(raw)
        except ValueError:
            return raw.rstrip()

    if field.decimal_places > 0:
        return round(int_value / (10 ** field.decimal_places), field.decimal_places)
    return int(int_value)


# ── File parser ─────────────────────────────────────────────────────────────

def parse_record(line: str, fields: list[FieldDef], record_length: int) -> dict[str, Any]:
    """Parse a single fixed-width record into a dict of field values."""
    # Pad to expected record length (trailing FILLER may be stripped in ASCII files)
    padded = line.ljust(record_length)
    record: dict[str, Any] = {}
    for f in fields:
        if f.name == "FILLER":
            continue
        raw = padded[f.offset : f.offset + f.length]
        record[f.name] = parse_field(raw, f)
    return record


def parse_file(
    filepath: str | Path,
    fields: list[FieldDef],
    record_length: int,
) -> list[dict[str, Any]]:
    """Parse a complete fixed-width data file into a list of record dicts."""
    path = Path(filepath)
    records: list[dict[str, Any]] = []
    with path.open("r", encoding="ascii", errors="replace") as fh:
        for line in fh:
            stripped = line.rstrip("\n").rstrip("\r")
            if not stripped:
                continue
            records.append(parse_record(stripped, fields, record_length))
    return records


def parse_by_name(filepath: str | Path) -> list[dict[str, Any]]:
    """Auto-detect the layout from the filename and parse the file."""
    path = Path(filepath)
    stem = path.stem.lower()
    if stem not in LAYOUT_REGISTRY:
        raise ValueError(
            f"Unknown data file '{stem}'. "
            f"Known layouts: {sorted(LAYOUT_REGISTRY.keys())}"
        )
    fields, reclen = LAYOUT_REGISTRY[stem]
    return parse_file(path, fields, reclen)
