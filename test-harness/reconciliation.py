#!/usr/bin/env python3
"""
reconciliation.py -- Aggregate data integrity checks for CardDemo migration.

Performs three levels of reconciliation:
  Level 1: Record counts (source vs migrated)
  Level 2: Control totals (sum of key numeric columns)
  Level 3: Referential integrity (cross-file foreign key checks)

Usage:
    python reconciliation.py [--golden-dir PATH] [--java-dir PATH] [--output PATH]

If --java-dir is omitted, runs checks against golden files only (self-validation).
"""

import argparse
import json
import os
import sys
from decimal import Decimal


def load_golden(golden_dir: str, file_key: str) -> dict:
    """Load a golden-reference JSON file."""
    path = os.path.join(golden_dir, f"{file_key}.golden.json")
    if not os.path.exists(path):
        return {"metadata": {}, "records": []}
    with open(path) as f:
        return json.load(f)


def check_record_counts(golden_dir: str, java_dir: str | None) -> list[dict]:
    """Level 1: Verify record counts match between source and migrated data."""
    checks = []
    file_keys = [
        "acctdata", "carddata", "cardxref", "custdata", "dailytran",
        "discgrp", "tcatbal", "trancatg", "trantype",
    ]

    for key in file_keys:
        golden = load_golden(golden_dir, key)
        golden_count = golden["metadata"].get("total_records", 0)

        check = {
            "check": f"record_count:{key}",
            "level": 1,
            "source_count": golden_count,
        }

        if java_dir:
            java_path = os.path.join(java_dir, f"{key}.java-output.json")
            if os.path.exists(java_path):
                with open(java_path) as f:
                    java_data = json.load(f)
                java_count = len(java_data.get("records", []))
                check["migrated_count"] = java_count
                check["status"] = "PASS" if golden_count == java_count else "FAIL"
                if check["status"] == "FAIL":
                    check["delta"] = java_count - golden_count
            else:
                check["status"] = "SKIP"
                check["reason"] = "Java output not found"
        else:
            check["status"] = "BASELINE"
            check["migrated_count"] = None

        checks.append(check)

    return checks


def check_control_totals(golden_dir: str, java_dir: str | None) -> list[dict]:
    """Level 2: Verify control totals for key numeric columns."""
    checks = []

    # Define which files/fields to sum
    total_specs = [
        {
            "file": "acctdata",
            "field": "ACCT-CURR-BAL",
            "description": "Sum of all account current balances",
        },
        {
            "file": "acctdata",
            "field": "ACCT-CREDIT-LIMIT",
            "description": "Sum of all account credit limits",
        },
        {
            "file": "acctdata",
            "field": "ACCT-CASH-CREDIT-LIMIT",
            "description": "Sum of all account cash credit limits",
        },
        {
            "file": "acctdata",
            "field": "ACCT-CURR-CYC-CREDIT",
            "description": "Sum of all current cycle credits",
        },
        {
            "file": "acctdata",
            "field": "ACCT-CURR-CYC-DEBIT",
            "description": "Sum of all current cycle debits",
        },
        {
            "file": "dailytran",
            "field": "TRAN-AMT",
            "description": "Sum of all daily transaction amounts",
        },
        {
            "file": "tcatbal",
            "field": "TRAN-CAT-BAL",
            "description": "Sum of all transaction category balances",
        },
        {
            "file": "discgrp",
            "field": "DIS-INT-RATE",
            "description": "Sum of all disclosure interest rates",
        },
    ]

    for spec in total_specs:
        golden = load_golden(golden_dir, spec["file"])
        golden_total = Decimal("0")
        for rec in golden.get("records", []):
            val = rec.get(spec["field"], "0")
            try:
                golden_total += Decimal(str(val))
            except Exception:
                pass

        check = {
            "check": f"control_total:{spec['file']}.{spec['field']}",
            "level": 2,
            "description": spec["description"],
            "source_total": str(golden_total),
        }

        if java_dir:
            java_path = os.path.join(java_dir, f"{spec['file']}.java-output.json")
            if os.path.exists(java_path):
                with open(java_path) as f:
                    java_data = json.load(f)
                java_total = Decimal("0")
                for rec in java_data.get("records", []):
                    val = rec.get(spec["field"], "0")
                    try:
                        java_total += Decimal(str(val))
                    except Exception:
                        pass
                check["migrated_total"] = str(java_total)
                delta = abs(golden_total - java_total)
                check["delta"] = str(delta)
                check["status"] = "PASS" if delta < Decimal("0.01") else "FAIL"
            else:
                check["status"] = "SKIP"
                check["reason"] = "Java output not found"
        else:
            check["status"] = "BASELINE"
            check["migrated_total"] = None

        checks.append(check)

    return checks


