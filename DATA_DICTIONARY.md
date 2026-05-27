# CardDemo Data Dictionary

> Business-friendly extraction of all data entities, their fields, data types, and storage characteristics derived from COBOL copybook PIC clauses.

---

## 1. Account Entity

**Copybook:** `CVACT01Y.cpy` · **Record Length:** 300 bytes · **Storage:** VSAM KSDS · **Dataset:** `AWS.M2.CARDDEMO.ACCTDATA`

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| Account ID | ACCT-ID | PIC 9(11) | Numeric | 11 | Unique account identifier (primary key) |
| Active Status | ACCT-ACTIVE-STATUS | PIC X(01) | Alpha | 1 | Account status flag (active/inactive) |
| Current Balance | ACCT-CURR-BAL | PIC S9(10)V99 | Signed Decimal | 12.2 | Current outstanding balance |
| Credit Limit | ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12.2 | Maximum authorized credit |
| Cash Credit Limit | ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 | Signed Decimal | 12.2 | Maximum cash advance limit |
| Open Date | ACCT-OPEN-DATE | PIC X(10) | Date String | 10 | Account opening date |
| Expiration Date | ACCT-EXPIRAION-DATE | PIC X(10) | Date String | 10 | Account expiration date |
| Reissue Date | ACCT-REISSUE-DATE | PIC X(10) | Date String | 10 | Last card reissue date |
| Current Cycle Credit | ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Signed Decimal | 12.2 | Credits posted in current cycle |
| Current Cycle Debit | ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 | Signed Decimal | 12.2 | Debits posted in current cycle |
| ZIP Code | ACCT-ADDR-ZIP | PIC X(10) | Alpha | 10 | Account billing ZIP code |
| Group ID | ACCT-GROUP-ID | PIC X(10) | Alpha | 10 | Disclosure/rate group identifier |
| Filler | FILLER | PIC X(178) | Reserved | 178 | Reserved for future use |

---

## 2. Card Entity

**Copybook:** `CVACT02Y.cpy` · **Record Length:** 150 bytes · **Storage:** VSAM KSDS · **Dataset:** `AWS.M2.CARDDEMO.CARDDATA`

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| Card Number | CARD-NUM | PIC X(16) | Alpha | 16 | Credit card number (primary key) |
| Account ID | CARD-ACCT-ID | PIC 9(11) | Numeric | 11 | Linked account identifier (FK → Account) |
| CVV Code | CARD-CVV-CD | PIC 9(03) | Numeric | 3 | Card verification value |
| Embossed Name | CARD-EMBOSSED-NAME | PIC X(50) | Alpha | 50 | Name printed on physical card |
| Expiration Date | CARD-EXPIRAION-DATE | PIC X(10) | Date String | 10 | Card expiration date |
| Active Status | CARD-ACTIVE-STATUS | PIC X(01) | Alpha | 1 | Card status flag |
| Filler | FILLER | PIC X(59) | Reserved | 59 | Reserved for future use |

---

## 3. Customer Entity

