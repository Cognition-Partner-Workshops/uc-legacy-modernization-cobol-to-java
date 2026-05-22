# Data Dictionary — CardDemo Mainframe System

> Generated: 2026-05-22 | Source: `uc-legacy-modernization-cobol-to-java`

## Overview

This dictionary catalogs every business entity extracted from the COBOL copybook PIC clauses. Each entity maps to a VSAM file or logical record structure. Field types follow standard COBOL conventions:

| PIC Pattern | Business Meaning | Java Equivalent |
|:------------|:-----------------|:----------------|
| `PIC 9(n)` | Unsigned integer | `long` / `int` |
| `PIC S9(n)V99` | Signed decimal (2 implied decimal places) | `BigDecimal` |
| `PIC S9(n) COMP-3` | Packed decimal | `BigDecimal` |
| `PIC X(n)` | Alphanumeric string | `String` |
| `PIC 9(n) COMP` | Binary integer | `int` / `long` |

---

## 1. Account (`CVACT01Y.cpy`)

**VSAM File:** `ACCTDAT` (KSDS) — Record Length: 300 bytes

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `ACCT-ID` | `9(11)` | Numeric | 11 | Account identifier (primary key) |
| `ACCT-ACTIVE-STATUS` | `X(01)` | Alpha | 1 | Account status flag (Y/N) |
| `ACCT-CURR-BAL` | `S9(10)V99` | Signed Decimal | 12 | Current account balance |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | Signed Decimal | 12 | Total credit limit |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | Signed Decimal | 12 | Cash advance credit limit |
| `ACCT-OPEN-DATE` | `X(10)` | Date | 10 | Account open date (YYYY-MM-DD) |
| `ACCT-EXPIRAION-DATE` | `X(10)` | Date | 10 | Account expiration date |
| `ACCT-REISSUE-DATE` | `X(10)` | Date | 10 | Last card reissue date |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | Signed Decimal | 12 | Current cycle credit total |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | Signed Decimal | 12 | Current cycle debit total |
| `ACCT-ADDR-ZIP` | `X(10)` | Alpha | 10 | Account holder zip code |
| `ACCT-GROUP-ID` | `X(10)` | Alpha | 10 | Disclosure group identifier |
| `FILLER` | `X(178)` | — | 178 | Reserved space |

---

## 2. Credit Card (`CVACT02Y.cpy`)

**VSAM File:** `CARDDAT` (KSDS) — Record Length: 150 bytes

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `CARD-NUM` | `X(16)` | Alpha | 16 | Credit card number (primary key) |
| `CARD-ACCT-ID` | `9(11)` | Numeric | 11 | Linked account ID (FK → Account) |
| `CARD-CVV-CD` | `9(03)` | Numeric | 3 | Card verification value |
| `CARD-EMBOSSED-NAME` | `X(50)` | Alpha | 50 | Cardholder name as embossed |
| `CARD-EXPIRAION-DATE` | `X(10)` | Date | 10 | Card expiration date |
| `CARD-ACTIVE-STATUS` | `X(01)` | Alpha | 1 | Active status flag (Y/N) |
| `FILLER` | `X(59)` | — | 59 | Reserved space |

---

## 3. Customer (`CVCUS01Y.cpy`)

