# CardDemo Data Dictionary

> **Source:** Copybook PIC clause analysis from `app/cpy/` and `app/cpy-bms/`
> **Format:** Business-friendly entity descriptions extracted from COBOL record layouts

---

## 1. Account Master &mdash; `CVACT01Y` (300 bytes)

**VSAM File:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` &nbsp;|&nbsp; **Key:** Account ID (11 digits)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Account ID | `ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Unique account identifier |
| Active Status | `ACCT-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 | Account status flag (Y/N) |
| Current Balance | `ACCT-CURR-BAL` | `PIC S9(10)V99` | Signed Decimal | 12.2 | Current account balance |
| Credit Limit | `ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | Signed Decimal | 12.2 | Maximum credit limit |
| Cash Credit Limit | `ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | Signed Decimal | 12.2 | Cash advance credit limit |
| Open Date | `ACCT-OPEN-DATE` | `PIC X(10)` | Date String | 10 | Account opening date |
| Expiration Date | `ACCT-EXPIRAION-DATE` | `PIC X(10)` | Date String | 10 | Account expiration date |
| Reissue Date | `ACCT-REISSUE-DATE` | `PIC X(10)` | Date String | 10 | Last reissue date |
| Current Cycle Credit | `ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | Signed Decimal | 12.2 | Credits in current billing cycle |
| Current Cycle Debit | `ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | Signed Decimal | 12.2 | Debits in current billing cycle |
| Group ID | `ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 | Disclosure/rate group assignment |
| Filler | `FILLER` | `PIC X(178)` | Padding | 178 | Reserved for future use |

---

## 2. Card Data &mdash; `CVACT02Y` (150 bytes)

**VSAM File:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` &nbsp;|&nbsp; **Key:** Card Number (16 digits)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Card Number | `CARD-NUM` | `PIC X(16)` | Alphanumeric | 16 | Credit card number (primary key) |
| Account ID | `CARD-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Parent account reference |
| CVV Code | `CARD-CVV-CD` | `PIC 9(03)` | Numeric | 3 | Card verification value |
| Embossed Name | `CARD-EMBOSSED-NAME` | `PIC X(50)` | Alpha | 50 | Name printed on card |
| Expiration Date | `CARD-EXPIRAION-DATE` | `PIC X(10)` | Date String | 10 | Card expiration date |
| Active Status | `CARD-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 | Card status (Y/N) |
| Filler | `FILLER` | `PIC X(59)` | Padding | 59 | Reserved for future use |

---

## 3. Customer &mdash; `CVCUS01Y` (500 bytes)

**VSAM File:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` &nbsp;|&nbsp; **Key:** Customer ID (9 digits)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Customer ID | `CUST-ID` | `PIC 9(09)` | Numeric | 9 | Unique customer identifier |
| First Name | `CUST-FIRST-NAME` | `PIC X(25)` | Alpha | 25 | Customer first name |
| Middle Name | `CUST-MIDDLE-NAME` | `PIC X(25)` | Alpha | 25 | Customer middle name |
| Last Name | `CUST-LAST-NAME` | `PIC X(25)` | Alpha | 25 | Customer last name |
| Address Line 1 | `CUST-ADDR-LINE-1` | `PIC X(50)` | Alpha | 50 | Street address line 1 |
| Address Line 2 | `CUST-ADDR-LINE-2` | `PIC X(50)` | Alpha | 50 | Street address line 2 |
| Address Line 3 | `CUST-ADDR-LINE-3` | `PIC X(50)` | Alpha | 50 | Street address line 3 |
| State/Province | `CUST-ADDR-STATE-CD` | `PIC X(02)` | Alpha | 2 | Two-letter state code |
| Country | `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | Alpha | 3 | Three-letter country code |
| ZIP Code | `CUST-ADDR-ZIP` | `PIC X(10)` | Alpha | 10 | Postal/ZIP code |
| Phone 1 | `CUST-PHONE-NUM-1` | `PIC X(15)` | Alpha | 15 | Primary phone number |
| Phone 2 | `CUST-PHONE-NUM-2` | `PIC X(15)` | Alpha | 15 | Secondary phone number |
| SSN | `CUST-SSN` | `PIC 9(09)` | Numeric | 9 | Social Security Number |
| Govt ID | `CUST-GOVT-ISSUED-ID` | `PIC X(20)` | Alpha | 20 | Government-issued ID |
| Date of Birth | `CUST-DOB-YYYYMMDD` | `PIC X(10)` | Date String | 10 | Date of birth |
| EFT Account ID | `CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | Alpha | 10 | Electronic fund transfer account |
| Primary Card Holder | `CUST-PRI-CARD-HOLDER-IND` | `PIC X(01)` | Alpha | 1 | Primary cardholder indicator (Y/N) |
| FICO Score | `CUST-FICO-CREDIT-SCORE` | `PIC 9(03)` | Numeric | 3 | FICO credit score |
| Filler | `FILLER` | `PIC X(168)` | Padding | 168 | Reserved for future use |

