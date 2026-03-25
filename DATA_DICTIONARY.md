# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** `app/cpy/*.cpy` copybook PIC clause analysis
> **Notation:** `PIC X(n)` = alphanumeric n bytes, `PIC 9(n)` = numeric n digits, `PIC S9(n)V99` = signed decimal with 2 implied decimals, `COMP` = binary

---

## 1. Account Master (`CVACT01Y.cpy`)

**VSAM Dataset:** `ACCTDATA.VSAM.KSDS` | **Record Length:** 300 bytes | **Key:** Account ID (11 digits)

| # | Business Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|---|---------------|------------|------------|------|-----:|----------------------|
| 1 | Account ID | ACCT-ID | PIC 9(11) | Numeric | 11 | Unique account identifier |
| 2 | Active Status | ACCT-ACTIVE-STATUS | PIC X(01) | Alpha | 1 | Account status (Y=Active, N=Inactive) |
| 3 | Current Balance | ACCT-CURR-BAL | PIC S9(10)V99 | Decimal | 12 | Current account balance (dollars.cents) |
| 4 | Credit Limit | ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Decimal | 12 | Maximum credit allowed |
| 5 | Cash Credit Limit | ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 | Decimal | 12 | Maximum cash advance allowed |
| 6 | Open Date | ACCT-OPEN-DATE | PIC X(10) | Date | 10 | Date account was opened (YYYY-MM-DD) |
| 7 | Expiration Date | ACCT-EXPIRAION-DATE | PIC X(10) | Date | 10 | Account expiration date |
| 8 | Reissue Date | ACCT-REISSUE-DATE | PIC X(10) | Date | 10 | Date of last card reissue |
| 9 | Current Cycle Credit | ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Decimal | 12 | Credits in current billing cycle |
| 10 | Current Cycle Debit | ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 | Decimal | 12 | Debits in current billing cycle |
| 11 | Account Group ID | ACCT-GROUP-ID | PIC X(10) | Alpha | 10 | Disclosure/interest group classification |
| 12 | Filler | FILLER | PIC X(178) | -- | 178 | Reserved space |

**Business Rules:**
- Primary entity for credit card account management
- Balance = debits - credits over lifetime of account
- Credit limit checked during transaction posting (CBTRN02C)
- Interest calculated by group (CBACT04C uses ACCT-GROUP-ID to look up rates)

---

## 2. Card Master (`CVACT02Y.cpy`)

**VSAM Dataset:** `CARDDATA.VSAM.KSDS` | **Record Length:** 150 bytes | **Key:** Card Number (16 digits)

| # | Business Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|---|---------------|------------|------------|------|-----:|----------------------|
| 1 | Card Number | CARD-NUM | PIC X(16) | Alpha | 16 | 16-digit credit card number (PAN) |
| 2 | Account ID | CARD-ACCT-ID | PIC 9(11) | Numeric | 11 | Parent account for this card |
| 3 | CVV Code | CARD-CVV-CD | PIC 9(03) | Numeric | 3 | Card verification value |
| 4 | Embossed Name | CARD-EMBOSSED-NAME | PIC X(50) | Alpha | 50 | Name printed on card |
| 5 | Expiration Date | CARD-EXPIRAION-DATE | PIC X(10) | Date | 10 | Card expiry (YYYY-MM-DD) |
| 6 | Active Status | CARD-ACTIVE-STATUS | PIC X(01) | Alpha | 1 | Card status (Y=Active, N=Inactive) |
| 7 | Filler | FILLER | PIC X(59) | -- | 59 | Reserved space |

**Business Rules:**
- Multiple cards can belong to one account (1:N relationship via CARD-ACCT-ID)
- Card number is the primary lookup key for transactions
- Card status must be active for transactions to post
- An alternate index (CARDAIX) provides access by Account ID

---

## 3. Customer Master (`CVCUS01Y.cpy`)

**VSAM Dataset:** `CUSTDATA.VSAM.KSDS` | **Record Length:** 500 bytes | **Key:** Customer ID (9 digits)

