# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Source**: Copybook PIC clause analysis across `app/cpy/`

## Overview

This data dictionary extracts business entities from COBOL copybook record layouts and translates PIC clauses into a business-friendly format. Each entity maps to a VSAM dataset on the mainframe and will become a relational database table in the modernized Java application.

---

## 1. Account Master &mdash; `CVACT01Y.cpy`

**VSAM Dataset**: `ACCTDAT` (KSDS) | **Record Length**: 300 bytes | **Key**: Account ID (11 digits)

| # | Field Name              | COBOL PIC        | Type        | Length | Business Description                    |
|---|-------------------------|------------------|-------------|--------|------------------------------------------|
| 1 | ACCT-ID                 | 9(11)            | Numeric     | 11     | Unique account identifier                |
| 2 | ACCT-ACTIVE-STATUS      | X(01)            | Alpha       | 1      | Account status (Y=Active, N=Inactive)    |
| 3 | ACCT-CURR-BAL           | S9(10)V99        | Decimal     | 12,2   | Current account balance                  |
| 4 | ACCT-CREDIT-LIMIT       | S9(10)V99        | Decimal     | 12,2   | Credit limit for the account             |
| 5 | ACCT-CASH-CREDIT-LIMIT  | S9(10)V99        | Decimal     | 12,2   | Cash advance credit limit                |
| 6 | ACCT-OPEN-DATE          | X(10)            | Date String | 10     | Account opening date (YYYY-MM-DD)        |
| 7 | ACCT-EXPIRAION-DATE     | X(10)            | Date String | 10     | Account expiration date (YYYY-MM-DD)     |
| 8 | ACCT-REISSUE-DATE       | X(10)            | Date String | 10     | Card reissue date (YYYY-MM-DD)           |
| 9 | ACCT-CURR-CYC-CREDIT    | S9(10)V99        | Decimal     | 12,2   | Credits in current billing cycle         |
| 10| ACCT-CURR-CYC-DEBIT     | S9(10)V99        | Decimal     | 12,2   | Debits in current billing cycle          |
| 11| ACCT-ADDR-ZIP           | X(10)            | Alpha       | 10     | Account holder ZIP code                  |
| 12| ACCT-GROUP-ID           | X(10)            | Alpha       | 10     | Disclosure/rate group identifier         |
| 13| FILLER                  | X(178)           | Filler      | 178    | Reserved for future use                  |

**Business Rules**:
- Primary key: `ACCT-ID`
- Status values: `Y` (active), `N` (inactive)
- All monetary fields are signed with 2 decimal places (stored as packed decimal)
- Dates stored as `YYYY-MM-DD` character strings

---

## 2. Card Master &mdash; `CVACT02Y.cpy`

**VSAM Dataset**: `CARDDAT` (KSDS) | **Record Length**: 150 bytes | **Key**: Card Number (16 digits)

| # | Field Name              | COBOL PIC        | Type        | Length | Business Description                    |
|---|-------------------------|------------------|-------------|--------|------------------------------------------|
| 1 | CARD-NUM                | X(16)            | Alpha       | 16     | Credit card number (primary key)         |
| 2 | CARD-ACCT-ID            | 9(11)            | Numeric     | 11     | Parent account ID (FK → Account)         |
| 3 | CARD-CVV-CD             | 9(03)            | Numeric     | 3      | Card verification value                  |
| 4 | CARD-EMBOSSED-NAME      | X(50)            | Alpha       | 50     | Name embossed on card                    |
| 5 | CARD-EXPIRAION-DATE     | X(10)            | Date String | 10     | Card expiration date (YYYY-MM-DD)        |
| 6 | CARD-ACTIVE-STATUS      | X(01)            | Alpha       | 1      | Card status (Y=Active, N=Inactive)       |
| 7 | FILLER                  | X(59)            | Filler      | 59     | Reserved for future use                  |

**Business Rules**:
- Primary key: `CARD-NUM` (16-character card number)
- Foreign key: `CARD-ACCT-ID` references `ACCT-ID` in Account Master
- Multiple cards can belong to one account
- CVV is a 3-digit numeric code
- Alternate index exists on `CARD-ACCT-ID` (VSAM path: `CARDAIX`)

---

## 3. Customer Master &mdash; `CVCUS01Y.cpy`

**VSAM Dataset**: `CUSTDAT` (KSDS) | **Record Length**: 500 bytes | **Key**: Customer ID (9 digits)

