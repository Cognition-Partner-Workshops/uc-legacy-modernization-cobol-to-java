# DATA DICTIONARY - CardDemo COBOL Estate

> **Generated:** 2026-03-25 | **Source:** `app/cpy/` (30 copybooks)

---

## 1. Account Entity

### 1.1 CVACT01Y.cpy - Account Master Record (RECLN 300)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|-----------------|-----------------|
| `ACCT-ID` | PIC 9(11) | Numeric | Unique account identifier (11-digit) | Primary key; must be numeric |
| `ACCT-ACTIVE-STATUS` | PIC X(01) | Alpha | Account active/inactive status flag | 'Y' = active, 'N' = inactive |
| `ACCT-CURR-BAL` | PIC S9(10)V99 | Signed decimal | Current account balance | Signed; allows negative (overdrawn) |
| `ACCT-CREDIT-LIMIT` | PIC S9(10)V99 | Signed decimal | Maximum credit limit for the account | Must be positive |
| `ACCT-CASH-CREDIT-LIMIT` | PIC S9(10)V99 | Signed decimal | Cash advance credit limit | Must be <= ACCT-CREDIT-LIMIT |
| `ACCT-OPEN-DATE` | PIC X(10) | Date string | Date the account was opened | Format: YYYY-MM-DD |
| `ACCT-EXPIRAION-DATE` | PIC X(10) | Date string | Account expiration date | Format: YYYY-MM-DD; must be > ACCT-OPEN-DATE |
| `ACCT-REISSUE-DATE` | PIC X(10) | Date string | Date the account/card was last reissued | Format: YYYY-MM-DD |
| `ACCT-CURR-CYC-CREDIT` | PIC S9(10)V99 | Signed decimal | Total credits in current billing cycle | Accumulated during cycle; reset at statement |
| `ACCT-CURR-CYC-DEBIT` | PIC S9(10)V99 | Signed decimal | Total debits in current billing cycle | Accumulated during cycle; reset at statement |
| `ACCT-ADDR-ZIP` | PIC X(10) | Alpha | Account holder ZIP/postal code | Validated against US ZIP code tables |
| `ACCT-GROUP-ID` | PIC X(10) | Alpha | Account group identifier for discount/interest grouping | Links to DIS-GROUP-RECORD |
| `FILLER` | PIC X(178) | Alpha | Reserved space for future fields | Unused |

**Record Length:** 300 bytes | **VSAM Key:** `ACCT-ID` | **Dataset:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`

---

### 1.2 CVACT03Y.cpy - Card Cross-Reference Record (RECLN 50)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|-----------------|-----------------|
| `XREF-CARD-NUM` | PIC X(16) | Alpha | Card number (primary key for cross-reference) | 16-digit card number; primary key |
| `XREF-CUST-ID` | PIC 9(09) | Numeric | Customer ID owning this card | Must exist in CUSTOMER-RECORD |
| `XREF-ACCT-ID` | PIC 9(11) | Numeric | Account ID linked to this card | Must exist in ACCOUNT-RECORD; alternate key |
| `FILLER` | PIC X(14) | Alpha | Reserved | Unused |

**Record Length:** 50 bytes | **VSAM Key:** `XREF-CARD-NUM` | **Alternate Key:** `XREF-ACCT-ID` | **Dataset:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS`

---

## 2. Card Entity

### 2.1 CVACT02Y.cpy - Card Master Record (RECLN 150)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|-----------------|-----------------|
| `CARD-NUM` | PIC X(16) | Alpha | Credit card number (primary key) | 16-digit card number; unique |
| `CARD-ACCT-ID` | PIC 9(11) | Numeric | Account ID this card belongs to | Must exist in ACCOUNT-RECORD |
| `CARD-CVV-CD` | PIC 9(03) | Numeric | Card verification value (CVV) code | 3-digit security code |
| `CARD-EMBOSSED-NAME` | PIC X(50) | Alpha | Name embossed on the physical card | Cardholder's name |
| `CARD-EXPIRAION-DATE` | PIC X(10) | Date string | Card expiration date | Format: YYYY-MM-DD |
| `CARD-ACTIVE-STATUS` | PIC X(01) | Alpha | Card active/inactive status | 'Y' = active, 'N' = inactive |
| `FILLER` | PIC X(59) | Alpha | Reserved | Unused |

