# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** Copybook PIC clause analysis
> **Application:** CardDemo (Credit Card Management System)

---

## Summary of Business Entities

| Entity | Copybook | Record Length | VSAM File | Key |
|---|---|---|---|---|
| Account | CVACT01Y | 300 bytes | ACCTDATA.VSAM.KSDS | Account ID (11 digits) |
| Card | CVACT02Y | 150 bytes | CARDDATA.VSAM.KSDS | Card Number (16 chars) |
| Card Cross-Reference | CVACT03Y | 50 bytes | CARDXREF.VSAM.KSDS | Card Number (16 chars) |
| Customer | CVCUS01Y | 500 bytes | CUSTDATA.VSAM.KSDS | Customer ID (9 digits) |
| Transaction | CVTRA05Y | 350 bytes | TRANSACT.VSAM.KSDS | Transaction ID (16 chars) |
| Daily Transaction | CVTRA06Y | 350 bytes | DALYTRAN.PS | Transaction ID (16 chars) |
| Transaction Category Balance | CVTRA01Y | 50 bytes | TCATBALF.VSAM.KSDS | Account ID + Type + Category |
| Disclosure Group | CVTRA02Y | 50 bytes | DISCGRP.VSAM.KSDS | Group ID + Type + Category |
| Transaction Type | CVTRA03Y | 60 bytes | TRANTYPE.VSAM.KSDS | Type Code (2 chars) |
| Transaction Category | CVTRA04Y | 60 bytes | TRANCATG.VSAM.KSDS | Type Code + Category Code |
| User Security | CSUSR01Y | 80 bytes | USRSEC.VSAM.KSDS | User ID (8 chars) |
| Export Record | CVEXPORT | Variable | Export flat file | Record Type + Key |
| Statement Transaction | COSTM01 | 350 bytes | TRXFL.VSAM.KSDS | Card Number + Transaction ID |

---

## 1. Account Entity (`CVACT01Y.cpy`)

**Business Purpose:** Stores credit card account master data including balances, credit limits, status, and key dates.

**VSAM Dataset:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`
**Record Length:** 300 bytes | **Key:** Account ID (11 digits, position 1)

| Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|
| ACCT-ID | PIC 9(11) | Numeric | 11 | **Primary Key.** Unique account identifier |
| ACCT-ACTIVE-STATUS | PIC X(01) | Alpha | 1 | Account status: 'Y' = Active, 'N' = Inactive |
| ACCT-CURR-BAL | PIC S9(10)V99 | Signed Decimal | 12 | Current account balance (dollars and cents) |
| ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12 | Maximum credit limit |
| ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12 | Cash advance credit limit |
| ACCT-OPEN-DATE | PIC X(10) | Alpha | 10 | Date account was opened (YYYY-MM-DD) |
| ACCT-EXPIRAION-DATE | PIC X(10) | Alpha | 10 | Account expiration date |
| ACCT-REISSUE-DATE | PIC X(10) | Alpha | 10 | Last card reissue date |
| ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Signed Decimal | 12 | Credits in current billing cycle |
| ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 | Signed Decimal | 12 | Debits in current billing cycle |
| ACCT-ADDR-ZIP | PIC X(10) | Alpha | 10 | Account holder ZIP code |
| ACCT-GROUP-ID | PIC X(10) | Alpha | 10 | Disclosure/interest rate group |
| FILLER | PIC X(178) | Alpha | 178 | Reserved for future use |

**Business Rules:**
- Balance = Previous Balance + Debits - Credits + Interest
- Credit limit enforcement is checked during transaction add (COTRN02C)
- Interest is calculated by CBACT04C using the ACCT-GROUP-ID to look up rates

---

## 2. Card Entity (`CVACT02Y.cpy`)

**Business Purpose:** Stores credit card details including card number, status, and embossed name.

**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`
**Record Length:** 150 bytes | **Key:** Card Number (16 chars, position 1)

| Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|
| CARD-NUM | PIC X(16) | Alpha | 16 | **Primary Key.** 16-digit credit card number |
| CARD-ACCT-ID | PIC 9(11) | Numeric | 11 | FK to Account -- links card to account |
| CARD-CVV-CD | PIC 9(03) | Numeric | 3 | Card verification value (CVV) |
| CARD-EMBOSSED-NAME | PIC X(50) | Alpha | 50 | Name embossed on physical card |
| CARD-EXPIRAION-DATE | PIC X(10) | Alpha | 10 | Card expiration date |
| CARD-ACTIVE-STATUS | PIC X(01) | Alpha | 1 | Card status: 'Y' = Active, 'N' = Inactive |
| FILLER | PIC X(59) | Alpha | 59 | Reserved for future use |

**Business Rules:**
- Multiple cards can belong to one account
- Card status is independent of account status
- Expiration date is validated during card update (COCRDUPC)

---

## 3. Card Cross-Reference Entity (`CVACT03Y.cpy`)

**Business Purpose:** Maps card numbers to account IDs and customer IDs, enabling lookups in either direction via alternate index.

**VSAM Dataset:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`
**Record Length:** 50 bytes | **Key:** Card Number (16 chars, position 1) | **Alternate Index:** Account ID (11 digits, position 26)

| Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|
| XREF-CARD-NUM | PIC X(16) | Alpha | 16 | **Primary Key.** Credit card number |
| XREF-CUST-ID | PIC 9(09) | Numeric | 9 | FK to Customer |
| XREF-ACCT-ID | PIC 9(11) | Numeric | 11 | FK to Account (alternate index key) |
| FILLER | PIC X(14) | Alpha | 14 | Reserved |

**Business Rules:**
- One card maps to exactly one account and one customer
- Alternate index allows browsing all cards for a given account
- Used extensively in transaction posting to resolve card-to-account

---

## 4. Customer Entity (`CVCUS01Y.cpy`)

**Business Purpose:** Stores customer demographic and contact information.

**VSAM Dataset:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`
**Record Length:** 500 bytes | **Key:** Customer ID (9 digits, position 1)

| Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|
| CUST-ID | PIC 9(09) | Numeric | 9 | **Primary Key.** Unique customer identifier |
| CUST-FIRST-NAME | PIC X(25) | Alpha | 25 | Customer first name |
| CUST-MIDDLE-NAME | PIC X(25) | Alpha | 25 | Customer middle name |
| CUST-LAST-NAME | PIC X(25) | Alpha | 25 | Customer last name |
| CUST-ADDR-LINE-1 | PIC X(50) | Alpha | 50 | Address line 1 |
| CUST-ADDR-LINE-2 | PIC X(50) | Alpha | 50 | Address line 2 |
| CUST-ADDR-LINE-3 | PIC X(50) | Alpha | 50 | Address line 3 |
| CUST-ADDR-STATE-CD | PIC X(02) | Alpha | 2 | State code |
| CUST-ADDR-COUNTRY-CD | PIC X(03) | Alpha | 3 | Country code |
| CUST-ADDR-ZIP | PIC X(10) | Alpha | 10 | ZIP/postal code |
| CUST-PHONE-NUM-1 | PIC X(15) | Alpha | 15 | Primary phone number |
| CUST-PHONE-NUM-2 | PIC X(15) | Alpha | 15 | Secondary phone number |
| CUST-SSN | PIC 9(09) | Numeric | 9 | Social Security Number (**PII - sensitive**) |
| CUST-GOVT-ISSUED-ID | PIC X(20) | Alpha | 20 | Government-issued ID number (**PII - sensitive**) |
| CUST-DOB-YYYYMMDD | PIC X(10) | Alpha | 10 | Date of birth (**PII - sensitive**) |
| CUST-EFT-ACCOUNT-ID | PIC X(10) | Alpha | 10 | Electronic funds transfer account |
| CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alpha | 1 | Primary card holder indicator |
| CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric | 3 | FICO credit score |
| FILLER | PIC X(168) | Alpha | 168 | Reserved for future use |

