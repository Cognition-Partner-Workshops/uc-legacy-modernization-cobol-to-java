# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** Copybook PIC clause analysis across `app/cpy/` and `app/cpy-bms/`
> **Purpose:** Business-friendly reference mapping COBOL record layouts to logical data entities

---

## Table of Contents

1. [Overview](#overview)
2. [VSAM File Inventory](#vsam-file-inventory)
3. [Account Entity](#1-account-entity)
4. [Credit Card Entity](#2-credit-card-entity)
5. [Card Cross-Reference Entity](#3-card-cross-reference-entity)
6. [Customer Entity](#4-customer-entity)
7. [Transaction Entity](#5-transaction-entity)
8. [Daily Transaction Entity](#6-daily-transaction-entity)
9. [Transaction Category Balance Entity](#7-transaction-category-balance-entity)
10. [Disclosure Group Entity](#8-disclosure-group-entity)
11. [Transaction Type Entity](#9-transaction-type-entity)
12. [Transaction Category Entity](#10-transaction-category-entity)
13. [User Security Entity](#11-user-security-entity)
14. [Common/Infrastructure Structures](#12-commoninfrastructure-structures)
15. [Statement Reporting Transaction Entity](#13-statement-reporting-transaction-entity)
16. [Export Record Entity](#14-export-record-entity)
17. [Report Layout Structures](#15-report-layout-structures)
18. [PIC Clause Reference](#pic-clause-reference)
19. [Entity Relationship Summary](#entity-relationship-summary)

---

## Overview

CardDemo uses **VSAM KSDS** (Key-Sequenced Data Sets) as its primary data store, with each file corresponding to a business entity. Copybooks define the record layouts using COBOL PIC clauses. This dictionary translates each copybook into a business-friendly format suitable for relational database schema design during modernization.

### Data Type Mapping (COBOL to Java/SQL)

| COBOL PIC Clause        | Bytes | Java Type       | SQL Type          | Description                |
|--------------------------|------:|-----------------|-------------------|----------------------------|
| `PIC X(n)`              |     n | String          | VARCHAR(n)        | Alphanumeric text          |
| `PIC 9(n)`              |     n | int / long      | NUMERIC(n)        | Unsigned integer           |
| `PIC S9(n)V99`          |   n+2 | BigDecimal      | DECIMAL(n+2, 2)   | Signed decimal (currency)  |
| `PIC S9(n) COMP`        |   2/4 | int / long      | INTEGER / BIGINT  | Binary integer             |
| `PIC 9(n) COMP`         |   2/4 | int / long      | INTEGER           | Binary unsigned integer    |
| `FILLER PIC X(n)`       |     n | _(skip)_        | _(skip)_          | Reserved/padding bytes     |

---

## VSAM File Inventory

| VSAM Dataset Name                              | Key      | Record Len | Entity                  | Copybook  |
|------------------------------------------------|----------|------------|-------------------------|-----------|
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS            | Acct ID  | 300 bytes  | Account                 | CVACT01Y  |
| AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS            | Card Num | 150 bytes  | Credit Card             | CVACT02Y  |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS            | Card Num | 50 bytes   | Card Cross-Reference    | CVACT03Y  |
| AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS            | Cust ID  | 500 bytes  | Customer                | CVCUS01Y  |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS            | Tran ID  | 350 bytes  | Transaction             | CVTRA05Y  |
| AWS.M2.CARDDEMO.DALYTRAN.VSAM.KSDS            | Tran ID  | 350 bytes  | Daily Transaction       | CVTRA06Y  |
| AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS             | Composite| 50 bytes   | Tran Category Balance   | CVTRA01Y  |
| AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS             | Composite| 50 bytes   | Disclosure Group        | CVTRA02Y  |
| AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS            | Type CD  | 60 bytes   | Transaction Type        | CVTRA03Y  |
| AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS            | Composite| 60 bytes   | Transaction Category    | CVTRA04Y  |
| AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS              | User ID  | 80 bytes   | User Security           | CSUSR01Y  |

---

## 1. Account Entity

**Copybook:** `CVACT01Y` | **Record:** `ACCOUNT-RECORD` | **Length:** 300 bytes

_Business Description:_ Represents a credit card account, including balances, credit limits, and status.

| # | COBOL Field Name          | PIC Clause        | Offset | Len  | Business Name               | Business Description                      | Nullable | Java Type    | SQL Type         |
|---|---------------------------|--------------------|--------|------|-----------------------------|-------------------------------------------|----------|--------------|------------------|
| 1 | ACCT-ID                   | PIC 9(11)          | 0      | 11   | Account ID                  | Unique account identifier                 | No (PK)  | long         | NUMERIC(11) PK   |
| 2 | ACCT-ACTIVE-STATUS        | PIC X(01)          | 11     | 1    | Account Status              | Active flag (Y/N)                         | No       | String       | CHAR(1)          |
| 3 | ACCT-CURR-BAL             | PIC S9(10)V99      | 12     | 12   | Current Balance             | Current outstanding balance               | No       | BigDecimal   | DECIMAL(12,2)    |
| 4 | ACCT-CREDIT-LIMIT         | PIC S9(10)V99      | 24     | 12   | Credit Limit                | Maximum allowed credit                    | No       | BigDecimal   | DECIMAL(12,2)    |
| 5 | ACCT-CASH-CREDIT-LIMIT    | PIC S9(10)V99      | 36     | 12   | Cash Credit Limit           | Cash advance limit                        | No       | BigDecimal   | DECIMAL(12,2)    |
| 6 | ACCT-OPEN-DATE            | PIC X(10)          | 48     | 10   | Account Open Date           | Date account was opened                   | No       | LocalDate    | DATE             |
| 7 | ACCT-EXPIRAION-DATE       | PIC X(10)          | 58     | 10   | Expiration Date             | Account expiration date                   | No       | LocalDate    | DATE             |
| 8 | ACCT-REISSUE-DATE         | PIC X(10)          | 68     | 10   | Reissue Date                | Last reissue date                         | Yes      | LocalDate    | DATE             |
| 9 | ACCT-CURR-CYC-CREDIT      | PIC S9(10)V99      | 78     | 12   | Current Cycle Credits       | Credits applied in current cycle          | No       | BigDecimal   | DECIMAL(12,2)    |
|10 | ACCT-CURR-CYC-DEBIT       | PIC S9(10)V99      | 90     | 12   | Current Cycle Debits        | Debits applied in current cycle           | No       | BigDecimal   | DECIMAL(12,2)    |
|11 | ACCT-ADDR-ZIP             | PIC X(10)          | 102    | 10   | ZIP Code                    | Account holder ZIP/postal code            | Yes      | String       | VARCHAR(10)      |
|12 | ACCT-GROUP-ID             | PIC X(10)          | 112    | 10   | Account Group ID            | Interest rate disclosure group            | No       | String       | VARCHAR(10)      |
|13 | FILLER                    | PIC X(178)         | 122    | 178  | _(reserved)_                | Reserved for future use                   | --       | --           | --               |

**Key Relationships:**
- One Account has many Credit Cards (via `CVACT03Y` cross-reference)
- Account Group ID links to Disclosure Group (`CVTRA02Y`)

---

## 2. Credit Card Entity

**Copybook:** `CVACT02Y` | **Record:** `CARD-RECORD` | **Length:** 150 bytes

_Business Description:_ Represents a physical credit card issued against an account.

| # | COBOL Field Name          | PIC Clause        | Offset | Len  | Business Name               | Business Description                      | Nullable | Java Type    | SQL Type         |
|---|---------------------------|--------------------|--------|------|-----------------------------|-------------------------------------------|----------|--------------|------------------|
| 1 | CARD-NUM                  | PIC X(16)          | 0      | 16   | Card Number                 | 16-digit credit card number               | No (PK)  | String       | CHAR(16) PK      |
| 2 | CARD-ACCT-ID              | PIC 9(11)          | 16     | 11   | Account ID                  | Parent account identifier (FK)            | No (FK)  | long         | NUMERIC(11) FK   |
| 3 | CARD-CVV-CD               | PIC 9(03)          | 27     | 3    | CVV Code                    | Card verification value                   | No       | int          | NUMERIC(3)       |
| 4 | CARD-EMBOSSED-NAME        | PIC X(50)          | 30     | 50   | Embossed Name               | Cardholder name as printed on card        | No       | String       | VARCHAR(50)      |
| 5 | CARD-EXPIRAION-DATE       | PIC X(10)          | 80     | 10   | Expiration Date             | Card expiration date                      | No       | LocalDate    | DATE             |
| 6 | CARD-ACTIVE-STATUS        | PIC X(01)          | 90     | 1    | Card Status                 | Active flag (Y/N)                         | No       | String       | CHAR(1)          |
| 7 | FILLER                    | PIC X(59)          | 91     | 59   | _(reserved)_                | Reserved for future use                   | --       | --           | --               |

**Key Relationships:**
- Card belongs to one Account (`CARD-ACCT-ID` -> `ACCT-ID`)
- Card is referenced in Transactions (`TRAN-CARD-NUM`)

---

## 3. Card Cross-Reference Entity

**Copybook:** `CVACT03Y` | **Record:** `CARD-XREF-RECORD` | **Length:** 50 bytes

_Business Description:_ Maps credit card numbers to their parent accounts, supporting alternate index lookups.

| # | COBOL Field Name          | PIC Clause        | Offset | Len  | Business Name               | Business Description                      | Nullable | Java Type    | SQL Type         |
|---|---------------------------|--------------------|--------|------|-----------------------------|-------------------------------------------|----------|--------------|------------------|
| 1 | XREF-CARD-NUM             | PIC X(16)          | 0      | 16   | Card Number                 | Credit card number (PK)                   | No (PK)  | String       | CHAR(16) PK      |
| 2 | XREF-ACCT-ID              | PIC 9(11)          | 16     | 11   | Account ID                  | Parent account (FK)                       | No (FK)  | long         | NUMERIC(11) FK   |
| 3 | XREF-CUST-ID              | PIC 9(09)          | 27     | 9    | Customer ID                 | Card-owning customer (FK)                 | No (FK)  | long         | NUMERIC(9) FK    |
| 4 | FILLER                    | PIC X(14)          | 36     | 14   | _(reserved)_                | Reserved for future use                   | --       | --           | --               |

**Key Relationships:**
- Links Card -> Account -> Customer (bridge/junction entity)
- Alternate index on `XREF-ACCT-ID` for reverse lookups

---

## 4. Customer Entity

**Copybook:** `CVCUS01Y` | **Record:** `CUSTOMER-RECORD` | **Length:** 500 bytes

_Business Description:_ Represents a customer (cardholder) with personal and contact information.

| # | COBOL Field Name          | PIC Clause        | Offset | Len  | Business Name               | Business Description                      | Nullable | Java Type    | SQL Type         |
|---|---------------------------|--------------------|--------|------|-----------------------------|-------------------------------------------|----------|--------------|------------------|
| 1 | CUST-ID                   | PIC 9(09)          | 0      | 9    | Customer ID                 | Unique customer identifier                | No (PK)  | long         | NUMERIC(9) PK    |
| 2 | CUST-FIRST-NAME           | PIC X(25)          | 9      | 25   | First Name                  | Customer first name                       | No       | String       | VARCHAR(25)      |
| 3 | CUST-MIDDLE-NAME          | PIC X(25)          | 34     | 25   | Middle Name                 | Customer middle name                      | Yes      | String       | VARCHAR(25)      |
| 4 | CUST-LAST-NAME            | PIC X(25)          | 59     | 25   | Last Name                   | Customer last name                        | No       | String       | VARCHAR(25)      |
| 5 | CUST-ADDR-LINE-1          | PIC X(50)          | 84     | 50   | Address Line 1              | Street address line 1                     | No       | String       | VARCHAR(50)      |
| 6 | CUST-ADDR-LINE-2          | PIC X(50)          | 134    | 50   | Address Line 2              | Street address line 2                     | Yes      | String       | VARCHAR(50)      |
| 7 | CUST-ADDR-LINE-3          | PIC X(50)          | 184    | 50   | Address Line 3              | Street address line 3                     | Yes      | String       | VARCHAR(50)      |
| 8 | CUST-ADDR-STATE-CD        | PIC X(02)          | 234    | 2    | State Code                  | US state code                             | No       | String       | CHAR(2)          |
| 9 | CUST-ADDR-COUNTRY-CD      | PIC X(03)          | 236    | 3    | Country Code                | ISO country code                          | No       | String       | CHAR(3)          |
|10 | CUST-ADDR-ZIP             | PIC X(10)          | 239    | 10   | ZIP Code                    | Postal/ZIP code                           | No       | String       | VARCHAR(10)      |
|11 | CUST-PHONE-NUM-1          | PIC X(15)          | 249    | 15   | Primary Phone               | Primary contact phone                     | Yes      | String       | VARCHAR(15)      |
|12 | CUST-PHONE-NUM-2          | PIC X(15)          | 264    | 15   | Secondary Phone             | Secondary contact phone                   | Yes      | String       | VARCHAR(15)      |
|13 | CUST-SSN                  | PIC 9(09)          | 279    | 9    | SSN                         | Social Security Number (PII)              | No       | String       | CHAR(9)          |
|14 | CUST-GOVT-ISSUED-ID       | PIC X(20)          | 288    | 20   | Government ID               | Government-issued identification          | Yes      | String       | VARCHAR(20)      |
|15 | CUST-DOB-YYYYMMDD         | PIC X(10)          | 308    | 10   | Date of Birth               | Customer date of birth (YYYY-MM-DD)       | No       | LocalDate    | DATE             |
|16 | CUST-EFT-ACCOUNT-ID       | PIC X(10)          | 318    | 10   | EFT Account ID              | Electronic funds transfer account         | Yes      | String       | VARCHAR(10)      |
|17 | CUST-PRI-CARD-HOLDER-IND  | PIC X(01)          | 328    | 1    | Primary Cardholder?         | Primary cardholder indicator (Y/N)        | No       | String       | CHAR(1)          |
|18 | CUST-FICO-CREDIT-SCORE    | PIC 9(03)          | 329    | 3    | FICO Score                  | Credit score (300-850)                    | Yes      | int          | NUMERIC(3)       |
|19 | FILLER                    | PIC X(168)         | 332    | 168  | _(reserved)_                | Reserved for future use                   | --       | --           | --               |

**Key Relationships:**
- One Customer can have many Cards (via Cross-Reference)
- Contains PII fields: SSN, DOB, Government ID (require encryption in modernized system)

---

## 5. Transaction Entity

**Copybook:** `CVTRA05Y` | **Record:** `TRAN-RECORD` | **Length:** 350 bytes

_Business Description:_ Represents a completed credit card transaction in the master transaction file.

| # | COBOL Field Name          | PIC Clause        | Offset | Len  | Business Name               | Business Description                      | Nullable | Java Type    | SQL Type         |
|---|---------------------------|--------------------|--------|------|-----------------------------|-------------------------------------------|----------|--------------|------------------|
| 1 | TRAN-ID                   | PIC X(16)          | 0      | 16   | Transaction ID              | Unique transaction identifier             | No (PK)  | String       | CHAR(16) PK      |
| 2 | TRAN-TYPE-CD              | PIC X(02)          | 16     | 2    | Transaction Type Code       | Type code (FK to Transaction Type)        | No (FK)  | String       | CHAR(2) FK       |
| 3 | TRAN-CAT-CD               | PIC 9(04)          | 18     | 4    | Category Code               | Transaction category (FK)                 | No (FK)  | int          | NUMERIC(4) FK    |
| 4 | TRAN-SOURCE               | PIC X(10)          | 22     | 10   | Transaction Source          | Origin channel (POS, ATM, Online, etc.)   | No       | String       | VARCHAR(10)      |
| 5 | TRAN-DESC                 | PIC X(100)         | 32     | 100  | Description                 | Free-text transaction description         | Yes      | String       | VARCHAR(100)     |
| 6 | TRAN-AMT                  | PIC S9(09)V99      | 132    | 11   | Amount                      | Transaction amount (signed)               | No       | BigDecimal   | DECIMAL(11,2)    |
| 7 | TRAN-MERCHANT-ID          | PIC 9(09)          | 143    | 9    | Merchant ID                 | Merchant identifier                       | No       | long         | NUMERIC(9)       |
| 8 | TRAN-MERCHANT-NAME        | PIC X(50)          | 152    | 50   | Merchant Name               | Name of merchant                          | No       | String       | VARCHAR(50)      |
| 9 | TRAN-MERCHANT-CITY        | PIC X(50)          | 202    | 50   | Merchant City               | Merchant city location                    | Yes      | String       | VARCHAR(50)      |
|10 | TRAN-MERCHANT-ZIP         | PIC X(10)          | 252    | 10   | Merchant ZIP                | Merchant postal code                      | Yes      | String       | VARCHAR(10)      |
|11 | TRAN-CARD-NUM             | PIC X(16)          | 262    | 16   | Card Number                 | Card used for this transaction (FK)       | No (FK)  | String       | CHAR(16) FK      |
|12 | TRAN-ORIG-TS              | PIC X(26)          | 278    | 26   | Origination Timestamp       | When transaction was initiated            | No       | Instant      | TIMESTAMP        |
|13 | TRAN-PROC-TS              | PIC X(26)          | 304    | 26   | Processing Timestamp        | When transaction was processed            | Yes      | Instant      | TIMESTAMP        |
|14 | FILLER                    | PIC X(20)          | 330    | 20   | _(reserved)_                | Reserved for future use                   | --       | --           | --               |

**Key Relationships:**
- Transaction references Card Number -> Account
- Type Code references Transaction Type entity
- Category Code references Transaction Category entity

---

## 6. Daily Transaction Entity

**Copybook:** `CVTRA06Y` | **Record:** `DALYTRAN-RECORD` | **Length:** 350 bytes

_Business Description:_ Represents an incoming daily transaction before posting to the master file. Identical structure to Transaction but uses the `DALYTRAN-` prefix.

| # | COBOL Field Name          | PIC Clause        | Offset | Len  | Business Name               | Business Description                      |
|---|---------------------------|--------------------|--------|------|-----------------------------|-------------------------------------------|
| 1 | DALYTRAN-ID               | PIC X(16)          | 0      | 16   | Transaction ID              | Unique transaction identifier             |
| 2 | DALYTRAN-TYPE-CD          | PIC X(02)          | 16     | 2    | Transaction Type Code       | Type code                                 |
| 3 | DALYTRAN-CAT-CD           | PIC 9(04)          | 18     | 4    | Category Code               | Transaction category                      |
| 4 | DALYTRAN-SOURCE           | PIC X(10)          | 22     | 10   | Transaction Source          | Origin channel                            |
| 5 | DALYTRAN-DESC             | PIC X(100)         | 32     | 100  | Description                 | Transaction description                   |
| 6 | DALYTRAN-AMT              | PIC S9(09)V99      | 132    | 11   | Amount                      | Transaction amount (signed)               |
| 7 | DALYTRAN-MERCHANT-ID      | PIC 9(09)          | 143    | 9    | Merchant ID                 | Merchant identifier                       |
| 8 | DALYTRAN-MERCHANT-NAME    | PIC X(50)          | 152    | 50   | Merchant Name               | Name of merchant                          |
| 9 | DALYTRAN-MERCHANT-CITY    | PIC X(50)          | 202    | 50   | Merchant City               | Merchant city location                    |
|10 | DALYTRAN-MERCHANT-ZIP     | PIC X(10)          | 252    | 10   | Merchant ZIP                | Merchant postal code                      |
|11 | DALYTRAN-CARD-NUM         | PIC X(16)          | 262    | 16   | Card Number                 | Card used for this transaction            |
|12 | DALYTRAN-ORIG-TS          | PIC X(26)          | 278    | 26   | Origination Timestamp       | When transaction was initiated            |
|13 | DALYTRAN-PROC-TS          | PIC X(26)          | 304    | 26   | Processing Timestamp        | When transaction was processed            |
|14 | FILLER                    | PIC X(20)          | 330    | 20   | _(reserved)_                | Reserved for future use                   |

> **Modernization Note:** In a relational model, Daily Transaction and Transaction can be the same table with a `status` column differentiating posted vs. pending records.

---

## 7. Transaction Category Balance Entity

**Copybook:** `CVTRA01Y` | **Record:** `TRAN-CAT-BAL-RECORD` | **Length:** 50 bytes

_Business Description:_ Tracks running balance per account per transaction type and category. Used for interest calculation.

| # | COBOL Field Name          | PIC Clause        | Offset | Len  | Business Name               | Business Description                      |
|---|---------------------------|--------------------|--------|------|-----------------------------|-------------------------------------------|
| 1 | TRANCAT-ACCT-ID           | PIC 9(11)          | 0      | 11   | Account ID                  | Account identifier (composite PK)        |
| 2 | TRANCAT-TYPE-CD           | PIC X(02)          | 11     | 2    | Transaction Type Code       | Type code (composite PK)                  |
| 3 | TRANCAT-CD                | PIC 9(04)          | 13     | 4    | Category Code               | Category code (composite PK)              |
| 4 | TRAN-CAT-BAL              | PIC S9(09)V99      | 17     | 11   | Category Balance            | Running balance for this category         |
| 5 | FILLER                    | PIC X(22)          | 28     | 22   | _(reserved)_                | Reserved for future use                   |

**Key:** Composite (ACCT-ID + TYPE-CD + CAT-CD)

---

## 8. Disclosure Group Entity

**Copybook:** `CVTRA02Y` | **Record:** `DIS-GROUP-RECORD` | **Length:** 50 bytes

_Business Description:_ Defines interest rates per disclosure group, transaction type, and category. Used during interest calculation.

| # | COBOL Field Name          | PIC Clause        | Offset | Len  | Business Name               | Business Description                      |
|---|---------------------------|--------------------|--------|------|-----------------------------|-------------------------------------------|
| 1 | DIS-ACCT-GROUP-ID         | PIC X(10)          | 0      | 10   | Account Group ID            | Disclosure group identifier (composite PK)|
| 2 | DIS-TRAN-TYPE-CD          | PIC X(02)          | 10     | 2    | Transaction Type Code       | Type code (composite PK)                  |
| 3 | DIS-TRAN-CAT-CD           | PIC 9(04)          | 12     | 4    | Category Code               | Category code (composite PK)              |
| 4 | DIS-INT-RATE              | PIC S9(04)V99      | 16     | 6    | Interest Rate               | Interest rate percentage                  |
| 5 | FILLER                    | PIC X(28)          | 22     | 28   | _(reserved)_                | Reserved for future use                   |

**Key:** Composite (GROUP-ID + TYPE-CD + CAT-CD)

---

## 9. Transaction Type Entity

**Copybook:** `CVTRA03Y` | **Record:** `TRAN-TYPE-RECORD` | **Length:** 60 bytes

_Business Description:_ Reference/lookup table for transaction type codes and their descriptions.

| # | COBOL Field Name          | PIC Clause        | Offset | Len  | Business Name               | Business Description                      |
|---|---------------------------|--------------------|--------|------|-----------------------------|-------------------------------------------|
| 1 | TRAN-TYPE                 | PIC X(02)          | 0      | 2    | Transaction Type Code       | Type code (PK)                            |
| 2 | TRAN-TYPE-DESC            | PIC X(50)          | 2      | 50   | Type Description            | Human-readable type description           |
| 3 | FILLER                    | PIC X(08)          | 52     | 8    | _(reserved)_                | Reserved for future use                   |

---

## 10. Transaction Category Entity

**Copybook:** `CVTRA04Y` | **Record:** `TRAN-CAT-RECORD` | **Length:** 60 bytes

_Business Description:_ Reference/lookup table for transaction category codes within each type.

| # | COBOL Field Name          | PIC Clause        | Offset | Len  | Business Name               | Business Description                      |
|---|---------------------------|--------------------|--------|------|-----------------------------|-------------------------------------------|
| 1 | TRAN-TYPE-CD              | PIC X(02)          | 0      | 2    | Transaction Type Code       | Parent type code (composite PK, FK)       |
| 2 | TRAN-CAT-CD               | PIC 9(04)          | 2      | 4    | Category Code               | Category code (composite PK)              |
| 3 | TRAN-CAT-TYPE-DESC        | PIC X(50)          | 6      | 50   | Category Description        | Human-readable category description       |
| 4 | FILLER                    | PIC X(04)          | 56     | 4    | _(reserved)_                | Reserved for future use                   |

**Key:** Composite (TYPE-CD + CAT-CD)

---

## 11. User Security Entity

**Copybook:** `CSUSR01Y` | **Record:** `SEC-USER-DATA` | **Length:** 80 bytes

_Business Description:_ Application user credentials and role assignment for the CardDemo signon process.

| # | COBOL Field Name          | PIC Clause        | Offset | Len  | Business Name               | Business Description                      |
|---|---------------------------|--------------------|--------|------|-----------------------------|-------------------------------------------|
| 1 | SEC-USR-ID                | PIC X(08)          | 0      | 8    | User ID                     | Login user identifier (PK)                |
| 2 | SEC-USR-FNAME             | PIC X(20)          | 8      | 20   | First Name                  | User first name                           |
| 3 | SEC-USR-LNAME             | PIC X(20)          | 28     | 20   | Last Name                   | User last name                            |
| 4 | SEC-USR-PWD               | PIC X(08)          | 48     | 8    | Password                    | User password (plaintext - security risk) |
| 5 | SEC-USR-TYPE              | PIC X(01)          | 56     | 1    | User Type                   | Role: 'A' = Admin, 'U' = Regular User    |
| 6 | FILLER                    | PIC X(23)          | 57     | 23   | _(reserved)_                | Reserved for future use                   |

> **Security Note:** Password is stored as plaintext. Modernized system MUST use hashed passwords (bcrypt/scrypt) and proper authentication (OAuth2/OIDC).

---

## 12. Common/Infrastructure Structures

### CICS Communication Area (COCOM01Y)

**Record:** `CARDDEMO-COMMAREA`

_Business Description:_ Passed between CICS programs to maintain session state and navigation context.

| # | Field Name                | PIC Clause        | Business Name               | Business Description                      |
|---|---------------------------|--------------------|-----------------------------|--------------------------------------------|
| 1 | CDEMO-FROM-TRANID         | PIC X(04)          | Source Transaction          | Transaction transferring control           |
| 2 | CDEMO-FROM-PROGRAM        | PIC X(08)          | Source Program              | Program transferring control               |
| 3 | CDEMO-TO-TRANID           | PIC X(04)          | Target Transaction          | Destination transaction                    |
| 4 | CDEMO-TO-PROGRAM          | PIC X(08)          | Target Program              | Destination program                        |
| 5 | CDEMO-USER-ID             | PIC X(08)          | Logged-in User ID           | Current session user                       |
| 6 | CDEMO-USER-TYPE           | PIC X(01)          | User Type                   | A=Admin, U=User                            |
| 7 | CDEMO-PGM-CONTEXT         | PIC 9(01)          | Program Context             | State flags for multi-step operations      |
| 8 | CDEMO-ACCT-ID             | PIC 9(11)          | Selected Account ID         | Currently selected account                 |
| 9 | CDEMO-CARD-NUM            | PIC X(16)          | Selected Card Number        | Currently selected card                    |
|10 | CDEMO-LAST-MAP            | PIC X(07)          | Last Map Sent               | BMS map name for screen tracking           |
|11 | CDEMO-LAST-MAPSET         | PIC X(07)          | Last Mapset                 | BMS mapset name                            |

> **Modernization Note:** COMMAREA maps to HTTP session state or JWT claims in a web application.

### Menu Definitions (COMEN02Y)

Defines the main menu options (transaction IDs and program names) available to regular users.

### Admin Menu Definitions (COADM02Y)

Defines the admin menu options available to administrator users.

### Title/Header (COTTL01Y)

Screen header template with application name, date, and time fields.

---

## 13. Statement Reporting Transaction Entity

**Copybook:** `COSTM01` | **Record:** `TRNX-RECORD` | **Length:** 350 bytes

_Business Description:_ Altered transaction layout keyed by Card Number + Transaction ID, used for statement generation.

| # | COBOL Field Name          | PIC Clause        | Offset | Len  | Business Name               | Business Description                      |
|---|---------------------------|--------------------|--------|------|-----------------------------|-------------------------------------------|
| 1 | TRNX-CARD-NUM             | PIC X(16)          | 0      | 16   | Card Number                 | Card number (composite PK, part 1)        |
| 2 | TRNX-ID                   | PIC X(16)          | 16     | 16   | Transaction ID              | Transaction ID (composite PK, part 2)     |
| 3 | TRNX-TYPE-CD              | PIC X(02)          | 32     | 2    | Transaction Type Code       | Type code                                 |
| 4 | TRNX-CAT-CD               | PIC 9(04)          | 34     | 4    | Category Code               | Category code                             |
| 5 | TRNX-SOURCE               | PIC X(10)          | 38     | 10   | Transaction Source          | Origin channel                            |
| 6 | TRNX-DESC                 | PIC X(100)         | 48     | 100  | Description                 | Transaction description                   |
| 7 | TRNX-AMT                  | PIC S9(09)V99      | 148    | 11   | Amount                      | Transaction amount                        |
| 8 | TRNX-MERCHANT-ID          | PIC 9(09)          | 159    | 9    | Merchant ID                 | Merchant identifier                       |
| 9 | TRNX-MERCHANT-NAME        | PIC X(50)          | 168    | 50   | Merchant Name               | Merchant name                             |
|10 | TRNX-MERCHANT-CITY        | PIC X(50)          | 218    | 50   | Merchant City               | Merchant city                             |
|11 | TRNX-MERCHANT-ZIP         | PIC X(10)          | 268    | 10   | Merchant ZIP                | Merchant postal code                      |
|12 | TRNX-ORIG-TS              | PIC X(26)          | 278    | 26   | Origination Timestamp       | Transaction origination time              |
|13 | TRNX-PROC-TS              | PIC X(26)          | 304    | 26   | Processing Timestamp        | Transaction processing time               |
|14 | FILLER                    | PIC X(20)          | 330    | 20   | _(reserved)_                | Reserved for future use                   |

> **Note:** This is the same data as CVTRA05Y but re-keyed with Card Number first for statement sorting.

---

## 14. Export Record Entity

**Copybook:** `CVEXPORT` | **Record:** `EXPORT-RECORD`

_Business Description:_ Unified export record layout used by CBEXPORT/CBIMPORT for data migration.

Contains a header field identifying the record type, followed by the data payload matching the corresponding entity structure (Account, Card, Customer, Transaction, or Cross-Reference).

---

## 15. Report Layout Structures

**Copybook:** `CVTRA07Y`

_Business Description:_ Print formatting structures for the Daily Transaction Report.

| Structure Name              | Function                                          |
|----------------------------|---------------------------------------------------|
| REPORT-NAME-HEADER         | Report title, date range header                   |
| TRANSACTION-DETAIL-REPORT  | Individual transaction line on report             |
| TRANSACTION-HEADER-1       | Column header row 1                               |
| TRANSACTION-HEADER-2       | Column header separator line                      |
| REPORT-PAGE-TOTALS         | Page subtotal line                                |
| REPORT-ACCOUNT-TOTALS      | Account subtotal line                             |
| REPORT-GRAND-TOTALS        | Grand total line                                  |

---

## PIC Clause Reference

Quick reference for reading COBOL PIC clauses in this codebase:

| Notation         | Meaning                                   | Example                    |
|-----------------|-------------------------------------------|----------------------------|
| `PIC X(n)`       | Alphanumeric, n characters               | `PIC X(50)` = 50-char text |
| `PIC 9(n)`       | Numeric display, n digits                | `PIC 9(11)` = 11-digit num |
| `PIC S9(n)V99`   | Signed numeric, n+2 digits, 2 decimals   | `PIC S9(09)V99` = currency |
| `PIC S9(n) COMP` | Binary integer (2 or 4 bytes)            | `PIC S9(4) COMP` = halfword|
| `PIC 9(n) COMP`  | Binary unsigned integer                  | `PIC 9(3) COMP` = unsigned |
| `FILLER`         | Unused padding bytes                     | `FILLER PIC X(20)`         |
| `VALUE`          | Default/initial value                    | `VALUE SPACES`             |

---

## Entity Relationship Summary

```
                    ┌──────────────┐
                    │   Customer   │
                    │  (CVCUS01Y)  │
                    │  PK: CUST-ID │
                    └──────┬───────┘
                           │ 1:N
                    ┌──────┴───────┐
                    │  Card XREF   │
                    │  (CVACT03Y)  │
                    │  PK: CARD-NUM│
                    └──┬───────┬───┘
                  FK   │       │ FK
           ┌───────────┘       └──────────┐
           ▼                              ▼
    ┌──────────────┐              ┌──────────────┐
    │  Credit Card │              │   Account    │
    │  (CVACT02Y)  │──────FK─────▶│  (CVACT01Y)  │
    │  PK: CARD-NUM│              │  PK: ACCT-ID │
    └──────┬───────┘              └──────┬───────┘
           │                             │
           │ 1:N                         │ FK (GROUP-ID)
           ▼                             ▼
    ┌──────────────┐              ┌──────────────┐
    │ Transaction  │              │  Disclosure  │
    │  (CVTRA05Y)  │              │    Group     │
    │  PK: TRAN-ID │              │  (CVTRA02Y)  │
    └──────┬───────┘              └──────────────┘
           │
      FK   │  FK
    ┌──────┴──────┐
    ▼             ▼
┌─────────┐  ┌──────────┐   ┌──────────────────┐
│Tran Type│  │ Tran Cat │   │ Tran Cat Balance  │
│(CVTRA03Y│  │(CVTRA04Y)│   │   (CVTRA01Y)     │
│PK: TYPE │  │PK: T+C   │   │PK: ACCT+TYPE+CAT │
└─────────┘  └──────────┘   └──────────────────┘

    ┌──────────────┐
    │ User Security│
    │  (CSUSR01Y)  │
    │  PK: USR-ID  │
    └──────────────┘
    (Standalone - auth only)
```
