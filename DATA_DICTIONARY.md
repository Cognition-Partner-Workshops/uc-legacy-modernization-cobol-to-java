# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo | **Source**: COBOL Copybooks (`app/cpy/`)

---

## Overview

This document extracts all business data entities from COBOL copybook PIC clause definitions and presents them in a business-friendly format. Each entity maps to a VSAM file (or DB2 table in extension modules) and represents a core business concept in the credit card management system.

---

## 1. Account Master (`CVACT01Y.cpy`)

**VSAM Dataset**: `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | **Record Length**: 300 bytes | **Key**: Account ID (11 digits)

| Field Name              | PIC Clause        | Type      | Length | Business Description                    |
|-------------------------|-------------------|-----------|--------|------------------------------------------|
| ACCT-ID                 | 9(11)             | Numeric   | 11     | Unique account identifier                |
| ACCT-ACTIVE-STATUS      | X(01)             | Alpha     | 1      | Account status (Y=Active, N=Inactive)    |
| ACCT-CURR-BAL           | S9(10)V99         | Signed Dec| 12     | Current account balance                  |
| ACCT-CREDIT-LIMIT       | S9(10)V99         | Signed Dec| 12     | Credit limit                             |
| ACCT-CASH-CREDIT-LIMIT  | S9(10)V99         | Signed Dec| 12     | Cash advance credit limit                |
| ACCT-OPEN-DATE          | X(10)             | Alpha     | 10     | Date account was opened                  |
| ACCT-EXPIRAION-DATE     | X(10)             | Alpha     | 10     | Account expiration date                  |
| ACCT-REISSUE-DATE       | X(10)             | Alpha     | 10     | Last card reissue date                   |
| ACCT-CURR-CYC-CREDIT    | S9(10)V99        | Signed Dec| 12     | Current cycle credit total               |
| ACCT-CURR-CYC-DEBIT     | S9(10)V99        | Signed Dec| 12     | Current cycle debit total                |
| ACCT-ADDR-ZIP           | X(10)             | Alpha     | 10     | Account holder ZIP code                  |
| ACCT-GROUP-ID           | X(10)             | Alpha     | 10     | Disclosure group ID (interest rate tier) |
| FILLER                  | X(178)            | —         | 178    | Reserved                                 |

**Business Context**: The central financial entity. Each account tracks balances, credit limits, cycle totals, and links to a disclosure group that determines the interest rate tier.

---

## 2. Card Data (`CVACT02Y.cpy`)

**VSAM Dataset**: `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` | **Record Length**: 150 bytes | **Key**: Card Number (16 chars)

| Field Name              | PIC Clause        | Type      | Length | Business Description                    |
|-------------------------|-------------------|-----------|--------|------------------------------------------|
| CARD-NUM                | X(16)             | Alpha     | 16     | Credit card number (PAN)                 |
| CARD-ACCT-ID            | 9(11)             | Numeric   | 11     | Linked account ID                        |
| CARD-CVV-CD             | 9(03)             | Numeric   | 3      | Card verification value (CVV)            |
| CARD-EMBOSSED-NAME      | X(50)             | Alpha     | 50     | Name embossed on card                    |
| CARD-EXPIRAION-DATE     | X(10)             | Alpha     | 10     | Card expiration date                     |
| CARD-ACTIVE-STATUS      | X(01)             | Alpha     | 1      | Card status (Y=Active, N=Inactive)       |
| FILLER                  | X(59)             | —         | 59     | Reserved                                 |

**Business Context**: Physical card details. Multiple cards can be linked to a single account via the cross-reference file. Sensitive fields include the CVV and card number (PAN).

---

## 3. Card Cross-Reference (`CVACT03Y.cpy`)

**VSAM Dataset**: `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | **Record Length**: 50 bytes | **Key**: Card Number (16 chars)

