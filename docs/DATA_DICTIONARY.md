# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Source**: Copybook PIC clause analysis (`app/cpy/`)

---

## Overview

This document extracts every business data entity from the CardDemo copybooks into a business-friendly format. Each VSAM record layout is mapped to its logical entity, fields are translated from COBOL PIC clauses to business data types, and relationships between entities are documented.

---

## Entity Relationship Summary

```
Customer (1) ──── (N) Account (1) ──── (N) Card
     │                    │                   │
     │                    │                   │
     │              (N) Transaction ◄─────────┘ (via Card Cross-Ref)
     │                    │
     │              (N) Category Balance
     │                    │
     │              (1) Disclosure Group
     │
     └── (1) User Security Record
```

---

## 1. Account Master — `CVACT01Y` (300 bytes)

**VSAM Dataset**: `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`
**Key**: `ACCT-ID` (11 bytes, KSDS primary key)

| # | COBOL Field | PIC Clause | Business Name | Type | Length | Description |
|---|-------------|-----------|---------------|------|--------|-------------|
| 1 | `ACCT-ID` | `9(11)` | Account ID | Numeric | 11 | Unique account identifier |
| 2 | `ACCT-ACTIVE-STATUS` | `X(01)` | Active Status | Char | 1 | Account status flag (Y/N) |
| 3 | `ACCT-CURR-BAL` | `S9(10)V99` | Current Balance | Signed Decimal | 12.2 | Current account balance |
| 4 | `ACCT-CREDIT-LIMIT` | `S9(10)V99` | Credit Limit | Signed Decimal | 12.2 | Maximum credit limit |
| 5 | `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | Cash Credit Limit | Signed Decimal | 12.2 | Cash advance limit |
| 6 | `ACCT-OPEN-DATE` | `X(10)` | Open Date | Date String | 10 | Account opening date |
| 7 | `ACCT-EXPIRAION-DATE` | `X(10)` | Expiration Date | Date String | 10 | Account expiration date |
| 8 | `ACCT-REISSUE-DATE` | `X(10)` | Reissue Date | Date String | 10 | Last card reissue date |
| 9 | `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | Current Cycle Credit | Signed Decimal | 12.2 | Credits in current billing cycle |
| 10 | `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | Current Cycle Debit | Signed Decimal | 12.2 | Debits in current billing cycle |
| 11 | `ACCT-ADDR-ZIP` | `X(10)` | ZIP Code | Char | 10 | Account holder ZIP code |
| 12 | `ACCT-GROUP-ID` | `X(10)` | Group ID | Char | 10 | Account group for disclosure rates |
| 13 | FILLER | `X(178)` | — | — | 178 | Reserved space |

**Business Rules**:
- Balance = Prior Balance + Cycle Debits - Cycle Credits
- Credit limit constrains transaction approvals
- Group ID links to disclosure group interest rates

---

## 2. Card Data — `CVACT02Y` (150 bytes)

**VSAM Dataset**: `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`
**Key**: `CARD-NUM` (16 bytes, KSDS primary key)

| # | COBOL Field | PIC Clause | Business Name | Type | Length | Description |
|---|-------------|-----------|---------------|------|--------|-------------|
| 1 | `CARD-NUM` | `X(16)` | Card Number | Char | 16 | Credit card number |
| 2 | `CARD-ACCT-ID` | `9(11)` | Account ID | Numeric | 11 | Parent account reference |
| 3 | `CARD-CVV-CD` | `9(03)` | CVV Code | Numeric | 3 | Card verification value |
| 4 | `CARD-EMBOSSED-NAME` | `X(50)` | Embossed Name | Char | 50 | Name printed on card |
| 5 | `CARD-EXPIRAION-DATE` | `X(10)` | Expiration Date | Date String | 10 | Card expiration date |
| 6 | `CARD-ACTIVE-STATUS` | `X(01)` | Active Status | Char | 1 | Card status (Y/N) |
| 7 | FILLER | `X(59)` | — | — | 59 | Reserved space |

**Business Rules**:
- One account can have multiple cards
- Card number is the primary lookup key for transaction processing
- CVV is used for card-not-present validation

---

## 3. Card Cross-Reference — `CVACT03Y` (50 bytes)

**VSAM Dataset**: `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`
**Key**: `XREF-CARD-NUM` (16 bytes, KSDS primary key)

| # | COBOL Field | PIC Clause | Business Name | Type | Length | Description |
|---|-------------|-----------|---------------|------|--------|-------------|
| 1 | `XREF-CARD-NUM` | `X(16)` | Card Number | Char | 16 | Card number (key) |
| 2 | `XREF-CUST-ID` | `9(09)` | Customer ID | Numeric | 9 | Owning customer |
| 3 | `XREF-ACCT-ID` | `9(11)` | Account ID | Numeric | 11 | Owning account |
| 4 | FILLER | `X(14)` | — | — | 14 | Reserved space |

**Business Rules**:
- Links card numbers to both customer and account
- Primary lookup used during transaction posting to find account
- Alternate index on ACCT-ID enables account-to-card lookups

---

## 4. Customer Master — `CVCUS01Y` (500 bytes)

**VSAM Dataset**: `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`
**Key**: `CUST-ID` (9 bytes, KSDS primary key)

| # | COBOL Field | PIC Clause | Business Name | Type | Length | Description |
|---|-------------|-----------|---------------|------|--------|-------------|
| 1 | `CUST-ID` | `9(09)` | Customer ID | Numeric | 9 | Unique customer identifier |
| 2 | `CUST-FIRST-NAME` | `X(25)` | First Name | Char | 25 | Customer first name |
| 3 | `CUST-MIDDLE-NAME` | `X(25)` | Middle Name | Char | 25 | Customer middle name |
| 4 | `CUST-LAST-NAME` | `X(25)` | Last Name | Char | 25 | Customer last name |
| 5 | `CUST-ADDR-LINE-1` | `X(50)` | Address Line 1 | Char | 50 | Street address |
| 6 | `CUST-ADDR-LINE-2` | `X(50)` | Address Line 2 | Char | 50 | Address line 2 |
| 7 | `CUST-ADDR-LINE-3` | `X(50)` | Address Line 3 | Char | 50 | Address line 3 |
| 8 | `CUST-ADDR-STATE-CD` | `X(02)` | State Code | Char | 2 | US state code |
| 9 | `CUST-ADDR-COUNTRY-CD` | `X(03)` | Country Code | Char | 3 | Country code |
| 10 | `CUST-ADDR-ZIP` | `X(10)` | ZIP Code | Char | 10 | Postal code |
| 11 | `CUST-PHONE-NUM-1` | `X(15)` | Phone Number 1 | Char | 15 | Primary phone |
| 12 | `CUST-PHONE-NUM-2` | `X(15)` | Phone Number 2 | Char | 15 | Secondary phone |
| 13 | `CUST-SSN` | `9(09)` | SSN | Numeric | 9 | Social Security Number |
| 14 | `CUST-GOVT-ISSUED-ID` | `X(20)` | Government ID | Char | 20 | Government-issued ID number |
| 15 | `CUST-DOB-YYYYMMDD` | `X(10)` | Date of Birth | Date String | 10 | Customer DOB |
| 16 | `CUST-EFT-ACCOUNT-ID` | `X(10)` | EFT Account ID | Char | 10 | Electronic funds transfer account |
| 17 | `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | Primary Holder Flag | Char | 1 | Primary card holder indicator |
| 18 | `CUST-FICO-CREDIT-SCORE` | `9(03)` | FICO Score | Numeric | 3 | Credit score |
| 19 | FILLER | `X(168)` | — | — | 168 | Reserved space |

