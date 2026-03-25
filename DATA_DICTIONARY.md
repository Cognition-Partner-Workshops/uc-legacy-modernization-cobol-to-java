# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Source**: COBOL Copybook PIC Clause Analysis
> **Purpose**: Business-friendly mapping of all data entities for modernization planning

---

## Overview

The CardDemo application manages data through VSAM KSDS files, each defined by a COBOL copybook record layout. This dictionary translates PIC clauses into business-readable field definitions.

| Entity                     | Copybook   | Record Size | VSAM Dataset                          | Key           |
|----------------------------|-----------|------------:|---------------------------------------|---------------|
| Account Master             | CVACT01Y  |  300 bytes  | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | ACCT-ID       |
| Credit Card                | CVACT02Y  |  150 bytes  | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` | CARD-NUM      |
| Card-Account Cross-Reference| CVACT03Y |   50 bytes  | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | XREF-CARD-NUM |
| Customer Master            | CVCUS01Y  |  500 bytes  | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` | CUST-ID       |
| Transaction (Online)       | CVTRA05Y  |  350 bytes  | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | TRAN-ID       |
| Daily Transaction          | CVTRA06Y  |  350 bytes  | `AWS.M2.CARDDEMO.DALYTRAN.PS`        | DALYTRAN-ID   |
| Transaction Category Balance| CVTRA01Y |   50 bytes  | `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` | Composite     |
| Discount/Interest Group    | CVTRA02Y  |   50 bytes  | `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`  | Composite     |
| Transaction Type           | CVTRA03Y  |   60 bytes  | `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` | TRAN-TYPE     |
| Transaction Category Type  | CVTRA04Y  |   60 bytes  | `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` | Composite     |
| User Security              | CSUSR01Y  |   80 bytes  | `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`   | SEC-USR-ID    |

---

## 1. Account Master (`CVACT01Y`)

**Business Purpose**: Stores credit card account information including balances, limits, and lifecycle dates.

| Field Name              | PIC Clause       | Data Type     | Size   | Business Description                     | Nullable | Constraints                  |
|-------------------------|------------------|---------------|--------|------------------------------------------|----------|------------------------------|
| ACCT-ID                 | `9(11)`          | Numeric       | 11 digits | **Primary Key** - Unique account number | No       | Unique, 11-digit integer     |
| ACCT-ACTIVE-STATUS      | `X(01)`          | Alpha         | 1 char | Account status (Y=Active, N=Inactive)    | No       | Values: Y, N                 |
| ACCT-CURR-BAL           | `S9(10)V99`      | Signed Decimal| 12,2   | Current balance (dollars and cents)      | No       | Signed, max +/-9,999,999,999.99 |
| ACCT-CREDIT-LIMIT       | `S9(10)V99`      | Signed Decimal| 12,2   | Credit limit                             | No       | Signed, max +/-9,999,999,999.99 |
| ACCT-CASH-CREDIT-LIMIT  | `S9(10)V99`      | Signed Decimal| 12,2   | Cash advance credit limit                | No       | Signed, max +/-9,999,999,999.99 |
| ACCT-OPEN-DATE          | `X(10)`          | Text Date     | 10 char| Date account was opened (YYYY-MM-DD)     | No       | Date format string           |
| ACCT-EXPIRAION-DATE     | `X(10)`          | Text Date     | 10 char| Account expiration date                  | No       | Date format string           |
| ACCT-REISSUE-DATE       | `X(10)`          | Text Date     | 10 char| Last card reissue date                   | No       | Date format string           |
| ACCT-CURR-CYC-CREDIT    | `S9(10)V99`      | Signed Decimal| 12,2   | Current cycle credit total               | No       | Running total for billing    |
| ACCT-CURR-CYC-DEBIT     | `S9(10)V99`      | Signed Decimal| 12,2   | Current cycle debit total                | No       | Running total for billing    |
| ACCT-ADDR-ZIP           | `X(10)`          | Alpha         | 10 char| Account holder ZIP code                  | Yes      | US ZIP or ZIP+4              |
| ACCT-GROUP-ID           | `X(10)`          | Alpha         | 10 char| Account group (for interest rates)       | No       | Links to CVTRA02Y            |
| FILLER                  | `X(178)`         | Padding       | 178    | Reserved space                           | ---      | Pad to 300 bytes             |

