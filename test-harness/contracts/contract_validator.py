"""
Contract validator for copybook-derived schemas.

Validates that records conform to the structural contracts defined
by COBOL copybook layouts: correct field types, lengths, value ranges,
and record structure.
"""

import json
from dataclasses import dataclass, field
from pathlib import Path


@dataclass
class ContractViolation:
    """A single contract violation."""
    record_index: int
    field_name: str
    constraint: str
    expected: str
    actual: str
    severity: str = "error"  # 'error' or 'warning'


@dataclass
class ContractValidationResult:
    """Result of validating records against their contract schema."""
    schema_name: str
    copybook: str
    total_records: int
    valid_records: int
    violations: list = field(default_factory=list)

    @property
    def passed(self) -> bool:
        return len([v for v in self.violations if v.severity == "error"]) == 0

    def to_dict(self) -> dict:
        return {
            "schema_name": self.schema_name,
            "copybook": self.copybook,
            "total_records": self.total_records,
            "valid_records": self.valid_records,
            "passed": self.passed,
            "total_violations": len(self.violations),
            "error_count": len([v for v in self.violations if v.severity == "error"]),
            "warning_count": len([v for v in self.violations if v.severity == "warning"]),
            "violations": [
                {
                    "record_index": v.record_index,
                    "field_name": v.field_name,
                    "constraint": v.constraint,
                    "expected": v.expected,
                    "actual": v.actual,
                    "severity": v.severity,
                }
                for v in self.violations[:100]
            ],
        }


