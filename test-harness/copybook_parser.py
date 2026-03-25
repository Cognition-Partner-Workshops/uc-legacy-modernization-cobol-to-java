"""
Generic COBOL copybook-aware fixed-width record parser.

Parses ASCII flat files produced by the CardDemo mainframe application
using field layout definitions derived from COBOL copybook (.cpy) files.

Each layout is a list of (field_name, length, field_type) tuples where
field_type is one of:
    'text'       -- plain alphanumeric, stripped of trailing spaces
    'numeric'    -- unsigned integer stored as PIC 9(n)
    'signed'     -- signed numeric with trailing overpunch (PIC S9(n)V99)
    'date'       -- date string (YYYY-MM-DD or similar)
    'timestamp'  -- timestamp string (YYYY-MM-DD HH:MM:SS.ffffff)
    'filler'     -- padding bytes, ignored in output
"""

from decimal import Decimal

# ── EBCDIC-style overpunch sign decoding ────────────────────────────────
# In zoned-decimal representation the sign is encoded in the last byte.
# Positive digits 0-9 map to '{ABCDEFGHI' and negative to '}JKLMNOPQR'.
POSITIVE_OVERPUNCH = {
    "{": "0", "A": "1", "B": "2", "C": "3", "D": "4",
    "E": "5", "F": "6", "G": "7", "H": "8", "I": "9",
}
NEGATIVE_OVERPUNCH = {
    "}": "0", "J": "1", "K": "2", "L": "3", "M": "4",
    "N": "5", "O": "6", "P": "7", "Q": "8", "R": "9",
}


def decode_overpunch(raw: str, decimal_places: int = 2) -> str:
    """Decode a zoned-decimal field with trailing overpunch sign.

    Returns a string representation of the signed decimal value.
    For example: '00000020200{' with 2 decimal places -> '20200.00'
    and '0000009190}' with 2 decimal places -> '-9190.00' (negative).
    """
    if not raw:
        return "0"

    last_char = raw[-1]
    digits = raw[:-1]

    if last_char in POSITIVE_OVERPUNCH:
        sign = ""
        digits += POSITIVE_OVERPUNCH[last_char]
    elif last_char in NEGATIVE_OVERPUNCH:
        sign = "-"
        digits += NEGATIVE_OVERPUNCH[last_char]
    elif last_char.isdigit():
        sign = ""
        digits += last_char
    else:
        sign = ""
        digits += "0"

    # Remove leading zeros but keep at least one digit
    digits = digits.lstrip("0") or "0"

    if decimal_places > 0:
        # Pad with leading zeros if needed
        digits = digits.zfill(decimal_places + 1)
        integer_part = digits[:-decimal_places]
        fractional_part = digits[-decimal_places:]
        return f"{sign}{integer_part}.{fractional_part}"

    return f"{sign}{digits}"


def parse_field(raw: str, field_type: str) -> object:
    """Parse a single field value based on its COBOL type."""
    if field_type == "filler":
        return None

    if field_type == "text":
        return raw.rstrip()

    if field_type == "numeric":
        stripped = raw.lstrip("0") or "0"
        try:
            return int(stripped)
        except ValueError:
            return raw.rstrip()

    if field_type in ("signed", "signed_4v2"):
        return decode_overpunch(raw, decimal_places=2)

    if field_type == "date":
        return raw.rstrip()

    if field_type == "timestamp":
        return raw.rstrip()

    return raw.rstrip()


def parse_record(line: str, layout: list) -> dict:
    """Parse a single fixed-width record using the given layout.

    Args:
        line:   The raw fixed-width text line.
        layout: List of (field_name, length, field_type) tuples.

    Returns:
        Dictionary of field_name -> parsed_value.
    """
    record = {}
    offset = 0
    for field_name, length, field_type in layout:
        raw = line[offset:offset + length]
        offset += length
        if field_type == "filler":
            continue
        record[field_name] = parse_field(raw, field_type)
    return record


def parse_file(filepath: str, layout: list, record_label: str = "records") -> dict:
    """Parse an entire fixed-width data file.

    Args:
        filepath:     Path to the ASCII data file.
        layout:       Field layout definition list.
        record_label: Label for the records array in the output.

    Returns:
        Dictionary with '_metadata' and the records array.
    """
    import os

    field_map = [
        {"name": name, "length": length, "type": ftype}
        for name, length, ftype in layout
    ]
    record_length = sum(length for _, length, _ in layout)

    records = []
    with open(filepath, "r", encoding="utf-8") as fh:
        for line_num, line in enumerate(fh, start=1):
            stripped = line.rstrip("\n\r")
            if not stripped:
                continue
            records.append(parse_record(stripped, layout))

    return {
        "_metadata": {
            "source_file": os.path.basename(filepath),
            "copybook_layout": field_map,
            "record_length": record_length,
            "record_count": len(records),
        },
        record_label: records,
    }


# ── Layout definitions derived from COBOL copybooks ────────────────────

# CVACT01Y.cpy -- Account record (RECLN 300)
ACCOUNT_LAYOUT = [
    ("acct_id",              11, "numeric"),
    ("acct_active_status",    1, "text"),
    ("acct_curr_bal",        12, "signed"),      # S9(10)V99 -> 10+2=12 bytes
    ("acct_credit_limit",    12, "signed"),      # S9(10)V99
    ("acct_cash_credit_limit", 12, "signed"),    # S9(10)V99
    ("acct_open_date",       10, "date"),
    ("acct_expiration_date", 10, "date"),
    ("acct_reissue_date",    10, "date"),
    ("acct_curr_cyc_credit", 12, "signed"),      # S9(10)V99
    ("acct_curr_cyc_debit",  12, "signed"),      # S9(10)V99
    ("acct_addr_zip",        10, "text"),
    ("acct_group_id",        10, "text"),
    ("filler",              178, "filler"),
]

