# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** `app/cpy/` copybook PIC clause analysis  
> **Purpose:** Business-friendly reference of all data entities, their fields, types, and sizes for modernization planning.

---

## 1. Account Master (`CVACT01Y.cpy`)

**VSAM File:** `ACCTDAT` (KSDS) | **Record Length:** 300 bytes | **Key:** Account ID (11 digits)

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | ACCT-ID | 9(11) | Numeric | 11 | Account identifier (primary key) |
| 2 | ACCT-ACTIVE-STATUS | X(01) | Alpha | 1 | Account status: `Y` = active, `N` = inactive |
| 3 | ACCT-CURR-BAL | S9(10)V99 | Signed Decimal | 12.2 | Current account balance |
| 4 | ACCT-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12.2 | Credit limit |
| 5 | ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12.2 | Cash advance credit limit |
| 6 | ACCT-OPEN-DATE | X(10) | Date String | 10 | Account opening date (YYYY-MM-DD) |
| 7 | ACCT-EXPIRAION-DATE | X(10) | Date String | 10 | Account expiration date |
| 8 | ACCT-REISSUE-DATE | X(10) | Date String | 10 | Last card reissue date |
| 9 | ACCT-CURR-CYC-CREDIT | S9(10)V99 | Signed Decimal | 12.2 | Current cycle credit total |
| 10 | ACCT-CURR-CYC-DEBIT | S9(10)V99 | Signed Decimal | 12.2 | Current cycle debit total |
| 11 | ACCT-ADDR-ZIP | X(10) | Alpha | 10 | Billing ZIP code |
| 12 | ACCT-GROUP-ID | X(10) | Alpha | 10 | Disclosure/rate group identifier |
| 13 | FILLER | X(178) | Filler | 178 | Reserved space |

**Java Target:** `Account` entity → JPA table `ACCOUNT`

---

## 2. Credit Card Record (`CVACT02Y.cpy`)

**VSAM File:** `CARDDAT` (KSDS) | **Record Length:** 150 bytes | **Key:** Card Number (16 digits)

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | CARD-NUM | X(16) | Alpha | 16 | Credit card number (primary key) |
| 2 | CARD-ACCT-ID | 9(11) | Numeric | 11 | Owning account ID (FK → Account) |
| 3 | CARD-CVV-CD | 9(03) | Numeric | 3 | Card verification value (CVV) |
| 4 | CARD-EMBOSSED-NAME | X(50) | Alpha | 50 | Name embossed on the card |
| 5 | CARD-EXPIRAION-DATE | X(10) | Date String | 10 | Card expiration date (YYYY-MM-DD) |
| 6 | CARD-ACTIVE-STATUS | X(01) | Alpha | 1 | Card status: `Y` = active, `N` = inactive |
| 7 | FILLER | X(59) | Filler | 59 | Reserved space |

**Java Target:** `CreditCard` entity → JPA table `CREDIT_CARD`

---

## 3. Customer Master (`CVCUS01Y.cpy`)

