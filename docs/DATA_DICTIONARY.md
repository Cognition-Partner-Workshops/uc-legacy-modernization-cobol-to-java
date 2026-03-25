# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Source**: COBOL Copybook PIC Clause Analysis
> **Application**: CardDemo — Mainframe Credit Card Management System

---

## Overview

This data dictionary extracts all business entities from the CardDemo COBOL copybooks, translating mainframe PIC clauses into business-friendly field definitions. Each entity maps to a VSAM KSDS file on the mainframe and will correspond to a relational database table in the modernized Java application.

### PIC Clause Reference

| COBOL PIC | Meaning | Java Equivalent |
|-----------|---------|----------------|
| `PIC X(n)` | Alphanumeric, n characters | `String` |
| `PIC 9(n)` | Unsigned numeric, n digits | `int` / `long` |
| `PIC S9(n)V99` | Signed decimal with 2 implied decimals | `BigDecimal` |
| `PIC 9(n) COMP` | Binary numeric | `int` / `long` |
| `FILLER` | Reserved/padding bytes | (not mapped) |

---

## 1. Account Master — `CVACT01Y`

**Copybook**: `app/cpy/CVACT01Y.cpy` | **Record Length**: 300 bytes
**VSAM File**: `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | **Key**: Account ID (11 bytes, pos 0)

| # | COBOL Field | PIC Clause | Offset | Len | Business Name | Business Description | Java Type |
|---|------------|-----------|--------|-----|--------------|---------------------|-----------|
| 1 | `ACCT-ID` | `PIC 9(11)` | 0 | 11 | Account ID | Unique account identifier | `long` |
| 2 | `ACCT-ACTIVE-STATUS` | `PIC X(01)` | 11 | 1 | Active Status | Account active flag (Y/N) | `String` |
| 3 | `ACCT-CURR-BAL` | `PIC S9(10)V99` | 12 | 12 | Current Balance | Current account balance | `BigDecimal` |
| 4 | `ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | 24 | 12 | Credit Limit | Maximum credit allowed | `BigDecimal` |
| 5 | `ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | 36 | 12 | Cash Credit Limit | Maximum cash advance allowed | `BigDecimal` |
| 6 | `ACCT-OPEN-DATE` | `PIC X(10)` | 48 | 10 | Open Date | Date account was opened | `LocalDate` |
| 7 | `ACCT-EXPIRAION-DATE` | `PIC X(10)` | 58 | 10 | Expiration Date | Account expiration date | `LocalDate` |
| 8 | `ACCT-REISSUE-DATE` | `PIC X(10)` | 68 | 10 | Reissue Date | Card reissue date | `LocalDate` |
| 9 | `ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | 78 | 12 | Current Cycle Credit | Credits in current billing cycle | `BigDecimal` |
| 10 | `ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | 90 | 12 | Current Cycle Debit | Debits in current billing cycle | `BigDecimal` |
| 11 | `ACCT-ADDR-ZIP` | `PIC X(10)` | 102 | 10 | ZIP Code | Account holder ZIP code | `String` |
| 12 | `ACCT-GROUP-ID` | `PIC X(10)` | 112 | 10 | Group ID | Account group/portfolio ID | `String` |
| 13 | `FILLER` | `PIC X(178)` | 122 | 178 | (Reserved) | Padding to 300 bytes | — |

---

## 2. Card Data — `CVACT02Y`

**Copybook**: `app/cpy/CVACT02Y.cpy` | **Record Length**: 150 bytes
**VSAM File**: `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` | **Key**: Card Number (16 bytes, pos 0)

| # | COBOL Field | PIC Clause | Offset | Len | Business Name | Business Description | Java Type |
|---|------------|-----------|--------|-----|--------------|---------------------|-----------|
| 1 | `CARD-NUM` | `PIC X(16)` | 0 | 16 | Card Number | 16-digit credit card number | `String` |
| 2 | `CARD-ACCT-ID` | `PIC 9(11)` | 16 | 11 | Account ID | Linked account identifier | `long` |
| 3 | `CARD-CVV-CD` | `PIC 9(03)` | 27 | 3 | CVV Code | Card verification value | `String` |
| 4 | `CARD-EMBOSSED-NAME` | `PIC X(50)` | 30 | 50 | Embossed Name | Name printed on card | `String` |
| 5 | `CARD-EXPIRAION-DATE` | `PIC X(10)` | 80 | 10 | Expiration Date | Card expiry date | `LocalDate` |
| 6 | `CARD-ACTIVE-STATUS` | `PIC X(01)` | 90 | 1 | Active Status | Card active flag (Y/N) | `String` |
| 7 | `FILLER` | `PIC X(59)` | 91 | 59 | (Reserved) | Padding to 150 bytes | — |

---

## 3. Customer Profile — `CVCUS01Y`

**Copybook**: `app/cpy/CVCUS01Y.cpy` | **Record Length**: 500 bytes
**VSAM File**: `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` | **Key**: Customer ID (9 bytes, pos 0)

| # | COBOL Field | PIC Clause | Offset | Len | Business Name | Business Description | Java Type |
|---|------------|-----------|--------|-----|--------------|---------------------|-----------|
| 1 | `CUST-ID` | `PIC 9(09)` | 0 | 9 | Customer ID | Unique customer identifier | `long` |
| 2 | `CUST-FIRST-NAME` | `PIC X(25)` | 9 | 25 | First Name | Customer first name | `String` |
| 3 | `CUST-MIDDLE-NAME` | `PIC X(25)` | 34 | 25 | Middle Name | Customer middle name | `String` |
| 4 | `CUST-LAST-NAME` | `PIC X(25)` | 59 | 25 | Last Name | Customer last name | `String` |
| 5 | `CUST-ADDR-LINE-1` | `PIC X(50)` | 84 | 50 | Address Line 1 | Street address line 1 | `String` |
| 6 | `CUST-ADDR-LINE-2` | `PIC X(50)` | 134 | 50 | Address Line 2 | Street address line 2 | `String` |
| 7 | `CUST-ADDR-LINE-3` | `PIC X(50)` | 184 | 50 | Address Line 3 | Street address line 3 | `String` |
| 8 | `CUST-ADDR-STATE-CD` | `PIC X(02)` | 234 | 2 | State Code | Two-letter US state code | `String` |
| 9 | `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | 236 | 3 | Country Code | Three-letter country code | `String` |
| 10 | `CUST-ADDR-ZIP` | `PIC X(10)` | 239 | 10 | ZIP Code | Postal/ZIP code | `String` |
| 11 | `CUST-PHONE-NUM-1` | `PIC X(15)` | 249 | 15 | Primary Phone | Primary phone number | `String` |
| 12 | `CUST-PHONE-NUM-2` | `PIC X(15)` | 264 | 15 | Secondary Phone | Secondary phone number | `String` |
| 13 | `CUST-SSN` | `PIC 9(09)` | 279 | 9 | SSN | Social Security Number | `String` |
| 14 | `CUST-GOVT-ISSUED-ID` | `PIC X(20)` | 288 | 20 | Government ID | Government-issued ID number | `String` |
| 15 | `CUST-DOB-YYYYMMDD` | `PIC X(10)` | 308 | 10 | Date of Birth | Date of birth (YYYY-MM-DD) | `LocalDate` |
| 16 | `CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | 318 | 10 | EFT Account ID | Electronic fund transfer account | `String` |
| 17 | `CUST-PRI-CARD-HOLDER-IND` | `PIC X(01)` | 328 | 1 | Primary Holder | Primary cardholder indicator (Y/N) | `String` |
| 18 | `CUST-FICO-CREDIT-SCORE` | `PIC 9(03)` | 329 | 3 | FICO Score | Customer credit score | `int` |
| 19 | `FILLER` | `PIC X(168)` | 332 | 168 | (Reserved) | Padding to 500 bytes | — |

---

## 4. Card Cross-Reference — `CVACT03Y`

**Copybook**: `app/cpy/CVACT03Y.cpy` | **Record Length**: 50 bytes
**VSAM File**: `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | **Key**: Card Number (16 bytes, pos 0)
**Alternate Index**: Account ID (11 bytes, pos 25) — non-unique

