# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** Copybook PIC clause analysis from `app/cpy/`
> **Purpose:** Business-friendly reference of all data entities, fields, types, and sizes

---

## 1. Account Master Record (`CVACT01Y`)

**VSAM Dataset:** `ACCTDATA.VSAM.KSDS` | **Record Length:** 300 bytes
**Business Purpose:** Stores all credit card account information including balances, limits, and status.

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | ACCT-ID | 9(11) | Numeric | 11 | Account identifier (primary key) |
| 2 | ACCT-ACTIVE-STATUS | X(01) | Alpha | 1 | Account status (Y=Active, N=Inactive) |
| 3 | ACCT-CURR-BAL | S9(10)V99 | Signed Decimal | 12.2 | Current account balance |
| 4 | ACCT-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12.2 | Credit limit |
| 5 | ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | 12.2 | Cash advance credit limit |
| 6 | ACCT-OPEN-DATE | X(10) | Date | 10 | Account open date (YYYY-MM-DD) |
| 7 | ACCT-EXPIRAION-DATE | X(10) | Date | 10 | Account expiration date |
| 8 | ACCT-REISSUE-DATE | X(10) | Date | 10 | Card reissue date |
| 9 | ACCT-CURR-CYC-CREDIT | S9(10)V99 | Signed Decimal | 12.2 | Current cycle credit total |
| 10 | ACCT-CURR-CYC-DEBIT | S9(10)V99 | Signed Decimal | 12.2 | Current cycle debit total |
| 11 | ACCT-ADDR-ZIP | X(10) | Alpha | 10 | Account holder ZIP code |
| 12 | ACCT-GROUP-ID | X(10) | Alpha | 10 | Disclosure/rate group identifier |
| 13 | FILLER | X(178) | Filler | 178 | Reserved for future use |

**Key Relationships:** Linked to cards via `CVACT03Y` (cross-reference). Linked to customers via `CVCUS01Y`.

---

## 2. Card Data Record (`CVACT02Y`)

**VSAM Dataset:** `CARDDATA.VSAM.KSDS` | **Record Length:** 150 bytes
**Business Purpose:** Stores credit card details including card number, embossed name, and status.

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | CARD-NUM | X(16) | Alpha | 16 | Credit card number (primary key part 1) |
| 2 | CARD-ACCT-ID | 9(11) | Numeric | 11 | Associated account ID (primary key part 2) |
| 3 | CARD-CVV-CD | 9(03) | Numeric | 3 | Card verification value (CVV) |
| 4 | CARD-EMBOSSED-NAME | X(50) | Alpha | 50 | Name embossed on card |
| 5 | CARD-EXPIRAION-DATE | X(10) | Date | 10 | Card expiration date (YYYY-MM-DD) |
| 6 | CARD-ACTIVE-STATUS | X(01) | Alpha | 1 | Card status (Y=Active, N=Inactive) |
| 7 | FILLER | X(59) | Filler | 59 | Reserved for future use |

**Key Relationships:** `CARD-ACCT-ID` → `ACCT-ID` in Account Master. Cards are listed/managed through cross-reference (`CVACT03Y`).

---

## 3. Card Cross-Reference Record (`CVACT03Y`)

**VSAM Dataset:** `CARDXREF.VSAM.KSDS` | **Record Length:** ~50 bytes
**Business Purpose:** Maps card numbers to account IDs and customer IDs for quick lookup.

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | XREF-CARD-NUM | X(16) | Alpha | 16 | Credit card number (primary key) |
| 2 | XREF-CUST-ID | 9(09) | Numeric | 9 | Associated customer ID |
| 3 | XREF-ACCT-ID | 9(11) | Numeric | 11 | Associated account ID |
| 4 | FILLER | X(14) | Filler | 14 | Reserved for future use |

**Key Relationships:** `XREF-CARD-NUM` → `CARD-NUM` in Card Data. `XREF-ACCT-ID` → `ACCT-ID` in Account Master. `XREF-CUST-ID` → `CUST-ID` in Customer Master.

