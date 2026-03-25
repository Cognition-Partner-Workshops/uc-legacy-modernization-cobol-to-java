# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management System)
> **Source:** COBOL Copybooks in `app/cpy/` — PIC clause analysis

---

## Executive Summary

The CardDemo application manages **7 core business entities** stored as VSAM KSDS files, plus supporting reference data. This dictionary extracts every field from the copybook PIC clauses into a business-friendly format, mapping COBOL data types to logical types and identifying primary/foreign key relationships.

| Entity                    | Copybook   | Record Length | Field Count | VSAM File             |
|---------------------------|------------|---------------|-------------|-----------------------|
| Account                   | CVACT01Y   | 300 bytes     | 12          | ACCTDAT               |
| Card                      | CVACT02Y   | 150 bytes     | 6           | CARDDAT               |
| Card Cross-Reference      | CVACT03Y   | 50 bytes      | 3           | CCXREF / CXACAIX      |
| Customer                  | CVCUS01Y   | 500 bytes     | 18          | CUSTDAT               |
| Transaction (Master)      | CVTRA05Y   | 350 bytes     | 13          | TRANSACT              |
| Daily Transaction         | CVTRA06Y   | 350 bytes     | 13          | DALYTRAN              |
| User Security             | CSUSR01Y   | 80 bytes      | 5           | USRSEC                |

---

## 1. Account Entity

> **Copybook:** `CVACT01Y.cpy` | **Record:** `ACCOUNT-RECORD` | **Length:** 300 bytes
> **VSAM File:** `ACCTDAT` (KSDS, key = ACCT-ID)
> **Java Target:** `Account` entity / DTO

| # | COBOL Field             | PIC Clause       | Bytes | Logical Type     | Business Name              | Description / Rules                          |
|---|-------------------------|------------------|-------|------------------|----------------------------|----------------------------------------------|
| 1 | ACCT-ID                 | 9(11)            | 11    | Integer (PK)     | Account Number             | Primary key. 11-digit unique account ID      |
| 2 | ACCT-ACTIVE-STATUS      | X(01)            | 1     | Flag             | Account Status             | 'Y' = Active, 'N' = Inactive                |
| 3 | ACCT-CURR-BAL           | S9(10)V99        | 12    | Currency (signed)| Current Balance            | Signed decimal, 2 decimal places. Max ~$9.99B|
| 4 | ACCT-CREDIT-LIMIT       | S9(10)V99        | 12    | Currency (signed)| Credit Limit               | Maximum allowed balance                      |
| 5 | ACCT-CASH-CREDIT-LIMIT  | S9(10)V99        | 12    | Currency (signed)| Cash Advance Limit         | Maximum cash advance allowed                 |
| 6 | ACCT-OPEN-DATE          | X(10)            | 10    | Date (string)    | Account Open Date          | Format: YYYY-MM-DD                           |
| 7 | ACCT-EXPIRAION-DATE     | X(10)            | 10    | Date (string)    | Account Expiration Date    | Format: YYYY-MM-DD (note: typo in source)    |
| 8 | ACCT-REISSUE-DATE       | X(10)            | 10    | Date (string)    | Reissue Date               | Date card was last reissued                  |
| 9 | ACCT-CURR-CYC-CREDIT    | S9(10)V99        | 12    | Currency (signed)| Current Cycle Credits      | Total credits in current billing cycle       |
|10 | ACCT-CURR-CYC-DEBIT     | S9(10)V99        | 12    | Currency (signed)| Current Cycle Debits       | Total debits in current billing cycle        |
|11 | ACCT-ADDR-ZIP           | X(10)            | 10    | String           | ZIP / Postal Code          | Account holder's postal code                 |
|12 | ACCT-GROUP-ID           | X(10)            | 10    | String (FK)      | Disclosure Group ID        | Links to Disclosure Group entity (DISCGRP)   |
|   | FILLER                  | X(178)           | 178   | -                | Reserved                   | Padding to 300-byte record                   |

