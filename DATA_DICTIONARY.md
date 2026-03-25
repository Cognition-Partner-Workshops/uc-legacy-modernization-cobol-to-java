# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** Copybook PIC clause analysis (`app/cpy/`) | **Application:** CardDemo

---

## Overview

This document extracts every business entity defined in CardDemo copybooks and translates the COBOL PIC clauses into business-friendly descriptions. Each entity maps to a VSAM KSDS file (or sequential file) on the mainframe and will map to a relational database table in the modernized Java application.

---

## 1. Account Entity

**Copybook:** `CVACT01Y.cpy` | **Record Length:** 300 bytes | **VSAM File:** `ACCTDAT` (KSDS, key = ACCT-ID)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| ACCT-ID | `PIC 9(11)` | Numeric | 11 digits | Unique account identifier |
| ACCT-ACTIVE-STATUS | `PIC X(01)` | Alpha | 1 char | Account status flag (e.g., "Y" = Active) |
| ACCT-CURR-BAL | `PIC S9(10)V99` | Signed Decimal | 12 digits, 2 dec | Current account balance |
| ACCT-CREDIT-LIMIT | `PIC S9(10)V99` | Signed Decimal | 12 digits, 2 dec | Maximum credit limit |
| ACCT-CASH-CREDIT-LIMIT | `PIC S9(10)V99` | Signed Decimal | 12 digits, 2 dec | Maximum cash advance limit |
| ACCT-OPEN-DATE | `PIC X(10)` | Alpha | 10 chars | Account opening date (YYYY-MM-DD) |
| ACCT-EXPIRAION-DATE | `PIC X(10)` | Alpha | 10 chars | Account expiration date (YYYY-MM-DD) |
| ACCT-REISSUE-DATE | `PIC X(10)` | Alpha | 10 chars | Card reissue date (YYYY-MM-DD) |
| ACCT-CURR-CYC-CREDIT | `PIC S9(10)V99` | Signed Decimal | 12 digits, 2 dec | Current billing cycle credits |
| ACCT-CURR-CYC-DEBIT | `PIC S9(10)V99` | Signed Decimal | 12 digits, 2 dec | Current billing cycle debits |
| ACCT-ADDR-ZIP | `PIC X(10)` | Alpha | 10 chars | Account holder ZIP/postal code |
| ACCT-GROUP-ID | `PIC X(10)` | Alpha | 10 chars | Disclosure/interest rate group identifier |
| FILLER | `PIC X(178)` | Alpha | 178 chars | Reserved for future use |

**Java Mapping:** `Account` entity / `accounts` table | **Primary Key:** `ACCT-ID`

---

## 2. Credit Card Entity

**Copybook:** `CVACT02Y.cpy` | **Record Length:** 150 bytes | **VSAM File:** `CARDDAT` (KSDS, key = CARD-NUM)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| CARD-NUM | `PIC X(16)` | Alpha | 16 chars | Credit card number (PAN) |
| CARD-ACCT-ID | `PIC 9(11)` | Numeric | 11 digits | Linked account identifier |
| CARD-CVV-CD | `PIC 9(03)` | Numeric | 3 digits | Card verification value (CVV) |
| CARD-EMBOSSED-NAME | `PIC X(50)` | Alpha | 50 chars | Name embossed on physical card |
| CARD-EXPIRAION-DATE | `PIC X(10)` | Alpha | 10 chars | Card expiration date (YYYY-MM-DD) |
| CARD-ACTIVE-STATUS | `PIC X(01)` | Alpha | 1 char | Card active status flag |
| FILLER | `PIC X(59)` | Alpha | 59 chars | Reserved for future use |

**Java Mapping:** `Card` entity / `cards` table | **Primary Key:** `CARD-NUM` | **Foreign Key:** `CARD-ACCT-ID` -> Account

---

## 3. Card Cross-Reference Entity

