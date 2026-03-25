# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
>
> This document extracts every business entity from the COBOL copybook PIC clauses and presents them in a business-friendly format suitable for Java domain modeling, database schema design, and API contract definition.

---

## 1. Entity Summary

| # | Entity | Copybook | Record Length | VSAM Dataset | Key | Java Target |
|---|---|---|---|---|---|---|
| 1 | Account | CVACT01Y | 300 bytes | ACCTDATA.VSAM.KSDS | Account ID (11-digit) | `Account.java` |
| 2 | Card | CVACT02Y | 150 bytes | CARDDATA.VSAM.KSDS | Card Number (16-digit) | `Card.java` |
| 3 | Card Cross-Reference | CVACT03Y | 50 bytes | CARDXREF.VSAM.KSDS | Card Number (16-digit) | `CardXref.java` |
| 4 | Customer | CVCUS01Y | 500 bytes | CUSTDATA.VSAM.KSDS | Customer ID (9-digit) | `Customer.java` |
| 5 | Transaction | CVTRA05Y | 350 bytes | TRANSACT.VSAM.KSDS | Transaction ID (16-char) | `Transaction.java` |
| 6 | Daily Transaction | CVTRA06Y | 350 bytes | DALYTRAN.PS | Transaction ID (16-char) | `DailyTransaction.java` |
| 7 | Tran Category Balance | CVTRA01Y | 50 bytes | TCATBALF.VSAM.KSDS | Acct ID + Type + Cat | `TranCategoryBalance.java` |
| 8 | Disclosure Group | CVTRA02Y | 50 bytes | DISCGRP.VSAM.KSDS | Group ID + Type + Cat | `DisclosureGroup.java` |
| 9 | Transaction Type | CVTRA03Y | 60 bytes | TRANTYPE.VSAM.KSDS | Type Code (2-char) | `TransactionType.java` |
| 10 | Transaction Category | CVTRA04Y | 60 bytes | TRANCATG.VSAM.KSDS | Type + Cat Code | `TransactionCategory.java` |
| 11 | User Security | CSUSR01Y | 80 bytes | USRSEC.VSAM.KSDS | User ID (8-char) | `UserSecurity.java` |
| 12 | COMMAREA | COCOM01Y | ~120 bytes | N/A (memory) | N/A | `CardDemoContext.java` |
| 13 | Card Detail (screen) | CVCRD01Y | variable | N/A | N/A | `CardDetailDTO.java` |
| 14 | Export Record | CVEXPORT | variable | EXPORT.DATA | Record Type | `ExportRecord.java` |
| 15 | Statement Transaction | COSTM01 | 350 bytes | TRXFL.VSAM.KSDS | Card+Tran ID (32-char) | `StatementTransaction.java` |
| 16 | Customer (statement) | CUSTREC | variable | N/A | N/A | embedded in Customer |
| 17 | Report Layout | CVTRA07Y | 133 bytes | N/A (print) | N/A | Report template |

---

## 2. Account Entity (CVACT01Y.cpy)

**VSAM File:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` · **Record Length:** 300 bytes

| # | COBOL Field | PIC Clause | Offset | Length | Java Type | Business Name | Description |
|---|---|---|---|---|---|---|---|
| 1 | ACCT-ID | 9(11) | 0 | 11 | `long` | Account ID | Unique account identifier |
| 2 | ACCT-ACTIVE-STATUS | X(01) | 11 | 1 | `String` | Active Status | Account status flag (Y/N) |
| 3 | ACCT-CURR-BAL | S9(10)V99 | 12 | 12 | `BigDecimal` | Current Balance | Current account balance |
| 4 | ACCT-CREDIT-LIMIT | S9(10)V99 | 24 | 12 | `BigDecimal` | Credit Limit | Maximum credit limit |
| 5 | ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | 36 | 12 | `BigDecimal` | Cash Credit Limit | Cash advance limit |
| 6 | ACCT-OPEN-DATE | X(10) | 48 | 10 | `LocalDate` | Open Date | Date account was opened |
| 7 | ACCT-EXPIRAION-DATE | X(10) | 58 | 10 | `LocalDate` | Expiration Date | Account expiration date |
| 8 | ACCT-REISSUE-DATE | X(10) | 68 | 10 | `LocalDate` | Reissue Date | Last card reissue date |
| 9 | ACCT-CURR-CYC-CREDIT | S9(10)V99 | 78 | 12 | `BigDecimal` | Cycle Credits | Current cycle credit total |
| 10 | ACCT-CURR-CYC-DEBIT | S9(10)V99 | 90 | 12 | `BigDecimal` | Cycle Debits | Current cycle debit total |
| 11 | ACCT-ADDR-ZIP | X(10) | 102 | 10 | `String` | ZIP Code | Account holder ZIP code |
| 12 | ACCT-GROUP-ID | X(10) | 112 | 10 | `String` | Group ID | Disclosure/rate group |
| 13 | FILLER | X(158) | 122 | 158 | — | (reserved) | Reserved for future use |

### Business Rules
- **Key:** `ACCT-ID` (11-digit numeric, VSAM KSDS primary key)
- **Alternate Index:** via CARDXREF on Account ID
- Balance fields use signed packed decimal (S9(10)V99) → map to `BigDecimal(12,2)`
- Date fields stored as `YYYY-MM-DD` character strings

---

## 3. Card Entity (CVACT02Y.cpy)

**VSAM File:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS` · **Record Length:** 150 bytes

