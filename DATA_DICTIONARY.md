# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** Copybook PIC clause analysis (`app/cpy/`)
> **Purpose:** Business-friendly reference mapping COBOL record layouts to logical data entities

---

## Overview

The CardDemo application uses **VSAM KSDS files** as its primary data store. Each file's record layout is defined in a copybook. This dictionary extracts every field, translates PIC clauses into business-readable types, and groups them by entity.

### PIC Clause Quick Reference

| COBOL PIC Clause     | Business Type        | Example                     |
|----------------------|----------------------|-----------------------------|
| `PIC X(n)`           | Text (n characters)  | Name, description           |
| `PIC 9(n)`           | Numeric integer      | ID, code                    |
| `PIC S9(n)V99`       | Signed decimal       | Dollar amount               |
| `PIC 9(n) COMP`      | Binary integer       | Packed numeric ID           |
| `FILLER PIC X(n)`    | Reserved/padding     | Alignment to record length  |

---

## 1. Account Master — `CVACT01Y.cpy`

**VSAM File:** `ACCTDAT` | **Record Length:** 300 bytes | **Key:** Account ID

Stores the master record for each credit card account.

| # | Field Name                | PIC Clause         | Type              | Length | Business Description                  |
|--:|---------------------------|--------------------:|-------------------|-------:|---------------------------------------|
| 1 | `ACCT-ID`                 | `9(11)`            | Numeric ID        |     11 | Unique account identifier             |
| 2 | `ACCT-ACTIVE-STATUS`      | `X(01)`            | Flag              |      1 | Account status (Y=Active, N=Closed)   |
| 3 | `ACCT-CURR-BAL`           | `S9(10)V99`        | Currency          |     12 | Current account balance               |
| 4 | `ACCT-CREDIT-LIMIT`       | `S9(10)V99`        | Currency          |     12 | Maximum credit limit                  |
| 5 | `ACCT-CASH-CREDIT-LIMIT`  | `S9(10)V99`        | Currency          |     12 | Cash advance credit limit             |
| 6 | `ACCT-OPEN-DATE`          | `X(10)`            | Date (text)       |     10 | Date account was opened               |
| 7 | `ACCT-EXPIRAION-DATE`     | `X(10)`            | Date (text)       |     10 | Account expiration date               |
| 8 | `ACCT-REISSUE-DATE`       | `X(10)`            | Date (text)       |     10 | Date of last card reissue             |
| 9 | `ACCT-CURR-CYC-CREDIT`    | `S9(10)V99`        | Currency          |     12 | Credits in current billing cycle      |
|10 | `ACCT-CURR-CYC-DEBIT`     | `S9(10)V99`        | Currency          |     12 | Debits in current billing cycle       |
|11 | `ACCT-ADDR-ZIP`           | `X(10)`            | Postal Code       |     10 | Account address ZIP / postal code     |
|12 | `ACCT-GROUP-ID`            | `X(10)`            | Code              |     10 | Disclosure/rate group assignment      |
|13 | `FILLER`                  | `X(178)`           | Reserved          |    178 | Padding to 300-byte record            |

---

## 2. Card Master — `CVACT02Y.cpy`

**VSAM File:** `CARDDAT` | **Record Length:** 150 bytes | **Key:** Card Number

Stores one record per physical credit card.

| # | Field Name                | PIC Clause         | Type              | Length | Business Description                  |
|--:|---------------------------|--------------------:|-------------------|-------:|---------------------------------------|
| 1 | `CARD-NUM`                | `X(16)`            | Card Number       |     16 | 16-digit credit card number           |
| 2 | `CARD-ACCT-ID`            | `9(11)`            | Numeric ID        |     11 | Parent account ID (FK → Account)      |
| 3 | `CARD-CVV-CD`             | `9(03)`            | Security Code     |      3 | Card verification value (CVV)         |
| 4 | `CARD-EMBOSSED-NAME`      | `X(50)`            | Text              |     50 | Cardholder name as embossed           |
| 5 | `CARD-EXPIRAION-DATE`     | `X(10)`            | Date (text)       |     10 | Card expiration date                  |
| 6 | `CARD-ACTIVE-STATUS`      | `X(01)`            | Flag              |      1 | Card status (Y=Active, N=Inactive)    |
| 7 | `FILLER`                  | `X(59)`            | Reserved          |     59 | Padding to 150-byte record            |

