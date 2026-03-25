# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** Copybook PIC clause analysis across `app/cpy/` and `app/cpy-bms/`

---

## Executive Summary

This data dictionary extracts all business entities defined in CardDemo's COBOL copybooks. Each VSAM file corresponds to a logical business entity that would map to a database table in a modernized Java application. The system manages **7 primary VSAM files** and **6 reference/lookup files**, with record sizes ranging from 50 to 500 bytes.

---

## 1. Account Master

**Copybook:** `CVACT01Y.cpy` | **VSAM File:** `ACCTDATA.VSAM.KSDS` | **Record Length:** 300 bytes

| Field | PIC Clause | Type | Size | Business Description |
|-------|-----------|------|------|---------------------|
| ACCT-ID | PIC 9(11) | Numeric | 11 digits | Unique account identifier (primary key) |
| ACCT-ACTIVE-STATUS | PIC X(01) | Alpha | 1 byte | Account status: 'Y' = Active, 'N' = Closed |
| ACCT-CURR-BAL | PIC S9(10)V99 | Signed Decimal | 12 digits | Current account balance (2 decimal places) |
| ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12 digits | Maximum credit limit |
| ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12 digits | Cash advance credit limit |
| ACCT-OPEN-DATE | PIC X(10) | Alpha | 10 bytes | Date account was opened (YYYY-MM-DD) |
| ACCT-EXPIRAION-DATE | PIC X(10) | Alpha | 10 bytes | Account expiration date |
| ACCT-REISSUE-DATE | PIC X(10) | Alpha | 10 bytes | Last reissue date |
| ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Signed Decimal | 12 digits | Credits in current billing cycle |
| ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 | Signed Decimal | 12 digits | Debits in current billing cycle |
| ACCT-ADDR-ZIP | PIC X(10) | Alpha | 10 bytes | Account holder ZIP code |
| ACCT-GROUP-ID | PIC X(10) | Alpha | 10 bytes | Disclosure/interest rate group |
| FILLER | PIC X(178) | — | 178 bytes | Reserved for future use |

**Modernization Target:** `Account` JPA entity / `accounts` table

---

## 2. Card Master

**Copybook:** `CVACT02Y.cpy` | **VSAM File:** `CARDDATA.VSAM.KSDS` | **Record Length:** 150 bytes

| Field | PIC Clause | Type | Size | Business Description |
|-------|-----------|------|------|---------------------|
| CARD-NUM | PIC X(16) | Alpha | 16 bytes | Card number (primary key) |
| CARD-ACCT-ID | PIC 9(11) | Numeric | 11 digits | Owning account (FK → Account) |
| CARD-CVV-CD | PIC 9(03) | Numeric | 3 digits | Card verification value |
| CARD-EMBOSSED-NAME | PIC X(50) | Alpha | 50 bytes | Name embossed on card |
| CARD-EXPIRAION-DATE | PIC X(10) | Alpha | 10 bytes | Card expiration date |
| CARD-ACTIVE-STATUS | PIC X(01) | Alpha | 1 byte | 'Y' = Active, 'N' = Inactive |
| FILLER | PIC X(59) | — | 59 bytes | Reserved |

**Modernization Target:** `Card` JPA entity / `cards` table

---

## 3. Customer Master

**Copybook:** `CVCUS01Y.cpy` | **VSAM File:** `CUSTDATA.VSAM.KSDS` | **Record Length:** 500 bytes

