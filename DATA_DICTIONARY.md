# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** CardDemo (AWS Mainframe Credit Card Management)
>
> This document extracts all business entities from the COBOL copybook PIC clauses
> and presents them in a business-friendly format suitable for Java POJO / database
> schema design during modernization.

---

## Entity Relationship Overview

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│   Customer   │1    N │   Account    │1    N │     Card     │
│  (CVCUS01Y)  │───────│  (CVACT01Y)  │───────│  (CVACT02Y)  │
└──────────────┘       └──────┬───────┘       └──────┬───────┘
                              │                      │
                              │                      │
                       ┌──────┴───────┐       ┌──────┴───────┐
                       │  Trans Cat   │       │  Card XREF   │
                       │   Balance    │       │  (CVACT03Y)  │
                       │  (CVTRA01Y)  │       └──────────────┘
                       └──────────────┘
                              │
                       ┌──────┴───────┐
                       │ Transaction  │       ┌──────────────┐
                       │  (CVTRA05Y)  │       │  User Sec    │
                       │  (CVTRA06Y)  │       │  (CSUSR01Y)  │
                       └──────────────┘       └──────────────┘
```

---

## 1. Customer (`CVCUS01Y.cpy`)

**Record Length:** 500 bytes | **VSAM Dataset:** `CARDDEMO.CUSTDATA.VSAM.KSDS` | **Key:** Customer ID (9 bytes)

| COBOL Field | PIC Clause | Offset | Length | Java Type | Business Name | Description |
|-------------|-----------|--------|--------|-----------|---------------|-------------|
| CUST-ID | 9(09) | 0 | 9 | `long` | Customer ID | Unique customer identifier (numeric) |
| CUST-FIRST-NAME | X(25) | 9 | 25 | `String` | First Name | Customer first name |
| CUST-MIDDLE-NAME | X(25) | 34 | 25 | `String` | Middle Name | Customer middle name |
| CUST-LAST-NAME | X(25) | 59 | 25 | `String` | Last Name | Customer last name |
| CUST-ADDR-LINE-1 | X(50) | 84 | 50 | `String` | Address Line 1 | Street address line 1 |
| CUST-ADDR-LINE-2 | X(50) | 134 | 50 | `String` | Address Line 2 | Street address line 2 |
| CUST-ADDR-LINE-3 | X(50) | 184 | 50 | `String` | Address Line 3 | Street address line 3 |
| CUST-ADDR-STATE-CD | X(02) | 234 | 2 | `String` | State Code | US state code (e.g., "CA") |
| CUST-ADDR-COUNTRY-CD | X(03) | 236 | 3 | `String` | Country Code | ISO country code |
| CUST-ADDR-ZIP | X(10) | 239 | 10 | `String` | ZIP Code | Postal/ZIP code |
| CUST-PHONE-NUM-1 | X(15) | 249 | 15 | `String` | Phone 1 | Primary phone number |
| CUST-PHONE-NUM-2 | X(15) | 264 | 15 | `String` | Phone 2 | Secondary phone number |
| CUST-SSN | 9(09) | 279 | 9 | `String` | SSN | Social Security Number (PII) |
| CUST-GOVT-ISSUED-ID | X(20) | 288 | 20 | `String` | Gov't ID | Government-issued identification |
| CUST-DOB-YYYYMMDD | X(10) | 308 | 10 | `LocalDate` | Date of Birth | Birth date in YYYY-MM-DD |
| CUST-EFT-ACCOUNT-ID | X(10) | 318 | 10 | `String` | EFT Account | Electronic fund transfer account |
| CUST-PRI-CARD-HOLDER-IND | X(01) | 328 | 1 | `String` | Primary Cardholder | Y/N — primary cardholder indicator |
| CUST-FICO-CREDIT-SCORE | 9(03) | 329 | 3 | `int` | FICO Score | Credit score (000–999) |
| FILLER | X(168) | 332 | 168 | — | — | Reserved space |

---

## 2. Account (`CVACT01Y.cpy`)

**Record Length:** 300 bytes | **VSAM Dataset:** `CARDDEMO.ACCTDATA.VSAM.KSDS` | **Key:** Account ID (11 bytes)

| COBOL Field | PIC Clause | Offset | Length | Java Type | Business Name | Description |
|-------------|-----------|--------|--------|-----------|---------------|-------------|
| ACCT-ID | 9(11) | 0 | 11 | `long` | Account ID | Unique account identifier |
| ACCT-ACTIVE-STATUS | X(01) | 11 | 1 | `String` | Active Status | Y = Active, N = Inactive |
| ACCT-CURR-BAL | S9(10)V99 | 12 | 12 | `BigDecimal` | Current Balance | Current account balance (signed, 2 decimals) |
| ACCT-CREDIT-LIMIT | S9(10)V99 | 24 | 12 | `BigDecimal` | Credit Limit | Maximum credit limit |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | 36 | 12 | `BigDecimal` | Cash Credit Limit | Cash advance credit limit |
| ACCT-OPEN-DATE | X(10) | 48 | 10 | `LocalDate` | Open Date | Account opening date |
| ACCT-EXPIRAION-DATE | X(10) | 58 | 10 | `LocalDate` | Expiration Date | Account expiration date |
| ACCT-REISSUE-DATE | X(10) | 68 | 10 | `LocalDate` | Reissue Date | Card reissue date |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | 78 | 12 | `BigDecimal` | Cycle Credit | Current cycle credits |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | 90 | 12 | `BigDecimal` | Cycle Debit | Current cycle debits |
| ACCT-ADDR-ZIP | X(10) | 102 | 10 | `String` | ZIP Code | Account billing ZIP code |
| ACCT-GROUP-ID | X(10) | 112 | 10 | `String` | Group ID | Disclosure / rate group identifier |
| FILLER | X(178) | 122 | 178 | — | — | Reserved space |

---

## 3. Card (`CVACT02Y.cpy`)

**Record Length:** 150 bytes | **VSAM Dataset:** `CARDDEMO.CARDDATA.VSAM.KSDS` | **Key:** Card Number (16 bytes)

| COBOL Field | PIC Clause | Offset | Length | Java Type | Business Name | Description |
|-------------|-----------|--------|--------|-----------|---------------|-------------|
| CARD-NUM | X(16) | 0 | 16 | `String` | Card Number | 16-digit credit card number (PII) |
| CARD-ACCT-ID | 9(11) | 16 | 11 | `long` | Account ID | Owning account identifier |
| CARD-CVV-CD | 9(03) | 27 | 3 | `String` | CVV Code | Card verification value (PII) |
| CARD-EMBOSSED-NAME | X(50) | 30 | 50 | `String` | Embossed Name | Name printed on card |
| CARD-EXPIRAION-DATE | X(10) | 80 | 10 | `LocalDate` | Expiration Date | Card expiration date |
| CARD-ACTIVE-STATUS | X(01) | 90 | 1 | `String` | Active Status | Y = Active, N = Inactive |
| FILLER | X(59) | 91 | 59 | — | — | Reserved space |

---

## 4. Card Cross-Reference (`CVACT03Y.cpy`)

**Record Length:** 50 bytes | **VSAM Dataset:** `CARDDEMO.CARDXREF.VSAM.KSDS` | **Key:** Card Number (16 bytes)

| COBOL Field | PIC Clause | Offset | Length | Java Type | Business Name | Description |
|-------------|-----------|--------|--------|-----------|---------------|-------------|
| XREF-CARD-NUM | X(16) | 0 | 16 | `String` | Card Number | Card number (FK to Card) |
| XREF-CUST-ID | 9(09) | 16 | 9 | `long` | Customer ID | Owning customer (FK to Customer) |
| XREF-ACCT-ID | 9(11) | 25 | 11 | `long` | Account ID | Owning account (FK to Account) |
| FILLER | X(14) | 36 | 14 | — | — | Reserved space |

> **Business Rule:** This cross-reference links a card number to both its customer and account,
> enabling lookup of account and customer data from a card number during transaction processing.

---

## 5. Transaction (`CVTRA05Y.cpy`)

**Record Length:** 350 bytes | **VSAM Dataset:** `CARDDEMO.TRANSACT.VSAM.KSDS` | **Key:** Transaction ID (16 bytes)

| COBOL Field | PIC Clause | Offset | Length | Java Type | Business Name | Description |
|-------------|-----------|--------|--------|-----------|---------------|-------------|
| TRAN-ID | X(16) | 0 | 16 | `String` | Transaction ID | Unique transaction identifier |
| TRAN-TYPE-CD | X(02) | 16 | 2 | `String` | Type Code | Transaction type code (FK to Tran Type) |
| TRAN-CAT-CD | 9(04) | 18 | 4 | `int` | Category Code | Transaction category code |
| TRAN-SOURCE | X(10) | 22 | 10 | `String` | Source | Transaction source system/channel |
| TRAN-DESC | X(100) | 32 | 100 | `String` | Description | Free-text transaction description |
| TRAN-AMT | S9(09)V99 | 132 | 11 | `BigDecimal` | Amount | Transaction amount (signed, 2 decimals) |
| TRAN-MERCHANT-ID | 9(09) | 143 | 9 | `long` | Merchant ID | Merchant identifier |
| TRAN-MERCHANT-NAME | X(50) | 152 | 50 | `String` | Merchant Name | Merchant business name |
| TRAN-MERCHANT-CITY | X(50) | 202 | 50 | `String` | Merchant City | Merchant city |
| TRAN-MERCHANT-ZIP | X(10) | 252 | 10 | `String` | Merchant ZIP | Merchant postal code |
| TRAN-CARD-NUM | X(16) | 262 | 16 | `String` | Card Number | Card used for transaction (FK to Card) |
| TRAN-ORIG-TS | X(26) | 278 | 26 | `Instant` | Origination Timestamp | When transaction was initiated |
| TRAN-PROC-TS | X(26) | 304 | 26 | `Instant` | Processing Timestamp | When transaction was processed |
| FILLER | X(20) | 330 | 20 | — | — | Reserved space |

---

## 6. Daily Transaction (`CVTRA06Y.cpy`)

**Record Length:** 350 bytes | **VSAM Dataset:** `CARDDEMO.DALYTRAN` | **Key:** Transaction ID (16 bytes)

Identical structure to Transaction (CVTRA05Y) but with `DALYTRAN-` prefix. This is the staging
area for daily transactions before they are posted to the master transaction file.

| COBOL Field | PIC Clause | Length | Java Type | Business Name |
|-------------|-----------|--------|-----------|---------------|
| DALYTRAN-ID | X(16) | 16 | `String` | Transaction ID |
| DALYTRAN-TYPE-CD | X(02) | 2 | `String` | Type Code |
| DALYTRAN-CAT-CD | 9(04) | 4 | `int` | Category Code |
| DALYTRAN-SOURCE | X(10) | 10 | `String` | Source |
| DALYTRAN-DESC | X(100) | 100 | `String` | Description |
| DALYTRAN-AMT | S9(09)V99 | 11 | `BigDecimal` | Amount |
| DALYTRAN-MERCHANT-ID | 9(09) | 9 | `long` | Merchant ID |
| DALYTRAN-MERCHANT-NAME | X(50) | 50 | `String` | Merchant Name |
| DALYTRAN-MERCHANT-CITY | X(50) | 50 | `String` | Merchant City |
| DALYTRAN-MERCHANT-ZIP | X(10) | 10 | `String` | Merchant ZIP |
| DALYTRAN-CARD-NUM | X(16) | 16 | `String` | Card Number |
| DALYTRAN-ORIG-TS | X(26) | 26 | `Instant` | Origination Timestamp |
| DALYTRAN-PROC-TS | X(26) | 26 | `Instant` | Processing Timestamp |
| FILLER | X(20) | 20 | — | — |

---

## 7. Transaction Category Balance (`CVTRA01Y.cpy`)

**Record Length:** 50 bytes | **VSAM Dataset:** `CARDDEMO.TCATBALF.VSAM.KSDS` | **Key:** Account ID + Type Code + Category Code

| COBOL Field | PIC Clause | Length | Java Type | Business Name | Description |
|-------------|-----------|--------|-----------|---------------|-------------|
| TRANCAT-ACCT-ID | 9(11) | 11 | `long` | Account ID | Account identifier (part of composite key) |
| TRANCAT-TYPE-CD | X(02) | 2 | `String` | Type Code | Transaction type code (part of composite key) |
| TRANCAT-CD | 9(04) | 4 | `int` | Category Code | Category code (part of composite key) |
| TRAN-CAT-BAL | S9(09)V99 | 11 | `BigDecimal` | Category Balance | Running balance for this category |
| FILLER | X(22) | 22 | — | — | Reserved space |

> **Business Rule:** Tracks the running balance per account per transaction type/category
> combination. Updated during transaction posting and interest calculation.

---

## 8. Disclosure Group (`CVTRA02Y.cpy`)

**Record Length:** 50 bytes | **VSAM Dataset:** `CARDDEMO.DISCGRP.VSAM.KSDS` | **Key:** Group ID + Type Code + Category Code

| COBOL Field | PIC Clause | Length | Java Type | Business Name | Description |
|-------------|-----------|--------|-----------|---------------|-------------|
| DIS-ACCT-GROUP-ID | X(10) | 10 | `String` | Group ID | Disclosure / rate group identifier |
| DIS-TRAN-TYPE-CD | X(02) | 2 | `String` | Type Code | Transaction type code |
| DIS-TRAN-CAT-CD | 9(04) | 4 | `int` | Category Code | Transaction category code |
| DIS-INT-RATE | S9(04)V99 | 6 | `BigDecimal` | Interest Rate | Annual interest rate (%) for this combination |
| FILLER | X(28) | 28 | — | — | Reserved space |

> **Business Rule:** Maps (Group, Type, Category) → Interest Rate. Accounts belong to
> a group; interest is calculated using the rate from this table for each category balance.

---

## 9. Transaction Type (`CVTRA03Y.cpy`)

**Record Length:** 60 bytes | **VSAM Dataset:** `CARDDEMO.TRANTYPE.VSAM.KSDS` | **Key:** Transaction Type (2 bytes)

| COBOL Field | PIC Clause | Length | Java Type | Business Name | Description |
|-------------|-----------|--------|-----------|---------------|-------------|
| TRAN-TYPE | X(02) | 2 | `String` | Type Code | Transaction type code (e.g., "01", "02") |
| TRAN-TYPE-DESC | X(50) | 50 | `String` | Type Description | Human-readable description |
| FILLER | X(08) | 8 | — | — | Reserved space |

---

## 10. Transaction Category (`CVTRA04Y.cpy`)

**Record Length:** 60 bytes | **VSAM Dataset:** `CARDDEMO.TRANCATG.VSAM.KSDS` | **Key:** Type Code + Category Code

| COBOL Field | PIC Clause | Length | Java Type | Business Name | Description |
|-------------|-----------|--------|-----------|---------------|-------------|
| TRAN-TYPE-CD | X(02) | 2 | `String` | Type Code | Transaction type code (FK to Tran Type) |
| TRAN-CAT-CD | 9(04) | 4 | `int` | Category Code | Category code within type |
| TRAN-CAT-TYPE-DESC | X(50) | 50 | `String` | Category Description | Human-readable category description |
| FILLER | X(04) | 4 | — | — | Reserved space |

---

## 11. User Security (`CSUSR01Y.cpy`)

**Record Length:** 80 bytes | **VSAM Dataset:** `CARDDEMO.USRSEC.VSAM.KSDS` | **Key:** User ID (8 bytes)

| COBOL Field | PIC Clause | Length | Java Type | Business Name | Description |
|-------------|-----------|--------|-----------|---------------|-------------|
| SEC-USR-ID | X(08) | 8 | `String` | User ID | Login user identifier |
| SEC-USR-FNAME | X(20) | 20 | `String` | First Name | User first name |
| SEC-USR-LNAME | X(20) | 20 | `String` | Last Name | User last name |
| SEC-USR-PWD | X(08) | 8 | `String` | Password | User password (plaintext — modernize to hashed) |
| SEC-USR-TYPE | X(01) | 1 | `String` | User Type | A = Admin, U = Regular User |
| FILLER | X(23) | 23 | — | — | Reserved space |

> **Security Note:** Passwords are stored in plaintext in the VSAM file. During modernization,
> this must be replaced with hashed passwords (bcrypt/scrypt) and proper authentication.

---

## 12. Common Communication Area (`COCOM01Y.cpy`)

**Purpose:** The COMMAREA passed between CICS programs to maintain session state.

| COBOL Field | PIC Clause | Length | Java Type | Business Name |
|-------------|-----------|--------|-----------|---------------|
| CDEMO-FROM-TRANID | X(04) | 4 | `String` | Source Transaction ID |
| CDEMO-FROM-PROGRAM | X(08) | 8 | `String` | Source Program Name |
| CDEMO-TO-TRANID | X(04) | 4 | `String` | Target Transaction ID |
| CDEMO-TO-PROGRAM | X(08) | 8 | `String` | Target Program Name |
| CDEMO-PGM-REENTER | X(01) | 1 | `boolean` | Re-entry Flag |
| CDEMO-USR-ID | X(08) | 8 | `String` | Current User ID |
| CDEMO-USR-TYP | X(01) | 1 | `String` | Current User Type |
| CDEMO-USR-FNAME | X(20) | 20 | `String` | Current User First Name |
| CDEMO-USR-LNAME | X(20) | 20 | `String` | Current User Last Name |
| CDEMO-ACCT-ID | 9(11) | 11 | `long` | Selected Account ID |
| CDEMO-ACCT-STATUS | X(01) | 1 | `String` | Account Status |
| CDEMO-CARD-NUM | X(16) | 16 | `String` | Selected Card Number |
| CDEMO-LAST-MAP | X(07) | 7 | `String` | Last BMS Map Sent |
| CDEMO-LAST-MAPSET | X(07) | 7 | `String` | Last BMS Mapset |

> **Modernization Note:** This COMMAREA maps to a session/context object in Java (e.g., HTTP session
> attributes, Spring Security context, or a DTO passed between service methods).

---

## 13. Export/Import Record (`CVEXPORT.cpy`)

**Purpose:** Unified record format for data export/import across all entity types.

| COBOL Field | PIC Clause | Length | Java Type | Business Name |
|-------------|-----------|--------|-----------|---------------|
| EXP-RECORD-TYPE | X(01) | 1 | `String` | Record Type Indicator |
| EXP-CUST-* | (various) | 500 | — | Customer fields (when type = 'C') |
| EXP-ACCT-* | (various) | 300 | — | Account fields (when type = 'A') |
| EXP-CARD-* | (various) | 150 | — | Card fields (when type = 'R') |
| EXP-TRAN-* | (various) | 350 | — | Transaction fields (when type = 'T') |
| EXP-XREF-* | (various) | 50 | — | Cross-reference fields (when type = 'X') |

> Record type indicators: **C** = Customer, **A** = Account, **R** = Card, **T** = Transaction, **X** = Cross-Reference

---

## 14. Report Structures (`CVTRA07Y.cpy`)

**Purpose:** Print/report formatting structures for the Daily Transaction Report.

| Structure | Description |
|-----------|-------------|
| REPORT-NAME-HEADER | Report title line: short name, long name, date range |
| TRANSACTION-DETAIL-REPORT | Detail line: Trans ID, Account, Type, Category, Source, Amount |
| TRANSACTION-HEADER-1 | Column header line 1 |
| TRANSACTION-HEADER-2 | Separator line (dashes) |
| REPORT-PAGE-TOTALS | Page subtotal line |
| REPORT-ACCOUNT-TOTALS | Account subtotal line |
| REPORT-GRAND-TOTALS | Grand total line |

---

## VSAM File Summary

| VSAM Dataset | Key | Rec Len | Copybook | Entity | Access |
|---|---|---|---|---|---|
| CARDDEMO.CUSTDATA.VSAM.KSDS | CUST-ID (9) | 500 | CVCUS01Y | Customer | Random + Sequential |
| CARDDEMO.ACCTDATA.VSAM.KSDS | ACCT-ID (11) | 300 | CVACT01Y | Account | Random + Sequential |
| CARDDEMO.CARDDATA.VSAM.KSDS | CARD-NUM (16) | 150 | CVACT02Y | Card | Random |
| CARDDEMO.CARDXREF.VSAM.KSDS | XREF-CARD-NUM (16) | 50 | CVACT03Y | Card XREF | Random |
| CARDDEMO.CARDXREF.VSAM.AIX | XREF-ACCT-ID (11) | 50 | CVACT03Y | Card XREF (by Acct) | AIX Path |
| CARDDEMO.TRANSACT.VSAM.KSDS | TRAN-ID (16) | 350 | CVTRA05Y | Transaction | Random + Sequential |
| CARDDEMO.DALYTRAN.* | DALYTRAN-ID (16) | 350 | CVTRA06Y | Daily Transaction | Sequential |
| CARDDEMO.TCATBALF.VSAM.KSDS | Composite (17) | 50 | CVTRA01Y | Category Balance | Random |
| CARDDEMO.DISCGRP.VSAM.KSDS | Composite (16) | 50 | CVTRA02Y | Disclosure Group | Random |
| CARDDEMO.TRANTYPE.VSAM.KSDS | TRAN-TYPE (2) | 60 | CVTRA03Y | Transaction Type | Random |
| CARDDEMO.TRANCATG.VSAM.KSDS | Composite (6) | 60 | CVTRA04Y | Transaction Category | Random |
| CARDDEMO.USRSEC.VSAM.KSDS | SEC-USR-ID (8) | 80 | CSUSR01Y | User Security | Random |

---

## PII / Sensitive Data Flags

| Field | Entity | Sensitivity | Modernization Action |
|-------|--------|-------------|---------------------|
| CUST-SSN | Customer | **PII — High** | Encrypt at rest; mask in UI |
| CARD-NUM | Card / Transaction | **PCI-DSS** | Tokenize; mask in logs and UI |
| CARD-CVV-CD | Card | **PCI-DSS** | Never store post-authorization |
| SEC-USR-PWD | User Security | **Credential** | Hash with bcrypt/scrypt; never store plaintext |
| CUST-DOB-YYYYMMDD | Customer | **PII — Medium** | Restrict access; mask in non-essential views |
| CUST-GOVT-ISSUED-ID | Customer | **PII — High** | Encrypt at rest |
