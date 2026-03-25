# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** COBOL copybooks in `app/cpy/` and optional modules
> **Application:** CardDemo — Mainframe Credit Card Management System

---

## Overview

This data dictionary catalogs every business entity defined in the CardDemo copybooks, translating COBOL PIC clauses into business-friendly field descriptions. The system manages **7 primary VSAM files** and **3 reference/lookup files**, plus supporting structures for IMS and DB2 in optional modules.

---

## 1. Account Master (CVACT01Y.cpy)

**VSAM Dataset:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`
**Record Length:** 300 bytes | **Key:** Account ID (11 bytes, position 0)
**Business Purpose:** Core account record storing credit card account details, balances, and credit limits.

| Field Name | COBOL PIC | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| ACCT-ID | 9(11) | Numeric | 11 | Account identifier (primary key) |
| ACCT-ACTIVE-STATUS | X(01) | Alpha | 1 | Account status flag (Y=Active, N=Inactive) |
| ACCT-CURR-BAL | S9(10)V99 | Signed Decimal | 12 | Current account balance |
| ACCT-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12 | Credit limit amount |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12 | Cash advance credit limit |
| ACCT-OPEN-DATE | X(10) | Alpha | 10 | Account open date |
| ACCT-EXPIRAION-DATE | X(10) | Alpha | 10 | Account expiration date |
| ACCT-REISSUE-DATE | X(10) | Alpha | 10 | Last card reissue date |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | Signed Decimal | 12 | Current cycle credits |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | Signed Decimal | 12 | Current cycle debits |
| ACCT-ADDR-ZIP | X(10) | Alpha | 10 | Account holder ZIP code |
| ACCT-GROUP-ID | X(10) | Alpha | 10 | Disclosure/interest rate group |
| FILLER | X(178) | — | 178 | Reserved space |

**Modernization Target:** `accounts` table in relational DB.

---

## 2. Card Master (CVACT02Y.cpy)

**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`
**Record Length:** 150 bytes | **Key:** Card Number (16 bytes, position 0)
**Business Purpose:** Credit card records linking cards to accounts and customers.

| Field Name | COBOL PIC | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| CARD-NUM | X(16) | Alpha | 16 | Credit card number (primary key) |
| CARD-ACCT-ID | 9(11) | Numeric | 11 | Parent account ID (FK → Account) |
| CARD-CVV-CD | 9(03) | Numeric | 3 | Card verification value (CVV) |
| CARD-EMBOSSED-NAME | X(50) | Alpha | 50 | Name embossed on card |
| CARD-EXPIRAION-DATE | X(10) | Alpha | 10 | Card expiration date |
| CARD-ACTIVE-STATUS | X(01) | Alpha | 1 | Card status (Y=Active, N=Inactive) |
| FILLER | X(59) | — | 59 | Reserved space |

**Modernization Target:** `cards` table; FK to `accounts`.

---

## 3. Customer Master (CVCUS01Y.cpy)

**VSAM Dataset:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`
**Record Length:** 500 bytes | **Key:** Customer ID (9 bytes, position 0)
**Business Purpose:** Customer personal information and demographics.

| Field Name | COBOL PIC | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| CUST-ID | 9(09) | Numeric | 9 | Customer identifier (primary key) |
| CUST-FIRST-NAME | X(25) | Alpha | 25 | First name |
| CUST-MIDDLE-NAME | X(25) | Alpha | 25 | Middle name |
| CUST-LAST-NAME | X(25) | Alpha | 25 | Last name |
| CUST-ADDR-LINE-1 | X(50) | Alpha | 50 | Address line 1 |
| CUST-ADDR-LINE-2 | X(50) | Alpha | 50 | Address line 2 |
| CUST-ADDR-LINE-3 | X(50) | Alpha | 50 | Address line 3 |
| CUST-ADDR-STATE-CD | X(02) | Alpha | 2 | State code |
| CUST-ADDR-COUNTRY-CD | X(03) | Alpha | 3 | Country code |
| CUST-ADDR-ZIP | X(10) | Alpha | 10 | ZIP/postal code |
| CUST-PHONE-NUM-1 | X(15) | Alpha | 15 | Primary phone |
| CUST-PHONE-NUM-2 | X(15) | Alpha | 15 | Secondary phone |
| CUST-SSN | 9(09) | Numeric | 9 | Social Security Number (PII) |
| CUST-GOVT-ISSUED-ID | X(20) | Alpha | 20 | Government-issued ID |
| CUST-DOB-YYYYMMDD | X(10) | Alpha | 10 | Date of birth |
| CUST-EFT-ACCOUNT-ID | X(10) | Alpha | 10 | EFT/bank account for payments |
| CUST-PRI-CARD-HOLDER-IND | X(01) | Alpha | 1 | Primary cardholder indicator |
| CUST-FICO-CREDIT-SCORE | 9(03) | Numeric | 3 | FICO credit score |
| FILLER | X(168) | — | 168 | Reserved space |

**PII Fields:** CUST-SSN, CUST-DOB-YYYYMMDD, CUST-GOVT-ISSUED-ID — require encryption/masking in modern system.
**Modernization Target:** `customers` table.

---

## 4. Card Cross-Reference (CVACT03Y.cpy)

**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`
**Record Length:** 50 bytes | **Key:** Card Number (16 bytes, position 0) | **AIX:** Account ID (11 bytes, position 25)
**Business Purpose:** Maps cards to accounts and customers (junction/bridge table).

