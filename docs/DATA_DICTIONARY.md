# DATA DICTIONARY -- CardDemo Business Entities

> **Generated:** 2026-03-25  
> **Source:** Copybooks in `app/cpy/` and optional module copybooks  
> **Approach:** Every `PIC` clause mapped to business-friendly field definitions

---

## 1. Account Entity

**Copybook:** `CVACT01Y.cpy` | **Record:** `ACCOUNT-RECORD` | **Length:** 300 bytes  
**VSAM File:** `ACCTDAT` (KSDS, key = `ACCT-ID`)  
**Java Target:** `Account.java` (JPA entity / DTO)

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description | Constraints |
|---|-------------|------------|-----------|------|---------------|-------------|-------------|
| 1 | ACCT-ID | `9(11)` | `long` | 11 digits | Account Number | Unique account identifier | PK, non-zero |
| 2 | ACCT-ACTIVE-STATUS | `X(01)` | `String` | 1 char | Active Status | Account active flag | `Y`/`N` |
| 3 | ACCT-CURR-BAL | `S9(10)V99` | `BigDecimal` | 12,2 signed | Current Balance | Current outstanding balance | Signed, 2 decimals |
| 4 | ACCT-CREDIT-LIMIT | `S9(10)V99` | `BigDecimal` | 12,2 signed | Credit Limit | Maximum credit allowed | Signed, 2 decimals |
| 5 | ACCT-CASH-CREDIT-LIMIT | `S9(10)V99` | `BigDecimal` | 12,2 signed | Cash Advance Limit | Maximum cash advance | Signed, 2 decimals |
| 6 | ACCT-OPEN-DATE | `X(10)` | `LocalDate` | 10 chars | Open Date | Account opening date | Format: YYYY-MM-DD |
| 7 | ACCT-EXPIRAION-DATE | `X(10)` | `LocalDate` | 10 chars | Expiration Date | Account expiration date | Format: YYYY-MM-DD |
| 8 | ACCT-REISSUE-DATE | `X(10)` | `LocalDate` | 10 chars | Reissue Date | Last card reissue date | Format: YYYY-MM-DD |
| 9 | ACCT-CURR-CYC-CREDIT | `S9(10)V99` | `BigDecimal` | 12,2 signed | Cycle Credits | Current cycle credit total | Signed, 2 decimals |
| 10 | ACCT-CURR-CYC-DEBIT | `S9(10)V99` | `BigDecimal` | 12,2 signed | Cycle Debits | Current cycle debit total | Signed, 2 decimals |
| 11 | ACCT-ADDR-ZIP | `X(10)` | `String` | 10 chars | ZIP Code | Account holder ZIP/postal code | -- |
| 12 | ACCT-GROUP-ID | `X(10)` | `String` | 10 chars | Account Group | Discount/interest rate group | FK to DISCGRP |
| 13 | FILLER | `X(178)` | -- | 178 bytes | Reserved | Padding to 300 bytes | -- |

---

## 2. Credit Card Entity

**Copybook:** `CVACT02Y.cpy` | **Record:** `CARD-RECORD` | **Length:** 150 bytes  
**VSAM File:** `CARDDAT` (KSDS, key = `CARD-NUM`), AIX on `CARD-ACCT-ID` via `CARDAIX`  
**Java Target:** `CreditCard.java`

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description | Constraints |
|---|-------------|------------|-----------|------|---------------|-------------|-------------|
| 1 | CARD-NUM | `X(16)` | `String` | 16 chars | Card Number | Credit card number | PK, 16-digit |
| 2 | CARD-ACCT-ID | `9(11)` | `long` | 11 digits | Account ID | Owning account | FK to Account |
| 3 | CARD-CVV-CD | `9(03)` | `int` | 3 digits | CVV Code | Card verification value | 3 digits |
| 4 | CARD-EMBOSSED-NAME | `X(50)` | `String` | 50 chars | Embossed Name | Name printed on card | -- |
| 5 | CARD-EXPIRAION-DATE | `X(10)` | `LocalDate` | 10 chars | Expiration Date | Card expiry date | YYYY-MM-DD |
| 6 | CARD-ACTIVE-STATUS | `X(01)` | `String` | 1 char | Active Status | Card status flag | `Y`/`N` |
| 7 | FILLER | `X(59)` | -- | 59 bytes | Reserved | Padding to 150 bytes | -- |

---

## 3. Customer Entity