| # | COBOL Field | PIC Clause | Offset | Length | Java Type | Business Name | Description |
|---|---|---|---|---|---|---|---|
| 1 | CARD-NUM | X(16) | 0 | 16 | `String` | Card Number | 16-digit card number |
| 2 | CARD-ACCT-ID | 9(11) | 16 | 11 | `long` | Account ID | Owning account reference |
| 3 | CARD-CVV-CD | 9(03) | 27 | 3 | `String` | CVV Code | Card verification value |
| 4 | CARD-EMBOSSED-NAME | X(50) | 30 | 50 | `String` | Embossed Name | Name printed on card |
| 5 | CARD-EXPIRAION-DATE | X(10) | 80 | 10 | `LocalDate` | Expiration Date | Card expiration date |
| 6 | CARD-ACTIVE-STATUS | X(01) | 90 | 1 | `String` | Active Status | Card status (Y/N) |
| 7 | FILLER | X(59) | 91 | 59 | — | (reserved) | Reserved for future use |

### Business Rules
- **Key:** `CARD-NUM` (16-character, VSAM KSDS primary key)
- **Foreign Key:** `CARD-ACCT-ID` → Account.ACCT-ID
- CVV stored as numeric(3) — sensitive PCI data, must be encrypted in Java

---

## 4. Card Cross-Reference Entity (CVACT03Y.cpy)

**VSAM File:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` · **Record Length:** 50 bytes

| # | COBOL Field | PIC Clause | Offset | Length | Java Type | Business Name | Description |
|---|---|---|---|---|---|---|---|
| 1 | XREF-CARD-NUM | X(16) | 0 | 16 | `String` | Card Number | Card number (primary key) |
| 2 | XREF-CUST-ID | 9(09) | 16 | 9 | `long` | Customer ID | Owning customer reference |
| 3 | XREF-ACCT-ID | 9(11) | 25 | 11 | `long` | Account ID | Associated account reference |
| 4 | FILLER | X(14) | 36 | 14 | — | (reserved) | Reserved for future use |

### Business Rules
- **Key:** `XREF-CARD-NUM` (16-character, VSAM KSDS primary key)
- **Alternate Index:** on `XREF-ACCT-ID` (non-unique, allows lookup by account)
- This is the central junction table linking Card ↔ Customer ↔ Account

---

## 5. Customer Entity (CVCUS01Y.cpy)

**VSAM File:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS` · **Record Length:** 500 bytes

