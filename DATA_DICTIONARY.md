# Data Dictionary — CardDemo COBOL Estate

> Extracted from all copybooks in `app/cpy/` and sub-application `cpy/` directories.
> Fields are grouped by business entity.

---

## Table of Contents

1. [Account Entity](#1-account-entity)
2. [Customer Entity](#2-customer-entity)
3. [Card Entity](#3-card-entity)
4. [Transaction Entity](#4-transaction-entity)
5. [User / Security Entity](#5-user--security-entity)
6. [Common / Shared Structures](#6-common--shared-structures)
7. [Authorization Sub-Application Structures](#7-authorization-sub-application-structures)
8. [DB2 Transaction Type Sub-Application Structures](#8-db2-transaction-type-sub-application-structures)

---

## 1. Account Entity

### CVACT01Y.cpy — Account Master Record (RECLN = 300)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | ACCT-RECORD | — | Group | Account master record | Record length = 300 bytes |
| 05 | ACCT-ID | 9(11) | Numeric | Unique account identifier | 11-digit numeric; primary key of ACCTDATA VSAM KSDS |
| 05 | ACCT-ACTIVE-STATUS | X(01) | Alphanumeric | Account active/inactive flag | 'Y' = Active, 'N' = Inactive |
| 05 | ACCT-CURR-BAL | S9(10)V99 | Signed Decimal | Current account balance | Signed; 10 integer + 2 decimal digits |
| 05 | ACCT-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | Credit limit for the account | Signed; must be > 0 for active accounts |
| 05 | ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | Signed Decimal | Cash advance credit limit | Signed; typically ≤ ACCT-CREDIT-LIMIT |
| 05 | ACCT-OPEN-DATE | X(10) | Alphanumeric | Date account was opened | Format: YYYY-MM-DD |
| 05 | ACCT-EXPIRAION-DATE | X(10) | Alphanumeric | Account expiration date | Format: YYYY-MM-DD (note: misspelling in source) |
| 05 | ACCT-REISSUE-DATE | X(10) | Alphanumeric | Date account was last reissued | Format: YYYY-MM-DD |
| 05 | ACCT-CURR-CYC-CREDIT | S9(10)V99 | Signed Decimal | Credits in current billing cycle | Signed |
| 05 | ACCT-CURR-CYC-DEBIT | S9(10)V99 | Signed Decimal | Debits in current billing cycle | Signed |
| 05 | ACCT-ADDR-ZIP | X(10) | Alphanumeric | Account holder ZIP code | US ZIP or ZIP+4 |
| 05 | ACCT-GROUP-ID | X(10) | Alphanumeric | Disclosure/interest-rate group | Links to DIS-GROUP-RECORD |
| 05 | FILLER | X(178) | — | Reserved space | Padding to 300 bytes |

### CVACT02Y.cpy — Card Cross-Reference Record (RECLN = 50)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | CARD-XREF-RECORD | — | Group | Card-to-account cross-reference | Record length = 50 bytes |
| 05 | XREF-CARD-NUM | X(16) | Alphanumeric | Card number (primary key) | 16-character card number |
| 05 | XREF-CUST-ID | 9(09) | Numeric | Customer ID owning this card | Links to CUST-ID in customer file |
| 05 | XREF-ACCT-ID | 9(11) | Numeric | Account ID linked to this card | Links to ACCT-ID in account file |
| 05 | FILLER | X(14) | — | Reserved space | Padding to 50 bytes |

### CVACT03Y.cpy — Account-Card Combined Record (RECLN = 50)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | ACCT-XREF-RECORD | — | Group | Account cross-reference record | Used in statement processing |
| 05 | ACCT-XREF-ACCT-ID | 9(11) | Numeric | Account identifier | 11-digit numeric |
| 05 | ACCT-XREF-CUST-ID | 9(09) | Numeric | Customer identifier | 9-digit numeric |
| 05 | FILLER | X(30) | — | Reserved space | Padding to 50 bytes |

---

## 2. Customer Entity

### CVCUS01Y.cpy — Customer Master Record (RECLN = 500)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | CUST-RECORD | — | Group | Customer master record | Record length = 500 bytes |
| 05 | CUST-ID | 9(09) | Numeric | Unique customer identifier | 9-digit numeric; primary key of CUSTDATA VSAM KSDS |
| 05 | CUST-FIRST-NAME | X(25) | Alphanumeric | Customer first name | Left-justified, space-padded |
| 05 | CUST-MIDDLE-NAME | X(25) | Alphanumeric | Customer middle name | Optional |
| 05 | CUST-LAST-NAME | X(25) | Alphanumeric | Customer last name | Required |
| 05 | CUST-ADDR-LINE-1 | X(50) | Alphanumeric | Street address line 1 | Required for active customers |
| 05 | CUST-ADDR-LINE-2 | X(50) | Alphanumeric | Street address line 2 | Optional |
| 05 | CUST-ADDR-LINE-3 | X(50) | Alphanumeric | Street address line 3 | Optional |
| 05 | CUST-ADDR-STATE-CD | X(02) | Alphanumeric | US state code | 2-letter state abbreviation |
| 05 | CUST-ADDR-COUNTRY-CD | X(03) | Alphanumeric | Country code | ISO 3-letter country code |
| 05 | CUST-ADDR-ZIP | X(10) | Alphanumeric | ZIP / postal code | US ZIP or ZIP+4 |
| 05 | CUST-PHONE-NUM-1 | X(15) | Alphanumeric | Primary phone number | Formatted phone number |
| 05 | CUST-PHONE-NUM-2 | X(15) | Alphanumeric | Secondary phone number | Optional |
| 05 | CUST-SSN | 9(09) | Numeric | Social Security Number | 9-digit SSN; sensitive PII |
| 05 | CUST-GOVT-ISSUED-ID | X(20) | Alphanumeric | Government-issued ID number | Driver's license or passport |
| 05 | CUST-DOB-YYYYMMDD | X(10) | Alphanumeric | Date of birth | Format: YYYY-MM-DD |
| 05 | CUST-EFT-ACCOUNT-ID | X(10) | Alphanumeric | EFT/ACH account number | For electronic fund transfers |
| 05 | CUST-PRI-CARD-HOLDER-IND | X(01) | Alphanumeric | Primary card holder indicator | 'Y' = Primary, 'N' = Authorized user |
| 05 | CUST-FICO-CREDIT-SCORE | 9(03) | Numeric | FICO credit score | Range: 300–850 |
| 05 | FILLER | X(168) | — | Reserved space | Padding to 500 bytes |

### CUSTREC.cpy — Customer Record (Statement Processing Layout)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | CUSTOMER-RECORD | — | Group | Customer record for statements | Used by CBSTM03A |
| 05 | CUST-ID | X(09) | Alphanumeric | Customer identifier | 9 characters |
| 05 | CUST-FIRST-NAME | X(25) | Alphanumeric | First name | — |
| 05 | CUST-MIDDLE-NAME | X(25) | Alphanumeric | Middle name | — |
| 05 | CUST-LAST-NAME | X(25) | Alphanumeric | Last name | — |
| 05 | CUST-ADDR-LINE-1 | X(50) | Alphanumeric | Address line 1 | — |
| 05 | CUST-ADDR-LINE-2 | X(50) | Alphanumeric | Address line 2 | — |
| 05 | CUST-ADDR-LINE-3 | X(50) | Alphanumeric | Address line 3 | — |
| 05 | CUST-ADDR-STATE-CD | X(02) | Alphanumeric | State code | — |
| 05 | CUST-ADDR-COUNTRY-CD | X(03) | Alphanumeric | Country code | — |
| 05 | CUST-ADDR-ZIP | X(10) | Alphanumeric | ZIP code | — |

---

## 3. Card Entity

### CVCRD01Y.cpy — Card Master Record (RECLN = 150)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | CARD-RECORD | — | Group | Credit card master record | Record length = 150 bytes |
| 05 | CARD-NUM | X(16) | Alphanumeric | Card number (primary key) | 16-digit card number |
| 05 | CARD-ACCT-ID | 9(11) | Numeric | Owning account ID | Links to ACCT-ID |
| 05 | CARD-CVV-CD | 9(03) | Numeric | Card Verification Value | 3-digit CVV code |
| 05 | CARD-EMBOSSED-NAME | X(50) | Alphanumeric | Name embossed on card | As printed on physical card |
| 05 | CARD-EXPIRAION-DATE | X(10) | Alphanumeric | Card expiration date | Format: YYYY-MM-DD (note: misspelling in source) |
| 05 | CARD-ACTIVE-STATUS | X(01) | Alphanumeric | Card active/inactive status | 'Y' = Active, 'N' = Inactive |
| 05 | FILLER | X(59) | — | Reserved space | Padding to 150 bytes |

---

## 4. Transaction Entity

### CVTRA05Y.cpy — Transaction Record (RECLN = 350)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | TRAN-RECORD | — | Group | Transaction history record | Record length = 350 bytes |
| 05 | TRAN-ID | X(16) | Alphanumeric | Unique transaction identifier | 16-character transaction ID; part of composite key |
| 05 | TRAN-TYPE-CD | X(02) | Alphanumeric | Transaction type code | Links to TRAN-TYPE in CVTRA03Y |
| 05 | TRAN-CAT-CD | 9(04) | Numeric | Transaction category code | Links to TRAN-CAT-CD in CVTRA04Y |
| 05 | TRAN-SOURCE | X(10) | Alphanumeric | Transaction origination source | e.g., 'POS', 'ATM', 'ONLINE', 'PHONE' |
| 05 | TRAN-DESC | X(100) | Alphanumeric | Transaction description | Free-text description |
| 05 | TRAN-AMT | S9(09)V99 | Signed Decimal | Transaction amount | Signed; negative for credits |
| 05 | TRAN-MERCHANT-ID | 9(09) | Numeric | Merchant identifier | 9-digit merchant code |
| 05 | TRAN-MERCHANT-NAME | X(50) | Alphanumeric | Merchant name | — |
| 05 | TRAN-MERCHANT-CITY | X(50) | Alphanumeric | Merchant city | — |
| 05 | TRAN-MERCHANT-ZIP | X(10) | Alphanumeric | Merchant ZIP code | — |
| 05 | TRAN-CARD-NUM | X(16) | Alphanumeric | Card number used | Links to CARD-NUM |
| 05 | TRAN-ORIG-TS | X(26) | Alphanumeric | Transaction origination timestamp | ISO-8601 format |
| 05 | TRAN-PROC-TS | X(26) | Alphanumeric | Transaction processing timestamp | ISO-8601 format |
| 05 | FILLER | X(20) | — | Reserved space | Padding to 350 bytes |

### CVTRA06Y.cpy — Daily Transaction Record (RECLN = 350)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | DALYTRAN-RECORD | — | Group | Daily transaction staging record | Same layout as TRAN-RECORD |
| 05 | DALYTRAN-ID | X(16) | Alphanumeric | Transaction ID | Unique per day |
| 05 | DALYTRAN-TYPE-CD | X(02) | Alphanumeric | Transaction type code | Links to TRAN-TYPE |
| 05 | DALYTRAN-CAT-CD | 9(04) | Numeric | Transaction category code | Links to TRAN-CAT-CD |
| 05 | DALYTRAN-SOURCE | X(10) | Alphanumeric | Origination source | — |
| 05 | DALYTRAN-DESC | X(100) | Alphanumeric | Description | — |
| 05 | DALYTRAN-AMT | S9(09)V99 | Signed Decimal | Amount | — |
| 05 | DALYTRAN-MERCHANT-ID | 9(09) | Numeric | Merchant ID | — |
| 05 | DALYTRAN-MERCHANT-NAME | X(50) | Alphanumeric | Merchant name | — |
| 05 | DALYTRAN-MERCHANT-CITY | X(50) | Alphanumeric | Merchant city | — |
| 05 | DALYTRAN-MERCHANT-ZIP | X(10) | Alphanumeric | Merchant ZIP | — |
| 05 | DALYTRAN-CARD-NUM | X(16) | Alphanumeric | Card number used | — |
| 05 | DALYTRAN-ORIG-TS | X(26) | Alphanumeric | Origination timestamp | — |
| 05 | DALYTRAN-PROC-TS | X(26) | Alphanumeric | Processing timestamp | — |
| 05 | FILLER | X(20) | — | Reserved space | — |

### CVTRA01Y.cpy — Transaction Category Balance Record (RECLN = 50)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | TRAN-CAT-BAL-RECORD | — | Group | Balance by transaction category per account | Record length = 50 bytes |
| 05 | TRAN-CAT-KEY | — | Group (Composite) | Composite primary key | — |
| 10 | TRANCAT-ACCT-ID | 9(11) | Numeric | Account identifier | Links to ACCT-ID |
| 10 | TRANCAT-TYPE-CD | X(02) | Alphanumeric | Transaction type code | Links to TRAN-TYPE |
| 10 | TRANCAT-CD | 9(04) | Numeric | Transaction category code | Links to TRAN-CAT-CD |
| 05 | TRAN-CAT-BAL | S9(09)V99 | Signed Decimal | Category balance amount | Running balance per acct/type/cat |
| 05 | FILLER | X(22) | — | Reserved | Padding to 50 bytes |

### CVTRA02Y.cpy — Disclosure Group Record (RECLN = 50)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | DIS-GROUP-RECORD | — | Group | Disclosure / interest rate group | Record length = 50 bytes |
| 05 | DIS-GROUP-KEY | — | Group (Composite) | Composite key | — |
| 10 | DIS-ACCT-GROUP-ID | X(10) | Alphanumeric | Account group identifier | Links to ACCT-GROUP-ID |
| 10 | DIS-TRAN-TYPE-CD | X(02) | Alphanumeric | Transaction type code | Links to TRAN-TYPE |
| 10 | DIS-TRAN-CAT-CD | 9(04) | Numeric | Transaction category code | Links to TRAN-CAT-CD |
| 05 | DIS-INT-RATE | S9(04)V99 | Signed Decimal | Interest rate for this combination | Annual interest rate percentage |
| 05 | FILLER | X(28) | — | Reserved | Padding to 50 bytes |

### CVTRA03Y.cpy — Transaction Type Record (RECLN = 60)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | TRAN-TYPE-RECORD | — | Group | Transaction type reference | Record length = 60 bytes |
| 05 | TRAN-TYPE | X(02) | Alphanumeric | Transaction type code (key) | 2-character code (e.g., '01', '02') |
| 05 | TRAN-TYPE-DESC | X(50) | Alphanumeric | Type description | e.g., 'Purchase', 'Cash Advance', 'Payment' |
| 05 | FILLER | X(08) | — | Reserved | Padding to 60 bytes |

### CVTRA04Y.cpy — Transaction Category Record (RECLN = 60)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | TRAN-CAT-RECORD | — | Group | Transaction category reference | Record length = 60 bytes |
| 05 | TRAN-CAT-KEY | — | Group (Composite) | Composite key | — |
| 10 | TRAN-TYPE-CD | X(02) | Alphanumeric | Transaction type code | Links to TRAN-TYPE |
| 10 | TRAN-CAT-CD | 9(04) | Numeric | Category code within type | — |
| 05 | TRAN-CAT-TYPE-DESC | X(50) | Alphanumeric | Category description | e.g., 'Retail', 'Grocery', 'Fuel' |
| 05 | FILLER | X(04) | — | Reserved | Padding to 60 bytes |

### CVTRA07Y.cpy — Transaction Report Structures

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | REPORT-NAME-HEADER | — | Group | Report header record | — |
| 05 | REPT-SHORT-NAME | X(38) | Alphanumeric | Short report name | Default: 'DALYREPT' |
| 05 | REPT-LONG-NAME | X(41) | Alphanumeric | Full report title | Default: 'Daily Transaction Report' |
| 05 | REPT-DATE-HEADER | X(12) | Alphanumeric | Date range label | 'Date Range: ' |
| 05 | REPT-START-DATE | X(10) | Alphanumeric | Report start date | YYYY-MM-DD |
| 05 | REPT-END-DATE | X(10) | Alphanumeric | Report end date | YYYY-MM-DD |
| 01 | TRANSACTION-DETAIL-REPORT | — | Group | Detail line for report | — |
| 05 | TRAN-REPORT-TRANS-ID | X(16) | Alphanumeric | Transaction ID | — |
| 05 | TRAN-REPORT-ACCOUNT-ID | X(11) | Alphanumeric | Account ID | — |
| 05 | TRAN-REPORT-TYPE-CD | X(02) | Alphanumeric | Type code | — |
| 05 | TRAN-REPORT-TYPE-DESC | X(15) | Alphanumeric | Type description | — |
| 05 | TRAN-REPORT-CAT-CD | 9(04) | Numeric | Category code | — |
| 05 | TRAN-REPORT-CAT-DESC | X(29) | Alphanumeric | Category description | — |
| 05 | TRAN-REPORT-SOURCE | X(10) | Alphanumeric | Source | — |
| 05 | TRAN-REPORT-AMT | -ZZZ,ZZZ,ZZZ.ZZ | Edited Numeric | Formatted amount | Negative sign leading |
| 01 | REPORT-PAGE-TOTALS | — | Group | Page total line | — |
| 05 | REPT-PAGE-TOTAL | +ZZZ,ZZZ,ZZZ.ZZ | Edited Numeric | Page total amount | — |
| 01 | REPORT-ACCOUNT-TOTALS | — | Group | Account total line | — |
| 05 | REPT-ACCOUNT-TOTAL | +ZZZ,ZZZ,ZZZ.ZZ | Edited Numeric | Account total | — |
| 01 | REPORT-GRAND-TOTALS | — | Group | Grand total line | — |
| 05 | REPT-GRAND-TOTAL | +ZZZ,ZZZ,ZZZ.ZZ | Edited Numeric | Grand total | — |

### COSTM01.CPY — Transaction Record (Statement Layout)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | TRNX-RECORD | — | Group | Transaction record keyed by card+tran ID | Used by CBSTM03A/B for statements |
| 05 | TRNX-KEY | — | Group (Composite) | Composite key | — |
| 10 | TRNX-CARD-NUM | X(16) | Alphanumeric | Card number | Primary sort key |
| 10 | TRNX-ID | X(16) | Alphanumeric | Transaction ID | Secondary sort key |
| 05 | TRNX-REST | — | Group | Remaining transaction data | — |
| 10 | TRNX-TYPE-CD | X(02) | Alphanumeric | Type code | — |
| 10 | TRNX-CAT-CD | 9(04) | Numeric | Category code | — |
| 10 | TRNX-SOURCE | X(10) | Alphanumeric | Source | — |
| 10 | TRNX-DESC | X(100) | Alphanumeric | Description | — |
| 10 | TRNX-AMT | S9(09)V99 | Signed Decimal | Amount | — |
| 10 | TRNX-MERCHANT-ID | 9(09) | Numeric | Merchant ID | — |
| 10 | TRNX-MERCHANT-NAME | X(50) | Alphanumeric | Merchant name | — |
| 10 | TRNX-MERCHANT-CITY | X(50) | Alphanumeric | Merchant city | — |
| 10 | TRNX-MERCHANT-ZIP | X(10) | Alphanumeric | Merchant ZIP | — |
| 10 | TRNX-ORIG-TS | X(26) | Alphanumeric | Origination timestamp | — |
| 10 | TRNX-PROC-TS | X(26) | Alphanumeric | Processing timestamp | — |
| 10 | FILLER | X(20) | — | Reserved | — |

---

## 5. User / Security Entity

### CSUSR01Y.cpy — User Security Record (RECLN = 80)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | SEC-USER-DATA | — | Group | User security / authentication record | Record length = 80 bytes |
| 05 | SEC-USR-ID | X(08) | Alphanumeric | User login ID (primary key) | 8-character user ID |
| 05 | SEC-USR-FNAME | X(20) | Alphanumeric | User first name | — |
| 05 | SEC-USR-LNAME | X(20) | Alphanumeric | User last name | — |
| 05 | SEC-USR-PWD | X(08) | Alphanumeric | User password | 8-character password (plain text in file) |
| 05 | SEC-USR-TYPE | X(01) | Alphanumeric | User type / role | 'A' = Admin, 'U' = Regular User |
| 05 | SEC-USR-FILLER | X(23) | — | Reserved | Padding to 80 bytes |

### UNUSED1Y.cpy — Unused Data Record (RECLN = 80)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | UNUSED-DATA | — | Group | Placeholder / deprecated user record | Same layout as SEC-USER-DATA; appears unused |
| 05 | UNUSED-ID | X(08) | Alphanumeric | User ID | — |
| 05 | UNUSED-FNAME | X(20) | Alphanumeric | First name | — |
| 05 | UNUSED-LNAME | X(20) | Alphanumeric | Last name | — |
| 05 | UNUSED-PWD | X(08) | Alphanumeric | Password | — |
| 05 | UNUSED-TYPE | X(01) | Alphanumeric | User type | — |
| 05 | UNUSED-FILLER | X(23) | — | Reserved | — |

---

## 6. Common / Shared Structures

### COCOM01Y.cpy — Common Communication Area

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | CARDDEMO-COMMAREA | — | Group | CICS COMMAREA passed between programs | Standard inter-program communication |
| 05 | CDEMO-FROM-TRANID | X(04) | Alphanumeric | Originating CICS transaction ID | 4-character CICS TRANID |
| 05 | CDEMO-FROM-PROGRAM | X(08) | Alphanumeric | Originating program name | 8-character program name |
| 05 | CDEMO-TO-TRANID | X(04) | Alphanumeric | Target CICS transaction ID | — |
| 05 | CDEMO-TO-PROGRAM | X(08) | Alphanumeric | Target program name | — |
| 05 | CDEMO-USER-ID | X(08) | Alphanumeric | Current logged-in user ID | Set by COSGN00C |
| 05 | CDEMO-USER-TYPE | X(01) | Alphanumeric | User type (Admin/User) | 'A' or 'U' |
| 05 | CDEMO-PGM-CONTEXT | 9(01) | Numeric | Program context flag | Controls screen flow |
| 05+ | *(additional fields)* | — | — | Various screen-state and navigation fields | Program-specific extensions |

### COTTL01Y.cpy — Screen Title Line

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | CCDA-TITLE01 | X(40) | Alphanumeric | Application title line 1 | Default: 'AWS CardDemo' |
| 01 | CCDA-TITLE02 | X(40) | Alphanumeric | Application title line 2 | Screen-specific subtitle |
| 01 | CCDA-TITLE-DIVIDER | X(40) | Alphanumeric | Divider line | Typically dashes |

### CSDAT01Y.cpy — Date and Time Fields

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | WS-CURDATE-DATA | — | Group | Current date/time working fields | — |
| 05 | WS-CURDATE | X(08) | Alphanumeric | Current date (YYYYMMDD) | From ACCEPT DATE |
| 05 | WS-CURTIME | X(08) | Alphanumeric | Current time (HHMMSSSS) | From ACCEPT TIME |
| 05 | WS-CURDATE-MONTH | 9(02) | Numeric | Month | 01–12 |
| 05 | WS-CURDATE-DAY | 9(02) | Numeric | Day | 01–31 |
| 05 | WS-CURDATE-YEAR | 9(04) | Numeric | Year | 4-digit year |

### CSMSG01Y.cpy — Message Area

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | WS-MESSAGE-DATA | — | Group | Screen message area | — |
| 05 | WS-RETURN-MSG | X(70) | Alphanumeric | Return message to display on screen | Informational or error message |
| 05 | WS-RETURN-MSG-OFF | — | Flag | Message display control | 88-level conditions |

### CSMSG02Y.cpy — Extended Message Area

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | WS-MISC-STORAGE | — | Group | Miscellaneous working storage | — |
| 05 | WS-LONG-MSG | X(500) | Alphanumeric | Extended message buffer | For DB2 error messages and long notifications |
| 05 | WS-TRNID-OFF | — | Flag | Transaction ID display toggle | — |

### COMEN02Y.cpy — Menu Option Definition

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | CARDDEMO-MENU-OPT-COUNT | 9(02) | Numeric | Number of menu options | — |
| 01 | CARDDEMO-MENU-OPT-ARRAY | — | Group | Menu option routing table | Array of program/tran mappings |

### COADM02Y.cpy — Admin Menu Option Definition

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | CARDDEMO-ADMIN-OPT-COUNT | 9(02) | Numeric | Number of admin options | — |
| 01 | CARDDEMO-ADMIN-OPT-ARRAY | — | Group | Admin option routing table | Array of admin program/tran mappings |

### CSSTRPFY.cpy — PFKey Storage Procedure

Common paragraph included via COPY in online programs to capture and store the last PFKey pressed.

### CSSETATY.cpy — Set Attribute Procedure

Common paragraph included via COPY REPLACING to set BMS field attributes (color, intensity) dynamically.

### CSLKPCDY.cpy — Lookup Code Structure

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | WS-LOOKUP-AREA | — | Group | Code lookup working area | For validating codes against reference tables |

### CSUTLDPY.cpy / CSUTLDWY.cpy — Date Utility Parameters

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | CSUTLDTC-DATE | — | Group | Date conversion parameters | Input/output area for CSUTLDTC utility |
| 05 | CSUTLDTC-DATE-IN | X(10) | Alphanumeric | Input date | — |
| 05 | CSUTLDTC-DATE-OUT | 9(08) | Numeric | Output date (Lilian) | — |

### CODATECN.cpy — Date Conversion Area

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | CODATECN-REC | — | Group | Date conversion record | Used by COBDATFT assembler routine |
| 05 | CODATECN-DATE | X(10) | Alphanumeric | Date to convert | — |
| 05 | CODATECN-FORMAT | X(08) | Alphanumeric | Desired output format | — |
| 05 | CODATECN-RESULT | X(20) | Alphanumeric | Formatted result | — |

### CVEXPORT.cpy — Export/Import Control Record

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | EXPORT-RECORD | — | Group | Export file control record | Identifies record type in flat file |
| 05 | EXPORT-REC-TYPE | X(01) | Alphanumeric | Record type indicator | 'A'=Account, 'C'=Customer, 'R'=Card, 'X'=Xref, 'U'=User, 'T'=Transaction |
| 05 | EXPORT-REC-DATA | X(500) | Alphanumeric | Record data payload | Varies by type |

---

## 7. Authorization Sub-Application Structures

### `app/app-authorization-ims-db2-mq/cpy/`

### CIPAUSMY.cpy — Pending Authorization Summary (IMS Root Segment)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | PENDING-AUTH-SUMMARY | — | Group | IMS root segment for pending authorizations | — |
| 05 | PA-ACCT-ID | 9(11) | Numeric | Account ID | IMS segment key |
| 05 | PA-CARD-NUM | X(16) | Alphanumeric | Card number | — |
| 05 | PA-TOTAL-PENDING-AMT | S9(09)V99 | Signed Decimal | Total pending authorization amount | Sum of all pending auths |
| 05 | PA-PENDING-COUNT | 9(05) | Numeric | Number of pending authorizations | Count of child segments |

### CIPAUDTY.cpy — Pending Authorization Detail (IMS Child Segment)

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | PENDING-AUTH-DETAIL | — | Group | IMS child segment for individual authorization | — |
| 05 | PAD-AUTH-ID | X(16) | Alphanumeric | Authorization ID | Unique per authorization |
| 05 | PAD-AUTH-AMT | S9(09)V99 | Signed Decimal | Authorization amount | — |
| 05 | PAD-AUTH-DATE | X(10) | Alphanumeric | Authorization date | YYYY-MM-DD |
| 05 | PAD-MERCHANT-ID | 9(09) | Numeric | Merchant ID | — |
| 05 | PAD-MERCHANT-NAME | X(50) | Alphanumeric | Merchant name | — |
| 05 | PAD-AUTH-STATUS | X(01) | Alphanumeric | Authorization status | 'P'=Pending, 'A'=Approved, 'R'=Rejected |

### CCPAURQY.cpy — Authorization MQ Request Message

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | AUTH-REQUEST | — | Group | MQ message: authorization request | — |
| 05 | ARQ-CARD-NUM | X(16) | Alphanumeric | Card number | — |
| 05 | ARQ-AUTH-AMT | S9(09)V99 | Signed Decimal | Requested auth amount | — |
| 05 | ARQ-MERCHANT-ID | 9(09) | Numeric | Merchant ID | — |
| 05 | ARQ-MERCHANT-NAME | X(50) | Alphanumeric | Merchant name | — |

### CCPAURLY.cpy — Authorization MQ Reply Message

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | AUTH-REPLY | — | Group | MQ message: authorization response | — |
| 05 | ARP-RESPONSE-CD | X(02) | Alphanumeric | Response code | '00'=Approved, '05'=Declined |
| 05 | ARP-AUTH-ID | X(16) | Alphanumeric | Assigned authorization ID | Set on approval |
| 05 | ARP-REASON-CD | X(04) | Alphanumeric | Decline reason code | Set on decline |

### CCPAUERY.cpy — Authorization MQ Error Message

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | AUTH-ERROR | — | Group | MQ message: authorization error | — |
| 05 | AER-ERROR-CD | X(04) | Alphanumeric | Error code | System error code |
| 05 | AER-ERROR-MSG | X(100) | Alphanumeric | Error description | — |

### IMSFUNCS.cpy — IMS DL/I Function Codes

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | IMS-FUNCTIONS | — | Group | IMS DL/I function code constants | — |
| 05 | FUNC-GU | X(04) | Alphanumeric | Get Unique | VALUE 'GU  ' |
| 05 | FUNC-GN | X(04) | Alphanumeric | Get Next | VALUE 'GN  ' |
| 05 | FUNC-GNP | X(04) | Alphanumeric | Get Next within Parent | VALUE 'GNP ' |
| 05 | FUNC-ISRT | X(04) | Alphanumeric | Insert | VALUE 'ISRT' |
| 05 | FUNC-REPL | X(04) | Alphanumeric | Replace | VALUE 'REPL' |
| 05 | FUNC-DLET | X(04) | Alphanumeric | Delete | VALUE 'DLET' |

### PCB Copybooks (PAUTBPCB.CPY, PASFLPCB.CPY, PADFLPCB.CPY)

These define IMS Program Communication Block (PCB) masks:

| Copybook | PCB Name | Key Fields | Purpose |
|----------|----------|------------|---------|
| PAUTBPCB.CPY | PAUTBPCB | PAUT-DBDNAME, PAUT-PCB-STATUS, PAUT-SEG-NAME, PAUT-KEYFB | Primary PCB for pending auth IMS DB |
| PASFLPCB.CPY | PASFLPCB | PASFL-DBDNAME, PASFL-PCB-STATUS, PASFL-SEG-NAME | Secondary field PCB |
| PADFLPCB.CPY | PADFLPCB | PADFL-DBDNAME, PADFL-PCB-STATUS, PADFL-SEG-NAME | Detail field PCB |

---

## 8. DB2 Transaction Type Sub-Application Structures

### `app/app-transaction-type-db2/cpy/`

### CSDB2RWY.cpy — DB2 Common Working Storage

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 05 | WS-DB2-COMMON-VARS | — | Group | DB2 working storage variables | — |
| 10 | WS-DISP-SQLCODE | ----9 | Edited Numeric | Displayable SQLCODE | For error messages |
| 10 | WS-DUMMY-DB2-INT | S9(4) COMP-3 | Packed Decimal | Dummy variable for priming query | — |
| 10 | WS-DB2-PROCESSING-FLAG | X(1) | Alphanumeric | DB2 processing status | '0'=OK, '1'=Error |
| 10 | WS-DB2-CURRENT-ACTION | X(72) | Alphanumeric | Current DB2 action description | For error context |
| 05 | WS-DSNTIAC-FORMATTED | — | Group | DSNTIAC formatted message area | — |
| 10 | WS-DSNTIAC-MESG-LEN | S9(4) COMP | Binary | Message length | VALUE +720 |
| 10 | WS-DSNTIAC-FMTD-TEXT | — | Group | Formatted text lines | 10 × 72-char lines |

### CSDB2RPY.cpy — DB2 Common Procedures

Contains inline PROCEDURE DIVISION paragraphs for:
- **9998-PRIMING-QUERY** — Dummy `SELECT 1 FROM SYSIBM.SYSDUMMY1` to verify DB2 connectivity
- **9999-FORMAT-DB2-MESSAGE** — Calls DSNTIAC to format SQLCA error messages for display

### DB2 DCL Copybooks (in `app/app-transaction-type-db2/dcl/`)

| DCL File | DB2 Table | Key Columns | Purpose |
|----------|-----------|-------------|---------|
| DCLTRTYP.dcl | CARDDEMO.TRANSACTION_TYPE | TR_TYPE (CHAR(2)), TR_DESCRIPTION (VARCHAR(50)) | Transaction type reference table |
| DCLTRCAT.dcl | CARDDEMO.TRANSACTION_TYPE_CATEGORY | TRC_TYPE_CODE, TRC_TYPE_CATEGORY, TRC_CAT_DATA | Transaction category sub-classification |

---

## VSAM File Summary

| Dataset Name | Key | Record Length | Entity |
|-------------|-----|--------------|--------|
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS | ACCT-ID (9(11)) | 300 | Account |
| AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS | CUST-ID (9(09)) | 500 | Customer |
| AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS | CARD-NUM (X(16)) | 150 | Card |
| AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS | XREF-CARD-NUM (X(16)) | 50 | Card Cross-Reference |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | TRAN-ID (X(16)) | 350 | Transaction |
| AWS.M2.CARDDEMO.DALYTRAN.PS | DALYTRAN-ID (X(16)) | 350 | Daily Transaction |
| AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS | TRAN-CAT-KEY (17 bytes) | 50 | Category Balance |
| AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS | DIS-GROUP-KEY (16 bytes) | 50 | Disclosure Group |
| AWS.M2.CARDDEMO.TRANTYPE.PS | TRAN-TYPE (X(02)) | 60 | Transaction Type |
| AWS.M2.CARDDEMO.TRANCATG.PS | TRAN-CAT-KEY (6 bytes) | 60 | Transaction Category |
| AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS | SEC-USR-ID (X(08)) | 80 | User Security |