**Copybook:** `CVCUS01Y.cpy` · **Record Length:** 500 bytes · **Storage:** VSAM KSDS · **Dataset:** `AWS.M2.CARDDEMO.CUSTDATA`

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| Customer ID | CUST-ID | PIC 9(09) | Numeric | 9 | Unique customer identifier (primary key) |
| First Name | CUST-FIRST-NAME | PIC X(25) | Alpha | 25 | Customer first name |
| Middle Name | CUST-MIDDLE-NAME | PIC X(25) | Alpha | 25 | Customer middle name |
| Last Name | CUST-LAST-NAME | PIC X(25) | Alpha | 25 | Customer last name |
| Address Line 1 | CUST-ADDR-LINE-1 | PIC X(50) | Alpha | 50 | Primary address line |
| Address Line 2 | CUST-ADDR-LINE-2 | PIC X(50) | Alpha | 50 | Secondary address line |
| Address Line 3 | CUST-ADDR-LINE-3 | PIC X(50) | Alpha | 50 | Tertiary address line |
| State Code | CUST-ADDR-STATE-CD | PIC X(02) | Alpha | 2 | US state abbreviation |
| Country Code | CUST-ADDR-COUNTRY-CD | PIC X(03) | Alpha | 3 | ISO country code |
| ZIP Code | CUST-ADDR-ZIP | PIC X(10) | Alpha | 10 | Postal/ZIP code |
| Phone Number 1 | CUST-PHONE-NUM-1 | PIC X(15) | Alpha | 15 | Primary phone number |
| Phone Number 2 | CUST-PHONE-NUM-2 | PIC X(15) | Alpha | 15 | Secondary phone number |
| SSN | CUST-SSN | PIC 9(09) | Numeric | 9 | Social Security Number (PII) |
| Government ID | CUST-GOVT-ISSUED-ID | PIC X(20) | Alpha | 20 | Government-issued identification |
| Date of Birth | CUST-DOB-YYYY-MM-DD | PIC X(10) | Date String | 10 | Customer date of birth |
| EFT Account ID | CUST-EFT-ACCOUNT-ID | PIC X(10) | Alpha | 10 | Electronic funds transfer account |
| Primary Cardholder | CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alpha | 1 | Primary cardholder indicator |
| FICO Credit Score | CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric | 3 | Credit score (300–850) |
| Filler | FILLER | PIC X(168) | Reserved | 168 | Reserved for future use |

---

## 4. Card Cross-Reference Entity

**Copybook:** `CVACT03Y.cpy` · **Record Length:** 50 bytes · **Storage:** VSAM KSDS · **Dataset:** `AWS.M2.CARDDEMO.CARDXREF`

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| Card Number | XREF-CARD-NUM | PIC X(16) | Alpha | 16 | Credit card number (primary key) |
| Customer ID | XREF-CUST-ID | PIC 9(09) | Numeric | 9 | Customer identifier (FK → Customer) |
| Account ID | XREF-ACCT-ID | PIC 9(11) | Numeric | 11 | Account identifier (FK → Account) |
| Filler | FILLER | PIC X(14) | Reserved | 14 | Reserved for future use |

---

## 5. Transaction Entity

**Copybook:** `CVTRA05Y.cpy` · **Record Length:** 350 bytes · **Storage:** VSAM KSDS · **Dataset:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| Transaction ID | TRAN-ID | PIC X(16) | Alpha | 16 | Unique transaction identifier |
| Type Code | TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code (FK → Tran Type) |
| Category Code | TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code |
| Source | TRAN-SOURCE | PIC X(10) | Alpha | 10 | Transaction origination source |
| Description | TRAN-DESC | PIC X(100) | Alpha | 100 | Transaction narrative |
| Amount | TRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11.2 | Transaction amount |
| Merchant ID | TRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant identifier |
| Merchant Name | TRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant business name |
| Merchant City | TRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant city |
| Merchant ZIP | TRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant postal code |
| Card Number | TRAN-CARD-NUM | PIC X(16) | Alpha | 16 | Card used for transaction |
| Origination Timestamp | TRAN-ORIG-TS | PIC X(26) | Timestamp | 26 | When transaction was initiated |
| Processing Timestamp | TRAN-PROC-TS | PIC X(26) | Timestamp | 26 | When transaction was processed |
| Filler | FILLER | PIC X(20) | Reserved | 20 | Reserved for future use |

---

## 6. Daily Transaction Entity

