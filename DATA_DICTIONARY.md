# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Source**: Copybook PIC clause analysis from `app/cpy/`
> Extracts business entities, field definitions, data types, and record layouts into a business-friendly format.

---

## 1. Account Master (`CVACT01Y.cpy`) — 300 bytes

The core account record stored in VSAM KSDS (`ACCTDATA.VSAM.KSDS`).

| # | Field | PIC Clause | Type | Length | Business Description |
|---|-------|-----------|------|--------|---------------------|
| 1 | ACCT-ID | 9(11) | Numeric | 11 | Unique account identifier |
| 2 | ACCT-ACTIVE-STATUS | X(01) | Alpha | 1 | Account status flag (active/inactive) |
| 3 | ACCT-CURR-BAL | S9(10)V99 | Signed Decimal | 12 | Current account balance |
| 4 | ACCT-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12 | Credit limit |
| 5 | ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12 | Cash advance credit limit |
| 6 | ACCT-OPEN-DATE | X(10) | Date String | 10 | Account opening date |
| 7 | ACCT-EXPIRAION-DATE | X(10) | Date String | 10 | Account expiration date |
| 8 | ACCT-REISSUE-DATE | X(10) | Date String | 10 | Last card reissue date |
| 9 | ACCT-CURR-CYC-CREDIT | S9(10)V99 | Signed Decimal | 12 | Current cycle credit total |
| 10 | ACCT-CURR-CYC-DEBIT | S9(10)V99 | Signed Decimal | 12 | Current cycle debit total |
| 11 | ACCT-ADDR-ZIP | X(10) | Alpha | 10 | Account holder ZIP code |
| 12 | ACCT-GROUP-ID | X(10) | Alpha | 10 | Disclosure/interest group identifier |
| 13 | FILLER | X(178) | Filler | 178 | Reserved space |

**Key**: ACCT-ID | **Record Length**: 300 bytes | **VSAM Type**: KSDS

---

## 2. Card Data Record (`CVACT02Y.cpy`) — 150 bytes

Credit card information stored in VSAM KSDS (`CARDDATA.VSAM.KSDS`).

| # | Field | PIC Clause | Type | Length | Business Description |
|---|-------|-----------|------|--------|---------------------|
| 1 | CARD-NUM | X(16) | Alpha | 16 | Credit card number (PAN) |
| 2 | CARD-ACCT-ID | 9(11) | Numeric | 11 | Linked account identifier |
| 3 | CARD-CVV-CD | 9(03) | Numeric | 3 | Card verification value (CVV) |
| 4 | CARD-EMBOSSED-NAME | X(50) | Alpha | 50 | Name embossed on card |
| 5 | CARD-EXPIRAION-DATE | X(10) | Date String | 10 | Card expiration date |
| 6 | CARD-ACTIVE-STATUS | X(01) | Alpha | 1 | Card status (Y=active, N=inactive) |
| 7 | FILLER | X(59) | Filler | 59 | Reserved space |

**Key**: CARD-NUM | **Record Length**: 150 bytes | **VSAM Type**: KSDS

---

## 3. Card-Account Cross-Reference (`CVACT03Y.cpy`) — 50 bytes

Maps cards to accounts, stored in VSAM KSDS (`CARDXREF.VSAM.KSDS`).

| # | Field | PIC Clause | Type | Length | Business Description |
|---|-------|-----------|------|--------|---------------------|
| 1 | XREF-CARD-NUM | X(16) | Alpha | 16 | Credit card number |
| 2 | XREF-ACCT-ID | 9(11) | Numeric | 11 | Associated account ID |
| 3 | FILLER | X(23) | Filler | 23 | Reserved space |

**Key**: XREF-CARD-NUM | **Record Length**: 50 bytes | **Alternate Index on**: XREF-ACCT-ID

---

## 4. Customer Master (`CVCUS01Y.cpy`) — 500 bytes

Customer demographic and contact information in VSAM KSDS (`CUSTDATA.VSAM.KSDS`).