---

## 4. Card Cross-Reference &mdash; `CVACT03Y` (50 bytes)

**VSAM File:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` &nbsp;|&nbsp; **Key:** Card Number (16 digits)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Card Number | `XREF-CARD-NUM` | `PIC X(16)` | Alphanumeric | 16 | Card number (primary key) |
| Customer ID | `XREF-CUST-ID` | `PIC 9(09)` | Numeric | 9 | Owning customer ID |
| Account ID | `XREF-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Associated account ID |
| Filler | `FILLER` | `PIC X(14)` | Padding | 14 | Reserved |

> **Alternate Index:** On `XREF-ACCT-ID` (non-unique) via `CARDXREF.VSAM.AIX`

---

## 5. Transaction Record &mdash; `CVTRA05Y` (350 bytes)

**VSAM File:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` &nbsp;|&nbsp; **Key:** Transaction ID (16 chars)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Transaction ID | `TRAN-ID` | `PIC X(16)` | Alphanumeric | 16 | Unique transaction identifier |
| Type Code | `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type (e.g., SA=Sale, CR=Credit) |
| Category Code | `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category |
| Source | `TRAN-SOURCE` | `PIC X(10)` | Alpha | 10 | Transaction origination source |
| Description | `TRAN-DESC` | `PIC X(100)` | Alpha | 100 | Free-text transaction description |
| Amount | `TRAN-AMT` | `PIC S9(09)V99` | Signed Decimal | 11.2 | Transaction amount |
| Merchant ID | `TRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant identifier |
| Merchant Name | `TRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant business name |
| Merchant City | `TRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant city |
| Merchant ZIP | `TRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant postal code |
| Card Number | `TRAN-CARD-NUM` | `PIC X(16)` | Alphanumeric | 16 | Card used for transaction |
| Origination Timestamp | `TRAN-ORIG-TS` | `PIC X(26)` | Timestamp | 26 | When transaction originated |
| Processing Timestamp | `TRAN-PROC-TS` | `PIC X(26)` | Timestamp | 26 | When transaction was processed |
| Filler | `FILLER` | `PIC X(20)` | Padding | 20 | Reserved |

---

## 6. Daily Transaction &mdash; `CVTRA06Y` (350 bytes)

**VSAM File:** `AWS.M2.CARDDEMO.DALYTRAN.PS` (sequential flat file)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Transaction ID | `DALYTRAN-ID` | `PIC X(16)` | Alphanumeric | 16 | Daily transaction ID |
| Type Code | `DALYTRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type code |
| Category Code | `DALYTRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category code |
| Source | `DALYTRAN-SOURCE` | `PIC X(10)` | Alpha | 10 | Origination source |
| Description | `DALYTRAN-DESC` | `PIC X(100)` | Alpha | 100 | Transaction description |
| Amount | `DALYTRAN-AMT` | `PIC S9(09)V99` | Signed Decimal | 11.2 | Transaction amount |
| Merchant ID | `DALYTRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant ID |
| Merchant Name | `DALYTRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant name |
| Merchant City | `DALYTRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant city |
| Merchant ZIP | `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP |
| Card Number | `DALYTRAN-CARD-NUM` | `PIC X(16)` | Alphanumeric | 16 | Card number |
| Origination Timestamp | `DALYTRAN-ORIG-TS` | `PIC X(26)` | Timestamp | 26 | Origination time |
| Processing Timestamp | `DALYTRAN-PROC-TS` | `PIC X(26)` | Timestamp | 26 | Processing time |
| Filler | `FILLER` | `PIC X(20)` | Padding | 20 | Reserved |

---

## 7. User Security &mdash; `CSUSR01Y` (80 bytes)

**VSAM File:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` &nbsp;|&nbsp; **Key:** User ID (8 chars)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| User ID | `SEC-USR-ID` | `PIC X(08)` | Alpha | 8 | Login user identifier |
| First Name | `SEC-USR-FNAME` | `PIC X(20)` | Alpha | 20 | User first name |
| Last Name | `SEC-USR-LNAME` | `PIC X(20)` | Alpha | 20 | User last name |
| Password | `SEC-USR-PWD` | `PIC X(08)` | Alpha | 8 | Login password (plaintext) |
| User Type | `SEC-USR-TYPE` | `PIC X(01)` | Alpha | 1 | Role: A=Admin, U=Regular User |
| Filler | `SEC-USR-FILLER` | `PIC X(23)` | Padding | 23 | Reserved |

