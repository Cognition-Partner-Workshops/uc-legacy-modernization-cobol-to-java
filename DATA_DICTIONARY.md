# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Source**: COBOL Copybooks in `app/cpy/` | **Application**: AWS CardDemo

## Overview

This data dictionary extracts every business entity defined in CardDemo copybooks. Each COBOL `PIC` clause is mapped to a business-friendly description with data types, lengths, and usage notes suitable for Java POJO/DTO design.

---

## 1. Account Master (`CVACT01Y`) -- Record Length: 300 bytes

**Business Entity**: Credit card account -- the central financial record linking customers to balances and limits.

| # | COBOL Field             | PIC Clause       | Type     | Length | Business Name             | Description / Business Rules                        |
|---|-------------------------|------------------|----------|-------:|---------------------------|-----------------------------------------------------|
| 1 | ACCT-ID                 | 9(11)            | Numeric  |     11 | Account Number            | Unique account identifier (primary key)             |
| 2 | ACCT-ACTIVE-STATUS      | X(01)            | Alpha    |      1 | Active Status             | Account status flag (Y=Active, N=Inactive)          |
| 3 | ACCT-CURR-BAL           | S9(10)V99        | Signed Decimal | 12 | Current Balance        | Current outstanding balance (2 decimal places)      |
| 4 | ACCT-CREDIT-LIMIT       | S9(10)V99        | Signed Decimal | 12 | Credit Limit           | Maximum credit allowed on account                   |
| 5 | ACCT-CASH-CREDIT-LIMIT  | S9(10)V99        | Signed Decimal | 12 | Cash Advance Limit     | Maximum cash advance allowed                        |
| 6 | ACCT-OPEN-DATE          | X(10)            | Alpha    |     10 | Account Open Date         | Date account was opened (YYYY-MM-DD)                |
| 7 | ACCT-EXPIRAION-DATE     | X(10)            | Alpha    |     10 | Expiration Date           | Account expiry date (note: typo in original source) |
| 8 | ACCT-REISSUE-DATE       | X(10)            | Alpha    |     10 | Reissue Date              | Last card reissue date                              |
| 9 | ACCT-CURR-CYC-CREDIT    | S9(10)V99        | Signed Decimal | 12 | Cycle Credits          | Credits posted in current billing cycle              |
|10 | ACCT-CURR-CYC-DEBIT     | S9(10)V99        | Signed Decimal | 12 | Cycle Debits           | Debits posted in current billing cycle               |
|11 | ACCT-ADDR-ZIP           | X(10)            | Alpha    |     10 | Billing ZIP Code          | Account billing address postal code                 |
|12 | ACCT-GROUP-ID           | X(10)            | Alpha    |     10 | Account Group             | Pricing/discount group assignment                   |
|13 | FILLER                  | X(178)           | Filler   |    178 | Reserved                  | Reserved for future use                             |

**Java Target**: `AccountEntity` / `AccountDTO`

---

## 2. Card Data (`CVACT02Y`) -- Record Length: 150 bytes

**Business Entity**: Physical credit card associated with an account.

| # | COBOL Field             | PIC Clause       | Type     | Length | Business Name             | Description / Business Rules                        |
|---|-------------------------|------------------|----------|-------:|---------------------------|-----------------------------------------------------|
| 1 | CARD-ACCT-ID            | 9(11)            | Numeric  |     11 | Account Number            | Parent account (FK to Account Master)               |
| 2 | CARD-CARD-NUM           | X(16)            | Alpha    |     16 | Card Number               | 16-digit card number (PAN)                          |
| 3 | CARD-CVV-CD             | 9(03)            | Numeric  |      3 | CVV Code                  | Card verification value                             |
| 4 | CARD-EMBOSSED-NAME      | X(50)            | Alpha    |     50 | Embossed Name             | Name printed on card                                |
| 5 | CARD-EXPIRAION-DATE     | X(10)            | Alpha    |     10 | Expiration Date           | Card expiry date (YYYY-MM-DD)                       |
| 6 | CARD-ACTIVE-STATUS      | X(01)            | Alpha    |      1 | Card Status               | Active status flag (Y/N)                            |
| 7 | FILLER                  | X(59)            | Filler   |     59 | Reserved                  | Reserved for future use                             |

