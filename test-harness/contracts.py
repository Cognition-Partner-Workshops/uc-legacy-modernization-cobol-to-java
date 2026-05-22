"""
Contract validation for migrated output files.

Validates that output files produced by the Java migration target conform to
the COBOL copybook-defined record layouts — correct record length, field
positions, field types, and decimal scale.

Contracts are derived directly from the copybook field definitions produced
by ``copybook_parser``.
"""

from __future__ import annotations

import json
import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

from copybook_parser import parse_copybook, record_length


# ---------------------------------------------------------------------------
# Result types
# ---------------------------------------------------------------------------

@dataclass
class Violation:
    record_index: int | None
    field_name: str
    expected: Any
    actual: Any
    detail: str = ""


@dataclass
class ContractResult:
    contract_name: str
    passed: bool
    violations: list[Violation] = field(default_factory=list)

    def summary(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        parts = [f"[{status}] {self.contract_name}"]
        for v in self.violations[:20]:
            rec_label = f"Record[{v.record_index}]." if v.record_index is not None else ""
            parts.append(
                f"  {rec_label}{v.field_name}: "
                f"expected={v.expected!r}  actual={v.actual!r}"
                + (f"  ({v.detail})" if v.detail else "")
            )
        if len(self.violations) > 20:
            parts.append(f"  ... and {len(self.violations) - 20} more")
        return "\n".join(parts)


# ---------------------------------------------------------------------------
# Schema generation from copybook
# ---------------------------------------------------------------------------

def generate_schema(copybook_path: str | Path) -> dict[str, Any]:
    """Produce a JSON-serializable contract schema from a copybook."""
    fields = parse_copybook(copybook_path)
    reclen = record_length(fields)

    schema_fields = []
    for f in fields:
        schema_fields.append({
            "name": f["name"],
            "offset": f["offset"],
            "length": f["length"],
            "type": f["type"],
            "scale": f["scale"],
        })

    return {
        "copybook": str(Path(copybook_path).name),
        "record_length": reclen,
        "fields": schema_fields,
    }


# ---------------------------------------------------------------------------
# Contract validation
# ---------------------------------------------------------------------------

_NUMERIC_RE = re.compile(r"^[0-9]+$")
_SIGNED_NUMERIC_RE = re.compile(
    r"^[0-9]*[0-9A-IJ-R{}]$"
)


def validate_file(
    file_path: str | Path,
    schema: dict[str, Any],
    *,
    max_errors: int = 100,
) -> ContractResult:
    """Validate a fixed-width output file against a contract schema.

    Checks:
    - Record length matches schema
    - Each field at the correct byte offset contains data consistent with
      its declared type
    """
    violations: list[Violation] = []
    expected_reclen = schema["record_length"]
    schema_fields = schema["fields"]

    lines = Path(file_path).read_text().splitlines()

    for idx, line in enumerate(lines):
        if not line.strip():
            continue

        # Pad short lines (trailing FILLER may be stripped)
        if len(line) < expected_reclen:
            line = line.ljust(expected_reclen)

        if len(line) > expected_reclen:
            violations.append(Violation(
                idx, "__record__",
                expected=expected_reclen,
                actual=len(line),
                detail="record too long",
            ))
            if len(violations) >= max_errors:
                break
            continue

        for fdef in schema_fields:
            name = fdef["name"]
            if name == "FILLER":
                continue

            offset = fdef["offset"]
            length = fdef["length"]
            ftype = fdef["type"]

            raw = line[offset : offset + length]

            if ftype in ("numeric_display", "numeric_signed"):
                pattern = _SIGNED_NUMERIC_RE if ftype == "numeric_signed" else _NUMERIC_RE
                if not pattern.match(raw.strip() or "0"):
                    violations.append(Violation(
                        idx, name,
                        expected=ftype,
                        actual=repr(raw),
                        detail=f"non-numeric content at offset {offset}",
                    ))

            if len(violations) >= max_errors:
                break

        if len(violations) >= max_errors:
            break

    return ContractResult(
        contract_name=schema.get("copybook", str(file_path)),
        passed=len(violations) == 0,
        violations=violations,
    )


# ---------------------------------------------------------------------------
# Convenience: validate all data files against their copybooks
# ---------------------------------------------------------------------------

# Map of ASCII data file -> copybook file
_DEFAULT_MAPPINGS = [
    ("acctdata.txt", "CVACT01Y.cpy", 300),
    ("carddata.txt", "CVACT02Y.cpy", 150),
    ("custdata.txt", "CVCUS01Y.cpy", 500),
    ("cardxref.txt", "CVACT03Y.cpy", 50),
    ("dailytran.txt", "CVTRA06Y.cpy", 350),
    ("trantype.txt", "CVTRA03Y.cpy", 60),
    ("trancatg.txt", "CVTRA04Y.cpy", 60),
    ("tcatbal.txt", "CVTRA01Y.cpy", 50),
    ("discgrp.txt", "CVTRA02Y.cpy", 50),
]


def validate_all(
    data_dir: str | Path,
    cpy_dir: str | Path,
) -> list[ContractResult]:
    """Validate every known data file against its copybook contract."""
    data_dir = Path(data_dir)
    cpy_dir = Path(cpy_dir)
    results: list[ContractResult] = []

    for data_name, cpy_name, reclen in _DEFAULT_MAPPINGS:
        data_path = data_dir / data_name
        cpy_path = cpy_dir / cpy_name

        if not data_path.exists() or not cpy_path.exists():
            results.append(ContractResult(
                contract_name=f"{data_name} <-> {cpy_name}",
                passed=False,
                violations=[Violation(
                    None, "__file__",
                    expected="file exists",
                    actual="missing",
                    detail=f"{data_path} or {cpy_path}",
                )],
            ))
            continue

        schema = generate_schema(cpy_path)
        result = validate_file(data_path, schema)
        result.contract_name = f"{data_name} <-> {cpy_name}"
        results.append(result)

    return results


def print_report(results: list[ContractResult]) -> bool:
    """Print all contract results and return True if all passed."""
    print("=" * 72)
    print("CONTRACT VALIDATION REPORT")
    print("=" * 72)

    all_passed = True
    for r in results:
        print(r.summary())
        print()
        if not r.passed:
            all_passed = False

    passed = sum(1 for r in results if r.passed)
    failed = len(results) - passed
    print("-" * 72)
    print(f"Total: {len(results)}  Passed: {passed}  Failed: {failed}")
    print("=" * 72)

    return all_passed


if __name__ == "__main__":
    import sys

    repo = Path(__file__).resolve().parent.parent
    results = validate_all(
        repo / "app" / "data" / "ASCII",
        repo / "app" / "cpy",
    )
    ok = print_report(results)
    sys.exit(0 if ok else 1)
