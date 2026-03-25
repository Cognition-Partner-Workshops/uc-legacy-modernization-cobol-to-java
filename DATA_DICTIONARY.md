# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Source:** Copybook PIC clause analysis from `app/cpy/`

---

## Overview

This data dictionary extracts all business entities from the CardDemo COBOL copybooks, translating mainframe PIC clauses into business-friendly descriptions. Each entity maps to a VSAM KSDS file used by CICS online and batch programs.

### Quick Reference — PIC Clause Translation

| COBOL PIC | Meaning | Java Equivalent |
|-----------|---------|-----------------|
| `PIC X(n)` | Alphanumeric, n characters | `String` |
| `PIC 9(n)` | Unsigned numeric, n digits | `int` / `long` |
| `PIC S9(n)V99` | Signed decimal with 2 implied decimals | `BigDecimal` |
| `PIC S9(n) COMP` | Binary integer | `int` / `long` |
| `PIC 9(n) COMP-3` | Packed decimal | `BigDecimal` |

---

## 1. Account Entity

> **Copybook:** `CVACT01Y.cpy` | **Record Name:** `ACCOUNT-RECORD` | **Record Length:** 300 bytes
> **VSAM File:** `ACCTDAT` (KSDS, key = ACCT-ID)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|------------|------|--------|---------------------|
| 1 | ACCT-ID | `9(11)` | Numeric | 11 | Account identifier (primary key) |
| 2 | ACCT-ACTIVE-STATUS | `X(01)` | Alpha | 1 | Account status: `Y` = Active, `N` = Inactive |
| 3 | ACCT-CURR-BAL | `S9(10)V99` | Decimal | 12.2 | Current account balance |
| 4 | ACCT-CREDIT-LIMIT | `S9(10)V99` | Decimal | 12.2 | Maximum credit limit |
| 5 | ACCT-CASH-CREDIT-LIMIT | `S9(10)V99` | Decimal | 12.2 | Cash advance credit limit |
| 6 | ACCT-OPEN-DATE | `X(10)` | Date | 10 | Account open date (YYYY-MM-DD) |
| 7 | ACCT-EXPIRAION-DATE | `X(10)` | Date | 10 | Account expiration date |
| 8 | ACCT-REISSUE-DATE | `X(10)` | Date | 10 | Last card reissue date |
| 9 | ACCT-CURR-CYC-CREDIT | `S9(10)V99` | Decimal | 12.2 | Current cycle credit total |
| 10 | ACCT-CURR-CYC-DEBIT | `S9(10)V99` | Decimal | 12.2 | Current cycle debit total |
| 11 | ACCT-ADDR-ZIP | `X(10)` | Alpha | 10 | Account holder ZIP code |
| 12 | ACCT-GROUP-ID | `X(10)` | Alpha | 10 | Account group identifier (for discount rates) |
| 13 | FILLER | `X(178)` | — | 178 | Reserved space |

**Business Rules:**
- Interest is calculated by `CBACT04C` using `ACCT-CURR-BAL` and discount group rates
- `ACCT-ACTIVE-STATUS` must be `Y` or `N` (validated by `COACTUPC`)
- Balance updates occur during transaction posting (`CBTRN02C`)

---

## 2. Credit Card Entity

> **Copybook:** `CVACT02Y.cpy` | **Record Name:** `CARD-RECORD` | **Record Length:** 150 bytes
> **VSAM File:** `CARDDAT` (KSDS, key = CARD-NUM) | **Alt Index:** `CARDAIX` (by CARD-ACCT-ID)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|------------|------|--------|---------------------|
| 1 | CARD-NUM | `X(16)` | Alpha | 16 | Credit card number (primary key) |
| 2 | CARD-ACCT-ID | `9(11)` | Numeric | 11 | Associated account ID (foreign key) |
| 3 | CARD-CVV-CD | `9(03)` | Numeric | 3 | Card verification value (CVV) |
| 4 | CARD-EMBOSSED-NAME | `X(50)` | Alpha | 50 | Name embossed on card |
| 5 | CARD-EXPIRAION-DATE | `X(10)` | Date | 10 | Card expiration date (YYYY-MM-DD) |
| 6 | CARD-ACTIVE-STATUS | `X(01)` | Alpha | 1 | Card status: `Y` = Active, `N` = Inactive |
| 7 | FILLER | `X(59)` | — | 59 | Reserved space |

