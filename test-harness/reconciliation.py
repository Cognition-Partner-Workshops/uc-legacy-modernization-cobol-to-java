"""Reconciliation checks for CardDemo migration validation.

Validates cross-file referential integrity, aggregate consistency,
and business rule preservation across the migrated data.
"""

import json
import sys
from dataclasses import dataclass, field
from decimal import Decimal
from pathlib import Path
from typing import Dict, List, Optional, Set

from .record_parser import parse_data_file


@dataclass
class CheckResult:
    """Result of a single reconciliation check."""
    check_id: str
    description: str
    status: str = "PENDING"  # 'PASS', 'FAIL', 'WARN', 'SKIP', 'PENDING'
    records_checked: int = 0
    violations: List[Dict] = field(default_factory=list)
    message: str = ""

    @property
    def passed(self) -> bool:
        return self.status == "PASS"


@dataclass
class ReconciliationReport:
    """Aggregated results of all reconciliation checks."""
    data_dir: str
    checks_run: int = 0
    checks_passed: int = 0
    checks_failed: int = 0
    checks_warned: int = 0
    checks_skipped: int = 0
    results: List[CheckResult] = field(default_factory=list)

    @property
    def passed(self) -> bool:
        return self.checks_failed == 0

    def summary(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        lines = [
            f"Reconciliation Report: {status}",
            f"  Data Directory: {self.data_dir}",
            f"  Checks Run:     {self.checks_run}",
            f"  Passed:         {self.checks_passed}",
            f"  Failed:         {self.checks_failed}",
            f"  Warnings:       {self.checks_warned}",
            f"  Skipped:        {self.checks_skipped}",
            "",
            "Details:",
        ]
        for r in self.results:
            icon = {"PASS": "[OK]", "FAIL": "[FAIL]", "WARN": "[WARN]", "SKIP": "[SKIP]"}
            lines.append(f"  {icon.get(r.status, '[??]')} {r.check_id}: {r.description}")
            if r.violations:
                for v in r.violations[:5]:
                    lines.append(f"       -> {v}")
                if len(r.violations) > 5:
                    lines.append(f"       ... and {len(r.violations) - 5} more")
        return "\n".join(lines)


def _load_data(data_dir: str, layout_name: str) -> Optional[List[Dict]]:
    """Load and parse a data file, returning None if not found."""
    filepath = Path(data_dir) / f"{layout_name}.txt"
    if not filepath.exists():
        return None
    return parse_data_file(str(filepath), layout_name)


def check_rc001_xref_accounts(data_dir: str) -> CheckResult:
    """RC-001: Every card in cardxref must reference a valid account in acctdata."""
    result = CheckResult(
        check_id="RC-001",
        description="Card XREF references valid accounts",
    )

    xref_records = _load_data(data_dir, "cardxref")
    acct_records = _load_data(data_dir, "acctdata")

    if xref_records is None or acct_records is None:
        result.status = "SKIP"
        result.message = "Required data files not found"
        return result

    valid_acct_ids = {r["ACCT-ID"] for r in acct_records}
    result.records_checked = len(xref_records)

    for rec in xref_records:
        acct_id = rec["XREF-ACCT-ID"]
        if acct_id not in valid_acct_ids:
            result.violations.append({
                "card": rec["XREF-CARD-NUM"],
                "invalid_acct_id": acct_id,
            })

    result.status = "PASS" if not result.violations else "FAIL"
    return result


def check_rc002_xref_customers(data_dir: str) -> CheckResult:
    """RC-002: Every card in cardxref must reference a valid customer in custdata."""
    result = CheckResult(
        check_id="RC-002",
        description="Card XREF references valid customers",
    )

    xref_records = _load_data(data_dir, "cardxref")
    cust_records = _load_data(data_dir, "custdata")

    if xref_records is None or cust_records is None:
        result.status = "SKIP"
        result.message = "Required data files not found"
        return result

    valid_cust_ids = {r["CUST-ID"] for r in cust_records}
    result.records_checked = len(xref_records)

    for rec in xref_records:
        cust_id = rec["XREF-CUST-ID"]
        if cust_id not in valid_cust_ids:
            result.violations.append({
                "card": rec["XREF-CARD-NUM"],
                "invalid_cust_id": cust_id,
            })

    result.status = "PASS" if not result.violations else "FAIL"
    return result


def check_rc003_category_balance_sum(data_dir: str) -> CheckResult:
    """RC-003: Sum of transaction category balances per account equals account balance."""
    result = CheckResult(
        check_id="RC-003",
        description="Category balance sums match account balances",
    )

    tcatbal_records = _load_data(data_dir, "tcatbal")
    acct_records = _load_data(data_dir, "acctdata")

    if tcatbal_records is None or acct_records is None:
        result.status = "SKIP"
        result.message = "Required data files not found"
        return result

    # Sum category balances per account
    cat_bal_sums: Dict[str, Decimal] = {}
    for rec in tcatbal_records:
        acct_id = rec["TRANCAT-ACCT-ID"]
        bal = Decimal(rec["TRAN-CAT-BAL"])
        cat_bal_sums[acct_id] = cat_bal_sums.get(acct_id, Decimal("0")) + bal

    # Compare with account balances
    result.records_checked = len(acct_records)
    for rec in acct_records:
        acct_id = rec["ACCT-ID"]
        acct_bal = Decimal(rec["ACCT-CURR-BAL"])
        cat_sum = cat_bal_sums.get(acct_id, Decimal("0"))

        if abs(acct_bal - cat_sum) > Decimal("0.01"):
            result.violations.append({
                "account": acct_id,
                "account_balance": str(acct_bal),
                "category_sum": str(cat_sum),
                "difference": str(acct_bal - cat_sum),
            })

    result.status = "PASS" if not result.violations else "WARN"
    result.message = (
        "Note: Initial data may not have matching sums until POSTTRAN runs"
        if result.violations else ""
    )
    return result


def check_rc004_dailytran_valid_cards(data_dir: str) -> CheckResult:
    """RC-004: Every daily transaction must reference a valid card in cardxref."""
    result = CheckResult(
        check_id="RC-004",
        description="Daily transactions reference valid cards",
    )

    tran_records = _load_data(data_dir, "dailytran")
    xref_records = _load_data(data_dir, "cardxref")

    if tran_records is None or xref_records is None:
        result.status = "SKIP"
        result.message = "Required data files not found"
        return result

    valid_cards = {r["XREF-CARD-NUM"] for r in xref_records}
    result.records_checked = len(tran_records)

    for rec in tran_records:
        card_num = rec["TRAN-CARD-NUM"]
        if card_num not in valid_cards:
            result.violations.append({
                "tran_id": rec["TRAN-ID"],
                "invalid_card": card_num,
            })

    result.status = "PASS" if not result.violations else "FAIL"
    return result


def check_rc005_valid_tran_types(data_dir: str) -> CheckResult:
    """RC-005: Transaction type codes in dailytran must exist in trantype."""
    result = CheckResult(
        check_id="RC-005",
        description="Transaction type codes are valid",
    )

    tran_records = _load_data(data_dir, "dailytran")
    type_records = _load_data(data_dir, "trantype")

    if tran_records is None or type_records is None:
        result.status = "SKIP"
        result.message = "Required data files not found"
        return result

    valid_types = {r["TRAN-TYPE"] for r in type_records}
    result.records_checked = len(tran_records)

    invalid_types: Set[str] = set()
    for rec in tran_records:
        type_cd = rec["TRAN-TYPE-CD"]
        if type_cd not in valid_types:
            invalid_types.add(type_cd)

    if invalid_types:
        for t in sorted(invalid_types):
            count = sum(1 for r in tran_records if r["TRAN-TYPE-CD"] == t)
            result.violations.append({
                "invalid_type_code": t,
                "occurrence_count": count,
            })

    result.status = "PASS" if not result.violations else "FAIL"
    return result


def check_rc006_valid_tran_categories(data_dir: str) -> CheckResult:
    """RC-006: Transaction category codes in dailytran must exist in trancatg."""
    result = CheckResult(
        check_id="RC-006",
        description="Transaction category codes are valid",
    )

    tran_records = _load_data(data_dir, "dailytran")
    cat_records = _load_data(data_dir, "trancatg")

    if tran_records is None or cat_records is None:
        result.status = "SKIP"
        result.message = "Required data files not found"
        return result

    valid_cats = {(r["TRAN-TYPE-CD"], r["TRAN-CAT-CD"]) for r in cat_records}
    result.records_checked = len(tran_records)

    invalid_combos: Set[tuple] = set()
    for rec in tran_records:
        combo = (rec["TRAN-TYPE-CD"], rec["TRAN-CAT-CD"])
        if combo not in valid_cats:
            invalid_combos.add(combo)

    if invalid_combos:
        for type_cd, cat_cd in sorted(invalid_combos):
            count = sum(
                1 for r in tran_records
                if r["TRAN-TYPE-CD"] == type_cd and r["TRAN-CAT-CD"] == cat_cd
            )
            result.violations.append({
                "type_code": type_cd,
                "category_code": cat_cd,
                "occurrence_count": count,
            })

    result.status = "PASS" if not result.violations else "FAIL"
    return result


def check_rc007_discgrp_valid_groups(data_dir: str) -> CheckResult:
    """RC-007: Disclosure group references must map to valid account groups."""
    result = CheckResult(
        check_id="RC-007",
        description="Disclosure groups reference valid account groups",
    )

    discgrp_records = _load_data(data_dir, "discgrp")
    acct_records = _load_data(data_dir, "acctdata")

    if discgrp_records is None or acct_records is None:
        result.status = "SKIP"
        result.message = "Required data files not found"
        return result

    valid_groups = {r["ACCT-GROUP-ID"] for r in acct_records}
    result.records_checked = len(discgrp_records)

    invalid_groups: Set[str] = set()
    for rec in discgrp_records:
        group_id = rec["DIS-ACCT-GROUP-ID"]
        if group_id not in valid_groups:
            invalid_groups.add(group_id)

    if invalid_groups:
        for g in sorted(invalid_groups):
            result.violations.append({"invalid_group_id": g})

    result.status = "PASS" if not result.violations else "WARN"
    result.message = (
        "Note: Disclosure groups may include system-defined groups "
        "(e.g., DEFAULT, ZEROAPR) not present in account data"
        if result.violations else ""
    )
    return result


def check_rc008_record_counts(data_dir: str) -> CheckResult:
    """RC-008: Verify record counts are consistent across files."""
    result = CheckResult(
        check_id="RC-008",
        description="Record counts are consistent",
    )

    counts = {}
    for layout in ["acctdata", "carddata", "custdata", "cardxref", "dailytran",
                   "tcatbal", "discgrp", "trantype", "trancatg"]:
        records = _load_data(data_dir, layout)
        if records is not None:
            counts[layout] = len(records)

    if not counts:
        result.status = "SKIP"
        result.message = "No data files found"
        return result

    result.records_checked = sum(counts.values())

    # Verify xref count matches card count
    if "cardxref" in counts and "carddata" in counts:
        if counts["cardxref"] != counts["carddata"]:
            result.violations.append({
                "check": "cardxref_count == carddata_count",
                "cardxref": counts["cardxref"],
                "carddata": counts["carddata"],
            })

    result.status = "PASS" if not result.violations else "WARN"
    result.message = f"File counts: {counts}"
    return result


def check_rc009_balance_integrity(data_dir: str) -> CheckResult:
    """RC-009: Account balance = prior balance + cycle debits - cycle credits.

    Note: This is a structural check on the balance fields.
    Full validation requires pre/post POSTTRAN snapshots.
    """
    result = CheckResult(
        check_id="RC-009",
        description="Account balance field structural integrity",
    )

    acct_records = _load_data(data_dir, "acctdata")
    if acct_records is None:
        result.status = "SKIP"
        result.message = "Account data file not found"
        return result

    result.records_checked = len(acct_records)

    for rec in acct_records:
        bal = Decimal(rec["ACCT-CURR-BAL"])
        credit_limit = Decimal(rec["ACCT-CREDIT-LIMIT"])

        # Balance should not exceed credit limit
        if bal > credit_limit:
            result.violations.append({
                "account": rec["ACCT-ID"],
                "balance": str(bal),
                "credit_limit": str(credit_limit),
                "over_limit_by": str(bal - credit_limit),
            })

    result.status = "PASS" if not result.violations else "WARN"
    return result


def check_rc010_no_orphan_cards(data_dir: str) -> CheckResult:
    """RC-010: No orphan cards (cards without a cross-reference entry)."""
    result = CheckResult(
        check_id="RC-010",
        description="No orphan cards without XREF entries",
    )

    card_records = _load_data(data_dir, "carddata")
    xref_records = _load_data(data_dir, "cardxref")

    if card_records is None or xref_records is None:
        result.status = "SKIP"
        result.message = "Required data files not found"
        return result

    xref_cards = {r["XREF-CARD-NUM"] for r in xref_records}
    result.records_checked = len(card_records)

    for rec in card_records:
        card_num = rec["CARD-NUM"]
        if card_num not in xref_cards:
            result.violations.append({
                "orphan_card": card_num,
                "account": rec["CARD-ACCT-ID"],
            })

    result.status = "PASS" if not result.violations else "FAIL"
    return result


# Registry of all reconciliation checks
ALL_CHECKS = [
    check_rc001_xref_accounts,
    check_rc002_xref_customers,
    check_rc003_category_balance_sum,
    check_rc004_dailytran_valid_cards,
    check_rc005_valid_tran_types,
    check_rc006_valid_tran_categories,
    check_rc007_discgrp_valid_groups,
    check_rc008_record_counts,
    check_rc009_balance_integrity,
    check_rc010_no_orphan_cards,
]


def run_reconciliation(
    data_dir: str,
    checks: Optional[List[str]] = None,
) -> ReconciliationReport:
    """Run reconciliation checks against the data directory.

    Args:
        data_dir: Path to the data directory containing .txt files.
        checks: Optional list of check IDs to run (e.g., ['RC-001', 'RC-002']).
                If None or 'all', runs all checks.

    Returns:
        ReconciliationReport with all results.
    """
    report = ReconciliationReport(data_dir=data_dir)

    for check_fn in ALL_CHECKS:
        # Extract check ID from docstring
        doc = check_fn.__doc__ or ""
        check_id = doc.split(":")[0].strip() if ":" in doc else ""

        if checks and checks != ["all"] and check_id not in checks:
            continue

        result = check_fn(data_dir)
        report.results.append(result)
        report.checks_run += 1

        if result.status == "PASS":
            report.checks_passed += 1
        elif result.status == "FAIL":
            report.checks_failed += 1
        elif result.status == "WARN":
            report.checks_warned += 1
        else:
            report.checks_skipped += 1

    return report


def report_to_json(report: ReconciliationReport) -> dict:
    """Convert report to JSON-serializable format."""
    return {
        "status": "PASS" if report.passed else "FAIL",
        "data_dir": report.data_dir,
        "checks_run": report.checks_run,
        "checks_passed": report.checks_passed,
        "checks_failed": report.checks_failed,
        "checks_warned": report.checks_warned,
        "checks_skipped": report.checks_skipped,
        "results": [
            {
                "check_id": r.check_id,
                "description": r.description,
                "status": r.status,
                "records_checked": r.records_checked,
                "violation_count": len(r.violations),
                "violations": r.violations[:10],
                "message": r.message,
            }
            for r in report.results
        ],
    }


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(
        description="Run reconciliation checks on CardDemo data files"
    )
    parser.add_argument(
        "--data-dir",
        default="app/data/ASCII",
        help="Path to data directory",
    )
    parser.add_argument(
        "--checks",
        nargs="*",
        default=["all"],
        help="Specific check IDs to run (default: all)",
    )
    parser.add_argument(
        "--output",
        help="Write JSON report to file",
    )

    args = parser.parse_args()

    report = run_reconciliation(args.data_dir, args.checks)

    print(report.summary())

    if args.output:
        json_report = report_to_json(report)
        Path(args.output).write_text(json.dumps(json_report, indent=2))
        print(f"\nJSON report written to: {args.output}")

    sys.exit(0 if report.passed else 1)
