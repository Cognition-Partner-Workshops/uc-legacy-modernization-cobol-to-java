# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management System)
> **Source:** Copybook PIC clause analysis from `app/cpy/` and optional modules

---

## Executive Summary

The CardDemo application manages **9 primary VSAM data stores** holding credit card business data. This dictionary extracts every business entity from the copybook record layouts, translating COBOL PIC clauses into business-friendly field descriptions, data types, and sizes.

---

## 1. Account Master (`CVACT01Y`) -- ACCTDATA.VSAM.KSDS

**Record Length:** 300 bytes | **Key:** Account ID (11 digits) | **VSAM Type:** KSDS

| # | Field Name | COBOL PIC | Business Name | Data Type | Length | Description |
|---|-----------|-----------|--------------|-----------|--------|-------------|
| 1 | ACCT-ID | 9(11) | Account ID | Numeric | 11 | Unique account identifier |
| 2 | ACCT-ACTIVE-STATUS | X(01) | Account Status | Text | 1 | Active/Inactive flag (Y/N) |
| 3 | ACCT-CURR-BAL | S9(10)V99 | Current Balance | Signed Decimal | 12.2 | Current account balance (dollars.cents) |
| 4 | ACCT-CREDIT-LIMIT | S9(10)V99 | Credit Limit | Signed Decimal | 12.2 | Maximum credit allowed |
| 5 | ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | Cash Advance Limit | Signed Decimal | 12.2 | Maximum cash advance allowed |
| 6 | ACCT-OPEN-DATE | X(10) | Account Open Date | Text (Date) | 10 | Date account was opened |
| 7 | ACCT-EXPIRAION-DATE | X(10) | Expiration Date | Text (Date) | 10 | Account expiration date |
| 8 | ACCT-REISSUE-DATE | X(10) | Reissue Date | Text (Date) | 10 | Last card reissue date |
| 9 | ACCT-CURR-CYC-CREDIT | S9(10)V99 | Current Cycle Credits | Signed Decimal | 12.2 | Credits in current billing cycle |
| 10 | ACCT-CURR-CYC-DEBIT | S9(10)V99 | Current Cycle Debits | Signed Decimal | 12.2 | Debits in current billing cycle |
| 11 | ACCT-ADDR-ZIP | X(10) | ZIP Code | Text | 10 | Account holder ZIP/postal code |
| 12 | ACCT-GROUP-ID | X(10) | Account Group | Text | 10 | Account group for disclosure/interest |
| 13 | FILLER | X(178) | Reserved | Filler | 178 | Reserved for future use |

**Business Rules:**
- Account is the central entity; linked to cards via cross-reference file
- Balance = Credits - Debits accumulated over billing cycles
- Credit limit constrains total balance; cash advance limit is a sub-limit

---

## 2. Card Data (`CVACT02Y`) -- CARDDATA.VSAM.KSDS

**Record Length:** 150 bytes | **Key:** Card Number (16 digits) | **VSAM Type:** KSDS

| # | Field Name | COBOL PIC | Business Name | Data Type | Length | Description |
|---|-----------|-----------|--------------|-----------|--------|-------------|
| 1 | CARD-NUM | X(16) | Card Number | Text | 16 | 16-digit credit card number (PAN) |
| 2 | CARD-ACCT-ID | 9(11) | Account ID | Numeric | 11 | Parent account reference |
| 3 | CARD-CVV-CD | 9(03) | CVV Code | Numeric | 3 | Card verification value |
| 4 | CARD-EMBOSSED-NAME | X(50) | Cardholder Name | Text | 50 | Name embossed on card |
| 5 | CARD-EXPIRAION-DATE | X(10) | Expiration Date | Text (Date) | 10 | Card expiration date |
| 6 | CARD-ACTIVE-STATUS | X(01) | Card Status | Text | 1 | Active/Inactive flag |
| 7 | FILLER | X(59) | Reserved | Filler | 59 | Reserved for future use |

**Business Rules:**
- Multiple cards can belong to one account (1:N relationship)
- Card number is the primary identifier for transaction processing
- CVV is stored for validation (security consideration for modernization)

---

## 3. Card Cross-Reference (`CVACT03Y`) -- CARDXREF.VSAM.KSDS

**Record Length:** 50 bytes | **Key:** Card Number (16 digits) | **VSAM Type:** KSDS with AIX on Account ID

