"""
Field-level comparison engine for CardDemo migration testing.

Compares records from golden files against modern Java system output,
producing structured diff reports showing exactly which fields diverge.

Usage:
    from comparator import compare_records, compare_datasets

    diffs = compare_records(golden_record, modern_record, tolerance_rules)
    report = compare_datasets(golden_data, modern_data, key_field, tolerance_rules)
"""

import json
import sys
from decimal import Decimal
from typing import Optional


# Default tolerance rules per field type
DEFAULT_TOLERANCES = {
    "exact_string": {
        "type": "string",
        "comparison": "exact",
        "trim": True,
    },
    "exact_numeric": {
        "type": "numeric",
        "comparison": "exact",
    },
    "decimal_2dp": {
        "type": "decimal",
        "comparison": "tolerance",
        "tolerance": 0.005,  # Half-cent tolerance for display rounding
    },
    "decimal_exact": {
        "type": "decimal",
        "comparison": "exact",
    },
    "date": {
        "type": "date",
        "comparison": "exact",
    },
    "timestamp": {
        "type": "timestamp",
        "comparison": "tolerance",
        "tolerance_seconds": 1,
    },
}

# Field-to-tolerance mapping for each entity
FIELD_TOLERANCES = {
    # Account fields
    "ACCT_ID": "exact_numeric",
    "ACCT_ACTIVE_STATUS": "exact_string",
    "ACCT_CURR_BAL": "decimal_exact",
    "ACCT_CREDIT_LIMIT": "decimal_exact",
    "ACCT_CASH_CREDIT_LIMIT": "decimal_exact",
    "ACCT_OPEN_DATE": "date",
    "ACCT_EXPIRAION_DATE": "date",
    "ACCT_REISSUE_DATE": "date",
    "ACCT_CURR_CYC_CREDIT": "decimal_exact",
    "ACCT_CURR_CYC_DEBIT": "decimal_exact",
    "ACCT_ADDR_ZIP": "exact_string",
    "ACCT_GROUP_ID": "exact_string",
    # Card fields
    "CARD_NUM": "exact_string",
    "CARD_ACCT_ID": "exact_numeric",
    "CARD_CVV_CD": "exact_numeric",
    "CARD_EMBOSSED_NAME": "exact_string",
    "CARD_EXPIRAION_DATE": "date",
    "CARD_ACTIVE_STATUS": "exact_string",
    # Daily transaction fields
    "DALYTRAN_AMT": "decimal_exact",
    "DALYTRAN_CAT_CD": "exact_numeric",
    "DALYTRAN_MERCHANT_ID": "exact_numeric",
    "DALYTRAN_ORIG_TS": "timestamp",
    "DALYTRAN_PROC_TS": "timestamp",
    # Category balance fields
    "TRAN_CAT_BAL": "decimal_exact",
    "TRANCAT_ACCT_ID": "exact_numeric",
    "TRANCAT_CD": "exact_numeric",
    # Disclosure group fields
    "DIS_INT_RATE": "decimal_exact",
    "DIS_TRAN_CAT_CD": "exact_numeric",
}


def compare_field(field_name: str, golden_value, modern_value,
                  tolerance_key: Optional[str] = None) -> Optional[dict]:
    """Compare a single field between golden and modern values.

    Returns None if values match, or a diff dict if they diverge.
    """
    if tolerance_key is None:
        tolerance_key = FIELD_TOLERANCES.get(field_name, "exact_string")

    rule = DEFAULT_TOLERANCES.get(tolerance_key, DEFAULT_TOLERANCES["exact_string"])

    if rule["type"] == "string":
        g = str(golden_value).strip() if golden_value is not None else ""
        m = str(modern_value).strip() if modern_value is not None else ""
        if rule.get("trim", True):
            g = g.strip()
            m = m.strip()
        if g == m:
            return None
        return {
            "field": field_name,
            "golden": g,
            "modern": m,
            "rule": tolerance_key,
            "verdict": "MISMATCH",
        }

    elif rule["type"] == "numeric":
        g = int(golden_value) if golden_value is not None else 0
        m = int(modern_value) if modern_value is not None else 0
        if g == m:
            return None
        return {
            "field": field_name,
            "golden": g,
            "modern": m,
            "rule": tolerance_key,
            "verdict": "MISMATCH",
        }

    elif rule["type"] == "decimal":
        g = Decimal(str(golden_value)) if golden_value is not None else Decimal("0")
        m = Decimal(str(modern_value)) if modern_value is not None else Decimal("0")

        if rule["comparison"] == "exact":
            if g == m:
                return None
        else:
            tolerance = Decimal(str(rule.get("tolerance", 0)))
            if abs(g - m) <= tolerance:
                return None

        return {
            "field": field_name,
            "golden": float(g),
            "modern": float(m),
            "difference": float(abs(g - m)),
            "rule": tolerance_key,
            "verdict": "MISMATCH",
        }

    elif rule["type"] in ("date", "timestamp"):
        g = str(golden_value).strip() if golden_value is not None else ""
        m = str(modern_value).strip() if modern_value is not None else ""
        if g == m:
            return None
        return {
            "field": field_name,
            "golden": g,
            "modern": m,
            "rule": tolerance_key,
            "verdict": "MISMATCH",
        }

    return None


