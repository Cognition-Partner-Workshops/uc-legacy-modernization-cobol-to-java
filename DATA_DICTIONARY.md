# Data Dictionary - CardDemo

> **Generated:** 2026-03-25 | **Source:** COBOL Copybook PIC Clause Analysis
> **Application:** AWS CardDemo - Credit Card Management System

---

## Overview

This document extracts all business data entities from the CardDemo COBOL copybooks, translating mainframe PIC clauses into business-friendly field definitions. Each entity maps to a VSAM KSDS file (or DB2 table in optional modules) and corresponds to a future Java POJO/DTO and database table in the modernized system.

### PIC Clause Quick Reference

| COBOL PIC | Meaning | Java Equivalent |
|-----------|---------|----------------|
| `PIC X(n)` | Alphanumeric, n characters | `String` |
| `PIC 9(n)` | Unsigned numeric, n digits | `int` / `long` |
| `PIC S9(n)V99` | Signed decimal, n.2 digits | `BigDecimal` |
| `PIC 9(n) COMP` | Binary integer | `int` / `long` |
| `FILLER` | Padding / reserved space | *(not mapped)* |

---

## 1. Account Master (`CVACT01Y`) — 300 bytes

**VSAM File:** ACCTFILE (KSDS) | **Key:** Account ID (11 digits)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Unique account identifier |
| 2 | `ACCT-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 | Account status (Y=Active, N=Inactive) |
| 3 | `ACCT-CURR-BAL` | `PIC S9(10)V99` | Decimal | 12.2 | Current account balance |
| 4 | `ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | Decimal | 12.2 | Credit limit |
| 5 | `ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | Decimal | 12.2 | Cash advance credit limit |
| 6 | `ACCT-OPEN-DATE` | `PIC X(10)` | Alpha | 10 | Account opening date (YYYY-MM-DD) |
| 7 | `ACCT-EXPIRAION-DATE` | `PIC X(10)` | Alpha | 10 | Account expiration date |
| 8 | `ACCT-REISSUE-DATE` | `PIC X(10)` | Alpha | 10 | Last reissue date |
| 9 | `ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | Decimal | 12.2 | Current cycle credit amount |
| 10 | `ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | Decimal | 12.2 | Current cycle debit amount |
| 11 | `ACCT-ADDR-ZIP` | `PIC X(10)` | Alpha | 10 | Billing ZIP code |
| 12 | `ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 | Disclosure/rate group ID |
| 13 | `FILLER` | `PIC X(178)` | -- | 178 | Reserved space |

**Business Rules:**
- Key entity for all financial operations
- Balance updated by transaction posting (CBTRN02C) and interest calculation (CBACT04C)
- Cross-referenced to cards via CVACT03Y

---

## 2. Card Master (`CVACT02Y`) — 150 bytes

**VSAM File:** CARDFILE (KSDS) | **Key:** Card Number (16 digits)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `CARD-NUM` | `PIC X(16)` | Alpha | 16 | Credit card number (primary key) |
| 2 | `CARD-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Parent account ID (FK to Account) |
| 3 | `CARD-CVV-CD` | `PIC 9(03)` | Numeric | 3 | Card verification value |
| 4 | `CARD-EMBOSSED-NAME` | `PIC X(50)` | Alpha | 50 | Name embossed on card |
| 5 | `CARD-EXPIRAION-DATE` | `PIC X(10)` | Alpha | 10 | Card expiration date |
| 6 | `CARD-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 | Card status (Y=Active, N=Inactive) |
| 7 | `FILLER` | `PIC X(59)` | -- | 59 | Reserved space |

**Business Rules:**
- Multiple cards can belong to one account
- Card number is the lookup key for transaction processing
- Status must be Active for transaction authorization

---

## 3. Card Cross-Reference (`CVACT03Y`)

**VSAM File:** XREFFILE (KSDS) | **Key:** Card Number (16 digits)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `XREF-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card number (primary key) |
| 2 | `XREF-CUST-NUM` | `PIC 9(09)` | Numeric | 9 | Customer ID (FK to Customer) |
| 3 | `XREF-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Account ID (FK to Account) |

**Business Rules:**
- Central lookup table linking Card → Customer → Account
- Used by nearly every program for entity resolution
- Critical for transaction validation (card → account mapping)

---

## 4. Customer Master (`CVCUS01Y`) — 500 bytes

