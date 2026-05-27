# CardDemo Data Dictionary

> **System**: CardDemo — Mainframe Credit Card Management System
> **Source**: Extracted from COBOL copybook PIC clauses and VSAM/DB2 definitions
> **Last Updated**: 2026-05-27

---

## 1. Customer Entity

**Source Copybook**: `CVCUS01Y.cpy` · **Record Length**: 500 bytes (FB)
**VSAM Dataset**: `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`
**Flat File**: `AWS.M2.CARDDEMO.CUSTDATA.PS`

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| CUST-ID | `PIC 9(09)` | Numeric (Zoned) | 9 | Customer unique identifier (primary key) |
| CUST-FIRST-NAME | `PIC X(25)` | Alphanumeric | 25 | Customer first name |
| CUST-MIDDLE-NAME | `PIC X(25)` | Alphanumeric | 25 | Customer middle name |
| CUST-LAST-NAME | `PIC X(25)` | Alphanumeric | 25 | Customer last name |
| CUST-ADDR-LINE-1 | `PIC X(50)` | Alphanumeric | 50 | Address line 1 |
| CUST-ADDR-LINE-2 | `PIC X(50)` | Alphanumeric | 50 | Address line 2 |
| CUST-ADDR-LINE-3 | `PIC X(50)` | Alphanumeric | 50 | Address line 3 |
| CUST-ADDR-STATE-CD | `PIC X(02)` | Alphanumeric | 2 | US state code (validated against CSLKPCDY) |
| CUST-ADDR-COUNTRY-CD | `PIC X(03)` | Alphanumeric | 3 | Country code |
| CUST-ADDR-ZIP | `PIC X(10)` | Alphanumeric | 10 | ZIP / postal code |
| CUST-PHONE-NUM-1 | `PIC X(15)` | Alphanumeric | 15 | Primary phone number |
| CUST-PHONE-NUM-2 | `PIC X(15)` | Alphanumeric | 15 | Secondary phone number |
| CUST-SSN | `PIC 9(09)` | Numeric (Zoned) | 9 | Social Security Number |
| CUST-GOVT-ISSUED-ID | `PIC X(20)` | Alphanumeric | 20 | Government-issued ID |
| CUST-DOB-YYYY-MM-DD | `PIC X(10)` | Alphanumeric | 10 | Date of birth (YYYY-MM-DD format) |
| CUST-EFT-ACCOUNT-ID | `PIC X(10)` | Alphanumeric | 10 | Electronic funds transfer account |
| CUST-PRI-CARD-HOLDER-IND | `PIC X(01)` | Alphanumeric | 1 | Primary cardholder indicator |
| CUST-FICO-CREDIT-SCORE | `PIC 9(03)` | Numeric (Zoned) | 3 | FICO credit score (300–850) |
| FILLER | `PIC X(168)` | Filler | 168 | Reserved for future use |

**Business Rules**:
- CUST-ID is the primary key used in XREF to link customers to accounts and cards
- Phone area codes validated against NANPA list in CSLKPCDY
- State codes validated against 50 US states + territories
- State-ZIP combinations cross-validated

---

## 2. Account Entity

**Source Copybook**: `CVACT01Y.cpy` · **Record Length**: 300 bytes (FB)
**VSAM Dataset**: `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`
**Flat File**: `AWS.M2.CARDDEMO.ACCTDATA.PS`

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| ACCT-ID | `PIC 9(11)` | Numeric (Zoned) | 11 | Account unique identifier (primary key) |
| ACCT-ACTIVE-STATUS | `PIC X(01)` | Alphanumeric | 1 | Account status (Active/Inactive) |
| ACCT-CURR-BAL | `PIC S9(10)V99` | Signed Numeric + Decimal | 12 | Current account balance |
| ACCT-CREDIT-LIMIT | `PIC S9(10)V99` | Signed Numeric + Decimal | 12 | Credit limit |
| ACCT-CASH-CREDIT-LIMIT | `PIC S9(10)V99` | Signed Numeric + Decimal | 12 | Cash advance credit limit |
| ACCT-OPEN-DATE | `PIC X(10)` | Alphanumeric | 10 | Account open date |
| ACCT-EXPIRAION-DATE | `PIC X(10)` | Alphanumeric | 10 | Account expiration date |
| ACCT-REISSUE-DATE | `PIC X(10)` | Alphanumeric | 10 | Card reissue date |
| ACCT-CURR-CYC-CREDIT | `PIC S9(10)V99` | Signed Numeric + Decimal | 12 | Current cycle credits |
| ACCT-CURR-CYC-DEBIT | `PIC S9(10)V99` | Signed Numeric + Decimal | 12 | Current cycle debits |
| ACCT-ADDR-ZIP | `PIC X(10)` | Alphanumeric | 10 | Account billing ZIP code |
| ACCT-GROUP-ID | `PIC X(10)` | Alphanumeric | 10 | Disclosure group ID (links to DIS-GROUP) |
| FILLER | `PIC X(178)` | Filler | 178 | Reserved for future use |

