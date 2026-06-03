#!/usr/bin/env python3
"""
Migration Test Runner — Orchestrates golden-file generation and reconciliation checks.

Usage:
    python run_tests.py [--generate] [--reconcile] [--all]

Flags:
    --generate   Parse ASCII data files and produce golden JSON references
    --reconcile  Run reconciliation checks against golden files
    --all        Run all steps (default if no flag given)
"""

import argparse
import json
import sys
from pathlib import Path

# Add test-harness to path for imports
sys.path.insert(0, str(Path(__file__).resolve().parent))

from record_parser import generate_golden_files
from reconciliation import run_all_reconciliation


def main():
    parser = argparse.ArgumentParser(description="CardDemo Migration Test Harness")
    parser.add_argument("--generate", action="store_true", help="Generate golden files")
    parser.add_argument("--reconcile", action="store_true", help="Run reconciliation checks")
    parser.add_argument("--all", action="store_true", help="Run all steps")
    args = parser.parse_args()

    # Default to --all if nothing specified
    if not (args.generate or args.reconcile or args.all):
        args.all = True

    repo_root = Path(__file__).resolve().parent.parent
    data_dir = repo_root / "app" / "data" / "ASCII"
    copybook_dir = repo_root / "app" / "cpy"
    golden_dir = repo_root / "golden-files"

    exit_code = 0

    # Step 1: Generate golden files
    if args.generate or args.all:
        print("=" * 60)
        print("STEP 1: Generating golden reference files")
        print("=" * 60)
        summary = generate_golden_files(data_dir, copybook_dir, golden_dir)
        print(f"\nGenerated {len(summary)} golden files, {sum(summary.values())} total records.\n")

    # Step 2: Reconciliation checks
    if args.reconcile or args.all:
        print("=" * 60)
        print("STEP 2: Running reconciliation checks")
        print("=" * 60)
        print()

        if not golden_dir.exists():
            print("ERROR: golden-files/ not found. Run with --generate first.")
            return 1

        reports = run_all_reconciliation(data_dir, golden_dir)

        all_passed = True
        for report in reports:
            status = "PASS" if report.passed else "FAIL"
            print(f"[{status}] {report.summary}")
            for check in report.checks:
                cs = "  PASS" if check.passed else "  FAIL"
                print(f"  {cs}: {check.name}")
                if not check.passed and check.details:
                    print(f"        {check.details}")
            print()
            if not report.passed:
                all_passed = False

        # Write report JSON
        report_path = golden_dir / "reconciliation_report.json"
        with report_path.open("w") as f:
            json.dump([r.to_dict() for r in reports], f, indent=2)
        print(f"Report written to: {report_path}")

        if not all_passed:
            print("\nSome checks FAILED.")
            exit_code = 1
        else:
            print("\nAll reconciliation checks PASSED.")

    return exit_code


if __name__ == "__main__":
    sys.exit(main())
