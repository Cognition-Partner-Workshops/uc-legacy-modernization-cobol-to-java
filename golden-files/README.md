# Golden Reference Files

Structured JSON representations of the ASCII data files in `app/data/ASCII/`,
parsed using the field layouts defined in the COBOL copybooks (`app/cpy/`).

## Files

| JSON File | Source Data | Copybook | Records | Key Field |
|---|---|---|---|---|
| `acctdata.json` | `acctdata.txt` | `CVACT01Y` | 50 | `ACCT-ID` (PIC 9(11)) |
| `carddata.json` | `carddata.txt` | `CVACT02Y` | 50 | `CARD-NUM` (PIC X(16)) |
| `cardxref.json` | `cardxref.txt` | `CVACT03Y` | 50 | `XREF-CARD-NUM` (PIC X(16)) |
| `custdata.json` | `custdata.txt` | `CVCUS01Y` | 50 | `CUST-ID` (PIC 9(09)) |
| `dailytran.json` | `dailytran.txt` | `CVTRA06Y` | 300 | `DALYTRAN-ID` (PIC X(16)) |
| `discgrp.json` | `discgrp.txt` | `CVTRA02Y` | 51 | Composite: group+type+cat |
| `tcatbal.json` | `tcatbal.txt` | `CVTRA01Y` | 50 | Composite: acct+type+cat |
| `trancatg.json` | `trancatg.txt` | `CVTRA04Y` | 18 | Composite: type+cat |
| `trantype.json` | `trantype.txt` | `CVTRA03Y` | 7 | `TRAN-TYPE` (PIC X(02)) |

## Field Definitions

### acctdata.json — Account Master (CVACT01Y, 300 bytes)

| Field | PIC | Type | Description |
|---|---|---|---|
| `ACCT-ID` | 9(11) | Numeric | Unique account identifier |
| `ACCT-ACTIVE-STATUS` | X(01) | Alpha | Account status: Y=active, N=inactive |
| `ACCT-CURR-BAL` | S9(10)V99 | Signed decimal | Current account balance |
| `ACCT-CREDIT-LIMIT` | S9(10)V99 | Signed decimal | Total credit limit |
| `ACCT-CASH-CREDIT-LIMIT` | S9(10)V99 | Signed decimal | Cash advance limit |
| `ACCT-OPEN-DATE` | X(10) | Date string | Account open date (YYYY-MM-DD) |
| `ACCT-EXPIRAION-DATE` | X(10) | Date string | Account expiration date |
| `ACCT-REISSUE-DATE` | X(10) | Date string | Card reissue date |
| `ACCT-CURR-CYC-CREDIT` | S9(10)V99 | Signed decimal | Current cycle credit total |
| `ACCT-CURR-CYC-DEBIT` | S9(10)V99 | Signed decimal | Current cycle debit total |
| `ACCT-ADDR-ZIP` | X(10) | Alpha | Billing zip code |
| `ACCT-GROUP-ID` | X(10) | Alpha | Disclosure group identifier |

### carddata.json — Card Master (CVACT02Y, 150 bytes)

| Field | PIC | Type | Description |
|---|---|---|---|
| `CARD-NUM` | X(16) | Alpha | Card number (primary key) |
| `CARD-ACCT-ID` | 9(11) | Numeric | Linked account ID |
| `CARD-CVV-CD` | 9(03) | Numeric | Card verification value |
| `CARD-EMBOSSED-NAME` | X(50) | Alpha | Cardholder name on card |
| `CARD-EXPIRAION-DATE` | X(10) | Date string | Card expiration date |
| `CARD-ACTIVE-STATUS` | X(01) | Alpha | Card status: Y=active, N=inactive |

### cardxref.json — Card Cross-Reference (CVACT03Y, 50 bytes)

| Field | PIC | Type | Description |
|---|---|---|---|
| `XREF-CARD-NUM` | X(16) | Alpha | Card number (FK → carddata) |
| `XREF-CUST-ID` | 9(09) | Numeric | Customer ID (FK → custdata) |
| `XREF-ACCT-ID` | 9(11) | Numeric | Account ID (FK → acctdata) |

### custdata.json — Customer Master (CVCUS01Y, 500 bytes)

