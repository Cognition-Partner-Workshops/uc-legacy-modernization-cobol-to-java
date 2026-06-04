#!/usr/bin/env python3
"""Run the full migration test harness and print a summary report.

Usage::

    python -m test_harness.run_harness [--golden-dir golden-files]
                                       [--data-dir app/data/ASCII]
                                       [--copybook-dir app/cpy]

Exit code 0 = all checks passed, 1 = one or more failures.
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path

_HERE = Path(__file__).resolve().parent
if str(_HERE.parent) not in sys.path:
    sys.path.insert(0, str(_HERE.parent))

from test_harness.copybook_parser import COPYBOOK_REGISTRY, parse_copybook_file
from test_harness.record_parser import parse_file
from test_harness.reconciliation import run_all_checks
from test_harness.contract_validator import validate_all_contracts


def _header(title: str) -> str:
    return f"\n{'=' * 60}\n  {title}\n{'=' * 60}"


def main() -> None:
    parser = argparse.ArgumentParser(description="Run migration test harness")
    parser.add_argument("--golden-dir", default="golden-files")
    parser.add_argument("--data-dir", default="app/data/ASCII")
    parser.add_argument("--copybook-dir", default="app/cpy")
    args = parser.parse_args()

    repo_root = _HERE.parent
    golden_dir = (repo_root / args.golden_dir).resolve()
    data_dir = (repo_root / args.data_dir).resolve()
    copybook_dir = (repo_root / args.copybook_dir).resolve()

    all_passed = True

    # ---- Golden-file existence check ----
    print(_header("Golden-File Verification"))
    for filename, meta in COPYBOOK_REGISTRY.items():
        ds = filename.replace(".txt", "")
        gf = golden_dir / f"{ds}.json"
        if gf.exists():
            records = json.loads(gf.read_text())
            print(f"  [OK]   {ds}.json — {len(records)} records")
        else:
            print(f"  [MISS] {ds}.json — not found")
            all_passed = False

    # ---- Reconciliation checks ----
    print(_header("Reconciliation Checks"))
    recon_results = run_all_checks(golden_dir)
    for r in recon_results:
        status = "PASS" if r.passed else "FAIL"
        print(f"  [{status}] {r.check_name}")
        if not r.passed:
            all_passed = False
            if r.message:
                print(f"         {r.message}")
            if r.actual is not None:
                print(f"         actual: {r.actual}")

    # ---- Contract validation ----
    print(_header("Contract Validation"))

    # Parse all datasets to feed into contract validator
    parsed: dict[str, list] = {}
    for filename, meta in COPYBOOK_REGISTRY.items():
        ds = filename.replace(".txt", "")
        data_path = data_dir / filename
        cpy_path = copybook_dir / meta["copybook"]
        if data_path.exists() and cpy_path.exists():
            fields = parse_copybook_file(cpy_path)
            parsed[ds] = parse_file(data_path, fields, meta["recln"])

    contract_results = validate_all_contracts(data_dir, copybook_dir, parsed)
    for cr in contract_results:
        status = "PASS" if cr.passed else "FAIL"
        errors = [v for v in cr.violations if v.severity == "ERROR"]
        warnings = [v for v in cr.violations if v.severity == "WARNING"]
        suffix = ""
        if warnings:
            suffix = f" ({len(warnings)} warning(s))"
        print(f"  [{status}] {cr.dataset}{suffix}")
        if errors:
            all_passed = False
            for v in errors[:3]:
                print(f"         {v.message}")

    # ---- Summary ----
    print(_header("Summary"))
    if all_passed:
        print("  All checks PASSED.")
    else:
        print("  Some checks FAILED — see details above.")

    sys.exit(0 if all_passed else 1)


if __name__ == "__main__":
    main()
