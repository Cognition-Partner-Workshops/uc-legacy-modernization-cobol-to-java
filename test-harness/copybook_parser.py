"""
Parse COBOL copybook PIC clauses into field-definition lists.

Each definition is a dict:
    {
        "name":   "ACCT-ID",
        "pic":    "9(11)",
        "type":   "numeric_display",   # alphanumeric | numeric_display | numeric_signed
        "length": 11,
        "scale":  0,                    # digits after implied decimal (V)
        "offset": 0                     # 0-based byte offset within the record
    }

Only level-05 / level-10 elementary items are emitted.  Group items (01, 05
with subordinates) and FILLER are included so the offset arithmetic stays
correct, but FILLER entries are still emitted so callers can verify total
record length.
"""

from __future__ import annotations

import re
from pathlib import Path
from typing import Any

# ---------------------------------------------------------------------------
# PIC-clause helpers
# ---------------------------------------------------------------------------

# Matches: PIC [S]9(10)V99  or  PIC [S]9(10)V9(02)  or  PIC [S]9(11)  or  PIC X(16)
_PIC_PAREN_RE = re.compile(
    r"PIC\s+"
    r"(S)?"                          # optional sign
    r"(9|X)"                         # base type
    r"\((\d+)\)"                     # repeat count e.g. 9(10)
    r"(?:"
    r"  V(?:9\((\d+)\)|(9+))"        # V9(2) or V99
    r")?"
    ,
    re.IGNORECASE | re.VERBOSE,
)

# Matches simple forms: PIC 999  PIC XX  PIC S999V99
_PIC_SIMPLE_RE = re.compile(
    r"PIC\s+"
    r"(S)?"
    r"(9+|X+)"
    r"(?:V(9+))?"
    ,
    re.IGNORECASE,
)


def _parse_pic(pic_text: str) -> dict[str, Any] | None:
    """Return (type, length, scale) from a PIC string."""
    pic_text = pic_text.strip().rstrip(".")

    m = _PIC_PAREN_RE.search(pic_text)
    if m:
        signed = m.group(1) is not None
        base = m.group(2).upper()
        count = int(m.group(3))

        # decimal digits — either from 9(n) group or 99 group
        if m.group(4) is not None:
            dec_count = int(m.group(4))
        elif m.group(5) is not None:
            dec_count = len(m.group(5))
        else:
            dec_count = 0

        if base == "X":
            return {
                "type": "alphanumeric",
                "length": count,
                "scale": 0,
            }
        total_len = count + dec_count
        return {
            "type": "numeric_signed" if signed else "numeric_display",
            "length": total_len,
            "scale": dec_count,
        }

    m = _PIC_SIMPLE_RE.search(pic_text)
    if m:
        signed = m.group(1) is not None
        base_chars = m.group(2)
        dec_chars = m.group(3) or ""
        base_type = base_chars[0].upper()
        count = len(base_chars)
        dec_count = len(dec_chars)

        if base_type == "X":
            return {
                "type": "alphanumeric",
                "length": count,
                "scale": 0,
            }
        total_len = count + dec_count
        return {
            "type": "numeric_signed" if signed else "numeric_display",
            "length": total_len,
            "scale": dec_count,
        }

    return None


# ---------------------------------------------------------------------------
# Copybook parser
# ---------------------------------------------------------------------------

_LEVEL_RE = re.compile(r"^\s*(\d{2})\s+([\w-]+)")
_PIC_CLAUSE_RE = re.compile(r"PIC\s+.+", re.IGNORECASE)
_REDEFINES_RE = re.compile(r"REDEFINES\s+", re.IGNORECASE)


def parse_copybook(path: str | Path) -> list[dict[str, Any]]:
    """Return an ordered list of field definitions from a copybook file."""
    lines = Path(path).read_text().splitlines()

    # Join continuation lines
    joined: list[str] = []
    for raw in lines:
        # Strip sequence numbers (cols 1-6) if present
        if len(raw) > 6 and raw[6] == " ":
            text = raw[6:]
        else:
            text = raw
        # Skip pure comment lines
        stripped = text.lstrip()
        if stripped.startswith("*"):
            continue
        if stripped.startswith("88 ") or stripped.startswith("88  "):
            continue
        joined.append(text)

    full_text = " ".join(joined)
    # Split on level numbers
    statements = re.split(r"(?=\b\d{2}\s+)", full_text)

    fields: list[dict[str, Any]] = []
    offset = 0

    for stmt in statements:
        stmt = stmt.strip()
        if not stmt:
            continue

        lm = _LEVEL_RE.match(stmt)
        if not lm:
            continue

        level = int(lm.group(1))
        name = lm.group(2)

        # Skip REDEFINES entries — they overlay existing storage
        if _REDEFINES_RE.search(stmt):
            continue

        pic_match = _PIC_CLAUSE_RE.search(stmt)
        if not pic_match:
            continue  # group item without PIC

        pic_info = _parse_pic(pic_match.group())
        if pic_info is None:
            continue

        field = {
            "name": name,
            "pic": pic_match.group().replace("PIC ", "").replace("pic ", "").strip().rstrip("."),
            "type": pic_info["type"],
            "length": pic_info["length"],
            "scale": pic_info["scale"],
            "offset": offset,
        }
        fields.append(field)
        offset += pic_info["length"]

    return fields


def record_length(fields: list[dict[str, Any]]) -> int:
    """Compute total record length from parsed fields."""
    if not fields:
        return 0
    last = fields[-1]
    return last["offset"] + last["length"]
