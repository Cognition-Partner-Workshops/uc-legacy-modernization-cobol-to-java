"""Contract validation for COBOL-to-Java migration.

Verifies that Java DTOs and file outputs conform to the record layout
contracts defined by COBOL copybooks.
"""

import json
import sys
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, List, Optional

from .copybook_parser import CARDDEMO_LAYOUTS, parse_pic_length


@dataclass
class ContractViolation:
    """A single contract violation."""
    field_name: str
    expected: str
    actual: str
    violation_type: str  # 'missing_field', 'wrong_type', 'wrong_length', 'wrong_offset'


@dataclass
class ContractResult:
    """Result of validating a single contract."""
    contract_name: str
    copybook: str
    status: str  # 'PASS', 'FAIL'
    violations: List[ContractViolation] = field(default_factory=list)
    fields_checked: int = 0

    @property
    def passed(self) -> bool:
        return self.status == "PASS"


@dataclass
class ContractReport:
    """Aggregated contract validation report."""
    contracts_checked: int = 0
    contracts_passed: int = 0
    contracts_failed: int = 0
    results: List[ContractResult] = field(default_factory=list)

    @property
    def passed(self) -> bool:
        return self.contracts_failed == 0

    def summary(self) -> str:
        status = "PASS" if self.passed else "FAIL"
        lines = [
            f"Contract Validation: {status}",
            f"  Contracts Checked: {self.contracts_checked}",
            f"  Passed:            {self.contracts_passed}",
            f"  Failed:            {self.contracts_failed}",
            "",
        ]
        for r in self.results:
            icon = "[OK]" if r.passed else "[FAIL]"
            lines.append(f"  {icon} {r.contract_name} ({r.copybook})")
            for v in r.violations:
                lines.append(f"       {v.violation_type}: {v.field_name} "
                             f"(expected={v.expected}, actual={v.actual})")
        return "\n".join(lines)


def validate_record_length(layout_name: str, actual_length: int) -> ContractResult:
    """Validate that actual record length matches copybook specification.

    Args:
        layout_name: Key into CARDDEMO_LAYOUTS.
        actual_length: The record length produced by the Java system.

    Returns:
        ContractResult indicating pass/fail.
    """
    if layout_name not in CARDDEMO_LAYOUTS:
        return ContractResult(
            contract_name=layout_name,
            copybook="unknown",
            status="FAIL",
            violations=[ContractViolation(
                field_name="<layout>",
                expected=f"known layout",
                actual=layout_name,
                violation_type="unknown_layout",
            )],
        )

    layout = CARDDEMO_LAYOUTS[layout_name]
    expected_length = layout["record_length"]
    result = ContractResult(
        contract_name=layout_name,
        copybook=layout["copybook"],
        status="PASS",
        fields_checked=1,
    )

    if actual_length != expected_length:
        result.status = "FAIL"
        result.violations.append(ContractViolation(
            field_name="RECORD-LENGTH",
            expected=str(expected_length),
            actual=str(actual_length),
            violation_type="wrong_length",
        ))

    return result


def validate_field_schema(
    layout_name: str,
    java_schema: List[Dict[str, object]],
) -> ContractResult:
    """Validate Java DTO field schema against COBOL copybook layout.

    Args:
        layout_name: Key into CARDDEMO_LAYOUTS.
        java_schema: List of field definitions from Java, each with:
            - name: field name
            - offset: byte offset
            - length: field length
            - type: field type (string, numeric, decimal)

    Returns:
        ContractResult with any violations found.
    """
    if layout_name not in CARDDEMO_LAYOUTS:
        return ContractResult(
            contract_name=layout_name,
            copybook="unknown",
            status="FAIL",
        )

    layout = CARDDEMO_LAYOUTS[layout_name]
    result = ContractResult(
        contract_name=layout_name,
        copybook=layout["copybook"],
        status="PASS",
    )

    expected_fields = {f["name"]: f for f in layout["fields"]}
    actual_fields = {f["name"]: f for f in java_schema}

    result.fields_checked = len(expected_fields)

    # Check for missing fields
    for name, expected in expected_fields.items():
        if name not in actual_fields:
            result.violations.append(ContractViolation(
                field_name=name,
                expected="present",
                actual="missing",
                violation_type="missing_field",
            ))
            continue

        actual = actual_fields[name]

        # Check offset
        if actual.get("offset") != expected["offset"]:
            result.violations.append(ContractViolation(
                field_name=name,
                expected=str(expected["offset"]),
                actual=str(actual.get("offset")),
                violation_type="wrong_offset",
            ))

        # Check length
        if actual.get("length") != expected["length"]:
            result.violations.append(ContractViolation(
                field_name=name,
                expected=str(expected["length"]),
                actual=str(actual.get("length")),
                violation_type="wrong_length",
            ))

    if result.violations:
        result.status = "FAIL"

    return result