**Java Target**: `CardEntity` / `CardDTO`

---

## 3. Customer Master (`CVCUS01Y`) -- Record Length: 500 bytes

**Business Entity**: Customer personal and demographic information.

| # | COBOL Field             | PIC Clause       | Type     | Length | Business Name             | Description / Business Rules                        |
|---|-------------------------|------------------|----------|-------:|---------------------------|-----------------------------------------------------|
| 1 | CUST-ID                 | 9(09)            | Numeric  |      9 | Customer ID               | Unique customer identifier (primary key)            |
| 2 | CUST-FIRST-NAME         | X(25)            | Alpha    |     25 | First Name                | Customer first name                                 |
| 3 | CUST-MIDDLE-NAME        | X(25)            | Alpha    |     25 | Middle Name               | Customer middle name                                |
| 4 | CUST-LAST-NAME          | X(25)            | Alpha    |     25 | Last Name                 | Customer last name                                  |
| 5 | CUST-ADDR-LINE-1        | X(50)            | Alpha    |     50 | Address Line 1            | Primary street address                              |
| 6 | CUST-ADDR-LINE-2        | X(50)            | Alpha    |     50 | Address Line 2            | Secondary address (apt, suite, etc.)                |
| 7 | CUST-ADDR-LINE-3        | X(50)            | Alpha    |     50 | Address Line 3            | Additional address line                             |
| 8 | CUST-ADDR-STATE-CD      | X(02)            | Alpha    |      2 | State Code                | US state abbreviation                               |
| 9 | CUST-ADDR-COUNTRY-CD    | X(03)            | Alpha    |      3 | Country Code              | ISO country code                                    |
|10 | CUST-ADDR-ZIP           | X(10)            | Alpha    |     10 | ZIP/Postal Code           | Postal code                                         |
|11 | CUST-PHONE-NUM-1        | X(15)            | Alpha    |     15 | Primary Phone             | Primary contact number                              |
|12 | CUST-PHONE-NUM-2        | X(15)            | Alpha    |     15 | Secondary Phone           | Alternate contact number                            |
|13 | CUST-SSN                | 9(09)            | Numeric  |      9 | SSN                       | Social Security Number (PII - encrypt in Java)      |
|14 | CUST-GOVT-ISSUED-ID     | X(20)            | Alpha    |     20 | Government ID             | Government-issued identification number             |
|15 | CUST-DOB-YYYYMMDD       | X(10)            | Alpha    |     10 | Date of Birth             | Customer DOB (YYYY-MM-DD format)                    |
|16 | CUST-EFT-ACCOUNT-ID     | X(10)            | Alpha    |     10 | EFT Account               | Electronic funds transfer account                   |
|17 | CUST-PRI-CARD-HOLDER-IND| X(01)            | Alpha    |      1 | Primary Cardholder Flag   | Y = primary holder, N = authorized user             |
|18 | CUST-FICO-CREDIT-SCORE  | 9(03)            | Numeric  |      3 | FICO Score                | Credit score (300-850 range)                        |
|19 | FILLER                  | X(168)           | Filler   |    168 | Reserved                  | Reserved for future use                             |

**Java Target**: `CustomerEntity` / `CustomerDTO`
**Security Note**: SSN, DOB, and Government ID are PII fields -- must be encrypted at rest and masked in logs.

---

## 4. Card Cross-Reference (`CVACT03Y`) -- Record Length: 50 bytes

**Business Entity**: Links card numbers to account IDs -- the lookup table enabling card-based queries.

| # | COBOL Field             | PIC Clause       | Type     | Length | Business Name             | Description / Business Rules                        |
|---|-------------------------|------------------|----------|-------:|---------------------------|-----------------------------------------------------|
| 1 | XREF-CARD-NUM           | X(16)            | Alpha    |     16 | Card Number               | Card PAN (primary key)                              |
| 2 | XREF-CUST-ID            | 9(09)            | Numeric  |      9 | Customer ID               | FK to Customer Master                               |
| 3 | XREF-ACCT-ID            | 9(11)            | Numeric  |     11 | Account Number            | FK to Account Master                                |
| 4 | FILLER                  | X(14)            | Filler   |     14 | Reserved                  | Reserved for future use                             |

