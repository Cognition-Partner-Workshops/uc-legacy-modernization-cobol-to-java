# CardDemo Data Dictionary

> Business-friendly extraction of all data entities, fields, and their types from the CardDemo COBOL copybook PIC clauses. Designed to support modernization planning, database schema design, and Java POJO generation.

---

## Table of Contents

1. [Data Type Reference](#data-type-reference)
2. [Account Master (CVACT01Y)](#account-master-cvact01y)
3. [Card Data (CVACT02Y)](#card-data-cvact02y)
4. [Card Cross-Reference (CVACT03Y)](#card-cross-reference-cvact03y)
5. [Card Record Alternate (CVCRD01Y)](#card-record-alternate-cvcrd01y)
6. [Customer Master (CVCUS01Y)](#customer-master-cvcus01y)
7. [Customer Record Alternate (CUSTREC)](#customer-record-alternate-custrec)
8. [Transaction Record (CVTRA05Y)](#transaction-record-cvtra05y)
9. [Daily Transaction Record (CVTRA06Y)](#daily-transaction-record-cvtra06y)
10. [Transaction for Statements (COSTM01)](#transaction-for-statements-costm01)
11. [Transaction Category Balance (CVTRA01Y)](#transaction-category-balance-cvtra01y)
12. [Disclosure Group (CVTRA02Y)](#disclosure-group-cvtra02y)
13. [Transaction Type (CVTRA03Y)](#transaction-type-cvtra03y)
14. [Transaction Category (CVTRA04Y)](#transaction-category-cvtra04y)
15. [Transaction Report Layout (CVTRA07Y)](#transaction-report-layout-cvtra07y)
16. [User Security Record (CSUSR01Y)](#user-security-record-csusr01y)
17. [Export/Import Record (CVEXPORT)](#exportimport-record-cvexport)
18. [Common Communication Area (COCOM01Y)](#common-communication-area-cocom01y)
19. [Menu Definitions (COMEN02Y / COADM02Y)](#menu-definitions)
20. [Screen Title/Header (COTTL01Y)](#screen-titleheader-cottl01y)
21. [System Utility Copybooks](#system-utility-copybooks)
22. [VSAM File Summary](#vsam-file-summary)
23. [Entity Relationship Summary](#entity-relationship-summary)

---

## Data Type Reference

| COBOL PIC Clause        | Business Meaning            | Java Equivalent      | SQL Equivalent         |
|-------------------------|-----------------------------|----------------------|------------------------|
| `PIC X(n)`              | Alphanumeric string, n chars| `String`             | `VARCHAR(n)`           |
| `PIC 9(n)`              | Unsigned integer, n digits  | `long` / `int`       | `NUMERIC(n)`           |
| `PIC S9(n)V99`          | Signed decimal, 2 dec places| `BigDecimal`         | `DECIMAL(n+2, 2)`     |
| `PIC S9(n)V9(m)`        | Signed decimal, m dec places| `BigDecimal`         | `DECIMAL(n+m, m)`     |
| `PIC 9(n) COMP`         | Binary integer              | `int` / `long`       | `INTEGER` / `BIGINT`  |
| `PIC X(01)` (flag)      | Single character flag/code  | `char` / `String`    | `CHAR(1)`             |

---

## Account Master (CVACT01Y)

**Copybook:** `app/cpy/CVACT01Y.cpy` | **Record Length:** 300 bytes | **VSAM File:** `ACCTDATA.VSAM.KSDS`

This is the core account record storing credit card account details including balances, limits, and status.

| # | COBOL Field Name              | PIC Clause       | Offset | Len | Business Name               | Business Description                        | Java Type     | Nullable |
|---|-------------------------------|------------------|--------|-----|-----------------------------|---------------------------------------------|---------------|----------|
| 1 | ACCT-ID                       | 9(11)            | 0      | 11  | Account ID                  | Unique account identifier (primary key)     | `long`        | No       |
| 2 | ACCT-ACTIVE-STATUS            | X(01)            | 11     | 1   | Account Status              | Active status flag (Y/N)                    | `String`      | No       |
| 3 | ACCT-CURR-BAL                 | S9(10)V99        | 12     | 12  | Current Balance             | Current account balance                     | `BigDecimal`  | No       |
| 4 | ACCT-CREDIT-LIMIT             | S9(10)V99        | 24     | 12  | Credit Limit                | Maximum credit limit                        | `BigDecimal`  | No       |
| 5 | ACCT-CASH-CREDIT-LIMIT        | S9(10)V99        | 36     | 12  | Cash Credit Limit           | Cash advance credit limit                   | `BigDecimal`  | No       |
| 6 | ACCT-OPEN-DATE                | X(10)            | 48     | 10  | Open Date                   | Date account was opened (YYYY-MM-DD)        | `LocalDate`   | No       |
| 7 | ACCT-EXPIRAION-DATE           | X(10)            | 58     | 10  | Expiration Date             | Account expiration date (YYYY-MM-DD)        | `LocalDate`   | No       |
| 8 | ACCT-REISSUE-DATE             | X(10)            | 68     | 10  | Reissue Date                | Date account was last reissued              | `LocalDate`   | Yes      |
| 9 | ACCT-CURR-CYC-CREDIT          | S9(10)V99        | 78     | 12  | Current Cycle Credit        | Credits applied in current billing cycle    | `BigDecimal`  | No       |
| 10| ACCT-CURR-CYC-DEBIT           | S9(10)V99        | 90     | 12  | Current Cycle Debit         | Debits applied in current billing cycle     | `BigDecimal`  | No       |
| 11| ACCT-ADDR-ZIP                 | X(10)            | 102    | 10  | ZIP Code                    | Account holder ZIP/postal code              | `String`      | No       |
| 12| ACCT-GROUP-ID                 | X(10)            | 112    | 10  | Account Group ID            | Disclosure/interest rate group identifier   | `String`      | No       |
| 13| FILLER                        | X(178)           | 122    | 178 | (Reserved)                  | Reserved for future use                     | --            | --       |

**Key Business Rules:**
- Primary key: `ACCT-ID`
- `ACCT-GROUP-ID` links to `CVTRA02Y` (Disclosure Group) for interest rate lookup
- `ACCT-CURR-BAL` must not exceed `ACCT-CREDIT-LIMIT`

---

## Card Data (CVACT02Y)

**Copybook:** `app/cpy/CVACT02Y.cpy` | **Record Length:** 150 bytes | **VSAM File:** `CARDDATA.VSAM.KSDS`

Stores credit card details linked to accounts.

| # | COBOL Field Name              | PIC Clause       | Len | Business Name               | Business Description                        | Java Type     |
|---|-------------------------------|------------------|-----|-----------------------------|---------------------------------------------|---------------|
| 1 | CARD-NUM                      | X(16)            | 16  | Card Number                 | 16-digit credit card number (primary key)   | `String`      |
| 2 | CARD-ACCT-ID                  | 9(11)            | 11  | Account ID                  | Parent account ID (FK to CVACT01Y)          | `long`        |
| 3 | CARD-CVV-CD                   | 9(03)            | 3   | CVV Code                    | Card verification value                     | `int`         |
| 4 | CARD-EMBOSSED-NAME            | X(50)            | 50  | Embossed Name               | Name printed on card                        | `String`      |
| 5 | CARD-EXPIRAION-DATE           | X(10)            | 10  | Expiration Date             | Card expiration date (YYYY-MM-DD)           | `LocalDate`   |
| 6 | CARD-ACTIVE-STATUS            | X(01)            | 1   | Card Status                 | Active status flag (Y/N)                    | `String`      |
| 7 | FILLER                        | X(59)            | 59  | (Reserved)                  | Reserved for future use                     | --            |

**Key Business Rules:**
- Primary key: `CARD-NUM`
- Foreign key: `CARD-ACCT-ID` references `CVACT01Y.ACCT-ID`
- Multiple cards can belong to one account

---

## Card Cross-Reference (CVACT03Y)

**Copybook:** `app/cpy/CVACT03Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `CARDXREF.VSAM.KSDS`

Cross-reference between cards and accounts, with an alternate index on account ID.

| # | COBOL Field Name              | PIC Clause       | Len | Business Name               | Business Description                        | Java Type     |
|---|-------------------------------|------------------|-----|-----------------------------|---------------------------------------------|---------------|
| 1 | XREF-CARD-NUM                 | X(16)            | 16  | Card Number                 | Card number (primary key)                   | `String`      |
| 2 | XREF-CUST-ID                  | 9(09)            | 9   | Customer ID                 | Customer ID (FK to CVCUS01Y)                | `long`        |
| 3 | XREF-ACCT-ID                  | 9(11)            | 11  | Account ID                  | Account ID (FK to CVACT01Y)                 | `long`        |
| 4 | FILLER                        | X(14)            | 14  | (Reserved)                  | Reserved for future use                     | --            |

**Key Business Rules:**
- Primary key: `XREF-CARD-NUM`
- Alternate index on: `XREF-ACCT-ID` (non-unique, defined in XREFFILE.jcl)
- Links the three core entities: Card -> Customer -> Account

---

## Card Record Alternate (CVCRD01Y)

**Copybook:** `app/cpy/CVCRD01Y.cpy` | **Record Length:** 150 bytes

Alternate card record layout used by some programs. Mirrors CVACT02Y with slight naming differences.

| # | COBOL Field Name              | PIC Clause       | Len | Business Name               | Java Type     |
|---|-------------------------------|------------------|-----|-----------------------------|---------------|
| 1 | CARD-NUM                      | X(16)            | 16  | Card Number                 | `String`      |
| 2 | CARD-ACCT-ID                  | 9(11)            | 11  | Account ID                  | `long`        |
| 3 | CARD-CVV-CD                   | 9(03)            | 3   | CVV Code                    | `int`         |
| 4 | CARD-EMBOSSED-NAME            | X(50)            | 50  | Embossed Name               | `String`      |
| 5 | CARD-EXPIRAION-DATE           | X(10)            | 10  | Expiration Date             | `LocalDate`   |
| 6 | CARD-ACTIVE-STATUS            | X(01)            | 1   | Card Status                 | `String`      |
| 7 | FILLER                        | X(59)            | 59  | (Reserved)                  | --            |

---

## Customer Master (CVCUS01Y)

**Copybook:** `app/cpy/CVCUS01Y.cpy` | **Record Length:** 500 bytes | **VSAM File:** `CUSTDATA.VSAM.KSDS`

Comprehensive customer record with personal information, contact details, and demographic data.

| # | COBOL Field Name              | PIC Clause       | Len | Business Name               | Business Description                        | Java Type     |
|---|-------------------------------|------------------|-----|-----------------------------|---------------------------------------------|---------------|
| 1 | CUST-ID                       | 9(09)            | 9   | Customer ID                 | Unique customer identifier (primary key)    | `long`        |
| 2 | CUST-FIRST-NAME               | X(25)            | 25  | First Name                  | Customer first name                         | `String`      |
| 3 | CUST-MIDDLE-NAME              | X(25)            | 25  | Middle Name                 | Customer middle name                        | `String`      |
| 4 | CUST-LAST-NAME                | X(25)            | 25  | Last Name                   | Customer last name                          | `String`      |
| 5 | CUST-ADDR-LINE-1              | X(50)            | 50  | Address Line 1              | Primary street address                      | `String`      |
| 6 | CUST-ADDR-LINE-2              | X(50)            | 50  | Address Line 2              | Secondary address (suite, apt)              | `String`      |
| 7 | CUST-ADDR-LINE-3              | X(50)            | 50  | Address Line 3              | Tertiary address line                       | `String`      |
| 8 | CUST-ADDR-STATE-CD            | X(02)            | 2   | State Code                  | US state code (e.g., NY, CA)                | `String`      |
| 9 | CUST-ADDR-COUNTRY-CD          | X(03)            | 3   | Country Code                | Country code (e.g., USA)                    | `String`      |
| 10| CUST-ADDR-ZIP                 | X(10)            | 10  | ZIP Code                    | ZIP/postal code                             | `String`      |
| 11| CUST-PHONE-NUM-1              | X(15)            | 15  | Primary Phone               | Primary phone number                        | `String`      |
| 12| CUST-PHONE-NUM-2              | X(15)            | 15  | Secondary Phone             | Secondary phone number                      | `String`      |
| 13| CUST-SSN                      | 9(09)            | 9   | Social Security Number      | Customer SSN (PII - sensitive)              | `String`      |
| 14| CUST-GOVT-ISSUED-ID           | X(20)            | 20  | Government ID               | Government-issued ID number                 | `String`      |
| 15| CUST-DOB-YYYY-MM-DD           | X(10)            | 10  | Date of Birth               | Date of birth (YYYY-MM-DD)                  | `LocalDate`   |
| 16| CUST-EFT-ACCOUNT-ID           | X(10)            | 10  | EFT Account ID              | Electronic funds transfer account           | `String`      |
| 17| CUST-PRI-CARD-HOLDER-IND      | X(01)            | 1   | Primary Cardholder Flag     | Primary cardholder indicator (Y/N)          | `String`      |
| 18| CUST-FICO-CREDIT-SCORE        | 9(03)            | 3   | FICO Score                  | Credit score (300-850)                      | `int`         |
| 19| FILLER                        | X(168)           | 168 | (Reserved)                  | Reserved for future use                     | --            |

**Key Business Rules:**
- Primary key: `CUST-ID`
- `CUST-SSN` is PII and must be encrypted/masked in modernized system
- `CUST-FICO-CREDIT-SCORE` valid range: 300-850
- Linked to accounts via `CVACT03Y` cross-reference

---

## Customer Record Alternate (CUSTREC)

**Copybook:** `app/cpy/CUSTREC.cpy` | **Record Length:** 500 bytes

Alternate customer layout used by some batch programs. Same structure as CVCUS01Y.

| # | COBOL Field Name              | PIC Clause       | Len | Business Name               | Java Type     |
|---|-------------------------------|------------------|-----|-----------------------------|---------------|
| 1 | CUST-ID                       | 9(09)            | 9   | Customer ID                 | `long`        |
| 2 | CUST-FIRST-NAME               | X(25)            | 25  | First Name                  | `String`      |
| 3 | CUST-MIDDLE-NAME              | X(25)            | 25  | Middle Name                 | `String`      |
| 4 | CUST-LAST-NAME                | X(25)            | 25  | Last Name                   | `String`      |
| 5 | CUST-ADDR-LINE-1              | X(50)            | 50  | Address Line 1              | `String`      |
| 6 | CUST-ADDR-LINE-2              | X(50)            | 50  | Address Line 2              | `String`      |
| 7 | CUST-ADDR-LINE-3              | X(50)            | 50  | Address Line 3              | `String`      |
| 8 | CUST-ADDR-STATE-CD            | X(02)            | 2   | State Code                  | `String`      |
| 9 | CUST-ADDR-COUNTRY-CD          | X(03)            | 3   | Country Code                | `String`      |
| 10| CUST-ADDR-ZIP                 | X(10)            | 10  | ZIP Code                    | `String`      |
| 11| CUST-PHONE-NUM-1              | X(15)            | 15  | Primary Phone               | `String`      |
| 12| CUST-PHONE-NUM-2              | X(15)            | 15  | Secondary Phone             | `String`      |
| 13| CUST-SSN                      | 9(09)            | 9   | SSN                         | `String`      |
| 14| CUST-GOVT-ISSUED-ID           | X(20)            | 20  | Government ID               | `String`      |
| 15| CUST-DOB-YYYY-MM-DD           | X(10)            | 10  | Date of Birth               | `LocalDate`   |
| 16| CUST-EFT-ACCOUNT-ID           | X(10)            | 10  | EFT Account ID              | `String`      |
| 17| CUST-PRI-CARD-HOLDER-IND      | X(01)            | 1   | Primary Cardholder          | `String`      |
| 18| CUST-FICO-CREDIT-SCORE        | 9(03)            | 3   | FICO Score                  | `int`         |
| 19| FILLER                        | X(168)           | 168 | (Reserved)                  | --            |

---

## Transaction Record (CVTRA05Y)

**Copybook:** `app/cpy/CVTRA05Y.cpy` | **Record Length:** 350 bytes | **VSAM File:** `TRANSACT.VSAM.KSDS`

The core transaction record for all posted credit card transactions.

| # | COBOL Field Name              | PIC Clause       | Len | Business Name               | Business Description                        | Java Type       |
|---|-------------------------------|------------------|-----|-----------------------------|---------------------------------------------|-----------------|
| 1 | TRAN-ID                       | X(16)            | 16  | Transaction ID              | Unique transaction identifier (primary key) | `String`        |
| 2 | TRAN-TYPE-CD                  | X(02)            | 2   | Transaction Type Code       | Type code (FK to CVTRA03Y)                  | `String`        |
| 3 | TRAN-CAT-CD                   | 9(04)            | 4   | Category Code               | Category code (FK to CVTRA04Y)              | `int`           |
| 4 | TRAN-SOURCE                   | X(10)            | 10  | Transaction Source          | Origin of transaction (POS, ATM, Online)    | `String`        |
| 5 | TRAN-DESC                     | X(100)           | 100 | Description                 | Transaction description                     | `String`        |
| 6 | TRAN-AMT                      | S9(09)V99        | 11  | Amount                      | Transaction amount (signed)                 | `BigDecimal`    |
| 7 | TRAN-MERCHANT-ID              | 9(09)            | 9   | Merchant ID                 | Unique merchant identifier                  | `long`          |
| 8 | TRAN-MERCHANT-NAME            | X(50)            | 50  | Merchant Name               | Name of merchant                            | `String`        |
| 9 | TRAN-MERCHANT-CITY            | X(50)            | 50  | Merchant City               | City of merchant                            | `String`        |
| 10| TRAN-MERCHANT-ZIP             | X(10)            | 10  | Merchant ZIP                | Merchant ZIP/postal code                    | `String`        |
| 11| TRAN-CARD-NUM                 | X(16)            | 16  | Card Number                 | Card used for transaction                   | `String`        |
| 12| TRAN-ORIG-TS                  | X(26)            | 26  | Origination Timestamp       | When transaction was initiated              | `LocalDateTime` |
| 13| TRAN-PROC-TS                  | X(26)            | 26  | Processing Timestamp        | When transaction was processed              | `LocalDateTime` |
| 14| FILLER                        | X(20)            | 20  | (Reserved)                  | Reserved for future use                     | --              |

**Key Business Rules:**
- Primary key: `TRAN-ID`
- `TRAN-TYPE-CD` references `CVTRA03Y.TRAN-TYPE`
- `TRAN-CAT-CD` references `CVTRA04Y.TRAN-CAT-CD`
- `TRAN-CARD-NUM` references `CVACT02Y.CARD-NUM`
- `TRAN-AMT` can be negative (credits/refunds)

---

## Daily Transaction Record (CVTRA06Y)

**Copybook:** `app/cpy/CVTRA06Y.cpy` | **Record Length:** 350 bytes | **VSAM File:** `DALYTRAN` (daily input)

Incoming daily transactions before they are posted to the master transaction file. Same structure as CVTRA05Y with `DALYTRAN-` prefix.

| # | COBOL Field Name              | PIC Clause       | Len | Business Name               | Business Description                        | Java Type       |
|---|-------------------------------|------------------|-----|-----------------------------|---------------------------------------------|-----------------|
| 1 | DALYTRAN-ID                   | X(16)            | 16  | Transaction ID              | Daily transaction identifier                | `String`        |
| 2 | DALYTRAN-TYPE-CD              | X(02)            | 2   | Transaction Type Code       | Type code                                   | `String`        |
| 3 | DALYTRAN-CAT-CD               | 9(04)            | 4   | Category Code               | Category code                               | `int`           |
| 4 | DALYTRAN-SOURCE               | X(10)            | 10  | Transaction Source          | Origin of transaction                       | `String`        |
| 5 | DALYTRAN-DESC                 | X(100)           | 100 | Description                 | Transaction description                     | `String`        |
| 6 | DALYTRAN-AMT                  | S9(09)V99        | 11  | Amount                      | Transaction amount                          | `BigDecimal`    |
| 7 | DALYTRAN-MERCHANT-ID          | 9(09)            | 9   | Merchant ID                 | Merchant identifier                         | `long`          |
| 8 | DALYTRAN-MERCHANT-NAME        | X(50)            | 50  | Merchant Name               | Name of merchant                            | `String`        |
| 9 | DALYTRAN-MERCHANT-CITY        | X(50)            | 50  | Merchant City               | City of merchant                            | `String`        |
| 10| DALYTRAN-MERCHANT-ZIP         | X(10)            | 10  | Merchant ZIP                | Merchant ZIP code                           | `String`        |
| 11| DALYTRAN-CARD-NUM             | X(16)            | 16  | Card Number                 | Card used for transaction                   | `String`        |
| 12| DALYTRAN-ORIG-TS              | X(26)            | 26  | Origination Timestamp       | When transaction was initiated              | `LocalDateTime` |
| 13| DALYTRAN-PROC-TS              | X(26)            | 26  | Processing Timestamp        | When transaction was processed              | `LocalDateTime` |
| 14| FILLER                        | X(20)            | 20  | (Reserved)                  | Reserved for future use                     | --              |

---

## Transaction for Statements (COSTM01)

**Copybook:** `app/cpy/COSTM01.CPY` | **Record Length:** 350 bytes

Altered transaction layout with a composite key of card number + transaction ID, used for statement generation.

| # | COBOL Field Name              | PIC Clause       | Len | Business Name               | Business Description                        | Java Type       |
|---|-------------------------------|------------------|-----|-----------------------------|---------------------------------------------|-----------------|
| 1 | TRNX-CARD-NUM                 | X(16)            | 16  | Card Number                 | Card number (part of composite key)         | `String`        |
| 2 | TRNX-ID                       | X(16)            | 16  | Transaction ID              | Transaction ID (part of composite key)      | `String`        |
| 3 | TRNX-TYPE-CD                  | X(02)            | 2   | Transaction Type Code       | Type code                                   | `String`        |
| 4 | TRNX-CAT-CD                   | 9(04)            | 4   | Category Code               | Category code                               | `int`           |
| 5 | TRNX-SOURCE                   | X(10)            | 10  | Transaction Source          | Origin of transaction                       | `String`        |
| 6 | TRNX-DESC                     | X(100)           | 100 | Description                 | Transaction description                     | `String`        |
| 7 | TRNX-AMT                      | S9(09)V99        | 11  | Amount                      | Transaction amount                          | `BigDecimal`    |
| 8 | TRNX-MERCHANT-ID              | 9(09)            | 9   | Merchant ID                 | Merchant identifier                         | `long`          |
| 9 | TRNX-MERCHANT-NAME            | X(50)            | 50  | Merchant Name               | Name of merchant                            | `String`        |
| 10| TRNX-MERCHANT-CITY            | X(50)            | 50  | Merchant City               | City of merchant                            | `String`        |
| 11| TRNX-MERCHANT-ZIP             | X(10)            | 10  | Merchant ZIP                | Merchant ZIP code                           | `String`        |
| 12| TRNX-ORIG-TS                  | X(26)            | 26  | Origination Timestamp       | When transaction was initiated              | `LocalDateTime` |
| 13| TRNX-PROC-TS                  | X(26)            | 26  | Processing Timestamp        | When transaction was processed              | `LocalDateTime` |
| 14| FILLER                        | X(20)            | 20  | (Reserved)                  | Reserved for future use                     | --              |

**Key Difference from CVTRA05Y:** Composite primary key is `(TRNX-CARD-NUM, TRNX-ID)` instead of just `TRAN-ID`, enabling efficient statement generation sorted by card.

---

## Transaction Category Balance (CVTRA01Y)

**Copybook:** `app/cpy/CVTRA01Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `TCATBALF.VSAM.KSDS`

Running balance by account, transaction type, and category.

| # | COBOL Field Name              | PIC Clause       | Len | Business Name               | Business Description                        | Java Type       |
|---|-------------------------------|------------------|-----|-----------------------------|---------------------------------------------|-----------------|
| 1 | TRANCAT-ACCT-ID               | 9(11)            | 11  | Account ID                  | Account identifier (part of composite key)  | `long`          |
| 2 | TRANCAT-TYPE-CD               | X(02)            | 2   | Transaction Type Code       | Type code (part of composite key)           | `String`        |
| 3 | TRANCAT-CD                    | 9(04)            | 4   | Category Code               | Category code (part of composite key)       | `int`           |
| 4 | TRAN-CAT-BAL                  | S9(09)V99        | 11  | Category Balance            | Running balance for this category           | `BigDecimal`    |
| 5 | FILLER                        | X(22)            | 22  | (Reserved)                  | Reserved for future use                     | --              |

**Key:** Composite key `(TRANCAT-ACCT-ID, TRANCAT-TYPE-CD, TRANCAT-CD)`

---

## Disclosure Group (CVTRA02Y)

**Copybook:** `app/cpy/CVTRA02Y.cpy` | **Record Length:** 50 bytes | **VSAM File:** `DISCGRP.VSAM.KSDS`

Interest rate definitions per account group and transaction category.

| # | COBOL Field Name              | PIC Clause       | Len | Business Name               | Business Description                        | Java Type       |
|---|-------------------------------|------------------|-----|-----------------------------|---------------------------------------------|-----------------|
| 1 | DIS-ACCT-GROUP-ID             | X(10)            | 10  | Account Group ID            | Matches CVACT01Y.ACCT-GROUP-ID              | `String`        |
| 2 | DIS-TRAN-TYPE-CD              | X(02)            | 2   | Transaction Type Code       | Transaction type                            | `String`        |
| 3 | DIS-TRAN-CAT-CD               | 9(04)            | 4   | Transaction Category Code   | Transaction category                        | `int`           |
| 4 | DIS-INT-RATE                  | S9(04)V99        | 6   | Interest Rate               | Interest rate percentage                    | `BigDecimal`    |
| 5 | FILLER                        | X(28)            | 28  | (Reserved)                  | Reserved for future use                     | --              |

**Key:** Composite key `(DIS-ACCT-GROUP-ID, DIS-TRAN-TYPE-CD, DIS-TRAN-CAT-CD)`

**Business Rule:** Used by CBACT04C (interest calculation) to look up the applicable interest rate for each transaction category balance.

---

## Transaction Type (CVTRA03Y)

**Copybook:** `app/cpy/CVTRA03Y.cpy` | **Record Length:** 60 bytes | **VSAM File:** `TRANTYPE.VSAM.KSDS`

Reference table of transaction type codes and descriptions.

| # | COBOL Field Name              | PIC Clause       | Len | Business Name               | Business Description                        | Java Type       |
|---|-------------------------------|------------------|-----|-----------------------------|---------------------------------------------|-----------------|
| 1 | TRAN-TYPE                     | X(02)            | 2   | Transaction Type Code       | Type code (primary key, e.g., "01", "02")   | `String`        |
| 2 | TRAN-TYPE-DESC                | X(50)            | 50  | Type Description            | Human-readable description                  | `String`        |
| 3 | FILLER                        | X(08)            | 8   | (Reserved)                  | Reserved                                    | --              |

---

## Transaction Category (CVTRA04Y)

**Copybook:** `app/cpy/CVTRA04Y.cpy` | **Record Length:** 60 bytes | **VSAM File:** `TRANCATG.VSAM.KSDS`

Reference table of transaction category codes within each type.

| # | COBOL Field Name              | PIC Clause       | Len | Business Name               | Business Description                        | Java Type       |
|---|-------------------------------|------------------|-----|-----------------------------|---------------------------------------------|-----------------|
| 1 | TRAN-TYPE-CD                  | X(02)            | 2   | Transaction Type Code       | Parent type code (part of composite key)    | `String`        |
| 2 | TRAN-CAT-CD                   | 9(04)            | 4   | Category Code               | Category code (part of composite key)       | `int`           |
| 3 | TRAN-CAT-TYPE-DESC            | X(50)            | 50  | Category Description        | Human-readable description                  | `String`        |
| 4 | FILLER                        | X(04)            | 4   | (Reserved)                  | Reserved                                    | --              |

**Key:** Composite key `(TRAN-TYPE-CD, TRAN-CAT-CD)`

---

## Transaction Report Layout (CVTRA07Y)

**Copybook:** `app/cpy/CVTRA07Y.cpy` | **Record Length:** N/A (report formatting)

Report layout definitions for the Daily Transaction Report (CBTRN03C). Not a data entity but a report structure.

| # | Structure Name                 | Purpose                                                |
|---|-------------------------------|--------------------------------------------------------|
| 1 | REPORT-NAME-HEADER            | Report title, short name ("DALYREPT"), date range      |
| 2 | TRANSACTION-DETAIL-REPORT     | Detail line: Trans ID, Account, Type, Category, Amount |
| 3 | TRANSACTION-HEADER-1          | Column headers for detail lines                        |
| 4 | TRANSACTION-HEADER-2          | Separator line (dashes)                                |
| 5 | REPORT-PAGE-TOTALS            | Page-level subtotal                                    |
| 6 | REPORT-ACCOUNT-TOTALS         | Account-level subtotal                                 |
| 7 | REPORT-GRAND-TOTALS           | Grand total across all accounts                        |

---

## User Security Record (CSUSR01Y)

**Copybook:** `app/cpy/CSUSR01Y.cpy` | **Record Length:** 80 bytes | **VSAM File:** `USRSEC.VSAM.KSDS`

User authentication and authorization record.

| # | COBOL Field Name              | PIC Clause       | Len | Business Name               | Business Description                        | Java Type       |
|---|-------------------------------|------------------|-----|-----------------------------|---------------------------------------------|-----------------|
| 1 | SEC-USR-ID                    | X(08)            | 8   | User ID                     | Login user ID (primary key)                 | `String`        |
| 2 | SEC-USR-FNAME                 | X(20)            | 20  | First Name                  | User first name                             | `String`        |
| 3 | SEC-USR-LNAME                 | X(20)            | 20  | Last Name                   | User last name                              | `String`        |
| 4 | SEC-USR-PWD                   | X(08)            | 8   | Password                    | User password (plaintext - security risk)   | `String`        |
| 5 | SEC-USR-TYPE                  | X(01)            | 1   | User Type                   | A=Admin, U=Regular User                     | `String`        |
| 6 | FILLER                        | X(23)            | 23  | (Reserved)                  | Reserved                                    | --              |

**Security Notes:**
- Password stored in plaintext -- must be hashed in modernized system
- Two user types: Admin (A) and Regular User (U)
- Default credentials: ADMIN001/PASSWORD, USER0001/PASSWORD

---

## Export/Import Record (CVEXPORT)

**Copybook:** `app/cpy/CVEXPORT.cpy`

Multi-entity export/import format. Contains a header identifying the entity type followed by entity-specific fields.

| # | COBOL Field Name              | PIC Clause       | Len | Business Name               | Business Description                        |
|---|-------------------------------|------------------|-----|-----------------------------|---------------------------------------------|
| 1 | EXP-DATA-HEADER               | X(06)            | 6   | Data Type Header            | Entity identifier (e.g., "CUST", "ACCT")    |
| 2 | (Entity-specific fields)      | (varies)         | --  | Entity Data                 | Fields matching the corresponding copybook  |

Contains sub-records for: Customer (EXP-CUST-*), Account (EXP-ACCT-*), Card (EXP-CARD-*), Transaction (EXP-TRAN-*), Cross-Reference (EXP-XREF-*).

---

## Common Communication Area (COCOM01Y)

**Copybook:** `app/cpy/COCOM01Y.cpy`

CICS COMMAREA structure used to pass data between online programs via XCTL/LINK.

| # | COBOL Field Name              | PIC Clause       | Len | Business Name               | Business Description                        |
|---|-------------------------------|------------------|-----|-----------------------------|---------------------------------------------|
| 1 | CDEMO-FROM-TRANID             | X(04)            | 4   | Source Transaction ID       | Calling transaction ID                      |
| 2 | CDEMO-FROM-PROGRAM            | X(08)            | 8   | Source Program              | Calling program name                        |
| 3 | CDEMO-TO-TRANID               | X(04)            | 4   | Target Transaction ID       | Target transaction ID                       |
| 4 | CDEMO-TO-PROGRAM              | X(08)            | 8   | Target Program              | Target program name                         |
| 5 | CDEMO-PGM-CONTEXT             | 9(01)            | 1   | Program Context             | Context flag for screen state               |
| 6 | CDEMO-ACCT-ID                 | 9(11)            | 11  | Account ID                  | Current account being viewed/edited         |
| 7 | CDEMO-CARD-NUM                | X(16)            | 16  | Card Number                 | Current card being viewed/edited            |
| 8 | CDEMO-CUST-ID                 | 9(09)            | 9   | Customer ID                 | Current customer being viewed/edited        |
| 9 | CDEMO-ACCT-STATUS             | X(01)            | 1   | Account Status              | Selected account status                     |
| 10| CDEMO-LAST-MAP                | X(07)            | 7   | Last Map Displayed          | Name of last BMS map sent                   |
| 11| CDEMO-LAST-MAPSET             | X(07)            | 7   | Last Mapset                 | Name of last BMS mapset used                |
| 12| CDEMO-USER-TYPE               | X(01)            | 1   | User Type                   | A=Admin, U=User (from sign-on)              |

---

## Menu Definitions

### Regular User Menu (COMEN02Y)

**Copybook:** `app/cpy/COMEN02Y.cpy`

Defines the main menu options for regular users with associated program names and transaction IDs.

| Option | Menu Text             | Target Program | Target Transaction |
|--------|-----------------------|----------------|--------------------|
| 1      | View Account          | COACTVWC       | CA01               |
| 2      | Update Account        | COACTUPC       | CA02               |
| 3      | Card List             | COCRDLIC       | CC01               |
| 4      | Bill Payment          | COBIL00C       | CB00               |
| 5      | Transaction List      | COTRN00C       | CT00               |
| 6      | Transaction Report    | CORPT00C       | CR00               |

### Admin Menu (COADM02Y)

**Copybook:** `app/cpy/COADM02Y.cpy`

Defines admin menu options for user management.

| Option | Menu Text             | Target Program | Target Transaction |
|--------|-----------------------|----------------|--------------------|
| 1      | User List             | COUSR00C       | CU00               |
| 2      | User Add              | COUSR01C       | CU01               |
| 3      | User Update           | COUSR02C       | CU02               |
| 4      | User Delete           | COUSR03C       | CU03               |

---

## Screen Title/Header (COTTL01Y)

**Copybook:** `app/cpy/COTTL01Y.cpy`

Common screen title and header definitions used across all BMS screens.

| Field             | Value/Purpose                                    |
|-------------------|--------------------------------------------------|
| Application Title | "AWS CardDemo"                                   |
| Title Line 1      | Program-specific title (e.g., "Account View")   |
| Title Line 2      | Sub-title or context information                 |
| Date Display      | Current date (MM/DD/YY format)                   |
| Time Display      | Current time (HH:MM:SS format)                   |

---

## System Utility Copybooks

| Copybook   | Purpose                                                    |
|------------|------------------------------------------------------------|
| CSDAT01Y   | Date work areas for date manipulation routines             |
| CSLKPCDY   | Lookup code table definitions (state codes, etc.)          |
| CSMSG01Y   | System message definitions (informational)                 |
| CSMSG02Y   | System message definitions (error messages)                |
| CSSETATY   | BMS screen attribute byte definitions (colors, protection) |
| CSSTRPFY   | String manipulation utility fields                         |
| CSUTLDPY   | Date utility parameter area (for CSUTLDTC calls)           |
| CSUTLDWY   | Date utility working storage area                          |
| CODATECN   | Date conversion constants and routines                     |
| UNUSED1Y   | Legacy unused data structure (candidate for removal)       |

---

## VSAM File Summary

| VSAM Dataset Name                      | Key     | Record Len | Copybook  | Entity               | Key Type |
|----------------------------------------|---------|------------|-----------|----------------------|----------|
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS    | 11,0    | 300        | CVACT01Y  | Account Master       | KSDS     |
| AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS    | 16,0    | 150        | CVACT02Y  | Card Data            | KSDS     |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS    | 16,0    | 50         | CVACT03Y  | Card Cross-Reference | KSDS     |
| AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS    | 9,0     | 500        | CVCUS01Y  | Customer Master      | KSDS     |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS    | 16,0    | 350        | CVTRA05Y  | Transaction Master   | KSDS     |
| AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS      | 8,0     | 80         | CSUSR01Y  | User Security        | KSDS     |
| AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS    | 2,0     | 60         | CVTRA03Y  | Transaction Type     | KSDS     |
| AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS    | 6,0     | 60         | CVTRA04Y  | Transaction Category | KSDS     |
| AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS    | 17,0    | 50         | CVTRA01Y  | Category Balance     | KSDS     |
| AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS     | 16,0    | 50         | CVTRA02Y  | Disclosure Group     | KSDS     |

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)
  |
  |-- 1:N --> Card Cross-Reference (CVACT03Y)
  |               |
  |               |-- N:1 --> Account (CVACT01Y)
  |               |               |
  |               |               |-- 1:1 --> Disclosure Group (CVTRA02Y)
  |               |               |               [via ACCT-GROUP-ID]
  |               |               |
  |               |               |-- 1:N --> Category Balance (CVTRA01Y)
  |               |               |               [via ACCT-ID]
  |               |
  |               |-- N:1 --> Card Data (CVACT02Y)
  |                               |
  |                               |-- 1:N --> Transaction (CVTRA05Y)
  |                                               [via CARD-NUM]
  |
  User Security (CSUSR01Y) -- independent entity for authentication

  Transaction Type (CVTRA03Y) -- reference table
      |
      |-- 1:N --> Transaction Category (CVTRA04Y)
```

### Key Relationships for Java Entity Design

| Parent Entity     | Child Entity         | Relationship | Join Field(s)                         |
|-------------------|----------------------|--------------|---------------------------------------|
| Customer          | Card Cross-Ref       | 1:N          | CUST-ID = XREF-CUST-ID               |
| Account           | Card Cross-Ref       | 1:N          | ACCT-ID = XREF-ACCT-ID               |
| Card Data         | Transaction          | 1:N          | CARD-NUM = TRAN-CARD-NUM             |
| Account           | Category Balance     | 1:N          | ACCT-ID = TRANCAT-ACCT-ID           |
| Account Group     | Disclosure Group     | 1:N          | ACCT-GROUP-ID = DIS-ACCT-GROUP-ID    |
| Transaction Type  | Transaction Category | 1:N          | TRAN-TYPE = TRAN-TYPE-CD             |
| Transaction Type  | Transaction          | 1:N          | TRAN-TYPE = TRAN-TYPE-CD             |
