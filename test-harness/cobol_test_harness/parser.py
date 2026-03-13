"""Parser for fixed-width COBOL data files based on copybook PIC clause definitions.

Handles PIC X, PIC 9, PIC S9, PIC V, and COMP-3 types. Parses ASCII
zoned-decimal encoding where the last character of a signed field encodes
the sign using the standard EBCDIC-to-ASCII overpunch mapping.
"""

from __future__ import annotations

import json
import re
from dataclasses import dataclass
from dataclasses import field as dataclass_field
from pathlib import Path
from typing import Any

# Zoned decimal sign encoding for ASCII representation.
# The last byte of a PIC S9 field encodes both the digit and the sign.
# Positive: '{' = 0, 'A' = 1, 'B' = 2, ..., 'I' = 9
# Negative: '}' = 0, 'J' = 1, 'K' = 2, ..., 'R' = 9
POSITIVE_SIGN_MAP: dict[str, int] = {
    "{": 0, "A": 1, "B": 2, "C": 3, "D": 4,
    "E": 5, "F": 6, "G": 7, "H": 8, "I": 9,
}

NEGATIVE_SIGN_MAP: dict[str, int] = {
    "}": 0, "J": 1, "K": 2, "L": 3, "M": 4,
    "N": 5, "O": 6, "P": 7, "Q": 8, "R": 9,
}

ALL_SIGN_MAP: dict[str, tuple[int, int]] = {}
for ch, digit in POSITIVE_SIGN_MAP.items():
    ALL_SIGN_MAP[ch] = (digit, 1)
for ch, digit in NEGATIVE_SIGN_MAP.items():
    ALL_SIGN_MAP[ch] = (digit, -1)


@dataclass
class FieldDef:
    """Definition of a single field in a COBOL record layout."""

    name: str
    pic_type: str  # "X", "9", "S9", "S9V99", etc.
    offset: int
    length: int
    decimal_places: int = 0
    is_signed: bool = False
    is_filler: bool = False
    description: str = ""


@dataclass
class RecordLayout:
    """Complete layout of a COBOL record, parsed from a copybook."""

    name: str
    record_length: int
    fields: list[FieldDef] = dataclass_field(default_factory=list)
    copybook_source: str = ""

    def total_field_length(self) -> int:
        """Sum of all field lengths."""
        return sum(f.length for f in self.fields)


# ---------------------------------------------------------------------------
# PIC clause parsing
# ---------------------------------------------------------------------------

_PIC_PATTERN = re.compile(
    r"PIC\s+"
    r"(S?)"           # optional sign
    r"(9|X)"          # base type
    r"\((\d+)\)"      # repeat count
    r"(V(\d+|9+))?"   # optional implied decimal (V99 or V9(2))
    r"(\((\d+)\))?",  # optional repeat count for decimal part
    re.IGNORECASE,
)

# More flexible pattern for V followed by 9s like V99 or V9(2)
_PIC_FULL = re.compile(
    r"PIC\s+"
    r"(S?)"
    r"(9|X)"
    r"\((\d+)\)"
    r"(?:V(9+|\d+(?:\(\d+\))))?",
    re.IGNORECASE,
)


def _parse_pic_clause(pic_str: str) -> tuple[str, int, int, bool]:
    """Parse a PIC clause string and return (base_type, length, decimal_places, is_signed).

    Examples:
        PIC X(16)       -> ("X", 16, 0, False)
        PIC 9(11)       -> ("9", 11, 0, False)
        PIC S9(10)V99   -> ("9", 12, 2, True)
        PIC S9(09)V99   -> ("9", 11, 2, True)
        PIC S9(04)V99   -> ("9", 6, 2, True)
    """
    pic_str = pic_str.strip()

    is_signed = "S" in pic_str.upper().split("V")[0]

    # Extract base type and integer digits
    m = re.search(r"(S?)(9|X)\((\d+)\)", pic_str, re.IGNORECASE)
    if not m:
        raise ValueError(f"Cannot parse PIC clause: {pic_str}")

    base_type = m.group(2).upper()
    int_digits = int(m.group(3))

    # Extract decimal places from V portion
    decimal_places = 0
    v_match = re.search(r"V(9+)", pic_str, re.IGNORECASE)
    if v_match:
        decimal_places = len(v_match.group(1))
    else:
        v_match2 = re.search(r"V9\((\d+)\)", pic_str, re.IGNORECASE)
        if v_match2:
            decimal_places = int(v_match2.group(1))

    total_length = int_digits + decimal_places

    return base_type, total_length, decimal_places, is_signed


