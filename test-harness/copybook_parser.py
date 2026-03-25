"""
Copybook-driven fixed-width record parser.

Parses ASCII data files using COBOL copybook field definitions to produce
structured Python dictionaries (serializable to JSON).

Handles:
- PIC 9(n)        -> integer
- PIC S9(n)V99    -> decimal (signed with zoned-decimal trailing sign)
- PIC S9(n)V99    -> decimal with overpunch sign encoding
- PIC X(n)        -> trimmed string
- FILLER          -> skipped
"""

import re
from decimal import Decimal, InvalidOperation
from typing import Any

# COBOL zoned-decimal overpunch sign map.
# The last byte of a signed numeric field encodes both the digit and the sign.
# '{' = +0, 'A'-'I' = +1 to +9, '}' = -0, 'J'-'R' = -1 to -9
OVERPUNCH_POSITIVE = {
    "{": "0",
    "A": "1",
    "B": "2",
    "C": "3",
    "D": "4",
    "E": "5",
    "F": "6",
    "G": "7",
    "H": "8",
    "I": "9",
}

OVERPUNCH_NEGATIVE = {
    "}": "0",
    "J": "1",
    "K": "2",
    "L": "3",
    "M": "4",
    "N": "5",
    "O": "6",
    "P": "7",
    "Q": "8",
    "R": "9",
}


class CopybookField:
    """A single field definition from a COBOL copybook."""

    def __init__(self, name: str, pic: str, length: int, decimals: int = 0,
                 signed: bool = False, field_type: str = "string"):
        self.name = name
        self.pic = pic
        self.length = length
        self.decimals = decimals
        self.signed = signed
        self.field_type = field_type  # "string", "integer", "decimal"

    def __repr__(self) -> str:
        return (f"CopybookField(name={self.name!r}, pic={self.pic!r}, "
                f"length={self.length}, type={self.field_type})")


def decode_overpunch(raw: str, decimals: int) -> str:
    """Decode a zoned-decimal field with trailing overpunch sign character.

    Returns a string representation of the signed decimal value.
    """
    if not raw:
        return "0"

    last_char = raw[-1]
    prefix = raw[:-1]

    sign = "+"
    digit = last_char

    if last_char in OVERPUNCH_POSITIVE:
        sign = "+"
        digit = OVERPUNCH_POSITIVE[last_char]
    elif last_char in OVERPUNCH_NEGATIVE:
        sign = "-"
        digit = OVERPUNCH_NEGATIVE[last_char]
    elif last_char.isdigit():
        sign = "+"
        digit = last_char
    else:
        # Unknown sign encoding; treat as positive zero
        sign = "+"
        digit = "0"

    numeric_str = prefix + digit

    # Remove leading zeros but keep at least one digit
    numeric_str = numeric_str.lstrip("0") or "0"

    if decimals > 0:
        # Insert decimal point
        # Pad with leading zeros if the numeric portion is shorter than decimals
        numeric_str = numeric_str.zfill(decimals + 1)
        integer_part = numeric_str[:-decimals]
        decimal_part = numeric_str[-decimals:]
        result = f"{integer_part}.{decimal_part}"
    else:
        result = numeric_str

    if sign == "-" and result != "0" and result != "0.00":
        result = "-" + result

    return result


