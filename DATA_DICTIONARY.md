# Data Dictionary - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo - Credit Card Management System
> **Source:** Copybook PIC clause analysis from `app/cpy/` and `app/app-*/cpy/`

---

## Overview

CardDemo stores all persistent data in **VSAM KSDS files** (Key-Sequenced Data Sets). Each VSAM file has a corresponding copybook that defines the fixed-length record layout. This dictionary translates those COBOL PIC clauses into business-friendly definitions.

### Entity Relationship Summary

```
Customer (1) ----< (N) Card ----< (N) Transaction
    |                    |
    |                    v
    +--- Card-Xref -->  Account (1) ----< (N) Tran-Category-Balance
                            |
                            +---> Disclosure-Group (interest rates)

User-Security (standalone - admin/auth)
Transaction-Type (reference data)
Transaction-Category (reference data)
```

---

## 1. Account Entity

> **Copybook:** `CVACT01Y.cpy` | **Record:** `ACCOUNT-RECORD` | **Length:** 300 bytes
> **VSAM File:** `ACCTFILE` (KSDS, key = ACCT-ID)

| Field Name               | PIC Clause        | Type      | Size  | Business Description                          | Example / Notes               |
|--------------------------|-------------------|-----------|------:|-----------------------------------------------|-------------------------------|
| `ACCT-ID`                | `9(11)`           | Numeric   |    11 | Unique account identifier                     | `00000000001`                 |
| `ACCT-ACTIVE-STATUS`     | `X(01)`           | Alpha     |     1 | Account status flag                           | `Y` = active, `N` = inactive |
| `ACCT-CURR-BAL`          | `S9(10)V99`       | Signed Dec|    12 | Current account balance (dollars.cents)        | Max +/- 9,999,999,999.99     |
| `ACCT-CREDIT-LIMIT`      | `S9(10)V99`       | Signed Dec|    12 | Credit limit                                  | Max 9,999,999,999.99         |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99`       | Signed Dec|    12 | Cash advance credit limit                     |                               |
| `ACCT-OPEN-DATE`         | `X(10)`           | Date      |    10 | Account opening date                          | `YYYY-MM-DD`                 |
| `ACCT-EXPIRAION-DATE`    | `X(10)`           | Date      |    10 | Account expiration date                       | Note: typo in source         |
| `ACCT-REISSUE-DATE`      | `X(10)`           | Date      |    10 | Card/account reissue date                     |                               |
| `ACCT-CURR-CYC-CREDIT`   | `S9(10)V99`       | Signed Dec|    12 | Current billing cycle credits                 |                               |
| `ACCT-CURR-CYC-DEBIT`    | `S9(10)V99`       | Signed Dec|    12 | Current billing cycle debits                  |                               |
| `ACCT-ADDR-ZIP`          | `X(10)`           | Alpha     |    10 | Account holder ZIP/postal code                |                               |
| `ACCT-GROUP-ID`          | `X(10)`           | Alpha     |    10 | Disclosure group identifier (links to rates)  | Links to `DIS-GROUP-RECORD`  |
| `FILLER`                 | `X(178)`          | Filler    |   178 | Reserved / padding to 300 bytes               |                               |

**Key business rules:**
- Primary key: `ACCT-ID` (11-digit numeric)
- Balance = Credits - Debits within the current cycle
- `ACCT-GROUP-ID` determines which interest rate applies via the Disclosure Group entity

---

## 2. Credit Card Entity

> **Copybook:** `CVACT02Y.cpy` | **Record:** `CARD-RECORD` | **Length:** 150 bytes
> **VSAM File:** `CARDFILE` (KSDS, key = CARD-NUM)

| Field Name               | PIC Clause        | Type      | Size  | Business Description                          | Example / Notes               |
|--------------------------|-------------------|-----------|------:|-----------------------------------------------|-------------------------------|
| `CARD-NUM`               | `X(16)`           | Alpha     |    16 | Credit card number (PAN)                      | `4000123456789012`           |
| `CARD-ACCT-ID`           | `9(11)`           | Numeric   |    11 | Owning account ID (FK to Account)             | Links to `ACCT-ID`           |
| `CARD-CVV-CD`            | `9(03)`           | Numeric   |     3 | Card verification value                       | `123`                        |
| `CARD-EMBOSSED-NAME`     | `X(50)`           | Alpha     |    50 | Name embossed on card                         | `JOHN Q PUBLIC`              |
| `CARD-EXPIRAION-DATE`    | `X(10)`           | Date      |    10 | Card expiration date                          | `YYYY-MM-DD`; note typo      |
| `CARD-ACTIVE-STATUS`     | `X(01)`           | Alpha     |     1 | Card active status flag                       | `Y` = active, `N` = inactive |
| `FILLER`                 | `X(59)`           | Filler    |    59 | Reserved / padding to 150 bytes               |                               |

**Key business rules:**
- Primary key: `CARD-NUM` (16-character, treated as string for leading-zero safety)
- Multiple cards can belong to one account (`CARD-ACCT-ID`)
- CVV is stored in plaintext (modernization must encrypt or tokenize)

---

## 3. Customer Entity

> **Copybook:** `CVCUS01Y.cpy` | **Record:** `CUSTOMER-RECORD` | **Length:** 500 bytes
> **VSAM File:** `CUSTFILE` (KSDS, key = CUST-ID)

| Field Name                  | PIC Clause        | Type      | Size  | Business Description                        | Example / Notes               |
|-----------------------------|-------------------|-----------|------:|---------------------------------------------|-------------------------------|
| `CUST-ID`                   | `9(09)`           | Numeric   |     9 | Unique customer identifier                  | `000000001`                  |
| `CUST-FIRST-NAME`           | `X(25)`           | Alpha     |    25 | First name                                  |                               |
| `CUST-MIDDLE-NAME`          | `X(25)`           | Alpha     |    25 | Middle name                                 |                               |
| `CUST-LAST-NAME`            | `X(25)`           | Alpha     |    25 | Last name                                   |                               |
| `CUST-ADDR-LINE-1`          | `X(50)`           | Alpha     |    50 | Address line 1                              |                               |
| `CUST-ADDR-LINE-2`          | `X(50)`           | Alpha     |    50 | Address line 2                              |                               |
| `CUST-ADDR-LINE-3`          | `X(50)`           | Alpha     |    50 | Address line 3                              |                               |
| `CUST-ADDR-STATE-CD`        | `X(02)`           | Alpha     |     2 | US state code                               | `CA`, `NY`, etc.             |
| `CUST-ADDR-COUNTRY-CD`      | `X(03)`           | Alpha     |     3 | Country code                                | `USA`                        |
| `CUST-ADDR-ZIP`             | `X(10)`           | Alpha     |    10 | ZIP/postal code                             | `90210`                      |
| `CUST-PHONE-NUM-1`          | `X(15)`           | Alpha     |    15 | Primary phone number                        |                               |
| `CUST-PHONE-NUM-2`          | `X(15)`           | Alpha     |    15 | Secondary phone number                      |                               |
| `CUST-SSN`                  | `9(09)`           | Numeric   |     9 | Social Security Number (PII)                | Must be encrypted/masked     |
| `CUST-GOVT-ISSUED-ID`       | `X(20)`           | Alpha     |    20 | Government-issued ID (driver's license etc.)|                               |
| `CUST-DOB-YYYY-MM-DD`       | `X(10)`           | Date      |    10 | Date of birth                               | `YYYY-MM-DD` format          |
| `CUST-EFT-ACCOUNT-ID`       | `X(10)`           | Alpha     |    10 | EFT/bank account ID for payments            |                               |
| `CUST-PRI-CARD-HOLDER-IND`  | `X(01)`           | Alpha     |     1 | Primary card holder indicator               | `Y` / `N`                   |
| `CUST-FICO-CREDIT-SCORE`    | `9(03)`           | Numeric   |     3 | FICO credit score                           | Range 300-850                |
| `FILLER`                    | `X(168)`          | Filler    |   168 | Reserved / padding to 500 bytes             |                               |

**Key business rules:**
- Primary key: `CUST-ID` (9-digit numeric)
- Contains PII fields (SSN, DOB, address, phone) requiring data protection
- Linked to cards via `CARD-XREF-RECORD`

---

## 4. Card Cross-Reference Entity

> **Copybook:** `CVACT03Y.cpy` | **Record:** `CARD-XREF-RECORD` | **Length:** 50 bytes
> **VSAM File:** `XREFFILE` (KSDS, key = XREF-CARD-NUM)

| Field Name               | PIC Clause        | Type      | Size  | Business Description                          | Example / Notes               |
|--------------------------|-------------------|-----------|------:|-----------------------------------------------|-------------------------------|
| `XREF-CARD-NUM`          | `X(16)`           | Alpha     |    16 | Card number (FK to Card)                      | Primary lookup key            |
| `XREF-CUST-ID`           | `9(09)`           | Numeric   |     9 | Customer ID (FK to Customer)                  | Links card to customer        |
| `XREF-ACCT-ID`           | `9(11)`           | Numeric   |    11 | Account ID (FK to Account)                    | Links card to account         |
| `FILLER`                 | `X(14)`           | Filler    |    14 | Reserved / padding to 50 bytes                |                               |

**Key business rules:**
- This is the **junction table** linking Cards, Customers, and Accounts
- Enables lookup: given a card number, find the customer and account
- Critical for transaction processing (CBTRN02C looks up account from card)

---

## 5. Transaction Entity

> **Copybook:** `CVTRA05Y.cpy` | **Record:** `TRAN-RECORD` | **Length:** 350 bytes
> **VSAM File:** `TRANSACT` (KSDS, key = TRAN-ID)

| Field Name               | PIC Clause        | Type      | Size  | Business Description                          | Example / Notes               |
|--------------------------|-------------------|-----------|------:|-----------------------------------------------|-------------------------------|
| `TRAN-ID`                | `X(16)`           | Alpha     |    16 | Unique transaction identifier                 | System-generated              |
| `TRAN-TYPE-CD`           | `X(02)`           | Alpha     |     2 | Transaction type code (FK)                    | `SA` = Sale, `RT` = Return   |
| `TRAN-CAT-CD`            | `9(04)`           | Numeric   |     4 | Transaction category code                     | `5411` = Grocery              |
| `TRAN-SOURCE`            | `X(10)`           | Alpha     |    10 | Transaction source/channel                    | `POS`, `ONLINE`, `ATM`       |
| `TRAN-DESC`              | `X(100)`          | Alpha     |   100 | Transaction description                       | Free-text                    |
| `TRAN-AMT`               | `S9(09)V99`       | Signed Dec|    11 | Transaction amount (dollars.cents)            | Negative = credit/return     |
| `TRAN-MERCHANT-ID`       | `9(09)`           | Numeric   |     9 | Merchant identifier                           |                               |
| `TRAN-MERCHANT-NAME`     | `X(50)`           | Alpha     |    50 | Merchant name                                 |                               |
| `TRAN-MERCHANT-CITY`     | `X(50)`           | Alpha     |    50 | Merchant city                                 |                               |
| `TRAN-MERCHANT-ZIP`      | `X(10)`           | Alpha     |    10 | Merchant ZIP/postal code                      |                               |
| `TRAN-CARD-NUM`          | `X(16)`           | Alpha     |    16 | Card number used (FK to Card)                 |                               |
| `TRAN-ORIG-TS`           | `X(26)`           | Timestamp |    26 | Original transaction timestamp                | `YYYY-MM-DD-HH.MM.SS.NNNNNN`|
| `TRAN-PROC-TS`           | `X(26)`           | Timestamp |    26 | Processing timestamp                          |                               |
| `FILLER`                 | `X(20)`           | Filler    |    20 | Reserved / padding to 350 bytes               |                               |

**Key business rules:**
- Highest-volume entity; drives batch processing cycle
- `TRAN-TYPE-CD` references `TRAN-TYPE-RECORD` for description
- `TRAN-CAT-CD` references `TRAN-CAT-RECORD` for category description
- Two timestamps track origination vs. processing time

---

## 6. Daily Transaction Entity

> **Copybook:** `CVTRA06Y.cpy` | **Record:** `DALYTRAN-RECORD` | **Length:** 350 bytes
> **VSAM File:** Daily transaction input (sequential)

| Field Name               | PIC Clause        | Type      | Size  | Business Description                          |
|--------------------------|-------------------|-----------|------:|-----------------------------------------------|
| `DALYTRAN-ID`            | `X(16)`           | Alpha     |    16 | Daily transaction identifier                  |
| `DALYTRAN-TYPE-CD`       | `X(02)`           | Alpha     |     2 | Transaction type code                         |
| `DALYTRAN-CAT-CD`        | `9(04)`           | Numeric   |     4 | Transaction category code                     |
| `DALYTRAN-SOURCE`        | `X(10)`           | Alpha     |    10 | Transaction source                            |
| `DALYTRAN-DESC`          | `X(100)`          | Alpha     |   100 | Transaction description                       |
| `DALYTRAN-AMT`           | `S9(09)V99`       | Signed Dec|    11 | Transaction amount                            |
| `DALYTRAN-MERCHANT-ID`   | `9(09)`           | Numeric   |     9 | Merchant identifier                           |
| `DALYTRAN-MERCHANT-NAME` | `X(50)`           | Alpha     |    50 | Merchant name                                 |
| `DALYTRAN-MERCHANT-CITY` | `X(50)`           | Alpha     |    50 | Merchant city                                 |
| `DALYTRAN-MERCHANT-ZIP`  | `X(10)`           | Alpha     |    10 | Merchant ZIP code                             |
| `DALYTRAN-CARD-NUM`      | `X(16)`           | Alpha     |    16 | Card number                                   |
| `DALYTRAN-ORIG-TS`       | `X(26)`           | Timestamp |    26 | Original timestamp                            |
| `DALYTRAN-PROC-TS`       | `X(26)`           | Timestamp |    26 | Processing timestamp                          |
| `FILLER`                 | `X(20)`           | Filler    |    20 | Reserved                                      |

**Key business rules:**
- Identical structure to Transaction; represents unprocessed daily input
- COMBTRAN job merges daily transactions into the master TRANSACT file
- POSTTRAN job posts these into the transaction master with balance updates

---

## 7. Transaction Category Balance Entity

> **Copybook:** `CVTRA01Y.cpy` | **Record:** `TRAN-CAT-BAL-RECORD` | **Length:** 50 bytes
> **VSAM File:** `TCATBALF` (KSDS, composite key)

| Field Name               | PIC Clause        | Type      | Size  | Business Description                          |
|--------------------------|-------------------|-----------|------:|-----------------------------------------------|
| `TRANCAT-ACCT-ID`        | `9(11)`           | Numeric   |    11 | Account ID (part of composite key)            |
| `TRANCAT-TYPE-CD`        | `X(02)`           | Alpha     |     2 | Transaction type code (part of key)           |
| `TRANCAT-CD`             | `9(04)`           | Numeric   |     4 | Transaction category code (part of key)       |
| `TRAN-CAT-BAL`           | `S9(09)V99`       | Signed Dec|    11 | Running balance for this category             |
| `FILLER`                 | `X(22)`           | Filler    |    22 | Reserved                                      |

**Key business rules:**
- Composite key: Account + Type + Category
- Tracks running totals per account per transaction category
- Updated during POSTTRAN batch processing

---

## 8. Disclosure Group Entity (Interest Rates)

> **Copybook:** `CVTRA02Y.cpy` | **Record:** `DIS-GROUP-RECORD` | **Length:** 50 bytes
> **VSAM File:** `DISCGRP` (KSDS, composite key)

| Field Name               | PIC Clause        | Type      | Size  | Business Description                          |
|--------------------------|-------------------|-----------|------:|-----------------------------------------------|
| `DIS-ACCT-GROUP-ID`      | `X(10)`           | Alpha     |    10 | Account group identifier (part of key)        |
| `DIS-TRAN-TYPE-CD`       | `X(02)`           | Alpha     |     2 | Transaction type code (part of key)           |
| `DIS-TRAN-CAT-CD`        | `9(04)`           | Numeric   |     4 | Transaction category code (part of key)       |
| `DIS-INT-RATE`           | `S9(04)V99`       | Signed Dec|     6 | Interest rate (e.g., 18.99)                   |
| `FILLER`                 | `X(28)`           | Filler    |    28 | Reserved                                      |

**Key business rules:**
- Defines interest rates per account group per transaction type/category
- Used by CBACT04C (interest calculator) to compute charges
- `DIS-ACCT-GROUP-ID` links to `ACCT-GROUP-ID` in the Account entity

---

## 9. Transaction Type Reference Entity

> **Copybook:** `CVTRA03Y.cpy` | **Record:** `TRAN-TYPE-RECORD` | **Length:** 60 bytes
> **VSAM File:** `TRANTYPE` (KSDS, key = TRAN-TYPE)

| Field Name               | PIC Clause        | Type      | Size  | Business Description                          |
|--------------------------|-------------------|-----------|------:|-----------------------------------------------|
| `TRAN-TYPE`              | `X(02)`           | Alpha     |     2 | Transaction type code (primary key)           |
| `TRAN-TYPE-DESC`         | `X(50)`           | Alpha     |    50 | Type description                              |
| `FILLER`                 | `X(08)`           | Filler    |     8 | Reserved                                      |

**Sample values:** `SA` = Sale, `RT` = Return, `CR` = Credit, `DB` = Debit

---

## 10. Transaction Category Reference Entity

> **Copybook:** `CVTRA04Y.cpy` | **Record:** `TRAN-CAT-RECORD` | **Length:** 60 bytes
> **VSAM File:** `TRANCATG` (KSDS, composite key)

| Field Name               | PIC Clause        | Type      | Size  | Business Description                          |
|--------------------------|-------------------|-----------|------:|-----------------------------------------------|
| `TRAN-TYPE-CD`           | `X(02)`           | Alpha     |     2 | Transaction type code (part of key)           |
| `TRAN-CAT-CD`            | `9(04)`           | Numeric   |     4 | Category code (part of key)                   |
| `TRAN-CAT-TYPE-DESC`     | `X(50)`           | Alpha     |    50 | Category description                          |
| `FILLER`                 | `X(04)`           | Filler    |     4 | Reserved                                      |

**Sample values:** `5411` = Grocery Stores, `5812` = Restaurants, `5541` = Gas Stations

---

## 11. User Security Entity

> **Copybook:** `CSUSR01Y.cpy` | **Record:** `SEC-USER-DATA` | **Length:** 80 bytes
> **VSAM File:** `USRSEC` (KSDS, key = SEC-USR-ID)

| Field Name               | PIC Clause        | Type      | Size  | Business Description                          | Example / Notes               |
|--------------------------|-------------------|-----------|------:|-----------------------------------------------|-------------------------------|
| `SEC-USR-ID`             | `X(08)`           | Alpha     |     8 | User login ID (primary key)                   | `ADMIN001`, `USER0001`       |
| `SEC-USR-FNAME`          | `X(20)`           | Alpha     |    20 | User first name                               |                               |
| `SEC-USR-LNAME`          | `X(20)`           | Alpha     |    20 | User last name                                |                               |
| `SEC-USR-PWD`            | `X(08)`           | Alpha     |     8 | Password (plaintext!)                         | Must be hashed in migration  |
| `SEC-USR-TYPE`           | `X(01)`           | Alpha     |     1 | User type                                     | `A` = Admin, `U` = Regular   |
| `SEC-USR-FILLER`         | `X(23)`           | Filler    |    23 | Reserved                                      |                               |

**Key business rules:**
- Passwords stored in plaintext - critical security concern for modernization
- User type controls menu access (Admin menu vs. Regular menu)
- 8-character max for user ID and password (mainframe constraint)

---

## 12. CICS Communication Area

> **Copybook:** `COCOM01Y.cpy` | **Record:** `CARDDEMO-COMMAREA` | **Length:** ~170 bytes
> **Storage:** CICS COMMAREA (passed between programs via XCTL)

| Field Name               | PIC Clause        | Type      | Size  | Business Description                          |
|--------------------------|-------------------|-----------|------:|-----------------------------------------------|
| `CDEMO-FROM-TRANID`      | `X(04)`           | Alpha     |     4 | Originating CICS transaction ID               |
| `CDEMO-FROM-PROGRAM`     | `X(08)`           | Alpha     |     8 | Originating program name                      |
| `CDEMO-TO-TRANID`        | `X(04)`           | Alpha     |     4 | Target CICS transaction ID                    |
| `CDEMO-TO-PROGRAM`       | `X(08)`           | Alpha     |     8 | Target program name                           |
| `CDEMO-USER-ID`          | `X(08)`           | Alpha     |     8 | Logged-in user ID                             |
| `CDEMO-USER-TYPE`        | `X(01)`           | Alpha     |     1 | User type (`A` = Admin, `U` = User)           |
| `CDEMO-PGM-CONTEXT`      | `9(01)`           | Numeric   |     1 | Program context (0=enter, 1=re-enter)         |
| `CDEMO-CUST-ID`          | `9(09)`           | Numeric   |     9 | Current customer ID in context                |
| `CDEMO-CUST-FNAME`       | `X(25)`           | Alpha     |    25 | Current customer first name                   |
| `CDEMO-CUST-MNAME`       | `X(25)`           | Alpha     |    25 | Current customer middle name                  |
| `CDEMO-CUST-LNAME`       | `X(25)`           | Alpha     |    25 | Current customer last name                    |
| `CDEMO-ACCT-ID`          | `9(11)`           | Numeric   |    11 | Current account ID in context                 |
| `CDEMO-ACCT-STATUS`      | `X(01)`           | Alpha     |     1 | Current account status                        |
| `CDEMO-CARD-NUM`         | `9(16)`           | Numeric   |    16 | Current card number in context                |
| `CDEMO-LAST-MAP`         | `X(7)`            | Alpha     |     7 | Last BMS map displayed                        |
| `CDEMO-LAST-MAPSET`      | `X(7)`            | Alpha     |     7 | Last BMS mapset used                          |

**Key business rules:**
- This is the "session state" passed between all online CICS programs
- Maps to a Java HTTP session or JWT token in modernized architecture
- Contains navigation context (from/to program routing)

---

## 13. Export Record (Multi-Type)

> **Copybook:** `CVEXPORT.cpy` | **Record:** `EXPORT-RECORD` | **Length:** 500 bytes
> **Storage:** Sequential file for branch migration

| Field Name                | PIC Clause          | Type      | Size  | Business Description                        |
|---------------------------|---------------------|-----------|------:|---------------------------------------------|
| `EXPORT-REC-TYPE`         | `X(1)`              | Alpha     |     1 | Record type discriminator                   |
| `EXPORT-TIMESTAMP`        | `X(26)`             | Timestamp |    26 | Export timestamp                            |
| `EXPORT-SEQUENCE-NUM`     | `9(9) COMP`         | Binary    |     4 | Sequence number                             |
| `EXPORT-BRANCH-ID`        | `X(4)`              | Alpha     |     4 | Branch identifier                           |
| `EXPORT-REGION-CODE`      | `X(5)`              | Alpha     |     5 | Region code                                 |
| `EXPORT-RECORD-DATA`      | `X(460)`            | Alpha     |   460 | Polymorphic data area (REDEFINES)           |

**REDEFINES variants:** Customer, Account, Transaction, Card-Xref, Card data - each redefines the 460-byte data area.

---

## 14. Report Layout Structures

> **Copybook:** `CVTRA07Y.cpy` | Used by: CBTRN03C (transaction report)

| Structure                  | Purpose                                        |
|----------------------------|------------------------------------------------|
| `REPORT-NAME-HEADER`       | Report title with date range                  |
| `TRANSACTION-DETAIL-REPORT` | One line per transaction detail               |
| `TRANSACTION-HEADER-1/2`   | Column headers and separator                  |
| `REPORT-PAGE-TOTALS`       | Page-level subtotals                          |
| `REPORT-ACCOUNT-TOTALS`    | Account-level subtotals                       |
| `REPORT-GRAND-TOTALS`      | Grand total across all accounts               |

---

## 15. Statement Layout

> **Copybook:** `COSTM01.CPY` | **Record:** `TRNX-RECORD` | Used by: CBSTM03A

| Field Name               | PIC Clause        | Type      | Size  | Business Description                          |
|--------------------------|-------------------|-----------|------:|-----------------------------------------------|
| `TRNX-CARD-NUM`          | `X(16)`           | Alpha     |    16 | Card number (part of composite key)           |
| `TRNX-ID`                | `X(16)`           | Alpha     |    16 | Transaction ID (part of composite key)        |
| `TRNX-TYPE-CD`           | `X(02)`           | Alpha     |     2 | Transaction type code                         |
| `TRNX-CAT-CD`            | `9(04)`           | Numeric   |     4 | Category code                                 |
| `TRNX-SOURCE`            | `X(10)`           | Alpha     |    10 | Transaction source                            |
| `TRNX-DESC`              | `X(100)`          | Alpha     |   100 | Description                                   |
| `TRNX-AMT`               | `S9(09)V99`       | Signed Dec|    11 | Amount                                        |
| `TRNX-MERCHANT-ID`       | `9(09)`           | Numeric   |     9 | Merchant ID                                   |
| `TRNX-MERCHANT-NAME`     | `X(50)`           | Alpha     |    50 | Merchant name                                 |
| `TRNX-MERCHANT-CITY`     | `X(50)`           | Alpha     |    50 | Merchant city                                 |
| `TRNX-MERCHANT-ZIP`      | `X(10)`           | Alpha     |    10 | Merchant ZIP                                  |
| `TRNX-ORIG-TS`           | `X(26)`           | Timestamp |    26 | Original timestamp                            |
| `TRNX-PROC-TS`           | `X(26)`           | Timestamp |    26 | Processing timestamp                          |
| `FILLER`                 | `X(20)`           | Filler    |    20 | Reserved                                      |

**Note:** Re-keyed version of Transaction with composite key (Card+TranID) for statement ordering.

---

## 16. Utility / Support Structures

### Date Conversion Record (`CODATECN.cpy`)

| Field Name               | PIC Clause        | Purpose                                     |
|--------------------------|-------------------|---------------------------------------------|
| `CODATECN-TYPE`          | `X`               | Input format: `1`=YYYYMMDD, `2`=YYYY-MM-DD |
| `CODATECN-INP-DATE`      | `X(20)`           | Input date string                           |
| `CODATECN-OUTTYPE`       | `X`               | Output format: `1`=YYYY-MM-DD, `2`=YYYYMMDD|
| `CODATECN-0UT-DATE`      | `X(20)`           | Output date string                          |
| `CODATECN-ERROR-MSG`     | `X(38)`           | Error message if conversion fails           |

### Date/Time Working Storage (`CSDAT01Y.cpy`)

Provides formatted date/time fields: `WS-CURDATE` (YYYYMMDD), `WS-CURDATE-MM-DD-YY`, `WS-CURTIME-HH-MM-SS`, and `WS-TIMESTAMP` (full ISO-style timestamp).

### Abend Data (`CSMSG02Y.cpy`)

| Field Name               | PIC Clause        | Purpose                            |
|--------------------------|-------------------|------------------------------------|
| `ABEND-CODE`             | `X(4)`            | Abend code                        |
| `ABEND-CULPRIT`          | `X(8)`            | Program causing the abend         |
| `ABEND-REASON`           | `X(50)`           | Reason text                       |
| `ABEND-MSG`              | `X(72)`           | Full error message                |

### Lookup Code Repository (`CSLKPCDY.cpy`)

1,318-line copybook containing hardcoded validation tables:
- **North American phone area codes** (88-level condition names)
- **US state codes** (2-letter)
- **State + ZIP prefix combinations** for address validation

---

## Modernization Notes

### PII / Security Concerns
| Field                    | Entity    | Risk                                        | Recommendation                    |
|--------------------------|-----------|---------------------------------------------|-----------------------------------|
| `SEC-USR-PWD`            | User      | Plaintext password storage                  | Hash with bcrypt/Argon2           |
| `CUST-SSN`               | Customer  | Social Security Number in clear             | Encrypt at rest, mask in UI       |
| `CARD-CVV-CD`            | Card      | CVV stored unencrypted                      | Tokenize, never store post-auth   |
| `CARD-NUM`               | Card/Xref | Full PAN in multiple files                  | Tokenize, PCI-DSS compliance      |

### COBOL-to-Java Type Mapping

| COBOL PIC               | Java Type                        | Notes                              |
|--------------------------|----------------------------------|------------------------------------|
| `9(n)`                   | `long` or `BigInteger`           | Use `long` for n <= 18            |
| `X(n)`                   | `String`                         | Right-trim spaces                 |
| `S9(n)V99`               | `BigDecimal`                     | Scale = 2 for currency            |
| `S9(n)V99 COMP-3`        | `BigDecimal`                     | Packed decimal; same Java type    |
| `9(n) COMP`              | `int` or `long`                  | Binary integer                    |
| `X(10)` dates            | `LocalDate`                      | Parse `YYYY-MM-DD` format         |
| `X(26)` timestamps       | `LocalDateTime`                  | Parse mainframe timestamp format  |

### VSAM-to-Relational Mapping

| VSAM File     | Suggested Table Name      | Primary Key                        |
|---------------|---------------------------|------------------------------------|
| ACCTFILE      | `accounts`                | `acct_id`                          |
| CARDFILE      | `cards`                   | `card_num`                         |
| CUSTFILE      | `customers`               | `cust_id`                          |
| XREFFILE      | `card_xref`               | `xref_card_num`                    |
| TRANSACT      | `transactions`            | `tran_id`                          |
| USRSEC        | `users`                   | `usr_id`                           |
| TRANTYPE      | `transaction_types`       | `tran_type`                        |
| TRANCATG      | `transaction_categories`  | `(tran_type_cd, tran_cat_cd)`      |
| TCATBALF      | `tran_category_balances`  | `(acct_id, type_cd, cat_cd)`       |
| DISCGRP       | `disclosure_groups`       | `(group_id, type_cd, cat_cd)`      |
