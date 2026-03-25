# CardDemo Data Dictionary

> **Generated**: 2026-03-25 | **Source**: Copybook PIC clause analysis from `app/cpy/`

## Overview

This data dictionary maps COBOL copybook record layouts to business-friendly entity descriptions. Each VSAM file stores fixed-length records defined by copybooks. PIC clauses define data types, and level numbers define hierarchy.

**COBOL PIC Clause Quick Reference:**
| PIC Pattern | Business Type | Example |
|---|---|---|
| `PIC X(n)` | Text/String, n characters | `PIC X(25)` = 25-char string |
| `PIC 9(n)` | Unsigned integer, n digits | `PIC 9(11)` = 11-digit number |
| `PIC S9(n)V99` | Signed decimal (2 decimal places) | `PIC S9(09)V99` = ±999,999,999.99 |
| `PIC S9(n) COMP` | Binary integer | Efficient storage for counters |
| `PIC S9(n) COMP-3` | Packed decimal | Efficient storage for money |

---

## 1. Account Entity

**Copybook**: `CVACT01Y` | **Record Length**: 300 bytes | **VSAM File**: `ACCTDATA.VSAM.KSDS`

This is the core account master record containing credit card account details.

| Field | COBOL Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| Account ID | `ACCT-ID` | `PIC 9(11)` | Integer | 11 digits | Primary key -- unique account identifier |
| Active Status | `ACCT-ACTIVE-STATUS` | `PIC X(01)` | Code | 1 char | Account active status (Y/N) |
| Current Balance | `ACCT-CURR-BAL` | `PIC S9(10)V99` | Currency | ±10B.99 | Current account balance |
| Credit Limit | `ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | Currency | ±10B.99 | Maximum credit limit |
| Cash Credit Limit | `ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | Currency | ±10B.99 | Cash advance credit limit |
| Open Date | `ACCT-OPEN-DATE` | `PIC X(10)` | Date | 10 chars | Date account was opened |
| Expiration Date | `ACCT-EXPIRAION-DATE` | `PIC X(10)` | Date | 10 chars | Account expiration date |
| Reissue Date | `ACCT-REISSUE-DATE` | `PIC X(10)` | Date | 10 chars | Last card reissue date |
| Current Cycle Credit | `ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | Currency | ±10B.99 | Credits in current billing cycle |
| Current Cycle Debit | `ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | Currency | ±10B.99 | Debits in current billing cycle |
| Group ID | `ACCT-GROUP-ID` | `PIC X(10)` | Code | 10 chars | Account group classification |
| Filler | `FILLER` | `PIC X(178)` | Reserved | 178 chars | Reserved for future use |

### Modernization Mapping
→ **Java Class**: `Account.java` (JPA Entity)
→ **Database Table**: `ACCOUNT`
→ **Primary Key**: `ACCT-ID` → `accountId` (BIGINT)
→ **Money fields**: Use `BigDecimal` in Java

---

## 2. Credit Card Entity

**Copybook**: `CVACT02Y` | **Record Length**: 150 bytes | **VSAM File**: `CARDDATA.VSAM.KSDS`

| Field | COBOL Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| Card Number | `CARD-NUM` | `PIC X(16)` | String | 16 chars | Primary key -- credit card number |
| Account ID | `CARD-ACCT-ID` | `PIC 9(11)` | Integer | 11 digits | Foreign key to Account |
| CVV Code | `CARD-CVV-CD` | `PIC 9(03)` | Integer | 3 digits | Card verification value |
| Embossed Name | `CARD-EMBOSSED-NAME` | `PIC X(50)` | String | 50 chars | Name printed on card |
| Expiration Date | `CARD-EXPIRAION-DATE` | `PIC X(10)` | Date | 10 chars | Card expiration date |
| Active Status | `CARD-ACTIVE-STATUS` | `PIC X(01)` | Code | 1 char | Card active status (Y/N) |
| Filler | `FILLER` | `PIC X(59)` | Reserved | 59 chars | Reserved for future use |

