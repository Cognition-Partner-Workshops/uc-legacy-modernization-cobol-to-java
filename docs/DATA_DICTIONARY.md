# CardDemo Data Dictionary

> **Generated from:** static analysis of copybook PIC clauses in the CardDemo COBOL codebase
>
> **Purpose:** Business-friendly reference mapping COBOL record layouts to logical data entities

---

## Table of Contents

1. [Account Entity](#1-account-entity)
2. [Card Entity](#2-card-entity)
3. [Customer Entity](#3-customer-entity)
4. [Transaction Entity](#4-transaction-entity)
5. [Daily Transaction Entity](#5-daily-transaction-entity)
6. [Transaction Category Balance Entity](#6-transaction-category-balance-entity)
7. [Disclosure Group Entity](#7-disclosure-group-entity)
8. [Transaction Type Entity](#8-transaction-type-entity)
9. [Transaction Category Entity](#9-transaction-category-entity)
10. [Card Cross-Reference Entity](#10-card-cross-reference-entity)
11. [User Security Entity](#11-user-security-entity)
12. [Common Communication Area](#12-common-communication-area)
13. [Export/Import Record Layouts](#13-exportimport-record-layouts)
14. [Authorization Module Entities](#14-authorization-module-entities)
15. [Report Layouts](#15-report-layouts)
16. [VSAM File Summary](#16-vsam-file-summary)

---

## 1. Account Entity

**Copybook:** `CVACT01Y.cpy` | **Record Length:** 300 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description |
|------------|-----------|------|-----:|----------------------|
| ACCT-ID | 9(11) | Numeric | 11 | Unique account identifier |
| ACCT-ACTIVE-STATUS | X(01) | Alpha | 1 | Account status (`Y`=Active, `N`=Inactive) |
| ACCT-CURR-BAL | S9(10)V99 | Signed Decimal | 12 | Current account balance (2 decimal places) |
| ACCT-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12 | Maximum credit limit |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12 | Maximum cash advance limit |
| ACCT-OPEN-DATE | X(10) | Date String | 10 | Date account was opened (YYYY-MM-DD) |
| ACCT-EXPIRAION-DATE | X(10) | Date String | 10 | Account expiration date |
| ACCT-REISSUE-DATE | X(10) | Date String | 10 | Last card reissue date |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | Signed Decimal | 12 | Credits in current billing cycle |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | Signed Decimal | 12 | Debits in current billing cycle |
| ACCT-ADDR-ZIP | X(10) | Alpha | 10 | Account holder ZIP code |
| ACCT-GROUP-ID | X(10) | Alpha | 10 | Disclosure/rate group identifier |
| FILLER | X(178) | Filler | 178 | Reserved space |

**Key:** `ACCT-ID` (primary, VSAM KSDS key)

---

## 2. Card Entity

**Copybook:** `CVACT02Y.cpy` | **Record Length:** 150 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description |
|------------|-----------|------|-----:|----------------------|
| CARD-NUM | X(16) | Alpha | 16 | Credit card number (primary key) |
| CARD-ACCT-ID | 9(11) | Numeric | 11 | Parent account identifier (FK to Account) |
| CARD-CVV-CD | 9(03) | Numeric | 3 | Card verification value (CVV) |
| CARD-EMBOSSED-NAME | X(50) | Alpha | 50 | Name embossed on the card |
| CARD-EXPIRAION-DATE | X(10) | Date String | 10 | Card expiration date |
| CARD-ACTIVE-STATUS | X(01) | Alpha | 1 | Card status (`Y`=Active, `N`=Inactive) |
| FILLER | X(59) | Filler | 59 | Reserved space |

**Key:** `CARD-NUM` (primary, VSAM KSDS key)

---

## 3. Customer Entity

**Copybook:** `CVCUS01Y.cpy` | **Record Length:** 500 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description |
|------------|-----------|------|-----:|----------------------|
| CUST-ID | 9(09) | Numeric | 9 | Unique customer identifier |
| CUST-FIRST-NAME | X(25) | Alpha | 25 | Customer first name |
| CUST-MIDDLE-NAME | X(25) | Alpha | 25 | Customer middle name |
| CUST-LAST-NAME | X(25) | Alpha | 25 | Customer last name |
| CUST-ADDR-LINE-1 | X(50) | Alpha | 50 | Street address line 1 |
| CUST-ADDR-LINE-2 | X(50) | Alpha | 50 | Street address line 2 |
| CUST-ADDR-LINE-3 | X(50) | Alpha | 50 | Street address line 3 |
| CUST-ADDR-STATE-CD | X(02) | Alpha | 2 | US state code |
| CUST-ADDR-COUNTRY-CD | X(03) | Alpha | 3 | Country code |
| CUST-ADDR-ZIP | X(10) | Alpha | 10 | ZIP/postal code |
| CUST-PHONE-NUM-1 | X(15) | Alpha | 15 | Primary phone number |
| CUST-PHONE-NUM-2 | X(15) | Alpha | 15 | Secondary phone number |
| CUST-SSN | 9(09) | Numeric | 9 | Social Security Number |
| CUST-GOVT-ISSUED-ID | X(20) | Alpha | 20 | Government-issued ID number |
| CUST-DOB-YYYYMMDD | X(10) | Date String | 10 | Date of birth |
| CUST-EFT-ACCOUNT-ID | X(10) | Alpha | 10 | Electronic fund transfer account ID |
| CUST-PRI-CARD-HOLDER-IND | X(01) | Alpha | 1 | Primary cardholder indicator (`Y`/`N`) |
| CUST-FICO-CREDIT-SCORE | 9(03) | Numeric | 3 | FICO credit score (0-999) |
| FILLER | X(168) | Filler | 168 | Reserved space |

**Key:** `CUST-ID` (primary, VSAM KSDS key)

---

## 4. Transaction Entity

**Copybook:** `CVTRA05Y.cpy` | **Record Length:** 350 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description |
|------------|-----------|------|-----:|----------------------|
| TRAN-ID | X(16) | Alpha | 16 | Unique transaction identifier |
| TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code (FK to Transaction Type) |
| TRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| TRAN-SOURCE | X(10) | Alpha | 10 | Transaction source (POS, ATM, Online, etc.) |
| TRAN-DESC | X(100) | Alpha | 100 | Transaction description |
| TRAN-AMT | S9(09)V99 | Signed Decimal | 11 | Transaction amount (2 decimal places) |
| TRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant identifier |
| TRAN-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| TRAN-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| TRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP code |
| TRAN-CARD-NUM | X(16) | Alpha | 16 | Card number used (FK to Card) |
| TRAN-ORIG-TS | X(26) | Timestamp | 26 | Original transaction timestamp |
| TRAN-PROC-TS | X(26) | Timestamp | 26 | Processing timestamp |
| FILLER | X(20) | Filler | 20 | Reserved space |

**Key:** `TRAN-ID` (primary, VSAM KSDS key)

---

## 5. Daily Transaction Entity

**Copybook:** `CVTRA06Y.cpy` | **Record Length:** 350 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.DALYTRAN.VSAM.KSDS`

Identical structure to the Transaction Entity but used for the current day's unposted transactions.

| Field Name | COBOL PIC | Type | Size | Business Description |
|------------|-----------|------|-----:|----------------------|
| DALYTRAN-ID | X(16) | Alpha | 16 | Daily transaction identifier |
| DALYTRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| DALYTRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| DALYTRAN-SOURCE | X(10) | Alpha | 10 | Transaction source |
| DALYTRAN-DESC | X(100) | Alpha | 100 | Transaction description |
| DALYTRAN-AMT | S9(09)V99 | Signed Decimal | 11 | Transaction amount |
| DALYTRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant identifier |
| DALYTRAN-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| DALYTRAN-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| DALYTRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP code |
| DALYTRAN-CARD-NUM | X(16) | Alpha | 16 | Card number used |
| DALYTRAN-ORIG-TS | X(26) | Timestamp | 26 | Original transaction timestamp |
| DALYTRAN-PROC-TS | X(26) | Timestamp | 26 | Processing timestamp |
| FILLER | X(20) | Filler | 20 | Reserved space |

**Key:** `DALYTRAN-ID` (primary)

---

## 6. Transaction Category Balance Entity

**Copybook:** `CVTRA01Y.cpy` | **Record Length:** 50 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description |
|------------|-----------|------|-----:|----------------------|
| TRANCAT-ACCT-ID | 9(11) | Numeric | 11 | Account ID (FK) |
| TRANCAT-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| TRANCAT-CD | 9(04) | Numeric | 4 | Category code |
| TRAN-CAT-BAL | S9(09)V99 | Signed Decimal | 11 | Running balance for this category |
| FILLER | X(22) | Filler | 22 | Reserved space |

**Key:** Composite (`TRANCAT-ACCT-ID` + `TRANCAT-TYPE-CD` + `TRANCAT-CD`)

---

## 7. Disclosure Group Entity

**Copybook:** `CVTRA02Y.cpy` | **Record Length:** 50 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description |
|------------|-----------|------|-----:|----------------------|
| DIS-ACCT-GROUP-ID | X(10) | Alpha | 10 | Disclosure group identifier |
| DIS-TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| DIS-TRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| DIS-INT-RATE | S9(04)V99 | Signed Decimal | 6 | Interest rate for this group/type combination |
| FILLER | X(28) | Filler | 28 | Reserved space |

**Key:** Composite (`DIS-ACCT-GROUP-ID` + `DIS-TRAN-TYPE-CD` + `DIS-TRAN-CAT-CD`)

**Business Rule:** Links account groups to interest rates by transaction type and category.

---

## 8. Transaction Type Entity

**Copybook:** `CVTRA03Y.cpy` | **Record Length:** 60 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description |
|------------|-----------|------|-----:|----------------------|
| TRAN-TYPE | X(02) | Alpha | 2 | Transaction type code (e.g., "01"=Purchase, "02"=Return) |
| TRAN-TYPE-DESC | X(50) | Alpha | 50 | Human-readable description of the type |
| FILLER | X(08) | Filler | 8 | Reserved space |

**Key:** `TRAN-TYPE`

---

## 9. Transaction Category Entity

**Copybook:** `CVTRA04Y.cpy` | **Record Length:** 60 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description |
|------------|-----------|------|-----:|----------------------|
| TRAN-TYPE-CD | X(02) | Alpha | 2 | Parent transaction type code |
| TRAN-CAT-CD | 9(04) | Numeric | 4 | Category code within the type |
| TRAN-CAT-TYPE-DESC | X(50) | Alpha | 50 | Category description |
| FILLER | X(04) | Filler | 4 | Reserved space |

**Key:** Composite (`TRAN-TYPE-CD` + `TRAN-CAT-CD`)

---

## 10. Card Cross-Reference Entity

**Copybook:** `CVACT03Y.cpy` | **Record Length:** 50 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description |
|------------|-----------|------|-----:|----------------------|
| XREF-CARD-NUM | X(16) | Alpha | 16 | Card number |
| XREF-CUST-ID | 9(09) | Numeric | 9 | Customer ID (FK to Customer) |
| XREF-ACCT-ID | 9(11) | Numeric | 11 | Account ID (FK to Account) |
| FILLER | X(14) | Filler | 14 | Reserved space |

**Key:** `XREF-CARD-NUM` (primary) | **Alternate Index:** `XREF-ACCT-ID` (non-unique)

**Business Rule:** This is the central cross-reference linking cards to both their owning customer and account. It enables lookups in both directions.

---

## 11. User Security Entity

**Copybook:** `CSUSR01Y.cpy` | **Record Length:** 80 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description |
|------------|-----------|------|-----:|----------------------|
| SEC-USR-ID | X(08) | Alpha | 8 | User login ID (e.g., "ADMIN001") |
| SEC-USR-FNAME | X(20) | Alpha | 20 | User first name |
| SEC-USR-LNAME | X(20) | Alpha | 20 | User last name |
| SEC-USR-PWD | X(08) | Alpha | 8 | User password (plaintext) |
| SEC-USR-TYPE | X(01) | Alpha | 1 | User type (`A`=Admin, `U`=Regular User) |
| SEC-USR-FILLER | X(23) | Filler | 23 | Reserved space |

**Key:** `SEC-USR-ID` (primary)

**Security Note:** Passwords are stored in plaintext -- a key modernization concern.

---

## 12. Common Communication Area

**Copybook:** `COCOM01Y.cpy` | Used by all online CICS programs for inter-program communication.

| Field Name | COBOL PIC | Type | Size | Business Description |
|------------|-----------|------|-----:|----------------------|
| CDEMO-FROM-TRANID | X(04) | Alpha | 4 | Originating CICS transaction ID |
| CDEMO-FROM-PROGRAM | X(08) | Alpha | 8 | Originating program name |
| CDEMO-TO-TRANID | X(04) | Alpha | 4 | Target CICS transaction ID |
| CDEMO-TO-PROGRAM | X(08) | Alpha | 8 | Target program name |
| CDEMO-PGM-REENTER | X(01) | Flag | 1 | Re-entry flag for pseudoconversational logic |
| CDEMO-USR-ID | X(08) | Alpha | 8 | Currently signed-on user ID |
| CDEMO-USR-TYP | X(01) | Alpha | 1 | Current user type (`A`/`U`) |
| CDEMO-PGM-CONTEXT | X(256) | Alpha | 256 | Program-specific context data area |

---

## 13. Export/Import Record Layouts

**Copybook:** `CVEXPORT.cpy` | Used by CBEXPORT and CBIMPORT for data migration.

Defines flat-file record formats for each entity during export/import operations:

| Export Record | Maps To | Key Fields |
|--------------|---------|------------|
| EXP-ACCT-RECORD | Account | ACCT-ID, balances, dates, limits |
| EXP-CUST-RECORD | Customer | CUST-ID, name, address, SSN, FICO |
| EXP-CARD-RECORD | Card | CARD-NUM, ACCT-ID, CVV, expiry |
| EXP-TRAN-RECORD | Transaction | TRAN-ID, amounts, merchant, timestamps |
| EXP-XREF-RECORD | Cross-Reference | CARD-NUM, CUST-ID, ACCT-ID |

Each record mirrors its VSAM counterpart in a fixed-length flat-file format suitable for mainframe data transfer.

---

## 14. Authorization Module Entities

### 14.1 Authorization Request

**Copybook:** `CCPAURQY.cpy`

| Field Name | Type | Business Description |
|------------|------|----------------------|
| APTS-REQUEST-ID | Alpha | Unique authorization request ID |
| APTS-CARD-NUM | Alpha(16) | Card number being authorized |
| APTS-TRAN-AMT | Decimal | Requested transaction amount |
| APTS-TRAN-TYPE | Alpha(2) | Transaction type code |
| APTS-MERCHANT-ID | Numeric | Merchant identifier |
| APTS-TIMESTAMP | Timestamp | Request timestamp |

### 14.2 Authorization Reply

**Copybook:** `CCPAURLY.cpy`

| Field Name | Type | Business Description |
|------------|------|----------------------|
| APTS-REPLY-CODE | Alpha(2) | Authorization response code |
| APTS-REASON-CODE | Alpha(4) | Decline reason code |
| APTS-AUTH-CODE | Alpha(6) | Authorization approval code |

### 14.3 Authorization Summary / Detail

**Copybooks:** `CIPAUSMY.cpy`, `CIPAUDTY.cpy`

Used by the authorization summary and detail view screens (COPAUS0C, COPAUS1C) to display pending authorization messages from the IMS database.

### 14.4 IMS PCB Layouts

**Copybooks:** `PADFLPCB.CPY`, `PASFLPCB.CPY`, `PAUTBPCB.CPY`

IMS Program Communication Blocks defining the database interface:

| PCB | Database | Purpose |
|-----|----------|---------|
| PADFLPCB | PADFL | Authorization detail flat segments |
| PASFLPCB | PASFL | Authorization summary flat segments |
| PAUTBPCB | PAUT | Authorization transaction base |

### 14.5 DB2 Common Layouts

**Copybooks:** `CSDB2RWY.cpy` (working storage), `CSDB2RPY.cpy` (procedures)

Shared DB2 infrastructure used by the transaction type DB2 module:
- SQLCODE display formatting
- DSNTIAC error message construction
- DB2 connectivity verification (priming query)

---

## 15. Report Layouts

**Copybook:** `CVTRA07Y.cpy` | Used by CBTRN03C for the daily transaction report.

| Layout | Fields | Purpose |
|--------|--------|---------|
| REPORT-NAME-HEADER | Short name, long name, date range | Report identification header |
| TRANSACTION-DETAIL-REPORT | Trans ID, Account ID, Type, Category, Source, Amount | One line per transaction |
| TRANSACTION-HEADER-1/2 | Column headers and separator | Report column headings |
| REPORT-PAGE-TOTALS | Page total amount | Running page total |
| REPORT-ACCOUNT-TOTALS | Account total amount | Running account total |
| REPORT-GRAND-TOTALS | Grand total amount | Overall report total |

---

## 16. VSAM File Summary

| VSAM Dataset | Key | Record Size | Entity | Access Method |
|-------------|-----|------------:|--------|---------------|
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | ACCT-ID (11 bytes) | 300 | Account | KSDS |
| AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | CARD-NUM (16 bytes) | 150 | Card | KSDS |
| AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | CUST-ID (9 bytes) | 500 | Customer | KSDS |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | TRAN-ID (16 bytes) | 350 | Transaction | KSDS |
| AWS.M2.CARDDEMO.DALYTRAN.VSAM.KSDS | DALYTRAN-ID (16 bytes) | 350 | Daily Transaction | KSDS |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | XREF-CARD-NUM (16 bytes) | 50 | Card Cross-Ref | KSDS + AIX |
| AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | SEC-USR-ID (8 bytes) | 80 | User Security | KSDS |
| AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | Composite (17 bytes) | 50 | Category Balance | KSDS |
| AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | Composite (16 bytes) | 50 | Disclosure Group | KSDS |
| AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | TRAN-TYPE (2 bytes) | 60 | Transaction Type | KSDS |
| AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | Composite (6 bytes) | 60 | Transaction Category | KSDS |

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)
    |
    |-- 1:N --> Card Cross-Reference (CVACT03Y)
    |               |
    |               |-- N:1 --> Account (CVACT01Y)
    |               |               |
    |               |               |-- 1:1 --> Disclosure Group (CVTRA02Y)
    |               |               |
    |               |               |-- 1:N --> Category Balance (CVTRA01Y)
    |               |
    |               |-- N:1 --> Card (CVACT02Y)
    |                               |
    |                               |-- 1:N --> Transaction (CVTRA05Y)
    |                               |
    |                               |-- 1:N --> Daily Transaction (CVTRA06Y)
    |
    Transaction Type (CVTRA03Y)
        |
        |-- 1:N --> Transaction Category (CVTRA04Y)

    User Security (CSUSR01Y)  [independent entity]
```
