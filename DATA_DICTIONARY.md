# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Source**: COBOL Copybooks in `app/cpy/` | **Platform**: IBM z/OS VSAM KSDS

---

## Overview

This document maps every COBOL copybook PIC clause to its business meaning. Each VSAM file stores fixed-length records whose layouts are defined in copybooks. The data types follow standard COBOL conventions:

| COBOL PIC | Java Equivalent | Description |
|-----------|-----------------|-------------|
| `PIC X(n)` | `String` (length n) | Alphanumeric, space-padded |
| `PIC 9(n)` | `long` / `int` | Unsigned numeric (display format) |
| `PIC S9(n)V99` | `BigDecimal` | Signed decimal with 2 implied decimals |
| `PIC S9(n) COMP` | `int` / `long` | Binary integer |
| `PIC 9(n) COMP-3` | `BigDecimal` | Packed decimal |
| `FILLER` | -- | Reserved/padding bytes |

---

## 1. Account Entity

> **Copybook**: `CVACT01Y.cpy` | **Record**: `ACCOUNT-RECORD` | **Length**: 300 bytes | **VSAM File**: `ACCTDAT` (KSDS, key = ACCT-ID)

| # | COBOL Field | PIC Clause | Offset | Size | Business Name | Business Description |
|---|-------------|-----------|--------|------|---------------|---------------------|
| 1 | `ACCT-ID` | `9(11)` | 0 | 11 | Account Number | Unique 11-digit account identifier |
| 2 | `ACCT-ACTIVE-STATUS` | `X(01)` | 11 | 1 | Account Status | Active/inactive flag (Y/N) |
| 3 | `ACCT-CURR-BAL` | `S9(10)V99` | 12 | 12 | Current Balance | Current account balance with 2 decimals |
| 4 | `ACCT-CREDIT-LIMIT` | `S9(10)V99` | 24 | 12 | Credit Limit | Maximum credit limit |
| 5 | `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | 36 | 12 | Cash Advance Limit | Maximum cash advance amount |
| 6 | `ACCT-OPEN-DATE` | `X(10)` | 48 | 10 | Account Open Date | Date account was opened (YYYY-MM-DD) |
| 7 | `ACCT-EXPIRAION-DATE` | `X(10)` | 58 | 10 | Expiration Date | Account expiration date (YYYY-MM-DD) |
| 8 | `ACCT-REISSUE-DATE` | `X(10)` | 68 | 10 | Reissue Date | Date of last card reissue (YYYY-MM-DD) |
| 9 | `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | 78 | 12 | Cycle Credits | Total credits in current billing cycle |
| 10 | `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | 90 | 12 | Cycle Debits | Total debits in current billing cycle |
| 11 | `ACCT-ADDR-ZIP` | `X(10)` | 102 | 10 | ZIP Code | Account holder's postal/ZIP code |
| 12 | `ACCT-GROUP-ID` | `X(10)` | 112 | 10 | Account Group | Disclosure group for interest rate lookup |
| 13 | `FILLER` | `X(178)` | 122 | 178 | (Reserved) | Padding to 300-byte record length |

**Business Rules**:
- Primary key is `ACCT-ID` (11-digit numeric)
- `ACCT-GROUP-ID` links to the Disclosure Group entity for interest rate determination
- Balance fields (`ACCT-CURR-BAL`, cycle credits/debits) are signed to support credits
- Updated by: COACTUPC (online), CBTRN02C (batch posting), CBACT04C (interest calc)

---

## 2. Credit Card Entity

> **Copybook**: `CVACT02Y.cpy` | **Record**: `CARD-RECORD` | **Length**: 150 bytes | **VSAM File**: `CARDDAT` (KSDS, key = CARD-NUM)

| # | COBOL Field | PIC Clause | Offset | Size | Business Name | Business Description |
|---|-------------|-----------|--------|------|---------------|---------------------|
| 1 | `CARD-NUM` | `X(16)` | 0 | 16 | Card Number | 16-digit credit card number (primary key) |
| 2 | `CARD-ACCT-ID` | `9(11)` | 16 | 11 | Account Number | Linked account ID (FK to ACCTDAT) |
| 3 | `CARD-CVV-CD` | `9(03)` | 27 | 3 | CVV Code | 3-digit card verification value |
| 4 | `CARD-EMBOSSED-NAME` | `X(50)` | 30 | 50 | Cardholder Name | Name embossed on the physical card |
| 5 | `CARD-EXPIRAION-DATE` | `X(10)` | 80 | 10 | Expiration Date | Card expiration date (YYYY-MM-DD) |
| 6 | `CARD-ACTIVE-STATUS` | `X(01)` | 90 | 1 | Card Status | Active/inactive flag (Y/N) |
| 7 | `FILLER` | `X(59)` | 91 | 59 | (Reserved) | Padding to 150-byte record length |

**Business Rules**:
- Primary key is `CARD-NUM` (16-character alphanumeric)
- Each card belongs to exactly one account via `CARD-ACCT-ID`
- Multiple cards can share the same account
- Alternate index `CARDAIX` provides access by `CARD-ACCT-ID`
- Read by: COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC

---

## 3. Customer Entity

> **Copybook**: `CVCUS01Y.cpy` | **Record**: `CUSTOMER-RECORD` | **Length**: 500 bytes | **VSAM File**: `CUSTDAT` (KSDS, key = CUST-ID)

| # | COBOL Field | PIC Clause | Offset | Size | Business Name | Business Description |
|---|-------------|-----------|--------|------|---------------|---------------------|
| 1 | `CUST-ID` | `9(09)` | 0 | 9 | Customer ID | Unique 9-digit customer identifier |
| 2 | `CUST-FIRST-NAME` | `X(25)` | 9 | 25 | First Name | Customer's first/given name |
| 3 | `CUST-MIDDLE-NAME` | `X(25)` | 34 | 25 | Middle Name | Customer's middle name |
| 4 | `CUST-LAST-NAME` | `X(25)` | 59 | 25 | Last Name | Customer's surname/family name |
| 5 | `CUST-ADDR-LINE-1` | `X(50)` | 84 | 50 | Address Line 1 | Primary street address |
| 6 | `CUST-ADDR-LINE-2` | `X(50)` | 134 | 50 | Address Line 2 | Secondary address (apt, suite, etc.) |
| 7 | `CUST-ADDR-LINE-3` | `X(50)` | 184 | 50 | Address Line 3 | Additional address information |
| 8 | `CUST-ADDR-STATE-CD` | `X(02)` | 234 | 2 | State Code | US state abbreviation (e.g., NY, CA) |
| 9 | `CUST-ADDR-COUNTRY-CD` | `X(03)` | 236 | 3 | Country Code | ISO country code (e.g., USA) |
| 10 | `CUST-ADDR-ZIP` | `X(10)` | 239 | 10 | ZIP Code | Postal/ZIP code |
| 11 | `CUST-PHONE-NUM-1` | `X(15)` | 249 | 15 | Primary Phone | Primary phone number |
| 12 | `CUST-PHONE-NUM-2` | `X(15)` | 264 | 15 | Secondary Phone | Alternate phone number |
| 13 | `CUST-SSN` | `9(09)` | 279 | 9 | SSN | Social Security Number (9-digit) |
| 14 | `CUST-GOVT-ISSUED-ID` | `X(20)` | 288 | 20 | Government ID | Government-issued identification number |
| 15 | `CUST-DOB-YYYY-MM-DD` | `X(10)` | 308 | 10 | Date of Birth | Customer date of birth (YYYY-MM-DD) |
| 16 | `CUST-EFT-ACCOUNT-ID` | `X(10)` | 318 | 10 | EFT Account | Electronic funds transfer account number |
| 17 | `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | 328 | 1 | Primary Holder | Primary cardholder indicator (Y/N) |
| 18 | `CUST-FICO-CREDIT-SCORE` | `9(03)` | 329 | 3 | FICO Score | Credit score (300-850 range) |
| 19 | `FILLER` | `X(168)` | 332 | 168 | (Reserved) | Padding to 500-byte record length |

