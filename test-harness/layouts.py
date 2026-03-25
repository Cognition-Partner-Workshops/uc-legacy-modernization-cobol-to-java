"""
Copybook layout definitions for CardDemo fixed-width data files.

Each layout is a list of (field_name, field_type, length) tuples derived
from the corresponding COBOL copybook PIC clauses.

Field types:
  - "X"    : Alphanumeric (PIC X(n)) — returned as stripped string
  - "9"    : Unsigned numeric (PIC 9(n)) — returned as int
  - "S9V2" : Signed decimal with 2 implied decimals (PIC S9(n)V99)
             Display length = integer_digits + 2 (sign overpunched in last byte)
  - "FILLER" : Padding bytes — skipped in output

COBOL DISPLAY format: The sign is encoded via overpunch in the last byte,
so PIC S9(10)V99 occupies 10+2 = 12 bytes, NOT 13.
"""

# CVACT01Y — Account Record (RECLN 300)
# PIC S9(10)V99 = 12 bytes in DISPLAY format (sign overpunched in last byte)
ACCTDATA_LAYOUT = [
    ("ACCT_ID",                "9",     11),
    ("ACCT_ACTIVE_STATUS",     "X",      1),
    ("ACCT_CURR_BAL",          "S9V2",  12),  # S9(10)V99
    ("ACCT_CREDIT_LIMIT",      "S9V2",  12),  # S9(10)V99
    ("ACCT_CASH_CREDIT_LIMIT", "S9V2",  12),  # S9(10)V99
    ("ACCT_OPEN_DATE",         "X",     10),
    ("ACCT_EXPIRAION_DATE",    "X",     10),
    ("ACCT_REISSUE_DATE",      "X",     10),
    ("ACCT_CURR_CYC_CREDIT",   "S9V2",  12),  # S9(10)V99
    ("ACCT_CURR_CYC_DEBIT",    "S9V2",  12),  # S9(10)V99
    ("ACCT_ADDR_ZIP",          "X",     10),
    ("ACCT_GROUP_ID",          "X",     10),
    ("FILLER",                 "FILLER", 178),
]
# Total: 11+1+12+12+12+10+10+10+12+12+10+10+178 = 300

# CVACT02Y — Card Record (RECLN 150)
CARDDATA_LAYOUT = [
    ("CARD_NUM",               "X",     16),
    ("CARD_ACCT_ID",           "9",     11),
    ("CARD_CVV_CD",            "9",      3),
    ("CARD_EMBOSSED_NAME",     "X",     50),
    ("CARD_EXPIRAION_DATE",    "X",     10),
    ("CARD_ACTIVE_STATUS",     "X",      1),
    ("FILLER",                 "FILLER", 59),
]

# CVACT03Y — Card Cross-Reference (RECLN 50)
CARDXREF_LAYOUT = [
    ("XREF_CARD_NUM",          "X",     16),
    ("XREF_CUST_ID",           "9",      9),
    ("XREF_ACCT_ID",           "9",     11),
    ("FILLER",                 "FILLER", 14),
]

# CVCUS01Y — Customer Record (RECLN 500)
CUSTDATA_LAYOUT = [
    ("CUST_ID",                "9",      9),
    ("CUST_FIRST_NAME",        "X",     25),
    ("CUST_MIDDLE_NAME",       "X",     25),
    ("CUST_LAST_NAME",         "X",     25),
    ("CUST_ADDR_LINE_1",       "X",     50),
    ("CUST_ADDR_LINE_2",       "X",     50),
    ("CUST_ADDR_LINE_3",       "X",     50),
    ("CUST_ADDR_STATE_CD",     "X",      2),
    ("CUST_ADDR_COUNTRY_CD",   "X",      3),
    ("CUST_ADDR_ZIP",          "X",     10),
    ("CUST_PHONE_NUM_1",       "X",     15),
    ("CUST_PHONE_NUM_2",       "X",     15),
    ("CUST_SSN",               "9",      9),
    ("CUST_GOVT_ISSUED_ID",    "X",     20),
    ("CUST_DOB_YYYY_MM_DD",    "X",     10),
    ("CUST_EFT_ACCOUNT_ID",    "X",     10),
    ("CUST_PRI_CARD_HOLDER_IND", "X",    1),
    ("CUST_FICO_CREDIT_SCORE", "9",      3),
    ("FILLER",                 "FILLER", 168),
]

