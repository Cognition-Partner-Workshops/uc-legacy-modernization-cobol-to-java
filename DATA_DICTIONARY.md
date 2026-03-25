# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** `uc-legacy-modernization-cobol-to-java`
> **Application:** CardDemo -- Mainframe Credit Card Management System

---

## Overview

This document extracts business entities from COBOL copybook PIC clauses and presents them in a business-friendly format suitable for database schema design, Java POJO/DTO generation, and data migration planning.

### Data Type Legend

| COBOL PIC Clause     | Business Type          | Java Equivalent         | SQL Equivalent     |
|----------------------|------------------------|-------------------------|--------------------|
| `PIC X(n)`           | Text / String (n chars)| `String`                | `VARCHAR(n)`       |
| `PIC 9(n)`           | Unsigned Integer       | `long` / `int`          | `NUMERIC(n)`       |
| `PIC S9(n)V99`       | Signed Decimal (2dp)   | `BigDecimal`            | `DECIMAL(n+2,2)`   |
| `PIC S9(n) COMP`     | Binary Integer         | `int`                   | `INTEGER`          |
| `PIC S9(n) COMP-3`   | Packed Decimal         | `BigDecimal`            | `DECIMAL(n)`       |

---

## 1. Account Entity

**Copybook:** `CVACT01Y` | **Record Length:** 300 bytes | **VSAM File:** `ACCTDATA.VSAM.KSDS`

| # | Field Name              | PIC Clause       | Type            | Business Description                  |
|---|-------------------------|------------------|-----------------|---------------------------------------|
| 1 | `ACCT-ID`               | `PIC 9(11)`      | Numeric (11)    | **Account ID** (primary key)          |
| 2 | `ACCT-ACTIVE-STATUS`    | `PIC X(01)`      | Text (1)        | Account status (A=Active, I=Inactive) |
| 3 | `ACCT-CURR-BAL`         | `PIC S9(10)V99`  | Decimal (12,2)  | Current account balance               |
| 4 | `ACCT-CREDIT-LIMIT`     | `PIC S9(10)V99`  | Decimal (12,2)  | Credit limit                          |
| 5 | `ACCT-CASH-CREDIT-LIMIT`| `PIC S9(10)V99`  | Decimal (12,2)  | Cash advance credit limit             |
| 6 | `ACCT-OPEN-DATE`        | `PIC X(10)`      | Text (10)       | Account open date (YYYY-MM-DD)        |
| 7 | `ACCT-EXPIRAION-DATE`   | `PIC X(10)`      | Text (10)       | Account expiration date               |
| 8 | `ACCT-REISSUE-DATE`     | `PIC X(10)`      | Text (10)       | Last reissue date                     |
| 9 | `ACCT-CURR-CYC-CREDIT`  | `PIC S9(10)V99`  | Decimal (12,2)  | Current cycle credit total            |
| 10| `ACCT-CURR-CYC-DEBIT`   | `PIC S9(10)V99`  | Decimal (12,2)  | Current cycle debit total             |
| 11| `ACCT-GROUP-ID`         | `PIC X(10)`      | Text (10)       | Account group / disclosure group ID   |
| 12| FILLER                  | `PIC X(178)`     | --              | Reserved space                        |

**Business Rules:**
- Key: `ACCT-ID` (11-digit numeric)
- Used by: Account View, Account Update, Interest Calc, Statement Gen, Bill Payment
- Cross-referenced via `CVACT03Y` (Card XREF) to link cards to accounts

---

## 2. Card Entity

**Copybook:** `CVACT02Y` | **Record Length:** 150 bytes | **VSAM File:** `CARDDATA.VSAM.KSDS`

