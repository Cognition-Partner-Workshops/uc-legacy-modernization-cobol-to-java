"""Reconciliation check functions for COBOL-to-Java migration testing.

Provides record count validation, numeric field sum validation,
and cross-reference integrity checks across CardDemo data files.
"""

from __future__ import annotations

import json
from dataclasses import dataclass
from dataclasses import field as dataclass_field
from decimal import Decimal, InvalidOperation
from pathlib import Path
from typing import Any


@dataclass
class CheckResult:
    """Result of a single reconciliation check."""

    check_name: str
    passed: bool
    expected: str
    actual: str
    details: str = ""

    def to_dict(self) -> dict[str, Any]:
        return {
            "check_name": self.check_name,
            "passed": self.passed,
            "expected": self.expected,
            "actual": self.actual,
            "details": self.details,
        }


@dataclass
class ReconciliationReport:
    """Aggregated result of all reconciliation checks."""

    checks: list[CheckResult] = dataclass_field(default_factory=list)

    @property
    def all_passed(self) -> bool:
        return all(c.passed for c in self.checks)

    @property
    def pass_count(self) -> int:
        return sum(1 for c in self.checks if c.passed)

    @property
    def fail_count(self) -> int:
        return sum(1 for c in self.checks if not c.passed)

    def summary(self) -> str:
        total = len(self.checks)
        lines = [
            f"Reconciliation Report: {'ALL PASSED' if self.all_passed else 'FAILURES DETECTED'}",
            f"  Total checks: {total}",
            f"  Passed: {self.pass_count}",
            f"  Failed: {self.fail_count}",
        ]
        if not self.all_passed:
            lines.append("  Failed checks:")
            for c in self.checks:
                if not c.passed:
                    lines.append(f"    - {c.check_name}: expected={c.expected}, actual={c.actual}")
                    if c.details:
                        lines.append(f"      {c.details}")
        return "\n".join(lines)

    def to_dict(self) -> dict[str, Any]:
        return {
            "status": "PASS" if self.all_passed else "FAIL",
            "total_checks": len(self.checks),
            "passed": self.pass_count,
            "failed": self.fail_count,
            "checks": [c.to_dict() for c in self.checks],
        }


def _load_records(path: str | Path) -> list[dict[str, Any]]:
    """Load records from a golden-file JSON."""
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    return data.get("records", [])


def _to_decimal(value: Any) -> Decimal:
    """Convert a value to Decimal, returning 0 on failure."""
    try:
        return Decimal(str(value).strip())
    except (InvalidOperation, ValueError):
        return Decimal("0")


# ---------------------------------------------------------------------------
# Record count validation
# ---------------------------------------------------------------------------


def check_record_count(
    records: list[dict[str, Any]],
    expected_count: int,
    file_name: str,
) -> CheckResult:
    """Validate that a file contains the expected number of records."""
    actual_count = len(records)
    return CheckResult(
        check_name=f"Record count: {file_name}",
        passed=actual_count == expected_count,
        expected=str(expected_count),
        actual=str(actual_count),
    )


def check_record_count_from_file(
    path: str | Path,
    expected_count: int,
) -> CheckResult:
    """Load a golden-file JSON and validate record count."""
    records = _load_records(path)
    return check_record_count(records, expected_count, Path(path).name)


# ---------------------------------------------------------------------------
# Numeric field sum validation
# ---------------------------------------------------------------------------


def check_field_sum(
    records: list[dict[str, Any]],
    field_name: str,
    expected_sum: Decimal | str | None = None,
    file_name: str = "",
) -> CheckResult:
    """Sum a numeric field across all records and optionally validate against expected.

    If expected_sum is None, the check always passes (used for baseline capture).
    """
    total = Decimal("0")
    for rec in records:
        val = rec.get(field_name)
        if val is not None:
            total += _to_decimal(val)

    if expected_sum is not None:
        exp = Decimal(str(expected_sum))
        passed = total == exp
    else:
        passed = True
        exp = total

    return CheckResult(
        check_name=f"Field sum: {file_name}.{field_name}",
        passed=passed,
        expected=str(exp),
        actual=str(total),
    )