**Copybook:** `CVACT03Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `CARDXREF` (KSDS, key = XREF-CARD-NUM) with alternate index `CARDAIX` (by XREF-ACCT-ID)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| XREF-CARD-NUM | `PIC X(16)` | Alpha | 16 chars | Credit card number |
| XREF-CUST-ID | `PIC 9(09)` | Numeric | 9 digits | Customer identifier |
| XREF-ACCT-ID | `PIC 9(11)` | Numeric | 11 digits | Account identifier |
| FILLER | `PIC X(14)` | Alpha | 14 chars | Reserved for future use |

**Java Mapping:** Relationship table `card_xref` or JPA `@ManyToOne` mappings | **Purpose:** Links cards to both customers and accounts

---

## 4. Customer Entity

**Copybook:** `CVCUS01Y.cpy` | **Record Length:** 500 bytes | **VSAM File:** `CUSTDAT` (KSDS, key = CUST-ID)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| CUST-ID | `PIC 9(09)` | Numeric | 9 digits | Unique customer identifier |
| CUST-FIRST-NAME | `PIC X(25)` | Alpha | 25 chars | Customer first name |
| CUST-MIDDLE-NAME | `PIC X(25)` | Alpha | 25 chars | Customer middle name |
| CUST-LAST-NAME | `PIC X(25)` | Alpha | 25 chars | Customer last name |
| CUST-ADDR-LINE-1 | `PIC X(50)` | Alpha | 50 chars | Address line 1 |
| CUST-ADDR-LINE-2 | `PIC X(50)` | Alpha | 50 chars | Address line 2 |
| CUST-ADDR-LINE-3 | `PIC X(50)` | Alpha | 50 chars | Address line 3 |
| CUST-ADDR-STATE-CD | `PIC X(02)` | Alpha | 2 chars | US state code |
| CUST-ADDR-COUNTRY-CD | `PIC X(03)` | Alpha | 3 chars | Country code |
| CUST-ADDR-ZIP | `PIC X(10)` | Alpha | 10 chars | ZIP/postal code |
| CUST-PHONE-NUM-1 | `PIC X(15)` | Alpha | 15 chars | Primary phone number |
| CUST-PHONE-NUM-2 | `PIC X(15)` | Alpha | 15 chars | Secondary phone number |
| CUST-SSN | `PIC 9(09)` | Numeric | 9 digits | Social Security Number (PII) |
| CUST-GOVT-ISSUED-ID | `PIC X(20)` | Alpha | 20 chars | Government-issued ID number |
| CUST-DOB-YYYY-MM-DD | `PIC X(10)` | Alpha | 10 chars | Date of birth (YYYY-MM-DD) |
| CUST-EFT-ACCOUNT-ID | `PIC X(10)` | Alpha | 10 chars | Electronic funds transfer account |
| CUST-PRI-CARD-HOLDER-IND | `PIC X(01)` | Alpha | 1 char | Primary card holder indicator |
| CUST-FICO-CREDIT-SCORE | `PIC 9(03)` | Numeric | 3 digits | FICO credit score (300-850) |
| FILLER | `PIC X(168)` | Alpha | 168 chars | Reserved for future use |

**Java Mapping:** `Customer` entity / `customers` table | **Primary Key:** `CUST-ID` | **PII Fields:** SSN, DOB, Govt ID

> **Note:** `CUSTREC.cpy` is an alternate layout of the same entity with `CUST-DOB-YYYYMMDD` (no hyphens) instead of `CUST-DOB-YYYY-MM-DD`.

---

## 5. Transaction Entity (Online)

**Copybook:** `CVTRA05Y.cpy` | **Record Length:** 350 bytes | **VSAM File:** `TRANSACT` (KSDS, key = TRAN-ID)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| TRAN-ID | `PIC X(16)` | Alpha | 16 chars | Unique transaction identifier |
| TRAN-TYPE-CD | `PIC X(02)` | Alpha | 2 chars | Transaction type code (SA=Sale, RE=Return, etc.) |
| TRAN-CAT-CD | `PIC 9(04)` | Numeric | 4 digits | Transaction category code |
| TRAN-SOURCE | `PIC X(10)` | Alpha | 10 chars | Transaction source (POS, ATM, Online, etc.) |
| TRAN-DESC | `PIC X(100)` | Alpha | 100 chars | Transaction description |
| TRAN-AMT | `PIC S9(09)V99` | Signed Decimal | 11 digits, 2 dec | Transaction amount |
| TRAN-MERCHANT-ID | `PIC 9(09)` | Numeric | 9 digits | Merchant identifier |
| TRAN-MERCHANT-NAME | `PIC X(50)` | Alpha | 50 chars | Merchant name |
| TRAN-MERCHANT-CITY | `PIC X(50)` | Alpha | 50 chars | Merchant city |
| TRAN-MERCHANT-ZIP | `PIC X(10)` | Alpha | 10 chars | Merchant ZIP code |
| TRAN-CARD-NUM | `PIC X(16)` | Alpha | 16 chars | Card number used for transaction |
| TRAN-ORIG-TS | `PIC X(26)` | Alpha | 26 chars | Original transaction timestamp |
| TRAN-PROC-TS | `PIC X(26)` | Alpha | 26 chars | Processing timestamp |
| FILLER | `PIC X(20)` | Alpha | 20 chars | Reserved for future use |

**Java Mapping:** `Transaction` entity / `transactions` table | **Primary Key:** `TRAN-ID` | **Foreign Key:** `TRAN-CARD-NUM` -> Card

---

## 6. Daily Transaction Entity (Batch Input)

**Copybook:** `CVTRA06Y.cpy` | **Record Length:** 350 bytes | **File:** `DALYTRAN` (Sequential input file)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| DALYTRAN-ID | `PIC X(16)` | Alpha | 16 chars | Daily transaction identifier |
| DALYTRAN-TYPE-CD | `PIC X(02)` | Alpha | 2 chars | Transaction type code |
| DALYTRAN-CAT-CD | `PIC 9(04)` | Numeric | 4 digits | Transaction category code |
| DALYTRAN-SOURCE | `PIC X(10)` | Alpha | 10 chars | Transaction source |
| DALYTRAN-DESC | `PIC X(100)` | Alpha | 100 chars | Transaction description |
| DALYTRAN-AMT | `PIC S9(09)V99` | Signed Decimal | 11 digits, 2 dec | Transaction amount |
| DALYTRAN-MERCHANT-ID | `PIC 9(09)` | Numeric | 9 digits | Merchant identifier |
| DALYTRAN-MERCHANT-NAME | `PIC X(50)` | Alpha | 50 chars | Merchant name |
| DALYTRAN-MERCHANT-CITY | `PIC X(50)` | Alpha | 50 chars | Merchant city |
| DALYTRAN-MERCHANT-ZIP | `PIC X(10)` | Alpha | 10 chars | Merchant ZIP code |
| DALYTRAN-CARD-NUM | `PIC X(16)` | Alpha | 16 chars | Card number |
| DALYTRAN-ORIG-TS | `PIC X(26)` | Alpha | 26 chars | Original timestamp |
| DALYTRAN-PROC-TS | `PIC X(26)` | Alpha | 26 chars | Processing timestamp |
| FILLER | `PIC X(20)` | Alpha | 20 chars | Reserved for future use |

**Java Mapping:** Same as Transaction entity; daily input is validated and posted to the `TRANSACT` file. In Java, this becomes an input DTO for the posting batch job.

---

## 7. Statement Transaction Entity

**Copybook:** `COSTM01.CPY` | **Record Length:** 350 bytes | **File:** `TRNXFILE` (KSDS, composite key = CARD-NUM + TRAN-ID)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| TRNX-CARD-NUM | `PIC X(16)` | Alpha | 16 chars | Card number (part of composite key) |
| TRNX-ID | `PIC X(16)` | Alpha | 16 chars | Transaction ID (part of composite key) |
| TRNX-TYPE-CD | `PIC X(02)` | Alpha | 2 chars | Transaction type code |
| TRNX-CAT-CD | `PIC 9(04)` | Numeric | 4 digits | Transaction category code |
| TRNX-SOURCE | `PIC X(10)` | Alpha | 10 chars | Transaction source |
| TRNX-DESC | `PIC X(100)` | Alpha | 100 chars | Transaction description |
| TRNX-AMT | `PIC S9(09)V99` | Signed Decimal | 11 digits, 2 dec | Transaction amount |
| TRNX-MERCHANT-ID | `PIC 9(09)` | Numeric | 9 digits | Merchant identifier |
| TRNX-MERCHANT-NAME | `PIC X(50)` | Alpha | 50 chars | Merchant name |
| TRNX-MERCHANT-CITY | `PIC X(50)` | Alpha | 50 chars | Merchant city |
| TRNX-MERCHANT-ZIP | `PIC X(10)` | Alpha | 10 chars | Merchant ZIP code |
| TRNX-ORIG-TS | `PIC X(26)` | Alpha | 26 chars | Original timestamp |
| TRNX-PROC-TS | `PIC X(26)` | Alpha | 26 chars | Processing timestamp |
| FILLER | `PIC X(20)` | Alpha | 20 chars | Reserved |

**Java Mapping:** Alternate view of Transaction indexed by card for statement generation.

---

## 8. Transaction Category Balance Entity

**Copybook:** `CVTRA01Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `TCATBALF` (KSDS, composite key = ACCT-ID + TYPE-CD + CAT-CD)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| TRANCAT-ACCT-ID | `PIC 9(11)` | Numeric | 11 digits | Account identifier |
| TRANCAT-TYPE-CD | `PIC X(02)` | Alpha | 2 chars | Transaction type code |
| TRANCAT-CD | `PIC 9(04)` | Numeric | 4 digits | Transaction category code |
| TRAN-CAT-BAL | `PIC S9(09)V99` | Signed Decimal | 11 digits, 2 dec | Running balance for this category |
| FILLER | `PIC X(22)` | Alpha | 22 chars | Reserved |

