# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** CardDemo (Credit Card Management System)
> **Source:** COBOL Copybook PIC clause analysis from `app/cpy/`

---

## Executive Summary

This data dictionary extracts all business entities from the CardDemo COBOL copybooks. Each VSAM file's record layout is documented with field-level detail including data types, sizes, and business meaning. The system manages **7 core VSAM datasets** and **3 reference datasets** with a total record footprint of approximately **1,940 bytes** across the primary entities.

---

## 1. Account Master (CVACT01Y)

**VSAM Dataset:** `AWS.M2.CARDDEMO.ACCTDAT.VSAM.KSDS`
**Record Length:** 300 bytes | **Key:** Account ID (11 bytes, position 1)
**Copybook:** `CVACT01Y.cpy` (24 lines)
**Business Purpose:** Stores credit card account financial data including balances, limits, and status.

| # | Field Name | PIC Clause | Type | Size | Offset | Business Description |
|---|-----------|------------|------|------|--------|---------------------|
| 1 | ACCT-ID | 9(11) | Numeric | 11 | 0 | **Primary Key.** Unique account identifier |
| 2 | ACCT-ACTIVE-STATUS | X(01) | Alpha | 1 | 11 | Account status: Y=Active, N=Inactive |
| 3 | ACCT-CURR-BAL | S9(10)V99 | Signed Decimal | 12 | 12 | Current account balance |
| 4 | ACCT-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12 | 24 | Total credit limit |
| 5 | ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12 | 36 | Cash advance credit limit |
| 6 | ACCT-OPEN-DATE | X(10) | Alpha | 10 | 48 | Account open date (YYYY-MM-DD) |
| 7 | ACCT-EXPIRAION-DATE | X(10) | Alpha | 10 | 58 | Account expiration date (YYYY-MM-DD) |
| 8 | ACCT-REISSUE-DATE | X(10) | Alpha | 10 | 68 | Card reissue date (YYYY-MM-DD) |
| 9 | ACCT-CURR-CYC-CREDIT | S9(10)V99 | Signed Decimal | 12 | 78 | Current cycle credit total |
| 10 | ACCT-CURR-CYC-DEBIT | S9(10)V99 | Signed Decimal | 12 | 90 | Current cycle debit total |
| 11 | ACCT-GROUP-ID | X(10) | Alpha | 10 | 102 | Disclosure/rate group identifier |
| 12 | FILLER | X(178) | Filler | 178 | 112 | Reserved for future use |

**Modernization Target:** `Account` JPA Entity / `accounts` table

---

## 2. Card Data (CVACT02Y)