---

## 3. Card Cross-Reference — `CVACT03Y.cpy`

**VSAM File:** `CARDXREF` / `CXACAIX` (alternate index) | **Record Length:** 50 bytes | **Key:** Card Number

Links card numbers to account IDs for reverse lookup.

| # | Field Name                | PIC Clause         | Type              | Length | Business Description                  |
|--:|---------------------------|--------------------:|-------------------|-------:|---------------------------------------|
| 1 | `XREF-CARD-NUM`           | `X(16)`            | Card Number       |     16 | Credit card number (primary key)      |
| 2 | `XREF-ACCT-ID`            | `9(11)`            | Numeric ID        |     11 | Associated account ID                 |
| 3 | `FILLER`                  | `X(23)`            | Reserved          |     23 | Padding to 50-byte record             |

---

## 4. Customer Master — `CVCUS01Y.cpy`

**VSAM File:** `CUSTDAT` | **Record Length:** 500 bytes | **Key:** Customer ID

Stores personal information for each cardholder.

| # | Field Name                | PIC Clause         | Type              | Length | Business Description                  |
|--:|---------------------------|--------------------:|-------------------|-------:|---------------------------------------|
| 1 | `CUST-ID`                 | `9(09)`            | Numeric ID        |      9 | Unique customer identifier            |
| 2 | `CUST-FIRST-NAME`         | `X(25)`            | Text              |     25 | Customer first name                   |
| 3 | `CUST-MIDDLE-NAME`        | `X(25)`            | Text              |     25 | Customer middle name                  |
| 4 | `CUST-LAST-NAME`          | `X(25)`            | Text              |     25 | Customer last name                    |
| 5 | `CUST-ADDR-LINE-1`        | `X(50)`            | Text              |     50 | Address line 1                        |
| 6 | `CUST-ADDR-LINE-2`        | `X(50)`            | Text              |     50 | Address line 2                        |
| 7 | `CUST-ADDR-LINE-3`        | `X(50)`            | Text              |     50 | Address line 3                        |
| 8 | `CUST-ADDR-STATE-CD`      | `X(02)`            | State Code        |      2 | US state code                         |
| 9 | `CUST-ADDR-COUNTRY-CD`    | `X(03)`            | Country Code      |      3 | Country code                          |
|10 | `CUST-ADDR-ZIP`           | `X(10)`            | Postal Code       |     10 | ZIP / postal code                     |
|11 | `CUST-PHONE-NUM-1`        | `X(15)`            | Phone             |     15 | Primary phone number                  |
|12 | `CUST-PHONE-NUM-2`        | `X(15)`            | Phone             |     15 | Secondary phone number                |
|13 | `CUST-SSN`                | `9(09)`            | SSN               |      9 | Social Security Number (PII)          |
|14 | `CUST-GOVT-ISSUED-ID`     | `X(20)`            | ID Document       |     20 | Government-issued ID number           |
|15 | `CUST-DOB-YYYY-MM-DD`     | `X(10)`            | Date (text)       |     10 | Date of birth                         |
|16 | `CUST-EFT-ACCOUNT-ID`     | `X(10)`            | Account Number    |     10 | EFT/bank account for payments         |
|17 | `CUST-PRI-CARD-HOLDER-IND`| `X(01)`            | Flag              |      1 | Primary cardholder indicator          |
|18 | `CUST-FICO-CREDIT-SCORE`  | `9(03)`            | Score             |      3 | FICO credit score                     |
|19 | `FILLER`                  | `X(168)`           | Reserved          |    168 | Padding to 500-byte record            |

---

## 5. Transaction Master — `CVTRA05Y.cpy`