**Java Target**: In a relational model, this becomes a join table or foreign keys on `Card` entity.

---

## 5. Transaction Record (`CVTRA05Y`) -- Record Length: 350 bytes

**Business Entity**: Individual credit card transaction -- the core financial event record.

| # | COBOL Field             | PIC Clause       | Type     | Length | Business Name             | Description / Business Rules                        |
|---|-------------------------|------------------|----------|-------:|---------------------------|-----------------------------------------------------|
| 1 | TRAN-ID                 | X(16)            | Alpha    |     16 | Transaction ID            | Unique transaction identifier (primary key)         |
| 2 | TRAN-TYPE-CD            | X(02)            | Alpha    |      2 | Transaction Type          | Type code (FK to Transaction Type table)            |
| 3 | TRAN-CAT-CD             | 9(04)            | Numeric  |      4 | Category Code             | Transaction category (FK to Category table)         |
| 4 | TRAN-SOURCE             | X(10)            | Alpha    |     10 | Transaction Source        | Origin channel (POS, ATM, WEB, etc.)                |
| 5 | TRAN-DESC               | X(100)           | Alpha    |    100 | Description               | Free-text transaction description                   |
| 6 | TRAN-AMT                | S9(09)V99        | Signed Decimal | 11 | Transaction Amount     | Signed amount (negative = credit/refund)            |
| 7 | TRAN-MERCHANT-ID        | 9(09)            | Numeric  |      9 | Merchant ID               | Merchant identifier                                 |
| 8 | TRAN-MERCHANT-NAME      | X(50)            | Alpha    |     50 | Merchant Name             | Merchant business name                              |
| 9 | TRAN-MERCHANT-CITY      | X(50)            | Alpha    |     50 | Merchant City             | Merchant location city                              |
|10 | TRAN-MERCHANT-ZIP       | X(10)            | Alpha    |     10 | Merchant ZIP              | Merchant postal code                                |
|11 | TRAN-CARD-NUM           | X(16)            | Alpha    |     16 | Card Number               | Card used for transaction (FK via XREF)             |
|12 | TRAN-ORIG-TS            | X(26)            | Alpha    |     26 | Origination Timestamp     | When transaction was initiated                      |
|13 | TRAN-PROC-TS            | X(26)            | Alpha    |     26 | Processing Timestamp      | When transaction was processed/posted               |
|14 | FILLER                  | X(20)            | Filler   |     20 | Reserved                  | Reserved for future use                             |

**Java Target**: `TransactionEntity` / `TransactionDTO`

---

## 6. Daily Transaction (`CVTRA06Y`) -- Record Length: 350 bytes

**Business Entity**: Unposted daily transaction -- identical structure to master but represents pending items.