**Business Rules**:
- Primary key is `CUST-ID` (9-digit numeric)
- Linked to cards/accounts via the Cross-Reference entity
- Contains PII (SSN, DOB, addresses) -- requires data protection in modernized system
- Phone numbers validated against North American area codes (CSLKPCDY.cpy)
- State codes validated against US state list (CSLKPCDY.cpy)
- Updated by: COACTUPC (online)

---

## 4. Card Cross-Reference Entity

> **Copybook**: `CVACT03Y.cpy` | **Record**: `CARD-XREF-RECORD` | **Length**: 50 bytes | **VSAM File**: `CARDXREF` (KSDS, key = XREF-CARD-NUM)

| # | COBOL Field | PIC Clause | Offset | Size | Business Name | Business Description |
|---|-------------|-----------|--------|------|---------------|---------------------|
| 1 | `XREF-CARD-NUM` | `X(16)` | 0 | 16 | Card Number | Card number (FK to CARDDAT, primary key) |
| 2 | `XREF-CUST-ID` | `9(09)` | 16 | 9 | Customer ID | Owning customer (FK to CUSTDAT) |
| 3 | `XREF-ACCT-ID` | `9(11)` | 25 | 11 | Account Number | Associated account (FK to ACCTDAT) |
| 4 | `FILLER` | `X(14)` | 36 | 14 | (Reserved) | Padding to 50-byte record length |