| Field Name              | PIC Clause        | Type      | Length | Business Description                    |
|-------------------------|-------------------|-----------|--------|------------------------------------------|
| XREF-CARD-NUM           | X(16)             | Alpha     | 16     | Credit card number                       |
| XREF-CUST-ID            | 9(09)             | Numeric   | 9      | Customer ID                              |
| XREF-ACCT-ID            | 9(11)             | Numeric   | 11     | Account ID                               |
| FILLER                  | X(14)             | —         | 14     | Reserved                                 |

**Business Context**: The critical junction table that links cards to customers and accounts. Has an alternate index on ACCT-ID for reverse lookups (find all cards for an account). This is the most frequently accessed file during transaction processing.

---

## 4. Customer Data (`CVCUS01Y.cpy`)

**VSAM Dataset**: `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` | **Record Length**: 500 bytes | **Key**: Customer ID (9 digits)

| Field Name              | PIC Clause        | Type      | Length | Business Description                    |
|-------------------------|-------------------|-----------|--------|------------------------------------------|
| CUST-ID                 | 9(09)             | Numeric   | 9      | Unique customer identifier               |
| CUST-FIRST-NAME         | X(25)             | Alpha     | 25     | Customer first name                      |
| CUST-MIDDLE-NAME        | X(25)             | Alpha     | 25     | Customer middle name                     |
| CUST-LAST-NAME          | X(25)             | Alpha     | 25     | Customer last name                       |
| CUST-ADDR-LINE-1        | X(50)             | Alpha     | 50     | Address line 1                           |
| CUST-ADDR-LINE-2        | X(50)             | Alpha     | 50     | Address line 2                           |
| CUST-ADDR-LINE-3        | X(50)             | Alpha     | 50     | Address line 3                           |
| CUST-ADDR-STATE-CD      | X(02)             | Alpha     | 2      | US state code                            |
| CUST-ADDR-COUNTRY-CD    | X(03)             | Alpha     | 3      | Country code                             |
| CUST-ADDR-ZIP           | X(10)             | Alpha     | 10     | ZIP / postal code                        |
| CUST-PHONE-NUM-1        | X(15)             | Alpha     | 15     | Primary phone                            |
| CUST-PHONE-NUM-2        | X(15)             | Alpha     | 15     | Secondary phone                          |
| CUST-SSN                | 9(09)             | Numeric   | 9      | Social Security Number (PII)             |
| CUST-GOVT-ISSUED-ID     | X(20)             | Alpha     | 20     | Government-issued ID number              |
| CUST-DOB-YYYY-MM-DD     | X(10)             | Alpha     | 10     | Date of birth                            |
| CUST-EFT-ACCOUNT-ID     | X(10)             | Alpha     | 10     | EFT / bank account for payments          |
| CUST-PRI-CARD-HOLDER-IND| X(01)             | Alpha     | 1      | Primary card holder indicator            |
| CUST-FICO-CREDIT-SCORE  | 9(03)             | Numeric   | 3      | FICO credit score                        |
| FILLER                  | X(168)            | —         | 168    | Reserved                                 |

**Business Context**: Full customer profile including PII (SSN, DOB, address). The FICO score drives credit decisions. Linked to accounts via the cross-reference file.

---

## 5. Transaction Record (`CVTRA05Y.cpy`)

