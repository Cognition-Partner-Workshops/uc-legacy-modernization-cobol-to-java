# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** Copybook PIC clause analysis across `app/cpy/` and `app/cpy-bms/`
> **Purpose:** Business-friendly reference mapping COBOL record layouts to logical data entities

---

## Entity Summary

| # | Business Entity              | Copybook   | Record Length | VSAM Dataset (Logical)      | Key Fields                  |
|---|------------------------------|------------|---------------|-----------------------------|-----------------------------|
| 1 | Account Master               | CVACT01Y   | 300 bytes     | ACCTDAT                     | ACCT-ID (11-digit)          |
| 2 | Credit Card                  | CVACT02Y   | 150 bytes     | CARDDAT                     | CARD-NUM (16-digit)         |
| 3 | Card–Account Cross-Reference | CVACT03Y   | 50 bytes      | CARDXREF / CXACAIX          | CARD-NUM + CUST-ID + ACCT-ID|
| 4 | Customer Master              | CVCUS01Y   | 500 bytes     | CUSTDAT                     | CUST-ID (9-digit)           |
| 5 | Transaction                  | CVTRA05Y   | 350 bytes     | TRANSACT                    | TRAN-ID (16-char)           |
| 6 | Daily Transaction            | CVTRA06Y   | 350 bytes     | DALYTRAN                    | DALYTRAN-ID (16-char)       |
| 7 | Transaction Category Balance | CVTRA01Y   | 50 bytes      | TCATBALF                    | ACCT-ID + TYPE-CD + CAT-CD  |
| 8 | Disclosure Group             | CVTRA02Y   | 50 bytes      | DISCGRP                     | GROUP-ID + TYPE-CD + CAT-CD |
| 9 | Transaction Type             | CVTRA03Y   | 60 bytes      | TRANTYPE                    | TRAN-TYPE (2-char)          |
|10 | Transaction Category         | CVTRA04Y   | 60 bytes      | TRANCATG                    | TYPE-CD + CAT-CD            |
|11 | User Security                | CSUSR01Y   | 80 bytes      | USRSEC                      | SEC-USR-ID (8-char)         |
|12 | Export/Import Record         | CVEXPORT   | 500 bytes     | Flat file                   | EXP-RECORD-TYPE             |
|13 | Application Communication    | COCOM01Y   | ~100 bytes    | In-memory (COMMAREA)        | N/A                         |

---

## 1. Account Master — `CVACT01Y`

**Business Description:** Core account record storing credit card account details including balances, credit limits, status, and important dates. One account can be linked to multiple cards.

**VSAM File:** `ACCTDAT` (KSDS, Key = ACCT-ID, Record Length = 300)

| Field Name               | PIC Clause         | Type       | Length | Business Meaning                              | Validation / Notes                |
|--------------------------|--------------------|------------|-------:|-----------------------------------------------|-----------------------------------|
| ACCT-ID                  | 9(11)              | Numeric    |     11 | Unique account identifier                     | Primary key                       |
| ACCT-ACTIVE-STATUS       | X(01)              | Alpha      |      1 | Account status flag                           | 'Y' = Active, 'N' = Inactive     |
| ACCT-CURR-BAL            | S9(10)V99          | Signed Dec |     12 | Current account balance                       | Dollars and cents                 |
| ACCT-CREDIT-LIMIT        | S9(10)V99          | Signed Dec |     12 | Total credit limit                            | Max spending allowed              |
| ACCT-CASH-CREDIT-LIMIT   | S9(10)V99          | Signed Dec |     12 | Cash advance credit limit                     | Subset of total credit            |
| ACCT-OPEN-DATE           | X(10)              | Date       |     10 | Date account was opened                       | Format: YYYY-MM-DD                |
| ACCT-EXPIRAION-DATE      | X(10)              | Date       |     10 | Account expiration date                       | Format: YYYY-MM-DD                |
| ACCT-REISSUE-DATE        | X(10)              | Date       |     10 | Card reissue date                             | Format: YYYY-MM-DD                |
| ACCT-CURR-CYC-CREDIT     | S9(10)V99          | Signed Dec |     12 | Current cycle credit total                    | Credits in current billing cycle  |
| ACCT-CURR-CYC-DEBIT      | S9(10)V99          | Signed Dec |     12 | Current cycle debit total                     | Debits in current billing cycle   |
| ACCT-ADDR-ZIP            | X(10)              | Alpha      |     10 | Account holder ZIP code                       |                                   |
| ACCT-GROUP-ID            | X(10)              | Alpha      |     10 | Disclosure/interest group identifier          | Links to DISCGRP                  |
| FILLER                   | X(178)             | Filler     |    178 | Reserved space                                | For future expansion              |