**VSAM File:** `CUSTDAT` (KSDS) | **Record Length:** 500 bytes | **Key:** Customer ID (9 digits)

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | CUST-ID | 9(09) | Numeric | 9 | Customer identifier (primary key) |
| 2 | CUST-FIRST-NAME | X(25) | Alpha | 25 | First name |
| 3 | CUST-MIDDLE-NAME | X(25) | Alpha | 25 | Middle name |
| 4 | CUST-LAST-NAME | X(25) | Alpha | 25 | Last name |
| 5 | CUST-ADDR-LINE-1 | X(50) | Alpha | 50 | Address line 1 |
| 6 | CUST-ADDR-LINE-2 | X(50) | Alpha | 50 | Address line 2 |
| 7 | CUST-ADDR-LINE-3 | X(50) | Alpha | 50 | Address line 3 |
| 8 | CUST-ADDR-STATE-CD | X(02) | Alpha | 2 | State code |
| 9 | CUST-ADDR-COUNTRY-CD | X(03) | Alpha | 3 | Country code |
| 10 | CUST-ADDR-ZIP | X(10) | Alpha | 10 | ZIP / postal code |
| 11 | CUST-PHONE-NUM-1 | X(15) | Alpha | 15 | Primary phone number |
| 12 | CUST-PHONE-NUM-2 | X(15) | Alpha | 15 | Secondary phone number |
| 13 | CUST-SSN | 9(09) | Numeric | 9 | Social Security Number |
| 14 | CUST-GOVT-ISSUED-ID | X(20) | Alpha | 20 | Government-issued ID |
| 15 | CUST-DOB-YYYYMMDD | X(10) | Date String | 10 | Date of birth |
| 16 | CUST-EFT-ACCOUNT-ID | X(10) | Alpha | 10 | Electronic funds transfer account |
| 17 | CUST-PRI-CARD-HOLDER-IND | X(01) | Alpha | 1 | Primary cardholder indicator |
| 18 | CUST-FICO-CREDIT-SCORE | 9(03) | Numeric | 3 | FICO credit score |
| 19 | FILLER | X(168) | Filler | 168 | Reserved space |

**Java Target:** `Customer` entity → JPA table `CUSTOMER`

---

## 4. Card-Account Cross-Reference (`CVACT03Y.cpy`)

**VSAM File:** `CARDXREF` (KSDS) | **Record Length:** 50 bytes | **Key:** Card Number (16 digits)

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | XREF-CARD-NUM | X(16) | Alpha | 16 | Card number (primary key) |
| 2 | XREF-CUST-ID | 9(09) | Numeric | 9 | Customer ID (FK → Customer) |
| 3 | XREF-ACCT-ID | 9(11) | Numeric | 11 | Account ID (FK → Account) |
| 4 | FILLER | X(14) | Filler | 14 | Reserved space |

**Java Target:** Relationship mapping — `CreditCard` ↔ `Account` ↔ `Customer` join table or embedded FK

---

## 5. Transaction Record (`CVTRA05Y.cpy`)

**VSAM File:** `TRANSACT` (KSDS) | **Record Length:** 350 bytes | **Key:** Transaction ID (16 chars)

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | TRAN-ID | X(16) | Alpha | 16 | Transaction identifier (primary key) |
| 2 | TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code (FK → Tran Type) |
| 3 | TRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| 4 | TRAN-SOURCE | X(10) | Alpha | 10 | Transaction source (POS, ATM, Online, etc.) |
| 5 | TRAN-DESC | X(100) | Alpha | 100 | Transaction description |
| 6 | TRAN-AMT | S9(09)V99 | Signed Decimal | 11.2 | Transaction amount |
| 7 | TRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant identifier |
| 8 | TRAN-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| 9 | TRAN-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| 10 | TRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP code |
| 11 | TRAN-CARD-NUM | X(16) | Alpha | 16 | Card number used |
| 12 | TRAN-ORIG-TS | X(26) | Timestamp | 26 | Original transaction timestamp |
| 13 | TRAN-PROC-TS | X(26) | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | X(20) | Filler | 20 | Reserved space |

**Java Target:** `Transaction` entity → JPA table `TRANSACTION`

---

## 6. Daily Transaction Record (`CVTRA06Y.cpy`)

**VSAM File:** `DALYTRAN` (Sequential) | **Record Length:** 350 bytes | **Key:** Daily Transaction ID

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | DALYTRAN-ID | X(16) | Alpha | 16 | Daily transaction ID |
| 2 | DALYTRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| 3 | DALYTRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| 4 | DALYTRAN-SOURCE | X(10) | Alpha | 10 | Source system |
| 5 | DALYTRAN-DESC | X(100) | Alpha | 100 | Description |
| 6 | DALYTRAN-AMT | S9(09)V99 | Signed Decimal | 11.2 | Amount |
| 7 | DALYTRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant ID |
| 8 | DALYTRAN-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| 9 | DALYTRAN-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| 10 | DALYTRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP |
| 11 | DALYTRAN-CARD-NUM | X(16) | Alpha | 16 | Card number |
| 12 | DALYTRAN-ORIG-TS | X(26) | Timestamp | 26 | Original timestamp |
| 13 | DALYTRAN-PROC-TS | X(26) | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | X(20) | Filler | 20 | Reserved space |