def compute_field_sums(
    records: list[dict[str, Any]],
    field_names: list[str],
) -> dict[str, Decimal]:
    """Compute sums for multiple numeric fields. Returns a dict of field_name -> sum."""
    sums: dict[str, Decimal] = {}
    for field_name in field_names:
        total = Decimal("0")
        for rec in records:
            val = rec.get(field_name)
            if val is not None:
                total += _to_decimal(val)
        sums[field_name] = total
    return sums


# ---------------------------------------------------------------------------
# Cross-reference integrity checks
# ---------------------------------------------------------------------------


def check_referential_integrity(
    child_records: list[dict[str, Any]],
    child_field: str,
    parent_records: list[dict[str, Any]],
    parent_field: str,
    check_name: str = "",
) -> CheckResult:
    """Verify that every value of child_field exists in parent_field.

    This validates foreign-key-like relationships between data files.
    """
    parent_values = {str(rec.get(parent_field, "")).strip() for rec in parent_records}
    # Remove empty values from parent set
    parent_values.discard("")

    missing: list[str] = []
    for i, rec in enumerate(child_records):
        child_val = str(rec.get(child_field, "")).strip()
        if child_val and child_val not in parent_values:
            missing.append(f"record[{i}].{child_field}={child_val}")

    passed = len(missing) == 0
    details = ""
    if missing:
        sample = missing[:10]
        details = f"Missing references ({len(missing)} total): {', '.join(sample)}"
        if len(missing) > 10:
            details += f" ... and {len(missing) - 10} more"

    return CheckResult(
        check_name=check_name or f"Referential integrity: {child_field} -> {parent_field}",
        passed=passed,
        expected="0 missing references",
        actual=f"{len(missing)} missing references",
        details=details,
    )


# ---------------------------------------------------------------------------
# CardDemo-specific reconciliation suite
# ---------------------------------------------------------------------------


