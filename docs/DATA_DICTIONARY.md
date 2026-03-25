# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** COBOL Copybooks in `app/cpy/` and `app/app-*/cpy/`
> **Purpose:** Business-friendly extraction of all data entities, their fields, types, and sizes from COBOL PIC clauses

---

## Table of Contents

1. [VSAM File Summary](#vsam-file-summary)
2. [Account Master (CVACT01Y)](#account-master-cvact01y)
3. [Card Data (CVACT02Y)](#card-data-cvact02y)
4. [Card Cross-Reference (CVACT03Y)](#card-cross-reference-cvact03y)
5. [Extended Card Record (CVCRD01Y)](#extended-card-record-cvcrd01y)
6. [Customer Data (CVCUS01Y)](#customer-data-cvcus01y)
7. [Transaction Record (CVTRA05Y)](#transaction-record-cvtra05y)
8. [Daily Transaction Record (CVTRA06Y)](#daily-transaction-record-cvtra06y)
9. [Transaction Category Balance (CVTRA01Y)](#transaction-category-balance-cvtra01y)
10. [Disclosure Group / Interest Rate (CVTRA02Y)](#disclosure-group--interest-rate-cvtra02y)
11. [Transaction Type (CVTRA03Y)](#transaction-type-cvtra03y)
12. [Transaction Category Type (CVTRA04Y)](#transaction-category-type-cvtra04y)
13. [Transaction Report Layout (CVTRA07Y)](#transaction-report-layout-cvtra07y)
14. [Statement Transaction Layout (COSTM01)](#statement-transaction-layout-costm01)
15. [User Security Record (CSUSR01Y)](#user-security-record-csusr01y)
16. [Common Communication Area (COCOM01Y)](#common-communication-area-cocom01y)
17. [Menu Definitions (COMEN02Y)](#menu-definitions-comen02y)
18. [Admin Menu Definitions (COADM02Y)](#admin-menu-definitions-coadm02y)
19. [Title / Header Layout (COTTL01Y)](#title--header-layout-cottl01y)
20. [Date Handling (CSDAT01Y)](#date-handling-csdat01y)
21. [Message Areas (CSMSG01Y / CSMSG02Y)](#message-areas-csmsg01y--csmsg02y)
22. [Export / Import Layout (CVEXPORT)](#export--import-layout-cvexport)
23. [Customer Record Alternate (CUSTREC)](#customer-record-alternate-custrec)
24. [Lookup Code Table (CSLKPCDY)](#lookup-code-table-cslkpcdy)
25. [Date Utility Structures (CSUTLDPY / CSUTLDWY)](#date-utility-structures-csutldpy--csutldwy)
26. [Date Conversion (CODATECN)](#date-conversion-codatecn)
27. [Attribute Setting (CSSETATY)](#attribute-setting-cssetaty)
28. [String Parsing (CSSTRPFY)](#string-parsing-csstrpfy)
29. [Unused Placeholder (UNUSED1Y)](#unused-placeholder-unused1y)
30. [Optional: IMS Pending Auth Detail (CIPAUDTY)](#optional-ims-pending-auth-detail-cipaudty)
31. [Optional: IMS Pending Auth Summary (CIPAUSMY)](#optional-ims-pending-auth-summary-cipausmy)
32. [Optional: IMS Authorization Activity (CIPAUATY)](#optional-ims-authorization-activity-cipauaty)
33. [Optional: IMS Function Codes (IMSFUNCS)](#optional-ims-function-codes-imsfuncs)
34. [Optional: DB2 Common Working Storage (CSDB2RWY)](#optional-db2-common-working-storage-csdb2rwy)
35. [PIC Clause Reference](#pic-clause-reference)

---

## VSAM File Summary

| Business Entity | VSAM Dataset | Record Length | Key | Copybook | Java Target |
|---|---|---|---|---|---|
| Account Master | `ACCTDATA.VSAM.KSDS` | 300 bytes | Account ID (11 digits) | CVACT01Y | `Account` entity |
| Card Data | `CARDDATA.VSAM.KSDS` | 150 bytes | Card Number (16 chars) | CVACT02Y | `Card` entity |
| Card Cross-Reference | `CARDXREF.VSAM.KSDS` | variable | Card Num + Account ID | CVACT03Y | `CardAccountXref` entity |
| Customer Data | `CUSTDATA.VSAM.KSDS` | 500 bytes | Customer ID (9 digits) | CVCUS01Y | `Customer` entity |
| Transaction Master | `TRANSACT.VSAM.KSDS` | 350 bytes | Transaction ID (16 chars) | CVTRA05Y | `Transaction` entity |
| Daily Transactions | `DALYTRAN` (sequential) | 350 bytes | None (sequential) | CVTRA06Y | `DailyTransaction` DTO |
| Category Balance | `TCATBALF.VSAM.KSDS` | 50 bytes | Account ID + Type + Cat | CVTRA01Y | `CategoryBalance` entity |
| Disclosure Group | `DISCGRP.VSAM.KSDS` | 50 bytes | Group ID + Type + Cat | CVTRA02Y | `DisclosureGroup` entity |
| Transaction Type | `TRANTYPE.VSAM.KSDS` | 60 bytes | Type Code (2 chars) | CVTRA03Y | `TransactionType` entity |
| Transaction Category | `TRANCATG.VSAM.KSDS` | 60 bytes | Type + Category Code | CVTRA04Y | `TransactionCategory` entity |
| User Security | `USRSEC.VSAM.KSDS` | ~80 bytes | User ID (8 chars) | CSUSR01Y | `UserSecurity` entity |

---

## Account Master (CVACT01Y)

**Copybook:** `app/cpy/CVACT01Y.cpy` | **Record Length:** 300 bytes | **VSAM:** `ACCTDATA.VSAM.KSDS`

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `ACCT-ID` | `9(11)` | `long` | 11 digits | Account identifier (primary key) |
| `ACCT-ACTIVE-STATUS` | `X(01)` | `String` | 1 char | Account status: 'Y' = Active, 'N' = Inactive |
| `ACCT-CURR-BAL` | `S9(10)V99` | `BigDecimal` | 12 digits + sign | Current account balance |
| `ACCT-CREDIT-LIMIT` | `S9(10)V99` | `BigDecimal` | 12 digits + sign | Credit limit |
| `ACCT-CASH-CREDIT-LIMIT` | `S9(10)V99` | `BigDecimal` | 12 digits + sign | Cash advance credit limit |
| `ACCT-OPEN-DATE` | `X(10)` | `LocalDate` | 10 chars | Account opening date (YYYY-MM-DD) |
| `ACCT-EXPIRAION-DATE` | `X(10)` | `LocalDate` | 10 chars | Account expiration date |
| `ACCT-REISSUE-DATE` | `X(10)` | `LocalDate` | 10 chars | Last reissue date |
| `ACCT-CURR-CYC-CREDIT` | `S9(10)V99` | `BigDecimal` | 12 digits + sign | Current cycle credit amount |
| `ACCT-CURR-CYC-DEBIT` | `S9(10)V99` | `BigDecimal` | 12 digits + sign | Current cycle debit amount |
| `ACCT-ADDR-ZIP` | `X(10)` | `String` | 10 chars | Account holder ZIP code |
| `ACCT-GROUP-ID` | `X(10)` | `String` | 10 chars | Disclosure group ID for interest rates |
| FILLER | `X(178)` | — | 178 bytes | Reserved space |

---

## Card Data (CVACT02Y)

**Copybook:** `app/cpy/CVACT02Y.cpy` | **Record Length:** 150 bytes | **VSAM:** `CARDDATA.VSAM.KSDS`

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `CARD-NUM` | `X(16)` | `String` | 16 chars | Credit card number (primary key) |
| `CARD-ACCT-ID` | `9(11)` | `long` | 11 digits | Associated account ID (FK) |
| `CARD-CVV-CD` | `9(03)` | `int` | 3 digits | Card verification value |
| `CARD-EMBOSSED-NAME` | `X(50)` | `String` | 50 chars | Name embossed on card |
| `CARD-EXPIRAION-DATE` | `X(10)` | `LocalDate` | 10 chars | Card expiration date |
| `CARD-ACTIVE-STATUS` | `X(01)` | `String` | 1 char | Card active status flag |
| FILLER | `X(59)` | — | 59 bytes | Reserved space |

---

## Card Cross-Reference (CVACT03Y)

**Copybook:** `app/cpy/CVACT03Y.cpy` | **VSAM:** `CARDXREF.VSAM.KSDS`

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `XREF-CARD-NUM` | `X(16)` | `String` | 16 chars | Card number (composite key part 1) |
| `XREF-CUST-ID` | `9(09)` | `long` | 9 digits | Customer ID (FK to Customer) |
| `XREF-ACCT-ID` | `9(11)` | `long` | 11 digits | Account ID (FK to Account) |
| FILLER | `X(14)` | — | 14 bytes | Reserved space |

> **Business Role:** Links cards to customers and accounts. A customer can have multiple cards; each card is associated with one account.

---

## Extended Card Record (CVCRD01Y)

**Copybook:** `app/cpy/CVCRD01Y.cpy` | **Used by:** Online CICS programs for card management screens

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `CCARD-ACCT-ID-N` | `9(11)` | `long` | 11 digits | Account number |
| `CCARD-CARD-NUM-N` | `9(16)` | `long` | 16 digits | Card number (numeric) |
| `CCARD-CVV-CD-N` | `9(03)` | `int` | 3 digits | CVV code |
| `CCARD-AID-ENTER` | `X(01)` | `boolean` | 1 char | Enter key pressed flag |
| `CCARD-AID-CLEAR` | `X(01)` | `boolean` | 1 char | Clear key pressed flag |
| `CCARD-AID-PFK03` | `X(01)` | `boolean` | 1 char | PF3 key (back) flag |
| `CCARD-AID-PFK07` | `X(01)` | `boolean` | 1 char | PF7 key (page up) flag |
| `CCARD-AID-PFK08` | `X(01)` | `boolean` | 1 char | PF8 key (page down) flag |
| `CCARD-LAST-MAP` | `X(07)` | `String` | 7 chars | Last displayed BMS map name |
| `CCARD-LAST-MAPSET` | `X(07)` | `String` | 7 chars | Last displayed mapset name |
| `CCARD-LAST-PGM` | `X(08)` | `String` | 8 chars | Last executing program name |
| `CCARD-NEXT-MAP` | `X(07)` | `String` | 7 chars | Next map to display |
| `CCARD-NEXT-MAPSET` | `X(07)` | `String` | 7 chars | Next mapset to display |
| `CCARD-NEXT-PGM` | `X(08)` | `String` | 8 chars | Next program to XCTL to |

> **Business Role:** Extended working storage for card operations, including UI navigation state.

---

## Customer Data (CVCUS01Y)

**Copybook:** `app/cpy/CVCUS01Y.cpy` | **Record Length:** 500 bytes | **VSAM:** `CUSTDATA.VSAM.KSDS`

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `CUST-ID` | `9(09)` | `long` | 9 digits | Customer ID (primary key) |
| `CUST-FIRST-NAME` | `X(25)` | `String` | 25 chars | First name |
| `CUST-MIDDLE-NAME` | `X(25)` | `String` | 25 chars | Middle name |
| `CUST-LAST-NAME` | `X(25)` | `String` | 25 chars | Last name |
| `CUST-ADDR-LINE-1` | `X(50)` | `String` | 50 chars | Address line 1 |
| `CUST-ADDR-LINE-2` | `X(50)` | `String` | 50 chars | Address line 2 |
| `CUST-ADDR-LINE-3` | `X(50)` | `String` | 50 chars | Address line 3 |
| `CUST-ADDR-STATE-CD` | `X(02)` | `String` | 2 chars | State code |
| `CUST-ADDR-COUNTRY-CD` | `X(03)` | `String` | 3 chars | Country code |
| `CUST-ADDR-ZIP` | `X(10)` | `String` | 10 chars | ZIP/postal code |
| `CUST-PHONE-NUM-1` | `X(15)` | `String` | 15 chars | Primary phone number |
| `CUST-PHONE-NUM-2` | `X(15)` | `String` | 15 chars | Secondary phone number |
| `CUST-SSN` | `9(09)` | `String` | 9 digits | Social Security Number (PII) |
| `CUST-GOVT-ISSUED-ID` | `X(20)` | `String` | 20 chars | Government-issued ID |
| `CUST-DOB-YYYYMMDD` | `X(10)` | `LocalDate` | 10 chars | Date of birth |
| `CUST-EFT-ACCOUNT-ID` | `X(10)` | `String` | 10 chars | Electronic funds transfer account |
| `CUST-PRI-CARD-HOLDER-IND` | `X(01)` | `String` | 1 char | Primary card holder indicator |
| `CUST-FICO-CREDIT-SCORE` | `9(03)` | `int` | 3 digits | FICO credit score |
| FILLER | `X(168)` | — | 168 bytes | Reserved space |

> **PII Notice:** Contains SSN (`CUST-SSN`), date of birth, and full address — requires encryption and access controls in the modernized application.

---

## Transaction Record (CVTRA05Y)

**Copybook:** `app/cpy/CVTRA05Y.cpy` | **Record Length:** 350 bytes | **VSAM:** `TRANSACT.VSAM.KSDS`

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `TRAN-ID` | `X(16)` | `String` | 16 chars | Transaction ID (primary key) |
| `TRAN-TYPE-CD` | `X(02)` | `String` | 2 chars | Transaction type code (FK to TRANTYPE) |
| `TRAN-CAT-CD` | `9(04)` | `int` | 4 digits | Transaction category code |
| `TRAN-SOURCE` | `X(10)` | `String` | 10 chars | Transaction source (e.g., POS, ATM, Online) |
| `TRAN-DESC` | `X(100)` | `String` | 100 chars | Transaction description |
| `TRAN-AMT` | `S9(09)V99` | `BigDecimal` | 11 digits + sign | Transaction amount |
| `TRAN-MERCHANT-ID` | `9(09)` | `long` | 9 digits | Merchant identifier |
| `TRAN-MERCHANT-NAME` | `X(50)` | `String` | 50 chars | Merchant name |
| `TRAN-MERCHANT-CITY` | `X(50)` | `String` | 50 chars | Merchant city |
| `TRAN-MERCHANT-ZIP` | `X(10)` | `String` | 10 chars | Merchant ZIP code |
| `TRAN-CARD-NUM` | `X(16)` | `String` | 16 chars | Card number used (FK to Card) |
| `TRAN-ORIG-TS` | `X(26)` | `LocalDateTime` | 26 chars | Transaction origination timestamp |
| `TRAN-PROC-TS` | `X(26)` | `LocalDateTime` | 26 chars | Transaction processing timestamp |
| FILLER | `X(20)` | — | 20 bytes | Reserved space |

---

## Daily Transaction Record (CVTRA06Y)

**Copybook:** `app/cpy/CVTRA06Y.cpy` | **Record Length:** 350 bytes | **File:** `DALYTRAN` (sequential)

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `DALYTRAN-ID` | `X(16)` | `String` | 16 chars | Daily transaction ID |
| `DALYTRAN-TYPE-CD` | `X(02)` | `String` | 2 chars | Transaction type code |
| `DALYTRAN-CAT-CD` | `9(04)` | `int` | 4 digits | Transaction category code |
| `DALYTRAN-SOURCE` | `X(10)` | `String` | 10 chars | Transaction source |
| `DALYTRAN-DESC` | `X(100)` | `String` | 100 chars | Transaction description |
| `DALYTRAN-AMT` | `S9(09)V99` | `BigDecimal` | 11 digits + sign | Transaction amount |
| `DALYTRAN-MERCHANT-ID` | `9(09)` | `long` | 9 digits | Merchant identifier |
| `DALYTRAN-MERCHANT-NAME` | `X(50)` | `String` | 50 chars | Merchant name |
| `DALYTRAN-MERCHANT-CITY` | `X(50)` | `String` | 50 chars | Merchant city |
| `DALYTRAN-MERCHANT-ZIP` | `X(10)` | `String` | 10 chars | Merchant ZIP |
| `DALYTRAN-CARD-NUM` | `X(16)` | `String` | 16 chars | Card number used |
| `DALYTRAN-ORIG-TS` | `X(26)` | `LocalDateTime` | 26 chars | Origination timestamp |
| `DALYTRAN-PROC-TS` | `X(26)` | `LocalDateTime` | 26 chars | Processing timestamp |
| FILLER | `X(20)` | — | 20 bytes | Reserved |

> **Note:** Identical structure to CVTRA05Y. Daily transactions are posted to the master (CVTRA05Y) by CBTRN02C.

---

## Transaction Category Balance (CVTRA01Y)

**Copybook:** `app/cpy/CVTRA01Y.cpy` | **Record Length:** 50 bytes | **VSAM:** `TCATBALF.VSAM.KSDS`

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `TRANCAT-ACCT-ID` | `9(11)` | `long` | 11 digits | Account ID (composite key part 1) |
| `TRANCAT-TYPE-CD` | `X(02)` | `String` | 2 chars | Transaction type (composite key part 2) |
| `TRANCAT-CD` | `9(04)` | `int` | 4 digits | Category code (composite key part 3) |
| `TRAN-CAT-BAL` | `S9(09)V99` | `BigDecimal` | 11 digits + sign | Balance for this category |
| FILLER | `X(22)` | — | 22 bytes | Reserved |

> **Business Role:** Tracks running balance per account per transaction type/category. Used by interest calculator (CBACT04C).

---

## Disclosure Group / Interest Rate (CVTRA02Y)

**Copybook:** `app/cpy/CVTRA02Y.cpy` | **Record Length:** 50 bytes | **VSAM:** `DISCGRP.VSAM.KSDS`

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `DIS-ACCT-GROUP-ID` | `X(10)` | `String` | 10 chars | Disclosure group ID (composite key part 1) |
| `DIS-TRAN-TYPE-CD` | `X(02)` | `String` | 2 chars | Transaction type (composite key part 2) |
| `DIS-TRAN-CAT-CD` | `9(04)` | `int` | 4 digits | Category code (composite key part 3) |
| `DIS-INT-RATE` | `S9(04)V99` | `BigDecimal` | 6 digits + sign | Interest rate for this group/type/category |
| FILLER | `X(28)` | — | 28 bytes | Reserved |

> **Business Role:** Defines interest rates per disclosure group, transaction type, and category. Joined to accounts via `ACCT-GROUP-ID`.

---

## Transaction Type (CVTRA03Y)

**Copybook:** `app/cpy/CVTRA03Y.cpy` | **Record Length:** 60 bytes | **VSAM:** `TRANTYPE.VSAM.KSDS`

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `TRAN-TYPE` | `X(02)` | `String` | 2 chars | Transaction type code (primary key) |
| `TRAN-TYPE-DESC` | `X(50)` | `String` | 50 chars | Type description (e.g., "Purchase", "Cash Advance") |
| FILLER | `X(08)` | — | 8 bytes | Reserved |

---

## Transaction Category Type (CVTRA04Y)

**Copybook:** `app/cpy/CVTRA04Y.cpy` | **Record Length:** 60 bytes | **VSAM:** `TRANCATG.VSAM.KSDS`

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `TRAN-TYPE-CD` | `X(02)` | `String` | 2 chars | Transaction type (composite key part 1) |
| `TRAN-CAT-CD` | `9(04)` | `int` | 4 digits | Category code (composite key part 2) |
| `TRAN-CAT-TYPE-DESC` | `X(50)` | `String` | 50 chars | Category description |
| FILLER | `X(04)` | — | 4 bytes | Reserved |

---

## Transaction Report Layout (CVTRA07Y)

**Copybook:** `app/cpy/CVTRA07Y.cpy` | **Used by:** CBTRN03C (report generation)

This copybook defines print-line layouts rather than a data record:

| Structure | Purpose |
|---|---|
| `REPORT-NAME-HEADER` | Report name ("DALYREPT"), title, and date range |
| `TRANSACTION-DETAIL-REPORT` | One line per transaction: ID, Account, Type, Category, Source, Amount |
| `TRANSACTION-HEADER-1` / `HEADER-2` | Column headers and separator line |
| `REPORT-PAGE-TOTALS` | Page subtotal line |
| `REPORT-ACCOUNT-TOTALS` | Account subtotal line |
| `REPORT-GRAND-TOTALS` | Grand total line |

---

## Statement Transaction Layout (COSTM01)

**Copybook:** `app/cpy/COSTM01.CPY` | **Used by:** CBSTM03A (statement generation)

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `TRNX-CARD-NUM` | `X(16)` | `String` | 16 chars | Card number (composite key part 1) |
| `TRNX-ID` | `X(16)` | `String` | 16 chars | Transaction ID (composite key part 2) |
| `TRNX-TYPE-CD` | `X(02)` | `String` | 2 chars | Transaction type code |
| `TRNX-CAT-CD` | `9(04)` | `int` | 4 digits | Transaction category code |
| `TRNX-SOURCE` | `X(10)` | `String` | 10 chars | Transaction source |
| `TRNX-DESC` | `X(100)` | `String` | 100 chars | Transaction description |
| `TRNX-AMT` | `S9(09)V99` | `BigDecimal` | 11 digits + sign | Transaction amount |
| `TRNX-MERCHANT-ID` | `9(09)` | `long` | 9 digits | Merchant ID |
| `TRNX-MERCHANT-NAME` | `X(50)` | `String` | 50 chars | Merchant name |
| `TRNX-MERCHANT-CITY` | `X(50)` | `String` | 50 chars | Merchant city |
| `TRNX-MERCHANT-ZIP` | `X(10)` | `String` | 10 chars | Merchant ZIP |
| `TRNX-ORIG-TS` | `X(26)` | `LocalDateTime` | 26 chars | Original timestamp |
| `TRNX-PROC-TS` | `X(26)` | `LocalDateTime` | 26 chars | Processed timestamp |
| FILLER | `X(20)` | — | 20 bytes | Reserved |

> **Note:** Key is `CARD-NUM + TRAN-ID` (sorted by card for statement grouping), unlike CVTRA05Y which keys on `TRAN-ID` alone.

---

## User Security Record (CSUSR01Y)

**Copybook:** `app/cpy/CSUSR01Y.cpy` | **VSAM:** `USRSEC.VSAM.KSDS`

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `SEC-USR-ID` | `X(08)` | `String` | 8 chars | User ID (primary key) |
| `SEC-USR-FNAME` | `X(20)` | `String` | 20 chars | First name |
| `SEC-USR-LNAME` | `X(20)` | `String` | 20 chars | Last name |
| `SEC-USR-PWD` | `X(08)` | `String` | 8 chars | Password (plaintext — must encrypt in Java) |
| `SEC-USR-TYPE` | `X(01)` | `String` | 1 char | User type: 'A' = Admin, 'U' = Regular User |
| FILLER | `X(23)` | — | 23 bytes | Reserved |

> **Security Notice:** Password is stored in plaintext. Modernized application must use hashed passwords with salt.

---

## Common Communication Area (COCOM01Y)

**Copybook:** `app/cpy/COCOM01Y.cpy` | **Used by:** All online CICS programs

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `CDEMO-FROM-TRANID` | `X(04)` | `String` | 4 chars | Originating CICS transaction ID |
| `CDEMO-FROM-PROGRAM` | `X(08)` | `String` | 8 chars | Calling program name |
| `CDEMO-TO-TRANID` | `X(04)` | `String` | 4 chars | Target transaction ID |
| `CDEMO-TO-PROGRAM` | `X(08)` | `String` | 8 chars | Target program name for XCTL |
| `CDEMO-USER-ID` | `X(08)` | `String` | 8 chars | Logged-in user ID |
| `CDEMO-USER-TYPE` | `X(01)` | `String` | 1 char | User type (A/U) |
| `CDEMO-PGM-REENTER` | `88-level` | `boolean` | Flag | Re-entry flag |
| `CDEMO-ACCT-ID` | `9(11)` | `long` | 11 digits | Current account context |
| `CDEMO-CARD-NUM` | `X(16)` | `String` | 16 chars | Current card context |
| `CDEMO-CUST-ID` | `9(09)` | `long` | 9 digits | Current customer context |
| `CDEMO-LAST-MAP` | `X(07)` | `String` | 7 chars | Last displayed map |
| `CDEMO-LAST-MAPSET` | `X(07)` | `String` | 7 chars | Last displayed mapset |

> **Business Role:** Session state passed between programs via CICS COMMAREA. Maps to HTTP session or JWT claims in Java.

---

## Menu Definitions (COMEN02Y)

**Copybook:** `app/cpy/COMEN02Y.cpy` | **Used by:** COMEN01C (Main Menu)

Defines the regular user menu options with program names, transaction IDs, and display text for the main menu screen. Contains arrays of menu items mapping option numbers to target CICS programs.

---

## Admin Menu Definitions (COADM02Y)

**Copybook:** `app/cpy/COADM02Y.cpy` | **Used by:** COADM01C (Admin Menu)

Defines the admin menu options with program names, transaction IDs, and display text for the admin menu screen.

---

## Title / Header Layout (COTTL01Y)

**Copybook:** `app/cpy/COTTL01Y.cpy` | **Used by:** All online CICS programs

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `CCDA-TITLE01` | `X(40)` | `String` | 40 chars | Screen title line 1 |
| `CCDA-TITLE02` | `X(40)` | `String` | 40 chars | Screen title line 2 |
| `CCDA-PGMNAME` | `X(08)` | `String` | 8 chars | Current program name display |
| `CCDA-TRNNAME` | `X(04)` | `String` | 4 chars | Current transaction name display |
| `CCDA-CURDATE` | `X(08)` | `String` | 8 chars | Current date display |
| `CCDA-CURTIME` | `X(08)` | `String` | 8 chars | Current time display |

---

## Date Handling (CSDAT01Y)

**Copybook:** `app/cpy/CSDAT01Y.cpy` | **Used by:** All online CICS programs

Contains date/time working storage variables used for CICS ASKTIME/FORMATTIME and date display formatting. Includes fields for year, month, day, and formatted date strings.

---

## Message Areas (CSMSG01Y / CSMSG02Y)

**CSMSG01Y** (`app/cpy/CSMSG01Y.cpy`): Short message area for status/error messages displayed at the bottom of screens. Used by all online programs.

**CSMSG02Y** (`app/cpy/CSMSG02Y.cpy`): Extended message area for longer diagnostic messages. Used by COACTUPC, COCRDUPC, COTRTUPC (complex update screens).

---

## Export / Import Layout (CVEXPORT)

**Copybook:** `app/cpy/CVEXPORT.cpy` | **Used by:** CBEXPORT, CBIMPORT

Multi-record export layout for branch migration data. Contains:

| Record Type | Prefix | Content |
|---|---|---|
| Header | `EXP-HDR` | Export timestamp, record counts, version |
| Customer | `EXP-CUST` | Full customer record (mirrors CVCUS01Y) |
| Account | `EXP-ACCT` | Full account record (mirrors CVACT01Y) |
| Card | `EXP-CARD` | Card number, account ID, CVV, name, expiry, status |
| Cross-Reference | `EXP-XREF` | Card-account-customer cross-reference |
| Transaction | `EXP-TRAN` | Transaction records |
| Trailer | `EXP-TRL` | Record counts and checksums |

Each record includes: `EXP-RECORD-TYPE` (`X(02)`), `EXP-RECORD-LEN` (`9(05)`), and type-specific payload.

---

## Customer Record Alternate (CUSTREC)

**Copybook:** `app/cpy/CUSTREC.cpy` | **Used by:** CBSTM03A (statement generation)

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `CUST-ID` | `9(09)` | `long` | 9 digits | Customer ID |
| `CUST-FIRST-NAME` | `X(25)` | `String` | 25 chars | First name |
| `CUST-MIDDLE-NAME` | `X(25)` | `String` | 25 chars | Middle name |
| `CUST-LAST-NAME` | `X(25)` | `String` | 25 chars | Last name |
| `CUST-ADDR-LINE-1` | `X(50)` | `String` | 50 chars | Address line 1 |
| `CUST-ADDR-LINE-2` | `X(50)` | `String` | 50 chars | Address line 2 |
| `CUST-ADDR-LINE-3` | `X(50)` | `String` | 50 chars | Address line 3 |
| `CUST-ADDR-STATE-CD` | `X(02)` | `String` | 2 chars | State code |
| `CUST-ADDR-COUNTRY-CD` | `X(03)` | `String` | 3 chars | Country code |
| `CUST-ADDR-ZIP` | `X(10)` | `String` | 10 chars | ZIP code |

> **Note:** Subset of CVCUS01Y used specifically for statement address block.

---

## Lookup Code Table (CSLKPCDY)

**Copybook:** `app/cpy/CSLKPCDY.cpy` | **Size:** 51 KB | **Used by:** COACTUPC

Large in-memory lookup table containing code/description pairs for account update validation. Includes state codes, country codes, and other reference data. In Java, this should be replaced by a database reference table or enum.

---

## Date Utility Structures (CSUTLDPY / CSUTLDWY)

**CSUTLDPY** (`app/cpy/CSUTLDPY.cpy`): Parameter layout for the CSUTLDTC date validation utility. Contains input date, format picture, and output validity flag.

**CSUTLDWY** (`app/cpy/CSUTLDWY.cpy`): Working storage extension for programs that call CSUTLDTC. Contains date validation work areas and formatted date fields.

---

## Date Conversion (CODATECN)

**Copybook:** `app/cpy/CODATECN.cpy` | **Used by:** CBACT01C

Record layout for the COBDATFT assembler date formatting routine. Contains input date field and output formatted date field.

---

## Attribute Setting (CSSETATY)

**Copybook:** `app/cpy/CSSETATY.cpy` | **Used by:** COACTUPC, COTRTUPC (via COPY REPLACING)

Reusable paragraph for setting BMS field attributes (color, highlight, protection) based on field validation status. Used with COPY REPLACING to apply to different fields.

---

## String Parsing (CSSTRPFY)

**Copybook:** `app/cpy/CSSTRPFY.cpy` | **Used by:** COTRTLIC, COTRTUPC

Reusable paragraph for string parsing and trimming operations. Used for processing user input fields.

---

## Unused Placeholder (UNUSED1Y)

**Copybook:** `app/cpy/UNUSED1Y.cpy` | **Used by:** None (dead code)

| Field Name | COBOL PIC | Size | Notes |
|---|---|---|---|
| `UNUSED-ID` | `X(08)` | 8 chars | Placeholder ID |
| `UNUSED-FNAME` | `X(20)` | 20 chars | Placeholder first name |
| `UNUSED-LNAME` | `X(20)` | 20 chars | Placeholder last name |
| `UNUSED-PWD` | `X(08)` | 8 chars | Placeholder password |
| `UNUSED-TYPE` | `X(01)` | 1 char | Placeholder type |
| `UNUSED-FILLER` | `X(23)` | 23 bytes | Filler |

> **Note:** Dead code. Appears to be an earlier version of CSUSR01Y. Safe to exclude from migration.

---

## Optional: IMS Pending Auth Detail (CIPAUDTY)

**Copybook:** `app/app-authorization-ims-db2-mq/cpy/CIPAUDTY.cpy`

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `PA-CARD-NUM` | `X(16)` | `String` | 16 chars | Card number |
| `PA-AUTH-AMOUNT` | `S9(09)V99 COMP-3` | `BigDecimal` | Packed | Authorization amount |
| `PA-AUTH-DATE` | `X(08)` | `LocalDate` | 8 chars | Authorization date |
| `PA-AUTH-TIME` | `X(06)` | `LocalTime` | 6 chars | Authorization time |
| `PA-AUTH-MERCHANT-ID` | `9(09)` | `long` | 9 digits | Merchant ID |
| `PA-AUTH-MERCHANT-NAME` | `X(50)` | `String` | 50 chars | Merchant name |
| `PA-AUTH-MATCH-STATUS` | `X(01)` | `String` | 1 char | Match status (P=Pending, D=Declined, E=Expired, M=Matched) |
| `PA-AUTH-FRAUD` | `X(01)` | `String` | 1 char | Fraud flag (F=Confirmed, R=Removed) |
| `PA-FRAUD-RPT-DATE` | `X(08)` | `LocalDate` | 8 chars | Fraud report date |

---

## Optional: IMS Pending Auth Summary (CIPAUSMY)

**Copybook:** `app/app-authorization-ims-db2-mq/cpy/CIPAUSMY.cpy`

| Field Name | COBOL PIC | Java Type | Size | Business Description |
|---|---|---|---|---|
| `PA-ACCT-ID` | `S9(11) COMP-3` | `long` | Packed | Account ID |
| `PA-CUST-ID` | `9(09)` | `long` | 9 digits | Customer ID |
| `PA-AUTH-STATUS` | `X(01)` | `String` | 1 char | Authorization status |
| `PA-ACCOUNT-STATUS` | `X(02) OCCURS 5` | `String[]` | 5×2 chars | Account status array |
| `PA-CREDIT-LIMIT` | `S9(09)V99 COMP-3` | `BigDecimal` | Packed | Credit limit |
| `PA-CASH-LIMIT` | `S9(09)V99 COMP-3` | `BigDecimal` | Packed | Cash limit |
| `PA-CREDIT-BALANCE` | `S9(09)V99 COMP-3` | `BigDecimal` | Packed | Credit balance |
| `PA-CASH-BALANCE` | `S9(09)V99 COMP-3` | `BigDecimal` | Packed | Cash balance |
| `PA-APPROVED-AUTH-CNT` | `S9(04) COMP` | `int` | Binary | Approved authorization count |
| `PA-DECLINED-AUTH-CNT` | `S9(04) COMP` | `int` | Binary | Declined authorization count |
| `PA-APPROVED-AUTH-AMT` | `S9(09)V99 COMP-3` | `BigDecimal` | Packed | Approved authorization total |
| `PA-DECLINED-AUTH-AMT` | `S9(09)V99 COMP-3` | `BigDecimal` | Packed | Declined authorization total |

---

## Optional: IMS Authorization Activity (CIPAUATY)

**Copybook:** `app/app-authorization-ims-db2-mq/cpy/CIPAUATY.cpy`

Contains authorization activity fields for the IMS database segment used by the authorization module.

---

## Optional: IMS Function Codes (IMSFUNCS)

**Copybook:** `app/app-authorization-ims-db2-mq/cpy/IMSFUNCS.cpy`

| Field Name | Value | Purpose |
|---|---|---|
| `FUNC-GU` | `'GU  '` | Get Unique (direct read) |
| `FUNC-GHU` | `'GHU '` | Get Hold Unique (read for update) |
| `FUNC-GN` | `'GN  '` | Get Next (sequential read) |
| `FUNC-GHN` | `'GHN '` | Get Hold Next |
| `FUNC-GNP` | `'GNP '` | Get Next within Parent |
| `FUNC-GHNP` | `'GHNP'` | Get Hold Next within Parent |
| `FUNC-REPL` | `'REPL'` | Replace (update) |
| `FUNC-ISRT` | `'ISRT'` | Insert |
| `FUNC-DLET` | `'DLET'` | Delete |

> **Note:** Standard IMS DL/I function codes. Maps to JPA repository methods in Java.

---

## Optional: DB2 Common Working Storage (CSDB2RWY)

**Copybook:** `app/app-transaction-type-db2/cpy/CSDB2RWY.cpy`

| Field Name | COBOL PIC | Purpose |
|---|---|---|
| `WS-DISP-SQLCODE` | `----9` | Display format for SQLCODE |
| `WS-DUMMY-DB2-INT` | `S9(4) COMP-3` | Dummy variable for DB2 priming query |
| `WS-DB2-PROCESSING-FLAG` | `X(1)` | DB2 status: '0' = OK, '1' = Error |
| `WS-DB2-CURRENT-ACTION` | `X(72)` | Current DB2 action description |
| `WS-DSNTIAC-FORMATTED` | Group | DSNTIAC message formatting area (10×72 chars) |
| `WS-DSNTIAC-LRECL` | `S9(4) COMP` | DSNTIAC logical record length |

---

## PIC Clause Reference

Quick reference for translating COBOL PIC clauses to Java types:

| COBOL PIC | Meaning | Java Type | Example |
|---|---|---|---|
| `X(n)` | Alphanumeric, n characters | `String` | `PIC X(16)` → 16-char string |
| `9(n)` | Unsigned numeric, n digits | `int` / `long` | `PIC 9(11)` → `long` |
| `S9(n)V99` | Signed decimal, n+2 digits | `BigDecimal` | `PIC S9(09)V99` → 11-digit decimal |
| `S9(n) COMP` | Binary integer | `int` / `long` | `PIC S9(04) COMP` → `short`/`int` |
| `S9(n) COMP-3` | Packed decimal | `BigDecimal` | `PIC S9(09)V99 COMP-3` → `BigDecimal` |
| `9(n) OCCURS m` | Array of n-digit numbers | `int[]` / `long[]` | Repeating group |
| FILLER | Unused space padding | — | Skip in Java mapping |
