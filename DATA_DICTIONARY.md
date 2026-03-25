# CardDemo Data Dictionary

> **Generated from**: Static analysis of copybook PIC clauses in `app/cpy/` and `app/cpy-bms/`
>
> **Purpose**: Maps COBOL data structures to business-friendly entity definitions for modernization planning

---

## Entity Relationship Overview

```
┌──────────────┐       ┌──────────────┐       ┌──────────────────┐
│   Customer   │1─────*│   Account    │1─────*│   Credit Card    │
│  (CVCUS01Y)  │       │  (CVACT01Y)  │       │   (CVACT02Y)     │
└──────────────┘       └──────┬───────┘       └────────┬─────────┘
                              │                        │
                              │                  ┌─────┴──────────┐
                              │                  │  Card X-Ref    │
                              │                  │  (CVACT03Y)    │
                              │                  └─────┬──────────┘
                              │                        │
                       ┌──────┴───────┐          ┌─────┴──────────┐
                       │  Tran Cat    │          │  Transaction   │
                       │  Balance     │          │  (CVTRA05Y)    │
                       │ (CVTRA01Y)   │          └────────────────┘
                       └──────────────┘                │
                                                 ┌─────┴──────────┐
                                                 │ Daily Tran     │
                                                 │ (CVTRA06Y)     │
                                                 └────────────────┘
┌──────────────┐       ┌──────────────┐       ┌──────────────────┐
│  User/Sec    │       │  Tran Type   │       │  Tran Category   │
│ (CSUSR01Y)   │       │ (CVTRA03Y)   │       │  (CVTRA04Y)      │
└──────────────┘       └──────────────┘       └──────────────────┘

┌──────────────┐
│ Discl. Group │
│ (CVTRA02Y)   │
└──────────────┘
```

---

## 1. Account (`CVACT01Y.cpy`)

**VSAM File**: `ACCTDATA.VSAM.KSDS` | **Record Length**: 300 bytes | **Key**: Account ID (11 digits)

| COBOL Field | PIC Clause | Type | Length | Business Name | Description |
|---|---|---|---|---|---|
| `ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Account ID | Unique account identifier (primary key) |
| `ACCT-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 | Account Status | Active status flag (Y/N) |
| `ACCT-CURR-BAL` | `PIC S9(10)V99` | Signed Decimal | 12.2 | Current Balance | Current account balance with 2 decimal places |
| `ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | Signed Decimal | 12.2 | Credit Limit | Maximum credit limit |
| `ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | Signed Decimal | 12.2 | Cash Advance Limit | Maximum cash advance amount |
| `ACCT-OPEN-DATE` | `PIC X(10)` | Date String | 10 | Open Date | Account opening date (YYYY-MM-DD) |
| `ACCT-EXPIRAION-DATE` | `PIC X(10)` | Date String | 10 | Expiration Date | Account expiration date |
| `ACCT-REISSUE-DATE` | `PIC X(10)` | Date String | 10 | Reissue Date | Last card reissue date |
| `ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | Signed Decimal | 12.2 | Cycle Credits | Credits in current billing cycle |
| `ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | Signed Decimal | 12.2 | Cycle Debits | Debits in current billing cycle |
| `ACCT-ADDR-ZIP` | `PIC X(10)` | Alpha | 10 | ZIP Code | Account holder ZIP code |
| `ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 | Account Group | Disclosure/rate group identifier |
| `FILLER` | `PIC X(178)` | -- | 178 | Reserved | Unused space for future expansion |

**Business Rules**:
- Account balance is updated during transaction posting (CBTRN02C) and interest calculation (CBACT04C)
- Cycle credits/debits reset at statement generation
- Account group links to disclosure group for interest rate lookup

---

## 2. Credit Card (`CVACT02Y.cpy`)

**VSAM File**: `CARDDATA.VSAM.KSDS` | **Record Length**: 150 bytes | **Key**: Card Number (16 digits)

| COBOL Field | PIC Clause | Type | Length | Business Name | Description |
|---|---|---|---|---|---|
| `CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card Number | 16-digit credit card number (primary key) |
| `CARD-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Account ID | Parent account (foreign key to CVACT01Y) |
| `CARD-CVV-CD` | `PIC 9(03)` | Numeric | 3 | CVV Code | Card verification value |
| `CARD-EMBOSSED-NAME` | `PIC X(50)` | Alpha | 50 | Embossed Name | Name printed on card |
| `CARD-EXPIRAION-DATE` | `PIC X(10)` | Date String | 10 | Expiration Date | Card expiration date |
| `CARD-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 | Card Status | Active status flag (Y/N) |
| `FILLER` | `PIC X(59)` | -- | 59 | Reserved | Unused space |

