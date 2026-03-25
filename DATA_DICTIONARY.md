# Data Dictionary -- CardDemo COBOL Codebase

> Extracted from copybook PIC clauses in `app/cpy/`.
> Each entity maps to a VSAM KSDS file on the mainframe and a relational table in the modernized system.

---

## 1. Account Entity (`CVACT01Y.cpy` -- Record Length 300 bytes)

**VSAM File:** `ACCTDAT` (KSDS, keyed on ACCT-ID)
**Business Purpose:** Stores credit card account master data including balances, limits, and lifecycle dates.

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Business Description |
|---|-------------|-----------|------|------|---------------|---------------------|
| 1 | ACCT-ID | PIC 9(11) | Numeric | 11 | Account Number | Unique account identifier |
| 2 | ACCT-ACTIVE-STATUS | PIC X(01) | Alpha | 1 | Account Status | Active/Inactive flag (Y/N) |
| 3 | ACCT-CURR-BAL | PIC S9(10)V99 | Signed Decimal | 12.2 | Current Balance | Outstanding account balance (dollars & cents) |
| 4 | ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12.2 | Credit Limit | Maximum credit line |
| 5 | ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12.2 | Cash Advance Limit | Maximum cash advance allowance |
| 6 | ACCT-OPEN-DATE | PIC X(10) | Date String | 10 | Account Open Date | Date account was opened (YYYY-MM-DD) |
| 7 | ACCT-EXPIRAION-DATE | PIC X(10) | Date String | 10 | Expiration Date | Account expiration date |
| 8 | ACCT-REISSUE-DATE | PIC X(10) | Date String | 10 | Reissue Date | Date card was last reissued |
| 9 | ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Signed Decimal | 12.2 | Cycle Credits | Total credits in current billing cycle |
| 10 | ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 | Signed Decimal | 12.2 | Cycle Debits | Total debits in current billing cycle |
| 11 | ACCT-ADDR-ZIP | PIC X(10) | Alpha | 10 | ZIP Code | Account holder postal code |
| 12 | ACCT-GROUP-ID | PIC X(10) | Alpha | 10 | Account Group | Disclosure/rate group assignment |
| 13 | FILLER | PIC X(178) | Filler | 178 | -- | Reserved space |

---

## 2. Card Entity (`CVACT02Y.cpy` -- Record Length 150 bytes)

**VSAM File:** `CARDDAT` (KSDS, keyed on CARD-NUM)
**Business Purpose:** Stores physical credit card details linked to accounts.

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Business Description |
|---|-------------|-----------|------|------|---------------|---------------------|
| 1 | CARD-NUM | PIC X(16) | Alpha | 16 | Card Number | 16-digit credit card number |
| 2 | CARD-ACCT-ID | PIC 9(11) | Numeric | 11 | Account Number | FK to Account entity |
| 3 | CARD-CVV-CD | PIC 9(03) | Numeric | 3 | CVV Code | Card verification value |
| 4 | CARD-EMBOSSED-NAME | PIC X(50) | Alpha | 50 | Cardholder Name | Name embossed on physical card |
| 5 | CARD-EXPIRAION-DATE | PIC X(10) | Date String | 10 | Card Expiration | Card expiration date |
| 6 | CARD-ACTIVE-STATUS | PIC X(01) | Alpha | 1 | Card Status | Active/Inactive flag |
| 7 | FILLER | PIC X(59) | Filler | 59 | -- | Reserved space |

---

## 3. Customer Entity (`CVCUS01Y.cpy` -- Record Length 500 bytes)

