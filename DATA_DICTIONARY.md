# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** COBOL copybooks in `app/cpy/`
> Extracted from PIC clause definitions — translated into business-friendly format.

---

## 1. Account Entity

**Source Copybook:** `CVACT01Y.cpy` (20 lines, ~300-byte record)
**VSAM File:** `ACCTDAT` (KSDS, keyed by Account ID)
**Business Purpose:** Master record for each credit card account — holds financial balances, credit limits, status, and key dates.

| # | COBOL Field | PIC Clause | Data Type | Length | Business Name | Description |
|---|-------------|------------|-----------|--------|---------------|-------------|
| 1 | `ACCT-ID` | `9(11)` | Numeric | 11 | Account ID | Unique account identifier (primary key) |
| 2 | `ACCT-ACTIVE-STATUS` | `X(01)` | Alpha | 1 | Account Status | Active/Inactive flag (`Y`/`N`) |
| 3 | `ACCT-CURR-BAL` | `S9(10)V99` | Signed Decimal | 12.2 | Current Balance | Current outstanding balance (±) |
| 4 | `ACCT-CREDIT-LIMIT` | `S9(10)V99` | Signed Decimal | 12.2 | Credit Limit | Maximum credit limit |
| 5 | `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | Signed Decimal | 12.2 | Cash Advance Limit | Maximum cash advance limit |
| 6 | `ACCT-OPEN-DATE` | `X(10)` | Date String | 10 | Account Open Date | Date account was opened (YYYY-MM-DD) |
| 7 | `ACCT-EXPIRAION-DATE` | `X(10)` | Date String | 10 | Expiration Date | Account expiration date |
| 8 | `ACCT-REISSUE-DATE` | `X(10)` | Date String | 10 | Reissue Date | Last card reissue date |
| 9 | `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | Signed Decimal | 12.2 | Cycle Credits | Credits posted in current billing cycle |
| 10 | `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | Signed Decimal | 12.2 | Cycle Debits | Debits posted in current billing cycle |
| 11 | `ACCT-GROUP-ID` | `X(10)` | Alpha | 10 | Account Group | Account group/portfolio classification |
| 12 | `FILLER` | `X(178)` | — | 178 | Reserved | Future expansion space |

**Java Mapping Target:** `Account.java` (JPA Entity → `ACCOUNT` table)

---

## 2. Card Entity

**Source Copybook:** `CVACT02Y.cpy` (14 lines, ~150-byte record)
**VSAM File:** `CARDDAT` (KSDS, keyed by Card Number)
**Alternate Index:** `CARDAIX` (by Account ID)
**Business Purpose:** Physical credit card record — one account may have multiple cards.

| # | COBOL Field | PIC Clause | Data Type | Length | Business Name | Description |
|---|-------------|------------|-----------|--------|---------------|-------------|
| 1 | `CARD-NUM` | `X(16)` | Alpha | 16 | Card Number | 16-digit credit card number (primary key) |
| 2 | `CARD-ACCT-ID` | `9(11)` | Numeric | 11 | Account ID | Foreign key to Account entity |
| 3 | `CARD-CVV-CD` | `9(03)` | Numeric | 3 | CVV Code | Card verification value |
| 4 | `CARD-EMBOSSED-NAME` | `X(50)` | Alpha | 50 | Cardholder Name | Name embossed on physical card |
| 5 | `CARD-EXPIRAION-DATE` | `X(10)` | Date String | 10 | Expiration Date | Card expiry date (YYYY-MM-DD) |
| 6 | `CARD-ACTIVE-STATUS` | `X(01)` | Alpha | 1 | Card Status | Active/Inactive flag |
| 7 | `FILLER` | `X(59)` | — | 59 | Reserved | Future expansion space |

**Java Mapping Target:** `Card.java` (JPA Entity → `CARD` table, FK to `ACCOUNT`)

---

## 3. Customer Entity

**Source Copybook:** `CVCUS01Y.cpy` (26 lines, ~500-byte record)
**VSAM File:** `CUSTDAT` (KSDS, keyed by Customer ID)
**Business Purpose:** Customer personal and demographic data — linked to accounts via cross-reference.

| # | COBOL Field | PIC Clause | Data Type | Length | Business Name | Description |
|---|-------------|------------|-----------|--------|---------------|-------------|
| 1 | `CUST-ID` | `9(09)` | Numeric | 9 | Customer ID | Unique customer identifier (primary key) |
| 2 | `CUST-FIRST-NAME` | `X(25)` | Alpha | 25 | First Name | Customer first name |
| 3 | `CUST-MIDDLE-NAME` | `X(25)` | Alpha | 25 | Middle Name | Customer middle name |
| 4 | `CUST-LAST-NAME` | `X(25)` | Alpha | 25 | Last Name | Customer last name |
| 5 | `CUST-ADDR-LINE-1` | `X(50)` | Alpha | 50 | Address Line 1 | Street address line 1 |
| 6 | `CUST-ADDR-LINE-2` | `X(50)` | Alpha | 50 | Address Line 2 | Street address line 2 |
| 7 | `CUST-ADDR-LINE-3` | `X(50)` | Alpha | 50 | Address Line 3 | City / locality |
| 8 | `CUST-ADDR-STATE-CD` | `X(02)` | Alpha | 2 | State Code | US state code |
| 9 | `CUST-ADDR-COUNTRY-CD` | `X(03)` | Alpha | 3 | Country Code | ISO country code |
| 10 | `CUST-ADDR-ZIP` | `X(10)` | Alpha | 10 | ZIP Code | Postal/ZIP code |
| 11 | `CUST-PHONE-NUM-1` | `X(15)` | Alpha | 15 | Primary Phone | Primary phone number |
| 12 | `CUST-PHONE-NUM-2` | `X(15)` | Alpha | 15 | Secondary Phone | Secondary/alternate phone |
| 13 | `CUST-SSN` | `9(09)` | Numeric | 9 | SSN | Social Security Number (PII — encrypt at rest) |
| 14 | `CUST-GOVT-ISSUED-ID` | `X(20)` | Alpha | 20 | Government ID | Government-issued identification |
| 15 | `CUST-DOB-YYYY-MM-DD` | `X(10)` | Date String | 10 | Date of Birth | Customer date of birth |
| 16 | `CUST-EFT-ACCOUNT-ID` | `X(10)` | Alpha | 10 | EFT Account | Electronic funds transfer account |
| 17 | `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | Alpha | 1 | Primary Cardholder | Primary (`Y`) or authorized user (`N`) |
| 18 | `CUST-FICO-CREDIT-SCORE` | `9(03)` | Numeric | 3 | FICO Score | Credit score (300–850) |
| 19 | `FILLER` | `X(168)` | — | 168 | Reserved | Future expansion space |