**VSAM File:** `TRANSACT` | **Record Length:** 350 bytes | **Key:** Transaction ID

The main transaction ledger for all posted credit card transactions.

| # | Field Name                | PIC Clause         | Type              | Length | Business Description                  |
|--:|---------------------------|--------------------:|-------------------|-------:|---------------------------------------|
| 1 | `TRAN-ID`                 | `X(16)`            | Transaction ID    |     16 | Unique transaction identifier         |
| 2 | `TRAN-TYPE-CD`            | `X(02)`            | Code              |      2 | Transaction type (FK → Tran Type)     |
| 3 | `TRAN-CAT-CD`             | `9(04)`            | Code              |      4 | Transaction category code             |
| 4 | `TRAN-SOURCE`             | `X(10)`            | Code              |     10 | Transaction source (POS, ATM, etc.)   |
| 5 | `TRAN-DESC`               | `X(100)`           | Text              |    100 | Transaction description               |
| 6 | `TRAN-AMT`                | `S9(09)V99`        | Currency          |     11 | Transaction amount                    |
| 7 | `TRAN-MERCHANT-ID`        | `9(09)`            | Numeric ID        |      9 | Merchant identifier                   |
| 8 | `TRAN-MERCHANT-NAME`      | `X(50)`            | Text              |     50 | Merchant business name                |
| 9 | `TRAN-MERCHANT-CITY`      | `X(50)`            | Text              |     50 | Merchant city                         |
|10 | `TRAN-MERCHANT-ZIP`       | `X(10)`            | Postal Code       |     10 | Merchant ZIP code                     |
|11 | `TRAN-CARD-NUM`           | `X(16)`            | Card Number       |     16 | Card used for this transaction        |
|12 | `TRAN-ORIG-TS`            | `X(26)`            | Timestamp         |     26 | Original transaction timestamp        |
|13 | `TRAN-PROC-TS`            | `X(26)`            | Timestamp         |     26 | Processing/posting timestamp          |
|14 | `FILLER`                  | `X(20)`            | Reserved          |     20 | Padding to 350-byte record            |

---

## 6. Daily Transaction — `CVTRA06Y.cpy`

**VSAM File:** `DALYTRAN` | **Record Length:** 350 bytes | **Key:** Daily Tran ID

Holds unposted daily transactions before they are posted to the master.

| # | Field Name                | PIC Clause         | Type              | Length | Business Description                  |
|--:|---------------------------|--------------------:|-------------------|-------:|---------------------------------------|
| 1 | `DALYTRAN-ID`             | `X(16)`            | Transaction ID    |     16 | Daily transaction identifier          |
| 2 | `DALYTRAN-TYPE-CD`        | `X(02)`            | Code              |      2 | Transaction type code                 |
| 3 | `DALYTRAN-CAT-CD`         | `9(04)`            | Code              |      4 | Transaction category code             |
| 4 | `DALYTRAN-SOURCE`         | `X(10)`            | Code              |     10 | Transaction source                    |
| 5 | `DALYTRAN-DESC`           | `X(100)`           | Text              |    100 | Transaction description               |
| 6 | `DALYTRAN-AMT`            | `S9(09)V99`        | Currency          |     11 | Transaction amount                    |
| 7 | `DALYTRAN-MERCHANT-ID`    | `9(09)`            | Numeric ID        |      9 | Merchant identifier                   |
| 8 | `DALYTRAN-MERCHANT-NAME`  | `X(50)`            | Text              |     50 | Merchant name                         |
| 9 | `DALYTRAN-MERCHANT-CITY`  | `X(50)`            | Text              |     50 | Merchant city                         |
|10 | `DALYTRAN-MERCHANT-ZIP`   | `X(10)`            | Postal Code       |     10 | Merchant ZIP code                     |
|11 | `DALYTRAN-CARD-NUM`       | `X(16)`            | Card Number       |     16 | Card number                           |
|12 | `DALYTRAN-ORIG-TS`        | `X(26)`            | Timestamp         |     26 | Original timestamp                    |
|13 | `DALYTRAN-PROC-TS`        | `X(26)`            | Timestamp         |     26 | Processing timestamp                  |
|14 | `FILLER`                  | `X(20)`            | Reserved          |     20 | Padding to 350-byte record            |

