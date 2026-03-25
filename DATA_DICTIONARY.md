# DATA DICTIONARY - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Source:** Copybook PIC clause analysis across `app/cpy/`
> **Purpose:** Business-friendly reference for all data entities, their fields, types, and sizes

---

## Table of Contents

1. [Account Entity](#1-account-entity)
2. [Card Entity](#2-card-entity)
3. [Card Cross-Reference Entity](#3-card-cross-reference-entity)
4. [Customer Entity](#4-customer-entity)
5. [Transaction Entity (Online)](#5-transaction-entity-online)
6. [Daily Transaction Entity](#6-daily-transaction-entity)
7. [Transaction Category Balance Entity](#7-transaction-category-balance-entity)
8. [Disclosure Group Entity](#8-disclosure-group-entity)
9. [Transaction Type Entity](#9-transaction-type-entity)
10. [Transaction Category Type Entity](#10-transaction-category-type-entity)
11. [User Security Entity](#11-user-security-entity)
12. [Statement Transaction Entity](#12-statement-transaction-entity)
13. [Transaction Report Structures](#13-transaction-report-structures)
14. [Export Record Layout](#14-export-record-layout)
15. [Application COMMAREA](#15-application-commarea)
16. [COBOL-to-Java Type Mapping Guide](#16-cobol-to-java-type-mapping-guide)

---

## 1. Account Entity

**Copybook:** `CVACT01Y.cpy` | **Record Length:** 300 bytes | **VSAM File:** `ACCTDAT` (KSDS)

| # | Field Name | PIC Clause | Type | Size | Business Meaning | Java Equivalent |
|---|------------|-----------|------|-----:|-----------------|-----------------|
| 1 | `ACCT-ID` | `9(11)` | Numeric | 11 | Account identifier (primary key) | `long` |
| 2 | `ACCT-ACTIVE-STATUS` | `X(01)` | Alpha | 1 | Active status flag (Y/N) | `String` |
| 3 | `ACCT-CURR-BAL` | `S9(10)V99` | Signed Decimal | 12 | Current account balance | `BigDecimal` |
| 4 | `ACCT-CREDIT-LIMIT` | `S9(10)V99` | Signed Decimal | 12 | Credit limit | `BigDecimal` |
| 5 | `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | Signed Decimal | 12 | Cash advance credit limit | `BigDecimal` |
| 6 | `ACCT-OPEN-DATE` | `X(10)` | Alphanumeric | 10 | Account open date (YYYY-MM-DD) | `LocalDate` |
| 7 | `ACCT-EXPIRAION-DATE` | `X(10)` | Alphanumeric | 10 | Account expiration date | `LocalDate` |
| 8 | `ACCT-REISSUE-DATE` | `X(10)` | Alphanumeric | 10 | Card reissue date | `LocalDate` |
| 9 | `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | Signed Decimal | 12 | Current cycle credit total | `BigDecimal` |
| 10 | `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | Signed Decimal | 12 | Current cycle debit total | `BigDecimal` |
| 11 | `ACCT-ADDR-ZIP` | `X(10)` | Alphanumeric | 10 | Account holder ZIP code | `String` |
| 12 | `ACCT-GROUP-ID` | `X(10)` | Alphanumeric | 10 | Disclosure group ID | `String` |
| 13 | `FILLER` | `X(178)` | Filler | 178 | Reserved/unused space | -- |

**Key:** `ACCT-ID` (11-digit numeric)

### Business Rules
- Balance can be negative (signed field)
- Active status is binary: 'Y' = active, 'N' = inactive
- Credit limits are always positive but stored as signed for calculation compatibility
- Dates stored as character strings in `YYYY-MM-DD` format

---

## 2. Card Entity

**Copybook:** `CVACT02Y.cpy` | **Record Length:** 150 bytes | **VSAM File:** `CARDDAT` (KSDS)

| # | Field Name | PIC Clause | Type | Size | Business Meaning | Java Equivalent |
|---|------------|-----------|------|-----:|-----------------|-----------------|
| 1 | `CARD-NUM` | `X(16)` | Alphanumeric | 16 | Credit card number (primary key) | `String` |
| 2 | `CARD-ACCT-ID` | `9(11)` | Numeric | 11 | Parent account ID (foreign key) | `long` |
| 3 | `CARD-CVV-CD` | `9(03)` | Numeric | 3 | CVV security code | `String` |
| 4 | `CARD-EMBOSSED-NAME` | `X(50)` | Alphanumeric | 50 | Name embossed on card | `String` |
| 5 | `CARD-EXPIRAION-DATE` | `X(10)` | Alphanumeric | 10 | Card expiration date (YYYY-MM-DD) | `LocalDate` |
| 6 | `CARD-ACTIVE-STATUS` | `X(01)` | Alpha | 1 | Card active status (Y/N) | `String` |
| 7 | `FILLER` | `X(59)` | Filler | 59 | Reserved space | -- |

**Key:** `CARD-NUM` (16-character card number)
**Alternate Index:** By `CARD-ACCT-ID` (via `CARDAIX` path)

### Business Rules
- One account can have multiple cards (1:N relationship)
- CVV is stored as numeric, must be zero-padded to 3 digits
- Card number is the primary key for all card lookups
- Account-based lookups use the alternate index path `CARDAIX`

---

## 3. Card Cross-Reference Entity

**Copybook:** `CVACT03Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `CARDXREF` (KSDS)

| # | Field Name | PIC Clause | Type | Size | Business Meaning | Java Equivalent |
|---|------------|-----------|------|-----:|-----------------|-----------------|
| 1 | `XREF-CARD-NUM` | `X(16)` | Alphanumeric | 16 | Card number (primary key) | `String` |
| 2 | `XREF-CUST-ID` | `9(09)` | Numeric | 9 | Customer ID (foreign key) | `long` |
| 3 | `XREF-ACCT-ID` | `9(11)` | Numeric | 11 | Account ID (foreign key) | `long` |
| 4 | `FILLER` | `X(14)` | Filler | 14 | Reserved space | -- |

**Key:** `XREF-CARD-NUM` (16-character)

### Business Rules
- Central lookup table linking cards to customers and accounts
- Enables navigation: Card -> Customer and Card -> Account
- Critical for transaction processing (card-based lookups resolve to account)

---

## 4. Customer Entity

**Copybook:** `CVCUS01Y.cpy` | **Record Length:** 500 bytes | **VSAM File:** `CUSTDAT` (KSDS)

| # | Field Name | PIC Clause | Type | Size | Business Meaning | Java Equivalent |
|---|------------|-----------|------|-----:|-----------------|-----------------|
| 1 | `CUST-ID` | `9(09)` | Numeric | 9 | Customer ID (primary key) | `long` |
| 2 | `CUST-FIRST-NAME` | `X(25)` | Alphanumeric | 25 | First name | `String` |
| 3 | `CUST-MIDDLE-NAME` | `X(25)` | Alphanumeric | 25 | Middle name | `String` |
| 4 | `CUST-LAST-NAME` | `X(25)` | Alphanumeric | 25 | Last name | `String` |
| 5 | `CUST-ADDR-LINE-1` | `X(50)` | Alphanumeric | 50 | Address line 1 | `String` |
| 6 | `CUST-ADDR-LINE-2` | `X(50)` | Alphanumeric | 50 | Address line 2 | `String` |
| 7 | `CUST-ADDR-LINE-3` | `X(50)` | Alphanumeric | 50 | Address line 3 | `String` |
| 8 | `CUST-ADDR-STATE-CD` | `X(02)` | Alpha | 2 | State code (e.g., NY) | `String` |
| 9 | `CUST-ADDR-COUNTRY-CD` | `X(03)` | Alpha | 3 | Country code (e.g., USA) | `String` |
| 10 | `CUST-ADDR-ZIP` | `X(10)` | Alphanumeric | 10 | ZIP/postal code | `String` |
| 11 | `CUST-PHONE-NUM-1` | `X(15)` | Alphanumeric | 15 | Primary phone | `String` |
| 12 | `CUST-PHONE-NUM-2` | `X(15)` | Alphanumeric | 15 | Secondary phone | `String` |
| 13 | `CUST-SSN` | `9(09)` | Numeric | 9 | Social Security Number | `String` (masked) |
| 14 | `CUST-GOVT-ISSUED-ID` | `X(20)` | Alphanumeric | 20 | Government-issued ID | `String` |
| 15 | `CUST-DOB-YYYYMMDD` | `X(10)` | Alphanumeric | 10 | Date of birth | `LocalDate` |
| 16 | `CUST-EFT-ACCOUNT-ID` | `X(10)` | Alphanumeric | 10 | EFT account for payments | `String` |
| 17 | `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | Alpha | 1 | Primary cardholder indicator (Y/N) | `boolean` |
| 18 | `CUST-FICO-CREDIT-SCORE` | `9(03)` | Numeric | 3 | FICO credit score (300-850) | `int` |
| 19 | `FILLER` | `X(168)` | Filler | 168 | Reserved space | -- |

**Key:** `CUST-ID` (9-digit numeric)

### Business Rules
- SSN must follow US format validation (parts 1-3, exclusion of 000, 666, 900-999)
- FICO score valid range: 300-850
- Phone numbers stored in format `(NNN)NNN-NNNN` with formatting characters
- Date of birth used for age verification and account eligibility
- Primary cardholder indicator distinguishes authorized users from primary holders

---

## 5. Transaction Entity (Online)

**Copybook:** `CVTRA05Y.cpy` | **Record Length:** 350 bytes | **VSAM File:** `TRANSACT` (KSDS)

| # | Field Name | PIC Clause | Type | Size | Business Meaning | Java Equivalent |
|---|------------|-----------|------|-----:|-----------------|-----------------|
| 1 | `TRAN-ID` | `X(16)` | Alphanumeric | 16 | Transaction ID (primary key) | `String` |
| 2 | `TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type code | `String` |
| 3 | `TRAN-CAT-CD` | `9(04)` | Numeric | 4 | Transaction category code | `int` |
| 4 | `TRAN-SOURCE` | `X(10)` | Alphanumeric | 10 | Transaction source (POS, ATM, etc.) | `String` |
| 5 | `TRAN-DESC` | `X(100)` | Alphanumeric | 100 | Transaction description | `String` |
| 6 | `TRAN-AMT` | `S9(09)V99` | Signed Decimal | 11 | Transaction amount | `BigDecimal` |
| 7 | `TRAN-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant identifier | `long` |
| 8 | `TRAN-MERCHANT-NAME` | `X(50)` | Alphanumeric | 50 | Merchant name | `String` |
| 9 | `TRAN-MERCHANT-CITY` | `X(50)` | Alphanumeric | 50 | Merchant city | `String` |
| 10 | `TRAN-MERCHANT-ZIP` | `X(10)` | Alphanumeric | 10 | Merchant ZIP code | `String` |
| 11 | `TRAN-CARD-NUM` | `X(16)` | Alphanumeric | 16 | Card number used | `String` |
| 12 | `TRAN-ORIG-TS` | `X(26)` | Alphanumeric | 26 | Original timestamp | `LocalDateTime` |
| 13 | `TRAN-PROC-TS` | `X(26)` | Alphanumeric | 26 | Processing timestamp | `LocalDateTime` |
| 14 | `FILLER` | `X(20)` | Filler | 20 | Reserved space | -- |

**Key:** `TRAN-ID` (16-character transaction ID)
**Alternate Index:** By `TRAN-CARD-NUM` (for card-based lookups)

### Business Rules
- Amount is signed: positive = debit/purchase, negative = credit/refund
- Transaction type codes: '01'=Purchase, '02'=Return, '03'=Cash Advance, etc.
- Timestamps stored as character in ISO-like format
- Card number links to Card entity for account resolution

---

## 6. Daily Transaction Entity

**Copybook:** `CVTRA06Y.cpy` | **Record Length:** 350 bytes | **VSAM File:** Daily transaction staging

| # | Field Name | PIC Clause | Type | Size | Business Meaning | Java Equivalent |
|---|------------|-----------|------|-----:|-----------------|-----------------|
| 1 | `DALYTRAN-ID` | `X(16)` | Alphanumeric | 16 | Daily transaction ID | `String` |
| 2 | `DALYTRAN-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type code | `String` |
| 3 | `DALYTRAN-CAT-CD` | `9(04)` | Numeric | 4 | Category code | `int` |
| 4 | `DALYTRAN-SOURCE` | `X(10)` | Alphanumeric | 10 | Source system | `String` |
| 5 | `DALYTRAN-DESC` | `X(100)` | Alphanumeric | 100 | Description | `String` |
| 6 | `DALYTRAN-AMT` | `S9(09)V99` | Signed Decimal | 11 | Amount | `BigDecimal` |
| 7 | `DALYTRAN-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant ID | `long` |
| 8 | `DALYTRAN-MERCHANT-NAME` | `X(50)` | Alphanumeric | 50 | Merchant name | `String` |
| 9 | `DALYTRAN-MERCHANT-CITY` | `X(50)` | Alphanumeric | 50 | Merchant city | `String` |
| 10 | `DALYTRAN-MERCHANT-ZIP` | `X(10)` | Alphanumeric | 10 | Merchant ZIP | `String` |
| 11 | `DALYTRAN-CARD-NUM` | `X(16)` | Alphanumeric | 16 | Card number | `String` |
| 12 | `DALYTRAN-ORIG-TS` | `X(26)` | Alphanumeric | 26 | Original timestamp | `LocalDateTime` |
| 13 | `DALYTRAN-PROC-TS` | `X(26)` | Alphanumeric | 26 | Processing timestamp | `LocalDateTime` |
| 14 | `FILLER` | `X(20)` | Filler | 20 | Reserved | -- |

**Purpose:** Staging area for daily incoming transactions before they are posted to the master TRANSACT file by the POSTTRAN batch job.

---

## 7. Transaction Category Balance Entity

**Copybook:** `CVTRA01Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `TCATBALF` (KSDS)

| # | Field Name | PIC Clause | Type | Size | Business Meaning | Java Equivalent |
|---|------------|-----------|------|-----:|-----------------|-----------------|
| 1 | `TRANCAT-ACCT-ID` | `9(11)` | Numeric | 11 | Account ID (compound key part 1) | `long` |
| 2 | `TRANCAT-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type (compound key part 2) | `String` |
| 3 | `TRANCAT-CD` | `9(04)` | Numeric | 4 | Category code (compound key part 3) | `int` |
| 4 | `TRAN-CAT-BAL` | `S9(09)V99` | Signed Decimal | 11 | Running balance for this category | `BigDecimal` |
| 5 | `FILLER` | `X(22)` | Filler | 22 | Reserved | -- |

**Key:** Composite (`TRANCAT-ACCT-ID` + `TRANCAT-TYPE-CD` + `TRANCAT-CD`)

### Business Rules
- Tracks running balance per account per transaction type per category
- Updated by CBTRN02C during daily transaction posting
- Used for interest calculation by CBACT04C

---

## 8. Disclosure Group Entity

**Copybook:** `CVTRA02Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `DISCGRP` (KSDS)

| # | Field Name | PIC Clause | Type | Size | Business Meaning | Java Equivalent |
|---|------------|-----------|------|-----:|-----------------|-----------------|
| 1 | `DIS-ACCT-GROUP-ID` | `X(10)` | Alphanumeric | 10 | Disclosure group ID (compound key part 1) | `String` |
| 2 | `DIS-TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type (compound key part 2) | `String` |
| 3 | `DIS-TRAN-CAT-CD` | `9(04)` | Numeric | 4 | Category code (compound key part 3) | `int` |
| 4 | `DIS-INT-RATE` | `S9(04)V99` | Signed Decimal | 6 | Interest rate for this group/type/category | `BigDecimal` |
| 5 | `FILLER` | `X(28)` | Filler | 28 | Reserved | -- |

**Key:** Composite (`DIS-ACCT-GROUP-ID` + `DIS-TRAN-TYPE-CD` + `DIS-TRAN-CAT-CD`)

### Business Rules
- Maps account groups to interest rates by transaction type/category
- Used by CBACT04C (interest calculation) to look up the applicable rate
- Account's `ACCT-GROUP-ID` links to this entity's `DIS-ACCT-GROUP-ID`

---

## 9. Transaction Type Entity

**Copybook:** `CVTRA03Y.cpy` | **Record Length:** 60 bytes | **VSAM File:** `TRANTYPE` (KSDS)

| # | Field Name | PIC Clause | Type | Size | Business Meaning | Java Equivalent |
|---|------------|-----------|------|-----:|-----------------|-----------------|
| 1 | `TRAN-TYPE` | `X(02)` | Alpha | 2 | Transaction type code (primary key) | `String` |
| 2 | `TRAN-TYPE-DESC` | `X(50)` | Alphanumeric | 50 | Type description | `String` |
| 3 | `FILLER` | `X(08)` | Filler | 8 | Reserved | -- |

**Key:** `TRAN-TYPE` (2-character code)

### Business Rules
- Lookup/reference table for transaction type descriptions
- Common types: '01'=Purchase, '02'=Return, '03'=Cash Advance, '04'=Payment

---

## 10. Transaction Category Type Entity

**Copybook:** `CVTRA04Y.cpy` | **Record Length:** 60 bytes | **VSAM File:** `TRANCATG` (KSDS)

| # | Field Name | PIC Clause | Type | Size | Business Meaning | Java Equivalent |
|---|------------|-----------|------|-----:|-----------------|-----------------|
| 1 | `TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type (compound key part 1) | `String` |
| 2 | `TRAN-CAT-CD` | `9(04)` | Numeric | 4 | Category code (compound key part 2) | `int` |
| 3 | `TRAN-CAT-TYPE-DESC` | `X(50)` | Alphanumeric | 50 | Category description | `String` |
| 4 | `FILLER` | `X(04)` | Filler | 4 | Reserved | -- |

**Key:** Composite (`TRAN-TYPE-CD` + `TRAN-CAT-CD`)

### Business Rules
- Sub-classification of transaction types into categories
- Example: Type '01' (Purchase) -> Category 5001 (Retail), 5002 (Online), etc.

---

## 11. User Security Entity

**Copybook:** `CSUSR01Y.cpy` | **Record Length:** 80 bytes | **VSAM File:** `USRSEC` (KSDS)

| # | Field Name | PIC Clause | Type | Size | Business Meaning | Java Equivalent |
|---|------------|-----------|------|-----:|-----------------|-----------------|
| 1 | `SEC-USR-ID` | `X(08)` | Alphanumeric | 8 | User ID (primary key) | `String` |
| 2 | `SEC-USR-FNAME` | `X(20)` | Alphanumeric | 20 | First name | `String` |
| 3 | `SEC-USR-LNAME` | `X(20)` | Alphanumeric | 20 | Last name | `String` |
| 4 | `SEC-USR-PWD` | `X(08)` | Alphanumeric | 8 | Password (plaintext) | `String` (hash) |
| 5 | `SEC-USR-TYPE` | `X(01)` | Alpha | 1 | User type: 'A'=Admin, 'U'=Regular | `enum` |
| 6 | `FILLER` | `X(23)` | Filler | 23 | Reserved | -- |

**Key:** `SEC-USR-ID` (8-character user ID)

### Business Rules
- Two user types: Admin ('A') and Regular ('U')
- Admin users access the admin menu (COADM01C) with user management
- Regular users access the main menu (COMEN01C) with card operations
- **Security Note:** Passwords stored in plaintext -- must be hashed in Java migration
- Default credentials: ADMIN001/PASSWORD, USER0001/PASSWORD

---

## 12. Statement Transaction Entity

**Copybook:** `COSTM01.CPY` | **Record Length:** 350 bytes

| # | Field Name | PIC Clause | Type | Size | Business Meaning | Java Equivalent |
|---|------------|-----------|------|-----:|-----------------|-----------------|
| 1 | `TRNX-CARD-NUM` | `X(16)` | Alphanumeric | 16 | Card number (compound key part 1) | `String` |
| 2 | `TRNX-ID` | `X(16)` | Alphanumeric | 16 | Transaction ID (compound key part 2) | `String` |
| 3 | `TRNX-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type | `String` |
| 4 | `TRNX-CAT-CD` | `9(04)` | Numeric | 4 | Category code | `int` |
| 5 | `TRNX-SOURCE` | `X(10)` | Alphanumeric | 10 | Source | `String` |
| 6 | `TRNX-DESC` | `X(100)` | Alphanumeric | 100 | Description | `String` |
| 7 | `TRNX-AMT` | `S9(09)V99` | Signed Decimal | 11 | Amount | `BigDecimal` |
| 8 | `TRNX-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant ID | `long` |
| 9 | `TRNX-MERCHANT-NAME` | `X(50)` | Alphanumeric | 50 | Merchant name | `String` |
| 10 | `TRNX-MERCHANT-CITY` | `X(50)` | Alphanumeric | 50 | Merchant city | `String` |
| 11 | `TRNX-MERCHANT-ZIP` | `X(10)` | Alphanumeric | 10 | Merchant ZIP | `String` |
| 12 | `TRNX-ORIG-TS` | `X(26)` | Alphanumeric | 26 | Original timestamp | `LocalDateTime` |
| 13 | `TRNX-PROC-TS` | `X(26)` | Alphanumeric | 26 | Processing timestamp | `LocalDateTime` |
| 14 | `FILLER` | `X(20)` | Filler | 20 | Reserved | -- |

**Key:** Composite (`TRNX-CARD-NUM` + `TRNX-ID`) -- re-keyed for statement generation
**Purpose:** Same data as CVTRA05Y but keyed by card number first, used by CBSTM03A for statement generation.

---

## 13. Transaction Report Structures

**Copybook:** `CVTRA07Y.cpy` | **Purpose:** Report formatting (not persisted)

### Report Header
| Field | PIC Clause | Size | Content |
|-------|-----------|-----:|---------|
| `REPT-SHORT-NAME` | `X(38)` | 38 | 'DALYREPT' |
| `REPT-LONG-NAME` | `X(41)` | 41 | 'Daily Transaction Report' |
| `REPT-DATE-HEADER` | `X(12)` | 12 | 'Date Range: ' |
| `REPT-START-DATE` | `X(10)` | 10 | Start date |
| `REPT-END-DATE` | `X(10)` | 10 | End date |

### Transaction Detail Line
| Field | PIC Clause | Size | Content |
|-------|-----------|-----:|---------|
| `TRAN-REPORT-TRANS-ID` | `X(16)` | 16 | Transaction ID |
| `TRAN-REPORT-ACCOUNT-ID` | `X(11)` | 11 | Account ID |
| `TRAN-REPORT-TYPE-CD` | `X(02)` | 2 | Type code |
| `TRAN-REPORT-TYPE-DESC` | `X(15)` | 15 | Type description |
| `TRAN-REPORT-CAT-CD` | `9(04)` | 4 | Category code |
| `TRAN-REPORT-CAT-DESC` | `X(29)` | 29 | Category description |
| `TRAN-REPORT-SOURCE` | `X(10)` | 10 | Source |
| `TRAN-REPORT-AMT` | `-ZZZ,ZZZ,ZZZ.ZZ` | 15 | Formatted amount |

### Report Totals
| Field | PIC Clause | Content |
|-------|-----------|---------|
| `REPT-PAGE-TOTAL` | `+ZZZ,ZZZ,ZZZ.ZZ` | Page subtotal |
| `REPT-ACCOUNT-TOTAL` | `+ZZZ,ZZZ,ZZZ.ZZ` | Account subtotal |
| `REPT-GRAND-TOTAL` | `+ZZZ,ZZZ,ZZZ.ZZ` | Grand total |

---

## 14. Export Record Layout

**Copybook:** `CVEXPORT.cpy` | **Purpose:** Multi-entity export format

### Account Export Fields
| Field | PIC Clause | Size |
|-------|-----------|-----:|
| `EXP-ACCT-ID` | `9(11)` | 11 |
| `EXP-ACCT-ACTIVE-STATUS` | `X(01)` | 1 |
| `EXP-ACCT-CURR-BAL` | `S9(10)V99` | 12 |
| `EXP-ACCT-CREDIT-LIMIT` | `S9(10)V99` | 12 |
| `EXP-ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | 12 |
| `EXP-ACCT-OPEN-DATE` | `X(10)` | 10 |
| `EXP-ACCT-EXPIRAION-DATE` | `X(10)` | 10 |
| `EXP-ACCT-REISSUE-DATE` | `X(10)` | 10 |
| `EXP-ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | 12 |
| `EXP-ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | 12 |

### Card Export Fields
| Field | PIC Clause | Size |
|-------|-----------|-----:|
| `EXP-CARD-NUM` | `X(16)` | 16 |
| `EXP-CARD-ACCT-ID` | `9(11)` | 11 |
| `EXP-CARD-CVV-CD` | `9(03) COMP` | 2 |
| `EXP-CARD-EMBOSSED-NAME` | `X(50)` | 50 |
| `EXP-CARD-EXPIRAION-DATE` | `X(10)` | 10 |
| `EXP-CARD-ACTIVE-STATUS` | `X(01)` | 1 |

---

## 15. Application COMMAREA

**Copybook:** `COCOM01Y.cpy` | **Purpose:** Inter-program communication area passed between CICS programs

### Key Fields
| Field | PIC Clause | Size | Purpose |
|-------|-----------|-----:|---------|
| `CDEMO-FROM-TRANID` | `X(04)` | 4 | Source transaction ID |
| `CDEMO-FROM-PROGRAM` | `X(08)` | 8 | Source program name |
| `CDEMO-TO-TRANID` | `X(04)` | 4 | Target transaction ID |
| `CDEMO-TO-PROGRAM` | `X(08)` | 8 | Target program name |
| `CDEMO-USER-ID` | `X(08)` | 8 | Signed-on user ID |
| `CDEMO-USER-TYPE` | `X(01)` | 1 | User type (A/U) |
| `CDEMO-PGM-REENTER` | `X(01)` | 1 | Re-entry flag |
| `CDEMO-ACCT-ID` | `9(11)` | 11 | Current account context |
| `CDEMO-CARD-NUM` | `X(16)` | 16 | Current card context |
| `CDEMO-CUST-ID` | `9(09)` | 9 | Current customer context |
| `CDEMO-LAST-MAP` | `X(07)` | 7 | Last displayed map name |
| `CDEMO-LAST-MAPSET` | `X(07)` | 7 | Last used mapset |

### Menu Option Array (within COMMAREA)
| Field | Occurs | Purpose |
|-------|--------|---------|
| `CDEMO-MENU-OPT-NUM` | 12 | Menu option number |
| `CDEMO-MENU-OPT-NAME` | 12 | Menu option display name |
| `CDEMO-MENU-OPT-PGMNAME` | 12 | Target program for each option |
| `CDEMO-MENU-OPT-TRANID` | 12 | Target transaction ID for each option |

---

## 16. COBOL-to-Java Type Mapping Guide

| COBOL PIC Clause | COBOL Type | Bytes | Java Type | Notes |
|-----------------|------------|------:|-----------|-------|
| `9(n)` | Unsigned integer | n | `int` / `long` | Use `long` if n > 9 |
| `S9(n)` | Signed integer | n | `int` / `long` | Includes sign |
| `S9(n)V99` | Signed decimal | n+2 | `BigDecimal` | V = implied decimal, 2 decimal places |
| `S9(n)V9(m)` | Signed decimal | n+m | `BigDecimal` | V = implied decimal, m decimal places |
| `S9(n) COMP` | Binary integer | 2 or 4 | `int` / `long` | Packed binary |
| `S9(n) COMP-3` | Packed decimal | ceil((n+1)/2) | `BigDecimal` | BCD encoding |
| `X(n)` | Alphanumeric | n | `String` | Fixed-length, space-padded |
| `A(n)` | Alphabetic only | n | `String` | Letters and spaces only |
| `9(n) COMP` | Binary unsigned | 2 or 4 | `int` / `long` | Packed binary |

### Entity Relationship Summary

```
Customer (CVCUS01Y) 1──N Card-XRef (CVACT03Y) N──1 Account (CVACT01Y)
                              │
                              │ 1
                              ▼
                         Card (CVACT02Y)
                              │
                              │ 1──N
                              ▼
                     Transaction (CVTRA05Y)
                              │
                    ┌─────────┼─────────┐
                    ▼         ▼         ▼
            Type (CVTRA03Y)  Cat (CVTRA04Y)  CatBal (CVTRA01Y)
                                              │
                                              ▼
                                    DiscGroup (CVTRA02Y)
```

### VSAM-to-RDBMS Table Mapping

| VSAM File | Copybook | Suggested Table Name | Key Type |
|-----------|----------|---------------------|----------|
| `ACCTDAT` | CVACT01Y | `accounts` | PK: `account_id` |
| `CARDDAT` | CVACT02Y | `cards` | PK: `card_number`, FK: `account_id` |
| `CARDXREF` | CVACT03Y | `card_cross_references` | PK: `card_number`, FK: `customer_id`, `account_id` |
| `CUSTDAT` | CVCUS01Y | `customers` | PK: `customer_id` |
| `TRANSACT` | CVTRA05Y | `transactions` | PK: `transaction_id`, FK: `card_number` |
| `DALYTRAN` | CVTRA06Y | `daily_transactions` | PK: `transaction_id` |
| `TCATBALF` | CVTRA01Y | `transaction_category_balances` | PK: (`account_id`, `type_cd`, `cat_cd`) |
| `DISCGRP` | CVTRA02Y | `disclosure_groups` | PK: (`group_id`, `type_cd`, `cat_cd`) |
| `TRANTYPE` | CVTRA03Y | `transaction_types` | PK: `type_code` |
| `TRANCATG` | CVTRA04Y | `transaction_categories` | PK: (`type_cd`, `cat_cd`) |
| `USRSEC` | CSUSR01Y | `users` | PK: `user_id` |
