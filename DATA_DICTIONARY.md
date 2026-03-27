# Data Dictionary -- CardDemo COBOL Codebase

> **Generated:** 2026-03-27 | **Scope:** `uc-legacy-modernization-cobol-to-java`
>
> This document extracts every business entity from the COBOL copybook PIC clauses
> and presents them in a business-friendly format suitable for domain modeling,
> database schema design, and Java POJO/DTO generation during modernization.

---

## Quick Reference -- COBOL PIC Clause Notation

| PIC Pattern | Meaning | Java Equivalent |
|---|---|---|
| `PIC 9(n)` | Unsigned numeric, n digits | `long` or `int` |
| `PIC S9(n)V99` | Signed decimal with 2 implied decimal places | `BigDecimal` |
| `PIC X(n)` | Alphanumeric, n characters | `String` |
| `PIC 9(n) COMP` | Binary integer | `int` / `long` |
| `PIC S9(n)V99 COMP-3` | Packed decimal with 2 decimal places | `BigDecimal` |
| `FILLER PIC X(n)` | Reserved/padding bytes (no business meaning) | *(skip)* |

---

## 1. Account Entity

> **Copybook:** `CVACT01Y.cpy` | **Record:** `ACCOUNT-RECORD` | **Size:** 300 bytes
> **VSAM File:** `ACCTDAT` (KSDS, key = `ACCT-ID`)

| # | Field Name | PIC Clause | Type | Length | Business Description | Constraints / Notes |
|---|------------|------------|------|--------|---------------------|-------------------|
| 1 | ACCT-ID | `PIC 9(11)` | Numeric | 11 digits | **Account Number** -- Unique identifier for the credit card account | Primary key |
| 2 | ACCT-ACTIVE-STATUS | `PIC X(01)` | Alpha | 1 char | **Account Status** -- Active/inactive indicator | `'Y'` = Active, `'N'` = Inactive |
| 3 | ACCT-CURR-BAL | `PIC S9(10)V99` | Decimal | 12 digits + sign | **Current Balance** -- Outstanding balance on the account | Signed; 2 decimal places |
| 4 | ACCT-CREDIT-LIMIT | `PIC S9(10)V99` | Decimal | 12 digits + sign | **Credit Limit** -- Maximum credit extended to the account | Signed; 2 decimal places |
| 5 | ACCT-CASH-CREDIT-LIMIT | `PIC S9(10)V99` | Decimal | 12 digits + sign | **Cash Advance Limit** -- Maximum cash advance allowed | Signed; 2 decimal places |
| 6 | ACCT-OPEN-DATE | `PIC X(10)` | Alpha | 10 chars | **Account Open Date** -- Date the account was opened | Format: `YYYY-MM-DD` |
| 7 | ACCT-EXPIRAION-DATE | `PIC X(10)` | Alpha | 10 chars | **Expiration Date** -- Account/card expiration date | Format: `YYYY-MM-DD` (note: misspelling in source) |
| 8 | ACCT-REISSUE-DATE | `PIC X(10)` | Alpha | 10 chars | **Reissue Date** -- Date the card was last reissued | Format: `YYYY-MM-DD` |
| 9 | ACCT-CURR-CYC-CREDIT | `PIC S9(10)V99` | Decimal | 12 digits + sign | **Current Cycle Credits** -- Total credits (payments) in current billing cycle | Signed; 2 decimal places |
| 10 | ACCT-CURR-CYC-DEBIT | `PIC S9(10)V99` | Decimal | 12 digits + sign | **Current Cycle Debits** -- Total debits (charges) in current billing cycle | Signed; 2 decimal places |
| 11 | ACCT-ADDR-ZIP | `PIC X(10)` | Alpha | 10 chars | **Billing ZIP Code** -- ZIP/postal code for the account | |
| 12 | ACCT-GROUP-ID | `PIC X(10)` | Alpha | 10 chars | **Account Group** -- Group classification for interest rate/discount rules | FK to DISCGRP |
| 13 | FILLER | `PIC X(178)` | -- | 178 bytes | *Reserved space for future expansion* | -- |