| # | Field Name              | PIC Clause       | Type            | Business Description                  |
|---|-------------------------|------------------|-----------------|---------------------------------------|
| 1 | `CARD-NUM`              | `PIC X(16)`      | Text (16)       | **Card number** (primary key)         |
| 2 | `CARD-ACCT-ID`          | `PIC 9(11)`      | Numeric (11)    | Owning account ID (FK to Account)     |
| 3 | `CARD-CVV-CD`           | `PIC 9(03)`      | Numeric (3)     | Card verification value (CVV)         |
| 4 | `CARD-EMBOSSED-NAME`    | `PIC X(50)`      | Text (50)       | Name embossed on card                 |
| 5 | `CARD-EXPIRAION-DATE`   | `PIC X(10)`      | Text (10)       | Card expiration date                  |
| 6 | `CARD-ACTIVE-STATUS`    | `PIC X(01)`      | Text (1)        | Card status (Y=Active, N=Inactive)    |
| 7 | FILLER                  | `PIC X(59)`      | --              | Reserved space                        |

**Business Rules:**
- Key: `CARD-NUM` (16-character, typically numeric)
- Multiple cards can belong to one account (1:N relationship)
- Used by: Card List, Card View, Card Update, Transaction Add

---

## 3. Card Cross-Reference Entity

**Copybook:** `CVACT03Y` | **Record Length:** 50 bytes | **VSAM File:** `CARDXREF.VSAM.KSDS`

| # | Field Name              | PIC Clause       | Type            | Business Description                  |
|---|-------------------------|------------------|-----------------|---------------------------------------|
| 1 | `XREF-CARD-NUM`         | `PIC X(16)`      | Text (16)       | **Card number** (primary key)         |
| 2 | `XREF-ACCT-ID`          | `PIC 9(11)`      | Numeric (11)    | Account ID (FK to Account)            |
| 3 | `XREF-CUST-ID`          | `PIC 9(09)`      | Numeric (9)     | Customer ID (FK to Customer)          |
| 4 | FILLER                  | `PIC X(14)`      | --              | Reserved space                        |

**Business Rules:**
- Links Card -> Account -> Customer (central relationship table)
- Alternate Index on `XREF-ACCT-ID` allows lookup by account
- Critical for transaction posting, statement generation, and most online queries

---

## 4. Customer Entity

**Copybook:** `CVCUS01Y` | **Record Length:** 500 bytes | **VSAM File:** `CUSTDATA.VSAM.KSDS`

| # | Field Name              | PIC Clause       | Type            | Business Description                  |
|---|-------------------------|------------------|-----------------|---------------------------------------|
| 1 | `CUST-ID`               | `PIC 9(09)`      | Numeric (9)     | **Customer ID** (primary key)         |
| 2 | `CUST-FIRST-NAME`       | `PIC X(25)`      | Text (25)       | First name                            |
| 3 | `CUST-MIDDLE-NAME`      | `PIC X(25)`      | Text (25)       | Middle name                           |
| 4 | `CUST-LAST-NAME`        | `PIC X(25)`      | Text (25)       | Last name                             |
| 5 | `CUST-ADDR-LINE-1`      | `PIC X(50)`      | Text (50)       | Address line 1                        |
| 6 | `CUST-ADDR-LINE-2`      | `PIC X(50)`      | Text (50)       | Address line 2                        |
| 7 | `CUST-ADDR-LINE-3`      | `PIC X(50)`      | Text (50)       | Address line 3                        |
| 8 | `CUST-ADDR-STATE-CD`    | `PIC X(02)`      | Text (2)        | State code (US)                       |
| 9 | `CUST-ADDR-COUNTRY-CD`  | `PIC X(03)`      | Text (3)        | Country code                          |
| 10| `CUST-ADDR-ZIP`         | `PIC X(10)`      | Text (10)       | ZIP / postal code                     |
| 11| `CUST-PHONE-NUM-1`      | `PIC X(15)`      | Text (15)       | Primary phone                         |
| 12| `CUST-PHONE-NUM-2`      | `PIC X(15)`      | Text (15)       | Secondary phone                       |
| 13| `CUST-SSN`              | `PIC 9(09)`      | Numeric (9)     | Social Security Number (PII)          |
| 14| `CUST-GOVT-ISSUED-ID`   | `PIC X(20)`      | Text (20)       | Government-issued ID                  |
| 15| `CUST-DOB-YYYYMMDD`     | `PIC X(10)`      | Text (10)       | Date of birth                         |
| 16| `CUST-EFT-ACCOUNT-ID`   | `PIC X(10)`      | Text (10)       | EFT / bank account for payments       |
| 17| `CUST-PRI-CARD-HOLDER-IND` | `PIC X(01)`   | Text (1)        | Primary cardholder indicator           |
| 18| `CUST-FICO-CREDIT-SCORE`| `PIC 9(03)`      | Numeric (3)     | FICO credit score                     |
| 19| FILLER                  | `PIC X(168)`     | --              | Reserved space                        |