**Java Mapping Target:** `Customer.java` (JPA Entity → `CUSTOMER` table)
**PII Fields:** `CUST-SSN`, `CUST-DOB-YYYY-MM-DD`, `CUST-GOVT-ISSUED-ID` — require encryption and access controls in modernized system.

---

## 4. Card Cross-Reference Entity

**Source Copybook:** `CVACT03Y.cpy` (11 lines, ~50-byte record)
**VSAM File:** `CCXREF` / `CXACAIX` (alternate index by Account ID)
**Business Purpose:** Maps cards to customers and accounts — the central relationship join.

| # | COBOL Field | PIC Clause | Data Type | Length | Business Name | Description |
|---|-------------|------------|-----------|--------|---------------|-------------|
| 1 | `XREF-CARD-NUM` | `X(16)` | Alpha | 16 | Card Number | Foreign key to Card entity |
| 2 | `XREF-CUST-ID` | `9(09)` | Numeric | 9 | Customer ID | Foreign key to Customer entity |
| 3 | `XREF-ACCT-ID` | `9(11)` | Numeric | 11 | Account ID | Foreign key to Account entity |
| 4 | `FILLER` | `X(14)` | — | 14 | Reserved | Future expansion space |

**Java Mapping Target:** Replaced by JPA `@ManyToOne` relationships between `Card`, `Customer`, and `Account` entities.

---

## 5. Transaction Entity (Online)

**Source Copybook:** `CVTRA05Y.cpy` (21 lines, ~350-byte record)
**VSAM File:** `TRANSACT` (KSDS, keyed by Transaction ID)
**Business Purpose:** Individual credit card transaction — purchases, payments, cash advances.