**Record Length:** 150 bytes | **VSAM Key:** `CARD-NUM` | **Dataset:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`

---

### 2.2 CVCRD01Y.cpy - Card Work Areas (Online Programs)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|-----------------|-----------------|
| `CCARD-AID` | PIC X(5) | Alpha | Attention identifier from terminal input | 88-level values: ENTER, CLEAR, PA1, PA2, PFK01-PFK12 |
| `CCARD-NEXT-PROG` | PIC X(8) | Alpha | Next program to transfer control to | Valid program name |
| `CCARD-NEXT-MAPSET` | PIC X(7) | Alpha | Next BMS mapset to display | Valid mapset name |
| `CCARD-NEXT-MAP` | PIC X(7) | Alpha | Next BMS map within mapset | Valid map name |
| `CCARD-ERROR-MSG` | PIC X(75) | Alpha | Error message for screen display | Free text |
| `CCARD-RETURN-MSG` | PIC X(75) | Alpha | Return/status message for screen | Free text; 88-level OFF = LOW-VALUES |
| `CC-ACCT-ID` / `CC-ACCT-ID-N` | PIC X(11) / PIC 9(11) | Alpha/Numeric | Account ID in work area (with numeric redefine) | Must be valid account |
| `CC-CARD-NUM` / `CC-CARD-NUM-N` | PIC X(16) / PIC 9(16) | Alpha/Numeric | Card number in work area (with numeric redefine) | Must be valid card number |
| `CC-CUST-ID` / `CC-CUST-ID-N` | PIC X(09) / PIC 9(9) | Alpha/Numeric | Customer ID in work area (with numeric redefine) | Must be valid customer |

---

## 3. Customer Entity

### 3.1 CVCUS01Y.cpy - Customer Master Record (RECLN 500)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|-----------------|-----------------|
| `CUST-ID` | PIC 9(09) | Numeric | Unique customer identifier | Primary key; 9-digit numeric |
| `CUST-FIRST-NAME` | PIC X(25) | Alpha | Customer's first name | Required; non-blank |
| `CUST-MIDDLE-NAME` | PIC X(25) | Alpha | Customer's middle name | Optional |
| `CUST-LAST-NAME` | PIC X(25) | Alpha | Customer's last name | Required; non-blank |
| `CUST-ADDR-LINE-1` | PIC X(50) | Alpha | Address line 1 (street address) | Required |
| `CUST-ADDR-LINE-2` | PIC X(50) | Alpha | Address line 2 (apt/suite) | Optional |
| `CUST-ADDR-LINE-3` | PIC X(50) | Alpha | Address line 3 (city) | Optional |
| `CUST-ADDR-STATE-CD` | PIC X(02) | Alpha | US state code | Validated against CSSETATY valid state list |
| `CUST-ADDR-COUNTRY-CD` | PIC X(03) | Alpha | Country code | 3-character ISO code |
| `CUST-ADDR-ZIP` | PIC X(10) | Alpha | ZIP/postal code | Validated against state-ZIP combinations in CSSETATY |
| `CUST-PHONE-NUM-1` | PIC X(15) | Alpha | Primary phone number | Area code validated against CSLKPCDY valid codes |
| `CUST-PHONE-NUM-2` | PIC X(15) | Alpha | Secondary phone number | Area code validated against CSLKPCDY valid codes |
| `CUST-SSN` | PIC 9(09) | Numeric | Social Security Number | 9-digit; sensitive PII |
| `CUST-GOVT-ISSUED-ID` | PIC X(20) | Alpha | Government-issued identification number | Driver's license, passport, etc. |
| `CUST-DOB-YYYY-MM-DD` | PIC X(10) | Date string | Date of birth | Format: YYYY-MM-DD; cannot be in future (validated in CSUTLDPY) |
| `CUST-EFT-ACCOUNT-ID` | PIC X(10) | Alpha | Electronic Funds Transfer account ID | For bill payment EFT |
| `CUST-PRI-CARD-HOLDER-IND` | PIC X(01) | Alpha | Primary cardholder indicator | 'Y' = primary, 'N' = authorized user |
| `CUST-FICO-CREDIT-SCORE` | PIC 9(03) | Numeric | FICO credit score | Range: 300-850 |
| `FILLER` | PIC X(168) | Alpha | Reserved | Unused |

**Record Length:** 500 bytes | **VSAM Key:** `CUST-ID` | **Dataset:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`

