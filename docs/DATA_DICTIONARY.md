# DATA DICTIONARY - CardDemo COBOL Application

> **Generated**: 2026-03-25 | **Source**: Copybook PIC clause analysis (`app/cpy/`)
> **Purpose**: Business-friendly reference of all data entities, fields, types, and sizes

---

## 1. Entity Overview

| # | Entity                    | Copybook   | Record Length | VSAM Type | Business Description                     |
|---|---------------------------|------------|--------------|-----------|------------------------------------------|
| 1 | Account Master            | CVACT01Y   | 300 bytes    | KSDS      | Credit card account records              |
| 2 | Card Master               | CVACT02Y   | 150 bytes    | KSDS      | Physical/virtual card records            |
| 3 | Card Cross-Reference      | CVACT03Y   | 50 bytes     | KSDS+AIX  | Maps cards to accounts and customers     |
| 4 | Customer Master           | CVCUS01Y   | 500 bytes    | KSDS      | Customer demographic/contact records     |
| 5 | Transaction Master        | CVTRA05Y   | 350 bytes    | KSDS      | Posted financial transactions            |
| 6 | Daily Transaction         | CVTRA06Y   | 350 bytes    | Sequential| Incoming daily transactions (pre-post)   |
| 7 | Tran Category Balance     | CVTRA01Y   | 50 bytes     | KSDS      | Running balance by account+type+category |
| 8 | Disclosure Group          | CVTRA02Y   | 50 bytes     | KSDS      | Interest rate rules by group/type/cat    |
| 9 | Transaction Type          | CVTRA03Y   | 60 bytes     | KSDS      | Reference: transaction type codes        |
|10 | Transaction Category      | CVTRA04Y   | 60 bytes     | KSDS      | Reference: transaction category codes    |
|11 | User Security             | CSUSR01Y   | 80 bytes     | KSDS      | User authentication records              |
|12 | Export Record             | CVEXPORT   | Variable     | Sequential| Cross-entity export/import format        |
|13 | Statement Transaction     | COSTM01    | 355 bytes    | Sequential| Transaction layout for statement report  |
|14 | Report Layout             | CVTRA07Y   | --           | --        | Report headers, detail lines, totals     |
|15 | Customer (alt layout)     | CUSTREC    | 80 bytes     | --        | Alternate customer record for statements |

---

## 2. Account Master (`CVACT01Y`)

**Record Name**: `ACCOUNT-RECORD` | **Record Length**: 300 bytes | **Key**: `ACCT-ID` (11 bytes)

| Field Name                 | PIC Clause        | Type      | Length | Business Description              |
|----------------------------|-------------------|-----------|--------|-----------------------------------|
| ACCT-ID                    | 9(11)             | Numeric   | 11     | Unique account identifier         |
| ACCT-ACTIVE-STATUS         | X(01)             | Alpha     | 1      | Account status (Y=Active)         |
| ACCT-CURR-BAL              | S9(10)V99         | Signed Dec| 12     | Current account balance           |
| ACCT-CREDIT-LIMIT          | S9(10)V99         | Signed Dec| 12     | Credit limit amount               |
| ACCT-CASH-CREDIT-LIMIT     | S9(10)V99         | Signed Dec| 12     | Cash advance credit limit         |
| ACCT-OPEN-DATE             | X(10)             | Date      | 10     | Account opening date              |
| ACCT-EXPIRAION-DATE        | X(10)             | Date      | 10     | Account expiration date           |
| ACCT-REISSUE-DATE          | X(10)             | Date      | 10     | Last card reissue date            |
| ACCT-CURR-CYC-CREDIT       | S9(10)V99         | Signed Dec| 12     | Current cycle credit total        |
| ACCT-CURR-CYC-DEBIT        | S9(10)V99         | Signed Dec| 12     | Current cycle debit total         |
| ACCT-ADDR-ZIP              | X(10)             | Alpha     | 10     | Account holder ZIP code           |
| ACCT-GROUP-ID              | X(10)             | Alpha     | 10     | Disclosure/pricing group ID       |
| FILLER                     | X(178)            | --        | 178    | Reserved / future use             |