| # | Field | PIC Clause | Type | Length | Business Description |
|---|-------|-----------|------|--------|---------------------|
| 1 | CUST-ID | 9(09) | Numeric | 9 | Unique customer identifier |
| 2 | CUST-FIRST-NAME | X(25) | Alpha | 25 | Customer first name |
| 3 | CUST-MIDDLE-NAME | X(25) | Alpha | 25 | Customer middle name |
| 4 | CUST-LAST-NAME | X(25) | Alpha | 25 | Customer last name |
| 5 | CUST-ADDR-LINE-1 | X(50) | Alpha | 50 | Street address line 1 |
| 6 | CUST-ADDR-LINE-2 | X(50) | Alpha | 50 | Street address line 2 |
| 7 | CUST-ADDR-LINE-3 | X(50) | Alpha | 50 | Street address line 3 |
| 8 | CUST-ADDR-STATE-CD | X(02) | Alpha | 2 | State code |
| 9 | CUST-ADDR-COUNTRY-CD | X(03) | Alpha | 3 | Country code |
| 10 | CUST-ADDR-ZIP | X(10) | Alpha | 10 | ZIP/postal code |
| 11 | CUST-PHONE-NUM-1 | X(15) | Alpha | 15 | Primary phone number |
| 12 | CUST-PHONE-NUM-2 | X(15) | Alpha | 15 | Secondary phone number |
| 13 | CUST-SSN | 9(09) | Numeric | 9 | Social Security Number (PII) |
| 14 | CUST-GOVT-ISSUED-ID | X(20) | Alpha | 20 | Government-issued ID number |
| 15 | CUST-DOB-YYYYMMDD | X(10) | Date String | 10 | Date of birth |
| 16 | CUST-EFT-ACCOUNT-ID | X(10) | Alpha | 10 | EFT/bank account identifier |
| 17 | CUST-PRI-CARD-HOLDER-IND | X(01) | Alpha | 1 | Primary cardholder indicator |
| 18 | CUST-FICO-CREDIT-SCORE | 9(03) | Numeric | 3 | FICO credit score |
| 19 | FILLER | X(168) | Filler | 168 | Reserved space |

**Key**: CUST-ID | **Record Length**: 500 bytes | **PII Fields**: CUST-SSN, CUST-DOB-YYYYMMDD, CUST-GOVT-ISSUED-ID

---

## 5. Transaction Record (`CVTRA05Y.cpy`) — 350 bytes

Individual financial transactions in VSAM KSDS (`TRANSACT.VSAM.KSDS`).

| # | Field | PIC Clause | Type | Length | Business Description |
|---|-------|-----------|------|--------|---------------------|
| 1 | TRAN-ID | X(16) | Alpha | 16 | Unique transaction identifier |
| 2 | TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| 3 | TRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| 4 | TRAN-SOURCE | X(10) | Alpha | 10 | Transaction source/channel |
| 5 | TRAN-DESC | X(100) | Alpha | 100 | Transaction description |
| 6 | TRAN-AMT | S9(09)V99 | Signed Decimal | 11 | Transaction amount |
| 7 | TRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant identifier |
| 8 | TRAN-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| 9 | TRAN-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| 10 | TRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP code |
| 11 | TRAN-CARD-NUM | X(16) | Alpha | 16 | Card number used |
| 12 | TRAN-ORIG-TS | X(26) | Timestamp | 26 | Original transaction timestamp |
| 13 | TRAN-PROC-TS | X(26) | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | X(20) | Filler | 20 | Reserved space |

**Key**: TRAN-ID | **Record Length**: 350 bytes

---

## 6. Daily Transaction Record (`CVTRA06Y.cpy`) — 350 bytes

Incoming daily transactions (pre-posting). Same layout as CVTRA05Y but uses `DALYTRAN-` prefix.

| # | Field | PIC Clause | Type | Length | Business Description |
|---|-------|-----------|------|--------|---------------------|
| 1 | DALYTRAN-ID | X(16) | Alpha | 16 | Daily transaction identifier |
| 2 | DALYTRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| 3 | DALYTRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| 4 | DALYTRAN-SOURCE | X(10) | Alpha | 10 | Source channel |
| 5 | DALYTRAN-DESC | X(100) | Alpha | 100 | Transaction description |
| 6 | DALYTRAN-AMT | S9(09)V99 | Signed Decimal | 11 | Transaction amount |
| 7 | DALYTRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant identifier |
| 8 | DALYTRAN-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| 9 | DALYTRAN-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| 10 | DALYTRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP code |
| 11 | DALYTRAN-CARD-NUM | X(16) | Alpha | 16 | Card number |
| 12 | DALYTRAN-ORIG-TS | X(26) | Timestamp | 26 | Original timestamp |
| 13 | DALYTRAN-PROC-TS | X(26) | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | X(20) | Filler | 20 | Reserved space |