| # | Field Name              | COBOL PIC        | Type        | Length | Business Description                    |
|---|-------------------------|------------------|-------------|--------|------------------------------------------|
| 1 | CUST-ID                 | 9(09)            | Numeric     | 9      | Unique customer identifier               |
| 2 | CUST-FIRST-NAME         | X(25)            | Alpha       | 25     | Customer first name                      |
| 3 | CUST-MIDDLE-NAME        | X(25)            | Alpha       | 25     | Customer middle name                     |
| 4 | CUST-LAST-NAME          | X(25)            | Alpha       | 25     | Customer last name                       |
| 5 | CUST-ADDR-LINE-1        | X(50)            | Alpha       | 50     | Address line 1                           |
| 6 | CUST-ADDR-LINE-2        | X(50)            | Alpha       | 50     | Address line 2                           |
| 7 | CUST-ADDR-LINE-3        | X(50)            | Alpha       | 50     | Address line 3                           |
| 8 | CUST-ADDR-STATE-CD      | X(02)            | Alpha       | 2      | US state code                            |
| 9 | CUST-ADDR-COUNTRY-CD    | X(03)            | Alpha       | 3      | Country code                             |
| 10| CUST-ADDR-ZIP           | X(10)            | Alpha       | 10     | ZIP / postal code                        |
| 11| CUST-PHONE-NUM-1        | X(15)            | Alpha       | 15     | Primary phone number                     |
| 12| CUST-PHONE-NUM-2        | X(15)            | Alpha       | 15     | Secondary phone number                   |
| 13| CUST-SSN                | 9(09)            | Numeric     | 9      | Social Security Number (PII)             |
| 14| CUST-GOVT-ISSUED-ID     | X(20)            | Alpha       | 20     | Government-issued ID number              |
| 15| CUST-DOB-YYYY-MM-DD     | X(10)            | Date String | 10     | Date of birth (YYYY-MM-DD)               |
| 16| CUST-EFT-ACCOUNT-ID     | X(10)            | Alpha       | 10     | EFT/bank account ID                      |
| 17| CUST-PRI-CARD-HOLDER-IND| X(01)            | Alpha       | 1      | Primary cardholder indicator (Y/N)       |
| 18| CUST-FICO-CREDIT-SCORE  | 9(03)            | Numeric     | 3      | FICO credit score                        |
| 19| FILLER                  | X(168)           | Filler      | 168    | Reserved for future use                  |

**Business Rules**:
- Primary key: `CUST-ID`
- SSN is sensitive PII &mdash; must be encrypted/masked in modernized system
- Phone numbers stored as formatted strings `(NNN)NNN-NNNN`
- One customer can have multiple accounts (via cross-reference)

---

## 4. Card-Account Cross Reference &mdash; `CVACT03Y.cpy`

**VSAM Dataset**: `CXACFIL` (KSDS) | **Record Length**: 50 bytes | **Compound Key**: Card Number + Customer ID + Account ID

| # | Field Name              | COBOL PIC        | Type        | Length | Business Description                    |
|---|-------------------------|------------------|-------------|--------|------------------------------------------|
| 1 | XREF-CARD-NUM           | X(16)            | Alpha       | 16     | Card number                              |
| 2 | XREF-CUST-ID            | 9(09)            | Numeric     | 9      | Customer ID (FK → Customer)              |
| 3 | XREF-ACCT-ID            | 9(11)            | Numeric     | 11     | Account ID (FK → Account)                |
| 4 | FILLER                  | X(14)            | Filler      | 14     | Reserved                                 |

**Business Rules**:
- Bridges the many-to-many relationship between cards, customers, and accounts
- Alternate index on Account ID (VSAM path: `CXACAIX`) for account-based lookups
- Critical join table for all card/account operations

---

## 5. Transaction (Online) &mdash; `CVTRA05Y.cpy`

**VSAM Dataset**: `TRANSACT` (KSDS) | **Record Length**: 350 bytes | **Key**: Transaction ID (16 chars)