def pic_to_field(name: str, pic: str) -> CopybookField:
    """Convert a COBOL PIC clause to a CopybookField definition."""
    pic_clean = pic.strip().upper().replace(" ", "")

    # Check for signed
    signed = pic_clean.startswith("S")
    if signed:
        pic_clean = pic_clean[1:]

    # PIC 9(n)V99 or PIC 9(n)V9(n)
    match_decimal = re.match(
        r"^9\((\d+)\)V(9+|9\((\d+)\))$", pic_clean
    )
    if match_decimal:
        integer_digits = int(match_decimal.group(1))
        if match_decimal.group(3):
            decimal_digits = int(match_decimal.group(3))
        else:
            decimal_digits = len(match_decimal.group(2))
        total_length = integer_digits + decimal_digits
        return CopybookField(
            name=name, pic=pic, length=total_length,
            decimals=decimal_digits, signed=signed, field_type="decimal"
        )

    # PIC 9(n)
    match_int = re.match(r"^9\((\d+)\)$", pic_clean)
    if match_int:
        length = int(match_int.group(1))
        return CopybookField(
            name=name, pic=pic, length=length,
            decimals=0, signed=False, field_type="integer"
        )

    # PIC X(n)
    match_str = re.match(r"^X\((\d+)\)$", pic_clean)
    if match_str:
        length = int(match_str.group(1))
        return CopybookField(
            name=name, pic=pic, length=length,
            decimals=0, signed=False, field_type="string"
        )

    # PIC 9(n) without parens (e.g., 999)
    match_raw_int = re.match(r"^(9+)$", pic_clean)
    if match_raw_int:
        length = len(match_raw_int.group(1))
        return CopybookField(
            name=name, pic=pic, length=length,
            decimals=0, signed=False, field_type="integer"
        )

    # PIC X(n) without parens (e.g., XX)
    match_raw_str = re.match(r"^(X+)$", pic_clean)
    if match_raw_str:
        length = len(match_raw_str.group(1))
        return CopybookField(
            name=name, pic=pic, length=length,
            decimals=0, signed=False, field_type="string"
        )

    raise ValueError(f"Unsupported PIC clause: {pic!r} for field {name!r}")


def parse_field_value(field: CopybookField, raw: str) -> Any:
    """Parse a raw fixed-width string into a typed Python value."""
    if field.field_type == "string":
        return raw.rstrip()

    if field.field_type == "integer":
        stripped = raw.strip()
        if not stripped:
            return 0
        try:
            return int(stripped)
        except ValueError:
            return 0

    if field.field_type == "decimal":
        stripped = raw.strip()
        if not stripped:
            return "0.00"
        if field.signed:
            return decode_overpunch(stripped, field.decimals)
        else:
            # Unsigned decimal: just insert decimal point
            numeric = stripped.lstrip("0") or "0"
            numeric = numeric.zfill(field.decimals + 1)
            integer_part = numeric[:-field.decimals]
            decimal_part = numeric[-field.decimals:]
            return f"{integer_part}.{decimal_part}"

    return raw


def parse_record(line: str, fields: list[CopybookField],
                 skip_filler: bool = True) -> dict[str, Any]:
    """Parse a single fixed-width record line into a dictionary.

    Args:
        line: The raw fixed-width text line.
        fields: Ordered list of CopybookField definitions.
        skip_filler: If True, fields named 'FILLER' are consumed but not
                     included in the output dictionary.

    Returns:
        Dictionary mapping field names to parsed values.
    """
    result = {}
    offset = 0

    for field in fields:
        if offset + field.length > len(line):
            # Pad short lines with spaces
            raw = line[offset:].ljust(field.length)
        else:
            raw = line[offset:offset + field.length]
        offset += field.length

        if skip_filler and field.name == "FILLER":
            continue

        result[field.name] = parse_field_value(field, raw)

    return result


def parse_file(filepath: str, fields: list[CopybookField],
               skip_filler: bool = True) -> list[dict[str, Any]]:
    """Parse an entire fixed-width data file into a list of record dicts.

    Args:
        filepath: Path to the ASCII data file.
        fields: Ordered list of CopybookField definitions.
        skip_filler: If True, FILLER fields are skipped in output.

    Returns:
        List of dictionaries, one per non-empty line in the file.
    """
    records = []
    with open(filepath, "r", encoding="utf-8") as f:
        for line_num, line in enumerate(f, 1):
            # Strip trailing newline/carriage-return but preserve spaces
            line = line.rstrip("\n").rstrip("\r")
            if not line.strip():
                continue
            record = parse_record(line, fields, skip_filler)
            record["_line_number"] = line_num
            records.append(record)
    return records


# ──────────────────────────────────────────────────────────────────────
# Pre-defined copybook layouts matching app/cpy/*.cpy
# ──────────────────────────────────────────────────────────────────────