**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDDAT.VSAM.KSDS`
**Record Length:** 150 bytes | **Key:** Card Number (16 bytes, position 1)
**Copybook:** `CVACT02Y.cpy` (16 lines)
**Business Purpose:** Stores physical credit card details including embossed name, CVV, and status.

| # | Field Name | PIC Clause | Type | Size | Offset | Business Description |
|---|-----------|------------|------|------|--------|---------------------|
| 1 | CARD-NUM | X(16) | Alpha | 16 | 0 | **Primary Key.** 16-digit credit card number |
| 2 | CARD-ACCT-ID | 9(11) | Numeric | 11 | 16 | **Foreign Key** to Account Master |
| 3 | CARD-CVV-CD | 9(03) | Numeric | 3 | 27 | Card verification value (CVV) |
| 4 | CARD-EMBOSSED-NAME | X(50) | Alpha | 50 | 30 | Name embossed on physical card |
| 5 | CARD-EXPIRAION-DATE | X(10) | Alpha | 10 | 80 | Card expiration date (YYYY-MM-DD) |
| 6 | CARD-ACTIVE-STATUS | X(01) | Alpha | 1 | 90 | Card status: Y=Active, N=Inactive |
| 7 | FILLER | X(59) | Filler | 59 | 91 | Reserved for future use |

**Modernization Target:** `CreditCard` JPA Entity / `credit_cards` table

---

## 3. Customer Master (CVCUS01Y)

**VSAM Dataset:** `AWS.M2.CARDDEMO.CUSTDAT.VSAM.KSDS`
**Record Length:** 500 bytes | **Key:** Customer ID (9 bytes, position 1)
**Copybook:** `CVCUS01Y.cpy` (40 lines)
**Business Purpose:** Stores customer personal information, contact details, and credit profile.

| # | Field Name | PIC Clause | Type | Size | Offset | Business Description |
|---|-----------|------------|------|------|--------|---------------------|
| 1 | CUST-ID | 9(09) | Numeric | 9 | 0 | **Primary Key.** Unique customer identifier |
| 2 | CUST-FIRST-NAME | X(25) | Alpha | 25 | 9 | Customer first name |
| 3 | CUST-MIDDLE-NAME | X(25) | Alpha | 25 | 34 | Customer middle name |
| 4 | CUST-LAST-NAME | X(25) | Alpha | 25 | 59 | Customer last name |
| 5 | CUST-ADDR-LINE-1 | X(50) | Alpha | 50 | 84 | Address line 1 |
| 6 | CUST-ADDR-LINE-2 | X(50) | Alpha | 50 | 134 | Address line 2 |
| 7 | CUST-ADDR-LINE-3 | X(50) | Alpha | 50 | 184 | Address line 3 |
| 8 | CUST-ADDR-STATE-CD | X(02) | Alpha | 2 | 234 | US state code |
| 9 | CUST-ADDR-COUNTRY-CD | X(03) | Alpha | 3 | 236 | Country code |
| 10 | CUST-ADDR-ZIP | X(10) | Alpha | 10 | 239 | ZIP/postal code |
| 11 | CUST-PHONE-NUM-1 | X(15) | Alpha | 15 | 249 | Primary phone number |
| 12 | CUST-PHONE-NUM-2 | X(15) | Alpha | 15 | 264 | Secondary phone number |
| 13 | CUST-SSN | 9(09) | Numeric | 9 | 279 | Social Security Number |
| 14 | CUST-GOVT-ISSUED-ID | X(20) | Alpha | 20 | 288 | Government-issued ID |
| 15 | CUST-DOB-YYYYMMDD | X(10) | Alpha | 10 | 308 | Date of birth (YYYY-MM-DD) |
| 16 | CUST-EFT-ACCOUNT-ID | X(10) | Alpha | 10 | 318 | EFT (electronic funds transfer) account |
| 17 | CUST-PRI-CARD-HOLDER-IND | X(01) | Alpha | 1 | 328 | Primary cardholder indicator (Y/N) |
| 18 | CUST-FICO-CREDIT-SCORE | 9(03) | Numeric | 3 | 329 | FICO credit score (300-850) |
| 19 | FILLER | X(168) | Filler | 168 | 332 | Reserved for future use |

**Modernization Target:** `Customer` JPA Entity / `customers` table
**PII Fields:** SSN, DOB, Phone Numbers, Address -- require encryption at rest

---

## 4. Card Cross-Reference (CVACT03Y)

**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`
**Record Length:** 50 bytes | **Key:** Card Number (16 bytes, position 1)
**Copybook:** `CVACT03Y.cpy` (15 lines)
**Business Purpose:** Links cards to customers and accounts. Enables lookup by card number to find account/customer.

| # | Field Name | PIC Clause | Type | Size | Offset | Business Description |
|---|-----------|------------|------|------|--------|---------------------|
| 1 | XREF-CARD-NUM | X(16) | Alpha | 16 | 0 | **Primary Key.** Credit card number |
| 2 | XREF-CUST-ID | 9(09) | Numeric | 9 | 16 | **Foreign Key** to Customer Master |
| 3 | XREF-ACCT-ID | 9(11) | Numeric | 11 | 25 | **Foreign Key** to Account Master |
| 4 | FILLER | X(14) | Filler | 14 | 36 | Reserved for future use |

**Alternate Index (AIX):** `CXACAIX` -- keyed by Account ID for reverse lookup
**Modernization Target:** Join table or embedded relationship in Account/Card entities

---

## 5. Transaction Record (CVTRA05Y)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`
**Record Length:** 350 bytes | **Key:** Transaction ID (16 bytes, position 1)
**Copybook:** `CVTRA05Y.cpy` (21 lines)
**Business Purpose:** Stores all processed credit card transactions with merchant details and timestamps.

