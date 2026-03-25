# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Application**: CardDemo (AWS Mainframe Credit Card Management)
> **Source**: Extracted from COBOL copybooks in `app/cpy/` and `app/cpy-bms/`

---

## Table of Contents

1. [Overview](#overview)
2. [VSAM File Inventory](#vsam-file-inventory)
3. [Business Entities](#business-entities)
   - [Account Master](#1-account-master-cvact01y)
   - [Card Master](#2-card-master-cvact02y)
   - [Card Cross-Reference](#3-card-cross-reference-cvact03y)
   - [Customer Master](#4-customer-master-cvcus01y)
   - [Transaction Record](#5-transaction-record-cvtra05y)
   - [Daily Transaction](#6-daily-transaction-cvtra06y)
   - [Transaction Category Balance](#7-transaction-category-balance-cvtra01y)
   - [Disclosure Group](#8-disclosure-group-cvtra02y)
   - [Transaction Type](#9-transaction-type-cvtra03y)
   - [Transaction Category](#10-transaction-category-cvtra04y)
   - [User Security Record](#11-user-security-record-csusr01y)
   - [Card Detail (Display)](#12-card-detail-display-cvcrd01y)
   - [Statement Transaction](#13-statement-transaction-costm01)
   - [Export/Import Record](#14-exportimport-record-cvexport)
   - [Transaction Report Layout](#15-transaction-report-layout-cvtra07y)
   - [Customer Record (Alternate)](#16-customer-record-alternate-custrec)
4. [Common Communication Areas](#common-communication-areas)
5. [PIC Clause Reference](#pic-clause-reference)
6. [Proposed Java/Database Mapping](#proposed-javadatabase-mapping)

---

## Overview

The CardDemo application stores all persistent data in VSAM KSDS (Key-Sequenced Data Sets) files on the mainframe. Each VSAM file has a corresponding COBOL copybook that defines the record layout using PIC (Picture) clauses.

### Data Type Legend

| COBOL PIC | Meaning | Java Equivalent | SQL Type |
|-----------|---------|-----------------|----------|
| `PIC X(n)` | Alphanumeric, n characters | `String` | `VARCHAR(n)` |
| `PIC 9(n)` | Unsigned numeric, n digits | `long` / `int` | `NUMERIC(n)` |
| `PIC S9(n)V99` | Signed decimal with 2 decimals | `BigDecimal` | `DECIMAL(n+2, 2)` |
| `PIC S9(n)V9(m)` | Signed decimal with m decimals | `BigDecimal` | `DECIMAL(n+m, m)` |
| `PIC 9(n) COMP` | Binary numeric | `int` / `long` | `INTEGER` |
| `FILLER PIC X(n)` | Reserved/padding bytes | (not mapped) | (not mapped) |

---

## VSAM File Inventory

| VSAM Dataset | Type | Key | Record Len | Copybook | Business Entity |
|--------------|------|-----|------------|----------|-----------------|
| `ACCTDATA.VSAM.KSDS` | KSDS | Account ID (11) | ~300 bytes | CVACT01Y | Account Master |
| `CARDDATA.VSAM.KSDS` | KSDS | Card Number (16) | ~150 bytes | CVACT02Y | Card Master |
| `CARDXREF.VSAM.KSDS` | KSDS | Card Number (16) | 50 bytes | CVACT03Y | Card Cross-Ref |
| `CUSTDATA.VSAM.KSDS` | KSDS | Customer ID (9) | ~500 bytes | CVCUS01Y | Customer Master |
| `TRANSACT.VSAM.KSDS` | KSDS | Transaction ID (16) | 350 bytes | CVTRA05Y | Transaction |
| `DALYTRAN.PS` | Sequential | N/A | 350 bytes | CVTRA06Y | Daily Transaction |
| `TCATBALF.VSAM.KSDS` | KSDS | Acct+Type+Cat (17) | 50 bytes | CVTRA01Y | Category Balance |
| `DISCGRP.VSAM.KSDS` | KSDS | Group+Type+Cat (16) | 50 bytes | CVTRA02Y | Disclosure Group |
| `TRANTYPE.VSAM.KSDS` | KSDS | Tran Type (2) | 60 bytes | CVTRA03Y | Transaction Type |
| `TRANCATG.VSAM.KSDS` | KSDS | Type+Cat (6) | 60 bytes | CVTRA04Y | Transaction Category |
| `USRSEC.VSAM.KSDS` | KSDS | User ID (8) | 80 bytes | CSUSR01Y | User Security |

---

## Business Entities

### 1. Account Master (`CVACT01Y`)

**Source**: `app/cpy/CVACT01Y.cpy` | **Record Length**: ~300 bytes | **VSAM**: `ACCTDATA.VSAM.KSDS`

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Account identifier (primary key) |
| `ACCT-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 | Account status (Y=Active, N=Inactive) |
| `ACCT-CURR-BAL` | `PIC S9(10)V99` | Decimal | 12.2 | Current account balance |
| `ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | Decimal | 12.2 | Credit limit |
| `ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | Decimal | 12.2 | Cash advance credit limit |
| `ACCT-OPEN-DATE` | `PIC X(10)` | Alpha | 10 | Account open date (YYYY-MM-DD) |
| `ACCT-EXPIRAION-DATE` | `PIC X(10)` | Alpha | 10 | Account expiration date |
| `ACCT-REISSUE-DATE` | `PIC X(10)` | Alpha | 10 | Card reissue date |
| `ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | Decimal | 12.2 | Current cycle credit amount |
| `ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | Decimal | 12.2 | Current cycle debit amount |
| `ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 | Account group identifier |
| FILLER | `PIC X(178)` | Padding | 178 | Reserved space |

**Business Rules**:
- Primary key: `ACCT-ID` (11-digit numeric)
- Financial fields are signed with 2 decimal places
- Status flag controls account accessibility in online transactions
- Used by: COACTVWC, COACTUPC, CBACT01C, CBACT04C, CBTRN02C, CBEXPORT, CBIMPORT, COBIL00C, COTRN02C

---

### 2. Card Master (`CVACT02Y`)

**Source**: `app/cpy/CVACT02Y.cpy` | **Record Length**: ~150 bytes | **VSAM**: `CARDDATA.VSAM.KSDS`

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card number (primary key) |
| `CARD-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Associated account ID (FK) |
| `CARD-CVV-CD` | `PIC 9(03)` | Numeric | 3 | Card verification value |
| `CARD-EMBOSSED-NAME` | `PIC X(50)` | Alpha | 50 | Name embossed on card |
| `CARD-EXPIRAION-DATE` | `PIC X(10)` | Alpha | 10 | Card expiration date |
| `CARD-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 | Card status (Y/N) |
| FILLER | `PIC X(59)` | Padding | 59 | Reserved space |

**Business Rules**:
- Primary key: `CARD-NUM` (16-character, typically all numeric)
- Foreign key: `CARD-ACCT-ID` links to Account Master
- CVV is stored as plain numeric (security concern for modernization)
- Used by: COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, CBACT02C, CBEXPORT, CBIMPORT

---

### 3. Card Cross-Reference (`CVACT03Y`)

**Source**: `app/cpy/CVACT03Y.cpy` | **Record Length**: 50 bytes | **VSAM**: `CARDXREF.VSAM.KSDS`

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `XREF-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card number (primary key) |
| `XREF-CUST-ID` | `PIC 9(09)` | Numeric | 9 | Customer ID (FK to Customer) |
| `XREF-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Account ID (FK to Account) |
| FILLER | `PIC X(14)` | Padding | 14 | Reserved space |

**Business Rules**:
- Bridge table linking Card -> Customer -> Account
- Has an alternate index on `XREF-ACCT-ID` for reverse lookups
- Critical for navigating between card, customer, and account entities
- Used by: CBACT03C, CBACT04C, CBTRN02C, CBSTM03A, CBEXPORT, CBIMPORT, COBIL00C, COTRN02C

---

### 4. Customer Master (`CVCUS01Y`)

**Source**: `app/cpy/CVCUS01Y.cpy` | **Record Length**: ~500 bytes | **VSAM**: `CUSTDATA.VSAM.KSDS`

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `CUST-ID` | `PIC 9(09)` | Numeric | 9 | Customer ID (primary key) |
| `CUST-FIRST-NAME` | `PIC X(25)` | Alpha | 25 | Customer first name |
| `CUST-MIDDLE-NAME` | `PIC X(25)` | Alpha | 25 | Customer middle name |
| `CUST-LAST-NAME` | `PIC X(25)` | Alpha | 25 | Customer last name |
| `CUST-ADDR-LINE-1` | `PIC X(50)` | Alpha | 50 | Address line 1 |
| `CUST-ADDR-LINE-2` | `PIC X(50)` | Alpha | 50 | Address line 2 |
| `CUST-ADDR-LINE-3` | `PIC X(50)` | Alpha | 50 | Address line 3 |
| `CUST-ADDR-STATE-CD` | `PIC X(02)` | Alpha | 2 | State code |
| `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | Alpha | 3 | Country code |
| `CUST-ADDR-ZIP` | `PIC X(10)` | Alpha | 10 | ZIP/postal code |
| `CUST-PHONE-NUM-1` | `PIC X(15)` | Alpha | 15 | Primary phone |
| `CUST-PHONE-NUM-2` | `PIC X(15)` | Alpha | 15 | Secondary phone |
| `CUST-SSN` | `PIC 9(09)` | Numeric | 9 | Social Security Number |
| `CUST-GOVT-ISSUED-ID` | `PIC X(20)` | Alpha | 20 | Government-issued ID |
| `CUST-DOB-YYYYMMDD` | `PIC X(10)` | Alpha | 10 | Date of birth |
| `CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | Alpha | 10 | EFT account for payments |
| `CUST-PRI-CARD-HOLDER-IND` | `PIC X(01)` | Alpha | 1 | Primary card holder (Y/N) |
| `CUST-FICO-CREDIT-SCORE` | `PIC 9(03)` | Numeric | 3 | FICO credit score |
| FILLER | `PIC X(168)` | Padding | 168 | Reserved space |

**Business Rules**:
- Primary key: `CUST-ID` (9-digit numeric)
- Contains PII (SSN, DOB, address) — requires special handling in modernization
- FICO score stored as 3-digit integer (300-850 range)
- Used by: COACTVWC, COCRDSLC, COCRDUPC, CBCUS01C, CBSTM03A, CBEXPORT, CBIMPORT

---

### 5. Transaction Record (`CVTRA05Y`)

**Source**: `app/cpy/CVTRA05Y.cpy` | **Record Length**: 350 bytes | **VSAM**: `TRANSACT.VSAM.KSDS`

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `TRAN-ID` | `PIC X(16)` | Alpha | 16 | Transaction ID (primary key) |
| `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type code (FK) |
| `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category code |
| `TRAN-SOURCE` | `PIC X(10)` | Alpha | 10 | Transaction source/channel |
| `TRAN-DESC` | `PIC X(100)` | Alpha | 100 | Transaction description |
| `TRAN-AMT` | `PIC S9(09)V99` | Decimal | 11.2 | Transaction amount (signed) |
| `TRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant identifier |
| `TRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant name |
| `TRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant city |
| `TRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP code |
| `TRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card used (FK to Card) |
| `TRAN-ORIG-TS` | `PIC X(26)` | Alpha | 26 | Origination timestamp |
| `TRAN-PROC-TS` | `PIC X(26)` | Alpha | 26 | Processing timestamp |
| FILLER | `PIC X(20)` | Padding | 20 | Reserved space |

**Business Rules**:
- Primary key: `TRAN-ID` (16-character)
- Foreign keys: `TRAN-CARD-NUM` -> Card Master, `TRAN-TYPE-CD` -> Transaction Type
- Amount is signed: positive = debit/purchase, negative = credit/return
- Timestamps stored as 26-char strings (ISO-like format)
- Highest-volume entity — central to batch processing cycle
- Used by: COTRN00C, COTRN01C, COTRN02C, CORPT00C, CBTRN01C, CBTRN02C, CBTRN03C, CBEXPORT, CBIMPORT, COBIL00C

---

### 6. Daily Transaction (`CVTRA06Y`)

**Source**: `app/cpy/CVTRA06Y.cpy` | **Record Length**: 350 bytes | **File**: `DALYTRAN.PS` (sequential)

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `DALYTRAN-ID` | `PIC X(16)` | Alpha | 16 | Daily transaction ID |
| `DALYTRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type code |
| `DALYTRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category code |
| `DALYTRAN-SOURCE` | `PIC X(10)` | Alpha | 10 | Transaction source |
| `DALYTRAN-DESC` | `PIC X(100)` | Alpha | 100 | Transaction description |
| `DALYTRAN-AMT` | `PIC S9(09)V99` | Decimal | 11.2 | Transaction amount |
| `DALYTRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant ID |
| `DALYTRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant name |
| `DALYTRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant city |
| `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP code |
| `DALYTRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card number |
| `DALYTRAN-ORIG-TS` | `PIC X(26)` | Alpha | 26 | Origination timestamp |
| `DALYTRAN-PROC-TS` | `PIC X(26)` | Alpha | 26 | Processing timestamp |
| FILLER | `PIC X(20)` | Padding | 20 | Reserved space |

**Business Rules**:
- Identical structure to Transaction Record (CVTRA05Y) but with `DALYTRAN-` prefix
- Represents unposted daily transactions awaiting batch processing
- Consumed by POSTTRAN job (CBTRN02C) which posts to the master TRANSACT file
- Used by: CBTRN01C, CBTRN02C

---

### 7. Transaction Category Balance (`CVTRA01Y`)

**Source**: `app/cpy/CVTRA01Y.cpy` | **Record Length**: 50 bytes | **VSAM**: `TCATBALF.VSAM.KSDS`

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `TRANCAT-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Account ID (part of key) |
| `TRANCAT-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type (part of key) |
| `TRANCAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category (part of key) |
| `TRAN-CAT-BAL` | `PIC S9(09)V99` | Decimal | 11.2 | Category balance amount |
| FILLER | `PIC X(22)` | Padding | 22 | Reserved space |

**Business Rules**:
- Composite key: Account ID + Type Code + Category Code
- Tracks running balance per account per transaction category
- Updated during interest calculation (CBACT04C) and transaction posting (CBTRN02C)
- Used by: CBACT04C, CBTRN02C

---

### 8. Disclosure Group (`CVTRA02Y`)

**Source**: `app/cpy/CVTRA02Y.cpy` | **Record Length**: 50 bytes | **VSAM**: `DISCGRP.VSAM.KSDS`

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `DIS-ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 | Account group ID (part of key) |
| `DIS-TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type (part of key) |
| `DIS-TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category (part of key) |
| `DIS-INT-RATE` | `PIC S9(04)V99` | Decimal | 6.2 | Interest rate for this category |
| FILLER | `PIC X(28)` | Padding | 28 | Reserved space |

**Business Rules**:
- Composite key: Group ID + Type + Category
- Defines interest rates per account group per transaction type/category
- Used by interest calculation batch process (CBACT04C)
- Reference/configuration data — rarely changes
- Used by: CBACT04C

---

### 9. Transaction Type (`CVTRA03Y`)

**Source**: `app/cpy/CVTRA03Y.cpy` | **Record Length**: 60 bytes | **VSAM**: `TRANTYPE.VSAM.KSDS`

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `TRAN-TYPE` | `PIC X(02)` | Alpha | 2 | Transaction type code (primary key) |
| `TRAN-TYPE-DESC` | `PIC X(50)` | Alpha | 50 | Type description |
| FILLER | `PIC X(08)` | Padding | 8 | Reserved space |

**Business Rules**:
- Simple lookup table (code -> description)
- Examples: "SA" = Sale, "CR" = Credit, "CA" = Cash Advance
- Referenced by transaction records via `TRAN-TYPE-CD`
- Used by: CBTRN03C

---

### 10. Transaction Category (`CVTRA04Y`)

**Source**: `app/cpy/CVTRA04Y.cpy` | **Record Length**: 60 bytes | **VSAM**: `TRANCATG.VSAM.KSDS`

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type (part of key) |
| `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Category code (part of key) |
| `TRAN-CAT-TYPE-DESC` | `PIC X(50)` | Alpha | 50 | Category description |
| FILLER | `PIC X(04)` | Padding | 4 | Reserved space |

**Business Rules**:
- Composite key: Type Code + Category Code
- Sub-classification within transaction types
- Used for reporting and interest rate grouping
- Used by: CBTRN03C

---

### 11. User Security Record (`CSUSR01Y`)

**Source**: `app/cpy/CSUSR01Y.cpy` | **Record Length**: 80 bytes | **VSAM**: `USRSEC.VSAM.KSDS`

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `SEC-USR-ID` | `PIC X(08)` | Alpha | 8 | User ID (primary key) |
| `SEC-USR-FNAME` | `PIC X(20)` | Alpha | 20 | User first name |
| `SEC-USR-LNAME` | `PIC X(20)` | Alpha | 20 | User last name |
| `SEC-USR-PWD` | `PIC X(08)` | Alpha | 8 | User password (plain text!) |
| `SEC-USR-TYPE` | `PIC X(01)` | Alpha | 1 | User type (A=Admin, U=Regular) |
| FILLER | `PIC X(23)` | Padding | 23 | Reserved space |

**Business Rules**:
- Primary key: `SEC-USR-ID` (8-char, e.g., "USER0001", "ADMIN001")
- **Security Risk**: Password stored in plain text — must be hashed in modernized version
- Type controls menu access: 'A' = Admin (full access), 'U' = Regular user
- Used by: COSGN00C (login), COUSR00C-03C (user management), COACTUPC, COADM01C, COCRDLIC, COCRDSLC, COCRDUPC, COMEN01C

---

### 12. Card Detail Display (`CVCRD01Y`)

**Source**: `app/cpy/CVCRD01Y.cpy` | **Record Length**: N/A (display structure)

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `CDEMO-CRD-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card number for display |
| `CDEMO-CRD-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Associated account ID |
| `CDEMO-CRD-CVV-CD` | `PIC 9(03)` | Numeric | 3 | CVV code |
| `CDEMO-CRD-EMBOSSED-NAME` | `PIC X(50)` | Alpha | 50 | Embossed name |
| `CDEMO-CRD-EXPIRAION-DATE` | `PIC X(10)` | Alpha | 10 | Expiration date |
| `CDEMO-CRD-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 | Card status |

**Business Rules**:
- Working storage structure for screen display, not a VSAM record
- Populated from Card Master (CVACT02Y) for online display
- Used by: COACTUPC, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC

---

### 13. Statement Transaction (`COSTM01`)

**Source**: `app/cpy/COSTM01.CPY` | **Record Length**: 350 bytes | **File**: `TRXFL.VSAM.KSDS` (temporary)

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| `TRNX-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card number (part of key) |
| `TRNX-ID` | `PIC X(16)` | Alpha | 16 | Transaction ID (part of key) |
| `TRNX-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type |
| `TRNX-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category |
| `TRNX-SOURCE` | `PIC X(10)` | Alpha | 10 | Source channel |
| `TRNX-DESC` | `PIC X(100)` | Alpha | 100 | Description |
| `TRNX-AMT` | `PIC S9(09)V99` | Decimal | 11.2 | Amount |
| `TRNX-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant ID |
| `TRNX-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant name |
| `TRNX-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant city |
| `TRNX-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP |
| `TRNX-ORIG-TS` | `PIC X(26)` | Alpha | 26 | Origination timestamp |
| `TRNX-PROC-TS` | `PIC X(26)` | Alpha | 26 | Processing timestamp |
| FILLER | `PIC X(20)` | Padding | 20 | Reserved space |

**Business Rules**:
- Re-keyed version of Transaction Record with Card Number as primary sort
- Composite key: Card Number + Transaction ID (allows statement grouping by card)
- Created during CREASTMT job processing (SORT step re-keys transactions)
- Used by: CBSTM03A (statement generation)

---

### 14. Export/Import Record (`CVEXPORT`)

**Source**: `app/cpy/CVEXPORT.cpy` | **Record Length**: Variable | **File**: `EXPORT.DATA`

Contains a superset structure combining fields from Account, Card, Customer, Cross-Reference, and Transaction records for bulk data export/import operations.

| Group | Key Fields | Description |
|-------|-----------|-------------|
| Account | `EXP-ACCT-ID`, `EXP-ACCT-ACTIVE-STATUS`, `EXP-ACCT-CURR-BAL`, etc. | Full account record |
| Card | `EXP-CARD-NUM`, `EXP-CARD-CVV-CD`, `EXP-CARD-EMBOSSED-NAME`, etc. | Full card record |
| Cross-Ref | `EXP-XREF-CARD-NUM`, `EXP-XREF-CUST-ID`, `EXP-XREF-ACCT-ID` | Cross-reference |
| Customer | `EXP-CUST-ID`, `EXP-CUST-FIRST-NAME`, `EXP-CUST-SSN`, etc. | Full customer record |
| Transaction | `EXP-TRAN-ID`, `EXP-TRAN-AMT`, `EXP-TRAN-CARD-NUM`, etc. | Full transaction record |

**Business Rules**:
- Used exclusively by CBEXPORT/CBIMPORT programs
- Contains a record type indicator to distinguish entity types in a single file
- Used by: CBEXPORT, CBIMPORT

---

### 15. Transaction Report Layout (`CVTRA07Y`)

**Source**: `app/cpy/CVTRA07Y.cpy` | **Record Length**: 133 bytes (print line)

| Structure | Key Fields | Description |
|-----------|-----------|-------------|
| `REPORT-NAME-HEADER` | `REPT-SHORT-NAME`, `REPT-LONG-NAME`, `REPT-START-DATE`, `REPT-END-DATE` | Report header with date range |
| `TRANSACTION-DETAIL-REPORT` | `TRAN-REPORT-TRANS-ID`, `TRAN-REPORT-ACCOUNT-ID`, `TRAN-REPORT-AMT`, etc. | Detail line |
| `TRANSACTION-HEADER-1` | Column headers | "Transaction ID", "Account ID", "Transaction Type", etc. |
| `TRANSACTION-HEADER-2` | Separator | Dashes |
| `REPORT-PAGE-TOTALS` | `REPT-PAGE-TOTAL` | Page subtotal |
| `REPORT-ACCOUNT-TOTALS` | `REPT-ACCOUNT-TOTAL` | Account subtotal |
| `REPORT-GRAND-TOTALS` | `REPT-GRAND-TOTAL` | Grand total |

**Business Rules**:
- Print-oriented layout for the Daily Transaction Report
- Amounts formatted as `+ZZZ,ZZZ,ZZZ.ZZ` (edited picture)
- Used by: CBTRN03C

---

### 16. Customer Record Alternate (`CUSTREC`)

**Source**: `app/cpy/CUSTREC.cpy` | **Record Length**: ~500 bytes

Alternate layout for customer data used in statement generation. Same fields as CVCUS01Y but with different field naming conventions for use in the CBSTM03A program.

- Used by: CBSTM03A

---

## Common Communication Areas

### CICS COMMAREA (`COCOM01Y`)

**Source**: `app/cpy/COCOM01Y.cpy` | Used by all 16 online CICS programs

| Field Name | PIC Clause | Business Purpose |
|------------|-----------|-----------------|
| `CDEMO-FROM-TRANID` | `PIC X(04)` | Source transaction ID |
| `CDEMO-FROM-PROGRAM` | `PIC X(08)` | Source program name |
| `CDEMO-TO-TRANID` | `PIC X(04)` | Target transaction ID |
| `CDEMO-TO-PROGRAM` | `PIC X(08)` | Target program name |
| `CDEMO-PGM-REENTER` | `PIC X(01)` | Program re-entry flag |
| `CDEMO-USR-ID` | `PIC X(08)` | Current user ID |
| `CDEMO-USR-TYP` | `PIC X(01)` | Current user type (A/U) |
| `CDEMO-ACCT-ID` | `PIC 9(11)` | Selected account ID |
| `CDEMO-CARD-NUM` | `PIC X(16)` | Selected card number |
| `CDEMO-CUST-ID` | `PIC 9(09)` | Selected customer ID |
| `CDEMO-LAST-MAP` | `PIC X(07)` | Last BMS map displayed |
| `CDEMO-LAST-MAPSET` | `PIC X(07)` | Last BMS mapset used |

**Business Rules**:
- Passed between programs via CICS XCTL/RETURN
- Maintains session state (user context, selected entities)
- Navigation control via FROM/TO program fields

### Menu Definitions (`COMEN02Y`)

Defines menu option arrays mapping option numbers to program names and transaction IDs for both the regular user menu and admin menu.

### Lookup Code Table (`CSLKPCDY`)

**Source**: `app/cpy/CSLKPCDY.cpy` | **Lines**: 1,318

The largest copybook — contains hardcoded lookup tables for:
- US State codes and names
- Country codes
- Date format masks
- Other reference data

**Modernization Note**: Should be externalized to a database table or configuration file.

---

## PIC Clause Reference

### Numeric Formats Used

| Pattern | Example | Bytes | Java Mapping |
|---------|---------|-------|-------------|
| `PIC 9(n)` | `PIC 9(11)` | n | `long` |
| `PIC 9(n) COMP` | `PIC 9(03) COMP` | 2 | `int` |
| `PIC S9(n)V99` | `PIC S9(09)V99` | n+3 | `BigDecimal(n+2, 2)` |
| `PIC S9(n)V9(m)` | `PIC S9(04)V99` | n+m+1 | `BigDecimal(n+m, m)` |

### Edited Formats (Reports Only)

| Pattern | Example | Output |
|---------|---------|--------|
| `PIC -ZZZ,ZZZ,ZZZ.ZZ` | -1234.56 | `-    1,234.56` |
| `PIC +ZZZ,ZZZ,ZZZ.ZZ` | 1234.56 | `+    1,234.56` |

---

## Proposed Java/Database Mapping

### Entity-to-Table Mapping

| COBOL Entity | Proposed Table | Primary Key | Notes |
|-------------|---------------|-------------|-------|
| CVACT01Y (Account) | `accounts` | `account_id BIGINT` | Add audit timestamps |
| CVACT02Y (Card) | `cards` | `card_number VARCHAR(16)` | Encrypt CVV |
| CVACT03Y (Cross-Ref) | `card_account_xref` | `card_number VARCHAR(16)` | Composite unique constraint |
| CVCUS01Y (Customer) | `customers` | `customer_id BIGINT` | Encrypt SSN, add email |
| CVTRA05Y (Transaction) | `transactions` | `transaction_id VARCHAR(16)` | Use TIMESTAMP columns |
| CVTRA06Y (Daily Trans) | `daily_transactions` | `id BIGSERIAL` | Staging table pattern |
| CVTRA01Y (Cat Balance) | `category_balances` | `(account_id, type_cd, cat_cd)` | Composite PK |
| CVTRA02Y (Disclosure) | `disclosure_groups` | `(group_id, type_cd, cat_cd)` | Configuration table |
| CVTRA03Y (Tran Type) | `transaction_types` | `type_code VARCHAR(2)` | Lookup table |
| CVTRA04Y (Tran Cat) | `transaction_categories` | `(type_code, cat_code)` | Lookup table |
| CSUSR01Y (User) | `users` | `user_id VARCHAR(8)` | Hash passwords, add roles |

### Key Modernization Concerns

| Concern | COBOL State | Recommended Java State |
|---------|------------|----------------------|
| Password Storage | Plain text in CSUSR01Y | BCrypt hash + salt |
| CVV Storage | Plain numeric in CVACT02Y | Encrypted at rest, tokenized |
| SSN Storage | Plain numeric in CVCUS01Y | AES-256 encrypted |
| Timestamps | 26-char strings | `java.time.Instant` / `TIMESTAMP WITH TIME ZONE` |
| Monetary Amounts | `PIC S9(09)V99` | `BigDecimal` / `DECIMAL(11,2)` |
| Date Fields | `PIC X(10)` strings | `java.time.LocalDate` / `DATE` |
| FILLER Bytes | Padding for fixed records | Eliminated |
| Lookup Tables | Hardcoded in CSLKPCDY | Database reference tables or enum |
