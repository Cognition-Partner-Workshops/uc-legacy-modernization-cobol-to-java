# CardDemo Data Dictionary

> **Source:** Copybook PIC clauses from `app/cpy/` and `app/cpy-bms/`
> **Application:** AWS CardDemo -- Credit Card Management System
> **Storage:** VSAM KSDS (Key-Sequenced Data Sets)

---

## 1. Account Master (`CVACT01Y.cpy`)

**VSAM File:** ACCTFILE | **Record Length:** 300 bytes | **Key:** ACCT-ID (11 digits)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Account ID | `ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Unique account identifier |
| Active Status | `ACCT-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 | Account status (Y/N) |
| Current Balance | `ACCT-CURR-BAL` | `PIC S9(10)V99` | Signed Decimal | 12 | Current account balance |
| Credit Limit | `ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | Signed Decimal | 12 | Maximum credit limit |
| Cash Advance Limit | `ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | Signed Decimal | 12 | Cash advance credit limit |
| Open Date | `ACCT-OPEN-DATE` | `PIC X(10)` | Date String | 10 | Account opening date |
| Expiration Date | `ACCT-EXPIRAION-DATE` | `PIC X(10)` | Date String | 10 | Account expiration date |
| Reissue Date | `ACCT-REISSUE-DATE` | `PIC X(10)` | Date String | 10 | Last card reissue date |
| Current Cycle Credit | `ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | Signed Decimal | 12 | Current cycle credits |
| Current Cycle Debit | `ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | Signed Decimal | 12 | Current cycle debits |
| Account Group ID | `ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 | Disclosure/interest group |
| Filler | `FILLER` | `PIC X(178)` | -- | 178 | Reserved space |

**Business Rules:**
- Primary entity for credit card account management
- Linked to customers via Card Cross-Reference (CVACT03Y)
- Balance updated by transaction posting (CBTRN02C) and interest calculation (CBACT04C)
- Group ID determines interest rate via Disclosure Group lookup

---

## 2. Card Master (`CVACT02Y.cpy`)

**VSAM File:** CARDFILE | **Record Length:** 150 bytes | **Key:** CARD-NUM (16 digits)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Card Number | `CARD-NUM` | `PIC X(16)` | Alphanumeric | 16 | 16-digit credit card number |
| Account ID | `CARD-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | FK to Account Master |
| CVV Code | `CARD-CVV-CD` | `PIC 9(03)` | Numeric | 3 | Card verification value |
| Embossed Name | `CARD-EMBOSSED-NAME` | `PIC X(50)` | Alpha | 50 | Name embossed on physical card |
| Expiration Date | `CARD-EXPIRAION-DATE` | `PIC X(10)` | Date String | 10 | Card expiration (YYYY-MM-DD) |
| Active Status | `CARD-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 | Card active status (Y/N) |
| Filler | `FILLER` | `PIC X(59)` | -- | 59 | Reserved space |

**Business Rules:**
- One account can have multiple cards
- Card number is the primary key for card-level operations
- Card status independently tracked from account status
- CVV stored for verification (security-sensitive)

---

## 3. Card Cross-Reference (`CVACT03Y.cpy`)

**VSAM File:** CARDXREF | **Record Length:** 50 bytes | **Key:** XREF-CARD-NUM (16 digits)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Card Number | `XREF-CARD-NUM` | `PIC X(16)` | Alphanumeric | 16 | Card number (FK to Card Master) |
| Account ID | `XREF-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Account ID (FK to Account Master) |
| Customer ID | `XREF-CUST-ID` | `PIC 9(09)` | Numeric | 9 | Customer ID (FK to Customer Master) |
| Filler | `FILLER` | `PIC X(14)` | -- | 14 | Reserved space |

**Business Rules:**
- Central lookup table connecting Card -> Account -> Customer
- Used by virtually all online and batch programs for navigation
- Critical path: given a card number, find the account and customer

---

## 4. Customer Master (`CVCUS01Y.cpy`)

