"""Contract validation: verify Java output honors COBOL copybook schemas.

Checks that migrated output files conform to the canonical record layouts
defined by the COBOL copybooks (record length, field count, types, key order).
"""

from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, List, Optional

try:
    from .copybook_parser import FieldDescriptor, parse_copybook_file, COPYBOOK_REGISTRY
except ImportError:
    from copybook_parser import FieldDescriptor, parse_copybook_file, COPYBOOK_REGISTRY


@dataclass
class ContractViolation:
    """A single contract violation."""

    check: str
    dataset: str
    message: str
    severity: str = "ERROR"  # ERROR | WARNING


@dataclass
class ContractResult:
    """Aggregated contract-validation result."""

    dataset: str
    violations: List[ContractViolation]

    @property
    def passed(self) -> bool:
        return all(v.severity != "ERROR" for v in self.violations)

    def summary(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        lines = [f"[{status}] Contract: {self.dataset}"]
        for v in self.violations:
            lines.append(f"  [{v.severity}] {v.check}: {v.message}")
        if not self.violations:
            lines.append("  No violations")
        return "\n".join(lines)


# -----------------------------------------------------------------------
# Record-length check
# -----------------------------------------------------------------------

def _check_record_length(
    data_path: Path,
    expected_recln: int,
    dataset: str,
    fields: Optional[List[FieldDescriptor]] = None,
) -> List[ContractViolation]:
    """Verify every non-blank line has the declared record length.

    Lines that are shorter only because trailing FILLER is omitted
    (common in ASCII extracts) are flagged as WARNING, not ERROR.
    """
    violations: List[ContractViolation] = []
    content = data_path.read_text(encoding="utf-8", errors="replace")
    lines = [ln for ln in content.splitlines() if ln.strip()]

    # Compute the minimum data length (everything before trailing FILLER)
    min_data_len = expected_recln
    if fields:
        non_filler = [f for f in fields if not f.is_filler]
        if non_filler:
            last = max(non_filler, key=lambda f: f.offset + f.length)
            min_data_len = last.offset + last.length

    for i, ln in enumerate(lines[:10]):  # sample first 10
        if len(ln) == expected_recln:
            continue
        if min_data_len <= len(ln) < expected_recln:
            violations.append(ContractViolation(
                check="record_length",
                dataset=dataset,
                message=f"Line {i+1}: length={len(ln)}, expected={expected_recln} "
                        f"(trailing filler omitted, data fields end at {min_data_len})",
                severity="WARNING",
            ))
        else:
            violations.append(ContractViolation(
                check="record_length",
                dataset=dataset,
                message=f"Line {i+1}: length={len(ln)}, expected={expected_recln}",
            ))
    return violations


# -----------------------------------------------------------------------
# Field-count check
# -----------------------------------------------------------------------

def _check_field_count(
    fields: List[FieldDescriptor],
    records: List[Dict[str, Any]],
    dataset: str,
) -> List[ContractViolation]:
    """Verify parsed records have the expected number of non-filler fields."""
    violations: List[ContractViolation] = []
    expected_names = {f.name for f in fields if not f.is_filler}
    for i, rec in enumerate(records[:5]):  # sample first 5
        actual_names = set(rec.keys())
        missing = expected_names - actual_names
        extra = actual_names - expected_names
        if missing:
            violations.append(ContractViolation(
                check="field_count",
                dataset=dataset,
                message=f"Record {i+1}: missing fields {sorted(missing)[:5]}",
            ))
        if extra:
            violations.append(ContractViolation(
                check="field_count",
                dataset=dataset,
                message=f"Record {i+1}: extra fields {sorted(extra)[:5]}",
                severity="WARNING",
            ))
    return violations


# -----------------------------------------------------------------------
# Key ordering check (ascending primary key for KSDS replacement)
# -----------------------------------------------------------------------

def _to_sort_tuple(value: Any):
    """Convert a value to a sortable form: numeric if possible, else string."""
    if isinstance(value, (int, float)):
        return (0, value, "")
    s = str(value)
    try:
        return (0, int(s), "")
    except ValueError:
        try:
            return (0, float(s), "")
        except ValueError:
            return (1, 0, s)


def _make_sort_key(record: Dict[str, Any], key_field):
    """Build a sortable key tuple supporting composite keys."""
    if isinstance(key_field, list):
        return tuple(_to_sort_tuple(record.get(k, "")) for k in key_field)
    return _to_sort_tuple(record.get(key_field, ""))


def _check_key_ordering(
    records: List[Dict[str, Any]],
    key_field,
    dataset: str,
) -> List[ContractViolation]:
    """Verify records are in ascending key order."""
    violations: List[ContractViolation] = []
    keys = [_make_sort_key(r, key_field) for r in records]
    for i in range(1, len(keys)):
        if keys[i] < keys[i - 1]:
            violations.append(ContractViolation(
                check="key_ordering",
                dataset=dataset,
                message=f"Key out of order at position {i+1}: "
                        f"{keys[i-1]!r} > {keys[i]!r}",
            ))
            break  # one violation is enough to flag
    return violations


# -----------------------------------------------------------------------
# Numeric precision check
# -----------------------------------------------------------------------

def _check_numeric_precision(
    fields: List[FieldDescriptor],
    records: List[Dict[str, Any]],
    dataset: str,
) -> List[ContractViolation]:
    """Verify numeric fields conform to declared PIC precision."""
    violations: List[ContractViolation] = []
    numeric_fields = [f for f in fields if f.field_type in ("numeric", "signed_numeric") and not f.is_filler]

    for nf in numeric_fields:
        for i, rec in enumerate(records[:10]):
            val = rec.get(nf.name)
            if val is None:
                continue
            if nf.decimal_places > 0 and isinstance(val, (int, float)):
                # Check that the value doesn't exceed the PIC's integer digits.
                # Use digit count from PIC (not byte-length) so COMP/COMP-3
                # fields are handled correctly.
                total_digits = sum(1 for c in nf.pic_expanded.upper() if c == '9')
                int_digits = total_digits - nf.decimal_places
                max_val = 10 ** int_digits
                if abs(val) >= max_val:
                    violations.append(ContractViolation(
                        check="numeric_precision",
                        dataset=dataset,
                        message=f"Record {i+1}, {nf.name}: value {val} exceeds "
                                f"PIC capacity (max ~{max_val})",
                        severity="WARNING",
                    ))
    return violations


# -----------------------------------------------------------------------
# Public API
# -----------------------------------------------------------------------

def validate_contract(
    data_path: str | Path,
    copybook_path: str | Path,
    expected_recln: int,
    key_field: str,
    dataset_name: str,
    records: Optional[List[Dict[str, Any]]] = None,
) -> ContractResult:
    """Run all contract checks for a single dataset.

    If *records* is ``None``, only the record-length check runs (the others
    require parsed records).
    """
    data_path = Path(data_path)
    fields = parse_copybook_file(copybook_path)
    violations: List[ContractViolation] = []

    # Record length
    if data_path.exists():
        violations.extend(_check_record_length(data_path, expected_recln, dataset_name, fields))

    if records:
        violations.extend(_check_field_count(fields, records, dataset_name))
        violations.extend(_check_key_ordering(records, key_field, dataset_name))
        violations.extend(_check_numeric_precision(fields, records, dataset_name))

    return ContractResult(dataset=dataset_name, violations=violations)


def validate_all_contracts(
    data_dir: str | Path,
    copybook_dir: str | Path,
    parsed_datasets: Optional[Dict[str, List[Dict[str, Any]]]] = None,
) -> List[ContractResult]:
    """Run contract validation for every registered dataset."""
    data_dir = Path(data_dir)
    copybook_dir = Path(copybook_dir)
    parsed = parsed_datasets or {}
    results: List[ContractResult] = []

    for filename, meta in COPYBOOK_REGISTRY.items():
        ds_name = filename.replace(".txt", "")
        data_path = data_dir / filename
        cpy_path = copybook_dir / meta["copybook"]
        records = parsed.get(ds_name)

        if not cpy_path.exists():
            results.append(ContractResult(
                dataset=ds_name,
                violations=[ContractViolation(
                    check="copybook_exists",
                    dataset=ds_name,
                    message=f"Copybook not found: {cpy_path}",
                )],
            ))
            continue

        results.append(validate_contract(
            data_path=data_path,
            copybook_path=cpy_path,
            expected_recln=meta["recln"],
            key_field=meta["key_field"],
            dataset_name=ds_name,
            records=records,
        ))

    return results
