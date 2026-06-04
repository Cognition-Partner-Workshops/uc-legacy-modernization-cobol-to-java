"""Parse COBOL copybook files into machine-readable field descriptors.

Each copybook defines a fixed-width record layout.  This module extracts
every elementary (leaf) field and computes its absolute byte offset within
the record so that ``record_parser`` can slice ASCII data files correctly.
"""

from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import List, Optional


# ---------------------------------------------------------------------------
# PIC-clause helpers
# ---------------------------------------------------------------------------

_PIC_RE = re.compile(
    r"PIC\s+"
    r"(?P<pic>[SXZ9AaB0-8\(\)\+\-\.,V]+)",
    re.IGNORECASE,
)

_EXPAND_RE = re.compile(r"([9XZA\-\+,\.])\((\d+)\)")

_OCCURS_RE = re.compile(r"OCCURS\s+(\d+)\s+TIMES", re.IGNORECASE)

_REDEFINES_RE = re.compile(r"REDEFINES\s+(\S+)", re.IGNORECASE)

_COMP_RE = re.compile(r"\bCOMP(?:-([0-3]))?\b", re.IGNORECASE)


def _expand_pic(pic: str) -> str:
    """Expand shorthand: ``9(11)`` → ``99999999999``."""
    return _EXPAND_RE.sub(lambda m: m.group(1) * int(m.group(2)), pic)


@dataclass
class FieldDescriptor:
    """Describes a single elementary COBOL field."""

    name: str
    level: int
    pic_raw: str = ""
    pic_expanded: str = ""
    offset: int = 0
    length: int = 0
    field_type: str = "alpha"  # alpha | numeric | signed_numeric | filler
    decimal_places: int = 0
    occurs: int = 1
    redefines: Optional[str] = None
    comp: Optional[str] = None  # None | "COMP" | "COMP-3"
    children: List["FieldDescriptor"] = field(default_factory=list)
    is_filler: bool = False

    @property
    def display_length(self) -> int:
        """Byte width in the *display* (ASCII/EBCDIC) representation."""
        return self.length * self.occurs


# ---------------------------------------------------------------------------
# Length calculation
# ---------------------------------------------------------------------------

def _pic_display_length(pic_expanded: str) -> int:
    """Return the display byte-length for an expanded PIC string."""
    length = 0
    for ch in pic_expanded.upper():
        if ch in ("9", "X", "Z", "A"):
            length += 1
        elif ch == "V":
            pass  # implied decimal — no storage
        elif ch == "S":
            pass  # sign — included in zone of last digit (display)
        elif ch in ("+", "-"):
            length += 1
        elif ch in (",", "."):
            length += 1  # edited numerics occupy a byte
    return length


def _comp_length(pic_expanded: str, comp_type: str) -> int:
    """Return byte-length for COMP or COMP-3 fields."""
    digit_count = sum(1 for c in pic_expanded.upper() if c in ("9",))
    if comp_type.upper() in ("COMP", "COMP-0"):
        if digit_count <= 4:
            return 2
        if digit_count <= 9:
            return 4
        return 8
    if comp_type.upper() == "COMP-3":
        return (digit_count + 2) // 2  # ceil((digits+1)/2)
    return _pic_display_length(pic_expanded)


def _field_type(pic_expanded: str) -> str:
    upper = pic_expanded.upper()
    if upper.startswith("S") or "V" in upper:
        return "signed_numeric"
    if all(c in "9V()S" for c in upper):
        return "numeric"
    return "alpha"


def _decimal_places(pic_expanded: str) -> int:
    upper = pic_expanded.upper()
    idx = upper.find("V")
    if idx == -1:
        return 0
    return sum(1 for c in upper[idx + 1:] if c == "9")


# ---------------------------------------------------------------------------
# Main parser
# ---------------------------------------------------------------------------

_FIELD_LINE_RE = re.compile(
    r"^\s*(\d{2})\s+([\w-]+)",
)


