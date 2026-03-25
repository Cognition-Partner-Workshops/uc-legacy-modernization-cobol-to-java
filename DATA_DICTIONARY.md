# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Source:** Copybook PIC clause analysis from `app/cpy/` and `app/cpy-bms/`

---

## Overview

This data dictionary extracts every business entity defined in the CardDemo copybooks and translates COBOL PIC clauses into a business-friendly format. Each entity maps directly to a VSAM file (or CICS COMMAREA) and will become a Java POJO / database table during modernization.

---

## 1. Account Master (`CVACT01Y`)

**VSAM File:** `ACCTDATA.VSAM.KSDS` | **Record Length:** 300 bytes | **Key:** Account ID (11 digits)

| Field Name | COBOL PIC | Data Type | Size | Business Description |
|------------|-----------|-----------|------|---------------------|
| ACCT-ID | 9(11) | Numeric | 11 | Unique account identifier |
| ACCT-ACTIVE-STATUS | X(01) | Text | 1 | Account status flag (Y=Active) |
| ACCT-CURR-BAL | S9(10)V99 | Signed Decimal | 12.2 | Current account balance |
| ACCT-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12.2 | Credit limit amount |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12.2 | Cash advance credit limit |
| ACCT-OPEN-DATE | X(10) | Text/Date | 10 | Account opening date |
| ACCT-EXPIRAION-DATE | X(10) | Text/Date | 10 | Account expiration date |
| ACCT-REISSUE-DATE | X(10) | Text/Date | 10 | Last card reissue date |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | Signed Decimal | 12.2 | Current cycle credit total |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | Signed Decimal | 12.2 | Current cycle debit total |
| ACCT-ADDR-ZIP | X(10) | Text | 10 | Account holder ZIP code |
| ACCT-GROUP-ID | X(10) | Text | 10 | Disclosure group ID (links to interest rates) |
| FILLER | X(178) | — | 178 | Reserved space |

**Business Rules:**
- Account balance = prior balance + debits − credits + interest
- Credit limit determines maximum allowed balance
- Group ID links to disclosure group for interest rate determination

---

## 2. Card Data (`CVACT02Y`)

**VSAM File:** `CARDDATA.VSAM.KSDS` | **Record Length:** 150 bytes | **Key:** Card Number (16 chars)

| Field Name | COBOL PIC | Data Type | Size | Business Description |
|------------|-----------|-----------|------|---------------------|
| ACCT-CARD-NUM | X(16) | Text | 16 | Credit card number (primary key) |
| ACCT-ID | 9(11) | Numeric | 11 | Parent account ID (foreign key) |
| CARD-ACCT-ID | 9(11) | Numeric | 11 | Account ID (alternate reference) |
| CARD-CVV-CD | 9(03) | Numeric | 3 | Card CVV security code |
| CARD-EMBOSSED-NAME | X(50) | Text | 50 | Name printed on card |
| CARD-EXPIRAION-DATE | X(10) | Text/Date | 10 | Card expiration date |
| CARD-ACTIVE-STATUS | X(01) | Text | 1 | Card status (Y=Active, N=Inactive) |
| FILLER | X(49) | — | 49 | Reserved space |

**Business Rules:**
- Multiple cards can be linked to a single account
- Card status must be Active for transaction processing
- CVV code is used for card-not-present transaction verification

---

## 3. Card Detail — Extended (`CVCRD01Y`)

**Used by:** Card view/update programs | **Extends:** CVACT02Y with additional display fields

| Field Name | COBOL PIC | Data Type | Size | Business Description |
|------------|-----------|-----------|------|---------------------|
| CRDSL-ID | X(16) | Text | 16 | Card number |
| CRDSL-ACCT-ID | 9(11) | Numeric | 11 | Account ID |
| CRDSL-CVV-CD | 9(03) COMP | Numeric (binary) | 3 | CVV code (packed) |
| CRDSL-EMBOSSED-NAME | X(50) | Text | 50 | Embossed name on card |
| CRDSL-EXPIRAION-DATE | X(10) | Text/Date | 10 | Expiration date |
| CRDSL-ACTIVE-STATUS | X(01) | Text | 1 | Active status flag |