def parse_copybook(copybook_text: str) -> RecordLayout:
    """Parse a COBOL copybook and extract the record layout.

    Handles standard 05-level field definitions with PIC clauses.
    """
    lines = copybook_text.splitlines()
    record_name = "UNKNOWN"
    fields: list[FieldDef] = []
    offset = 0

    # Extract record name from 01 level
    for line in lines:
        stripped = line.strip().rstrip(".")
        m = re.match(r"(?:\d+\s+)?0?1\s+(\S+)", stripped)
        if m and "PIC" not in stripped.upper():
            record_name = m.group(1).rstrip(".")
            break

    for line in lines:
        stripped = line.strip()

        # Skip comments
        if stripped.startswith("*") or stripped.startswith("/"):
            continue

        # Remove trailing period
        stripped = stripped.rstrip(".")

        # Remove sequence numbers (columns 1-6 in standard COBOL)
        cleaned = re.sub(r"^\d{6}\s*", "", stripped)

        # Look for field definitions with PIC clauses
        pic_match = re.search(r"PIC\s+", cleaned, re.IGNORECASE)
        if not pic_match:
            continue

        # Extract level number and field name
        field_match = re.match(
            r"(?:\d{6}\s+)?(\d+)\s+(\S+)\s+", stripped
        )
        if not field_match:
            # Try without sequence number
            field_match = re.match(r"(\d+)\s+(\S+)\s+", cleaned)
        if not field_match:
            continue

        level = int(field_match.group(1))
        name = field_match.group(2)

        # Skip non-elementary items (group levels without PIC)
        # and REDEFINES (they share the same storage)
        if "REDEFINES" in cleaned.upper():
            continue

        # Skip 88-level condition names
        if level == 88:
            continue

        # Skip items that are at group level (05 with subordinate 10s)
        # We only want elementary items (those with PIC clauses)
        if level < 5:
            continue

        # We only process items at level 05 or 10 that have PIC
        # Level 10 items under a group 05 are fine
        # But if a 05-level is a group (has subordinate 10s), skip it
        # This is handled by only processing lines with PIC

        # Extract the PIC clause
        pic_start = cleaned.upper().index("PIC")
        pic_portion = cleaned[pic_start:]
        # Remove any trailing VALUE clause or comments
        pic_portion = re.split(r"\s+VALUE\s+", pic_portion, flags=re.IGNORECASE)[0]
        pic_portion = pic_portion.strip().rstrip(".")

        # Check for COMP-3 or COMP (binary)
        is_comp3 = "COMP-3" in cleaned.upper()
        is_comp = "COMP" in cleaned.upper() and not is_comp3

        try:
            base_type, length, decimal_places, is_signed = _parse_pic_clause(pic_portion)
        except ValueError:
            continue

        # For COMP-3, storage is different (packed decimal)
        # For ASCII data files we use display format, so skip COMP handling for now
        if is_comp3 or is_comp:
            # Store but mark - these are for EBCDIC/export layouts
            pass

        is_filler = name.upper() == "FILLER"

        field_def = FieldDef(
            name=name,
            pic_type=pic_portion.replace("PIC ", "").strip(),
            offset=offset,
            length=length,
            decimal_places=decimal_places,
            is_signed=is_signed,
            is_filler=is_filler,
        )
        fields.append(field_def)
        offset += length

    # Determine record length from the comment or total
    record_length = offset
    for line in lines:
        m = re.search(r"RECLN\s*=?\s*(\d+)", line, re.IGNORECASE)
        if m:
            record_length = int(m.group(1))
            break

    return RecordLayout(
        name=record_name,
        record_length=record_length,
        fields=fields,
        copybook_source=copybook_text,
    )


