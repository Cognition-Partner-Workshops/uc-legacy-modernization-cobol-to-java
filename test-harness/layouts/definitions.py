"""
COBOL copybook layout definitions for CardDemo data files.

Each layout is a list of (field_name, pic_type, length) tuples where:
- field_name: The COBOL field name
- pic_type: One of 'X' (alphanumeric), '9' (unsigned numeric),
            'S9' (signed zoned decimal), 'S9V99' (signed with 2 decimal places)
- length: Number of characters the field occupies in the fixed-width record

For signed numeric fields (S9/S9V99), the last byte uses zoned decimal
overpunch encoding to represent the sign.
"""

from typing import NamedTuple


class FieldDef(NamedTuple):
    name: str
    pic_type: str
    length: int


# CVACT01Y.cpy - Account Record (RECLN 300)
ACCOUNT_LAYOUT = [
    FieldDef("ACCT-ID", "9", 11),
    FieldDef("ACCT-ACTIVE-STATUS", "X", 1),
    FieldDef("ACCT-CURR-BAL", "S9V99", 12),
    FieldDef("ACCT-CREDIT-LIMIT", "S9V99", 12),
    FieldDef("ACCT-CASH-CREDIT-LIMIT", "S9V99", 12),
    FieldDef("ACCT-OPEN-DATE", "X", 10),
    FieldDef("ACCT-EXPIRAION-DATE", "X", 10),
    FieldDef("ACCT-REISSUE-DATE", "X", 10),
    FieldDef("ACCT-CURR-CYC-CREDIT", "S9V99", 12),
    FieldDef("ACCT-CURR-CYC-DEBIT", "S9V99", 12),
    FieldDef("ACCT-ADDR-ZIP", "X", 10),
    FieldDef("ACCT-GROUP-ID", "X", 10),
    FieldDef("FILLER", "X", 178),
]

# CVCUS01Y.cpy - Customer Record (RECLN 500)
CUSTOMER_LAYOUT = [
    FieldDef("CUST-ID", "9", 9),
    FieldDef("CUST-FIRST-NAME", "X", 25),
    FieldDef("CUST-MIDDLE-NAME", "X", 25),
    FieldDef("CUST-LAST-NAME", "X", 25),
    FieldDef("CUST-ADDR-LINE-1", "X", 50),
    FieldDef("CUST-ADDR-LINE-2", "X", 50),
    FieldDef("CUST-ADDR-LINE-3", "X", 50),
    FieldDef("CUST-ADDR-STATE-CD", "X", 2),
    FieldDef("CUST-ADDR-COUNTRY-CD", "X", 3),
    FieldDef("CUST-ADDR-ZIP", "X", 10),
    FieldDef("CUST-PHONE-NUM-1", "X", 15),
    FieldDef("CUST-PHONE-NUM-2", "X", 15),
    FieldDef("CUST-SSN", "9", 9),
    FieldDef("CUST-GOVT-ISSUED-ID", "X", 20),
    FieldDef("CUST-DOB-YYYY-MM-DD", "X", 10),
    FieldDef("CUST-EFT-ACCOUNT-ID", "X", 10),
    FieldDef("CUST-PRI-CARD-HOLDER-IND", "X", 1),
    FieldDef("CUST-FICO-CREDIT-SCORE", "9", 3),
    FieldDef("FILLER", "X", 168),
]

# CVACT02Y.cpy - Card Record (RECLN 150)
CARD_LAYOUT = [
    FieldDef("CARD-NUM", "X", 16),
    FieldDef("CARD-ACCT-ID", "9", 11),
    FieldDef("CARD-CVV-CD", "9", 3),
    FieldDef("CARD-EMBOSSED-NAME", "X", 50),
    FieldDef("CARD-EXPIRAION-DATE", "X", 10),
    FieldDef("CARD-ACTIVE-STATUS", "X", 1),
    FieldDef("FILLER", "X", 59),
]

# CVACT03Y.cpy - Card Cross-Reference Record (RECLN 50)
CARD_XREF_LAYOUT = [
    FieldDef("XREF-CARD-NUM", "X", 16),
    FieldDef("XREF-CUST-ID", "9", 9),
    FieldDef("XREF-ACCT-ID", "9", 11),
    FieldDef("FILLER", "X", 14),
]

# CVTRA05Y.cpy - Transaction Record (RECLN 350)
TRANSACTION_LAYOUT = [
    FieldDef("TRAN-ID", "X", 16),
    FieldDef("TRAN-TYPE-CD", "X", 2),
    FieldDef("TRAN-CAT-CD", "9", 4),
    FieldDef("TRAN-SOURCE", "X", 10),
    FieldDef("TRAN-DESC", "X", 100),
    FieldDef("TRAN-AMT", "S9V99", 11),
    FieldDef("TRAN-MERCHANT-ID", "9", 9),
    FieldDef("TRAN-MERCHANT-NAME", "X", 50),
    FieldDef("TRAN-MERCHANT-CITY", "X", 50),
    FieldDef("TRAN-MERCHANT-ZIP", "X", 10),
    FieldDef("TRAN-CARD-NUM", "X", 16),
    FieldDef("TRAN-ORIG-TS", "X", 26),
    FieldDef("TRAN-PROC-TS", "X", 26),
    FieldDef("FILLER", "X", 20),
]

