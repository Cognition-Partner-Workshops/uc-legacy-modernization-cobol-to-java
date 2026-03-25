# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** Copybook PIC clause analysis across `app/cpy/` and optional modules
> **Storage:** VSAM KSDS (Keyed Sequential Data Sets) on z/OS

---

## Table of Contents

1. [Entity-Relationship Overview](#entity-relationship-overview)
2. [Account Entity (CVACT01Y)](#account-entity)
3. [Credit Card Entity (CVACT02Y)](#credit-card-entity)
4. [Customer Entity (CVCUS01Y)](#customer-entity)
5. [Card Cross-Reference Entity (CVACT03Y)](#card-cross-reference-entity)
6. [Transaction Entity (CVTRA05Y)](#transaction-entity)
7. [Daily Transaction Entity (CVTRA06Y)](#daily-transaction-entity)
8. [Transaction Category Balance (CVTRA01Y)](#transaction-category-balance)
9. [Disclosure Group (CVTRA02Y)](#disclosure-group)
10. [Transaction Type (CVTRA03Y)](#transaction-type)
11. [Transaction Category (CVTRA04Y)](#transaction-category)
12. [User Security Record (CSUSR01Y)](#user-security-record)
13. [Statement Transaction (COSTM01)](#statement-transaction)
14. [Export Record (CVEXPORT)](#export-record)
15. [Report Structures (CVTRA07Y)](#report-structures)
16. [Card Working Area (CVCRD01Y)](#card-working-area)
17. [Application Communication Area (COCOM01Y)](#application-communication-area)
18. [VSAM File Catalog](#vsam-file-catalog)

---

## Entity-Relationship Overview

```
                    ┌──────────────┐
                    │   Customer   │
                    │  (CVCUS01Y)  │
                    │  500 bytes   │
                    └──────┬───────┘
                           │ 1:N
                           ▼
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  Card X-Ref  │◄───│   Account    │───►│  Disclosure   │
│  (CVACT03Y)  │    │  (CVACT01Y)  │    │   Group       │
│   50 bytes   │    │  300 bytes   │    │  (CVTRA02Y)   │
└──────┬───────┘    └──────┬───────┘    └───────────────┘
       │                   │ 1:N
       │ 1:1               ▼
       ▼            ┌──────────────┐    ┌──────────────┐
┌──────────────┐    │ Transaction  │───►│  Tran Type   │
│  Credit Card │    │  (CVTRA05Y)  │    │  (CVTRA03Y)  │
│  (CVACT02Y)  │    │  350 bytes   │    │   60 bytes   │
│  150 bytes   │    └──────┬───────┘    └──────────────┘
└──────────────┘           │
                           ▼
                    ┌──────────────┐    ┌──────────────┐
                    │ Tran Cat Bal │    │  Tran Cat    │
                    │  (CVTRA01Y)  │    │  (CVTRA04Y)  │
                    │   50 bytes   │    │   60 bytes   │
                    └──────────────┘    └──────────────┘
```

---

## Account Entity

**Copybook:** `CVACT01Y` | **Record Length:** 300 bytes | **VSAM File:** `ACCTDAT`
**Key:** Account ID (11 digits) | **Business Purpose:** Stores credit card account master data

| Field Name | PIC Clause | Type | Size | Business Name | Description |
|---|---|---|---|---|---|
| ACCT-ID | 9(11) | Numeric | 11 | Account Number | Unique account identifier |
| ACCT-ACTIVE-STATUS | X(01) | Alpha | 1 | Account Status | Active status flag (Y/N) |
| ACCT-CURR-BAL | S9(10)V99 | Signed Decimal | 12 | Current Balance | Current outstanding balance |
| ACCT-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12 | Credit Limit | Maximum credit limit |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12 | Cash Advance Limit | Maximum cash advance limit |
| ACCT-OPEN-DATE | X(10) | Alpha | 10 | Account Open Date | Date account was opened (YYYY-MM-DD) |
| ACCT-EXPIRAION-DATE | X(10) | Alpha | 10 | Expiration Date | Account expiration date |
| ACCT-REISSUE-DATE | X(10) | Alpha | 10 | Reissue Date | Last card reissue date |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | Signed Decimal | 12 | Cycle Credits | Current billing cycle credits |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | Signed Decimal | 12 | Cycle Debits | Current billing cycle debits |
| ACCT-GROUP-ID | X(10) | Alpha | 10 | Account Group | Disclosure/rate group assignment |
| ACCT-CUST-ID | 9(09) | Numeric | 9 | Customer ID | FK to Customer entity |
| ACCT-FICO-CREDIT-SCORE | 9(03) | Numeric | 3 | FICO Score | Credit score (300-850) |
| FILLER | X(168) | — | 168 | Reserved | Padding to 300-byte record |

**Business Rules:**
- One customer may have multiple accounts
- ACCT-ACTIVE-STATUS must be 'Y' or 'N'
- FICO score range: 300-850
- Balance = previous balance + cycle debits - cycle credits - payments

---

## Credit Card Entity

**Copybook:** `CVACT02Y` | **Record Length:** 150 bytes | **VSAM File:** `CARDDAT`
**Key:** Card Number (16 digits) | **Business Purpose:** Stores physical credit card details

| Field Name | PIC Clause | Type | Size | Business Name | Description |
|---|---|---|---|---|---|
| CARD-NUM | X(16) | Alpha | 16 | Card Number | 16-digit card number (PAN) |
| CARD-ACCT-ID | 9(11) | Numeric | 11 | Account Number | FK to Account entity |
| CARD-CVV-CD | 9(03) | Numeric | 3 | CVV Code | Card verification value |
| CARD-EMBOSSED-NAME | X(50) | Alpha | 50 | Embossed Name | Name printed on card |
| CARD-EXPIRAION-DATE | X(10) | Alpha | 10 | Expiration Date | Card expiration (YYYY-MM-DD) |
| CARD-ACTIVE-STATUS | X(01) | Alpha | 1 | Card Status | Active/Inactive flag |
| FILLER | X(59) | — | 59 | Reserved | Padding to 150-byte record |

**Business Rules:**
- Multiple cards can belong to one account
- Card number is the primary key for VSAM KSDS
- CVV code is a 3-digit numeric value
- Card status: 'Y' = active, 'N' = inactive

---

## Customer Entity

**Copybook:** `CVCUS01Y` | **Record Length:** 500 bytes | **VSAM File:** `CUSTDAT`
**Key:** Customer ID (9 digits) | **Business Purpose:** Stores cardholder personal information

| Field Name | PIC Clause | Type | Size | Business Name | Description |
|---|---|---|---|---|---|
| CUST-ID | 9(09) | Numeric | 9 | Customer ID | Unique customer identifier |
| CUST-FIRST-NAME | X(25) | Alpha | 25 | First Name | Customer first name |
| CUST-MIDDLE-NAME | X(25) | Alpha | 25 | Middle Name | Customer middle name |
| CUST-LAST-NAME | X(25) | Alpha | 25 | Last Name | Customer last name |
| CUST-ADDR-LINE-1 | X(50) | Alpha | 50 | Address Line 1 | Street address |
| CUST-ADDR-LINE-2 | X(50) | Alpha | 50 | Address Line 2 | Suite/apartment |
| CUST-ADDR-LINE-3 | X(50) | Alpha | 50 | Address Line 3 | Additional address |
| CUST-ADDR-STATE-CD | X(02) | Alpha | 2 | State Code | US state abbreviation |
| CUST-ADDR-COUNTRY-CD | X(03) | Alpha | 3 | Country Code | ISO country code |
| CUST-ADDR-ZIP | X(10) | Alpha | 10 | ZIP Code | Postal/ZIP code |
| CUST-PHONE-NUM-1 | X(15) | Alpha | 15 | Phone Number 1 | Primary phone ((NNN)NNN-NNNN) |
| CUST-PHONE-NUM-2 | X(15) | Alpha | 15 | Phone Number 2 | Secondary phone |
| CUST-SSN | 9(09) | Numeric | 9 | SSN | Social Security Number |
| CUST-GOVT-ISSUED-ID | X(20) | Alpha | 20 | Government ID | Government-issued identifier |
| CUST-DOB-YYYY-MM-DD | X(10) | Alpha | 10 | Date of Birth | Date of birth (YYYY-MM-DD) |
| CUST-EFT-ACCOUNT-ID | X(10) | Alpha | 10 | EFT Account | Electronic funds transfer account |
| CUST-PRI-CARD-HOLDER-IND | X(01) | Alpha | 1 | Primary Cardholder | Y/N primary cardholder flag |
| CUST-FICO-CREDIT-SCORE | 9(03) | Numeric | 3 | FICO Score | Customer credit score |
| FILLER | X(168) | — | 168 | Reserved | Padding to 500-byte record |

**Business Rules:**
- Contains PII (SSN, DOB, addresses) — sensitive data
- Phone format: (NNN)NNN-NNNN
- SSN validation: parts 1 cannot be 000, 666, or 900-999
- One customer can have multiple accounts

---

## Card Cross-Reference Entity

**Copybook:** `CVACT03Y` | **Record Length:** 50 bytes | **VSAM File:** `CARDXREF` (with AIX `CXACAIX`)
**Key:** Card Number (16 digits) | **Business Purpose:** Links cards to accounts and customers

| Field Name | PIC Clause | Type | Size | Business Name | Description |
|---|---|---|---|---|---|
| XREF-CARD-NUM | X(16) | Alpha | 16 | Card Number | FK to Credit Card entity |
| XREF-CUST-ID | 9(09) | Numeric | 9 | Customer ID | FK to Customer entity |
| XREF-ACCT-ID | 9(11) | Numeric | 11 | Account Number | FK to Account entity |
| FILLER | X(14) | — | 14 | Reserved | Padding to 50-byte record |

**Business Rules:**
- Three-way cross-reference linking cards, accounts, and customers
- Alternate index (CXACAIX) allows lookup by account ID
- Central to all account/card lookup operations

---

## Transaction Entity

**Copybook:** `CVTRA05Y` | **Record Length:** 350 bytes | **VSAM File:** `TRANSACT` (with AIX on processed timestamp)
**Key:** Transaction ID (16 chars) | **Business Purpose:** Stores processed/posted transactions

| Field Name | PIC Clause | Type | Size | Business Name | Description |
|---|---|---|---|---|---|
| TRAN-ID | X(16) | Alpha | 16 | Transaction ID | Unique transaction identifier |
| TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction Type | FK to Transaction Type (e.g., 01=Purchase) |
| TRAN-CAT-CD | 9(04) | Numeric | 4 | Category Code | FK to Transaction Category |
| TRAN-SOURCE | X(10) | Alpha | 10 | Transaction Source | Origin of transaction |
| TRAN-DESC | X(100) | Alpha | 100 | Description | Transaction narrative |
| TRAN-AMT | S9(09)V99 | Signed Decimal | 11 | Amount | Transaction amount (positive=debit) |
| TRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant ID | Merchant identifier |
| TRAN-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant Name | Name of merchant |
| TRAN-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant City | City of merchant |
| TRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP | Postal code of merchant |
| TRAN-CARD-NUM | X(16) | Alpha | 16 | Card Number | FK to Credit Card entity |
| TRAN-ORIG-TS | X(26) | Alpha | 26 | Origination Timestamp | When transaction was initiated |
| TRAN-PROC-TS | X(26) | Alpha | 26 | Processing Timestamp | When transaction was posted |
| FILLER | X(20) | — | 20 | Reserved | Padding to 350-byte record |

**Business Rules:**
- Transactions are created by daily posting (CBTRN02C)
- Alternate index exists on TRAN-PROC-TS for time-based queries
- Amount is signed: positive = debit, negative = credit
- Card number links back to account via cross-reference

---

## Daily Transaction Entity

**Copybook:** `CVTRA06Y` | **Record Length:** 350 bytes | **VSAM File:** `DALYTRAN`
**Key:** Transaction ID (16 chars) | **Business Purpose:** Incoming daily transactions awaiting posting

| Field Name | PIC Clause | Type | Size | Business Name | Description |
|---|---|---|---|---|---|
| DALYTRAN-ID | X(16) | Alpha | 16 | Transaction ID | Daily transaction identifier |
| DALYTRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction Type | Type code |
| DALYTRAN-CAT-CD | 9(04) | Numeric | 4 | Category Code | Category code |
| DALYTRAN-SOURCE | X(10) | Alpha | 10 | Source | Transaction origin |
| DALYTRAN-DESC | X(100) | Alpha | 100 | Description | Transaction narrative |
| DALYTRAN-AMT | S9(09)V99 | Signed Decimal | 11 | Amount | Transaction amount |
| DALYTRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant ID | Merchant identifier |
| DALYTRAN-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant Name | Merchant name |
| DALYTRAN-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant City | Merchant city |
| DALYTRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP | Merchant postal code |
| DALYTRAN-CARD-NUM | X(16) | Alpha | 16 | Card Number | Card used |
| DALYTRAN-ORIG-TS | X(26) | Alpha | 26 | Origination Timestamp | When initiated |
| DALYTRAN-PROC-TS | X(26) | Alpha | 26 | Processing Timestamp | When processed |
| FILLER | X(20) | — | 20 | Reserved | Padding to 350 bytes |

**Business Rules:**
- Mirror structure of Transaction entity (CVTRA05Y)
- Input file for batch posting process (CBTRN02C)
- Rejected transactions are written to DALYREJS file
- Cleared after successful posting cycle

---

## Transaction Category Balance

**Copybook:** `CVTRA01Y` | **Record Length:** 50 bytes | **VSAM File:** `TCATBALF`
**Key:** Account ID + Type Code + Category Code (17 bytes)
**Business Purpose:** Running balance by transaction category per account

| Field Name | PIC Clause | Type | Size | Business Name | Description |
|---|---|---|---|---|---|
| TRANCAT-ACCT-ID | 9(11) | Numeric | 11 | Account Number | FK to Account entity |
| TRANCAT-TYPE-CD | X(02) | Alpha | 2 | Transaction Type | Type code |
| TRANCAT-CD | 9(04) | Numeric | 4 | Category Code | Category code |
| TRAN-CAT-BAL | S9(09)V99 | Signed Decimal | 11 | Category Balance | Running balance for this category |
| FILLER | X(22) | — | 22 | Reserved | Padding to 50 bytes |

---

## Disclosure Group

**Copybook:** `CVTRA02Y` | **Record Length:** 50 bytes | **VSAM File:** `DISCGRP`
**Key:** Group ID + Type Code + Category Code (16 bytes)
**Business Purpose:** Interest rate definitions by account group and transaction type

| Field Name | PIC Clause | Type | Size | Business Name | Description |
|---|---|---|---|---|---|
| DIS-ACCT-GROUP-ID | X(10) | Alpha | 10 | Account Group ID | Rate group identifier |
| DIS-TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction Type | Type code |
| DIS-TRAN-CAT-CD | 9(04) | Numeric | 4 | Category Code | Category code |
| DIS-INT-RATE | S9(04)V99 | Signed Decimal | 6 | Interest Rate | Annual interest rate (%) |
| FILLER | X(28) | — | 28 | Reserved | Padding to 50 bytes |

---

## Transaction Type

**Copybook:** `CVTRA03Y` | **Record Length:** 60 bytes | **VSAM File:** `TRANTYPE`
**Key:** Transaction Type Code (2 bytes)
**Business Purpose:** Lookup table for transaction type descriptions

| Field Name | PIC Clause | Type | Size | Business Name | Description |
|---|---|---|---|---|---|
| TRAN-TYPE | X(02) | Alpha | 2 | Type Code | Transaction type identifier |
| TRAN-TYPE-DESC | X(50) | Alpha | 50 | Type Description | Human-readable description |
| FILLER | X(08) | — | 8 | Reserved | Padding to 60 bytes |

---

## Transaction Category

**Copybook:** `CVTRA04Y` | **Record Length:** 60 bytes | **VSAM File:** `TRANCATG`
**Key:** Type Code + Category Code (6 bytes)
**Business Purpose:** Sub-classification of transactions within types

| Field Name | PIC Clause | Type | Size | Business Name | Description |
|---|---|---|---|---|---|
| TRAN-TYPE-CD | X(02) | Alpha | 2 | Type Code | FK to Transaction Type |
| TRAN-CAT-CD | 9(04) | Numeric | 4 | Category Code | Category identifier |
| TRAN-CAT-TYPE-DESC | X(50) | Alpha | 50 | Category Description | Human-readable description |
| FILLER | X(04) | — | 4 | Reserved | Padding to 60 bytes |

---

## User Security Record

**Copybook:** `CSUSR01Y` | **VSAM File:** `USRSEC`
**Key:** User ID (8 bytes)
**Business Purpose:** Authentication and authorization for application users

| Field Name | PIC Clause | Type | Size | Business Name | Description |
|---|---|---|---|---|---|
| SEC-USR-ID | X(08) | Alpha | 8 | User ID | Login username |
| SEC-USR-FNAME | X(20) | Alpha | 20 | First Name | User first name |
| SEC-USR-LNAME | X(20) | Alpha | 20 | Last Name | User last name |
| SEC-USR-PWD | X(08) | Alpha | 8 | Password | Login password (plain text) |
| SEC-USR-TYPE | X(01) | Alpha | 1 | User Type | 'A' = Admin, 'U' = Regular User |
| SEC-USR-FILLER | X(23) | — | 23 | Reserved | Padding |

**Business Rules:**
- User type 'A' grants access to Admin Menu (COADM01C)
- User type 'U' restricted to Regular User Menu (COMEN01C)
- Default accounts: ADMIN001/PASSWORD (admin), USER0001/PASSWORD (user)
- Passwords stored in plain text (modernization should add encryption)

---

## Statement Transaction

**Copybook:** `COSTM01` | **Record Length:** ~350 bytes
**Business Purpose:** Reformatted transaction record for statement generation, keyed by card + transaction ID

| Field Name | PIC Clause | Type | Size | Business Name | Description |
|---|---|---|---|---|---|
| TRNX-CARD-NUM | X(16) | Alpha | 16 | Card Number | Card number (first part of key) |
| TRNX-ID | X(16) | Alpha | 16 | Transaction ID | Transaction ID (second part of key) |
| TRNX-TYPE-CD | X(02) | Alpha | 2 | Type Code | Transaction type |
| TRNX-CAT-CD | 9(04) | Numeric | 4 | Category Code | Transaction category |
| TRNX-SOURCE | X(10) | Alpha | 10 | Source | Transaction source |
| TRNX-DESC | X(100) | Alpha | 100 | Description | Transaction narrative |
| TRNX-AMT | S9(09)V99 | Signed Decimal | 11 | Amount | Transaction amount |
| TRNX-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant ID | Merchant identifier |
| TRNX-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant Name | Merchant name |
| TRNX-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant City | Merchant city |
| TRNX-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP | Merchant postal code |
| TRNX-ORIG-TS | X(26) | Alpha | 26 | Origination Timestamp | When initiated |
| TRNX-PROC-TS | X(26) | Alpha | 26 | Processing Timestamp | When posted |
| FILLER | X(20) | — | 20 | Reserved | Padding |

**Note:** Same structure as CVTRA05Y but with a composite key (card + tran ID) for statement ordering.

---

## Export Record

**Copybook:** `CVEXPORT` | **Business Purpose:** Unified record layout for data export/import operations

The export record is a tagged union containing sub-records for each entity type:

| Tag | Entity | Key Fields | Description |
|---|---|---|---|
| CUST | Customer | EXP-CUST-ID (9 digits) | Full customer record with PII |
| ACCT | Account | EXP-ACCT-ID (11 digits) | Full account record with balances |
| CARD | Card | EXP-CARD-NUM (16 chars) | Full card record |
| XREF | Cross-Ref | EXP-XREF-CARD-NUM (16 chars) | Card-to-account-to-customer mapping |
| TRAN | Transaction | EXP-TRAN-ID (16 chars) | Full transaction record |

Each sub-record mirrors the FILLER-padded structure of its corresponding VSAM entity. The export file uses a record-type indicator prefix.

---

## Report Structures

**Copybook:** `CVTRA07Y` | **Business Purpose:** Column headers, detail lines, and total lines for the Daily Transaction Report

| Structure | Description |
|---|---|
| REPORT-NAME-HEADER | Report name, date range |
| TRANSACTION-DETAIL-REPORT | Detail line: Tran ID, Account, Type, Category, Source, Amount |
| TRANSACTION-HEADER-1/2 | Column headers and separator line |
| REPORT-PAGE-TOTALS | Page subtotal line |
| REPORT-ACCOUNT-TOTALS | Account subtotal line |
| REPORT-GRAND-TOTALS | Grand total line |

---

## Card Working Area

**Copybook:** `CVCRD01Y` | **Business Purpose:** Shared working-storage for card-related screen operations

Contains field-level editing variables and validation flags used by the online card management programs (COCRDLIC, COCRDSLC, COCRDUPC). Defines the `CC-WORK-AREA` structure used for inter-field communication during BMS screen processing.

---

## Application Communication Area

**Copybook:** `COCOM01Y` | **Business Purpose:** CICS COMMAREA passed between all online programs

The `CARDDEMO-COMMAREA` structure is the central mechanism for inter-program communication:

| Field Group | Key Fields | Purpose |
|---|---|---|
| Navigation Context | CDEMO-FROM-PROGRAM, CDEMO-FROM-TRANID, CDEMO-TO-PROGRAM | Program-to-program flow control |
| User Context | CDEMO-USER-ID, CDEMO-USER-TYPE | Authenticated user info |
| Program State | CDEMO-PGM-CONTEXT (ENTER/REENTER) | Controls screen send/receive cycle |
| Data Context | CDEMO-ACCT-ID, CDEMO-CARD-NUM | Currently selected business keys |
| AID Key Mapping | CCARD-AID-ENTER, CCARD-AID-PFK03, etc. | Mapped function key storage |
| Screen State | CDEMO-LAST-MAP, CDEMO-LAST-MAPSET | Last-displayed screen reference |

---

## VSAM File Catalog

| Logical Name | VSAM Type | Key Field(s) | Record Size | Copybook | Business Entity |
|---|---|---|---|---|---|
| USRSEC | KSDS | User ID (8) | 80 | CSUSR01Y | User Security |
| ACCTDAT | KSDS | Account ID (11) | 300 | CVACT01Y | Account Master |
| CARDDAT | KSDS | Card Number (16) | 150 | CVACT02Y | Credit Card |
| CARDAIX | AIX | Account ID path | 150 | CVACT02Y | Card by Account |
| CUSTDAT | KSDS | Customer ID (9) | 500 | CVCUS01Y | Customer |
| CARDXREF | KSDS | Card Number (16) | 50 | CVACT03Y | Card Cross-Reference |
| CXACAIX | AIX | Account ID path | 50 | CVACT03Y | Cross-Ref by Account |
| TRANSACT | KSDS | Transaction ID (16) | 350 | CVTRA05Y | Posted Transactions |
| DALYTRAN | KSDS | Transaction ID (16) | 350 | CVTRA06Y | Daily Transactions |
| DALYREJS | KSDS | Transaction ID (16) | 350 | CVTRA06Y | Rejected Transactions |
| TCATBALF | KSDS | Acct+Type+Cat (17) | 50 | CVTRA01Y | Category Balance |
| DISCGRP | KSDS | Group+Type+Cat (16) | 50 | CVTRA02Y | Disclosure/Rate Group |
| TRANTYPE | KSDS | Type Code (2) | 60 | CVTRA03Y | Transaction Type |
| TRANCATG | KSDS | Type+Cat (6) | 60 | CVTRA04Y | Transaction Category |

### Alternate Indexes

| AIX Name | Base Cluster | AIX Key | Unique? | Purpose |
|---|---|---|---|---|
| CARDAIX | CARDDAT | Account ID at offset in card record | No | Find cards by account |
| CXACAIX | CARDXREF | Account ID at offset in xref record | No | Find xrefs by account |
| TRANSACT AIX | TRANSACT | Processed timestamp (26 bytes at offset 304) | No | Time-range queries |