**Business Rules**:
- Acts as a junction/association table linking Card <-> Customer <-> Account
- Primary key is `XREF-CARD-NUM`
- Alternate index `CXACAIX` provides access by `XREF-ACCT-ID`
- Essential for navigating from any one entity to the related entities
- Read by virtually every online and batch program

---

## 5. Transaction Entity

> **Copybook**: `CVTRA05Y.cpy` | **Record**: `TRAN-RECORD` | **Length**: 350 bytes | **VSAM File**: `TRANSACT` (KSDS, key = TRAN-ID)

| # | COBOL Field | PIC Clause | Offset | Size | Business Name | Business Description |
|---|-------------|-----------|--------|------|---------------|---------------------|
| 1 | `TRAN-ID` | `X(16)` | 0 | 16 | Transaction ID | Unique 16-character transaction identifier |
| 2 | `TRAN-TYPE-CD` | `X(02)` | 16 | 2 | Type Code | Transaction type (e.g., SA=Sale, CR=Credit) |
| 3 | `TRAN-CAT-CD` | `9(04)` | 18 | 4 | Category Code | 4-digit transaction category |
| 4 | `TRAN-SOURCE` | `X(10)` | 22 | 10 | Source | Origination source of the transaction |
| 5 | `TRAN-DESC` | `X(100)` | 32 | 100 | Description | Free-text transaction description |
| 6 | `TRAN-AMT` | `S9(09)V99` | 132 | 11 | Amount | Transaction amount (signed, 2 decimals) |
| 7 | `TRAN-MERCHANT-ID` | `9(09)` | 143 | 9 | Merchant ID | Unique merchant identifier |
| 8 | `TRAN-MERCHANT-NAME` | `X(50)` | 152 | 50 | Merchant Name | Name of the merchant |
| 9 | `TRAN-MERCHANT-CITY` | `X(50)` | 202 | 50 | Merchant City | City where merchant is located |
| 10 | `TRAN-MERCHANT-ZIP` | `X(10)` | 252 | 10 | Merchant ZIP | Merchant postal/ZIP code |
| 11 | `TRAN-CARD-NUM` | `X(16)` | 262 | 16 | Card Number | Card used for this transaction |
| 12 | `TRAN-ORIG-TS` | `X(26)` | 278 | 26 | Origination Timestamp | When transaction was originated |
| 13 | `TRAN-PROC-TS` | `X(26)` | 304 | 26 | Processing Timestamp | When transaction was processed/posted |
| 14 | `FILLER` | `X(20)` | 330 | 20 | (Reserved) | Padding to 350-byte record length |

