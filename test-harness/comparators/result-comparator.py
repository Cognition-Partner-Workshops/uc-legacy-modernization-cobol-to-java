#!/usr/bin/env python3
"""
Enhanced Result Comparator - Legacy vs Modern System Output Comparison

Compares the outputs of business scenarios executed against both the legacy
COBOL/CICS/VSAM system and the modernized Java system. Supports financial
tolerance rules, wildcard/pattern matching, and detailed mismatch reporting.

Usage:
    python result-comparator.py --legacy results/legacy/ --modern results/modern/
    python result-comparator.py --legacy results/legacy/ --modern results/modern/ \\
        --tolerance-config tolerances.yaml --output report.json
"""

import argparse
import fnmatch
import json
import re
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any, Optional


# ---------------------------------------------------------------------------
# Financial tolerance rule presets
# ---------------------------------------------------------------------------

FINANCIAL_TOLERANCE_RULES: dict[str, float] = {
    # Currency amounts: allow 1-cent rounding difference
    "gross_pay": 0.01,
    "net_pay": 0.01,
    "base_pay": 0.01,
    "overtime_pay": 0.01,
    "holiday_premium_pay": 0.01,
    "holiday_worked_pay": 0.01,
    "federal_tax": 0.01,
    "state_tax": 0.01,
    "fica": 0.01,
    "medicare": 0.01,
    "total_gross": 0.01,
    "total_net": 0.01,
    "total_deductions": 0.01,
    "regular_pay": 0.01,
    "salary": 0.01,
    "hourly_rate": 0.001,
    # Percentage-based fields: allow 0.1% tolerance
    "tax_rate": 0.001,
    "overtime_rate": 0.001,
    "premium_rate": 0.001,
}

# Fields where wildcard matching is always enabled
WILDCARD_FIELDS: set[str] = {
    "emp_number",
    "emp_id",
    "request_id",
    "run_id",
    "timecard_id",
    "transaction_id",
    "session_id",
    "record_id",
}


@dataclass
class ComparisonResult:
    """Stores the outcome of comparing a single field between legacy and modern."""

    scenario: str
    step: str
    field: str
    legacy_value: Any
    modern_value: Any
    status: str  # MATCH, MISMATCH, TOLERANCE, WILDCARD, MISSING_LEGACY, MISSING_MODERN
    tolerance: float = 0.0
    message: str = ""


@dataclass
class ComparisonSummary:
    """Aggregated comparison statistics."""

    total: int = 0
    matches: int = 0
    tolerance_matches: int = 0
    wildcard_matches: int = 0
    mismatches: int = 0
    missing_legacy: int = 0
    missing_modern: int = 0
    details: list[ComparisonResult] = field(default_factory=list)


def _is_financial_field(field_name: str) -> bool:
    """Check if a field name represents a financial/monetary value."""
    financial_keywords = (
        "pay", "salary", "tax", "gross", "net", "deduction",
        "fica", "medicare", "rate", "amount", "total", "balance",
        "wage", "bonus", "commission", "allowance", "premium",
    )
    lower = field_name.lower()
    return any(kw in lower for kw in financial_keywords)


def _get_tolerance(field_name: str, step_tolerance: dict[str, float],
                   custom_rules: Optional[dict[str, float]] = None) -> float:
    """Resolve the tolerance for a field using a priority chain.

    Priority:
        1. Per-step tolerance from the scenario YAML
        2. Custom tolerance config (loaded from --tolerance-config)
        3. Built-in financial tolerance rules
        4. Zero (exact match)
    """
    if field_name in step_tolerance:
        return step_tolerance[field_name]
    if custom_rules and field_name in custom_rules:
        return custom_rules[field_name]
    if field_name in FINANCIAL_TOLERANCE_RULES:
        return FINANCIAL_TOLERANCE_RULES[field_name]
    return 0.0


def match_wildcard(pattern: str, value: str) -> bool:
    """Match a value against a wildcard pattern.

    Supports:
        *           - matches any sequence of characters
        ?           - matches any single character
        [seq]       - matches any character in seq
        EMP-*       - matches any string starting with 'EMP-'
        /regex/     - if the pattern is enclosed in slashes, treat as regex
    """
    if not isinstance(pattern, str) or not isinstance(value, str):
        return False

    # Regex mode: /pattern/
    if pattern.startswith("/") and pattern.endswith("/") and len(pattern) > 2:
        try:
            return bool(re.fullmatch(pattern[1:-1], value))
        except re.error:
            return False

    # fnmatch-style glob matching
    return fnmatch.fnmatch(value, pattern)