**Copybook:** `CVCUS01Y.cpy` | **Record:** `CUSTOMER-RECORD` | **Length:** 500 bytes  
**VSAM File:** `CUSTDAT` (KSDS, key = `CUST-ID`)  
**Java Target:** `Customer.java`

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description | Constraints |
|---|-------------|------------|-----------|------|---------------|-------------|-------------|
| 1 | CUST-ID | `9(09)` | `int` | 9 digits | Customer ID | Unique customer identifier | PK |
| 2 | CUST-FIRST-NAME | `X(25)` | `String` | 25 chars | First Name | Customer first name | -- |
| 3 | CUST-MIDDLE-NAME | `X(25)` | `String` | 25 chars | Middle Name | Customer middle name | Optional |
| 4 | CUST-LAST-NAME | `X(25)` | `String` | 25 chars | Last Name | Customer last name | -- |
| 5 | CUST-ADDR-LINE-1 | `X(50)` | `String` | 50 chars | Address Line 1 | Street address | -- |
| 6 | CUST-ADDR-LINE-2 | `X(50)` | `String` | 50 chars | Address Line 2 | Suite/apartment | Optional |
| 7 | CUST-ADDR-LINE-3 | `X(50)` | `String` | 50 chars | Address Line 3 | City | -- |
| 8 | CUST-ADDR-STATE-CD | `X(02)` | `String` | 2 chars | State Code | US state abbreviation | Validated via CSLKPCDY |
| 9 | CUST-ADDR-COUNTRY-CD | `X(03)` | `String` | 3 chars | Country Code | ISO country code | Validated via CSLKPCDY |
| 10 | CUST-ADDR-ZIP | `X(10)` | `String` | 10 chars | ZIP Code | Postal code | -- |
| 11 | CUST-PHONE-NUM-1 | `X(15)` | `String` | 15 chars | Phone Number 1 | Primary phone | Format: (NNN)NNN-NNNN |
| 12 | CUST-PHONE-NUM-2 | `X(15)` | `String` | 15 chars | Phone Number 2 | Secondary phone | Optional |
| 13 | CUST-SSN | `9(09)` | `String` | 9 digits | SSN | Social Security Number | PII, validated |
| 14 | CUST-GOVT-ISSUED-ID | `X(20)` | `String` | 20 chars | Government ID | Government-issued ID | -- |
| 15 | CUST-DOB-YYYY-MM-DD | `X(10)` | `LocalDate` | 10 chars | Date of Birth | Customer DOB | YYYY-MM-DD |
| 16 | CUST-EFT-ACCOUNT-ID | `X(10)` | `String` | 10 chars | EFT Account | Electronic funds transfer acct | For bill pay |
| 17 | CUST-PRI-CARD-HOLDER-IND | `X(01)` | `String` | 1 char | Primary Holder | Primary card holder indicator | `Y`/`N` |
| 18 | CUST-FICO-CREDIT-SCORE | `9(03)` | `int` | 3 digits | FICO Score | Credit score | 300-850 |
| 19 | FILLER | `X(168)` | -- | 168 bytes | Reserved | Padding to 500 bytes | -- |

---

## 4. Transaction Entity

**Copybook:** `CVTRA05Y.cpy` | **Record:** `TRAN-RECORD` | **Length:** 350 bytes  
**VSAM File:** `TRANSACT` (KSDS, key = `TRAN-ID`)  
**Java Target:** `Transaction.java`

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description | Constraints |
|---|-------------|------------|-----------|------|---------------|-------------|-------------|
| 1 | TRAN-ID | `X(16)` | `String` | 16 chars | Transaction ID | Unique transaction identifier | PK |
| 2 | TRAN-TYPE-CD | `X(02)` | `String` | 2 chars | Transaction Type | Type code (e.g., SA=Sale) | FK to TRANTYPE |
| 3 | TRAN-CAT-CD | `9(04)` | `int` | 4 digits | Category Code | Transaction category | FK to TRANCATG |
| 4 | TRAN-SOURCE | `X(10)` | `String` | 10 chars | Source | Transaction source channel | -- |
| 5 | TRAN-DESC | `X(100)` | `String` | 100 chars | Description | Transaction description | -- |
| 6 | TRAN-AMT | `S9(09)V99` | `BigDecimal` | 11,2 signed | Amount | Transaction amount | Signed |
| 7 | TRAN-MERCHANT-ID | `9(09)` | `int` | 9 digits | Merchant ID | Merchant identifier | -- |
| 8 | TRAN-MERCHANT-NAME | `X(50)` | `String` | 50 chars | Merchant Name | Merchant business name | -- |
| 9 | TRAN-MERCHANT-CITY | `X(50)` | `String` | 50 chars | Merchant City | Merchant location city | -- |
| 10 | TRAN-MERCHANT-ZIP | `X(10)` | `String` | 10 chars | Merchant ZIP | Merchant postal code | -- |
| 11 | TRAN-CARD-NUM | `X(16)` | `String` | 16 chars | Card Number | Card used for transaction | FK to CreditCard |
| 12 | TRAN-ORIG-TS | `X(26)` | `Timestamp` | 26 chars | Origination Timestamp | When transaction originated | ISO timestamp |
| 13 | TRAN-PROC-TS | `X(26)` | `Timestamp` | 26 chars | Processing Timestamp | When transaction was posted | ISO timestamp |
| 14 | FILLER | `X(20)` | -- | 20 bytes | Reserved | Padding to 350 bytes | -- |