### Modernization Mapping
→ **Java Class**: `CreditCard.java`
→ **Database Table**: `CREDIT_CARD`
→ **Foreign Key**: `CARD-ACCT-ID` → `Account.accountId`
→ **Security Note**: `CARD-NUM` and `CARD-CVV-CD` require encryption at rest

---

## 3. Customer Entity

**Copybook**: `CVCUS01Y` | **Record Length**: 500 bytes | **VSAM File**: `CUSTDATA.VSAM.KSDS`

| Field | COBOL Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| Customer ID | `CUST-ID` | `PIC 9(09)` | Integer | 9 digits | Primary key -- unique customer ID |
| First Name | `CUST-FIRST-NAME` | `PIC X(25)` | String | 25 chars | Customer first name |
| Middle Name | `CUST-MIDDLE-NAME` | `PIC X(25)` | String | 25 chars | Customer middle name |
| Last Name | `CUST-LAST-NAME` | `PIC X(25)` | String | 25 chars | Customer last name |
| Address Line 1 | `CUST-ADDR-LINE-1` | `PIC X(50)` | String | 50 chars | Street address line 1 |
| Address Line 2 | `CUST-ADDR-LINE-2` | `PIC X(50)` | String | 50 chars | Street address line 2 |
| Address Line 3 | `CUST-ADDR-LINE-3` | `PIC X(50)` | String | 50 chars | Street address line 3 |
| State/Country Code | `CUST-ADDR-STATE-CD` | `PIC X(02)` | Code | 2 chars | US state or country code |
| Country Code | `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | Code | 3 chars | ISO country code |
| ZIP Code | `CUST-ADDR-ZIP` | `PIC X(10)` | String | 10 chars | ZIP/postal code |
| Phone 1 | `CUST-PHONE-NUM-1` | `PIC X(15)` | String | 15 chars | Primary phone number |
| Phone 2 | `CUST-PHONE-NUM-2` | `PIC X(15)` | String | 15 chars | Secondary phone number |
| SSN | `CUST-SSN` | `PIC 9(09)` | Integer | 9 digits | Social Security Number |
| Govt ID | `CUST-GOVT-ISSUED-ID` | `PIC X(20)` | String | 20 chars | Government-issued ID |
| Date of Birth | `CUST-DOB-YYYYMMDD` | `PIC X(10)` | Date | 10 chars | Date of birth |
| EFT Account ID | `CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | String | 10 chars | Electronic fund transfer acct |
| Primary Card Holder | `CUST-PRI-CARD-HOLDER-IND` | `PIC X(01)` | Code | 1 char | Primary cardholder indicator |
| FICO Score | `CUST-FICO-CREDIT-SCORE` | `PIC 9(03)` | Integer | 3 digits | FICO credit score |
| Filler | `FILLER` | `PIC X(168)` | Reserved | 168 chars | Reserved for future use |

### Modernization Mapping
→ **Java Class**: `Customer.java`
→ **Database Table**: `CUSTOMER`
→ **PII Fields**: `CUST-SSN`, `CUST-DOB-YYYYMMDD`, `CUST-GOVT-ISSUED-ID` require encryption
→ **Address**: Consider extracting to separate `Address` value object

---

## 4. Card-Account Cross-Reference Entity

**Copybook**: `CVACT03Y` | **Record Length**: 50 bytes | **VSAM File**: `CARDXREF.VSAM.KSDS`

Links credit cards to accounts. Key file for navigating from card number to account.

| Field | COBOL Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| Card Number | `XREF-CARD-NUM` | `PIC X(16)` | String | 16 chars | Primary key -- card number |
| Account ID | `XREF-ACCT-ID` | `PIC 9(11)` | Integer | 11 digits | Foreign key to Account |
| Customer ID | `XREF-CUST-ID` | `PIC 9(09)` | Integer | 9 digits | Foreign key to Customer |
| Filler | `FILLER` | `PIC X(14)` | Reserved | 14 chars | Reserved for future use |

### Modernization Mapping
→ In Java, this becomes a JPA relationship (not a separate table): `CreditCard` → `Account` → `Customer`
→ The alternate index (AIX) on `XREF-ACCT-ID` maps to a DB index on the foreign key

---

## 5. Transaction Entity

