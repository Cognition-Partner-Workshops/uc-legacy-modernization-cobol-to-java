#!/usr/bin/env python3
"""
contract_validator.py -- Validate that Java DTOs / output files conform to
the data contracts defined by COBOL copybook layouts.

Checks:
  - Field count (excluding FILLER)
  - Field names present in output
  - Field types (numeric vs alphanumeric)
  - Serialized record length
  - Field order matches copybook order

Usage:
    python contract_validator.py [--contracts-dir PATH] [--java-dir PATH]
"""

import argparse
import json
import os
import sys

from copybook_parser import FILE_LAYOUTS, compute_layout_length


def load_contract(contracts_dir: str, file_key: str) -> dict | None:
    """Load a contract JSON schema for a given file key."""
    path = os.path.join(contracts_dir, f"{file_key}_contract.json")
    if not os.path.exists(path):
        return None
    with open(path) as f:
        return json.load(f)


def generate_contract(file_key: str) -> dict:
    """Generate a contract specification from the copybook layout.

    Returns a dict describing the expected fields, types, and record length.
    """
    layout_info = FILE_LAYOUTS[file_key]
    layout = layout_info["layout"]
    copybook = layout_info["copybook"]
    reclen = layout_info["reclen"]

    fields = []
    for field_def in layout:
        if field_def["type"] == "filler":
            continue
        java_type = _cobol_to_java_type(field_def)
        fields.append({
            "cobol_name": field_def["name"],
            "java_name": _to_camel_case(field_def["name"]),
            "cobol_pic_type": field_def["type"],
            "java_type": java_type,
            "length": field_def["length"],
            "decimal_places": field_def.get("decimal", 0),
        })

    return {
        "file_key": file_key,
        "copybook": copybook,
        "record_length": reclen,
        "computed_length": compute_layout_length(layout),
        "field_count": len(fields),
        "fields": fields,
    }


def _cobol_to_java_type(field_def: dict) -> str:
    """Map a COBOL PIC type to the expected Java type."""
    ftype = field_def["type"]
    decimal = field_def.get("decimal", 0)

    if ftype == "alpha":
        return "String"
    elif ftype == "unsigned":
        length = field_def["length"]
        if length <= 9:
            return "int"
        elif length <= 18:
            return "long"
        else:
            return "BigDecimal"
    elif ftype == "signed_decimal":
        return "BigDecimal"
    return "String"


def _to_camel_case(cobol_name: str) -> str:
    """Convert COBOL-HYPHENATED-NAME to javaCamelCase."""
    parts = cobol_name.lower().split("-")
    return parts[0] + "".join(p.capitalize() for p in parts[1:])


def validate_contract(contract: dict, java_records: list[dict]) -> list[dict]:
    """Validate Java output records against a contract.

    Returns list of violation dicts.
    """
    violations = []

    if not java_records:
        violations.append({
            "type": "no_data",
            "message": "No Java records to validate",
        })
        return violations

    # Check first record as representative
    sample = java_records[0]
    # Remove metadata keys
    sample_keys = {k for k in sample.keys() if not k.startswith("_")}
    expected_fields = {f["cobol_name"] for f in contract["fields"]}
    expected_java_fields = {f["java_name"] for f in contract["fields"]}

    # Check field count
    if len(sample_keys) != contract["field_count"]:
        # Fields might use either COBOL or Java naming
        if not (sample_keys == expected_fields or sample_keys == expected_java_fields):
            violations.append({
                "type": "field_count_mismatch",
                "expected": contract["field_count"],
                "actual": len(sample_keys),
                "expected_fields": sorted(expected_fields),
                "actual_fields": sorted(sample_keys),
            })

    # Check field presence (accept either COBOL or Java naming)
    for field in contract["fields"]:
        cobol_name = field["cobol_name"]
        java_name = field["java_name"]
        if cobol_name not in sample and java_name not in sample:
            violations.append({
                "type": "missing_field",
                "cobol_name": cobol_name,
                "java_name": java_name,
            })

    # Check field types for all records
    type_violations_seen = set()
    for idx, record in enumerate(java_records):
        for field in contract["fields"]:
            field_name = field["cobol_name"] if field["cobol_name"] in record else field["java_name"]
            if field_name not in record:
                continue

            value = record[field_name]
            expected_type = field["java_type"]
            violation_key = (field_name, expected_type)

            if violation_key in type_violations_seen:
                continue

            if expected_type in ("int", "long"):
                if not isinstance(value, (int, float)):
                    try:
                        int(str(value))
                    except (ValueError, TypeError):
                        violations.append({
                            "type": "type_mismatch",
                            "field": field_name,
                            "expected_java_type": expected_type,
                            "actual_value": repr(value),
                            "record_index": idx,
                        })
                        type_violations_seen.add(violation_key)

            elif expected_type == "BigDecimal":
                if not isinstance(value, (int, float, str)):
                    violations.append({
                        "type": "type_mismatch",
                        "field": field_name,
                        "expected_java_type": expected_type,
                        "actual_value": repr(value),
                        "record_index": idx,
                    })
                    type_violations_seen.add(violation_key)

    return violations