| # | Business Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|---|---------------|------------|------------|------|-----:|----------------------|
| 1 | Customer ID | CUST-ID | PIC 9(09) | Numeric | 9 | Unique customer identifier |
| 2 | First Name | CUST-FIRST-NAME | PIC X(25) | Alpha | 25 | Customer first name |
| 3 | Middle Name | CUST-MIDDLE-NAME | PIC X(25) | Alpha | 25 | Customer middle name |
| 4 | Last Name | CUST-LAST-NAME | PIC X(25) | Alpha | 25 | Customer last name |
| 5 | Address Line 1 | CUST-ADDR-LINE-1 | PIC X(50) | Alpha | 50 | Street address |
| 6 | Address Line 2 | CUST-ADDR-LINE-2 | PIC X(50) | Alpha | 50 | Additional address |
| 7 | Address Line 3 | CUST-ADDR-LINE-3 | PIC X(50) | Alpha | 50 | City/region |
| 8 | State Code | CUST-ADDR-STATE-CD | PIC X(02) | Alpha | 2 | US state code |
| 9 | Country Code | CUST-ADDR-COUNTRY-CD | PIC X(03) | Alpha | 3 | Country code |
| 10 | ZIP Code | CUST-ADDR-ZIP | PIC X(10) | Alpha | 10 | Postal/ZIP code |
| 11 | Phone Number 1 | CUST-PHONE-NUM-1 | PIC X(15) | Alpha | 15 | Primary phone |
| 12 | Phone Number 2 | CUST-PHONE-NUM-2 | PIC X(15) | Alpha | 15 | Secondary phone |
| 13 | SSN | CUST-SSN | PIC 9(09) | Numeric | 9 | Social Security Number |
| 14 | Government ID | CUST-GOVT-ISSUED-ID | PIC X(20) | Alpha | 20 | Government-issued ID |
| 15 | Date of Birth | CUST-DOB-YYYYMMDD | PIC X(10) | Date | 10 | Date of birth |
| 16 | EFT Account ID | CUST-EFT-ACCOUNT-ID | PIC X(10) | Alpha | 10 | Electronic fund transfer account |
| 17 | Primary Holder Ind | CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alpha | 1 | Primary card holder indicator (Y/N) |
| 18 | FICO Score | CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric | 3 | Credit score (300-850) |
| 19 | Filler | FILLER | PIC X(168) | -- | 168 | Reserved space |

**Business Rules:**
- Customer linked to account via cross-reference file (CVACT03Y)
- SSN validated in COACTUPC with standard rules (no 000, 666, or 900-999 prefix)
- Phone validated as US format: (NNN)NNN-NNNN
- FICO score used for credit risk assessment

---

## 4. Card Cross-Reference (`CVACT03Y.cpy`)

**VSAM Dataset:** `CARDXREF.VSAM.KSDS` | **Record Length:** 50 bytes | **Key:** Card Number (16 digits)

| # | Business Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|---|---------------|------------|------------|------|-----:|----------------------|
| 1 | Card Number | XREF-CARD-NUM | PIC X(16) | Alpha | 16 | Credit card number (foreign key) |
| 2 | Customer ID | XREF-CUST-ID | PIC 9(09) | Numeric | 9 | Customer who owns this card |
| 3 | Account ID | XREF-ACCT-ID | PIC 9(11) | Numeric | 11 | Account this card belongs to |
| 4 | Filler | FILLER | PIC X(14) | -- | 14 | Reserved space |

**Business Rules:**
- Central lookup table: given a card number, find customer and account
- Used by nearly every program that processes transactions
- Alternate index (CXACAIX) provides access by Account ID
- Critical for transaction posting -- card must exist in xref to be valid

---

## 5. Transaction Master (`CVTRA05Y.cpy`)

**VSAM Dataset:** `TRANSACT.VSAM.KSDS` | **Record Length:** 350 bytes | **Key:** Transaction ID (16 chars)

| # | Business Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|---|---------------|------------|------------|------|-----:|----------------------|
| 1 | Transaction ID | TRAN-ID | PIC X(16) | Alpha | 16 | Unique transaction identifier |
| 2 | Type Code | TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type (SA=Sale, RE=Return, etc.) |
| 3 | Category Code | TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category within type |
| 4 | Source | TRAN-SOURCE | PIC X(10) | Alpha | 10 | Origination channel |
| 5 | Description | TRAN-DESC | PIC X(100) | Alpha | 100 | Transaction description |
| 6 | Amount | TRAN-AMT | PIC S9(09)V99 | Decimal | 11 | Transaction amount (signed, 2 decimals) |
| 7 | Merchant ID | TRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant identifier |
| 8 | Merchant Name | TRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant business name |
| 9 | Merchant City | TRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant city |
| 10 | Merchant ZIP | TRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant postal code |
| 11 | Card Number | TRAN-CARD-NUM | PIC X(16) | Alpha | 16 | Card used for this transaction |
| 12 | Origination Timestamp | TRAN-ORIG-TS | PIC X(26) | Timestamp | 26 | When transaction occurred |
| 13 | Processing Timestamp | TRAN-PROC-TS | PIC X(26) | Timestamp | 26 | When transaction was posted |
| 14 | Filler | FILLER | PIC X(20) | -- | 20 | Reserved space |

