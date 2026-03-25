# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** CardDemo - Credit Card Management System
> **Source:** COBOL copybooks in `app/cpy/` and `app/cpy-bms/`

---

## Overview

This document extracts every business data entity from the CardDemo copybook PIC clauses and presents them in a business-friendly format. Each entity maps to a VSAM KSDS file on the mainframe and will translate to a relational database table in the modernized Java application.

---

## 1. Account Master (`CVACT01Y.cpy`)

**VSAM File:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`
**Record Length:** 300 bytes | **Key:** Account ID (11 digits, position 0)
**Business Purpose:** Stores credit card account information including balances, limits, and status.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | ACCT-ID | `9(11)` | Numeric | 11 | Account identifier (primary key) |
| 2 | ACCT-ACTIVE-STATUS | `X(01)` | Alpha | 1 | Account status (Y=Active, N=Inactive) |
| 3 | ACCT-CURR-BAL | `S9(10)V99` | Signed Decimal | 12.2 | Current account balance |
| 4 | ACCT-CREDIT-LIMIT | `S9(10)V99` | Signed Decimal | 12.2 | Credit limit |
| 5 | ACCT-CASH-CREDIT-LIMIT | `S9(10)V99` | Signed Decimal | 12.2 | Cash advance credit limit |
| 6 | ACCT-OPEN-DATE | `X(10)` | Date String | 10 | Account opening date |
| 7 | ACCT-EXPIRAION-DATE | `X(10)` | Date String | 10 | Account expiration date |
| 8 | ACCT-REISSUE-DATE | `X(10)` | Date String | 10 | Card reissue date |
| 9 | ACCT-CURR-CYC-CREDIT | `S9(10)V99` | Signed Decimal | 12.2 | Current cycle credits |
| 10 | ACCT-CURR-CYC-DEBIT | `S9(10)V99` | Signed Decimal | 12.2 | Current cycle debits |
| 11 | ACCT-ADDR-ZIP | `X(10)` | Alpha | 10 | Account holder ZIP code |
| 12 | ACCT-GROUP-ID | `X(10)` | Alpha | 10 | Disclosure group ID |
| 13 | FILLER | `X(178)` | Filler | 178 | Reserved space |

**Modernization Target:** `Account` JPA entity / `accounts` table

---

## 2. Card Data (`CVACT02Y.cpy`)

**VSAM File:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`
**Record Length:** 150 bytes | **Key:** Card Number (16 chars, position 0)
**Business Purpose:** Stores individual credit card details linked to accounts.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | CARD-NUM | `X(16)` | Alpha | 16 | Card number (primary key) |
| 2 | CARD-ACCT-ID | `9(11)` | Numeric | 11 | Parent account ID (FK to Account) |
| 3 | CARD-CVV-CD | `9(03)` | Numeric | 3 | Card CVV code |
| 4 | CARD-EMBOSSED-NAME | `X(50)` | Alpha | 50 | Name embossed on card |
| 5 | CARD-EXPIRAION-DATE | `X(10)` | Date String | 10 | Card expiration date |
| 6 | CARD-ACTIVE-STATUS | `X(01)` | Alpha | 1 | Card status (Y=Active, N=Inactive) |
| 7 | FILLER | `X(59)` | Filler | 59 | Reserved space |

**Modernization Target:** `Card` JPA entity / `cards` table

---

## 3. Customer Data (`CVCUS01Y.cpy`)