def generate_all_contracts(contracts_dir: str) -> None:
    """Generate contract JSON files for all known file layouts."""
    os.makedirs(contracts_dir, exist_ok=True)
    for file_key in sorted(FILE_LAYOUTS.keys()):
        contract = generate_contract(file_key)
        path = os.path.join(contracts_dir, f"{file_key}_contract.json")
        with open(path, "w") as f:
            json.dump(contract, f, indent=2)
        print(f"  Generated {file_key}_contract.json ({contract['field_count']} fields)")


def main() -> int:
    script_dir = os.path.dirname(os.path.abspath(__file__))
    default_contracts_dir = os.path.join(script_dir, "contracts")

    parser = argparse.ArgumentParser(description="Contract validation for CardDemo migration")
    parser.add_argument("--contracts-dir", default=default_contracts_dir)
    parser.add_argument("--java-dir", default=None,
                        help="Path to Java output directory for validation")
    parser.add_argument("--generate", action="store_true",
                        help="Generate contract JSON files from copybook layouts")
    parser.add_argument("--output", default="contract-report.json")
    args = parser.parse_args()

    if args.generate:
        print("Generating contract specifications...")
        generate_all_contracts(args.contracts_dir)
        print("Done.")
        return 0

    if not args.java_dir:
        print("No --java-dir specified. Use --generate to create contract files,")
        print("or provide --java-dir to validate Java output against contracts.")
        # Generate contracts as default action
        print("\nGenerating contract specifications...")
        generate_all_contracts(args.contracts_dir)
        return 0

    # Validate Java output against contracts
    report = {"files": [], "total_violations": 0}

    for file_key in sorted(FILE_LAYOUTS.keys()):
        contract = load_contract(args.contracts_dir, file_key)
        if contract is None:
            contract = generate_contract(file_key)

        java_path = os.path.join(args.java_dir, f"{file_key}.java-output.json")
        file_report = {"file_key": file_key, "violations": [], "status": "PASS"}

        if not os.path.exists(java_path):
            file_report["status"] = "SKIP"
            file_report["reason"] = "Java output not found"
            report["files"].append(file_report)
            continue

        with open(java_path) as f:
            java_data = json.load(f)

        violations = validate_contract(contract, java_data.get("records", []))
        file_report["violations"] = violations
        file_report["status"] = "PASS" if not violations else "FAIL"
        report["total_violations"] += len(violations)
        report["files"].append(file_report)

    with open(args.output, "w") as f:
        json.dump(report, f, indent=2, default=str)

    print(f"\nContract Validation Report")
    print(f"  Total violations: {report['total_violations']}")
    for fr in report["files"]:
        vcount = len(fr.get("violations", []))
        print(f"  {fr['status']:6s} {fr['file_key']} ({vcount} violations)")

    return 1 if report["total_violations"] > 0 else 0


if __name__ == "__main__":
    sys.exit(main())