---

## 4. Customer Master Record (`CVCUS01Y`)

**VSAM Dataset:** `CUSTDATA.VSAM.KSDS` | **Record Length:** 500 bytes
**Business Purpose:** Contains full customer demographic and contact information.

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | CUST-ID | 9(09) | Numeric | 9 | Customer identifier (primary key) |
| 2 | CUST-FIRST-NAME | X(25) | Alpha | 25 | Customer first name |
| 3 | CUST-MIDDLE-NAME | X(25) | Alpha | 25 | Customer middle name |
| 4 | CUST-LAST-NAME | X(25) | Alpha | 25 | Customer last name |
| 5 | CUST-ADDR-LINE-1 | X(50) | Alpha | 50 | Address line 1 |
| 6 | CUST-ADDR-LINE-2 | X(50) | Alpha | 50 | Address line 2 |
| 7 | CUST-ADDR-LINE-3 | X(50) | Alpha | 50 | Address line 3 |
| 8 | CUST-ADDR-STATE-CD | X(02) | Alpha | 2 | State code |
| 9 | CUST-ADDR-COUNTRY-CD | X(03) | Alpha | 3 | Country code |
| 10 | CUST-ADDR-ZIP | X(10) | Alpha | 10 | ZIP/postal code |
| 11 | CUST-PHONE-NUM-1 | X(15) | Alpha | 15 | Primary phone number |
| 12 | CUST-PHONE-NUM-2 | X(15) | Alpha | 15 | Secondary phone number |
| 13 | CUST-SSN | 9(09) | Numeric | 9 | Social Security Number |
| 14 | CUST-GOVT-ISSUED-ID | X(20) | Alpha | 20 | Government-issued ID |
| 15 | CUST-DOB-YYYYMMDD | X(10) | Date | 10 | Date of birth |
| 16 | CUST-EFT-ACCOUNT-ID | X(10) | Alpha | 10 | EFT account identifier |
| 17 | CUST-PRI-CARD-HOLDER-IND | X(01) | Alpha | 1 | Primary card holder indicator |
| 18 | CUST-FICO-CREDIT-SCORE | 9(03) | Numeric | 3 | FICO credit score |
| 19 | FILLER | X(168) | Filler | 168 | Reserved for future use |

**Key Relationships:** `CUST-ID` ← `XREF-CUST-ID` in Cross-Reference. Customers can have multiple cards/accounts.

---

## 5. Transaction Record (`CVTRA05Y`)

**VSAM Dataset:** `TRANSACT.VSAM.KSDS` | **Record Length:** 350 bytes
**Business Purpose:** Master record for all posted credit card transactions.

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | TRAN-ID | X(16) | Alpha | 16 | Transaction identifier (primary key) |
| 2 | TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code (e.g., SA=Sale) |
| 3 | TRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| 4 | TRAN-SOURCE | X(10) | Alpha | 10 | Transaction source (POS, ATM, Online, etc.) |
| 5 | TRAN-DESC | X(100) | Alpha | 100 | Transaction description |
| 6 | TRAN-AMT | S9(09)V99 | Signed Decimal | 11.2 | Transaction amount |
| 7 | TRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant identifier |
| 8 | TRAN-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| 9 | TRAN-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| 10 | TRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP code |
| 11 | TRAN-CARD-NUM | X(16) | Alpha | 16 | Card number used |
| 12 | TRAN-ORIG-TS | X(26) | Timestamp | 26 | Original transaction timestamp |
| 13 | TRAN-PROC-TS | X(26) | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | X(20) | Filler | 20 | Reserved for future use |

**Key Relationships:** `TRAN-CARD-NUM` → `CARD-NUM` in Card Data / Cross-Reference. `TRAN-TYPE-CD` → `TRAN-TYPE` in Transaction Type. `TRAN-CAT-CD` → `TRAN-CAT-CD` in Transaction Category.

---

