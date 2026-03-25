# CardDemo Data Dictionary

> **System**: AWS CardDemo -- Mainframe Credit Card Management System
> **Source**: COBOL Copybook PIC Clause Analysis
> **Generated**: 2026-03-25

---

## Overview

This document extracts every business data entity from the CardDemo COBOL copybooks and translates the mainframe PIC clauses into a business-friendly format suitable for Java/relational target modeling.

### PIC Clause Quick Reference

| COBOL PIC         | Meaning                        | Java Equivalent     | SQL Equivalent        |
|--------------------|--------------------------------|---------------------|-----------------------|
| `PIC X(n)`        | Alphanumeric, n characters      | `String`            | `VARCHAR(n)`          |
| `PIC 9(n)`        | Unsigned numeric, n digits      | `long` / `int`      | `NUMERIC(n)`          |
| `PIC S9(n)V99`    | Signed decimal, 2 decimal places| `BigDecimal`        | `NUMERIC(n+2, 2)`    |
| `PIC S9(n) COMP`  | Binary (half/full/double word)  | `int` / `long`      | `INTEGER` / `BIGINT`  |
| `PIC 9(n) COMP-3` | Packed decimal                  | `BigDecimal`        | `NUMERIC(n)`          |
| `FILLER PIC X(n)` | Reserved / padding              | -- (omit)           | -- (omit)             |

---

## 1. Account Entity

**Copybook**: `CVACT01Y.cpy` (20 lines, ~300-byte record)
**VSAM File**: `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`
**Business Description**: Stores credit card account master data including balances, limits, dates, and grouping.

| # | Field Name              | PIC Clause       | Length  | Type       | Business Description                    | Nullable | Key  |
|---|------------------------|------------------|---------|------------|------------------------------------------|----------|------|
| 1 | ACCT-ID                | `9(11)`          | 11 dig  | Numeric    | Unique account identifier                | No       | PK   |
| 2 | ACCT-ACTIVE-STATUS     | `X(01)`          | 1 char  | Flag       | Active status (`Y`/`N`)                  | No       |      |
| 3 | ACCT-CURR-BAL          | `S9(10)V99`      | 12,2    | Currency   | Current account balance                  | No       |      |
| 4 | ACCT-CREDIT-LIMIT      | `S9(10)V99`      | 12,2    | Currency   | Credit limit                             | No       |      |
| 5 | ACCT-CASH-CREDIT-LIMIT | `S9(10)V99`      | 12,2    | Currency   | Cash advance credit limit                | No       |      |
| 6 | ACCT-OPEN-DATE         | `X(10)`          | 10 char | Date       | Account opening date (YYYY-MM-DD)        | No       |      |
| 7 | ACCT-EXPIRAION-DATE    | `X(10)`          | 10 char | Date       | Account expiration date                  | No       |      |
| 8 | ACCT-REISSUE-DATE      | `X(10)`          | 10 char | Date       | Last card reissue date                   | Yes      |      |
| 9 | ACCT-CURR-CYC-CREDIT   | `S9(10)V99`      | 12,2    | Currency   | Current cycle credit total               | No       |      |
| 10| ACCT-CURR-CYC-DEBIT   | `S9(10)V99`      | 12,2    | Currency   | Current cycle debit total                | No       |      |
| 11| ACCT-ADDR-ZIP          | `X(10)`          | 10 char | Text       | Account holder ZIP code                  | Yes      |      |
| 12| ACCT-GROUP-ID          | `X(10)`          | 10 char | Text       | Account group identifier (for interest)  | Yes      | FK   |
| -- | FILLER                | `X(178)`         | 178     | Padding    | Reserved space                           | --       | --   |

**Business Rules**:
- ACCT-ID is the VSAM KSDS primary key
- ACCT-GROUP-ID links to the Discount Group entity for interest rate determination
- Spelling note: `EXPIRAION` is the original field name (missing letter "T")

---

## 2. Card Entity

