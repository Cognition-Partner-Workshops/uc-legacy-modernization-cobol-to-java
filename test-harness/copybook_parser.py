"""
Copybook Parser for CardDemo COBOL-to-Java Migration Test Harness.

Parses fixed-width ASCII data files using COBOL copybook layout definitions.
Handles COBOL-specific data encodings:
  - Zoned decimal (PIC 9): unsigned integers
  - Signed zoned decimal (PIC S9 with overpunch): EBCDIC sign encoding in last byte
  - Alphanumeric (PIC X): text fields, trailing-space trimmed
  - Implied decimal (V): virtual decimal point (e.g., PIC S9(10)V99 = 12 digits, 2 decimal)
"""

import json
import re
from dataclasses import dataclass, field
from decimal import Decimal
from pathlib import Path
from typing import Any


# EBCDIC overpunch encoding for signed zoned decimals in ASCII representation.
# Last byte of a signed field encodes both the digit and the sign.
# Positive: { = +0, A = +1, B = +2, C = +3, D = +4, E = +5, F = +6, G = +7, H = +8, I = +9
# Negative: } = -0, J = -1, K = -2, L = -3, M = -4, N = -5, O = -6, P = -7, Q = -8, R = -9
OVERPUNCH_POSITIVE = {
    "{": "0", "A": "1", "B": "2", "C": "3", "D": "4",
    "E": "5", "F": "6", "G": "7", "H": "8", "I": "9",
}
OVERPUNCH_NEGATIVE = {
    "}": "0", "J": "1", "K": "2", "L": "3", "M": "4",
    "N": "5", "O": "6", "P": "7", "Q": "8", "R": "9",
}


@dataclass
class CopybookField:
    """A single field from a COBOL copybook layout."""
    name: str
    pic: str
    offset: int  # 0-based byte offset
    length: int  # total byte length
    field_type: str  # "alpha", "unsigned_int", "signed_decimal"
    integer_digits: int = 0
    decimal_digits: int = 0
    is_signed: bool = False
    is_filler: bool = False


@dataclass
class CopybookLayout:
    """Complete layout for a COBOL record."""
    name: str
    record_length: int
    fields: list = field(default_factory=list)


def parse_pic_clause(pic_str: str) -> dict:
    """Parse a COBOL PIC clause and return field metadata.

    Examples:
        PIC 9(11)       -> unsigned int, 11 digits
        PIC S9(10)V99   -> signed decimal, 10 integer + 2 decimal digits
        PIC X(50)       -> alphanumeric, 50 chars
        PIC S9(04)V99   -> signed decimal, 4 integer + 2 decimal digits
        PIC 9(03)       -> unsigned int, 3 digits
    """
    pic = pic_str.strip().upper().replace(" ", "")

    # Alphanumeric: PIC X(nn) or PIC X
    match = re.match(r"X\((\d+)\)", pic)
    if match:
        length = int(match.group(1))
        return {
            "field_type": "alpha",
            "length": length,
            "integer_digits": 0,
            "decimal_digits": 0,
            "is_signed": False,
        }
    if pic == "X":
        return {
            "field_type": "alpha",
            "length": 1,
            "integer_digits": 0,
            "decimal_digits": 0,
            "is_signed": False,
        }

    # Numeric: PIC [S]9(nn)[V][9(mm)|99]
    is_signed = pic.startswith("S")
    if is_signed:
        pic = pic[1:]

    integer_digits = 0
    decimal_digits = 0

    # Split on V (implied decimal point)
    parts = pic.split("V")
    integer_part = parts[0]
    decimal_part = parts[1] if len(parts) > 1 else ""

    # Parse integer part: 9(nn) or 9 or 99 etc.
    int_match = re.match(r"9\((\d+)\)", integer_part)
    if int_match:
        integer_digits = int(int_match.group(1))
    else:
        integer_digits = integer_part.count("9")

    # Parse decimal part: 9(mm) or 99 etc.
    if decimal_part:
        dec_match = re.match(r"9\((\d+)\)", decimal_part)
        if dec_match:
            decimal_digits = int(dec_match.group(1))
        else:
            decimal_digits = decimal_part.count("9")

    total_length = integer_digits + decimal_digits
    field_type = "signed_decimal" if is_signed or decimal_digits > 0 else "unsigned_int"

    return {
        "field_type": field_type,
        "length": total_length,
        "integer_digits": integer_digits,
        "decimal_digits": decimal_digits,
        "is_signed": is_signed,
    }


