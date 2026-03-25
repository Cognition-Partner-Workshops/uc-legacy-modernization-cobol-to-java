"""
Reconciliation check engine for CardDemo migration testing.

Performs aggregate data integrity checks across golden files and modern
system output — record counts, balance totals, referential integrity.

Usage:
    from reconciliation import run_all_checks, run_check

    results = run_all_checks(golden_dir="golden-files/")
    result = run_check("RC-001", golden_dir="golden-files/", modern_counts={...})
"""

import json
import sys
from decimal import Decimal
from pathlib import Path
from typing import Optional


def load_golden(golden_dir: str, file_key: str) -> dict:
    """Load a golden JSON file."""
    path = Path(golden_dir) / f"{file_key}.json"
    with open(path) as f:
        return json.load(f)


def rc001_account_record_count(golden_dir: str,
                                modern_count: Optional[int] = None) -> dict:
    """RC-001: Account Record Count — DB COUNT(*) = golden record count."""
    golden = load_golden(golden_dir, "acctdata")
    golden_count = golden["metadata"]["record_count"]

    result = {
        "check_id": "RC-001",
        "check_name": "Account Record Count",
        "golden_count": golden_count,
        "modern_count": modern_count,
    }

    if modern_count is not None:
        result["pass"] = golden_count == modern_count
        result["difference"] = modern_count - golden_count
    else:
        result["pass"] = None
        result["note"] = "Modern count not provided — golden baseline only"

    return result


def rc002_account_balance_sum(golden_dir: str,
                               modern_sum: Optional[float] = None) -> dict:
    """RC-002: Account Balance Sum — SUM(current_balance) matches."""
    golden = load_golden(golden_dir, "acctdata")
    golden_sum = sum(
        Decimal(str(r["ACCT_CURR_BAL"]))
        for r in golden["records"]
    )

    result = {
        "check_id": "RC-002",
        "check_name": "Account Balance Sum",
        "golden_sum": float(golden_sum),
        "modern_sum": modern_sum,
    }

    if modern_sum is not None:
        diff = abs(golden_sum - Decimal(str(modern_sum)))
        result["pass"] = diff == 0
        result["difference"] = float(diff)
    else:
        result["pass"] = None
        result["note"] = "Modern sum not provided — golden baseline only"

    return result


def rc003_card_record_count(golden_dir: str,
                             modern_count: Optional[int] = None) -> dict:
    """RC-003: Card Record Count."""
    golden = load_golden(golden_dir, "carddata")
    golden_count = golden["metadata"]["record_count"]

    result = {
        "check_id": "RC-003",
        "check_name": "Card Record Count",
        "golden_count": golden_count,
        "modern_count": modern_count,
    }

    if modern_count is not None:
        result["pass"] = golden_count == modern_count
        result["difference"] = modern_count - golden_count
    else:
        result["pass"] = None
        result["note"] = "Modern count not provided — golden baseline only"

    return result


def rc004_card_account_referential_integrity(golden_dir: str) -> dict:
    """RC-004: Card-Account Referential Integrity — every card→valid account."""
    cards = load_golden(golden_dir, "carddata")
    accounts = load_golden(golden_dir, "acctdata")

    account_ids = {r["ACCT_ID"] for r in accounts["records"]}
    orphans = []

    for card in cards["records"]:
        if card["CARD_ACCT_ID"] not in account_ids:
            orphans.append({
                "card_num": card["CARD_NUM"],
                "acct_id": card["CARD_ACCT_ID"],
            })

    return {
        "check_id": "RC-004",
        "check_name": "Card-Account Referential Integrity",
        "total_cards": cards["metadata"]["record_count"],
        "orphan_count": len(orphans),
        "orphans": orphans[:10],  # Show first 10
        "pass": len(orphans) == 0,
    }


def rc005_customer_record_count(golden_dir: str,
                                 modern_count: Optional[int] = None) -> dict:
    """RC-005: Customer Record Count."""
    golden = load_golden(golden_dir, "custdata")
    golden_count = golden["metadata"]["record_count"]

    result = {
        "check_id": "RC-005",
        "check_name": "Customer Record Count",
        "golden_count": golden_count,
        "modern_count": modern_count,
    }

    if modern_count is not None:
        result["pass"] = golden_count == modern_count
        result["difference"] = modern_count - golden_count
    else:
        result["pass"] = None
        result["note"] = "Modern count not provided — golden baseline only"

    return result


def rc006_transaction_record_count(golden_dir: str,
                                    modern_count: Optional[int] = None) -> dict:
    """RC-006: Daily Transaction Record Count."""
    golden = load_golden(golden_dir, "dailytran")
    golden_count = golden["metadata"]["record_count"]

    result = {
        "check_id": "RC-006",
        "check_name": "Daily Transaction Record Count",
        "golden_count": golden_count,
        "modern_count": modern_count,
    }

    if modern_count is not None:
        result["pass"] = golden_count == modern_count
        result["difference"] = modern_count - golden_count
    else:
        result["pass"] = None
        result["note"] = "Modern count not provided — golden baseline only"

    return result


