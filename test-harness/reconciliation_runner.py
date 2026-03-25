#!/usr/bin/env python3
"""
Reconciliation check executor for CardDemo migration.

Loads post-execution data (JSON files) and evaluates a catalogue of data-
integrity invariants.  Each check returns PASS, FAIL, or WARN with details.

Usage:
    python reconciliation_runner.py <data_dir> [--checks all|record_counts|...]
"""

from __future__ import annotations

import json
import sys
from collections import Counter
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any


# ---------------------------------------------------------------------------
# Check result model
# ---------------------------------------------------------------------------
@dataclass
class CheckResult:
    """Outcome of a single reconciliation check."""
    check_name: str
    status: str          # "PASS", "FAIL", "WARN"
    message: str = ""
    details: dict[str, Any] = field(default_factory=dict)


@dataclass
class ReconciliationReport:
    """Aggregated report of all reconciliation checks."""
    data_dir: str
    results: list[CheckResult] = field(default_factory=list)

    @property
    def passed(self) -> bool:
        return all(r.status == "PASS" for r in self.results)

    @property
    def pass_count(self) -> int:
        return sum(1 for r in self.results if r.status == "PASS")

    @property
    def fail_count(self) -> int:
        return sum(1 for r in self.results if r.status == "FAIL")

    @property
    def warn_count(self) -> int:
        return sum(1 for r in self.results if r.status == "WARN")

    def summary(self) -> str:
        overall = "PASS" if self.passed else "FAIL"
        lines = [
            f"[{overall}] Reconciliation Report for {self.data_dir}",
            f"  Checks: {len(self.results)}  "
            f"PASS: {self.pass_count}  FAIL: {self.fail_count}  WARN: {self.warn_count}",
            "",
        ]
        for r in self.results:
            lines.append(f"  [{r.status}] {r.check_name}: {r.message}")
            if r.details:
                for k, v in r.details.items():
                    lines.append(f"         {k}: {v}")
        return "\n".join(lines)


# ---------------------------------------------------------------------------
# Helper: load JSON records
# ---------------------------------------------------------------------------
def _load_records(data_dir: Path, filename: str) -> list[dict[str, Any]]:
    """Load the 'records' array from a golden-file JSON."""
    path = data_dir / filename
    if not path.exists():
        return []
    with open(path, "r", encoding="utf-8") as fh:
        data = json.load(fh)
    return data.get("records", [])


# ---------------------------------------------------------------------------
# Check implementations
# ---------------------------------------------------------------------------

def check_record_counts(data_dir: Path) -> list[CheckResult]:
    """Verify that each data file has a non-zero, plausible record count."""
    results: list[CheckResult] = []
    expected_files = {
        "acctdata.json": ("Accounts", 1),
        "carddata.json": ("Cards", 1),
        "custdata.json": ("Customers", 1),
        "cardxref.json": ("Card cross-refs", 1),
        "dailytran.json": ("Daily transactions", 0),
        "discgrp.json": ("Disclosure groups", 1),
        "tcatbal.json": ("Tran category balances", 1),
        "trancatg.json": ("Transaction categories", 1),
        "trantype.json": ("Transaction types", 1),
    }
    for fname, (label, min_count) in expected_files.items():
        records = _load_records(data_dir, fname)
        count = len(records)
        if count >= min_count:
            results.append(CheckResult(
                f"record_count_{fname}", "PASS",
                f"{label}: {count} records",
            ))
        else:
            results.append(CheckResult(
                f"record_count_{fname}", "FAIL",
                f"{label}: expected >= {min_count}, got {count}",
            ))
    return results


def check_key_uniqueness(data_dir: Path) -> list[CheckResult]:
    """Verify no duplicate primary keys in master files."""
    results: list[CheckResult] = []
    checks = [
        ("acctdata.json", "ACCT-ID", "Account ID"),
        ("carddata.json", "CARD-NUM", "Card number"),
        ("custdata.json", "CUST-ID", "Customer ID"),
        ("cardxref.json", "XREF-CARD-NUM", "Xref card number"),
        ("trantype.json", "TRAN-TYPE", "Transaction type code"),
    ]
    for fname, key_field, label in checks:
        records = _load_records(data_dir, fname)
        keys = [r.get(key_field) for r in records]
        counter = Counter(keys)
        dupes = {k: c for k, c in counter.items() if c > 1}
        if not dupes:
            results.append(CheckResult(
                f"key_unique_{fname}", "PASS",
                f"{label}: all {len(keys)} keys unique",
            ))
        else:
            results.append(CheckResult(
                f"key_unique_{fname}", "FAIL",
                f"{label}: {len(dupes)} duplicate key(s)",
                details={"duplicates": dict(list(dupes.items())[:10])},
            ))
    return results