| # | COBOL Field             | PIC Clause       | Type     | Length | Business Name             | Description / Business Rules                        |
|---|-------------------------|------------------|----------|-------:|---------------------------|-----------------------------------------------------|
| 1 | DALYTRAN-ID             | X(16)            | Alpha    |     16 | Daily Transaction ID      | Same layout as TRAN-ID                              |
| 2 | DALYTRAN-TYPE-CD        | X(02)            | Alpha    |      2 | Transaction Type          | Same as TRAN-TYPE-CD                                |
| 3 | DALYTRAN-CAT-CD         | 9(04)            | Numeric  |      4 | Category Code             | Same as TRAN-CAT-CD                                 |
| 4 | DALYTRAN-SOURCE         | X(10)            | Alpha    |     10 | Transaction Source        | Same as TRAN-SOURCE                                 |
| 5 | DALYTRAN-DESC           | X(100)           | Alpha    |    100 | Description               | Same as TRAN-DESC                                   |
| 6 | DALYTRAN-AMT            | S9(09)V99        | Signed Decimal | 11 | Transaction Amount     | Same as TRAN-AMT                                    |
| 7 | DALYTRAN-MERCHANT-ID    | 9(09)            | Numeric  |      9 | Merchant ID               | Same as TRAN-MERCHANT-ID                            |
| 8 | DALYTRAN-MERCHANT-NAME  | X(50)            | Alpha    |     50 | Merchant Name             | Same as TRAN-MERCHANT-NAME                          |
| 9 | DALYTRAN-MERCHANT-CITY  | X(50)            | Alpha    |     50 | Merchant City             | Same as TRAN-MERCHANT-CITY                          |
|10 | DALYTRAN-MERCHANT-ZIP   | X(10)            | Alpha    |     10 | Merchant ZIP              | Same as TRAN-MERCHANT-ZIP                           |
|11 | DALYTRAN-CARD-NUM       | X(16)            | Alpha    |     16 | Card Number               | Same as TRAN-CARD-NUM                               |
|12 | DALYTRAN-ORIG-TS        | X(26)            | Alpha    |     26 | Origination Timestamp     | Same as TRAN-ORIG-TS                                |
|13 | DALYTRAN-PROC-TS        | X(26)            | Alpha    |     26 | Processing Timestamp      | Same as TRAN-PROC-TS                                |
|14 | FILLER                  | X(20)            | Filler   |     20 | Reserved                  | Reserved for future use                             |

**Java Target**: Merge with `TransactionEntity` -- add `status` field (PENDING/POSTED) to differentiate.

---

## 7. Transaction Category Balance (`CVTRA01Y`) -- Record Length: 50 bytes

**Business Entity**: Running balance per account per transaction category -- used for interest calculations.

| # | COBOL Field             | PIC Clause       | Type     | Length | Business Name             | Description / Business Rules                        |
|---|-------------------------|------------------|----------|-------:|---------------------------|-----------------------------------------------------|
| 1 | TRANCAT-ACCT-ID         | 9(11)            | Numeric  |     11 | Account Number            | FK to Account Master                                |
| 2 | TRANCAT-TYPE-CD         | X(02)            | Alpha    |      2 | Transaction Type          | Type code for category                              |
| 3 | TRANCAT-CD              | 9(04)            | Numeric  |      4 | Category Code             | Category code                                       |
| 4 | TRAN-CAT-BAL            | S9(09)V99        | Signed Decimal | 11 | Category Balance       | Running balance for this category                   |
| 5 | FILLER                  | X(22)            | Filler   |     22 | Reserved                  | Reserved for future use                             |

**Java Target**: `CategoryBalanceEntity`

---

## 8. Disclosure Group (`CVTRA02Y`) -- Record Length: 50 bytes

**Business Entity**: Interest rate schedule per account group and transaction category.

| # | COBOL Field             | PIC Clause       | Type     | Length | Business Name             | Description / Business Rules                        |
|---|-------------------------|------------------|----------|-------:|---------------------------|-----------------------------------------------------|
| 1 | DIS-ACCT-GROUP-ID       | X(10)            | Alpha    |     10 | Account Group ID          | Pricing group identifier                            |
| 2 | DIS-TRAN-TYPE-CD        | X(02)            | Alpha    |      2 | Transaction Type          | Transaction type code                               |
| 3 | DIS-TRAN-CAT-CD         | 9(04)            | Numeric  |      4 | Category Code             | Category code                                       |
| 4 | DIS-INT-RATE            | S9(04)V99        | Signed Decimal |  6 | Interest Rate          | Annual interest rate for this combination           |
| 5 | FILLER                  | X(28)            | Filler   |     28 | Reserved                  | Reserved for future use                             |

**Java Target**: `InterestRateScheduleEntity`

---

## 9. Transaction Type (`CVTRA03Y`) -- Record Length: 60 bytes

**Business Entity**: Lookup table for transaction type codes and descriptions.

| # | COBOL Field             | PIC Clause       | Type     | Length | Business Name             | Description / Business Rules                        |
|---|-------------------------|------------------|----------|-------:|---------------------------|-----------------------------------------------------|
| 1 | TRAN-TYPE               | X(02)            | Alpha    |      2 | Type Code                 | Transaction type code (primary key)                 |
| 2 | TRAN-TYPE-DESC          | X(50)            | Alpha    |     50 | Type Description          | Human-readable description                          |
| 3 | FILLER                  | X(08)            | Filler   |      8 | Reserved                  | Reserved for future use                             |