**Business Rules:**
- Multiple cards can be linked to one account via `CARD-ACCT-ID`
- Card listing (`COCRDLIC`) browses by CARDDAT or CARDAIX alternate index
- Card status validated during updates (`COCRDUPC`)

---

## 3. Customer Entity

> **Copybook:** `CVCUS01Y.cpy` | **Record Name:** `CUSTOMER-RECORD` | **Record Length:** 500 bytes
> **VSAM File:** `CUSTDAT` (KSDS, key = CUST-ID)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|------------|------|--------|---------------------|
| 1 | CUST-ID | `9(09)` | Numeric | 9 | Customer identifier (primary key) |
| 2 | CUST-FIRST-NAME | `X(25)` | Alpha | 25 | Customer first name |
| 3 | CUST-MIDDLE-NAME | `X(25)` | Alpha | 25 | Customer middle name |
| 4 | CUST-LAST-NAME | `X(25)` | Alpha | 25 | Customer last name |
| 5 | CUST-ADDR-LINE-1 | `X(50)` | Alpha | 50 | Address line 1 |
| 6 | CUST-ADDR-LINE-2 | `X(50)` | Alpha | 50 | Address line 2 |
| 7 | CUST-ADDR-LINE-3 | `X(50)` | Alpha | 50 | Address line 3 |
| 8 | CUST-ADDR-STATE-CD | `X(02)` | Alpha | 2 | US state code |
| 9 | CUST-ADDR-COUNTRY-CD | `X(03)` | Alpha | 3 | Country code |
| 10 | CUST-ADDR-ZIP | `X(10)` | Alpha | 10 | ZIP/postal code |
| 11 | CUST-PHONE-NUM-1 | `X(15)` | Alpha | 15 | Primary phone number |
| 12 | CUST-PHONE-NUM-2 | `X(15)` | Alpha | 15 | Secondary phone number |
| 13 | CUST-SSN | `9(09)` | Numeric | 9 | Social Security Number |
| 14 | CUST-GOVT-ISSUED-ID | `X(20)` | Alpha | 20 | Government-issued ID number |
| 15 | CUST-DOB-YYYY-MM-DD | `X(10)` | Date | 10 | Date of birth |
| 16 | CUST-EFT-ACCOUNT-ID | `X(10)` | Alpha | 10 | Electronic funds transfer account |
| 17 | CUST-PRI-CARD-HOLDER-IND | `X(01)` | Alpha | 1 | Primary cardholder indicator |
| 18 | CUST-FICO-CREDIT-SCORE | `9(03)` | Numeric | 3 | FICO credit score (300–850) |
| 19 | FILLER | `X(168)` | — | 168 | Reserved space |

**Business Rules:**
- Customer is linked to accounts via the cross-reference file (`CVACT03Y`)
- SSN validated with 3-part structure (area/group/serial) in `COACTUPC`
- Date of birth validated to not be in the future
- FICO score displayed in account view and update screens

---

## 4. Card Cross-Reference Entity

> **Copybook:** `CVACT03Y.cpy` | **Record Name:** `CARD-XREF-RECORD` | **Record Length:** 50 bytes
> **VSAM File:** `CARDXREF` (KSDS, key = XREF-CARD-NUM) | **Alt Index:** `CXACAIX` (by XREF-ACCT-ID)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|------------|------|--------|---------------------|
| 1 | XREF-CARD-NUM | `X(16)` | Alpha | 16 | Card number (primary key) |
| 2 | XREF-CUST-ID | `9(09)` | Numeric | 9 | Customer ID (foreign key) |
| 3 | XREF-ACCT-ID | `9(11)` | Numeric | 11 | Account ID (foreign key) |
| 4 | FILLER | `X(14)` | — | 14 | Reserved space |

