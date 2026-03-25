# Data Dictionary - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Source:** Copybook PIC clause analysis from `app/cpy/`
> **Storage:** VSAM KSDS (Key-Sequenced Data Sets) on z/OS

---

## 1. Account Entity

**Copybook:** `CVACT01Y.cpy` | **Record Size:** 300 bytes | **VSAM Dataset:** `ACCTDATA.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description | Modernization Target |
|------------|-----------|------|------|---------------------|---------------------|
| ACCT-ID | `9(11)` | Numeric | 11 | Account identifier (primary key) | `BIGINT` / `Long` |
| ACCT-ACTIVE-STATUS | `X(01)` | Alpha | 1 | Active status flag (Y/N) | `BOOLEAN` |
| ACCT-CURR-BAL | `S9(10)V99` | Signed Decimal | 12.2 | Current account balance | `DECIMAL(12,2)` |
| ACCT-CREDIT-LIMIT | `S9(10)V99` | Signed Decimal | 12.2 | Credit limit | `DECIMAL(12,2)` |
| ACCT-CASH-CREDIT-LIMIT | `S9(10)V99` | Signed Decimal | 12.2 | Cash advance credit limit | `DECIMAL(12,2)` |
| ACCT-OPEN-DATE | `X(10)` | Date String | 10 | Account open date (YYYY-MM-DD) | `DATE` |
| ACCT-EXPIRAION-DATE | `X(10)` | Date String | 10 | Account expiration date | `DATE` |
| ACCT-REISSUE-DATE | `X(10)` | Date String | 10 | Reissue date | `DATE` |
| ACCT-CURR-CYC-CREDIT | `S9(10)V99` | Signed Decimal | 12.2 | Current cycle credits | `DECIMAL(12,2)` |
| ACCT-CURR-CYC-DEBIT | `S9(10)V99` | Signed Decimal | 12.2 | Current cycle debits | `DECIMAL(12,2)` |
| ACCT-ADDR-ZIP | `X(10)` | Alphanumeric | 10 | ZIP code | `VARCHAR(10)` |
| ACCT-GROUP-ID | `X(10)` | Alphanumeric | 10 | Account group identifier | `VARCHAR(10)` |
| FILLER | `X(178)` | Filler | 178 | Reserved / padding | -- |

**Business Rules:**
- Primary key: `ACCT-ID` (11-digit numeric)
- Status values: Y = Active, N = Inactive
- All monetary fields are signed with 2 decimal places
- Date format: YYYY-MM-DD as alphanumeric (not packed decimal)

---

## 2. Card Entity

**Copybook:** `CVACT02Y.cpy` | **Record Size:** 150 bytes | **VSAM Dataset:** `CARDDATA.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description | Modernization Target |
|------------|-----------|------|------|---------------------|---------------------|
| CARD-NUM | `X(16)` | Alphanumeric | 16 | Card number (primary key) | `VARCHAR(16)` |
| CARD-ACCT-ID | `9(11)` | Numeric | 11 | Associated account ID (FK) | `BIGINT` |
| CARD-CVV-CD | `9(03)` | Numeric | 3 | CVV security code | `SMALLINT` |
| CARD-EMBOSSED-NAME | `X(50)` | Alphanumeric | 50 | Name on card | `VARCHAR(50)` |
| CARD-EXPIRAION-DATE | `X(10)` | Date String | 10 | Card expiration date | `DATE` |
| CARD-ACTIVE-STATUS | `X(01)` | Alpha | 1 | Active status (Y/N) | `BOOLEAN` |
| FILLER | `X(59)` | Filler | 59 | Reserved / padding | -- |

**Business Rules:**
- Primary key: `CARD-NUM` (16-char)
- Foreign key: `CARD-ACCT-ID` references Account entity
- One account can have multiple cards
- Alternate index path: `CARDAIX` for access by account ID

---

## 3. Card-Account Cross-Reference

