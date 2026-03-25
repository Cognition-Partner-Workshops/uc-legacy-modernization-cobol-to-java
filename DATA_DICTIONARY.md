# Data Dictionary - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management)
> **Source:** COBOL Copybooks in `app/cpy/`

---

## Overview

This document extracts business entities from the COBOL copybook PIC clauses and translates them into a business-friendly format. Each entity maps to a VSAM KSDS file on the mainframe and will correspond to a database table in the modernized Java application.

---

## 1. Account Entity

**Source Copybook:** `CVACT01Y.cpy` | **Record Length:** 300 bytes | **VSAM File:** ACCTDAT

| # | Field Name               | COBOL PIC Clause    | Data Type     | Size  | Business Description                     | Nullable | Notes                           |
|---|--------------------------|---------------------|---------------|------:|------------------------------------------|----------|---------------------------------|
| 1 | ACCT-ID                  | PIC 9(11)           | Numeric       |    11 | Unique account identifier                | No       | Primary key                     |
| 2 | ACCT-ACTIVE-STATUS       | PIC X(01)           | Character     |     1 | Account status flag                      | No       | 'Y' = Active, 'N' = Inactive   |
| 3 | ACCT-CURR-BAL            | PIC S9(10)V99       | Signed Decimal|  12,2 | Current account balance                  | No       | Signed; can be negative         |
| 4 | ACCT-CREDIT-LIMIT        | PIC S9(10)V99       | Signed Decimal|  12,2 | Credit limit                             | No       |                                 |
| 5 | ACCT-CASH-CREDIT-LIMIT   | PIC S9(10)V99       | Signed Decimal|  12,2 | Cash advance credit limit                | No       |                                 |
| 6 | ACCT-OPEN-DATE           | PIC X(10)           | Date String   |    10 | Account open date                        | No       | Format: YYYY-MM-DD              |
| 7 | ACCT-EXPIRAION-DATE      | PIC X(10)           | Date String   |    10 | Account expiration date                  | No       | Typo in source ("EXPIRAION")    |
| 8 | ACCT-REISSUE-DATE        | PIC X(10)           | Date String   |    10 | Card reissue date                        | Yes      |                                 |
| 9 | ACCT-CURR-CYC-CREDIT     | PIC S9(10)V99       | Signed Decimal|  12,2 | Current cycle credit total               | No       |                                 |
|10 | ACCT-CURR-CYC-DEBIT      | PIC S9(10)V99       | Signed Decimal|  12,2 | Current cycle debit total                | No       |                                 |
|11 | ACCT-ADDR-ZIP            | PIC X(10)           | Character     |    10 | Account holder ZIP/postal code           | Yes      |                                 |
|12 | ACCT-GROUP-ID            | PIC X(10)           | Character     |    10 | Disclosure/interest rate group           | Yes      | FK to Disclosure Group          |
|13 | FILLER                   | PIC X(178)          | Padding       |   178 | Reserved space                           | ---      | Unused                          |

**Business Rules:**
- Balance can be negative (overpayment scenario)
- Credit limit must be positive
- Expiration date must be after open date
- Group ID links to the disclosure group for interest rate calculation

---

## 2. Card Entity

**Source Copybook:** `CVACT02Y.cpy` | **Record Length:** 150 bytes | **VSAM File:** CARDDAT

| # | Field Name               | COBOL PIC Clause    | Data Type     | Size  | Business Description                     | Nullable | Notes                           |
|---|--------------------------|---------------------|---------------|------:|------------------------------------------|----------|---------------------------------|
| 1 | CARD-NUM                 | PIC X(16)           | Character     |    16 | Credit card number                       | No       | Primary key; 16-digit PAN       |
| 2 | CARD-ACCT-ID             | PIC 9(11)           | Numeric       |    11 | Associated account ID                    | No       | FK to Account entity            |
| 3 | CARD-CVV-CD              | PIC 9(03)           | Numeric       |     3 | Card verification value                  | No       | 3-digit CVV code                |
| 4 | CARD-EMBOSSED-NAME       | PIC X(50)           | Character     |    50 | Name printed on card                     | No       |                                 |
| 5 | CARD-EXPIRAION-DATE      | PIC X(10)           | Date String   |    10 | Card expiration date                     | No       | Typo in source ("EXPIRAION")    |
| 6 | CARD-ACTIVE-STATUS       | PIC X(01)           | Character     |     1 | Card status flag                         | No       | 'Y' = Active, 'N' = Inactive   |
| 7 | FILLER                   | PIC X(59)           | Padding       |    59 | Reserved space                           | ---      | Unused                          |

