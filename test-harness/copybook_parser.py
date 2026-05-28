"""Parse COBOL copybook files into field definitions for record parsing."""

import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import List, Optional


@dataclass
class CopybookField:
    """Represents a single field definition from a COBOL copybook."""
    name: str
    level: int
    pic: str
    offset: int
    length: int
    field_type: str  # 'alphanumeric', 'numeric', 'signed_decimal'
    decimal_places: int = 0
    is_filler: bool = False
    redefines: Optional[str] = None


@dataclass
class CopybookLayout:
    """Complete layout of a COBOL copybook with all field definitions."""
    name: str
    record_length: int
    fields: List[CopybookField] = field(default_factory=list)

    def get_field(self, name: str) -> Optional[CopybookField]:
        for f in self.fields:
            if f.name == name:
                return f
        return None

    def data_fields(self) -> List[CopybookField]:
        """Return non-FILLER, non-REDEFINES fields for parsing."""
        return [f for f in self.fields if not f.is_filler and f.redefines is None]


def parse_pic_length(pic: str) -> tuple:
    """Parse PIC clause to get (display_length, decimal_places, field_type)."""
    original = pic.strip().upper()
    is_signed = original.startswith("S")
    working = original.lstrip("S")

    has_decimal = "V" in working

    if has_decimal:
        parts = working.split("V")
        int_part = parts[0]
        dec_part = parts[1] if len(parts) > 1 else ""
        int_len = _expand_length(int_part)
        dec_len = _expand_length(dec_part)
        total = int_len + dec_len
        ftype = "signed_decimal" if is_signed else "numeric"
        return (total, dec_len, ftype)

    length = _expand_length(working)

    if "X" in working or "A" in working:
        return (length, 0, "alphanumeric")
    elif "9" in working:
        ftype = "signed_decimal" if is_signed else "numeric"
        return (length, 0, ftype)
    return (length, 0, "alphanumeric")


def _expand_length(fragment: str) -> int:
    """Expand PIC fragment like 9(11) or XX to character count."""
    total = 0
    i = 0
    while i < len(fragment):
        ch = fragment[i]
        if ch in ("9", "X", "A", "Z", "-", "+", ".", ","):
            if i + 1 < len(fragment) and fragment[i + 1] == "(":
                close = fragment.index(")", i + 2)
                count = int(fragment[i + 2:close])
                total += count
                i = close + 1
            else:
                total += 1
                i += 1
        else:
            i += 1
    return total


def parse_copybook(filepath: str) -> CopybookLayout:
    """Parse a COBOL copybook file and return a CopybookLayout.

    Handles standard fixed-format COBOL (columns 7-72) and free-format.
    Skips REDEFINES fields and comment lines.
    """
    path = Path(filepath)
    lines = path.read_text().splitlines()

    # Collect logical statements (join continuations)
    statements = []
    current = ""

    for line in lines:
        # Skip empty lines
        if not line.strip():
            continue

        # Handle fixed-format: column 7 is indicator area
        content = line
        if len(line) > 6:
            indicator = line[6] if len(line) > 6 else " "
            if indicator == "*":
                continue
            # Strip sequence numbers (cols 1-6) and identification (73-80)
            content = line[6:72] if len(line) >= 72 else line[6:]

        # Remove line numbers at start (for numbered source)
        content = re.sub(r"^\d{6}\s?", "", content)

        stripped = content.strip()
        if not stripped or stripped.startswith("*"):
            continue

        current += " " + stripped

        if current.rstrip().endswith("."):
            statements.append(current.strip().rstrip("."))
            current = ""

    # Parse statements into fields
    fields = []
    offset = 0
    layout_name = path.stem

    for stmt in statements:
        # Match level number, name, and optional PIC/REDEFINES
        match = re.match(
            r"(\d{2})\s+([\w-]+)(?:\s+REDEFINES\s+([\w-]+))?\s*(?:PIC\s+(.+?))?$",
            stmt, re.IGNORECASE
        )
        if not match:
            # Try matching FILLER
            filler_match = re.match(
                r"(\d{2})\s+FILLER\s+PIC\s+(.+?)$",
                stmt, re.IGNORECASE
            )
            if filler_match:
                level = int(filler_match.group(1))
                pic = filler_match.group(2).strip()
                length, dec_places, ftype = parse_pic_length(pic)
                fields.append(CopybookField(
                    name="FILLER",
                    level=level,
                    pic=pic,
                    offset=offset,
                    length=length,
                    field_type=ftype,
                    decimal_places=dec_places,
                    is_filler=True,
                ))
                offset += length
            continue

        level = int(match.group(1))
        name = match.group(2)
        redefines = match.group(3)
        pic = match.group(4)

        # Skip group-level items (no PIC clause)
        if not pic:
            continue

        pic = pic.strip()
        # Remove VALUE clause if present
        pic = re.sub(r"\s+VALUE\s+.*$", "", pic, flags=re.IGNORECASE)
        pic = pic.strip()

        length, dec_places, ftype = parse_pic_length(pic)

        # REDEFINES fields occupy the same space
        if redefines:
            redef_field = next((f for f in fields if f.name == redefines), None)
            redef_offset = redef_field.offset if redef_field else offset
            fields.append(CopybookField(
                name=name,
                level=level,
                pic=pic,
                offset=redef_offset,
                length=length,
                field_type=ftype,
                decimal_places=dec_places,
                redefines=redefines,
            ))
            continue

        is_filler = name.upper() == "FILLER"
        fields.append(CopybookField(
            name=name,
            level=level,
            pic=pic,
            offset=offset,
            length=length,
            field_type=ftype,
            decimal_places=dec_places,
            is_filler=is_filler,
        ))
        offset += length

    record_length = offset
    return CopybookLayout(name=layout_name, record_length=record_length, fields=fields)


