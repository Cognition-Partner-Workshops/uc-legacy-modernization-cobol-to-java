# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo | **Source:** Copybook PIC clause analysis

## Overview

This data dictionary extracts business entities from COBOL copybook record layouts. Each VSAM file stores fixed-length records whose structure is defined by copybooks. The PIC clauses define field types, sizes, and precision.

### PIC Clause Quick Reference

| PIC Pattern | Meaning | Java Equivalent |
|-------------|---------|-----------------|
| `PIC X(n)` | Alphanumeric, n characters | `String` |
| `PIC 9(n)` | Unsigned numeric, n digits | `int` / `long` |
| `PIC S9(n)V99` | Signed decimal, n+2 digits, 2 decimal places | `BigDecimal` |
| `COMP` | Binary (computational) | `int` / `long` |
| `COMP-3` | Packed decimal | `BigDecimal` |

---

## 1. Account Entity

**Source:** `CVACT01Y.cpy` | **Record Length:** 300 bytes | **VSAM File:** `ACCTDATA.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| `ACCT-ID` | `PIC 9(11)` | Numeric | 11 digits | **Primary Key.** Unique account identifier |
| `ACCT-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 char | Account status flag (Y=Active) |
| `ACCT-CURR-BAL` | `PIC S9(10)V99` | Decimal | 12.2 | Current account balance |
| `ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | Decimal | 12.2 | Credit limit amount |
| `ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | Decimal | 12.2 | Cash advance credit limit |
| `ACCT-OPEN-DATE` | `PIC X(10)` | Alpha | 10 chars | Account opening date |
| `ACCT-EXPIRAION-DATE` | `PIC X(10)` | Alpha | 10 chars | Account expiration date |
| `ACCT-REISSUE-DATE` | `PIC X(10)` | Alpha | 10 chars | Last reissue date |
| `ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | Decimal | 12.2 | Current cycle credit total |
| `ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | Decimal | 12.2 | Current cycle debit total |
| `ACCT-ADDR-ZIP` | `PIC X(10)` | Alpha | 10 chars | Account address ZIP code |
| `ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 chars | Account group/portfolio ID |
| `FILLER` | `PIC X(178)` | Alpha | 178 chars | Reserved for future use |

**Business Rules:**
- Key: Account ID (11-digit numeric)
- Balances use signed packed decimal with 2 decimal places
- Dates stored as 10-character strings (YYYY-MM-DD format)

---

## 2. Card Entity

**Source:** `CVACT02Y.cpy` | **Record Length:** 150 bytes | **VSAM File:** `CARDDATA.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| `CARD-NUM` | `PIC X(16)` | Alpha | 16 chars | **Primary Key.** Card number |
| `CARD-ACCT-ID` | `PIC 9(11)` | Numeric | 11 digits | Parent account ID (FK to Account) |
| `CARD-CVV-CD` | `PIC 9(03)` | Numeric | 3 digits | Card verification value (CVV) |
| `CARD-EMBOSSED-NAME` | `PIC X(50)` | Alpha | 50 chars | Name embossed on card |
| `CARD-EXPIRAION-DATE` | `PIC X(10)` | Alpha | 10 chars | Card expiration date |
| `CARD-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 char | Card status (Y=Active) |
| `FILLER` | `PIC X(59)` | Alpha | 59 chars | Reserved for future use |

**Business Rules:**
- Card number is 16-character alphanumeric (supports formatted card numbers)
- Each card belongs to exactly one account
- CVV is a 3-digit numeric code

---

## 3. Customer Entity

**Source:** `CVCUS01Y.cpy` | **Record Length:** 500 bytes | **VSAM File:** `CUSTDATA.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| `CUST-ID` | `PIC 9(09)` | Numeric | 9 digits | **Primary Key.** Customer identifier |
| `CUST-FIRST-NAME` | `PIC X(25)` | Alpha | 25 chars | Customer first name |
| `CUST-MIDDLE-NAME` | `PIC X(25)` | Alpha | 25 chars | Customer middle name |
| `CUST-LAST-NAME` | `PIC X(25)` | Alpha | 25 chars | Customer last name |
| `CUST-ADDR-LINE-1` | `PIC X(50)` | Alpha | 50 chars | Address line 1 |
| `CUST-ADDR-LINE-2` | `PIC X(50)` | Alpha | 50 chars | Address line 2 |
| `CUST-ADDR-LINE-3` | `PIC X(50)` | Alpha | 50 chars | Address line 3 |
| `CUST-ADDR-STATE-CD` | `PIC X(02)` | Alpha | 2 chars | State code |
| `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | Alpha | 3 chars | Country code |
| `CUST-ADDR-ZIP` | `PIC X(10)` | Alpha | 10 chars | ZIP/postal code |
| `CUST-PHONE-NUM-1` | `PIC X(15)` | Alpha | 15 chars | Primary phone |
| `CUST-PHONE-NUM-2` | `PIC X(15)` | Alpha | 15 chars | Secondary phone |
| `CUST-SSN` | `PIC 9(09)` | Numeric | 9 digits | Social Security Number (PII) |
| `CUST-GOVT-ISSUED-ID` | `PIC X(20)` | Alpha | 20 chars | Government-issued ID |
| `CUST-DOB-YYYY-MM-DD` | `PIC X(10)` | Alpha | 10 chars | Date of birth |
| `CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | Alpha | 10 chars | EFT (electronic funds transfer) account |
| `CUST-PRI-CARD-HOLDER-IND` | `PIC X(01)` | Alpha | 1 char | Primary cardholder indicator |
| `CUST-FICO-CREDIT-SCORE` | `PIC 9(03)` | Numeric | 3 digits | FICO credit score |
| `FILLER` | `PIC X(168)` | Alpha | 168 chars | Reserved for future use |