**Business Rules**:
- Primary key is `TRAN-ID` (16-character)
- `TRAN-TYPE-CD` references the Transaction Type entity
- `TRAN-CAT-CD` references the Transaction Category entity
- Amount is signed: positive for debits/purchases, negative for credits/returns
- Timestamps are in ISO-like format (26 chars)
- Created by: COTRN02C (online add), CBTRN02C (batch posting)
- Read by: COTRN00C (list), COTRN01C (view), COBIL00C (bill payment)

---

## 6. Daily Transaction Entity (Batch Input)

> **Copybook**: `CVTRA06Y.cpy` | **Record**: `DALYTRAN-RECORD` | **Length**: 350 bytes | **File**: Sequential (batch input)

| # | COBOL Field | PIC Clause | Offset | Size | Business Name | Business Description |
|---|-------------|-----------|--------|------|---------------|---------------------|
| 1 | `DALYTRAN-ID` | `X(16)` | 0 | 16 | Transaction ID | Daily transaction identifier |
| 2 | `DALYTRAN-TYPE-CD` | `X(02)` | 16 | 2 | Type Code | Transaction type code |
| 3 | `DALYTRAN-CAT-CD` | `9(04)` | 18 | 4 | Category Code | Transaction category code |
| 4 | `DALYTRAN-SOURCE` | `X(10)` | 22 | 10 | Source | Transaction origination source |
| 5 | `DALYTRAN-DESC` | `X(100)` | 32 | 100 | Description | Transaction description |
| 6 | `DALYTRAN-AMT` | `S9(09)V99` | 132 | 11 | Amount | Transaction amount |
| 7 | `DALYTRAN-MERCHANT-ID` | `9(09)` | 143 | 9 | Merchant ID | Merchant identifier |
| 8 | `DALYTRAN-MERCHANT-NAME` | `X(50)` | 152 | 50 | Merchant Name | Merchant name |
| 9 | `DALYTRAN-MERCHANT-CITY` | `X(50)` | 202 | 50 | Merchant City | Merchant city |
| 10 | `DALYTRAN-MERCHANT-ZIP` | `X(10)` | 252 | 10 | Merchant ZIP | Merchant ZIP code |
| 11 | `DALYTRAN-CARD-NUM` | `X(16)` | 262 | 16 | Card Number | Card used for transaction |
| 12 | `DALYTRAN-ORIG-TS` | `X(26)` | 278 | 26 | Origination Timestamp | Original transaction timestamp |
| 13 | `DALYTRAN-PROC-TS` | `X(26)` | 304 | 26 | Processing Timestamp | Processing timestamp |
| 14 | `FILLER` | `X(20)` | 330 | 20 | (Reserved) | Padding |

**Business Rules**:
- Identical structure to Transaction entity but stored in a sequential file
- Fed into batch posting programs (CBTRN01C validates, CBTRN02C posts)
- After posting, records move from daily file to the TRANSACT VSAM master

---

## 7. Transaction Category Balance Entity

> **Copybook**: `CVTRA01Y.cpy` | **Record**: `TRAN-CAT-BAL-RECORD` | **Length**: 50 bytes | **VSAM File**: `TCATBAL`

