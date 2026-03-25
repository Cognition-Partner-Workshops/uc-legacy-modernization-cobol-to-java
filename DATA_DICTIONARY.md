# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo | **Source:** COBOL Copybook PIC Clause Analysis

---

## Overview

This dictionary catalogs every business data entity defined in the CardDemo copybooks. Each field is mapped from its COBOL PIC clause to a business-friendly type, with notes on validation rules and 88-level condition names.

**Type Legend:**

| COBOL PIC | Business Type | Notes |
|-----------|---------------|-------|
| `PIC X(n)` | Text (n chars) | Alphanumeric |
| `PIC 9(n)` | Integer (n digits) | Unsigned numeric |
| `PIC S9(n)V99` | Decimal (n.2) | Signed with 2 decimal places |
| `PIC S9(n) COMP` | Binary Integer | Computational |
| `PIC S9(n) COMP-3` | Packed Decimal | Efficient storage |

---

## 1. Account Master (CVACT01Y)

**VSAM File:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | **Record Length:** 300 bytes | **Key:** Account ID (11 digits, position 0)

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 01 | ACCT-DATA | -- | Record | Account master record |
| 05 | ACCT-ID | 9(11) | Account Number | Unique account identifier (primary key) |
| 05 | ACCT-ACTIVE-STATUS | X(01) | Status Code | Account active status flag |
| 05 | ACCT-CURR-BAL | S9(10)V99 | Currency (10.2) | Current account balance |
| 05 | ACCT-CREDIT-LIMIT | S9(10)V99 | Currency (10.2) | Credit limit for the account |
| 05 | ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | Currency (10.2) | Cash advance credit limit |
| 05 | ACCT-OPEN-DATE | X(10) | Date (YYYY-MM-DD) | Account opening date |
| 05 | ACCT-EXPIRAION-DATE | X(10) | Date (YYYY-MM-DD) | Account expiration date |
| 05 | ACCT-REISSUE-DATE | X(10) | Date (YYYY-MM-DD) | Last card reissue date |
| 05 | ACCT-CURR-CYC-CREDIT | S9(10)V99 | Currency (10.2) | Current cycle credit amount |
| 05 | ACCT-CURR-CYC-DEBIT | S9(10)V99 | Currency (10.2) | Current cycle debit amount |
| 05 | ACCT-ADDR-ZIP | X(10) | Postal Code | Account holder ZIP code |
| 05 | ACCT-GROUP-ID | X(10) | Group Code | Account group for rate disclosure |
| 05 | FILLER | X(178) | Padding | Reserved space |

**Business Rules:**
- Account ID is the VSAM primary key used across all account lookups
- Balance fields are signed to allow both credit and debit positions
- Group ID links to Disclosure Group (CVTRA02Y) for interest rate determination

---

## 2. Card Master (CVACT02Y)

