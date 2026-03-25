# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Source**: Copybook PIC clause analysis across `app/cpy/`, `app/cpy-bms/`, and optional modules
> **Application**: AWS CardDemo — Mainframe Credit Card Management System

---

## Overview

This dictionary extracts every business entity from the CardDemo copybook record layouts, translating COBOL PIC clauses into a business-friendly format. Each entity maps to a VSAM KSDS file on the mainframe and will become a relational database table (or Java POJO/DTO) in the modernized application.

### COBOL PIC Clause Quick Reference

| PIC Pattern        | Meaning                     | Java Equivalent       |
|--------------------|-----------------------------|-----------------------|
| `PIC X(n)`         | Alphanumeric, n characters  | `String`              |
| `PIC 9(n)`         | Unsigned integer, n digits  | `int` / `long`        |
| `PIC S9(n)`        | Signed integer              | `int` / `long`        |
| `PIC S9(n)V99`     | Signed decimal, 2 places    | `BigDecimal`          |
| `PIC S9(n) COMP`   | Binary integer              | `int` / `long`        |
| `PIC S9(n) COMP-3` | Packed decimal              | `BigDecimal`          |

---

## 1. Account Entity

**Copybook**: `CVACT01Y.cpy` | **Record Length**: 300 bytes | **VSAM File**: `ACCTDATA.VSAM.KSDS`

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| ACCT-ID                  | `9(11)`            | Numeric    | 11     | Account number (primary key)         |
| ACCT-ACTIVE-STATUS       | `X(01)`            | Alpha      | 1      | Account status (Y=Active, N=Closed)  |
| ACCT-CURR-BAL            | `S9(10)V99`        | Decimal    | 12.2   | Current account balance              |
| ACCT-CREDIT-LIMIT        | `S9(10)V99`        | Decimal    | 12.2   | Credit limit                         |
| ACCT-CASH-CREDIT-LIMIT   | `S9(10)V99`        | Decimal    | 12.2   | Cash advance credit limit            |
| ACCT-OPEN-DATE           | `X(10)`            | Date       | 10     | Account open date                    |
| ACCT-EXPIRAION-DATE      | `X(10)`            | Date       | 10     | Account expiration date              |
| ACCT-REISSUE-DATE        | `X(10)`            | Date       | 10     | Last card reissue date               |
| ACCT-CURR-CYC-CREDIT     | `S9(10)V99`        | Decimal    | 12.2   | Current cycle credits                |
| ACCT-CURR-CYC-DEBIT      | `S9(10)V99`        | Decimal    | 12.2   | Current cycle debits                 |
| ACCT-GROUP-ID            | `X(10)`            | Alpha      | 10     | Disclosure/rate group ID             |
| FILLER                   | `X(178)`           | —          | 178    | Reserved                             |

**Key**: `ACCT-ID` (11-digit account number)

---

## 2. Card Entity

**Copybook**: `CVACT02Y.cpy` | **Record Length**: 150 bytes | **VSAM File**: `CARDDATA.VSAM.KSDS`

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| CARD-NUM                 | `X(16)`            | Alpha      | 16     | Card number (primary key)            |
| CARD-ACCT-ID             | `9(11)`            | Numeric    | 11     | Parent account number (FK)           |
| CARD-CVV-CD              | `9(03)`            | Numeric    | 3      | Card verification value (CVV)        |
| CARD-EMBOSSED-NAME       | `X(50)`            | Alpha      | 50     | Name embossed on card                |
| CARD-EXPIRAION-DATE      | `X(10)`            | Date       | 10     | Card expiration date                 |
| CARD-ACTIVE-STATUS       | `X(01)`            | Alpha      | 1      | Card status (Y=Active, N=Inactive)   |
| FILLER                   | `X(59)`            | —          | 59     | Reserved                             |

**Key**: `CARD-NUM` (16-character card number)

---

## 3. Card Extended Detail Entity