| # | COBOL Field | PIC Clause | Offset | Size | Business Name | Business Description |
|---|-------------|-----------|--------|------|---------------|---------------------|
| 1 | `TRANCAT-ACCT-ID` | `9(11)` | 0 | 11 | Account Number | Account ID (part of composite key) |
| 2 | `TRANCAT-TYPE-CD` | `X(02)` | 11 | 2 | Type Code | Transaction type (part of composite key) |
| 3 | `TRANCAT-CD` | `9(04)` | 13 | 4 | Category Code | Category code (part of composite key) |
| 4 | `TRAN-CAT-BAL` | `S9(09)V99` | 17 | 11 | Category Balance | Running balance for this account/type/category |
| 5 | `FILLER` | `X(22)` | 28 | 22 | (Reserved) | Padding to 50-byte record length |

**Business Rules**:
- Composite key: Account + Type + Category
- Tracks running balance per transaction category per account
- Updated during batch posting (CBTRN02C)
- Used for interest calculation grouping

---

## 8. Disclosure Group Entity

> **Copybook**: `CVTRA02Y.cpy` | **Record**: `DIS-GROUP-RECORD` | **Length**: 50 bytes | **VSAM File**: `DISCGRP`

| # | COBOL Field | PIC Clause | Offset | Size | Business Name | Business Description |
|---|-------------|-----------|--------|------|---------------|---------------------|
| 1 | `DIS-ACCT-GROUP-ID` | `X(10)` | 0 | 10 | Account Group ID | Group identifier (links to ACCT-GROUP-ID) |
| 2 | `DIS-TRAN-TYPE-CD` | `X(02)` | 10 | 2 | Type Code | Transaction type for this rate |
| 3 | `DIS-TRAN-CAT-CD` | `9(04)` | 12 | 4 | Category Code | Transaction category for this rate |
| 4 | `DIS-INT-RATE` | `S9(04)V99` | 16 | 6 | Interest Rate | Annual interest rate percentage |
| 5 | `FILLER` | `X(28)` | 22 | 28 | (Reserved) | Padding to 50-byte record length |

**Business Rules**:
- Composite key: Group + Type + Category
- Links Account Group to interest rates by transaction type/category
- Used by CBACT04C for interest calculations
- Enables different rates for purchases vs. cash advances vs. balance transfers

---

## 9. Transaction Type Entity

> **Copybook**: `CVTRA03Y.cpy` | **Record**: `TRAN-TYPE-RECORD` | **Length**: 60 bytes | **VSAM File**: `TRANTYPE`

| # | COBOL Field | PIC Clause | Offset | Size | Business Name | Business Description |
|---|-------------|-----------|--------|------|---------------|---------------------|
| 1 | `TRAN-TYPE` | `X(02)` | 0 | 2 | Type Code | 2-character transaction type code (primary key) |
| 2 | `TRAN-TYPE-DESC` | `X(50)` | 2 | 50 | Type Description | Human-readable description of this type |
| 3 | `FILLER` | `X(08)` | 52 | 8 | (Reserved) | Padding to 60-byte record length |

**Business Rules**:
- Reference/lookup table for transaction type codes
- Common values: SA (Sale), CR (Credit), PR (Purchase Return), CA (Cash Advance)
- Admin users can manage via DB2 optional module (COTRTUPC, COTRTLIC)

---

## 10. Transaction Category Entity

> **Copybook**: `CVTRA04Y.cpy` | **Record**: `TRAN-CAT-RECORD` | **Length**: 60 bytes | **VSAM File**: `TRANCATG`

| # | COBOL Field | PIC Clause | Offset | Size | Business Name | Business Description |
|---|-------------|-----------|--------|------|---------------|---------------------|
| 1 | `TRAN-TYPE-CD` | `X(02)` | 0 | 2 | Type Code | Parent transaction type (composite key part 1) |
| 2 | `TRAN-CAT-CD` | `9(04)` | 2 | 4 | Category Code | Category within the type (composite key part 2) |
| 3 | `TRAN-CAT-TYPE-DESC` | `X(50)` | 6 | 50 | Category Description | Human-readable category description |
| 4 | `FILLER` | `X(04)` | 56 | 4 | (Reserved) | Padding to 60-byte record length |