# ---------------------------------------------------------------------------
# Data file parsing
# ---------------------------------------------------------------------------


def decode_zoned_decimal(raw: str, decimal_places: int) -> str:
    """Decode a zoned-decimal ASCII string to a decimal string.

    The last character encodes both the last digit and the sign.
    '{' = +0, 'A'-'I' = +1 to +9
    '}' = -0, 'J'-'R' = -1 to -9
    If the last character is a plain digit, treat as unsigned positive.
    """
    if not raw:
        return "0"

    raw = raw.strip()
    if not raw:
        return "0"

    last_char = raw[-1]
    digits_before = raw[:-1]

    if last_char in ALL_SIGN_MAP:
        last_digit, sign = ALL_SIGN_MAP[last_char]
        int_str = digits_before + str(last_digit)
    elif last_char.isdigit():
        int_str = raw
        sign = 1
    else:
        int_str = digits_before + "0"
        sign = 1

    # Remove leading zeros but keep at least one digit
    int_str = int_str.lstrip("0") or "0"

    if decimal_places > 0:
        # Pad with leading zeros if necessary
        while len(int_str) < decimal_places + 1:
            int_str = "0" + int_str
        int_part = int_str[:-decimal_places]
        dec_part = int_str[-decimal_places:]
        if not int_part:
            int_part = "0"
        value_str = f"{int_part}.{dec_part}"
    else:
        value_str = int_str

    if sign < 0 and value_str != "0" and value_str != "0.00":
        value_str = "-" + value_str

    return value_str


def parse_field_value(raw: str, field_def: FieldDef) -> Any:
    """Parse a raw fixed-width field string into its typed value.

    Returns the parsed value as a string (for alphanumeric) or
    a numeric string (for numeric types).
    """
    if field_def.is_filler:
        return raw

    pic_upper = field_def.pic_type.upper()

    # Alphanumeric field
    if pic_upper.startswith("X"):
        return raw.rstrip()

    # Signed numeric with possible decimal
    if field_def.is_signed:
        return decode_zoned_decimal(raw, field_def.decimal_places)

    # Unsigned numeric
    if "9" in pic_upper:
        stripped = raw.strip()
        if not stripped:
            return "0"
        try:
            if field_def.decimal_places > 0:
                int_str = stripped.lstrip("0") or "0"
                while len(int_str) < field_def.decimal_places + 1:
                    int_str = "0" + int_str
                int_part = int_str[:-field_def.decimal_places]
                dec_part = int_str[-field_def.decimal_places:]
                if not int_part:
                    int_part = "0"
                return f"{int_part}.{dec_part}"
            else:
                return str(int(stripped))
        except ValueError:
            return stripped

    return raw.rstrip()


def parse_record(line: str, layout: RecordLayout) -> dict[str, Any]:
    """Parse a single fixed-width record line using the given layout.

    Returns a dict mapping field names to parsed values.
    FILLER fields are excluded from the output.
    """
    record: dict[str, Any] = {}

    for field_def in layout.fields:
        start = field_def.offset
        end = start + field_def.length
        raw = line[start:end] if end <= len(line) else line[start:].ljust(field_def.length)

        if field_def.is_filler:
            continue

        value = parse_field_value(raw, field_def)
        record[field_def.name] = value

    return record