**Key**: DALYTRAN-ID | **Record Length**: 350 bytes | **Lifecycle**: Fed into CBTRN02C posting engine, then merged into TRANSACT master.

---

## 7. Transaction Category Balance (`CVTRA01Y.cpy`) — 50 bytes

Running balance per account per transaction category, in VSAM KSDS (`TCATBALF.VSAM.KSDS`).

| # | Field | PIC Clause | Type | Length | Business Description |
|---|-------|-----------|------|--------|---------------------|
| 1 | TRANCAT-ACCT-ID | 9(11) | Numeric | 11 | Account identifier |
| 2 | TRANCAT-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| 3 | TRANCAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| 4 | TRAN-CAT-BAL | S9(09)V99 | Signed Decimal | 11 | Category balance amount |
| 5 | FILLER | X(22) | Filler | 22 | Reserved space |

**Key**: TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD (composite)

---

## 8. Disclosure Group (`CVTRA02Y.cpy`) — 50 bytes

Interest rate configuration per account group / transaction type, in VSAM KSDS (`DISCGRP.VSAM.KSDS`).

| # | Field | PIC Clause | Type | Length | Business Description |
|---|-------|-----------|------|--------|---------------------|
| 1 | DIS-ACCT-GROUP-ID | X(10) | Alpha | 10 | Account group identifier |
| 2 | DIS-TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| 3 | DIS-TRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| 4 | DIS-INT-RATE | S9(04)V99 | Signed Decimal | 6 | Interest rate percentage |
| 5 | FILLER | X(28) | Filler | 28 | Reserved space |

**Key**: DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD (composite)

---

## 9. Transaction Type (`CVTRA03Y.cpy`) — 60 bytes

Reference table of transaction type codes, in VSAM KSDS (`TRANTYPE.VSAM.KSDS`).

| # | Field | PIC Clause | Type | Length | Business Description |
|---|-------|-----------|------|--------|---------------------|
| 1 | TRAN-TYPE | X(02) | Alpha | 2 | Transaction type code (e.g., "SA", "PU") |
| 2 | TRAN-TYPE-DESC | X(50) | Alpha | 50 | Type description |
| 3 | FILLER | X(08) | Filler | 8 | Reserved space |

**Key**: TRAN-TYPE

---

## 10. Transaction Category Type (`CVTRA04Y.cpy`) — 60 bytes

Sub-categories within each transaction type, in VSAM KSDS (`TRANCATG.VSAM.KSDS`).

| # | Field | PIC Clause | Type | Length | Business Description |
|---|-------|-----------|------|--------|---------------------|
| 1 | TRAN-TYPE-CD | X(02) | Alpha | 2 | Parent transaction type code |
| 2 | TRAN-CAT-CD | 9(04) | Numeric | 4 | Category code within type |
| 3 | TRAN-CAT-TYPE-DESC | X(50) | Alpha | 50 | Category description |
| 4 | FILLER | X(04) | Filler | 4 | Reserved space |

**Key**: TRAN-TYPE-CD + TRAN-CAT-CD (composite)

---

## 11. User Security Record (`CSUSR01Y.cpy`) — 80 bytes

User authentication and authorization data, in VSAM KSDS (`USRSEC.VSAM.KSDS`).

| # | Field | PIC Clause | Type | Length | Business Description |
|---|-------|-----------|------|--------|---------------------|
| 1 | SEC-USR-ID | X(08) | Alpha | 8 | User login ID |
| 2 | SEC-USR-FNAME | X(20) | Alpha | 20 | First name |
| 3 | SEC-USR-LNAME | X(20) | Alpha | 20 | Last name |
| 4 | SEC-USR-PWD | X(08) | Alpha | 8 | Password (plaintext) |
| 5 | SEC-USR-TYPE | X(01) | Alpha | 1 | User type (A=Admin, U=Regular) |
| 6 | SEC-USR-FILLER | X(23) | Filler | 23 | Reserved space |