---

## 4. Card Cross-Reference (`CVACT03Y`)

**VSAM File:** `CARDXREF.VSAM.KSDS` | **Record Length:** 50 bytes | **Key:** Card Number (16 chars) | **Alternate Index:** Account ID

| Field Name | COBOL PIC | Data Type | Size | Business Description |
|------------|-----------|-----------|------|---------------------|
| XREF-CARD-NUM | X(16) | Text | 16 | Card number (primary key) |
| XREF-ACCT-ID | 9(11) | Numeric | 11 | Account ID (alternate key, non-unique) |
| XREF-CUST-ID | 9(09) | Numeric | 9 | Customer ID |
| FILLER | X(14) | — | 14 | Reserved space |

**Business Rules:**
- Provides fast lookup from card number to account and customer
- Alternate index allows lookup of all cards for a given account
- Central to transaction posting (card → account resolution)

---

## 5. Customer Master (`CVCUS01Y`)

**VSAM File:** `CUSTDATA.VSAM.KSDS` | **Record Length:** 500 bytes | **Key:** Customer ID (9 digits)

| Field Name | COBOL PIC | Data Type | Size | Business Description |
|------------|-----------|-----------|------|---------------------|
| CUST-ID | 9(09) | Numeric | 9 | Unique customer identifier |
| CUST-FIRST-NAME | X(25) | Text | 25 | Customer first name |
| CUST-MIDDLE-NAME | X(25) | Text | 25 | Customer middle name |
| CUST-LAST-NAME | X(25) | Text | 25 | Customer last name |
| CUST-ADDR-LINE-1 | X(50) | Text | 50 | Street address line 1 |
| CUST-ADDR-LINE-2 | X(50) | Text | 50 | Street address line 2 |
| CUST-ADDR-LINE-3 | X(50) | Text | 50 | Street address line 3 |
| CUST-ADDR-STATE-CD | X(02) | Text | 2 | State code |
| CUST-ADDR-COUNTRY-CD | X(03) | Text | 3 | Country code |
| CUST-ADDR-ZIP | X(10) | Text | 10 | ZIP/postal code |
| CUST-PHONE-NUM-1 | X(15) | Text | 15 | Primary phone number |
| CUST-PHONE-NUM-2 | X(15) | Text | 15 | Secondary phone number |
| CUST-SSN | 9(09) | Numeric | 9 | Social Security Number (PII) |
| CUST-GOVT-ISSUED-ID | X(20) | Text | 20 | Government-issued ID number |
| CUST-DOB-YYYYMMDD | X(10) | Text/Date | 10 | Date of birth |
| CUST-EFT-ACCOUNT-ID | X(10) | Text | 10 | Electronic funds transfer account |
| CUST-PRI-CARD-HOLDER-IND | X(01) | Text | 1 | Primary cardholder indicator (Y/N) |
| CUST-FICO-CREDIT-SCORE | 9(03) | Numeric | 3 | FICO credit score |
| FILLER | X(168) | — | 168 | Reserved space |

**Business Rules:**
- SSN and DOB are PII fields requiring encryption in modernized system
- FICO score used for credit decisions
- Primary cardholder indicator distinguishes account owner from authorized users

---

## 6. Transaction Record (`CVTRA05Y`)

**VSAM File:** `TRANSACT.VSAM.KSDS` | **Record Length:** 350 bytes | **Key:** Transaction ID (16 chars)