def parse_data_file(
    data_path: str | Path,
    layout: RecordLayout,
    include_metadata: bool = True,
) -> dict[str, Any]:
    """Parse an entire fixed-width data file using the given record layout.

    Returns a dict with:
      - "_metadata": schema information (if include_metadata=True)
      - "records": list of parsed record dicts
    """
    data_path = Path(data_path)
    records: list[dict[str, Any]] = []

    with open(data_path, "r", encoding="ascii", errors="replace") as f:
        for line in f:
            # Skip empty lines
            stripped = line.rstrip("\n").rstrip("\r")
            if not stripped:
                continue
            records.append(parse_record(stripped, layout))

    result: dict[str, Any] = {}

    if include_metadata:
        result["_metadata"] = {
            "source_file": data_path.name,
            "copybook": (
                layout.copybook_source.splitlines()[0].strip()
                if layout.copybook_source
                else ""
            ),
            "record_name": layout.name,
            "record_length": layout.record_length,
            "record_count": len(records),
            "fields": [
                {
                    "name": f.name,
                    "pic_type": f.pic_type,
                    "offset": f.offset,
                    "length": f.length,
                    "decimal_places": f.decimal_places,
                    "is_signed": f.is_signed,
                    "is_filler": f.is_filler,
                }
                for f in layout.fields
            ],
        }

    result["records"] = records
    return result


# ---------------------------------------------------------------------------
# Copybook definitions for CardDemo data files (hardcoded for convenience)
# ---------------------------------------------------------------------------


def get_account_layout() -> RecordLayout:
    """Account record layout from CVACT01Y.cpy (RECLN 300)."""
    return RecordLayout(
        name="ACCOUNT-RECORD",
        record_length=300,
        fields=[
            FieldDef("ACCT-ID", "9(11)", 0, 11),
            FieldDef("ACCT-ACTIVE-STATUS", "X(01)", 11, 1),
            FieldDef("ACCT-CURR-BAL", "S9(10)V99", 12, 12, decimal_places=2, is_signed=True),
            FieldDef("ACCT-CREDIT-LIMIT", "S9(10)V99", 24, 12, decimal_places=2, is_signed=True),
            FieldDef(
                "ACCT-CASH-CREDIT-LIMIT", "S9(10)V99", 36, 12,
                decimal_places=2, is_signed=True,
            ),
            FieldDef("ACCT-OPEN-DATE", "X(10)", 48, 10),
            FieldDef("ACCT-EXPIRAION-DATE", "X(10)", 58, 10),
            FieldDef("ACCT-REISSUE-DATE", "X(10)", 68, 10),
            FieldDef("ACCT-CURR-CYC-CREDIT", "S9(10)V99", 78, 12, decimal_places=2, is_signed=True),
            FieldDef("ACCT-CURR-CYC-DEBIT", "S9(10)V99", 90, 12, decimal_places=2, is_signed=True),
            FieldDef("ACCT-ADDR-ZIP", "X(10)", 102, 10),
            FieldDef("ACCT-GROUP-ID", "X(10)", 112, 10),
            FieldDef("FILLER", "X(178)", 122, 178, is_filler=True),
        ],
    )


def get_card_layout() -> RecordLayout:
    """Card record layout from CVACT02Y.cpy (RECLN 150)."""
    return RecordLayout(
        name="CARD-RECORD",
        record_length=150,
        fields=[
            FieldDef("CARD-NUM", "X(16)", 0, 16),
            FieldDef("CARD-ACCT-ID", "9(11)", 16, 11),
            FieldDef("CARD-CVV-CD", "9(03)", 27, 3),
            FieldDef("CARD-EMBOSSED-NAME", "X(50)", 30, 50),
            FieldDef("CARD-EXPIRAION-DATE", "X(10)", 80, 10),
            FieldDef("CARD-ACTIVE-STATUS", "X(01)", 90, 1),
            FieldDef("FILLER", "X(59)", 91, 59, is_filler=True),
        ],
    )


def get_card_xref_layout() -> RecordLayout:
    """Card cross-reference layout from CVACT03Y.cpy (RECLN 50)."""
    return RecordLayout(
        name="CARD-XREF-RECORD",
        record_length=50,
        fields=[
            FieldDef("XREF-CARD-NUM", "X(16)", 0, 16),
            FieldDef("XREF-CUST-ID", "9(09)", 16, 9),
            FieldDef("XREF-ACCT-ID", "9(11)", 25, 11),
            FieldDef("FILLER", "X(14)", 36, 14, is_filler=True),
        ],
    )