**Copybook**: `CVTRA05Y` | **Record Length**: 350 bytes | **VSAM File**: `TRANSACT.VSAM.KSDS`

The core transaction record for all credit card transactions.

| Field | COBOL Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| Transaction ID | `TRAN-ID` | `PIC X(16)` | String | 16 chars | Primary key -- unique transaction ID |
| Type Code | `TRAN-TYPE-CD` | `PIC X(02)` | Code | 2 chars | Transaction type (FK to type table) |
| Category Code | `TRAN-CAT-CD` | `PIC 9(04)` | Integer | 4 digits | Transaction category code |
| Source | `TRAN-SOURCE` | `PIC X(10)` | String | 10 chars | Origination source |
| Description | `TRAN-DESC` | `PIC X(100)` | String | 100 chars | Transaction description |
| Amount | `TRAN-AMT` | `PIC S9(09)V99` | Currency | ±999M.99 | Transaction amount |
| Merchant ID | `TRAN-MERCHANT-ID` | `PIC 9(09)` | Integer | 9 digits | Merchant identifier |
| Merchant Name | `TRAN-MERCHANT-NAME` | `PIC X(50)` | String | 50 chars | Merchant name |
| Merchant City | `TRAN-MERCHANT-CITY` | `PIC X(50)` | String | 50 chars | Merchant city |
| Merchant ZIP | `TRAN-MERCHANT-ZIP` | `PIC X(10)` | String | 10 chars | Merchant ZIP code |
| Card Number | `TRAN-CARD-NUM` | `PIC X(16)` | String | 16 chars | FK -- card used for transaction |
| Origination Timestamp | `TRAN-ORIG-TS` | `PIC X(26)` | Timestamp | 26 chars | When transaction originated |
| Processing Timestamp | `TRAN-PROC-TS` | `PIC X(26)` | Timestamp | 26 chars | When transaction was processed |
| Filler | `FILLER` | `PIC X(20)` | Reserved | 20 chars | Reserved for future use |

### Modernization Mapping
→ **Java Class**: `Transaction.java`
→ **Database Table**: `TRANSACTION`
→ **Timestamps**: Convert `PIC X(26)` to `java.time.Instant`
→ **Merchant**: Consider extracting to separate `Merchant` entity

---

## 6. Daily Transaction Entity

**Copybook**: `CVTRA06Y` | **Record Length**: 350 bytes | **VSAM File**: `DALYTRAN.PS` (sequential)

Identical structure to Transaction but holds unposted daily transactions pending batch processing.

| Field | COBOL Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| Transaction ID | `DALYTRAN-ID` | `PIC X(16)` | String | 16 chars | Daily transaction ID |
| Type Code | `DALYTRAN-TYPE-CD` | `PIC X(02)` | Code | 2 chars | Transaction type code |
| Category Code | `DALYTRAN-CAT-CD` | `PIC 9(04)` | Integer | 4 digits | Transaction category |
| Source | `DALYTRAN-SOURCE` | `PIC X(10)` | String | 10 chars | Origination source |
| Description | `DALYTRAN-DESC` | `PIC X(100)` | String | 100 chars | Transaction description |
| Amount | `DALYTRAN-AMT` | `PIC S9(09)V99` | Currency | ±999M.99 | Transaction amount |
| Merchant ID | `DALYTRAN-MERCHANT-ID` | `PIC 9(09)` | Integer | 9 digits | Merchant identifier |
| Merchant Name | `DALYTRAN-MERCHANT-NAME` | `PIC X(50)` | String | 50 chars | Merchant name |
| Merchant City | `DALYTRAN-MERCHANT-CITY` | `PIC X(50)` | String | 50 chars | Merchant city |
| Merchant ZIP | `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)` | String | 10 chars | Merchant ZIP code |
| Card Number | `DALYTRAN-CARD-NUM` | `PIC X(16)` | String | 16 chars | Card used |
| Origination Timestamp | `DALYTRAN-ORIG-TS` | `PIC X(26)` | Timestamp | 26 chars | Origination timestamp |
| Processing Timestamp | `DALYTRAN-PROC-TS` | `PIC X(26)` | Timestamp | 26 chars | Processing timestamp |
| Filler | `FILLER` | `PIC X(20)` | Reserved | 20 chars | Reserved |