**Copybook:** `CVACT03Y.cpy` | **Record Size:** 50 bytes | **VSAM Dataset:** `CARDXREF.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description | Modernization Target |
|------------|-----------|------|------|---------------------|---------------------|
| XREF-CARD-NUM | `X(16)` | Alphanumeric | 16 | Card number (key part 1) | `VARCHAR(16)` |
| XREF-CUST-ID | `9(09)` | Numeric | 9 | Customer ID (key part 2) | `INT` |
| XREF-ACCT-ID | `9(11)` | Numeric | 11 | Account ID (key part 3) | `BIGINT` |
| FILLER | `X(14)` | Filler | 14 | Reserved | -- |

**Business Rules:**
- Composite key: Card + Customer + Account
- Links the three core entities together
- Alternate index paths: `CXACAIX` for access by account ID

---

## 4. Customer Entity

**Copybook:** `CVCUS01Y.cpy` | **Record Size:** 500 bytes | **VSAM Dataset:** `CUSTDATA.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description | Modernization Target |
|------------|-----------|------|------|---------------------|---------------------|
| CUST-ID | `9(09)` | Numeric | 9 | Customer ID (primary key) | `INT` |
| CUST-FIRST-NAME | `X(25)` | Alphanumeric | 25 | First name | `VARCHAR(25)` |
| CUST-MIDDLE-NAME | `X(25)` | Alphanumeric | 25 | Middle name | `VARCHAR(25)` |
| CUST-LAST-NAME | `X(25)` | Alphanumeric | 25 | Last name | `VARCHAR(25)` |
| CUST-ADDR-LINE-1 | `X(50)` | Alphanumeric | 50 | Address line 1 | `VARCHAR(50)` |
| CUST-ADDR-LINE-2 | `X(50)` | Alphanumeric | 50 | Address line 2 | `VARCHAR(50)` |
| CUST-ADDR-LINE-3 | `X(50)` | Alphanumeric | 50 | Address line 3 | `VARCHAR(50)` |
| CUST-ADDR-STATE-CD | `X(02)` | Alpha | 2 | State code | `CHAR(2)` |
| CUST-ADDR-COUNTRY-CD | `X(03)` | Alpha | 3 | Country code | `CHAR(3)` |
| CUST-ADDR-ZIP | `X(10)` | Alphanumeric | 10 | ZIP/postal code | `VARCHAR(10)` |
| CUST-PHONE-NUM-1 | `X(15)` | Alphanumeric | 15 | Primary phone | `VARCHAR(15)` |
| CUST-PHONE-NUM-2 | `X(15)` | Alphanumeric | 15 | Secondary phone | `VARCHAR(15)` |
| CUST-SSN | `9(09)` | Numeric | 9 | Social Security Number (PII) | `VARCHAR(9)` (encrypted) |
| CUST-GOVT-ISSUED-ID | `X(20)` | Alphanumeric | 20 | Government-issued ID | `VARCHAR(20)` |
| CUST-DOB-YYYY-MM-DD | `X(10)` | Date String | 10 | Date of birth | `DATE` |
| CUST-EFT-ACCOUNT-ID | `X(10)` | Alphanumeric | 10 | EFT account identifier | `VARCHAR(10)` |
| CUST-PRI-CARD-HOLDER-IND | `X(01)` | Alpha | 1 | Primary cardholder flag | `BOOLEAN` |
| CUST-FICO-CREDIT-SCORE | `9(03)` | Numeric | 3 | FICO credit score | `SMALLINT` |
| FILLER | `X(168)` | Filler | 168 | Reserved | -- |

**Business Rules:**
- Primary key: `CUST-ID` (9-digit)
- Contains PII fields: SSN, DOB, government ID
- FICO score range: 0-999
- Linked to accounts via cross-reference entity

---

## 5. Transaction Entity