**VSAM File:** `CUSTDAT` (KSDS) — Record Length: 500 bytes

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `CUST-ID` | `9(09)` | Numeric | 9 | Customer identifier (primary key) |
| `CUST-FIRST-NAME` | `X(25)` | Alpha | 25 | First name |
| `CUST-MIDDLE-NAME` | `X(25)` | Alpha | 25 | Middle name |
| `CUST-LAST-NAME` | `X(25)` | Alpha | 25 | Last name |
| `CUST-ADDR-LINE-1` | `X(50)` | Alpha | 50 | Address line 1 |
| `CUST-ADDR-LINE-2` | `X(50)` | Alpha | 50 | Address line 2 |
| `CUST-ADDR-LINE-3` | `X(50)` | Alpha | 50 | Address line 3 |
| `CUST-ADDR-STATE-CD` | `X(02)` | Alpha | 2 | US state code |
| `CUST-ADDR-COUNTRY-CD` | `X(03)` | Alpha | 3 | Country code |
| `CUST-ADDR-ZIP` | `X(10)` | Alpha | 10 | Zip/postal code |
| `CUST-PHONE-NUM-1` | `X(15)` | Alpha | 15 | Primary phone number |
| `CUST-PHONE-NUM-2` | `X(15)` | Alpha | 15 | Secondary phone number |
| `CUST-SSN` | `9(09)` | Numeric | 9 | Social security number (PII) |
| `CUST-GOVT-ISSUED-ID` | `X(20)` | Alpha | 20 | Government-issued ID |
| `CUST-DOB-YYYY-MM-DD` | `X(10)` | Date | 10 | Date of birth (PII) |
| `CUST-EFT-ACCOUNT-ID` | `X(10)` | Alpha | 10 | Electronic funds transfer account |
| `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | Alpha | 1 | Primary cardholder indicator |
| `CUST-FICO-CREDIT-SCORE` | `9(03)` | Numeric | 3 | FICO credit score |
| `FILLER` | `X(168)` | — | 168 | Reserved space |

---

## 4. Card Cross-Reference (`CVACT03Y.cpy`)

**VSAM File:** `CXREF` (KSDS) — Record Length: 50 bytes

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `XREF-CARD-NUM` | `X(16)` | Alpha | 16 | Card number (primary key) |
| `XREF-CUST-ID` | `9(09)` | Numeric | 9 | Customer ID (FK → Customer) |
| `XREF-ACCT-ID` | `9(11)` | Numeric | 11 | Account ID (FK → Account) |
| `FILLER` | `X(14)` | — | 14 | Reserved space |

**Relationships:** Links Card ↔ Customer ↔ Account (the central join entity).

---

## 5. Transaction (`CVTRA05Y.cpy`)

**VSAM File:** `TRANSACT` (KSDS with AIX) — Record Length: 350 bytes

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `TRAN-ID` | `X(16)` | Alpha | 16 | Transaction identifier |
| `TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type code (FK → Tran Type) |
| `TRAN-CAT-CD` | `9(04)` | Numeric | 4 | Transaction category code (FK → Tran Cat) |
| `TRAN-SOURCE` | `X(10)` | Alpha | 10 | Transaction source (online/batch/etc.) |
| `TRAN-DESC` | `X(100)` | Alpha | 100 | Transaction description |
| `TRAN-AMT` | `S9(09)V99` | Signed Decimal | 11 | Transaction amount |
| `TRAN-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant identifier |
| `TRAN-MERCHANT-NAME` | `X(50)` | Alpha | 50 | Merchant name |
| `TRAN-MERCHANT-CITY` | `X(50)` | Alpha | 50 | Merchant city |
| `TRAN-MERCHANT-ZIP` | `X(10)` | Alpha | 10 | Merchant zip code |
| `TRAN-CARD-NUM` | `X(16)` | Alpha | 16 | Card number (FK → Card) |
| `TRAN-ORIG-TS` | `X(26)` | Timestamp | 26 | Original transaction timestamp |
| `TRAN-PROC-TS` | `X(26)` | Timestamp | 26 | Processing timestamp |
| `FILLER` | `X(20)` | — | 20 | Reserved space |

---

## 6. Daily Transaction (`CVTRA06Y.cpy`)

**File:** `DALYTRAN` (Sequential PS) — Record Length: 350 bytes

Same layout as Transaction (CVTRA05Y) but with `DALYTRAN-` prefix. Used as batch input before posting to the TRANSACT VSAM.

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `DALYTRAN-ID` | `X(16)` | Alpha | 16 | Daily transaction identifier |
| `DALYTRAN-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type code |
| `DALYTRAN-CAT-CD` | `9(04)` | Numeric | 4 | Category code |
| `DALYTRAN-SOURCE` | `X(10)` | Alpha | 10 | Source |
| `DALYTRAN-DESC` | `X(100)` | Alpha | 100 | Description |
| `DALYTRAN-AMT` | `S9(09)V99` | Signed Decimal | 11 | Amount |
| `DALYTRAN-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant ID |
| `DALYTRAN-MERCHANT-NAME` | `X(50)` | Alpha | 50 | Merchant name |
| `DALYTRAN-MERCHANT-CITY` | `X(50)` | Alpha | 50 | Merchant city |
| `DALYTRAN-MERCHANT-ZIP` | `X(10)` | Alpha | 10 | Merchant zip |
| `DALYTRAN-CARD-NUM` | `X(16)` | Alpha | 16 | Card number |
| `DALYTRAN-ORIG-TS` | `X(26)` | Timestamp | 26 | Original timestamp |
| `DALYTRAN-PROC-TS` | `X(26)` | Timestamp | 26 | Processing timestamp |
| `FILLER` | `X(20)` | — | 20 | Reserved space |

---

## 7. Transaction Keyed Record (`COSTM01.CPY`)

**VSAM File:** `TRNXFILE` (KSDS, keyed by Card+Tran ID) — Record Length: 350 bytes

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `TRNX-CARD-NUM` | `X(16)` | Alpha | 16 | Card number (part of composite key) |
| `TRNX-ID` | `X(16)` | Alpha | 16 | Transaction ID (part of composite key) |
| `TRNX-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type code |
| `TRNX-CAT-CD` | `9(04)` | Numeric | 4 | Category code |
| `TRNX-SOURCE` | `X(10)` | Alpha | 10 | Source |
| `TRNX-DESC` | `X(100)` | Alpha | 100 | Description |
| `TRNX-AMT` | `S9(09)V99` | Signed Decimal | 11 | Amount |
| `TRNX-MERCHANT-ID` | `9(09)` | Numeric | 9 | Merchant ID |
| `TRNX-MERCHANT-NAME` | `X(50)` | Alpha | 50 | Merchant name |
| `TRNX-MERCHANT-CITY` | `X(50)` | Alpha | 50 | Merchant city |
| `TRNX-MERCHANT-ZIP` | `X(10)` | Alpha | 10 | Merchant zip |
| `TRNX-ORIG-TS` | `X(26)` | Timestamp | 26 | Original timestamp |
| `TRNX-PROC-TS` | `X(26)` | Timestamp | 26 | Processing timestamp |
| `FILLER` | `X(20)` | — | 20 | Reserved space |