# CVACT02Y.cpy -- Card record (RECLN 150)
CARD_LAYOUT = [
    ("card_num",             16, "text"),
    ("card_acct_id",         11, "numeric"),
    ("card_cvv_cd",           3, "numeric"),
    ("card_embossed_name",   50, "text"),
    ("card_expiration_date", 10, "date"),
    ("card_active_status",    1, "text"),
    ("filler",               59, "filler"),
]

# CVACT03Y.cpy -- Card cross-reference (RECLN 50)
CARD_XREF_LAYOUT = [
    ("xref_card_num",        16, "text"),
    ("xref_cust_id",          9, "numeric"),
    ("xref_acct_id",         11, "numeric"),
    ("filler",               14, "filler"),
]

# CVCUS01Y.cpy -- Customer record (RECLN 500)
CUSTOMER_LAYOUT = [
    ("cust_id",               9, "numeric"),
    ("cust_first_name",      25, "text"),
    ("cust_middle_name",     25, "text"),
    ("cust_last_name",       25, "text"),
    ("cust_addr_line_1",     50, "text"),
    ("cust_addr_line_2",     50, "text"),
    ("cust_addr_line_3",     50, "text"),
    ("cust_addr_state_cd",    2, "text"),
    ("cust_addr_country_cd",  3, "text"),
    ("cust_addr_zip",        10, "text"),
    ("cust_phone_num_1",     15, "text"),
    ("cust_phone_num_2",     15, "text"),
    ("cust_ssn",              9, "numeric"),
    ("cust_govt_issued_id",  20, "text"),
    ("cust_dob_yyyy_mm_dd",  10, "date"),
    ("cust_eft_account_id",  10, "text"),
    ("cust_pri_card_holder_ind", 1, "text"),
    ("cust_fico_credit_score", 3, "numeric"),
    ("filler",              168, "filler"),
]

# CVTRA05Y.cpy / CVTRA06Y.cpy -- Transaction / Daily Transaction (RECLN 350)
TRANSACTION_LAYOUT = [
    ("tran_id",              16, "text"),
    ("tran_type_cd",          2, "text"),
    ("tran_cat_cd",           4, "numeric"),
    ("tran_source",          10, "text"),
    ("tran_desc",           100, "text"),
    ("tran_amt",             11, "signed"),       # S9(09)V99 -> 9+2=11 bytes
    ("tran_merchant_id",      9, "numeric"),
    ("tran_merchant_name",   50, "text"),
    ("tran_merchant_city",   50, "text"),
    ("tran_merchant_zip",    10, "text"),
    ("tran_card_num",        16, "text"),
    ("tran_orig_ts",         26, "timestamp"),
    ("tran_proc_ts",         26, "timestamp"),
    ("filler",               20, "filler"),
]

# CVTRA03Y.cpy -- Transaction type (RECLN 60)
TRAN_TYPE_LAYOUT = [
    ("tran_type",             2, "text"),
    ("tran_type_desc",       50, "text"),
    ("filler",                8, "filler"),
]

# CVTRA04Y.cpy -- Transaction category (RECLN 60)
TRAN_CATG_LAYOUT = [
    ("tran_type_cd",          2, "text"),
    ("tran_cat_cd",           4, "numeric"),
    ("tran_cat_type_desc",   50, "text"),
    ("filler",                4, "filler"),
]

# CVTRA01Y.cpy -- Transaction category balance (RECLN 50)
TRAN_CAT_BAL_LAYOUT = [
    ("trancat_acct_id",      11, "numeric"),
    ("trancat_type_cd",       2, "text"),
    ("trancat_cd",            4, "numeric"),
    ("tran_cat_bal",         11, "signed"),       # S9(09)V99 -> 9+2=11 bytes
    ("filler",               22, "filler"),
]

# CVTRA02Y.cpy -- Disclosure group (RECLN 50)
DISC_GROUP_LAYOUT = [
    ("dis_acct_group_id",    10, "text"),
    ("dis_tran_type_cd",      2, "text"),
    ("dis_tran_cat_cd",       4, "numeric"),
    ("dis_int_rate",          6, "signed_4v2"),   # S9(04)V99 -> 4+2=6 bytes
    ("filler",               28, "filler"),
]


# ── Mapping of data files to their layouts ──────────────────────────────

FILE_LAYOUTS = {
    "acctdata.txt":  ("accounts",               ACCOUNT_LAYOUT),
    "carddata.txt":  ("cards",                   CARD_LAYOUT),
    "cardxref.txt":  ("card_xrefs",              CARD_XREF_LAYOUT),
    "custdata.txt":  ("customers",               CUSTOMER_LAYOUT),
    "dailytran.txt": ("daily_transactions",      TRANSACTION_LAYOUT),
    "discgrp.txt":   ("disclosure_groups",        DISC_GROUP_LAYOUT),
    "tcatbal.txt":   ("tran_category_balances",  TRAN_CAT_BAL_LAYOUT),
    "trancatg.txt":  ("transaction_categories",  TRAN_CATG_LAYOUT),
    "trantype.txt":  ("transaction_types",       TRAN_TYPE_LAYOUT),
}