| # | COBOL Field | PIC Clause | Offset | Len | Business Name | Business Description | Java Type |
|---|------------|-----------|--------|-----|--------------|---------------------|-----------|
| 1 | `XREF-CARD-NUM` | `PIC X(16)` | 0 | 16 | Card Number | Card number (primary key) | `String` |
| 2 | `XREF-CUST-ID` | `PIC 9(09)` | 16 | 9 | Customer ID | Owning customer ID | `long` |
| 3 | `XREF-ACCT-ID` | `PIC 9(11)` | 25 | 11 | Account ID | Linked account ID (AIX key) | `long` |
| 4 | `FILLER` | `PIC X(14)` | 36 | 14 | (Reserved) | Padding to 50 bytes | — |

---

## 5. Transaction Record — `CVTRA05Y`

**Copybook**: `app/cpy/CVTRA05Y.cpy` | **Record Length**: 350 bytes
**VSAM File**: `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | **Key**: Transaction ID (16 bytes, pos 0)

| # | COBOL Field | PIC Clause | Offset | Len | Business Name | Business Description | Java Type |
|---|------------|-----------|--------|-----|--------------|---------------------|-----------|
| 1 | `TRAN-ID` | `PIC X(16)` | 0 | 16 | Transaction ID | Unique transaction identifier | `String` |
| 2 | `TRAN-TYPE-CD` | `PIC X(02)` | 16 | 2 | Type Code | Transaction type code (FK to CVTRA03Y) | `String` |
| 3 | `TRAN-CAT-CD` | `PIC 9(04)` | 18 | 4 | Category Code | Transaction category code (FK to CVTRA04Y) | `int` |
| 4 | `TRAN-SOURCE` | `PIC X(10)` | 22 | 10 | Source | Transaction source (online/batch/etc.) | `String` |
| 5 | `TRAN-DESC` | `PIC X(100)` | 32 | 100 | Description | Transaction description | `String` |
| 6 | `TRAN-AMT` | `PIC S9(09)V99` | 132 | 11 | Amount | Transaction amount | `BigDecimal` |
| 7 | `TRAN-MERCHANT-ID` | `PIC 9(09)` | 143 | 9 | Merchant ID | Merchant identifier | `long` |
| 8 | `TRAN-MERCHANT-NAME` | `PIC X(50)` | 152 | 50 | Merchant Name | Merchant business name | `String` |
| 9 | `TRAN-MERCHANT-CITY` | `PIC X(50)` | 202 | 50 | Merchant City | Merchant city | `String` |
| 10 | `TRAN-MERCHANT-ZIP` | `PIC X(10)` | 252 | 10 | Merchant ZIP | Merchant postal code | `String` |
| 11 | `TRAN-CARD-NUM` | `PIC X(16)` | 262 | 16 | Card Number | Card used for transaction | `String` |
| 12 | `TRAN-ORIG-TS` | `PIC X(26)` | 278 | 26 | Origination Timestamp | When transaction originated | `Instant` |
| 13 | `TRAN-PROC-TS` | `PIC X(26)` | 304 | 26 | Processing Timestamp | When transaction was processed | `Instant` |
| 14 | `FILLER` | `PIC X(20)` | 330 | 20 | (Reserved) | Padding to 350 bytes | — |

---

## 6. Daily Transaction — `CVTRA06Y`

**Copybook**: `app/cpy/CVTRA06Y.cpy` | **Record Length**: 350 bytes
**VSAM File**: `AWS.M2.CARDDEMO.DALYTRAN.PS` | **Organization**: Sequential

| # | COBOL Field | PIC Clause | Offset | Len | Business Name | Business Description | Java Type |
|---|------------|-----------|--------|-----|--------------|---------------------|-----------|
| 1 | `DALYTRAN-ID` | `PIC X(16)` | 0 | 16 | Transaction ID | Daily transaction identifier | `String` |
| 2 | `DALYTRAN-TYPE-CD` | `PIC X(02)` | 16 | 2 | Type Code | Transaction type code | `String` |
| 3 | `DALYTRAN-CAT-CD` | `PIC 9(04)` | 18 | 4 | Category Code | Transaction category code | `int` |
| 4 | `DALYTRAN-SOURCE` | `PIC X(10)` | 22 | 10 | Source | Transaction source | `String` |
| 5 | `DALYTRAN-DESC` | `PIC X(100)` | 32 | 100 | Description | Transaction description | `String` |
| 6 | `DALYTRAN-AMT` | `PIC S9(09)V99` | 132 | 11 | Amount | Transaction amount | `BigDecimal` |
| 7 | `DALYTRAN-MERCHANT-ID` | `PIC 9(09)` | 143 | 9 | Merchant ID | Merchant identifier | `long` |
| 8 | `DALYTRAN-MERCHANT-NAME` | `PIC X(50)` | 152 | 50 | Merchant Name | Merchant business name | `String` |
| 9 | `DALYTRAN-MERCHANT-CITY` | `PIC X(50)` | 202 | 50 | Merchant City | Merchant city | `String` |
| 10 | `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)` | 252 | 10 | Merchant ZIP | Merchant postal code | `String` |
| 11 | `DALYTRAN-CARD-NUM` | `PIC X(16)` | 262 | 16 | Card Number | Card used for transaction | `String` |
| 12 | `DALYTRAN-ORIG-TS` | `PIC X(26)` | 278 | 26 | Origination Timestamp | When transaction originated | `Instant` |
| 13 | `DALYTRAN-PROC-TS` | `PIC X(26)` | 304 | 26 | Processing Timestamp | When transaction was processed | `Instant` |
| 14 | `FILLER` | `PIC X(20)` | 330 | 20 | (Reserved) | Padding to 350 bytes | — |

---

## 7. Transaction Category Balance — `CVTRA01Y`

**Copybook**: `app/cpy/CVTRA01Y.cpy` | **Record Length**: 50 bytes
**VSAM File**: `AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS` | **Key**: Composite (Account ID + Type Code + Category Code)

| # | COBOL Field | PIC Clause | Offset | Len | Business Name | Business Description | Java Type |
|---|------------|-----------|--------|-----|--------------|---------------------|-----------|
| 1 | `TRANCAT-ACCT-ID` | `PIC 9(11)` | 0 | 11 | Account ID | Account identifier | `long` |
| 2 | `TRANCAT-TYPE-CD` | `PIC X(02)` | 11 | 2 | Type Code | Transaction type code | `String` |
| 3 | `TRANCAT-CD` | `PIC 9(04)` | 13 | 4 | Category Code | Transaction category code | `int` |
| 4 | `TRAN-CAT-BAL` | `PIC S9(09)V99` | 17 | 11 | Category Balance | Running balance for this category | `BigDecimal` |
| 5 | `FILLER` | `PIC X(22)` | 28 | 22 | (Reserved) | Padding to 50 bytes | — |

---

## 8. Disclosure Group — `CVTRA02Y`

**Copybook**: `app/cpy/CVTRA02Y.cpy` | **Record Length**: 50 bytes
**VSAM File**: `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` | **Key**: Composite (Group ID + Type + Category)

| # | COBOL Field | PIC Clause | Offset | Len | Business Name | Business Description | Java Type |
|---|------------|-----------|--------|-----|--------------|---------------------|-----------|
| 1 | `DIS-ACCT-GROUP-ID` | `PIC X(10)` | 0 | 10 | Account Group ID | Disclosure group identifier | `String` |
| 2 | `DIS-TRAN-TYPE-CD` | `PIC X(02)` | 10 | 2 | Type Code | Transaction type code | `String` |
| 3 | `DIS-TRAN-CAT-CD` | `PIC 9(04)` | 12 | 4 | Category Code | Transaction category code | `int` |
| 4 | `DIS-INT-RATE` | `PIC S9(04)V99` | 16 | 6 | Interest Rate | Interest rate for this group/category | `BigDecimal` |
| 5 | `FILLER` | `PIC X(28)` | 22 | 28 | (Reserved) | Padding to 50 bytes | — |

---

## 9. Transaction Type — `CVTRA03Y`

**Copybook**: `app/cpy/CVTRA03Y.cpy` | **Record Length**: 60 bytes
**VSAM File**: `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` | **Key**: Transaction Type (2 bytes, pos 0)

| # | COBOL Field | PIC Clause | Offset | Len | Business Name | Business Description | Java Type |
|---|------------|-----------|--------|-----|--------------|---------------------|-----------|
| 1 | `TRAN-TYPE` | `PIC X(02)` | 0 | 2 | Type Code | Transaction type code (PK) | `String` |
| 2 | `TRAN-TYPE-DESC` | `PIC X(50)` | 2 | 50 | Type Description | Human-readable type name | `String` |
| 3 | `FILLER` | `PIC X(08)` | 52 | 8 | (Reserved) | Padding to 60 bytes | — |

---

## 10. Transaction Category — `CVTRA04Y`

**Copybook**: `app/cpy/CVTRA04Y.cpy` | **Record Length**: 60 bytes
**VSAM File**: `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` | **Key**: Composite (Type Code + Category Code)

| # | COBOL Field | PIC Clause | Offset | Len | Business Name | Business Description | Java Type |
|---|------------|-----------|--------|-----|--------------|---------------------|-----------|
| 1 | `TRAN-TYPE-CD` | `PIC X(02)` | 0 | 2 | Type Code | Transaction type code (part of key) | `String` |
| 2 | `TRAN-CAT-CD` | `PIC 9(04)` | 2 | 4 | Category Code | Transaction category code (part of key) | `int` |
| 3 | `TRAN-CAT-TYPE-DESC` | `PIC X(50)` | 6 | 50 | Category Description | Human-readable category name | `String` |
| 4 | `FILLER` | `PIC X(04)` | 56 | 4 | (Reserved) | Padding to 60 bytes | — |

---

## 11. User Security Record — `CSUSR01Y`

**Copybook**: `app/cpy/CSUSR01Y.cpy` | **Record Length**: 80 bytes
**VSAM File**: `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` | **Key**: User ID (8 bytes, pos 0)

| # | COBOL Field | PIC Clause | Offset | Len | Business Name | Business Description | Java Type |
|---|------------|-----------|--------|-----|--------------|---------------------|-----------|
| 1 | `SEC-USR-ID` | `PIC X(08)` | 0 | 8 | User ID | Login user identifier | `String` |
| 2 | `SEC-USR-FNAME` | `PIC X(20)` | 8 | 20 | First Name | User first name | `String` |
| 3 | `SEC-USR-LNAME` | `PIC X(20)` | 28 | 20 | Last Name | User last name | `String` |
| 4 | `SEC-USR-PWD` | `PIC X(08)` | 48 | 8 | Password | User password (plaintext) | `String` |
| 5 | `SEC-USR-TYPE` | `PIC X(01)` | 56 | 1 | User Type | A=Admin, U=Regular User | `String` |
| 6 | `SEC-USR-FILLER` | `PIC X(23)` | 57 | 23 | (Reserved) | Padding to 80 bytes | — |

---

## 12. Common Communication Area — `COCOM01Y`

**Copybook**: `app/cpy/COCOM01Y.cpy` | **Usage**: CICS COMMAREA passed between programs

| # | COBOL Field | PIC Clause | Len | Business Name | Business Description |
|---|------------|-----------|-----|--------------|---------------------|
| 1 | `CDEMO-FROM-TRANID` | `PIC X(04)` | 4 | Source Transaction | Calling CICS transaction ID |
| 2 | `CDEMO-FROM-PROGRAM` | `PIC X(08)` | 8 | Source Program | Calling program name |
| 3 | `CDEMO-TO-TRANID` | `PIC X(04)` | 4 | Target Transaction | Target CICS transaction ID |
| 4 | `CDEMO-TO-PROGRAM` | `PIC X(08)` | 8 | Target Program | Target program name |
| 5 | `CDEMO-USER-ID` | `PIC X(08)` | 8 | User ID | Authenticated user ID |
| 6 | `CDEMO-USER-TYPE` | `PIC X(01)` | 1 | User Type | A=Admin, U=User |
| 7 | `CDEMO-PGM-CONTEXT` | `PIC 9(01)` | 1 | Program Context | Context flag for program flow |
| 8 | `CDEMO-ACCT-ID` | `PIC 9(11)` | 11 | Account ID | Current account in context |
| 9 | `CDEMO-CARD-NUM` | `PIC X(16)` | 16 | Card Number | Current card in context |
| 10 | `CDEMO-CUST-ID` | `PIC 9(09)` | 9 | Customer ID | Current customer in context |
| 11 | `CDEMO-LAST-MAP` | `PIC X(07)` | 7 | Last Map | Last BMS map displayed |
| 12 | `CDEMO-LAST-MAPSET` | `PIC X(07)` | 7 | Last Mapset | Last BMS mapset |

---

## 13. Transaction Report Layout — `CVTRA07Y`

**Copybook**: `app/cpy/CVTRA07Y.cpy` | **Usage**: Report formatting structures

### Report Header
| # | COBOL Field | PIC Clause | Business Name |
|---|------------|-----------|--------------|
| 1 | `REPT-SHORT-NAME` | `PIC X(38)` | Report Short Name ('DALYREPT') |
| 2 | `REPT-LONG-NAME` | `PIC X(41)` | Report Title ('Daily Transaction Report') |
| 3 | `REPT-START-DATE` | `PIC X(10)` | Report Start Date |
| 4 | `REPT-END-DATE` | `PIC X(10)` | Report End Date |

### Transaction Detail Line
| # | COBOL Field | PIC Clause | Business Name |
|---|------------|-----------|--------------|
| 1 | `TRAN-REPORT-TRANS-ID` | `PIC X(16)` | Transaction ID |
| 2 | `TRAN-REPORT-ACCOUNT-ID` | `PIC X(11)` | Account ID |
| 3 | `TRAN-REPORT-TYPE-CD` | `PIC X(02)` | Type Code |
| 4 | `TRAN-REPORT-TYPE-DESC` | `PIC X(15)` | Type Description |
| 5 | `TRAN-REPORT-CAT-CD` | `PIC 9(04)` | Category Code |
| 6 | `TRAN-REPORT-CAT-DESC` | `PIC X(29)` | Category Description |
| 7 | `TRAN-REPORT-SOURCE` | `PIC X(10)` | Transaction Source |
| 8 | `TRAN-REPORT-AMT` | `PIC -ZZZ,ZZZ,ZZZ.ZZ` | Transaction Amount |

### Report Totals
| # | COBOL Field | PIC Clause | Business Name |
|---|------------|-----------|--------------|
| 1 | `REPT-PAGE-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Page Total |
| 2 | `REPT-ACCOUNT-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Account Total |
| 3 | `REPT-GRAND-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Grand Total |

