"""
Golden-file test runner for CardDemo migration.

Compares golden JSON files against modern system output to verify
data migration and API response accuracy.

Usage:
    python run_golden_tests.py <golden_dir> <modern_dir>
    python run_golden_tests.py golden-files/ modern-output/
"""

import json
import sys
from pathlib import Path

from comparator import compare_datasets


# Key fields for matching records across golden and modern datasets
KEY_FIELDS = {
    "acctdata":  "ACCT_ID",
    "carddata":  "CARD_NUM",
    "cardxref":  "XREF_CARD_NUM",
    "custdata":  "CUST_ID",
    "dailytran": "DALYTRAN_ID",
    "discgrp":   "DIS_ACCT_GROUP_ID",
    "tcatbal":   "TRANCAT_ACCT_ID",
    "trancatg":  "TRAN_TYPE_CD",
    "trantype":  "TRAN_TYPE",
}


def run_golden_tests(golden_dir: str, modern_dir: str) -> dict:
    """Run golden-file comparison for all available datasets.

    Args:
        golden_dir: Path to golden-files/ directory
        modern_dir: Path to directory with modern system JSON output

    Returns:
        Aggregated test report
    """
    golden_path = Path(golden_dir)
    modern_path = Path(modern_dir)

    results = []
    pass_count = 0
    fail_count = 0

    for golden_file in sorted(golden_path.glob("*.json")):
        file_key = golden_file.stem
        modern_file = modern_path / golden_file.name

        if not modern_file.exists():
            results.append({
                "dataset": file_key,
                "status": "SKIPPED",
                "reason": f"No modern output file: {modern_file}",
            })
            continue

        with open(golden_file) as f:
            golden_data = json.load(f)
        with open(modern_file) as f:
            modern_data = json.load(f)

        key_field = KEY_FIELDS.get(file_key)
        if not key_field:
            results.append({
                "dataset": file_key,
                "status": "SKIPPED",
                "reason": f"No key field defined for {file_key}",
            })
            continue

        modern_records = modern_data.get("records", modern_data)
        if isinstance(modern_records, dict):
            modern_records = [modern_records]

        report = compare_datasets(golden_data, modern_records, key_field)

        status = "PASS" if report["summary"]["pass"] else "FAIL"
        if status == "PASS":
            pass_count += 1
        else:
            fail_count += 1

        results.append({
            "dataset": file_key,
            "status": status,
            "summary": report["summary"],
            "sample_diffs": report["record_diffs"][:5],  # First 5 diffs
        })

    return {
        "overall": {
            "datasets_tested": pass_count + fail_count,
            "passed": pass_count,
            "failed": fail_count,
            "skipped": len(results) - pass_count - fail_count,
            "all_pass": fail_count == 0 and pass_count > 0,
        },
        "results": results,
    }


def main():
    if len(sys.argv) < 3:
        print("Usage: python run_golden_tests.py <golden_dir> <modern_dir>")
        print("\nThis tool compares golden JSON files against modern system output.")
        print("Both directories should contain identically-named JSON files.")
        sys.exit(1)

    golden_dir = sys.argv[1]
    modern_dir = sys.argv[2]

    report = run_golden_tests(golden_dir, modern_dir)

    print(json.dumps(report, indent=2, default=str))

    overall = report["overall"]
    print(f"\n{'='*50}")
    print(f"Golden-File Test Results")
    print(f"{'='*50}")
    print(f"Tested:  {overall['datasets_tested']}")
    print(f"Passed:  {overall['passed']}")
    print(f"Failed:  {overall['failed']}")
    print(f"Skipped: {overall['skipped']}")
    print(f"Overall: {'PASS' if overall['all_pass'] else 'FAIL'}")

    if not overall["all_pass"]:
        sys.exit(1)


if __name__ == "__main__":
    main()