def decode_signed_value(raw: str, decimal_digits: int) -> str:
    """Decode a COBOL signed zoned decimal value with overpunch encoding.

    The last character encodes the sign and final digit.
    Returns a string representation of the decimal value.
    """
    if not raw:
        return "0"

    last_char = raw[-1]
    prefix = raw[:-1]

    if last_char in OVERPUNCH_POSITIVE:
        digit = OVERPUNCH_POSITIVE[last_char]
        sign = ""
    elif last_char in OVERPUNCH_NEGATIVE:
        digit = OVERPUNCH_NEGATIVE[last_char]
        sign = "-"
    elif last_char.isdigit():
        # No overpunch -- treat as positive
        digit = last_char
        sign = ""
    else:
        # Unknown encoding -- treat as zero
        digit = "0"
        sign = ""

    digits_str = prefix + digit

    # Remove leading zeros but keep at least one digit before decimal
    digits_str = digits_str.lstrip("0") or "0"

    if decimal_digits > 0:
        # Pad with leading zeros if needed for decimal placement
        full_digits = prefix + digit
        while len(full_digits) < decimal_digits + 1:
            full_digits = "0" + full_digits
        integer_part = full_digits[:-decimal_digits] or "0"
        decimal_part = full_digits[-decimal_digits:]
        return f"{sign}{integer_part.lstrip('0') or '0'}.{decimal_part}"
    else:
        return f"{sign}{digits_str}"


def decode_field_value(raw: str, fld: CopybookField) -> Any:
    """Decode a raw string value based on its copybook field definition."""
    if fld.is_filler:
        return None

    if fld.field_type == "alpha":
        return raw.rstrip()

    if fld.field_type == "unsigned_int":
        stripped = raw.strip()
        if not stripped or not stripped.isdigit():
            return 0
        return int(stripped)

    if fld.field_type == "signed_decimal":
        stripped = raw.strip()
        if not stripped:
            return "0.00" if fld.decimal_digits > 0 else "0"
        return decode_signed_value(stripped, fld.decimal_digits)

    return raw.rstrip()


# ---------------------------------------------------------------------------
# CardDemo Copybook Layout Definitions
# ---------------------------------------------------------------------------
# These are manually derived from the copybook PIC clauses in app/cpy/.
# Each layout specifies field name, PIC clause, byte offset, and length.


def _build_layout(name: str, record_length: int, field_defs: list) -> CopybookLayout:
    """Build a CopybookLayout from a list of (name, pic_string) tuples."""
    layout = CopybookLayout(name=name, record_length=record_length)
    offset = 0
    for field_name, pic_string in field_defs:
        parsed = parse_pic_clause(pic_string)
        fld = CopybookField(
            name=field_name,
            pic=pic_string,
            offset=offset,
            length=parsed["length"],
            field_type=parsed["field_type"],
            integer_digits=parsed["integer_digits"],
            decimal_digits=parsed["decimal_digits"],
            is_signed=parsed["is_signed"],
            is_filler=field_name.upper() == "FILLER",
        )
        layout.fields.append(fld)
        offset += parsed["length"]
    return layout


# CVACT01Y - Account Record (RECLN 300)
ACCOUNT_LAYOUT = _build_layout("ACCOUNT-RECORD", 300, [
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
])

# CVACT02Y - Card Record (RECLN 150)
CARD_LAYOUT = _build_layout("CARD-RECORD", 150, [
    ("CARD-NUM", "X(16)"),
    ("CARD-ACCT-ID", "9(11)"),
    ("CARD-CVV-CD", "9(03)"),
    ("CARD-EMBOSSED-NAME", "X(50)"),
    ("CARD-EXPIRAION-DATE", "X(10)"),
    ("CARD-ACTIVE-STATUS", "X(01)"),
    ("FILLER", "X(59)"),
])

# CVACT03Y - Card Cross-Reference (RECLN 50)
CARDXREF_LAYOUT = _build_layout("CARD-XREF-RECORD", 50, [
    ("XREF-CARD-NUM", "X(16)"),
    ("XREF-CUST-ID", "9(09)"),
    ("XREF-ACCT-ID", "9(11)"),
    ("FILLER", "X(14)"),
])

# CVCUS01Y - Customer Record (RECLN 500)
CUSTOMER_LAYOUT = _build_layout("CUSTOMER-RECORD", 500, [
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
])

# CVTRA05Y - Transaction Record (RECLN 350)
TRANSACTION_LAYOUT = _build_layout("TRAN-RECORD", 350, [
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
])

# CVTRA06Y - Daily Transaction Record (RECLN 350) -- same layout as CVTRA05Y
DAILY_TRANSACTION_LAYOUT = _build_layout("DALYTRAN-RECORD", 350, [
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
])

# CVTRA01Y - Transaction Category Balance (RECLN 50)
TRAN_CAT_BAL_LAYOUT = _build_layout("TRAN-CAT-BAL-RECORD", 50, [
    ("TRANCAT-ACCT-ID", "9(11)"),
    ("TRANCAT-TYPE-CD", "X(02)"),
    ("TRANCAT-CD", "9(04)"),
    ("TRAN-CAT-BAL", "S9(09)V99"),
    ("FILLER", "X(22)"),
])