**VSAM Dataset**: `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | **Record Length**: 350 bytes | **Key**: Transaction ID (16 chars)

| Field Name              | PIC Clause        | Type      | Length | Business Description                    |
|-------------------------|-------------------|-----------|--------|------------------------------------------|
| TRAN-ID                 | X(16)             | Alpha     | 16     | Unique transaction identifier            |
| TRAN-TYPE-CD            | X(02)             | Alpha     | 2      | Transaction type code                    |
| TRAN-CAT-CD             | 9(04)             | Numeric   | 4      | Transaction category code                |
| TRAN-SOURCE             | X(10)             | Alpha     | 10     | Transaction source (POS, ATM, Online)    |
| TRAN-DESC               | X(100)            | Alpha     | 100    | Transaction description / memo           |
| TRAN-AMT                | S9(09)V99         | Signed Dec| 11     | Transaction amount                       |
| TRAN-MERCHANT-ID        | 9(09)             | Numeric   | 9      | Merchant identifier                      |
| TRAN-MERCHANT-NAME      | X(50)             | Alpha     | 50     | Merchant name                            |
| TRAN-MERCHANT-CITY      | X(50)             | Alpha     | 50     | Merchant city                            |
| TRAN-MERCHANT-ZIP       | X(10)             | Alpha     | 10     | Merchant ZIP code                        |
| TRAN-CARD-NUM           | X(16)             | Alpha     | 16     | Card number used                         |
| TRAN-ORIG-TS            | X(26)             | Alpha     | 26     | Original transaction timestamp           |
| TRAN-PROC-TS            | X(26)             | Alpha     | 26     | Processing timestamp                     |
| FILLER                  | X(20)             | —         | 20     | Reserved                                 |

**Business Context**: Core transaction record capturing every purchase, payment, or cash advance. Contains merchant details, amounts, and timestamps. Posted by the daily batch cycle (CBTRN02C).

---

## 6. Daily Transaction Record (`CVTRA06Y.cpy`)

**VSAM Dataset**: `AWS.M2.CARDDEMO.DALYTRAN.PS` | **Record Length**: 350 bytes | **Sequential file**

| Field Name              | PIC Clause        | Type      | Length | Business Description                    |
|-------------------------|-------------------|-----------|--------|------------------------------------------|
| DALYTRAN-ID             | X(16)             | Alpha     | 16     | Daily transaction identifier             |
| DALYTRAN-TYPE-CD        | X(02)             | Alpha     | 2      | Transaction type code                    |
| DALYTRAN-CAT-CD         | 9(04)             | Numeric   | 4      | Transaction category code                |
| DALYTRAN-SOURCE         | X(10)             | Alpha     | 10     | Transaction source                       |
| DALYTRAN-DESC           | X(100)            | Alpha     | 100    | Transaction description                  |
| DALYTRAN-AMT            | S9(09)V99         | Signed Dec| 11     | Transaction amount                       |
| DALYTRAN-MERCHANT-ID    | 9(09)             | Numeric   | 9      | Merchant identifier                      |
| DALYTRAN-MERCHANT-NAME  | X(50)             | Alpha     | 50     | Merchant name                            |
| DALYTRAN-MERCHANT-CITY  | X(50)             | Alpha     | 50     | Merchant city                            |
| DALYTRAN-MERCHANT-ZIP   | X(10)             | Alpha     | 10     | Merchant ZIP code                        |
| DALYTRAN-CARD-NUM       | X(16)             | Alpha     | 16     | Card number used                         |
| DALYTRAN-ORIG-TS        | X(26)             | Alpha     | 26     | Original timestamp                       |
| DALYTRAN-PROC-TS        | X(26)             | Alpha     | 26     | Processing timestamp                     |
| FILLER                  | X(20)             | —         | 20     | Reserved                                 |

**Business Context**: Inbound daily feed of new transactions. This is the input to the posting batch cycle. Same layout as the master transaction file but stored as a sequential flat file for efficient batch processing.

---

## 7. Transaction Category Balance (`CVTRA01Y.cpy`)

**VSAM Dataset**: `AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS` | **Record Length**: 50 bytes | **Key**: Account ID + Type Code + Category Code

| Field Name              | PIC Clause        | Type      | Length | Business Description                    |
|-------------------------|-------------------|-----------|--------|------------------------------------------|
| TRANCAT-ACCT-ID         | 9(11)             | Numeric   | 11     | Account ID                               |
| TRANCAT-TYPE-CD         | X(02)             | Alpha     | 2      | Transaction type code                    |
| TRANCAT-CD              | 9(04)             | Numeric   | 4      | Transaction category code                |
| TRAN-CAT-BAL            | S9(09)V99         | Signed Dec| 11     | Running balance for this category        |
| FILLER                  | X(22)             | —         | 22     | Reserved                                 |

**Business Context**: Tracks the running balance for each transaction category within an account. Used by the interest calculation engine (CBACT04C) to apply category-specific interest rates.

---

## 8. Disclosure Group (`CVTRA02Y.cpy`)

**VSAM Dataset**: `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` | **Record Length**: 50 bytes | **Key**: Group ID + Type Code + Category Code

| Field Name              | PIC Clause        | Type      | Length | Business Description                    |
|-------------------------|-------------------|-----------|--------|------------------------------------------|
| DIS-ACCT-GROUP-ID       | X(10)             | Alpha     | 10     | Account disclosure group ID              |
| DIS-TRAN-TYPE-CD        | X(02)             | Alpha     | 2      | Transaction type code                    |
| DIS-TRAN-CAT-CD         | 9(04)             | Numeric   | 4      | Transaction category code                |
| DIS-INT-RATE            | S9(04)V99         | Signed Dec| 6      | Interest rate (APR %)                    |
| FILLER                  | X(28)             | —         | 28     | Reserved                                 |

**Business Context**: Defines interest rates per transaction type/category for each disclosure group. Accounts are assigned to a group via `ACCT-GROUP-ID`. This drives the interest calculation engine.

---

## 9. Transaction Type (`CVTRA03Y.cpy`)

**VSAM Dataset**: `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` | **Record Length**: 60 bytes | **Key**: Type Code (2 chars)

| Field Name              | PIC Clause        | Type      | Length | Business Description                    |
|-------------------------|-------------------|-----------|--------|------------------------------------------|
| TRAN-TYPE               | X(02)             | Alpha     | 2      | Transaction type code (e.g., SA, CR, FE)|
| TRAN-TYPE-DESC          | X(50)             | Alpha     | 50     | Description (e.g., "Sale", "Credit")     |
| FILLER                  | X(08)             | —         | 8      | Reserved                                 |

**Business Context**: Reference data defining the high-level transaction types (Sales, Credits, Cash Advances, Fees, Payments, etc.).

---

## 10. Transaction Category Type (`CVTRA04Y.cpy`)

**VSAM Dataset**: `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` | **Record Length**: 60 bytes | **Key**: Type Code + Category Code

| Field Name              | PIC Clause        | Type      | Length | Business Description                    |
|-------------------------|-------------------|-----------|--------|------------------------------------------|
| TRAN-TYPE-CD            | X(02)             | Alpha     | 2      | Transaction type code                    |
| TRAN-CAT-CD             | 9(04)             | Numeric   | 4      | Category code within type                |
| TRAN-CAT-TYPE-DESC      | X(50)             | Alpha     | 50     | Category description                     |
| FILLER                  | X(04)             | —         | 4      | Reserved                                 |

**Business Context**: Sub-classification of transactions. For example, under Sales (SA), categories might include Retail, Gas, Grocery, etc. Used for reporting and interest calculation.

---

## 11. User Security Record (`CSUSR01Y.cpy`)

**VSAM Dataset**: `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` | **Record Length**: 80 bytes | **Key**: User ID (8 chars)

| Field Name              | PIC Clause        | Type      | Length | Business Description                    |
|-------------------------|-------------------|-----------|--------|------------------------------------------|
| SEC-USR-ID              | X(08)             | Alpha     | 8      | User login ID                            |
| SEC-USR-FNAME           | X(20)             | Alpha     | 20     | User first name                          |
| SEC-USR-LNAME           | X(20)             | Alpha     | 20     | User last name                           |
| SEC-USR-PWD             | X(08)             | Alpha     | 8      | User password (plaintext)                |
| SEC-USR-TYPE            | X(01)             | Alpha     | 1      | User type (A=Admin, U=Regular)           |
| SEC-USR-FILLER          | X(23)             | —         | 23     | Reserved                                 |

**Business Context**: Authentication and authorization. Admin users access the admin menu (user CRUD, transaction type management). Passwords stored in plaintext -- a modernization target for proper hashing.

---

## 12. Statement Transaction Layout (`COSTM01.CPY`)

**Purpose**: Alternate record layout for reporting (key = Card Number + Transaction ID)

| Field Name              | PIC Clause        | Type      | Length | Business Description                    |
|-------------------------|-------------------|-----------|--------|------------------------------------------|
| TRNX-CARD-NUM           | X(16)             | Alpha     | 16     | Card number (part of key)                |
| TRNX-ID                 | X(16)             | Alpha     | 16     | Transaction ID (part of key)             |
| TRNX-TYPE-CD            | X(02)             | Alpha     | 2      | Transaction type code                    |
| TRNX-CAT-CD             | 9(04)             | Numeric   | 4      | Transaction category code                |
| TRNX-SOURCE             | X(10)             | Alpha     | 10     | Transaction source                       |
| TRNX-DESC               | X(100)            | Alpha     | 100    | Description                              |
| TRNX-AMT                | S9(09)V99         | Signed Dec| 11     | Amount                                   |
| TRNX-MERCHANT-ID        | 9(09)             | Numeric   | 9      | Merchant ID                              |
| TRNX-MERCHANT-NAME      | X(50)             | Alpha     | 50     | Merchant name                            |
| TRNX-MERCHANT-CITY      | X(50)             | Alpha     | 50     | Merchant city                            |
| TRNX-MERCHANT-ZIP       | X(10)             | Alpha     | 10     | Merchant ZIP                             |
| TRNX-ORIG-TS            | X(26)             | Alpha     | 26     | Original timestamp                       |
| TRNX-PROC-TS            | X(26)             | Alpha     | 26     | Processing timestamp                     |
| FILLER                  | X(20)             | —         | 20     | Reserved                                 |

**Business Context**: Re-keyed transaction layout with card number as the primary sort key, enabling per-card statement generation in CBSTM03A.

---

## 13. Export Multi-Record Layout (`CVEXPORT.cpy`)

**Purpose**: Multi-record type export file for branch data migration

| Field Name              | PIC Clause        | Type      | Length | Business Description                    |
|-------------------------|-------------------|-----------|--------|------------------------------------------|
| EXP-RECORD-TYPE         | X(02)             | Alpha     | 2      | Record type (CU/AC/XR/TR/CD)            |
| EXP-TIMESTAMP           | X(26)             | Alpha     | 26     | Export timestamp                         |
| *Customer fields*       | *(see CVCUS01Y)*  | —         | —      | Full customer record when type = CU      |
| *Account fields*        | *(see CVACT01Y)*  | —         | —      | Full account record when type = AC       |
| *Card-XREF fields*      | *(see CVACT03Y)*  | —         | —      | Cross-ref record when type = XR          |
| *Transaction fields*    | *(see CVTRA05Y)*  | —         | —      | Transaction record when type = TR        |
| *Card fields*           | *(see CVACT02Y)*  | —         | —      | Card data record when type = CD          |

**Business Context**: Unified export format for branch migration. CBEXPORT writes these records; CBIMPORT reads and splits them back into individual VSAM files.

---

## 14. Transaction Report Layout (`CVTRA07Y.cpy`)

**Purpose**: Print-formatted report data structures

| Structure               | Fields                                     | Business Description             |
|-------------------------|--------------------------------------------|----------------------------------|
| REPORT-NAME-HEADER      | Short name, long name, date range          | Report header line               |
| TRANSACTION-DETAIL-REPORT| Trans ID, Acct ID, Type, Category, Source, Amount | Detail line per transaction |
| TRANSACTION-HEADER-1/2  | Column headers and separator               | Report column headers            |
| REPORT-PAGE-TOTALS      | Page total amount                          | Running page total               |
| REPORT-ACCOUNT-TOTALS   | Account total amount                       | Per-account subtotal             |
| REPORT-GRAND-TOTALS     | Grand total amount                         | Report grand total               |

**Business Context**: Print layout for the daily transaction report (CBTRN03C / TRANREPT job).

---

## Entity Relationship Summary

```
┌──────────────┐     ┌──────────────────┐     ┌──────────────┐
│  CUSTOMER    │     │   CARD XREF      │     │   ACCOUNT    │
│  (CVCUS01Y)  │◄────│   (CVACT03Y)     │────►│  (CVACT01Y)  │
│  Key: CustID │     │  Key: CardNum    │     │  Key: AcctID │
└──────────────┘     │  FK: CustID,     │     └──────┬───────┘
                     │      AcctID       │            │
                     └────────┬──────────┘            │
                              │                       │
                     ┌────────▼──────────┐   ┌───────▼────────┐
                     │   CARD DATA       │   │  TRAN CAT BAL  │
                     │   (CVACT02Y)      │   │  (CVTRA01Y)    │
                     │  Key: CardNum     │   │  Key: AcctID+  │
                     └────────┬──────────┘   │   Type+Cat     │
                              │              └───────┬────────┘
                     ┌────────▼──────────┐           │
                     │  TRANSACTION      │   ┌───────▼────────┐
                     │  (CVTRA05Y)       │   │ DISCLOSURE GRP │
                     │  Key: TranID      │   │  (CVTRA02Y)    │
                     │  FK: CardNum      │   │  Key: GrpID+   │
                     └──────────────────┘   │   Type+Cat     │
                                             └────────────────┘
                     ┌──────────────────┐
                     │ TRAN TYPE        │   ┌──────────────────┐
                     │ (CVTRA03Y)       │   │ TRAN CATEGORY    │
                     │ Key: TypeCD      │   │ (CVTRA04Y)       │
                     └──────────────────┘   │ Key: TypeCD+Cat  │
                                             └──────────────────┘
                     ┌──────────────────┐
                     │ USER SECURITY    │
                     │ (CSUSR01Y)       │
                     │ Key: UserID      │
                     └──────────────────┘