**VSAM File:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`
**Record Length:** 500 bytes | **Key:** Customer ID (9 digits, position 0)
**Business Purpose:** Stores customer personal and contact information.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | CUST-ID | `9(09)` | Numeric | 9 | Customer identifier (primary key) |
| 2 | CUST-FIRST-NAME | `X(25)` | Alpha | 25 | Customer first name |
| 3 | CUST-MIDDLE-NAME | `X(25)` | Alpha | 25 | Customer middle name |
| 4 | CUST-LAST-NAME | `X(25)` | Alpha | 25 | Customer last name |
| 5 | CUST-ADDR-LINE-1 | `X(50)` | Alpha | 50 | Address line 1 |
| 6 | CUST-ADDR-LINE-2 | `X(50)` | Alpha | 50 | Address line 2 |
| 7 | CUST-ADDR-LINE-3 | `X(50)` | Alpha | 50 | Address line 3 |
| 8 | CUST-ADDR-STATE-CD | `X(02)` | Alpha | 2 | State code |
| 9 | CUST-ADDR-COUNTRY-CD | `X(03)` | Alpha | 3 | Country code |
| 10 | CUST-ADDR-ZIP | `X(10)` | Alpha | 10 | ZIP/Postal code |
| 11 | CUST-PHONE-NUM-1 | `X(15)` | Alpha | 15 | Primary phone |
| 12 | CUST-PHONE-NUM-2 | `X(15)` | Alpha | 15 | Secondary phone |
| 13 | CUST-SSN | `9(09)` | Numeric | 9 | Social Security Number (PII) |
| 14 | CUST-GOVT-ISSUED-ID | `X(20)` | Alpha | 20 | Government-issued ID |
| 15 | CUST-DOB-YYYY-MM-DD | `X(10)` | Date String | 10 | Date of birth |
| 16 | CUST-EFT-ACCOUNT-ID | `X(10)` | Alpha | 10 | EFT/bank account ID |
| 17 | CUST-PRI-CARD-HOLDER-IND | `X(01)` | Alpha | 1 | Primary card holder flag |
| 18 | CUST-FICO-CREDIT-SCORE | `9(03)` | Numeric | 3 | FICO credit score |
| 19 | FILLER | `X(168)` | Filler | 168 | Reserved space |

**Modernization Target:** `Customer` JPA entity / `customers` table

> **PII Warning:** Fields CUST-SSN, CUST-DOB-YYYY-MM-DD, and CUST-GOVT-ISSUED-ID contain personally identifiable information requiring encryption at rest in the target system.

---

## 4. Card Cross-Reference (`CVACT03Y.cpy`)

**VSAM File:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`
**Record Length:** 50 bytes | **Key:** Card Number (16 chars, position 0)
**Alternate Index:** Account ID (11 bytes at position 25, non-unique)
**Business Purpose:** Links card numbers to account IDs and customer IDs for lookup.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | XREF-CARD-NUM | `X(16)` | Alpha | 16 | Card number (primary key) |
| 2 | XREF-CUST-ID | `9(09)` | Numeric | 9 | Customer ID (FK) |
| 3 | XREF-ACCT-ID | `9(11)` | Numeric | 11 | Account ID (FK, alternate index key) |
| 4 | FILLER | `X(14)` | Filler | 14 | Reserved space |

**Modernization Target:** `CardCrossReference` JPA entity / `card_xref` table (or denormalized into Card)

---

## 5. Online Program Work Area (`CVCRD01Y.cpy`)

**Structure:** `CC-WORK-AREAS` | **Used By:** COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC
**Business Purpose:** Shared communication/work area for online CICS programs. Carries navigation state (next program, mapset, map), AID key identification, error/return messages, and current entity IDs between screen interactions.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | CCARD-AID | `X(5)` | Alpha | 5 | AID key pressed (ENTER, CLEAR, PFK01-12, PA1-2) |
| 2 | CCARD-NEXT-PROG | `X(8)` | Alpha | 8 | Next program to XCTL to |
| 3 | CCARD-NEXT-MAPSET | `X(7)` | Alpha | 7 | Next BMS mapset name |
| 4 | CCARD-NEXT-MAP | `X(7)` | Alpha | 7 | Next BMS map name |
| 5 | CCARD-ERROR-MSG | `X(75)` | Alpha | 75 | Error message for screen display |
| 6 | CCARD-RETURN-MSG | `X(75)` | Alpha | 75 | Return/info message for screen display |
| 7 | CC-ACCT-ID | `X(11)` | Alpha | 11 | Current account ID context |
| 8 | CC-CARD-NUM | `X(16)` | Alpha | 16 | Current card number context |
| 9 | CC-CUST-ID | `X(09)` | Alpha | 9 | Current customer ID context |