**VSAM File:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` | **Record Length:** 150 bytes | **Key:** Card Number (16 chars, position 0)

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 01 | CARD-RECORD | -- | Record | Card master record |
| 05 | CARD-NUM | X(16) | Card Number | 16-digit credit card number (primary key) |
| 05 | CARD-ACCT-ID | 9(11) | Account Number | Parent account ID (FK to Account Master) |
| 05 | CARD-CVV-CD | 9(03) | CVV Code | Card verification value |
| 05 | CARD-EMBOSSED-NAME | X(50) | Person Name | Name embossed on card |
| 05 | CARD-EXPIRAION-DATE | X(10) | Date (YYYY-MM-DD) | Card expiration date |
| 05 | CARD-ACTIVE-STATUS | X(01) | Status Code | Card active/inactive status |
| 05 | FILLER | X(59) | Padding | Reserved space |

**Business Rules:**
- Multiple cards can belong to one account (one-to-many via CARD-ACCT-ID)
- Card number is the VSAM primary key
- Cross-referenced via CARDXREF for account-based lookups

---

## 3. Card Cross-Reference (CVACT03Y)

**VSAM File:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | **Record Length:** 50 bytes | **Key:** Card Number (16 chars), **Alt Index Key:** Account ID (11 digits, position 25)

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 01 | CARD-XREF-RECORD | -- | Record | Card cross-reference record |
| 05 | XREF-CARD-NUM | X(16) | Card Number | Card number (primary key) |
| 05 | XREF-CUST-ID | 9(09) | Customer ID | Customer ID associated with card |
| 05 | XREF-ACCT-ID | 9(11) | Account Number | Account ID associated with card |
| 05 | FILLER | X(14) | Padding | Reserved space |

**Business Rules:**
- Central lookup table linking cards to customers and accounts
- Alternate index on ACCT-ID enables account-based card searches
- Used by transaction posting, interest calc, and statement generation

---

## 4. Customer Master (CVCUS01Y)

**VSAM File:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` | **Record Length:** 500 bytes | **Key:** Customer ID (9 digits, position 0)

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 01 | CUSTOMER-RECORD | -- | Record | Customer master record |
| 05 | CUST-ID | 9(09) | Customer ID | Unique customer identifier (primary key) |
| 05 | CUST-FIRST-NAME | X(25) | Person Name | Customer first name |
| 05 | CUST-MIDDLE-NAME | X(25) | Person Name | Customer middle name |
| 05 | CUST-LAST-NAME | X(25) | Person Name | Customer last name |
| 05 | CUST-ADDR-LINE-1 | X(50) | Address | Street address line 1 |
| 05 | CUST-ADDR-LINE-2 | X(50) | Address | Street address line 2 |
| 05 | CUST-ADDR-LINE-3 | X(50) | Address | Street address line 3 |
| 05 | CUST-ADDR-STATE-CD | X(02) | State Code | US state code |
| 05 | CUST-ADDR-COUNTRY-CD | X(03) | Country Code | Country code |
| 05 | CUST-ADDR-ZIP | X(10) | Postal Code | ZIP/postal code |
| 05 | CUST-PHONE-NUM-1 | X(15) | Phone | Primary phone number |
| 05 | CUST-PHONE-NUM-2 | X(15) | Phone | Secondary phone number |
| 05 | CUST-SSN | 9(09) | SSN | Social Security Number |
| 05 | CUST-GOVT-ISSUED-ID | X(20) | Government ID | Government-issued identification |
| 05 | CUST-DOB-YYYYMMDD | X(10) | Date (YYYY-MM-DD) | Date of birth |
| 05 | CUST-EFT-ACCOUNT-ID | X(10) | Bank Account | EFT/ACH account identifier |
| 05 | CUST-PRI-CARD-HOLDER-IND | X(01) | Flag (Y/N) | Primary cardholder indicator |
| 05 | CUST-FICO-CREDIT-SCORE | 9(03) | Credit Score | FICO credit score (300-850) |
| 05 | FILLER | X(168) | Padding | Reserved space |

**Business Rules:**
- Customer ID links to Card Cross-Reference (CVACT03Y.XREF-CUST-ID)
- SSN and FICO score are sensitive PII fields requiring encryption in target system
- EFT Account ID enables electronic fund transfers / bill payments

---

## 5. Transaction Record (CVTRA05Y)

**VSAM File:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | **Record Length:** 350 bytes | **Key:** Transaction ID (16 chars, position 0)

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 01 | TRAN-RECORD | -- | Record | Transaction record |
| 05 | TRAN-ID | X(16) | Transaction ID | Unique transaction identifier (primary key) |
| 05 | TRAN-TYPE-CD | X(02) | Type Code | Transaction type (FK to Transaction Type) |
| 05 | TRAN-CAT-CD | 9(04) | Category Code | Transaction category code |
| 05 | TRAN-SOURCE | X(10) | Source Code | Transaction origination source |
| 05 | TRAN-DESC | X(100) | Description | Free-text transaction description |
| 05 | TRAN-AMT | S9(09)V99 | Currency (9.2) | Transaction amount (signed) |
| 05 | TRAN-MERCHANT-ID | 9(09) | Merchant ID | Merchant identifier |
| 05 | TRAN-MERCHANT-NAME | X(50) | Merchant Name | Merchant business name |
| 05 | TRAN-MERCHANT-CITY | X(50) | City | Merchant city |
| 05 | TRAN-MERCHANT-ZIP | X(10) | Postal Code | Merchant ZIP code |
| 05 | TRAN-CARD-NUM | X(16) | Card Number | Card used for transaction |
| 05 | TRAN-ORIG-TS | X(26) | Timestamp | Transaction origination timestamp |
| 05 | TRAN-PROC-TS | X(26) | Timestamp | Transaction processing timestamp |
| 05 | FILLER | X(20) | Padding | Reserved space |