---

## 8. Transaction Category Balance (`CVTRA01Y.cpy`)

**VSAM File:** `TCATBALF` (KSDS) — Record Length: 50 bytes

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `TRANCAT-ACCT-ID` | `9(11)` | Numeric | 11 | Account ID (composite key part 1) |
| `TRANCAT-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type (composite key part 2) |
| `TRANCAT-CD` | `9(04)` | Numeric | 4 | Category code (composite key part 3) |
| `TRAN-CAT-BAL` | `S9(09)V99` | Signed Decimal | 11 | Running category balance |
| `FILLER` | `X(22)` | — | 22 | Reserved space |

---

## 9. Disclosure Group (`CVTRA02Y.cpy`)

**VSAM File:** `DISCGRP` (KSDS) — Record Length: 50 bytes

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `DIS-ACCT-GROUP-ID` | `X(10)` | Alpha | 10 | Account group (composite key part 1) |
| `DIS-TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type (composite key part 2) |
| `DIS-TRAN-CAT-CD` | `9(04)` | Numeric | 4 | Category code (composite key part 3) |
| `DIS-INT-RATE` | `S9(04)V99` | Signed Decimal | 6 | Interest rate for this disclosure group |
| `FILLER` | `X(28)` | — | 28 | Reserved space |

---

## 10. Transaction Type (`CVTRA03Y.cpy`)

**VSAM File:** `TRANTYPE` (KSDS) — Record Length: 60 bytes

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `TRAN-TYPE` | `X(02)` | Alpha | 2 | Transaction type code (primary key) |
| `TRAN-TYPE-DESC` | `X(50)` | Alpha | 50 | Type description |
| `FILLER` | `X(08)` | — | 8 | Reserved space |

---

## 11. Transaction Category (`CVTRA04Y.cpy`)

**VSAM File:** `TRANCATG` (KSDS) — Record Length: 60 bytes

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `TRAN-TYPE-CD` | `X(02)` | Alpha | 2 | Transaction type (composite key part 1) |
| `TRAN-CAT-CD` | `9(04)` | Numeric | 4 | Category code (composite key part 2) |
| `TRAN-CAT-TYPE-DESC` | `X(50)` | Alpha | 50 | Category description |
| `FILLER` | `X(04)` | — | 4 | Reserved space |

---

## 12. User Security (`CSUSR01Y.cpy`)

