# CardDemo Data Dictionary

> Business entities extracted from COBOL copybook PIC clauses, translated into a business-friendly format for modernization planning.

---

## Entity Relationship Overview

```
┌──────────┐     ┌──────────┐     ┌──────────────┐
│ CUSTOMER │1───N│ CARD-XREF│N───1│   ACCOUNT    │
│ CVCUS01Y │     │ CVACT03Y │     │  CVACT01Y    │
└──────────┘     └─────┬────┘     └──────┬───────┘
                       │                 │
                  ┌────┴─────┐     ┌─────┴────────┐
                  │   CARD   │     │ TRAN-CAT-BAL │
                  │ CVACT02Y │     │  CVTRA01Y    │
                  └────┬─────┘     └──────────────┘
                       │
              ┌────────┴──────────┐
              │   TRANSACTION     │
              │ CVTRA05Y/CVTRA06Y │
              └───────────────────┘
```

---

## 1. Customer (CVCUS01Y / CUSTREC)

**Dataset**: `AWS.M2.CARDDEMO.CUSTDATA.PS` — VSAM KSDS  
**Record Length**: 500 bytes (FB)  
**Key**: `CUST-ID` (9 digits)

| # | Field Name               | PIC Clause     | Type     | Length | Business Description                        |
|--:|:-------------------------|:---------------|:---------|-------:|:--------------------------------------------|
| 1 | CUST-ID                  | 9(09)          | Numeric  |      9 | Unique customer identifier                  |
| 2 | CUST-FIRST-NAME          | X(25)          | Alpha    |     25 | Customer first name                         |
| 3 | CUST-MIDDLE-NAME         | X(25)          | Alpha    |     25 | Customer middle name                        |
| 4 | CUST-LAST-NAME           | X(25)          | Alpha    |     25 | Customer last name                          |
| 5 | CUST-ADDR-LINE-1         | X(50)          | Alpha    |     50 | Address line 1                              |
| 6 | CUST-ADDR-LINE-2         | X(50)          | Alpha    |     50 | Address line 2                              |
| 7 | CUST-ADDR-LINE-3         | X(50)          | Alpha    |     50 | Address line 3                              |
| 8 | CUST-ADDR-STATE-CD       | X(02)          | Alpha    |      2 | US state code (validated via CSLKPCDY)      |
| 9 | CUST-ADDR-COUNTRY-CD     | X(03)          | Alpha    |      3 | Country code                                |
|10 | CUST-ADDR-ZIP            | X(10)          | Alpha    |     10 | ZIP / postal code                           |
|11 | CUST-PHONE-NUM-1         | X(15)          | Alpha    |     15 | Primary phone number                        |
|12 | CUST-PHONE-NUM-2         | X(15)          | Alpha    |     15 | Secondary phone number                      |
|13 | CUST-SSN                 | 9(09)          | Numeric  |      9 | Social Security Number (sensitive PII)      |
|14 | CUST-GOVT-ISSUED-ID      | X(20)          | Alpha    |     20 | Government-issued ID number                 |
|15 | CUST-DOB-YYYY-MM-DD      | X(10)          | Date     |     10 | Date of birth (YYYY-MM-DD format)           |
|16 | CUST-EFT-ACCOUNT-ID      | X(10)          | Alpha    |     10 | Electronic funds transfer account           |
|17 | CUST-PRI-CARD-HOLDER-IND | X(01)          | Flag     |      1 | Primary cardholder indicator (Y/N)          |
|18 | CUST-FICO-CREDIT-SCORE   | 9(03)          | Numeric  |      3 | FICO credit score (300–850)                 |
|19 | FILLER                   | X(168)         | Pad      |    168 | Reserved space                              |

---

## 2. Account (CVACT01Y)

**Dataset**: `AWS.M2.CARDDEMO.ACCTDATA.PS` — VSAM KSDS  
**Record Length**: 300 bytes (FB)  
**Key**: `ACCT-ID` (11 digits)

