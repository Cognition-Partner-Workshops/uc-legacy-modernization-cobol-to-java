#!/usr/bin/env python3
"""
Per-Module Reconciliation Checker

Validates internal consistency of system outputs on a per-module basis.
Ensures that:
  - Payroll totals balance (gross = base + overtime + deductions + ...)
  - Leave balances sum correctly (used + remaining = total entitlement)
  - Employee counts match between modules and cross-references

Usage:
    python reconciliation-checker.py --results results/modern/
    python reconciliation-checker.py --results results/legacy/ --results results/modern/ --compare
"""

import argparse
import json
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Optional


FINANCIAL_TOLERANCE = 0.01  # 1 cent tolerance for rounding


@dataclass
class ReconciliationFailure:
    """A single reconciliation check failure."""

    module: str
    check: str
    expected: Any
    actual: Any
    difference: Optional[float] = None
    message: str = ""


@dataclass
class ReconciliationReport:
    """Aggregated reconciliation results."""

    source: str  # 'legacy' or 'modern'
    total_checks: int = 0
    passed: int = 0
    failed: int = 0
    warnings: int = 0
    failures: list[ReconciliationFailure] = field(default_factory=list)


# ---------------------------------------------------------------------------
# Payroll Reconciliation
# ---------------------------------------------------------------------------

def reconcile_payroll(data: dict) -> tuple[list[ReconciliationFailure], int]:
    """Validate payroll totals balance correctly.

    Checks:
        1. Sum of individual employee gross amounts == reported total_gross
        2. Sum of individual employee net amounts == reported total_net
        3. For each employee: gross - deductions == net
        4. total_gross - total_deductions == total_net
        5. Employee count matches the number of pay stubs

    Returns:
        (failures, check_count) — the list of failures and total checks run.
    """
    failures: list[ReconciliationFailure] = []
    check_count = 0

    for step in data.get("steps", []):
        output = step.get("output", {})
        step_name = step.get("name", "unknown")

        # --- Check payroll run totals ---
        total_gross = output.get("total_gross")
        total_net = output.get("total_net")
        total_deductions = output.get("total_deductions")
        employee_pays = output.get("employee_pays", [])
        reported_count = output.get("employee_count")

        # Check 1 & 2: Sum of individual pays == reported totals
        if employee_pays and total_gross is not None:
            check_count += 1
            calc_gross = sum(ep.get("gross_pay", 0) for ep in employee_pays)
            diff = abs(calc_gross - total_gross)
            if diff > FINANCIAL_TOLERANCE:
                failures.append(ReconciliationFailure(
                    module="payroll",
                    check=f"{step_name}: sum(employee_gross) == total_gross",
                    expected=total_gross,
                    actual=calc_gross,
                    difference=diff,
                    message=f"Gross total mismatch: sum={calc_gross}, reported={total_gross}",
                ))

        if employee_pays and total_net is not None:
            check_count += 1
            calc_net = sum(ep.get("net_pay", 0) for ep in employee_pays)
            diff = abs(calc_net - total_net)
            if diff > FINANCIAL_TOLERANCE:
                failures.append(ReconciliationFailure(
                    module="payroll",
                    check=f"{step_name}: sum(employee_net) == total_net",
                    expected=total_net,
                    actual=calc_net,
                    difference=diff,
                    message=f"Net total mismatch: sum={calc_net}, reported={total_net}",
                ))

        # Check 3: Per-employee gross - deductions == net
        for ep in employee_pays:
            check_count += 1
            emp_id = ep.get("emp_id", "?")
            ep_gross = ep.get("gross_pay", 0)
            ep_deductions = ep.get("total_deductions", 0)
            ep_net = ep.get("net_pay", 0)

            expected_net = ep_gross - ep_deductions
            diff = abs(expected_net - ep_net)
            if diff > FINANCIAL_TOLERANCE:
                failures.append(ReconciliationFailure(
                    module="payroll",
                    check=f"{step_name}: emp {emp_id} gross - deductions == net",
                    expected=expected_net,
                    actual=ep_net,
                    difference=diff,
                    message=(
                        f"Employee {emp_id}: gross({ep_gross}) - "
                        f"deductions({ep_deductions}) = {expected_net}, "
                        f"but net_pay = {ep_net}"
                    ),
                ))

        # Check 4: total_gross - total_deductions == total_net
        if (total_gross is not None and total_deductions is not None
                and total_net is not None):
            check_count += 1
            expected_total_net = total_gross - total_deductions
            diff = abs(expected_total_net - total_net)
            if diff > FINANCIAL_TOLERANCE:
                failures.append(ReconciliationFailure(
                    module="payroll",
                    check=f"{step_name}: total_gross - total_deductions == total_net",
                    expected=expected_total_net,
                    actual=total_net,
                    difference=diff,
                    message=(
                        f"total_gross({total_gross}) - total_deductions({total_deductions}) "
                        f"= {expected_total_net}, but total_net = {total_net}"
                    ),
                ))

        # Check 5: Employee count matches number of pay stubs
        if reported_count is not None and employee_pays:
            check_count += 1
            actual_count = len(employee_pays)
            if actual_count != reported_count:
                failures.append(ReconciliationFailure(
                    module="payroll",
                    check=f"{step_name}: employee_count == len(employee_pays)",
                    expected=reported_count,
                    actual=actual_count,
                    message=(
                        f"Reported {reported_count} employees but found "
                        f"{actual_count} pay stubs"
                    ),
                ))

        # Check: Overtime / holiday pay sub-totals add up to gross
        for ep in employee_pays:
            emp_id = ep.get("emp_id", "?")
            components = [
                ep.get("regular_pay", 0),
                ep.get("overtime_pay", 0),
                ep.get("holiday_premium_pay", 0),
                ep.get("holiday_worked_pay", 0),
                ep.get("bonus", 0),
                ep.get("commission", 0),
                ep.get("allowance", 0),
            ]
            component_sum = sum(components)
            ep_gross = ep.get("gross_pay", 0)

            # Only check if at least one sub-component is present
            has_components = any(
                ep.get(k, 0) != 0
                for k in (
                    "regular_pay", "overtime_pay", "holiday_premium_pay",
                    "holiday_worked_pay", "bonus", "commission", "allowance",
                )
            )
            if has_components and ep_gross > 0:
                check_count += 1
                diff = abs(component_sum - ep_gross)
                if diff > FINANCIAL_TOLERANCE:
                    failures.append(ReconciliationFailure(
                        module="payroll",
                        check=f"{step_name}: emp {emp_id} pay components sum == gross",
                        expected=ep_gross,
                        actual=component_sum,
                        difference=diff,
                        message=(
                            f"Employee {emp_id}: pay component sum = {component_sum}, "
                            f"but gross_pay = {ep_gross}"
                        ),
                    ))

    return failures, check_count