| Field Name | COBOL PIC | Data Type | Size | Business Description |
|------------|-----------|-----------|------|---------------------|
| TRAN-ID | X(16) | Text | 16 | Unique transaction identifier |
| TRAN-TYPE-CD | X(02) | Text | 2 | Transaction type code (FK → CVTRA03Y) |
| TRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code (FK → CVTRA04Y) |
| TRAN-SOURCE | X(10) | Text | 10 | Transaction source (POS, ATM, Online, etc.) |
| TRAN-DESC | X(100) | Text | 100 | Transaction description |
| TRAN-AMT | S9(09)V99 | Signed Decimal | 11.2 | Transaction amount |
| TRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant identifier |
| TRAN-MERCHANT-NAME | X(50) | Text | 50 | Merchant name |
| TRAN-MERCHANT-CITY | X(50) | Text | 50 | Merchant city |
| TRAN-MERCHANT-ZIP | X(10) | Text | 10 | Merchant ZIP code |
| TRAN-CARD-NUM | X(16) | Text | 16 | Card number used (FK → CVACT02Y) |
| TRAN-ORIG-TS | X(26) | Text/Timestamp | 26 | Original transaction timestamp |
| TRAN-PROC-TS | X(26) | Text/Timestamp | 26 | Processing timestamp |
| FILLER | X(20) | — | 20 | Reserved space |

**Business Rules:**
- Transactions link to cards via TRAN-CARD-NUM, then to accounts via cross-reference
- Amount is signed: negative = credit/refund, positive = debit/purchase
- Timestamps use 26-char format for microsecond precision

---

## 7. Daily Transaction Record (`CVTRA06Y`)

**VSAM File:** `DALYTRAN` dataset | **Record Length:** 350 bytes | **Identical structure to CVTRA05Y**

| Field Name | COBOL PIC | Data Type | Size | Business Description |
|------------|-----------|-----------|------|---------------------|
| DALYTRAN-ID | X(16) | Text | 16 | Daily transaction identifier |
| DALYTRAN-TYPE-CD | X(02) | Text | 2 | Transaction type code |
| DALYTRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| DALYTRAN-SOURCE | X(10) | Text | 10 | Transaction source |
| DALYTRAN-DESC | X(100) | Text | 100 | Transaction description |
| DALYTRAN-AMT | S9(09)V99 | Signed Decimal | 11.2 | Transaction amount |
| DALYTRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant identifier |
| DALYTRAN-MERCHANT-NAME | X(50) | Text | 50 | Merchant name |
| DALYTRAN-MERCHANT-CITY | X(50) | Text | 50 | Merchant city |
| DALYTRAN-MERCHANT-ZIP | X(10) | Text | 10 | Merchant ZIP code |
| DALYTRAN-CARD-NUM | X(16) | Text | 16 | Card number used |
| DALYTRAN-ORIG-TS | X(26) | Text/Timestamp | 26 | Original timestamp |
| DALYTRAN-PROC-TS | X(26) | Text/Timestamp | 26 | Processing timestamp |
| FILLER | X(20) | — | 20 | Reserved space |

**Business Rules:**
- Daily transactions are the staging area before posting to the master transaction file
- CBTRN02C reads daily transactions and posts them to the master
- After posting, daily file is archived and cleared

---

## 8. Statement Transaction Layout (`COSTM01`)

**Used by:** CBSTM03A/B (Statement generation) | **Record Length:** 350 bytes | **Key:** Card Number + Transaction ID (composite 32-byte key)

| Field Name | COBOL PIC | Data Type | Size | Business Description |
|------------|-----------|-----------|------|---------------------|
| TRNX-CARD-NUM | X(16) | Text | 16 | Card number (part of composite key) |
| TRNX-ID | X(16) | Text | 16 | Transaction ID (part of composite key) |
| TRNX-TYPE-CD | X(02) | Text | 2 | Transaction type code |
| TRNX-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| TRNX-SOURCE | X(10) | Text | 10 | Transaction source |
| TRNX-DESC | X(100) | Text | 100 | Transaction description |
| TRNX-AMT | S9(09)V99 | Signed Decimal | 11.2 | Transaction amount |
| TRNX-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant identifier |
| TRNX-MERCHANT-NAME | X(50) | Text | 50 | Merchant name |
| TRNX-MERCHANT-CITY | X(50) | Text | 50 | Merchant city |
| TRNX-MERCHANT-ZIP | X(10) | Text | 10 | Merchant ZIP code |
| TRNX-ORIG-TS | X(26) | Text/Timestamp | 26 | Original timestamp |
| TRNX-PROC-TS | X(26) | Text/Timestamp | 26 | Processing timestamp |
| FILLER | X(20) | — | 20 | Reserved space |