```

---

## VSAM Dataset Summary

| VSAM Dataset Name                              | Record Len | Key          | Organization | Copybook   |
|------------------------------------------------|------------|--------------|--------------|------------|
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS            | 300        | AcctID(11,0) | KSDS         | CVACT01Y   |
| AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS            | 150        | CardNum(16,0)| KSDS         | CVACT02Y   |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS            | 50         | CardNum(16,0)| KSDS + AIX   | CVACT03Y   |
| AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS            | 500        | CustID(9,0)  | KSDS         | CVCUS01Y   |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS            | 350        | TranID(16,0) | KSDS + AIX   | CVTRA05Y   |
| AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS             | 50         | Composite(17)| KSDS         | CVTRA01Y   |
| AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS             | 50         | Composite(16)| KSDS         | CVTRA02Y   |
| AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS            | 60         | TypeCD(2,0)  | KSDS         | CVTRA03Y   |
| AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS            | 60         | Composite(6) | KSDS         | CVTRA04Y   |
| AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS              | 80         | UserID(8,0)  | KSDS         | CSUSR01Y   |
| AWS.M2.CARDDEMO.DALYTRAN.PS                    | 350        | Sequential   | PS (flat)    | CVTRA06Y   |

---

## PII / Sensitive Data Inventory

| Field                   | Entity     | Sensitivity | Modernization Note                      |
|-------------------------|------------|-------------|------------------------------------------|
| CUST-SSN                | Customer   | **HIGH**    | Must be encrypted at rest and in transit |
| CUST-DOB-YYYY-MM-DD     | Customer   | **HIGH**    | PII - age/identity                       |
| CUST-GOVT-ISSUED-ID     | Customer   | **HIGH**    | Government ID number                     |
| CARD-NUM                | Card       | **HIGH**    | PCI-DSS: must be tokenized/masked        |
| CARD-CVV-CD             | Card       | **HIGH**    | PCI-DSS: must never be stored post-auth  |
| SEC-USR-PWD             | User Sec   | **CRITICAL**| Plaintext password - must hash (bcrypt)  |
| CUST-PHONE-NUM-*        | Customer   | Medium      | PII - contact information                |
| CUST-ADDR-*             | Customer   | Medium      | PII - physical address                   |
| CUST-EFT-ACCOUNT-ID     | Customer   | **HIGH**    | Bank account number                      |