| # | Field Name               | PIC Clause       | Type       | Length | Business Description                      |
|--:|:-------------------------|:-----------------|:-----------|-------:|:------------------------------------------|
| 1 | ACCT-ID                  | 9(11)            | Numeric    |     11 | Unique account identifier                 |
| 2 | ACCT-ACTIVE-STATUS       | X(01)            | Flag       |      1 | Account status (Active/Inactive)          |
| 3 | ACCT-CURR-BAL            | S9(10)V99        | Signed Dec |     12 | Current balance (± up to 9,999,999,999.99)|
| 4 | ACCT-CREDIT-LIMIT        | S9(10)V99        | Signed Dec |     12 | Credit limit                              |
| 5 | ACCT-CASH-CREDIT-LIMIT   | S9(10)V99        | Signed Dec |     12 | Cash advance credit limit                 |
| 6 | ACCT-OPEN-DATE           | X(10)            | Date       |     10 | Account open date                         |
| 7 | ACCT-EXPIRAION-DATE      | X(10)            | Date       |     10 | Account expiration date                   |
| 8 | ACCT-REISSUE-DATE        | X(10)            | Date       |     10 | Card reissue date                         |
| 9 | ACCT-CURR-CYC-CREDIT     | S9(10)V99        | Signed Dec |     12 | Current cycle credit total                |
|10 | ACCT-CURR-CYC-DEBIT      | S9(10)V99        | Signed Dec |     12 | Current cycle debit total                 |
|11 | ACCT-ADDR-ZIP            | X(10)            | Alpha      |     10 | Account billing ZIP code                  |
|12 | ACCT-GROUP-ID            | X(10)            | Alpha      |     10 | Disclosure/rate group identifier          |
|13 | FILLER                   | X(178)           | Pad        |    178 | Reserved space                            |

---

## 3. Card (CVACT02Y)

**Dataset**: `AWS.M2.CARDDEMO.CARDDATA.PS` — VSAM KSDS  
**Record Length**: 150 bytes (FB)  
**Key**: `CARD-NUM` (16 chars)

| # | Field Name               | PIC Clause     | Type     | Length | Business Description                        |
|--:|:-------------------------|:---------------|:---------|-------:|:--------------------------------------------|
| 1 | CARD-NUM                 | X(16)          | Alpha    |     16 | Credit card number (PAN — sensitive PCI)    |
| 2 | CARD-ACCT-ID             | 9(11)          | Numeric  |     11 | Linked account ID (FK → ACCOUNT)            |
| 3 | CARD-CVV-CD              | 9(03)          | Numeric  |      3 | Card verification value (sensitive PCI)     |
| 4 | CARD-EMBOSSED-NAME       | X(50)          | Alpha    |     50 | Name embossed on the physical card          |
| 5 | CARD-EXPIRAION-DATE      | X(10)          | Date     |     10 | Card expiration date                        |
| 6 | CARD-ACTIVE-STATUS       | X(01)          | Flag     |      1 | Card status (Active/Inactive)               |
| 7 | FILLER                   | X(59)          | Pad      |     59 | Reserved space                              |

---

## 4. Card Cross-Reference (CVACT03Y)

**Dataset**: `AWS.M2.CARDDEMO.CARDXREF.PS` — VSAM KSDS  
**Record Length**: 50 bytes (FB)  
**Key**: `XREF-CARD-NUM` (16 chars)

| # | Field Name               | PIC Clause     | Type     | Length | Business Description                        |
|--:|:-------------------------|:---------------|:---------|-------:|:--------------------------------------------|
| 1 | XREF-CARD-NUM            | X(16)          | Alpha    |     16 | Card number (FK → CARD)                     |
| 2 | XREF-CUST-ID             | 9(09)          | Numeric  |      9 | Customer ID (FK → CUSTOMER)                 |
| 3 | XREF-ACCT-ID             | 9(11)          | Numeric  |     11 | Account ID (FK → ACCOUNT)                   |
| 4 | FILLER                   | X(14)          | Pad      |     14 | Reserved space                              |