**Business Rules:**
- Contains PII fields (SSN, DOB, Gov ID) requiring encryption in modernized system
- Three address lines support international addresses
- FICO score is 3-digit numeric (300-850 range)

---

## 4. Card Cross-Reference Entity

**Source:** `CVACT03Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `CARDXREF.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| `XREF-CARD-NUM` | `PIC X(16)` | Alpha | 16 chars | **Primary Key.** Card number |
| `XREF-CUST-ID` | `PIC 9(09)` | Numeric | 9 digits | Customer ID (FK to Customer) |
| `XREF-ACCT-ID` | `PIC 9(11)` | Numeric | 11 digits | Account ID (FK to Account) |
| `FILLER` | `PIC X(14)` | Alpha | 14 chars | Reserved |

**Business Rules:**
- Central lookup table linking Card -> Customer -> Account
- Has an alternate index (AIX) on Account ID for reverse lookups
- Used by nearly every online and batch program for entity resolution

---

## 5. Transaction Entity

**Source:** `CVTRA05Y.cpy` | **Record Length:** 350 bytes | **VSAM File:** `TRANSACT.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| `TRAN-ID` | `PIC X(16)` | Alpha | 16 chars | **Primary Key.** Transaction identifier |
| `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | Transaction type code (FK to Tran Type) |
| `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 digits | Transaction category code |
| `TRAN-SOURCE` | `PIC X(10)` | Alpha | 10 chars | Transaction source/channel |
| `TRAN-DESC` | `PIC X(100)` | Alpha | 100 chars | Transaction description |
| `TRAN-AMT` | `PIC S9(09)V99` | Decimal | 11.2 | Transaction amount (signed) |
| `TRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 digits | Merchant identifier |
| `TRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 chars | Merchant name |
| `TRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 chars | Merchant city |
| `TRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 chars | Merchant ZIP code |
| `TRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 chars | Card number (FK to Card) |
| `TRAN-ORIG-TS` | `PIC X(26)` | Alpha | 26 chars | Origination timestamp |
| `TRAN-PROC-TS` | `PIC X(26)` | Alpha | 26 chars | Processing timestamp |
| `FILLER` | `PIC X(20)` | Alpha | 20 chars | Reserved |

**Business Rules:**
- Transaction amount is signed (positive = debit, negative = credit)
- Timestamps are 26-character ISO-like format
- Links to card via card number, which links to account via cross-reference

---

## 6. Daily Transaction Entity