| # | Field Name | PIC Clause | Type | Size | Offset | Business Description |
|---|-----------|------------|------|------|--------|---------------------|
| 1 | TRAN-ID | X(16) | Alpha | 16 | 0 | **Primary Key.** Transaction identifier |
| 2 | TRAN-TYPE-CD | X(02) | Alpha | 2 | 16 | Transaction type code (FK to TRANTYPE) |
| 3 | TRAN-CAT-CD | 9(04) | Numeric | 4 | 18 | Transaction category code |
| 4 | TRAN-SOURCE | X(10) | Alpha | 10 | 22 | Transaction source (POS, ATM, Online, etc.) |
| 5 | TRAN-DESC | X(100) | Alpha | 100 | 32 | Transaction description |
| 6 | TRAN-AMT | S9(09)V99 | Signed Decimal | 11 | 132 | Transaction amount (max 999,999,999.99) |
| 7 | TRAN-MERCHANT-ID | 9(09) | Numeric | 9 | 143 | Merchant identifier |
| 8 | TRAN-MERCHANT-NAME | X(50) | Alpha | 50 | 152 | Merchant name |
| 9 | TRAN-MERCHANT-CITY | X(50) | Alpha | 50 | 202 | Merchant city |
| 10 | TRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | 252 | Merchant ZIP code |
| 11 | TRAN-CARD-NUM | X(16) | Alpha | 16 | 262 | **Foreign Key** to Card/Cross-Reference |
| 12 | TRAN-ORIG-TS | X(26) | Alpha | 26 | 278 | Original transaction timestamp |
| 13 | TRAN-PROC-TS | X(26) | Alpha | 26 | 304 | Processing timestamp |
| 14 | FILLER | X(20) | Filler | 20 | 330 | Reserved for future use |

**Alternate Index (AIX):** Keyed on TRAN-PROC-TS (position 304, length 26) for date-range queries
**Modernization Target:** `Transaction` JPA Entity / `transactions` table

---

## 6. Daily Transaction Record (CVTRA06Y)

**VSAM Dataset:** Sequential file (daily input batch)
**Record Length:** 350 bytes | **Layout:** Identical to Transaction Record
**Copybook:** `CVTRA06Y.cpy` (21 lines)
**Business Purpose:** Incoming daily transactions for batch posting. Same structure as CVTRA05Y with DALYTRAN- prefix.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | DALYTRAN-ID | X(16) | Alpha | 16 | Transaction identifier |
| 2 | DALYTRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| 3 | DALYTRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| 4 | DALYTRAN-SOURCE | X(10) | Alpha | 10 | Transaction source |
| 5 | DALYTRAN-DESC | X(100) | Alpha | 100 | Transaction description |
| 6 | DALYTRAN-AMT | S9(09)V99 | Signed Decimal | 11 | Transaction amount |
| 7 | DALYTRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant identifier |
| 8 | DALYTRAN-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| 9 | DALYTRAN-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| 10 | DALYTRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP code |
| 11 | DALYTRAN-CARD-NUM | X(16) | Alpha | 16 | Card number |
| 12 | DALYTRAN-ORIG-TS | X(26) | Alpha | 26 | Original transaction timestamp |
| 13 | DALYTRAN-PROC-TS | X(26) | Alpha | 26 | Processing timestamp |
| 14 | FILLER | X(20) | Filler | 20 | Reserved |

**Modernization Target:** Ingested via Spring Batch `ItemReader` into Transaction table

---

## 7. User Security Record (CSUSR01Y)

**VSAM Dataset:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`
**Record Length:** 80 bytes | **Key:** User ID (8 bytes, position 1)
**Copybook:** `CSUSR01Y.cpy` (10 lines)
**Business Purpose:** Stores application user credentials and role for authentication and authorization.

| # | Field Name | PIC Clause | Type | Size | Offset | Business Description |
|---|-----------|------------|------|------|--------|---------------------|
| 1 | SEC-USR-ID | X(08) | Alpha | 8 | 0 | **Primary Key.** User login ID |
| 2 | SEC-USR-FNAME | X(20) | Alpha | 20 | 8 | User first name |
| 3 | SEC-USR-LNAME | X(20) | Alpha | 20 | 28 | User last name |
| 4 | SEC-USR-PWD | X(08) | Alpha | 8 | 48 | User password (plaintext) |
| 5 | SEC-USR-TYPE | X(01) | Alpha | 1 | 56 | User type: A=Admin, U=Regular User |
| 6 | SEC-USR-FILLER | X(23) | Filler | 23 | 57 | Reserved for future use |

**Security Note:** Passwords stored in plaintext -- must migrate to hashed/salted storage
**Modernization Target:** `User` JPA Entity / `users` table with Spring Security integration

---

## 8. Transaction Category Balance (CVTRA01Y)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS`
**Record Length:** 50 bytes | **Composite Key:** Account ID + Type Code + Category Code (17 bytes)
**Copybook:** `CVTRA01Y.cpy` (13 lines)
**Business Purpose:** Tracks running balance per transaction category per account for interest calculation.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | TRANCAT-ACCT-ID | 9(11) | Numeric | 11 | Account ID (part of composite key) |
| 2 | TRANCAT-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| 3 | TRANCAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| 4 | TRAN-CAT-BAL | S9(09)V99 | Signed Decimal | 11 | Category balance amount |
| 5 | FILLER | X(22) | Filler | 22 | Reserved |

