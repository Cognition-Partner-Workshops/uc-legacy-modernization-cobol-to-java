# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** COBOL Copybooks in `app/cpy/`
> **Storage:** VSAM KSDS (Indexed), VSAM ESDS (Sequential), Sequential Flat Files

---

## Entity Relationship Overview

```
┌──────────────┐       ┌──────────────┐       ┌──────────────────┐
│   CUSTOMER   │1─────*│   ACCOUNT    │1─────*│   CREDIT CARD    │
│  (CVCUS01Y)  │       │  (CVACT01Y)  │       │   (CVACT02Y)     │
└──────────────┘       └──────┬───────┘       └────────┬─────────┘
                              │                        │
                              │                  ┌─────┴──────────┐
                              │                  │  CARD XREF     │
                              │                  │  (CVACT03Y)    │
                              │                  └────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
              ┌─────┴──────┐    ┌───────┴────────┐
              │ TRANSACTION│    │  TRAN CATEGORY  │
              │ (CVTRA05Y) │    │  BALANCE        │
              │            │    │  (CVTRA01Y)     │
              └─────┬──────┘    └────────────────┘
                    │
         ┌──────────┴──────────┐
         │                     │
   ┌─────┴──────┐    ┌────────┴───────┐
   │ TRAN TYPE  │    │  TRAN CATEGORY │
   │ (CVTRA03Y) │    │  (CVTRA04Y)    │
   └────────────┘    └────────────────┘

┌──────────────────┐     ┌──────────────────┐
│  USER SECURITY   │     │ DISCLOSURE GROUP  │
│  (CSUSR01Y)      │     │  (CVTRA02Y)       │
└──────────────────┘     └──────────────────┘
```

---

## 1. Customer (`CVCUS01Y`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`
**Record Length:** 500 bytes | **Key:** Customer ID (11 digits) | **Access:** Indexed (KSDS)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| CUST-ID | `9(11)` | Numeric | 11 | Unique customer identifier |
| CUST-FIRST-NAME | `X(25)` | Alpha | 25 | Customer first name |
| CUST-MIDDLE-NAME | `X(25)` | Alpha | 25 | Customer middle name |
| CUST-LAST-NAME | `X(25)` | Alpha | 25 | Customer last name |
| CUST-ADDR-LINE-1 | `X(50)` | Alpha | 50 | Street address line 1 |
| CUST-ADDR-LINE-2 | `X(50)` | Alpha | 50 | Street address line 2 |
| CUST-ADDR-LINE-3 | `X(50)` | Alpha | 50 | Street address line 3 |
| CUST-ADDR-STATE-CD | `X(02)` | Alpha | 2 | US state code |
| CUST-ADDR-COUNTRY-CD | `X(03)` | Alpha | 3 | Country code |
| CUST-ADDR-ZIP | `X(10)` | Alpha | 10 | ZIP/postal code |
| CUST-PHONE-NUM-1 | `X(15)` | Alpha | 15 | Primary phone number |
| CUST-PHONE-NUM-2 | `X(15)` | Alpha | 15 | Secondary phone number |
| CUST-SSN | `9(09)` | Numeric | 9 | Social Security Number |
| CUST-GOVT-ISSUED-ID | `X(20)` | Alpha | 20 | Government-issued ID |
| CUST-DOB-YYYYMMDD | `X(10)` | Date | 10 | Date of birth (YYYY-MM-DD) |
| CUST-EFT-ACCOUNT-ID | `X(10)` | Alpha | 10 | EFT/bank account reference |
| CUST-PRI-CARD-HOLDER-IND | `X(01)` | Flag | 1 | Primary card holder indicator (Y/N) |
| CUST-FICO-CREDIT-SCORE | `9(03)` | Numeric | 3 | FICO credit score (300-850) |
| FILLER | `X(168)` | -- | 168 | Reserved for future use |

**Business Rules:**
- SSN must be valid (no 000, 666, or 900-999 in first group)
- FICO score range: 300-850
- Phone format: (NNN)NNN-NNNN

---