**Copybook:** `CVTRA05Y.cpy` | **Record Size:** 350 bytes | **VSAM Dataset:** `TRANSACT.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description | Modernization Target |
|------------|-----------|------|------|---------------------|---------------------|
| TRAN-ID | `X(16)` | Alphanumeric | 16 | Transaction ID (primary key) | `VARCHAR(16)` |
| TRAN-TYPE-CD | `X(02)` | Alphanumeric | 2 | Transaction type code (FK) | `CHAR(2)` |
| TRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code (FK) | `SMALLINT` |
| TRAN-SOURCE | `X(10)` | Alphanumeric | 10 | Transaction source/channel | `VARCHAR(10)` |
| TRAN-DESC | `X(100)` | Alphanumeric | 100 | Transaction description | `VARCHAR(100)` |
| TRAN-AMT | `S9(09)V99` | Signed Decimal | 11.2 | Transaction amount | `DECIMAL(11,2)` |
| TRAN-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant identifier | `INT` |
| TRAN-MERCHANT-NAME | `X(50)` | Alphanumeric | 50 | Merchant name | `VARCHAR(50)` |
| TRAN-MERCHANT-CITY | `X(50)` | Alphanumeric | 50 | Merchant city | `VARCHAR(50)` |
| TRAN-MERCHANT-ZIP | `X(10)` | Alphanumeric | 10 | Merchant ZIP code | `VARCHAR(10)` |
| TRAN-CARD-NUM | `X(16)` | Alphanumeric | 16 | Card number used (FK) | `VARCHAR(16)` |
| TRAN-ORIG-TS | `X(26)` | Timestamp | 26 | Origination timestamp | `TIMESTAMP` |
| TRAN-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp | `TIMESTAMP` |
| FILLER | `X(20)` | Filler | 20 | Reserved | -- |

**Business Rules:**
- Primary key: `TRAN-ID` (16-char generated)
- Transaction amount is signed (positive = debit, negative = credit)
- Timestamps stored as ISO-format strings (26 chars)
- Linked to cards via `TRAN-CARD-NUM`

---

## 6. Daily Transaction Entity

**Copybook:** `CVTRA06Y.cpy` | **Record Size:** 350 bytes | **VSAM Dataset:** `DALYTRAN.PS`

| Field Name | COBOL PIC | Type | Size | Business Description | Modernization Target |
|------------|-----------|------|------|---------------------|---------------------|
| DALYTRAN-ID | `X(16)` | Alphanumeric | 16 | Daily transaction ID | `VARCHAR(16)` |
| DALYTRAN-TYPE-CD | `X(02)` | Alphanumeric | 2 | Transaction type | `CHAR(2)` |
| DALYTRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category | `SMALLINT` |
| DALYTRAN-SOURCE | `X(10)` | Alphanumeric | 10 | Source channel | `VARCHAR(10)` |
| DALYTRAN-DESC | `X(100)` | Alphanumeric | 100 | Description | `VARCHAR(100)` |
| DALYTRAN-AMT | `S9(09)V99` | Signed Decimal | 11.2 | Amount | `DECIMAL(11,2)` |
| DALYTRAN-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant ID | `INT` |
| DALYTRAN-MERCHANT-NAME | `X(50)` | Alphanumeric | 50 | Merchant name | `VARCHAR(50)` |
| DALYTRAN-MERCHANT-CITY | `X(50)` | Alphanumeric | 50 | Merchant city | `VARCHAR(50)` |
| DALYTRAN-MERCHANT-ZIP | `X(10)` | Alphanumeric | 10 | Merchant ZIP | `VARCHAR(10)` |
| DALYTRAN-CARD-NUM | `X(16)` | Alphanumeric | 16 | Card number | `VARCHAR(16)` |
| DALYTRAN-ORIG-TS | `X(26)` | Timestamp | 26 | Origination timestamp | `TIMESTAMP` |
| DALYTRAN-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp | `TIMESTAMP` |
| FILLER | `X(20)` | Filler | 20 | Reserved | -- |

**Business Rules:**
- Identical layout to Transaction (staging table pattern)
- Loaded from daily feed, posted by CBTRN02C into TRANSACT master
- Rejected records written to DALYREJS dataset

---

## 7. Transaction Category Balance

**Copybook:** `CVTRA01Y.cpy` | **Record Size:** 50 bytes | **VSAM Dataset:** `TCATBALF.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description | Modernization Target |
|------------|-----------|------|------|---------------------|---------------------|
| TRANCAT-ACCT-ID | `9(11)` | Numeric | 11 | Account ID (key part 1) | `BIGINT` |
| TRANCAT-TYPE-CD | `X(02)` | Alphanumeric | 2 | Transaction type (key part 2) | `CHAR(2)` |
| TRANCAT-CD | `9(04)` | Numeric | 4 | Category code (key part 3) | `SMALLINT` |
| TRAN-CAT-BAL | `S9(09)V99` | Signed Decimal | 11.2 | Category balance | `DECIMAL(11,2)` |
| FILLER | `X(22)` | Filler | 22 | Reserved | -- |

**Business Rules:**
- Composite key: Account + Type + Category
- Tracks running balance per category per account
- Updated during batch transaction posting