**Business Rules:**
- Key: `CUST-ID` (9-digit numeric)
- Contains PII (SSN, DOB) -- sensitive data handling required
- Referenced via `CVACT03Y` cross-reference from card/account
- Used by: Account View, Card View, Statement Generation

---

## 5. Transaction Entity

**Copybook:** `CVTRA05Y` | **Record Length:** 350 bytes | **VSAM File:** `TRANSACT.VSAM.KSDS`

| # | Field Name              | PIC Clause         | Type            | Business Description                  |
|---|-------------------------|--------------------|-----------------|---------------------------------------|
| 1 | `TRAN-ID`               | `PIC X(16)`        | Text (16)       | **Transaction ID** (primary key)      |
| 2 | `TRAN-TYPE-CD`          | `PIC X(02)`        | Text (2)        | Transaction type code (FK)            |
| 3 | `TRAN-CAT-CD`           | `PIC 9(04)`        | Numeric (4)     | Transaction category code (FK)        |
| 4 | `TRAN-SOURCE`           | `PIC X(10)`        | Text (10)       | Transaction source (POS, ATM, etc.)   |
| 5 | `TRAN-DESC`             | `PIC X(100)`       | Text (100)      | Transaction description               |
| 6 | `TRAN-AMT`              | `PIC S9(09)V99`    | Decimal (11,2)  | Transaction amount                    |
| 7 | `TRAN-MERCHANT-ID`      | `PIC 9(09)`        | Numeric (9)     | Merchant identifier                   |
| 8 | `TRAN-MERCHANT-NAME`    | `PIC X(50)`        | Text (50)       | Merchant name                         |
| 9 | `TRAN-MERCHANT-CITY`    | `PIC X(50)`        | Text (50)       | Merchant city                         |
| 10| `TRAN-MERCHANT-ZIP`     | `PIC X(10)`        | Text (10)       | Merchant ZIP code                     |
| 11| `TRAN-CARD-NUM`         | `PIC X(16)`        | Text (16)       | Card number (FK to Card)              |
| 12| `TRAN-ORIG-TS`          | `PIC X(26)`        | Text (26)       | Original timestamp                    |
| 13| `TRAN-PROC-TS`          | `PIC X(26)`        | Text (26)       | Processing timestamp                  |
| 14| FILLER                  | `PIC X(20)`        | --              | Reserved space                        |

**Business Rules:**
- Key: `TRAN-ID` (16-character unique identifier)
- Links to Card via `TRAN-CARD-NUM`
- Core financial record -- highest volume entity
- Used by: Transaction List/View/Add, Posting, Reports, Statements

---

## 6. Daily Transaction Entity

**Copybook:** `CVTRA06Y` | **Record Length:** 350 bytes | **VSAM File:** `DALYTRAN`