# CVTRA06Y — Daily Transaction Record (RECLN 350)
# PIC S9(09)V99 = 11 bytes in DISPLAY format
DAILYTRAN_LAYOUT = [
    ("DALYTRAN_ID",            "X",     16),
    ("DALYTRAN_TYPE_CD",       "X",      2),
    ("DALYTRAN_CAT_CD",        "9",      4),
    ("DALYTRAN_SOURCE",        "X",     10),
    ("DALYTRAN_DESC",          "X",    100),
    ("DALYTRAN_AMT",           "S9V2",  11),  # S9(09)V99
    ("DALYTRAN_MERCHANT_ID",   "9",      9),
    ("DALYTRAN_MERCHANT_NAME", "X",     50),
    ("DALYTRAN_MERCHANT_CITY", "X",     50),
    ("DALYTRAN_MERCHANT_ZIP",  "X",     10),
    ("DALYTRAN_CARD_NUM",      "X",     16),
    ("DALYTRAN_ORIG_TS",       "X",     26),
    ("DALYTRAN_PROC_TS",       "X",     26),
    ("FILLER",                 "FILLER", 20),
]
# Total: 16+2+4+10+100+11+9+50+50+10+16+26+26+20 = 350

# CVTRA01Y — Transaction Category Balance (RECLN 50)
# PIC S9(09)V99 = 11 bytes in DISPLAY format
TCATBAL_LAYOUT = [
    ("TRANCAT_ACCT_ID",        "9",     11),
    ("TRANCAT_TYPE_CD",        "X",      2),
    ("TRANCAT_CD",             "9",      4),
    ("TRAN_CAT_BAL",           "S9V2",  11),  # S9(09)V99
    ("FILLER",                 "FILLER", 22),
]
# Total: 11+2+4+11+22 = 50

# CVTRA02Y — Disclosure Group (RECLN 50)
# PIC S9(04)V99 = 6 bytes in DISPLAY format
DISCGRP_LAYOUT = [
    ("DIS_ACCT_GROUP_ID",      "X",     10),
    ("DIS_TRAN_TYPE_CD",       "X",      2),
    ("DIS_TRAN_CAT_CD",        "9",      4),
    ("DIS_INT_RATE",           "S9V2",   6),  # S9(04)V99
    ("FILLER",                 "FILLER", 28),
]
# Total: 10+2+4+6+28 = 50

# CVTRA03Y — Transaction Type (RECLN 60)
TRANTYPE_LAYOUT = [
    ("TRAN_TYPE",              "X",      2),
    ("TRAN_TYPE_DESC",         "X",     50),
    ("FILLER",                 "FILLER",  8),
]

# CVTRA04Y — Transaction Category (RECLN 60)
TRANCATG_LAYOUT = [
    ("TRAN_TYPE_CD",           "X",      2),
    ("TRAN_CAT_CD",            "9",      4),
    ("TRAN_CAT_TYPE_DESC",     "X",     50),
    ("FILLER",                 "FILLER",  4),
]

# Map data file names to their layouts
FILE_LAYOUTS = {
    "acctdata":  ACCTDATA_LAYOUT,
    "carddata":  CARDDATA_LAYOUT,
    "cardxref":  CARDXREF_LAYOUT,
    "custdata":  CUSTDATA_LAYOUT,
    "dailytran": DAILYTRAN_LAYOUT,
    "discgrp":   DISCGRP_LAYOUT,
    "tcatbal":   TCATBAL_LAYOUT,
    "trancatg":  TRANCATG_LAYOUT,
    "trantype":  TRANTYPE_LAYOUT,
}

# Map data file names to their copybook sources
FILE_COPYBOOKS = {
    "acctdata":  "CVACT01Y",
    "carddata":  "CVACT02Y",
    "cardxref":  "CVACT03Y",
    "custdata":  "CVCUS01Y",
    "dailytran": "CVTRA06Y",
    "discgrp":   "CVTRA02Y",
    "tcatbal":   "CVTRA01Y",
    "trancatg":  "CVTRA04Y",
    "trantype":  "CVTRA03Y",
}