---

## 8. Transaction Category Balance &mdash; `CVTRA01Y` (50 bytes)

**VSAM File:** `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` &nbsp;|&nbsp; **Key:** Account ID + Type + Category (17 bytes)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Account ID | `TRANCAT-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Account identifier |
| Type Code | `TRANCAT-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type |
| Category Code | `TRANCAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category |
| Balance | `TRAN-CAT-BAL` | `PIC S9(09)V99` | Signed Decimal | 11.2 | Running balance for this category |
| Filler | `FILLER` | `PIC X(22)` | Padding | 22 | Reserved |

---

## 9. Disclosure Group &mdash; `CVTRA02Y` (50 bytes)

**VSAM File:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` &nbsp;|&nbsp; **Key:** Group ID + Type + Category (16 bytes)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Account Group ID | `DIS-ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 | Disclosure group identifier |
| Transaction Type Code | `DIS-TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type |
| Transaction Category Code | `DIS-TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category |
| Interest Rate | `DIS-INT-RATE` | `PIC S9(04)V99` | Signed Decimal | 6.2 | Interest rate percentage |
| Filler | `FILLER` | `PIC X(28)` | Padding | 28 | Reserved |

---

## 10. Transaction Type &mdash; `CVTRA03Y` (60 bytes)

**VSAM File:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` &nbsp;|&nbsp; **Key:** Type Code (2 chars)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Type Code | `TRAN-TYPE` | `PIC X(02)` | Alpha | 2 | Transaction type code (SA, CR, etc.) |
| Description | `TRAN-TYPE-DESC` | `PIC X(50)` | Alpha | 50 | Human-readable type description |
| Filler | `FILLER` | `PIC X(08)` | Padding | 8 | Reserved |

---

## 11. Transaction Category &mdash; `CVTRA04Y` (60 bytes)