| # | Field Name              | COBOL PIC        | Type        | Length | Business Description                    |
|---|-------------------------|------------------|-------------|--------|------------------------------------------|
| 1 | TRAN-ID                 | X(16)            | Alpha       | 16     | Unique transaction identifier            |
| 2 | TRAN-TYPE-CD            | X(02)            | Alpha       | 2      | Transaction type code (FK → Tran Type)   |
| 3 | TRAN-CAT-CD             | 9(04)            | Numeric     | 4      | Transaction category code                |
| 4 | TRAN-SOURCE             | X(10)            | Alpha       | 10     | Transaction source (POS, ATM, ONLINE)    |
| 5 | TRAN-DESC               | X(100)           | Alpha       | 100    | Transaction description                  |
| 6 | TRAN-AMT                | S9(09)V99        | Decimal     | 11,2   | Transaction amount (signed)              |
| 7 | TRAN-MERCHANT-ID        | 9(09)            | Numeric     | 9      | Merchant identifier                      |
| 8 | TRAN-MERCHANT-NAME      | X(50)            | Alpha       | 50     | Merchant name                            |
| 9 | TRAN-MERCHANT-CITY      | X(50)            | Alpha       | 50     | Merchant city                            |
| 10| TRAN-MERCHANT-ZIP       | X(10)            | Alpha       | 10     | Merchant ZIP code                        |
| 11| TRAN-CARD-NUM           | X(16)            | Alpha       | 16     | Card number used (FK → Card)             |
| 12| TRAN-ORIG-TS            | X(26)            | Timestamp   | 26     | Original transaction timestamp           |
| 13| TRAN-PROC-TS            | X(26)            | Timestamp   | 26     | Processing timestamp                     |
| 14| FILLER                  | X(20)            | Filler      | 20     | Reserved                                 |

**Business Rules**:
- Primary key: `TRAN-ID` (system-generated 16-char identifier)
- Negative amounts represent credits/returns; positive are debits/charges
- `TRAN-ORIG-TS` and `TRAN-PROC-TS` use 26-char ISO-like timestamp format
- Used by both online CICS screens and batch processing

---

## 6. Daily Transaction &mdash; `CVTRA06Y.cpy`

**VSAM Dataset**: `DALYTRAN` (sequential/KSDS) | **Record Length**: 350 bytes

| # | Field Name              | COBOL PIC        | Type        | Length | Business Description                    |
|---|-------------------------|------------------|-------------|--------|------------------------------------------|
| 1 | DALYTRAN-ID             | X(16)            | Alpha       | 16     | Daily transaction identifier             |
| 2 | DALYTRAN-TYPE-CD        | X(02)            | Alpha       | 2      | Transaction type code                    |
| 3 | DALYTRAN-CAT-CD         | 9(04)            | Numeric     | 4      | Transaction category code                |
| 4 | DALYTRAN-SOURCE         | X(10)            | Alpha       | 10     | Transaction source                       |
| 5 | DALYTRAN-DESC           | X(100)           | Alpha       | 100    | Description                              |
| 6 | DALYTRAN-AMT            | S9(09)V99        | Decimal     | 11,2   | Transaction amount                       |
| 7 | DALYTRAN-MERCHANT-ID    | 9(09)            | Numeric     | 9      | Merchant ID                              |
| 8 | DALYTRAN-MERCHANT-NAME  | X(50)            | Alpha       | 50     | Merchant name                            |
| 9 | DALYTRAN-MERCHANT-CITY  | X(50)            | Alpha       | 50     | Merchant city                            |
| 10| DALYTRAN-MERCHANT-ZIP   | X(10)            | Alpha       | 10     | Merchant ZIP                             |
| 11| DALYTRAN-CARD-NUM       | X(16)            | Alpha       | 16     | Card number used                         |
| 12| DALYTRAN-ORIG-TS        | X(26)            | Timestamp   | 26     | Original timestamp                       |
| 13| DALYTRAN-PROC-TS        | X(26)            | Timestamp   | 26     | Processing timestamp                     |
| 14| FILLER                  | X(20)            | Filler      | 20     | Reserved                                 |

**Business Rules**:
- Identical layout to Transaction (CVTRA05Y) but represents the daily batch input
- Fed into the batch posting cycle (POSTTRAN → CBTRN02C)
- After posting, records are merged into the main TRANSACT file (COMBTRAN)

---

## 7. Transaction Category Balance &mdash; `CVTRA01Y.cpy`

**VSAM Dataset**: `TCATBALF` (KSDS) | **Record Length**: 50 bytes | **Key**: Account ID + Type Code + Category Code

| # | Field Name              | COBOL PIC        | Type        | Length | Business Description                    |
|---|-------------------------|------------------|-------------|--------|------------------------------------------|
| 1 | TRANCAT-ACCT-ID         | 9(11)            | Numeric     | 11     | Account identifier                       |
| 2 | TRANCAT-TYPE-CD         | X(02)            | Alpha       | 2      | Transaction type code                    |
| 3 | TRANCAT-CD              | 9(04)            | Numeric     | 4      | Transaction category code                |
| 4 | TRAN-CAT-BAL            | S9(09)V99        | Decimal     | 11,2   | Running balance for this category        |
| 5 | FILLER                  | X(22)            | Filler      | 22     | Reserved                                 |