**Business Rules**:
- Multiple cards can be linked to one account
- Card status can be updated via COCRDUPC (Card Update screen)
- Card number is used as a key to look up transactions

---

## 3. Card Cross-Reference (`CVACT03Y.cpy`)

**VSAM File**: `CARDXREF.VSAM.KSDS` | **Record Length**: 50 bytes | **Key**: Card Number (16 digits)

| COBOL Field | PIC Clause | Type | Length | Business Name | Description |
|---|---|---|---|---|---|
| `XREF-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card Number | Card number (primary key, FK to CVACT02Y) |
| `XREF-CUST-ID` | `PIC 9(09)` | Numeric | 9 | Customer ID | Customer owning this card (FK to CVCUS01Y) |
| `XREF-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Account ID | Account linked to this card (FK to CVACT01Y) |
| `FILLER` | `PIC X(14)` | -- | 14 | Reserved | Unused space |

**Business Rules**:
- Provides the card → customer → account linkage
- Used extensively in transaction posting to resolve card numbers to accounts
- Has an alternate index (AIX) on Account ID for reverse lookups (CXACAIX)

---

## 4. Customer (`CVCUS01Y.cpy`)

**VSAM File**: `CUSTDATA.VSAM.KSDS` | **Record Length**: 500 bytes | **Key**: Customer ID (9 digits)

| COBOL Field | PIC Clause | Type | Length | Business Name | Description |
|---|---|---|---|---|---|
| `CUST-ID` | `PIC 9(09)` | Numeric | 9 | Customer ID | Unique customer identifier (primary key) |
| `CUST-FIRST-NAME` | `PIC X(25)` | Alpha | 25 | First Name | Customer first name |
| `CUST-MIDDLE-NAME` | `PIC X(25)` | Alpha | 25 | Middle Name | Customer middle name |
| `CUST-LAST-NAME` | `PIC X(25)` | Alpha | 25 | Last Name | Customer last name |
| `CUST-ADDR-LINE-1` | `PIC X(50)` | Alpha | 50 | Address Line 1 | Street address line 1 |
| `CUST-ADDR-LINE-2` | `PIC X(50)` | Alpha | 50 | Address Line 2 | Street address line 2 |
| `CUST-ADDR-LINE-3` | `PIC X(50)` | Alpha | 50 | Address Line 3 | Street address line 3 |
| `CUST-ADDR-STATE-CD` | `PIC X(02)` | Alpha | 2 | State Code | US state code |
| `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | Alpha | 3 | Country Code | Country code |
| `CUST-ADDR-ZIP` | `PIC X(10)` | Alpha | 10 | ZIP Code | Postal/ZIP code |
| `CUST-PHONE-NUM-1` | `PIC X(15)` | Alpha | 15 | Phone 1 | Primary phone number |
| `CUST-PHONE-NUM-2` | `PIC X(15)` | Alpha | 15 | Phone 2 | Secondary phone number |
| `CUST-SSN` | `PIC 9(09)` | Numeric | 9 | SSN | Social Security Number |
| `CUST-GOVT-ISSUED-ID` | `PIC X(20)` | Alpha | 20 | Government ID | Government-issued identification |
| `CUST-DOB-YYYYMMDD` | `PIC X(10)` | Date String | 10 | Date of Birth | Customer date of birth |
| `CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | Alpha | 10 | EFT Account | Electronic funds transfer account ID |
| `CUST-PRI-CARD-HOLDER-IND` | `PIC X(01)` | Alpha | 1 | Primary Cardholder | Primary cardholder indicator (Y/N) |
| `CUST-FICO-CREDIT-SCORE` | `PIC 9(03)` | Numeric | 3 | FICO Score | Credit score |
| `FILLER` | `PIC X(168)` | -- | 168 | Reserved | Unused space |