| Field | PIC Clause | Type | Size | Business Description |
|-------|-----------|------|------|---------------------|
| CUST-ID | PIC 9(09) | Numeric | 9 digits | Unique customer identifier (primary key) |
| CUST-FIRST-NAME | PIC X(25) | Alpha | 25 bytes | Customer first name |
| CUST-MIDDLE-NAME | PIC X(25) | Alpha | 25 bytes | Customer middle name |
| CUST-LAST-NAME | PIC X(25) | Alpha | 25 bytes | Customer last name |
| CUST-ADDR-LINE-1 | PIC X(50) | Alpha | 50 bytes | Address line 1 |
| CUST-ADDR-LINE-2 | PIC X(50) | Alpha | 50 bytes | Address line 2 |
| CUST-ADDR-LINE-3 | PIC X(50) | Alpha | 50 bytes | Address line 3 |
| CUST-ADDR-STATE-CD | PIC X(02) | Alpha | 2 bytes | State code |
| CUST-ADDR-COUNTRY-CD | PIC X(03) | Alpha | 3 bytes | Country code |
| CUST-ADDR-ZIP | PIC X(10) | Alpha | 10 bytes | ZIP / postal code |
| CUST-PHONE-NUM-1 | PIC X(15) | Alpha | 15 bytes | Primary phone number |
| CUST-PHONE-NUM-2 | PIC X(15) | Alpha | 15 bytes | Secondary phone number |
| CUST-SSN | PIC 9(09) | Numeric | 9 digits | Social Security Number |
| CUST-GOVT-ISSUED-ID | PIC X(20) | Alpha | 20 bytes | Government-issued ID |
| CUST-DOB-YYYYMMDD | PIC X(10) | Alpha | 10 bytes | Date of birth |
| CUST-EFT-ACCOUNT-ID | PIC X(10) | Alpha | 10 bytes | EFT/direct deposit account |
| CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alpha | 1 byte | 'Y' = primary cardholder |
| CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric | 3 digits | FICO credit score |
| FILLER | PIC X(168) | — | 168 bytes | Reserved |

**Modernization Target:** `Customer` JPA entity / `customers` table

---

## 4. Card Cross-Reference

**Copybook:** `CVACT03Y.cpy` | **VSAM File:** `CARDXREF.VSAM.KSDS` | **Record Length:** 50 bytes

| Field | PIC Clause | Type | Size | Business Description |
|-------|-----------|------|------|---------------------|
| XREF-CARD-NUM | PIC X(16) | Alpha | 16 bytes | Card number (primary key) |
| XREF-CUST-ID | PIC 9(09) | Numeric | 9 digits | Customer ID (FK → Customer) |
| XREF-ACCT-ID | PIC 9(11) | Numeric | 11 digits | Account ID (FK → Account) |
| FILLER | PIC X(14) | — | 14 bytes | Reserved |

**Purpose:** Links cards to both their owning account and customer. This is the central join table for the Card → Account → Customer relationship.

**Modernization Target:** Resolved via JPA `@ManyToOne` relationships or a `card_xref` join table

---

## 5. Transaction Master

**Copybook:** `CVTRA05Y.cpy` | **VSAM File:** `TRANSACT.VSAM.KSDS` | **Record Length:** 350 bytes

| Field | PIC Clause | Type | Size | Business Description |
|-------|-----------|------|------|---------------------|
| TRAN-ID | PIC X(16) | Alpha | 16 bytes | Unique transaction identifier (primary key) |
| TRAN-TYPE-CD | PIC X(02) | Alpha | 2 bytes | Transaction type code (FK → Tran Type) |
| TRAN-CAT-CD | PIC 9(04) | Numeric | 4 digits | Transaction category code (FK → Tran Category) |
| TRAN-SOURCE | PIC X(10) | Alpha | 10 bytes | Source channel (POS, ATM, ONLINE, etc.) |
| TRAN-DESC | PIC X(100) | Alpha | 100 bytes | Transaction description |
| TRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11 digits | Transaction amount |
| TRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 digits | Merchant identifier |
| TRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 bytes | Merchant name |
| TRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 bytes | Merchant city |
| TRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 bytes | Merchant ZIP code |
| TRAN-CARD-NUM | PIC X(16) | Alpha | 16 bytes | Card used (FK → Card) |
| TRAN-ORIG-TS | PIC X(26) | Alpha | 26 bytes | Original transaction timestamp |
| TRAN-PROC-TS | PIC X(26) | Alpha | 26 bytes | Processing timestamp |
| FILLER | PIC X(20) | — | 20 bytes | Reserved |

**Modernization Target:** `Transaction` JPA entity / `transactions` table

---

## 6. Daily Transaction

**Copybook:** `CVTRA06Y.cpy` | **VSAM File:** `DALYTRAN.VSAM.KSDS` | **Record Length:** 350 bytes