**Business Rules**:
- SSN is PII — requires masking in modernized system
- FICO score used for credit decisions
- EFT account used for bill payment/ACH
- Primary holder indicator distinguishes authorized users from account owners

---

## 5. Transaction Record — `CVTRA05Y` (350 bytes)

**VSAM Dataset**: `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`
**Key**: `TRAN-ID` (16 bytes)

| # | COBOL Field | PIC Clause | Business Name | Type | Length | Description |
|---|-------------|-----------|---------------|------|--------|-------------|
| 1 | `TRAN-ID` | `X(16)` | Transaction ID | Char | 16 | Unique transaction identifier |
| 2 | `TRAN-TYPE-CD` | `X(02)` | Transaction Type | Char | 2 | Type code (SA, PR, etc.) |
| 3 | `TRAN-CAT-CD` | `9(04)` | Category Code | Numeric | 4 | Transaction category |
| 4 | `TRAN-SOURCE` | `X(10)` | Transaction Source | Char | 10 | Origin (POS, ATM, Online) |
| 5 | `TRAN-DESC` | `X(100)` | Description | Char | 100 | Transaction description |
| 6 | `TRAN-AMT` | `S9(09)V99` | Amount | Signed Decimal | 11.2 | Transaction amount |
| 7 | `TRAN-MERCHANT-ID` | `9(09)` | Merchant ID | Numeric | 9 | Merchant identifier |
| 8 | `TRAN-MERCHANT-NAME` | `X(50)` | Merchant Name | Char | 50 | Merchant name |
| 9 | `TRAN-MERCHANT-CITY` | `X(50)` | Merchant City | Char | 50 | Merchant city |
| 10 | `TRAN-MERCHANT-ZIP` | `X(10)` | Merchant ZIP | Char | 10 | Merchant postal code |
| 11 | `TRAN-CARD-NUM` | `X(16)` | Card Number | Char | 16 | Card used for transaction |
| 12 | `TRAN-ORIG-TS` | `X(26)` | Origination Timestamp | Timestamp | 26 | When transaction originated |
| 13 | `TRAN-PROC-TS` | `X(26)` | Processing Timestamp | Timestamp | 26 | When transaction was processed |
| 14 | FILLER | `X(20)` | — | — | 20 | Reserved space |

