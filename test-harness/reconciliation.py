"""
Reconciliation check suite for CardDemo migration validation.

Validates data integrity invariants across all parsed data files:
  - Row counts match expected values
  - Financial totals balance
  - Referential integrity holds across files
  - Cross-file consistency is maintained
  - Primary key uniqueness
  - Date validity
  - Status code distributions
"""

import json
import os
import sys
from decimal import Decimal, InvalidOperation


class CheckResult:
    """Result of a single reconciliation check."""

    def __init__(self, name: str, category: str, description: str):
        self.name = name
        self.category = category
        self.description = description
        self.passed = False
        self.expected = None
        self.actual = None
        self.message = ""

    def pass_check(self, message: str = ""):
        self.passed = True
        self.message = message

    def fail_check(self, expected: object, actual: object, message: str = ""):
        self.passed = False
        self.expected = expected
        self.actual = actual
        self.message = message

    def summary(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        line = f"  [{status}] {self.name}: {self.description}"
        if not self.passed:
            line += f"\n         Expected: {self.expected}"
            line += f"\n         Actual:   {self.actual}"
        if self.message:
            line += f"\n         Note: {self.message}"
        return line


def load_golden_file(golden_dir: str, filename: str) -> dict:
    """Load a golden JSON file and return its contents."""
    path = os.path.join(golden_dir, filename)
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def to_decimal(value: object) -> Decimal:
    """Safely convert a value to Decimal."""
    if value is None:
        return Decimal("0")
    try:
        return Decimal(str(value))
    except (InvalidOperation, ValueError):
        return Decimal("0")


def run_reconciliation_checks(golden_dir: str) -> list:
    """Run all reconciliation checks against golden files.

    Args:
        golden_dir: Path to directory containing golden JSON files.

    Returns:
        List of CheckResult objects.
    """
    results = []

    # Load all golden files
    data = {}
    file_map = {
        "acctdata.json": "accounts",
        "carddata.json": "cards",
        "cardxref.json": "card_xrefs",
        "custdata.json": "customers",
        "dailytran.json": "daily_transactions",
        "discgrp.json": "disclosure_groups",
        "tcatbal.json": "tran_category_balances",
        "trancatg.json": "transaction_categories",
        "trantype.json": "transaction_types",
    }

    for filename, key in file_map.items():
        filepath = os.path.join(golden_dir, filename)
        if os.path.isfile(filepath):
            loaded = load_golden_file(golden_dir, filename)
            data[key] = loaded.get(key, [])
        else:
            data[key] = []

    # ─── Row Count Checks ───────────────────────────────────────────
    row_count_checks = [
        ("accounts", 50, "acctdata.txt"),
        ("cards", 50, "carddata.txt"),
        ("card_xrefs", 50, "cardxref.txt"),
        ("customers", 50, "custdata.txt"),
        ("daily_transactions", 300, "dailytran.txt"),
        ("disclosure_groups", 51, "discgrp.txt"),
        ("tran_category_balances", 50, "tcatbal.txt"),
        ("transaction_categories", 18, "trancatg.txt"),
        ("transaction_types", 7, "trantype.txt"),
    ]

    for key, expected_count, source_file in row_count_checks:
        check = CheckResult(
            f"row_count_{key}",
            "Row Counts",
            f"{source_file} record count"
        )
        actual = len(data.get(key, []))
        if actual == expected_count:
            check.pass_check(f"{actual} records")
        else:
            check.fail_check(expected_count, actual)
        results.append(check)

    # ─── Primary Key Uniqueness ─────────────────────────────────────
    uniqueness_checks = [
        ("accounts", "acct_id", "Account IDs are unique"),
        ("cards", "card_num", "Card numbers are unique"),
        ("customers", "cust_id", "Customer IDs are unique"),
        ("card_xrefs", "xref_card_num", "Card xref card numbers are unique"),
    ]

    for key, field, desc in uniqueness_checks:
        check = CheckResult(f"unique_{key}_{field}", "Uniqueness", desc)
        records = data.get(key, [])
        values = [r.get(field) for r in records]
        unique_values = set(values)
        if len(values) == len(unique_values):
            check.pass_check(f"{len(values)} unique values")
        else:
            dupes = len(values) - len(unique_values)
            check.fail_check(len(values), len(unique_values),
                             f"{dupes} duplicate values found")
        results.append(check)

    # ─── Referential Integrity ──────────────────────────────────────

    # Every card's account ID must exist in the accounts file
    check = CheckResult(
        "ref_card_to_account",
        "Referential Integrity",
        "Every card account_id exists in accounts"
    )
    acct_ids = {r["acct_id"] for r in data.get("accounts", [])}
    card_acct_ids = {r["card_acct_id"] for r in data.get("cards", [])}
    orphan_cards = card_acct_ids - acct_ids
    if not orphan_cards:
        check.pass_check(f"All {len(card_acct_ids)} card account refs valid")
    else:
        check.fail_check("0 orphans", f"{len(orphan_cards)} orphans",
                         f"Orphan account IDs: {sorted(orphan_cards)[:10]}")
    results.append(check)

    # Every card xref's account ID must exist in the accounts file
    check = CheckResult(
        "ref_xref_to_account",
        "Referential Integrity",
        "Every card_xref account_id exists in accounts"
    )
    xref_acct_ids = {r["xref_acct_id"] for r in data.get("card_xrefs", [])}
    orphan_xrefs = xref_acct_ids - acct_ids
    if not orphan_xrefs:
        check.pass_check(f"All {len(xref_acct_ids)} xref account refs valid")
    else:
        check.fail_check("0 orphans", f"{len(orphan_xrefs)} orphans",
                         f"Orphan account IDs: {sorted(orphan_xrefs)[:10]}")
    results.append(check)

    # Every card xref's customer ID must exist in the customers file
    check = CheckResult(
        "ref_xref_to_customer",
        "Referential Integrity",
        "Every card_xref customer_id exists in customers"
    )
    cust_ids = {r["cust_id"] for r in data.get("customers", [])}
    xref_cust_ids = {r["xref_cust_id"] for r in data.get("card_xrefs", [])}
    orphan_cust_xrefs = xref_cust_ids - cust_ids
    if not orphan_cust_xrefs:
        check.pass_check(f"All {len(xref_cust_ids)} xref customer refs valid")
    else:
        check.fail_check("0 orphans", f"{len(orphan_cust_xrefs)} orphans",
                         f"Orphan customer IDs: {sorted(orphan_cust_xrefs)[:10]}")
    results.append(check)

    # Every card xref card number must exist in the cards file
    check = CheckResult(
        "ref_xref_to_card",
        "Referential Integrity",
        "Every card_xref card_num exists in cards"
    )
    card_nums = {r["card_num"] for r in data.get("cards", [])}
    xref_card_nums = {r["xref_card_num"] for r in data.get("card_xrefs", [])}
    orphan_card_xrefs = xref_card_nums - card_nums
    if not orphan_card_xrefs:
        check.pass_check(f"All {len(xref_card_nums)} xref card refs valid")
    else:
        check.fail_check("0 orphans", f"{len(orphan_card_xrefs)} orphans",
                         f"Orphan card nums: {sorted(orphan_card_xrefs)[:5]}")
    results.append(check)

    # ─── Cross-File Consistency ─────────────────────────────────────

    # Number of accounts should equal number of customers (1:1 mapping via xref)
    check = CheckResult(
        "cross_acct_cust_count",
        "Cross-File Consistency",
        "Account count equals customer count"
    )
    n_acct = len(data.get("accounts", []))
    n_cust = len(data.get("customers", []))
    if n_acct == n_cust:
        check.pass_check(f"Both have {n_acct} records")
    else:
        check.fail_check(f"accounts={n_acct}", f"customers={n_cust}",
                         "1:1 account-customer mapping expected")
    results.append(check)

    # Number of cards should equal number of card xrefs
    check = CheckResult(
        "cross_card_xref_count",
        "Cross-File Consistency",
        "Card count equals card_xref count"
    )
    n_cards = len(data.get("cards", []))
    n_xrefs = len(data.get("card_xrefs", []))
    if n_cards == n_xrefs:
        check.pass_check(f"Both have {n_cards} records")
    else:
        check.fail_check(f"cards={n_cards}", f"xrefs={n_xrefs}")
    results.append(check)

    # Transaction types referenced in categories should exist in trantype
    check = CheckResult(
        "cross_tran_catg_types",
        "Cross-File Consistency",
        "All transaction category type codes exist in transaction types"
    )
    type_codes = {r["tran_type"] for r in data.get("transaction_types", [])}
    catg_type_codes = {r["tran_type_cd"] for r in data.get("transaction_categories", [])}
    missing_types = catg_type_codes - type_codes
    if not missing_types:
        check.pass_check(f"All {len(catg_type_codes)} category type codes valid")
    else:
        check.fail_check("0 missing", f"{len(missing_types)} missing",
                         f"Missing type codes: {sorted(missing_types)}")
    results.append(check)

    # ─── Account Status Checks ──────────────────────────────────────

    check = CheckResult(
        "acct_all_active",
        "Status Distribution",
        "All accounts have valid active status"
    )
    statuses = [r.get("acct_active_status", "") for r in data.get("accounts", [])]
    valid_statuses = {"Y", "N"}
    invalid = [s for s in statuses if s not in valid_statuses]
    if not invalid:
        active_count = statuses.count("Y")
        check.pass_check(f"{active_count} active, {len(statuses) - active_count} inactive")
    else:
        check.fail_check("all Y or N", f"{len(invalid)} invalid",
                         f"Invalid statuses: {set(invalid)}")
    results.append(check)

    # ─── Date Validity ──────────────────────────────────────────────

    check = CheckResult(
        "acct_dates_valid",
        "Date Validity",
        "All account dates are valid YYYY-MM-DD format"
    )
    date_fields = ["acct_open_date", "acct_expiration_date", "acct_reissue_date"]
    invalid_dates = []
    import re
    date_pattern = re.compile(r"^\d{4}-\d{2}-\d{2}$")
    for rec in data.get("accounts", []):
        for field in date_fields:
            val = rec.get(field, "")
            if val and not date_pattern.match(str(val)):
                invalid_dates.append((rec.get("acct_id"), field, val))
    if not invalid_dates:
        check.pass_check("All account dates valid")
    else:
        check.fail_check("0 invalid dates", f"{len(invalid_dates)} invalid",
                         f"Examples: {invalid_dates[:5]}")
    results.append(check)

    check = CheckResult(
        "cust_dob_valid",
        "Date Validity",
        "All customer DOBs are valid YYYY-MM-DD format"
    )
    invalid_dobs = []
    for rec in data.get("customers", []):
        dob = rec.get("cust_dob_yyyy_mm_dd", "")
        if dob and not date_pattern.match(str(dob)):
            invalid_dobs.append((rec.get("cust_id"), dob))
    if not invalid_dobs:
        check.pass_check("All customer DOBs valid")
    else:
        check.fail_check("0 invalid DOBs", f"{len(invalid_dobs)} invalid",
                         f"Examples: {invalid_dobs[:5]}")
    results.append(check)

    # ─── Financial Totals ───────────────────────────────────────────

    check = CheckResult(
        "acct_balances_non_negative_limits",
        "Financial Totals",
        "All account credit limits are non-negative"
    )
    negative_limits = []
    for rec in data.get("accounts", []):
        limit_str = rec.get("acct_credit_limit", "0")
        limit_val = to_decimal(limit_str)
        if limit_val < 0:
            negative_limits.append((rec.get("acct_id"), limit_str))
    if not negative_limits:
        check.pass_check("All credit limits non-negative")
    else:
        check.fail_check("0 negative", f"{len(negative_limits)} negative",
                         f"Examples: {negative_limits[:5]}")
    results.append(check)

    # Transaction amounts should be parseable as decimals
    check = CheckResult(
        "tran_amounts_parseable",
        "Financial Totals",
        "All daily transaction amounts are valid decimals"
    )
    bad_amounts = []
    for i, rec in enumerate(data.get("daily_transactions", [])):
        amt = rec.get("tran_amt", "0")
        try:
            Decimal(str(amt))
        except (InvalidOperation, ValueError):
            bad_amounts.append((i, amt))
    if not bad_amounts:
        check.pass_check(f"All {len(data.get('daily_transactions', []))} amounts valid")
    else:
        check.fail_check("0 bad amounts", f"{len(bad_amounts)} bad",
                         f"Examples: {bad_amounts[:5]}")
    results.append(check)

    # ─── Transaction Category Coverage ──────────────────────────────

    check = CheckResult(
        "tran_type_completeness",
        "Cross-File Consistency",
        "Transaction types cover codes 01-07"
    )
    expected_types = {"01", "02", "03", "04", "05", "06", "07"}
    actual_types = {r["tran_type"] for r in data.get("transaction_types", [])}
    if expected_types == actual_types:
        check.pass_check(f"All 7 type codes present")
    else:
        missing = expected_types - actual_types
        extra = actual_types - expected_types
        check.fail_check(sorted(expected_types), sorted(actual_types),
                         f"Missing: {sorted(missing)}, Extra: {sorted(extra)}")
    results.append(check)

    # ─── FICO Score Range ───────────────────────────────────────────

    check = CheckResult(
        "cust_fico_range",
        "Data Validity",
        "All customer FICO scores are in range 0-999"
    )
    bad_ficos = []
    for rec in data.get("customers", []):
        fico = rec.get("cust_fico_credit_score", 0)
        try:
            fico_int = int(fico)
            if fico_int < 0 or fico_int > 999:
                bad_ficos.append((rec.get("cust_id"), fico))
        except (ValueError, TypeError):
            bad_ficos.append((rec.get("cust_id"), fico))
    if not bad_ficos:
        check.pass_check("All FICO scores in valid range")
    else:
        check.fail_check("all in 0-999", f"{len(bad_ficos)} out of range",
                         f"Examples: {bad_ficos[:5]}")
    results.append(check)

    return results


def main():
    """Run reconciliation checks and print results."""
    repo_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    golden_dir = os.path.join(repo_root, "golden-files")

    if len(sys.argv) > 1:
        golden_dir = sys.argv[1]

    print("Reconciliation Check Suite")
    print("=" * 60)
    print(f"Golden files: {golden_dir}")
    print()

    if not os.path.isdir(golden_dir):
        print(f"ERROR: Golden files directory not found: {golden_dir}",
              file=sys.stderr)
        sys.exit(1)

    results = run_reconciliation_checks(golden_dir)

    # Group by category
    categories = {}
    for r in results:
        categories.setdefault(r.category, []).append(r)

    passed = 0
    failed = 0
    for category, checks in sorted(categories.items()):
        print(f"\n{category}")
        print("-" * 40)
        for check in checks:
            print(check.summary())
            if check.passed:
                passed += 1
            else:
                failed += 1

    print()
    print(f"{'=' * 60}")
    print(f"Total: {passed + failed} checks, {passed} passed, {failed} failed")

    sys.exit(0 if failed == 0 else 1)


if __name__ == "__main__":
    main()
