"""
copybook_parser.py -- COBOL copybook layout definitions for all CardDemo data files.

Each layout is a list of field descriptors:
    {
        "name":    str,          # Field name (COBOL style, e.g. "ACCT-ID")
        "length":  int,          # Field width in bytes
        "type":    str,          # "alpha" | "unsigned" | "signed_decimal" | "filler"
        "decimal": int,          # Implied decimal places (only for signed_decimal)
    }

Record lengths match the COBOL copybook RECLN comments.
"""

# ---------------------------------------------------------------------------
# CVACT01Y.cpy -- Account record (RECLN 300)
# ---------------------------------------------------------------------------
ACCOUNT_LAYOUT = [
    {"name": "ACCT-ID",               "length": 11, "type": "unsigned",       "decimal": 0},
    {"name": "ACCT-ACTIVE-STATUS",    "length":  1, "type": "alpha",          "decimal": 0},
    {"name": "ACCT-CURR-BAL",         "length": 12, "type": "signed_decimal", "decimal": 2},
    {"name": "ACCT-CREDIT-LIMIT",     "length": 12, "type": "signed_decimal", "decimal": 2},
    {"name": "ACCT-CASH-CREDIT-LIMIT","length": 12, "type": "signed_decimal", "decimal": 2},
    {"name": "ACCT-OPEN-DATE",        "length": 10, "type": "alpha",          "decimal": 0},
    {"name": "ACCT-EXPIRAION-DATE",   "length": 10, "type": "alpha",          "decimal": 0},
    {"name": "ACCT-REISSUE-DATE",     "length": 10, "type": "alpha",          "decimal": 0},
    {"name": "ACCT-CURR-CYC-CREDIT",  "length": 12, "type": "signed_decimal", "decimal": 2},
    {"name": "ACCT-CURR-CYC-DEBIT",   "length": 12, "type": "signed_decimal", "decimal": 2},
    {"name": "ACCT-ADDR-ZIP",         "length": 10, "type": "alpha",          "decimal": 0},
    {"name": "ACCT-GROUP-ID",         "length": 10, "type": "alpha",          "decimal": 0},
    {"name": "FILLER",                "length": 178,"type": "filler",         "decimal": 0},
]
ACCOUNT_RECLEN = 300

# ---------------------------------------------------------------------------
# CVACT02Y.cpy -- Card record (RECLN 150)
# ---------------------------------------------------------------------------
CARD_LAYOUT = [
    {"name": "CARD-NUM",              "length": 16, "type": "alpha",          "decimal": 0},
    {"name": "CARD-ACCT-ID",          "length": 11, "type": "unsigned",       "decimal": 0},
    {"name": "CARD-CVV-CD",           "length":  3, "type": "unsigned",       "decimal": 0},
    {"name": "CARD-EMBOSSED-NAME",    "length": 50, "type": "alpha",          "decimal": 0},
    {"name": "CARD-EXPIRAION-DATE",   "length": 10, "type": "alpha",          "decimal": 0},
    {"name": "CARD-ACTIVE-STATUS",    "length":  1, "type": "alpha",          "decimal": 0},
    {"name": "FILLER",                "length": 59, "type": "filler",         "decimal": 0},
]
CARD_RECLEN = 150

# ---------------------------------------------------------------------------
# CVACT03Y.cpy -- Card cross-reference (RECLN 50)
# Note: actual ASCII file records are 36 bytes (no FILLER padding in file)
# ---------------------------------------------------------------------------
CARD_XREF_LAYOUT = [
    {"name": "XREF-CARD-NUM",         "length": 16, "type": "alpha",          "decimal": 0},
    {"name": "XREF-CUST-ID",          "length":  9, "type": "unsigned",       "decimal": 0},
    {"name": "XREF-ACCT-ID",          "length": 11, "type": "unsigned",       "decimal": 0},
]
CARD_XREF_RECLEN = 36  # Actual file record length (copybook says 50 with FILLER)