| # | COBOL Field | PIC Clause | Offset | Length | Java Type | Business Name | Description |
|---|---|---|---|---|---|---|---|
| 1 | CUST-ID | 9(09) | 0 | 9 | `long` | Customer ID | Unique customer identifier |
| 2 | CUST-FIRST-NAME | X(25) | 9 | 25 | `String` | First Name | Customer first name |
| 3 | CUST-MIDDLE-NAME | X(25) | 34 | 25 | `String` | Middle Name | Customer middle name |
| 4 | CUST-LAST-NAME | X(25) | 59 | 25 | `String` | Last Name | Customer last name |
| 5 | CUST-ADDR-LINE-1 | X(50) | 84 | 50 | `String` | Address Line 1 | Primary address |
| 6 | CUST-ADDR-LINE-2 | X(50) | 134 | 50 | `String` | Address Line 2 | Secondary address |
| 7 | CUST-ADDR-LINE-3 | X(50) | 184 | 50 | `String` | Address Line 3 | Tertiary address |
| 8 | CUST-ADDR-STATE-CD | X(02) | 234 | 2 | `String` | State Code | US state code |
| 9 | CUST-ADDR-COUNTRY-CD | X(03) | 236 | 3 | `String` | Country Code | Country code |
| 10 | CUST-ADDR-ZIP | X(10) | 239 | 10 | `String` | ZIP Code | Postal code |
| 11 | CUST-PHONE-NUM-1 | X(15) | 249 | 15 | `String` | Phone 1 | Primary phone |
| 12 | CUST-PHONE-NUM-2 | X(15) | 264 | 15 | `String` | Phone 2 | Secondary phone |
| 13 | CUST-SSN | 9(09) | 279 | 9 | `String` | SSN | Social Security Number (PII) |
| 14 | CUST-GOVT-ISSUED-ID | X(20) | 288 | 20 | `String` | Government ID | Government-issued ID |
| 15 | CUST-DOB-YYYYMMDD | X(10) | 308 | 10 | `LocalDate` | Date of Birth | Customer DOB |
| 16 | CUST-EFT-ACCOUNT-ID | X(10) | 318 | 10 | `String` | EFT Account ID | Electronic fund transfer account |
| 17 | CUST-PRI-CARD-HOLDER-IND | X(01) | 328 | 1 | `String` | Primary Card Holder | Primary cardholder indicator (Y/N) |
| 18 | CUST-FICO-CREDIT-SCORE | 9(03) | 329 | 3 | `int` | FICO Score | Credit score |
| 19 | FILLER | X(168) | 332 | 168 | — | (reserved) | Reserved for future use |

### Business Rules
- **Key:** `CUST-ID` (9-digit numeric, VSAM KSDS primary key)
- **PII Fields:** SSN, DOB, Government ID — require encryption and access controls
- FICO score stored as 3-digit integer (300–850 range)

---

## 6. Transaction Entity (CVTRA05Y.cpy)