---

## 8. Disclosure Group

**Copybook:** `CVTRA02Y.cpy` | **Record Size:** 50 bytes | **VSAM Dataset:** `DISCGRP.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description | Modernization Target |
|------------|-----------|------|------|---------------------|---------------------|
| DIS-ACCT-GROUP-ID | `X(10)` | Alphanumeric | 10 | Account group ID (key part 1) | `VARCHAR(10)` |
| DIS-TRAN-TYPE-CD | `X(02)` | Alphanumeric | 2 | Transaction type (key part 2) | `CHAR(2)` |
| DIS-TRAN-CAT-CD | `9(04)` | Numeric | 4 | Category code (key part 3) | `SMALLINT` |
| DIS-INT-RATE | `S9(04)V99` | Signed Decimal | 6.2 | Interest rate | `DECIMAL(6,2)` |
| FILLER | `X(28)` | Filler | 28 | Reserved | -- |

**Business Rules:**
- Defines interest rates by account group and transaction category
- Used by interest calculation batch (CBACT04C)

---

## 9. Transaction Type (Reference Data)

**Copybook:** `CVTRA03Y.cpy` | **Record Size:** 60 bytes | **VSAM Dataset:** `TRANTYPE.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description | Modernization Target |
|------------|-----------|------|------|---------------------|---------------------|
| TRAN-TYPE | `X(02)` | Alphanumeric | 2 | Transaction type code (PK) | `CHAR(2)` |
| TRAN-TYPE-DESC | `X(50)` | Alphanumeric | 50 | Type description | `VARCHAR(50)` |
| FILLER | `X(08)` | Filler | 8 | Reserved | -- |

---

## 10. Transaction Category (Reference Data)