## 6. Daily Transaction Record (`CVTRA06Y`)

**File:** `DALYTRAN` (sequential) | **Record Length:** 350 bytes
**Business Purpose:** Incoming daily transaction feed before posting to the transaction master.

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | DALYTRAN-ID | X(16) | Alpha | 16 | Daily transaction identifier |
| 2 | DALYTRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| 3 | DALYTRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code |
| 4 | DALYTRAN-SOURCE | X(10) | Alpha | 10 | Transaction source |
| 5 | DALYTRAN-DESC | X(100) | Alpha | 100 | Transaction description |
| 6 | DALYTRAN-AMT | S9(09)V99 | Signed Decimal | 11.2 | Transaction amount |
| 7 | DALYTRAN-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant identifier |
| 8 | DALYTRAN-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| 9 | DALYTRAN-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| 10 | DALYTRAN-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP code |
| 11 | DALYTRAN-CARD-NUM | X(16) | Alpha | 16 | Card number used |
| 12 | DALYTRAN-ORIG-TS | X(26) | Timestamp | 26 | Original timestamp |
| 13 | DALYTRAN-PROC-TS | X(26) | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | X(20) | Filler | 20 | Reserved for future use |

**Notes:** Identical layout to Transaction Record (CVTRA05Y). Validated and posted by batch programs CBTRN01C/CBTRN02C.

---

## 7. Transaction Category Balance (`CVTRA01Y`)

**VSAM Dataset:** `TCATBALF.VSAM.KSDS` | **Record Length:** 50 bytes
**Business Purpose:** Running balance per account per transaction type/category for interest calculation.

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | TRANCAT-ACCT-ID | 9(11) | Numeric | 11 | Account identifier (key part 1) |
| 2 | TRANCAT-TYPE-CD | X(02) | Alpha | 2 | Transaction type code (key part 2) |
| 3 | TRANCAT-CD | 9(04) | Numeric | 4 | Transaction category code (key part 3) |
| 4 | TRAN-CAT-BAL | S9(09)V99 | Signed Decimal | 11.2 | Category balance amount |
| 5 | FILLER | X(22) | Filler | 22 | Reserved |

**Key Relationships:** Composite key links to Account Master and Transaction Type/Category.

---

## 8. Disclosure Group Record (`CVTRA02Y`)

**VSAM Dataset:** `DISCGRP.VSAM.KSDS` | **Record Length:** 50 bytes
**Business Purpose:** Defines interest rates per disclosure group, transaction type, and category.

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | DIS-ACCT-GROUP-ID | X(10) | Alpha | 10 | Disclosure group identifier (key part 1) |
| 2 | DIS-TRAN-TYPE-CD | X(02) | Alpha | 2 | Transaction type code (key part 2) |
| 3 | DIS-TRAN-CAT-CD | 9(04) | Numeric | 4 | Transaction category code (key part 3) |
| 4 | DIS-INT-RATE | S9(04)V99 | Signed Decimal | 6.2 | Interest rate percentage |
| 5 | FILLER | X(28) | Filler | 28 | Reserved |

**Notes:** Used by interest calculation batch (CBACT04C) to determine rates per account group.

---

## 9. Transaction Type Record (`CVTRA03Y`)

**VSAM Dataset:** `TRANTYPE.VSAM.KSDS` | **Record Length:** 60 bytes
**Business Purpose:** Lookup table for transaction type codes and descriptions.

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | TRAN-TYPE | X(02) | Alpha | 2 | Transaction type code (primary key) |
| 2 | TRAN-TYPE-DESC | X(50) | Alpha | 50 | Type description (e.g., "Sale", "Return") |
| 3 | FILLER | X(08) | Filler | 8 | Reserved |

---

## 10. Transaction Category Record (`CVTRA04Y`)