def parse_copybook(source: str) -> List[FieldDescriptor]:
    """Parse a COBOL copybook source string into a flat list of fields.

    Only elementary (leaf) items with a PIC clause are included.  Group
    items (no PIC) are used only for calculating offsets of their children.
    REDEFINES clauses are tracked but the redefined fields share the same
    offset.

    Returns the list sorted by offset (ascending).
    """
    fields: List[FieldDescriptor] = []
    offset = 0
    # Track named-field offsets so REDEFINES can reset
    name_offsets: dict[str, int] = {}
    # Stack for REDEFINES offset tracking
    redefines_base_offset: Optional[int] = None

    for raw_line in source.splitlines():
        # Strip sequence numbers (cols 1-6) if present
        line = raw_line
        if len(line) > 6 and line[6] == " " and line[:6].strip().isdigit():
            line = line[6:]
        # Skip comment lines
        stripped = line.lstrip()
        if stripped.startswith("*") or not stripped:
            continue
        # Skip 88-level condition names
        if stripped.startswith("88 ") or stripped.startswith("88\t"):
            continue

        # Try to match a data item
        m = _FIELD_LINE_RE.match(stripped)
        if not m:
            continue

        level = int(m.group(1))
        name = m.group(2).upper()
        is_filler = name == "FILLER"

        # PIC clause
        pic_match = _PIC_RE.search(stripped)
        pic_raw = pic_match.group("pic").rstrip(".") if pic_match else ""
        pic_expanded = _expand_pic(pic_raw) if pic_raw else ""

        # OCCURS
        occurs_match = _OCCURS_RE.search(stripped)
        occurs = int(occurs_match.group(1)) if occurs_match else 1

        # REDEFINES
        redefines_match = _REDEFINES_RE.search(stripped)
        redefines_target = redefines_match.group(1).upper() if redefines_match else None

        # COMP / COMP-3
        comp_match = _COMP_RE.search(stripped)
        comp_type: Optional[str] = None
        if comp_match:
            suffix = comp_match.group(1)
            comp_type = f"COMP-{suffix}" if suffix else "COMP"

        # Calculate length
        if pic_expanded:
            if comp_type:
                length = _comp_length(pic_expanded, comp_type)
            else:
                length = _pic_display_length(pic_expanded)
        else:
            length = 0  # group item — children will contribute

        # Handle REDEFINES: reset offset to the redefined field
        if redefines_target and redefines_target in name_offsets:
            offset = name_offsets[redefines_target]

        # Record the offset for this name
        if not is_filler:
            name_offsets[name] = offset

        fd = FieldDescriptor(
            name=name,
            level=level,
            pic_raw=pic_raw,
            pic_expanded=pic_expanded,
            offset=offset,
            length=length,
            field_type=_field_type(pic_expanded) if pic_expanded else "group",
            decimal_places=_decimal_places(pic_expanded) if pic_expanded else 0,
            occurs=occurs,
            redefines=redefines_target,
            comp=comp_type,
            is_filler=is_filler,
        )

        if pic_expanded:
            fields.append(fd)
            if redefines_target is None:
                offset += length * occurs

    return fields


def parse_copybook_file(path: str | Path) -> List[FieldDescriptor]:
    """Convenience wrapper: read a file and parse it."""
    return parse_copybook(Path(path).read_text(encoding="utf-8", errors="replace"))


# ---------------------------------------------------------------------------
# Registry: maps data-file basenames to their copybook + declared RECLN
# ---------------------------------------------------------------------------

COPYBOOK_REGISTRY: dict[str, dict] = {
    "acctdata.txt": {
        "copybook": "CVACT01Y.cpy",
        "recln": 300,
        "key_field": "ACCT-ID",
    },
    "carddata.txt": {
        "copybook": "CVACT02Y.cpy",
        "recln": 150,
        "key_field": "CARD-NUM",
    },
    "cardxref.txt": {
        "copybook": "CVACT03Y.cpy",
        "recln": 50,
        "key_field": "XREF-CARD-NUM",
    },
    "custdata.txt": {
        "copybook": "CVCUS01Y.cpy",
        "recln": 500,
        "key_field": "CUST-ID",
    },
    "dailytran.txt": {
        "copybook": "CVTRA05Y.cpy",
        "recln": 350,
        "key_field": "TRAN-ID",
    },
    "discgrp.txt": {
        "copybook": "CVTRA02Y.cpy",
        "recln": 50,
        "key_field": ["DIS-ACCT-GROUP-ID", "DIS-TRAN-TYPE-CD", "DIS-TRAN-CAT-CD"],
    },
    "tcatbal.txt": {
        "copybook": "CVTRA01Y.cpy",
        "recln": 50,
        "key_field": ["TRANCAT-ACCT-ID", "TRANCAT-TYPE-CD", "TRANCAT-CD"],
    },
    "trancatg.txt": {
        "copybook": "CVTRA04Y.cpy",
        "recln": 60,
        "key_field": ["TRAN-TYPE-CD", "TRAN-CAT-CD"],
    },
    "trantype.txt": {
        "copybook": "CVTRA03Y.cpy",
        "recln": 60,
        "key_field": "TRAN-TYPE",
    },
}
