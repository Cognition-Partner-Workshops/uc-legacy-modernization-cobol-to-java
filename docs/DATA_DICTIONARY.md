# CardDemo Data Dictionary

> **System**: CardDemo -- Credit Card Management System
> **Source**: COBOL Copybooks (`app/cpy/`)
> **Date**: 2026-03-25

---

## Table of Contents

1. [Entity Relationship Overview](#entity-relationship-overview)
2. [Customer Entity](#1-customer-entity)
3. [Account Entity](#2-account-entity)
4. [Credit Card Entity](#3-credit-card-entity)
5. [Card Cross-Reference Entity](#4-card-cross-reference-entity)
6. [Transaction Entity (Online)](#5-transaction-entity-online)
7. [Transaction Entity (Daily/Batch)](#6-transaction-entity-dailybatch)
8. [Transaction Category Balance Entity](#7-transaction-category-balance-entity)
9. [Disclosure Group Entity](#8-disclosure-group-entity)
10. [Transaction Type Reference](#9-transaction-type-reference)
11. [Transaction Category Reference](#10-transaction-category-reference)
12. [User Security Entity](#11-user-security-entity)
13. [Statement Transaction Record](#12-statement-transaction-record)
14. [Export Record (Multi-Type)](#13-export-record-multi-type)
15. [Communication Area (COMMAREA)](#14-communication-area-commarea)
16. [Date/Time Structures](#15-datetime-structures)
17. [Abend Data](#16-abend-data)
18. [PIC Clause Reference](#pic-clause-reference)

---

## Entity Relationship Overview

```
                          +-----------------+
                          |    CUSTOMER     |
                          |  (CVCUS01Y)     |
                          |  PK: CUST-ID    |
                          +--------+--------+
                                   |
                                   | 1:N
                                   v
+----------------+       +------------------+       +-----------------+
| CARD XREF      |<------+    ACCOUNT       |       | DISCLOSURE GRP  |
| (CVACT03Y)     |  1:N  |   (CVACT01Y)     +------>| (CVTRA02Y)      |
| FK: CUST-ID    |       |  PK: ACCT-ID     | via   | PK: GROUP-ID +  |
| FK: ACCT-ID    |       |  FK: GROUP-ID    | GRP-ID|    TYPE + CAT   |
+-------+--------+       +--------+---------+       +-----------------+
        |                          |
        | 1:1                      | 1:N
        v                          v
+----------------+       +------------------+       +-----------------+
|  CREDIT CARD   |       | TRAN CAT BALANCE |       | TRANSACTION     |
|  (CVACT02Y)    |       |   (CVTRA01Y)     |       | (CVTRA05Y)      |
| PK: CARD-NUM   |       | PK: ACCT + TYPE  |       | PK: TRAN-ID     |
| FK: ACCT-ID    |       |     + CAT        |       | FK: CARD-NUM    |
+----------------+       +------------------+       +-----------------+
                                                            |
                                                            | references
                                                            v
                                                    +-----------------+
                                                    | TRAN TYPE       |
                                                    | (CVTRA03Y)      |
                                                    | PK: TRAN-TYPE   |
                                                    +-----------------+
                                                            |
                                                            | 1:N
                                                            v
                                                    +-----------------+
                                                    | TRAN CATEGORY   |
                                                    | (CVTRA04Y)      |
                                                    | PK: TYPE + CAT  |
                                                    +-----------------+
```

---

## 1. Customer Entity

**Copybook**: `CVCUS01Y.cpy` | **Record Name**: `CUSTOMER-RECORD` | **Record Length**: 500 bytes | **Storage**: VSAM KSDS (`CUSTFILE`)

| Field Name | Business Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| `CUST-ID` | Customer ID | `PIC 9(09)` | Numeric | 9 digits | **Primary Key**. Unique customer identifier |
| `CUST-FIRST-NAME` | First Name | `PIC X(25)` | Alpha | 25 chars | Customer first name |
| `CUST-MIDDLE-NAME` | Middle Name | `PIC X(25)` | Alpha | 25 chars | Customer middle name |
| `CUST-LAST-NAME` | Last Name | `PIC X(25)` | Alpha | 25 chars | Customer last name |
| `CUST-ADDR-LINE-1` | Address Line 1 | `PIC X(50)` | Alpha | 50 chars | Street address |
| `CUST-ADDR-LINE-2` | Address Line 2 | `PIC X(50)` | Alpha | 50 chars | Suite/apartment |
| `CUST-ADDR-LINE-3` | Address Line 3 | `PIC X(50)` | Alpha | 50 chars | Additional address |
| `CUST-ADDR-STATE-CD` | State Code | `PIC X(02)` | Alpha | 2 chars | US state code (validated against CSLKPCDY) |
| `CUST-ADDR-COUNTRY-CD` | Country Code | `PIC X(03)` | Alpha | 3 chars | ISO country code |
| `CUST-ADDR-ZIP` | ZIP Code | `PIC X(10)` | Alpha | 10 chars | ZIP/postal code (validated against CSLKPCDY) |
| `CUST-PHONE-NUM-1` | Primary Phone | `PIC X(15)` | Alpha | 15 chars | Primary phone number (area code validated) |
| `CUST-PHONE-NUM-2` | Secondary Phone | `PIC X(15)` | Alpha | 15 chars | Secondary phone number |
| `CUST-SSN` | Social Security Number | `PIC 9(09)` | Numeric | 9 digits | SSN (PII -- sensitive) |
| `CUST-GOVT-ISSUED-ID` | Government ID | `PIC X(20)` | Alpha | 20 chars | Government-issued identification |
| `CUST-DOB-YYYY-MM-DD` | Date of Birth | `PIC X(10)` | Date | 10 chars | Format: YYYY-MM-DD |
| `CUST-EFT-ACCOUNT-ID` | EFT Account ID | `PIC X(10)` | Alpha | 10 chars | Electronic funds transfer account |
| `CUST-PRI-CARD-HOLDER-IND` | Primary Cardholder | `PIC X(01)` | Flag | 1 char | Primary cardholder indicator (Y/N) |
| `CUST-FICO-CREDIT-SCORE` | FICO Score | `PIC 9(03)` | Numeric | 3 digits | Credit score (300-850 range) |
| `FILLER` | -- | `PIC X(168)` | Filler | 168 bytes | Reserved space |

**Business Rules**:
- One customer can hold multiple accounts
- SSN and Government ID are sensitive PII fields
- State code, ZIP, and phone area code are validated against lookup tables in `CSLKPCDY.cpy`
- FICO score determines credit eligibility

---

## 2. Account Entity

**Copybook**: `CVACT01Y.cpy` | **Record Name**: `ACCOUNT-RECORD` | **Record Length**: 300 bytes | **Storage**: VSAM KSDS (`ACCTFILE`)

| Field Name | Business Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| `ACCT-ID` | Account ID | `PIC 9(11)` | Numeric | 11 digits | **Primary Key**. Unique account number |
| `ACCT-ACTIVE-STATUS` | Active Status | `PIC X(01)` | Flag | 1 char | Account status: 'Y' = Active, 'N' = Inactive |
| `ACCT-CURR-BAL` | Current Balance | `PIC S9(10)V99` | Signed Decimal | 12 digits (10.2) | Current account balance (can be negative) |
| `ACCT-CREDIT-LIMIT` | Credit Limit | `PIC S9(10)V99` | Signed Decimal | 12 digits (10.2) | Maximum credit allowance |
| `ACCT-CASH-CREDIT-LIMIT` | Cash Advance Limit | `PIC S9(10)V99` | Signed Decimal | 12 digits (10.2) | Cash advance credit limit |
| `ACCT-OPEN-DATE` | Open Date | `PIC X(10)` | Date | 10 chars | Account opening date (YYYY-MM-DD) |
| `ACCT-EXPIRAION-DATE` | Expiration Date | `PIC X(10)` | Date | 10 chars | Account expiration date (YYYY-MM-DD) |
| `ACCT-REISSUE-DATE` | Reissue Date | `PIC X(10)` | Date | 10 chars | Last card reissue date (YYYY-MM-DD) |
| `ACCT-CURR-CYC-CREDIT` | Cycle Credits | `PIC S9(10)V99` | Signed Decimal | 12 digits (10.2) | Current billing cycle credit total |
| `ACCT-CURR-CYC-DEBIT` | Cycle Debits | `PIC S9(10)V99` | Signed Decimal | 12 digits (10.2) | Current billing cycle debit total |
| `ACCT-ADDR-ZIP` | Account ZIP | `PIC X(10)` | Alpha | 10 chars | Account billing ZIP code |
| `ACCT-GROUP-ID` | Disclosure Group | `PIC X(10)` | Alpha | 10 chars | **FK** to Disclosure Group -- determines interest rates |
| `FILLER` | -- | `PIC X(178)` | Filler | 178 bytes | Reserved space |

**Business Rules**:
- Account balance is updated by transaction posting (CBTRN02C) and bill payment (COBIL00C)
- Credit limit and cash advance limit are separately tracked
- Disclosure Group ID links to interest rate tables
- Cycle credits/debits reset at statement generation

---

## 3. Credit Card Entity

**Copybook**: `CVACT02Y.cpy` | **Record Name**: `CARD-RECORD` | **Record Length**: 150 bytes | **Storage**: VSAM KSDS (`CARDFILE`)

| Field Name | Business Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| `CARD-NUM` | Card Number | `PIC X(16)` | Alpha | 16 chars | **Primary Key**. Credit card number (PAN) |
| `CARD-ACCT-ID` | Account ID | `PIC 9(11)` | Numeric | 11 digits | **FK** to Account. Associated account |
| `CARD-CVV-CD` | CVV Code | `PIC 9(03)` | Numeric | 3 digits | Card verification value (PCI-sensitive) |
| `CARD-EMBOSSED-NAME` | Embossed Name | `PIC X(50)` | Alpha | 50 chars | Name embossed on physical card |
| `CARD-EXPIRAION-DATE` | Expiration Date | `PIC X(10)` | Date | 10 chars | Card expiration date (YYYY-MM-DD) |
| `CARD-ACTIVE-STATUS` | Active Status | `PIC X(01)` | Flag | 1 char | Card status: 'Y' = Active, 'N' = Inactive |
| `FILLER` | -- | `PIC X(59)` | Filler | 59 bytes | Reserved space |

**Business Rules**:
- Multiple cards can be issued against one account
- CVV is a PCI-DSS sensitive field
- Card number is the primary lookup key for transactions
- Note: field name `CARD-EXPIRAION-DATE` contains a typo (missing 'T') in original source

---

## 4. Card Cross-Reference Entity

**Copybook**: `CVACT03Y.cpy` | **Record Name**: `CARD-XREF-RECORD` | **Record Length**: 50 bytes | **Storage**: VSAM KSDS (`XREFFILE`)

| Field Name | Business Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| `XREF-CARD-NUM` | Card Number | `PIC X(16)` | Alpha | 16 chars | **PK/FK** to Card. Card number being referenced |
| `XREF-CUST-ID` | Customer ID | `PIC 9(09)` | Numeric | 9 digits | **FK** to Customer. Owning customer |
| `XREF-ACCT-ID` | Account ID | `PIC 9(11)` | Numeric | 11 digits | **FK** to Account. Associated account |
| `FILLER` | -- | `PIC X(14)` | Filler | 14 bytes | Reserved space |

**Business Rules**:
- This is a junction/bridge table linking cards to both customers and accounts
- Enables lookup of customer and account from a card number
- One card maps to exactly one customer and one account

---

## 5. Transaction Entity (Online)

**Copybook**: `CVTRA05Y.cpy` | **Record Name**: `TRAN-RECORD` | **Record Length**: 350 bytes | **Storage**: VSAM KSDS (`TRANSACT`)

| Field Name | Business Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| `TRAN-ID` | Transaction ID | `PIC X(16)` | Alpha | 16 chars | **Primary Key**. Unique transaction identifier |
| `TRAN-TYPE-CD` | Transaction Type | `PIC X(02)` | Code | 2 chars | **FK** to Transaction Type (e.g., "SA"=Sale, "CR"=Credit) |
| `TRAN-CAT-CD` | Category Code | `PIC 9(04)` | Numeric | 4 digits | **FK** to Transaction Category |
| `TRAN-SOURCE` | Source | `PIC X(10)` | Alpha | 10 chars | Transaction origination source |
| `TRAN-DESC` | Description | `PIC X(100)` | Alpha | 100 chars | Free-text transaction description |
| `TRAN-AMT` | Amount | `PIC S9(09)V99` | Signed Decimal | 11 digits (9.2) | Transaction amount (signed; negative = credit) |
| `TRAN-MERCHANT-ID` | Merchant ID | `PIC 9(09)` | Numeric | 9 digits | Merchant identifier |
| `TRAN-MERCHANT-NAME` | Merchant Name | `PIC X(50)` | Alpha | 50 chars | Merchant business name |
| `TRAN-MERCHANT-CITY` | Merchant City | `PIC X(50)` | Alpha | 50 chars | Merchant city |
| `TRAN-MERCHANT-ZIP` | Merchant ZIP | `PIC X(10)` | Alpha | 10 chars | Merchant ZIP/postal code |
| `TRAN-CARD-NUM` | Card Number | `PIC X(16)` | Alpha | 16 chars | **FK** to Card. Card used for transaction |
| `TRAN-ORIG-TS` | Origination Timestamp | `PIC X(26)` | Timestamp | 26 chars | When transaction was initiated |
| `TRAN-PROC-TS` | Processing Timestamp | `PIC X(26)` | Timestamp | 26 chars | When transaction was processed |
| `FILLER` | -- | `PIC X(20)` | Filler | 20 bytes | Reserved space |

**Business Rules**:
- Transactions are created online (COTRN02C) or loaded via batch (CBTRN01C)
- Amount is signed: positive = debit/charge, negative = credit/refund
- Each transaction references a card, which links to an account and customer
- Transaction type + category determines interest rate via disclosure group

---

## 6. Transaction Entity (Daily/Batch)

**Copybook**: `CVTRA06Y.cpy` | **Record Name**: `DALYTRAN-RECORD` | **Record Length**: 350 bytes | **Storage**: Sequential file / VSAM

Identical structure to the Online Transaction Entity but with `DALYTRAN-` prefix. Used for daily batch processing input.

| Field Name | Business Name | PIC Clause | Maps to Online Field |
|---|---|---|---|
| `DALYTRAN-ID` | Transaction ID | `PIC X(16)` | TRAN-ID |
| `DALYTRAN-TYPE-CD` | Transaction Type | `PIC X(02)` | TRAN-TYPE-CD |
| `DALYTRAN-CAT-CD` | Category Code | `PIC 9(04)` | TRAN-CAT-CD |
| `DALYTRAN-SOURCE` | Source | `PIC X(10)` | TRAN-SOURCE |
| `DALYTRAN-DESC` | Description | `PIC X(100)` | TRAN-DESC |
| `DALYTRAN-AMT` | Amount | `PIC S9(09)V99` | TRAN-AMT |
| `DALYTRAN-MERCHANT-ID` | Merchant ID | `PIC 9(09)` | TRAN-MERCHANT-ID |
| `DALYTRAN-MERCHANT-NAME` | Merchant Name | `PIC X(50)` | TRAN-MERCHANT-NAME |
| `DALYTRAN-MERCHANT-CITY` | Merchant City | `PIC X(50)` | TRAN-MERCHANT-CITY |
| `DALYTRAN-MERCHANT-ZIP` | Merchant ZIP | `PIC X(10)` | TRAN-MERCHANT-ZIP |
| `DALYTRAN-CARD-NUM` | Card Number | `PIC X(16)` | TRAN-CARD-NUM |
| `DALYTRAN-ORIG-TS` | Origination Timestamp | `PIC X(26)` | TRAN-ORIG-TS |
| `DALYTRAN-PROC-TS` | Processing Timestamp | `PIC X(26)` | TRAN-PROC-TS |
| `FILLER` | -- | `PIC X(20)` | FILLER |

---

## 7. Transaction Category Balance Entity

**Copybook**: `CVTRA01Y.cpy` | **Record Name**: `TRAN-CAT-BAL-RECORD` | **Record Length**: 50 bytes | **Storage**: VSAM KSDS (`TCATBALF`)

| Field Name | Business Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| `TRANCAT-ACCT-ID` | Account ID | `PIC 9(11)` | Numeric | 11 digits | **Composite PK part 1 / FK** to Account |
| `TRANCAT-TYPE-CD` | Transaction Type | `PIC X(02)` | Code | 2 chars | **Composite PK part 2 / FK** to Tran Type |
| `TRANCAT-CD` | Category Code | `PIC 9(04)` | Numeric | 4 digits | **Composite PK part 3 / FK** to Tran Category |
| `TRAN-CAT-BAL` | Category Balance | `PIC S9(09)V99` | Signed Decimal | 11 digits (9.2) | Running balance for this account/type/category |
| `FILLER` | -- | `PIC X(22)` | Filler | 22 bytes | Reserved space |

**Business Rules**:
- Tracks running balance per account per transaction type per category
- Updated during transaction posting (CBTRN02C)
- Used by interest calculation (CBACT04C) to compute per-category interest

---

## 8. Disclosure Group Entity

**Copybook**: `CVTRA02Y.cpy` | **Record Name**: `DIS-GROUP-RECORD` | **Record Length**: 50 bytes | **Storage**: VSAM KSDS (`DISCGRP`)

| Field Name | Business Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| `DIS-ACCT-GROUP-ID` | Account Group ID | `PIC X(10)` | Alpha | 10 chars | **Composite PK part 1**. Disclosure group identifier |
| `DIS-TRAN-TYPE-CD` | Transaction Type | `PIC X(02)` | Code | 2 chars | **Composite PK part 2 / FK** to Tran Type |
| `DIS-TRAN-CAT-CD` | Category Code | `PIC 9(04)` | Numeric | 4 digits | **Composite PK part 3 / FK** to Tran Category |
| `DIS-INT-RATE` | Interest Rate | `PIC S9(04)V99` | Signed Decimal | 6 digits (4.2) | Annual interest rate percentage |
| `FILLER` | -- | `PIC X(28)` | Filler | 28 bytes | Reserved space |

**Business Rules**:
- Links account groups to interest rates by transaction type and category
- Account's `ACCT-GROUP-ID` references this table
- Used by CBACT04C (interest calculation) to determine applicable rate

---

## 9. Transaction Type Reference

**Copybook**: `CVTRA03Y.cpy` | **Record Name**: `TRAN-TYPE-RECORD` | **Record Length**: 60 bytes | **Storage**: VSAM KSDS (`TRANTYPE`)

| Field Name | Business Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| `TRAN-TYPE` | Type Code | `PIC X(02)` | Code | 2 chars | **Primary Key**. Transaction type code (e.g., SA, CR, PA) |
| `TRAN-TYPE-DESC` | Type Description | `PIC X(50)` | Alpha | 50 chars | Human-readable description |
| `FILLER` | -- | `PIC X(08)` | Filler | 8 bytes | Reserved space |

**Known Type Codes**:
- `SA` -- Sale / Purchase
- `CR` -- Credit / Refund
- `PA` -- Payment
- `CA` -- Cash Advance

---

## 10. Transaction Category Reference

**Copybook**: `CVTRA04Y.cpy` | **Record Name**: `TRAN-CAT-RECORD` | **Record Length**: 60 bytes | **Storage**: VSAM KSDS (`TRANCATG`)

| Field Name | Business Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| `TRAN-TYPE-CD` | Type Code | `PIC X(02)` | Code | 2 chars | **Composite PK part 1 / FK** to Tran Type |
| `TRAN-CAT-CD` | Category Code | `PIC 9(04)` | Numeric | 4 digits | **Composite PK part 2**. Category within type |
| `TRAN-CAT-TYPE-DESC` | Category Description | `PIC X(50)` | Alpha | 50 chars | Human-readable description |
| `FILLER` | -- | `PIC X(04)` | Filler | 4 bytes | Reserved space |

---

## 11. User Security Entity

**Copybook**: `CSUSR01Y.cpy` | **Record Name**: `SEC-USER-DATA` | **Record Length**: 80 bytes | **Storage**: VSAM KSDS (`USRSEC`)

| Field Name | Business Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| `SEC-USR-ID` | User ID | `PIC X(08)` | Alpha | 8 chars | **Primary Key**. Login user ID |
| `SEC-USR-FNAME` | First Name | `PIC X(20)` | Alpha | 20 chars | User first name |
| `SEC-USR-LNAME` | Last Name | `PIC X(20)` | Alpha | 20 chars | User last name |
| `SEC-USR-PWD` | Password | `PIC X(08)` | Alpha | 8 chars | Plain-text password (security risk in modernization) |
| `SEC-USR-TYPE` | User Type | `PIC X(01)` | Code | 1 char | 'A' = Administrator, 'U' = Regular User |
| `SEC-USR-FILLER` | -- | `PIC X(23)` | Filler | 23 bytes | Reserved space |

**Business Rules**:
- Passwords stored in plain text (modernization must add hashing)
- User type determines menu routing: 'A' -> Admin Menu, 'U' -> Main Menu
- Default credentials: ADMIN001/PASSWORD, USER0001/PASSWORD

---

## 12. Statement Transaction Record

**Copybook**: `COSTM01.CPY` | **Record Name**: `TRNX-RECORD` | **Record Length**: ~340 bytes | **Storage**: Intermediate work file

| Field Name | Business Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| `TRNX-CARD-NUM` | Card Number | `PIC X(16)` | Alpha | 16 chars | **Composite Key part 1**. Card number |
| `TRNX-ID` | Transaction ID | `PIC X(16)` | Alpha | 16 chars | **Composite Key part 2**. Transaction ID |
| `TRNX-TYPE-CD` | Type Code | `PIC X(02)` | Code | 2 chars | Transaction type |
| `TRNX-CAT-CD` | Category Code | `PIC 9(04)` | Numeric | 4 digits | Transaction category |
| `TRNX-SOURCE` | Source | `PIC X(10)` | Alpha | 10 chars | Origination source |
| `TRNX-DESC` | Description | `PIC X(100)` | Alpha | 100 chars | Transaction description |
| `TRNX-AMT` | Amount | `PIC S9(09)V99` | Signed Decimal | 11 digits (9.2) | Transaction amount |
| `TRNX-MERCHANT-ID` | Merchant ID | `PIC 9(09)` | Numeric | 9 digits | Merchant identifier |
| `TRNX-MERCHANT-NAME` | Merchant Name | `PIC X(50)` | Alpha | 50 chars | Merchant name |
| `TRNX-MERCHANT-CITY` | Merchant City | `PIC X(50)` | Alpha | 50 chars | Merchant city |
| `TRNX-MERCHANT-ZIP` | Merchant ZIP | `PIC X(10)` | Alpha | 10 chars | Merchant ZIP |
| `TRNX-ORIG-TS` | Origination Timestamp | `PIC X(26)` | Timestamp | 26 chars | Origination time |
| `TRNX-PROC-TS` | Processing Timestamp | `PIC X(26)` | Timestamp | 26 chars | Processing time |

**Business Rules**:
- Keyed by Card Number + Transaction ID (composite key for SORT)
- Used by statement generation (CBSTM03A) for ordered output
- Derived from CVTRA05Y with different key structure

---

## 13. Export Record (Multi-Type)

**Copybook**: `CVEXPORT.cpy` | **Record Name**: `EXPORT-RECORD` | **Record Length**: 500 bytes | **Storage**: Sequential file

### Common Header (all record types)

| Field Name | Business Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| `EXPORT-REC-TYPE` | Record Type | `PIC X(1)` | Code | 1 char | Discriminator: C=Customer, A=Account, T=Transaction, X=Xref, D=Card |
| `EXPORT-TIMESTAMP` | Timestamp | `PIC X(26)` | Timestamp | 26 chars | Export timestamp |
| `EXPORT-SEQUENCE-NUM` | Sequence Number | `PIC 9(9) COMP` | Binary | 4 bytes | Record sequence within export |
| `EXPORT-BRANCH-ID` | Branch ID | `PIC X(4)` | Alpha | 4 chars | Originating branch |
| `EXPORT-REGION-CODE` | Region Code | `PIC X(5)` | Alpha | 5 chars | Region code |
| `EXPORT-RECORD-DATA` | Record Data | `PIC X(460)` | Alpha | 460 bytes | Type-specific data (REDEFINES) |

### Record Type Variants (via REDEFINES)

The `EXPORT-RECORD-DATA` field is redefined for each entity type:
- **Customer** (`EXPORT-CUSTOMER-DATA`): Mirrors CVCUS01Y with COMP fields for CUST-ID and FICO
- **Account** (`EXPORT-ACCOUNT-DATA`): Mirrors CVACT01Y with COMP-3 for balances
- **Transaction** (`EXPORT-TRANSACTION-DATA`): Mirrors CVTRA05Y with COMP-3/COMP for amounts
- **Card Cross-Ref** (`EXPORT-CARD-XREF-DATA`): Mirrors CVACT03Y with COMP for ACCT-ID
- **Card** (`EXPORT-CARD-DATA`): Mirrors CVACT02Y with COMP for ACCT-ID and CVV

**Business Rules**:
- Uses storage-optimized formats (COMP, COMP-3) for numeric fields
- Branch migration use case: export from one branch, import to another
- Record type discriminator enables multi-entity sequential file

---

## 14. Communication Area (COMMAREA)

**Copybook**: `COCOM01Y.cpy` | **Record Name**: `CARDDEMO-COMMAREA` | **Storage**: CICS COMMAREA (passed via XCTL/LINK)

### General Information Section

| Field Name | Business Name | PIC Clause | Description |
|---|---|---|---|
| `CDEMO-FROM-TRANID` | Source Transaction | `PIC X(04)` | CICS transaction ID of calling program |
| `CDEMO-FROM-PROGRAM` | Source Program | `PIC X(08)` | Program name of caller |
| `CDEMO-TO-TRANID` | Target Transaction | `PIC X(04)` | CICS transaction ID of target program |
| `CDEMO-TO-PROGRAM` | Target Program | `PIC X(08)` | Program name of target |
| `CDEMO-USER-ID` | User ID | `PIC X(08)` | Authenticated user ID |
| `CDEMO-USER-TYPE` | User Type | `PIC X(01)` | 'A' = Admin, 'U' = User (88-level conditions) |
| `CDEMO-PGM-CONTEXT` | Program Context | `PIC 9(01)` | 0 = First entry, 1 = Re-entry |

### Customer Information Section

| Field Name | Business Name | PIC Clause | Description |
|---|---|---|---|
| `CDEMO-CUST-ID` | Customer ID | `PIC 9(09)` | Current customer context |
| `CDEMO-CUST-FNAME` | First Name | `PIC X(25)` | Customer first name |
| `CDEMO-CUST-MNAME` | Middle Name | `PIC X(25)` | Customer middle name |
| `CDEMO-CUST-LNAME` | Last Name | `PIC X(25)` | Customer last name |

### Account & Card Section

| Field Name | Business Name | PIC Clause | Description |
|---|---|---|---|
| `CDEMO-ACCT-ID` | Account ID | `PIC 9(11)` | Current account context |
| `CDEMO-ACCT-STATUS` | Account Status | `PIC X(01)` | Current account status |
| `CDEMO-CARD-NUM` | Card Number | `PIC 9(16)` | Current card context |

### Navigation Section

| Field Name | Business Name | PIC Clause | Description |
|---|---|---|---|
| `CDEMO-LAST-MAP` | Last Map | `PIC X(7)` | Last BMS map displayed |
| `CDEMO-LAST-MAPSET` | Last Mapset | `PIC X(7)` | Last BMS mapset used |

---

## 15. Date/Time Structures

### Current Date/Time (`CSDAT01Y.cpy`)

| Field Name | PIC Clause | Description |
|---|---|---|
| `WS-CURDATE-YEAR` | `PIC 9(04)` | Current year (4 digits) |
| `WS-CURDATE-MONTH` | `PIC 9(02)` | Current month |
| `WS-CURDATE-DAY` | `PIC 9(02)` | Current day |
| `WS-CURTIME-HOURS` | `PIC 9(02)` | Current hours |
| `WS-CURTIME-MINUTE` | `PIC 9(02)` | Current minutes |
| `WS-CURTIME-SECOND` | `PIC 9(02)` | Current seconds |
| `WS-TIMESTAMP` | `PIC (26 total)` | Full timestamp: YYYY-MM-DD HH:MM:SS.NNNNNN |

### Date Conversion (`CODATECN.cpy`)

| Field Name | PIC Clause | Description |
|---|---|---|
| `CODATECN-TYPE` | `PIC X` | Input format: '1' = YYYYMMDD, '2' = YYYY-MM-DD |
| `CODATECN-INP-DATE` | `PIC X(20)` | Input date string |
| `CODATECN-OUTTYPE` | `PIC X` | Output format: '1' = YYYY-MM-DD, '2' = YYYYMMDD |
| `CODATECN-0UT-DATE` | `PIC X(20)` | Output date string |
| `CODATECN-ERROR-MSG` | `PIC X(38)` | Error message if conversion fails |

---

## 16. Abend Data

**Copybook**: `CSMSG02Y.cpy` | **Record Name**: `ABEND-DATA`

| Field Name | Business Name | PIC Clause | Description |
|---|---|---|---|
| `ABEND-CODE` | Abend Code | `PIC X(4)` | System or application abend code |
| `ABEND-CULPRIT` | Culprit Program | `PIC X(8)` | Program that caused the abend |
| `ABEND-REASON` | Reason | `PIC X(50)` | Description of abend cause |
| `ABEND-MSG` | Message | `PIC X(72)` | Full error message for display/logging |

---

## PIC Clause Reference

For modernization teams unfamiliar with COBOL PIC clauses:

| PIC Clause | Java Equivalent | Description | Example |
|---|---|---|---|
| `PIC X(n)` | `String` (length n) | Alphanumeric, fixed width | `PIC X(25)` -> 25-char string |
| `PIC 9(n)` | `long` or `BigInteger` | Unsigned integer, n digits | `PIC 9(11)` -> 11-digit number |
| `PIC S9(n)V99` | `BigDecimal` | Signed decimal with 2 decimal places | `PIC S9(10)V99` -> +/- 10 digits.2 |
| `PIC S9(n)V99 COMP-3` | `BigDecimal` | Packed decimal (storage-optimized) | Same value, fewer bytes |
| `PIC 9(n) COMP` | `int` or `long` | Binary integer (storage-optimized) | `PIC 9(9) COMP` -> 4-byte binary |
| `FILLER` | -- (not mapped) | Unused padding bytes | Space for future fields |
| `REDEFINES` | Union / type cast | Same memory, different interpretation | Multi-type record layouts |
| `88 level` | `enum` / boolean | Condition names (named values) | `88 ADMIN VALUE 'A'` |
| `OCCURS n TIMES` | Array / `List<>` | Repeating group | `OCCURS 3 TIMES` -> array[3] |

---

## VSAM File Summary

| Logical Name | Dataset | Key Field | Record Size | Entity |
|---|---|---|---|---|
| `ACCTFILE` | Account Master | `ACCT-ID` (11 bytes) | 300 bytes | Account |
| `CARDFILE` | Card Master | `CARD-NUM` (16 bytes) | 150 bytes | Credit Card |
| `CUSTFILE` | Customer Master | `CUST-ID` (9 bytes) | 500 bytes | Customer |
| `XREFFILE` | Card Cross-Reference | `XREF-CARD-NUM` (16 bytes) | 50 bytes | Card Xref |
| `TRANSACT` | Transaction Master | `TRAN-ID` (16 bytes) | 350 bytes | Transaction |
| `USRSEC` | User Security | `SEC-USR-ID` (8 bytes) | 80 bytes | User Security |
| `TCATBALF` | Trans Category Balance | Composite (17 bytes) | 50 bytes | Tran Cat Balance |
| `DISCGRP` | Disclosure Group | Composite (16 bytes) | 50 bytes | Disclosure Group |
| `TRANTYPE` | Transaction Type | `TRAN-TYPE` (2 bytes) | 60 bytes | Tran Type |
| `TRANCATG` | Transaction Category | Composite (6 bytes) | 60 bytes | Tran Category |