**VSAM File:** CUSTFILE | **Record Length:** 500 bytes | **Key:** CUST-ID (9 digits)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Customer ID | `CUST-ID` | `PIC 9(09)` | Numeric | 9 | Unique customer identifier |
| First Name | `CUST-FIRST-NAME` | `PIC X(25)` | Alpha | 25 | Customer first name |
| Middle Name | `CUST-MIDDLE-NAME` | `PIC X(25)` | Alpha | 25 | Customer middle name |
| Last Name | `CUST-LAST-NAME` | `PIC X(25)` | Alpha | 25 | Customer last name |
| Address Line 1 | `CUST-ADDR-LINE-1` | `PIC X(50)` | Alpha | 50 | Street address line 1 |
| Address Line 2 | `CUST-ADDR-LINE-2` | `PIC X(50)` | Alpha | 50 | Street address line 2 |
| Address Line 3 | `CUST-ADDR-LINE-3` | `PIC X(50)` | Alpha | 50 | Street address line 3 |
| State Code | `CUST-ADDR-STATE-CD` | `PIC X(02)` | Alpha | 2 | US state code (validated) |
| Country Code | `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | Alpha | 3 | Country code |
| Zip Code | `CUST-ADDR-ZIP` | `PIC X(10)` | Alpha | 10 | US ZIP code (ZIP+4) |
| Phone Home | `CUST-PHONE-NUM-1` | `PIC X(15)` | Alpha | 15 | Home phone number |
| Phone Work | `CUST-PHONE-NUM-2` | `PIC X(15)` | Alpha | 15 | Work phone number |
| SSN | `CUST-SSN` | `PIC 9(09)` | Numeric | 9 | Social Security Number |
| Gov ID | `CUST-GOVT-ISSUED-ID` | `PIC X(20)` | Alpha | 20 | Government-issued ID |
| Date of Birth | `CUST-DOB-YYYYMMDD` | `PIC X(10)` | Date String | 10 | Date of birth |
| EFT Account ID | `CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | Alpha | 10 | EFT/Direct deposit account |
| PII Nat'l ID | `CUST-PRI-CARD-HOLDER-IND` | `PIC X(01)` | Alpha | 1 | Primary card holder indicator |
| FICO Score | `CUST-FICO-CREDIT-SCORE` | `PIC 9(03)` | Numeric | 3 | FICO credit score (300-850) |
| Filler | `FILLER` | `PIC X(168)` | -- | 168 | Reserved space |

**Business Rules:**
- Contains PII (SSN, DOB, address) -- security-critical
- Linked to accounts via Cross-Reference file
- One customer can have multiple accounts
- FICO score used for credit decisions
- State code validated against CSLKPCDY lookup table
- Phone area codes validated against NANPA list

---

## 5. Transaction Master (`CVTRA05Y.cpy`)

**VSAM File:** TRANSACT | **Record Length:** 350 bytes | **Key:** TRAN-ID (16 chars)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Transaction ID | `TRAN-ID` | `PIC X(16)` | Alphanumeric | 16 | Unique transaction identifier |
| Transaction Type | `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Type code (FK to TRAN-TYPE) |
| Category Code | `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Category code (FK to TRAN-CAT) |
| Source | `TRAN-SOURCE` | `PIC X(10)` | Alpha | 10 | Transaction source system |
| Description | `TRAN-DESC` | `PIC X(100)` | Alpha | 100 | Transaction description |
| Amount | `TRAN-AMT` | `PIC S9(09)V99` | Signed Decimal | 11 | Transaction amount |
| Merchant ID | `TRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant identifier |
| Merchant Name | `TRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant name |
| Merchant City | `TRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant city |
| Merchant ZIP | `TRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP code |
| Card Number | `TRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card used (FK to Card Master) |
| Origination Timestamp | `TRAN-ORIG-TS` | `PIC X(26)` | Timestamp | 26 | When transaction originated |
| Processing Timestamp | `TRAN-PROC-TS` | `PIC X(26)` | Timestamp | 26 | When transaction was processed |
| Filler | `FILLER` | `PIC X(20)` | -- | 20 | Reserved space |

**Business Rules:**
- Core transaction record for all credit card activity
- Populated by daily posting batch (CBTRN02C)
- Referenced by reports, statements, and online views
- Amount is signed (positive = charge, negative = credit/payment)
- Links to card via TRAN-CARD-NUM, then to account via cross-ref

---

## 6. Daily Transaction (`CVTRA06Y.cpy`)

**VSAM File:** DALYTRAN | **Record Length:** 350 bytes | **Key:** DALYTRAN-ID

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Transaction ID | `DALYTRAN-ID` | `PIC X(16)` | Alphanumeric | 16 | Daily transaction identifier |
| Type Code | `DALYTRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type code |
| Category Code | `DALYTRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category code |
| Source | `DALYTRAN-SOURCE` | `PIC X(10)` | Alpha | 10 | Transaction source |
| Description | `DALYTRAN-DESC` | `PIC X(100)` | Alpha | 100 | Transaction description |
| Amount | `DALYTRAN-AMT` | `PIC S9(09)V99` | Signed Decimal | 11 | Transaction amount |
| Merchant ID | `DALYTRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant ID |
| Merchant Name | `DALYTRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant name |
| Merchant City | `DALYTRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant city |
| Merchant ZIP | `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP code |
| Card Number | `DALYTRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card number used |
| Origination TS | `DALYTRAN-ORIG-TS` | `PIC X(26)` | Timestamp | 26 | Origination timestamp |
| Processing TS | `DALYTRAN-PROC-TS` | `PIC X(26)` | Timestamp | 26 | Processing timestamp |
| Filler | `FILLER` | `PIC X(20)` | -- | 20 | Reserved |

