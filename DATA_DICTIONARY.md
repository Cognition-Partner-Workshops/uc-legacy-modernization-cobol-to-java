# Data Dictionary - CardDemo COBOL Codebase

> **Generated**: March 2026  
> **Application**: CardDemo - Mainframe Credit Card Management System  
> **Source**: Copybook PIC clause analysis from `app/cpy/` and `app/cpy/COSTM01.CPY`

---

## Table of Contents

1. [Overview](#overview)
2. [Account Entity](#1-account-entity)
3. [Card Entity](#2-card-entity)
4. [Customer Entity](#3-customer-entity)
5. [Transaction Entity](#4-transaction-entity)
6. [Daily Transaction Entity](#5-daily-transaction-entity)
7. [Card-Account Cross-Reference Entity](#6-card-account-cross-reference-entity)
8. [Transaction Category Balance Entity](#7-transaction-category-balance-entity)
9. [Disclosure Group Entity](#8-disclosure-group-entity)
10. [Transaction Type Entity](#9-transaction-type-entity)
11. [Transaction Category Entity](#10-transaction-category-entity)
12. [User Security Entity](#11-user-security-entity)
13. [Statement Transaction Entity](#12-statement-transaction-entity)
14. [Customer (Statement) Entity](#13-customer-statement-entity)
15. [Export Record Entity](#14-export-record-entity)
16. [Report Data Structures](#15-report-data-structures)
17. [Communication Area (COMMAREA)](#16-communication-area)
18. [Date Utility Structures](#17-date-utility-structures)
19. [VSAM File Catalog](#vsam-file-catalog)
20. [Data Type Reference](#data-type-reference)

---

## Overview

The CardDemo application uses **VSAM KSDS** (Key-Sequenced Data Sets) files as its primary data store. Each VSAM file has a corresponding copybook that defines the fixed-length record layout using COBOL PIC (Picture) clauses. This dictionary translates those technical layouts into business-friendly field descriptions.

**Key**: PIC `X(n)` = alphanumeric n chars, PIC `9(n)` = numeric n digits, PIC `S9(n)V99` = signed decimal with 2 implied decimal places, `COMP` = binary storage.

---

## 1. Account Entity

**Copybook**: `CVACT01Y.cpy` | **Record Length**: 300 bytes | **VSAM File**: `ACCTDATA.VSAM.KSDS`

| Field Name               | PIC Clause       | Type           | Size  | Business Description                        |
|--------------------------|------------------|----------------|-------|---------------------------------------------|
| `ACCT-ID`                | `9(11)`          | Numeric        | 11    | **Primary Key** - Unique account identifier |
| `ACCT-ACTIVE-STATUS`     | `X(01)`          | Alpha          | 1     | Account status (Active/Inactive)            |
| `ACCT-CURR-BAL`          | `S9(10)V99`      | Signed Decimal | 12    | Current account balance (dollars.cents)     |
| `ACCT-CREDIT-LIMIT`      | `S9(10)V99`      | Signed Decimal | 12    | Credit limit amount                         |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99`      | Signed Decimal | 12    | Cash advance credit limit                   |
| `ACCT-OPEN-DATE`         | `X(10)`          | Alpha-Date     | 10    | Account opening date                        |
| `ACCT-EXPIRAION-DATE`    | `X(10)`          | Alpha-Date     | 10    | Account expiration date                     |
| `ACCT-REISSUE-DATE`      | `X(10)`          | Alpha-Date     | 10    | Card reissue date                           |
| `ACCT-CURR-CYC-CREDIT`   | `S9(10)V99`     | Signed Decimal | 12    | Current cycle credit total                  |
| `ACCT-CURR-CYC-DEBIT`    | `S9(10)V99`     | Signed Decimal | 12    | Current cycle debit total                   |
| `ACCT-ADDR-ZIP`          | `X(10)`          | Alpha          | 10    | Account holder ZIP code                     |
| `ACCT-GROUP-ID`          | `X(10)`          | Alpha          | 10    | Account group identifier                    |
| FILLER                   | `X(178)`         | Padding        | 178   | Reserved for future use                     |

**Business Rules**:
- Account ID is the primary key for all account-level lookups
- Balance fields use signed decimals to support credits and debits
- Dates stored as text strings (YYYY-MM-DD format)

---

## 2. Card Entity

**Copybook**: `CVACT02Y.cpy` | **Record Length**: 150 bytes | **VSAM File**: `CARDDATA.VSAM.KSDS`

| Field Name               | PIC Clause       | Type           | Size  | Business Description                        |
|--------------------------|------------------|----------------|-------|---------------------------------------------|
| `CARD-NUM`               | `X(16)`          | Alpha          | 16    | **Primary Key** - Credit card number        |
| `CARD-ACCT-ID`           | `9(11)`          | Numeric        | 11    | Foreign key to Account entity               |
| `CARD-CVV-CD`            | `9(03)`          | Numeric        | 3     | Card Verification Value (CVV)               |
| `CARD-EMBOSSED-NAME`     | `X(50)`          | Alpha          | 50    | Name embossed on physical card              |
| `CARD-EXPIRAION-DATE`    | `X(10)`          | Alpha-Date     | 10    | Card expiration date                        |
| `CARD-ACTIVE-STATUS`     | `X(01)`          | Alpha          | 1     | Card active status flag                     |
| FILLER                   | `X(59)`          | Padding        | 59    | Reserved for future use                     |

**Business Rules**:
- Card number is the primary lookup key
- One account can have multiple cards (one-to-many relationship)
- CVV is stored as plain numeric (security consideration for modernization)

---

## 3. Customer Entity

**Copybook**: `CVCUS01Y.cpy` | **Record Length**: 500 bytes | **VSAM File**: `CUSTDATA.VSAM.KSDS`

| Field Name               | PIC Clause       | Type           | Size  | Business Description                        |
|--------------------------|------------------|----------------|-------|---------------------------------------------|
| `CUST-ID`                | `9(09)`          | Numeric        | 9     | **Primary Key** - Customer identifier       |
| `CUST-FIRST-NAME`        | `X(25)`          | Alpha          | 25    | Customer first name                         |
| `CUST-MIDDLE-NAME`       | `X(25)`          | Alpha          | 25    | Customer middle name                        |
| `CUST-LAST-NAME`         | `X(25)`          | Alpha          | 25    | Customer last name                          |
| `CUST-ADDR-LINE-1`       | `X(50)`          | Alpha          | 50    | Street address line 1                       |
| `CUST-ADDR-LINE-2`       | `X(50)`          | Alpha          | 50    | Street address line 2                       |
| `CUST-ADDR-LINE-3`       | `X(50)`          | Alpha          | 50    | Street address line 3                       |
| `CUST-ADDR-STATE-CD`     | `X(02)`          | Alpha          | 2     | State code (2-letter US state)              |
| `CUST-ADDR-COUNTRY-CD`   | `X(03)`          | Alpha          | 3     | Country code (3-letter ISO)                 |
| `CUST-ADDR-ZIP`          | `X(10)`          | Alpha          | 10    | ZIP/postal code                             |
| `CUST-PHONE-NUM-1`       | `X(15)`          | Alpha          | 15    | Primary phone number                        |
| `CUST-PHONE-NUM-2`       | `X(15)`          | Alpha          | 15    | Secondary phone number                      |
| `CUST-SSN`               | `9(09)`          | Numeric        | 9     | Social Security Number (PII)                |
| `CUST-GOVT-ISSUED-ID`    | `X(20)`          | Alpha          | 20    | Government-issued ID number                 |
| `CUST-DOB-YYYYMMDD`      | `X(10)`          | Alpha-Date     | 10    | Date of birth                               |
| `CUST-EFT-ACCOUNT-ID`    | `X(10)`          | Alpha          | 10    | EFT (Electronic Funds Transfer) account ID  |
| `CUST-PRI-CARD-HOLDER-IND`| `X(01)`         | Alpha          | 1     | Primary card holder indicator               |
| `CUST-FICO-CREDIT-SCORE`  | `9(03)`         | Numeric        | 3     | FICO credit score                           |
| FILLER                   | `X(168)`         | Padding        | 168   | Reserved for future use                     |

**Business Rules**:
- SSN is stored as plain numeric (critical PII - requires encryption in modernized system)
- FICO score range: 300-850
- Customer ID links to accounts via the cross-reference entity

---

## 4. Transaction Entity

**Copybook**: `CVTRA05Y.cpy` | **Record Length**: 350 bytes | **VSAM File**: `TRANSACT.VSAM.KSDS`

| Field Name               | PIC Clause       | Type           | Size  | Business Description                        |
|--------------------------|------------------|----------------|-------|---------------------------------------------|
| `TRAN-ID`                | `X(16)`          | Alpha          | 16    | **Primary Key** - Transaction identifier    |
| `TRAN-TYPE-CD`           | `X(02)`          | Alpha          | 2     | Transaction type code (FK to type table)    |
| `TRAN-CAT-CD`            | `9(04)`          | Numeric        | 4     | Transaction category code                   |
| `TRAN-SOURCE`            | `X(10)`          | Alpha          | 10    | Transaction source (POS, ATM, Online, etc.) |
| `TRAN-DESC`              | `X(100)`         | Alpha          | 100   | Transaction description                     |
| `TRAN-AMT`               | `S9(09)V99`      | Signed Decimal | 11    | Transaction amount (signed for credits)     |
| `TRAN-MERCHANT-ID`       | `9(09)`          | Numeric        | 9     | Merchant identifier                         |
| `TRAN-MERCHANT-NAME`     | `X(50)`          | Alpha          | 50    | Merchant business name                      |
| `TRAN-MERCHANT-CITY`     | `X(50)`          | Alpha          | 50    | Merchant city                               |
| `TRAN-MERCHANT-ZIP`      | `X(10)`          | Alpha          | 10    | Merchant ZIP code                           |
| `TRAN-CARD-NUM`          | `X(16)`          | Alpha          | 16    | Card number used for transaction            |
| `TRAN-ORIG-TS`           | `X(26)`          | Alpha-Timestamp| 26    | Original transaction timestamp              |
| `TRAN-PROC-TS`           | `X(26)`          | Alpha-Timestamp| 26    | Processing timestamp                        |
| FILLER                   | `X(20)`          | Padding        | 20    | Reserved for future use                     |

**Business Rules**:
- Transaction ID is system-generated unique identifier
- Amount is signed: negative = debit/purchase, positive = credit/refund
- Timestamps stored as text strings (ISO format)
- Card number links to Card entity

---

## 5. Daily Transaction Entity

**Copybook**: `CVTRA06Y.cpy` | **Record Length**: 350 bytes | **VSAM File**: `DALYTRAN`

| Field Name               | PIC Clause       | Type           | Size  | Business Description                        |
|--------------------------|------------------|----------------|-------|---------------------------------------------|
| `DALYTRAN-ID`            | `X(16)`          | Alpha          | 16    | Daily transaction identifier                |
| `DALYTRAN-TYPE-CD`       | `X(02)`          | Alpha          | 2     | Transaction type code                       |
| `DALYTRAN-CAT-CD`        | `9(04)`          | Numeric        | 4     | Transaction category code                   |
| `DALYTRAN-SOURCE`        | `X(10)`          | Alpha          | 10    | Transaction source                          |
| `DALYTRAN-DESC`          | `X(100)`         | Alpha          | 100   | Transaction description                     |
| `DALYTRAN-AMT`           | `S9(09)V99`      | Signed Decimal | 11    | Transaction amount                          |
| `DALYTRAN-MERCHANT-ID`   | `9(09)`          | Numeric        | 9     | Merchant identifier                         |
| `DALYTRAN-MERCHANT-NAME` | `X(50)`          | Alpha          | 50    | Merchant name                               |
| `DALYTRAN-MERCHANT-CITY` | `X(50)`          | Alpha          | 50    | Merchant city                               |
| `DALYTRAN-MERCHANT-ZIP`  | `X(10)`          | Alpha          | 10    | Merchant ZIP code                           |
| `DALYTRAN-CARD-NUM`      | `X(16)`          | Alpha          | 16    | Card number used                            |
| `DALYTRAN-ORIG-TS`       | `X(26)`          | Alpha-Timestamp| 26    | Original timestamp                          |
| `DALYTRAN-PROC-TS`       | `X(26)`          | Alpha-Timestamp| 26    | Processing timestamp                        |
| FILLER                   | `X(20)`          | Padding        | 20    | Reserved                                    |

**Business Rules**:
- Mirrors Transaction entity structure exactly
- Used as a staging area for daily batch processing
- Records flow: Daily Transaction -> posted to -> Transaction master
- Rejected records written to DALYREJS file

---

## 6. Card-Account Cross-Reference Entity

**Copybook**: `CVACT03Y.cpy` | **Record Length**: 50 bytes | **VSAM File**: `CARDXREF.VSAM.KSDS`

| Field Name               | PIC Clause       | Type           | Size  | Business Description                        |
|--------------------------|------------------|----------------|-------|---------------------------------------------|
| `XREF-CARD-NUM`          | `X(16)`          | Alpha          | 16    | **Primary Key** - Card number               |
| `XREF-ACCT-ID`           | `9(11)`          | Numeric        | 11    | Foreign key to Account entity               |
| `XREF-CUST-ID`           | `9(09)`          | Numeric        | 9     | Foreign key to Customer entity              |
| FILLER                   | `X(14)`          | Padding        | 14    | Reserved                                    |

**Business Rules**:
- Central lookup table linking Card -> Account -> Customer
- Enables navigation from any card number to its owning account and customer
- Used by nearly every online and batch program for entity resolution

---

## 7. Transaction Category Balance Entity

**Copybook**: `CVTRA01Y.cpy` | **Record Length**: 50 bytes | **VSAM File**: `TCATBALF.VSAM.KSDS`

| Field Name               | PIC Clause       | Type           | Size  | Business Description                        |
|--------------------------|------------------|----------------|-------|---------------------------------------------|
| `TRANCAT-ACCT-ID`        | `9(11)`          | Numeric        | 11    | Account identifier (part of composite key)  |
| `TRANCAT-TYPE-CD`        | `X(02)`          | Alpha          | 2     | Transaction type code (part of composite key)|
| `TRANCAT-CD`             | `9(04)`          | Numeric        | 4     | Category code (part of composite key)       |
| `TRAN-CAT-BAL`           | `S9(09)V99`      | Signed Decimal | 11    | Running balance for this category           |
| FILLER                   | `X(22)`          | Padding        | 22    | Reserved                                    |

**Business Rules**:
- Composite key: Account ID + Type Code + Category Code
- Maintains running balances per transaction category per account
- Updated during batch transaction posting (CBTRN02C)
- Used for interest calculations (CBACT04C)

---

## 8. Disclosure Group Entity

**Copybook**: `CVTRA02Y.cpy` | **Record Length**: 50 bytes | **VSAM File**: `DISCGRP.VSAM.KSDS`

| Field Name               | PIC Clause       | Type           | Size  | Business Description                        |
|--------------------------|------------------|----------------|-------|---------------------------------------------|
| `DIS-ACCT-GROUP-ID`      | `X(10)`          | Alpha          | 10    | Account group identifier (part of key)      |
| `DIS-TRAN-TYPE-CD`       | `X(02)`          | Alpha          | 2     | Transaction type (part of key)              |
| `DIS-TRAN-CAT-CD`        | `9(04)`          | Numeric        | 4     | Transaction category (part of key)          |
| `DIS-INT-RATE`           | `S9(04)V99`      | Signed Decimal | 6     | Interest rate for this group/category       |
| FILLER                   | `X(28)`          | Padding        | 28    | Reserved                                    |

**Business Rules**:
- Defines interest rates by account group and transaction category
- Read by interest calculation program (CBACT04C)
- Enables differential interest rates (e.g., purchases vs. cash advances)

---

## 9. Transaction Type Entity

**Copybook**: `CVTRA03Y.cpy` | **Record Length**: 60 bytes | **VSAM File**: `TRANTYPE.VSAM.KSDS`

| Field Name               | PIC Clause       | Type           | Size  | Business Description                        |
|--------------------------|------------------|----------------|-------|---------------------------------------------|
| `TRAN-TYPE`              | `X(02)`          | Alpha          | 2     | **Primary Key** - Transaction type code     |
| `TRAN-TYPE-DESC`         | `X(50)`          | Alpha          | 50    | Human-readable type description             |
| FILLER                   | `X(08)`          | Padding        | 8     | Reserved                                    |

**Business Rules**:
- Reference/lookup table for transaction type codes
- Examples: "SA" = Sale, "RT" = Return, "CA" = Cash Advance
- Used for display and reporting purposes

---

## 10. Transaction Category Entity

**Copybook**: `CVTRA04Y.cpy` | **Record Length**: 60 bytes | **VSAM File**: `TRANCATG.VSAM.KSDS`

| Field Name               | PIC Clause       | Type           | Size  | Business Description                        |
|--------------------------|------------------|----------------|-------|---------------------------------------------|
| `TRAN-TYPE-CD`           | `X(02)`          | Alpha          | 2     | Transaction type code (part of composite key)|
| `TRAN-CAT-CD`            | `9(04)`          | Numeric        | 4     | **Primary Key** - Category code             |
| `TRAN-CAT-TYPE-DESC`     | `X(50)`          | Alpha          | 50    | Category description                        |
| FILLER                   | `X(04)`          | Padding        | 4     | Reserved                                    |

**Business Rules**:
- Sub-classification within transaction types
- Composite key: Type Code + Category Code
- Examples: Under "SA" (Sale): 0001 = Retail, 0002 = Online, etc.

---

## 11. User Security Entity

**Copybook**: `CSUSR01Y.cpy` | **Record Length**: 80 bytes | **VSAM File**: `USRSEC.VSAM.KSDS`

| Field Name               | PIC Clause       | Type           | Size  | Business Description                        |
|--------------------------|------------------|----------------|-------|---------------------------------------------|
| `SEC-USR-ID`             | `X(08)`          | Alpha          | 8     | **Primary Key** - User login ID             |
| `SEC-USR-FNAME`          | `X(20)`          | Alpha          | 20    | User first name                             |
| `SEC-USR-LNAME`          | `X(20)`          | Alpha          | 20    | User last name                              |
| `SEC-USR-PWD`            | `X(08)`          | Alpha          | 8     | User password (plain text)                  |
| `SEC-USR-TYPE`           | `X(01)`          | Alpha          | 1     | User type (A=Admin, U=Regular User)         |
| FILLER                   | `X(23)`          | Padding        | 23    | Reserved                                    |

**Business Rules**:
- Passwords stored as plain text (critical security concern for modernization)
- User type determines menu access: Admin vs. Regular
- Default accounts: ADMIN001/PASSWORD (Admin), USER0001/PASSWORD (Regular)

---

## 12. Statement Transaction Entity

**Copybook**: `COSTM01.CPY` | **Record Length**: ~355 bytes | **Used By**: CBSTM03A/B

| Field Name               | PIC Clause       | Type           | Size  | Business Description                        |
|--------------------------|------------------|----------------|-------|---------------------------------------------|
| `TRNX-CARD-NUM`          | `X(16)`          | Alpha          | 16    | Card number (part of composite key)         |
| `TRNX-ID`                | `X(16)`          | Alpha          | 16    | Transaction ID (part of composite key)      |
| `TRNX-TYPE-CD`           | `X(02)`          | Alpha          | 2     | Transaction type code                       |
| `TRNX-CAT-CD`            | `9(04)`          | Numeric        | 4     | Transaction category code                   |
| `TRNX-SOURCE`            | `X(10)`          | Alpha          | 10    | Transaction source                          |
| `TRNX-DESC`              | `X(100)`         | Alpha          | 100   | Transaction description                     |
| `TRNX-AMT`               | `S9(09)V99`      | Signed Decimal | 11    | Transaction amount                          |
| `TRNX-MERCHANT-ID`       | `9(09)`          | Numeric        | 9     | Merchant ID                                 |
| `TRNX-MERCHANT-NAME`     | `X(50)`          | Alpha          | 50    | Merchant name                               |
| `TRNX-MERCHANT-CITY`     | `X(50)`          | Alpha          | 50    | Merchant city                               |
| `TRNX-MERCHANT-ZIP`      | `X(10)`          | Alpha          | 10    | Merchant ZIP                                |
| `TRNX-ORIG-TS`           | `X(26)`          | Alpha-Timestamp| 26    | Original timestamp                          |
| `TRNX-PROC-TS`           | `X(26)`          | Alpha-Timestamp| 26    | Processing timestamp                        |
| FILLER                   | `X(20)`          | Padding        | 20    | Reserved                                    |

**Business Rules**:
- Altered layout of the transaction record keyed by Card Number + Transaction ID
- Used specifically by the statement generation process
- Sorted by card number for efficient statement grouping

---

## 13. Customer (Statement) Entity

**Copybook**: `CUSTREC.cpy` | **Used By**: CBSTM03A

| Field Name               | PIC Clause       | Type           | Size  | Business Description                        |
|--------------------------|------------------|----------------|-------|---------------------------------------------|
| `CUST-ID`                | `9(09)`          | Numeric        | 9     | Customer identifier                         |
| `CUST-FIRST-NAME`        | `X(25)`          | Alpha          | 25    | First name                                  |
| `CUST-MIDDLE-NAME`       | `X(25)`          | Alpha          | 25    | Middle name                                 |
| `CUST-LAST-NAME`         | `X(25)`          | Alpha          | 25    | Last name                                   |
| `CUST-ADDR-LINE-1`       | `X(50)`          | Alpha          | 50    | Address line 1                              |
| `CUST-ADDR-LINE-2`       | `X(50)`          | Alpha          | 50    | Address line 2                              |
| `CUST-ADDR-LINE-3`       | `X(50)`          | Alpha          | 50    | Address line 3                              |
| `CUST-ADDR-STATE-CD`     | `X(02)`          | Alpha          | 2     | State code                                  |
| `CUST-ADDR-COUNTRY-CD`   | `X(03)`          | Alpha          | 3     | Country code                                |
| `CUST-ADDR-ZIP`          | `X(10)`          | Alpha          | 10    | ZIP code                                    |

**Business Rules**:
- Subset of customer fields used for statement address block
- Does not include sensitive fields (SSN, DOB)

---

## 14. Export Record Entity

**Copybook**: `CVEXPORT.cpy` | **Used By**: CBEXPORT, CBIMPORT

| Field Name               | PIC Clause       | Type           | Size  | Business Description                        |
|--------------------------|------------------|----------------|-------|---------------------------------------------|
| `EXP-RECORD-TYPE`        | `X(01)`          | Alpha          | 1     | Record type (C=Customer, A=Account, X=Xref, T=Transaction, R=Card) |
| `EXP-DATA`               | (varies)         | Varies         | Varies| Embedded entity data based on record type   |

**Sub-structures by record type**:
- **Customer (C)**: Customer ID, name, address, SSN, DOB, FICO score
- **Account (A)**: Account ID, status, balances, limits, dates
- **Card (R)**: Card number, account ID, CVV, embossed name, expiration, status
- **Cross-Ref (X)**: Card number, account ID, customer ID
- **Transaction (T)**: Full transaction record

**Business Rules**:
- Single export file containing all entity types
- Record type field in position 1 identifies the entity
- Used for data migration and cross-system transfers

---

## 15. Report Data Structures

**Copybook**: `CVTRA07Y.cpy` | **Used By**: CBTRN03C

| Structure                | Fields                                            | Business Description           |
|--------------------------|---------------------------------------------------|-------------------------------|
| `REPORT-NAME-HEADER`     | Short name, long name, date range                | Report header with title       |
| `TRANSACTION-DETAIL-REPORT`| Trans ID, Account ID, Type, Category, Source, Amount | Detail line for each transaction |
| `TRANSACTION-HEADER-1/2` | Column headers + separator line                   | Report column headers          |
| `REPORT-PAGE-TOTALS`     | Page total amount                                | Page subtotal line             |
| `REPORT-ACCOUNT-TOTALS`  | Account total amount                             | Account subtotal line          |
| `REPORT-GRAND-TOTALS`    | Grand total amount                               | Report grand total             |

---

## 16. Communication Area

**Copybook**: `COCOM01Y.cpy` | **Used By**: All CICS online programs

The COMMAREA (Communication Area) is passed between CICS programs during screen navigation. It contains:

| Field Group              | Business Description                                |
|--------------------------|-----------------------------------------------------|
| Program context          | Current/previous program ID, transaction ID         |
| User context             | Current user ID, user type (Admin/Regular)          |
| Navigation state         | Selected account, card, transaction IDs             |
| Screen state             | Current page, scroll position, error flags          |
| Search criteria          | Filter values for list screens                      |

---

## 17. Date Utility Structures

**Copybooks**: `CSDAT01Y.cpy`, `CODATECN.cpy`, `CSUTLDPY.cpy`, `CSUTLDWY.cpy`

| Structure                | Business Description                                |
|--------------------------|-----------------------------------------------------|
| `CSDAT01Y`               | Standard date fields for screen display             |
| `CODATECN`               | Date conversion record for assembler COBDATFT call  |
| `CSUTLDPY`               | Parameter area for CSUTLDTC date utility            |
| `CSUTLDWY`               | Working storage for CSUTLDTC date utility           |

---

## VSAM File Catalog

| File DD Name   | DSN Pattern                              | VSAM Type | Key Field          | Record Size | Entity                |
|----------------|------------------------------------------|-----------|--------------------|-------------|-----------------------|
| ACCTFILE       | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`    | KSDS      | ACCT-ID            | 300 bytes   | Account               |
| CARDFILE       | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`    | KSDS      | CARD-NUM           | 150 bytes   | Card                  |
| CUSTFILE       | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`    | KSDS      | CUST-ID            | 500 bytes   | Customer              |
| TRANSACT       | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`    | KSDS      | TRAN-ID            | 350 bytes   | Transaction           |
| CARDXREF       | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`    | KSDS      | XREF-CARD-NUM      | 50 bytes    | Cross-Reference       |
| USRSEC         | `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`      | KSDS      | SEC-USR-ID         | 80 bytes    | User Security         |
| TCATBALF       | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS`    | KSDS      | ACCT+TYPE+CAT      | 50 bytes    | Category Balance      |
| DISCGRP        | `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`     | KSDS      | GRP+TYPE+CAT       | 50 bytes    | Disclosure Group      |
| TRANTYPE       | `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`    | KSDS      | TRAN-TYPE          | 60 bytes    | Transaction Type      |
| TRANCATG       | `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`    | KSDS      | TYPE+CAT           | 60 bytes    | Transaction Category  |
| DALYTRAN       | `AWS.M2.CARDDEMO.DALYTRAN.PS`            | PS (Seq)  | N/A (sequential)   | 350 bytes   | Daily Transactions    |
| DALYREJS       | `AWS.M2.CARDDEMO.DALYREJS`               | PS (Seq)  | N/A (sequential)   | 350 bytes   | Rejected Transactions |

---

## Data Type Reference

| COBOL PIC Clause | Java Equivalent      | SQL Type            | Description                  |
|------------------|---------------------|---------------------|------------------------------|
| `X(n)`           | `String`            | `VARCHAR(n)`        | Alphanumeric, space-padded   |
| `9(n)`           | `long` / `int`      | `NUMERIC(n)`        | Unsigned integer             |
| `S9(n)V99`       | `BigDecimal`        | `DECIMAL(n+2,2)`    | Signed decimal, 2 places     |
| `S9(n)V99 COMP`  | `BigDecimal`        | `DECIMAL(n+2,2)`    | Binary signed decimal        |
| `9(n) COMP`      | `int`               | `INTEGER`           | Binary unsigned integer      |
| `X(10)` (date)   | `LocalDate`         | `DATE`              | Date as text string          |
| `X(26)` (ts)     | `LocalDateTime`     | `TIMESTAMP`         | Timestamp as text string     |

---

## Entity Relationship Summary

```
Customer (CVCUS01Y) ──1:N──> Cross-Reference (CVACT03Y) <──N:1── Account (CVACT01Y)
                                     │
                                     │ 1:N
                                     ▼
                              Card (CVACT02Y)
                                     │
                                     │ 1:N
                                     ▼
                           Transaction (CVTRA05Y)
                                     │
                           ┌─────────┼─────────┐
                           ▼         ▼         ▼
                   Type (CVTRA03Y) Cat (CVTRA04Y) CatBal (CVTRA01Y)
                                                         │
                                                         ▼
                                              DiscGroup (CVTRA02Y)

User Security (CSUSR01Y) ── Standalone authentication entity
```