**Business Rules**:
- Composite key: Type Code + Category Code
- Sub-classification within each transaction type
- Example: Type "SA" (Sale) may have categories for retail, online, recurring, etc.

---

## 11. User Security Entity

> **Copybook**: `CSUSR01Y.cpy` | **Record**: `SEC-USER-DATA` | **Length**: 80 bytes | **VSAM File**: `USRSEC` (KSDS, key = SEC-USR-ID)

| # | COBOL Field | PIC Clause | Offset | Size | Business Name | Business Description |
|---|-------------|-----------|--------|------|---------------|---------------------|
| 1 | `SEC-USR-ID` | `X(08)` | 0 | 8 | User ID | Login user identifier (primary key) |
| 2 | `SEC-USR-FNAME` | `X(20)` | 8 | 20 | First Name | User's first name |
| 3 | `SEC-USR-LNAME` | `X(20)` | 28 | 20 | Last Name | User's last name |
| 4 | `SEC-USR-PWD` | `X(08)` | 48 | 8 | Password | Login password (stored in clear text) |
| 5 | `SEC-USR-TYPE` | `X(01)` | 56 | 1 | User Type | 'A' = Admin, 'U' = Regular user |
| 6 | `SEC-USR-FILLER` | `X(23)` | 57 | 23 | (Reserved) | Padding to 80-byte record length |

**Business Rules**:
- Primary key is `SEC-USR-ID` (8-character)
- Password stored in plain text -- critical security concern for modernization
- User type determines menu access: Admin gets COADM01C, Regular gets COMEN01C
- Managed by: COUSR00C (list), COUSR01C (add), COUSR02C (update), COUSR03C (delete)
- Read by: COSGN00C (authentication)

---

## 12. Communication Area (COMMAREA)

> **Copybook**: `COCOM01Y.cpy` | **Record**: `CARDDEMO-COMMAREA` | **Scope**: Shared across all CICS programs via XCTL

| # | COBOL Field | PIC Clause | Size | Business Name | Business Description |
|---|-------------|-----------|------|---------------|---------------------|
| 1 | `CDEMO-FROM-TRANID` | `X(04)` | 4 | Source Transaction | Transaction ID of the calling program |
| 2 | `CDEMO-FROM-PROGRAM` | `X(08)` | 8 | Source Program | Program name of the caller |
| 3 | `CDEMO-TO-TRANID` | `X(04)` | 4 | Target Transaction | Transaction ID to route to |
| 4 | `CDEMO-TO-PROGRAM` | `X(08)` | 8 | Target Program | Program name to route to |
| 5 | `CDEMO-USER-ID` | `X(08)` | 8 | User ID | Logged-in user's ID |
| 6 | `CDEMO-USER-TYPE` | `X(01)` | 1 | User Type | 'A' = Admin, 'U' = Regular |
| 7 | `CDEMO-PGM-CONTEXT` | `9(01)` | 1 | Context Flag | 0 = first entry, 1 = re-entry |
| 8 | `CDEMO-CUST-ID` | `9(09)` | 9 | Customer ID | Selected customer context |
| 9 | `CDEMO-CUST-FNAME` | `X(25)` | 25 | Customer First Name | Selected customer first name |
| 10 | `CDEMO-CUST-MNAME` | `X(25)` | 25 | Customer Middle Name | Selected customer middle name |
| 11 | `CDEMO-CUST-LNAME` | `X(25)` | 25 | Customer Last Name | Selected customer last name |
| 12 | `CDEMO-ACCT-ID` | `9(11)` | 11 | Account Number | Selected account context |
| 13 | `CDEMO-ACCT-STATUS` | `X(01)` | 1 | Account Status | Selected account status |
| 14 | `CDEMO-CARD-NUM` | `9(16)` | 16 | Card Number | Selected card context |
| 15 | `CDEMO-LAST-MAP` | `X(7)` | 7 | Last Map | Last BMS map displayed |
| 16 | `CDEMO-LAST-MAPSET` | `X(7)` | 7 | Last Mapset | Last BMS mapset used |