| # | Field Name              | PIC Clause         | Type            | Business Description                  |
|---|-------------------------|--------------------|-----------------|---------------------------------------|
| 1 | `DALYTRAN-ID`           | `PIC X(16)`        | Text (16)       | Daily transaction ID                  |
| 2 | `DALYTRAN-TYPE-CD`      | `PIC X(02)`        | Text (2)        | Transaction type code                 |
| 3 | `DALYTRAN-CAT-CD`       | `PIC 9(04)`        | Numeric (4)     | Transaction category code             |
| 4 | `DALYTRAN-SOURCE`       | `PIC X(10)`        | Text (10)       | Transaction source                    |
| 5 | `DALYTRAN-DESC`         | `PIC X(100)`       | Text (100)      | Transaction description               |
| 6 | `DALYTRAN-AMT`          | `PIC S9(09)V99`    | Decimal (11,2)  | Transaction amount                    |
| 7 | `DALYTRAN-MERCHANT-ID`  | `PIC 9(09)`        | Numeric (9)     | Merchant identifier                   |
| 8 | `DALYTRAN-MERCHANT-NAME`| `PIC X(50)`        | Text (50)       | Merchant name                         |
| 9 | `DALYTRAN-MERCHANT-CITY`| `PIC X(50)`        | Text (50)       | Merchant city                         |
| 10| `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)`        | Text (10)       | Merchant ZIP code                     |
| 11| `DALYTRAN-CARD-NUM`     | `PIC X(16)`        | Text (16)       | Card number                           |
| 12| `DALYTRAN-ORIG-TS`      | `PIC X(26)`        | Text (26)       | Original timestamp                    |
| 13| `DALYTRAN-PROC-TS`      | `PIC X(26)`        | Text (26)       | Processing timestamp                  |
| 14| FILLER                  | `PIC X(20)`        | --              | Reserved space                        |

**Business Rules:**
- Identical layout to Transaction Entity (`CVTRA05Y`)
- Serves as staging/input for batch posting cycle
- Validated by `CBTRN01C`, posted to master by `CBTRN02C`
- Rejected records written to `DALYREJS` file

---

## 7. Transaction Category Balance Entity

**Copybook:** `CVTRA01Y` | **Record Length:** 50 bytes | **VSAM File:** `TCATBALF`

| # | Field Name              | PIC Clause         | Type            | Business Description                  |
|---|-------------------------|--------------------|-----------------|---------------------------------------|
| 1 | `TRANCAT-ACCT-ID`       | `PIC 9(11)`        | Numeric (11)    | Account ID (part of composite key)    |
| 2 | `TRANCAT-TYPE-CD`       | `PIC X(02)`        | Text (2)        | Transaction type code (part of key)   |
| 3 | `TRANCAT-CD`            | `PIC 9(04)`        | Numeric (4)     | Transaction category code (part of key)|
| 4 | `TRAN-CAT-BAL`          | `PIC S9(09)V99`    | Decimal (11,2)  | Running balance for this category     |
| 5 | FILLER                  | `PIC X(22)`        | --              | Reserved space                        |

**Business Rules:**
- Composite Key: Account + Type + Category
- Updated during transaction posting (`CBTRN02C`)
- Used for interest calculation by category (`CBACT04C`)

---

## 8. Disclosure Group Entity

**Copybook:** `CVTRA02Y` | **Record Length:** 50 bytes | **VSAM File:** `DISCGRP`

| # | Field Name              | PIC Clause         | Type            | Business Description                  |
|---|-------------------------|--------------------|-----------------|---------------------------------------|
| 1 | `DIS-ACCT-GROUP-ID`     | `PIC X(10)`        | Text (10)       | Account group ID (part of key)        |
| 2 | `DIS-TRAN-TYPE-CD`      | `PIC X(02)`        | Text (2)        | Transaction type code (part of key)   |
| 3 | `DIS-TRAN-CAT-CD`       | `PIC 9(04)`        | Numeric (4)     | Transaction category code (part of key)|
| 4 | `DIS-INT-RATE`          | `PIC S9(04)V99`    | Decimal (6,2)   | Interest rate for this group/category |
| 5 | FILLER                  | `PIC X(28)`        | --              | Reserved space                        |

**Business Rules:**
- Defines interest rates per account group + transaction type + category
- Referenced by interest calculation (`CBACT04C`)
- Regulatory disclosure grouping for different card products

