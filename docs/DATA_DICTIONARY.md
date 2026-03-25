# Data Dictionary -- CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Source:** Copybooks in `app/cpy/`  
> **Methodology:** PIC clauses extracted from copybook record layouts and mapped to business-friendly types.

---

## PIC Clause Reference

| COBOL PIC | Business Type | Example |
|-----------|--------------|---------|
| `PIC X(n)` | Text / String (n chars) | `PIC X(50)` = 50-char string |
| `PIC 9(n)` | Unsigned integer (n digits) | `PIC 9(11)` = 11-digit number |
| `PIC S9(n)V99` | Signed decimal (n.2) | `PIC S9(09)V99` = amount +-999,999,999.99 |
| `PIC 9(n) COMP` | Binary integer | `PIC 9(03) COMP` = packed 3-digit int |

---

## 1. Account Entity

**Copybook:** `CVACT01Y` | **Record:** `ACCT-RECORD` | **Size:** 300 bytes  
**VSAM File:** `ACCTDATA.VSAM.KSDS` | **Key:** Account ID

| Field | COBOL Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| Account ID | `ACCT-ID` | `PIC 9(11)` | Integer (11) | Unique account identifier (primary key) |
| Active Status | `ACCT-ACTIVE-STATUS` | `PIC X(01)` | Flag (Y/N) | Whether account is active |
| Current Balance | `ACCT-CURR-BAL` | `PIC S9(10)V99` | Currency (12.2) | Current account balance |
| Credit Limit | `ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | Currency (12.2) | Maximum credit allowed |
| Cash Credit Limit | `ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | Currency (12.2) | Cash advance credit limit |
| Open Date | `ACCT-OPEN-DATE` | `PIC X(10)` | Date (YYYY-MM-DD) | Date account was opened |
| Expiration Date | `ACCT-EXPIRAION-DATE` | `PIC X(10)` | Date (YYYY-MM-DD) | Account expiration date |
| Reissue Date | `ACCT-REISSUE-DATE` | `PIC X(10)` | Date (YYYY-MM-DD) | Last reissue date |
| Current Cycle Credit | `ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | Currency (12.2) | Credits posted this cycle |
| Current Cycle Debit | `ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | Currency (12.2) | Debits posted this cycle |
| Account Group | `ACCT-GROUP-ID` | `PIC X(10)` | Code | Disclosure/rate group assignment |
| Filler | `FILLER` | `PIC X(178)` | Reserved | Unused space for future fields |

---

## 2. Card Entity

**Copybook:** `CVACT02Y` | **Record:** `CARD-RECORD` | **Size:** 150 bytes  
**VSAM File:** `CARDDATA.VSAM.KSDS` | **Key:** Card Number

| Field | COBOL Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| Card Number | `CARD-NUM` | `PIC X(16)` | String (16) | 16-digit card number (primary key) |
| Account ID | `CARD-ACCT-ID` | `PIC 9(11)` | Integer (11) | FK to Account entity |
| CVV Code | `CARD-CVV-CD` | `PIC 9(03)` | Integer (3) | Card verification value (sensitive) |
| Embossed Name | `CARD-EMBOSSED-NAME` | `PIC X(50)` | String (50) | Name printed on physical card |
| Expiration Date | `CARD-EXPIRAION-DATE` | `PIC X(10)` | Date (YYYY-MM-DD) | Card expiration date |
| Active Status | `CARD-ACTIVE-STATUS` | `PIC X(01)` | Flag (Y/N) | Whether card is active |
| Filler | `FILLER` | `PIC X(59)` | Reserved | Unused space |

---

## 3. Customer Entity

**Copybook:** `CVCUS01Y` | **Record:** `CUSTOMER-RECORD` | **Size:** 500 bytes  
**VSAM File:** `CUSTDATA.VSAM.KSDS` | **Key:** Customer ID