---

## 7. Transaction Category Balance — `CVTRA01Y.cpy`

**VSAM File:** `TCATBAL` | **Record Length:** 50 bytes | **Key:** Account ID + Type Code + Category Code

Running balance per transaction category per account.

| # | Field Name                | PIC Clause         | Type              | Length | Business Description                  |
|--:|---------------------------|--------------------:|-------------------|-------:|---------------------------------------|
| 1 | `TRANCAT-ACCT-ID`         | `9(11)`            | Numeric ID        |     11 | Account identifier                    |
| 2 | `TRANCAT-TYPE-CD`          | `X(02)`            | Code              |      2 | Transaction type code                 |
| 3 | `TRANCAT-CD`              | `9(04)`            | Code              |      4 | Transaction category code             |
| 4 | `TRAN-CAT-BAL`            | `S9(09)V99`        | Currency          |     11 | Category balance amount               |
| 5 | `FILLER`                  | `X(22)`            | Reserved          |     22 | Padding to 50-byte record             |

---

## 8. Disclosure Group — `CVTRA02Y.cpy`

**VSAM File:** `DISCGRP` | **Record Length:** 50 bytes | **Key:** Group ID + Type Code + Category Code

Interest rate rules per disclosure group.

| # | Field Name                | PIC Clause         | Type              | Length | Business Description                  |
|--:|---------------------------|--------------------:|-------------------|-------:|---------------------------------------|
| 1 | `DIS-ACCT-GROUP-ID`       | `X(10)`            | Group ID          |     10 | Disclosure group identifier           |
| 2 | `DIS-TRAN-TYPE-CD`        | `X(02)`            | Code              |      2 | Transaction type code                 |
| 3 | `DIS-TRAN-CAT-CD`         | `9(04)`            | Code              |      4 | Transaction category code             |
| 4 | `DIS-INT-RATE`            | `S9(04)V99`        | Percentage        |      6 | Interest rate for this group          |
| 5 | `FILLER`                  | `X(28)`            | Reserved          |     28 | Padding to 50-byte record             |

---

## 9. Transaction Type — `CVTRA03Y.cpy`

**VSAM File:** `TRANTYPE` | **Record Length:** 60 bytes | **Key:** Transaction Type Code

Reference table of transaction types.

| # | Field Name                | PIC Clause         | Type              | Length | Business Description                  |
|--:|---------------------------|--------------------:|-------------------|-------:|---------------------------------------|
| 1 | `TRAN-TYPE`               | `X(02)`            | Code              |      2 | Transaction type code (e.g., "SA")    |
| 2 | `TRAN-TYPE-DESC`          | `X(50)`            | Text              |     50 | Type description ("Sale", etc.)       |
| 3 | `FILLER`                  | `X(08)`            | Reserved          |      8 | Padding to 60-byte record             |

---

## 10. Transaction Category — `CVTRA04Y.cpy`

**VSAM File:** `TRANCATG` | **Record Length:** 60 bytes | **Key:** Type Code + Category Code

Reference table of transaction categories within each type.

| # | Field Name                | PIC Clause         | Type              | Length | Business Description                  |
|--:|---------------------------|--------------------:|-------------------|-------:|---------------------------------------|
| 1 | `TRAN-TYPE-CD`            | `X(02)`            | Code              |      2 | Transaction type code                 |
| 2 | `TRAN-CAT-CD`             | `9(04)`            | Code              |      4 | Transaction category code             |
| 3 | `TRAN-CAT-TYPE-DESC`      | `X(50)`            | Text              |     50 | Category description                  |
| 4 | `FILLER`                  | `X(04)`            | Reserved          |      4 | Padding to 60-byte record             |

---

## 11. User Security — `CSUSR01Y.cpy`