**Source:** `CVTRA06Y.cpy` | **Record Length:** 350 bytes | **VSAM File:** `DALYTRAN.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| `DALYTRAN-ID` | `PIC X(16)` | Alpha | 16 chars | **Primary Key.** Daily transaction ID |
| `DALYTRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | Transaction type code |
| `DALYTRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 digits | Transaction category code |
| `DALYTRAN-SOURCE` | `PIC X(10)` | Alpha | 10 chars | Transaction source |
| `DALYTRAN-DESC` | `PIC X(100)` | Alpha | 100 chars | Description |
| `DALYTRAN-AMT` | `PIC S9(09)V99` | Decimal | 11.2 | Amount |
| `DALYTRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 digits | Merchant ID |
| `DALYTRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 chars | Merchant name |
| `DALYTRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 chars | Merchant city |
| `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 chars | Merchant ZIP |
| `DALYTRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 chars | Card number |
| `DALYTRAN-ORIG-TS` | `PIC X(26)` | Alpha | 26 chars | Origination timestamp |
| `DALYTRAN-PROC-TS` | `PIC X(26)` | Alpha | 26 chars | Processing timestamp |
| `FILLER` | `PIC X(20)` | Alpha | 20 chars | Reserved |

**Business Rules:**
- Identical layout to Transaction record (staging/input table)
- Populated externally, consumed by CBTRN02C (posting) batch job
- After posting, records move to the master TRANSACT file

---

## 7. Transaction Category Balance Entity

**Source:** `CVTRA01Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `TCATBALF.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| `TRANCAT-ACCT-ID` | `PIC 9(11)` | Numeric | 11 digits | **Composite Key Part 1.** Account ID |
| `TRANCAT-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | **Composite Key Part 2.** Transaction type |
| `TRANCAT-CD` | `PIC 9(04)` | Numeric | 4 digits | **Composite Key Part 3.** Category code |
| `TRAN-CAT-BAL` | `PIC S9(09)V99` | Decimal | 11.2 | Running category balance |
| `FILLER` | `PIC X(22)` | Alpha | 22 chars | Reserved |

**Business Rules:**
- Composite key: Account + Type + Category
- Maintains running balance per transaction category per account
- Updated during daily transaction posting (CBTRN02C)

---

## 8. Disclosure Group Entity

**Source:** `CVTRA02Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `DISCGRP.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| `DIS-ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 chars | **Composite Key Part 1.** Account group ID |
| `DIS-TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | **Composite Key Part 2.** Transaction type |
| `DIS-TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 digits | **Composite Key Part 3.** Category code |
| `DIS-INT-RATE` | `PIC S9(04)V99` | Decimal | 6.2 | Interest rate for this group/type/category |
| `FILLER` | `PIC X(28)` | Alpha | 28 chars | Reserved |

**Business Rules:**
- Maps account groups to interest rates by transaction type and category
- Used during interest calculation (CBACT04C) to determine applicable rate
- Reference/configuration data, rarely changes

---

## 9. Transaction Type Entity

**Source:** `CVTRA03Y.cpy` | **Record Length:** 60 bytes | **VSAM File:** `TRANTYPE.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| `TRAN-TYPE` | `PIC X(02)` | Alpha | 2 chars | **Primary Key.** Transaction type code |
| `TRAN-TYPE-DESC` | `PIC X(50)` | Alpha | 50 chars | Type description (e.g., "Purchase", "Cash Advance") |
| `FILLER` | `PIC X(08)` | Alpha | 8 chars | Reserved |

**Business Rules:**
- Lookup/reference table for transaction type codes
- Used for report labeling and validation

---

## 10. Transaction Category Entity

**Source:** `CVTRA04Y.cpy` | **Record Length:** 60 bytes | **VSAM File:** `TRANCATG.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | **Composite Key Part 1.** Transaction type |
| `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 digits | **Composite Key Part 2.** Category code |
| `TRAN-CAT-TYPE-DESC` | `PIC X(50)` | Alpha | 50 chars | Category description |
| `FILLER` | `PIC X(04)` | Alpha | 4 chars | Reserved |

**Business Rules:**
- Subcategories within each transaction type
- Used for fine-grained reporting and interest rate determination

---

## 11. User Security Entity

**Source:** `CSUSR01Y.cpy` | **Record Length:** 80 bytes | **VSAM File:** `USRSEC.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| `SEC-USR-ID` | `PIC X(08)` | Alpha | 8 chars | **Primary Key.** User login ID |
| `SEC-USR-FNAME` | `PIC X(20)` | Alpha | 20 chars | User first name |
| `SEC-USR-LNAME` | `PIC X(20)` | Alpha | 20 chars | User last name |
| `SEC-USR-PWD` | `PIC X(08)` | Alpha | 8 chars | User password (plaintext!) |
| `SEC-USR-TYPE` | `PIC X(01)` | Alpha | 1 char | User type (A=Admin, U=Regular) |
| `FILLER` | `PIC X(23)` | Alpha | 23 chars | Reserved |