> Central junction table linking Cards ↔ Customers ↔ Accounts.

---

## 5. Transaction (CVTRA05Y)

**Dataset**: `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS` — VSAM KSDS  
**Record Length**: 350 bytes (FB)  
**Key**: `TRAN-ID` (16 chars)

| # | Field Name               | PIC Clause     | Type       | Length | Business Description                      |
|--:|:-------------------------|:---------------|:-----------|-------:|:------------------------------------------|
| 1 | TRAN-ID                  | X(16)          | Alpha      |     16 | Unique transaction identifier             |
| 2 | TRAN-TYPE-CD             | X(02)          | Alpha      |      2 | Transaction type code (FK → TRAN-TYPE)    |
| 3 | TRAN-CAT-CD              | 9(04)          | Numeric    |      4 | Transaction category code (FK → TRAN-CAT) |
| 4 | TRAN-SOURCE              | X(10)          | Alpha      |     10 | Origination source (online/batch/etc.)    |
| 5 | TRAN-DESC                | X(100)         | Alpha      |    100 | Transaction description                   |
| 6 | TRAN-AMT                 | S9(09)V99      | Signed Dec |     11 | Transaction amount (± up to 999,999,999.99)|
| 7 | TRAN-MERCHANT-ID         | 9(09)          | Numeric    |      9 | Merchant identifier                       |
| 8 | TRAN-MERCHANT-NAME       | X(50)          | Alpha      |     50 | Merchant name                             |
| 9 | TRAN-MERCHANT-CITY       | X(50)          | Alpha      |     50 | Merchant city                             |
|10 | TRAN-MERCHANT-ZIP        | X(10)          | Alpha      |     10 | Merchant ZIP code                         |
|11 | TRAN-CARD-NUM            | X(16)          | Alpha      |     16 | Card used (FK → CARD)                     |
|12 | TRAN-ORIG-TS             | X(26)          | Timestamp  |     26 | Origination timestamp                     |
|13 | TRAN-PROC-TS             | X(26)          | Timestamp  |     26 | Processing timestamp                      |
|14 | FILLER                   | X(20)          | Pad        |     20 | Reserved space                            |

---

## 6. Daily Transaction (CVTRA06Y)

**Dataset**: `AWS.M2.CARDDEMO.DALYTRAN.PS` — Sequential  
**Record Length**: 350 bytes (FB)  
**Key**: `DALYTRAN-ID`

> Identical structure to Transaction (CVTRA05Y); holds unposted daily transactions pending batch posting.

| # | Field Name               | PIC Clause     | Type       | Length | Business Description                      |
|--:|:-------------------------|:---------------|:-----------|-------:|:------------------------------------------|
| 1 | DALYTRAN-ID              | X(16)          | Alpha      |     16 | Daily transaction identifier              |
| 2 | DALYTRAN-TYPE-CD         | X(02)          | Alpha      |      2 | Transaction type code                     |
| 3 | DALYTRAN-CAT-CD          | 9(04)          | Numeric    |      4 | Transaction category code                 |
| 4 | DALYTRAN-SOURCE          | X(10)          | Alpha      |     10 | Origination source                        |
| 5 | DALYTRAN-DESC            | X(100)         | Alpha      |     100 | Transaction description                  |
| 6 | DALYTRAN-AMT             | S9(09)V99      | Signed Dec |     11 | Transaction amount                        |
| 7 | DALYTRAN-MERCHANT-ID     | 9(09)          | Numeric    |      9 | Merchant identifier                       |
| 8 | DALYTRAN-MERCHANT-NAME   | X(50)          | Alpha      |     50 | Merchant name                             |
| 9 | DALYTRAN-MERCHANT-CITY   | X(50)          | Alpha      |     50 | Merchant city                             |
|10 | DALYTRAN-MERCHANT-ZIP    | X(10)          | Alpha      |     10 | Merchant ZIP code                         |
|11 | DALYTRAN-CARD-NUM        | X(16)          | Alpha      |     16 | Card used                                 |
|12 | DALYTRAN-ORIG-TS         | X(26)          | Timestamp  |     26 | Origination timestamp                     |
|13 | DALYTRAN-PROC-TS         | X(26)          | Timestamp  |     26 | Processing timestamp                      |
|14 | FILLER                   | X(20)          | Pad        |     20 | Reserved space                            |