| Field | COBOL Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| Customer ID | `CUST-ID` | `PIC 9(09)` | Integer (9) | Unique customer identifier (primary key) |
| First Name | `CUST-FIRST-NAME` | `PIC X(25)` | String (25) | Customer first name |
| Middle Name | `CUST-MIDDLE-NAME` | `PIC X(25)` | String (25) | Customer middle name |
| Last Name | `CUST-LAST-NAME` | `PIC X(25)` | String (25) | Customer last name |
| Address Line 1 | `CUST-ADDR-LINE-1` | `PIC X(50)` | String (50) | Street address line 1 |
| Address Line 2 | `CUST-ADDR-LINE-2` | `PIC X(50)` | String (50) | Street address line 2 |
| Address Line 3 | `CUST-ADDR-LINE-3` | `PIC X(50)` | String (50) | Street address line 3 |
| State Code | `CUST-ADDR-STATE-CD` | `PIC X(02)` | State Code | US state (validated against CSLKPCDY) |
| Country Code | `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | Country Code | ISO country code |
| Zip Code | `CUST-ADDR-ZIP` | `PIC X(10)` | Postal Code | ZIP / postal code |
| Phone 1 | `CUST-PHONE-NUM-1` | `PIC X(15)` | Phone | Primary phone number |
| Phone 2 | `CUST-PHONE-NUM-2` | `PIC X(15)` | Phone | Secondary phone number |
| SSN | `CUST-SSN` | `PIC 9(09)` | SSN (sensitive) | Social Security Number |
| Govt-Issued ID | `CUST-GOVT-ISSUED-ID` | `PIC X(20)` | ID String | Government-issued identification |
| Date of Birth | `CUST-DOB-YYYYMMDD` | `PIC X(10)` | Date | Date of birth |
| EFT Account ID | `CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | String (10) | Electronic funds transfer account |
| Primary Holder | `CUST-PRI-CARD-HOLDER-IND` | `PIC X(01)` | Flag (Y/N) | Primary card holder indicator |
| FICO Score | `CUST-FICO-CREDIT-SCORE` | `PIC 9(03)` | Integer (3) | FICO credit score (300-850) |
| Filler | `FILLER` | `PIC X(168)` | Reserved | Unused space |

---

## 4. Card Cross-Reference Entity

**Copybook:** `CVACT03Y` | **Record:** `CARD-XREF-RECORD` | **Size:** 50 bytes  
**VSAM File:** `CARDXREF.VSAM.KSDS` | **Key:** Card Number

| Field | COBOL Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| Card Number | `XREF-CARD-NUM` | `PIC X(16)` | String (16) | Card number (FK to Card) |
| Account ID | `XREF-ACCT-ID` | `PIC 9(11)` | Integer (11) | FK to Account entity |
| Customer ID | `XREF-CUST-ID` | `PIC 9(09)` | Integer (9) | FK to Customer entity |
| Filler | `FILLER` | `PIC X(14)` | Reserved | Unused space |

**Business Role:** Central lookup table linking Card -> Account -> Customer. Used extensively by both online and batch programs to resolve card-to-account relationships.

---

## 5. Transaction Entity

**Copybook:** `CVTRA05Y` | **Record:** `TRAN-RECORD` | **Size:** 350 bytes  
**VSAM File:** `TRANSACT.VSAM.KSDS` | **Key:** Transaction ID

| Field | COBOL Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| Transaction ID | `TRAN-ID` | `PIC X(16)` | String (16) | Unique transaction identifier |
| Type Code | `TRAN-TYPE-CD` | `PIC X(02)` | Code | Transaction type (FK to CVTRA03Y) |
| Category Code | `TRAN-CAT-CD` | `PIC 9(04)` | Integer (4) | Transaction category (FK to CVTRA04Y) |
| Source | `TRAN-SOURCE` | `PIC X(10)` | Code | Origination source |
| Description | `TRAN-DESC` | `PIC X(100)` | Text | Free-text transaction description |
| Amount | `TRAN-AMT` | `PIC S9(09)V99` | Currency (11.2) | Transaction amount (signed) |
| Merchant ID | `TRAN-MERCHANT-ID` | `PIC 9(09)` | Integer (9) | Merchant identifier |
| Merchant Name | `TRAN-MERCHANT-NAME` | `PIC X(50)` | String (50) | Merchant business name |
| Merchant City | `TRAN-MERCHANT-CITY` | `PIC X(50)` | String (50) | Merchant city |
| Merchant ZIP | `TRAN-MERCHANT-ZIP` | `PIC X(10)` | Postal Code | Merchant postal code |
| Card Number | `TRAN-CARD-NUM` | `PIC X(16)` | String (16) | Card used for transaction |
| Origination TS | `TRAN-ORIG-TS` | `PIC X(26)` | Timestamp | When transaction occurred |
| Processing TS | `TRAN-PROC-TS` | `PIC X(26)` | Timestamp | When transaction was processed |
| Filler | `FILLER` | `PIC X(20)` | Reserved | Unused space |