ACCOUNT_FIELDS = [
    CopybookField("ACCT-ID", "9(11)", 11, field_type="integer"),
    CopybookField("ACCT-ACTIVE-STATUS", "X(01)", 1, field_type="string"),
    CopybookField("ACCT-CURR-BAL", "S9(10)V99", 12, decimals=2, signed=True, field_type="decimal"),
    CopybookField("ACCT-CREDIT-LIMIT", "S9(10)V99", 12, decimals=2, signed=True, field_type="decimal"),
    CopybookField("ACCT-CASH-CREDIT-LIMIT", "S9(10)V99", 12, decimals=2, signed=True, field_type="decimal"),
    CopybookField("ACCT-OPEN-DATE", "X(10)", 10, field_type="string"),
    CopybookField("ACCT-EXPIRATION-DATE", "X(10)", 10, field_type="string"),
    CopybookField("ACCT-REISSUE-DATE", "X(10)", 10, field_type="string"),
    CopybookField("ACCT-CURR-CYC-CREDIT", "S9(10)V99", 12, decimals=2, signed=True, field_type="decimal"),
    CopybookField("ACCT-CURR-CYC-DEBIT", "S9(10)V99", 12, decimals=2, signed=True, field_type="decimal"),
    CopybookField("ACCT-ADDR-ZIP", "X(10)", 10, field_type="string"),
    CopybookField("ACCT-GROUP-ID", "X(10)", 10, field_type="string"),
    CopybookField("FILLER", "X(178)", 178, field_type="string"),
]

CARD_FIELDS = [
    CopybookField("CARD-NUM", "X(16)", 16, field_type="string"),
    CopybookField("CARD-ACCT-ID", "9(11)", 11, field_type="integer"),
    CopybookField("CARD-CVV-CD", "9(03)", 3, field_type="integer"),
    CopybookField("CARD-EMBOSSED-NAME", "X(50)", 50, field_type="string"),
    CopybookField("CARD-EXPIRATION-DATE", "X(10)", 10, field_type="string"),
    CopybookField("CARD-ACTIVE-STATUS", "X(01)", 1, field_type="string"),
    CopybookField("FILLER", "X(59)", 59, field_type="string"),
]

CUSTOMER_FIELDS = [
    CopybookField("CUST-ID", "9(09)", 9, field_type="integer"),
    CopybookField("CUST-FIRST-NAME", "X(25)", 25, field_type="string"),
    CopybookField("CUST-MIDDLE-NAME", "X(25)", 25, field_type="string"),
    CopybookField("CUST-LAST-NAME", "X(25)", 25, field_type="string"),
    CopybookField("CUST-ADDR-LINE-1", "X(50)", 50, field_type="string"),
    CopybookField("CUST-ADDR-LINE-2", "X(50)", 50, field_type="string"),
    CopybookField("CUST-ADDR-LINE-3", "X(50)", 50, field_type="string"),
    CopybookField("CUST-ADDR-STATE-CD", "X(02)", 2, field_type="string"),
    CopybookField("CUST-ADDR-COUNTRY-CD", "X(03)", 3, field_type="string"),
    CopybookField("CUST-ADDR-ZIP", "X(10)", 10, field_type="string"),
    CopybookField("CUST-PHONE-NUM-1", "X(15)", 15, field_type="string"),
    CopybookField("CUST-PHONE-NUM-2", "X(15)", 15, field_type="string"),
    CopybookField("CUST-SSN", "9(09)", 9, field_type="integer"),
    CopybookField("CUST-GOVT-ISSUED-ID", "X(20)", 20, field_type="string"),
    CopybookField("CUST-DOB-YYYY-MM-DD", "X(10)", 10, field_type="string"),
    CopybookField("CUST-EFT-ACCOUNT-ID", "X(10)", 10, field_type="string"),
    CopybookField("CUST-PRI-CARD-HOLDER-IND", "X(01)", 1, field_type="string"),
    CopybookField("CUST-FICO-CREDIT-SCORE", "9(03)", 3, field_type="integer"),
    CopybookField("FILLER", "X(168)", 168, field_type="string"),
]