**Business Rules:**
- Passwords stored in plaintext (critical security finding for modernization)
- Two user types: Admin (A) and Regular User (U)
- Admin users can manage other users and access admin menu
- Max 8-character user ID and password

---

## 12. Statement Transaction Layout

**Source:** `COSTM01.CPY` | **Record Length:** 350 bytes | **Used by:** CBSTM03A/B

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| `TRNX-CARD-NUM` | `PIC X(16)` | Alpha | 16 chars | **Composite Key Part 1.** Card number |
| `TRNX-ID` | `PIC X(16)` | Alpha | 16 chars | **Composite Key Part 2.** Transaction ID |
| `TRNX-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | Transaction type code |
| `TRNX-CAT-CD` | `PIC 9(04)` | Numeric | 4 digits | Category code |
| `TRNX-SOURCE` | `PIC X(10)` | Alpha | 10 chars | Source |
| `TRNX-DESC` | `PIC X(100)` | Alpha | 100 chars | Description |
| `TRNX-AMT` | `PIC S9(09)V99` | Decimal | 11.2 | Amount |
| `TRNX-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 digits | Merchant ID |
| `TRNX-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 chars | Merchant name |
| `TRNX-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 chars | Merchant city |
| `TRNX-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 chars | Merchant ZIP |
| `TRNX-ORIG-TS` | `PIC X(26)` | Alpha | 26 chars | Origination timestamp |
| `TRNX-PROC-TS` | `PIC X(26)` | Alpha | 26 chars | Processing timestamp |
| `FILLER` | `PIC X(20)` | Alpha | 20 chars | Reserved |

**Business Rules:**
- Re-keyed version of transaction with Card Number as primary sort key
- Produced by SORT in CREASTMT.JCL for per-card statement generation

---

## 13. Export Record Layout

**Source:** `CVEXPORT.cpy` | **Record Length:** variable | **Used by:** CBEXPORT, CBIMPORT

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| `EXP-RECORD-TYPE` | `PIC X(01)` | Alpha | 1 char | Record type (C=Customer, A=Account, X=Xref, T=Transaction, D=Card) |
| `EXP-CUST-*` | Various | Various | Various | Customer fields (when type = C) |
| `EXP-ACCT-*` | Various | Various | Various | Account fields (when type = A) |
| `EXP-CARD-*` | Various | Various | Various | Card fields (when type = D) |
| `EXP-XREF-*` | Various | Various | Various | Cross-reference fields (when type = X) |
| `EXP-TRAN-*` | Various | Various | Various | Transaction fields (when type = T) |

**Business Rules:**
- Multiplexed record layout - type indicator determines which REDEFINES to use
- Used for bulk data export/import across environments
- Contains all 5 entity types in a single flat file

---

## 14. COMMAREA (Communication Area)

**Source:** `COCOM01Y.cpy` | **Used by:** All online CICS programs

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| `CDEMO-FROM-TRANID` | `PIC X(04)` | Alpha | 4 chars | Calling transaction ID |
| `CDEMO-FROM-PROGRAM` | `PIC X(08)` | Alpha | 8 chars | Calling program name |
| `CDEMO-TO-TRANID` | `PIC X(04)` | Alpha | 4 chars | Target transaction ID |
| `CDEMO-TO-PROGRAM` | `PIC X(08)` | Alpha | 8 chars | Target program name |
| `CDEMO-USER-ID` | `PIC X(08)` | Alpha | 8 chars | Current user ID |
| `CDEMO-USER-TYPE` | `PIC X(01)` | Alpha | 1 char | User type (A/U) |
| `CDEMO-PGM-CONTEXT` | `PIC 9(01)` | Numeric | 1 digit | Program context flag |
| `CDEMO-CUST-ID` | `PIC 9(09)` | Numeric | 9 digits | Selected customer ID |
| `CDEMO-CUST-FNAME` | `PIC X(25)` | Alpha | 25 chars | Selected customer first name |
| `CDEMO-CUST-MNAME` | `PIC X(25)` | Alpha | 25 chars | Selected customer middle name |
| `CDEMO-CUST-LNAME` | `PIC X(25)` | Alpha | 25 chars | Selected customer last name |
| `CDEMO-ACCT-ID` | `PIC 9(11)` | Numeric | 11 digits | Selected account ID |
| `CDEMO-ACCT-STATUS` | `PIC X(01)` | Alpha | 1 char | Selected account status |
| `CDEMO-CARD-NUM` | `PIC 9(16)` | Numeric | 16 digits | Selected card number |
| `CDEMO-LAST-MAP` | `PIC X(07)` | Alpha | 7 chars | Last displayed map |
| `CDEMO-LAST-MAPSET` | `PIC X(07)` | Alpha | 7 chars | Last displayed mapset |

**Business Rules:**
- Passed between all online programs via CICS COMMAREA
- Carries navigation context (from/to program), user identity, and selected entity IDs
- Equivalent to HTTP session state in a web application

---

## Entity Relationship Summary

```
Customer (CVCUS01Y) ─────┐
  PK: CUST-ID             │
                           │
