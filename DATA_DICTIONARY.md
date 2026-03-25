# CardDemo Data Dictionary

> **Generated:** 2026-03-25 | **Source:** COBOL Copybooks in `app/cpy/`
>
> This dictionary maps mainframe COBOL record layouts (PIC clauses) to business-friendly
> entity definitions suitable for Java POJO / relational-database design.

---

## Entity Summary

| # | Entity                     | Copybook   | VSAM File          | Record Len | Key                           |
|---|----------------------------|------------|--------------------|:----------:|-------------------------------|
| 1 | Account                    | CVACT01Y   | ACCTDATA.VSAM.KSDS |    300     | Account ID (11 digits)        |
| 2 | Card                       | CVACT02Y   | CARDDATA.VSAM.KSDS |    150     | Card Number (16 chars)        |
| 3 | Card Cross-Reference       | CVACT03Y   | CARDXREF.VSAM.KSDS |     50     | Card Number (16 chars)        |
| 4 | Customer                   | CVCUS01Y   | CUSTDATA.VSAM.KSDS |    500     | Customer ID (9 digits)        |
| 5 | Transaction                | CVTRA05Y   | TRANSACT.VSAM.KSDS |    350     | Transaction ID (16 chars)     |
| 6 | Daily Transaction          | CVTRA06Y   | DALYTRAN.VSAM.KSDS |    350     | Transaction ID (16 chars)     |
| 7 | Transaction Category Balance| CVTRA01Y  | TCATBALF.VSAM.KSDS |     50     | Account ID + Type + Category  |
| 8 | Disclosure Group           | CVTRA02Y   | DISCGRP.VSAM.KSDS  |     50     | Group ID + Type + Category    |
| 9 | Transaction Type           | CVTRA03Y   | TRANTYPE.VSAM.KSDS |     60     | Type Code (2 chars)           |
|10 | Transaction Category       | CVTRA04Y   | TRANCATG.VSAM.KSDS |     60     | Type Code + Category Code     |
|11 | User Security              | CSUSR01Y   | USRSEC.VSAM.KSDS   |     80     | User ID (8 chars)             |
|12 | Transaction Report Layout  | CVTRA07Y   | — (report output)   |    133     | —                             |
|13 | Statement Transaction      | COSTM01    | — (sorted copy)     |    350     | Card Num + Tran ID (32 chars) |
|14 | Export Record              | CVEXPORT   | EXPFILE (seq)       |   varies   | Record-type prefix            |

---

## 1. Account Entity (`CVACT01Y`) — 300 bytes

**Business Description:** Core credit card account master record containing balances, limits, dates, and status.

| # | Field Name               | PIC Clause       | Type      | Length | Business Meaning                              | Java Type     |
|---|--------------------------|------------------|-----------|:------:|-----------------------------------------------|---------------|
| 1 | ACCT-ID                  | 9(11)            | Numeric   |   11   | Unique account identifier                     | `long`        |
| 2 | ACCT-ACTIVE-STATUS       | X(01)            | Alpha     |    1   | Active status flag (Y/N)                      | `String`      |
| 3 | ACCT-CURR-BAL            | S9(10)V99        | Signed Dec|   12   | Current account balance                       | `BigDecimal`  |
| 4 | ACCT-CREDIT-LIMIT        | S9(10)V99        | Signed Dec|   12   | Credit limit                                  | `BigDecimal`  |
| 5 | ACCT-CASH-CREDIT-LIMIT   | S9(10)V99        | Signed Dec|   12   | Cash advance credit limit                     | `BigDecimal`  |
| 6 | ACCT-OPEN-DATE           | X(10)            | Alpha     |   10   | Account open date (YYYY-MM-DD)                | `LocalDate`   |
| 7 | ACCT-EXPIRAION-DATE      | X(10)            | Alpha     |   10   | Account expiration date                       | `LocalDate`   |
| 8 | ACCT-REISSUE-DATE        | X(10)            | Alpha     |   10   | Card reissue date                             | `LocalDate`   |
| 9 | ACCT-CURR-CYC-CREDIT     | S9(10)V99       | Signed Dec|   12   | Current cycle credits                         | `BigDecimal`  |
|10 | ACCT-CURR-CYC-DEBIT      | S9(10)V99       | Signed Dec|   12   | Current cycle debits                          | `BigDecimal`  |
|11 | ACCT-ADDR-ZIP            | X(10)            | Alpha     |   10   | Account holder ZIP code                       | `String`      |
|12 | ACCT-GROUP-ID            | X(10)            | Alpha     |   10   | Account group / disclosure group              | `String`      |
|13 | FILLER                   | X(178)           | —         |  178   | Reserved space                                | —             |