**Copybook:** `CVTRA06Y.cpy` · **Record Length:** 350 bytes · **Storage:** Sequential PS · **Dataset:** `AWS.M2.CARDDEMO.DALYTRAN.PS`

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| Transaction ID | DALYTRAN-ID | PIC X(16) | Alpha | 16 | Daily batch transaction identifier |
| Type Code | DALYTRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code |
| Category Code | DALYTRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code |
| Source | DALYTRAN-SOURCE | PIC X(10) | Alpha | 10 | Origination source |
| Description | DALYTRAN-DESC | PIC X(100) | Alpha | 100 | Transaction narrative |
| Amount | DALYTRAN-AMT | PIC S9(09)V99 | Signed Decimal | 11.2 | Transaction amount |
| Merchant ID | DALYTRAN-MERCHANT-ID | PIC 9(09) | Numeric | 9 | Merchant identifier |
| Merchant Name | DALYTRAN-MERCHANT-NAME | PIC X(50) | Alpha | 50 | Merchant business name |
| Merchant City | DALYTRAN-MERCHANT-CITY | PIC X(50) | Alpha | 50 | Merchant city |
| Merchant ZIP | DALYTRAN-MERCHANT-ZIP | PIC X(10) | Alpha | 10 | Merchant postal code |
| Card Number | DALYTRAN-CARD-NUM | PIC X(16) | Alpha | 16 | Card used for transaction |
| Origination Timestamp | DALYTRAN-ORIG-TS | PIC X(26) | Timestamp | 26 | When transaction was initiated |
| Processing Timestamp | DALYTRAN-PROC-TS | PIC X(26) | Timestamp | 26 | When transaction was processed |
| Filler | FILLER | PIC X(20) | Reserved | 20 | Reserved for future use |

---

## 7. Transaction Type Entity

**Copybook:** `CVTRA03Y.cpy` · **Record Length:** 60 bytes · **Storage:** VSAM KSDS · **Dataset:** `AWS.M2.CARDDEMO.TRANTYPE`

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| Type Code | TRAN-TYPE | PIC X(02) | Alpha | 2 | Transaction type code (primary key) |
| Description | TRAN-TYPE-DESC | PIC X(50) | Alpha | 50 | Human-readable type description |
| Filler | FILLER | PIC X(08) | Reserved | 8 | Reserved for future use |

---

## 8. Transaction Category Type Entity

**Copybook:** `CVTRA04Y.cpy` · **Record Length:** 60 bytes · **Storage:** VSAM KSDS · **Dataset:** `AWS.M2.CARDDEMO.TRANCATG`

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| Type Code | TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code (composite key part 1) |
| Category Code | TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Transaction category code (composite key part 2) |
| Description | TRAN-CAT-TYPE-DESC | PIC X(50) | Alpha | 50 | Category description |
| Filler | FILLER | PIC X(04) | Reserved | 4 | Reserved for future use |

---

## 9. Transaction Category Balance Entity

**Copybook:** `CVTRA01Y.cpy` · **Record Length:** 50 bytes · **Storage:** VSAM KSDS · **Dataset:** `AWS.M2.CARDDEMO.TCATBALF`

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| Account ID | TRANCAT-ACCT-ID | PIC 9(11) | Numeric | 11 | Account identifier (composite key part 1) |
| Type Code | TRANCAT-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code (composite key part 2) |
| Category Code | TRANCAT-CD | PIC 9(04) | Numeric | 4 | Category code (composite key part 3) |
| Balance | TRAN-CAT-BAL | PIC S9(09)V99 | Signed Decimal | 11.2 | Running balance for this category |
| Filler | FILLER | PIC X(22) | Reserved | 22 | Reserved for future use |

---

## 10. Disclosure Group Entity

**Copybook:** `CVTRA02Y.cpy` · **Record Length:** 50 bytes · **Storage:** VSAM KSDS · **Dataset:** `AWS.M2.CARDDEMO.DISCGRP`

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| Account Group ID | DIS-ACCT-GROUP-ID | PIC X(10) | Alpha | 10 | Account group identifier (composite key part 1) |
| Type Code | DIS-TRAN-TYPE-CD | PIC X(02) | Alpha | 2 | Transaction type code (composite key part 2) |
| Category Code | DIS-TRAN-CAT-CD | PIC 9(04) | Numeric | 4 | Category code (composite key part 3) |
| Interest Rate | DIS-INT-RATE | PIC S9(04)V99 | Signed Decimal | 6.2 | Interest rate for this group/type/category |
| Filler | FILLER | PIC X(28) | Reserved | 28 | Reserved for future use |

---

## 11. User Security Entity