**VSAM File:** `CUSTDAT` (KSDS, keyed on CUST-ID)
**Business Purpose:** Stores customer personal and contact information.

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Business Description |
|---|-------------|-----------|------|------|---------------|---------------------|
| 1 | CUST-ID | PIC 9(09) | Numeric | 9 | Customer ID | Unique customer identifier |
| 2 | CUST-FIRST-NAME | PIC X(25) | Alpha | 25 | First Name | Customer first name |
| 3 | CUST-MIDDLE-NAME | PIC X(25) | Alpha | 25 | Middle Name | Customer middle name |
| 4 | CUST-LAST-NAME | PIC X(25) | Alpha | 25 | Last Name | Customer last name |
| 5 | CUST-ADDR-LINE-1 | PIC X(50) | Alpha | 50 | Address Line 1 | Primary street address |
| 6 | CUST-ADDR-LINE-2 | PIC X(50) | Alpha | 50 | Address Line 2 | Secondary address line |
| 7 | CUST-ADDR-LINE-3 | PIC X(50) | Alpha | 50 | Address Line 3 | Tertiary address line |
| 8 | CUST-ADDR-STATE-CD | PIC X(02) | Alpha | 2 | State Code | US state abbreviation |
| 9 | CUST-ADDR-COUNTRY-CD | PIC X(03) | Alpha | 3 | Country Code | ISO country code |
| 10 | CUST-ADDR-ZIP | PIC X(10) | Alpha | 10 | ZIP Code | Postal code |
| 11 | CUST-PHONE-NUM-1 | PIC X(15) | Alpha | 15 | Primary Phone | Primary contact phone |
| 12 | CUST-PHONE-NUM-2 | PIC X(15) | Alpha | 15 | Secondary Phone | Alternate contact phone |
| 13 | CUST-SSN | PIC 9(09) | Numeric | 9 | SSN | Social Security Number (PII) |
| 14 | CUST-GOVT-ISSUED-ID | PIC X(20) | Alpha | 20 | Government ID | Government-issued identification |
| 15 | CUST-DOB-YYYY-MM-DD | PIC X(10) | Date String | 10 | Date of Birth | Customer date of birth |
| 16 | CUST-EFT-ACCOUNT-ID | PIC X(10) | Alpha | 10 | EFT Account | Electronic funds transfer account |
| 17 | CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alpha | 1 | Primary Holder | Primary cardholder indicator (Y/N) |
| 18 | CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric | 3 | FICO Score | Credit score (300-850) |
| 19 | FILLER | PIC X(168) | Filler | 168 | -- | Reserved space |

---

## 4. Card Cross-Reference (`CVACT03Y.cpy` -- Record Length 50 bytes)

**VSAM File:** `CCXREF` (KSDS, keyed on XREF-CARD-NUM) / Alternate index path `CXACAIX` (keyed on XREF-ACCT-ID)
**Business Purpose:** Links cards to customers and accounts. Enables lookup by card number or account number.

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Business Description |
|---|-------------|-----------|------|------|---------------|---------------------|
| 1 | XREF-CARD-NUM | PIC X(16) | Alpha | 16 | Card Number | FK to Card entity (primary key) |
| 2 | XREF-CUST-ID | PIC 9(09) | Numeric | 9 | Customer ID | FK to Customer entity |
| 3 | XREF-ACCT-ID | PIC 9(11) | Numeric | 11 | Account Number | FK to Account entity |
| 4 | FILLER | PIC X(14) | Filler | 14 | -- | Reserved space |

---

## 5. Transaction Record (`CVTRA05Y.cpy` -- Record Length 350 bytes)