| # | COBOL Field | PIC Clause | Data Type | Length | Business Name | Description |
|---|-------------|------------|-----------|--------|---------------|-------------|
| 1 | `TRAN-ID` | `X(16)` | Alpha | 16 | Transaction ID | Unique transaction identifier (primary key) |
| 2 | `TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction Type | Type code (e.g., `SA`=Sale, `CR`=Credit) |
| 3 | `TRAN-CAT-CD` | `9(04)` | Numeric | 4 | Category Code | MCC-style merchant category code |
| 4 | `TRAN-SOURCE` | `X(10)` | Alpha | 10 | Transaction Source | Origin channel (POS, ATM, Online, etc.) |
| 5 | `TRAN-DESC` | `X(100)` | Alpha | 100 | Description | Free-text transaction description |
| 6 | `TRAN-AMT` | `S9(09)V99` | Signed Decimal | 11.2 | Amount | Transaction amount (± for debits/credits) |
| 7 | `TRAN-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant ID | Merchant identifier |
| 8 | `TRAN-MERCHANT-NAME` | `X(50)` | Alpha | 50 | Merchant Name | Merchant business name |
| 9 | `TRAN-MERCHANT-CITY` | `X(50)` | Alpha | 50 | Merchant City | Merchant city location |
| 10 | `TRAN-MERCHANT-ZIP` | `X(10)` | Alpha | 10 | Merchant ZIP | Merchant postal code |
| 11 | `TRAN-CARD-NUM` | `X(16)` | Alpha | 16 | Card Number | Card used (FK to Card entity) |
| 12 | `TRAN-ORIG-TS` | `X(26)` | Timestamp | 26 | Origination Timestamp | When transaction was initiated |
| 13 | `TRAN-PROC-TS` | `X(26)` | Timestamp | 26 | Processing Timestamp | When transaction was posted |
| 14 | `FILLER` | `X(20)` | — | 20 | Reserved | Future expansion space |

**Java Mapping Target:** `Transaction.java` (JPA Entity → `TRANSACTION` table, FK to `CARD`)

---

## 6. Daily Transaction Entity

**Source Copybook:** `CVTRA06Y.cpy` (21 lines, mirrors CVTRA05Y)
**VSAM File:** `DALYTRAN` (batch input)
**Business Purpose:** Daily batch transactions pending posting — same structure as online transactions with `DALYTRAN-` prefix.

| # | COBOL Field | PIC Clause | Business Name | Notes |
|---|-------------|------------|---------------|-------|
| 1 | `DALYTRAN-ID` | `X(16)` | Transaction ID | Same layout as CVTRA05Y |
| 2 | `DALYTRAN-TYPE-CD` | `X(02)` | Transaction Type | |
| 3 | `DALYTRAN-CAT-CD` | `9(04)` | Category Code | |
| 4 | `DALYTRAN-SOURCE` | `X(10)` | Transaction Source | |
| 5 | `DALYTRAN-DESC` | `X(100)` | Description | |
| 6 | `DALYTRAN-AMT` | `S9(09)V99` | Amount | |
| 7 | `DALYTRAN-MERCHANT-ID` | `9(09)` | Merchant ID | |
| 8 | `DALYTRAN-MERCHANT-NAME` | `X(50)` | Merchant Name | |
| 9 | `DALYTRAN-MERCHANT-CITY` | `X(50)` | Merchant City | |
| 10 | `DALYTRAN-MERCHANT-ZIP` | `X(10)` | Merchant ZIP | |
| 11 | `DALYTRAN-CARD-NUM` | `X(16)` | Card Number | |
| 12 | `DALYTRAN-ORIG-TS` | `X(26)` | Origination Timestamp | |
| 13 | `DALYTRAN-PROC-TS` | `X(26)` | Processing Timestamp | |
| 14 | `FILLER` | `X(20)` | Reserved | |

**Java Mapping Target:** Same `Transaction.java` entity with a `status` enum (PENDING/POSTED).

---

## 7. Transaction Reference Data

### 7.1 Transaction Type

**Source Copybook:** `CVTRA03Y.cpy` (10 lines)
**VSAM File:** `TRANTYPE`

| # | COBOL Field | PIC Clause | Business Name | Description |
|---|-------------|------------|---------------|-------------|
| 1 | `TRAN-TYPE` | `X(02)` | Type Code | 2-char code (e.g., SA, CR, PR, BA) |
| 2 | `TRAN-TYPE-DESC` | `X(50)` | Type Description | Human-readable name |

### 7.2 Transaction Category Type