| Field | PIC Clause | Type | Size | Business Description |
|-------|-----------|------|------|---------------------|
| DALYTRAN-ID | PIC X(16) | Alpha | 16 bytes | Daily transaction ID |
| DALYTRAN-TYPE-CD | PIC X(02) | Alpha | 2 bytes | Transaction type code |
| DALYTRAN-CAT-CD | PIC 9(04) | Numeric | 4 digits | Transaction category code |
| DALYTRAN-SOURCE | PIC X(10) | Alpha | 10 bytes | Source channel |
| DALYTRAN-DESC | PIC X(100) | Alpha | 100 bytes | Transaction description |
| DALYTRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11 digits | Amount |
| DALYTRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 digits | Merchant ID |
| DALYTRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 bytes | Merchant name |
| DALYTRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 bytes | Merchant city |
| DALYTRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 bytes | Merchant ZIP |
| DALYTRAN-CARD-NUM | PIC X(16) | Alpha | 16 bytes | Card number |
| DALYTRAN-ORIG-TS | PIC X(26) | Alpha | 26 bytes | Original timestamp |
| DALYTRAN-PROC-TS | PIC X(26) | Alpha | 26 bytes | Processing timestamp |
| FILLER | PIC X(20) | — | 20 bytes | Reserved |

**Purpose:** Staging area for daily transactions before posting to the master file. Same layout as Transaction Master. Cleared after each batch cycle.

**Modernization Target:** `DailyTransaction` staging entity or a `status` flag on the `transactions` table

---

## 7. User Security

**Copybook:** `CSUSR01Y.cpy` | **VSAM File:** `USRSEC.VSAM.KSDS` | **Record Length:** 80 bytes

| Field | PIC Clause | Type | Size | Business Description |
|-------|-----------|------|------|---------------------|
| SEC-USR-ID | PIC X(08) | Alpha | 8 bytes | User ID (primary key) |
| SEC-USR-FNAME | PIC X(20) | Alpha | 20 bytes | User first name |
| SEC-USR-LNAME | PIC X(20) | Alpha | 20 bytes | User last name |
| SEC-USR-PWD | PIC X(08) | Alpha | 8 bytes | User password (plaintext) |
| SEC-USR-TYPE | PIC X(01) | Alpha | 1 byte | User type: 'A' = Admin, 'U' = Regular |
| SEC-USR-FILLER | PIC X(23) | — | 23 bytes | Reserved |

**Security Note:** Passwords stored in plaintext. Modernization must add hashing (bcrypt/scrypt) and proper auth.

**Modernization Target:** `User` JPA entity / `users` table with Spring Security integration

---

## 8. Reference/Lookup Entities

### 8.1 Transaction Type

**Copybook:** `CVTRA03Y.cpy` | **VSAM File:** `TRANTYPE.VSAM.KSDS` | **Record Length:** 60 bytes

| Field | PIC Clause | Type | Size | Business Description |
|-------|-----------|------|------|---------------------|
| TRAN-TYPE | PIC X(02) | Alpha | 2 bytes | Type code (primary key) |
| TRAN-TYPE-DESC | PIC X(50) | Alpha | 50 bytes | Type description (e.g., "Purchase", "Cash Advance") |
| FILLER | PIC X(08) | — | 8 bytes | Reserved |

### 8.2 Transaction Category

**Copybook:** `CVTRA04Y.cpy` | **VSAM File:** `TRANCATG.VSAM.KSDS` | **Record Length:** 60 bytes

| Field | PIC Clause | Type | Size | Business Description |
|-------|-----------|------|------|---------------------|
| TRAN-TYPE-CD | PIC X(02) | Alpha | 2 bytes | Parent type code (FK → Tran Type) |
| TRAN-CAT-CD | PIC 9(04) | Numeric | 4 digits | Category code (composite key with type) |
| TRAN-CAT-TYPE-DESC | PIC X(50) | Alpha | 50 bytes | Category description |
| FILLER | PIC X(04) | — | 4 bytes | Reserved |

### 8.3 Transaction Category Balance

**Copybook:** `CVTRA01Y.cpy` | **VSAM File:** `TCATBALF.VSAM.KSDS` | **Record Length:** 50 bytes

| Field | PIC Clause | Type | Size | Business Description |
|-------|-----------|------|------|---------------------|
| TRANCAT-ACCT-ID | PIC 9(11) | Numeric | 11 digits | Account ID (composite key) |
| TRANCAT-TYPE-CD | PIC X(02) | Alpha | 2 bytes | Transaction type (composite key) |
| TRANCAT-CD | PIC 9(04) | Numeric | 4 digits | Category code (composite key) |
| TRAN-CAT-BAL | PIC S9(09)V99 | Signed Decimal | 11 digits | Running balance for this category |
| FILLER | PIC X(22) | — | 22 bytes | Reserved |