**Business Rules:**
- Re-keyed version of transaction data with card number as primary sort
- Used exclusively for statement generation (card-centric view of transactions)

---

## 9. Transaction Category Balance (`CVTRA01Y`)

**VSAM File:** `TCATBAL.VSAM.KSDS` | **Record Length:** 50 bytes | **Key:** Account ID + Type Code + Category Code

| Field Name | COBOL PIC | Data Type | Size | Business Description |
|------------|-----------|-----------|------|---------------------|
| TRANCAT-ACCT-ID | 9(11) | Numeric | 11 | Account identifier |
| TRANCAT-TYPE-CD | X(02) | Text | 2 | Transaction type code |
| TRANCAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| TRAN-CAT-BAL | S9(09)V99 | Signed Decimal | 11.2 | Accumulated balance for this category |
| FILLER | X(22) | — | 22 | Reserved space |

**Business Rules:**
- Maintains running balance per account per transaction category
- Used for interest calculation (different rates per category)
- Updated during daily transaction posting

---

## 10. Disclosure Group (`CVTRA02Y`)

**VSAM File:** `DISCGRP.VSAM.KSDS` | **Record Length:** 50 bytes | **Key:** Group ID + Type Code + Category Code

| Field Name | COBOL PIC | Data Type | Size | Business Description |
|------------|-----------|-----------|------|---------------------|
| DIS-ACCT-GROUP-ID | X(10) | Text | 10 | Account group identifier |
| DIS-TRAN-TYPE-CD | X(02) | Text | 2 | Transaction type code |
| DIS-TRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| DIS-INT-RATE | S9(04)V99 | Signed Decimal | 6.2 | Interest rate (annual %) |
| FILLER | X(28) | — | 28 | Reserved space |

**Business Rules:**
- Defines interest rates per disclosure group, type, and category
- Linked to accounts via ACCT-GROUP-ID in the account master
- Central to the interest calculation process (CBACT04C)

---

## 11. Transaction Type (`CVTRA03Y`)

**VSAM File:** `TRANTYPE.VSAM.KSDS` | **Record Length:** 60 bytes | **Key:** Type Code (2 chars)

| Field Name | COBOL PIC | Data Type | Size | Business Description |
|------------|-----------|-----------|------|---------------------|
| TRAN-TYPE | X(02) | Text | 2 | Transaction type code (e.g., "PR"=Purchase, "CA"=Cash Advance) |
| TRAN-TYPE-DESC | X(50) | Text | 50 | Human-readable type description |
| FILLER | X(08) | — | 8 | Reserved space |

---

## 12. Transaction Category (`CVTRA04Y`)

**VSAM File:** `TRANCATG.VSAM.KSDS` | **Record Length:** 60 bytes | **Key:** Type Code + Category Code

| Field Name | COBOL PIC | Data Type | Size | Business Description |
|------------|-----------|-----------|------|---------------------|
| TRAN-TYPE-CD | X(02) | Text | 2 | Transaction type code (part of key) |
| TRAN-CAT-CD | 9(04) | Numeric | 4 | Category code (part of key) |
| TRAN-CAT-TYPE-DESC | X(50) | Text | 50 | Category description |
| FILLER | X(04) | — | 4 | Reserved space |

---

## 13. User Security Record (`CSUSR01Y`)

**VSAM File:** `USRSEC.VSAM.KSDS` | **Record Length:** 80 bytes | **Key:** User ID (8 chars)

| Field Name | COBOL PIC | Data Type | Size | Business Description |
|------------|-----------|-----------|------|---------------------|
| SEC-USR-ID | X(08) | Text | 8 | User login ID (e.g., "ADMIN001", "USER0001") |
| SEC-USR-FNAME | X(20) | Text | 20 | User first name |
| SEC-USR-LNAME | X(20) | Text | 20 | User last name |
| SEC-USR-PWD | X(08) | Text | 8 | Password (plain text — security risk) |
| SEC-USR-TYPE | X(01) | Text | 1 | User type: A=Admin, U=Regular User |
| SEC-USR-FILLER | X(23) | — | 23 | Reserved space |

