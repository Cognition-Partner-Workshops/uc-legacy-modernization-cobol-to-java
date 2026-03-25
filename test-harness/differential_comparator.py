#!/usr/bin/env python3
"""
Differential comparator for COBOL-vs-Java batch output.

Compares two datasets (one produced by the COBOL batch program, one by the
Java equivalent) and classifies every difference as:
  - field_value_mismatch  -- same record exists in both but a field differs
  - missing_record        -- record in COBOL output but not Java output
  - extra_record          -- record in Java output but not COBOL output

Usage:
    python differential_comparator.py <cobol_output.json> <java_output.json> \\
        [--key FIELD] [--tolerance 0.01] [--ignore-fields FILLER]
"""

from __future__ import annotations

import json
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

from golden_file_comparator import values_equal


@dataclass
class DiffEntry:
    """A single difference found between COBOL and Java outputs."""
    diff_type: str            # "field_value_mismatch", "missing_record", "extra_record"
    key_value: Any = None     # value of the key field for this record
    record_index: int = -1
    field_name: str = ""
    cobol_value: Any = None
    java_value: Any = None


@dataclass
class DifferentialResult:
    """Full result of a differential comparison run."""
    cobol_path: str
    java_path: str
    cobol_count: int = 0
    java_count: int = 0
    matched: int = 0
    field_mismatches: int = 0
    missing_records: int = 0
    extra_records: int = 0
    diffs: list[DiffEntry] = field(default_factory=list)

    @property
    def passed(self) -> bool:
        return not self.diffs

    def summary(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        lines = [
            f"[{status}] Differential: COBOL={self.cobol_path}  Java={self.java_path}",
            f"  Records -- COBOL: {self.cobol_count}  Java: {self.java_count}",
            f"  Matched: {self.matched}  Field-mismatches: {self.field_mismatches}",
            f"  Missing (in Java): {self.missing_records}  Extra (in Java): {self.extra_records}",
        ]
        if self.diffs:
            lines.append(f"  Differences (first 30):")
            for d in self.diffs[:30]:
                if d.diff_type == "field_value_mismatch":
                    lines.append(
                        f"    [{d.diff_type}] key={d.key_value} field={d.field_name} "
                        f"cobol={d.cobol_value!r} java={d.java_value!r}"
                    )
                else:
                    lines.append(f"    [{d.diff_type}] key={d.key_value}")
            if len(self.diffs) > 30:
                lines.append(f"    ... and {len(self.diffs) - 30} more")
        return "\n".join(lines)


def compare_differential(
    cobol_path: str | Path,
    java_path: str | Path,
    key_field: str | None = None,
    tolerance: float = 0.01,
    ignore_fields: set[str] | None = None,
) -> DifferentialResult:
    """
    Run a differential comparison between COBOL and Java batch outputs.

    Both files must be JSON with ``{"records": [...]}``.
    """
    ignore_fields = ignore_fields or {"FILLER"}

    with open(cobol_path, "r", encoding="utf-8") as fh:
        cobol_data = json.load(fh)
    with open(java_path, "r", encoding="utf-8") as fh:
        java_data = json.load(fh)

    cobol_records = cobol_data.get("records", [])
    java_records = java_data.get("records", [])

    result = DifferentialResult(
        cobol_path=str(cobol_path),
        java_path=str(java_path),
        cobol_count=len(cobol_records),
        java_count=len(java_records),
    )

    if key_field:
        cobol_map: dict[Any, dict] = {}
        for r in cobol_records:
            cobol_map[r.get(key_field)] = r
        java_map: dict[Any, dict] = {}
        for r in java_records:
            java_map[r.get(key_field)] = r

        all_keys = set(cobol_map.keys()) | set(java_map.keys())
        for k in sorted(all_keys, key=str):
            if k not in java_map:
                result.missing_records += 1
                result.diffs.append(DiffEntry("missing_record", key_value=k))
            elif k not in cobol_map:
                result.extra_records += 1
                result.diffs.append(DiffEntry("extra_record", key_value=k))
            else:
                record_ok = True
                for fname in set(cobol_map[k].keys()) | set(java_map[k].keys()):
                    if fname in ignore_fields or fname.startswith("_"):
                        continue
                    cv = cobol_map[k].get(fname)
                    jv = java_map[k].get(fname)
                    if not values_equal(cv, jv, tolerance):
                        result.field_mismatches += 1
                        result.diffs.append(DiffEntry(
                            "field_value_mismatch", key_value=k,
                            field_name=fname, cobol_value=cv, java_value=jv,
                        ))
                        record_ok = False
                if record_ok:
                    result.matched += 1
    else:
        # Positional comparison
        max_len = max(len(cobol_records), len(java_records))
        for i in range(max_len):
            if i >= len(java_records):
                result.missing_records += 1
                result.diffs.append(DiffEntry("missing_record", record_index=i))
            elif i >= len(cobol_records):
                result.extra_records += 1
                result.diffs.append(DiffEntry("extra_record", record_index=i))
            else:
                record_ok = True
                for fname in set(cobol_records[i].keys()) | set(java_records[i].keys()):
                    if fname in ignore_fields or fname.startswith("_"):
                        continue
                    cv = cobol_records[i].get(fname)
                    jv = java_records[i].get(fname)
                    if not values_equal(cv, jv, tolerance):
                        result.field_mismatches += 1
                        result.diffs.append(DiffEntry(
                            "field_value_mismatch", record_index=i,
                            field_name=fname, cobol_value=cv, java_value=jv,
                        ))
                        record_ok = False
                if record_ok:
                    result.matched += 1

    return result


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------
def main() -> None:
    if len(sys.argv) < 3:
        print(
            "Usage: differential_comparator.py <cobol.json> <java.json> "
            "[--key FIELD] [--tolerance N] [--ignore-fields F1,F2]"
        )
        sys.exit(1)

    cobol = sys.argv[1]
    java = sys.argv[2]
    key_field = None
    tolerance = 0.01
    ignore_fields = {"FILLER"}

    args = sys.argv[3:]
    i = 0
    while i < len(args):
        if args[i] == "--key":
            key_field = args[i + 1]
            i += 2
        elif args[i] == "--tolerance":
            tolerance = float(args[i + 1])
            i += 2
        elif args[i] == "--ignore-fields":
            ignore_fields = set(args[i + 1].split(","))
            i += 2
        else:
            i += 1

    result = compare_differential(cobol, java, key_field, tolerance, ignore_fields)
    print(result.summary())
    sys.exit(0 if result.passed else 1)


if __name__ == "__main__":
    main()
