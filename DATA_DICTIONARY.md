# DATA DICTIONARY — CardDemo COBOL Codebase

> **Generated:** 2026-03-26 | **Scope:** `uc-legacy-modernization-cobol-to-java`
>
> This dictionary extracts every business entity from the COBOL copybook PIC clauses
> and presents them in a business-friendly format suitable for Java POJO / database-table mapping.

---

## Entity Relationship Overview

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│   Customer   │1────M│   Account    │1────M│     Card     │
│  (CVCUS01Y)  │       │  (CVACT01Y)  │       │  (CVACT02Y)  │
└──────┬───────┘       └──────┬───────┘       └──────┬───────┘
       │                      │                      │
       │                      │                      │
       └──────────┬───────────┘                      │
                  │                                   │
           ┌──────┴───────┐                          │
           │  Card XREF   │◄─────────────────────────┘
           │  (CVACT03Y)  │
           └──────┬───────┘
                  │
                  ▼
           ┌──────────────┐       ┌──────────────┐
           │ Transaction  │──────►│  Tran Type   │
           │  (CVTRA05Y)  │       │  (CVTRA03Y)  │
           └──────┬───────┘       └──────────────┘
                  │                      │
                  │               ┌──────┴───────┐
                  │               │ Tran Category│
                  │               │  (CVTRA04Y)  │
                  │               └──────────────┘
                  │
           ┌──────┴───────┐       ┌──────────────┐
           │  Tran Cat    │       │  Disclosure  │
           │   Balance    │       │    Group     │
           │  (CVTRA01Y)  │       │  (CVTRA02Y)  │
           └──────────────┘       └──────────────┘

           ┌──────────────┐
           │  User Sec    │  (standalone)
           │  (CSUSR01Y)  │
           └──────────────┘