**Copybook**: `CVACT02Y.cpy` (14 lines, ~150-byte record)
**VSAM File**: `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`
**Business Description**: Stores physical credit card details linked to an account.

| # | Field Name              | PIC Clause       | Length  | Type       | Business Description                    | Nullable | Key  |
|---|------------------------|------------------|---------|------------|------------------------------------------|----------|------|
| 1 | CARD-NUM               | `X(16)`          | 16 char | Text       | Card number (PAN)                        | No       | PK   |
| 2 | CARD-ACCT-ID           | `9(11)`          | 11 dig  | Numeric    | Parent account ID                        | No       | FK   |
| 3 | CARD-CVV-CD            | `9(03)`          | 3 dig   | Numeric    | Card verification value (CVV)            | No       |      |
| 4 | CARD-EMBOSSED-NAME     | `X(50)`          | 50 char | Text       | Name embossed on card                    | No       |      |
| 5 | CARD-EXPIRAION-DATE    | `X(10)`          | 10 char | Date       | Card expiration date                     | No       |      |
| 6 | CARD-ACTIVE-STATUS     | `X(01)`          | 1 char  | Flag       | Card active status (`Y`/`N`)             | No       |      |
| -- | FILLER                | `X(59)`          | 59      | Padding    | Reserved space                           | --       | --   |

**Business Rules**:
- CARD-NUM is the VSAM KSDS primary key
- Multiple cards can belong to one account (CARD-ACCT-ID -> ACCT-ID)
- CVV is stored as cleartext in mainframe (security concern for modernization)

---

## 3. Customer Entity

**Copybook**: `CVCUS01Y.cpy` (26 lines, ~500-byte record)
**VSAM File**: `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`
**Business Description**: Stores customer personal and contact information.

| # | Field Name                | PIC Clause   | Length  | Type       | Business Description                | Nullable | Key  |
|---|--------------------------|--------------|---------|------------|--------------------------------------|----------|------|
| 1 | CUST-ID                  | `9(09)`      | 9 dig   | Numeric    | Unique customer identifier           | No       | PK   |
| 2 | CUST-FIRST-NAME          | `X(25)`      | 25 char | Text       | Customer first name                  | No       |      |
| 3 | CUST-MIDDLE-NAME         | `X(25)`      | 25 char | Text       | Customer middle name                 | Yes      |      |
| 4 | CUST-LAST-NAME           | `X(25)`      | 25 char | Text       | Customer last name                   | No       |      |
| 5 | CUST-ADDR-LINE-1         | `X(50)`      | 50 char | Text       | Address line 1                       | No       |      |
| 6 | CUST-ADDR-LINE-2         | `X(50)`      | 50 char | Text       | Address line 2                       | Yes      |      |
| 7 | CUST-ADDR-LINE-3         | `X(50)`      | 50 char | Text       | Address line 3                       | Yes      |      |
| 8 | CUST-ADDR-STATE-CD       | `X(02)`      | 2 char  | Code       | US state code                        | No       |      |
| 9 | CUST-ADDR-COUNTRY-CD     | `X(03)`      | 3 char  | Code       | Country code                         | No       |      |
| 10| CUST-ADDR-ZIP            | `X(10)`      | 10 char | Text       | ZIP / postal code                    | No       |      |
| 11| CUST-PHONE-NUM-1         | `X(15)`      | 15 char | Text       | Primary phone number                 | Yes      |      |
| 12| CUST-PHONE-NUM-2         | `X(15)`      | 15 char | Text       | Secondary phone number               | Yes      |      |
| 13| CUST-SSN                 | `9(09)`      | 9 dig   | PII        | Social Security Number               | No       |      |
| 14| CUST-GOVT-ISSUED-ID      | `X(20)`      | 20 char | PII        | Government-issued ID                 | Yes      |      |
| 15| CUST-DOB-YYYY-MM-DD      | `X(10)`      | 10 char | Date       | Date of birth                        | No       |      |
| 16| CUST-EFT-ACCOUNT-ID      | `X(10)`      | 10 char | Text       | EFT/bank account for payments        | Yes      |      |
| 17| CUST-PRI-CARD-HOLDER-IND | `X(01)`      | 1 char  | Flag       | Primary cardholder indicator          | No       |      |
| 18| CUST-FICO-CREDIT-SCORE   | `9(03)`      | 3 dig   | Numeric    | FICO credit score (300-850)          | Yes      |      |
| -- | FILLER                  | `X(168)`     | 168     | Padding    | Reserved space                       | --       | --   |