**Business Rules:**
- Staging area for incoming daily transactions
- Validated by CBTRN01C, posted to TRANSACT by CBTRN02C
- Invalid transactions written to rejection file (DALYREJS)
- Identical structure to Transaction Master for seamless posting

---

## 7. Transaction Category Balance (`CVTRA01Y.cpy`)

**VSAM File:** TCATBALF | **Record Length:** 50 bytes | **Compound Key:** ACCT-ID + TYPE-CD + CAT-CD

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Account ID | `TRANCAT-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Account identifier |
| Type Code | `TRANCAT-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type |
| Category Code | `TRANCAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category |
| Category Balance | `TRAN-CAT-BAL` | `PIC S9(09)V99` | Signed Decimal | 11 | Running balance for this category |
| Filler | `FILLER` | `PIC X(22)` | -- | 22 | Reserved |

**Business Rules:**
- Tracks running balance per account per transaction type/category
- Updated during transaction posting (CBTRN02C)
- Used by interest calculation (CBACT04C) to determine rate tiers
- Enables category-level balance inquiries

---

## 8. Disclosure Group / Interest Rate (`CVTRA02Y.cpy`)

**VSAM File:** DISCGRP | **Record Length:** 50 bytes | **Compound Key:** GROUP-ID + TRAN-TYPE + CAT-CD

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Account Group ID | `DIS-ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 | Account group identifier |
| Transaction Type | `DIS-TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type code |
| Category Code | `DIS-TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category |
| Interest Rate | `DIS-INT-RATE` | `PIC S9(04)V99` | Signed Decimal | 6 | Interest rate (%) |
| Filler | `FILLER` | `PIC X(28)` | -- | 28 | Reserved |

**Business Rules:**
- Reference table for interest rate determination
- Linked to accounts via ACCT-GROUP-ID
- Different rates per transaction type/category (purchases vs cash advances)
- Used exclusively by CBACT04C (interest calculation)

---

## 9. Transaction Type (`CVTRA03Y.cpy`)

**VSAM File:** TRANTYPE | **Record Length:** 60 bytes | **Key:** TRAN-TYPE (2 chars)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Type Code | `TRAN-TYPE` | `PIC X(02)` | Alpha | 2 | Transaction type code |
| Description | `TRAN-TYPE-DESC` | `PIC X(50)` | Alpha | 50 | Type description |
| Filler | `FILLER` | `PIC X(08)` | -- | 8 | Reserved |

**Business Rules:**
- Code table for transaction types (e.g., "01" = Purchase, "02" = Cash Advance)
- Referenced by reports (CBTRN03C) for human-readable descriptions
- Managed via optional DB2 module (COTRTUPC/COTRTLIC)

---

## 10. Transaction Category (`CVTRA04Y.cpy`)

**VSAM File:** TRANCATG | **Record Length:** 60 bytes | **Compound Key:** TYPE-CD + CAT-CD

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Type Code | `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Parent transaction type |
| Category Code | `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Category code |
| Description | `TRAN-CAT-TYPE-DESC` | `PIC X(50)` | Alpha | 50 | Category description |
| Filler | `FILLER` | `PIC X(04)` | -- | 4 | Reserved |

**Business Rules:**
- Sub-classification of transaction types
- Hierarchical: Type -> Category
- Used for reporting granularity and interest rate tiering

---

## 11. User Security Record (`CSUSR01Y.cpy`)