---

## 14. Statement Transaction Layout — `COSTM01`

**Copybook**: `app/cpy/COSTM01.CPY` | **Record Length**: 350 bytes
**Usage**: Altered layout with composite key (Card + Transaction ID) for statement generation

| # | COBOL Field | PIC Clause | Offset | Len | Business Name | Java Type |
|---|------------|-----------|--------|-----|--------------|-----------|
| 1 | `TRNX-CARD-NUM` | `PIC X(16)` | 0 | 16 | Card Number (part of key) | `String` |
| 2 | `TRNX-ID` | `PIC X(16)` | 16 | 16 | Transaction ID (part of key) | `String` |
| 3 | `TRNX-TYPE-CD` | `PIC X(02)` | 32 | 2 | Type Code | `String` |
| 4 | `TRNX-CAT-CD` | `PIC 9(04)` | 34 | 4 | Category Code | `int` |
| 5 | `TRNX-SOURCE` | `PIC X(10)` | 38 | 10 | Source | `String` |
| 6 | `TRNX-DESC` | `PIC X(100)` | 48 | 100 | Description | `String` |
| 7 | `TRNX-AMT` | `PIC S9(09)V99` | 148 | 11 | Amount | `BigDecimal` |
| 8 | `TRNX-MERCHANT-ID` | `PIC 9(09)` | 159 | 9 | Merchant ID | `long` |
| 9 | `TRNX-MERCHANT-NAME` | `PIC X(50)` | 168 | 50 | Merchant Name | `String` |
| 10 | `TRNX-MERCHANT-CITY` | `PIC X(50)` | 218 | 50 | Merchant City | `String` |
| 11 | `TRNX-MERCHANT-ZIP` | `PIC X(10)` | 268 | 10 | Merchant ZIP | `String` |
| 12 | `TRNX-ORIG-TS` | `PIC X(26)` | 278 | 26 | Origination Timestamp | `Instant` |
| 13 | `TRNX-PROC-TS` | `PIC X(26)` | 304 | 26 | Processing Timestamp | `Instant` |
| 14 | `FILLER` | `PIC X(20)` | 330 | 20 | (Reserved) | — |

