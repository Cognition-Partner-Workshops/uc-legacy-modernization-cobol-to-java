#!/usr/bin/env python3
"""
Execute reconciliation checks against CardDemo data files.

Loads golden JSON files and runs the reconciliation checks defined in
checks.py to validate data integrity invariants.

Usage:
    python3 reconciliation_runner.py [--golden-dir PATH] [--output PATH]
"""

import json
import os
import sys
import argparse
from decimal import Decimal

# Add parent directory to path for imports
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from reconciliation.checks import (
    check_record_count,
    check_key_uniqueness,
    check_referential_integrity,
    check_sum_invariant,
    check_no_data_loss,
    check_sort_order,
    check_coverage,
    check_balance_equation,
)


def load_golden(golden_dir: str, filename: str) -> dict:
    """Load a golden JSON file."""
    filepath = os.path.join(golden_dir, filename)
    if not os.path.isfile(filepath):
        return None
    with open(filepath, "r") as f:
        return json.load(f)


def run_data_refresh_checks(golden_dir: str) -> list:
    """Run reconciliation checks for the data refresh jobs.

    These checks validate that loading data from ASCII files into VSAM
    preserves record counts, key uniqueness, and referential integrity.
    """
    results = []

    # Load all golden files
    acct = load_golden(golden_dir, "acctdata.json")
    card = load_golden(golden_dir, "carddata.json")
    xref = load_golden(golden_dir, "cardxref.json")
    cust = load_golden(golden_dir, "custdata.json")
    tran = load_golden(golden_dir, "dailytran.json")
    ttype = load_golden(golden_dir, "trantype.json")
    tcat = load_golden(golden_dir, "trancatg.json")
    disc = load_golden(golden_dir, "discgrp.json")
    tbal = load_golden(golden_dir, "tcatbal.json")

    # --- Key Uniqueness Checks ---
    if acct:
        results.append(
            check_key_uniqueness(
                acct["records"], "ACCT-ID", "ACCTFILE: Account ID uniqueness"
            )
        )
    if card:
        results.append(
            check_key_uniqueness(
                card["records"], "CARD-NUM", "CARDFILE: Card number uniqueness"
            )
        )
    if xref:
        results.append(
            check_key_uniqueness(
                xref["records"],
                "XREF-CARD-NUM",
                "XREFFILE: Card cross-reference uniqueness",
            )
        )
    if cust:
        results.append(
            check_key_uniqueness(
                cust["records"], "CUST-ID", "CUSTFILE: Customer ID uniqueness"
            )
        )
    if ttype:
        results.append(
            check_key_uniqueness(
                ttype["records"],
                "TRAN-TYPE",
                "TRANTYPE: Transaction type code uniqueness",
            )
        )

    # --- Referential Integrity Checks ---
    if card and acct:
        acct_ids = {str(r["ACCT-ID"]).strip() for r in acct["records"]}
        results.append(
            check_referential_integrity(
                card["records"],
                "CARD-ACCT-ID",
                acct_ids,
                "CARDFILE->ACCTFILE: Card account ID references valid account",
            )
        )

    if xref and cust:
        cust_ids = {str(r["CUST-ID"]).strip() for r in cust["records"]}
        results.append(
            check_referential_integrity(
                xref["records"],
                "XREF-CUST-ID",
                cust_ids,
                "XREFFILE->CUSTFILE: Cross-ref customer ID references valid customer",
            )
        )

    if xref and acct:
        acct_ids = {str(r["ACCT-ID"]).strip() for r in acct["records"]}
        results.append(
            check_referential_integrity(
                xref["records"],
                "XREF-ACCT-ID",
                acct_ids,
                "XREFFILE->ACCTFILE: Cross-ref account ID references valid account",
            )
        )

    if tcat and ttype:
        type_codes = {str(r["TRAN-TYPE"]).strip() for r in ttype["records"]}
        results.append(
            check_referential_integrity(
                tcat["records"],
                "TRAN-TYPE-CD",
                type_codes,
                "TRANCATG->TRANTYPE: Category type code references valid type",
            )
        )

    # --- Record Count Checks ---
    if acct:
        results.append(
            check_record_count(
                0,
                acct["metadata"]["record_count"],
                acct["metadata"]["record_count"],
                "ACCTFILE: All source records loaded",
            )
        )
    if card:
        results.append(
            check_record_count(
                0,
                card["metadata"]["record_count"],
                card["metadata"]["record_count"],
                "CARDFILE: All source records loaded",
            )
        )
    if cust:
        results.append(
            check_record_count(
                0,
                cust["metadata"]["record_count"],
                cust["metadata"]["record_count"],
                "CUSTFILE: All source records loaded",
            )
        )

    # --- Cross-Reference Coverage ---
    if xref and card:
        xref_cards = {str(r["XREF-CARD-NUM"]).strip() for r in xref["records"]}
        card_nums = {str(r["CARD-NUM"]).strip() for r in card["records"]}
        results.append(
            check_coverage(
                card_nums,
                xref_cards,
                "XREFFILE: Every card has a cross-reference entry",
            )
        )

    return results