---

## 5. Daily Transaction Feed Entity

**Copybook:** `CVTRA06Y.cpy` | **Record:** `DALYTRAN-RECORD` | **Length:** 350 bytes  
**File:** `DALYTRAN` (sequential input feed)  
**Java Target:** `DailyTransactionFeed.java` (batch input DTO)

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | DALYTRAN-ID | `X(16)` | `String` | 16 chars | Transaction ID | Feed transaction identifier |
| 2 | DALYTRAN-TYPE-CD | `X(02)` | `String` | 2 chars | Type Code | Transaction type |
| 3 | DALYTRAN-CAT-CD | `9(04)` | `int` | 4 digits | Category Code | Transaction category |
| 4 | DALYTRAN-SOURCE | `X(10)` | `String` | 10 chars | Source | Origination channel |
| 5 | DALYTRAN-DESC | `X(100)` | `String` | 100 chars | Description | Transaction description |
| 6 | DALYTRAN-AMT | `S9(09)V99` | `BigDecimal` | 11,2 signed | Amount | Transaction amount |
| 7 | DALYTRAN-MERCHANT-ID | `9(09)` | `int` | 9 digits | Merchant ID | Merchant identifier |
| 8 | DALYTRAN-MERCHANT-NAME | `X(50)` | `String` | 50 chars | Merchant Name | Merchant business name |
| 9 | DALYTRAN-MERCHANT-CITY | `X(50)` | `String` | 50 chars | Merchant City | Merchant location |
| 10 | DALYTRAN-MERCHANT-ZIP | `X(10)` | `String` | 10 chars | Merchant ZIP | Merchant postal code |
| 11 | DALYTRAN-CARD-NUM | `X(16)` | `String` | 16 chars | Card Number | Card used |
| 12 | DALYTRAN-ORIG-TS | `X(26)` | `Timestamp` | 26 chars | Origination Time | Transaction timestamp |
| 13 | DALYTRAN-PROC-TS | `X(26)` | `Timestamp` | 26 chars | Processing Time | Processing timestamp |
| 14 | FILLER | `X(20)` | -- | 20 bytes | Reserved | Padding |

---

## 6. Card Cross-Reference Entity

**Copybook:** `CVACT03Y.cpy` | **Record:** `CARD-XREF-RECORD` | **Length:** 50 bytes  
**VSAM File:** `CCXREF` / `CXACAIX` (KSDS, key = `XREF-CARD-NUM`, AIX on `XREF-ACCT-ID`)  
**Java Target:** `CardCrossReference.java` (join table)

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | XREF-CARD-NUM | `X(16)` | `String` | 16 chars | Card Number | FK to CreditCard |
| 2 | XREF-CUST-ID | `9(09)` | `int` | 9 digits | Customer ID | FK to Customer |
| 3 | XREF-ACCT-ID | `9(11)` | `long` | 11 digits | Account ID | FK to Account |
| 4 | FILLER | `X(14)` | -- | 14 bytes | Reserved | Padding to 50 bytes |

> **Business Purpose:** This is the central join entity linking Cards ↔ Customers ↔ Accounts. Nearly every online program uses this to resolve relationships.

---

## 7. User Security Entity