**Business Rules**:
- Customer data is displayed on account view (COACTVWC) and updatable via COACTUPC
- SSN and FICO score are sensitive PII fields
- Linked to cards and accounts via the cross-reference file

---

## 5. Transaction (`CVTRA05Y.cpy`)

**VSAM File**: `TRANSACT.VSAM.KSDS` | **Record Length**: 350 bytes | **Key**: Transaction ID (16 chars)

| COBOL Field | PIC Clause | Type | Length | Business Name | Description |
|---|---|---|---|---|---|
| `TRAN-ID` | `PIC X(16)` | Alpha | 16 | Transaction ID | Unique transaction identifier (primary key) |
| `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction Type | Type code (FK to CVTRA03Y) |
| `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Category Code | Category code (FK to CVTRA04Y) |
| `TRAN-SOURCE` | `PIC X(10)` | Alpha | 10 | Source | Transaction origination source |
| `TRAN-DESC` | `PIC X(100)` | Alpha | 100 | Description | Free-text transaction description |
| `TRAN-AMT` | `PIC S9(09)V99` | Signed Decimal | 9.2 | Amount | Transaction amount (positive=debit, negative=credit) |
| `TRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant ID | Merchant identifier |
| `TRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant Name | Merchant business name |
| `TRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant City | Merchant city |
| `TRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP | Merchant postal code |
| `TRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card Number | Card used for transaction (FK to CVACT02Y) |
| `TRAN-ORIG-TS` | `PIC X(26)` | Timestamp | 26 | Origination Timestamp | When transaction was initiated |
| `TRAN-PROC-TS` | `PIC X(26)` | Timestamp | 26 | Processing Timestamp | When transaction was posted |
| `FILLER` | `PIC X(20)` | -- | 20 | Reserved | Unused space |

**Business Rules**:
- Created during online entry (COTRN02C) or batch posting (CBTRN02C)
- Amount sign determines debit (positive) vs. credit (negative)
- Interest transactions are auto-generated by CBACT04C
- Bill payments create transactions via COBIL00C

---

## 6. Daily Transaction (`CVTRA06Y.cpy`)

**VSAM File**: `DALYTRAN.PS` (Sequential) | **Record Length**: 350 bytes

| COBOL Field | PIC Clause | Type | Length | Business Name | Description |
|---|---|---|---|---|---|
| `DALYTRAN-ID` | `PIC X(16)` | Alpha | 16 | Transaction ID | Daily transaction identifier |
| `DALYTRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction Type | Type code |
| `DALYTRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Category Code | Category code |
| `DALYTRAN-SOURCE` | `PIC X(10)` | Alpha | 10 | Source | Origination source |
| `DALYTRAN-DESC` | `PIC X(100)` | Alpha | 100 | Description | Transaction description |
| `DALYTRAN-AMT` | `PIC S9(09)V99` | Signed Decimal | 9.2 | Amount | Transaction amount |
| `DALYTRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant ID | Merchant identifier |
| `DALYTRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant Name | Merchant name |
| `DALYTRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant City | Merchant city |
| `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP | Merchant ZIP |
| `DALYTRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card Number | Card used |
| `DALYTRAN-ORIG-TS` | `PIC X(26)` | Timestamp | 26 | Origination Timestamp | When originated |
| `DALYTRAN-PROC-TS` | `PIC X(26)` | Timestamp | 26 | Processing Timestamp | When processed |
| `FILLER` | `PIC X(20)` | -- | 20 | Reserved | Unused space |

