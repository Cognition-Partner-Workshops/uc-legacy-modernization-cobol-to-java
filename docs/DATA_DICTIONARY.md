# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** Copybook PIC clause analysis across `app/cpy/` and `app/cpy-bms/`  
> **Purpose:** Business-friendly reference for all data entities, fields, and their types for modernization planning

---

## Entity Relationship Overview

```
CUSTOMER (1) ──── (N) ACCOUNT (1) ──── (N) CARD-XREF (1) ──── (N) TRANSACTION
    │                     │                      │
    │                     │                      └── Links card number to account
    │                     │
    │                     ├──── (N) TRAN-CAT-BAL (category subtotals)
    │                     │
    │                     └──── (N) DISCLOSURE-GROUP (interest rates)
    │
    └── Identified by CUST-ID, linked via ACCT-ID

USER-SECURITY ──── Standalone authentication table (no FK to Customer)

TRANSACTION-TYPE ──── Reference table for type codes
TRANSACTION-CATEGORY ──── Reference table for category codes

DAILY-TRANSACTION ──── Staging file for batch posting into TRANSACTION
```

---

## 1. Account Master (`CVACT01Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`  
**Record Length:** 300 bytes | **Key:** Account ID (11 bytes, position 0)  
**Business Purpose:** Stores credit card account details including balances, limits, and status.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|-------:|----------------------|
| ACCT-ID | 9(11) | Numeric | 11 | Account identifier (primary key) |
| ACCT-ACTIVE-STATUS | X(01) | Alpha | 1 | Account status flag (Y=Active) |
| ACCT-CURR-BAL | S9(10)V99 | Signed Decimal | 12 | Current account balance |
| ACCT-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12 | Credit limit |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12 | Cash advance credit limit |
| ACCT-OPEN-DATE | X(10) | Date String | 10 | Account opening date |
| ACCT-EXPIRAION-DATE | X(10) | Date String | 10 | Account expiration date |
| ACCT-REISSUE-DATE | X(10) | Date String | 10 | Card reissue date |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | Signed Decimal | 12 | Current cycle credit total |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | Signed Decimal | 12 | Current cycle debit total |
| ACCT-ADDR-ZIP | X(10) | Alpha | 10 | Account billing ZIP code |
| ACCT-GROUP-ID | X(10) | Alpha | 10 | Account group for disclosure/rate lookup |
| FILLER | X(178) | -- | 178 | Reserved space |

**Modernization Notes:**
- Maps to a `Account` JPA entity / database table
- `ACCT-EXPIRAION-DATE` contains a typo (missing 't') -- preserve or fix during migration
- Balance fields use `S9(10)V99` -- map to `BigDecimal` in Java

---

## 2. Card Data (`CVACT02Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`  
**Record Length:** 150 bytes | **Key:** Card Number (16 bytes, position 0)  
**Business Purpose:** Physical credit card details -- embossed name, CVV, expiration, status.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|-------:|----------------------|
| CARD-NUM | X(16) | Alpha | 16 | Card number (primary key) |
| CARD-ACCT-ID | 9(11) | Numeric | 11 | Owning account ID (FK to Account) |
| CARD-CVV-CD | 9(03) | Numeric | 3 | Card verification value |
| CARD-EMBOSSED-NAME | X(50) | Alpha | 50 | Name printed on card |
| CARD-EXPIRAION-DATE | X(10) | Date String | 10 | Card expiration date |
| CARD-ACTIVE-STATUS | X(01) | Alpha | 1 | Card active flag (Y/N) |
| FILLER | X(59) | -- | 59 | Reserved space |

**Modernization Notes:**
- Maps to a `Card` entity with FK to `Account`
- CVV should be encrypted at rest in modernized system
- Card number should be masked in logs/UI (PCI-DSS compliance)

---