---

## 7. Transaction Category Balance (CVTRA01Y)

**Dataset**: `AWS.M2.CARDDEMO.TCATBALF.PS` — VSAM KSDS  
**Record Length**: 50 bytes (FB)  
**Key**: Composite (`TRANCAT-ACCT-ID` + `TRANCAT-TYPE-CD` + `TRANCAT-CD`)

| # | Field Name               | PIC Clause     | Type       | Length | Business Description                      |
|--:|:-------------------------|:---------------|:-----------|-------:|:------------------------------------------|
| 1 | TRANCAT-ACCT-ID          | 9(11)          | Numeric    |     11 | Account ID (FK → ACCOUNT)                 |
| 2 | TRANCAT-TYPE-CD          | X(02)          | Alpha      |      2 | Transaction type code                     |
| 3 | TRANCAT-CD               | 9(04)          | Numeric    |      4 | Transaction category code                 |
| 4 | TRAN-CAT-BAL             | S9(09)V99      | Signed Dec |     11 | Running balance for this category         |
| 5 | FILLER                   | X(22)          | Pad        |     22 | Reserved space                            |

---

## 8. Disclosure Group (CVTRA02Y)

**Dataset**: `AWS.M2.CARDDEMO.DISCGRP.PS` — VSAM KSDS  
**Record Length**: 50 bytes (FB)  
**Key**: Composite (`DIS-ACCT-GROUP-ID` + `DIS-TRAN-TYPE-CD` + `DIS-TRAN-CAT-CD`)

| # | Field Name               | PIC Clause     | Type       | Length | Business Description                      |
|--:|:-------------------------|:---------------|:-----------|-------:|:------------------------------------------|
| 1 | DIS-ACCT-GROUP-ID        | X(10)          | Alpha      |     10 | Account group identifier                  |
| 2 | DIS-TRAN-TYPE-CD         | X(02)          | Alpha      |      2 | Transaction type code                     |
| 3 | DIS-TRAN-CAT-CD          | 9(04)          | Numeric    |      4 | Transaction category code                 |
| 4 | DIS-INT-RATE             | S9(04)V99      | Signed Dec |      6 | Interest rate for this disclosure group   |
| 5 | FILLER                   | X(28)          | Pad        |     28 | Reserved space                            |

> Used by the interest calculator (CBACT04C) to determine applicable rates per account group + transaction category.

---

## 9. Transaction Type (CVTRA03Y)

**Dataset**: `AWS.M2.CARDDEMO.TRANTYPE.PS` — VSAM KSDS  
**Record Length**: 60 bytes (FB)  
**Key**: `TRAN-TYPE` (2 chars)

| # | Field Name               | PIC Clause     | Type     | Length | Business Description                        |
|--:|:-------------------------|:---------------|:---------|-------:|:--------------------------------------------|
| 1 | TRAN-TYPE                | X(02)          | Alpha    |      2 | Transaction type code (e.g., "SA", "CR")    |
| 2 | TRAN-TYPE-DESC           | X(50)          | Alpha    |     50 | Human-readable type description             |
| 3 | FILLER                   | X(08)          | Pad      |      8 | Reserved space                              |

---

## 10. Transaction Category (CVTRA04Y)