**VSAM File:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` · **Record Length:** 350 bytes

| # | COBOL Field | PIC Clause | Offset | Length | Java Type | Business Name | Description |
|---|---|---|---|---|---|---|---|
| 1 | TRAN-ID | X(16) | 0 | 16 | `String` | Transaction ID | Unique transaction identifier |
| 2 | TRAN-TYPE-CD | X(02) | 16 | 2 | `String` | Type Code | Transaction type (FK→TRANTYPE) |
| 3 | TRAN-CAT-CD | 9(04) | 18 | 4 | `int` | Category Code | Transaction category (FK→TRANCATG) |
| 4 | TRAN-SOURCE | X(10) | 22 | 10 | `String` | Source | Transaction source/channel |
| 5 | TRAN-DESC | X(100) | 32 | 100 | `String` | Description | Transaction description |
| 6 | TRAN-AMT | S9(09)V99 | 132 | 11 | `BigDecimal` | Amount | Transaction amount |
| 7 | TRAN-MERCHANT-ID | 9(09) | 143 | 9 | `long` | Merchant ID | Merchant identifier |
| 8 | TRAN-MERCHANT-NAME | X(50) | 152 | 50 | `String` | Merchant Name | Merchant name |
| 9 | TRAN-MERCHANT-CITY | X(50) | 202 | 50 | `String` | Merchant City | Merchant city |
| 10 | TRAN-MERCHANT-ZIP | X(10) | 252 | 10 | `String` | Merchant ZIP | Merchant postal code |
| 11 | TRAN-CARD-NUM | X(16) | 262 | 16 | `String` | Card Number | Card used (FK→CARDDATA) |
| 12 | TRAN-ORIG-TS | X(26) | 278 | 26 | `LocalDateTime` | Origination Timestamp | When transaction was initiated |
| 13 | TRAN-PROC-TS | X(26) | 304 | 26 | `LocalDateTime` | Processing Timestamp | When transaction was processed |
| 14 | FILLER | X(20) | 330 | 20 | — | (reserved) | Reserved for future use |

### Business Rules
- **Key:** `TRAN-ID` (16-character, VSAM KSDS primary key)
- **Foreign Keys:** `TRAN-TYPE-CD` → TransactionType, `TRAN-CAT-CD` → TransactionCategory, `TRAN-CARD-NUM` → Card
- Amount is signed decimal (S9(09)V99) — credits positive, debits negative
- Timestamps stored as 26-character strings (format: `YYYY-MM-DD-HH.MM.SS.FFFFFF`)

---

## 7. Daily Transaction Entity (CVTRA06Y.cpy)

**File:** `AWS.M2.CARDDEMO.DALYTRAN.PS` (sequential flat file) · **Record Length:** 350 bytes

| # | COBOL Field | PIC Clause | Length | Java Type | Business Name |
|---|---|---|---|---|---|
| 1 | DALYTRAN-ID | X(16) | 16 | `String` | Transaction ID |
| 2 | DALYTRAN-TYPE-CD | X(02) | 2 | `String` | Type Code |
| 3 | DALYTRAN-CAT-CD | 9(04) | 4 | `int` | Category Code |
| 4 | DALYTRAN-SOURCE | X(10) | 10 | `String` | Source |
| 5 | DALYTRAN-DESC | X(100) | 100 | `String` | Description |
| 6 | DALYTRAN-AMT | S9(09)V99 | 11 | `BigDecimal` | Amount |
| 7 | DALYTRAN-MERCHANT-ID | 9(09) | 9 | `long` | Merchant ID |
| 8 | DALYTRAN-MERCHANT-NAME | X(50) | 50 | `String` | Merchant Name |
| 9 | DALYTRAN-MERCHANT-CITY | X(50) | 50 | `String` | Merchant City |
| 10 | DALYTRAN-MERCHANT-ZIP | X(10) | 10 | `String` | Merchant ZIP |
| 11 | DALYTRAN-CARD-NUM | X(16) | 16 | `String` | Card Number |
| 12 | DALYTRAN-ORIG-TS | X(26) | 26 | `LocalDateTime` | Origination Timestamp |
| 13 | DALYTRAN-PROC-TS | X(26) | 26 | `LocalDateTime` | Processing Timestamp |
| 14 | FILLER | X(20) | 20 | — | (reserved) |

> **Note:** Identical structure to CVTRA05Y. This is the batch input feed that gets posted to the TRANSACT master by CBTRN02C.

---

## 8. Transaction Category Balance (CVTRA01Y.cpy)

**VSAM File:** `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS` · **Record Length:** 50 bytes

| # | COBOL Field | PIC Clause | Length | Java Type | Business Name | Description |
|---|---|---|---|---|---|---|
| 1 | TRANCAT-ACCT-ID | 9(11) | 11 | `long` | Account ID | Account reference |
| 2 | TRANCAT-TYPE-CD | X(02) | 2 | `String` | Type Code | Transaction type |
| 3 | TRANCAT-CD | 9(04) | 4 | `int` | Category Code | Category code |
| 4 | TRAN-CAT-BAL | S9(09)V99 | 11 | `BigDecimal` | Category Balance | Running balance for this category |
| 5 | FILLER | X(22) | 22 | — | (reserved) | Reserved |

### Business Rules
- **Composite Key:** Account ID + Type Code + Category Code
- Tracks balance per transaction category per account (used for interest calculation)

---

## 9. Disclosure Group (CVTRA02Y.cpy)

**VSAM File:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS` · **Record Length:** 50 bytes

| # | COBOL Field | PIC Clause | Length | Java Type | Business Name | Description |
|---|---|---|---|---|---|---|
| 1 | DIS-ACCT-GROUP-ID | X(10) | 10 | `String` | Account Group ID | Disclosure group identifier |
| 2 | DIS-TRAN-TYPE-CD | X(02) | 2 | `String` | Type Code | Transaction type |
| 3 | DIS-TRAN-CAT-CD | 9(04) | 4 | `int` | Category Code | Category code |
| 4 | DIS-INT-RATE | S9(04)V99 | 6 | `BigDecimal` | Interest Rate | Interest rate for this group/type/category |
| 5 | FILLER | X(28) | 28 | — | (reserved) | Reserved |