---

## 6. Daily Transaction Entity

**Copybook:** `CVTRA06Y` | **Record:** `DALYTRAN-RECORD` | **Size:** 350 bytes  
**VSAM File:** `DALYTRAN.PS` (sequential) | **Key:** N/A (sequential input)

| Field | COBOL Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| Transaction ID | `DALYTRAN-ID` | `PIC X(16)` | String (16) | Daily transaction identifier |
| Type Code | `DALYTRAN-TYPE-CD` | `PIC X(02)` | Code | Transaction type code |
| Category Code | `DALYTRAN-CAT-CD` | `PIC 9(04)` | Integer (4) | Transaction category code |
| Source | `DALYTRAN-SOURCE` | `PIC X(10)` | Code | Origination source |
| Description | `DALYTRAN-DESC` | `PIC X(100)` | Text | Transaction description |
| Amount | `DALYTRAN-AMT` | `PIC S9(09)V99` | Currency (11.2) | Transaction amount |
| Merchant ID | `DALYTRAN-MERCHANT-ID` | `PIC 9(09)` | Integer (9) | Merchant ID |
| Merchant Name | `DALYTRAN-MERCHANT-NAME` | `PIC X(50)` | String (50) | Merchant name |
| Merchant City | `DALYTRAN-MERCHANT-CITY` | `PIC X(50)` | String (50) | Merchant city |
| Merchant ZIP | `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)` | Postal Code | Merchant ZIP |
| Card Number | `DALYTRAN-CARD-NUM` | `PIC X(16)` | String (16) | Card number |
| Origination TS | `DALYTRAN-ORIG-TS` | `PIC X(26)` | Timestamp | Origination timestamp |
| Processing TS | `DALYTRAN-PROC-TS` | `PIC X(26)` | Timestamp | Processing timestamp |
| Filler | `FILLER` | `PIC X(20)` | Reserved | Unused space |

**Business Role:** Staging area for incoming daily transactions. Identical layout to Transaction (CVTRA05Y). Processed by CBTRN01C (validation) and CBTRN02C (posting) before merging into the master Transaction file.

---

## 7. Transaction Category Balance Entity

**Copybook:** `CVTRA01Y` | **Record:** `TRAN-CAT-BAL-RECORD` | **Size:** 50 bytes  
**VSAM File:** `TCATBALF.VSAM.KSDS` | **Key:** Account ID + Type Code + Category Code

| Field | COBOL Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| Account ID | `TRANCAT-ACCT-ID` | `PIC 9(11)` | Integer (11) | FK to Account |
| Type Code | `TRANCAT-TYPE-CD` | `PIC X(02)` | Code | Transaction type |
| Category Code | `TRANCAT-CD` | `PIC 9(04)` | Integer (4) | Transaction category |
| Balance | `TRAN-CAT-BAL` | `PIC S9(09)V99` | Currency (11.2) | Running balance for this category |
| Filler | `FILLER` | `PIC X(22)` | Reserved | Unused space |

**Business Role:** Running totals per account per transaction type/category. Used by CBACT04C for interest calculation and by CBTRN02C during posting.

---

## 8. Disclosure Group Entity

**Copybook:** `CVTRA02Y` | **Record:** `DIS-GROUP-RECORD` | **Size:** 50 bytes  
**VSAM File:** `DISCGRP.VSAM.KSDS` | **Key:** Group ID + Type Code + Category Code

| Field | COBOL Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| Group ID | `DIS-ACCT-GROUP-ID` | `PIC X(10)` | Code | Disclosure group identifier |
| Transaction Type | `DIS-TRAN-TYPE-CD` | `PIC X(02)` | Code | Transaction type code |
| Category Code | `DIS-TRAN-CAT-CD` | `PIC 9(04)` | Integer (4) | Transaction category code |
| Interest Rate | `DIS-INT-RATE` | `PIC S9(04)V99` | Percentage (6.2) | Annual interest rate for this group/type |
| Filler | `FILLER` | `PIC X(28)` | Reserved | Unused space |