**Dataset**: `AWS.M2.CARDDEMO.TRANCATG.PS` — VSAM KSDS  
**Record Length**: 60 bytes (FB)  
**Key**: Composite (`TRAN-TYPE-CD` + `TRAN-CAT-CD`)

| # | Field Name               | PIC Clause     | Type     | Length | Business Description                        |
|--:|:-------------------------|:---------------|:---------|-------:|:--------------------------------------------|
| 1 | TRAN-TYPE-CD             | X(02)          | Alpha    |      2 | Transaction type code                       |
| 2 | TRAN-CAT-CD              | 9(04)          | Numeric  |      4 | Transaction category code within type       |
| 3 | TRAN-CAT-TYPE-DESC       | X(50)          | Alpha    |     50 | Category description                        |
| 4 | FILLER                   | X(04)          | Pad      |      4 | Reserved space                              |

---

## 11. User Security (CSUSR01Y)

**Dataset**: `AWS.M2.CARDDEMO.USRSEC.PS` — VSAM KSDS  
**Record Length**: 80 bytes (FB)  
**Key**: `SEC-USR-ID` (8 chars)

| # | Field Name               | PIC Clause     | Type     | Length | Business Description                        |
|--:|:-------------------------|:---------------|:---------|-------:|:--------------------------------------------|
| 1 | SEC-USR-ID               | X(08)          | Alpha    |      8 | User login ID (e.g., ADMIN001, USER0001)    |
| 2 | SEC-USR-FNAME            | X(20)          | Alpha    |     20 | User first name                             |
| 3 | SEC-USR-LNAME            | X(20)          | Alpha    |     20 | User last name                              |
| 4 | SEC-USR-PWD              | X(08)          | Alpha    |      8 | Password (plaintext — security concern)     |
| 5 | SEC-USR-TYPE             | X(01)          | Flag     |      1 | User type: 'A' = Admin, 'U' = Regular user |
| 6 | SEC-USR-FILLER           | X(23)          | Pad      |     23 | Reserved space                              |

---

## 12. COMMAREA — Session State (COCOM01Y)

**Usage**: Passed between CICS programs via `EXEC CICS XCTL` and `EXEC CICS RETURN`  
**Total Size**: ~152 bytes

| # | Group / Field            | PIC Clause     | Type     | Length | Business Description                        |
|--:|:-------------------------|:---------------|:---------|-------:|:--------------------------------------------|
|   | **CDEMO-GENERAL-INFO**   |                |          |        |                                             |
| 1 | CDEMO-FROM-TRANID        | X(04)          | Alpha    |      4 | Originating transaction ID                  |
| 2 | CDEMO-FROM-PROGRAM       | X(08)          | Alpha    |      8 | Originating program name                    |
| 3 | CDEMO-TO-TRANID          | X(04)          | Alpha    |      4 | Target transaction ID                       |
| 4 | CDEMO-TO-PROGRAM         | X(08)          | Alpha    |      8 | Target program name                         |
| 5 | CDEMO-USER-ID            | X(08)          | Alpha    |      8 | Current user ID                             |
| 6 | CDEMO-USER-TYPE          | X(01)          | Flag     |      1 | 'A' = Admin, 'U' = User                    |
| 7 | CDEMO-PGM-CONTEXT        | 9(01)          | Numeric  |      1 | 0 = first entry, 1 = re-entry              |
|   | **CDEMO-CUSTOMER-INFO**  |                |          |        |                                             |
| 8 | CDEMO-CUST-ID            | 9(09)          | Numeric  |      9 | Current customer context                    |
| 9 | CDEMO-CUST-FNAME         | X(25)          | Alpha    |     25 | Customer first name                         |
|10 | CDEMO-CUST-MNAME         | X(25)          | Alpha    |     25 | Customer middle name                        |
|11 | CDEMO-CUST-LNAME         | X(25)          | Alpha    |     25 | Customer last name                          |
|   | **CDEMO-ACCOUNT-INFO**   |                |          |        |                                             |
|12 | CDEMO-ACCT-ID            | 9(11)          | Numeric  |     11 | Current account context                     |
|13 | CDEMO-ACCT-STATUS        | X(01)          | Flag     |      1 | Account status                              |
|   | **CDEMO-CARD-INFO**      |                |          |        |                                             |
|14 | CDEMO-CARD-NUM           | 9(16)          | Numeric  |     16 | Current card context                        |
|   | **CDEMO-MORE-INFO**      |                |          |        |                                             |
|15 | CDEMO-LAST-MAP           | X(7)           | Alpha    |      7 | Last BMS map displayed                      |
|16 | CDEMO-LAST-MAPSET        | X(7)           | Alpha    |      7 | Last BMS mapset used                        |

