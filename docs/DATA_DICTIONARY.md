# Data Dictionary — CardDemo Mainframe System

> **Generated**: 2026-05-27 | **Source**: Copybook PIC clause analysis

## Overview

This document extracts business entities from COBOL copybook record definitions into a business-friendly format. Each entity corresponds to a VSAM dataset (or DB2 table) and represents a core business concept in the credit card management domain.

---

## 1. Account Entity

**Copybook**: `CVACT01Y.cpy` | **Record Length**: 300 bytes | **Storage**: VSAM KSDS

| Field | COBOL Name | PIC Clause | Data Type | Size | Business Description |
|:------|:-----------|:-----------|:----------|:-----|:---------------------|
| Account ID | ACCT-ID | PIC 9(11) | Numeric | 11 digits | Unique account identifier (primary key) |
| Active Status | ACCT-ACTIVE-STATUS | PIC X(01) | Alpha | 1 char | Account status flag (Y=Active, N=Closed) |
| Current Balance | ACCT-CURR-BAL | PIC S9(10)V99 | Signed Decimal | 12 digits, 2 dec | Outstanding balance on the account |
| Credit Limit | ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12 digits, 2 dec | Maximum credit extended |
| Cash Credit Limit | ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12 digits, 2 dec | Maximum cash advance allowed |
| Open Date | ACCT-OPEN-DATE | PIC X(10) | Date String | 10 chars | Date account was opened (YYYY-MM-DD) |
| Expiration Date | ACCT-EXPIRAION-DATE | PIC X(10) | Date String | 10 chars | Account expiration date |
| Reissue Date | ACCT-REISSUE-DATE | PIC X(10) | Date String | 10 chars | Date card was last reissued |
| Current Cycle Credit | ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Signed Decimal | 12 digits, 2 dec | Credits applied in current billing cycle |
| Current Cycle Debit | ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 | Signed Decimal | 12 digits, 2 dec | Debits applied in current billing cycle |
| Address ZIP | ACCT-ADDR-ZIP | PIC X(10) | Alpha | 10 chars | Account holder ZIP/postal code |
| Group ID | ACCT-GROUP-ID | PIC X(10) | Alpha | 10 chars | Disclosure/rate group assignment |
| Filler | FILLER | PIC X(178) | — | 178 bytes | Reserved for future use |

**Business Rules**:
- Primary key: `ACCT-ID`
- Balance = Credits − Debits applied over time
- `ACCT-GROUP-ID` links to Disclosure Group for interest rate determination

---

## 2. Card Entity

**Copybook**: `CVACT02Y.cpy` | **Record Length**: 150 bytes | **Storage**: VSAM KSDS

| Field | COBOL Name | PIC Clause | Data Type | Size | Business Description |
|:------|:-----------|:-----------|:----------|:-----|:---------------------|
| Card Number | CARD-NUM | PIC X(16) | Alpha | 16 chars | Credit card number (primary key) |
| Account ID | CARD-ACCT-ID | PIC 9(11) | Numeric | 11 digits | Owning account (FK → Account) |
| CVV Code | CARD-CVV-CD | PIC 9(03) | Numeric | 3 digits | Card verification value |
| Embossed Name | CARD-EMBOSSED-NAME | PIC X(50) | Alpha | 50 chars | Name printed on the physical card |
| Expiration Date | CARD-EXPIRAION-DATE | PIC X(10) | Date String | 10 chars | Card expiry date |
| Active Status | CARD-ACTIVE-STATUS | PIC X(01) | Alpha | 1 char | Card status (Y=Active, N=Inactive) |
| Filler | FILLER | PIC X(59) | — | 59 bytes | Reserved |

**Business Rules**:
- Primary key: `CARD-NUM`
- Each card belongs to exactly one account
- Multiple cards may reference the same account

---

## 3. Customer Entity

**Copybook**: `CVCUS01Y.cpy` | **Record Length**: 500 bytes | **Storage**: VSAM KSDS

