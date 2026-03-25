#!/usr/bin/env python3
"""
Reconciliation Runner for CardDemo Migration Test Harness.

Validates data consistency between COBOL VSAM files (represented as ASCII flat
files) and Java/PostgreSQL output during the migration dual-run period.

Reconciliation checks:
  - Record count matching
  - Balance/amount summation
  - Referential integrity (cross-entity key validation)
  - Orphan detection
  - Field-level checksum comparison

Usage:
    python3 test-harness/reconciliation_runner.py \
        --vsam-dir app/data/ASCII/ \
        --golden-dir golden-files/ \
        --report-dir reports/recon/

    python3 test-harness/reconciliation_runner.py \
        --check account_balance \
        --vsam-dir app/data/ASCII/ \
        --golden-dir golden-files/
"""

import argparse
import hashlib
import json
import sys
from dataclasses import dataclass, field
from decimal import Decimal, InvalidOperation
from pathlib import Path
from typing import Any

sys.path.insert(0, str(Path(__file__).parent))

from copybook_parser import (
    LAYOUT_REGISTRY,
    parse_file,
)


# ---------------------------------------------------------------------------
# Reconciliation check result types
# ---------------------------------------------------------------------------

@dataclass
class CheckResult:
    """Result of a single reconciliation check."""
    check_name: str
    entity: str
    status: str  # "PASS", "FAIL", "WARN", "SKIP"
    message: str
    expected: Any = None
    actual: Any = None
    details: dict = field(default_factory=dict)


@dataclass
class ReconciliationReport:
    """Complete reconciliation report."""
    checks: list = field(default_factory=list)

    @property
    def passed(self) -> bool:
        return all(c.status in ("PASS", "SKIP") for c in self.checks)

    @property
    def summary(self) -> dict:
        return {
            "total_checks": len(self.checks),
            "passed": sum(1 for c in self.checks if c.status == "PASS"),
            "failed": sum(1 for c in self.checks if c.status == "FAIL"),
            "warnings": sum(1 for c in self.checks if c.status == "WARN"),
            "skipped": sum(1 for c in self.checks if c.status == "SKIP"),
        }


# ---------------------------------------------------------------------------
# Reconciliation checks
# ---------------------------------------------------------------------------

def check_record_count(vsam_records: list[dict], target_records: list[dict],
                       entity: str) -> CheckResult:
    """Verify record counts match between VSAM and target."""
    vsam_count = len(vsam_records)
    target_count = len(target_records)

    if vsam_count == target_count:
        return CheckResult(
            check_name="record_count",
            entity=entity,
            status="PASS",
            message=f"Record counts match: {vsam_count}",
            expected=vsam_count,
            actual=target_count,
        )
    else:
        return CheckResult(
            check_name="record_count",
            entity=entity,
            status="FAIL",
            message=f"Record count mismatch: VSAM={vsam_count}, target={target_count}",
            expected=vsam_count,
            actual=target_count,
            details={"delta": target_count - vsam_count},
        )


def check_balance_sum(records: list[dict], balance_fields: list[str],
                      entity: str, source_label: str) -> dict[str, Decimal]:
    """Sum all balance/amount fields across records.

    Returns a dict of field_name -> total_sum.
    """
    sums: dict[str, Decimal] = {}
    for field_name in balance_fields:
        total = Decimal("0")
        for record in records:
            value = record.get(field_name)
            if value is not None:
                try:
                    total += Decimal(str(value))
                except (InvalidOperation, ValueError):
                    pass
        sums[field_name] = total
    return sums


def check_balance_reconciliation(vsam_records: list[dict], target_records: list[dict],
                                 balance_fields: list[str], entity: str) -> list[CheckResult]:
    """Compare balance/amount sums between VSAM and target."""
    results = []

    vsam_sums = check_balance_sum(vsam_records, balance_fields, entity, "VSAM")
    target_sums = check_balance_sum(target_records, balance_fields, entity, "target")

    for field_name in balance_fields:
        vsam_total = vsam_sums.get(field_name, Decimal("0"))
        target_total = target_sums.get(field_name, Decimal("0"))

        if vsam_total == target_total:
            results.append(CheckResult(
                check_name=f"balance_sum_{field_name}",
                entity=entity,
                status="PASS",
                message=f"{field_name} sum matches: {vsam_total}",
                expected=str(vsam_total),
                actual=str(target_total),
            ))
        else:
            delta = target_total - vsam_total
            results.append(CheckResult(
                check_name=f"balance_sum_{field_name}",
                entity=entity,
                status="FAIL",
                message=f"{field_name} sum mismatch: VSAM={vsam_total}, target={target_total}, delta={delta}",
                expected=str(vsam_total),
                actual=str(target_total),
                details={"delta": str(delta)},
            ))

    return results