**VSAM File:** `TRANSACT` (KSDS, keyed on TRAN-ID)
**Business Purpose:** Stores all posted financial transactions (purchases, payments, fees).

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Business Description |
|---|-------------|-----------|------|------|---------------|---------------------|
| 1 | TRAN-ID | PIC X(16) | Alpha | 16 | Transaction ID | Unique transaction identifier |
| 2 | TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction Type | Type code (01=Purchase, 02=Payment, etc.) |
| 3 | TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Category Code | Transaction category code |
| 4 | TRAN-SOURCE | PIC X(10) | Alpha | 10 | Source | Origination channel (POS TERM, ONLINE, etc.) |
| 5 | TRAN-DESC | PIC X(100) | Alpha | 100 | Description | Free-text transaction description |
| 6 | TRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11.2 | Amount | Transaction amount (dollars & cents) |
| 7 | TRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant ID | Merchant identifier |
| 8 | TRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant Name | Merchant business name |
| 9 | TRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant City | Merchant location city |
| 10 | TRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant ZIP | Merchant postal code |
| 11 | TRAN-CARD-NUM | PIC X(16) | Alpha | 16 | Card Number | Card used for transaction |
| 12 | TRAN-ORIG-TS | PIC X(26) | Timestamp | 26 | Origination Timestamp | When transaction was initiated |
| 13 | TRAN-PROC-TS | PIC X(26) | Timestamp | 26 | Processing Timestamp | When transaction was processed |
| 14 | FILLER | PIC X(20) | Filler | 20 | -- | Reserved space |

---

## 6. Daily Transaction Record (`CVTRA06Y.cpy` -- Record Length 350 bytes)

**Sequential File:** `DALYTRAN` (input to batch posting)
**Business Purpose:** Incoming daily transaction feed from external sources, validated and posted by CBTRN02C.

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Business Description |
|---|-------------|-----------|------|------|---------------|---------------------|
| 1 | DALYTRAN-ID | PIC X(16) | Alpha | 16 | Transaction ID | Incoming transaction identifier |
| 2 | DALYTRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction Type | Type code |
| 3 | DALYTRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Category Code | Transaction category |
| 4 | DALYTRAN-SOURCE | PIC X(10) | Alpha | 10 | Source | Origination channel |
| 5 | DALYTRAN-DESC | PIC X(100) | Alpha | 100 | Description | Transaction description |
| 6 | DALYTRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11.2 | Amount | Transaction amount |
| 7 | DALYTRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant ID | Merchant identifier |
| 8 | DALYTRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant Name | Merchant name |
| 9 | DALYTRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant City | Merchant city |
| 10 | DALYTRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant ZIP | Merchant postal code |
| 11 | DALYTRAN-CARD-NUM | PIC X(16) | Alpha | 16 | Card Number | Card used |
| 12 | DALYTRAN-ORIG-TS | PIC X(26) | Timestamp | 26 | Origination Timestamp | When initiated |
| 13 | DALYTRAN-PROC-TS | PIC X(26) | Timestamp | 26 | Processing Timestamp | When processed |
| 14 | FILLER | PIC X(20) | Filler | 20 | -- | Reserved space |

---

## 7. Transaction Category Balance (`CVTRA01Y.cpy` -- Record Length 50 bytes)

**VSAM File:** `TCATBAL` (KSDS, keyed on TRAN-CAT-KEY)
**Business Purpose:** Aggregated balance per account per transaction type and category. Used for interest calculations.

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Business Description |
|---|-------------|-----------|------|------|---------------|---------------------|
| 1 | TRANCAT-ACCT-ID | PIC 9(11) | Numeric | 11 | Account Number | FK to Account |
| 2 | TRANCAT-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction Type | Type code |
| 3 | TRANCAT-CD | PIC 9(04) | Numeric | 4 | Category Code | Category code |
| 4 | TRAN-CAT-BAL | PIC S9(09)V99 | Signed Decimal | 11.2 | Category Balance | Accumulated balance for this type+category |
| 5 | FILLER | PIC X(22) | Filler | 22 | -- | Reserved space |

> **Composite Key:** TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD

---

## 8. Disclosure Group / Interest Rate (`CVTRA02Y.cpy` -- Record Length 50 bytes)