---

## 3. Card Master (`CVACT02Y`)

**Record Name**: `CARD-RECORD` | **Record Length**: 150 bytes | **Key**: `CARD-NUM` (16 bytes)

| Field Name                 | PIC Clause        | Type      | Length | Business Description              |
|----------------------------|-------------------|-----------|--------|-----------------------------------|
| CARD-NUM                   | X(16)             | Alpha     | 16     | Card number (PAN)                 |
| CARD-ACCT-ID               | 9(11)             | Numeric   | 11     | Linked account ID                 |
| CARD-CVV-CD                | 9(03)             | Numeric   | 3      | Card verification value (CVV)     |
| CARD-EMBOSSED-NAME         | X(50)             | Alpha     | 50     | Name embossed on card             |
| CARD-EXPIRAION-DATE        | X(10)             | Date      | 10     | Card expiration date              |
| CARD-ACTIVE-STATUS         | X(01)             | Alpha     | 1      | Card status (Y=Active)            |
| FILLER                     | X(59)             | --        | 59     | Reserved / future use             |

---

## 4. Card Cross-Reference (`CVACT03Y`)

**Record Name**: `CARD-XREF-RECORD` | **Record Length**: 50 bytes | **Key**: `XREF-CARD-NUM` (16 bytes) | **AIX Key**: `XREF-ACCT-ID` (11 bytes, non-unique)

| Field Name                 | PIC Clause        | Type      | Length | Business Description              |
|----------------------------|-------------------|-----------|--------|-----------------------------------|
| XREF-CARD-NUM              | X(16)             | Alpha     | 16     | Card number (primary key)         |
| XREF-CUST-ID               | 9(09)             | Numeric   | 9      | Associated customer ID            |
| XREF-ACCT-ID               | 9(11)             | Numeric   | 11     | Associated account ID (AIX key)   |
| FILLER                     | X(14)             | --        | 14     | Reserved / future use             |

---

## 5. Customer Master (`CVCUS01Y`)

**Record Name**: `CUSTOMER-RECORD` | **Record Length**: 500 bytes | **Key**: `CUST-ID` (9 bytes)

| Field Name                 | PIC Clause        | Type      | Length | Business Description              |
|----------------------------|-------------------|-----------|--------|-----------------------------------|
| CUST-ID                    | 9(09)             | Numeric   | 9      | Unique customer identifier        |
| CUST-FIRST-NAME            | X(25)             | Alpha     | 25     | Customer first name               |
| CUST-MIDDLE-NAME           | X(25)             | Alpha     | 25     | Customer middle name              |
| CUST-LAST-NAME             | X(25)             | Alpha     | 25     | Customer last name                |
| CUST-ADDR-LINE-1           | X(50)             | Alpha     | 50     | Address line 1                    |
| CUST-ADDR-LINE-2           | X(50)             | Alpha     | 50     | Address line 2                    |
| CUST-ADDR-LINE-3           | X(50)             | Alpha     | 50     | Address line 3                    |
| CUST-ADDR-STATE-CD         | X(02)             | Alpha     | 2      | State code                        |
| CUST-ADDR-COUNTRY-CD       | X(03)             | Alpha     | 3      | Country code                      |
| CUST-ADDR-ZIP              | X(10)             | Alpha     | 10     | ZIP / postal code                 |
| CUST-PHONE-NUM-1           | X(15)             | Alpha     | 15     | Primary phone number              |
| CUST-PHONE-NUM-2           | X(15)             | Alpha     | 15     | Secondary phone number            |
| CUST-SSN                   | 9(09)             | Numeric   | 9      | Social Security Number (PII)      |
| CUST-GOVT-ISSUED-ID        | X(20)             | Alpha     | 20     | Government-issued ID number       |
| CUST-DOB-YYYY-MM-DD        | X(10)             | Date      | 10     | Date of birth (YYYY-MM-DD)        |
| CUST-EFT-ACCOUNT-ID        | 9(09)             | Numeric   | 9      | Linked EFT/bank account ID        |
| CUST-PRI-CARD-HOLDER-IND   | X(01)             | Alpha     | 1      | Primary cardholder indicator      |
| CUST-FICO-CREDIT-SCORE     | 9(03)             | Numeric   | 3      | FICO credit score                 |
| FILLER                     | X(168)            | --        | 168    | Reserved / future use             |