**VSAM File:** `USRSEC` | **Record Length:** 80 bytes | **Key:** User ID

Authentication and authorization records.

| # | Field Name                | PIC Clause         | Type              | Length | Business Description                  |
|--:|---------------------------|--------------------:|-------------------|-------:|---------------------------------------|
| 1 | `SEC-USR-ID`              | `X(08)`            | User ID           |      8 | Login user identifier                 |
| 2 | `SEC-USR-FNAME`           | `X(20)`            | Text              |     20 | User first name                       |
| 3 | `SEC-USR-LNAME`           | `X(20)`            | Text              |     20 | User last name                        |
| 4 | `SEC-USR-PWD`             | `X(08)`            | Password          |      8 | Login password (plaintext)            |
| 5 | `SEC-USR-TYPE`            | `X(01)`            | Code              |      1 | User type (A=Admin, U=User)           |
| 6 | `SEC-USR-FILLER`          | `X(23)`            | Reserved          |     23 | Padding to 80-byte record             |

---

## 12. CICS Communication Area — `COCOM01Y.cpy`

**Usage:** Passed between online programs via COMMAREA

| # | Field Name                | PIC Clause         | Type              | Length | Business Description                  |
|--:|---------------------------|--------------------:|-------------------|-------:|---------------------------------------|
| 1 | `CDEMO-FROM-TRANID`       | `X(04)`            | CICS Trans ID     |      4 | Originating transaction               |
| 2 | `CDEMO-FROM-PROGRAM`      | `X(08)`            | Program Name      |      8 | Calling program name                  |
| 3 | `CDEMO-TO-TRANID`         | `X(04)`            | CICS Trans ID     |      4 | Target transaction                    |
| 4 | `CDEMO-TO-PROGRAM`        | `X(08)`            | Program Name      |      8 | Target program name                   |
| 5 | `CDEMO-USER-ID`           | `X(08)`            | User ID           |      8 | Authenticated user ID                 |
| 6 | `CDEMO-USER-TYPE`         | `X(01)`            | Code              |      1 | User type (A=Admin, U=User)           |
| 7 | `CDEMO-PGM-CONTEXT`       | `9(01)`            | Flag              |      1 | 0=first entry, 1=re-entry             |
| 8 | `CDEMO-CUST-ID`           | `9(09)`            | Numeric ID        |      9 | Selected customer ID                  |
| 9 | `CDEMO-CUST-FNAME`        | `X(25)`            | Text              |     25 | Customer first name                   |
|10 | `CDEMO-CUST-MNAME`        | `X(25)`            | Text              |     25 | Customer middle name                  |
|11 | `CDEMO-CUST-LNAME`        | `X(25)`            | Text              |     25 | Customer last name                    |
|12 | `CDEMO-ACCT-ID`           | `9(11)`            | Numeric ID        |     11 | Selected account ID                   |
|13 | `CDEMO-ACCT-STATUS`       | `X(01)`            | Flag              |      1 | Account status                        |
|14 | `CDEMO-CARD-NUM`          | `9(16)`            | Card Number       |     16 | Selected card number                  |
|15 | `CDEMO-LAST-MAP`          | `X(07)`            | Map Name          |      7 | Last BMS map displayed                |
|16 | `CDEMO-LAST-MAPSET`       | `X(07)`            | Mapset Name       |      7 | Last BMS mapset used                  |

---

## 13. Statement Transaction Layout — `COSTM01.CPY`

**Usage:** Reformatted transaction record for statement generation (key = card + tran ID)

