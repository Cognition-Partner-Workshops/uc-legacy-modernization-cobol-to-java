# Data Dictionary - CardDemo

> Business entity definitions extracted from COBOL copybook PIC clauses.

---

## Entity Relationship Overview

```
Customer (1) ──< Card (N) ──< Transaction (N)
    │                │
    └── XREF ────────┘── Account (1)
                              │
                              ├── Transaction Category Balance (N)
                              └── Disclosure Group (N)

User Security ── independent (RACF-simulated)
```

---

## 1. Account (`CVACT01Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.ACCTDATA`
**Record Length:** 300 bytes | **Key:** ACCT-ID | **Format:** FB

| Field | PIC Clause | Type | Length | Business Meaning |
|-------|-----------|------|-------:|-----------------|
| ACCT-ID | PIC 9(11) | Numeric | 11 | Unique account identifier |
| ACCT-ACTIVE-STATUS | PIC X(01) | Alpha | 1 | Account status (Y=Active, N=Closed) |
| ACCT-CURR-BAL | PIC S9(10)V99 | Signed Decimal | 12.2 | Current account balance |
| ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12.2 | Maximum credit limit |
| ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12.2 | Cash advance limit |
| ACCT-OPEN-DATE | PIC X(10) | Date (YYYY-MM-DD) | 10 | Account opening date |
| ACCT-EXPIRAION-DATE | PIC X(10) | Date (YYYY-MM-DD) | 10 | Account expiration date |
| ACCT-REISSUE-DATE | PIC X(10) | Date (YYYY-MM-DD) | 10 | Last card reissue date |
| ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Signed Decimal | 12.2 | Current cycle credits |
| ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 | Signed Decimal | 12.2 | Current cycle debits |
| ACCT-ADDR-ZIP | PIC X(10) | Alpha | 10 | Account holder ZIP code |
| ACCT-GROUP-ID | PIC X(10) | Alpha | 10 | Disclosure/interest rate group |
| FILLER | PIC X(178) | — | 178 | Reserved space |

---

## 2. Card (`CVACT02Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDDATA`
**Record Length:** 150 bytes | **Key:** CARD-NUM | **Format:** FB

| Field | PIC Clause | Type | Length | Business Meaning |
|-------|-----------|------|-------:|-----------------|
| CARD-NUM | PIC X(16) | Alpha | 16 | Credit card number (PAN) |
| CARD-ACCT-ID | PIC 9(11) | Numeric | 11 | Linked account ID (FK) |
| CARD-CVV-CD | PIC 9(03) | Numeric | 3 | Card verification value |
| CARD-EMBOSSED-NAME | PIC X(50) | Alpha | 50 | Name printed on card |
| CARD-EXPIRAION-DATE | PIC X(10) | Date | 10 | Card expiration date |
| CARD-ACTIVE-STATUS | PIC X(01) | Alpha | 1 | Card status (Y/N) |
| FILLER | PIC X(59) | — | 59 | Reserved space |

---

## 3. Customer (`CVCUS01Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.CUSTDATA`
**Record Length:** 500 bytes | **Key:** CUST-ID | **Format:** FB

| Field | PIC Clause | Type | Length | Business Meaning |
|-------|-----------|------|-------:|-----------------|
| CUST-ID | PIC 9(09) | Numeric | 9 | Unique customer identifier |
| CUST-FIRST-NAME | PIC X(25) | Alpha | 25 | First name |
| CUST-MIDDLE-NAME | PIC X(25) | Alpha | 25 | Middle name |
| CUST-LAST-NAME | PIC X(25) | Alpha | 25 | Last name |
| CUST-ADDR-LINE-1 | PIC X(50) | Alpha | 50 | Address line 1 |
| CUST-ADDR-LINE-2 | PIC X(50) | Alpha | 50 | Address line 2 |
| CUST-ADDR-LINE-3 | PIC X(50) | Alpha | 50 | Address line 3 |
| CUST-ADDR-STATE-CD | PIC X(02) | Alpha | 2 | US state code |
| CUST-ADDR-COUNTRY-CD | PIC X(03) | Alpha | 3 | Country code |
| CUST-ADDR-ZIP | PIC X(10) | Alpha | 10 | ZIP / postal code |
| CUST-PHONE-NUM-1 | PIC X(15) | Alpha | 15 | Primary phone |
| CUST-PHONE-NUM-2 | PIC X(15) | Alpha | 15 | Secondary phone |
| CUST-SSN | PIC 9(09) | Numeric | 9 | Social Security Number |
| CUST-GOVT-ISSUED-ID | PIC X(20) | Alpha | 20 | Government-issued ID |
| CUST-DOB-YYYY-MM-DD | PIC X(10) | Date | 10 | Date of birth |
| CUST-EFT-ACCOUNT-ID | PIC X(10) | Alpha | 10 | EFT/bank account for payments |
| CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alpha | 1 | Primary cardholder? (Y/N) |
| CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric | 3 | FICO credit score (300-850) |
| FILLER | PIC X(168) | — | 168 | Reserved space |

