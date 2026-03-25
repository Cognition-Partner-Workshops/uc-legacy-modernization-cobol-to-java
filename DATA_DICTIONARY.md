# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo - Credit Card Management System
> **Source:** Extracted from COBOL copybook PIC clauses in `app/cpy/` and optional modules

---

## Table of Contents

1. [Entity Overview](#entity-overview)
2. [Account (CVACT01Y)](#account-cvact01y)
3. [Card (CVACT02Y)](#card-cvact02y)
4. [Card-Account Cross-Reference (CVACT03Y)](#card-account-cross-reference-cvact03y)
5. [Customer (CVCUS01Y)](#customer-cvcus01y)
6. [Transaction (CVTRA05Y)](#transaction-cvtra05y)
7. [Daily Transaction (CVTRA06Y)](#daily-transaction-cvtra06y)
8. [Transaction Type (CVTRA03Y)](#transaction-type-cvtra03y)
9. [Transaction Category Type (CVTRA04Y)](#transaction-category-type-cvtra04y)
10. [Transaction Category Balance (CVTRA01Y)](#transaction-category-balance-cvtra01y)
11. [Discount Group / Interest Rate (CVTRA02Y)](#discount-group--interest-rate-cvtra02y)
12. [User Security (CSUSR01Y)](#user-security-csusr01y)
13. [Statement Transaction (COSTM01)](#statement-transaction-costm01)
14. [Export Record (CVEXPORT)](#export-record-cvexport)
15. [Transaction Report Layout (CVTRA07Y)](#transaction-report-layout-cvtra07y)
16. [Common Communication Area (COCOM01Y)](#common-communication-area-cocom01y)
17. [Date/Time Fields (CSDAT01Y)](#datetime-fields-csdat01y)
18. [Date Conversion Parameters (CODATECN)](#date-conversion-parameters-codatecn)
19. [Authorization Detail (CIPAUDTY)](#authorization-detail-cipaudty)
20. [Authorization Summary (CIPAUSMY)](#authorization-summary-cipausmy)
21. [Authorization Request (CCPAURQY)](#authorization-request-ccpaurqy)
22. [Authorization Reply (CCPAURLY)](#authorization-reply-ccpaurly)
23. [VSAM-to-Table Mapping](#vsam-to-table-mapping)

---

## Entity Overview

```
+----------------+     1:N     +----------------+     1:N     +------------------+
|   Customer     |------------>|    Account     |------------>|   Transaction    |
|  (CVCUS01Y)    |             |  (CVACT01Y)    |             |   (CVTRA05Y)     |
+----------------+             +----------------+             +------------------+
       |                             |
       |                             | 1:N
       |                             v
       |                       +----------------+
       +---------------------->|     Card       |
              via XREF         |  (CVACT02Y)    |
           (CVACT03Y)          +----------------+
                                      |
                                      | 1:N (Optional)
                                      v
                               +------------------+
                               |  Authorization   |
                               |  (CIPAUDTY)      |
                               +------------------+
```

| Entity                    | Copybook   | VSAM File / Dataset                    | Record Size | Key Field(s)           |
|---------------------------|------------|----------------------------------------|------------:|------------------------|
| Account                   | CVACT01Y   | ACCTDATA.VSAM.KSDS                     |    300 bytes | ACCT-ID (11 digits)    |
| Card                      | CVACT02Y   | CARDDATA.VSAM.KSDS                     |    150 bytes | CARD-NUM (16 chars)    |
| Cross-Reference           | CVACT03Y   | CARDXREF.VSAM.KSDS                     |     50 bytes | XREF-CARD-NUM (16)     |
| Customer                  | CVCUS01Y   | CUSTDATA.VSAM.KSDS                     |    500 bytes | CUST-ID (9 digits)     |
| Transaction               | CVTRA05Y   | TRANSACT.VSAM.KSDS                     |    350 bytes | TRAN-ID (16 chars)     |
| Daily Transaction         | CVTRA06Y   | DALYTRAN.PS                            |    350 bytes | DALYTRAN-ID (16 chars) |
| Transaction Type          | CVTRA03Y   | TRANTYPE.VSAM.KSDS                     |     60 bytes | TRAN-TYPE (2 chars)    |
| Transaction Category Type | CVTRA04Y   | TRANCATG.VSAM.KSDS                     |     60 bytes | TRAN-TYPE-CD + CAT-CD  |
| Transaction Category Bal  | CVTRA01Y   | TCATBAL.VSAM.KSDS                      |     50 bytes | ACCT-ID + TYPE + CAT   |
| Discount Group            | CVTRA02Y   | DISCGRP.VSAM.KSDS                      |     50 bytes | GROUP-ID + TYPE + CAT  |
| User Security             | CSUSR01Y   | USRSEC.VSAM.KSDS                       |     80 bytes | SEC-USR-ID (8 chars)   |
| Authorization Detail      | CIPAUDTY   | IMS DB (PAUTHDTL)                      |    200 bytes | PA-CARD-NUM + date     |
| Authorization Summary     | CIPAUSMY   | IMS DB (PAUTHSUM)                      |    100 bytes | PA-ACCT-ID             |

---

## Account (CVACT01Y)

**Source:** `app/cpy/CVACT01Y.cpy` (20 lines)
**VSAM Dataset:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`
**Business Purpose:** Stores credit card account master data including balances, limits, and cycle information.

| Field Name              | PIC Clause       | Type      | Size     | Business Description                   | Nullable | Example Value       |
|-------------------------|------------------|-----------|----------|----------------------------------------|----------|---------------------|
| ACCT-ID                 | PIC 9(11)        | Numeric   | 11 digits| **Primary Key.** Unique account number | No       | 00000000001         |
| ACCT-ACTIVE-STATUS      | PIC X(01)        | Alpha     | 1 char   | Account status: Y=Active, N=Inactive   | No       | Y                   |
| ACCT-CURR-BAL           | PIC S9(10)V99    | Signed Dec| 12,2     | Current account balance                | No       | +0000050000.00      |
| ACCT-CREDIT-LIMIT       | PIC S9(10)V99    | Signed Dec| 12,2     | Credit limit                           | No       | +0000100000.00      |
| ACCT-CASH-CREDIT-LIMIT  | PIC S9(10)V99    | Signed Dec| 12,2     | Cash advance credit limit              | No       | +0000010000.00      |
| ACCT-OPEN-DATE          | PIC X(10)        | Alpha     | 10 chars | Account opening date (YYYY-MM-DD)      | No       | 2020-01-15          |
| ACCT-EXPIRAION-DATE     | PIC X(10)        | Alpha     | 10 chars | Account expiration date                | No       | 2025-01-15          |
| ACCT-REISSUE-DATE       | PIC X(10)        | Alpha     | 10 chars | Last card reissue date                 | Yes      | 2023-01-15          |
| ACCT-CURR-CYC-CREDIT    | PIC S9(10)V99    | Signed Dec| 12,2     | Current cycle credits (payments)       | No       | +0000001000.00      |
| ACCT-CURR-CYC-DEBIT     | PIC S9(10)V99    | Signed Dec| 12,2     | Current cycle debits (charges)         | No       | +0000002500.00      |
| ACCT-ADDR-ZIP           | PIC X(10)        | Alpha     | 10 chars | Account holder ZIP code                | Yes      | 10001               |
| ACCT-GROUP-ID           | PIC X(10)        | Alpha     | 10 chars | Discount/rate group identifier         | Yes      | PREMIUM             |
| FILLER                  | PIC X(178)       | Alpha     | 178 chars| Reserved for future use                | -        | SPACES              |

**Modernization Notes:**
- Maps to a `accounts` database table
- `ACCT-CURR-BAL` should be `DECIMAL(12,2)` in SQL
- `ACCT-ACTIVE-STATUS` maps to a `BOOLEAN` or `ENUM('ACTIVE','INACTIVE')`
- Dates should be converted to `DATE` type

---

## Card (CVACT02Y)

**Source:** `app/cpy/CVACT02Y.cpy` (14 lines)
**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`
**Business Purpose:** Stores physical/virtual credit card details linked to accounts.

| Field Name              | PIC Clause       | Type      | Size     | Business Description                   | Nullable | Example Value       |
|-------------------------|------------------|-----------|----------|----------------------------------------|----------|---------------------|
| CARD-NUM                | PIC X(16)        | Alpha     | 16 chars | **Primary Key.** Card number (PAN)     | No       | 4111111111111111    |
| CARD-ACCT-ID            | PIC 9(11)        | Numeric   | 11 digits| **FK to Account.** Parent account ID   | No       | 00000000001         |
| CARD-CVV-CD             | PIC 9(03)        | Numeric   | 3 digits | Card verification value (CVV)          | No       | 123                 |
| CARD-EMBOSSED-NAME      | PIC X(50)        | Alpha     | 50 chars | Name embossed on card                  | No       | JOHN Q DOE          |
| CARD-EXPIRAION-DATE     | PIC X(10)        | Alpha     | 10 chars | Card expiration date                   | No       | 2025-12-31          |
| CARD-ACTIVE-STATUS      | PIC X(01)        | Alpha     | 1 char   | Card status: Y=Active, N=Inactive      | No       | Y                   |
| FILLER                  | PIC X(59)        | Alpha     | 59 chars | Reserved for future use                | -        | SPACES              |

**Modernization Notes:**
- Maps to a `cards` table with FK to `accounts`
- `CARD-NUM` is sensitive PII - requires encryption at rest
- `CARD-CVV-CD` must never be stored in production (PCI-DSS)

---

## Card-Account Cross-Reference (CVACT03Y)

**Source:** `app/cpy/CVACT03Y.cpy` (11 lines)
**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`
**Business Purpose:** Maps cards to customers and accounts, enabling lookup in any direction.

| Field Name              | PIC Clause       | Type      | Size     | Business Description                   | Nullable | Example Value       |
|-------------------------|------------------|-----------|----------|----------------------------------------|----------|---------------------|
| XREF-CARD-NUM           | PIC X(16)        | Alpha     | 16 chars | **Primary Key.** Card number           | No       | 4111111111111111    |
| XREF-CUST-ID            | PIC 9(09)        | Numeric   | 9 digits | **FK to Customer.** Customer ID        | No       | 000000001           |
| XREF-ACCT-ID            | PIC 9(11)        | Numeric   | 11 digits| **FK to Account.** Account ID          | No       | 00000000001         |
| FILLER                  | PIC X(14)        | Alpha     | 14 chars | Reserved                               | -        | SPACES              |

**Modernization Notes:**
- In a relational model this becomes a join table or FKs on the `cards` table
- Enables the Customer -> Account -> Card navigation path

---

## Customer (CVCUS01Y)

**Source:** `app/cpy/CVCUS01Y.cpy` (26 lines)
**VSAM Dataset:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`
**Business Purpose:** Stores customer personal information, contact details, and identification.

| Field Name                  | PIC Clause       | Type      | Size     | Business Description                      | Nullable | Example Value       |
|-----------------------------|------------------|-----------|----------|-------------------------------------------|----------|---------------------|
| CUST-ID                     | PIC 9(09)        | Numeric   | 9 digits | **Primary Key.** Customer identifier      | No       | 000000001           |
| CUST-FIRST-NAME             | PIC X(25)        | Alpha     | 25 chars | Customer first name                       | No       | JOHN                |
| CUST-MIDDLE-NAME            | PIC X(25)        | Alpha     | 25 chars | Customer middle name                      | Yes      | QUINCY              |
| CUST-LAST-NAME              | PIC X(25)        | Alpha     | 25 chars | Customer last name                        | No       | DOE                 |
| CUST-ADDR-LINE-1            | PIC X(50)        | Alpha     | 50 chars | Address line 1                            | No       | 123 MAIN ST         |
| CUST-ADDR-LINE-2            | PIC X(50)        | Alpha     | 50 chars | Address line 2                            | Yes      | APT 4B              |
| CUST-ADDR-LINE-3            | PIC X(50)        | Alpha     | 50 chars | Address line 3                            | Yes      | SPACES              |
| CUST-ADDR-STATE-CD          | PIC X(02)        | Alpha     | 2 chars  | US state code                             | No       | NY                  |
| CUST-ADDR-COUNTRY-CD        | PIC X(03)        | Alpha     | 3 chars  | Country code (ISO 3166)                   | No       | USA                 |
| CUST-ADDR-ZIP               | PIC X(10)        | Alpha     | 10 chars | ZIP/postal code                           | No       | 10001               |
| CUST-PHONE-NUM-1            | PIC X(15)        | Alpha     | 15 chars | Primary phone number                      | No       | 212-555-0100        |
| CUST-PHONE-NUM-2            | PIC X(15)        | Alpha     | 15 chars | Secondary phone number                    | Yes      | 212-555-0101        |
| CUST-SSN                    | PIC 9(09)        | Numeric   | 9 digits | Social Security Number                    | No       | 123456789           |
| CUST-GOVT-ISSUED-ID         | PIC X(20)        | Alpha     | 20 chars | Government-issued ID number               | Yes      | DL-NY-12345678      |
| CUST-DOB-YYYY-MM-DD         | PIC X(10)        | Alpha     | 10 chars | Date of birth                             | No       | 1985-03-15          |
| CUST-EFT-ACCOUNT-ID         | PIC X(10)        | Alpha     | 10 chars | EFT (bank) account for payments           | Yes      | 1234567890          |
| CUST-PRI-CARD-HOLDER-IND    | PIC X(01)        | Alpha     | 1 char   | Primary card holder? Y/N                  | No       | Y                   |
| CUST-FICO-CREDIT-SCORE      | PIC 9(03)        | Numeric   | 3 digits | FICO credit score (300-850)               | No       | 750                 |
| FILLER                      | PIC X(168)       | Alpha     | 168 chars| Reserved for future use                   | -        | SPACES              |

**Modernization Notes:**
- `CUST-SSN` is highly sensitive PII - requires encryption, masking, and access controls
- Address fields may benefit from normalization into a separate `addresses` table
- `CUST-FICO-CREDIT-SCORE` should have CHECK constraint (300-850)

---

## Transaction (CVTRA05Y)

**Source:** `app/cpy/CVTRA05Y.cpy` (21 lines)
**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`
**Business Purpose:** Core transaction record for all credit card charges, payments, and adjustments.

| Field Name              | PIC Clause       | Type      | Size     | Business Description                   | Nullable | Example Value       |
|-------------------------|------------------|-----------|----------|----------------------------------------|----------|---------------------|
| TRAN-ID                 | PIC X(16)        | Alpha     | 16 chars | **Primary Key.** Transaction ID        | No       | 0000000000000001    |
| TRAN-TYPE-CD            | PIC X(02)        | Alpha     | 2 chars  | Transaction type code (FK to CVTRA03Y) | No       | SA                  |
| TRAN-CAT-CD             | PIC 9(04)        | Numeric   | 4 digits | Transaction category code              | No       | 5001                |
| TRAN-SOURCE             | PIC X(10)        | Alpha     | 10 chars | Origination source                     | No       | ONLINE              |
| TRAN-DESC               | PIC X(100)       | Alpha     | 100 chars| Transaction description                | No       | PURCHASE AT STORE   |
| TRAN-AMT                | PIC S9(09)V99    | Signed Dec| 11,2     | Transaction amount                     | No       | +000050000.00       |
| TRAN-MERCHANT-ID        | PIC 9(09)        | Numeric   | 9 digits | Merchant identifier                    | Yes      | 000012345           |
| TRAN-MERCHANT-NAME      | PIC X(50)        | Alpha     | 50 chars | Merchant name                          | Yes      | ACME CORP           |
| TRAN-MERCHANT-CITY      | PIC X(50)        | Alpha     | 50 chars | Merchant city                          | Yes      | NEW YORK            |
| TRAN-MERCHANT-ZIP       | PIC X(10)        | Alpha     | 10 chars | Merchant ZIP code                      | Yes      | 10001               |
| TRAN-CARD-NUM           | PIC X(16)        | Alpha     | 16 chars | Card used for transaction              | No       | 4111111111111111    |
| TRAN-ORIG-TS            | PIC X(26)        | Alpha     | 26 chars | Origination timestamp                  | No       | 2024-01-15 10:30:00.000000 |
| TRAN-PROC-TS            | PIC X(26)        | Alpha     | 26 chars | Processing timestamp                   | No       | 2024-01-15 10:30:01.000000 |
| FILLER                  | PIC X(20)        | Alpha     | 20 chars | Reserved                               | -        | SPACES              |

**Modernization Notes:**
- Highest-volume entity - consider partitioning by date in the target DB
- `TRAN-AMT` is signed for credits (positive) and debits (negative)
- Timestamps should be `TIMESTAMP WITH TIME ZONE` in the target

---

## Daily Transaction (CVTRA06Y)

**Source:** `app/cpy/CVTRA06Y.cpy` (21 lines)
**VSAM Dataset:** `AWS.M2.CARDDEMO.DALYTRAN.PS`
**Business Purpose:** Staging file for incoming daily transactions before posting to the master.

| Field Name              | PIC Clause       | Type      | Size     | Business Description                   |
|-------------------------|------------------|-----------|----------|----------------------------------------|
| DALYTRAN-ID             | PIC X(16)        | Alpha     | 16 chars | Transaction ID                         |
| DALYTRAN-TYPE-CD        | PIC X(02)        | Alpha     | 2 chars  | Transaction type code                  |
| DALYTRAN-CAT-CD         | PIC 9(04)        | Numeric   | 4 digits | Category code                          |
| DALYTRAN-SOURCE         | PIC X(10)        | Alpha     | 10 chars | Source system                          |
| DALYTRAN-DESC           | PIC X(100)       | Alpha     | 100 chars| Description                            |
| DALYTRAN-AMT            | PIC S9(09)V99    | Signed Dec| 11,2     | Amount                                 |
| DALYTRAN-MERCHANT-ID    | PIC 9(09)        | Numeric   | 9 digits | Merchant ID                            |
| DALYTRAN-MERCHANT-NAME  | PIC X(50)        | Alpha     | 50 chars | Merchant name                          |
| DALYTRAN-MERCHANT-CITY  | PIC X(50)        | Alpha     | 50 chars | Merchant city                          |
| DALYTRAN-MERCHANT-ZIP   | PIC X(10)        | Alpha     | 10 chars | Merchant ZIP                           |
| DALYTRAN-CARD-NUM       | PIC X(16)        | Alpha     | 16 chars | Card number                            |
| DALYTRAN-ORIG-TS        | PIC X(26)        | Alpha     | 26 chars | Origination timestamp                  |
| DALYTRAN-PROC-TS        | PIC X(26)        | Alpha     | 26 chars | Processing timestamp                   |
| FILLER                  | PIC X(20)        | Alpha     | 20 chars | Reserved                               |

**Modernization Notes:**
- Same layout as CVTRA05Y (Transaction) -- in a modernized system this becomes a staging table or message queue
- Read by CBTRN01C (daily posting) and fed into CBTRN02C (master update)

---

## Transaction Type (CVTRA03Y)

**Source:** `app/cpy/CVTRA03Y.cpy` (10 lines)
**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`
**Business Purpose:** Lookup table defining transaction type codes and descriptions.

| Field Name              | PIC Clause       | Type      | Size     | Business Description                   |
|-------------------------|------------------|-----------|----------|----------------------------------------|
| TRAN-TYPE               | PIC X(02)        | Alpha     | 2 chars  | **Primary Key.** Type code (SA, PR...) |
| TRAN-TYPE-DESC          | PIC X(50)        | Alpha     | 50 chars | Human-readable description             |
| FILLER                  | PIC X(08)        | Alpha     | 8 chars  | Reserved                               |

**Known Type Codes:** SA=Sale, PR=Purchase, RT=Return, PM=Payment, CR=Credit, IN=Interest

---

## Transaction Category Type (CVTRA04Y)

**Source:** `app/cpy/CVTRA04Y.cpy` (12 lines)
**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`
**Business Purpose:** Combines type and category codes with descriptions for detailed classification.

| Field Name              | PIC Clause       | Type      | Size     | Business Description                   |
|-------------------------|------------------|-----------|----------|----------------------------------------|
| TRAN-TYPE-CD            | PIC X(02)        | Alpha     | 2 chars  | **Composite Key.** Type code           |
| TRAN-CAT-CD             | PIC 9(04)        | Numeric   | 4 digits | **Composite Key.** Category code       |
| TRAN-CAT-TYPE-DESC      | PIC X(50)        | Alpha     | 50 chars | Category type description              |
| FILLER                  | PIC X(04)        | Alpha     | 4 chars  | Reserved                               |

---

## Transaction Category Balance (CVTRA01Y)

**Source:** `app/cpy/CVTRA01Y.cpy` (13 lines)
**VSAM Dataset:** `AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS`
**Business Purpose:** Running balance per account, per transaction type/category combination.

| Field Name              | PIC Clause       | Type      | Size     | Business Description                   |
|-------------------------|------------------|-----------|----------|----------------------------------------|
| TRANCAT-ACCT-ID         | PIC 9(11)        | Numeric   | 11 digits| **Composite Key.** Account ID          |
| TRANCAT-TYPE-CD         | PIC X(02)        | Alpha     | 2 chars  | **Composite Key.** Type code           |
| TRANCAT-CD              | PIC 9(04)        | Numeric   | 4 digits | **Composite Key.** Category code       |
| TRAN-CAT-BAL            | PIC S9(09)V99    | Signed Dec| 11,2     | Running balance for this combination   |
| FILLER                  | PIC X(22)        | Alpha     | 22 chars | Reserved                               |

---

## Discount Group / Interest Rate (CVTRA02Y)

**Source:** `app/cpy/CVTRA02Y.cpy` (13 lines)
**VSAM Dataset:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`
**Business Purpose:** Defines interest rates per account group and transaction type/category.

| Field Name              | PIC Clause       | Type      | Size     | Business Description                   |
|-------------------------|------------------|-----------|----------|----------------------------------------|
| DIS-ACCT-GROUP-ID       | PIC X(10)        | Alpha     | 10 chars | **Composite Key.** Account group       |
| DIS-TRAN-TYPE-CD        | PIC X(02)        | Alpha     | 2 chars  | **Composite Key.** Type code           |
| DIS-TRAN-CAT-CD         | PIC 9(04)        | Numeric   | 4 digits | **Composite Key.** Category code       |
| DIS-INT-RATE            | PIC S9(04)V99    | Signed Dec| 6,2      | Interest rate percentage               |
| FILLER                  | PIC X(28)        | Alpha     | 28 chars | Reserved                               |

---

## User Security (CSUSR01Y)

**Source:** `app/cpy/CSUSR01Y.cpy` (26 lines)
**VSAM Dataset:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`
**Business Purpose:** Authentication and authorization records for application users.

| Field Name              | PIC Clause       | Type      | Size     | Business Description                   |
|-------------------------|------------------|-----------|----------|----------------------------------------|
| SEC-USR-ID              | PIC X(08)        | Alpha     | 8 chars  | **Primary Key.** User login ID         |
| SEC-USR-FNAME           | PIC X(20)        | Alpha     | 20 chars | User first name                        |
| SEC-USR-LNAME           | PIC X(20)        | Alpha     | 20 chars | User last name                         |
| SEC-USR-PWD             | PIC X(08)        | Alpha     | 8 chars  | Password (plain text!)                 |
| SEC-USR-TYPE            | PIC X(01)        | Alpha     | 1 char   | User type: A=Admin, U=Regular          |
| SEC-USR-FILLER          | PIC X(23)        | Alpha     | 23 chars | Reserved                               |

**Modernization Notes:**
- **CRITICAL:** Passwords stored in plain text -- must implement hashing (bcrypt/scrypt)
- User type should become a role-based access control (RBAC) model
- Default credentials: ADMIN001/PASSWORD (admin), USER0001/PASSWORD (regular)

---

## Statement Transaction (COSTM01)

**Source:** `app/cpy/COSTM01.CPY` (38 lines)
**Business Purpose:** Working storage for statement generation batch process.

| Field Name              | PIC Clause       | Type      | Size     | Business Description                   |
|-------------------------|------------------|-----------|----------|----------------------------------------|
| TRNX-CARD-NUM           | PIC X(16)        | Alpha     | 16 chars | Card number for statement              |
| TRNX-ID                 | PIC X(16)        | Alpha     | 16 chars | Transaction ID                         |
| TRNX-TYPE-CD            | PIC X(02)        | Alpha     | 2 chars  | Type code                              |
| TRNX-CAT-CD             | PIC 9(04)        | Numeric   | 4 digits | Category code                          |
| TRNX-SOURCE             | PIC X(10)        | Alpha     | 10 chars | Source                                 |
| TRNX-DESC               | PIC X(100)       | Alpha     | 100 chars| Description                            |
| TRNX-AMT                | PIC S9(09)V99    | Signed Dec| 11,2     | Amount                                 |
| TRNX-MERCHANT-ID        | PIC 9(09)        | Numeric   | 9 digits | Merchant ID                            |
| TRNX-MERCHANT-NAME      | PIC X(50)        | Alpha     | 50 chars | Merchant name                          |
| TRNX-MERCHANT-CITY      | PIC X(50)        | Alpha     | 50 chars | Merchant city                          |
| TRNX-MERCHANT-ZIP       | PIC X(10)        | Alpha     | 10 chars | Merchant ZIP                           |
| TRNX-ORIG-TS            | PIC X(26)        | Alpha     | 26 chars | Origination timestamp                  |
| TRNX-PROC-TS            | PIC X(26)        | Alpha     | 26 chars | Processing timestamp                   |
| FILLER                  | PIC X(20)        | Alpha     | 20 chars | Reserved                               |

---

## Export Record (CVEXPORT)

**Source:** `app/cpy/CVEXPORT.cpy` (103 lines)
**Business Purpose:** Multi-type export record with a header and type-specific payload. Used by CBEXPORT/CBIMPORT for data interchange.

### Header Fields (All Record Types)

| Field Name              | PIC Clause       | Type      | Size     | Business Description                   |
|-------------------------|------------------|-----------|----------|----------------------------------------|
| EXPORT-REC-TYPE         | PIC X(1)         | Alpha     | 1 char   | Record type: C=Customer, A=Account, T=Transaction, X=Xref, D=Card |
| EXPORT-TIMESTAMP        | PIC X(26)        | Alpha     | 26 chars | Export timestamp                       |
| EXPORT-SEQUENCE-NUM     | PIC 9(9) COMP    | Binary    | 4 bytes  | Sequence number within export          |
| EXPORT-BRANCH-ID        | PIC X(4)         | Alpha     | 4 chars  | Originating branch                     |
| EXPORT-REGION-CODE      | PIC X(5)         | Alpha     | 5 chars  | Region code                            |
| EXPORT-RECORD-DATA      | PIC X(460)       | Alpha     | 460 chars| Type-specific payload (see below)      |

### Payload Variants (REDEFINES of EXPORT-RECORD-DATA)
- **Customer (Type C):** EXP-CUST-ID, name, address, SSN, DOB, FICO score
- **Account (Type A):** EXP-ACCT-ID, status, balances, limits, dates
- **Transaction (Type T):** EXP-TRAN-ID, amounts, merchant details, timestamps
- **Cross-Reference (Type X):** EXP-XREF-CARD-NUM, customer ID, account ID
- **Card (Type D):** EXP-CARD-NUM, account ID, CVV, name, expiry, status

---

## Transaction Report Layout (CVTRA07Y)

**Source:** `app/cpy/CVTRA07Y.cpy` (73 lines)
**Business Purpose:** Print layout definitions for batch transaction reports. Contains headers, detail lines, and totals.

| Section               | Key Fields                                          | Description                  |
|-----------------------|-----------------------------------------------------|------------------------------|
| Report Headers        | REPT-SHORT-NAME, REPT-LONG-NAME, REPT-DATE-HEADER  | Report identification        |
| Date Range            | REPT-START-DATE, REPT-END-DATE                      | Reporting period             |
| Detail Line           | TRAN-REPORT-TRANS-ID, ACCOUNT-ID, TYPE-CD/DESC, CAT-CD/DESC, SOURCE, AMT | Per-transaction data |
| Page Total            | REPT-PAGE-TOTAL                                     | Subtotal per page            |
| Account Total         | REPT-ACCOUNT-TOTAL                                  | Subtotal per account         |
| Grand Total           | REPT-GRAND-TOTAL                                    | Report grand total           |

---

## Common Communication Area (COCOM01Y)

**Source:** `app/cpy/COCOM01Y.cpy` (47 lines)
**Business Purpose:** CICS COMMAREA passed between online programs for session context and navigation.

| Field Name              | PIC Clause       | Type      | Business Description                           |
|-------------------------|------------------|-----------|-------------------------------------------------|
| CDEMO-FROM-TRANID       | PIC X(04)        | Alpha     | Source transaction ID                          |
| CDEMO-FROM-PROGRAM      | PIC X(08)        | Alpha     | Source program name                            |
| CDEMO-TO-TRANID         | PIC X(04)        | Alpha     | Target transaction ID                          |
| CDEMO-TO-PROGRAM        | PIC X(08)        | Alpha     | Target program name                            |
| CDEMO-USER-ID           | PIC X(08)        | Alpha     | Authenticated user ID                          |
| CDEMO-USER-TYPE         | PIC X(01)        | Alpha     | User type (A=Admin, U=Regular)                 |
| CDEMO-PGM-CONTEXT       | PIC 9(01)        | Numeric   | Program context flag                           |
| CDEMO-CUST-ID           | PIC 9(09)        | Numeric   | Current customer context                       |
| CDEMO-CUST-FNAME        | PIC X(25)        | Alpha     | Customer first name (display)                  |
| CDEMO-CUST-MNAME        | PIC X(25)        | Alpha     | Customer middle name (display)                 |
| CDEMO-CUST-LNAME        | PIC X(25)        | Alpha     | Customer last name (display)                   |
| CDEMO-ACCT-ID           | PIC 9(11)        | Numeric   | Current account context                        |
| CDEMO-ACCT-STATUS       | PIC X(01)        | Alpha     | Current account status                         |
| CDEMO-CARD-NUM          | PIC 9(16)        | Numeric   | Current card context                           |
| CDEMO-LAST-MAP          | PIC X(7)         | Alpha     | Last BMS map displayed                         |
| CDEMO-LAST-MAPSET       | PIC X(7)         | Alpha     | Last BMS mapset used                           |

**Modernization Notes:**
- Maps to HTTP session state or JWT token claims in a modern web application
- Navigation context (FROM/TO program) becomes URL routing

---

## Date/Time Fields (CSDAT01Y)

**Source:** `app/cpy/CSDAT01Y.cpy` (58 lines)
**Business Purpose:** Standard date and time working storage fields used by all online programs.

| Field Name              | PIC Clause       | Business Description                           |
|-------------------------|------------------|-------------------------------------------------|
| WS-CURDATE-YEAR         | PIC 9(04)        | Current year (YYYY)                            |
| WS-CURDATE-MONTH        | PIC 9(02)        | Current month (MM)                             |
| WS-CURDATE-DAY          | PIC 9(02)        | Current day (DD)                               |
| WS-CURTIME-HOURS        | PIC 9(02)        | Current hours (HH)                             |
| WS-CURTIME-MINUTE       | PIC 9(02)        | Current minutes (MM)                           |
| WS-CURTIME-SECOND       | PIC 9(02)        | Current seconds (SS)                           |
| WS-TIMESTAMP-*          | Various          | Full timestamp (YYYY-MM-DD HH:MM:SS.MMMMMM)   |

---

## Date Conversion Parameters (CODATECN)

**Source:** `app/cpy/CODATECN.cpy` (52 lines)
**Business Purpose:** Parameters for date format conversion between COBOL internal and display formats.

| Field Name              | PIC Clause       | Business Description                           |
|-------------------------|------------------|-------------------------------------------------|
| CODATECN-TYPE           | PIC X            | Conversion type indicator                      |
| CODATECN-INP-DATE       | PIC X(20)        | Input date string                              |
| CODATECN-OUTTYPE        | PIC X            | Output format type                             |
| CODATECN-0UT-DATE       | PIC X(20)        | Formatted output date                          |
| CODATECN-ERROR-MSG      | PIC X(38)        | Error message if conversion fails              |

---

## Authorization Detail (CIPAUDTY)

**Source:** `app/app-authorization-ims-db2-mq/cpy/CIPAUDTY.cpy`
**Storage:** IMS Database (PAUTHDTL segment)
**Business Purpose:** Individual payment authorization records with fraud tracking.

| Field Name              | PIC Clause         | Type        | Business Description                      |
|-------------------------|--------------------|-------------|-------------------------------------------|
| PA-AUTH-DATE-9C         | PIC S9(05) COMP-3  | Packed Dec  | Authorization date (packed)               |
| PA-AUTH-TIME-9C         | PIC S9(09) COMP-3  | Packed Dec  | Authorization time (packed)               |
| PA-AUTH-ORIG-DATE       | PIC X(06)          | Alpha       | Original date (YYMMDD)                    |
| PA-AUTH-ORIG-TIME       | PIC X(06)          | Alpha       | Original time (HHMMSS)                    |
| PA-CARD-NUM             | PIC X(16)          | Alpha       | Card number                               |
| PA-AUTH-TYPE            | PIC X(04)          | Alpha       | Authorization type                        |
| PA-CARD-EXPIRY-DATE     | PIC X(04)          | Alpha       | Card expiry (YYMM)                        |
| PA-MESSAGE-TYPE         | PIC X(06)          | Alpha       | Message type code                         |
| PA-MESSAGE-SOURCE       | PIC X(06)          | Alpha       | Message origination source                |
| PA-AUTH-ID-CODE         | PIC X(06)          | Alpha       | Authorization ID code                     |
| PA-AUTH-RESP-CODE       | PIC X(02)          | Alpha       | Response code (00=Approved)               |
| PA-AUTH-RESP-REASON     | PIC X(04)          | Alpha       | Decline reason code                       |
| PA-PROCESSING-CODE      | PIC 9(06)          | Numeric     | Processing code                           |
| PA-TRANSACTION-AMT      | PIC S9(10)V99 COMP-3| Packed Dec | Transaction amount                        |
| PA-APPROVED-AMT         | PIC S9(10)V99 COMP-3| Packed Dec | Approved amount                           |
| PA-MERCHANT-*           | Various            | Various     | Merchant details (ID, name, city, state, ZIP) |
| PA-TRANSACTION-ID       | PIC X(15)          | Alpha       | Transaction reference ID                  |
| PA-MATCH-STATUS         | PIC X(01)          | Alpha       | Match status flag                         |
| PA-AUTH-FRAUD           | PIC X(01)          | Alpha       | Fraud flag (Y/N)                          |
| PA-FRAUD-RPT-DATE       | PIC X(08)          | Alpha       | Date fraud was reported                   |

---

## Authorization Summary (CIPAUSMY)

**Source:** `app/app-authorization-ims-db2-mq/cpy/CIPAUSMY.cpy`
**Storage:** IMS Database (PAUTHSUM segment)
**Business Purpose:** Aggregated authorization statistics per account for risk monitoring.

| Field Name              | PIC Clause         | Type        | Business Description                      |
|-------------------------|--------------------|-------------|-------------------------------------------|
| PA-ACCT-ID              | PIC S9(11) COMP-3  | Packed Dec  | Account ID                                |
| PA-CUST-ID              | PIC 9(09)          | Numeric     | Customer ID                               |
| PA-AUTH-STATUS          | PIC X(01)          | Alpha       | Overall authorization status              |
| PA-ACCOUNT-STATUS       | PIC X(02) OCCURS 5 | Alpha Array | Status history (5 periods)                |
| PA-CREDIT-LIMIT         | PIC S9(09)V99 COMP-3| Packed Dec | Credit limit                              |
| PA-CASH-LIMIT           | PIC S9(09)V99 COMP-3| Packed Dec | Cash advance limit                        |
| PA-CREDIT-BALANCE       | PIC S9(09)V99 COMP-3| Packed Dec | Credit balance                            |
| PA-CASH-BALANCE         | PIC S9(09)V99 COMP-3| Packed Dec | Cash balance                              |
| PA-APPROVED-AUTH-CNT    | PIC S9(04) COMP    | Binary      | Count of approved authorizations          |
| PA-DECLINED-AUTH-CNT    | PIC S9(04) COMP    | Binary      | Count of declined authorizations          |
| PA-APPROVED-AUTH-AMT    | PIC S9(09)V99 COMP-3| Packed Dec | Total approved amount                     |
| PA-DECLINED-AUTH-AMT    | PIC S9(09)V99 COMP-3| Packed Dec | Total declined amount                     |

---

## Authorization Request (CCPAURQY)

**Source:** `app/app-authorization-ims-db2-mq/cpy/CCPAURQY.cpy`
**Transport:** MQ Message
**Business Purpose:** Real-time authorization request from POS terminal or online system.

| Field Name                  | PIC Clause       | Business Description                      |
|-----------------------------|------------------|-------------------------------------------|
| PA-RQ-AUTH-DATE             | PIC X(06)        | Request date                              |
| PA-RQ-AUTH-TIME             | PIC X(06)        | Request time                              |
| PA-RQ-CARD-NUM              | PIC X(16)        | Card number                               |
| PA-RQ-AUTH-TYPE             | PIC X(04)        | Authorization type                        |
| PA-RQ-CARD-EXPIRY-DATE      | PIC X(04)        | Card expiry                               |
| PA-RQ-MESSAGE-TYPE          | PIC X(06)        | Message type                              |
| PA-RQ-TRANSACTION-AMT       | PIC +9(10).99    | Requested transaction amount              |
| PA-RQ-MERCHANT-*            | Various          | Merchant details                          |
| PA-RQ-TRANSACTION-ID        | PIC X(15)        | Transaction reference                     |

---

## Authorization Reply (CCPAURLY)

**Source:** `app/app-authorization-ims-db2-mq/cpy/CCPAURLY.cpy`
**Transport:** MQ Message
**Business Purpose:** Authorization decision response back to the requesting system.

| Field Name              | PIC Clause       | Business Description                      |
|-------------------------|------------------|-------------------------------------------|
| PA-RL-CARD-NUM          | PIC X(16)        | Card number (echo back)                   |
| PA-RL-TRANSACTION-ID    | PIC X(15)        | Transaction reference                     |
| PA-RL-AUTH-ID-CODE      | PIC X(06)        | Authorization ID (approval code)          |
| PA-RL-AUTH-RESP-CODE    | PIC X(02)        | Response: 00=Approved, else declined      |
| PA-RL-AUTH-RESP-REASON  | PIC X(04)        | Reason code for decline                   |
| PA-RL-APPROVED-AMT      | PIC +9(10).99    | Approved amount                           |

---

## VSAM-to-Table Mapping

The following table maps COBOL VSAM files to suggested relational database tables for modernization:

| VSAM Dataset                           | Suggested Table         | Primary Key              | Foreign Keys                |
|----------------------------------------|-------------------------|--------------------------|-----------------------------|
| ACCTDATA.VSAM.KSDS                     | `accounts`              | `account_id`             | -                           |
| CARDDATA.VSAM.KSDS                     | `cards`                 | `card_number`            | `account_id` -> accounts    |
| CARDXREF.VSAM.KSDS                     | `card_xref` (or FKs)   | `card_number`            | `customer_id`, `account_id` |
| CUSTDATA.VSAM.KSDS                     | `customers`             | `customer_id`            | -                           |
| TRANSACT.VSAM.KSDS                     | `transactions`          | `transaction_id`         | `card_number` -> cards      |
| DALYTRAN.PS                            | `daily_transactions`    | `transaction_id`         | `card_number` -> cards      |
| TRANTYPE.VSAM.KSDS                     | `transaction_types`     | `type_code`              | -                           |
| TRANCATG.VSAM.KSDS                     | `transaction_categories`| `type_code, category_code`| `type_code` -> types       |
| TCATBAL.VSAM.KSDS                      | `category_balances`     | `account_id, type, cat`  | `account_id` -> accounts    |
| DISCGRP.VSAM.KSDS                      | `discount_groups`       | `group_id, type, cat`    | -                           |
| USRSEC.VSAM.KSDS                       | `users`                 | `user_id`                | -                           |
| IMS PAUTHDTL                           | `auth_details`          | `card_num, auth_date`    | `card_number` -> cards      |
| IMS PAUTHSUM                           | `auth_summaries`        | `account_id`             | `account_id` -> accounts    |

### COBOL PIC to SQL Type Mapping

| COBOL PIC Pattern        | SQL Type               | Java Type        | Notes                        |
|--------------------------|------------------------|------------------|------------------------------|
| PIC 9(n)                 | BIGINT / INT           | long / int       | Unsigned integer             |
| PIC S9(n)V99             | DECIMAL(n+2, 2)        | BigDecimal       | Signed with 2 decimal places |
| PIC S9(n)V99 COMP-3      | DECIMAL(n+2, 2)        | BigDecimal       | Packed decimal               |
| PIC S9(n) COMP           | INT / BIGINT           | int / long       | Binary integer               |
| PIC X(n)                 | VARCHAR(n)             | String           | Alphanumeric                 |
| PIC X(10) (date)         | DATE                   | LocalDate        | Parse YYYY-MM-DD             |
| PIC X(26) (timestamp)    | TIMESTAMP              | LocalDateTime    | Parse full timestamp         |
| PIC X(01) (flag)         | CHAR(1) or BOOLEAN     | boolean / String | Y/N or A/U flags             |