**Key Relationships:**
- `ACCT-ID` is referenced by `XREF-ACCT-ID` in Card Cross-Reference
- `ACCT-GROUP-ID` links to `DIS-ACCT-GROUP-ID` in Disclosure Group (CVTRA02Y)

---

## 2. Card Entity

> **Copybook:** `CVACT02Y.cpy` | **Record:** `CARD-RECORD` | **Length:** 150 bytes
> **VSAM File:** `CARDDAT` (KSDS, key = CARD-NUM)
> **Java Target:** `CreditCard` entity / DTO

| # | COBOL Field             | PIC Clause       | Bytes | Logical Type     | Business Name              | Description / Rules                          |
|---|-------------------------|------------------|-------|------------------|----------------------------|----------------------------------------------|
| 1 | CARD-NUM                | X(16)            | 16    | String (PK)      | Card Number                | Primary key. 16-char credit card number      |
| 2 | CARD-ACCT-ID            | 9(11)            | 11    | Integer (FK)     | Account Number             | FK to Account entity (ACCT-ID)               |
| 3 | CARD-CVV-CD             | 9(03)            | 3     | Integer          | CVV Code                   | 3-digit card verification value              |
| 4 | CARD-EMBOSSED-NAME      | X(50)            | 50    | String           | Embossed Name              | Name printed on the physical card            |
| 5 | CARD-EXPIRAION-DATE     | X(10)            | 10    | Date (string)    | Card Expiration Date       | Format: YYYY-MM-DD (note: typo in source)    |
| 6 | CARD-ACTIVE-STATUS      | X(01)            | 1     | Flag             | Card Status                | 'Y' = Active, 'N' = Inactive                |
|   | FILLER                  | X(59)            | 59    | -                | Reserved                   | Padding to 150-byte record                   |

**Key Relationships:**
- `CARD-NUM` is referenced by `XREF-CARD-NUM` in Card Cross-Reference
- `CARD-ACCT-ID` is a foreign key to Account entity

---

## 3. Card Cross-Reference Entity

> **Copybook:** `CVACT03Y.cpy` | **Record:** `CARD-XREF-RECORD` | **Length:** 50 bytes
> **VSAM File:** `CCXREF` (KSDS, key = XREF-CARD-NUM) + `CXACAIX` (Alternate Index by ACCT-ID)
> **Java Target:** `CardCrossReference` entity / junction table

| # | COBOL Field             | PIC Clause       | Bytes | Logical Type     | Business Name              | Description / Rules                          |
|---|-------------------------|------------------|-------|------------------|----------------------------|----------------------------------------------|
| 1 | XREF-CARD-NUM           | X(16)            | 16    | String (PK)      | Card Number                | Primary key. Links to Card entity            |
| 2 | XREF-CUST-ID            | 9(09)            | 9     | Integer (FK)     | Customer ID                | FK to Customer entity (CUST-ID)              |
| 3 | XREF-ACCT-ID            | 9(11)            | 11    | Integer (FK)     | Account ID                 | FK to Account entity (ACCT-ID)               |
|   | FILLER                  | X(14)            | 14    | -                | Reserved                   | Padding to 50-byte record                    |

**Key Relationships:**
- Central junction table linking Card <-> Customer <-> Account
- `CXACAIX` alternate index enables lookup by Account ID

---

## 4. Customer Entity

> **Copybook:** `CVCUS01Y.cpy` | **Record:** `CUSTOMER-RECORD` | **Length:** 500 bytes
> **VSAM File:** `CUSTDAT` (KSDS, key = CUST-ID)
> **Java Target:** `Customer` entity / DTO