**Java Target:** `Account.java` (JPA Entity) → `accounts` table

---

## 2. Credit Card — `CVACT02Y`

**Business Description:** Individual credit card record with card number, embossed name, expiration, CVV, and active status. Linked to an account via cross-reference.

**VSAM File:** `CARDDAT` (KSDS, Key = CARD-NUM, Record Length = 150)

| Field Name               | PIC Clause         | Type       | Length | Business Meaning                              | Validation / Notes                |
|--------------------------|--------------------|------------|-------:|-----------------------------------------------|-----------------------------------|
| CARD-NUM                 | X(16)              | Alpha      |     16 | Card number (PAN)                             | Primary key; 16-digit PAN         |
| CARD-ACCT-ID             | 9(11)              | Numeric    |     11 | Associated account ID                         | Foreign key → Account             |
| CARD-CVV-CD              | 9(03)              | Numeric    |      3 | Card verification value                       | 3-digit security code             |
| CARD-EMBOSSED-NAME       | X(50)              | Alpha      |     50 | Name printed on card                          | Cardholder name                   |
| CARD-EXPIRAION-DATE      | X(10)              | Date       |     10 | Card expiration date                          | Format: YYYY-MM-DD                |
| CARD-ACTIVE-STATUS       | X(01)              | Alpha      |      1 | Card active status                            | 'Y' = Active, 'N' = Inactive     |
| FILLER                   | X(59)              | Filler     |     59 | Reserved space                                |                                   |

**Java Target:** `CreditCard.java` (JPA Entity) → `credit_cards` table

---

## 3. Card–Account Cross-Reference — `CVACT03Y`

**Business Description:** Links credit card numbers to customer IDs and account IDs. Provides the many-to-many relationship between cards, customers, and accounts.

**VSAM File:** `CARDXREF` / `CXACAIX` (KSDS with AIX, Key = CARD-NUM, Record Length = 50)

| Field Name               | PIC Clause         | Type       | Length | Business Meaning                              | Validation / Notes                |
|--------------------------|--------------------|------------|-------:|-----------------------------------------------|-----------------------------------|
| XREF-CARD-NUM            | X(16)              | Alpha      |     16 | Card number                                   | Primary key                       |
| XREF-CUST-ID             | 9(09)              | Numeric    |      9 | Customer ID                                   | Foreign key → Customer            |
| XREF-ACCT-ID             | 9(11)              | Numeric    |     11 | Account ID                                    | Foreign key → Account             |
| FILLER                   | X(14)              | Filler     |     14 | Reserved space                                |                                   |

**Java Target:** Join table or relationship annotations on `CreditCard.java`

---

## 4. Customer Master — `CVCUS01Y`

**Business Description:** Customer personal information including name, address, phone, SSN, date of birth, FICO score, and other demographics. One customer can hold multiple accounts.

**VSAM File:** `CUSTDAT` (KSDS, Key = CUST-ID, Record Length = 500)