**Modernization Target:** Replace with Spring MVC session attributes, controller state, or SPA client-side state management. AID key mapping becomes UI event handling.

---

## 6. Transaction Record (`CVTRA05Y.cpy`)

**VSAM File:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`
**Record Length:** 350 bytes | **Key:** Transaction ID (16 chars, position 0)
**Business Purpose:** Master transaction record for all posted credit card transactions.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | TRAN-ID | `X(16)` | Alpha | 16 | Transaction ID (primary key) |
| 2 | TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code (FK) |
| 3 | TRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code (FK) |
| 4 | TRAN-SOURCE | `X(10)` | Alpha | 10 | Transaction source (POS, ATM, etc.) |
| 5 | TRAN-DESC | `X(100)` | Alpha | 100 | Transaction description |
| 6 | TRAN-AMT | `S9(09)V99` | Signed Decimal | 11.2 | Transaction amount |
| 7 | TRAN-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant identifier |
| 8 | TRAN-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| 9 | TRAN-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| 10 | TRAN-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP code |
| 11 | TRAN-CARD-NUM | `X(16)` | Alpha | 16 | Card number used (FK) |
| 12 | TRAN-ORIG-TS | `X(26)` | Timestamp | 26 | Original transaction timestamp |
| 13 | TRAN-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | `X(20)` | Filler | 20 | Reserved space |

**Modernization Target:** `Transaction` JPA entity / `transactions` table

---

## 7. Daily Transaction Record (`CVTRA06Y.cpy`)

**VSAM File:** `AWS.M2.CARDDEMO.DALYTRAN.PS` (sequential)
**Record Length:** 350 bytes
**Business Purpose:** Daily batch input of new transactions before posting to master.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | DALYTRAN-ID | `X(16)` | Alpha | 16 | Daily transaction ID |
| 2 | DALYTRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| 3 | DALYTRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 4 | DALYTRAN-SOURCE | `X(10)` | Alpha | 10 | Transaction source |
| 5 | DALYTRAN-DESC | `X(100)` | Alpha | 100 | Transaction description |
| 6 | DALYTRAN-AMT | `S9(09)V99` | Signed Decimal | 11.2 | Transaction amount |
| 7 | DALYTRAN-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant identifier |
| 8 | DALYTRAN-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| 9 | DALYTRAN-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| 10 | DALYTRAN-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP code |
| 11 | DALYTRAN-CARD-NUM | `X(16)` | Alpha | 16 | Card number used |
| 12 | DALYTRAN-ORIG-TS | `X(26)` | Timestamp | 26 | Original timestamp |
| 13 | DALYTRAN-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | `X(20)` | Filler | 20 | Reserved space |

**Modernization Target:** Staging table `daily_transactions` or direct API input

---

## 8. Transaction Category Balance (`CVTRA01Y.cpy`)

**VSAM File:** `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS`
**Record Length:** 50 bytes | **Key:** Account ID + Type Code + Category Code (17 bytes)
**Business Purpose:** Running balance per account per transaction type/category.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | TRANCAT-ACCT-ID | `9(11)` | Numeric | 11 | Account identifier (partial key) |
| 2 | TRANCAT-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code (partial key) |
| 3 | TRANCAT-CD | `9(04)` | Numeric | 4 | Category code (partial key) |
| 4 | TRAN-CAT-BAL | `S9(09)V99` | Signed Decimal | 11.2 | Category balance amount |
| 5 | FILLER | `X(22)` | Filler | 22 | Reserved space |

**Modernization Target:** `TransactionCategoryBalance` JPA entity / `tran_cat_balances` table

---

## 9. Disclosure Group (`CVTRA02Y.cpy`)

**VSAM File:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`
**Record Length:** 50 bytes | **Key:** Group ID + Type Code + Category Code (16 bytes)
**Business Purpose:** Interest rate configuration by disclosure group and transaction category.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | DIS-ACCT-GROUP-ID | `X(10)` | Alpha | 10 | Disclosure group identifier (partial key) |
| 2 | DIS-TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code (partial key) |
| 3 | DIS-TRAN-CAT-CD | `9(04)` | Numeric | 4 | Category code (partial key) |
| 4 | DIS-INT-RATE | `S9(04)V99` | Signed Decimal | 6.2 | Interest rate percentage |
| 5 | FILLER | `X(28)` | Filler | 28 | Reserved space |

