"""
Contract validator for CardDemo COBOL-to-Java migration.

Validates that the migrated Java application's output conforms to the
contracts defined by the original COBOL copybook record layouts. Each
contract specifies the expected fields, their types, and constraints
that must hold for the Java output to be considered compatible.
"""

import json
import os
import re
import sys
from decimal import Decimal, InvalidOperation


class ContractViolation:
    """A single contract violation."""

    def __init__(self, contract_name: str, record_index: int,
                 field: str, violation_type: str, message: str):
        self.contract_name = contract_name
        self.record_index = record_index
        self.field = field
        self.violation_type = violation_type
        self.message = message

    def __str__(self):
        return (f"[{self.violation_type}] {self.contract_name} "
                f"record {self.record_index}, field '{self.field}': "
                f"{self.message}")


# ── Contract definitions ────────────────────────────────────────────────
# Each contract is a dict of field_name -> validation spec.
# Validation spec keys:
#   required:  bool -- field must be present
#   type:      str  -- 'int', 'decimal', 'string', 'date', 'timestamp'
#   max_length: int -- max string length
#   pattern:   str  -- regex pattern the value must match
#   min_value: num  -- minimum numeric value
#   max_value: num  -- maximum numeric value
#   values:    set  -- allowed values

ACCOUNT_CONTRACT = {
    "acct_id":              {"required": True, "type": "int", "min_value": 1},
    "acct_active_status":   {"required": True, "type": "string", "max_length": 1,
                             "values": {"Y", "N"}},
    "acct_curr_bal":        {"required": True, "type": "decimal"},
    "acct_credit_limit":    {"required": True, "type": "decimal", "min_value": 0},
    "acct_cash_credit_limit": {"required": True, "type": "decimal", "min_value": 0},
    "acct_open_date":       {"required": True, "type": "date"},
    "acct_expiration_date": {"required": True, "type": "date"},
    "acct_reissue_date":    {"required": True, "type": "date"},
    "acct_curr_cyc_credit": {"required": True, "type": "decimal"},
    "acct_curr_cyc_debit":  {"required": True, "type": "decimal"},
    "acct_addr_zip":        {"required": False, "type": "string", "max_length": 10},
    "acct_group_id":        {"required": False, "type": "string", "max_length": 10},
}

CARD_CONTRACT = {
    "card_num":             {"required": True, "type": "string", "max_length": 16},
    "card_acct_id":         {"required": True, "type": "int", "min_value": 1},
    "card_cvv_cd":          {"required": True, "type": "int",
                             "min_value": 0, "max_value": 999},
    "card_embossed_name":   {"required": True, "type": "string", "max_length": 50},
    "card_expiration_date": {"required": True, "type": "date"},
    "card_active_status":   {"required": True, "type": "string", "max_length": 1,
                             "values": {"Y", "N"}},
}

CARD_XREF_CONTRACT = {
    "xref_card_num":  {"required": True, "type": "string", "max_length": 16},
    "xref_cust_id":   {"required": True, "type": "int", "min_value": 1},
    "xref_acct_id":   {"required": True, "type": "int", "min_value": 1},
}

CUSTOMER_CONTRACT = {
    "cust_id":                  {"required": True, "type": "int", "min_value": 1},
    "cust_first_name":          {"required": True, "type": "string", "max_length": 25},
    "cust_middle_name":         {"required": False, "type": "string", "max_length": 25},
    "cust_last_name":           {"required": True, "type": "string", "max_length": 25},
    "cust_addr_line_1":         {"required": False, "type": "string", "max_length": 50},
    "cust_addr_line_2":         {"required": False, "type": "string", "max_length": 50},
    "cust_addr_line_3":         {"required": False, "type": "string", "max_length": 50},
    "cust_addr_state_cd":       {"required": False, "type": "string", "max_length": 2},
    "cust_addr_country_cd":     {"required": False, "type": "string", "max_length": 3},
    "cust_addr_zip":            {"required": False, "type": "string", "max_length": 10},
    "cust_phone_num_1":         {"required": False, "type": "string", "max_length": 15},
    "cust_phone_num_2":         {"required": False, "type": "string", "max_length": 15},
    "cust_ssn":                 {"required": True, "type": "int"},
    "cust_govt_issued_id":      {"required": False, "type": "string", "max_length": 20},
    "cust_dob_yyyy_mm_dd":      {"required": True, "type": "date"},
    "cust_eft_account_id":      {"required": False, "type": "string", "max_length": 10},
    "cust_pri_card_holder_ind": {"required": True, "type": "string", "max_length": 1,
                                 "values": {"Y", "N"}},
    "cust_fico_credit_score":   {"required": True, "type": "int",
                                 "min_value": 0, "max_value": 999},
}