| # | COBOL Field                  | PIC Clause  | Bytes | Logical Type     | Business Name              | Description / Rules                        |
|---|------------------------------|-------------|-------|------------------|----------------------------|--------------------------------------------|
| 1 | CUST-ID                      | 9(09)       | 9     | Integer (PK)     | Customer ID                | Primary key. 9-digit unique customer ID    |
| 2 | CUST-FIRST-NAME              | X(25)       | 25    | String           | First Name                 | Customer's first name                      |
| 3 | CUST-MIDDLE-NAME             | X(25)       | 25    | String           | Middle Name                | Customer's middle name                     |
| 4 | CUST-LAST-NAME               | X(25)       | 25    | String           | Last Name                  | Customer's last name                       |
| 5 | CUST-ADDR-LINE-1             | X(50)       | 50    | String           | Address Line 1             | Primary street address                     |
| 6 | CUST-ADDR-LINE-2             | X(50)       | 50    | String           | Address Line 2             | Secondary address (apt, suite, etc.)       |
| 7 | CUST-ADDR-LINE-3             | X(50)       | 50    | String           | Address Line 3             | Additional address line                    |
| 8 | CUST-ADDR-STATE-CD           | X(02)       | 2     | Code             | State Code                 | US state abbreviation (e.g., 'NY', 'CA')   |
| 9 | CUST-ADDR-COUNTRY-CD         | X(03)       | 3     | Code             | Country Code               | ISO country code (e.g., 'USA')             |
|10 | CUST-ADDR-ZIP                | X(10)       | 10    | String           | ZIP / Postal Code          | ZIP+4 format supported                     |
|11 | CUST-PHONE-NUM-1             | X(15)       | 15    | Phone            | Primary Phone              | Format: (999)999-9999                      |
|12 | CUST-PHONE-NUM-2             | X(15)       | 15    | Phone            | Secondary Phone            | Format: (999)999-9999                      |
|13 | CUST-SSN                     | 9(09)       | 9     | SSN (PII)        | Social Security Number     | **Sensitive PII** - 9-digit SSN            |
|14 | CUST-GOVT-ISSUED-ID          | X(20)       | 20    | String (PII)     | Government ID              | **Sensitive PII** - driver license, etc.   |
|15 | CUST-DOB-YYYY-MM-DD          | X(10)       | 10    | Date (string)    | Date of Birth              | Format: YYYY-MM-DD. **PII**                |
|16 | CUST-EFT-ACCOUNT-ID          | X(10)       | 10    | String           | EFT Account ID             | Electronic funds transfer account           |
|17 | CUST-PRI-CARD-HOLDER-IND     | X(01)       | 1     | Flag             | Primary Cardholder Flag    | 'Y' = Primary, 'N' = Additional            |
|18 | CUST-FICO-CREDIT-SCORE       | 9(03)       | 3     | Integer          | FICO Credit Score          | Range: 300-850                             |
|   | FILLER                       | X(168)      | 168   | -                | Reserved                   | Padding to 500-byte record                 |

**PII Fields:** CUST-SSN, CUST-GOVT-ISSUED-ID, CUST-DOB-YYYY-MM-DD require encryption/masking in modernized application.

---

## 5. Transaction Entity (Master)

> **Copybook:** `CVTRA05Y.cpy` | **Record:** `TRAN-RECORD` | **Length:** 350 bytes
> **VSAM File:** `TRANSACT` (KSDS, key = TRAN-ID)
> **Java Target:** `Transaction` entity / DTO