# Pre-defined layouts for CardDemo data files based on copybook analysis
CARDDEMO_LAYOUTS = {
    "acctdata": {
        "copybook": "CVACT01Y.cpy",
        "record_length": 300,
        "fields": [
            {"name": "ACCT-ID", "pic": "9(11)", "offset": 0, "length": 11},
            {"name": "ACCT-ACTIVE-STATUS", "pic": "X(01)", "offset": 11, "length": 1},
            {"name": "ACCT-CURR-BAL", "pic": "S9(10)V99", "offset": 12, "length": 12},
            {"name": "ACCT-CREDIT-LIMIT", "pic": "S9(10)V99", "offset": 24, "length": 12},
            {"name": "ACCT-CASH-CREDIT-LIMIT", "pic": "S9(10)V99", "offset": 36, "length": 12},
            {"name": "ACCT-OPEN-DATE", "pic": "X(10)", "offset": 48, "length": 10},
            {"name": "ACCT-EXPIRAION-DATE", "pic": "X(10)", "offset": 58, "length": 10},
            {"name": "ACCT-REISSUE-DATE", "pic": "X(10)", "offset": 68, "length": 10},
            {"name": "ACCT-CURR-CYC-CREDIT", "pic": "S9(10)V99", "offset": 78, "length": 12},
            {"name": "ACCT-CURR-CYC-DEBIT", "pic": "S9(10)V99", "offset": 90, "length": 12},
            {"name": "ACCT-ADDR-ZIP", "pic": "X(10)", "offset": 102, "length": 10},
            {"name": "ACCT-GROUP-ID", "pic": "X(10)", "offset": 112, "length": 10},
        ],
    },
    "carddata": {
        "copybook": "CVACT02Y.cpy",
        "record_length": 150,
        "fields": [
            {"name": "CARD-NUM", "pic": "X(16)", "offset": 0, "length": 16},
            {"name": "CARD-ACCT-ID", "pic": "9(11)", "offset": 16, "length": 11},
            {"name": "CARD-CVV-CD", "pic": "9(03)", "offset": 27, "length": 3},
            {"name": "CARD-EMBOSSED-NAME", "pic": "X(50)", "offset": 30, "length": 50},
            {"name": "CARD-EXPIRAION-DATE", "pic": "X(10)", "offset": 80, "length": 10},
            {"name": "CARD-ACTIVE-STATUS", "pic": "X(01)", "offset": 90, "length": 1},
        ],
    },
    "custdata": {
        "copybook": "CVCUS01Y.cpy",
        "record_length": 500,
        "fields": [
            {"name": "CUST-ID", "pic": "9(09)", "offset": 0, "length": 9},
            {"name": "CUST-FIRST-NAME", "pic": "X(25)", "offset": 9, "length": 25},
            {"name": "CUST-MIDDLE-NAME", "pic": "X(25)", "offset": 34, "length": 25},
            {"name": "CUST-LAST-NAME", "pic": "X(25)", "offset": 59, "length": 25},
            {"name": "CUST-ADDR-LINE-1", "pic": "X(50)", "offset": 84, "length": 50},
            {"name": "CUST-ADDR-LINE-2", "pic": "X(50)", "offset": 134, "length": 50},
            {"name": "CUST-ADDR-LINE-3", "pic": "X(50)", "offset": 184, "length": 50},
            {"name": "CUST-ADDR-STATE-CD", "pic": "X(02)", "offset": 234, "length": 2},
            {"name": "CUST-ADDR-COUNTRY-CD", "pic": "X(03)", "offset": 236, "length": 3},
            {"name": "CUST-ADDR-ZIP", "pic": "X(10)", "offset": 239, "length": 10},
            {"name": "CUST-PHONE-NUM-1", "pic": "X(15)", "offset": 249, "length": 15},
            {"name": "CUST-PHONE-NUM-2", "pic": "X(15)", "offset": 264, "length": 15},
            {"name": "CUST-SSN", "pic": "9(09)", "offset": 279, "length": 9},
            {"name": "CUST-GOVT-ISSUED-ID", "pic": "X(20)", "offset": 288, "length": 20},
            {"name": "CUST-DOB-YYYY-MM-DD", "pic": "X(10)", "offset": 308, "length": 10},
            {"name": "CUST-EFT-ACCOUNT-ID", "pic": "X(10)", "offset": 318, "length": 10},
            {"name": "CUST-PRI-CARD-HOLDER-IND", "pic": "X(01)", "offset": 328, "length": 1},
            {"name": "CUST-FICO-CREDIT-SCORE", "pic": "9(03)", "offset": 329, "length": 3},
        ],
    },
    "cardxref": {
        "copybook": "CVACT03Y.cpy",
        "record_length": 50,
        "fields": [
            {"name": "XREF-CARD-NUM", "pic": "X(16)", "offset": 0, "length": 16},
            {"name": "XREF-CUST-ID", "pic": "9(09)", "offset": 16, "length": 9},
            {"name": "XREF-ACCT-ID", "pic": "9(11)", "offset": 25, "length": 11},
        ],
    },
    "dailytran": {
        "copybook": "CVTRA05Y.cpy",
        "record_length": 350,
        "fields": [
            {"name": "TRAN-ID", "pic": "X(16)", "offset": 0, "length": 16},
            {"name": "TRAN-TYPE-CD", "pic": "X(02)", "offset": 16, "length": 2},
            {"name": "TRAN-CAT-CD", "pic": "9(04)", "offset": 18, "length": 4},
            {"name": "TRAN-SOURCE", "pic": "X(10)", "offset": 22, "length": 10},
            {"name": "TRAN-DESC", "pic": "X(100)", "offset": 32, "length": 100},
            {"name": "TRAN-AMT", "pic": "S9(09)V99", "offset": 132, "length": 11},
            {"name": "TRAN-MERCHANT-ID", "pic": "9(09)", "offset": 143, "length": 9},
            {"name": "TRAN-MERCHANT-NAME", "pic": "X(50)", "offset": 152, "length": 50},
            {"name": "TRAN-MERCHANT-CITY", "pic": "X(50)", "offset": 202, "length": 50},
            {"name": "TRAN-MERCHANT-ZIP", "pic": "X(10)", "offset": 252, "length": 10},
            {"name": "TRAN-CARD-NUM", "pic": "X(16)", "offset": 262, "length": 16},
            {"name": "TRAN-ORIG-TS", "pic": "X(26)", "offset": 278, "length": 26},
            {"name": "TRAN-PROC-TS", "pic": "X(26)", "offset": 304, "length": 26},
        ],
    },
    "tcatbal": {
        "copybook": "CVTRA01Y.cpy",
        "record_length": 50,
        "fields": [
            {"name": "TRANCAT-ACCT-ID", "pic": "9(11)", "offset": 0, "length": 11},
            {"name": "TRANCAT-TYPE-CD", "pic": "X(02)", "offset": 11, "length": 2},
            {"name": "TRANCAT-CD", "pic": "9(04)", "offset": 13, "length": 4},
            {"name": "TRAN-CAT-BAL", "pic": "S9(09)V99", "offset": 17, "length": 11},
        ],
    },
    "discgrp": {
        "copybook": "CVTRA02Y.cpy",
        "record_length": 50,
        "fields": [
            {"name": "DIS-ACCT-GROUP-ID", "pic": "X(10)", "offset": 0, "length": 10},
            {"name": "DIS-TRAN-TYPE-CD", "pic": "X(02)", "offset": 10, "length": 2},
            {"name": "DIS-TRAN-CAT-CD", "pic": "9(04)", "offset": 12, "length": 4},
            {"name": "DIS-INT-RATE", "pic": "S9(04)V99", "offset": 16, "length": 6},
        ],
    },
    "trantype": {
        "copybook": "CVTRA03Y.cpy",
        "record_length": 60,
        "fields": [
            {"name": "TRAN-TYPE", "pic": "X(02)", "offset": 0, "length": 2},
            {"name": "TRAN-TYPE-DESC", "pic": "X(50)", "offset": 2, "length": 50},
        ],
    },
    "trancatg": {
        "copybook": "CVTRA04Y.cpy",
        "record_length": 60,
        "fields": [
            {"name": "TRAN-TYPE-CD", "pic": "X(02)", "offset": 0, "length": 2},
            {"name": "TRAN-CAT-CD", "pic": "9(04)", "offset": 2, "length": 4},
            {"name": "TRAN-CAT-TYPE-DESC", "pic": "X(50)", "offset": 6, "length": 50},
        ],
    },
}