### Business Rules
- **Composite Key:** Group ID + Type Code + Category Code
- Links account groups to interest rates by transaction category
- Used by CBACT04C (interest calculation batch)

---

## 10. Transaction Type (CVTRA03Y.cpy)

**VSAM File:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS` · **Record Length:** 60 bytes

| # | COBOL Field | PIC Clause | Length | Java Type | Business Name | Description |
|---|---|---|---|---|---|---|
| 1 | TRAN-TYPE | X(02) | 2 | `String` | Type Code | Transaction type code (e.g., "01", "02") |
| 2 | TRAN-TYPE-DESC | X(50) | 50 | `String` | Type Description | Human-readable type name |
| 3 | FILLER | X(08) | 8 | — | (reserved) | Reserved |

### Business Rules
- **Key:** `TRAN-TYPE` (2-character code)
- Reference/lookup table — maps codes to descriptions

---

## 11. Transaction Category (CVTRA04Y.cpy)

**VSAM File:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS` · **Record Length:** 60 bytes

| # | COBOL Field | PIC Clause | Length | Java Type | Business Name | Description |
|---|---|---|---|---|---|---|
| 1 | TRAN-TYPE-CD | X(02) | 2 | `String` | Type Code | Parent transaction type |
| 2 | TRAN-CAT-CD | 9(04) | 4 | `int` | Category Code | Category code |
| 3 | TRAN-CAT-TYPE-DESC | X(50) | 50 | `String` | Category Description | Human-readable category name |
| 4 | FILLER | X(04) | 4 | — | (reserved) | Reserved |

### Business Rules
- **Composite Key:** Type Code + Category Code
- Child of Transaction Type (hierarchical: Type → Category)

---

## 12. User Security (CSUSR01Y.cpy)