**VSAM File:** `USRSEC` (KSDS) — Record Length: 80 bytes

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `SEC-USR-ID` | `X(08)` | Alpha | 8 | User ID (primary key) |
| `SEC-USR-FNAME` | `X(20)` | Alpha | 20 | First name |
| `SEC-USR-LNAME` | `X(20)` | Alpha | 20 | Last name |
| `SEC-USR-PWD` | `X(08)` | Alpha | 8 | Password (plaintext — security risk) |
| `SEC-USR-TYPE` | `X(01)` | Alpha | 1 | User type ('A' = Admin, 'U' = User) |
| `SEC-USR-FILLER` | `X(23)` | — | 23 | Reserved space |

---

## 13. Branch Migration Export Record (`CVEXPORT.cpy`)

**File:** `EXPORTFILE` (Sequential) — Record Length: ~505 bytes

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `EXPORT-REC-TYPE` | `X(1)` | Alpha | 1 | Record type indicator (C=Customer, A=Account, X=Xref, T=Transaction) |
| `EXPORT-TIMESTAMP` | `X(26)` | Timestamp | 26 | Export timestamp |
| `EXPORT-SEQUENCE-NUM` | `9(9) COMP` | Binary | 4 | Sequence number |
| `EXPORT-BRANCH-ID` | `X(4)` | Alpha | 4 | Source branch ID |
| `EXPORT-REGION-CODE` | `X(5)` | Alpha | 5 | Region code |
| `EXPORT-RECORD-DATA` | `X(460)` | Alpha | 460 | Payload (REDEFINES below) |

**REDEFINES — Customer payload (`EXPORT-CUSTOMER-DATA`):**

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `EXP-CUST-ID` | `9(09) COMP` | Binary | 4 | Customer ID |
| `EXP-CUST-FIRST-NAME` | `X(25)` | Alpha | 25 | First name |
| `EXP-CUST-MIDDLE-NAME` | `X(25)` | Alpha | 25 | Middle name |
| `EXP-CUST-LAST-NAME` | `X(25)` | Alpha | 25 | Last name |
| `EXP-CUST-ADDR-LINE` | `X(50)` | Alpha | 150 | Address lines (OCCURS 3) |
| `EXP-CUST-ADDR-STATE-CD` | `X(02)` | Alpha | 2 | State code |
| `EXP-CUST-ADDR-COUNTRY-CD` | `X(03)` | Alpha | 3 | Country code |
| `EXP-CUST-ADDR-ZIP` | `X(10)` | Alpha | 10 | Zip code |
| `EXP-CUST-PHONE-NUM` | `X(15)` | Alpha | 30 | Phone numbers (OCCURS 2) |
| `EXP-CUST-SSN` | `9(09)` | Numeric | 9 | SSN (PII) |
| `EXP-CUST-GOVT-ISSUED-ID` | `X(20)` | Alpha | 20 | Government ID |
| `EXP-CUST-DOB-YYYY-MM-DD` | `X(10)` | Date | 10 | Date of birth |
| `EXP-CUST-EFT-ACCOUNT-ID` | `X(10)` | Alpha | 10 | EFT account |
| `EXP-CUST-PRI-CARD-HOLDER-IND` | `X(01)` | Alpha | 1 | Primary cardholder |
| `EXP-CUST-FICO-CREDIT-SCORE` | `9(03) COMP-3` | Packed Decimal | 2 | FICO score |

---

## 14. CICS Commarea (`COCOM01Y.cpy`)

**In-memory structure passed between online programs.**

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `CDEMO-FROM-TRANID` | `X(04)` | Alpha | 4 | Source transaction ID |
| `CDEMO-FROM-PROGRAM` | `X(08)` | Alpha | 8 | Source program name |
| `CDEMO-TO-TRANID` | `X(04)` | Alpha | 4 | Target transaction ID |
| `CDEMO-TO-PROGRAM` | `X(08)` | Alpha | 8 | Target program name |
| `CDEMO-USER-ID` | `X(08)` | Alpha | 8 | Authenticated user ID |
| `CDEMO-USER-TYPE` | `X(01)` | Alpha | 1 | 'A' = Admin, 'U' = User |
| `CDEMO-PGM-CONTEXT` | `9(01)` | Numeric | 1 | 0 = First entry, 1 = Re-entry |
| `CDEMO-CUST-ID` | `9(09)` | Numeric | 9 | Current customer ID |
| `CDEMO-CUST-FNAME` | `X(25)` | Alpha | 25 | Customer first name |
| `CDEMO-CUST-MNAME` | `X(25)` | Alpha | 25 | Customer middle name |
| `CDEMO-CUST-LNAME` | `X(25)` | Alpha | 25 | Customer last name |
| `CDEMO-ACCT-ID` | `9(11)` | Numeric | 11 | Current account ID |
| `CDEMO-ACCT-STATUS` | `X(01)` | Alpha | 1 | Account status |
| `CDEMO-CARD-NUM` | `9(16)` | Numeric | 16 | Current card number |
| `CDEMO-LAST-MAP` | `X(7)` | Alpha | 7 | Last displayed BMS map |
| `CDEMO-LAST-MAPSET` | `X(7)` | Alpha | 7 | Last displayed mapset |