---

## 9. Transaction Type Entity

**Copybook:** `CVTRA03Y` | **Record Length:** 60 bytes | **VSAM File:** `TRANTYPE`

| # | Field Name              | PIC Clause         | Type            | Business Description                  |
|---|-------------------------|--------------------|-----------------|---------------------------------------|
| 1 | `TRAN-TYPE`             | `PIC X(02)`        | Text (2)        | **Transaction type code** (PK)        |
| 2 | `TRAN-TYPE-DESC`        | `PIC X(50)`        | Text (50)       | Type description                      |
| 3 | FILLER                  | `PIC X(08)`        | --              | Reserved space                        |

**Business Rules:**
- Lookup / reference table
- Examples: "01" = Purchase, "02" = Return, "03" = Cash Advance
- Used by reports and transaction screens for display

---

## 10. Transaction Category Type Entity

**Copybook:** `CVTRA04Y` | **Record Length:** 60 bytes | **VSAM File:** `TRANCATG`

| # | Field Name              | PIC Clause         | Type            | Business Description                  |
|---|-------------------------|--------------------|-----------------|---------------------------------------|
| 1 | `TRAN-TYPE-CD`          | `PIC X(02)`        | Text (2)        | Transaction type code (part of key)   |
| 2 | `TRAN-CAT-CD`           | `PIC 9(04)`        | Numeric (4)     | Transaction category code (part of key)|
| 3 | `TRAN-CAT-TYPE-DESC`    | `PIC X(50)`        | Text (50)       | Category description                  |
| 4 | FILLER                  | `PIC X(04)`        | --              | Reserved space                        |

**Business Rules:**
- Composite Key: Transaction Type + Category
- Sub-classification under Transaction Type
- Used for detailed reporting and interest rate determination

---

## 11. User Security Entity

**Copybook:** `CSUSR01Y` | **Record Length:** ~80 bytes | **VSAM File:** `USRSEC.VSAM.KSDS`

| # | Field Name              | PIC Clause         | Type            | Business Description                  |
|---|-------------------------|--------------------|-----------------|---------------------------------------|
| 1 | `SEC-USR-ID`            | `PIC X(08)`        | Text (8)        | **User ID** (primary key)             |
| 2 | `SEC-USR-FNAME`         | `PIC X(20)`        | Text (20)       | First name                            |
| 3 | `SEC-USR-LNAME`         | `PIC X(20)`        | Text (20)       | Last name                             |
| 4 | `SEC-USR-PWD`           | `PIC X(08)`        | Text (8)        | Password (plain text -- security risk)|
| 5 | `SEC-USR-TYPE`          | `PIC X(01)`        | Text (1)        | User type (A=Admin, U=User)           |
| 6 | FILLER                  | `PIC X(23)`        | --              | Reserved space                        |

**Business Rules:**
- Key: `SEC-USR-ID` (8-character)
- Two roles: Admin (A) and Regular User (U)
- **Security Note:** Passwords stored in plain text -- must be hashed in modernized version
- Used by: Sign-On (COSGN00C), User CRUD (COUSR00C-03C)

---

## 12. Communication Area (COMMAREA)

**Copybook:** `COCOM01Y` | **Size:** ~180 bytes | **Usage:** CICS inter-program communication