**Modernization Target**: `Account` JPA entity / `accounts` database table

---

## 2. Credit Card (`CVACT02Y`)

**Business Purpose**: Stores physical credit card details linked to an account.

| Field Name              | PIC Clause       | Data Type     | Size   | Business Description                     | Nullable | Constraints                  |
|-------------------------|------------------|---------------|--------|------------------------------------------|----------|------------------------------|
| CARD-NUM                | `X(16)`          | Alpha         | 16 char| **Primary Key** - Card number (PAN)      | No       | 16-character card number     |
| CARD-ACCT-ID            | `9(11)`          | Numeric       | 11 digits | **FK** - Linked account ID             | No       | References CVACT01Y.ACCT-ID  |
| CARD-CVV-CD             | `9(03)`          | Numeric       | 3 digits| Card verification value                  | No       | 3-digit CVV                  |
| CARD-EMBOSSED-NAME      | `X(50)`          | Alpha         | 50 char| Name embossed on card                    | No       | Cardholder name              |
| CARD-EXPIRAION-DATE     | `X(10)`          | Text Date     | 10 char| Card expiration date                     | No       | Date format string           |
| CARD-ACTIVE-STATUS      | `X(01)`          | Alpha         | 1 char | Card status (Y=Active, N=Inactive)       | No       | Values: Y, N                 |
| FILLER                  | `X(59)`          | Padding       | 59     | Reserved space                           | ---      | Pad to 150 bytes             |

**Modernization Target**: `CreditCard` JPA entity / `credit_cards` database table

---

## 3. Card-Account Cross-Reference (`CVACT03Y`)

**Business Purpose**: Links cards to customers and accounts. One customer can have multiple cards across multiple accounts.

| Field Name              | PIC Clause       | Data Type     | Size   | Business Description                     | Nullable | Constraints                  |
|-------------------------|------------------|---------------|--------|------------------------------------------|----------|------------------------------|
| XREF-CARD-NUM           | `X(16)`          | Alpha         | 16 char| **Primary Key** - Card number            | No       | References CVACT02Y.CARD-NUM |
| XREF-CUST-ID            | `9(09)`          | Numeric       | 9 digits| **FK** - Customer ID                    | No       | References CVCUS01Y.CUST-ID  |
| XREF-ACCT-ID            | `9(11)`          | Numeric       | 11 digits| **FK** - Account ID                    | No       | References CVACT01Y.ACCT-ID  |
| FILLER                  | `X(14)`          | Padding       | 14     | Reserved space                           | ---      | Pad to 50 bytes              |

**Modernization Target**: `CardCrossReference` JPA entity / `card_xref` database table (or a join table)

---

## 4. Customer Master (`CVCUS01Y`)

**Business Purpose**: Stores customer personal and contact information. Core identity entity for the system.