**Java Target:** Staging table `DAILY_TRANSACTION` or merged into `TRANSACTION` after posting

---

## 7. Transaction Category Balance (`CVTRA01Y.cpy`)

**VSAM File:** `TCATBALF` (KSDS) | **Record Length:** 50 bytes | **Key:** Account ID + Type Code + Category Code

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | TRANCAT-ACCT-ID | 9(11) | Numeric | 11 | Account ID (part of composite key) |
| 2 | TRANCAT-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| 3 | TRANCAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| 4 | TRAN-CAT-BAL | S9(09)V99 | Signed Decimal | 11.2 | Running category balance |
| 5 | FILLER | X(22) | Filler | 22 | Reserved space |

**Java Target:** `TransactionCategoryBalance` entity → aggregate/summary table

---

## 8. Disclosure Group (`CVTRA02Y.cpy`)

**VSAM File:** `DISCGRP` (KSDS) | **Record Length:** 50 bytes | **Key:** Group ID + Type Code + Category Code

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | DIS-ACCT-GROUP-ID | X(10) | Alpha | 10 | Account disclosure group ID |
| 2 | DIS-TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| 3 | DIS-TRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| 4 | DIS-INT-RATE | S9(04)V99 | Signed Decimal | 6.2 | Interest rate for this group/type/category |
| 5 | FILLER | X(28) | Filler | 28 | Reserved space |

**Java Target:** `DisclosureGroup` entity → lookup/configuration table

---

## 9. Transaction Type (`CVTRA03Y.cpy`)

**VSAM File:** `TRANTYPE` (KSDS) | **Record Length:** 60 bytes | **Key:** Transaction Type Code

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | TRAN-TYPE | X(02) | Alpha | 2 | Transaction type code (primary key) |
| 2 | TRAN-TYPE-DESC | X(50) | Alpha | 50 | Transaction type description |
| 3 | FILLER | X(08) | Filler | 8 | Reserved space |

**Java Target:** `TransactionType` enum or lookup table

---

## 10. Transaction Category (`CVTRA04Y.cpy`)

**VSAM File:** `TRANCATG` (KSDS) | **Record Length:** 60 bytes | **Key:** Type Code + Category Code

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code (composite key part 1) |
| 2 | TRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code (composite key part 2) |
| 3 | TRAN-CAT-TYPE-DESC | X(50) | Alpha | 50 | Category description |
| 4 | FILLER | X(04) | Filler | 4 | Reserved space |

**Java Target:** `TransactionCategory` entity → lookup table

---

## 11. User Security Record (`CSUSR01Y.cpy`)

**VSAM File:** `USRSEC` (KSDS) | **Record Length:** 80 bytes | **Key:** User ID (8 chars)

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | SEC-USR-ID | X(08) | Alpha | 8 | User ID (primary key) |
| 2 | SEC-USR-FNAME | X(20) | Alpha | 20 | First name |
| 3 | SEC-USR-LNAME | X(20) | Alpha | 20 | Last name |
| 4 | SEC-USR-PWD | X(08) | Alpha | 8 | Password (plaintext) |
| 5 | SEC-USR-TYPE | X(01) | Alpha | 1 | User type: `A` = admin, `U` = regular user |
| 6 | FILLER | X(23) | Filler | 23 | Reserved space |

**Java Target:** `User` entity with Spring Security integration → BCrypt password hashing

---

## 12. Statement Transaction Layout (`COSTM01.CPY`)