---

## 2. Card Entity (`CVACT02Y`) — 150 bytes

**Business Description:** Credit card record with embossed name, expiration, and activation status.

| # | Field Name               | PIC Clause       | Type      | Length | Business Meaning                              | Java Type     |
|---|--------------------------|------------------|-----------|:------:|-----------------------------------------------|---------------|
| 1 | CARD-NUM                 | X(16)            | Alpha     |   16   | Card number (PAN)                             | `String`      |
| 2 | CARD-ACCT-ID             | 9(11)            | Numeric   |   11   | Owning account ID (FK → Account)              | `long`        |
| 3 | CARD-CVV-CD              | 9(03)            | Numeric   |    3   | Card verification value                       | `String`      |
| 4 | CARD-EMBOSSED-NAME       | X(50)            | Alpha     |   50   | Name embossed on card                         | `String`      |
| 5 | CARD-EXPIRAION-DATE      | X(10)            | Alpha     |   10   | Card expiration date                          | `LocalDate`   |
| 6 | CARD-ACTIVE-STATUS       | X(01)            | Alpha     |    1   | Active status (Y/N)                           | `String`      |
| 7 | FILLER                   | X(59)            | —         |   59   | Reserved space                                | —             |

---

## 3. Card Cross-Reference Entity (`CVACT03Y`) — 50 bytes

**Business Description:** Links a card number to its owning account and customer, enabling reverse lookups. Has an alternate index on Account ID.

| # | Field Name               | PIC Clause       | Type      | Length | Business Meaning                              | Java Type     |
|---|--------------------------|------------------|-----------|:------:|-----------------------------------------------|---------------|
| 1 | XREF-CARD-NUM            | X(16)            | Alpha     |   16   | Card number (PK)                              | `String`      |
| 2 | XREF-CUST-ID             | 9(09)            | Numeric   |    9   | Customer ID (FK → Customer)                   | `long`        |
| 3 | XREF-ACCT-ID             | 9(11)            | Numeric   |   11   | Account ID (FK → Account)                     | `long`        |
| 4 | FILLER                   | X(14)            | —         |   14   | Reserved space                                | —             |

**Alternate Index:** KEYS(11,25) — Account ID at offset 25, non-unique.

---

## 4. Customer Entity (`CVCUS01Y`) — 500 bytes

**Business Description:** Full customer demographic record with name, address, SSN, credit score, and contact info.

| # | Field Name               | PIC Clause       | Type      | Length | Business Meaning                              | Java Type     |
|---|--------------------------|------------------|-----------|:------:|-----------------------------------------------|---------------|
| 1 | CUST-ID                  | 9(09)            | Numeric   |    9   | Unique customer identifier                    | `long`        |
| 2 | CUST-FIRST-NAME          | X(25)            | Alpha     |   25   | First name                                    | `String`      |
| 3 | CUST-MIDDLE-NAME         | X(25)            | Alpha     |   25   | Middle name                                   | `String`      |
| 4 | CUST-LAST-NAME           | X(25)            | Alpha     |   25   | Last name                                     | `String`      |
| 5 | CUST-ADDR-LINE-1         | X(50)            | Alpha     |   50   | Address line 1                                | `String`      |
| 6 | CUST-ADDR-LINE-2         | X(50)            | Alpha     |   50   | Address line 2                                | `String`      |
| 7 | CUST-ADDR-LINE-3         | X(50)            | Alpha     |   50   | Address line 3                                | `String`      |
| 8 | CUST-ADDR-STATE-CD       | X(02)            | Alpha     |    2   | State code                                    | `String`      |
| 9 | CUST-ADDR-COUNTRY-CD     | X(03)            | Alpha     |    3   | Country code                                  | `String`      |
|10 | CUST-ADDR-ZIP            | X(10)            | Alpha     |   10   | ZIP / postal code                             | `String`      |
|11 | CUST-PHONE-NUM-1         | X(15)            | Alpha     |   15   | Primary phone                                 | `String`      |
|12 | CUST-PHONE-NUM-2         | X(15)            | Alpha     |   15   | Secondary phone                               | `String`      |
|13 | CUST-SSN                 | 9(09)            | Numeric   |    9   | Social Security Number                        | `String`      |
|14 | CUST-GOVT-ISSUED-ID      | X(20)            | Alpha     |   20   | Government-issued ID                          | `String`      |
|15 | CUST-DOB-YYYY-MM-DD      | X(10)            | Alpha     |   10   | Date of birth (YYYY-MM-DD)                    | `LocalDate`   |
|16 | CUST-EFT-ACCOUNT-ID      | X(10)            | Alpha     |   10   | Electronic funds transfer account             | `String`      |
|17 | CUST-PRI-CARD-HOLDER-IND | X(01)            | Alpha     |    1   | Primary cardholder indicator (Y/N)            | `String`      |
|18 | CUST-FICO-CREDIT-SCORE   | 9(03)            | Numeric   |    3   | FICO credit score                             | `int`         |
|19 | FILLER                   | X(168)           | —         |  168   | Reserved space                                | —             |