**Copybook:** `CSUSR01Y.cpy` | **Record:** `SEC-USER-DATA` | **Length:** 80 bytes  
**VSAM File:** `USRSEC` (KSDS, key = `SEC-USR-ID`)  
**Java Target:** `UserSecurity.java`

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description | Constraints |
|---|-------------|------------|-----------|------|---------------|-------------|-------------|
| 1 | SEC-USR-ID | `X(08)` | `String` | 8 chars | User ID | Login user identifier | PK |
| 2 | SEC-USR-FNAME | `X(20)` | `String` | 20 chars | First Name | User first name | -- |
| 3 | SEC-USR-LNAME | `X(20)` | `String` | 20 chars | Last Name | User last name | -- |
| 4 | SEC-USR-PWD | `X(08)` | `String` | 8 chars | Password | Login password | Plain text (legacy) |
| 5 | SEC-USR-TYPE | `X(01)` | `String` | 1 char | User Type | Role: Admin or Regular | `A` / `U` |
| 6 | SEC-USR-FILLER | `X(23)` | -- | 23 bytes | Reserved | Padding to 80 bytes | -- |

---

## 8. Transaction Category Balance Entity

**Copybook:** `CVTRA01Y.cpy` | **Record:** `TRANCAT-RECORD` | **Length:** ~50 bytes  
**VSAM File:** `TCATBALF` (KSDS)  
**Java Target:** `TransactionCategoryBalance.java`

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | TRANCAT-ACCT-ID | `9(11)` | `long` | 11 digits | Account ID | FK to Account |
| 2 | TRANCAT-TYPE-CD | `X(02)` | `String` | 2 chars | Type Code | Transaction type |
| 3 | TRANCAT-CD | `9(04)` | `int` | 4 digits | Category Code | Category code |
| 4 | TRAN-CAT-BAL | `S9(09)V99` | `BigDecimal` | 11,2 | Category Balance | Running balance by category |
| 5 | FILLER | `X(22)` | -- | 22 bytes | Reserved | Padding |

---

## 9. Discount / Interest Rate Group Entity

**Copybook:** `CVTRA02Y.cpy` | **Record:** `DISCGRP-RECORD` | **Length:** ~50 bytes  
**VSAM File:** `DISCGRP` (KSDS)  
**Java Target:** `DiscountGroup.java`

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | DIS-ACCT-GROUP-ID | `X(10)` | `String` | 10 chars | Group ID | Discount group identifier |
| 2 | DIS-TRAN-TYPE-CD | `X(02)` | `String` | 2 chars | Type Code | Transaction type |
| 3 | DIS-TRAN-CAT-CD | `9(04)` | `int` | 4 digits | Category Code | Transaction category |
| 4 | DIS-INT-RATE | `S9(04)V99` | `BigDecimal` | 6,2 | Interest Rate | Rate for this group/type/cat |
| 5 | FILLER | `X(28)` | -- | 28 bytes | Reserved | Padding |

---

## 10. Transaction Type Lookup Entity

**Copybook:** `CVTRA03Y.cpy` | **Record:** `TRANTYPE-RECORD` | **Length:** ~60 bytes  
**VSAM File:** `TRANTYPE` (KSDS, key = `TRAN-TYPE`)  
**Java Target:** `TransactionType.java`

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | TRAN-TYPE | `X(02)` | `String` | 2 chars | Type Code | Transaction type code (PK) |
| 2 | TRAN-TYPE-DESC | `X(50)` | `String` | 50 chars | Type Description | Human-readable description |
| 3 | FILLER | `X(08)` | -- | 8 bytes | Reserved | Padding |

---

## 11. Transaction Category Lookup Entity

**Copybook:** `CVTRA04Y.cpy` | **Record:** `TRANCATG-RECORD` | **Length:** ~60 bytes  
**VSAM File:** `TRANCATG` (KSDS, compound key)  
**Java Target:** `TransactionCategory.java`

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | TRAN-TYPE-CD | `X(02)` | `String` | 2 chars | Type Code | FK to TransactionType |
| 2 | TRAN-CAT-CD | `9(04)` | `int` | 4 digits | Category Code | Category code |
| 3 | TRAN-CAT-TYPE-DESC | `X(50)` | `String` | 50 chars | Description | Category description |
| 4 | FILLER | `X(04)` | -- | 4 bytes | Reserved | Padding |

---

## 12. Data Export/Import Record