**Business Rules**:
- Identical layout to CVTRA05Y but represents unposted daily transactions
- Read by CBTRN02C (posting) and CBTRN01C (validation)
- After posting, records move to TRANSACT master and rejects go to DALYREJS

---

## 7. Transaction Category Balance (`CVTRA01Y.cpy`)

**VSAM File**: `TCATBALF.VSAM.KSDS` | **Record Length**: 50 bytes | **Key**: Account ID + Type + Category

| COBOL Field | PIC Clause | Type | Length | Business Name | Description |
|---|---|---|---|---|---|
| `TRANCAT-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Account ID | Account identifier (part of composite key) |
| `TRANCAT-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction Type | Type code (part of composite key) |
| `TRANCAT-CD` | `PIC 9(04)` | Numeric | 4 | Category Code | Category code (part of composite key) |
| `TRAN-CAT-BAL` | `PIC S9(09)V99` | Signed Decimal | 9.2 | Category Balance | Running balance for this category |
| `FILLER` | `PIC X(22)` | -- | 22 | Reserved | Unused space |

**Business Rules**:
- Tracks running balances per account per transaction type/category
- Updated during transaction posting (CBTRN02C)
- Read during interest calculation (CBACT04C) to compute per-category interest

---

## 8. Disclosure Group (`CVTRA02Y.cpy`)

**VSAM File**: `DISCGRP.VSAM.KSDS` | **Record Length**: 50 bytes | **Key**: Group ID + Type + Category