def check_referential_integrity(golden_dir: str) -> list[dict]:
    """Level 3: Validate cross-file referential integrity in golden data.

    This validates the source data relationships. The same checks should be
    run against migrated data to ensure relationships are preserved.
    """
    checks = []

    # Load all golden datasets
    acctdata = load_golden(golden_dir, "acctdata")
    carddata = load_golden(golden_dir, "carddata")
    cardxref = load_golden(golden_dir, "cardxref")
    custdata = load_golden(golden_dir, "custdata")
    dailytran = load_golden(golden_dir, "dailytran")
    tcatbal = load_golden(golden_dir, "tcatbal")
    trancatg = load_golden(golden_dir, "trancatg")
    trantype = load_golden(golden_dir, "trantype")
    discgrp = load_golden(golden_dir, "discgrp")

    # Build lookup sets
    acct_ids = {rec["ACCT-ID"] for rec in acctdata.get("records", [])}
    cust_ids = {rec["CUST-ID"] for rec in custdata.get("records", [])}
    card_nums = {rec["CARD-NUM"] for rec in carddata.get("records", [])}
    tran_types = {rec["TRAN-TYPE"] for rec in trantype.get("records", [])}
    acct_group_ids = {rec["ACCT-GROUP-ID"] for rec in acctdata.get("records", [])}

    # Check 1: Every card references a valid account
    orphan_cards = []
    for rec in carddata.get("records", []):
        if rec["CARD-ACCT-ID"] not in acct_ids:
            orphan_cards.append({"card": rec["CARD-NUM"], "acct_id": rec["CARD-ACCT-ID"]})
    checks.append({
        "check": "ref_integrity:card->account",
        "level": 3,
        "parent": "acctdata.ACCT-ID",
        "child": "carddata.CARD-ACCT-ID",
        "total_children": len(carddata.get("records", [])),
        "orphans": len(orphan_cards),
        "status": "PASS" if len(orphan_cards) == 0 else "FAIL",
        "orphan_details": orphan_cards[:10],  # First 10 for brevity
    })

    # Check 2: Every xref references a valid customer
    orphan_xref_cust = []
    for rec in cardxref.get("records", []):
        if rec["XREF-CUST-ID"] not in cust_ids:
            orphan_xref_cust.append({"card": rec["XREF-CARD-NUM"], "cust_id": rec["XREF-CUST-ID"]})
    checks.append({
        "check": "ref_integrity:xref->customer",
        "level": 3,
        "parent": "custdata.CUST-ID",
        "child": "cardxref.XREF-CUST-ID",
        "total_children": len(cardxref.get("records", [])),
        "orphans": len(orphan_xref_cust),
        "status": "PASS" if len(orphan_xref_cust) == 0 else "FAIL",
        "orphan_details": orphan_xref_cust[:10],
    })

    # Check 3: Every xref references a valid account
    orphan_xref_acct = []
    for rec in cardxref.get("records", []):
        if rec["XREF-ACCT-ID"] not in acct_ids:
            orphan_xref_acct.append({"card": rec["XREF-CARD-NUM"], "acct_id": rec["XREF-ACCT-ID"]})
    checks.append({
        "check": "ref_integrity:xref->account",
        "level": 3,
        "parent": "acctdata.ACCT-ID",
        "child": "cardxref.XREF-ACCT-ID",
        "total_children": len(cardxref.get("records", [])),
        "orphans": len(orphan_xref_acct),
        "status": "PASS" if len(orphan_xref_acct) == 0 else "FAIL",
        "orphan_details": orphan_xref_acct[:10],
    })

    # Check 4: Every xref references a valid card
    orphan_xref_card = []
    for rec in cardxref.get("records", []):
        if rec["XREF-CARD-NUM"] not in card_nums:
            orphan_xref_card.append({"card": rec["XREF-CARD-NUM"]})
    checks.append({
        "check": "ref_integrity:xref->card",
        "level": 3,
        "parent": "carddata.CARD-NUM",
        "child": "cardxref.XREF-CARD-NUM",
        "total_children": len(cardxref.get("records", [])),
        "orphans": len(orphan_xref_card),
        "status": "PASS" if len(orphan_xref_card) == 0 else "FAIL",
        "orphan_details": orphan_xref_card[:10],
    })

    # Check 5: Every daily transaction references a valid card
    orphan_tran_card = []
    for rec in dailytran.get("records", []):
        if rec["TRAN-CARD-NUM"] not in card_nums:
            orphan_tran_card.append({"tran_id": rec["TRAN-ID"], "card": rec["TRAN-CARD-NUM"]})
    checks.append({
        "check": "ref_integrity:dailytran->card",
        "level": 3,
        "parent": "carddata.CARD-NUM",
        "child": "dailytran.TRAN-CARD-NUM",
        "total_children": len(dailytran.get("records", [])),
        "orphans": len(orphan_tran_card),
        "status": "PASS" if len(orphan_tran_card) == 0 else "FAIL",
        "orphan_details": orphan_tran_card[:10],
    })

    # Check 6: Every tcatbal references a valid account
    orphan_tcatbal = []
    for rec in tcatbal.get("records", []):
        if rec["TRANCAT-ACCT-ID"] not in acct_ids:
            orphan_tcatbal.append({"acct_id": rec["TRANCAT-ACCT-ID"]})
    checks.append({
        "check": "ref_integrity:tcatbal->account",
        "level": 3,
        "parent": "acctdata.ACCT-ID",
        "child": "tcatbal.TRANCAT-ACCT-ID",
        "total_children": len(tcatbal.get("records", [])),
        "orphans": len(orphan_tcatbal),
        "status": "PASS" if len(orphan_tcatbal) == 0 else "FAIL",
        "orphan_details": orphan_tcatbal[:10],
    })

    # Check 7: Every trancatg references a valid transaction type
    orphan_trancatg = []
    for rec in trancatg.get("records", []):
        if rec["TRAN-TYPE-CD"] not in tran_types:
            orphan_trancatg.append({"type_cd": rec["TRAN-TYPE-CD"], "cat_cd": rec["TRAN-CAT-CD"]})
    checks.append({
        "check": "ref_integrity:trancatg->trantype",
        "level": 3,
        "parent": "trantype.TRAN-TYPE",
        "child": "trancatg.TRAN-TYPE-CD",
        "total_children": len(trancatg.get("records", [])),
        "orphans": len(orphan_trancatg),
        "status": "PASS" if len(orphan_trancatg) == 0 else "FAIL",
        "orphan_details": orphan_trancatg[:10],
    })

    # Check 8: Every disclosure group references a valid account group
    orphan_discgrp = []
    for rec in discgrp.get("records", []):
        if rec["DIS-ACCT-GROUP-ID"] not in acct_group_ids:
            orphan_discgrp.append({"group_id": rec["DIS-ACCT-GROUP-ID"]})
    checks.append({
        "check": "ref_integrity:discgrp->acctgroup",
        "level": 3,
        "parent": "acctdata.ACCT-GROUP-ID",
        "child": "discgrp.DIS-ACCT-GROUP-ID",
        "total_children": len(discgrp.get("records", [])),
        "orphans": len(orphan_discgrp),
        "status": "PASS" if len(orphan_discgrp) == 0 else "FAIL",
        "orphan_details": orphan_discgrp[:10],
    })

    return checks