### 8.4 Disclosure Group

**Copybook:** `CVTRA02Y.cpy` | **VSAM File:** `DISCGRP.VSAM.KSDS` | **Record Length:** 50 bytes

| Field | PIC Clause | Type | Size | Business Description |
|-------|-----------|------|------|---------------------|
| DIS-ACCT-GROUP-ID | PIC X(10) | Alpha | 10 bytes | Account group (composite key) |
| DIS-TRAN-TYPE-CD | PIC X(02) | Alpha | 2 bytes | Transaction type (composite key) |
| DIS-TRAN-CAT-CD | PIC 9(04) | Numeric | 4 digits | Category code (composite key) |
| DIS-INT-RATE | PIC S9(04)V99 | Signed Decimal | 6 digits | Interest rate for this group/type/category |
| FILLER | PIC X(28) | — | 28 bytes | Reserved |

---

## 9. Specialized/Internal Records

### 9.1 Transaction (Statement-Keyed Layout)

**Copybook:** `COSTM01.CPY` | Used by `CBSTM03A` for statement generation

| Field | PIC Clause | Type | Size | Business Description |
|-------|-----------|------|------|---------------------|
| TRNX-CARD-NUM | PIC X(16) | Alpha | 16 bytes | Card number (part of composite key) |
| TRNX-ID | PIC X(16) | Alpha | 16 bytes | Transaction ID (part of composite key) |
| TRNX-TYPE-CD | PIC X(02) | Alpha | 2 bytes | Transaction type |
| TRNX-CAT-CD | PIC 9(04) | Numeric | 4 digits | Category code |
| TRNX-SOURCE | PIC X(10) | Alpha | 10 bytes | Source channel |
| TRNX-DESC | PIC X(100) | Alpha | 100 bytes | Description |
| TRNX-AMT | PIC S9(09)V99 | Signed Decimal | 11 digits | Amount |
| TRNX-MERCHANT-ID | PIC 9(09) | Numeric | 9 digits | Merchant ID |
| TRNX-MERCHANT-NAME | PIC X(50) | Alpha | 50 bytes | Merchant name |
| TRNX-MERCHANT-CITY | PIC X(50) | Alpha | 50 bytes | Merchant city |
| TRNX-MERCHANT-ZIP | PIC X(10) | Alpha | 10 bytes | Merchant ZIP |
| TRNX-ORIG-TS | PIC X(26) | Alpha | 26 bytes | Original timestamp |
| TRNX-PROC-TS | PIC X(26) | Alpha | 26 bytes | Processing timestamp |
| FILLER | PIC X(20) | — | 20 bytes | Reserved |

**Note:** Same fields as CVTRA05Y but keyed by CARD-NUM + TRAN-ID (for statement ordering by card).

### 9.2 Export Record (Multi-Type)

**Copybook:** `CVEXPORT.cpy` | Used by `CBEXPORT`/`CBIMPORT`

The export file uses a tagged-record format with a 4-byte type prefix:

| Record Type | Tag | Embedded Layout |
|-------------|-----|----------------|
| Customer | `CUST` | Customer fields (CUST-ID through CUST-FICO-CREDIT-SCORE) |
| Account | `ACCT` | Account fields (ACCT-ID through ACCT-GROUP-ID) |
| Card | `CARD` | Card fields (CARD-NUM through CARD-ACTIVE-STATUS) |
| Cross-Reference | `XREF` | XREF-CARD-NUM, XREF-CUST-ID, XREF-ACCT-ID |
| Transaction | `TRAN` | Transaction fields (all TRAN-* fields) |

### 9.3 Report Layouts

**Copybook:** `CVTRA07Y.cpy` | Used by `CBTRN03C`

Defines print-formatted report structures:
- `REPORT-NAME-HEADER` — Report title and date range
- `TRANSACTION-DETAIL-REPORT` — One line per transaction
- `TRANSACTION-HEADER-1/2` — Column headers
- `REPORT-PAGE-TOTALS` / `REPORT-ACCOUNT-TOTALS` / `REPORT-GRAND-TOTALS` — Summary lines

### 9.4 Card Detail (Extended)

**Copybook:** `CVCRD01Y.cpy` | Used by `COCRDLIC`, `COCRDSLC`, `COCRDUPC`