**Modernization Target:** `CategoryBalance` entity or computed view

---

## 9. Disclosure Group (CVTRA02Y)

**VSAM Dataset:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`
**Record Length:** 50 bytes | **Composite Key:** Group ID + Type Code + Category Code (16 bytes)
**Copybook:** `CVTRA02Y.cpy` (13 lines)
**Business Purpose:** Defines interest rates per disclosure group, transaction type, and category.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | DIS-ACCT-GROUP-ID | X(10) | Alpha | 10 | Disclosure group identifier |
| 2 | DIS-TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| 3 | DIS-TRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| 4 | DIS-INT-RATE | S9(04)V99 | Signed Decimal | 6 | Interest rate (e.g., 18.99) |
| 5 | FILLER | X(28) | Filler | 28 | Reserved |

**Modernization Target:** `DisclosureGroup` entity or configuration table

---

## 10. Transaction Type (CVTRA03Y)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`
**Record Length:** 60 bytes | **Key:** Transaction Type (2 bytes)
**Copybook:** `CVTRA03Y.cpy` (10 lines)
**Business Purpose:** Reference table defining transaction type codes and descriptions.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | TRAN-TYPE | X(02) | Alpha | 2 | **Primary Key.** Type code (e.g., "PR"=Purchase) |
| 2 | TRAN-TYPE-DESC | X(50) | Alpha | 50 | Type description |
| 3 | FILLER | X(08) | Filler | 8 | Reserved |

**Modernization Target:** `TransactionType` enum or reference table

---

## 11. Transaction Category (CVTRA04Y)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`
**Record Length:** 60 bytes | **Composite Key:** Type Code + Category Code (6 bytes)
**Copybook:** `CVTRA04Y.cpy` (12 lines)
**Business Purpose:** Sub-classification of transaction types into categories.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code (part of key) |
| 2 | TRAN-CAT-CD | 9(04) | Numeric | 4 | Category code (part of key) |
| 3 | TRAN-CAT-TYPE-DESC | X(50) | Alpha | 50 | Category description |
| 4 | FILLER | X(04) | Filler | 4 | Reserved |

**Modernization Target:** `TransactionCategory` entity or reference table

---

## 12. Application Communication Area (COCOM01Y)

**Usage:** CICS COMMAREA passed between all online programs
**Size:** ~100 bytes | **Not persisted to disk**
**Copybook:** `COCOM01Y.cpy` (47 lines)
**Business Purpose:** Session state passed between CICS programs via transfer control (XCTL).

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | CDEMO-FROM-TRANID | X(04) | Alpha | 4 | Originating transaction ID |
| 2 | CDEMO-FROM-PROGRAM | X(08) | Alpha | 8 | Originating program name |
| 3 | CDEMO-TO-TRANID | X(04) | Alpha | 4 | Target transaction ID |
| 4 | CDEMO-TO-PROGRAM | X(08) | Alpha | 8 | Target program name |
| 5 | CDEMO-USER-ID | X(08) | Alpha | 8 | Logged-in user ID |
| 6 | CDEMO-USER-TYPE | X(01) | Alpha | 1 | User role: A=Admin, U=User |
| 7 | CDEMO-PGM-CONTEXT | 9(01) | Numeric | 1 | 0=Enter (fresh), 1=Re-enter |
| 8 | CDEMO-CUST-ID | 9(09) | Numeric | 9 | Current customer ID in context |
| 9 | CDEMO-CUST-FNAME | X(25) | Alpha | 25 | Customer first name |
| 10 | CDEMO-CUST-MNAME | X(25) | Alpha | 25 | Customer middle name |
| 11 | CDEMO-CUST-LNAME | X(25) | Alpha | 25 | Customer last name |
| 12 | CDEMO-ACCT-ID | 9(11) | Numeric | 11 | Current account ID in context |
| 13 | CDEMO-ACCT-STATUS | X(01) | Alpha | 1 | Account status |
| 14 | CDEMO-CARD-NUM | 9(16) | Numeric | 16 | Current card number in context |
| 15 | CDEMO-LAST-MAP | X(07) | Alpha | 7 | Last displayed BMS map |
| 16 | CDEMO-LAST-MAPSET | X(07) | Alpha | 7 | Last displayed BMS mapset |