**Business Rules:**
- This is the central relationship table linking cards → customers → accounts
- Used by account view (`COACTVWC`), transaction add (`COTRN02C`), and bill payment (`COBIL00C`)
- The CXACAIX alternate index allows lookup by account ID

---

## 5. Transaction Entity (Online)

> **Copybook:** `CVTRA05Y.cpy` | **Record Name:** `TRAN-RECORD` | **Record Length:** 350 bytes
> **VSAM File:** `TRANSACT` (KSDS, key = TRAN-ID)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|------------|------|--------|---------------------|
| 1 | TRAN-ID | `X(16)` | Alpha | 16 | Transaction ID (primary key) |
| 2 | TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code (e.g., `SA` = Sale) |
| 3 | TRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 4 | TRAN-SOURCE | `X(10)` | Alpha | 10 | Transaction source channel |
| 5 | TRAN-DESC | `X(100)` | Alpha | 100 | Transaction description |
| 6 | TRAN-AMT | `S9(09)V99` | Decimal | 11.2 | Transaction amount |
| 7 | TRAN-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant identifier |
| 8 | TRAN-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| 9 | TRAN-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| 10 | TRAN-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP code |
| 11 | TRAN-CARD-NUM | `X(16)` | Alpha | 16 | Card used for transaction |
| 12 | TRAN-ORIG-TS | `X(26)` | Timestamp | 26 | Original transaction timestamp |
| 13 | TRAN-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | `X(20)` | — | 20 | Reserved space |

**Business Rules:**
- Transactions are added online via `COTRN02C` and posted from daily files by `CBTRN02C`
- Transaction list (`COTRN00C`) browses the TRANSACT file with paging
- Reports (`CBTRN03C`) aggregate by type and category codes

---

## 6. Daily Transaction Entity

> **Copybook:** `CVTRA06Y.cpy` | **Record Name:** `DALYTRAN-RECORD` | **Record Length:** 350 bytes
> **VSAM File:** `DALYTRAN` (sequential input for batch posting)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|------------|------|--------|---------------------|
| 1 | DALYTRAN-ID | `X(16)` | Alpha | 16 | Daily transaction ID |
| 2 | DALYTRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| 3 | DALYTRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 4 | DALYTRAN-SOURCE | `X(10)` | Alpha | 10 | Transaction source |
| 5 | DALYTRAN-DESC | `X(100)` | Alpha | 100 | Transaction description |
| 6 | DALYTRAN-AMT | `S9(09)V99` | Decimal | 11.2 | Transaction amount |
| 7 | DALYTRAN-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant identifier |
| 8 | DALYTRAN-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| 9 | DALYTRAN-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| 10 | DALYTRAN-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP code |
| 11 | DALYTRAN-CARD-NUM | `X(16)` | Alpha | 16 | Card number |
| 12 | DALYTRAN-ORIG-TS | `X(26)` | Timestamp | 26 | Original timestamp |
| 13 | DALYTRAN-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | `X(20)` | — | 20 | Reserved space |

**Business Rules:**
- Same structure as online transactions (CVTRA05Y) with `DALYTRAN-` prefix
- Read by `CBTRN01C` and `CBTRN02C` during batch posting
- Rejected daily transactions are written to `DALYREJS` file

---

## 7. User Security Entity

> **Copybook:** `CSUSR01Y.cpy` | **Record Name:** `SEC-USER-DATA` | **Record Length:** 80 bytes
> **VSAM File:** `USRSEC` (KSDS, key = SEC-USR-ID)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|------------|------|--------|---------------------|
| 1 | SEC-USR-ID | `X(08)` | Alpha | 8 | User ID (primary key) |
| 2 | SEC-USR-FNAME | `X(20)` | Alpha | 20 | User first name |
| 3 | SEC-USR-LNAME | `X(20)` | Alpha | 20 | User last name |
| 4 | SEC-USR-PWD | `X(08)` | Alpha | 8 | User password (plain text) |
| 5 | SEC-USR-TYPE | `X(01)` | Alpha | 1 | User type: `A` = Admin, `U` = Regular |
| 6 | SEC-USR-FILLER | `X(23)` | — | 23 | Reserved space |