| Field Name                   | PIC Clause  | Data Type     | Size   | Business Description                   | Nullable | Constraints                  |
|------------------------------|-------------|---------------|--------|----------------------------------------|----------|------------------------------|
| CUST-ID                      | `9(09)`     | Numeric       | 9 digits| **Primary Key** - Customer ID         | No       | Unique, 9-digit integer      |
| CUST-FIRST-NAME              | `X(25)`     | Alpha         | 25 char| First name                             | No       |                              |
| CUST-MIDDLE-NAME             | `X(25)`     | Alpha         | 25 char| Middle name                            | Yes      |                              |
| CUST-LAST-NAME               | `X(25)`     | Alpha         | 25 char| Last name                              | No       |                              |
| CUST-ADDR-LINE-1             | `X(50)`     | Alpha         | 50 char| Address line 1                         | No       |                              |
| CUST-ADDR-LINE-2             | `X(50)`     | Alpha         | 50 char| Address line 2                         | Yes      |                              |
| CUST-ADDR-LINE-3             | `X(50)`     | Alpha         | 50 char| Address line 3                         | Yes      |                              |
| CUST-ADDR-STATE-CD           | `X(02)`     | Alpha         | 2 char | US state code                          | No       | 2-letter state abbreviation  |
| CUST-ADDR-COUNTRY-CD         | `X(03)`     | Alpha         | 3 char | Country code                           | No       | ISO 3166 alpha-3             |
| CUST-ADDR-ZIP                | `X(10)`     | Alpha         | 10 char| ZIP / postal code                      | No       | US ZIP or ZIP+4              |
| CUST-PHONE-NUM-1             | `X(15)`     | Alpha         | 15 char| Primary phone number                   | No       |                              |
| CUST-PHONE-NUM-2             | `X(15)`     | Alpha         | 15 char| Secondary phone number                 | Yes      |                              |
| CUST-SSN                     | `9(09)`     | Numeric       | 9 digits| Social Security Number (PII)          | No       | 9-digit, sensitive           |
| CUST-GOVT-ISSUED-ID          | `X(20)`     | Alpha         | 20 char| Government-issued ID number (PII)      | Yes      | Sensitive                    |
| CUST-DOB-YYYY-MM-DD          | `X(10)`     | Text Date     | 10 char| Date of birth (PII)                    | No       | YYYY-MM-DD format            |
| CUST-EFT-ACCOUNT-ID          | `X(10)`     | Alpha         | 10 char| EFT / bank account ID for payments     | Yes      | Linked bank account          |
| CUST-PRI-CARD-HOLDER-IND     | `X(01)`     | Alpha         | 1 char | Primary cardholder indicator           | No       | Y/N flag                     |
| CUST-FICO-CREDIT-SCORE       | `9(03)`     | Numeric       | 3 digits| FICO credit score                     | No       | Range 300-850                |
| FILLER                       | `X(168)`    | Padding       | 168    | Reserved space                         | ---      | Pad to 500 bytes             |

**PII Fields**: CUST-SSN, CUST-GOVT-ISSUED-ID, CUST-DOB-YYYY-MM-DD, CUST-PHONE-NUM-*, CUST-ADDR-*

**Modernization Target**: `Customer` JPA entity / `customers` database table

---

## 5. Transaction - Online (`CVTRA05Y`)

**Business Purpose**: Stores individual credit card transactions including purchase details and merchant information.

| Field Name              | PIC Clause       | Data Type     | Size    | Business Description                    | Nullable | Constraints                  |
|-------------------------|------------------|---------------|---------|-----------------------------------------|----------|------------------------------|
| TRAN-ID                 | `X(16)`          | Alpha         | 16 char | **Primary Key** - Transaction ID        | No       | Unique transaction identifier|
| TRAN-TYPE-CD            | `X(02)`          | Alpha         | 2 char  | Transaction type code                   | No       | FK to CVTRA03Y.TRAN-TYPE     |
| TRAN-CAT-CD             | `9(04)`          | Numeric       | 4 digits| Transaction category code               | No       | FK to CVTRA04Y               |
| TRAN-SOURCE             | `X(10)`          | Alpha         | 10 char | Transaction source/channel              | No       | e.g., POS, ATM, ONLINE       |
| TRAN-DESC               | `X(100)`         | Alpha         | 100 char| Transaction description                 | Yes      | Free-text description        |
| TRAN-AMT                | `S9(09)V99`      | Signed Decimal| 11,2    | Transaction amount                      | No       | Max +/-999,999,999.99        |
| TRAN-MERCHANT-ID        | `9(09)`          | Numeric       | 9 digits| Merchant identifier                     | No       |                              |
| TRAN-MERCHANT-NAME      | `X(50)`          | Alpha         | 50 char | Merchant name                           | No       |                              |
| TRAN-MERCHANT-CITY      | `X(50)`          | Alpha         | 50 char | Merchant city                           | No       |                              |
| TRAN-MERCHANT-ZIP       | `X(10)`          | Alpha         | 10 char | Merchant ZIP code                       | No       |                              |
| TRAN-CARD-NUM           | `X(16)`          | Alpha         | 16 char | **FK** - Card used for transaction      | No       | References CVACT02Y.CARD-NUM |
| TRAN-ORIG-TS            | `X(26)`          | Timestamp     | 26 char | Origination timestamp                   | No       | YYYY-MM-DD-HH.MM.SS.NNNNNN  |
| TRAN-PROC-TS            | `X(26)`          | Timestamp     | 26 char | Processing timestamp                    | No       | YYYY-MM-DD-HH.MM.SS.NNNNNN  |
| FILLER                  | `X(20)`          | Padding       | 20      | Reserved space                          | ---      | Pad to 350 bytes             |