**Business Rules**:
- Balance = Prior Balance + Cycle Debits − Cycle Credits
- ACCT-GROUP-ID links to Disclosure Group for interest rate lookup
- Interest calculated monthly by CBACT04C using disclosure group rates
- Dates stored as text strings in YYYY-MM-DD format

---

## 3. Credit Card Entity

**Source Copybook**: `CVACT02Y.cpy` · **Record Length**: 150 bytes (FB)
**VSAM Dataset**: `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`
**Flat File**: `AWS.M2.CARDDEMO.CARDDATA.PS`

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| CARD-NUM | `PIC X(16)` | Alphanumeric | 16 | Credit card number (primary key) |
| CARD-ACCT-ID | `PIC 9(11)` | Numeric (Zoned) | 11 | Associated account ID (FK → Account) |
| CARD-CVV-CD | `PIC 9(03)` | Numeric (Zoned) | 3 | Card verification value |
| CARD-EMBOSSED-NAME | `PIC X(50)` | Alphanumeric | 50 | Name printed on card |
| CARD-EXPIRAION-DATE | `PIC X(10)` | Alphanumeric | 10 | Card expiration date |
| CARD-ACTIVE-STATUS | `PIC X(01)` | Alphanumeric | 1 | Card status (Active/Inactive) |
| FILLER | `PIC X(59)` | Filler | 59 | Reserved for future use |

**Business Rules**:
- A card is accessed via VSAM KSDS by CARD-NUM (primary) or by CARD-ACCT-ID (via AIX)
- Multiple cards may belong to one account

---

## 4. Customer-Account-Card Cross-Reference

**Source Copybook**: `CVACT03Y.cpy` · **Record Length**: 50 bytes (FB)
**VSAM Dataset**: `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`
**Flat File**: `AWS.M2.CARDDEMO.CARDXREF.PS`

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| XREF-CARD-NUM | `PIC X(16)` | Alphanumeric | 16 | Card number (primary key) |
| XREF-CUST-ID | `PIC 9(09)` | Numeric (Zoned) | 9 | Customer ID (FK → Customer) |
| XREF-ACCT-ID | `PIC 9(11)` | Numeric (Zoned) | 11 | Account ID (FK → Account) |
| FILLER | `PIC X(14)` | Filler | 14 | Reserved for future use |

**Business Rules**:
- Central cross-reference linking all three master entities
- Keyed by card number; used to look up customer and account from a card
- AIX defined on ACCT-ID for reverse lookups

---

## 5. Transaction Entity (Online)