**Modernization Target:** `DisclosureGroup` JPA entity / `disclosure_groups` table

---

## 10. Transaction Type (`CVTRA03Y.cpy`)

**VSAM File:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`
**Record Length:** 60 bytes | **Key:** Transaction Type Code (2 chars)
**Business Purpose:** Reference table of transaction type codes and descriptions.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | TRAN-TYPE | `X(02)` | Alpha | 2 | Transaction type code (primary key) |
| 2 | TRAN-TYPE-DESC | `X(50)` | Alpha | 50 | Type description |
| 3 | FILLER | `X(08)` | Filler | 8 | Reserved space |

**Modernization Target:** `TransactionType` JPA entity / `transaction_types` table (or enum)

---

## 11. Transaction Category (`CVTRA04Y.cpy`)

**VSAM File:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`
**Record Length:** 60 bytes | **Key:** Type Code + Category Code (6 bytes)
**Business Purpose:** Reference table of transaction categories within each type.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code (partial key) |
| 2 | TRAN-CAT-CD | `9(04)` | Numeric | 4 | Category code (partial key) |
| 3 | TRAN-CAT-TYPE-DESC | `X(50)` | Alpha | 50 | Category description |
| 4 | FILLER | `X(04)` | Filler | 4 | Reserved space |

**Modernization Target:** `TransactionCategory` JPA entity / `transaction_categories` table

---

## 12. User Security Record (`CSUSR01Y.cpy`)

