"""
Reconciliation check functions for CardDemo batch migration validation.

Provides record-count validation, numeric-field sum validation, and
cross-reference integrity checks.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any


@dataclass
class CheckResult:
    """Outcome of a single reconciliation check."""

    name: str
    passed: bool
    expected: Any = None
    actual: Any = None
    details: str = ""

    def __str__(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        msg = f"[{status}] {self.name}"
        if not self.passed:
            msg += f" — expected={self.expected!r}, actual={self.actual!r}"
            if self.details:
                msg += f" ({self.details})"
        return msg


@dataclass
class ReconciliationReport:
    """Collection of reconciliation check results."""

    checks: list[CheckResult] = field(default_factory=list)

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
        lines = [f"Reconciliation: {self.pass_count} passed, {self.fail_count} failed"]
        for c in self.checks:
            lines.append(f"  {c}")
        return "\n".join(lines)


# ── Record Count Checks ────────────────────────────────────────────────────

def check_record_count(
    records: list[dict[str, Any]],
    expected_count: int,
    name: str,
) -> CheckResult:
    """Verify that a parsed file has exactly the expected number of records."""
    actual = len(records)
    return CheckResult(
        name=name,
        passed=actual == expected_count,
        expected=expected_count,
        actual=actual,
    )


def check_posttran_counts(
    input_daily: list[dict[str, Any]],
    posted: list[dict[str, Any]],
    rejected: list[dict[str, Any]],
) -> CheckResult:
    """POSTTRAN: input == posted + rejected."""
    total_in = len(input_daily)
    total_out = len(posted) + len(rejected)
    return CheckResult(
        name="POSTTRAN record count (input == posted + rejected)",
        passed=total_in == total_out,
        expected=total_in,
        actual=total_out,
        details=f"posted={len(posted)}, rejected={len(rejected)}",
    )


# ── Numeric Sum Checks ─────────────────────────────────────────────────────

def sum_field(
    records: list[dict[str, Any]],
    field_name: str,
) -> float:
    """Sum a numeric field across all records."""
    total = 0.0
    for rec in records:
        val = rec.get(field_name, 0)
        if isinstance(val, (int, float)):
            total += val
    return round(total, 2)


def check_sum_balance(
    records_a: list[dict[str, Any]],
    field_a: str,
    records_b: list[dict[str, Any]],
    field_b: str,
    name: str,
    tolerance: float = 0.01,
) -> CheckResult:
    """Check that the sum of field_a across records_a equals the sum of field_b
    across records_b, within a tolerance."""
    sum_a = sum_field(records_a, field_a)
    sum_b = sum_field(records_b, field_b)
    return CheckResult(
        name=name,
        passed=abs(sum_a - sum_b) <= tolerance,
        expected=sum_a,
        actual=sum_b,
        details=f"difference={round(sum_a - sum_b, 2)}",
    )


def check_transaction_amount_conservation(
    input_daily: list[dict[str, Any]],
    posted: list[dict[str, Any]],
    rejected: list[dict[str, Any]],
    amt_field_in: str = "DALYTRAN-AMT",
    amt_field_posted: str = "TRAN-AMT",
    amt_field_rejected: str = "DALYTRAN-AMT",
    tolerance: float = 0.01,
) -> CheckResult:
    """Sum of input amounts == sum of posted amounts + sum of rejected amounts."""
    total_in = sum_field(input_daily, amt_field_in)
    total_posted = sum_field(posted, amt_field_posted)
    total_rejected = sum_field(rejected, amt_field_rejected)
    total_out = round(total_posted + total_rejected, 2)
    return CheckResult(
        name="Transaction amount conservation (input == posted + rejected)",
        passed=abs(total_in - total_out) <= tolerance,
        expected=total_in,
        actual=total_out,
        details=f"posted_sum={total_posted}, rejected_sum={total_rejected}",
    )


# ── Cross-Reference Integrity Checks ───────────────────────────────────────

def _extract_keys(
    records: list[dict[str, Any]],
    key_field: str,
) -> set[str]:
    """Extract unique key values as strings from a list of records."""
    keys: set[str] = set()
    for rec in records:
        val = rec.get(key_field)
        if val is not None:
            keys.add(str(val).strip())
    return keys


def check_referential_integrity(
    child_records: list[dict[str, Any]],
    child_key: str,
    parent_records: list[dict[str, Any]],
    parent_key: str,
    name: str,
) -> CheckResult:
    """Every value of child_key in child_records must exist as parent_key in
    parent_records (foreign-key style check)."""
    child_keys = _extract_keys(child_records, child_key)
    parent_keys = _extract_keys(parent_records, parent_key)
    orphans = child_keys - parent_keys
    return CheckResult(
        name=name,
        passed=len(orphans) == 0,
        expected=0,
        actual=len(orphans),
        details=f"orphan keys (first 10): {sorted(orphans)[:10]}" if orphans else "",
    )


def check_xref_card_in_carddata(
    xref_records: list[dict[str, Any]],
    card_records: list[dict[str, Any]],
) -> CheckResult:
    """Every XREF-CARD-NUM must exist in CARD-NUM."""
    return check_referential_integrity(
        xref_records, "XREF-CARD-NUM",
        card_records, "CARD-NUM",
        "XREF card numbers exist in card master",
    )


def check_xref_acct_in_acctdata(
    xref_records: list[dict[str, Any]],
    acct_records: list[dict[str, Any]],
) -> CheckResult:
    """Every XREF-ACCT-ID must exist in ACCT-ID."""
    return check_referential_integrity(
        xref_records, "XREF-ACCT-ID",
        acct_records, "ACCT-ID",
        "XREF account IDs exist in account master",
    )


def check_xref_cust_in_custdata(
    xref_records: list[dict[str, Any]],
    cust_records: list[dict[str, Any]],
) -> CheckResult:
    """Every XREF-CUST-ID must exist in CUST-ID."""
    return check_referential_integrity(
        xref_records, "XREF-CUST-ID",
        cust_records, "CUST-ID",
        "XREF customer IDs exist in customer master",
    )


def check_tcatbal_acct_in_acctdata(
    tcatbal_records: list[dict[str, Any]],
    acct_records: list[dict[str, Any]],
) -> CheckResult:
    """Every TRANCAT-ACCT-ID must exist in ACCT-ID."""
    return check_referential_integrity(
        tcatbal_records, "TRANCAT-ACCT-ID",
        acct_records, "ACCT-ID",
        "TCATBAL account IDs exist in account master",
    )


def check_dailytran_card_in_xref(
    dailytran_records: list[dict[str, Any]],
    xref_records: list[dict[str, Any]],
) -> CheckResult:
    """Every DALYTRAN-CARD-NUM should exist in XREF-CARD-NUM."""
    return check_referential_integrity(
        dailytran_records, "DALYTRAN-CARD-NUM",
        xref_records, "XREF-CARD-NUM",
        "Daily transaction card numbers exist in XREF",
    )


# ── Full Reconciliation Suite ──────────────────────────────────────────────

def run_data_integrity_checks(
    acct_records: list[dict[str, Any]],
    card_records: list[dict[str, Any]],
    xref_records: list[dict[str, Any]],
    cust_records: list[dict[str, Any]],
    dailytran_records: list[dict[str, Any]],
    tcatbal_records: list[dict[str, Any]],
) -> ReconciliationReport:
    """Run the full suite of cross-reference integrity and count checks."""
    report = ReconciliationReport()

    report.checks.append(check_record_count(acct_records, 50, "Account file has 50 records"))
    report.checks.append(check_record_count(card_records, 50, "Card file has 50 records"))
    report.checks.append(check_record_count(xref_records, 50, "XREF file has 50 records"))
    report.checks.append(check_record_count(cust_records, 50, "Customer file has 50 records"))
    report.checks.append(check_record_count(dailytran_records, 300, "Daily transaction file has 300 records"))
    report.checks.append(check_record_count(tcatbal_records, 50, "TCATBAL file has 50 records"))

    report.checks.append(check_xref_card_in_carddata(xref_records, card_records))
    report.checks.append(check_xref_acct_in_acctdata(xref_records, acct_records))
    report.checks.append(check_xref_cust_in_custdata(xref_records, cust_records))
    report.checks.append(check_tcatbal_acct_in_acctdata(tcatbal_records, acct_records))
    report.checks.append(check_dailytran_card_in_xref(dailytran_records, xref_records))

    return report