### Modernization Mapping
→ In Java, daily transactions can share the `Transaction` entity with a `status` enum (`PENDING`, `POSTED`, `REJECTED`)

---

## 7. Transaction Category Balance Entity

**Copybook**: `CVTRA01Y` | **Record Length**: 50 bytes | **VSAM File**: `TCATBALF.VSAM.KSDS`

Running balance per account per transaction type per category.

| Field | COBOL Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| Account ID | `TRANCAT-ACCT-ID` | `PIC 9(11)` | Integer | 11 digits | FK to Account (part of composite key) |
| Type Code | `TRANCAT-TYPE-CD` | `PIC X(02)` | Code | 2 chars | Transaction type (part of composite key) |
| Category Code | `TRANCAT-CD` | `PIC 9(04)` | Integer | 4 digits | Category code (part of composite key) |
| Balance | `TRAN-CAT-BAL` | `PIC S9(09)V99` | Currency | ±999M.99 | Running category balance |
| Filler | `FILLER` | `PIC X(22)` | Reserved | 22 chars | Reserved |

### Modernization Mapping
→ **Java Class**: `TransactionCategoryBalance.java`
→ **Composite Key**: (`accountId`, `typeCode`, `categoryCode`)

---

## 8. Disclosure Group Entity

**Copybook**: `CVTRA02Y` | **Record Length**: 50 bytes | **VSAM File**: `DISCGRP.VSAM.KSDS`

Interest rate configuration per account group, transaction type, and category.

| Field | COBOL Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| Account Group ID | `DIS-ACCT-GROUP-ID` | `PIC X(10)` | Code | 10 chars | Account group (part of composite key) |
| Transaction Type | `DIS-TRAN-TYPE-CD` | `PIC X(02)` | Code | 2 chars | Transaction type (part of composite key) |
| Category Code | `DIS-TRAN-CAT-CD` | `PIC 9(04)` | Integer | 4 digits | Category code (part of composite key) |
| Interest Rate | `DIS-INT-RATE` | `PIC S9(04)V99` | Decimal | ±9999.99 | Interest rate percentage |
| Filler | `FILLER` | `PIC X(28)` | Reserved | 28 chars | Reserved |

### Modernization Mapping
→ **Java Class**: `DisclosureGroup.java` (configuration/reference data)
→ Used by interest calculation batch (CBACT04C)

---

## 9. Transaction Type Entity

**Copybook**: `CVTRA03Y` | **Record Length**: 60 bytes | **VSAM File**: `TRANTYPE.VSAM.KSDS`

Reference table for transaction type codes.

| Field | COBOL Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| Type Code | `TRAN-TYPE` | `PIC X(02)` | Code | 2 chars | Primary key -- type code |
| Description | `TRAN-TYPE-DESC` | `PIC X(50)` | String | 50 chars | Human-readable description |
| Filler | `FILLER` | `PIC X(08)` | Reserved | 8 chars | Reserved |

### Modernization Mapping
→ **Java**: Could be an `enum` or small reference table

---

## 10. Transaction Category Entity

**Copybook**: `CVTRA04Y` | **Record Length**: 60 bytes | **VSAM File**: `TRANCATG.VSAM.KSDS`

Reference table for transaction category codes within each type.

| Field | COBOL Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| Type Code | `TRAN-TYPE-CD` | `PIC X(02)` | Code | 2 chars | FK to Transaction Type (composite key) |
| Category Code | `TRAN-CAT-CD` | `PIC 9(04)` | Integer | 4 digits | Category code (composite key) |
| Description | `TRAN-CAT-TYPE-DESC` | `PIC X(50)` | String | 50 chars | Category description |
| Filler | `FILLER` | `PIC X(04)` | Reserved | 4 chars | Reserved |

### Modernization Mapping
→ **Java Class**: `TransactionCategory.java`
→ **Composite Key**: (`typeCode`, `categoryCode`)

---

## 11. User Security Entity