**Business Rules**:
- CUST-ID is the VSAM KSDS primary key
- Customer is linked to accounts via the Cross-Reference entity
- SSN and GOVT-ISSUED-ID are PII -- require encryption in modernized system
- An alternate layout exists in `CUSTREC.cpy` with identical fields

---

## 4. Card Cross-Reference Entity

**Copybook**: `CVACT03Y.cpy` (11 lines, ~50-byte record)
**VSAM File**: `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`
**Business Description**: Links cards to customers and accounts (many-to-many junction).

| # | Field Name          | PIC Clause   | Length  | Type       | Business Description               | Nullable | Key  |
|---|---------------------|--------------|---------|------------|-------------------------------------|----------|------|
| 1 | XREF-CARD-NUM      | `X(16)`      | 16 char | Text       | Card number                         | No       | PK   |
| 2 | XREF-CUST-ID       | `9(09)`      | 9 dig   | Numeric    | Customer ID                         | No       | FK   |
| 3 | XREF-ACCT-ID       | `9(11)`      | 11 dig  | Numeric    | Account ID                          | No       | FK   |
| -- | FILLER             | `X(14)`      | 14      | Padding    | Reserved space                      | --       | --   |

**Business Rules**:
- XREF-CARD-NUM is the VSAM primary key
- An alternate index exists on XREF-ACCT-ID for account-based lookups
- This is the central join table for the Card -> Customer -> Account relationship

---

## 5. Transaction Entity

**Copybook**: `CVTRA05Y.cpy` (21 lines, ~350-byte record)
**VSAM File**: `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`
**Business Description**: Stores posted credit card transactions.

| # | Field Name           | PIC Clause   | Length  | Type       | Business Description                | Nullable | Key  |
|---|---------------------|--------------|---------|------------|--------------------------------------|----------|------|
| 1 | TRAN-ID             | `X(16)`      | 16 char | Text       | Unique transaction identifier        | No       | PK   |
| 2 | TRAN-TYPE-CD        | `X(02)`      | 2 char  | Code       | Transaction type code                | No       | FK   |
| 3 | TRAN-CAT-CD         | `9(04)`      | 4 dig   | Numeric    | Transaction category code            | No       | FK   |
| 4 | TRAN-SOURCE         | `X(10)`      | 10 char | Text       | Transaction source (POS, ATM, etc.)  | No       |      |
| 5 | TRAN-DESC           | `X(100)`     | 100 char| Text       | Transaction description              | Yes      |      |
| 6 | TRAN-AMT            | `S9(09)V99`  | 11,2    | Currency   | Transaction amount (signed)          | No       |      |
| 7 | TRAN-MERCHANT-ID    | `9(09)`      | 9 dig   | Numeric    | Merchant identifier                  | Yes      |      |
| 8 | TRAN-MERCHANT-NAME  | `X(50)`      | 50 char | Text       | Merchant name                        | Yes      |      |
| 9 | TRAN-MERCHANT-CITY  | `X(50)`      | 50 char | Text       | Merchant city                        | Yes      |      |
| 10| TRAN-MERCHANT-ZIP   | `X(10)`      | 10 char | Text       | Merchant ZIP code                    | Yes      |      |
| 11| TRAN-CARD-NUM       | `X(16)`      | 16 char | Text       | Card used for transaction            | No       | FK   |
| 12| TRAN-ORIG-TS        | `X(26)`      | 26 char | Timestamp  | Transaction origination timestamp    | No       |      |
| 13| TRAN-PROC-TS        | `X(26)`      | 26 char | Timestamp  | Transaction processing timestamp     | No       |      |
| -- | FILLER             | `X(20)`      | 20      | Padding    | Reserved space                       | --       | --   |

