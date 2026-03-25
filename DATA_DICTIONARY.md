# DATA DICTIONARY — CardDemo COBOL Application

> **Generated:** 2026-03-25 | **Source:** 30 copybooks in `app/cpy/` and `app/cpy-bms/`
> **Purpose:** Business-friendly data model extracted from COBOL PIC clauses

---

## Table of Contents

1. [Entity Relationship Overview](#1-entity-relationship-overview)
2. [Account Entity](#2-account-entity)
3. [Card Entity](#3-card-entity)
4. [Card Cross-Reference Entity](#4-card-cross-reference-entity)
5. [Customer Entity](#5-customer-entity)
6. [Transaction Entity](#6-transaction-entity)
7. [Daily Transaction Entity](#7-daily-transaction-entity)
8. [Transaction Category Balance Entity](#8-transaction-category-balance-entity)
9. [Disclosure Group Entity](#9-disclosure-group-entity)
10. [Transaction Type Entity](#10-transaction-type-entity)
11. [Transaction Category Entity](#11-transaction-category-entity)
12. [User Security Entity](#12-user-security-entity)
13. [Card Detail Entity](#13-card-detail-entity)
14. [Export Record Entity](#14-export-record-entity)
15. [Statement Transaction Entity](#15-statement-transaction-entity)
16. [Report Layout Structures](#16-report-layout-structures)
17. [Common / Shared Data Areas](#17-common--shared-data-areas)
18. [COBOL-to-Java Type Mapping Reference](#18-cobol-to-java-type-mapping-reference)

---

## 1. Entity Relationship Overview

```
┌─────────────┐       ┌──────────────────┐       ┌─────────────────┐
│  Customer    │1    N │   Account        │1    N │   Card          │
│  (CVCUS01Y)  ├───────┤  (CVACT01Y)      ├───────┤  (CVACT02Y)     │
│  500 bytes   │       │  300 bytes       │       │  150 bytes      │
└─────────────┘       └────────┬─────────┘       └────────┬────────┘
                               │                          │
                               │                          │
                      ┌────────┴─────────┐       ┌────────┴────────┐
                      │  Transaction     │       │  Card XRef      │
                      │  (CVTRA05Y)      │       │  (CVACT03Y)     │
                      │  350 bytes       │       │  50 bytes       │
                      └────────┬─────────┘       └─────────────────┘
                               │
              ┌────────────────┼────────────────┐
              │                │                │
     ┌────────┴────────┐ ┌────┴─────────┐ ┌────┴──────────┐
     │ Tran Cat Bal    │ │ Tran Type    │ │ Tran Category │
     │ (CVTRA01Y)      │ │ (CVTRA03Y)   │ │ (CVTRA04Y)    │
     │ 50 bytes        │ │ 60 bytes     │ │ 60 bytes      │
     └─────────────────┘ └──────────────┘ └───────────────┘
              │
     ┌────────┴────────┐       ┌─────────────────┐
     │ Disclosure Grp  │       │ User Security   │
     │ (CVTRA02Y)      │       │ (CSUSR01Y)      │
     │ 50 bytes        │       │ 80 bytes        │
     └─────────────────┘       └─────────────────┘
```

---

## 2. Account Entity

**Copybook:** `CVACT01Y` | **Record Length:** 300 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Length | Java Type | Business Name | Description |
|---|-------------|-----------|------|--------|-----------|---------------|-------------|
| 1 | ACCT-ID | 9(11) | Numeric | 11 | `long` | Account ID | Unique account identifier |
| 2 | ACCT-ACTIVE-STATUS | X(01) | Alpha | 1 | `String` | Active Status | Account active flag (Y/N) |
| 3 | ACCT-CURR-BAL | S9(10)V99 | Signed Decimal | 12.2 | `BigDecimal` | Current Balance | Current account balance |
| 4 | ACCT-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12.2 | `BigDecimal` | Credit Limit | Maximum credit limit |
| 5 | ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12.2 | `BigDecimal` | Cash Advance Limit | Cash advance credit limit |
| 6 | ACCT-OPEN-DATE | X(10) | Alpha | 10 | `LocalDate` | Open Date | Account opening date |
| 7 | ACCT-EXPIRAION-DATE | X(10) | Alpha | 10 | `LocalDate` | Expiration Date | Account expiration date |
| 8 | ACCT-REISSUE-DATE | X(10) | Alpha | 10 | `LocalDate` | Reissue Date | Last reissue date |
| 9 | ACCT-CURR-CYC-CREDIT | S9(10)V99 | Signed Decimal | 12.2 | `BigDecimal` | Cycle Credits | Current cycle credit total |
| 10 | ACCT-CURR-CYC-DEBIT | S9(10)V99 | Signed Decimal | 12.2 | `BigDecimal` | Cycle Debits | Current cycle debit total |
| 11 | ACCT-GROUP-ID | X(10) | Alpha | 10 | `String` | Group ID | Account group/portfolio ID |
| 12 | FILLER | X(178) | Alpha | 178 | — | Reserved | Future use padding |

**Key:** ACCT-ID (primary) | **Business Rules:** Balance must not exceed credit limit.

---

## 3. Card Entity

**Copybook:** `CVACT02Y` | **Record Length:** 150 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Length | Java Type | Business Name | Description |
|---|-------------|-----------|------|--------|-----------|---------------|-------------|
| 1 | CARD-NUM | X(16) | Alpha | 16 | `String` | Card Number | 16-digit credit card number |
| 2 | CARD-ACCT-ID | 9(11) | Numeric | 11 | `long` | Account ID | Owning account reference |
| 3 | CARD-CVV-CD | 9(03) | Numeric | 3 | `String` | CVV Code | Card verification value |
| 4 | CARD-EMBOSSED-NAME | X(50) | Alpha | 50 | `String` | Embossed Name | Name printed on card |
| 5 | CARD-EXPIRAION-DATE | X(10) | Alpha | 10 | `LocalDate` | Expiration Date | Card expiration date |
| 6 | CARD-ACTIVE-STATUS | X(01) | Alpha | 1 | `String` | Active Status | Card active flag (Y/N) |
| 7 | FILLER | X(59) | Alpha | 59 | — | Reserved | Future use padding |

**Key:** CARD-NUM (primary) | **FK:** CARD-ACCT-ID → Account.ACCT-ID

---

## 4. Card Cross-Reference Entity

**Copybook:** `CVACT03Y` | **Record Length:** 50 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Length | Java Type | Business Name | Description |
|---|-------------|-----------|------|--------|-----------|---------------|-------------|
| 1 | XREF-CARD-NUM | X(16) | Alpha | 16 | `String` | Card Number | Card number (lookup key) |
| 2 | XREF-CUST-ID | 9(09) | Numeric | 9 | `long` | Customer ID | Owning customer reference |
| 3 | XREF-ACCT-ID | 9(11) | Numeric | 11 | `long` | Account ID | Owning account reference |
| 4 | FILLER | X(14) | Alpha | 14 | — | Reserved | Future use padding |

**Key:** XREF-CARD-NUM (primary) | **FK:** XREF-CUST-ID → Customer, XREF-ACCT-ID → Account
**Purpose:** Links cards to both their customer and account — enables reverse lookups.

---

## 5. Customer Entity

**Copybook:** `CVCUS01Y` | **Record Length:** 500 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Length | Java Type | Business Name | Description |
|---|-------------|-----------|------|--------|-----------|---------------|-------------|
| 1 | CUST-ID | 9(09) | Numeric | 9 | `long` | Customer ID | Unique customer identifier |
| 2 | CUST-FIRST-NAME | X(25) | Alpha | 25 | `String` | First Name | Customer first name |
| 3 | CUST-MIDDLE-NAME | X(25) | Alpha | 25 | `String` | Middle Name | Customer middle name |
| 4 | CUST-LAST-NAME | X(25) | Alpha | 25 | `String` | Last Name | Customer last name |
| 5 | CUST-ADDR-LINE-1 | X(50) | Alpha | 50 | `String` | Address Line 1 | Street address line 1 |
| 6 | CUST-ADDR-LINE-2 | X(50) | Alpha | 50 | `String` | Address Line 2 | Street address line 2 |
| 7 | CUST-ADDR-LINE-3 | X(50) | Alpha | 50 | `String` | Address Line 3 | Street address line 3 |
| 8 | CUST-ADDR-STATE-CD | X(02) | Alpha | 2 | `String` | State Code | US state code |
| 9 | CUST-ADDR-COUNTRY-CD | X(03) | Alpha | 3 | `String` | Country Code | Country code |
| 10 | CUST-ADDR-ZIP | X(10) | Alpha | 10 | `String` | ZIP Code | Postal/ZIP code |
| 11 | CUST-PHONE-NUM-1 | X(15) | Alpha | 15 | `String` | Phone 1 | Primary phone number |
| 12 | CUST-PHONE-NUM-2 | X(15) | Alpha | 15 | `String` | Phone 2 | Secondary phone number |
| 13 | CUST-SSN | 9(09) | Numeric | 9 | `String` | SSN | Social Security Number (PII) |
| 14 | CUST-GOVT-ISSUED-ID | X(20) | Alpha | 20 | `String` | Govt ID | Government-issued identification |
| 15 | CUST-DOB-YYYYMMDD | X(10) | Alpha | 10 | `LocalDate` | Date of Birth | Customer date of birth (PII) |
| 16 | CUST-EFT-ACCOUNT-ID | X(10) | Alpha | 10 | `String` | EFT Account | Electronic funds transfer acct |
| 17 | CUST-PRI-CARD-HOLDER-IND | X(01) | Alpha | 1 | `String` | Primary Cardholder | Primary cardholder indicator |
| 18 | CUST-FICO-CREDIT-SCORE | 9(03) | Numeric | 3 | `int` | FICO Score | Customer credit score |
| 19 | FILLER | X(168) | Alpha | 168 | — | Reserved | Future use padding |

**Key:** CUST-ID (primary) | **PII Fields:** CUST-SSN, CUST-DOB-YYYYMMDD, CUST-GOVT-ISSUED-ID
**Note:** PII fields require special handling during migration (encryption, masking, GDPR/CCPA compliance).

---

## 6. Transaction Entity

**Copybook:** `CVTRA05Y` | **Record Length:** 350 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Length | Java Type | Business Name | Description |
|---|-------------|-----------|------|--------|-----------|---------------|-------------|
| 1 | TRAN-ID | X(16) | Alpha | 16 | `String` | Transaction ID | Unique transaction identifier |
| 2 | TRAN-TYPE-CD | X(02) | Alpha | 2 | `String` | Type Code | Transaction type code (FK) |
| 3 | TRAN-CAT-CD | 9(04) | Numeric | 4 | `int` | Category Code | Transaction category code (FK) |
| 4 | TRAN-SOURCE | X(10) | Alpha | 10 | `String` | Source | Transaction source/channel |
| 5 | TRAN-DESC | X(100) | Alpha | 100 | `String` | Description | Transaction description |
| 6 | TRAN-AMT | S9(09)V99 | Signed Decimal | 11.2 | `BigDecimal` | Amount | Transaction amount |
| 7 | TRAN-MERCHANT-ID | 9(09) | Numeric | 9 | `long` | Merchant ID | Merchant identifier |
| 8 | TRAN-MERCHANT-NAME | X(50) | Alpha | 50 | `String` | Merchant Name | Merchant business name |
| 9 | TRAN-MERCHANT-CITY | X(50) | Alpha | 50 | `String` | Merchant City | Merchant city |
| 10 | TRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | `String` | Merchant ZIP | Merchant postal code |
| 11 | TRAN-CARD-NUM | X(16) | Alpha | 16 | `String` | Card Number | Card used for transaction (FK) |
| 12 | TRAN-ORIG-TS | X(26) | Alpha | 26 | `LocalDateTime` | Origination Timestamp | When transaction was initiated |
| 13 | TRAN-PROC-TS | X(26) | Alpha | 26 | `LocalDateTime` | Processing Timestamp | When transaction was processed |
| 14 | FILLER | X(20) | Alpha | 20 | — | Reserved | Future use padding |

**Key:** TRAN-ID (primary) | **FK:** TRAN-CARD-NUM → Card, TRAN-TYPE-CD → Transaction Type, TRAN-CAT-CD → Transaction Category

---

## 7. Daily Transaction Entity

**Copybook:** `CVTRA06Y` | **Record Length:** 350 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.DALYTRAN.PS`

| # | COBOL Field | PIC Clause | Type | Length | Java Type | Business Name | Description |
|---|-------------|-----------|------|--------|-----------|---------------|-------------|
| 1 | DALYTRAN-ID | X(16) | Alpha | 16 | `String` | Daily Tran ID | Daily transaction identifier |
| 2 | DALYTRAN-TYPE-CD | X(02) | Alpha | 2 | `String` | Type Code | Transaction type code |
| 3 | DALYTRAN-CAT-CD | 9(04) | Numeric | 4 | `int` | Category Code | Transaction category code |
| 4 | DALYTRAN-SOURCE | X(10) | Alpha | 10 | `String` | Source | Transaction source |
| 5 | DALYTRAN-DESC | X(100) | Alpha | 100 | `String` | Description | Transaction description |
| 6 | DALYTRAN-AMT | S9(09)V99 | Signed Decimal | 11.2 | `BigDecimal` | Amount | Transaction amount |
| 7 | DALYTRAN-MERCHANT-ID | 9(09) | Numeric | 9 | `long` | Merchant ID | Merchant identifier |
| 8 | DALYTRAN-MERCHANT-NAME | X(50) | Alpha | 50 | `String` | Merchant Name | Merchant business name |
| 9 | DALYTRAN-MERCHANT-CITY | X(50) | Alpha | 50 | `String` | Merchant City | Merchant city |
| 10 | DALYTRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | `String` | Merchant ZIP | Merchant postal code |
| 11 | DALYTRAN-CARD-NUM | X(16) | Alpha | 16 | `String` | Card Number | Card used for transaction |
| 12 | DALYTRAN-ORIG-TS | X(26) | Alpha | 26 | `LocalDateTime` | Origination TS | When transaction originated |
| 13 | DALYTRAN-PROC-TS | X(26) | Alpha | 26 | `LocalDateTime` | Processing TS | When transaction was processed |
| 14 | FILLER | X(20) | Alpha | 20 | — | Reserved | Future use padding |

**Purpose:** Staging area for daily transactions before they are posted to the main Transaction file by CBTRN02C.

---

## 8. Transaction Category Balance Entity

**Copybook:** `CVTRA01Y` | **Record Length:** 50 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Length | Java Type | Business Name | Description |
|---|-------------|-----------|------|--------|-----------|---------------|-------------|
| 1 | TRANCAT-ACCT-ID | 9(11) | Numeric | 11 | `long` | Account ID | Account identifier (composite key) |
| 2 | TRANCAT-TYPE-CD | X(02) | Alpha | 2 | `String` | Type Code | Transaction type (composite key) |
| 3 | TRANCAT-CD | 9(04) | Numeric | 4 | `int` | Category Code | Category code (composite key) |
| 4 | TRAN-CAT-BAL | S9(09)V99 | Signed Decimal | 11.2 | `BigDecimal` | Category Balance | Running balance per category |
| 5 | FILLER | X(22) | Alpha | 22 | — | Reserved | Future use padding |

**Key:** TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD (composite)

---

## 9. Disclosure Group Entity

**Copybook:** `CVTRA02Y` | **Record Length:** 50 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Length | Java Type | Business Name | Description |
|---|-------------|-----------|------|--------|-----------|---------------|-------------|
| 1 | DIS-ACCT-GROUP-ID | X(10) | Alpha | 10 | `String` | Account Group | Account group ID (composite key) |
| 2 | DIS-TRAN-TYPE-CD | X(02) | Alpha | 2 | `String` | Tran Type | Transaction type (composite key) |
| 3 | DIS-TRAN-CAT-CD | 9(04) | Numeric | 4 | `int` | Tran Category | Transaction category (composite key) |
| 4 | DIS-INT-RATE | S9(04)V99 | Signed Decimal | 6.2 | `BigDecimal` | Interest Rate | Interest rate for this group/type |
| 5 | FILLER | X(28) | Alpha | 28 | — | Reserved | Future use padding |

**Key:** DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD (composite)
**Purpose:** Defines interest rates by account group, transaction type, and category for interest calculation (CBACT04C).

---

## 10. Transaction Type Entity

**Copybook:** `CVTRA03Y` | **Record Length:** 60 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Length | Java Type | Business Name | Description |
|---|-------------|-----------|------|--------|-----------|---------------|-------------|
| 1 | TRAN-TYPE | X(02) | Alpha | 2 | `String` | Type Code | Transaction type code (PK) |
| 2 | TRAN-TYPE-DESC | X(50) | Alpha | 50 | `String` | Type Description | Human-readable description |
| 3 | FILLER | X(08) | Alpha | 8 | — | Reserved | Future use padding |

**Key:** TRAN-TYPE (primary) | **Examples:** "01" = Purchase, "02" = Return, "03" = Cash Advance

---

## 11. Transaction Category Entity

**Copybook:** `CVTRA04Y` | **Record Length:** 60 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Length | Java Type | Business Name | Description |
|---|-------------|-----------|------|--------|-----------|---------------|-------------|
| 1 | TRAN-TYPE-CD | X(02) | Alpha | 2 | `String` | Type Code | Parent transaction type (composite key) |
| 2 | TRAN-CAT-CD | 9(04) | Numeric | 4 | `int` | Category Code | Category code (composite key) |
| 3 | TRAN-CAT-TYPE-DESC | X(50) | Alpha | 50 | `String` | Category Description | Human-readable description |
| 4 | FILLER | X(04) | Alpha | 4 | — | Reserved | Future use padding |

**Key:** TRAN-TYPE-CD + TRAN-CAT-CD (composite) | **FK:** TRAN-TYPE-CD → Transaction Type

---

## 12. User Security Entity

**Copybook:** `CSUSR01Y` | **Record Length:** 80 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`

| # | COBOL Field | PIC Clause | Type | Length | Java Type | Business Name | Description |
|---|-------------|-----------|------|--------|-----------|---------------|-------------|
| 1 | SEC-USR-ID | X(08) | Alpha | 8 | `String` | User ID | Login user identifier |
| 2 | SEC-USR-FNAME | X(20) | Alpha | 20 | `String` | First Name | User first name |
| 3 | SEC-USR-LNAME | X(20) | Alpha | 20 | `String` | Last Name | User last name |
| 4 | SEC-USR-PWD | X(08) | Alpha | 8 | `String` | Password | User password (plaintext!) |
| 5 | SEC-USR-TYPE | X(01) | Alpha | 1 | `String` | User Type | "R" = Regular, "A" = Admin |
| 6 | FILLER | X(23) | Alpha | 23 | — | Reserved | Future use padding |

**Key:** SEC-USR-ID (primary)
**Security Note:** Passwords stored in plaintext — migration must implement proper hashing (bcrypt/scrypt).

---

## 13. Card Detail Entity

**Copybook:** `CVCRD01Y` | **Used by:** Online card management screens

| # | COBOL Field | PIC Clause | Type | Length | Java Type | Business Name | Description |
|---|-------------|-----------|------|--------|-----------|---------------|-------------|
| 1 | CRDSL-ID | X(11) | Alpha | 11 | `String` | Record ID | Display record identifier |
| 2 | CRDSL-ACCT-ID | X(11) | Alpha | 11 | `String` | Account ID | Owning account |
| 3 | CRDSL-CARD-NUM | X(16) | Alpha | 16 | `String` | Card Number | Card number |
| 4 | CRDSL-CUST-ID | X(09) | Alpha | 9 | `String` | Customer ID | Customer reference |
| 5 | CRDSL-CUST-FNAME | X(25) | Alpha | 25 | `String` | First Name | Customer first name |
| 6 | CRDSL-CUST-MNAME | X(25) | Alpha | 25 | `String` | Middle Name | Customer middle name |
| 7 | CRDSL-CUST-LNAME | X(25) | Alpha | 25 | `String` | Last Name | Customer last name |

**Purpose:** Composite view structure combining card, account, and customer data for screen display.

---

## 14. Export Record Entity

**Copybook:** `CVEXPORT` | **Used by:** CBEXPORT / CBIMPORT programs

| # | COBOL Field | PIC Clause | Type | Length | Java Type | Business Name | Description |
|---|-------------|-----------|------|--------|-----------|---------------|-------------|
| 1 | EXP-REC-TYPE | X(02) | Alpha | 2 | `String` | Record Type | Identifies entity type in export |
| 2 | EXP-ACCT-ID | 9(11) | Numeric | 11 | `long` | Account ID | Account identifier |
| 3 | EXP-CARD-NUM | X(16) | Alpha | 16 | `String` | Card Number | Card number |
| 4 | EXP-ACCT-DATA | (varies) | — | — | — | Account Data | Account fields (conditional) |
| 5 | EXP-CARD-DATA | (varies) | — | — | — | Card Data | Card fields (conditional) |
| 6 | EXP-CUST-DATA | (varies) | — | — | — | Customer Data | Customer fields (conditional) |
| 7 | EXP-TRAN-DATA | (varies) | — | — | — | Transaction Data | Transaction fields (conditional) |

**Purpose:** Multiplexed record format — EXP-REC-TYPE determines which entity fields are populated. Used for bulk data export/import operations.

---

## 15. Statement Transaction Entity

**Copybook:** `COSTM01` | **Used by:** CBSTM03A (statement generation)

| # | COBOL Field | PIC Clause | Type | Length | Java Type | Business Name | Description |
|---|-------------|-----------|------|--------|-----------|---------------|-------------|
| 1 | TRNX-CARD-NUM | X(16) | Alpha | 16 | `String` | Card Number | Card number (composite key part 1) |
| 2 | TRNX-ID | X(16) | Alpha | 16 | `String` | Transaction ID | Transaction ID (composite key part 2) |
| 3 | TRNX-TYPE-CD | X(02) | Alpha | 2 | `String` | Type Code | Transaction type |
| 4 | TRNX-CAT-CD | 9(04) | Numeric | 4 | `int` | Category Code | Transaction category |
| 5 | TRNX-SOURCE | X(10) | Alpha | 10 | `String` | Source | Transaction source |
| 6 | TRNX-DESC | X(100) | Alpha | 100 | `String` | Description | Transaction description |
| 7 | TRNX-AMT | S9(09)V99 | Signed Decimal | 11.2 | `BigDecimal` | Amount | Transaction amount |
| 8 | TRNX-MERCHANT-ID | 9(09) | Numeric | 9 | `long` | Merchant ID | Merchant identifier |
| 9 | TRNX-MERCHANT-NAME | X(50) | Alpha | 50 | `String` | Merchant Name | Merchant name |
| 10 | TRNX-MERCHANT-CITY | X(50) | Alpha | 50 | `String` | Merchant City | Merchant city |
| 11 | TRNX-MERCHANT-ZIP | X(10) | Alpha | 10 | `String` | Merchant ZIP | Merchant postal code |
| 12 | TRNX-ORIG-TS | X(26) | Alpha | 26 | `LocalDateTime` | Origination TS | Origination timestamp |
| 13 | TRNX-PROC-TS | X(26) | Alpha | 26 | `LocalDateTime` | Processing TS | Processing timestamp |

**Key:** TRNX-CARD-NUM + TRNX-ID (composite) — keyed by card for statement grouping
**Note:** This is a re-keyed version of the Transaction entity, sorted by card number for statement generation.

---

## 16. Report Layout Structures

**Copybook:** `CVTRA07Y` | **Used by:** CBTRN03C (daily transaction report)

| Structure | Fields | Purpose |
|-----------|--------|---------|
| REPORT-NAME-HEADER | REPT-SHORT-NAME, REPT-LONG-NAME, REPT-DATE-HEADER, REPT-START-DATE, REPT-END-DATE | Report title and date range |
| TRANSACTION-DETAIL-REPORT | TRAN-REPORT-TRANS-ID, TRAN-REPORT-ACCOUNT-ID, TRAN-REPORT-TYPE-CD/DESC, TRAN-REPORT-CAT-CD/DESC, TRAN-REPORT-SOURCE, TRAN-REPORT-AMT | Detail line per transaction |
| TRANSACTION-HEADER-1/2 | Column headers and separator line | Report column headings |
| REPORT-PAGE-TOTALS | REPT-PAGE-TOTAL | Subtotal per page |
| REPORT-ACCOUNT-TOTALS | REPT-ACCOUNT-TOTAL | Subtotal per account |
| REPORT-GRAND-TOTALS | REPT-GRAND-TOTAL | Grand total for report |

---

## 17. Common / Shared Data Areas

### 17.1 Communication Area (COCOM01Y)

| Field | PIC | Description |
|-------|-----|-------------|
| CDEMO-FROM-TRANID | X(04) | Originating CICS transaction ID |
| CDEMO-FROM-PROGRAM | X(08) | Originating program name |
| CDEMO-TO-TRANID | X(04) | Target CICS transaction ID |
| CDEMO-TO-PROGRAM | X(08) | Target program name |
| CDEMO-PGM-CONTEXT | 9(01) | Program context flag |
| CDEMO-ACCT-ID | 9(11) | Selected account ID |
| CDEMO-CARD-NUM | X(16) | Selected card number |
| CDEMO-CUST-ID | 9(09) | Selected customer ID |
| CDEMO-LAST-MAP | X(07) | Last BMS map sent |
| CDEMO-LAST-MAPSET | X(07) | Last BMS mapset sent |

**Purpose:** Passed between CICS programs via EXEC CICS XCTL/RETURN — maintains navigation state.

### 17.2 Date Structures (CSDAT01Y)

| Field | PIC | Description |
|-------|-----|-------------|
| WS-CURDATE | X(08) | Current date (YYYYMMDD) |
| WS-CURTIME | X(08) | Current time (HH:MM:SS) |
| WS-CURDATE-MM/DD/YY | X(08) | Formatted date |

### 17.3 Messages (CSMSG01Y / CSMSG02Y)

| Field | PIC | Description |
|-------|-----|-------------|
| WS-MESSAGE | X(80) | General message area |
| ERRMSGO | X(78) | Error message output to screen |
| INFMSGO | X(78) | Info message output to screen |

### 17.4 Unused Record (UNUSED1Y)

| Field | PIC | Description |
|-------|-----|-------------|
| UNUSED-ID | X(08) | Deprecated user ID |
| UNUSED-FNAME | X(20) | Deprecated first name |
| UNUSED-LNAME | X(20) | Deprecated last name |
| UNUSED-PWD | X(08) | Deprecated password |
| UNUSED-TYPE | X(01) | Deprecated user type |

**Status:** DEPRECATED — Not referenced by any active program. Candidate for removal.

---

## 18. COBOL-to-Java Type Mapping Reference

| COBOL PIC | COBOL Type | Java Type | Notes |
|-----------|-----------|-----------|-------|
| `X(n)` | Alphanumeric | `String` | Fixed-length → trim on read |
| `9(n)` | Unsigned numeric | `int` / `long` | Use `long` for n > 9 |
| `S9(n)V99` | Signed decimal | `BigDecimal` | **Never use `double` for money** |
| `S9(n)` | Signed integer | `int` / `long` | |
| `X(10)` date | Date as string | `LocalDate` | Parse with DateTimeFormatter |
| `X(26)` timestamp | Timestamp as string | `LocalDateTime` | Parse with DateTimeFormatter |

### VSAM-to-RDBMS Mapping

| VSAM Concept | RDBMS Equivalent |
|---|---|
| KSDS (Key-Sequenced) | Table with PRIMARY KEY |
| Alternate Index (AIX) | Secondary INDEX / UNIQUE INDEX |
| KSDS Path (over AIX) | View or indexed query |
| ESDS (Entry-Sequenced) | Table with auto-increment PK |
| RRDS (Relative Record) | Table with integer PK |
| GDG (Generation Data Group) | Versioned backup table / audit trail |
| Sequential (PS) | Staging/import table or flat file |