**Business Rules**:
- Passed between CICS programs via XCTL COMMAREA
- Maintains navigation state (from/to program routing)
- Carries selected entity context (customer, account, card) across screens
- `CDEMO-PGM-CONTEXT` distinguishes first entry vs. return to a screen

---

## 13. Export Record (Multi-Type)

> **Copybook**: `CVEXPORT.cpy` | **Record**: `EXPORT-RECORD` | **Length**: 500 bytes | **File**: Sequential export file

| # | COBOL Field | PIC Clause | Size | Business Name | Business Description |
|---|-------------|-----------|------|---------------|---------------------|
| 1 | `EXPORT-REC-TYPE` | `X(1)` | 1 | Record Type | C=Customer, A=Account, T=Transaction, X=Xref, D=Card |
| 2 | `EXPORT-TIMESTAMP` | `X(26)` | 26 | Export Timestamp | When the record was exported |
| 3 | `EXPORT-SEQUENCE-NUM` | `9(9) COMP` | 4 | Sequence Number | Sequential record counter |
| 4 | `EXPORT-BRANCH-ID` | `X(4)` | 4 | Branch ID | Originating branch identifier |
| 5 | `EXPORT-REGION-CODE` | `X(5)` | 5 | Region Code | Geographic region code |
| 6 | `EXPORT-RECORD-DATA` | `X(460)` | 460 | Record Payload | REDEFINES area for entity-specific data |

**REDEFINES variants** (within `EXPORT-RECORD-DATA`):
- `EXPORT-CUSTOMER-DATA` -- Customer fields (with COMP/COMP-3 optimization)
- `EXPORT-ACCOUNT-DATA` -- Account fields (with COMP-3 for balances)
- `EXPORT-TRANSACTION-DATA` -- Transaction fields (with COMP-3 for amounts)
- `EXPORT-CARD-XREF-DATA` -- Cross-reference fields (with COMP for IDs)
- `EXPORT-CARD-DATA` -- Card fields (with COMP for IDs)

**Business Rules**:
- Used by CBEXPORT/CBIMPORT for branch migration
- REDEFINES pattern packs multiple record types into one file
- Uses COMP/COMP-3 for storage optimization (differs from source VSAM layouts)

---

## 14. Statement/Report Transaction Layout

> **Copybook**: `COSTM01.CPY` | **Record**: `TRNX-RECORD` | **Scope**: Batch statement generation

| # | COBOL Field | PIC Clause | Size | Business Name | Business Description |
|---|-------------|-----------|------|---------------|---------------------|
| 1 | `TRNX-CARD-NUM` | `X(16)` | 16 | Card Number | Card number (part of composite key) |
| 2 | `TRNX-ID` | `X(16)` | 16 | Transaction ID | Transaction identifier (part of composite key) |
| 3 | `TRNX-TYPE-CD` | `X(02)` | 2 | Type Code | Transaction type |
| 4 | `TRNX-CAT-CD` | `9(04)` | 4 | Category Code | Transaction category |
| 5 | `TRNX-SOURCE` | `X(10)` | 10 | Source | Transaction source |
| 6 | `TRNX-DESC` | `X(100)` | 100 | Description | Transaction description |
| 7 | `TRNX-AMT` | `S9(09)V99` | 11 | Amount | Transaction amount |
| 8 | `TRNX-MERCHANT-ID` | `9(09)` | 9 | Merchant ID | Merchant identifier |
| 9 | `TRNX-MERCHANT-NAME` | `X(50)` | 50 | Merchant Name | Merchant name |
| 10 | `TRNX-MERCHANT-CITY` | `X(50)` | 50 | Merchant City | Merchant city |
| 11 | `TRNX-MERCHANT-ZIP` | `X(10)` | 10 | Merchant ZIP | Merchant ZIP code |
| 12 | `TRNX-ORIG-TS` | `X(26)` | 26 | Origination Timestamp | Original timestamp |
| 13 | `TRNX-PROC-TS` | `X(26)` | 26 | Processing Timestamp | Processing timestamp |