**Business Rules:**
- Admin users can access user management and admin menu
- Regular users access card/account/transaction functions only
- Password stored in plain text (critical modernization concern)

---

## 14. Report Structures (`CVTRA07Y`)

**Used by:** CBTRN03C (Daily Transaction Report) | **Not a VSAM record — print layout structures**

### Report Name Header
| Field Name | COBOL PIC | Size | Description |
|------------|-----------|------|-------------|
| REPT-SHORT-NAME | X(38) | 38 | Report short name ("DALYREPT") |
| REPT-LONG-NAME | X(41) | 41 | Report long name ("Daily Transaction Report") |
| REPT-DATE-HEADER | X(12) | 12 | "Date Range: " |
| REPT-START-DATE | X(10) | 10 | Report start date |
| REPT-END-DATE | X(10) | 10 | Report end date |

### Transaction Detail Line
| Field Name | COBOL PIC | Size | Description |
|------------|-----------|------|-------------|
| TRAN-REPORT-TRANS-ID | X(16) | 16 | Transaction ID |
| TRAN-REPORT-ACCOUNT-ID | X(11) | 11 | Account ID |
| TRAN-REPORT-TYPE-CD | X(02) | 2 | Type code |
| TRAN-REPORT-TYPE-DESC | X(15) | 15 | Type description |
| TRAN-REPORT-CAT-CD | 9(04) | 4 | Category code |
| TRAN-REPORT-CAT-DESC | X(29) | 29 | Category description |
| TRAN-REPORT-SOURCE | X(10) | 10 | Transaction source |
| TRAN-REPORT-AMT | -ZZZ,ZZZ,ZZZ.ZZ | 15 | Formatted amount |

### Report Totals
| Field Name | COBOL PIC | Description |
|------------|-----------|-------------|
| REPT-PAGE-TOTAL | +ZZZ,ZZZ,ZZZ.ZZ | Page-level subtotal |
| REPT-ACCOUNT-TOTAL | +ZZZ,ZZZ,ZZZ.ZZ | Account-level subtotal |
| REPT-GRAND-TOTAL | +ZZZ,ZZZ,ZZZ.ZZ | Grand total for report |

---

## 15. Export Record (`CVEXPORT`)

**Used by:** CBEXPORT / CBIMPORT programs | **Multi-entity export layout**

### Account Export Section
| Field Name | COBOL PIC | Size | Description |
|------------|-----------|------|-------------|
| EXP-ACCT-ID | 9(11) | 11 | Account ID |
| EXP-ACCT-ACTIVE-STATUS | X(01) | 1 | Status flag |
| EXP-ACCT-CURR-BAL | S9(10)V99 | 12 | Current balance |
| EXP-ACCT-CREDIT-LIMIT | S9(10)V99 | 12 | Credit limit |
| EXP-ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | 12 | Cash advance limit |
| EXP-ACCT-OPEN-DATE | X(10) | 10 | Open date |
| EXP-ACCT-EXPIRAION-DATE | X(10) | 10 | Expiration date |
| EXP-ACCT-REISSUE-DATE | X(10) | 10 | Reissue date |
| EXP-ACCT-CURR-CYC-CREDIT | S9(10)V99 | 12 | Cycle credits |
| EXP-ACCT-CURR-CYC-DEBIT | S9(10)V99 | 12 | Cycle debits |
| EXP-ACCT-GROUP-ID | X(10) | 10 | Group ID |
| EXP-ACCT-ADDR-ZIP | X(10) | 10 | ZIP code |