---

## 6. Transaction Master (`CVTRA05Y`)

**Record Name**: `TRAN-RECORD` | **Record Length**: 350 bytes | **Key**: `TRAN-ID` (16 bytes)

| Field Name                 | PIC Clause        | Type      | Length | Business Description              |
|----------------------------|-------------------|-----------|--------|-----------------------------------|
| TRAN-ID                    | X(16)             | Alpha     | 16     | Unique transaction identifier     |
| TRAN-TYPE-CD               | X(02)             | Alpha     | 2      | Transaction type code             |
| TRAN-CAT-CD                | 9(04)             | Numeric   | 4      | Transaction category code         |
| TRAN-SOURCE                | X(10)             | Alpha     | 10     | Transaction source/channel        |
| TRAN-DESC                  | X(100)            | Alpha     | 100    | Transaction description           |
| TRAN-AMT                   | S9(09)V99         | Signed Dec| 11     | Transaction amount                |
| TRAN-MERCHANT-ID           | 9(09)             | Numeric   | 9      | Merchant identifier               |
| TRAN-MERCHANT-NAME         | X(50)             | Alpha     | 50     | Merchant name                     |
| TRAN-MERCHANT-CITY         | X(50)             | Alpha     | 50     | Merchant city                     |
| TRAN-MERCHANT-ZIP          | X(10)             | Alpha     | 10     | Merchant ZIP code                 |
| TRAN-CARD-NUM              | X(16)             | Alpha     | 16     | Card number used                  |
| TRAN-ORIG-TS               | X(26)             | Timestamp | 26     | Original transaction timestamp    |
| TRAN-PROC-TS               | X(26)             | Timestamp | 26     | Processing timestamp              |
| FILLER                     | X(20)             | --        | 20     | Reserved / future use             |

---

## 7. Daily Transaction (`CVTRA06Y`)

**Record Name**: `DALYTRAN-RECORD` | **Record Length**: 350 bytes | **Layout**: Mirrors CVTRA05Y

| Field Name                 | PIC Clause        | Type      | Length | Business Description              |
|----------------------------|-------------------|-----------|--------|-----------------------------------|
| DALYTRAN-ID                | X(16)             | Alpha     | 16     | Daily transaction identifier      |
| DALYTRAN-TYPE-CD           | X(02)             | Alpha     | 2      | Transaction type code             |
| DALYTRAN-CAT-CD            | 9(04)             | Numeric   | 4      | Transaction category code         |
| DALYTRAN-SOURCE            | X(10)             | Alpha     | 10     | Transaction source                |
| DALYTRAN-DESC              | X(100)            | Alpha     | 100    | Transaction description           |
| DALYTRAN-AMT               | S9(09)V99         | Signed Dec| 11     | Transaction amount                |
| DALYTRAN-MERCHANT-ID       | 9(09)             | Numeric   | 9      | Merchant identifier               |
| DALYTRAN-MERCHANT-NAME     | X(50)             | Alpha     | 50     | Merchant name                     |
| DALYTRAN-MERCHANT-CITY     | X(50)             | Alpha     | 50     | Merchant city                     |
| DALYTRAN-MERCHANT-ZIP      | X(10)             | Alpha     | 10     | Merchant ZIP code                 |
| DALYTRAN-CARD-NUM          | X(16)             | Alpha     | 16     | Card number                       |
| DALYTRAN-ORIG-TS           | X(26)             | Timestamp | 26     | Original timestamp                |
| DALYTRAN-PROC-TS           | X(26)             | Timestamp | 26     | Processing timestamp              |
| FILLER                     | X(20)             | --        | 20     | Reserved / future use             |