**File:** Sorted transaction file for statement generation | **Key:** Card Number + Transaction ID

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | TRNX-CARD-NUM | X(16) | Alpha | 16 | Card number (sort key part 1) |
| 2 | TRNX-ID | X(16) | Alpha | 16 | Transaction ID (sort key part 2) |
| 3 | TRNX-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| 4 | TRNX-CAT-CD | 9(04) | Numeric | 4 | Category code |
| 5 | TRNX-SOURCE | X(10) | Alpha | 10 | Transaction source |
| 6 | TRNX-DESC | X(100) | Alpha | 100 | Description |
| 7 | TRNX-AMT | S9(09)V99 | Signed Decimal | 11.2 | Amount |
| 8 | TRNX-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant ID |
| 9 | TRNX-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| 10 | TRNX-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| 11 | TRNX-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP |
| 12 | TRNX-ORIG-TS | X(26) | Timestamp | 26 | Original timestamp |
| 13 | TRNX-PROC-TS | X(26) | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | X(20) | Filler | 20 | Reserved |

**Java Target:** Intermediate DTO used in statement generation batch job

---

## 13. Export/Import Record (`CVEXPORT.cpy`)

**File:** Sequential export file | **Record Length:** 500 bytes per sub-record

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | EXP-RECORD-TYPE | X(01) | Alpha | 1 | Record type indicator (`C`=Customer, `A`=Account, `X`=Xref, `T`=Transaction, `R`=Card) |
| 2 | EXP-DATA (variant) | — | — | 499 | Data payload varies by record type |

Sub-record types embed the full record from the corresponding entity above.

**Java Target:** CSV/JSON export service

---

## 14. Transaction Report Layout (`CVTRA07Y.cpy`)

**Output:** Print file / statement report

| # | Structure | Business Description |
|---|-----------|---------------------|
| 1 | REPORT-NAME-HEADER | Report title, date range (`DALYREPT — Daily Transaction Report`) |
| 2 | TRANSACTION-DETAIL-REPORT | Per-transaction line: ID, Account, Type, Category, Source, Amount |
| 3 | TRANSACTION-HEADER-1/2 | Column headers and separator line |
| 4 | REPORT-PAGE-TOTALS | Page subtotal |
| 5 | REPORT-ACCOUNT-TOTALS | Account subtotal |
| 6 | REPORT-GRAND-TOTALS | Grand total |

**Java Target:** Report POJO / Jasper template / PDF generator

---

## 15. Common Application COMMAREA (`COCOM01Y.cpy`)

**Shared across all online CICS programs via EXEC CICS RETURN COMMAREA**

| # | Field Name | COBOL PIC | Business Description |
|---|-----------|-----------|---------------------|
| 1 | CDEMO-FROM-TRANID | X(04) | Source transaction ID |
| 2 | CDEMO-FROM-PROGRAM | X(08) | Source program name |
| 3 | CDEMO-TO-TRANID | X(04) | Destination transaction ID |
| 4 | CDEMO-TO-PROGRAM | X(08) | Destination program name |
| 5 | CDEMO-USER-ID | X(08) | Logged-in user ID |
| 6 | CDEMO-USER-TYPE | X(01) | User type (`A`/`U`) |
| 7 | CDEMO-PGM-CONTEXT | 9(01) | Program context (0=enter, 1=reenter) |
| 8 | CDEMO-ACCT-ID | 9(11) | Current account ID in context |
| 9 | CDEMO-CARD-NUM | X(16) | Current card number in context |
| 10 | CDEMO-LAST-MAP | X(07) | Last BMS map displayed |
| 11 | CDEMO-LAST-MAPSET | X(07) | Last BMS mapset used |
| 12 | CCARD-AID-* | X(01) | PF key indicator flags |
| 13 | CCARD-ERROR-MSG | X(80) | Error message area |
| 14 | CCARD-NEXT-PROG | X(08) | Next program to transfer to |

**Java Target:** `SessionContext` or HTTP session attributes / Spring Security context

---

## 16. Optional Module Entities

### 16.1 Authorization Summary (`CIPAUSMY.cpy`)

| # | Field Name | COBOL PIC | Type | Business Description |
|---|-----------|-----------|------|---------------------|
| 1 | PAUS-TRAN-KEY | X(16) | Alpha | Authorization transaction key |
| 2 | PAUS-CARD-NUM | X(16) | Alpha | Card number |
| 3 | PAUS-TRAN-AMT | S9(09)V99 | Decimal | Authorization amount |
| 4 | PAUS-TRAN-DT | X(10) | Date | Transaction date |
| 5 | PAUS-TRAN-STATUS | X(02) | Alpha | Status (AP=approved, DN=denied) |