**VSAM File:** CUSTFILE (KSDS) | **Key:** Customer ID (9 digits)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `CUST-ID` | `PIC 9(09)` | Numeric | 9 | Unique customer identifier |
| 2 | `CUST-FIRST-NAME` | `PIC X(25)` | Alpha | 25 | Customer first name |
| 3 | `CUST-MIDDLE-NAME` | `PIC X(25)` | Alpha | 25 | Customer middle name |
| 4 | `CUST-LAST-NAME` | `PIC X(25)` | Alpha | 25 | Customer last name |
| 5 | `CUST-ADDR-LINE-1` | `PIC X(50)` | Alpha | 50 | Address line 1 |
| 6 | `CUST-ADDR-LINE-2` | `PIC X(50)` | Alpha | 50 | Address line 2 |
| 7 | `CUST-ADDR-LINE-3` | `PIC X(50)` | Alpha | 50 | Address line 3 |
| 8 | `CUST-ADDR-STATE-CD` | `PIC X(02)` | Alpha | 2 | US state code |
| 9 | `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | Alpha | 3 | Country code |
| 10 | `CUST-ADDR-ZIP` | `PIC X(10)` | Alpha | 10 | ZIP / postal code |
| 11 | `CUST-PHONE-NUM-1` | `PIC X(15)` | Alpha | 15 | Primary phone number |
| 12 | `CUST-PHONE-NUM-2` | `PIC X(15)` | Alpha | 15 | Secondary phone number |
| 13 | `CUST-SSN` | `PIC 9(09)` | Numeric | 9 | Social Security Number (PII) |
| 14 | `CUST-GOVT-ISSUED-ID` | `PIC X(20)` | Alpha | 20 | Government-issued ID |
| 15 | `CUST-DOB-YYYYMMDD` | `PIC X(10)` | Alpha | 10 | Date of birth |
| 16 | `CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | Alpha | 10 | EFT/ACH account ID |
| 17 | `CUST-PRI-CARD-HOLDER-IND` | `PIC X(01)` | Alpha | 1 | Primary card holder indicator |
| 18 | `CUST-FICO-CREDIT-SCORE` | `PIC 9(03)` | Numeric | 3 | FICO credit score |
| 19 | `FILLER` | `PIC X(168)` | -- | 168 | Reserved space |

**Business Rules:**
- Contains PII data (SSN, DOB) — requires encryption in modernized system
- Linked to accounts via cross-reference table
- FICO score used for credit decisions
- Address validated against US state codes and ZIP code tables (CSLKPCDY)

---

## 5. Transaction Master (`CVTRA05Y`) — 350 bytes

**VSAM File:** TRANSACT (KSDS) | **Key:** Transaction ID (16 chars)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `TRAN-ID` | `PIC X(16)` | Alpha | 16 | Unique transaction identifier |
| 2 | `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type code (FK) |
| 3 | `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category code (FK) |
| 4 | `TRAN-SOURCE` | `PIC X(10)` | Alpha | 10 | Transaction source/channel |
| 5 | `TRAN-DESC` | `PIC X(100)` | Alpha | 100 | Transaction description |
| 6 | `TRAN-AMT` | `PIC S9(09)V99` | Decimal | 11.2 | Transaction amount (signed) |
| 7 | `TRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant identifier |
| 8 | `TRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant name |
| 9 | `TRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant city |
| 10 | `TRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP code |
| 11 | `TRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card number used |
| 12 | `TRAN-ORIG-TS` | `PIC X(26)` | Alpha | 26 | Original timestamp |
| 13 | `TRAN-PROC-TS` | `PIC X(26)` | Alpha | 26 | Processing timestamp |
| 14 | `FILLER` | `PIC X(20)` | -- | 20 | Reserved space |

**Business Rules:**
- Core financial record — audit trail for all card transactions
- Posted from daily transactions by CBTRN02C
- Amount is signed: positive = debit, negative = credit
- Timestamps in DB2 format (YYYY-MM-DD-HH.MM.SS.FFFFFF)

---

## 6. Daily Transaction (`CVTRA06Y`) — 350 bytes

**VSAM File:** DALYTRAN (ESDS) | **Key:** Sequential

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `DALYTRAN-ID` | `PIC X(16)` | Alpha | 16 | Daily transaction ID |
| 2 | `DALYTRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type code |
| 3 | `DALYTRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category code |
| 4 | `DALYTRAN-SOURCE` | `PIC X(10)` | Alpha | 10 | Transaction source |
| 5 | `DALYTRAN-DESC` | `PIC X(100)` | Alpha | 100 | Transaction description |
| 6 | `DALYTRAN-AMT` | `PIC S9(09)V99` | Decimal | 11.2 | Transaction amount |
| 7 | `DALYTRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant ID |
| 8 | `DALYTRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant name |
| 9 | `DALYTRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant city |
| 10 | `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP |
| 11 | `DALYTRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card number |
| 12 | `DALYTRAN-ORIG-TS` | `PIC X(26)` | Alpha | 26 | Original timestamp |
| 13 | `DALYTRAN-PROC-TS` | `PIC X(26)` | Alpha | 26 | Processing timestamp |
| 14 | `FILLER` | `PIC X(20)` | -- | 20 | Reserved space |