| # | Field Name | COBOL PIC | Business Name | Data Type | Length | Description |
|---|-----------|-----------|--------------|-----------|--------|-------------|
| 1 | XREF-CARD-NUM | X(16) | Card Number | Text | 16 | Card number (primary key) |
| 2 | XREF-ACCT-ID | 9(11) | Account ID | Numeric | 11 | Linked account (alternate index key) |
| 3 | FILLER | X(23) | Reserved | Filler | 23 | Reserved for future use |

**Business Rules:**
- Provides fast card-to-account and account-to-card lookups
- Alternate index (AIX) on ACCT-ID enables reverse lookup (non-unique: multiple cards per account)
- Critical for transaction posting (card number → account) and statement generation

---

## 4. Customer Master (`CVCUS01Y`) -- CUSTDATA.VSAM.KSDS

**Record Length:** 500 bytes | **Key:** Customer ID (9 digits) | **VSAM Type:** KSDS

| # | Field Name | COBOL PIC | Business Name | Data Type | Length | Description |
|---|-----------|-----------|--------------|-----------|--------|-------------|
| 1 | CUST-ID | 9(09) | Customer ID | Numeric | 9 | Unique customer identifier |
| 2 | CUST-FIRST-NAME | X(25) | First Name | Text | 25 | Customer first name |
| 3 | CUST-MIDDLE-NAME | X(25) | Middle Name | Text | 25 | Customer middle name |
| 4 | CUST-LAST-NAME | X(25) | Last Name | Text | 25 | Customer last name |
| 5 | CUST-ADDR-LINE-1 | X(50) | Address Line 1 | Text | 50 | Street address line 1 |
| 6 | CUST-ADDR-LINE-2 | X(50) | Address Line 2 | Text | 50 | Street address line 2 |
| 7 | CUST-ADDR-LINE-3 | X(50) | Address Line 3 | Text | 50 | Additional address line |
| 8 | CUST-ADDR-STATE-CD | X(02) | State Code | Text | 2 | US state code |
| 9 | CUST-ADDR-COUNTRY-CD | X(03) | Country Code | Text | 3 | Country code |
| 10 | CUST-ADDR-ZIP | X(10) | ZIP Code | Text | 10 | ZIP/postal code |
| 11 | CUST-PHONE-NUM-1 | X(15) | Primary Phone | Text | 15 | Primary phone number |
| 12 | CUST-PHONE-NUM-2 | X(15) | Secondary Phone | Text | 15 | Secondary phone number |
| 13 | CUST-SSN | 9(09) | SSN | Numeric | 9 | Social Security Number (PII) |
| 14 | CUST-GOVT-ISSUED-ID | X(20) | Government ID | Text | 20 | Government-issued identification |
| 15 | CUST-DOB-YYYYMMDD | X(10) | Date of Birth | Text (Date) | 10 | Customer date of birth |
| 16 | CUST-EFT-ACCOUNT-ID | X(10) | EFT Account | Text | 10 | Electronic funds transfer account |
| 17 | CUST-PRI-CARD-HOLDER-IND | X(01) | Primary Cardholder | Text | 1 | Primary cardholder indicator |
| 18 | CUST-FICO-CREDIT-SCORE | 9(03) | FICO Score | Numeric | 3 | Credit score |
| 19 | FILLER | X(168) | Reserved | Filler | 168 | Reserved for future use |

**Business Rules:**
- Customer is the person; linked to accounts which hold cards
- SSN and DOB are PII fields requiring encryption in modernized system
- FICO score used for credit decisions and risk assessment
- EFT account enables electronic bill payments

---

## 5. Transaction Master (`CVTRA05Y`) -- TRANSACT.VSAM.KSDS

**Record Length:** 350 bytes | **Key:** Transaction ID (16 chars) | **VSAM Type:** KSDS