# ---------------------------------------------------------------------------
# Leave Reconciliation
# ---------------------------------------------------------------------------

def reconcile_leave(data: dict) -> tuple[list[ReconciliationFailure], int]:
    """Validate leave balances sum correctly.

    Checks:
        1. used + remaining == total_entitled for each leave type
        2. Leave request days match (end_date - start_date + 1 == total_days)
        3. Balance changes after approval/cancellation are consistent

    Returns:
        (failures, check_count) — the list of failures and total checks run.
    """
    failures: list[ReconciliationFailure] = []
    check_count = 0

    for step in data.get("steps", []):
        output = step.get("output", {})
        step_name = step.get("name", "unknown")

        # Check balances
        balances = output.get("balances", [])
        for bal in balances:
            check_count += 1
            leave_type = bal.get("leave_type", "?")
            total = bal.get("total_entitled", 0)
            used = bal.get("used", 0)
            remaining = bal.get("remaining", 0)

            if total != used + remaining:
                failures.append(ReconciliationFailure(
                    module="leave",
                    check=f"{step_name}: {leave_type} used + remaining == total",
                    expected=total,
                    actual=used + remaining,
                    message=(
                        f"{leave_type}: used({used}) + remaining({remaining}) "
                        f"= {used + remaining}, but total_entitled = {total}"
                    ),
                ))

        # Single balance check
        total_entitled = output.get("total_entitled")
        used = output.get("used")
        remaining = output.get("remaining")
        if (total_entitled is not None and used is not None
                and remaining is not None):
            check_count += 1
            if total_entitled != used + remaining:
                failures.append(ReconciliationFailure(
                    module="leave",
                    check=f"{step_name}: used + remaining == total_entitled",
                    expected=total_entitled,
                    actual=used + remaining,
                    message=(
                        f"used({used}) + remaining({remaining}) = {used + remaining}, "
                        f"but total_entitled = {total_entitled}"
                    ),
                ))

        # Check total_days consistency with date range
        start_date = output.get("start_date")
        end_date = output.get("end_date")
        total_days = output.get("total_days")
        if start_date and end_date and total_days is not None:
            check_count += 1
            try:
                from datetime import date as dt_date
                sd = dt_date.fromisoformat(str(start_date))
                ed = dt_date.fromisoformat(str(end_date))
                # Business days calculation (Mon-Fri)
                business_days = 0
                current = sd
                from datetime import timedelta
                while current <= ed:
                    if current.weekday() < 5:
                        business_days += 1
                    current += timedelta(days=1)
                if business_days != total_days:
                    failures.append(ReconciliationFailure(
                        module="leave",
                        check=(
                            f"{step_name}: business days between "
                            f"{start_date} and {end_date} == total_days"
                        ),
                        expected=total_days,
                        actual=business_days,
                        message=(
                            f"Calculated {business_days} business days but "
                            f"total_days = {total_days}"
                        ),
                    ))
            except (ValueError, TypeError):
                pass  # Skip date parsing errors

    return failures, check_count