**Modernization Target:** HTTP Session or JWT token payload

---

## 13. Statement Transaction Layout (COSTM01)

**Usage:** Batch statement generation (CBSTM03A/B)
**Record Length:** ~350 bytes | **Key:** Card Number + Transaction ID (32 bytes)
**Copybook:** `COSTM01.CPY` (38 lines)
**Business Purpose:** Re-keyed transaction layout for statement generation sorted by card.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | TRNX-CARD-NUM | X(16) | Alpha | 16 | Card number (primary sort key) |
| 2 | TRNX-ID | X(16) | Alpha | 16 | Transaction ID (secondary key) |
| 3 | TRNX-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| 4 | TRNX-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| 5 | TRNX-SOURCE | X(10) | Alpha | 10 | Transaction source |
| 6 | TRNX-DESC | X(100) | Alpha | 100 | Transaction description |
| 7 | TRNX-AMT | S9(09)V99 | Signed Decimal | 11 | Transaction amount |
| 8 | TRNX-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant ID |
| 9 | TRNX-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| 10 | TRNX-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| 11 | TRNX-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP |
| 12 | TRNX-ORIG-TS | X(26) | Alpha | 26 | Original timestamp |
| 13 | TRNX-PROC-TS | X(26) | Alpha | 26 | Processing timestamp |
| 14 | FILLER | X(20) | Filler | 20 | Reserved |

---

## 14. Export Record Layout (CVEXPORT)

**Usage:** Data export/import (CBEXPORT/CBIMPORT)
**Copybook:** `CVEXPORT.cpy`
**Business Purpose:** Unified record layout for exporting all VSAM files to a single flat file for migration.

Contains sub-records for each entity type:
- `EXP-CUSTOMER-RECORD` -- mirrors CVCUS01Y
- `EXP-ACCOUNT-RECORD` -- mirrors CVACT01Y
- `EXP-CARD-XREF-RECORD` -- mirrors CVACT03Y
- `EXP-TRANSACTION-RECORD` -- mirrors CVTRA05Y
- `EXP-CARD-RECORD` -- mirrors CVACT02Y with Card#, AcctID, CVV, Name, Expiry, Status

---

## 15. Date Conversion Record (CODATECN)

**Usage:** Date format conversion utility (CBACT01C via COBDATFT)
**Copybook:** `CODATECN.cpy` (52 lines)
**Business Purpose:** Converts between YYYYMMDD and YYYY-MM-DD date formats.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | CODATECN-TYPE | X | Alpha | 1 | Input format: 1=YYYYMMDD, 2=YYYY-MM-DD |
| 2 | CODATECN-INP-DATE | X(20) | Alpha | 20 | Input date value |
| 3 | CODATECN-OUTTYPE | X | Alpha | 1 | Output format: 1=YYYY-MM-DD, 2=YYYYMMDD |
| 4 | CODATECN-0UT-DATE | X(20) | Alpha | 20 | Output date value |
| 5 | CODATECN-ERROR-MSG | X(38) | Alpha | 38 | Error message if conversion fails |

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)          1:N         Card Cross-Ref (CVACT03Y)
  CUST-ID ─────────────────────────────── XREF-CUST-ID
                                              │
Account (CVACT01Y)           1:N              │
  ACCT-ID ─────────────────────────────── XREF-ACCT-ID
                                              │
                                              │ 1:1
