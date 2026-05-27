"""
Reconciliation runner for post-batch-job validation.

Orchestrates per-job integrity checks to verify data consistency
after batch processing in the migrated Java system.
"""

import json
import importlib
from dataclasses import dataclass, field
from pathlib import Path
from typing import Callable


@dataclass
class CheckResult:
    """Result of a single reconciliation check."""
    check_name: str
    job_name: str
    passed: bool
    expected: object = None
    actual: object = None
    message: str = ""
    details: dict = field(default_factory=dict)


@dataclass
class ReconciliationReport:
    """Aggregate report of all reconciliation checks for a job."""
    job_name: str
    total_checks: int = 0
    passed_checks: int = 0
    failed_checks: int = 0
    results: list = field(default_factory=list)

    @property
    def passed(self) -> bool:
        return self.failed_checks == 0

    def to_dict(self) -> dict:
        return {
            "job_name": self.job_name,
            "passed": self.passed,
            "total_checks": self.total_checks,
            "passed_checks": self.passed_checks,
            "failed_checks": self.failed_checks,
            "results": [
                {
                    "check_name": r.check_name,
                    "job_name": r.job_name,
                    "passed": r.passed,
                    "expected": r.expected,
                    "actual": r.actual,
                    "message": r.message,
                    "details": r.details,
                }
                for r in self.results
            ],
        }


def load_records(file_path: str) -> list[dict]:
    """Load parsed records from a JSON file.

    Args:
        file_path: Path to the golden or output JSON file

    Returns:
        List of record dictionaries
    """
    with Path(file_path).open("r") as f:
        data = json.load(f)
    return data.get("records", [])


def check_record_count(records: list[dict], expected_count: int, label: str) -> CheckResult:
    """Verify record count matches expected value.

    Args:
        records: List of parsed records
        expected_count: Expected number of records
        label: Description label for the check

    Returns:
        CheckResult indicating pass/fail
    """
    actual_count = len(records)
    return CheckResult(
        check_name=f"record_count_{label}",
        job_name="",
        passed=(actual_count == expected_count),
        expected=expected_count,
        actual=actual_count,
        message=(
            f"Record count OK: {actual_count}"
            if actual_count == expected_count
            else f"Record count mismatch: expected {expected_count}, got {actual_count}"
        ),
    )


def check_referential_integrity(
    parent_records: list[dict],
    child_records: list[dict],
    parent_key: str,
    child_key: str,
    label: str,
) -> CheckResult:
    """Verify all child records reference valid parent records.

    Args:
        parent_records: List of parent entity records
        child_records: List of child entity records
        parent_key: Field name for the parent key
        child_key: Field name for the child foreign key
        label: Description label for the check

    Returns:
        CheckResult with orphan details if any
    """
    parent_ids = {r[parent_key] for r in parent_records if parent_key in r}
    orphans = []

    for i, child in enumerate(child_records):
        if child_key in child and child[child_key] not in parent_ids:
            orphans.append({"index": i, "value": child[child_key]})

    return CheckResult(
        check_name=f"referential_integrity_{label}",
        job_name="",
        passed=(len(orphans) == 0),
        expected=0,
        actual=len(orphans),
        message=(
            f"Referential integrity OK: all {child_key} values reference valid {parent_key}"
            if not orphans
            else f"Found {len(orphans)} orphan records with invalid {child_key} references"
        ),
        details={"orphans": orphans[:20]} if orphans else {},
    )


def check_balance_consistency(
    account_records: list[dict],
    category_balance_records: list[dict],
) -> CheckResult:
    """Verify category balances are consistent with account data.

    Each account's category balance records should exist for valid accounts.

    Args:
        account_records: Parsed account records
        category_balance_records: Parsed tcatbal records

    Returns:
        CheckResult indicating consistency
    """
    account_ids = {r["ACCT-ID"] for r in account_records if "ACCT-ID" in r}
    invalid_refs = []

    for i, catbal in enumerate(category_balance_records):
        acct_id = catbal.get("TRANCAT-ACCT-ID")
        if acct_id is not None and acct_id not in account_ids:
            invalid_refs.append({"index": i, "acct_id": acct_id})

    return CheckResult(
        check_name="balance_account_reference",
        job_name="POSTTRAN",
        passed=(len(invalid_refs) == 0),
        expected=0,
        actual=len(invalid_refs),
        message=(
            "All category balance records reference valid accounts"
            if not invalid_refs
            else f"Found {len(invalid_refs)} category balance records with invalid account references"
        ),
        details={"invalid_refs": invalid_refs[:20]} if invalid_refs else {},
    )