**Business Rules**:
- Posted via CBTRN02C batch (daily cycle)
- Linked to account via card cross-reference
- Amount is signed: positive = debit, negative = credit
- Timestamps in ISO-like format (26 chars)

---

## 6. Daily Transaction — `CVTRA06Y` (350 bytes)

**VSAM Dataset**: `AWS.M2.CARDDEMO.DALYTRAN.PS` (sequential)
**Key**: None (sequential file)

| # | COBOL Field | PIC Clause | Business Name | Type | Length | Description |
|---|-------------|-----------|---------------|------|--------|-------------|
| 1 | `DALYTRAN-ID` | `X(16)` | Transaction ID | Char | 16 | Unique ID |
| 2 | `DALYTRAN-TYPE-CD` | `X(02)` | Type Code | Char | 2 | Transaction type |
| 3 | `DALYTRAN-CAT-CD` | `9(04)` | Category Code | Numeric | 4 | Category |
| 4 | `DALYTRAN-SOURCE` | `X(10)` | Source | Char | 10 | Origin |
| 5 | `DALYTRAN-DESC` | `X(100)` | Description | Char | 100 | Description |
| 6 | `DALYTRAN-AMT` | `S9(09)V99` | Amount | Signed Decimal | 11.2 | Amount |
| 7 | `DALYTRAN-MERCHANT-ID` | `9(09)` | Merchant ID | Numeric | 9 | Merchant |
| 8 | `DALYTRAN-MERCHANT-NAME` | `X(50)` | Merchant Name | Char | 50 | Merchant name |
| 9 | `DALYTRAN-MERCHANT-CITY` | `X(50)` | Merchant City | Char | 50 | Merchant city |
| 10 | `DALYTRAN-MERCHANT-ZIP` | `X(10)` | Merchant ZIP | Char | 10 | Merchant ZIP |
| 11 | `DALYTRAN-CARD-NUM` | `X(16)` | Card Number | Char | 16 | Card used |
| 12 | `DALYTRAN-ORIG-TS` | `X(26)` | Origination Timestamp | Timestamp | 26 | When originated |
| 13 | `DALYTRAN-PROC-TS` | `X(26)` | Processing Timestamp | Timestamp | 26 | When processed |
| 14 | FILLER | `X(20)` | — | — | 20 | Reserved |

**Business Rules**:
- Identical layout to CVTRA05Y but for daily inbound feed
- Input to POSTTRAN (CBTRN02C) batch cycle
- Validated before posting to master transaction file
- Rejects written to DALYREJS GDG

---

## 7. Transaction Category Balance — `CVTRA01Y` (60 bytes)