def check_xref_integrity(data_dir: Path) -> list[CheckResult]:
    """Verify card cross-references point to valid accounts and customers."""
    results: list[CheckResult] = []

    accounts = _load_records(data_dir, "acctdata.json")
    customers = _load_records(data_dir, "custdata.json")
    xrefs = _load_records(data_dir, "cardxref.json")

    acct_ids = {r.get("ACCT-ID") for r in accounts}
    cust_ids = {r.get("CUST-ID") for r in customers}

    orphan_accts: list[Any] = []
    orphan_custs: list[Any] = []
    for xr in xrefs:
        if xr.get("XREF-ACCT-ID") not in acct_ids:
            orphan_accts.append(xr.get("XREF-CARD-NUM"))
        if xr.get("XREF-CUST-ID") not in cust_ids:
            orphan_custs.append(xr.get("XREF-CARD-NUM"))

    if not orphan_accts:
        results.append(CheckResult(
            "xref_acct_integrity", "PASS",
            f"All {len(xrefs)} xref account references valid",
        ))
    else:
        results.append(CheckResult(
            "xref_acct_integrity", "FAIL",
            f"{len(orphan_accts)} xref(s) reference non-existent account",
            details={"orphan_cards": orphan_accts[:10]},
        ))

    if not orphan_custs:
        results.append(CheckResult(
            "xref_cust_integrity", "PASS",
            f"All {len(xrefs)} xref customer references valid",
        ))
    else:
        results.append(CheckResult(
            "xref_cust_integrity", "FAIL",
            f"{len(orphan_custs)} xref(s) reference non-existent customer",
            details={"orphan_cards": orphan_custs[:10]},
        ))

    return results


def check_card_account_ref(data_dir: Path) -> list[CheckResult]:
    """Every card must reference a valid account."""
    results: list[CheckResult] = []

    accounts = _load_records(data_dir, "acctdata.json")
    cards = _load_records(data_dir, "carddata.json")

    acct_ids = {r.get("ACCT-ID") for r in accounts}
    orphans = [c.get("CARD-NUM") for c in cards if c.get("CARD-ACCT-ID") not in acct_ids]

    if not orphans:
        results.append(CheckResult(
            "card_acct_ref", "PASS",
            f"All {len(cards)} cards reference valid accounts",
        ))
    else:
        results.append(CheckResult(
            "card_acct_ref", "FAIL",
            f"{len(orphans)} card(s) reference non-existent account",
            details={"orphan_cards": orphans[:10]},
        ))
    return results


def check_tran_card_ref(data_dir: Path) -> list[CheckResult]:
    """Every daily transaction must reference a valid card number."""
    results: list[CheckResult] = []

    cards = _load_records(data_dir, "carddata.json")
    trans = _load_records(data_dir, "dailytran.json")

    card_nums = {r.get("CARD-NUM") for r in cards}
    orphans = [
        t.get("DALYTRAN-ID")
        for t in trans
        if t.get("DALYTRAN-CARD-NUM") and t.get("DALYTRAN-CARD-NUM").strip()
           and t.get("DALYTRAN-CARD-NUM") not in card_nums
    ]

    if not orphans:
        results.append(CheckResult(
            "tran_card_ref", "PASS",
            f"All {len(trans)} daily transactions reference valid cards",
        ))
    else:
        results.append(CheckResult(
            "tran_card_ref", "FAIL",
            f"{len(orphans)} transaction(s) reference non-existent card",
            details={"orphan_tran_ids": orphans[:10]},
        ))
    return results