## 3. Customer Master (`CVCUS01Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`  
**Record Length:** 500 bytes | **Key:** Customer ID (9 bytes, position 0)  
**Business Purpose:** Customer personal information -- name, address, contact, demographics.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|-------:|----------------------|
| CUST-ID | 9(09) | Numeric | 9 | Customer identifier (primary key) |
| CUST-FIRST-NAME | X(25) | Alpha | 25 | First name |
| CUST-MIDDLE-NAME | X(25) | Alpha | 25 | Middle name |
| CUST-LAST-NAME | X(25) | Alpha | 25 | Last name |
| CUST-ADDR-LINE-1 | X(50) | Alpha | 50 | Address line 1 |
| CUST-ADDR-LINE-2 | X(50) | Alpha | 50 | Address line 2 |
| CUST-ADDR-LINE-3 | X(50) | Alpha | 50 | Address line 3 |
| CUST-ADDR-STATE-CD | X(02) | Alpha | 2 | State code |
| CUST-ADDR-COUNTRY-CD | X(03) | Alpha | 3 | Country code |
| CUST-ADDR-ZIP | X(10) | Alpha | 10 | ZIP/postal code |
| CUST-PHONE-NUM-1 | X(15) | Alpha | 15 | Primary phone |
| CUST-PHONE-NUM-2 | X(15) | Alpha | 15 | Secondary phone |
| CUST-SSN | 9(09) | Numeric | 9 | Social Security Number |
| CUST-GOVT-ISSUED-ID | X(20) | Alpha | 20 | Government-issued ID |
| CUST-DOB-YYYYMMDD | X(10) | Date String | 10 | Date of birth |
| CUST-EFT-ACCOUNT-ID | X(10) | Alpha | 10 | EFT/direct deposit account |
| CUST-PRI-CARD-HOLDER-IND | X(01) | Alpha | 1 | Primary cardholder indicator |
| CUST-FICO-CREDIT-SCORE | 9(03) | Numeric | 3 | FICO credit score |
| FILLER | X(168) | -- | 168 | Reserved space |

**Modernization Notes:**
- Maps to a `Customer` entity
- `CUST-SSN` is PII -- must be encrypted and access-controlled
- `CUST-FICO-CREDIT-SCORE` should be an integer (0-850 range)
- Address fields can be normalized into an `Address` embedded object

---

## 4. Card Cross-Reference (`CVACT03Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`  
**Record Length:** 50 bytes | **Key:** Card Number (16 bytes, position 0)  
**Alternate Index:** Account ID (11 bytes, position 16) -- non-unique  
**Business Purpose:** Links card numbers to account IDs. Used for lookups in both directions.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|-------:|----------------------|
| XREF-CARD-NUM | X(16) | Alpha | 16 | Card number (primary key) |
| XREF-ACCT-ID | 9(11) | Numeric | 11 | Account ID (FK to Account) |
| XREF-CUST-ID | 9(09) | Numeric | 9 | Customer ID (FK to Customer) |
| FILLER | X(14) | -- | 14 | Reserved space |

**Modernization Notes:**
- In a relational model, this becomes a join table or can be eliminated if Card already has ACCT-ID
- The AIX (Alternate Index) maps to a database index on ACCT-ID

---

## 5. Transaction Record (`CVTRA05Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`  
**Record Length:** 350 bytes | **Key:** Transaction ID (16 bytes, position 0)  
**Business Purpose:** Core transaction master -- every credit card purchase, payment, and adjustment.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|-------:|----------------------|
| TRAN-ID | X(16) | Alpha | 16 | Transaction identifier (primary key) |
| TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code (FK to Tran Type) |
| TRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| TRAN-SOURCE | X(10) | Alpha | 10 | Transaction source (POS, ATM, Online, etc.) |
| TRAN-DESC | X(100) | Alpha | 100 | Transaction description |
| TRAN-AMT | S9(09)V99 | Signed Decimal | 11 | Transaction amount |
| TRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant identifier |
| TRAN-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| TRAN-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| TRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP code |
| TRAN-CARD-NUM | X(16) | Alpha | 16 | Card used for transaction |
| TRAN-ORIG-TS | X(26) | Timestamp | 26 | Transaction origination timestamp |
| TRAN-PROC-TS | X(26) | Timestamp | 26 | Transaction processing timestamp |
| FILLER | X(20) | -- | 20 | Reserved space |