---

## 15. IMS Authorization Detail (`CIPAUDTY.cpy`)

**IMS DB Segment** — Pending Authorization extension

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `PA-AUTH-DATE-9C` | `S9(05) COMP-3` | Packed Decimal | 3 | Authorization date (key, packed) |
| `PA-AUTH-TIME-9C` | `S9(09) COMP-3` | Packed Decimal | 5 | Authorization time (key, packed) |
| `PA-AUTH-ORIG-DATE` | `X(06)` | Alpha | 6 | Original date |
| `PA-AUTH-ORIG-TIME` | `X(06)` | Alpha | 6 | Original time |
| `PA-CARD-NUM` | `X(16)` | Alpha | 16 | Card number |
| `PA-AUTH-TYPE` | `X(04)` | Alpha | 4 | Authorization type |
| `PA-CARD-EXPIRY-DATE` | `X(04)` | Alpha | 4 | Card expiry |
| `PA-MESSAGE-TYPE` | `X(06)` | Alpha | 6 | Message type |
| `PA-MESSAGE-SOURCE` | `X(06)` | Alpha | 6 | Message source |
| `PA-AUTH-ID-CODE` | `X(06)` | Alpha | 6 | Authorization ID code |
| `PA-AUTH-RESP-CODE` | `X(02)` | Alpha | 2 | Response code ('00' = approved) |
| `PA-AUTH-RESP-REASON` | `X(04)` | Alpha | 4 | Response reason |
| `PA-TRANSACTION-AMT` | `+9(10).99` | Signed Decimal | 14 | Transaction amount |
| `PA-MERCHANT-*` | Various | Alpha | Various | Merchant category, ID, name, city, state, zip |
| `PA-FRAUD-FLAG` | `X(01)` | Alpha | 1 | Fraud indicator |

---

## 16. Authorization Request Message (`CCPAURQY.cpy`)

**MQ Message** — sent to authorization queue

| Field | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----|-----:|:---------------------|
| `PA-RQ-AUTH-DATE` | `X(06)` | Alpha | 6 | Request date |
| `PA-RQ-AUTH-TIME` | `X(06)` | Alpha | 6 | Request time |
| `PA-RQ-CARD-NUM` | `X(16)` | Alpha | 16 | Card number |
| `PA-RQ-AUTH-TYPE` | `X(04)` | Alpha | 4 | Authorization type |
| `PA-RQ-CARD-EXPIRY-DATE` | `X(04)` | Alpha | 4 | Card expiry |
| `PA-RQ-MESSAGE-TYPE` | `X(06)` | Alpha | 6 | Message type |
| `PA-RQ-MESSAGE-SOURCE` | `X(06)` | Alpha | 6 | Message source |
| `PA-RQ-PROCESSING-CODE` | `9(06)` | Numeric | 6 | Processing code |
| `PA-RQ-TRANSACTION-AMT` | `+9(10).99` | Signed Decimal | 14 | Amount |
| `PA-RQ-MERCHANT-CATAGORY-CODE` | `X(04)` | Alpha | 4 | MCC code |
| `PA-RQ-ACQR-COUNTRY-CODE` | `X(03)` | Alpha | 3 | Acquirer country |
| `PA-RQ-POS-ENTRY-MODE` | `9(02)` | Numeric | 2 | POS entry mode |
| `PA-RQ-MERCHANT-ID` | `X(15)` | Alpha | 15 | Merchant ID |
| `PA-RQ-MERCHANT-NAME` | `X(22)` | Alpha | 22 | Merchant name |
| `PA-RQ-MERCHANT-CITY` | `X(13)` | Alpha | 13 | Merchant city |
| `PA-RQ-MERCHANT-STATE` | `X(02)` | Alpha | 2 | Merchant state |
| `PA-RQ-MERCHANT-ZIP` | `X(09)` | Alpha | 9 | Merchant zip |
| `PA-RQ-TRANSACTION-ID` | `X(15)` | Alpha | 15 | Transaction ID |