| # | Field Name              | PIC Clause         | Type            | Business Description                  |
|---|-------------------------|--------------------|-----------------|---------------------------------------|
| 1 | `CDEMO-FROM-TRANID`     | `PIC X(04)`        | Text (4)        | Originating CICS transaction ID       |
| 2 | `CDEMO-FROM-PROGRAM`    | `PIC X(08)`        | Text (8)        | Originating program name              |
| 3 | `CDEMO-TO-TRANID`       | `PIC X(04)`        | Text (4)        | Target CICS transaction ID            |
| 4 | `CDEMO-TO-PROGRAM`      | `PIC X(08)`        | Text (8)        | Target program name                   |
| 5 | `CDEMO-USER-ID`         | `PIC X(08)`        | Text (8)        | Current user ID                       |
| 6 | `CDEMO-USER-TYPE`       | `PIC X(01)`        | Text (1)        | User type (A=Admin, U=User)           |
| 7 | `CDEMO-PGM-CONTEXT`     | `PIC 9(01)`        | Numeric (1)     | 0=Enter, 1=Re-enter                   |
| 8 | `CDEMO-CUST-ID`         | `PIC 9(09)`        | Numeric (9)     | Selected customer ID                  |
| 9 | `CDEMO-CUST-FNAME`      | `PIC X(25)`        | Text (25)       | Customer first name (display)         |
| 10| `CDEMO-CUST-MNAME`      | `PIC X(25)`        | Text (25)       | Customer middle name (display)        |
| 11| `CDEMO-CUST-LNAME`      | `PIC X(25)`        | Text (25)       | Customer last name (display)          |
| 12| `CDEMO-ACCT-ID`         | `PIC 9(11)`        | Numeric (11)    | Selected account ID                   |
| 13| `CDEMO-ACCT-STATUS`     | `PIC X(01)`        | Text (1)        | Account status                        |
| 14| `CDEMO-CARD-NUM`        | `PIC 9(16)`        | Numeric (16)    | Selected card number                  |
| 15| `CDEMO-LAST-MAP`        | `PIC X(7)`         | Text (7)        | Last BMS map displayed                |
| 16| `CDEMO-LAST-MAPSET`     | `PIC X(7)`         | Text (7)        | Last BMS mapset displayed             |

**Business Rules:**
- Passed between all online CICS programs via DFHCOMMAREA
- Acts as session state (no server-side session in CICS)
- Maps to: HTTP session, JWT token claims, or application context in Java

---

## 13. Card Detail (Extended) Entity

**Copybook:** `CVCRD01Y` | **Record Length:** ~550 bytes | **Usage:** Online screen working storage

| # | Field Name              | PIC Clause         | Type            | Business Description                  |
|---|-------------------------|--------------------|-----------------|---------------------------------------|
| 1 | `CCARD-AID`             | `PIC X(01)`        | Text (1)        | Attention identifier (PF key pressed) |
| 2 | `CCARD-LAST-MAP`        | `PIC X(7)`         | Text (7)        | Last map displayed                    |
| 3 | `CCARD-NEXT-MAP`        | `PIC X(7)`         | Text (7)        | Next map to display                   |
| 4 | `CCARD-ACCT-ID`         | `PIC 9(11)`        | Numeric (11)    | Account ID context                    |
| 5 | `CCARD-CARD-NUM`        | `PIC 9(16)`        | Numeric (16)    | Card number context                   |
| 6 | `CCARD-CUST-ID`         | `PIC 9(09)`        | Numeric (9)     | Customer ID context                   |

**Business Rules:**
- Extended working-storage area for Card-related screens
- Maintains navigation state for card browse/view/update operations

---

## 14. Transaction for Statements (Keyed)

**Copybook:** `COSTM01` | **Record Length:** 350 bytes | **VSAM File:** `TRXFL.VSAM.KSDS`