**Copybook:** `CVEXPORT.cpy` | **Record:** `EXPORT-RECORD` | **Length:** ~500 bytes  
**File:** `EXPFILE` (sequential)  
**Java Target:** `ExportRecord.java` (batch I/O DTO)

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | EXPORT-REC-TYPE | `X(1)` | `String` | 1 char | Record Type | C=Customer, A=Account, X=Xref, T=Transaction |
| 2 | EXPORT-TIMESTAMP | `X(26)` | `Timestamp` | 26 chars | Export Timestamp | When record was exported |
| 3 | EXPORT-SEQUENCE-NUM | `9(9) COMP` | `int` | 4 bytes | Sequence Number | Sequential record number |
| 4 | EXPORT-BRANCH-ID | `X(4)` | `String` | 4 chars | Branch ID | Originating branch |
| 5 | EXPORT-REGION-CODE | `X(5)` | `String` | 5 chars | Region Code | Geographic region |
| 6 | EXPORT-RECORD-DATA | `X(460)` | varies | 460 bytes | Record Payload | Type-dependent data (Customer/Account/Xref/Transaction) |

### Export Payload: Customer Sub-Record

| Field | PIC | Java Type | Business Name |
|-------|-----|-----------|---------------|
| EXP-CUST-ID | `9(09) COMP` | `int` | Customer ID |
| EXP-CUST-FIRST-NAME | `X(25)` | `String` | First Name |
| EXP-CUST-MIDDLE-NAME | `X(25)` | `String` | Middle Name |
| EXP-CUST-LAST-NAME | `X(25)` | `String` | Last Name |
| EXP-CUST-ADDR-LINE (x3) | `X(50)` | `String` | Address Lines |
| EXP-CUST-ADDR-STATE-CD | `X(02)` | `String` | State |
| EXP-CUST-ADDR-COUNTRY-CD | `X(03)` | `String` | Country |
| EXP-CUST-ADDR-ZIP | `X(10)` | `String` | ZIP |
| EXP-CUST-PHONE-NUM (x2) | `X(15)` | `String` | Phone Numbers |
| EXP-CUST-SSN | `9(09)` | `String` | SSN |
| EXP-CUST-GOVT-ISSUED-ID | `X(20)` | `String` | Gov ID |
| EXP-CUST-DOB-YYYY-MM-DD | `X(10)` | `LocalDate` | DOB |
| EXP-CUST-EFT-ACCOUNT-ID | `X(10)` | `String` | EFT Account |
| EXP-CUST-PRI-CARD-HOLDER-IND | `X(01)` | `String` | Primary Holder |
| EXP-CUST-FICO-CREDIT-SCORE | `9(03) COMP-3` | `int` | FICO Score |

### Export Payload: Account Sub-Record

| Field | PIC | Java Type | Business Name |
|-------|-----|-----------|---------------|
| EXP-ACCT-ID | `9(11)` | `long` | Account ID |
| EXP-ACCT-ACTIVE-STATUS | `X(01)` | `String` | Active Status |
| EXP-ACCT-CURR-BAL | `S9(10)V99 COMP-3` | `BigDecimal` | Current Balance |
| EXP-ACCT-CREDIT-LIMIT | `S9(10)V99` | `BigDecimal` | Credit Limit |
| EXP-ACCT-CASH-CREDIT-LIMIT | `S9(10)V99 COMP-3` | `BigDecimal` | Cash Limit |

---

## 13. Communication Area (COMMAREA)

**Copybook:** `COCOM01Y.cpy` | **Record:** `CARDDEMO-COMMAREA`  
**Purpose:** Passed between all CICS programs to maintain session state  
**Java Target:** `CardDemoSession.java` (HTTP session / security context)

| # | COBOL Field | PIC Clause | Java Type | Business Name | Description |
|---|-------------|------------|-----------|---------------|-------------|
| 1 | CDEMO-FROM-TRANID | `X(04)` | `String` | Source Transaction | Calling transaction ID |
| 2 | CDEMO-FROM-PROGRAM | `X(08)` | `String` | Source Program | Calling program name |
| 3 | CDEMO-TO-TRANID | `X(04)` | `String` | Target Transaction | Next transaction ID |
| 4 | CDEMO-TO-PROGRAM | `X(08)` | `String` | Target Program | Next program name |
| 5 | CDEMO-USER-ID | `X(08)` | `String` | User ID | Logged-in user |
| 6 | CDEMO-USER-TYPE | `X(01)` | `String` | User Role | `A`=Admin, `U`=User |
| 7 | CDEMO-PGM-CONTEXT | `9(01)` | `int` | Program State | 0=Enter, 1=Reenter |
| 8 | CDEMO-CUST-ID | `9(09)` | `int` | Customer ID | Selected customer |
| 9 | CDEMO-CUST-FNAME | `X(25)` | `String` | First Name | Customer first name |
| 10 | CDEMO-CUST-MNAME | `X(25)` | `String` | Middle Name | Customer middle name |
| 11 | CDEMO-CUST-LNAME | `X(25)` | `String` | Last Name | Customer last name |
| 12 | CDEMO-ACCT-ID | `9(11)` | `long` | Account ID | Selected account |
| 13 | CDEMO-ACCT-STATUS | `X(01)` | `String` | Account Status | Account active status |
| 14 | CDEMO-CARD-NUM | `9(16)` | `String` | Card Number | Selected card |
| 15 | CDEMO-LAST-MAP | `X(7)` | `String` | Last Map | Last displayed BMS map |
| 16 | CDEMO-LAST-MAPSET | `X(7)` | `String` | Last Mapset | Last used mapset |