**VSAM File:** `DISCGRP` (KSDS, keyed on DIS-GROUP-KEY)
**Business Purpose:** Maps account groups and transaction categories to interest rates. Drives interest calculation in CBACT04C.

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Business Description |
|---|-------------|-----------|------|------|---------------|---------------------|
| 1 | DIS-ACCT-GROUP-ID | PIC X(10) | Alpha | 10 | Account Group ID | Rate group identifier |
| 2 | DIS-TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction Type | Type code |
| 3 | DIS-TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Category Code | Category code |
| 4 | DIS-INT-RATE | PIC S9(04)V99 | Signed Decimal | 6.2 | Interest Rate | Annual percentage rate |
| 5 | FILLER | PIC X(28) | Filler | 28 | -- | Reserved space |

> **Composite Key:** DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD

---

## 9. Transaction Type (`CVTRA03Y.cpy` -- Record Length 60 bytes)

**VSAM File:** `TRANTYPE` (KSDS, keyed on TRAN-TYPE)
**Business Purpose:** Reference table of transaction type codes and their descriptions.

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Business Description |
|---|-------------|-----------|------|------|---------------|---------------------|
| 1 | TRAN-TYPE | PIC X(02) | Alpha | 2 | Type Code | Transaction type code (PK) |
| 2 | TRAN-TYPE-DESC | PIC X(50) | Alpha | 50 | Type Description | Human-readable type name |
| 3 | FILLER | PIC X(08) | Filler | 8 | -- | Reserved space |

---

## 10. Transaction Category Type (`CVTRA04Y.cpy` -- Record Length 60 bytes)

**VSAM File:** `TRANCATG` (KSDS, keyed on TRAN-CAT-KEY)
**Business Purpose:** Reference table mapping type+category combinations to descriptions.

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Business Description |
|---|-------------|-----------|------|------|---------------|---------------------|
| 1 | TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Type Code | Transaction type code |
| 2 | TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Category Code | Transaction category code |
| 3 | TRAN-CAT-TYPE-DESC | PIC X(50) | Alpha | 50 | Category Description | Human-readable category name |
| 4 | FILLER | PIC X(04) | Filler | 4 | -- | Reserved space |

> **Composite Key:** TRAN-TYPE-CD + TRAN-CAT-CD

---

## 11. User Security Record (`CSUSR01Y.cpy` -- Record Length 80 bytes)

**VSAM File:** `USRSEC` (KSDS, keyed on SEC-USR-ID)
**Business Purpose:** Stores user credentials and role assignments for application access control.

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Business Description |
|---|-------------|-----------|------|------|---------------|---------------------|
| 1 | SEC-USR-ID | PIC X(08) | Alpha | 8 | User ID | Login username (PK) |
| 2 | SEC-USR-FNAME | PIC X(20) | Alpha | 20 | First Name | User first name |
| 3 | SEC-USR-LNAME | PIC X(20) | Alpha | 20 | Last Name | User last name |
| 4 | SEC-USR-PWD | PIC X(08) | Alpha | 8 | Password | User password (plain text) |
| 5 | SEC-USR-TYPE | PIC X(01) | Alpha | 1 | User Type | Role: A=Admin, U=Regular User |
| 6 | SEC-USR-FILLER | PIC X(23) | Filler | 23 | -- | Reserved space |

---

## 12. COMMAREA -- Inter-Program Communication (`COCOM01Y.cpy`)