**Modernization Target**: `Transaction` JPA entity / `transactions` database table

---

## 6. Daily Transaction (`CVTRA06Y`)

**Business Purpose**: Staging file for daily batch-submitted transactions before posting to the master transaction file.

| Field Name              | PIC Clause       | Data Type     | Size    | Business Description                    |
|-------------------------|------------------|---------------|---------|-----------------------------------------|
| DALYTRAN-ID             | `X(16)`          | Alpha         | 16 char | Transaction ID                          |
| DALYTRAN-TYPE-CD        | `X(02)`          | Alpha         | 2 char  | Transaction type code                   |
| DALYTRAN-CAT-CD         | `9(04)`          | Numeric       | 4 digits| Transaction category code               |
| DALYTRAN-SOURCE         | `X(10)`          | Alpha         | 10 char | Transaction source                      |
| DALYTRAN-DESC           | `X(100)`         | Alpha         | 100 char| Transaction description                 |
| DALYTRAN-AMT            | `S9(09)V99`      | Signed Decimal| 11,2    | Transaction amount                      |
| DALYTRAN-MERCHANT-ID    | `9(09)`          | Numeric       | 9 digits| Merchant identifier                     |
| DALYTRAN-MERCHANT-NAME  | `X(50)`          | Alpha         | 50 char | Merchant name                           |
| DALYTRAN-MERCHANT-CITY  | `X(50)`          | Alpha         | 50 char | Merchant city                           |
| DALYTRAN-MERCHANT-ZIP   | `X(10)`          | Alpha         | 10 char | Merchant ZIP code                       |
| DALYTRAN-CARD-NUM       | `X(16)`          | Alpha         | 16 char | Card number                             |
| DALYTRAN-ORIG-TS        | `X(26)`          | Timestamp     | 26 char | Origination timestamp                   |
| DALYTRAN-PROC-TS        | `X(26)`          | Timestamp     | 26 char | Processing timestamp                    |
| FILLER                  | `X(20)`          | Padding       | 20      | Reserved space                          |

**Note**: Identical layout to CVTRA05Y. In the modernized system, this would be a staging table or message queue rather than a separate entity.

---

## 7. Transaction Category Balance (`CVTRA01Y`)

**Business Purpose**: Aggregated balance per account, transaction type, and category. Used by the interest calculation batch.

| Field Name              | PIC Clause       | Data Type     | Size    | Business Description                    |
|-------------------------|------------------|---------------|---------|-----------------------------------------|
| TRANCAT-ACCT-ID         | `9(11)`          | Numeric       | 11 digits | Account ID (composite key part 1)     |
| TRANCAT-TYPE-CD         | `X(02)`          | Alpha         | 2 char  | Transaction type code (key part 2)      |
| TRANCAT-CD              | `9(04)`          | Numeric       | 4 digits| Transaction category code (key part 3)  |
| TRAN-CAT-BAL            | `S9(09)V99`      | Signed Decimal| 11,2    | Aggregated balance for this category    |
| FILLER                  | `X(22)`          | Padding       | 22      | Reserved space                          |

**Modernization Target**: Could be a materialized view or computed field in the `accounts` table.

---

## 8. Discount/Interest Rate Group (`CVTRA02Y`)

**Business Purpose**: Interest rate lookup by account group, transaction type, and category. Drives interest calculations.

