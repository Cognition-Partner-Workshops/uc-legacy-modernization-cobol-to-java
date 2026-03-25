#!/usr/bin/env python3
"""
Validate Java DTOs and entities against COBOL copybook contracts.

Parses copybook field definitions and checks that a corresponding Java class
or JSON schema contains all required fields with compatible types and sizes.

Usage:
    python3 contract_validator.py --copybook PATH --schema PATH [--output PATH]
"""

import json
import os
import sys
import argparse

sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from copybook_parser import parse_copybook, fields_to_dict


# Mapping from COBOL PIC types to compatible Java types
COBOL_TO_JAVA_TYPES = {
    "alpha": {"String", "CharSequence", "char[]"},
    "numeric": {"int", "long", "Integer", "Long", "BigDecimal", "BigInteger", "String"},
    "signed_decimal": {"BigDecimal", "double", "Double", "float", "Float", "String"},
}


def extract_contract(copybook_path: str) -> dict:
    """Extract the data contract from a COBOL copybook.

    Returns a dict with field specs suitable for validation.
    """
    fields = parse_copybook(copybook_path)
    contract_fields = []
    total_length = 0

    for f in fields:
        total_length = max(total_length, f.offset + f.length)
        if f.name == "FILLER":
            continue
        contract_fields.append(
            {
                "name": f.name,
                "pic": f.pic,
                "offset": f.offset,
                "length": f.length,
                "type": f.field_type,
                "decimal_places": f.decimal_places,
                "compatible_java_types": sorted(
                    COBOL_TO_JAVA_TYPES.get(f.field_type, {"String"})
                ),
            }
        )

    return {
        "copybook": os.path.basename(copybook_path),
        "total_record_length": total_length,
        "field_count": len(contract_fields),
        "fields": contract_fields,
    }


def validate_schema(contract: dict, schema: dict) -> dict:
    """Validate a JSON schema (representing Java DTO) against a copybook contract.

    The schema should be a dict with:
        - "fields": list of {"name": str, "type": str, "max_length": int}
        - "record_length": int (optional)

    Returns a validation report.
    """
    contract_fields = {f["name"]: f for f in contract["fields"]}
    schema_fields = {f["name"]: f for f in schema.get("fields", [])}

    missing_fields = []
    type_mismatches = []
    size_violations = []
    matched = 0

    for name, cf in contract_fields.items():
        if name not in schema_fields:
            missing_fields.append(
                {
                    "field": name,
                    "expected_type": cf["type"],
                    "expected_length": cf["length"],
                }
            )
            continue

        sf = schema_fields[name]
        matched += 1

        # Check type compatibility
        java_type = sf.get("type", "")
        if java_type not in cf["compatible_java_types"]:
            type_mismatches.append(
                {
                    "field": name,
                    "cobol_type": cf["type"],
                    "java_type": java_type,
                    "compatible_types": cf["compatible_java_types"],
                }
            )

        # Check size constraint
        max_length = sf.get("max_length")
        if max_length is not None and max_length != cf["length"]:
            size_violations.append(
                {
                    "field": name,
                    "expected_length": cf["length"],
                    "actual_max_length": max_length,
                }
            )

    # Check record length
    record_length_ok = True
    if "record_length" in schema:
        record_length_ok = schema["record_length"] == contract["total_record_length"]

    extra_fields = [
        name for name in schema_fields if name not in contract_fields
    ]

    return {
        "copybook": contract["copybook"],
        "contract_field_count": len(contract_fields),
        "schema_field_count": len(schema_fields),
        "matched": matched,
        "missing_fields": missing_fields,
        "type_mismatches": type_mismatches,
        "size_violations": size_violations,
        "extra_fields": extra_fields,
        "record_length_match": record_length_ok,
        "expected_record_length": contract["total_record_length"],
        "actual_record_length": schema.get("record_length"),
        "pass": (
            len(missing_fields) == 0
            and len(type_mismatches) == 0
            and len(size_violations) == 0
            and record_length_ok
        ),
    }


def main():
    parser = argparse.ArgumentParser(
        description="Validate Java schemas against COBOL copybook contracts."
    )
    parser.add_argument(
        "--copybook", required=True, help="Path to COBOL copybook file."
    )
    parser.add_argument(
        "--schema", required=True, help="Path to Java schema JSON file."
    )
    parser.add_argument(
        "--output", default=None, help="Path to write report JSON."
    )
    args = parser.parse_args()

    contract = extract_contract(args.copybook)

    with open(args.schema, "r") as f:
        schema = json.load(f)

    report = validate_schema(contract, schema)
    report_json = json.dumps(report, indent=2)

    if args.output:
        with open(args.output, "w") as f:
            f.write(report_json)
        print(f"Report written to {args.output}")
    else:
        print(report_json)

    status = "PASS" if report["pass"] else "FAIL"
    print(f"\nResult: {status}", file=sys.stderr)
    print(f"  Matched:         {report['matched']}", file=sys.stderr)
    print(f"  Missing fields:  {len(report['missing_fields'])}", file=sys.stderr)
    print(f"  Type mismatches: {len(report['type_mismatches'])}", file=sys.stderr)
    print(f"  Size violations: {len(report['size_violations'])}", file=sys.stderr)

    sys.exit(0 if report["pass"] else 1)


if __name__ == "__main__":
    main()