def run_carddemo_reconciliation(
    golden_files_dir: str | Path,
) -> ReconciliationReport:
    """Run the full CardDemo reconciliation suite against golden-file JSONs.

    Expects the following files in golden_files_dir:
      acctdata.json, carddata.json, cardxref.json, custdata.json,
      dailytran.json, tcatbal.json, trancatg.json, trantype.json, discgrp.json
    """
    gdir = Path(golden_files_dir)
    report = ReconciliationReport()

    # Load all record sets
    acct_records = _load_records(gdir / "acctdata.json")
    card_records = _load_records(gdir / "carddata.json")
    xref_records = _load_records(gdir / "cardxref.json")
    cust_records = _load_records(gdir / "custdata.json")
    tran_records = _load_records(gdir / "dailytran.json")
    tcat_records = _load_records(gdir / "tcatbal.json")
    catg_records = _load_records(gdir / "trancatg.json")
    type_records = _load_records(gdir / "trantype.json")
    disc_records = _load_records(gdir / "discgrp.json")

    # -----------------------------------------------------------------------
    # 1. Record count validation
    # -----------------------------------------------------------------------
    report.checks.append(check_record_count(acct_records, 50, "acctdata.txt"))
    report.checks.append(check_record_count(card_records, 50, "carddata.txt"))
    report.checks.append(check_record_count(xref_records, 50, "cardxref.txt"))
    report.checks.append(check_record_count(cust_records, 50, "custdata.txt"))
    report.checks.append(check_record_count(tran_records, 300, "dailytran.txt"))
    report.checks.append(check_record_count(tcat_records, 50, "tcatbal.txt"))
    report.checks.append(check_record_count(catg_records, 18, "trancatg.txt"))
    report.checks.append(check_record_count(type_records, 7, "trantype.txt"))
    report.checks.append(check_record_count(disc_records, 51, "discgrp.txt"))

    # -----------------------------------------------------------------------
    # 2. Numeric field sum validation (baseline capture -- no expected values)
    # -----------------------------------------------------------------------
    acct_sum_fields = [
        "ACCT-CURR-BAL",
        "ACCT-CREDIT-LIMIT",
        "ACCT-CASH-CREDIT-LIMIT",
        "ACCT-CURR-CYC-CREDIT",
        "ACCT-CURR-CYC-DEBIT",
    ]
    for field_name in acct_sum_fields:
        report.checks.append(
            check_field_sum(acct_records, field_name, file_name="acctdata")
        )

    report.checks.append(
        check_field_sum(tran_records, "DALYTRAN-AMT", file_name="dailytran")
    )
    report.checks.append(
        check_field_sum(tcat_records, "TRAN-CAT-BAL", file_name="tcatbal")
    )
    report.checks.append(
        check_field_sum(disc_records, "DIS-INT-RATE", file_name="discgrp")
    )

    # -----------------------------------------------------------------------
    # 3. Cross-reference integrity checks
    # -----------------------------------------------------------------------

    # Every card xref account ID must exist in account data
    report.checks.append(check_referential_integrity(
        child_records=xref_records,
        child_field="XREF-ACCT-ID",
        parent_records=acct_records,
        parent_field="ACCT-ID",
        check_name="Card xref -> Account: XREF-ACCT-ID exists in ACCT-ID",
    ))

    # Every card xref customer ID must exist in customer data
    report.checks.append(check_referential_integrity(
        child_records=xref_records,
        child_field="XREF-CUST-ID",
        parent_records=cust_records,
        parent_field="CUST-ID",
        check_name="Card xref -> Customer: XREF-CUST-ID exists in CUST-ID",
    ))

    # Every card xref card number must exist in card data
    report.checks.append(check_referential_integrity(
        child_records=xref_records,
        child_field="XREF-CARD-NUM",
        parent_records=card_records,
        parent_field="CARD-NUM",
        check_name="Card xref -> Card: XREF-CARD-NUM exists in CARD-NUM",
    ))

    # Every daily transaction card number must exist in card data
    report.checks.append(check_referential_integrity(
        child_records=tran_records,
        child_field="DALYTRAN-CARD-NUM",
        parent_records=card_records,
        parent_field="CARD-NUM",
        check_name="Transaction -> Card: DALYTRAN-CARD-NUM exists in CARD-NUM",
    ))

    # Every transaction category balance account ID must exist in account data
    report.checks.append(check_referential_integrity(
        child_records=tcat_records,
        child_field="TRANCAT-ACCT-ID",
        parent_records=acct_records,
        parent_field="ACCT-ID",
        check_name="Category balance -> Account: TRANCAT-ACCT-ID exists in ACCT-ID",
    ))

    # Every card's account ID must exist in account data
    report.checks.append(check_referential_integrity(
        child_records=card_records,
        child_field="CARD-ACCT-ID",
        parent_records=acct_records,
        parent_field="ACCT-ID",
        check_name="Card -> Account: CARD-ACCT-ID exists in ACCT-ID",
    ))

    return report


# ---------------------------------------------------------------------------
# CLI entry point
# ---------------------------------------------------------------------------


def main() -> None:
    """Command-line interface for running reconciliation checks."""
    import argparse

    parser = argparse.ArgumentParser(
        description="Run reconciliation checks on CardDemo golden files."
    )
    parser.add_argument(
        "--golden-dir",
        required=True,
        help="Path to the directory containing golden-file JSONs",
    )
    parser.add_argument(
        "--output",
        required=False,
        help="Path to write the reconciliation report JSON (default: stdout)",
    )

    args = parser.parse_args()

    report = run_carddemo_reconciliation(args.golden_dir)

    print(report.summary())
    print()

    report_json = json.dumps(report.to_dict(), indent=2)

    if args.output:
        Path(args.output).write_text(report_json + "\n", encoding="utf-8")
        print(f"Report written to {args.output}")
    else:
        print(report_json)


if __name__ == "__main__":
    main()