| Field Name              | PIC Clause       | Data Type     | Size    | Business Description                    |
|-------------------------|------------------|---------------|---------|-----------------------------------------|
| DIS-ACCT-GROUP-ID       | `X(10)`          | Alpha         | 10 char | Account group (composite key part 1)    |
| DIS-TRAN-TYPE-CD        | `X(02)`          | Alpha         | 2 char  | Transaction type (key part 2)           |
| DIS-TRAN-CAT-CD         | `9(04)`          | Numeric       | 4 digits| Category code (key part 3)              |
| DIS-INT-RATE            | `S9(04)V99`      | Signed Decimal| 6,2     | Interest rate percentage                |
| FILLER                  | `X(28)`          | Padding       | 28      | Reserved space                          |

**Modernization Target**: `InterestRate` configuration entity / `interest_rates` table

---

## 9. Transaction Type (`CVTRA03Y`)

**Business Purpose**: Reference table of transaction type codes and descriptions (e.g., "SA" = Sale, "RT" = Return).

| Field Name              | PIC Clause       | Data Type     | Size    | Business Description                    |
|-------------------------|------------------|---------------|---------|-----------------------------------------|
| TRAN-TYPE               | `X(02)`          | Alpha         | 2 char  | **Primary Key** - Type code             |
| TRAN-TYPE-DESC          | `X(50)`          | Alpha         | 50 char | Human-readable description              |
| FILLER                  | `X(08)`          | Padding       | 8       | Reserved space                          |

**Modernization Target**: `TransactionType` enum or reference table / `transaction_types` table

---

## 10. Transaction Category Type (`CVTRA04Y`)

**Business Purpose**: Reference table of transaction category codes within a type. More granular than transaction type.

| Field Name              | PIC Clause       | Data Type     | Size    | Business Description                    |
|-------------------------|------------------|---------------|---------|-----------------------------------------|
| TRAN-TYPE-CD            | `X(02)`          | Alpha         | 2 char  | Transaction type (composite key part 1) |
| TRAN-CAT-CD             | `9(04)`          | Numeric       | 4 digits| Category code (composite key part 2)    |
| TRAN-CAT-TYPE-DESC      | `X(50)`          | Alpha         | 50 char | Category description                    |
| FILLER                  | `X(04)`          | Padding       | 4       | Reserved space                          |

**Modernization Target**: `TransactionCategory` reference table / `transaction_categories` table

---

## 11. User Security (`CSUSR01Y`)

**Business Purpose**: Application user credentials and role assignment. Supports Regular (U) and Admin (A) roles.

| Field Name              | PIC Clause       | Data Type     | Size    | Business Description                    |
|-------------------------|------------------|---------------|---------|-----------------------------------------|
| SEC-USR-ID              | `X(08)`          | Alpha         | 8 char  | **Primary Key** - User ID               |
| SEC-USR-FNAME           | `X(20)`          | Alpha         | 20 char | First name                              |
| SEC-USR-LNAME           | `X(20)`          | Alpha         | 20 char | Last name                               |
| SEC-USR-PWD             | `X(08)`          | Alpha         | 8 char  | Password (plaintext - security risk)    |
| SEC-USR-TYPE            | `X(01)`          | Alpha         | 1 char  | User type: A=Admin, U=Regular User      |
| SEC-USR-FILLER          | `X(23)`          | Padding       | 23      | Reserved space                          |

**Security Note**: Passwords stored in plaintext. Modernized system must use hashed passwords with salt.

**Modernization Target**: `AppUser` JPA entity / `app_users` table with Spring Security integration

---

## 12. Common Communication Area (`COCOM01Y`)

**Business Purpose**: CICS COMMAREA used to pass context between online programs during screen navigation.