def check_referential_integrity(parent_records: list[dict], child_records: list[dict],
                                parent_key: str, child_key: str,
                                parent_entity: str, child_entity: str) -> CheckResult:
    """Verify every child record references a valid parent."""
    parent_keys = set()
    for rec in parent_records:
        val = rec.get(parent_key)
        if val is not None:
            parent_keys.add(str(val))

    orphans = []
    for i, rec in enumerate(child_records):
        val = rec.get(child_key)
        if val is not None and str(val) not in parent_keys:
            orphans.append({"index": i, "key": str(val)})

    if not orphans:
        return CheckResult(
            check_name=f"referential_integrity_{child_entity}_to_{parent_entity}",
            entity=child_entity,
            status="PASS",
            message=f"All {child_entity} records reference valid {parent_entity} keys",
            expected=0,
            actual=0,
        )
    else:
        return CheckResult(
            check_name=f"referential_integrity_{child_entity}_to_{parent_entity}",
            entity=child_entity,
            status="FAIL",
            message=f"{len(orphans)} orphan {child_entity} records with invalid {parent_entity} keys",
            expected=0,
            actual=len(orphans),
            details={"orphans": orphans[:20]},  # Limit detail to first 20
        )


def check_field_checksum(vsam_records: list[dict], target_records: list[dict],
                         key_field: str | list[str], check_fields: list[str],
                         entity: str) -> CheckResult:
    """Compare field-level checksums between VSAM and target for matched records."""
    def record_checksum(record: dict, fields: list[str]) -> str:
        values = []
        for f in sorted(fields):
            val = record.get(f, "")
            values.append(f"{f}={val}")
        return hashlib.md5("|".join(values).encode()).hexdigest()

    vsam_by_key = {}
    for rec in vsam_records:
        key = _make_key(rec, key_field)
        vsam_by_key[key] = record_checksum(rec, check_fields)

    target_by_key = {}
    for rec in target_records:
        key = _make_key(rec, key_field)
        target_by_key[key] = record_checksum(rec, check_fields)

    mismatches = []
    for key in vsam_by_key:
        if key in target_by_key and vsam_by_key[key] != target_by_key[key]:
            mismatches.append(key)

    if not mismatches:
        return CheckResult(
            check_name=f"field_checksum_{entity}",
            entity=entity,
            status="PASS",
            message=f"All {entity} record checksums match",
            expected=0,
            actual=0,
        )
    else:
        return CheckResult(
            check_name=f"field_checksum_{entity}",
            entity=entity,
            status="FAIL",
            message=f"{len(mismatches)} {entity} records have checksum mismatches",
            expected=0,
            actual=len(mismatches),
            details={"mismatched_keys": mismatches[:20]},
        )


def _make_key(record: dict, key_field: str | list[str]) -> str:
    """Build a composite or simple key string from a record."""
    if isinstance(key_field, list):
        return "|".join(str(record.get(f, "")) for f in key_field)
    return str(record.get(key_field, ""))


def check_duplicate_keys(records: list[dict], key_field: str | list[str],
                         entity: str, source: str) -> CheckResult:
    """Check for duplicate primary keys in a dataset."""
    keys_seen: dict[str, int] = {}
    duplicates = []
    label = key_field if isinstance(key_field, str) else "+".join(key_field)

    for rec in records:
        key = _make_key(rec, key_field)
        if key in keys_seen:
            duplicates.append(key)
        keys_seen[key] = keys_seen.get(key, 0) + 1

    if not duplicates:
        return CheckResult(
            check_name=f"duplicate_keys_{entity}_{source}",
            entity=entity,
            status="PASS",
            message=f"No duplicate {label} keys in {source} {entity} data",
            expected=0,
            actual=0,
        )
    else:
        return CheckResult(
            check_name=f"duplicate_keys_{entity}_{source}",
            entity=entity,
            status="FAIL",
            message=f"{len(duplicates)} duplicate {label} keys in {source} {entity} data",
            expected=0,
            actual=len(duplicates),
            details={"duplicate_keys": list(set(duplicates))[:20]},
        )