**VSAM Dataset**: `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS`
**Key**: `TRANCAT-ACCT-ID` + `TRANCAT-TYPE-CD` + `TRANCAT-CD` (compound)

| # | COBOL Field | PIC Clause | Business Name | Type | Length | Description |
|---|-------------|-----------|---------------|------|--------|-------------|
| 1 | `TRANCAT-ACCT-ID` | `9(11)` | Account ID | Numeric | 11 | Account |
| 2 | `TRANCAT-TYPE-CD` | `X(02)` | Type Code | Char | 2 | Transaction type |
| 3 | `TRANCAT-CD` | `9(04)` | Category Code | Numeric | 4 | Category |
| 4 | `TRAN-CAT-BAL` | `S9(09)V99` | Category Balance | Signed Decimal | 11.2 | Running balance for category |
| 5 | FILLER | `X(22)` | — | — | 22 | Reserved |

**Business Rules**:
- Tracks balance per account per transaction type/category
- Updated during transaction posting (CBTRN02C)
- Used in interest calculation (CBACT04C) to apply category-specific rates

---

## 8. Disclosure Group — `CVTRA02Y` (50 bytes)

**VSAM Dataset**: `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`
**Key**: `DIS-ACCT-GROUP-ID` + `DIS-TRAN-TYPE-CD` + `DIS-TRAN-CAT-CD` (compound)

| # | COBOL Field | PIC Clause | Business Name | Type | Length | Description |
|---|-------------|-----------|---------------|------|--------|-------------|
| 1 | `DIS-ACCT-GROUP-ID` | `X(10)` | Account Group ID | Char | 10 | Disclosure group |
| 2 | `DIS-TRAN-TYPE-CD` | `X(02)` | Transaction Type | Char | 2 | Transaction type |
| 3 | `DIS-TRAN-CAT-CD` | `9(04)` | Category Code | Numeric | 4 | Category |
| 4 | `DIS-INT-RATE` | `S9(04)V99` | Interest Rate | Signed Decimal | 6.2 | APR for this group/type/category |
| 5 | FILLER | `X(28)` | — | — | 28 | Reserved |

**Business Rules**:
- Provides interest rate by account group, transaction type, and category
- Looked up by CBACT04C during interest calculation
- Account group assigned at account level (ACCT-GROUP-ID)

---

## 9. Transaction Type — `CVTRA03Y` (60 bytes)

**VSAM Dataset**: `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`
**Key**: `TRAN-TYPE` (2 bytes)

| # | COBOL Field | PIC Clause | Business Name | Type | Length | Description |
|---|-------------|-----------|---------------|------|--------|-------------|
| 1 | `TRAN-TYPE` | `X(02)` | Type Code | Char | 2 | Transaction type code |
| 2 | `TRAN-TYPE-DESC` | `X(50)` | Description | Char | 50 | Human-readable description |
| 3 | FILLER | `X(08)` | — | — | 8 | Reserved |

**Known Type Codes**: SA (Sale), PR (Purchase), RT (Return), CR (Credit), PA (Payment)

---

## 10. Transaction Category — `CVTRA04Y` (60 bytes)

**VSAM Dataset**: `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`
**Key**: `TRAN-TYPE-CD` + `TRAN-CAT-CD` (compound)

| # | COBOL Field | PIC Clause | Business Name | Type | Length | Description |
|---|-------------|-----------|---------------|------|--------|-------------|
| 1 | `TRAN-TYPE-CD` | `X(02)` | Type Code | Char | 2 | Parent transaction type |
| 2 | `TRAN-CAT-CD` | `9(04)` | Category Code | Numeric | 4 | Category code |
| 3 | `TRAN-CAT-TYPE-DESC` | `X(50)` | Description | Char | 50 | Category description |
| 4 | FILLER | `X(04)` | — | — | 4 | Reserved |

**Business Rules**:
- Categories subdivide transaction types (e.g., Purchase > Grocery, Gas, etc.)
- Used in reporting and interest rate lookups

---

## 11. User Security — `CSUSR01Y`

**VSAM Dataset**: `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`
**Key**: `SEC-USR-ID` (8 bytes)