**Java Target**: `TransactionTypeEntity` or Java `enum`

---

## 10. Transaction Category (`CVTRA04Y`) -- Record Length: 60 bytes

**Business Entity**: Lookup table for transaction category codes within a type.

| # | COBOL Field             | PIC Clause       | Type     | Length | Business Name             | Description / Business Rules                        |
|---|-------------------------|------------------|----------|-------:|---------------------------|-----------------------------------------------------|
| 1 | TRAN-TYPE-CD            | X(02)            | Alpha    |      2 | Type Code                 | Parent transaction type (composite key)             |
| 2 | TRAN-CAT-CD             | 9(04)            | Numeric  |      4 | Category Code             | Category within type (composite key)                |
| 3 | TRAN-CAT-TYPE-DESC      | X(50)            | Alpha    |     50 | Category Description      | Human-readable description                          |
| 4 | FILLER                  | X(04)            | Filler   |      4 | Reserved                  | Reserved for future use                             |

**Java Target**: `TransactionCategoryEntity` or Java `enum`

---

## 11. User Security (`CSUSR01Y`) -- Record Length: 80 bytes

**Business Entity**: Application user credentials and role assignment.

| # | COBOL Field             | PIC Clause       | Type     | Length | Business Name             | Description / Business Rules                        |
|---|-------------------------|------------------|----------|-------:|---------------------------|-----------------------------------------------------|
| 1 | SEC-USR-ID              | X(08)            | Alpha    |      8 | User ID                   | Login username (primary key)                        |
| 2 | SEC-USR-FNAME           | X(20)            | Alpha    |     20 | First Name                | User first name                                     |
| 3 | SEC-USR-LNAME           | X(20)            | Alpha    |     20 | Last Name                 | User last name                                      |
| 4 | SEC-USR-PWD             | X(08)            | Alpha    |      8 | Password                  | Plain-text password (must hash in Java!)            |
| 5 | SEC-USR-TYPE            | X(01)            | Alpha    |      1 | User Type                 | A=Admin, U=Regular User                             |
| 6 | SEC-USR-FILLER          | X(23)            | Filler   |     23 | Reserved                  | Reserved for future use                             |

**Java Target**: `UserEntity` with Spring Security integration
**Security Note**: Passwords stored in plain text in VSAM -- must implement bcrypt/scrypt hashing in Java.

---

## 12. Common Communication Area (`COCOM01Y`)

**Business Entity**: Inter-program communication structure passed via CICS COMMAREA.

| # | COBOL Field                  | PIC Clause  | Type   | Length | Business Name              | Description                                  |
|---|------------------------------|-------------|--------|-------:|----------------------------|----------------------------------------------|
| 1 | CDEMO-FROM-TRANID            | X(04)       | Alpha  |      4 | Source Transaction ID       | CICS transaction ID of caller                |
| 2 | CDEMO-FROM-PROGRAM           | X(08)       | Alpha  |      8 | Source Program              | Program name of caller                       |
| 3 | CDEMO-TO-TRANID              | X(04)       | Alpha  |      4 | Target Transaction ID       | Target CICS transaction                      |
| 4 | CDEMO-TO-PROGRAM             | X(08)       | Alpha  |      8 | Target Program              | Program to transfer to                       |
| 5 | CDEMO-USER-ID                | X(08)       | Alpha  |      8 | Current User ID             | Logged-in user                               |
| 6 | CDEMO-USER-TYPE              | X(01)       | Alpha  |      1 | User Type                   | A=Admin, U=User                              |
| 7 | CDEMO-PGM-CONTEXT            | (varies)    | Group  |   varies | Program Context            | Program-specific working data                |

**Java Target**: Replace with HTTP session / JWT token + request DTOs. Navigation becomes URL routing.

---

## 13. Export Record (`CVEXPORT`) -- Variable Layout

**Business Entity**: Multi-record export format for branch data migration.

