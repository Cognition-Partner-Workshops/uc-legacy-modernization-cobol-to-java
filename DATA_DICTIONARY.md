# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management System)
> **Source:** Copybook PIC clause analysis from `app/cpy/` and `app/cpy-bms/`

---

## Table of Contents

1. [Overview](#overview)
2. [Business Entities](#business-entities)
   - [Account](#1-account-cvact01y)
   - [Card (Credit Card)](#2-card-credit-card-cvact02y)
   - [Card Cross-Reference](#3-card-cross-reference-cvact03y)
   - [Customer](#4-customer-cvcus01y)
   - [Transaction](#5-transaction-cvtra05y)
   - [Daily Transaction](#6-daily-transaction-cvtra06y)
   - [Transaction (Statement Layout)](#7-transaction-statement-layout-costm01)
   - [User Security](#8-user-security-csusr01y)
   - [Transaction Category Balance](#9-transaction-category-balance-cvtra01y)
   - [Disclosure Group](#10-disclosure-group-cvtra02y)
   - [Transaction Type](#11-transaction-type-cvtra03y)
   - [Transaction Category](#12-transaction-category-cvtra04y)
3. [Communication & Navigation Structures](#communication--navigation-structures)
   - [COMMAREA](#commarea-cocom01y)
   - [Main Menu Options](#main-menu-options-comen02y)
   - [Admin Menu Options](#admin-menu-options-coadm02y)
4. [Presentation / UI Structures](#presentation--ui-structures)
5. [Utility Structures](#utility-structures)
6. [VSAM File Summary](#vsam-file-summary)
7. [Data Type Reference](#data-type-reference)

---

## Overview

The CardDemo application stores all persistent data in **VSAM KSDS** (Key-Sequenced Data Sets) files on the mainframe. Each VSAM file's record layout is defined in a COBOL copybook. This dictionary translates those copybook PIC clauses into business-friendly descriptions.

**Naming Conventions:**
- `CV*` — VSAM data structure copybooks
- `CO*` — Communication / online program copybooks
- `CS*` — Shared system / utility copybooks
- `PIC X(n)` — Alphanumeric field, n characters
- `PIC 9(n)` — Numeric field, n digits
- `PIC S9(n)V99` — Signed decimal with 2 decimal places
- `COMP` — Binary (computational) storage

---

## Business Entities

### 1. Account (CVACT01Y)

**VSAM File:** `ACCTDATA.VSAM.KSDS` | **Record Length:** 300 bytes | **Key:** Account ID (11 digits)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|---|
| 1 | ACCT-ID | `9(11)` | Numeric | 11 | Unique account identifier |
| 2 | ACCT-ACTIVE-STATUS | `X(01)` | Alpha | 1 | Account status (Y=Active) |
| 3 | ACCT-CURR-BAL | `S9(10)V99` | Decimal | 12 | Current account balance |
| 4 | ACCT-CREDIT-LIMIT | `S9(10)V99` | Decimal | 12 | Credit limit |
| 5 | ACCT-CASH-CREDIT-LIMIT | `S9(10)V99` | Decimal | 12 | Cash advance credit limit |
| 6 | ACCT-OPEN-DATE | `X(10)` | Date | 10 | Account opening date |
| 7 | ACCT-EXPIRAION-DATE | `X(10)` | Date | 10 | Account expiration date |
| 8 | ACCT-REISSUE-DATE | `X(10)` | Date | 10 | Last card reissue date |
| 9 | ACCT-CURR-CYC-CREDIT | `S9(10)V99` | Decimal | 12 | Current cycle credits |
| 10 | ACCT-CURR-CYC-DEBIT | `S9(10)V99` | Decimal | 12 | Current cycle debits |
| 11 | ACCT-ADDR-ZIP | `X(10)` | Alpha | 10 | Account ZIP code |
| 12 | ACCT-GROUP-ID | `X(10)` | Alpha | 10 | Account group (for disclosure/interest rates) |
| 13 | FILLER | `X(178)` | -- | 178 | Reserved space |

**Business Rules:**
- Account ID is the primary key for all account lookups
- Balance = Credits - Debits against credit limit
- Group ID links to disclosure/interest rate tables (DISCGRP)

---

### 2. Card (Credit Card) (CVACT02Y)

**VSAM File:** `CARDDATA.VSAM.KSDS` | **Record Length:** 150 bytes | **Key:** Card Number (16 digits)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|---|
| 1 | CARD-NUM | `X(16)` | Alpha | 16 | Credit card number (PAN) |
| 2 | CARD-ACCT-ID | `9(11)` | Numeric | 11 | Linked account ID |
| 3 | CARD-CVV-CD | `9(03)` | Numeric | 3 | Card verification value (CVV) |
| 4 | CARD-EMBOSSED-NAME | `X(50)` | Alpha | 50 | Name embossed on card |
| 5 | CARD-EXPIRAION-DATE | `X(10)` | Date | 10 | Card expiration date |
| 6 | CARD-ACTIVE-STATUS | `X(01)` | Alpha | 1 | Card status (Y=Active, N=Inactive) |
| 7 | FILLER | `X(59)` | -- | 59 | Reserved space |

**Business Rules:**
- One account can have multiple cards
- Card links to account via CARD-ACCT-ID
- CVV is stored in the record (security consideration for modernization)

---

### 3. Card Cross-Reference (CVACT03Y)

**VSAM File:** `CARDXREF.VSAM.KSDS` | **Record Length:** 50 bytes | **Key:** Card Number (16 chars)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|---|
| 1 | XREF-CARD-NUM | `X(16)` | Alpha | 16 | Credit card number |
| 2 | XREF-CUST-ID | `9(09)` | Numeric | 9 | Customer ID |
| 3 | XREF-ACCT-ID | `9(11)` | Numeric | 11 | Account ID |
| 4 | FILLER | `X(14)` | -- | 14 | Reserved space |

**Business Rules:**
- Links Card → Customer → Account in a single lookup
- Has an alternate index (AIX) on Account ID for reverse lookups
- Critical for transaction posting (card-to-account resolution)

---

### 4. Customer (CVCUS01Y)

**VSAM File:** `CUSTDATA.VSAM.KSDS` | **Record Length:** 500 bytes | **Key:** Customer ID (9 digits)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|---|
| 1 | CUST-ID | `9(09)` | Numeric | 9 | Unique customer identifier |
| 2 | CUST-FIRST-NAME | `X(25)` | Alpha | 25 | First name |
| 3 | CUST-MIDDLE-NAME | `X(25)` | Alpha | 25 | Middle name |
| 4 | CUST-LAST-NAME | `X(25)` | Alpha | 25 | Last name |
| 5 | CUST-ADDR-LINE-1 | `X(50)` | Alpha | 50 | Address line 1 |
| 6 | CUST-ADDR-LINE-2 | `X(50)` | Alpha | 50 | Address line 2 |
| 7 | CUST-ADDR-LINE-3 | `X(50)` | Alpha | 50 | Address line 3 |
| 8 | CUST-ADDR-STATE-CD | `X(02)` | Alpha | 2 | State code |
| 9 | CUST-ADDR-COUNTRY-CD | `X(03)` | Alpha | 3 | Country code |
| 10 | CUST-ADDR-ZIP | `X(10)` | Alpha | 10 | ZIP/postal code |
| 11 | CUST-PHONE-NUM-1 | `X(15)` | Alpha | 15 | Primary phone |
| 12 | CUST-PHONE-NUM-2 | `X(15)` | Alpha | 15 | Secondary phone |
| 13 | CUST-SSN | `9(09)` | Numeric | 9 | Social Security Number (PII) |
| 14 | CUST-GOVT-ISSUED-ID | `X(20)` | Alpha | 20 | Government-issued ID |
| 15 | CUST-DOB-YYYYMMDD | `X(10)` | Date | 10 | Date of birth |
| 16 | CUST-EFT-ACCOUNT-ID | `X(10)` | Alpha | 10 | EFT (electronic funds transfer) account |
| 17 | CUST-PRI-CARD-HOLDER-IND | `X(01)` | Alpha | 1 | Primary cardholder indicator |
| 18 | CUST-FICO-CREDIT-SCORE | `9(03)` | Numeric | 3 | FICO credit score |
| 19 | FILLER | `X(168)` | -- | 168 | Reserved space |

**Business Rules:**
- SSN and DOB are PII — critical for data privacy in modernization
- FICO score drives credit decisions
- One customer can have multiple accounts/cards (via cross-reference)

---

### 5. Transaction (CVTRA05Y)

**VSAM File:** `TRANSACT.VSAM.KSDS` | **Record Length:** 350 bytes | **Key:** Transaction ID (16 chars)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|---|
| 1 | TRAN-ID | `X(16)` | Alpha | 16 | Unique transaction identifier |
| 2 | TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| 3 | TRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 4 | TRAN-SOURCE | `X(10)` | Alpha | 10 | Transaction source (POS, ATM, Online, etc.) |
| 5 | TRAN-DESC | `X(100)` | Alpha | 100 | Transaction description |
| 6 | TRAN-AMT | `S9(09)V99` | Decimal | 11 | Transaction amount |
| 7 | TRAN-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant identifier |
| 8 | TRAN-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| 9 | TRAN-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| 10 | TRAN-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP code |
| 11 | TRAN-CARD-NUM | `X(16)` | Alpha | 16 | Card number used |
| 12 | TRAN-ORIG-TS | `X(26)` | Timestamp | 26 | Original transaction timestamp |
| 13 | TRAN-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | `X(20)` | -- | 20 | Reserved space |

**Business Rules:**
- Central transaction record — the heart of the system
- TRAN-TYPE-CD links to Transaction Type (CVTRA03Y)
- TRAN-CAT-CD links to Transaction Category (CVTRA04Y)
- TRAN-CARD-NUM links to Card Cross-Reference for account resolution
- Has alternate index on Card Number for card-based lookups

---

### 6. Daily Transaction (CVTRA06Y)

**VSAM File:** `DALYTRAN.PS` (sequential) | **Record Length:** 350 bytes

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|---|
| 1 | DALYTRAN-ID | `X(16)` | Alpha | 16 | Daily transaction identifier |
| 2 | DALYTRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| 3 | DALYTRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 4 | DALYTRAN-SOURCE | `X(10)` | Alpha | 10 | Transaction source |
| 5 | DALYTRAN-DESC | `X(100)` | Alpha | 100 | Transaction description |
| 6 | DALYTRAN-AMT | `S9(09)V99` | Decimal | 11 | Transaction amount |
| 7 | DALYTRAN-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant identifier |
| 8 | DALYTRAN-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| 9 | DALYTRAN-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| 10 | DALYTRAN-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP code |
| 11 | DALYTRAN-CARD-NUM | `X(16)` | Alpha | 16 | Card number used |
| 12 | DALYTRAN-ORIG-TS | `X(26)` | Timestamp | 26 | Original timestamp |
| 13 | DALYTRAN-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | `X(20)` | -- | 20 | Reserved space |

**Business Rules:**
- Identical layout to CVTRA05Y but for daily (unposted) transactions
- Batch job POSTTRAN (CBTRN02C) posts these to the master TRANSACT file
- After posting, rejected transactions go to DALYREJS GDG

---

### 7. Transaction — Statement Layout (COSTM01)

**Used by:** CBSTM03A/B (statement generation) | **Record Length:** 350 bytes | **Key:** Card Number + Transaction ID (32 chars)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|---|
| 1 | TRNX-CARD-NUM | `X(16)` | Alpha | 16 | Card number (key part 1) |
| 2 | TRNX-ID | `X(16)` | Alpha | 16 | Transaction ID (key part 2) |
| 3 | TRNX-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type |
| 4 | TRNX-CAT-CD | `9(04)` | Numeric | 4 | Transaction category |
| 5 | TRNX-SOURCE | `X(10)` | Alpha | 10 | Transaction source |
| 6 | TRNX-DESC | `X(100)` | Alpha | 100 | Description |
| 7 | TRNX-AMT | `S9(09)V99` | Decimal | 11 | Amount |
| 8 | TRNX-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant ID |
| 9 | TRNX-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| 10 | TRNX-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| 11 | TRNX-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP |
| 12 | TRNX-ORIG-TS | `X(26)` | Timestamp | 26 | Original timestamp |
| 13 | TRNX-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | `X(20)` | -- | 20 | Reserved |

**Business Rules:**
- Re-keyed version of the transaction record with Card Number as the leading key
- Created by CREASTMT JCL (SORT step) to enable card-based sequential processing
- Used exclusively by the statement generation batch process

---

### 8. User Security (CSUSR01Y)

**VSAM File:** `USRSEC.VSAM.KSDS` | **Record Length:** 80 bytes | **Key:** User ID (8 chars)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|---|
| 1 | SEC-USR-ID | `X(08)` | Alpha | 8 | User login ID |
| 2 | SEC-USR-FNAME | `X(20)` | Alpha | 20 | User first name |
| 3 | SEC-USR-LNAME | `X(20)` | Alpha | 20 | User last name |
| 4 | SEC-USR-PWD | `X(08)` | Alpha | 8 | Password (plaintext — security risk) |
| 5 | SEC-USR-TYPE | `X(01)` | Alpha | 1 | User type: A=Admin, U=Regular |
| 6 | FILLER | `X(23)` | -- | 23 | Reserved space |

**Business Rules:**
- Passwords stored in plaintext — major modernization concern
- Two user types control menu access (Admin sees user management options)
- Default accounts: ADMIN001/PASSWORD (Admin), USER0001/PASSWORD (Regular)

---

### 9. Transaction Category Balance (CVTRA01Y)

**VSAM File:** `TCATBALF.VSAM.KSDS` | **Record Length:** 50 bytes | **Key:** Account ID + Type Code + Category Code

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|---|
| 1 | TRANCAT-ACCT-ID | `9(11)` | Numeric | 11 | Account ID |
| 2 | TRANCAT-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| 3 | TRANCAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 4 | TRAN-CAT-BAL | `S9(09)V99` | Decimal | 11 | Category balance |
| 5 | FILLER | `X(22)` | -- | 22 | Reserved |

**Business Rules:**
- Tracks balance per account per transaction type/category
- Updated by transaction posting (CBTRN02C) and interest calculation (CBACT04C)
- Composite key enables granular balance tracking

---

### 10. Disclosure Group (CVTRA02Y)

**VSAM File:** `DISCGRP.VSAM.KSDS` | **Record Length:** 50 bytes | **Key:** Group ID + Type + Category

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|---|
| 1 | DIS-ACCT-GROUP-ID | `X(10)` | Alpha | 10 | Account disclosure group ID |
| 2 | DIS-TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type |
| 3 | DIS-TRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category |
| 4 | DIS-INT-RATE | `S9(04)V99` | Decimal | 6 | Interest rate (%) |
| 5 | FILLER | `X(28)` | -- | 28 | Reserved |

**Business Rules:**
- Maps account groups to interest rates per transaction type/category
- Used by interest calculation batch (CBACT04C)
- Account's ACCT-GROUP-ID links to DIS-ACCT-GROUP-ID

---

### 11. Transaction Type (CVTRA03Y)

**VSAM File:** `TRANTYPE.VSAM.KSDS` | **Record Length:** 60 bytes | **Key:** Type Code (2 chars)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|---|
| 1 | TRAN-TYPE | `X(02)` | Alpha | 2 | Transaction type code |
| 2 | TRAN-TYPE-DESC | `X(50)` | Alpha | 50 | Type description |
| 3 | FILLER | `X(08)` | -- | 8 | Reserved |

**Business Rules:**
- Reference/lookup table for transaction type codes
- Used by reporting programs to decode type codes into descriptions

---

### 12. Transaction Category (CVTRA04Y)

**VSAM File:** `TRANCATG.VSAM.KSDS` | **Record Length:** 60 bytes | **Key:** Type Code + Category Code

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|---|
| 1 | TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| 2 | TRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 3 | TRAN-CAT-TYPE-DESC | `X(50)` | Alpha | 50 | Category description |
| 4 | FILLER | `X(04)` | -- | 4 | Reserved |

**Business Rules:**
- Sub-classification within transaction types
- Composite key (type + category) provides two-level classification
- Used by reporting and interest calculation

---

## Communication & Navigation Structures

### COMMAREA (COCOM01Y)

The CICS communication area passed between all online programs via EXEC CICS XCTL/RETURN.

| Field | PIC Clause | Description |
|---|---|---|
| CDEMO-FROM-TRANID | `X(04)` | Source transaction ID |
| CDEMO-FROM-PROGRAM | `X(08)` | Source program name |
| CDEMO-TO-TRANID | `X(04)` | Target transaction ID |
| CDEMO-TO-PROGRAM | `X(08)` | Target program name |
| CDEMO-USER-ID | `X(08)` | Logged-in user ID |
| CDEMO-USER-TYPE | `X(01)` | User type (A=Admin, U=User) |
| CDEMO-PGM-CONTEXT | `9(01)` | Program context (0=Enter, 1=Reenter) |
| CDEMO-CUST-ID | `9(09)` | Current customer ID |
| CDEMO-CUST-FNAME | `X(25)` | Customer first name |
| CDEMO-CUST-MNAME | `X(25)` | Customer middle name |
| CDEMO-CUST-LNAME | `X(25)` | Customer last name |
| CDEMO-ACCT-ID | `9(11)` | Current account ID |
| CDEMO-ACCT-STATUS | `X(01)` | Account status |
| CDEMO-CARD-NUM | `9(16)` | Current card number |
| CDEMO-LAST-MAP | `X(07)` | Last BMS map displayed |
| CDEMO-LAST-MAPSET | `X(07)` | Last BMS mapset used |

### Main Menu Options (COMEN02Y)

Defines 11 menu options for regular users. Each option maps to a COBOL program:

| Option # | Menu Label | Target Program | User Type |
|---|---|---|---|
| 1 | Account View | COACTVWC | U |
| 2 | Account Update | COACTUPC | U |
| 3 | Credit Card List | COCRDLIC | U |
| 4 | Credit Card View | COCRDSLC | U |
| 5 | Credit Card Update | COCRDUPC | U |
| 6 | Transaction List | COTRN00C | U |
| 7 | Transaction View | COTRN01C | U |
| 8 | Transaction Add | COTRN02C | U |
| 9 | Transaction Reports | CORPT00C | U |
| 10 | Bill Payment | COBIL00C | U |
| 11 | Pending Authorization View | COPAUS0C | U |

### Admin Menu Options (COADM02Y)

Defines 6 menu options for admin users:

| Option # | Menu Label | Target Program |
|---|---|---|
| 1 | User List (Security) | COUSR00C |
| 2 | User Add (Security) | COUSR01C |
| 3 | User Update (Security) | COUSR02C |
| 4 | User Delete (Security) | COUSR03C |
| 5 | Transaction Type List/Update (DB2) | COTRTLIC |
| 6 | Transaction Type Maintenance (DB2) | COTRTUPC |

---

## Presentation / UI Structures

| Copybook | Description | Used By |
|---|---|---|
| COTTL01Y | Screen titles: "AWS Mainframe Modernization" / "CardDemo" / thank-you message | All online programs |
| CSDAT01Y | Date/time formatting: YYYYMMDD, MM/DD/YY, HH:MM:SS, full timestamp | All online programs |
| CSMSG01Y | Message line 1 — single-line error/info messages (78 chars) | All online programs |
| CSMSG02Y | Message line 2 — extended messages | Most online programs |
| CODATECN | Date conversion record — input/output formats for COBDATFT assembler call | CBACT01C |
| CVTRA07Y | Report layout — headers, detail lines, page/account/grand totals for daily transaction report | CBTRN03C |
| CUSTREC | Alternate customer record layout for statement programs | CBSTM03A |
| CVEXPORT | Multi-file export record structures for CBEXPORT/CBIMPORT | CBEXPORT, CBIMPORT |

---

## Utility Structures

| Copybook | Description | Used By |
|---|---|---|
| CSLKPCDY | Lookup code repository: North America phone area codes, US state codes, state+ZIP prefix validation | COACTUPC |
| CSSETATY | Set field attribute utility — used with REPLACING to set BMS field attributes dynamically | COACTUPC |
| CSSTRPFY | String strip/pad function utility | COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| CSUTLDPY | Date utility parameters for CSUTLDTC subroutine | COACTUPC |
| CSUTLDWY | Date utility working storage for CSUTLDTC subroutine | COACTUPC |
| CVCRD01Y | Card detail display structure (formatted for screen output) | Online card programs |
| UNUSED1Y | Placeholder/unused record (80 bytes) — same layout as user security | Not actively used |

---

## VSAM File Summary

| Logical File | Dataset Name | Org | Key | RECL | Business Entity | Primary Programs |
|---|---|---|---|---|---|---|
| USRSEC | USRSEC.VSAM.KSDS | KSDS | User ID (8) | 80 | User Security | COSGN00C, COUSR00C–03C |
| ACCTDATA | ACCTDATA.VSAM.KSDS | KSDS | Account ID (11) | 300 | Account | COACTVWC, COACTUPC, CBACT04C |
| CARDDATA | CARDDATA.VSAM.KSDS | KSDS | Card Number (16) | 150 | Card | COCRDLIC, COCRDSLC, COCRDUPC |
| CUSTDATA | CUSTDATA.VSAM.KSDS | KSDS | Customer ID (9) | 500 | Customer | COACTVWC, CBSTM03A |
| CARDXREF | CARDXREF.VSAM.KSDS | KSDS | Card Number (16) | 50 | Cross-Reference | COBIL00C, COTRN02C, CBTRN02C |
| TRANSACT | TRANSACT.VSAM.KSDS | KSDS | Transaction ID (16) | 350 | Transaction | COTRN00C–02C, CBTRN02C |
| DALYTRAN | DALYTRAN.PS | SEQ | -- | 350 | Daily Transaction | CBTRN02C (POSTTRAN) |
| TCATBALF | TCATBALF.VSAM.KSDS | KSDS | Acct+Type+Cat (17) | 50 | Category Balance | CBTRN02C, CBACT04C |
| DISCGRP | DISCGRP.VSAM.KSDS | KSDS | Group+Type+Cat (16) | 50 | Disclosure Group | CBACT04C |
| TRANTYPE | TRANTYPE.VSAM.KSDS | KSDS | Type Code (2) | 60 | Transaction Type | CBTRN03C |
| TRANCATG | TRANCATG.VSAM.KSDS | KSDS | Type+Cat (6) | 60 | Transaction Category | CBTRN03C |

---

## Data Type Reference

| COBOL PIC Clause | Java Equivalent | SQL Equivalent | Description |
|---|---|---|---|
| `PIC X(n)` | `String` | `VARCHAR(n)` / `CHAR(n)` | Alphanumeric, n characters |
| `PIC 9(n)` | `long` / `int` | `NUMERIC(n)` / `BIGINT` | Unsigned integer, n digits |
| `PIC S9(n)V99` | `BigDecimal` | `DECIMAL(n+2, 2)` | Signed decimal with 2 places |
| `PIC S9(n) COMP` | `int` / `long` | `INTEGER` / `BIGINT` | Binary integer |
| `PIC 9(n) COMP` | `int` / `long` | `INTEGER` / `BIGINT` | Unsigned binary integer |
| `PIC X(10)` (date) | `LocalDate` | `DATE` | Date as YYYY-MM-DD string |
| `PIC X(26)` (timestamp) | `LocalDateTime` | `TIMESTAMP` | ISO-style timestamp |
| `PIC X(01)` (flag) | `boolean` / `enum` | `CHAR(1)` | Status flag (Y/N, A/U) |