| # | Field Name                | PIC Clause         | Type              | Length | Business Description                  |
|--:|---------------------------|--------------------:|-------------------|-------:|---------------------------------------|
| 1 | `TRNX-CARD-NUM`           | `X(16)`            | Card Number       |     16 | Card number (part of composite key)   |
| 2 | `TRNX-ID`                 | `X(16)`            | Transaction ID    |     16 | Transaction ID (part of composite key)|
| 3 | `TRNX-TYPE-CD`            | `X(02)`            | Code              |      2 | Transaction type code                 |
| 4 | `TRNX-CAT-CD`             | `9(04)`            | Code              |      4 | Transaction category code             |
| 5 | `TRNX-SOURCE`             | `X(10)`            | Code              |     10 | Transaction source                    |
| 6 | `TRNX-DESC`               | `X(100)`           | Text              |    100 | Transaction description               |
| 7 | `TRNX-AMT`                | `S9(09)V99`        | Currency          |     11 | Transaction amount                    |
| 8 | `TRNX-MERCHANT-ID`        | `9(09)`            | Numeric ID        |      9 | Merchant ID                           |
| 9 | `TRNX-MERCHANT-NAME`      | `X(50)`            | Text              |     50 | Merchant name                         |
|10 | `TRNX-MERCHANT-CITY`      | `X(50)`            | Text              |     50 | Merchant city                         |
|11 | `TRNX-MERCHANT-ZIP`       | `X(10)`            | Postal Code       |     10 | Merchant ZIP code                     |
|12 | `TRNX-ORIG-TS`            | `X(26)`            | Timestamp         |     26 | Original timestamp                    |
|13 | `TRNX-PROC-TS`            | `X(26)`            | Timestamp         |     26 | Processing timestamp                  |
|14 | `FILLER`                  | `X(20)`            | Reserved          |     20 | Padding                               |

---

## 14. Card Detail (Expanded) — `CVCRD01Y.cpy`

**Usage:** Working-storage structure joining card + account + customer data for display screens.

| # | Field Name                     | PIC Clause    | Type         | Business Description              |
|--:|--------------------------------|---------------|--------------|-----------------------------------|
| 1 | `CCARD-CARD-NUM`               | `X(16)`       | Card Number  | Credit card number                |
| 2 | `CCARD-ACCT-ID`                | `9(11)`       | Numeric ID   | Account ID                        |
| 3 | `CCARD-CVV-CD`                 | `9(03)`       | Security     | CVV code                          |
| 4 | `CCARD-EMBOSSED-NAME`          | `X(50)`       | Text         | Embossed cardholder name          |
| 5 | `CCARD-EXPIRAION-DATE`         | `X(10)`       | Date         | Card expiration date              |
| 6 | `CCARD-ACTIVE-STATUS`          | `X(01)`       | Flag         | Active status                     |
| 7 | `CCARD-ACCT-CURR-BAL`          | `S9(10)V99`   | Currency     | Current account balance           |
| 8 | `CCARD-ACCT-CREDIT-LIMIT`      | `S9(10)V99`   | Currency     | Account credit limit              |
| 9 | `CCARD-ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99`   | Currency     | Cash advance limit                |
|10 | `CCARD-ACCT-OPEN-DATE`         | `X(10)`       | Date         | Account open date                 |
|11 | `CCARD-ACCT-EXPIRAION-DATE`    | `X(10)`       | Date         | Account expiration date           |
|12 | `CCARD-ACCT-GROUP-ID`          | `X(10)`       | Code         | Disclosure group ID               |
|13 | `CCARD-CUST-ID`                | `9(09)`       | Numeric ID   | Customer ID                       |
|14 | `CCARD-CUST-FIRST-NAME`        | `X(25)`       | Text         | Customer first name               |
|15 | `CCARD-CUST-MIDDLE-NAME`       | `X(25)`       | Text         | Customer middle name              |
|16 | `CCARD-CUST-LAST-NAME`         | `X(25)`       | Text         | Customer last name                |

---

## 15. Export Record Layouts — `CVEXPORT.cpy`

**Usage:** Sequential file layouts for the CBEXPORT/CBIMPORT batch programs.

Contains export-format records for: **Customers**, **Accounts**, **Cards**, **Transactions**, and **Cross-References** — mirroring the VSAM layouts above with an `EXP-` prefix and fixed-length sequential formatting.