**Source Copybook**: `CVTRA05Y.cpy` · **Record Length**: 350 bytes (FB)
**VSAM Dataset**: `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| TRAN-ID | `PIC X(16)` | Alphanumeric | 16 | Transaction ID (system-generated key) |
| TRAN-TYPE-CD | `PIC X(02)` | Alphanumeric | 2 | Transaction type code (FK → TRAN-TYPE) |
| TRAN-CAT-CD | `PIC 9(04)` | Numeric (Zoned) | 4 | Transaction category code (FK → TRAN-CAT) |
| TRAN-SOURCE | `PIC X(10)` | Alphanumeric | 10 | Transaction source system |
| TRAN-DESC | `PIC X(100)` | Alphanumeric | 100 | Transaction description |
| TRAN-AMT | `PIC S9(09)V99` | Signed Numeric + Decimal | 11 | Transaction amount |
| TRAN-MERCHANT-ID | `PIC 9(09)` | Numeric (Zoned) | 9 | Merchant identifier |
| TRAN-MERCHANT-NAME | `PIC X(50)` | Alphanumeric | 50 | Merchant name |
| TRAN-MERCHANT-CITY | `PIC X(50)` | Alphanumeric | 50 | Merchant city |
| TRAN-MERCHANT-ZIP | `PIC X(10)` | Alphanumeric | 10 | Merchant ZIP code |
| TRAN-CARD-NUM | `PIC X(16)` | Alphanumeric | 16 | Card number used (FK → Card) |
| TRAN-ORIG-TS | `PIC X(26)` | Alphanumeric | 26 | Original transaction timestamp |
| TRAN-PROC-TS | `PIC X(26)` | Alphanumeric | 26 | Processing timestamp |
| FILLER | `PIC X(20)` | Filler | 20 | Reserved for future use |

---

## 6. Daily Transaction Entity (Batch Input)

**Source Copybook**: `CVTRA06Y.cpy` · **Record Length**: 350 bytes (FB)
**Flat File**: `AWS.M2.CARDDEMO.DALYTRAN.PS`

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| DALYTRAN-ID | `PIC X(16)` | Alphanumeric | 16 | Daily transaction ID |
| DALYTRAN-TYPE-CD | `PIC X(02)` | Alphanumeric | 2 | Transaction type code |
| DALYTRAN-CAT-CD | `PIC 9(04)` | Numeric (Zoned) | 4 | Transaction category code |
| DALYTRAN-SOURCE | `PIC X(10)` | Alphanumeric | 10 | Transaction source |
| DALYTRAN-DESC | `PIC X(100)` | Alphanumeric | 100 | Transaction description |
| DALYTRAN-AMT | `PIC S9(09)V99` | Signed Numeric + Decimal | 11 | Transaction amount |
| DALYTRAN-MERCHANT-ID | `PIC 9(09)` | Numeric (Zoned) | 9 | Merchant identifier |
| DALYTRAN-MERCHANT-NAME | `PIC X(50)` | Alphanumeric | 50 | Merchant name |
| DALYTRAN-MERCHANT-CITY | `PIC X(50)` | Alphanumeric | 50 | Merchant city |
| DALYTRAN-MERCHANT-ZIP | `PIC X(10)` | Alphanumeric | 10 | Merchant ZIP code |
| DALYTRAN-CARD-NUM | `PIC X(16)` | Alphanumeric | 16 | Card number |
| DALYTRAN-ORIG-TS | `PIC X(26)` | Alphanumeric | 26 | Original timestamp |
| DALYTRAN-PROC-TS | `PIC X(26)` | Alphanumeric | 26 | Processing timestamp |
| FILLER | `PIC X(20)` | Filler | 20 | Reserved |

**Business Rules**:
- Daily transactions are batch input consumed by POSTTRAN (CBTRN02C)
- After posting, they are written to the Transaction VSAM file (CVTRA05Y)
- Rejected transactions written to DALYREJS GDG file

---

## 7. Transaction Category Balance

**Source Copybook**: `CVTRA01Y.cpy` · **Record Length**: 50 bytes (FB)
**VSAM Dataset**: `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| TRANCAT-ACCT-ID | `PIC 9(11)` | Numeric (Zoned) | 11 | Account ID (part of composite key) |
| TRANCAT-TYPE-CD | `PIC X(02)` | Alphanumeric | 2 | Transaction type code (part of composite key) |
| TRANCAT-CD | `PIC 9(04)` | Numeric (Zoned) | 4 | Category code (part of composite key) |
| TRAN-CAT-BAL | `PIC S9(09)V99` | Signed Numeric + Decimal | 11 | Running balance for this category |
| FILLER | `PIC X(22)` | Filler | 22 | Reserved |

**Business Rules**:
- Composite key: ACCT-ID + TYPE-CD + CAT-CD
- Updated during transaction posting and interest calculation
- Used for category-level balance tracking per account

---

## 8. Disclosure Group