---

## 4. Card Cross-Reference (`CVACT03Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDXREF`
**Record Length:** 50 bytes | **Key:** XREF-CARD-NUM | **Format:** FB

| Field | PIC Clause | Type | Length | Business Meaning |
|-------|-----------|------|-------:|-----------------|
| XREF-CARD-NUM | PIC X(16) | Alpha | 16 | Card number (PK, links to Card) |
| XREF-CUST-ID | PIC 9(09) | Numeric | 9 | Customer ID (FK) |
| XREF-ACCT-ID | PIC 9(11) | Numeric | 11 | Account ID (FK) |
| FILLER | PIC X(14) | — | 14 | Reserved space |

---

## 5. Transaction (`CVTRA05Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`
**Record Length:** 350 bytes | **Key:** TRAN-ID | **Format:** FB

| Field | PIC Clause | Type | Length | Business Meaning |
|-------|-----------|------|-------:|-----------------|
| TRAN-ID | PIC X(16) | Alpha | 16 | Unique transaction identifier |
| TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code (FK to TRANTYPE) |
| TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code |
| TRAN-SOURCE | PIC X(10) | Alpha | 10 | Origination source |
| TRAN-DESC | PIC X(100) | Alpha | 100 | Transaction description |
| TRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11.2 | Transaction amount |
| TRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant identifier |
| TRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant name |
| TRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant city |
| TRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant ZIP code |
| TRAN-CARD-NUM | PIC X(16) | Alpha | 16 | Card used (FK to Card) |
| TRAN-ORIG-TS | PIC X(26) | Timestamp | 26 | Original transaction timestamp |
| TRAN-PROC-TS | PIC X(26) | Timestamp | 26 | Processing timestamp |
| FILLER | PIC X(20) | — | 20 | Reserved space |

---

## 6. Daily Transaction (`CVTRA06Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.DALYTRAN`
**Record Length:** 350 bytes | **Format:** FB
> Same structure as Transaction; used as a staging file for daily batch posting.

| Field | PIC Clause | Type | Length | Business Meaning |
|-------|-----------|------|-------:|-----------------|
| DALYTRAN-ID | PIC X(16) | Alpha | 16 | Daily transaction ID |
| DALYTRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type |
| DALYTRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Category code |
| DALYTRAN-SOURCE | PIC X(10) | Alpha | 10 | Source system |
| DALYTRAN-DESC | PIC X(100) | Alpha | 100 | Description |
| DALYTRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11.2 | Amount |
| DALYTRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant ID |
| DALYTRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant name |
| DALYTRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant city |
| DALYTRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant ZIP |
| DALYTRAN-CARD-NUM | PIC X(16) | Alpha | 16 | Card number |
| DALYTRAN-ORIG-TS | PIC X(26) | Timestamp | 26 | Original timestamp |
| DALYTRAN-PROC-TS | PIC X(26) | Timestamp | 26 | Processing timestamp |
| FILLER | PIC X(20) | — | 20 | Reserved |

---

## 7. Transaction Category Balance (`CVTRA01Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TCATBALF`
**Record Length:** 50 bytes | **Key:** Composite (ACCT-ID + TYPE-CD + CAT-CD) | **Format:** FB

| Field | PIC Clause | Type | Length | Business Meaning |
|-------|-----------|------|-------:|-----------------|
| TRANCAT-ACCT-ID | PIC 9(11) | Numeric | 11 | Account ID (FK) |
| TRANCAT-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code |
| TRANCAT-CD | PIC 9(04) | Numeric | 4 | Category code |
| TRAN-CAT-BAL | PIC S9(09)V99 | Signed Decimal | 11.2 | Balance for this category |
| FILLER | PIC X(22) | — | 22 | Reserved |

---

## 8. Disclosure Group (`CVTRA02Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.DISCGRP`
**Record Length:** 50 bytes | **Key:** Composite (GROUP-ID + TYPE-CD + CAT-CD) | **Format:** FB

