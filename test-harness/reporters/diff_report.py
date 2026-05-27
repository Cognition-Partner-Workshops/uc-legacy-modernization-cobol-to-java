"""
Human-readable diff report generator.

Produces formatted reports summarizing comparison and validation results
for review by migration engineers.
"""

import json
from pathlib import Path
from datetime import datetime, timezone


def generate_summary_report(
    golden_results: dict = None,
    differential_results: dict = None,
    reconciliation_results: dict = None,
    contract_results: dict = None,
    output_path: str = None,
) -> str:
    """Generate a comprehensive summary report of all test dimensions.

    Args:
        golden_results: Results from golden file comparisons
        differential_results: Results from differential comparisons
        reconciliation_results: Results from reconciliation checks
        contract_results: Results from contract validation
        output_path: Optional path to write report JSON

    Returns:
        Formatted summary string
    """
    report = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "dimensions": {},
        "overall_passed": True,
    }

    lines = []
    lines.append("=" * 70)
    lines.append("  MIGRATION TEST HARNESS - SUMMARY REPORT")
    lines.append(f"  Generated: {report['timestamp']}")
    lines.append("=" * 70)
    lines.append("")

    # Golden file results
    if golden_results:
        lines.append("─" * 70)
        lines.append("  1. GOLDEN-FILE TESTING")
        lines.append("─" * 70)
        passed = all(r.get("passed", False) for r in golden_results.values())
        report["dimensions"]["golden_file"] = {"passed": passed}
        if not passed:
            report["overall_passed"] = False
        for name, result in golden_results.items():
            status = "PASS" if result.get("passed") else "FAIL"
            lines.append(f"    [{status}] {name}: {result.get('summary', '')}")
        lines.append("")

    # Differential results
    if differential_results:
        lines.append("─" * 70)
        lines.append("  2. DIFFERENTIAL TESTING")
        lines.append("─" * 70)
        passed = all(r.get("passed", False) for r in differential_results.values())
        report["dimensions"]["differential"] = {"passed": passed}
        if not passed:
            report["overall_passed"] = False
        for name, result in differential_results.items():
            status = "PASS" if result.get("passed") else "FAIL"
            lines.append(f"    [{status}] {name}: {result.get('summary', '')}")
        lines.append("")

    # Reconciliation results
    if reconciliation_results:
        lines.append("─" * 70)
        lines.append("  3. RECONCILIATION TESTING")
        lines.append("─" * 70)
        all_reports = reconciliation_results
        if hasattr(reconciliation_results, "values"):
            all_reports = reconciliation_results
        passed = all(
            r.get("passed", False) if isinstance(r, dict) else r.passed
            for r in (all_reports.values() if isinstance(all_reports, dict) else [])
        )
        report["dimensions"]["reconciliation"] = {"passed": passed}
        if not passed:
            report["overall_passed"] = False
        if isinstance(all_reports, dict):
            for job_name, job_report in all_reports.items():
                if hasattr(job_report, "to_dict"):
                    data = job_report.to_dict()
                else:
                    data = job_report
                status = "PASS" if data.get("passed", False) else "FAIL"
                total = data.get("total_checks", 0)
                failed = data.get("failed_checks", 0)
                lines.append(f"    [{status}] {job_name}: {total} checks, {failed} failures")
                if failed > 0 and "results" in data:
                    for check in data["results"]:
                        if not check.get("passed", True):
                            lines.append(f"           - {check.get('check_name')}: {check.get('message')}")
        lines.append("")

    # Contract results
    if contract_results:
        lines.append("─" * 70)
        lines.append("  4. CONTRACT TESTING")
        lines.append("─" * 70)
        all_valid = True
        for name, result in contract_results.items():
            if hasattr(result, "to_dict"):
                data = result.to_dict()
            else:
                data = result
            passed = data.get("passed", False)
            if not passed:
                all_valid = False
            status = "PASS" if passed else "FAIL"
            total = data.get("total_records", 0)
            valid = data.get("valid_records", 0)
            violations = data.get("total_violations", 0)
            lines.append(
                f"    [{status}] {name} ({data.get('copybook', '')}): "
                f"{valid}/{total} records valid, {violations} violations"
            )
        report["dimensions"]["contract"] = {"passed": all_valid}
        if not all_valid:
            report["overall_passed"] = False
        lines.append("")

    # Overall verdict
    lines.append("=" * 70)
    overall = "PASS" if report["overall_passed"] else "FAIL"
    lines.append(f"  OVERALL RESULT: {overall}")
    lines.append("=" * 70)

    summary_text = "\n".join(lines)

    if output_path:
        output_file = Path(output_path)
        output_file.parent.mkdir(parents=True, exist_ok=True)
        report["summary_text"] = summary_text
        with output_file.open("w") as f:
            json.dump(report, f, indent=2, default=str)

    return summary_text