**Business Rules**:
- TRAN-ID is the VSAM KSDS primary key (composite: card-num + timestamp typically)
- Negative TRAN-AMT indicates credits/refunds
- TRAN-TYPE-CD links to Transaction Type entity
- TRAN-CAT-CD links to Transaction Category entity

---

## 6. Daily Transaction Entity

**Copybook**: `CVTRA06Y.cpy` (21 lines, ~350-byte record)
**VSAM File**: `AWS.M2.CARDDEMO.DALYTRAN.PS` (sequential)
**Business Description**: Staging file for daily incoming transactions before posting to master.

| # | Field Name              | PIC Clause   | Length  | Type       | Business Description                | Key  |
|---|------------------------|--------------|---------|------------|--------------------------------------|------|
| 1 | DALYTRAN-ID            | `X(16)`      | 16 char | Text       | Daily transaction ID                 | PK   |
| 2 | DALYTRAN-TYPE-CD       | `X(02)`      | 2 char  | Code       | Transaction type code                |      |
| 3 | DALYTRAN-CAT-CD        | `9(04)`      | 4 dig   | Numeric    | Transaction category code            |      |
| 4 | DALYTRAN-SOURCE        | `X(10)`      | 10 char | Text       | Transaction source                   |      |
| 5 | DALYTRAN-DESC          | `X(100)`     | 100 char| Text       | Transaction description              |      |
| 6 | DALYTRAN-AMT           | `S9(09)V99`  | 11,2    | Currency   | Transaction amount (signed)          |      |
| 7 | DALYTRAN-MERCHANT-ID   | `9(09)`      | 9 dig   | Numeric    | Merchant identifier                  |      |
| 8 | DALYTRAN-MERCHANT-NAME | `X(50)`      | 50 char | Text       | Merchant name                        |      |
| 9 | DALYTRAN-MERCHANT-CITY | `X(50)`      | 50 char | Text       | Merchant city                        |      |
| 10| DALYTRAN-MERCHANT-ZIP  | `X(10)`      | 10 char | Text       | Merchant ZIP code                    |      |
| 11| DALYTRAN-CARD-NUM      | `X(16)`      | 16 char | Text       | Card number                          | FK   |
| 12| DALYTRAN-ORIG-TS       | `X(26)`      | 26 char | Timestamp  | Origination timestamp                |      |
| 13| DALYTRAN-PROC-TS       | `X(26)`      | 26 char | Timestamp  | Processing timestamp                 |      |
| -- | FILLER                | `X(20)`      | 20      | Padding    | Reserved space                       | --   |

**Business Rules**:
- Identical structure to Transaction but used as an input staging area
- CBTRN02C reads daily transactions and posts validated ones to the Transaction master
- Rejected records go to DALYREJS GDG

---

## 7. Transaction Category Balance Entity

**Copybook**: `CVTRA01Y.cpy` (13 lines, ~50-byte record)
**VSAM File**: `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS`
**Business Description**: Running balance by account, transaction type, and category.

| # | Field Name          | PIC Clause   | Length  | Type       | Business Description                | Key  |
|---|---------------------|--------------|---------|------------|--------------------------------------|------|
| 1 | TRANCAT-ACCT-ID    | `9(11)`      | 11 dig  | Numeric    | Account ID                           | PK*  |
| 2 | TRANCAT-TYPE-CD    | `X(02)`      | 2 char  | Code       | Transaction type code                | PK*  |
| 3 | TRANCAT-CD         | `9(04)`      | 4 dig   | Numeric    | Transaction category code            | PK*  |
| 4 | TRAN-CAT-BAL       | `S9(09)V99`  | 11,2    | Currency   | Running category balance             |      |
| -- | FILLER            | `X(22)`      | 22      | Padding    | Reserved space                       | --   |

