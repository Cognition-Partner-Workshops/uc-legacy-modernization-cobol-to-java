# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Source**: COBOL Copybooks in `app/cpy/`
> **Purpose**: Business-friendly reference of all data entities, fields, and record layouts

---

## Overview

The CardDemo application stores its data in VSAM KSDS (Key-Sequenced Data Sets) files, with records defined through COBOL copybooks using PIC (Picture) clauses. This dictionary translates those technical layouts into business-readable terms.

### Quick Reference — Key Conversion Rules

| COBOL PIC Clause     | Business Meaning          | Java Equivalent      |
|---------------------|---------------------------|----------------------|
| `PIC X(n)`          | Alphanumeric, n chars     | `String`             |
| `PIC 9(n)`          | Unsigned integer, n digits| `long` / `int`       |
| `PIC S9(n)V99`      | Signed decimal, 2 places  | `BigDecimal`         |
| `PIC S9(n) COMP`    | Binary integer             | `int` / `long`       |
| `PIC 9(n) COMP`     | Binary unsigned integer    | `int` / `long`       |
| `FILLER PIC X(n)`   | Padding / reserved space   | _(not mapped)_       |

---

## 1. Account Master (`CVACT01Y`)

**VSAM File**: ACCTDAT | **Record Length**: 300 bytes | **Key**: Account ID (11 digits)

This is the core account record containing financial balances, credit limits, and account status.