**Business Rules:**
- Customer is the top-level entity; accounts and cards hang off customer
- SSN, DOB, and government ID are PII fields -- must be protected during migration
- FICO score influences credit limit decisions

---

## 5. Transaction Entity (`CVTRA05Y.cpy`)

**Business Purpose:** Records individual credit card transactions (purchases, payments, adjustments).

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`
**Record Length:** 350 bytes | **Key:** Transaction ID (16 chars, position 1)

| Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|
| TRAN-ID | PIC X(16) | Alpha | 16 | **Primary Key.** Unique transaction identifier |
| TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code (FK to TRANTYPE) |
| TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code |
| TRAN-SOURCE | PIC X(10) | Alpha | 10 | Transaction source (POS, ATM, ONLINE, etc.) |
| TRAN-DESC | PIC X(100) | Alpha | 100 | Transaction description |
| TRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11 | Transaction amount (signed: +debit, -credit) |
| TRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant identifier |
| TRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant name |
| TRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant city |
| TRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant ZIP code |
| TRAN-CARD-NUM | PIC X(16) | Alpha | 16 | Card number used for transaction |
| TRAN-ORIG-TS | PIC X(26) | Alpha | 26 | Original transaction timestamp |
| TRAN-PROC-TS | PIC X(26) | Alpha | 26 | Processing timestamp |
| FILLER | PIC X(20) | Alpha | 20 | Reserved |

**Business Rules:**
- Daily transactions (CVTRA06Y) are posted to this master file by CBTRN02C
- Transaction amount is signed: positive = charge, negative = payment/credit
- Card number links to XREF for account resolution

---

## 6. Daily Transaction Entity (`CVTRA06Y.cpy`)

**Business Purpose:** Holds incoming daily transactions before batch posting to the master transaction file.

**Dataset:** `AWS.M2.CARDDEMO.DALYTRAN.PS` (sequential flat file)
**Record Length:** 350 bytes | **Same layout as CVTRA05Y with DALYTRAN- prefix**

| Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|
| DALYTRAN-ID | PIC X(16) | Alpha | 16 | Daily transaction identifier |
| DALYTRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code |
| DALYTRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code |
| DALYTRAN-SOURCE | PIC X(10) | Alpha | 10 | Transaction source |
| DALYTRAN-DESC | PIC X(100) | Alpha | 100 | Transaction description |
| DALYTRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11 | Transaction amount |
| DALYTRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant identifier |
| DALYTRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant name |
| DALYTRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant city |
| DALYTRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant ZIP code |
| DALYTRAN-CARD-NUM | PIC X(16) | Alpha | 16 | Card number |
| DALYTRAN-ORIG-TS | PIC X(26) | Alpha | 26 | Original timestamp |
| DALYTRAN-PROC-TS | PIC X(26) | Alpha | 26 | Processing timestamp |
| FILLER | PIC X(20) | Alpha | 20 | Reserved |

**Business Rules:**
- Input to POSTTRAN batch job (CBTRN02C)
- Records failing validation are written to DALYREJS reject file
- After posting, daily file is cleared for next cycle

---

## 7. Transaction Category Balance Entity (`CVTRA01Y.cpy`)

**Business Purpose:** Tracks running balance per account per transaction type/category for interest calculation.

**VSAM Dataset:** `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS`
**Record Length:** 50 bytes | **Key:** Account ID + Type Code + Category Code

| Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|
| TRANCAT-ACCT-ID | PIC 9(11) | Numeric | 11 | Account identifier (part of composite key) |
| TRANCAT-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code (part of composite key) |
| TRANCAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code (part of composite key) |
| TRAN-CAT-BAL | PIC S9(09)V99 | Signed Decimal | 11 | Running balance for this type/category |
| FILLER | PIC X(22) | Alpha | 22 | Reserved |

---

## 8. Disclosure Group Entity (`CVTRA02Y.cpy`)

**Business Purpose:** Defines interest rates per account group, transaction type, and category.

**VSAM Dataset:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`
**Record Length:** 50 bytes | **Key:** Group ID + Type Code + Category Code

| Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|
| DIS-ACCT-GROUP-ID | PIC X(10) | Alpha | 10 | Account group identifier |
| DIS-TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code |
| DIS-TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code |
| DIS-INT-RATE | PIC S9(04)V99 | Signed Decimal | 6 | Interest rate (annual percentage) |
| FILLER | PIC X(28) | Alpha | 28 | Reserved |

**Business Rules:**
- Used by CBACT04C to determine interest rate for each transaction category balance
- Account's ACCT-GROUP-ID maps to DIS-ACCT-GROUP-ID

---

## 9. Transaction Type Reference (`CVTRA03Y.cpy`)

**Business Purpose:** Lookup table for transaction type codes and descriptions.

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`
**Record Length:** 60 bytes | **Key:** Type Code (2 chars)

| Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|
| TRAN-TYPE | PIC X(02) | Alpha | 2 | **Primary Key.** Transaction type code |
| TRAN-TYPE-DESC | PIC X(50) | Alpha | 50 | Type description (e.g., "Purchase", "Cash Advance") |
| FILLER | PIC X(08) | Alpha | 8 | Reserved |

---

## 10. Transaction Category Reference (`CVTRA04Y.cpy`)

**Business Purpose:** Lookup table for transaction category codes within a type.

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`
**Record Length:** 60 bytes | **Key:** Type Code + Category Code

| Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|
| TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code (part of composite key) |
| TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Category code (part of composite key) |
| TRAN-CAT-TYPE-DESC | PIC X(50) | Alpha | 50 | Category description |
| FILLER | PIC X(04) | Alpha | 4 | Reserved |

---

## 11. User Security Entity (`CSUSR01Y.cpy`)

**Business Purpose:** Stores user credentials and role for application sign-on.

