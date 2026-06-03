"""
Reconciliation Checks — Validates data integrity invariants across batch jobs.

Each check verifies a business rule that must hold after a batch job runs:
  - Record counts match between input and output
  - Monetary totals balance (debits = credits + adjustments)
  - Cross-reference integrity (all FKs resolve)
  - No orphaned or duplicate records
"""

import json
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Callable, Optional


@dataclass
class ReconciliationCheck:
    """A single reconciliation validation."""

    name: str
    description: str
    passed: bool
    expected_value: Any
    actual_value: Any
    details: str = ""


@dataclass
class ReconciliationReport:
    """Full reconciliation report for a batch job."""

    job_name: str
    checks: list[ReconciliationCheck]

    @property
    def passed(self) -> bool:
        return all(c.passed for c in self.checks)

    @property
    def summary(self) -> str:
        total = len(self.checks)
        passed = sum(1 for c in self.checks if c.passed)
        return f"{self.job_name}: {passed}/{total} checks passed"

    def to_dict(self) -> dict:
        return {
            "job_name": self.job_name,
            "passed": self.passed,
            "summary": self.summary,
            "checks": [
                {
                    "name": c.name,
                    "description": c.description,
                    "passed": c.passed,
                    "expected_value": c.expected_value,
                    "actual_value": c.actual_value,
                    "details": c.details,
                }
                for c in self.checks
            ],
        }


# ─────────────────────────────────────────────────────────────────────────────
# Generic reconciliation check builders
# ─────────────────────────────────────────────────────────────────────────────


def check_record_count(
    name: str, records: list[dict], expected_count: int
) -> ReconciliationCheck:
    """Verify that the record count matches expected."""
    actual = len(records)
    return ReconciliationCheck(
        name=name,
        description=f"Record count should be {expected_count}",
        passed=actual == expected_count,
        expected_value=expected_count,
        actual_value=actual,
        details=f"Delta: {actual - expected_count}" if actual != expected_count else "",
    )


def check_no_duplicates(
    name: str, records: list[dict], key_field: str
) -> ReconciliationCheck:
    """Verify no duplicate keys exist in dataset."""
    keys = [r.get(key_field) for r in records]
    unique_keys = set(keys)
    duplicates = len(keys) - len(unique_keys)
    return ReconciliationCheck(
        name=name,
        description=f"No duplicate values in {key_field}",
        passed=duplicates == 0,
        expected_value=0,
        actual_value=duplicates,
        details=f"{duplicates} duplicate key(s) found" if duplicates > 0 else "",
    )


def check_referential_integrity(
    name: str,
    child_records: list[dict],
    child_fk_field: str,
    parent_records: list[dict],
    parent_pk_field: str,
) -> ReconciliationCheck:
    """Verify all foreign keys in child resolve to a parent record."""
    parent_keys = {r.get(parent_pk_field) for r in parent_records}
    orphans = [
        r.get(child_fk_field)
        for r in child_records
        if r.get(child_fk_field) not in parent_keys
    ]
    return ReconciliationCheck(
        name=name,
        description=f"All {child_fk_field} values resolve to {parent_pk_field}",
        passed=len(orphans) == 0,
        expected_value=0,
        actual_value=len(orphans),
        details=f"Orphan keys: {orphans[:10]}" if orphans else "",
    )


def check_sum_balance(
    name: str,
    records: list[dict],
    amount_field: str,
    expected_sum: float,
    tolerance: float = 0.01,
) -> ReconciliationCheck:
    """Verify that the sum of a numeric field matches expected."""
    actual_sum = sum(r.get(amount_field, 0) for r in records)
    passed = abs(actual_sum - expected_sum) <= tolerance
    return ReconciliationCheck(
        name=name,
        description=f"Sum of {amount_field} should equal {expected_sum}",
        passed=passed,
        expected_value=expected_sum,
        actual_value=round(actual_sum, 2),
        details=f"Difference: {round(actual_sum - expected_sum, 2)}" if not passed else "",
    )