| # | Field Name | COBOL PIC | Business Name | Data Type | Length | Description |
|---|-----------|-----------|--------------|-----------|--------|-------------|
| 1 | TRAN-ID | X(16) | Transaction ID | Text | 16 | Unique transaction identifier |
| 2 | TRAN-TYPE-CD | X(02) | Transaction Type | Text | 2 | Type code (e.g., PR=Purchase, CR=Credit) |
| 3 | TRAN-CAT-CD | 9(04) | Category Code | Numeric | 4 | Transaction category (e.g., 5001=Retail) |
| 4 | TRAN-SOURCE | X(10) | Source | Text | 10 | Transaction origination source |
| 5 | TRAN-DESC | X(100) | Description | Text | 100 | Transaction description |
| 6 | TRAN-AMT | S9(09)V99 | Amount | Signed Decimal | 11.2 | Transaction amount (dollars.cents) |
| 7 | TRAN-MERCHANT-ID | 9(09) | Merchant ID | Numeric | 9 | Merchant identifier |
| 8 | TRAN-MERCHANT-NAME | X(50) | Merchant Name | Text | 50 | Merchant business name |
| 9 | TRAN-MERCHANT-CITY | X(50) | Merchant City | Text | 50 | Merchant city |
| 10 | TRAN-MERCHANT-ZIP | X(10) | Merchant ZIP | Text | 10 | Merchant ZIP code |
| 11 | TRAN-CARD-NUM | X(16) | Card Number | Text | 16 | Card used for transaction |
| 12 | TRAN-ORIG-TS | X(26) | Origination Timestamp | Text (Timestamp) | 26 | When transaction was originated |
| 13 | TRAN-PROC-TS | X(26) | Processing Timestamp | Text (Timestamp) | 26 | When transaction was processed |
| 14 | FILLER | X(20) | Reserved | Filler | 20 | Reserved for future use |

**Business Rules:**
- Central transaction ledger for the entire card system
- Linked to cards via TRAN-CARD-NUM → XREF → Account
- Type + Category codes determine interest rate calculations
- Daily transactions (CVTRA06Y) are identical in structure but represent unposted daily entries

---

## 6. Daily Transaction (`CVTRA06Y`) -- DALYTRAN.PS

**Record Length:** 350 bytes | **Key:** Sequential | **File Type:** Sequential PS

Same structure as Transaction Master (CVTRA05Y) with field prefix `DALYTRAN-` instead of `TRAN-`. Represents transactions awaiting posting to the master file.

---

## 7. Transaction Category Balance (`CVTRA01Y`) -- TCATBALF.VSAM.KSDS

**Record Length:** 50 bytes | **Key:** Account ID + Type + Category (17 bytes) | **VSAM Type:** KSDS

| # | Field Name | COBOL PIC | Business Name | Data Type | Length | Description |
|---|-----------|-----------|--------------|-----------|--------|-------------|
| 1 | TRANCAT-ACCT-ID | 9(11) | Account ID | Numeric | 11 | Account identifier |
| 2 | TRANCAT-TYPE-CD | X(02) | Transaction Type | Text | 2 | Transaction type code |
| 3 | TRANCAT-CD | 9(04) | Category Code | Numeric | 4 | Transaction category code |
| 4 | TRAN-CAT-BAL | S9(09)V99 | Category Balance | Signed Decimal | 11.2 | Accumulated balance for this category |
| 5 | FILLER | X(22) | Reserved | Filler | 22 | Reserved for future use |

**Business Rules:**
- Tracks balance per account per transaction type per category
- Used by interest calculation (CBACT04C) to apply different rates per category
- Updated during transaction posting

---

## 8. Disclosure Group / Interest Rate (`CVTRA02Y`) -- DISCGRP.VSAM.KSDS

**Record Length:** 50 bytes | **Key:** Group ID + Type + Category (16 bytes) | **VSAM Type:** KSDS

| # | Field Name | COBOL PIC | Business Name | Data Type | Length | Description |
|---|-----------|-----------|--------------|-----------|--------|-------------|
| 1 | DIS-ACCT-GROUP-ID | X(10) | Account Group ID | Text | 10 | Account group identifier |
| 2 | DIS-TRAN-TYPE-CD | X(02) | Transaction Type | Text | 2 | Transaction type code |
| 3 | DIS-TRAN-CAT-CD | 9(04) | Category Code | Numeric | 4 | Transaction category code |
| 4 | DIS-INT-RATE | S9(04)V99 | Interest Rate | Signed Decimal | 6.2 | Annual interest rate percentage |
| 5 | FILLER | X(28) | Reserved | Filler | 28 | Reserved for future use |

**Business Rules:**
- Defines interest rates by account group + transaction type + category
- Account group (from ACCT-GROUP-ID) links accounts to their disclosure terms
- Different rates for purchases vs. cash advances vs. balance transfers

---

## 9. Transaction Type (`CVTRA03Y`) -- TRANTYPE.VSAM.KSDS

**Record Length:** 60 bytes | **Key:** Type Code (2 chars) | **VSAM Type:** KSDS

| # | Field Name | COBOL PIC | Business Name | Data Type | Length | Description |
|---|-----------|-----------|--------------|-----------|--------|-------------|
| 1 | TRAN-TYPE | X(02) | Type Code | Text | 2 | Transaction type code |
| 2 | TRAN-TYPE-DESC | X(50) | Type Description | Text | 50 | Human-readable type description |
| 3 | FILLER | X(08) | Reserved | Filler | 8 | Reserved for future use |