**Business Rules**:
- Compound key: Account + Type + Category
- Maintains running balance per transaction category per account
- Updated by the interest calculation batch (CBACT04C)

---

## 8. Disclosure Group &mdash; `CVTRA02Y.cpy`

**VSAM Dataset**: `DISCGRP` (KSDS) | **Record Length**: 50 bytes | **Key**: Group ID + Type Code + Category Code

| # | Field Name              | COBOL PIC        | Type        | Length | Business Description                    |
|---|-------------------------|------------------|-------------|--------|------------------------------------------|
| 1 | DIS-ACCT-GROUP-ID       | X(10)            | Alpha       | 10     | Account group identifier                 |
| 2 | DIS-TRAN-TYPE-CD        | X(02)            | Alpha       | 2      | Transaction type code                    |
| 3 | DIS-TRAN-CAT-CD         | 9(04)            | Numeric     | 4      | Transaction category code                |
| 4 | DIS-INT-RATE            | S9(04)V99        | Decimal     | 6,2    | Interest rate for this group/type/cat    |
| 5 | FILLER                  | X(28)            | Filler      | 28     | Reserved                                 |

**Business Rules**:
- Links account groups to interest rates by transaction type and category
- Used during interest calculation (CBACT04C) to determine applicable rates
- `DIS-ACCT-GROUP-ID` references `ACCT-GROUP-ID` in the Account Master

---

## 9. Transaction Type &mdash; `CVTRA03Y.cpy`

**VSAM Dataset**: `TRANTYPE` (KSDS) | **Record Length**: 60 bytes | **Key**: Transaction Type Code

| # | Field Name              | COBOL PIC        | Type        | Length | Business Description                    |
|---|-------------------------|------------------|-------------|--------|------------------------------------------|
| 1 | TRAN-TYPE               | X(02)            | Alpha       | 2      | Transaction type code (primary key)      |
| 2 | TRAN-TYPE-DESC          | X(50)            | Alpha       | 50     | Transaction type description             |
| 3 | FILLER                  | X(08)            | Filler      | 8      | Reserved                                 |

**Business Rules**:
- Lookup/reference table for transaction type codes
- Examples: `01`=Purchase, `02`=Return, `03`=Cash Advance, etc.

---

## 10. Transaction Category &mdash; `CVTRA04Y.cpy`

**VSAM Dataset**: `TRANCATG` (KSDS) | **Record Length**: 60 bytes | **Key**: Type Code + Category Code

| # | Field Name              | COBOL PIC        | Type        | Length | Business Description                    |
|---|-------------------------|------------------|-------------|--------|------------------------------------------|
| 1 | TRAN-TYPE-CD            | X(02)            | Alpha       | 2      | Transaction type code                    |
| 2 | TRAN-CAT-CD             | 9(04)            | Numeric     | 4      | Category code within type                |
| 3 | TRAN-CAT-TYPE-DESC      | X(50)            | Alpha       | 50     | Category description                     |
| 4 | FILLER                  | X(04)            | Filler      | 4      | Reserved                                 |

**Business Rules**:
- Sub-classification within transaction types
- Compound key: Type Code + Category Code
- Used in reporting and interest calculation

---

## 11. User Security &mdash; `CSUSR01Y.cpy`

**VSAM Dataset**: `USRSEC` (KSDS) | **Record Length**: 80 bytes | **Key**: User ID (8 chars)

| # | Field Name              | COBOL PIC        | Type        | Length | Business Description                    |
|---|-------------------------|------------------|-------------|--------|------------------------------------------|
| 1 | SEC-USR-ID              | X(08)            | Alpha       | 8      | User login ID (primary key)              |
| 2 | SEC-USR-FNAME           | X(20)            | Alpha       | 20     | User first name                          |
| 3 | SEC-USR-LNAME           | X(20)            | Alpha       | 20     | User last name                           |
| 4 | SEC-USR-PWD             | X(08)            | Alpha       | 8      | User password (plaintext)                |
| 5 | SEC-USR-TYPE            | X(01)            | Alpha       | 1      | User type (A=Admin, U=Regular User)      |
| 6 | FILLER                  | X(23)            | Filler      | 23     | Reserved                                 |