| Field Name               | PIC Clause         | Type       | Length | Business Meaning                              | Validation / Notes                |
|--------------------------|--------------------|------------|-------:|-----------------------------------------------|-----------------------------------|
| CUST-ID                  | 9(09)              | Numeric    |      9 | Unique customer identifier                    | Primary key                       |
| CUST-FIRST-NAME          | X(25)              | Alpha      |     25 | Customer first name                           |                                   |
| CUST-MIDDLE-NAME         | X(25)              | Alpha      |     25 | Customer middle name                          |                                   |
| CUST-LAST-NAME           | X(25)              | Alpha      |     25 | Customer last name                            |                                   |
| CUST-ADDR-LINE-1         | X(50)              | Alpha      |     50 | Address line 1                                |                                   |
| CUST-ADDR-LINE-2         | X(50)              | Alpha      |     50 | Address line 2                                |                                   |
| CUST-ADDR-LINE-3         | X(50)              | Alpha      |     50 | Address line 3                                |                                   |
| CUST-ADDR-STATE-CD       | X(02)              | Alpha      |      2 | US state code                                 | 2-letter state abbreviation       |
| CUST-ADDR-COUNTRY-CD     | X(03)              | Alpha      |      3 | Country code                                  |                                   |
| CUST-ADDR-ZIP            | X(10)              | Alpha      |     10 | ZIP / postal code                             |                                   |
| CUST-PHONE-NUM-1         | X(15)              | Alpha      |     15 | Primary phone number                          | Format: (NNN)NNN-NNNN            |
| CUST-PHONE-NUM-2         | X(15)              | Alpha      |     15 | Secondary phone number                        |                                   |
| CUST-SSN                 | 9(09)              | Numeric    |      9 | Social Security Number                        | PII — requires encryption         |
| CUST-GOVT-ISSUED-ID      | X(20)              | Alpha      |     20 | Government-issued ID number                   |                                   |
| CUST-DOB-YYYY-MM-DD      | X(10)              | Date       |     10 | Date of birth                                 | Format: YYYY-MM-DD                |
| CUST-EFT-ACCOUNT-ID      | X(10)              | Alpha      |     10 | Electronic funds transfer account             | Bank account for payments         |
| CUST-PRI-CARD-HOLDER-IND | X(01)              | Alpha      |      1 | Primary cardholder indicator                  | 'Y' / 'N'                        |
| CUST-FICO-CREDIT-SCORE   | 9(03)              | Numeric    |      3 | FICO credit score                             | Range: 300–850                    |
| FILLER                   | X(168)             | Filler     |    168 | Reserved space                                |                                   |

**Java Target:** `Customer.java` (JPA Entity) → `customers` table

---

## 5. Transaction — `CVTRA05Y`

**Business Description:** Core transaction record for credit card purchases, payments, and other financial activities. Contains merchant details, timestamps, and transaction classification.

**VSAM File:** `TRANSACT` (KSDS, Key = TRAN-ID, Record Length = 350)

| Field Name               | PIC Clause         | Type       | Length | Business Meaning                              | Validation / Notes                |
|--------------------------|--------------------|------------|-------:|-----------------------------------------------|-----------------------------------|
| TRAN-ID                  | X(16)              | Alpha      |     16 | Unique transaction identifier                 | Primary key                       |
| TRAN-TYPE-CD             | X(02)              | Alpha      |      2 | Transaction type code                         | FK → Transaction Type             |
| TRAN-CAT-CD              | 9(04)              | Numeric    |      4 | Transaction category code                     | FK → Transaction Category         |
| TRAN-SOURCE              | X(10)              | Alpha      |     10 | Transaction source                            | e.g., POS, ATM, ONLINE           |
| TRAN-DESC                | X(100)             | Alpha      |    100 | Transaction description                       | Free-text description             |
| TRAN-AMT                 | S9(09)V99          | Signed Dec |     11 | Transaction amount                            | Dollars and cents; signed         |
| TRAN-MERCHANT-ID         | 9(09)              | Numeric    |      9 | Merchant identifier                           |                                   |
| TRAN-MERCHANT-NAME       | X(50)              | Alpha      |     50 | Merchant business name                        |                                   |
| TRAN-MERCHANT-CITY       | X(50)              | Alpha      |     50 | Merchant city                                 |                                   |
| TRAN-MERCHANT-ZIP        | X(10)              | Alpha      |     10 | Merchant ZIP code                             |                                   |
| TRAN-CARD-NUM            | X(16)              | Alpha      |     16 | Card number used                              | FK → Credit Card                  |
| TRAN-ORIG-TS             | X(26)              | Timestamp  |     26 | Transaction origination timestamp             | High-precision timestamp          |
| TRAN-PROC-TS             | X(26)              | Timestamp  |     26 | Transaction processing timestamp              |                                   |
| FILLER                   | X(20)              | Filler     |     20 | Reserved space                                |                                   |