**Business Rules:**
- Highest-volume entity in the system
- Posted from daily transactions (CVTRA06Y) by CBTRN02C
- Amount can be positive (charge) or negative (credit/return)
- Card number links to cross-reference for account resolution
- Timestamps in COBOL FUNCTION CURRENT-DATE format

---

## 6. Daily Transaction Input (`CVTRA06Y.cpy`)

**Dataset:** `DALYTRAN.PS` (sequential) | **Record Length:** 350 bytes

| # | Business Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|---|---------------|------------|------------|------|-----:|----------------------|
| 1 | Transaction ID | DALYTRAN-ID | PIC X(16) | Alpha | 16 | Daily transaction identifier |
| 2 | Type Code | DALYTRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code |
| 3 | Category Code | DALYTRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category |
| 4 | Source | DALYTRAN-SOURCE | PIC X(10) | Alpha | 10 | Origination channel |
| 5 | Description | DALYTRAN-DESC | PIC X(100) | Alpha | 100 | Transaction description |
| 6 | Amount | DALYTRAN-AMT | PIC S9(09)V99 | Decimal | 11 | Transaction amount |
| 7 | Merchant ID | DALYTRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant identifier |
| 8 | Merchant Name | DALYTRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant name |
| 9 | Merchant City | DALYTRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant city |
| 10 | Merchant ZIP | DALYTRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant postal code |
| 11 | Card Number | DALYTRAN-CARD-NUM | PIC X(16) | Alpha | 16 | Card used |
| 12 | Origination TS | DALYTRAN-ORIG-TS | PIC X(26) | Timestamp | 26 | When transaction occurred |
| 13 | Processing TS | DALYTRAN-PROC-TS | PIC X(26) | Timestamp | 26 | When posted |
| 14 | Filler | FILLER | PIC X(20) | -- | 20 | Reserved |

**Business Rules:**
- Input feed for batch posting cycle
- Same layout as Transaction Master (CVTRA05Y) but with DALYTRAN- prefix
- Validated by CBTRN01C, posted by CBTRN02C
- Rejected records written to DALYREJS GDG

---

## 7. Transaction Category Balance (`CVTRA01Y.cpy`)

**VSAM Dataset:** `TCATBALF.VSAM.KSDS` | **Record Length:** 50 bytes | **Composite Key:** Account ID + Type + Category

| # | Business Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|---|---------------|------------|------------|------|-----:|----------------------|
| 1 | Account ID | TRANCAT-ACCT-ID | PIC 9(11) | Numeric | 11 | Account identifier |
| 2 | Type Code | TRANCAT-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type |
| 3 | Category Code | TRANCAT-CD | PIC 9(04) | Numeric | 4 | Transaction category |
| 4 | Balance | TRAN-CAT-BAL | PIC S9(09)V99 | Decimal | 11 | Running balance for this category |
| 5 | Filler | FILLER | PIC X(22) | -- | 22 | Reserved |

**Business Rules:**
- Running balance per account per transaction type/category
- Updated during transaction posting (CBTRN02C)
- Used for interest calculation (CBACT04C) -- different rates per category
- Backed up and printed via PRTCATBL job

---

## 8. Disclosure / Interest Rate Group (`CVTRA02Y.cpy`)

**VSAM Dataset:** `DISCGRP.VSAM.KSDS` | **Record Length:** 50 bytes | **Composite Key:** Group ID + Type + Category

| # | Business Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|---|---------------|------------|------------|------|-----:|----------------------|
| 1 | Account Group ID | DIS-ACCT-GROUP-ID | PIC X(10) | Alpha | 10 | Interest rate group |
| 2 | Transaction Type | DIS-TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code |
| 3 | Transaction Category | DIS-TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Category within type |
| 4 | Interest Rate | DIS-INT-RATE | PIC S9(04)V99 | Decimal | 6 | Annual interest rate (%) |
| 5 | Filler | FILLER | PIC X(28) | -- | 28 | Reserved |

**Business Rules:**
- Defines interest rates by account group, type, and category
- Referenced by CBACT04C during interest calculation
- Account's ACCT-GROUP-ID maps to DIS-ACCT-GROUP-ID
- Allows differentiated pricing (e.g., purchases vs. cash advances)

---

## 9. Transaction Type (`CVTRA03Y.cpy`)

**VSAM Dataset:** `TRANTYPE.VSAM.KSDS` | **Record Length:** 60 bytes | **Key:** Type Code (2 chars)

