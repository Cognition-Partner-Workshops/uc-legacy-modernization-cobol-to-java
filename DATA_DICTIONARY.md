# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Source:** COBOL copybooks in `app/cpy/` and optional module copybook directories

---

## Table of Contents

1. [VSAM File Inventory](#vsam-file-inventory)
2. [Account Entity (CVACT01Y)](#account-entity)
3. [Card Entity (CVACT02Y)](#card-entity)
4. [Card Cross-Reference Entity (CVACT03Y)](#card-cross-reference-entity)
5. [Customer Entity (CVCUS01Y)](#customer-entity)
6. [Transaction Entity (CVTRA05Y)](#transaction-entity)
7. [Daily Transaction Entity (CVTRA06Y)](#daily-transaction-entity)
8. [Transaction Category Balance Entity (CVTRA01Y)](#transaction-category-balance-entity)
9. [Disclosure Group Entity (CVTRA02Y)](#disclosure-group-entity)
10. [Transaction Type Entity (CVTRA03Y)](#transaction-type-entity)
11. [Transaction Category Type Entity (CVTRA04Y)](#transaction-category-type-entity)
12. [User Security Entity (CSUSR01Y)](#user-security-entity)
13. [Communication Area (COCOM01Y)](#communication-area)
14. [Card Detail Record (CVCRD01Y)](#card-detail-record)
15. [Statement Transaction Layout (COSTM01)](#statement-transaction-layout)
16. [Export Record Layout (CVEXPORT)](#export-record-layout)
17. [Transaction Report Layout (CVTRA07Y)](#transaction-report-layout)
18. [Date Conversion Record (CODATECN)](#date-conversion-record)
19. [Lookup Code Table (CSLKPCDY)](#lookup-code-table)
20. [Entity Relationship Summary](#entity-relationship-summary)

---

## VSAM File Inventory

| VSAM Dataset | Type | Key | Rec Len | Business Entity | Copybook |
|---|---|---|---:|---|---|
| ACCTDATA.VSAM.KSDS | KSDS | Account ID (11) | 300 | Account Master | CVACT01Y |
| CARDDATA.VSAM.KSDS | KSDS | Card Number (16) | 150 | Card Master | CVACT02Y |
| CARDXREF.VSAM.KSDS | KSDS | Card Number (16) | 50 | Card-Account XREF | CVACT03Y |
| CUSTDATA.VSAM.KSDS | KSDS | Customer ID (9) | 500 | Customer Master | CVCUS01Y |
| TRANSACT.VSAM.KSDS | KSDS | Transaction ID (16) | 350 | Transaction Master | CVTRA05Y |
| DALYTRAN.PS | Sequential | — | 350 | Daily Transactions | CVTRA06Y |
| TCATBALF.VSAM.KSDS | KSDS | Acct+Type+Cat (17) | 50 | Category Balance | CVTRA01Y |
| DISCGRP.VSAM.KSDS | KSDS | Group+Type+Cat (16) | 50 | Disclosure Group | CVTRA02Y |
| TRANTYPE.VSAM.KSDS | KSDS | Type Code (2) | 60 | Transaction Type | CVTRA03Y |
| TRANCATG.VSAM.KSDS | KSDS | Type+Cat (6) | 60 | Transaction Category | CVTRA04Y |
| USRSEC.VSAM.KSDS | KSDS | User ID (8) | 80 | User Security | CSUSR01Y |

---

## Account Entity

**Copybook:** `CVACT01Y.cpy` | **Record Length:** 300 bytes | **VSAM:** ACCTDATA.VSAM.KSDS

| Field Name | PIC Clause | Offset | Length | Data Type | Business Description |
|---|---|---:|---:|---|---|
| ACCT-ID | 9(11) | 0 | 11 | Numeric | Account identifier (primary key) |
| ACCT-ACTIVE-STATUS | X(01) | 11 | 1 | Alpha | Account status (Y=Active, N=Inactive) |
| ACCT-CURR-BAL | S9(10)V99 | 12 | 12 | Signed Decimal | Current account balance |
| ACCT-CREDIT-LIMIT | S9(10)V99 | 24 | 12 | Signed Decimal | Credit limit amount |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | 36 | 12 | Signed Decimal | Cash advance credit limit |
| ACCT-OPEN-DATE | X(10) | 48 | 10 | Date (YYYY-MM-DD) | Account opening date |
| ACCT-EXPIRAION-DATE | X(10) | 58 | 10 | Date (YYYY-MM-DD) | Account expiration date |
| ACCT-REISSUE-DATE | X(10) | 68 | 10 | Date (YYYY-MM-DD) | Card reissue date |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | 78 | 12 | Signed Decimal | Current cycle credit amount |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | 90 | 12 | Signed Decimal | Current cycle debit amount |
| ACCT-ADDR-ZIP | X(10) | 102 | 10 | Alphanumeric | Billing ZIP code |
| ACCT-GROUP-ID | X(10) | 112 | 10 | Alphanumeric | Account group (for disclosure rates) |
| FILLER | X(178) | 122 | 178 | — | Reserved for future use |

**Business Rules:**
- Linked to cards via CARDXREF cross-reference
- Balance updated during transaction posting (CBTRN02C)
- Interest calculated based on ACCT-GROUP-ID → DISCGRP rates (CBACT04C)

---

## Card Entity

**Copybook:** `CVACT02Y.cpy` | **Record Length:** 150 bytes | **VSAM:** CARDDATA.VSAM.KSDS

| Field Name | PIC Clause | Offset | Length | Data Type | Business Description |
|---|---|---:|---:|---|---|
| CARD-NUM | 9(16) | 0 | 16 | Numeric | Card number (primary key) |
| CARD-ACCT-ID | 9(11) | 16 | 11 | Numeric | Associated account ID (FK→Account) |
| CARD-CVV-CD | 9(03) | 27 | 3 | Numeric | Card verification value |
| CARD-EMBOSSED-NAME | X(50) | 30 | 50 | Alpha | Name embossed on card |
| CARD-EXPIRAION-DATE | X(10) | 80 | 10 | Date (YYYY-MM-DD) | Card expiration date |
| CARD-ACTIVE-STATUS | X(01) | 90 | 1 | Alpha | Card status (Y=Active, N=Inactive) |
| FILLER | X(59) | 91 | 59 | — | Reserved |

**Business Rules:**
- One account may have multiple cards
- Card status can be updated via COCRDUPC (Card Update screen)
- Card number is the key for transaction lookups

---

## Card Cross-Reference Entity

**Copybook:** `CVACT03Y.cpy` | **Record Length:** 50 bytes | **VSAM:** CARDXREF.VSAM.KSDS

| Field Name | PIC Clause | Offset | Length | Data Type | Business Description |
|---|---|---:|---:|---|---|
| XREF-CARD-NUM | X(16) | 0 | 16 | Alphanumeric | Card number (primary key) |
| XREF-CUST-NUM | 9(09) | 16 | 9 | Numeric | Customer ID (FK→Customer) |
| XREF-ACCT-ID | 9(11) | 25 | 11 | Numeric | Account ID (FK→Account) |
| FILLER | X(14) | 36 | 14 | — | Reserved |

**Business Rules:**
- Central mapping table: Card → Customer + Account
- Has an alternate index on ACCT-ID (defined in XREFFILE.jcl) for reverse lookups
- Used heavily in interest calculation and statement generation

---

## Customer Entity

**Copybook:** `CVCUS01Y.cpy` | **Record Length:** 500 bytes | **VSAM:** CUSTDATA.VSAM.KSDS

| Field Name | PIC Clause | Offset | Length | Data Type | Business Description |
|---|---|---:|---:|---|---|
| CUST-ID | 9(09) | 0 | 9 | Numeric | Customer identifier (primary key) |
| CUST-FIRST-NAME | X(25) | 9 | 25 | Alpha | First name |
| CUST-MIDDLE-NAME | X(25) | 34 | 25 | Alpha | Middle name |
| CUST-LAST-NAME | X(25) | 59 | 25 | Alpha | Last name |
| CUST-ADDR-LINE-1 | X(50) | 84 | 50 | Alpha | Address line 1 |
| CUST-ADDR-LINE-2 | X(50) | 134 | 50 | Alpha | Address line 2 |
| CUST-ADDR-LINE-3 | X(50) | 184 | 50 | Alpha | Address line 3 |
| CUST-ADDR-STATE-CD | X(02) | 234 | 2 | Alpha | State code |
| CUST-ADDR-COUNTRY-CD | X(03) | 236 | 3 | Alpha | Country code |
| CUST-ADDR-ZIP | X(10) | 239 | 10 | Alphanumeric | ZIP/postal code |
| CUST-PHONE-NUM-1 | X(15) | 249 | 15 | Alphanumeric | Primary phone |
| CUST-PHONE-NUM-2 | X(15) | 264 | 15 | Alphanumeric | Secondary phone |
| CUST-SSN | 9(09) | 279 | 9 | Numeric | Social Security Number (PII) |
| CUST-GOVT-ISSUED-ID | X(20) | 288 | 20 | Alphanumeric | Government-issued ID |
| CUST-DOB-YYYYMMDD | X(10) | 308 | 10 | Date | Date of birth |
| CUST-EFT-ACCOUNT-ID | X(10) | 318 | 10 | Alphanumeric | EFT/bank account for payments |
| CUST-PRI-CARD-HOLDER-IND | X(01) | 328 | 1 | Alpha | Primary cardholder indicator |
| CUST-FICO-CREDIT-SCORE | 9(03) | 329 | 3 | Numeric | FICO credit score |
| FILLER | X(168) | 332 | 168 | — | Reserved |

**Business Rules:**
- Contains PII (SSN, DOB) — **requires special handling during modernization**
- Linked to accounts via cross-reference
- Used in statement generation for mailing address

---

## Transaction Entity

**Copybook:** `CVTRA05Y.cpy` | **Record Length:** 350 bytes | **VSAM:** TRANSACT.VSAM.KSDS

| Field Name | PIC Clause | Offset | Length | Data Type | Business Description |
|---|---|---:|---:|---|---|
| TRAN-ID | X(16) | 0 | 16 | Alphanumeric | Transaction identifier (primary key) |
| TRAN-TYPE-CD | X(02) | 16 | 2 | Alpha | Transaction type code (FK→TRANTYPE) |
| TRAN-CAT-CD | 9(04) | 18 | 4 | Numeric | Transaction category code (FK→TRANCATG) |
| TRAN-SOURCE | X(10) | 22 | 10 | Alphanumeric | Transaction source (POS, ATM, Online) |
| TRAN-DESC | X(100) | 32 | 100 | Alpha | Transaction description |
| TRAN-AMT | S9(09)V99 | 132 | 11 | Signed Decimal | Transaction amount |
| TRAN-MERCHANT-ID | 9(09) | 143 | 9 | Numeric | Merchant identifier |
| TRAN-MERCHANT-NAME | X(50) | 152 | 50 | Alpha | Merchant name |
| TRAN-MERCHANT-CITY | X(50) | 202 | 50 | Alpha | Merchant city |
| TRAN-MERCHANT-ZIP | X(10) | 252 | 10 | Alphanumeric | Merchant ZIP code |
| TRAN-CARD-NUM | X(16) | 262 | 16 | Alphanumeric | Card number used |
| TRAN-ORIG-TS | X(26) | 278 | 26 | Timestamp | Original transaction timestamp |
| TRAN-PROC-TS | X(26) | 304 | 26 | Timestamp | Processing timestamp |
| FILLER | X(20) | 330 | 20 | — | Reserved |

**Business Rules:**
- Central business fact table — highest volume data
- Posted from daily transactions by CBTRN02C
- Amount updates account balance and category balance
- Has alternate index on Card Number for card-based lookups
- Timestamps in ISO 8601 format

---

## Daily Transaction Entity

**Copybook:** `CVTRA06Y.cpy` | **Record Length:** 350 bytes | **File:** DALYTRAN.PS (sequential)

| Field Name | PIC Clause | Offset | Length | Data Type | Business Description |
|---|---|---:|---:|---|---|
| DALYTRAN-ID | X(16) | 0 | 16 | Alphanumeric | Daily transaction ID |
| DALYTRAN-TYPE-CD | X(02) | 16 | 2 | Alpha | Transaction type code |
| DALYTRAN-CAT-CD | 9(04) | 18 | 4 | Numeric | Transaction category code |
| DALYTRAN-SOURCE | X(10) | 22 | 10 | Alphanumeric | Transaction source |
| DALYTRAN-DESC | X(100) | 32 | 100 | Alpha | Transaction description |
| DALYTRAN-AMT | S9(09)V99 | 132 | 11 | Signed Decimal | Transaction amount |
| DALYTRAN-MERCHANT-ID | 9(09) | 143 | 9 | Numeric | Merchant identifier |
| DALYTRAN-MERCHANT-NAME | X(50) | 152 | 50 | Alpha | Merchant name |
| DALYTRAN-MERCHANT-CITY | X(50) | 202 | 50 | Alpha | Merchant city |
| DALYTRAN-MERCHANT-ZIP | X(10) | 252 | 10 | Alphanumeric | Merchant ZIP code |
| DALYTRAN-CARD-NUM | X(16) | 262 | 16 | Alphanumeric | Card number |
| DALYTRAN-ORIG-TS | X(26) | 278 | 26 | Timestamp | Original timestamp |
| DALYTRAN-PROC-TS | X(26) | 304 | 26 | Timestamp | Processing timestamp |
| FILLER | X(20) | 330 | 20 | — | Reserved |

**Business Rules:**
- Input file for batch posting cycle (POSTTRAN job)
- Identical layout to CVTRA05Y — staging area before posting to master
- Rejected records written to DALYREJS GDG

---

## Transaction Category Balance Entity

**Copybook:** `CVTRA01Y.cpy` | **Record Length:** 50 bytes | **VSAM:** TCATBALF.VSAM.KSDS

| Field Name | PIC Clause | Offset | Length | Data Type | Business Description |
|---|---|---:|---:|---|---|
| TRANCAT-ACCT-ID | 9(11) | 0 | 11 | Numeric | Account ID (part of composite key) |
| TRANCAT-TYPE-CD | X(02) | 11 | 2 | Alpha | Transaction type (part of key) |
| TRANCAT-CD | 9(04) | 13 | 4 | Numeric | Transaction category (part of key) |
| TRAN-CAT-BAL | S9(09)V99 | 17 | 11 | Signed Decimal | Running balance for this category |
| FILLER | X(22) | 28 | 22 | — | Reserved |

**Business Rules:**
- Tracks balance by account + transaction type + category
- Updated during transaction posting
- Used for interest calculation (different rates per category)

---

## Disclosure Group Entity

**Copybook:** `CVTRA02Y.cpy` | **Record Length:** 50 bytes | **VSAM:** DISCGRP.VSAM.KSDS

| Field Name | PIC Clause | Offset | Length | Data Type | Business Description |
|---|---|---:|---:|---|---|
| DIS-ACCT-GROUP-ID | X(10) | 0 | 10 | Alphanumeric | Account group ID (part of key) |
| DIS-TRAN-TYPE-CD | X(02) | 10 | 2 | Alpha | Transaction type (part of key) |
| DIS-TRAN-CAT-CD | 9(04) | 12 | 4 | Numeric | Transaction category (part of key) |
| DIS-INT-RATE | S9(04)V99 | 16 | 6 | Signed Decimal | Interest rate (APR %) |
| FILLER | X(28) | 22 | 28 | — | Reserved |

**Business Rules:**
- Defines interest rates per account group + transaction type + category
- Account's ACCT-GROUP-ID links to DIS-ACCT-GROUP-ID
- Used by CBACT04C (interest calculation batch)

---

## Transaction Type Entity

**Copybook:** `CVTRA03Y.cpy` | **Record Length:** 60 bytes | **VSAM:** TRANTYPE.VSAM.KSDS

| Field Name | PIC Clause | Offset | Length | Data Type | Business Description |
|---|---|---:|---:|---|---|
| TRAN-TYPE | X(02) | 0 | 2 | Alpha | Transaction type code (primary key) |
| TRAN-TYPE-DESC | X(50) | 2 | 50 | Alpha | Type description (e.g., "Purchase", "Cash Advance") |
| FILLER | X(08) | 52 | 8 | — | Reserved |

**Business Rules:**
- Reference/lookup table for transaction classification
- Optionally managed via DB2 in the Transaction Type DB2 module

---

## Transaction Category Type Entity

**Copybook:** `CVTRA04Y.cpy` | **Record Length:** 60 bytes | **VSAM:** TRANCATG.VSAM.KSDS

| Field Name | PIC Clause | Offset | Length | Data Type | Business Description |
|---|---|---:|---:|---|---|
| TRAN-TYPE-CD | X(02) | 0 | 2 | Alpha | Transaction type (part of composite key) |
| TRAN-CAT-CD | 9(04) | 2 | 4 | Numeric | Category code (part of composite key) |
| TRAN-CAT-TYPE-DESC | X(50) | 6 | 50 | Alpha | Category description |
| FILLER | X(04) | 56 | 4 | — | Reserved |

**Business Rules:**
- Sub-classification within transaction types
- Links to category balance and disclosure group records

---

## User Security Entity

**Copybook:** `CSUSR01Y.cpy` | **Record Length:** 80 bytes | **VSAM:** USRSEC.VSAM.KSDS

| Field Name | PIC Clause | Offset | Length | Data Type | Business Description |
|---|---|---:|---:|---|---|
| SEC-USR-ID | X(08) | 0 | 8 | Alphanumeric | User ID (primary key) |
| SEC-USR-FNAME | X(20) | 8 | 20 | Alpha | User first name |
| SEC-USR-LNAME | X(20) | 28 | 20 | Alpha | User last name |
| SEC-USR-PWD | X(08) | 48 | 8 | Alphanumeric | User password (**plain text — security risk**) |
| SEC-USR-TYPE | X(01) | 56 | 1 | Alpha | User type (A=Admin, U=Regular) |
| SEC-USR-FILLER | X(23) | 57 | 23 | — | Reserved |

**Business Rules:**
- Passwords stored in plain text — **must be hashed in modernized system**
- Admin users access user management and admin menu
- Regular users access card/account/transaction functions
- Default accounts: ADMIN001/PASSWORD (Admin), USER0001/PASSWORD (Regular)

---

## Communication Area

**Copybook:** `COCOM01Y.cpy` | **Purpose:** CICS inter-program communication (COMMAREA)

| Field Name | PIC Clause | Length | Business Description |
|---|---|---:|---|
| CDEMO-FROM-TRANID | X(04) | 4 | Source CICS transaction ID |
| CDEMO-FROM-PROGRAM | X(08) | 8 | Source program name |
| CDEMO-TO-TRANID | X(04) | 4 | Target CICS transaction ID |
| CDEMO-TO-PROGRAM | X(08) | 8 | Target program name |
| CDEMO-USER-ID | X(08) | 8 | Logged-in user ID |
| CDEMO-USER-TYPE | X(01) | 1 | User type (A=Admin, U=User) |
| CDEMO-PGM-CONTEXT | 9(01) | 1 | Program context (0=Enter, 1=Re-enter) |
| CDEMO-CUST-ID | 9(09) | 9 | Selected customer ID |
| CDEMO-CUST-FNAME | X(25) | 25 | Customer first name |
| CDEMO-CUST-MNAME | X(25) | 25 | Customer middle name |
| CDEMO-CUST-LNAME | X(25) | 25 | Customer last name |
| CDEMO-ACCT-ID | 9(11) | 11 | Selected account ID |
| CDEMO-ACCT-STATUS | X(01) | 1 | Account status |
| CDEMO-CARD-NUM | 9(16) | 16 | Selected card number |
| CDEMO-LAST-MAP | X(07) | 7 | Last displayed BMS map |
| CDEMO-LAST-MAPSET | X(07) | 7 | Last displayed BMS mapset |

**Notes:**
- Passed between all online CICS programs via XCTL COMMAREA
- Carries user session context and selected entity IDs
- Maps to Java session/request scope in modernized system

---

## Card Detail Record

**Copybook:** `CVCRD01Y.cpy` | **Purpose:** Extended card record used in online programs

This copybook provides an expanded view of card information used by the Account Update (COACTUPC), Account View (COACTVWC), Card List (COCRDLIC), Card Select (COCRDSLC), and Card Update (COCRDUPC) programs. It contains additional display fields beyond the base CVACT02Y layout.

---

## Statement Transaction Layout

**Copybook:** `COSTM01.CPY` | **Record Length:** 350 bytes | **Purpose:** Re-keyed transaction for statements

| Field Name | PIC Clause | Length | Business Description |
|---|---|---:|---|
| TRNX-CARD-NUM | X(16) | 16 | Card number (part of composite key) |
| TRNX-ID | X(16) | 16 | Transaction ID (part of composite key) |
| TRNX-TYPE-CD | X(02) | 2 | Transaction type |
| TRNX-CAT-CD | 9(04) | 4 | Transaction category |
| TRNX-SOURCE | X(10) | 10 | Transaction source |
| TRNX-DESC | X(100) | 100 | Transaction description |
| TRNX-AMT | S9(09)V99 | 11 | Transaction amount |
| TRNX-MERCHANT-ID | 9(09) | 9 | Merchant ID |
| TRNX-MERCHANT-NAME | X(50) | 50 | Merchant name |
| TRNX-MERCHANT-CITY | X(50) | 50 | Merchant city |
| TRNX-MERCHANT-ZIP | X(10) | 10 | Merchant ZIP |
| TRNX-ORIG-TS | X(26) | 26 | Original timestamp |
| TRNX-PROC-TS | X(26) | 26 | Processing timestamp |
| FILLER | X(20) | 20 | Reserved |

**Notes:**
- Same data as CVTRA05Y but re-keyed with Card Number as primary sort
- Created by CREASTMT.JCL SORT step for card-based statement grouping

---

## Export Record Layout

**Copybook:** `CVEXPORT.cpy` | **Purpose:** Combined export format for data migration

Contains sub-layouts for all major entities (Account, Customer, Card, Cross-Reference, Transaction) prefixed with `EXP-` for export/import operations via CBEXPORT/CBIMPORT programs.

---

## Transaction Report Layout

**Copybook:** `CVTRA07Y.cpy` | **Purpose:** Report formatting structures

| Structure | Description |
|---|---|
| REPORT-NAME-HEADER | Report title, date range headers |
| TRANSACTION-DETAIL-REPORT | Detail line: Trans ID, Account, Type, Category, Source, Amount |
| TRANSACTION-HEADER-1/2 | Column headers and separator lines |
| REPORT-PAGE-TOTALS | Page-level amount total |
| REPORT-ACCOUNT-TOTALS | Account-level amount total |
| REPORT-GRAND-TOTALS | Grand total across all accounts |

---

## Date Conversion Record

**Copybook:** `CODATECN.cpy` | **Purpose:** Date format conversion utility

| Structure | Description |
|---|---|
| CODATECN-IN-REC | Input: date type flag + raw date string |
| CODATECN-OUT-REC | Output: formatted date in requested format |
| CODATECN-ERROR-MSG | Error message (38 chars) |

**Supported Formats:** YYYYMMDD ↔ YYYY-MM-DD

---

## Lookup Code Table

**Copybook:** `CSLKPCDY.cpy` | **Size:** ~51KB | **Purpose:** Embedded reference data

This is the largest copybook, containing hard-coded lookup values for:
- Country codes and names
- State/province codes
- Other reference data used by the Account Update screen (COACTUPC)

**Modernization Note:** Should be migrated to a database reference table rather than embedded in code.

---

## Entity Relationship Summary

```
                    ┌──────────────┐
                    │   Customer   │
                    │  (CVCUS01Y)  │
                    │  PK: CUST-ID │
                    └──────┬───────┘
                           │ 1:N
                           ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│    Account   │◄───│  Cross-Ref   │───►│     Card     │
│  (CVACT01Y)  │    │  (CVACT03Y)  │    │  (CVACT02Y)  │
│ PK: ACCT-ID  │    │ PK: CARD-NUM │    │ PK: CARD-NUM │
└──────┬───────┘    └──────────────┘    └──────┬───────┘
       │                                       │
       │ 1:N                                   │ 1:N
       ▼                                       ▼
┌──────────────┐                        ┌──────────────┐
│  Cat Balance │                        │ Transaction  │
│  (CVTRA01Y)  │                        │  (CVTRA05Y)  │
│ PK: ACCT+    │                        │ PK: TRAN-ID  │
│   TYPE+CAT   │                        └──────┬───────┘
└──────────────┘                               │
                                               │ N:1
       ┌──────────────┐    ┌──────────────┐    ▼
       │  Disc Group  │    │  Tran Type   │◄──────
       │  (CVTRA02Y)  │    │  (CVTRA03Y)  │
       │ PK: GRP+     │    │ PK: TYPE-CD  │
       │  TYPE+CAT    │    └──────┬───────┘
       └──────────────┘           │ 1:N
                                  ▼
                           ┌──────────────┐
                           │  Tran Cat    │
                           │  (CVTRA04Y)  │
                           │ PK: TYPE+CAT │
                           └──────────────┘

┌──────────────┐
│ User Security│   (Standalone — manages application access)
│  (CSUSR01Y)  │
│ PK: USR-ID   │
└──────────────┘
```

### Key Relationships

| Parent Entity | Child Entity | Cardinality | Join Key |
|---|---|---|---|
| Customer | Cross-Reference | 1:N | CUST-ID = XREF-CUST-NUM |
| Account | Cross-Reference | 1:N | ACCT-ID = XREF-ACCT-ID |
| Card | Cross-Reference | 1:1 | CARD-NUM = XREF-CARD-NUM |
| Card | Transaction | 1:N | CARD-NUM = TRAN-CARD-NUM |
| Account | Category Balance | 1:N | ACCT-ID = TRANCAT-ACCT-ID |
| Transaction Type | Transaction | 1:N | TRAN-TYPE = TRAN-TYPE-CD |
| Transaction Type | Category Type | 1:N | TRAN-TYPE = TRAN-TYPE-CD |
| Account Group | Disclosure Group | 1:N | ACCT-GROUP-ID = DIS-ACCT-GROUP-ID |

### PII Fields Requiring Special Handling

| Entity | Field | Type | Sensitivity |
|---|---|---|---|
| Customer | CUST-SSN | SSN | **High** — encrypt at rest, mask in UI |
| Customer | CUST-DOB-YYYYMMDD | DOB | **Medium** — restrict access |
| Customer | CUST-PHONE-NUM-1/2 | Phone | **Medium** — mask in logs |
| User Security | SEC-USR-PWD | Password | **Critical** — must hash (bcrypt/Argon2) |

### Modernization Mapping (VSAM → Relational)

| VSAM File | Suggested Table Name | Primary Key | Notes |
|---|---|---|---|
| ACCTDATA | `accounts` | `account_id` | Add FK to `customers` |
| CARDDATA | `cards` | `card_number` | Add FK to `accounts` |
| CARDXREF | *(Eliminate)* | — | Replace with direct FKs on `cards` |
| CUSTDATA | `customers` | `customer_id` | Encrypt SSN column |
| TRANSACT | `transactions` | `transaction_id` | Partition by date |
| TCATBALF | `category_balances` | `(account_id, type_cd, cat_cd)` | Consider materialized view |
| DISCGRP | `disclosure_groups` | `(group_id, type_cd, cat_cd)` | Reference/config table |
| TRANTYPE | `transaction_types` | `type_code` | Reference/config table |
| TRANCATG | `transaction_categories` | `(type_cd, cat_cd)` | Reference/config table |
| USRSEC | `users` | `user_id` | Hash passwords, add roles table |