**Copybook:** `CSUSR01Y.cpy` · **Record Length:** 80 bytes · **Storage:** VSAM KSDS · **Dataset:** `AWS.M2.CARDDEMO.USRSEC`

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| User ID | SEC-USR-ID | PIC X(08) | Alpha | 8 | Login user identifier (primary key) |
| First Name | SEC-USR-FNAME | PIC X(20) | Alpha | 20 | User first name |
| Last Name | SEC-USR-LNAME | PIC X(20) | Alpha | 20 | User last name |
| Password | SEC-USR-PWD | PIC X(08) | Alpha | 8 | User password (stored in clear text) |
| User Type | SEC-USR-TYPE | PIC X(01) | Alpha | 1 | User role: 'A' = Admin, 'U' = Regular |
| Filler | SEC-USR-FILLER | PIC X(23) | Reserved | 23 | Reserved for future use |

---

## 12. COMMAREA (Inter-Program Communication)

**Copybook:** `COCOM01Y.cpy` · **Purpose:** Shared communication area passed between CICS programs

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| From Transaction ID | CDEMO-FROM-TRANID | PIC X(04) | Alpha | 4 | Originating CICS transaction |
| From Program | CDEMO-FROM-PROGRAM | PIC X(08) | Alpha | 8 | Originating program name |
| To Transaction ID | CDEMO-TO-TRANID | PIC X(04) | Alpha | 4 | Target CICS transaction |
| To Program | CDEMO-TO-PROGRAM | PIC X(08) | Alpha | 8 | Target program name |
| User ID | CDEMO-USER-ID | PIC X(08) | Alpha | 8 | Authenticated user ID |
| User Type | CDEMO-USER-TYPE | PIC X(01) | Alpha | 1 | 'A' = Admin, 'U' = User |
| Program Context | CDEMO-PGM-CONTEXT | PIC 9(01) | Numeric | 1 | 0 = first entry, 1 = re-entry |
| Customer ID | CDEMO-CUST-ID | PIC 9(09) | Numeric | 9 | Current customer context |
| Customer First Name | CDEMO-CUST-FNAME | PIC X(25) | Alpha | 25 | Customer first name |
| Customer Middle Name | CDEMO-CUST-MNAME | PIC X(25) | Alpha | 25 | Customer middle name |
| Customer Last Name | CDEMO-CUST-LNAME | PIC X(25) | Alpha | 25 | Customer last name |
| Account ID | CDEMO-ACCT-ID | PIC 9(11) | Numeric | 11 | Current account context |
| Account Status | CDEMO-ACCT-STATUS | PIC X(01) | Alpha | 1 | Account status |
| Card Number | CDEMO-CARD-NUM | PIC 9(16) | Numeric | 16 | Current card context |
| Last Map | CDEMO-LAST-MAP | PIC X(7) | Alpha | 7 | Last BMS map displayed |
| Last Mapset | CDEMO-LAST-MAPSET | PIC X(7) | Alpha | 7 | Last BMS mapset |

---

## 13. Multi-Record Export Layout

**Copybook:** `CVEXPORT.cpy` · **Record Length:** 500 bytes · **Purpose:** Branch migration data interchange

### Header Fields (common to all record types)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| Record Type | EXPORT-REC-TYPE | PIC X(1) | Alpha | 1 | C=Customer, A=Account, T=Transaction, X=Xref |
| Timestamp | EXPORT-TIMESTAMP | PIC X(26) | Timestamp | 26 | Export generation timestamp |
| Sequence Number | EXPORT-SEQUENCE-NUM | PIC 9(9) COMP | Binary | 4 | Sequential record counter |
| Branch ID | EXPORT-BRANCH-ID | PIC X(4) | Alpha | 4 | Originating branch |
| Region Code | EXPORT-REGION-CODE | PIC X(5) | Alpha | 5 | Geographic region code |