| Field | COBOL Name | PIC Clause | Data Type | Size | Business Description |
|:------|:-----------|:-----------|:----------|:-----|:---------------------|
| Customer ID | CUST-ID | PIC 9(09) | Numeric | 9 digits | Unique customer identifier (primary key) |
| First Name | CUST-FIRST-NAME | PIC X(25) | Alpha | 25 chars | Legal first name |
| Middle Name | CUST-MIDDLE-NAME | PIC X(25) | Alpha | 25 chars | Legal middle name |
| Last Name | CUST-LAST-NAME | PIC X(25) | Alpha | 25 chars | Legal last name |
| Address Line 1 | CUST-ADDR-LINE-1 | PIC X(50) | Alpha | 50 chars | Primary address line |
| Address Line 2 | CUST-ADDR-LINE-2 | PIC X(50) | Alpha | 50 chars | Secondary address line |
| Address Line 3 | CUST-ADDR-LINE-3 | PIC X(50) | Alpha | 50 chars | Tertiary address line |
| State Code | CUST-ADDR-STATE-CD | PIC X(02) | Alpha | 2 chars | US state abbreviation |
| Country Code | CUST-ADDR-COUNTRY-CD | PIC X(03) | Alpha | 3 chars | ISO country code |
| ZIP Code | CUST-ADDR-ZIP | PIC X(10) | Alpha | 10 chars | ZIP/postal code |
| Phone Number 1 | CUST-PHONE-NUM-1 | PIC X(15) | Alpha | 15 chars | Primary phone |
| Phone Number 2 | CUST-PHONE-NUM-2 | PIC X(15) | Alpha | 15 chars | Secondary phone |
| SSN | CUST-SSN | PIC 9(09) | Numeric | 9 digits | Social Security Number |
| Government ID | CUST-GOVT-ISSUED-ID | PIC X(20) | Alpha | 20 chars | Government-issued ID number |
| Date of Birth | CUST-DOB-YYYY-MM-DD | PIC X(10) | Date String | 10 chars | Customer birth date |
| EFT Account ID | CUST-EFT-ACCOUNT-ID | PIC X(10) | Alpha | 10 chars | Electronic funds transfer account |
| Primary Cardholder | CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alpha | 1 char | Y=Primary, N=Authorized user |
| FICO Credit Score | CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric | 3 digits | Customer credit score (300-850) |
| Filler | FILLER | PIC X(168) | — | 168 bytes | Reserved |

**Business Rules**:
- Primary key: `CUST-ID`
- Linked to accounts via Cross-Reference (XREF)
- Contains PII: SSN, DOB, addresses

---

## 4. Card Cross-Reference Entity

**Copybook**: `CVACT03Y.cpy` | **Record Length**: 50 bytes | **Storage**: VSAM KSDS

| Field | COBOL Name | PIC Clause | Data Type | Size | Business Description |
|:------|:-----------|:-----------|:----------|:-----|:---------------------|
| Card Number | XREF-CARD-NUM | PIC X(16) | Alpha | 16 chars | Card number (primary key) |
| Customer ID | XREF-CUST-ID | PIC 9(09) | Numeric | 9 digits | Owning customer (FK → Customer) |
| Account ID | XREF-ACCT-ID | PIC 9(11) | Numeric | 11 digits | Associated account (FK → Account) |
| Filler | FILLER | PIC X(14) | — | 14 bytes | Reserved |

**Business Rules**:
- Primary key: `XREF-CARD-NUM`
- Provides the join between Customer ↔ Account ↔ Card
- One record per card issued

---

## 5. Transaction Entity

**Copybook**: `CVTRA05Y.cpy` | **Record Length**: 350 bytes | **Storage**: VSAM KSDS (online)