| # | COBOL Field             | PIC Clause       | Type     | Length | Business Name             | Description / Business Rules                        |
|---|-------------------------|------------------|----------|-------:|---------------------------|-----------------------------------------------------|
| 1 | EXP-RECORD-TYPE         | X(02)            | Alpha    |      2 | Record Type               | CU=Customer, AC=Account, XR=Xref, TR=Transaction, CD=Card |
| 2 | EXP-TIMESTAMP           | X(26)            | Alpha    |     26 | Export Timestamp           | When record was exported                            |
| 3 | EXP-DATA                | X(500)           | Alpha    |    500 | Record Data               | Embedded record (layout depends on type)            |

**Java Target**: Replace with JSON/CSV export with typed records.

---

## 14. Transaction Report Layout (`CVTRA07Y`)

**Business Entity**: Print formatting structures for the daily transaction report.

| # | COBOL Field                  | PIC Clause       | Type   | Length | Business Name              |
|---|------------------------------|------------------|--------|-------:|----------------------------|
| 1 | REPT-SHORT-NAME              | X(38)            | Alpha  |     38 | Report Short Name          |
| 2 | REPT-LONG-NAME               | X(41)            | Alpha  |     41 | Report Title               |
| 3 | REPT-START-DATE              | X(10)            | Alpha  |     10 | Report Start Date          |
| 4 | REPT-END-DATE                | X(10)            | Alpha  |     10 | Report End Date            |
| 5 | TRAN-REPORT-TRANS-ID         | X(16)            | Alpha  |     16 | Transaction ID             |
| 6 | TRAN-REPORT-ACCOUNT-ID       | X(11)            | Alpha  |     11 | Account ID                 |
| 7 | TRAN-REPORT-TYPE-CD          | X(02)            | Alpha  |      2 | Type Code                  |
| 8 | TRAN-REPORT-TYPE-DESC        | X(15)            | Alpha  |     15 | Type Description           |
| 9 | TRAN-REPORT-CAT-CD           | 9(04)            | Numeric|      4 | Category Code              |
|10 | TRAN-REPORT-CAT-DESC         | X(29)            | Alpha  |     29 | Category Description       |
|11 | TRAN-REPORT-SOURCE           | X(10)            | Alpha  |     10 | Transaction Source         |
|12 | TRAN-REPORT-AMT              | -ZZZ,ZZZ,ZZZ.ZZ | Edited |     15 | Amount (formatted)         |
|13 | REPT-PAGE-TOTAL              | +ZZZ,ZZZ,ZZZ.ZZ | Edited |     15 | Page Total                 |
|14 | REPT-ACCOUNT-TOTAL           | +ZZZ,ZZZ,ZZZ.ZZ | Edited |     15 | Account Total              |
|15 | REPT-GRAND-TOTAL             | +ZZZ,ZZZ,ZZZ.ZZ | Edited |     15 | Grand Total                |

**Java Target**: Replace with reporting library (JasperReports) or HTML/PDF template.

---

## 15. Statement Record (`COSTM01`) -- Record Length: ~350 bytes

**Business Entity**: Altered transaction layout keyed by card number + transaction ID for statement generation.