**Relationships:**
- One Account has many Cards (via `CVACT02Y`)
- One Account has many Transactions (via `CVTRA05Y`)
- Account Group links to Discount Group rates (via `CVTRA02Y`)

---

## 2. Credit Card Entity

> **Copybook:** `CVACT02Y.cpy` | **Record:** `CARD-RECORD` | **Size:** 150 bytes
> **VSAM File:** `CARDDAT` (KSDS, key = `CARD-NUM`)

| # | Field Name | PIC Clause | Type | Length | Business Description | Constraints / Notes |
|---|------------|------------|------|--------|---------------------|-------------------|
| 1 | CARD-NUM | `PIC X(16)` | Alpha | 16 chars | **Card Number** -- Full 16-digit credit card number | Primary key |
| 2 | CARD-ACCT-ID | `PIC 9(11)` | Numeric | 11 digits | **Account Number** -- Associated account | FK to ACCOUNT-RECORD |
| 3 | CARD-CVV-CD | `PIC 9(03)` | Numeric | 3 digits | **CVV Code** -- Card verification value | Security-sensitive |
| 4 | CARD-EMBOSSED-NAME | `PIC X(50)` | Alpha | 50 chars | **Embossed Name** -- Name printed on the card | |
| 5 | CARD-EXPIRAION-DATE | `PIC X(10)` | Alpha | 10 chars | **Expiration Date** -- Card expiration date | Format: `YYYY-MM-DD` |
| 6 | CARD-ACTIVE-STATUS | `PIC X(01)` | Alpha | 1 char | **Card Status** -- Active/inactive indicator | `'Y'` = Active, `'N'` = Inactive |
| 7 | FILLER | `PIC X(59)` | -- | 59 bytes | *Reserved space* | -- |

**Relationships:**
- Many Cards belong to one Account (`CARD-ACCT-ID` -> `ACCT-ID`)
- Card Number appears in Transactions (`TRAN-CARD-NUM`)
- Card Number is the key in Cross-Reference records

---

## 3. Customer Entity

> **Copybook:** `CVCUS01Y.cpy` (also `CUSTREC.cpy`) | **Record:** `CUSTOMER-RECORD` | **Size:** ~500 bytes
> **VSAM File:** `CUSTDAT` (KSDS, key = `CUST-ID`)

| # | Field Name | PIC Clause | Type | Length | Business Description | Constraints / Notes |
|---|------------|------------|------|--------|---------------------|-------------------|
| 1 | CUST-ID | `PIC 9(09)` | Numeric | 9 digits | **Customer ID** -- Unique customer identifier | Primary key |
| 2 | CUST-FIRST-NAME | `PIC X(25)` | Alpha | 25 chars | **First Name** | |
| 3 | CUST-MIDDLE-NAME | `PIC X(25)` | Alpha | 25 chars | **Middle Name** | |
| 4 | CUST-LAST-NAME | `PIC X(25)` | Alpha | 25 chars | **Last Name** | |
| 5 | CUST-ADDR-LINE-1 | `PIC X(50)` | Alpha | 50 chars | **Address Line 1** | |
| 6 | CUST-ADDR-LINE-2 | `PIC X(50)` | Alpha | 50 chars | **Address Line 2** | |
| 7 | CUST-ADDR-LINE-3 | `PIC X(50)` | Alpha | 50 chars | **Address Line 3** | |
| 8 | CUST-ADDR-STATE-CD | `PIC X(02)` | Alpha | 2 chars | **State Code** -- US state abbreviation | |
| 9 | CUST-ADDR-COUNTRY-CD | `PIC X(03)` | Alpha | 3 chars | **Country Code** -- ISO country code | |
| 10 | CUST-ADDR-ZIP | `PIC X(10)` | Alpha | 10 chars | **ZIP/Postal Code** | |
| 11 | CUST-PHONE-NUM-1 | `PIC X(15)` | Alpha | 15 chars | **Primary Phone** | |
| 12 | CUST-PHONE-NUM-2 | `PIC X(15)` | Alpha | 15 chars | **Secondary Phone** | |
| 13 | CUST-SSN | `PIC 9(09)` | Numeric | 9 digits | **Social Security Number** | PII -- must be encrypted/masked |
| 14 | CUST-GOVT-ISSUED-ID | `PIC X(20)` | Alpha | 20 chars | **Government ID** -- Driver's license or passport | PII |
| 15 | CUST-DOB-YYYY-MM-DD | `PIC X(10)` | Alpha | 10 chars | **Date of Birth** | Format: `YYYY-MM-DD`; PII |
| 16 | CUST-EFT-ACCOUNT-ID | `PIC X(10)` | Alpha | 10 chars | **EFT Account** -- Electronic funds transfer bank account ID | Used for bill payments |
| 17 | CUST-PRI-CARD-HOLDER-IND | `PIC X(01)` | Alpha | 1 char | **Primary Cardholder Indicator** | `'Y'` = Primary, `'N'` = Authorized user |
| 18 | CUST-FICO-CREDIT-SCORE | `PIC 9(03)` | Numeric | 3 digits | **FICO Credit Score** | Range: 300-850 |
| 19 | FILLER | `PIC X(168)` | -- | 168 bytes | *Reserved space* | -- |

