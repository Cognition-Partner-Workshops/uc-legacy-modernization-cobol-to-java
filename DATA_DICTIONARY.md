# Data Dictionary — CardDemo COBOL Codebase

> **Source:** Copybook PIC clause analysis from `app/cpy/`
> **Purpose:** Business-friendly reference mapping COBOL record layouts to logical data entities for modernization planning.

---

## Table of Contents

1. [Account Entity](#1-account-entity)
2. [Card Entity](#2-card-entity)
3. [Customer Entity](#3-customer-entity)
4. [Transaction Entity](#4-transaction-entity)
5. [Daily Transaction Entity](#5-daily-transaction-entity)
6. [Card–Account Cross-Reference](#6-cardaccount-cross-reference)
7. [User Security Entity](#7-user-security-entity)
8. [Transaction Category Balance](#8-transaction-category-balance)
9. [Discount / Interest Rate Group](#9-discount--interest-rate-group)
10. [Transaction Type Reference](#10-transaction-type-reference)
11. [Transaction Category Type Reference](#11-transaction-category-type-reference)
12. [Common Area (COMMAREA)](#12-common-area-commarea)
13. [Export Record (Unified)](#13-export-record-unified)
14. [Statement Transaction Record](#14-statement-transaction-record)
15. [Report Layout Record](#15-report-layout-record)
16. [VSAM File Summary](#16-vsam-file-summary)

---

## 1. Account Entity

**Copybook:** `CVACT01Y.cpy` (20 lines) — Record length: ~300 bytes
**VSAM Dataset:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`
**Key:** `ACCT-ID`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Java Type Suggestion |
|------------|-----------|-----------|-------:|------------------|---------------------|
| ACCT-ID | PIC 9(11) | Numeric | 11 | Account identifier (primary key) | `long` |
| ACCT-ACTIVE-STATUS | PIC X(01) | Alpha | 1 | Active/Inactive flag | `String` / `enum` |
| ACCT-CURR-BAL | PIC S9(10)V99 | Signed decimal | 12.2 | Current account balance | `BigDecimal` |
| ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Signed decimal | 12.2 | Credit limit | `BigDecimal` |
| ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 | Signed decimal | 12.2 | Cash advance credit limit | `BigDecimal` |
| ACCT-OPEN-DATE | PIC X(10) | Alpha-date | 10 | Account open date | `LocalDate` |
| ACCT-EXPIRAION-DATE | PIC X(10) | Alpha-date | 10 | Account expiration date | `LocalDate` |
| ACCT-REISSUE-DATE | PIC X(10) | Alpha-date | 10 | Card reissue date | `LocalDate` |
| ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Signed decimal | 12.2 | Current cycle credits | `BigDecimal` |
| ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 | Signed decimal | 12.2 | Current cycle debits | `BigDecimal` |
| ACCT-ADDR-ZIP | PIC X(10) | Alpha | 10 | Account holder ZIP code | `String` |
| ACCT-GROUP-ID | PIC X(10) | Alpha | 10 | Account discount/rate group | `String` |
| FILLER | PIC X(178) | — | 178 | Reserved space | — |

**Business Rules:**
- Balance = Credits − Debits accumulated over billing cycle
- `ACCT-GROUP-ID` links to Discount Group (CVTRA02Y) for interest rate determination
- Status values: typically `Y` = Active, `N` = Inactive

---

## 2. Card Entity

**Copybook:** `CVACT02Y.cpy` (14 lines) — Record length: ~150 bytes
**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`
**Key:** `CARD-NUM`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Java Type Suggestion |
|------------|-----------|-----------|-------:|------------------|---------------------|
| CARD-NUM | PIC X(16) | Alpha | 16 | Credit card number (primary key) | `String` |
| CARD-ACCT-ID | PIC 9(11) | Numeric | 11 | Linked account ID (FK → Account) | `long` |
| CARD-CVV-CD | PIC 9(03) | Numeric | 3 | Card verification value | `String` (preserve leading zeros) |
| CARD-EMBOSSED-NAME | PIC X(50) | Alpha | 50 | Name embossed on card | `String` |
| CARD-EXPIRAION-DATE | PIC X(10) | Alpha-date | 10 | Card expiration date | `LocalDate` |
| CARD-ACTIVE-STATUS | PIC X(01) | Alpha | 1 | Active/Inactive flag | `String` / `enum` |
| FILLER | PIC X(59) | — | 59 | Reserved space | — |

**Business Rules:**
- One account can have multiple cards
- `CARD-ACCT-ID` is a foreign key to Account entity
- Note: field name contains typo "EXPIRAION" — preserve in migration mapping

---

## 3. Customer Entity

**Copybook:** `CVCUS01Y.cpy` (26 lines) — Record length: ~500 bytes
**VSAM Dataset:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`
**Key:** `CUST-ID`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Java Type Suggestion |
|------------|-----------|-----------|-------:|------------------|---------------------|
| CUST-ID | PIC 9(09) | Numeric | 9 | Customer identifier (primary key) | `long` |
| CUST-FIRST-NAME | PIC X(25) | Alpha | 25 | First name | `String` |
| CUST-MIDDLE-NAME | PIC X(25) | Alpha | 25 | Middle name | `String` |
| CUST-LAST-NAME | PIC X(25) | Alpha | 25 | Last name | `String` |
| CUST-ADDR-LINE-1 | PIC X(50) | Alpha | 50 | Address line 1 | `String` |
| CUST-ADDR-LINE-2 | PIC X(50) | Alpha | 50 | Address line 2 | `String` |
| CUST-ADDR-LINE-3 | PIC X(50) | Alpha | 50 | Address line 3 | `String` |
| CUST-ADDR-STATE-CD | PIC X(02) | Alpha | 2 | US state code | `String` |
| CUST-ADDR-COUNTRY-CD | PIC X(03) | Alpha | 3 | Country code | `String` |
| CUST-ADDR-ZIP | PIC X(10) | Alpha | 10 | ZIP / postal code | `String` |
| CUST-PHONE-NUM-1 | PIC X(15) | Alpha | 15 | Primary phone number | `String` |
| CUST-PHONE-NUM-2 | PIC X(15) | Alpha | 15 | Secondary phone number | `String` |
| CUST-SSN | PIC 9(09) | Numeric | 9 | Social Security Number (PII) | `String` (encrypted) |
| CUST-GOVT-ISSUED-ID | PIC X(20) | Alpha | 20 | Government-issued ID (PII) | `String` (encrypted) |
| CUST-DOB-YYYY-MM-DD | PIC X(10) | Alpha-date | 10 | Date of birth (PII) | `LocalDate` |
| CUST-EFT-ACCOUNT-ID | PIC X(10) | Alpha | 10 | EFT / bank account for payments | `String` |
| CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alpha | 1 | Primary cardholder indicator | `boolean` |
| CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric | 3 | FICO credit score | `int` |
| FILLER | PIC X(168) | — | 168 | Reserved space | — |

**Business Rules:**
- Contains PII fields (SSN, DOB, Govt ID) — must encrypt at rest in Java
- Linked to accounts via Cross-Reference entity (CVACT03Y)
- `CUST-PRI-CARD-HOLDER-IND`: `Y` = primary, `N` = authorized user

---

## 4. Transaction Entity

**Copybook:** `CVTRA05Y.cpy` (21 lines) — Record length: ~350 bytes
**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`
**Key:** `TRAN-ID`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Java Type Suggestion |
|------------|-----------|-----------|-------:|------------------|---------------------|
| TRAN-ID | PIC X(16) | Alpha | 16 | Transaction identifier (primary key) | `String` |
| TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code (FK → Tran Type) | `String` |
| TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code | `int` |
| TRAN-SOURCE | PIC X(10) | Alpha | 10 | Transaction source (POS, ATM, Online) | `String` |
| TRAN-DESC | PIC X(100) | Alpha | 100 | Transaction description | `String` |
| TRAN-AMT | PIC S9(09)V99 | Signed decimal | 11.2 | Transaction amount | `BigDecimal` |
| TRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant identifier | `long` |
| TRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant name | `String` |
| TRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant city | `String` |
| TRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant ZIP code | `String` |
| TRAN-CARD-NUM | PIC X(16) | Alpha | 16 | Card number used (FK → Card) | `String` |
| TRAN-ORIG-TS | PIC X(26) | Alpha-timestamp | 26 | Original transaction timestamp | `Instant` |
| TRAN-PROC-TS | PIC X(26) | Alpha-timestamp | 26 | Processing timestamp | `Instant` |
| FILLER | PIC X(20) | — | 20 | Reserved space | — |

**Business Rules:**
- Transactions posted from Daily Transaction file during batch cycle
- `TRAN-CARD-NUM` links to Card and via Cross-Reference to Account/Customer
- Negative `TRAN-AMT` = credit; positive = debit

---

## 5. Daily Transaction Entity

**Copybook:** `CVTRA06Y.cpy` (21 lines) — Record length: ~350 bytes
**VSAM Dataset:** `AWS.M2.CARDDEMO.DAILYTRAN.VSAM.KSDS`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Java Type Suggestion |
|------------|-----------|-----------|-------:|------------------|---------------------|
| DALYTRAN-ID | PIC X(16) | Alpha | 16 | Daily transaction ID | `String` |
| DALYTRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code | `String` |
| DALYTRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Category code | `int` |
| DALYTRAN-SOURCE | PIC X(10) | Alpha | 10 | Source channel | `String` |
| DALYTRAN-DESC | PIC X(100) | Alpha | 100 | Description | `String` |
| DALYTRAN-AMT | PIC S9(09)V99 | Signed decimal | 11.2 | Amount | `BigDecimal` |
| DALYTRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant ID | `long` |
| DALYTRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant name | `String` |
| DALYTRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant city | `String` |
| DALYTRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant ZIP | `String` |
| DALYTRAN-CARD-NUM | PIC X(16) | Alpha | 16 | Card number | `String` |
| DALYTRAN-ORIG-TS | PIC X(26) | Alpha-timestamp | 26 | Original timestamp | `Instant` |
| DALYTRAN-PROC-TS | PIC X(26) | Alpha-timestamp | 26 | Processing timestamp | `Instant` |
| FILLER | PIC X(20) | — | 20 | Reserved space | — |

**Business Rules:**
- Staging area for unposted transactions
- Batch program CBTRN02C validates and posts to permanent Transaction file
- Rejected transactions written to DALYREJS output file

---

## 6. Card–Account Cross-Reference

**Copybook:** `CVACT03Y.cpy` (11 lines) — Record length: ~50 bytes
**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`
**Key:** `XREF-CARD-NUM`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Java Type Suggestion |
|------------|-----------|-----------|-------:|------------------|---------------------|
| XREF-CARD-NUM | PIC X(16) | Alpha | 16 | Card number (primary key) | `String` |
| XREF-CUST-ID | PIC 9(09) | Numeric | 9 | Customer ID (FK → Customer) | `long` |
| XREF-ACCT-ID | PIC 9(11) | Numeric | 11 | Account ID (FK → Account) | `long` |
| FILLER | PIC X(14) | — | 14 | Reserved space | — |

**Business Rules:**
- Central lookup: given a card number, find the customer and account
- Used by most online and batch programs for card-based operations
- In a relational model, this becomes a join table or foreign keys on Card

---

## 7. User Security Entity

**Copybook:** `CSUSR01Y.cpy` (26 lines) — Record length: ~80 bytes
**VSAM Dataset:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`
**Key:** `SEC-USR-ID`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Java Type Suggestion |
|------------|-----------|-----------|-------:|------------------|---------------------|
| SEC-USR-ID | PIC X(08) | Alpha | 8 | User login ID (primary key) | `String` |
| SEC-USR-FNAME | PIC X(20) | Alpha | 20 | User first name | `String` |
| SEC-USR-LNAME | PIC X(20) | Alpha | 20 | User last name | `String` |
| SEC-USR-PWD | PIC X(08) | Alpha | 8 | User password (plaintext!) | `String` (must hash) |
| SEC-USR-TYPE | PIC X(01) | Alpha | 1 | User type: `A` = Admin, `U` = Regular | `enum` |
| SEC-USR-FILLER | PIC X(23) | — | 23 | Reserved space | — |

**Business Rules:**
- Password stored in plaintext — critical security gap for modernization
- Two user types: Admin (full CRUD on users + transaction types) and Regular (card operations)
- Default credentials: ADMIN001/PASSWORD, USER0001/PASSWORD

---

## 8. Transaction Category Balance

**Copybook:** `CVTRA01Y.cpy` (13 lines) — Record length: ~50 bytes
**VSAM Dataset:** `AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS`
**Composite Key:** `TRANCAT-ACCT-ID` + `TRANCAT-TYPE-CD` + `TRANCAT-CD`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Java Type Suggestion |
|------------|-----------|-----------|-------:|------------------|---------------------|
| TRANCAT-ACCT-ID | PIC 9(11) | Numeric | 11 | Account ID (part of key) | `long` |
| TRANCAT-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code | `String` |
| TRANCAT-CD | PIC 9(04) | Numeric | 4 | Category code | `int` |
| TRAN-CAT-BAL | PIC S9(09)V99 | Signed decimal | 11.2 | Running balance for this category | `BigDecimal` |
| FILLER | PIC X(22) | — | 22 | Reserved space | — |

**Business Rules:**
- Tracks running balance per account per transaction type/category
- Updated during POSTTRAN batch job (CBTRN02C)
- Used in interest calculation (CBACT04C) to determine applicable rates

---

## 9. Discount / Interest Rate Group

**Copybook:** `CVTRA02Y.cpy` (13 lines) — Record length: ~50 bytes
**VSAM Dataset:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`
**Composite Key:** `DIS-ACCT-GROUP-ID` + `DIS-TRAN-TYPE-CD` + `DIS-TRAN-CAT-CD`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Java Type Suggestion |
|------------|-----------|-----------|-------:|------------------|---------------------|
| DIS-ACCT-GROUP-ID | PIC X(10) | Alpha | 10 | Account group identifier | `String` |
| DIS-TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code | `String` |
| DIS-TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code | `int` |
| DIS-INT-RATE | PIC S9(04)V99 | Signed decimal | 6.2 | Interest rate (%) | `BigDecimal` |
| FILLER | PIC X(28) | — | 28 | Reserved space | — |

**Business Rules:**
- Maps account group + transaction type/category to an interest rate
- Referenced by CBACT04C during interest calculation
- `ACCT-GROUP-ID` in Account entity links here

---

## 10. Transaction Type Reference

**Copybook:** `CVTRA03Y.cpy` (10 lines) — Record length: ~60 bytes
**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`
**Key:** `TRAN-TYPE`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Java Type Suggestion |
|------------|-----------|-----------|-------:|------------------|---------------------|
| TRAN-TYPE | PIC X(02) | Alpha | 2 | Type code (primary key) | `String` |
| TRAN-TYPE-DESC | PIC X(50) | Alpha | 50 | Type description | `String` |
| FILLER | PIC X(08) | — | 8 | Reserved space | — |

---

## 11. Transaction Category Type Reference

**Copybook:** `CVTRA04Y.cpy` (12 lines) — Record length: ~60 bytes
**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`
**Composite Key:** `TRAN-TYPE-CD` + `TRAN-CAT-CD`

| Field Name | PIC Clause | Data Type | Length | Business Meaning | Java Type Suggestion |
|------------|-----------|-----------|-------:|------------------|---------------------|
| TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code | `String` |
| TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Category code | `int` |
| TRAN-CAT-TYPE-DESC | PIC X(50) | Alpha | 50 | Category description | `String` |
| FILLER | PIC X(04) | — | 4 | Reserved space | — |

---

## 12. Common Area (COMMAREA)

**Copybook:** `COCOM01Y.cpy` (47 lines) — Used for CICS inter-program communication

| Field Name | PIC Clause | Length | Business Meaning |
|------------|-----------|-------:|------------------|
| CDEMO-FROM-TRANID | PIC X(04) | 4 | Source CICS transaction ID |
| CDEMO-FROM-PROGRAM | PIC X(08) | 8 | Source program name |
| CDEMO-TO-TRANID | PIC X(04) | 4 | Target CICS transaction ID |
| CDEMO-TO-PROGRAM | PIC X(08) | 8 | Target program name |
| CDEMO-USER-ID | PIC X(08) | 8 | Logged-in user ID |
| CDEMO-USER-TYPE | PIC X(01) | 1 | User type (A/U) |
| CDEMO-PGM-CONTEXT | PIC 9(01) | 1 | Program context flag |
| CDEMO-CUST-ID | PIC 9(09) | 9 | Selected customer ID |
| CDEMO-CUST-FNAME | PIC X(25) | 25 | Customer first name (display) |
| CDEMO-CUST-MNAME | PIC X(25) | 25 | Customer middle name |
| CDEMO-CUST-LNAME | PIC X(25) | 25 | Customer last name |
| CDEMO-ACCT-ID | PIC 9(11) | 11 | Selected account ID |
| CDEMO-ACCT-STATUS | PIC X(01) | 1 | Account status |
| CDEMO-CARD-NUM | PIC 9(16) | 16 | Selected card number |
| CDEMO-LAST-MAP | PIC X(7) | 7 | Last displayed BMS map |
| CDEMO-LAST-MAPSET | PIC X(7) | 7 | Last used BMS mapset |

**Modernization Note:** This COMMAREA maps to a session/context object in Java (e.g., `CardDemoSession` DTO or HTTP session attributes).

---

## 13. Export Record (Unified)

**Copybook:** `CVEXPORT.cpy` (103 lines) — Record length: ~500 bytes
**Used by:** CBEXPORT, CBIMPORT

A union/variant record with header fields plus a REDEFINES block for each entity type:

| Record Type Code | Entity | Key Fields in Export |
|-----------------|--------|---------------------|
| `C` | Customer | EXP-CUST-ID, name, address, SSN, DOB, FICO score |
| `A` | Account | EXP-ACCT-ID, balance, limits, dates, group ID |
| `T` | Transaction | EXP-TRAN-ID, amount, merchant info, timestamps |
| `X` | Cross-Reference | EXP-XREF-CARD-NUM, customer ID, account ID |
| `D` | Card | EXP-CARD-NUM, account ID, CVV, embossed name |

Header fields common to all types: `EXPORT-REC-TYPE`, `EXPORT-TIMESTAMP`, `EXPORT-SEQUENCE-NUM`, `EXPORT-BRANCH-ID`, `EXPORT-REGION-CODE`.

---

## 14. Statement Transaction Record

**Copybook:** `COSTM01.CPY` (38 lines) — Used by CBSTM03A/B for statement generation

| Field Name | PIC Clause | Length | Business Meaning |
|------------|-----------|-------:|------------------|
| TRNX-CARD-NUM | PIC X(16) | 16 | Card number |
| TRNX-ID | PIC X(16) | 16 | Transaction ID |
| TRNX-TYPE-CD | PIC X(02) | 2 | Type code |
| TRNX-CAT-CD | PIC 9(04) | 4 | Category code |
| TRNX-SOURCE | PIC X(10) | 10 | Source |
| TRNX-DESC | PIC X(100) | 100 | Description |
| TRNX-AMT | PIC S9(09)V99 | 11.2 | Amount |
| TRNX-MERCHANT-ID | PIC 9(09) | 9 | Merchant ID |
| TRNX-MERCHANT-NAME | PIC X(50) | 50 | Merchant name |
| TRNX-MERCHANT-CITY | PIC X(50) | 50 | Merchant city |
| TRNX-MERCHANT-ZIP | PIC X(10) | 10 | Merchant ZIP |
| TRNX-ORIG-TS | PIC X(26) | 26 | Original timestamp |
| TRNX-PROC-TS | PIC X(26) | 26 | Processing timestamp |

---

## 15. Report Layout Record

**Copybook:** `CVTRA07Y.cpy` (73 lines) — Used by CBTRN03C for printed reports

Contains report headers, detail line format, and total lines:
- **Detail line:** Transaction ID, Account ID, Type+Desc, Category+Desc, Source, Amount
- **Amount format:** `PIC -ZZZ,ZZZ,ZZZ.ZZ` (edited, with sign and comma separators)
- **Totals:** Page total, Account total, Grand total — all `PIC +ZZZ,ZZZ,ZZZ.ZZ`

---

## 16. VSAM File Summary

| VSAM Dataset | Key Type | Record Entity | Key Field(s) | Est. Record Size | Copybook |
|-------------|----------|---------------|-------------|----------------:|----------|
| ACCTDATA.VSAM.KSDS | KSDS | Account | ACCT-ID | 300 bytes | CVACT01Y |
| CARDDATA.VSAM.KSDS | KSDS | Card | CARD-NUM | 150 bytes | CVACT02Y |
| CUSTDATA.VSAM.KSDS | KSDS | Customer | CUST-ID | 500 bytes | CVCUS01Y |
| TRANSACT.VSAM.KSDS | KSDS | Transaction | TRAN-ID | 350 bytes | CVTRA05Y |
| DAILYTRAN.VSAM.KSDS | KSDS | Daily Transaction | DALYTRAN-ID | 350 bytes | CVTRA06Y |
| CARDXREF.VSAM.KSDS | KSDS | Cross-Reference | XREF-CARD-NUM | 50 bytes | CVACT03Y |
| USRSEC.VSAM.KSDS | KSDS | User Security | SEC-USR-ID | 80 bytes | CSUSR01Y |
| TCATBAL.VSAM.KSDS | KSDS | Category Balance | Composite | 50 bytes | CVTRA01Y |
| DISCGRP.VSAM.KSDS | KSDS | Discount Group | Composite | 50 bytes | CVTRA02Y |
| TRANTYPE.VSAM.KSDS | KSDS | Transaction Type | TRAN-TYPE | 60 bytes | CVTRA03Y |
| TRANCATG.VSAM.KSDS | KSDS | Transaction Category | Composite | 60 bytes | CVTRA04Y |

### Entity-Relationship Summary (Logical)

```
Customer (1) ──── (M) Cross-Reference (M) ──── (1) Account
                        │
                        └── Card Number (PK)
                               │
                               ▼
                        Card (M) ──── (1) Account

Transaction (M) ──── (1) Card (via TRAN-CARD-NUM)
Transaction (M) ──── (1) Transaction Type
Transaction (M) ──── (1) Transaction Category

Account (1) ──── (M) Category Balance
Account ──── (via GROUP-ID) ──── Discount Group ──── Interest Rate

User Security (standalone, no FK to business entities)
```
