# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Source:** COBOL Copybooks in `app/cpy/` and `app/cpy-bms/`

---

## Table of Contents

- [1. Overview](#1-overview)
- [2. Account Entity (CVACT01Y)](#2-account-entity-cvact01y)
- [3. Card Entity (CVACT02Y)](#3-card-entity-cvact02y)
- [4. Card Cross-Reference Entity (CVACT03Y)](#4-card-cross-reference-entity-cvact03y)
- [5. Card Extended Entity (CVCRD01Y)](#5-card-extended-entity-cvcrd01y)
- [6. Customer Entity (CVCUS01Y)](#6-customer-entity-cvcus01y)
- [7. Customer Record — Alternate Layout (CUSTREC)](#7-customer-record--alternate-layout-custrec)
- [8. User Security Entity (CSUSR01Y)](#8-user-security-entity-csusr01y)
- [9. Transaction Entity (CVTRA05Y)](#9-transaction-entity-cvtra05y)
- [10. Daily Transaction Entity (CVTRA06Y)](#10-daily-transaction-entity-cvtra06y)
- [11. Transaction Category Balance Entity (CVTRA01Y)](#11-transaction-category-balance-entity-cvtra01y)
- [12. Disclosure Group Entity (CVTRA02Y)](#12-disclosure-group-entity-cvtra02y)
- [13. Transaction Type Entity (CVTRA03Y)](#13-transaction-type-entity-cvtra03y)
- [14. Transaction Category Entity (CVTRA04Y)](#14-transaction-category-entity-cvtra04y)
- [15. Statement Transaction Entity (COSTM01)](#15-statement-transaction-entity-costm01)
- [16. Transaction Report Structures (CVTRA07Y)](#16-transaction-report-structures-cvtra07y)
- [17. Export Record Entity (CVEXPORT)](#17-export-record-entity-cvexport)
- [18. Common Communication Area (COCOM01Y)](#18-common-communication-area-cocom01y)
- [19. Menu Definitions (COMEN02Y)](#19-menu-definitions-comen02y)
- [20. Admin Menu Definitions (COADM02Y)](#20-admin-menu-definitions-coadm02y)
- [21. Lookup Code Tables (CSLKPCDY)](#21-lookup-code-tables-cslkpcdy)
- [22. Date Utility Fields (CSDAT01Y, CODATECN)](#22-date-utility-fields-csdat01y-codatecn)
- [23. Unused Entity (UNUSED1Y)](#23-unused-entity-unused1y)
- [24. VSAM File Summary](#24-vsam-file-summary)
- [25. PIC Clause Reference](#25-pic-clause-reference)

---

## 1. Overview

This dictionary documents every business data entity extracted from the CardDemo COBOL copybooks. Each entity maps to a VSAM file (or working-storage structure) and is presented in a business-friendly format with field-level detail.

### Entity Relationship Summary

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Customer   │────▶│   Account    │────▶│     Card     │
│  (CVCUS01Y)  │ 1:N │  (CVACT01Y)  │ 1:N │  (CVACT02Y)  │
└──────────────┘     └──────┬───────┘     └──────┬───────┘
                           │                     │
                           │              ┌──────▼───────┐
                           │              │  Card X-Ref  │
                           │              │  (CVACT03Y)  │
                           │              └──────────────┘
                           │
                    ┌──────▼───────┐     ┌──────────────┐
                    │ Transaction  │────▶│  Tran Type   │
                    │  (CVTRA05Y)  │     │  (CVTRA03Y)  │
                    └──────┬───────┘     └──────────────┘
                           │
                    ┌──────▼───────┐     ┌──────────────┐
                    │ Tran Cat Bal │     │ Disclosure   │
                    │  (CVTRA01Y)  │     │  (CVTRA02Y)  │
                    └──────────────┘     └──────────────┘

┌──────────────┐
│ User Security│
│  (CSUSR01Y)  │
└──────────────┘
```

---

## 2. Account Entity (CVACT01Y)

**Copybook:** `app/cpy/CVACT01Y.cpy` | **Record Length:** 300 bytes | **VSAM File:** `ACCTDATA.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Description |
|---|-------------|-----------|------|------|---------------|-------------|
| 1 | `ACCT-ID` | `9(11)` | Numeric | 11 | Account ID | Unique account identifier (primary key) |
| 2 | `ACCT-ACTIVE-STATUS` | `X(01)` | Alpha | 1 | Account Status | Active status flag (Y/N) |
| 3 | `ACCT-CURR-BAL` | `S9(10)V99` | Signed Decimal | 12 | Current Balance | Current account balance (dollars.cents) |
| 4 | `ACCT-CREDIT-LIMIT` | `S9(10)V99` | Signed Decimal | 12 | Credit Limit | Maximum credit limit |
| 5 | `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | Signed Decimal | 12 | Cash Credit Limit | Maximum cash advance limit |
| 6 | `ACCT-OPEN-DATE` | `X(10)` | Alpha | 10 | Open Date | Account opening date |
| 7 | `ACCT-EXPIRAION-DATE` | `X(10)` | Alpha | 10 | Expiration Date | Account expiration date |
| 8 | `ACCT-REISSUE-DATE` | `X(10)` | Alpha | 10 | Reissue Date | Last card reissue date |
| 9 | `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | Signed Decimal | 12 | Current Cycle Credit | Credits in current billing cycle |
| 10 | `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | Signed Decimal | 12 | Current Cycle Debit | Debits in current billing cycle |
| 11 | `ACCT-ADDR-ZIP` | `X(10)` | Alpha | 10 | ZIP Code | Account holder ZIP code |
| 12 | `ACCT-GROUP-ID` | `X(10)` | Alpha | 10 | Group ID | Account group identifier (for disclosure) |
| 13 | `FILLER` | `X(178)` | — | 178 | (Reserved) | Unused space for future expansion |

**Key:** `ACCT-ID` (11-byte numeric)

---

## 3. Card Entity (CVACT02Y)

**Copybook:** `app/cpy/CVACT02Y.cpy` | **Record Length:** 150 bytes | **VSAM File:** `CARDDATA.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Description |
|---|-------------|-----------|------|------|---------------|-------------|
| 1 | `CARD-NUM` | `X(16)` | Alpha | 16 | Card Number | 16-digit credit card number (primary key) |
| 2 | `CARD-ACCT-ID` | `9(11)` | Numeric | 11 | Account ID | Parent account identifier (FK → Account) |
| 3 | `CARD-CVV-CD` | `9(03)` | Numeric | 3 | CVV Code | Card verification value |
| 4 | `CARD-EMBOSSED-NAME` | `X(50)` | Alpha | 50 | Embossed Name | Name printed on card |
| 5 | `CARD-EXPIRAION-DATE` | `X(10)` | Alpha | 10 | Expiration Date | Card expiry date |
| 6 | `CARD-ACTIVE-STATUS` | `X(01)` | Alpha | 1 | Active Status | Card active flag (Y/N) |
| 7 | `FILLER` | `X(59)` | — | 59 | (Reserved) | Unused space |

**Key:** `CARD-NUM` (16-byte alphanumeric)
**Alternate Index:** On `CARD-ACCT-ID` (non-unique) — allows lookup of all cards for an account

---

## 4. Card Cross-Reference Entity (CVACT03Y)

**Copybook:** `app/cpy/CVACT03Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `CARDXREF.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Description |
|---|-------------|-----------|------|------|---------------|-------------|
| 1 | `XREF-CARD-NUM` | `X(16)` | Alpha | 16 | Card Number | Card number (primary key) |
| 2 | `XREF-CUST-ID` | `9(09)` | Numeric | 9 | Customer ID | Customer identifier (FK → Customer) |
| 3 | `XREF-ACCT-ID` | `9(11)` | Numeric | 11 | Account ID | Account identifier (FK → Account) |
| 4 | `FILLER` | `X(14)` | — | 14 | (Reserved) | Unused space |

**Key:** `XREF-CARD-NUM` (16-byte alphanumeric)
**Alternate Index:** On `XREF-ACCT-ID` (non-unique)
**Purpose:** Links cards to both customers and accounts — the central join table of the system.

---

## 5. Card Extended Entity (CVCRD01Y)

**Copybook:** `app/cpy/CVCRD01Y.cpy` | **Working-storage structure used by online card programs**

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Description |
|---|-------------|-----------|------|------|---------------|-------------|
| 1 | `CDEMO-CCARD-ID` | `9(04)` | Numeric | 4 | Card Record ID | Internal sequence number |
| 2 | `CDEMO-CCARD-NUM` | `X(16)` | Alpha | 16 | Card Number | 16-digit card number |
| 3 | `CDEMO-CCARD-ACCT-ID` | `9(11)` | Numeric | 11 | Account ID | Parent account ID |
| 4 | `CDEMO-CCARD-CVV-CD` | `9(03)` | Numeric | 3 | CVV Code | Card verification value |
| 5 | `CDEMO-CCARD-EMBOSSED-NAME` | `X(50)` | Alpha | 50 | Embossed Name | Name on card |
| 6 | `CDEMO-CCARD-EXPIRAION-DATE` | `X(10)` | Alpha | 10 | Expiration Date | Card expiry date |
| 7 | `CDEMO-CCARD-ACTIVE-STATUS` | `X(01)` | Alpha | 1 | Active Status | Card active flag |

**Note:** This is an in-memory working structure used by COCRDLIC, COCRDSLC, and COCRDUPC for screen display. Not persisted directly.

---

## 6. Customer Entity (CVCUS01Y)

**Copybook:** `app/cpy/CVCUS01Y.cpy` | **Record Length:** 500 bytes | **VSAM File:** `CUSTDATA.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Description |
|---|-------------|-----------|------|------|---------------|-------------|
| 1 | `CUST-ID` | `9(09)` | Numeric | 9 | Customer ID | Unique customer identifier (primary key) |
| 2 | `CUST-FIRST-NAME` | `X(25)` | Alpha | 25 | First Name | Customer first name |
| 3 | `CUST-MIDDLE-NAME` | `X(25)` | Alpha | 25 | Middle Name | Customer middle name |
| 4 | `CUST-LAST-NAME` | `X(25)` | Alpha | 25 | Last Name | Customer last name |
| 5 | `CUST-ADDR-LINE-1` | `X(50)` | Alpha | 50 | Address Line 1 | Street address |
| 6 | `CUST-ADDR-LINE-2` | `X(50)` | Alpha | 50 | Address Line 2 | Apt/Suite |
| 7 | `CUST-ADDR-LINE-3` | `X(50)` | Alpha | 50 | Address Line 3 | Additional address |
| 8 | `CUST-ADDR-STATE-CD` | `X(02)` | Alpha | 2 | State Code | US state code |
| 9 | `CUST-ADDR-COUNTRY-CD` | `X(03)` | Alpha | 3 | Country Code | ISO country code |
| 10 | `CUST-ADDR-ZIP` | `X(10)` | Alpha | 10 | ZIP Code | Postal code |
| 11 | `CUST-PHONE-NUM-1` | `X(15)` | Alpha | 15 | Phone 1 | Primary phone |
| 12 | `CUST-PHONE-NUM-2` | `X(15)` | Alpha | 15 | Phone 2 | Secondary phone |
| 13 | `CUST-SSN` | `9(09)` | Numeric | 9 | SSN | Social Security Number |
| 14 | `CUST-GOVT-ISSUED-ID` | `X(20)` | Alpha | 20 | Government ID | Government-issued ID number |
| 15 | `CUST-DOB-YYYYMMDD` | `X(10)` | Alpha | 10 | Date of Birth | YYYY-MM-DD format |
| 16 | `CUST-EFT-ACCOUNT-ID` | `X(10)` | Alpha | 10 | EFT Account ID | Electronic funds transfer account |
| 17 | `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | Alpha | 1 | Primary Cardholder | Y=primary, N=authorized user |
| 18 | `CUST-FICO-CREDIT-SCORE` | `9(03)` | Numeric | 3 | FICO Score | Credit score (300-850) |
| 19 | `FILLER` | `X(168)` | — | 168 | (Reserved) | Unused space |

**Key:** `CUST-ID` (9-byte numeric)

---

## 7. Customer Record — Alternate Layout (CUSTREC)

**Copybook:** `app/cpy/CUSTREC.cpy` | **Used by:** CBSTM03A (statement generation)

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Description |
|---|-------------|-----------|------|------|---------------|-------------|
| 1 | `CUST-ID` | `9(09)` | Numeric | 9 | Customer ID | Same as CVCUS01Y |
| 2 | `CUST-FIRST-NAME` | `X(25)` | Alpha | 25 | First Name | |
| 3 | `CUST-MIDDLE-NAME` | `X(25)` | Alpha | 25 | Middle Name | |
| 4 | `CUST-LAST-NAME` | `X(25)` | Alpha | 25 | Last Name | |
| 5 | `CUST-ADDR-LINE-1` | `X(50)` | Alpha | 50 | Address Line 1 | |
| 6 | `CUST-ADDR-LINE-2` | `X(50)` | Alpha | 50 | Address Line 2 | |
| 7 | `CUST-ADDR-LINE-3` | `X(50)` | Alpha | 50 | Address Line 3 | |
| 8 | `CUST-ADDR-STATE-CD` | `X(02)` | Alpha | 2 | State Code | |
| 9 | `CUST-ADDR-COUNTRY-CD` | `X(03)` | Alpha | 3 | Country Code | |
| 10 | `CUST-ADDR-ZIP` | `X(10)` | Alpha | 10 | ZIP Code | |
| 11 | `CUST-PHONE-NUM-1` | `X(15)` | Alpha | 15 | Phone 1 | |
| 12 | `CUST-PHONE-NUM-2` | `X(15)` | Alpha | 15 | Phone 2 | |
| 13 | `CUST-SSN` | `9(09)` | Numeric | 9 | SSN | |
| 14 | `CUST-GOVT-ISSUED-ID` | `X(20)` | Alpha | 20 | Government ID | |
| 15 | `CUST-DOB-YYYYMMDD` | `X(10)` | Alpha | 10 | Date of Birth | |
| 16 | `CUST-EFT-ACCOUNT-ID` | `X(10)` | Alpha | 10 | EFT Account ID | |
| 17 | `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | Alpha | 1 | Primary Cardholder | |
| 18 | `CUST-FICO-CREDIT-SCORE` | `9(03)` | Numeric | 3 | FICO Score | |
| 19 | `FILLER` | `X(168)` | — | 168 | (Reserved) | |

**Note:** Identical layout to CVCUS01Y but defined under record name `CUSTOMER-RECORD` for use in batch statement generation.

---

## 8. User Security Entity (CSUSR01Y)

**Copybook:** `app/cpy/CSUSR01Y.cpy` | **Record Length:** 80 bytes | **VSAM File:** `USRSEC.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Description |
|---|-------------|-----------|------|------|---------------|-------------|
| 1 | `SEC-USR-ID` | `X(08)` | Alpha | 8 | User ID | Login user ID (primary key) |
| 2 | `SEC-USR-FNAME` | `X(20)` | Alpha | 20 | First Name | User first name |
| 3 | `SEC-USR-LNAME` | `X(20)` | Alpha | 20 | Last Name | User last name |
| 4 | `SEC-USR-PWD` | `X(08)` | Alpha | 8 | Password | User password (plaintext) |
| 5 | `SEC-USR-TYPE` | `X(01)` | Alpha | 1 | User Type | A=Admin, U=Regular User |
| 6 | `SEC-USR-FILLER` | `X(23)` | — | 23 | (Reserved) | Unused space |

**Key:** `SEC-USR-ID` (8-byte alphanumeric)
**Security Note:** Passwords stored in plaintext — critical modernization concern.

---

## 9. Transaction Entity (CVTRA05Y)

**Copybook:** `app/cpy/CVTRA05Y.cpy` | **Record Length:** 350 bytes | **VSAM File:** `TRANSACT.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Description |
|---|-------------|-----------|------|------|---------------|-------------|
| 1 | `TRAN-ID` | `X(16)` | Alpha | 16 | Transaction ID | Unique transaction identifier (primary key) |
| 2 | `TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction Type | Type code (FK → Tran Type) |
| 3 | `TRAN-CAT-CD` | `9(04)` | Numeric | 4 | Category Code | Category code (FK → Tran Category) |
| 4 | `TRAN-SOURCE` | `X(10)` | Alpha | 10 | Source | Transaction origin (POS, ATM, Online, etc.) |
| 5 | `TRAN-DESC` | `X(100)` | Alpha | 100 | Description | Transaction narrative |
| 6 | `TRAN-AMT` | `S9(09)V99` | Signed Decimal | 11 | Amount | Transaction amount (dollars.cents) |
| 7 | `TRAN-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant ID | Merchant identifier |
| 8 | `TRAN-MERCHANT-NAME` | `X(50)` | Alpha | 50 | Merchant Name | Merchant business name |
| 9 | `TRAN-MERCHANT-CITY` | `X(50)` | Alpha | 50 | Merchant City | Merchant city |
| 10 | `TRAN-MERCHANT-ZIP` | `X(10)` | Alpha | 10 | Merchant ZIP | Merchant postal code |
| 11 | `TRAN-CARD-NUM` | `X(16)` | Alpha | 16 | Card Number | Card used (FK → Card) |
| 12 | `TRAN-ORIG-TS` | `X(26)` | Alpha | 26 | Origination Timestamp | When transaction was initiated |
| 13 | `TRAN-PROC-TS` | `X(26)` | Alpha | 26 | Processing Timestamp | When transaction was processed |
| 14 | `FILLER` | `X(20)` | — | 20 | (Reserved) | Unused space |

**Key:** `TRAN-ID` (16-byte alphanumeric)

---

## 10. Daily Transaction Entity (CVTRA06Y)

**Copybook:** `app/cpy/CVTRA06Y.cpy` | **Record Length:** 350 bytes | **File:** `DALYTRAN` (sequential/VSAM)

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Description |
|---|-------------|-----------|------|------|---------------|-------------|
| 1 | `DALYTRAN-ID` | `X(16)` | Alpha | 16 | Transaction ID | Daily transaction ID |
| 2 | `DALYTRAN-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction Type | Type code |
| 3 | `DALYTRAN-CAT-CD` | `9(04)` | Numeric | 4 | Category Code | Category code |
| 4 | `DALYTRAN-SOURCE` | `X(10)` | Alpha | 10 | Source | Transaction origin |
| 5 | `DALYTRAN-DESC` | `X(100)` | Alpha | 100 | Description | Transaction narrative |
| 6 | `DALYTRAN-AMT` | `S9(09)V99` | Signed Decimal | 11 | Amount | Transaction amount |
| 7 | `DALYTRAN-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant ID | Merchant identifier |
| 8 | `DALYTRAN-MERCHANT-NAME` | `X(50)` | Alpha | 50 | Merchant Name | Business name |
| 9 | `DALYTRAN-MERCHANT-CITY` | `X(50)` | Alpha | 50 | Merchant City | City |
| 10 | `DALYTRAN-MERCHANT-ZIP` | `X(10)` | Alpha | 10 | Merchant ZIP | Postal code |
| 11 | `DALYTRAN-CARD-NUM` | `X(16)` | Alpha | 16 | Card Number | Card used |
| 12 | `DALYTRAN-ORIG-TS` | `X(26)` | Alpha | 26 | Origination Timestamp | When initiated |
| 13 | `DALYTRAN-PROC-TS` | `X(26)` | Alpha | 26 | Processing Timestamp | When processed |
| 14 | `FILLER` | `X(20)` | — | 20 | (Reserved) | Unused space |

**Note:** Identical structure to CVTRA05Y but prefixed `DALYTRAN-`. This holds unposted daily transactions before they are validated and moved to the master transaction file by CBTRN02C.

---

## 11. Transaction Category Balance Entity (CVTRA01Y)

**Copybook:** `app/cpy/CVTRA01Y.cpy` | **Record Length:** 50 bytes | **File:** `TCATBALF`

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Description |
|---|-------------|-----------|------|------|---------------|-------------|
| 1 | `TRANCAT-ACCT-ID` | `9(11)` | Numeric | 11 | Account ID | Account identifier (compound key part 1) |
| 2 | `TRANCAT-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction Type | Type code (compound key part 2) |
| 3 | `TRANCAT-CD` | `9(04)` | Numeric | 4 | Category Code | Category code (compound key part 3) |
| 4 | `TRAN-CAT-BAL` | `S9(09)V99` | Signed Decimal | 11 | Category Balance | Running balance per account/type/category |
| 5 | `FILLER` | `X(22)` | — | 22 | (Reserved) | Unused space |

**Key:** Compound — `TRANCAT-ACCT-ID` + `TRANCAT-TYPE-CD` + `TRANCAT-CD`

---

## 12. Disclosure Group Entity (CVTRA02Y)

**Copybook:** `app/cpy/CVTRA02Y.cpy` | **Record Length:** 50 bytes | **File:** `DISCGRP`

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Description |
|---|-------------|-----------|------|------|---------------|-------------|
| 1 | `DIS-ACCT-GROUP-ID` | `X(10)` | Alpha | 10 | Account Group ID | Disclosure group identifier (compound key part 1) |
| 2 | `DIS-TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction Type | Type code (compound key part 2) |
| 3 | `DIS-TRAN-CAT-CD` | `9(04)` | Numeric | 4 | Category Code | Category code (compound key part 3) |
| 4 | `DIS-INT-RATE` | `S9(04)V99` | Signed Decimal | 6 | Interest Rate | Interest rate percentage for this group/type/category |
| 5 | `FILLER` | `X(28)` | — | 28 | (Reserved) | Unused space |

**Key:** Compound — `DIS-ACCT-GROUP-ID` + `DIS-TRAN-TYPE-CD` + `DIS-TRAN-CAT-CD`
**Business Rule:** Interest rates are looked up by matching an account's group ID with the transaction type and category.

---

## 13. Transaction Type Entity (CVTRA03Y)

**Copybook:** `app/cpy/CVTRA03Y.cpy` | **Record Length:** 60 bytes | **File:** `TRANTYPE`

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Description |
|---|-------------|-----------|------|------|---------------|-------------|
| 1 | `TRAN-TYPE` | `X(02)` | Alpha | 2 | Transaction Type Code | Primary key (e.g., "SA" = Sale, "CR" = Credit) |
| 2 | `TRAN-TYPE-DESC` | `X(50)` | Alpha | 50 | Type Description | Human-readable description |
| 3 | `FILLER` | `X(08)` | — | 8 | (Reserved) | Unused space |

**Key:** `TRAN-TYPE` (2-byte alphanumeric)

---

## 14. Transaction Category Entity (CVTRA04Y)

**Copybook:** `app/cpy/CVTRA04Y.cpy` | **Record Length:** 60 bytes | **File:** `TRANCATG`

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Description |
|---|-------------|-----------|------|------|---------------|-------------|
| 1 | `TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction Type Code | Type code (compound key part 1, FK → Tran Type) |
| 2 | `TRAN-CAT-CD` | `9(04)` | Numeric | 4 | Category Code | Category code (compound key part 2) |
| 3 | `TRAN-CAT-TYPE-DESC` | `X(50)` | Alpha | 50 | Category Description | Human-readable category description |
| 4 | `FILLER` | `X(04)` | — | 4 | (Reserved) | Unused space |

**Key:** Compound — `TRAN-TYPE-CD` + `TRAN-CAT-CD`

---

## 15. Statement Transaction Entity (COSTM01)

**Copybook:** `app/cpy/COSTM01.CPY` | **Record Length:** 350 bytes | **Used by:** CBSTM03A/B (statement generation)

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Description |
|---|-------------|-----------|------|------|---------------|-------------|
| 1 | `TRNX-CARD-NUM` | `X(16)` | Alpha | 16 | Card Number | Card number (compound key part 1) |
| 2 | `TRNX-ID` | `X(16)` | Alpha | 16 | Transaction ID | Transaction ID (compound key part 2) |
| 3 | `TRNX-TYPE-CD` | `X(02)` | Alpha | 2 | Type Code | Transaction type |
| 4 | `TRNX-CAT-CD` | `9(04)` | Numeric | 4 | Category Code | Transaction category |
| 5 | `TRNX-SOURCE` | `X(10)` | Alpha | 10 | Source | Transaction origin |
| 6 | `TRNX-DESC` | `X(100)` | Alpha | 100 | Description | Narrative |
| 7 | `TRNX-AMT` | `S9(09)V99` | Signed Decimal | 11 | Amount | Transaction amount |
| 8 | `TRNX-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant ID | Merchant |
| 9 | `TRNX-MERCHANT-NAME` | `X(50)` | Alpha | 50 | Merchant Name | Business name |
| 10 | `TRNX-MERCHANT-CITY` | `X(50)` | Alpha | 50 | Merchant City | City |
| 11 | `TRNX-MERCHANT-ZIP` | `X(10)` | Alpha | 10 | Merchant ZIP | Postal code |
| 12 | `TRNX-ORIG-TS` | `X(26)` | Alpha | 26 | Origination Timestamp | When initiated |
| 13 | `TRNX-PROC-TS` | `X(26)` | Alpha | 26 | Processing Timestamp | When processed |
| 14 | `FILLER` | `X(20)` | — | 20 | (Reserved) | Unused space |

**Note:** Re-keyed version of the transaction record with `CARD-NUM + TRAN-ID` as the composite key (instead of just `TRAN-ID`). Created by CREASTMT JCL using SORT to enable card-by-card statement generation.

---

## 16. Transaction Report Structures (CVTRA07Y)

**Copybook:** `app/cpy/CVTRA07Y.cpy` | **Used by:** CBTRN03C (transaction report)

### Report Header
| # | COBOL Field | PIC Clause | Type | Business Name |
|---|-------------|-----------|------|---------------|
| 1 | `REPT-SHORT-NAME` | `X(38)` | Alpha | Report Short Name ("DALYREPT") |
| 2 | `REPT-LONG-NAME` | `X(41)` | Alpha | Report Title ("Daily Transaction Report") |
| 3 | `REPT-DATE-HEADER` | `X(12)` | Alpha | Date Header Label |
| 4 | `REPT-START-DATE` | `X(10)` | Alpha | Report Start Date |
| 5 | `REPT-END-DATE` | `X(10)` | Alpha | Report End Date |

### Transaction Detail Line
| # | COBOL Field | PIC Clause | Type | Business Name |
|---|-------------|-----------|------|---------------|
| 1 | `TRAN-REPORT-TRANS-ID` | `X(16)` | Alpha | Transaction ID |
| 2 | `TRAN-REPORT-ACCOUNT-ID` | `X(11)` | Alpha | Account ID |
| 3 | `TRAN-REPORT-TYPE-CD` | `X(02)` | Alpha | Type Code |
| 4 | `TRAN-REPORT-TYPE-DESC` | `X(15)` | Alpha | Type Description |
| 5 | `TRAN-REPORT-CAT-CD` | `9(04)` | Numeric | Category Code |
| 6 | `TRAN-REPORT-CAT-DESC` | `X(29)` | Alpha | Category Description |
| 7 | `TRAN-REPORT-SOURCE` | `X(10)` | Alpha | Source |
| 8 | `TRAN-REPORT-AMT` | `-ZZZ,ZZZ,ZZZ.ZZ` | Edited Numeric | Amount |

### Report Totals
| Structure | Field | Description |
|-----------|-------|-------------|
| `REPORT-PAGE-TOTALS` | `REPT-PAGE-TOTAL` | Subtotal per printed page |
| `REPORT-ACCOUNT-TOTALS` | `REPT-ACCOUNT-TOTAL` | Subtotal per account |
| `REPORT-GRAND-TOTALS` | `REPT-GRAND-TOTAL` | Grand total for the entire report |

---

## 17. Export Record Entity (CVEXPORT)

**Copybook:** `app/cpy/CVEXPORT.cpy` | **Used by:** CBEXPORT / CBIMPORT

This is a union-type record that wraps all five core entities into a single export format:

| # | COBOL Field | PIC Clause | Size | Business Name | Description |
|---|-------------|-----------|------|---------------|-------------|
| 1 | `EXP-RECORD-TYPE` | `X(01)` | 1 | Record Type | C=Customer, A=Account, X=X-Ref, R=Card, T=Transaction |
| 2 | `EXP-DATA` | `X(499)` | 499 | Record Data | Payload (redefined per type below) |

**Record Type Redefinitions:**

| Type Code | Structure | Maps To |
|-----------|-----------|---------|
| C | `EXP-CUST-*` fields | Customer (CVCUS01Y layout) |
| A | `EXP-ACCT-*` fields | Account (CVACT01Y layout) |
| X | `EXP-XREF-*` fields | Card X-Ref (CVACT03Y layout) |
| R | `EXP-CARD-*` fields | Card (CVACT02Y layout) |
| T | `EXP-TRAN-*` fields | Transaction (CVTRA05Y layout) |

**Purpose:** Enables bulk data export/import through a single sequential file, used for data migration and backup.

---

## 18. Common Communication Area (COCOM01Y)

**Copybook:** `app/cpy/COCOM01Y.cpy` | **Used by:** All online CICS programs

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Description |
|---|-------------|-----------|------|------|---------------|-------------|
| 1 | `CDEMO-FROM-TRANID` | `X(04)` | Alpha | 4 | Source Transaction | CICS transaction ID of the calling program |
| 2 | `CDEMO-FROM-PROGRAM` | `X(08)` | Alpha | 8 | Source Program | Program name of the caller |
| 3 | `CDEMO-TO-TRANID` | `X(04)` | Alpha | 4 | Target Transaction | CICS transaction ID to navigate to |
| 4 | `CDEMO-TO-PROGRAM` | `X(08)` | Alpha | 8 | Target Program | Program name to XCTL to |
| 5 | `CDEMO-PGM-REENTER` | `9(01)` | Numeric | 1 | Re-enter Flag | 0=first entry, 1=re-enter from child |
| 6 | `CDEMO-USR-ID` | `X(08)` | Alpha | 8 | User ID | Currently signed-in user |
| 7 | `CDEMO-USR-TYP` | `X(01)` | Alpha | 1 | User Type | A=Admin, U=Regular |
| 8 | `CDEMO-USR-FNAME` | `X(20)` | Alpha | 20 | First Name | Current user's first name |
| 9 | `CDEMO-USR-LNAME` | `X(20)` | Alpha | 20 | Last Name | Current user's last name |
| — | Additional `CDEMO-*` fields | Various | — | — | Context Data | Account IDs, card numbers, selection flags |

**Purpose:** This is the COMMAREA — the shared state passed between all CICS programs via XCTL and RETURN. It carries user session context, navigation state, and selected entity keys.

---

## 19. Menu Definitions (COMEN02Y)

**Copybook:** `app/cpy/COMEN02Y.cpy` | **Used by:** COMEN01C (Main Menu)

Contains arrays of menu option definitions:
- `CDEMO-MENU-OPT-COUNT` — Number of menu options
- `CDEMO-MENU-OPT-NAME(n)` — Display name for option n
- `CDEMO-MENU-OPT-PGMNAME(n)` — Program to invoke for option n
- `CDEMO-MENU-OPT-TRNNAME(n)` — CICS transaction ID for option n
- `CDEMO-MENU-OPT-USRTYPE(n)` — Required user type (A/U/B for both)

---

## 20. Admin Menu Definitions (COADM02Y)

**Copybook:** `app/cpy/COADM02Y.cpy` | **Used by:** COADM01C (Admin Menu)

Same structure as COMEN02Y but for admin-specific menu options (user management CRUD).

---

## 21. Lookup Code Tables (CSLKPCDY)

**Copybook:** `app/cpy/CSLKPCDY.cpy` | **1,318 lines** | **Used by:** Programs needing reference data validation

Contains hardcoded lookup tables:
- **US State Codes** — 50 states + DC + territories with names
- **Country Codes** — ISO 3166 country codes with names
- **Transaction Source Codes** — POS, ATM, Online, Phone, Mail, etc.

**Modernization Note:** These hardcoded tables should be externalized to a database reference table.

---

## 22. Date Utility Fields (CSDAT01Y, CODATECN)

**CSDAT01Y** (`app/cpy/CSDAT01Y.cpy`) — Working storage for date/time operations:
- `WS-CURDATE` — Current date (YYYYMMDD)
- `WS-CURTIME` — Current time (HHMMSS)
- `WS-CURDATE-YEAR/MONTH/DAY` — Individual date components

**CODATECN** (`app/cpy/CODATECN.cpy`) — Date conversion constants:
- Date format patterns for CEEDAYS API
- Century window constants
- Date validation ranges

---

## 23. Unused Entity (UNUSED1Y)

**Copybook:** `app/cpy/UNUSED1Y.cpy` | **Record Length:** 80 bytes

| # | COBOL Field | PIC Clause | Size | Business Name |
|---|-------------|-----------|------|---------------|
| 1 | `UNUSED-ID` | `X(08)` | 8 | ID |
| 2 | `UNUSED-FNAME` | `X(20)` | 20 | First Name |
| 3 | `UNUSED-LNAME` | `X(20)` | 20 | Last Name |
| 4 | `UNUSED-PWD` | `X(08)` | 8 | Password |
| 5 | `UNUSED-TYPE` | `X(01)` | 1 | Type |
| 6 | `UNUSED-FILLER` | `X(23)` | 23 | Filler |

**Status:** Deprecated. Appears to be an earlier version of the user security record. Not referenced by any active program.

---

## 24. VSAM File Summary

| VSAM Dataset | Key | Record Len | Copybook | Entity |
|---|---|---|---|---|
| `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | ACCT-ID (11) | 300 | CVACT01Y | Account |
| `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` | CARD-NUM (16) | 150 | CVACT02Y | Card |
| `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | XREF-CARD-NUM (16) | 50 | CVACT03Y | Card X-Ref |
| `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` | CUST-ID (9) | 500 | CVCUS01Y | Customer |
| `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | TRAN-ID (16) | 350 | CVTRA05Y | Transaction |
| `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` | SEC-USR-ID (8) | 80 | CSUSR01Y | User Security |
| `AWS.M2.CARDDEMO.TCATBALF` | Compound (17) | 50 | CVTRA01Y | Tran Cat Balance |
| `AWS.M2.CARDDEMO.DISCGRP` | Compound (16) | 50 | CVTRA02Y | Disclosure Group |
| `AWS.M2.CARDDEMO.TRANTYPE` | TRAN-TYPE (2) | 60 | CVTRA03Y | Transaction Type |
| `AWS.M2.CARDDEMO.TRANCATG` | Compound (6) | 60 | CVTRA04Y | Tran Category |

---

## 25. PIC Clause Reference

For readers unfamiliar with COBOL PIC clauses:

| PIC Clause | Java Equivalent | Description |
|---|---|---|
| `X(n)` | `String` (length n) | Alphanumeric, fixed-length |
| `9(n)` | `long` / `int` | Unsigned integer, n digits |
| `S9(n)` | `long` / `int` | Signed integer, n digits |
| `S9(n)V99` | `BigDecimal` | Signed decimal, n integer digits + 2 decimal |
| `9(n) COMP` | `int` / `long` | Binary integer (packed) |
| `S9(n) COMP` | `int` / `long` | Signed binary integer |
| `-ZZZ,ZZZ,ZZZ.ZZ` | Formatted `String` | Edited numeric for display |