**Copybook**: `CVCRD01Y.cpy` | **VSAM File**: `CARDDATA.VSAM.KSDS` (extended view)

| Field Name                     | COBOL PIC          | Type       | Length | Business Description                |
|--------------------------------|--------------------|------------|--------|-------------------------------------|
| CCARD-ACCT-ID-N                | `9(11)`            | Numeric    | 11     | Account number                      |
| CCARD-CARD-NUM-N               | `X(16)`            | Alpha      | 16     | Card number                         |
| CCARD-CVV-CD-N                 | `9(03)`            | Numeric    | 3      | CVV code                            |
| CCARD-EMBOSSED-NAME            | `X(50)`            | Alpha      | 50     | Embossed cardholder name            |
| CCARD-EXPIRAION-DATE           | `X(10)`            | Date       | 10     | Expiration date                     |
| CCARD-ACTIVE-STATUS            | `X(01)`            | Alpha      | 1      | Active status flag                  |
| CCARD-ACCT-CURR-BAL-N          | `S9(10)V99`        | Decimal    | 12.2   | Account balance (denormalized)      |
| CCARD-ACCT-CREDIT-LIMIT-N      | `S9(10)V99`        | Decimal    | 12.2   | Credit limit (denormalized)         |
| CCARD-ACCT-CASH-CREDIT-LIMIT-N | `S9(10)V99`        | Decimal    | 12.2   | Cash limit (denormalized)           |
| CCARD-ACCT-OPEN-DATE           | `X(10)`            | Date       | 10     | Account open date (denormalized)    |
| CCARD-ACCT-EXPIRAION-DATE      | `X(10)`            | Date       | 10     | Account expiry (denormalized)       |
| CCARD-ACCT-GROUP-ID            | `X(10)`            | Alpha      | 10     | Disclosure group (denormalized)     |

> **Note**: This copybook is a denormalized view combining Card + Account fields, used by online CICS screens for display purposes.

---

## 4. Customer Entity

**Copybook**: `CVCUS01Y.cpy` | **Record Length**: 500 bytes | **VSAM File**: `CUSTDATA.VSAM.KSDS`

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| CUST-ID                  | `9(09)`            | Numeric    | 9      | Customer ID (primary key)            |
| CUST-FIRST-NAME          | `X(25)`            | Alpha      | 25     | First name                           |
| CUST-MIDDLE-NAME         | `X(25)`            | Alpha      | 25     | Middle name                          |
| CUST-LAST-NAME           | `X(25)`            | Alpha      | 25     | Last name                            |
| CUST-ADDR-LINE-1         | `X(50)`            | Alpha      | 50     | Address line 1                       |
| CUST-ADDR-LINE-2         | `X(50)`            | Alpha      | 50     | Address line 2                       |
| CUST-ADDR-LINE-3         | `X(50)`            | Alpha      | 50     | Address line 3                       |
| CUST-ADDR-STATE-CD       | `X(02)`            | Alpha      | 2      | State code                           |
| CUST-ADDR-COUNTRY-CD     | `X(03)`            | Alpha      | 3      | Country code                         |
| CUST-ADDR-ZIP            | `X(10)`            | Alpha      | 10     | ZIP/postal code                      |
| CUST-PHONE-NUM-1         | `X(15)`            | Alpha      | 15     | Primary phone number                 |
| CUST-PHONE-NUM-2         | `X(15)`            | Alpha      | 15     | Secondary phone number               |
| CUST-SSN                 | `9(09)`            | Numeric    | 9      | Social Security Number (PII)         |
| CUST-GOVT-ISSUED-ID      | `X(20)`            | Alpha      | 20     | Government-issued ID                 |
| CUST-DOB-YYYYMMDD        | `X(10)`            | Date       | 10     | Date of birth                        |
| CUST-EFT-ACCOUNT-ID      | `X(10)`            | Alpha      | 10     | EFT/bank account for payments        |
| CUST-PRI-CARD-HOLDER-IND | `X(01)`            | Alpha      | 1      | Primary cardholder indicator         |
| CUST-FICO-CREDIT-SCORE   | `9(03)`            | Numeric    | 3      | FICO credit score                    |
| FILLER                   | `X(168)`           | —          | 168    | Reserved                             |