| # | COBOL Field             | PIC Clause       | Bytes | Logical Type     | Business Name              | Description / Rules                          |
|---|-------------------------|------------------|-------|------------------|----------------------------|----------------------------------------------|
| 1 | TRAN-ID                 | X(16)            | 16    | String (PK)      | Transaction ID             | Primary key. 16-char unique transaction ID   |
| 2 | TRAN-TYPE-CD            | X(02)            | 2     | Code (FK)        | Transaction Type Code      | FK to Transaction Type (CVTRA03Y)            |
| 3 | TRAN-CAT-CD             | 9(04)            | 4     | Integer (FK)     | Transaction Category Code  | FK to Transaction Category (CVTRA04Y)        |
| 4 | TRAN-SOURCE             | X(10)            | 10    | String           | Transaction Source         | Origin of transaction (POS, ATM, Online)     |
| 5 | TRAN-DESC               | X(100)           | 100   | String           | Description                | Free-text transaction description            |
| 6 | TRAN-AMT                | S9(09)V99        | 11    | Currency (signed)| Transaction Amount         | Signed amount. Positive=debit, Negative=credit|
| 7 | TRAN-MERCHANT-ID        | 9(09)            | 9     | Integer          | Merchant ID                | Unique merchant identifier                   |
| 8 | TRAN-MERCHANT-NAME      | X(50)            | 50    | String           | Merchant Name              | Business name of merchant                    |
| 9 | TRAN-MERCHANT-CITY      | X(50)            | 50    | String           | Merchant City              | City where merchant is located               |
|10 | TRAN-MERCHANT-ZIP       | X(10)            | 10    | String           | Merchant ZIP Code          | Merchant postal code                         |
|11 | TRAN-CARD-NUM           | X(16)            | 16    | String (FK)      | Card Number                | FK to Card entity (CARD-NUM)                 |
|12 | TRAN-ORIG-TS            | X(26)            | 26    | Timestamp        | Origination Timestamp      | DB2 format: YYYY-MM-DD-HH.MM.SS.FFFFFF      |
|13 | TRAN-PROC-TS            | X(26)            | 26    | Timestamp        | Processing Timestamp       | When transaction was posted                  |
|   | FILLER                  | X(20)            | 20    | -                | Reserved                   | Padding to 350-byte record                   |

---

## 6. Daily Transaction Entity

> **Copybook:** `CVTRA06Y.cpy` | **Record:** `DALYTRAN-RECORD` | **Length:** 350 bytes
> **VSAM File:** `DALYTRAN` (Sequential PS file, processed daily)
> **Java Target:** Reuse `Transaction` DTO (same structure as master)

| # | COBOL Field             | PIC Clause       | Bytes | Logical Type     | Business Name              | Description / Rules                          |
|---|-------------------------|------------------|-------|------------------|----------------------------|----------------------------------------------|
| 1 | DALYTRAN-ID             | X(16)            | 16    | String (PK)      | Transaction ID             | Same layout as TRAN-RECORD                   |
| 2 | DALYTRAN-TYPE-CD        | X(02)            | 2     | Code             | Transaction Type Code      | Same as TRAN-TYPE-CD                         |
| 3 | DALYTRAN-CAT-CD         | 9(04)            | 4     | Integer          | Transaction Category Code  | Same as TRAN-CAT-CD                          |
| 4 | DALYTRAN-SOURCE         | X(10)            | 10    | String           | Transaction Source         | Same as TRAN-SOURCE                          |
| 5 | DALYTRAN-DESC           | X(100)           | 100   | String           | Description                | Same as TRAN-DESC                            |
| 6 | DALYTRAN-AMT            | S9(09)V99        | 11    | Currency (signed)| Transaction Amount         | Same as TRAN-AMT                             |
| 7 | DALYTRAN-MERCHANT-ID    | 9(09)            | 9     | Integer          | Merchant ID                | Same as TRAN-MERCHANT-ID                     |
| 8 | DALYTRAN-MERCHANT-NAME  | X(50)            | 50    | String           | Merchant Name              | Same as TRAN-MERCHANT-NAME                   |
| 9 | DALYTRAN-MERCHANT-CITY  | X(50)            | 50    | String           | Merchant City              | Same as TRAN-MERCHANT-CITY                   |
|10 | DALYTRAN-MERCHANT-ZIP   | X(10)            | 10    | String           | Merchant ZIP Code          | Same as TRAN-MERCHANT-ZIP                    |
|11 | DALYTRAN-CARD-NUM       | X(16)            | 16    | String           | Card Number                | Same as TRAN-CARD-NUM                        |
|12 | DALYTRAN-ORIG-TS        | X(26)            | 26    | Timestamp        | Origination Timestamp      | Same as TRAN-ORIG-TS                         |
|13 | DALYTRAN-PROC-TS        | X(26)            | 26    | Timestamp        | Processing Timestamp       | Same as TRAN-PROC-TS                         |
|   | FILLER                  | X(20)            | 20    | -                | Reserved                   | Padding to 350 bytes                         |