| Field Name              | PIC Clause       | Data Type | Size    | Business Description                    |
|-------------------------|------------------|-----------|---------|-----------------------------------------|
| CDEMO-FROM-TRANID       | `X(04)`          | Alpha     | 4 char  | Source CICS transaction ID              |
| CDEMO-FROM-PROGRAM      | `X(08)`          | Alpha     | 8 char  | Source program name                     |
| CDEMO-TO-TRANID         | `X(04)`          | Alpha     | 4 char  | Target CICS transaction ID              |
| CDEMO-TO-PROGRAM        | `X(08)`          | Alpha     | 8 char  | Target program name                     |
| CDEMO-USER-ID           | `X(08)`          | Alpha     | 8 char  | Logged-in user ID                       |
| CDEMO-USER-TYPE         | `X(01)`          | Alpha     | 1 char  | User type (A/U)                         |
| CDEMO-PGM-CONTEXT       | `9(01)`          | Numeric   | 1 digit | Program context flag                    |
| CDEMO-CUST-ID           | `9(09)`          | Numeric   | 9 digits| Selected customer ID                    |
| CDEMO-CUST-FNAME        | `X(25)`          | Alpha     | 25 char | Customer first name                     |
| CDEMO-CUST-MNAME        | `X(25)`          | Alpha     | 25 char | Customer middle name                    |
| CDEMO-CUST-LNAME        | `X(25)`          | Alpha     | 25 char | Customer last name                      |
| CDEMO-ACCT-ID           | `9(11)`          | Numeric   | 11 digits| Selected account ID                    |
| CDEMO-ACCT-STATUS       | `X(01)`          | Alpha     | 1 char  | Selected account status                 |
| CDEMO-CARD-NUM          | `9(16)`          | Numeric   | 16 digits| Selected card number                   |
| CDEMO-LAST-MAP          | `X(7)`           | Alpha     | 7 char  | Last displayed BMS map                  |
| CDEMO-LAST-MAPSET       | `X(7)`           | Alpha     | 7 char  | Last displayed BMS mapset               |

**Modernization Target**: HTTP session state or JWT claims in a Spring Boot application

---

## 13. Authorization Module Entities (Optional)

### 13.1 Authorization Request (`CCPAURQY`)

| Field Name                    | PIC Clause       | Data Type     | Size    | Business Description               |
|-------------------------------|------------------|---------------|---------|------------------------------------|
| PA-RQ-AUTH-DATE               | `X(06)`          | Alpha         | 6 char  | Authorization date (YYMMDD)        |
| PA-RQ-AUTH-TIME               | `X(06)`          | Alpha         | 6 char  | Authorization time (HHMMSS)        |
| PA-RQ-CARD-NUM                | `X(16)`          | Alpha         | 16 char | Card number                        |
| PA-RQ-AUTH-TYPE               | `X(04)`          | Alpha         | 4 char  | Authorization type                 |
| PA-RQ-CARD-EXPIRY-DATE        | `X(04)`          | Alpha         | 4 char  | Card expiry (YYMM)                 |
| PA-RQ-MESSAGE-TYPE            | `X(06)`          | Alpha         | 6 char  | Message type indicator             |
| PA-RQ-MESSAGE-SOURCE          | `X(06)`          | Alpha         | 6 char  | Message source                     |
| PA-RQ-PROCESSING-CODE         | `9(06)`          | Numeric       | 6 digits| Processing code                    |
| PA-RQ-TRANSACTION-AMT         | `+9(10).99`      | Signed Decimal| 12,2    | Requested transaction amount       |
| PA-RQ-MERCHANT-CATAGORY-CODE  | `X(04)`          | Alpha         | 4 char  | Merchant Category Code (MCC)       |
| PA-RQ-ACQR-COUNTRY-CODE       | `X(03)`          | Alpha         | 3 char  | Acquirer country code              |
| PA-RQ-POS-ENTRY-MODE          | `9(02)`          | Numeric       | 2 digits| POS entry mode                     |
| PA-RQ-MERCHANT-ID             | `X(15)`          | Alpha         | 15 char | Merchant ID                        |
| PA-RQ-MERCHANT-NAME           | `X(22)`          | Alpha         | 22 char | Merchant name                      |
| PA-RQ-MERCHANT-CITY           | `X(13)`          | Alpha         | 13 char | Merchant city                      |
| PA-RQ-MERCHANT-STATE          | `X(02)`          | Alpha         | 2 char  | Merchant state                     |
| PA-RQ-MERCHANT-ZIP            | `X(09)`          | Alpha         | 9 char  | Merchant ZIP                       |
| PA-RQ-TRANSACTION-ID          | `X(15)`          | Alpha         | 15 char | Transaction ID                     |

### 13.2 Authorization Response (`CCPAURLY`)