**Key**: `CUST-ID` (9-digit customer ID)

> **PII Fields**: `CUST-SSN`, `CUST-DOB-YYYYMMDD`, `CUST-GOVT-ISSUED-ID` — require masking/encryption in modernized system.

---

## 5. Card Cross-Reference Entity

**Copybook**: `CVACT03Y.cpy` | **Record Length**: 50 bytes | **VSAM File**: `CARDXREF.VSAM.KSDS`

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| XREF-CARD-NUM            | `X(16)`            | Alpha      | 16     | Card number (primary key)            |
| XREF-CUST-ID             | `9(09)`            | Numeric    | 9      | Customer ID (FK to Customer)         |
| XREF-ACCT-ID             | `9(11)`            | Numeric    | 11     | Account ID (FK to Account)           |
| FILLER                   | `X(14)`            | —          | 14     | Reserved                             |

**Key**: `XREF-CARD-NUM` | **Alternate Index**: `XREF-ACCT-ID`

> **Purpose**: Links Card → Customer → Account. This is the central join table for the entire application. Every transaction lookup starts here.

---

## 6. Transaction Entity

**Copybook**: `CVTRA05Y.cpy` | **Record Length**: 350 bytes | **VSAM File**: `TRANSACT.VSAM.KSDS`

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| TRAN-ID                  | `X(16)`            | Alpha      | 16     | Transaction ID (primary key)         |
| TRAN-TYPE-CD             | `X(02)`            | Alpha      | 2      | Transaction type code (FK)           |
| TRAN-CAT-CD              | `9(04)`            | Numeric    | 4      | Transaction category code (FK)       |
| TRAN-SOURCE              | `X(10)`            | Alpha      | 10     | Transaction source/channel           |
| TRAN-DESC                | `X(100)`           | Alpha      | 100    | Transaction description              |
| TRAN-AMT                 | `S9(09)V99`        | Decimal    | 11.2   | Transaction amount                   |
| TRAN-MERCHANT-ID         | `9(09)`            | Numeric    | 9      | Merchant identifier                  |
| TRAN-MERCHANT-NAME       | `X(50)`            | Alpha      | 50     | Merchant name                        |
| TRAN-MERCHANT-CITY       | `X(50)`            | Alpha      | 50     | Merchant city                        |
| TRAN-MERCHANT-ZIP        | `X(10)`            | Alpha      | 10     | Merchant ZIP code                    |
| TRAN-CARD-NUM            | `X(16)`            | Alpha      | 16     | Card number (FK to Card)             |
| TRAN-ORIG-TS             | `X(26)`            | Timestamp  | 26     | Original transaction timestamp       |
| TRAN-PROC-TS             | `X(26)`            | Timestamp  | 26     | Processing timestamp                 |
| FILLER                   | `X(20)`            | —          | 20     | Reserved                             |

**Key**: `TRAN-ID` (16-character transaction ID)

---

## 7. Daily Transaction Entity

