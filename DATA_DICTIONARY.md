# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo | **Source:** COBOL Copybook PIC Clause Analysis
>
> This document translates every COBOL copybook record layout into a business-friendly data dictionary, mapping mainframe PIC clauses to logical data types, sizes, and business meanings.

---

## Table of Contents

1. [PIC Clause Quick Reference](#pic-clause-quick-reference)
2. [Customer Entity](#1-customer-entity)
3. [Account Entity](#2-account-entity)
4. [Credit Card Entity](#3-credit-card-entity)
5. [Card Cross-Reference Entity](#4-card-cross-reference-entity)
6. [Transaction Entity](#5-transaction-entity)
7. [Daily Transaction Entity](#6-daily-transaction-entity)
8. [User Security Entity](#7-user-security-entity)
9. [Transaction Category Balance Entity](#8-transaction-category-balance-entity)
10. [Disclosure Group Entity](#9-disclosure-group-entity)
11. [Transaction Type Entity](#10-transaction-type-entity)
12. [Transaction Category Entity](#11-transaction-category-entity)
13. [Application Communication Area](#12-application-communication-area-commarea)
14. [Export Record (Multi-Entity)](#13-export-record-multi-entity)
15. [Reporting Structures](#14-reporting-structures)
16. [Entity Relationship Summary](#entity-relationship-summary)
17. [VSAM File Catalog](#vsam-file-catalog)

---

## PIC Clause Quick Reference

For readers unfamiliar with COBOL data types:

| COBOL PIC Clause       | Logical Type       | Java Equivalent       | Description                                |
|------------------------|--------------------|-----------------------|--------------------------------------------|
| `PIC X(n)`             | String (n chars)   | `String`              | Alphanumeric, fixed length                 |
| `PIC 9(n)`             | Unsigned integer   | `long` / `int`        | Numeric, n digits, no sign                 |
| `PIC S9(n)`            | Signed integer     | `long` / `int`        | Numeric with sign                          |
| `PIC S9(n)V99`         | Signed decimal     | `BigDecimal`          | Signed with 2 implied decimal places       |
| `PIC S9(n)V99 COMP-3`  | Packed decimal     | `BigDecimal`          | BCD-encoded signed decimal                 |
| `PIC 9(n) COMP`        | Binary integer     | `int` / `long`        | Binary representation                      |
| `PIC S9(n) COMP`       | Signed binary      | `int` / `long`        | Signed binary representation               |
| `FILLER PIC X(n)`      | Reserved space     | (not mapped)          | Padding for fixed-length records           |

---

## 1. Customer Entity

> **Copybook:** `CVCUS01Y.cpy` (primary) / `CUSTREC.cpy` (alternate batch layout)
> **VSAM File:** `CUSTDAT` (KSDS) | **Record Length:** 500 bytes | **Key:** CUST-ID (9 digits)

| # | Field Name               | PIC Clause    | Type           | Size  | Business Name            | Description / Rules                           |
|---|--------------------------|---------------|----------------|-------|--------------------------|-----------------------------------------------|
| 1 | CUST-ID                  | 9(09)         | Integer        | 9     | Customer ID              | Unique customer identifier (primary key)      |
| 2 | CUST-FIRST-NAME          | X(25)         | String         | 25    | First Name               | Customer's given name                         |
| 3 | CUST-MIDDLE-NAME         | X(25)         | String         | 25    | Middle Name              | Customer's middle name (optional)             |
| 4 | CUST-LAST-NAME           | X(25)         | String         | 25    | Last Name                | Customer's surname                            |
| 5 | CUST-ADDR-LINE-1         | X(50)         | String         | 50    | Address Line 1           | Primary street address                        |
| 6 | CUST-ADDR-LINE-2         | X(50)         | String         | 50    | Address Line 2           | Secondary address (apt, suite, etc.)          |
| 7 | CUST-ADDR-LINE-3         | X(50)         | String         | 50    | Address Line 3           | Tertiary address line                         |
| 8 | CUST-ADDR-STATE-CD       | X(02)         | String         | 2     | State Code               | US state abbreviation (e.g., "CA", "NY")      |
| 9 | CUST-ADDR-COUNTRY-CD     | X(03)         | String         | 3     | Country Code             | ISO country code (e.g., "USA")                |
| 10| CUST-ADDR-ZIP            | X(10)         | String         | 10    | ZIP Code                 | Postal code (supports ZIP+4)                  |
| 11| CUST-PHONE-NUM-1         | X(15)         | String         | 15    | Primary Phone            | Format: (999)999-9999 plus extension          |
| 12| CUST-PHONE-NUM-2         | X(15)         | String         | 15    | Secondary Phone          | Alternate contact number                      |
| 13| CUST-SSN                 | 9(09)         | Integer        | 9     | Social Security Number   | SSN for identity verification (PII)           |
| 14| CUST-GOVT-ISSUED-ID      | X(20)         | String         | 20    | Government ID            | Driver's license or other govt-issued ID      |
| 15| CUST-DOB-YYYY-MM-DD      | X(10)         | Date (String)  | 10    | Date of Birth            | Format: YYYY-MM-DD                            |
| 16| CUST-EFT-ACCOUNT-ID      | X(10)         | String         | 10    | EFT Account ID           | Electronic funds transfer account link        |
| 17| CUST-PRI-CARD-HOLDER-IND | X(01)         | String         | 1     | Primary Cardholder Flag  | "Y" = primary holder, "N" = authorized user   |
| 18| CUST-FICO-CREDIT-SCORE   | 9(03)         | Integer        | 3     | FICO Credit Score        | Range: 300-850                                |
| 19| FILLER                   | X(168)        | Reserved       | 168   | (Padding)                | Future expansion space                        |

**Business Rules:**
- SSN validated with 3-part structure (XXX-XX-XXXX); part 1 excludes 000, 666, 900-999
- Phone numbers validated as US format: (area)prefix-number
- State code validated against US state lookup table (CSLKPCDY copybook contains 1,000+ entries)

---

## 2. Account Entity

> **Copybook:** `CVACT01Y.cpy` | **VSAM File:** `ACCTDAT` (KSDS) | **Record Length:** 300 bytes | **Key:** ACCT-ID (11 digits)

| # | Field Name               | PIC Clause       | Type           | Size  | Business Name            | Description / Rules                        |
|---|--------------------------|------------------|----------------|-------|--------------------------|--------------------------------------------|
| 1 | ACCT-ID                  | 9(11)            | Integer        | 11    | Account Number           | Unique account identifier (primary key)    |
| 2 | ACCT-ACTIVE-STATUS       | X(01)            | String         | 1     | Account Status           | "Y" = Active, "N" = Inactive/Closed        |
| 3 | ACCT-CURR-BAL            | S9(10)V99        | Decimal        | 12.2  | Current Balance          | Signed; negative = credit balance           |
| 4 | ACCT-CREDIT-LIMIT        | S9(10)V99        | Decimal        | 12.2  | Credit Limit             | Maximum allowed credit balance              |
| 5 | ACCT-CASH-CREDIT-LIMIT   | S9(10)V99        | Decimal        | 12.2  | Cash Advance Limit       | Maximum cash advance allowed                |
| 6 | ACCT-OPEN-DATE           | X(10)            | Date (String)  | 10    | Account Open Date        | Format: YYYY-MM-DD                         |
| 7 | ACCT-EXPIRAION-DATE      | X(10)            | Date (String)  | 10    | Expiration Date          | Account/card expiration                     |
| 8 | ACCT-REISSUE-DATE        | X(10)            | Date (String)  | 10    | Reissue Date             | Last card reissue date                      |
| 9 | ACCT-CURR-CYC-CREDIT     | S9(10)V99        | Decimal        | 12.2  | Cycle Credits            | Total credits in current billing cycle      |
| 10| ACCT-CURR-CYC-DEBIT      | S9(10)V99        | Decimal        | 12.2  | Cycle Debits             | Total debits in current billing cycle       |
| 11| ACCT-ADDR-ZIP            | X(10)            | String         | 10    | Billing ZIP Code         | Account billing address ZIP                 |
| 12| ACCT-GROUP-ID            | X(10)            | String         | 10    | Disclosure Group ID      | Links to interest rate disclosure group     |
| 13| FILLER                   | X(178)           | Reserved       | 178   | (Padding)                | Future expansion space                      |

**Business Rules:**
- Balance = previous balance + cycle debits - cycle credits + interest
- Credit limit and cash limit validated as positive non-zero amounts
- All dates validated for calendar correctness via CSUTLDTC utility
- Account group ID links to disclosure group for interest rate determination

---

## 3. Credit Card Entity

> **Copybook:** `CVACT02Y.cpy` | **VSAM File:** `CARDDAT` (KSDS) | **Record Length:** 150 bytes | **Key:** CARD-NUM (16 chars)

| # | Field Name               | PIC Clause    | Type           | Size  | Business Name            | Description / Rules                        |
|---|--------------------------|---------------|----------------|-------|--------------------------|--------------------------------------------|
| 1 | CARD-NUM                 | X(16)         | String         | 16    | Card Number              | 16-digit credit card number (primary key)  |
| 2 | CARD-ACCT-ID             | 9(11)         | Integer        | 11    | Account Number           | FK to Account entity                       |
| 3 | CARD-CVV-CD              | 9(03)         | Integer        | 3     | CVV Code                 | 3-digit card verification value            |
| 4 | CARD-EMBOSSED-NAME       | X(50)         | String         | 50    | Embossed Name            | Name printed on the physical card          |
| 5 | CARD-EXPIRAION-DATE      | X(10)         | Date (String)  | 10    | Card Expiration Date     | Format: YYYY-MM-DD                         |
| 6 | CARD-ACTIVE-STATUS       | X(01)         | String         | 1     | Card Status              | "Y" = Active, "N" = Inactive/Cancelled     |
| 7 | FILLER                   | X(59)         | Reserved       | 59    | (Padding)                | Future expansion space                     |

**Business Rules:**
- Multiple cards can exist per account (one-to-many relationship)
- Card status change triggers through COCRDUPC (card update program)
- Alternate index path `CARDAIX` allows lookup by account ID

---

## 4. Card Cross-Reference Entity

> **Copybook:** `CVACT03Y.cpy` | **VSAM File:** `CARDXREF` (KSDS) | **Record Length:** 50 bytes | **Key:** XREF-CARD-NUM (16 chars)

| # | Field Name               | PIC Clause    | Type           | Size  | Business Name            | Description / Rules                        |
|---|--------------------------|---------------|----------------|-------|--------------------------|--------------------------------------------|
| 1 | XREF-CARD-NUM            | X(16)         | String         | 16    | Card Number              | FK to Card entity (primary key)            |
| 2 | XREF-CUST-ID             | 9(09)         | Integer        | 9     | Customer ID              | FK to Customer entity                      |
| 3 | XREF-ACCT-ID             | 9(11)         | Integer        | 11    | Account Number           | FK to Account entity                       |
| 4 | FILLER                   | X(14)         | Reserved       | 14    | (Padding)                | Future expansion space                     |

**Business Rules:**
- This is the central hub linking Cards ↔ Customers ↔ Accounts
- Alternate index path `CXACAIX` allows lookup by account ID
- Used by nearly every online program to navigate between entities

---

## 5. Transaction Entity

> **Copybook:** `CVTRA05Y.cpy` | **VSAM File:** `TRANSACT` (KSDS) | **Record Length:** 350 bytes | **Key:** TRAN-ID (16 chars)

| # | Field Name               | PIC Clause    | Type           | Size  | Business Name            | Description / Rules                        |
|---|--------------------------|---------------|----------------|-------|--------------------------|--------------------------------------------|
| 1 | TRAN-ID                  | X(16)         | String         | 16    | Transaction ID           | Unique transaction identifier (primary key)|
| 2 | TRAN-TYPE-CD             | X(02)         | String         | 2     | Transaction Type Code    | FK to Transaction Type (e.g., "SA"=Sale)   |
| 3 | TRAN-CAT-CD              | 9(04)         | Integer        | 4     | Category Code            | FK to Transaction Category                 |
| 4 | TRAN-SOURCE              | X(10)         | String         | 10    | Transaction Source       | Origin channel (POS, ATM, ONLINE, etc.)    |
| 5 | TRAN-DESC                | X(100)        | String         | 100   | Description              | Free-text transaction description          |
| 6 | TRAN-AMT                 | S9(09)V99     | Decimal        | 11.2  | Amount                   | Signed amount (negative = refund/credit)   |
| 7 | TRAN-MERCHANT-ID         | 9(09)         | Integer        | 9     | Merchant ID              | Merchant identifier                        |
| 8 | TRAN-MERCHANT-NAME       | X(50)         | String         | 50    | Merchant Name            | Business name of merchant                  |
| 9 | TRAN-MERCHANT-CITY       | X(50)         | String         | 50    | Merchant City            | City where transaction occurred            |
| 10| TRAN-MERCHANT-ZIP        | X(10)         | String         | 10    | Merchant ZIP             | Merchant postal code                       |
| 11| TRAN-CARD-NUM            | X(16)         | String         | 16    | Card Number              | FK to Card entity (card used)              |
| 12| TRAN-ORIG-TS             | X(26)         | Timestamp      | 26    | Origination Timestamp    | When transaction was initiated             |
| 13| TRAN-PROC-TS             | X(26)         | Timestamp      | 26    | Processing Timestamp     | When transaction was posted                |
| 14| FILLER                   | X(20)         | Reserved       | 20    | (Padding)                | Future expansion space                     |

**Business Rules:**
- Transaction type + category code together determine the classification and applicable interest rate
- Transaction posting (CBTRN02C) validates against cross-reference file, updates account balance, and writes to category balance
- Negative amounts represent credits/refunds
- Timestamps in ISO 8601 format

---

## 6. Daily Transaction Entity

> **Copybook:** `CVTRA06Y.cpy` | **VSAM File:** `DALYTRAN` (Sequential) | **Record Length:** 350 bytes

| # | Field Name               | PIC Clause    | Type           | Size  | Business Name            | Description / Rules                        |
|---|--------------------------|---------------|----------------|-------|--------------------------|--------------------------------------------|
| 1 | DALYTRAN-ID              | X(16)         | String         | 16    | Transaction ID           | Matches TRAN-ID from Transaction entity    |
| 2 | DALYTRAN-TYPE-CD         | X(02)         | String         | 2     | Transaction Type Code    | Same codes as Transaction entity           |
| 3 | DALYTRAN-CAT-CD          | 9(04)         | Integer        | 4     | Category Code            | Same codes as Transaction entity           |
| 4 | DALYTRAN-SOURCE          | X(10)         | String         | 10    | Transaction Source       | Same as Transaction entity                 |
| 5 | DALYTRAN-DESC            | X(100)        | String         | 100   | Description              | Same as Transaction entity                 |
| 6 | DALYTRAN-AMT             | S9(09)V99     | Decimal        | 11.2  | Amount                   | Same as Transaction entity                 |
| 7 | DALYTRAN-MERCHANT-ID     | 9(09)         | Integer        | 9     | Merchant ID              | Same as Transaction entity                 |
| 8 | DALYTRAN-MERCHANT-NAME   | X(50)         | String         | 50    | Merchant Name            | Same as Transaction entity                 |
| 9 | DALYTRAN-MERCHANT-CITY   | X(50)         | String         | 50    | Merchant City            | Same as Transaction entity                 |
| 10| DALYTRAN-MERCHANT-ZIP    | X(10)         | String         | 10    | Merchant ZIP             | Same as Transaction entity                 |
| 11| DALYTRAN-CARD-NUM        | X(16)         | String         | 16    | Card Number              | Same as Transaction entity                 |
| 12| DALYTRAN-ORIG-TS         | X(26)         | Timestamp      | 26    | Origination Timestamp    | Same as Transaction entity                 |
| 13| DALYTRAN-PROC-TS         | X(26)         | Timestamp      | 26    | Processing Timestamp     | Same as Transaction entity                 |
| 14| FILLER                   | X(20)         | Reserved       | 20    | (Padding)                | Future expansion space                     |

**Business Rules:**
- Identical structure to Transaction entity; represents the daily feed of new transactions
- Input to POSTTRAN (CBTRN02C) batch job for posting into the Transaction master
- Rejected transactions are written to DALYREJS file

---

## 7. User Security Entity

> **Copybook:** `CSUSR01Y.cpy` | **VSAM File:** `USRSEC` (KSDS) | **Record Length:** 80 bytes | **Key:** SEC-USR-ID (8 chars)

| # | Field Name               | PIC Clause    | Type           | Size  | Business Name            | Description / Rules                        |
|---|--------------------------|---------------|----------------|-------|--------------------------|--------------------------------------------|
| 1 | SEC-USR-ID               | X(08)         | String         | 8     | User ID                  | Login user ID (primary key)                |
| 2 | SEC-USR-FNAME            | X(20)         | String         | 20    | First Name               | User's first name                          |
| 3 | SEC-USR-LNAME            | X(20)         | String         | 20    | Last Name                | User's last name                           |
| 4 | SEC-USR-PWD              | X(08)         | String         | 8     | Password                 | Plain-text password (8 char max)           |
| 5 | SEC-USR-TYPE             | X(01)         | String         | 1     | User Type                | "A" = Admin, "U" = Regular User            |
| 6 | SEC-USR-FILLER           | X(23)         | Reserved       | 23    | (Padding)                | Future expansion space                     |

**Business Rules:**
- Admin users access the admin menu (COADM01C); regular users get the main menu (COMEN01C)
- Password stored in plain text (mainframe convention for demo; modernization target)
- Default credentials: ADMIN001/PASSWORD (admin), USER0001/PASSWORD (user)
- CRUD operations through COUSR00C-COUSR03C (admin only)

---

## 8. Transaction Category Balance Entity

> **Copybook:** `CVTRA01Y.cpy` | **VSAM File:** `TCATBALF` (KSDS) | **Record Length:** 50 bytes | **Key:** Composite (ACCT-ID + TYPE-CD + CAT-CD)

| # | Field Name               | PIC Clause    | Type           | Size  | Business Name            | Description / Rules                        |
|---|--------------------------|---------------|----------------|-------|--------------------------|--------------------------------------------|
| 1 | TRANCAT-ACCT-ID          | 9(11)         | Integer        | 11    | Account Number           | FK to Account entity (part of composite key)|
| 2 | TRANCAT-TYPE-CD          | X(02)         | String         | 2     | Transaction Type Code    | FK to Transaction Type (part of key)       |
| 3 | TRANCAT-CD               | 9(04)         | Integer        | 4     | Category Code            | FK to Transaction Category (part of key)   |
| 4 | TRAN-CAT-BAL             | S9(09)V99     | Decimal        | 11.2  | Category Balance         | Running balance for this acct/type/cat     |
| 5 | FILLER                   | X(22)         | Reserved       | 22    | (Padding)                | Future expansion space                     |

**Business Rules:**
- Tracks running balance per account per transaction type/category combination
- Updated during transaction posting (CBTRN02C) and interest calculation (CBACT04C)
- Used to calculate interest charges by applying disclosure group rates

---

## 9. Disclosure Group Entity

> **Copybook:** `CVTRA02Y.cpy` | **VSAM File:** `DISCGRP` (KSDS) | **Record Length:** 50 bytes | **Key:** Composite (GROUP-ID + TYPE-CD + CAT-CD)

| # | Field Name               | PIC Clause    | Type           | Size  | Business Name            | Description / Rules                        |
|---|--------------------------|---------------|----------------|-------|--------------------------|--------------------------------------------|
| 1 | DIS-ACCT-GROUP-ID        | X(10)         | String         | 10    | Disclosure Group ID      | Links from Account ACCT-GROUP-ID           |
| 2 | DIS-TRAN-TYPE-CD         | X(02)         | String         | 2     | Transaction Type Code    | Type of transaction this rate applies to   |
| 3 | DIS-TRAN-CAT-CD          | 9(04)         | Integer        | 4     | Category Code            | Category this rate applies to              |
| 4 | DIS-INT-RATE             | S9(04)V99     | Decimal        | 6.2   | Interest Rate            | Annual interest rate percentage             |
| 5 | FILLER                   | X(28)         | Reserved       | 28    | (Padding)                | Future expansion space                     |

**Business Rules:**
- Each account belongs to a disclosure group (via ACCT-GROUP-ID)
- Interest rate is looked up by group + transaction type + category
- Used by CBACT04C (interest calculation batch) to compute interest charges

---

## 10. Transaction Type Entity

> **Copybook:** `CVTRA03Y.cpy` | **VSAM File:** `TRANTYPE` (KSDS) | **Record Length:** 60 bytes | **Key:** TRAN-TYPE (2 chars)

| # | Field Name               | PIC Clause    | Type           | Size  | Business Name            | Description / Rules                        |
|---|--------------------------|---------------|----------------|-------|--------------------------|--------------------------------------------|
| 1 | TRAN-TYPE                | X(02)         | String         | 2     | Transaction Type Code    | Two-character code (primary key)           |
| 2 | TRAN-TYPE-DESC           | X(50)         | String         | 50    | Type Description         | Human-readable description                 |
| 3 | FILLER                   | X(08)         | Reserved       | 8     | (Padding)                | Future expansion space                     |

**Example Values:** SA = Sale, CR = Credit, CA = Cash Advance, FE = Fee, IN = Interest

---

## 11. Transaction Category Entity

> **Copybook:** `CVTRA04Y.cpy` | **VSAM File:** `TRANCATG` (KSDS) | **Record Length:** 60 bytes | **Key:** Composite (TYPE-CD + CAT-CD)

| # | Field Name               | PIC Clause    | Type           | Size  | Business Name            | Description / Rules                        |
|---|--------------------------|---------------|----------------|-------|--------------------------|--------------------------------------------|
| 1 | TRAN-TYPE-CD             | X(02)         | String         | 2     | Transaction Type Code    | FK to Transaction Type (part of key)       |
| 2 | TRAN-CAT-CD              | 9(04)         | Integer        | 4     | Category Code            | Numeric category (part of key)             |
| 3 | TRAN-CAT-TYPE-DESC       | X(50)         | String         | 50    | Category Description     | Detailed category description              |
| 4 | FILLER                   | X(04)         | Reserved       | 4     | (Padding)                | Future expansion space                     |

**Business Rules:**
- Categories are sub-classifications within a transaction type
- Used in reporting (CBTRN03C) and interest calculation (CBACT04C)

---

## 12. Application Communication Area (COMMAREA)

> **Copybook:** `COCOM01Y.cpy` | **Usage:** Passed between all online CICS programs via XCTL/RETURN

| # | Field Name               | PIC Clause    | Type           | Size  | Business Name            | Description / Rules                        |
|---|--------------------------|---------------|----------------|-------|--------------------------|--------------------------------------------|
| 1 | CDEMO-FROM-TRANID        | X(04)         | String         | 4     | Source Transaction ID    | CICS transaction that initiated call       |
| 2 | CDEMO-FROM-PROGRAM       | X(08)         | String         | 8     | Source Program           | Program that transferred control           |
| 3 | CDEMO-TO-TRANID          | X(04)         | String         | 4     | Target Transaction ID    | Where to return on PF3/exit                |
| 4 | CDEMO-TO-PROGRAM         | X(08)         | String         | 8     | Target Program           | Return-to program name                     |
| 5 | CDEMO-USER-ID            | X(08)         | String         | 8     | Logged-In User ID        | Currently authenticated user               |
| 6 | CDEMO-USER-TYPE          | X(01)         | String         | 1     | User Type                | "A" = Admin, "U" = User                    |
| 7 | CDEMO-PGM-CONTEXT        | 9(01)         | Integer        | 1     | Program Context          | 0 = First entry, 1 = Re-entry              |
| 8 | CDEMO-CUST-ID            | 9(09)         | Integer        | 9     | Selected Customer ID     | Current customer context                   |
| 9 | CDEMO-CUST-FNAME         | X(25)         | String         | 25    | Customer First Name      | Cached for display                         |
| 10| CDEMO-CUST-MNAME         | X(25)         | String         | 25    | Customer Middle Name     | Cached for display                         |
| 11| CDEMO-CUST-LNAME         | X(25)         | String         | 25    | Customer Last Name       | Cached for display                         |
| 12| CDEMO-ACCT-ID            | 9(11)         | Integer        | 11    | Selected Account ID      | Current account context                    |
| 13| CDEMO-ACCT-STATUS        | X(01)         | String         | 1     | Account Status           | Cached account status                      |
| 14| CDEMO-CARD-NUM           | 9(16)         | Integer        | 16    | Selected Card Number     | Current card context                       |
| 15| CDEMO-LAST-MAP           | X(07)         | String         | 7     | Last BMS Map             | For screen navigation tracking             |
| 16| CDEMO-LAST-MAPSET        | X(07)         | String         | 7     | Last BMS Mapset          | For screen navigation tracking             |

**Business Rules:**
- Every CICS program receives and passes this structure via EXEC CICS RETURN COMMAREA
- PGM-CONTEXT distinguishes initial entry (0) from re-entry after user interaction (1)
- User type determines menu access: Admin sees COADM01C menu, User sees COMEN01C menu

---

## 13. Export Record (Multi-Entity)

> **Copybook:** `CVEXPORT.cpy` | **File:** Sequential export file | **Record Length:** 500 bytes

### Header Fields (common to all record types)

| # | Field Name               | PIC Clause    | Type           | Size  | Business Name            |
|---|--------------------------|---------------|----------------|-------|--------------------------|
| 1 | EXPORT-REC-TYPE          | X(1)          | String         | 1     | Record Type Indicator    |
| 2 | EXPORT-TIMESTAMP         | X(26)         | Timestamp      | 26    | Export Timestamp         |
| 3 | EXPORT-SEQUENCE-NUM      | 9(9) COMP     | Binary Int     | 4     | Sequence Number          |
| 4 | EXPORT-BRANCH-ID         | X(4)          | String         | 4     | Branch Identifier        |
| 5 | EXPORT-REGION-CODE       | X(5)          | String         | 5     | Region Code              |

### Record Type Values

| Type Code | Entity       | REDEFINES Section            |
|-----------|-------------|------------------------------|
| C         | Customer    | EXPORT-CUSTOMER-DATA          |
| A         | Account     | EXPORT-ACCOUNT-DATA           |
| T         | Transaction | EXPORT-TRANSACTION-DATA       |
| X         | Card Xref   | EXPORT-CARD-XREF-DATA         |
| D         | Card        | EXPORT-CARD-DATA              |

**Note:** Uses COMP and COMP-3 fields for storage optimization compared to source copybooks.

---

## 14. Reporting Structures

> **Copybook:** `CVTRA07Y.cpy` | **Used by:** CBTRN03C (Daily Transaction Report)

### Report Header
| Field                    | Value/Purpose                              |
|--------------------------|--------------------------------------------|
| REPT-SHORT-NAME          | "DALYREPT" - report identifier             |
| REPT-LONG-NAME           | "Daily Transaction Report"                 |
| REPT-START-DATE          | Report period start (YYYY-MM-DD)           |
| REPT-END-DATE            | Report period end (YYYY-MM-DD)             |

### Transaction Detail Line (133 chars)
| Field                    | Format         | Description                    |
|--------------------------|----------------|--------------------------------|
| TRAN-REPORT-TRANS-ID     | X(16)          | Transaction ID                 |
| TRAN-REPORT-ACCOUNT-ID   | X(11)          | Account Number                 |
| TRAN-REPORT-TYPE-CD      | X(02)          | Type code                      |
| TRAN-REPORT-TYPE-DESC    | X(15)          | Type description               |
| TRAN-REPORT-CAT-CD       | 9(04)          | Category code                  |
| TRAN-REPORT-CAT-DESC     | X(29)          | Category description           |
| TRAN-REPORT-SOURCE        | X(10)          | Source channel                 |
| TRAN-REPORT-AMT          | -ZZZ,ZZZ,ZZZ.ZZ | Formatted amount             |

### Report Totals
| Level                    | Edit Mask              | Description                    |
|--------------------------|------------------------|--------------------------------|
| REPT-PAGE-TOTAL          | +ZZZ,ZZZ,ZZZ.ZZ       | Page subtotal                  |
| REPT-ACCOUNT-TOTAL       | +ZZZ,ZZZ,ZZZ.ZZ       | Account subtotal               |
| REPT-GRAND-TOTAL         | +ZZZ,ZZZ,ZZZ.ZZ       | Grand total all transactions   |

---

## Entity Relationship Summary

```
                    ┌──────────────┐
                    │   CUSTOMER   │
                    │  (CVCUS01Y)  │
                    │  Key: CUST-ID│
                    └──────┬───────┘
                           │ 1
                           │
                           │ M
                    ┌──────┴───────┐
                    │  CARD XREF   │
                    │  (CVACT03Y)  │◄────────────────┐
                    │Key: CARD-NUM │                  │
                    └──┬───────┬───┘                  │
                   1:1 │       │ M:1                  │
                       │       │                      │
              ┌────────┘       └────────┐             │
              ▼                         ▼             │
       ┌──────────────┐         ┌──────────────┐      │
       │    CARD      │         │   ACCOUNT    │      │
       │  (CVACT02Y)  │         │  (CVACT01Y)  │      │
       │Key: CARD-NUM │         │Key: ACCT-ID  │      │
       └──────┬───────┘         └──────┬───────┘      │
              │                        │              │
              │ 1                      │ 1            │
              │                        │              │
              │ M                      │ M            │
       ┌──────┴───────┐         ┌──────┴───────┐      │
       │ TRANSACTION  │         │ TRAN CAT BAL │      │
       │  (CVTRA05Y)  │─────────│  (CVTRA01Y)  │      │
       │Key: TRAN-ID  │         │Key: Composite│      │
       └──────────────┘         └──────────────┘      │
                                       │              │
                                       │              │
                                ┌──────┴───────┐      │
                                │ DISCLOSURE   │      │
                                │   GROUP      │      │
                                │  (CVTRA02Y)  │      │
                                │Key: Composite│      │
                                └──────────────┘      │
                                                      │
       ┌──────────────┐         ┌──────────────┐      │
       │  TRAN TYPE   │         │  TRAN CAT    │      │
       │  (CVTRA03Y)  │────────►│  (CVTRA04Y)  │──────┘
       │Key: TYPE-CD  │         │Key: Composite│
       └──────────────┘         └──────────────┘

       ┌──────────────┐
       │ USER SECURITY│  (Independent entity)
       │  (CSUSR01Y)  │
       │Key: USR-ID   │
       └──────────────┘
```

**Key Relationships:**
- **Customer → Cards:** One customer can have multiple cards (via Cross-Reference)
- **Account → Cards:** One account can have multiple cards (via Cross-Reference)
- **Card → Transactions:** One card can have many transactions
- **Account → Category Balances:** One account has many category balance records
- **Account → Disclosure Group:** Account's GROUP-ID maps to interest rate rules
- **Transaction Type → Categories:** One type has many sub-categories

---

## VSAM File Catalog

| DD Name    | VSAM Type | DSN Pattern                              | Record Layout  | Key Field(s)         | Key Len |
|------------|-----------|------------------------------------------|----------------|----------------------|---------|
| ACCTDAT    | KSDS      | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS      | CVACT01Y       | ACCT-ID              | 11      |
| CARDDAT    | KSDS      | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS      | CVACT02Y       | CARD-NUM             | 16      |
| CARDAIX    | AIX PATH  | AWS.M2.CARDDEMO.CARDDATA.VSAM.AIX.PATH  | CVACT02Y       | CARD-ACCT-ID (alt)   | 11      |
| CUSTDAT    | KSDS      | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS      | CVCUS01Y       | CUST-ID              | 9       |
| CARDXREF   | KSDS      | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS      | CVACT03Y       | XREF-CARD-NUM        | 16      |
| CXACAIX    | AIX PATH  | AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH  | CVACT03Y       | XREF-ACCT-ID (alt)   | 11      |
| TRANSACT   | KSDS      | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS      | CVTRA05Y       | TRAN-ID              | 16      |
| USRSEC     | KSDS      | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS        | CSUSR01Y       | SEC-USR-ID           | 8       |
| TCATBALF   | KSDS      | AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS      | CVTRA01Y       | Composite (17)       | 17      |
| DISCGRP    | KSDS      | AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS       | CVTRA02Y       | Composite (16)       | 16      |
| TRANTYPE   | KSDS      | AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS      | CVTRA03Y       | TRAN-TYPE            | 2       |
| TRANCATG   | KSDS      | AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS      | CVTRA04Y       | Composite (6)        | 6       |
| DALYTRAN   | SEQ/GDG   | AWS.M2.CARDDEMO.DALYTRAN.PS             | CVTRA06Y       | N/A (sequential)     | -       |

### GDG (Generation Data Group) Files

| GDG Base                                    | Purpose                                    |
|---------------------------------------------|--------------------------------------------|
| AWS.M2.CARDDEMO.TRANSACT.BKUP              | Transaction master backups                 |
| AWS.M2.CARDDEMO.TRANSACT.DALY              | Daily transaction extracts                 |
| AWS.M2.CARDDEMO.TRANSACT.COMBINED          | Combined transactions after merge          |
| AWS.M2.CARDDEMO.SYSTRAN                    | System-generated transaction records       |