**Business Rules:**
- Transaction Type links to CVTRA03Y; Transaction Category links to CVTRA04Y
- Amount is signed: positive = purchase/debit, negative = credit/refund
- Card number links back to Card Master and Cross-Reference

---

## 6. Daily Transaction (CVTRA06Y)

**File:** `AWS.M2.CARDDEMO.DALYTRAN.PS` | **Record Length:** 350 bytes

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 01 | DALYTRAN-RECORD | -- | Record | Daily pending transaction |
| 05 | DALYTRAN-ID | X(16) | Transaction ID | Daily transaction identifier |
| 05 | DALYTRAN-TYPE-CD | X(02) | Type Code | Transaction type code |
| 05 | DALYTRAN-CAT-CD | 9(04) | Category Code | Transaction category code |
| 05 | DALYTRAN-SOURCE | X(10) | Source Code | Transaction source |
| 05 | DALYTRAN-DESC | X(100) | Description | Transaction description |
| 05 | DALYTRAN-AMT | S9(09)V99 | Currency (9.2) | Transaction amount |
| 05 | DALYTRAN-MERCHANT-ID | 9(09) | Merchant ID | Merchant identifier |
| 05 | DALYTRAN-MERCHANT-NAME | X(50) | Merchant Name | Merchant name |
| 05 | DALYTRAN-MERCHANT-CITY | X(50) | City | Merchant city |
| 05 | DALYTRAN-MERCHANT-ZIP | X(10) | Postal Code | Merchant ZIP code |
| 05 | DALYTRAN-CARD-NUM | X(16) | Card Number | Card number |
| 05 | DALYTRAN-ORIG-TS | X(26) | Timestamp | Origination timestamp |
| 05 | DALYTRAN-PROC-TS | X(26) | Timestamp | Processing timestamp |
| 05 | FILLER | X(20) | Padding | Reserved space |

**Business Rules:**
- Identical layout to Transaction Record (CVTRA05Y) but stored in flat file
- Represents unposted daily transactions awaiting batch posting via CBTRN02C
- After posting, records move from DALYTRAN to TRANSACT master

---

## 7. Transaction Category Balance (CVTRA01Y)

**VSAM File:** `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` | **Record Length:** 50 bytes | **Key:** Account ID + Type + Category

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 01 | TRAN-CAT-BAL-RECORD | -- | Record | Category balance record |
| 05 | TRAN-CAT-KEY | -- | Composite Key | |
| 10 | TRANCAT-ACCT-ID | 9(11) | Account Number | Account identifier |
| 10 | TRANCAT-TYPE-CD | X(02) | Type Code | Transaction type code |
| 10 | TRANCAT-CD | 9(04) | Category Code | Transaction category code |
| 05 | TRAN-CAT-BAL | S9(09)V99 | Currency (9.2) | Running balance for this category |
| 05 | FILLER | X(22) | Padding | Reserved space |

**Business Rules:**
- Tracks running balance per account/type/category combination
- Updated during transaction posting (CBTRN02C)
- Used for interest calculation (CBACT04C) - balance determines interest charges

---

## 8. Disclosure Group (CVTRA02Y)

**VSAM File:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` | **Record Length:** 50 bytes | **Key:** Group ID + Type + Category

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 01 | DIS-GROUP-RECORD | -- | Record | Disclosure group record |
| 05 | DIS-GROUP-KEY | -- | Composite Key | |
| 10 | DIS-ACCT-GROUP-ID | X(10) | Group Code | Account group identifier |
| 10 | DIS-TRAN-TYPE-CD | X(02) | Type Code | Transaction type code |
| 10 | DIS-TRAN-CAT-CD | 9(04) | Category Code | Transaction category code |
| 05 | DIS-INT-RATE | S9(04)V99 | Percentage (4.2) | Interest rate for this group/type/category |
| 05 | FILLER | X(28) | Padding | Reserved space |

**Business Rules:**
- Links account groups to interest rates by transaction type and category
- Account's GROUP-ID (from CVACT01Y) determines which disclosure rates apply
- Used exclusively by interest calculation batch (CBACT04C)

---

## 9. Transaction Type (CVTRA03Y)

**VSAM File:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` | **Record Length:** 60 bytes | **Key:** Type Code (2 chars)

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 01 | TRAN-TYPE-RECORD | -- | Record | Transaction type reference |
| 05 | TRAN-TYPE | X(02) | Type Code | Transaction type code (primary key) |
| 05 | TRAN-TYPE-DESC | X(50) | Description | Type description (e.g., "Purchase", "Cash Advance") |
| 05 | FILLER | X(08) | Padding | Reserved space |