# ---------------------------------------------------------------------------
# CVCUS01Y.cpy -- Customer record (RECLN 500)
# ---------------------------------------------------------------------------
CUSTOMER_LAYOUT = [
    {"name": "CUST-ID",               "length":  9, "type": "unsigned",       "decimal": 0},
    {"name": "CUST-FIRST-NAME",       "length": 25, "type": "alpha",          "decimal": 0},
    {"name": "CUST-MIDDLE-NAME",      "length": 25, "type": "alpha",          "decimal": 0},
    {"name": "CUST-LAST-NAME",        "length": 25, "type": "alpha",          "decimal": 0},
    {"name": "CUST-ADDR-LINE-1",      "length": 50, "type": "alpha",          "decimal": 0},
    {"name": "CUST-ADDR-LINE-2",      "length": 50, "type": "alpha",          "decimal": 0},
    {"name": "CUST-ADDR-LINE-3",      "length": 50, "type": "alpha",          "decimal": 0},
    {"name": "CUST-ADDR-STATE-CD",    "length":  2, "type": "alpha",          "decimal": 0},
    {"name": "CUST-ADDR-COUNTRY-CD",  "length":  3, "type": "alpha",          "decimal": 0},
    {"name": "CUST-ADDR-ZIP",         "length": 10, "type": "alpha",          "decimal": 0},
    {"name": "CUST-PHONE-NUM-1",      "length": 15, "type": "alpha",          "decimal": 0},
    {"name": "CUST-PHONE-NUM-2",      "length": 15, "type": "alpha",          "decimal": 0},
    {"name": "CUST-SSN",              "length":  9, "type": "unsigned",       "decimal": 0},
    {"name": "CUST-GOVT-ISSUED-ID",   "length": 20, "type": "alpha",          "decimal": 0},
    {"name": "CUST-DOB-YYYY-MM-DD",   "length": 10, "type": "alpha",          "decimal": 0},
    {"name": "CUST-EFT-ACCOUNT-ID",   "length": 10, "type": "alpha",          "decimal": 0},
    {"name": "CUST-PRI-CARD-HOLDER-IND", "length": 1, "type": "alpha",        "decimal": 0},
    {"name": "CUST-FICO-CREDIT-SCORE","length":  3, "type": "unsigned",       "decimal": 0},
    {"name": "FILLER",                "length": 168,"type": "filler",         "decimal": 0},
]
CUSTOMER_RECLEN = 500

# ---------------------------------------------------------------------------
# CSUSR01Y.cpy -- User security record (RECLN 80)
# ---------------------------------------------------------------------------
USER_SECURITY_LAYOUT = [
    {"name": "SEC-USR-ID",            "length":  8, "type": "alpha",          "decimal": 0},
    {"name": "SEC-USR-FNAME",         "length": 20, "type": "alpha",          "decimal": 0},
    {"name": "SEC-USR-LNAME",         "length": 20, "type": "alpha",          "decimal": 0},
    {"name": "SEC-USR-PWD",           "length":  8, "type": "alpha",          "decimal": 0},
    {"name": "SEC-USR-TYPE",          "length":  1, "type": "alpha",          "decimal": 0},
    {"name": "SEC-USR-FILLER",        "length": 23, "type": "filler",         "decimal": 0},
]
USER_SECURITY_RECLEN = 80

# ---------------------------------------------------------------------------
# CVTRA05Y.cpy -- Transaction record (RECLN 350)
# (Also used for CVTRA06Y daily transactions -- same layout)
# ---------------------------------------------------------------------------
TRANSACTION_LAYOUT = [
    {"name": "TRAN-ID",               "length": 16, "type": "alpha",          "decimal": 0},
    {"name": "TRAN-TYPE-CD",          "length":  2, "type": "alpha",          "decimal": 0},
    {"name": "TRAN-CAT-CD",           "length":  4, "type": "unsigned",       "decimal": 0},
    {"name": "TRAN-SOURCE",           "length": 10, "type": "alpha",          "decimal": 0},
    {"name": "TRAN-DESC",             "length": 100,"type": "alpha",          "decimal": 0},
    {"name": "TRAN-AMT",              "length": 11, "type": "signed_decimal", "decimal": 2},
    {"name": "TRAN-MERCHANT-ID",      "length":  9, "type": "unsigned",       "decimal": 0},
    {"name": "TRAN-MERCHANT-NAME",    "length": 50, "type": "alpha",          "decimal": 0},
    {"name": "TRAN-MERCHANT-CITY",    "length": 50, "type": "alpha",          "decimal": 0},
    {"name": "TRAN-MERCHANT-ZIP",     "length": 10, "type": "alpha",          "decimal": 0},
    {"name": "TRAN-CARD-NUM",         "length": 16, "type": "alpha",          "decimal": 0},
    {"name": "TRAN-ORIG-TS",          "length": 26, "type": "alpha",          "decimal": 0},
    {"name": "TRAN-PROC-TS",          "length": 26, "type": "alpha",          "decimal": 0},
    {"name": "FILLER",                "length": 21, "type": "filler",         "decimal": 0},
]
TRANSACTION_RECLEN = 350  # Used for both dailytran.txt and transaction master