**Key**: SEC-USR-ID | **Security Note**: Passwords stored in plaintext — critical modernization concern.

---

## 12. Transaction Report Layout (`CVTRA07Y.cpy`)

Print-format layout used by CBTRN03C for the daily transaction report.

| # | Structure | Fields | Business Description |
|---|-----------|--------|---------------------|
| 1 | REPORT-NAME-HEADER | REPT-SHORT-NAME, REPT-LONG-NAME, date range | Report title block |
| 2 | TRANSACTION-DETAIL-REPORT | Trans ID, Account ID, Type, Category, Source, Amount | Detail line per transaction |
| 3 | TRANSACTION-HEADER-1/2 | Column headers and separator | Report column headers |
| 4 | REPORT-PAGE-TOTALS | REPT-PAGE-TOTAL | Per-page amount subtotal |
| 5 | REPORT-ACCOUNT-TOTALS | REPT-ACCOUNT-TOTAL | Per-account amount subtotal |
| 6 | REPORT-GRAND-TOTALS | REPT-GRAND-TOTAL | Grand total of all transactions |

---

## 13. Export Record Layout (`CVEXPORT.cpy`)

Multi-record export file format used by CBEXPORT/CBIMPORT for data migration.

| # | Field | PIC Clause | Type | Length | Business Description |
|---|-------|-----------|------|--------|---------------------|
| 1 | EXP-RECORD-TYPE | X(01) | Alpha | 1 | Record type (C=Customer, A=Account, X=Xref, T=Transaction, D=Card) |
| 2 | (varies by type) | — | — | — | Record-type-specific payload embedded in union |

Supports five record types in a single sequential file for bulk migration.

---

## 14. Common Communication Area (`COCOM01Y.cpy`)

CICS COMMAREA passed between all online programs for session state.

| # | Field | PIC Clause | Type | Length | Business Description |
|---|-------|-----------|------|--------|---------------------|
| 1 | CCARD-AID | X(01) | Alpha | 1 | AID key pressed |
| 2 | CCARD-LAST-PROG | X(08) | Alpha | 8 | Last program executed |
| 3 | CCARD-LAST-MAP | X(07) | Alpha | 7 | Last BMS map displayed |
| 4 | CCARD-NEXT-PROG | X(08) | Alpha | 8 | Next program to transfer to |
| 5 | CCARD-NEXT-MAP | X(07) | Alpha | 7 | Next BMS map to display |
| 6 | CCARD-ACCT-ID | 9(11) | Numeric | 11 | Selected account ID (context) |
| 7 | CCARD-CARD-NUM | X(16) | Alpha | 16 | Selected card number (context) |
| 8 | CCARD-USR-ID | X(08) | Alpha | 8 | Logged-in user ID |
| 9 | CCARD-USR-TYPE | X(01) | Alpha | 1 | User type (A/U) |
| 10 | CDEMO-FROM-PROGRAM | X(08) | Alpha | 8 | Calling program name |

---

## 15. Statement Report Layout (`COSTM01.CPY`)

Alternate transaction record layout keyed by card number + transaction ID for statement generation.

| # | Field | PIC Clause | Type | Length | Business Description |
|---|-------|-----------|------|--------|---------------------|
| 1 | TRNX-CARD-NUM | X(16) | Alpha | 16 | Card number (primary sort key) |
| 2 | TRNX-ID | X(16) | Alpha | 16 | Transaction identifier |
| 3 | TRNX-TYPE-CD | X(02) | Alpha | 2 | Transaction type |
| 4 | TRNX-CAT-CD | 9(04) | Numeric | 4 | Transaction category |
| 5 | TRNX-SOURCE | X(10) | Alpha | 10 | Transaction source |
| 6 | TRNX-DESC | X(100) | Alpha | 100 | Description |
| 7 | TRNX-AMT | S9(09)V99 | Signed Decimal | 11 | Amount |
| 8 | TRNX-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant ID |
| 9 | TRNX-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| 10 | TRNX-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| 11 | TRNX-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP |
| 12 | TRNX-ORIG-TS | X(26) | Timestamp | 26 | Original timestamp |
| 13 | TRNX-PROC-TS | X(26) | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | X(20) | Filler | 20 | Reserved |