| # | COBOL Field             | PIC Clause       | Type     | Length | Business Name             | Description / Business Rules                        |
|---|-------------------------|------------------|----------|-------:|---------------------------|-----------------------------------------------------|
| 1 | TRNX-CARD-NUM           | X(16)            | Alpha    |     16 | Card Number               | Primary sort key for statements                     |
| 2 | TRNX-ID                 | X(16)            | Alpha    |     16 | Transaction ID            | Secondary sort key                                  |
| 3 | TRNX-TYPE-CD            | X(02)            | Alpha    |      2 | Transaction Type          | Type code                                           |
| 4 | TRNX-CAT-CD             | 9(04)            | Numeric  |      4 | Category Code             | Category code                                       |
| 5 | TRNX-SOURCE             | X(10)            | Alpha    |     10 | Source                    | Transaction origin                                  |
| 6 | TRNX-DESC               | X(100)           | Alpha    |    100 | Description               | Transaction description                             |
| 7 | TRNX-AMT                | S9(09)V99        | Signed Decimal | 11 | Amount                 | Transaction amount                                  |
| 8 | TRNX-MERCHANT-ID        | 9(09)            | Numeric  |      9 | Merchant ID               | Merchant identifier                                 |
| 9 | TRNX-MERCHANT-NAME      | X(50)            | Alpha    |     50 | Merchant Name             | Merchant name                                       |
|10 | TRNX-MERCHANT-CITY      | X(50)            | Alpha    |     50 | Merchant City             | Merchant city                                       |
|11 | TRNX-MERCHANT-ZIP       | X(10)            | Alpha    |     10 | Merchant ZIP              | Merchant postal code                                |
|12 | TRNX-ORIG-TS            | X(26)            | Alpha    |     26 | Origination Timestamp     | Transaction origination time                        |
|13 | TRNX-PROC-TS            | X(26)            | Alpha    |     26 | Processing Timestamp      | Transaction processing time                         |
|14 | FILLER                  | X(20)            | Filler   |     20 | Reserved                  | Reserved                                            |

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)
  |
  +-- 1:N --> Account (CVACT01Y)
  |             |
  |             +-- 1:N --> Card (CVACT02Y)
  |             |             |
  |             |             +-- via XREF (CVACT03Y) --> Transaction (CVTRA05Y)
  |             |
  |             +-- 1:N --> Category Balance (CVTRA01Y)
  |
  +-- via XREF --> Card Cross-Reference (CVACT03Y)

Transaction Type (CVTRA03Y) --lookup--> Transaction (CVTRA05Y)
Transaction Category (CVTRA04Y) --lookup--> Transaction (CVTRA05Y)
Disclosure Group (CVTRA02Y) --rates--> Account Group + Category

User Security (CSUSR01Y) -- standalone authentication table

Daily Transaction (CVTRA06Y) --posted-to--> Transaction (CVTRA05Y)
```

---

## COBOL-to-Java Type Mapping Reference

| COBOL PIC Clause   | Java Type         | Notes                                    |
|--------------------|-------------------|------------------------------------------|
| `9(n)`             | `long` / `String` | Use `String` if leading zeros matter     |
| `X(n)`             | `String`          | Trim trailing spaces                     |
| `S9(n)V99`         | `BigDecimal`      | Always use BigDecimal for money          |
| `S9(n)V9(m)`       | `BigDecimal`      | Scale = m decimal places                 |
| `9(n)V99`          | `BigDecimal`      | Unsigned decimal                         |
| `-ZZZ,ZZZ,ZZZ.ZZ`  | `String` (display)| Formatted output only -- use NumberFormat|
| FILLER             | (omit)            | Do not map to Java fields                |

---

## VSAM-to-RDBMS Mapping

| VSAM Dataset                    | Table Name              | Key Type     | Record Copybook |
|---------------------------------|-------------------------|--------------|-----------------|
| ACCTDATA.VSAM.KSDS             | `account`               | KSDS (11)    | CVACT01Y        |
| CARDDATA.VSAM.KSDS             | `card`                  | KSDS (27)    | CVACT02Y        |
| CUSTDATA.VSAM.KSDS             | `customer`              | KSDS (9)     | CVCUS01Y        |
| CARDXREF.VSAM.KSDS             | `card_xref`             | KSDS (16)    | CVACT03Y        |
| TRANSACT.VSAM.KSDS             | `transaction`           | KSDS (16)    | CVTRA05Y        |
| USRSEC.VSAM.KSDS               | `app_user`              | KSDS (8)     | CSUSR01Y        |
| TCATBALF.VSAM.KSDS             | `category_balance`      | KSDS (17)    | CVTRA01Y        |
| DISCGRP.VSAM.KSDS              | `interest_rate`         | KSDS (16)    | CVTRA02Y        |
| TRANTYPE.VSAM.KSDS             | `transaction_type`      | KSDS (2)     | CVTRA03Y        |
| TRANCATG.VSAM.KSDS             | `transaction_category`  | KSDS (6)     | CVTRA04Y        |
| DALYTRAN.PS                     | `daily_transaction`     | Sequential   | CVTRA06Y        |