**Java Target:** `Transaction.java` (JPA Entity) → `transactions` table

---

## 6. Daily Transaction — `CVTRA06Y`

**Business Description:** Identical structure to Transaction but represents daily unprocessed transactions that feed into the batch posting cycle. After posting, records move to the main Transaction file.

**VSAM File:** `DALYTRAN` (Sequential or KSDS, Record Length = 350)

| Field Name               | PIC Clause         | Type       | Length | Business Meaning                              |
|--------------------------|--------------------|------------|-------:|-----------------------------------------------|
| DALYTRAN-ID              | X(16)              | Alpha      |     16 | Daily transaction identifier                  |
| DALYTRAN-TYPE-CD         | X(02)              | Alpha      |      2 | Transaction type code                         |
| DALYTRAN-CAT-CD          | 9(04)              | Numeric    |      4 | Transaction category code                     |
| DALYTRAN-SOURCE          | X(10)              | Alpha      |     10 | Transaction source                            |
| DALYTRAN-DESC            | X(100)             | Alpha      |    100 | Transaction description                       |
| DALYTRAN-AMT             | S9(09)V99          | Signed Dec |     11 | Transaction amount                            |
| DALYTRAN-MERCHANT-ID     | 9(09)              | Numeric    |      9 | Merchant identifier                           |
| DALYTRAN-MERCHANT-NAME   | X(50)              | Alpha      |     50 | Merchant business name                        |
| DALYTRAN-MERCHANT-CITY   | X(50)              | Alpha      |     50 | Merchant city                                 |
| DALYTRAN-MERCHANT-ZIP    | X(10)              | Alpha      |     10 | Merchant ZIP code                             |
| DALYTRAN-CARD-NUM        | X(16)              | Alpha      |     16 | Card number used                              |
| DALYTRAN-ORIG-TS         | X(26)              | Timestamp  |     26 | Origination timestamp                         |
| DALYTRAN-PROC-TS         | X(26)              | Timestamp  |     26 | Processing timestamp                          |
| FILLER                   | X(20)              | Filler     |     20 | Reserved space                                |

**Java Target:** Same as `Transaction.java` with a `status` field to distinguish daily vs. posted

---

## 7. Transaction Category Balance — `CVTRA01Y`

**Business Description:** Running balance per account per transaction type and category. Used for interest calculation and reporting.

**VSAM File:** `TCATBALF` (KSDS, Compound Key, Record Length = 50)

| Field Name               | PIC Clause         | Type       | Length | Business Meaning                              |
|--------------------------|--------------------|------------|-------:|-----------------------------------------------|
| TRANCAT-ACCT-ID          | 9(11)              | Numeric    |     11 | Account identifier                            |
| TRANCAT-TYPE-CD          | X(02)              | Alpha      |      2 | Transaction type code                         |
| TRANCAT-CD               | 9(04)              | Numeric    |      4 | Transaction category code                     |
| TRAN-CAT-BAL             | S9(09)V99          | Signed Dec |     11 | Category balance amount                       |
| FILLER                   | X(22)              | Filler     |     22 | Reserved space                                |

**Java Target:** `TransactionCategoryBalance.java` → `tran_cat_balances` table

---

## 8. Disclosure Group — `CVTRA02Y`

**Business Description:** Interest rate configuration per account group, transaction type, and category. Drives the interest calculation engine.

**VSAM File:** `DISCGRP` (KSDS, Compound Key, Record Length = 50)