**Java Mapping:** `TransactionCategoryBalance` entity / summary table | **Composite Key:** account + type + category

---

## 9. Disclosure Group Entity

**Copybook:** `CVTRA02Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `DISCGRP` (KSDS, composite key = GROUP-ID + TYPE-CD + CAT-CD)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| DIS-ACCT-GROUP-ID | `PIC X(10)` | Alpha | 10 chars | Account group identifier |
| DIS-TRAN-TYPE-CD | `PIC X(02)` | Alpha | 2 chars | Transaction type code |
| DIS-TRAN-CAT-CD | `PIC 9(04)` | Numeric | 4 digits | Transaction category code |
| DIS-INT-RATE | `PIC S9(04)V99` | Signed Decimal | 6 digits, 2 dec | Interest rate percentage |
| FILLER | `PIC X(28)` | Alpha | 28 chars | Reserved |

**Java Mapping:** `DisclosureGroup` or `InterestRateConfig` entity | **Purpose:** Defines interest rates by account group and transaction type/category

---

## 10. Transaction Type Entity

**Copybook:** `CVTRA03Y.cpy` | **Record Length:** 60 bytes | **VSAM File:** `TRANTYPE` (KSDS, key = TRAN-TYPE)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| TRAN-TYPE | `PIC X(02)` | Alpha | 2 chars | Transaction type code (SA, RE, CR, DB, etc.) |
| TRAN-TYPE-DESC | `PIC X(50)` | Alpha | 50 chars | Type description (e.g., "Sale", "Return") |
| FILLER | `PIC X(08)` | Alpha | 8 chars | Reserved |

**Java Mapping:** `TransactionType` enum or reference table | **Primary Key:** `TRAN-TYPE`

---

## 11. Transaction Category Entity

**Copybook:** `CVTRA04Y.cpy` | **Record Length:** 60 bytes | **VSAM File:** `TRANCATG` (KSDS, composite key = TYPE-CD + CAT-CD)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| TRAN-TYPE-CD | `PIC X(02)` | Alpha | 2 chars | Transaction type code |
| TRAN-CAT-CD | `PIC 9(04)` | Numeric | 4 digits | Category code |
| TRAN-CAT-TYPE-DESC | `PIC X(50)` | Alpha | 50 chars | Category description |
| FILLER | `PIC X(04)` | Alpha | 4 chars | Reserved |

**Java Mapping:** `TransactionCategory` reference table | **Composite Key:** type + category

---

## 12. User Security Entity

**Copybook:** `CSUSR01Y.cpy` | **Record Length:** 80 bytes | **VSAM File:** `USRSEC` (KSDS, key = SEC-USR-ID)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| SEC-USR-ID | `PIC X(08)` | Alpha | 8 chars | User login ID |
| SEC-USR-FNAME | `PIC X(20)` | Alpha | 20 chars | User first name |
| SEC-USR-LNAME | `PIC X(20)` | Alpha | 20 chars | User last name |
| SEC-USR-PWD | `PIC X(08)` | Alpha | 8 chars | User password (plaintext -- modernize to hashed!) |
| SEC-USR-TYPE | `PIC X(01)` | Alpha | 1 char | User type: "A" = Admin, "U" = Regular user |
| SEC-USR-FILLER | `PIC X(23)` | Alpha | 23 chars | Reserved |

**Java Mapping:** `User` entity / `users` table with Spring Security integration | **Primary Key:** `SEC-USR-ID`

> **Security Note:** Passwords are stored in plaintext in the legacy system. The modernized application must use bcrypt/scrypt hashing.

---

## 13. Communication Area (COMMAREA)

**Copybook:** `COCOM01Y.cpy` | **Purpose:** Inter-program communication -- not persisted to disk

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| CDEMO-FROM-TRANID | `PIC X(04)` | Alpha | 4 chars | Originating CICS transaction ID |
| CDEMO-FROM-PROGRAM | `PIC X(08)` | Alpha | 8 chars | Originating program name |
| CDEMO-TO-TRANID | `PIC X(04)` | Alpha | 4 chars | Target CICS transaction ID |
| CDEMO-TO-PROGRAM | `PIC X(08)` | Alpha | 8 chars | Target program name |
| CDEMO-USER-ID | `PIC X(08)` | Alpha | 8 chars | Current user ID |
| CDEMO-USER-TYPE | `PIC X(01)` | Alpha | 1 char | "A" = Admin, "U" = User |
| CDEMO-PGM-CONTEXT | `PIC 9(01)` | Numeric | 1 digit | 0 = first entry, 1 = re-entry |
| CDEMO-CUST-ID | `PIC 9(09)` | Numeric | 9 digits | Selected customer ID |
| CDEMO-CUST-FNAME | `PIC X(25)` | Alpha | 25 chars | Customer first name |
| CDEMO-CUST-MNAME | `PIC X(25)` | Alpha | 25 chars | Customer middle name |
| CDEMO-CUST-LNAME | `PIC X(25)` | Alpha | 25 chars | Customer last name |
| CDEMO-ACCT-ID | `PIC 9(11)` | Numeric | 11 digits | Selected account ID |
| CDEMO-ACCT-STATUS | `PIC X(01)` | Alpha | 1 char | Account status |
| CDEMO-CARD-NUM | `PIC 9(16)` | Numeric | 16 digits | Selected card number |
| CDEMO-LAST-MAP | `PIC X(7)` | Alpha | 7 chars | Last BMS map displayed |
| CDEMO-LAST-MAPSET | `PIC X(7)` | Alpha | 7 chars | Last BMS mapset used |

**Java Mapping:** Session state (HTTP session or Spring `@SessionScope` bean) | **Not persisted** -- transient navigation context

---

## 14. Export Record (Multi-Type)

**Copybook:** `CVEXPORT.cpy` | **Record Length:** 500 bytes | **File:** Sequential export file

This is a union record using COBOL `REDEFINES` to pack multiple entity types into a single sequential file for branch migration/data export.

| Field Name | PIC Clause | Type | Business Description |
|------------|-----------|------|---------------------|
| EXPORT-REC-TYPE | `PIC X(1)` | Alpha | Record type discriminator |
| EXPORT-TIMESTAMP | `PIC X(26)` | Alpha | Export timestamp |
| EXPORT-SEQUENCE-NUM | `PIC 9(9) COMP` | Binary | Sequence number |
| EXPORT-BRANCH-ID | `PIC X(4)` | Alpha | Branch identifier |
| EXPORT-REGION-CODE | `PIC X(5)` | Alpha | Region code |
| EXPORT-RECORD-DATA | `PIC X(460)` | Alpha | Polymorphic record data (REDEFINES below) |

**REDEFINES variants:** EXPORT-CUSTOMER-DATA, EXPORT-ACCOUNT-DATA, EXPORT-TRANSACTION-DATA, EXPORT-CARD-XREF-DATA, EXPORT-CARD-DATA -- each mirrors the corresponding entity with some fields using COMP/COMP-3 for storage optimization.

**Java Mapping:** Not needed as a single entity; instead, export/import logic will serialize/deserialize to JSON or CSV per entity type.

---

## 15. Utility / Framework Data Structures

### 15.1 Date/Time Working Storage (`CSDAT01Y.cpy`)

| Field Name | PIC Clause | Business Description |
|------------|-----------|---------------------|
| WS-CURDATE-YEAR | `PIC 9(04)` | Current year (YYYY) |
| WS-CURDATE-MONTH | `PIC 9(02)` | Current month |
| WS-CURDATE-DAY | `PIC 9(02)` | Current day |
| WS-CURTIME-HOURS | `PIC 9(02)` | Current hour |
| WS-CURTIME-MINUTE | `PIC 9(02)` | Current minute |
| WS-CURTIME-SECOND | `PIC 9(02)` | Current second |
| WS-TIMESTAMP | composite | Full timestamp (YYYY-MM-DD HH:MM:SS.SSSSSS) |

**Java Mapping:** `java.time.LocalDateTime` / `java.time.Instant`

### 15.2 Date Conversion Area (`CODATECN.cpy`)

| Field Name | PIC Clause | Business Description |
|------------|-----------|---------------------|
| CODATECN-TYPE | `PIC X` | Input format: "1" = YYYYMMDD, "2" = YYYY-MM-DD |
| CODATECN-INP-DATE | `PIC X(20)` | Input date string |
| CODATECN-OUTTYPE | `PIC X` | Output format: "1" = YYYY-MM-DD, "2" = YYYYMMDD |
| CODATECN-0UT-DATE | `PIC X(20)` | Output date string |
| CODATECN-ERROR-MSG | `PIC X(38)` | Conversion error message |

**Java Mapping:** Not needed; use `java.time.format.DateTimeFormatter`

### 15.3 Abend Handling (`CSMSG02Y.cpy`)

| Field Name | PIC Clause | Business Description |
|------------|-----------|---------------------|
| ABEND-CODE | `PIC X(4)` | Abend code |
| ABEND-CULPRIT | `PIC X(8)` | Program that caused the abend |
| ABEND-REASON | `PIC X(50)` | Reason description |
| ABEND-MSG | `PIC X(72)` | Full error message |

**Java Mapping:** Exception handling / structured error response DTO

### 15.4 Credit Card Work Area (`CVCRD01Y.cpy`)

| Field Name | PIC Clause | Business Description |
|------------|-----------|---------------------|
| CCARD-AID | `PIC X(5)` | Terminal AID key pressed |
| CCARD-NEXT-PROG | `PIC X(8)` | Next program to transfer to |
| CCARD-NEXT-MAPSET | `PIC X(7)` | Next BMS mapset |
| CCARD-NEXT-MAP | `PIC X(7)` | Next BMS map |
| CCARD-ERROR-MSG | `PIC X(75)` | Error message to display |
| CCARD-RETURN-MSG | `PIC X(75)` | Return/info message |
| CC-ACCT-ID | `PIC X(11)` | Working account ID |
| CC-CARD-NUM | `PIC X(16)` | Working card number |
| CC-CUST-ID | `PIC X(09)` | Working customer ID |

**Java Mapping:** Controller/service state; maps to Spring MVC model attributes

### 15.5 Lookup Code Repository (`CSLKPCDY.cpy` -- 1,318 lines)

Contains hardcoded validation tables:
- **North American phone area codes** (440+ codes)
- **US state codes** (50 states + territories)
- **State-to-ZIP prefix mappings**

**Java Mapping:** Database reference tables or `enum`s with validation annotations

---

## Entity Relationship Summary

```
Customer (CUST-ID)
    |
    |-- 1:N --> Card Cross-Ref (XREF-CARD-NUM, XREF-CUST-ID, XREF-ACCT-ID)
    |               |
    |               |-- N:1 --> Account (ACCT-ID)
    |               |               |
    |               |               |-- 1:N --> Tran Category Balance (ACCT-ID + TYPE + CAT)
    |               |               |
    |               |               |-- N:1 --> Disclosure Group (GROUP-ID + TYPE + CAT)
    |               |
    |               |-- N:1 --> Card (CARD-NUM)
    |                               |
    |                               |-- 1:N --> Transaction (TRAN-ID, TRAN-CARD-NUM)
    |