**Business Rules**:
- Passwords stored in plaintext (legacy pattern &mdash; must be hashed in modernized system)
- Type `A` grants access to admin menu and user management functions
- Type `U` restricts to regular card/account operations
- Default users: `ADMIN001`/`PASSWORD`, `USER0001`/`PASSWORD`

---

## 12. Application Common Area &mdash; `COCOM01Y.cpy`

**Usage**: CICS COMMAREA (passed between programs) | **Not persisted to VSAM**

| # | Field Name              | COBOL PIC        | Type    | Length | Business Description                    |
|---|-------------------------|------------------|---------|--------|------------------------------------------|
| 1 | CDEMO-FROM-TRANID       | X(04)            | Alpha   | 4      | Originating transaction ID               |
| 2 | CDEMO-FROM-PROGRAM      | X(08)            | Alpha   | 8      | Originating program name                 |
| 3 | CDEMO-TO-TRANID         | X(04)            | Alpha   | 4      | Target transaction ID                    |
| 4 | CDEMO-TO-PROGRAM        | X(08)            | Alpha   | 8      | Target program name                      |
| 5 | CDEMO-USER-ID           | X(08)            | Alpha   | 8      | Logged-in user ID                        |
| 6 | CDEMO-USER-TYPE         | X(01)            | Alpha   | 1      | User type (A=Admin, U=User)              |
| 7 | CDEMO-PGM-CONTEXT       | 9(01)            | Numeric | 1      | Program context (0=Enter, 1=Reenter)     |
| 8 | CDEMO-CUST-ID           | 9(09)            | Numeric | 9      | Selected customer ID                     |
| 9 | CDEMO-CUST-FNAME        | X(25)            | Alpha   | 25     | Customer first name                      |
| 10| CDEMO-CUST-MNAME        | X(25)            | Alpha   | 25     | Customer middle name                     |
| 11| CDEMO-CUST-LNAME        | X(25)            | Alpha   | 25     | Customer last name                       |
| 12| CDEMO-ACCT-ID           | 9(11)            | Numeric | 11     | Selected account ID                      |
| 13| CDEMO-ACCT-STATUS       | X(01)            | Alpha   | 1      | Selected account status                  |
| 14| CDEMO-CARD-NUM          | 9(16)            | Numeric | 16     | Selected card number                     |
| 15| CDEMO-LAST-MAP          | X(07)            | Alpha   | 7      | Last BMS map displayed                   |
| 16| CDEMO-LAST-MAPSET       | X(07)            | Alpha   | 7      | Last BMS mapset used                     |

**Business Rules**:
- Shared communication area passed via `EXEC CICS XCTL COMMAREA`
- Carries navigation state, user identity, and selected entity keys
- `CDEMO-PGM-CONTEXT` controls first-time vs. re-enter logic in every screen
- Fields `CCARD-AID-*` and `CCARD-ERROR-MSG` are sometimes used alongside this area but belong to `CVCRD01Y.cpy` (Credit Card Work Area), not this copybook

---

## 13. Transaction Record (Statement Layout) &mdash; `COSTM01.CPY`

**Usage**: Statement generation (CBSTM03A/B) | **Record Length**: 350 bytes | **Key**: Card Number + Transaction ID

| # | Field Name              | COBOL PIC        | Type        | Length | Business Description                    |
|---|-------------------------|------------------|-------------|--------|------------------------------------------|
| 1 | TRNX-CARD-NUM           | X(16)            | Alpha       | 16     | Card number (part of compound key)       |
| 2 | TRNX-ID                 | X(16)            | Alpha       | 16     | Transaction ID (part of compound key)    |
| 3 | TRNX-TYPE-CD            | X(02)            | Alpha       | 2      | Transaction type code                    |
| 4 | TRNX-CAT-CD             | 9(04)            | Numeric     | 4      | Transaction category code                |
| 5 | TRNX-SOURCE             | X(10)            | Alpha       | 10     | Transaction source                       |
| 6 | TRNX-DESC               | X(100)           | Alpha       | 100    | Description                              |
| 7 | TRNX-AMT                | S9(09)V99        | Decimal     | 11,2   | Amount                                   |
| 8 | TRNX-MERCHANT-ID        | 9(09)            | Numeric     | 9      | Merchant ID                              |
| 9 | TRNX-MERCHANT-NAME      | X(50)            | Alpha       | 50     | Merchant name                            |
| 10| TRNX-MERCHANT-CITY      | X(50)            | Alpha       | 50     | Merchant city                            |
| 11| TRNX-MERCHANT-ZIP       | X(10)            | Alpha       | 10     | Merchant ZIP                             |
| 12| TRNX-ORIG-TS            | X(26)            | Timestamp   | 26     | Original timestamp                       |
| 13| TRNX-PROC-TS            | X(26)            | Timestamp   | 26     | Processing timestamp                     |
| 14| FILLER                  | X(20)            | Filler      | 20     | Reserved                                 |