| Field Name               | PIC Clause         | Type       | Length | Business Meaning                              |
|--------------------------|--------------------|------------|-------:|-----------------------------------------------|
| DIS-ACCT-GROUP-ID        | X(10)              | Alpha      |     10 | Account group identifier                      |
| DIS-TRAN-TYPE-CD         | X(02)              | Alpha      |      2 | Transaction type code                         |
| DIS-TRAN-CAT-CD          | 9(04)              | Numeric    |      4 | Transaction category code                     |
| DIS-INT-RATE             | S9(04)V99          | Signed Dec |      6 | Interest rate                                 |
| FILLER                   | X(28)              | Filler     |     28 | Reserved space                                |

**Java Target:** `DisclosureGroup.java` → `disclosure_groups` table

---

## 9. Transaction Type — `CVTRA03Y`

**Business Description:** Lookup table for transaction type codes and their descriptions (e.g., "01" = Purchase, "02" = Cash Advance).

**VSAM File:** `TRANTYPE` (KSDS, Key = TRAN-TYPE, Record Length = 60)

| Field Name               | PIC Clause         | Type       | Length | Business Meaning                              |
|--------------------------|--------------------|------------|-------:|-----------------------------------------------|
| TRAN-TYPE                | X(02)              | Alpha      |      2 | Transaction type code                         |
| TRAN-TYPE-DESC           | X(50)              | Alpha      |     50 | Type description                              |
| FILLER                   | X(08)              | Filler     |      8 | Reserved space                                |

**Java Target:** `TransactionType.java` (enum or lookup table) → `transaction_types` table

---

## 10. Transaction Category — `CVTRA04Y`

**Business Description:** Sub-classification within each transaction type (e.g., within Purchases: groceries, fuel, travel).

**VSAM File:** `TRANCATG` (KSDS, Compound Key, Record Length = 60)

| Field Name               | PIC Clause         | Type       | Length | Business Meaning                              |
|--------------------------|--------------------|------------|-------:|-----------------------------------------------|
| TRAN-TYPE-CD             | X(02)              | Alpha      |      2 | Transaction type code (part of key)           |
| TRAN-CAT-CD              | 9(04)              | Numeric    |      4 | Transaction category code (part of key)       |
| TRAN-CAT-TYPE-DESC       | X(50)              | Alpha      |     50 | Category description                          |
| FILLER                   | X(04)              | Filler     |      4 | Reserved space                                |

**Java Target:** `TransactionCategory.java` → `transaction_categories` table

---

## 11. User Security — `CSUSR01Y`

**Business Description:** User authentication record containing user ID, password, first/last name, and user type (admin vs. regular).

**VSAM File:** `USRSEC` (KSDS, Key = SEC-USR-ID, Record Length = 80)

| Field Name               | PIC Clause         | Type       | Length | Business Meaning                              | Validation / Notes                |
|--------------------------|--------------------|------------|-------:|-----------------------------------------------|-----------------------------------|
| SEC-USR-ID               | X(08)              | Alpha      |      8 | User login identifier                         | Primary key                       |
| SEC-USR-FNAME            | X(20)              | Alpha      |     20 | User first name                               |                                   |
| SEC-USR-LNAME            | X(20)              | Alpha      |     20 | User last name                                |                                   |
| SEC-USR-PWD              | X(08)              | Alpha      |      8 | User password                                 | Plaintext — needs hashing in Java |
| SEC-USR-TYPE             | X(01)              | Alpha      |      1 | User type                                     | 'A' = Admin, 'U' = Regular User   |
| SEC-USR-FILLER           | X(23)              | Filler     |     23 | Reserved space                                |                                   |

**Java Target:** `User.java` (Spring Security UserDetails) → `users` table

---

## 12. Export/Import Record — `CVEXPORT`

**Business Description:** Multiplexed record format used by CBEXPORT/CBIMPORT to serialize all VSAM data into a single flat file. A type-code prefix identifies the entity within each record.