### 16.2 Authorization Detail (`CIPAUDTY.cpy`)

| # | Field Name | COBOL PIC | Type | Business Description |
|---|-----------|-----------|------|---------------------|
| 1 | PAUD-TRAN-KEY | X(16) | Alpha | Authorization key |
| 2 | PAUD-CARD-NUM | X(16) | Alpha | Card number |
| 3 | PAUD-TRAN-AMT | S9(09)V99 | Decimal | Amount |
| 4 | PAUD-MERCHANT-NAME | X(50) | Alpha | Merchant name |
| 5 | PAUD-MERCHANT-CITY | X(50) | Alpha | Merchant city |
| 6 | PAUD-FRAUD-FLAG | X(01) | Alpha | Fraud flag (Y/N) |
| 7 | PAUD-REASON-CD | X(04) | Alpha | Decision reason code |

### 16.3 DB2 Transaction Type (DB2 module `CSDB2RWY.cpy`)

Additional DB2 working-storage for SQLCA, SQLCODE handling, DSNTIAC message formatting.

---

## 17. VSAM-to-RDBMS Mapping Summary

| VSAM File | VSAM Type | Key | Record Len | Target Table | Est. Volume |
|-----------|-----------|-----|------------|-------------|-------------|
| `USRSEC` | KSDS | User ID (8) | 80 | `APP_USER` | Low (~100) |
| `ACCTDAT` | KSDS | Account ID (11) | 300 | `ACCOUNT` | Medium (~10K) |
| `CUSTDAT` | KSDS | Customer ID (9) | 500 | `CUSTOMER` | Medium (~10K) |
| `CARDDAT` | KSDS | Card Num (16) | 150 | `CREDIT_CARD` | Medium (~20K) |
| `CARDXREF` | KSDS | Card Num (16) | 50 | FK relationships | Medium (~20K) |
| `TRANSACT` | KSDS | Tran ID (16) | 350 | `TRANSACTION` | High (~1M+) |
| `DALYTRAN` | Sequential | Tran ID (16) | 350 | `DAILY_TRANSACTION` (staging) | High (~50K/day) |
| `TCATBALF` | KSDS | Acct+Type+Cat | 50 | `TRAN_CATEGORY_BALANCE` | Medium |
| `TRANTYPE` | KSDS | Type Code (2) | 60 | `TRANSACTION_TYPE` | Low (~20) |
| `TRANCATG` | KSDS | Type+Cat | 60 | `TRANSACTION_CATEGORY` | Low (~100) |
| `DISCGRP` | KSDS | Group+Type+Cat | 50 | `DISCLOSURE_GROUP` | Low (~50) |
| `DALYREJS` | KSDS | — | — | `DAILY_REJECT` | Low |

---

## 18. Data Type Mapping Reference

| COBOL PIC Clause | Java Type | SQL Type | Notes |
|-----------------|-----------|----------|-------|
| `X(n)` | `String` | `VARCHAR(n)` | Fixed-length, space-padded → trim on read |
| `9(n)` | `long` / `int` | `BIGINT` / `INT` | Unsigned integer |
| `S9(n)V99` | `BigDecimal` | `DECIMAL(n+2,2)` | Signed with 2 decimal places |
| `S9(n) COMP` | `int` / `long` | `INT` / `BIGINT` | Binary integer |
| `S9(n) COMP-3` | `BigDecimal` | `DECIMAL(n,0)` | Packed decimal |
| `X(10)` date | `LocalDate` | `DATE` | Parse YYYY-MM-DD format |
| `X(26)` timestamp | `LocalDateTime` | `TIMESTAMP` | Parse ISO-like format |
| `X(01)` flag | `boolean` / `enum` | `CHAR(1)` | Map Y/N → true/false |