**Copybook**: `CVTRA06Y.cpy` | **Record Length**: 350 bytes | **VSAM File**: `DALYTRAN`

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| DALYTRAN-ID              | `X(16)`            | Alpha      | 16     | Daily transaction ID                 |
| DALYTRAN-TYPE-CD         | `X(02)`            | Alpha      | 2      | Transaction type code                |
| DALYTRAN-CAT-CD          | `9(04)`            | Numeric    | 4      | Transaction category code            |
| DALYTRAN-SOURCE          | `X(10)`            | Alpha      | 10     | Transaction source                   |
| DALYTRAN-DESC            | `X(100)`           | Alpha      | 100    | Description                          |
| DALYTRAN-AMT             | `S9(09)V99`        | Decimal    | 11.2   | Amount                               |
| DALYTRAN-MERCHANT-ID     | `9(09)`            | Numeric    | 9      | Merchant ID                          |
| DALYTRAN-MERCHANT-NAME   | `X(50)`            | Alpha      | 50     | Merchant name                        |
| DALYTRAN-MERCHANT-CITY   | `X(50)`            | Alpha      | 50     | Merchant city                        |
| DALYTRAN-MERCHANT-ZIP    | `X(10)`            | Alpha      | 10     | Merchant ZIP                         |
| DALYTRAN-CARD-NUM        | `X(16)`            | Alpha      | 16     | Card number                          |
| DALYTRAN-ORIG-TS         | `X(26)`            | Timestamp  | 26     | Original timestamp                   |
| DALYTRAN-PROC-TS         | `X(26)`            | Timestamp  | 26     | Processing timestamp                 |
| FILLER                   | `X(20)`            | —          | 20     | Reserved                             |

> **Note**: Identical structure to Transaction (CVTRA05Y) but represents the daily staging file. Transactions flow: `DALYTRAN` → `CBTRN02C` (posting) → `TRANSACT`.

---

## 8. Transaction Category Balance Entity

**Copybook**: `CVTRA01Y.cpy` | **Record Length**: 50 bytes | **VSAM File**: `TCATBALF`

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| TRANCAT-ACCT-ID          | `9(11)`            | Numeric    | 11     | Account ID (part of composite key)   |
| TRANCAT-TYPE-CD          | `X(02)`            | Alpha      | 2      | Transaction type (part of key)       |
| TRANCAT-CD               | `9(04)`            | Numeric    | 4      | Category code (part of key)          |
| TRAN-CAT-BAL             | `S9(09)V99`        | Decimal    | 11.2   | Running balance for this category    |
| FILLER                   | `X(22)`            | —          | 22     | Reserved                             |

**Composite Key**: `TRANCAT-ACCT-ID` + `TRANCAT-TYPE-CD` + `TRANCAT-CD`

---

## 9. Disclosure Group Entity

**Copybook**: `CVTRA02Y.cpy` | **Record Length**: 50 bytes | **VSAM File**: `DISCGRP`

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| DIS-ACCT-GROUP-ID        | `X(10)`            | Alpha      | 10     | Account disclosure group ID          |
| DIS-TRAN-TYPE-CD         | `X(02)`            | Alpha      | 2      | Transaction type code                |
| DIS-TRAN-CAT-CD          | `9(04)`            | Numeric    | 4      | Transaction category code            |
| DIS-INT-RATE             | `S9(04)V99`        | Decimal    | 6.2    | Interest rate for this combination   |
| FILLER                   | `X(28)`            | —          | 28     | Reserved                             |

**Composite Key**: `DIS-ACCT-GROUP-ID` + `DIS-TRAN-TYPE-CD` + `DIS-TRAN-CAT-CD`

> **Purpose**: Maps account groups to interest rates by transaction type/category. Used by CBACT04C (interest calculation).

---

## 10. Transaction Type Entity

**Copybook**: `CVTRA03Y.cpy` | **Record Length**: 60 bytes | **VSAM File**: `TRANTYPE`

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| TRAN-TYPE                | `X(02)`            | Alpha      | 2      | Transaction type code (primary key)  |
| TRAN-TYPE-DESC           | `X(50)`            | Alpha      | 50     | Type description                     |
| FILLER                   | `X(08)`            | —          | 8      | Reserved                             |

**Key**: `TRAN-TYPE` (2-character code, e.g., "PR"=Purchase, "CA"=Cash Advance)

---

## 11. Transaction Category Entity