# ---------------------------------------------------------------------------
# CVTRA01Y.cpy -- Transaction category balance (RECLN 50)
# ---------------------------------------------------------------------------
TRAN_CAT_BAL_LAYOUT = [
    {"name": "TRANCAT-ACCT-ID",       "length": 11, "type": "unsigned",       "decimal": 0},
    {"name": "TRANCAT-TYPE-CD",       "length":  2, "type": "alpha",          "decimal": 0},
    {"name": "TRANCAT-CD",            "length":  4, "type": "unsigned",       "decimal": 0},
    {"name": "TRAN-CAT-BAL",          "length": 11, "type": "signed_decimal", "decimal": 2},
    {"name": "FILLER",                "length": 22, "type": "filler",         "decimal": 0},
]
TRAN_CAT_BAL_RECLEN = 50

# ---------------------------------------------------------------------------
# CVTRA02Y.cpy -- Disclosure group (RECLN 50)
# ---------------------------------------------------------------------------
DISC_GROUP_LAYOUT = [
    {"name": "DIS-ACCT-GROUP-ID",     "length": 10, "type": "alpha",          "decimal": 0},
    {"name": "DIS-TRAN-TYPE-CD",      "length":  2, "type": "alpha",          "decimal": 0},
    {"name": "DIS-TRAN-CAT-CD",       "length":  4, "type": "unsigned",       "decimal": 0},
    {"name": "DIS-INT-RATE",          "length":  6, "type": "signed_decimal", "decimal": 2},
    {"name": "FILLER",                "length": 28, "type": "filler",         "decimal": 0},
]
DISC_GROUP_RECLEN = 50

# ---------------------------------------------------------------------------
# CVTRA03Y.cpy -- Transaction type (RECLN 60)
# ---------------------------------------------------------------------------
TRAN_TYPE_LAYOUT = [
    {"name": "TRAN-TYPE",             "length":  2, "type": "alpha",          "decimal": 0},
    {"name": "TRAN-TYPE-DESC",        "length": 50, "type": "alpha",          "decimal": 0},
    {"name": "FILLER",                "length":  8, "type": "filler",         "decimal": 0},
]
TRAN_TYPE_RECLEN = 60

# ---------------------------------------------------------------------------
# CVTRA04Y.cpy -- Transaction category (RECLN 60)
# ---------------------------------------------------------------------------
TRAN_CATEGORY_LAYOUT = [
    {"name": "TRAN-TYPE-CD",          "length":  2, "type": "alpha",          "decimal": 0},
    {"name": "TRAN-CAT-CD",           "length":  4, "type": "unsigned",       "decimal": 0},
    {"name": "TRAN-CAT-TYPE-DESC",    "length": 50, "type": "alpha",          "decimal": 0},
    {"name": "FILLER",                "length":  4, "type": "filler",         "decimal": 0},
]
TRAN_CATEGORY_RECLEN = 60

# ---------------------------------------------------------------------------
# Registry: maps data file basenames to their layout and record length
# ---------------------------------------------------------------------------
FILE_LAYOUTS = {
    "acctdata":  {"layout": ACCOUNT_LAYOUT,       "reclen": ACCOUNT_RECLEN,       "copybook": "CVACT01Y.cpy"},
    "carddata":  {"layout": CARD_LAYOUT,           "reclen": CARD_RECLEN,          "copybook": "CVACT02Y.cpy"},
    "cardxref":  {"layout": CARD_XREF_LAYOUT,      "reclen": CARD_XREF_RECLEN,     "copybook": "CVACT03Y.cpy"},
    "custdata":  {"layout": CUSTOMER_LAYOUT,        "reclen": CUSTOMER_RECLEN,      "copybook": "CVCUS01Y.cpy"},
    "dailytran": {"layout": TRANSACTION_LAYOUT,     "reclen": TRANSACTION_RECLEN,   "copybook": "CVTRA06Y.cpy"},
    "discgrp":   {"layout": DISC_GROUP_LAYOUT,      "reclen": DISC_GROUP_RECLEN,    "copybook": "CVTRA02Y.cpy"},
    "tcatbal":   {"layout": TRAN_CAT_BAL_LAYOUT,    "reclen": TRAN_CAT_BAL_RECLEN,  "copybook": "CVTRA01Y.cpy"},
    "trancatg":  {"layout": TRAN_CATEGORY_LAYOUT,   "reclen": TRAN_CATEGORY_RECLEN, "copybook": "CVTRA04Y.cpy"},
    "trantype":  {"layout": TRAN_TYPE_LAYOUT,        "reclen": TRAN_TYPE_RECLEN,     "copybook": "CVTRA03Y.cpy"},
}


def get_layout(file_key: str) -> dict:
    """Return layout definition for a data file key.

    Args:
        file_key: Base name without extension (e.g., "acctdata").

    Returns:
        Dict with keys: layout (list of field defs), reclen (int), copybook (str).

    Raises:
        KeyError: If file_key is not recognized.
    """
    return FILE_LAYOUTS[file_key]


def compute_layout_length(layout: list[dict]) -> int:
    """Sum the total byte length of a copybook layout."""
    return sum(field["length"] for field in layout)