---

## 13. IMS Segment — Pending Authorization Detail (CIPAUDTY)

**Database**: IMS hierarchical DB (Auth module)  
**Segment Size**: ~200 bytes

| # | Field Name                 | PIC Clause       | Type       | Length | Business Description                    |
|--:|:---------------------------|:-----------------|:-----------|-------:|:----------------------------------------|
| 1 | PA-AUTH-DATE-9C            | S9(05) COMP-3    | Packed Dec |      3 | Authorization date (packed)             |
| 2 | PA-AUTH-TIME-9C            | S9(09) COMP-3    | Packed Dec |      5 | Authorization time (packed)             |
| 3 | PA-AUTH-ORIG-DATE          | X(06)            | Alpha      |      6 | Original auth date (display)            |
| 4 | PA-AUTH-ORIG-TIME          | X(06)            | Alpha      |      6 | Original auth time (display)            |
| 5 | PA-CARD-NUM                | X(16)            | Alpha      |     16 | Card number                             |
| 6 | PA-AUTH-TYPE               | X(04)            | Alpha      |      4 | Authorization type                      |
| 7 | PA-CARD-EXPIRY-DATE        | X(04)            | Alpha      |      4 | Card expiry date                        |
| 8 | PA-MESSAGE-TYPE            | X(06)            | Alpha      |      6 | Message type code                       |
| 9 | PA-MESSAGE-SOURCE          | X(06)            | Alpha      |      6 | Message source                          |
|10 | PA-AUTH-ID-CODE            | X(06)            | Alpha      |      6 | Authorization identification code       |
|11 | PA-AUTH-RESP-CODE          | X(02)            | Alpha      |      2 | Response code ('00' = approved)         |
|12 | PA-AUTH-RESP-REASON        | X(04)            | Alpha      |      4 | Response reason code                    |
|13 | PA-PROCESSING-CODE         | 9(06)            | Numeric    |      6 | Processing code                         |
|14 | PA-TRANSACTION-AMT         | S9(10)V99 COMP-3 | Packed Dec |      7 | Requested transaction amount            |
|15 | PA-APPROVED-AMT            | S9(10)V99 COMP-3 | Packed Dec |      7 | Approved amount                         |
|16 | PA-MERCHANT-CATAGORY-CODE  | X(04)            | Alpha      |      4 | Merchant category code (MCC)            |
|17 | PA-ACQR-COUNTRY-CODE       | X(03)            | Alpha      |      3 | Acquirer country code                   |
|18 | PA-POS-ENTRY-MODE          | 9(02)            | Numeric    |      2 | Point-of-sale entry mode                |
|19 | PA-MERCHANT-ID             | X(15)            | Alpha      |     15 | Merchant ID                             |
|20 | PA-MERCHANT-NAME           | X(22)            | Alpha      |     22 | Merchant name                           |
|21 | PA-MERCHANT-CITY           | X(13)            | Alpha      |     13 | Merchant city                           |
|22 | PA-MERCHANT-STATE          | X(02)            | Alpha      |      2 | Merchant state                          |
|23 | PA-MERCHANT-ZIP            | X(09)            | Alpha      |      9 | Merchant ZIP                            |
|24 | PA-TRANSACTION-ID          | X(15)            | Alpha      |     15 | Transaction reference ID                |
|25 | PA-MATCH-STATUS            | X(01)            | Flag       |      1 | P=Pending, D=Declined, E=Expired, M=Matched |
|26 | PA-AUTH-FRAUD              | X(01)            | Flag       |      1 | F=Fraud confirmed, R=Fraud removed      |
|27 | PA-FRAUD-RPT-DATE          | X(08)            | Date       |      8 | Fraud report date                       |