---

## 8. Transaction Category Balance (`CVTRA01Y`)

**Record Name**: `TRAN-CAT-BAL-RECORD` | **Record Length**: 50 bytes | **Key**: Composite

| Field Name                 | PIC Clause        | Type      | Length | Business Description              |
|----------------------------|-------------------|-----------|--------|-----------------------------------|
| TRANCAT-ACCT-ID            | 9(11)             | Numeric   | 11     | Account ID (key part 1)           |
| TRANCAT-TYPE-CD            | X(02)             | Alpha     | 2      | Transaction type (key part 2)     |
| TRANCAT-CD                 | 9(04)             | Numeric   | 4      | Category code (key part 3)        |
| TRAN-CAT-BAL               | S9(09)V99         | Signed Dec| 11     | Running category balance          |
| FILLER                     | X(22)             | --        | 22     | Reserved / future use             |

---

## 9. Disclosure Group (`CVTRA02Y`)

**Record Name**: `DIS-GROUP-RECORD` | **Record Length**: 50 bytes | **Key**: Composite

| Field Name                 | PIC Clause        | Type      | Length | Business Description              |
|----------------------------|-------------------|-----------|--------|-----------------------------------|
| DIS-ACCT-GROUP-ID          | X(10)             | Alpha     | 10     | Account group ID (key part 1)     |
| DIS-TRAN-TYPE-CD           | X(02)             | Alpha     | 2      | Transaction type (key part 2)     |
| DIS-TRAN-CAT-CD            | 9(04)             | Numeric   | 4      | Category code (key part 3)        |
| DIS-INT-RATE               | S9(04)V99         | Signed Dec| 6      | Interest rate (APR percentage)    |
| FILLER                     | X(28)             | --        | 28     | Reserved / future use             |

---

## 10. Transaction Type (`CVTRA03Y`)

**Record Name**: `TRAN-TYPE-RECORD` | **Record Length**: 60 bytes | **Key**: `TRAN-TYPE`

| Field Name                 | PIC Clause        | Type      | Length | Business Description              |
|----------------------------|-------------------|-----------|--------|-----------------------------------|
| TRAN-TYPE                  | X(02)             | Alpha     | 2      | Transaction type code (key)       |
| TRAN-TYPE-DESC             | X(50)             | Alpha     | 50     | Type description                  |
| FILLER                     | X(08)             | --        | 8      | Reserved / future use             |

---

## 11. Transaction Category (`CVTRA04Y`)

**Record Name**: `TRAN-CAT-RECORD` | **Record Length**: 60 bytes | **Key**: Composite

| Field Name                 | PIC Clause        | Type      | Length | Business Description              |
|----------------------------|-------------------|-----------|--------|-----------------------------------|
| TRAN-TYPE-CD               | X(02)             | Alpha     | 2      | Transaction type (key part 1)     |
| TRAN-CAT-CD                | 9(04)             | Numeric   | 4      | Category code (key part 2)        |
| TRAN-CAT-TYPE-DESC         | X(50)             | Alpha     | 50     | Category description              |
| FILLER                     | X(04)             | --        | 4      | Reserved / future use             |

---

## 12. User Security Record (`CSUSR01Y`)

**Record Name**: `SEC-USER-DATA` | **Record Length**: 80 bytes | **Key**: `SEC-USR-ID`

| Field Name                 | PIC Clause        | Type      | Length | Business Description              |
|----------------------------|-------------------|-----------|--------|-----------------------------------|
| SEC-USR-ID                 | X(08)             | Alpha     | 8      | User login ID (key)               |
| SEC-USR-FNAME              | X(20)             | Alpha     | 20     | User first name                   |
| SEC-USR-LNAME              | X(20)             | Alpha     | 20     | User last name                    |
| SEC-USR-PWD                | X(08)             | Alpha     | 8      | User password (plaintext)         |
| SEC-USR-TYPE               | X(01)             | Alpha     | 1      | User type (A=Admin, U=User)       |
| SEC-USR-FILLER             | X(23)             | --        | 23     | Reserved / future use             |

