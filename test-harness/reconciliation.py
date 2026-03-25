"""
Reconciliation check utilities for CardDemo batch job validation.

Provides aggregate-level assertions that verify data invariants after
batch jobs complete -- independent of field-level correctness.
"""

import json
import os
from collections import defaultdict

from copybook_parser import parse_file


def count_records(filepath, layout):
    """Count the number of records in a fixed-width data file.

    Args:
        filepath: Path to the ASCII data file.
        layout: Copybook layout for the file.

    Returns:
        Integer record count.
    """
    records = parse_file(filepath, layout)
    return len(records)


def sum_field(filepath, layout, field_name):
    """Sum a numeric field across all records in a data file.

    Args:
        filepath: Path to the ASCII data file.
        layout: Copybook layout for the file.
        field_name: Name of the field to sum.

    Returns:
        The numeric total (float or int).
    """
    records = parse_file(filepath, layout)
    total = 0
    for rec in records:
        val = rec.get(field_name, 0)
        if isinstance(val, (int, float)):
            total += val
    return round(total, 2)


def sum_field_grouped(filepath, layout, field_name, group_fields):
    """Sum a numeric field grouped by one or more key fields.

    Args:
        filepath: Path to the ASCII data file.
        layout: Copybook layout for the file.
        field_name: Name of the field to sum.
        group_fields: List of field names to group by.

    Returns:
        A dict mapping group key tuples to their totals.
    """
    records = parse_file(filepath, layout)
    groups = defaultdict(float)
    for rec in records:
        key = tuple(rec.get(gf, "") for gf in group_fields)
        val = rec.get(field_name, 0)
        if isinstance(val, (int, float)):
            groups[key] += val
    return {k: round(v, 2) for k, v in groups.items()}


def unique_keys(filepath, layout, key_fields):
    """Extract the set of distinct key values from a data file.

    Args:
        filepath: Path to the ASCII data file.
        layout: Copybook layout for the file.
        key_fields: List of field names that form the key.

    Returns:
        A set of tuples representing unique keys.
    """
    records = parse_file(filepath, layout)
    keys = set()
    for rec in records:
        key = tuple(rec.get(kf, "") for kf in key_fields)
        keys.add(key)
    return keys


def cross_reference(file_a, layout_a, key_fields_a, file_b, layout_b, key_fields_b):
    """Check referential integrity between two files.

    Verifies that every key in file_a exists in file_b.

    Args:
        file_a: Path to the referencing file.
        layout_a: Copybook layout for file_a.
        key_fields_a: Key fields in file_a.
        file_b: Path to the referenced file.
        layout_b: Copybook layout for file_b.
        key_fields_b: Key fields in file_b.

    Returns:
        A dict with:
            - 'valid': bool, True if all keys in A exist in B.
            - 'total_keys_a': number of distinct keys in A.
            - 'total_keys_b': number of distinct keys in B.
            - 'orphan_keys': list of keys in A not found in B.
    """
    keys_a = unique_keys(file_a, layout_a, key_fields_a)
    keys_b = unique_keys(file_b, layout_b, key_fields_b)

    orphans = keys_a - keys_b

    return {
        "valid": len(orphans) == 0,
        "total_keys_a": len(keys_a),
        "total_keys_b": len(keys_b),
        "orphan_keys": sorted([list(k) for k in orphans]),
    }


class ReconciliationCheck:
    """A single named reconciliation assertion."""

    def __init__(self, check_id, description, check_fn):
        """
        Args:
            check_id: e.g. "RECON-01"
            description: Human-readable description.
            check_fn: Callable that returns (passed: bool, details: dict).
        """
        self.check_id = check_id
        self.description = description
        self.check_fn = check_fn

    def run(self):
        try:
            passed, details = self.check_fn()
        except Exception as e:
            passed = False
            details = {"error": str(e)}
        return {
            "check_id": self.check_id,
            "description": self.description,
            "passed": passed,
            "details": details,
        }


class ReconciliationSuite:
    """A collection of reconciliation checks to run as a suite."""

    def __init__(self, name="CardDemo Reconciliation"):
        self.name = name
        self.checks = []

    def add(self, check):
        """Add a ReconciliationCheck to the suite."""
        self.checks.append(check)

    def run_all(self):
        """Execute all checks and return a summary report.

        Returns:
            A dict with suite name, overall pass/fail, and per-check results.
        """
        results = []
        all_passed = True
        for check in self.checks:
            result = check.run()
            results.append(result)
            if not result["passed"]:
                all_passed = False

        return {
            "suite": self.name,
            "all_passed": all_passed,
            "total_checks": len(self.checks),
            "passed_count": sum(1 for r in results if r["passed"]),
            "failed_count": sum(1 for r in results if not r["passed"]),
            "results": results,
        }

    def run_and_print(self):
        """Run all checks and print results to stdout."""
        report = self.run_all()
        print(f"\n{'=' * 70}")
        print(f"  Reconciliation Suite: {report['suite']}")
        print(f"{'=' * 70}")
        for r in report["results"]:
            status = "PASS" if r["passed"] else "FAIL"
            print(f"  [{status}] {r['check_id']}: {r['description']}")
            if not r["passed"]:
                for k, v in r["details"].items():
                    print(f"         {k}: {v}")
        print(f"{'=' * 70}")
        print(
            f"  Total: {report['total_checks']}  "
            f"Passed: {report['passed_count']}  "
            f"Failed: {report['failed_count']}"
        )
        print(f"{'=' * 70}\n")
        return report