**Storage:** CICS COMMAREA (passed between programs via XCTL)
**Business Purpose:** Carries user context, navigation state, and selected entity IDs between screens.

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Business Description |
|---|-------------|-----------|------|------|---------------|---------------------|
| 1 | CDEMO-FROM-TRANID | PIC X(04) | Alpha | 4 | Source Transaction | Calling transaction ID |
| 2 | CDEMO-FROM-PROGRAM | PIC X(08) | Alpha | 8 | Source Program | Calling program name |
| 3 | CDEMO-TO-TRANID | PIC X(04) | Alpha | 4 | Target Transaction | Destination transaction ID |
| 4 | CDEMO-TO-PROGRAM | PIC X(08) | Alpha | 8 | Target Program | Destination program name |
| 5 | CDEMO-USER-ID | PIC X(08) | Alpha | 8 | User ID | Logged-in user |
| 6 | CDEMO-USER-TYPE | PIC X(01) | Alpha | 1 | User Type | A=Admin, U=User |
| 7 | CDEMO-PGM-CONTEXT | PIC 9(01) | Numeric | 1 | Program Context | 0=Enter, 1=Re-enter |
| 8 | CDEMO-CUST-ID | PIC 9(09) | Numeric | 9 | Customer ID | Selected customer |
| 9 | CDEMO-CUST-FNAME | PIC X(25) | Alpha | 25 | First Name | Customer first name (display) |
| 10 | CDEMO-CUST-MNAME | PIC X(25) | Alpha | 25 | Middle Name | Customer middle name (display) |
| 11 | CDEMO-CUST-LNAME | PIC X(25) | Alpha | 25 | Last Name | Customer last name (display) |
| 12 | CDEMO-ACCT-ID | PIC 9(11) | Numeric | 11 | Account ID | Selected account |
| 13 | CDEMO-ACCT-STATUS | PIC X(01) | Alpha | 1 | Account Status | Selected account status |
| 14 | CDEMO-CARD-NUM | PIC 9(16) | Numeric | 16 | Card Number | Selected card |
| 15 | CDEMO-LAST-MAP | PIC X(7) | Alpha | 7 | Last Map | Previous BMS map name |
| 16 | CDEMO-LAST-MAPSET | PIC X(7) | Alpha | 7 | Last Mapset | Previous BMS mapset name |

---

## 13. Export Record (`CVEXPORT.cpy` -- Record Length 500 bytes)

**Sequential File:** `EXPFILE` (output of CBEXPORT, input to CBIMPORT)
**Business Purpose:** Multi-record-type export format for branch data migration. Uses REDEFINES for polymorphic records.

### Header Fields (Common to All Record Types)

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Business Description |
|---|-------------|-----------|------|------|---------------|---------------------|
| 1 | EXPORT-REC-TYPE | PIC X(1) | Alpha | 1 | Record Type | C=Customer, A=Account, T=Transaction, X=Xref, D=Card |
| 2 | EXPORT-TIMESTAMP | PIC X(26) | Timestamp | 26 | Export Timestamp | When record was exported |
| 3 | EXPORT-SEQUENCE-NUM | PIC 9(9) COMP | Binary | 4 | Sequence Number | Record sequence within export |
| 4 | EXPORT-BRANCH-ID | PIC X(4) | Alpha | 4 | Branch ID | Originating branch |
| 5 | EXPORT-REGION-CODE | PIC X(5) | Alpha | 5 | Region Code | Geographic region |
| 6 | EXPORT-RECORD-DATA | PIC X(460) | Alpha | 460 | Record Data | Entity-specific data (REDEFINES) |

> The EXPORT-RECORD-DATA field is redefined for each entity type (Customer, Account, Transaction, Card, Cross-Reference), mirroring the fields of the corresponding core entity copybooks.

---

## 14. Abend Data (`CSMSG02Y.cpy`)

**Storage:** Working-Storage (error handling)
**Business Purpose:** Captures abnormal termination context for debugging.

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Business Description |
|---|-------------|-----------|------|------|---------------|---------------------|
| 1 | ABEND-CODE | PIC X(4) | Alpha | 4 | Abend Code | System abend code |
| 2 | ABEND-CULPRIT | PIC X(8) | Alpha | 8 | Failing Program | Program that caused the abend |
| 3 | ABEND-REASON | PIC X(50) | Alpha | 50 | Reason | Human-readable error description |
| 4 | ABEND-MSG | PIC X(72) | Alpha | 72 | Error Message | Detailed error message |

---

## 15. Statement Transaction Layout (`COSTM01.CPY`)