---

## 14. Entity Relationship Summary

```
                          ┌─────────────┐
                          │  Customer   │
                          │  CVCUS01Y   │
                          │ PK: CUST-ID │
                          └──────┬──────┘
                                 │ 1:N
                                 │
                     ┌───────────┴───────────┐
                     │   Card Cross-Ref      │
                     │   CVACT03Y            │
                     │ XREF-CARD-NUM (PK)    │
                     │ XREF-CUST-ID (FK)     │
                     │ XREF-ACCT-ID (FK)     │
                     └───┬───────────┬───────┘
                         │           │
                    1:N  │           │ N:1
                         ▼           ▼
               ┌─────────────┐  ┌─────────────┐
               │ Credit Card │  │   Account   │
               │  CVACT02Y   │  │  CVACT01Y   │
               │PK: CARD-NUM │  │PK: ACCT-ID  │
               └──────┬──────┘  └──────┬──────┘
                      │                │
                      │ 1:N            │ 1:N
                      ▼                ▼
               ┌────────────────────────────┐
               │       Transaction          │
               │        CVTRA05Y            │
               │ PK: TRAN-ID                │
               │ FK: TRAN-CARD-NUM          │
               └─────────────┬──────────────┘
                             │
                    ┌────────┴────────┐
                    │                 │
                    ▼                 ▼
          ┌──────────────┐  ┌──────────────────┐
          │  Tran Type   │  │ Tran Category    │
          │  CVTRA03Y    │  │  CVTRA04Y        │
          │PK: TRAN-TYPE │  │PK: TYPE-CD+CAT-CD│
          └──────────────┘  └──────────────────┘
```

---

## 15. VSAM File-to-Entity Mapping

| VSAM File (DD Name) | VSAM Type | Key | Record Layout | Entity |
|---------------------|-----------|-----|---------------|--------|
| ACCTDAT | KSDS | ACCT-ID | CVACT01Y | Account |
| CARDDAT | KSDS | CARD-NUM | CVACT02Y | Credit Card |
| CARDAIX | AIX | CARD-ACCT-ID | CVACT02Y | Credit Card (by Account) |
| CUSTDAT | KSDS | CUST-ID | CVCUS01Y | Customer |
| CCXREF / CXACAIX | KSDS + AIX | XREF-CARD-NUM / XREF-ACCT-ID | CVACT03Y | Card Cross-Reference |
| TRANSACT | KSDS | TRAN-ID | CVTRA05Y | Transaction |
| USRSEC | KSDS | SEC-USR-ID | CSUSR01Y | User Security |
| DALYTRAN | Sequential | -- | CVTRA06Y | Daily Transaction Feed |
| TCATBALF | KSDS | Compound | CVTRA01Y | Category Balance |
| TRANTYPE | KSDS | TRAN-TYPE | CVTRA03Y | Transaction Type |
| TRANCATG | KSDS | TYPE-CD + CAT-CD | CVTRA04Y | Transaction Category |
| DISCGRP | KSDS | Group + Type + Cat | CVTRA02Y | Discount Group |

---

## 16. PII / Sensitive Data Fields

| Entity | Field | Sensitivity | Modernization Note |
|--------|-------|-------------|-------------------|
| Customer | CUST-SSN | **PII - High** | Must encrypt at rest and in transit |
| Customer | CUST-DOB-YYYY-MM-DD | **PII - Medium** | Subject to privacy regulations |
| Customer | CUST-PHONE-NUM-1/2 | **PII - Medium** | Mask in UI displays |
| Customer | CUST-GOVT-ISSUED-ID | **PII - High** | Government ID, encrypt |
| User Security | SEC-USR-PWD | **Credential** | Currently plain text -- must hash (BCrypt) |
| Credit Card | CARD-NUM | **PCI-DSS** | Must tokenize or encrypt, never log |
| Credit Card | CARD-CVV-CD | **PCI-DSS** | Must never persist post-authorization |
