# DATA DICTIONARY -- CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Source:** Copybook PIC clauses from `app/cpy/` and optional modules
> **Purpose:** Business-friendly reference mapping COBOL record layouts to logical data entities

---

## 1. Account Entity

**Source Copybook:** `CVACT01Y.cpy` | **Record Length:** 300 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`

| # | Field Name | PIC Clause | Type | Offset | Length | Business Description |
|---|------------|-----------|------|--------|--------|---------------------|
| 1 | ACCT-ID | `9(11)` | Numeric | 0 | 11 | Unique account identifier |
| 2 | ACCT-ACTIVE-STATUS | `X(01)` | Alpha | 11 | 1 | Account status (Y=Active, N=Inactive) |
| 3 | ACCT-CURR-BAL | `S9(10)V99` | Signed Decimal | 12 | 12 | Current account balance |
| 4 | ACCT-CREDIT-LIMIT | `S9(10)V99` | Signed Decimal | 24 | 12 | Credit limit |
| 5 | ACCT-CASH-CREDIT-LIMIT | `S9(10)V99` | Signed Decimal | 36 | 12 | Cash advance credit limit |
| 6 | ACCT-OPEN-DATE | `X(10)` | Date String | 48 | 10 | Account open date (YYYY-MM-DD) |
| 7 | ACCT-EXPIRAION-DATE | `X(10)` | Date String | 58 | 10 | Account expiration date |
| 8 | ACCT-REISSUE-DATE | `X(10)` | Date String | 68 | 10 | Card reissue date |
| 9 | ACCT-CURR-CYC-CREDIT | `S9(10)V99` | Signed Decimal | 78 | 12 | Current cycle credit total |
| 10 | ACCT-CURR-CYC-DEBIT | `S9(10)V99` | Signed Decimal | 90 | 12 | Current cycle debit total |
| 11 | ACCT-ADDR-ZIP | `X(10)` | Alpha | 102 | 10 | Account holder ZIP code |
| 12 | ACCT-GROUP-ID | `X(10)` | Alpha | 112 | 10 | Account group/portfolio ID |
| 13 | FILLER | `X(178)` | Filler | 122 | 178 | Reserved space |

**Key:** ACCT-ID (primary) | **Java Target:** `Account` entity / `AccountDTO`

---

## 2. Card Entity

**Source Copybook:** `CVACT02Y.cpy` | **Record Length:** 150 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|------------|-----------|------|--------|---------------------|
| 1 | CARD-NUM | `X(16)` | Alpha | 16 | Credit card number (PAN) |
| 2 | CARD-ACCT-ID | `9(11)` | Numeric | 11 | Parent account ID (FK to Account) |
| 3 | CARD-CVV-CD | `9(03)` | Numeric | 3 | Card verification value |
| 4 | CARD-EMBOSSED-NAME | `X(50)` | Alpha | 50 | Name printed on card |
| 5 | CARD-EXPIRAION-DATE | `X(10)` | Date String | 10 | Card expiration date |
| 6 | CARD-ACTIVE-STATUS | `X(01)` | Alpha | 1 | Card status (Y=Active, N=Inactive) |
| 7 | FILLER | `X(59)` | Filler | 59 | Reserved space |

**Key:** CARD-NUM (primary), CARD-ACCT-ID (FK) | **Java Target:** `Card` entity

---

## 3. Customer Entity

**Source Copybook:** `CVCUS01Y.cpy` | **Record Length:** 500 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|------------|-----------|------|--------|---------------------|
| 1 | CUST-ID | `9(09)` | Numeric | 9 | Unique customer identifier |
| 2 | CUST-FIRST-NAME | `X(25)` | Alpha | 25 | Customer first name |
| 3 | CUST-MIDDLE-NAME | `X(25)` | Alpha | 25 | Customer middle name |
| 4 | CUST-LAST-NAME | `X(25)` | Alpha | 25 | Customer last name |
| 5 | CUST-ADDR-LINE-1 | `X(50)` | Alpha | 50 | Street address line 1 |
| 6 | CUST-ADDR-LINE-2 | `X(50)` | Alpha | 50 | Street address line 2 |
| 7 | CUST-ADDR-LINE-3 | `X(50)` | Alpha | 50 | Street address line 3 |
| 8 | CUST-ADDR-STATE-CD | `X(02)` | Alpha | 2 | State code (US 2-letter) |
| 9 | CUST-ADDR-COUNTRY-CD | `X(03)` | Alpha | 3 | Country code |
| 10 | CUST-ADDR-ZIP | `X(10)` | Alpha | 10 | ZIP / postal code |
| 11 | CUST-PHONE-NUM-1 | `X(15)` | Alpha | 15 | Primary phone number |
| 12 | CUST-PHONE-NUM-2 | `X(15)` | Alpha | 15 | Secondary phone number |
| 13 | CUST-SSN | `9(09)` | Numeric | 9 | Social Security Number |
| 14 | CUST-GOVT-ISSUED-ID | `X(20)` | Alpha | 20 | Government-issued ID number |
| 15 | CUST-DOB-YYYY-MM-DD | `X(10)` | Date String | 10 | Date of birth |
| 16 | CUST-EFT-ACCOUNT-ID | `X(10)` | Alpha | 10 | EFT/ACH account for bill payment |
| 17 | CUST-PRI-CARD-HOLDER-IND | `X(01)` | Alpha | 1 | Primary cardholder indicator (Y/N) |
| 18 | CUST-FICO-CREDIT-SCORE | `9(03)` | Numeric | 3 | FICO credit score |
| 19 | FILLER | `X(168)` | Filler | 168 | Reserved space |

**Key:** CUST-ID (primary) | **Java Target:** `Customer` entity | **PII Fields:** CUST-SSN, CUST-DOB, CUST-GOVT-ISSUED-ID

---

## 4. Card Cross-Reference Entity

**Source Copybook:** `CVACT03Y.cpy` | **Record Length:** 50 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|------------|-----------|------|--------|---------------------|
| 1 | XREF-CARD-NUM | `X(16)` | Alpha | 16 | Card number (FK to Card) |
| 2 | XREF-CUST-ID | `9(09)` | Numeric | 9 | Customer ID (FK to Customer) |
| 3 | XREF-ACCT-ID | `9(11)` | Numeric | 11 | Account ID (FK to Account) |
| 4 | FILLER | `X(14)` | Filler | 14 | Reserved space |

**Key:** XREF-CARD-NUM (primary) | **Purpose:** Links card -> customer -> account (many-to-one)

---

## 5. Transaction Entity

**Source Copybook:** `CVTRA05Y.cpy` | **Record Length:** 350 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|------------|-----------|------|--------|---------------------|
| 1 | TRAN-ID | `X(16)` | Alpha | 16 | Unique transaction identifier |
| 2 | TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code (FK to Tran Type) |
| 3 | TRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 4 | TRAN-SOURCE | `X(10)` | Alpha | 10 | Transaction source (POS, ATM, ONLINE, etc.) |
| 5 | TRAN-DESC | `X(100)` | Alpha | 100 | Transaction description |
| 6 | TRAN-AMT | `S9(09)V99` | Signed Decimal | 11 | Transaction amount |
| 7 | TRAN-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant identifier |
| 8 | TRAN-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| 9 | TRAN-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| 10 | TRAN-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP code |
| 11 | TRAN-CARD-NUM | `X(16)` | Alpha | 16 | Card used for transaction (FK to Card) |
| 12 | TRAN-ORIG-TS | `X(26)` | Timestamp | 26 | Transaction origination timestamp |
| 13 | TRAN-PROC-TS | `X(26)` | Timestamp | 26 | Transaction processing timestamp |
| 14 | FILLER | `X(20)` | Filler | 20 | Reserved space |

**Key:** TRAN-ID (primary), TRAN-CARD-NUM (FK) | **Java Target:** `Transaction` entity

---

## 6. Daily Transaction Entity

**Source Copybook:** `CVTRA06Y.cpy` | **Record Length:** 350 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.DALYTRAN.VSAM.KSDS`

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|------------|-----------|------|--------|---------------------|
| 1 | DALYTRAN-ID | `X(16)` | Alpha | 16 | Daily transaction identifier |
| 2 | DALYTRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| 3 | DALYTRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 4 | DALYTRAN-SOURCE | `X(10)` | Alpha | 10 | Transaction source |
| 5 | DALYTRAN-DESC | `X(100)` | Alpha | 100 | Transaction description |
| 6 | DALYTRAN-AMT | `S9(09)V99` | Signed Decimal | 11 | Transaction amount |
| 7 | DALYTRAN-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant identifier |
| 8 | DALYTRAN-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| 9 | DALYTRAN-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| 10 | DALYTRAN-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP code |
| 11 | DALYTRAN-CARD-NUM | `X(16)` | Alpha | 16 | Card number |
| 12 | DALYTRAN-ORIG-TS | `X(26)` | Timestamp | 26 | Origination timestamp |
| 13 | DALYTRAN-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | `X(20)` | Filler | 20 | Reserved space |