def rc007_transaction_amount_sum(golden_dir: str,
                                  modern_sum: Optional[float] = None) -> dict:
    """RC-007: Transaction Amount Sum."""
    golden = load_golden(golden_dir, "dailytran")
    golden_sum = sum(
        Decimal(str(r["DALYTRAN_AMT"]))
        for r in golden["records"]
    )

    result = {
        "check_id": "RC-007",
        "check_name": "Transaction Amount Sum",
        "golden_sum": float(golden_sum),
        "modern_sum": modern_sum,
    }

    if modern_sum is not None:
        diff = abs(golden_sum - Decimal(str(modern_sum)))
        result["pass"] = diff == 0
        result["difference"] = float(diff)
    else:
        result["pass"] = None
        result["note"] = "Modern sum not provided — golden baseline only"

    return result


def rc008_category_balance_consistency(golden_dir: str) -> dict:
    """RC-008: Category Balance Consistency — check category balances exist for accounts."""
    tcatbal = load_golden(golden_dir, "tcatbal")
    accounts = load_golden(golden_dir, "acctdata")

    account_ids = {r["ACCT_ID"] for r in accounts["records"]}
    catbal_account_ids = {r["TRANCAT_ACCT_ID"] for r in tcatbal["records"]}

    # Check that category balance accounts are valid
    orphan_catbal = catbal_account_ids - account_ids

    return {
        "check_id": "RC-008",
        "check_name": "Category Balance Consistency",
        "total_catbal_records": tcatbal["metadata"]["record_count"],
        "unique_accounts_in_catbal": len(catbal_account_ids),
        "orphan_account_ids": sorted(orphan_catbal),
        "pass": len(orphan_catbal) == 0,
    }


def rc009_xref_completeness(golden_dir: str) -> dict:
    """RC-009: Cross-Reference Completeness — every XREF→valid card AND account."""
    xref = load_golden(golden_dir, "cardxref")
    cards = load_golden(golden_dir, "carddata")
    accounts = load_golden(golden_dir, "acctdata")

    card_nums = {r["CARD_NUM"] for r in cards["records"]}
    account_ids = {r["ACCT_ID"] for r in accounts["records"]}

    missing_cards = []
    missing_accounts = []

    for rec in xref["records"]:
        if rec["XREF_CARD_NUM"] not in card_nums:
            missing_cards.append(rec["XREF_CARD_NUM"])
        if rec["XREF_ACCT_ID"] not in account_ids:
            missing_accounts.append(rec["XREF_ACCT_ID"])

    return {
        "check_id": "RC-009",
        "check_name": "Cross-Reference Completeness",
        "total_xref_records": xref["metadata"]["record_count"],
        "missing_card_refs": len(missing_cards),
        "missing_account_refs": len(missing_accounts),
        "pass": len(missing_cards) == 0 and len(missing_accounts) == 0,
    }


# Registry of all checks
ALL_CHECKS = {
    "RC-001": rc001_account_record_count,
    "RC-002": rc002_account_balance_sum,
    "RC-003": rc003_card_record_count,
    "RC-004": rc004_card_account_referential_integrity,
    "RC-005": rc005_customer_record_count,
    "RC-006": rc006_transaction_record_count,
    "RC-007": rc007_transaction_amount_sum,
    "RC-008": rc008_category_balance_consistency,
    "RC-009": rc009_xref_completeness,
}

# Checks that only need golden_dir (no modern system input)
GOLDEN_ONLY_CHECKS = ["RC-004", "RC-008", "RC-009"]


def run_all_checks(golden_dir: str, modern_data: Optional[dict] = None) -> dict:
    """Run all reconciliation checks against golden files.

    Args:
        golden_dir: Path to golden-files/ directory
        modern_data: Optional dict with modern system counts/sums for comparison

    Returns:
        Full reconciliation report
    """
    results = []
    pass_count = 0
    fail_count = 0
    baseline_count = 0

    for check_id in sorted(ALL_CHECKS.keys()):
        check_fn = ALL_CHECKS[check_id]

        if check_id in GOLDEN_ONLY_CHECKS:
            result = check_fn(golden_dir)
        else:
            # Pass modern data if available
            modern_value = None
            if modern_data:
                modern_value = modern_data.get(check_id)
            result = check_fn(golden_dir, modern_value)

        results.append(result)

        if result["pass"] is True:
            pass_count += 1
        elif result["pass"] is False:
            fail_count += 1
        else:
            baseline_count += 1

    return {
        "summary": {
            "total_checks": len(results),
            "passed": pass_count,
            "failed": fail_count,
            "baseline_only": baseline_count,
            "overall_pass": fail_count == 0,
        },
        "checks": results,
    }


def main():
    """CLI: python reconciliation.py <golden_dir>"""
    if len(sys.argv) < 2:
        print("Usage: python reconciliation.py <golden_dir>")
        sys.exit(1)

    golden_dir = sys.argv[1]
    report = run_all_checks(golden_dir)

    print(json.dumps(report, indent=2, default=str))

    summary = report["summary"]
    print(f"\n{'='*50}")
    print(f"Reconciliation Report")
    print(f"{'='*50}")
    print(f"Total checks:   {summary['total_checks']}")
    print(f"Passed:         {summary['passed']}")
    print(f"Failed:         {summary['failed']}")
    print(f"Baseline only:  {summary['baseline_only']}")
    print(f"Overall:        {'PASS' if summary['overall_pass'] else 'FAIL'}")

    if not summary["overall_pass"]:
        sys.exit(1)


if __name__ == "__main__":
    main()