**Key**: TRNX-CARD-NUM + TRNX-ID (composite, for card-based reporting)

---

## 16. Entity Relationship Summary

```
CUSTOMER (CVCUS01Y)
  └──< ACCOUNT (CVACT01Y)         [CUST-ID ↔ ACCT-ID via application logic]
        ├──< CARD (CVACT02Y)       [CARD-ACCT-ID → ACCT-ID]
        │     └── XREF (CVACT03Y)  [XREF-CARD-NUM → CARD-NUM, XREF-ACCT-ID → ACCT-ID]
        ├──< TRANSACTION (CVTRA05Y) [TRAN-CARD-NUM → CARD-NUM]
        └──< TRAN-CAT-BAL (CVTRA01Y) [TRANCAT-ACCT-ID → ACCT-ID]

DISCLOSURE-GROUP (CVTRA02Y)
  └── Interest rates by ACCT-GROUP-ID + TRAN-TYPE + TRAN-CAT

TRAN-TYPE (CVTRA03Y) ──< TRAN-CATEGORY (CVTRA04Y)
  └── Reference data for transaction classification

USER-SECURITY (CSUSR01Y)
  └── Authentication & role-based access control

DAILY-TRANSACTION (CVTRA06Y) → [POSTING ENGINE] → TRANSACTION (CVTRA05Y)
```

---

## 17. VSAM File Catalog

| Business Name | VSAM Dataset | Type | Key Field | Record Length | Copybook |
|---------------|-------------|------|-----------|---------------|----------|
| Account Master | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | KSDS | ACCT-ID | 300 | CVACT01Y |
| Card Master | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | KSDS | CARD-NUM | 150 | CVACT02Y |
| Card Cross-Reference | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | KSDS | XREF-CARD-NUM | 50 | CVACT03Y |
| Customer Master | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | KSDS | CUST-ID | 500 | CVCUS01Y |
| Transaction Master | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | KSDS | TRAN-ID | 350 | CVTRA05Y |
| Daily Transactions | AWS.M2.CARDDEMO.DALYTRAN.PS | Sequential | — | 350 | CVTRA06Y |
| Category Balance | AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | KSDS | Composite | 50 | CVTRA01Y |
| Disclosure Group | AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | KSDS | Composite | 50 | CVTRA02Y |
| Transaction Type | AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | KSDS | TRAN-TYPE | 60 | CVTRA03Y |
| Transaction Category | AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | KSDS | Composite | 60 | CVTRA04Y |
| User Security | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | KSDS | SEC-USR-ID | 80 | CSUSR01Y |
| Export Data | AWS.M2.CARDDEMO.EXPORT.DATA | Sequential | — | Variable | CVEXPORT |

---

## 18. Data Quality & Modernization Notes

### PII Fields Requiring Encryption/Masking
- `CUST-SSN` (CVCUS01Y) — Social Security Number
- `CUST-DOB-YYYYMMDD` (CVCUS01Y) — Date of birth
- `CUST-GOVT-ISSUED-ID` (CVCUS01Y) — Government ID
- `SEC-USR-PWD` (CSUSR01Y) — **Password stored in plaintext**
- `CARD-NUM` (CVACT02Y) — Credit card PAN (PCI-DSS scope)
- `CARD-CVV-CD` (CVACT02Y) — CVV code (PCI-DSS scope, should not be stored)

### Date Format Inconsistencies
- Most date fields use `X(10)` free-form strings (no enforced format)
- CUST-DOB uses `YYYYMMDD` convention per field name
- Dates should be standardized to ISO 8601 during modernization

### Numeric Precision
- All monetary amounts use `S9(09)V99` or `S9(10)V99` (implied decimal, 2 places)
- Map to Java `BigDecimal` with scale=2 during conversion
- Interest rates use `S9(04)V99` — 4 integer + 2 decimal digits

### Record Padding
- Every record has significant FILLER space (30-60% of record is unused)
- This was standard practice for VSAM forward-compatibility
- Can be eliminated during modernization to reduce storage footprint