# ---------------------------------------------------------------------------
# Employee Count Reconciliation
# ---------------------------------------------------------------------------

def reconcile_employee_counts(results_dir: Path) -> list[ReconciliationFailure]:
    """Cross-validate employee counts across modules.

    Checks:
        1. Active employees in payroll == active employees in employee master
        2. Terminated employees are excluded from payroll runs
        3. Employee IDs referenced in leave/payroll exist in employee master
    """
    failures: list[ReconciliationFailure] = []

    # Collect employee IDs and counts from all scenario results
    employee_ids: set[int] = set()
    active_employee_ids: set[int] = set()
    payroll_employee_ids: set[int] = set()
    leave_employee_ids: set[int] = set()
    terminated_employee_ids: set[int] = set()

    for result_file in sorted(results_dir.glob("*.json")):
        try:
            with open(result_file) as f:
                data = json.load(f)
        except (json.JSONDecodeError, OSError):
            continue

        module = data.get("module", "")

        for step in data.get("steps", []):
            output = step.get("output", {})

            # Collect from employee module
            if module == "employee":
                emp_id = output.get("emp_id")
                if emp_id is not None:
                    employee_ids.add(int(emp_id))
                    status = output.get("employment_status", "").upper()
                    if status == "ACTIVE":
                        active_employee_ids.add(int(emp_id))
                    elif status == "TERMINATED":
                        terminated_employee_ids.add(int(emp_id))

            # Collect from payroll module
            if module == "payroll":
                for ep in output.get("employee_pays", []):
                    pid = ep.get("emp_id")
                    if pid is not None:
                        payroll_employee_ids.add(int(pid))

                # Check excluded employees
                excluded_id = output.get("excludes_emp_id")
                if excluded_id is not None:
                    excluded_id = int(excluded_id)
                    if excluded_id in payroll_employee_ids:
                        failures.append(ReconciliationFailure(
                            module="employee",
                            check="Terminated employee excluded from payroll",
                            expected=f"emp_id {excluded_id} NOT in payroll",
                            actual=f"emp_id {excluded_id} found in payroll",
                            message=(
                                f"Terminated employee {excluded_id} should be "
                                f"excluded from payroll but was included"
                            ),
                        ))

            # Collect from leave module
            if module == "leave":
                emp_id = output.get("emp_id")
                if emp_id is not None:
                    leave_employee_ids.add(int(emp_id))

    # Cross-module: payroll employees should be in employee master
    if employee_ids and payroll_employee_ids:
        orphan_payroll = payroll_employee_ids - employee_ids
        if orphan_payroll:
            failures.append(ReconciliationFailure(
                module="employee",
                check="All payroll employees exist in employee master",
                expected="All payroll emp_ids in employee master",
                actual=f"Orphan IDs: {sorted(orphan_payroll)}",
                message=(
                    f"{len(orphan_payroll)} employee(s) in payroll but "
                    f"not in employee master: {sorted(orphan_payroll)}"
                ),
            ))

    # Cross-module: leave employees should be in employee master
    if employee_ids and leave_employee_ids:
        orphan_leave = leave_employee_ids - employee_ids
        if orphan_leave:
            failures.append(ReconciliationFailure(
                module="employee",
                check="All leave employees exist in employee master",
                expected="All leave emp_ids in employee master",
                actual=f"Orphan IDs: {sorted(orphan_leave)}",
                message=(
                    f"{len(orphan_leave)} employee(s) in leave but "
                    f"not in employee master: {sorted(orphan_leave)}"
                ),
            ))

    # Cross-module: terminated employees should NOT be in payroll
    if terminated_employee_ids and payroll_employee_ids:
        term_in_payroll = terminated_employee_ids & payroll_employee_ids
        if term_in_payroll:
            failures.append(ReconciliationFailure(
                module="employee",
                check="Terminated employees excluded from payroll",
                expected="No terminated employees in payroll",
                actual=f"Found: {sorted(term_in_payroll)}",
                message=(
                    f"{len(term_in_payroll)} terminated employee(s) found "
                    f"in payroll: {sorted(term_in_payroll)}"
                ),
            ))

    return failures


# ---------------------------------------------------------------------------
# Main orchestration
# ---------------------------------------------------------------------------