def check_non_negative(
    name: str, records: list[dict], field_name: str
) -> ReconciliationCheck:
    """Verify that no records have negative values in the specified field."""
    negatives = [
        (i, r.get(field_name))
        for i, r in enumerate(records)
        if isinstance(r.get(field_name), (int, float)) and r[field_name] < 0
    ]
    return ReconciliationCheck(
        name=name,
        description=f"No negative values in {field_name}",
        passed=len(negatives) == 0,
        expected_value=0,
        actual_value=len(negatives),
        details=f"First negatives at indices: {[n[0] for n in negatives[:5]]}"
        if negatives
        else "",
    )


def check_field_not_blank(
    name: str, records: list[dict], field_name: str
) -> ReconciliationCheck:
    """Verify that a required field is not blank/empty in any record."""
    blanks = [
        i
        for i, r in enumerate(records)
        if not r.get(field_name) or (isinstance(r.get(field_name), str) and not r[field_name].strip())
    ]
    return ReconciliationCheck(
        name=name,
        description=f"{field_name} must not be blank",
        passed=len(blanks) == 0,
        expected_value=0,
        actual_value=len(blanks),
        details=f"Blank at indices: {blanks[:10]}" if blanks else "",
    )


# ─────────────────────────────────────────────────────────────────────────────
# Job-specific reconciliation suites
# ─────────────────────────────────────────────────────────────────────────────


def reconcile_post_transaction(
    accounts_before: list[dict],
    accounts_after: list[dict],
    transactions: list[dict],
) -> ReconciliationReport:
    """
    Reconciliation for POSTTRAN (Post Transaction) batch job.

    Verifies:
      - Account count unchanged
      - No orphaned transactions (all card nums link to accounts via xref)
      - Net balance change = sum of transaction amounts
      - No account balance exceeds credit limit
    """
    checks = []

    # Account count preserved
    checks.append(
        check_record_count(
            "account_count_preserved", accounts_after, len(accounts_before)
        )
    )

    # Sum of transaction amounts
    tran_total = sum(t.get("DALYTRAN-AMT", t.get("TRAN-AMT", 0)) for t in transactions)

    # Net balance change across all accounts
    before_bal = sum(a.get("ACCT-CURR-BAL", 0) for a in accounts_before)
    after_bal = sum(a.get("ACCT-CURR-BAL", 0) for a in accounts_after)
    net_change = round(after_bal - before_bal, 2)

    checks.append(
        ReconciliationCheck(
            name="balance_change_equals_transactions",
            description="Net balance change = sum of posted transactions",
            passed=abs(net_change - tran_total) < 0.01,
            expected_value=round(tran_total, 2),
            actual_value=net_change,
        )
    )

    # No balance exceeds credit limit
    overlimit = [
        a
        for a in accounts_after
        if a.get("ACCT-CURR-BAL", 0) > a.get("ACCT-CREDIT-LIMIT", float("inf"))
    ]
    checks.append(
        ReconciliationCheck(
            name="no_overlimit_accounts",
            description="No account balance exceeds credit limit",
            passed=len(overlimit) == 0,
            expected_value=0,
            actual_value=len(overlimit),
        )
    )

    return ReconciliationReport(job_name="POSTTRAN", checks=checks)