| Field Name               | PIC Clause         | Type           | Length | Business Description                          |
|--------------------------|-------------------|----------------|:------:|-----------------------------------------------|
| `ACCT-ID`                | `9(11)`           | Numeric ID     | 11     | **Primary Key** — Unique account identifier    |
| `ACCT-ACTIVE-STATUS`     | `X(01)`           | Status Code    | 1      | Account status: `Y` = Active, `N` = Closed    |
| `ACCT-CURR-BAL`          | `S9(10)V99`       | Currency       | 12     | Current account balance (signed, 2 decimals)   |
| `ACCT-CREDIT-LIMIT`      | `S9(10)V99`       | Currency       | 12     | Maximum credit limit                           |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99`       | Currency       | 12     | Maximum cash advance credit limit              |
| `ACCT-OPEN-DATE`         | `X(10)`           | Date String    | 10     | Date account was opened (YYYY-MM-DD)           |
| `ACCT-EXPIRAION-DATE`    | `X(10)`           | Date String    | 10     | Account expiration date (YYYY-MM-DD)           |
| `ACCT-REISSUE-DATE`      | `X(10)`           | Date String    | 10     | Last card reissue date (YYYY-MM-DD)            |
| `ACCT-CURR-CYC-CREDIT`   | `S9(10)V99`       | Currency       | 12     | Credits posted in current billing cycle        |
| `ACCT-CURR-CYC-DEBIT`    | `S9(10)V99`       | Currency       | 12     | Debits posted in current billing cycle         |
| `ACCT-ADDR-ZIP`          | `X(10)`           | Postal Code    | 10     | Account holder ZIP code                        |
| `ACCT-GROUP-ID`          | `X(10)`           | Group Code     | 10     | Disclosure/interest rate group                 |
| `ACCT-FICO-CREDIT-SCORE` | `9(03)`           | Score          | 3      | FICO credit score (300–850)                    |
| `FILLER`                 | `X(168)`          | Reserved       | 168    | Future use padding                             |

### Business Rules
- Account balance can be negative (credit) or positive (debit)
- Credit limit and cash credit limit are maximum thresholds
- Cycle credits/debits are reset at statement generation

---

## 2. Credit Card Master (`CVACT02Y`)

**VSAM File**: CARDDAT | **Record Length**: 150 bytes | **Key**: Card Number (16 digits)

| Field Name               | PIC Clause         | Type           | Length | Business Description                          |
|--------------------------|-------------------|----------------|:------:|-----------------------------------------------|
| `CARD-NUM`               | `X(16)`           | Card Number    | 16     | **Primary Key** — 16-digit credit card number  |
| `CARD-ACCT-ID`           | `9(11)`           | Foreign Key    | 11     | Associated account ID (→ ACCTDAT)              |
| `CARD-CVV-CD`            | `9(03)`           | Security Code  | 3      | Card verification value (CVV)                  |
| `CARD-EMBOSSED-NAME`     | `X(50)`           | Name           | 50     | Name embossed on physical card                 |
| `CARD-EXPIRAION-DATE`    | `X(10)`           | Date String    | 10     | Card expiration date (YYYY-MM-DD)              |
| `CARD-ACTIVE-STATUS`     | `X(01)`           | Status Code    | 1      | Card status: `Y` = Active, `N` = Inactive     |
| `FILLER`                 | `X(59)`           | Reserved       | 59     | Future use padding                             |

### Business Rules
- Multiple cards can be linked to one account
- Card number is the unique key; account ID is the foreign key
- CVV is stored as numeric — security consideration for modernization

---

## 3. Customer Master (`CVCUS01Y`)

**VSAM File**: CUSTDAT | **Record Length**: 500 bytes | **Key**: Customer ID (9 digits)

| Field Name               | PIC Clause         | Type           | Length | Business Description                          |
|--------------------------|-------------------|----------------|:------:|-----------------------------------------------|
| `CUST-ID`                | `9(09)`           | Numeric ID     | 9      | **Primary Key** — Unique customer identifier   |
| `CUST-FIRST-NAME`        | `X(25)`           | Name           | 25     | Customer first name                            |
| `CUST-MIDDLE-NAME`       | `X(25)`           | Name           | 25     | Customer middle name                           |
| `CUST-LAST-NAME`         | `X(25)`           | Name           | 25     | Customer last name                             |
| `CUST-ADDR-LINE-1`       | `X(50)`           | Address        | 50     | Street address line 1                          |
| `CUST-ADDR-LINE-2`       | `X(50)`           | Address        | 50     | Street address line 2                          |
| `CUST-ADDR-LINE-3`       | `X(50)`           | Address        | 50     | Street address line 3                          |
| `CUST-ADDR-STATE-CD`     | `X(02)`           | State Code     | 2      | US state code (e.g., NY, CA)                   |
| `CUST-ADDR-COUNTRY-CD`   | `X(03)`           | Country Code   | 3      | Country code (e.g., USA)                       |
| `CUST-ADDR-ZIP`          | `X(10)`           | Postal Code    | 10     | ZIP/postal code                                |
| `CUST-PHONE-NUM-1`       | `X(15)`           | Phone          | 15     | Primary phone number                           |
| `CUST-PHONE-NUM-2`       | `X(15)`           | Phone          | 15     | Secondary phone number                         |
| `CUST-SSN`               | `9(09)`           | SSN            | 9      | Social Security Number                         |
| `CUST-GOVT-ISSUED-ID`    | `X(20)`           | Gov't ID       | 20     | Government-issued ID number                    |
| `CUST-DOB-YYYYMMDD`      | `X(10)`           | Date String    | 10     | Date of birth (YYYY-MM-DD)                     |
| `CUST-EFT-ACCOUNT-ID`    | `X(10)`           | Bank Acct      | 10     | EFT/ACH bank account number                    |
| `CUST-PRI-CARD-HOLDER-IND`| `X(01)`          | Indicator      | 1      | Primary cardholder: `Y`/`N`                    |
| `CUST-FICO-CREDIT-SCORE` | `9(03)`           | Score          | 3      | FICO credit score (300–850)                    |
| `FILLER`                 | `X(168)`          | Reserved       | 168    | Future use padding                             |

### Business Rules
- One customer can hold multiple accounts
- SSN is stored as numeric — PII consideration for modernization
- FICO score is maintained at both customer and account level

---

## 4. Card-Account Cross-Reference (`CVACT03Y`)

**VSAM File**: CARDXREF / CXACAIX | **Record Length**: 50 bytes | **Key**: Card Number (16 digits)

| Field Name               | PIC Clause         | Type           | Length | Business Description                          |
|--------------------------|-------------------|----------------|:------:|-----------------------------------------------|
| `XREF-CARD-NUM`          | `X(16)`           | Card Number    | 16     | **Primary Key** — Credit card number           |
| `XREF-CUST-ID`           | `9(09)`           | Foreign Key    | 9      | Customer ID (→ CUSTDAT)                        |
| `XREF-ACCT-ID`           | `9(11)`           | Foreign Key    | 11     | Account ID (→ ACCTDAT)                         |
| `FILLER`                 | `X(14)`           | Reserved       | 14     | Future use padding                             |

### Business Rules
- Maps cards → customers → accounts (the central join table)
- Accessed via alternate index (CXACAIX) to look up by account ID
- Critical for account view, bill payment, and transaction posting

---

## 5. Transaction Master (`CVTRA05Y`)

**VSAM File**: TRANSACT | **Record Length**: 350 bytes | **Key**: Transaction ID (16 chars)

| Field Name               | PIC Clause         | Type           | Length | Business Description                          |
|--------------------------|-------------------|----------------|:------:|-----------------------------------------------|
| `TRAN-ID`                | `X(16)`           | Transaction ID | 16     | **Primary Key** — Unique transaction identifier|
| `TRAN-TYPE-CD`           | `X(02)`           | Type Code      | 2      | Transaction type (→ TRANTYPE)                  |
| `TRAN-CAT-CD`            | `9(04)`           | Category Code  | 4      | Transaction category (→ TRANCATG)              |
| `TRAN-SOURCE`            | `X(10)`           | Source Code    | 10     | Origin channel (POS, ATM, ONLINE, etc.)        |
| `TRAN-DESC`              | `X(100)`          | Description    | 100    | Free-text transaction description              |
| `TRAN-AMT`               | `S9(09)V99`       | Currency       | 11     | Transaction amount (signed, 2 decimals)        |
| `TRAN-MERCHANT-ID`       | `9(09)`           | Merchant ID    | 9      | Merchant identifier                            |
| `TRAN-MERCHANT-NAME`     | `X(50)`           | Merchant Name  | 50     | Merchant business name                         |
| `TRAN-MERCHANT-CITY`     | `X(50)`           | City           | 50     | Merchant city                                  |
| `TRAN-MERCHANT-ZIP`      | `X(10)`           | Postal Code    | 10     | Merchant ZIP code                              |
| `TRAN-CARD-NUM`          | `X(16)`           | Card Number    | 16     | Card used for this transaction                 |
| `TRAN-ORIG-TS`           | `X(26)`           | Timestamp      | 26     | Original transaction timestamp                 |
| `TRAN-PROC-TS`           | `X(26)`           | Timestamp      | 26     | Processing/posting timestamp                   |
| `FILLER`                 | `X(20)`           | Reserved       | 20     | Future use padding                             |

### Business Rules
- Negative amounts represent credits/refunds
- Transaction flows through daily staging before posting to master
- Merchant info is denormalized into each transaction record

---

## 6. Daily Transaction (`CVTRA06Y`)

**File**: DALYTRAN (Sequential) | **Record Length**: 350 bytes

Identical structure to Transaction Master (`CVTRA05Y`) with prefix `DALYTRAN-` instead of `TRAN-`. This is the daily staging file that holds unposted transactions before the batch posting cycle.

| Field Name               | PIC Clause         | Business Description                          |
|--------------------------|-------------------|-----------------------------------------------|
| `DALYTRAN-ID`            | `X(16)`           | Daily transaction identifier                   |
| `DALYTRAN-TYPE-CD`       | `X(02)`           | Transaction type code                          |
| `DALYTRAN-CAT-CD`        | `9(04)`           | Transaction category code                      |
| `DALYTRAN-SOURCE`        | `X(10)`           | Origin channel                                 |
| `DALYTRAN-DESC`          | `X(100)`          | Transaction description                        |
| `DALYTRAN-AMT`           | `S9(09)V99`       | Transaction amount                             |
| `DALYTRAN-MERCHANT-ID`   | `9(09)`           | Merchant identifier                            |
| `DALYTRAN-MERCHANT-NAME` | `X(50)`           | Merchant name                                  |
| `DALYTRAN-MERCHANT-CITY` | `X(50)`           | Merchant city                                  |
| `DALYTRAN-MERCHANT-ZIP`  | `X(10)`           | Merchant ZIP code                              |
| `DALYTRAN-CARD-NUM`      | `X(16)`           | Card number used                               |
| `DALYTRAN-ORIG-TS`       | `X(26)`           | Original timestamp                             |
| `DALYTRAN-PROC-TS`       | `X(26)`           | Processing timestamp                           |
| `FILLER`                 | `X(20)`           | Reserved                                       |

---

## 7. Transaction Category Balance (`CVTRA01Y`)

**VSAM File**: TCATBALF | **Record Length**: 50 bytes | **Key**: Account ID + Type + Category

| Field Name               | PIC Clause         | Type           | Length | Business Description                          |
|--------------------------|-------------------|----------------|:------:|-----------------------------------------------|
| `TRANCAT-ACCT-ID`        | `9(11)`           | Composite Key  | 11     | Account identifier                             |
| `TRANCAT-TYPE-CD`        | `X(02)`           | Composite Key  | 2      | Transaction type code                          |
| `TRANCAT-CD`             | `9(04)`           | Composite Key  | 4      | Transaction category code                      |
| `TRAN-CAT-BAL`           | `S9(09)V99`       | Currency       | 11     | Running balance for this category              |
| `FILLER`                 | `X(22)`           | Reserved       | 22     | Future use padding                             |

---

## 8. Disclosure Group / Interest Rate (`CVTRA02Y`)

**VSAM File**: DISCGRP | **Record Length**: 50 bytes | **Key**: Group ID + Type + Category

| Field Name               | PIC Clause         | Type           | Length | Business Description                          |
|--------------------------|-------------------|----------------|:------:|-----------------------------------------------|
| `DIS-ACCT-GROUP-ID`      | `X(10)`           | Composite Key  | 10     | Account group identifier                       |
| `DIS-TRAN-TYPE-CD`       | `X(02)`           | Composite Key  | 2      | Transaction type code                          |
| `DIS-TRAN-CAT-CD`        | `9(04)`           | Composite Key  | 4      | Transaction category code                      |
| `DIS-INT-RATE`           | `S9(04)V99`       | Percentage     | 6      | Interest rate for this group/type/category     |
| `FILLER`                 | `X(28)`           | Reserved       | 28     | Future use padding                             |

### Business Rules
- Links account groups to interest rates by transaction type
- Used by CBACT04C (interest calculation) to determine applicable rates

---

## 9. Transaction Type (`CVTRA03Y`)

**VSAM File**: TRANTYPE | **Record Length**: 60 bytes | **Key**: Type Code (2 chars)

| Field Name               | PIC Clause         | Type           | Length | Business Description                          |
|--------------------------|-------------------|----------------|:------:|-----------------------------------------------|
| `TRAN-TYPE`              | `X(02)`           | Type Code      | 2      | **Primary Key** — Transaction type code        |
| `TRAN-TYPE-DESC`         | `X(50)`           | Description    | 50     | Human-readable type description                |
| `FILLER`                 | `X(08)`           | Reserved       | 8      | Future use padding                             |

---

## 10. Transaction Category (`CVTRA04Y`)

**VSAM File**: TRANCATG | **Record Length**: 60 bytes | **Key**: Type Code + Category Code

| Field Name               | PIC Clause         | Type           | Length | Business Description                          |
|--------------------------|-------------------|----------------|:------:|-----------------------------------------------|
| `TRAN-TYPE-CD`           | `X(02)`           | Composite Key  | 2      | Transaction type code                          |
| `TRAN-CAT-CD`            | `9(04)`           | Composite Key  | 4      | Transaction category code                      |
| `TRAN-CAT-TYPE-DESC`     | `X(50)`           | Description    | 50     | Category description                           |
| `FILLER`                 | `X(04)`           | Reserved       | 4      | Future use padding                             |

---

## 11. User Security Record (`CSUSR01Y`)

**VSAM File**: USRSEC | **Record Length**: 80 bytes | **Key**: User ID (8 chars)

| Field Name               | PIC Clause         | Type           | Length | Business Description                          |
|--------------------------|-------------------|----------------|:------:|-----------------------------------------------|
| `SEC-USR-ID`             | `X(08)`           | User ID        | 8      | **Primary Key** — Login user identifier        |
| `SEC-USR-FNAME`          | `X(20)`           | Name           | 20     | User first name                                |
| `SEC-USR-LNAME`          | `X(20)`           | Name           | 20     | User last name                                 |
| `SEC-USR-PWD`            | `X(08)`           | Password       | 8      | User password (plain text — security risk)     |
| `SEC-USR-TYPE`           | `X(01)`           | Role Code      | 1      | User type: `A` = Admin, `U` = Regular User     |
| `FILLER`                 | `X(23)`           | Reserved       | 23     | Future use padding                             |

### Business Rules
- Two user types control menu access: Admin sees User Management, Regular sees Card Operations
- Password stored in plain text — must be hashed in modernized version
- Default accounts: `ADMIN001`/`PASSWORD` (Admin), `USER0001`/`PASSWORD` (User)

---

## 12. Application COMMAREA (`COCOM01Y`)

**Not a file** — This is the inter-program communication area passed between CICS programs.

| Field Name               | PIC Clause         | Type           | Length | Business Description                          |
|--------------------------|-------------------|----------------|:------:|-----------------------------------------------|
| `CDEMO-FROM-TRANID`      | `X(04)`           | Trans ID       | 4      | Calling transaction ID                         |
| `CDEMO-FROM-PROGRAM`     | `X(08)`           | Program ID     | 8      | Calling program name                           |
| `CDEMO-TO-TRANID`        | `X(04)`           | Trans ID       | 4      | Target transaction ID                          |
| `CDEMO-TO-PROGRAM`       | `X(08)`           | Program ID     | 8      | Target program name                            |
| `CDEMO-USER-ID`          | `X(08)`           | User ID        | 8      | Current logged-in user                         |
| `CDEMO-USER-TYPE`        | `X(01)`           | Role Code      | 1      | `A` = Admin, `U` = User                       |
| `CDEMO-PGM-CONTEXT`      | `9(01)`           | Context Flag   | 1      | `0` = Enter, `1` = Re-enter                   |
| `CDEMO-CUST-ID`          | `9(09)`           | Customer ID    | 9      | Selected customer ID                           |
| `CDEMO-CUST-FNAME`       | `X(25)`           | Name           | 25     | Customer first name (cached)                   |
| `CDEMO-CUST-MNAME`       | `X(25)`           | Name           | 25     | Customer middle name (cached)                  |
| `CDEMO-CUST-LNAME`       | `X(25)`           | Name           | 25     | Customer last name (cached)                    |
| `CDEMO-ACCT-ID`          | `9(11)`           | Account ID     | 11     | Selected account ID                            |
| `CDEMO-ACCT-STATUS`      | `X(01)`           | Status Code    | 1      | Account status flag                            |
| `CDEMO-CARD-NUM`         | `9(16)`           | Card Number    | 16     | Selected card number                           |
| `CDEMO-LAST-MAP`         | `X(07)`           | Map Name       | 7      | Last BMS map displayed                         |
| `CDEMO-LAST-MAPSET`      | `X(07)`           | Mapset Name    | 7      | Last BMS mapset used                           |

### Modernization Note
This COMMAREA maps to a **Session/Context Object** in Java — it carries user state, navigation context, and selected entity IDs between screen interactions.

---

## 13. Export Record (`CVEXPORT`)

**File**: Sequential export file | **Record Types**: Header, Account, Card, Customer, Transaction

The export file uses a discriminator field (`EXPORT-RECORD-TYPE`) to multiplex different record types into a single sequential file for branch migration.

| Record Type | Code | Key Fields Exported                                    |
|------------|:----:|--------------------------------------------------------|
| Header     | `H`  | Export timestamp, record counts                         |
| Account    | `A`  | Full account record (CVACT01Y fields)                   |
| Card       | `C`  | Full card record (CVACT02Y fields)                      |
| Customer   | `U`  | Full customer record (CVCUS01Y fields)                  |
| Transaction| `T`  | Full transaction record (CVTRA05Y fields)               |

---

## 14. Statement Transaction Layout (`COSTM01`)

**Used by**: CBSTM03A (Statement Generation)

| Field Name               | PIC Clause         | Business Description                          |
|--------------------------|-------------------|-----------------------------------------------|
| `TRNX-CARD-NUM`          | `X(16)`           | Card number (part of composite key)            |
| `TRNX-ID`                | `X(16)`           | Transaction ID (part of composite key)         |
| `TRNX-TYPE-CD`           | `X(02)`           | Transaction type code                          |
| `TRNX-CAT-CD`            | `9(04)`           | Transaction category code                      |
| `TRNX-SOURCE`            | `X(10)`           | Transaction source                             |
| `TRNX-DESC`              | `X(100)`          | Transaction description                        |
| `TRNX-AMT`               | `S9(09)V99`       | Transaction amount                             |
| `TRNX-MERCHANT-ID`       | `9(09)`           | Merchant ID                                    |
| `TRNX-MERCHANT-NAME`     | `X(50)`           | Merchant name                                  |
| `TRNX-MERCHANT-CITY`     | `X(50)`           | Merchant city                                  |
| `TRNX-MERCHANT-ZIP`      | `X(10)`           | Merchant ZIP code                              |
| `TRNX-ORIG-TS`           | `X(26)`           | Original timestamp                             |
| `TRNX-PROC-TS`           | `X(26)`           | Processing timestamp                           |

---

## Entity Relationship Summary

```
  CUSTOMER (CUSTDAT)          USER SECURITY (USRSEC)
  ┌──────────────┐            ┌──────────────┐
  │ CUST-ID (PK) │            │ USR-ID (PK)  │
  │ Name, Addr   │            │ Name, Pwd    │
  │ SSN, DOB     │            │ USR-TYPE     │
  │ FICO Score   │            │ (A/U)        │
  └──────┬───────┘            └──────────────┘
         │ 1:N
         ▼
  CROSS-REFERENCE (CARDXREF)
  ┌──────────────────┐
  │ CARD-NUM (PK)    │──────┐
  │ CUST-ID (FK)     │      │
  │ ACCT-ID (FK)     │      │
  └──────────────────┘      │
         │                   │
    1:1  │              N:1  │
         ▼                   ▼
  CREDIT CARD (CARDDAT)    ACCOUNT (ACCTDAT)
  ┌──────────────────┐    ┌──────────────────┐
  │ CARD-NUM (PK)    │    │ ACCT-ID (PK)     │
  │ ACCT-ID (FK)     │    │ Status, Balances │
  │ CVV, Name        │    │ Credit Limits    │
  │ Expiry, Status   │    │ Dates, FICO      │
  └──────────────────┘    │ GROUP-ID (FK)    │
                           └────────┬─────────┘
                                    │ 1:N
                                    ▼
                          TRANSACTION (TRANSACT)
                          ┌──────────────────┐
                          │ TRAN-ID (PK)     │
                          │ Type, Category   │
                          │ Amount, Merchant │
                          │ CARD-NUM (FK)    │
                          │ Timestamps       │
                          └────────┬─────────┘
                                   │ N:1
                    ┌──────────────┼──────────────┐
                    ▼              ▼              ▼
             TRAN TYPE       TRAN CATEGORY   TRAN CAT BALANCE
             (TRANTYPE)      (TRANCATG)      (TCATBALF)
             ┌──────────┐   ┌──────────────┐ ┌──────────────┐
             │ TYPE (PK) │   │ TYPE+CAT(PK) │ │ ACCT+TYPE+   │
             │ Desc      │   │ Desc         │ │ CAT (PK)     │
             └──────────┘   └──────────────┘ │ Balance      │
                                              └──────────────┘

  DISCLOSURE GROUP (DISCGRP)
  ┌──────────────────────┐
  │ GROUP+TYPE+CAT (PK)  │
  │ Interest Rate        │
  └──────────────────────┘
  (Linked to Account via ACCT-GROUP-ID)