**VSAM File:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`
**Record Length:** 80 bytes | **Key:** User ID (8 chars)
**Business Purpose:** Application user credentials and role assignments.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | SEC-USR-ID | `X(08)` | Alpha | 8 | User ID (primary key) |
| 2 | SEC-USR-FNAME | `X(20)` | Alpha | 20 | User first name |
| 3 | SEC-USR-LNAME | `X(20)` | Alpha | 20 | User last name |
| 4 | SEC-USR-PWD | `X(08)` | Alpha | 8 | User password (plaintext) |
| 5 | SEC-USR-TYPE | `X(01)` | Alpha | 1 | User type (A=Admin, U=Regular) |
| 6 | SEC-USR-FILLER | `X(23)` | Filler | 23 | Reserved space |

**Modernization Target:** `User` JPA entity / `users` table

> **Security Warning:** Passwords are stored in plaintext. The modernized system MUST use salted password hashing (e.g., bcrypt) and proper authentication/authorization (Spring Security).

---

## 13. Statement Transaction Layout (`COSTM01.CPY`)

**Record Length:** 350 bytes | **Key:** Card Number + Transaction ID (32 bytes)
**Business Purpose:** Re-keyed transaction layout for statement generation (sorted by card).

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | TRNX-CARD-NUM | `X(16)` | Alpha | 16 | Card number (primary sort key) |
| 2 | TRNX-ID | `X(16)` | Alpha | 16 | Transaction ID (secondary sort key) |
| 3 | TRNX-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| 4 | TRNX-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 5 | TRNX-SOURCE | `X(10)` | Alpha | 10 | Transaction source |
| 6 | TRNX-DESC | `X(100)` | Alpha | 100 | Transaction description |
| 7 | TRNX-AMT | `S9(09)V99` | Signed Decimal | 11.2 | Transaction amount |
| 8 | TRNX-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant identifier |
| 9 | TRNX-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| 10 | TRNX-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| 11 | TRNX-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP code |
| 12 | TRNX-ORIG-TS | `X(26)` | Timestamp | 26 | Original transaction timestamp |
| 13 | TRNX-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | `X(20)` | Filler | 20 | Reserved space |

**Modernization Target:** SQL VIEW or DTO projection (no separate table needed)

---

## 14. Export Record (`CVEXPORT.cpy`)

**File:** `AWS.M2.CARDDEMO.EXPORT.DATA` (sequential)
**Business Purpose:** Consolidated export record layout containing data from all entity types.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | EXP-RECORD-TYPE | `X(01)` | Alpha | 1 | Record type identifier (C=Customer, A=Account, X=Xref, T=Transaction, R=Card) |
| 2 | EXP-CUST-ID | `9(09)` | Numeric | 9 | Customer ID |
| 3 | EXP-CUST-FIRST-NAME | `X(25)` | Alpha | 25 | Customer first name |
| 4 | EXP-CUST-MIDDLE-NAME | `X(25)` | Alpha | 25 | Customer middle name |
| 5 | EXP-CUST-LAST-NAME | `X(25)` | Alpha | 25 | Customer last name |
| 6 | EXP-ACCT-ID | `9(11)` | Numeric | 11 | Account ID |
| 7 | EXP-ACCT-CURR-BAL | `S9(10)V99` | Signed Decimal | 12.2 | Current balance |
| 8 | EXP-ACCT-CREDIT-LIMIT | `S9(10)V99` | Signed Decimal | 12.2 | Credit limit |
| 9 | EXP-CARD-NUM | `X(16)` | Alpha | 16 | Card number |
| 10 | EXP-CARD-EXPIRAION-DATE | `X(10)` | Date String | 10 | Card expiration date |
| 11 | EXP-CARD-ACTIVE-STATUS | `X(01)` | Alpha | 1 | Card active status |
| 12 | FILLER | `X(373)` | Filler | 373 | Reserved space |

**Modernization Target:** Export DTO / CSV serialization logic

---

## 15. Transaction Report Layout (`CVTRA07Y.cpy`)

**Business Purpose:** Print layout definitions for the daily transaction report.

### Report Header
| Field | PIC Clause | Value/Size | Description |
|-------|-----------|------------|-------------|
| REPT-SHORT-NAME | `X(38)` | 'DALYREPT' | Report short name |
| REPT-LONG-NAME | `X(41)` | 'Daily Transaction Report' | Report title |
| REPT-DATE-HEADER | `X(12)` | 'Date Range: ' | Date range label |
| REPT-START-DATE | `X(10)` | (runtime) | Report start date |
| REPT-END-DATE | `X(10)` | (runtime) | Report end date |

### Detail Line (133 chars)
| Field | PIC Clause | Size | Description |
|-------|-----------|------|-------------|
| TRAN-REPORT-TRANS-ID | `X(16)` | 16 | Transaction ID |
| TRAN-REPORT-ACCOUNT-ID | `X(11)` | 11 | Account ID |
| TRAN-REPORT-TYPE-CD | `X(02)` | 2 | Type code |
| TRAN-REPORT-TYPE-DESC | `X(15)` | 15 | Type description |
| TRAN-REPORT-CAT-CD | `9(04)` | 4 | Category code |
| TRAN-REPORT-CAT-DESC | `X(29)` | 29 | Category description |
| TRAN-REPORT-SOURCE | `X(10)` | 10 | Transaction source |
| TRAN-REPORT-AMT | `-ZZZ,ZZZ,ZZZ.ZZ` | 16 | Formatted amount |

### Totals
| Field | PIC Clause | Description |
|-------|-----------|-------------|
| REPT-PAGE-TOTAL | `+ZZZ,ZZZ,ZZZ.ZZ` | Page subtotal |
| REPT-ACCOUNT-TOTAL | `+ZZZ,ZZZ,ZZZ.ZZ` | Account subtotal |
| REPT-GRAND-TOTAL | `+ZZZ,ZZZ,ZZZ.ZZ` | Grand total |

**Modernization Target:** Report service / PDF generator / JasperReports template

---

## 16. Common Communication Area (`COCOM01Y.cpy`)

**Business Purpose:** CICS COMMAREA used to pass context between online programs during screen navigation.

| Field Group | Key Fields | Description |
|-------------|-----------|-------------|
| CDEMO-FROM-PROGRAM | `X(08)` | Calling program name |
| CDEMO-FROM-TRANID | `X(04)` | Calling transaction ID |
| CDEMO-TO-PROGRAM | `X(08)` | Target program name |
| CDEMO-TO-TRANID | `X(04)` | Target transaction ID |
| CDEMO-PGM-CONTEXT | varies | Program-specific context (selected IDs, page state, etc.) |
| CDEMO-USER-ID | `X(08)` | Current logged-in user |
| CDEMO-USER-TYPE | `X(01)` | Current user type (A/U) |

**Modernization Target:** HTTP Session / JWT claims / Spring Security context

---

## 17. Unused Record (`UNUSED1Y.cpy`)

**Record Length:** 80 bytes
**Status:** **DEPRECATED** - No active references found.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | UNUSED-ID | `X(08)` | Alpha | 8 | Identifier |
| 2 | UNUSED-FNAME | `X(20)` | Alpha | 20 | First name |
| 3 | UNUSED-LNAME | `X(20)` | Alpha | 20 | Last name |
| 4 | UNUSED-PWD | `X(08)` | Alpha | 8 | Password |
| 5 | UNUSED-TYPE | `X(01)` | Alpha | 1 | Type |
| 6 | UNUSED-FILLER | `X(23)` | Filler | 23 | Reserved |

**Modernization Target:** Exclude from migration

---

## Entity Relationship Summary

```
Customer (CVCUS01Y) 1──┐
                       │
                       ├──M Card Cross-Reference (CVACT03Y)
                       │        │