*Composite key: ACCT-ID + TYPE-CD + CAT-CD

---

## 8. Discount Group / Interest Rate Entity

**Copybook**: `CVTRA02Y.cpy` (13 lines, ~50-byte record)
**VSAM File**: `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`
**Business Description**: Interest rates by account group, transaction type, and category.

| # | Field Name          | PIC Clause   | Length  | Type       | Business Description                | Key  |
|---|---------------------|--------------|---------|------------|--------------------------------------|------|
| 1 | DIS-ACCT-GROUP-ID  | `X(10)`      | 10 char | Text       | Account group ID                     | PK*  |
| 2 | DIS-TRAN-TYPE-CD   | `X(02)`      | 2 char  | Code       | Transaction type code                | PK*  |
| 3 | DIS-TRAN-CAT-CD    | `9(04)`      | 4 dig   | Numeric    | Transaction category code            | PK*  |
| 4 | DIS-INT-RATE       | `S9(04)V99`  | 6,2     | Percentage | Interest rate for this group/type    |      |
| -- | FILLER            | `X(28)`      | 28      | Padding    | Reserved space                       | --   |

*Composite key: GROUP-ID + TYPE-CD + CAT-CD

---

## 9. Transaction Type Entity

**Copybook**: `CVTRA03Y.cpy` (10 lines, ~60-byte record)
**VSAM File**: `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`
**Business Description**: Reference table of transaction type codes and descriptions.

| # | Field Name          | PIC Clause   | Length  | Type       | Business Description                | Key  |
|---|---------------------|--------------|---------|------------|--------------------------------------|------|
| 1 | TRAN-TYPE           | `X(02)`      | 2 char  | Code       | Transaction type code                | PK   |
| 2 | TRAN-TYPE-DESC      | `X(50)`      | 50 char | Text       | Type description (e.g., "Purchase")  |      |
| -- | FILLER             | `X(08)`      | 8       | Padding    | Reserved space                       | --   |

---

## 10. Transaction Category Type Entity

**Copybook**: `CVTRA04Y.cpy` (12 lines, ~60-byte record)
**VSAM File**: `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`
**Business Description**: Reference table of transaction category codes and descriptions.

| # | Field Name          | PIC Clause   | Length  | Type       | Business Description                | Key  |
|---|---------------------|--------------|---------|------------|--------------------------------------|------|
| 1 | TRAN-TYPE-CD        | `X(02)`      | 2 char  | Code       | Transaction type code                | PK*  |
| 2 | TRAN-CAT-CD         | `9(04)`      | 4 dig   | Numeric    | Transaction category code            | PK*  |
| 3 | TRAN-CAT-TYPE-DESC  | `X(50)`      | 50 char | Text       | Category description                 |      |
| -- | FILLER             | `X(04)`      | 4       | Padding    | Reserved space                       | --   |

*Composite key: TYPE-CD + CAT-CD

---

## 11. User Security Entity

**Copybook**: `CSUSR01Y.cpy` (26 lines, ~80-byte record)
**VSAM File**: `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`
**Business Description**: Application user credentials and role assignments.

| # | Field Name      | PIC Clause   | Length  | Type       | Business Description                  | Key  |
|---|-----------------|--------------|---------|------------|----------------------------------------|------|
| 1 | SEC-USR-ID     | `X(08)`      | 8 char  | Text       | User login ID                          | PK   |
| 2 | SEC-USR-FNAME  | `X(20)`      | 20 char | Text       | User first name                        |      |
| 3 | SEC-USR-LNAME  | `X(20)`      | 20 char | Text       | User last name                         |      |
| 4 | SEC-USR-PWD    | `X(08)`      | 8 char  | Credential | User password (plaintext)              |      |
| 5 | SEC-USR-TYPE   | `X(01)`      | 1 char  | Code       | User type (`A`=Admin, `U`=Regular)     |      |
| -- | SEC-USR-FILLER| `X(23)`      | 23      | Padding    | Reserved space                         | --   |