| COBOL Field | PIC Clause | Type | Length | Business Name | Description |
|---|---|---|---|---|---|
| `DIS-ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 | Account Group ID | Rate group identifier (part of composite key) |
| `DIS-TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction Type | Type code (part of composite key) |
| `DIS-TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Category Code | Category code (part of composite key) |
| `DIS-INT-RATE` | `PIC S9(04)V99` | Signed Decimal | 4.2 | Interest Rate | Annual interest rate percentage |
| `FILLER` | `PIC X(28)` | -- | 28 | Reserved | Unused space |

**Business Rules**:
- Links account groups to interest rates per transaction type/category
- Used by CBACT04C to look up the applicable interest rate
- Account's ACCT-GROUP-ID links to DIS-ACCT-GROUP-ID

---

## 9. Transaction Type (`CVTRA03Y.cpy`)

**VSAM File**: `TRANTYPE.VSAM.KSDS` | **Record Length**: 60 bytes | **Key**: Type Code (2 chars)

| COBOL Field | PIC Clause | Type | Length | Business Name | Description |
|---|---|---|---|---|---|
| `TRAN-TYPE` | `PIC X(02)` | Alpha | 2 | Type Code | Transaction type code (primary key) |
| `TRAN-TYPE-DESC` | `PIC X(50)` | Alpha | 50 | Type Description | Human-readable type description |
| `FILLER` | `PIC X(08)` | -- | 8 | Reserved | Unused space |

**Business Rules**:
- Reference/lookup table for transaction type codes
- Used in reporting (CBTRN03C) to decode type codes
- Managed via DB2 in the optional transaction type module

---

## 10. Transaction Category (`CVTRA04Y.cpy`)

**VSAM File**: `TRANCATG.VSAM.KSDS` | **Record Length**: 60 bytes | **Key**: Type Code + Category Code

| COBOL Field | PIC Clause | Type | Length | Business Name | Description |
|---|---|---|---|---|---|
| `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Type Code | Transaction type (part of composite key) |
| `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Category Code | Category code (part of composite key) |
| `TRAN-CAT-TYPE-DESC` | `PIC X(50)` | Alpha | 50 | Category Description | Human-readable category description |
| `FILLER` | `PIC X(04)` | -- | 4 | Reserved | Unused space |

**Business Rules**:
- Provides sub-classification within transaction types
- Used in reporting for category-level detail
- Categories are hierarchically linked to types

---

## 11. User Security Record (`CSUSR01Y.cpy`)

**VSAM File**: `USRSEC.VSAM.KSDS` | **Record Length**: 80 bytes | **Key**: User ID (8 chars)

| COBOL Field | PIC Clause | Type | Length | Business Name | Description |
|---|---|---|---|---|---|
| `SEC-USR-ID` | `PIC X(08)` | Alpha | 8 | User ID | Login user identifier (primary key) |
| `SEC-USR-FNAME` | `PIC X(20)` | Alpha | 20 | First Name | User first name |
| `SEC-USR-LNAME` | `PIC X(20)` | Alpha | 20 | Last Name | User last name |
| `SEC-USR-PWD` | `PIC X(08)` | Alpha | 8 | Password | User password (plain text) |
| `SEC-USR-TYPE` | `PIC X(01)` | Alpha | 1 | User Type | User type: 'A' = Admin, 'U' = Regular User |
| `SEC-USR-FILLER` | `PIC X(23)` | -- | 23 | Reserved | Unused space |

**Business Rules**:
- Passwords stored in plain text (modernization should use hashing)
- User type determines menu routing: Admin → COADM01C, User → COMEN01C
- Managed through COUSR00C-03C admin screens

---

## 12. Statement Transaction Record (`COSTM01.CPY`)

**VSAM File**: `TRXFL.VSAM.KSDS` (derived/sorted) | **Record Length**: 350 bytes | **Key**: Card Number + Transaction ID

| COBOL Field | PIC Clause | Type | Length | Business Name | Description |
|---|---|---|---|---|---|
| `TRNX-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card Number | Card number (part of composite key) |
| `TRNX-ID` | `PIC X(16)` | Alpha | 16 | Transaction ID | Transaction ID (part of composite key) |
| `TRNX-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Type Code | Transaction type |
| `TRNX-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Category Code | Transaction category |
| `TRNX-SOURCE` | `PIC X(10)` | Alpha | 10 | Source | Transaction source |
| `TRNX-DESC` | `PIC X(100)` | Alpha | 100 | Description | Transaction description |
| `TRNX-AMT` | `PIC S9(09)V99` | Signed Decimal | 9.2 | Amount | Transaction amount |
| `TRNX-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant ID | Merchant ID |
| `TRNX-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant Name | Merchant name |
| `TRNX-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant City | Merchant city |
| `TRNX-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP | Merchant ZIP |
| `TRNX-ORIG-TS` | `PIC X(26)` | Timestamp | 26 | Origination Timestamp | Origination timestamp |
| `TRNX-PROC-TS` | `PIC X(26)` | Timestamp | 26 | Processing Timestamp | Processing timestamp |
| `FILLER` | `PIC X(20)` | -- | 20 | Reserved | Unused space |

**Business Rules**:
- Re-keyed version of CVTRA05Y with card number prepended to transaction ID
- Created by SORT step in CREASTMT.JCL for statement generation
- Consumed by CBSTM03A to produce per-card statements

---

## 13. Customer Record - Statement Layout (`CUSTREC.cpy`)