```

---

## 1. Customer — `CVCUS01Y.cpy`

**VSAM File:** `CUSTFILE` (KSDS, Record Length 500 bytes)
**Java Target:** `Customer.java` / `customer` table

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | `CUST-ID` | `PIC 9(09)` | `long` | 9 digits | Customer ID | Primary key — unique customer identifier |
| 2 | `CUST-FIRST-NAME` | `PIC X(25)` | `String` | 25 chars | First Name | Customer's first/given name |
| 3 | `CUST-MIDDLE-NAME` | `PIC X(25)` | `String` | 25 chars | Middle Name | Customer's middle name |
| 4 | `CUST-LAST-NAME` | `PIC X(25)` | `String` | 25 chars | Last Name | Customer's surname |
| 5 | `CUST-ADDR-LINE-1` | `PIC X(50)` | `String` | 50 chars | Address Line 1 | Primary street address |
| 6 | `CUST-ADDR-LINE-2` | `PIC X(50)` | `String` | 50 chars | Address Line 2 | Secondary address (apt, suite) |
| 7 | `CUST-ADDR-LINE-3` | `PIC X(50)` | `String` | 50 chars | Address Line 3 | Additional address line |
| 8 | `CUST-ADDR-STATE-CD` | `PIC X(02)` | `String` | 2 chars | State Code | US state abbreviation |
| 9 | `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | `String` | 3 chars | Country Code | ISO country code |
| 10 | `CUST-ADDR-ZIP` | `PIC X(10)` | `String` | 10 chars | ZIP Code | Postal/ZIP code |
| 11 | `CUST-PHONE-NUM-1` | `PIC X(15)` | `String` | 15 chars | Primary Phone | Primary contact number |
| 12 | `CUST-PHONE-NUM-2` | `PIC X(15)` | `String` | 15 chars | Secondary Phone | Alternate contact number |
| 13 | `CUST-SSN` | `PIC 9(09)` | `long` | 9 digits | SSN | Social Security Number (**PII — encrypt**) |
| 14 | `CUST-GOVT-ISSUED-ID` | `PIC X(20)` | `String` | 20 chars | Government ID | Government-issued identification number |
| 15 | `CUST-DOB-YYYY-MM-DD` | `PIC X(10)` | `LocalDate` | 10 chars | Date of Birth | Customer DOB in YYYY-MM-DD format |
| 16 | `CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | `String` | 10 chars | EFT Account ID | Electronic Funds Transfer account reference |
| 17 | `CUST-PRI-CARD-HOLDER-IND` | `PIC X(01)` | `String` | 1 char | Primary Cardholder | "Y"/"N" — is this the primary cardholder? |
| 18 | `CUST-FICO-CREDIT-SCORE` | `PIC 9(03)` | `int` | 3 digits | FICO Score | Credit score (300-850) |
| 19 | `FILLER` | `PIC X(168)` | — | 168 chars | Reserved | Future use |

---

## 2. Account — `CVACT01Y.cpy`

**VSAM File:** `ACCTDAT` / `ACCTFILE` (KSDS, Record Length 300 bytes)
**Java Target:** `Account.java` / `account` table

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | `ACCT-ID` | `PIC 9(11)` | `long` | 11 digits | Account ID | Primary key — unique account number |
| 2 | `ACCT-ACTIVE-STATUS` | `PIC X(01)` | `String` | 1 char | Active Status | "Y"=Active, "N"=Inactive |
| 3 | `ACCT-CURR-BAL` | `PIC S9(10)V99` | `BigDecimal` | 12,2 | Current Balance | Current outstanding balance (signed) |
| 4 | `ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | `BigDecimal` | 12,2 | Credit Limit | Maximum credit line |
| 5 | `ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | `BigDecimal` | 12,2 | Cash Advance Limit | Cash advance credit limit |
| 6 | `ACCT-OPEN-DATE` | `PIC X(10)` | `LocalDate` | 10 chars | Open Date | Account opening date |
| 7 | `ACCT-EXPIRAION-DATE` | `PIC X(10)` | `LocalDate` | 10 chars | Expiration Date | Account expiration date |
| 8 | `ACCT-REISSUE-DATE` | `PIC X(10)` | `LocalDate` | 10 chars | Reissue Date | Last card reissue date |
| 9 | `ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | `BigDecimal` | 12,2 | Current Cycle Credits | Total credits in current billing cycle |
| 10 | `ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | `BigDecimal` | 12,2 | Current Cycle Debits | Total debits in current billing cycle |
| 11 | `ACCT-ADDR-ZIP` | `PIC X(10)` | `String` | 10 chars | Account ZIP | ZIP code associated with account |
| 12 | `ACCT-GROUP-ID` | `PIC X(10)` | `String` | 10 chars | Group ID | Disclosure/pricing group for interest rates |
| 13 | `FILLER` | `PIC X(178)` | — | 178 chars | Reserved | Future use |

---

## 3. Card — `CVACT02Y.cpy`

**VSAM File:** `CARDDAT` / `CARDFILE` (KSDS, Record Length 150 bytes)
**Java Target:** `Card.java` / `card` table

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | `CARD-NUM` | `PIC X(16)` | `String` | 16 chars | Card Number | Primary key — 16-digit card number (**PCI — tokenize**) |
| 2 | `CARD-ACCT-ID` | `PIC 9(11)` | `long` | 11 digits | Account ID | FK → Account.ACCT-ID |
| 3 | `CARD-CVV-CD` | `PIC 9(03)` | `int` | 3 digits | CVV Code | Card verification value (**PCI — do not store**) |
| 4 | `CARD-EMBOSSED-NAME` | `PIC X(50)` | `String` | 50 chars | Embossed Name | Name embossed on physical card |
| 5 | `CARD-EXPIRAION-DATE` | `PIC X(10)` | `LocalDate` | 10 chars | Expiration Date | Card expiration date |
| 6 | `CARD-ACTIVE-STATUS` | `PIC X(01)` | `String` | 1 char | Active Status | "Y"=Active, "N"=Inactive |
| 7 | `FILLER` | `PIC X(59)` | — | 59 chars | Reserved | Future use |

---

## 4. Card Cross-Reference — `CVACT03Y.cpy`

**VSAM File:** `CARDXREF` / `XREFFILE` (KSDS, Record Length 50 bytes)
**Java Target:** `CardCrossReference.java` / `card_xref` table (or resolved via JOINs)

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | `XREF-CARD-NUM` | `PIC X(16)` | `String` | 16 chars | Card Number | FK → Card.CARD-NUM (primary key of XREF) |
| 2 | `XREF-CUST-ID` | `PIC 9(09)` | `long` | 9 digits | Customer ID | FK → Customer.CUST-ID |
| 3 | `XREF-ACCT-ID` | `PIC 9(11)` | `long` | 11 digits | Account ID | FK → Account.ACCT-ID |
| 4 | `FILLER` | `PIC X(14)` | — | 14 chars | Reserved | Future use |

> **Note:** In a relational database, this cross-reference entity can be eliminated by placing `CUST-ID` directly on the Card or Account table and using JOINs.

---

## 5. Transaction — `CVTRA05Y.cpy`

**VSAM File:** `TRANSACT` / `TRANFILE` (KSDS, Record Length 350 bytes)
**Java Target:** `Transaction.java` / `transaction` table

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | `TRAN-ID` | `PIC X(16)` | `String` | 16 chars | Transaction ID | Primary key — unique transaction identifier |
| 2 | `TRAN-TYPE-CD` | `PIC X(02)` | `String` | 2 chars | Transaction Type | FK → TransactionType.TRAN-TYPE ("SA"=Sale, "RT"=Return, etc.) |
| 3 | `TRAN-CAT-CD` | `PIC 9(04)` | `int` | 4 digits | Category Code | FK → TransactionCategory.TRAN-CAT-CD |
| 4 | `TRAN-SOURCE` | `PIC X(10)` | `String` | 10 chars | Source | Transaction source (POS, ATM, Online, etc.) |
| 5 | `TRAN-DESC` | `PIC X(100)` | `String` | 100 chars | Description | Free-text transaction description |
| 6 | `TRAN-AMT` | `PIC S9(09)V99` | `BigDecimal` | 11,2 | Amount | Transaction amount (signed — negative=credit) |
| 7 | `TRAN-MERCHANT-ID` | `PIC 9(09)` | `long` | 9 digits | Merchant ID | Merchant identification number |
| 8 | `TRAN-MERCHANT-NAME` | `PIC X(50)` | `String` | 50 chars | Merchant Name | Merchant business name |
| 9 | `TRAN-MERCHANT-CITY` | `PIC X(50)` | `String` | 50 chars | Merchant City | Merchant city |
| 10 | `TRAN-MERCHANT-ZIP` | `PIC X(10)` | `String` | 10 chars | Merchant ZIP | Merchant postal code |
| 11 | `TRAN-CARD-NUM` | `PIC X(16)` | `String` | 16 chars | Card Number | FK → Card.CARD-NUM |
| 12 | `TRAN-ORIG-TS` | `PIC X(26)` | `LocalDateTime` | 26 chars | Origination Timestamp | When the transaction was initiated |
| 13 | `TRAN-PROC-TS` | `PIC X(26)` | `LocalDateTime` | 26 chars | Processing Timestamp | When the transaction was posted |
| 14 | `FILLER` | `PIC X(20)` | — | 20 chars | Reserved | Future use |

---

## 6. Daily Transaction — `CVTRA06Y.cpy`

**VSAM File:** `DALYTRAN` (Sequential PS, Record Length 350 bytes)
**Java Target:** Merged into `Transaction.java` with a `status` flag, or a staging table

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | `DALYTRAN-ID` | `PIC X(16)` | `String` | 16 chars | Transaction ID | Same layout as Transaction |
| 2 | `DALYTRAN-TYPE-CD` | `PIC X(02)` | `String` | 2 chars | Transaction Type | Same as TRAN-TYPE-CD |
| 3 | `DALYTRAN-CAT-CD` | `PIC 9(04)` | `int` | 4 digits | Category Code | Same as TRAN-CAT-CD |
| 4 | `DALYTRAN-SOURCE` | `PIC X(10)` | `String` | 10 chars | Source | Same as TRAN-SOURCE |
| 5 | `DALYTRAN-DESC` | `PIC X(100)` | `String` | 100 chars | Description | Same as TRAN-DESC |
| 6 | `DALYTRAN-AMT` | `PIC S9(09)V99` | `BigDecimal` | 11,2 | Amount | Same as TRAN-AMT |
| 7 | `DALYTRAN-MERCHANT-ID` | `PIC 9(09)` | `long` | 9 digits | Merchant ID | Same as TRAN-MERCHANT-ID |
| 8 | `DALYTRAN-MERCHANT-NAME` | `PIC X(50)` | `String` | 50 chars | Merchant Name | Same as TRAN-MERCHANT-NAME |
| 9 | `DALYTRAN-MERCHANT-CITY` | `PIC X(50)` | `String` | 50 chars | Merchant City | Same as TRAN-MERCHANT-CITY |
| 10 | `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)` | `String` | 10 chars | Merchant ZIP | Same as TRAN-MERCHANT-ZIP |
| 11 | `DALYTRAN-CARD-NUM` | `PIC X(16)` | `String` | 16 chars | Card Number | Same as TRAN-CARD-NUM |
| 12 | `DALYTRAN-ORIG-TS` | `PIC X(26)` | `LocalDateTime` | 26 chars | Origination Timestamp | Same as TRAN-ORIG-TS |
| 13 | `DALYTRAN-PROC-TS` | `PIC X(26)` | `LocalDateTime` | 26 chars | Processing Timestamp | Same as TRAN-PROC-TS |
| 14 | `FILLER` | `PIC X(20)` | — | 20 chars | Reserved | Future use |

> **Modernization Note:** CVTRA06Y is structurally identical to CVTRA05Y. In a modernized system, daily transactions should be staged in the same `transaction` table with a `status` column (e.g., `PENDING`, `POSTED`, `REJECTED`) rather than maintaining a separate entity.

---

## 7. Transaction Category Balance — `CVTRA01Y.cpy`

**VSAM File:** `TCATBALF` (KSDS, Record Length 50 bytes)
**Java Target:** `TransactionCategoryBalance.java` / `tran_cat_balance` table

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | `TRANCAT-ACCT-ID` | `PIC 9(11)` | `long` | 11 digits | Account ID | FK → Account (part of composite key) |
| 2 | `TRANCAT-TYPE-CD` | `PIC X(02)` | `String` | 2 chars | Transaction Type | FK → TransactionType (part of composite key) |
| 3 | `TRANCAT-CD` | `PIC 9(04)` | `int` | 4 digits | Category Code | FK → TransactionCategory (part of composite key) |
| 4 | `TRAN-CAT-BAL` | `PIC S9(09)V99` | `BigDecimal` | 11,2 | Category Balance | Running balance per account/type/category |
| 5 | `FILLER` | `PIC X(22)` | — | 22 chars | Reserved | Future use |

---

## 8. Disclosure Group — `CVTRA02Y.cpy`

**VSAM File:** `DISCGRP` (KSDS, Record Length 50 bytes)
**Java Target:** `DisclosureGroup.java` / `disclosure_group` table

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | `DIS-ACCT-GROUP-ID` | `PIC X(10)` | `String` | 10 chars | Account Group ID | Pricing group identifier (part of composite key) |
| 2 | `DIS-TRAN-TYPE-CD` | `PIC X(02)` | `String` | 2 chars | Transaction Type | FK → TransactionType (part of composite key) |
| 3 | `DIS-TRAN-CAT-CD` | `PIC 9(04)` | `int` | 4 digits | Category Code | FK → TransactionCategory (part of composite key) |
| 4 | `DIS-INT-RATE` | `PIC S9(04)V99` | `BigDecimal` | 6,2 | Interest Rate | Annual interest rate percentage |
| 5 | `FILLER` | `PIC X(28)` | — | 28 chars | Reserved | Future use |

---

## 9. Transaction Type — `CVTRA03Y.cpy`

**VSAM File:** `TRANTYPE` (KSDS, Record Length 60 bytes)
**Java Target:** `TransactionType.java` / `transaction_type` table

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | `TRAN-TYPE` | `PIC X(02)` | `String` | 2 chars | Type Code | Primary key — e.g., "SA"=Sale, "RT"=Return, "PM"=Payment |
| 2 | `TRAN-TYPE-DESC` | `PIC X(50)` | `String` | 50 chars | Type Description | Human-readable type name |
| 3 | `FILLER` | `PIC X(08)` | — | 8 chars | Reserved | Future use |

---

## 10. Transaction Category — `CVTRA04Y.cpy`

**VSAM File:** `TRANCATG` (KSDS, Record Length 60 bytes)
**Java Target:** `TransactionCategory.java` / `transaction_category` table

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | `TRAN-TYPE-CD` | `PIC X(02)` | `String` | 2 chars | Type Code | FK → TransactionType (part of composite key) |
| 2 | `TRAN-CAT-CD` | `PIC 9(04)` | `int` | 4 digits | Category Code | Category within type (part of composite key) |
| 3 | `TRAN-CAT-TYPE-DESC` | `PIC X(50)` | `String` | 50 chars | Category Description | Human-readable category name |
| 4 | `FILLER` | `PIC X(04)` | — | 4 chars | Reserved | Future use |

---

## 11. User Security — `CSUSR01Y.cpy`

**VSAM File:** `USRSEC` (KSDS, Record Length 80 bytes)
**Java Target:** `UserSecurity.java` / `user_security` table (or Spring Security `UserDetails`)

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | `SEC-USR-ID` | `PIC X(08)` | `String` | 8 chars | User ID | Primary key — login identifier |
| 2 | `SEC-USR-FNAME` | `PIC X(20)` | `String` | 20 chars | First Name | User's first name |
| 3 | `SEC-USR-LNAME` | `PIC X(20)` | `String` | 20 chars | Last Name | User's last name |
| 4 | `SEC-USR-PWD` | `PIC X(08)` | `String` | 8 chars | Password | Plaintext password (**must hash in Java — BCrypt**) |
| 5 | `SEC-USR-TYPE` | `PIC X(01)` | `String` | 1 char | User Type | "A"=Admin, "U"=Regular User |
| 6 | `SEC-USR-FILLER` | `PIC X(23)` | — | 23 chars | Reserved | Future use |

---

## 12. Statement Transaction Layout — `COSTM01.CPY`

**Usage:** Re-keyed transaction record used by CBSTM03A/B for statement generation
**Java Target:** Merged into `Transaction.java` (no separate entity needed)

| # | COBOL Field | PIC Clause | Java Type | Size | Business Name | Description |
|---|-------------|------------|-----------|------|---------------|-------------|
| 1 | `TRNX-CARD-NUM` | `PIC X(16)` | `String` | 16 chars | Card Number | Part of composite key |
| 2 | `TRNX-ID` | `PIC X(16)` | `String` | 16 chars | Transaction ID | Part of composite key |
| 3 | `TRNX-TYPE-CD` | `PIC X(02)` | `String` | 2 chars | Type Code | Transaction type |
| 4 | `TRNX-CAT-CD` | `PIC 9(04)` | `int` | 4 digits | Category Code | Transaction category |
| 5 | `TRNX-SOURCE` | `PIC X(10)` | `String` | 10 chars | Source | Transaction source |
| 6 | `TRNX-DESC` | `PIC X(100)` | `String` | 100 chars | Description | Transaction description |
| 7 | `TRNX-AMT` | `PIC S9(09)V99` | `BigDecimal` | 11,2 | Amount | Transaction amount |
| 8 | `TRNX-MERCHANT-ID` | `PIC 9(09)` | `long` | 9 digits | Merchant ID | Merchant identifier |
| 9 | `TRNX-MERCHANT-NAME` | `PIC X(50)` | `String` | 50 chars | Merchant Name | Merchant name |
| 10 | `TRNX-MERCHANT-CITY` | `PIC X(50)` | `String` | 50 chars | Merchant City | Merchant city |
| 11 | `TRNX-MERCHANT-ZIP` | `PIC X(10)` | `String` | 10 chars | Merchant ZIP | Merchant postal code |
| 12 | `TRNX-ORIG-TS` | `PIC X(26)` | `LocalDateTime` | 26 chars | Origination TS | Transaction origination time |
| 13 | `TRNX-PROC-TS` | `PIC X(26)` | `LocalDateTime` | 26 chars | Processing TS | Transaction processing time |

---

## 13. Transaction Report Layout — `CVTRA07Y.cpy`

**Usage:** Print report formatting — not a stored entity
**Java Target:** Report DTO / View Model only

| # | COBOL Field | PIC Clause | Business Name | Description |
|---|-------------|------------|---------------|-------------|
| 1 | `REPT-SHORT-NAME` | `PIC X(38)` | Report Short Name | Value: "DALYREPT" |
| 2 | `REPT-LONG-NAME` | `PIC X(41)` | Report Long Name | Value: "Daily Transaction Report" |
| 3 | `REPT-START-DATE` | `PIC X(10)` | Start Date | Report date range start |
| 4 | `REPT-END-DATE` | `PIC X(10)` | End Date | Report date range end |
| 5 | `TRAN-REPORT-TRANS-ID` | `PIC X(16)` | Transaction ID | Detail line — transaction ID |
| 6 | `TRAN-REPORT-ACCOUNT-ID` | `PIC X(11)` | Account ID | Detail line — account |
| 7 | `TRAN-REPORT-TYPE-CD` | `PIC X(02)` | Type Code | Detail line — type code |
| 8 | `TRAN-REPORT-TYPE-DESC` | `PIC X(15)` | Type Description | Detail line — type name |
| 9 | `TRAN-REPORT-CAT-CD` | `PIC 9(04)` | Category Code | Detail line — category code |
| 10 | `TRAN-REPORT-CAT-DESC` | `PIC X(29)` | Category Description | Detail line — category name |
| 11 | `TRAN-REPORT-SOURCE` | `PIC X(10)` | Source | Detail line — transaction source |
| 12 | `TRAN-REPORT-AMT` | `PIC -ZZZ,ZZZ,ZZZ.ZZ` | Amount | Detail line — formatted amount |
| 13 | `REPT-PAGE-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Page Total | Per-page subtotal |
| 14 | `REPT-ACCOUNT-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Account Total | Per-account subtotal |
| 15 | `REPT-GRAND-TOTAL` | `PIC +ZZZ,ZZZ,ZZZ.ZZ` | Grand Total | Report grand total |

---

## 14. Export Record — `CVEXPORT.cpy`

**Usage:** Multi-entity export/import format used by CBEXPORT/CBIMPORT
**Java Target:** Not a persistent entity — serialization format only

Contains sub-records for Account, Card, Cross-Reference, Customer, and Transaction with record-type indicators, enabling all VSAM data to be serialized into a single sequential file.

---

## 15. COMMAREA — `COCOM01Y.cpy`

**Usage:** CICS inter-program communication area — passed between all online programs
**Java Target:** `SessionContext.java` or HTTP session attributes

| # | COBOL Field | PIC Clause | Java Type | Business Name | Description |
|---|-------------|------------|-----------|---------------|-------------|
| 1 | `CDEMO-FROM-TRANID` | `PIC X(04)` | `String` | Source Transaction | Previous CICS transaction ID |
| 2 | `CDEMO-FROM-PROGRAM` | `PIC X(08)` | `String` | Source Program | Previous program name |
| 3 | `CDEMO-TO-TRANID` | `PIC X(04)` | `String` | Target Transaction | Next CICS transaction ID |
| 4 | `CDEMO-TO-PROGRAM` | `PIC X(08)` | `String` | Target Program | Next program name |
| 5 | `CDEMO-USER-ID` | `PIC X(08)` | `String` | User ID | Authenticated user |
| 6 | `CDEMO-USER-TYPE` | `PIC X(01)` | `String` | User Type | "A"=Admin, "U"=User (88-level conditions) |
| 7 | `CDEMO-PGM-CONTEXT` | `PIC 9(01)` | `int` | Program Context | 0=First entry, 1=Re-entry |
| 8 | `CDEMO-CUST-ID` | `PIC 9(09)` | `long` | Customer ID | Current customer context |
| 9 | `CDEMO-CUST-FNAME` | `PIC X(25)` | `String` | First Name | Cached customer first name |
| 10 | `CDEMO-CUST-MNAME` | `PIC X(25)` | `String` | Middle Name | Cached customer middle name |
| 11 | `CDEMO-CUST-LNAME` | `PIC X(25)` | `String` | Last Name | Cached customer last name |
| 12 | `CDEMO-ACCT-ID` | `PIC 9(11)` | `long` | Account ID | Current account context |
| 13 | `CDEMO-ACCT-STATUS` | `PIC X(01)` | `String` | Account Status | Current account status |
| 14 | `CDEMO-CARD-NUM` | `PIC 9(16)` | `long` | Card Number | Current card context |
| 15 | `CDEMO-LAST-MAP` | `PIC X(7)` | `String` | Last Map | Last displayed BMS map name |
| 16 | `CDEMO-LAST-MAPSET` | `PIC X(7)` | `String` | Last Mapset | Last BMS mapset name |

---

## 16. Date/Time — `CSDAT01Y.cpy`

**Usage:** Working-storage for current date/time and formatted timestamps
**Java Target:** Replace with `java.time.LocalDateTime` / `java.time.format.DateTimeFormatter`

| # | COBOL Field | PIC Clause | Java Type | Business Name |
|---|-------------|------------|-----------|---------------|
| 1 | `WS-CURDATE-YEAR` | `PIC 9(04)` | `int` | Current Year |
| 2 | `WS-CURDATE-MONTH` | `PIC 9(02)` | `int` | Current Month |
| 3 | `WS-CURDATE-DAY` | `PIC 9(02)` | `int` | Current Day |
| 4 | `WS-CURTIME-HOURS` | `PIC 9(02)` | `int` | Current Hours |
| 5 | `WS-CURTIME-MINUTE` | `PIC 9(02)` | `int` | Current Minutes |
| 6 | `WS-CURTIME-SECOND` | `PIC 9(02)` | `int` | Current Seconds |
| 7 | `WS-TIMESTAMP` | `PIC (formatted)` | `LocalDateTime` | Full Timestamp |

---

## 17. Authorization Module Entities

### 17a. Pending Authorization Detail — `CIPAUDTY.cpy`

**Storage:** IMS DB + DB2 table
**Java Target:** `PendingAuthorization.java` / `pending_authorization` table

Contains authorization request details: account ID, card number, authorization amount, merchant information, request timestamp, fraud flag, and processing status.

### 17b. Pending Authorization Summary — `CIPAUSMY.cpy`

**Storage:** IMS DB
**Java Target:** View/projection of `PendingAuthorization`

Summary-level fields for the authorization list screen — count, total amount, status.

### 17c. Authorization Request/Reply/Error — `CCPAURQY.cpy`, `CCPAURLY.cpy`, `CCPAUERY.cpy`

**Usage:** MQ message structures for authorization request/response
**Java Target:** Request/Response DTOs for REST API or message queue integration

---

## VSAM-to-Database Mapping Summary

| VSAM File | VSAM Type | Record Len | Copybook | Proposed Table | Primary Key |
|-----------|-----------|------------|----------|---------------|-------------|
| `ACCTDAT` | KSDS | 300 | CVACT01Y | `account` | `acct_id` |
| `CARDDAT` | KSDS | 150 | CVACT02Y | `card` | `card_num` |
| `CARDXREF` | KSDS | 50 | CVACT03Y | `card_xref` (or eliminate) | `xref_card_num` |
| `CUSTFILE` | KSDS | 500 | CVCUS01Y | `customer` | `cust_id` |
| `TRANSACT` | KSDS | 350 | CVTRA05Y | `transaction` | `tran_id` |
| `DALYTRAN` | PS | 350 | CVTRA06Y | `transaction` (staging) | `dalytran_id` |
| `USRSEC` | KSDS | 80 | CSUSR01Y | `user_security` | `sec_usr_id` |
| `TCATBALF` | KSDS | 50 | CVTRA01Y | `tran_cat_balance` | `(acct_id, type_cd, cat_cd)` |
| `DISCGRP` | KSDS | 50 | CVTRA02Y | `disclosure_group` | `(group_id, type_cd, cat_cd)` |
| `TRANTYPE` | KSDS | 60 | CVTRA03Y | `transaction_type` | `tran_type` |
| `TRANCATG` | KSDS | 60 | CVTRA04Y | `transaction_category` | `(type_cd, cat_cd)` |

---

## PIC Clause Quick Reference

| PIC Pattern | COBOL Meaning | Java Equivalent |
|-------------|--------------|-----------------|
| `PIC 9(n)` | Unsigned integer, n digits | `int` / `long` |
| `PIC S9(n)V99` | Signed decimal, n+2 digits with 2 decimal places | `BigDecimal` |
| `PIC X(n)` | Alphanumeric string, n characters | `String` |
| `PIC S9(n) COMP` | Binary integer | `int` / `long` |
| `PIC S9(n) COMP-3` | Packed decimal | `BigDecimal` |
| `PIC -ZZZ,ZZZ,ZZZ.ZZ` | Edited numeric (display only) | `String` (formatted) |
