# DATA DICTIONARY — CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Source:** Copybook PIC clause analysis from `app/cpy/`

---

## Table of Contents

1. [Overview](#overview)
2. [Account Entity](#1-account-entity)
3. [Card Entity](#2-card-entity)
4. [Customer Entity](#3-customer-entity)
5. [Transaction Entity](#4-transaction-entity)
6. [Daily Transaction Entity](#5-daily-transaction-entity)
7. [Card Cross-Reference Entity](#6-card-cross-reference-entity)
8. [Transaction Category Balance Entity](#7-transaction-category-balance-entity)
9. [Disclosure Group Entity](#8-disclosure-group-entity)
10. [Transaction Type Entity](#9-transaction-type-entity)
11. [Transaction Category Entity](#10-transaction-category-entity)
12. [User Security Entity](#11-user-security-entity)
13. [Communication Area (COMMAREA)](#12-communication-area-commarea)
14. [Export/Import Record](#13-exportimport-record)
15. [Report Structures](#14-report-structures)
16. [VSAM File Catalog](#vsam-file-catalog)

---

## Overview

The CardDemo application stores all persistent data in **VSAM KSDS** (Key-Sequenced Data Sets) files on the mainframe. Each VSAM file has a corresponding copybook that defines its record layout. This dictionary translates those COBOL PIC clauses into business-friendly descriptions.

**PIC Clause Quick Reference:**
| PIC Pattern | Meaning | Example |
|---|---|---|
| `PIC X(n)` | Alphanumeric, n characters | `PIC X(25)` → 25-char string |
| `PIC 9(n)` | Numeric unsigned, n digits | `PIC 9(11)` → 11-digit number |
| `PIC S9(n)V99` | Signed numeric with 2 decimals | `PIC S9(09)V99` → ±999,999,999.99 |
| `FILLER` | Unused padding bytes | Reserved for future use |

---

## 1. Account Entity

**Copybook:** `CVACT01Y.cpy` | **Record Length:** 300 bytes | **VSAM File:** `ACCTDATA.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|---|---|---|---|---|
| `ACCT-ID` | `PIC 9(11)` | Numeric | 11 digits | **Primary Key.** Unique account identifier |
| `ACCT-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 char | Account status: `Y` = Active, `N` = Inactive |
| `ACCT-CURR-BAL` | `PIC S9(10)V99` | Decimal | 12 digits | Current account balance (signed, 2 decimal places) |
| `ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | Decimal | 12 digits | Maximum credit limit |
| `ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | Decimal | 12 digits | Cash advance credit limit |
| `ACCT-OPEN-DATE` | `PIC X(10)` | Alpha | 10 chars | Account opening date (YYYY-MM-DD) |
| `ACCT-EXPIRAION-DATE` | `PIC X(10)` | Alpha | 10 chars | Account expiration date (YYYY-MM-DD) |
| `ACCT-REISSUE-DATE` | `PIC X(10)` | Alpha | 10 chars | Card reissue date |
| `ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | Decimal | 12 digits | Current cycle credit total |
| `ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | Decimal | 12 digits | Current cycle debit total |
| `ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 chars | Account group classification |
| `FILLER` | `PIC X(178)` | — | 178 bytes | Reserved |

---

## 2. Card Entity

**Copybook:** `CVACT02Y.cpy` | **Record Length:** 150 bytes | **VSAM File:** `CARDDATA.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|---|---|---|---|---|
| `CARD-NUM` | `PIC X(16)` | Alpha | 16 chars | **Primary Key.** Credit card number |
| `CARD-ACCT-ID` | `PIC 9(11)` | Numeric | 11 digits | **Foreign Key** → Account. Associated account ID |
| `CARD-CVV-CD` | `PIC 9(03)` | Numeric | 3 digits | Card verification value (CVV) |
| `CARD-EMBOSSED-NAME` | `PIC X(50)` | Alpha | 50 chars | Name embossed on card |
| `CARD-EXPIRAION-DATE` | `PIC X(10)` | Alpha | 10 chars | Card expiration date |
| `CARD-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 char | Card status: `Y` = Active, `N` = Inactive |
| `FILLER` | `PIC X(59)` | — | 59 bytes | Reserved |

---

## 3. Customer Entity

**Copybook:** `CVCUS01Y.cpy` | **Record Length:** 500 bytes | **VSAM File:** `CUSTDATA.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|---|---|---|---|---|
| `CUST-ID` | `PIC 9(09)` | Numeric | 9 digits | **Primary Key.** Unique customer identifier |
| `CUST-FIRST-NAME` | `PIC X(25)` | Alpha | 25 chars | Customer first name |
| `CUST-MIDDLE-NAME` | `PIC X(25)` | Alpha | 25 chars | Customer middle name |
| `CUST-LAST-NAME` | `PIC X(25)` | Alpha | 25 chars | Customer last name |
| `CUST-ADDR-LINE-1` | `PIC X(50)` | Alpha | 50 chars | Address line 1 |
| `CUST-ADDR-LINE-2` | `PIC X(50)` | Alpha | 50 chars | Address line 2 |
| `CUST-ADDR-LINE-3` | `PIC X(50)` | Alpha | 50 chars | Address line 3 |
| `CUST-ADDR-STATE-CD` | `PIC X(02)` | Alpha | 2 chars | State code |
| `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | Alpha | 3 chars | Country code |
| `CUST-ADDR-ZIP` | `PIC X(10)` | Alpha | 10 chars | Postal/ZIP code |
| `CUST-PHONE-NUM-1` | `PIC X(15)` | Alpha | 15 chars | Primary phone number |
| `CUST-PHONE-NUM-2` | `PIC X(15)` | Alpha | 15 chars | Secondary phone number |
| `CUST-SSN` | `PIC 9(09)` | Numeric | 9 digits | Social Security Number |
| `CUST-GOVT-ISSUED-ID` | `PIC X(20)` | Alpha | 20 chars | Government-issued ID number |
| `CUST-DOB-YYYYMMDD` | `PIC X(10)` | Alpha | 10 chars | Date of birth |
| `CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | Alpha | 10 chars | EFT (electronic fund transfer) account |
| `CUST-PRI-CARD-HOLDER-IND` | `PIC X(01)` | Alpha | 1 char | Primary card holder indicator: `Y`/`N` |
| `CUST-FICO-CREDIT-SCORE` | `PIC 9(03)` | Numeric | 3 digits | FICO credit score (000–999) |
| `FILLER` | `PIC X(168)` | — | 168 bytes | Reserved |

---

## 4. Transaction Entity

**Copybook:** `CVTRA05Y.cpy` | **Record Length:** 350 bytes | **VSAM File:** `TRANSACT.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|---|---|---|---|---|
| `TRAN-ID` | `PIC X(16)` | Alpha | 16 chars | **Primary Key.** Unique transaction identifier |
| `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | **FK** → Transaction Type. Type code (e.g., SA=Sale, CR=Credit) |
| `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 digits | **FK** → Transaction Category. Category code |
| `TRAN-SOURCE` | `PIC X(10)` | Alpha | 10 chars | Origination channel (e.g., POS, ATM, Online) |
| `TRAN-DESC` | `PIC X(100)` | Alpha | 100 chars | Transaction description / memo |
| `TRAN-AMT` | `PIC S9(09)V99` | Decimal | 11 digits | Transaction amount (signed, 2 decimals) |
| `TRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 digits | Merchant identifier |
| `TRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 chars | Merchant name |
| `TRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 chars | Merchant city |
| `TRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 chars | Merchant ZIP code |
| `TRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 chars | **FK** → Card. Card number used |
| `TRAN-ORIG-TS` | `PIC X(26)` | Alpha | 26 chars | Transaction origination timestamp |
| `TRAN-PROC-TS` | `PIC X(26)` | Alpha | 26 chars | Transaction processing timestamp |
| `FILLER` | `PIC X(20)` | — | 20 bytes | Reserved |

---

## 5. Daily Transaction Entity

**Copybook:** `CVTRA06Y.cpy` | **Record Length:** 350 bytes | **File:** `DAILYTRAN` (sequential input)

Identical structure to the Transaction Entity but used as a staging file for daily batch processing.

| Field Name | PIC Clause | Type | Size | Business Description |
|---|---|---|---|---|
| `DALYTRAN-ID` | `PIC X(16)` | Alpha | 16 chars | Daily transaction ID |
| `DALYTRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | Transaction type code |
| `DALYTRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 digits | Transaction category code |
| `DALYTRAN-SOURCE` | `PIC X(10)` | Alpha | 10 chars | Origination channel |
| `DALYTRAN-DESC` | `PIC X(100)` | Alpha | 100 chars | Transaction description |
| `DALYTRAN-AMT` | `PIC S9(09)V99` | Decimal | 11 digits | Transaction amount |
| `DALYTRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 digits | Merchant ID |
| `DALYTRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 chars | Merchant name |
| `DALYTRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 chars | Merchant city |
| `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 chars | Merchant ZIP code |
| `DALYTRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 chars | Card number |
| `DALYTRAN-ORIG-TS` | `PIC X(26)` | Alpha | 26 chars | Origination timestamp |
| `DALYTRAN-PROC-TS` | `PIC X(26)` | Alpha | 26 chars | Processing timestamp |
| `FILLER` | `PIC X(20)` | — | 20 bytes | Reserved |

---

## 6. Card Cross-Reference Entity

**Copybook:** `CVACT03Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `CARDXREF.VSAM.KSDS`

Links card numbers to account IDs. Has an **alternate index** on `XREF-ACCT-ID` for reverse lookup.

| Field Name | PIC Clause | Type | Size | Business Description |
|---|---|---|---|---|
| `XREF-CARD-NUM` | `PIC X(16)` | Alpha | 16 chars | **Primary Key.** Card number |
| `XREF-CUST-ID` | `PIC 9(09)` | Numeric | 9 digits | **FK** → Customer ID |
| `XREF-ACCT-ID` | `PIC 9(11)` | Numeric | 11 digits | **FK** → Account ID (alternate index key) |
| `FILLER` | `PIC X(14)` | — | 14 bytes | Reserved |

---

## 7. Transaction Category Balance Entity

**Copybook:** `CVTRA01Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `TCATBAL.VSAM.KSDS`

Tracks running balances per account per transaction category.

| Field Name | PIC Clause | Type | Size | Business Description |
|---|---|---|---|---|
| `TRANCAT-ACCT-ID` | `PIC 9(11)` | Numeric | 11 digits | **Composite Key (1/3).** Account ID |
| `TRANCAT-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | **Composite Key (2/3).** Transaction type code |
| `TRANCAT-CD` | `PIC 9(04)` | Numeric | 4 digits | **Composite Key (3/3).** Category code |
| `TRAN-CAT-BAL` | `PIC S9(09)V99` | Decimal | 11 digits | Running balance for this category |
| `FILLER` | `PIC X(22)` | — | 22 bytes | Reserved |

---

## 8. Disclosure Group Entity

**Copybook:** `CVTRA02Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `DISCGRP.VSAM.KSDS`

Defines interest rates by account group and transaction category.

| Field Name | PIC Clause | Type | Size | Business Description |
|---|---|---|---|---|
| `DIS-ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 chars | **Composite Key (1/3).** Account group |
| `DIS-TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | **Composite Key (2/3).** Transaction type |
| `DIS-TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 digits | **Composite Key (3/3).** Category code |
| `DIS-INT-RATE` | `PIC S9(04)V99` | Decimal | 6 digits | Interest rate (e.g., 18.99%) |
| `FILLER` | `PIC X(28)` | — | 28 bytes | Reserved |

---

## 9. Transaction Type Entity

**Copybook:** `CVTRA03Y.cpy` | **Record Length:** 60 bytes | **VSAM File:** `TRANTYPE.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|---|---|---|---|---|
| `TRAN-TYPE` | `PIC X(02)` | Alpha | 2 chars | **Primary Key.** Transaction type code |
| `TRAN-TYPE-DESC` | `PIC X(50)` | Alpha | 50 chars | Description (e.g., "Sale", "Credit", "Cash Advance") |
| `FILLER` | `PIC X(08)` | — | 8 bytes | Reserved |

---

## 10. Transaction Category Entity

**Copybook:** `CVTRA04Y.cpy` | **Record Length:** 60 bytes | **VSAM File:** `TRANCATG.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|---|---|---|---|---|
| `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | **Composite Key (1/2).** Parent transaction type |
| `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 digits | **Composite Key (2/2).** Category code |
| `TRAN-CAT-TYPE-DESC` | `PIC X(50)` | Alpha | 50 chars | Category description |
| `FILLER` | `PIC X(04)` | — | 4 bytes | Reserved |

---

## 11. User Security Entity

**Copybook:** `CSUSR01Y.cpy` | **Record Length:** 80 bytes | **VSAM File:** `USRSEC.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|---|---|---|---|---|
| `SEC-USR-ID` | `PIC X(08)` | Alpha | 8 chars | **Primary Key.** User login ID |
| `SEC-USR-FNAME` | `PIC X(20)` | Alpha | 20 chars | First name |
| `SEC-USR-LNAME` | `PIC X(20)` | Alpha | 20 chars | Last name |
| `SEC-USR-PWD` | `PIC X(08)` | Alpha | 8 chars | Password (plaintext) |
| `SEC-USR-TYPE` | `PIC X(01)` | Alpha | 1 char | User type: `A` = Admin, `U` = Regular User |
| `SEC-USR-FILLER` | `PIC X(23)` | — | 23 bytes | Reserved |

---

## 12. Communication Area (COMMAREA)

**Copybook:** `COCOM01Y.cpy` | **Record Length:** variable | **Usage:** Passed between CICS programs via XCTL/LINK

| Field Name | PIC Clause | Type | Size | Business Description |
|---|---|---|---|---|
| `CDEMO-FROM-TRANID` | `PIC X(04)` | Alpha | 4 chars | Source CICS transaction ID |
| `CDEMO-FROM-PROGRAM` | `PIC X(08)` | Alpha | 8 chars | Source program name |
| `CDEMO-TO-TRANID` | `PIC X(04)` | Alpha | 4 chars | Target CICS transaction ID |
| `CDEMO-TO-PROGRAM` | `PIC X(08)` | Alpha | 8 chars | Target program name |
| `CDEMO-USER-ID` | `PIC X(08)` | Alpha | 8 chars | Authenticated user ID |
| `CDEMO-USER-TYPE` | `PIC X(01)` | Alpha | 1 char | User type (`A`=Admin, `U`=User) |
| `CDEMO-PGM-CONTEXT` | `PIC 9(01)` | Numeric | 1 digit | 0=Enter, 1=Re-enter |
| `CDEMO-CUST-ID` | `PIC 9(09)` | Numeric | 9 digits | Current customer ID |
| `CDEMO-CUST-FNAME` | `PIC X(25)` | Alpha | 25 chars | Customer first name |
| `CDEMO-CUST-MNAME` | `PIC X(25)` | Alpha | 25 chars | Customer middle name |
| `CDEMO-CUST-LNAME` | `PIC X(25)` | Alpha | 25 chars | Customer last name |
| `CDEMO-ACCT-ID` | `PIC 9(11)` | Numeric | 11 digits | Current account ID |
| `CDEMO-ACCT-STATUS` | `PIC X(01)` | Alpha | 1 char | Account status |
| `CDEMO-CARD-NUM` | `PIC 9(16)` | Numeric | 16 digits | Current card number |
| `CDEMO-LAST-MAP` | `PIC X(07)` | Alpha | 7 chars | Last BMS map displayed |
| `CDEMO-LAST-MAPSET` | `PIC X(07)` | Alpha | 7 chars | Last BMS mapset |

---

## 13. Export/Import Record

**Copybook:** `CVEXPORT.cpy` | **Record Length:** variable | **Usage:** Flat file for data migration

The export record is a **multi-type** record that encapsulates all entity types in a single sequential file. The `EXPORT-RECORD-TYPE` field discriminates which entity is stored in each record:

| Record Type Code | Entity Contained |
|---|---|
| `C` | Customer record |
| `A` | Account record |
| `X` | Card cross-reference record |
| `T` | Transaction record |
| `R` | Card record |

---

## 14. Report Structures

### Transaction Report Layout — `CVTRA07Y.cpy`

| Field Name | PIC Clause | Business Description |
|---|---|---|
| `REPT-SHORT-NAME` | `PIC X(38)` | Report short name ("DALYREPT") |
| `REPT-LONG-NAME` | `PIC X(41)` | Report title ("Daily Transaction Report") |
| `REPT-START-DATE` | `PIC X(10)` | Report period start date |
| `REPT-END-DATE` | `PIC X(10)` | Report period end date |
| `TRAN-REPORT-TRANS-ID` | `PIC X(16)` | Transaction ID |
| `TRAN-REPORT-ACCOUNT-ID` | `PIC X(11)` | Account ID |
| `TRAN-REPORT-TYPE-CD` | `PIC X(02)` | Transaction type |
| `TRAN-REPORT-TYPE-DESC` | `PIC X(15)` | Type description |
| `TRAN-REPORT-CAT-CD` | `PIC 9(04)` | Category code |
| `TRAN-REPORT-CAT-DESC` | `PIC X(29)` | Category description |
| `TRAN-REPORT-SOURCE` | `PIC X(10)` | Transaction source |
| `TRAN-REPORT-AMT` | `PIC -ZZZ,ZZZ,ZZZ.ZZ` | Formatted amount |
| `REPT-PAGE-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Page subtotal |
| `REPT-ACCOUNT-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Account subtotal |
| `REPT-GRAND-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Report grand total |

### Statement Report Layout — `COSTM01.CPY`

| Field Name | PIC Clause | Business Description |
|---|---|---|
| `TRNX-CARD-NUM` | `PIC X(16)` | **Composite Key (1/2).** Card number |
| `TRNX-ID` | `PIC X(16)` | **Composite Key (2/2).** Transaction ID |
| `TRNX-TYPE-CD` | `PIC X(02)` | Transaction type |
| `TRNX-CAT-CD` | `PIC 9(04)` | Category code |
| `TRNX-SOURCE` | `PIC X(10)` | Source channel |
| `TRNX-DESC` | `PIC X(100)` | Description |
| `TRNX-AMT` | `PIC S9(09)V99` | Transaction amount |
| `TRNX-MERCHANT-ID` | `PIC 9(09)` | Merchant ID |
| `TRNX-MERCHANT-NAME` | `PIC X(50)` | Merchant name |
| `TRNX-MERCHANT-CITY` | `PIC X(50)` | Merchant city |
| `TRNX-MERCHANT-ZIP` | `PIC X(10)` | Merchant ZIP |
| `TRNX-ORIG-TS` | `PIC X(26)` | Origination timestamp |
| `TRNX-PROC-TS` | `PIC X(26)` | Processing timestamp |

---

## VSAM File Catalog

| VSAM Dataset Name | Key | Rec Len | Copybook | Business Entity |
|---|---|---|---|---|
| `ACCTDATA.VSAM.KSDS` | `ACCT-ID` (11 bytes) | 300 | CVACT01Y | Account Master |
| `CARDDATA.VSAM.KSDS` | `CARD-NUM` (16 bytes) | 150 | CVACT02Y | Card Master |
| `CUSTDATA.VSAM.KSDS` | `CUST-ID` (9 bytes) | 500 | CVCUS01Y | Customer Master |
| `TRANSACT.VSAM.KSDS` | `TRAN-ID` (16 bytes) | 350 | CVTRA05Y | Transaction Master |
| `CARDXREF.VSAM.KSDS` | `XREF-CARD-NUM` (16 bytes) | 50 | CVACT03Y | Card ↔ Account Xref |
| `USRSEC.VSAM.KSDS` | `SEC-USR-ID` (8 bytes) | 80 | CSUSR01Y | User Security |
| `TCATBAL.VSAM.KSDS` | Composite (17 bytes) | 50 | CVTRA01Y | Category Balance |
| `DISCGRP.VSAM.KSDS` | Composite (16 bytes) | 50 | CVTRA02Y | Disclosure Group |
| `TRANTYPE.VSAM.KSDS` | `TRAN-TYPE` (2 bytes) | 60 | CVTRA03Y | Transaction Type |
| `TRANCATG.VSAM.KSDS` | Composite (6 bytes) | 60 | CVTRA04Y | Transaction Category |
| `DALYREJS.VSAM.KSDS` | — | — | — | Daily Rejects |

### Alternate Indexes

| AIX Name | Base Cluster | AIX Key | Purpose |
|---|---|---|---|
| `CARDXREF.VSAM.AIX` | `CARDXREF.VSAM.KSDS` | `XREF-ACCT-ID` (11 bytes, pos 25) | Look up cards by account |
| `TRANSACT.VSAM.AIX` | `TRANSACT.VSAM.KSDS` | `TRAN-CARD-NUM` (16 bytes) | Look up transactions by card |

### Entity Relationship Summary

```
Customer (1) ──── (N) Card Cross-Reference (1) ──── (1) Card
    │                         │
    │                         │
    └───── (1) ──── (N) ─────┘
                Account
                  │
                  ├── (1) ──── (N) Transaction
                  │
                  └── (1) ──── (N) Transaction Category Balance
                                        │
                        Transaction Type ┘── Transaction Category
                                                    │
                                        Disclosure Group (interest rates)
```