---

### 3.2 CUSTREC.cpy - Customer Record (Alternate Layout for Statement Processing)

Identical structure to CVCUS01Y with minor field name differences (e.g., `CUST-DOB-YYYYMMDD` instead of `CUST-DOB-YYYY-MM-DD`). Used by CBSTM03A for statement generation.

---

## 4. Transaction Entity

### 4.1 CVTRA05Y.cpy - Transaction Master Record (RECLN 350)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|-----------------|-----------------|
| `TRAN-ID` | PIC X(16) | Alpha | Unique transaction identifier | Primary key; system-generated |
| `TRAN-TYPE-CD` | PIC X(02) | Alpha | Transaction type code (e.g., SA=Sale, CR=Credit) | Must exist in TRAN-TYPE-RECORD |
| `TRAN-CAT-CD` | PIC 9(04) | Numeric | Transaction category code | Must exist in TRAN-CAT-RECORD |
| `TRAN-SOURCE` | PIC X(10) | Alpha | Source of the transaction | e.g., "POS", "ONLINE", "ATM" |
| `TRAN-DESC` | PIC X(100) | Alpha | Transaction description | Free text description |
| `TRAN-AMT` | PIC S9(09)V99 | Signed decimal | Transaction amount | Signed; negative for credits/refunds |
| `TRAN-MERCHANT-ID` | PIC 9(09) | Numeric | Merchant identifier | Numeric merchant code |
| `TRAN-MERCHANT-NAME` | PIC X(50) | Alpha | Merchant business name | Free text |
| `TRAN-MERCHANT-CITY` | PIC X(50) | Alpha | Merchant city | Free text |
| `TRAN-MERCHANT-ZIP` | PIC X(10) | Alpha | Merchant ZIP code | |
| `TRAN-CARD-NUM` | PIC X(16) | Alpha | Card number used for transaction | Must exist in CARD-RECORD |
| `TRAN-ORIG-TS` | PIC X(26) | Timestamp | Original transaction timestamp | DB2-format: YYYY-MM-DD-HH.MM.SS.NNNNNN |
| `TRAN-PROC-TS` | PIC X(26) | Timestamp | Processing/posting timestamp | DB2-format; alternate key for VSAM |
| `FILLER` | PIC X(20) | Alpha | Reserved | Unused |