**Purpose:** Staging file for daily batch transaction posting; same layout as Transaction master but separate file for batch isolation.

---

## 7. Transaction Category Balance Entity

**Source Copybook:** `CVTRA01Y.cpy` | **Record Length:** 50 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS`

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|------------|-----------|------|--------|---------------------|
| 1 | TRANCAT-ACCT-ID | `9(11)` | Numeric | 11 | Account ID (FK) |
| 2 | TRANCAT-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| 3 | TRANCAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 4 | TRAN-CAT-BAL | `S9(09)V99` | Signed Decimal | 11 | Running balance for this category |
| 5 | FILLER | `X(22)` | Filler | 22 | Reserved space |

**Key:** Composite (TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD) | **Purpose:** Tracks balance per account per transaction type/category for interest calculation.

---

## 8. Disclosure Group Entity

**Source Copybook:** `CVTRA02Y.cpy` | **Record Length:** 50 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|------------|-----------|------|--------|---------------------|
| 1 | DIS-ACCT-GROUP-ID | `X(10)` | Alpha | 10 | Account group/portfolio ID |
| 2 | DIS-TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| 3 | DIS-TRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 4 | DIS-INT-RATE | `S9(04)V99` | Signed Decimal | 6 | Interest rate for this group/type/category |
| 5 | FILLER | `X(28)` | Filler | 28 | Reserved space |

**Key:** Composite (DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD) | **Purpose:** Interest rate lookup table by account group and transaction classification.

---

## 9. Transaction Type Entity

**Source Copybook:** `CVTRA03Y.cpy` | **Record Length:** 60 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|------------|-----------|------|--------|---------------------|
| 1 | TRAN-TYPE | `X(02)` | Alpha | 2 | Transaction type code (e.g., SA=Sale, RF=Refund) |
| 2 | TRAN-TYPE-DESC | `X(50)` | Alpha | 50 | Transaction type description |
| 3 | FILLER | `X(08)` | Filler | 8 | Reserved space |

**Key:** TRAN-TYPE (primary) | **Java Target:** `TransactionType` reference entity

---

## 10. Transaction Category Entity

**Source Copybook:** `CVTRA04Y.cpy` | **Record Length:** 60 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|------------|-----------|------|--------|---------------------|
| 1 | TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code (FK to Tran Type) |
| 2 | TRAN-CAT-CD | `9(04)` | Numeric | 4 | Category code within type |
| 3 | TRAN-CAT-TYPE-DESC | `X(50)` | Alpha | 50 | Category description |
| 4 | FILLER | `X(04)` | Filler | 4 | Reserved space |

**Key:** Composite (TRAN-TYPE-CD + TRAN-CAT-CD) | **Java Target:** `TransactionCategory` reference entity

---

## 11. User Security Entity

**Source Copybook:** `CSUSR01Y.cpy` | **Record Length:** 80 bytes | **VSAM Dataset:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|------------|-----------|------|--------|---------------------|
| 1 | SEC-USR-ID | `X(08)` | Alpha | 8 | User ID (login name) |
| 2 | SEC-USR-FNAME | `X(20)` | Alpha | 20 | User first name |
| 3 | SEC-USR-LNAME | `X(20)` | Alpha | 20 | User last name |
| 4 | SEC-USR-PWD | `X(08)` | Alpha | 8 | Password (plain text) |
| 5 | SEC-USR-TYPE | `X(01)` | Alpha | 1 | User type (A=Admin, U=Regular user) |
| 6 | FILLER | `X(23)` | Filler | 23 | Reserved space |

**Key:** SEC-USR-ID (primary) | **Security Note:** Password stored in plain text -- must be hashed in Java migration | **Java Target:** `UserSecurity` entity

---

## 12. Common Area (COMMAREA)

**Source Copybook:** `COCOM01Y.cpy` | **Purpose:** Session state passed between CICS programs via XCTL/LINK

| # | Field Group | Key Fields | Business Description |
|---|------------|-----------|---------------------|
| 1 | Program Navigation | CDEMO-FROM-PROGRAM, CDEMO-TO-PROGRAM, CDEMO-FROM-TRANID, CDEMO-TO-TRANID | Source/target program and transaction IDs for screen flow |
| 2 | User Context | CDEMO-USER-ID, CDEMO-USER-TYPE | Current logged-in user identity and role |
| 3 | Account Context | CDEMO-ACCT-ID, CDEMO-CARD-NUM, CDEMO-CUST-ID | Currently selected account/card/customer |
| 4 | Last Map Info | CDEMO-LAST-MAP, CDEMO-LAST-MAPSET | Last BMS map sent to terminal |
| 5 | Flags | CDEMO-PGM-REENTER | Re-entry indicator for pseudo-conversational flow |

**Java Target:** HTTP session attributes or request-scoped DTOs

---

## 13. Statement Transaction Record (Alternate Layout)

**Source Copybook:** `COSTM01.CPY` | **Purpose:** Re-keyed transaction for statement generation

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|------------|-----------|------|--------|---------------------|
| 1 | TRNX-CARD-NUM | `X(16)` | Alpha | 16 | Card number (part of composite key) |
| 2 | TRNX-ID | `X(16)` | Alpha | 16 | Transaction ID (part of composite key) |
| 3 | TRNX-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| 4 | TRNX-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 5 | TRNX-SOURCE | `X(10)` | Alpha | 10 | Transaction source |
| 6 | TRNX-DESC | `X(100)` | Alpha | 100 | Transaction description |
| 7 | TRNX-AMT | `S9(09)V99` | Signed Decimal | 11 | Transaction amount |
| 8 | TRNX-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant ID |
| 9 | TRNX-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| 10 | TRNX-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| 11 | TRNX-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP |
| 12 | TRNX-ORIG-TS | `X(26)` | Timestamp | 26 | Origination timestamp |
| 13 | TRNX-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |

**Purpose:** Same data as CVTRA05Y but keyed by CARD-NUM + TRAN-ID for sorted statement output.

---

## 14. Export/Import Record

**Source Copybook:** `CVEXPORT.cpy` | **Total Record Length:** 500 bytes | **Purpose:** Multi-record branch migration file

### 14a. Header Fields (common to all record types)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|------------|-----------|------|--------|---------------------|
| 1 | EXPORT-REC-TYPE | `X(1)` | Alpha | 1 | Record type (C=Customer, A=Account, X=Xref, T=Transaction, R=Card) |
| 2 | EXPORT-TIMESTAMP | `X(26)` | Timestamp | 26 | Export timestamp (REDEFINES into EXPORT-DATE X(10) + separator + EXPORT-TIME X(15)) |
| 3 | EXPORT-SEQUENCE-NUM | `9(9) COMP` | Binary | 4 | Sequence number within export run |
| 4 | EXPORT-BRANCH-ID | `X(4)` | Alpha | 4 | Originating branch identifier |
| 5 | EXPORT-REGION-CODE | `X(5)` | Alpha | 5 | Region code |
| 6 | EXPORT-RECORD-DATA | `X(460)` | Group | 460 | Record-type-specific data (REDEFINES below) |

### 14b. Customer Record (EXPORT-CUSTOMER-DATA REDEFINES EXPORT-RECORD-DATA)

| # | Field Name | PIC Clause | Type | Bytes | Business Description |
|---|------------|-----------|------|-------|---------------------|
| 1 | EXP-CUST-ID | `9(09) COMP` | Binary | 4 | Customer ID |
| 2 | EXP-CUST-FIRST-NAME | `X(25)` | Alpha | 25 | First name |
| 3 | EXP-CUST-MIDDLE-NAME | `X(25)` | Alpha | 25 | Middle name |
| 4 | EXP-CUST-LAST-NAME | `X(25)` | Alpha | 25 | Last name |
| 5 | EXP-CUST-ADDR-LINE (x3) | `X(50)` | Alpha | 150 | Address lines (OCCURS 3 TIMES) |
| 6 | EXP-CUST-ADDR-STATE-CD | `X(02)` | Alpha | 2 | State code |
| 7 | EXP-CUST-ADDR-COUNTRY-CD | `X(03)` | Alpha | 3 | Country code |
| 8 | EXP-CUST-ADDR-ZIP | `X(10)` | Alpha | 10 | ZIP / postal code |
| 9 | EXP-CUST-PHONE-NUM (x2) | `X(15)` | Alpha | 30 | Phone numbers (OCCURS 2 TIMES) |
| 10 | EXP-CUST-SSN | `9(09)` | Numeric | 9 | Social Security Number |
| 11 | EXP-CUST-GOVT-ISSUED-ID | `X(20)` | Alpha | 20 | Government-issued ID |
| 12 | EXP-CUST-DOB-YYYY-MM-DD | `X(10)` | Date String | 10 | Date of birth |
| 13 | EXP-CUST-EFT-ACCOUNT-ID | `X(10)` | Alpha | 10 | EFT/ACH account |
| 14 | EXP-CUST-PRI-CARD-HOLDER-IND | `X(01)` | Alpha | 1 | Primary cardholder indicator |
| 15 | EXP-CUST-FICO-CREDIT-SCORE | `9(03) COMP-3` | Packed | 2 | FICO credit score |
| 16 | FILLER | `X(134)` | Filler | 134 | Reserved |

### 14c. Account Record (EXPORT-ACCOUNT-DATA REDEFINES EXPORT-RECORD-DATA)

| # | Field Name | PIC Clause | Type | Bytes | Business Description |
|---|------------|-----------|------|-------|---------------------|
| 1 | EXP-ACCT-ID | `9(11)` | Numeric | 11 | Account ID |
| 2 | EXP-ACCT-ACTIVE-STATUS | `X(01)` | Alpha | 1 | Active status |
| 3 | EXP-ACCT-CURR-BAL | `S9(10)V99 COMP-3` | Packed | 7 | Current balance |
| 4 | EXP-ACCT-CREDIT-LIMIT | `S9(10)V99` | Signed Display | 13 | Credit limit |
| 5 | EXP-ACCT-CASH-CREDIT-LIMIT | `S9(10)V99 COMP-3` | Packed | 7 | Cash credit limit |
| 6 | EXP-ACCT-OPEN-DATE | `X(10)` | Date String | 10 | Account open date |
| 7 | EXP-ACCT-EXPIRAION-DATE | `X(10)` | Date String | 10 | Account expiration date |
| 8 | EXP-ACCT-REISSUE-DATE | `X(10)` | Date String | 10 | Reissue date |
| 9 | EXP-ACCT-CURR-CYC-CREDIT | `S9(10)V99` | Signed Display | 13 | Current cycle credit |
| 10 | EXP-ACCT-CURR-CYC-DEBIT | `S9(10)V99 COMP` | Binary | 8 | Current cycle debit |
| 11 | EXP-ACCT-ADDR-ZIP | `X(10)` | Alpha | 10 | Account ZIP code |
| 12 | EXP-ACCT-GROUP-ID | `X(10)` | Alpha | 10 | Account group ID |
| 13 | FILLER | `X(352)` | Filler | 352 | Reserved |

### 14d. Transaction Record (EXPORT-TRANSACTION-DATA REDEFINES EXPORT-RECORD-DATA)

| # | Field Name | PIC Clause | Type | Bytes | Business Description |
|---|------------|-----------|------|-------|---------------------|
| 1 | EXP-TRAN-ID | `X(16)` | Alpha | 16 | Transaction ID |
| 2 | EXP-TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| 3 | EXP-TRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 4 | EXP-TRAN-SOURCE | `X(10)` | Alpha | 10 | Transaction source |
| 5 | EXP-TRAN-DESC | `X(100)` | Alpha | 100 | Transaction description |
| 6 | EXP-TRAN-AMT | `S9(09)V99 COMP-3` | Packed | 6 | Transaction amount |
| 7 | EXP-TRAN-MERCHANT-ID | `9(09) COMP` | Binary | 4 | Merchant ID |
| 8 | EXP-TRAN-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| 9 | EXP-TRAN-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| 10 | EXP-TRAN-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP code |
| 11 | EXP-TRAN-CARD-NUM | `X(16)` | Alpha | 16 | Card number |
| 12 | EXP-TRAN-ORIG-TS | `X(26)` | Timestamp | 26 | Origination timestamp |
| 13 | EXP-TRAN-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | `X(140)` | Filler | 140 | Reserved |

### 14e. Card Cross-Reference (EXPORT-CARD-XREF-DATA REDEFINES EXPORT-RECORD-DATA)

| # | Field Name | PIC Clause | Type | Bytes | Business Description |
|---|------------|-----------|------|-------|---------------------|
| 1 | EXP-XREF-CARD-NUM | `X(16)` | Alpha | 16 | Card number |
| 2 | EXP-XREF-CUST-ID | `9(09)` | Numeric | 9 | Customer ID |
| 3 | EXP-XREF-ACCT-ID | `9(11) COMP` | Binary | 4 | Account ID |
| 4 | FILLER | `X(427)` | Filler | 427 | Reserved |

### 14f. Card Record (EXPORT-CARD-DATA REDEFINES EXPORT-RECORD-DATA)

| # | Field Name | PIC Clause | Type | Bytes | Business Description |
|---|------------|-----------|------|-------|---------------------|
| 1 | EXP-CARD-NUM | `X(16)` | Alpha | 16 | Card number |
| 2 | EXP-CARD-ACCT-ID | `9(11) COMP` | Binary | 4 | Account ID |
| 3 | EXP-CARD-CVV-CD | `9(03) COMP` | Binary | 2 | Card CVV code |
| 4 | EXP-CARD-EMBOSSED-NAME | `X(50)` | Alpha | 50 | Embossed name |
| 5 | EXP-CARD-EXPIRAION-DATE | `X(10)` | Date String | 10 | Card expiration date |
| 6 | EXP-CARD-ACTIVE-STATUS | `X(01)` | Alpha | 1 | Card active status |
| 7 | FILLER | `X(373)` | Filler | 373 | Reserved |

**Note:** Numeric fields use mixed storage formats -- `COMP` (binary), `COMP-3` (packed decimal), and DISPLAY (character). Byte lengths for COMP/COMP-3 fields differ from digit counts (e.g., `9(09) COMP` = 4 bytes, `S9(10)V99 COMP-3` = 7 bytes). This is critical for correct byte-level data mapping during Java migration.

---

## 15. Report Layout Structures

**Source Copybook:** `CVTRA07Y.cpy` | **Purpose:** Print formatting for transaction detail report

| Structure | Key Fields | Description |
|-----------|-----------|-------------|
| REPORT-NAME-HEADER | REPT-SHORT-NAME, REPT-LONG-NAME, REPT-START-DATE, REPT-END-DATE | Report title and date range header |
| TRANSACTION-DETAIL-REPORT | TRAN-REPORT-TRANS-ID, TRAN-REPORT-ACCOUNT-ID, TRAN-REPORT-AMT | Detail line per transaction |
| TRANSACTION-HEADER-1/2 | Column headings | Column headers and separator line |
| REPORT-PAGE-TOTALS | REPT-PAGE-TOTAL | Page-level amount subtotal |
| REPORT-ACCOUNT-TOTALS | REPT-ACCOUNT-TOTAL | Account-level amount subtotal |
| REPORT-GRAND-TOTALS | REPT-GRAND-TOTAL | Report grand total |

---

## 16. Authorization Module Entities (Optional)

### 16a. IMS Authorization Summary Segment

**Source Copybook:** `CIPAUSMY.cpy` | **Database:** PAUTDB (IMS)

| # | Field Name | Business Description |
|---|------------|---------------------|
| 1 | PAUS-CARD-NUM | Card number being authorized |
| 2 | PAUS-TRAN-AMT | Authorization amount |
| 3 | PAUS-TRAN-DT | Authorization date |
| 4 | PAUS-AUTH-STATUS | Authorization status (A=Approved, D=Declined, F=Fraud) |
| 5 | PAUS-MERCHANT-ID | Merchant requesting authorization |

### 16b. IMS Authorization Detail Segment

**Source Copybook:** `CIPAUDTY.cpy` | **Database:** PAUTDB (IMS)

| # | Field Name | Business Description |
|---|------------|---------------------|
| 1 | PAUD-DETAIL-ID | Detail record ID |
| 2 | PAUD-AUTH-REASON-CD | Approval/decline reason code |
| 3 | PAUD-FRAUD-FLAG | Fraud indicator |
| 4 | PAUD-TIMESTAMP | Processing timestamp |

### 16c. MQ Message Layouts

| Copybook | Purpose | Key Fields |
|----------|---------|-----------|
| CCPAURQY.cpy | Authorization Request | Card number, amount, merchant, timestamp |
| CCPAURLY.cpy | Authorization Reply | Approval code, decline reason, auth ID |
| CCPAUERY.cpy | Authorization Error | Error code, error description |

### 16d. IMS PCB Masks

| Copybook | Purpose |
|----------|---------|
| PAUTBPCB.CPY | PCB for main auth database (key length 255) |
| PASFLPCB.CPY | PCB for summary flat file (key length 100) |
| PADFLPCB.CPY | PCB for detail flat file (key length 255) |

---

## 17. DB2 Transaction Type Entities (Optional)

### 17a. TRTYP Table (Transaction Type)

**Source:** DB2 DCLTRTYP (INCLUDE in programs) | **Module:** `app-transaction-type-db2`

| Column | COBOL Host Var | Type | Description |
|--------|---------------|------|-------------|
| TR_TYPE | TR-TYPE | CHAR(2) | Transaction type code |
| TR_TYPE_DESC | TR-TYPE-DESC | VARCHAR(50) | Type description |
| TR_CAT_CD | TR-CAT-CD | INTEGER | Category code |

### 17b. TRCAT Table (Transaction Category)

**Source:** DB2 DCLTRCAT (INCLUDE in programs)

| Column | Type | Description |
|--------|------|-------------|
| TR_TYPE_CD | CHAR(2) | Transaction type code (FK) |
| TR_CAT_CD | INTEGER | Category code |
| TR_CAT_DESC | VARCHAR(50) | Category description |

### 17c. DB2 Common Working Storage

**Source Copybook:** `CSDB2RWY.cpy` | **Purpose:** Shared DB2 processing variables

| Field | PIC Clause | Description |
|-------|-----------|-------------|
| WS-DISP-SQLCODE | `----9` | Displayable SQLCODE |
| WS-DUMMY-DB2-INT | `S9(4) COMP-3` | Priming query target |
| WS-DB2-PROCESSING-FLAG | `X(1)` | 88-level: WS-DB2-OK / WS-DB2-ERROR |
| WS-DB2-CURRENT-ACTION | `X(72)` | Current DB2 action description |
| WS-DSNTIAC-FORMATTED | Group | DSNTIAC formatted error messages (10 x 72 chars) |

---

## 18. Utility / Infrastructure Copybooks

| Copybook | LOC | Purpose | Key Fields |
|----------|-----|---------|-----------|
| **COMEN02Y.cpy** | 40 | Menu option-to-program mapping | CDEMO-MENU-OPT-NAME(12), CDEMO-MENU-OPT-PGMNAME(12) |
| **COADM02Y.cpy** | 18 | Admin menu option-to-program mapping | CDEMO-ADMIN-OPT-NAME(12), CDEMO-ADMIN-OPT-PGMNAME(12) |
| **COTTL01Y.cpy** | 8 | Screen title definitions | CCDA-TITLE01, CCDA-TITLE02 |
| **CSDAT01Y.cpy** | 20 | Date formatting variables | WS-CURDATE, WS-CURTIME, WS-CURDATE-MM/DD/YY |
| **CSMSG01Y.cpy** | 5 | Short message working storage | WS-MESSAGE (X(80)) |
| **CSMSG02Y.cpy** | 12 | Long message working storage | WS-LONG-MSG (X(500)), WS-RETURN-MSG |
| **CSSETATY.cpy** | 7 | BMS attribute byte setter (COPY REPLACING) | Generic attr-setting paragraph |
| **CSSTRPFY.cpy** | 42 | String padding/formatting procedures | 9800-STRIP-FIELD paragraph |
| **CSUTLDPY.cpy** | 15 | Date utility procedure division | Calls CEEDAYS for date math |
| **CSUTLDWY.cpy** | 18 | Date utility working-storage | CSUTLDTC-DATE group |
| **CSLKPCDY.cpy** | 30 | Lookup code tables | Card status codes, account status codes |
| **CODATECN.cpy** | 12 | ASM date conversion record | CODATECN-REC used with COBDATFT |
| **CVCRD01Y.cpy** | 15 | Card record working storage | Internal card fields used in online programs |
| **IMSFUNCS.cpy** | 15 | IMS DL/I function codes | FUNC-GU, FUNC-GN, FUNC-GNP, FUNC-ISRT, etc. |
| **UNUSED1Y.cpy** | 10 | Deprecated/unused record | UNUSED-DATA (80 bytes) |

---

## 19. VSAM Dataset Summary

| VSAM Dataset (DSN) | Type | Record Len | Key Field | Key Len | Source Copybook | Business Entity |
|---------------------|------|-----------|-----------|---------|-----------------|-----------------|
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | KSDS | 300 | ACCT-ID | 11 | CVACT01Y | Account |
| AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | KSDS | 150 | CARD-NUM | 16 | CVACT02Y | Card |
| AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | KSDS | 500 | CUST-ID | 9 | CVCUS01Y | Customer |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | KSDS | 50 | XREF-CARD-NUM | 16 | CVACT03Y | Card-Account XREF |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | KSDS | 350 | TRAN-ID | 16 | CVTRA05Y | Transaction |
| AWS.M2.CARDDEMO.DALYTRAN.VSAM.KSDS | KSDS | 350 | DALYTRAN-ID | 16 | CVTRA06Y | Daily Transaction |
| AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS | KSDS | 50 | Composite | 17 | CVTRA01Y | Tran Cat Balance |
| AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | KSDS | 50 | Composite | 16 | CVTRA02Y | Disclosure Group |
| AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | KSDS | 60 | TRAN-TYPE | 2 | CVTRA03Y | Transaction Type |
| AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | KSDS | 60 | Composite | 6 | CVTRA04Y | Transaction Category |
| AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | KSDS | 80 | SEC-USR-ID | 8 | CSUSR01Y | User Security |

---

## 20. Entity Relationship Summary

```
Customer (CVCUS01Y)
  |
  +-- 1:N --> Account (CVACT01Y) via XREF
  |             |
  |             +-- 1:N --> Card (CVACT02Y) via CARD-ACCT-ID
  |             |
  |             +-- 1:N --> Transaction (CVTRA05Y) via XREF lookup
  |             |
  |             +-- 1:N --> Tran Cat Balance (CVTRA01Y) via TRANCAT-ACCT-ID
  |             |
  |             +-- N:1 --> Disclosure Group (CVTRA02Y) via ACCT-GROUP-ID
  |
  +-- 1:N --> Card Cross-Ref (CVACT03Y) via XREF-CUST-ID
                |
                +-- links Card <-> Account <-> Customer

Transaction Type (CVTRA03Y)
  |
  +-- 1:N --> Transaction Category (CVTRA04Y) via TRAN-TYPE-CD

User Security (CSUSR01Y) -- independent auth entity

Daily Transaction (CVTRA06Y) -- staging entity, same shape as Transaction
```
