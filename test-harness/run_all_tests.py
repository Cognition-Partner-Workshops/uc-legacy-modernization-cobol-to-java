"""
Test orchestrator for CardDemo migration test harness.

Runs all four test dimensions in order:
  1. Golden-file generation and self-validation
  2. Contract validation
  3. Reconciliation checks
  4. Differential test scaffolding (reports readiness)

Usage:
    python test-harness/run_all_tests.py [--golden-only] [--recon-only] [--contract-only]
"""

import json
import os
import sys
import time

# Allow imports from the test-harness directory
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from golden_file_generator import generate_golden_files
from reconciliation import run_reconciliation_checks
from contract_validator import validate_all_contracts


def print_banner(title: str):
    print()
    print("=" * 60)
    print(f"  {title}")
    print("=" * 60)
    print()


def run_golden_file_tests(data_dir: str, golden_dir: str) -> bool:
    """Generate golden files and verify they were created correctly."""
    print_banner("Phase 1: Golden-File Generation")

    summary = generate_golden_files(data_dir, golden_dir)

    if summary["errors"]:
        print(f"\nFAILED: {len(summary['errors'])} error(s) during generation")
        for err in summary["errors"]:
            print(f"  - {err}")
        return False

    # Self-validation: re-read each golden file and verify structure
    print("\nSelf-validation: verifying golden file structure...")
    for filename in sorted(os.listdir(golden_dir)):
        if not filename.endswith(".json"):
            continue
        path = os.path.join(golden_dir, filename)
        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)

        if "_metadata" not in data:
            print(f"  FAIL: {filename} missing _metadata")
            return False

        record_keys = [k for k in data if k != "_metadata"]
        if not record_keys:
            print(f"  FAIL: {filename} has no record array")
            return False

        count = len(data[record_keys[0]])
        expected = data["_metadata"]["record_count"]
        if count != expected:
            print(f"  FAIL: {filename} record count mismatch "
                  f"(array={count}, metadata={expected})")
            return False

        print(f"  OK: {filename} ({count} records)")

    print(f"\nPASSED: {summary['files_generated']} golden files, "
          f"{summary['total_records']} total records")
    return True


def run_contract_tests(golden_dir: str) -> bool:
    """Run contract validation against golden files."""
    print_banner("Phase 2: Contract Validation")

    summary = validate_all_contracts(golden_dir)

    for filename, result in sorted(summary["file_results"].items()):
        status = result["status"]
        if status == "SKIP":
            print(f"  [SKIP] {filename}: {result['reason']}")
        elif status == "PASS":
            print(f"  [PASS] {filename}")
        else:
            v_count = result["violation_count"]
            print(f"  [FAIL] {filename}: {v_count} violations")
            for v in result["violations"][:5]:
                print(f"         {v}")
            if v_count > 5:
                print(f"         ... and {v_count - 5} more")

    total_v = summary["total_violations"]
    if total_v == 0:
        print(f"\nPASSED: {summary['files_checked']} files, 0 violations")
        return True
    else:
        print(f"\nFAILED: {total_v} violation(s) across "
              f"{summary['files_checked']} files")
        return False


def run_reconciliation_tests(golden_dir: str) -> bool:
    """Run reconciliation checks against golden files."""
    print_banner("Phase 3: Reconciliation Checks")

    results = run_reconciliation_checks(golden_dir)

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

    if failed == 0:
        print(f"\nPASSED: {passed} checks, 0 failures")
        return True
    else:
        print(f"\nFAILED: {failed} failure(s) out of {passed + failed} checks")
        return False


def run_differential_readiness_check(data_dir: str) -> bool:
    """Report readiness for differential testing (Phase 4).

    Differential testing requires both the legacy and Java systems to be
    running. This phase just verifies that the input data files exist and
    reports which batch job pairs are ready for comparison.
    """
    print_banner("Phase 4: Differential Test Readiness")

    batch_pairs = [
        ("POSTTRAN", "TransactionPostingJob",
         ["dailytran.txt", "acctdata.txt"]),
        ("INTCALC", "InterestCalculationJob",
         ["acctdata.txt", "discgrp.txt"]),
        ("CREASTMT", "StatementGenerationJob",
         ["acctdata.txt", "custdata.txt"]),
        ("COMBTRAN", "CombineTransactionsJob",
         ["dailytran.txt"]),
    ]

    all_ready = True
    for legacy_job, java_job, required_files in batch_pairs:
        missing = [f for f in required_files
                   if not os.path.isfile(os.path.join(data_dir, f))]
        if missing:
            print(f"  [NOT READY] {legacy_job} -> {java_job}")
            print(f"              Missing inputs: {', '.join(missing)}")
            all_ready = False
        else:
            print(f"  [READY]     {legacy_job} -> {java_job}")
            print(f"              Inputs: {', '.join(required_files)}")

    print()
    if all_ready:
        print("All differential test pairs have required input files.")
        print("To execute: run both legacy and Java systems with these inputs,")
        print("then use comparator.py to diff the outputs.")
    else:
        print("Some differential test pairs are missing input files.")

    # This phase is informational only; it doesn't fail the suite
    return True


def main():
    repo_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    data_dir = os.path.join(repo_root, "app", "data", "ASCII")
    golden_dir = os.path.join(repo_root, "golden-files")

    # Parse flags
    golden_only = "--golden-only" in sys.argv
    recon_only = "--recon-only" in sys.argv
    contract_only = "--contract-only" in sys.argv
    run_all = not (golden_only or recon_only or contract_only)

    print("CardDemo Migration Test Harness")
    print("=" * 60)
    print(f"Repository root: {repo_root}")
    print(f"Data directory:  {data_dir}")
    print(f"Golden files:    {golden_dir}")

    start_time = time.time()
    results = {}

    if run_all or golden_only:
        results["golden"] = run_golden_file_tests(data_dir, golden_dir)

    if run_all or contract_only:
        results["contract"] = run_contract_tests(golden_dir)

    if run_all or recon_only:
        results["reconciliation"] = run_reconciliation_tests(golden_dir)

    if run_all:
        results["differential"] = run_differential_readiness_check(data_dir)

    elapsed = time.time() - start_time

    # Final summary
    print()
    print("=" * 60)
    print("  FINAL SUMMARY")
    print("=" * 60)
    print()

    all_passed = True
    for phase, passed in results.items():
        status = "PASS" if passed else "FAIL"
        print(f"  {phase:25s} [{status}]")
        if not passed:
            all_passed = False

    print()
    print(f"  Elapsed time: {elapsed:.2f}s")
    print()

    if all_passed:
        print("  ALL PHASES PASSED")
    else:
        print("  SOME PHASES FAILED -- see details above")

    sys.exit(0 if all_passed else 1)


if __name__ == "__main__":
    main()