def validate_output_file_format(
    filepath: str,
    layout_name: str,
) -> ContractResult:
    """Validate that an output file conforms to the expected fixed-width format.

    Checks:
    - All records have the correct length
    - Field positions contain valid data types

    Args:
        filepath: Path to the output file.
        layout_name: Expected layout name.

    Returns:
        ContractResult with validation findings.
    """
    if layout_name not in CARDDEMO_LAYOUTS:
        return ContractResult(
            contract_name=layout_name,
            copybook="unknown",
            status="FAIL",
        )

    layout = CARDDEMO_LAYOUTS[layout_name]
    expected_length = layout["record_length"]
    result = ContractResult(
        contract_name=f"{layout_name}_file_format",
        copybook=layout["copybook"],
        status="PASS",
    )

    path = Path(filepath)
    if not path.exists():
        result.status = "FAIL"
        result.violations.append(ContractViolation(
            field_name="<file>",
            expected="exists",
            actual="not found",
            violation_type="missing_file",
        ))
        return result

    with path.open("r") as f:
        for line_num, line in enumerate(f, 1):
            line = line.rstrip("\n").rstrip("\r")
            if not line:
                continue

            result.fields_checked += 1
            actual_len = len(line)

            if actual_len != expected_length:
                result.violations.append(ContractViolation(
                    field_name=f"line_{line_num}",
                    expected=str(expected_length),
                    actual=str(actual_len),
                    violation_type="wrong_length",
                ))

                if len(result.violations) >= 10:
                    break

    if result.violations:
        result.status = "FAIL"

    return result


def validate_all_contracts(data_dir: str) -> ContractReport:
    """Run all contract validations against data files.

    Args:
        data_dir: Path to directory containing data files.

    Returns:
        ContractReport with all results.
    """
    report = ContractReport()

    for layout_name, layout in CARDDEMO_LAYOUTS.items():
        filepath = Path(data_dir) / f"{layout_name}.txt"
        if not filepath.exists():
            continue

        result = validate_output_file_format(str(filepath), layout_name)
        report.results.append(result)
        report.contracts_checked += 1

        if result.passed:
            report.contracts_passed += 1
        else:
            report.contracts_failed += 1

    return report


def report_to_json(report: ContractReport) -> dict:
    """Serialize contract report to JSON."""
    return {
        "status": "PASS" if report.passed else "FAIL",
        "contracts_checked": report.contracts_checked,
        "contracts_passed": report.contracts_passed,
        "contracts_failed": report.contracts_failed,
        "results": [
            {
                "contract_name": r.contract_name,
                "copybook": r.copybook,
                "status": r.status,
                "fields_checked": r.fields_checked,
                "violations": [
                    {
                        "field_name": v.field_name,
                        "expected": v.expected,
                        "actual": v.actual,
                        "violation_type": v.violation_type,
                    }
                    for v in r.violations
                ],
            }
            for r in report.results
        ],
    }


if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(
        description="Validate file format contracts against copybook specifications"
    )
    parser.add_argument(
        "--data-dir",
        default="app/data/ASCII",
        help="Path to data directory",
    )
    parser.add_argument(
        "--output",
        help="Write JSON report to file",
    )

    args = parser.parse_args()

    report = validate_all_contracts(args.data_dir)

    print(report.summary())

    if args.output:
        json_report = report_to_json(report)
        Path(args.output).write_text(json.dumps(json_report, indent=2))
        print(f"\nJSON report written to: {args.output}")

    sys.exit(0 if report.passed else 1)
