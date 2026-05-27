"""
Reconciliation checks for CardDemo migration validation.

Validates cross-file referential integrity, aggregate consistency,
and business rule invariants across the migrated dataset.
"""

from __future__ import annotations

import json
import os
from dataclasses import dataclass, field
from typing import Optional


@dataclass
class Violation:
    """A single reconciliation violation."""
    check_id: str
    severity: str  # "ERROR" or "WARNING"
    message: str
    record_index: Optional[int] = None
    field_name: Optional[str] = None
    field_value: Optional[object] = None


@dataclass
class CheckResult:
    """Result of a single reconciliation check."""
    check_id: str
    description: str
    passed: bool
    violations: list[Violation] = field(default_factory=list)

    @property
    def summary(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        detail = f" ({len(self.violations)} violations)" if self.violations else ""
        return f"[{status}] {self.check_id}: {self.description}{detail}"


def load_golden_file(golden_dir: str, basename: str) -> list[dict]:
    """Load records from a golden JSON file."""
    path = os.path.join(golden_dir, f"{basename}.golden.json")
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    return data["records"]


def _extract_field_set(records: list[dict], field_name: str) -> set:
    values = set()
    for r in records:
        val = r.get(field_name)
        if val is not None:
            values.add(str(val).strip())
    return values


# --- Referential Integrity Checks ---

def check_ri_001(xref: list[dict], cards: list[dict]) -> CheckResult:
    """RI-001: Every XREF-CARD-NUM must exist in carddata CARD-NUM."""
    card_nums = _extract_field_set(cards, "CARD-NUM")
    violations = []
    for i, rec in enumerate(xref):
        card_num = str(rec.get("XREF-CARD-NUM", "")).strip()
        if card_num and card_num not in card_nums:
            violations.append(Violation(
                check_id="RI-001",
                severity="ERROR",
                message=f"XREF-CARD-NUM '{card_num}' not found in carddata",
                record_index=i,
                field_name="XREF-CARD-NUM",
                field_value=card_num,
            ))
    return CheckResult(
        check_id="RI-001",
        description="Every XREF-CARD-NUM in cardxref must exist in carddata CARD-NUM",
        passed=len(violations) == 0,
        violations=violations,
    )


def check_ri_002(xref: list[dict], accounts: list[dict]) -> CheckResult:
    """RI-002: Every XREF-ACCT-ID must exist in acctdata ACCT-ID."""
    acct_ids = _extract_field_set(accounts, "ACCT-ID")
    violations = []
    for i, rec in enumerate(xref):
        acct_id = str(rec.get("XREF-ACCT-ID", "")).strip()
        if acct_id and acct_id not in acct_ids:
            violations.append(Violation(
                check_id="RI-002",
                severity="ERROR",
                message=f"XREF-ACCT-ID '{acct_id}' not found in acctdata",
                record_index=i,
                field_name="XREF-ACCT-ID",
                field_value=acct_id,
            ))
    return CheckResult(
        check_id="RI-002",
        description="Every XREF-ACCT-ID in cardxref must exist in acctdata ACCT-ID",
        passed=len(violations) == 0,
        violations=violations,
    )


def check_ri_003(xref: list[dict], customers: list[dict]) -> CheckResult:
    """RI-003: Every XREF-CUST-ID must exist in custdata CUST-ID."""
    cust_ids = _extract_field_set(customers, "CUST-ID")
    violations = []
    for i, rec in enumerate(xref):
        cust_id = str(rec.get("XREF-CUST-ID", "")).strip()
        if cust_id and cust_id not in cust_ids:
            violations.append(Violation(
                check_id="RI-003",
                severity="ERROR",
                message=f"XREF-CUST-ID '{cust_id}' not found in custdata",
                record_index=i,
                field_name="XREF-CUST-ID",
                field_value=cust_id,
            ))
    return CheckResult(
        check_id="RI-003",
        description="Every XREF-CUST-ID in cardxref must exist in custdata CUST-ID",
        passed=len(violations) == 0,
        violations=violations,
    )


def check_ri_004(cards: list[dict], accounts: list[dict]) -> CheckResult:
    """RI-004: Every CARD-ACCT-ID must exist in acctdata ACCT-ID."""
    acct_ids = _extract_field_set(accounts, "ACCT-ID")
    violations = []
    for i, rec in enumerate(cards):
        acct_id = str(rec.get("CARD-ACCT-ID", "")).strip()
        if acct_id and acct_id not in acct_ids:
            violations.append(Violation(
                check_id="RI-004",
                severity="ERROR",
                message=f"CARD-ACCT-ID '{acct_id}' not found in acctdata",
                record_index=i,
                field_name="CARD-ACCT-ID",
                field_value=acct_id,
            ))
    return CheckResult(
        check_id="RI-004",
        description="Every CARD-ACCT-ID in carddata must exist in acctdata ACCT-ID",
        passed=len(violations) == 0,
        violations=violations,
    )


def check_ri_005(dailytran: list[dict], xref: list[dict]) -> CheckResult:
    """RI-005: Every DALYTRAN-CARD-NUM must exist in cardxref XREF-CARD-NUM."""
    xref_cards = _extract_field_set(xref, "XREF-CARD-NUM")
    violations = []
    for i, rec in enumerate(dailytran):
        card_num = str(rec.get("DALYTRAN-CARD-NUM", "")).strip()
        if card_num and card_num not in xref_cards:
            violations.append(Violation(
                check_id="RI-005",
                severity="ERROR",
                message=f"DALYTRAN-CARD-NUM '{card_num}' not found in cardxref",
                record_index=i,
                field_name="DALYTRAN-CARD-NUM",
                field_value=card_num,
            ))
    return CheckResult(
        check_id="RI-005",
        description="Every DALYTRAN-CARD-NUM in dailytran must exist in cardxref",
        passed=len(violations) == 0,
        violations=violations,
    )


def check_ri_006(tcatbal: list[dict], accounts: list[dict]) -> CheckResult:
    """RI-006: Every TRANCAT-ACCT-ID must exist in acctdata ACCT-ID."""
    acct_ids = _extract_field_set(accounts, "ACCT-ID")
    violations = []
    for i, rec in enumerate(tcatbal):
        acct_id = str(rec.get("TRANCAT-ACCT-ID", "")).strip()
        if acct_id and acct_id not in acct_ids:
            violations.append(Violation(
                check_id="RI-006",
                severity="ERROR",
                message=f"TRANCAT-ACCT-ID '{acct_id}' not found in acctdata",
                record_index=i,
                field_name="TRANCAT-ACCT-ID",
                field_value=acct_id,
            ))
    return CheckResult(
        check_id="RI-006",
        description="Every TRANCAT-ACCT-ID in tcatbal must exist in acctdata ACCT-ID",
        passed=len(violations) == 0,
        violations=violations,
    )


# --- Aggregate Consistency Checks ---

def check_ag_002(
    golden_dir: str,
    migrated_dir: str,
) -> CheckResult:
    """AG-002: Record counts per entity must match between COBOL and Java."""
    violations = []
    for basename in ["acctdata", "custdata", "carddata", "cardxref",
                     "dailytran", "trantype", "trancatg", "tcatbal", "discgrp"]:
        golden_path = os.path.join(golden_dir, f"{basename}.golden.json")
        migrated_path = os.path.join(migrated_dir, f"{basename}.golden.json")

        if not os.path.exists(golden_path):
            continue
        if not os.path.exists(migrated_path):
            violations.append(Violation(
                check_id="AG-002",
                severity="ERROR",
                message=f"Migrated file not found: {basename}.golden.json",
            ))
            continue

        with open(golden_path, "r") as f:
            golden_count = json.load(f)["metadata"]["record_count"]
        with open(migrated_path, "r") as f:
            migrated_count = json.load(f)["metadata"]["record_count"]

        if golden_count != migrated_count:
            violations.append(Violation(
                check_id="AG-002",
                severity="ERROR",
                message=(
                    f"{basename}: expected {golden_count} records, "
                    f"got {migrated_count}"
                ),
            ))

    return CheckResult(
        check_id="AG-002",
        description="Record counts per entity must match between COBOL and Java",
        passed=len(violations) == 0,
        violations=violations,
    )


# --- Business Rule Checks ---

def check_br_003(cards: list[dict], accounts: list[dict]) -> CheckResult:
    """BR-003: Every active card must reference an active account."""
    acct_status = {}
    for rec in accounts:
        acct_id = str(rec.get("ACCT-ID", "")).strip()
        status = str(rec.get("ACCT-ACTIVE-STATUS", "")).strip()
        acct_status[acct_id] = status

    violations = []
    for i, rec in enumerate(cards):
        card_status = str(rec.get("CARD-ACTIVE-STATUS", "")).strip()
        if card_status != "Y":
            continue
        acct_id = str(rec.get("CARD-ACCT-ID", "")).strip()
        acct_stat = acct_status.get(acct_id)
        if acct_stat is None:
            violations.append(Violation(
                check_id="BR-003",
                severity="ERROR",
                message=f"Active card references non-existent account {acct_id}",
                record_index=i,
                field_name="CARD-ACCT-ID",
                field_value=acct_id,
            ))
        elif acct_stat != "Y":
            violations.append(Violation(
                check_id="BR-003",
                severity="ERROR",
                message=(
                    f"Active card references inactive account {acct_id} "
                    f"(status='{acct_stat}')"
                ),
                record_index=i,
                field_name="CARD-ACCT-ID",
                field_value=acct_id,
            ))

    return CheckResult(
        check_id="BR-003",
        description="Every active card must reference an active account",
        passed=len(violations) == 0,
        violations=violations,
    )


def check_br_004(dailytran: list[dict], trantypes: list[dict]) -> CheckResult:
    """BR-004: Transaction type codes in dailytran must exist in trantype."""
    valid_types = _extract_field_set(trantypes, "TRAN-TYPE")
    violations = []
    for i, rec in enumerate(dailytran):
        type_cd = str(rec.get("DALYTRAN-TYPE-CD", "")).strip()
        if type_cd and type_cd not in valid_types:
            violations.append(Violation(
                check_id="BR-004",
                severity="ERROR",
                message=f"DALYTRAN-TYPE-CD '{type_cd}' not in trantype",
                record_index=i,
                field_name="DALYTRAN-TYPE-CD",
                field_value=type_cd,
            ))
    return CheckResult(
        check_id="BR-004",
        description="Transaction type codes in dailytran must exist in trantype",
        passed=len(violations) == 0,
        violations=violations,
    )


def check_br_005(dailytran: list[dict], trancatg: list[dict]) -> CheckResult:
    """BR-005: Transaction category codes must exist in trancatg for the type."""
    valid_combos = set()
    for rec in trancatg:
        type_cd = str(rec.get("TRAN-TYPE-CD", "")).strip()
        cat_cd = str(rec.get("TRAN-CAT-CD", "")).strip()
        valid_combos.add((type_cd, cat_cd))

    violations = []
    for i, rec in enumerate(dailytran):
        type_cd = str(rec.get("DALYTRAN-TYPE-CD", "")).strip()
        cat_cd = str(rec.get("DALYTRAN-CAT-CD", "")).strip()
        if type_cd and cat_cd and (type_cd, cat_cd) not in valid_combos:
            violations.append(Violation(
                check_id="BR-005",
                severity="ERROR",
                message=(
                    f"DALYTRAN type/category '{type_cd}/{cat_cd}' "
                    f"not in trancatg"
                ),
                record_index=i,
                field_name="DALYTRAN-CAT-CD",
                field_value=f"{type_cd}/{cat_cd}",
            ))
    return CheckResult(
        check_id="BR-005",
        description="Transaction category codes must exist in trancatg for the type",
        passed=len(violations) == 0,
        violations=violations,
    )


def run_all_reconciliation_checks(golden_dir: str) -> list[CheckResult]:
    """Run all reconciliation checks against golden file data.

    Args:
        golden_dir: Path to the golden-files directory.

    Returns:
        List of CheckResult objects for each check executed.
    """
    accounts = load_golden_file(golden_dir, "acctdata")
    customers = load_golden_file(golden_dir, "custdata")
    cards = load_golden_file(golden_dir, "carddata")
    xref = load_golden_file(golden_dir, "cardxref")
    dailytran = load_golden_file(golden_dir, "dailytran")
    trantypes = load_golden_file(golden_dir, "trantype")
    trancatg = load_golden_file(golden_dir, "trancatg")
    tcatbal = load_golden_file(golden_dir, "tcatbal")

    results = [
        check_ri_001(xref, cards),
        check_ri_002(xref, accounts),
        check_ri_003(xref, customers),
        check_ri_004(cards, accounts),
        check_ri_005(dailytran, xref),
        check_ri_006(tcatbal, accounts),
        check_br_003(cards, accounts),
        check_br_004(dailytran, trantypes),
        check_br_005(dailytran, trancatg),
    ]

    return results