| Field Name | COBOL PIC | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| XREF-CARD-NUM | X(16) | Alpha | 16 | Card number (primary key) |
| XREF-CUST-ID | 9(09) | Numeric | 9 | Customer ID (FK → Customer) |
| XREF-ACCT-ID | 9(11) | Numeric | 11 | Account ID (FK → Account) |
| FILLER | X(14) | — | 14 | Reserved space |

**Modernization Target:** May be eliminated — relationships can be expressed as FKs on `cards` table.

---

## 5. Transaction Master (CVTRA05Y.cpy)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`
**Record Length:** 350 bytes | **Key:** Transaction ID (16 bytes, position 0) | **AIX:** Card Number
**Business Purpose:** Core transaction records for all credit card activities.

| Field Name | COBOL PIC | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| TRAN-ID | X(16) | Alpha | 16 | Transaction identifier (primary key) |
| TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code (FK → Tran Type) |
| TRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| TRAN-SOURCE | X(10) | Alpha | 10 | Transaction source (POS, ATM, Online, etc.) |
| TRAN-DESC | X(100) | Alpha | 100 | Transaction description |
| TRAN-AMT | S9(09)V99 | Signed Decimal | 11 | Transaction amount |
| TRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant identifier |
| TRAN-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| TRAN-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| TRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP code |
| TRAN-CARD-NUM | X(16) | Alpha | 16 | Card used for transaction |
| TRAN-ORIG-TS | X(26) | Alpha | 26 | Original timestamp |
| TRAN-PROC-TS | X(26) | Alpha | 26 | Processing timestamp |
| FILLER | X(20) | — | 20 | Reserved space |

**Modernization Target:** `transactions` table; FKs to `cards`, `transaction_types`, `merchants`.

---

## 6. Daily Transaction (CVTRA06Y.cpy)

**VSAM Dataset:** `AWS.M2.CARDDEMO.DAILYTRAN.VSAM.KSDS`
**Record Length:** 350 bytes | **Key:** Daily Transaction ID (16 bytes)
**Business Purpose:** Staging area for daily unposted transactions before batch posting to Transaction Master.

| Field Name | COBOL PIC | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| DALYTRAN-ID | X(16) | Alpha | 16 | Daily transaction ID |
| DALYTRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| DALYTRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| DALYTRAN-SOURCE | X(10) | Alpha | 10 | Transaction source |
| DALYTRAN-DESC | X(100) | Alpha | 100 | Transaction description |
| DALYTRAN-AMT | S9(09)V99 | Signed Decimal | 11 | Transaction amount |
| DALYTRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant identifier |
| DALYTRAN-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| DALYTRAN-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| DALYTRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP code |
| DALYTRAN-CARD-NUM | X(16) | Alpha | 16 | Card number |
| DALYTRAN-ORIG-TS | X(26) | Alpha | 26 | Original timestamp |
| DALYTRAN-PROC-TS | X(26) | Alpha | 26 | Processing timestamp |
| FILLER | X(20) | — | 20 | Reserved space |