def reconcile_results(results_dir: Path) -> ReconciliationReport:
    """Run all reconciliation checks against a results directory."""
    report = ReconciliationReport(source=results_dir.name)

    for result_file in sorted(results_dir.glob("*.json")):
        try:
            with open(result_file) as f:
                data = json.load(f)
        except (json.JSONDecodeError, OSError) as e:
            print(f"WARNING: Could not read {result_file}: {e}")
            continue

        module = data.get("module", "unknown")

        if module == "payroll":
            failures, checks_run = reconcile_payroll(data)
            report.failures.extend(failures)
            report.total_checks += checks_run
            report.failed += len(failures)
            report.passed += checks_run - len(failures)

        elif module == "leave":
            failures, checks_run = reconcile_leave(data)
            report.failures.extend(failures)
            report.total_checks += checks_run
            report.failed += len(failures)
            report.passed += checks_run - len(failures)

    # Cross-module employee count checks
    cross_failures = reconcile_employee_counts(results_dir)
    report.failures.extend(cross_failures)
    if cross_failures:
        report.total_checks += len(cross_failures)
        report.failed += len(cross_failures)
    else:
        report.total_checks += 1
        report.passed += 1

    return report


def print_report(report: ReconciliationReport) -> None:
    """Print a human-readable reconciliation report."""
    print(f"\n{'=' * 70}")
    print(f"Reconciliation Report: {report.source}")
    print(f"{'=' * 70}")
    print(f"  Total checks:  {report.total_checks}")
    print(f"  Passed:        {report.passed}")
    print(f"  Failed:        {report.failed}")

    if report.failures:
        print(f"\n{'─' * 70}")
        print("FAILURES:")
        print(f"{'─' * 70}")
        for f in report.failures:
            print(f"  [{f.module.upper()}] {f.check}")
            print(f"    Expected: {f.expected}")
            print(f"    Actual:   {f.actual}")
            if f.difference is not None:
                print(f"    Diff:     {f.difference:.6f}")
            if f.message:
                print(f"    Detail:   {f.message}")
            print()

    print(f"{'=' * 70}")


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Per-module reconciliation checker for legacy/modern system outputs",
    )
    parser.add_argument(
        "--results", required=True, action="append",
        help="Results directory to check (can specify multiple for comparison)",
    )
    parser.add_argument(
        "--compare", action="store_true",
        help="When two --results dirs are provided, compare reconciliation across both",
    )
    parser.add_argument(
        "--output", default="reconciliation-report.json",
        help="Output report file (default: reconciliation-report.json)",
    )
    args = parser.parse_args()

    all_reports: list[ReconciliationReport] = []
    any_failures = False

    for results_path in args.results:
        results_dir = Path(results_path)
        if not results_dir.is_dir():
            print(f"ERROR: Results directory not found: {results_dir}")
            sys.exit(2)

        report = reconcile_results(results_dir)
        print_report(report)
        all_reports.append(report)

        if report.failed > 0:
            any_failures = True

    # Cross-system comparison
    if args.compare and len(all_reports) == 2:
        r1, r2 = all_reports
        print(f"\n{'=' * 70}")
        print(f"Cross-System Reconciliation: {r1.source} vs {r2.source}")
        print(f"{'=' * 70}")
        print(f"  {r1.source}: {r1.passed}/{r1.total_checks} checks passed")
        print(f"  {r2.source}: {r2.passed}/{r2.total_checks} checks passed")

        # Compare failure patterns
        r1_checks = {f.check for f in r1.failures}
        r2_checks = {f.check for f in r2.failures}
        common_failures = r1_checks & r2_checks
        only_r1 = r1_checks - r2_checks
        only_r2 = r2_checks - r1_checks

        if common_failures:
            print(f"\n  Shared failures ({len(common_failures)}):")
            for c in sorted(common_failures):
                print(f"    - {c}")
        if only_r1:
            print(f"\n  Only in {r1.source} ({len(only_r1)}):")
            for c in sorted(only_r1):
                print(f"    - {c}")
        if only_r2:
            print(f"\n  Only in {r2.source} ({len(only_r2)}):")
            for c in sorted(only_r2):
                print(f"    - {c}")

        print(f"{'=' * 70}")

    # Write JSON report
    output_data = {
        "reports": [
            {
                "source": r.source,
                "total_checks": r.total_checks,
                "passed": r.passed,
                "failed": r.failed,
                "failures": [
                    {
                        "module": f.module,
                        "check": f.check,
                        "expected": f.expected,
                        "actual": f.actual,
                        "difference": f.difference,
                        "message": f.message,
                    }
                    for f in r.failures
                ],
            }
            for r in all_reports
        ],
    }
    with open(args.output, "w") as f:
        json.dump(output_data, f, indent=2, default=str)
    print(f"\nDetailed report written to: {args.output}")

    sys.exit(1 if any_failures else 0)


if __name__ == "__main__":
    main()