User Security (SEC-USR-ID) -- standalone authentication entity

Reference Data:
    Transaction Type (TRAN-TYPE) -- lookup for TRAN-TYPE-CD
    Transaction Category (TYPE-CD + CAT-CD) -- lookup for TRAN-CAT-CD
    Disclosure Group (GROUP-ID + TYPE-CD + CAT-CD) -- interest rates
```

---

## VSAM File Summary

| VSAM File | Entity | Key | Record Len | Access Method |
|-----------|--------|-----|-----------|---------------|
| ACCTDAT | Account | ACCT-ID | 300 | KSDS |
| CARDDAT | Card | CARD-NUM | 150 | KSDS |
| CARDXREF | Card Cross-Ref | XREF-CARD-NUM | 50 | KSDS + AIX (CARDAIX) |
| CUSTDAT | Customer | CUST-ID | 500 | KSDS |
| TRANSACT | Transaction | TRAN-ID | 350 | KSDS |
| USRSEC | User Security | SEC-USR-ID | 80 | KSDS |
| TCATBALF | Tran Cat Balance | ACCT+TYPE+CAT | 50 | KSDS |
| DISCGRP | Disclosure Group | GROUP+TYPE+CAT | 50 | KSDS |
| TRANTYPE | Transaction Type | TRAN-TYPE | 60 | KSDS |
| TRANCATG | Transaction Category | TYPE+CAT | 60 | KSDS |
| DALYTRAN | Daily Transaction | (Sequential) | 350 | Sequential |
| DALYREJS | Daily Rejects | (Sequential) | 350 | Sequential |
| TRNXFILE | Statement Tran | CARD+TRAN-ID | 350 | KSDS |