**Record Length:** 350 bytes | **VSAM Key:** `TRAN-ID` | **Alternate Key:** `TRAN-PROC-TS` | **Dataset:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`

---

### 4.2 CVTRA06Y.cpy - Daily Transaction Record (RECLN 350)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|-----------------|-----------------|
| `DALYTRAN-ID` | PIC X(16) | Alpha | Daily transaction identifier | Matches TRAN-ID format |
| `DALYTRAN-TYPE-CD` | PIC X(02) | Alpha | Transaction type code | Must exist in TRANTYPE reference |
| `DALYTRAN-CAT-CD` | PIC 9(04) | Numeric | Transaction category code | Must exist in TRANCATG reference |
| `DALYTRAN-SOURCE` | PIC X(10) | Alpha | Transaction source | |
| `DALYTRAN-DESC` | PIC X(100) | Alpha | Transaction description | |
| `DALYTRAN-AMT` | PIC S9(09)V99 | Signed decimal | Transaction amount | |
| `DALYTRAN-MERCHANT-ID` | PIC 9(09) | Numeric | Merchant ID | |
| `DALYTRAN-MERCHANT-NAME` | PIC X(50) | Alpha | Merchant name | |
| `DALYTRAN-MERCHANT-CITY` | PIC X(50) | Alpha | Merchant city | |
| `DALYTRAN-MERCHANT-ZIP` | PIC X(10) | Alpha | Merchant ZIP | |
| `DALYTRAN-CARD-NUM` | PIC X(16) | Alpha | Card number | |
| `DALYTRAN-ORIG-TS` | PIC X(26) | Timestamp | Original timestamp | |
| `DALYTRAN-PROC-TS` | PIC X(26) | Timestamp | Processing timestamp | |
| `FILLER` | PIC X(20) | Alpha | Reserved | |

**Purpose:** Staging file for daily incoming transactions before posting to master. Same layout as CVTRA05Y with `DALYTRAN-` prefix.

---

### 4.3 CVTRA01Y.cpy - Transaction Category Balance Record

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|-----------------|-----------------|
| `TRAN-CAT-KEY` (composite) | | | Composite key | |
| &nbsp;&nbsp;`TRANCAT-ACCT-ID` | PIC 9(11) | Numeric | Account ID | Must exist in ACCOUNT-RECORD |
| &nbsp;&nbsp;`TRANCAT-TYPE-CD` | PIC X(02) | Alpha | Transaction type code | |
| &nbsp;&nbsp;`TRANCAT-CD` | PIC 9(04) | Numeric | Transaction category code | |
| `TRAN-CAT-BAL` | PIC S9(09)V99 | Signed decimal | Running balance for this account/type/category | Used for interest calculation |
| `FILLER` | PIC X(22) | Alpha | Reserved | |

**VSAM Key:** `TRAN-CAT-KEY` (ACCT-ID + TYPE-CD + CAT-CD) | **Dataset:** `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS`

---

### 4.4 CVTRA02Y.cpy - Discount Group Record

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|-----------------|-----------------|
| `DIS-GROUP-KEY` (composite) | | | Composite key | |
| &nbsp;&nbsp;`DIS-ACCT-GROUP-ID` | PIC X(10) | Alpha | Account group identifier | Links to ACCT-GROUP-ID |
| &nbsp;&nbsp;`DIS-TRAN-TYPE-CD` | PIC X(02) | Alpha | Transaction type | |
| &nbsp;&nbsp;`DIS-TRAN-CAT-CD` | PIC 9(04) | Numeric | Transaction category | |
| `DIS-INT-RATE` | PIC S9(04)V99 | Signed decimal | Interest rate for this group/type/category | Percentage; used in CBACT04C interest calculation |
| `FILLER` | PIC X(28) | Alpha | Reserved | |

**VSAM Key:** `DIS-GROUP-KEY` | **Dataset:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`

---

### 4.5 CVTRA03Y.cpy - Transaction Type Reference

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|-----------------|-----------------|
| `TRAN-TYPE` | PIC X(02) | Alpha | Transaction type code (primary key) | e.g., "SA" (sale), "CR" (credit) |
| `TRAN-TYPE-DESC` | PIC X(50) | Alpha | Description of the transaction type | Free text |
| `FILLER` | PIC X(08) | Alpha | Reserved | |

**VSAM Key:** `TRAN-TYPE` | **Dataset:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`

---

### 4.6 CVTRA04Y.cpy - Transaction Category Reference

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|-----------------|-----------------|
| `TRAN-CAT-KEY` (composite) | | | Composite key | |
| &nbsp;&nbsp;`TRAN-TYPE-CD` | PIC X(02) | Alpha | Transaction type code | |
| &nbsp;&nbsp;`TRAN-CAT-CD` | PIC 9(04) | Numeric | Category code within type | |
| `TRAN-CAT-TYPE-DESC` | PIC X(50) | Alpha | Description of the category | Free text |
| `FILLER` | PIC X(04) | Alpha | Reserved | |

**VSAM Key:** `TRAN-TYPE-CD + TRAN-CAT-CD` | **Dataset:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`

---