**Business Rules**:
- Re-keyed version of CVTRA05Y with compound key (Card + TranID) for statement sorting
- Used exclusively by the statement generation batch job (CREASTMT)

---

## 14. Export Record Layout &mdash; `CVEXPORT.cpy`

**Usage**: Data export/import programs (CBEXPORT, CBIMPORT)

Defines export layouts for all major entities with a record-type prefix:

| Record Type | Prefix       | Entity Exported    | Key Fields                       |
|-------------|--------------|--------------------|----------------------------------|
| Customer    | EXP-CUST-*   | Customer Master    | CUST-ID, Name, Address, SSN, DOB |
| Account     | EXP-ACCT-*   | Account Master     | ACCT-ID, Status, Balances, Dates |
| Card        | EXP-CARD-*   | Card Master        | CARD-NUM, ACCT-ID, CVV, Dates   |

---

## Entity Relationship Summary

```
┌─────────────────┐       ┌──────────────────┐       ┌─────────────────┐
│   CUSTOMER      │       │  CROSS-REFERENCE │       │    ACCOUNT      │
│   (CVCUS01Y)    │◄──────│   (CVACT03Y)     │──────►│   (CVACT01Y)    │
│                 │  1:N  │                  │  N:1  │                 │
│ CUST-ID (PK)    │       │ XREF-CARD-NUM    │       │ ACCT-ID (PK)    │
│ Name, Address   │       │ XREF-CUST-ID(FK) │       │ Balance, Limits │
│ SSN, DOB        │       │ XREF-ACCT-ID(FK) │       │ Dates, Status   │
└─────────────────┘       └────────┬─────────┘       └────────┬────────┘
                                   │                          │
                                   │ 1:N                      │ 1:N (via group)
                                   ▼                          ▼
                          ┌──────────────────┐       ┌──────────────────┐
                          │      CARD        │       │ DISCLOSURE GROUP │
                          │   (CVACT02Y)     │       │   (CVTRA02Y)     │
                          │                  │       │                  │
                          │ CARD-NUM (PK)    │       │ Group + Type     │
                          │ CARD-ACCT-ID(FK) │       │ Interest Rate    │
                          │ CVV, Status      │       └──────────────────┘
                          └────────┬─────────┘
                                   │ 1:N
                                   ▼
                          ┌──────────────────┐       ┌──────────────────┐
                          │   TRANSACTION    │──────►│ TRAN CATEGORY    │
                          │   (CVTRA05Y)     │  N:1  │ BALANCE          │
                          │                  │       │   (CVTRA01Y)     │
                          │ TRAN-ID (PK)     │       └──────────────────┘
                          │ TRAN-CARD-NUM(FK)│
                          │ Amount, Merchant │       ┌──────────────────┐
                          │ Timestamps       │──────►│  TRAN TYPE       │
                          └──────────────────┘  N:1  │   (CVTRA03Y)     │
                                                     └──────────────────┘
                                                            │ 1:N
                                                            ▼
                                                     ┌──────────────────┐
                                                     │  TRAN CATEGORY   │
                                                     │   (CVTRA04Y)     │
                                                     └──────────────────┘

                          ┌──────────────────┐
                          │  USER SECURITY   │
                          │   (CSUSR01Y)     │
                          │                  │
                          │ USR-ID (PK)      │
                          │ Name, Password   │
                          │ Type (A/U)       │
                          └──────────────────┘
```

---

## COBOL PIC Clause Quick Reference

| PIC Clause      | Java Equivalent       | Description                          |
|-----------------|-----------------------|--------------------------------------|
| `X(n)`          | `String` (length n)   | Alphanumeric, fixed length           |
| `9(n)`          | `long` / `int`        | Unsigned numeric, n digits           |
| `S9(n)`         | `long` / `int`        | Signed numeric, n digits             |
| `S9(n)V99`      | `BigDecimal`          | Signed decimal with 2 implied places |
| `S9(n) COMP`    | `int` / `long`        | Binary (computational)               |
| `S9(n) COMP-3`  | `BigDecimal`          | Packed decimal                       |