**Business Rules**:
- Composite key: Card Number + Transaction ID (for sorting by card)
- Used by CBSTM03A/B for statement generation
- Reordered version of TRAN-RECORD with card number first in key

---

## 15. Date/Time Working Storage

> **Copybook**: `CSDAT01Y.cpy` | **Record**: `WS-DATE-TIME` | **Scope**: Used by all CICS programs

| # | COBOL Field | PIC Clause | Business Name | Description |
|---|-------------|-----------|---------------|-------------|
| 1 | `WS-CURDATE-YEAR` | `9(04)` | Current Year | 4-digit year |
| 2 | `WS-CURDATE-MONTH` | `9(02)` | Current Month | 2-digit month |
| 3 | `WS-CURDATE-DAY` | `9(02)` | Current Day | 2-digit day |
| 4 | `WS-CURTIME-HOURS` | `9(02)` | Current Hours | Hours (24h format) |
| 5 | `WS-CURTIME-MINUTE` | `9(02)` | Current Minutes | Minutes |
| 6 | `WS-CURTIME-SECOND` | `9(02)` | Current Seconds | Seconds |
| 7 | `WS-CURDATE-MM-DD-YY` | formatted | Display Date | MM/DD/YY format for screen display |
| 8 | `WS-CURTIME-HH-MM-SS` | formatted | Display Time | HH:MM:SS format for screen display |
| 9 | `WS-TIMESTAMP` | formatted | Full Timestamp | YYYY-MM-DD HH:MM:SS.ffffff format |

---

## Entity Relationship Summary

```
CUSTDAT (Customer)
   |
   | 1:N via CARDXREF
   v
CARDXREF (Cross-Reference) -----> ACCTDAT (Account)
   |                                  |
   | 1:1                              | 1:N
   v                                  v
CARDDAT (Card)                   DISCGRP (Disclosure Group)
                                     |
                                     | provides rates to
                                     v
                                 TCATBAL (Category Balance)
                                     ^
                                     | updated by
                                     |
TRANSACT (Transaction) <--- DALYTRAN (Daily Transaction Input)
   |
   | classified by
   v
TRANTYPE (Type) + TRANCATG (Category)
```

---

## VSAM File Summary

| VSAM File | Entity | Key | Record Len | Access Method |
|-----------|--------|-----|-----------|---------------|
| `USRSEC` | User Security | SEC-USR-ID (8) | 80 | KSDS |
| `ACCTDAT` | Account | ACCT-ID (11) | 300 | KSDS |
| `CARDDAT` | Card | CARD-NUM (16) | 150 | KSDS + AIX |
| `CUSTDAT` | Customer | CUST-ID (9) | 500 | KSDS |
| `CARDXREF` | Cross-Reference | XREF-CARD-NUM (16) | 50 | KSDS + AIX |
| `TRANSACT` | Transaction | TRAN-ID (16) | 350 | KSDS |
| `TRANTYPE` | Transaction Type | TRAN-TYPE (2) | 60 | KSDS |
| `TRANCATG` | Transaction Category | Type+Cat (6) | 60 | KSDS |
| `TCATBAL` | Category Balance | Acct+Type+Cat (17) | 50 | KSDS |
| `DISCGRP` | Disclosure Group | Group+Type+Cat (16) | 50 | KSDS |
| `CARDAIX` | (Alternate Index) | CARD-ACCT-ID | -- | AIX on CARDDAT |
| `CXACAIX` | (Alternate Index) | XREF-ACCT-ID | -- | AIX on CARDXREF |