| Field | PIC | Type | Description |
|---|---|---|---|
| `CUST-ID` | 9(09) | Numeric | Unique customer identifier |
| `CUST-FIRST-NAME` | X(25) | Alpha | First name |
| `CUST-MIDDLE-NAME` | X(25) | Alpha | Middle name |
| `CUST-LAST-NAME` | X(25) | Alpha | Last name |
| `CUST-ADDR-LINE-1` | X(50) | Alpha | Address line 1 |
| `CUST-ADDR-LINE-2` | X(50) | Alpha | Address line 2 |
| `CUST-ADDR-LINE-3` | X(50) | Alpha | Address line 3 |
| `CUST-ADDR-STATE-CD` | X(02) | Alpha | State code |
| `CUST-ADDR-COUNTRY-CD` | X(03) | Alpha | Country code |
| `CUST-ADDR-ZIP` | X(10) | Alpha | Zip code |
| `CUST-PHONE-NUM-1` | X(15) | Alpha | Primary phone |
| `CUST-PHONE-NUM-2` | X(15) | Alpha | Secondary phone |
| `CUST-SSN` | 9(09) | Numeric | Social Security Number |
| `CUST-GOVT-ISSUED-ID` | X(20) | Alpha | Government-issued ID |
| `CUST-DOB-YYYY-MM-DD` | X(10) | Date string | Date of birth |
| `CUST-EFT-ACCOUNT-ID` | X(10) | Alpha | EFT account identifier |
| `CUST-PRI-CARD-HOLDER-IND` | X(01) | Alpha | Primary cardholder indicator |
| `CUST-FICO-CREDIT-SCORE` | 9(03) | Numeric | FICO credit score |

### dailytran.json — Daily Transactions (CVTRA06Y, 350 bytes)

| Field | PIC | Type | Description |
|---|---|---|---|
| `DALYTRAN-ID` | X(16) | Alpha | Transaction identifier |
| `DALYTRAN-TYPE-CD` | X(02) | Alpha | Transaction type code (FK → trantype) |
| `DALYTRAN-CAT-CD` | 9(04) | Numeric | Transaction category code |
| `DALYTRAN-SOURCE` | X(10) | Alpha | Transaction source |
| `DALYTRAN-DESC` | X(100) | Alpha | Transaction description |
| `DALYTRAN-AMT` | S9(09)V99 | Signed decimal | Transaction amount |
| `DALYTRAN-MERCHANT-ID` | 9(09) | Numeric | Merchant identifier |
| `DALYTRAN-MERCHANT-NAME` | X(50) | Alpha | Merchant name |
| `DALYTRAN-MERCHANT-CITY` | X(50) | Alpha | Merchant city |
| `DALYTRAN-MERCHANT-ZIP` | X(10) | Alpha | Merchant zip code |
| `DALYTRAN-CARD-NUM` | X(16) | Alpha | Card number (FK → cardxref) |
| `DALYTRAN-ORIG-TS` | X(26) | Timestamp | Origination timestamp |
| `DALYTRAN-PROC-TS` | X(26) | Timestamp | Processing timestamp |

### discgrp.json — Disclosure Groups (CVTRA02Y, 50 bytes)

| Field | PIC | Type | Description |
|---|---|---|---|
| `DIS-ACCT-GROUP-ID` | X(10) | Alpha | Account group identifier |
| `DIS-TRAN-TYPE-CD` | X(02) | Alpha | Transaction type code |
| `DIS-TRAN-CAT-CD` | 9(04) | Numeric | Transaction category code |
| `DIS-INT-RATE` | S9(04)V99 | Signed decimal | Annual interest rate |

### tcatbal.json — Transaction Category Balance (CVTRA01Y, 50 bytes)

| Field | PIC | Type | Description |
|---|---|---|---|
| `TRANCAT-ACCT-ID` | 9(11) | Numeric | Account identifier |
| `TRANCAT-TYPE-CD` | X(02) | Alpha | Transaction type code |
| `TRANCAT-CD` | 9(04) | Numeric | Transaction category code |
| `TRAN-CAT-BAL` | S9(09)V99 | Signed decimal | Accumulated balance for this category |

### trancatg.json — Transaction Category Types (CVTRA04Y, 60 bytes)

| Field | PIC | Type | Description |
|---|---|---|---|
| `TRAN-TYPE-CD` | X(02) | Alpha | Transaction type code |
| `TRAN-CAT-CD` | 9(04) | Numeric | Transaction category code |
| `TRAN-CAT-TYPE-DESC` | X(50) | Alpha | Category description |

### trantype.json — Transaction Types (CVTRA03Y, 60 bytes)

| Field | PIC | Type | Description |
|---|---|---|---|
| `TRAN-TYPE` | X(02) | Alpha | Transaction type code (primary key) |
| `TRAN-TYPE-DESC` | X(50) | Alpha | Transaction type description |

## Regeneration

To regenerate golden files from the source data:

```bash
cd test-harness
python generate_golden_files.py
```