| # | Business Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|---|---------------|------------|------------|------|-----:|----------------------|
| 1 | Transaction Type | TRAN-TYPE | PIC X(02) | Alpha | 2 | Type code (e.g., SA, RE, PA) |
| 2 | Type Description | TRAN-TYPE-DESC | PIC X(50) | Alpha | 50 | Human-readable description |
| 3 | Filler | FILLER | PIC X(08) | -- | 8 | Reserved |

---

## 10. Transaction Category (`CVTRA04Y.cpy`)

**VSAM Dataset:** `TRANCATG.VSAM.KSDS` | **Record Length:** 60 bytes | **Composite Key:** Type Code + Category Code

| # | Business Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|---|---------------|------------|------------|------|-----:|----------------------|
| 1 | Type Code | TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Parent transaction type |
| 2 | Category Code | TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Category within type |
| 3 | Category Description | TRAN-CAT-TYPE-DESC | PIC X(50) | Alpha | 50 | Human-readable description |
| 4 | Filler | FILLER | PIC X(04) | -- | 4 | Reserved |

---

## 11. User Security Record (`CSUSR01Y.cpy`)

**VSAM Dataset:** `USRSEC.VSAM.KSDS` | **Record Length:** 80 bytes | **Key:** User ID (8 chars)

| # | Business Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|---|---------------|------------|------------|------|-----:|----------------------|
| 1 | User ID | SEC-USR-ID | PIC X(08) | Alpha | 8 | Login user identifier |
| 2 | First Name | SEC-USR-FNAME | PIC X(20) | Alpha | 20 | User first name |
| 3 | Last Name | SEC-USR-LNAME | PIC X(20) | Alpha | 20 | User last name |
| 4 | Password | SEC-USR-PWD | PIC X(08) | Alpha | 8 | Login password (plaintext) |
| 5 | User Type | SEC-USR-TYPE | PIC X(01) | Alpha | 1 | Role: A=Admin, U=Regular User |
| 6 | Filler | SEC-USR-FILLER | PIC X(23) | -- | 23 | Reserved |

**Business Rules:**
- Authentication performed in COSGN00C (plaintext password comparison)
- User type determines menu access (admin vs. regular)
- CRUD managed by COUSR00C-03C (admin only)
- Security concern: passwords stored in cleartext

---

## 12. Application Communication Area (`COCOM01Y.cpy`)

**Purpose:** Shared context passed between CICS programs via COMMAREA

| # | Business Field | COBOL Name | PIC Clause | Business Description |
|---|---------------|------------|------------|----------------------|
| 1 | From Transaction | CDEMO-FROM-TRANID | PIC X(04) | Calling transaction ID |
| 2 | From Program | CDEMO-FROM-PROGRAM | PIC X(08) | Calling program name |
| 3 | To Transaction | CDEMO-TO-TRANID | PIC X(04) | Target transaction ID |
| 4 | To Program | CDEMO-TO-PROGRAM | PIC X(08) | Target program name |
| 5 | User ID | CDEMO-USER-ID | PIC X(08) | Logged-in user |
| 6 | User Type | CDEMO-USER-TYPE | PIC X(01) | A=Admin, U=User |
| 7 | Program Context | CDEMO-PGM-CONTEXT | PIC 9(01) | 0=Enter, 1=Re-enter |
| 8 | Customer ID | CDEMO-CUST-ID | PIC 9(09) | Current customer context |
| 9 | Customer Name | CDEMO-CUST-FNAME/MNAME/LNAME | PIC X(25) x3 | Customer name parts |
| 10 | Account ID | CDEMO-ACCT-ID | PIC 9(11) | Current account context |
| 11 | Account Status | CDEMO-ACCT-STATUS | PIC X(01) | Current account status |
| 12 | Card Number | CDEMO-CARD-NUM | PIC 9(16) | Current card context |
| 13 | Last Map/Mapset | CDEMO-LAST-MAP/MAPSET | PIC X(7) | Screen navigation state |

---

## 13. Export/Import Record (`CVEXPORT.cpy`)

**Dataset:** `EXPORT.DATA` (sequential) | **Variable structure by entity type**

| # | Business Field | COBOL Name | PIC Clause | Business Description |
|---|---------------|------------|------------|----------------------|
| 1 | Table Name | EXP-TABLE-NAME | PIC X(10) | Entity identifier (CUSTDATA, ACCTDATA, etc.) |
| 2 | Data Payload | (varies by entity) | -- | Embedded full record of the corresponding entity |

**Business Rules:**
- Used by CBEXPORT/CBIMPORT for bulk data migration
- Contains union of all entity layouts (customer, account, xref, transaction, card)
- Table name field determines which sub-structure to interpret