| Field | COBOL Name | PIC Clause | Data Type | Size | Business Description |
|:------|:-----------|:-----------|:----------|:-----|:---------------------|
| Transaction ID | TRAN-ID | PIC X(16) | Alpha | 16 chars | Unique transaction identifier (primary key) |
| Type Code | TRAN-TYPE-CD | PIC X(02) | Alpha | 2 chars | Transaction type (FK → Tran Type) |
| Category Code | TRAN-CAT-CD | PIC 9(04) | Numeric | 4 digits | Category classification |
| Source | TRAN-SOURCE | PIC X(10) | Alpha | 10 chars | Origination channel |
| Description | TRAN-DESC | PIC X(100) | Alpha | 100 chars | Transaction description |
| Amount | TRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11 digits, 2 dec | Transaction dollar amount |
| Merchant ID | TRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 digits | Merchant identifier |
| Merchant Name | TRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 chars | Merchant business name |
| Merchant City | TRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 chars | Merchant location city |
| Merchant ZIP | TRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 chars | Merchant postal code |
| Card Number | TRAN-CARD-NUM | PIC X(16) | Alpha | 16 chars | Card used (FK → Card) |
| Origination Timestamp | TRAN-ORIG-TS | PIC X(26) | Timestamp | 26 chars | When transaction occurred |
| Processing Timestamp | TRAN-PROC-TS | PIC X(26) | Timestamp | 26 chars | When transaction was posted |
| Filler | FILLER | PIC X(20) | — | 20 bytes | Reserved |

**Business Rules**:
- Primary key: `TRAN-ID`
- Has Alternate Index (AIX) for card-based lookups
- Signed amount: positive = purchase, negative = credit/return

---

## 6. Daily Transaction Entity

**Copybook**: `CVTRA06Y.cpy` | **Record Length**: 350 bytes | **Storage**: Sequential (batch input)

| Field | COBOL Name | PIC Clause | Data Type | Size | Business Description |
|:------|:-----------|:-----------|:----------|:-----|:---------------------|
| Transaction ID | DALYTRAN-ID | PIC X(16) | Alpha | 16 chars | Daily transaction ID |
| Type Code | DALYTRAN-TYPE-CD | PIC X(02) | Alpha | 2 chars | Transaction type code |
| Category Code | DALYTRAN-CAT-CD | PIC 9(04) | Numeric | 4 digits | Category code |
| Source | DALYTRAN-SOURCE | PIC X(10) | Alpha | 10 chars | Source channel |
| Description | DALYTRAN-DESC | PIC X(100) | Alpha | 100 chars | Description |
| Amount | DALYTRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11 digits, 2 dec | Dollar amount |
| Merchant ID | DALYTRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 digits | Merchant ID |
| Merchant Name | DALYTRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 chars | Merchant name |
| Merchant City | DALYTRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 chars | Merchant city |
| Merchant ZIP | DALYTRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 chars | Merchant ZIP |
| Card Number | DALYTRAN-CARD-NUM | PIC X(16) | Alpha | 16 chars | Card number |
| Origination TS | DALYTRAN-ORIG-TS | PIC X(26) | Timestamp | 26 chars | Original timestamp |
| Processing TS | DALYTRAN-PROC-TS | PIC X(26) | Timestamp | 26 chars | Processing timestamp |
| Filler | FILLER | PIC X(20) | — | 20 bytes | Reserved |

**Business Rules**:
- Batch input file for daily transaction processing (POSTTRAN job)
- Same structure as online Transaction for merge compatibility
- Posted into VSAM Transaction Master after batch processing

---

## 7. Transaction Category Balance Entity

**Copybook**: `CVTRA01Y.cpy` | **Record Length**: 50 bytes | **Storage**: VSAM KSDS

| Field | COBOL Name | PIC Clause | Data Type | Size | Business Description |
|:------|:-----------|:-----------|:----------|:-----|:---------------------|
| Account ID | TRANCAT-ACCT-ID | PIC 9(11) | Numeric | 11 digits | Account (composite key part 1) |
| Type Code | TRANCAT-TYPE-CD | PIC X(02) | Alpha | 2 chars | Transaction type (composite key part 2) |
| Category Code | TRANCAT-CD | PIC 9(04) | Numeric | 4 digits | Category (composite key part 3) |
| Category Balance | TRAN-CAT-BAL | PIC S9(09)V99 | Signed Decimal | 11 digits, 2 dec | Running balance for this category |
| Filler | FILLER | PIC X(22) | — | 22 bytes | Reserved |