**Source Copybook**: `CVTRA02Y.cpy` · **Record Length**: 50 bytes (FB)
**VSAM Dataset**: `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| DIS-ACCT-GROUP-ID | `PIC X(10)` | Alphanumeric | 10 | Account group ID (part of composite key) |
| DIS-TRAN-TYPE-CD | `PIC X(02)` | Alphanumeric | 2 | Transaction type code (part of composite key) |
| DIS-TRAN-CAT-CD | `PIC 9(04)` | Numeric (Zoned) | 4 | Category code (part of composite key) |
| DIS-INT-RATE | `PIC S9(04)V99` | Signed Numeric + Decimal | 6 | Interest rate for this disclosure group |
| FILLER | `PIC X(28)` | Filler | 28 | Reserved |

**Business Rules**:
- Links account groups to interest rates by transaction type and category
- Used by CBACT04C (interest calculation) to determine applicable rates

---

## 9. Transaction Type

**Source Copybook**: `CVTRA03Y.cpy` · **Record Length**: 60 bytes (FB)
**VSAM Dataset**: `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`
**DB2 Table**: `TRNTYPE` (optional module)

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| TRAN-TYPE | `PIC X(02)` | Alphanumeric | 2 | Transaction type code (primary key) |
| TRAN-TYPE-DESC | `PIC X(50)` | Alphanumeric | 50 | Type description |
| FILLER | `PIC X(08)` | Filler | 8 | Reserved |

---

## 10. Transaction Category

**Source Copybook**: `CVTRA04Y.cpy` · **Record Length**: 60 bytes (FB)
**VSAM Dataset**: `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`
**DB2 Table**: `TRNTYCAT` (optional module)

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| TRAN-TYPE-CD | `PIC X(02)` | Alphanumeric | 2 | Transaction type (part of composite key) |
| TRAN-CAT-CD | `PIC 9(04)` | Numeric (Zoned) | 4 | Category code (part of composite key) |
| TRAN-CAT-TYPE-DESC | `PIC X(50)` | Alphanumeric | 50 | Category description |
| FILLER | `PIC X(04)` | Filler | 4 | Reserved |

---

## 11. User Security Record

**Source Copybook**: `CSUSR01Y.cpy` · **Record Length**: 80 bytes (FB)
**VSAM Dataset**: `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`
**Flat File**: `AWS.M2.CARDDEMO.USRSEC.PS`

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| SEC-USR-ID | `PIC X(08)` | Alphanumeric | 8 | User ID (primary key, e.g. USER0001) |
| SEC-USR-FNAME | `PIC X(20)` | Alphanumeric | 20 | User first name |
| SEC-USR-LNAME | `PIC X(20)` | Alphanumeric | 20 | User last name |
| SEC-USR-PWD | `PIC X(08)` | Alphanumeric | 8 | User password (plaintext) |
| SEC-USR-TYPE | `PIC X(01)` | Alphanumeric | 1 | User type: 'A' = Admin, 'U' = Regular User |
| SEC-USR-FILLER | `PIC X(23)` | Filler | 23 | Reserved |

---

## 12. CICS Communication Area (COMMAREA)

**Source Copybook**: `COCOM01Y.cpy`

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| CDEMO-FROM-TRANID | `PIC X(04)` | Alphanumeric | 4 | Source CICS transaction ID |
| CDEMO-FROM-PROGRAM | `PIC X(08)` | Alphanumeric | 8 | Source program name |
| CDEMO-TO-TRANID | `PIC X(04)` | Alphanumeric | 4 | Target CICS transaction ID |
| CDEMO-TO-PROGRAM | `PIC X(08)` | Alphanumeric | 8 | Target program name |
| CDEMO-USER-ID | `PIC X(08)` | Alphanumeric | 8 | Current user ID |
| CDEMO-USER-TYPE | `PIC X(01)` | Alphanumeric | 1 | 'A' = Admin, 'U' = User (88-level) |
| CDEMO-PGM-CONTEXT | `PIC 9(01)` | Numeric | 1 | 0 = Fresh entry, 1 = Re-entry |
| CDEMO-CUST-ID | `PIC 9(09)` | Numeric | 9 | Selected customer ID |
| CDEMO-CUST-FNAME | `PIC X(25)` | Alphanumeric | 25 | Customer first name (display) |
| CDEMO-CUST-MNAME | `PIC X(25)` | Alphanumeric | 25 | Customer middle name (display) |
| CDEMO-CUST-LNAME | `PIC X(25)` | Alphanumeric | 25 | Customer last name (display) |
| CDEMO-ACCT-ID | `PIC 9(11)` | Numeric | 11 | Selected account ID |
| CDEMO-ACCT-STATUS | `PIC X(01)` | Alphanumeric | 1 | Account status |
| CDEMO-CARD-NUM | `PIC 9(16)` | Numeric | 16 | Selected card number |
| CDEMO-LAST-MAP | `PIC X(7)` | Alphanumeric | 7 | Last displayed BMS map name |
| CDEMO-LAST-MAPSET | `PIC X(7)` | Alphanumeric | 7 | Last used BMS mapset name |

---

## 13. Branch Migration Export Record

**Source Copybook**: `CVEXPORT.cpy` · **Record Length**: ~505 bytes

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| EXPORT-REC-TYPE | `PIC X(1)` | Alphanumeric | 1 | Record type indicator |
| EXPORT-TIMESTAMP | `PIC X(26)` | Alphanumeric | 26 | Export timestamp |
| EXPORT-DATE | `PIC X(10)` | Alphanumeric (redefines) | 10 | Date portion |
| EXPORT-TIME | `PIC X(15)` | Alphanumeric (redefines) | 15 | Time portion |
| EXPORT-SEQUENCE-NUM | `PIC 9(9) COMP` | Binary | 4 | Sequence number |
| EXPORT-BRANCH-ID | `PIC X(4)` | Alphanumeric | 4 | Branch identifier |
| EXPORT-REGION-CODE | `PIC X(5)` | Alphanumeric | 5 | Region code |
| EXPORT-RECORD-DATA | `PIC X(460)` | Alphanumeric | 460 | Polymorphic record data |

**Embedded Customer Data** (REDEFINES EXPORT-RECORD-DATA):

| Field Name | PIC Clause | Type | Size |
|:-----------|:-----------|:-----|-----:|
| EXP-CUST-ID | `PIC 9(09) COMP` | Binary | 4 |
| EXP-CUST-FIRST-NAME | `PIC X(25)` | Alphanumeric | 25 |
| EXP-CUST-MIDDLE-NAME | `PIC X(25)` | Alphanumeric | 25 |
| EXP-CUST-LAST-NAME | `PIC X(25)` | Alphanumeric | 25 |
| EXP-CUST-ADDR-LINE (×3) | `PIC X(50)` | OCCURS 3 | 150 |
| EXP-CUST-ADDR-STATE-CD | `PIC X(02)` | Alphanumeric | 2 |
| EXP-CUST-ADDR-COUNTRY-CD | `PIC X(03)` | Alphanumeric | 3 |
| EXP-CUST-ADDR-ZIP | `PIC X(10)` | Alphanumeric | 10 |
| EXP-CUST-PHONE-NUM (×2) | `PIC X(15)` | OCCURS 2 | 30 |
| EXP-CUST-SSN | `PIC 9(09)` | Numeric | 9 |
| EXP-CUST-GOVT-ISSUED-ID | `PIC X(20)` | Alphanumeric | 20 |
| EXP-CUST-DOB-YYYY-MM-DD | `PIC X(10)` | Alphanumeric | 10 |
| EXP-CUST-EFT-ACCOUNT-ID | `PIC X(10)` | Alphanumeric | 10 |
| EXP-CUST-PRI-CARD-HOLDER-IND | `PIC X(01)` | Alphanumeric | 1 |
| EXP-CUST-FICO-CREDIT-SCORE | `PIC 9(03) COMP-3` | Packed Decimal | 2 |

**Data Format Notes**:
- Uses OCCURS for address lines and phone numbers
- FICO score stored as COMP-3 (packed decimal) — different from base CVCUS01Y (zoned)
- REDEFINES allows the same 460-byte area to carry different record types

---

## 14. Authorization Detail Record (IMS)

**Source Copybook**: `CIPAUDTY.cpy` (Optional: IMS-DB2-MQ module)

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| PA-AUTH-DATE-9C | `PIC S9(05) COMP-3` | Packed Decimal | 3 | Auth date (Julian, packed) |
| PA-AUTH-TIME-9C | `PIC S9(09) COMP-3` | Packed Decimal | 5 | Auth time (packed) |
| PA-AUTH-ORIG-DATE | `PIC X(06)` | Alphanumeric | 6 | Original date (MMDDYY) |
| PA-AUTH-ORIG-TIME | `PIC X(06)` | Alphanumeric | 6 | Original time (HHMMSS) |
| PA-CARD-NUM | `PIC X(16)` | Alphanumeric | 16 | Card number |
| PA-AUTH-TYPE | `PIC X(04)` | Alphanumeric | 4 | Authorization type |
| PA-CARD-EXPIRY-DATE | `PIC X(04)` | Alphanumeric | 4 | Card expiry (MMYY) |
| PA-MESSAGE-TYPE | `PIC X(06)` | Alphanumeric | 6 | Message type indicator |
| PA-MESSAGE-SOURCE | `PIC X(06)` | Alphanumeric | 6 | Message source |
| PA-AUTH-ID-CODE | `PIC X(06)` | Alphanumeric | 6 | Authorization ID code |
| PA-AUTH-RESP-CODE | `PIC X(02)` | Alphanumeric | 2 | Response code |
| PA-AUTH-RESP-REASON | `PIC X(04)` | Alphanumeric | 4 | Response reason |
| PA-PROCESSING-CODE | `PIC 9(06)` | Numeric | 6 | Processing code |
| PA-TRANSACTION-AMT | `PIC S9(10)V99 COMP-3` | Packed Decimal | 7 | Transaction amount |
| PA-APPROVED-AMT | `PIC S9(10)V99 COMP-3` | Packed Decimal | 7 | Approved amount |
| PA-MERCHANT-CATAGORY-CODE | `PIC X(04)` | Alphanumeric | 4 | MCC code |
| PA-ACQR-COUNTRY-CODE | `PIC X(03)` | Alphanumeric | 3 | Acquirer country code |
| PA-POS-ENTRY-MODE | `PIC 9(02)` | Numeric | 2 | POS entry mode |
| PA-MERCHANT-ID | `PIC X(15)` | Alphanumeric | 15 | Merchant ID |
| PA-MERCHANT-NAME | `PIC X(22)` | Alphanumeric | 22 | Merchant name |

---

## 15. Authorization Request (MQ Message)

**Source Copybook**: `CCPAURQY.cpy` (Optional: IMS-DB2-MQ module)

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| PA-RQ-AUTH-DATE | `PIC X(06)` | Alphanumeric | 6 | Request date |
| PA-RQ-AUTH-TIME | `PIC X(06)` | Alphanumeric | 6 | Request time |
| PA-RQ-CARD-NUM | `PIC X(16)` | Alphanumeric | 16 | Card number |
| PA-RQ-AUTH-TYPE | `PIC X(04)` | Alphanumeric | 4 | Authorization type |
| PA-RQ-CARD-EXPIRY-DATE | `PIC X(04)` | Alphanumeric | 4 | Card expiry date |
| PA-RQ-MESSAGE-TYPE | `PIC X(06)` | Alphanumeric | 6 | Message type |
| PA-RQ-MESSAGE-SOURCE | `PIC X(06)` | Alphanumeric | 6 | Message source |
| PA-RQ-PROCESSING-CODE | `PIC 9(06)` | Numeric | 6 | Processing code |
| PA-RQ-TRANSACTION-AMT | `PIC +9(10).99` | Edited Numeric | 14 | Transaction amount |
| PA-RQ-MERCHANT-CATAGORY-CODE | `PIC X(04)` | Alphanumeric | 4 | MCC code |
| PA-RQ-ACQR-COUNTRY-CODE | `PIC X(03)` | Alphanumeric | 3 | Acquirer country |
| PA-RQ-POS-ENTRY-MODE | `PIC 9(02)` | Numeric | 2 | POS entry mode |
| PA-RQ-MERCHANT-ID | `PIC X(15)` | Alphanumeric | 15 | Merchant ID |
| PA-RQ-MERCHANT-NAME | `PIC X(22)` | Alphanumeric | 22 | Merchant name |
| PA-RQ-MERCHANT-CITY | `PIC X(13)` | Alphanumeric | 13 | Merchant city |
| PA-RQ-MERCHANT-STATE | `PIC X(02)` | Alphanumeric | 2 | Merchant state |
| PA-RQ-MERCHANT-ZIP | `PIC X(09)` | Alphanumeric | 9 | Merchant ZIP |
| PA-RQ-TRANSACTION-ID | `PIC X(15)` | Alphanumeric | 15 | Transaction ID |

---

## 16. Authorization Summary (IMS)

**Source Copybook**: `CIPAUSMY.cpy` (Optional: IMS-DB2-MQ module)

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| PA-ACCT-ID | `PIC S9(11) COMP-3` | Packed Decimal | 6 | Account ID |
| PA-CUST-ID | `PIC 9(09)` | Numeric | 9 | Customer ID |
| PA-AUTH-STATUS | `PIC X(01)` | Alphanumeric | 1 | Authorization status |
| PA-ACCOUNT-STATUS (×5) | `PIC X(02) OCCURS 5` | OCCURS | 10 | Account status codes |
| PA-CREDIT-LIMIT | `PIC S9(09)V99 COMP-3` | Packed Decimal | 6 | Credit limit |
| PA-CASH-LIMIT | `PIC S9(09)V99 COMP-3` | Packed Decimal | 6 | Cash advance limit |

---

## 17. Date Conversion I/O Area

**Source Copybook**: `CODATECN.cpy`

| Field Name | PIC Clause | Type | Size | Description |
|:-----------|:-----------|:-----|-----:|:------------|
| CODATECN-TYPE | `PIC X` | Alphanumeric | 1 | Input type: '1'=YYYYMMDD, '2'=YYYY-MM-DD |
| CODATECN-INP-DATE | `PIC X(20)` | Alphanumeric | 20 | Input date string |
| CODATECN-OUTTYPE | `PIC X` | Alphanumeric | 1 | Output type: '1'=YYYY-MM-DD, '2'=YYYYMMDD |
| CODATECN-0UT-DATE | `PIC X(20)` | Alphanumeric | 20 | Output date string |
| CODATECN-ERROR-MSG | `PIC X(38)` | Alphanumeric | 38 | Error message |

**Redefines** provide structured access to individual YYYY, MM, DD components of both input and output dates.

---

## 18. Entity Relationship Summary

```
┌──────────┐    ┌──────────┐    ┌──────────┐
│ CUSTOMER │◄──►│  XREF    │◄──►│ ACCOUNT  │
│ CVCUS01Y │    │ CVACT03Y │    │ CVACT01Y │
│ PK:      │    │ PK:      │    │ PK:      │
│ CUST-ID  │    │ CARD-NUM │    │ ACCT-ID  │
└──────────┘    └──────────┘    └──────────┘
                     │               │
                     │               │
                     ▼               │
                ┌──────────┐         │
                │   CARD   │─────────┘
                │ CVACT02Y │
                │ PK:      │
                │ CARD-NUM │
                └──────────┘
                     │
                     ▼
              ┌──────────────┐     ┌───────────┐    ┌──────────┐
              │ TRANSACTION  │────►│ TRAN-TYPE │    │ DISC-GRP │
              │   CVTRA05Y   │     │  CVTRA03Y │    │ CVTRA02Y │
              │ PK: TRAN-ID  │     └───────────┘    └──────────┘
              └──────────────┘           │                │
                     │              ┌────┴────┐           │
                     │              │TRAN-CAT │           │
                     │              │ CVTRA04Y│           │
                     │              └─────────┘           │
                     ▼                                    │
              ┌──────────────┐                            │
              │  CAT-BALANCE │◄───────────────────────────┘
              │   CVTRA01Y   │
              └──────────────┘
```

---

## 19. Data Type Reference

| COBOL PIC | Business Meaning | Java Equivalent | Bytes |
|:----------|:-----------------|:----------------|------:|
| `PIC 9(n)` | Unsigned integer, zoned decimal | `long` / `BigDecimal` | n |
| `PIC S9(n)V99` | Signed decimal with 2 implied decimal places | `BigDecimal` | n+2 |
| `PIC S9(n)V99 COMP-3` | Packed decimal (BCD) | `BigDecimal` | ⌈(n+3)/2⌉ |
| `PIC 9(n) COMP` | Binary integer | `int` / `long` | 2 or 4 |
| `PIC X(n)` | Character string (EBCDIC) | `String` | n |
| `PIC +9(n).99` | Edited numeric for display | `String` (formatted) | n+3 |