---

## 5. Transaction Entity (`CVTRA05Y`) — 350 bytes

**Business Description:** Individual credit card transaction with merchant details, amounts, and timestamps.

| # | Field Name               | PIC Clause       | Type      | Length | Business Meaning                              | Java Type        |
|---|--------------------------|------------------|-----------|:------:|-----------------------------------------------|------------------|
| 1 | TRAN-ID                  | X(16)            | Alpha     |   16   | Unique transaction identifier                 | `String`         |
| 2 | TRAN-TYPE-CD             | X(02)            | Alpha     |    2   | Transaction type code (FK → Tran Type)        | `String`         |
| 3 | TRAN-CAT-CD              | 9(04)            | Numeric   |    4   | Transaction category code                     | `int`            |
| 4 | TRAN-SOURCE              | X(10)            | Alpha     |   10   | Originating source/channel                    | `String`         |
| 5 | TRAN-DESC                | X(100)           | Alpha     |  100   | Transaction description                       | `String`         |
| 6 | TRAN-AMT                 | S9(09)V99        | Signed Dec|   11   | Transaction amount                            | `BigDecimal`     |
| 7 | TRAN-MERCHANT-ID         | 9(09)            | Numeric   |    9   | Merchant identifier                           | `long`           |
| 8 | TRAN-MERCHANT-NAME       | X(50)            | Alpha     |   50   | Merchant name                                 | `String`         |
| 9 | TRAN-MERCHANT-CITY       | X(50)            | Alpha     |   50   | Merchant city                                 | `String`         |
|10 | TRAN-MERCHANT-ZIP        | X(10)            | Alpha     |   10   | Merchant ZIP code                             | `String`         |
|11 | TRAN-CARD-NUM            | X(16)            | Alpha     |   16   | Card number used                              | `String`         |
|12 | TRAN-ORIG-TS             | X(26)            | Alpha     |   26   | Origination timestamp                         | `LocalDateTime`  |
|13 | TRAN-PROC-TS             | X(26)            | Alpha     |   26   | Processing timestamp                          | `LocalDateTime`  |
|14 | FILLER                   | X(20)            | —         |   20   | Reserved space                                | —                |

---

## 6. Daily Transaction Entity (`CVTRA06Y`) — 350 bytes

**Business Description:** Inbound daily transaction feed — same layout as Transaction but uses DALYTRAN- prefix. Processed by batch posting and then merged into the master transaction file.