# Contract schemas derived from copybook definitions
SCHEMAS = {
    "account": {
        "copybook": "CVACT01Y.cpy",
        "record_length": 300,
        "fields": {
            "ACCT-ID": {"type": "integer", "min": 1, "max": 99999999999, "required": True},
            "ACCT-ACTIVE-STATUS": {"type": "string", "length": 1, "valid_values": ["Y", "N"], "required": True},
            "ACCT-CURR-BAL": {"type": "number", "required": True},
            "ACCT-CREDIT-LIMIT": {"type": "number", "min": 0, "required": True},
            "ACCT-CASH-CREDIT-LIMIT": {"type": "number", "min": 0, "required": True},
            "ACCT-OPEN-DATE": {"type": "date", "format": "YYYY-MM-DD", "required": True},
            "ACCT-EXPIRAION-DATE": {"type": "date", "format": "YYYY-MM-DD", "required": True},
            "ACCT-REISSUE-DATE": {"type": "date", "format": "YYYY-MM-DD", "required": True},
            "ACCT-CURR-CYC-CREDIT": {"type": "number", "required": True},
            "ACCT-CURR-CYC-DEBIT": {"type": "number", "required": True},
            "ACCT-ADDR-ZIP": {"type": "string", "max_length": 10, "required": False},
            "ACCT-GROUP-ID": {"type": "string", "max_length": 10, "required": False},
        },
    },
    "card": {
        "copybook": "CVACT02Y.cpy",
        "record_length": 150,
        "fields": {
            "CARD-NUM": {"type": "string", "length": 16, "required": True},
            "CARD-ACCT-ID": {"type": "integer", "min": 1, "max": 99999999999, "required": True},
            "CARD-CVV-CD": {"type": "integer", "min": 0, "max": 999, "required": True},
            "CARD-EMBOSSED-NAME": {"type": "string", "max_length": 50, "required": True},
            "CARD-EXPIRAION-DATE": {"type": "date", "format": "YYYY-MM-DD", "required": True},
            "CARD-ACTIVE-STATUS": {"type": "string", "length": 1, "valid_values": ["Y", "N"], "required": True},
        },
    },
    "customer": {
        "copybook": "CVCUS01Y.cpy",
        "record_length": 500,
        "fields": {
            "CUST-ID": {"type": "integer", "min": 1, "max": 999999999, "required": True},
            "CUST-FIRST-NAME": {"type": "string", "max_length": 25, "required": True},
            "CUST-MIDDLE-NAME": {"type": "string", "max_length": 25, "required": False},
            "CUST-LAST-NAME": {"type": "string", "max_length": 25, "required": True},
            "CUST-ADDR-LINE-1": {"type": "string", "max_length": 50, "required": True},
            "CUST-ADDR-LINE-2": {"type": "string", "max_length": 50, "required": False},
            "CUST-ADDR-LINE-3": {"type": "string", "max_length": 50, "required": False},
            "CUST-ADDR-STATE-CD": {"type": "string", "length": 2, "required": True},
            "CUST-ADDR-COUNTRY-CD": {"type": "string", "max_length": 3, "required": True},
            "CUST-ADDR-ZIP": {"type": "string", "max_length": 10, "required": True},
            "CUST-PHONE-NUM-1": {"type": "string", "max_length": 15, "required": False},
            "CUST-PHONE-NUM-2": {"type": "string", "max_length": 15, "required": False},
            "CUST-SSN": {"type": "integer", "min": 0, "max": 999999999, "required": True},
            "CUST-GOVT-ISSUED-ID": {"type": "string", "max_length": 20, "required": False},
            "CUST-DOB-YYYY-MM-DD": {"type": "date", "format": "YYYY-MM-DD", "required": True},
            "CUST-EFT-ACCOUNT-ID": {"type": "string", "max_length": 10, "required": False},
            "CUST-PRI-CARD-HOLDER-IND": {"type": "string", "length": 1, "valid_values": ["Y", "N"], "required": True},
            "CUST-FICO-CREDIT-SCORE": {"type": "integer", "min": 0, "max": 999, "required": True},
        },
    },
    "transaction": {
        "copybook": "CVTRA05Y.cpy",
        "record_length": 350,
        "fields": {
            "TRAN-ID": {"type": "string", "length": 16, "required": True},
            "TRAN-TYPE-CD": {"type": "string", "max_length": 2, "required": True},
            "TRAN-CAT-CD": {"type": "integer", "min": 0, "max": 9999, "required": True},
            "TRAN-SOURCE": {"type": "string", "max_length": 10, "required": True},
            "TRAN-DESC": {"type": "string", "max_length": 100, "required": False},
            "TRAN-AMT": {"type": "number", "required": True},
            "TRAN-MERCHANT-ID": {"type": "integer", "min": 0, "required": True},
            "TRAN-MERCHANT-NAME": {"type": "string", "max_length": 50, "required": False},
            "TRAN-MERCHANT-CITY": {"type": "string", "max_length": 50, "required": False},
            "TRAN-MERCHANT-ZIP": {"type": "string", "max_length": 10, "required": False},
            "TRAN-CARD-NUM": {"type": "string", "length": 16, "required": True},
            "TRAN-ORIG-TS": {"type": "string", "max_length": 26, "required": True},
            "TRAN-PROC-TS": {"type": "string", "max_length": 26, "required": False},
        },
    },
    "card_xref": {
        "copybook": "CVACT03Y.cpy",
        "record_length": 50,
        "fields": {
            "XREF-CARD-NUM": {"type": "string", "length": 16, "required": True},
            "XREF-CUST-ID": {"type": "integer", "min": 1, "max": 999999999, "required": True},
            "XREF-ACCT-ID": {"type": "integer", "min": 1, "max": 99999999999, "required": True},
        },
    },
    "tran_type": {
        "copybook": "CVTRA03Y.cpy",
        "record_length": 60,
        "fields": {
            "TRAN-TYPE": {"type": "string", "max_length": 2, "required": True},
            "TRAN-TYPE-DESC": {"type": "string", "max_length": 50, "required": True},
        },
    },
    "tran_category": {
        "copybook": "CVTRA04Y.cpy",
        "record_length": 60,
        "fields": {
            "TRAN-TYPE-CD": {"type": "string", "max_length": 2, "required": True},
            "TRAN-CAT-CD": {"type": "integer", "min": 0, "max": 9999, "required": True},
            "TRAN-CAT-TYPE-DESC": {"type": "string", "max_length": 50, "required": True},
        },
    },
    "disclosure_group": {
        "copybook": "CVTRA02Y.cpy",
        "record_length": 50,
        "fields": {
            "DIS-ACCT-GROUP-ID": {"type": "string", "max_length": 10, "required": True},
            "DIS-TRAN-TYPE-CD": {"type": "string", "max_length": 2, "required": True},
            "DIS-TRAN-CAT-CD": {"type": "integer", "min": 0, "max": 9999, "required": True},
            "DIS-INT-RATE": {"type": "number", "required": True},
        },
    },
    "tran_cat_balance": {
        "copybook": "CVTRA01Y.cpy",
        "record_length": 50,
        "fields": {
            "TRANCAT-ACCT-ID": {"type": "integer", "min": 1, "max": 99999999999, "required": True},
            "TRANCAT-TYPE-CD": {"type": "string", "max_length": 2, "required": True},
            "TRANCAT-CD": {"type": "integer", "min": 0, "max": 9999, "required": True},
            "TRAN-CAT-BAL": {"type": "number", "required": True},
        },
    },
}