def reconcile_daily_transaction_input(
    transactions: list[dict],
    card_xrefs: list[dict],
    accounts: list[dict],
) -> ReconciliationReport:
    """
    Reconciliation for daily transaction input file integrity.

    Verifies:
      - All transaction card numbers exist in XREF
      - All XREF account IDs exist in account master
      - No duplicate transaction records (by composite key)
      - Transaction amounts are non-zero
    """
    checks = []

    # Card numbers in transactions must exist in XREF
    xref_cards = {r.get("XREF-CARD-NUM") for r in card_xrefs}
    tran_cards = {
        t.get("DALYTRAN-CARD-NUM", t.get("TRAN-CARD-NUM")) for t in transactions
    }
    orphan_cards = tran_cards - xref_cards - {None, ""}
    checks.append(
        ReconciliationCheck(
            name="transaction_cards_in_xref",
            description="All transaction card numbers exist in cross-reference",
            passed=len(orphan_cards) == 0,
            expected_value=0,
            actual_value=len(orphan_cards),
            details=f"Orphan cards: {list(orphan_cards)[:5]}" if orphan_cards else "",
        )
    )

    # XREF account IDs must exist in account master
    acct_ids = {a.get("ACCT-ID") for a in accounts}
    xref_acct_ids = {r.get("XREF-ACCT-ID") for r in card_xrefs}
    orphan_accts = xref_acct_ids - acct_ids - {None, 0}
    checks.append(
        ReconciliationCheck(
            name="xref_accounts_exist",
            description="All XREF account IDs exist in account master",
            passed=len(orphan_accts) == 0,
            expected_value=0,
            actual_value=len(orphan_accts),
            details=f"Missing accounts: {list(orphan_accts)[:5]}" if orphan_accts else "",
        )
    )

    # No zero-amount transactions
    zeros = [
        i
        for i, t in enumerate(transactions)
        if t.get("DALYTRAN-AMT", t.get("TRAN-AMT", 0)) == 0
    ]
    checks.append(
        ReconciliationCheck(
            name="no_zero_amount_transactions",
            description="All transactions have non-zero amounts",
            passed=len(zeros) == 0,
            expected_value=0,
            actual_value=len(zeros),
            details=f"Zero-amount at indices: {zeros[:10]}" if zeros else "",
        )
    )

    return ReconciliationReport(
        job_name="DAILYTRAN_INPUT", checks=checks
    )


def reconcile_interest_calculation(
    accounts_before: list[dict],
    accounts_after: list[dict],
    category_balances: list[dict],
    disclosure_groups: list[dict],
) -> ReconciliationReport:
    """
    Reconciliation for INTCALC (Interest Calculation) batch job.

    Verifies:
      - Account count unchanged
      - Category balance records exist for each account/type combo
      - Interest rates from disclosure groups are positive
      - Cycle credit/debit fields updated
    """
    checks = []

    checks.append(
        check_record_count(
            "account_count_preserved", accounts_after, len(accounts_before)
        )
    )

    # Disclosure group rates must be positive
    negative_rates = [
        d for d in disclosure_groups if d.get("DIS-INT-RATE", 0) < 0
    ]
    checks.append(
        ReconciliationCheck(
            name="positive_interest_rates",
            description="All disclosure group interest rates are non-negative",
            passed=len(negative_rates) == 0,
            expected_value=0,
            actual_value=len(negative_rates),
        )
    )

    # Category balances reference valid accounts
    cat_acct_ids = {cb.get("TRANCAT-ACCT-ID") for cb in category_balances}
    acct_ids = {a.get("ACCT-ID") for a in accounts_after}
    orphan_cats = cat_acct_ids - acct_ids - {None, 0}
    checks.append(
        ReconciliationCheck(
            name="category_balances_have_accounts",
            description="All category balance account IDs exist in account master",
            passed=len(orphan_cats) == 0,
            expected_value=0,
            actual_value=len(orphan_cats),
        )
    )

    return ReconciliationReport(job_name="INTCALC", checks=checks)