| # | Field Name               | PIC Clause       | Type      | Length | Business Meaning                              |
|---|--------------------------|------------------|-----------|:------:|-----------------------------------------------|
| 1 | DALYTRAN-ID              | X(16)            | Alpha     |   16   | Daily transaction ID                          |
| 2 | DALYTRAN-TYPE-CD         | X(02)            | Alpha     |    2   | Transaction type code                         |
| 3 | DALYTRAN-CAT-CD          | 9(04)            | Numeric   |    4   | Category code                                 |
| 4 | DALYTRAN-SOURCE          | X(10)            | Alpha     |   10   | Source channel                                |
| 5 | DALYTRAN-DESC            | X(100)           | Alpha     |  100   | Description                                   |
| 6 | DALYTRAN-AMT             | S9(09)V99        | Signed Dec|   11   | Amount                                        |
| 7 | DALYTRAN-MERCHANT-ID     | 9(09)            | Numeric   |    9   | Merchant ID                                   |
| 8 | DALYTRAN-MERCHANT-NAME   | X(50)            | Alpha     |   50   | Merchant name                                 |
| 9 | DALYTRAN-MERCHANT-CITY   | X(50)            | Alpha     |   50   | Merchant city                                 |
|10 | DALYTRAN-MERCHANT-ZIP    | X(10)            | Alpha     |   10   | Merchant ZIP                                  |
|11 | DALYTRAN-CARD-NUM        | X(16)            | Alpha     |   16   | Card number                                   |
|12 | DALYTRAN-ORIG-TS         | X(26)            | Alpha     |   26   | Original timestamp                            |
|13 | DALYTRAN-PROC-TS         | X(26)            | Alpha     |   26   | Processing timestamp                          |
|14 | FILLER                   | X(20)            | —         |   20   | Reserved                                      |

---

## 7. Transaction Category Balance (`CVTRA01Y`) — 50 bytes

**Business Description:** Running balance by account, transaction type, and category. Updated during posting and used for interest calculation.

| # | Field Name               | PIC Clause       | Type      | Length | Business Meaning                              |
|---|--------------------------|------------------|-----------|:------:|-----------------------------------------------|
| 1 | TRANCAT-ACCT-ID          | 9(11)            | Numeric   |   11   | Account ID (FK → Account)                     |
| 2 | TRANCAT-TYPE-CD          | X(02)            | Alpha     |    2   | Transaction type code                         |
| 3 | TRANCAT-CD               | 9(04)            | Numeric   |    4   | Category code                                 |
| 4 | TRAN-CAT-BAL             | S9(09)V99        | Signed Dec|   11   | Running balance for this category             |
| 5 | FILLER                   | X(22)            | —         |   22   | Reserved                                      |

---

## 8. Disclosure Group / Interest Rate (`CVTRA02Y`) — 50 bytes

**Business Description:** Interest rate configuration by account group, transaction type, and category.

| # | Field Name               | PIC Clause       | Type      | Length | Business Meaning                              |
|---|--------------------------|------------------|-----------|:------:|-----------------------------------------------|
| 1 | DIS-ACCT-GROUP-ID        | X(10)            | Alpha     |   10   | Account group identifier                      |
| 2 | DIS-TRAN-TYPE-CD         | X(02)            | Alpha     |    2   | Transaction type code                         |
| 3 | DIS-TRAN-CAT-CD          | 9(04)            | Numeric   |    4   | Transaction category code                     |
| 4 | DIS-INT-RATE             | S9(04)V99        | Signed Dec|    6   | Interest rate (APR percentage)                |
| 5 | FILLER                   | X(28)            | —         |   28   | Reserved                                      |

---

## 9. Transaction Type (`CVTRA03Y`) — 60 bytes

**Business Description:** Reference table for transaction type codes (e.g., Purchase, Cash Advance, Payment).

| # | Field Name               | PIC Clause       | Type      | Length | Business Meaning                              |
|---|--------------------------|------------------|-----------|:------:|-----------------------------------------------|
| 1 | TRAN-TYPE                | X(02)            | Alpha     |    2   | Transaction type code (PK)                    |
| 2 | TRAN-TYPE-DESC           | X(50)            | Alpha     |   50   | Description                                   |
| 3 | FILLER                   | X(08)            | —         |    8   | Reserved                                      |

---

## 10. Transaction Category (`CVTRA04Y`) — 60 bytes

**Business Description:** Reference table for transaction category codes within a type.

| # | Field Name               | PIC Clause       | Type      | Length | Business Meaning                              |
|---|--------------------------|------------------|-----------|:------:|-----------------------------------------------|
| 1 | TRAN-TYPE-CD             | X(02)            | Alpha     |    2   | Parent transaction type code                  |
| 2 | TRAN-CAT-CD              | 9(04)            | Numeric   |    4   | Category code                                 |
| 3 | TRAN-CAT-TYPE-DESC       | X(50)            | Alpha     |   50   | Category description                          |
| 4 | FILLER                   | X(04)            | —         |    4   | Reserved                                      |

---

