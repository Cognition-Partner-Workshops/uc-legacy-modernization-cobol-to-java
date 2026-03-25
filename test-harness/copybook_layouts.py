"""
Copybook layout definitions for all CardDemo ASCII data files.

Each layout is a list of tuples:
    (field_name, pic_type, length, decimals)

Field mapping from COBOL PIC clauses:
    PIC 9(n)        -> ("numeric", n, 0)
    PIC S9(n)V99    -> ("signed_numeric", n+2, 2)   -- V99 adds 2 implied decimals
    PIC S9(n)V99    -> total width = integer digits + decimal digits
    PIC X(n)        -> ("alpha", n, 0)

Sign overpunch: For SIGNED fields the last byte encodes the sign.
The total width in the file equals the number of digits (integer + decimal).
    PIC S9(10)V99 -> 12 characters in file (10 integer + 2 decimal, sign in last byte)
    PIC S9(09)V99 -> 11 characters in file
    PIC S9(04)V99 -> 6 characters in file
"""

# ---------------------------------------------------------------------------
# CVACT01Y.cpy - Account Record (RECLN 300)
# ---------------------------------------------------------------------------
ACCOUNT_LAYOUT = [
    ("ACCT-ID",               "numeric",        11, 0),
    ("ACCT-ACTIVE-STATUS",    "alpha",            1, 0),
    ("ACCT-CURR-BAL",         "signed_numeric",  12, 2),
    ("ACCT-CREDIT-LIMIT",     "signed_numeric",  12, 2),
    ("ACCT-CASH-CREDIT-LIMIT","signed_numeric",  12, 2),
    ("ACCT-OPEN-DATE",        "alpha",           10, 0),
    ("ACCT-EXPIRAION-DATE",   "alpha",           10, 0),
    ("ACCT-REISSUE-DATE",     "alpha",           10, 0),
    ("ACCT-CURR-CYC-CREDIT",  "signed_numeric", 12, 2),
    ("ACCT-CURR-CYC-DEBIT",   "signed_numeric", 12, 2),
    ("ACCT-ADDR-ZIP",         "alpha",           10, 0),
    ("ACCT-GROUP-ID",         "alpha",           10, 0),
    ("FILLER",                "alpha",          178, 0),
]

# ---------------------------------------------------------------------------
# CVACT02Y.cpy - Card Record (RECLN 150)
# ---------------------------------------------------------------------------
CARD_LAYOUT = [
    ("CARD-NUM",              "alpha",           16, 0),
    ("CARD-ACCT-ID",          "numeric",         11, 0),
    ("CARD-CVV-CD",           "numeric",          3, 0),
    ("CARD-EMBOSSED-NAME",    "alpha",           50, 0),
    ("CARD-EXPIRAION-DATE",   "alpha",           10, 0),
    ("CARD-ACTIVE-STATUS",    "alpha",            1, 0),
    ("FILLER",                "alpha",           59, 0),
]

# ---------------------------------------------------------------------------
# CVCUS01Y.cpy - Customer Record (RECLN 500)
# ---------------------------------------------------------------------------
CUSTOMER_LAYOUT = [
    ("CUST-ID",               "numeric",          9, 0),
    ("CUST-FIRST-NAME",       "alpha",           25, 0),
    ("CUST-MIDDLE-NAME",      "alpha",           25, 0),
    ("CUST-LAST-NAME",        "alpha",           25, 0),
    ("CUST-ADDR-LINE-1",      "alpha",           50, 0),
    ("CUST-ADDR-LINE-2",      "alpha",           50, 0),
    ("CUST-ADDR-LINE-3",      "alpha",           50, 0),
    ("CUST-ADDR-STATE-CD",    "alpha",            2, 0),
    ("CUST-ADDR-COUNTRY-CD",  "alpha",            3, 0),
    ("CUST-ADDR-ZIP",         "alpha",           10, 0),
    ("CUST-PHONE-NUM-1",      "alpha",           15, 0),
    ("CUST-PHONE-NUM-2",      "alpha",           15, 0),
    ("CUST-SSN",              "numeric",          9, 0),
    ("CUST-GOVT-ISSUED-ID",   "alpha",           20, 0),
    ("CUST-DOB-YYYY-MM-DD",   "alpha",           10, 0),
    ("CUST-EFT-ACCOUNT-ID",   "alpha",           10, 0),
    ("CUST-PRI-CARD-HOLDER-IND", "alpha",         1, 0),
    ("CUST-FICO-CREDIT-SCORE","numeric",          3, 0),
    ("FILLER",                "alpha",          168, 0),
]

# ---------------------------------------------------------------------------
# CVACT03Y.cpy - Card Cross-Reference Record (RECLN 50)
# ---------------------------------------------------------------------------
CARD_XREF_LAYOUT = [
    ("XREF-CARD-NUM",         "alpha",           16, 0),
    ("XREF-CUST-ID",          "numeric",          9, 0),
    ("XREF-ACCT-ID",          "numeric",         11, 0),
    ("FILLER",                "alpha",           14, 0),
]