Card Data (CVACT02Y)                          │
  CARD-NUM ───────────────────────────── XREF-CARD-NUM
      │
      │ 1:N
Transaction (CVTRA05Y)
  TRAN-CARD-NUM ──────────────────────── CARD-NUM
  TRAN-TYPE-CD ──┐
                 ├──── Transaction Type (CVTRA03Y)
                 │       TRAN-TYPE
                 │
  TRAN-CAT-CD ──┤
                 └──── Transaction Category (CVTRA04Y)
                         TRAN-TYPE-CD + TRAN-CAT-CD

Category Balance (CVTRA01Y)
  TRANCAT-ACCT-ID ───────────────────── ACCT-ID
  TRANCAT-TYPE-CD + TRANCAT-CD

Disclosure Group (CVTRA02Y)
  DIS-ACCT-GROUP-ID ─────────────────── ACCT-GROUP-ID
  DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD

User Security (CSUSR01Y)
  SEC-USR-ID (standalone, no FK relationships)
```

---

## VSAM File Inventory

| # | DD Name | VSAM Dataset | Type | Key | Rec Len | Entity |
|---|---------|-------------|------|-----|---------|--------|
| 1 | ACCTDAT | ACCTDAT.VSAM.KSDS | KSDS | ACCT-ID (11,0) | 300 | Account |
| 2 | CARDDAT | CARDDAT.VSAM.KSDS | KSDS | CARD-NUM (16,0) | 150 | Card |
| 3 | CUSTDAT | CUSTDAT.VSAM.KSDS | KSDS | CUST-ID (9,0) | 500 | Customer |
| 4 | CARDXREF / CCXREF | CARDXREF.VSAM.KSDS | KSDS | XREF-CARD-NUM (16,0) | 50 | Cross-Ref |
| 5 | CXACAIX | CARDXREF.VSAM.AIX | AIX | XREF-ACCT-ID (11,16) | -- | Cross-Ref (by acct) |
| 6 | TRANSACT | TRANSACT.VSAM.KSDS | KSDS | TRAN-ID (16,0) | 350 | Transaction |
| 7 | USRSEC | USRSEC.VSAM.KSDS | KSDS | SEC-USR-ID (8,0) | 80 | User Security |
| 8 | TCATBALF | TCATBAL.VSAM.KSDS | KSDS | Composite (17,0) | 50 | Category Balance |
| 9 | DISCGRP | DISCGRP.VSAM.KSDS | KSDS | Composite (16,0) | 50 | Disclosure Group |
| 10 | TRANTYPE | TRANTYPE.VSAM.KSDS | KSDS | TRAN-TYPE (2,0) | 60 | Transaction Type |
| 11 | TRANCATG | TRANCATG.VSAM.KSDS | KSDS | Composite (6,0) | 60 | Transaction Category |
| 12 | DALYTRAN | Sequential | SEQ | -- | 350 | Daily Transactions |

---

## Modernization Mapping Summary

| COBOL Entity | Java Class | DB Table | Spring Component |
|-------------|-----------|----------|-----------------|
| Account (CVACT01Y) | `Account.java` | `accounts` | JPA Entity |
| Card (CVACT02Y) | `CreditCard.java` | `credit_cards` | JPA Entity |
| Customer (CVCUS01Y) | `Customer.java` | `customers` | JPA Entity |
| Cross-Ref (CVACT03Y) | Embedded relationship | `card_xref` or FK | JPA `@ManyToOne` |
| Transaction (CVTRA05Y) | `Transaction.java` | `transactions` | JPA Entity |
| Daily Trans (CVTRA06Y) | `DailyTransaction.java` | staging table | Spring Batch Item |
| User Security (CSUSR01Y) | `User.java` | `users` | Spring Security UserDetails |
| Category Balance (CVTRA01Y) | `CategoryBalance.java` | `category_balances` | JPA Entity |
| Disclosure Group (CVTRA02Y) | `DisclosureGroup.java` | `disclosure_groups` | JPA Entity |
| Transaction Type (CVTRA03Y) | `TransactionType` enum | `transaction_types` | Reference Data |
| Transaction Category (CVTRA04Y) | `TransactionCategory.java` | `transaction_categories` | Reference Data |
| COMMAREA (COCOM01Y) | Session/JWT | -- | `HttpSession` / JWT |
