# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo | **Source**: Copybook PIC clause analysis
>
> This document translates COBOL copybook record layouts into a business-friendly data dictionary suitable for Java/relational database modernization planning.

---

## Table of Contents

1. [Entity Relationship Overview](#entity-relationship-overview)
2. [Account Entity](#account-entity-cvact01y)
3. [Credit Card Entity](#credit-card-entity-cvact02y)
4. [Card Cross-Reference Entity](#card-cross-reference-entity-cvact03y)
5. [Customer Entity](#customer-entity-cvcus01y)
6. [Transaction Entity](#transaction-entity-cvtra05y)
7. [Daily Transaction Entity](#daily-transaction-entity-cvtra06y)
8. [User Security Entity](#user-security-entity-csusr01y)
9. [Transaction Category Balance Entity](#transaction-category-balance-entity-cvtra01y)
10. [Disclosure Group Entity](#disclosure-group-entity-cvtra02y)
11. [Transaction Type Entity](#transaction-type-entity-cvtra03y)
12. [Transaction Category Entity](#transaction-category-entity-cvtra04y)
13. [Report Structures](#report-structures-cvtra07y)
14. [Export Record Entity](#export-record-entity-cvexport)
15. [Communication Area](#communication-area-cocom01y)
16. [PIC Clause Reference](#pic-clause-reference)

---

## Entity Relationship Overview

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│   Customer   │1────N│   Account    │1────N│  Credit Card │
│  (CVCUS01Y)  │       │  (CVACT01Y)  │       │  (CVACT02Y)  │
└──────────────┘       └──────┬───────┘       └──────┬───────┘
                              │                      │
                              │                      │
                       ┌──────┴───────┐       ┌──────┴───────┐
                       │ Transaction  │       │  Card XREF   │
                       │  (CVTRA05Y)  │       │  (CVACT03Y)  │
                       └──────┬───────┘       └──────────────┘
                              │
                 ┌────────────┼────────────┐
                 │            │            │
          ┌──────┴──────┐ ┌──┴──────┐ ┌───┴──────────┐
          │  Tran Type  │ │Tran Cat │ │ Cat Balance  │
          │ (CVTRA03Y)  │ │(CVTRA04Y)│ │ (CVTRA01Y)  │
          └─────────────┘ └─────────┘ └──────────────┘

┌──────────────┐       ┌──────────────┐
│ User Security│       │  Disclosure  │
│  (CSUSR01Y)  │       │   Group      │
└──────────────┘       │  (CVTRA02Y)  │
                       └──────────────┘
```

---

## Account Entity (CVACT01Y)

**Copybook**: `app/cpy/CVACT01Y.cpy` | **Record Size**: ~300 bytes | **VSAM File**: `ACCTDATA.VSAM.KSDS`

> Represents a credit card account with balance, credit limits, and cycle information.

| COBOL Field Name           | PIC Clause         | Java Type    | DB Column Suggestion     | Business Description                      |
|---------------------------|--------------------|-------------|--------------------------|-------------------------------------------|
| ACCT-ID                   | PIC 9(11)          | long         | ACCOUNT_ID (PK)          | Unique account identifier                 |
| ACCT-ACTIVE-STATUS        | PIC X(01)          | String       | ACTIVE_STATUS            | Account status flag (Y/N)                 |
| ACCT-CURR-BAL             | PIC S9(10)V99 COMP-3 | BigDecimal | CURRENT_BALANCE          | Current account balance (signed decimal)  |
| ACCT-CREDIT-LIMIT         | PIC S9(10)V99      | BigDecimal   | CREDIT_LIMIT             | Maximum credit limit                      |
| ACCT-CASH-CREDIT-LIMIT    | PIC S9(10)V99 COMP-3 | BigDecimal | CASH_CREDIT_LIMIT        | Cash advance credit limit                 |
| ACCT-OPEN-DATE            | PIC X(10)          | LocalDate    | OPEN_DATE                | Account opening date                      |
| ACCT-EXPIRAION-DATE       | PIC X(10)          | LocalDate    | EXPIRATION_DATE          | Account expiration date                   |
| ACCT-REISSUE-DATE         | PIC X(10)          | LocalDate    | REISSUE_DATE             | Last card reissue date                    |
| ACCT-CURR-CYC-CREDIT      | PIC S9(10)V99     | BigDecimal   | CURRENT_CYCLE_CREDIT     | Credits in current billing cycle          |
| ACCT-CURR-CYC-DEBIT       | PIC S9(10)V99 COMP| BigDecimal   | CURRENT_CYCLE_DEBIT      | Debits in current billing cycle           |
| ACCT-ADDR-ZIP             | PIC X(10)          | String       | ADDRESS_ZIP              | Account holder ZIP code                   |
| ACCT-GROUP-ID             | PIC X(10)          | String       | GROUP_ID                 | Disclosure/rate group identifier          |

**Key**: `ACCT-ID` (Primary Key, VSAM KSDS key)

**Business Rules**:
- Balance is signed to allow credit balances (overpayment)
- COMP-3 (packed decimal) fields require special conversion during migration
- Group ID links to Disclosure Group for interest rate determination

---

## Credit Card Entity (CVACT02Y)

**Copybook**: `app/cpy/CVACT02Y.cpy` | **Record Size**: ~150 bytes | **VSAM File**: `CARDDATA.VSAM.KSDS`

> Represents a physical credit card linked to an account.

| COBOL Field Name           | PIC Clause         | Java Type    | DB Column Suggestion     | Business Description                |
|---------------------------|--------------------|-------------|--------------------------|-------------------------------------|
| CARD-NUM                  | PIC X(16)          | String       | CARD_NUMBER (PK)         | 16-digit card number                |
| CARD-ACCT-ID              | PIC 9(11)          | long         | ACCOUNT_ID (FK)          | Parent account identifier           |
| CARD-CVV-CD               | PIC 9(03)          | int          | CVV_CODE                 | 3-digit card verification value     |
| CARD-EMBOSSED-NAME        | PIC X(50)          | String       | EMBOSSED_NAME            | Name printed on card                |
| CARD-EXPIRAION-DATE       | PIC X(10)          | LocalDate    | EXPIRATION_DATE          | Card expiration date                |
| CARD-ACTIVE-STATUS        | PIC X(01)          | String       | ACTIVE_STATUS            | Card status flag (Y/N)              |

**Key**: `CARD-NUM` (Primary Key) | **Foreign Key**: `CARD-ACCT-ID` → Account

**Security Note**: CVV code is stored in plain text. Migration should implement encryption/tokenization.

---

## Card Cross-Reference Entity (CVACT03Y)

**Copybook**: `app/cpy/CVACT03Y.cpy` | **Record Size**: ~36 bytes | **VSAM File**: `CARDXREF.VSAM.KSDS`

> Links cards to customers and accounts. Serves as the primary lookup bridge.

| COBOL Field Name           | PIC Clause         | Java Type    | DB Column Suggestion     | Business Description                |
|---------------------------|--------------------|-------------|--------------------------|-------------------------------------|
| XREF-CARD-NUM             | PIC X(16)          | String       | CARD_NUMBER (PK)         | Card number (lookup key)            |
| XREF-CUST-ID              | PIC 9(09)          | long         | CUSTOMER_ID (FK)         | Owning customer identifier          |
| XREF-ACCT-ID              | PIC 9(11)          | long         | ACCOUNT_ID (FK)          | Associated account identifier       |

**Key**: `XREF-CARD-NUM` (Primary Key) | **Foreign Keys**: `XREF-CUST-ID` → Customer, `XREF-ACCT-ID` → Account

**Migration Note**: In a relational model, this entity may be replaced by direct foreign keys on Card and Account tables, or kept as a junction table for the many-to-many relationship.

---

## Customer Entity (CVCUS01Y)

**Copybook**: `app/cpy/CVCUS01Y.cpy` | **Record Size**: ~500 bytes | **VSAM File**: `CUSTDATA.VSAM.KSDS`

> Represents a bank customer with personal details, contact information, and credit scoring.

| COBOL Field Name           | PIC Clause         | Java Type    | DB Column Suggestion     | Business Description                |
|---------------------------|--------------------|-------------|--------------------------|-------------------------------------|
| CUST-ID                   | PIC 9(09)          | long         | CUSTOMER_ID (PK)         | Unique customer identifier          |
| CUST-FIRST-NAME           | PIC X(25)          | String       | FIRST_NAME               | Customer first name                 |
| CUST-MIDDLE-NAME          | PIC X(25)          | String       | MIDDLE_NAME              | Customer middle name                |
| CUST-LAST-NAME            | PIC X(25)          | String       | LAST_NAME                | Customer last name                  |
| CUST-ADDR-LINE-1          | PIC X(50)          | String       | ADDRESS_LINE_1           | Street address line 1               |
| CUST-ADDR-LINE-2          | PIC X(50)          | String       | ADDRESS_LINE_2           | Street address line 2               |
| CUST-ADDR-LINE-3          | PIC X(50)          | String       | ADDRESS_LINE_3           | Street address line 3               |
| CUST-ADDR-STATE-CD        | PIC X(02)          | String       | STATE_CODE               | 2-letter state code                 |
| CUST-ADDR-COUNTRY-CD      | PIC X(03)          | String       | COUNTRY_CODE             | 3-letter country code               |
| CUST-ADDR-ZIP             | PIC X(10)          | String       | ZIP_CODE                 | Postal/ZIP code                     |
| CUST-PHONE-NUM-1          | PIC X(15)          | String       | PHONE_NUMBER_1           | Primary phone number                |
| CUST-PHONE-NUM-2          | PIC X(15)          | String       | PHONE_NUMBER_2           | Secondary phone number              |
| CUST-SSN                  | PIC 9(09)          | String       | SSN                      | Social Security Number              |
| CUST-GOVT-ISSUED-ID       | PIC X(20)          | String       | GOVT_ISSUED_ID           | Government-issued ID number         |
| CUST-DOB-YYYY-MM-DD       | PIC X(10)          | LocalDate    | DATE_OF_BIRTH            | Date of birth                       |
| CUST-EFT-ACCOUNT-ID       | PIC X(10)          | String       | EFT_ACCOUNT_ID           | Electronic funds transfer account   |
| CUST-PRI-CARD-HOLDER-IND  | PIC X(01)          | String       | PRIMARY_CARDHOLDER_FLAG  | Primary cardholder indicator (Y/N)  |
| CUST-FICO-CREDIT-SCORE    | PIC 9(03)          | int          | FICO_CREDIT_SCORE        | FICO credit score (300-850)         |

**Key**: `CUST-ID` (Primary Key)

**PII Fields**: SSN, DOB, phone numbers, address — require encryption at rest in target system.

**Array Note**: Address lines (OCCURS 3) and phone numbers (OCCURS 2) are flattened arrays. In Java, consider `List<String>` or separate columns.

---

## Transaction Entity (CVTRA05Y)

**Copybook**: `app/cpy/CVTRA05Y.cpy` | **Record Size**: ~350 bytes | **VSAM File**: `TRANSACT.VSAM.KSDS`

> Represents a posted credit card transaction (the master transaction record).

| COBOL Field Name           | PIC Clause         | Java Type    | DB Column Suggestion      | Business Description                |
|---------------------------|--------------------|-------------|---------------------------|-------------------------------------|
| TRAN-ID                   | PIC X(16)          | String       | TRANSACTION_ID (PK)       | Unique transaction identifier       |
| TRAN-TYPE-CD              | PIC X(02)          | String       | TRANSACTION_TYPE_CODE (FK)| Transaction type code               |
| TRAN-CAT-CD               | PIC 9(04)          | int          | CATEGORY_CODE (FK)        | Transaction category code           |
| TRAN-SOURCE               | PIC X(10)          | String       | TRANSACTION_SOURCE        | Source channel (online/POS/ATM)     |
| TRAN-DESC                 | PIC X(100)         | String       | DESCRIPTION               | Transaction description             |
| TRAN-AMT                  | PIC S9(09)V99      | BigDecimal   | AMOUNT                    | Transaction amount (signed)         |
| TRAN-MERCHANT-ID          | PIC 9(09)          | long         | MERCHANT_ID               | Merchant identifier                 |
| TRAN-MERCHANT-NAME        | PIC X(50)          | String       | MERCHANT_NAME             | Merchant business name              |
| TRAN-MERCHANT-CITY        | PIC X(50)          | String       | MERCHANT_CITY             | Merchant city                       |
| TRAN-MERCHANT-ZIP         | PIC X(10)          | String       | MERCHANT_ZIP              | Merchant ZIP code                   |
| TRAN-CARD-NUM             | PIC X(16)          | String       | CARD_NUMBER (FK)          | Card used for transaction           |
| TRAN-ORIG-TS              | PIC X(26)          | LocalDateTime| ORIGINATED_TIMESTAMP      | When transaction originated         |
| TRAN-PROC-TS              | PIC X(26)          | LocalDateTime| PROCESSED_TIMESTAMP       | When transaction was processed      |

**Key**: `TRAN-ID` (Primary Key) | **Foreign Keys**: `TRAN-CARD-NUM` → Card, `TRAN-TYPE-CD` → Transaction Type

**Business Rules**:
- Amount is signed: positive = debit/charge, negative = credit/refund
- Timestamps are 26-char strings (YYYY-MM-DD-HH.MM.SS.NNNNNN format)

---

## Daily Transaction Entity (CVTRA06Y)

**Copybook**: `app/cpy/CVTRA06Y.cpy` | **Record Size**: ~350 bytes | **VSAM File**: `DALYTRAN.VSAM.KSDS`

> Represents an unposted daily transaction awaiting batch posting to the master.

| COBOL Field Name             | PIC Clause        | Java Type    | DB Column Suggestion      | Business Description                  |
|-----------------------------|--------------------|-------------|---------------------------|---------------------------------------|
| DALYTRAN-ID                 | PIC X(16)          | String       | DAILY_TRAN_ID (PK)        | Daily transaction identifier          |
| DALYTRAN-TYPE-CD            | PIC X(02)          | String       | TRANSACTION_TYPE_CODE     | Transaction type code                 |
| DALYTRAN-CAT-CD             | PIC 9(04)          | int          | CATEGORY_CODE             | Transaction category                  |
| DALYTRAN-SOURCE             | PIC X(10)          | String       | TRANSACTION_SOURCE        | Source channel                        |
| DALYTRAN-DESC               | PIC X(100)         | String       | DESCRIPTION               | Transaction description               |
| DALYTRAN-AMT                | PIC S9(09)V99      | BigDecimal   | AMOUNT                    | Transaction amount                    |
| DALYTRAN-MERCHANT-ID        | PIC 9(09)          | long         | MERCHANT_ID               | Merchant identifier                   |
| DALYTRAN-MERCHANT-NAME      | PIC X(50)          | String       | MERCHANT_NAME             | Merchant name                         |
| DALYTRAN-MERCHANT-CITY      | PIC X(50)          | String       | MERCHANT_CITY             | Merchant city                         |
| DALYTRAN-MERCHANT-ZIP       | PIC X(10)          | String       | MERCHANT_ZIP              | Merchant ZIP                          |
| DALYTRAN-CARD-NUM           | PIC X(16)          | String       | CARD_NUMBER               | Card number                           |
| DALYTRAN-ORIG-TS            | PIC X(26)          | LocalDateTime| ORIGINATED_TIMESTAMP      | When originated                       |
| DALYTRAN-PROC-TS            | PIC X(26)          | LocalDateTime| PROCESSED_TIMESTAMP       | When processed                        |

**Lifecycle**: Daily transactions are created online (COTRN02C, COBIL00C) → posted nightly (CBTRN02C/POSTTRAN) → moved to master (TRANSACT).

---

## User Security Entity (CSUSR01Y)

**Copybook**: `app/cpy/CSUSR01Y.cpy` | **Record Size**: ~80 bytes | **VSAM File**: `USRSEC.VSAM.KSDS`

> Stores user credentials and role for the CardDemo application.

| COBOL Field Name           | PIC Clause         | Java Type    | DB Column Suggestion     | Business Description                |
|---------------------------|--------------------|-------------|--------------------------|-------------------------------------|
| SEC-USR-ID                | PIC X(08)          | String       | USER_ID (PK)             | Login user identifier               |
| SEC-USR-FNAME             | PIC X(20)          | String       | FIRST_NAME               | User first name                     |
| SEC-USR-LNAME             | PIC X(20)          | String       | LAST_NAME                | User last name                      |
| SEC-USR-PWD               | PIC X(08)          | String       | PASSWORD                 | User password (plaintext!)          |
| SEC-USR-TYPE              | PIC X(01)          | String       | USER_TYPE                | Role: 'A'=Admin, 'U'=Regular user  |
| SEC-USR-FILLER            | PIC X(23)          | -            | -                        | Reserved filler space               |

**Security Concern**: Passwords stored in plaintext. Migration MUST implement hashing (bcrypt/scrypt) and proper authentication.

**Business Rules**:
- Admin users (type 'A') can access COUSR00C-03C (user management) and admin menu options
- Regular users (type 'U') access the main menu functions only
- Default credentials: ADMIN001/PASSWORD (admin), USER0001/PASSWORD (regular)

---

## Transaction Category Balance Entity (CVTRA01Y)

**Copybook**: `app/cpy/CVTRA01Y.cpy` | **Record Size**: 50 bytes | **VSAM File**: `TCATBALF.VSAM.KSDS`

> Tracks running balance by account, transaction type, and category.

| COBOL Field Name           | PIC Clause         | Java Type    | DB Column Suggestion     | Business Description                |
|---------------------------|--------------------|-------------|--------------------------|-------------------------------------|
| TRANCAT-ACCT-ID           | PIC 9(11)          | long         | ACCOUNT_ID (PK, FK)      | Account identifier                  |
| TRANCAT-TYPE-CD           | PIC X(02)          | String       | TYPE_CODE (PK, FK)       | Transaction type code               |
| TRANCAT-CD                | PIC 9(04)          | int          | CATEGORY_CODE (PK)       | Transaction category code           |
| TRAN-CAT-BAL              | PIC S9(09)V99      | BigDecimal   | CATEGORY_BALANCE         | Running balance for this category   |

**Key**: Composite (`TRANCAT-ACCT-ID` + `TRANCAT-TYPE-CD` + `TRANCAT-CD`)

---

## Disclosure Group Entity (CVTRA02Y)

**Copybook**: `app/cpy/CVTRA02Y.cpy` | **Record Size**: 50 bytes | **VSAM File**: `DISCGRP.VSAM.KSDS`

> Defines interest rates by account group, transaction type, and category.

| COBOL Field Name           | PIC Clause         | Java Type    | DB Column Suggestion     | Business Description                |
|---------------------------|--------------------|-------------|--------------------------|-------------------------------------|
| DIS-ACCT-GROUP-ID         | PIC X(10)          | String       | GROUP_ID (PK)            | Account group identifier            |
| DIS-TRAN-TYPE-CD          | PIC X(02)          | String       | TYPE_CODE (PK)           | Transaction type code               |
| DIS-TRAN-CAT-CD           | PIC 9(04)          | int          | CATEGORY_CODE (PK)       | Transaction category code           |
| DIS-INT-RATE              | PIC S9(04)V99      | BigDecimal   | INTEREST_RATE            | Annual interest rate (%)            |

**Key**: Composite (`DIS-ACCT-GROUP-ID` + `DIS-TRAN-TYPE-CD` + `DIS-TRAN-CAT-CD`)

**Business Role**: Used by CBACT04C (interest calculation) to look up the appropriate rate for each transaction category on an account.

---

## Transaction Type Entity (CVTRA03Y)

**Copybook**: `app/cpy/CVTRA03Y.cpy` | **Record Size**: 60 bytes | **VSAM File**: `TRANTYPE.VSAM.KSDS`

> Reference table of transaction type codes and descriptions.

| COBOL Field Name           | PIC Clause         | Java Type    | DB Column Suggestion     | Business Description                |
|---------------------------|--------------------|-------------|--------------------------|-------------------------------------|
| TRAN-TYPE                 | PIC X(02)          | String       | TYPE_CODE (PK)           | 2-character type code               |
| TRAN-TYPE-DESC            | PIC X(50)          | String       | TYPE_DESCRIPTION         | Human-readable description          |

**Key**: `TRAN-TYPE` (Primary Key)

**Sample Values**: Purchase, Cash Advance, Balance Transfer, Payment, Fee, Interest, etc.

---

## Transaction Category Entity (CVTRA04Y)

**Copybook**: `app/cpy/CVTRA04Y.cpy` | **Record Size**: 60 bytes | **VSAM File**: `TRANCATG.VSAM.KSDS`

> Reference table of transaction category codes within each type.

| COBOL Field Name           | PIC Clause         | Java Type    | DB Column Suggestion     | Business Description                |
|---------------------------|--------------------|-------------|--------------------------|-------------------------------------|
| TRAN-TYPE-CD              | PIC X(02)          | String       | TYPE_CODE (PK, FK)       | Parent transaction type code        |
| TRAN-CAT-CD               | PIC 9(04)          | int          | CATEGORY_CODE (PK)       | Category code within type           |
| TRAN-CAT-TYPE-DESC        | PIC X(50)          | String       | CATEGORY_DESCRIPTION     | Human-readable description          |

**Key**: Composite (`TRAN-TYPE-CD` + `TRAN-CAT-CD`)

---

## Report Structures (CVTRA07Y)

**Copybook**: `app/cpy/CVTRA07Y.cpy` | **Record Size**: Variable | **Used By**: CBTRN03C

> Defines print report layouts for the daily transaction report.

| Structure Name              | Description                              | Key Fields                     |
|----------------------------|------------------------------------------|--------------------------------|
| REPORT-NAME-HEADER         | Report header with name and date range   | REPT-SHORT-NAME, REPT-START-DATE, REPT-END-DATE |
| TRANSACTION-DETAIL-REPORT  | Detail line for each transaction         | TRAN-REPORT-TRANS-ID, TRAN-REPORT-AMT |
| TRANSACTION-HEADER-1       | Column header line                       | Static labels                  |
| TRANSACTION-HEADER-2       | Separator line (all dashes)             | Static                         |
| REPORT-PAGE-TOTALS         | Page subtotal line                       | REPT-PAGE-TOTAL                |
| REPORT-ACCOUNT-TOTALS      | Account subtotal line                    | REPT-ACCOUNT-TOTAL             |
| REPORT-GRAND-TOTALS        | Grand total line                         | REPT-GRAND-TOTAL               |

---

## Export Record Entity (CVEXPORT)

**Copybook**: `app/cpy/CVEXPORT.cpy` | **Record Size**: ~506 bytes | **Used By**: CBEXPORT, CBIMPORT

> Multi-record export format using REDEFINES to carry different entity types in a single file.

| COBOL Field Name              | PIC Clause        | Description                                |
|------------------------------|--------------------|--------------------------------------------|
| EXPORT-REC-TYPE              | PIC X(1)           | Record type: C=Customer, A=Account, T=Transaction, X=Xref, R=Card |
| EXPORT-TIMESTAMP             | PIC X(26)          | Export timestamp                           |
| EXPORT-SEQUENCE-NUM          | PIC 9(9) COMP      | Sequential record number                   |
| EXPORT-BRANCH-ID            | PIC X(4)           | Branch identifier                          |
| EXPORT-REGION-CODE          | PIC X(5)           | Region code                                |
| EXPORT-RECORD-DATA          | PIC X(460)         | Payload — REDEFINES per record type:       |
| → EXPORT-CUSTOMER-DATA      | (REDEFINES)        | Customer fields (mirrors CVCUS01Y)         |
| → EXPORT-ACCOUNT-DATA       | (REDEFINES)        | Account fields (mirrors CVACT01Y)          |
| → EXPORT-TRANSACTION-DATA   | (REDEFINES)        | Transaction fields (mirrors CVTRA05Y)      |
| → EXPORT-CARD-XREF-DATA     | (REDEFINES)        | Card cross-ref fields (mirrors CVACT03Y)   |
| → EXPORT-CARD-DATA          | (REDEFINES)        | Card fields (mirrors CVACT02Y)             |

**Migration Pattern**: REDEFINES → Java polymorphism (abstract `ExportRecord` base class with subclasses per type), or a discriminated union / sealed class.

---

## Communication Area (COCOM01Y)

**Copybook**: `app/cpy/COCOM01Y.cpy` | **Used By**: All online CICS programs

> The COMMAREA passed between CICS programs for inter-program communication.

| COBOL Field Name           | PIC Clause         | Java Type    | Description                          |
|---------------------------|--------------------|-------------|--------------------------------------|
| CDEMO-FROM-TRANID         | PIC X(04)          | String       | Source transaction ID                |
| CDEMO-FROM-PROGRAM        | PIC X(08)          | String       | Source program name                  |
| CDEMO-TO-TRANID           | PIC X(04)          | String       | Target transaction ID                |
| CDEMO-TO-PROGRAM          | PIC X(08)          | String       | Target program name                  |
| CDEMO-USER-ID             | PIC X(08)          | String       | Current logged-in user               |
| CDEMO-USER-TYPE           | PIC X(01)          | String       | User role (A/U)                      |
| CDEMO-PGM-CONTEXT         | PIC 9(01)          | int          | Program context flag                 |
| (+ additional fields)     |                     |              | Account ID, card number, etc.        |

**Migration Pattern**: COMMAREA → Java session state / Spring `@SessionScope` bean, or request-scoped DTO passed through service layers.

---

## PIC Clause Reference

For teams unfamiliar with COBOL data types, here is a quick reference:

| COBOL PIC Clause       | Meaning                              | Java Equivalent     | SQL Type         |
|-----------------------|--------------------------------------|--------------------|-----------------  |
| PIC X(n)              | Alphanumeric string, n chars         | String             | VARCHAR(n)        |
| PIC 9(n)              | Unsigned integer, n digits           | int / long         | NUMERIC(n)        |
| PIC S9(n)             | Signed integer, n digits             | int / long         | NUMERIC(n)        |
| PIC S9(n)V99          | Signed decimal, n+2 digits           | BigDecimal         | NUMERIC(n+2, 2)   |
| PIC S9(n)V99 COMP-3   | Packed decimal (BCD encoding)       | BigDecimal         | NUMERIC(n+2, 2)   |
| PIC 9(n) COMP         | Binary integer                      | int / long         | INTEGER / BIGINT  |
| PIC S9(n) BINARY      | Signed binary integer               | int / long         | INTEGER / BIGINT  |
| FILLER                | Unused padding bytes                 | (omit)             | (omit)            |
| REDEFINES             | Union type / overlay                 | Inheritance/sealed | (separate tables) |
| OCCURS n TIMES        | Fixed-size array                     | List<T> / T[]      | Normalized table  |

### Data Size Considerations

| Entity          | COBOL Record | Est. Row Size (DB) | Notes                          |
|----------------|-------------|--------------------|---------------------------------|
| Account        | ~300 bytes  | ~200 bytes         | Drop FILLERs, normalize dates   |
| Customer       | ~500 bytes  | ~350 bytes         | Normalize addresses, phones     |
| Credit Card    | ~150 bytes  | ~100 bytes         | Encrypt CVV                     |
| Transaction    | ~350 bytes  | ~250 bytes         | Index on card, date, type       |
| User Security  | ~80 bytes   | ~70 bytes          | Hash passwords                  |