---

## 14. IMS Segment — Pending Authorization Summary (CIPAUSMY)

| # | Field Name                 | PIC Clause       | Type       | Length | Business Description                    |
|--:|:---------------------------|:-----------------|:-----------|-------:|:----------------------------------------|
| 1 | PA-ACCT-ID                 | S9(11) COMP-3    | Packed Dec |      7 | Account ID (packed)                     |
| 2 | PA-CUST-ID                 | 9(09)            | Numeric    |      9 | Customer ID                             |
| 3 | PA-AUTH-STATUS             | X(01)            | Flag       |      1 | Authorization status                    |
| 4 | PA-ACCOUNT-STATUS          | X(02) OCCURS 5   | Alpha      |     10 | Account status array (5 entries)        |
| 5 | PA-CREDIT-LIMIT            | S9(09)V99 COMP-3 | Packed Dec |      7 | Credit limit                            |
| 6 | PA-CASH-LIMIT              | S9(09)V99 COMP-3 | Packed Dec |      7 | Cash advance limit                      |
| 7 | PA-CREDIT-BALANCE          | S9(09)V99 COMP-3 | Packed Dec |      7 | Credit balance                          |
| 8 | PA-CASH-BALANCE            | S9(09)V99 COMP-3 | Packed Dec |      7 | Cash balance                            |
| 9 | PA-APPROVED-AUTH-CNT       | S9(04) COMP      | Binary     |      2 | Count of approved authorizations        |
|10 | PA-DECLINED-AUTH-CNT       | S9(04) COMP      | Binary     |      2 | Count of declined authorizations        |
|11 | PA-APPROVED-AUTH-AMT       | S9(09)V99 COMP-3 | Packed Dec |      7 | Total approved amount                   |
|12 | PA-DECLINED-AUTH-AMT       | S9(09)V99 COMP-3 | Packed Dec |      7 | Total declined amount                   |

---

## 15. MQ Message — Auth Request (CCPAURQY)

| # | Field Name                 | PIC Clause     | Type     | Length | Business Description                      |
|--:|:---------------------------|:---------------|:---------|-------:|:------------------------------------------|
| 1 | PA-RQ-AUTH-DATE            | X(06)          | Alpha    |      6 | Authorization request date                |
| 2 | PA-RQ-AUTH-TIME            | X(06)          | Alpha    |      6 | Authorization request time                |
| 3 | PA-RQ-CARD-NUM             | X(16)          | Alpha    |     16 | Card number                               |
| 4 | PA-RQ-AUTH-TYPE            | X(04)          | Alpha    |      4 | Authorization type                        |
| 5 | PA-RQ-CARD-EXPIRY-DATE     | X(04)          | Alpha    |      4 | Card expiry                               |
| 6 | PA-RQ-MESSAGE-TYPE         | X(06)          | Alpha    |      6 | Message type                              |
| 7 | PA-RQ-MESSAGE-SOURCE       | X(06)          | Alpha    |      6 | Message source                            |
| 8 | PA-RQ-PROCESSING-CODE      | 9(06)          | Numeric  |      6 | Processing code                           |
| 9 | PA-RQ-TRANSACTION-AMT      | S9(10)V99      | Signed Dec|    12 | Requested amount                          |
|10 | PA-RQ-MERCHANT-CAT-CODE    | X(04)          | Alpha    |      4 | MCC                                       |
|11 | PA-RQ-ACQR-COUNTRY-CODE    | X(03)          | Alpha    |      3 | Acquirer country                          |
|12 | PA-RQ-POS-ENTRY-MODE       | 9(02)          | Numeric  |      2 | POS entry mode                            |
|13 | PA-RQ-MERCHANT-ID          | X(15)          | Alpha    |     15 | Merchant ID                               |
|14 | PA-RQ-MERCHANT-NAME        | X(22)          | Alpha    |     22 | Merchant name                             |
|15 | PA-RQ-MERCHANT-CITY        | X(13)          | Alpha    |     13 | Merchant city                             |
|16 | PA-RQ-MERCHANT-STATE       | X(02)          | Alpha    |      2 | Merchant state                            |
|17 | PA-RQ-MERCHANT-ZIP         | X(09)          | Alpha    |      9 | Merchant ZIP                              |
|18 | PA-RQ-TRANSACTION-ID       | X(15)          | Alpha    |     15 | Transaction ID                            |