| # | Field Name              | PIC Clause         | Type            | Business Description                  |
|---|-------------------------|--------------------|-----------------|---------------------------------------|
| 1 | `TRNX-CARD-NUM`         | `PIC X(16)`        | Text (16)       | Card number (part of composite key)   |
| 2 | `TRNX-ID`               | `PIC X(16)`        | Text (16)       | Transaction ID (part of composite key)|
| 3 | `TRNX-TYPE-CD`          | `PIC X(02)`        | Text (2)        | Transaction type code                 |
| 4 | `TRNX-CAT-CD`           | `PIC 9(04)`        | Numeric (4)     | Transaction category code             |
| 5 | `TRNX-SOURCE`           | `PIC X(10)`        | Text (10)       | Transaction source                    |
| 6 | `TRNX-DESC`             | `PIC X(100)`       | Text (100)      | Transaction description               |
| 7 | `TRNX-AMT`              | `PIC S9(09)V99`    | Decimal (11,2)  | Transaction amount                    |
| 8 | `TRNX-MERCHANT-ID`      | `PIC 9(09)`        | Numeric (9)     | Merchant identifier                   |
| 9 | `TRNX-MERCHANT-NAME`    | `PIC X(50)`        | Text (50)       | Merchant name                         |
| 10| `TRNX-MERCHANT-CITY`    | `PIC X(50)`        | Text (50)       | Merchant city                         |
| 11| `TRNX-MERCHANT-ZIP`     | `PIC X(10)`        | Text (10)       | Merchant ZIP code                     |
| 12| `TRNX-ORIG-TS`          | `PIC X(26)`        | Text (26)       | Original timestamp                    |
| 13| `TRNX-PROC-TS`          | `PIC X(26)`        | Text (26)       | Processing timestamp                  |
| 14| FILLER                  | `PIC X(20)`        | --              | Reserved space                        |

**Business Rules:**
- Re-keyed copy of transactions with Card Number as primary sort
- Created by CREASTMT job (SORT step) for statement generation
- Composite Key: Card Number + Transaction ID (allows statement grouping by card)

---

## 15. Export/Import Record

**Copybook:** `CVEXPORT` | **Record Length:** Variable | **Usage:** Data migration flat file

| # | Field Name              | PIC Clause         | Type            | Business Description                  |
|---|-------------------------|--------------------|-----------------|---------------------------------------|
| 1 | `EXP-REC-TYPE`          | `PIC X(01)`        | Text (1)        | Record type (C=Customer, A=Account, X=Xref, T=Transaction, D=Card) |
| 2 | `EXP-CUST-*`            | (varies)           | (varies)        | Customer fields (when type=C)         |
| 3 | `EXP-ACCT-*`            | (varies)           | (varies)        | Account fields (when type=A)          |
| 4 | `EXP-CARD-*`            | (varies)           | (varies)        | Card fields (when type=D)             |
| 5 | `EXP-XREF-*`            | (varies)           | (varies)        | Cross-ref fields (when type=X)        |
| 6 | `EXP-TRAN-*`            | (varies)           | (varies)        | Transaction fields (when type=T)      |

**Business Rules:**
- Multi-type record format using record-type discriminator
- Used by CBEXPORT/CBIMPORT for bulk data migration
- Each record type contains full entity fields plus FILLER padding

---

## 16. Report Layout Structures

**Copybook:** `CVTRA07Y` | **Usage:** Print report formatting

| # | Structure Name                | Business Description                    |
|---|-------------------------------|-----------------------------------------|
| 1 | `REPORT-NAME-HEADER`          | Report title, date range                |
| 2 | `TRANSACTION-DETAIL-REPORT`   | Detail line: ID, Account, Type, Amount  |
| 3 | `TRANSACTION-HEADER-1`        | Column headers for detail lines         |
| 4 | `TRANSACTION-HEADER-2`        | Separator line (dashes)                 |
| 5 | `REPORT-PAGE-TOTALS`          | Page subtotal line                      |
| 6 | `REPORT-ACCOUNT-TOTALS`       | Account subtotal line                   |
| 7 | `REPORT-GRAND-TOTALS`         | Grand total line                        |

---

## 17. Authorization Module Entities (Optional)

### 17a. Authorization Summary

**Copybook:** `CIPAUSMY` | **Storage:** IMS DB