**Copybook:** `CVTRA04Y.cpy` | **Record Size:** 60 bytes | **VSAM Dataset:** `TRANCATG.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description | Modernization Target |
|------------|-----------|------|------|---------------------|---------------------|
| TRAN-TYPE-CD | `X(02)` | Alphanumeric | 2 | Type code (key part 1) | `CHAR(2)` |
| TRAN-CAT-CD | `9(04)` | Numeric | 4 | Category code (key part 2) | `SMALLINT` |
| TRAN-CAT-TYPE-DESC | `X(50)` | Alphanumeric | 50 | Category description | `VARCHAR(50)` |
| FILLER | `X(04)` | Filler | 4 | Reserved | -- |

---

## 11. User Security Record

**Copybook:** `CSUSR01Y.cpy` | **VSAM Dataset:** `USRSEC.VSAM.KSDS`

| Field Name | COBOL PIC | Type | Size | Business Description | Modernization Target |
|------------|-----------|------|------|---------------------|---------------------|
| SEC-USR-ID | `X(08)` | Alphanumeric | 8 | User ID (primary key) | `VARCHAR(8)` |
| SEC-USR-FNAME | `X(20)` | Alphanumeric | 20 | First name | `VARCHAR(20)` |
| SEC-USR-LNAME | `X(20)` | Alphanumeric | 20 | Last name | `VARCHAR(20)` |
| SEC-USR-PWD | `X(08)` | Alphanumeric | 8 | Password (plaintext!) | `VARCHAR(255)` (hashed) |
| SEC-USR-TYPE | `X(01)` | Alpha | 1 | User type (A=Admin, U=User) | `ENUM('ADMIN','USER')` |
| FILLER | `X(23)` | Filler | 23 | Reserved | -- |

**Business Rules:**
- Password stored in plaintext (security risk for modernization)
- Two user types: Admin (A) and Regular User (U)
- Admin users access COADM01C menu; regular users access COMEN01C

---

## 12. Communication Area (COMMAREA)

**Copybook:** `COCOM01Y.cpy` | **Purpose:** Inter-program data passing via CICS COMMAREA

| Field Name | COBOL PIC | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| CDEMO-FROM-TRANID | `X(04)` | Alphanumeric | 4 | Source transaction ID |
| CDEMO-FROM-PROGRAM | `X(08)` | Alphanumeric | 8 | Source program name |
| CDEMO-TO-TRANID | `X(04)` | Alphanumeric | 4 | Target transaction ID |
| CDEMO-TO-PROGRAM | `X(08)` | Alphanumeric | 8 | Target program name |
| CDEMO-USER-ID | `X(08)` | Alphanumeric | 8 | Logged-in user ID |
| CDEMO-USER-TYPE | `X(01)` | Alpha | 1 | User type (A/U) |
| CDEMO-PGM-CONTEXT | `9(01)` | Numeric | 1 | Program context (0=Enter, 1=Reenter) |
| CDEMO-CUST-ID | `9(09)` | Numeric | 9 | Current customer ID |
| CDEMO-CUST-FNAME | `X(25)` | Alphanumeric | 25 | Customer first name |
| CDEMO-CUST-MNAME | `X(25)` | Alphanumeric | 25 | Customer middle name |
| CDEMO-CUST-LNAME | `X(25)` | Alphanumeric | 25 | Customer last name |
| CDEMO-ACCT-ID | `9(11)` | Numeric | 11 | Current account ID |
| CDEMO-ACCT-STATUS | `X(01)` | Alpha | 1 | Account status |
| CDEMO-CARD-NUM | `9(16)` | Numeric | 16 | Current card number |
| CDEMO-LAST-MAP | `X(7)` | Alphanumeric | 7 | Last displayed BMS map |
| CDEMO-LAST-MAPSET | `X(7)` | Alphanumeric | 7 | Last displayed mapset |

**Modernization Note:** This COMMAREA maps to HTTP session state or JWT claims in a modernized application.

---

## 13. Statement Transaction (Rekeyed)

**Copybook:** `COSTM01.CPY` | **Record Size:** 350 bytes | **Purpose:** Statement generation

| Field Name | COBOL PIC | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| TRNX-CARD-NUM | `X(16)` | Alphanumeric | 16 | Card number (key part 1) |
| TRNX-ID | `X(16)` | Alphanumeric | 16 | Transaction ID (key part 2) |
| TRNX-TYPE-CD | `X(02)` | Alphanumeric | 2 | Transaction type |
| TRNX-CAT-CD | `9(04)` | Numeric | 4 | Category code |
| TRNX-SOURCE | `X(10)` | Alphanumeric | 10 | Source channel |
| TRNX-DESC | `X(100)` | Alphanumeric | 100 | Description |
| TRNX-AMT | `S9(09)V99` | Signed Decimal | 11.2 | Amount |
| TRNX-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant ID |
| TRNX-MERCHANT-NAME | `X(50)` | Alphanumeric | 50 | Merchant name |
| TRNX-MERCHANT-CITY | `X(50)` | Alphanumeric | 50 | Merchant city |
| TRNX-MERCHANT-ZIP | `X(10)` | Alphanumeric | 10 | Merchant ZIP |
| TRNX-ORIG-TS | `X(26)` | Timestamp | 26 | Origination timestamp |
| TRNX-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| FILLER | `X(20)` | Filler | 20 | Reserved |

**Note:** Same fields as CVTRA05Y but re-keyed by Card+TranID for statement generation ordering.

---

## 14. Export Record Layout

**Copybook:** `CVEXPORT.cpy` | **Purpose:** Branch migration data export

| Field Name | COBOL PIC | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| EXP-RECORD-TYPE | `X(01)` | Alpha | 1 | Record type discriminator |
| EXP-CUST-ID | `9(09)` | Numeric | 9 | Customer ID |
| EXP-ACCT-ID | `9(11)` | Numeric | 11 | Account ID |
| EXP-CARD-NUM | `X(16)` | Alphanumeric | 16 | Card number |
| EXP-CARD-ACCT-ID | `9(11)` | Numeric | 11 | Card's account ID |
| EXP-CARD-CVV-CD | `9(03)` | Numeric | 3 | CVV code |
| EXP-CARD-EMBOSSED-NAME | `X(50)` | Alphanumeric | 50 | Name on card |
| EXP-CARD-EXPIRAION-DATE | `X(10)` | Date String | 10 | Card expiry date |
| EXP-CARD-ACTIVE-STATUS | `X(01)` | Alpha | 1 | Active flag |

**Note:** Multi-record export format using record-type discriminator for polymorphic records.

---

## 15. Entity Relationship Summary

```
                    ┌─────────────┐
                    │   Customer  │
                    │  CVCUS01Y   │
                    │  PK: CUST-ID│
                    └──────┬──────┘
                           │ 1:N
                    ┌──────┴──────┐
                    │  Card-Xref  │
                    │  CVACT03Y   │
                    │  CARD+CUST  │
                    │  +ACCT      │
                    └──┬─────┬───┘
                  1:N  │     │ N:1
          ┌────────────┘     └────────────┐
   ┌──────┴──────┐                 ┌──────┴──────┐
   │    Card     │                 │   Account   │
   │  CVACT02Y   │                 │  CVACT01Y   │
   │ PK: CARD-NUM│                 │ PK: ACCT-ID │
   └──────┬──────┘                 └──────┬──────┘
          │ 1:N                           │ 1:N
   ┌──────┴──────┐                 ┌──────┴──────┐
   │ Transaction │                 │  Cat Balance │
   │  CVTRA05Y   │                 │  CVTRA01Y    │
   │ PK: TRAN-ID │                 │ ACCT+TYPE+CAT│
   └─────────────┘                 └──────────────┘
          │ N:1                           │ N:1
   ┌──────┴──────┐                 ┌──────┴──────┐
   │  Tran Type  │                 │ Discl. Group │
   │  CVTRA03Y   │                 │  CVTRA02Y    │
   │ PK: TYPE-CD │                 │ GRP+TYPE+CAT │
   └──────┬──────┘                 └─────────────┘
          │ 1:N
   ┌──────┴──────┐
   │  Tran Cat   │
   │  CVTRA04Y   │
   │ TYPE+CAT-CD │
   └─────────────┘

        ┌─────────────┐
        │ User Security│
        │  CSUSR01Y    │ (standalone)
        │ PK: USR-ID   │
        └──────────────┘
