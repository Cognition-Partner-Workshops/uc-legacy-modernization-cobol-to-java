#!/usr/bin/env python3
"""
Main entry point for the CardDemo Migration Test Harness.

Usage:
    python run_harness.py [--data-dir PATH] [--golden-dir PATH] [--report-dir PATH]

This script:
1. Parses ASCII data files into golden JSON references
2. Validates records against copybook-derived contracts
3. Runs reconciliation checks across datasets
4. Generates a summary report
"""

import argparse
import json
import sys
from pathlib import Path

# Add the test-harness directory itself to path for imports
sys.path.insert(0, str(Path(__file__).parent))
# Also add parent for package-style imports
sys.path.insert(0, str(Path(__file__).parent.parent))

from parsers.copybook_parser import generate_golden_file
from parsers.layouts import FILE_LAYOUT_MAP
from contracts.contract_validator import validate_contracts
from reconciliation.reconciliation_runner import run_reconciliation
from reporters.diff_report import generate_summary_report


def main():
    parser = argparse.ArgumentParser(
        description="CardDemo Migration Test Harness",
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument(
        "--data-dir",
        default=str(Path(__file__).parent.parent / "app" / "data" / "ASCII"),
        help="Path to ASCII data files directory",
    )
    parser.add_argument(
        "--golden-dir",
        default=str(Path(__file__).parent.parent / "golden-files"),
        help="Path to golden files output directory",
    )
    parser.add_argument(
        "--report-dir",
        default=str(Path(__file__).parent / "reports"),
        help="Path to report output directory",
    )
    parser.add_argument(
        "--skip-parse",
        action="store_true",
        help="Skip parsing step (use existing golden files)",
    )

    args = parser.parse_args()
    data_dir = Path(args.data_dir)
    golden_dir = Path(args.golden_dir)
    report_dir = Path(args.report_dir)
    report_dir.mkdir(parents=True, exist_ok=True)

    print("=" * 60)
    print("  CardDemo Migration Test Harness v1.0.0")
    print("=" * 60)
    print()

    # Step 1: Parse data files into golden references
    if not args.skip_parse:
        print("[1/3] Parsing ASCII data files into golden JSON references...")
        print(f"      Source: {data_dir}")
        print(f"      Output: {golden_dir}")
        golden_dir.mkdir(parents=True, exist_ok=True)

        for file_key, layout in FILE_LAYOUT_MAP.items():
            data_file = data_dir / f"{file_key}.txt"
            if data_file.exists():
                output_file = golden_dir / f"{file_key}.golden.json"
                generate_golden_file(str(data_file), file_key, str(output_file))
                print(f"      OK: {data_file.name} -> {output_file.name}")
            else:
                print(f"      SKIP: {data_file.name} not found")
        print()
    else:
        print("[1/3] Skipping parse step (using existing golden files)")
        print()

    # Step 2: Contract validation
    print("[2/3] Running contract validation against copybook schemas...")
    contract_results = validate_contracts(str(golden_dir))
    for schema_name, result in contract_results.items():
        status = "PASS" if result.passed else "FAIL"
        print(f"      [{status}] {schema_name}: {result.valid_records}/{result.total_records} valid")
    print()

    # Step 3: Reconciliation checks
    print("[3/3] Running reconciliation checks...")
    recon_results = run_reconciliation(str(golden_dir))
    for job_name, report in recon_results.items():
        status = "PASS" if report.passed else "FAIL"
        print(f"      [{status}] {job_name}: {report.passed_checks}/{report.total_checks} checks passed")
    print()

    # Generate summary report
    print("Generating summary report...")
    summary = generate_summary_report(
        reconciliation_results=recon_results,
        contract_results=contract_results,
        output_path=str(report_dir / "summary_report.json"),
    )
    print()
    print(summary)

    # Write detailed results
    contract_output = {
        name: result.to_dict() for name, result in contract_results.items()
    }
    with (report_dir / "contract_results.json").open("w") as f:
        json.dump(contract_output, f, indent=2)

    recon_output = {
        name: report.to_dict() for name, report in recon_results.items()
    }
    with (report_dir / "reconciliation_results.json").open("w") as f:
        json.dump(recon_output, f, indent=2)

    # Exit code based on results
    all_passed = all(r.passed for r in contract_results.values()) and all(
        r.passed for r in recon_results.values()
    )

    if all_passed:
        print("\nAll checks passed!")
        return 0
    else:
        print("\nSome checks failed. See reports for details.")
        return 1


if __name__ == "__main__":
    sys.exit(main())