def check_xref_completeness(
    card_records: list[dict],
    xref_records: list[dict],
    account_records: list[dict],
    customer_records: list[dict],
) -> list[CheckResult]:
    """Verify cross-reference file links valid cards, accounts, and customers.

    Args:
        card_records: Parsed card records
        xref_records: Parsed cross-reference records
        account_records: Parsed account records
        customer_records: Parsed customer records

    Returns:
        List of CheckResults for each referential check
    """
    results = []

    # Check xref -> card
    card_nums = {r["CARD-NUM"] for r in card_records if "CARD-NUM" in r}
    xref_card_orphans = [
        {"index": i, "card_num": r.get("XREF-CARD-NUM")}
        for i, r in enumerate(xref_records)
        if r.get("XREF-CARD-NUM") and r["XREF-CARD-NUM"] not in card_nums
    ]
    results.append(CheckResult(
        check_name="xref_card_reference",
        job_name="XREFFILE",
        passed=(len(xref_card_orphans) == 0),
        expected=0,
        actual=len(xref_card_orphans),
        message=(
            "All XREF card numbers reference valid cards"
            if not xref_card_orphans
            else f"Found {len(xref_card_orphans)} XREF records with invalid card references"
        ),
        details={"orphans": xref_card_orphans[:10]} if xref_card_orphans else {},
    ))

    # Check xref -> account
    account_ids = {r["ACCT-ID"] for r in account_records if "ACCT-ID" in r}
    xref_acct_orphans = [
        {"index": i, "acct_id": r.get("XREF-ACCT-ID")}
        for i, r in enumerate(xref_records)
        if r.get("XREF-ACCT-ID") and r["XREF-ACCT-ID"] not in account_ids
    ]
    results.append(CheckResult(
        check_name="xref_account_reference",
        job_name="XREFFILE",
        passed=(len(xref_acct_orphans) == 0),
        expected=0,
        actual=len(xref_acct_orphans),
        message=(
            "All XREF account IDs reference valid accounts"
            if not xref_acct_orphans
            else f"Found {len(xref_acct_orphans)} XREF records with invalid account references"
        ),
        details={"orphans": xref_acct_orphans[:10]} if xref_acct_orphans else {},
    ))

    # Check xref -> customer
    cust_ids = {r["CUST-ID"] for r in customer_records if "CUST-ID" in r}
    xref_cust_orphans = [
        {"index": i, "cust_id": r.get("XREF-CUST-ID")}
        for i, r in enumerate(xref_records)
        if r.get("XREF-CUST-ID") and r["XREF-CUST-ID"] not in cust_ids
    ]
    results.append(CheckResult(
        check_name="xref_customer_reference",
        job_name="XREFFILE",
        passed=(len(xref_cust_orphans) == 0),
        expected=0,
        actual=len(xref_cust_orphans),
        message=(
            "All XREF customer IDs reference valid customers"
            if not xref_cust_orphans
            else f"Found {len(xref_cust_orphans)} XREF records with invalid customer references"
        ),
        details={"orphans": xref_cust_orphans[:10]} if xref_cust_orphans else {},
    ))

    return results


def check_transaction_types_valid(
    transaction_records: list[dict],
    type_records: list[dict],
    category_records: list[dict],
) -> list[CheckResult]:
    """Verify transactions reference valid types and categories.

    Args:
        transaction_records: Parsed daily transaction records
        type_records: Parsed transaction type reference records
        category_records: Parsed transaction category records

    Returns:
        List of CheckResults
    """
    results = []

    valid_types = {r["TRAN-TYPE"] for r in type_records if "TRAN-TYPE" in r}
    invalid_type_refs = [
        {"index": i, "type_cd": r.get("TRAN-TYPE-CD")}
        for i, r in enumerate(transaction_records)
        if r.get("TRAN-TYPE-CD") and r["TRAN-TYPE-CD"].strip() not in valid_types
    ]
    results.append(CheckResult(
        check_name="transaction_type_reference",
        job_name="TRANFILE",
        passed=(len(invalid_type_refs) == 0),
        expected=0,
        actual=len(invalid_type_refs),
        message=(
            "All transactions reference valid transaction types"
            if not invalid_type_refs
            else f"Found {len(invalid_type_refs)} transactions with invalid type codes"
        ),
        details={"invalid": invalid_type_refs[:10]} if invalid_type_refs else {},
    ))

    valid_cats = {
        (r.get("TRAN-TYPE-CD", "").strip(), r.get("TRAN-CAT-CD"))
        for r in category_records
    }
    invalid_cat_refs = [
        {"index": i, "type_cd": r.get("TRAN-TYPE-CD"), "cat_cd": r.get("TRAN-CAT-CD")}
        for i, r in enumerate(transaction_records)
        if (r.get("TRAN-TYPE-CD", "").strip(), r.get("TRAN-CAT-CD")) not in valid_cats
    ]
    results.append(CheckResult(
        check_name="transaction_category_reference",
        job_name="TRANFILE",
        passed=(len(invalid_cat_refs) == 0),
        expected=0,
        actual=len(invalid_cat_refs),
        message=(
            "All transactions reference valid type/category combinations"
            if not invalid_cat_refs
            else f"Found {len(invalid_cat_refs)} transactions with invalid category codes"
        ),
        details={"invalid": invalid_cat_refs[:10]} if invalid_cat_refs else {},
    ))

    return results