TRANSACTION_CONTRACT = {
    "tran_id":            {"required": True, "type": "string", "max_length": 16},
    "tran_type_cd":       {"required": True, "type": "string", "max_length": 2},
    "tran_cat_cd":        {"required": True, "type": "int"},
    "tran_source":        {"required": True, "type": "string", "max_length": 10},
    "tran_desc":          {"required": True, "type": "string", "max_length": 100},
    "tran_amt":           {"required": True, "type": "decimal"},
    "tran_merchant_id":   {"required": True, "type": "int"},
    "tran_merchant_name": {"required": True, "type": "string", "max_length": 50},
    "tran_merchant_city": {"required": True, "type": "string", "max_length": 50},
    "tran_merchant_zip":  {"required": False, "type": "string", "max_length": 10},
    "tran_card_num":      {"required": True, "type": "string", "max_length": 16},
    "tran_orig_ts":       {"required": True, "type": "timestamp"},
    "tran_proc_ts":       {"required": False, "type": "timestamp"},
}

TRAN_TYPE_CONTRACT = {
    "tran_type":      {"required": True, "type": "string", "max_length": 2},
    "tran_type_desc": {"required": True, "type": "string", "max_length": 50},
}

TRAN_CATG_CONTRACT = {
    "tran_type_cd":       {"required": True, "type": "string", "max_length": 2},
    "tran_cat_cd":        {"required": True, "type": "int"},
    "tran_cat_type_desc": {"required": True, "type": "string", "max_length": 50},
}

TRAN_CAT_BAL_CONTRACT = {
    "trancat_acct_id": {"required": True, "type": "int", "min_value": 1},
    "trancat_type_cd": {"required": True, "type": "string", "max_length": 2},
    "trancat_cd":      {"required": True, "type": "int"},
    "tran_cat_bal":    {"required": True, "type": "decimal"},
}

DISC_GROUP_CONTRACT = {
    "dis_acct_group_id": {"required": True, "type": "string", "max_length": 10},
    "dis_tran_type_cd":  {"required": True, "type": "string", "max_length": 2},
    "dis_tran_cat_cd":   {"required": True, "type": "int"},
    "dis_int_rate":      {"required": True, "type": "decimal"},
}


# Mapping from golden file name to (record_key, contract)
CONTRACTS = {
    "acctdata.json":  ("accounts", ACCOUNT_CONTRACT),
    "carddata.json":  ("cards", CARD_CONTRACT),
    "cardxref.json":  ("card_xrefs", CARD_XREF_CONTRACT),
    "custdata.json":  ("customers", CUSTOMER_CONTRACT),
    "dailytran.json": ("daily_transactions", TRANSACTION_CONTRACT),
    "discgrp.json":   ("disclosure_groups", DISC_GROUP_CONTRACT),
    "tcatbal.json":   ("tran_category_balances", TRAN_CAT_BAL_CONTRACT),
    "trancatg.json":  ("transaction_categories", TRAN_CATG_CONTRACT),
    "trantype.json":  ("transaction_types", TRAN_TYPE_CONTRACT),
}


DATE_PATTERN = re.compile(r"^\d{4}-\d{2}-\d{2}$")
TIMESTAMP_PATTERN = re.compile(r"^\d{4}-\d{2}-\d{2}")