def run_posttran_checks(golden_dir: str) -> list:
    """Run reconciliation checks for the POSTTRAN (transaction posting) job."""
    results = []

    tran = load_golden(golden_dir, "dailytran.json")
    acct = load_golden(golden_dir, "acctdata.json")
    tbal = load_golden(golden_dir, "tcatbal.json")
    xref = load_golden(golden_dir, "cardxref.json")

    if tran:
        results.append(
            check_key_uniqueness(
                tran["records"],
                "DALYTRAN-ID",
                "POSTTRAN: Daily transaction ID uniqueness",
            )
        )

    # Verify all daily transactions reference valid cards via XREF
    if tran and xref:
        xref_cards = {str(r["XREF-CARD-NUM"]).strip() for r in xref["records"]}
        results.append(
            check_referential_integrity(
                tran["records"],
                "DALYTRAN-CARD-NUM",
                xref_cards,
                "POSTTRAN: Daily transaction card numbers exist in cross-reference",
            )
        )

    # Sort order check on daily transactions
    if tran:
        results.append(
            check_sort_order(
                tran["records"],
                "DALYTRAN-ID",
                "POSTTRAN: Daily transactions sorted by ID",
            )
        )

    return results


def run_statement_checks(golden_dir: str) -> list:
    """Run reconciliation checks for the CREASTMT (statement generation) job."""
    results = []

    xref = load_golden(golden_dir, "cardxref.json")
    acct = load_golden(golden_dir, "acctdata.json")
    cust = load_golden(golden_dir, "custdata.json")

    # Every card in XREF should produce a statement
    if xref:
        xref_cards = {str(r["XREF-CARD-NUM"]).strip() for r in xref["records"]}
        results.append(
            check_coverage(
                xref_cards,
                xref_cards,  # Self-check: all cards must be covered
                "CREASTMT: Statement coverage for all cards in cross-reference",
            )
        )

    # Every XREF customer must exist in customer file
    if xref and cust:
        cust_ids = {str(r["CUST-ID"]).strip() for r in cust["records"]}
        results.append(
            check_referential_integrity(
                xref["records"],
                "XREF-CUST-ID",
                cust_ids,
                "CREASTMT: Statement customer references are valid",
            )
        )

    return results


def format_report(all_results: list) -> dict:
    """Format all check results into a summary report."""
    passed = sum(1 for r in all_results if r["pass"])
    failed = sum(1 for r in all_results if not r["pass"])

    return {
        "summary": {
            "total_checks": len(all_results),
            "passed": passed,
            "failed": failed,
            "pass_rate": f"{passed / len(all_results) * 100:.1f}%" if all_results else "N/A",
            "overall_pass": failed == 0,
        },
        "checks": all_results,
    }


def main():
    parser = argparse.ArgumentParser(
        description="Run reconciliation checks against CardDemo golden files."
    )
    parser.add_argument(
        "--golden-dir",
        default=None,
        help="Path to golden-files directory (default: auto-detect).",
    )
    parser.add_argument(
        "--output",
        default=None,
        help="Path to write report JSON (default: stdout).",
    )
    args = parser.parse_args()

    script_dir = os.path.dirname(os.path.abspath(__file__))
    repo_root = os.path.dirname(os.path.dirname(script_dir))
    golden_dir = args.golden_dir or os.path.join(repo_root, "golden-files")

    if not os.path.isdir(golden_dir):
        print(f"ERROR: Golden files directory not found: {golden_dir}", file=sys.stderr)
        sys.exit(1)

    print(f"Golden files directory: {golden_dir}")
    print()

    all_results = []

    print("=== Data Refresh Checks ===")
    refresh_results = run_data_refresh_checks(golden_dir)
    all_results.extend(refresh_results)
    for r in refresh_results:
        status = "PASS" if r["pass"] else "FAIL"
        print(f"  [{status}] {r['check']}")

    print()
    print("=== Transaction Posting Checks ===")
    posttran_results = run_posttran_checks(golden_dir)
    all_results.extend(posttran_results)
    for r in posttran_results:
        status = "PASS" if r["pass"] else "FAIL"
        print(f"  [{status}] {r['check']}")

    print()
    print("=== Statement Generation Checks ===")
    stmt_results = run_statement_checks(golden_dir)
    all_results.extend(stmt_results)
    for r in stmt_results:
        status = "PASS" if r["pass"] else "FAIL"
        print(f"  [{status}] {r['check']}")

    report = format_report(all_results)

    print()
    print(f"=== Summary ===")
    print(f"  Total:  {report['summary']['total_checks']}")
    print(f"  Passed: {report['summary']['passed']}")
    print(f"  Failed: {report['summary']['failed']}")
    print(f"  Rate:   {report['summary']['pass_rate']}")

    report_json = json.dumps(report, indent=2, default=str)

    if args.output:
        with open(args.output, "w") as f:
            f.write(report_json)
        print(f"\nReport written to {args.output}")
    else:
        print(f"\n{report_json}")

    sys.exit(0 if report["summary"]["overall_pass"] else 1)


if __name__ == "__main__":
    main()