# CVTRA02Y - Disclosure Group (RECLN 50)
DISCLOSURE_GROUP_LAYOUT = _build_layout("DIS-GROUP-RECORD", 50, [
    ("DIS-ACCT-GROUP-ID", "X(10)"),
    ("DIS-TRAN-TYPE-CD", "X(02)"),
    ("DIS-TRAN-CAT-CD", "9(04)"),
    ("DIS-INT-RATE", "S9(04)V99"),
    ("FILLER", "X(28)"),
])

# CVTRA03Y - Transaction Type (RECLN 60)
TRAN_TYPE_LAYOUT = _build_layout("TRAN-TYPE-RECORD", 60, [
    ("TRAN-TYPE", "X(02)"),
    ("TRAN-TYPE-DESC", "X(50)"),
    ("FILLER", "X(08)"),
])

# CVTRA04Y - Transaction Category (RECLN 60)
TRAN_CATEGORY_LAYOUT = _build_layout("TRAN-CAT-RECORD", 60, [
    ("TRAN-TYPE-CD", "X(02)"),
    ("TRAN-CAT-CD", "9(04)"),
    ("TRAN-CAT-TYPE-DESC", "X(50)"),
    ("FILLER", "X(04)"),
])

# CSUSR01Y - User Security Record (RECLN 80)
USER_SECURITY_LAYOUT = _build_layout("SEC-USER-DATA", 80, [
    ("SEC-USR-ID", "X(08)"),
    ("SEC-USR-FNAME", "X(20)"),
    ("SEC-USR-LNAME", "X(20)"),
    ("SEC-USR-PWD", "X(08)"),
    ("SEC-USR-TYPE", "X(01)"),
    ("FILLER", "X(23)"),
])


# Mapping from data file name to layout
LAYOUT_REGISTRY: dict[str, CopybookLayout] = {
    "acctdata": ACCOUNT_LAYOUT,
    "carddata": CARD_LAYOUT,
    "cardxref": CARDXREF_LAYOUT,
    "custdata": CUSTOMER_LAYOUT,
    "dailytran": DAILY_TRANSACTION_LAYOUT,
    "discgrp": DISCLOSURE_GROUP_LAYOUT,
    "tcatbal": TRAN_CAT_BAL_LAYOUT,
    "trancatg": TRAN_CATEGORY_LAYOUT,
    "trantype": TRAN_TYPE_LAYOUT,
}


def parse_record(line: str, layout: CopybookLayout) -> dict[str, Any]:
    """Parse a single fixed-width record line using the given layout.

    Returns a dict of field_name -> decoded_value, excluding FILLER fields.
    """
    record: dict[str, Any] = {}
    for fld in layout.fields:
        if fld.is_filler:
            continue
        raw = line[fld.offset:fld.offset + fld.length]
        record[fld.name] = decode_field_value(raw, fld)
    return record


def parse_file(file_path: str | Path, layout: CopybookLayout) -> list[dict[str, Any]]:
    """Parse an entire fixed-width data file using the given layout.

    Returns a list of parsed records (dicts).
    """
    file_path = Path(file_path)
    records = []
    with open(file_path, "r", encoding="ascii", errors="replace") as f:
        for line_num, line in enumerate(f, 1):
            # Strip newline but preserve record content
            line = line.rstrip("\n").rstrip("\r")
            if not line.strip():
                continue
            record = parse_record(line, layout)
            record["_line_number"] = line_num
            records.append(record)
    return records


def parse_file_to_json(file_path: str | Path, layout: CopybookLayout,
                       output_path: str | Path | None = None) -> str:
    """Parse a data file and return (or write) JSON output.

    Args:
        file_path: Path to the ASCII data file.
        layout: CopybookLayout to use for parsing.
        output_path: If provided, write JSON to this file.

    Returns:
        JSON string of parsed records.
    """
    records = parse_file(file_path, layout)

    # Build metadata
    metadata = {
        "source_file": str(file_path),
        "copybook": layout.name,
        "record_length": layout.record_length,
        "record_count": len(records),
        "fields": [
            {
                "name": f.name,
                "pic": f.pic,
                "offset": f.offset,
                "length": f.length,
                "type": f.field_type,
            }
            for f in layout.fields
            if not f.is_filler
        ],
    }

    output = {
        "_metadata": metadata,
        "records": records,
    }

    json_str = json.dumps(output, indent=2, default=str)

    if output_path:
        output_path = Path(output_path)
        output_path.parent.mkdir(parents=True, exist_ok=True)
        with open(output_path, "w", encoding="utf-8") as f:
            f.write(json_str)
            f.write("\n")

    return json_str
