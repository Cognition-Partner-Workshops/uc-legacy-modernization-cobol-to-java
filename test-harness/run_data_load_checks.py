#!/usr/bin/env python3
"""
Run data-load reconciliation checks against the CardDemo ASCII data files.

These checks validate referential integrity and consistency of the static
data files *before* any batch processing -- i.e. the baseline data load.

Usage:
    python run_data_load_checks.py [--data-dir PATH]
"""

import argparse
import math
import os
import sys

from copybook_layouts import (
    ACCOUNT_LAYOUT,
    CARD_LAYOUT,
    CARD_XREF_LAYOUT,
    CUSTOMER_LAYOUT,
    DAILY_TRANSACTION_LAYOUT,
    DISCLOSURE_GROUP_LAYOUT,
    TRAN_CAT_BAL_LAYOUT,
    TRAN_CATEGORY_LAYOUT,
    TRAN_TYPE_LAYOUT,
)
from copybook_parser import parse_file
from reconciliation import (
    ReconciliationCheck,
    ReconciliationSuite,
    count_records,
    cross_reference,
    sum_field,
    unique_keys,
)


def build_suite(data_dir):
    """Build the data-load reconciliation suite.

    Args:
        data_dir: Path to the ASCII data directory.

    Returns:
        A ReconciliationSuite ready to run.
    """
    suite = ReconciliationSuite("CardDemo Data Load Reconciliation")

    acctdata = os.path.join(data_dir, "acctdata.txt")
    carddata = os.path.join(data_dir, "carddata.txt")
    custdata = os.path.join(data_dir, "custdata.txt")
    cardxref = os.path.join(data_dir, "cardxref.txt")
    dailytran = os.path.join(data_dir, "dailytran.txt")
    trantype = os.path.join(data_dir, "trantype.txt")
    trancatg = os.path.join(data_dir, "trancatg.txt")
    tcatbal = os.path.join(data_dir, "tcatbal.txt")
    discgrp = os.path.join(data_dir, "discgrp.txt")

    # RECON-09: Every CARD.CARD-ACCT-ID exists in ACCOUNT.ACCT-ID
    def check_card_acct_ref():
        xref = cross_reference(
            carddata, CARD_LAYOUT, ["CARD-ACCT-ID"],
            acctdata, ACCOUNT_LAYOUT, ["ACCT-ID"],
        )
        return xref["valid"], xref

    suite.add(ReconciliationCheck(
        "RECON-09",
        "Every CARD.CARD-ACCT-ID exists in ACCOUNT.ACCT-ID",
        check_card_acct_ref,
    ))

    # RECON-10: Every XREF.XREF-ACCT-ID exists in ACCOUNT.ACCT-ID
    def check_xref_acct_ref():
        xref = cross_reference(
            cardxref, CARD_XREF_LAYOUT, ["XREF-ACCT-ID"],
            acctdata, ACCOUNT_LAYOUT, ["ACCT-ID"],
        )
        return xref["valid"], xref

    suite.add(ReconciliationCheck(
        "RECON-10",
        "Every XREF.XREF-ACCT-ID exists in ACCOUNT.ACCT-ID",
        check_xref_acct_ref,
    ))

    # RECON-11: Every XREF.XREF-CARD-NUM exists in CARD.CARD-NUM
    def check_xref_card_ref():
        xref = cross_reference(
            cardxref, CARD_XREF_LAYOUT, ["XREF-CARD-NUM"],
            carddata, CARD_LAYOUT, ["CARD-NUM"],
        )
        return xref["valid"], xref

    suite.add(ReconciliationCheck(
        "RECON-11",
        "Every XREF.XREF-CARD-NUM exists in CARD.CARD-NUM",
        check_xref_card_ref,
    ))

    # RECON-12: Every XREF.XREF-CUST-ID exists in CUSTOMER.CUST-ID
    def check_xref_cust_ref():
        xref = cross_reference(
            cardxref, CARD_XREF_LAYOUT, ["XREF-CUST-ID"],
            custdata, CUSTOMER_LAYOUT, ["CUST-ID"],
        )
        return xref["valid"], xref

    suite.add(ReconciliationCheck(
        "RECON-12",
        "Every XREF.XREF-CUST-ID exists in CUSTOMER.CUST-ID",
        check_xref_cust_ref,
    ))

    # RECON-13: Record counts are non-zero for all master files
    def check_master_file_counts():
        counts = {
            "accounts": count_records(acctdata, ACCOUNT_LAYOUT),
            "cards": count_records(carddata, CARD_LAYOUT),
            "customers": count_records(custdata, CUSTOMER_LAYOUT),
            "card_xref": count_records(cardxref, CARD_XREF_LAYOUT),
            "tran_types": count_records(trantype, TRAN_TYPE_LAYOUT),
            "tran_categories": count_records(trancatg, TRAN_CATEGORY_LAYOUT),
        }
        all_nonzero = all(v > 0 for v in counts.values())
        return all_nonzero, counts

    suite.add(ReconciliationCheck(
        "RECON-13",
        "All master files have non-zero record counts",
        check_master_file_counts,
    ))

    # RECON-14: 1:1 correspondence between cards and xref entries
    def check_card_xref_count():
        card_count = count_records(carddata, CARD_LAYOUT)
        xref_count = count_records(cardxref, CARD_XREF_LAYOUT)
        passed = card_count == xref_count
        return passed, {
            "card_count": card_count,
            "xref_count": xref_count,
        }

    suite.add(ReconciliationCheck(
        "RECON-14",
        "Card file and cross-reference file have equal record counts",
        check_card_xref_count,
    ))

    # RECON-15: Account count matches tcatbal distinct account count
    def check_acct_tcatbal_coverage():
        acct_ids = unique_keys(acctdata, ACCOUNT_LAYOUT, ["ACCT-ID"])
        tcatbal_acct_ids = unique_keys(tcatbal, TRAN_CAT_BAL_LAYOUT, ["TRANCAT-ACCT-ID"])
        passed = acct_ids == tcatbal_acct_ids
        return passed, {
            "account_ids_count": len(acct_ids),
            "tcatbal_acct_ids_count": len(tcatbal_acct_ids),
            "in_acct_not_tcatbal": len(acct_ids - tcatbal_acct_ids),
            "in_tcatbal_not_acct": len(tcatbal_acct_ids - acct_ids),
        }

    suite.add(ReconciliationCheck(
        "RECON-15",
        "Account IDs in acctdata match distinct account IDs in tcatbal",
        check_acct_tcatbal_coverage,
    ))

    # RECON-16: All daily transactions reference valid cards via xref
    def check_dailytran_card_ref():
        xref = cross_reference(
            dailytran, DAILY_TRANSACTION_LAYOUT, ["DALYTRAN-CARD-NUM"],
            cardxref, CARD_XREF_LAYOUT, ["XREF-CARD-NUM"],
        )
        return xref["valid"], xref

    suite.add(ReconciliationCheck(
        "RECON-16",
        "Every DAILYTRAN.DALYTRAN-CARD-NUM exists in CARDXREF.XREF-CARD-NUM",
        check_dailytran_card_ref,
    ))

    # RECON-17: Transaction types referenced by categories all exist
    def check_trancatg_type_ref():
        xref = cross_reference(
            trancatg, TRAN_CATEGORY_LAYOUT, ["TRAN-TYPE-CD"],
            trantype, TRAN_TYPE_LAYOUT, ["TRAN-TYPE"],
        )
        return xref["valid"], xref

    suite.add(ReconciliationCheck(
        "RECON-17",
        "Every TRANCATG.TRAN-TYPE-CD exists in TRANTYPE.TRAN-TYPE",
        check_trancatg_type_ref,
    ))

    return suite


def main():
    parser = argparse.ArgumentParser(
        description="Run data-load reconciliation checks."
    )
    parser.add_argument(
        "--data-dir",
        default=os.path.join(os.path.dirname(__file__), "..", "app", "data", "ASCII"),
        help="Path to the ASCII data directory.",
    )
    args = parser.parse_args()

    data_dir = os.path.abspath(args.data_dir)
    if not os.path.isdir(data_dir):
        print(f"ERROR: Data directory not found: {data_dir}", file=sys.stderr)
        sys.exit(1)

    suite = build_suite(data_dir)
    report = suite.run_and_print()

    sys.exit(0 if report["all_passed"] else 1)


if __name__ == "__main__":
    main()