**Modernization Notes:**
- Core business entity -- maps to `Transaction` JPA entity
- Merchant data could be extracted into a separate `Merchant` entity (normalization)
- Timestamps should become `java.time.Instant` or `LocalDateTime`
- `TRAN-AMT` with `S9(09)V99` maps to `BigDecimal` (max +-999,999,999.99)

---

## 6. Daily Transaction Record (`CVTRA06Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.DALYTRAN.VSAM.KSDS`  
**Record Length:** 350 bytes | **Key:** Daily Transaction ID (16 bytes)  
**Business Purpose:** Staging file for incoming daily transactions before posting to master.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|-------:|----------------------|
| DALYTRAN-ID | X(16) | Alpha | 16 | Daily transaction ID |
| DALYTRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| DALYTRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| DALYTRAN-SOURCE | X(10) | Alpha | 10 | Transaction source |
| DALYTRAN-DESC | X(100) | Alpha | 100 | Description |
| DALYTRAN-AMT | S9(09)V99 | Signed Decimal | 11 | Amount |
| DALYTRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant ID |
| DALYTRAN-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| DALYTRAN-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| DALYTRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP |
| DALYTRAN-CARD-NUM | X(16) | Alpha | 16 | Card number |
| DALYTRAN-ORIG-TS | X(26) | Timestamp | 26 | Origination timestamp |
| DALYTRAN-PROC-TS | X(26) | Timestamp | 26 | Processing timestamp |
| FILLER | X(20) | -- | 20 | Reserved space |

**Modernization Notes:**
- Identical layout to CVTRA05Y -- in Java, reuse the same `Transaction` DTO
- In a modern system, this becomes a message queue or staging table
- Batch posting (CBTRN02C) reads this and writes to Transaction master

---

## 7. Transaction Category Balance (`CVTRA01Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS`  
**Record Length:** 50 bytes | **Key:** Account ID + Type Code + Category Code (17 bytes)  
**Business Purpose:** Running balance per account per transaction type/category.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|-------:|----------------------|
| TRANCAT-ACCT-ID | 9(11) | Numeric | 11 | Account ID (part of composite key) |
| TRANCAT-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| TRANCAT-CD | 9(04) | Numeric | 4 | Category code |
| TRAN-CAT-BAL | S9(09)V99 | Signed Decimal | 11 | Category balance |
| FILLER | X(22) | -- | 22 | Reserved space |

---

## 8. Disclosure Group (`CVTRA02Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`  
**Record Length:** 50 bytes | **Key:** Group ID + Type Code + Category Code (16 bytes)  
**Business Purpose:** Interest rate lookup by account group and transaction type/category.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|-------:|----------------------|
| DIS-ACCT-GROUP-ID | X(10) | Alpha | 10 | Account group identifier |
| DIS-TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| DIS-TRAN-CAT-CD | 9(04) | Numeric | 4 | Category code |
| DIS-INT-RATE | S9(04)V99 | Signed Decimal | 6 | Interest rate (e.g., 18.99%) |
| FILLER | X(28) | -- | 28 | Reserved space |

---

## 9. Transaction Type (`CVTRA03Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`  
**Record Length:** 60 bytes | **Key:** Transaction Type (2 bytes)  
**Business Purpose:** Reference table mapping type codes to descriptions.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|-------:|----------------------|
| TRAN-TYPE | X(02) | Alpha | 2 | Type code (e.g., "01"=Purchase, "02"=Return) |
| TRAN-TYPE-DESC | X(50) | Alpha | 50 | Type description |
| FILLER | X(08) | -- | 8 | Reserved space |

---