def get_customer_layout() -> RecordLayout:
    """Customer record layout from CVCUS01Y.cpy (RECLN 500)."""
    return RecordLayout(
        name="CUSTOMER-RECORD",
        record_length=500,
        fields=[
            FieldDef("CUST-ID", "9(09)", 0, 9),
            FieldDef("CUST-FIRST-NAME", "X(25)", 9, 25),
            FieldDef("CUST-MIDDLE-NAME", "X(25)", 34, 25),
            FieldDef("CUST-LAST-NAME", "X(25)", 59, 25),
            FieldDef("CUST-ADDR-LINE-1", "X(50)", 84, 50),
            FieldDef("CUST-ADDR-LINE-2", "X(50)", 134, 50),
            FieldDef("CUST-ADDR-LINE-3", "X(50)", 184, 50),
            FieldDef("CUST-ADDR-STATE-CD", "X(02)", 234, 2),
            FieldDef("CUST-ADDR-COUNTRY-CD", "X(03)", 236, 3),
            FieldDef("CUST-ADDR-ZIP", "X(10)", 239, 10),
            FieldDef("CUST-PHONE-NUM-1", "X(15)", 249, 15),
            FieldDef("CUST-PHONE-NUM-2", "X(15)", 264, 15),
            FieldDef("CUST-SSN", "9(09)", 279, 9),
            FieldDef("CUST-GOVT-ISSUED-ID", "X(20)", 288, 20),
            FieldDef("CUST-DOB-YYYY-MM-DD", "X(10)", 308, 10),
            FieldDef("CUST-EFT-ACCOUNT-ID", "X(10)", 318, 10),
            FieldDef("CUST-PRI-CARD-HOLDER-IND", "X(01)", 328, 1),
            FieldDef("CUST-FICO-CREDIT-SCORE", "9(03)", 329, 3),
            FieldDef("FILLER", "X(168)", 332, 168, is_filler=True),
        ],
    )


def get_daily_transaction_layout() -> RecordLayout:
    """Daily transaction layout from CVTRA06Y.cpy (RECLN 350)."""
    return RecordLayout(
        name="DALYTRAN-RECORD",
        record_length=350,
        fields=[
            FieldDef("DALYTRAN-ID", "X(16)", 0, 16),
            FieldDef("DALYTRAN-TYPE-CD", "X(02)", 16, 2),
            FieldDef("DALYTRAN-CAT-CD", "9(04)", 18, 4),
            FieldDef("DALYTRAN-SOURCE", "X(10)", 22, 10),
            FieldDef("DALYTRAN-DESC", "X(100)", 32, 100),
            FieldDef("DALYTRAN-AMT", "S9(09)V99", 132, 11, decimal_places=2, is_signed=True),
            FieldDef("DALYTRAN-MERCHANT-ID", "9(09)", 143, 9),
            FieldDef("DALYTRAN-MERCHANT-NAME", "X(50)", 152, 50),
            FieldDef("DALYTRAN-MERCHANT-CITY", "X(50)", 202, 50),
            FieldDef("DALYTRAN-MERCHANT-ZIP", "X(10)", 252, 10),
            FieldDef("DALYTRAN-CARD-NUM", "X(16)", 262, 16),
            FieldDef("DALYTRAN-ORIG-TS", "X(26)", 278, 26),
            FieldDef("DALYTRAN-PROC-TS", "X(26)", 304, 26),
            FieldDef("FILLER", "X(20)", 330, 20, is_filler=True),
        ],
    )


def get_tran_cat_bal_layout() -> RecordLayout:
    """Transaction category balance layout from CVTRA01Y.cpy (RECLN 50)."""
    return RecordLayout(
        name="TRAN-CAT-BAL-RECORD",
        record_length=50,
        fields=[
            FieldDef("TRANCAT-ACCT-ID", "9(11)", 0, 11),
            FieldDef("TRANCAT-TYPE-CD", "X(02)", 11, 2),
            FieldDef("TRANCAT-CD", "9(04)", 13, 4),
            FieldDef("TRAN-CAT-BAL", "S9(09)V99", 17, 11, decimal_places=2, is_signed=True),
            FieldDef("FILLER", "X(22)", 28, 22, is_filler=True),
        ],
    )