**Business Rules:**
- Authenticated by `COSGN00C` — reads USRSEC, compares password
- Admin users (`A`) get the admin menu (`COADM01C`); regular users (`U`) get the main menu (`COMEN01C`)
- CRUD operations via `COUSR00C`–`COUSR03C` (admin only)
- Loaded initially by `DUSRSECJ` JCL job from inline data

---

## 8. Transaction Category Balance Entity

> **Copybook:** `CVTRA01Y.cpy` | **Record Name:** `TRAN-CAT-BAL-RECORD`
> **VSAM File:** `TCATBALF`

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|------------|------|--------|---------------------|
| 1 | TRANCAT-ACCT-ID | `9(11)` | Numeric | 11 | Account ID (part of composite key) |
| 2 | TRANCAT-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| 3 | TRANCAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 4 | TRAN-CAT-BAL | `S9(09)V99` | Decimal | 11.2 | Category balance amount |
| 5 | FILLER | `X(22)` | — | 22 | Reserved space |

**Business Rules:**
- Aggregated balance per account + transaction type + category
- Updated during interest calculation (`CBACT04C`) and transaction posting (`CBTRN02C`)

---

## 9. Discount/Interest Rate Group Entity

> **Copybook:** `CVTRA02Y.cpy` | **Record Name:** `DIS-INT-RATE-RECORD`
> **VSAM File:** `DISCGRP`

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|------------|------|--------|---------------------|
| 1 | DIS-ACCT-GROUP-ID | `X(10)` | Alpha | 10 | Account group ID (key) |
| 2 | DIS-TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| 3 | DIS-TRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| 4 | DIS-INT-RATE | `S9(04)V99` | Decimal | 6.2 | Interest/discount rate percentage |
| 5 | FILLER | `X(28)` | — | 28 | Reserved space |

**Business Rules:**
- Interest rates are looked up by account group + transaction type + category
- Used by `CBACT04C` to calculate interest charges

---

## 10. Transaction Type Lookup Entity

> **Copybook:** `CVTRA03Y.cpy` | **Record Name:** `TRAN-TYPE-RECORD` | **Record Length:** 60 bytes
> **VSAM File:** `TRANTYPE`

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|------------|------|--------|---------------------|
| 1 | TRAN-TYPE | `X(02)` | Alpha | 2 | Transaction type code (key) |
| 2 | TRAN-TYPE-DESC | `X(50)` | Alpha | 50 | Human-readable type description |
| 3 | FILLER | `X(08)` | — | 8 | Reserved space |

---

## 11. Transaction Category Type Entity

> **Copybook:** `CVTRA04Y.cpy` | **Record Name:** `TRAN-CAT-TYPE-RECORD`
> **VSAM File:** `TRANCATG`

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|------------|------|--------|---------------------|
| 1 | TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code (composite key part 1) |
| 2 | TRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code (composite key part 2) |
| 3 | TRAN-CAT-TYPE-DESC | `X(50)` | Alpha | 50 | Category + type description |
| 4 | FILLER | `X(04)` | — | 4 | Reserved space |

---

## 12. Application Communication Area