Key records defined:
- `EXP-CUST-RECORD` — Customer export (500 bytes)
- `EXP-ACCT-RECORD` — Account export (300 bytes)
- `EXP-CARD-RECORD` — Card export (150 bytes)
- `EXP-TRAN-RECORD` — Transaction export (350 bytes)
- `EXP-XREF-RECORD` — Cross-reference export (50 bytes)

---

## 16. Lookup Code Tables — `CSLKPCDY.cpy`

**Usage:** Hard-coded reference data (1,318 lines) embedded in working storage.

Contains lookup tables for:
- **US State codes** (50 states + territories)
- **Country codes** (ISO 3166)
- **Transaction type codes**
- **Transaction category codes**

> **Modernization Note:** These should migrate to database reference tables or configuration files.

---

## Entity Relationship Summary

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│   Customer   │ 1───N │   Account    │ 1───N │     Card     │
│  CVCUS01Y    │       │  CVACT01Y    │       │  CVACT02Y    │
│  CUSTDAT     │       │  ACCTDAT     │       │  CARDDAT     │
└──────────────┘       └──────┬───────┘       └──────┬───────┘
                              │                       │
                              │                       │
                       ┌──────┴───────┐       ┌──────┴───────┐
                       │  Card XRef   │       │ Transaction  │
                       │  CVACT03Y    │       │  CVTRA05Y    │
                       │  CARDXREF    │       │  TRANSACT    │
                       └──────────────┘       └──────┬───────┘
                                                     │
                              ┌───────────────┬──────┴──────┐
                       ┌──────┴───────┐┌──────┴──────┐┌─────┴──────┐
                       │  Tran Type   ││  Tran Cat   ││ Cat Balance│
                       │  CVTRA03Y    ││  CVTRA04Y   ││ CVTRA01Y   │
                       │  TRANTYPE    ││  TRANCATG   ││ TCATBAL    │
                       └──────────────┘└─────────────┘└────────────┘

                       ┌──────────────┐       ┌──────────────┐
                       │  Disclosure  │       │ Daily Trans  │
                       │   Group      │       │  CVTRA06Y    │
                       │  CVTRA02Y    │       │  DALYTRAN    │
                       │  DISCGRP     │       └──────────────┘
                       └──────────────┘

                       ┌──────────────┐
                       │ User Security│
                       │  CSUSR01Y    │
                       │  USRSEC      │
                       └──────────────┘
```

---

## VSAM File Summary

| # | VSAM File Name | Copybook   | Record Len | Key Field(s)                     | Key Len | Access     |
|--:|----------------|------------|:----------:|----------------------------------|:-------:|------------|
| 1 | ACCTDAT        | CVACT01Y   | 300        | ACCT-ID                          | 11      | KSDS       |
| 2 | CARDDAT        | CVACT02Y   | 150        | CARD-NUM                         | 16      | KSDS       |
| 3 | CARDXREF       | CVACT03Y   |  50        | XREF-CARD-NUM                    | 16      | KSDS + AIX |
| 4 | CUSTDAT        | CVCUS01Y   | 500        | CUST-ID                          |  9      | KSDS       |
| 5 | TRANSACT       | CVTRA05Y   | 350        | TRAN-ID                          | 16      | KSDS + AIX |
| 6 | DALYTRAN       | CVTRA06Y   | 350        | DALYTRAN-ID                      | 16      | KSDS       |
| 7 | TCATBAL        | CVTRA01Y   |  50        | ACCT-ID + TYPE-CD + CAT-CD       | 17      | KSDS       |
| 8 | DISCGRP        | CVTRA02Y   |  50        | GROUP-ID + TYPE-CD + CAT-CD      | 16      | KSDS       |
| 9 | TRANTYPE       | CVTRA03Y   |  60        | TRAN-TYPE                        |  2      | KSDS       |
|10 | TRANCATG       | CVTRA04Y   |  60        | TYPE-CD + CAT-CD                 |  6      | KSDS       |
|11 | USRSEC         | CSUSR01Y   |  80        | SEC-USR-ID                       |  8      | KSDS       |