**Copybook**: `CVTRA04Y.cpy` | **Record Length**: 60 bytes | **VSAM File**: `TRANCATG`

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| TRAN-TYPE-CD             | `X(02)`            | Alpha      | 2      | Parent transaction type (part of key)|
| TRAN-CAT-CD              | `9(04)`            | Numeric    | 4      | Category code (part of key)          |
| TRAN-CAT-TYPE-DESC       | `X(50)`            | Alpha      | 50     | Category description                 |
| FILLER                   | `X(04)`            | —          | 4      | Reserved                             |

**Composite Key**: `TRAN-TYPE-CD` + `TRAN-CAT-CD`

---

## 12. User Security Entity

**Copybook**: `CSUSR01Y.cpy` | **Record Length**: 80 bytes | **VSAM File**: `USRSEC.VSAM.KSDS`

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| SEC-USR-ID               | `X(08)`            | Alpha      | 8      | User ID (primary key)                |
| SEC-USR-FNAME            | `X(20)`            | Alpha      | 20     | User first name                      |
| SEC-USR-LNAME            | `X(20)`            | Alpha      | 20     | User last name                       |
| SEC-USR-PWD              | `X(08)`            | Alpha      | 8      | Password (plaintext — modernize!)    |
| SEC-USR-TYPE             | `X(01)`            | Alpha      | 1      | User type (A=Admin, U=Regular)       |
| FILLER                   | `X(23)`            | —          | 23     | Reserved                             |

**Key**: `SEC-USR-ID`

> **Security Note**: Passwords are stored in plaintext. The modernized system must implement proper hashing (bcrypt/scrypt) and role-based access control.

---

## 13. Statement Transaction Layout

**Copybook**: `COSTM01.CPY` | **Used by**: CBSTM03A (Statement Generator)

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| TRNX-CARD-NUM            | `X(16)`            | Alpha      | 16     | Card number (part of composite key)  |
| TRNX-ID                  | `X(16)`            | Alpha      | 16     | Transaction ID (part of key)         |
| TRNX-TYPE-CD             | `X(02)`            | Alpha      | 2      | Transaction type code                |
| TRNX-CAT-CD              | `9(04)`            | Numeric    | 4      | Category code                        |
| TRNX-SOURCE              | `X(10)`            | Alpha      | 10     | Source channel                       |
| TRNX-DESC                | `X(100)`           | Alpha      | 100    | Description                          |
| TRNX-AMT                 | `S9(09)V99`        | Decimal    | 11.2   | Amount                               |
| TRNX-MERCHANT-ID         | `9(09)`            | Numeric    | 9      | Merchant ID                          |
| TRNX-MERCHANT-NAME       | `X(50)`            | Alpha      | 50     | Merchant name                        |
| TRNX-MERCHANT-CITY       | `X(50)`            | Alpha      | 50     | Merchant city                        |
| TRNX-MERCHANT-ZIP        | `X(10)`            | Alpha      | 10     | Merchant ZIP                         |
| TRNX-ORIG-TS             | `X(26)`            | Timestamp  | 26     | Original timestamp                   |
| TRNX-PROC-TS             | `X(26)`            | Timestamp  | 26     | Processing timestamp                 |
| FILLER                   | `X(20)`            | —          | 20     | Reserved                             |

**Key**: `TRNX-CARD-NUM` + `TRNX-ID` (sorted by card for statement grouping)

---

## 14. Customer Record (Batch Statement)