def compare_values(
    legacy: Any,
    modern: Any,
    field_name: str,
    tolerance: float = 0.0,
    wildcard: bool = False,
) -> tuple[str, str]:
    """Compare two values with tolerance and wildcard support.

    Returns:
        (status, message) tuple
    """
    # Handle None / missing
    if legacy is None and modern is None:
        return "MATCH", ""
    if legacy is None:
        return "MISSING_LEGACY", "Field missing from legacy output"
    if modern is None:
        return "MISSING_MODERN", "Field missing from modern output"

    # Wildcard matching
    if wildcard or field_name in WILDCARD_FIELDS:
        if isinstance(legacy, str) and (
            "*" in legacy or "?" in legacy or legacy.startswith("/")
        ):
            if match_wildcard(legacy, str(modern)):
                return "WILDCARD", f"Pattern '{legacy}' matched '{modern}'"
            return "MISMATCH", f"Pattern '{legacy}' did not match '{modern}'"

    # Comparison operator expressions (e.g. "> 0", ">= 5")
    if isinstance(legacy, str):
        op_match = re.match(r"^(>=?|<=?|!=)\s*(.+)$", legacy)
        if op_match:
            op, threshold_str = op_match.groups()
            try:
                threshold = float(threshold_str)
                modern_num = float(modern)
                ops = {
                    ">": modern_num > threshold,
                    ">=": modern_num >= threshold,
                    "<": modern_num < threshold,
                    "<=": modern_num <= threshold,
                    "!=": modern_num != threshold,
                }
                if ops.get(op, False):
                    return "MATCH", f"{modern_num} {op} {threshold}"
                return "MISMATCH", f"{modern_num} not {op} {threshold}"
            except (ValueError, TypeError):
                pass

    # Numeric comparison with tolerance
    if isinstance(legacy, (int, float)) and isinstance(modern, (int, float)):
        diff = abs(legacy - modern)
        if diff == 0:
            return "MATCH", ""
        if diff <= tolerance:
            return "TOLERANCE", f"Within tolerance: diff={diff:.6f}, tol={tolerance}"
        return "MISMATCH", f"Numeric mismatch: diff={diff:.6f}, tol={tolerance}"

    # Boolean comparison (handle string/bool mismatches)
    if isinstance(legacy, bool) or isinstance(modern, bool):
        truthy = {"true", "1", "yes", "y"}
        l_bool = str(legacy).lower() in truthy
        m_bool = str(modern).lower() in truthy
        if l_bool == m_bool:
            return "MATCH", "Boolean equivalence"
        return "MISMATCH", f"Boolean mismatch: legacy={legacy}, modern={modern}"

    # String comparison (case-insensitive for status/code fields)
    l_str = str(legacy).strip()
    m_str = str(modern).strip()
    if l_str == m_str:
        return "MATCH", ""

    # Try case-insensitive match for status-like fields
    status_keywords = ("status", "flag", "type", "code", "reason")
    if any(kw in field_name.lower() for kw in status_keywords):
        if l_str.upper() == m_str.upper():
            return "MATCH", "Case-insensitive match"

    return "MISMATCH", f"legacy='{l_str}', modern='{m_str}'"


def compare_scenario(
    legacy_file: Path,
    modern_file: Path,
    custom_tolerances: Optional[dict[str, float]] = None,
) -> list[ComparisonResult]:
    """Compare results from a single scenario file pair."""
    results: list[ComparisonResult] = []

    with open(legacy_file) as f:
        legacy_data = json.load(f)
    with open(modern_file) as f:
        modern_data = json.load(f)

    scenario_name = legacy_data.get("name", legacy_file.stem)

    legacy_steps = legacy_data.get("steps", [])
    modern_steps = modern_data.get("steps", [])

    # Warn on step count mismatch
    if len(legacy_steps) != len(modern_steps):
        results.append(ComparisonResult(
            scenario=scenario_name,
            step="_meta",
            field="step_count",
            legacy_value=len(legacy_steps),
            modern_value=len(modern_steps),
            status="MISMATCH",
            message="Step count mismatch between legacy and modern results",
        ))

    for i, (legacy_step, modern_step) in enumerate(zip(legacy_steps, modern_steps)):
        step_name = legacy_step.get("name", f"step_{i}")
        step_tolerance = legacy_step.get("tolerance", {})

        legacy_output = legacy_step.get("output", {})
        modern_output = modern_step.get("output", {})

        all_fields = set(legacy_output.keys()) | set(modern_output.keys())

        for field_name in sorted(all_fields):
            l_val = legacy_output.get(field_name)
            m_val = modern_output.get(field_name)

            tol = _get_tolerance(field_name, step_tolerance, custom_tolerances)
            is_wildcard = field_name in WILDCARD_FIELDS

            status, message = compare_values(
                l_val, m_val, field_name, tolerance=tol, wildcard=is_wildcard,
            )

            results.append(ComparisonResult(
                scenario=scenario_name,
                step=step_name,
                field=field_name,
                legacy_value=l_val,
                modern_value=m_val,
                status=status,
                tolerance=tol,
                message=message,
            ))

    return results