**Business Rules:**
- One account can have multiple cards
- Card number is stored as alphanumeric (preserves leading zeros)
- CVV is a 3-digit numeric code
- Card status is independent of account status

---

## 3. Customer Entity

**Source Copybook:** `CVCUS01Y.cpy` | **Record Length:** 500 bytes | **VSAM File:** CUSTDAT

| # | Field Name               | COBOL PIC Clause    | Data Type     | Size  | Business Description                     | Nullable | Notes                           |
|---|--------------------------|---------------------|---------------|------:|------------------------------------------|----------|---------------------------------|
| 1 | CUST-ID                  | PIC 9(09)           | Numeric       |     9 | Unique customer identifier               | No       | Primary key                     |
| 2 | CUST-FIRST-NAME          | PIC X(25)           | Character     |    25 | Customer first name                      | No       |                                 |
| 3 | CUST-MIDDLE-NAME         | PIC X(25)           | Character     |    25 | Customer middle name                     | Yes      |                                 |
| 4 | CUST-LAST-NAME           | PIC X(25)           | Character     |    25 | Customer last name                       | No       |                                 |
| 5 | CUST-ADDR-LINE-1         | PIC X(50)           | Character     |    50 | Address line 1                           | No       |                                 |
| 6 | CUST-ADDR-LINE-2         | PIC X(50)           | Character     |    50 | Address line 2                           | Yes      |                                 |
| 7 | CUST-ADDR-LINE-3         | PIC X(50)           | Character     |    50 | Address line 3                           | Yes      |                                 |
| 8 | CUST-ADDR-STATE-CD       | PIC X(02)           | Character     |     2 | US state code                            | No       | Validated against CSLKPCDY      |
| 9 | CUST-ADDR-COUNTRY-CD     | PIC X(03)           | Character     |     3 | Country code                             | No       |                                 |
|10 | CUST-ADDR-ZIP            | PIC X(10)           | Character     |    10 | ZIP/postal code                          | No       | Validated against CSLKPCDY      |
|11 | CUST-PHONE-NUM-1         | PIC X(15)           | Character     |    15 | Primary phone number                     | No       | Area code validated (CSLKPCDY)  |
|12 | CUST-PHONE-NUM-2         | PIC X(15)           | Character     |    15 | Secondary phone number                   | Yes      |                                 |
|13 | CUST-SSN                 | PIC 9(09)           | Numeric       |     9 | Social Security Number                   | No       | **PII - Sensitive**             |
|14 | CUST-GOVT-ISSUED-ID      | PIC X(20)           | Character     |    20 | Government-issued ID number              | Yes      | **PII - Sensitive**             |
|15 | CUST-DOB-YYYY-MM-DD      | PIC X(10)           | Date String   |    10 | Date of birth                            | No       | **PII - Sensitive**; validated  |
|16 | CUST-EFT-ACCOUNT-ID      | PIC X(10)           | Character     |    10 | Electronic funds transfer account        | Yes      |                                 |
|17 | CUST-PRI-CARD-HOLDER-IND | PIC X(01)           | Character     |     1 | Primary cardholder indicator             | No       | 'Y' = Primary, 'N' = Authorized|
|18 | CUST-FICO-CREDIT-SCORE   | PIC 9(03)           | Numeric       |     3 | FICO credit score                        | No       | Range: 300-850                  |
|19 | FILLER                   | PIC X(168)          | Padding       |   168 | Reserved space                           | ---      | Unused                          |