**Business Rules**:
- SEC-USR-ID is the VSAM primary key
- Passwords stored in plaintext -- critical security modernization concern
- Two roles: Admin (`A`) has access to user CRUD; Regular (`U`) has standard operations
- Default credentials: ADMIN001/PASSWORD, USER0001/PASSWORD

---

## 12. CICS Commarea (Navigation State)

**Copybook**: `COCOM01Y.cpy` (47 lines)
**Storage**: CICS COMMAREA (passed between programs via XCTL/RETURN)
**Business Description**: Session state passed between online CICS programs for navigation.

| # | Field Name             | PIC Clause   | Length  | Type       | Business Description                |
|---|------------------------|--------------|---------|------------|--------------------------------------|
| 1 | CDEMO-FROM-TRANID     | `X(04)`      | 4 char  | Code       | Source transaction ID                |
| 2 | CDEMO-FROM-PROGRAM    | `X(08)`      | 8 char  | Text       | Source program name                  |
| 3 | CDEMO-TO-TRANID       | `X(04)`      | 4 char  | Code       | Target transaction ID                |
| 4 | CDEMO-TO-PROGRAM      | `X(08)`      | 8 char  | Text       | Target program name                  |
| 5 | CDEMO-USER-ID         | `X(08)`      | 8 char  | Text       | Authenticated user ID                |
| 6 | CDEMO-USER-TYPE       | `X(01)`      | 1 char  | Code       | User role (A/U)                      |
| 7 | CDEMO-PGM-CONTEXT     | `9(01)`      | 1 dig   | Numeric    | Program context flag                 |
| 8 | CDEMO-CUST-ID         | `9(09)`      | 9 dig   | Numeric    | Selected customer ID                 |
| 9 | CDEMO-CUST-FNAME      | `X(25)`      | 25 char | Text       | Customer first name (display)        |
| 10| CDEMO-CUST-MNAME      | `X(25)`      | 25 char | Text       | Customer middle name (display)       |
| 11| CDEMO-CUST-LNAME      | `X(25)`      | 25 char | Text       | Customer last name (display)         |
| 12| CDEMO-ACCT-ID         | `9(11)`      | 11 dig  | Numeric    | Selected account ID                  |
| 13| CDEMO-ACCT-STATUS     | `X(01)`      | 1 char  | Flag       | Account active status                |
| 14| CDEMO-CARD-NUM        | `9(16)`      | 16 dig  | Numeric    | Selected card number                 |
| 15| CDEMO-LAST-MAP        | `X(7)`       | 7 char  | Text       | Last displayed BMS map name          |
| 16| CDEMO-LAST-MAPSET     | `X(7)`       | 7 char  | Text       | Last displayed BMS map set name      |

---

## 13. Export/Import Composite Record

**Copybook**: `CVEXPORT.cpy` (103 lines, ~500-byte record)
**File**: `AWS.M2.CARDDEMO.EXPORT.DATA` (sequential)
**Business Description**: Multi-entity flat file for bulk data exchange. Each record contains a type indicator and entity-specific data overlay.

### Header Fields

| # | Field Name             | PIC Clause   | Length  | Type       | Business Description                |
|---|------------------------|--------------|---------|------------|--------------------------------------|
| 1 | EXPORT-REC-TYPE       | `X(1)`       | 1 char  | Code       | Record type (C/A/T/X/D)             |
| 2 | EXPORT-TIMESTAMP      | `X(26)`      | 26 char | Timestamp  | Export timestamp                     |
| 3 | EXPORT-SEQUENCE-NUM   | `9(9) COMP`  | 4 bytes | Binary     | Sequence number within export        |
| 4 | EXPORT-BRANCH-ID      | `X(4)`       | 4 char  | Text       | Branch identifier                    |
| 5 | EXPORT-REGION-CODE    | `X(5)`       | 5 char  | Text       | Region code                          |
| 6 | EXPORT-RECORD-DATA    | `X(460)`     | 460 char| Overlay    | Entity-specific data (REDEFINES)     |