### Card Export Section
| Field Name | COBOL PIC | Size | Description |
|------------|-----------|------|-------------|
| EXP-CARD-NUM | X(16) | 16 | Card number |
| EXP-CARD-ACCT-ID | 9(11) | 11 | Account ID |
| EXP-CARD-CVV-CD | 9(03) COMP | 3 | CVV code |
| EXP-CARD-EMBOSSED-NAME | X(50) | 50 | Embossed name |
| EXP-CARD-EXPIRAION-DATE | X(10) | 10 | Expiration date |
| EXP-CARD-ACTIVE-STATUS | X(01) | 1 | Status flag |

---

## 16. Common Communication Area (`COCOM01Y`)

**Used by:** All online CICS programs | **Passed via CICS COMMAREA between programs**

| Field Name | COBOL PIC | Size | Business Description |
|------------|-----------|------|---------------------|
| CDEMO-FROM-TRANID | X(04) | 4 | Originating transaction ID |
| CDEMO-FROM-PROGRAM | X(08) | 8 | Originating program name |
| CDEMO-TO-TRANID | X(04) | 4 | Target transaction ID |
| CDEMO-TO-PROGRAM | X(08) | 8 | Target program name |
| CDEMO-PGM-REENTER | X(01) | 1 | Re-entry flag (Y/N) |
| CDEMO-USR-ID | X(08) | 8 | Current logged-in user ID |
| CDEMO-USR-TYP | X(01) | 1 | User type (A=Admin, U=User) |
| CDEMO-USR-FNAME | X(20) | 20 | User first name |
| CDEMO-USR-LNAME | X(20) | 20 | User last name |
| CDEMO-LAST-MAP | X(07) | 7 | Last BMS map sent |
| CDEMO-LAST-MAPSET | X(07) | 7 | Last BMS mapset sent |
| CDEMO-ACCT-ID | 9(11) | 11 | Current account context |
| CDEMO-CARD-NUM | X(16) | 16 | Current card context |
| CDEMO-CUST-ID | 9(09) | 9 | Current customer context |

**Business Rules:**
- COMMAREA is the session state mechanism for CICS pseudo-conversational programming
- Every XCTL (program transfer) passes this area to maintain context
- User credentials travel in the COMMAREA after sign-on

---

## 17. Menu Definitions (`COMEN02Y` / `COADM02Y`)

### Regular User Menu (`COMEN02Y`)

| Option | Program Name | Transaction | Screen Title |
|--------|-------------|-------------|--------------|
| 1 | COACTVWC | CA01 | Account View |
| 2 | COCRDLIC | CC01 | Card List |
| 3 | COTRN00C | CT00 | Transaction List |
| 4 | COBIL00C | CB00 | Bill Payment |
| 5 | CORPT00C | CR00 | Reports |

### Admin Menu (`COADM02Y`)

| Option | Program Name | Transaction | Screen Title |
|--------|-------------|-------------|--------------|
| 1 | COUSR00C | CU00 | User List |

---

## 18. Utility Copybooks

### Date Formatting (`CSDAT01Y`)
| Field Name | Purpose |
|------------|---------|
| WS-CURDATE-DATA | Current date storage (YYYYMMDD) |
| WS-CURDATE-MONTH / DAY / YEAR | Formatted date components |
| WS-CURTIME-DATA | Current time storage (HHMMSS) |
| WS-CURTIME-HOURS / MINS / SECS | Formatted time components |

### Date Conversion (`CODATECN`)
| Field Name | Purpose |
|------------|---------|
| CODATECN-YYYYMMDD | Input date in YYYYMMDD format |
| CODATECN-MMDDYYYY | Output date in MMDDYYYY format |
| CODATECN-CC / YY / MM / DD | Individual date components |

### Message Areas (`CSMSG01Y` / `CSMSG02Y`)
| Field Name | Purpose |
|------------|---------|
| CSMSG-INFORMATIONAL | Informational message text |
| CSMSG-THANK-YOU | Standard sign-off message |

### Screen Attribute Setting (`CSSETATY`)
| Field Name | Purpose |
|------------|---------|
| WS-SET-ATTR-* | Screen field attribute manipulation (BRT/NORM/DRK) |

