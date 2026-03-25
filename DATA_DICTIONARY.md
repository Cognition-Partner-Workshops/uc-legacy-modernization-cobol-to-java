# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Application**: CardDemo (Credit Card Management System)
> **Source**: Copybook PIC clause analysis from `app/cpy/` and optional module copybooks

---

## Table of Contents

1. [Overview](#overview)
2. [Account Entity (CVACT01Y)](#account-entity-cvact01y)
3. [Credit Card Entity (CVACT02Y)](#credit-card-entity-cvact02y)
4. [Card-Account Cross-Reference (CVACT03Y)](#card-account-cross-reference-cvact03y)
5. [Customer Entity (CVCUS01Y)](#customer-entity-cvcus01y)
6. [Transaction Entity (CVTRA05Y)](#transaction-entity-cvtra05y)
7. [Daily Transaction Entity (CVTRA06Y)](#daily-transaction-entity-cvtra06y)
8. [Transaction Category Balance (CVTRA01Y)](#transaction-category-balance-cvtra01y)
9. [Disclosure Group (CVTRA02Y)](#disclosure-group-cvtra02y)
10. [Transaction Type (CVTRA03Y)](#transaction-type-cvtra03y)
11. [Transaction Category (CVTRA04Y)](#transaction-category-cvtra04y)
12. [User Security Record (CSUSR01Y)](#user-security-record-csusr01y)
13. [Statement Transaction Record (COSTM01)](#statement-transaction-record-costm01)
14. [Export Record (CVEXPORT)](#export-record-cvexport)
15. [Report Structures (CVTRA07Y)](#report-structures-cvtra07y)
16. [Common Communication Area (COCOM01Y)](#common-communication-area-cocom01y)
17. [Authorization Module Entities](#authorization-module-entities)
18. [VSAM File Summary](#vsam-file-summary)
19. [Java Modernization Mapping](#java-modernization-mapping)

---

## Overview

CardDemo uses **VSAM KSDS** (Key-Sequenced Data Sets) as its primary data store. Each VSAM file corresponds to a copybook that defines its record layout. This dictionary translates the COBOL PIC clauses into business-friendly field descriptions with data types suitable for Java/relational database migration.

**PIC Clause Reference**:
- `PIC X(n)` = Alphanumeric string, n characters
- `PIC 9(n)` = Unsigned numeric, n digits
- `PIC S9(n)V99` = Signed numeric with 2 decimal places
- `PIC S9(n) COMP` = Binary integer (computational)
- `FILLER` = Reserved/padding bytes

---

## Account Entity (CVACT01Y)

**Copybook**: `app/cpy/CVACT01Y.cpy` | **Record Length**: 300 bytes
**VSAM File**: `ACCTDAT` (ACCTDATA.VSAM.KSDS) | **Key**: Account ID (11 digits)
**Used By**: 16 programs | **Business Domain**: Account Management

| COBOL Field | PIC Clause | Offset | Length | Business Name | Data Type (Java) | Description | Constraints |
|-------------|-----------|--------|--------|---------------|-------------------|-------------|-------------|
| ACCT-ID | 9(11) | 0 | 11 | Account ID | long | Unique account identifier | PK, non-zero |
| ACCT-ACTIVE-STATUS | X(01) | 11 | 1 | Account Status | String | Active/inactive flag | 'Y' or 'N' |
| ACCT-CURR-BAL | S9(10)V99 | 12 | 12 | Current Balance | BigDecimal | Current account balance | Signed decimal |
| ACCT-CREDIT-LIMIT | S9(10)V99 | 24 | 12 | Credit Limit | BigDecimal | Maximum credit allowed | Signed decimal |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | 36 | 12 | Cash Advance Limit | BigDecimal | Maximum cash advance | Signed decimal |
| ACCT-OPEN-DATE | X(10) | 48 | 10 | Account Open Date | LocalDate | Date account was opened | YYYY-MM-DD |
| ACCT-EXPIRAION-DATE | X(10) | 58 | 10 | Expiration Date | LocalDate | Account expiration date | YYYY-MM-DD |
| ACCT-REISSUE-DATE | X(10) | 68 | 10 | Reissue Date | LocalDate | Last card reissue date | YYYY-MM-DD |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | 78 | 12 | Cycle Credits | BigDecimal | Credits in current cycle | Signed decimal |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | 90 | 12 | Cycle Debits | BigDecimal | Debits in current cycle | Signed decimal |
| ACCT-GROUP-ID | X(10) | 102 | 10 | Account Group | String | Disclosure/rate group | FK to DISCGRP |
| ACCT-FICO-CREDIT-SCORE | 9(03) | 112 | 3 | FICO Score | int | Credit score (300-850) | 300-850 range |
| FILLER | X(168) | 115 | 168 | Reserved | — | Padding to 300 bytes | — |

---

## Credit Card Entity (CVACT02Y)

**Copybook**: `app/cpy/CVACT02Y.cpy` | **Record Length**: 150 bytes
**VSAM File**: `CARDDAT` (CARDDATA.VSAM.KSDS) | **Key**: Card Number (16 digits)
**Alternate Index**: `CARDAIX` (by Account ID)
**Used By**: 10 programs | **Business Domain**: Card Management

| COBOL Field | PIC Clause | Offset | Length | Business Name | Data Type (Java) | Description | Constraints |
|-------------|-----------|--------|--------|---------------|-------------------|-------------|-------------|
| CARD-NUM | X(16) | 0 | 16 | Card Number | String | Credit card number | PK, 16 digits |
| CARD-ACCT-ID | 9(11) | 16 | 11 | Account ID | long | Owning account | FK to ACCTDAT |
| CARD-CVV-CD | 9(03) | 27 | 3 | CVV Code | String | Card verification value | 3 digits |
| CARD-EMBOSSED-NAME | X(50) | 30 | 50 | Cardholder Name | String | Name printed on card | — |
| CARD-EXPIRAION-DATE | X(10) | 80 | 10 | Expiration Date | LocalDate | Card expiry date | YYYY-MM-DD |
| CARD-ACTIVE-STATUS | X(01) | 90 | 1 | Card Status | String | Active/inactive flag | 'Y' or 'N' |
| FILLER | X(59) | 91 | 59 | Reserved | — | Padding to 150 bytes | — |

---

## Card-Account Cross-Reference (CVACT03Y)

**Copybook**: `app/cpy/CVACT03Y.cpy` | **Record Length**: ~36 bytes
**VSAM File**: `CARDXREF` (CARDXREF.VSAM.KSDS) | **Key**: Card Number (16 digits)
**Alternate Index**: `CXACAIX` (by Account ID)
**Used By**: 16 programs | **Business Domain**: Card-Account Linkage

| COBOL Field | PIC Clause | Offset | Length | Business Name | Data Type (Java) | Description |
|-------------|-----------|--------|--------|---------------|-------------------|-------------|
| XREF-CARD-NUM | X(16) | 0 | 16 | Card Number | String | Credit card number (PK) |
| XREF-CUST-ID | 9(09) | 16 | 9 | Customer ID | long | Owning customer |
| XREF-ACCT-ID | 9(11) | 25 | 11 | Account ID | long | Associated account |

**Business Purpose**: Links cards to accounts and customers. The primary key is the card number; alternate index allows lookup by account ID.

---

## Customer Entity (CVCUS01Y)

**Copybook**: `app/cpy/CVCUS01Y.cpy` | **Record Length**: 500 bytes
**VSAM File**: `CUSTDAT` (CUSTDATA.VSAM.KSDS) | **Key**: Customer ID (9 digits)
**Used By**: 10 programs | **Business Domain**: Customer Management

| COBOL Field | PIC Clause | Offset | Length | Business Name | Data Type (Java) | Description |
|-------------|-----------|--------|--------|---------------|-------------------|-------------|
| CUST-ID | 9(09) | 0 | 9 | Customer ID | long | Unique customer identifier (PK) |
| CUST-FIRST-NAME | X(25) | 9 | 25 | First Name | String | Customer first name |
| CUST-MIDDLE-NAME | X(25) | 34 | 25 | Middle Name | String | Customer middle name |
| CUST-LAST-NAME | X(25) | 59 | 25 | Last Name | String | Customer last name |
| CUST-ADDR-LINE-1 | X(50) | 84 | 50 | Address Line 1 | String | Street address |
| CUST-ADDR-LINE-2 | X(50) | 134 | 50 | Address Line 2 | String | Apt/Suite/PO Box |
| CUST-ADDR-LINE-3 | X(50) | 184 | 50 | Address Line 3 | String | Additional address |
| CUST-ADDR-STATE-CD | X(02) | 234 | 2 | State Code | String | US state abbreviation |
| CUST-ADDR-COUNTRY-CD | X(03) | 236 | 3 | Country Code | String | Country code |
| CUST-ADDR-ZIP | X(10) | 239 | 10 | ZIP Code | String | Postal code (ZIP+4) |
| CUST-PHONE-NUM-1 | X(15) | 249 | 15 | Primary Phone | String | Primary contact number |
| CUST-PHONE-NUM-2 | X(15) | 264 | 15 | Secondary Phone | String | Alternate contact |
| CUST-SSN | 9(09) | 279 | 9 | SSN | String | Social Security Number (PII) |
| CUST-GOVT-ISSUED-ID | X(20) | 288 | 20 | Government ID | String | Government-issued ID number |
| CUST-DOB-YYYYMMDD | X(10) | 308 | 10 | Date of Birth | LocalDate | Customer DOB |
| CUST-EFT-ACCOUNT-ID | X(10) | 318 | 10 | EFT Account | String | Electronic funds transfer acct |
| CUST-PRI-CARD-HOLDER-IND | X(01) | 328 | 1 | Primary Cardholder | String | Primary holder flag ('Y'/'N') |
| CUST-FICO-CREDIT-SCORE | 9(03) | 329 | 3 | FICO Score | int | Customer credit score (300-850) |
| FILLER | X(168) | 332 | 168 | Reserved | — | Padding to 500 bytes |

**PII Fields**: SSN (`CUST-SSN`), DOB (`CUST-DOB-YYYYMMDD`), Government ID (`CUST-GOVT-ISSUED-ID`). These require encryption and masking in modernized application.

---

## Transaction Entity (CVTRA05Y)

**Copybook**: `app/cpy/CVTRA05Y.cpy` | **Record Length**: 350 bytes
**VSAM File**: `TRANSACT` (TRANSACT.VSAM.KSDS) | **Key**: Transaction ID (16 chars)
**Used By**: 11 programs | **Business Domain**: Transaction Processing

| COBOL Field | PIC Clause | Offset | Length | Business Name | Data Type (Java) | Description |
|-------------|-----------|--------|--------|---------------|-------------------|-------------|
| TRAN-ID | X(16) | 0 | 16 | Transaction ID | String | Unique transaction identifier (PK) |
| TRAN-TYPE-CD | X(02) | 16 | 2 | Transaction Type | String | Type code (FK to TRANTYPE) |
| TRAN-CAT-CD | 9(04) | 18 | 4 | Category Code | int | Category (FK to TRANCATG) |
| TRAN-SOURCE | X(10) | 22 | 10 | Source | String | Transaction source/channel |
| TRAN-DESC | X(100) | 32 | 100 | Description | String | Transaction description |
| TRAN-AMT | S9(09)V99 | 132 | 11 | Amount | BigDecimal | Transaction amount (signed) |
| TRAN-MERCHANT-ID | 9(09) | 143 | 9 | Merchant ID | long | Merchant identifier |
| TRAN-MERCHANT-NAME | X(50) | 152 | 50 | Merchant Name | String | Merchant business name |
| TRAN-MERCHANT-CITY | X(50) | 202 | 50 | Merchant City | String | Merchant city |
| TRAN-MERCHANT-ZIP | X(10) | 252 | 10 | Merchant ZIP | String | Merchant postal code |
| TRAN-CARD-NUM | X(16) | 262 | 16 | Card Number | String | Card used (FK to CARDDAT) |
| TRAN-ORIG-TS | X(26) | 278 | 26 | Origination Timestamp | LocalDateTime | When transaction originated |
| TRAN-PROC-TS | X(26) | 304 | 26 | Processing Timestamp | LocalDateTime | When transaction was processed |
| FILLER | X(20) | 330 | 20 | Reserved | — | Padding to 350 bytes |

---

## Daily Transaction Entity (CVTRA06Y)

**Copybook**: `app/cpy/CVTRA06Y.cpy` | **Record Length**: 350 bytes
**VSAM File**: `DALYTRAN` (DALYTRAN.VSAM.KSDS) | **Key**: Daily Transaction ID
**Used By**: 2 programs (CBTRN01C, CBTRN02C) | **Business Domain**: Batch Transaction Input

| COBOL Field | PIC Clause | Offset | Length | Business Name | Data Type (Java) | Description |
|-------------|-----------|--------|--------|---------------|-------------------|-------------|
| DALYTRAN-ID | X(16) | 0 | 16 | Daily Transaction ID | String | Unique daily transaction ID (PK) |
| DALYTRAN-TYPE-CD | X(02) | 16 | 2 | Transaction Type | String | Type code |
| DALYTRAN-CAT-CD | 9(04) | 18 | 4 | Category Code | int | Category code |
| DALYTRAN-SOURCE | X(10) | 22 | 10 | Source | String | Transaction source |
| DALYTRAN-DESC | X(100) | 32 | 100 | Description | String | Transaction description |
| DALYTRAN-AMT | S9(09)V99 | 132 | 11 | Amount | BigDecimal | Transaction amount |
| DALYTRAN-MERCHANT-ID | 9(09) | 143 | 9 | Merchant ID | long | Merchant identifier |
| DALYTRAN-MERCHANT-NAME | X(50) | 152 | 50 | Merchant Name | String | Merchant business name |
| DALYTRAN-MERCHANT-CITY | X(50) | 202 | 50 | Merchant City | String | Merchant city |
| DALYTRAN-MERCHANT-ZIP | X(10) | 252 | 10 | Merchant ZIP | String | Merchant postal code |
| DALYTRAN-CARD-NUM | X(16) | 262 | 16 | Card Number | String | Card used |
| DALYTRAN-ORIG-TS | X(26) | 278 | 26 | Origination Timestamp | LocalDateTime | When originated |
| DALYTRAN-PROC-TS | X(26) | 304 | 26 | Processing Timestamp | LocalDateTime | When processed |
| FILLER | X(20) | 330 | 20 | Reserved | — | Padding to 350 bytes |

**Note**: Identical layout to CVTRA05Y. Daily transactions are staged in DALYTRAN, then posted to TRANSACT by the batch posting job (CBTRN02C).

---

## Transaction Category Balance (CVTRA01Y)

**Copybook**: `app/cpy/CVTRA01Y.cpy` | **Record Length**: 50 bytes
**VSAM File**: `TCATBALF` (TCATBAL.VSAM.KSDS) | **Key**: Account ID + Type Code + Category Code
**Used By**: 2 programs | **Business Domain**: Financial Aggregation

| COBOL Field | PIC Clause | Offset | Length | Business Name | Data Type (Java) | Description |
|-------------|-----------|--------|--------|---------------|-------------------|-------------|
| TRANCAT-ACCT-ID | 9(11) | 0 | 11 | Account ID | long | Account (part of composite key) |
| TRANCAT-TYPE-CD | X(02) | 11 | 2 | Type Code | String | Transaction type (part of key) |
| TRANCAT-CD | 9(04) | 13 | 4 | Category Code | int | Category (part of key) |
| TRAN-CAT-BAL | S9(09)V99 | 17 | 11 | Category Balance | BigDecimal | Running balance for this category |
| FILLER | X(22) | 28 | 22 | Reserved | — | Padding to 50 bytes |

---

## Disclosure Group (CVTRA02Y)

**Copybook**: `app/cpy/CVTRA02Y.cpy` | **Record Length**: 50 bytes
**VSAM File**: `DISCGRP` (DISCGRP.VSAM.KSDS) | **Key**: Group ID + Type Code + Category Code
**Used By**: 1 program (CBACT04C) | **Business Domain**: Interest Rate Configuration

| COBOL Field | PIC Clause | Offset | Length | Business Name | Data Type (Java) | Description |
|-------------|-----------|--------|--------|---------------|-------------------|-------------|
| DIS-ACCT-GROUP-ID | X(10) | 0 | 10 | Account Group ID | String | Disclosure group identifier (part of key) |
| DIS-TRAN-TYPE-CD | X(02) | 10 | 2 | Transaction Type | String | Type code (part of key) |
| DIS-TRAN-CAT-CD | 9(04) | 12 | 4 | Category Code | int | Category code (part of key) |
| DIS-INT-RATE | S9(04)V99 | 16 | 6 | Interest Rate | BigDecimal | Interest rate percentage |
| FILLER | X(28) | 22 | 28 | Reserved | — | Padding to 50 bytes |

---

## Transaction Type (CVTRA03Y)

**Copybook**: `app/cpy/CVTRA03Y.cpy` | **Record Length**: 60 bytes
**VSAM File**: `TRANTYPE` (TRANTYPE.VSAM.KSDS) | **Key**: Transaction Type (2 chars)
**Used By**: 1 program (CBTRN03C) | **Business Domain**: Reference Data

| COBOL Field | PIC Clause | Offset | Length | Business Name | Data Type (Java) | Description |
|-------------|-----------|--------|--------|---------------|-------------------|-------------|
| TRAN-TYPE | X(02) | 0 | 2 | Type Code | String | Transaction type code (PK) |
| TRAN-TYPE-DESC | X(50) | 2 | 50 | Type Description | String | Human-readable type name |
| FILLER | X(08) | 52 | 8 | Reserved | — | Padding to 60 bytes |

---

## Transaction Category (CVTRA04Y)

**Copybook**: `app/cpy/CVTRA04Y.cpy` | **Record Length**: 60 bytes
**VSAM File**: `TRANCATG` (TRANCATG.VSAM.KSDS) | **Key**: Type Code + Category Code
**Used By**: 1 program (CBTRN03C) | **Business Domain**: Reference Data

| COBOL Field | PIC Clause | Offset | Length | Business Name | Data Type (Java) | Description |
|-------------|-----------|--------|--------|---------------|-------------------|-------------|
| TRAN-TYPE-CD | X(02) | 0 | 2 | Type Code | String | Transaction type (part of composite key) |
| TRAN-CAT-CD | 9(04) | 2 | 4 | Category Code | int | Category code (part of key) |
| TRAN-CAT-TYPE-DESC | X(50) | 6 | 50 | Category Description | String | Human-readable category name |
| FILLER | X(04) | 56 | 4 | Reserved | — | Padding to 60 bytes |

---

## User Security Record (CSUSR01Y)

**Copybook**: `app/cpy/CSUSR01Y.cpy` | **Record Length**: ~80 bytes
**VSAM File**: `USRSEC` (USRSEC.VSAM.KSDS) | **Key**: User ID (8 chars)
**Used By**: 14 programs | **Business Domain**: Security / Authentication

| COBOL Field | PIC Clause | Offset | Length | Business Name | Data Type (Java) | Description |
|-------------|-----------|--------|--------|---------------|-------------------|-------------|
| SEC-USR-ID | X(08) | 0 | 8 | User ID | String | Login user ID (PK) |
| SEC-USR-FNAME | X(20) | 8 | 20 | First Name | String | User first name |
| SEC-USR-LNAME | X(20) | 28 | 20 | Last Name | String | User last name |
| SEC-USR-PWD | X(08) | 48 | 8 | Password | String | User password (plaintext!) |
| SEC-USR-TYPE | X(01) | 56 | 1 | User Type | String | 'A' = Admin, 'U' = Regular |
| SEC-USR-FILLER | X(23) | 57 | 23 | Reserved | — | Padding |

**Security Note**: Passwords are stored in plaintext. Modernized application must implement proper password hashing (e.g., BCrypt).

---

## Statement Transaction Record (COSTM01)

**Copybook**: `app/cpy/COSTM01.CPY` | **Record Length**: ~350 bytes
**Used By**: CBSTM03A (statement generation) | **Business Domain**: Statement Processing

| COBOL Field | PIC Clause | Length | Business Name | Data Type (Java) | Description |
|-------------|-----------|--------|---------------|-------------------|-------------|
| TRNX-CARD-NUM | X(16) | 16 | Card Number | String | Card number (part of key) |
| TRNX-ID | X(16) | 16 | Transaction ID | String | Transaction ID (part of key) |
| TRNX-TYPE-CD | X(02) | 2 | Type Code | String | Transaction type |
| TRNX-CAT-CD | 9(04) | 4 | Category Code | int | Transaction category |
| TRNX-SOURCE | X(10) | 10 | Source | String | Transaction source |
| TRNX-DESC | X(100) | 100 | Description | String | Transaction description |
| TRNX-AMT | S9(09)V99 | 11 | Amount | BigDecimal | Transaction amount |
| TRNX-MERCHANT-ID | 9(09) | 9 | Merchant ID | long | Merchant identifier |
| TRNX-MERCHANT-NAME | X(50) | 50 | Merchant Name | String | Merchant name |
| TRNX-MERCHANT-CITY | X(50) | 50 | Merchant City | String | Merchant city |
| TRNX-MERCHANT-ZIP | X(10) | 10 | Merchant ZIP | String | Merchant postal code |
| TRNX-ORIG-TS | X(26) | 26 | Origination Timestamp | LocalDateTime | When originated |
| TRNX-PROC-TS | X(26) | 26 | Processing Timestamp | LocalDateTime | When processed |
| FILLER | X(20) | 20 | Reserved | — | Padding |

**Note**: Key is composite (Card Number + Transaction ID) to enable card-ordered statement generation.

---

## Export Record (CVEXPORT)

**Copybook**: `app/cpy/CVEXPORT.cpy` | **Used By**: CBEXPORT, CBIMPORT
**Business Domain**: Data Migration / ETL

Contains record layouts for exporting/importing all entity types in a unified flat file format. Each export record is typed (Customer, Account, Card, Transaction, Cross-Reference) with a record-type indicator.

| Record Type | Prefix Fields | Embedded Layout |
|-------------|--------------|-----------------|
| Customer | EXP-CUST-* | Mirrors CVCUS01Y fields |
| Account | EXP-ACCT-* | Mirrors CVACT01Y fields |
| Card | EXP-CARD-* | Mirrors CVACT02Y fields |
| Transaction | EXP-TRAN-* | Mirrors CVTRA05Y fields |
| Cross-Reference | EXP-XREF-* | Mirrors CVACT03Y fields |

---

## Report Structures (CVTRA07Y)

**Copybook**: `app/cpy/CVTRA07Y.cpy` | **Used By**: CBTRN03C
**Business Domain**: Transaction Reporting

| Structure | Fields | Purpose |
|-----------|--------|---------|
| REPORT-NAME-HEADER | REPT-SHORT-NAME, REPT-LONG-NAME, date range | Report header with date range |
| TRANSACTION-DETAIL-REPORT | Trans ID, Account ID, Type, Category, Source, Amount | One line per transaction |
| TRANSACTION-HEADER-1/2 | Column headers and separator | Report column headers |
| REPORT-PAGE-TOTALS | REPT-PAGE-TOTAL | Page subtotal line |
| REPORT-ACCOUNT-TOTALS | REPT-ACCOUNT-TOTAL | Account subtotal line |
| REPORT-GRAND-TOTALS | REPT-GRAND-TOTAL | Grand total line |

Amount format: `+ZZZ,ZZZ,ZZZ.ZZ` (signed with comma separators)

---

## Common Communication Area (COCOM01Y)

**Copybook**: `app/cpy/COCOM01Y.cpy` | **Used By**: 21 programs
**Business Domain**: Inter-Program Communication (CICS COMMAREA)

This is the backbone data structure passed between all CICS transactions via the COMMAREA.

| COBOL Field | PIC Clause | Business Name | Description |
|-------------|-----------|---------------|-------------|
| CDEMO-FROM-TRANID | X(04) | Source Transaction | Transaction ID that sent control |
| CDEMO-FROM-PROGRAM | X(08) | Source Program | Program that sent control |
| CDEMO-TO-TRANID | X(04) | Target Transaction | Next transaction to invoke |
| CDEMO-TO-PROGRAM | X(08) | Target Program | Next program to invoke |
| CDEMO-USER-ID | X(08) | Current User | Logged-in user ID |
| CDEMO-USER-TYPE | X(01) | User Type | 'A' = Admin, 'U' = Regular |
| CDEMO-PGM-CONTEXT | 9(01) | Program Context | 0 = Enter, 1 = Reenter |
| CDEMO-ACCT-ID | 9(11) | Account ID | Current account context |
| CDEMO-CARD-NUM | X(16) | Card Number | Current card context |
| CDEMO-LAST-MAP | X(07) | Last Map | Last BMS map displayed |
| CDEMO-LAST-MAPSET | X(07) | Last Mapset | Last BMS mapset used |
| CCARD-AID-* | X(01) | AID Key Flags | Mapped PF key indicators |
| CCARD-ERROR-MSG | X(75) | Error Message | Error message for display |

**Modernization**: This COMMAREA maps to an HTTP session object or JWT token payload in a Java web application.

---

## Authorization Module Entities

### Authorization Summary (CIPAUSMY)

**IMS Database Segment** | **Module**: app-authorization-ims-db2-mq

| COBOL Field | PIC Clause | Business Name | Description |
|-------------|-----------|---------------|-------------|
| PA-SUM-KEY | X(16) | Authorization Key | Composite key for summary |
| PA-SUM-CARD-NUM | X(16) | Card Number | Card being authorized |
| PA-SUM-ACCT-ID | 9(11) | Account ID | Associated account |
| PA-SUM-AUTH-AMT | S9(09)V99 | Authorization Amount | Requested amount |
| PA-SUM-AUTH-STATUS | X(02) | Status | AP=Approved, DN=Denied, PD=Pending |
| PA-SUM-AUTH-TS | X(26) | Timestamp | Authorization timestamp |

### Authorization Detail (CIPAUDTY)

**IMS Database Segment** | **Module**: app-authorization-ims-db2-mq

| COBOL Field | PIC Clause | Business Name | Description |
|-------------|-----------|---------------|-------------|
| PA-DTL-KEY | X(16) | Detail Key | Detail record key |
| PA-DTL-MERCHANT-ID | 9(09) | Merchant ID | Merchant identifier |
| PA-DTL-MERCHANT-NAME | X(50) | Merchant Name | Merchant business name |
| PA-DTL-FRAUD-FLAG | X(01) | Fraud Flag | 'Y' if marked as fraud |

---

## VSAM File Summary

| DD Name | VSAM Dataset | Key Field | Record Length | Entity | Access Pattern |
|---------|-------------|-----------|---------------|--------|----------------|
| USRSEC | USRSEC.VSAM.KSDS | User ID (8) | ~80 | User Security | KSDS by User ID |
| ACCTDAT | ACCTDATA.VSAM.KSDS | Account ID (11) | 300 | Account | KSDS by Account ID |
| CARDDAT | CARDDATA.VSAM.KSDS | Card Number (16) | 150 | Credit Card | KSDS by Card Number |
| CARDAIX | CARDDATA.VSAM.AIX | Account ID (11) | 150 | Credit Card | AIX by Account ID |
| CUSTDAT | CUSTDATA.VSAM.KSDS | Customer ID (9) | 500 | Customer | KSDS by Customer ID |
| CARDXREF | CARDXREF.VSAM.KSDS | Card Number (16) | ~36 | Cross-Reference | KSDS by Card Number |
| CXACAIX | CARDXREF.VSAM.AIX | Account ID (11) | ~36 | Cross-Reference | AIX by Account ID |
| TRANSACT | TRANSACT.VSAM.KSDS | Transaction ID (16) | 350 | Transaction | KSDS by Trans ID |
| DALYTRAN | DALYTRAN.VSAM.KSDS | Daily Trans ID (16) | 350 | Daily Transaction | KSDS (batch input) |
| TRANTYPE | TRANTYPE.VSAM.KSDS | Type Code (2) | 60 | Transaction Type | KSDS (reference) |
| TRANCATG | TRANCATG.VSAM.KSDS | Type+Cat Code (6) | 60 | Transaction Category | KSDS (reference) |
| TCATBALF | TCATBAL.VSAM.KSDS | Acct+Type+Cat (17) | 50 | Category Balance | KSDS (aggregation) |
| DISCGRP | DISCGRP.VSAM.KSDS | Group+Type+Cat (16) | 50 | Disclosure Group | KSDS (reference) |

---

## Java Modernization Mapping

| COBOL Entity | Java Class | JPA Entity | DB Table | Notes |
|-------------|------------|------------|----------|-------|
| CVACT01Y (Account) | `Account.java` | `@Entity` | `accounts` | PK: account_id (BIGINT) |
| CVACT02Y (Card) | `CreditCard.java` | `@Entity` | `credit_cards` | PK: card_number (VARCHAR 16) |
| CVACT03Y (Xref) | `CardAccountXref.java` | `@Entity` | `card_account_xref` | Or use `@ManyToOne` relationships |
| CVCUS01Y (Customer) | `Customer.java` | `@Entity` | `customers` | PK: customer_id (BIGINT), encrypt SSN |
| CVTRA05Y (Transaction) | `Transaction.java` | `@Entity` | `transactions` | PK: transaction_id (VARCHAR 16) |
| CVTRA06Y (Daily Tran) | `DailyTransaction.java` | `@Entity` | `daily_transactions` | Staging table for batch |
| CVTRA01Y (Cat Balance) | `CategoryBalance.java` | `@Entity` | `category_balances` | Composite PK |
| CVTRA02Y (Disclosure) | `DisclosureGroup.java` | `@Entity` | `disclosure_groups` | Composite PK, interest rates |
| CVTRA03Y (Tran Type) | `TransactionType.java` | `@Entity` | `transaction_types` | PK: type_code (VARCHAR 2) |
| CVTRA04Y (Tran Cat) | `TransactionCategory.java` | `@Entity` | `transaction_categories` | Composite PK |
| CSUSR01Y (User) | `AppUser.java` | `@Entity` | `app_users` | Hash passwords, add roles |
| COCOM01Y (Commarea) | `SessionContext.java` | N/A | N/A | Map to HTTP session or JWT |

### Type Mapping Reference

| COBOL PIC | Java Type | SQL Type | Notes |
|-----------|-----------|----------|-------|
| PIC X(n) | String | VARCHAR(n) | Trim trailing spaces |
| PIC 9(n) | long / int | BIGINT / INT | Use long for n > 9 |
| PIC S9(n)V99 | BigDecimal | DECIMAL(n+2, 2) | Always use BigDecimal for money |
| PIC S9(n) COMP | int / long | INT / BIGINT | Binary representation |
| PIC X(10) date | LocalDate | DATE | Parse YYYY-MM-DD |
| PIC X(26) timestamp | LocalDateTime | TIMESTAMP | Parse ISO-like format |