**VSAM Dataset:** `TRANCATG.VSAM.KSDS` | **Record Length:** 60 bytes
**Business Purpose:** Lookup table for transaction category codes within a type.

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | TRAN-TYPE-CD | X(02) | Alpha | 2 | Parent transaction type (key part 1) |
| 2 | TRAN-CAT-CD | 9(04) | Numeric | 4 | Category code (key part 2) |
| 3 | TRAN-CAT-TYPE-DESC | X(50) | Alpha | 50 | Category description |
| 4 | FILLER | X(04) | Filler | 4 | Reserved |

---

## 11. User Security Record (`CSUSR01Y`)

**VSAM Dataset:** `USRSEC.VSAM.KSDS` | **Record Length:** ~80 bytes
**Business Purpose:** Stores user credentials and role for application authentication.

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | SEC-USR-ID | X(08) | Alpha | 8 | User ID (primary key) |
| 2 | SEC-USR-FNAME | X(20) | Alpha | 20 | First name |
| 3 | SEC-USR-LNAME | X(20) | Alpha | 20 | Last name |
| 4 | SEC-USR-PWD | X(08) | Alpha | 8 | Password |
| 5 | SEC-USR-TYPE | X(01) | Alpha | 1 | User type (A=Admin, U=Regular) |
| 6 | SEC-USR-FILLER | X(23) | Filler | 23 | Reserved |

**Notes:** Accessed by COSGN00C for authentication. Managed by COUSR00-03C (Admin CRUD).

---

## 12. Transaction Report Layout (`CVTRA07Y`)

**Output:** Report files | **Business Purpose:** Defines printed transaction detail report format.

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | REPT-SHORT-NAME | X(38) | Alpha | 38 | Report short name ("DALYREPT") |
| 2 | REPT-LONG-NAME | X(41) | Alpha | 41 | Report title ("Daily Transaction Report") |
| 3 | REPT-DATE-HEADER | X(12) | Alpha | 12 | Date range header |
| 4 | REPT-START-DATE | X(10) | Date | 10 | Report start date |
| 5 | REPT-END-DATE | X(10) | Date | 10 | Report end date |
| 6 | TRAN-REPORT-TRANS-ID | X(16) | Alpha | 16 | Transaction ID |
| 7 | TRAN-REPORT-ACCOUNT-ID | X(11) | Alpha | 11 | Account ID |
| 8 | TRAN-REPORT-TYPE-CD | X(02) | Alpha | 2 | Transaction type |
| 9 | TRAN-REPORT-TYPE-DESC | X(15) | Alpha | 15 | Type description |
| 10 | TRAN-REPORT-CAT-CD | 9(04) | Numeric | 4 | Category code |
| 11 | TRAN-REPORT-CAT-DESC | X(29) | Alpha | 29 | Category description |
| 12 | TRAN-REPORT-SOURCE | X(10) | Alpha | 10 | Transaction source |
| 13 | TRAN-REPORT-AMT | -ZZZ,ZZZ,ZZZ.ZZ | Edited Numeric | 15 | Formatted amount |

---

## 13. Statement Transaction Layout (`COSTM01`)

**Output:** Statement files | **Business Purpose:** Restructured transaction record for statement generation, keyed by card + transaction ID.

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | TRNX-CARD-NUM | X(16) | Alpha | 16 | Card number (key part 1) |
| 2 | TRNX-ID | X(16) | Alpha | 16 | Transaction ID (key part 2) |
| 3 | TRNX-TYPE-CD | X(02) | Alpha | 2 | Transaction type code |
| 4 | TRNX-CAT-CD | 9(04) | Numeric | 4 | Transaction category |
| 5 | TRNX-SOURCE | X(10) | Alpha | 10 | Transaction source |
| 6 | TRNX-DESC | X(100) | Alpha | 100 | Description |
| 7 | TRNX-AMT | S9(09)V99 | Signed Decimal | 11.2 | Amount |
| 8 | TRNX-MERCHANT-ID | 9(09) | Numeric | 9 | Merchant ID |
| 9 | TRNX-MERCHANT-NAME | X(50) | Alpha | 50 | Merchant name |
| 10 | TRNX-MERCHANT-CITY | X(50) | Alpha | 50 | Merchant city |
| 11 | TRNX-MERCHANT-ZIP | X(10) | Alpha | 10 | Merchant ZIP |
| 12 | TRNX-ORIG-TS | X(26) | Timestamp | 26 | Original timestamp |
| 13 | TRNX-PROC-TS | X(26) | Timestamp | 26 | Processing timestamp |
| 14 | FILLER | X(20) | Filler | 20 | Reserved |