**Storage:** Working-Storage (used in CBSTM03A/B for statement generation)
**Business Purpose:** Alternate transaction layout keyed by card number + transaction ID for statement ordering.

| # | COBOL Field | PIC Clause | Type | Size | Business Name | Business Description |
|---|-------------|-----------|------|------|---------------|---------------------|
| 1 | TRNX-CARD-NUM | PIC X(16) | Alpha | 16 | Card Number | Card number (part of composite key) |
| 2 | TRNX-ID | PIC X(16) | Alpha | 16 | Transaction ID | Transaction identifier |
| 3 | TRNX-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction Type | Type code |
| 4 | TRNX-CAT-CD | PIC 9(04) | Numeric | 4 | Category Code | Category code |
| 5 | TRNX-SOURCE | PIC X(10) | Alpha | 10 | Source | Origination channel |
| 6 | TRNX-DESC | PIC X(100) | Alpha | 100 | Description | Transaction description |
| 7 | TRNX-AMT | PIC S9(09)V99 | Signed Decimal | 11.2 | Amount | Transaction amount |
| 8 | TRNX-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant ID | Merchant identifier |
| 9 | TRNX-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant Name | Merchant name |
| 10 | TRNX-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant City | Merchant city |
| 11 | TRNX-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant ZIP | Merchant postal code |
| 12 | TRNX-ORIG-TS | PIC X(26) | Timestamp | 26 | Origination Timestamp | When initiated |
| 13 | TRNX-PROC-TS | PIC X(26) | Timestamp | 26 | Processing Timestamp | When processed |
| 14 | FILLER | PIC X(20) | Filler | 20 | -- | Reserved space |

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)
  |
  |-- 1:M --> Card Cross-Reference (CVACT03Y)
  |               |
  |               |-- M:1 --> Account (CVACT01Y)
  |               |-- M:1 --> Card (CVACT02Y)
  |
  |               Card (CVACT02Y)
  |                 |
  |                 |-- 1:M --> Transaction (CVTRA05Y)
  |
  Account (CVACT01Y)
    |
    |-- 1:M --> Tran Category Balance (CVTRA01Y)
    |              |
    |              |-- M:1 --> Disclosure Group (CVTRA02Y)  [interest rate lookup]
    |
    |-- 1:M --> Transaction (CVTRA05Y)

  Transaction Type (CVTRA03Y) --< Tran Category Type (CVTRA04Y)

  User Security (CSUSR01Y)  [standalone authentication table]
```

### VSAM File to Entity Mapping

| VSAM File DD Name | VSAM Type | Copybook | Entity | Key |
|-------------------|-----------|----------|--------|-----|
| ACCTDAT / ACCTFILE | KSDS | CVACT01Y | Account | ACCT-ID |
| CARDDAT / CARDFILE | KSDS | CVACT02Y | Card | CARD-NUM |
| CUSTDAT / CUSTFILE | KSDS | CVCUS01Y | Customer | CUST-ID |
| CCXREF / XREFFILE | KSDS | CVACT03Y | Card Cross-Ref | XREF-CARD-NUM |
| CXACAIX | KSDS AIX | CVACT03Y | Card Cross-Ref (alt) | XREF-ACCT-ID |
| TRANSACT / TRANFILE | KSDS | CVTRA05Y | Transaction | TRAN-ID |
| DALYTRAN | Sequential | CVTRA06Y | Daily Transaction | -- (sequential) |
| TCATBALF | KSDS | CVTRA01Y | Tran Cat Balance | Composite key |
| DISCGRP | KSDS | CVTRA02Y | Disclosure Group | Composite key |
| TRANTYPE | KSDS | CVTRA03Y | Transaction Type | TRAN-TYPE |
| TRANCATG | KSDS | CVTRA04Y | Tran Category Type | Composite key |
| USRSEC | KSDS | CSUSR01Y | User Security | SEC-USR-ID |