### 4.7 CVTRA07Y.cpy - Transaction Report Layouts

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|-----------------|-----------------|
| **REPORT-NAME-HEADER** | | | Report header block | |
| `REPT-SHORT-NAME` | PIC X(38) | Alpha | Short report title | |
| `REPT-LONG-NAME` | PIC X(41) | Alpha | Full report title | |
| `REPT-DATE-HEADER` | PIC X(12) | Alpha | "Date Range:" label | |
| `REPT-START-DATE` | PIC X(10) | Date string | Report start date | |
| `REPT-END-DATE` | PIC X(10) | Date string | Report end date | |
| **TRANSACTION-DETAIL-REPORT** | | | Report detail line | |
| `TRAN-REPORT-TRANS-ID` | PIC X(16) | Alpha | Transaction ID column | |
| `TRAN-REPORT-ACCOUNT-ID` | PIC X(11) | Alpha | Account ID column | |
| `TRAN-REPORT-TYPE-CD` | PIC X(02) | Alpha | Type code column | |
| `TRAN-REPORT-TYPE-DESC` | PIC X(15) | Alpha | Type description column | |
| `TRAN-REPORT-CAT-CD` | PIC 9(04) | Numeric | Category code column | |
| `TRAN-REPORT-CAT-DESC` | PIC X(29) | Alpha | Category description column | |
| `TRAN-REPORT-SOURCE` | PIC X(10) | Alpha | Source column | |
| `TRAN-REPORT-AMT` | PIC -ZZZ,ZZZ,ZZZ.ZZ | Edited numeric | Amount column (formatted) | |
| **REPORT-PAGE-TOTALS** | | | Page total line | |
| `REPT-PAGE-TOTAL` | PIC +ZZZ,ZZZ,ZZZ.ZZ | Edited numeric | Running page total | |
| **REPORT-ACCOUNT-TOTALS** | | | Account total line | |
| `REPT-ACCOUNT-TOTAL` | PIC +ZZZ,ZZZ,ZZZ.ZZ | Edited numeric | Account subtotal | |
| **REPORT-GRAND-TOTALS** | | | Grand total line | |
| `REPT-GRAND-TOTAL` | PIC +ZZZ,ZZZ,ZZZ.ZZ | Edited numeric | Grand total of all transactions | |

---

### 4.8 COSTM01.CPY - Statement Transaction Record (for CBSTM03A/B)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|-----------------|-----------------|
| `TRNX-KEY` (composite) | | | Composite key for sorted transaction | |
| &nbsp;&nbsp;`TRNX-CARD-NUM` | PIC X(16) | Alpha | Card number | |
| &nbsp;&nbsp;`TRNX-ID` | PIC X(16) | Alpha | Transaction ID | |
| `TRNX-TYPE-CD` | PIC X(02) | Alpha | Transaction type code | |
| `TRNX-CAT-CD` | PIC 9(04) | Numeric | Transaction category code | |
| `TRNX-SOURCE` | PIC X(10) | Alpha | Transaction source | |
| `TRNX-DESC` | PIC X(100) | Alpha | Description | |
| `TRNX-AMT` | PIC S9(09)V99 | Signed decimal | Amount | |
| `TRNX-MERCHANT-ID` | PIC 9(09) | Numeric | Merchant ID | |
| `TRNX-MERCHANT-NAME` | PIC X(50) | Alpha | Merchant name | |
| `TRNX-MERCHANT-CITY` | PIC X(50) | Alpha | Merchant city | |
| `TRNX-MERCHANT-ZIP` | PIC X(10) | Alpha | Merchant ZIP | |
| `TRNX-ORIG-TS` | PIC X(26) | Timestamp | Original timestamp | |
| `TRNX-PROC-TS` | PIC X(26) | Timestamp | Processing timestamp | |
| `FILLER` | PIC X(20) | Alpha | Reserved | |

**VSAM Key:** `TRNX-CARD-NUM + TRNX-ID` | **Dataset:** `AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS`

---

## 5. Security / User Entity

### 5.1 CSUSR01Y.cpy - User Security Record (RECLN 80)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|-----------------|-----------------|
| `SEC-USR-ID` | PIC X(08) | Alpha | User login ID (primary key) | Unique; e.g., "USER0001", "ADMIN001" |
| `SEC-USR-FNAME` | PIC X(20) | Alpha | User first name | |
| `SEC-USR-LNAME` | PIC X(20) | Alpha | User last name | |
| `SEC-USR-PWD` | PIC X(08) | Alpha | User password | Plain text; 8 characters max |
| `SEC-USR-TYPE` | PIC X(01) | Alpha | User type | 'A' = Admin, 'U' = Regular user |
| `SEC-USR-FILLER` | PIC X(23) | Alpha | Reserved | |