```

---

## Modernization Mapping Guide

| COBOL Entity              | VSAM File  | Suggested Java Class       | Suggested DB Table      |
|--------------------------|-----------|----------------------------|------------------------|
| ACCT-RECORD              | ACCTDAT   | `Account.java`             | `accounts`             |
| CARD-RECORD              | CARDDAT   | `CreditCard.java`          | `credit_cards`         |
| CUSTOMER-RECORD          | CUSTDAT   | `Customer.java`            | `customers`            |
| CARD-XREF-RECORD         | CARDXREF  | _(embedded in JPA relations)_ | _(FK relationships)_  |
| TRAN-RECORD              | TRANSACT  | `Transaction.java`         | `transactions`         |
| DALYTRAN-RECORD          | DALYTRAN  | `DailyTransaction.java`    | `daily_transactions`   |
| TRAN-CAT-BAL-RECORD      | TCATBALF  | `CategoryBalance.java`     | `category_balances`    |
| DIS-GROUP-RECORD         | DISCGRP   | `DisclosureGroup.java`     | `disclosure_groups`    |
| TRAN-TYPE-RECORD         | TRANTYPE  | `TransactionType.java`     | `transaction_types`    |
| TRAN-CAT-RECORD          | TRANCATG  | `TransactionCategory.java` | `transaction_categories`|
| SEC-USER-DATA            | USRSEC    | `User.java`                | `users`                |
| CARDDEMO-COMMAREA        | _(memory)_| `SessionContext.java`      | _(HTTP session/JWT)_   |

### Key PII Fields Requiring Special Handling

| Field             | Entity    | Risk Level | Recommendation                       |
|------------------|-----------|:----------:|--------------------------------------|
| `SEC-USR-PWD`    | User      | **Critical**| Hash with bcrypt; never store plain text |
| `CUST-SSN`       | Customer  | **Critical**| Encrypt at rest; mask in UI (***-**-1234) |
| `CARD-NUM`       | Card      | **High**    | PCI-DSS tokenization required         |
| `CARD-CVV-CD`    | Card      | **High**    | Do not persist; verify and discard    |
| `CUST-DOB`       | Customer  | **Medium**  | Access-control restricted             |