**Source Copybook:** `CVTRA04Y.cpy` (12 lines)
**VSAM File:** `TRANCATG`

| # | COBOL Field | PIC Clause | Business Name | Description |
|---|-------------|------------|---------------|-------------|
| 1 | `TRAN-TYPE-CD` | `X(02)` | Type Code | FK to Transaction Type |
| 2 | `TRAN-CAT-CD` | `9(04)` | Category Code | MCC-style category |
| 3 | `TRAN-CAT-TYPE-DESC` | `X(50)` | Category Description | Human-readable category name |

### 7.3 Transaction Category Balance

**Source Copybook:** `CVTRA01Y.cpy` (13 lines)
**VSAM File:** `TCATBALF`

| # | COBOL Field | PIC Clause | Business Name | Description |
|---|-------------|------------|---------------|-------------|
| 1 | `TRANCAT-ACCT-ID` | `9(11)` | Account ID | FK to Account |
| 2 | `TRANCAT-TYPE-CD` | `X(02)` | Type Code | Transaction type |
| 3 | `TRANCAT-CD` | `9(04)` | Category Code | Transaction category |
| 4 | `TRAN-CAT-BAL` | `S9(09)V99` | Category Balance | Running balance per category per account |

### 7.4 Discount/Interest Rate Group

**Source Copybook:** `CVTRA02Y.cpy` (13 lines)
**VSAM File:** `DISCGRP`

| # | COBOL Field | PIC Clause | Business Name | Description |
|---|-------------|------------|---------------|-------------|
| 1 | `DIS-ACCT-GROUP-ID` | `X(10)` | Account Group | Portfolio/group identifier |
| 2 | `DIS-TRAN-TYPE-CD` | `X(02)` | Type Code | Transaction type |
| 3 | `DIS-TRAN-CAT-CD` | `9(04)` | Category Code | Transaction category |
| 4 | `DIS-INT-RATE` | `S9(04)V99` | Interest Rate | Rate applied to this group/category combo |

---

## 8. User Security Entity

**Source Copybook:** `CSUSR01Y.cpy`
**VSAM File:** `USRSEC` (KSDS, keyed by User ID)
**Business Purpose:** Application-level user authentication and authorization.

| # | COBOL Field | PIC Clause | Business Name | Description |
|---|-------------|------------|---------------|-------------|
| 1 | `SEC-USR-ID` | `X(08)` | User ID | Login identifier (primary key) |
| 2 | `SEC-USR-FNAME` | `X(20)` | First Name | User first name |
| 3 | `SEC-USR-LNAME` | `X(20)` | Last Name | User last name |
| 4 | `SEC-USR-PWD` | `X(08)` | Password | Plaintext password (modernize to hashed!) |
| 5 | `SEC-USR-TYPE` | `X(01)` | User Type | `A` = Admin, `U` = Regular user |
| 6 | `FILLER` | `X(23)` | Reserved | Future expansion space |

**Java Mapping Target:** `User.java` with Spring Security integration. **Critical:** Password must be BCrypt-hashed in modernized system.

---

## 9. Export Composite Record

**Source Copybook:** `CVEXPORT.cpy` (103 lines)
**Sequential File:** `EXPORTFL`
**Business Purpose:** Composite record combining customer + account + card data for batch export/migration.

| # | COBOL Field | PIC Clause | Business Name | Description |
|---|-------------|------------|---------------|-------------|
| 1 | `EXPORT-REC-TYPE` | `X(1)` | Record Type | C=Customer, A=Account, X=Xref |
| 2 | `EXPORT-TIMESTAMP` | `X(26)` | Export Timestamp | When record was exported |
| 3 | `EXPORT-SEQUENCE-NUM` | `9(9) COMP` | Sequence Number | Export ordering sequence |
| 4 | `EXPORT-BRANCH-ID` | `X(4)` | Branch ID | Originating branch |
| 5 | `EXPORT-REGION-CODE` | `X(5)` | Region Code | Geographic region |
| 6 | `EXPORT-RECORD-DATA` | `X(460)` | Record Payload | Redefined per record type (see below) |

**Customer sub-record** (when `EXPORT-REC-TYPE` = 'C'):

