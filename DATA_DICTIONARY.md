# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** Copybook PIC clause analysis across 30 copybooks
> **Application:** CardDemo — Mainframe Credit Card Management System

---

## Table of Contents

1. [Overview](#overview)
2. [Entity: Account (CVACT01Y)](#entity-account-cvact01y)
3. [Entity: Card (CVACT02Y)](#entity-card-cvact02y)
4. [Entity: Card Cross-Reference (CVACT03Y)](#entity-card-cross-reference-cvact03y)
5. [Entity: Customer (CVCUS01Y)](#entity-customer-cvcus01y)
6. [Entity: Transaction (CVTRA05Y)](#entity-transaction-cvtra05y)
7. [Entity: Daily Transaction (CVTRA06Y)](#entity-daily-transaction-cvtra06y)
8. [Entity: Transaction Category Balance (CVTRA01Y)](#entity-transaction-category-balance-cvtra01y)
9. [Entity: Disclosure/Interest Rate Group (CVTRA02Y)](#entity-disclosureinterest-rate-group-cvtra02y)
10. [Entity: Transaction Type (CVTRA03Y)](#entity-transaction-type-cvtra03y)
11. [Entity: Transaction Category (CVTRA04Y)](#entity-transaction-category-cvtra04y)
12. [Entity: User Security (CSUSR01Y)](#entity-user-security-csusr01y)
13. [Entity: Statement Transaction (COSTM01)](#entity-statement-transaction-costm01)
14. [Entity: Export Record (CVEXPORT)](#entity-export-record-cvexport)
15. [Entity: Transaction Report Layout (CVTRA07Y)](#entity-transaction-report-layout-cvtra07y)
16. [Supporting Structures](#supporting-structures)
17. [VSAM File Catalog](#vsam-file-catalog)
18. [Modernization Notes](#modernization-notes)

---

## Overview

CardDemo's data model is centered around **five core business entities** stored as VSAM KSDS files, plus several reference/lookup entities. All record layouts are defined in COBOL copybooks using PIC clauses.

### Core Entities Relationship

```
Customer (500 bytes)
    │
    ├── 1:N ──► Account (300 bytes)
    │               │
    │               ├── 1:N ──► Card (150 bytes)
    │               │               │
    │               │               └── via Card Cross-Reference (50 bytes)
    │               │
    │               └── 1:N ──► Transaction (350 bytes)
    │
    └── 1:1 ──► User Security (80 bytes)  [for app login]
```

---

## Entity: Account (CVACT01Y)

**Copybook:** `CVACT01Y.cpy` | **Record Name:** `ACCOUNT-RECORD` | **Record Length:** 300 bytes
**VSAM File:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | **Key:** Account ID (KSDS)
**Business Purpose:** Master record for credit card accounts — stores balances, limits, cycle data, and status.

| # | Field Name | PIC Clause | Type | Size | Business Description | Constraints / Notes |
|---|-----------|------------|------|------|---------------------|-------------------|
| 1 | ACCT-ID | PIC 9(11) | Numeric | 11 | **Primary Key.** Unique account identifier | Not null, unique |
| 2 | ACCT-ACTIVE-STATUS | PIC X(01) | Alpha | 1 | Account status flag | 'Y' = Active, 'N' = Inactive |
| 3 | ACCT-CURR-BAL | PIC S9(10)V99 | Signed Decimal | 12 | Current account balance | Can be negative (credit) |
| 4 | ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12 | Maximum credit limit | Always positive |
| 5 | ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12 | Cash advance credit limit | Subset of credit limit |
| 6 | ACCT-OPEN-DATE | PIC X(10) | Alpha-Date | 10 | Date account was opened | Format: YYYY-MM-DD |
| 7 | ACCT-EXPIRAION-DATE | PIC X(10) | Alpha-Date | 10 | Account expiration date | Format: YYYY-MM-DD |
| 8 | ACCT-REISSUE-DATE | PIC X(10) | Alpha-Date | 10 | Last card reissue date | Format: YYYY-MM-DD |
| 9 | ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Signed Decimal | 12 | Credits in current billing cycle | Running total |
| 10 | ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 | Signed Decimal | 12 | Debits in current billing cycle | Running total |
| 11 | ACCT-ADDR-ZIP | PIC X(10) | Alpha | 10 | Account holder ZIP code | US format or extended |
| 12 | ACCT-GROUP-ID | PIC X(10) | Alpha | 10 | Account group / disclosure group | Links to DISCGRP for interest rates |
| 13 | FILLER | PIC X(178) | Alpha | 178 | Reserved / padding | Future expansion |

**Java Mapping:** `Account.java` (JPA Entity) → `accounts` table

---

## Entity: Card (CVACT02Y)

**Copybook:** `CVACT02Y.cpy` | **Record Name:** `CARD-RECORD` | **Record Length:** 150 bytes
**VSAM File:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` | **Key:** Card Number (KSDS)
**Business Purpose:** Physical credit card records — tracks card status, embossed name, and association to account.

| # | Field Name | PIC Clause | Type | Size | Business Description | Constraints / Notes |
|---|-----------|------------|------|------|---------------------|-------------------|
| 1 | CARD-NUM | PIC X(16) | Alpha | 16 | **Primary Key.** Credit card number | 16-digit PAN |
| 2 | CARD-ACCT-ID | PIC 9(11) | Numeric | 11 | Associated account ID | FK → ACCOUNT-RECORD |
| 3 | CARD-CVV-CD | PIC 9(03) | Numeric | 3 | Card verification value | 3-digit CVV |
| 4 | CARD-EMBOSSED-NAME | PIC X(50) | Alpha | 50 | Name printed on card | Cardholder name |
| 5 | CARD-EXPIRAION-DATE | PIC X(10) | Alpha-Date | 10 | Card expiration date | Format: YYYY-MM-DD |
| 6 | CARD-ACTIVE-STATUS | PIC X(01) | Alpha | 1 | Card active status | 'Y' = Active, 'N' = Inactive |
| 7 | FILLER | PIC X(59) | Alpha | 59 | Reserved / padding | Future expansion |

**Java Mapping:** `CreditCard.java` (JPA Entity) → `credit_cards` table

---

## Entity: Card Cross-Reference (CVACT03Y)

**Copybook:** `CVACT03Y.cpy` | **Record Name:** `CARD-XREF-RECORD` | **Record Length:** 50 bytes
**VSAM File:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | **Key:** Card Number (KSDS), Alt Key: Account ID (AIX)
**Business Purpose:** Links cards to customers and accounts — the bridge entity for card-account-customer relationships.

| # | Field Name | PIC Clause | Type | Size | Business Description | Constraints / Notes |
|---|-----------|------------|------|------|---------------------|-------------------|
| 1 | XREF-CARD-NUM | PIC X(16) | Alpha | 16 | **Primary Key.** Card number | FK → CARD-RECORD |
| 2 | XREF-CUST-ID | PIC 9(09) | Numeric | 9 | Customer ID | FK → CUSTOMER-RECORD |
| 3 | XREF-ACCT-ID | PIC 9(11) | Numeric | 11 | Account ID | FK → ACCOUNT-RECORD, AIX key |
| 4 | FILLER | PIC X(14) | Alpha | 14 | Reserved / padding | Future expansion |

**Java Mapping:** Resolved as relationships on `CreditCard.java` (ManyToOne → Account, Customer)

---

## Entity: Customer (CVCUS01Y)

**Copybook:** `CVCUS01Y.cpy` | **Record Name:** `CUSTOMER-RECORD` | **Record Length:** 500 bytes
**VSAM File:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` | **Key:** Customer ID (KSDS)
**Business Purpose:** Customer master — full demographic and contact information for cardholders.

| # | Field Name | PIC Clause | Type | Size | Business Description | Constraints / Notes |
|---|-----------|------------|------|------|---------------------|-------------------|
| 1 | CUST-ID | PIC 9(09) | Numeric | 9 | **Primary Key.** Unique customer ID | Not null, unique |
| 2 | CUST-FIRST-NAME | PIC X(25) | Alpha | 25 | Customer first name | |
| 3 | CUST-MIDDLE-NAME | PIC X(25) | Alpha | 25 | Customer middle name | Optional |
| 4 | CUST-LAST-NAME | PIC X(25) | Alpha | 25 | Customer last name | |
| 5 | CUST-ADDR-LINE-1 | PIC X(50) | Alpha | 50 | Address line 1 | Street address |
| 6 | CUST-ADDR-LINE-2 | PIC X(50) | Alpha | 50 | Address line 2 | Apt/Suite |
| 7 | CUST-ADDR-LINE-3 | PIC X(50) | Alpha | 50 | Address line 3 | Additional address |
| 8 | CUST-ADDR-STATE-CD | PIC X(02) | Alpha | 2 | State code | US 2-letter code |
| 9 | CUST-ADDR-COUNTRY-CD | PIC X(03) | Alpha | 3 | Country code | ISO 3-letter |
| 10 | CUST-ADDR-ZIP | PIC X(10) | Alpha | 10 | ZIP/postal code | US ZIP+4 format |
| 11 | CUST-PHONE-NUM-1 | PIC X(15) | Alpha | 15 | Primary phone | |
| 12 | CUST-PHONE-NUM-2 | PIC X(15) | Alpha | 15 | Secondary phone | Optional |
| 13 | CUST-SSN | PIC 9(09) | Numeric | 9 | Social Security Number | **PII — encrypt in Java** |
| 14 | CUST-GOVT-ISSUED-ID | PIC X(20) | Alpha | 20 | Government-issued ID | Driver license, passport, etc. |
| 15 | CUST-DOB-YYYY-MM-DD | PIC X(10) | Alpha-Date | 10 | Date of birth | Format: YYYY-MM-DD |
| 16 | CUST-EFT-ACCOUNT-ID | PIC 9(09) | Numeric | 9 | EFT/bank account for payments | External bank account |
| 17 | CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alpha | 1 | Primary cardholder indicator | 'Y' = Primary, 'N' = Authorized user |
| 18 | CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric | 3 | FICO credit score | Range: 300-850 |
| 19 | FILLER | PIC X(168) | Alpha | 168 | Reserved / padding | Future expansion |

**Java Mapping:** `Customer.java` (JPA Entity) → `customers` table
**PII Fields:** SSN, DOB, Government ID, Phone numbers — require encryption at rest

---

## Entity: Transaction (CVTRA05Y)

**Copybook:** `CVTRA05Y.cpy` | **Record Name:** `TRAN-RECORD` | **Record Length:** 350 bytes
**VSAM File:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | **Key:** Transaction ID, Alt Key: Processed Timestamp (AIX)
**Business Purpose:** Posted (processed) transaction records — the financial ledger of all card activity.

| # | Field Name | PIC Clause | Type | Size | Business Description | Constraints / Notes |
|---|-----------|------------|------|------|---------------------|-------------------|
| 1 | TRAN-ID | PIC X(16) | Alpha | 16 | **Primary Key.** Transaction identifier | System-generated |
| 2 | TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code | FK → TRAN-TYPE-RECORD |
| 3 | TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code | FK → TRAN-CAT-RECORD |
| 4 | TRAN-SOURCE | PIC X(10) | Alpha | 10 | Transaction source/channel | e.g., 'POS', 'ATM', 'ONLINE' |
| 5 | TRAN-DESC | PIC X(100) | Alpha | 100 | Transaction description | Free text |
| 6 | TRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11 | Transaction amount | Positive=debit, Negative=credit |
| 7 | TRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant identifier | |
| 8 | TRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant business name | |
| 9 | TRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant city | |
| 10 | TRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant ZIP code | |
| 11 | TRAN-CARD-NUM | PIC X(16) | Alpha | 16 | Card number used | FK → CARD-RECORD |
| 12 | TRAN-ORIG-TS | PIC X(26) | Alpha-Timestamp | 26 | Original transaction timestamp | DB2 format: YYYY-MM-DD-HH.MM.SS.NNNNNN |
| 13 | TRAN-PROC-TS | PIC X(26) | Alpha-Timestamp | 26 | Processing timestamp | When posted by batch |
| 14 | FILLER | PIC X(20) | Alpha | 20 | Reserved / padding | Future expansion |

**Java Mapping:** `Transaction.java` (JPA Entity) → `transactions` table

---

## Entity: Daily Transaction (CVTRA06Y)

**Copybook:** `CVTRA06Y.cpy` | **Record Name:** `DALYTRAN-RECORD` | **Record Length:** 350 bytes
**VSAM File:** `AWS.M2.CARDDEMO.DALYTRAN.PS` (Sequential)
**Business Purpose:** Unprocessed daily transaction feed — input to the batch posting cycle. Identical layout to Transaction but represents pre-posting state.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | DALYTRAN-ID | PIC X(16) | Alpha | 16 | **Key.** Transaction identifier |
| 2 | DALYTRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code |
| 3 | DALYTRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code |
| 4 | DALYTRAN-SOURCE | PIC X(10) | Alpha | 10 | Transaction source/channel |
| 5 | DALYTRAN-DESC | PIC X(100) | Alpha | 100 | Transaction description |
| 6 | DALYTRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11 | Transaction amount |
| 7 | DALYTRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant identifier |
| 8 | DALYTRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant name |
| 9 | DALYTRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant city |
| 10 | DALYTRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant ZIP code |
| 11 | DALYTRAN-CARD-NUM | PIC X(16) | Alpha | 16 | Card number used |
| 12 | DALYTRAN-ORIG-TS | PIC X(26) | Alpha-Timestamp | 26 | Original transaction timestamp |
| 13 | DALYTRAN-PROC-TS | PIC X(26) | Alpha-Timestamp | 26 | Processing timestamp |
| 14 | FILLER | PIC X(20) | Alpha | 20 | Reserved / padding |

**Java Mapping:** Same entity as Transaction — represents the staging/input state before posting

---

## Entity: Transaction Category Balance (CVTRA01Y)

**Copybook:** `CVTRA01Y.cpy` | **Record Name:** `TRAN-CAT-BAL-RECORD` | **Record Length:** 50 bytes
**VSAM File:** `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` | **Key:** Composite (Account ID + Type Code + Category Code)
**Business Purpose:** Running balance by account and transaction category — used for interest calculation.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | TRANCAT-ACCT-ID | PIC 9(11) | Numeric | 11 | Account ID (part of composite key) |
| 2 | TRANCAT-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code (part of key) |
| 3 | TRANCAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code (part of key) |
| 4 | TRAN-CAT-BAL | PIC S9(09)V99 | Signed Decimal | 11 | Category balance amount |
| 5 | FILLER | PIC X(22) | Alpha | 22 | Reserved / padding |

**Java Mapping:** `TransactionCategoryBalance.java` → `tran_cat_balances` table (composite PK)

---

## Entity: Disclosure/Interest Rate Group (CVTRA02Y)

**Copybook:** `CVTRA02Y.cpy` | **Record Name:** `DIS-GROUP-RECORD` | **Record Length:** 50 bytes
**VSAM File:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` | **Key:** Composite (Group ID + Type Code + Category Code)
**Business Purpose:** Interest rate configuration — maps account groups to interest rates by transaction type and category.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | DIS-ACCT-GROUP-ID | PIC X(10) | Alpha | 10 | Account group identifier (part of key) |
| 2 | DIS-TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code (part of key) |
| 3 | DIS-TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code (part of key) |
| 4 | DIS-INT-RATE | PIC S9(04)V99 | Signed Decimal | 6 | Annual interest rate (%) |
| 5 | FILLER | PIC X(28) | Alpha | 28 | Reserved / padding |

**Java Mapping:** `DisclosureGroup.java` → `disclosure_groups` table (composite PK)

---

## Entity: Transaction Type (CVTRA03Y)

**Copybook:** `CVTRA03Y.cpy` | **Record Name:** `TRAN-TYPE-RECORD` | **Record Length:** 60 bytes
**VSAM File:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` | **Key:** Transaction Type Code
**Business Purpose:** Reference table of transaction type codes and their descriptions.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | TRAN-TYPE | PIC X(02) | Alpha | 2 | **Primary Key.** Type code (e.g., 'SA'=Sale, 'CR'=Credit) |
| 2 | TRAN-TYPE-DESC | PIC X(50) | Alpha | 50 | Human-readable description |
| 3 | FILLER | PIC X(08) | Alpha | 8 | Reserved / padding |

**Java Mapping:** `TransactionType.java` → `transaction_types` table or Java enum

---

## Entity: Transaction Category (CVTRA04Y)

**Copybook:** `CVTRA04Y.cpy` | **Record Name:** `TRAN-CAT-RECORD` | **Record Length:** 60 bytes
**VSAM File:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` | **Key:** Composite (Type Code + Category Code)
**Business Purpose:** Reference table for transaction categories within each type.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code (part of key) |
| 2 | TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Category code (part of key) |
| 3 | TRAN-CAT-TYPE-DESC | PIC X(50) | Alpha | 50 | Category description |
| 4 | FILLER | PIC X(04) | Alpha | 4 | Reserved / padding |

**Java Mapping:** `TransactionCategory.java` → `transaction_categories` table

---

## Entity: User Security (CSUSR01Y)

**Copybook:** `CSUSR01Y.cpy` | **Record Name:** `SEC-USER-DATA` | **Record Length:** 80 bytes
**VSAM File:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` | **Key:** User ID
**Business Purpose:** Application user credentials and role assignments for CardDemo login.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | SEC-USR-ID | PIC X(08) | Alpha | 8 | **Primary Key.** User login ID (e.g., USER0001) |
| 2 | SEC-USR-FNAME | PIC X(20) | Alpha | 20 | User first name |
| 3 | SEC-USR-LNAME | PIC X(20) | Alpha | 20 | User last name |
| 4 | SEC-USR-PWD | PIC X(08) | Alpha | 8 | Password (**plaintext — must hash in Java**) |
| 5 | SEC-USR-TYPE | PIC X(01) | Alpha | 1 | User type/role | 'R' = Regular, 'A' = Admin |
| 6 | SEC-USR-FILLER | PIC X(23) | Alpha | 23 | Reserved / padding |

**Java Mapping:** `AppUser.java` → `app_users` table
**Security Note:** Passwords stored in plaintext in VSAM — must implement bcrypt/scrypt hashing in Java

---

## Entity: Statement Transaction (COSTM01)

**Copybook:** `COSTM01.CPY` | **Record Name:** `TRNX-RECORD` | **Record Length:** 350 bytes
**Business Purpose:** Altered transaction layout keyed by card number + transaction ID for statement generation. Reorders the Transaction record for card-first access.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | TRNX-CARD-NUM | PIC X(16) | Alpha | 16 | Card number (part of composite key) |
| 2 | TRNX-ID | PIC X(16) | Alpha | 16 | Transaction ID (part of composite key) |
| 3 | TRNX-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code |
| 4 | TRNX-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code |
| 5 | TRNX-SOURCE | PIC X(10) | Alpha | 10 | Transaction source |
| 6 | TRNX-DESC | PIC X(100) | Alpha | 100 | Transaction description |
| 7 | TRNX-AMT | PIC S9(09)V99 | Signed Decimal | 11 | Transaction amount |
| 8 | TRNX-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant ID |
| 9 | TRNX-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant name |
| 10 | TRNX-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant city |
| 11 | TRNX-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant ZIP |
| 12 | TRNX-ORIG-TS | PIC X(26) | Alpha-Timestamp | 26 | Original timestamp |
| 13 | TRNX-PROC-TS | PIC X(26) | Alpha-Timestamp | 26 | Processed timestamp |
| 14 | FILLER | PIC X(20) | Alpha | 20 | Reserved |

**Java Mapping:** Uses same `Transaction` entity with a different query/sort order (by card number)

---

## Entity: Export Record (CVEXPORT)

**Copybook:** `CVEXPORT.cpy` | **Record Name:** `EXPORT-RECORD` | **Record Length:** 500 bytes
**Business Purpose:** Multi-type export record for data migration. Uses a record-type indicator to distinguish customer, account, cross-reference, transaction, and card records in a single file.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | EXPORT-REC-TYPE | PIC X(01) | Alpha | 1 | Record type: C=Customer, A=Account, X=Xref, T=Transaction, D=Card |
| 2 | EXPORT-TIMESTAMP | PIC X(26) | Alpha-Timestamp | 26 | Export timestamp |
| 3 | EXPORT-SEQUENCE-NUM | PIC 9(09) | Numeric | 9 | Sequence number (primary key) |
| 4 | EXPORT-BRANCH-ID | PIC X(04) | Alpha | 4 | Branch identifier |
| 5 | EXPORT-REGION-CODE | PIC X(05) | Alpha | 5 | Region code |
| 6 | *Variant fields* | — | — | ~455 | Fields vary by EXPORT-REC-TYPE |

**Java Mapping:** Deserialized into respective entity classes based on record type discriminator

---

## Entity: Transaction Report Layout (CVTRA07Y)

**Copybook:** `CVTRA07Y.cpy` | **Record Names:** Multiple report structures
**Business Purpose:** Print layout definitions for the daily transaction report.

### Report Name Header
| Field | PIC Clause | Value/Description |
|-------|-----------|-------------------|
| REPT-SHORT-NAME | PIC X(38) | 'DALYREPT' |
| REPT-LONG-NAME | PIC X(41) | 'Daily Transaction Report' |
| REPT-DATE-HEADER | PIC X(12) | 'Date Range: ' |
| REPT-START-DATE | PIC X(10) | Report start date |
| REPT-END-DATE | PIC X(10) | Report end date |

### Transaction Detail Line
| Field | PIC Clause | Description |
|-------|-----------|-------------|
| TRAN-REPORT-TRANS-ID | PIC X(16) | Transaction ID |
| TRAN-REPORT-ACCOUNT-ID | PIC X(11) | Account ID |
| TRAN-REPORT-TYPE-CD | PIC X(02) | Type code |
| TRAN-REPORT-TYPE-DESC | PIC X(15) | Type description |
| TRAN-REPORT-CAT-CD | PIC 9(04) | Category code |
| TRAN-REPORT-CAT-DESC | PIC X(29) | Category description |
| TRAN-REPORT-SOURCE | PIC X(10) | Source channel |
| TRAN-REPORT-AMT | PIC -ZZZ,ZZZ,ZZZ.ZZ | Formatted amount |

### Report Totals
| Structure | Field | PIC Clause |
|-----------|-------|-----------|
| Page Totals | REPT-PAGE-TOTAL | PIC +ZZZ,ZZZ,ZZZ.ZZ |
| Account Totals | REPT-ACCOUNT-TOTAL | PIC +ZZZ,ZZZ,ZZZ.ZZ |
| Grand Totals | REPT-GRAND-TOTAL | PIC +ZZZ,ZZZ,ZZZ.ZZ |

---

## Supporting Structures

### Common Area / COMMAREA (COCOM01Y)

Inter-program communication area passed via CICS COMMAREA for screen-to-screen navigation.

| Key Fields | Description |
|-----------|-------------|
| CDEMO-FROM-PROGRAM | Source program name |
| CDEMO-TO-PROGRAM | Target program name |
| CDEMO-FROM-TRANID | Source CICS transaction ID |
| CDEMO-TO-TRANID | Target CICS transaction ID |
| CDEMO-PGM-CONTEXT | Program context flags |
| CDEMO-ACCT-ID | Current account in context |
| CDEMO-CARD-NUM | Current card in context |
| CDEMO-CUST-ID | Current customer in context |

### Menu Definitions (COMEN02Y, COADM02Y)

Maps numeric menu options to program names for XCTL dispatch.

### Message Areas (CSMSG01Y, CSMSG02Y)

Standard message display fields for informational/error messages on screens.

### Date Conversion (CODATECN)

Parameters for the COBDATFT assembler date formatting routine.

| Field | Description |
|-------|-------------|
| CODATECN-INP-DATE | Input date string |
| CODATECN-TYPE | Input date format type |
| CODATECN-OUTTYPE | Output date format type |
| CODATECN-0UT-DATE | Formatted output date |

---

## VSAM File Catalog

| # | VSAM Dataset Name | Org | Key | Rec Len | Copybook | Business Entity |
|---|------------------|-----|-----|---------|----------|-----------------|
| 1 | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | KSDS | ACCT-ID | 300 | CVACT01Y | Account |
| 2 | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | KSDS | CARD-NUM | 150 | CVACT02Y | Card |
| 3 | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | KSDS | XREF-CARD-NUM | 50 | CVACT03Y | Cross-Reference |
| 4 | AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX | AIX | XREF-ACCT-ID | — | — | (Alt index on Xref) |
| 5 | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | KSDS | CUST-ID | 500 | CVCUS01Y | Customer |
| 6 | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | KSDS | TRAN-ID | 350 | CVTRA05Y | Transaction |
| 7 | AWS.M2.CARDDEMO.TRANSACT.VSAM.AIX | AIX | TRAN-PROC-TS | — | — | (Alt index on Tran) |
| 8 | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | KSDS | SEC-USR-ID | 80 | CSUSR01Y | User Security |
| 9 | AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | KSDS | Composite | 50 | CVTRA01Y | Tran Cat Balance |
| 10 | AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | KSDS | Composite | 50 | CVTRA02Y | Disclosure Group |
| 11 | AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | KSDS | TRAN-TYPE | 60 | CVTRA03Y | Transaction Type |
| 12 | AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | KSDS | Composite | 60 | CVTRA04Y | Transaction Category |

### Sequential / GDG Files

| Dataset Pattern | Type | Description |
|----------------|------|-------------|
| AWS.M2.CARDDEMO.DALYTRAN.PS | PS | Daily transaction feed (input) |
| AWS.M2.CARDDEMO.TRANSACT.BKUP(*) | GDG | Transaction backup generations |
| AWS.M2.CARDDEMO.SYSTRAN(*) | GDG | System-generated transactions (interest) |
| AWS.M2.CARDDEMO.TRANSACT.COMBINED(*) | GDG | Combined transaction archive |
| AWS.M2.CARDDEMO.DALYREJS(*) | GDG | Daily rejects |
| AWS.M2.CARDDEMO.TRANREPT | GDG | Transaction reports |
| AWS.M2.CARDDEMO.EXPORT.DATA | PS | Export file for migration |

---

## Modernization Notes

### PIC Clause → Java Type Mapping

| COBOL PIC | Java Type | Notes |
|-----------|-----------|-------|
| PIC 9(n) | `long` or `int` | Use `long` for n > 9 |
| PIC X(n) | `String` | Trim trailing spaces |
| PIC S9(n)V99 | `BigDecimal` | **Always use BigDecimal for money** |
| PIC S9(n) COMP | `int` / `long` | Binary representation |
| PIC X(10) (dates) | `LocalDate` | Parse from YYYY-MM-DD |
| PIC X(26) (timestamps) | `LocalDateTime` | Parse from DB2 timestamp format |
| FILLER | — | Do not map; reserved space |

### Data Quality Considerations

- **Trailing spaces:** All PIC X fields are space-padded to full length — trim on import
- **Packed decimal:** COMP-3 fields (ACCT-CURR-CYC-DEBIT in some outputs) need special decoding
- **Date formats:** Mixed formats exist (YYYY-MM-DD in data, YYYYMMDD in some working storage)
- **Plaintext passwords:** CSUSR01Y stores passwords in clear text — hash immediately during migration
- **PII data:** Customer SSN, DOB, phone numbers need encryption at rest in the target system
