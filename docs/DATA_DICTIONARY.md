# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** COBOL Copybook PIC Clause Analysis  
> **Purpose:** Business-friendly reference of all data entities, fields, types, and sizes extracted from the CardDemo COBOL copybooks.

---

## Table of Contents

1. [Account Entity](#1-account-entity)
2. [Card Entity](#2-card-entity)
3. [Customer Entity](#3-customer-entity)
4. [Card Cross-Reference Entity](#4-card-cross-reference-entity)
5. [Transaction Entity (Online)](#5-transaction-entity-online)
6. [Daily Transaction Entity](#6-daily-transaction-entity)
7. [Transaction Type Entity](#7-transaction-type-entity)
8. [Transaction Category Entity](#8-transaction-category-entity)
9. [Transaction Category Balance Entity](#9-transaction-category-balance-entity)
10. [Disclosure Group Entity](#10-disclosure-group-entity)
11. [User Security Entity](#11-user-security-entity)
12. [Application Communication Area (COMMAREA)](#12-application-communication-area)
13. [Export Record (Multi-Entity)](#13-export-record-multi-entity)
14. [Statement Transaction Record](#14-statement-transaction-record)
15. [Transaction Report Layout](#15-transaction-report-layout)
16. [Date/Time Working Storage](#16-datetime-working-storage)
17. [Date Validation Working Storage](#17-date-validation-working-storage)
18. [Menu Option Structures](#18-menu-option-structures)
19. [Card Work Areas / Navigation](#19-card-work-areas--navigation)
20. [Authorization Module Entities (Optional)](#20-authorization-module-entities-optional)
21. [VSAM File Catalog](#21-vsam-file-catalog)
22. [Type Mapping Reference](#22-type-mapping-reference)

---

## 1. Account Entity

**Copybook:** `CVACT01Y.cpy` | **Record Name:** `ACCOUNT-RECORD` | **Record Length:** 300 bytes  
**VSAM File:** Account Master (KSDS, key = ACCT-ID)  
**Used By:** COACTVWC, COACTUPC, CBACT01C, CBACT04C, CBSTM03A, COBIL00C, CBEXPORT, CBIMPORT, COPAUA0C, COPAUS0C

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | ACCT-ID | PIC 9(11) | Numeric | 11 digits | **Primary Key.** Unique account identifier |
| 2 | ACCT-ACTIVE-STATUS | PIC X(01) | Alpha | 1 char | Account status flag (A=Active, I=Inactive) |
| 3 | ACCT-CURR-BAL | PIC S9(10)V99 | Signed Decimal | 12 digits, 2 dec | Current account balance |
| 4 | ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12 digits, 2 dec | Credit limit for the account |
| 5 | ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12 digits, 2 dec | Cash advance credit limit |
| 6 | ACCT-OPEN-DATE | PIC X(10) | Date String | 10 chars | Account opening date (YYYY-MM-DD) |
| 7 | ACCT-EXPIRAION-DATE | PIC X(10) | Date String | 10 chars | Account expiration date (YYYY-MM-DD) |
| 8 | ACCT-REISSUE-DATE | PIC X(10) | Date String | 10 chars | Last card reissue date |
| 9 | ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Signed Decimal | 12 digits, 2 dec | Current billing cycle credits |
| 10 | ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 | Signed Decimal | 12 digits, 2 dec | Current billing cycle debits |
| 11 | ACCT-ADDR-ZIP | PIC X(10) | Alpha | 10 chars | Account holder ZIP/postal code |
| 12 | ACCT-GROUP-ID | PIC X(10) | Alpha | 10 chars | Disclosure group assignment |
| 13 | FILLER | PIC X(178) | Filler | 178 chars | Reserved for future use |

**Business Rules:**
- Balance = Credits - Debits accumulated over billing cycles
- ACCT-GROUP-ID links to Disclosure Group for interest rate determination
- ACCT-EXPIRAION-DATE drives card renewal processing

---

## 2. Card Entity

**Copybook:** `CVACT02Y.cpy` | **Record Name:** `CARD-RECORD` | **Record Length:** 150 bytes  
**VSAM File:** Card Master (KSDS, key = CARD-NUM)  
**Used By:** COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBEXPORT, CBIMPORT

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | CARD-NUM | PIC X(16) | Alpha | 16 chars | **Primary Key.** Credit card number |
| 2 | CARD-ACCT-ID | PIC 9(11) | Numeric | 11 digits | **FK → Account.** Owning account ID |
| 3 | CARD-CVV-CD | PIC 9(03) | Numeric | 3 digits | Card verification value (CVV) |
| 4 | CARD-EMBOSSED-NAME | PIC X(50) | Alpha | 50 chars | Name embossed on physical card |
| 5 | CARD-EXPIRAION-DATE | PIC X(10) | Date String | 10 chars | Card expiration date |
| 6 | CARD-ACTIVE-STATUS | PIC X(01) | Alpha | 1 char | Card status (Y=Active, N=Inactive) |
| 7 | FILLER | PIC X(59) | Filler | 59 chars | Reserved for future use |

**Business Rules:**
- Multiple cards can exist per account (1:N relationship)
- CARD-ACCT-ID establishes the card-to-account relationship
- CVV is stored in clear text (security concern for modernization)

---

## 3. Customer Entity

**Copybook:** `CVCUS01Y.cpy` | **Record Name:** `CUSTOMER-RECORD` | **Record Length:** 500 bytes  
**VSAM File:** Customer Master (KSDS, key = CUST-ID)  
**Used By:** CBCUS01C, COACTUPC, CBEXPORT, CBIMPORT, COPAUA0C, COPAUS0C

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | CUST-ID | PIC 9(09) | Numeric | 9 digits | **Primary Key.** Customer identifier |
| 2 | CUST-FIRST-NAME | PIC X(25) | Alpha | 25 chars | Customer first name |
| 3 | CUST-MIDDLE-NAME | PIC X(25) | Alpha | 25 chars | Customer middle name |
| 4 | CUST-LAST-NAME | PIC X(25) | Alpha | 25 chars | Customer last name |
| 5 | CUST-ADDR-LINE-1 | PIC X(50) | Alpha | 50 chars | Address line 1 |
| 6 | CUST-ADDR-LINE-2 | PIC X(50) | Alpha | 50 chars | Address line 2 |
| 7 | CUST-ADDR-LINE-3 | PIC X(50) | Alpha | 50 chars | Address line 3 |
| 8 | CUST-ADDR-STATE-CD | PIC X(02) | Alpha | 2 chars | US state code (validated via CSLKPCDY) |
| 9 | CUST-ADDR-COUNTRY-CD | PIC X(03) | Alpha | 3 chars | Country code (ISO 3166) |
| 10 | CUST-ADDR-ZIP | PIC X(10) | Alpha | 10 chars | ZIP/postal code |
| 11 | CUST-PHONE-NUM-1 | PIC X(15) | Alpha | 15 chars | Primary phone number |
| 12 | CUST-PHONE-NUM-2 | PIC X(15) | Alpha | 15 chars | Secondary phone number |
| 13 | CUST-SSN | PIC 9(09) | Numeric | 9 digits | Social Security Number (PII) |
| 14 | CUST-GOVT-ISSUED-ID | PIC X(20) | Alpha | 20 chars | Government-issued ID number |
| 15 | CUST-DOB-YYYY-MM-DD | PIC X(10) | Date String | 10 chars | Date of birth |
| 16 | CUST-EFT-ACCOUNT-ID | PIC X(10) | Alpha | 10 chars | Electronic funds transfer account |
| 17 | CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alpha | 1 char | Primary card holder indicator (Y/N) |
| 18 | CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric | 3 digits | FICO credit score (300-850) |
| 19 | FILLER | PIC X(168) | Filler | 168 chars | Reserved for future use |

**Business Rules:**
- Customer linked to Account via Card Cross-Reference (CVACT03Y)
- SSN and DOB are PII fields requiring encryption in modernized system
- Phone area codes validated against NANPA list in CSLKPCDY.cpy
- State codes validated against US state list in CSLKPCDY.cpy

> **Note:** `CUSTREC.cpy` contains an identical layout with minor formatting differences (CUST-DOB-YYYYMMDD vs CUST-DOB-YYYY-MM-DD).

---

## 4. Card Cross-Reference Entity

**Copybook:** `CVACT03Y.cpy` | **Record Name:** `CARD-XREF-RECORD` | **Record Length:** 50 bytes  
**VSAM File:** Card Cross-Reference (KSDS, key = XREF-CARD-NUM)  
**Used By:** COTRN02C, COBIL00C, CBACT03C, CBEXPORT, CBIMPORT, COPAUA0C

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | XREF-CARD-NUM | PIC X(16) | Alpha | 16 chars | **Primary Key.** Card number |
| 2 | XREF-CUST-ID | PIC 9(09) | Numeric | 9 digits | **FK → Customer.** Customer ID |
| 3 | XREF-ACCT-ID | PIC 9(11) | Numeric | 11 digits | **FK → Account.** Account ID |
| 4 | FILLER | PIC X(14) | Filler | 14 chars | Reserved |

**Business Rules:**
- This is the central junction table linking Card ↔ Customer ↔ Account
- Used for lookups: given a card number, find the customer and account
- Critical for transaction processing and authorization

---

## 5. Transaction Entity (Online)

**Copybook:** `CVTRA05Y.cpy` | **Record Name:** `TRAN-RECORD` | **Record Length:** 350 bytes  
**VSAM File:** Transaction Master (KSDS, key = TRAN-ID)  
**Used By:** COTRN00C, COTRN01C, COTRN02C, COBIL00C, CBTRN01C, CBTRN02C, CBEXPORT, CBIMPORT

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | TRAN-ID | PIC X(16) | Alpha | 16 chars | **Primary Key.** Transaction identifier |
| 2 | TRAN-TYPE-CD | PIC X(02) | Alpha | 2 chars | **FK → Tran Type.** Transaction type code |
| 3 | TRAN-CAT-CD | PIC 9(04) | Numeric | 4 digits | **FK → Tran Category.** Category code |
| 4 | TRAN-SOURCE | PIC X(10) | Alpha | 10 chars | Transaction source (POS, ATM, Online, etc.) |
| 5 | TRAN-DESC | PIC X(100) | Alpha | 100 chars | Transaction description |
| 6 | TRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11 digits, 2 dec | Transaction amount |
| 7 | TRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 digits | Merchant identifier |
| 8 | TRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 chars | Merchant name |
| 9 | TRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 chars | Merchant city |
| 10 | TRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 chars | Merchant ZIP code |
| 11 | TRAN-CARD-NUM | PIC X(16) | Alpha | 16 chars | **FK → Card.** Card used for transaction |
| 12 | TRAN-ORIG-TS | PIC X(26) | Timestamp | 26 chars | Original transaction timestamp |
| 13 | TRAN-PROC-TS | PIC X(26) | Timestamp | 26 chars | Processing timestamp |
| 14 | FILLER | PIC X(20) | Filler | 20 chars | Reserved |

**Business Rules:**
- TRAN-TYPE-CD + TRAN-CAT-CD together classify the transaction for interest calculation
- Negative TRAN-AMT indicates a credit/refund; positive is a debit/purchase
- TRAN-ORIG-TS records when the transaction occurred; TRAN-PROC-TS when it was posted
- This is the highest-volume entity in the system

---

## 6. Daily Transaction Entity

**Copybook:** `CVTRA06Y.cpy` | **Record Name:** `DALYTRAN-RECORD` | **Record Length:** 350 bytes  
**VSAM File:** Daily Transaction (sequential input for batch processing)  
**Used By:** CBTRN01C, CBTRN02C

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | DALYTRAN-ID | PIC X(16) | Alpha | 16 chars | Daily transaction identifier |
| 2 | DALYTRAN-TYPE-CD | PIC X(02) | Alpha | 2 chars | Transaction type code |
| 3 | DALYTRAN-CAT-CD | PIC 9(04) | Numeric | 4 digits | Category code |
| 4 | DALYTRAN-SOURCE | PIC X(10) | Alpha | 10 chars | Transaction source |
| 5 | DALYTRAN-DESC | PIC X(100) | Alpha | 100 chars | Description |
| 6 | DALYTRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11 digits, 2 dec | Amount |
| 7 | DALYTRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 digits | Merchant ID |
| 8 | DALYTRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 chars | Merchant name |
| 9 | DALYTRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 chars | Merchant city |
| 10 | DALYTRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 chars | Merchant ZIP |
| 11 | DALYTRAN-CARD-NUM | PIC X(16) | Alpha | 16 chars | Card number |
| 12 | DALYTRAN-ORIG-TS | PIC X(26) | Timestamp | 26 chars | Original timestamp |
| 13 | DALYTRAN-PROC-TS | PIC X(26) | Timestamp | 26 chars | Processing timestamp |
| 14 | FILLER | PIC X(20) | Filler | 20 chars | Reserved |

**Business Rules:**
- Mirror structure of TRAN-RECORD for daily batch input
- Fed into CBTRN02C (posting) which validates and writes to Transaction Master
- After posting, daily transactions are merged into the master via COMBTRAN job

---

## 7. Transaction Type Entity

**Copybook:** `CVTRA03Y.cpy` | **Record Name:** `TRAN-TYPE-RECORD` | **Record Length:** 60 bytes  
**VSAM File:** Transaction Type (KSDS, key = TRAN-TYPE)  
**Used By:** CBTRN03C, COTRN02C (type validation)

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | TRAN-TYPE | PIC X(02) | Alpha | 2 chars | **Primary Key.** Transaction type code |
| 2 | TRAN-TYPE-DESC | PIC X(50) | Alpha | 50 chars | Type description (e.g., "Purchase", "Cash Advance") |
| 3 | FILLER | PIC X(08) | Filler | 8 chars | Reserved |

---

## 8. Transaction Category Entity

**Copybook:** `CVTRA04Y.cpy` | **Record Name:** `TRAN-CAT-RECORD` | **Record Length:** 60 bytes  
**VSAM File:** Transaction Category (KSDS, key = TRAN-TYPE-CD + TRAN-CAT-CD)  
**Used By:** CBTRN03C, COTRN02C

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | TRAN-TYPE-CD | PIC X(02) | Alpha | 2 chars | **Composite Key part 1.** Type code |
| 2 | TRAN-CAT-CD | PIC 9(04) | Numeric | 4 digits | **Composite Key part 2.** Category code |
| 3 | TRAN-CAT-TYPE-DESC | PIC X(50) | Alpha | 50 chars | Category description |
| 4 | FILLER | PIC X(04) | Filler | 4 chars | Reserved |

---

## 9. Transaction Category Balance Entity

**Copybook:** `CVTRA01Y.cpy` | **Record Name:** `TRAN-CAT-BAL-RECORD` | **Record Length:** 50 bytes  
**VSAM File:** Category Balance (KSDS, key = TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD)  
**Used By:** CBTRN02C, CBACT04C

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | TRANCAT-ACCT-ID | PIC 9(11) | Numeric | 11 digits | **Composite Key part 1.** Account ID |
| 2 | TRANCAT-TYPE-CD | PIC X(02) | Alpha | 2 chars | **Composite Key part 2.** Transaction type |
| 3 | TRANCAT-CD | PIC 9(04) | Numeric | 4 digits | **Composite Key part 3.** Category code |
| 4 | TRAN-CAT-BAL | PIC S9(09)V99 | Signed Decimal | 11 digits, 2 dec | Running balance for this category |
| 5 | FILLER | PIC X(22) | Filler | 22 chars | Reserved |

**Business Rules:**
- Accumulates transaction totals per account per type/category combination
- Used by interest calculation (CBACT04C) to apply category-specific rates
- Updated during transaction posting (CBTRN02C)

---

## 10. Disclosure Group Entity

**Copybook:** `CVTRA02Y.cpy` | **Record Name:** `DIS-GROUP-RECORD` | **Record Length:** 50 bytes  
**VSAM File:** Disclosure Group (KSDS, key = DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD)  
**Used By:** CBACT04C

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | DIS-ACCT-GROUP-ID | PIC X(10) | Alpha | 10 chars | **Composite Key part 1.** Account group |
| 2 | DIS-TRAN-TYPE-CD | PIC X(02) | Alpha | 2 chars | **Composite Key part 2.** Transaction type |
| 3 | DIS-TRAN-CAT-CD | PIC 9(04) | Numeric | 4 digits | **Composite Key part 3.** Category code |
| 4 | DIS-INT-RATE | PIC S9(04)V99 | Signed Decimal | 6 digits, 2 dec | Interest rate for this group/type/category |
| 5 | FILLER | PIC X(28) | Filler | 28 chars | Reserved |

**Business Rules:**
- Maps account groups to interest rates by transaction type and category
- ACCT-GROUP-ID in the Account record links to DIS-ACCT-GROUP-ID here
- Core to the interest calculation business logic in CBACT04C

---

## 11. User Security Entity

**Copybook:** `CSUSR01Y.cpy` | **Record Name:** `SEC-USER-DATA` | **Record Length:** 80 bytes  
**VSAM File:** User Security (KSDS, key = SEC-USR-ID)  
**Used By:** COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | SEC-USR-ID | PIC X(08) | Alpha | 8 chars | **Primary Key.** User login ID |
| 2 | SEC-USR-FNAME | PIC X(20) | Alpha | 20 chars | User first name |
| 3 | SEC-USR-LNAME | PIC X(20) | Alpha | 20 chars | User last name |
| 4 | SEC-USR-PWD | PIC X(08) | Alpha | 8 chars | User password (plain text - security risk) |
| 5 | SEC-USR-TYPE | PIC X(01) | Alpha | 1 char | User type: A=Admin, U=Regular User |
| 6 | SEC-USR-FILLER | PIC X(23) | Filler | 23 chars | Reserved |

**Business Rules:**
- SEC-USR-TYPE drives menu access: Admin gets COADM01C, User gets COMEN01C
- Password stored in clear text (critical security concern for modernization)
- Default users: ADMIN001/PASSWORD (Admin), USER0001/PASSWORD (User)

---

## 12. Application Communication Area

**Copybook:** `COCOM01Y.cpy` | **Record Name:** `CARDDEMO-COMMAREA`  
**Purpose:** Inter-program communication area passed via CICS COMMAREA between all online programs.

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | CDEMO-FROM-TRANID | PIC X(04) | Alpha | 4 chars | Source CICS transaction ID |
| 2 | CDEMO-FROM-PROGRAM | PIC X(08) | Alpha | 8 chars | Source program name |
| 3 | CDEMO-TO-TRANID | PIC X(04) | Alpha | 4 chars | Target CICS transaction ID |
| 4 | CDEMO-TO-PROGRAM | PIC X(08) | Alpha | 8 chars | Target program name |
| 5 | CDEMO-USER-ID | PIC X(08) | Alpha | 8 chars | Logged-in user ID |
| 6 | CDEMO-USER-TYPE | PIC X(01) | Alpha | 1 char | User type (A=Admin, U=User) |
| 7 | CDEMO-PGM-CONTEXT | PIC 9(01) | Numeric | 1 digit | 0=First entry, 1=Re-entry |
| 8 | CDEMO-CUST-ID | PIC 9(09) | Numeric | 9 digits | Current customer context |
| 9 | CDEMO-CUST-FNAME | PIC X(25) | Alpha | 25 chars | Customer first name (display) |
| 10 | CDEMO-CUST-MNAME | PIC X(25) | Alpha | 25 chars | Customer middle name |
| 11 | CDEMO-CUST-LNAME | PIC X(25) | Alpha | 25 chars | Customer last name |
| 12 | CDEMO-ACCT-ID | PIC 9(11) | Numeric | 11 digits | Current account context |
| 13 | CDEMO-ACCT-STATUS | PIC X(01) | Alpha | 1 char | Current account status |
| 14 | CDEMO-CARD-NUM | PIC 9(16) | Numeric | 16 digits | Current card context |
| 15 | CDEMO-LAST-MAP | PIC X(7) | Alpha | 7 chars | Last BMS map displayed |
| 16 | CDEMO-LAST-MAPSET | PIC X(7) | Alpha | 7 chars | Last BMS mapset used |

**Business Rules:**
- Passed via EXEC CICS XCTL between all online programs
- Carries user session context (who, what screen, what entity)
- CDEMO-PGM-CONTEXT distinguishes first-time display from data re-entry

---

## 13. Export Record (Multi-Entity)

**Copybook:** `CVEXPORT.cpy` | **Record Name:** `EXPORT-RECORD` | **Record Length:** 500 bytes  
**Purpose:** Branch migration export format with REDEFINES for multiple entity types.  
**Used By:** CBEXPORT, CBIMPORT

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | EXPORT-REC-TYPE | PIC X(1) | Alpha | 1 char | Record type discriminator |
| 2 | EXPORT-TIMESTAMP | PIC X(26) | Timestamp | 26 chars | Export timestamp |
| 3 | EXPORT-SEQUENCE-NUM | PIC 9(9) COMP | Binary | 4 bytes | Sequence number |
| 4 | EXPORT-BRANCH-ID | PIC X(4) | Alpha | 4 chars | Source branch ID |
| 5 | EXPORT-REGION-CODE | PIC X(5) | Alpha | 5 chars | Source region code |
| 6 | EXPORT-RECORD-DATA | PIC X(460) | Alpha | 460 chars | Polymorphic data area |

**REDEFINES structures:** EXPORT-CUSTOMER-DATA, EXPORT-ACCOUNT-DATA, EXPORT-TRANSACTION-DATA, EXPORT-CARD-XREF-DATA, EXPORT-CARD-DATA

**Notable:** Uses COMP and COMP-3 fields for storage optimization in the export format (e.g., `EXP-ACCT-CURR-BAL PIC S9(10)V99 COMP-3`, `EXP-TRAN-MERCHANT-ID PIC 9(09) COMP`).

---

## 14. Statement Transaction Record

**Copybook:** `COSTM01.CPY` | **Record Name:** `TRNX-RECORD`  
**Purpose:** Altered transaction layout keyed by card number + transaction ID for statement generation.  
**Used By:** CBSTM03A, CBSTM03B

| # | Field Name | PIC Clause | Type | Size | Business Description |
|---|-----------|------------|------|------|---------------------|
| 1 | TRNX-CARD-NUM | PIC X(16) | Alpha | 16 chars | **Composite Key part 1.** Card number |
| 2 | TRNX-ID | PIC X(16) | Alpha | 16 chars | **Composite Key part 2.** Transaction ID |
| 3 | TRNX-TYPE-CD | PIC X(02) | Alpha | 2 chars | Transaction type |
| 4 | TRNX-CAT-CD | PIC 9(04) | Numeric | 4 digits | Category code |
| 5 | TRNX-SOURCE | PIC X(10) | Alpha | 10 chars | Source |
| 6 | TRNX-DESC | PIC X(100) | Alpha | 100 chars | Description |
| 7 | TRNX-AMT | PIC S9(09)V99 | Signed Decimal | 11 digits, 2 dec | Amount |
| 8 | TRNX-MERCHANT-ID | PIC 9(09) | Numeric | 9 digits | Merchant ID |
| 9 | TRNX-MERCHANT-NAME | PIC X(50) | Alpha | 50 chars | Merchant name |
| 10 | TRNX-MERCHANT-CITY | PIC X(50) | Alpha | 50 chars | Merchant city |
| 11 | TRNX-MERCHANT-ZIP | PIC X(10) | Alpha | 10 chars | Merchant ZIP |
| 12 | TRNX-ORIG-TS | PIC X(26) | Timestamp | 26 chars | Original timestamp |
| 13 | TRNX-PROC-TS | PIC X(26) | Timestamp | 26 chars | Processing timestamp |
| 14 | FILLER | PIC X(20) | Filler | 20 chars | Reserved |

---

## 15. Transaction Report Layout

**Copybook:** `CVTRA07Y.cpy` | **Purpose:** Print formatting structures for the daily transaction report.  
**Used By:** CBTRN03C

| Structure | Key Fields | Description |
|-----------|-----------|-------------|
| REPORT-NAME-HEADER | REPT-SHORT-NAME ('DALYREPT'), REPT-LONG-NAME, REPT-START-DATE, REPT-END-DATE | Report header with date range |
| TRANSACTION-DETAIL-REPORT | TRAN-REPORT-TRANS-ID, TRAN-REPORT-ACCOUNT-ID, TRAN-REPORT-TYPE-CD, TRAN-REPORT-AMT | Detail line format |
| TRANSACTION-HEADER-1/2 | Column headers | Column labels and separator |
| REPORT-PAGE-TOTALS | REPT-PAGE-TOTAL (PIC +ZZZ,ZZZ,ZZZ.ZZ) | Page subtotals |
| REPORT-ACCOUNT-TOTALS | REPT-ACCOUNT-TOTAL | Account subtotals |
| REPORT-GRAND-TOTALS | REPT-GRAND-TOTAL | Grand total |

---

## 16. Date/Time Working Storage

**Copybook:** `CSDAT01Y.cpy` | **Record Name:** `WS-DATE-TIME`  
**Purpose:** Standard date/time fields used across all online programs.

| Field Group | Key Fields | Format |
|-------------|-----------|--------|
| WS-CURDATE | WS-CURDATE-YEAR, WS-CURDATE-MONTH, WS-CURDATE-DAY | YYYYMMDD (numeric) |
| WS-CURTIME | WS-CURTIME-HOURS, WS-CURTIME-MINUTE, WS-CURTIME-SECOND | HHMMSSMS |
| WS-CURDATE-MM-DD-YY | Formatted display date | MM/DD/YY |
| WS-CURTIME-HH-MM-SS | Formatted display time | HH:MM:SS |
| WS-TIMESTAMP | Full precision timestamp | YYYY-MM-DD HH:MM:SS.SSSSSS |

---

## 17. Date Validation Working Storage

**Copybook:** `CSUTLDWY.cpy` | **Purpose:** Working storage for the reusable date validation logic in CSUTLDPY.cpy.

| Field Group | Key Fields | Description |
|-------------|-----------|-------------|
| WS-EDIT-DATE-CCYYMMDD | CC, YY, MM, DD components | Date being validated |
| WS-EDIT-DATE-FLGS | FLG-YEAR-ISVALID, FLG-MONTH-ISVALID, FLG-DAY-ISVALID | Validation status flags |
| WS-DATE-VALIDATION-RESULT | WS-SEVERITY, WS-MSG-NO, WS-RESULT | LE service response |
| 88-level conditions | THIS-CENTURY (20), LAST-CENTURY (19), WS-VALID-MONTH (1-12), WS-VALID-DAY (1-31), WS-FEBRUARY (2) | Business rule values |

---

## 18. Menu Option Structures

### Main Menu (COMEN02Y.cpy)

**Record:** `CARDDEMO-MAIN-MENU-OPTIONS` | 11 options

| Option # | Name | Target Program | User Type |
|----------|------|---------------|-----------|
| 1 | Account View | COACTVWC | U (User) |
| 2 | Account Update | COACTUPC | U |
| 3 | Credit Card List | COCRDLIC | U |
| 4 | Credit Card View | COCRDSLC | U |
| 5 | Credit Card Update | COCRDUPC | U |
| 6 | Transaction List | COTRN00C | U |
| 7 | Transaction View | COTRN01C | U |
| 8 | Transaction Add | COTRN02C | U |
| 9 | Transaction Reports | CORPT00C | U |
| 10 | Bill Payment | COBIL00C | U |
| 11 | Pending Authorization View | COPAUS0C | U |

### Admin Menu (COADM02Y.cpy)

**Record:** `CARDDEMO-ADMIN-MENU-OPTIONS` | 6 options

| Option # | Name | Target Program |
|----------|------|---------------|
| 1 | User List (Security) | COUSR00C |
| 2 | User Add (Security) | COUSR01C |
| 3 | User Update (Security) | COUSR02C |
| 4 | User Delete (Security) | COUSR03C |
| 5 | Transaction Type List/Update (Db2) | COTRTLIC |
| 6 | Transaction Type Maintenance (Db2) | COTRTUPC |

---

## 19. Card Work Areas / Navigation

**Copybook:** `CVCRD01Y.cpy` | **Record Name:** `CC-WORK-AREAS`  
**Purpose:** Screen navigation control and work fields for card-related programs.

| # | Field Name | PIC Clause | Description |
|---|-----------|------------|-------------|
| 1 | CCARD-AID | PIC X(5) | Last AID key pressed (ENTER, CLEAR, PFK01-12) |
| 2 | CCARD-NEXT-PROG | PIC X(8) | Next program to XCTL to |
| 3 | CCARD-NEXT-MAPSET | PIC X(7) | Next BMS mapset |
| 4 | CCARD-NEXT-MAP | PIC X(7) | Next BMS map |
| 5 | CCARD-ERROR-MSG | PIC X(75) | Error message for display |
| 6 | CCARD-RETURN-MSG | PIC X(75) | Return/info message |
| 7 | CC-ACCT-ID | PIC X(11) / 9(11) | Account ID (with REDEFINES) |
| 8 | CC-CARD-NUM | PIC X(16) / 9(16) | Card number (with REDEFINES) |
| 9 | CC-CUST-ID | PIC X(09) / 9(9) | Customer ID (with REDEFINES) |

---

## 20. Authorization Module Entities (Optional)

### Pending Authorization Summary (CIPAUSMY.cpy)

| Field | Type | Description |
|-------|------|-------------|
| Authorization ID | Numeric | Unique authorization request ID |
| Account ID | Numeric | Associated account |
| Card Number | Alpha | Card used |
| Amount | Decimal | Authorization amount |
| Status | Alpha | Pending/Approved/Declined/Fraud |

### Pending Authorization Detail (CIPAUDTY.cpy)

| Field | Type | Description |
|-------|------|-------------|
| Detail fields | Various | Full authorization detail including merchant data |

### MQ Message Structures

| Copybook | Purpose |
|----------|---------|
| CCPAURQY | Authorization request message format |
| CCPAURLY | Authorization reply message format |
| CCPAUERY | Authorization error message format |

### IMS DB Structures

| Copybook | Purpose |
|----------|---------|
| IMSFUNCS | IMS DL/I function codes (GU, GN, GNP, ISRT, etc.) |
| PAUTBPCB | IMS PCB mask for authorization DB |
| PASFLPCB | IMS PCB mask for status feedback |
| PADFLPCB | IMS PCB mask for data feedback |

---

## 21. VSAM File Catalog

| VSAM Dataset | Type | Key | Record Len | Copybook | Business Entity |
|-------------|------|-----|-----------|----------|----------------|
| AWS.M2.CARDDEMO.ACCTDATA | KSDS | ACCT-ID (11) | 300 | CVACT01Y | Account Master |
| AWS.M2.CARDDEMO.CARDDATA | KSDS | CARD-NUM (16) | 150 | CVACT02Y | Card Master |
| AWS.M2.CARDDEMO.CUSTDATA | KSDS | CUST-ID (9) | 500 | CVCUS01Y | Customer Master |
| AWS.M2.CARDDEMO.CARDXREF | KSDS | XREF-CARD-NUM (16) | 50 | CVACT03Y | Card Cross-Reference |
| AWS.M2.CARDDEMO.TRANSACT | KSDS | TRAN-ID (16) | 350 | CVTRA05Y | Transaction Master |
| AWS.M2.CARDDEMO.DALYTRAN | SEQ | N/A | 350 | CVTRA06Y | Daily Transactions |
| AWS.M2.CARDDEMO.TRANTYPE | KSDS | TRAN-TYPE (2) | 60 | CVTRA03Y | Transaction Type |
| AWS.M2.CARDDEMO.TRANCATG | KSDS | TYPE+CAT (6) | 60 | CVTRA04Y | Transaction Category |
| AWS.M2.CARDDEMO.TCATBALF | KSDS | ACCT+TYPE+CAT (17) | 50 | CVTRA01Y | Category Balance |
| AWS.M2.CARDDEMO.DISCGRP | KSDS | GROUP+TYPE+CAT (16) | 50 | CVTRA02Y | Disclosure Group |
| AWS.M2.CARDDEMO.USRSEC | KSDS | SEC-USR-ID (8) | 80 | CSUSR01Y | User Security |

---

## 22. Type Mapping Reference

### COBOL PIC → Java / SQL Type Mapping

| COBOL PIC Pattern | Example | Java Type | SQL Type | Notes |
|-------------------|---------|-----------|----------|-------|
| PIC 9(n) | PIC 9(11) | `long` | `BIGINT` | Unsigned integer, use `int` if n ≤ 9 |
| PIC X(n) | PIC X(50) | `String` | `VARCHAR(n)` | Fixed-length in COBOL, variable in Java |
| PIC S9(n)V99 | PIC S9(10)V99 | `BigDecimal` | `DECIMAL(n+2, 2)` | Signed decimal, 2 implied decimals |
| PIC S9(n)V99 COMP-3 | PIC S9(10)V99 COMP-3 | `BigDecimal` | `DECIMAL(n+2, 2)` | Packed decimal (BCD storage) |
| PIC 9(n) COMP | PIC 9(9) COMP | `int` / `long` | `INTEGER` / `BIGINT` | Binary storage |
| PIC X(10) (date) | ACCT-OPEN-DATE | `LocalDate` | `DATE` | Parse from YYYY-MM-DD string |
| PIC X(26) (timestamp) | TRAN-ORIG-TS | `LocalDateTime` | `TIMESTAMP` | Full precision timestamp |
| PIC X(01) (flag) | ACCT-ACTIVE-STATUS | `enum` or `boolean` | `CHAR(1)` | Map to enum for clarity |
| 88-level condition | CDEMO-USRTYP-ADMIN | `enum` constant | N/A | Map to Java enum values |

### Key Relationships (Entity-Relationship)

```
Customer (CUST-ID)
    │
    ├──< Card-XREF (XREF-CARD-NUM) ──> Card (CARD-NUM)
    │                                      │
    └──────────────> Account (ACCT-ID) <───┘
                        │
                        ├──< Transaction (TRAN-ID)
                        │       │
                        │       ├── Tran-Type (TRAN-TYPE-CD)
                        │       └── Tran-Category (TYPE-CD + CAT-CD)
                        │
                        ├──< Category-Balance (ACCT + TYPE + CAT)
                        │
                        └──> Disclosure-Group (GROUP-ID + TYPE + CAT)
                                └── Interest Rate

User-Security (SEC-USR-ID) ── independent entity for application access
```