**VSAM File:** USRSEC | **Key:** SEC-USR-ID (8 chars)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| User ID | `SEC-USR-ID` | `PIC X(08)` | Alpha | 8 | Login user ID |
| First Name | `SEC-USR-FNAME` | `PIC X(20)` | Alpha | 20 | User first name |
| Last Name | `SEC-USR-LNAME` | `PIC X(20)` | Alpha | 20 | User last name |
| Password | `SEC-USR-PWD` | `PIC X(08)` | Alpha | 8 | User password (plaintext!) |
| User Type | `SEC-USR-TYPE` | `PIC X(01)` | Alpha | 1 | 'A' = Admin, 'U' = Regular |
| Filler | `SEC-USR-FILLER` | `PIC X(23)` | -- | 23 | Reserved |

**Business Rules:**
- Authentication and authorization for CICS online sessions
- Two roles: Admin (full access) and User (card operations only)
- Default accounts: ADMIN001/PASSWORD, USER0001/PASSWORD
- **Security Risk:** Passwords stored in plaintext

---

## 12. Communication Area (`COCOM01Y.cpy`)

**Type:** CICS COMMAREA (in-memory, passed between programs)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| From Transaction | `CDEMO-FROM-TRANID` | `PIC X(04)` | Alpha | 4 | Calling transaction ID |
| From Program | `CDEMO-FROM-PROGRAM` | `PIC X(08)` | Alpha | 8 | Calling program name |
| To Transaction | `CDEMO-TO-TRANID` | `PIC X(04)` | Alpha | 4 | Target transaction ID |
| To Program | `CDEMO-TO-PROGRAM` | `PIC X(08)` | Alpha | 8 | Target program name |
| User ID | `CDEMO-USER-ID` | `PIC X(08)` | Alpha | 8 | Logged-in user ID |
| User Type | `CDEMO-USER-TYPE` | `PIC X(01)` | Alpha | 1 | User type (A/U) |
| Program Context | `CDEMO-PGM-CONTEXT` | `PIC 9(01)` | Numeric | 1 | 0=Enter, 1=Re-enter |
| Customer ID | `CDEMO-CUST-ID` | `PIC 9(09)` | Numeric | 9 | Current customer context |
| Customer Name | `CDEMO-CUST-FNAME/MNAME/LNAME` | `PIC X(25)` each | Alpha | 75 | Customer name fields |
| Account ID | `CDEMO-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | Current account context |
| Account Status | `CDEMO-ACCT-STATUS` | `PIC X(01)` | Alpha | 1 | Current account status |
| Card Number | `CDEMO-CARD-NUM` | `PIC 9(16)` | Numeric | 16 | Current card context |
| Last Map | `CDEMO-LAST-MAP` | `PIC X(7)` | Alpha | 7 | Last BMS map sent |
| Last Mapset | `CDEMO-LAST-MAPSET` | `PIC X(7)` | Alpha | 7 | Last BMS mapset sent |

**Business Rules:**
- Session state carrier for all online CICS programs
- Passed via EXEC CICS XCTL and EXEC CICS RETURN TRANSID
- Contains navigation context (from/to program) and business context (customer/account/card)
- User type controls menu visibility and operation authorization

---

## 13. Statement Transaction Layout (`COSTM01.CPY`)

**Used By:** CBSTM03A (Statement generation)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Card Number | `TRNX-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card number (sort key) |
| Transaction ID | `TRNX-ID` | `PIC X(16)` | Alpha | 16 | Transaction ID |
| Type Code | `TRNX-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type |
| Category Code | `TRNX-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Category code |
| Source | `TRNX-SOURCE` | `PIC X(10)` | Alpha | 10 | Transaction source |
| Description | `TRNX-DESC` | `PIC X(100)` | Alpha | 100 | Description |
| Amount | `TRNX-AMT` | `PIC S9(09)V99` | Signed Decimal | 11 | Amount |
| Merchant ID | `TRNX-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant ID |
| Merchant Name | `TRNX-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant name |
| Merchant City | `TRNX-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant city |
| Merchant ZIP | `TRNX-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP |
| Origination TS | `TRNX-ORIG-TS` | `PIC X(26)` | Timestamp | 26 | Origination timestamp |
| Processing TS | `TRNX-PROC-TS` | `PIC X(26)` | Timestamp | 26 | Processing timestamp |

**Business Rules:**
- Re-keyed transaction layout with card number as primary sort key
- Enables statement generation grouped by card number
- Used only by statement batch (CBSTM03A/B)

---

## 14. Date/Time Working Storage (`CSDAT01Y.cpy`)

**Type:** Working storage for date/time formatting