---

## 17. Report Layout (`CVTRA07Y.cpy`)

**Output structure for daily transaction report.**

| Field | PIC Clause | Type | Business Description |
|:------|:-----------|:-----|:---------------------|
| `REPT-SHORT-NAME` | `X(38)` | Alpha | Report short name ("DALYREPT") |
| `REPT-LONG-NAME` | `X(41)` | Alpha | Report full title |
| `REPT-START-DATE` | `X(10)` | Date | Report period start |
| `REPT-END-DATE` | `X(10)` | Date | Report period end |
| `TRAN-REPORT-TRANS-ID` | `X(16)` | Alpha | Transaction ID |
| `TRAN-REPORT-ACCOUNT-ID` | `X(11)` | Alpha | Account ID |
| `TRAN-REPORT-TYPE-CD` | `X(02)` | Alpha | Type code |
| `TRAN-REPORT-TYPE-DESC` | `X(15)` | Alpha | Type description |
| `TRAN-REPORT-CAT-CD` | `9(04)` | Numeric | Category code |
| `TRAN-REPORT-CAT-DESC` | `X(29)` | Alpha | Category description |
| `TRAN-REPORT-SOURCE` | `X(10)` | Alpha | Transaction source |
| `TRAN-REPORT-AMT` | `-ZZZ,ZZZ,ZZZ.ZZ` | Edited Numeric | Transaction amount (formatted) |

---

## Entity Relationship Summary

```
                    ┌──────────────┐
                    │   CUSTOMER   │
                    │  (CVCUS01Y)  │
                    │  PK: CUST-ID │
                    └──────┬───────┘
                           │ 1:N
                    ┌──────┴───────┐
                    │  CARD-XREF   │
                    │  (CVACT03Y)  │
                    │ PK: CARD-NUM │
                    └──┬───────┬───┘
                  1:1  │       │ 1:1
           ┌───────────┘       └───────────┐
    ┌──────┴───────┐              ┌────────┴──────┐
    │   ACCOUNT    │              │  CREDIT CARD  │
    │  (CVACT01Y)  │              │  (CVACT02Y)   │
    │  PK: ACCT-ID │              │ PK: CARD-NUM  │
    └──────┬───────┘              └───────────────┘
           │ 1:N
    ┌──────┴───────┐     ┌───────────────┐     ┌───────────────┐
    │ TRANSACTION  │────▶│  TRAN TYPE    │     │ DISCLOSURE    │
    │  (CVTRA05Y)  │     │  (CVTRA03Y)   │     │   GROUP       │
    │ PK: TRAN-ID  │     │ PK: TRAN-TYPE │     │  (CVTRA02Y)   │
    └──────┬───────┘     └───────────────┘     └───────────────┘
           │ N:1                                       │
    ┌──────┴───────┐                            ┌──────┴───────┐
    │  TRAN CAT    │                            │  TRAN CAT    │
    │   BALANCE    │                            │    TYPE       │
    │  (CVTRA01Y)  │                            │  (CVTRA04Y)  │
    └──────────────┘                            └──────────────┘
```

---

## PII / Sensitive Data Inventory

| Entity | Field | Sensitivity | Notes |
|:-------|:------|:------------|:------|
| Customer | `CUST-SSN` | **High (PII)** | Social Security Number — must be encrypted at rest |
| Customer | `CUST-DOB-YYYY-MM-DD` | **Medium (PII)** | Date of birth |
| Customer | `CUST-PHONE-NUM-1/2` | **Medium (PII)** | Phone numbers |
| Customer | `CUST-GOVT-ISSUED-ID` | **High (PII)** | Government ID |
| User Security | `SEC-USR-PWD` | **Critical** | Plaintext password — must be hashed in modernized system |
| Credit Card | `CARD-NUM` | **High (PCI-DSS)** | Card number — requires tokenization/encryption |
| Credit Card | `CARD-CVV-CD` | **Critical (PCI-DSS)** | CVV — must never be stored post-authorization |
| Export | `EXP-CUST-SSN` | **High (PII)** | SSN in migration export |