**Copybook**: `CUSTREC.cpy` | **Used by**: CBSTM03B

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| CUST-ID                  | `9(09)`            | Numeric    | 9      | Customer ID                          |
| CUST-FIRST-NAME          | `X(25)`            | Alpha      | 25     | First name                           |
| CUST-MIDDLE-NAME         | `X(25)`            | Alpha      | 25     | Middle name                          |
| CUST-LAST-NAME           | `X(25)`            | Alpha      | 25     | Last name                            |
| CUST-ADDR-LINE-1         | `X(50)`            | Alpha      | 50     | Address line 1                       |
| CUST-ADDR-LINE-2         | `X(50)`            | Alpha      | 50     | Address line 2                       |
| CUST-ADDR-LINE-3         | `X(50)`            | Alpha      | 50     | Address line 3                       |
| CUST-ADDR-STATE-CD       | `X(02)`            | Alpha      | 2      | State code                           |
| CUST-ADDR-COUNTRY-CD     | `X(03)`            | Alpha      | 3      | Country code                         |
| CUST-ADDR-ZIP            | `X(10)`            | Alpha      | 10     | ZIP code                             |
| CUST-PHONE-NUM-1         | `X(15)`            | Alpha      | 15     | Phone number 1                       |
| CUST-PHONE-NUM-2         | `X(15)`            | Alpha      | 15     | Phone number 2                       |
| CUST-SSN                 | `9(09)`            | Numeric    | 9      | SSN (PII)                            |
| CUST-GOVT-ISSUED-ID      | `X(20)`            | Alpha      | 20     | Government ID                        |
| CUST-DOB-YYYYMMDD        | `X(10)`            | Date       | 10     | Date of birth                        |
| CUST-EFT-ACCOUNT-ID      | `X(10)`            | Alpha      | 10     | EFT account                          |
| CUST-PRI-CARD-HOLDER-IND | `X(01)`            | Alpha      | 1      | Primary cardholder flag              |
| CUST-FICO-CREDIT-SCORE   | `9(03)`            | Numeric    | 3      | FICO score                           |
| FILLER                   | `X(168)`           | —          | 168    | Reserved                             |

---

## 15. Export Record (Multi-Entity)

**Copybook**: `CVEXPORT.cpy` | **Used by**: CBEXPORT, CBIMPORT

This copybook defines a unified export format that encapsulates all entity types in a single flat file. It uses `EXPORT-REC-TYPE` to discriminate between record types.

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| EXPORT-REC-TYPE          | `X(01)`            | Alpha      | 1      | Record discriminator (C/A/X/T/R)    |

**Record Types**:

| Code | Entity    | Prefix         | Description                |
|------|-----------|----------------|----------------------------|
| `C`  | Customer  | `EXP-CUST-*`  | Customer record export     |
| `A`  | Account   | `EXP-ACCT-*`  | Account record export      |
| `X`  | XRef      | `EXP-XREF-*`  | Card cross-reference export|
| `T`  | Transaction| `EXP-TRAN-*` | Transaction record export  |
| `R`  | Card      | `EXP-CARD-*`  | Card record export         |

Each sub-record mirrors the corresponding entity's field layout with `EXP-` prefixed field names.

---

## 16. Communication Area (COMMAREA)

**Copybook**: `COCOM01Y.cpy` | **Used by**: All CICS programs

| Field Name                   | COBOL PIC      | Type     | Length | Business Description              |
|------------------------------|----------------|----------|--------|-----------------------------------|
| CDEMO-FROM-TRANID            | `X(04)`        | Alpha    | 4      | Originating transaction ID        |
| CDEMO-FROM-PROGRAM           | `X(08)`        | Alpha    | 8      | Originating program name          |
| CDEMO-TO-TRANID              | `X(04)`        | Alpha    | 4      | Target transaction ID             |
| CDEMO-TO-PROGRAM             | `X(08)`        | Alpha    | 8      | Target program name               |
| CDEMO-USER-ID                | `X(08)`        | Alpha    | 8      | Current user ID                   |
| CDEMO-USER-TYPE              | `X(01)`        | Alpha    | 1      | User type (A=Admin, U=Regular)    |
| CDEMO-PGM-CONTEXT            | `9(01)`        | Numeric  | 1      | Program context flag              |
| CDEMO-ACCT-ID                | `9(11)`        | Numeric  | 11     | Selected account ID               |
| CDEMO-CARD-NUM               | `X(16)`        | Alpha    | 16     | Selected card number              |
| CDEMO-CUST-ID                | `9(09)`        | Numeric  | 9      | Selected customer ID              |
| CDEMO-LAST-MAP               | `X(07)`        | Alpha    | 7      | Last BMS map sent                 |
| CDEMO-LAST-MAPSET            | `X(07)`        | Alpha    | 7      | Last BMS mapset                   |