**Record Length:** 80 bytes | **VSAM Key:** `SEC-USR-ID` | **Dataset:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`

---

## 6. Application Control / Communication Structures

### 6.1 COCOM01Y.cpy - CICS Communication Area (COMMAREA)

| Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|------------|-----------|-----------|-----------------|-----------------|
| **CDEMO-GENERAL-INFO** | | | General navigation context | |
| `CDEMO-FROM-TRANID` | PIC X(04) | Alpha | Transaction ID that invoked current program | |
| `CDEMO-FROM-PROGRAM` | PIC X(08) | Alpha | Program that transferred control | |
| `CDEMO-TO-TRANID` | PIC X(04) | Alpha | Target transaction ID | |
| `CDEMO-TO-PROGRAM` | PIC X(08) | Alpha | Target program to transfer to | |
| `CDEMO-USER-ID` | PIC X(08) | Alpha | Currently authenticated user ID | |
| `CDEMO-USER-TYPE` | PIC X(01) | Alpha | User type | 88: 'A' = ADMIN, 'U' = USER |
| `CDEMO-PGM-CONTEXT` | PIC 9(01) | Numeric | Program context flag | 88: 0 = ENTER (first entry), 1 = REENTER (return) |
| **CDEMO-CUSTOMER-INFO** | | | Customer context passed between screens | |
| `CDEMO-CUST-ID` | PIC 9(09) | Numeric | Customer ID in context | |
| `CDEMO-CUST-FNAME` | PIC X(25) | Alpha | Customer first name | |
| `CDEMO-CUST-MNAME` | PIC X(25) | Alpha | Customer middle name | |
| `CDEMO-CUST-LNAME` | PIC X(25) | Alpha | Customer last name | |
| **CDEMO-ACCOUNT-INFO** | | | Account context | |
| `CDEMO-ACCT-ID` | PIC 9(11) | Numeric | Account ID in context | |
| `CDEMO-ACCT-STATUS` | PIC X(01) | Alpha | Account status | |
| **CDEMO-CARD-INFO** | | | Card context | |
| `CDEMO-CARD-NUM` | PIC 9(16) | Numeric | Card number in context | |
| **CDEMO-MORE-INFO** | | | Map navigation state | |
| `CDEMO-LAST-MAP` | PIC X(7) | Alpha | Last BMS map displayed | |
| `CDEMO-LAST-MAPSET` | PIC X(7) | Alpha | Last BMS mapset used | |

---

### 6.2 COADM02Y.cpy - Admin Menu Options

Contains menu option definitions for the admin user interface with option numbers mapped to program names and transaction IDs.

### 6.3 COMEN02Y.cpy - Regular User Menu Options

Contains menu option definitions for the regular user interface, mapping function numbers to their corresponding CICS programs and transaction IDs.

---

## 7. Utility / Shared Structures

### 7.1 COTTL01Y.cpy - Title/Header Line

Screen title and header fields used by all online CICS programs for consistent UI display.

### 7.2 CSDAT01Y.cpy - Date Display Fields

Date/time fields for screen header display (current date, time).

### 7.3 CSMSG01Y.cpy - Message Area (Short)

Standard message area for displaying info/error messages on BMS screens (single line).

### 7.4 CSMSG02Y.cpy - Message Area (Extended)

Extended message area supporting multi-line messages and thank-you/confirmation messages.

### 7.5 CODATECN.cpy - Date Conversion Record

| Field Name | PIC Clause | Data Type | Business Meaning |
|------------|-----------|-----------|-----------------|
| `CODATECN-INP-DATE` | PIC X(10) | Date string | Input date to convert |
| `CODATECN-TYPE` | PIC X(01) | Alpha | Input date format type |
| `CODATECN-OUTTYPE` | PIC X(01) | Alpha | Desired output format type |
| `CODATECN-0UT-DATE` | PIC X(10) | Date string | Converted output date |

Used with COBDATFT assembler program for date format conversion.

### 7.6 CSSETATY.cpy - Set Attribute Utility

Contains screen attribute control bytes (BMS attributes) for setting field colors and protection states.

### 7.7 CSSTRPFY.cpy - String Strip Function

Inline COBOL paragraphs (not a data structure) for stripping leading/trailing spaces from strings. Used via COPY statement in online programs.

### 7.8 CSLKPCDY.cpy - Lookup Code Validation Tables

| Field Name | PIC Clause | Data Type | Business Meaning |
|------------|-----------|-----------|-----------------|
| `PHONE-AREA-CODE-TO-EDIT` | PIC X(3) | Alpha | Phone area code to validate |
| `US-STATE-CODE-TO-EDIT` | PIC X(2) | Alpha | US state code to validate |
| `US-STATE-ZIPCODE-TO-EDIT` | PIC X(5) | Alpha | State+ZIP prefix to validate |

Contains extensive 88-level validation tables for:
- **Valid US phone area codes** (full NPA list)
- **Valid US state codes** (50 states + territories)
- **Valid state-ZIP code combinations** (first 2 digits of ZIP by state)

### 7.9 CSUTLDPY.cpy - Date Validation Working Storage

Inline COBOL paragraphs for comprehensive date validation including:
- Year/month/day range validation
- Leap year calculation
- Date-of-birth future-date check
- LE (Language Environment) date validation via CSUTLDTC

### 7.10 CSUTLDWY.cpy - Date Validation Working Storage Variables

Working storage fields used by CSUTLDPY, including:
- Date component fields (CCYY, MM, DD) with 88-level validators
- Flags for date validity (year OK, month OK, day OK)
- LE service result areas

---

## 8. Export/Import Structure

### 8.1 CVEXPORT.cpy - Multi-Record Export Layout (RECLN 500)

| Field Name | PIC Clause | Data Type | Business Meaning |
|------------|-----------|-----------|-----------------|
| **EXPORT-RECORD (common header)** | | | |
| `EXPORT-REC-TYPE` | PIC X(01) | Alpha | Record type: C=Customer, A=Account, T=Transaction, X=Xref, D=Card |
| `EXPORT-TIMESTAMP` | PIC X(26) | Timestamp | Export timestamp |
| `EXPORT-SEQUENCE-NUM` | PIC 9(09) | Numeric | Sequence number (primary key) |
| `EXPORT-BRANCH-ID` | PIC X(04) | Alpha | Branch identifier |
| `EXPORT-REGION-CODE` | PIC X(10) | Alpha | Region code |
| **EXPORT-CUSTOMER-DATA** (REDEFINES) | | | Customer fields: ID, name, address, SSN, DOB, FICO score, etc. |
| **EXPORT-ACCOUNT-DATA** (REDEFINES) | | | Account fields: ID, status, balances, dates, group ID |
| **EXPORT-TRANSACTION-DATA** (REDEFINES) | | | Transaction fields: ID, type, amount, merchant info, timestamps |
| **EXPORT-CARD-XREF-DATA** (REDEFINES) | | | Cross-reference: card number, customer ID, account ID |
| **EXPORT-CARD-DATA** (REDEFINES) | | | Card fields: number, account ID, CVV, embossed name, expiration |

**Record Length:** 500 bytes | Uses REDEFINES for polymorphic record types.

---

## 9. Unused / Placeholder

### 9.1 UNUSED1Y.cpy

| Field Name | PIC Clause | Data Type | Business Meaning |
|------------|-----------|-----------|-----------------|
| `UNUSED-ID` | PIC X(08) | Alpha | Placeholder ID |
| `UNUSED-FNAME` | PIC X(20) | Alpha | Placeholder first name |
| `UNUSED-LNAME` | PIC X(20) | Alpha | Placeholder last name |
| `UNUSED-PWD` | PIC X(08) | Alpha | Placeholder password |
| `UNUSED-TYPE` | PIC X(01) | Alpha | Placeholder type |
| `UNUSED-FILLER` | PIC X(23) | Alpha | Filler |

Appears to be a deprecated copy of the user security structure. Not referenced by any active programs.

---

## 10. Copybook Summary

| # | Copybook | Business Entity | Record Length | Key Fields | Used By (Programs) |
|---|----------|----------------|--------------|------------|---------------------|
| 1 | CVACT01Y | Account | 300 | ACCT-ID | CBACT01C, CBACT04C, COACTVWC, COACTUPC, COTRN02C, COBIL00C, CBEXPORT, CBIMPORT, CBSTM03A |
| 2 | CVACT02Y | Card | 150 | CARD-NUM | CBACT02C, COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, CBEXPORT, CBIMPORT |
| 3 | CVACT03Y | Card Cross-Ref | 50 | XREF-CARD-NUM | CBACT03C, CBACT04C, COACTVWC, COTRN02C, COBIL00C, CBEXPORT, CBIMPORT, CBSTM03A |
| 4 | CVCUS01Y | Customer | 500 | CUST-ID | CBCUS01C, COACTVWC, COCRDSLC, COCRDUPC, CBEXPORT, CBIMPORT |
| 5 | CVTRA05Y | Transaction | 350 | TRAN-ID | COTRN00C, COTRN01C, COTRN02C, CORPT00C, COBIL00C, CBACT04C, CBEXPORT, CBIMPORT, CBTRN01C, CBTRN02C |
| 6 | CVTRA06Y | Daily Transaction | 350 | DALYTRAN-ID | CBTRN01C, CBTRN02C |
| 7 | CVTRA01Y | Category Balance | ~50 | TRAN-CAT-KEY | CBACT04C, CBTRN01C |
| 8 | CVTRA02Y | Discount Group | ~50 | DIS-GROUP-KEY | CBACT04C |
| 9 | CVTRA03Y | Transaction Type | 60 | TRAN-TYPE | CBTRN01C, CBTRN03C |
| 10 | CVTRA04Y | Transaction Category | 60 | TRAN-CAT-KEY | CBTRN03C |
| 11 | CVTRA07Y | Report Layouts | N/A | N/A | CBTRN03C |
| 12 | CSUSR01Y | User Security | 80 | SEC-USR-ID | COSGN00C, COMEN01C, COADM01C, COUSR00C-03C, COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| 13 | COCOM01Y | COMMAREA | ~200 | N/A | All online CICS programs |
| 14 | CVCRD01Y | Card Work Area | ~250 | N/A | COCRDLIC, COCRDSLC, COCRDUPC |
| 15 | CVEXPORT | Export Record | 500 | EXPORT-SEQUENCE-NUM | CBEXPORT, CBIMPORT |
| 16 | COSTM01 | Statement Txn | ~350 | TRNX-KEY | CBSTM03A |
| 17 | CUSTREC | Customer (alt) | 500 | CUST-ID | CBSTM03A |
| 18 | CODATECN | Date Conversion | ~30 | N/A | CBACT01C |
| 19 | COADM02Y | Admin Menu | N/A | N/A | COADM01C |
| 20 | COMEN02Y | User Menu | N/A | N/A | COMEN01C |
| 21 | COTTL01Y | Screen Title | N/A | N/A | All online CICS programs |
| 22 | CSDAT01Y | Date Display | N/A | N/A | All online CICS programs |
| 23 | CSMSG01Y | Message Area | N/A | N/A | All online CICS programs |
| 24 | CSMSG02Y | Extended Msg | N/A | N/A | COACTVWC, COCRDSLC, COCRDUPC |
| 25 | CSSETATY | Set Attributes | N/A | N/A | Online programs with field coloring |
| 26 | CSSTRPFY | String Strip | N/A | N/A | COACTVWC, COCRDLIC, COCRDSLC, COCRDUPC |
| 27 | CSLKPCDY | Lookup Codes | N/A | N/A | Online programs with validation |
| 28 | CSUTLDPY | Date Validation | N/A | N/A | CORPT00C, COTRN02C (via inline COPY) |
| 29 | CSUTLDWY | Date WS Vars | N/A | N/A | Used with CSUTLDPY |
| 30 | UNUSED1Y | Unused | 80 | N/A | None (deprecated) |