**Business Rules**:
- Composite key: `TRANCAT-ACCT-ID` + `TRANCAT-TYPE-CD` + `TRANCAT-CD`
- Tracks balances per transaction category per account
- Updated during interest calculation (INTCALC)

---

## 8. Disclosure Group Entity

**Copybook**: `CVTRA02Y.cpy` | **Record Length**: 50 bytes | **Storage**: VSAM KSDS

| Field | COBOL Name | PIC Clause | Data Type | Size | Business Description |
|:------|:-----------|:-----------|:----------|:-----|:---------------------|
| Account Group ID | DIS-ACCT-GROUP-ID | PIC X(10) | Alpha | 10 chars | Rate group (composite key part 1) |
| Transaction Type | DIS-TRAN-TYPE-CD | PIC X(02) | Alpha | 2 chars | Transaction type (composite key part 2) |
| Category Code | DIS-TRAN-CAT-CD | PIC 9(04) | Numeric | 4 digits | Category (composite key part 3) |
| Interest Rate | DIS-INT-RATE | PIC S9(04)V99 | Signed Decimal | 6 digits, 2 dec | Annual interest rate (%) |
| Filler | FILLER | PIC X(28) | — | 28 bytes | Reserved |

**Business Rules**:
- Composite key: Group + Type + Category
- Defines interest rates by account group and transaction classification
- Used by INTCALC (CBACT04C) for interest computation

---

## 9. Transaction Type Entity

**Copybook**: `CVTRA03Y.cpy` | **Record Length**: 60 bytes | **Storage**: VSAM KSDS / DB2

| Field | COBOL Name | PIC Clause | Data Type | Size | Business Description |
|:------|:-----------|:-----------|:----------|:-----|:---------------------|
| Type Code | TRAN-TYPE | PIC X(02) | Alpha | 2 chars | Transaction type code (primary key) |
| Description | TRAN-TYPE-DESC | PIC X(50) | Alpha | 50 chars | Human-readable type description |
| Filler | FILLER | PIC X(08) | — | 8 bytes | Reserved |

**Business Rules**:
- Reference/lookup table
- Examples: "SA" = Sale, "RT" = Return, "CA" = Cash Advance

---

## 10. Transaction Category Entity

**Copybook**: `CVTRA04Y.cpy` | **Record Length**: 60 bytes | **Storage**: VSAM KSDS / DB2

| Field | COBOL Name | PIC Clause | Data Type | Size | Business Description |
|:------|:-----------|:-----------|:----------|:-----|:---------------------|
| Type Code | TRAN-TYPE-CD | PIC X(02) | Alpha | 2 chars | Parent type (composite key part 1) |
| Category Code | TRAN-CAT-CD | PIC 9(04) | Numeric | 4 digits | Category code (composite key part 2) |
| Description | TRAN-CAT-TYPE-DESC | PIC X(50) | Alpha | 50 chars | Category description |
| Filler | FILLER | PIC X(04) | — | 4 bytes | Reserved |

**Business Rules**:
- Composite key: `TRAN-TYPE-CD` + `TRAN-CAT-CD`
- Sub-classification within a transaction type
- Examples: Under "SA" (Sale): 0001=Retail, 0002=Online, etc.

---

## 11. User Security Entity

**Copybook**: `CSUSR01Y.cpy` | **Record Length**: 80 bytes | **Storage**: VSAM KSDS