# ---------------------------------------------------------------------------
# CVTRA05Y.cpy - Transaction Record (RECLN 350)
# ---------------------------------------------------------------------------
TRANSACTION_LAYOUT = [
    ("TRAN-ID",               "alpha",           16, 0),
    ("TRAN-TYPE-CD",          "alpha",            2, 0),
    ("TRAN-CAT-CD",           "numeric",          4, 0),
    ("TRAN-SOURCE",           "alpha",           10, 0),
    ("TRAN-DESC",             "alpha",          100, 0),
    ("TRAN-AMT",              "signed_numeric",  11, 2),
    ("TRAN-MERCHANT-ID",      "numeric",          9, 0),
    ("TRAN-MERCHANT-NAME",    "alpha",           50, 0),
    ("TRAN-MERCHANT-CITY",    "alpha",           50, 0),
    ("TRAN-MERCHANT-ZIP",     "alpha",           10, 0),
    ("TRAN-CARD-NUM",         "alpha",           16, 0),
    ("TRAN-ORIG-TS",          "alpha",           26, 0),
    ("TRAN-PROC-TS",          "alpha",           26, 0),
    ("FILLER",                "alpha",           20, 0),
]

# ---------------------------------------------------------------------------
# CVTRA06Y.cpy - Daily Transaction Record (RECLN 350)
# ---------------------------------------------------------------------------
DAILY_TRANSACTION_LAYOUT = [
    ("DALYTRAN-ID",           "alpha",           16, 0),
    ("DALYTRAN-TYPE-CD",      "alpha",            2, 0),
    ("DALYTRAN-CAT-CD",       "numeric",          4, 0),
    ("DALYTRAN-SOURCE",       "alpha",           10, 0),
    ("DALYTRAN-DESC",         "alpha",          100, 0),
    ("DALYTRAN-AMT",          "signed_numeric",  11, 2),
    ("DALYTRAN-MERCHANT-ID",  "numeric",          9, 0),
    ("DALYTRAN-MERCHANT-NAME","alpha",           50, 0),
    ("DALYTRAN-MERCHANT-CITY","alpha",           50, 0),
    ("DALYTRAN-MERCHANT-ZIP", "alpha",           10, 0),
    ("DALYTRAN-CARD-NUM",     "alpha",           16, 0),
    ("DALYTRAN-ORIG-TS",      "alpha",           26, 0),
    ("DALYTRAN-PROC-TS",      "alpha",           26, 0),
    ("FILLER",                "alpha",           20, 0),
]

# ---------------------------------------------------------------------------
# CVTRA01Y.cpy - Transaction Category Balance Record (RECLN 50)
# ---------------------------------------------------------------------------
TRAN_CAT_BAL_LAYOUT = [
    ("TRANCAT-ACCT-ID",       "numeric",         11, 0),
    ("TRANCAT-TYPE-CD",       "alpha",            2, 0),
    ("TRANCAT-CD",            "numeric",          4, 0),
    ("TRAN-CAT-BAL",          "signed_numeric",  11, 2),
    ("FILLER",                "alpha",           22, 0),
]

# ---------------------------------------------------------------------------
# CVTRA02Y.cpy - Disclosure Group Record (RECLN 50)
# ---------------------------------------------------------------------------
DISCLOSURE_GROUP_LAYOUT = [
    ("DIS-ACCT-GROUP-ID",     "alpha",           10, 0),
    ("DIS-TRAN-TYPE-CD",      "alpha",            2, 0),
    ("DIS-TRAN-CAT-CD",       "numeric",          4, 0),
    ("DIS-INT-RATE",          "signed_numeric",   6, 2),
    ("FILLER",                "alpha",           28, 0),
]

# ---------------------------------------------------------------------------
# CVTRA03Y.cpy - Transaction Type Record (RECLN 60)
# ---------------------------------------------------------------------------
TRAN_TYPE_LAYOUT = [
    ("TRAN-TYPE",             "alpha",            2, 0),
    ("TRAN-TYPE-DESC",        "alpha",           50, 0),
    ("FILLER",                "alpha",            8, 0),
]

# ---------------------------------------------------------------------------
# CVTRA04Y.cpy - Transaction Category Record (RECLN 60)
# ---------------------------------------------------------------------------
TRAN_CATEGORY_LAYOUT = [
    ("TRAN-TYPE-CD",          "alpha",            2, 0),
    ("TRAN-CAT-CD",           "numeric",          4, 0),
    ("TRAN-CAT-TYPE-DESC",    "alpha",           50, 0),
    ("FILLER",                "alpha",            4, 0),
]

# ---------------------------------------------------------------------------
# CSUSR01Y.cpy - User Security Record (RECLN 80)
# ---------------------------------------------------------------------------
USER_SECURITY_LAYOUT = [
    ("SEC-USR-ID",            "alpha",            8, 0),
    ("SEC-USR-FNAME",         "alpha",           20, 0),
    ("SEC-USR-LNAME",         "alpha",           20, 0),
    ("SEC-USR-PWD",           "alpha",            8, 0),
    ("SEC-USR-TYPE",          "alpha",            1, 0),
    ("FILLER",                "alpha",           23, 0),
]

# ---------------------------------------------------------------------------
# Mapping from data file basenames to their layouts
# ---------------------------------------------------------------------------
FILE_LAYOUT_MAP = {
    "acctdata.txt":  ACCOUNT_LAYOUT,
    "carddata.txt":  CARD_LAYOUT,
    "custdata.txt":  CUSTOMER_LAYOUT,
    "cardxref.txt":  CARD_XREF_LAYOUT,
    "dailytran.txt": DAILY_TRANSACTION_LAYOUT,
    "trantype.txt":  TRAN_TYPE_LAYOUT,
    "trancatg.txt":  TRAN_CATEGORY_LAYOUT,
    "tcatbal.txt":   TRAN_CAT_BAL_LAYOUT,
    "discgrp.txt":   DISCLOSURE_GROUP_LAYOUT,
}