def compare_records(golden: dict, modern: dict,
                    skip_fields: Optional[set] = None) -> list:
    """Compare two records field-by-field.

    Args:
        golden: Dict from golden file
        modern: Dict from modern system
        skip_fields: Fields to skip (e.g., _line_number, internal IDs)

    Returns:
        List of diff dicts for mismatched fields (empty = perfect match)
    """
    if skip_fields is None:
        skip_fields = {"_line_number"}

    diffs = []

    all_fields = set(golden.keys()) | set(modern.keys())
    for field in sorted(all_fields):
        if field in skip_fields:
            continue

        g_val = golden.get(field)
        m_val = modern.get(field)

        if field not in golden:
            diffs.append({
                "field": field,
                "golden": "<MISSING>",
                "modern": m_val,
                "verdict": "FIELD_ADDED",
            })
        elif field not in modern:
            diffs.append({
                "field": field,
                "golden": g_val,
                "modern": "<MISSING>",
                "verdict": "FIELD_MISSING",
            })
        else:
            diff = compare_field(field, g_val, m_val)
            if diff is not None:
                diffs.append(diff)

    return diffs


def compare_datasets(golden_data: dict, modern_records: list,
                     key_field: str,
                     skip_fields: Optional[set] = None) -> dict:
    """Compare an entire golden dataset against modern system output.

    Args:
        golden_data: Parsed golden file (from parser.py output)
        modern_records: List of dicts from modern system
        key_field: Field name to use as record key for matching
        skip_fields: Fields to skip in comparison

    Returns:
        Comparison report dict with summary and per-record details
    """
    golden_records = golden_data.get("records", [])
    golden_by_key = {}
    for rec in golden_records:
        key = rec.get(key_field)
        if key is not None:
            golden_by_key[key] = rec

    modern_by_key = {}
    for rec in modern_records:
        key = rec.get(key_field)
        if key is not None:
            modern_by_key[key] = rec

    golden_keys = set(golden_by_key.keys())
    modern_keys = set(modern_by_key.keys())

    missing_in_modern = golden_keys - modern_keys
    extra_in_modern = modern_keys - golden_keys
    common_keys = golden_keys & modern_keys

    record_diffs = []
    match_count = 0
    mismatch_count = 0

    for key in sorted(common_keys, key=str):
        diffs = compare_records(golden_by_key[key], modern_by_key[key], skip_fields)
        if diffs:
            mismatch_count += 1
            record_diffs.append({
                "key": key,
                "key_field": key_field,
                "diffs": diffs,
            })
        else:
            match_count += 1

    return {
        "summary": {
            "golden_count": len(golden_records),
            "modern_count": len(modern_records),
            "matched": match_count,
            "mismatched": mismatch_count,
            "missing_in_modern": len(missing_in_modern),
            "extra_in_modern": len(extra_in_modern),
            "pass": mismatch_count == 0 and len(missing_in_modern) == 0,
        },
        "missing_keys": sorted(missing_in_modern, key=str),
        "extra_keys": sorted(extra_in_modern, key=str),
        "record_diffs": record_diffs,
    }


def main():
    """CLI: python comparator.py <golden.json> <modern.json> <key_field>"""
    if len(sys.argv) < 4:
        print("Usage: python comparator.py <golden.json> <modern.json> <key_field>")
        sys.exit(1)

    golden_path = sys.argv[1]
    modern_path = sys.argv[2]
    key_field = sys.argv[3]

    with open(golden_path) as f:
        golden_data = json.load(f)

    with open(modern_path) as f:
        modern_data = json.load(f)

    modern_records = modern_data.get("records", modern_data)
    if isinstance(modern_records, dict):
        modern_records = [modern_records]

    report = compare_datasets(golden_data, modern_records, key_field)

    print(json.dumps(report, indent=2, default=str))

    if report["summary"]["pass"]:
        print(f"\nPASS: All {report['summary']['matched']} records match.")
    else:
        print(f"\nFAIL: {report['summary']['mismatched']} mismatches, "
              f"{report['summary']['missing_in_modern']} missing.")
        sys.exit(1)


if __name__ == "__main__":
    main()