**Relationships:**
- One Customer may hold multiple Cards/Accounts
- Customer linked to Account via Cross-Reference (`CVACT03Y`)

---

## 4. Card Cross-Reference Entity

> **Copybook:** `CVACT03Y.cpy` | **Record:** `CARD-XREF-RECORD` | **Size:** ~50 bytes
> **VSAM File:** `CARDXREF` (KSDS, key = `XREF-CARD-NUM`)

| # | Field Name | PIC Clause | Type | Length | Business Description | Constraints / Notes |
|---|------------|------------|------|--------|---------------------|-------------------|
| 1 | XREF-CARD-NUM | `PIC X(16)` | Alpha | 16 chars | **Card Number** -- The credit card number | Primary key; FK to CARD-RECORD |
| 2 | XREF-CUST-ID | `PIC 9(09)` | Numeric | 9 digits | **Customer ID** -- Owner of this card | FK to CUSTOMER-RECORD |
| 3 | XREF-ACCT-ID | `PIC 9(11)` | Numeric | 11 digits | **Account ID** -- Account this card belongs to | FK to ACCOUNT-RECORD |
| 4 | FILLER | `PIC X(14)` | -- | 14 bytes | *Reserved space* | -- |

**Purpose:** Central lookup table that connects Card -> Customer -> Account. Used extensively by transaction processing to resolve card numbers to accounts.

---

## 5. Transaction Entity

> **Copybook:** `CVTRA05Y.cpy` | **Record:** `TRAN-RECORD` | **Size:** 350 bytes
> **VSAM File:** `TRANSACT` (KSDS, key = `TRAN-ID`)

| # | Field Name | PIC Clause | Type | Length | Business Description | Constraints / Notes |
|---|------------|------------|------|--------|---------------------|-------------------|
| 1 | TRAN-ID | `PIC X(16)` | Alpha | 16 chars | **Transaction ID** -- Unique transaction identifier | Primary key |
| 2 | TRAN-TYPE-CD | `PIC X(02)` | Alpha | 2 chars | **Transaction Type Code** -- SA (Sale), RT (Return), CR (Credit), etc. | FK to TRAN-TYPE-RECORD |
| 3 | TRAN-CAT-CD | `PIC 9(04)` | Numeric | 4 digits | **Category Code** -- Merchant category (e.g., 5411 = Grocery) | FK to TRAN-CAT-RECORD |
| 4 | TRAN-SOURCE | `PIC X(10)` | Alpha | 10 chars | **Transaction Source** -- Channel (POS, ATM, ONLINE, etc.) | |
| 5 | TRAN-DESC | `PIC X(100)` | Alpha | 100 chars | **Description** -- Free-text transaction description | |
| 6 | TRAN-AMT | `PIC S9(09)V99` | Decimal | 11 digits + sign | **Transaction Amount** -- Dollar amount | Signed; 2 decimal places |
| 7 | TRAN-MERCHANT-ID | `PIC 9(09)` | Numeric | 9 digits | **Merchant ID** -- Unique merchant identifier | |
| 8 | TRAN-MERCHANT-NAME | `PIC X(50)` | Alpha | 50 chars | **Merchant Name** | |
| 9 | TRAN-MERCHANT-CITY | `PIC X(50)` | Alpha | 50 chars | **Merchant City** | |
| 10 | TRAN-MERCHANT-ZIP | `PIC X(10)` | Alpha | 10 chars | **Merchant ZIP Code** | |
| 11 | TRAN-CARD-NUM | `PIC X(16)` | Alpha | 16 chars | **Card Number** -- Card used for this transaction | FK to CARD-RECORD |
| 12 | TRAN-ORIG-TS | `PIC X(26)` | Alpha | 26 chars | **Origination Timestamp** -- When the transaction was initiated | ISO timestamp |
| 13 | TRAN-PROC-TS | `PIC X(26)` | Alpha | 26 chars | **Processing Timestamp** -- When the transaction was processed | ISO timestamp |
| 14 | FILLER | `PIC X(20)` | -- | 20 bytes | *Reserved space* | -- |