---

## 14. Export/Import Record (`CVEXPORT`)

**File:** Export flat file | **Business Purpose:** Composite record for data migration containing all entity types.

### Header Record
| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | EXP-HEADER-REC-TYPE | X(01) | Alpha | 1 | Record type = "H" |
| 2 | EXP-HEADER-EXPORT-DATE | X(10) | Date | 10 | Export date |
| 3 | EXP-HEADER-EXPORT-TIME | X(08) | Alpha | 8 | Export time |
| 4 | EXP-HEADER-SOURCE-SYS | X(08) | Alpha | 8 | Source system ID |

### Customer Sub-Record
| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | EXP-CUST-REC-TYPE | X(01) | Alpha | 1 | Record type = "C" |
| 2 | EXP-CUST-ID | 9(09) | Numeric | 9 | Customer ID |
| 3 | EXP-CUST-FIRST-NAME | X(25) | Alpha | 25 | First name |
| 4 | EXP-CUST-LAST-NAME | X(25) | Alpha | 25 | Last name |
| 5+ | *(additional fields mirror CVCUS01Y)* | | | | |

### Account Sub-Record
| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | EXP-ACCT-REC-TYPE | X(01) | Alpha | 1 | Record type = "A" |
| 2 | EXP-ACCT-ID | 9(11) | Numeric | 11 | Account ID |
| 3+ | *(additional fields mirror CVACT01Y)* | | | | |

### Card Sub-Record
| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | EXP-CARD-REC-TYPE | X(01) | Alpha | 1 | Record type = "D" |
| 2 | EXP-CARD-NUM | X(16) | Alpha | 16 | Card number |
| 3+ | *(additional fields mirror CVACT02Y)* | | | | |

---

## 15. Credit Card Work Area (`CVCRD01Y`)

**Purpose:** In-memory work area used by online CICS programs for card operations.

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | CC-ACCT-ID | 9(11) | Numeric | 11 | Working account ID |
| 2 | CC-CARD-NUM | X(16) | Alpha | 16 | Working card number |
| 3 | CC-CUST-ID | 9(09) | Numeric | 9 | Working customer ID |
| 4 | CC-WORK-AREA | *(various)* | Group | Variable | Temporary processing fields |

---

## 16. Common Application Area (`COCOM01Y`)

**Purpose:** COMMAREA passed between all CICS programs for session state.

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|-----------|-----------|------|------|---------------------|
| 1 | CDEMO-FROM-TRANID | X(04) | Alpha | 4 | Source transaction ID |
| 2 | CDEMO-FROM-PROGRAM | X(08) | Alpha | 8 | Source program name |
| 3 | CDEMO-TO-TRANID | X(04) | Alpha | 4 | Target transaction ID |
| 4 | CDEMO-TO-PROGRAM | X(08) | Alpha | 8 | Target program name |
| 5 | CDEMO-USER-ID | X(08) | Alpha | 8 | Signed-on user ID |
| 6 | CDEMO-USER-TYPE | X(01) | Alpha | 1 | User type (A/U) |
| 7 | CDEMO-PGM-CONTEXT | 9(01) | Numeric | 1 | Program context flag |
| 8 | CDEMO-ACCT-ID | 9(11) | Numeric | 11 | Current account ID |
| 9 | CDEMO-CARD-NUM | X(16) | Alpha | 16 | Current card number |
| 10 | CDEMO-CUST-ID | 9(09) | Numeric | 9 | Current customer ID |
| 11 | CDEMO-LAST-MAP | X(07) | Alpha | 7 | Last displayed map |
| 12 | CDEMO-LAST-MAPSET | X(07) | Alpha | 7 | Last displayed mapset |
| 13 | CCARD-AID | X(01) | Alpha | 1 | Attention identifier (PF key) |
| 14 | CCARD-ERROR-MSG | X(75) | Alpha | 75 | Error message buffer |