Account (CVACT01Y) ───────┤
  PK: ACCT-ID             │
                           ├── Card Cross-Reference (CVACT03Y)
Card (CVACT02Y) ──────────┤     PK: XREF-CARD-NUM
  PK: CARD-NUM            │     FK: XREF-CUST-ID -> Customer
  FK: CARD-ACCT-ID        │     FK: XREF-ACCT-ID -> Account
                           │
Transaction (CVTRA05Y) ───┘
  PK: TRAN-ID
  FK: TRAN-CARD-NUM -> Card

Daily Transaction (CVTRA06Y) ──> Transaction (posting)
  PK: DALYTRAN-ID

Tran Cat Balance (CVTRA01Y)
  CK: ACCT-ID + TYPE + CAT

Disclosure Group (CVTRA02Y)
  CK: GROUP-ID + TYPE + CAT

Transaction Type (CVTRA03Y)        Transaction Category (CVTRA04Y)
  PK: TRAN-TYPE                      CK: TRAN-TYPE-CD + TRAN-CAT-CD

User Security (CSUSR01Y)
  PK: SEC-USR-ID
```

---

## Modernization Notes

### Data Type Mapping to Java/JPA

| COBOL PIC | Java Type | JPA Annotation |
|-----------|-----------|----------------|
| `PIC X(n)` | `String` | `@Column(length = n)` |
| `PIC 9(n)` | `long` (n > 9) or `int` | `@Column` |
| `PIC S9(n)V99` | `BigDecimal` | `@Column(precision = n+2, scale = 2)` |
| `PIC 9(03) COMP` | `int` | `@Column` |
| Date fields (`PIC X(10)`) | `LocalDate` | `@Column` with converter |
| Timestamp (`PIC X(26)`) | `LocalDateTime` | `@Column` with converter |

### Security Concerns for Migration

1. **Plaintext Passwords** (`CSUSR01Y.cpy`): `SEC-USR-PWD` stored as `PIC X(08)` plaintext. Must be replaced with hashed passwords (bcrypt/argon2).
2. **PII Fields** (`CVCUS01Y.cpy`): SSN (`CUST-SSN`), DOB, Government ID require encryption at rest.
3. **Card Numbers** (`CVACT02Y.cpy`): 16-digit card numbers require PCI-DSS compliant tokenization.

### VSAM-to-RDBMS Table Mapping

| VSAM File | Suggested Table Name | Primary Key |
|-----------|---------------------|-------------|
| ACCTDATA.VSAM.KSDS | `accounts` | `acct_id` |
| CARDDATA.VSAM.KSDS | `cards` | `card_num` |
| CUSTDATA.VSAM.KSDS | `customers` | `cust_id` |
| CARDXREF.VSAM.KSDS | `card_xref` | `card_num` |
| TRANSACT.VSAM.KSDS | `transactions` | `tran_id` |
| DALYTRAN | `daily_transactions` | `dalytran_id` |
| TCATBALF.VSAM.KSDS | `tran_category_balances` | `(acct_id, type_cd, cat_cd)` |
| DISCGRP.VSAM.KSDS | `disclosure_groups` | `(group_id, type_cd, cat_cd)` |
| TRANTYPE.VSAM.KSDS | `transaction_types` | `tran_type` |
| TRANCATG.VSAM.KSDS | `transaction_categories` | `(type_cd, cat_cd)` |
| USRSEC.VSAM.KSDS | `users` | `user_id` |