---

## 6. Daily Transaction Entity

> **Copybook:** `CVTRA06Y.cpy` | **Record:** `DALYTRAN-RECORD` | **Size:** 350 bytes
> **VSAM File:** `DALYTRAN` (sequential input for batch posting)

| # | Field Name | PIC Clause | Type | Length | Business Description | Constraints / Notes |
|---|------------|------------|------|--------|---------------------|-------------------|
| 1 | DALYTRAN-ID | `PIC X(16)` | Alpha | 16 chars | **Daily Transaction ID** | Same layout as TRAN-RECORD |
| 2 | DALYTRAN-TYPE-CD | `PIC X(02)` | Alpha | 2 chars | **Transaction Type Code** | |
| 3 | DALYTRAN-CAT-CD | `PIC 9(04)` | Numeric | 4 digits | **Category Code** | |
| 4 | DALYTRAN-SOURCE | `PIC X(10)` | Alpha | 10 chars | **Transaction Source** | |
| 5 | DALYTRAN-DESC | `PIC X(100)` | Alpha | 100 chars | **Description** | |
| 6 | DALYTRAN-AMT | `PIC S9(09)V99` | Decimal | 11 digits + sign | **Amount** | |
| 7 | DALYTRAN-MERCHANT-ID | `PIC 9(09)` | Numeric | 9 digits | **Merchant ID** | |
| 8 | DALYTRAN-MERCHANT-NAME | `PIC X(50)` | Alpha | 50 chars | **Merchant Name** | |
| 9 | DALYTRAN-MERCHANT-CITY | `PIC X(50)` | Alpha | 50 chars | **Merchant City** | |
| 10 | DALYTRAN-MERCHANT-ZIP | `PIC X(10)` | Alpha | 10 chars | **Merchant ZIP** | |
| 11 | DALYTRAN-CARD-NUM | `PIC X(16)` | Alpha | 16 chars | **Card Number** | |
| 12 | DALYTRAN-ORIG-TS | `PIC X(26)` | Alpha | 26 chars | **Origination Timestamp** | |
| 13 | DALYTRAN-PROC-TS | `PIC X(26)` | Alpha | 26 chars | **Processing Timestamp** | |
| 14 | FILLER | `PIC X(20)` | -- | 20 bytes | *Reserved space* | -- |

**Note:** Identical layout to Transaction Entity. Daily transactions are staged here before batch posting (CBTRN02C) merges them into the master TRANSACT file.

---

## 7. Transaction Category Balance Entity

> **Copybook:** `CVTRA01Y.cpy` | **Record:** `TRAN-CAT-BAL-RECORD` | **Size:** ~50 bytes
> **VSAM File:** `TCATBALF` (KSDS, key = `ACCT-ID` + `TYPE-CD` + `CAT-CD`)

| # | Field Name | PIC Clause | Type | Length | Business Description | Constraints / Notes |
|---|------------|------------|------|--------|---------------------|-------------------|
| 1 | TRANCAT-ACCT-ID | `PIC 9(11)` | Numeric | 11 digits | **Account ID** | Part of composite key |
| 2 | TRANCAT-TYPE-CD | `PIC X(02)` | Alpha | 2 chars | **Transaction Type Code** | Part of composite key |
| 3 | TRANCAT-CD | `PIC 9(04)` | Numeric | 4 digits | **Category Code** | Part of composite key |
| 4 | TRAN-CAT-BAL | `PIC S9(09)V99` | Decimal | 11 digits + sign | **Category Balance** -- Running balance for this account+type+category | Used by interest calculation |
| 5 | FILLER | `PIC X(22)` | -- | 22 bytes | *Reserved space* | -- |