**VSAM Dataset:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`
**Record Length:** 80 bytes | **Key:** User ID (8 chars)

| Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|
| SEC-USR-ID | PIC X(08) | Alpha | 8 | **Primary Key.** User login ID |
| SEC-USR-FNAME | PIC X(20) | Alpha | 20 | User first name |
| SEC-USR-LNAME | PIC X(20) | Alpha | 20 | User last name |
| SEC-USR-PWD | PIC X(08) | Alpha | 8 | User password (**stored in plain text -- security risk**) |
| SEC-USR-TYPE | PIC X(01) | Alpha | 1 | User type: 'A' = Admin, 'U' = Regular User |
| SEC-USR-FILLER | PIC X(23) | Alpha | 23 | Reserved |

**Business Rules:**
- Admin users see Admin Menu (COADM01C); Regular users see Main Menu (COMEN01C)
- Password is stored unencrypted -- critical security concern for modernization
- Default accounts: ADMIN001/PASSWORD (admin), USER0001/PASSWORD (user)

---

## 12. Export Record (`CVEXPORT.cpy`)

**Business Purpose:** Multi-record type layout for data migration export/import files.

| Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|
| EXPORT-RECORD-TYPE | PIC X(02) | Alpha | 2 | Record type: 'CU'=Customer, 'AC'=Account, 'XR'=Xref, 'TR'=Transaction |
| EXPORT-DATA | PIC X(498) | Alpha | 498 | Record data (overlaid per type) |

---

## 13. Statement Transaction (`COSTM01.CPY`)

**Business Purpose:** Reorganized transaction layout with card number as leading key, used for statement generation.

**VSAM Dataset:** `AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS`
**Record Length:** 350 bytes | **Key:** Card Number (16) + Transaction ID (16) = 32 bytes

| Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|
| TRNX-CARD-NUM | PIC X(16) | Alpha | 16 | Card number (part of composite key) |
| TRNX-ID | PIC X(16) | Alpha | 16 | Transaction ID (part of composite key) |
| TRNX-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code |
| TRNX-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category |
| TRNX-SOURCE | PIC X(10) | Alpha | 10 | Transaction source |
| TRNX-DESC | PIC X(100) | Alpha | 100 | Description |
| TRNX-AMT | PIC S9(09)V99 | Signed Decimal | 11 | Amount |
| TRNX-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant ID |
| TRNX-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant name |
| TRNX-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant city |
| TRNX-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant ZIP |
| TRNX-ORIG-TS | PIC X(26) | Alpha | 26 | Original timestamp |
| TRNX-PROC-TS | PIC X(26) | Alpha | 26 | Processing timestamp |
| FILLER | PIC X(20) | Alpha | 20 | Reserved |

---

## 14. Common Communication Area (`COCOM01Y.cpy`)

**Business Purpose:** Shared COMMAREA passed between CICS programs for session state management.

| Field Name | PIC Clause | Type | Length | Business Description |
|---|---|---|---|---|
| CDEMO-FROM-TRANID | PIC X(04) | Alpha | 4 | Transaction ID of calling program |
| CDEMO-FROM-PROGRAM | PIC X(08) | Alpha | 8 | Program name of caller |
| CDEMO-TO-TRANID | PIC X(04) | Alpha | 4 | Target transaction ID |
| CDEMO-TO-PROGRAM | PIC X(08) | Alpha | 8 | Target program name |
| CDEMO-USER-ID | PIC X(08) | Alpha | 8 | Current signed-in user ID |
| CDEMO-USER-TYPE | PIC X(01) | Alpha | 1 | Current user type (A/U) |
| CDEMO-PGM-CONTEXT | PIC 9(01) | Numeric | 1 | Program context indicator |
| CDEMO-ACCT-ID | PIC 9(11) | Numeric | 11 | Current account in context |
| CDEMO-CARD-NUM | PIC X(16) | Alpha | 16 | Current card number in context |
| CDEMO-LAST-MAP | PIC X(07) | Alpha | 7 | Last BMS map displayed |
| CDEMO-LAST-MAPSET | PIC X(07) | Alpha | 7 | Last BMS mapset used |

**Business Rules:**
- Passed via CICS COMMAREA on every XCTL/RETURN
- Carries user session state across the entire navigation flow
- FROM/TO fields enable back-navigation and screen chaining

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)
  └── 1:N ── Card Cross-Reference (CVACT03Y)
                ├── N:1 ── Account (CVACT01Y)
                │            ├── 1:N ── Trans Category Balance (CVTRA01Y)
                │            └── N:1 ── Disclosure Group (CVTRA02Y)
                └── N:1 ── Card (CVACT02Y)
                             └── 1:N ── Transaction (CVTRA05Y)
                                          ├── N:1 ── Transaction Type (CVTRA03Y)
                                          └── N:1 ── Transaction Category (CVTRA04Y)

User Security (CSUSR01Y) -- standalone entity for authentication
```

---

## PII / Sensitive Data Flags

| Entity | Field | Sensitivity | Migration Note |
|---|---|---|---|
| Customer | CUST-SSN | **HIGH** -- Social Security Number | Must be encrypted or tokenized |
| Customer | CUST-DOB-YYYYMMDD | **HIGH** -- Date of Birth | PII protection required |
| Customer | CUST-GOVT-ISSUED-ID | **HIGH** -- Government ID | PII protection required |
| Card | CARD-NUM | **HIGH** -- Card Number (PAN) | PCI-DSS compliance required |
| Card | CARD-CVV-CD | **HIGH** -- CVV | Must never be stored post-auth |
| User Security | SEC-USR-PWD | **CRITICAL** -- Plain text password | Must hash (bcrypt/scrypt) in target |
| Transaction | TRAN-CARD-NUM | **HIGH** -- Card Number | PCI-DSS: mask or tokenize |