**Note:** Daily transaction records are identical in structure to master transaction records. In modernization, these can share the same Java class with a status flag to distinguish staged vs. posted.

---

## 7. User Security Entity

> **Copybook:** `CSUSR01Y.cpy` | **Record:** `SEC-USER-DATA` | **Length:** 80 bytes
> **VSAM File:** `USRSEC` (KSDS, key = SEC-USR-ID)
> **Java Target:** `User` / `SecurityUser` entity

| # | COBOL Field             | PIC Clause       | Bytes | Logical Type     | Business Name              | Description / Rules                          |
|---|-------------------------|------------------|-------|------------------|----------------------------|----------------------------------------------|
| 1 | SEC-USR-ID              | X(08)            | 8     | String (PK)      | User ID                    | Primary key. 8-char login identifier         |
| 2 | SEC-USR-FNAME           | X(20)            | 20    | String           | First Name                 | User's first name                            |
| 3 | SEC-USR-LNAME           | X(20)            | 20    | String           | Last Name                  | User's last name                             |
| 4 | SEC-USR-PWD             | X(08)            | 8     | String (PII)     | Password                   | **Sensitive** - Plain text in legacy. Must hash in Java |
| 5 | SEC-USR-TYPE            | X(01)            | 1     | Flag             | User Type                  | 'A' = Admin, 'U' = Regular User             |
|   | SEC-USR-FILLER          | X(23)            | 23    | -                | Reserved                   | Padding to 80-byte record                    |

**Security Note:** Passwords are stored in plain text in the legacy system. Modernized application MUST implement hashing (e.g., bcrypt) and proper authentication.

---

## 8. Supporting Reference Entities

### 8.1 Transaction Category Balance

> **Copybook:** `CVTRA01Y.cpy` | **Record:** `TRAN-CAT-BAL-RECORD` | **Length:** 50 bytes
> **VSAM File:** `TCATBALF` (KSDS, composite key)

| # | COBOL Field             | PIC Clause       | Bytes | Logical Type     | Business Name              |
|---|-------------------------|------------------|-------|------------------|----------------------------|
| 1 | TRANCAT-ACCT-ID         | 9(11)            | 11    | Integer (PK/FK)  | Account ID                 |
| 2 | TRANCAT-TYPE-CD         | X(02)            | 2     | Code (PK/FK)     | Transaction Type Code      |
| 3 | TRANCAT-CD              | 9(04)            | 4     | Integer (PK/FK)  | Transaction Category Code  |
| 4 | TRAN-CAT-BAL            | S9(09)V99        | 11    | Currency (signed)| Category Balance           |
|   | FILLER                  | X(22)            | 22    | -                | Reserved                   |

### 8.2 Disclosure Group

> **Copybook:** `CVTRA02Y.cpy` | **Record:** `DIS-GROUP-RECORD` | **Length:** 50 bytes
> **VSAM File:** `DISCGRP` (KSDS, composite key)

| # | COBOL Field             | PIC Clause       | Bytes | Logical Type     | Business Name              |
|---|-------------------------|------------------|-------|------------------|----------------------------|
| 1 | DIS-ACCT-GROUP-ID       | X(10)            | 10    | String (PK)      | Account Group ID           |
| 2 | DIS-TRAN-TYPE-CD        | X(02)            | 2     | Code (PK)        | Transaction Type Code      |
| 3 | DIS-TRAN-CAT-CD         | 9(04)            | 4     | Integer (PK)     | Transaction Category Code  |
| 4 | DIS-INT-RATE            | S9(04)V99        | 6     | Decimal (signed) | Interest Rate              |
|   | FILLER                  | X(28)            | 28    | -                | Reserved                   |

### 8.3 Transaction Type

> **Copybook:** `CVTRA03Y.cpy` | **Record:** `TRAN-TYPE-RECORD` | **Length:** 60 bytes
> **VSAM File:** `TRANTYPE` (KSDS, key = TRAN-TYPE)