def load_tolerance_config(config_path: str) -> dict[str, float]:
    """Load custom tolerance rules from a JSON or YAML file."""
    path = Path(config_path)
    with open(path) as f:
        if path.suffix in (".yaml", ".yml"):
            try:
                import yaml
                return yaml.safe_load(f) or {}
            except ImportError:
                print("WARNING: PyYAML not installed; skipping YAML tolerance config")
                return {}
        return json.load(f)


def generate_report(summary: ComparisonSummary, output_path: str) -> None:
    """Write a detailed JSON comparison report."""
    report = {
        "summary": {
            "total_fields": summary.total,
            "matches": summary.matches,
            "tolerance_matches": summary.tolerance_matches,
            "wildcard_matches": summary.wildcard_matches,
            "mismatches": summary.mismatches,
            "missing_legacy": summary.missing_legacy,
            "missing_modern": summary.missing_modern,
            "pass_rate": (
                "{:.1f}%".format(
                    (summary.matches + summary.tolerance_matches
                     + summary.wildcard_matches) / summary.total * 100
                )
                if summary.total > 0 else "N/A"
            ),
        },
        "details": [
            {
                "scenario": r.scenario,
                "step": r.step,
                "field": r.field,
                "legacy": r.legacy_value,
                "modern": r.modern_value,
                "status": r.status,
                "tolerance": r.tolerance,
                "message": r.message,
            }
            for r in summary.details
        ],
    }
    with open(output_path, "w") as f:
        json.dump(report, f, indent=2, default=str)


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Enhanced comparator: legacy COBOL vs modern Java system outputs",
    )
    parser.add_argument("--legacy", required=True, help="Directory with legacy results")
    parser.add_argument("--modern", required=True, help="Directory with modern results")
    parser.add_argument("--tolerance-config", default=None,
                        help="Optional JSON/YAML file with custom tolerance overrides")
    parser.add_argument("--output", default="comparison-report.json",
                        help="Output report file (default: comparison-report.json)")
    args = parser.parse_args()

    legacy_dir = Path(args.legacy)
    modern_dir = Path(args.modern)

    if not legacy_dir.is_dir():
        print(f"ERROR: Legacy results directory not found: {legacy_dir}")
        sys.exit(2)
    if not modern_dir.is_dir():
        print(f"ERROR: Modern results directory not found: {modern_dir}")
        sys.exit(2)

    custom_tolerances = None
    if args.tolerance_config:
        custom_tolerances = load_tolerance_config(args.tolerance_config)

    summary = ComparisonSummary()

    legacy_files = sorted(legacy_dir.glob("*.json"))
    if not legacy_files:
        print("WARNING: No legacy result files found")

    for legacy_file in legacy_files:
        modern_file = modern_dir / legacy_file.name
        if not modern_file.exists():
            print(f"WARNING: No modern result for {legacy_file.name}")
            continue

        results = compare_scenario(legacy_file, modern_file, custom_tolerances)
        summary.details.extend(results)

        for r in results:
            summary.total += 1
            if r.status == "MATCH":
                summary.matches += 1
            elif r.status == "TOLERANCE":
                summary.tolerance_matches += 1
                print(f"  TOLERANCE: {r.scenario}/{r.step}/{r.field}: {r.message}")
            elif r.status == "WILDCARD":
                summary.wildcard_matches += 1
            elif r.status == "MISSING_LEGACY":
                summary.missing_legacy += 1
                print(f"  MISSING(legacy): {r.scenario}/{r.step}/{r.field}")
            elif r.status == "MISSING_MODERN":
                summary.missing_modern += 1
                print(f"  MISSING(modern): {r.scenario}/{r.step}/{r.field}")
            else:
                summary.mismatches += 1
                print(f"  MISMATCH: {r.scenario}/{r.step}/{r.field}: {r.message}")

    # Print summary
    passed = summary.matches + summary.tolerance_matches + summary.wildcard_matches
    print(f"\n{'=' * 70}")
    print(f"Comparison complete: {summary.total} fields checked")
    print(f"  Exact matches:     {summary.matches}")
    print(f"  Tolerance matches: {summary.tolerance_matches}")
    print(f"  Wildcard matches:  {summary.wildcard_matches}")
    print(f"  Mismatches:        {summary.mismatches}")
    print(f"  Missing (legacy):  {summary.missing_legacy}")
    print(f"  Missing (modern):  {summary.missing_modern}")
    if summary.total > 0:
        print(f"  Pass rate:         {passed / summary.total * 100:.1f}%")
    else:
        print("  No data to compare")
    print(f"{'=' * 70}")

    generate_report(summary, args.output)
    print(f"Detailed report written to: {args.output}")

    sys.exit(1 if summary.mismatches > 0 else 0)


if __name__ == "__main__":
    main()