| Field | PIC | Business Name |
|-------|-----|---------------|
| `EXP-CUST-ID` | `9(09) COMP` | Customer ID |
| `EXP-CUST-FIRST-NAME` | `X(25)` | First Name |
| `EXP-CUST-MIDDLE-NAME` | `X(25)` | Middle Name |
| `EXP-CUST-LAST-NAME` | `X(25)` | Last Name |
| `EXP-CUST-ADDR-LINE` (x3) | `X(50)` | Address Lines |
| `EXP-CUST-ADDR-STATE-CD` | `X(02)` | State |
| `EXP-CUST-ADDR-COUNTRY-CD` | `X(03)` | Country |
| `EXP-CUST-ADDR-ZIP` | `X(10)` | ZIP Code |
| `EXP-CUST-PHONE-NUM` (x2) | `X(15)` | Phone Numbers |
| `EXP-CUST-SSN` | `9(09)` | SSN |
| `EXP-CUST-GOVT-ISSUED-ID` | `X(20)` | Government ID |
| `EXP-CUST-DOB-YYYY-MM-DD` | `X(10)` | Date of Birth |
| `EXP-CUST-EFT-ACCOUNT-ID` | `X(10)` | EFT Account |
| `EXP-CUST-PRI-CARD-HOLDER-IND` | `X(01)` | Primary Holder |
| `EXP-CUST-FICO-CREDIT-SCORE` | `9(03) COMP-3` | FICO Score |

**Account sub-record** (when `EXPORT-REC-TYPE` = 'A'):

| Field | PIC | Business Name |
|-------|-----|---------------|
| `EXP-ACCT-ID` | `9(11)` | Account ID |
| `EXP-ACCT-ACTIVE-STATUS` | `X(01)` | Status |
| `EXP-ACCT-CURR-BAL` | `S9(10)V99 COMP-3` | Current Balance |
| `EXP-ACCT-CREDIT-LIMIT` | `S9(10)V99` | Credit Limit |
| `EXP-ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99 COMP-3` | Cash Advance Limit |

---

## 10. Statement Transaction Record

**Source Copybook:** `COSTM01.CPY` (38 lines)
**Used By:** `CBSTM03A` / `CBSTM03B` (Statement Generation)
**Business Purpose:** Transaction record layout used during statement rendering.

| # | COBOL Field | PIC Clause | Business Name | Description |
|---|-------------|------------|---------------|-------------|
| 1 | `TRNX-CARD-NUM` | `X(16)` | Card Number | Card associated with transaction |
| 2 | `TRNX-ID` | `X(16)` | Transaction ID | Transaction identifier |
| 3 | `TRNX-TYPE-CD` | `X(02)` | Type Code | Transaction type |
| 4 | `TRNX-CAT-CD` | `9(04)` | Category Code | Transaction category |
| 5 | `TRNX-SOURCE` | `X(10)` | Source | Transaction channel |
| 6 | `TRNX-DESC` | `X(100)` | Description | Transaction description |
| 7 | `TRNX-AMT` | `S9(09)V99` | Amount | Transaction amount |
| 8 | `TRNX-MERCHANT-ID` | `9(09)` | Merchant ID | Merchant identifier |
| 9 | `TRNX-MERCHANT-NAME` | `X(50)` | Merchant Name | Business name |
| 10 | `TRNX-MERCHANT-CITY` | `X(50)` | Merchant City | City location |
| 11 | `TRNX-MERCHANT-ZIP` | `X(10)` | Merchant ZIP | Postal code |
| 12 | `TRNX-ORIG-TS` | `X(26)` | Origination TS | Origination timestamp |
| 13 | `TRNX-PROC-TS` | `X(26)` | Processing TS | Processing timestamp |

---

## 11. Entity-Relationship Summary