| Field | COBOL Name | PIC Clause | Data Type | Size | Business Description |
|:------|:-----------|:-----------|:----------|:-----|:---------------------|
| User ID | SEC-USR-ID | PIC X(08) | Alpha | 8 chars | Login identifier (primary key) |
| First Name | SEC-USR-FNAME | PIC X(20) | Alpha | 20 chars | User first name |
| Last Name | SEC-USR-LNAME | PIC X(20) | Alpha | 20 chars | User last name |
| Password | SEC-USR-PWD | PIC X(08) | Alpha | 8 chars | User password (plaintext) |
| User Type | SEC-USR-TYPE | PIC X(01) | Alpha | 1 char | A=Admin, U=Regular User |
| Filler | SEC-USR-FILLER | PIC X(23) | — | 23 bytes | Reserved |

**Business Rules**:
- Primary key: `SEC-USR-ID`
- Type determines menu access (Admin vs User functions)
- Password stored in plaintext (legacy pattern — security risk)

---

## 12. Branch Migration Export Entity

**Copybook**: `CVEXPORT.cpy` | **Record Length**: 500 bytes | **Storage**: Sequential File

| Field | COBOL Name | PIC Clause | Data Type | Size | Business Description |
|:------|:-----------|:-----------|:----------|:-----|:---------------------|
| Record Type | EXPORT-REC-TYPE | PIC X(1) | Alpha | 1 char | C=Customer, A=Account, T=Transaction, X=Xref |
| Timestamp | EXPORT-TIMESTAMP | PIC X(26) | Timestamp | 26 chars | Export timestamp |
| Sequence Number | EXPORT-SEQUENCE-NUM | PIC 9(9) COMP | Binary | 4 bytes | Record sequence within export |
| Branch ID | EXPORT-BRANCH-ID | PIC X(4) | Alpha | 4 chars | Source branch identifier |
| Region Code | EXPORT-REGION-CODE | PIC X(5) | Alpha | 5 chars | Geographic region code |
| Record Data | EXPORT-RECORD-DATA | PIC X(460) | Alpha | 460 bytes | Polymorphic payload (REDEFINES) |

**Sub-Structures** (via REDEFINES on EXPORT-RECORD-DATA):
- `EXPORT-CUSTOMER-DATA` — Customer fields with COMP packed IDs and OCCURS arrays
- `EXPORT-ACCOUNT-DATA` — Account fields with COMP-3 balances
- `EXPORT-TRANSACTION-DATA` — Transaction fields with packed decimals
- `EXPORT-XREF-DATA` — Cross-reference with card/account/customer links

**Business Rules**:
- Multi-record-type sequential file for branch consolidation/migration
- Uses advanced COBOL features: REDEFINES, OCCURS, COMP, COMP-3
- Designed for inter-branch data transfer

---

## 13. Communication Area (Commarea)

**Copybook**: `COCOM01Y.cpy` | **Storage**: CICS Commarea (in-memory)

| Field | COBOL Name | PIC Clause | Data Type | Size | Business Description |
|:------|:-----------|:-----------|:----------|:-----|:---------------------|
| From Transaction | CDEMO-FROM-TRANID | PIC X(04) | Alpha | 4 chars | Calling transaction ID |
| From Program | CDEMO-FROM-PROGRAM | PIC X(08) | Alpha | 8 chars | Calling program name |
| To Transaction | CDEMO-TO-TRANID | PIC X(04) | Alpha | 4 chars | Target transaction ID |
| To Program | CDEMO-TO-PROGRAM | PIC X(08) | Alpha | 8 chars | Target program name |
| User ID | CDEMO-USER-ID | PIC X(08) | Alpha | 8 chars | Logged-in user ID |
| User Type | CDEMO-USER-TYPE | PIC X(01) | Alpha | 1 char | A=Admin, U=User |
| Program Context | CDEMO-PGM-CONTEXT | PIC 9(01) | Numeric | 1 digit | 0=Enter, 1=Reenter |
| Customer ID | CDEMO-CUST-ID | PIC 9(09) | Numeric | 9 digits | Current customer context |
| Customer Name | CDEMO-CUST-FNAME/MNAME/LNAME | PIC X(25) each | Alpha | 75 chars | Customer name fields |
| Account ID | CDEMO-ACCT-ID | PIC 9(11) | Numeric | 11 digits | Current account context |
| Account Status | CDEMO-ACCT-STATUS | PIC X(01) | Alpha | 1 char | Account status |
| Card Number | CDEMO-CARD-NUM | PIC 9(16) | Numeric | 16 digits | Current card context |
| Last Map | CDEMO-LAST-MAP | PIC X(7) | Alpha | 7 chars | Previous BMS map name |
| Last Mapset | CDEMO-LAST-MAPSET | PIC X(7) | Alpha | 7 chars | Previous BMS mapset |