---

## 14. Entity-Relationship Summary

```
┌─────────────┐     ┌─────────────────┐     ┌──────────────┐
│  CUSTOMER   │────▶│  CARD XREF      │◀────│   ACCOUNT    │
│  CVCUS01Y   │     │  CVACT03Y       │     │   CVACT01Y   │
│  Key: 9-dig │     │  Key: 16-char   │     │   Key: 11-dig│
│  Len: 500   │     │  Len: 50        │     │   Len: 300   │
└─────────────┘     └────────┬────────┘     └──────┬───────┘
                             │                      │
                    ┌────────▼────────┐     ┌──────▼───────┐
                    │   CARD MASTER   │     │  TRAN CAT BAL│
                    │   CVACT02Y      │     │  CVTRA01Y    │
                    │   Key: 16-char  │     │  Key: Comp   │
                    │   Len: 150      │     │  Len: 50     │
                    └────────┬────────┘     └──────────────┘
                             │
                    ┌────────▼────────┐     ┌──────────────┐
                    │  TRANSACTION    │────▶│ TRAN TYPE    │
                    │  CVTRA05Y       │     │ CVTRA03Y     │
                    │  Key: 16-char   │     │ Key: 2-char  │
                    │  Len: 350       │     │ Len: 60      │
                    └─────────────────┘     └──────┬───────┘
                                                   │
                    ┌─────────────────┐     ┌──────▼───────┐
                    │ DISCLOSURE GRP  │     │ TRAN CATEGORY│
                    │ CVTRA02Y        │     │ CVTRA04Y     │
                    │ Key: Composite  │     │ Key: Comp    │
                    │ Len: 50         │     │ Len: 60      │
                    └─────────────────┘     └──────────────┘

┌─────────────────┐
│  USER SECURITY  │  (Independent entity -- authentication)
│  CSUSR01Y       │
│  Key: 8-char    │
│  Len: 80        │
└─────────────────┘
```

### Key Relationships

| From | To | Relationship | Join Field |
|------|----|-------------|------------|
| Card Xref | Customer | N:1 | XREF-CUST-ID → CUST-ID |
| Card Xref | Account | N:1 | XREF-ACCT-ID → ACCT-ID |
| Card Xref | Card | 1:1 | XREF-CARD-NUM → CARD-NUM |
| Card | Account | N:1 | CARD-ACCT-ID → ACCT-ID |
| Transaction | Card | N:1 | TRAN-CARD-NUM → CARD-NUM |
| Transaction | Tran Type | N:1 | TRAN-TYPE-CD → TRAN-TYPE |
| Transaction | Tran Category | N:1 | TRAN-TYPE-CD + TRAN-CAT-CD → composite key |
| Tran Cat Balance | Account | N:1 | TRANCAT-ACCT-ID → ACCT-ID |
| Account | Disclosure Group | N:1 | ACCT-GROUP-ID → DIS-ACCT-GROUP-ID |

---

## 15. Modernization Mapping Guide

| COBOL Entity (Copybook) | Suggested Java Class | Suggested DB Table | Notes |
|--------------------------|---------------------|-------------------|-------|
| CVACT01Y (Account) | `Account.java` | `ACCOUNT` | Primary business entity |
| CVACT02Y (Card) | `CreditCard.java` | `CREDIT_CARD` | FK to Account |
| CVACT03Y (Card Xref) | -- (JPA relationship) | -- (FK constraints) | Replace with DB joins |
| CVCUS01Y (Customer) | `Customer.java` | `CUSTOMER` | Large record, many nullable fields |
| CVTRA05Y (Transaction) | `Transaction.java` | `TRANSACTION` | Highest volume table |
| CVTRA06Y (Daily Tran) | `DailyTransaction.java` | `DAILY_TRANSACTION` | Staging table for batch |
| CVTRA01Y (Cat Balance) | `CategoryBalance.java` | `CATEGORY_BALANCE` | Materialized aggregate |
| CVTRA02Y (Disc Group) | `DisclosureGroup.java` | `DISCLOSURE_GROUP` | Reference/config data |
| CVTRA03Y (Tran Type) | `TransactionType.java` | `TRANSACTION_TYPE` | Reference/lookup |
| CVTRA04Y (Tran Category) | `TransactionCategory.java` | `TRANSACTION_CATEGORY` | Reference/lookup |
| CSUSR01Y (User Security) | `UserSecurity.java` | `APP_USER` | Needs password hashing! |
| COCOM01Y (Commarea) | `SessionContext.java` | -- (HTTP session) | Navigation state |