| Field Name                    | PIC Clause       | Data Type     | Size    | Business Description               |
|-------------------------------|------------------|---------------|---------|------------------------------------|
| PA-RL-CARD-NUM                | `X(16)`          | Alpha         | 16 char | Card number                        |
| PA-RL-TRANSACTION-ID          | `X(15)`          | Alpha         | 15 char | Transaction ID                     |
| PA-RL-AUTH-ID-CODE            | `X(06)`          | Alpha         | 6 char  | Authorization ID code              |
| PA-RL-AUTH-RESP-CODE          | `X(02)`          | Alpha         | 2 char  | Response code (00=Approved)        |
| PA-RL-AUTH-RESP-REASON        | `X(04)`          | Alpha         | 4 char  | Response reason code               |
| PA-RL-APPROVED-AMT            | `+9(10).99`      | Signed Decimal| 12,2    | Approved amount                    |

### 13.3 Authorization Detail (IMS) (`CIPAUDTY`)

| Field Name                    | PIC Clause       | Data Type        | Size    | Business Description             |
|-------------------------------|------------------|------------------|---------|----------------------------------|
| PA-AUTH-DATE-9C               | `S9(05) COMP-3`  | Packed Decimal   | 3 bytes | Auth date (packed)               |
| PA-AUTH-TIME-9C               | `S9(09) COMP-3`  | Packed Decimal   | 5 bytes | Auth time (packed)               |
| PA-AUTH-ORIG-DATE             | `X(06)`          | Alpha            | 6 char  | Original date                    |
| PA-AUTH-ORIG-TIME             | `X(06)`          | Alpha            | 6 char  | Original time                    |
| PA-CARD-NUM                   | `X(16)`          | Alpha            | 16 char | Card number                      |
| PA-AUTH-TYPE                  | `X(04)`          | Alpha            | 4 char  | Authorization type               |
| PA-CARD-EXPIRY-DATE           | `X(04)`          | Alpha            | 4 char  | Card expiry                      |
| PA-MESSAGE-TYPE               | `X(06)`          | Alpha            | 6 char  | Message type                     |
| PA-MESSAGE-SOURCE             | `X(06)`          | Alpha            | 6 char  | Message source                   |
| PA-AUTH-ID-CODE               | `X(06)`          | Alpha            | 6 char  | Auth ID code                     |
| PA-AUTH-RESP-CODE             | `X(02)`          | Alpha            | 2 char  | Response code                    |
| PA-AUTH-RESP-REASON           | `X(04)`          | Alpha            | 4 char  | Response reason                  |
| PA-PROCESSING-CODE            | `9(06)`          | Numeric          | 6 digits| Processing code                  |
| PA-TRANSACTION-AMT            | `S9(10)V99 COMP-3`| Packed Decimal  | 7 bytes | Transaction amount               |
| PA-APPROVED-AMT               | `S9(10)V99 COMP-3`| Packed Decimal  | 7 bytes | Approved amount                  |
| PA-MERCHANT-CATAGORY-CODE     | `X(04)`          | Alpha            | 4 char  | MCC code                         |
| PA-ACQR-COUNTRY-CODE          | `X(03)`          | Alpha            | 3 char  | Acquirer country                 |
| PA-POS-ENTRY-MODE             | `9(02)`          | Numeric          | 2 digits| POS entry mode                   |
| PA-MERCHANT-ID                | `X(15)`          | Alpha            | 15 char | Merchant ID                      |
| PA-MERCHANT-NAME              | `X(22)`          | Alpha            | 22 char | Merchant name                    |
| PA-MERCHANT-CITY              | `X(13)`          | Alpha            | 13 char | Merchant city                    |
| PA-MERCHANT-STATE             | `X(02)`          | Alpha            | 2 char  | Merchant state                   |
| PA-MERCHANT-ZIP               | `X(09)`          | Alpha            | 9 char  | Merchant ZIP                     |
| PA-TRANSACTION-ID             | `X(15)`          | Alpha            | 15 char | Transaction ID                   |
| PA-MATCH-STATUS               | `X(01)`          | Alpha            | 1 char  | Match status flag                |
| PA-AUTH-FRAUD                 | `X(01)`          | Alpha            | 1 char  | Fraud flag (Y/N)                 |
| PA-FRAUD-RPT-DATE             | `X(08)`          | Alpha            | 8 char  | Fraud report date                |