---

## Entity-Relationship Summary

```
┌─────────────────┐     1:N     ┌──────────────────┐
│   CUSTOMER       │────────────│   CROSS-REF       │
│   (CVCUS01Y)     │            │   (CVACT03Y)      │
│   Key: CUST-ID   │            │   Key: CARD-NUM   │
└─────────────────┘            └──────────────────┘
                                    │           │
                                    │ N:1       │ N:1
                                    ▼           ▼
                              ┌──────────┐ ┌──────────────┐
                              │  CARD     │ │  ACCOUNT      │
                              │ (CVACT02Y)│ │ (CVACT01Y)    │
                              │Key: CARD# │ │Key: ACCT-ID   │
                              │    +ACCT  │ └──────────────┘
                              └──────────┘       │
                                    │            │ 1:N
                                    │            ▼
                                    │    ┌────────────────┐
                                    │    │  TRAN-CAT-BAL   │
                                    │    │  (CVTRA01Y)     │
                                    │    │Key: ACCT+TYPE   │
                                    │    │     +CAT        │
                                    │    └────────────────┘
                                    │
                                    │ 1:N
                                    ▼
                              ┌──────────────────┐
                              │  TRANSACTION       │
                              │  (CVTRA05Y)        │
                              │  Key: TRAN-ID      │
                              │  FK: TRAN-CARD-NUM │
                              └──────────────────┘
                                    │
                                    │ N:1          N:1
                                    ▼               ▼
                              ┌──────────────┐ ┌──────────────┐
                              │  TRAN-TYPE    │ │  TRAN-CAT     │
                              │  (CVTRA03Y)   │ │  (CVTRA04Y)   │
                              │Key: TYPE-CD   │ │Key: TYPE+CAT  │
                              └──────────────┘ └──────────────┘

┌─────────────────┐        ┌──────────────────┐
│  USER SECURITY   │        │  DISCLOSURE GRP   │
│  (CSUSR01Y)      │        │  (CVTRA02Y)       │
│  Key: USR-ID     │        │  Key: GRP+TYPE    │
└─────────────────┘        │       +CAT         │
                            └──────────────────┘
```

---

## VSAM Dataset Summary

| Dataset Name | Record Type | Key Field(s) | Record Length | Copybook |
|-------------|-------------|-------------|---------------|----------|
| ACCTDATA.VSAM.KSDS | Account | ACCT-ID | 300 | CVACT01Y |
| CARDDATA.VSAM.KSDS | Card | CARD-NUM + CARD-ACCT-ID | 150 | CVACT02Y |
| CARDXREF.VSAM.KSDS | Cross-Ref | XREF-CARD-NUM | ~50 | CVACT03Y |
| CUSTDATA.VSAM.KSDS | Customer | CUST-ID | 500 | CVCUS01Y |
| TRANSACT.VSAM.KSDS | Transaction | TRAN-ID | 350 | CVTRA05Y |
| USRSEC.VSAM.KSDS | User Security | SEC-USR-ID | ~80 | CSUSR01Y |
| TCATBALF.VSAM.KSDS | Category Balance | ACCT+TYPE+CAT | 50 | CVTRA01Y |
| DISCGRP.VSAM.KSDS | Disclosure | GRP+TYPE+CAT | 50 | CVTRA02Y |
| TRANTYPE.VSAM.KSDS | Tran Type | TRAN-TYPE | 60 | CVTRA03Y |
| TRANCATG.VSAM.KSDS | Tran Category | TYPE+CAT | 60 | CVTRA04Y |