def check_tran_category_ref(data_dir: Path) -> list[CheckResult]:
    """Every transaction category balance must reference a valid account."""
    results: list[CheckResult] = []

    accounts = _load_records(data_dir, "acctdata.json")
    tcatbals = _load_records(data_dir, "tcatbal.json")

    acct_ids = {r.get("ACCT-ID") for r in accounts}
    orphans = [
        r.get("TRANCAT-ACCT-ID")
        for r in tcatbals
        if r.get("TRANCAT-ACCT-ID") not in acct_ids
    ]

    if not orphans:
        results.append(CheckResult(
            "tcatbal_acct_ref", "PASS",
            f"All {len(tcatbals)} category balances reference valid accounts",
        ))
    else:
        results.append(CheckResult(
            "tcatbal_acct_ref", "FAIL",
            f"{len(orphans)} category balance(s) reference non-existent account",
            details={"orphan_acct_ids": list(set(orphans))[:10]},
        ))
    return results


def check_disclosure_group_consistency(data_dir: Path) -> list[CheckResult]:
    """Disclosure group entries must reference valid transaction type codes."""
    results: list[CheckResult] = []

    tran_types = _load_records(data_dir, "trantype.json")
    disc_groups = _load_records(data_dir, "discgrp.json")

    valid_type_cds = {r.get("TRAN-TYPE") for r in tran_types}
    invalid = [
        (r.get("DIS-ACCT-GROUP-ID"), r.get("DIS-TRAN-TYPE-CD"))
        for r in disc_groups
        if r.get("DIS-TRAN-TYPE-CD") not in valid_type_cds
    ]

    if not invalid:
        results.append(CheckResult(
            "discgrp_type_ref", "PASS",
            f"All {len(disc_groups)} disclosure groups reference valid type codes",
        ))
    else:
        results.append(CheckResult(
            "discgrp_type_ref", "FAIL",
            f"{len(invalid)} disclosure group(s) have invalid type codes",
            details={"invalid_entries": invalid[:10]},
        ))
    return results


def check_account_balance_signs(data_dir: Path) -> list[CheckResult]:
    """Warn if any account has a negative credit limit (likely data issue)."""
    results: list[CheckResult] = []

    accounts = _load_records(data_dir, "acctdata.json")
    negative_limits = [
        r.get("ACCT-ID")
        for r in accounts
        if (r.get("ACCT-CREDIT-LIMIT") or 0) < 0
    ]

    if not negative_limits:
        results.append(CheckResult(
            "acct_balance_signs", "PASS",
            f"All {len(accounts)} accounts have non-negative credit limits",
        ))
    else:
        results.append(CheckResult(
            "acct_balance_signs", "WARN",
            f"{len(negative_limits)} account(s) have negative credit limits",
            details={"account_ids": negative_limits[:10]},
        ))
    return results


def check_account_card_count_parity(data_dir: Path) -> list[CheckResult]:
    """Each account should have at least one card assigned via cross-ref."""
    results: list[CheckResult] = []

    accounts = _load_records(data_dir, "acctdata.json")
    xrefs = _load_records(data_dir, "cardxref.json")

    xref_accts = {r.get("XREF-ACCT-ID") for r in xrefs}
    acct_ids = {r.get("ACCT-ID") for r in accounts}
    missing = acct_ids - xref_accts

    if not missing:
        results.append(CheckResult(
            "acct_card_parity", "PASS",
            f"All {len(acct_ids)} accounts have at least one card cross-ref",
        ))
    else:
        results.append(CheckResult(
            "acct_card_parity", "WARN",
            f"{len(missing)} account(s) have no card cross-reference",
            details={"account_ids": sorted(missing)[:10]},
        ))
    return results


# ---------------------------------------------------------------------------
# Check registry
# ---------------------------------------------------------------------------
ALL_CHECKS = [
    check_record_counts,
    check_key_uniqueness,
    check_xref_integrity,
    check_card_account_ref,
    check_tran_card_ref,
    check_tran_category_ref,
    check_disclosure_group_consistency,
    check_account_balance_signs,
    check_account_card_count_parity,
]


def run_all_checks(data_dir: str | Path) -> ReconciliationReport:
    """Execute every registered reconciliation check."""
    data_dir = Path(data_dir)
    report = ReconciliationReport(data_dir=str(data_dir))
    for check_fn in ALL_CHECKS:
        report.results.extend(check_fn(data_dir))
    return report


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------
def main() -> None:
    if len(sys.argv) < 2:
        print("Usage: reconciliation_runner.py <data_dir>")
        print("  data_dir should contain the golden-file JSON files.")
        sys.exit(1)

    data_dir = sys.argv[1]
    report = run_all_checks(data_dir)
    print(report.summary())
    sys.exit(0 if report.passed else 1)


if __name__ == "__main__":
    main()