**Modernization Note:** In a modern system this would be a `daily_transactions` staging table or an event queue.

---

## 7. Statement Transaction Layout (COSTM01.CPY)

**Used By:** CBSTM03A (statement generation)
**Record Length:** 350 bytes | **Key:** Card Number (16) + Transaction ID (16) = 32 bytes
**Business Purpose:** Re-keyed transaction layout for statement generation, sorted by card then transaction.

| Field Name | COBOL PIC | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| TRNX-CARD-NUM | X(16) | Alpha | 16 | Card number (primary sort key) |
| TRNX-ID | X(16) | Alpha | 16 | Transaction ID (secondary sort key) |
| TRNX-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| TRNX-CAT-CD | 9(04) | Numeric | 4 | Category code |
| TRNX-SOURCE | X(10) | Alpha | 10 | Transaction source |
| TRNX-DESC | X(100) | Alpha | 100 | Description |
| TRNX-AMT | S9(09)V99 | Signed Decimal | 11 | Amount |
| TRNX-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant ID |
| TRNX-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| TRNX-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| TRNX-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP |
| TRNX-ORIG-TS | X(26) | Alpha | 26 | Original timestamp |
| TRNX-PROC-TS | X(26) | Alpha | 26 | Processing timestamp |
| FILLER | X(20) | — | 20 | Reserved |

---

## 8. User Security Record (CSUSR01Y.cpy)

**VSAM Dataset:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`
**Record Length:** 80 bytes | **Key:** User ID (8 bytes)
**Business Purpose:** User authentication and authorization records.

| Field Name | COBOL PIC | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| SEC-USR-ID | X(08) | Alpha | 8 | User login ID (primary key) |
| SEC-USR-FNAME | X(20) | Alpha | 20 | User first name |
| SEC-USR-LNAME | X(20) | Alpha | 20 | User last name |
| SEC-USR-PWD | X(08) | Alpha | 8 | Password (plaintext — security risk!) |
| SEC-USR-TYPE | X(01) | Alpha | 1 | User type (A=Admin, U=Regular User) |
| SEC-USR-FILLER | X(23) | — | 23 | Reserved space |

**Security Note:** Passwords are stored in plaintext. Modern system must use hashed passwords with salt.
**Modernization Target:** `users` table with bcrypt/argon2 password hashing.

---

## 9. Transaction Category Balance (CVTRA01Y.cpy)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS`
**Record Length:** 50 bytes | **Key:** Account ID (11) + Type Code (2) + Category Code (4) = 17 bytes
**Business Purpose:** Running balance per transaction category per account (e.g., purchases, cash advances, balance transfers).

| Field Name | COBOL PIC | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| TRANCAT-ACCT-ID | 9(11) | Numeric | 11 | Account ID |
| TRANCAT-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| TRANCAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| TRAN-CAT-BAL | S9(09)V99 | Signed Decimal | 11 | Category balance amount |
| FILLER | X(22) | — | 22 | Reserved |

**Modernization Target:** Computed view or `account_category_balances` table.

---

## 10. Disclosure Group / Interest Rate (CVTRA02Y.cpy)

**VSAM Dataset:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`
**Record Length:** 50 bytes | **Key:** Group ID (10) + Type Code (2) + Category Code (4) = 16 bytes
**Business Purpose:** Interest rate configuration per disclosure group, transaction type, and category.

| Field Name | COBOL PIC | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| DIS-ACCT-GROUP-ID | X(10) | Alpha | 10 | Disclosure group identifier |
| DIS-TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| DIS-TRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| DIS-INT-RATE | S9(04)V99 | Signed Decimal | 6 | Interest rate (APR) |
| FILLER | X(28) | — | 28 | Reserved |

**Modernization Target:** `interest_rate_rules` configuration table.

---

## 11. Transaction Type (CVTRA03Y.cpy)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`
**Record Length:** 60 bytes | **Key:** Type Code (2 bytes)
**Business Purpose:** Lookup table for transaction type codes and descriptions.

| Field Name | COBOL PIC | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| TRAN-TYPE | X(02) | Alpha | 2 | Transaction type code (primary key) |
| TRAN-TYPE-DESC | X(50) | Alpha | 50 | Transaction type description |
| FILLER | X(08) | — | 8 | Reserved |