**Business Rules:**
- Staging area for incoming transactions (pre-posting)
- Same layout as Transaction Master — enables direct copy during posting
- Read by CBTRN01C (validation) and CBTRN02C (posting)
- Cleared after successful posting cycle

---

## 7. Transaction Category Balance (`CVTRA01Y`) — 50 bytes

**VSAM File:** TCATBALF (KSDS) | **Key:** Account ID + Type Code + Category Code

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `TRANCAT-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Account ID (part of key) |
| 2 | `TRANCAT-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type code (part of key) |
| 3 | `TRANCAT-CD` | `PIC 9(04)` | Numeric | 4 | Category code (part of key) |
| 4 | `TRAN-CAT-BAL` | `PIC S9(09)V99` | Decimal | 11.2 | Running balance for this category |
| 5 | `FILLER` | `PIC X(22)` | -- | 22 | Reserved space |

**Business Rules:**
- Tracks balance per account per transaction category
- Updated during transaction posting (CBTRN02C)
- Read during interest calculation (CBACT04C)

---

## 8. Disclosure Group (`CVTRA02Y`) — 50 bytes

**VSAM File:** DISCGRP (KSDS) | **Key:** Group ID + Type Code + Category Code

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `DIS-ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 | Disclosure group ID (part of key) |
| 2 | `DIS-TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type code (part of key) |
| 3 | `DIS-TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Category code (part of key) |
| 4 | `DIS-INT-RATE` | `PIC S9(04)V99` | Decimal | 6.2 | Interest rate for this group/category |
| 5 | `FILLER` | `PIC X(28)` | -- | 28 | Reserved space |

**Business Rules:**
- Reference table for interest rates by disclosure group
- Used by CBACT04C to look up applicable interest rate
- Linked to accounts via ACCT-GROUP-ID field in Account Master

---

## 9. Transaction Type (`CVTRA03Y`) — 60 bytes

**VSAM File:** TRANTYPE (KSDS) | **Key:** Transaction Type Code

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `TRAN-TYPE` | `PIC X(02)` | Alpha | 2 | Transaction type code (primary key) |
| 2 | `TRAN-TYPE-DESC` | `PIC X(50)` | Alpha | 50 | Type description (e.g., "Purchase", "Cash Advance") |
| 3 | `FILLER` | `PIC X(08)` | -- | 8 | Reserved space |

**Business Rules:**
- Reference/lookup table for transaction type codes
- Used in reporting (CBTRN03C) to decode type codes
- Also maintained via DB2 in optional module (COTRTLIC/COTRTUPC)

---

## 10. Transaction Category Type (`CVTRA04Y`) — 60 bytes

**VSAM File:** TRANCATG (KSDS) | **Key:** Type Code + Category Code

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type code (part of key) |
| 2 | `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Category code (part of key) |
| 3 | `TRAN-CAT-TYPE-DESC` | `PIC X(50)` | Alpha | 50 | Category description |
| 4 | `FILLER` | `PIC X(04)` | -- | 4 | Reserved space |

**Business Rules:**
- Sub-categorization of transaction types
- Used in transaction reporting for detailed breakdowns

---

## 11. User Security Record (`CSUSR01Y`) — 80 bytes

**VSAM File:** USRSEC (KSDS) | **Key:** User ID (8 chars)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `SEC-USR-ID` | `PIC X(08)` | Alpha | 8 | User login ID (primary key) |
| 2 | `SEC-USR-FNAME` | `PIC X(20)` | Alpha | 20 | User first name |
| 3 | `SEC-USR-LNAME` | `PIC X(20)` | Alpha | 20 | User last name |
| 4 | `SEC-USR-PWD` | `PIC X(08)` | Alpha | 8 | User password (plaintext) |
| 5 | `SEC-USR-TYPE` | `PIC X(01)` | Alpha | 1 | User type (A=Admin, U=Regular) |
| 6 | `SEC-USR-FILLER` | `PIC X(23)` | -- | 23 | Reserved space |

**Business Rules:**
- Authentication and authorization record
- Password stored in plaintext (security risk — must be hashed in modernized system)
- User type controls menu visibility: Admin sees COADM01C, Regular sees COMEN01C
- Default users: ADMIN001/PASSWORD (admin), USER0001/PASSWORD (regular)