### Record Types

| Code | Entity       | Overlay Fields                                                        |
|------|-------------|-----------------------------------------------------------------------|
| `C`  | Customer    | EXP-CUST-ID, EXP-CUST-FIRST-NAME, ..., EXP-CUST-FICO-CREDIT-SCORE   |
| `A`  | Account     | EXP-ACCT-ID, EXP-ACCT-ACTIVE-STATUS, ..., EXP-ACCT-GROUP-ID          |
| `T`  | Transaction | EXP-TRAN-ID, EXP-TRAN-TYPE-CD, ..., EXP-TRAN-PROC-TS                 |
| `X`  | Cross-Ref   | EXP-XREF-CARD-NUM, EXP-XREF-CUST-ID, EXP-XREF-ACCT-ID              |
| `D`  | Card        | EXP-CARD-NUM, EXP-CARD-ACCT-ID, ..., EXP-CARD-ACTIVE-STATUS          |

**Note**: Some fields use `COMP` and `COMP-3` storage in the export format (differs from VSAM display format).

---

## 14. Statement Transaction Record

**Copybook**: `COSTM01.CPY` (38 lines)
**File**: `AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS` (sorted transaction extract)
**Business Description**: Sorted transaction record used by statement generation (CBSTM03A/B).

| # | Field Name           | PIC Clause   | Length  | Type       | Business Description                |
|---|---------------------|--------------|---------|------------|--------------------------------------|
| 1 | TRNX-CARD-NUM       | `X(16)`      | 16 char | Text       | Card number (sort key prefix)        |
| 2 | TRNX-ID             | `X(16)`      | 16 char | Text       | Transaction ID                       |
| 3 | TRNX-TYPE-CD        | `X(02)`      | 2 char  | Code       | Transaction type code                |
| 4 | TRNX-CAT-CD         | `9(04)`      | 4 dig   | Numeric    | Transaction category code            |
| 5 | TRNX-SOURCE         | `X(10)`      | 10 char | Text       | Transaction source                   |
| 6 | TRNX-DESC           | `X(100)`     | 100 char| Text       | Transaction description              |
| 7 | TRNX-AMT            | `S9(09)V99`  | 11,2    | Currency   | Transaction amount                   |
| 8 | TRNX-MERCHANT-ID    | `9(09)`      | 9 dig   | Numeric    | Merchant ID                          |
| 9 | TRNX-MERCHANT-NAME  | `X(50)`      | 50 char | Text       | Merchant name                        |
| 10| TRNX-MERCHANT-CITY  | `X(50)`      | 50 char | Text       | Merchant city                        |
| 11| TRNX-MERCHANT-ZIP   | `X(10)`      | 10 char | Text       | Merchant ZIP code                    |
| 12| TRNX-ORIG-TS        | `X(26)`      | 26 char | Timestamp  | Origination timestamp                |
| 13| TRNX-PROC-TS        | `X(26)`      | 26 char | Timestamp  | Processing timestamp                 |
| -- | FILLER             | `X(20)`      | 20      | Padding    | Reserved space                       |

---

## 15. Report Layout

**Copybook**: `CVTRA07Y.cpy` (73 lines)
**Output**: `AWS.M2.CARDDEMO.TRANREPT` (GDG report output)
**Business Description**: Print report layout for transaction reports generated by CBTRN03C.