```

---

## 16. VSAM Dataset Catalog

| Dataset Name (HLQ: AWS.M2.CARDDEMO) | Type | Record Size | Key | Copybook | Entity |
|--------------------------------------|------|-------------|-----|----------|--------|
| ACCTDATA.VSAM.KSDS | KSDS | 300 | ACCT-ID (11) | CVACT01Y | Account |
| CARDDATA.VSAM.KSDS | KSDS | 150 | CARD-NUM (16) | CVACT02Y | Card |
| CARDXREF.VSAM.KSDS | KSDS | 50 | CARD+CUST+ACCT (36) | CVACT03Y | Cross-Reference |
| CUSTDATA.VSAM.KSDS | KSDS | 500 | CUST-ID (9) | CVCUS01Y | Customer |
| TRANSACT.VSAM.KSDS | KSDS | 350 | TRAN-ID (16) | CVTRA05Y | Transaction |
| USRSEC.VSAM.KSDS | KSDS | 80 | USR-ID (8) | CSUSR01Y | User Security |
| TCATBALF.VSAM.KSDS | KSDS | 50 | ACCT+TYPE+CAT (17) | CVTRA01Y | Category Balance |
| DISCGRP.VSAM.KSDS | KSDS | 50 | GRP+TYPE+CAT (16) | CVTRA02Y | Disclosure Group |
| TRANTYPE.VSAM.KSDS | KSDS | 60 | TYPE (2) | CVTRA03Y | Transaction Type |
| TRANCATG.VSAM.KSDS | KSDS | 60 | TYPE+CAT (6) | CVTRA04Y | Transaction Category |
| DALYTRAN.PS | Sequential | 350 | -- | CVTRA06Y | Daily Transactions |

---

## 17. PII / Sensitive Data Fields

| Entity | Field | Sensitivity | Modernization Action |
|--------|-------|-------------|---------------------|
| Customer | CUST-SSN | **HIGH** - Social Security Number | Encrypt at rest, mask in UI |
| Customer | CUST-DOB-YYYY-MM-DD | **MEDIUM** - Date of Birth | Access control |
| Customer | CUST-GOVT-ISSUED-ID | **HIGH** - Government ID | Encrypt at rest |
| Customer | CUST-PHONE-NUM-1/2 | **MEDIUM** - Contact info | PII compliance |
| Card | CARD-NUM | **HIGH** - PCI DSS | Tokenize, mask in logs/UI |
| Card | CARD-CVV-CD | **HIGH** - PCI DSS | Never store post-auth |
| User | SEC-USR-PWD | **CRITICAL** - Plaintext password | Hash with bcrypt/scrypt |
