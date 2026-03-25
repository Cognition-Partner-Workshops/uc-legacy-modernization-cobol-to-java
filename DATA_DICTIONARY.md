# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Source:** COBOL Copybooks in `app/cpy/` | **Storage:** VSAM KSDS files

---

## Table of Contents

1. [VSAM File Summary](#1-vsam-file-summary)
2. [Account Entity (CVACT01Y)](#2-account-entity)
3. [Card Entity (CVACT02Y)](#3-card-entity)
4. [Card Cross-Reference (CVACT03Y)](#4-card-cross-reference)
5. [Customer Entity (CVCUS01Y / CUSTREC)](#5-customer-entity)
6. [Transaction Entity (CVTRA05Y)](#6-transaction-entity)
7. [Daily Transaction (CVTRA06Y)](#7-daily-transaction)
8. [User Security (CSUSR01Y)](#8-user-security)
9. [Transaction Category Balance (CVTRA01Y)](#9-transaction-category-balance)
10. [Discount / Disclosure Group (CVTRA02Y)](#10-discount-group)
11. [Transaction Type (CVTRA03Y)](#11-transaction-type)
12. [Transaction Category (CVTRA04Y)](#12-transaction-category)
13. [Statement Transaction (COSTM01)](#13-statement-transaction)
14. [Export Record (CVEXPORT)](#14-export-record)
15. [Communication Area (COCOM01Y)](#15-communication-area)
16. [Entity Relationship Summary](#16-entity-relationship-summary)
17. [Modernization Mapping](#17-modernization-mapping)

---

## 1. VSAM File Summary

| VSAM File  | CICS DD Name | Record Layout | Key Field(s)              | Record Length | Access  |
|------------|--------------|---------------|---------------------------|---------------|---------|
| ACCTDAT    | ACCTDAT      | CVACT01Y      | ACCT-ID (11 digits)       | 300 bytes     | KSDS    |
| CARDDAT    | CARDDAT      | CVACT02Y      | CARD-NUM (16 chars)       | 150 bytes     | KSDS    |
| CARDAIX    | CARDAIX      | CVACT02Y      | Alternate index on ACCT-ID| 150 bytes     | AIX     |
| CARDXREF   | CXACAIX      | CVACT03Y      | XREF-CARD-NUM (16 chars)  | 50 bytes      | KSDS    |
| CUSTDAT    | CUSTDAT      | CVCUS01Y      | CUST-ID (9 digits)        | 500 bytes     | KSDS    |
| TRANSACT   | TRANSACT     | CVTRA05Y      | TRAN-ID (16 chars)        | 350 bytes     | KSDS    |
| DALYTRAN   | DALYTRAN     | CVTRA06Y      | DALYTRAN-ID (16 chars)    | 350 bytes     | KSDS    |
| USRSEC     | USRSEC       | CSUSR01Y      | SEC-USR-ID (8 chars)      | 80 bytes      | KSDS    |
| TCATBAL    | TCATBALF     | CVTRA01Y      | ACCT-ID + TYPE-CD + CAT-CD| 50 bytes      | KSDS    |
| DISCGRP    | DISCGRP      | CVTRA02Y      | GROUP-ID + TYPE + CAT     | 50 bytes      | KSDS    |
| TRANTYPE   | TRANTYPE     | CVTRA03Y      | TYPE-CD (2 chars)         | ~20 bytes     | KSDS    |
| TRANCATG   | TRANCATG     | CVTRA04Y      | CAT-CD (4 digits)         | ~40 bytes     | KSDS    |

---

## 2. Account Entity

**Copybook:** `CVACT01Y` | **VSAM File:** ACCTDAT | **Record Length:** 300 bytes

| COBOL Field Name         | PIC Clause       | Type     | Size  | Business Name              | Business Description                           | Nullable | Example Value      |
|--------------------------|------------------|----------|-------|----------------------------|------------------------------------------------|----------|--------------------|
| ACCT-ID                  | 9(11)            | Numeric  | 11    | Account Number             | Unique account identifier (primary key)        | No       | 00000000012        |
| ACCT-ACTIVE-STATUS       | X(01)            | Alpha    | 1     | Account Status             | Active/Inactive flag (Y/N)                     | No       | Y                  |
| ACCT-CURR-BAL            | S9(10)V99        | Decimal  | 12.2  | Current Balance            | Current outstanding balance (signed)           | No       | +0000050000.00     |
| ACCT-CREDIT-LIMIT        | S9(10)V99        | Decimal  | 12.2  | Credit Limit               | Maximum credit limit for the account           | No       | +0000100000.00     |
| ACCT-CASH-CREDIT-LIMIT   | S9(10)V99        | Decimal  | 12.2  | Cash Advance Limit         | Maximum cash advance credit limit              | No       | +0000020000.00     |
| ACCT-OPEN-DATE           | X(10)            | Date     | 10    | Account Open Date          | Date the account was opened (YYYY-MM-DD)       | No       | 2020-01-15         |
| ACCT-EXPIRAION-DATE      | X(10)            | Date     | 10    | Account Expiration Date    | Date the account expires                       | No       | 2025-01-15         |
| ACCT-REISSUE-DATE        | X(10)            | Date     | 10    | Reissue Date               | Date of last card reissue                      | Yes      | 2023-01-15         |
| ACCT-CURR-CYC-CREDIT     | S9(10)V99        | Decimal  | 12.2  | Current Cycle Credits      | Total credits in current billing cycle         | No       | +0000005000.00     |
| ACCT-CURR-CYC-DEBIT      | S9(10)V99        | Decimal  | 12.2  | Current Cycle Debits       | Total debits in current billing cycle          | No       | +0000003000.00     |
| ACCT-ADDR-ZIP            | X(10)            | Alpha    | 10    | Account ZIP Code           | ZIP/postal code associated with account        | Yes      | 10001              |
| ACCT-GROUP-ID            | X(10)            | Alpha    | 10    | Account Group ID           | Discount/disclosure group assignment           | Yes      | GRP001             |
| FILLER                   | X(178)           | Filler   | 178   | Reserved                   | Reserved space for future expansion            | —        | SPACES             |

**Business Rules:**
- Primary key: `ACCT-ID` (11-digit numeric)
- Linked to Customer via `CVACT03Y` (Card Cross-Reference)
- Balance fields are signed to allow negative values (overpayments)
- Account Group ID links to `CVTRA02Y` (Discount Group) for interest rate determination

---

## 3. Card Entity

**Copybook:** `CVACT02Y` | **VSAM File:** CARDDAT | **Record Length:** 150 bytes

| COBOL Field Name         | PIC Clause       | Type     | Size  | Business Name              | Business Description                           | Nullable | Example Value      |
|--------------------------|------------------|----------|-------|----------------------------|------------------------------------------------|----------|--------------------|
| CARD-NUM                 | X(16)            | Alpha    | 16    | Card Number                | Credit card number (primary key)               | No       | 4111111111111111   |
| CARD-ACCT-ID             | 9(11)            | Numeric  | 11    | Account Number             | Owning account ID (FK to ACCTDAT)              | No       | 00000000012        |
| CARD-CVV-CD              | 9(03)            | Numeric  | 3     | CVV Code                   | Card verification value code                   | No       | 123                |
| CARD-EMBOSSED-NAME       | X(50)            | Alpha    | 50    | Embossed Name              | Name printed on the physical card              | No       | JOHN DOE           |
| CARD-EXPIRAION-DATE      | X(10)            | Date     | 10    | Card Expiration Date       | Card expiry date (YYYY-MM-DD)                  | No       | 2025-12-31         |
| CARD-ACTIVE-STATUS       | X(01)            | Alpha    | 1     | Card Status                | Active/Inactive flag (Y/N)                     | No       | Y                  |
| FILLER                   | X(59)            | Filler   | 59    | Reserved                   | Reserved space for future expansion            | —        | SPACES             |

**Business Rules:**
- Primary key: `CARD-NUM` (16-character string)
- Foreign key: `CARD-ACCT-ID` → `ACCT-ID` in Account entity
- Alternate index (CARDAIX) on `CARD-ACCT-ID` for account-based lookups
- One account can have multiple cards

---

## 4. Card Cross-Reference

**Copybook:** `CVACT03Y` | **VSAM File:** CARDXREF | **Record Length:** 50 bytes

| COBOL Field Name         | PIC Clause       | Type     | Size  | Business Name              | Business Description                           | Nullable | Example Value      |
|--------------------------|------------------|----------|-------|----------------------------|------------------------------------------------|----------|--------------------|
| XREF-CARD-NUM            | X(16)            | Alpha    | 16    | Card Number                | Card number (primary key)                      | No       | 4111111111111111   |
| XREF-CUST-ID             | 9(09)            | Numeric  | 9     | Customer ID                | Customer who owns this card (FK to CUSTDAT)    | No       | 000000001          |
| XREF-ACCT-ID             | 9(11)            | Numeric  | 11    | Account Number             | Account linked to this card (FK to ACCTDAT)    | No       | 00000000012        |
| FILLER                   | X(14)            | Filler   | 14    | Reserved                   | Reserved space for future expansion            | —        | SPACES             |

**Business Rules:**
- This is the **central junction table** linking Card ↔ Customer ↔ Account
- Primary key: `XREF-CARD-NUM`
- Used by most online programs to navigate between entities
- Alternate index (CXACAIX) on `XREF-ACCT-ID` for account-based lookups

---

## 5. Customer Entity

**Copybook:** `CVCUS01Y` (online) / `CUSTREC` (batch) | **VSAM File:** CUSTDAT | **Record Length:** 500 bytes

| COBOL Field Name                | PIC Clause       | Type     | Size  | Business Name              | Business Description                           | Nullable | Example Value      |
|---------------------------------|------------------|----------|-------|----------------------------|------------------------------------------------|----------|--------------------|
| CUST-ID                         | 9(09)            | Numeric  | 9     | Customer ID                | Unique customer identifier (primary key)       | No       | 000000001          |
| CUST-FIRST-NAME                 | X(25)            | Alpha    | 25    | First Name                 | Customer's first name                          | No       | JOHN               |
| CUST-MIDDLE-NAME                | X(25)            | Alpha    | 25    | Middle Name                | Customer's middle name                         | Yes      | M                  |
| CUST-LAST-NAME                  | X(25)            | Alpha    | 25    | Last Name                  | Customer's last name                           | No       | DOE                |
| CUST-ADDR-LINE-1                | X(50)            | Alpha    | 50    | Address Line 1             | Primary street address                         | No       | 123 MAIN ST        |
| CUST-ADDR-LINE-2                | X(50)            | Alpha    | 50    | Address Line 2             | Secondary address (apt, suite, etc.)           | Yes      | APT 4B             |
| CUST-ADDR-LINE-3                | X(50)            | Alpha    | 50    | Address Line 3             | Additional address line                        | Yes      | SPACES             |
| CUST-ADDR-STATE-CD              | X(02)            | Code     | 2     | State Code                 | US state code (validated against CSLKPCDY)     | No       | NY                 |
| CUST-ADDR-COUNTRY-CD            | X(03)            | Code     | 3     | Country Code               | Country code (validated against CSLKPCDY)      | No       | US                 |
| CUST-ADDR-ZIP                   | X(10)            | Alpha    | 10    | ZIP Code                   | ZIP/postal code                                | No       | 10001              |
| CUST-PHONE-NUM-1                | X(15)            | Alpha    | 15    | Primary Phone              | Primary phone number                           | No       | (212)555-0100      |
| CUST-PHONE-NUM-2                | X(15)            | Alpha    | 15    | Secondary Phone            | Secondary/alternate phone number               | Yes      | (212)555-0101      |
| CUST-SSN                        | 9(09)            | Numeric  | 9     | Social Security Number     | Customer SSN (PII - sensitive)                 | No       | 123456789          |
| CUST-GOVT-ISSUED-ID             | X(20)            | Alpha    | 20    | Government ID              | Government-issued identification number        | Yes      | DL12345678         |
| CUST-DOB-YYYY-MM-DD             | X(10)            | Date     | 10    | Date of Birth              | Customer date of birth                         | No       | 1985-06-15         |
| CUST-EFT-ACCOUNT-ID             | X(10)            | Alpha    | 10    | EFT Account ID             | Electronic funds transfer account for payments | Yes      | EFT0001234         |
| CUST-PRI-CARD-HOLDER-IND        | X(01)            | Alpha    | 1     | Primary Cardholder         | Primary cardholder indicator (Y/N)             | No       | Y                  |
| CUST-FICO-CREDIT-SCORE          | 9(03)            | Numeric  | 3     | FICO Score                 | Customer credit score (300-850)                | No       | 750                |
| FILLER                          | X(168)           | Filler   | 168   | Reserved                   | Reserved space for future expansion            | —        | SPACES             |

**Business Rules:**
- Primary key: `CUST-ID` (9-digit numeric)
- Contains **PII data** (SSN, DOB, address) — requires encryption/masking in modernized system
- Linked to accounts via Card Cross-Reference (`CVACT03Y`)
- State/Country codes validated against lookup tables in `CSLKPCDY`
- FICO score range: 300–850

---

## 6. Transaction Entity

**Copybook:** `CVTRA05Y` | **VSAM File:** TRANSACT | **Record Length:** 350 bytes

| COBOL Field Name         | PIC Clause       | Type     | Size  | Business Name              | Business Description                           | Nullable | Example Value      |
|--------------------------|------------------|----------|-------|----------------------------|------------------------------------------------|----------|--------------------|
| TRAN-ID                  | X(16)            | Alpha    | 16    | Transaction ID             | Unique transaction identifier (primary key)    | No       | TRN0000000000001   |
| TRAN-TYPE-CD             | X(02)            | Code     | 2     | Transaction Type Code      | Type of transaction (FK to TRANTYPE)           | No       | SA                 |
| TRAN-CAT-CD              | 9(04)            | Numeric  | 4     | Transaction Category Code  | Category of transaction (FK to TRANCATG)       | No       | 5001               |
| TRAN-SOURCE              | X(10)            | Alpha    | 10    | Transaction Source         | Source/channel of transaction                  | No       | ONLINE             |
| TRAN-DESC                | X(100)           | Alpha    | 100   | Description                | Free-text transaction description              | Yes      | PURCHASE AT STORE  |
| TRAN-AMT                 | S9(09)V99        | Decimal  | 11.2  | Transaction Amount         | Dollar amount (signed: +debit, -credit)        | No       | +000050000.00      |
| TRAN-MERCHANT-ID         | 9(09)            | Numeric  | 9     | Merchant ID                | Merchant identifier                            | Yes      | 000123456          |
| TRAN-MERCHANT-NAME       | X(50)            | Alpha    | 50    | Merchant Name              | Name of the merchant                           | Yes      | AMAZON.COM         |
| TRAN-MERCHANT-CITY       | X(50)            | Alpha    | 50    | Merchant City              | City of the merchant                           | Yes      | SEATTLE            |
| TRAN-MERCHANT-ZIP        | X(10)            | Alpha    | 10    | Merchant ZIP               | ZIP code of the merchant                       | Yes      | 98101              |
| TRAN-CARD-NUM            | X(16)            | Alpha    | 16    | Card Number                | Card used for this transaction                 | No       | 4111111111111111   |
| TRAN-ORIG-TS             | X(26)            | Timestamp| 26    | Origination Timestamp      | When transaction was originated                | No       | 2022-07-18-12.30.00|
| TRAN-PROC-TS             | X(26)            | Timestamp| 26    | Processing Timestamp       | When transaction was processed/posted          | Yes      | 2022-07-18-14.00.00|
| FILLER                   | X(20)            | Filler   | 20    | Reserved                   | Reserved space for future expansion            | —        | SPACES             |

**Business Rules:**
- Primary key: `TRAN-ID` (16-character string)
- Amount is signed: positive = debit/purchase, negative = credit/refund
- `TRAN-TYPE-CD` links to Transaction Type reference file
- `TRAN-CAT-CD` links to Transaction Category reference file
- `TRAN-CARD-NUM` links back to Card entity

---

## 7. Daily Transaction

**Copybook:** `CVTRA06Y` | **VSAM File:** DALYTRAN | **Record Length:** 350 bytes

Identical field layout to Transaction Entity (CVTRA05Y) but with `DALYTRAN-` prefix. This is the **staging file** for unposted daily transactions. After batch posting (CBTRN02C), records move from DALYTRAN → TRANSACT.

| COBOL Field Name         | PIC Clause       | Maps To (CVTRA05Y)         |
|--------------------------|------------------|----------------------------|
| DALYTRAN-ID              | X(16)            | TRAN-ID                    |
| DALYTRAN-TYPE-CD         | X(02)            | TRAN-TYPE-CD               |
| DALYTRAN-CAT-CD          | 9(04)            | TRAN-CAT-CD                |
| DALYTRAN-SOURCE          | X(10)            | TRAN-SOURCE                |
| DALYTRAN-DESC            | X(100)           | TRAN-DESC                  |
| DALYTRAN-AMT             | S9(09)V99        | TRAN-AMT                   |
| DALYTRAN-MERCHANT-ID     | 9(09)            | TRAN-MERCHANT-ID           |
| DALYTRAN-MERCHANT-NAME   | X(50)            | TRAN-MERCHANT-NAME         |
| DALYTRAN-MERCHANT-CITY   | X(50)            | TRAN-MERCHANT-CITY         |
| DALYTRAN-MERCHANT-ZIP    | X(10)            | TRAN-MERCHANT-ZIP          |
| DALYTRAN-CARD-NUM        | X(16)            | TRAN-CARD-NUM              |
| DALYTRAN-ORIG-TS         | X(26)            | TRAN-ORIG-TS               |
| DALYTRAN-PROC-TS         | X(26)            | TRAN-PROC-TS               |

---

## 8. User Security

**Copybook:** `CSUSR01Y` | **VSAM File:** USRSEC | **Record Length:** 80 bytes

| COBOL Field Name         | PIC Clause       | Type     | Size  | Business Name              | Business Description                           | Nullable | Example Value      |
|--------------------------|------------------|----------|-------|----------------------------|------------------------------------------------|----------|--------------------|
| SEC-USR-ID               | X(08)            | Alpha    | 8     | User ID                    | Login user ID (primary key)                    | No       | USER0001           |
| SEC-USR-FNAME            | X(20)            | Alpha    | 20    | First Name                 | User's first name                              | No       | JOHN               |
| SEC-USR-LNAME            | X(20)            | Alpha    | 20    | Last Name                  | User's last name                               | No       | DOE                |
| SEC-USR-PWD              | X(08)            | Alpha    | 8     | Password                   | User's password (plaintext — security risk)    | No       | PASSWORD           |
| SEC-USR-TYPE             | X(01)            | Code     | 1     | User Type                  | 'A' = Admin, 'U' = Regular User                | No       | U                  |
| SEC-USR-FILLER           | X(23)            | Filler   | 23    | Reserved                   | Reserved space                                 | —        | SPACES             |

**Business Rules:**
- Primary key: `SEC-USR-ID` (8-character, uppercased)
- **Security concern:** Password stored in plaintext — must be hashed in modernized system
- User type determines menu access: Admin sees COADM01C, Regular sees COMEN01C
- Default users: `ADMIN001` (Admin), `USER0001` (Regular)

---

## 9. Transaction Category Balance

**Copybook:** `CVTRA01Y` | **VSAM File:** TCATBAL | **Record Length:** 50 bytes

| COBOL Field Name         | PIC Clause       | Type     | Size  | Business Name              | Business Description                           |
|--------------------------|------------------|----------|-------|----------------------------|------------------------------------------------|
| TRANCAT-ACCT-ID          | 9(11)            | Numeric  | 11    | Account Number             | Account ID (part of composite key)             |
| TRANCAT-TYPE-CD          | X(02)            | Code     | 2     | Transaction Type Code      | Transaction type (part of composite key)       |
| TRANCAT-CD               | 9(04)            | Numeric  | 4     | Category Code              | Transaction category (part of composite key)   |
| TRAN-CAT-BAL             | S9(09)V99        | Decimal  | 11.2  | Category Balance           | Running balance for this account/type/category |
| FILLER                   | X(22)            | Filler   | 22    | Reserved                   | Reserved space                                 |

**Business Rules:**
- Composite key: `ACCT-ID` + `TYPE-CD` + `CAT-CD`
- Maintains running totals per account per transaction type per category
- Updated during batch posting (CBTRN02C)

---

## 10. Discount Group

**Copybook:** `CVTRA02Y` | **VSAM File:** DISCGRP | **Record Length:** 50 bytes

| COBOL Field Name         | PIC Clause       | Type     | Size  | Business Name              | Business Description                           |
|--------------------------|------------------|----------|-------|----------------------------|------------------------------------------------|
| DIS-ACCT-GROUP-ID        | X(10)            | Alpha    | 10    | Account Group ID           | Disclosure/discount group identifier           |
| DIS-TRAN-TYPE-CD         | X(02)            | Code     | 2     | Transaction Type Code      | Transaction type for this rate                 |
| DIS-TRAN-CAT-CD          | 9(04)            | Numeric  | 4     | Category Code              | Transaction category for this rate             |
| DIS-INT-RATE             | S9(04)V99        | Decimal  | 6.2   | Interest Rate              | Interest rate for this group/type/category     |
| FILLER                   | X(28)            | Filler   | 28    | Reserved                   | Reserved space                                 |

**Business Rules:**
- Composite key: `GROUP-ID` + `TYPE-CD` + `CAT-CD`
- Links to Account via `ACCT-GROUP-ID` field in Account entity
- Used by interest calculation batch (CBACT04C) to determine applicable rates

---

## 11. Transaction Type

**Copybook:** `CVTRA03Y` | **VSAM File:** TRANTYPE | **Record Length:** ~20 bytes

| COBOL Field Name         | PIC Clause       | Type     | Size  | Business Name              | Business Description                           |
|--------------------------|------------------|----------|-------|----------------------------|------------------------------------------------|
| TRAN-TYPE                | X(02)            | Code     | 2     | Transaction Type Code      | 2-letter code (primary key)                    |
| TRAN-TYPE-DESC           | X(15)            | Alpha    | 15    | Transaction Type Description | Human-readable description                   |
| FILLER                   | X(03)            | Filler   | 3     | Reserved                   | Reserved space                                 |

**Business Rules:**
- Reference/lookup table for transaction types
- Example codes: SA (Sale), CR (Credit), RE (Return), CA (Cash Advance)

---

## 12. Transaction Category

**Copybook:** `CVTRA04Y` | **VSAM File:** TRANCATG | **Record Length:** ~40 bytes

| COBOL Field Name         | PIC Clause       | Type     | Size  | Business Name              | Business Description                           |
|--------------------------|------------------|----------|-------|----------------------------|------------------------------------------------|
| TRAN-CAT-CD              | 9(04)            | Numeric  | 4     | Category Code              | 4-digit category code (primary key)            |
| TRAN-CAT-DESC            | X(29)            | Alpha    | 29    | Category Description       | Human-readable category description            |
| FILLER                   | X(07)            | Filler   | 7     | Reserved                   | Reserved space                                 |

**Business Rules:**
- Reference/lookup table for transaction categories
- Example: 5001 = Retail Purchase, 5411 = Grocery Store

---

## 13. Statement Transaction

**Copybook:** `COSTM01` | **Used By:** CBSTM03A (Statement Generation) | **Record Length:** ~355 bytes

This is a **re-keyed transaction layout** used for statement reporting. Records are sorted by card number + transaction ID for statement generation.

| COBOL Field Name         | PIC Clause       | Type     | Size  | Business Name              |
|--------------------------|------------------|----------|-------|----------------------------|
| TRNX-CARD-NUM            | X(16)            | Alpha    | 16    | Card Number (sort key 1)   |
| TRNX-ID                  | X(16)            | Alpha    | 16    | Transaction ID (sort key 2)|
| TRNX-TYPE-CD             | X(02)            | Code     | 2     | Transaction Type Code      |
| TRNX-CAT-CD              | 9(04)            | Numeric  | 4     | Transaction Category Code  |
| TRNX-SOURCE              | X(10)            | Alpha    | 10    | Transaction Source         |
| TRNX-DESC                | X(100)           | Alpha    | 100   | Transaction Description    |
| TRNX-AMT                 | S9(09)V99        | Decimal  | 11.2  | Transaction Amount         |
| TRNX-MERCHANT-ID         | 9(09)            | Numeric  | 9     | Merchant ID                |
| TRNX-MERCHANT-NAME       | X(50)            | Alpha    | 50    | Merchant Name              |
| TRNX-MERCHANT-CITY       | X(50)            | Alpha    | 50    | Merchant City              |
| TRNX-MERCHANT-ZIP        | X(10)            | Alpha    | 10    | Merchant ZIP Code          |
| TRNX-ORIG-TS             | X(26)            | Timestamp| 26    | Origination Timestamp      |
| TRNX-PROC-TS             | X(26)            | Timestamp| 26    | Processing Timestamp       |

---

## 14. Export Record

**Copybook:** `CVEXPORT` | **Used By:** CBEXPORT / CBIMPORT | **Record Length:** 500 bytes

A multi-record-type layout using REDEFINES for data migration scenarios.

| Field               | PIC          | Description                                      |
|---------------------|--------------|--------------------------------------------------|
| EXPORT-REC-TYPE     | X(1)         | Record type discriminator (C=Customer, A=Account, T=Transaction, X=Xref, D=Card) |
| EXPORT-TIMESTAMP    | X(26)        | Export timestamp (YYYY-MM-DD + time)             |
| EXPORT-SEQUENCE-NUM | 9(9) COMP    | Sequential record number                         |
| EXPORT-BRANCH-ID    | X(4)         | Branch identifier for migration                  |
| EXPORT-REGION-CODE  | X(5)         | Region code for migration                        |
| EXPORT-RECORD-DATA  | X(460)       | Record payload — REDEFINES per type:             |

**REDEFINES variants:**
- `EXPORT-CUSTOMER-DATA` — mirrors CVCUS01Y fields with `EXP-CUST-` prefix; uses COMP for CUST-ID, COMP-3 for FICO
- `EXPORT-ACCOUNT-DATA` — mirrors CVACT01Y fields with `EXP-ACCT-` prefix; uses COMP-3 for balances
- `EXPORT-TRANSACTION-DATA` — mirrors CVTRA05Y fields with `EXP-TRAN-` prefix; uses COMP-3 for amount
- `EXPORT-CARD-XREF-DATA` — mirrors CVACT03Y fields with `EXP-XREF-` prefix; uses COMP for ACCT-ID
- `EXPORT-CARD-DATA` — mirrors CVACT02Y fields with `EXP-CARD-` prefix; uses COMP for IDs

---

## 15. Communication Area

**Copybook:** `COCOM01Y` | **Used By:** All online CICS programs

The COMMAREA is the shared communication area passed between CICS programs via XCTL.

| COBOL Field Name         | PIC Clause       | Type     | Business Name              | Business Description                           |
|--------------------------|------------------|----------|----------------------------|------------------------------------------------|
| CDEMO-FROM-TRANID        | X(04)            | Code     | Source Transaction ID      | Transaction that called this program           |
| CDEMO-FROM-PROGRAM       | X(08)            | Alpha    | Source Program             | Program that transferred control               |
| CDEMO-TO-TRANID          | X(04)            | Code     | Target Transaction ID      | Transaction to return to                       |
| CDEMO-TO-PROGRAM         | X(08)            | Alpha    | Target Program             | Program to transfer control to                 |
| CDEMO-USER-ID            | X(08)            | Alpha    | Current User ID            | Authenticated user ID                          |
| CDEMO-USER-TYPE          | X(01)            | Code     | User Type                  | 'A' = Admin, 'U' = Regular                    |
| CDEMO-PGM-CONTEXT        | 9(01)            | Numeric  | Program Context            | 0 = First entry, 1 = Re-entry (conversational)|
| CDEMO-CUST-ID            | 9(09)            | Numeric  | Customer ID                | Current customer being operated on             |
| CDEMO-CUST-FNAME         | X(25)            | Alpha    | Customer First Name        | Cached customer first name                     |
| CDEMO-CUST-MNAME         | X(25)            | Alpha    | Customer Middle Name       | Cached customer middle name                    |
| CDEMO-CUST-LNAME         | X(25)            | Alpha    | Customer Last Name         | Cached customer last name                      |
| CDEMO-ACCT-ID            | 9(11)            | Numeric  | Account ID                 | Current account being operated on              |
| CDEMO-ACCT-STATUS        | X(01)            | Code     | Account Status             | Cached account active status                   |
| CDEMO-CARD-NUM           | 9(16)            | Numeric  | Card Number                | Current card being operated on                 |
| CDEMO-LAST-MAP           | X(7)             | Alpha    | Last BMS Map               | Most recently displayed BMS map name           |
| CDEMO-LAST-MAPSET        | X(7)             | Alpha    | Last BMS Mapset            | Most recently used BMS mapset name             |

---

## 16. Entity Relationship Summary

```
┌─────────────┐       ┌──────────────────┐       ┌─────────────┐
│  CUSTOMER   │←──────│  CARD CROSS-REF  │──────→│   ACCOUNT   │
│  (CUSTDAT)  │  1:N  │   (CARDXREF)     │  N:1  │  (ACCTDAT)  │
│             │       │  XREF-CARD-NUM   │       │             │
│  CUST-ID ●──┤       │  XREF-CUST-ID ──→│       │←──ACCT-ID ● │
│  Name       │       │  XREF-ACCT-ID ──→│       │  Balance    │
│  Address    │       └────────┬─────────┘       │  Limits     │
│  SSN (PII)  │               │                  │  Dates      │
│  FICO Score │               │ 1:1              │  Group-ID──→├─────────────────┐
└─────────────┘        ┌──────┴──────┐           └─────────────┘  ┌─────────────┤
                       │    CARD     │                             │  DISC GROUP │
                       │  (CARDDAT)  │                             │  (DISCGRP)  │
                       │             │                             │  Int Rate   │
                       │  CARD-NUM ● │                             └─────────────┘
                       │  CVV        │
                       │  Name       │
                       │  Exp Date   │
                       └──────┬──────┘
                              │ 1:N
                       ┌──────┴──────┐      ┌──────────────┐   ┌──────────────┐
                       │ TRANSACTION │─────→│  TRAN TYPE   │   │  TRAN CAT    │
                       │ (TRANSACT)  │      │  (TRANTYPE)  │   │  (TRANCATG)  │
                       │             │      └──────────────┘   └──────────────┘
                       │  TRAN-ID ●  │
                       │  Amount     │──────→ ┌──────────────┐
                       │  Merchant   │        │ TCAT BALANCE │
                       │  Timestamps │        │  (TCATBAL)   │
                       └─────────────┘        └──────────────┘

    ● = Primary Key    → = Foreign Key    ←→ = Bidirectional Lookup
```

---

## 17. Modernization Mapping

| COBOL Entity           | Suggested Java Class     | Suggested DB Table       | Notes                                     |
|------------------------|--------------------------|--------------------------|-------------------------------------------|
| ACCOUNT-RECORD         | `Account.java`           | `accounts`               | Balance fields → BigDecimal               |
| CARD-RECORD            | `Card.java`              | `cards`                  | FK to accounts table                      |
| CARD-XREF-RECORD       | —                        | (Use FK relationships)   | Replaced by JPA relationships             |
| CUSTOMER-RECORD        | `Customer.java`          | `customers`              | Encrypt SSN; mask PII in logs             |
| TRAN-RECORD            | `Transaction.java`       | `transactions`           | Timestamps → `java.time.Instant`          |
| DALYTRAN-RECORD        | `DailyTransaction.java`  | `daily_transactions`     | Or use `status` flag in transactions table|
| SEC-USER-DATA          | `User.java`              | `users`                  | Hash passwords (BCrypt); add roles table  |
| TRAN-CAT-BAL-RECORD    | `CategoryBalance.java`   | `category_balances`      | Composite key → @IdClass or @EmbeddedId   |
| DIS-GROUP-RECORD       | `DiscountGroup.java`     | `discount_groups`        | Rate → BigDecimal                         |
| TRAN-TYPE              | `TransactionType.java`   | `transaction_types`      | Enum or reference table                   |
| TRAN-CAT               | `TransactionCategory.java`| `transaction_categories`| Enum or reference table                   |
| CARDDEMO-COMMAREA      | `SessionContext.java`    | (HTTP Session / JWT)     | Replace COMMAREA with session/token state |

**Key Modernization Considerations:**
- All `PIC S9(n)V99` fields → `java.math.BigDecimal` (never `double` for financial data)
- All `PIC X(10)` date fields → `java.time.LocalDate`
- All `PIC X(26)` timestamp fields → `java.time.Instant` or `java.time.LocalDateTime`
- FILLER fields are dropped in the modernized schema
- VSAM KSDS → relational DB with primary keys and foreign key constraints
- REDEFINES in CVEXPORT → polymorphic inheritance or discriminator pattern

---

*End of Data Dictionary*