| Field | PIC Clause | Type | Length | Business Meaning |
|-------|-----------|------|-------:|-----------------|
| DIS-ACCT-GROUP-ID | PIC X(10) | Alpha | 10 | Account group identifier |
| DIS-TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code |
| DIS-TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code |
| DIS-INT-RATE | PIC S9(04)V99 | Signed Decimal | 6.2 | Interest rate for this group |
| FILLER | PIC X(28) | — | 28 | Reserved |

---

## 9. Transaction Type (`CVTRA03Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANTYPE`
**Record Length:** 60 bytes | **Key:** TRAN-TYPE | **Format:** FB

| Field | PIC Clause | Type | Length | Business Meaning |
|-------|-----------|------|-------:|-----------------|
| TRAN-TYPE | PIC X(02) | Alpha | 2 | Type code (e.g., "PR"=Purchase) |
| TRAN-TYPE-DESC | PIC X(50) | Alpha | 50 | Human-readable description |
| FILLER | PIC X(08) | — | 8 | Reserved |

---

## 10. Transaction Category (`CVTRA04Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANCATG`
**Record Length:** 60 bytes | **Key:** Composite (TYPE-CD + CAT-CD) | **Format:** FB

| Field | PIC Clause | Type | Length | Business Meaning |
|-------|-----------|------|-------:|-----------------|
| TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code |
| TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Category code within type |
| TRAN-CAT-TYPE-DESC | PIC X(50) | Alpha | 50 | Category description |
| FILLER | PIC X(04) | — | 4 | Reserved |

---

## 11. User Security (`CSUSR01Y.cpy`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.USRSEC`
**Record Length:** 80 bytes | **Key:** SEC-USR-ID | **Format:** FB

| Field | PIC Clause | Type | Length | Business Meaning |
|-------|-----------|------|-------:|-----------------|
| SEC-USR-ID | PIC X(08) | Alpha | 8 | User login ID |
| SEC-USR-FNAME | PIC X(20) | Alpha | 20 | First name |
| SEC-USR-LNAME | PIC X(20) | Alpha | 20 | Last name |
| SEC-USR-PWD | PIC X(08) | Alpha | 8 | Password (plaintext) |
| SEC-USR-TYPE | PIC X(01) | Alpha | 1 | User type: A=Admin, U=User |
| SEC-USR-FILLER | PIC X(23) | — | 23 | Reserved |

---

## 12. Communication Area (`COCOM01Y.cpy`)

> Shared between all CICS programs via COMMAREA for inter-program data passing.

| Field | PIC Clause | Type | Length | Business Meaning |
|-------|-----------|------|-------:|-----------------|
| CDEMO-FROM-TRANID | PIC X(04) | Alpha | 4 | Originating transaction ID |
| CDEMO-FROM-PROGRAM | PIC X(08) | Alpha | 8 | Originating program name |
| CDEMO-TO-TRANID | PIC X(04) | Alpha | 4 | Target transaction ID |
| CDEMO-TO-PROGRAM | PIC X(08) | Alpha | 8 | Target program name |
| CDEMO-USER-ID | PIC X(08) | Alpha | 8 | Current user ID |
| CDEMO-USER-TYPE | PIC X(01) | Alpha | 1 | A=Admin, U=User |
| CDEMO-PGM-CONTEXT | PIC 9(01) | Numeric | 1 | 0=Enter, 1=Re-enter |
| CDEMO-CUST-ID | PIC 9(09) | Numeric | 9 | Selected customer |
| CDEMO-CUST-FNAME | PIC X(25) | Alpha | 25 | Customer first name |
| CDEMO-CUST-MNAME | PIC X(25) | Alpha | 25 | Customer middle name |
| CDEMO-CUST-LNAME | PIC X(25) | Alpha | 25 | Customer last name |
| CDEMO-ACCT-ID | PIC 9(11) | Numeric | 11 | Selected account |
| CDEMO-ACCT-STATUS | PIC X(01) | Alpha | 1 | Account status |
| CDEMO-CARD-NUM | PIC 9(16) | Numeric | 16 | Selected card number |
| CDEMO-LAST-MAP | PIC X(07) | Alpha | 7 | Last BMS map sent |
| CDEMO-LAST-MAPSET | PIC X(07) | Alpha | 7 | Last BMS mapset sent |

---

## 13. Export Record (`CVEXPORT.cpy`)

> Multi-record export layout using REDEFINES for branch migration.
> **Record Length:** 500 bytes | Uses COMP and COMP-3 packed fields.