def validate_field(value, constraint: dict, field_name: str, record_index: int) -> list[ContractViolation]:
    """Validate a single field value against its contract constraint.

    Args:
        value: The parsed field value
        constraint: The constraint definition from the schema
        field_name: Name of the field
        record_index: Index of the record being validated

    Returns:
        List of violations (empty if valid)
    """
    violations = []

    # Required check
    if constraint.get("required", False):
        if value is None or (isinstance(value, str) and not value.strip()):
            violations.append(ContractViolation(
                record_index=record_index,
                field_name=field_name,
                constraint="required",
                expected="non-empty value",
                actual=repr(value),
            ))
            return violations  # No point checking further

    if value is None or (isinstance(value, str) and not value.strip()):
        return violations  # Optional field with no value is OK

    expected_type = constraint.get("type")

    # Type check
    if expected_type == "integer":
        if not isinstance(value, int):
            violations.append(ContractViolation(
                record_index=record_index,
                field_name=field_name,
                constraint="type",
                expected="integer",
                actual=type(value).__name__,
            ))
        else:
            if "min" in constraint and value < constraint["min"]:
                violations.append(ContractViolation(
                    record_index=record_index,
                    field_name=field_name,
                    constraint="min_value",
                    expected=str(constraint["min"]),
                    actual=str(value),
                ))
            if "max" in constraint and value > constraint["max"]:
                violations.append(ContractViolation(
                    record_index=record_index,
                    field_name=field_name,
                    constraint="max_value",
                    expected=str(constraint["max"]),
                    actual=str(value),
                ))

    elif expected_type == "number":
        if not isinstance(value, (int, float)):
            violations.append(ContractViolation(
                record_index=record_index,
                field_name=field_name,
                constraint="type",
                expected="number",
                actual=type(value).__name__,
            ))
        else:
            if "min" in constraint and value < constraint["min"]:
                violations.append(ContractViolation(
                    record_index=record_index,
                    field_name=field_name,
                    constraint="min_value",
                    expected=str(constraint["min"]),
                    actual=str(value),
                ))

    elif expected_type == "string":
        if not isinstance(value, str):
            violations.append(ContractViolation(
                record_index=record_index,
                field_name=field_name,
                constraint="type",
                expected="string",
                actual=type(value).__name__,
            ))
        else:
            if "length" in constraint and len(value.rstrip()) != constraint["length"]:
                violations.append(ContractViolation(
                    record_index=record_index,
                    field_name=field_name,
                    constraint="exact_length",
                    expected=str(constraint["length"]),
                    actual=str(len(value.rstrip())),
                    severity="error",
                ))
            if "max_length" in constraint and len(value.rstrip()) > constraint["max_length"]:
                violations.append(ContractViolation(
                    record_index=record_index,
                    field_name=field_name,
                    constraint="max_length",
                    expected=str(constraint["max_length"]),
                    actual=str(len(value.rstrip())),
                ))
            if "valid_values" in constraint:
                if value.strip() not in constraint["valid_values"]:
                    violations.append(ContractViolation(
                        record_index=record_index,
                        field_name=field_name,
                        constraint="valid_values",
                        expected=str(constraint["valid_values"]),
                        actual=repr(value.strip()),
                    ))

    elif expected_type == "date":
        if isinstance(value, str):
            stripped = value.strip()
            if stripped and len(stripped) != 10:
                violations.append(ContractViolation(
                    record_index=record_index,
                    field_name=field_name,
                    constraint="date_format",
                    expected="YYYY-MM-DD (10 chars)",
                    actual=f"length {len(stripped)}",
                    severity="warning",
                ))

    return violations


def validate_records(records: list[dict], schema_name: str) -> ContractValidationResult:
    """Validate a list of records against a named schema.

    Args:
        records: List of parsed record dictionaries
        schema_name: Key into SCHEMAS dict

    Returns:
        ContractValidationResult with all violations
    """
    schema = SCHEMAS[schema_name]
    result = ContractValidationResult(
        schema_name=schema_name,
        copybook=schema["copybook"],
        total_records=len(records),
        valid_records=0,
    )

    for i, record in enumerate(records):
        record_valid = True
        for field_name, constraint in schema["fields"].items():
            value = record.get(field_name)
            violations = validate_field(value, constraint, field_name, i)
            if violations:
                result.violations.extend(violations)
                if any(v.severity == "error" for v in violations):
                    record_valid = False
        if record_valid:
            result.valid_records += 1

    return result


def validate_contracts(golden_files_dir: str) -> dict[str, ContractValidationResult]:
    """Run contract validation on all golden files.

    Args:
        golden_files_dir: Path to directory with golden JSON files

    Returns:
        Dictionary mapping schema names to validation results
    """
    gf_dir = Path(golden_files_dir)

    file_to_schema = {
        "acctdata.golden.json": "account",
        "carddata.golden.json": "card",
        "custdata.golden.json": "customer",
        "dailytran.golden.json": "transaction",
        "cardxref.golden.json": "card_xref",
        "trantype.golden.json": "tran_type",
        "trancatg.golden.json": "tran_category",
        "discgrp.golden.json": "disclosure_group",
        "tcatbal.golden.json": "tran_cat_balance",
    }

    results = {}
    for filename, schema_name in file_to_schema.items():
        filepath = gf_dir / filename
        if filepath.exists():
            with filepath.open("r") as f:
                data = json.load(f)
            records = data.get("records", [])
            results[schema_name] = validate_records(records, schema_name)

    return results