---

## 10. Transaction Category (CVTRA04Y)

**VSAM File:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` | **Record Length:** 60 bytes | **Key:** Type Code + Category Code

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 01 | TRAN-CAT-RECORD | -- | Record | Transaction category reference |
| 05 | TRAN-CAT-KEY | -- | Composite Key | |
| 10 | TRAN-TYPE-CD | X(02) | Type Code | Parent transaction type code |
| 10 | TRAN-CAT-CD | 9(04) | Category Code | Category code within type |
| 05 | TRAN-CAT-TYPE-DESC | X(50) | Description | Category description |
| 05 | FILLER | X(04) | Padding | Reserved space |

---

## 11. User Security Record (CSUSR01Y)

**VSAM File:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` | **Record Length:** 80 bytes | **Key:** User ID (8 chars)

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 01 | SEC-USER-DATA | -- | Record | User security record |
| 05 | SEC-USR-ID | X(08) | User ID | Login user identifier (primary key) |
| 05 | SEC-USR-FNAME | X(20) | Person Name | User first name |
| 05 | SEC-USR-LNAME | X(20) | Person Name | User last name |
| 05 | SEC-USR-PWD | X(08) | Password | User password (plaintext) |
| 05 | SEC-USR-TYPE | X(01) | Role Code | User type: A=Admin, U=Regular User |
| 05 | SEC-USR-FILLER | X(23) | Padding | Reserved space |

**88-Level Conditions:**
- `SEC-USR-TYPE-ADMIN` VALUE 'A' - Administrator role
- `SEC-USR-TYPE-USER` VALUE 'U' - Regular user role

**Business Rules:**
- Passwords stored in plaintext (migration target should use hashing)
- User type controls menu routing: Admin → COADM01C, User → COMEN01C
- Default accounts: ADMIN001/PASSWORD (Admin), USER0001/PASSWORD (User)

---

## 12. Statement Transaction (COSTM01)

**Record Length:** 350 bytes | **Key:** Card Number (16) + Transaction ID (16)

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 01 | TRNX-RECORD | -- | Record | Statement transaction record |
| 05 | TRNX-KEY | -- | Composite Key | |
| 10 | TRNX-CARD-NUM | X(16) | Card Number | Card number (first key part) |
| 10 | TRNX-ID | X(16) | Transaction ID | Transaction ID (second key part) |
| 05 | TRNX-REST | -- | Group | Remaining fields |
| 10 | TRNX-TYPE-CD | X(02) | Type Code | Transaction type |
| 10 | TRNX-CAT-CD | 9(04) | Category Code | Transaction category |
| 10 | TRNX-SOURCE | X(10) | Source Code | Transaction source |
| 10 | TRNX-DESC | X(100) | Description | Description |
| 10 | TRNX-AMT | S9(09)V99 | Currency (9.2) | Amount |
| 10 | TRNX-MERCHANT-ID | 9(09) | Merchant ID | Merchant ID |
| 10 | TRNX-MERCHANT-NAME | X(50) | Merchant Name | Merchant name |
| 10 | TRNX-MERCHANT-CITY | X(50) | City | Merchant city |
| 10 | TRNX-MERCHANT-ZIP | X(10) | Postal Code | Merchant ZIP |
| 10 | TRNX-ORIG-TS | X(26) | Timestamp | Origination timestamp |
| 10 | TRNX-PROC-TS | X(26) | Timestamp | Processing timestamp |
| 10 | FILLER | X(20) | Padding | Reserved |

**Business Rules:**
- Re-keyed version of CVTRA05Y with card number as primary sort key
- Created by CREASTMT JCL (SORT step) for statement generation
- Enables grouping transactions by card for per-card statements

---

## 13. Export Record (CVEXPORT)