def main() -> int:
    script_dir = os.path.dirname(os.path.abspath(__file__))
    default_golden_dir = os.path.join(script_dir, "..", "golden-files")

    parser = argparse.ArgumentParser(description="Reconciliation checks for CardDemo migration")
    parser.add_argument("--golden-dir", default=default_golden_dir)
    parser.add_argument("--java-dir", default=None,
                        help="Path to Java output (omit for baseline-only)")
    parser.add_argument("--output", default="reconciliation-report.json")
    args = parser.parse_args()

    report = {
        "level_1_record_counts": check_record_counts(args.golden_dir, args.java_dir),
        "level_2_control_totals": check_control_totals(args.golden_dir, args.java_dir),
        "level_3_referential_integrity": check_referential_integrity(args.golden_dir),
    }

    # Summary
    all_checks = (
        report["level_1_record_counts"]
        + report["level_2_control_totals"]
        + report["level_3_referential_integrity"]
    )
    passed = sum(1 for c in all_checks if c["status"] == "PASS")
    failed = sum(1 for c in all_checks if c["status"] == "FAIL")
    skipped = sum(1 for c in all_checks if c["status"] == "SKIP")
    baseline = sum(1 for c in all_checks if c["status"] == "BASELINE")

    report["summary"] = {
        "total_checks": len(all_checks),
        "passed": passed,
        "failed": failed,
        "skipped": skipped,
        "baseline": baseline,
    }

    with open(args.output, "w") as f:
        json.dump(report, f, indent=2, default=str)

    print(f"\nReconciliation Report")
    print(f"  Total checks: {len(all_checks)}")
    print(f"  Passed:   {passed}")
    print(f"  Failed:   {failed}")
    print(f"  Skipped:  {skipped}")
    print(f"  Baseline: {baseline}")

    print(f"\nLevel 1 - Record Counts:")
    for c in report["level_1_record_counts"]:
        print(f"  {c['status']:8s} {c['check']} (source={c['source_count']})")

    print(f"\nLevel 2 - Control Totals:")
    for c in report["level_2_control_totals"]:
        print(f"  {c['status']:8s} {c['check']} (total={c['source_total']})")

    print(f"\nLevel 3 - Referential Integrity:")
    for c in report["level_3_referential_integrity"]:
        print(f"  {c['status']:8s} {c['check']} (orphans={c['orphans']}/{c['total_children']})")

    return 1 if failed > 0 else 0


if __name__ == "__main__":
    sys.exit(main())