**Business Role:** Lookup table for interest rates. CBACT04C reads this to determine the applicable rate when computing interest charges.

---

## 9. Transaction Type Reference Entity

**Copybook:** `CVTRA03Y` | **Record:** `TRAN-TYPE-RECORD` | **Size:** 60 bytes  
**VSAM File:** `TRANTYPE.VSAM.KSDS` | **Key:** Type Code

| Field | COBOL Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| Type Code | `TRAN-TYPE` | `PIC X(02)` | Code | Transaction type code (e.g., "01", "02") |
| Description | `TRAN-TYPE-DESC` | `PIC X(50)` | Text | Human-readable type description |
| Filler | `FILLER` | `PIC X(08)` | Reserved | Unused space |

---

## 10. Transaction Category Reference Entity

**Copybook:** `CVTRA04Y` | **Record:** `TRAN-CAT-RECORD` | **Size:** 60 bytes  
**VSAM File:** `TRANCATG.VSAM.KSDS` | **Key:** Type Code + Category Code

| Field | COBOL Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| Type Code | `TRAN-TYPE-CD` | `PIC X(02)` | Code | Transaction type code |
| Category Code | `TRAN-CAT-CD` | `PIC 9(04)` | Integer (4) | Category code within type |
| Description | `TRAN-CAT-TYPE-DESC` | `PIC X(50)` | Text | Category description |
| Filler | `FILLER` | `PIC X(04)` | Reserved | Unused space |

---

## 11. User Security Entity

**Copybook:** `CSUSR01Y` | **Record:** `SEC-USER-DATA` | **Size:** 80 bytes  
**VSAM File:** `USRSEC.VSAM.KSDS` | **Key:** User ID

| Field | COBOL Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| User ID | `SEC-USR-ID` | `PIC X(08)` | String (8) | Login user ID (primary key) |
| First Name | `SEC-USR-FNAME` | `PIC X(20)` | String (20) | User first name |
| Last Name | `SEC-USR-LNAME` | `PIC X(20)` | String (20) | User last name |
| Password | `SEC-USR-PWD` | `PIC X(08)` | String (8, sensitive) | Login password (plaintext) |
| Type | `SEC-USR-TYPE` | `PIC X(01)` | Flag | User type: 'A' = Admin, 'U' = Regular |
| Filler | `SEC-USR-FILLER` | `PIC X(23)` | Reserved | Unused space |

**Security Note:** Passwords are stored in plaintext -- a critical modernization concern.

---

## 12. Statement Transaction Record (Reporting)

**Copybook:** `COSTM01` | **Record:** `TRNX-RECORD` | **Size:** ~350 bytes  
**Usage:** Intermediate file for statement generation (CBSTM03A/B)

| Field | COBOL Name | PIC Clause | Business Type | Description |
|-------|-----------|------------|---------------|-------------|
| Card Number | `TRNX-CARD-NUM` | `PIC X(16)` | String (16) | Card number (part of composite key) |
| Transaction ID | `TRNX-ID` | `PIC X(16)` | String (16) | Transaction ID (part of composite key) |
| Type Code | `TRNX-TYPE-CD` | `PIC X(02)` | Code | Transaction type |
| Category Code | `TRNX-CAT-CD` | `PIC 9(04)` | Integer (4) | Transaction category |
| Source | `TRNX-SOURCE` | `PIC X(10)` | Code | Origination source |
| Description | `TRNX-DESC` | `PIC X(100)` | Text | Transaction description |
| Amount | `TRNX-AMT` | `PIC S9(09)V99` | Currency (11.2) | Transaction amount |
| Merchant ID | `TRNX-MERCHANT-ID` | `PIC 9(09)` | Integer (9) | Merchant ID |
| Merchant Name | `TRNX-MERCHANT-NAME` | `PIC X(50)` | String (50) | Merchant name |
| Merchant City | `TRNX-MERCHANT-CITY` | `PIC X(50)` | String (50) | Merchant city |
| Merchant ZIP | `TRNX-MERCHANT-ZIP` | `PIC X(10)` | Postal Code | Merchant ZIP code |
| Origination TS | `TRNX-ORIG-TS` | `PIC X(26)` | Timestamp | Origination timestamp |
| Processing TS | `TRNX-PROC-TS` | `PIC X(26)` | Timestamp | Processing timestamp |

