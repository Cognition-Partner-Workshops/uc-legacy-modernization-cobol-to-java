# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** Copybook PIC clause analysis from `app/cpy/`
> **Purpose:** Business-friendly reference for all data entities, fields, types, and relationships

---

## Table of Contents

1. [Entity Relationship Overview](#entity-relationship-overview)
2. [Account Entity](#1-account-entity)
3. [Card Entity](#2-card-entity)
4. [Card Cross-Reference Entity](#3-card-cross-reference-entity)
5. [Customer Entity](#4-customer-entity)
6. [Transaction Entity](#5-transaction-entity)
7. [Daily Transaction Entity](#6-daily-transaction-entity)
8. [Transaction Category Balance Entity](#7-transaction-category-balance-entity)
9. [Disclosure Group Entity](#8-disclosure-group-entity)
10. [Transaction Type Entity](#9-transaction-type-entity)
11. [Transaction Category Entity](#10-transaction-category-entity)
12. [User Security Entity](#11-user-security-entity)
13. [Card Detail Entity (Extended)](#12-card-detail-entity-extended)
14. [Statement Transaction Entity](#13-statement-transaction-entity)
15. [Export Record Layouts](#14-export-record-layouts)
16. [Report Structures](#15-report-structures)
17. [System / Shared Structures](#16-system--shared-structures)
18. [PIC Clause Reference](#pic-clause-reference)

---

## Entity Relationship Overview

```
┌──────────────┐     1:N     ┌──────────────┐     1:1     ┌──────────────────┐
│   Customer   │────────────▶│   Account    │◀───────────│  Category Balance │
│  CVCUS01Y    │             │  CVACT01Y    │             │    CVTRA01Y       │
└──────────────┘             └──────┬───────┘             └──────────────────┘
                                    │ 1:N                         ▲
                                    ▼                             │
                             ┌──────────────┐              ┌─────┴────────────┐
                             │    Card      │              │ Disclosure Group  │
                             │  CVACT02Y    │              │    CVTRA02Y       │
                             └──────┬───────┘              └──────────────────┘
                                    │ 1:1
                                    ▼
                             ┌──────────────┐     N:1     ┌──────────────────┐
                             │  Cross-Ref   │            │ Transaction Type  │
                             │  CVACT03Y    │            │    CVTRA03Y       │
                             └──────┬───────┘            └──────────────────┘
                                    │ 1:N                        ▲
                                    ▼                            │
                             ┌──────────────┐            ┌──────┴───────────┐
                             │ Transaction  │───────────▶│ Transaction Cat   │
                             │  CVTRA05Y    │            │    CVTRA04Y       │
                             └──────────────┘            └──────────────────┘

┌──────────────┐
│ User Security│  (Independent — authentication/authorization)
│  CSUSR01Y    │
└──────────────┘
```

---

## 1. Account Entity

**Copybook:** `CVACT01Y.cpy` | **Record Length:** 300 bytes | **VSAM Dataset:** `ACCTDATA.VSAM.KSDS`
**Business Purpose:** Master record for each credit card account, storing balances, limits, and status.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `ACCT-ID` | `9(11)` | Numeric | 11 | **Primary Key** — Unique account identifier |
| `ACCT-ACTIVE-STATUS` | `X(01)` | Alpha | 1 | Account status: `Y` = Active, `N` = Inactive |
| `ACCT-CURR-BAL` | `S9(10)V99` | Signed Decimal | 12.2 | Current account balance (cents precision) |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | Signed Decimal | 12.2 | Maximum credit limit |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | Signed Decimal | 12.2 | Cash advance credit limit |
| `ACCT-OPEN-DATE` | `X(10)` | Alpha | 10 | Date account was opened (YYYY-MM-DD) |
| `ACCT-EXPIRAION-DATE` | `X(10)` | Alpha | 10 | Account expiration date |
| `ACCT-REISSUE-DATE` | `X(10)` | Alpha | 10 | Last card reissue date |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | Signed Decimal | 12.2 | Credits in current billing cycle |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | Signed Decimal | 12.2 | Debits in current billing cycle |
| `ACCT-ADDR-ZIP` | `X(10)` | Alpha | 10 | Account holder ZIP code |
| `ACCT-GROUP-ID` | `X(10)` | Alpha | 10 | Disclosure/interest rate group |
| `FILLER` | `X(178)` | Alpha | 178 | Reserved for future use |

**Key Relationships:**
- One Account → Many Cards (via `CVACT03Y` cross-reference)
- One Account → One Customer (linked externally)
- One Account → Many Transactions (via card number linkage)

---

## 2. Card Entity

**Copybook:** `CVACT02Y.cpy` | **Record Length:** 150 bytes | **VSAM Dataset:** `CARDDATA.VSAM.KSDS`
**Business Purpose:** Credit card master record — one card per physical plastic issued.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `CARD-NUM` | `X(16)` | Alpha | 16 | **Primary Key** — 16-digit card number |
| `CARD-ACCT-ID` | `9(11)` | Numeric | 11 | **Foreign Key** → Account ID |
| `CARD-CVV-CD` | `9(03)` | Numeric | 3 | Card verification value (CVV) |
| `CARD-EMBOSSED-NAME` | `X(50)` | Alpha | 50 | Name embossed on card |
| `CARD-EXPIRAION-DATE` | `X(10)` | Alpha | 10 | Card expiration date |
| `CARD-ACTIVE-STATUS` | `X(01)` | Alpha | 1 | `Y` = Active, `N` = Inactive |
| `FILLER` | `X(59)` | Alpha | 59 | Reserved |

---

## 3. Card Cross-Reference Entity

**Copybook:** `CVACT03Y.cpy` | **Record Length:** 50 bytes | **VSAM Dataset:** `CARDXREF.VSAM.KSDS`
**Business Purpose:** Maps card numbers to account IDs — enables lookup of account from card number.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `XREF-CARD-NUM` | `X(16)` | Alpha | 16 | **Primary Key** — Card number |
| `XREF-CUST-ID` | `9(09)` | Numeric | 9 | **Foreign Key** → Customer ID |
| `XREF-ACCT-ID` | `9(11)` | Numeric | 11 | **Foreign Key** → Account ID |
| `FILLER` | `X(14)` | Alpha | 14 | Reserved |

**Alternate Index:** Account ID (non-unique) — allows lookup of all cards for an account.

---

## 4. Customer Entity

**Copybook:** `CVCUS01Y.cpy` | **Record Length:** 500 bytes | **VSAM Dataset:** `CUSTDATA.VSAM.KSDS`
**Business Purpose:** Customer personal information — name, address, contact details, government ID.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `CUST-ID` | `9(09)` | Numeric | 9 | **Primary Key** — Customer identifier |
| `CUST-FIRST-NAME` | `X(25)` | Alpha | 25 | Customer first name |
| `CUST-MIDDLE-NAME` | `X(25)` | Alpha | 25 | Customer middle name |
| `CUST-LAST-NAME` | `X(25)` | Alpha | 25 | Customer last name |
| `CUST-ADDR-LINE-1` | `X(50)` | Alpha | 50 | Street address line 1 |
| `CUST-ADDR-LINE-2` | `X(50)` | Alpha | 50 | Street address line 2 |
| `CUST-ADDR-LINE-3` | `X(50)` | Alpha | 50 | Street address line 3 |
| `CUST-ADDR-STATE-CD` | `X(02)` | Alpha | 2 | State code (US 2-letter) |
| `CUST-ADDR-COUNTRY-CD` | `X(03)` | Alpha | 3 | Country code (ISO 3-letter) |
| `CUST-ADDR-ZIP` | `X(10)` | Alpha | 10 | ZIP / postal code |
| `CUST-PHONE-NUM-1` | `X(15)` | Alpha | 15 | Primary phone number |
| `CUST-PHONE-NUM-2` | `X(15)` | Alpha | 15 | Secondary phone number |
| `CUST-SSN` | `9(09)` | Numeric | 9 | Social Security Number (PII) |
| `CUST-GOVT-ISSUED-ID` | `X(20)` | Alpha | 20 | Government-issued ID number |
| `CUST-DOB-YYYY-MM-DD` | `X(10)` | Alpha | 10 | Date of birth |
| `CUST-EFT-ACCOUNT-ID` | `X(10)` | Alpha | 10 | EFT/bank account for payments |
| `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | Alpha | 1 | Primary cardholder indicator |
| `CUST-FICO-CREDIT-SCORE` | `9(03)` | Numeric | 3 | FICO credit score |
| `FILLER` | `X(168)` | Alpha | 168 | Reserved |

**Sensitive Data (PII):** SSN, DOB, Government ID, Phone Numbers — requires encryption in modernized system.

---

## 5. Transaction Entity

**Copybook:** `CVTRA05Y.cpy` | **Record Length:** 350 bytes | **VSAM Dataset:** `TRANSACT.VSAM.KSDS`
**Business Purpose:** Master transaction record — stores all posted credit card transactions.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `TRAN-ID` | `X(16)` | Alpha | 16 | **Primary Key** — Unique transaction ID |
| `TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | **Foreign Key** → Transaction type code |
| `TRAN-CAT-CD` | `9(04)` | Numeric | 4 | **Foreign Key** → Transaction category code |
| `TRAN-SOURCE` | `X(10)` | Alpha | 10 | Transaction source (e.g., POS, ATM, Online) |
| `TRAN-DESC` | `X(100)` | Alpha | 100 | Transaction description / memo |
| `TRAN-AMT` | `S9(09)V99` | Signed Decimal | 9.2 | Transaction amount (signed, cents precision) |
| `TRAN-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant identifier |
| `TRAN-MERCHANT-NAME` | `X(50)` | Alpha | 50 | Merchant business name |
| `TRAN-MERCHANT-CITY` | `X(50)` | Alpha | 50 | Merchant city |
| `TRAN-MERCHANT-ZIP` | `X(10)` | Alpha | 10 | Merchant ZIP code |
| `TRAN-CARD-NUM` | `X(16)` | Alpha | 16 | Card number used for transaction |
| `TRAN-ORIG-TS` | `X(26)` | Alpha | 26 | Original transaction timestamp |
| `TRAN-PROC-TS` | `X(26)` | Alpha | 26 | Processing / posting timestamp |
| `FILLER` | `X(20)` | Alpha | 20 | Reserved |

---

## 6. Daily Transaction Entity

**Copybook:** `CVTRA06Y.cpy` | **Record Length:** 350 bytes | **VSAM Dataset:** `DALYTRAN`
**Business Purpose:** Daily transaction staging file — holds unposted transactions for batch processing.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `DALYTRAN-ID` | `X(16)` | Alpha | 16 | **Primary Key** — Daily transaction ID |
| `DALYTRAN-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type code |
| `DALYTRAN-CAT-CD` | `9(04)` | Numeric | 4 | Transaction category code |
| `DALYTRAN-SOURCE` | `X(10)` | Alpha | 10 | Source channel |
| `DALYTRAN-DESC` | `X(100)` | Alpha | 100 | Description |
| `DALYTRAN-AMT` | `S9(09)V99` | Signed Decimal | 9.2 | Amount |
| `DALYTRAN-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant ID |
| `DALYTRAN-MERCHANT-NAME` | `X(50)` | Alpha | 50 | Merchant name |
| `DALYTRAN-MERCHANT-CITY` | `X(50)` | Alpha | 50 | Merchant city |
| `DALYTRAN-MERCHANT-ZIP` | `X(10)` | Alpha | 10 | Merchant ZIP |
| `DALYTRAN-CARD-NUM` | `X(16)` | Alpha | 16 | Card number |
| `DALYTRAN-ORIG-TS` | `X(26)` | Alpha | 26 | Original timestamp |
| `DALYTRAN-PROC-TS` | `X(26)` | Alpha | 26 | Processing timestamp |
| `FILLER` | `X(20)` | Alpha | 20 | Reserved |

**Note:** Identical structure to Transaction (CVTRA05Y). Daily records are posted to the master by CBTRN02C.

---

## 7. Transaction Category Balance Entity

**Copybook:** `CVTRA01Y.cpy` | **Record Length:** 50 bytes | **VSAM Dataset:** `TCATBALF`
**Business Purpose:** Running balance by account + transaction type + category — used for interest calculation.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `TRANCAT-ACCT-ID` | `9(11)` | Numeric | 11 | **Key Part 1** — Account ID |
| `TRANCAT-TYPE-CD` | `X(02)` | Alpha | 2 | **Key Part 2** — Transaction type |
| `TRANCAT-CD` | `9(04)` | Numeric | 4 | **Key Part 3** — Transaction category |
| `TRAN-CAT-BAL` | `S9(09)V99` | Signed Decimal | 9.2 | Running balance for this category |
| `FILLER` | `X(22)` | Alpha | 22 | Reserved |

---

## 8. Disclosure Group Entity

**Copybook:** `CVTRA02Y.cpy` | **Record Length:** 50 bytes | **VSAM Dataset:** `DISCGRP`
**Business Purpose:** Interest rate table — maps account group + transaction type + category to APR.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `DIS-ACCT-GROUP-ID` | `X(10)` | Alpha | 10 | **Key Part 1** — Account group |
| `DIS-TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | **Key Part 2** — Transaction type |
| `DIS-TRAN-CAT-CD` | `9(04)` | Numeric | 4 | **Key Part 3** — Category |
| `DIS-INT-RATE` | `S9(04)V99` | Signed Decimal | 4.2 | Interest rate (APR) |
| `FILLER` | `X(28)` | Alpha | 28 | Reserved |

---

## 9. Transaction Type Entity

**Copybook:** `CVTRA03Y.cpy` | **Record Length:** 60 bytes | **VSAM Dataset:** `TRANTYPE`
**Business Purpose:** Lookup table mapping 2-character type codes to descriptions.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `TRAN-TYPE` | `X(02)` | Alpha | 2 | **Primary Key** — Type code (e.g., `SA`, `CR`) |
| `TRAN-TYPE-DESC` | `X(50)` | Alpha | 50 | Human-readable description |
| `FILLER` | `X(08)` | Alpha | 8 | Reserved |

---

## 10. Transaction Category Entity

**Copybook:** `CVTRA04Y.cpy` | **Record Length:** 60 bytes | **VSAM Dataset:** `TRANCATG`
**Business Purpose:** Lookup table for transaction sub-categories within each type.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | **Key Part 1** — Parent transaction type |
| `TRAN-CAT-CD` | `9(04)` | Numeric | 4 | **Key Part 2** — Category code |
| `TRAN-CAT-TYPE-DESC` | `X(50)` | Alpha | 50 | Category description |
| `FILLER` | `X(04)` | Alpha | 4 | Reserved |

---

## 11. User Security Entity

**Copybook:** `CSUSR01Y.cpy` | **Record Length:** 80 bytes | **VSAM Dataset:** `USRSEC.VSAM.KSDS`
**Business Purpose:** Application user credentials and role assignment.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `SEC-USR-ID` | `X(08)` | Alpha | 8 | **Primary Key** — User ID (e.g., `USER0001`) |
| `SEC-USR-FNAME` | `X(20)` | Alpha | 20 | First name |
| `SEC-USR-LNAME` | `X(20)` | Alpha | 20 | Last name |
| `SEC-USR-PWD` | `X(08)` | Alpha | 8 | Password (plaintext — security risk) |
| `SEC-USR-TYPE` | `X(01)` | Alpha | 1 | User type: `R` = Regular, `A` = Admin |
| `SEC-USR-FILLER` | `X(23)` | Alpha | 23 | Reserved |

**Security Notes:**
- Passwords stored in plaintext — must be hashed in modernized system
- No password complexity rules, no expiration, no lockout mechanism
- Role model is binary (Regular vs Admin) — consider RBAC in modernization

---

## 12. Card Detail Entity (Extended)

**Copybook:** `CVCRD01Y.cpy` | **Record Length:** Variable
**Business Purpose:** Extended card information used for online display and validation.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `CARD-NUM` | `X(16)` | Alpha | 16 | Card number |
| `CARD-ACCT-ID` | `9(11)` | Numeric | 11 | Account ID |
| `CARD-CVV-CD` | `9(03)` | Numeric | 3 | CVV code |
| `CARD-EMBOSSED-NAME` | `X(50)` | Alpha | 50 | Embossed name |
| `CARD-EXPIRAION-DATE` | `X(10)` | Alpha | 10 | Expiration date |
| `CARD-ACTIVE-STATUS` | `X(01)` | Alpha | 1 | Active status |

**Note:** Similar to CVACT02Y but used in online programs with additional display formatting.

---

## 13. Statement Transaction Entity

**Copybook:** `COSTM01.CPY` | **Record Length:** 350 bytes
**Business Purpose:** Re-keyed transaction layout with card number + transaction ID as composite key — used for statement generation.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `TRNX-CARD-NUM` | `X(16)` | Alpha | 16 | **Key Part 1** — Card number |
| `TRNX-ID` | `X(16)` | Alpha | 16 | **Key Part 2** — Transaction ID |
| `TRNX-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type |
| `TRNX-CAT-CD` | `9(04)` | Numeric | 4 | Category code |
| `TRNX-SOURCE` | `X(10)` | Alpha | 10 | Source |
| `TRNX-DESC` | `X(100)` | Alpha | 100 | Description |
| `TRNX-AMT` | `S9(09)V99` | Signed Decimal | 9.2 | Amount |
| `TRNX-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant ID |
| `TRNX-MERCHANT-NAME` | `X(50)` | Alpha | 50 | Merchant name |
| `TRNX-MERCHANT-CITY` | `X(50)` | Alpha | 50 | Merchant city |
| `TRNX-MERCHANT-ZIP` | `X(10)` | Alpha | 10 | Merchant ZIP |
| `TRNX-ORIG-TS` | `X(26)` | Alpha | 26 | Original timestamp |
| `TRNX-PROC-TS` | `X(26)` | Alpha | 26 | Processing timestamp |
| `FILLER` | `X(20)` | Alpha | 20 | Reserved |

---

## 14. Export Record Layouts

**Copybook:** `CVEXPORT.cpy` | **Used by:** CBEXPORT, CBIMPORT
**Business Purpose:** Flat-file representations of all VSAM entities for data migration / backup.

### Exported Entities

| Export Record | Source Entity | Key Differences from VSAM |
|--------------|--------------|--------------------------|
| `EXP-ACCT-*` | Account (CVACT01Y) | Sequential flat file layout, same fields |
| `EXP-CARD-*` | Card (CVACT02Y) | COMP fields converted for sequential I/O |
| `EXP-CUST-*` | Customer (CVCUS01Y) | Identical field structure |
| `EXP-XREF-*` | Cross-Ref (CVACT03Y) | Identical field structure |
| `EXP-TRAN-*` | Transaction (CVTRA05Y) | Identical field structure |

---

## 15. Report Structures

**Copybook:** `CVTRA07Y.cpy`
**Business Purpose:** Report header, detail, and total line layouts for daily transaction report.

| Structure | Fields | Purpose |
|-----------|--------|---------|
| `REPORT-NAME-HEADER` | Short name, Long name, Date range | Report identification |
| `TRANSACTION-DETAIL-REPORT` | Trans ID, Account, Type, Category, Source, Amount | Detail line |
| `TRANSACTION-HEADER-1` | Column headings | Column labels |
| `TRANSACTION-HEADER-2` | Separator line (dashes) | Visual separator |
| `REPORT-PAGE-TOTALS` | Page total amount | Page subtotal |
| `REPORT-ACCOUNT-TOTALS` | Account total amount | Per-account subtotal |
| `REPORT-GRAND-TOTALS` | Grand total amount | Report grand total |

---

## 16. System / Shared Structures

### Common Communication Area — `COCOM01Y.cpy`

| Field | PIC | Description |
|-------|-----|-------------|
| `CCARD-AID` | `X(01)` | Last AID key pressed |
| `CCARD-LAST-PROG` | `X(08)` | Last program executed |
| `CCARD-NEXT-PROG` | `X(08)` | Next program to transfer to |
| `CCARD-LAST-MAP` | `X(07)` | Last BMS map displayed |
| `CCARD-NEXT-MAP` | `X(07)` | Next BMS map to display |
| `CCARD-LAST-MAPSET` | `X(07)` | Last mapset |
| `CCARD-NEXT-MAPSET` | `X(07)` | Next mapset |
| `CCARD-ACCT-ID` | `9(11)` | Current account in context |
| `CCARD-CARD-NUM` | `X(16)` | Current card in context |
| `CCARD-USR-ID` | `X(08)` | Current user ID |
| `CCARD-USR-TYPE` | `X(01)` | Current user type |

### Menu Definitions — `COMEN02Y.cpy`

Defines the main menu structure with up to 13 options, each mapping to a program name and transaction ID.

### Admin Menu Definitions — `COADM02Y.cpy`

Defines admin-specific menu options with up to 10 entries.

### Lookup Codes — `CSLKPCDY.cpy` (1,318 lines)

Comprehensive lookup tables including:
- US State codes and names (50 states + territories)
- Country codes and names
- Used by account/customer update screens for validation

### Date Utility — `CSDAT01Y.cpy`, `CSUTLDPY.cpy`, `CSUTLDWY.cpy`, `CODATECN.cpy`

Working storage and parameter areas for date formatting, validation, and conversion between formats.

### Message Areas — `CSMSG01Y.cpy`, `CSMSG02Y.cpy`

Two-line message display area for user feedback (error messages, confirmations, info messages).

### Screen Title — `COTTL01Y.cpy`

Common title bar fields: application name, program title, date, time.

### Screen Attribute Setter — `CSSETATY.cpy`

BMS field attribute manipulation (protected, unprotected, bright, dark) via COPY REPLACING.

### String Utility — `CSSTRPFY.cpy`

String manipulation: strip leading/trailing spaces, pad fields, format display values.

---

## PIC Clause Reference

For readers unfamiliar with COBOL data types:

| PIC Pattern | Java Equivalent | Description |
|-------------|----------------|-------------|
| `X(n)` | `String` (length n) | Alphanumeric, fixed length |
| `9(n)` | `int` / `long` | Unsigned numeric, n digits |
| `S9(n)` | `int` / `long` | Signed numeric, n digits |
| `S9(n)V99` | `BigDecimal` | Signed decimal, n.2 digits |
| `9(n) COMP` | `int` / `long` | Binary/computational numeric |
| `S9(n)V99 COMP-3` | `BigDecimal` | Packed decimal |

### VSAM File Type Mapping for Modernization

| VSAM Type | COBOL Access | Modern Equivalent |
|-----------|-------------|-------------------|
| KSDS (Key-Sequenced) | READ/WRITE by key | Relational table with primary key |
| AIX (Alternate Index) | READ by alternate key | Secondary index / composite key |
| ESDS (Entry-Sequenced) | Sequential access | Append-only log table |
| RRDS (Relative Record) | Access by slot number | Array / fixed-position table |

### Data Type Migration Recommendations

| COBOL Type | Recommended Java Type | Notes |
|-----------|----------------------|-------|
| `PIC X(n)` where n ≤ 255 | `String` | Trim trailing spaces |
| `PIC 9(n)` where n ≤ 9 | `int` | Direct mapping |
| `PIC 9(n)` where n > 9 | `long` | Direct mapping |
| `PIC S9(n)V99` | `BigDecimal` | Preserve decimal precision |
| `PIC X(10)` dates | `LocalDate` | Parse YYYY-MM-DD format |
| `PIC X(26)` timestamps | `LocalDateTime` | Parse ISO timestamp format |
| `FILLER` | — | Omit in Java classes |