## 2. Account (`CVACT01Y`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`
**Record Length:** 300 bytes | **Key:** Account ID (11 digits) | **Access:** Indexed (KSDS)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| ACCT-ID | `9(11)` | Numeric | 11 | Unique account identifier |
| ACCT-ACTIVE-STATUS | `X(01)` | Flag | 1 | Account status (Y=Active, N=Inactive) |
| ACCT-CURR-BAL | `S9(10)V99` | Signed Decimal | 12 | Current balance |
| ACCT-CREDIT-LIMIT | `S9(10)V99` | Signed Decimal | 12 | Credit limit |
| ACCT-CASH-CREDIT-LIMIT | `S9(10)V99` | Signed Decimal | 12 | Cash advance credit limit |
| ACCT-OPEN-DATE | `X(10)` | Date | 10 | Account open date (YYYY-MM-DD) |
| ACCT-EXPIRAION-DATE | `X(10)` | Date | 10 | Account expiration date |
| ACCT-REISSUE-DATE | `X(10)` | Date | 10 | Card reissue date |
| ACCT-CURR-CYC-CREDIT | `S9(10)V99` | Signed Decimal | 12 | Current cycle credit total |
| ACCT-CURR-CYC-DEBIT | `S9(10)V99` | Signed Decimal | 12 | Current cycle debit total |
| ACCT-ADDR-ZIP | `X(10)` | Alpha | 10 | Billing ZIP code |
| ACCT-GROUP-ID | `X(10)` | Alpha | 10 | Disclosure group assignment |
| FILLER | `X(178)` | -- | 178 | Reserved for future use |

**Business Rules:**
- Balance = prior balance + debits - credits - payments
- Credit limit must be > 0
- Cash credit limit <= credit limit

---

## 3. Credit Card (`CVACT02Y`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`
**Record Length:** 150 bytes | **Key:** Card Number (16 chars) | **Access:** Indexed (KSDS)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| CARD-NUM | `X(16)` | Alpha | 16 | Credit card number (PAN) |
| CARD-ACCT-ID | `9(11)` | Numeric | 11 | Linked account ID (FK) |
| CARD-CVV-CD | `9(03)` | Numeric | 3 | Card verification value |
| CARD-EMBOSSED-NAME | `X(50)` | Alpha | 50 | Name embossed on card |
| CARD-EXPIRAION-DATE | `X(10)` | Date | 10 | Card expiration date |
| CARD-ACTIVE-STATUS | `X(01)` | Flag | 1 | Card status (Y=Active, N=Inactive) |
| FILLER | `X(59)` | -- | 59 | Reserved for future use |

---

## 4. Card Cross-Reference (`CVACT03Y`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`
**Record Length:** 50 bytes | **Key:** Card Number (16 chars) | **Alternate Index:** Account ID
**Access:** Indexed (KSDS) with AIX on Account ID

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| XREF-CARD-NUM | `X(16)` | Alpha | 16 | Card number (PK) |
| XREF-CUST-ID | `9(09)` | Numeric | 9 | Customer ID (FK) |
| XREF-ACCT-ID | `9(11)` | Numeric | 11 | Account ID (FK, AIX key at offset 25) |
| FILLER | `X(14)` | -- | 14 | Reserved |

**Purpose:** Links cards to customers and accounts. The alternate index on Account ID allows lookup of all cards for a given account.

---

## 5. Transaction (`CVTRA05Y`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`
**Record Length:** 350 bytes | **Key:** Transaction ID (16 chars) | **Access:** Indexed (KSDS)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| TRAN-ID | `X(16)` | Alpha | 16 | Unique transaction identifier |
| TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code (FK to TRAN-TYPE) |
| TRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| TRAN-SOURCE | `X(10)` | Alpha | 10 | Transaction source (POS, ATM, WEB, etc.) |
| TRAN-DESC | `X(100)` | Alpha | 100 | Transaction description |
| TRAN-AMT | `S9(09)V99` | Signed Decimal | 11 | Transaction amount |
| TRAN-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant identifier |
| TRAN-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| TRAN-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| TRAN-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP code |
| TRAN-CARD-NUM | `X(16)` | Alpha | 16 | Card number used |
| TRAN-ORIG-TS | `X(26)` | Timestamp | 26 | Original transaction timestamp |
| TRAN-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| FILLER | `X(20)` | -- | 20 | Reserved |

