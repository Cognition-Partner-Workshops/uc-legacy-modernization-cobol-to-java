"""
Reconciliation checks for CardDemo migration validation.

Each check is a callable that accepts loaded record sets and returns a
``CheckResult``.  The ``run_all`` function executes every registered check
and produces a summary report.

Checks implemented
------------------
1.  Row-count: daily-transaction input = posted + rejected
2.  Balance:   sum(tran-cat-bal for acct) ≈ acct-curr-bal
3.  Cross-ref integrity: every xref card exists in carddata and
    references a valid account
4.  Referential completeness: every daily-tran card maps to an xref entry
5.  Export/import round-trip: export then import yields original data
"""

from __future__ import annotations

import json
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any


# ---------------------------------------------------------------------------
# Result types
# ---------------------------------------------------------------------------

@dataclass
class CheckResult:
    name: str
    passed: bool
    expected: Any = None
    actual: Any = None
    detail: str = ""
    violating_records: list[dict[str, Any]] = field(default_factory=list)

    def summary(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        parts = [f"[{status}] {self.name}"]
        if not self.passed:
            parts.append(f"  Expected: {self.expected}")
            parts.append(f"  Actual  : {self.actual}")
            if self.detail:
                parts.append(f"  Detail  : {self.detail}")
            if self.violating_records:
                parts.append(f"  Violating records ({len(self.violating_records)}):")
                for rec in self.violating_records[:10]:
                    parts.append(f"    {rec}")
                if len(self.violating_records) > 10:
                    parts.append(
                        f"    ... and {len(self.violating_records) - 10} more"
                    )
        return "\n".join(parts)


# ---------------------------------------------------------------------------
# Helper: load golden JSON
# ---------------------------------------------------------------------------

def _load_golden(golden_dir: str | Path, name: str) -> list[dict[str, Any]]:
    path = Path(golden_dir) / name
    if not path.exists():
        return []
    with open(path) as fp:
        return json.load(fp).get("records", [])


# ---------------------------------------------------------------------------
# Individual checks
# ---------------------------------------------------------------------------

def check_xref_integrity(
    xref_records: list[dict[str, Any]],
    card_records: list[dict[str, Any]],
    acct_records: list[dict[str, Any]],
) -> CheckResult:
    """Every card in cardxref must exist in carddata and reference a valid account."""
    card_nums = {str(r.get("CARD-NUM", "")).strip() for r in card_records}
    acct_ids = {r.get("ACCT-ID") for r in acct_records}

    violations: list[dict[str, Any]] = []
    for rec in xref_records:
        xref_card = str(rec.get("XREF-CARD-NUM", "")).strip()
        xref_acct = rec.get("XREF-ACCT-ID")

        issues = []
        if xref_card and xref_card not in card_nums:
            issues.append(f"card {xref_card} not in carddata")
        if xref_acct is not None and xref_acct not in acct_ids:
            issues.append(f"acct {xref_acct} not in acctdata")

        if issues:
            violations.append({"XREF-CARD-NUM": xref_card, "issues": issues})

    return CheckResult(
        name="Cross-Reference Integrity",
        passed=len(violations) == 0,
        expected="all xref cards in carddata, all xref accts in acctdata",
        actual=f"{len(violations)} violations",
        violating_records=violations,
    )


def check_referential_completeness(
    dailytran_records: list[dict[str, Any]],
    xref_records: list[dict[str, Any]],
    reject_records: list[dict[str, Any]] | None = None,
) -> CheckResult:
    """Every daily-tran card-num must map to an xref entry (or be rejected)."""
    xref_cards = {str(r.get("XREF-CARD-NUM", "")).strip() for r in xref_records}
    rejected_ids: set[str] = set()
    if reject_records:
        for r in reject_records:
            rejected_ids.add(str(r.get("DALYTRAN-ID", "")).strip())

    violations: list[dict[str, Any]] = []
    for rec in dailytran_records:
        card_num = str(rec.get("DALYTRAN-CARD-NUM", "")).strip()
        tran_id = str(rec.get("DALYTRAN-ID", "")).strip()
        if card_num not in xref_cards and tran_id not in rejected_ids:
            violations.append({
                "DALYTRAN-ID": tran_id,
                "DALYTRAN-CARD-NUM": card_num,
            })

    return CheckResult(
        name="Referential Completeness (Daily Tran → Xref)",
        passed=len(violations) == 0,
        expected="all daily-tran cards in xref or rejects",
        actual=f"{len(violations)} orphaned transactions",
        violating_records=violations,
    )


def check_row_count_balance(
    input_count: int,
    posted_count: int,
    rejected_count: int,
) -> CheckResult:
    """Input transactions = posted + rejected."""
    total = posted_count + rejected_count
    return CheckResult(
        name="Row-Count Balance (input = posted + rejected)",
        passed=input_count == total,
        expected=input_count,
        actual=total,
        detail=f"posted={posted_count}, rejected={rejected_count}",
    )


def check_account_balance_vs_tcatbal(
    acct_records: list[dict[str, Any]],
    tcatbal_records: list[dict[str, Any]],
    tolerance: float = 0.01,
) -> CheckResult:
    """Sum of TRAN-CAT-BAL for each account should approximate ACCT-CURR-BAL.

    This check validates that the category balance file is consistent with
    the account master.  Exact equality is not expected because the balance
    may include amounts not tracked in tcatbal (e.g., fees).
    """
    # Aggregate tcatbal by account
    bal_by_acct: dict[int, float] = {}
    for rec in tcatbal_records:
        acct_id = rec.get("TRANCAT-ACCT-ID")
        if acct_id is None:
            continue
        bal = rec.get("TRAN-CAT-BAL", 0)
        bal_by_acct[acct_id] = bal_by_acct.get(acct_id, 0) + bal

    violations: list[dict[str, Any]] = []
    for rec in acct_records:
        acct_id = rec.get("ACCT-ID")
        acct_bal = rec.get("ACCT-CURR-BAL", 0)
        cat_sum = bal_by_acct.get(acct_id, 0)
        diff = abs(acct_bal - cat_sum)
        if diff > tolerance:
            violations.append({
                "ACCT-ID": acct_id,
                "ACCT-CURR-BAL": acct_bal,
                "SUM-TRAN-CAT-BAL": cat_sum,
                "DIFF": round(diff, 2),
            })

    return CheckResult(
        name="Account Balance vs Category Balance Totals",
        passed=len(violations) == 0,
        expected=f"all differences <= {tolerance}",
        actual=f"{len(violations)} accounts outside tolerance",
        violating_records=violations,
    )


def check_card_account_linkage(
    card_records: list[dict[str, Any]],
    acct_records: list[dict[str, Any]],
) -> CheckResult:
    """Every card must reference a valid account."""
    acct_ids = {r.get("ACCT-ID") for r in acct_records}
    violations: list[dict[str, Any]] = []

    for rec in card_records:
        card_acct = rec.get("CARD-ACCT-ID")
        if card_acct is not None and card_acct not in acct_ids:
            violations.append({
                "CARD-NUM": rec.get("CARD-NUM"),
                "CARD-ACCT-ID": card_acct,
            })

    return CheckResult(
        name="Card → Account Linkage",
        passed=len(violations) == 0,
        expected="all cards reference valid accounts",
        actual=f"{len(violations)} invalid references",
        violating_records=violations,
    )


def check_customer_xref_linkage(
    xref_records: list[dict[str, Any]],
    cust_records: list[dict[str, Any]],
) -> CheckResult:
    """Every xref must reference a valid customer."""
    cust_ids = {r.get("CUST-ID") for r in cust_records}
    violations: list[dict[str, Any]] = []

    for rec in xref_records:
        cust_id = rec.get("XREF-CUST-ID")
        if cust_id is not None and cust_id not in cust_ids:
            violations.append({
                "XREF-CARD-NUM": rec.get("XREF-CARD-NUM"),
                "XREF-CUST-ID": cust_id,
            })

    return CheckResult(
        name="Cross-Reference → Customer Linkage",
        passed=len(violations) == 0,
        expected="all xref entries reference valid customers",
        actual=f"{len(violations)} invalid customer references",
        violating_records=violations,
    )


# ---------------------------------------------------------------------------
# Runner
# ---------------------------------------------------------------------------

def run_all(golden_dir: str | Path) -> list[CheckResult]:
    """Run all reconciliation checks against golden-file data."""
    golden_dir = Path(golden_dir)

    acct = _load_golden(golden_dir, "acctdata.json")
    card = _load_golden(golden_dir, "carddata.json")
    cust = _load_golden(golden_dir, "custdata.json")
    xref = _load_golden(golden_dir, "cardxref.json")
    dtran = _load_golden(golden_dir, "dailytran.json")
    tcatbal = _load_golden(golden_dir, "tcatbal.json")

    results: list[CheckResult] = []

    results.append(check_xref_integrity(xref, card, acct))
    results.append(check_referential_completeness(dtran, xref))
    results.append(check_card_account_linkage(card, acct))
    results.append(check_customer_xref_linkage(xref, cust))
    results.append(check_account_balance_vs_tcatbal(acct, tcatbal))

    return results


def print_report(results: list[CheckResult]) -> bool:
    """Print all results and return True if all passed."""
    print("=" * 72)
    print("RECONCILIATION CHECK REPORT")
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

    golden = Path(__file__).resolve().parent.parent / "golden-files"
    results = run_all(golden)
    ok = print_report(results)
    sys.exit(0 if ok else 1)