**Business Rules**:
- Passed between all CICS programs via EXEC CICS XCTL/RETURN
- Maintains session state across screen transitions
- User type determines which menu options are available

---

## Entity Relationship Summary

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────┐
│   Customer   │◄─────►│  Cross-Reference │◄─────►│   Account    │
│  CVCUS01Y    │  1:N  │    CVACT03Y      │  N:1  │  CVACT01Y    │
│  PK: CUST-ID │       │  PK: XREF-CARD   │       │  PK: ACCT-ID │
└──────────────┘       └────────┬─────────┘       └──────┬───────┘
                                │                         │
                                │ 1:1                     │ 1:N
                                ▼                         ▼
                       ┌──────────────┐        ┌─────────────────────┐
                       │     Card     │        │   Tran Cat Balance  │
                       │   CVACT02Y   │        │      CVTRA01Y       │
                       │ PK: CARD-NUM │        │ PK: ACCT+TYPE+CAT   │
                       └──────┬───────┘        └─────────────────────┘
                              │                          ▲
                              │ 1:N                      │ references
                              ▼                          │
                       ┌──────────────┐        ┌─────────────────────┐
                       │ Transaction  │───────►│  Disclosure Group   │
                       │   CVTRA05Y   │  via   │      CVTRA02Y       │
                       │ PK: TRAN-ID  │ type   │ PK: GRP+TYPE+CAT    │
                       └──────────────┘        └─────────────────────┘
                              │
                              │ FK
                              ▼
                 ┌────────────────────────┐
                 │  Transaction Type      │
                 │      CVTRA03Y          │
                 │  PK: TRAN-TYPE         │
                 └────────────┬───────────┘
                              │ 1:N
                              ▼
                 ┌────────────────────────┐
                 │  Transaction Category  │
                 │      CVTRA04Y          │
                 │  PK: TYPE-CD + CAT-CD  │
                 └────────────────────────┘
```

---

## Dataset Mapping

| Business Entity | VSAM Dataset Name | Key | Format |
|:----------------|:------------------|:----|:-------|
| Account | AWS.M2.CARDDEMO.ACCTDATA | ACCT-ID | FB 300 |
| Card | AWS.M2.CARDDEMO.CARDDATA | CARD-NUM | FB 150 |
| Customer | AWS.M2.CARDDEMO.CUSTDATA | CUST-ID | FB 500 |
| Cross-Reference | AWS.M2.CARDDEMO.CARDXREF | XREF-CARD-NUM | FB 50 |
| Transaction (online) | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | TRAN-ID | FB 350 |
| Daily Transaction (batch) | AWS.M2.CARDDEMO.DALYTRAN.PS | Sequential | FB 350 |
| Disclosure Group | AWS.M2.CARDDEMO.DISCGRP | Composite | FB 50 |
| Transaction Category Balance | AWS.M2.CARDDEMO.TCATBALF | Composite | FB 50 |
| Transaction Category | AWS.M2.CARDDEMO.TRANCATG | Composite | FB 60 |
| Transaction Type | AWS.M2.CARDDEMO.TRANTYPE | TRAN-TYPE | FB 60 |
| User Security | AWS.M2.CARDDEMO.USRSEC | SEC-USR-ID | FB 80 |