### Storage Optimization Notes
- COMP-3 (packed decimal) fields used for: `EXP-CUST-ID`, `EXP-ACCT-CURR-BAL`, `EXP-ACCT-CASH-CREDIT-LIMIT`, `EXP-CUST-FICO-CREDIT-SCORE`, `EXP-TRAN-AMT`
- COMP (binary) fields used for: `EXPORT-SEQUENCE-NUM`, `EXP-TRAN-MERCHANT-ID`, `EXP-ACCT-CURR-CYC-DEBIT`
- OCCURS clause for: `EXP-CUST-ADDR-LINES` (3 times), `EXP-CUST-PHONE-NUMS` (2 times)
- REDEFINES used to overlay Customer, Account, Transaction, and XREF layouts on the same 460-byte data area

---

## 14. Pending Authorization Request (IMS-DB2-MQ Extension)

**Copybook:** `CCPAURQY.cpy` · **Purpose:** MQ message for card authorization requests

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| Auth Date | PA-RQ-AUTH-DATE | PIC X(06) | Date | 6 | Authorization request date |
| Auth Time | PA-RQ-AUTH-TIME | PIC X(06) | Time | 6 | Authorization request time |
| Card Number | PA-RQ-CARD-NUM | PIC X(16) | Alpha | 16 | Card being authorized |
| Auth Type | PA-RQ-AUTH-TYPE | PIC X(04) | Alpha | 4 | Type of authorization |
| Card Expiry | PA-RQ-CARD-EXPIRY-DATE | PIC X(04) | Date | 4 | Card expiry (MMYY) |
| Message Type | PA-RQ-MESSAGE-TYPE | PIC X(06) | Alpha | 6 | ISO 8583 message type |
| Message Source | PA-RQ-MESSAGE-SOURCE | PIC X(06) | Alpha | 6 | Originating system |
| Processing Code | PA-RQ-PROCESSING-CODE | PIC 9(06) | Numeric | 6 | ISO 8583 processing code |
| Transaction Amount | PA-RQ-TRANSACTION-AMT | PIC +9(10).99 | Edited Numeric | 14 | Requested authorization amount |
| Merchant Category | PA-RQ-MERCHANT-CATAGORY-CODE | PIC X(04) | Alpha | 4 | MCC code |
| Acquirer Country | PA-RQ-ACQR-COUNTRY-CODE | PIC X(03) | Alpha | 3 | Acquirer country |
| POS Entry Mode | PA-RQ-POS-ENTRY-MODE | PIC 9(02) | Numeric | 2 | Point-of-sale entry method |
| Merchant ID | PA-RQ-MERCHANT-ID | PIC X(15) | Alpha | 15 | Merchant identifier |
| Merchant Name | PA-RQ-MERCHANT-NAME | PIC X(22) | Alpha | 22 | Merchant name |
| Merchant City | PA-RQ-MERCHANT-CITY | PIC X(13) | Alpha | 13 | Merchant city |
| Merchant State | PA-RQ-MERCHANT-STATE | PIC X(02) | Alpha | 2 | Merchant state |
| Merchant ZIP | PA-RQ-MERCHANT-ZIP | PIC X(09) | Alpha | 9 | Merchant postal code |
| Transaction ID | PA-RQ-TRANSACTION-ID | PIC X(15) | Alpha | 15 | Authorization transaction ID |

---

## 15. Pending Authorization Response (IMS-DB2-MQ Extension)

**Copybook:** `CCPAURLY.cpy` · **Purpose:** MQ message for authorization responses

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| Card Number | PA-RL-CARD-NUM | PIC X(16) | Alpha | 16 | Card that was authorized |
| Transaction ID | PA-RL-TRANSACTION-ID | PIC X(15) | Alpha | 15 | Authorization transaction ID |
| Auth ID Code | PA-RL-AUTH-ID-CODE | PIC X(06) | Alpha | 6 | Authorization identification code |
| Response Code | PA-RL-AUTH-RESP-CODE | PIC X(02) | Alpha | 2 | Authorization response (approve/decline) |
| Response Reason | PA-RL-AUTH-RESP-REASON | PIC X(04) | Alpha | 4 | Decline reason code |
| Approved Amount | PA-RL-APPROVED-AMT | PIC +9(10).99 | Edited Numeric | 14 | Approved authorization amount |

---