### Lookup Code (`CSLKPCDY`)
| Field Name | Purpose |
|------------|---------|
| CDEMO-LOOKUP-CODE | Generic lookup code working storage |

---

## Entity Relationship Summary

```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐
│  Customer    │────▶│  Account     │────▶│  Card        │
│  CVCUS01Y    │ 1:N │  CVACT01Y    │ 1:N │  CVACT02Y    │
│  (500 bytes) │     │  (300 bytes) │     │  (150 bytes) │
└─────────────┘     └──────┬───────┘     └──────┬───────┘
                           │                     │
                    ┌──────▼───────┐     ┌───────▼──────┐
                    │  Cat Balance │     │  Cross-Ref   │
                    │  CVTRA01Y    │     │  CVACT03Y    │
                    │  (50 bytes)  │     │  (50 bytes)  │
                    └──────────────┘     └──────┬───────┘
                                                │
┌──────────────┐    ┌──────────────┐     ┌──────▼───────┐
│ Disclosure   │    │ Trans Type   │────▶│ Transaction  │
│ Group        │    │ CVTRA03Y     │     │ CVTRA05Y     │
│ CVTRA02Y     │    │ (60 bytes)   │     │ (350 bytes)  │
│ (50 bytes)   │    └──────────────┘     └──────────────┘
└──────────────┘           │
                    ┌──────▼───────┐     ┌──────────────┐
                    │ Trans Cat    │     │  User Sec    │
                    │ CVTRA04Y     │     │  CSUSR01Y    │
                    │ (60 bytes)   │     │  (80 bytes)  │
                    └──────────────┘     └──────────────┘
```

---

## VSAM File Catalog

| Logical Name | Dataset Name | Organization | Record Size | Key | Key Length | Key Offset |
|-------------|-------------|--------------|-------------|-----|-----------|-----------|
| USRSEC | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | KSDS | 80 | User ID | 8 | 0 |
| ACCTDATA | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | KSDS | 300 | Account ID | 11 | 0 |
| CARDDATA | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | KSDS | 150 | Card Number | 16 | 0 |
| CUSTDATA | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | KSDS | 500 | Customer ID | 9 | 0 |
| CARDXREF | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | KSDS | 50 | Card Number | 16 | 0 |
| TRANSACT | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | KSDS | 350 | Transaction ID | 16 | 0 |
| DALYTRAN | AWS.M2.CARDDEMO.DALYTRAN.VSAM.KSDS | KSDS | 350 | Transaction ID | 16 | 0 |
| TRANTYPE | AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | KSDS | 60 | Type Code | 2 | 0 |
| TRANCATG | AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | KSDS | 60 | Type+Cat Code | 6 | 0 |
| TCATBAL | AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS | KSDS | 50 | Acct+Type+Cat | 17 | 0 |
| DISCGRP | AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | KSDS | 50 | Group+Type+Cat | 16 | 0 |

---

## Modernization Mapping

| COBOL Entity | Copybook | Java Class (suggested) | Database Table (suggested) |
|-------------|----------|----------------------|--------------------------|
| Account Master | CVACT01Y | `Account.java` | `accounts` |
| Card Data | CVACT02Y | `Card.java` | `cards` |
| Card Cross-Ref | CVACT03Y | `CardCrossReference.java` | `card_cross_references` |
| Customer Master | CVCUS01Y | `Customer.java` | `customers` |
| Transaction | CVTRA05Y | `Transaction.java` | `transactions` |
| Daily Transaction | CVTRA06Y | `DailyTransaction.java` | `daily_transactions` |
| Transaction Type | CVTRA03Y | `TransactionType.java` | `transaction_types` |
| Transaction Category | CVTRA04Y | `TransactionCategory.java` | `transaction_categories` |
| Category Balance | CVTRA01Y | `CategoryBalance.java` | `category_balances` |
| Disclosure Group | CVTRA02Y | `DisclosureGroup.java` | `disclosure_groups` |
| User Security | CSUSR01Y | `User.java` | `users` |
| COMMAREA | COCOM01Y | `SessionContext.java` | HTTP Session / JWT |