| COBOL Field | PIC Clause | Type | Length | Business Name | Description |
|---|---|---|---|---|---|
| `CUST-ID` | `PIC 9(09)` | Numeric | 9 | Customer ID | Customer identifier |
| `CUST-FIRST-NAME` | `PIC X(25)` | Alpha | 25 | First Name | Customer first name |
| `CUST-MIDDLE-NAME` | `PIC X(25)` | Alpha | 25 | Middle Name | Customer middle name |
| `CUST-LAST-NAME` | `PIC X(25)` | Alpha | 25 | Last Name | Customer last name |
| `CUST-ADDR-LINE-1` | `PIC X(50)` | Alpha | 50 | Address Line 1 | Street address |
| `CUST-ADDR-LINE-2` | `PIC X(50)` | Alpha | 50 | Address Line 2 | Address line 2 |
| `CUST-ADDR-LINE-3` | `PIC X(50)` | Alpha | 50 | Address Line 3 | Address line 3 |
| `CUST-ADDR-STATE-CD` | `PIC X(02)` | Alpha | 2 | State Code | State |
| `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | Alpha | 3 | Country Code | Country |
| `CUST-ADDR-ZIP` | `PIC X(10)` | Alpha | 10 | ZIP Code | ZIP code |

**Notes**: Subset of CVCUS01Y used specifically by statement generation (CBSTM03A).

---

## 14. Export/Import Record (`CVEXPORT.cpy`)

| COBOL Field | PIC Clause | Type | Length | Business Name | Description |
|---|---|---|---|---|---|
| `EXPORT-RECORD-TYPE` | `PIC X(01)` | Alpha | 1 | Record Type Tag | C=Customer, A=Account, X=Xref, T=Transaction, R=Card |
| `EXPORT-DATA` | `PIC X(499)` | Alpha | 499 | Record Data | Raw record data (interpreted by type) |

**Business Rules**:
- Used by CBEXPORT/CBIMPORT for bulk data migration
- Type tag determines which entity layout to apply when parsing

---

## 15. Common Communication Area (`COCOM01Y.cpy`)

**Usage**: Passed via CICS COMMAREA between all online programs

| COBOL Field | PIC Clause | Type | Length | Business Name | Description |
|---|---|---|---|---|---|
| `CDEMO-FROM-TRANID` | `PIC X(04)` | Alpha | 4 | Source Transaction | Transaction ID of calling program |
| `CDEMO-FROM-PROGRAM` | `PIC X(08)` | Alpha | 8 | Source Program | Program name of caller |
| `CDEMO-TO-TRANID` | `PIC X(04)` | Alpha | 4 | Target Transaction | Transaction ID of target program |
| `CDEMO-TO-PROGRAM` | `PIC X(08)` | Alpha | 8 | Target Program | Program name of target |
| `CDEMO-PGM-CONTEXT` | `PIC 9(01)` | Numeric | 1 | Program Context | Context flag (0=fresh, 1=return) |
| `CDEMO-PGM-REENTER` | `PIC X(01)` | Alpha | 1 | Re-enter Flag | Whether program is re-entered |
| `CDEMO-USR-ID` | `PIC X(08)` | Alpha | 8 | User ID | Logged-in user ID |
| `CDEMO-USRTYP` | `PIC X(01)` | Alpha | 1 | User Type | A=Admin, U=User |
| `CDEMO-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Account ID | Selected account context |
| `CDEMO-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card Number | Selected card context |
| `CDEMO-CUST-ID` | `PIC 9(09)` | Numeric | 9 | Customer ID | Selected customer context |
| `CDEMO-LAST-MAP` | `PIC X(07)` | Alpha | 7 | Last Map | Last BMS map sent |
| `CDEMO-LAST-MAPSET` | `PIC X(07)` | Alpha | 7 | Last Mapset | Last BMS mapset sent |

**Business Rules**:
- This structure is the glue connecting all online CICS programs
- Carries navigation state, user identity, and selected entity context
- Maps directly to a Java session/request context in modernized application

---

## 16. Transaction Report Structures (`CVTRA07Y.cpy`)

### Report Header

| COBOL Field | PIC Clause | Business Name |
|---|---|---|
| `REPT-SHORT-NAME` | `PIC X(38)` | Report Short Name (DALYREPT) |
| `REPT-LONG-NAME` | `PIC X(41)` | Report Title (Daily Transaction Report) |
| `REPT-START-DATE` | `PIC X(10)` | Report Start Date |
| `REPT-END-DATE` | `PIC X(10)` | Report End Date |

### Transaction Detail Line

| COBOL Field | PIC Clause | Business Name |
|---|---|---|
| `TRAN-REPORT-TRANS-ID` | `PIC X(16)` | Transaction ID |
| `TRAN-REPORT-ACCOUNT-ID` | `PIC X(11)` | Account ID |
| `TRAN-REPORT-TYPE-CD` | `PIC X(02)` | Type Code |
| `TRAN-REPORT-TYPE-DESC` | `PIC X(15)` | Type Description |
| `TRAN-REPORT-CAT-CD` | `PIC 9(04)` | Category Code |
| `TRAN-REPORT-CAT-DESC` | `PIC X(29)` | Category Description |
| `TRAN-REPORT-SOURCE` | `PIC X(10)` | Transaction Source |
| `TRAN-REPORT-AMT` | `PIC -ZZZ,ZZZ,ZZZ.ZZ` | Formatted Amount |

### Report Totals

| COBOL Field | PIC Clause | Business Name |
|---|---|---|
| `REPT-PAGE-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Page Total |
| `REPT-ACCOUNT-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Account Total |
| `REPT-GRAND-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Grand Total |