---

## 15. Export Record — `CVEXPORT`

**Copybook**: `app/cpy/CVEXPORT.cpy` | **Usage**: Multi-record export for data migration

### Export Header Record
| # | COBOL Field | PIC Clause | Len | Business Name |
|---|------------|-----------|-----|--------------|
| 1 | `EXP-HEADER-REC-TYPE` | `PIC X(02)` | 2 | Record Type ('HD') |
| 2 | `EXP-HEADER-VERSION` | `PIC X(05)` | 5 | Export Version |
| 3 | `EXP-HEADER-TIMESTAMP` | `PIC X(26)` | 26 | Export Timestamp |
| 4 | `EXP-HEADER-SOURCE-SYS` | `PIC X(08)` | 8 | Source System |
| 5 | `EXP-HEADER-TOTAL-RECS` | `PIC 9(09)` | 9 | Total Records |

### Export Customer Record
| # | COBOL Field | PIC Clause | Len | Business Name |
|---|------------|-----------|-----|--------------|
| 1 | `EXP-CUST-REC-TYPE` | `PIC X(02)` | 2 | Record Type ('CU') |
| 2 | `EXP-CUST-ID` | `PIC 9(09)` | 9 | Customer ID |
| 3 | `EXP-CUST-FIRST-NAME` | `PIC X(25)` | 25 | First Name |
| 4 | `EXP-CUST-LAST-NAME` | `PIC X(25)` | 25 | Last Name |
| 5 | `EXP-CUST-DOB` | `PIC X(10)` | 10 | Date of Birth |
| 6 | `EXP-CUST-SSN` | `PIC 9(09)` | 9 | SSN |
| 7 | `EXP-CUST-ADDR-LINE-1` | `PIC X(50)` | 50 | Address Line 1 |
| 8 | `EXP-CUST-STATE-CD` | `PIC X(02)` | 2 | State Code |
| 9 | `EXP-CUST-ZIP` | `PIC X(10)` | 10 | ZIP Code |
| 10 | `EXP-CUST-PHONE-1` | `PIC X(15)` | 15 | Phone Number |
| 11 | `EXP-CUST-FICO-SCORE` | `PIC 9(03)` | 3 | FICO Score |