**File:** Sequential export file | **Used by:** CBEXPORT, CBIMPORT

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 01 | EXPORT-RECORD | -- | Record | Combined export record |
| 05 | EXP-RECORD-TYPE | X(01) | Record Type | C=Customer, A=Account, X=Xref, T=Transaction, R=Card |
| 05 | EXP-DATA | -- | Union | Record-type-specific data |
| -- | EXP-CUST-* | (CVCUS01Y layout) | Customer | Customer fields when type=C |
| -- | EXP-ACCT-* | (CVACT01Y layout) | Account | Account fields when type=A |
| -- | EXP-CARD-* | (CVACT02Y layout) | Card | Card fields when type=R |
| -- | EXP-XREF-* | (CVACT03Y layout) | Cross-Ref | Cross-reference fields when type=X |
| -- | EXP-TRAN-* | (CVTRA05Y layout) | Transaction | Transaction fields when type=T |

---

## 14. Common Communication Area (COCOM01Y)

**Used by:** All online CICS programs via DFHCOMMAREA

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 01 | CARDDEMO-COMMAREA | -- | Record | Inter-program communication area |
| 05 | CDEMO-FROM-TRANID | X(04) | Transaction ID | Calling transaction ID |
| 05 | CDEMO-FROM-PROGRAM | X(08) | Program Name | Calling program name |
| 05 | CDEMO-TO-TRANID | X(04) | Transaction ID | Target transaction ID |
| 05 | CDEMO-TO-PROGRAM | X(08) | Program Name | Target program name |
| 05 | CDEMO-PGM-CONTEXT | 9(01) | Context Code | Program context/mode |
| 05 | CDEMO-CUST-ID | 9(09) | Customer ID | Current customer ID |
| 05 | CDEMO-ACCT-ID | 9(11) | Account Number | Current account ID |
| 05 | CDEMO-CARD-NUM | X(16) | Card Number | Current card number |
| 05 | CDEMO-MORE-DATA | X(01) | Flag (Y/N) | More data available indicator |
| 05 | CDEMO-LAST-MAP | X(07) | Map Name | Last BMS map sent |
| 05 | CDEMO-LAST-MAPSET | X(07) | Map Set | Last BMS mapset used |
| 05 | CDEMO-USER-* | (various) | User Info | Current user session data |

**Business Rules:**
- Passed between all CICS programs via EXEC CICS XCTL / RETURN
- Contains navigation state (from/to program) and current entity context
- Enables stateful navigation in a stateless 3270 terminal environment

---

## 15. Authorization Module Entities

### 15.1 Pending Authorization Detail (CIPAUDTY)

**Storage:** IMS Database | **Segment Type:** Detail

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 05 | PA-AUTHORIZATION-KEY | -- | Composite Key | |
| 10 | PA-AUTH-DATE-9C | S9(05) COMP-3 | Packed Date | Authorization date (packed) |
| 10 | PA-AUTH-TIME-9C | S9(09) COMP-3 | Packed Time | Authorization time (packed) |
| 05 | PA-CARD-NUM | X(16) | Card Number | Card number |
| 05 | PA-AUTH-TYPE | X(04) | Auth Type | Authorization type |
| 05 | PA-MESSAGE-TYPE | X(06) | Message Type | ISO message type |
| 05 | PA-MESSAGE-SOURCE | X(06) | Source | Message origination |
| 05 | PA-AUTH-ID-CODE | X(06) | Auth Code | Authorization ID code |
| 05 | PA-AUTH-RESP-CODE | X(02) | Response Code | 00=Approved |
| 05 | PA-AUTH-RESP-REASON | X(04) | Reason Code | Response reason |
| 05 | PA-TRANSACTION-AMT | S9(10)V99 COMP-3 | Currency (10.2) | Requested amount |
| 05 | PA-APPROVED-AMT | S9(10)V99 COMP-3 | Currency (10.2) | Approved amount |
| 05 | PA-MERCHANT-ID | X(15) | Merchant ID | Merchant identifier |
| 05 | PA-MERCHANT-NAME | X(22) | Merchant Name | Merchant name |
| 05 | PA-MERCHANT-CITY | X(13) | City | Merchant city |
| 05 | PA-MERCHANT-STATE | X(02) | State | Merchant state |
| 05 | PA-MERCHANT-ZIP | X(09) | Postal Code | Merchant ZIP |
| 05 | PA-TRANSACTION-ID | X(15) | Transaction ID | Transaction identifier |
| 05 | PA-MATCH-STATUS | X(01) | Status Code | P=Pending, D=Declined, E=Expired, M=Matched |
| 05 | PA-AUTH-FRAUD | X(01) | Fraud Flag | F=Fraud Confirmed, R=Fraud Removed |
| 05 | PA-FRAUD-RPT-DATE | X(08) | Date | Fraud report date |