| # | Field Name                | PIC Clause              | Description                          |
|---|--------------------------|------------------------|--------------------------------------|
| 1 | REPT-SHORT-NAME          | `X(38)`                | Report short title                   |
| 2 | REPT-LONG-NAME           | `X(41)`                | Report full title                    |
| 3 | REPT-DATE-HEADER         | `X(12)`                | Date range header label              |
| 4 | REPT-START-DATE          | `X(10)`                | Report start date                    |
| 5 | REPT-END-DATE            | `X(10)`                | Report end date                      |
| 6 | TRAN-REPORT-TRANS-ID     | `X(16)`                | Transaction ID column                |
| 7 | TRAN-REPORT-ACCOUNT-ID   | `X(11)`                | Account ID column                    |
| 8 | TRAN-REPORT-TYPE-CD      | `X(02)`                | Type code column                     |
| 9 | TRAN-REPORT-TYPE-DESC    | `X(15)`                | Type description column              |
| 10| TRAN-REPORT-CAT-CD      | `9(04)`                | Category code column                 |
| 11| TRAN-REPORT-CAT-DESC    | `X(29)`                | Category description column          |
| 12| TRAN-REPORT-SOURCE       | `X(10)`                | Source column                        |
| 13| TRAN-REPORT-AMT         | `-ZZZ,ZZZ,ZZZ.ZZ`     | Formatted amount column              |
| 14| REPT-PAGE-TOTAL          | `+ZZZ,ZZZ,ZZZ.ZZ`     | Page subtotal                        |
| 15| REPT-ACCOUNT-TOTAL       | `+ZZZ,ZZZ,ZZZ.ZZ`     | Account subtotal                     |
| 16| REPT-GRAND-TOTAL         | `+ZZZ,ZZZ,ZZZ.ZZ`     | Grand total                          |

---

## Entity-Relationship Summary

```
                    +-----------+
                    |  Customer |
                    | CVCUS01Y  |
                    +-----+-----+
                          |
                          | 1:N (via XREF)
                          |
                    +-----+------+         +---------------+
                    | Cross-Ref  |---------| Discount Group|
                    | CVACT03Y   |         | CVTRA02Y      |
                    +-----+------+         +-------+-------+
                          |                        |
              +-----------+-----------+            | (via GROUP-ID)
              |                       |            |
        +-----+-----+          +-----+-----+------+
        |   Card     |          |  Account   |
        | CVACT02Y   |          | CVACT01Y   |
        +-----+------+          +-----+------+
              |                        |
              | (via CARD-NUM)         | (via ACCT-ID)
              |                        |
        +-----+------+         +------+--------+
        | Transaction|         | Tran Cat Bal  |
        | CVTRA05Y   |         | CVTRA01Y      |
        +-----+------+         +---------------+
              |
              | (via TYPE-CD, CAT-CD)
              |
    +---------+---------+
    |                   |
+---+--------+   +-----+------+
| Tran Type  |   | Tran Cat   |
| CVTRA03Y   |   | CVTRA04Y   |
+------------+   +------------+
```

---

## Modernization Notes

### Data Type Mapping Recommendations

| COBOL Pattern          | Java Target              | JPA Annotation                        |
|------------------------|--------------------------|---------------------------------------|
| `PIC 9(n)`             | `Long` / `Integer`       | `@Column(precision=n)`               |
| `PIC X(n)`             | `String`                 | `@Column(length=n)`                  |
| `PIC S9(n)V99`         | `BigDecimal`             | `@Column(precision=n+2, scale=2)`    |
| `PIC S9(n)V99 COMP-3`  | `BigDecimal`            | `@Column(precision=n+2, scale=2)`    |
| `PIC 9(n) COMP`        | `Integer` / `Long`      | `@Column`                            |
| Date `X(10)`           | `LocalDate`              | `@Column(columnDefinition="DATE")`   |
| Timestamp `X(26)`      | `LocalDateTime`          | `@Column(columnDefinition="TIMESTAMP")` |
| Flag `X(01)` (Y/N)     | `Boolean` (JPA converter)| `@Convert(converter=YNBooleanConverter.class)` |

### PII Fields Requiring Encryption

| Entity   | Field                    | Sensitivity |
|----------|--------------------------|-------------|
| Customer | CUST-SSN                 | Critical    |
| Customer | CUST-GOVT-ISSUED-ID      | Critical    |
| Customer | CUST-DOB-YYYY-MM-DD      | High        |
| Card     | CARD-NUM (PAN)           | Critical    |
| Card     | CARD-CVV-CD              | Critical    |
| User     | SEC-USR-PWD              | Critical    |