**VSAM File:** Flat sequential file (Record Length = 500)

| Field Name               | PIC Clause         | Type       | Length | Business Meaning                              |
|--------------------------|--------------------|------------|-------:|-----------------------------------------------|
| EXP-RECORD-TYPE          | X(01)              | Alpha      |      1 | Record type discriminator                     |
| EXP-DATA (union)         | X(499)             | Alpha      |    499 | Payload — layout varies by record type        |

**Record Type Values:**

| Type Code | Entity              | Overlay Copybook |
|-----------|---------------------|------------------|
| 1         | Customer            | CVCUS01Y         |
| 2         | Account             | CVACT01Y         |
| 3         | Card Cross-Ref      | CVACT03Y         |
| 4         | Transaction         | CVTRA05Y         |
| 5         | Card                | CVACT02Y         |

---

## 13. Application Communication Area — `COCOM01Y`

**Business Description:** CICS COMMAREA passed between programs to maintain session state, user context, navigation history, and current screen selections.

| Field Name               | PIC Clause         | Type       | Length | Business Meaning                              |
|--------------------------|--------------------|------------|-------:|-----------------------------------------------|
| CDEMO-FROM-TRANID        | X(04)              | Alpha      |      4 | Originating transaction ID                    |
| CDEMO-FROM-PROGRAM       | X(08)              | Alpha      |      8 | Originating program name                      |
| CDEMO-TO-TRANID          | X(04)              | Alpha      |      4 | Target transaction ID                         |
| CDEMO-TO-PROGRAM         | X(08)              | Alpha      |      8 | Target program name                           |
| CDEMO-USER-ID            | X(08)              | Alpha      |      8 | Logged-in user ID                             |
| CDEMO-USER-TYPE          | X(01)              | Alpha      |      1 | User type (A=Admin, U=User)                   |
| CDEMO-PGM-CONTEXT        | 9(01)              | Numeric    |      1 | Program context flag (0=Enter, 1=Reenter)     |
| CDEMO-ACCT-ID            | 9(11)              | Numeric    |     11 | Selected account ID                           |
| CDEMO-CARD-NUM           | X(16)              | Alpha      |     16 | Selected card number                          |
| CDEMO-LAST-MAP           | X(07)              | Alpha      |      7 | Last BMS map displayed                        |
| CDEMO-LAST-MAPSET        | X(07)              | Alpha      |      7 | Last BMS mapset displayed                     |
| CCARD-AID-*              | X(01)              | Alpha      |      1 | AID key flags (Enter, PF3, PF7, PF8, etc.)   |
| CCARD-ERROR-MSG          | X(75)              | Alpha      |     75 | Error message for display                     |

**Java Target:** HTTP Session attributes or Spring Security context

---

## 14. Supplementary Copybooks

### Report Layout — `CVTRA07Y`

Used by CBTRN03C for the Daily Transaction Report.

| Structure                 | Purpose                                        |
|---------------------------|------------------------------------------------|
| REPORT-NAME-HEADER        | Report header with name, date range             |
| TRANSACTION-DETAIL-REPORT | Detail line: Tran ID, Account, Type, Category, Source, Amount |
| TRANSACTION-HEADER-1/2    | Column headers and separator line               |
| REPORT-PAGE-TOTALS        | Page subtotal                                   |
| REPORT-ACCOUNT-TOTALS     | Account-level subtotal                          |
| REPORT-GRAND-TOTALS       | Grand total line                                |

### Statement Layout — `COSTM01`

Alternate transaction layout for statement generation (CBSTM03A/B). Key differs from CVTRA05Y by using CARD-NUM + TRAN-ID as compound key.

| Field Name               | PIC Clause         | Length | Business Meaning                              |
|--------------------------|--------------------|-------:|-----------------------------------------------|
| TRNX-CARD-NUM            | X(16)              |     16 | Card number (part of key)                     |
| TRNX-ID                  | X(16)              |     16 | Transaction ID (part of key)                  |
| TRNX-TYPE-CD             | X(02)              |      2 | Transaction type                              |
| TRNX-AMT                 | S9(09)V99          |     11 | Transaction amount                            |
| *(remaining fields)*      | *(same as CVTRA05Y)*|       | Merchant, timestamps, etc.                    |