**Note:** Key difference from CVTRA05Y -- composite key is `CARD-NUM + TRAN-ID` (card-first ordering for statement grouping).

---

## 13. Export/Import Record

**Copybook:** `CVEXPORT` | **Usage:** Data migration flat file

| Record Type | Prefix | Key Fields | Description |
|-------------|--------|------------|-------------|
| Customer | `EXP-CUST-*` | Customer ID | Full customer data for export |
| Account | `EXP-ACCT-*` | Account ID | Full account data for export |
| Cross-Reference | `EXP-XREF-*` | Card Number | Card-account-customer xref |
| Transaction | `EXP-TRAN-*` | Transaction ID | Full transaction data |
| Card | `EXP-CARD-*` | Card Number | Card data with CVV, status |

Each record type is prefixed with a record-type indicator for parsing during import (CBIMPORT).

---

## 14. Common Communication Area

**Copybook:** `COCOM01Y` | **Record:** `CDEMO-CDA-*` | **Size:** ~100 bytes  
**Usage:** CICS COMMAREA passed between all online programs

| Field | COBOL Name | Description |
|-------|-----------|-------------|
| From Transaction | `CDEMO-FROM-TRANID` | Calling transaction ID |
| From Program | `CDEMO-FROM-PROGRAM` | Calling program name |
| To Transaction | `CDEMO-TO-TRANID` | Target transaction ID |
| To Program | `CDEMO-TO-PROGRAM` | Target program name |
| User ID | `CDEMO-USER-ID` | Currently signed-on user |
| User Type | `CDEMO-USER-TYPE` | 'A' (Admin) or 'U' (User) |
| Account ID | `CDEMO-ACCT-ID` | Selected account context |
| Card Number | `CDEMO-CARD-NUM` | Selected card context |
| Last Map | `CDEMO-LAST-MAP` | Last BMS map sent |
| Last Mapset | `CDEMO-LAST-MAPSET` | Last BMS mapset |
| PFK Key | `CDEMO-PFK-SHADOW` | Last PF key pressed |

---

## 15. Lookup Code Tables

**Copybook:** `CSLKPCDY` (1,318 lines) | **Usage:** Inline validation data

| Table | Content | Example Values |
|-------|---------|----------------|
| US State Codes | 50 states + territories | AL, AK, AZ, ... , WY, DC, PR, GU |
| Country Codes | ISO country codes | US, CA, MX, GB, ... |
| State Names | Full state names for display | Alabama, Alaska, ... |

**Note:** This is the largest copybook (1,318 LOC). Contains hardcoded lookup arrays used by COACTUPC for address validation. A prime candidate for externalization to a database table during modernization.

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)
    |
    | 1:N via XREF
    v
Card-XREF (CVACT03Y) -----> Account (CVACT01Y)
    |                            |
    | 1:1                        | 1:N
    v                            v
Card (CVACT02Y)           Tran-Cat-Balance (CVTRA01Y)
                                 |
Transaction (CVTRA05Y) <---------+
    |                            |
    | type FK                    | rate lookup
    v                            v
Tran-Type (CVTRA03Y)     Disclosure-Group (CVTRA02Y)
    |
    | category FK
    v
Tran-Category (CVTRA04Y)

User-Security (CSUSR01Y) -- standalone auth table
```

---

## Modernization Notes

| COBOL Pattern | Java Equivalent |
|---------------|----------------|
| `PIC X(n)` | `String` (with `@Size(max=n)`) |
| `PIC 9(n)` | `long` or `int` (depending on size) |
| `PIC S9(n)V99` | `BigDecimal` (for financial amounts) |
| `PIC 9(n) COMP` | `int` (binary packed) |
| VSAM KSDS | JPA Entity with `@Id` on key field |
| FILLER fields | Omit -- not needed in Java |
| Copybook record | Java POJO / DTO / JPA Entity |
| 88-level conditions | Java `enum` or boolean methods |
| REDEFINES | Java inheritance or union types |