> **Purpose**: Passed between CICS programs via EXEC CICS XCTL/RETURN. Contains navigation state, user identity, and selected entity keys.

---

## 17. Report Layout

**Copybook**: `CVTRA07Y.cpy` | **Used by**: CBTRN03C

### Report Header

| Field Name               | COBOL PIC          | Type       | Length | Description                          |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| REPT-SHORT-NAME          | `X(38)`            | Alpha      | 38     | Report code (DALYREPT)               |
| REPT-LONG-NAME           | `X(41)`            | Alpha      | 41     | Report title                         |
| REPT-START-DATE          | `X(10)`            | Date       | 10     | Report period start                  |
| REPT-END-DATE            | `X(10)`            | Date       | 10     | Report period end                    |

### Transaction Detail Line

| Field Name                   | COBOL PIC      | Length | Description                          |
|------------------------------|----------------|--------|--------------------------------------|
| TRAN-REPORT-TRANS-ID         | `X(16)`        | 16     | Transaction ID                       |
| TRAN-REPORT-ACCOUNT-ID       | `X(11)`        | 11     | Account ID                           |
| TRAN-REPORT-TYPE-CD          | `X(02)`        | 2      | Type code                            |
| TRAN-REPORT-TYPE-DESC        | `X(15)`        | 15     | Type description                     |
| TRAN-REPORT-CAT-CD           | `9(04)`        | 4      | Category code                        |
| TRAN-REPORT-CAT-DESC         | `X(29)`        | 29     | Category description                 |
| TRAN-REPORT-SOURCE            | `X(10)`        | 10     | Source                               |
| TRAN-REPORT-AMT              | `-ZZZ,ZZZ,ZZZ.ZZ` | 15  | Formatted amount                     |

### Report Totals

| Field Name               | Format                 | Description                          |
|--------------------------|------------------------|--------------------------------------|
| REPT-PAGE-TOTAL          | `+ZZZ,ZZZ,ZZZ.ZZ`     | Page subtotal                        |
| REPT-ACCOUNT-TOTAL       | `+ZZZ,ZZZ,ZZZ.ZZ`     | Account subtotal                     |
| REPT-GRAND-TOTAL         | `+ZZZ,ZZZ,ZZZ.ZZ`     | Grand total                          |

---

## 18. Optional Module Entities

### 18.1 Authorization Request (IMS/DB2/MQ Module)

**Copybook**: `CCPAURQY.cpy`

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| AU-REQUEST-ID            | `X(16)`            | Alpha      | 16     | Authorization request ID             |
| AU-CARD-NUM              | `X(16)`            | Alpha      | 16     | Card number being authorized         |
| AU-TRAN-AMT              | `S9(09)V99`        | Decimal    | 11.2   | Authorization amount                 |
| AU-MERCHANT-ID           | `9(09)`            | Numeric    | 9      | Merchant requesting auth             |
| AU-TRAN-DT               | `X(10)`            | Date       | 10     | Transaction date                     |
| AU-TRAN-TM               | `X(08)`            | Alpha      | 8      | Transaction time                     |

### 18.2 Authorization Reply

**Copybook**: `CCPAURLY.cpy`

| Field Name               | COBOL PIC          | Type       | Length | Business Description                 |
|--------------------------|--------------------|------------|--------|--------------------------------------|
| AU-REPLY-ID              | `X(16)`            | Alpha      | 16     | Reply ID                             |
| AU-REPLY-CD              | `X(02)`            | Alpha      | 2      | Reply code (AP=Approved, DN=Denied)  |
| AU-REPLY-REASON          | `X(50)`            | Alpha      | 50     | Reason text                          |

