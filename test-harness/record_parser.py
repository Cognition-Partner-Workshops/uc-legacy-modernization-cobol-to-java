"""Parse fixed-width ASCII data files into Python dicts using copybook layouts.

The CardDemo ASCII data files are fixed-width records whose layout is defined
by the corresponding COBOL copybook.  This module reads those files line-by-line,
slices each line according to the field descriptors produced by
``copybook_parser``, and converts raw text into typed Python values.
"""

from __future__ import annotations

import re
from pathlib import Path
from typing import Any, Dict, List, Optional

from .copybook_parser import FieldDescriptor

# ---------------------------------------------------------------------------
# COBOL sign-overpunch decoding (ASCII representation)
# ---------------------------------------------------------------------------

_POSITIVE_OVERPUNCH = {
    "{": 0, "A": 1, "B": 2, "C": 3, "D": 4,
    "E": 5, "F": 6, "G": 7, "H": 8, "I": 9,
}

_NEGATIVE_OVERPUNCH = {
    "}": 0, "J": 1, "K": 2, "L": 3, "M": 4,
    "N": 5, "O": 6, "P": 7, "Q": 8, "R": 9,
}


def decode_signed_numeric(raw: str, decimal_places: int = 0) -> Optional[float]:
    """Decode a COBOL signed-numeric display field (with overpunch).

    Returns ``None`` if the field is blank / unparseable.
    """
    raw = raw.strip()
    if not raw:
        return None

    sign = 1
    last = raw[-1]
    digits = raw

    if last in _POSITIVE_OVERPUNCH:
        digits = raw[:-1] + str(_POSITIVE_OVERPUNCH[last])
        sign = 1
    elif last in _NEGATIVE_OVERPUNCH:
        digits = raw[:-1] + str(_NEGATIVE_OVERPUNCH[last])
        sign = -1
    elif last == "-":
        digits = raw[:-1]
        sign = -1
    elif last == "+":
        digits = raw[:-1]
        sign = 1

    # Remove any non-digit characters (spaces, etc.)
    digits = re.sub(r"[^0-9]", "", digits)
    if not digits:
        return None

    value = int(digits) * sign
    if decimal_places > 0:
        value = value / (10 ** decimal_places)
    return value


def decode_unsigned_numeric(raw: str) -> Optional[int]:
    """Decode a plain ``PIC 9(n)`` field."""
    stripped = raw.strip()
    if not stripped:
        return None
    digits = re.sub(r"[^0-9]", "", stripped)
    if not digits:
        return None
    return int(digits)


# ---------------------------------------------------------------------------
# Single-record parser
# ---------------------------------------------------------------------------

def parse_record(
    line: str,
    fields: List[FieldDescriptor],
    *,
    strip_alpha: bool = True,
) -> Dict[str, Any]:
    """Slice *line* according to *fields* and return a typed dict.

    Parameters
    ----------
    line:
        A single fixed-width record (without trailing newline).
    fields:
        Field descriptors from ``copybook_parser.parse_copybook``.
    strip_alpha:
        If ``True``, right-strip whitespace from ``PIC X`` fields.
    """
    record: Dict[str, Any] = {}

    for fd in fields:
        if fd.is_filler:
            continue

        start = fd.offset
        end = start + fd.length

        # Guard against short lines
        if start >= len(line):
            raw = ""
        elif end > len(line):
            raw = line[start:]
        else:
            raw = line[start:end]

        # Type conversion
        if fd.field_type == "signed_numeric":
            record[fd.name] = decode_signed_numeric(raw, fd.decimal_places)
        elif fd.field_type == "numeric":
            record[fd.name] = decode_unsigned_numeric(raw)
        else:
            record[fd.name] = raw.rstrip() if strip_alpha else raw

    return record


# ---------------------------------------------------------------------------
# Multi-record file parser
# ---------------------------------------------------------------------------

def parse_file(
    data_path: str | Path,
    fields: List[FieldDescriptor],
    recln: int,
    *,
    strip_alpha: bool = True,
) -> List[Dict[str, Any]]:
    """Parse an entire fixed-width ASCII data file.

    Lines shorter than *recln* are right-padded with spaces.  Blank lines
    are skipped.
    """
    data_path = Path(data_path)
    content = data_path.read_text(encoding="utf-8", errors="replace")
    records: List[Dict[str, Any]] = []

    # Some files use exactly recln chars per "line" without newlines.
    # Try line-based first; fall back to chunk-based if lines are wrong length.
    lines = content.splitlines()
    non_blank = [ln for ln in lines if ln.strip()]

    if non_blank and all(len(ln) == recln for ln in non_blank):
        # Perfect fixed-width with newlines
        for ln in non_blank:
            records.append(parse_record(ln, fields, strip_alpha=strip_alpha))
    elif non_blank and any(len(ln) > recln for ln in non_blank):
        # Lines may be multi-record or wrapped; try chunk-based
        flat = "".join(non_blank)
        for i in range(0, len(flat), recln):
            chunk = flat[i : i + recln]
            if chunk.strip():
                records.append(
                    parse_record(chunk, fields, strip_alpha=strip_alpha)
                )
    else:
        # Lines shorter than recln — pad
        for ln in non_blank:
            padded = ln.ljust(recln)
            records.append(parse_record(padded, fields, strip_alpha=strip_alpha))

    return records