> **Copybook:** `COCOM01Y.cpy` | **Record Name:** `CARDDEMO-COMMAREA`
> **Usage:** Passed between all CICS online programs via DFHCOMMAREA

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|------------|------|--------|---------------------|
| 1 | CDEMO-FROM-TRANID | `X(04)` | Alpha | 4 | Originating transaction ID |
| 2 | CDEMO-FROM-PROGRAM | `X(08)` | Alpha | 8 | Originating program name |
| 3 | CDEMO-TO-TRANID | `X(04)` | Alpha | 4 | Target transaction ID |
| 4 | CDEMO-TO-PROGRAM | `X(08)` | Alpha | 8 | Target program name |
| 5 | CDEMO-USER-ID | `X(08)` | Alpha | 8 | Logged-in user ID |
| 6 | CDEMO-USER-TYPE | `X(01)` | Alpha | 1 | `A` = Admin, `U` = User |
| 7 | CDEMO-PGM-CONTEXT | `9(01)` | Numeric | 1 | 0 = first entry, 1 = re-entry |
| 8 | CDEMO-CUST-ID | `9(09)` | Numeric | 9 | Selected customer ID |
| 9 | CDEMO-CUST-FNAME | `X(25)` | Alpha | 25 | Customer first name |
| 10 | CDEMO-CUST-MNAME | `X(25)` | Alpha | 25 | Customer middle name |
| 11 | CDEMO-CUST-LNAME | `X(25)` | Alpha | 25 | Customer last name |
| 12 | CDEMO-ACCT-ID | `9(11)` | Numeric | 11 | Selected account ID |
| 13 | CDEMO-ACCT-STATUS | `X(01)` | Alpha | 1 | Account status |
| 14 | CDEMO-CARD-NUM | `9(16)` | Numeric | 16 | Selected card number |
| 15 | CDEMO-LAST-MAP | `X(7)` | Alpha | 7 | Last displayed BMS map |
| 16 | CDEMO-LAST-MAPSET | `X(7)` | Alpha | 7 | Last displayed mapset |

---

## 13. Export Record (Data Migration)

> **Copybook:** `CVEXPORT.cpy` | **Record Name:** `EXPORT-RECORD` | **Record Length:** 500 bytes
> **Usage:** Multi-record export file for branch data migration

### 13.1 Export Header Fields

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|------------|------|--------|---------------------|
| 1 | EXPORT-REC-TYPE | `X(1)` | Alpha | 1 | Record type: `H`=Header, `C`=Customer, `A`=Account, `T`=Transaction, `F`=Footer |
| 2 | EXPORT-TIMESTAMP | `X(26)` | Timestamp | 26 | Export timestamp |
| 3 | EXPORT-SEQUENCE-NUM | `9(9) COMP` | Numeric | 4 | Record sequence number |
| 4 | EXPORT-BRANCH-ID | `X(4)` | Alpha | 4 | Branch identifier |
| 5 | EXPORT-REGION-CODE | `X(5)` | Alpha | 5 | Region code |
| 6 | EXPORT-RECORD-DATA | `X(460)` | Alpha | 460 | Record payload (redefined per type) |

### 13.2 Export Customer Sub-Record

| Field | PIC | Description |
|-------|-----|-------------|
| EXP-CUST-ID | `9(09) COMP` | Customer ID |
| EXP-CUST-FIRST-NAME | `X(25)` | First name |
| EXP-CUST-LAST-NAME | `X(25)` | Last name |
| EXP-CUST-SSN | `9(09)` | Social Security Number |
| EXP-CUST-DOB-YYYY-MM-DD | `X(10)` | Date of birth |
| EXP-CUST-FICO-CREDIT-SCORE | `9(03) COMP-3` | FICO score |

### 13.3 Export Account Sub-Record

| Field | PIC | Description |
|-------|-----|-------------|
| EXP-ACCT-ID | `9(11)` | Account ID |
| EXP-ACCT-ACTIVE-STATUS | `X(01)` | Active status |
| EXP-ACCT-CURR-BAL | `S9(10)V99 COMP-3` | Current balance |
| EXP-ACCT-CREDIT-LIMIT | `S9(10)V99` | Credit limit |
| EXP-ACCT-OPEN-DATE | `X(10)` | Open date |

### 13.4 Export Transaction Sub-Record

| Field | PIC | Description |
|-------|-----|-------------|
| EXP-TRAN-ID | `X(16)` | Transaction ID |
| EXP-TRAN-TYPE-CD | `X(02)` | Type code |
| EXP-TRAN-AMT | `S9(09)V99 COMP-3` | Amount |
| EXP-TRAN-CARD-NUM | `X(16)` | Card number |
| EXP-TRAN-MERCHANT-NAME | `X(50)` | Merchant name |