# ---------------------------------------------------------------------------
# Entity-specific reconciliation suites
# ---------------------------------------------------------------------------

# Define which balance fields to check per entity
ENTITY_BALANCE_FIELDS = {
    "acctdata": [
        "ACCT-CURR-BAL", "ACCT-CREDIT-LIMIT", "ACCT-CASH-CREDIT-LIMIT",
        "ACCT-CURR-CYC-CREDIT", "ACCT-CURR-CYC-DEBIT",
    ],
    "dailytran": ["DALYTRAN-AMT"],
    "tcatbal": ["TRAN-CAT-BAL"],
    "discgrp": ["DIS-INT-RATE"],
}

# Define primary key fields per entity.
# Composite keys are represented as lists; single keys as strings.
ENTITY_KEY_FIELDS = {
    "acctdata": "ACCT-ID",
    "carddata": "CARD-NUM",
    "cardxref": "XREF-CARD-NUM",
    "custdata": "CUST-ID",
    "dailytran": "DALYTRAN-ID",
    "tcatbal": ["TRANCAT-ACCT-ID", "TRANCAT-TYPE-CD", "TRANCAT-CD"],
    "discgrp": ["DIS-ACCT-GROUP-ID", "DIS-TRAN-TYPE-CD", "DIS-TRAN-CAT-CD"],
    "trancatg": ["TRAN-TYPE-CD", "TRAN-CAT-CD"],
    "trantype": "TRAN-TYPE",
}

# Referential integrity relationships: (child_entity, child_key, parent_entity, parent_key)
REFERENTIAL_CHECKS = [
    ("carddata", "CARD-ACCT-ID", "acctdata", "ACCT-ID"),
    ("cardxref", "XREF-ACCT-ID", "acctdata", "ACCT-ID"),
    ("cardxref", "XREF-CARD-NUM", "carddata", "CARD-NUM"),
    ("cardxref", "XREF-CUST-ID", "custdata", "CUST-ID"),
    ("tcatbal", "TRANCAT-ACCT-ID", "acctdata", "ACCT-ID"),
]


def run_entity_checks(vsam_records: list[dict], target_records: list[dict],
                      entity: str) -> list[CheckResult]:
    """Run all applicable checks for a given entity."""
    results = []

    # Record count
    results.append(check_record_count(vsam_records, target_records, entity))

    # Balance sums
    balance_fields = ENTITY_BALANCE_FIELDS.get(entity, [])
    if balance_fields:
        results.extend(
            check_balance_reconciliation(vsam_records, target_records, balance_fields, entity)
        )

    # Duplicate keys
    key_field = ENTITY_KEY_FIELDS.get(entity)
    if key_field:
        results.append(check_duplicate_keys(vsam_records, key_field, entity, "VSAM"))
        results.append(check_duplicate_keys(target_records, key_field, entity, "target"))

    # Field checksums
    if key_field:
        check_fields = [f for f in vsam_records[0].keys() if not f.startswith("_")] if vsam_records else []
        if check_fields:
            results.append(
                check_field_checksum(vsam_records, target_records, key_field, check_fields, entity)
            )

    return results


def run_cross_entity_checks(all_vsam: dict[str, list[dict]]) -> list[CheckResult]:
    """Run referential integrity checks across entities."""
    results = []

    for child_entity, child_key, parent_entity, parent_key in REFERENTIAL_CHECKS:
        child_records = all_vsam.get(child_entity, [])
        parent_records = all_vsam.get(parent_entity, [])

        if not child_records or not parent_records:
            results.append(CheckResult(
                check_name=f"referential_integrity_{child_entity}_to_{parent_entity}",
                entity=child_entity,
                status="SKIP",
                message=f"Skipped: {child_entity} or {parent_entity} data not available",
            ))
            continue

        results.append(
            check_referential_integrity(
                parent_records, child_records,
                parent_key, child_key,
                parent_entity, child_entity,
            )
        )

    return results