**VSAM File:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS` · **Record Length:** 80 bytes

| # | COBOL Field | PIC Clause | Length | Java Type | Business Name | Description |
|---|---|---|---|---|---|---|
| 1 | SEC-USR-ID | X(08) | 8 | `String` | User ID | Login user identifier |
| 2 | SEC-USR-FNAME | X(20) | 20 | `String` | First Name | User first name |
| 3 | SEC-USR-LNAME | X(20) | 20 | `String` | Last Name | User last name |
| 4 | SEC-USR-PWD | X(08) | 8 | `String` | Password | User password (plaintext!) |
| 5 | SEC-USR-TYPE | X(01) | 1 | `String` | User Type | 'A' = Admin, 'U' = Regular User |
| 6 | SEC-USR-FILLER | X(23) | 23 | — | (reserved) | Reserved |

### Business Rules
- **Key:** `SEC-USR-ID` (8-character)
- **CRITICAL SECURITY NOTE:** Password stored in plaintext — must be hashed (BCrypt/Argon2) in Java
- User type determines menu access: Admin gets COADM01C, Regular gets COMEN01C

---

## 13. COMMAREA — Inter-Program Communication (COCOM01Y.cpy)

This is not a file record but the in-memory data area passed between CICS programs via XCTL/LINK.

| # | COBOL Field | PIC Clause | Length | Java Type | Business Name |
|---|---|---|---|---|---|
| 1 | CDEMO-FROM-TRANID | X(04) | 4 | `String` | Source Transaction ID |
| 2 | CDEMO-FROM-PROGRAM | X(08) | 8 | `String` | Source Program |
| 3 | CDEMO-TO-TRANID | X(04) | 4 | `String` | Target Transaction ID |
| 4 | CDEMO-TO-PROGRAM | X(08) | 8 | `String` | Target Program |
| 5 | CDEMO-USER-ID | X(08) | 8 | `String` | Logged-in User ID |
| 6 | CDEMO-USER-TYPE | X(01) | 1 | `String` | User Type (A/U) |
| 7 | CDEMO-PGM-CONTEXT | 9(01) | 1 | `int` | Program Context (0=enter, 1=reenter) |
| 8 | CDEMO-CUST-ID | 9(09) | 9 | `long` | Current Customer ID |
| 9 | CDEMO-CUST-FNAME | X(25) | 25 | `String` | Customer First Name |
| 10 | CDEMO-CUST-MNAME | X(25) | 25 | `String` | Customer Middle Name |
| 11 | CDEMO-CUST-LNAME | X(25) | 25 | `String` | Customer Last Name |
| 12 | CDEMO-ACCT-ID | 9(11) | 11 | `long` | Current Account ID |
| 13 | CDEMO-ACCT-STATUS | X(01) | 1 | `String` | Account Status |
| 14 | CDEMO-CARD-NUM | 9(16) | 16 | `String` | Current Card Number |
| 15 | CDEMO-LAST-MAP | X(7) | 7 | `String` | Last BMS Map |
| 16 | CDEMO-LAST-MAPSET | X(7) | 7 | `String` | Last BMS Mapset |

> **Java Target:** Spring `@SessionScope` bean or HTTP session attributes.

---

## 14. Statement Transaction (COSTM01.CPY)

**VSAM File:** `AWS.M2.CARDDEMO.TRXFL.VSAM.KSDS` · **Record Length:** 350 bytes
This is a re-keyed version of the Transaction record with a compound key for statement generation.

| # | COBOL Field | PIC Clause | Length | Java Type | Business Name |
|---|---|---|---|---|---|
| 1 | TRNX-CARD-NUM | X(16) | 16 | `String` | Card Number (key part 1) |
| 2 | TRNX-ID | X(16) | 16 | `String` | Transaction ID (key part 2) |
| 3 | TRNX-TYPE-CD | X(02) | 2 | `String` | Type Code |
| 4 | TRNX-CAT-CD | 9(04) | 4 | `int` | Category Code |
| 5 | TRNX-SOURCE | X(10) | 10 | `String` | Source |
| 6 | TRNX-DESC | X(100) | 100 | `String` | Description |
| 7 | TRNX-AMT | S9(09)V99 | 11 | `BigDecimal` | Amount |
| 8 | TRNX-MERCHANT-ID | 9(09) | 9 | `long` | Merchant ID |
| 9 | TRNX-MERCHANT-NAME | X(50) | 50 | `String` | Merchant Name |
| 10 | TRNX-MERCHANT-CITY | X(50) | 50 | `String` | Merchant City |
| 11 | TRNX-MERCHANT-ZIP | X(10) | 10 | `String` | Merchant ZIP |
| 12 | TRNX-ORIG-TS | X(26) | 26 | `LocalDateTime` | Origination Timestamp |
| 13 | TRNX-PROC-TS | X(26) | 26 | `LocalDateTime` | Processing Timestamp |
| 14 | FILLER | X(20) | 20 | — | (reserved) |

> **Key:** Compound key = Card Number (16) + Transaction ID (16) = 32 bytes. Sorted by card for statement grouping.

---

## 15. Export Record (CVEXPORT.cpy)

**File:** `AWS.M2.CARDDEMO.EXPORT.DATA` (sequential flat file)

| # | COBOL Field | PIC Clause | Length | Java Type | Business Name |
|---|---|---|---|---|---|
| 1 | EXP-REC-TYPE | X(01) | 1 | `String` | Record Type (C=Cust, A=Acct, X=Xref, T=Tran, R=Card) |
| 2 | EXP-CUST-* | (embedded) | varies | — | Customer fields (when type=C) |
| 3 | EXP-ACCT-* | (embedded) | varies | — | Account fields (when type=A) |
| 4 | EXP-CARD-* | (embedded) | varies | — | Card fields (when type=R) |

> This is a multi-record-type flat file. The first byte indicates the record type, and subsequent fields are interpreted based on type. In Java, use a discriminator pattern or separate DTOs per type.

---

## 16. Date Conversion (CODATECN.cpy)

Utility copybook for date format conversion between `YYYYMMDD` and `YYYY-MM-DD`.

| # | COBOL Field | PIC Clause | Purpose |
|---|---|---|---|
| 1 | CODATECN-TYPE | X | Input format indicator: "1" = YYYYMMDD, "2" = YYYY-MM-DD |
| 2 | CODATECN-INP-DATE | X(20) | Input date string |
| 3 | CODATECN-OUTTYPE | X | Output format indicator |
| 4 | CODATECN-0UT-DATE | X(20) | Output date string |
| 5 | CODATECN-ERROR-MSG | X(38) | Error message if conversion fails |

> **Java Target:** `java.time.LocalDate` with `DateTimeFormatter` — this entire copybook becomes unnecessary.

---

## 17. Entity Relationship Diagram (Logical)

```
┌─────────────┐     1:N     ┌──────────────┐     1:N     ┌─────────────┐
│  Customer   │────────────→│  Card Xref   │────────────→│   Account   │
│ (CVCUS01Y)  │             │ (CVACT03Y)   │             │ (CVACT01Y)  │
│ PK: CUST-ID │             │ PK: CARD-NUM │             │ PK: ACCT-ID │
└─────────────┘             │ FK: CUST-ID  │             └──────┬──────┘
                            │ FK: ACCT-ID  │                    │
                            └──────┬───────┘                    │
                                   │                            │
                              1:1  │                       1:N  │
                                   ▼                            │
                            ┌──────────────┐                    │
                            │    Card      │                    │
                            │ (CVACT02Y)   │                    │
                            │ PK: CARD-NUM │                    │
                            │ FK: ACCT-ID  │                    │
                            └──────┬───────┘                    │
                                   │                            │
                              1:N  │                            │
                                   ▼                            │
                            ┌──────────────┐              ┌─────┴────────┐
                            │ Transaction  │              │  Tran Cat    │
                            │ (CVTRA05Y)   │              │  Balance     │
                            │ PK: TRAN-ID  │              │ (CVTRA01Y)   │
                            │ FK: CARD-NUM │              │ CK: ACCT+    │
                            │ FK: TYPE-CD  │              │   TYPE+CAT   │
                            │ FK: CAT-CD   │              └──────────────┘
                            └──────┬───────┘
                                   │
                         FK    ┌───┴───┐    FK
                               │       │
                               ▼       ▼
                    ┌──────────────┐  ┌──────────────┐
                    │ Tran Type    │  │ Tran Category│
                    │ (CVTRA03Y)   │  │ (CVTRA04Y)   │
                    │ PK: TYPE     │  │ CK: TYPE+CAT │
                    └──────────────┘  └──────────────┘
                               │
                          1:N  │
                               ▼
                    ┌──────────────┐      ┌──────────────┐
                    │ Disclosure   │      │ User Security│
                    │ Group        │      │ (CSUSR01Y)   │
                    │ (CVTRA02Y)   │      │ PK: USR-ID   │
                    │ CK: GRP+     │      └──────────────┘
                    │   TYPE+CAT   │
                    └──────────────┘
