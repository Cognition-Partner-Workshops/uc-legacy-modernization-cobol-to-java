# CardDemo Data Dictionary

> **Source:** Copybooks in `app/cpy/` and `app/cpy-bms/`
> **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Format:** Business-friendly field descriptions extracted from COBOL PIC clauses

---

## Table of Contents

1. [Entity Relationship Overview](#entity-relationship-overview)
2. [Account Entity](#1-account-entity)
3. [Card Entity](#2-card-entity)
4. [Card–Account Cross-Reference](#3-cardaccount-cross-reference)
5. [Customer Entity](#4-customer-entity)
6. [Transaction Entity](#5-transaction-entity)
7. [Daily Transaction Entity](#6-daily-transaction-entity)
8. [Transaction Category Balance](#7-transaction-category-balance)
9. [Disclosure Group](#8-disclosure-group)
10. [Transaction Type](#9-transaction-type)
11. [Transaction Category](#10-transaction-category)
12. [User Security Entity](#11-user-security-entity)
13. [Communication Area (COMMAREA)](#12-communication-area-commarea)
14. [Date Conversion Record](#13-date-conversion-record)
15. [Statement Transaction Record](#14-statement-transaction-record)
16. [Customer Record (Statement)](#15-customer-record-statement)
17. [Export Record](#16-export-record)
18. [Report Structures](#17-report-structures)
19. [Menu Definitions](#18-menu-definitions)
20. [PIC Clause Reference](#pic-clause-reference)

---

## Entity Relationship Overview

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────┐
│   CUSTOMER   │ 1───* │     ACCOUNT      │ 1───* │     CARD     │
│  CVCUS01Y    │       │    CVACT01Y      │       │   CVACT02Y   │
│  (500 bytes) │       │   (300 bytes)    │       │  (150 bytes) │
└──────────────┘       └────────┬─────────┘       └──────┬───────┘
                                │                        │
                                │    ┌───────────────┐   │
                                └────┤  XREF (link)  ├───┘
                                     │   CVACT03Y    │
                                     │  (50 bytes)   │
                                     └───────────────┘
                                │
                   ┌────────────┴────────────┐
                   │                         │
          ┌────────┴────────┐    ┌───────────┴──────────┐
          │   TRANSACTION   │    │   DAILY TRANSACTION  │
          │    CVTRA05Y     │    │      CVTRA06Y        │
          │   (350 bytes)   │    │     (350 bytes)      │
          └────────┬────────┘    └──────────────────────┘
                   │
     ┌─────────────┼─────────────┐
     │             │             │
┌────┴─────┐ ┌────┴──────┐ ┌────┴──────────┐
│TRAN TYPE │ │ TRAN CAT  │ │ CAT BALANCE   │
│ CVTRA03Y │ │ CVTRA04Y  │ │   CVTRA01Y    │
│(60 bytes)│ │ (60 bytes)│ │  (50 bytes)   │
└──────────┘ └───────────┘ └───────────────┘

┌──────────────────┐     ┌──────────────────┐
│  USER SECURITY   │     │ DISCLOSURE GROUP │
│    CSUSR01Y      │     │    CVTRA02Y      │
│   (80 bytes)     │     │   (50 bytes)     │
└──────────────────┘     └──────────────────┘
```

---

## 1. Account Entity

**Copybook:** `CVACT01Y.cpy` | **Record Length:** 300 bytes | **VSAM File:** `ACCTDATA.VSAM.KSDS`

| Field Name | COBOL PIC | Data Type | Length | Business Description |
|------------|-----------|-----------|--------|---------------------|
| `ACCT-ID` | `9(11)` | Numeric | 11 | **Primary Key.** Unique account identifier |
| `ACCT-ACTIVE-STATUS` | `X(01)` | Alpha | 1 | Account status: `Y` = Active, `N` = Inactive |
| `ACCT-CURR-BAL` | `S9(10)V99` | Signed Decimal | 12.2 | Current account balance (cents precision) |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | Signed Decimal | 12.2 | Maximum credit limit |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | Signed Decimal | 12.2 | Cash advance credit limit |
| `ACCT-OPEN-DATE` | `X(10)` | Date String | 10 | Account opening date (YYYY-MM-DD) |
| `ACCT-EXPIRAION-DATE` | `X(10)` | Date String | 10 | Account expiration date |
| `ACCT-REISSUE-DATE` | `X(10)` | Date String | 10 | Date of last card reissue |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | Signed Decimal | 12.2 | Credits in current billing cycle |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | Signed Decimal | 12.2 | Debits in current billing cycle |
| `ACCT-ADDR-ZIP` | `X(10)` | Alpha | 10 | Account holder ZIP/postal code |
| `ACCT-GROUP-ID` | `X(10)` | Alpha | 10 | Account group (for disclosure/interest rates) |
| `FILLER` | `X(178)` | — | 178 | Reserved space |

**Key Business Rules:**
- Balance must not exceed `ACCT-CREDIT-LIMIT`
- Cash transactions limited to `ACCT-CASH-CREDIT-LIMIT`
- `ACCT-GROUP-ID` links to Disclosure Group for interest rate lookup

---

## 2. Card Entity

**Copybook:** `CVACT02Y.cpy` | **Record Length:** 150 bytes | **VSAM File:** `CARDDATA.VSAM.KSDS`

| Field Name | COBOL PIC | Data Type | Length | Business Description |
|------------|-----------|-----------|--------|---------------------|
| `CARD-NUM` | `X(16)` | Alpha | 16 | **Primary Key.** 16-digit credit card number |
| `CARD-ACCT-ID` | `9(11)` | Numeric | 11 | **Foreign Key** → Account.ACCT-ID |
| `CARD-CVV-CD` | `9(03)` | Numeric | 3 | Card Verification Value (CVV) |
| `CARD-EMBOSSED-NAME` | `X(50)` | Alpha | 50 | Cardholder name as embossed on card |
| `CARD-EXPIRAION-DATE` | `X(10)` | Date String | 10 | Card expiration date |
| `CARD-ACTIVE-STATUS` | `X(01)` | Alpha | 1 | Card status: `Y` = Active, `N` = Inactive |
| `FILLER` | `X(59)` | — | 59 | Reserved space |

**Key Business Rules:**
- Multiple cards can be linked to one account
- Card status is independent of account status

---

## 3. Card–Account Cross-Reference

**Copybook:** `CVACT03Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `CARDXREF.VSAM.KSDS`

| Field Name | COBOL PIC | Data Type | Length | Business Description |
|------------|-----------|-----------|--------|---------------------|
| `XREF-CARD-NUM` | `X(16)` | Alpha | 16 | **Primary Key.** Card number |
| `XREF-ACCT-ID` | `9(11)` | Numeric | 11 | **Foreign Key** → Account.ACCT-ID |
| `XREF-CUST-ID` | `9(09)` | Numeric | 9 | **Foreign Key** → Customer.CUST-ID |
| `FILLER` | `X(14)` | — | 14 | Reserved space |

**Purpose:** Provides fast lookup from card number → account + customer. Used by transaction posting to resolve card → account ownership.

---

## 4. Customer Entity

**Copybook:** `CVCUS01Y.cpy` | **Record Length:** 500 bytes | **VSAM File:** `CUSTDATA.VSAM.KSDS`

| Field Name | COBOL PIC | Data Type | Length | Business Description |
|------------|-----------|-----------|--------|---------------------|
| `CUST-ID` | `9(09)` | Numeric | 9 | **Primary Key.** Unique customer identifier |
| `CUST-FIRST-NAME` | `X(25)` | Alpha | 25 | Customer first name |
| `CUST-MIDDLE-NAME` | `X(25)` | Alpha | 25 | Customer middle name |
| `CUST-LAST-NAME` | `X(25)` | Alpha | 25 | Customer last name |
| `CUST-ADDR-LINE-1` | `X(50)` | Alpha | 50 | Street address line 1 |
| `CUST-ADDR-LINE-2` | `X(50)` | Alpha | 50 | Street address line 2 |
| `CUST-ADDR-LINE-3` | `X(50)` | Alpha | 50 | Street address line 3 |
| `CUST-ADDR-STATE-CD` | `X(02)` | Alpha | 2 | State/province code |
| `CUST-ADDR-COUNTRY-CD` | `X(03)` | Alpha | 3 | Country code |
| `CUST-ADDR-ZIP` | `X(10)` | Alpha | 10 | ZIP/postal code |
| `CUST-PHONE-NUM-1` | `X(15)` | Alpha | 15 | Primary phone number |
| `CUST-PHONE-NUM-2` | `X(15)` | Alpha | 15 | Secondary phone number |
| `CUST-SSN` | `9(09)` | Numeric | 9 | Social Security Number (**PII — sensitive**) |
| `CUST-GOVT-ISSUED-ID` | `X(20)` | Alpha | 20 | Government-issued ID number (**PII**) |
| `CUST-DOB-YYYYMMDD` | `X(10)` | Date String | 10 | Date of birth (**PII**) |
| `CUST-EFT-ACCOUNT-ID` | `X(10)` | Alpha | 10 | Electronic funds transfer account |
| `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | Alpha | 1 | Primary cardholder indicator (`Y`/`N`) |
| `CUST-FICO-CREDIT-SCORE` | `9(03)` | Numeric | 3 | FICO credit score (300–850) |
| `FILLER` | `X(168)` | — | 168 | Reserved space |

**PII Warning:** Contains SSN, DOB, and government ID. Requires encryption/masking in modernized system.

---

## 5. Transaction Entity

**Copybook:** `CVTRA05Y.cpy` | **Record Length:** 350 bytes | **VSAM File:** `TRANSACT.VSAM.KSDS`

| Field Name | COBOL PIC | Data Type | Length | Business Description |
|------------|-----------|-----------|--------|---------------------|
| `TRAN-ID` | `X(16)` | Alpha | 16 | **Primary Key.** Unique transaction identifier |
| `TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | **FK** → Transaction Type code |
| `TRAN-CAT-CD` | `9(04)` | Numeric | 4 | **FK** → Transaction Category code |
| `TRAN-SOURCE` | `X(10)` | Alpha | 10 | Transaction source (POS, ATM, Online, etc.) |
| `TRAN-DESC` | `X(100)` | Alpha | 100 | Transaction description / memo |
| `TRAN-AMT` | `S9(09)V99` | Signed Decimal | 11.2 | Transaction amount (negative = credit) |
| `TRAN-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant identifier |
| `TRAN-MERCHANT-NAME` | `X(50)` | Alpha | 50 | Merchant business name |
| `TRAN-MERCHANT-CITY` | `X(50)` | Alpha | 50 | Merchant city |
| `TRAN-MERCHANT-ZIP` | `X(10)` | Alpha | 10 | Merchant ZIP code |
| `TRAN-CARD-NUM` | `X(16)` | Alpha | 16 | **FK** → Card number used |
| `TRAN-ORIG-TS` | `X(26)` | Timestamp | 26 | Original transaction timestamp |
| `TRAN-PROC-TS` | `X(26)` | Timestamp | 26 | Processing timestamp |
| `FILLER` | `X(20)` | — | 20 | Reserved space |

**Key Business Rules:**
- Card number links to cross-reference for account resolution
- Signed amount: positive = debit (purchase), negative = credit (refund)
- Timestamps in ISO-like format for audit trail

---

## 6. Daily Transaction Entity

**Copybook:** `CVTRA06Y.cpy` | **Record Length:** 350 bytes | **VSAM File:** `DALYTRAN.VSAM.KSDS`

| Field Name | COBOL PIC | Data Type | Length | Business Description |
|------------|-----------|-----------|--------|---------------------|
| `DALYTRAN-ID` | `X(16)` | Alpha | 16 | **Primary Key.** Daily transaction ID |
| `DALYTRAN-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type code |
| `DALYTRAN-CAT-CD` | `9(04)` | Numeric | 4 | Transaction category code |
| `DALYTRAN-SOURCE` | `X(10)` | Alpha | 10 | Source channel |
| `DALYTRAN-DESC` | `X(100)` | Alpha | 100 | Transaction description |
| `DALYTRAN-AMT` | `S9(09)V99` | Signed Decimal | 11.2 | Transaction amount |
| `DALYTRAN-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant ID |
| `DALYTRAN-MERCHANT-NAME` | `X(50)` | Alpha | 50 | Merchant name |
| `DALYTRAN-MERCHANT-CITY` | `X(50)` | Alpha | 50 | Merchant city |
| `DALYTRAN-MERCHANT-ZIP` | `X(10)` | Alpha | 10 | Merchant ZIP |
| `DALYTRAN-CARD-NUM` | `X(16)` | Alpha | 16 | Card number |
| `DALYTRAN-ORIG-TS` | `X(26)` | Timestamp | 26 | Original timestamp |
| `DALYTRAN-PROC-TS` | `X(26)` | Timestamp | 26 | Processing timestamp |
| `FILLER` | `X(20)` | — | 20 | Reserved space |

**Purpose:** Staging area for incoming daily transactions. Batch job `CBTRN02C` (POSTTRAN) validates and posts these to the Transaction master file, rejecting invalid records to `DALYREJS`.

---

## 7. Transaction Category Balance

**Copybook:** `CVTRA01Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `TCATBALF.VSAM.KSDS`

| Field Name | COBOL PIC | Data Type | Length | Business Description |
|------------|-----------|-----------|--------|---------------------|
| `TRANCAT-ACCT-ID` | `9(11)` | Numeric | 11 | **Composite Key** — Account ID |
| `TRANCAT-TYPE-CD` | `X(02)` | Alpha | 2 | **Composite Key** — Transaction type |
| `TRANCAT-CD` | `9(04)` | Numeric | 4 | **Composite Key** — Category code |
| `TRAN-CAT-BAL` | `S9(09)V99` | Signed Decimal | 11.2 | Running balance for this category |
| `FILLER` | `X(22)` | — | 22 | Reserved space |

**Purpose:** Tracks running balances per account per transaction category. Used by interest calculation (`CBACT04C`) and reporting.

---

## 8. Disclosure Group

**Copybook:** `CVTRA02Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `DISCGRP.VSAM.KSDS`

| Field Name | COBOL PIC | Data Type | Length | Business Description |
|------------|-----------|-----------|--------|---------------------|
| `DIS-ACCT-GROUP-ID` | `X(10)` | Alpha | 10 | **Composite Key** — Account group |
| `DIS-TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | **Composite Key** — Transaction type |
| `DIS-TRAN-CAT-CD` | `9(04)` | Numeric | 4 | **Composite Key** — Category code |
| `DIS-INT-RATE` | `S9(04)V99` | Signed Decimal | 6.2 | Interest rate (annual %) for this group/type |
| `FILLER` | `X(28)` | — | 28 | Reserved space |

**Purpose:** Defines interest rates per account group and transaction type. Linked from Account via `ACCT-GROUP-ID`. Core to interest calculation logic.

---

## 9. Transaction Type

**Copybook:** `CVTRA03Y.cpy` | **Record Length:** 60 bytes | **VSAM File:** `TRANTYPE.VSAM.KSDS`

| Field Name | COBOL PIC | Data Type | Length | Business Description |
|------------|-----------|-----------|--------|---------------------|
| `TRAN-TYPE` | `X(02)` | Alpha | 2 | **Primary Key.** Transaction type code (e.g., `01` = Purchase) |
| `TRAN-TYPE-DESC` | `X(50)` | Alpha | 50 | Human-readable description |
| `FILLER` | `X(08)` | — | 8 | Reserved space |

**Purpose:** Reference/lookup table for transaction types. Managed via Admin menu (DB2 module) or VSAM load.

---

## 10. Transaction Category

**Copybook:** `CVTRA04Y.cpy` | **Record Length:** 60 bytes | **VSAM File:** `TRANCATG.VSAM.KSDS`

| Field Name | COBOL PIC | Data Type | Length | Business Description |
|------------|-----------|-----------|--------|---------------------|
| `TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | **Composite Key** — Parent transaction type |
| `TRAN-CAT-CD` | `9(04)` | Numeric | 4 | **Composite Key** — Category code |
| `TRAN-CAT-TYPE-DESC` | `X(50)` | Alpha | 50 | Category description |
| `FILLER` | `X(04)` | — | 4 | Reserved space |

**Purpose:** Sub-classification of transactions. A type can have many categories (e.g., Purchase → Retail, Purchase → Online).

---

## 11. User Security Entity

**Copybook:** `CSUSR01Y.cpy` | **Record Length:** ~80 bytes | **VSAM File:** `USRSEC.VSAM.KSDS`

| Field Name | COBOL PIC | Data Type | Length | Business Description |
|------------|-----------|-----------|--------|---------------------|
| `SEC-USR-ID` | `X(08)` | Alpha | 8 | **Primary Key.** User login ID |
| `SEC-USR-FNAME` | `X(20)` | Alpha | 20 | User first name |
| `SEC-USR-LNAME` | `X(20)` | Alpha | 20 | User last name |
| `SEC-USR-PWD` | `X(08)` | Alpha | 8 | Password (**plaintext — security risk**) |
| `SEC-USR-TYPE` | `X(01)` | Alpha | 1 | User type: `A` = Admin, `U` = Regular |
| `SEC-USR-FILLER` | `X(23)` | — | 23 | Reserved space |

**Security Notes:**
- Passwords stored in plaintext — must be hashed in modernized system
- Admin users (`A`) can access User CRUD and Admin menu
- Regular users (`U`) limited to main menu functions

---

## 12. Communication Area (COMMAREA)

**Copybook:** `COCOM01Y.cpy` | **Purpose:** Passed between CICS programs via XCTL/RETURN

| Field Name | COBOL PIC | Data Type | Length | Business Description |
|------------|-----------|-----------|--------|---------------------|
| `CDEMO-FROM-TRANID` | `X(04)` | Alpha | 4 | Source CICS transaction ID |
| `CDEMO-FROM-PROGRAM` | `X(08)` | Alpha | 8 | Source program name |
| `CDEMO-TO-TRANID` | `X(04)` | Alpha | 4 | Target CICS transaction ID |
| `CDEMO-TO-PROGRAM` | `X(08)` | Alpha | 8 | Target program name |
| `CDEMO-USER-ID` | `X(08)` | Alpha | 8 | Current logged-in user ID |
| `CDEMO-USER-TYPE` | `X(01)` | Alpha | 1 | User type (`A`/`U`) — 88-levels: `CDEMO-USRTYP-ADMIN`, `CDEMO-USRTYP-USER` |
| `CDEMO-PGM-CONTEXT` | `9(01)` | Numeric | 1 | 0 = First entry, 1 = Re-entry |
| `CDEMO-CUST-ID` | `9(09)` | Numeric | 9 | Current customer ID in context |
| `CDEMO-CUST-FNAME` | `X(25)` | Alpha | 25 | Customer first name (display) |
| `CDEMO-CUST-MNAME` | `X(25)` | Alpha | 25 | Customer middle name |
| `CDEMO-CUST-LNAME` | `X(25)` | Alpha | 25 | Customer last name |
| `CDEMO-ACCT-ID` | `9(11)` | Numeric | 11 | Current account ID in context |
| `CDEMO-ACCT-STATUS` | `X(01)` | Alpha | 1 | Account status |
| `CDEMO-CARD-NUM` | `9(16)` | Numeric | 16 | Current card number in context |
| `CDEMO-LAST-MAP` | `X(7)` | Alpha | 7 | Last BMS map displayed |
| `CDEMO-LAST-MAPSET` | `X(7)` | Alpha | 7 | Last BMS mapset used |

**Purpose:** Session state container — equivalent to an HTTP session in web applications. Every CICS program reads and writes this area.

---

## 13. Date Conversion Record

**Copybook:** `CODATECN.cpy` | **Purpose:** Input/output for date format conversion

| Field Name | COBOL PIC | Data Type | Length | Business Description |
|------------|-----------|-----------|--------|---------------------|
| `CODATECN-TYPE` | `X` | Alpha | 1 | Input format: `1` = YYYYMMDD, `2` = YYYY-MM-DD |
| `CODATECN-INP-DATE` | `X(20)` | Alpha | 20 | Input date string |
| `CODATECN-OUTTYPE` | `X` | Alpha | 1 | Output format: `1` = YYYY-MM-DD, `2` = YYYYMMDD |
| `CODATECN-0UT-DATE` | `X(20)` | Alpha | 20 | Output date string |
| `CODATECN-ERROR-MSG` | `X(38)` | Alpha | 38 | Error message if conversion fails |

---

## 14. Statement Transaction Record

**Copybook:** `COSTM01.CPY` | **Purpose:** Altered transaction layout keyed by card+transaction for statement generation

| Field Name | COBOL PIC | Data Type | Length | Business Description |
|------------|-----------|-----------|--------|---------------------|
| `TRNX-CARD-NUM` | `X(16)` | Alpha | 16 | **Composite Key** — Card number |
| `TRNX-ID` | `X(16)` | Alpha | 16 | **Composite Key** — Transaction ID |
| `TRNX-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type |
| `TRNX-CAT-CD` | `9(04)` | Numeric | 4 | Transaction category |
| `TRNX-SOURCE` | `X(10)` | Alpha | 10 | Source channel |
| `TRNX-DESC` | `X(100)` | Alpha | 100 | Description |
| `TRNX-AMT` | `S9(09)V99` | Signed Decimal | 11.2 | Amount |
| `TRNX-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant ID |
| `TRNX-MERCHANT-NAME` | `X(50)` | Alpha | 50 | Merchant name |
| `TRNX-MERCHANT-CITY` | `X(50)` | Alpha | 50 | Merchant city |
| `TRNX-MERCHANT-ZIP` | `X(10)` | Alpha | 10 | Merchant ZIP |
| `TRNX-ORIG-TS` | `X(26)` | Timestamp | 26 | Original timestamp |
| `TRNX-PROC-TS` | `X(26)` | Timestamp | 26 | Processing timestamp |

**Note:** Re-keyed by `CARD-NUM + TRAN-ID` (vs. `TRAN-ID` only in CVTRA05Y) to enable per-card statement generation via sorted sequential access.

---

## 15. Customer Record (Statement)

**Copybook:** `CUSTREC.cpy` | **Purpose:** Simplified customer record for statement generation

Contains customer name, address, and account linkage fields used by `CBSTM03A` to print statement headers with customer mailing information.

---

## 16. Export Record

**Copybook:** `CVEXPORT.cpy` | **Record Length:** 500+ bytes | **Purpose:** Flat-file export layout

Contains a union of all VSAM entity fields in a single sequential record for data export (`CBEXPORT`) and import (`CBIMPORT`). Includes:

| Embedded Entity | Key Fields |
|----------------|------------|
| Customer data | `EXP-CUST-ID`, `EXP-CUST-FIRST-NAME`, `EXP-CUST-LAST-NAME`, etc. |
| Account data | `EXP-ACCT-ID`, `EXP-ACCT-ACTIVE-STATUS`, `EXP-ACCT-CURR-BAL`, etc. |
| Card data | `EXP-CARD-NUM`, `EXP-CARD-ACCT-ID`, `EXP-CARD-CVV-CD`, etc. |

---

## 17. Report Structures

**Copybook:** `CVTRA07Y.cpy` | **Purpose:** Print layout for daily transaction report

| Structure | Fields | Description |
|-----------|--------|-------------|
| `REPORT-NAME-HEADER` | `REPT-SHORT-NAME`, `REPT-LONG-NAME`, `REPT-START-DATE`, `REPT-END-DATE` | Report title and date range |
| `TRANSACTION-DETAIL-REPORT` | `TRAN-REPORT-TRANS-ID`, `TRAN-REPORT-ACCOUNT-ID`, `TRAN-REPORT-TYPE-CD`, `TRAN-REPORT-AMT` | One line per transaction |
| `TRANSACTION-HEADER-1/2` | Column headers | Report column headers and separator |
| `REPORT-PAGE-TOTALS` | `REPT-PAGE-TOTAL` | Page subtotal (edited: `+ZZZ,ZZZ,ZZZ.ZZ`) |
| `REPORT-ACCOUNT-TOTALS` | `REPT-ACCOUNT-TOTAL` | Account subtotal |
| `REPORT-GRAND-TOTALS` | `REPT-GRAND-TOTAL` | Grand total |

---

## 18. Menu Definitions

### Main Menu (`COMEN02Y.cpy`)

11 options available to regular users:

| Option | Label | Target Program | User Type |
|--------|-------|---------------|-----------|
| 1 | Account View | COACTVWC | U |
| 2 | Account Update | COACTUPC | U |
| 3 | Credit Card List | COCRDLIC | U |
| 4 | Credit Card View | COCRDSLC | U |
| 5 | Credit Card Update | COCRDUPC | U |
| 6 | Transaction List | COTRN00C | U |
| 7 | Transaction View | COTRN01C | U |
| 8 | Transaction Add | COTRN02C | U |
| 9 | Transaction Reports | CORPT00C | U |
| 10 | Bill Payment | COBIL00C | U |
| 11 | Pending Authorization View | COPAUS0C | U |

### Admin Menu (`COADM02Y.cpy`)

6 options available to admin users:

| Option | Label | Target Program |
|--------|-------|---------------|
| 1 | User List (Security) | COUSR00C |
| 2 | User Add (Security) | COUSR01C |
| 3 | User Update (Security) | COUSR02C |
| 4 | User Delete (Security) | COUSR03C |
| 5 | Transaction Type List/Update (DB2) | COTRTLIC |
| 6 | Transaction Type Maintenance (DB2) | COTRTUPC |

---

## PIC Clause Reference

For readers unfamiliar with COBOL PIC (Picture) clauses:

| PIC Pattern | Meaning | Java Equivalent |
|-------------|---------|-----------------|
| `X(n)` | Alphanumeric, n characters | `String` (length n) |
| `9(n)` | Unsigned numeric, n digits | `long` or `BigDecimal` |
| `S9(n)` | Signed numeric | `long` or `BigDecimal` |
| `S9(n)V99` | Signed decimal, 2 decimal places | `BigDecimal` |
| `9(n) COMP` | Binary (computational) | `int` / `long` |
| `X(n) VALUE '...'` | Initialized constant | `static final String` |
| `88 name VALUE '...'` | Condition name (boolean) | `enum` or `boolean` check |
| `FILLER` | Unused/reserved bytes | Not mapped |

**Modernization Mapping:**
- `PIC X(n)` → `String` (trim trailing spaces)
- `PIC 9(n)` → `long` (or `int` if small)
- `PIC S9(n)V99` → `BigDecimal` (preserve precision for financial amounts)
- `PIC X(10)` dates → `LocalDate` (parse YYYY-MM-DD)
- `PIC X(26)` timestamps → `LocalDateTime` or `Instant`
- `88-level conditions` → Java `enum` or validation methods