def run_reconciliation(golden_files_dir: str) -> dict[str, ReconciliationReport]:
    """Run all reconciliation checks against parsed golden files.

    Args:
        golden_files_dir: Path to directory containing golden JSON files

    Returns:
        Dictionary mapping job names to their ReconciliationReports
    """
    gf_dir = Path(golden_files_dir)

    # Load all available golden files
    datasets = {}
    file_map = {
        "acctdata": "acctdata.golden.json",
        "carddata": "carddata.golden.json",
        "custdata": "custdata.golden.json",
        "dailytran": "dailytran.golden.json",
        "cardxref": "cardxref.golden.json",
        "trantype": "trantype.golden.json",
        "trancatg": "trancatg.golden.json",
        "discgrp": "discgrp.golden.json",
        "tcatbal": "tcatbal.golden.json",
    }

    for key, filename in file_map.items():
        filepath = gf_dir / filename
        if filepath.exists():
            datasets[key] = load_records(str(filepath))

    reports = {}

    # POSTTRAN reconciliation
    posttran_report = ReconciliationReport(job_name="POSTTRAN")
    if "acctdata" in datasets and "tcatbal" in datasets:
        result = check_balance_consistency(datasets["acctdata"], datasets["tcatbal"])
        result.job_name = "POSTTRAN"
        posttran_report.results.append(result)
    posttran_report.total_checks = len(posttran_report.results)
    posttran_report.passed_checks = sum(1 for r in posttran_report.results if r.passed)
    posttran_report.failed_checks = posttran_report.total_checks - posttran_report.passed_checks
    reports["POSTTRAN"] = posttran_report

    # XREFFILE reconciliation
    xref_report = ReconciliationReport(job_name="XREFFILE")
    if all(k in datasets for k in ["carddata", "cardxref", "acctdata", "custdata"]):
        xref_results = check_xref_completeness(
            datasets["carddata"], datasets["cardxref"],
            datasets["acctdata"], datasets["custdata"],
        )
        for r in xref_results:
            xref_report.results.append(r)
    xref_report.total_checks = len(xref_report.results)
    xref_report.passed_checks = sum(1 for r in xref_report.results if r.passed)
    xref_report.failed_checks = xref_report.total_checks - xref_report.passed_checks
    reports["XREFFILE"] = xref_report

    # TRANFILE reconciliation
    tran_report = ReconciliationReport(job_name="TRANFILE")
    if all(k in datasets for k in ["dailytran", "trantype", "trancatg"]):
        tran_results = check_transaction_types_valid(
            datasets["dailytran"], datasets["trantype"], datasets["trancatg"],
        )
        for r in tran_results:
            tran_report.results.append(r)
    tran_report.total_checks = len(tran_report.results)
    tran_report.passed_checks = sum(1 for r in tran_report.results if r.passed)
    tran_report.failed_checks = tran_report.total_checks - tran_report.passed_checks
    reports["TRANFILE"] = tran_report

    # Record count checks
    count_report = ReconciliationReport(job_name="DATA_INTEGRITY")
    if "acctdata" in datasets:
        r = check_record_count(datasets["acctdata"], 50, "accounts")
        r.job_name = "DATA_INTEGRITY"
        count_report.results.append(r)
    if "carddata" in datasets:
        r = check_record_count(datasets["carddata"], 50, "cards")
        r.job_name = "DATA_INTEGRITY"
        count_report.results.append(r)
    if "custdata" in datasets:
        r = check_record_count(datasets["custdata"], 50, "customers")
        r.job_name = "DATA_INTEGRITY"
        count_report.results.append(r)
    if "dailytran" in datasets:
        r = check_record_count(datasets["dailytran"], 300, "transactions")
        r.job_name = "DATA_INTEGRITY"
        count_report.results.append(r)
    if "cardxref" in datasets:
        r = check_record_count(datasets["cardxref"], 50, "xref")
        r.job_name = "DATA_INTEGRITY"
        count_report.results.append(r)
    count_report.total_checks = len(count_report.results)
    count_report.passed_checks = sum(1 for r in count_report.results if r.passed)
    count_report.failed_checks = count_report.total_checks - count_report.passed_checks
    reports["DATA_INTEGRITY"] = count_report

    return reports