**Business Rules:**
- SSN, DOB, and Government ID are PII fields requiring encryption in modernized system
- State code and ZIP are cross-validated using CSLKPCDY lookup tables
- Phone area codes are validated against North America Numbering Plan
- Date of birth cannot be in the future (validated by CSUTLDPY)
- FICO score is a 3-digit number (300-850 typical range)

---

## 4. Card Cross-Reference Entity

**Source Copybook:** `CVACT03Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** CARDXREF

| # | Field Name               | COBOL PIC Clause    | Data Type     | Size  | Business Description                     | Nullable | Notes                           |
|---|--------------------------|---------------------|---------------|------:|------------------------------------------|----------|---------------------------------|
| 1 | XREF-CARD-NUM            | PIC X(16)           | Character     |    16 | Card number                              | No       | Primary key; FK to Card         |
| 2 | XREF-CUST-ID             | PIC 9(09)           | Numeric       |     9 | Customer ID                              | No       | FK to Customer                  |
| 3 | XREF-ACCT-ID             | PIC 9(11)           | Numeric       |    11 | Account ID                               | No       | FK to Account                   |
| 4 | FILLER                   | PIC X(14)           | Padding       |    14 | Reserved space                           | ---      | Unused                          |

**Business Rules:**
- This is a junction/bridge entity linking Card → Customer → Account
- Enables lookup of account and customer given a card number
- Critical for transaction processing (card-based lookup)
- Has VSAM alternate index paths for account-based and customer-based access

---

## 5. Transaction Entity

**Source Copybook:** `CVTRA05Y.cpy` | **Record Length:** 350 bytes | **VSAM File:** TRANSACT

| # | Field Name               | COBOL PIC Clause    | Data Type     | Size  | Business Description                     | Nullable | Notes                           |
|---|--------------------------|---------------------|---------------|------:|------------------------------------------|----------|---------------------------------|
| 1 | TRAN-ID                  | PIC X(16)           | Character     |    16 | Unique transaction identifier            | No       | Primary key                     |
| 2 | TRAN-TYPE-CD             | PIC X(02)           | Character     |     2 | Transaction type code                    | No       | FK to Transaction Type          |
| 3 | TRAN-CAT-CD              | PIC 9(04)           | Numeric       |     4 | Transaction category code                | No       | FK to Transaction Category      |
| 4 | TRAN-SOURCE              | PIC X(10)           | Character     |    10 | Transaction source/channel               | No       | e.g., "ONLINE", "POS", "ATM"   |
| 5 | TRAN-DESC                | PIC X(100)          | Character     |   100 | Transaction description                  | Yes      |                                 |
| 6 | TRAN-AMT                 | PIC S9(09)V99       | Signed Decimal|  11,2 | Transaction amount                       | No       | Signed; negative = credit       |
| 7 | TRAN-MERCHANT-ID         | PIC 9(09)           | Numeric       |     9 | Merchant identifier                      | Yes      |                                 |
| 8 | TRAN-MERCHANT-NAME       | PIC X(50)           | Character     |    50 | Merchant name                            | Yes      |                                 |
| 9 | TRAN-MERCHANT-CITY       | PIC X(50)           | Character     |    50 | Merchant city                            | Yes      |                                 |
|10 | TRAN-MERCHANT-ZIP        | PIC X(10)           | Character     |    10 | Merchant ZIP code                        | Yes      |                                 |
|11 | TRAN-CARD-NUM            | PIC X(16)           | Character     |    16 | Card used for transaction                | No       | FK to Card                      |
|12 | TRAN-ORIG-TS             | PIC X(26)           | Timestamp     |    26 | Original transaction timestamp           | No       | ISO-like format                 |
|13 | TRAN-PROC-TS             | PIC X(26)           | Timestamp     |    26 | Processing timestamp                     | No       | Set during batch posting        |
|14 | FILLER                   | PIC X(20)           | Padding       |    20 | Reserved space                           | ---      | Unused                          |

**Business Rules:**
- Transaction amount is signed: positive = debit, negative = credit/refund
- Transaction type and category are separate dimensions (type is broad, category is specific)
- Card number links back through cross-reference to determine account and customer
- Transactions flow: Daily input (DALYTRAN) → Validation (CBTRN01C) → Posting (CBTRN02C) → Master (TRANSACT)

---

## 6. Daily Transaction Entity (Batch Input)

**Source Copybook:** `CVTRA06Y.cpy` | **Record Length:** 350 bytes | **VSAM File:** DALYTRAN

| # | Field Name               | COBOL PIC Clause    | Data Type     | Size  | Business Description                     | Nullable | Notes                           |
|---|--------------------------|---------------------|---------------|------:|------------------------------------------|----------|---------------------------------|
| 1 | DALYTRAN-ID              | PIC X(16)           | Character     |    16 | Daily transaction identifier             | No       | Primary key                     |
| 2 | DALYTRAN-TYPE-CD         | PIC X(02)           | Character     |     2 | Transaction type code                    | No       |                                 |
| 3 | DALYTRAN-CAT-CD          | PIC 9(04)           | Numeric       |     4 | Transaction category code                | No       |                                 |
| 4 | DALYTRAN-SOURCE          | PIC X(10)           | Character     |    10 | Transaction source                       | No       |                                 |
| 5 | DALYTRAN-DESC            | PIC X(100)          | Character     |   100 | Transaction description                  | Yes      |                                 |
| 6 | DALYTRAN-AMT             | PIC S9(09)V99       | Signed Decimal|  11,2 | Transaction amount                       | No       |                                 |
| 7 | DALYTRAN-MERCHANT-ID     | PIC 9(09)           | Numeric       |     9 | Merchant identifier                      | Yes      |                                 |
| 8 | DALYTRAN-MERCHANT-NAME   | PIC X(50)           | Character     |    50 | Merchant name                            | Yes      |                                 |
| 9 | DALYTRAN-MERCHANT-CITY   | PIC X(50)           | Character     |    50 | Merchant city                            | Yes      |                                 |
|10 | DALYTRAN-MERCHANT-ZIP    | PIC X(10)           | Character     |    10 | Merchant ZIP code                        | Yes      |                                 |
|11 | DALYTRAN-CARD-NUM        | PIC X(16)           | Character     |    16 | Card used                                | No       |                                 |
|12 | DALYTRAN-ORIG-TS         | PIC X(26)           | Timestamp     |    26 | Original timestamp                       | No       |                                 |
|13 | DALYTRAN-PROC-TS         | PIC X(26)           | Timestamp     |    26 | Processing timestamp                     | No       |                                 |
|14 | FILLER                   | PIC X(20)           | Padding       |    20 | Reserved space                           | ---      |                                 |

**Business Rules:**
- Identical structure to Transaction entity (staging table pattern)
- Daily transactions are validated and posted to the master TRANSACT file
- Rejected records are written to DALYREJS file

---

## 7. User Security Entity

**Source Copybook:** `CSUSR01Y.cpy` | **Record Length:** 80 bytes | **VSAM File:** USRSEC

| # | Field Name               | COBOL PIC Clause    | Data Type     | Size  | Business Description                     | Nullable | Notes                           |
|---|--------------------------|---------------------|---------------|------:|------------------------------------------|----------|---------------------------------|
| 1 | SEC-USR-ID               | PIC X(08)           | Character     |     8 | User login ID                            | No       | Primary key                     |
| 2 | SEC-USR-FNAME            | PIC X(20)           | Character     |    20 | User first name                          | No       |                                 |
| 3 | SEC-USR-LNAME            | PIC X(20)           | Character     |    20 | User last name                           | No       |                                 |
| 4 | SEC-USR-PWD              | PIC X(08)           | Character     |     8 | User password                            | No       | **Plain text - security risk**  |
| 5 | SEC-USR-TYPE             | PIC X(01)           | Character     |     1 | User type                                | No       | 'A' = Admin, 'U' = Regular User |
| 6 | SEC-USR-FILLER           | PIC X(23)           | Padding       |    23 | Reserved space                           | ---      | Unused                          |

**Business Rules:**
- Passwords stored in plain text (major security concern for modernization)
- Only two user types: Admin and User
- Admin users see COADM01C menu; regular users see COMEN01C menu
- User IDs are 8 characters (e.g., ADMIN001, USER0001)

---

## 8. Transaction Category Balance Entity

**Source Copybook:** `CVTRA01Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** TCATBALF