### 13.4 Authorization Summary (IMS) (`CIPAUSMY`)

| Field Name                    | PIC Clause         | Data Type       | Size    | Business Description             |
|-------------------------------|--------------------|-----------------|---------|----------------------------------|
| PA-ACCT-ID                    | `S9(11) COMP-3`    | Packed Decimal  | 6 bytes | Account ID                       |
| PA-CUST-ID                    | `9(09)`            | Numeric         | 9 digits| Customer ID                      |
| PA-AUTH-STATUS                | `X(01)`            | Alpha           | 1 char  | Authorization status             |
| PA-ACCOUNT-STATUS             | `X(02) OCCURS 5`   | Alpha Array     | 10 char | Account status history (5 entries)|
| PA-CREDIT-LIMIT               | `S9(09)V99 COMP-3` | Packed Decimal | 6 bytes | Credit limit                     |
| PA-CASH-LIMIT                 | `S9(09)V99 COMP-3` | Packed Decimal | 6 bytes | Cash limit                       |
| PA-CREDIT-BALANCE             | `S9(09)V99 COMP-3` | Packed Decimal | 6 bytes | Credit balance                   |
| PA-CASH-BALANCE               | `S9(09)V99 COMP-3` | Packed Decimal | 6 bytes | Cash balance                     |
| PA-APPROVED-AUTH-CNT          | `S9(04) COMP`      | Binary          | 2 bytes | Approved auth count              |
| PA-DECLINED-AUTH-CNT          | `S9(04) COMP`      | Binary          | 2 bytes | Declined auth count              |
| PA-APPROVED-AUTH-AMT          | `S9(09)V99 COMP-3` | Packed Decimal | 6 bytes | Approved auth total amount       |
| PA-DECLINED-AUTH-AMT          | `S9(09)V99 COMP-3` | Packed Decimal | 6 bytes | Declined auth total amount       |

---

## 14. PIC Clause Quick Reference

For developers unfamiliar with COBOL PIC clauses:

| PIC Clause      | Java Equivalent     | Description                                          |
|-----------------|---------------------|------------------------------------------------------|
| `X(n)`          | `String`            | Alphanumeric, n characters                           |
| `9(n)`          | `long` / `int`      | Unsigned numeric, n digits                           |
| `S9(n)`         | `long` / `int`      | Signed numeric, n digits                             |
| `S9(n)V99`      | `BigDecimal`        | Signed decimal, n integer digits + 2 decimal places  |
| `S9(n)V99 COMP-3` | `BigDecimal`     | Packed decimal (BCD), same logical value             |
| `S9(n) COMP`    | `short` / `int`     | Binary (halfword or fullword)                        |
| `S9(n) COMP-3`  | `BigDecimal`        | Packed decimal (BCD encoding)                        |
| `9(n) COMP`     | `int`               | Unsigned binary                                      |

---

## 15. Entity Relationship Summary

```
Customer (CVCUS01Y)
  |
  +-- 1:N --> CardXRef (CVACT03Y)
  |               |
  |               +-- N:1 --> Account (CVACT01Y)
  |               |               |
  |               |               +-- 1:N --> TransCatBal (CVTRA01Y)
  |               |               +-- N:1 --> DiscountGrp (CVTRA02Y) via ACCT-GROUP-ID
  |               |
  |               +-- N:1 --> CreditCard (CVACT02Y)
  |                               |
  |                               +-- 1:N --> Transaction (CVTRA05Y) via TRAN-CARD-NUM
  |
  +-- (via CardXRef) --> AuthSummary (CIPAUSMY) [Optional]
                              |
                              +-- 1:N --> AuthDetail (CIPAUDTY) [Optional]

UserSecurity (CSUSR01Y) -- standalone authentication entity

TransactionType (CVTRA03Y) -- reference lookup
TransactionCategory (CVTRA04Y) -- reference lookup
```