**Business Rules:**
- Amount is signed: positive = debit/purchase, negative = credit/refund
- Card number must exist in XREF file for validation
- Timestamps in DB2 format: YYYY-MM-DD-HH.MM.SS.FFFFFF

---

## 6. Daily Transaction (`CVTRA06Y`)

**Dataset:** `AWS.M2.CARDDEMO.DALYTRAN` (Sequential)
**Record Length:** 350 bytes | **Access:** Sequential

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| DALYTRAN-ID | `X(16)` | Alpha | 16 | Daily transaction identifier |
| DALYTRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| DALYTRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| DALYTRAN-SOURCE | `X(10)` | Alpha | 10 | Transaction source |
| DALYTRAN-DESC | `X(100)` | Alpha | 100 | Transaction description |
| DALYTRAN-AMT | `S9(09)V99` | Signed Decimal | 11 | Transaction amount |
| DALYTRAN-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant ID |
| DALYTRAN-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| DALYTRAN-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| DALYTRAN-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP |
| DALYTRAN-CARD-NUM | `X(16)` | Alpha | 16 | Card number |
| DALYTRAN-ORIG-TS | `X(26)` | Timestamp | 26 | Original timestamp |
| DALYTRAN-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| FILLER | `X(20)` | -- | 20 | Reserved |

**Purpose:** Staging file for daily transactions before posting to master. Same layout as Transaction master but accessed sequentially for batch processing.

---

## 7. Transaction Category Balance (`CVTRA01Y`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS`
**Record Length:** 50 bytes | **Key:** Acct ID + Type Code + Category Code | **Access:** Indexed (KSDS)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| TRANCAT-ACCT-ID | `9(11)` | Numeric | 11 | Account ID (composite key part 1) |
| TRANCAT-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type (composite key part 2) |
| TRANCAT-CD | `9(04)` | Numeric | 4 | Category code (composite key part 3) |
| TRAN-CAT-BAL | `S9(09)V99` | Signed Decimal | 11 | Running balance for this category |
| FILLER | `X(22)` | -- | 22 | Reserved |

**Purpose:** Tracks running balances per account, per transaction type, per category. Used for interest calculation and reporting.

---

## 8. Disclosure Group / Interest Rate (`CVTRA02Y`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`
**Record Length:** 50 bytes | **Key:** Group ID + Type Code + Category Code | **Access:** Indexed (KSDS)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| DIS-ACCT-GROUP-ID | `X(10)` | Alpha | 10 | Disclosure group identifier |
| DIS-TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type code |
| DIS-TRAN-CAT-CD | `9(04)` | Numeric | 4 | Transaction category code |
| DIS-INT-RATE | `S9(04)V99` | Signed Decimal | 6 | Interest rate for this group/type/category |
| FILLER | `X(28)` | -- | 28 | Reserved |

**Purpose:** Defines interest rates by disclosure group. Each account is assigned to a group via `ACCT-GROUP-ID`. Interest calculation looks up the rate here.

---

## 9. Transaction Type Reference (`CVTRA03Y`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`
**Record Length:** 60 bytes | **Key:** Transaction Type Code | **Access:** Indexed (KSDS)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| TRAN-TYPE | `X(02)` | Alpha | 2 | Transaction type code (PK) |
| TRAN-TYPE-DESC | `X(50)` | Alpha | 50 | Type description (e.g., "Purchase", "Cash Advance") |
| FILLER | `X(08)` | -- | 8 | Reserved |

---

## 10. Transaction Category Reference (`CVTRA04Y`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`
**Record Length:** 60 bytes | **Key:** Type Code + Category Code | **Access:** Indexed (KSDS)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| TRAN-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type (composite key part 1) |
| TRAN-CAT-CD | `9(04)` | Numeric | 4 | Category code (composite key part 2) |
| TRAN-CAT-TYPE-DESC | `X(50)` | Alpha | 50 | Category description (e.g., "Retail Purchase") |
| FILLER | `X(04)` | -- | 4 | Reserved |