**Copybook**: `CSUSR01Y` | **Record Length**: 80 bytes | **VSAM File**: `USRSEC.VSAM.KSDS`

User authentication and authorization records.

| Field | COBOL Name | PIC Clause | Type | Size | Description |
|---|---|---|---|---|---|
| User ID | `SEC-USR-ID` | `PIC X(08)` | String | 8 chars | Primary key -- login user ID |
| First Name | `SEC-USR-FNAME` | `PIC X(20)` | String | 20 chars | User first name |
| Last Name | `SEC-USR-LNAME` | `PIC X(20)` | String | 20 chars | User last name |
| Password | `SEC-USR-PWD` | `PIC X(08)` | String | 8 chars | Password (plaintext) |
| User Type | `SEC-USR-TYPE` | `PIC X(01)` | Code | 1 char | A=Admin, U=Regular User |
| Filler | `SEC-USR-FILLER` | `PIC X(23)` | Reserved | 23 chars | Reserved |

### Modernization Mapping
→ **Java Class**: `User.java` with Spring Security integration
→ **Security**: Passwords must be hashed (BCrypt) -- mainframe stores plaintext
→ **User Type**: Map to Spring Security roles (`ROLE_ADMIN`, `ROLE_USER`)

---

## 12. Communication Area (COMMAREA)

**Copybook**: `COCOM01Y` | **Used by**: All online CICS programs

The COMMAREA is passed between CICS programs to maintain session state. Not stored in VSAM.

| Field | COBOL Name | PIC Clause | Type | Description |
|---|---|---|---|---|
| From Transaction ID | `CDEMO-FROM-TRANID` | `PIC X(04)` | Code | Source CICS transaction |
| From Program | `CDEMO-FROM-PROGRAM` | `PIC X(08)` | Code | Source program name |
| To Transaction ID | `CDEMO-TO-TRANID` | `PIC X(04)` | Code | Target CICS transaction |
| To Program | `CDEMO-TO-PROGRAM` | `PIC X(08)` | Code | Target program name |
| User ID | `CDEMO-USER-ID` | `PIC X(08)` | String | Logged-in user |
| User Type | `CDEMO-USER-TYPE` | `PIC X(01)` | Code | A=Admin, U=User |
| Program Context | `CDEMO-PGM-CONTEXT` | `PIC 9(01)` | Integer | 0=Enter, 1=Re-enter |
| Customer ID | `CDEMO-CUST-ID` | `PIC 9(09)` | Integer | Current customer in context |
| Customer First Name | `CDEMO-CUST-FNAME` | `PIC X(25)` | String | Customer first name |
| Customer Middle Name | `CDEMO-CUST-MNAME` | `PIC X(25)` | String | Customer middle name |
| Customer Last Name | `CDEMO-CUST-LNAME` | `PIC X(25)` | String | Customer last name |
| Account ID | `CDEMO-ACCT-ID` | `PIC 9(11)` | Integer | Current account in context |
| Account Status | `CDEMO-ACCT-STATUS` | `PIC X(01)` | Code | Account status |
| Card Number | `CDEMO-CARD-NUM` | `PIC 9(16)` | Integer | Current card in context |
| Last Map | `CDEMO-LAST-MAP` | `PIC X(7)` | Code | Last BMS map displayed |
| Last Mapset | `CDEMO-LAST-MAPSET` | `PIC X(7)` | Code | Last BMS mapset used |

### Modernization Mapping
→ **Java**: Replace with HTTP session or JWT token containing user context
→ Navigation state managed by Spring MVC or frontend router

---

## 13. Statement Report Layout

**Copybook**: `COSTM01` | **Used by**: CBSTM03A (statement generation)

Altered transaction layout used specifically for statement reporting with card-number-first key.