Account (CVACT01Y) 1───┘        │
       │                        │
       ├── M Card (CVACT02Y) ──┘
       │
       ├── M Transaction (CVTRA05Y)
       │        │
       │        ├── 1 Transaction Type (CVTRA03Y)
       │        └── 1 Transaction Category (CVTRA04Y)
       │
       ├── M Tran Category Balance (CVTRA01Y)
       │
       └── 1 Disclosure Group (CVTRA02Y)

User Security (CSUSR01Y) -- standalone

Daily Transaction (CVTRA06Y) -- staging input → Transaction
```

---

## VSAM-to-Table Mapping Summary

| VSAM Dataset | Copybook | Proposed Table | Key Type | Record Size |
|-------------|----------|---------------|----------|-------------|
| ACCTDATA.VSAM.KSDS | CVACT01Y | `accounts` | KSDS (Acct ID) | 300 |
| CARDDATA.VSAM.KSDS | CVACT02Y | `cards` | KSDS (Card #) | 150 |
| CUSTDATA.VSAM.KSDS | CVCUS01Y | `customers` | KSDS (Cust ID) | 500 |
| CARDXREF.VSAM.KSDS | CVACT03Y | `card_xref` | KSDS + AIX | 50 |
| TRANSACT.VSAM.KSDS | CVTRA05Y | `transactions` | KSDS (Tran ID) | 350 |
| DALYTRAN.PS | CVTRA06Y | `daily_transactions` | Sequential | 350 |
| TCATBALF.VSAM.KSDS | CVTRA01Y | `tran_cat_balances` | KSDS (Composite) | 50 |
| DISCGRP.VSAM.KSDS | CVTRA02Y | `disclosure_groups` | KSDS (Composite) | 50 |
| TRANTYPE.VSAM.KSDS | CVTRA03Y | `transaction_types` | KSDS (Type CD) | 60 |
| TRANCATG.VSAM.KSDS | CVTRA04Y | `transaction_categories` | KSDS (Composite) | 60 |
| USRSEC.VSAM.KSDS | CSUSR01Y | `users` | KSDS (User ID) | 80 |
