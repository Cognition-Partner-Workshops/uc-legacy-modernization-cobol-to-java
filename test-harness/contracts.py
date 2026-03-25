"""
contracts.py - Contract validation utilities for COBOL-to-Java migration.

Validates that Java data models honour the implicit contracts defined by
COBOL copybooks: field names, types, sizes, and byte offsets.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Any

from .copybook_parser import FieldDef, pic_type


# ---------------------------------------------------------------------------
# Java type mapping expectations
# ---------------------------------------------------------------------------
COBOL_TO_JAVA_TYPE: dict[str, str] = {
    "alpha":          "String",
    "unsigned":       "long",
    "signed_decimal": "BigDecimal",
}


@dataclass
class ContractViolation:
    """A single contract violation."""
    copybook: str
    field_name: str
    violation_type: str  # "missing_field", "wrong_type", "wrong_length"
    expected: str
    actual: str
    message: str = ""


@dataclass
class ContractReport:
    """Result of a contract validation."""
    copybook: str
    java_class: str
    violations: list[ContractViolation] = field(default_factory=list)

    @property
    def passed(self) -> bool:
        return len(self.violations) == 0

    def summary(self) -> dict[str, Any]:
        return {
            "copybook": self.copybook,
            "java_class": self.java_class,
            "passed": self.passed,
            "violation_count": len(self.violations),
            "violations": [
                {
                    "field": v.field_name,
                    "type": v.violation_type,
                    "expected": v.expected,
                    "actual": v.actual,
                    "message": v.message,
                }
                for v in self.violations
            ],
        }


@dataclass
class JavaFieldDef:
    """Definition of a Java DTO field for contract comparison."""
    name: str
    java_type: str
    length: int | None = None  # max length for String fields


def validate_dto_contract(
    *,
    copybook_name: str,
    copybook_layout: list[FieldDef],
    java_class_name: str,
    java_fields: list[JavaFieldDef],
) -> ContractReport:
    """Validate that a Java DTO covers all copybook fields with correct types.

    Args:
        copybook_name: Name of the COBOL copybook (e.g., "CVACT01Y").
        copybook_layout: Parsed field layout from the copybook.
        java_class_name: Name of the Java DTO class.
        java_fields: List of Java field definitions to validate against.

    Returns:
        A ContractReport with any violations found.
    """
    report = ContractReport(copybook=copybook_name, java_class=java_class_name)

    # Build a lookup of Java fields by name (normalised)
    java_lookup: dict[str, JavaFieldDef] = {}
    for jf in java_fields:
        # Normalise: COBOL uses hyphens, Java uses camelCase or underscores
        normalised = jf.name.upper().replace("_", "-")
        java_lookup[normalised] = jf

    for cfield in copybook_layout:
        # Skip FILLER fields
        if cfield.name.upper().startswith("FILLER"):
            continue

        cobol_name = cfield.name.upper()
        ptype = pic_type(cfield.pic)
        expected_java_type = COBOL_TO_JAVA_TYPE.get(ptype, "String")

        # Check field exists
        jfield = java_lookup.get(cobol_name)
        if jfield is None:
            report.violations.append(ContractViolation(
                copybook=copybook_name,
                field_name=cobol_name,
                violation_type="missing_field",
                expected=f"Field {cobol_name} ({expected_java_type})",
                actual="not found",
                message=f"Java class {java_class_name} is missing field {cobol_name}",
            ))
            continue

        # Check type
        if jfield.java_type != expected_java_type:
            report.violations.append(ContractViolation(
                copybook=copybook_name,
                field_name=cobol_name,
                violation_type="wrong_type",
                expected=expected_java_type,
                actual=jfield.java_type,
                message=(
                    f"Field {cobol_name}: expected {expected_java_type}, "
                    f"got {jfield.java_type}"
                ),
            ))

        # Check length for String fields
        if ptype == "alpha" and jfield.length is not None:
            if jfield.length != cfield.length:
                report.violations.append(ContractViolation(
                    copybook=copybook_name,
                    field_name=cobol_name,
                    violation_type="wrong_length",
                    expected=str(cfield.length),
                    actual=str(jfield.length),
                    message=(
                        f"Field {cobol_name}: expected length {cfield.length}, "
                        f"got {jfield.length}"
                    ),
                ))

    return report


def validate_record_layout_offsets(
    *,
    copybook_name: str,
    copybook_layout: list[FieldDef],
    expected_record_length: int,
) -> ContractReport:
    """Validate that a copybook layout has the correct total record length.

    This ensures the layout definition itself is internally consistent.
    """
    report = ContractReport(copybook=copybook_name, java_class="(layout self-check)")

    total = sum(f.length for f in copybook_layout)
    if total != expected_record_length:
        report.violations.append(ContractViolation(
            copybook=copybook_name,
            field_name="_record_length",
            violation_type="wrong_length",
            expected=str(expected_record_length),
            actual=str(total),
            message=(
                f"Layout total length {total} != expected record length {expected_record_length}"
            ),
        ))

    # Verify offsets are contiguous
    expected_offset = 0
    for f in copybook_layout:
        if f.offset != expected_offset:
            report.violations.append(ContractViolation(
                copybook=copybook_name,
                field_name=f.name,
                violation_type="wrong_offset",
                expected=str(expected_offset),
                actual=str(f.offset),
                message=f"Field {f.name} offset gap: expected {expected_offset}, got {f.offset}",
            ))
        expected_offset = f.offset + f.length

    return report


# ---------------------------------------------------------------------------
# Batch I/O contract checks
# ---------------------------------------------------------------------------

@dataclass
class BatchIOContract:
    """Expected I/O contract for a batch job."""
    job_name: str
    input_datasets: list[dict[str, Any]]   # [{name, record_length, key_fields}]
    output_datasets: list[dict[str, Any]]  # [{name, record_length, key_fields}]


# CardDemo batch job I/O contracts (derived from JCL DD statements)
POSTTRAN_CONTRACT = BatchIOContract(
    job_name="POSTTRAN",
    input_datasets=[
        {"name": "DALYTRAN", "record_length": 350, "key_fields": ["DALYTRAN-ID"]},
        {"name": "XREFFILE", "record_length": 50, "key_fields": ["XREF-CARD-NUM"]},
        {"name": "ACCTFILE", "record_length": 300, "key_fields": ["ACCT-ID"]},
        {"name": "TCATBALF", "record_length": 50, "key_fields": ["TRANCAT-ACCT-ID", "TRANCAT-TYPE-CD", "TRANCAT-CD"]},
    ],
    output_datasets=[
        {"name": "TRANFILE", "record_length": 350, "key_fields": ["TRAN-ID"]},
        {"name": "DALYREJS", "record_length": 430, "key_fields": []},
        {"name": "ACCTFILE", "record_length": 300, "key_fields": ["ACCT-ID"]},
        {"name": "TCATBALF", "record_length": 50, "key_fields": ["TRANCAT-ACCT-ID", "TRANCAT-TYPE-CD", "TRANCAT-CD"]},
    ],
)

INTCALC_CONTRACT = BatchIOContract(
    job_name="INTCALC",
    input_datasets=[
        {"name": "TCATBALF", "record_length": 50, "key_fields": ["TRANCAT-ACCT-ID", "TRANCAT-TYPE-CD", "TRANCAT-CD"]},
        {"name": "XREFFILE", "record_length": 50, "key_fields": ["XREF-CARD-NUM"]},
        {"name": "ACCTFILE", "record_length": 300, "key_fields": ["ACCT-ID"]},
        {"name": "DISCGRP", "record_length": 50, "key_fields": ["DIS-ACCT-GROUP-ID", "DIS-TRAN-TYPE-CD", "DIS-TRAN-CAT-CD"]},
    ],
    output_datasets=[
        {"name": "TRANSACT", "record_length": 350, "key_fields": ["TRAN-ID"]},
    ],
)

CREASTMT_CONTRACT = BatchIOContract(
    job_name="CREASTMT",
    input_datasets=[
        {"name": "TRNXFILE", "record_length": 350, "key_fields": ["TRAN-CARD-NUM", "TRAN-ID"]},
        {"name": "XREFFILE", "record_length": 50, "key_fields": ["XREF-CARD-NUM"]},
        {"name": "ACCTFILE", "record_length": 300, "key_fields": ["ACCT-ID"]},
        {"name": "CUSTFILE", "record_length": 500, "key_fields": ["CUST-ID"]},
    ],
    output_datasets=[
        {"name": "STMTFILE", "record_length": 80, "key_fields": []},
        {"name": "HTMLFILE", "record_length": 100, "key_fields": []},
    ],
)

COMBTRAN_CONTRACT = BatchIOContract(
    job_name="COMBTRAN",
    input_datasets=[
        {"name": "TRANSACT.BKUP", "record_length": 350, "key_fields": ["TRAN-ID"]},
        {"name": "SYSTRAN", "record_length": 350, "key_fields": ["TRAN-ID"]},
    ],
    output_datasets=[
        {"name": "TRANSACT.COMBINED", "record_length": 350, "key_fields": ["TRAN-ID"]},
    ],
)

TRANBKP_CONTRACT = BatchIOContract(
    job_name="TRANBKP",
    input_datasets=[
        {"name": "TRANSACT.VSAM.KSDS", "record_length": 350, "key_fields": ["TRAN-ID"]},
    ],
    output_datasets=[
        {"name": "TRANSACT.BKUP", "record_length": 350, "key_fields": ["TRAN-ID"]},
    ],
)

ALL_BATCH_CONTRACTS = [
    POSTTRAN_CONTRACT,
    INTCALC_CONTRACT,
    CREASTMT_CONTRACT,
    COMBTRAN_CONTRACT,
    TRANBKP_CONTRACT,
]


def validate_batch_io_contract(
    contract: BatchIOContract,
    actual_inputs: list[dict[str, Any]] | None = None,
    actual_outputs: list[dict[str, Any]] | None = None,
) -> ContractReport:
    """Validate that a Java batch job's actual I/O matches the expected contract.

    Args:
        contract: The expected I/O contract.
        actual_inputs: List of dicts with {name, record_length} from the Java job.
        actual_outputs: List of dicts with {name, record_length} from the Java job.

    Returns:
        A ContractReport with any violations.
    """
    report = ContractReport(
        copybook=f"JCL:{contract.job_name}",
        java_class=f"Java:{contract.job_name}",
    )

    if actual_inputs is not None:
        expected_names = {d["name"] for d in contract.input_datasets}
        actual_names = {d["name"] for d in actual_inputs}
        for missing in expected_names - actual_names:
            report.violations.append(ContractViolation(
                copybook=f"JCL:{contract.job_name}",
                field_name=missing,
                violation_type="missing_input",
                expected=missing,
                actual="not found",
                message=f"Input dataset {missing} not provided by Java job",
            ))

    if actual_outputs is not None:
        expected_names = {d["name"] for d in contract.output_datasets}
        actual_names = {d["name"] for d in actual_outputs}
        for missing in expected_names - actual_names:
            report.violations.append(ContractViolation(
                copybook=f"JCL:{contract.job_name}",
                field_name=missing,
                violation_type="missing_output",
                expected=missing,
                actual="not found",
                message=f"Output dataset {missing} not produced by Java job",
            ))

    return report
