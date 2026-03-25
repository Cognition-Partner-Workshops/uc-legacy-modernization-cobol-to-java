"""
Cross-entity reconciliation checks for CardDemo migration validation.

Validates referential integrity, balance consistency, and data completeness
across the migrated data entities.
"""

import json
from pathlib import Path
from typing import Any


class ReconciliationResult:
    """Result of a single reconciliation check."""

    def __init__(self, check_id: str, description: str, passed: bool,
                 details: str = "", violations: list[str] | None = None):
        self.check_id = check_id
        self.description = description
        self.passed = passed
        self.details = details
        self.violations = violations or []

    def to_dict(self) -> dict[str, Any]:
        result = {
            "check_id": self.check_id,
            "description": self.description,
            "passed": self.passed,
            "details": self.details,
        }
        if self.violations:
            result["violation_count"] = len(self.violations)
            result["violations_sample"] = self.violations[:10]
        return result

    def __repr__(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        return f"[{status}] {self.check_id}: {self.description}"


def load_golden_file(filepath: str | Path) -> list[dict[str, Any]]:
    """Load a golden-file JSON and return the records list."""
    path = Path(filepath)
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    if isinstance(data, dict) and "records" in data:
        return data["records"]
    if isinstance(data, list):
        return data
    raise ValueError(f"Unexpected golden file format in {filepath}")


class ReconciliationRunner:
    """Executes all reconciliation checks against a set of golden files."""

    def __init__(self, golden_dir: str | Path):
        self.golden_dir = Path(golden_dir)
        self._cache: dict[str, list[dict[str, Any]]] = {}

    def _load(self, name: str) -> list[dict[str, Any]]:
        """Load and cache a golden file by base name (without extension)."""
        if name not in self._cache:
            path = self.golden_dir / f"{name}.json"
            if not path.exists():
                raise FileNotFoundError(f"Golden file not found: {path}")
            self._cache[name] = load_golden_file(path)
        return self._cache[name]

    def check_r01_card_references_account(self) -> ReconciliationResult:
        """R-01: Every card references an existing account."""
        cards = self._load("carddata")
        accounts = self._load("acctdata")
        acct_ids = {r["ACCT-ID"] for r in accounts}

        violations = []
        for card in cards:
            acct_id = card.get("CARD-ACCT-ID")
            if acct_id not in acct_ids:
                violations.append(
                    f"Card {card.get('CARD-NUM', '?')} references "
                    f"non-existent account {acct_id}"
                )

        return ReconciliationResult(
            check_id="R-01",
            description="Every card references an existing account",
            passed=len(violations) == 0,
            details=f"Checked {len(cards)} cards against {len(acct_ids)} accounts",
            violations=violations,
        )

    def check_r02_xref_integrity(self) -> ReconciliationResult:
        """R-02: Every cross-reference links valid card, customer, and account."""
        xrefs = self._load("cardxref")
        cards = self._load("carddata")
        customers = self._load("custdata")
        accounts = self._load("acctdata")

        card_nums = {r["CARD-NUM"] for r in cards}
        cust_ids = {r["CUST-ID"] for r in customers}
        acct_ids = {r["ACCT-ID"] for r in accounts}

        violations = []
        for xref in xrefs:
            card_num = xref.get("XREF-CARD-NUM", "").strip()
            cust_id = xref.get("XREF-CUST-ID")
            acct_id = xref.get("XREF-ACCT-ID")

            if card_num not in card_nums:
                violations.append(
                    f"XREF card {card_num} not found in carddata"
                )
            if cust_id not in cust_ids:
                violations.append(
                    f"XREF customer {cust_id} not found in custdata"
                )
            if acct_id not in acct_ids:
                violations.append(
                    f"XREF account {acct_id} not found in acctdata"
                )

        return ReconciliationResult(
            check_id="R-02",
            description="Every cross-reference links valid card, customer, and account",
            passed=len(violations) == 0,
            details=f"Checked {len(xrefs)} cross-references",
            violations=violations,
        )

    def check_r03_transaction_card_refs(self) -> ReconciliationResult:
        """R-03: Every daily transaction references a valid card number."""
        transactions = self._load("dailytran")
        cards = self._load("carddata")
        card_nums = {r["CARD-NUM"] for r in cards}

        violations = []
        for txn in transactions:
            card_num = txn.get("DALYTRAN-CARD-NUM", "").strip()
            if card_num and card_num not in card_nums:
                violations.append(
                    f"Transaction {txn.get('DALYTRAN-ID', '?')} references "
                    f"unknown card {card_num}"
                )

        return ReconciliationResult(
            check_id="R-03",
            description="Every daily transaction references a valid card number",
            passed=len(violations) == 0,
            details=f"Checked {len(transactions)} transactions against {len(card_nums)} cards",
            violations=violations,
        )

    def check_r04_category_balance_coverage(self) -> ReconciliationResult:
        """R-04: Transaction category balance records reference valid accounts."""
        tcatbal = self._load("tcatbal")
        accounts = self._load("acctdata")
        acct_ids = {r["ACCT-ID"] for r in accounts}

        violations = []
        for bal in tcatbal:
            acct_id = bal.get("TRANCAT-ACCT-ID")
            if acct_id not in acct_ids:
                violations.append(
                    f"Category balance for account {acct_id} not found in acctdata"
                )

        return ReconciliationResult(
            check_id="R-04",
            description="Transaction category balance records reference valid accounts",
            passed=len(violations) == 0,
            details=f"Checked {len(tcatbal)} category balance records",
            violations=violations,
        )

    def check_r05_disclosure_group_refs(self) -> ReconciliationResult:
        """R-05: Disclosure group entries use valid transaction types and accounts reference valid groups.

        Disclosure groups are keyed by a group ID (e.g. 'DEFAULT', 'ZEROAPR',
        or an account-specific ID like 'A000000000'). Accounts reference these
        groups via ACCT-GROUP-ID. This check validates:
        1. Transaction type codes in disclosure groups exist in the type master.
        2. Non-empty account group IDs reference an existing disclosure group.
        """
        discgrp = self._load("discgrp")
        accounts = self._load("acctdata")
        trantype = self._load("trantype")

        tran_types = {r["TRAN-TYPE"] for r in trantype}
        disc_group_ids = {r.get("DIS-ACCT-GROUP-ID", "").strip() for r in discgrp}

        violations = []

        # Validate transaction type codes within disclosure groups
        for grp in discgrp:
            type_cd = grp.get("DIS-TRAN-TYPE-CD", "").strip()
            if type_cd and type_cd not in tran_types:
                violations.append(
                    f"Disclosure group tran type '{type_cd}' not in trantype master"
                )

        # Validate that non-empty account group IDs reference a known disclosure group
        for acct in accounts:
            group_id = acct.get("ACCT-GROUP-ID", "").strip()
            if group_id and group_id not in disc_group_ids:
                violations.append(
                    f"Account {acct.get('ACCT-ID', '?')} group '{group_id}' "
                    f"not found in disclosure groups"
                )

        return ReconciliationResult(
            check_id="R-05",
            description="Disclosure group transaction types are valid and account groups reference existing disclosure groups",
            passed=len(violations) == 0,
            details=f"Checked {len(discgrp)} disclosure groups, {len(accounts)} account group references",
            violations=violations,
        )

    def check_r06_transaction_category_codes(self) -> ReconciliationResult:
        """R-06: Transaction category codes in dailytran exist in category master."""
        transactions = self._load("dailytran")
        trancatg = self._load("trancatg")

        valid_cats = {
            (r["TRAN-TYPE-CD"], r["TRAN-CAT-CD"])
            for r in trancatg
        }

        violations = []
        for txn in transactions:
            type_cd = txn.get("DALYTRAN-TYPE-CD", "").strip()
            cat_cd = txn.get("DALYTRAN-CAT-CD")
            if type_cd and (type_cd, cat_cd) not in valid_cats:
                violations.append(
                    f"Transaction {txn.get('DALYTRAN-ID', '?')} has "
                    f"category ({type_cd}, {cat_cd}) not in master"
                )

        return ReconciliationResult(
            check_id="R-06",
            description="Transaction category codes exist in the category master",
            passed=len(violations) == 0,
            details=f"Checked {len(transactions)} transactions against {len(valid_cats)} categories",
            violations=violations,
        )

    def check_r07_transaction_type_codes(self) -> ReconciliationResult:
        """R-07: Transaction type codes in dailytran exist in type master."""
        transactions = self._load("dailytran")
        trantype = self._load("trantype")
        valid_types = {r["TRAN-TYPE"] for r in trantype}

        violations = []
        for txn in transactions:
            type_cd = txn.get("DALYTRAN-TYPE-CD", "").strip()
            if type_cd and type_cd not in valid_types:
                violations.append(
                    f"Transaction {txn.get('DALYTRAN-ID', '?')} has "
                    f"type code '{type_cd}' not in master"
                )

        return ReconciliationResult(
            check_id="R-07",
            description="Transaction type codes exist in the type master",
            passed=len(violations) == 0,
            details=f"Checked {len(transactions)} transactions against {len(valid_types)} types",
            violations=violations,
        )

    def check_r08_record_counts(self,
                                expected_counts: dict[str, int] | None = None
                                ) -> ReconciliationResult:
        """R-08: Record counts are preserved across migration."""
        actual_counts = {}
        for name in ["acctdata", "carddata", "custdata", "cardxref",
                      "dailytran", "trantype", "trancatg", "tcatbal", "discgrp"]:
            try:
                records = self._load(name)
                actual_counts[name] = len(records)
            except FileNotFoundError:
                actual_counts[name] = -1

        if expected_counts is None:
            # When no expected counts provided, just verify all files loaded
            missing = [k for k, v in actual_counts.items() if v < 0]
            return ReconciliationResult(
                check_id="R-08",
                description="Record counts are preserved across migration",
                passed=len(missing) == 0,
                details=f"Counts: {actual_counts}",
                violations=[f"Missing file: {m}" for m in missing],
            )

        violations = []
        for name, exp_count in expected_counts.items():
            act_count = actual_counts.get(name, -1)
            if act_count != exp_count:
                violations.append(
                    f"{name}: expected {exp_count}, got {act_count}"
                )

        return ReconciliationResult(
            check_id="R-08",
            description="Record counts are preserved across migration",
            passed=len(violations) == 0,
            details=f"Expected: {expected_counts}, Actual: {actual_counts}",
            violations=violations,
        )

    def check_r09_account_status_consistency(self) -> ReconciliationResult:
        """R-09: Active accounts have valid open/expiration dates."""
        accounts = self._load("acctdata")

        violations = []
        for acct in accounts:
            status = acct.get("ACCT-ACTIVE-STATUS", "").strip()
            open_date = acct.get("ACCT-OPEN-DATE", "").strip()
            exp_date = acct.get("ACCT-EXPIRATION-DATE", "").strip()
            acct_id = acct.get("ACCT-ID", "?")

            if status == "Y":
                if not open_date or open_date == "0000-00-00":
                    violations.append(
                        f"Active account {acct_id} has no valid open date"
                    )
                if not exp_date or exp_date == "0000-00-00":
                    violations.append(
                        f"Active account {acct_id} has no valid expiration date"
                    )

        return ReconciliationResult(
            check_id="R-09",
            description="Active accounts have valid open and expiration dates",
            passed=len(violations) == 0,
            details=f"Checked {len(accounts)} accounts",
            violations=violations,
        )

    def check_r10_customer_card_symmetry(self) -> ReconciliationResult:
        """R-10: Customer-card relationship is symmetric via cross-reference."""
        xrefs = self._load("cardxref")
        customers = self._load("custdata")
        cards = self._load("carddata")

        # Every customer should have at least one card via xref
        cust_ids_in_xref = {r["XREF-CUST-ID"] for r in xrefs}
        cust_ids_in_custdata = {r["CUST-ID"] for r in customers}

        # Every card should appear in xref
        card_nums_in_xref = {r["XREF-CARD-NUM"].strip() for r in xrefs}
        card_nums_in_carddata = {r["CARD-NUM"] for r in cards}

        violations = []

        orphan_customers = cust_ids_in_custdata - cust_ids_in_xref
        for cust_id in sorted(orphan_customers):
            violations.append(
                f"Customer {cust_id} has no cross-reference entry"
            )

        orphan_cards = card_nums_in_carddata - card_nums_in_xref
        for card_num in sorted(orphan_cards):
            violations.append(
                f"Card {card_num} has no cross-reference entry"
            )

        return ReconciliationResult(
            check_id="R-10",
            description="Customer-card relationship is symmetric via cross-reference",
            passed=len(violations) == 0,
            details=(f"Customers in xref: {len(cust_ids_in_xref)}, "
                     f"Cards in xref: {len(card_nums_in_xref)}"),
            violations=violations,
        )

    def run_all(self, expected_counts: dict[str, int] | None = None
                ) -> list[ReconciliationResult]:
        """Execute all reconciliation checks and return results."""
        results = [
            self.check_r01_card_references_account(),
            self.check_r02_xref_integrity(),
            self.check_r03_transaction_card_refs(),
            self.check_r04_category_balance_coverage(),
            self.check_r05_disclosure_group_refs(),
            self.check_r06_transaction_category_codes(),
            self.check_r07_transaction_type_codes(),
            self.check_r08_record_counts(expected_counts),
            self.check_r09_account_status_consistency(),
            self.check_r10_customer_card_symmetry(),
        ]
        return results


def format_reconciliation_report(results: list[ReconciliationResult]) -> str:
    """Format reconciliation results into a human-readable report."""
    lines = []
    lines.append("=" * 72)
    lines.append("RECONCILIATION REPORT")
    lines.append("=" * 72)

    passed = sum(1 for r in results if r.passed)
    failed = sum(1 for r in results if not r.passed)
    lines.append(f"Total checks: {len(results)}")
    lines.append(f"Passed: {passed}")
    lines.append(f"Failed: {failed}")
    lines.append(f"Overall: {'PASS' if failed == 0 else 'FAIL'}")
    lines.append("")

    for result in results:
        status = "PASS" if result.passed else "FAIL"
        lines.append(f"[{status}] {result.check_id}: {result.description}")
        lines.append(f"  {result.details}")
        if result.violations:
            lines.append(f"  Violations ({len(result.violations)}):")
            for v in result.violations[:5]:
                lines.append(f"    - {v}")
            if len(result.violations) > 5:
                lines.append(f"    ... and {len(result.violations) - 5} more")
        lines.append("")

    lines.append("=" * 72)
    return "\n".join(lines)