**VSAM File:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` &nbsp;|&nbsp; **Key:** Type + Category (6 bytes)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Type Code | `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Parent transaction type |
| Category Code | `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Category code |
| Description | `TRAN-CAT-TYPE-DESC` | `PIC X(50)` | Alpha | 50 | Category description |
| Filler | `FILLER` | `PIC X(04)` | Padding | 4 | Reserved |

---

## 12. Statement Transaction Layout &mdash; `COSTM01` (350 bytes)

**Used by:** CBSTM03A (statement generation) &mdash; re-keyed transaction record

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Card Number | `TRNX-CARD-NUM` | `PIC X(16)` | Alphanumeric | 16 | Card number (first part of composite key) |
| Transaction ID | `TRNX-ID` | `PIC X(16)` | Alphanumeric | 16 | Transaction ID (second part of key) |
| Type Code | `TRNX-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type |
| Category Code | `TRNX-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category |
| Source | `TRNX-SOURCE` | `PIC X(10)` | Alpha | 10 | Transaction source |
| Description | `TRNX-DESC` | `PIC X(100)` | Alpha | 100 | Description |
| Amount | `TRNX-AMT` | `PIC S9(09)V99` | Signed Decimal | 11.2 | Amount |
| Merchant ID | `TRNX-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant ID |
| Merchant Name | `TRNX-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant name |
| Merchant City | `TRNX-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant city |
| Merchant ZIP | `TRNX-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP |
| Origination Timestamp | `TRNX-ORIG-TS` | `PIC X(26)` | Timestamp | 26 | Origination time |
| Processing Timestamp | `TRNX-PROC-TS` | `PIC X(26)` | Timestamp | 26 | Processing time |
| Filler | `FILLER` | `PIC X(20)` | Padding | 20 | Reserved |

---

## 13. Export Record &mdash; `CVEXPORT` (variable)

**Used by:** CBEXPORT / CBIMPORT programs for data migration

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Record Type | `EXP-REC-TYPE` | `PIC X(01)` | Alpha | 1 | Record type identifier |
| Card Number | `EXP-CARD-NUM` | `PIC X(16)` | Alphanumeric | 16 | Card number |
| Account ID | `EXP-CARD-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Account ID |
| CVV Code | `EXP-CARD-CVV-CD` | `PIC 9(03) COMP` | Binary | 2 | Card verification value |
| Embossed Name | `EXP-CARD-EMBOSSED-NAME` | `PIC X(50)` | Alpha | 50 | Name on card |
| Expiration Date | `EXP-CARD-EXPIRAION-DATE` | `PIC X(10)` | Date String | 10 | Expiration date |
| Active Status | `EXP-CARD-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 | Card active flag |
| Filler | `FILLER` | `PIC X(373)` | Padding | 373 | Reserved |

---

## 14. Common Communication Area &mdash; `COCOM01Y`

**Used by:** All online CICS programs for inter-program data passing via COMMAREA

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Last Program | `CDEMO-FROM-PROGRAM` | `PIC X(08)` | Alpha | 8 | Calling program name |
| Last Map | `CDEMO-FROM-MAPSET` | `PIC X(07)` | Alpha | 7 | Calling BMS mapset |
| To Program | `CDEMO-TO-PROGRAM` | `PIC X(08)` | Alpha | 8 | Target program for XCTL |
| To Map | `CDEMO-TO-MAPSET` | `PIC X(07)` | Alpha | 7 | Target BMS mapset |
| User ID | `CDEMO-USER-ID` | `PIC X(08)` | Alpha | 8 | Logged-in user |
| User Type | `CDEMO-USER-TYPE` | `PIC X(01)` | Alpha | 1 | User role (A/U) |
| Account ID | `CDEMO-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Current account context |
| Card Number | `CDEMO-CARD-NUM` | `PIC X(16)` | Alphanumeric | 16 | Current card context |
| Last Mapset | `CDEMO-LAST-MAPSET` | `PIC X(07)` | Alpha | 7 | Previous mapset visited |
| Last Map | `CDEMO-LAST-MAP` | `PIC X(07)` | Alpha | 7 | Previous map visited |
| Error Message | `CCARD-ERROR-MSG` | `PIC X(75)` | Alpha | 75 | Error/info message to display |
| PF Key | `CCARD-AID-KEY` | (various) | Alpha | varies | Function key pressed |

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)
  |
  |-- 1:N --> Card Cross-Reference (CVACT03Y)
  |               |
  |               |-- N:1 --> Account Master (CVACT01Y)
  |               |-- N:1 --> Card Data (CVACT02Y)
  |
  Account Master (CVACT01Y)
      |
      |-- 1:N --> Transaction Category Balance (CVTRA01Y)
      |               |
      |               |-- N:1 --> Disclosure Group (CVTRA02Y)
      |
      |-- via Card --> Transaction Record (CVTRA05Y)
                          |
                          |-- N:1 --> Transaction Type (CVTRA03Y)
                          |-- N:1 --> Transaction Category (CVTRA04Y)

  User Security (CSUSR01Y) -- standalone authentication table
```

### VSAM File Catalog

| VSAM Dataset | Key | Rec Len | Copybook | Entity |
|-------------|-----|--------:|----------|--------|
| `ACCTDATA.VSAM.KSDS` | Account ID (11) | 300 | CVACT01Y | Account Master |
| `CARDDATA.VSAM.KSDS` | Card Num (16) | 150 | CVACT02Y | Card Data |
| `CUSTDATA.VSAM.KSDS` | Customer ID (9) | 500 | CVCUS01Y | Customer |
| `CARDXREF.VSAM.KSDS` | Card Num (16) | 50 | CVACT03Y | Card Cross-Reference |
| `TRANSACT.VSAM.KSDS` | Tran ID (16) | 350 | CVTRA05Y | Transaction Master |
| `DALYTRAN.PS` | Sequential | 350 | CVTRA06Y | Daily Transaction Input |
| `USRSEC.VSAM.KSDS` | User ID (8) | 80 | CSUSR01Y | User Security |
| `TCATBALF.VSAM.KSDS` | Acct+Type+Cat (17) | 50 | CVTRA01Y | Category Balance |
| `DISCGRP.VSAM.KSDS` | Group+Type+Cat (16) | 50 | CVTRA02Y | Disclosure Group |
| `TRANTYPE.VSAM.KSDS` | Type Code (2) | 60 | CVTRA03Y | Transaction Type |
| `TRANCATG.VSAM.KSDS` | Type+Cat (6) | 60 | CVTRA04Y | Transaction Category |