**Example Values:** "01" = Purchase, "02" = Cash Advance, "03" = Balance Transfer, etc.
**Modernization Target:** `transaction_types` lookup/enum table.

---

## 12. Transaction Category (CVTRA04Y.cpy)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`
**Record Length:** 60 bytes | **Key:** Type Code (2) + Category Code (4) = 6 bytes
**Business Purpose:** Sub-categories within each transaction type.

| Field Name | COBOL PIC | Type | Length | Business Description |
|------------|-----------|------|--------|---------------------|
| TRAN-TYPE-CD | X(02) | Alpha | 2 | Parent transaction type code |
| TRAN-CAT-CD | 9(04) | Numeric | 4 | Category code |
| TRAN-CAT-TYPE-DESC | X(50) | Alpha | 50 | Category description |
| FILLER | X(04) | — | 4 | Reserved |

**Modernization Target:** `transaction_categories` table; FK to `transaction_types`.

---

## 13. Transaction Report Layout (CVTRA07Y.cpy)

**Used By:** CBTRN03C (daily transaction report)
**Business Purpose:** Print formatting structures for the daily transaction report.

### Report Header
| Field Name | COBOL PIC | Length | Description |
|------------|-----------|--------|-------------|
| REPT-SHORT-NAME | X(38) | 38 | Report short name ("DALYREPT") |
| REPT-LONG-NAME | X(41) | 41 | Report title ("Daily Transaction Report") |
| REPT-DATE-HEADER | X(12) | 12 | "Date Range: " label |
| REPT-START-DATE | X(10) | 10 | Report start date |
| REPT-END-DATE | X(10) | 10 | Report end date |

### Transaction Detail Line
| Field Name | COBOL PIC | Length | Description |
|------------|-----------|--------|-------------|
| TRAN-REPORT-TRANS-ID | X(16) | 16 | Transaction ID |
| TRAN-REPORT-ACCOUNT-ID | X(11) | 11 | Account ID |
| TRAN-REPORT-TYPE-CD | X(02) | 2 | Type code |
| TRAN-REPORT-TYPE-DESC | X(15) | 15 | Type description |
| TRAN-REPORT-CAT-CD | 9(04) | 4 | Category code |
| TRAN-REPORT-CAT-DESC | X(29) | 29 | Category description |
| TRAN-REPORT-SOURCE | X(10) | 10 | Transaction source |
| TRAN-REPORT-AMT | -ZZZ,ZZZ,ZZZ.ZZ | 15 | Formatted amount |

### Report Totals
| Field Name | Format | Description |
|------------|--------|-------------|
| REPT-PAGE-TOTAL | +ZZZ,ZZZ,ZZZ.ZZ | Page subtotal |
| REPT-ACCOUNT-TOTAL | +ZZZ,ZZZ,ZZZ.ZZ | Account subtotal |
| REPT-GRAND-TOTAL | +ZZZ,ZZZ,ZZZ.ZZ | Report grand total |

---

## 14. Common Communication Area (COCOM01Y.cpy)

**Used By:** All online CICS programs (passed via DFHCOMMAREA)
**Business Purpose:** Shared data area passed between CICS programs for navigation, user context, and inter-program communication.

| Field Name | COBOL PIC | Length | Description |
|------------|-----------|--------|-------------|
| CDEMO-FROM-TRANID | X(04) | 4 | Calling transaction ID |
| CDEMO-FROM-PROGRAM | X(08) | 8 | Calling program name |
| CDEMO-TO-TRANID | X(04) | 4 | Target transaction ID |
| CDEMO-TO-PROGRAM | X(08) | 8 | Target program name |
| CDEMO-USER-ID | X(08) | 8 | Current logged-in user ID |
| CDEMO-USER-TYPE | X(01) | 1 | User type (A=Admin, U=User) |
| CDEMO-PGM-CONTEXT | 9(01) | 1 | Program context flag |
| CDEMO-ACCT-ID | 9(11) | 11 | Current account being viewed |
| CDEMO-CARD-NUM | X(16) | 16 | Current card number |
| CDEMO-CUST-ID | 9(09) | 9 | Current customer ID |
| CDEMO-LAST-MAP | X(07) | 7 | Last BMS map sent |
| CDEMO-LAST-MAPSET | X(07) | 7 | Last BMS mapset used |

