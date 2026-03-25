"""
Reconciliation check runner for CardDemo migration.

Runs all reconciliation checks against golden files and optionally
against modern system output. Produces a structured report.

Usage:
    # Baseline mode (golden files only — verify internal consistency):
    python run_reconciliation.py ../golden-files/

    # Comparison mode (golden vs modern):
    python run_reconciliation.py ../golden-files/ --modern-counts '{"RC-001": 50, ...}'
"""

import argparse
import json
import sys

from reconciliation import run_all_checks


def main():
    parser = argparse.ArgumentParser(
        description="Run reconciliation checks for CardDemo migration"
    )
    parser.add_argument(
        "golden_dir",
        help="Path to golden-files/ directory"
    )
    parser.add_argument(
        "--modern-counts",
        type=str,
        default=None,
        help="JSON string with modern system counts (e.g., '{\"RC-001\": 50}')"
    )
    parser.add_argument(
        "--output",
        type=str,
        default=None,
        help="Output file path for JSON report"
    )

    args = parser.parse_args()

    modern_data = None
    if args.modern_counts:
        modern_data = json.loads(args.modern_counts)

    report = run_all_checks(args.golden_dir, modern_data)

    json_output = json.dumps(report, indent=2, default=str)

    if args.output:
        with open(args.output, "w") as f:
            f.write(json_output)
            f.write("\n")
        print(f"Report written to {args.output}")
    else:
        print(json_output)

    summary = report["summary"]
    print(f"\n{'='*50}")
    print(f"Reconciliation Report")
    print(f"{'='*50}")
    for check in report["checks"]:
        status = "PASS" if check["pass"] is True else (
            "FAIL" if check["pass"] is False else "BASELINE"
        )
        print(f"  [{status:8s}] {check['check_id']}: {check['check_name']}")

    print(f"\nTotal: {summary['total_checks']} checks | "
          f"Passed: {summary['passed']} | "
          f"Failed: {summary['failed']} | "
          f"Baseline: {summary['baseline_only']}")

    if not summary["overall_pass"]:
        sys.exit(1)


if __name__ == "__main__":
    main()