### 18.3 IMS DB PCB Layouts

**Copybooks**: `PADFLPCB.CPY`, `PASFLPCB.CPY`, `PAUTBPCB.CPY`

| Field Name Pattern       | COBOL PIC          | Description                          |
|--------------------------|--------------------|--------------------------------------|
| *-DBDNAME                | `X(08)`            | IMS database name                    |
| *-SEG-LEVEL              | `X(02)`            | IMS segment level                    |
| *-PCB-STATUS             | `X(02)`            | IMS status code                      |
| *-PCB-PROCOPT            | `X(04)`            | Processing option (AIRD, etc.)       |
| *-SEG-NAME               | `X(08)`            | Segment name                         |
| *-KEYFB                  | `X(100-255)`       | Key feedback area                    |

### 18.4 DB2 Common Working Storage

**Copybook**: `CSDB2RWY.cpy`

| Field Name               | COBOL PIC          | Type       | Description                          |
|--------------------------|--------------------|------------|--------------------------------------|
| WS-DISP-SQLCODE          | `----9`            | Display    | Formatted SQLCODE for display        |
| WS-DUMMY-DB2-INT         | `S9(4) COMP-3`     | Packed     | Priming query result                 |
| WS-DB2-PROCESSING-FLAG   | `X(1)`             | Flag       | 0=OK, 1=Error                        |
| WS-DB2-CURRENT-ACTION    | `X(72)`            | Alpha      | Current DB2 action description       |
| WS-DSNTIAC-FORMATTED     | `X(720)`           | Alpha      | DSNTIAC formatted error message      |

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)
  │
  ├──< Card Cross-Reference (CVACT03Y) >──┤
  │         │                              │
  │         ▼                              ▼
  │    Card (CVACT02Y)              Account (CVACT01Y)
  │         │                              │
  │         ▼                              ├── Disclosure Group (CVTRA02Y)
  │    Transaction (CVTRA05Y)              │
  │         │                              ├── Tran Category Balance (CVTRA01Y)
  │         ├── Tran Type (CVTRA03Y)       │
  │         └── Tran Category (CVTRA04Y)   │
  │                                        │
  ▼                                        ▼
Daily Transaction (CVTRA06Y)         Statement Output
  │                                  (COSTM01 + CVTRA07Y)
  └── [Batch Posting] ──────────►
```

---

## VSAM File Inventory

| Logical Name       | Dataset Name (DSN)                          | Key Field(s)           | Record Len | Entity            |
|--------------------|---------------------------------------------|------------------------|------------|-------------------|
| ACCTFILE           | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS         | ACCT-ID (11)           | 300        | Account           |
| CARDFILE           | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS         | CARD-NUM (16)          | 150        | Card              |
| CUSTFILE           | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS         | CUST-ID (9)            | 500        | Customer          |
| CARDXREF           | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS         | XREF-CARD-NUM (16)     | 50         | Cross-Reference   |
| TRANSACT           | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS         | TRAN-ID (16)           | 350        | Transaction       |
| DALYTRAN           | AWS.M2.CARDDEMO.DALYTRAN.VSAM.KSDS         | DALYTRAN-ID (16)       | 350        | Daily Transaction |
| USRSEC             | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS           | SEC-USR-ID (8)         | 80         | User Security     |
| TCATBALF           | AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS          | Composite (17)         | 50         | Category Balance  |
| DISCGRP            | AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS          | Composite (16)         | 50         | Disclosure Group  |
| TRANTYPE           | AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS         | TRAN-TYPE (2)          | 60         | Transaction Type  |
| TRANCATG           | AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS         | Composite (6)          | 60         | Tran Category     |