---

## 10. Transaction Category (`CVTRA04Y`) -- TRANCATG.VSAM.KSDS

**Record Length:** 60 bytes | **Key:** Type + Category (6 bytes) | **VSAM Type:** KSDS

| # | Field Name | COBOL PIC | Business Name | Data Type | Length | Description |
|---|-----------|-----------|--------------|-----------|--------|-------------|
| 1 | TRAN-TYPE-CD | X(02) | Type Code | Text | 2 | Transaction type code |
| 2 | TRAN-CAT-CD | 9(04) | Category Code | Numeric | 4 | Category code |
| 3 | TRAN-CAT-TYPE-DESC | X(50) | Category Description | Text | 50 | Human-readable category description |
| 4 | FILLER | X(04) | Reserved | Filler | 4 | Reserved for future use |

---

## 11. User Security (`CSUSR01Y`) -- USRSEC.VSAM.KSDS

**Record Length:** 80 bytes | **Key:** User ID (8 chars) | **VSAM Type:** KSDS

| # | Field Name | COBOL PIC | Business Name | Data Type | Length | Description |
|---|-----------|-----------|--------------|-----------|--------|-------------|
| 1 | SEC-USR-ID | X(08) | User ID | Text | 8 | Login user identifier |
| 2 | SEC-USR-FNAME | X(20) | First Name | Text | 20 | User first name |
| 3 | SEC-USR-LNAME | X(20) | Last Name | Text | 20 | User last name |
| 4 | SEC-USR-PWD | X(08) | Password | Text | 8 | Login password (plaintext) |
| 5 | SEC-USR-TYPE | X(01) | User Type | Text | 1 | R=Regular, A=Admin |
| 6 | FILLER | X(23) | Reserved | Filler | 23 | Reserved for future use |

**Business Rules:**
- Controls application access; two roles: Admin and Regular
- Admin users access user management and admin menu
- Passwords stored in plaintext (critical security concern for modernization)

---

## 12. Statement Transaction Layout (`COSTM01`) -- Internal

**Record Length:** 350 bytes | **Key:** Card Number + Transaction ID (32 bytes composite)

| # | Field Name | COBOL PIC | Business Name | Data Type | Length | Description |
|---|-----------|-----------|--------------|-----------|--------|-------------|
| 1 | TRNX-CARD-NUM | X(16) | Card Number | Text | 16 | Card number (part of sort key) |
| 2 | TRNX-ID | X(16) | Transaction ID | Text | 16 | Transaction ID |
| 3 | TRNX-TYPE-CD | X(02) | Type Code | Text | 2 | Transaction type |
| 4 | TRNX-CAT-CD | 9(04) | Category Code | Numeric | 4 | Transaction category |
| 5 | TRNX-SOURCE | X(10) | Source | Text | 10 | Transaction source |
| 6 | TRNX-DESC | X(100) | Description | Text | 100 | Transaction description |
| 7 | TRNX-AMT | S9(09)V99 | Amount | Signed Decimal | 11.2 | Transaction amount |
| 8 | TRNX-MERCHANT-ID | 9(09) | Merchant ID | Numeric | 9 | Merchant identifier |
| 9 | TRNX-MERCHANT-NAME | X(50) | Merchant Name | Text | 50 | Merchant name |
| 10 | TRNX-MERCHANT-CITY | X(50) | Merchant City | Text | 50 | Merchant city |
| 11 | TRNX-MERCHANT-ZIP | X(10) | Merchant ZIP | Text | 10 | Merchant ZIP code |
| 12 | TRNX-ORIG-TS | X(26) | Origination Timestamp | Text | 26 | Origination timestamp |
| 13 | TRNX-PROC-TS | X(26) | Processing Timestamp | Text | 26 | Processing timestamp |
| 14 | FILLER | X(20) | Reserved | Filler | 20 | Reserved for future use |

**Business Rules:**
- Re-keyed version of the transaction record for statement generation
- Sorted by Card Number + Transaction ID to group transactions per card
- Used exclusively by CBSTM03A/B during the CREASTMT batch job

---

## 13. Export Record (`CVEXPORT`) -- EXPORT.DATA

Composite record used by CBEXPORT/CBIMPORT for data migration. Contains sub-record types:

| # | Field Name | COBOL PIC | Business Name | Data Type | Length | Description |
|---|-----------|-----------|--------------|-----------|--------|-------------|
| 1 | EXP-RECORD-TYPE | X(01) | Record Type | Text | 1 | C=Customer, A=Account, R=Card/Xref, T=Transaction |
| 2 | EXP-CUST-* | (varies) | Customer Fields | Mixed | ~500 | Full customer record |
| 3 | EXP-ACCT-* | (varies) | Account Fields | Mixed | ~300 | Full account record |
| 4 | EXP-CARD-* | (varies) | Card Fields | Mixed | ~150 | Card data including embossed name |
| 5 | EXP-TRAN-* | (varies) | Transaction Fields | Mixed | ~350 | Full transaction record |

---

## 14. Communication Area (`COCOM01Y`) -- In-Memory

Passed between CICS programs via COMMAREA:

| # | Field Name | COBOL PIC | Business Name | Data Type | Length | Description |
|---|-----------|-----------|--------------|-----------|--------|-------------|
| 1 | CDEMO-FROM-TRANID | X(04) | Source Transaction | Text | 4 | Calling CICS transaction ID |
| 2 | CDEMO-FROM-PROGRAM | X(08) | Source Program | Text | 8 | Calling program name |
| 3 | CDEMO-TO-TRANID | X(04) | Target Transaction | Text | 4 | Target CICS transaction ID |
| 4 | CDEMO-TO-PROGRAM | X(08) | Target Program | Text | 8 | Target program name |
| 5 | CDEMO-PGM-REENTER | X(01) | Re-enter Flag | Text | 1 | Program re-entry indicator |
| 6 | CDEMO-USR-ID | X(08) | User ID | Text | 8 | Logged-in user ID |
| 7 | CDEMO-USR-TYP | X(01) | User Type | Text | 1 | R=Regular, A=Admin |
| 8 | CDEMO-PGM-CONTEXT | X(256) | Program Context | Text | 256 | Context data passed between programs |

**Business Rules:**
- Every online CICS program reads/writes this COMMAREA
- Navigation flow is controlled by FROM/TO transaction and program fields
- User type determines menu access (Admin vs. Regular)

---

## 15. Entity Relationship Summary

```
Customer (CVCUS01Y)
    │
    ├── 1:N ──► Account (CVACT01Y)
    │               │
    │               ├── 1:N ──► Card (CVACT02Y)
    │               │              │
    │               │              └── mapped via ──► Cross-Reference (CVACT03Y)
    │               │
    │               ├── 1:N ──► Transaction (CVTRA05Y / CVTRA06Y)
    │               │
    │               ├── 1:N ──► Category Balance (CVTRA01Y)
    │               │
    │               └── N:1 ──► Account Group ──► Disclosure/Interest (CVTRA02Y)
    │
    └── (no direct FK)

Transaction Type (CVTRA03Y) ──► 1:N ──► Transaction Category (CVTRA04Y)

User Security (CSUSR01Y) ── independent auth entity
```

---

## 16. Modernization Mapping Guide

| COBOL/VSAM Entity | Suggested Java Entity | Suggested DB Table | Notes |
|-------------------|----------------------|-------------------|-------|
| CVACT01Y (Account) | `Account.java` | `accounts` | Add audit columns, UUID PK |
| CVACT02Y (Card) | `Card.java` | `cards` | Encrypt PAN, add FK to accounts |
| CVACT03Y (Xref) | -- (eliminate) | FK in `cards` table | Replace with relational FK |
| CVCUS01Y (Customer) | `Customer.java` | `customers` | Encrypt SSN/DOB, add FK to accounts |
| CVTRA05Y (Transaction) | `Transaction.java` | `transactions` | Add FK to cards, index on timestamps |
| CVTRA06Y (Daily Tran) | `DailyTransaction.java` | `daily_transactions` | Or use status column in transactions |
| CVTRA01Y (Cat Balance) | `CategoryBalance.java` | `category_balances` | Composite FK |
| CVTRA02Y (Disclosure) | `DisclosureGroup.java` | `disclosure_groups` | Configuration table |
| CVTRA03Y (Tran Type) | `TransactionType.java` | `transaction_types` | Lookup/reference table |
| CVTRA04Y (Tran Cat) | `TransactionCategory.java` | `transaction_categories` | Lookup/reference table |
| CSUSR01Y (User Sec) | `User.java` | `users` | Hash passwords, add roles table |
| COCOM01Y (COMMAREA) | Session/Context object | HTTP session / JWT | Replace with stateless auth |