---

## 12. Communication Area (`COCOM01Y`)

**Type:** Inter-program COMMAREA (passed via CICS XCTL/LINK)

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `CDEMO-FROM-TRANID` | `PIC X(04)` | Alpha | 4 | Source transaction ID |
| 2 | `CDEMO-FROM-PROGRAM` | `PIC X(08)` | Alpha | 8 | Source program name |
| 3 | `CDEMO-TO-TRANID` | `PIC X(04)` | Alpha | 4 | Target transaction ID |
| 4 | `CDEMO-TO-PROGRAM` | `PIC X(08)` | Alpha | 8 | Target program name |
| 5 | `CDEMO-USER-ID` | `PIC X(08)` | Alpha | 8 | Current user ID |
| 6 | `CDEMO-USER-TYPE` | `PIC X(01)` | Alpha | 1 | User type (A/U) |
| 7 | `CDEMO-PGM-CONTEXT` | `PIC 9(01)` | Numeric | 1 | Program context (0=Enter, 1=Reenter) |
| 8 | `CDEMO-CUST-ID` | `PIC 9(09)` | Numeric | 9 | Selected customer ID |
| 9 | `CDEMO-CUST-FNAME` | `PIC X(25)` | Alpha | 25 | Customer first name |
| 10 | `CDEMO-CUST-MNAME` | `PIC X(25)` | Alpha | 25 | Customer middle name |
| 11 | `CDEMO-CUST-LNAME` | `PIC X(25)` | Alpha | 25 | Customer last name |
| 12 | `CDEMO-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Selected account ID |
| 13 | `CDEMO-ACCT-STATUS` | `PIC X(01)` | Alpha | 1 | Account status |
| 14 | `CDEMO-CARD-NUM` | `PIC 9(16)` | Numeric | 16 | Selected card number |
| 15 | `CDEMO-LAST-MAP` | `PIC X(7)` | Alpha | 7 | Last displayed map name |
| 16 | `CDEMO-LAST-MAPSET` | `PIC X(7)` | Alpha | 7 | Last displayed mapset name |

**Business Rules:**
- Shared state between all online CICS programs
- Carries user identity, selected entity context, and navigation state
- Maps to HTTP session or JWT claims in modernized system

---

## 13. Statement Transaction Layout (`COSTM01`) — 355 bytes

**Type:** Altered transaction layout for statement generation

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `TRNX-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card number (part of key) |
| 2 | `TRNX-ID` | `PIC X(16)` | Alpha | 16 | Transaction ID (part of key) |
| 3 | `TRNX-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type |
| 4 | `TRNX-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Category code |
| 5 | `TRNX-SOURCE` | `PIC X(10)` | Alpha | 10 | Source channel |
| 6 | `TRNX-DESC` | `PIC X(100)` | Alpha | 100 | Description |
| 7 | `TRNX-AMT` | `PIC S9(09)V99` | Decimal | 11.2 | Amount |
| 8 | `TRNX-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant ID |
| 9 | `TRNX-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant name |
| 10 | `TRNX-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant city |
| 11 | `TRNX-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP |
| 12 | `TRNX-ORIG-TS` | `PIC X(26)` | Alpha | 26 | Original timestamp |
| 13 | `TRNX-PROC-TS` | `PIC X(26)` | Alpha | 26 | Processing timestamp |
| 14 | `FILLER` | `PIC X(20)` | -- | 20 | Reserved |

**Business Rules:**
- Re-keyed version of CVTRA05Y with composite key (Card + Transaction ID)
- Used exclusively by CBSTM03A for statement generation
- Allows sequential access by card number for grouped statements

---

## 14. Export/Import Record (`CVEXPORT`)

**Type:** Portable data exchange format

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `EXPORT-REC-TYPE` | `PIC X(01)` | Alpha | 1 | Record type (C=Customer, A=Account, X=Xref, T=Transaction, D=Card) |
| 2 | `EXPORT-DATA` | `PIC X(...)` | Alpha | Variable | Record data (format depends on type) |

**Business Rules:**
- Multiplexed flat file format for bulk data export/import
- Record type field determines which entity layout applies
- Used by CBEXPORT (write) and CBIMPORT (read/validate)

---

## 15. Date Conversion Record (`CODATECN`)