# ---------------------------------------------------------------------------
# Main runner
# ---------------------------------------------------------------------------

def load_vsam_data(vsam_dir: Path) -> dict[str, list[dict]]:
    """Load all VSAM ASCII files using copybook layouts."""
    all_data = {}
    for entity, layout in LAYOUT_REGISTRY.items():
        data_file = vsam_dir / f"{entity}.txt"
        if data_file.exists():
            all_data[entity] = parse_file(data_file, layout)
        else:
            print(f"  SKIP  {entity}: {data_file} not found")
    return all_data


def load_golden_data(golden_dir: Path) -> dict[str, list[dict]]:
    """Load all golden JSON files as target comparison data."""
    all_data = {}
    for json_file in sorted(golden_dir.glob("*.json")):
        entity = json_file.stem
        with open(json_file, "r", encoding="utf-8") as f:
            data = json.load(f)
        all_data[entity] = data.get("records", [])
    return all_data


def run_all_checks(vsam_dir: Path, golden_dir: Path) -> ReconciliationReport:
    """Run all reconciliation checks."""
    report = ReconciliationReport()

    print("Loading VSAM data...")
    vsam_data = load_vsam_data(vsam_dir)

    print("Loading golden/target data...")
    target_data = load_golden_data(golden_dir)

    print()
    print("Running entity-level checks...")
    for entity in sorted(set(vsam_data.keys()) | set(target_data.keys())):
        vsam_records = vsam_data.get(entity, [])
        target_records = target_data.get(entity, [])

        if not vsam_records and not target_records:
            continue

        results = run_entity_checks(vsam_records, target_records, entity)
        report.checks.extend(results)

        for r in results:
            status_icon = {"PASS": "OK", "FAIL": "FAIL", "WARN": "WARN", "SKIP": "SKIP"}[r.status]
            print(f"  {status_icon:4}  {entity}/{r.check_name}: {r.message}")

    print()
    print("Running cross-entity referential integrity checks...")
    xref_results = run_cross_entity_checks(vsam_data)
    report.checks.extend(xref_results)
    for r in xref_results:
        status_icon = {"PASS": "OK", "FAIL": "FAIL", "WARN": "WARN", "SKIP": "SKIP"}[r.status]
        print(f"  {status_icon:4}  {r.check_name}: {r.message}")

    return report


def write_report(report: ReconciliationReport, report_dir: Path):
    """Write reconciliation report to JSON file."""
    report_dir.mkdir(parents=True, exist_ok=True)
    report_file = report_dir / "reconciliation_report.json"

    output = {
        "summary": report.summary,
        "checks": [
            {
                "check_name": c.check_name,
                "entity": c.entity,
                "status": c.status,
                "message": c.message,
                "expected": c.expected,
                "actual": c.actual,
                "details": c.details,
            }
            for c in report.checks
        ],
    }

    with open(report_file, "w", encoding="utf-8") as f:
        json.dump(output, f, indent=2, default=str)
        f.write("\n")

    print(f"\nReport written to {report_file}")


def main():
    parser = argparse.ArgumentParser(
        description="Run reconciliation checks for CardDemo migration."
    )
    parser.add_argument(
        "--vsam-dir", type=Path, default=Path("app/data/ASCII"),
        help="Directory containing VSAM ASCII data files",
    )
    parser.add_argument(
        "--golden-dir", type=Path, default=Path("golden-files"),
        help="Directory containing golden JSON files (used as target comparison)",
    )
    parser.add_argument(
        "--report-dir", type=Path, default=Path("reports/recon"),
        help="Directory for reconciliation report output",
    )
    parser.add_argument(
        "--check", type=str, help="Run a specific check only (e.g., account_balance)",
    )
    args = parser.parse_args()

    print("CardDemo Reconciliation Runner")
    print("=" * 50)
    print()

    report = run_all_checks(args.vsam_dir, args.golden_dir)

    print()
    print("=" * 50)
    print(f"Summary: {report.summary}")
    print(f"Overall: {'PASSED' if report.passed else 'FAILED'}")

    write_report(report, args.report_dir)

    return 0 if report.passed else 1


if __name__ == "__main__":
    sys.exit(main())