| # | Field Name               | COBOL PIC Clause    | Data Type     | Size  | Business Description                     | Nullable | Notes                           |
|---|--------------------------|---------------------|---------------|------:|------------------------------------------|----------|---------------------------------|
| 1 | TRANCAT-ACCT-ID          | PIC 9(11)           | Numeric       |    11 | Account ID (part of composite key)       | No       | FK to Account; part of PK       |
| 2 | TRANCAT-TYPE-CD          | PIC X(02)           | Character     |     2 | Transaction type (part of composite key) | No       | Part of PK                      |
| 3 | TRANCAT-CD               | PIC 9(04)           | Numeric       |     4 | Category code (part of composite key)    | No       | Part of PK                      |
| 4 | TRAN-CAT-BAL             | PIC S9(09)V99       | Signed Decimal|  11,2 | Running balance for this category        | No       |                                 |
| 5 | FILLER                   | PIC X(22)           | Padding       |    22 | Reserved space                           | ---      |                                 |

**Business Rules:**
- Composite primary key: Account + Type + Category
- Maintains running balances per transaction category per account
- Updated during batch posting (CBTRN02C) and interest calculation (CBACT04C)

---

## 9. Disclosure Group Entity (Interest Rates)

**Source Copybook:** `CVTRA02Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** DISCGRP

| # | Field Name               | COBOL PIC Clause    | Data Type     | Size  | Business Description                     | Nullable | Notes                           |
|---|--------------------------|---------------------|---------------|------:|------------------------------------------|----------|---------------------------------|
| 1 | DIS-ACCT-GROUP-ID        | PIC X(10)           | Character     |    10 | Account group (part of composite key)    | No       | Links to ACCT-GROUP-ID          |
| 2 | DIS-TRAN-TYPE-CD         | PIC X(02)           | Character     |     2 | Transaction type (part of composite key) | No       |                                 |
| 3 | DIS-TRAN-CAT-CD          | PIC 9(04)           | Numeric       |     4 | Transaction category (part of key)       | No       |                                 |
| 4 | DIS-INT-RATE             | PIC S9(04)V99       | Signed Decimal|   6,2 | Interest rate percentage                 | No       |                                 |
| 5 | FILLER                   | PIC X(28)           | Padding       |    28 | Reserved space                           | ---      |                                 |

**Business Rules:**
- Interest rates are defined per group + transaction type + category combination
- Account's group ID determines which interest rates apply
- Used by CBACT04C during interest calculation batch run

---

## 10. Transaction Type Entity

**Source Copybook:** `CVTRA03Y.cpy` | **Record Length:** 60 bytes | **VSAM File:** TRANTYPE

| # | Field Name               | COBOL PIC Clause    | Data Type     | Size  | Business Description                     | Nullable | Notes                           |
|---|--------------------------|---------------------|---------------|------:|------------------------------------------|----------|---------------------------------|
| 1 | TRAN-TYPE                | PIC X(02)           | Character     |     2 | Transaction type code                    | No       | Primary key                     |
| 2 | TRAN-TYPE-DESC           | PIC X(50)           | Character     |    50 | Type description                         | No       | e.g., "Purchase", "Cash Advance"|
| 3 | FILLER                   | PIC X(08)           | Padding       |     8 | Reserved space                           | ---      |                                 |

---

## 11. Transaction Category Entity

**Source Copybook:** `CVTRA04Y.cpy` | **Record Length:** 60 bytes | **VSAM File:** TRANCATG

| # | Field Name               | COBOL PIC Clause    | Data Type     | Size  | Business Description                     | Nullable | Notes                           |
|---|--------------------------|---------------------|---------------|------:|------------------------------------------|----------|---------------------------------|
| 1 | TRAN-TYPE-CD             | PIC X(02)           | Character     |     2 | Transaction type (part of composite key) | No       | FK to Transaction Type; part of PK |
| 2 | TRAN-CAT-CD              | PIC 9(04)           | Numeric       |     4 | Category code (part of composite key)    | No       | Part of PK                      |
| 3 | TRAN-CAT-TYPE-DESC       | PIC X(50)           | Character     |    50 | Category description                     | No       |                                 |
| 4 | FILLER                   | PIC X(04)           | Padding       |     4 | Reserved space                           | ---      |                                 |

---

## 12. Communication Area (COMMAREA)

**Source Copybook:** `COCOM01Y.cpy` | **Type:** In-memory structure passed between CICS programs

| # | Field Name               | COBOL PIC Clause    | Data Type     | Size  | Business Description                     |
|---|--------------------------|---------------------|---------------|------:|------------------------------------------|
| 1 | CDEMO-FROM-TRANID        | PIC X(04)           | Character     |     4 | Calling transaction ID                   |
| 2 | CDEMO-FROM-PROGRAM       | PIC X(08)           | Character     |     8 | Calling program name                     |
| 3 | CDEMO-TO-TRANID          | PIC X(04)           | Character     |     4 | Target transaction ID                    |
| 4 | CDEMO-TO-PROGRAM         | PIC X(08)           | Character     |     8 | Target program name                      |
| 5 | CDEMO-USER-ID            | PIC X(08)           | Character     |     8 | Logged-in user ID                        |
| 6 | CDEMO-USER-TYPE          | PIC X(01)           | Character     |     1 | User type ('A'=Admin, 'U'=User)          |
| 7 | CDEMO-PGM-CONTEXT        | PIC 9(01)           | Numeric       |     1 | 0=First entry, 1=Re-entry                |
| 8 | CDEMO-CUST-ID            | PIC 9(09)           | Numeric       |     9 | Current customer context                 |
| 9 | CDEMO-CUST-FNAME         | PIC X(25)           | Character     |    25 | Customer first name (display)            |
|10 | CDEMO-CUST-MNAME         | PIC X(25)           | Character     |    25 | Customer middle name (display)           |
|11 | CDEMO-CUST-LNAME         | PIC X(25)           | Character     |    25 | Customer last name (display)             |
|12 | CDEMO-ACCT-ID            | PIC 9(11)           | Numeric       |    11 | Current account context                  |
|13 | CDEMO-ACCT-STATUS        | PIC X(01)           | Character     |     1 | Current account status                   |
|14 | CDEMO-CARD-NUM           | PIC 9(16)           | Numeric       |    16 | Current card context                     |
|15 | CDEMO-LAST-MAP           | PIC X(7)            | Character     |     7 | Last BMS map displayed                   |
|16 | CDEMO-LAST-MAPSET        | PIC X(7)            | Character     |     7 | Last BMS mapset used                     |

**Business Rules:**
- COMMAREA is the session state mechanism — equivalent to HTTP session in web apps
- Passed via CICS XCTL (transfer control) between programs
- Contains both navigation state and business context

---

## 13. Export Record (Multi-Entity)

**Source Copybook:** `CVEXPORT.cpy` | **Record Length:** 500 bytes | **File:** Sequential export file

| # | Field Name               | COBOL PIC Clause    | Data Type     | Size  | Business Description                     |
|---|--------------------------|---------------------|---------------|------:|------------------------------------------|
| 1 | EXPORT-REC-TYPE          | PIC X(1)            | Character     |     1 | Record type discriminator                |
| 2 | EXPORT-TIMESTAMP         | PIC X(26)           | Timestamp     |    26 | Export timestamp                         |
| 3 | EXPORT-SEQUENCE-NUM      | PIC 9(9) COMP       | Binary        |     4 | Sequence number (binary packed)          |
| 4 | EXPORT-BRANCH-ID         | PIC X(4)            | Character     |     4 | Branch identifier                        |
| 5 | EXPORT-REGION-CODE       | PIC X(5)            | Character     |     5 | Region code                              |
| 6 | EXPORT-RECORD-DATA       | PIC X(460)          | Character     |   460 | Polymorphic record data (REDEFINES)      |

**REDEFINES Variants:**
- Customer data (mirrors CVCUS01Y with COMP-3 optimizations)
- Account data (mirrors CVACT01Y with COMP-3 optimizations)
- Transaction data (mirrors CVTRA05Y with COMP-3 optimizations)
- Card cross-reference data (mirrors CVACT03Y with COMP optimization)
- Card data (mirrors CVACT02Y with COMP optimizations)

**Notes:**
- Uses COMP and COMP-3 packed fields for storage optimization
- REDEFINES structure is the COBOL equivalent of a discriminated union / polymorphic type

---

## 14. Date Conversion Record

**Source Copybook:** `CODATECN.cpy` | **Type:** Working storage for date format conversion

| # | Field Name               | COBOL PIC Clause    | Data Type     | Size  | Business Description                     |
|---|--------------------------|---------------------|---------------|------:|------------------------------------------|
| 1 | CODATECN-TYPE            | PIC X               | Character     |     1 | Input format: '1'=YYYYMMDD, '2'=YYYY-MM-DD |
| 2 | CODATECN-INP-DATE        | PIC X(20)           | Character     |    20 | Input date value                         |
| 3 | CODATECN-OUTTYPE         | PIC X               | Character     |     1 | Output format: '1'=YYYY-MM-DD, '2'=YYYYMMDD |
| 4 | CODATECN-0UT-DATE        | PIC X(20)           | Character     |    20 | Output date value                        |
| 5 | CODATECN-ERROR-MSG       | PIC X(38)           | Character     |    38 | Conversion error message                 |

---

## Entity Relationship Summary

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   Customer   │────<│  Card XRef   │>────│   Account    │
│  CVCUS01Y    │     │  CVACT03Y    │     │  CVACT01Y    │
│  (CUSTDAT)   │     │  (CARDXREF)  │     │  (ACCTDAT)   │
└──────┬───────┘     └──────┬───────┘     └──────┬───────┘
       │                    │                    │
       │             ┌──────┴───────┐            │
       │             │    Card      │            │
       │             │  CVACT02Y    │            │
       │             │  (CARDDAT)   │            │
       │             └──────┬───────┘            │
       │                    │                    │
       │             ┌──────┴───────┐     ┌──────┴───────┐
       │             │ Transaction  │     │  Cat Balance │
       │             │  CVTRA05Y    │     │  CVTRA01Y    │
       │             │  (TRANSACT)  │     │  (TCATBALF)  │
       │             └──────┬───────┘     └──────────────┘
       │                    │
       │             ┌──────┴───────┐     ┌──────────────┐
       │             │  Tran Type   │────<│ Tran Category│
       │             │  CVTRA03Y    │     │  CVTRA04Y    │
       │             │  (TRANTYPE)  │     │  (TRANCATG)  │
       │             └──────────────┘     └──────────────┘
       │
┌──────┴───────┐                          ┌──────────────┐
│  User/Sec    │                          │ Disclosure   │
│  CSUSR01Y    │                          │  CVTRA02Y    │
│  (USRSEC)    │                          │  (DISCGRP)   │
└──────────────┘                          └──────────────┘
```

---

## PII & Sensitivity Classification

| Field                    | Entity    | Sensitivity | Modernization Note                              |
|--------------------------|-----------|-------------|--------------------------------------------------|
| CUST-SSN                 | Customer  | **High**    | Must be encrypted at rest and in transit          |
| CUST-DOB-YYYY-MM-DD      | Customer  | **High**    | PII; encrypt or hash                             |
| CUST-GOVT-ISSUED-ID      | Customer  | **High**    | Government ID; encrypt at rest                   |
| CARD-NUM                 | Card      | **High**    | PCI-DSS: must be tokenized or encrypted          |
| CARD-CVV-CD              | Card      | **High**    | PCI-DSS: must never be stored post-authorization |
| SEC-USR-PWD              | User      | **Critical**| Plain text; must be hashed (bcrypt/scrypt)       |
| CUST-PHONE-NUM-1/2       | Customer  | **Medium**  | PII; consider masking in logs                    |
| CUST-ADDR-*              | Customer  | **Medium**  | PII; address data                                |