| Field | COBOL Name | PIC Clause | Type | Description |
|---|---|---|---|---|
| Card Number (Key Part 1) | `TRNX-CARD-NUM` | `PIC X(16)` | String | Card number (part of composite key) |
| Transaction ID (Key Part 2) | `TRNX-ID` | `PIC X(16)` | String | Transaction ID (part of composite key) |
| Type Code | `TRNX-TYPE-CD` | `PIC X(02)` | Code | Transaction type |
| Category Code | `TRNX-CAT-CD` | `PIC 9(04)` | Integer | Transaction category |
| Source | `TRNX-SOURCE` | `PIC X(10)` | String | Origination source |
| Description | `TRNX-DESC` | `PIC X(100)` | String | Transaction description |
| Amount | `TRNX-AMT` | `PIC S9(09)V99` | Currency | Transaction amount |
| Merchant ID | `TRNX-MERCHANT-ID` | `PIC 9(09)` | Integer | Merchant ID |
| Merchant Name | `TRNX-MERCHANT-NAME` | `PIC X(50)` | String | Merchant name |
| Merchant City | `TRNX-MERCHANT-CITY` | `PIC X(50)` | String | Merchant city |
| Merchant ZIP | `TRNX-MERCHANT-ZIP` | `PIC X(10)` | String | Merchant ZIP |
| Origination Timestamp | `TRNX-ORIG-TS` | `PIC X(26)` | Timestamp | When originated |
| Processing Timestamp | `TRNX-PROC-TS` | `PIC X(26)` | Timestamp | When processed |

---

## 14. Export/Import Record

**Copybook**: `CVEXPORT` | **Used by**: CBEXPORT, CBIMPORT

Combined record layout for data migration export/import operations.

| Field | COBOL Name | PIC Clause | Type | Description |
|---|---|---|---|---|
| Record Type | `EXP-REC-TYPE` | `PIC X(01)` | Code | A=Account, C=Customer, X=Xref, T=Transaction, D=Card |
| Account ID | `EXP-ACCT-ID` | `PIC 9(11)` | Integer | Account identifier |
| Account Status | `EXP-ACCT-ACTIVE-STATUS` | `PIC X(01)` | Code | Active status |
| Current Balance | `EXP-ACCT-CURR-BAL` | `PIC S9(10)V99 COMP-3` | Currency | Account balance |
| Credit Limit | `EXP-ACCT-CREDIT-LIMIT` | `PIC S9(10)V99 COMP-3` | Currency | Credit limit |
| Customer ID | `EXP-CUST-ID` | `PIC 9(09)` | Integer | Customer identifier |
| Customer First Name | `EXP-CUST-FIRST-NAME` | `PIC X(25)` | String | First name |
| Customer Last Name | `EXP-CUST-LAST-NAME` | `PIC X(25)` | String | Last name |
| Card Number | `EXP-CARD-NUM` | `PIC X(16)` | String | Card number |
| Card CVV | `EXP-CARD-CVV-CD` | `PIC 9(03) COMP` | Integer | CVV code |
| Card Embossed Name | `EXP-CARD-EMBOSSED-NAME` | `PIC X(50)` | String | Name on card |
| Card Expiration | `EXP-CARD-EXPIRAION-DATE` | `PIC X(10)` | Date | Card expiration |
| Card Active Status | `EXP-CARD-ACTIVE-STATUS` | `PIC X(01)` | Code | Card status |

---

## 15. Report Data Structure

**Copybook**: `CVTRA07Y` | **Used by**: CBTRN03C (transaction report)