Extends the basic card record with display-oriented fields for online screens (card number formatting, account display fields, customer name fields).

---

## 10. Common Communication Area (COMMAREA)

**Copybook:** `COCOM01Y.cpy` | Passed between all online CICS programs

| Field | PIC Clause | Type | Business Description |
|-------|-----------|------|---------------------|
| CDEMO-FROM-TRANID | PIC X(04) | Alpha | Calling transaction ID |
| CDEMO-FROM-PROGRAM | PIC X(08) | Alpha | Calling program name |
| CDEMO-TO-TRANID | PIC X(04) | Alpha | Target transaction ID |
| CDEMO-TO-PROGRAM | PIC X(08) | Alpha | Target program name |
| CDEMO-PGM-REENTER | PIC X(01) | Alpha | Re-entry flag ('Y'/'N') |
| CDEMO-USR-ID | PIC X(08) | Alpha | Current user ID |
| CDEMO-USR-TYP | PIC X(01) | Alpha | User type ('A'/'U') |
| CDEMO-PGM-CONTEXT | PIC 9(01) | Numeric | Program context flag |
| CDEMO-ACCT-ID | PIC 9(11) | Numeric | Selected account ID |
| CDEMO-CARD-NUM | PIC X(16) | Alpha | Selected card number |
| CDEMO-CUST-ID | PIC 9(09) | Numeric | Selected customer ID |
| CDEMO-LAST-MAP | PIC X(07) | Alpha | Last map sent |
| CDEMO-LAST-MAPSET | PIC X(07) | Alpha | Last mapset sent |

**Modernization Target:** HTTP session attributes or Spring Security context + request parameters

---

## 11. Entity Relationship Summary

```
Customer (CVCUS01Y)
  │
  ├──< Card Cross-Reference (CVACT03Y)  [CUST-ID → XREF-CUST-ID]
  │         │
  │         ├── Card (CVACT02Y)          [XREF-CARD-NUM → CARD-NUM]
  │         │     │
  │         │     └──< Transaction (CVTRA05Y)  [CARD-NUM → TRAN-CARD-NUM]
  │         │
  │         └── Account (CVACT01Y)       [XREF-ACCT-ID → ACCT-ID]
  │               │
  │               └──< Tran Cat Balance (CVTRA01Y)  [ACCT-ID → TRANCAT-ACCT-ID]
  │
  └── User Security (CSUSR01Y)           [independent — login credentials]

Transaction Type (CVTRA03Y)
  └──< Transaction Category (CVTRA04Y)   [TRAN-TYPE → TRAN-TYPE-CD]

Disclosure Group (CVTRA02Y)              [ACCT-GROUP-ID from Account]
  └── Interest rates per type/category

Daily Transaction (CVTRA06Y)             [staging → posted to CVTRA05Y]
```

---

## 12. VSAM File Inventory

| VSAM Dataset | Key Field(s) | Record Len | Type | Copybook |
|-------------|-------------|-----------|------|----------|
| ACCTDATA.VSAM.KSDS | ACCT-ID | 300 | KSDS | CVACT01Y |
| CARDDATA.VSAM.KSDS | CARD-NUM | 150 | KSDS | CVACT02Y |
| CUSTDATA.VSAM.KSDS | CUST-ID | 500 | KSDS | CVCUS01Y |
| CARDXREF.VSAM.KSDS | XREF-CARD-NUM | 50 | KSDS | CVACT03Y |
| TRANSACT.VSAM.KSDS | TRAN-ID | 350 | KSDS | CVTRA05Y |
| DALYTRAN.VSAM.KSDS | DALYTRAN-ID | 350 | KSDS | CVTRA06Y |
| USRSEC.VSAM.KSDS | SEC-USR-ID | 80 | KSDS | CSUSR01Y |
| TRANTYPE.VSAM.KSDS | TRAN-TYPE | 60 | KSDS | CVTRA03Y |
| TRANCATG.VSAM.KSDS | TYPE-CD + CAT-CD | 60 | KSDS | CVTRA04Y |
| TCATBALF.VSAM.KSDS | ACCT + TYPE + CAT | 50 | KSDS | CVTRA01Y |
| DISCGRP.VSAM.KSDS | GROUP + TYPE + CAT | 50 | KSDS | CVTRA02Y |