## 11. User Security Entity (`CSUSR01Y`) — 80 bytes

**Business Description:** User authentication record for the CardDemo application (CICS sign-on).

| # | Field Name               | PIC Clause       | Type      | Length | Business Meaning                              |
|---|--------------------------|------------------|-----------|:------:|-----------------------------------------------|
| 1 | SEC-USR-ID               | X(08)            | Alpha     |    8   | User ID (PK)                                  |
| 2 | SEC-USR-FNAME            | X(20)            | Alpha     |   20   | First name                                    |
| 3 | SEC-USR-LNAME            | X(20)            | Alpha     |   20   | Last name                                     |
| 4 | SEC-USR-PWD              | X(08)            | Alpha     |    8   | Password (plaintext — legacy)                 |
| 5 | SEC-USR-TYPE             | X(01)            | Alpha     |    1   | User type (A=Admin, U=User)                   |
| 6 | SEC-USR-FILLER           | X(23)            | —         |   23   | Reserved                                      |

---

## 12. Statement Transaction Layout (`COSTM01`) — 350 bytes

**Business Description:** Re-keyed transaction record for statement generation, sorted by card number then transaction ID.

| # | Field Name               | PIC Clause       | Type      | Length | Business Meaning                              |
|---|--------------------------|------------------|-----------|:------:|-----------------------------------------------|
| 1 | TRNX-CARD-NUM            | X(16)            | Alpha     |   16   | Card number (primary sort key)                |
| 2 | TRNX-ID                  | X(16)            | Alpha     |   16   | Transaction ID (secondary sort key)           |
| 3 | TRNX-TYPE-CD             | X(02)            | Alpha     |    2   | Transaction type                              |
| 4 | TRNX-CAT-CD              | 9(04)            | Numeric   |    4   | Category code                                 |
| 5 | TRNX-SOURCE              | X(10)            | Alpha     |   10   | Source channel                                |
| 6 | TRNX-DESC                | X(100)           | Alpha     |  100   | Description                                   |
| 7 | TRNX-AMT                 | S9(09)V99        | Signed Dec|   11   | Amount                                        |
| 8 | TRNX-MERCHANT-ID         | 9(09)            | Numeric   |    9   | Merchant ID                                   |
| 9 | TRNX-MERCHANT-NAME       | X(50)            | Alpha     |   50   | Merchant name                                 |
|10 | TRNX-MERCHANT-CITY       | X(50)            | Alpha     |   50   | Merchant city                                 |
|11 | TRNX-MERCHANT-ZIP        | X(10)            | Alpha     |   10   | Merchant ZIP                                  |
|12 | TRNX-ORIG-TS             | X(26)            | Alpha     |   26   | Original timestamp                            |
|13 | TRNX-PROC-TS             | X(26)            | Alpha     |   26   | Processing timestamp                          |
|14 | FILLER                   | X(20)            | —         |   20   | Reserved                                      |

---

## 13. Export Record (`CVEXPORT`)

**Business Description:** Portable export/import format for data migration. Each record starts with a type code identifying the entity.

| # | Field Name               | PIC Clause       | Type      | Length | Business Meaning                              |
|---|--------------------------|------------------|-----------|:------:|-----------------------------------------------|
| 1 | EXP-DATA-HEADER          | X(01)            | Alpha     |    1   | Record type (C=Customer, A=Account, X=Xref, T=Transaction, D=Card) |
| 2 | _Followed by entity-specific fields matching the original copybook layout_ | | | | |

### Export Sub-Record: Customer (Header = 'C')
| Field | PIC | Length | Meaning |
|-------|-----|:------:|---------|
| EXP-CUST-ID | 9(09) | 9 | Customer ID |
| EXP-CUST-FIRST-NAME | X(25) | 25 | First name |
| EXP-CUST-MIDDLE-NAME | X(25) | 25 | Middle name |
| EXP-CUST-LAST-NAME | X(25) | 25 | Last name |
| _(remaining fields mirror CVCUS01Y)_ | | | |

### Export Sub-Record: Card (Header = 'D')
| Field | PIC | Length | Meaning |
|-------|-----|:------:|---------|
| EXP-CARD-NUM | X(16) | 16 | Card number |
| EXP-CARD-ACCT-ID | 9(11) COMP | 11 | Account ID |
| EXP-CARD-CVV-CD | 9(03) COMP | 3 | CVV |
| EXP-CARD-EMBOSSED-NAME | X(50) | 50 | Embossed name |
| EXP-CARD-EXPIRAION-DATE | X(10) | 10 | Expiration date |
| EXP-CARD-ACTIVE-STATUS | X(01) | 1 | Active status |