| # | COBOL Field             | PIC Clause       | Bytes | Logical Type     | Business Name              |
|---|-------------------------|------------------|-------|------------------|----------------------------|
| 1 | TRAN-TYPE               | X(02)            | 2     | Code (PK)        | Transaction Type Code      |
| 2 | TRAN-TYPE-DESC          | X(50)            | 50    | String           | Type Description           |
|   | FILLER                  | X(08)            | 8     | -                | Reserved                   |

### 8.4 Transaction Category

> **Copybook:** `CVTRA04Y.cpy` | **Record:** `TRAN-CAT-RECORD` | **Length:** 60 bytes
> **VSAM File:** `TRANCATG` (KSDS, composite key)

| # | COBOL Field             | PIC Clause       | Bytes | Logical Type     | Business Name              |
|---|-------------------------|------------------|-------|------------------|----------------------------|
| 1 | TRAN-TYPE-CD            | X(02)            | 2     | Code (PK/FK)     | Transaction Type Code      |
| 2 | TRAN-CAT-CD             | 9(04)            | 4     | Integer (PK)     | Category Code              |
| 3 | TRAN-CAT-TYPE-DESC      | X(50)            | 50    | String           | Category Description       |
|   | FILLER                  | X(04)            | 4     | -                | Reserved                   |

---

## 9. Inter-Program Communication Structure

### 9.1 COMMAREA (Application Communication Area)

> **Copybook:** `COCOM01Y.cpy` | **Record:** `CARDDEMO-COMMAREA`
> **Used by:** All 17 online CICS programs
> **Java Target:** Session state / DTO passed between controllers

| # | COBOL Field             | PIC Clause  | Bytes | Logical Type | Business Name              | Description                              |
|---|-------------------------|-------------|-------|--------------|----------------------------|------------------------------------------|
| 1 | CDEMO-FROM-TRANID       | X(04)       | 4     | Code         | Source Transaction ID      | Trans ID that invoked current program    |
| 2 | CDEMO-FROM-PROGRAM      | X(08)       | 8     | Code         | Source Program Name        | Program that transferred control         |
| 3 | CDEMO-TO-TRANID         | X(04)       | 4     | Code         | Target Transaction ID      | Where to return on PF3                   |
| 4 | CDEMO-TO-PROGRAM        | X(08)       | 8     | Code         | Target Program Name        | Program to transfer control to           |
| 5 | CDEMO-USER-ID           | X(08)       | 8     | String       | Current User ID            | Signed-on user ID                        |
| 6 | CDEMO-USER-TYPE         | X(01)       | 1     | Flag         | User Type                  | 'A' = Admin, 'U' = User                 |
| 7 | CDEMO-PGM-CONTEXT       | 9(01)       | 1     | Integer      | Program Context            | 0 = Enter, 1 = Re-enter                 |
| 8 | CDEMO-CUST-ID           | 9(09)       | 9     | Integer      | Selected Customer ID       | Passed between screens                   |
| 9 | CDEMO-CUST-FNAME        | X(25)       | 25    | String       | Customer First Name        | Display name for context                 |
|10 | CDEMO-CUST-MNAME        | X(25)       | 25    | String       | Customer Middle Name       | Display name for context                 |
|11 | CDEMO-CUST-LNAME        | X(25)       | 25    | String       | Customer Last Name         | Display name for context                 |
|12 | CDEMO-ACCT-ID           | 9(11)       | 11    | Integer      | Selected Account ID        | Passed between screens                   |
|13 | CDEMO-ACCT-STATUS       | X(01)       | 1     | Flag         | Account Status             | Passed between screens                   |
|14 | CDEMO-CARD-NUM          | 9(16)       | 16    | Integer      | Selected Card Number       | Passed between screens                   |
|15 | CDEMO-LAST-MAP          | X(07)       | 7     | Code         | Last BMS Map               | For screen navigation tracking           |
|16 | CDEMO-LAST-MAPSET       | X(07)       | 7     | Code         | Last BMS Mapset            | For screen navigation tracking           |