---

## 11. User Security (`CSUSR01Y`)

**VSAM Dataset:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`
**Record Length:** 80 bytes | **Key:** User ID (8 chars) | **Access:** Indexed (KSDS)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| SEC-USR-ID | `X(08)` | Alpha | 8 | User login ID (PK) |
| SEC-USR-FNAME | `X(20)` | Alpha | 20 | First name |
| SEC-USR-LNAME | `X(20)` | Alpha | 20 | Last name |
| SEC-USR-PWD | `X(08)` | Alpha | 8 | Password (plaintext) |
| SEC-USR-TYPE | `X(01)` | Flag | 1 | User type (R=Regular, A=Admin) |
| SEC-USR-FILLER | `X(23)` | -- | 23 | Reserved |

**Business Rules:**
- Admin users (type 'A') can access COADM01C admin menu
- Regular users (type 'R') see standard COMEN01C menu
- Default accounts: ADMIN001/PASSWORD (admin), USER0001/PASSWORD (regular)

---

## 12. Statement Transaction Layout (`COSTM01`)

**Purpose:** Altered transaction layout with card-number-first key for statement generation

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| TRNX-CARD-NUM | `X(16)` | Alpha | 16 | Card number (key part 1) |
| TRNX-ID | `X(16)` | Alpha | 16 | Transaction ID (key part 2) |
| TRNX-TYPE-CD | `X(02)` | Alpha | 2 | Transaction type |
| TRNX-CAT-CD | `9(04)` | Numeric | 4 | Category code |
| TRNX-SOURCE | `X(10)` | Alpha | 10 | Source |
| TRNX-DESC | `X(100)` | Alpha | 100 | Description |
| TRNX-AMT | `S9(09)V99` | Signed Decimal | 11 | Amount |
| TRNX-MERCHANT-ID | `9(09)` | Numeric | 9 | Merchant ID |
| TRNX-MERCHANT-NAME | `X(50)` | Alpha | 50 | Merchant name |
| TRNX-MERCHANT-CITY | `X(50)` | Alpha | 50 | Merchant city |
| TRNX-MERCHANT-ZIP | `X(10)` | Alpha | 10 | Merchant ZIP |
| TRNX-ORIG-TS | `X(26)` | Timestamp | 26 | Original timestamp |
| TRNX-PROC-TS | `X(26)` | Timestamp | 26 | Processing timestamp |
| FILLER | `X(20)` | -- | 20 | Reserved |

**Note:** Same data as CVTRA05Y but re-keyed by Card Number + Transaction ID for sequential statement generation.

---

## 13. Card Detail Record (`CVCRD01Y`)

**Purpose:** Extended card information for online display (combines card + account data)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| CCARD-ID | `9(04)` | Numeric | 4 | Card sequence number |
| CCARD-SIT | `X(02)` | Alpha | 2 | Card situation code |
| CCARD-SIT-DAT | `X(10)` | Date | 10 | Situation date |
| CCARD-TYPE | `X(02)` | Alpha | 2 | Card type |
| CCARD-PCRD-NUM-N | `X(16)` | Alpha | 16 | Previous card number |
| CCARD-ACRD-NUM-N | `X(16)` | Alpha | 16 | Associated card number |
| CCARD-NUM-N | `X(16)` | Alpha | 16 | Current card number |
| CCARD-ACCT-ID-N | `9(11)` | Numeric | 11 | Account ID |
| CCARD-CVV-CD | `9(03)` | Numeric | 3 | CVV code |
| CCARD-EMBOSSED-NAME | `X(50)` | Alpha | 50 | Embossed name |
| CCARD-EXPIRAION-DATE | `X(10)` | Date | 10 | Expiration date |
| CCARD-ACTIVE-STATUS | `X(01)` | Flag | 1 | Active status |

---

## 14. Export Record (`CVEXPORT`)

**Purpose:** Unified export format for data migration (used by CBEXPORT/CBIMPORT)

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| EXP-REC-TYPE | `X(01)` | Flag | 1 | Record type: C=Customer, A=Account, X=Xref, T=Transaction, D=Card |
| EXP-TIMESTAMP | `X(26)` | Timestamp | 26 | Export timestamp |
| EXP-DATA | (varies) | -- | -- | Type-specific data follows |

Sub-records by type:
- **Customer (C):** All fields from CVCUS01Y
- **Account (A):** All fields from CVACT01Y
- **Cross-Reference (X):** All fields from CVACT03Y
- **Transaction (T):** All fields from CVTRA05Y
- **Card (D):** Card number, account ID, CVV, embossed name, expiration, status

---

## 15. Communication Area (`COCOM01Y`)

**Purpose:** CICS COMMAREA for inter-program navigation and data passing

| Field Name | PIC Clause | Type | Size | Business Description |
|------------|-----------|------|------|---------------------|
| CDEMO-FROM-TRANID | `X(04)` | Alpha | 4 | Originating transaction ID |
| CDEMO-FROM-PROGRAM | `X(08)` | Alpha | 8 | Originating program name |
| CDEMO-TO-TRANID | `X(04)` | Alpha | 4 | Target transaction ID |
| CDEMO-TO-PROGRAM | `X(08)` | Alpha | 8 | Target program name |
| CDEMO-USER-ID | `X(08)` | Alpha | 8 | Current logged-in user |
| CDEMO-USER-TYPE | `X(01)` | Flag | 1 | User type (R/A) |
| CDEMO-PGM-CONTEXT | `9(01)` | Numeric | 1 | Program context flag |

**Purpose:** Passed between all online CICS programs via EXEC CICS XCTL/RETURN to maintain session state, user identity, and navigation context.

---

## 16. Lookup Code Tables (`CSLKPCDY`)

**Purpose:** In-memory lookup tables compiled into programs (1,318 lines)

Contains hard-coded reference data for:
- **US State codes** (50 states + territories)
- **Country codes** (ISO 3166)
- **Transaction type codes**
- **Card status codes**
- **Account status codes**

---

## VSAM Dataset Summary

| Dataset Name | Key | Rec Len | Entity | Copybook |
|-------------|-----|---------|--------|----------|
| AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | CUST-ID (11) | 500 | Customer | CVCUS01Y |
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | ACCT-ID (11) | 300 | Account | CVACT01Y |
| AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | CARD-NUM (16) | 150 | Credit Card | CVACT02Y |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | XREF-CARD-NUM (16) | 50 | Card Cross-Ref | CVACT03Y |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | TRAN-ID (16) | 350 | Transaction | CVTRA05Y |
| AWS.M2.CARDDEMO.TCATBAL.VSAM.KSDS | Composite (17) | 50 | Category Balance | CVTRA01Y |
| AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | Composite (16) | 50 | Disclosure Group | CVTRA02Y |
| AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | TRAN-TYPE (2) | 60 | Transaction Type | CVTRA03Y |
| AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | Composite (6) | 60 | Transaction Category | CVTRA04Y |
| AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | SEC-USR-ID (8) | 80 | User Security | CSUSR01Y |
| AWS.M2.CARDDEMO.DALYTRAN | Sequential | 350 | Daily Transaction | CVTRA06Y |
| AWS.M2.CARDDEMO.DALYREJS | Sequential | 430 | Daily Rejects | -- |

---

## PIC Clause Quick Reference

| PIC Pattern | COBOL Type | Java Equivalent | Example |
|-------------|-----------|-----------------|---------|
| `X(n)` | Alphanumeric | `String` | `PIC X(16)` → 16-char string |
| `9(n)` | Unsigned numeric | `long` / `int` | `PIC 9(11)` → 11-digit number |
| `S9(n)V99` | Signed decimal | `BigDecimal` | `PIC S9(09)V99` → ±999999999.99 |
| `S9(n) COMP` | Binary integer | `int` / `long` | `PIC S9(09) COMP` → binary int |
| `S9(n) COMP-3` | Packed decimal | `BigDecimal` | `PIC S9(4) COMP-3` → packed BCD |