---

## 15. Export File Descriptors (CVEXPORT.cpy)

**Used By:** CBEXPORT, CBIMPORT
**Business Purpose:** File descriptors for all VSAM-to-flat-file export/import operations.

Defines FD records for:
- Account data export (FD-ACCTFILE-REC, 300 bytes)
- Customer data export (FD-CUSTFILE-REC, 500 bytes)
- Card data export (FD-CARDFILE-REC, 150 bytes)
- Cross-reference export (FD-CARDXREF-REC, 50 bytes)
- Transaction data export (FD-TRANFILE-REC, 350 bytes)
- Transaction type export (FD-TRANTYPE-REC, 60 bytes)
- Transaction category export (FD-TRANCATG-REC, 60 bytes)
- Category balance export (FD-TCATBALF-REC, 50 bytes)

---

## 16. Authorization Module Entities (Optional)

### 16a. Auth Summary Segment (CIPAUSMY.cpy)

**IMS Database:** PAUTDB | **Segment:** Summary
**Business Purpose:** Summary of payment authorization messages from MQ.

| Field Name | COBOL PIC | Length | Description |
|------------|-----------|--------|-------------|
| PAUS-CARD-NUM | X(16) | 16 | Card number |
| PAUS-TRAN-AMT | S9(09)V99 | 11 | Authorization amount |
| PAUS-TRAN-DATE | X(08) | 8 | Transaction date |
| PAUS-TRAN-TIME | X(08) | 8 | Transaction time |
| PAUS-TRAN-STATUS | X(02) | 2 | Status (AP=Approved, DN=Denied) |
| PAUS-MERC-ID | 9(09) | 9 | Merchant ID |
| PAUS-MERC-NAME | X(30) | 30 | Merchant name |

### 16b. Auth Detail Segment (CIPAUDTY.cpy)

**IMS Database:** PAUTDB | **Segment:** Detail (child of Summary)
**Business Purpose:** Detailed authorization data including fraud flags.

| Field Name | COBOL PIC | Length | Description |
|------------|-----------|--------|-------------|
| PAUD-TRAN-ID | X(16) | 16 | Transaction ID |
| PAUD-RESP-CODE | X(04) | 4 | Authorization response code |
| PAUD-FRAUD-FLAG | X(01) | 1 | Fraud indicator (Y/N) |
| PAUD-REVIEW-DATE | X(08) | 8 | Fraud review date |
| PAUD-REVIEW-BY | X(08) | 8 | Reviewer user ID |

### 16c. Auth MQ Request (CCPAURQY.cpy)

| Field Name | Length | Description |
|------------|--------|-------------|
| PAURQ-CARD-NUM | 16 | Card number for authorization |
| PAURQ-TRAN-AMT | 11 | Requested amount |
| PAURQ-MERC-ID | 9 | Merchant ID |
| PAURQ-MERC-NAME | 30 | Merchant name |

### 16d. Auth MQ Reply (CCPAURLY.cpy)

| Field Name | Length | Description |
|------------|--------|-------------|
| PAURL-RESP-CODE | 4 | Authorization response code |
| PAURL-AVAIL-BAL | 11 | Available balance after auth |
| PAURL-STATUS | 2 | AP (Approved) / DN (Denied) |

---

## 17. DB2 Module Entities (Optional)

### DB2 Common Working Storage (CSDB2RWY.cpy)

| Field Name | COBOL PIC | Description |
|------------|-----------|-------------|
| WS-DISP-SQLCODE | ----9 | Displayable SQLCODE |
| WS-DUMMY-DB2-INT | S9(4) COMP-3 | DB2 connectivity test variable |
| WS-DB2-PROCESSING-FLAG | X(1) | DB2 status (0=OK, 1=Error) |
| WS-DB2-CURRENT-ACTION | X(72) | Current DB2 action description |
| WS-DSNTIAC-FORMATTED | — | DSNTIAC error message area |

---

## 18. IMS PCB Structures (Optional)

### PAUTBPCB.CPY — Auth Database PCB