### Header Fields (Common)

| Field | PIC Clause | Type | Length | Business Meaning |
|-------|-----------|------|-------:|-----------------|
| EXPORT-REC-TYPE | PIC X(1) | Alpha | 1 | C=Customer, A=Account, T=Transaction, X=Xref |
| EXPORT-TIMESTAMP | PIC X(26) | Timestamp | 26 | Export timestamp |
| EXPORT-SEQUENCE-NUM | PIC 9(9) COMP | Binary | 4 | Sequence number |
| EXPORT-BRANCH-ID | PIC X(4) | Alpha | 4 | Branch identifier |
| EXPORT-REGION-CODE | PIC X(5) | Alpha | 5 | Region code |

### Variant: Customer Data (REDEFINES EXPORT-RECORD-DATA)
Uses OCCURS for address lines (3) and phone numbers (2). Contains COMP-3 packed FICO score.

### Variant: Account Data (REDEFINES EXPORT-RECORD-DATA)
Uses COMP-3 for balance and cash limit fields, COMP for cycle debit.

### Variant: Transaction Data (REDEFINES EXPORT-RECORD-DATA)
Uses COMP-3 for amount, COMP for merchant ID.

### Variant: Card Cross-Reference (REDEFINES EXPORT-RECORD-DATA)
Simple alphanumeric fields matching CVACT03Y structure.

---

## 14. Transaction Report Layout (`COSTM01.CPY`)

> Alternate transaction layout re-keyed by card number for reporting use.

| Field | PIC Clause | Type | Length | Business Meaning |
|-------|-----------|------|-------:|-----------------|
| TRNX-CARD-NUM | PIC X(16) | Alpha | 16 | Card number (primary sort key) |
| TRNX-ID | PIC X(16) | Alpha | 16 | Transaction ID (secondary key) |
| TRNX-TYPE-CD | PIC X(02) | Alpha | 2 | Type code |
| TRNX-CAT-CD | PIC 9(04) | Numeric | 4 | Category code |
| TRNX-SOURCE | PIC X(10) | Alpha | 10 | Source |
| TRNX-DESC | PIC X(100) | Alpha | 100 | Description |
| TRNX-AMT | PIC S9(09)V99 | Signed Decimal | 11.2 | Amount |
| TRNX-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant |
| TRNX-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant name |
| TRNX-MERCHANT-CITY | PIC X(50) | Alpha | 50 | City |
| TRNX-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | ZIP |
| TRNX-ORIG-TS | PIC X(26) | Timestamp | 26 | Original timestamp |
| TRNX-PROC-TS | PIC X(26) | Timestamp | 26 | Processing timestamp |

---

## Data Storage Summary

| VSAM Dataset | Entity | Key | RECL | Access Pattern |
|-------------|--------|-----|-----:|---------------|
| ACCTDATA | Account | ACCT-ID | 300 | KSDS random + sequential |
| CARDDATA | Card | CARD-NUM | 150 | KSDS random |
| CUSTDATA | Customer | CUST-ID | 500 | KSDS random |
| CARDXREF | Cross-Reference | XREF-CARD-NUM | 50 | KSDS random (multi-key lookup) |
| TRANSACT | Transaction | TRAN-ID | 350 | KSDS + AIX by card |
| DALYTRAN | Daily Transaction | DALYTRAN-ID | 350 | Sequential (batch input) |
| USRSEC | User Security | SEC-USR-ID | 80 | KSDS random |
| TCATBALF | Category Balance | Composite | 50 | KSDS random |
| DISCGRP | Disclosure Group | Composite | 50 | KSDS random |
| TRANCATG | Transaction Category | Composite | 60 | KSDS sequential |
| TRANTYPE | Transaction Type | TRAN-TYPE | 60 | KSDS random |

---

## Data Type Reference

| COBOL PIC | Java Equivalent | Notes |
|-----------|----------------|-------|
| PIC 9(n) | `long` / `int` | Unsigned numeric display |
| PIC S9(n)V99 | `BigDecimal` | Signed with 2 implied decimals |
| PIC S9(n)V99 COMP-3 | `BigDecimal` | Packed decimal (BCD) |
| PIC 9(n) COMP | `int` / `long` | Binary integer |
| PIC X(n) | `String` | Alphanumeric, space-padded |
| PIC X(10) (dates) | `LocalDate` | YYYY-MM-DD format |
| PIC X(26) (timestamps) | `LocalDateTime` | YYYY-MM-DD-HH.MM.SS.ffffff |