## 10. Transaction Category (`CVTRA04Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`  
**Record Length:** 60 bytes | **Key:** Type Code + Category Code (6 bytes)  
**Business Purpose:** Reference table for transaction sub-categories within each type.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|-------:|----------------------|
| TRAN-TYPE-CD | X(02) | Alpha | 2 | Parent type code |
| TRAN-CAT-CD | 9(04) | Numeric | 4 | Category code |
| TRAN-CAT-TYPE-DESC | X(50) | Alpha | 50 | Category description |
| FILLER | X(04) | -- | 4 | Reserved space |

---

## 11. User Security (`CSUSR01Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`  
**Record Length:** 80 bytes | **Key:** User ID (8 bytes)  
**Business Purpose:** User authentication and role management.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|-------:|----------------------|
| SEC-USR-ID | X(08) | Alpha | 8 | User login ID (primary key) |
| SEC-USR-FNAME | X(20) | Alpha | 20 | First name |
| SEC-USR-LNAME | X(20) | Alpha | 20 | Last name |
| SEC-USR-PWD | X(08) | Alpha | 8 | Password (plaintext) |
| SEC-USR-TYPE | X(01) | Alpha | 1 | User type (A=Admin, U=Regular) |
| SEC-USR-FILLER | X(23) | -- | 23 | Reserved space |

**Modernization Notes:**
- **Critical security issue:** Passwords stored in plaintext
- Must implement bcrypt/scrypt hashing in modernized system
- User type should map to role-based access control (RBAC)
- 8-char password limit should be extended

---

## 12. Export Record (`CVEXPORT.cpy`)

**Record Length:** Variable (multi-record format)  
**Business Purpose:** Multi-record export format for data migration. Contains header, customer, account, card-xref, and transaction records identified by a record-type prefix.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|-------:|----------------------|
| EXP-REC-TYPE | X(01) | Alpha | 1 | Record type (H=Header, C=Customer, A=Account, X=Xref, T=Transaction) |
| EXP-ACCT-ID | 9(11) | Numeric | 11 | Account ID (in header) |
| EXP-CUST-ID | 9(09) | Numeric | 9 | Customer ID |
| *(then type-specific fields follow)* | | | | |

---

## 13. Statement Report Layout (`COSTM01.CPY`)

**Record Length:** 350 bytes | **Key:** Card Number + Transaction ID (32 bytes)  
**Business Purpose:** Re-keyed transaction layout for statement generation -- keyed by card, then transaction.

| Field Name | PIC Clause | Type | Length | Business Description |
|------------|-----------|------|-------:|----------------------|
| TRNX-CARD-NUM | X(16) | Alpha | 16 | Card number (part of composite key) |
| TRNX-ID | X(16) | Alpha | 16 | Transaction ID (part of composite key) |
| TRNX-TYPE-CD | X(02) | Alpha | 2 | Transaction type |
| TRNX-CAT-CD | 9(04) | Numeric | 4 | Category code |
| TRNX-SOURCE | X(10) | Alpha | 10 | Source |
| TRNX-DESC | X(100) | Alpha | 100 | Description |
| TRNX-AMT | S9(09)V99 | Signed Decimal | 11 | Amount |
| TRNX-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant ID |
| TRNX-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| TRNX-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| TRNX-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP |
| TRNX-ORIG-TS | X(26) | Timestamp | 26 | Origination timestamp |
| TRNX-PROC-TS | X(26) | Timestamp | 26 | Processing timestamp |
| FILLER | X(20) | -- | 20 | Reserved |

---

## 14. Transaction Report Headers (`CVTRA07Y.cpy`)

**Business Purpose:** Print formatting structures for the daily transaction report.

| Structure | Fields | Business Description |
|-----------|--------|----------------------|
| REPORT-NAME-HEADER | REPT-SHORT-NAME, REPT-LONG-NAME, date range | Report title block |
| TRANSACTION-DETAIL-REPORT | All transaction fields formatted for print | One line per transaction |
| TRANSACTION-HEADER-1/2 | Column headers and separator line | Print header rows |
| REPORT-PAGE-TOTALS | REPT-PAGE-TOTAL | Subtotal per page |
| REPORT-ACCOUNT-TOTALS | REPT-ACCOUNT-TOTAL | Subtotal per account |
| REPORT-GRAND-TOTALS | REPT-GRAND-TOTAL | Grand total for all transactions |