```
┌──────────┐     1:N     ┌──────────┐     1:N     ┌──────────────┐
│ CUSTOMER │────────────▶│  ACCOUNT │────────────▶│     CARD     │
│ CVCUS01Y │             │ CVACT01Y │             │  CVACT02Y    │
│ CUSTDAT  │             │ ACCTDAT  │             │  CARDDAT     │
└──────────┘             └──────────┘             └──────────────┘
      │                       │                         │
      │                       │                         │
      └───────┐   ┌──────────┘                         │
              ▼   ▼                                     │
         ┌──────────────┐                               │
         │  CROSS-REF   │                               │
         │  CVACT03Y    │◀──────────────────────────────┘
         │  CCXREF      │
         └──────────────┘
                                    ┌──────────────┐
              ┌─────────────────────│ TRANSACTION  │
              │                     │  CVTRA05Y    │
              │                     │  TRANSACT    │
              │                     └──────────────┘
              │                            │
              ▼                            ▼
     ┌──────────────┐            ┌──────────────┐
     │  TRAN TYPE   │            │  TRAN CAT    │
     │  CVTRA03Y    │            │  CVTRA04Y    │
     │  TRANTYPE    │            │  TRANCATG    │
     └──────────────┘            └──────────────┘
              │
              ▼
     ┌──────────────┐            ┌──────────────┐
     │ CAT BALANCE  │            │  DISC GROUP  │
     │  CVTRA01Y    │            │  CVTRA02Y    │
     │  TCATBALF    │            │  DISCGRP     │
     └──────────────┘            └──────────────┘

     ┌──────────────┐
     │ USER SECURITY│  (Standalone — application auth)
     │  CSUSR01Y    │
     │  USRSEC      │
     └──────────────┘
```

---

## 12. VSAM File Inventory

| VSAM File | Dataset Name (DSN) | Type | Key Field | Record Layout | Approx Record Size |
|-----------|--------------------|------|-----------|---------------|--------------------|
| ACCTDAT | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | KSDS | ACCT-ID (11) | CVACT01Y | ~300 bytes |
| CARDDAT | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` | KSDS | CARD-NUM (16) | CVACT02Y | ~150 bytes |
| CUSTDAT | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` | KSDS | CUST-ID (9) | CVCUS01Y | ~500 bytes |
| CCXREF | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | KSDS | XREF-CARD-NUM (16) | CVACT03Y | ~50 bytes |
| CXACAIX | (Alternate index on CCXREF) | AIX | XREF-ACCT-ID (11) | CVACT03Y | — |
| CARDAIX | (Alternate index on CARDDAT) | AIX | CARD-ACCT-ID (11) | CVACT02Y | — |
| TRANSACT | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | KSDS | TRAN-ID (16) | CVTRA05Y | ~350 bytes |
| USRSEC | `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` | KSDS | SEC-USR-ID (8) | CSUSR01Y | ~80 bytes |
| TRANTYPE | `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` | KSDS | TRAN-TYPE (2) | CVTRA03Y | ~60 bytes |
| TRANCATG | `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` | KSDS | TYPE+CAT (6) | CVTRA04Y | ~60 bytes |
| TCATBALF | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` | KSDS | ACCT+TYPE+CAT | CVTRA01Y | ~35 bytes |
| DISCGRP | `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` | KSDS | GROUP+TYPE+CAT | CVTRA02Y | ~50 bytes |

---

## 13. PIC Clause Type Reference

For modernization teams unfamiliar with COBOL:

| COBOL PIC | Java Equivalent | SQL Type | Notes |
|-----------|----------------|----------|-------|
| `X(n)` | `String` (length n) | `VARCHAR(n)` | Alphanumeric, space-padded |
| `9(n)` | `long` or `BigInteger` | `NUMERIC(n)` | Unsigned integer |
| `S9(n)` | `long` | `NUMERIC(n)` | Signed integer |
| `S9(n)V99` | `BigDecimal` | `DECIMAL(n+2, 2)` | Signed with 2 decimal places |
| `S9(n)V99 COMP-3` | `BigDecimal` | `DECIMAL(n+2, 2)` | Packed decimal (BCD encoding) |
| `9(n) COMP` | `int` / `long` | `INTEGER` | Binary integer |

---

## 14. Data Quality & Modernization Notes

1. **PII Fields** requiring encryption: `CUST-SSN`, `CUST-DOB-YYYY-MM-DD`, `CUST-GOVT-ISSUED-ID`, `CARD-NUM`, `CARD-CVV-CD`
2. **Password Storage**: `SEC-USR-PWD` is stored in plaintext — must migrate to BCrypt/Argon2 hashing
3. **Date Formats**: Dates stored as `X(10)` strings (YYYY-MM-DD) — map to `java.time.LocalDate`
4. **Timestamps**: `X(26)` format — map to `java.time.Instant` or `java.time.LocalDateTime`
5. **FILLER fields**: Present in all records for expansion — can be dropped in relational schema
6. **REDEFINES**: `CVEXPORT` uses REDEFINES extensively for polymorphic record types — map to inheritance or discriminator column
7. **COMP-3 (Packed Decimal)**: Used in export records — requires byte-level conversion during data migration
