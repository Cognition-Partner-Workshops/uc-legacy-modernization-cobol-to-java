# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** COBOL Copybooks in `app/cpy/`
> **Purpose:** Business-friendly reference of all data entities, fields, types, and sizes extracted from PIC clauses.

---

## Table of Contents

1. [Account Entity](#1-account-entity-cvact01y)
2. [Card Entity](#2-card-entity-cvact02y)
3. [Card Cross-Reference](#3-card-cross-reference-cvact03y)
4. [Customer Entity](#4-customer-entity-cvcus01y)
5. [Transaction Entity](#5-transaction-entity-cvtra05y)
6. [Daily Transaction Entity](#6-daily-transaction-entity-cvtra06y)
7. [Transaction Category Balance](#7-transaction-category-balance-cvtra01y)
8. [Disclosure Group](#8-disclosure-group-cvtra02y)
9. [Transaction Type](#9-transaction-type-cvtra03y)
10. [Transaction Category](#10-transaction-category-cvtra04y)
11. [User Security Entity](#11-user-security-entity-csusr01y)
12. [Communication Area (COMMAREA)](#12-communication-area-cocom01y)
13. [Transaction Report Layout](#13-transaction-report-layout-cvtra07y)
14. [Statement Transaction Layout](#14-statement-transaction-layout-costm01)
15. [Customer Record (Statement)](#15-customer-record-statement-custrec)
16. [Card Detail Structure](#16-card-detail-structure-cvcrd01y)
17. [Export Record Layout](#17-export-record-layout-cvexport)
18. [Unused Record](#18-unused-record-unused1y)
19. [PIC Clause Reference](#pic-clause-reference)

---

## 1. Account Entity (`CVACT01Y`)

**Copybook:** `app/cpy/CVACT01Y.cpy` | **Record Size:** 300 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`

Represents a credit card account with financial and status information.

| Business Field              | COBOL Field Name             | PIC Clause        | Type      | Size   | Business Description                          |
|-----------------------------|------------------------------|--------------------|-----------|--------|-----------------------------------------------|
| Account ID                  | `ACCT-ID`                    | `9(11)`            | Numeric   | 11     | Unique account identifier (key)               |
| Account Status              | `ACCT-ACTIVE-STATUS`         | `X(01)`            | Alpha     | 1      | Active/Inactive flag                          |
| Current Balance             | `ACCT-CURR-BAL`              | `S9(10)V99`        | Signed Dec| 12,2   | Current account balance                       |
| Credit Limit                | `ACCT-CREDIT-LIMIT`          | `S9(10)V99`        | Signed Dec| 12,2   | Maximum credit allowed                        |
| Cash Credit Limit           | `ACCT-CASH-CREDIT-LIMIT`     | `S9(10)V99`        | Signed Dec| 12,2   | Maximum cash advance allowed                  |
| Open Date                   | `ACCT-OPEN-DATE`             | `X(10)`            | Alpha     | 10     | Date account was opened                       |
| Expiration Date             | `ACCT-EXPIRAION-DATE`        | `X(10)`            | Alpha     | 10     | Account expiration date                       |
| Reissue Date                | `ACCT-REISSUE-DATE`          | `X(10)`            | Alpha     | 10     | Last reissue date                             |
| Current Cycle Credit        | `ACCT-CURR-CYC-CREDIT`      | `S9(10)V99`        | Signed Dec| 12,2   | Credits in current billing cycle              |
| Current Cycle Debit         | `ACCT-CURR-CYC-DEBIT`       | `S9(10)V99`        | Signed Dec| 12,2   | Debits in current billing cycle               |
| Account Group ID            | `ACCT-GROUP-ID`              | `X(10)`            | Alpha     | 10     | Disclosure/interest rate group                |
| Filler                      | `FILLER`                     | `X(178)`           | Alpha     | 178    | Reserved space                                |

**Key:** `ACCT-ID` (11 bytes, numeric)

---

## 2. Card Entity (`CVACT02Y`)

**Copybook:** `app/cpy/CVACT02Y.cpy` | **Record Size:** 150 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`

Represents a physical credit card linked to an account.

| Business Field              | COBOL Field Name             | PIC Clause        | Type      | Size   | Business Description                          |
|-----------------------------|------------------------------|--------------------|-----------|--------|-----------------------------------------------|
| Card Number                 | `CARD-NUM`                   | `X(16)`            | Alpha     | 16     | 16-digit card number (key)                    |
| Card Account ID             | `CARD-ACCT-ID`               | `9(11)`            | Numeric   | 11     | Linked account ID                             |
| Card CVV Code               | `CARD-CVV-CD`                | `9(03)`            | Numeric   | 3      | Card verification value                       |
| Embossed Name               | `CARD-EMBOSSED-NAME`         | `X(50)`            | Alpha     | 50     | Name printed on card                          |
| Expiration Date             | `CARD-EXPIRAION-DATE`        | `X(10)`            | Alpha     | 10     | Card expiration date                          |
| Active Status               | `CARD-ACTIVE-STATUS`         | `X(01)`            | Alpha     | 1      | Card active/inactive flag                     |
| Filler                      | `FILLER`                     | `X(59)`            | Alpha     | 59     | Reserved space                                |

**Key:** `CARD-NUM` (16 bytes, alphanumeric)

---

## 3. Card Cross-Reference (`CVACT03Y`)

**Copybook:** `app/cpy/CVACT03Y.cpy` | **Record Size:** 50 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`

Maps card numbers to account IDs; supports alternate index on Account ID.

| Business Field              | COBOL Field Name             | PIC Clause        | Type      | Size   | Business Description                          |
|-----------------------------|------------------------------|--------------------|-----------|--------|-----------------------------------------------|
| Card Number                 | `XREF-CARD-NUM`              | `X(16)`            | Alpha     | 16     | Card number (primary key)                     |
| Account ID                  | `XREF-ACCT-ID`               | `9(11)`            | Numeric   | 11     | Associated account (alt key)                  |
| Customer ID                 | `XREF-CUST-ID`               | `9(09)`            | Numeric   | 9      | Associated customer ID                        |
| Filler                      | `FILLER`                     | `X(14)`            | Alpha     | 14     | Reserved space                                |

**Primary Key:** `XREF-CARD-NUM` (16 bytes) | **Alternate Key:** `XREF-ACCT-ID` (11 bytes, non-unique)

---

## 4. Customer Entity (`CVCUS01Y`)

**Copybook:** `app/cpy/CVCUS01Y.cpy` | **Record Size:** 500 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`

Master customer record with personal and demographic information.

| Business Field              | COBOL Field Name             | PIC Clause        | Type      | Size   | Business Description                          |
|-----------------------------|------------------------------|--------------------|-----------|--------|-----------------------------------------------|
| Customer ID                 | `CUST-ID`                    | `9(09)`            | Numeric   | 9      | Unique customer identifier (key)              |
| First Name                  | `CUST-FIRST-NAME`            | `X(25)`            | Alpha     | 25     | Customer first name                           |
| Middle Name                 | `CUST-MIDDLE-NAME`           | `X(25)`            | Alpha     | 25     | Customer middle name                          |
| Last Name                   | `CUST-LAST-NAME`             | `X(25)`            | Alpha     | 25     | Customer last name                            |
| Address Line 1              | `CUST-ADDR-LINE-1`           | `X(50)`            | Alpha     | 50     | Street address line 1                         |
| Address Line 2              | `CUST-ADDR-LINE-2`           | `X(50)`            | Alpha     | 50     | Street address line 2                         |
| Address Line 3              | `CUST-ADDR-LINE-3`           | `X(50)`            | Alpha     | 50     | Street address line 3                         |
| City                        | `CUST-ADDR-STATE-CD`         | `X(02)`            | Alpha     | 2      | State code                                    |
| Country Code                | `CUST-ADDR-COUNTRY-CD`       | `X(03)`            | Alpha     | 3      | Country code                                  |
| Zip Code                    | `CUST-ADDR-ZIP`              | `X(10)`            | Alpha     | 10     | Zip/postal code                               |
| Phone Number 1              | `CUST-PHONE-NUM-1`           | `X(15)`            | Alpha     | 15     | Primary phone number                          |
| Phone Number 2              | `CUST-PHONE-NUM-2`           | `X(15)`            | Alpha     | 15     | Secondary phone number                        |
| SSN                         | `CUST-SSN`                   | `9(09)`            | Numeric   | 9      | Social Security Number (PII)                  |
| Government ID               | `CUST-GOVT-ISSUED-ID`        | `X(20)`            | Alpha     | 20     | Government-issued ID number                   |
| Date of Birth               | `CUST-DOB-YYYYMMDD`          | `X(10)`            | Alpha     | 10     | Date of birth                                 |
| EFT Account ID              | `CUST-EFT-ACCOUNT-ID`        | `X(10)`            | Alpha     | 10     | Electronic funds transfer account             |
| Primary Holder Indicator    | `CUST-PRI-CARD-HOLDER-IND`   | `X(01)`            | Alpha     | 1      | Primary cardholder flag (Y/N)                 |
| FICO Score                  | `CUST-FICO-CREDIT-SCORE`     | `9(03)`            | Numeric   | 3      | FICO credit score                             |
| Filler                      | `FILLER`                     | `X(168)`           | Alpha     | 168    | Reserved space                                |

**Key:** `CUST-ID` (9 bytes, numeric)

> **PII Warning:** This entity contains SSN, Date of Birth, and Government ID — requires special handling during migration.

---

## 5. Transaction Entity (`CVTRA05Y`)

**Copybook:** `app/cpy/CVTRA05Y.cpy` | **Record Size:** 350 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`

Core transaction record capturing all credit card activity.

| Business Field              | COBOL Field Name             | PIC Clause        | Type      | Size   | Business Description                          |
|-----------------------------|------------------------------|--------------------|-----------|--------|-----------------------------------------------|
| Transaction ID              | `TRAN-ID`                    | `X(16)`            | Alpha     | 16     | Unique transaction identifier (key)           |
| Transaction Type Code       | `TRAN-TYPE-CD`               | `X(02)`            | Alpha     | 2      | Type code (purchase, payment, etc.)           |
| Transaction Category Code   | `TRAN-CAT-CD`                | `9(04)`            | Numeric   | 4      | Category within type                          |
| Transaction Source          | `TRAN-SOURCE`                | `X(10)`            | Alpha     | 10     | Origin (online, POS, ATM, etc.)               |
| Description                 | `TRAN-DESC`                  | `X(100)`           | Alpha     | 100    | Free-text transaction description             |
| Amount                      | `TRAN-AMT`                   | `S9(09)V99`        | Signed Dec| 11,2   | Transaction amount                            |
| Merchant ID                 | `TRAN-MERCHANT-ID`           | `9(09)`            | Numeric   | 9      | Merchant identifier                           |
| Merchant Name               | `TRAN-MERCHANT-NAME`         | `X(50)`            | Alpha     | 50     | Merchant business name                        |
| Merchant City               | `TRAN-MERCHANT-CITY`         | `X(50)`            | Alpha     | 50     | Merchant city                                 |
| Merchant Zip                | `TRAN-MERCHANT-ZIP`          | `X(10)`            | Alpha     | 10     | Merchant zip/postal code                      |
| Card Number                 | `TRAN-CARD-NUM`              | `X(16)`            | Alpha     | 16     | Card used for transaction                     |
| Original Timestamp          | `TRAN-ORIG-TS`               | `X(26)`            | Alpha     | 26     | When transaction originated                   |
| Processed Timestamp         | `TRAN-PROC-TS`               | `X(26)`            | Alpha     | 26     | When transaction was processed                |
| Filler                      | `FILLER`                     | `X(20)`            | Alpha     | 20     | Reserved space                                |

**Key:** `TRAN-ID` (16 bytes, alphanumeric)

---

## 6. Daily Transaction Entity (`CVTRA06Y`)

**Copybook:** `app/cpy/CVTRA06Y.cpy` | **Record Size:** 350 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.DALYTRAN`

Staging area for daily unposted transactions. Identical structure to Transaction Entity.

| Business Field              | COBOL Field Name             | PIC Clause        | Type      | Size   | Business Description                          |
|-----------------------------|------------------------------|--------------------|-----------|--------|-----------------------------------------------|
| Daily Trans ID              | `DALYTRAN-ID`                | `X(16)`            | Alpha     | 16     | Daily transaction identifier                  |
| Type Code                   | `DALYTRAN-TYPE-CD`           | `X(02)`            | Alpha     | 2      | Transaction type code                         |
| Category Code               | `DALYTRAN-CAT-CD`            | `9(04)`            | Numeric   | 4      | Transaction category                          |
| Source                      | `DALYTRAN-SOURCE`            | `X(10)`            | Alpha     | 10     | Transaction source                            |
| Description                 | `DALYTRAN-DESC`              | `X(100)`           | Alpha     | 100    | Transaction description                       |
| Amount                      | `DALYTRAN-AMT`               | `S9(09)V99`        | Signed Dec| 11,2   | Transaction amount                            |
| Merchant ID                 | `DALYTRAN-MERCHANT-ID`       | `9(09)`            | Numeric   | 9      | Merchant identifier                           |
| Merchant Name               | `DALYTRAN-MERCHANT-NAME`     | `X(50)`            | Alpha     | 50     | Merchant name                                 |
| Merchant City               | `DALYTRAN-MERCHANT-CITY`     | `X(50)`            | Alpha     | 50     | Merchant city                                 |
| Merchant Zip                | `DALYTRAN-MERCHANT-ZIP`      | `X(10)`            | Alpha     | 10     | Merchant zip code                             |
| Card Number                 | `DALYTRAN-CARD-NUM`          | `X(16)`            | Alpha     | 16     | Card number used                              |
| Original Timestamp          | `DALYTRAN-ORIG-TS`           | `X(26)`            | Alpha     | 26     | Origination timestamp                         |
| Processed Timestamp         | `DALYTRAN-PROC-TS`           | `X(26)`            | Alpha     | 26     | Processing timestamp                          |
| Filler                      | `FILLER`                     | `X(20)`            | Alpha     | 20     | Reserved space                                |

---

## 7. Transaction Category Balance (`CVTRA01Y`)

**Copybook:** `app/cpy/CVTRA01Y.cpy` | **Record Size:** 50 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TCATBAL`

Running balance per account per transaction category — used for interest calculations.

| Business Field              | COBOL Field Name             | PIC Clause        | Type      | Size   | Business Description                          |
|-----------------------------|------------------------------|--------------------|-----------|--------|-----------------------------------------------|
| Account ID                  | `TRANCAT-ACCT-ID`            | `9(11)`            | Numeric   | 11     | Account identifier (composite key part)       |
| Transaction Type Code       | `TRANCAT-TYPE-CD`            | `X(02)`            | Alpha     | 2      | Transaction type (composite key part)         |
| Category Code               | `TRANCAT-CD`                 | `9(04)`            | Numeric   | 4      | Category code (composite key part)            |
| Category Balance            | `TRAN-CAT-BAL`               | `S9(09)V99`        | Signed Dec| 11,2   | Running balance for this category             |
| Filler                      | `FILLER`                     | `X(22)`            | Alpha     | 22     | Reserved space                                |

**Composite Key:** `TRANCAT-ACCT-ID` + `TRANCAT-TYPE-CD` + `TRANCAT-CD` (17 bytes)

---

## 8. Disclosure Group (`CVTRA02Y`)

**Copybook:** `app/cpy/CVTRA02Y.cpy` | **Record Size:** 50 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.DISCGRP`

Interest rate configuration per account group and transaction type.

| Business Field              | COBOL Field Name             | PIC Clause        | Type      | Size   | Business Description                          |
|-----------------------------|------------------------------|--------------------|-----------|--------|-----------------------------------------------|
| Account Group ID            | `DIS-ACCT-GROUP-ID`          | `X(10)`            | Alpha     | 10     | Disclosure group identifier                   |
| Transaction Type Code       | `DIS-TRAN-TYPE-CD`           | `X(02)`            | Alpha     | 2      | Transaction type                              |
| Category Code               | `DIS-TRAN-CAT-CD`            | `9(04)`            | Numeric   | 4      | Transaction category                          |
| Interest Rate               | `DIS-INT-RATE`               | `S9(04)V99`        | Signed Dec| 6,2    | Applicable interest rate (%)                  |
| Filler                      | `FILLER`                     | `X(28)`            | Alpha     | 28     | Reserved space                                |

**Composite Key:** `DIS-ACCT-GROUP-ID` + `DIS-TRAN-TYPE-CD` + `DIS-TRAN-CAT-CD` (16 bytes)

---

## 9. Transaction Type (`CVTRA03Y`)

**Copybook:** `app/cpy/CVTRA03Y.cpy` | **Record Size:** 60 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TRANTYPE`

Lookup table for transaction type codes and descriptions.

| Business Field              | COBOL Field Name             | PIC Clause        | Type      | Size   | Business Description                          |
|-----------------------------|------------------------------|--------------------|-----------|--------|-----------------------------------------------|
| Transaction Type            | `TRAN-TYPE`                  | `X(02)`            | Alpha     | 2      | Type code (key)                               |
| Type Description            | `TRAN-TYPE-DESC`             | `X(50)`            | Alpha     | 50     | Human-readable description                    |
| Filler                      | `FILLER`                     | `X(08)`            | Alpha     | 8      | Reserved space                                |

**Key:** `TRAN-TYPE` (2 bytes)

---

## 10. Transaction Category (`CVTRA04Y`)

**Copybook:** `app/cpy/CVTRA04Y.cpy` | **Record Size:** 60 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TRANCATG`

Lookup table for transaction categories within each type.

| Business Field              | COBOL Field Name             | PIC Clause        | Type      | Size   | Business Description                          |
|-----------------------------|------------------------------|--------------------|-----------|--------|-----------------------------------------------|
| Type Code                   | `TRAN-TYPE-CD`               | `X(02)`            | Alpha     | 2      | Transaction type (composite key part)         |
| Category Code               | `TRAN-CAT-CD`                | `9(04)`            | Numeric   | 4      | Category code (composite key part)            |
| Category Description        | `TRAN-CAT-TYPE-DESC`         | `X(50)`            | Alpha     | 50     | Category description                          |
| Filler                      | `FILLER`                     | `X(04)`            | Alpha     | 4      | Reserved space                                |

**Composite Key:** `TRAN-TYPE-CD` + `TRAN-CAT-CD` (6 bytes)

---

## 11. User Security Entity (`CSUSR01Y`)

**Copybook:** `app/cpy/CSUSR01Y.cpy` | **Record Size:** 80 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`

User authentication and authorization record.

| Business Field              | COBOL Field Name             | PIC Clause        | Type      | Size   | Business Description                          |
|-----------------------------|------------------------------|--------------------|-----------|--------|-----------------------------------------------|
| User ID                     | `SEC-USR-ID`                 | `X(08)`            | Alpha     | 8      | Login user ID (key)                           |
| First Name                  | `SEC-USR-FNAME`              | `X(20)`            | Alpha     | 20     | User first name                               |
| Last Name                   | `SEC-USR-LNAME`              | `X(20)`            | Alpha     | 20     | User last name                                |
| Password                    | `SEC-USR-PWD`                | `X(08)`            | Alpha     | 8      | User password (plaintext — security risk)     |
| User Type                   | `SEC-USR-TYPE`               | `X(01)`            | Alpha     | 1      | User role: 'A' = Admin, 'U' = Regular        |
| Filler                      | `SEC-USR-FILLER`             | `X(23)`            | Alpha     | 23     | Reserved space                                |

**Key:** `SEC-USR-ID` (8 bytes)

> **Security Warning:** Passwords stored in plaintext. Must implement hashing during modernization.

---

## 12. Communication Area (`COCOM01Y`)

**Copybook:** `app/cpy/COCOM01Y.cpy` | **Purpose:** CICS COMMAREA — shared state between CICS programs.

This is the central data structure passed between all online CICS programs via XCTL/LINK. It carries navigation state, user session data, and inter-program communication parameters.

| Business Field              | COBOL Field Name             | PIC Clause        | Description                                   |
|-----------------------------|------------------------------|--------------------|-----------------------------------------------|
| Current Transaction ID      | `CDEMO-FROM-TRANID`          | `X(04)`            | Source CICS transaction ID                    |
| Current Program             | `CDEMO-FROM-PROGRAM`         | `X(08)`            | Source program name                           |
| Target Program              | `CDEMO-TO-PROGRAM`           | `X(08)`            | Destination program for XCTL                  |
| Target Transaction          | `CDEMO-TO-TRANID`            | `X(04)`            | Destination transaction ID                    |
| User ID                     | `CDEMO-USER-ID`              | `X(08)`            | Logged-in user ID                             |
| User Type                   | `CDEMO-USER-TYPE`            | `X(01)`            | User role (Admin/Regular)                     |
| Current Account ID          | `CDEMO-ACCT-ID`              | `9(11)`            | Selected account context                      |
| Current Card Number         | `CDEMO-CARD-NUM`             | `X(16)`            | Selected card context                         |
| Last Map                    | `CDEMO-LAST-MAP`             | `X(07)`            | Previously displayed BMS map                  |
| Last Mapset                 | `CDEMO-LAST-MAPSET`          | `X(07)`            | Previously used BMS mapset                    |
| PF-Key Entered              | `CDEMO-PFK-ENTERED`          | `X(02)`            | Function key pressed by user                  |

---

## 13. Transaction Report Layout (`CVTRA07Y`)

**Copybook:** `app/cpy/CVTRA07Y.cpy` | **Purpose:** Report formatting structures for batch transaction reports.

| Structure                    | Business Purpose                         |
|-----------------------------|------------------------------------------|
| `REPORT-NAME-HEADER`        | Report title, date range header          |
| `TRANSACTION-DETAIL-REPORT` | Per-transaction line in report           |
| `TRANSACTION-HEADER-1`      | Column headers for report                |
| `TRANSACTION-HEADER-2`      | Separator line (133 dashes)              |
| `REPORT-PAGE-TOTALS`        | Page subtotals                           |
| `REPORT-ACCOUNT-TOTALS`     | Per-account totals                       |
| `REPORT-GRAND-TOTALS`       | Grand total across all accounts          |

---

## 14. Statement Transaction Layout (`COSTM01`)

**Copybook:** `app/cpy/COSTM01.CPY` | **Record Size:** 350 bytes | **Purpose:** Re-keyed transaction layout for statement generation (key = Card Number + Transaction ID).

| Business Field              | COBOL Field Name             | PIC Clause        | Size   | Business Description                          |
|-----------------------------|------------------------------|--------------------|--------|-----------------------------------------------|
| Card Number                 | `TRNX-CARD-NUM`              | `X(16)`            | 16     | Card number (part of composite key)           |
| Transaction ID              | `TRNX-ID`                    | `X(16)`            | 16     | Transaction ID (part of composite key)        |
| Type Code                   | `TRNX-TYPE-CD`               | `X(02)`            | 2      | Transaction type                              |
| Category Code               | `TRNX-CAT-CD`                | `9(04)`            | 4      | Transaction category                          |
| Source                      | `TRNX-SOURCE`                | `X(10)`            | 10     | Transaction source                            |
| Description                 | `TRNX-DESC`                  | `X(100)`           | 100    | Transaction description                       |
| Amount                      | `TRNX-AMT`                   | `S9(09)V99`        | 11,2   | Transaction amount                            |
| Merchant ID                 | `TRNX-MERCHANT-ID`           | `9(09)`            | 9      | Merchant identifier                           |
| Merchant Name               | `TRNX-MERCHANT-NAME`         | `X(50)`            | 50     | Merchant name                                 |
| Merchant City               | `TRNX-MERCHANT-CITY`         | `X(50)`            | 50     | Merchant city                                 |
| Merchant Zip                | `TRNX-MERCHANT-ZIP`          | `X(10)`            | 10     | Merchant zip code                             |
| Original Timestamp          | `TRNX-ORIG-TS`               | `X(26)`            | 26     | Origination timestamp                         |
| Processed Timestamp         | `TRNX-PROC-TS`               | `X(26)`            | 26     | Processing timestamp                          |

**Composite Key:** `TRNX-CARD-NUM` + `TRNX-ID` (32 bytes)

---

## 15. Customer Record — Statement (`CUSTREC`)

**Copybook:** `app/cpy/CUSTREC.cpy` | **Purpose:** Denormalized customer + account record used by statement generation batch program (CBSTM03A).

Contains flattened fields from Customer, Account, and Card entities for efficient batch reporting.

---

## 16. Card Detail Structure (`CVCRD01Y`)

**Copybook:** `app/cpy/CVCRD01Y.cpy` | **Purpose:** Internal working storage structure for card detail screens.

Used by online CICS programs (COCRDLIC, COCRDSLC, COCRDUPC) to manage card browsing and editing state.

---

## 17. Export Record Layout (`CVEXPORT`)

**Copybook:** `app/cpy/CVEXPORT.cpy` | **Purpose:** Record layout for data export/import operations.

Defines the structure used by CBEXPORT and CBIMPORT programs for bulk data migration between VSAM and sequential files. Contains fields for:

| Business Field              | COBOL Field Name               | PIC Clause        | Size   | Description                                   |
|-----------------------------|--------------------------------|--------------------|--------|-----------------------------------------------|
| Card Account ID             | `EXP-CARD-ACCT-ID`            | `9(11) COMP`       | 11     | Account ID (computational)                    |
| Card CVV Code               | `EXP-CARD-CVV-CD`             | `9(03) COMP`       | 3      | CVV code (computational)                      |
| Embossed Name               | `EXP-CARD-EMBOSSED-NAME`      | `X(50)`            | 50     | Name on card                                  |
| Expiration Date             | `EXP-CARD-EXPIRAION-DATE`     | `X(10)`            | 10     | Card expiration date                          |
| Active Status               | `EXP-CARD-ACTIVE-STATUS`      | `X(01)`            | 1      | Active/inactive flag                          |

---

## 18. Unused Record (`UNUSED1Y`)

**Copybook:** `app/cpy/UNUSED1Y.cpy` | **Record Size:** 80 bytes | **Purpose:** Placeholder/deprecated record — not actively used.

| Business Field              | COBOL Field Name             | PIC Clause        | Size   |
|-----------------------------|------------------------------|--------------------|--------|
| ID                          | `UNUSED-ID`                  | `X(08)`            | 8      |
| First Name                  | `UNUSED-FNAME`               | `X(20)`            | 20     |
| Last Name                   | `UNUSED-LNAME`               | `X(20)`            | 20     |
| Password                    | `UNUSED-PWD`                 | `X(08)`            | 8      |
| Type                        | `UNUSED-TYPE`                | `X(01)`            | 1      |
| Filler                      | `UNUSED-FILLER`              | `X(23)`            | 23     |

> **Note:** Structure mirrors CSUSR01Y — likely an older version of the user security record.

---

## PIC Clause Reference

For readers unfamiliar with COBOL PIC clauses:

| PIC Pattern   | Meaning                                   | Java Equivalent      |
|---------------|-------------------------------------------|----------------------|
| `X(n)`        | Alphanumeric, n characters                | `String`             |
| `9(n)`        | Unsigned numeric, n digits                | `long` / `int`       |
| `S9(n)V99`    | Signed numeric with 2 decimal places      | `BigDecimal`         |
| `S9(n)V9(m)`  | Signed numeric with m decimal places      | `BigDecimal`         |
| `9(n) COMP`   | Binary/computational numeric              | `int` / `long`       |

### Entity Relationship Summary

```
Customer (CVCUS01Y)
  └── 1:N → Card Cross-Reference (CVACT03Y)
                ├── → Account (CVACT01Y)
                └── → Card (CVACT02Y)

Account (CVACT01Y)
  ├── 1:N → Transaction (CVTRA05Y) via Card
  ├── 1:N → Category Balance (CVTRA01Y)
  └── N:1 → Disclosure Group (CVTRA02Y) via Account Group

Transaction (CVTRA05Y)
  ├── N:1 → Transaction Type (CVTRA03Y)
  └── N:1 → Transaction Category (CVTRA04Y)

Daily Transaction (CVTRA06Y)  →  staged  →  Transaction (CVTRA05Y)

User Security (CSUSR01Y)  — independent authentication entity
```