---

## 14. Date Conversion Record

> **Copybook:** `CODATECN.cpy` | **Record Name:** `CODATECN-REC`
> **Usage:** Parameter block for `COBDATFT` assembler date formatting routine

| # | Field Name | PIC Clause | Description |
|---|-----------|------------|-------------|
| 1 | CODATECN-TYPE | `X` | Input format: `1` = YYYYMMDD, `2` = YYYY-MM-DD |
| 2 | CODATECN-INP-DATE | `X(20)` | Input date string |
| 3 | CODATECN-OUTTYPE | `X` | Output format: `1` = YYYY-MM-DD, `2` = YYYYMMDD |
| 4 | CODATECN-0UT-DATE | `X(20)` | Formatted output date |
| 5 | CODATECN-ERROR-MSG | `X(38)` | Error message if conversion fails |

---

## 15. Entity Relationship Summary

```
CUSTOMER (CVCUS01Y)
    │
    ├──< CARD-XREF (CVACT03Y) >──┐
    │         │                    │
    │         ▼                    ▼
    │    CARD (CVACT02Y)    ACCOUNT (CVACT01Y)
    │                              │
    │                              ├──< TRAN-CAT-BAL (CVTRA01Y)
    │                              │
    │                              └──< DISCOUNT-GROUP (CVTRA02Y)
    │
    └──< TRANSACTION (CVTRA05Y)
              │
              ├── TRAN-TYPE (CVTRA03Y)
              └── TRAN-CATEGORY (CVTRA04Y)

USER-SECURITY (CSUSR01Y) — standalone authentication table
```

### Key Relationships

| From Entity | To Entity | Relationship | Join Key |
|------------|-----------|-------------|----------|
| Customer | Card Cross-Ref | 1:Many | CUST-ID = XREF-CUST-ID |
| Account | Card Cross-Ref | 1:Many | ACCT-ID = XREF-ACCT-ID |
| Card | Card Cross-Ref | 1:1 | CARD-NUM = XREF-CARD-NUM |
| Account | Transaction | 1:Many | via XREF → TRAN-CARD-NUM |
| Account | Tran Cat Balance | 1:Many | ACCT-ID = TRANCAT-ACCT-ID |
| Account Group | Discount Rate | 1:Many | ACCT-GROUP-ID = DIS-ACCT-GROUP-ID |
| Transaction | Tran Type | N:1 | TRAN-TYPE-CD = TRAN-TYPE |
| Transaction | Tran Category | N:1 | TRAN-TYPE-CD + TRAN-CAT-CD |

---

## 16. VSAM File Inventory

| VSAM File | Key Field | Record Length | Entity Copybook | Access Pattern |
|-----------|-----------|--------------|----------------|----------------|
| ACCTDAT | ACCT-ID | 300 | CVACT01Y | KSDS (direct + browse) |
| CARDDAT | CARD-NUM | 150 | CVACT02Y | KSDS + Alt Index (CARDAIX) |
| CUSTDAT | CUST-ID | 500 | CVCUS01Y | KSDS (direct read) |
| CARDXREF | XREF-CARD-NUM | 50 | CVACT03Y | KSDS + Alt Index (CXACAIX) |
| TRANSACT | TRAN-ID | 350 | CVTRA05Y | KSDS (direct + browse) |
| USRSEC | SEC-USR-ID | 80 | CSUSR01Y | KSDS (direct + browse) |
| DALYTRAN | — | 350 | CVTRA06Y | Sequential input |
| DALYREJS | — | 350 | CVTRA06Y | Sequential output |
| TCATBALF | composite | ~50 | CVTRA01Y | KSDS |
| DISCGRP | composite | ~50 | CVTRA02Y | KSDS |
| TRANTYPE | TRAN-TYPE | 60 | CVTRA03Y | KSDS |
| TRANCATG | composite | ~60 | CVTRA04Y | KSDS |