**Type:** Parameter area for date formatting

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `CODATECN-TYPE` | `PIC X` | Alpha | 1 | Input format (1=YYYYMMDD, 2=YYYY-MM-DD) |
| 2 | `CODATECN-INP-DATE` | `PIC X(20)` | Alpha | 20 | Input date value |
| 3 | `CODATECN-OUTTYPE` | `PIC X` | Alpha | 1 | Output format (1=YYYY-MM-DD, 2=YYYYMMDD) |
| 4 | `CODATECN-0UT-DATE` | `PIC X(20)` | Alpha | 20 | Output date value |
| 5 | `CODATECN-ERROR-MSG` | `PIC X(38)` | Alpha | 38 | Error message if conversion fails |

**Business Rules:**
- Bidirectional date format converter
- Used by assembler program COBDATFT for formatted account date display
- Supports YYYYMMDD ↔ YYYY-MM-DD conversions

---

## 16. Transaction Report Layout (`CVTRA07Y`)

**Type:** Print/report formatting layout

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|-----------|-----------|------|--------|---------------------|
| 1 | `REPT-SHORT-NAME` | `PIC X(38)` | Alpha | 38 | Report short name ("DALYREPT") |
| 2 | `REPT-LONG-NAME` | `PIC X(41)` | Alpha | 41 | Report title |
| 3 | `REPT-START-DATE` | `PIC X(10)` | Alpha | 10 | Report date range start |
| 4 | `REPT-END-DATE` | `PIC X(10)` | Alpha | 10 | Report date range end |
| 5 | `TRAN-REPORT-TRANS-ID` | `PIC X(16)` | Alpha | 16 | Transaction ID (detail line) |
| 6 | `TRAN-REPORT-ACCOUNT-ID` | `PIC X(11)` | Alpha | 11 | Account ID (detail line) |
| 7 | `TRAN-REPORT-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type code |
| 8 | `TRAN-REPORT-TYPE-DESC` | `PIC X(15)` | Alpha | 15 | Type description |
| 9 | `TRAN-REPORT-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Category code |
| 10 | `TRAN-REPORT-CAT-DESC` | `PIC X(29)` | Alpha | 29 | Category description |
| 11 | `TRAN-REPORT-SOURCE` | `PIC X(10)` | Alpha | 10 | Transaction source |
| 12 | `TRAN-REPORT-AMT` | `PIC -ZZZ,ZZZ,ZZZ.ZZ` | Edited | 16 | Formatted amount |
| 13 | `REPT-PAGE-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Edited | 16 | Page subtotal |
| 14 | `REPT-ACCOUNT-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Edited | 16 | Account subtotal |
| 15 | `REPT-GRAND-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Edited | 16 | Grand total |

**Business Rules:**
- Used by CBTRN03C for daily transaction report generation
- Contains header, detail, and total lines
- Formatted for 133-column print output

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)
  └──< Card Cross-Reference (CVACT03Y) [via CUST-NUM]
         ├── Card (CVACT02Y) [via CARD-NUM]
         └── Account (CVACT01Y) [via ACCT-ID]
               ├──< Transaction (CVTRA05Y) [via card lookup]
               ├──< Daily Transaction (CVTRA06Y) [staging]
               ├──< Category Balance (CVTRA01Y) [via ACCT-ID]
               └──> Disclosure Group (CVTRA02Y) [via GROUP-ID]
                     └── Interest Rate

Transaction Type (CVTRA03Y) ──< Transaction Category (CVTRA04Y)
                                  └──> Category Balance (CVTRA01Y)

User Security (CSUSR01Y) [independent entity]
```

---

## Modernization Mapping

| COBOL Entity | Recommended Java Class | Database Table | Notes |
|-------------|----------------------|---------------|-------|
| CVACT01Y | `Account.java` | `accounts` | Add audit columns, UUID PK |
| CVACT02Y | `CreditCard.java` | `credit_cards` | Encrypt CVV, add card_id PK |
| CVACT03Y | `CardCrossReference.java` | `card_cross_references` | May merge into cards table |
| CVCUS01Y | `Customer.java` | `customers` | Encrypt SSN, hash sensitive fields |
| CVTRA05Y | `Transaction.java` | `transactions` | Add auto-increment PK |
| CVTRA06Y | `DailyTransaction.java` | `daily_transactions` | Same schema as transactions |
| CVTRA01Y | `CategoryBalance.java` | `category_balances` | Composite PK |
| CVTRA02Y | `DisclosureGroup.java` | `disclosure_groups` | Reference table |
| CVTRA03Y | `TransactionType.java` | `transaction_types` | Reference table |
| CVTRA04Y | `TransactionCategory.java` | `transaction_categories` | Reference table |
| CSUSR01Y | `User.java` | `users` | Hash passwords, add roles table |
| COCOM01Y | `SessionContext.java` | *(HTTP session)* | JWT or session-based auth |