---

## 16. Export Record (CVEXPORT)

**Record Length**: 500 bytes — Multi-type record with REDEFINES  
**Used by**: CBEXPORT (write), CBIMPORT (read)

### Header Fields (common to all record types)

| # | Field Name               | PIC Clause       | Type     | Length | Business Description                      |
|--:|:-------------------------|:-----------------|:---------|-------:|:------------------------------------------|
| 1 | EXPORT-REC-TYPE          | X(1)             | Flag     |      1 | Record type (C=Cust, A=Acct, T=Tran, X=Xref, D=Card) |
| 2 | EXPORT-TIMESTAMP         | X(26)            | Timestamp|     26 | Export timestamp                          |
| 3 | EXPORT-SEQUENCE-NUM      | 9(9) COMP        | Binary   |      4 | Record sequence number                    |
| 4 | EXPORT-BRANCH-ID         | X(4)             | Alpha    |      4 | Source branch ID                          |
| 5 | EXPORT-REGION-CODE       | X(5)             | Alpha    |      5 | Source region code                        |
| 6 | EXPORT-RECORD-DATA       | X(460)           | Alpha    |    460 | Payload (REDEFINES per record type)       |

> Data portion uses COMP and COMP-3 packed decimals for storage optimization — important for migration tooling to handle binary formats correctly.

---

## Data Type Legend

| PIC Pattern       | COBOL Type     | Java Equivalent   | Notes                              |
|:------------------|:---------------|:------------------|:-----------------------------------|
| X(n)              | Alphanumeric   | String            | Fixed-length, space-padded         |
| 9(n)              | Unsigned Numeric| long / BigDecimal| Display numeric (zoned decimal)    |
| S9(n)V99          | Signed Decimal | BigDecimal        | 2 implied decimal places           |
| S9(n)V99 COMP-3   | Packed Decimal | BigDecimal        | BCD encoding, ~half the bytes      |
| S9(n) COMP        | Binary Integer | int / long        | 2 or 4 bytes depending on precision|
| 9(n) COMP         | Unsigned Binary| int / long        | Binary storage                     |

---

## Sensitive Data Fields (PII / PCI)

| Entity   | Field                    | Sensitivity  | Migration Note                           |
|:---------|:-------------------------|:-------------|:-----------------------------------------|
| Customer | CUST-SSN                 | PII / SSN    | Must be encrypted at rest and in transit |
| Customer | CUST-DOB-YYYY-MM-DD      | PII          | Date of birth — restrict access          |
| Customer | CUST-GOVT-ISSUED-ID      | PII          | Government ID                            |
| Card     | CARD-NUM                 | PCI-DSS      | Primary Account Number — tokenize        |
| Card     | CARD-CVV-CD              | PCI-DSS      | Never store post-authorization           |
| User     | SEC-USR-PWD              | Credential   | Plaintext! Must hash in target system    |

---

*Generated from static analysis of CardDemo copybooks.*
