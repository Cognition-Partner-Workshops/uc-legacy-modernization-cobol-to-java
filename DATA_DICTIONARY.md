# Data Dictionary — CardDemo COBOL Estate

> Field-level documentation for every copybook in the CardDemo system, grouped by business entity.

---

## Table of Contents

1. [Account Entity](#1-account-entity)
2. [Customer Entity](#2-customer-entity)
3. [Card Entity](#3-card-entity)
4. [Transaction Entity](#4-transaction-entity)
5. [Transaction Reference Data](#5-transaction-reference-data)
6. [Cross-Reference Entity](#6-cross-reference-entity)
7. [User / Security Entity](#7-user--security-entity)
8. [Common / Shared Structures](#8-common--shared-structures)
9. [Reporting Structures](#9-reporting-structures)
10. [Export/Import Structure](#10-exportimport-structure)
11. [Authorization Sub-App Copybooks](#11-authorization-sub-app-copybooks)
12. [DB2 Sub-App Copybooks](#12-db2-sub-app-copybooks)

---

## 1. Account Entity

### CVACT01Y.cpy — Account Master Record (RECLN = 300)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `ACCT-ID` | `9(11)` | Numeric | Unique account identifier | Must be 11-digit numeric |
| `ACCT-ACTIVE-STATUS` | `X(01)` | Alphanumeric | Account status flag | 'Y' = Active, 'N' = Inactive |
| `ACCT-CURR-BAL` | `S9(10)V99` | Signed decimal | Current account balance | Signed, allows negative (overlimit) |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | Signed decimal | Maximum credit limit | Must be positive |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | Signed decimal | Cash advance credit limit | Must be positive, <= credit limit |
| `ACCT-OPEN-DATE` | `X(10)` | Date string | Date account was opened | Format: YYYY-MM-DD |
| `ACCT-EXPIRAION-DATE` | `X(10)` | Date string | Account expiration date | Format: YYYY-MM-DD, must be > open date |
| `ACCT-REISSUE-DATE` | `X(10)` | Date string | Date of last card reissue | Format: YYYY-MM-DD |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | Signed decimal | Current billing cycle credits | Running total of cycle credits |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | Signed decimal | Current billing cycle debits | Running total of cycle debits |
| `ACCT-ADDR-ZIP` | `X(10)` | Alphanumeric | Account holder ZIP code | — |
| `ACCT-GROUP-ID` | `X(10)` | Alphanumeric | Disclosure/interest rate group | Links to disclosure group table |
| `FILLER` | `X(178)` | Filler | Reserved space | — |

### CVACT02Y.cpy — Account Record (Alternate Layout)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `ACCT-ID` | `9(11)` | Numeric | Account identifier | Same as CVACT01Y |
| `ACCT-ACTIVE-STATUS` | `X(01)` | Alphanumeric | Account status | 'Y'/'N' |
| `ACCT-CURR-BAL` | `S9(10)V99` | Signed decimal | Current balance | — |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | Signed decimal | Credit limit | — |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | Signed decimal | Cash credit limit | — |
| `ACCT-OPEN-DATE` | `X(10)` | Date string | Open date | — |
| `ACCT-EXPIRAION-DATE` | `X(10)` | Date string | Expiration date | — |
| `ACCT-REISSUE-DATE` | `X(10)` | Date string | Reissue date | — |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | Signed decimal | Cycle credits | — |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | Signed decimal | Cycle debits | — |
| `ACCT-GROUP-ID` | `X(10)` | Alphanumeric | Disclosure group | — |
| `FILLER` | `X(178)` | Filler | Reserved | — |

### CVACT03Y.cpy — Card Cross-Reference Record (RECLN = 50)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `XREF-CARD-NUM` | `X(16)` | Alphanumeric | Credit card number (primary key) | 16-digit card number |
| `XREF-CUST-ID` | `9(09)` | Numeric | Customer ID who owns the card | Must match a CUSTFILE record |
| `XREF-ACCT-ID` | `9(11)` | Numeric | Account ID linked to card | Must match an ACCTDAT record |
| `FILLER` | `X(14)` | Filler | Reserved | — |

---

## 2. Customer Entity

### CVCUS01Y.cpy — Customer Master Record (RECLN = 500)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `CUST-ID` | `9(09)` | Numeric | Unique customer identifier (primary key) | 9-digit numeric |
| `CUST-FIRST-NAME` | `X(25)` | Alphanumeric | Customer first name | Non-blank |
| `CUST-MIDDLE-NAME` | `X(25)` | Alphanumeric | Customer middle name | Optional |
| `CUST-LAST-NAME` | `X(25)` | Alphanumeric | Customer last name | Non-blank |
| `CUST-ADDR-LINE-1` | `X(50)` | Alphanumeric | Address line 1 | Non-blank |
| `CUST-ADDR-LINE-2` | `X(50)` | Alphanumeric | Address line 2 | Optional |
| `CUST-ADDR-LINE-3` | `X(50)` | Alphanumeric | Address line 3 | Optional |
| `CUST-ADDR-STATE-CD` | `X(02)` | Alphanumeric | State code | 2-letter US state code |
| `CUST-ADDR-COUNTRY-CD` | `X(03)` | Alphanumeric | Country code | 3-letter ISO country code |
| `CUST-ADDR-ZIP` | `X(10)` | Alphanumeric | ZIP/postal code | US ZIP format |
| `CUST-PHONE-NUM-1` | `X(15)` | Alphanumeric | Primary phone number | — |
| `CUST-PHONE-NUM-2` | `X(15)` | Alphanumeric | Secondary phone number | Optional |
| `CUST-SSN` | `9(09)` | Numeric | Social Security Number | 9-digit, sensitive PII |
| `CUST-GOVT-ISSUED-ID` | `X(20)` | Alphanumeric | Government-issued ID number | — |
| `CUST-DOB-YYYY-MM-DD` | `X(10)` | Date string | Date of birth | Format: YYYY-MM-DD |
| `CUST-EFT-ACCOUNT-ID` | `X(10)` | Alphanumeric | Electronic funds transfer account | Bank account for payments |
| `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | Alphanumeric | Primary cardholder indicator | 'Y' = Primary, 'N' = Authorized user |
| `CUST-FICO-CREDIT-SCORE` | `9(03)` | Numeric | FICO credit score | Range: 300-850 |
| `FILLER` | `X(168)` | Filler | Reserved | — |

### CUSTREC.cpy — Customer Record (Statement Layout)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `CUST-ID` | `9(09)` | Numeric | Customer ID | — |
| `CUST-FIRST-NAME` | `X(25)` | Alphanumeric | First name | — |
| `CUST-MIDDLE-NAME` | `X(25)` | Alphanumeric | Middle name | — |
| `CUST-LAST-NAME` | `X(25)` | Alphanumeric | Last name | — |
| `CUST-ADDR-LINE-1` | `X(50)` | Alphanumeric | Address line 1 | — |
| `CUST-ADDR-LINE-2` | `X(50)` | Alphanumeric | Address line 2 | — |
| `CUST-ADDR-LINE-3` | `X(50)` | Alphanumeric | Address line 3 | — |
| `CUST-ADDR-STATE-CD` | `X(02)` | Alphanumeric | State code | — |
| `CUST-ADDR-COUNTRY-CD` | `X(03)` | Alphanumeric | Country code | — |
| `CUST-ADDR-ZIP` | `X(10)` | Alphanumeric | ZIP code | — |
| `CUST-PHONE-NUM-1` | `X(15)` | Alphanumeric | Primary phone | — |
| `CUST-PHONE-NUM-2` | `X(15)` | Alphanumeric | Secondary phone | — |
| `CUST-SSN` | `9(09)` | Numeric | SSN | Sensitive PII |
| `CUST-GOVT-ISSUED-ID` | `X(20)` | Alphanumeric | Govt ID | — |
| `CUST-DOB-YYYY-MM-DD` | `X(10)` | Date string | Date of birth | — |
| `CUST-EFT-ACCOUNT-ID` | `X(10)` | Alphanumeric | EFT account | — |
| `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | Alphanumeric | Primary holder flag | — |
| `CUST-FICO-CREDIT-SCORE` | `9(03)` | Numeric | FICO score | — |
| `FILLER` | `X(168)` | Filler | Reserved | — |

---

## 3. Card Entity

### CVCRD01Y.cpy — Credit Card Record (RECLN = 150)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `CARD-NUM` | `X(16)` | Alphanumeric | Credit card number (primary key) | 16-digit card number |
| `CARD-ACCT-ID` | `9(11)` | Numeric | Linked account ID | Must exist in ACCTDAT |
| `CARD-CVV-CD` | `9(03)` | Numeric | Card verification value | 3-digit CVV |
| `CARD-EMBOSSED-NAME` | `X(50)` | Alphanumeric | Name embossed on card | Cardholder name |
| `CARD-EXPIRAION-DATE` | `X(10)` | Date string | Card expiration date | Format: YYYY-MM-DD |
| `CARD-ACTIVE-STATUS` | `X(01)` | Alphanumeric | Card active status | 'Y' = Active, 'N' = Inactive |
| `FILLER` | `X(59)` | Filler | Reserved | — |

---

## 4. Transaction Entity

### CVTRA05Y.cpy — Transaction Record (RECLN = 350)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `TRAN-ID` | `X(16)` | Alphanumeric | Unique transaction ID | System-generated |
| `TRAN-TYPE-CD` | `X(02)` | Alphanumeric | Transaction type code | Must exist in TRANTYPE file |
| `TRAN-CAT-CD` | `9(04)` | Numeric | Transaction category code | Must exist in TRANCATG file |
| `TRAN-SOURCE` | `X(10)` | Alphanumeric | Transaction source | e.g., 'Online', 'POS', 'System' |
| `TRAN-DESC` | `X(100)` | Alphanumeric | Transaction description | Free-text description |
| `TRAN-AMT` | `S9(09)V99` | Signed decimal | Transaction amount | Positive=debit, Negative=credit |
| `TRAN-MERCHANT-ID` | `9(09)` | Numeric | Merchant identifier | — |
| `TRAN-MERCHANT-NAME` | `X(50)` | Alphanumeric | Merchant name | — |
| `TRAN-MERCHANT-CITY` | `X(50)` | Alphanumeric | Merchant city | — |
| `TRAN-MERCHANT-ZIP` | `X(10)` | Alphanumeric | Merchant ZIP code | — |
| `TRAN-CARD-NUM` | `X(16)` | Alphanumeric | Card number used | Must exist in CARDXREF |
| `TRAN-ORIG-TS` | `X(26)` | Alphanumeric | Original timestamp | DB2 timestamp format |
| `TRAN-PROC-TS` | `X(26)` | Alphanumeric | Processing timestamp | DB2 timestamp format |
| `FILLER` | `X(20)` | Filler | Reserved | — |

### CVTRA06Y.cpy — Daily Transaction Record (RECLN = 350)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `DALYTRAN-ID` | `X(16)` | Alphanumeric | Daily transaction ID | Unique per day |
| `DALYTRAN-TYPE-CD` | `X(02)` | Alphanumeric | Transaction type code | Same codes as TRAN-TYPE-CD |
| `DALYTRAN-CAT-CD` | `9(04)` | Numeric | Transaction category | Same as TRAN-CAT-CD |
| `DALYTRAN-SOURCE` | `X(10)` | Alphanumeric | Source system | — |
| `DALYTRAN-DESC` | `X(100)` | Alphanumeric | Description | — |
| `DALYTRAN-AMT` | `S9(09)V99` | Signed decimal | Transaction amount | — |
| `DALYTRAN-MERCHANT-ID` | `9(09)` | Numeric | Merchant ID | — |
| `DALYTRAN-MERCHANT-NAME` | `X(50)` | Alphanumeric | Merchant name | — |
| `DALYTRAN-MERCHANT-CITY` | `X(50)` | Alphanumeric | Merchant city | — |
| `DALYTRAN-MERCHANT-ZIP` | `X(10)` | Alphanumeric | Merchant ZIP | — |
| `DALYTRAN-CARD-NUM` | `X(16)` | Alphanumeric | Card number | — |
| `DALYTRAN-ORIG-TS` | `X(26)` | Alphanumeric | Original timestamp | — |
| `DALYTRAN-PROC-TS` | `X(26)` | Alphanumeric | Processing timestamp | — |
| `FILLER` | `X(20)` | Filler | Reserved | — |

### COSTM01.CPY — Transaction Record (Statement Layout, key = CARD+TRAN-ID)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `TRNX-CARD-NUM` | `X(16)` | Alphanumeric | Card number (part of composite key) | — |
| `TRNX-ID` | `X(16)` | Alphanumeric | Transaction ID (part of composite key) | — |
| `TRNX-TYPE-CD` | `X(02)` | Alphanumeric | Transaction type | — |
| `TRNX-CAT-CD` | `9(04)` | Numeric | Transaction category | — |
| `TRNX-SOURCE` | `X(10)` | Alphanumeric | Source | — |
| `TRNX-DESC` | `X(100)` | Alphanumeric | Description | — |
| `TRNX-AMT` | `S9(09)V99` | Signed decimal | Amount | — |
| `TRNX-MERCHANT-ID` | `9(09)` | Numeric | Merchant ID | — |
| `TRNX-MERCHANT-NAME` | `X(50)` | Alphanumeric | Merchant name | — |
| `TRNX-MERCHANT-CITY` | `X(50)` | Alphanumeric | Merchant city | — |
| `TRNX-MERCHANT-ZIP` | `X(10)` | Alphanumeric | Merchant ZIP | — |
| `TRNX-ORIG-TS` | `X(26)` | Alphanumeric | Original timestamp | — |
| `TRNX-PROC-TS` | `X(26)` | Alphanumeric | Processing timestamp | — |
| `FILLER` | `X(20)` | Filler | Reserved | — |

---

## 5. Transaction Reference Data

### CVTRA01Y.cpy — Transaction Category Balance (RECLN = 50)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `TRANCAT-ACCT-ID` | `9(11)` | Numeric | Account ID (part of composite key) | Must exist in ACCTDAT |
| `TRANCAT-TYPE-CD` | `X(02)` | Alphanumeric | Transaction type (part of key) | Must exist in TRANTYPE |
| `TRANCAT-CD` | `9(04)` | Numeric | Transaction category (part of key) | Must exist in TRANCATG |
| `TRAN-CAT-BAL` | `S9(09)V99` | Signed decimal | Balance for this account/type/category | Running balance |
| `FILLER` | `X(22)` | Filler | Reserved | — |

### CVTRA02Y.cpy — Disclosure Group (RECLN = 50)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `DIS-ACCT-GROUP-ID` | `X(10)` | Alphanumeric | Disclosure group ID (part of key) | Links to ACCT-GROUP-ID; 'DEFAULT' as fallback |
| `DIS-TRAN-TYPE-CD` | `X(02)` | Alphanumeric | Transaction type (part of key) | — |
| `DIS-TRAN-CAT-CD` | `9(04)` | Numeric | Transaction category (part of key) | — |
| `DIS-INT-RATE` | `S9(04)V99` | Signed decimal | Interest rate (annual %) | Used in interest calculation: `(balance * rate) / 1200` |
| `FILLER` | `X(28)` | Filler | Reserved | — |

### CVTRA03Y.cpy — Transaction Type (RECLN = 60)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `TRAN-TYPE` | `X(02)` | Alphanumeric | Transaction type code (primary key) | 2-character code (e.g., '01', '02') |
| `TRAN-TYPE-DESC` | `X(50)` | Alphanumeric | Type description | e.g., 'Purchase', 'Cash Advance', 'Payment' |
| `FILLER` | `X(08)` | Filler | Reserved | — |

### CVTRA04Y.cpy — Transaction Category Type (RECLN = 60)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `TRAN-TYPE-CD` | `X(02)` | Alphanumeric | Transaction type (part of composite key) | Must exist in TRANTYPE |
| `TRAN-CAT-CD` | `9(04)` | Numeric | Category code (part of composite key) | — |
| `TRAN-CAT-TYPE-DESC` | `X(50)` | Alphanumeric | Category description | e.g., 'Retail Purchase', 'Online Purchase' |
| `FILLER` | `X(04)` | Filler | Reserved | — |

---

## 6. Cross-Reference Entity

### CVACT03Y.cpy — Card-to-Account Cross-Reference (RECLN = 50)

(See [Account Entity](#cvact03ycpy--card-cross-reference-record-recln--50) above — this copybook maps cards to customers and accounts.)

---

## 7. User / Security Entity

### CSUSR01Y.cpy — User Security Record (RECLN = 80)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `SEC-USR-ID` | `X(08)` | Alphanumeric | User login ID (primary key) | Unique, non-blank |
| `SEC-USR-FNAME` | `X(20)` | Alphanumeric | User first name | — |
| `SEC-USR-LNAME` | `X(20)` | Alphanumeric | User last name | — |
| `SEC-USR-PWD` | `X(08)` | Alphanumeric | User password | Sensitive credential |
| `SEC-USR-TYPE` | `X(01)` | Alphanumeric | User type/role | 'A' = Admin, 'U' = Regular User |
| `SEC-USR-FILLER` | `X(23)` | Filler | Reserved | — |

### UNUSED1Y.cpy — Unused Data Record (same layout as CSUSR01Y)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `UNUSED-ID` | `X(08)` | Alphanumeric | Placeholder ID | Unused copybook |
| `UNUSED-FNAME` | `X(20)` | Alphanumeric | Placeholder first name | — |
| `UNUSED-LNAME` | `X(20)` | Alphanumeric | Placeholder last name | — |
| `UNUSED-PWD` | `X(08)` | Alphanumeric | Placeholder password | — |
| `UNUSED-TYPE` | `X(01)` | Alphanumeric | Placeholder type | — |
| `UNUSED-FILLER` | `X(23)` | Filler | Reserved | — |

---

## 8. Common / Shared Structures

### COCOM01Y.cpy — Common Communication Area (COMMAREA)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `CDEMO-FROM-TRANID` | `X(04)` | Alphanumeric | Originating CICS transaction ID | — |
| `CDEMO-FROM-PROGRAM` | `X(08)` | Alphanumeric | Originating program name | — |
| `CDEMO-TO-TRANID` | `X(04)` | Alphanumeric | Target CICS transaction ID | — |
| `CDEMO-TO-PROGRAM` | `X(08)` | Alphanumeric | Target program name | — |
| `CDEMO-PGM-REENTER` | `X(01)` | Alphanumeric | Program re-entry flag | 'Y' = re-entering program |
| `CDEMO-USR-ID` | `X(08)` | Alphanumeric | Current user ID | — |
| `CDEMO-USR-TYP` | `X(01)` | Alphanumeric | Current user type | 'A' = Admin |
| `CDEMO-USR-SEC-LVL` | `9(02)` | Numeric | User security level | — |
| `CDEMO-ACCT-ID` | `9(11)` | Numeric | Selected account ID | Passed between screens |
| `CDEMO-CARD-NUM` | `X(16)` | Alphanumeric | Selected card number | Passed between screens |
| `CDEMO-CUST-ID` | `9(09)` | Numeric | Selected customer ID | — |
| `CDEMO-LAST-MAP` | `X(07)` | Alphanumeric | Last BMS map displayed | — |
| `CDEMO-LAST-MAPSET` | `X(07)` | Alphanumeric | Last BMS mapset used | — |

### COADM02Y.cpy — Admin Menu Options

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `CDEMO-ADMIN-OPT-COUNT` | `9(02)` | Numeric | Number of admin options available | — |
| `CDEMO-ADMIN-OPT-PGMNAME` | `X(08)` | Alphanumeric | Program name for each option (array) | Occurs 10 times |
| `CDEMO-ADMIN-OPT-TRNNAME` | `X(04)` | Alphanumeric | Transaction name for each option (array) | Occurs 10 times |
| `CDEMO-ADMIN-OPT-DESCP` | `X(40)` | Alphanumeric | Description of each option (array) | Occurs 10 times |

### COMEN02Y.cpy — Main Menu Options

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `CDEMO-MENU-OPT-COUNT` | `9(02)` | Numeric | Number of menu options | — |
| `CDEMO-MENU-OPT-PGMNAME` | `X(08)` | Alphanumeric | Program for each option (array) | Occurs 10 times |
| `CDEMO-MENU-OPT-TRNNAME` | `X(04)` | Alphanumeric | Transaction for each option (array) | Occurs 10 times |
| `CDEMO-MENU-OPT-DESCP` | `X(40)` | Alphanumeric | Description (array) | Occurs 10 times |

### COTTL01Y.cpy — Title/Header Line

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `CCDA-TITLE01` | `X(40)` | Alphanumeric | Application title line 1 | Constant: 'AWS CardDemo' |
| `CCDA-TITLE02` | `X(40)` | Alphanumeric | Application title line 2 | — |
| `CCDA-THANK-YOU` | `X(40)` | Alphanumeric | Thank-you message | — |
| `CCDA-PGM-TITLE` | `X(40)` | Alphanumeric | Current program title | Set per-screen |

### CSDAT01Y.cpy — Date/Time Working Storage

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `WS-CURDATE` | `X(08)` | Alphanumeric | Current date (YYYYMMDD) | From CURRENT-DATE |
| `WS-CURTIME` | `X(08)` | Alphanumeric | Current time (HHMMSSss) | From CURRENT-DATE |
| `WS-CURDATE-DISPLAY` | `X(10)` | Alphanumeric | Formatted date (YYYY/MM/DD) | Display format |
| `WS-CURTIME-DISPLAY` | `X(08)` | Alphanumeric | Formatted time (HH:MM:SS) | Display format |

### CSMSG01Y.cpy — Message Area (Short)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `WS-MSG` | `X(80)` | Alphanumeric | Message text for screen display | — |
| `WS-RETURN-MSG` | `X(80)` | Alphanumeric | Return message to calling program | — |

### CSMSG02Y.cpy — Message Area (Long)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `WS-LONG-MSG` | `X(500)` | Alphanumeric | Extended message text | For detailed error messages |

### CSSETATY.cpy — Screen Attribute Settings

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `CCARD-AID-ENTER` | `X(01)` | Alphanumeric | AID key for Enter | — |
| `CCARD-AID-PFK03` | `X(01)` | Alphanumeric | AID key for PF3 | — |
| `CCARD-AID-PFK07` | `X(01)` | Alphanumeric | AID key for PF7 | — |
| `CCARD-AID-PFK08` | `X(01)` | Alphanumeric | AID key for PF8 | — |

### CSSTRPFY.cpy — String Processing Flags

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `WS-STRIP-RESULT` | `X(256)` | Alphanumeric | Result of string strip operation | — |
| `WS-STRIP-LEN` | `S9(04) COMP` | Binary | Length of stripped string | — |

### CSLKPCDY.cpy — Lookup Code Structure

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `WS-LOOKUP-CODE` | `X(10)` | Alphanumeric | Code to look up | — |
| `WS-LOOKUP-DESC` | `X(50)` | Alphanumeric | Description returned | — |

### CSUTLDPY.cpy — Date Utility Parameters

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `CSUTLDTC-DATE` | `X(10)` | Alphanumeric | Date to validate | — |
| `CSUTLDTC-DATE-FORMAT` | `X(10)` | Alphanumeric | Date format mask | e.g., 'YYYY-MM-DD' |
| `CSUTLDTC-RESULT-SEV-CD` | `X(04)` | Alphanumeric | Severity code from CEEDAYS | '0000' = success |
| `CSUTLDTC-RESULT-MSG-NUM` | `X(04)` | Alphanumeric | Message number | '2513' = valid date |

### CSUTLDWY.cpy — Date Utility Working Storage

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `WS-DATE-TO-TEST` | Variable-length string | Alphanumeric | Date value being validated | — |
| `WS-DATE-FORMAT` | Variable-length string | Alphanumeric | Format mask for validation | — |
| `OUTPUT-LILLIAN` | `S9(9) BINARY` | Binary | Lillian date output from CEEDAYS | — |

### CODATECN.cpy — Date Conversion Record

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `CODATECN-INP-DATE` | `X(10)` | Alphanumeric | Input date to convert | — |
| `CODATECN-TYPE` | `X(01)` | Alphanumeric | Input date type | '1' = MM/DD/YYYY, '2' = YYYY-MM-DD |
| `CODATECN-OUTTYPE` | `X(01)` | Alphanumeric | Output date type | '1' = MM/DD/YYYY, '2' = YYYY-MM-DD |
| `CODATECN-0UT-DATE` | `X(10)` | Alphanumeric | Converted output date | — |

---

## 9. Reporting Structures

### CVTRA07Y.cpy — Transaction Report Data Structure

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `REPT-SHORT-NAME` | `X(38)` | Alphanumeric | Report short name | 'DALYREPT' |
| `REPT-LONG-NAME` | `X(41)` | Alphanumeric | Report title | 'Daily Transaction Report' |
| `REPT-DATE-HEADER` | `X(12)` | Alphanumeric | Date range label | 'Date Range: ' |
| `REPT-START-DATE` | `X(10)` | Alphanumeric | Report start date | — |
| `REPT-END-DATE` | `X(10)` | Alphanumeric | Report end date | — |
| `TRAN-REPORT-TRANS-ID` | `X(16)` | Alphanumeric | Transaction ID in report | — |
| `TRAN-REPORT-ACCOUNT-ID` | `X(11)` | Alphanumeric | Account ID in report | — |
| `TRAN-REPORT-TYPE-CD` | `X(02)` | Alphanumeric | Type code in report | — |
| `TRAN-REPORT-TYPE-DESC` | `X(15)` | Alphanumeric | Type description | — |
| `TRAN-REPORT-CAT-CD` | `9(04)` | Numeric | Category code | — |
| `TRAN-REPORT-CAT-DESC` | `X(29)` | Alphanumeric | Category description | — |
| `TRAN-REPORT-SOURCE` | `X(10)` | Alphanumeric | Source | — |
| `TRAN-REPORT-AMT` | `-ZZZ,ZZZ,ZZZ.ZZ` | Edited numeric | Amount (formatted) | — |
| `REPT-PAGE-TOTAL` | `+ZZZ,ZZZ,ZZZ.ZZ` | Edited numeric | Page total | — |
| `REPT-ACCOUNT-TOTAL` | `+ZZZ,ZZZ,ZZZ.ZZ` | Edited numeric | Account total | — |
| `REPT-GRAND-TOTAL` | `+ZZZ,ZZZ,ZZZ.ZZ` | Edited numeric | Grand total | — |

---

## 10. Export/Import Structure

### CVEXPORT.cpy — Data Export Record

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `EXPORT-REC-TYPE` | `X(01)` | Alphanumeric | Record type identifier | 'C'=Customer, 'A'=Account, 'X'=Xref, 'T'=Transaction, 'D'=Card |
| `EXPORT-SEQUENCE-NUM` | `9(07)` | Numeric | Export sequence number | Sequential counter |
| _Customer fields_ | (various) | — | Maps to CVCUS01Y fields | When REC-TYPE = 'C' |
| _Account fields_ | (various) | — | Maps to CVACT01Y fields | When REC-TYPE = 'A' |
| _Xref fields_ | (various) | — | Maps to CVACT03Y fields | When REC-TYPE = 'X' |
| _Transaction fields_ | (various) | — | Maps to CVTRA05Y fields | When REC-TYPE = 'T' |
| _Card fields_ | (various) | — | Maps to CVCRD01Y fields | When REC-TYPE = 'D' |

---

## 11. Authorization Sub-App Copybooks

### CCPAURQY.cpy — Payment Authorization Request

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `PAURQ-CARD-NUM` | `X(16)` | Alphanumeric | Card number for authorization | — |
| `PAURQ-TRAN-AMT` | `S9(09)V99` | Signed decimal | Transaction amount to authorize | — |
| `PAURQ-TRAN-TYPE` | `X(02)` | Alphanumeric | Transaction type | — |
| `PAURQ-MERCHANT-ID` | `9(09)` | Numeric | Merchant requesting auth | — |
| `PAURQ-TIMESTAMP` | `X(26)` | Alphanumeric | Request timestamp | — |

### CCPAURLY.cpy — Payment Authorization Reply

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `PAURL-AUTH-CODE` | `X(06)` | Alphanumeric | Authorization code | Generated on approval |
| `PAURL-RESP-CD` | `X(02)` | Alphanumeric | Response code | '00'=Approved, '05'=Declined |
| `PAURL-RESP-MSG` | `X(50)` | Alphanumeric | Response message | — |
| `PAURL-TIMESTAMP` | `X(26)` | Alphanumeric | Response timestamp | — |

### CCPAUERY.cpy — Payment Authorization Error

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `PAUERR-ERROR-CD` | `X(04)` | Alphanumeric | Error code | — |
| `PAUERR-ERROR-MSG` | `X(80)` | Alphanumeric | Error message | — |

### CIPAUDTY.cpy — Authorization Detail

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `PAUDT-AUTH-ID` | `X(16)` | Alphanumeric | Authorization record ID | — |
| `PAUDT-CARD-NUM` | `X(16)` | Alphanumeric | Card number | — |
| `PAUDT-TRAN-AMT` | `S9(09)V99` | Signed decimal | Transaction amount | — |
| `PAUDT-AUTH-STATUS` | `X(01)` | Alphanumeric | Authorization status | 'A'=Approved, 'D'=Declined, 'P'=Pending |
| `PAUDT-AUTH-CODE` | `X(06)` | Alphanumeric | Auth code | — |

### CIPAUSMY.cpy — Authorization Summary

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `PAUSM-TOTAL-AUTHS` | `9(09)` | Numeric | Total authorizations processed | — |
| `PAUSM-APPROVED` | `9(09)` | Numeric | Number approved | — |
| `PAUSM-DECLINED` | `9(09)` | Numeric | Number declined | — |
| `PAUSM-TOTAL-AMT` | `S9(13)V99` | Signed decimal | Total dollar amount | — |

### IMSFUNCS.cpy — IMS DL/I Function Codes

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `FUNC-GU` | `X(04)` | Alphanumeric | Get Unique | Value: 'GU  ' |
| `FUNC-GN` | `X(04)` | Alphanumeric | Get Next | Value: 'GN  ' |
| `FUNC-GHN` | `X(04)` | Alphanumeric | Get Hold Next | Value: 'GHN ' |
| `FUNC-GHU` | `X(04)` | Alphanumeric | Get Hold Unique | Value: 'GHU ' |
| `FUNC-REPL` | `X(04)` | Alphanumeric | Replace | Value: 'REPL' |
| `FUNC-ISRT` | `X(04)` | Alphanumeric | Insert | Value: 'ISRT' |
| `FUNC-DLET` | `X(04)` | Alphanumeric | Delete | Value: 'DLET' |
| `PARMCOUNT` | `S9(05) COMP-5` | Binary | DL/I parameter count | Value: +4 |

### PCB Copybooks (PAUTBPCB.CPY, PADFLPCB.CPY, PASFLPCB.CPY)

These define IMS Program Communication Blocks:

| Field Name Pattern | PIC Clause | Data Type | Business Meaning |
|-------------------|-----------|-----------|------------------|
| `*-DBDNAME` | `X(08)` | Alphanumeric | Database Description name |
| `*-SEG-LEVEL` | `X(02)` | Alphanumeric | Segment level number |
| `*-PCB-STATUS` | `X(02)` | Alphanumeric | Status code (spaces = success) |
| `*-PCB-PROCOPT` | `X(04)` | Alphanumeric | Processing options |
| `*-SEG-NAME` | `X(08)` | Alphanumeric | Current segment name |
| `*-KEYFB` | `X(255)` | Alphanumeric | Key feedback area |

---

## 12. DB2 Sub-App Copybooks

### CSDB2RWY.cpy — DB2 Common Working Storage

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|------------------|-----------------|
| `WS-DISP-SQLCODE` | `----9` | Edited numeric | Display-formatted SQLCODE | — |
| `WS-DUMMY-DB2-INT` | `S9(4) COMP-3` | Packed decimal | Dummy variable for DB2 connectivity test | — |
| `WS-DB2-PROCESSING-FLAG` | `X(1)` | Alphanumeric | DB2 processing status | '0'=OK, '1'=Error |
| `WS-DB2-CURRENT-ACTION` | `X(72)` | Alphanumeric | Description of current DB2 action | For error messages |
| `WS-DSNTIAC-FORMATTED` | Group | — | DSNTIAC message construction area | 10 lines x 72 chars |
| `WS-DSNTIAC-LRECL` | `S9(4) COMP` | Binary | Logical record length for DSNTIAC | Value: +72 |

### CSDB2RPY.cpy — DB2 Common Procedures

Contains inline PERFORM paragraphs (not field definitions):

| Paragraph | Purpose |
|-----------|---------|
| `9998-PRIMING-QUERY` | Executes `SELECT 1 FROM SYSIBM.SYSDUMMY1` to verify DB2 connectivity |
| `9999-FORMAT-DB2-MESSAGE` | Calls DSNTIAC utility to format SQLCA error messages |

---

## Summary

| Business Entity | Primary Copybook | Record Length | Key Fields |
|----------------|-----------------|--------------|------------|
| Account | CVACT01Y | 300 bytes | ACCT-ID (11 numeric) |
| Customer | CVCUS01Y | 500 bytes | CUST-ID (9 numeric) |
| Card | CVCRD01Y | 150 bytes | CARD-NUM (16 alpha) |
| Transaction | CVTRA05Y | 350 bytes | TRAN-ID (16 alpha) |
| Daily Transaction | CVTRA06Y | 350 bytes | DALYTRAN-ID (16 alpha) |
| Transaction Cat Balance | CVTRA01Y | 50 bytes | ACCT-ID + TYPE-CD + CAT-CD |
| Disclosure Group | CVTRA02Y | 50 bytes | GROUP-ID + TYPE-CD + CAT-CD |
| Transaction Type | CVTRA03Y | 60 bytes | TRAN-TYPE (2 alpha) |
| Transaction Category | CVTRA04Y | 60 bytes | TYPE-CD + CAT-CD |
| Card Cross-Reference | CVACT03Y | 50 bytes | XREF-CARD-NUM (16 alpha) |
| User Security | CSUSR01Y | 80 bytes | SEC-USR-ID (8 alpha) |