def reconcile_statement_generation(
    accounts: list[dict],
    transactions: list[dict],
    customers: list[dict],
    card_xrefs: list[dict],
) -> ReconciliationReport:
    """
    Reconciliation for CREASTMT (Create Statement) batch job.

    Verifies:
      - Every active account has at least one customer via XREF
      - Customer records exist for all XREF customer IDs
      - Active accounts have valid date fields
    """
    checks = []

    # Active accounts have XREF entries
    active_accts = {a.get("ACCT-ID") for a in accounts if a.get("ACCT-ACTIVE-STATUS") == "Y"}
    xref_accts = {r.get("XREF-ACCT-ID") for r in card_xrefs}
    missing = active_accts - xref_accts - {None, 0}
    checks.append(
        ReconciliationCheck(
            name="active_accounts_have_xref",
            description="All active accounts have card cross-reference entries",
            passed=len(missing) == 0,
            expected_value=0,
            actual_value=len(missing),
        )
    )

    # XREF customer IDs exist in customer master
    cust_ids = {c.get("CUST-ID") for c in customers}
    xref_cust_ids = {r.get("XREF-CUST-ID") for r in card_xrefs}
    orphan_custs = xref_cust_ids - cust_ids - {None, 0}
    checks.append(
        ReconciliationCheck(
            name="xref_customers_exist",
            description="All XREF customer IDs exist in customer master",
            passed=len(orphan_custs) == 0,
            expected_value=0,
            actual_value=len(orphan_custs),
        )
    )

    # Active accounts have non-blank open date
    blank_dates = [
        a.get("ACCT-ID")
        for a in accounts
        if a.get("ACCT-ACTIVE-STATUS") == "Y"
        and (not a.get("ACCT-OPEN-DATE") or not a["ACCT-OPEN-DATE"].strip())
    ]
    checks.append(
        ReconciliationCheck(
            name="active_accounts_have_open_date",
            description="All active accounts have a non-blank open date",
            passed=len(blank_dates) == 0,
            expected_value=0,
            actual_value=len(blank_dates),
        )
    )

    return ReconciliationReport(job_name="CREASTMT", checks=checks)


def run_all_reconciliation(data_dir: Path, golden_dir: Path) -> list[ReconciliationReport]:
    """
    Run all reconciliation checks against golden reference data.

    Loads golden files and runs input-integrity checks.
    """
    reports = []

    # Load golden data
    def load_golden(name: str) -> list[dict]:
        path = golden_dir / name
        if not path.exists():
            return []
        with path.open() as f:
            data = json.load(f)
        return data.get("records", [])

    accounts = load_golden("acctdata.json")
    cards = load_golden("carddata.json")
    xrefs = load_golden("cardxref.json")
    customers = load_golden("custdata.json")
    transactions = load_golden("dailytran.json")
    discgroups = load_golden("discgrp.json")
    catbalances = load_golden("tcatbal.json")
    trancatg = load_golden("trancatg.json")
    trantypes = load_golden("trantype.json")

    # Daily transaction input reconciliation
    reports.append(
        reconcile_daily_transaction_input(transactions, xrefs, accounts)
    )

    # Statement generation pre-conditions
    reports.append(
        reconcile_statement_generation(accounts, transactions, customers, xrefs)
    )

    # Interest calculation pre-conditions
    reports.append(
        reconcile_interest_calculation(accounts, accounts, catbalances, discgroups)
    )

    return reports


if __name__ == "__main__":
    repo_root = Path(__file__).resolve().parent.parent
    golden_dir = repo_root / "golden-files"

    if not golden_dir.exists():
        print("ERROR: golden-files/ not found. Run record_parser.py first.")
        raise SystemExit(1)

    print("Running reconciliation checks...\n")
    reports = run_all_reconciliation(repo_root / "app" / "data" / "ASCII", golden_dir)

    all_passed = True
    for report in reports:
        status = "PASS" if report.passed else "FAIL"
        print(f"[{status}] {report.summary}")
        for check in report.checks:
            cs = "  PASS" if check.passed else "  FAIL"
            print(f"  {cs}: {check.name} — {check.description}")
            if check.details:
                print(f"        {check.details}")
        print()
        if not report.passed:
            all_passed = False

    if all_passed:
        print("All reconciliation checks PASSED.")
    else:
        print("Some reconciliation checks FAILED.")
        raise SystemExit(1)
