# CardDemo Data Dictionary

> **Generated for**: Mainframe-to-Java Modernization Assessment
> **Application**: CardDemo - Credit Card Management System
> **Source**: COBOL Copybook PIC clause analysis

---

## Table of Contents

1. [Overview](#overview)
2. [Account Entity](#1-account-entity)
3. [Card Entity](#2-card-entity)
4. [Customer Entity](#3-customer-entity)
5. [Transaction Entity](#4-transaction-entity)
6. [Daily Transaction Entity](#5-daily-transaction-entity)
7. [Transaction Category Balance Entity](#6-transaction-category-balance-entity)
8. [Disclosure Group Entity](#7-disclosure-group-entity)
9. [Transaction Type Entity](#8-transaction-type-entity)
10. [Transaction Category Entity](#9-transaction-category-entity)
11. [User Security Entity](#10-user-security-entity)
12. [Card Cross-Reference Entity](#11-card-cross-reference-entity)
13. [Card Detail Entity](#12-card-detail-entity)
14. [Statement Transaction Entity](#13-statement-transaction-entity)
15. [Export Record Entity](#14-export-record-entity)
16. [Report Structures](#15-report-structures)
17. [Common Communication Area](#16-common-communication-area)
18. [Lookup Reference Data](#17-lookup-reference-data)
19. [VSAM File Catalog](#vsam-file-catalog)
20. [Java Modernization Type Mapping](#java-modernization-type-mapping)

---

## Overview

The CardDemo application uses VSAM KSDS (Key-Sequenced Data Sets) files as its primary data store. Each VSAM file has a corresponding COBOL copybook that defines its record layout using PIC (Picture) clauses. This dictionary translates those layouts into a business-friendly format suitable for designing relational database schemas and Java POJOs/DTOs.

### COBOL PIC Clause Quick Reference

| COBOL PIC | Meaning | Java Equivalent |
|-----------|---------|-----------------|
| `PIC X(n)` | Alphanumeric, n characters | `String` |
| `PIC 9(n)` | Unsigned numeric, n digits | `int` / `long` |
| `PIC S9(n)V99` | Signed decimal with 2 decimal places | `BigDecimal` |
| `PIC 9(n) COMP` | Binary (computational) integer | `int` / `long` |
| `FILLER` | Padding / reserved space | Not mapped |

---

## 1. Account Entity

> **Copybook**: `CVACT01Y.cpy` | **VSAM File**: `ACCTDATA.VSAM.KSDS` | **Record Length**: 300 bytes

| # | COBOL Field | PIC Clause | Offset | Business Name | Description | Data Type | Constraints |
|---|------------|------------|--------|---------------|-------------|-----------|-------------|
| 1 | `ACCT-ID` | `PIC 9(11)` | 0 | Account ID | Unique account identifier | Numeric (11) | **Primary Key** |
| 2 | `ACCT-ACTIVE-STATUS` | `PIC X(01)` | 11 | Active Status | Account status flag | Char(1) | 'Y'=Active, 'N'=Inactive |
| 3 | `ACCT-CURR-BAL` | `PIC S9(10)V99` | 12 | Current Balance | Current account balance | Decimal(12,2) | Signed |
| 4 | `ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | 24 | Credit Limit | Maximum credit allowed | Decimal(12,2) | Signed |
| 5 | `ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | 36 | Cash Credit Limit | Cash advance limit | Decimal(12,2) | Signed |
| 6 | `ACCT-OPEN-DATE` | `PIC X(10)` | 48 | Open Date | Account opening date | String(10) | Format: YYYY-MM-DD |
| 7 | `ACCT-EXPIRAION-DATE` | `PIC X(10)` | 58 | Expiration Date | Account expiration date | String(10) | Format: YYYY-MM-DD |
| 8 | `ACCT-REISSUE-DATE` | `PIC X(10)` | 68 | Reissue Date | Last card reissue date | String(10) | Format: YYYY-MM-DD |
| 9 | `ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | 78 | Current Cycle Credit | Credits in current cycle | Decimal(12,2) | Signed |
| 10 | `ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | 90 | Current Cycle Debit | Debits in current cycle | Decimal(12,2) | Signed |
| 11 | `ACCT-ADDR-ZIP` | `PIC X(10)` | 102 | ZIP Code | Account holder ZIP | String(10) | |
| 12 | `ACCT-GROUP-ID` | `PIC X(10)` | 112 | Group ID | Account group classification | String(10) | FK → Disclosure Group |
| 13 | `FILLER` | `PIC X(178)` | 122 | Reserved | Padding to 300 bytes | - | Not mapped |

**Key Relationships**: Account → Card (1:N via Cross-Reference), Account → Transaction (1:N)

---

## 2. Card Entity

> **Copybook**: `CVACT02Y.cpy` | **VSAM File**: `CARDDATA.VSAM.KSDS` | **Record Length**: 150 bytes

| # | COBOL Field | PIC Clause | Business Name | Description | Data Type | Constraints |
|---|------------|------------|---------------|-------------|-----------|-------------|
| 1 | `CARD-NUM` | `PIC X(16)` | Card Number | Credit card number | String(16) | **Primary Key** |
| 2 | `CARD-ACCT-ID` | `PIC 9(11)` | Account ID | Linked account | Numeric(11) | **FK → Account** |
| 3 | `CARD-CVV-CD` | `PIC 9(03)` | CVV Code | Card verification value | Numeric(3) | Sensitive |
| 4 | `CARD-EMBOSSED-NAME` | `PIC X(50)` | Embossed Name | Name printed on card | String(50) | |
| 5 | `CARD-EXPIRAION-DATE` | `PIC X(10)` | Expiration Date | Card expiry date | String(10) | Format: YYYY-MM-DD |
| 6 | `CARD-ACTIVE-STATUS` | `PIC X(01)` | Active Status | Card status flag | Char(1) | 'Y'=Active, 'N'=Inactive |
| 7 | `FILLER` | `PIC X(59)` | Reserved | Padding | - | Not mapped |

**Key Relationships**: Card → Account (N:1), Card → Transaction (1:N)

---

## 3. Customer Entity

> **Copybook**: `CVCUS01Y.cpy` | **VSAM File**: `CUSTDATA.VSAM.KSDS` | **Record Length**: 500 bytes

| # | COBOL Field | PIC Clause | Business Name | Description | Data Type | Constraints |
|---|------------|------------|---------------|-------------|-----------|-------------|
| 1 | `CUST-ID` | `PIC 9(09)` | Customer ID | Unique customer identifier | Numeric(9) | **Primary Key** |
| 2 | `CUST-FIRST-NAME` | `PIC X(25)` | First Name | Customer first name | String(25) | |
| 3 | `CUST-MIDDLE-NAME` | `PIC X(25)` | Middle Name | Customer middle name | String(25) | Optional |
| 4 | `CUST-LAST-NAME` | `PIC X(25)` | Last Name | Customer last name | String(25) | |
| 5 | `CUST-ADDR-LINE-1` | `PIC X(50)` | Address Line 1 | Street address | String(50) | |
| 6 | `CUST-ADDR-LINE-2` | `PIC X(50)` | Address Line 2 | Suite/apt | String(50) | Optional |
| 7 | `CUST-ADDR-LINE-3` | `PIC X(50)` | Address Line 3 | Additional address | String(50) | Optional |
| 8 | `CUST-ADDR-STATE-CD` | `PIC X(02)` | State Code | US state code | String(2) | Lookup: CSLKPCDY |
| 9 | `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | Country Code | ISO country code | String(3) | Lookup: CSLKPCDY |
| 10 | `CUST-ADDR-ZIP` | `PIC X(10)` | ZIP Code | Postal code | String(10) | |
| 11 | `CUST-PHONE-NUM-1` | `PIC X(15)` | Phone 1 | Primary phone | String(15) | |
| 12 | `CUST-PHONE-NUM-2` | `PIC X(15)` | Phone 2 | Secondary phone | String(15) | Optional |
| 13 | `CUST-SSN` | `PIC 9(09)` | SSN | Social Security Number | Numeric(9) | **Sensitive / PII** |
| 14 | `CUST-GOVT-ISSUED-ID` | `PIC X(20)` | Government ID | Government-issued ID | String(20) | **Sensitive / PII** |
| 15 | `CUST-DOB-YYYYMMDD` | `PIC X(10)` | Date of Birth | Customer DOB | String(10) | Format: YYYY-MM-DD |
| 16 | `CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | EFT Account | Electronic funds transfer account | String(10) | |
| 17 | `CUST-PRI-CARD-HOLDER-IND` | `PIC X(01)` | Primary Holder | Primary cardholder indicator | Char(1) | 'Y'/'N' |
| 18 | `CUST-FICO-CREDIT-SCORE` | `PIC 9(03)` | FICO Score | Credit score | Numeric(3) | Range: 300-850 |
| 19 | `FILLER` | `PIC X(168)` | Reserved | Padding to 500 bytes | - | Not mapped |

**Key Relationships**: Customer → Account (1:N), Customer → Card (1:N via Account)

---

## 4. Transaction Entity

> **Copybook**: `CVTRA05Y.cpy` | **VSAM File**: `TRANSACT.VSAM.KSDS` | **Record Length**: 350 bytes

| # | COBOL Field | PIC Clause | Business Name | Description | Data Type | Constraints |
|---|------------|------------|---------------|-------------|-----------|-------------|
| 1 | `TRAN-ID` | `PIC X(16)` | Transaction ID | Unique transaction identifier | String(16) | **Primary Key** |
| 2 | `TRAN-TYPE-CD` | `PIC X(02)` | Type Code | Transaction type code | String(2) | FK → Transaction Type |
| 3 | `TRAN-CAT-CD` | `PIC 9(04)` | Category Code | Transaction category code | Numeric(4) | FK → Transaction Category |
| 4 | `TRAN-SOURCE` | `PIC X(10)` | Source | Transaction origin source | String(10) | e.g. "ONLINE", "POS" |
| 5 | `TRAN-DESC` | `PIC X(100)` | Description | Transaction description | String(100) | |
| 6 | `TRAN-AMT` | `PIC S9(09)V99` | Amount | Transaction amount | Decimal(11,2) | Signed (+credit/-debit) |
| 7 | `TRAN-MERCHANT-ID` | `PIC 9(09)` | Merchant ID | Merchant identifier | Numeric(9) | |
| 8 | `TRAN-MERCHANT-NAME` | `PIC X(50)` | Merchant Name | Merchant business name | String(50) | |
| 9 | `TRAN-MERCHANT-CITY` | `PIC X(50)` | Merchant City | Merchant city | String(50) | |
| 10 | `TRAN-MERCHANT-ZIP` | `PIC X(10)` | Merchant ZIP | Merchant postal code | String(10) | |
| 11 | `TRAN-CARD-NUM` | `PIC X(16)` | Card Number | Card used for transaction | String(16) | **FK → Card** |
| 12 | `TRAN-ORIG-TS` | `PIC X(26)` | Origination Timestamp | When transaction originated | String(26) | ISO timestamp |
| 13 | `TRAN-PROC-TS` | `PIC X(26)` | Processing Timestamp | When transaction was processed | String(26) | ISO timestamp |
| 14 | `FILLER` | `PIC X(20)` | Reserved | Padding | - | Not mapped |

**Key Relationships**: Transaction → Card (N:1), Transaction → Transaction Type (N:1), Transaction → Transaction Category (N:1)

---

## 5. Daily Transaction Entity

> **Copybook**: `CVTRA06Y.cpy` | **VSAM File**: `DALYTRAN.VSAM.KSDS` | **Record Length**: 350 bytes

| # | COBOL Field | PIC Clause | Business Name | Description | Data Type |
|---|------------|------------|---------------|-------------|-----------|
| 1 | `DALYTRAN-ID` | `PIC X(16)` | Transaction ID | Daily transaction ID | String(16) |
| 2 | `DALYTRAN-TYPE-CD` | `PIC X(02)` | Type Code | Transaction type | String(2) |
| 3 | `DALYTRAN-CAT-CD` | `PIC 9(04)` | Category Code | Transaction category | Numeric(4) |
| 4 | `DALYTRAN-SOURCE` | `PIC X(10)` | Source | Transaction origin | String(10) |
| 5 | `DALYTRAN-DESC` | `PIC X(100)` | Description | Transaction description | String(100) |
| 6 | `DALYTRAN-AMT` | `PIC S9(09)V99` | Amount | Transaction amount | Decimal(11,2) |
| 7 | `DALYTRAN-MERCHANT-ID` | `PIC 9(09)` | Merchant ID | Merchant identifier | Numeric(9) |
| 8 | `DALYTRAN-MERCHANT-NAME` | `PIC X(50)` | Merchant Name | Merchant name | String(50) |
| 9 | `DALYTRAN-MERCHANT-CITY` | `PIC X(50)` | Merchant City | Merchant city | String(50) |
| 10 | `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)` | Merchant ZIP | Merchant postal code | String(10) |
| 11 | `DALYTRAN-CARD-NUM` | `PIC X(16)` | Card Number | Card used | String(16) |
| 12 | `DALYTRAN-ORIG-TS` | `PIC X(26)` | Origination TS | When originated | String(26) |
| 13 | `DALYTRAN-PROC-TS` | `PIC X(26)` | Processing TS | When processed | String(26) |
| 14 | `FILLER` | `PIC X(20)` | Reserved | Padding | - |

> **Note**: Identical structure to Transaction Entity. Daily transactions are staged here before being posted to the master TRANSACT file by CBTRN02C.

---

## 6. Transaction Category Balance Entity

> **Copybook**: `CVTRA01Y.cpy` | **VSAM File**: `TCATBAL.VSAM.KSDS` | **Record Length**: 50 bytes

| # | COBOL Field | PIC Clause | Business Name | Description | Data Type | Constraints |
|---|------------|------------|---------------|-------------|-----------|-------------|
| 1 | `TRANCAT-ACCT-ID` | `PIC 9(11)` | Account ID | Account identifier | Numeric(11) | **Composite PK part 1** |
| 2 | `TRANCAT-TYPE-CD` | `PIC X(02)` | Type Code | Transaction type | String(2) | **Composite PK part 2** |
| 3 | `TRANCAT-CD` | `PIC 9(04)` | Category Code | Transaction category | Numeric(4) | **Composite PK part 3** |
| 4 | `TRAN-CAT-BAL` | `PIC S9(09)V99` | Category Balance | Running balance for this category | Decimal(11,2) | Signed |
| 5 | `FILLER` | `PIC X(22)` | Reserved | Padding | - | Not mapped |

**Business Rule**: Tracks the running balance per account per transaction type/category combination. Updated during transaction posting (CBTRN02C) and interest calculation (CBACT04C).

---

## 7. Disclosure Group Entity

> **Copybook**: `CVTRA02Y.cpy` | **VSAM File**: `DISCGRP.VSAM.KSDS` | **Record Length**: 50 bytes

| # | COBOL Field | PIC Clause | Business Name | Description | Data Type | Constraints |
|---|------------|------------|---------------|-------------|-----------|-------------|
| 1 | `DIS-ACCT-GROUP-ID` | `PIC X(10)` | Account Group ID | Disclosure group identifier | String(10) | **Composite PK part 1** |
| 2 | `DIS-TRAN-TYPE-CD` | `PIC X(02)` | Type Code | Transaction type | String(2) | **Composite PK part 2** |
| 3 | `DIS-TRAN-CAT-CD` | `PIC 9(04)` | Category Code | Transaction category | Numeric(4) | **Composite PK part 3** |
| 4 | `DIS-INT-RATE` | `PIC S9(04)V99` | Interest Rate | Interest rate for this group/type/category | Decimal(6,2) | Percentage |
| 5 | `FILLER` | `PIC X(28)` | Reserved | Padding | - | Not mapped |

**Business Rule**: Defines the interest rate that applies to a specific account group + transaction type + category combination. Used by CBACT04C for interest calculation.

---

## 8. Transaction Type Entity

> **Copybook**: `CVTRA03Y.cpy` | **VSAM File**: `TRANTYPE.VSAM.KSDS` | **Record Length**: 60 bytes

| # | COBOL Field | PIC Clause | Business Name | Description | Data Type | Constraints |
|---|------------|------------|---------------|-------------|-----------|-------------|
| 1 | `TRAN-TYPE` | `PIC X(02)` | Transaction Type | Type code (e.g., "01"=Purchase) | String(2) | **Primary Key** |
| 2 | `TRAN-TYPE-DESC` | `PIC X(50)` | Description | Human-readable type name | String(50) | |
| 3 | `FILLER` | `PIC X(08)` | Reserved | Padding | - | Not mapped |

---

## 9. Transaction Category Entity

> **Copybook**: `CVTRA04Y.cpy` | **VSAM File**: `TRANCATG.VSAM.KSDS` | **Record Length**: 60 bytes

| # | COBOL Field | PIC Clause | Business Name | Description | Data Type | Constraints |
|---|------------|------------|---------------|-------------|-----------|-------------|
| 1 | `TRAN-TYPE-CD` | `PIC X(02)` | Type Code | Parent transaction type | String(2) | **Composite PK part 1**, FK → Transaction Type |
| 2 | `TRAN-CAT-CD` | `PIC 9(04)` | Category Code | Category within type | Numeric(4) | **Composite PK part 2** |
| 3 | `TRAN-CAT-TYPE-DESC` | `PIC X(50)` | Description | Category description | String(50) | |
| 4 | `FILLER` | `PIC X(04)` | Reserved | Padding | - | Not mapped |

---

## 10. User Security Entity

> **Copybook**: `CSUSR01Y.cpy` | **VSAM File**: `USRSEC.VSAM.KSDS` | **Record Length**: 80 bytes

| # | COBOL Field | PIC Clause | Business Name | Description | Data Type | Constraints |
|---|------------|------------|---------------|-------------|-----------|-------------|
| 1 | `SEC-USR-ID` | `PIC X(08)` | User ID | Login user identifier | String(8) | **Primary Key** |
| 2 | `SEC-USR-FNAME` | `PIC X(20)` | First Name | User first name | String(20) | |
| 3 | `SEC-USR-LNAME` | `PIC X(20)` | Last Name | User last name | String(20) | |
| 4 | `SEC-USR-PWD` | `PIC X(08)` | Password | User password | String(8) | **Sensitive** - plaintext |
| 5 | `SEC-USR-TYPE` | `PIC X(01)` | User Type | Role indicator | Char(1) | 'R'=Regular, 'A'=Admin |
| 6 | `SEC-USR-FILLER` | `PIC X(23)` | Reserved | Padding | - | Not mapped |

**Security Note**: Passwords are stored in plaintext. Modernized version must implement hashing (bcrypt/scrypt) and proper authentication mechanisms.

---

## 11. Card Cross-Reference Entity

> **Copybook**: `CVACT03Y.cpy` | **VSAM File**: `CARDXREF.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Business Name | Description | Data Type | Constraints |
|---|------------|------------|---------------|-------------|-----------|-------------|
| 1 | `XREF-CARD-NUM` | `PIC X(16)` | Card Number | Credit card number | String(16) | **Primary Key** |
| 2 | `XREF-ACCT-ID` | `PIC 9(11)` | Account ID | Associated account | Numeric(11) | FK → Account |
| 3 | `XREF-CUST-ID` | `PIC 9(09)` | Customer ID | Associated customer | Numeric(9) | FK → Customer |

**Business Rule**: Provides the many-to-many linkage between cards, accounts, and customers. Central to navigating from a card to its owner and account.

---

## 12. Card Detail Entity

> **Copybook**: `CVCRD01Y.cpy` | Used by online programs for card management screens

| # | COBOL Field | PIC Clause | Business Name | Description | Data Type |
|---|------------|------------|---------------|-------------|-----------|
| 1 | `CCARD-CARD-NUM` | `PIC X(16)` | Card Number | Card number | String(16) |
| 2 | `CCARD-ACCT-ID` | `PIC 9(11)` | Account ID | Linked account | Numeric(11) |
| 3 | `CCARD-CVV-CD` | `PIC 9(03)` | CVV Code | Card verification | Numeric(3) |
| 4 | `CCARD-EMBOSSED-NAME` | `PIC X(50)` | Embossed Name | Cardholder name | String(50) |
| 5 | `CCARD-EXPIRAION-DATE` | `PIC X(10)` | Expiration Date | Card expiry | String(10) |
| 6 | `CCARD-ACTIVE-STATUS` | `PIC X(01)` | Active Status | Status flag | Char(1) |

> **Note**: This is a working-storage version of the Card entity used in CICS programs for screen interaction. Maps to the same underlying VSAM data as CVACT02Y.

---

## 13. Statement Transaction Entity

> **Copybook**: `COSTM01.CPY` | Used by CBSTM03A for statement generation

| # | COBOL Field | PIC Clause | Business Name | Description | Data Type |
|---|------------|------------|---------------|-------------|-----------|
| 1 | `TRNX-CARD-NUM` | `PIC X(16)` | Card Number | Card for this transaction | String(16) |
| 2 | `TRNX-ID` | `PIC X(16)` | Transaction ID | Transaction identifier | String(16) |
| 3 | `TRNX-TYPE-CD` | `PIC X(02)` | Type Code | Transaction type | String(2) |
| 4 | `TRNX-CAT-CD` | `PIC 9(04)` | Category Code | Transaction category | Numeric(4) |
| 5 | `TRNX-SOURCE` | `PIC X(10)` | Source | Transaction source | String(10) |
| 6 | `TRNX-DESC` | `PIC X(100)` | Description | Transaction description | String(100) |
| 7 | `TRNX-AMT` | `PIC S9(09)V99` | Amount | Transaction amount | Decimal(11,2) |
| 8 | `TRNX-MERCHANT-ID` | `PIC 9(09)` | Merchant ID | Merchant identifier | Numeric(9) |
| 9 | `TRNX-MERCHANT-NAME` | `PIC X(50)` | Merchant Name | Merchant name | String(50) |
| 10 | `TRNX-MERCHANT-CITY` | `PIC X(50)` | Merchant City | Merchant city | String(50) |
| 11 | `TRNX-MERCHANT-ZIP` | `PIC X(10)` | Merchant ZIP | Merchant ZIP | String(10) |
| 12 | `TRNX-ORIG-TS` | `PIC X(26)` | Origination TS | Origination timestamp | String(26) |
| 13 | `TRNX-PROC-TS` | `PIC X(26)` | Processing TS | Processing timestamp | String(26) |

> **Note**: Key structure is `TRNX-CARD-NUM + TRNX-ID` (32-byte composite key), sorted for statement generation. This re-keys the standard transaction record by card number first.

---

## 14. Export Record Entity

> **Copybook**: `CVEXPORT.cpy` | Used by CBEXPORT/CBIMPORT for data migration

| # | COBOL Field | PIC Clause | Business Name | Description |
|---|------------|------------|---------------|-------------|
| 1 | `EXP-RECORD-TYPE` | `PIC X(01)` | Record Type | 'C'=Customer, 'A'=Account, 'X'=Xref, 'T'=Transaction, 'R'=Card |
| 2 | (Union) | varies | Record Data | One of the following based on record type: |

**Customer Export Fields** (when Record Type = 'C'):
| Field | PIC | Business Name |
|-------|-----|---------------|
| `EXP-CUST-ID` | `PIC 9(09)` | Customer ID |
| `EXP-CUST-FIRST-NAME` | `PIC X(25)` | First Name |
| `EXP-CUST-MIDDLE-NAME` | `PIC X(25)` | Middle Name |
| `EXP-CUST-LAST-NAME` | `PIC X(25)` | Last Name |
| `EXP-CUST-ADDR-LINE-1` | `PIC X(50)` | Address Line 1 |
| `EXP-CUST-ADDR-LINE-2` | `PIC X(50)` | Address Line 2 |
| `EXP-CUST-ADDR-LINE-3` | `PIC X(50)` | Address Line 3 |
| `EXP-CUST-ADDR-STATE-CD` | `PIC X(02)` | State Code |
| `EXP-CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | Country Code |
| `EXP-CUST-ADDR-ZIP` | `PIC X(10)` | ZIP Code |
| `EXP-CUST-PHONE-NUM-1` | `PIC X(15)` | Phone 1 |
| `EXP-CUST-PHONE-NUM-2` | `PIC X(15)` | Phone 2 |
| `EXP-CUST-SSN` | `PIC 9(09)` | SSN |
| `EXP-CUST-GOVT-ISSUED-ID` | `PIC X(20)` | Government ID |
| `EXP-CUST-DOB-YYYYMMDD` | `PIC X(10)` | Date of Birth |
| `EXP-CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | EFT Account |
| `EXP-CUST-PRI-HOLDER-IND` | `PIC X(01)` | Primary Holder |
| `EXP-CUST-FICO-SCORE` | `PIC 9(03)` | FICO Score |

**Account Export Fields** (when Record Type = 'A'):
| Field | PIC | Business Name |
|-------|-----|---------------|
| `EXP-ACCT-ID` | `PIC 9(11)` | Account ID |
| `EXP-ACCT-ACTIVE-STATUS` | `PIC X(01)` | Active Status |
| `EXP-ACCT-CURR-BAL` | `PIC S9(10)V99` | Current Balance |
| `EXP-ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | Credit Limit |
| `EXP-ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | Cash Limit |
| `EXP-ACCT-OPEN-DATE` | `PIC X(10)` | Open Date |
| `EXP-ACCT-EXPIRAION-DATE` | `PIC X(10)` | Expiration Date |
| `EXP-ACCT-REISSUE-DATE` | `PIC X(10)` | Reissue Date |
| `EXP-ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | Cycle Credit |
| `EXP-ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | Cycle Debit |
| `EXP-ACCT-GROUP-ID` | `PIC X(10)` | Group ID |

**Card Export Fields** (when Record Type = 'R'):
| Field | PIC | Business Name |
|-------|-----|---------------|
| `EXP-CARD-NUM` | `PIC X(16)` | Card Number |
| `EXP-CARD-ACCT-ID` | `PIC 9(11) COMP` | Account ID |
| `EXP-CARD-CVV-CD` | `PIC 9(03) COMP` | CVV Code |
| `EXP-CARD-EMBOSSED-NAME` | `PIC X(50)` | Embossed Name |
| `EXP-CARD-EXPIRAION-DATE` | `PIC X(10)` | Expiration Date |
| `EXP-CARD-ACTIVE-STATUS` | `PIC X(01)` | Active Status |

---

## 15. Report Structures

> **Copybook**: `CVTRA07Y.cpy` | Used by CBTRN03C for daily transaction reports

### Report Name Header
| Field | PIC | Value/Description |
|-------|-----|-------------------|
| `REPT-SHORT-NAME` | `PIC X(38)` | "DALYREPT" |
| `REPT-LONG-NAME` | `PIC X(41)` | "Daily Transaction Report" |
| `REPT-START-DATE` | `PIC X(10)` | Report start date |
| `REPT-END-DATE` | `PIC X(10)` | Report end date |

### Transaction Detail Line
| Field | PIC | Business Name |
|-------|-----|---------------|
| `TRAN-REPORT-TRANS-ID` | `PIC X(16)` | Transaction ID |
| `TRAN-REPORT-ACCOUNT-ID` | `PIC X(11)` | Account ID |
| `TRAN-REPORT-TYPE-CD` | `PIC X(02)` | Type Code |
| `TRAN-REPORT-TYPE-DESC` | `PIC X(15)` | Type Description |
| `TRAN-REPORT-CAT-CD` | `PIC 9(04)` | Category Code |
| `TRAN-REPORT-CAT-DESC` | `PIC X(29)` | Category Description |
| `TRAN-REPORT-SOURCE` | `PIC X(10)` | Source |
| `TRAN-REPORT-AMT` | `PIC -ZZZ,ZZZ,ZZZ.ZZ` | Formatted Amount |

### Report Totals
| Field | PIC | Description |
|-------|-----|-------------|
| `REPT-PAGE-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Page subtotal |
| `REPT-ACCOUNT-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Account subtotal |
| `REPT-GRAND-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Report grand total |

---

## 16. Common Communication Area

> **Copybook**: `COCOM01Y.cpy` | Used by all online CICS programs for inter-program communication

| # | COBOL Field | PIC Clause | Business Name | Description |
|---|------------|------------|---------------|-------------|
| 1 | `CDEMO-FROM-TRANID` | `PIC X(04)` | Source Transaction | Calling CICS transaction ID |
| 2 | `CDEMO-FROM-PROGRAM` | `PIC X(08)` | Source Program | Calling program name |
| 3 | `CDEMO-TO-TRANID` | `PIC X(04)` | Target Transaction | Target CICS transaction ID |
| 4 | `CDEMO-TO-PROGRAM` | `PIC X(08)` | Target Program | Target program name |
| 5 | `CDEMO-USER-ID` | `PIC X(08)` | User ID | Authenticated user |
| 6 | `CDEMO-USER-TYPE` | `PIC X(01)` | User Type | 'R'=Regular, 'A'=Admin |
| 7 | `CDEMO-PGM-CONTEXT` | `PIC 9(01)` | Context | Program context flag |

> **Note**: This COMMAREA is passed via CICS XCTL between all online programs. It maintains session state including authentication, navigation history, and data context.

---

## 17. Lookup Reference Data

> **Copybook**: `CSLKPCDY.cpy` | 1,318 lines | Hardcoded lookup tables

Contains embedded lookup tables for:

- **US State Codes**: All 50 states + DC + territories (e.g., "AL" → "Alabama")
- **Country Codes**: ISO 3166 country codes (e.g., "USA" → "United States")

**Modernization Note**: These hardcoded values should be migrated to a reference data table or enum in the modernized application.

---

## VSAM File Catalog

| VSAM Dataset Name | Record Length | Key | Key Length | Copybook | Business Entity |
|-------------------|-------------|-----|-----------|----------|-----------------|
| `ACCTDATA.VSAM.KSDS` | 300 | ACCT-ID | 11 | CVACT01Y | Account |
| `CARDDATA.VSAM.KSDS` | 150 | CARD-NUM | 16 | CVACT02Y | Card |
| `CUSTDATA.VSAM.KSDS` | 500 | CUST-ID | 9 | CVCUS01Y | Customer |
| `TRANSACT.VSAM.KSDS` | 350 | TRAN-ID | 16 | CVTRA05Y | Transaction |
| `DALYTRAN.VSAM.KSDS` | 350 | DALYTRAN-ID | 16 | CVTRA06Y | Daily Transaction |
| `CARDXREF.VSAM.KSDS` | ~36 | XREF-CARD-NUM | 16 | CVACT03Y | Card Cross-Reference |
| `USRSEC.VSAM.KSDS` | 80 | SEC-USR-ID | 8 | CSUSR01Y | User Security |
| `TRANTYPE.VSAM.KSDS` | 60 | TRAN-TYPE | 2 | CVTRA03Y | Transaction Type |
| `TRANCATG.VSAM.KSDS` | 60 | TRAN-TYPE-CD + TRAN-CAT-CD | 6 | CVTRA04Y | Transaction Category |
| `TCATBAL.VSAM.KSDS` | 50 | TRANCAT-ACCT-ID + TYPE + CD | 17 | CVTRA01Y | Category Balance |
| `DISCGRP.VSAM.KSDS` | 50 | DIS-ACCT-GROUP-ID + TYPE + CD | 16 | CVTRA02Y | Disclosure Group |

---

## Java Modernization Type Mapping

### Recommended JPA Entity Mapping

| COBOL PIC Clause | Java Type | JPA Annotation | Notes |
|-----------------|-----------|----------------|-------|
| `PIC X(n)` | `String` | `@Column(length=n)` | Trim trailing spaces |
| `PIC 9(n)` where n ≤ 9 | `int` | `@Column` | |
| `PIC 9(n)` where n > 9 | `long` | `@Column` | |
| `PIC S9(n)V99` | `BigDecimal` | `@Column(precision=n+2, scale=2)` | Use `BigDecimal` for all money |
| `PIC 9(n) COMP` | `int` / `long` | `@Column` | Binary storage in COBOL |
| `PIC X(10)` date fields | `LocalDate` | `@Column` | Parse YYYY-MM-DD |
| `PIC X(26)` timestamp | `LocalDateTime` | `@Column` | Parse ISO timestamp |
| `FILLER` | - | Not mapped | Ignore padding fields |

### Sensitive Data Handling

| Field | Current State | Modernization Requirement |
|-------|--------------|---------------------------|
| `SEC-USR-PWD` | Plaintext (8 chars) | bcrypt/scrypt hash, remove length limit |
| `CUST-SSN` | Plaintext numeric | Encrypt at rest (AES-256), mask in UI |
| `CARD-CVV-CD` | Plaintext numeric | Never store; use tokenization |
| `CARD-NUM` | Plaintext string | PCI-DSS: tokenize or encrypt, mask display |
| `CUST-GOVT-ISSUED-ID` | Plaintext string | Encrypt at rest, restrict access |
| `CUST-DOB-YYYYMMDD` | Plaintext date | PII: restrict access, audit logging |

### Proposed Entity Relationship Diagram (Logical)

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│   Customer   │1     N│  Cross-Ref   │N     1│   Account    │
│  (CVCUS01Y)  │───────│  (CVACT03Y)  │───────│  (CVACT01Y)  │
│              │       │              │       │              │
│ CUST-ID (PK) │       │ CARD-NUM(PK) │       │ ACCT-ID (PK) │
│ Name, Addr,  │       │ ACCT-ID (FK) │       │ Balance,     │
│ SSN, DOB     │       │ CUST-ID (FK) │       │ Credit Limit │
└──────────────┘       └──────┬───────┘       └──────┬───────┘
                              │                      │
                              │1                     │1
                              │                      │
                       ┌──────┴───────┐       ┌──────┴───────┐
                       │     Card     │       │  Category    │
                       │  (CVACT02Y)  │       │   Balance    │
                       │              │       │  (CVTRA01Y)  │
                       │ CARD-NUM(PK) │       │ ACCT+TYPE+CAT│
                       │ ACCT-ID (FK) │       │ Balance      │
                       └──────┬───────┘       └──────────────┘
                              │1
                              │
                              │N
                       ┌──────┴───────┐       ┌──────────────┐
                       │ Transaction  │N     1│  Tran Type   │
                       │  (CVTRA05Y)  │───────│  (CVTRA03Y)  │
                       │              │       │              │
                       │ TRAN-ID (PK) │       │ TYPE-CD (PK) │
                       │ CARD-NUM(FK) │       │ Description  │
                       │ Amount, Desc │       └──────┬───────┘
                       └──────────────┘              │1
                                                     │
                                              ┌──────┴───────┐
                                              │  Tran Cat    │
                                              │  (CVTRA04Y)  │
                                              │              │
                                              │ TYPE+CAT(PK) │
                                              │ Description  │
                                              └──────────────┘

┌──────────────┐       ┌──────────────┐
│ User Security│       │  Disclosure  │
│  (CSUSR01Y)  │       │    Group     │
│              │       │  (CVTRA02Y)  │
│ USR-ID (PK)  │       │ GRP+TYPE+CAT │
│ Name, Pwd,   │       │ Interest Rate│
│ Type (R/A)   │       └──────────────┘
└──────────────┘
```