---

## VSAM File Summary

| CICS DD Name | VSAM Dataset | Key | Record Len | Entity | Access Pattern |
|---|---|---|---|---|---|
| `USRSEC` | `USRSEC.VSAM.KSDS` | User ID (8) | 80 | User Security | R/W/D by COUSR*, R by COSGN00C |
| `ACCTDAT` | `ACCTDATA.VSAM.KSDS` | Account ID (11) | 300 | Account | R by COACTVWC, R/W by COACTUPC/COBIL00C, R/W by batch |
| `CARDDAT` | `CARDDATA.VSAM.KSDS` | Card Num (16) | 150 | Credit Card | Browse by COCRDLIC, R by COCRDSLC, R/W by COCRDUPC |
| `CCXREF` | `CARDXREF.VSAM.KSDS` | Card Num (16) | 50 | Cross-Reference | R by COTRN02C/COBIL00C, R by batch |
| `CXACAIX` | `CARDXREF.VSAM.AIX` | Account ID (11) | 50 | Cross-Ref (AIX) | R by COTRN02C/COBIL00C (reverse lookup) |
| `TRANSACT` | `TRANSACT.VSAM.KSDS` | Tran ID (16) | 350 | Transaction | Browse by COTRN00C, R by COTRN01C, W by COTRN02C/batch |
| -- | `TCATBALF.VSAM.KSDS` | Acct+Type+Cat | 50 | Category Balance | R/W by CBTRN02C, R by CBACT04C |
| -- | `DISCGRP.VSAM.KSDS` | Group+Type+Cat | 50 | Disclosure Group | R by CBACT04C |
| -- | `TRANTYPE.VSAM.KSDS` | Type (2) | 60 | Transaction Type | R by CBTRN03C |
| -- | `TRANCATG.VSAM.KSDS` | Type+Cat | 60 | Transaction Category | R by CBTRN03C |

---

## Modernization Mapping: COBOL → Java

| COBOL Concept | Java Equivalent |
|---|---|
| Copybook (01-level record) | Java POJO / JPA Entity class |
| PIC X(n) | `String` (length n) |
| PIC 9(n) | `long` or `int` |
| PIC S9(n)V99 | `BigDecimal` |
| PIC X(10) date | `LocalDate` |
| PIC X(26) timestamp | `LocalDateTime` |
| VSAM KSDS | JPA Repository / SQL Table |
| VSAM AIX | Database secondary index |
| FILLER | Not mapped (padding removed) |
| 88-level condition names | Java `enum` or boolean methods |
| COMMAREA | Spring session / request DTO |