# CVTRA06Y.cpy - Daily Transaction Record (RECLN 350)
DAILY_TRANSACTION_LAYOUT = [
    FieldDef("DALYTRAN-ID", "X", 16),
    FieldDef("DALYTRAN-TYPE-CD", "X", 2),
    FieldDef("DALYTRAN-CAT-CD", "9", 4),
    FieldDef("DALYTRAN-SOURCE", "X", 10),
    FieldDef("DALYTRAN-DESC", "X", 100),
    FieldDef("DALYTRAN-AMT", "S9V99", 11),
    FieldDef("DALYTRAN-MERCHANT-ID", "9", 9),
    FieldDef("DALYTRAN-MERCHANT-NAME", "X", 50),
    FieldDef("DALYTRAN-MERCHANT-CITY", "X", 50),
    FieldDef("DALYTRAN-MERCHANT-ZIP", "X", 10),
    FieldDef("DALYTRAN-CARD-NUM", "X", 16),
    FieldDef("DALYTRAN-ORIG-TS", "X", 26),
    FieldDef("DALYTRAN-PROC-TS", "X", 26),
    FieldDef("FILLER", "X", 20),
]

# CVTRA03Y.cpy - Transaction Type Record (RECLN 60)
TRANSACTION_TYPE_LAYOUT = [
    FieldDef("TRAN-TYPE", "X", 2),
    FieldDef("TRAN-TYPE-DESC", "X", 50),
    FieldDef("FILLER", "X", 8),
]

# CVTRA04Y.cpy - Transaction Category Record (RECLN 60)
TRANSACTION_CATEGORY_LAYOUT = [
    FieldDef("TRAN-TYPE-CD", "X", 2),
    FieldDef("TRAN-CAT-CD", "9", 4),
    FieldDef("TRAN-CAT-TYPE-DESC", "X", 50),
    FieldDef("FILLER", "X", 4),
]

# CVTRA01Y.cpy - Transaction Category Balance Record (RECLN 50)
TRAN_CAT_BALANCE_LAYOUT = [
    FieldDef("TRANCAT-ACCT-ID", "9", 11),
    FieldDef("TRANCAT-TYPE-CD", "X", 2),
    FieldDef("TRANCAT-CD", "9", 4),
    FieldDef("TRAN-CAT-BAL", "S9V99", 11),
    FieldDef("FILLER", "X", 22),
]

# CVTRA02Y.cpy - Disclosure Group Record (RECLN 50)
DISCLOSURE_GROUP_LAYOUT = [
    FieldDef("DIS-ACCT-GROUP-ID", "X", 10),
    FieldDef("DIS-TRAN-TYPE-CD", "X", 2),
    FieldDef("DIS-TRAN-CAT-CD", "9", 4),
    FieldDef("DIS-INT-RATE", "S9V99", 6),
    FieldDef("FILLER", "X", 28),
]


# Map of data file basenames to their layouts and record lengths
FILE_LAYOUT_MAP = {
    "acctdata": {
        "layout": ACCOUNT_LAYOUT,
        "record_length": 300,
        "copybook": "CVACT01Y.cpy",
        "record_name": "ACCOUNT-RECORD",
    },
    "custdata": {
        "layout": CUSTOMER_LAYOUT,
        "record_length": 500,
        "copybook": "CVCUS01Y.cpy",
        "record_name": "CUSTOMER-RECORD",
    },
    "carddata": {
        "layout": CARD_LAYOUT,
        "record_length": 150,
        "copybook": "CVACT02Y.cpy",
        "record_name": "CARD-RECORD",
    },
    "cardxref": {
        "layout": CARD_XREF_LAYOUT,
        "record_length": 50,
        "copybook": "CVACT03Y.cpy",
        "record_name": "CARD-XREF-RECORD",
    },
    "dailytran": {
        "layout": DAILY_TRANSACTION_LAYOUT,
        "record_length": 350,
        "copybook": "CVTRA06Y.cpy",
        "record_name": "DALYTRAN-RECORD",
    },
    "trantype": {
        "layout": TRANSACTION_TYPE_LAYOUT,
        "record_length": 60,
        "copybook": "CVTRA03Y.cpy",
        "record_name": "TRAN-TYPE-RECORD",
    },
    "trancatg": {
        "layout": TRANSACTION_CATEGORY_LAYOUT,
        "record_length": 60,
        "copybook": "CVTRA04Y.cpy",
        "record_name": "TRAN-CAT-RECORD",
    },
    "tcatbal": {
        "layout": TRAN_CAT_BALANCE_LAYOUT,
        "record_length": 50,
        "copybook": "CVTRA01Y.cpy",
        "record_name": "TRAN-CAT-BAL-RECORD",
    },
    "discgrp": {
        "layout": DISCLOSURE_GROUP_LAYOUT,
        "record_length": 50,
        "copybook": "CVTRA02Y.cpy",
        "record_name": "DIS-GROUP-RECORD",
    },
}