| # | COBOL Field | PIC Clause | Business Name | Type | Length | Description |
|---|-------------|-----------|---------------|------|--------|-------------|
| 1 | `SEC-USR-ID` | `X(08)` | User ID | Char | 8 | Login user ID |
| 2 | `SEC-USR-FNAME` | `X(20)` | First Name | Char | 20 | User first name |
| 3 | `SEC-USR-LNAME` | `X(20)` | Last Name | Char | 20 | User last name |
| 4 | `SEC-USR-PWD` | `X(08)` | Password | Char | 8 | User password (plaintext) |
| 5 | `SEC-USR-TYPE` | `X(01)` | User Type | Char | 1 | A=Admin, R=Regular |
| 6 | FILLER | `X(23)` | — | — | 23 | Reserved |

**Business Rules**:
- Passwords stored in plaintext (modernization should add hashing)
- User type controls menu routing: Admin (COADM01C) vs Regular (COMEN01C)
- Managed via COUSR00C-COUSR03C screens

---

## 12. Transaction Report Layout — `CVTRA07Y`

**Not a VSAM record** — report formatting structure used by CBTRN03C.

| # | COBOL Field | PIC Clause | Business Name | Type | Length |
|---|-------------|-----------|---------------|------|--------|
| 1 | `REPT-SHORT-NAME` | `X(38)` | Report Name (short) | Char | 38 |
| 2 | `REPT-LONG-NAME` | `X(41)` | Report Name (long) | Char | 41 |
| 3 | `REPT-START-DATE` | `X(10)` | Start Date | Char | 10 |
| 4 | `REPT-END-DATE` | `X(10)` | End Date | Char | 10 |
| 5 | `TRAN-REPORT-TRANS-ID` | `X(16)` | Transaction ID | Char | 16 |
| 6 | `TRAN-REPORT-ACCOUNT-ID` | `X(11)` | Account ID | Char | 11 |
| 7 | `TRAN-REPORT-TYPE-CD` | `X(02)` | Type Code | Char | 2 |
| 8 | `TRAN-REPORT-TYPE-DESC` | `X(15)` | Type Description | Char | 15 |
| 9 | `TRAN-REPORT-CAT-CD` | `9(04)` | Category Code | Numeric | 4 |
| 10 | `TRAN-REPORT-CAT-DESC` | `X(29)` | Category Description | Char | 29 |
| 11 | `TRAN-REPORT-SOURCE` | `X(10)` | Source | Char | 10 |
| 12 | `TRAN-REPORT-AMT` | `-ZZZ,ZZZ,ZZZ.ZZ` | Amount | Edited Decimal | 16 |
| 13 | `REPT-PAGE-TOTAL` | `+ZZZ,ZZZ,ZZZ.ZZ` | Page Total | Edited Decimal | 16 |
| 14 | `REPT-ACCOUNT-TOTAL` | `+ZZZ,ZZZ,ZZZ.ZZ` | Account Total | Edited Decimal | 16 |
| 15 | `REPT-GRAND-TOTAL` | `+ZZZ,ZZZ,ZZZ.ZZ` | Grand Total | Edited Decimal | 16 |

---

## 13. Statement Transaction Layout — `COSTM01` (350+ bytes)

**Used by**: CBSTM03A (Statement Generation)
**Key**: `TRNX-CARD-NUM` (16) + `TRNX-ID` (16) — compound key

| # | COBOL Field | PIC Clause | Business Name | Type | Length |
|---|-------------|-----------|---------------|------|--------|
| 1 | `TRNX-CARD-NUM` | `X(16)` | Card Number | Char | 16 |
| 2 | `TRNX-ID` | `X(16)` | Transaction ID | Char | 16 |
| 3 | `TRNX-TYPE-CD` | `X(02)` | Type Code | Char | 2 |
| 4 | `TRNX-CAT-CD` | `9(04)` | Category Code | Numeric | 4 |
| 5 | `TRNX-SOURCE` | `X(10)` | Source | Char | 10 |
| 6 | `TRNX-DESC` | `X(100)` | Description | Char | 100 |
| 7 | `TRNX-AMT` | `S9(09)V99` | Amount | Signed Decimal | 11.2 |
| 8 | `TRNX-MERCHANT-ID` | `9(09)` | Merchant ID | Numeric | 9 |
| 9 | `TRNX-MERCHANT-NAME` | `X(50)` | Merchant Name | Char | 50 |
| 10 | `TRNX-MERCHANT-CITY` | `X(50)` | Merchant City | Char | 50 |
| 11 | `TRNX-MERCHANT-ZIP` | `X(10)` | Merchant ZIP | Char | 10 |
| 12 | `TRNX-ORIG-TS` | `X(26)` | Origination Timestamp | Timestamp | 26 |
| 13 | `TRNX-PROC-TS` | `X(26)` | Processing Timestamp | Timestamp | 26 |
| 14 | FILLER | `X(20)` | — | — | 20 | Reserved |