| Field Name | COBOL PIC | Description |
|------------|-----------|-------------|
| PAUT-DBDNAME | X(08) | IMS DBD name |
| PAUT-SEG-LEVEL | X(02) | Segment level |
| PAUT-PCB-STATUS | X(02) | PCB status code (blank=OK, GE=not found) |
| PAUT-PCB-PROCOPT | X(04) | Processing options |
| PAUT-SEG-NAME | X(08) | Segment name |
| PAUT-KEYFB | X(255) | Key feedback area |

---

## Entity Relationship Summary

```
┌─────────────┐     ┌──────────────┐     ┌─────────────────┐
│  Customer    │────<│  Card XREF   │>────│    Account      │
│  (CVCUS01Y)  │     │  (CVACT03Y)  │     │   (CVACT01Y)    │
│  Key: CustID │     │  Key: CardNum│     │   Key: AcctID   │
└─────────────┘     └──────┬───────┘     └────────┬────────┘
                           │                      │
                    ┌──────┴───────┐     ┌────────┴────────┐
                    │     Card     │     │  Tran Cat Bal   │
                    │  (CVACT02Y)  │     │   (CVTRA01Y)    │
                    │  Key: CardNum│     │ Key: Acct+Type  │
                    └──────┬───────┘     │     +Cat        │
                           │             └─────────────────┘
                    ┌──────┴───────┐
                    │ Transaction  │     ┌─────────────────┐
                    │  (CVTRA05Y)  │────>│  Tran Type      │
                    │  Key: TranID │     │   (CVTRA03Y)    │
                    └──────┬───────┘     └─────────────────┘
                           │
                    ┌──────┴───────┐     ┌─────────────────┐
                    │Daily Trans   │     │  Tran Category  │
                    │  (CVTRA06Y)  │     │   (CVTRA04Y)    │
                    │  Staging     │     │ Key: Type+Cat   │
                    └──────────────┘     └─────────────────┘

                    ┌──────────────┐     ┌─────────────────┐
                    │ User Security│     │ Disclosure Grp  │
                    │  (CSUSR01Y)  │     │   (CVTRA02Y)    │
                    │  Key: UserID │     │ Key: Grp+Type   │
                    └──────────────┘     │     +Cat        │
                                         └─────────────────┘
```

---

## VSAM File Summary

| # | Logical Name | VSAM Type | Dataset Name | Record Len | Key Len | Key Pos | Business Entity |
|---|-------------|-----------|-------------|------------|---------|---------|-----------------|
| 1 | ACCTFILE | KSDS | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | 300 | 11 | 0 | Account |
| 2 | CARDFILE | KSDS | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | 150 | 16 | 0 | Card |
| 3 | CUSTFILE | KSDS | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | 500 | 9 | 0 | Customer |
| 4 | CARDXREF | KSDS+AIX | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | 50 | 16 | 0 | Card Cross-Ref |
| 5 | TRANSACT | KSDS+AIX | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | 350 | 16 | 0 | Transaction |
| 6 | DALYTRAN | KSDS | AWS.M2.CARDDEMO.DAILYTRAN.VSAM.KSDS | 350 | 16 | 0 | Daily Transaction |
| 7 | USRSEC | KSDS | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | 80 | 8 | 0 | User Security |
| 8 | TRANTYPE | KSDS | AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | 60 | 2 | 0 | Transaction Type |
| 9 | TRANCATG | KSDS | AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | 60 | 6 | 0 | Transaction Category |
| 10 | TCATBALF | KSDS | AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | 50 | 17 | 0 | Category Balance |
| 11 | DISCGRP | KSDS | AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | 50 | 16 | 0 | Disclosure Group |

---

## COBOL PIC Clause Quick Reference

| COBOL PIC | Java Type | SQL Type | Description |
|-----------|-----------|----------|-------------|
| X(n) | String | VARCHAR(n) | Alphanumeric, n characters |
| 9(n) | long / int | NUMERIC(n) | Unsigned integer, n digits |
| S9(n)V99 | BigDecimal | DECIMAL(n+2,2) | Signed decimal with 2 decimal places |
| S9(n) COMP | int / long | INTEGER | Binary integer |
| S9(n) COMP-3 | BigDecimal | DECIMAL | Packed decimal |