def validate_field(value: object, spec: dict) -> str:
    """Validate a single field value against its contract spec.

    Returns an error message if invalid, or empty string if valid.
    """
    if value is None or (isinstance(value, str) and value.strip() == ""):
        if spec.get("required", False):
            return "required field is missing or empty"
        return ""

    field_type = spec.get("type", "string")

    if field_type == "int":
        try:
            int_val = int(value)
        except (ValueError, TypeError):
            return f"expected integer, got {type(value).__name__}: {value!r}"
        if "min_value" in spec and int_val < spec["min_value"]:
            return f"value {int_val} below minimum {spec['min_value']}"
        if "max_value" in spec and int_val > spec["max_value"]:
            return f"value {int_val} above maximum {spec['max_value']}"

    elif field_type == "decimal":
        try:
            dec_val = Decimal(str(value))
        except (InvalidOperation, ValueError):
            return f"expected decimal, got: {value!r}"
        if "min_value" in spec and dec_val < Decimal(str(spec["min_value"])):
            return f"value {dec_val} below minimum {spec['min_value']}"
        if "max_value" in spec and dec_val > Decimal(str(spec["max_value"])):
            return f"value {dec_val} above maximum {spec['max_value']}"

    elif field_type == "string":
        str_val = str(value)
        if "max_length" in spec and len(str_val) > spec["max_length"]:
            return f"string length {len(str_val)} exceeds max {spec['max_length']}"

    elif field_type == "date":
        if not DATE_PATTERN.match(str(value)):
            return f"expected YYYY-MM-DD date, got: {value!r}"

    elif field_type == "timestamp":
        if not TIMESTAMP_PATTERN.match(str(value)):
            return f"expected timestamp, got: {value!r}"

    if "values" in spec:
        if str(value) not in spec["values"]:
            return f"value {value!r} not in allowed set {spec['values']}"

    return ""


def validate_contract(golden_path: str, record_key: str,
                      contract: dict) -> list:
    """Validate all records in a golden file against a contract.

    Returns list of ContractViolation objects.
    """
    violations = []

    with open(golden_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    records = data.get(record_key, [])
    contract_name = os.path.basename(golden_path)

    for i, record in enumerate(records):
        # Check for required fields that are missing entirely
        for field, spec in contract.items():
            value = record.get(field)
            error = validate_field(value, spec)
            if error:
                violations.append(ContractViolation(
                    contract_name, i, field, "FIELD_VIOLATION", error
                ))

        # Check for unexpected fields (not in contract)
        for field in record:
            if field not in contract:
                violations.append(ContractViolation(
                    contract_name, i, field, "EXTRA_FIELD",
                    f"field not defined in contract"
                ))

    return violations


def validate_all_contracts(golden_dir: str) -> dict:
    """Validate all golden files against their contracts.

    Returns a dict with per-file results and overall summary.
    """
    results = {}
    total_violations = 0
    total_files = 0

    for filename, (record_key, contract) in sorted(CONTRACTS.items()):
        golden_path = os.path.join(golden_dir, filename)
        if not os.path.isfile(golden_path):
            results[filename] = {
                "status": "SKIP",
                "reason": "file not found",
                "violations": [],
            }
            continue

        total_files += 1
        violations = validate_contract(golden_path, record_key, contract)
        status = "PASS" if not violations else "FAIL"
        results[filename] = {
            "status": status,
            "violation_count": len(violations),
            "violations": violations,
        }
        total_violations += len(violations)

    return {
        "files_checked": total_files,
        "total_violations": total_violations,
        "file_results": results,
    }


def main():
    """Run contract validation and print results."""
    repo_root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    golden_dir = os.path.join(repo_root, "golden-files")

    if len(sys.argv) > 1:
        golden_dir = sys.argv[1]

    print("Contract Validator")
    print("=" * 60)
    print(f"Golden files: {golden_dir}")
    print()

    if not os.path.isdir(golden_dir):
        print(f"ERROR: Golden files directory not found: {golden_dir}",
              file=sys.stderr)
        sys.exit(1)

    summary = validate_all_contracts(golden_dir)

    for filename, result in sorted(summary["file_results"].items()):
        status = result["status"]
        if status == "SKIP":
            print(f"  [SKIP] {filename}: {result['reason']}")
        elif status == "PASS":
            print(f"  [PASS] {filename}")
        else:
            v_count = result["violation_count"]
            print(f"  [FAIL] {filename}: {v_count} violations")
            for v in result["violations"][:10]:
                print(f"         {v}")
            if v_count > 10:
                print(f"         ... and {v_count - 10} more")

    print()
    print(f"Files checked:    {summary['files_checked']}")
    print(f"Total violations: {summary['total_violations']}")

    sys.exit(0 if summary["total_violations"] == 0 else 1)


if __name__ == "__main__":
    main()