### 15.2 Pending Authorization Summary (CIPAUSMY)

**Storage:** IMS Database | **Segment Type:** Summary (Parent)

| Level | Field Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| 05 | PA-ACCT-ID | S9(11) COMP-3 | Account Number | Account identifier |
| 05 | PA-CUST-ID | 9(09) | Customer ID | Customer identifier |
| 05 | PA-AUTH-STATUS | X(01) | Status | Authorization status |
| 05 | PA-ACCOUNT-STATUS | X(02) OCCURS 5 | Status Array | Account status codes (5 entries) |
| 05 | PA-CREDIT-LIMIT | S9(09)V99 COMP-3 | Currency (9.2) | Credit limit |
| 05 | PA-CASH-LIMIT | S9(09)V99 COMP-3 | Currency (9.2) | Cash limit |
| 05 | PA-CREDIT-BALANCE | S9(09)V99 COMP-3 | Currency (9.2) | Credit balance |
| 05 | PA-CASH-BALANCE | S9(09)V99 COMP-3 | Currency (9.2) | Cash balance |
| 05 | PA-APPROVED-AUTH-CNT | S9(04) COMP | Counter | Approved authorization count |
| 05 | PA-DECLINED-AUTH-CNT | S9(04) COMP | Counter | Declined authorization count |
| 05 | PA-APPROVED-AUTH-AMT | S9(09)V99 COMP-3 | Currency (9.2) | Total approved amount |
| 05 | PA-DECLINED-AUTH-AMT | S9(09)V99 COMP-3 | Currency (9.2) | Total declined amount |

---

## 16. Entity Relationship Summary

```
Customer (CVCUS01Y)
    |
    | 1:N via XREF-CUST-ID
    v
Card Cross-Reference (CVACT03Y) -----> Account (CVACT01Y)
    |                                       |
    | 1:1 via CARD-NUM                      | 1:N via ACCT-GROUP-ID
    v                                       v
Card Master (CVACT02Y)              Disclosure Group (CVTRA02Y)
    |                                       |
    | 1:N via TRAN-CARD-NUM                 | rates for
    v                                       v
Transaction (CVTRA05Y)              Category Balance (CVTRA01Y)
    |
    | typed by
    v
Transaction Type (CVTRA03Y) <---> Transaction Category (CVTRA04Y)

User Security (CSUSR01Y) -- standalone authentication table

[Authorization Module - IMS]
Auth Summary (CIPAUSMY) --1:N--> Auth Detail (CIPAUDTY)
```

---

## 17. VSAM File Catalog

| # | Dataset Name | Type | Key | Record Len | Business Entity |
|---|-------------|------|-----|------------|-----------------|
| 1 | ACCTDATA.VSAM.KSDS | KSDS | ACCT-ID (11,0) | 300 | Account Master |
| 2 | CARDDATA.VSAM.KSDS | KSDS | CARD-NUM (16,0) | 150 | Card Master |
| 3 | CARDXREF.VSAM.KSDS | KSDS | XREF-CARD-NUM (16,0) | 50 | Card Cross-Reference |
| 4 | CARDXREF.VSAM.AIX | AIX | XREF-ACCT-ID (11,25) | 50 | Card Xref (by Account) |
| 5 | CUSTDATA.VSAM.KSDS | KSDS | CUST-ID (9,0) | 500 | Customer Master |
| 6 | TRANSACT.VSAM.KSDS | KSDS | TRAN-ID (16,0) | 350 | Transaction Master |
| 7 | USRSEC.VSAM.KSDS | KSDS | SEC-USR-ID (8,0) | 80 | User Security |
| 8 | TCATBALF.VSAM.KSDS | KSDS | Composite (17,0) | 50 | Category Balance |
| 9 | TRANTYPE.VSAM.KSDS | KSDS | TRAN-TYPE (2,0) | 60 | Transaction Type |
| 10 | TRANCATG.VSAM.KSDS | KSDS | Composite (6,0) | 60 | Transaction Category |
| 11 | DISCGRP.VSAM.KSDS | KSDS | Composite (16,0) | 50 | Disclosure Group |
| 12 | DALYTRAN.PS | Sequential | -- | 350 | Daily Transactions |