---

## 13. Statement Transaction Layout (`COSTM01`)

**Record Name**: `TRNX-RECORD` | **Record Length**: 355 bytes | **Key**: Composite (`TRNX-CARD-NUM` + `TRNX-ID`)

| Field Name                 | PIC Clause        | Type      | Length | Business Description              |
|----------------------------|-------------------|-----------|--------|-----------------------------------|
| TRNX-CARD-NUM              | X(16)             | Alpha     | 16     | Card number (key part 1)          |
| TRNX-ID                    | X(16)             | Alpha     | 16     | Transaction ID (key part 2)       |
| TRNX-TYPE-CD               | X(02)             | Alpha     | 2      | Transaction type code             |
| TRNX-CAT-CD                | 9(04)             | Numeric   | 4      | Transaction category code         |
| TRNX-SOURCE                | X(10)             | Alpha     | 10     | Transaction source                |
| TRNX-DESC                  | X(100)            | Alpha     | 100    | Transaction description           |
| TRNX-AMT                   | S9(09)V99         | Signed Dec| 11     | Transaction amount                |
| TRNX-MERCHANT-ID           | 9(09)             | Numeric   | 9      | Merchant ID                       |
| TRNX-MERCHANT-NAME         | X(50)             | Alpha     | 50     | Merchant name                     |
| TRNX-MERCHANT-CITY         | X(50)             | Alpha     | 50     | Merchant city                     |
| TRNX-MERCHANT-ZIP          | X(10)             | Alpha     | 10     | Merchant ZIP code                 |
| TRNX-ORIG-TS               | X(26)             | Timestamp | 26     | Original timestamp                |
| TRNX-PROC-TS               | X(26)             | Timestamp | 26     | Processing timestamp              |
| FILLER                     | X(20)             | --        | 20     | Reserved                          |

---

## 14. Export Record (`CVEXPORT`)

**Record Name**: `EXPORT-RECORD` | **Layout**: Multi-type record with header fields

### 14.1 Export Header Fields

| Field Name                 | PIC Clause        | Type      | Length | Business Description              |
|----------------------------|-------------------|-----------|--------|-----------------------------------|
| EXPORT-REC-TYPE            | X(01)             | Alpha     | 1      | Record type (C/A/X/T/D)          |
| EXPORT-TIMESTAMP           | X(26)             | Timestamp | 26     | Export timestamp                  |
| EXPORT-SEQUENCE-NUM        | 9(09)             | Numeric   | 9      | Sequence counter                  |
| EXPORT-BRANCH-ID           | X(04)             | Alpha     | 4      | Branch identifier                 |
| EXPORT-REGION-CODE         | X(05)             | Alpha     | 5      | Region code                       |

### 14.2 Export Record Types

| Code | Entity     | Prefix         | Description                    |
|------|-----------|----------------|--------------------------------|
| C    | Customer  | EXP-CUST-*     | Customer demographic export    |
| A    | Account   | EXP-ACCT-*     | Account financial export       |
| X    | Xref      | EXP-XREF-*     | Card cross-reference export    |
| T    | Transaction| EXP-TRAN-*    | Transaction detail export      |
| D    | Card      | EXP-CARD-*     | Card data export               |

---

## 15. Common Communication Area (`COCOM01Y`)

**Record Name**: `CARDDEMO-COMMAREA` | **Purpose**: Passes state between CICS programs