| Field | COBOL Name | PIC Clause | Type | Description |
|---|---|---|---|---|
| Report Short Name | `REPT-SHORT-NAME` | `PIC X(38)` | String | 'DALYREPT' |
| Report Long Name | `REPT-LONG-NAME` | `PIC X(41)` | String | 'Daily Transaction Report' |
| Start Date | `REPT-START-DATE` | `PIC X(10)` | Date | Report date range start |
| End Date | `REPT-END-DATE` | `PIC X(10)` | Date | Report date range end |
| Transaction ID (Detail) | `TRAN-REPORT-TRANS-ID` | `PIC X(16)` | String | Transaction ID for report line |
| Account ID (Detail) | `TRAN-REPORT-ACCOUNT-ID` | `PIC X(11)` | String | Account ID for report line |
| Amount (Detail) | `TRAN-REPORT-AMT` | `PIC -ZZZ,ZZZ,ZZZ.ZZ` | Edited | Formatted amount |
| Page Total | `REPT-PAGE-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Edited | Page subtotal |
| Account Total | `REPT-ACCOUNT-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Edited | Account subtotal |
| Grand Total | `REPT-GRAND-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Edited | Report grand total |

---

## 16. Date/Time Structures

**Copybook**: `CSDAT01Y` | **Used by**: All online CICS programs

| Field | COBOL Name | PIC Clause | Description |
|---|---|---|---|
| Current Date (YYYYMMDD) | `WS-CURDATE` | `PIC 9(08)` | System date |
| Current Time (HHMMSSMS) | `WS-CURTIME` | `PIC 9(08)` | System time |
| Formatted Date | `WS-CURDATE-MM-DD-YY` | `MM/DD/YY` | Display format |
| Formatted Time | `WS-CURTIME-HH-MM-SS` | `HH:MM:SS` | Display format |
| Timestamp | `WS-TIMESTAMP` | `YYYY-MM-DD HH:MM:SS.NNNNNN` | Full timestamp |

**Copybook**: `CODATECN` | **Used by**: CBACT01C (date conversion)

| Field | COBOL Name | Description |
|---|---|---|
| Input Type | `CODATECN-TYPE` | 1=YYYYMMDD, 2=YYYY-MM-DD |
| Input Date | `CODATECN-INP-DATE` | Date to convert |
| Output Type | `CODATECN-OUTTYPE` | 1=YYYY-MM-DD, 2=YYYYMMDD |
| Output Date | `CODATECN-0UT-DATE` | Converted date |
| Error Message | `CODATECN-ERROR-MSG` | Conversion error |

---

## 17. Lookup / Validation Data

**Copybook**: `CSLKPCDY` | **Used by**: COACTUPC, COCRDUPC

Contains embedded validation tables:
- **North America Phone Area Codes**: ~400+ valid 3-digit area codes (sourced from NANPA)
- **US State Codes**: 2-letter state abbreviations
- **State-ZIP Prefix Mapping**: State code to first 2 digits of ZIP code

### Modernization Mapping
→ Replace with database reference tables or external validation services

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)
  └── 1:N ── Account (CVACT01Y)
                ├── 1:N ── Credit Card (CVACT02Y)
                │            └── N:1 ── Card-Account XREF (CVACT03Y)
                ├── 1:N ── Transaction (CVTRA05Y)
                ├── 1:N ── Category Balance (CVTRA01Y)
                └── N:1 ── Disclosure Group (CVTRA02Y)

Transaction Type (CVTRA03Y)
  └── 1:N ── Transaction Category (CVTRA04Y)

User Security (CSUSR01Y) -- standalone authentication entity

Daily Transaction (CVTRA06Y) -- staging for batch posting into Transaction
```

## VSAM File Summary

| VSAM Dataset | Key | Rec Len | Copybook | Access |
|---|---|---|---|---|
| `ACCTDATA.VSAM.KSDS` | Account ID (11) | 300 | CVACT01Y | Random + Sequential |
| `CARDDATA.VSAM.KSDS` | Card Number (16) | 150 | CVACT02Y | Random + Sequential |
| `CUSTDATA.VSAM.KSDS` | Customer ID (9) | 500 | CVCUS01Y | Random |
| `CARDXREF.VSAM.KSDS` | Card Number (16) | 50 | CVACT03Y | Random + AIX on Acct ID |
| `TRANSACT.VSAM.KSDS` | Transaction ID (16) | 350 | CVTRA05Y | Random + Sequential |
| `TCATBALF.VSAM.KSDS` | Acct+Type+Cat (17) | 50 | CVTRA01Y | Random |
| `DISCGRP.VSAM.KSDS` | Group+Type+Cat (16) | 50 | CVTRA02Y | Random |
| `TRANTYPE.VSAM.KSDS` | Type Code (2) | 60 | CVTRA03Y | Random |
| `TRANCATG.VSAM.KSDS` | Type+Cat (6) | 60 | CVTRA04Y | Random |
| `USRSEC.VSAM.KSDS` | User ID (8) | 80 | CSUSR01Y | Random |