---

## 8. Discount/Interest Group Entity

> **Copybook:** `CVTRA02Y.cpy` | **Record:** `DIS-GROUP-RECORD` | **Size:** ~50 bytes
> **VSAM File:** `DISCGRP` (KSDS, key = `GROUP-ID` + `TYPE-CD` + `CAT-CD`)

| # | Field Name | PIC Clause | Type | Length | Business Description | Constraints / Notes |
|---|------------|------------|------|--------|---------------------|-------------------|
| 1 | DIS-ACCT-GROUP-ID | `PIC X(10)` | Alpha | 10 chars | **Account Group ID** -- Links to ACCT-GROUP-ID | Part of composite key |
| 2 | DIS-TRAN-TYPE-CD | `PIC X(02)` | Alpha | 2 chars | **Transaction Type Code** | Part of composite key |
| 3 | DIS-TRAN-CAT-CD | `PIC 9(04)` | Numeric | 4 digits | **Category Code** | Part of composite key |
| 4 | DIS-INT-RATE | `PIC S9(04)V99` | Decimal | 6 digits + sign | **Interest Rate** -- Annual rate for this group+type+category | Percent; 2 decimal places |
| 5 | FILLER | `PIC X(28)` | -- | 28 bytes | *Reserved space* | -- |

**Purpose:** Defines the interest rate applied to each transaction category within an account group. Used by CBACT04C (interest calculation).

---

## 9. Transaction Type Reference Entity

> **Copybook:** `CVTRA03Y.cpy` | **Record:** `TRAN-TYPE-RECORD` | **Size:** 60 bytes
> **VSAM File:** `TRANTYPE` (KSDS, key = `TRAN-TYPE`)

| # | Field Name | PIC Clause | Type | Length | Business Description | Constraints / Notes |
|---|------------|------------|------|--------|---------------------|-------------------|
| 1 | TRAN-TYPE | `PIC X(02)` | Alpha | 2 chars | **Type Code** -- e.g., SA, RT, CR, IN | Primary key |
| 2 | TRAN-TYPE-DESC | `PIC X(50)` | Alpha | 50 chars | **Type Description** -- e.g., "Sale", "Return" | |
| 3 | FILLER | `PIC X(08)` | -- | 8 bytes | *Reserved space* | -- |

---

## 10. Transaction Category Reference Entity

> **Copybook:** `CVTRA04Y.cpy` | **Record:** `TRAN-CAT-RECORD` | **Size:** 60 bytes
> **VSAM File:** `TRANCATG` (KSDS, key = `TYPE-CD` + `CAT-CD`)

| # | Field Name | PIC Clause | Type | Length | Business Description | Constraints / Notes |
|---|------------|------------|------|--------|---------------------|-------------------|
| 1 | TRAN-TYPE-CD | `PIC X(02)` | Alpha | 2 chars | **Type Code** | Part of composite key |
| 2 | TRAN-CAT-CD | `PIC 9(04)` | Numeric | 4 digits | **Category Code** -- e.g., 5411 (Grocery), 5812 (Restaurant) | Part of composite key |
| 3 | TRAN-CAT-TYPE-DESC | `PIC X(50)` | Alpha | 50 chars | **Category Description** | |
| 4 | FILLER | `PIC X(04)` | -- | 4 bytes | *Reserved space* | -- |

---

## 11. User Security Entity

> **Copybook:** `CSUSR01Y.cpy` | **Record:** `SEC-USER-DATA` | **Size:** 80 bytes
> **VSAM File:** `USRSEC` (KSDS, key = `SEC-USR-ID`)

