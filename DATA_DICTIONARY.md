# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo — Mainframe Credit Card Management System
>
> This dictionary extracts every business entity from the COBOL copybook PIC clauses
> and presents them in a business-friendly format suitable for Java/relational mapping.

---

## Table of Contents

1. [VSAM File Catalog](#vsam-file-catalog)
2. [Account Entity (CVACT01Y)](#1-account-entity--cvact01y)
3. [Card Entity (CVACT02Y)](#2-card-entity--cvact02y)
4. [Card Cross-Reference Entity (CVACT03Y)](#3-card-cross-reference-entity--cvact03y)
5. [Customer Entity (CVCUS01Y)](#4-customer-entity--cvcus01y)
6. [Transaction Entity (CVTRA05Y)](#5-transaction-entity--cvtra05y)
7. [Daily Transaction Entity (CVTRA06Y)](#6-daily-transaction-entity--cvtra06y)
8. [Transaction Category Balance Entity (CVTRA01Y)](#7-transaction-category-balance-entity--cvtra01y)
9. [Disclosure Group Entity (CVTRA02Y)](#8-disclosure-group-entity--cvtra02y)
10. [Transaction Type Entity (CVTRA03Y)](#9-transaction-type-entity--cvtra03y)
11. [Transaction Category Entity (CVTRA04Y)](#10-transaction-category-entity--cvtra04y)
12. [User Security Entity (CSUSR01Y)](#11-user-security-entity--csusr01y)
13. [Communication Area (COCOM01Y)](#12-communication-area--cocom01y)
14. [Export Record (CVEXPORT)](#13-export-record--cvexport)
15. [Statement Transaction Layout (COSTM01)](#14-statement-transaction-layout--costm01)
16. [Report Structures (CVTRA07Y)](#15-report-structures--cvtra07y)
17. [PIC Clause Reference](#pic-clause-reference)
18. [Suggested Java/SQL Type Mappings](#suggested-javasql-type-mappings)

---

## VSAM File Catalog

| VSAM Dataset (DSN)                         | COBOL DD Name | Record Len | Key                  | Access  | Entity                  |
|--------------------------------------------|---------------|------------|----------------------|---------|-------------------------|
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS        | ACCTFILE      | 300 bytes  | ACCT-ID (11,0)       | KSDS    | Account                 |
| AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS        | CARDFILE      | 150 bytes  | CARD-NUM (16,0)      | KSDS    | Card                    |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS        | XREFFILE      | 50 bytes   | XREF-CARD-NUM (16,0) | KSDS    | Card Cross-Reference    |
| AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS        | CUSTFILE      | 500 bytes  | CUST-ID (9,0)        | KSDS    | Customer                |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS        | TRANSACT      | 350 bytes  | TRAN-ID (16,0)       | KSDS    | Transaction             |
| AWS.M2.CARDDEMO.DALYTRAN.PS               | DALYTRAN      | 350 bytes  | DALYTRAN-ID (16,0)   | SEQ     | Daily Transaction       |
| AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS       | TCATBALF      | 50 bytes   | Composite (17,0)     | KSDS    | Trans Category Balance  |
| AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS        | DISCGRP       | 50 bytes   | Composite (16,0)     | KSDS    | Disclosure Group        |
| AWS.M2.CARDDEMO.TRANTYPE.PS               | TRANTYPE      | 60 bytes   | TRAN-TYPE (2,0)      | SEQ     | Transaction Type        |
| AWS.M2.CARDDEMO.TRANCATG.PS               | TRANCATG      | 60 bytes   | Composite (6,0)      | SEQ     | Transaction Category    |
| AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS         | USRSEC        | 80 bytes   | SEC-USR-ID (8,0)     | KSDS    | User Security           |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.AIX.PATH   | CXAIPATH      | 50 bytes   | XREF-ACCT-ID (11,25) | AIX     | Card XRef (by Account)  |

---

## 1. Account Entity — `CVACT01Y`

**Copybook**: `app/cpy/CVACT01Y.cpy` | **Record Length**: 300 bytes | **VSAM**: KSDS

> Represents a credit card account with balances, limits, dates, and status.

| # | COBOL Field Name              | PIC Clause          | Offset | Len | Type     | Business Name               | Java Type      | SQL Type            | Notes                          |
|---|-------------------------------|---------------------|--------|-----|----------|-----------------------------|----------------|---------------------|--------------------------------|
| 1 | ACCT-ID                       | `9(11)`             | 0      | 11  | Numeric  | Account ID                  | `long`         | `BIGINT`            | Primary Key                    |
| 2 | ACCT-ACTIVE-STATUS            | `X(01)`             | 11     | 1   | Alpha    | Active Status               | `String`       | `CHAR(1)`           | 'Y'=Active, 'N'=Inactive      |
| 3 | ACCT-CURR-BAL                 | `S9(10)V99`         | 12     | 12  | Signed   | Current Balance             | `BigDecimal`   | `DECIMAL(12,2)`     | Signed, 2 decimal places       |
| 4 | ACCT-CREDIT-LIMIT             | `S9(10)V99`         | 24     | 12  | Signed   | Credit Limit                | `BigDecimal`   | `DECIMAL(12,2)`     |                                |
| 5 | ACCT-CASH-CREDIT-LIMIT        | `S9(10)V99`         | 36     | 12  | Signed   | Cash Advance Credit Limit   | `BigDecimal`   | `DECIMAL(12,2)`     |                                |
| 6 | ACCT-OPEN-DATE                | `X(10)`             | 48     | 10  | Alpha    | Account Open Date           | `LocalDate`    | `DATE`              | Format: YYYY-MM-DD             |
| 7 | ACCT-EXPIRAION-DATE           | `X(10)`             | 58     | 10  | Alpha    | Account Expiration Date     | `LocalDate`    | `DATE`              | Note: typo in original source  |
| 8 | ACCT-REISSUE-DATE             | `X(10)`             | 68     | 10  | Alpha    | Reissue Date                | `LocalDate`    | `DATE`              |                                |
| 9 | ACCT-CURR-CYC-CREDIT          | `S9(10)V99`         | 78     | 12  | Signed   | Current Cycle Credit        | `BigDecimal`   | `DECIMAL(12,2)`     |                                |
|10 | ACCT-CURR-CYC-DEBIT           | `S9(10)V99`         | 90     | 12  | Signed   | Current Cycle Debit         | `BigDecimal`   | `DECIMAL(12,2)`     |                                |
|11 | ACCT-ADDR-ZIP                 | `X(10)`             | 102    | 10  | Alpha    | ZIP Code                    | `String`       | `VARCHAR(10)`       |                                |
|12 | ACCT-GROUP-ID                 | `X(10)`             | 112    | 10  | Alpha    | Account Group ID            | `String`       | `VARCHAR(10)`       | Links to Disclosure Group      |
|13 | FILLER                        | `X(178)`            | 122    | 178 | —        | Reserved                    | —              | —                   | Padding to 300 bytes           |

**Business Rules**:
- Current Balance = Previous Balance + Debits − Credits + Interest
- Credit Limit must be > 0
- Account Group ID links to Disclosure Group for interest rate lookup

---

## 2. Card Entity — `CVACT02Y`

**Copybook**: `app/cpy/CVACT02Y.cpy` | **Record Length**: 150 bytes | **VSAM**: KSDS

> Represents a physical credit card linked to an account.

| # | COBOL Field Name              | PIC Clause          | Offset | Len | Type     | Business Name               | Java Type      | SQL Type            | Notes                          |
|---|-------------------------------|---------------------|--------|-----|----------|-----------------------------|----------------|---------------------|--------------------------------|
| 1 | CARD-NUM                      | `X(16)`             | 0      | 16  | Alpha    | Card Number                 | `String`       | `CHAR(16)`          | Primary Key                    |
| 2 | CARD-ACCT-ID                  | `9(11)`             | 16     | 11  | Numeric  | Account ID                  | `long`         | `BIGINT`            | FK → Account                   |
| 3 | CARD-CVV-CD                   | `9(03)`             | 27     | 3   | Numeric  | CVV Code                    | `String`       | `CHAR(3)`           | Security code                  |
| 4 | CARD-EMBOSSED-NAME            | `X(50)`             | 30     | 50  | Alpha    | Embossed Name               | `String`       | `VARCHAR(50)`       | Name on card                   |
| 5 | CARD-EXPIRAION-DATE           | `X(10)`             | 80     | 10  | Alpha    | Expiration Date             | `LocalDate`    | `DATE`              | Note: typo in original         |
| 6 | CARD-ACTIVE-STATUS            | `X(01)`             | 90     | 1   | Alpha    | Active Status               | `String`       | `CHAR(1)`           | 'Y'=Active, 'N'=Inactive      |
| 7 | FILLER                        | `X(59)`             | 91     | 59  | —        | Reserved                    | —              | —                   | Padding to 150 bytes           |

**Business Rules**:
- Each card is linked to exactly one account via CARD-ACCT-ID
- Card Number is cross-referenced in the XREF file for transaction lookup

---

## 3. Card Cross-Reference Entity — `CVACT03Y`

**Copybook**: `app/cpy/CVACT03Y.cpy` | **Record Length**: 50 bytes | **VSAM**: KSDS + AIX

> Maps card numbers to account IDs for fast transaction-to-account lookup.

| # | COBOL Field Name              | PIC Clause          | Offset | Len | Type     | Business Name               | Java Type      | SQL Type            | Notes                          |
|---|-------------------------------|---------------------|--------|-----|----------|-----------------------------|----------------|---------------------|--------------------------------|
| 1 | XREF-CARD-NUM                 | `X(16)`             | 0      | 16  | Alpha    | Card Number                 | `String`       | `CHAR(16)`          | Primary Key                    |
| 2 | XREF-CUST-ID                  | `9(09)`             | 16     | 9   | Numeric  | Customer ID                 | `long`         | `BIGINT`            | FK → Customer                  |
| 3 | XREF-ACCT-ID                  | `9(11)`             | 25     | 11  | Numeric  | Account ID                  | `long`         | `BIGINT`            | FK → Account; AIX key          |
| 4 | FILLER                        | `X(14)`             | 36     | 14  | —        | Reserved                    | —              | —                   | Padding to 50 bytes            |

**Business Rules**:
- Alternate index defined on XREF-ACCT-ID (non-unique) for account-based card lookup
- Central join table connecting Card ↔ Customer ↔ Account

---

## 4. Customer Entity — `CVCUS01Y`

**Copybook**: `app/cpy/CVCUS01Y.cpy` | **Record Length**: 500 bytes | **VSAM**: KSDS

> Full customer profile including name, address, demographics, and credit score.

| # | COBOL Field Name              | PIC Clause          | Offset | Len | Type     | Business Name               | Java Type      | SQL Type            | Notes                          |
|---|-------------------------------|---------------------|--------|-----|----------|-----------------------------|----------------|---------------------|--------------------------------|
| 1 | CUST-ID                       | `9(09)`             | 0      | 9   | Numeric  | Customer ID                 | `long`         | `BIGINT`            | Primary Key                    |
| 2 | CUST-FIRST-NAME               | `X(25)`             | 9      | 25  | Alpha    | First Name                  | `String`       | `VARCHAR(25)`       |                                |
| 3 | CUST-MIDDLE-NAME              | `X(25)`             | 34     | 25  | Alpha    | Middle Name                 | `String`       | `VARCHAR(25)`       |                                |
| 4 | CUST-LAST-NAME                | `X(25)`             | 59     | 25  | Alpha    | Last Name                   | `String`       | `VARCHAR(25)`       |                                |
| 5 | CUST-ADDR-LINE-1              | `X(50)`             | 84     | 50  | Alpha    | Address Line 1              | `String`       | `VARCHAR(50)`       |                                |
| 6 | CUST-ADDR-LINE-2              | `X(50)`             | 134    | 50  | Alpha    | Address Line 2              | `String`       | `VARCHAR(50)`       |                                |
| 7 | CUST-ADDR-LINE-3              | `X(50)`             | 184    | 50  | Alpha    | Address Line 3              | `String`       | `VARCHAR(50)`       |                                |
| 8 | CUST-ADDR-STATE-CD            | `X(02)`             | 234    | 2   | Alpha    | State Code                  | `String`       | `CHAR(2)`           | US state abbreviation          |
| 9 | CUST-ADDR-COUNTRY-CD          | `X(03)`             | 236    | 3   | Alpha    | Country Code                | `String`       | `CHAR(3)`           | ISO 3166 country code          |
|10 | CUST-ADDR-ZIP                 | `X(10)`             | 239    | 10  | Alpha    | ZIP/Postal Code             | `String`       | `VARCHAR(10)`       |                                |
|11 | CUST-PHONE-NUM-1              | `X(15)`             | 249    | 15  | Alpha    | Primary Phone               | `String`       | `VARCHAR(15)`       |                                |
|12 | CUST-PHONE-NUM-2              | `X(15)`             | 264    | 15  | Alpha    | Secondary Phone             | `String`       | `VARCHAR(15)`       |                                |
|13 | CUST-SSN                      | `9(09)`             | 279    | 9   | Numeric  | Social Security Number      | `String`       | `CHAR(9)`           | PII — must be encrypted        |
|14 | CUST-GOVT-ISSUED-ID           | `X(20)`             | 288    | 20  | Alpha    | Government Issued ID        | `String`       | `VARCHAR(20)`       | PII — must be encrypted        |
|15 | CUST-DOB-YYYYMMDD             | `X(10)`             | 308    | 10  | Alpha    | Date of Birth               | `LocalDate`    | `DATE`              | Format: YYYY-MM-DD             |
|16 | CUST-EFT-ACCOUNT-ID           | `X(10)`             | 318    | 10  | Alpha    | EFT Account ID              | `String`       | `VARCHAR(10)`       | Electronic Funds Transfer      |
|17 | CUST-PRI-CARD-HOLDER-IND      | `X(01)`             | 328    | 1   | Alpha    | Primary Cardholder?         | `String`       | `CHAR(1)`           | 'Y'=Primary, 'N'=Authorized   |
|18 | CUST-FICO-CREDIT-SCORE        | `9(03)`             | 329    | 3   | Numeric  | FICO Credit Score           | `int`          | `SMALLINT`          | Range: 300-850                 |
|19 | FILLER                        | `X(168)`            | 332    | 168 | —        | Reserved                    | —              | —                   | Padding to 500 bytes           |

**Business Rules**:
- SSN and Government ID are PII — require encryption at rest and masking in UI
- FICO score affects credit limit decisions
- Primary cardholder indicator determines billing responsibility

---

## 5. Transaction Entity — `CVTRA05Y`

**Copybook**: `app/cpy/CVTRA05Y.cpy` | **Record Length**: 350 bytes | **VSAM**: KSDS

> Core transaction record — posted (confirmed) transactions.

| # | COBOL Field Name              | PIC Clause          | Offset | Len | Type     | Business Name               | Java Type         | SQL Type            | Notes                          |
|---|-------------------------------|---------------------|--------|-----|----------|-----------------------------|-------------------|---------------------|--------------------------------|
| 1 | TRAN-ID                       | `X(16)`             | 0      | 16  | Alpha    | Transaction ID              | `String`          | `CHAR(16)`          | Primary Key                    |
| 2 | TRAN-TYPE-CD                  | `X(02)`             | 16     | 2   | Alpha    | Transaction Type Code       | `String`          | `CHAR(2)`           | FK → Transaction Type          |
| 3 | TRAN-CAT-CD                   | `9(04)`             | 18     | 4   | Numeric  | Transaction Category Code   | `int`             | `SMALLINT`          | FK → Transaction Category      |
| 4 | TRAN-SOURCE                   | `X(10)`             | 22     | 10  | Alpha    | Transaction Source          | `String`          | `VARCHAR(10)`       | e.g., 'POS', 'ATM', 'ONLINE'  |
| 5 | TRAN-DESC                     | `X(100)`            | 32     | 100 | Alpha    | Description                 | `String`          | `VARCHAR(100)`      |                                |
| 6 | TRAN-AMT                      | `S9(09)V99`         | 132    | 11  | Signed   | Transaction Amount          | `BigDecimal`      | `DECIMAL(11,2)`     | Signed — debits negative       |
| 7 | TRAN-MERCHANT-ID              | `9(09)`             | 143    | 9   | Numeric  | Merchant ID                 | `long`            | `BIGINT`            |                                |
| 8 | TRAN-MERCHANT-NAME            | `X(50)`             | 152    | 50  | Alpha    | Merchant Name               | `String`          | `VARCHAR(50)`       |                                |
| 9 | TRAN-MERCHANT-CITY            | `X(50)`             | 202    | 50  | Alpha    | Merchant City               | `String`          | `VARCHAR(50)`       |                                |
|10 | TRAN-MERCHANT-ZIP             | `X(10)`             | 252    | 10  | Alpha    | Merchant ZIP                | `String`          | `VARCHAR(10)`       |                                |
|11 | TRAN-CARD-NUM                 | `X(16)`             | 262    | 16  | Alpha    | Card Number                 | `String`          | `CHAR(16)`          | FK → Card                      |
|12 | TRAN-ORIG-TS                  | `X(26)`             | 278    | 26  | Alpha    | Origination Timestamp       | `LocalDateTime`   | `TIMESTAMP`         | When transaction occurred      |
|13 | TRAN-PROC-TS                  | `X(26)`             | 304    | 26  | Alpha    | Processing Timestamp        | `LocalDateTime`   | `TIMESTAMP`         | When transaction was posted    |
|14 | FILLER                        | `X(20)`             | 330    | 20  | —        | Reserved                    | —                 | —                   | Padding to 350 bytes           |

**Business Rules**:
- TRAN-ID is system-generated, unique across all transactions
- Amount is signed: positive = credit to account, negative = debit
- Transaction is linked to card via TRAN-CARD-NUM, then to account via Cross-Reference

---

## 6. Daily Transaction Entity — `CVTRA06Y`

**Copybook**: `app/cpy/CVTRA06Y.cpy` | **Record Length**: 350 bytes | **Input**: Sequential

> Inbound daily transactions awaiting validation and posting. Same layout as CVTRA05Y.

| # | COBOL Field Name              | PIC Clause          | Offset | Len | Type     | Business Name               | Java Type         | SQL Type            |
|---|-------------------------------|---------------------|--------|-----|----------|-----------------------------|-------------------|---------------------|
| 1 | DALYTRAN-ID                   | `X(16)`             | 0      | 16  | Alpha    | Transaction ID              | `String`          | `CHAR(16)`          |
| 2 | DALYTRAN-TYPE-CD              | `X(02)`             | 16     | 2   | Alpha    | Transaction Type Code       | `String`          | `CHAR(2)`           |
| 3 | DALYTRAN-CAT-CD               | `9(04)`             | 18     | 4   | Numeric  | Transaction Category Code   | `int`             | `SMALLINT`          |
| 4 | DALYTRAN-SOURCE               | `X(10)`             | 22     | 10  | Alpha    | Transaction Source          | `String`          | `VARCHAR(10)`       |
| 5 | DALYTRAN-DESC                 | `X(100)`            | 32     | 100 | Alpha    | Description                 | `String`          | `VARCHAR(100)`      |
| 6 | DALYTRAN-AMT                  | `S9(09)V99`         | 132    | 11  | Signed   | Transaction Amount          | `BigDecimal`      | `DECIMAL(11,2)`     |
| 7 | DALYTRAN-MERCHANT-ID          | `9(09)`             | 143    | 9   | Numeric  | Merchant ID                 | `long`            | `BIGINT`            |
| 8 | DALYTRAN-MERCHANT-NAME        | `X(50)`             | 152    | 50  | Alpha    | Merchant Name               | `String`          | `VARCHAR(50)`       |
| 9 | DALYTRAN-MERCHANT-CITY        | `X(50)`             | 202    | 50  | Alpha    | Merchant City               | `String`          | `VARCHAR(50)`       |
|10 | DALYTRAN-MERCHANT-ZIP         | `X(10)`             | 252    | 10  | Alpha    | Merchant ZIP                | `String`          | `VARCHAR(10)`       |
|11 | DALYTRAN-CARD-NUM             | `X(16)`             | 262    | 16  | Alpha    | Card Number                 | `String`          | `CHAR(16)`          |
|12 | DALYTRAN-ORIG-TS              | `X(26)`             | 278    | 26  | Alpha    | Origination Timestamp       | `LocalDateTime`   | `TIMESTAMP`         |
|13 | DALYTRAN-PROC-TS              | `X(26)`             | 304    | 26  | Alpha    | Processing Timestamp        | `LocalDateTime`   | `TIMESTAMP`         |
|14 | FILLER                        | `X(20)`             | 330    | 20  | —        | Reserved                    | —                 | —                   |

**Business Rules**:
- Identical layout to CVTRA05Y; represents unposted/pending transactions
- Validated by CBTRN01C; posted (moved to TRANSACT) by CBTRN02C
- Invalid records written to DALYREJS rejection file

---

## 7. Transaction Category Balance Entity — `CVTRA01Y`

**Copybook**: `app/cpy/CVTRA01Y.cpy` | **Record Length**: 50 bytes | **VSAM**: KSDS

> Running balance per account per transaction type/category combination.

| # | COBOL Field Name              | PIC Clause          | Offset | Len | Type     | Business Name                | Java Type      | SQL Type            | Notes                      |
|---|-------------------------------|---------------------|--------|-----|----------|------------------------------|----------------|---------------------|----------------------------|
| 1 | TRANCAT-ACCT-ID               | `9(11)`             | 0      | 11  | Numeric  | Account ID                   | `long`         | `BIGINT`            | Composite PK part 1; FK   |
| 2 | TRANCAT-TYPE-CD               | `X(02)`             | 11     | 2   | Alpha    | Transaction Type Code        | `String`       | `CHAR(2)`           | Composite PK part 2       |
| 3 | TRANCAT-CD                    | `9(04)`             | 13     | 4   | Numeric  | Transaction Category Code    | `int`          | `SMALLINT`          | Composite PK part 3       |
| 4 | TRAN-CAT-BAL                  | `S9(09)V99`         | 17     | 11  | Signed   | Category Balance             | `BigDecimal`   | `DECIMAL(11,2)`     | Running balance            |
| 5 | FILLER                        | `X(22)`             | 28     | 22  | —        | Reserved                     | —              | —                   |                            |

**Business Rules**:
- Updated during transaction posting (CBTRN02C) and interest calculation (CBACT04C)
- Used for statement generation and interest rate determination

---

## 8. Disclosure Group Entity — `CVTRA02Y`

**Copybook**: `app/cpy/CVTRA02Y.cpy` | **Record Length**: 50 bytes | **VSAM**: KSDS

> Interest rates by account group / transaction type / category.

| # | COBOL Field Name              | PIC Clause          | Offset | Len | Type     | Business Name                | Java Type      | SQL Type            | Notes                      |
|---|-------------------------------|---------------------|--------|-----|----------|------------------------------|----------------|---------------------|----------------------------|
| 1 | DIS-ACCT-GROUP-ID             | `X(10)`             | 0      | 10  | Alpha    | Account Group ID             | `String`       | `VARCHAR(10)`       | Composite PK part 1       |
| 2 | DIS-TRAN-TYPE-CD              | `X(02)`             | 10     | 2   | Alpha    | Transaction Type Code        | `String`       | `CHAR(2)`           | Composite PK part 2       |
| 3 | DIS-TRAN-CAT-CD               | `9(04)`             | 12     | 4   | Numeric  | Transaction Category Code    | `int`          | `SMALLINT`          | Composite PK part 3       |
| 4 | DIS-INT-RATE                  | `S9(04)V99`         | 16     | 6   | Signed   | Interest Rate (%)            | `BigDecimal`   | `DECIMAL(6,2)`      | Annual percentage rate     |
| 5 | FILLER                        | `X(28)`             | 22     | 28  | —        | Reserved                     | —              | —                   |                            |

**Business Rules**:
- Joined to Account via ACCT-GROUP-ID for interest calculation
- Different rates apply per transaction type within the same group

---

## 9. Transaction Type Entity — `CVTRA03Y`

**Copybook**: `app/cpy/CVTRA03Y.cpy` | **Record Length**: 60 bytes | **File**: Sequential

> Lookup table for transaction type codes.

| # | COBOL Field Name              | PIC Clause          | Offset | Len | Type     | Business Name                | Java Type      | SQL Type            | Notes                      |
|---|-------------------------------|---------------------|--------|-----|----------|------------------------------|----------------|---------------------|----------------------------|
| 1 | TRAN-TYPE                     | `X(02)`             | 0      | 2   | Alpha    | Transaction Type Code        | `String`       | `CHAR(2)`           | Primary Key                |
| 2 | TRAN-TYPE-DESC                | `X(50)`             | 2      | 50  | Alpha    | Type Description             | `String`       | `VARCHAR(50)`       | e.g., 'Purchase', 'Return' |
| 3 | FILLER                        | `X(08)`             | 52     | 8   | —        | Reserved                     | —              | —                   |                            |

---

## 10. Transaction Category Entity — `CVTRA04Y`

**Copybook**: `app/cpy/CVTRA04Y.cpy` | **Record Length**: 60 bytes | **File**: Sequential

> Lookup table for transaction category codes within each type.

| # | COBOL Field Name              | PIC Clause          | Offset | Len | Type     | Business Name                | Java Type      | SQL Type            | Notes                      |
|---|-------------------------------|---------------------|--------|-----|----------|------------------------------|----------------|---------------------|----------------------------|
| 1 | TRAN-TYPE-CD                  | `X(02)`             | 0      | 2   | Alpha    | Transaction Type Code        | `String`       | `CHAR(2)`           | Composite PK part 1; FK   |
| 2 | TRAN-CAT-CD                   | `9(04)`             | 2      | 4   | Numeric  | Transaction Category Code    | `int`          | `SMALLINT`          | Composite PK part 2       |
| 3 | TRAN-CAT-TYPE-DESC            | `X(50)`             | 6      | 50  | Alpha    | Category Description         | `String`       | `VARCHAR(50)`       |                            |
| 4 | FILLER                        | `X(04)`             | 56     | 4   | —        | Reserved                     | —              | —                   |                            |

---

## 11. User Security Entity — `CSUSR01Y`

**Copybook**: `app/cpy/CSUSR01Y.cpy` | **Record Length**: 80 bytes | **VSAM**: KSDS

> Application user authentication and authorization record.

| # | COBOL Field Name              | PIC Clause          | Offset | Len | Type     | Business Name                | Java Type      | SQL Type            | Notes                      |
|---|-------------------------------|---------------------|--------|-----|----------|------------------------------|----------------|---------------------|----------------------------|
| 1 | SEC-USR-ID                    | `X(08)`             | 0      | 8   | Alpha    | User ID                      | `String`       | `CHAR(8)`           | Primary Key                |
| 2 | SEC-USR-FNAME                 | `X(20)`             | 8      | 20  | Alpha    | First Name                   | `String`       | `VARCHAR(20)`       |                            |
| 3 | SEC-USR-LNAME                 | `X(20)`             | 28     | 20  | Alpha    | Last Name                    | `String`       | `VARCHAR(20)`       |                            |
| 4 | SEC-USR-PWD                   | `X(08)`             | 48     | 8   | Alpha    | Password                     | `String`       | `CHAR(8)`           | Must be hashed in Java     |
| 5 | SEC-USR-TYPE                  | `X(01)`             | 56     | 1   | Alpha    | User Type                    | `String`       | `CHAR(1)`           | 'A'=Admin, 'U'=Regular     |
| 6 | SEC-USR-FILLER                | `X(23)`             | 57     | 23  | —        | Reserved                     | —              | —                   | Padding to 80 bytes        |

**Business Rules**:
- User Type determines menu routing: Admin → COADM01C, Regular → COMEN01C
- Password stored in plaintext on mainframe; must be hashed (e.g., bcrypt) in Java

---

## 12. Communication Area — `COCOM01Y`

**Copybook**: `app/cpy/COCOM01Y.cpy` | **Type**: CICS COMMAREA (in-memory)

> Passed between online CICS programs to maintain session state (no VSAM persistence).

| # | COBOL Field Name              | PIC Clause          | Len | Business Name                | Notes                              |
|---|-------------------------------|---------------------|-----|------------------------------|------------------------------------|
| 1 | CDEMO-FROM-TRANID             | `X(04)`             | 4   | Source Transaction ID        | e.g., 'CC00'                       |
| 2 | CDEMO-FROM-PROGRAM            | `X(08)`             | 8   | Source Program Name          | Calling program                    |
| 3 | CDEMO-TO-TRANID               | `X(04)`             | 4   | Target Transaction ID        |                                    |
| 4 | CDEMO-TO-PROGRAM              | `X(08)`             | 8   | Target Program Name          | Program to transfer to             |
| 5 | CDEMO-USER-ID                 | `X(08)`             | 8   | Logged-in User ID            |                                    |
| 6 | CDEMO-USER-TYPE               | `X(01)`             | 1   | User Type                    | 'A'=Admin, 'U'=User               |
| 7 | CDEMO-PGM-CONTEXT             | `9(01)`             | 1   | Program Context Flag         | 0=Enter, 1=Re-enter               |
| 8 | CDEMO-CUST-ID                 | `9(09)`             | 9   | Selected Customer ID         |                                    |
| 9 | CDEMO-CUST-FNAME              | `X(25)`             | 25  | Customer First Name          |                                    |
|10 | CDEMO-CUST-MNAME              | `X(25)`             | 25  | Customer Middle Name         |                                    |
|11 | CDEMO-CUST-LNAME              | `X(25)`             | 25  | Customer Last Name           |                                    |
|12 | CDEMO-ACCT-ID                 | `9(11)`             | 11  | Selected Account ID          |                                    |
|13 | CDEMO-ACCT-STATUS             | `X(01)`             | 1   | Account Status               |                                    |
|14 | CDEMO-CARD-NUM                | `9(16)`             | 16  | Selected Card Number         |                                    |
|15 | CDEMO-LAST-MAP                | `X(07)`             | 7   | Last BMS Map Sent            | For screen navigation              |
|16 | CDEMO-LAST-MAPSET             | `X(07)`             | 7   | Last BMS Mapset Sent         |                                    |

**Java Mapping**: → `SessionContext` or HTTP Session attributes

---

## 13. Export Record — `CVEXPORT`

**Copybook**: `app/cpy/CVEXPORT.cpy` | **Type**: Flat file record

> Unified export/import format wrapping all entity types into a single sequential file.

| # | COBOL Field Name              | PIC Clause          | Len | Business Name                | Notes                              |
|---|-------------------------------|---------------------|-----|------------------------------|------------------------------------|
| 1 | EXPORT-REC-TYPE               | `X(01)`             | 1   | Record Type Indicator        | 'C'=Customer, 'A'=Account, etc.   |
| 2 | EXPORT-DATA                   | `X(499)`            | 499 | Record Data                  | Contents vary by record type       |

**Subtypes** (determined by EXPORT-REC-TYPE):
- `C` — Customer record (maps to CVCUS01Y)
- `A` — Account record (maps to CVACT01Y)
- `X` — Cross-reference record (maps to CVACT03Y)
- `T` — Transaction record (maps to CVTRA05Y)
- `R` — Card record (maps to CVACT02Y)

---

## 14. Statement Transaction Layout — `COSTM01`

**Copybook**: `app/cpy/COSTM01.CPY` | **Record Length**: 350 bytes

> Re-keyed transaction layout for statement generation — key is Card Number + Transaction ID.

| # | COBOL Field Name              | PIC Clause          | Offset | Len | Type     | Business Name                |
|---|-------------------------------|---------------------|--------|-----|----------|------------------------------|
| 1 | TRNX-CARD-NUM                 | `X(16)`             | 0      | 16  | Alpha    | Card Number (Primary Key 1)  |
| 2 | TRNX-ID                       | `X(16)`             | 16     | 16  | Alpha    | Transaction ID (Primary Key 2)|
| 3 | TRNX-TYPE-CD                  | `X(02)`             | 32     | 2   | Alpha    | Transaction Type Code        |
| 4 | TRNX-CAT-CD                   | `9(04)`             | 34     | 4   | Numeric  | Transaction Category Code    |
| 5 | TRNX-SOURCE                   | `X(10)`             | 38     | 10  | Alpha    | Transaction Source           |
| 6 | TRNX-DESC                     | `X(100)`            | 48     | 100 | Alpha    | Description                  |
| 7 | TRNX-AMT                      | `S9(09)V99`         | 148    | 11  | Signed   | Amount                       |
| 8 | TRNX-MERCHANT-ID              | `9(09)`             | 159    | 9   | Numeric  | Merchant ID                  |
| 9 | TRNX-MERCHANT-NAME            | `X(50)`             | 168    | 50  | Alpha    | Merchant Name                |
|10 | TRNX-MERCHANT-CITY            | `X(50)`             | 218    | 50  | Alpha    | Merchant City                |
|11 | TRNX-MERCHANT-ZIP             | `X(10)`             | 268    | 10  | Alpha    | Merchant ZIP                 |
|12 | TRNX-ORIG-TS                  | `X(26)`             | 278    | 26  | Alpha    | Origination Timestamp        |
|13 | TRNX-PROC-TS                  | `X(26)`             | 304    | 26  | Alpha    | Processing Timestamp         |
|14 | FILLER                        | `X(20)`             | 330    | 20  | —        | Reserved                     |

---

## 15. Report Structures — `CVTRA07Y`

**Copybook**: `app/cpy/CVTRA07Y.cpy` | **Type**: Working-Storage report formatting

> Defines header, detail, and total lines for the Daily Transaction Report.

| Structure                  | Key Fields                                              | Purpose                    |
|----------------------------|---------------------------------------------------------|----------------------------|
| REPORT-NAME-HEADER         | REPT-SHORT-NAME, REPT-LONG-NAME, date range            | Report title block         |
| TRANSACTION-DETAIL-REPORT  | Trans ID, Account, Type, Category, Source, Amount       | Detail line per transaction |
| TRANSACTION-HEADER-1/2     | Column headers and separator line                       | Page header                |
| REPORT-PAGE-TOTALS         | REPT-PAGE-TOTAL `+ZZZ,ZZZ,ZZZ.ZZ`                      | Page subtotal              |
| REPORT-ACCOUNT-TOTALS      | REPT-ACCOUNT-TOTAL `+ZZZ,ZZZ,ZZZ.ZZ`                   | Account subtotal           |
| REPORT-GRAND-TOTALS        | REPT-GRAND-TOTAL `+ZZZ,ZZZ,ZZZ.ZZ`                     | Grand total                |

---

## PIC Clause Reference

| PIC Pattern        | Meaning                                      | Example              |
|--------------------|----------------------------------------------|----------------------|
| `9(n)`             | Unsigned integer, n digits                   | `9(11)` = 11-digit   |
| `X(n)`             | Alphanumeric, n characters                   | `X(16)` = 16-char    |
| `S9(n)V99`         | Signed decimal, n integer digits + 2 decimal | `S9(09)V99` = ±11.2  |
| `S9(n)V9(m)`       | Signed decimal, n.m places                   | `S9(04)V99` = ±6.2   |
| `+ZZZ,ZZZ,ZZZ.ZZ`  | Edited numeric for display                   | Formatted with commas |
| `PIC XX`           | Same as `X(02)`                              | 2-char alpha          |

---

## Suggested Java/SQL Type Mappings

| COBOL Pattern          | Java Type          | SQL Type             | Notes                                    |
|------------------------|--------------------|----------------------|------------------------------------------|
| `9(1-9)`               | `int`              | `INT`                | Up to 9 digits                           |
| `9(10-18)`             | `long`             | `BIGINT`             | 10+ digits                               |
| `S9(n)V99`             | `BigDecimal`       | `DECIMAL(n+2,2)`     | Always use BigDecimal for money           |
| `X(1)`                 | `String`/`char`    | `CHAR(1)`            | Status flags, type codes                 |
| `X(2-50)`              | `String`           | `VARCHAR(n)`         | Names, descriptions                      |
| `X(10)` date           | `LocalDate`        | `DATE`               | Parse YYYY-MM-DD format                  |
| `X(26)` timestamp      | `LocalDateTime`    | `TIMESTAMP`          | Parse ISO-like timestamp                 |
| COMMAREA               | Session/DTO        | —                    | Map to HTTP session or request-scoped DTO|
| VSAM KSDS              | JPA Entity         | DB Table             | Key → @Id, AIX → @Index                  |
| 88-level conditions    | `enum`             | `CHECK` constraint   | Map condition names to enum constants    |

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)
    │
    ├── 1:N ── Card Cross-Reference (CVACT03Y)
    │              │
    │              ├── N:1 ── Account (CVACT01Y)
    │              │              │
    │              │              ├── 1:N ── Trans Category Balance (CVTRA01Y)
    │              │              │
    │              │              └── N:1 ── Disclosure Group (CVTRA02Y)
    │              │                            └── has Interest Rate
    │              │
    │              └── N:1 ── Card (CVACT02Y)
    │
    └── (via Card) ── Transaction (CVTRA05Y / CVTRA06Y)
                          │
                          ├── N:1 ── Transaction Type (CVTRA03Y)
                          │
                          └── N:1 ── Transaction Category (CVTRA04Y)

User Security (CSUSR01Y) ── standalone authentication table
```

---

*End of Data Dictionary*