```

---

## 18. PIC Clause to Java Type Mapping Guide

| COBOL PIC Pattern | Example | Java Type | Notes |
|---|---|---|---|
| `9(n)` | `PIC 9(11)` | `long` / `int` | Unsigned integer; use `long` if >9 digits |
| `S9(n)V99` | `PIC S9(09)V99` | `BigDecimal` | Signed decimal with 2 implied decimal places |
| `S9(n)V9(m)` | `PIC S9(04)V99` | `BigDecimal` | Signed decimal with m decimal places |
| `X(n)` | `PIC X(50)` | `String` | Alphanumeric; trim trailing spaces |
| `X(10)` (date) | Date fields | `LocalDate` | Parse from `YYYY-MM-DD` format |
| `X(26)` (timestamp) | Timestamp fields | `LocalDateTime` | Parse from `YYYY-MM-DD-HH.MM.SS.FFFFFF` |
| `9(03) COMP` | `PIC 9(03) COMP` | `int` | Binary/computational integer |
| `S9(4) BINARY` | `PIC S9(4) BINARY` | `short` | Signed binary half-word |

---

## 19. Sensitive Data Fields (PCI / PII)

| Entity | Field | Data Type | Sensitivity | Java Requirement |
|---|---|---|---|---|
| Card | CARD-NUM | X(16) | **PCI-DSS** | Tokenize or encrypt at rest |
| Card | CARD-CVV-CD | 9(03) | **PCI-DSS** | Never store; use tokenization |
| Customer | CUST-SSN | 9(09) | **PII** | Encrypt at rest, mask in logs |
| Customer | CUST-DOB-YYYYMMDD | X(10) | **PII** | Access-controlled |
| Customer | CUST-GOVT-ISSUED-ID | X(20) | **PII** | Encrypt at rest |
| User Security | SEC-USR-PWD | X(08) | **Credential** | Hash with BCrypt/Argon2 |