| # | Field Name | PIC Clause | Type | Length | Business Description | Constraints / Notes |
|---|------------|------------|------|--------|---------------------|-------------------|
| 1 | SEC-USR-ID | `PIC X(08)` | Alpha | 8 chars | **User ID** -- Login identifier | Primary key |
| 2 | SEC-USR-FNAME | `PIC X(20)` | Alpha | 20 chars | **First Name** | |
| 3 | SEC-USR-LNAME | `PIC X(20)` | Alpha | 20 chars | **Last Name** | |
| 4 | SEC-USR-PWD | `PIC X(08)` | Alpha | 8 chars | **Password** -- Stored in plain text | **Security risk** -- must hash in modernization |
| 5 | SEC-USR-TYPE | `PIC X(01)` | Alpha | 1 char | **User Type** -- Role classification | `'A'` = Admin, `'U'` = Regular user |
| 6 | SEC-USR-FILLER | `PIC X(23)` | -- | 23 bytes | *Reserved space* | -- |

---

## 12. Export Record Entity

> **Copybook:** `CVEXPORT.cpy` | **Record:** (envelope structure) | **Size:** ~500 bytes
> **File:** Sequential PS (used by CBEXPORT/CBIMPORT)

### 12.1 Export Envelope Header

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|------------|------------|------|--------|---------------------|
| 1 | EXPORT-REC-TYPE | `PIC X(1)` | Alpha | 1 char | **Record Type** -- C=Customer, A=Account, X=Xref, T=Transaction, D=Card |
| 2 | EXPORT-TIMESTAMP | `PIC X(26)` | Alpha | 26 chars | **Export Timestamp** |
| 3 | EXPORT-SEQUENCE-NUM | `PIC 9(9) COMP` | Binary | 4 bytes | **Sequence Number** |
| 4 | EXPORT-BRANCH-ID | `PIC X(4)` | Alpha | 4 chars | **Branch ID** |
| 5 | EXPORT-REGION-CODE | `PIC X(5)` | Alpha | 5 chars | **Region Code** |
| 6 | EXPORT-RECORD-DATA | `PIC X(460)` | Alpha | 460 bytes | **Embedded Record** -- Entity data redefined by type |

### 12.2 Embedded Customer Export

| # | Field Name | PIC Clause | Type | Length |
|---|------------|------------|------|--------|
| 1 | EXP-CUST-ID | `PIC 9(09) COMP` | Binary | 4 bytes |
| 2 | EXP-CUST-FIRST-NAME | `PIC X(25)` | Alpha | 25 chars |
| 3 | EXP-CUST-MIDDLE-NAME | `PIC X(25)` | Alpha | 25 chars |
| 4 | EXP-CUST-LAST-NAME | `PIC X(25)` | Alpha | 25 chars |
| 5 | EXP-CUST-ADDR-LINE (x3) | `PIC X(50)` | Alpha | 50 chars each |
| 6 | EXP-CUST-ADDR-STATE-CD | `PIC X(02)` | Alpha | 2 chars |
| 7 | EXP-CUST-ADDR-COUNTRY-CD | `PIC X(03)` | Alpha | 3 chars |
| 8 | EXP-CUST-ADDR-ZIP | `PIC X(10)` | Alpha | 10 chars |
| 9 | EXP-CUST-PHONE-NUM (x2) | `PIC X(15)` | Alpha | 15 chars each |
| 10 | EXP-CUST-SSN | `PIC 9(09)` | Numeric | 9 digits |
| 11 | EXP-CUST-GOVT-ISSUED-ID | `PIC X(20)` | Alpha | 20 chars |
| 12 | EXP-CUST-DOB-YYYY-MM-DD | `PIC X(10)` | Alpha | 10 chars |
| 13 | EXP-CUST-EFT-ACCOUNT-ID | `PIC X(10)` | Alpha | 10 chars |
| 14 | EXP-CUST-PRI-CARD-HOLDER-IND | `PIC X(01)` | Alpha | 1 char |
| 15 | EXP-CUST-FICO-CREDIT-SCORE | `PIC 9(03) COMP-3` | Packed | 2 bytes |

### 12.3 Embedded Account Export