| Field Name                 | PIC Clause        | Type      | Length | Business Description              |
|----------------------------|-------------------|-----------|--------|-----------------------------------|
| CDEMO-FROM-TRANID          | X(04)             | Alpha     | 4      | Originating transaction ID        |
| CDEMO-FROM-PROGRAM         | X(08)             | Alpha     | 8      | Originating program name          |
| CDEMO-TO-TRANID            | X(04)             | Alpha     | 4      | Target transaction ID             |
| CDEMO-TO-PROGRAM           | X(08)             | Alpha     | 8      | Target program name               |
| CDEMO-USER-ID              | X(08)             | Alpha     | 8      | Current user ID                   |
| CDEMO-USER-TYPE            | X(01)             | Alpha     | 1      | User type (A/U)                   |
| CDEMO-PGM-CONTEXT          | 9(01)             | Numeric   | 1      | Program context flag              |
| CDEMO-ACCT-ID              | 9(11)             | Numeric   | 11     | Selected account ID               |
| CDEMO-CARD-NUM             | 9(16)             | Numeric   | 16     | Selected card number              |

---

## 16. Date Conversion Control Block (`CODATECN`)

**Record Name**: `CODATECN-REC` | **Purpose**: Parameter block for date formatting calls

| Field Name                 | PIC Clause        | Type      | Length | Business Description              |
|----------------------------|-------------------|-----------|--------|-----------------------------------|
| CODATECN-INP-DATE          | X(10)             | Date      | 10     | Input date                        |
| CODATECN-TYPE              | X(01)             | Alpha     | 1      | Input date format type            |
| CODATECN-OUTTYPE           | X(01)             | Alpha     | 1      | Output date format type           |
| CODATECN-0UT-DATE          | X(10)             | Date      | 10     | Formatted output date             |

---

## 17. Entity Relationship Summary

```
Customer (CVCUS01Y)
   |
   | 1:N via CUST-ID
   v
Card-Xref (CVACT03Y) -----> Account (CVACT01Y)
   |                    N:1 via ACCT-ID
   | 1:1 via CARD-NUM
   v
Card (CVACT02Y)
   |
   | 1:N via CARD-NUM
   v
Transaction (CVTRA05Y) <--- Daily Transaction (CVTRA06Y)
   |                         (staging -> posted)
   |
   | Aggregated by ACCT-ID + TYPE + CATEGORY
   v
Tran-Cat-Balance (CVTRA01Y)
   |
   | Rate lookup by GROUP + TYPE + CATEGORY
   v
Disclosure Group (CVTRA02Y)

Reference Tables:
  Transaction Type (CVTRA03Y) --- type code descriptions
  Transaction Category (CVTRA04Y) --- category code descriptions
  User Security (CSUSR01Y) --- authentication records
```

---

## 18. PII and Sensitive Data Fields

| Entity    | Field                     | Sensitivity | Notes                              |
|-----------|--------------------------|-------------|-------------------------------------|
| Customer  | CUST-SSN                 | **HIGH**    | Social Security Number - encrypt    |
| Customer  | CUST-DOB-YYYY-MM-DD      | HIGH        | Date of birth - PII                 |
| Customer  | CUST-GOVT-ISSUED-ID      | HIGH        | Government ID - PII                 |
| Customer  | CUST-PHONE-NUM-1/2       | MEDIUM      | Personal phone numbers              |
| Customer  | CUST-ADDR-*              | MEDIUM      | Physical address                    |
| Card      | CARD-NUM                 | **HIGH**    | PAN - PCI-DSS regulated             |
| Card      | CARD-CVV-CD              | **HIGH**    | CVV - PCI-DSS regulated             |
| User      | SEC-USR-PWD              | **HIGH**    | Stored in plaintext - must hash     |
| Account   | ACCT-CURR-BAL            | MEDIUM      | Financial data                      |
| Account   | ACCT-CREDIT-LIMIT        | MEDIUM      | Financial data                      |
| Customer  | CUST-FICO-CREDIT-SCORE   | HIGH        | Credit score - regulated            |
| Customer  | CUST-EFT-ACCOUNT-ID      | HIGH        | Bank account number                 |