## 16. Error Log Record (IMS-DB2-MQ Extension)

**Copybook:** `CCPAUERY.cpy` · **Purpose:** Structured error/audit logging

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| Date | ERR-DATE | PIC X(06) | Date | 6 | Error occurrence date |
| Time | ERR-TIME | PIC X(06) | Time | 6 | Error occurrence time |
| Application | ERR-APPLICATION | PIC X(08) | Alpha | 8 | Application identifier |
| Program | ERR-PROGRAM | PIC X(08) | Alpha | 8 | Program that generated error |
| Location | ERR-LOCATION | PIC X(04) | Alpha | 4 | Code location/paragraph |
| Level | ERR-LEVEL | PIC X(01) | Alpha | 1 | L=Log, I=Info, W=Warning, C=Critical |
| Subsystem | ERR-SUBSYSTEM | PIC X(01) | Alpha | 1 | A=App, C=CICS, I=IMS, D=DB2, M=MQ, F=File |
| Error Code 1 | ERR-CODE-1 | PIC X(09) | Alpha | 9 | Primary error/return code |
| Error Code 2 | ERR-CODE-2 | PIC X(09) | Alpha | 9 | Secondary error/reason code |
| Error Message | ERR-MESSAGE | PIC X(50) | Alpha | 50 | Human-readable error description |
| Event Key | ERR-EVENT-KEY | PIC X(20) | Alpha | 20 | Business key for error correlation |

---

## 17. Date Conversion Record

**Copybook:** `CODATECN.cpy` · **Purpose:** Date format transformation utility

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|:------|:-----------|:-----------|:-----|-----:|:---------------------|
| Input Type | CODATECN-TYPE | PIC X | Alpha | 1 | "1" = YYYYMMDD, "2" = YYYY-MM-DD |
| Input Date | CODATECN-INP-DATE | PIC X(20) | Alpha | 20 | Date string to convert |
| Output Type | CODATECN-OUTTYPE | PIC X | Alpha | 1 | "1" = YYYY-MM-DD, "2" = YYYYMMDD |
| Output Date | CODATECN-0UT-DATE | PIC X(20) | Alpha | 20 | Converted date string |
| Error Message | CODATECN-ERROR-MSG | PIC X(38) | Alpha | 38 | Conversion error description |

**Note:** Uses REDEFINES to overlay different date formats on input and output fields.

---

## Entity Relationship Summary

```
Customer (CVCUS01Y)
    │
    ├──< Card-XREF (CVACT03Y)  [XREF-CUST-ID → CUST-ID]
    │       │
    │       ├── Card Number  ──> Card (CVACT02Y)  [XREF-CARD-NUM → CARD-NUM]
    │       │
    │       └── Account ID   ──> Account (CVACT01Y)  [XREF-ACCT-ID → ACCT-ID]
    │                               │
    │                               ├──< Transaction (CVTRA05Y)  [via XREF Card lookup]
    │                               │
    │                               ├──< Tran Cat Balance (CVTRA01Y)  [TRANCAT-ACCT-ID]
    │                               │
    │                               └──  Disclosure Group (CVTRA02Y)  [via ACCT-GROUP-ID]
    │
    └──  User Security (CSUSR01Y)  [authentication, separate entity]

Transaction Type (CVTRA03Y)  ──< Transaction Category (CVTRA04Y)
```

---

## Data Type Reference

| PIC Pattern | COBOL Type | Business Equivalent | Example |
|:------------|:-----------|:-------------------|:--------|
| PIC 9(n) | Zoned Decimal | Integer | Account ID, Customer ID |
| PIC X(n) | Alphanumeric | String | Names, descriptions, codes |
| PIC S9(n)V99 | Signed Decimal (2 places) | Currency / Amount | Balances, transaction amounts |
| PIC S9(n)V99 COMP-3 | Packed Decimal | Currency (optimized storage) | Export balances |
| PIC 9(n) COMP | Binary Integer | Counter / Numeric ID | Sequence numbers |
| PIC +9(n).99 | Edited Numeric | Display currency | Authorization amounts |