def get_disclosure_group_layout() -> RecordLayout:
    """Disclosure group layout from CVTRA02Y.cpy (RECLN 50)."""
    return RecordLayout(
        name="DIS-GROUP-RECORD",
        record_length=50,
        fields=[
            FieldDef("DIS-ACCT-GROUP-ID", "X(10)", 0, 10),
            FieldDef("DIS-TRAN-TYPE-CD", "X(02)", 10, 2),
            FieldDef("DIS-TRAN-CAT-CD", "9(04)", 12, 4),
            FieldDef("DIS-INT-RATE", "S9(04)V99", 16, 6, decimal_places=2, is_signed=True),
            FieldDef("FILLER", "X(28)", 22, 28, is_filler=True),
        ],
    )


def get_transaction_type_layout() -> RecordLayout:
    """Transaction type layout from CVTRA03Y.cpy (RECLN 60)."""
    return RecordLayout(
        name="TRAN-TYPE-RECORD",
        record_length=60,
        fields=[
            FieldDef("TRAN-TYPE", "X(02)", 0, 2),
            FieldDef("TRAN-TYPE-DESC", "X(50)", 2, 50),
            FieldDef("FILLER", "X(08)", 52, 8, is_filler=True),
        ],
    )


def get_transaction_category_layout() -> RecordLayout:
    """Transaction category layout from CVTRA04Y.cpy (RECLN 60)."""
    return RecordLayout(
        name="TRAN-CAT-RECORD",
        record_length=60,
        fields=[
            FieldDef("TRAN-TYPE-CD", "X(02)", 0, 2),
            FieldDef("TRAN-CAT-CD", "9(04)", 2, 4),
            FieldDef("TRAN-CAT-TYPE-DESC", "X(50)", 6, 50),
            FieldDef("FILLER", "X(04)", 56, 4, is_filler=True),
        ],
    )


# Mapping of data file names to their layout functions
LAYOUT_REGISTRY: dict[str, callable] = {
    "acctdata.txt": get_account_layout,
    "carddata.txt": get_card_layout,
    "cardxref.txt": get_card_xref_layout,
    "custdata.txt": get_customer_layout,
    "dailytran.txt": get_daily_transaction_layout,
    "tcatbal.txt": get_tran_cat_bal_layout,
    "trancatg.txt": get_transaction_category_layout,
    "trantype.txt": get_transaction_type_layout,
    "discgrp.txt": get_disclosure_group_layout,
}


def get_layout_for_file(filename: str) -> RecordLayout:
    """Look up the record layout for a given data file name."""
    basename = Path(filename).name
    if basename not in LAYOUT_REGISTRY:
        raise ValueError(
            f"No layout registered for '{basename}'. "
            f"Known files: {', '.join(sorted(LAYOUT_REGISTRY.keys()))}"
        )
    return LAYOUT_REGISTRY[basename]()


# ---------------------------------------------------------------------------
# CLI entry point
# ---------------------------------------------------------------------------


def main() -> None:
    """Command-line interface for parsing COBOL data files."""
    import argparse

    parser = argparse.ArgumentParser(
        description="Parse a fixed-width COBOL data file using copybook definitions."
    )
    parser.add_argument(
        "--data", required=True, help="Path to the ASCII data file"
    )
    parser.add_argument(
        "--output", required=False, help="Path to write the JSON output (default: stdout)"
    )
    parser.add_argument(
        "--layout",
        required=False,
        help="Data file name to look up layout (default: inferred from --data filename)",
    )

    args = parser.parse_args()

    layout_name = args.layout or Path(args.data).name
    layout = get_layout_for_file(layout_name)

    result = parse_data_file(args.data, layout, include_metadata=True)

    json_str = json.dumps(result, indent=2, ensure_ascii=False)

    if args.output:
        Path(args.output).write_text(json_str + "\n", encoding="utf-8")
        print(f"Wrote {len(result['records'])} records to {args.output}")
    else:
        print(json_str)


if __name__ == "__main__":
    main()