| # | Field Name | PIC Clause | Type | Length |
|---|------------|------------|------|--------|
| 1 | EXP-ACCT-ID | `PIC 9(11)` | Numeric | 11 digits |
| 2 | EXP-ACCT-ACTIVE-STATUS | `PIC X(01)` | Alpha | 1 char |
| 3 | EXP-ACCT-CURR-BAL | `PIC S9(10)V99 COMP-3` | Packed | 7 bytes |
| 4 | EXP-ACCT-CREDIT-LIMIT | `PIC S9(10)V99` | Decimal | 12+sign |
| 5 | EXP-ACCT-CASH-CREDIT-LIMIT | `PIC S9(10)V99 COMP-3` | Packed | 7 bytes |
| 6 | EXP-ACCT-OPEN-DATE | `PIC X(10)` | Alpha | 10 chars |
| 7 | EXP-ACCT-EXPIRAION-DATE | `PIC X(10)` | Alpha | 10 chars |
| 8 | EXP-ACCT-REISSUE-DATE | `PIC X(10)` | Alpha | 10 chars |
| 9 | EXP-ACCT-CURR-CYC-CREDIT | `PIC S9(10)V99` | Decimal | 12+sign |
| 10 | EXP-ACCT-CURR-CYC-DEBIT | `PIC S9(10)V99 COMP` | Binary | 8 bytes |
| 11 | EXP-ACCT-ADDR-ZIP | `PIC X(10)` | Alpha | 10 chars |
| 12 | EXP-ACCT-GROUP-ID | `PIC X(10)` | Alpha | 10 chars |

---

## 13. Statement Processing Work Areas

> **Copybook:** `COSTM01.CPY` | Used by: `CBSTM03A`

| # | Field Name | PIC Clause | Type | Length | Business Description |
|---|------------|------------|------|--------|---------------------|
| 1 | TRNX-CARD-NUM | `PIC X(16)` | Alpha | 16 chars | Card number for statement line |
| 2 | TRNX-ID | `PIC X(16)` | Alpha | 16 chars | Transaction ID |
| 3 | TRNX-TYPE-CD | `PIC X(02)` | Alpha | 2 chars | Transaction type code |
| 4 | TRNX-CAT-CD | `PIC 9(04)` | Numeric | 4 digits | Category code |
| 5 | TRNX-SOURCE | `PIC X(10)` | Alpha | 10 chars | Transaction source |
| 6 | TRNX-DESC | `PIC X(100)` | Alpha | 100 chars | Transaction description |
| 7 | TRNX-AMT | `PIC S9(09)V99` | Decimal | 11+sign | Transaction amount |
| 8 | TRNX-MERCHANT-ID | `PIC 9(09)` | Numeric | 9 digits | Merchant ID |
| 9 | TRNX-MERCHANT-NAME | `PIC X(50)` | Alpha | 50 chars | Merchant name |
| 10 | TRNX-MERCHANT-CITY | `PIC X(50)` | Alpha | 50 chars | Merchant city |
| 11 | TRNX-MERCHANT-ZIP | `PIC X(10)` | Alpha | 10 chars | Merchant ZIP |
| 12 | TRNX-ORIG-TS | `PIC X(26)` | Alpha | 26 chars | Origination timestamp |
| 13 | TRNX-PROC-TS | `PIC X(26)` | Alpha | 26 chars | Processing timestamp |

---

## 14. Report Layout Fields

> **Copybook:** `CVTRA07Y.cpy` | Used by: `CBTRN03C`

| # | Field Name | PIC Clause | Business Description |
|---|------------|------------|---------------------|
| 1 | REPT-SHORT-NAME | `PIC X(38)` | Report short title |
| 2 | REPT-LONG-NAME | `PIC X(41)` | Report long title |
| 3 | REPT-DATE-HEADER | `PIC X(12)` | "Date Range:" label |
| 4 | REPT-START-DATE | `PIC X(10)` | Report start date |
| 5 | REPT-END-DATE | `PIC X(10)` | Report end date |
| 6 | TRAN-REPORT-TRANS-ID | `PIC X(16)` | Transaction ID column |
| 7 | TRAN-REPORT-ACCOUNT-ID | `PIC X(11)` | Account ID column |
| 8 | TRAN-REPORT-TYPE-CD | `PIC X(02)` | Type code column |
| 9 | TRAN-REPORT-TYPE-DESC | `PIC X(15)` | Type description column |
| 10 | TRAN-REPORT-CAT-CD | `PIC 9(04)` | Category code column |
| 11 | TRAN-REPORT-CAT-DESC | `PIC X(29)` | Category description column |
| 12 | TRAN-REPORT-SOURCE | `PIC X(10)` | Source column |
| 13 | TRAN-REPORT-AMT | `PIC -ZZZ,ZZZ,ZZZ.ZZ` | Amount column (edited) |
| 14 | REPT-PAGE-TOTAL | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Page subtotal |
| 15 | REPT-ACCOUNT-TOTAL | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Account subtotal |
| 16 | REPT-GRAND-TOTAL | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Grand total |