---

## 15. Common Communication Area (`COCOM01Y.cpy`)

**Business Purpose:** CICS COMMAREA shared across all online programs for session state.

| Field Name | PIC Clause | Type | Business Description |
|------------|-----------|------|----------------------|
| CDEMO-FROM-TRANID | X(04) | Alpha | Originating transaction ID |
| CDEMO-FROM-PROGRAM | X(08) | Alpha | Originating program name |
| CDEMO-TO-TRANID | X(04) | Alpha | Target transaction ID |
| CDEMO-TO-PROGRAM | X(08) | Alpha | Target program name |
| CDEMO-PGM-REENTER | X(01) | Alpha | Re-entry flag |
| CDEMO-USR-ID | X(08) | Alpha | Current logged-in user |
| CDEMO-USR-TYP | X(01) | Alpha | User type (A/U) |
| CDEMO-USR-FNAME | X(20) | Alpha | User first name |
| CDEMO-USR-LNAME | X(20) | Alpha | User last name |
| CDEMO-ACCT-ID | 9(11) | Numeric | Current account in context |
| CDEMO-CARD-NUM | X(16) | Alpha | Current card in context |
| CDEMO-LAST-MAP | X(07) | Alpha | Last BMS map sent |
| CDEMO-LAST-MAPSET | X(07) | Alpha | Last BMS mapset sent |

**Modernization Notes:**
- This is the session state -- maps to HTTP session or JWT token claims
- Navigation fields (FROM/TO program/tran) become URL routing in a web app

---

## 16. VSAM Dataset Summary

| Dataset Name | Copybook | Key | Rec Len | Entity |
|-------------|----------|-----|--------:|--------|
| ACCTDATA.VSAM.KSDS | CVACT01Y | ACCT-ID (11) | 300 | Account |
| CARDDATA.VSAM.KSDS | CVACT02Y | CARD-NUM (16) | 150 | Card |
| CUSTDATA.VSAM.KSDS | CVCUS01Y | CUST-ID (9) | 500 | Customer |
| CARDXREF.VSAM.KSDS | CVACT03Y | CARD-NUM (16) | 50 | Card-Xref |
| TRANSACT.VSAM.KSDS | CVTRA05Y | TRAN-ID (16) | 350 | Transaction |
| DALYTRAN.VSAM.KSDS | CVTRA06Y | DALYTRAN-ID (16) | 350 | Daily Transaction |
| USRSEC.VSAM.KSDS | CSUSR01Y | SEC-USR-ID (8) | 80 | User Security |
| TCATBAL.VSAM.KSDS | CVTRA01Y | Composite (17) | 50 | Category Balance |
| DISCGRP.VSAM.KSDS | CVTRA02Y | Composite (16) | 50 | Disclosure Group |
| TRANTYPE.VSAM.KSDS | CVTRA03Y | TRAN-TYPE (2) | 60 | Transaction Type |
| TRANCATG.VSAM.KSDS | CVTRA04Y | Composite (6) | 60 | Transaction Category |

---

## 17. PIC Clause to Java Type Mapping Reference

| COBOL PIC | Example | Java Type | Notes |
|-----------|---------|-----------|-------|
| X(n) | X(16) | `String` | Fixed-length, right-padded with spaces |
| 9(n) | 9(11) | `long` / `String` | Use `String` if leading zeros matter (IDs) |
| S9(n)V99 | S9(09)V99 | `BigDecimal` | Implied decimal, signed -- never use `double` |
| S9(n) | S9(04) | `int` / `long` | Signed integer |
| 9(n) COMP | 9(03) COMP | `int` | Binary/packed storage |