### Export Account Record
| # | COBOL Field | PIC Clause | Len | Business Name |
|---|------------|-----------|-----|--------------|
| 1 | `EXP-ACCT-REC-TYPE` | `PIC X(02)` | 2 | Record Type ('AC') |
| 2 | `EXP-ACCT-ID` | `PIC 9(11)` | 11 | Account ID |
| 3 | `EXP-ACCT-CURR-BAL` | `PIC S9(10)V99` | 12 | Current Balance |
| 4 | `EXP-ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | 12 | Credit Limit |
| 5 | `EXP-ACCT-OPEN-DATE` | `PIC X(10)` | 10 | Open Date |
| 6 | `EXP-ACCT-STATUS` | `PIC X(01)` | 1 | Active Status |
| 7 | `EXP-ACCT-GROUP-ID` | `PIC X(10)` | 10 | Group ID |

### Export Card Record
| # | COBOL Field | PIC Clause | Len | Business Name |
|---|------------|-----------|-----|--------------|
| 1 | `EXP-CARD-REC-TYPE` | `PIC X(02)` | 2 | Record Type ('CD') |
| 2 | `EXP-CARD-NUM` | `PIC X(16)` | 16 | Card Number |
| 3 | `EXP-CARD-ACCT-ID` | `PIC 9(11) COMP` | 4 | Account ID |
| 4 | `EXP-CARD-CVV-CD` | `PIC 9(03) COMP` | 2 | CVV Code |
| 5 | `EXP-CARD-EMBOSSED-NAME` | `PIC X(50)` | 50 | Embossed Name |
| 6 | `EXP-CARD-EXPIRAION-DATE` | `PIC X(10)` | 10 | Expiration Date |
| 7 | `EXP-CARD-ACTIVE-STATUS` | `PIC X(01)` | 1 | Active Status |

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)
  |
  |-- 1:N --> Card Cross-Reference (CVACT03Y)
  |               |
  |               |-- N:1 --> Account (CVACT01Y)
  |               |               |
  |               |               |-- 1:N --> Trans Cat Balance (CVTRA01Y)
  |               |               |
  |               |               |-- N:1 --> Disclosure Group (CVTRA02Y)
  |               |
  |               |-- 1:1 --> Card Data (CVACT02Y)
  |                               |
  |                               |-- 1:N --> Transaction (CVTRA05Y)
  |                                               |
  |                                               |-- N:1 --> Transaction Type (CVTRA03Y)
  |                                               |
  |                                               |-- N:1 --> Transaction Category (CVTRA04Y)

User Security (CSUSR01Y) -- standalone authentication table

Daily Transaction (CVTRA06Y) -- staging table for batch posting
```

---

## VSAM File Summary

| Entity | VSAM Dataset | Type | Key | Record Len |
|--------|-------------|------|-----|-----------|
| Account | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` | KSDS | ACCT-ID (11) | 300 |
| Card | `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` | KSDS | CARD-NUM (16) | 150 |
| Customer | `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` | KSDS | CUST-ID (9) | 500 |
| Card XREF | `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` | KSDS + AIX | CARD-NUM (16) | 50 |
| Transaction | `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` | KSDS | TRAN-ID (16) | 350 |
| Daily Trans | `AWS.M2.CARDDEMO.DALYTRAN.PS` | Sequential | — | 350 |
| Cat Balance | `AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS` | KSDS | Composite (17) | 50 |
| Disclosure | `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` | KSDS | Composite (16) | 50 |
| Tran Type | `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` | KSDS | TRAN-TYPE (2) | 60 |
| Tran Category | `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` | KSDS | Composite (6) | 60 |
| User Security | `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` | KSDS | SEC-USR-ID (8) | 80 |
| Daily Rejects | `AWS.M2.CARDDEMO.DALYREJS.PS` | Sequential | — | 350 |
