# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Source**: COBOL Copybooks (PIC clause analysis)
> **Application**: CardDemo - Mainframe Credit Card Management System

This document extracts every business entity from the COBOL copybook PIC clauses and presents them in a business-friendly format suitable for modernization planning.

---

## Table of Contents

- [1. Account Entity](#1-account-entity)
- [2. Credit Card Entity](#2-credit-card-entity)
- [3. Customer Entity](#3-customer-entity)
- [4. Card-Account Cross-Reference Entity](#4-card-account-cross-reference-entity)
- [5. Transaction Entity (Online)](#5-transaction-entity-online)
- [6. Daily Transaction Entity (Batch)](#6-daily-transaction-entity-batch)
- [7. Transaction Entity (Statement Layout)](#7-transaction-entity-statement-layout)
- [8. Transaction Category Balance Entity](#8-transaction-category-balance-entity)
- [9. Disclosure Group Entity](#9-disclosure-group-entity)
- [10. Transaction Type Entity](#10-transaction-type-entity)
- [11. Transaction Category Type Entity](#11-transaction-category-type-entity)
- [12. User Security Entity](#12-user-security-entity)
- [13. Report Layout Structures](#13-report-layout-structures)
- [14. Export Record Entity](#14-export-record-entity)
- [15. Communication Area (COMMAREA)](#15-communication-area-commarea)
- [16. IMS Pending Authorization Entities (Optional)](#16-ims-pending-authorization-entities-optional)
- [17. DB2 Transaction Type Entities (Optional)](#17-db2-transaction-type-entities-optional)
- [18. Data Type Reference](#18-data-type-reference)

---

## 1. Account Entity

**Copybook**: `CVACT01Y.cpy` | **Record**: `ACCT-RECORD` | **Length**: 300 bytes
**VSAM Dataset**: `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | **Key**: Account ID (11 bytes, pos 0)

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| ACCT-ID | `9(11)` | Numeric | 11 | Unique account identifier |
| ACCT-ACTIVE-STATUS | `X(01)` | Alpha | 1 | Account status flag (Y=Active) |
| ACCT-CURR-BAL | `S9(10)V99` | Signed Decimal | 12 | Current account balance |
| ACCT-CREDIT-LIMIT | `S9(10)V99` | Signed Decimal | 12 | Maximum credit limit |
| ACCT-CASH-CREDIT-LIMIT | `S9(10)V99` | Signed Decimal | 12 | Cash advance credit limit |
| ACCT-OPEN-DATE | `X(10)` | Date String | 10 | Account opening date |
| ACCT-EXPIRAION-DATE | `X(10)` | Date String | 10 | Account expiration date |
| ACCT-REISSUE-DATE | `X(10)` | Date String | 10 | Last card reissue date |
| ACCT-CURR-CYC-CREDIT | `S9(10)V99` | Signed Decimal | 12 | Current cycle credit total |
| ACCT-CURR-CYC-DEBIT | `S9(10)V99` | Signed Decimal | 12 | Current cycle debit total |
| ACCT-ADDR-ZIP | `X(10)` | Alpha | 10 | Account holder ZIP code |
| ACCT-GROUP-ID | `X(10)` | Alpha | 10 | Account group classification |
| FILLER | `X(178)` | Filler | 178 | Reserved space |

**Business Rules**:
- Primary key for all account-related lookups
- Balance fields use signed packed decimal for financial precision
- Dates stored as formatted strings (YYYY-MM-DD)

---

## 2. Credit Card Entity

**Copybook**: `CVACT02Y.cpy` | **Record**: `CARD-RECORD` | **Length**: 150 bytes
**VSAM Dataset**: `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` | **Key**: Card Number (16 bytes, pos 0)

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| CARD-NUM | `X(16)` | Alpha | 16 | Credit card number (primary key) |
| CARD-ACCT-ID | `9(11)` | Numeric | 11 | Linked account identifier |
| CARD-CVV-CD | `9(03)` | Numeric | 3 | Card verification value |
| CARD-EMBOSSED-NAME | `X(50)` | Alpha | 50 | Name embossed on card |
| CARD-EXPIRAION-DATE | `X(10)` | Date String | 10 | Card expiration date |
| CARD-ACTIVE-STATUS | `X(01)` | Alpha | 1 | Card status (Y=Active, N=Inactive) |
| FILLER | `X(59)` | Filler | 59 | Reserved space |

**Business Rules**:
- One account can have multiple cards (1:N relationship via CARD-ACCT-ID)
- Card number is the unique key for VSAM KSDS access
- CVV stored as numeric (modernization note: consider encryption)

---

## 3. Customer Entity

**Copybook**: `CVCUS01Y.cpy` | **Record**: `CUSTOMER-RECORD` | **Length**: 500 bytes
**VSAM Dataset**: `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` | **Key**: Customer ID (9 bytes, pos 0)

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| CUST-ID | `9(09)` | Numeric | 9 | Unique customer identifier |
| CUST-FIRST-NAME | `X(25)` | Alpha | 25 | Customer first name |
| CUST-MIDDLE-NAME | `X(25)` | Alpha | 25 | Customer middle name |
| CUST-LAST-NAME | `X(25)` | Alpha | 25 | Customer last name |
| CUST-ADDR-LINE-1 | `X(50)` | Alpha | 50 | Street address line 1 |
| CUST-ADDR-LINE-2 | `X(50)` | Alpha | 50 | Street address line 2 |
| CUST-ADDR-LINE-3 | `X(50)` | Alpha | 50 | Street address line 3 |
| CUST-ADDR-STATE-CD | `X(02)` | Alpha | 2 | State code (US 2-letter) |
| CUST-ADDR-COUNTRY-CD | `X(03)` | Alpha | 3 | Country code |
| CUST-ADDR-ZIP | `X(10)` | Alpha | 10 | ZIP/postal code |
| CUST-PHONE-NUM-1 | `X(15)` | Alpha | 15 | Primary phone number |
| CUST-PHONE-NUM-2 | `X(15)` | Alpha | 15 | Secondary phone number |
| CUST-SSN | `9(09)` | Numeric | 9 | Social Security Number |
| CUST-GOVT-ISSUED-ID | `X(20)` | Alpha | 20 | Government-issued ID |
| CUST-DOB-YYYYMMDD | `X(10)` | Date String | 10 | Date of birth |
| CUST-EFT-ACCOUNT-ID | `X(10)` | Alpha | 10 | EFT/bank account reference |
| CUST-PRI-CARD-HOLDER-IND | `X(01)` | Alpha | 1 | Primary cardholder indicator |
| CUST-FICO-CREDIT-SCORE | `9(03)` | Numeric | 3 | FICO credit score |
| FILLER | `X(168)` | Filler | 168 | Reserved space |

**Business Rules**:
- Contains PII fields (SSN, DOB, address) - requires special handling during modernization
- FICO score is a 3-digit numeric (300-850 range)
- Linked to accounts through the Cross-Reference entity

---

## 4. Card-Account Cross-Reference Entity

**Copybook**: `CVACT03Y.cpy` | **Record**: `CARD-XREF-RECORD` | **Length**: 50 bytes
**VSAM Dataset**: `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | **Key**: Card Number (16 bytes, pos 0)
**Alternate Index**: Account ID (11 bytes, pos 25) - Non-unique

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| XREF-CARD-NUM | `X(16)` | Alpha | 16 | Credit card number (primary key) |
| XREF-CUST-ID | `9(09)` | Numeric | 9 | Customer identifier (FK) |
| XREF-ACCT-ID | `9(11)` | Numeric | 11 | Account identifier (FK, AIX key) |
| FILLER | `X(14)` | Filler | 14 | Reserved space |

**Business Rules**:
- Central junction/bridge table linking Card, Customer, and Account
- Alternate index on ACCT-ID enables account-based lookups
- Critical for bill payment, statement, and transaction processing

---

## 5. Transaction Entity (Online)

**Copybook**: `CVTRA05Y.cpy` | **Record**: `TRAN-RECORD` | **Length**: 350 bytes
**VSAM Dataset**: `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | **Key**: Transaction ID (16 bytes, pos 0)

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| TRAN-ID | `X(16)` | Alpha | 16 | Unique transaction identifier |
| TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code (FK to TRAN-TYPE) |
| TRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| TRAN-SOURCE | `X(10)` | Alpha | 10 | Transaction source system |
| TRAN-DESC | `X(100)` | Alpha | 100 | Transaction description |
| TRAN-AMT | `S9(09)V99` | Signed Decimal | 11 | Transaction amount |
| TRAN-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant identifier |
| TRAN-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| TRAN-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| TRAN-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP code |
| TRAN-CARD-NUM | `X(16)` | Alpha | 16 | Card number used |
| TRAN-ORIG-TS | `X(26)` | Timestamp | 26 | Transaction origination timestamp |
| TRAN-PROC-TS | `X(26)` | Timestamp | 26 | Transaction processing timestamp |
| FILLER | `X(20)` | Filler | 20 | Reserved space |

**Business Rules**:
- Primary transaction record for both online and batch processing
- Amount field uses signed decimal to handle credits and debits
- Timestamps stored as 26-char strings (ISO-like format)
- Card number links to Card and Cross-Reference entities

---

## 6. Daily Transaction Entity (Batch)

**Copybook**: `CVTRA06Y.cpy` | **Record**: `DALYTRAN-RECORD` | **Length**: 350 bytes
**Dataset**: `AWS.M2.CARDDEMO.DALYTRAN.PS` (Sequential)

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| DALYTRAN-ID | `X(16)` | Alpha | 16 | Daily transaction identifier |
| DALYTRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| DALYTRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| DALYTRAN-SOURCE | `X(10)` | Alpha | 10 | Transaction source |
| DALYTRAN-DESC | `X(100)` | Alpha | 100 | Transaction description |
| DALYTRAN-AMT | `S9(09)V99` | Signed Decimal | 11 | Transaction amount |
| DALYTRAN-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant identifier |
| DALYTRAN-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| DALYTRAN-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| DALYTRAN-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP code |
| DALYTRAN-CARD-NUM | `X(16)` | Alpha | 16 | Card number used |
| DALYTRAN-ORIG-TS | `X(26)` | Timestamp | 26 | Origination timestamp |
| DALYTRAN-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| FILLER | `X(20)` | Filler | 20 | Reserved space |

**Business Rules**:
- Identical layout to TRAN-RECORD but held in flat sequential file
- Input to POSTTRAN job (CBTRN02C) which validates and posts to TRANSACT VSAM
- Rejected transactions written to DALYREJS file

---

## 7. Transaction Entity (Statement Layout)

**Copybook**: `COSTM01.CPY` | **Record**: `TRNX-RECORD` | **Length**: 350 bytes
**VSAM Dataset**: `AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS` | **Key**: Card Number + Transaction ID (32 bytes)

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| TRNX-CARD-NUM | `X(16)` | Alpha | 16 | Card number (part of composite key) |
| TRNX-ID | `X(16)` | Alpha | 16 | Transaction ID (part of composite key) |
| TRNX-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| TRNX-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| TRNX-SOURCE | `X(10)` | Alpha | 10 | Transaction source |
| TRNX-DESC | `X(100)` | Alpha | 100 | Transaction description |
| TRNX-AMT | `S9(09)V99` | Signed Decimal | 11 | Transaction amount |
| TRNX-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant identifier |
| TRNX-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| TRNX-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| TRNX-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP code |
| TRNX-ORIG-TS | `X(26)` | Timestamp | 26 | Origination timestamp |
| TRNX-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| FILLER | `X(20)` | Filler | 20 | Reserved space |

**Business Rules**:
- Reordered layout of transaction record for statement processing
- Composite key (Card + Tran ID) enables per-card statement generation
- Created by SORT step in CREASTMT JCL, consumed by CBSTM03A

---

## 8. Transaction Category Balance Entity

**Copybook**: `CVTRA01Y.cpy` | **Record**: `TRAN-CAT-BAL-RECORD` | **Length**: 50 bytes
**VSAM Dataset**: `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS`

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| TRANCAT-ACCT-ID | `9(11)` | Numeric | 11 | Account identifier (part of key) |
| TRANCAT-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code (part of key) |
| TRANCAT-CD | `9(04)` | Numeric | 4 | Transaction category code (part of key) |
| TRAN-CAT-BAL | `S9(09)V99` | Signed Decimal | 11 | Running balance for this category |
| FILLER | `X(22)` | Filler | 22 | Reserved space |

**Business Rules**:
- Composite key: Account + Type + Category
- Used by interest calculation (CBACT04C) to compute per-category balances
- Updated during transaction posting (CBTRN02C)

---

## 9. Disclosure Group Entity

**Copybook**: `CVTRA02Y.cpy` | **Record**: `DIS-GROUP-RECORD` | **Length**: 50 bytes
**VSAM Dataset**: `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| DIS-ACCT-GROUP-ID | `X(10)` | Alpha | 10 | Account group identifier (part of key) |
| DIS-TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code (part of key) |
| DIS-TRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code (part of key) |
| DIS-INT-RATE | `S9(04)V99` | Signed Decimal | 6 | Interest rate for this disclosure group |
| FILLER | `X(28)` | Filler | 28 | Reserved space |

**Business Rules**:
- Maps account groups to interest rates per transaction type and category
- Core input for interest calculation (CBACT04C)
- Rate stored with 2 decimal places (e.g., 18.99%)

---

## 10. Transaction Type Entity

**Copybook**: `CVTRA03Y.cpy` | **Record**: `TRAN-TYPE-RECORD` | **Length**: 60 bytes
**VSAM Dataset**: `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| TRAN-TYPE | `X(02)` | Alpha | 2 | Transaction type code (primary key) |
| TRAN-TYPE-DESC | `X(50)` | Alpha | 50 | Transaction type description |
| FILLER | `X(08)` | Filler | 8 | Reserved space |

**Business Rules**:
- Reference/lookup table for transaction type codes
- Examples: "SA" = Sale, "RT" = Return, etc.
- Managed via DB2 in the optional Transaction Type Management module

---

## 11. Transaction Category Type Entity

**Copybook**: `CVTRA04Y.cpy` | **Record**: `TRAN-CAT-RECORD` | **Length**: 60 bytes
**VSAM Dataset**: `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code (part of composite key) |
| TRAN-CAT-CD | `9(04)` | Numeric | 4 | Category code (part of composite key) |
| TRAN-CAT-TYPE-DESC | `X(50)` | Alpha | 50 | Category description |
| FILLER | `X(04)` | Filler | 4 | Reserved space |

**Business Rules**:
- Subcategorization within each transaction type
- Composite key: Type Code + Category Code
- Used in reporting and interest calculation

---

## 12. User Security Entity

**Copybook**: `CSUSR01Y.cpy` | **Record**: `SEC-USER-DATA` | **Length**: 80 bytes
**VSAM Dataset**: `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` | **Key**: User ID (8 bytes, pos 0)

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| SEC-USR-ID | `X(08)` | Alpha | 8 | User login identifier (primary key) |
| SEC-USR-FNAME | `X(20)` | Alpha | 20 | User first name |
| SEC-USR-LNAME | `X(20)` | Alpha | 20 | User last name |
| SEC-USR-PWD | `X(08)` | Alpha | 8 | User password (plain text) |
| SEC-USR-TYPE | `X(01)` | Alpha | 1 | User type (A=Admin, U=Regular User) |
| SEC-USR-FILLER | `X(23)` | Filler | 23 | Reserved space |

**Business Rules**:
- Controls application access via signon screen (COSGN00C)
- Password stored in plain text (modernization priority: hash/encrypt)
- Admin users (type 'A') access admin menu; regular users (type 'U') access user menu
- Default accounts: ADMIN001/PASSWORD, USER0001/PASSWORD

---

## 13. Report Layout Structures

**Copybook**: `CVTRA07Y.cpy` | Used by: CBTRN03C (Daily Transaction Report)

### Report Header

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| REPT-SHORT-NAME | `X(38)` | Alpha | 38 | Report short name ("DALYREPT") |
| REPT-LONG-NAME | `X(41)` | Alpha | 41 | Report title ("Daily Transaction Report") |
| REPT-DATE-HEADER | `X(12)` | Alpha | 12 | Date range label |
| REPT-START-DATE | `X(10)` | Alpha | 10 | Report start date |
| REPT-END-DATE | `X(10)` | Alpha | 10 | Report end date |

### Transaction Detail Line

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| TRAN-REPORT-TRANS-ID | `X(16)` | Alpha | 16 | Transaction ID |
| TRAN-REPORT-ACCOUNT-ID | `X(11)` | Alpha | 11 | Account ID |
| TRAN-REPORT-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| TRAN-REPORT-TYPE-DESC | `X(15)` | Alpha | 15 | Type description |
| TRAN-REPORT-CAT-CD | `9(04)` | Numeric | 4 | Category code |
| TRAN-REPORT-CAT-DESC | `X(29)` | Alpha | 29 | Category description |
| TRAN-REPORT-SOURCE | `X(10)` | Alpha | 10 | Transaction source |
| TRAN-REPORT-AMT | `-ZZZ,ZZZ,ZZZ.ZZ` | Edited Numeric | 16 | Formatted transaction amount |

### Report Totals

| Field Name | PIC Clause | Business Description |
|-----------|-----------|---------------------|
| REPT-PAGE-TOTAL | `+ZZZ,ZZZ,ZZZ.ZZ` | Page subtotal |
| REPT-ACCOUNT-TOTAL | `+ZZZ,ZZZ,ZZZ.ZZ` | Per-account total |
| REPT-GRAND-TOTAL | `+ZZZ,ZZZ,ZZZ.ZZ` | Grand total across all accounts |

---

## 14. Export Record Entity

**Copybook**: `CVEXPORT.cpy` | **Record**: `EXPORT-RECORD` | **Length**: 500+ bytes
**Dataset**: `AWS.M2.CARDDEMO.EXPORT.DATA.PS`

This copybook uses REDEFINES to overlay multiple entity types into a single export record format:

| Overlay | Prefix | Entity | Key Fields |
|---------|--------|--------|-----------|
| Account | EXP-ACCT- | Account data | ACCT-ID (9(11)) |
| Card | EXP-CARD- | Card data | CARD-NUM (X(16)), CARD-ACCT-ID (9(11) COMP) |
| Customer | EXP-CUST- | Customer data | CUST-ID (9(09)) |
| Transaction | EXP-TRAN- | Transaction data | TRAN-ID (X(16)) |

**Notable Technical Details**:
- Uses COMP (binary) for some numeric fields (e.g., `EXP-CARD-ACCT-ID PIC 9(11) COMP`)
- This differs from the standard VSAM layouts which use display numerics
- REDEFINES pattern enables multi-entity export through a single file

---

## 15. Communication Area (COMMAREA)

**Copybook**: `COCOM01Y.cpy` | Used by: All CICS programs

The COMMAREA is the inter-program communication structure passed between CICS transactions via XCTL and RETURN TRANSID.

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| CDEMO-FROM-TRANID | `X(04)` | Alpha | 4 | Originating transaction ID |
| CDEMO-FROM-PROGRAM | `X(08)` | Alpha | 8 | Originating program name |
| CDEMO-TO-TRANID | `X(04)` | Alpha | 4 | Target transaction ID |
| CDEMO-TO-PROGRAM | `X(08)` | Alpha | 8 | Target program name |
| CDEMO-USER-ID | `X(08)` | Alpha | 8 | Current logged-in user |
| CDEMO-USER-TYPE | `X(01)` | Alpha | 1 | User type (A=Admin, U=User) |
| CDEMO-PGM-CONTEXT | `9(01)` | Numeric | 1 | Program context flags |

**Business Rules**:
- Every CICS screen program reads/writes this area
- Navigation is driven by TO-PROGRAM and TO-TRANID fields
- User context (ID, type) flows through the entire session

---

## 16. IMS Pending Authorization Entities (Optional)

### Authorization Summary Segment

**Copybook**: `CIPAUSMY.cpy` | **IMS DB**: PSBPAUTB

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| PAUS-CARD-NUM | `X(16)` | Alpha | 16 | Card number |
| PAUS-TRAN-AMT | `S9(09)V99 COMP-3` | Packed Decimal | 6 | Authorization amount |
| PAUS-TRAN-DATE | `X(08)` | Alpha | 8 | Authorization date |
| PAUS-TRAN-TIME | `X(06)` | Alpha | 6 | Authorization time |
| PAUS-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant ID |
| PAUS-STATUS | `X(01)` | Alpha | 1 | Auth status (P=Pending, A=Approved, D=Denied) |

### Authorization Detail Segment

**Copybook**: `CIPAUDTY.cpy` | **IMS DB**: PSBPAUTB

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| PAUD-CARD-NUM | `X(16)` | Alpha | 16 | Card number |
| PAUD-AUTH-CODE | `X(06)` | Alpha | 6 | Authorization response code |
| PAUD-REASON-CD | `X(04)` | Alpha | 4 | Decision reason code |
| PAUD-FRAUD-FLAG | `X(01)` | Alpha | 1 | Fraud indicator |

### IMS PCB Structures

| Copybook | PCB Name | Fields | Description |
|----------|----------|--------|-------------|
| PAUTBPCB.CPY | PAUTBPCB | DBD name, segment level, status, key feedback | Auth base database PCB |
| PADFLPCB.CPY | PADFLPCB | DBD name, segment level, status, key feedback | Detail flat file PCB |
| PASFLPCB.CPY | PASFLPCB | DBD name, segment level, status, key feedback | Summary flat file PCB |

### Authorization Request/Reply (MQ Messages)

| Copybook | Record | Description |
|----------|--------|-------------|
| CCPAURQY.cpy | AUTH-REQUEST | MQ authorization request message |
| CCPAURLY.cpy | AUTH-REPLY | MQ authorization reply message |
| CCPAUERY.cpy | AUTH-ERROR | MQ authorization error response |

---

## 17. DB2 Transaction Type Entities (Optional)

### DB2 Common Working Storage

**Copybook**: `CSDB2RWY.cpy`

| Field Name | PIC Clause | Type | Length | Business Description |
|-----------|-----------|------|-------:|---------------------|
| WS-DISP-SQLCODE | `----9` | Edited Numeric | 5 | Display format for SQLCODE |
| WS-DUMMY-DB2-INT | `S9(4) COMP-3` | Packed Decimal | 3 | DB2 connectivity test field |
| WS-DB2-PROCESSING-FLAG | `X(1)` | Alpha | 1 | DB2 status (0=OK, 1=Error) |
| WS-DB2-CURRENT-ACTION | `X(72)` | Alpha | 72 | Current DB2 action description |
| WS-DSNTIAC-FORMATTED | (group) | Group | 720+ | DSNTIAC formatted error messages |

### DB2 Tables (from DDL)

**Transaction Type Table** (`TRNTYPE.ddl`):
- Maps to `TRAN-TYPE-RECORD` layout
- Primary key: TRAN_TYPE_CD

**Transaction Category Table** (`TRNTYCAT.ddl`):
- Maps to `TRAN-CAT-RECORD` layout
- Primary key: TRAN_TYPE_CD + TRAN_CAT_CD

**Authorization Fraud Table** (`AUTHFRDS.ddl`):
- Logs fraud-flagged authorization decisions
- Linked to IMS authorization data

---

## 18. Data Type Reference

### COBOL PIC Clause to Business Type Mapping

| PIC Pattern | COBOL Type | Business Type | Java Equivalent | Notes |
|------------|-----------|--------------|----------------|-------|
| `X(n)` | Alphanumeric | Text/String | `String` | Fixed-length, space-padded |
| `9(n)` | Numeric Display | Integer | `long` / `int` | Unsigned numeric |
| `S9(n)V99` | Signed Decimal | Currency/Amount | `BigDecimal` | 2 decimal places implied |
| `S9(n) COMP` | Binary | Integer | `int` / `long` | Mainframe binary format |
| `S9(n) COMP-3` | Packed Decimal | Currency/Amount | `BigDecimal` | BCD encoding, half-byte per digit |
| `9(n) COMP` | Unsigned Binary | Integer | `int` | Used in export records |
| `-ZZZ,ZZZ,ZZZ.ZZ` | Edited Numeric | Display Amount | `String` (formatted) | Report output only |
| `+ZZZ,ZZZ,ZZZ.ZZ` | Edited Numeric | Display Amount | `String` (formatted) | Report totals |

### VSAM Dataset Summary

| Dataset Name | Key | Key Length | Record Length | Entity | Access Method |
|-------------|-----|----------:|-------------:|--------|--------------|
| ACCTDATA.VSAM.KSDS | ACCT-ID | 11 | 300 | Account | KSDS |
| CARDDATA.VSAM.KSDS | CARD-NUM | 16 | 150 | Card | KSDS |
| CUSTDATA.VSAM.KSDS | CUST-ID | 9 | 500 | Customer | KSDS |
| CARDXREF.VSAM.KSDS | XREF-CARD-NUM | 16 | 50 | Cross-Reference | KSDS + AIX |
| TRANSACT.VSAM.KSDS | TRAN-ID | 16 | 350 | Transaction | KSDS |
| USRSEC.VSAM.KSDS | SEC-USR-ID | 8 | 80 | User Security | KSDS |
| TCATBALF.VSAM.KSDS | Composite | 17 | 50 | Category Balance | KSDS |
| DISCGRP.VSAM.KSDS | Composite | 16 | 50 | Disclosure Group | KSDS |
| TRANTYPE.VSAM.KSDS | TRAN-TYPE | 2 | 60 | Transaction Type | KSDS |
| TRANCATG.VSAM.KSDS | Composite | 6 | 60 | Transaction Category | KSDS |

### Entity Relationship Summary

```
Customer (1) ──────┐
                    ├──> Cross-Reference (N) ──> Card (1)
Account  (1) ──────┘                              │
    │                                              │
    ├──> Category Balance (N)                      │
    │                                              │
    └──> Disclosure Group (N)                      │
                                                   │
Transaction (N) <──────────────────────────────────┘
    │
    ├──> Transaction Type (1)
    └──> Transaction Category (1)

User Security ── independent (authentication only)
```