| # | Field Name              | PIC Clause         | Type            | Business Description                  |
|---|-------------------------|--------------------|-----------------|---------------------------------------|
| 1 | `PAUS-ACCT-ID`          | `PIC X(11)`        | Text (11)       | Account ID                            |
| 2 | `PAUS-CARD-NUM`         | `PIC X(16)`        | Text (16)       | Card number                           |
| 3 | `PAUS-TRAN-AMT`         | `PIC S9(09)V99`    | Decimal (11,2)  | Authorization amount                  |
| 4 | `PAUS-TRAN-DT`          | `PIC X(10)`        | Text (10)       | Authorization date                    |
| 5 | `PAUS-TRAN-TM`          | `PIC X(08)`        | Text (8)        | Authorization time                    |
| 6 | `PAUS-AUTH-CD`          | `PIC X(06)`        | Text (6)        | Authorization code                    |
| 7 | `PAUS-RSN-CD`           | `PIC X(02)`        | Text (2)        | Reason code                           |
| 8 | `PAUS-FRAUD-IND`        | `PIC X(01)`        | Text (1)        | Fraud indicator                       |

### 17b. Authorization Detail

**Copybook:** `CIPAUDTY` | **Storage:** IMS DB (child segment)

| # | Field Name              | PIC Clause         | Type            | Business Description                  |
|---|-------------------------|--------------------|-----------------|---------------------------------------|
| 1 | `PAUD-SEQ-NUM`          | `PIC 9(04)`        | Numeric (4)     | Sequence number                       |
| 2 | `PAUD-MERCHANT-NAME`    | `PIC X(50)`        | Text (50)       | Merchant name                         |
| 3 | `PAUD-MERCHANT-CITY`    | `PIC X(50)`        | Text (50)       | Merchant city                         |
| 4 | `PAUD-MERCHANT-ZIP`     | `PIC X(10)`        | Text (10)       | Merchant ZIP                          |

---

## 18. Lookup Code Tables

**Copybook:** `CSLKPCDY` (1,318 lines) | **Usage:** In-memory reference data

| Table           | Content                                    | Entries |
|-----------------|--------------------------------------------|---------|
| State Codes     | US state abbreviations and names           | 50+     |
| Country Codes   | Country abbreviations and names            | 200+    |

**Business Rules:**
- Hard-coded lookup tables embedded in COBOL working storage
- In Java: should be externalized to database reference tables or enum classes

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)
    |
    | 1:N via CVACT03Y (Cross-Reference)
    |
    v
Account (CVACT01Y) ---------> Disclosure Group (CVTRA02Y)
    |                              |
    | 1:N                          | defines interest rates
    v                              v
Card (CVACT02Y) ----------> Transaction (CVTRA05Y)
                                |
                                | classified by
                                v
                          Transaction Type (CVTRA03Y)
                                |
                                | sub-classified by
                                v
                          Transaction Category (CVTRA04Y)
                                |
                                | aggregated in
                                v
                          Category Balance (CVTRA01Y)
```

---

## VSAM File Catalog

| # | VSAM Dataset Name                           | Key       | Rec Len | Entity        | Access  |
|---|---------------------------------------------|-----------|---------|---------------|---------|
| 1 | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`       | 11 bytes  | 300     | Account       | KSDS    |
| 2 | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`       | 16 bytes  | 150     | Card          | KSDS    |
| 3 | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`       | 16 bytes  | 50      | Cross-Ref     | KSDS+AIX|
| 4 | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`       | 9 bytes   | 500     | Customer      | KSDS    |
| 5 | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`       | 16 bytes  | 350     | Transaction   | KSDS    |
| 6 | `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`         | 8 bytes   | 80      | User Security | KSDS    |
| 7 | `AWS.M2.CARDDEMO.DALYTRAN.VSAM.KSDS`       | 16 bytes  | 350     | Daily Tran    | KSDS    |
| 8 | `AWS.M2.CARDDEMO.DALYREJS.VSAM.KSDS`       | 16 bytes  | 350     | Rejected Tran | KSDS    |
| 9 | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS`       | 17 bytes  | 50      | Cat Balance   | KSDS    |
| 10| `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`        | 16 bytes  | 50      | Disclosure Grp| KSDS    |
| 11| `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`       | 2 bytes   | 60      | Tran Type     | KSDS    |
| 12| `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`       | 6 bytes   | 60      | Tran Category | KSDS    |