---

## 14. Report Layout (`CVTRA07Y`)

**Business Description:** Print layout for the Daily Transaction Report generated by CBTRN03C.

| Structure | Fields | Purpose |
|-----------|--------|---------|
| REPORT-NAME-HEADER | REPT-SHORT-NAME, REPT-LONG-NAME, date range | Report header line |
| TRANSACTION-HEADER-1 | Column titles | Column header row |
| TRANSACTION-HEADER-2 | Dashes (133 chars) | Separator line |
| TRANSACTION-DETAIL-REPORT | Trans ID, Account ID, Type, Category, Source, Amount | Detail line |
| REPORT-PAGE-TOTALS | REPT-PAGE-TOTAL | Page subtotal |
| REPORT-ACCOUNT-TOTALS | REPT-ACCOUNT-TOTAL | Account subtotal |
| REPORT-GRAND-TOTALS | REPT-GRAND-TOTAL | Grand total |

---

## 15. Shared / System Copybooks

| Copybook   | Purpose                                           | Key Fields                                    |
|------------|---------------------------------------------------|-----------------------------------------------|
| COCOM01Y   | Common communication area (CICS COMMAREA)         | CDEMO-FROM-TRANID, CDEMO-FROM-PROGRAM, CDEMO-TO-TRANID, CDEMO-TO-PROGRAM, CDEMO-PGM-REENTER, CDEMO-ACCT-ID, CDEMO-CARD-NUM, CDEMO-CUST-ID |
| COTTL01Y   | Title/header line for all screens                 | CCDA-TITLE01, CCDA-TITLE02                    |
| CSDAT01Y   | Date work area                                    | WS-CURDATE, WS-CURTIME                        |
| CSMSG01Y   | Primary message area                              | WS-MESSAGE                                    |
| CSMSG02Y   | Secondary info message area                       | WS-INFO-MSG                                   |
| COMEN02Y   | Main menu option definitions                      | Menu option text/program/transaction arrays    |
| COADM02Y   | Admin menu option definitions                     | Admin menu option text/program/transaction arrays |
| CSSETATY   | COPY REPLACING macro for BMS field attributes     | Parameterized attribute-setting pattern        |
| CSSTRPFY   | Abend handling / send-error-text routine          | Error display paragraph                       |
| CSUTLDWY   | Date utility working-storage definitions          | Date conversion fields                        |
| CSUTLDPY   | Date utility procedure paragraph                  | Date conversion PERFORM logic                 |
| CSLKPCDY   | Lookup code table (US states, etc.)               | State code validation arrays                  |
| CODATECN   | Date conversion record for assembler call         | CODATECN-REC                                  |
| CUSTREC    | Customer record variant for statement generation  | Fields aligned with CBSTM03A needs            |
| CVCRD01Y   | Card detail working storage (online screens)      | Card display fields for BMS maps              |
| UNUSED1Y   | Placeholder / dead-code record                    | 80-byte unused record                         |

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)
  │
  ├──< Card Cross-Reference (CVACT03Y)  [CUST-ID → XREF-CUST-ID]
  │       │
  │       ├── Card (CVACT02Y)            [XREF-CARD-NUM → CARD-NUM]
  │       │
  │       └── Account (CVACT01Y)         [XREF-ACCT-ID → ACCT-ID]
  │               │
  │               ├──< Transaction (CVTRA05Y)     [via XREF lookup]
  │               │
  │               ├──< Daily Transaction (CVTRA06Y) [via XREF lookup]
  │               │
  │               ├──< Tran Category Balance (CVTRA01Y) [ACCT-ID]
  │               │
  │               └──> Disclosure Group (CVTRA02Y)  [ACCT-GROUP-ID]
  │
  └── User Security (CSUSR01Y)  [independent — app authentication]

Reference Tables:
  Transaction Type (CVTRA03Y)  ←── TRAN-TYPE-CD
  Transaction Category (CVTRA04Y) ←── TRAN-TYPE-CD + TRAN-CAT-CD
```