---

## 15. Entity Relationship Summary

```
CUSTOMER (CVCUS01Y)
  |
  |-- 1:N --> CARD-XREF (CVACT03Y)  [XREF-CUST-ID]
                |
                |-- N:1 --> ACCOUNT (CVACT01Y)  [XREF-ACCT-ID]
                |             |
                |             |-- 1:N --> TRAN-CAT-BAL (CVTRA01Y)  [TRANCAT-ACCT-ID]
                |             |
                |             |-- N:1 --> DISC-GROUP (CVTRA02Y)  [ACCT-GROUP-ID]
                |
                |-- N:1 --> CARD (CVACT02Y)  [XREF-CARD-NUM]
                              |
                              |-- 1:N --> TRANSACTION (CVTRA05Y)  [TRAN-CARD-NUM]
                                            |
                                            |-- N:1 --> TRAN-TYPE (CVTRA03Y)  [TRAN-TYPE-CD]
                                            |
                                            |-- N:1 --> TRAN-CAT (CVTRA04Y)  [TYPE-CD + CAT-CD]

USER-SECURITY (CSUSR01Y)  -- standalone authentication entity
```

---

## 16. VSAM File Catalog

| VSAM File | Dataset Name (DSN) | Organization | Key | Record Layout | Size |
|---|---|---|---|---|---|
| ACCTDAT | AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | KSDS | ACCT-ID (11) | CVACT01Y | 300 |
| CARDDAT | AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | KSDS | CARD-NUM (16) | CVACT02Y | 150 |
| CUSTDAT | AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | KSDS | CUST-ID (9) | CVCUS01Y | 500 |
| CARDXREF | AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | KSDS | XREF-CARD-NUM (16) | CVACT03Y | 50 |
| TRANSACT | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | KSDS | TRAN-ID (16) | CVTRA05Y | 350 |
| USRSEC | AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | KSDS | SEC-USR-ID (8) | CSUSR01Y | 80 |
| TRANTYPE | AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS | KSDS | TRAN-TYPE (2) | CVTRA03Y | 60 |
| TRANCATG | AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS | KSDS | TYPE+CAT (6) | CVTRA04Y | 60 |
| TCATBALF | AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | KSDS | ACCT+TYPE+CAT (17) | CVTRA01Y | 50 |
| DISCGRP | AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | KSDS | GROUP+TYPE+CAT (16) | CVTRA02Y | 50 |
| DALYTRAN | AWS.M2.CARDDEMO.DALYTRAN.PS | Sequential | -- | CVTRA06Y | 350 |

---

## 17. Modernization Notes -- PII and Security Flags

The following fields contain Personally Identifiable Information (PII) or pose
security risks and require special handling during modernization:

| Entity | Field | Risk | Recommendation |
|--------|-------|------|----------------|
| Customer | CUST-SSN | PII -- Social Security Number | Encrypt at rest; mask in UI (show last 4 only) |
| Customer | CUST-GOVT-ISSUED-ID | PII -- Government ID | Encrypt at rest |
| Customer | CUST-DOB-YYYY-MM-DD | PII -- Date of Birth | Restrict access |
| Card | CARD-NUM | PCI-DSS -- Full card number | Tokenize; mask in UI (show last 4) |
| Card | CARD-CVV-CD | PCI-DSS -- CVV code | Never store; remove from persistence |
| User Security | SEC-USR-PWD | Plain text password | Hash with bcrypt/scrypt; enforce complexity |