---

## 10. Export/Import Multi-Record Structure

> **Copybook:** `CVEXPORT.cpy` | **Record:** `EXPORT-RECORD` | **Length:** 500 bytes
> **Purpose:** Branch migration / data interchange format

| # | COBOL Field             | PIC Clause      | Bytes | Logical Type | Business Name              |
|---|-------------------------|-----------------|-------|--------------|----------------------------|
| 1 | EXPORT-REC-TYPE         | X(1)            | 1     | Code         | Record Type Discriminator  |
| 2 | EXPORT-TIMESTAMP        | X(26)           | 26    | Timestamp    | Export Timestamp           |
| 3 | EXPORT-SEQUENCE-NUM     | 9(9) COMP       | 4     | Integer      | Sequence Number            |
| 4 | EXPORT-BRANCH-ID        | X(4)            | 4     | String       | Branch Identifier          |
| 5 | EXPORT-REGION-CODE      | X(5)            | 5     | String       | Region Code                |
| 6 | EXPORT-RECORD-DATA      | X(460)          | 460   | Union        | Record Data (REDEFINES)    |

**REDEFINES variants:**
- `EXPORT-CUSTOMER-DATA` — Customer fields with COMP/COMP-3 optimization
- `EXPORT-ACCOUNT-DATA` — Account fields with COMP-3 for balances
- `EXPORT-TRANSACTION-DATA` — Transaction fields with COMP-3 amounts
- `EXPORT-CARD-XREF-DATA` — Cross-reference fields with COMP account ID
- `EXPORT-CARD-DATA` — Card fields with COMP IDs

---

## 11. COBOL to Java Type Mapping Reference

| COBOL PIC Clause          | COBOL Type         | Java Type                  | Notes                                    |
|---------------------------|--------------------|----------------------------|------------------------------------------|
| `PIC 9(n)`                | Unsigned integer   | `int` / `long`             | Use `long` when n > 9                    |
| `PIC S9(n)`               | Signed integer     | `int` / `long`             | Includes sign                            |
| `PIC S9(n)V99`            | Signed decimal     | `BigDecimal`               | Always use BigDecimal for currency       |
| `PIC S9(n)V99 COMP-3`     | Packed decimal     | `BigDecimal`               | Same logical type, different storage     |
| `PIC 9(n) COMP`           | Binary integer     | `int` / `long`             | More compact storage                     |
| `PIC X(n)`                | Alphanumeric       | `String`                   | Trim trailing spaces                     |
| `PIC X(10)` (dates)       | Date string        | `LocalDate`                | Parse YYYY-MM-DD format                  |
| `PIC X(26)` (timestamps)  | Timestamp string   | `LocalDateTime`            | Parse DB2 timestamp format               |
| `PIC X(01)` (flags)       | Flag               | `boolean` or `enum`        | Map 'Y'/'N' to true/false               |
| `PIC X(02)` (codes)       | Code               | `enum` or `String`         | Prefer enum for known value sets         |

---

## 12. Entity Relationship Summary

```
Customer (CVCUS01Y)
    |
    | 1:N via XREF-CUST-ID
    |
Card Cross-Reference (CVACT03Y) -------- Card (CVACT02Y)
    |                                         |
    | N:1 via XREF-ACCT-ID                    | N:1 via CARD-ACCT-ID
    |                                         |
Account (CVACT01Y) ----------------------+
    |                                     |
    | 1:N via card linkage                | 1:N via ACCT-GROUP-ID
    |                                     |
Transaction (CVTRA05Y)            Disclosure Group (CVTRA02Y)
    |
    | N:1 via TRAN-TYPE-CD + TRAN-CAT-CD
    |
Transaction Type (CVTRA03Y) ---- Transaction Category (CVTRA04Y)

User Security (CSUSR01Y) — standalone entity for authentication

Transaction Category Balance (CVTRA01Y) — aggregate by ACCT + TYPE + CAT
```
