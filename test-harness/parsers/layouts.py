"""
Copybook layout definitions for CardDemo data files.

Each layout is a list of field definitions:
  (field_name, pic_type, length, options)

pic_type values:
  'X'    - Alphanumeric (string)
  '9'    - Unsigned numeric (integer or decimal)
  'S9'   - Signed numeric (zoned decimal with sign overpunch)
  'FILLER' - Padding bytes (ignored in output)

options dict keys:
  'decimal' - Number of implied decimal places (V99 = 2)
  'occurs'  - Repeat count for OCCURS clause
"""

ACCOUNT_LAYOUT = {
    "copybook": "CVACT01Y.cpy",
    "record_length": 300,
    "fields": [
        ("ACCT-ID", "9", 11, {}),
        ("ACCT-ACTIVE-STATUS", "X", 1, {}),
        ("ACCT-CURR-BAL", "S9", 12, {"decimal": 2}),
        ("ACCT-CREDIT-LIMIT", "S9", 12, {"decimal": 2}),
        ("ACCT-CASH-CREDIT-LIMIT", "S9", 12, {"decimal": 2}),
        ("ACCT-OPEN-DATE", "X", 10, {}),
        ("ACCT-EXPIRAION-DATE", "X", 10, {}),
        ("ACCT-REISSUE-DATE", "X", 10, {}),
        ("ACCT-CURR-CYC-CREDIT", "S9", 12, {"decimal": 2}),
        ("ACCT-CURR-CYC-DEBIT", "S9", 12, {"decimal": 2}),
        ("ACCT-ADDR-ZIP", "X", 10, {}),
        ("ACCT-GROUP-ID", "X", 10, {}),
        ("FILLER", "FILLER", 178, {}),
    ],
}

CARD_LAYOUT = {
    "copybook": "CVACT02Y.cpy",
    "record_length": 150,
    "fields": [
        ("CARD-NUM", "X", 16, {}),
        ("CARD-ACCT-ID", "9", 11, {}),
        ("CARD-CVV-CD", "9", 3, {}),
        ("CARD-EMBOSSED-NAME", "X", 50, {}),
        ("CARD-EXPIRAION-DATE", "X", 10, {}),
        ("CARD-ACTIVE-STATUS", "X", 1, {}),
        ("FILLER", "FILLER", 59, {}),
    ],
}

CUSTOMER_LAYOUT = {
    "copybook": "CVCUS01Y.cpy",
    "record_length": 500,
    "fields": [
        ("CUST-ID", "9", 9, {}),
        ("CUST-FIRST-NAME", "X", 25, {}),
        ("CUST-MIDDLE-NAME", "X", 25, {}),
        ("CUST-LAST-NAME", "X", 25, {}),
        ("CUST-ADDR-LINE-1", "X", 50, {}),
        ("CUST-ADDR-LINE-2", "X", 50, {}),
        ("CUST-ADDR-LINE-3", "X", 50, {}),
        ("CUST-ADDR-STATE-CD", "X", 2, {}),
        ("CUST-ADDR-COUNTRY-CD", "X", 3, {}),
        ("CUST-ADDR-ZIP", "X", 10, {}),
        ("CUST-PHONE-NUM-1", "X", 15, {}),
        ("CUST-PHONE-NUM-2", "X", 15, {}),
        ("CUST-SSN", "9", 9, {}),
        ("CUST-GOVT-ISSUED-ID", "X", 20, {}),
        ("CUST-DOB-YYYY-MM-DD", "X", 10, {}),
        ("CUST-EFT-ACCOUNT-ID", "X", 10, {}),
        ("CUST-PRI-CARD-HOLDER-IND", "X", 1, {}),
        ("CUST-FICO-CREDIT-SCORE", "9", 3, {}),
        ("FILLER", "FILLER", 168, {}),
    ],
}

TRANSACTION_LAYOUT = {
    "copybook": "CVTRA05Y.cpy",
    "record_length": 350,
    "fields": [
        ("TRAN-ID", "X", 16, {}),
        ("TRAN-TYPE-CD", "X", 2, {}),
        ("TRAN-CAT-CD", "9", 4, {}),
        ("TRAN-SOURCE", "X", 10, {}),
        ("TRAN-DESC", "X", 100, {}),
        ("TRAN-AMT", "S9", 11, {"decimal": 2}),
        ("TRAN-MERCHANT-ID", "9", 9, {}),
        ("TRAN-MERCHANT-NAME", "X", 50, {}),
        ("TRAN-MERCHANT-CITY", "X", 50, {}),
        ("TRAN-MERCHANT-ZIP", "X", 10, {}),
        ("TRAN-CARD-NUM", "X", 16, {}),
        ("TRAN-ORIG-TS", "X", 26, {}),
        ("TRAN-PROC-TS", "X", 26, {}),
        ("FILLER", "FILLER", 20, {}),
    ],
}

CARD_XREF_LAYOUT = {
    "copybook": "CVACT03Y.cpy",
    "record_length": 50,
    "fields": [
        ("XREF-CARD-NUM", "X", 16, {}),
        ("XREF-CUST-ID", "9", 9, {}),
        ("XREF-ACCT-ID", "9", 11, {}),
        ("FILLER", "FILLER", 14, {}),
    ],
}

TRAN_TYPE_LAYOUT = {
    "copybook": "CVTRA03Y.cpy",
    "record_length": 60,
    "fields": [
        ("TRAN-TYPE", "X", 2, {}),
        ("TRAN-TYPE-DESC", "X", 50, {}),
        ("FILLER", "FILLER", 8, {}),
    ],
}

TRAN_CATEGORY_LAYOUT = {
    "copybook": "CVTRA04Y.cpy",
    "record_length": 60,
    "fields": [
        ("TRAN-TYPE-CD", "X", 2, {}),
        ("TRAN-CAT-CD", "9", 4, {}),
        ("TRAN-CAT-TYPE-DESC", "X", 50, {}),
        ("FILLER", "FILLER", 4, {}),
    ],
}

DISCLOSURE_GROUP_LAYOUT = {
    "copybook": "CVTRA02Y.cpy",
    "record_length": 50,
    "fields": [
        ("DIS-ACCT-GROUP-ID", "X", 10, {}),
        ("DIS-TRAN-TYPE-CD", "X", 2, {}),
        ("DIS-TRAN-CAT-CD", "9", 4, {}),
        ("DIS-INT-RATE", "S9", 6, {"decimal": 2}),
        ("FILLER", "FILLER", 28, {}),
    ],
}

TRAN_CAT_BAL_LAYOUT = {
    "copybook": "CVTRA01Y.cpy",
    "record_length": 50,
    "fields": [
        ("TRANCAT-ACCT-ID", "9", 11, {}),
        ("TRANCAT-TYPE-CD", "X", 2, {}),
        ("TRANCAT-CD", "9", 4, {}),
        ("TRAN-CAT-BAL", "S9", 11, {"decimal": 2}),
        ("FILLER", "FILLER", 22, {}),
    ],
}

# Mapping from data file names to their layouts
FILE_LAYOUT_MAP = {
    "acctdata": ACCOUNT_LAYOUT,
    "carddata": CARD_LAYOUT,
    "custdata": CUSTOMER_LAYOUT,
    "dailytran": TRANSACTION_LAYOUT,
    "cardxref": CARD_XREF_LAYOUT,
    "trantype": TRAN_TYPE_LAYOUT,
    "trancatg": TRAN_CATEGORY_LAYOUT,
    "discgrp": DISCLOSURE_GROUP_LAYOUT,
    "tcatbal": TRAN_CAT_BAL_LAYOUT,
}