| Field | COBOL Name | PIC Clause | Business Description |
|-------|-----------|------------|----------------------|
| Current Date | `WS-CURDATE` | `PIC 9(08)` | YYYYMMDD format |
| Current Time | `WS-CURTIME` | `PIC 9(08)` | HHMMSSCC format |
| Display Date | `WS-CURDATE-MM-DD-YY` | `MM/DD/YY` | Formatted for screens |
| Display Time | `WS-CURTIME-HH-MM-SS` | `HH:MM:SS` | Formatted for screens |
| DB2 Timestamp | `WS-TIMESTAMP` | `YYYY-MM-DD HH:MM:SS.FFFFFF` | ISO timestamp format |

---

## 15. Date Conversion (`CODATECN.cpy`)

**Type:** Parameter area for date format conversion (used with COBDATFT assembler routine)

| Field | COBOL Name | PIC Clause | Business Description |
|-------|-----------|------------|----------------------|
| Input Type | `CODATECN-TYPE` | `PIC X` | "1"=YYYYMMDD, "2"=YYYY-MM-DD |
| Input Date | `CODATECN-INP-DATE` | `PIC X(20)` | Date to convert |
| Output Type | `CODATECN-OUTTYPE` | `PIC X` | "1"=YYYY-MM-DD, "2"=YYYYMMDD |
| Output Date | `CODATECN-0UT-DATE` | `PIC X(20)` | Converted date |
| Error Message | `CODATECN-ERROR-MSG` | `PIC X(38)` | Conversion error message |

---

## 16. Export Record (`CVEXPORT.cpy`)

**Type:** Multi-entity export format for data migration

| Field | COBOL Name | PIC Clause | Business Description |
|-------|-----------|------------|----------------------|
| Record Type | `EXP-REC-TYPE` | `PIC X(01)` | C=Customer, A=Account, X=Xref, T=Tran, R=Card |
| Timestamp | `EXP-TIMESTAMP` | `PIC X(26)` | Export timestamp |
| (varies by type) | -- | -- | Entity-specific fields follow |

**Business Rules:**
- Unified export format for all entity types
- Used by CBEXPORT (export) and CBIMPORT (import)
- Record type discriminator at position 1
- Supports full data migration between environments

---

## Entity Relationship Summary

```
CUSTOMER (CVCUS01Y)          1 --- * CARD-XREF (CVACT03Y)
    |                                    |
    |  CUST-ID                    XREF-CARD-NUM
    |                                    |
    +--- * ACCOUNT (CVACT01Y)     CARD (CVACT02Y) * --- 1 ACCOUNT
              |                          |
              |  ACCT-ID          CARD-NUM
              |                          |
              +--- * TRAN-CAT-BAL        +--- * TRANSACTION (CVTRA05Y)
              |      (CVTRA01Y)                    |
              |                             TRAN-TYPE-CD + TRAN-CAT-CD
              |                                    |
              +--- DISC-GROUP              TRAN-TYPE (CVTRA03Y)
                   (CVTRA02Y)              TRAN-CAT (CVTRA04Y)

USER-SECURITY (CSUSR01Y) -- standalone authentication table

DAILY-TRAN (CVTRA06Y) --[posting]--> TRANSACTION (CVTRA05Y)
```

---

## VSAM File Summary

| VSAM File | Copybook | Record Size | Key | Key Size | Access Method |
|-----------|----------|------------:|-----|------:|---------------|
| ACCTFILE | CVACT01Y | 300 | ACCT-ID | 11 | KSDS |
| CARDFILE | CVACT02Y | 150 | CARD-NUM | 16 | KSDS |
| CARDXREF | CVACT03Y | 50 | XREF-CARD-NUM | 16 | KSDS |
| CUSTFILE | CVCUS01Y | 500 | CUST-ID | 9 | KSDS |
| TRANSACT | CVTRA05Y | 350 | TRAN-ID | 16 | KSDS |
| DALYTRAN | CVTRA06Y | 350 | DALYTRAN-ID | 16 | KSDS |
| TCATBALF | CVTRA01Y | 50 | ACCT+TYPE+CAT | 17 | KSDS |
| DISCGRP | CVTRA02Y | 50 | GRP+TYPE+CAT | 16 | KSDS |
| TRANTYPE | CVTRA03Y | 60 | TRAN-TYPE | 2 | KSDS |
| TRANCATG | CVTRA04Y | 60 | TYPE+CAT | 6 | KSDS |
| USRSEC | CSUSR01Y | 80 | SEC-USR-ID | 8 | KSDS |

**Total estimated storage per record cycle:** ~2,000 bytes across all entities