### Customer Record (Statement) — `CUSTREC`

Alternate customer layout for statement generation with address formatted differently.

### Card Work Area — `CVCRD01Y`

Working-storage area for credit card screen processing. Not a VSAM record.

### Date Handling — `CSDAT01Y`

| Field Name               | PIC Clause         | Purpose                           |
|--------------------------|--------------------|-----------------------------------|
| WS-CURDATE-DATA          | X(21)              | CURRENT-DATE intrinsic result     |
| WS-CURDATE-YEAR          | 9(04)              | 4-digit year                      |
| WS-CURDATE-MONTH         | 9(02)              | 2-digit month                     |
| WS-CURDATE-DAY           | 9(02)              | 2-digit day                       |
| WS-CURTIME-HOURS         | 9(02)              | Hours                             |
| WS-CURTIME-MINUTE        | 9(02)              | Minutes                           |
| WS-CURTIME-SECOND        | 9(02)              | Seconds                           |

---

## Entity-Relationship Summary

```
┌──────────────┐       ┌──────────────────┐       ┌──────────────┐
│   Customer   │1─────*│  Card Cross-Ref  │*─────1│   Account    │
│  (CVCUS01Y)  │       │   (CVACT03Y)     │       │  (CVACT01Y)  │
│              │       │                  │       │              │
│  CUST-ID (PK)│       │  XREF-CARD-NUM   │       │  ACCT-ID (PK)│
│  Name        │       │  XREF-CUST-ID(FK)│       │  Balance     │
│  Address     │       │  XREF-ACCT-ID(FK)│       │  Credit Limit│
│  SSN         │       └────────┬─────────┘       │  Status      │
│  FICO Score  │                │                  │  Group ID    │
└──────────────┘                │                  └──────┬───────┘
                                │                         │
                         ┌──────┴───────┐          ┌──────┴───────┐
                         │ Credit Card  │          │  Tran Cat    │
                         │ (CVACT02Y)   │          │   Balance    │
                         │              │          │ (CVTRA01Y)   │
                         │ CARD-NUM (PK)│          └──────────────┘
                         │ Name, CVV    │
                         │ Expiry, Stat │
                         └──────┬───────┘
                                │
                         ┌──────┴───────┐       ┌──────────────┐
                         │ Transaction  │*─────1│  Tran Type   │
                         │ (CVTRA05Y)   │       │ (CVTRA03Y)   │
                         │              │       └──────────────┘
                         │ TRAN-ID (PK) │       ┌──────────────┐
                         │ Amount       │*─────1│  Tran Cat    │
                         │ Merchant     │       │ (CVTRA04Y)   │
                         │ Timestamps   │       └──────────────┘
                         └──────────────┘
                                              ┌──────────────┐
                                              │ Disclosure   │
                                              │   Group      │
                                              │ (CVTRA02Y)   │
                                              │ Interest Rate│
                                              └──────────────┘
```

---

## PII and Security Considerations

| Field                | Entity    | Risk Level | Modernization Action                |
|----------------------|-----------|------------|-------------------------------------|
| CUST-SSN             | Customer  | **Critical** | Encrypt at rest; mask in UI         |
| SEC-USR-PWD          | User      | **Critical** | Replace with bcrypt/scrypt hash     |
| CARD-NUM             | Card      | **High**     | Tokenize; PCI DSS compliance        |
| CARD-CVV-CD          | Card      | **High**     | Never store; remove from database   |
| CUST-DOB-YYYY-MM-DD  | Customer  | **Medium**   | Access control; audit logging       |
| CUST-PHONE-NUM-*     | Customer  | **Medium**   | Encrypt or mask last 4 digits       |
| CUST-GOVT-ISSUED-ID  | Customer  | **High**     | Encrypt at rest                     |