**Business Rules**:
- Re-keyed version of transaction record (card+tran compound key)
- Used in statement generation to group transactions by card number

---

## 14. Export/Import Record — `CVEXPORT`

**Used by**: CBEXPORT, CBIMPORT
Multi-record layout for branch migration data exchange.

| # | Record Type | Content | Layout |
|---|-------------|---------|--------|
| 1 | CUST | Customer data | Maps to CVCUS01Y fields |
| 2 | ACCT | Account data | Maps to CVACT01Y fields |
| 3 | XREF | Cross-reference | Maps to CVACT03Y fields |
| 4 | TRAN | Transaction data | Maps to CVTRA05Y fields |
| 5 | CARD | Card data | Maps to CVACT02Y fields |

---

## 15. Common Communication Area — `COCOM01Y`

Passed between CICS programs via COMMAREA to maintain session state.

| # | Field Group | Purpose |
|---|-------------|---------|
| 1 | Program name fields | Track current/previous program |
| 2 | Transaction ID | Current CICS transaction |
| 3 | User session data | Signed-in user ID, type |
| 4 | Navigation state | Previous screen, return program |
| 5 | Selection context | Selected account/card/transaction |
| 6 | Message area | Error/info message passing |

---

## VSAM-to-Relational Mapping (Modernization Reference)

| VSAM Dataset | Suggested Table | Primary Key | Foreign Keys |
|---|---|---|---|
| ACCTDATA.VSAM.KSDS | `accounts` | `account_id` | — |
| CARDDATA.VSAM.KSDS | `cards` | `card_number` | `account_id → accounts` |
| CARDXREF.VSAM.KSDS | `card_xref` | `card_number` | `customer_id → customers`, `account_id → accounts` |
| CUSTDATA.VSAM.KSDS | `customers` | `customer_id` | — |
| TRANSACT.VSAM.KSDS | `transactions` | `transaction_id` | `card_number → cards` |
| DALYTRAN.PS | `daily_transactions` (staging) | `transaction_id` | — |
| TCATBALF.VSAM.KSDS | `category_balances` | (`account_id`, `type_cd`, `cat_cd`) | `account_id → accounts` |
| DISCGRP.VSAM.KSDS | `disclosure_groups` | (`group_id`, `type_cd`, `cat_cd`) | — |
| TRANTYPE.VSAM.KSDS | `transaction_types` | `type_code` | — |
| TRANCATG.VSAM.KSDS | `transaction_categories` | (`type_code`, `cat_code`) | `type_code → transaction_types` |
| USRSEC.VSAM.KSDS | `users` | `user_id` | — |

---

## PII & Sensitive Data Flags

| Entity | Field | Sensitivity | Modernization Note |
|--------|-------|-------------|-------------------|
| Customer | `CUST-SSN` | **PII - Critical** | Must be encrypted at rest, masked in UI |
| Customer | `CUST-DOB-YYYYMMDD` | **PII** | Date of birth |
| Customer | `CUST-PHONE-NUM-*` | **PII** | Phone numbers |
| Customer | `CUST-ADDR-*` | **PII** | Physical address |
| Card | `CARD-NUM` | **PCI-DSS** | Must be tokenized or encrypted |
| Card | `CARD-CVV-CD` | **PCI-DSS Critical** | Never store post-authorization |
| User | `SEC-USR-PWD` | **Critical** | Plaintext — must hash (bcrypt/scrypt) |
| Transaction | `TRAN-CARD-NUM` | **PCI-DSS** | Mask in logs and reports |