CARD_XREF_FIELDS = [
    CopybookField("XREF-CARD-NUM", "X(16)", 16, field_type="string"),
    CopybookField("XREF-CUST-ID", "9(09)", 9, field_type="integer"),
    CopybookField("XREF-ACCT-ID", "9(11)", 11, field_type="integer"),
    CopybookField("FILLER", "X(14)", 14, field_type="string"),
]

DAILY_TRAN_FIELDS = [
    CopybookField("DALYTRAN-ID", "X(16)", 16, field_type="string"),
    CopybookField("DALYTRAN-TYPE-CD", "X(02)", 2, field_type="string"),
    CopybookField("DALYTRAN-CAT-CD", "9(04)", 4, field_type="integer"),
    CopybookField("DALYTRAN-SOURCE", "X(10)", 10, field_type="string"),
    CopybookField("DALYTRAN-DESC", "X(100)", 100, field_type="string"),
    CopybookField("DALYTRAN-AMT", "S9(09)V99", 11, decimals=2, signed=True, field_type="decimal"),
    CopybookField("DALYTRAN-MERCHANT-ID", "9(09)", 9, field_type="integer"),
    CopybookField("DALYTRAN-MERCHANT-NAME", "X(50)", 50, field_type="string"),
    CopybookField("DALYTRAN-MERCHANT-CITY", "X(50)", 50, field_type="string"),
    CopybookField("DALYTRAN-MERCHANT-ZIP", "X(10)", 10, field_type="string"),
    CopybookField("DALYTRAN-CARD-NUM", "X(16)", 16, field_type="string"),
    CopybookField("DALYTRAN-ORIG-TS", "X(26)", 26, field_type="string"),
    CopybookField("DALYTRAN-PROC-TS", "X(26)", 26, field_type="string"),
    CopybookField("FILLER", "X(20)", 20, field_type="string"),
]

TRAN_TYPE_FIELDS = [
    CopybookField("TRAN-TYPE", "X(02)", 2, field_type="string"),
    CopybookField("TRAN-TYPE-DESC", "X(50)", 50, field_type="string"),
    CopybookField("FILLER", "X(08)", 8, field_type="string"),
]

TRAN_CAT_FIELDS = [
    CopybookField("TRAN-TYPE-CD", "X(02)", 2, field_type="string"),
    CopybookField("TRAN-CAT-CD", "9(04)", 4, field_type="integer"),
    CopybookField("TRAN-CAT-TYPE-DESC", "X(50)", 50, field_type="string"),
    CopybookField("FILLER", "X(04)", 4, field_type="string"),
]

TRAN_CAT_BAL_FIELDS = [
    CopybookField("TRANCAT-ACCT-ID", "9(11)", 11, field_type="integer"),
    CopybookField("TRANCAT-TYPE-CD", "X(02)", 2, field_type="string"),
    CopybookField("TRANCAT-CD", "9(04)", 4, field_type="integer"),
    CopybookField("TRAN-CAT-BAL", "S9(09)V99", 11, decimals=2, signed=True, field_type="decimal"),
    CopybookField("FILLER", "X(22)", 22, field_type="string"),
]

DISC_GROUP_FIELDS = [
    CopybookField("DIS-ACCT-GROUP-ID", "X(10)", 10, field_type="string"),
    CopybookField("DIS-TRAN-TYPE-CD", "X(02)", 2, field_type="string"),
    CopybookField("DIS-TRAN-CAT-CD", "9(04)", 4, field_type="integer"),
    CopybookField("DIS-INT-RATE", "S9(04)V99", 6, decimals=2, signed=True, field_type="decimal"),
    CopybookField("FILLER", "X(28)", 28, field_type="string"),
]

# Mapping of data file basenames to their field layouts
LAYOUT_REGISTRY: dict[str, list[CopybookField]] = {
    "acctdata": ACCOUNT_FIELDS,
    "carddata": CARD_FIELDS,
    "custdata": CUSTOMER_FIELDS,
    "cardxref": CARD_XREF_FIELDS,
    "dailytran": DAILY_TRAN_FIELDS,
    "trantype": TRAN_TYPE_FIELDS,
    "trancatg": TRAN_CAT_FIELDS,
    "tcatbal": TRAN_CAT_BAL_FIELDS,
    "discgrp": DISC_GROUP_FIELDS,
}
