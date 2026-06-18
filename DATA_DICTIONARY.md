# Data Dictionary — CardDemo COBOL Estate

All field definitions are extracted from copybooks in `app/cpy/` and sub-application `cpy/` directories. Fields are grouped by business entity.

---

## 1. Account Entity

### CVACT01Y.cpy — Account Master Record
VSAM KSDS record layout for the account data file (`ACCTDAT` / `ACCTFILE`).

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | ACCOUNT-RECORD | — | Group | Root record (300 bytes implied) | — |
| 05 | ACCT-ID | PIC 9(11) | Numeric, 11 digits | Unique account identifier | Primary key |
| 05 | ACCT-ACTIVE-STATUS | PIC X(01) | Alphanumeric, 1 char | Account status flag | 'Y' = active, 'N' = inactive |
| 05 | ACCT-CURR-BAL | PIC S9(10)V99 | Signed decimal, 12 digits (2 decimal) | Current account balance | Signed; can be negative |
| 05 | ACCT-CREDIT-LIMIT | PIC S9(10)V99 | Signed decimal | Maximum credit limit | Must be positive |
| 05 | ACCT-CASH-CREDIT-LIMIT | PIC S9(10)V99 | Signed decimal | Cash advance credit limit | Must be positive |
| 05 | ACCT-OPEN-DATE | PIC X(10) | Alphanumeric date | Date account was opened | Format: YYYY-MM-DD |
| 05 | ACCT-EXPIRAION-DATE | PIC X(10) | Alphanumeric date | Account expiration date | Format: YYYY-MM-DD |
| 05 | ACCT-REISSUE-DATE | PIC X(10) | Alphanumeric date | Last card reissue date | Format: YYYY-MM-DD |
| 05 | ACCT-CURR-CYC-CREDIT | PIC S9(10)V99 | Signed decimal | Current billing cycle credits | Running total |
| 05 | ACCT-CURR-CYC-DEBIT | PIC S9(10)V99 | Signed decimal | Current billing cycle debits | Running total |
| 05 | ACCT-ADDR-ZIP | PIC X(10) | Alphanumeric | Account holder ZIP/postal code | — |
| 05 | ACCT-GROUP-ID | PIC X(10) | Alphanumeric | Disclosure/interest rate group | Foreign key to DIS-GROUP-RECORD |
| 05 | FILLER | PIC X(178) | Filler | Reserved/padding | — |

---

## 2. Card Entity

### CVACT02Y.cpy — Card Master Record
VSAM KSDS record layout for the card data file (`CARDDAT` / `CARDFILE`). Alternate indexes exist on `CARD-ACCT-ID`.

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | CARD-RECORD | — | Group | Root record (150 bytes implied) | — |
| 05 | CARD-NUM | PIC X(16) | Alphanumeric, 16 chars | Credit card number (PAN) | Primary key; 16 digits |
| 05 | CARD-ACCT-ID | PIC 9(11) | Numeric, 11 digits | Associated account ID | FK to ACCOUNT-RECORD |
| 05 | CARD-CVV-CD | PIC 9(03) | Numeric, 3 digits | Card verification value (CVV) | 3-digit code |
| 05 | CARD-EMBOSSED-NAME | PIC X(50) | Alphanumeric | Name embossed on physical card | — |
| 05 | CARD-EXPIRAION-DATE | PIC X(10) | Alphanumeric date | Card expiration date | Format: YYYY-MM-DD |
| 05 | CARD-ACTIVE-STATUS | PIC X(01) | Alphanumeric, 1 char | Card active status | 'Y' = active, 'N' = inactive |
| 05 | FILLER | PIC X(59) | Filler | Reserved/padding | — |

---

## 3. Customer Entity

### CVCUS01Y.cpy — Customer Master Record
VSAM KSDS record layout for the customer data file (`CUSTDAT` / `CUSTFILE`).

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | CUSTOMER-RECORD | — | Group | Root record (500 bytes implied) | — |
| 05 | CUST-ID | PIC 9(09) | Numeric, 9 digits | Unique customer identifier | Primary key |
| 05 | CUST-FIRST-NAME | PIC X(25) | Alphanumeric | Customer first name | — |
| 05 | CUST-MIDDLE-NAME | PIC X(25) | Alphanumeric | Customer middle name | — |
| 05 | CUST-LAST-NAME | PIC X(25) | Alphanumeric | Customer last name | — |
| 05 | CUST-ADDR-LINE-1 | PIC X(50) | Alphanumeric | Address line 1 | — |
| 05 | CUST-ADDR-LINE-2 | PIC X(50) | Alphanumeric | Address line 2 | — |
| 05 | CUST-ADDR-LINE-3 | PIC X(50) | Alphanumeric | Address line 3 | — |
| 05 | CUST-ADDR-STATE-CD | PIC X(02) | Alphanumeric | US state code | Validated against 88-level list in CSLKPCDY |
| 05 | CUST-ADDR-COUNTRY-CD | PIC X(03) | Alphanumeric | Country code | — |
| 05 | CUST-ADDR-ZIP | PIC X(10) | Alphanumeric | ZIP/postal code | Validated against state+zip table in CSLKPCDY |
| 05 | CUST-PHONE-NUM-1 | PIC X(15) | Alphanumeric | Primary phone number | Area code validated via CSLKPCDY (NANPA list) |
| 05 | CUST-PHONE-NUM-2 | PIC X(15) | Alphanumeric | Secondary phone number | Same area code validation |
| 05 | CUST-SSN | PIC 9(09) | Numeric, 9 digits | Social Security Number | Must not be zero; 9 digits |
| 05 | CUST-GOVT-ISSUED-ID | PIC X(20) | Alphanumeric | Government-issued ID number | — |
| 05 | CUST-DOB-YYYY-MM-DD | PIC X(10) | Alphanumeric date | Date of birth | Format: YYYY-MM-DD; validated via CSUTLDPY |
| 05 | CUST-EFT-ACCOUNT-ID | PIC X(10) | Alphanumeric | EFT/bank account for payments | — |
| 05 | CUST-PRI-CARD-HOLDER-IND | PIC X(01) | Alphanumeric | Primary cardholder indicator | 'Y' = primary |
| 05 | CUST-FICO-CREDIT-SCORE | PIC 9(03) | Numeric, 3 digits | FICO credit score | Range: 300-850 (typical) |
| 05 | FILLER | PIC X(168) | Filler | Reserved/padding | — |

### CUSTREC.cpy — Alternate Customer Record
Identical structure to CVCUS01Y with minor formatting differences. Used in CBSTM03A statement generation.

---

## 4. Cross-Reference (Account ↔ Card ↔ Customer)

### CVACT03Y.cpy — Cross-Reference Record
VSAM KSDS record linking cards to customers and accounts (`CARDXREF` / `XREFFILE`). Alternate indexes on `XREF-CUST-ID` and `XREF-ACCT-ID`.

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | CARD-XREF-RECORD | — | Group | Root record (50 bytes) | — |
| 05 | XREF-CARD-NUM | PIC X(16) | Alphanumeric | Card number (PAN) | Primary key; FK to CARD-RECORD |
| 05 | XREF-CUST-ID | PIC 9(09) | Numeric | Customer ID | FK to CUSTOMER-RECORD |
| 05 | XREF-ACCT-ID | PIC 9(11) | Numeric | Account ID | FK to ACCOUNT-RECORD |
| 05 | FILLER | PIC X(14) | Filler | Reserved | — |

---

## 5. Transaction Entity

### CVTRA05Y.cpy — Transaction Master Record
VSAM KSDS record for the transaction master file (`TRANSACT` / `TRANFILE`). Key is card-number + transaction-ID composite.

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | TRAN-RECORD | — | Group | Root record (350 bytes implied) | — |
| 05 | TRAN-ID | PIC X(16) | Alphanumeric | Unique transaction identifier | Composite key part 2 |
| 05 | TRAN-TYPE-CD | PIC X(02) | Alphanumeric | Transaction type code | FK to TRAN-TYPE-RECORD |
| 05 | TRAN-CAT-CD | PIC 9(04) | Numeric | Transaction category code | FK to TRAN-CAT-RECORD |
| 05 | TRAN-SOURCE | PIC X(10) | Alphanumeric | Transaction origination source | — |
| 05 | TRAN-DESC | PIC X(100) | Alphanumeric | Transaction description text | — |
| 05 | TRAN-AMT | PIC S9(09)V99 | Signed decimal | Transaction amount | Signed; negative = credit |
| 05 | TRAN-MERCHANT-ID | PIC 9(09) | Numeric | Merchant identifier | — |
| 05 | TRAN-MERCHANT-NAME | PIC X(50) | Alphanumeric | Merchant name | — |
| 05 | TRAN-MERCHANT-CITY | PIC X(50) | Alphanumeric | Merchant city | — |
| 05 | TRAN-MERCHANT-ZIP | PIC X(10) | Alphanumeric | Merchant ZIP code | — |
| 05 | TRAN-CARD-NUM | PIC X(16) | Alphanumeric | Card number for this transaction | Composite key part 1 (in VSAM key) |
| 05 | TRAN-ORIG-TS | PIC X(26) | Alphanumeric timestamp | Original transaction timestamp | ISO format |
| 05 | TRAN-PROC-TS | PIC X(26) | Alphanumeric timestamp | Processing timestamp | ISO format |
| 05 | FILLER | PIC X(20) | Filler | Reserved | — |

### CVTRA06Y.cpy — Daily Transaction Record
Same layout as CVTRA05Y but with `DALYTRAN-` prefix. Represents incoming daily batch transactions before posting.

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|------------------|
| 01 | DALYTRAN-RECORD | — | Group | Daily transaction input record |
| 05 | DALYTRAN-ID | PIC X(16) | Alphanumeric | Daily transaction ID |
| 05 | DALYTRAN-TYPE-CD | PIC X(02) | Alphanumeric | Transaction type code |
| 05 | DALYTRAN-CAT-CD | PIC 9(04) | Numeric | Transaction category code |
| 05 | DALYTRAN-SOURCE | PIC X(10) | Alphanumeric | Origination source |
| 05 | DALYTRAN-DESC | PIC X(100) | Alphanumeric | Description |
| 05 | DALYTRAN-AMT | PIC S9(09)V99 | Signed decimal | Amount |
| 05 | DALYTRAN-MERCHANT-ID | PIC 9(09) | Numeric | Merchant ID |
| 05 | DALYTRAN-MERCHANT-NAME | PIC X(50) | Alphanumeric | Merchant name |
| 05 | DALYTRAN-MERCHANT-CITY | PIC X(50) | Alphanumeric | Merchant city |
| 05 | DALYTRAN-MERCHANT-ZIP | PIC X(10) | Alphanumeric | Merchant ZIP |
| 05 | DALYTRAN-CARD-NUM | PIC X(16) | Alphanumeric | Card number |
| 05 | DALYTRAN-ORIG-TS | PIC X(26) | Alphanumeric | Original timestamp |
| 05 | DALYTRAN-PROC-TS | PIC X(26) | Alphanumeric | Processing timestamp |
| 05 | FILLER | PIC X(20) | Filler | Reserved |

### COSTM01.CPY — Statement Transaction Record
Alternate transaction layout used by statement generation (CBSTM03A). Key structure differs — `TRNX-CARD-NUM` + `TRNX-ID` as composite key.

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|------------------|
| 01 | TRNX-RECORD | — | Group | Statement transaction record |
| 05/10 | TRNX-CARD-NUM | PIC X(16) | Alphanumeric | Card number (key part 1) |
| 05/10 | TRNX-ID | PIC X(16) | Alphanumeric | Transaction ID (key part 2) |
| 10 | TRNX-TYPE-CD | PIC X(02) | Alphanumeric | Type code |
| 10 | TRNX-CAT-CD | PIC 9(04) | Numeric | Category code |
| 10 | TRNX-SOURCE | PIC X(10) | Alphanumeric | Source |
| 10 | TRNX-DESC | PIC X(100) | Alphanumeric | Description |
| 10 | TRNX-AMT | PIC S9(09)V99 | Signed decimal | Amount |
| 10 | TRNX-MERCHANT-ID | PIC 9(09) | Numeric | Merchant ID |
| 10 | TRNX-MERCHANT-NAME | PIC X(50) | Alphanumeric | Merchant name |
| 10 | TRNX-MERCHANT-CITY | PIC X(50) | Alphanumeric | Merchant city |
| 10 | TRNX-MERCHANT-ZIP | PIC X(10) | Alphanumeric | Merchant ZIP |
| 10 | TRNX-ORIG-TS | PIC X(26) | Alphanumeric | Original timestamp |
| 10 | TRNX-PROC-TS | PIC X(26) | Alphanumeric | Processing timestamp |

---

## 6. Transaction Reference Data

### CVTRA03Y.cpy — Transaction Type Record
VSAM KSDS record for transaction type codes (`TRANTYPE`). Also stored in DB2 table TRTYP.

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|------------------|
| 01 | TRAN-TYPE-RECORD | — | Group | 60-byte record |
| 05 | TRAN-TYPE | PIC X(02) | Alphanumeric | Transaction type code (primary key) |
| 05 | TRAN-TYPE-DESC | PIC X(50) | Alphanumeric | Type description (e.g., "Purchase", "Cash Advance") |
| 05 | FILLER | PIC X(08) | Filler | Reserved |

### CVTRA04Y.cpy — Transaction Category Record
VSAM KSDS record for transaction category codes (`TRANCATG`). Also stored in DB2 table TRCAT.

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|------------------|
| 01 | TRAN-CAT-RECORD | — | Group | 60-byte record |
| 05/10 | TRAN-TYPE-CD | PIC X(02) | Alphanumeric | Transaction type code (composite key part 1) |
| 05/10 | TRAN-CAT-CD | PIC 9(04) | Numeric | Category code (composite key part 2) |
| 05 | TRAN-CAT-TYPE-DESC | PIC X(50) | Alphanumeric | Category description |
| 05 | FILLER | PIC X(04) | Filler | Reserved |

### CVTRA01Y.cpy — Transaction Category Balance Record
VSAM KSDS record tracking running balances per account+type+category (`TCATBALF`).

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|------------------|
| 01 | TRAN-CAT-BAL-RECORD | — | Group | Balance tracking record |
| 10 | TRANCAT-ACCT-ID | PIC 9(11) | Numeric | Account ID (composite key part 1) |
| 10 | TRANCAT-TYPE-CD | PIC X(02) | Alphanumeric | Transaction type code (key part 2) |
| 10 | TRANCAT-CD | PIC 9(04) | Numeric | Category code (key part 3) |
| 05 | TRAN-CAT-BAL | PIC S9(09)V99 | Signed decimal | Running balance for this category | 
| 05 | FILLER | PIC X(22) | Filler | Reserved |

### CVTRA02Y.cpy — Disclosure Group Record
VSAM KSDS record for interest rate disclosure groups (`DISCGRP`).

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|------------------|
| 01 | DIS-GROUP-RECORD | — | Group | Disclosure group record |
| 10 | DIS-ACCT-GROUP-ID | PIC X(10) | Alphanumeric | Group ID (composite key part 1) |
| 10 | DIS-TRAN-TYPE-CD | PIC X(02) | Alphanumeric | Transaction type code (key part 2) |
| 10 | DIS-TRAN-CAT-CD | PIC 9(04) | Numeric | Category code (key part 3) |
| 05 | DIS-INT-RATE | PIC S9(04)V99 | Signed decimal | Interest rate for this group+type+category |
| 05 | FILLER | PIC X(28) | Filler | Reserved |

### CVTRA07Y.cpy — Transaction Report Layout
Working storage for report formatting used by CBTRN03C.

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|------------------|
| 01 | REPORT-NAME-HEADER | — | Group | Report header block |
| 05 | REPT-SHORT-NAME | PIC X(38) | Alphanumeric | Report short name ('DALYREPT') |
| 05 | REPT-LONG-NAME | PIC X(41) | Alphanumeric | Report long name ('Daily Transaction Report') |
| 05 | REPT-DATE-HEADER | PIC X(12) | Alphanumeric | 'Date Range: ' |
| 05 | REPT-START-DATE | PIC X(10) | Alphanumeric | Report start date |
| 05 | REPT-END-DATE | PIC X(10) | Alphanumeric | Report end date |
| 01 | TRANSACTION-DETAIL-REPORT | — | Group | Detail line |
| 05 | TRAN-REPORT-TRANS-ID | PIC X(16) | Alphanumeric | Transaction ID |
| 05 | TRAN-REPORT-ACCOUNT-ID | PIC X(11) | Alphanumeric | Account ID |
| 05 | TRAN-REPORT-TYPE-CD | PIC X(02) | Alphanumeric | Type code |
| 05 | TRAN-REPORT-TYPE-DESC | PIC X(15) | Alphanumeric | Type description |
| 05 | TRAN-REPORT-CAT-CD | PIC 9(04) | Numeric | Category code |
| 05 | TRAN-REPORT-CAT-DESC | PIC X(29) | Alphanumeric | Category description |
| 05 | TRAN-REPORT-SOURCE | PIC X(10) | Alphanumeric | Source |
| 05 | TRAN-REPORT-AMT | PIC -ZZZ,ZZZ,ZZZ.ZZ | Edited numeric | Amount (formatted) |

---

## 7. Security / User Entity

### CSUSR01Y.cpy — User Security Record
VSAM KSDS record for user authentication (`USRSEC`).

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 01 | SEC-USER-DATA | — | Group | 80-byte record | — |
| 05 | SEC-USR-ID | PIC X(08) | Alphanumeric | User login ID | Primary key |
| 05 | SEC-USR-FNAME | PIC X(20) | Alphanumeric | User first name | — |
| 05 | SEC-USR-LNAME | PIC X(20) | Alphanumeric | User last name | — |
| 05 | SEC-USR-PWD | PIC X(08) | Alphanumeric | Password (plaintext) | 8 chars max |
| 05 | SEC-USR-TYPE | PIC X(01) | Alphanumeric | User type | 'A' = admin, 'U' = regular user |
| 05 | SEC-USR-FILLER | PIC X(23) | Filler | Reserved | — |

### UNUSED1Y.cpy — Unused Security Record (Deprecated)
Identical structure to CSUSR01Y with `UNUSED-` prefix. Appears to be a placeholder or template.

---

## 8. Authorization Entity (IMS/DB2)

### CIPAUDTY.cpy — Pending Authorization Detail Record
IMS hierarchical database child segment for individual authorization events.

| Level | Field Name | PIC Clause | Data Type | Business Meaning | Validation Rules |
|-------|-----------|------------|-----------|------------------|-----------------|
| 05 | PA-AUTHORIZATION-KEY | — | Group | Composite key | — |
| 10 | PA-AUTH-DATE-9C | PIC S9(05) COMP-3 | Packed decimal | Authorization date (sortable) | — |
| 10 | PA-AUTH-TIME-9C | PIC S9(09) COMP-3 | Packed decimal | Authorization time (sortable) | — |
| 05 | PA-AUTH-ORIG-DATE | PIC X(06) | Alphanumeric | Original date (display) | YYMMDD |
| 05 | PA-AUTH-ORIG-TIME | PIC X(06) | Alphanumeric | Original time (display) | HHMMSS |
| 05 | PA-CARD-NUM | PIC X(16) | Alphanumeric | Card number | — |
| 05 | PA-AUTH-TYPE | PIC X(04) | Alphanumeric | Authorization type | — |
| 05 | PA-CARD-EXPIRY-DATE | PIC X(04) | Alphanumeric | Card expiry | YYMM |
| 05 | PA-MESSAGE-TYPE | PIC X(06) | Alphanumeric | ISO message type | — |
| 05 | PA-MESSAGE-SOURCE | PIC X(06) | Alphanumeric | Message source | — |
| 05 | PA-AUTH-ID-CODE | PIC X(06) | Alphanumeric | Authorization ID/approval code | — |
| 05 | PA-AUTH-RESP-CODE | PIC X(02) | Alphanumeric | Response code | 88: '00' = approved |
| 05 | PA-AUTH-RESP-REASON | PIC X(04) | Alphanumeric | Decline reason code | — |
| 05 | PA-PROCESSING-CODE | PIC 9(06) | Numeric | Processing code | — |
| 05 | PA-TRANSACTION-AMT | PIC S9(10)V99 COMP-3 | Packed decimal | Requested amount | — |
| 05 | PA-APPROVED-AMT | PIC S9(10)V99 COMP-3 | Packed decimal | Approved amount | — |
| 05 | PA-MERCHANT-CATAGORY-CODE | PIC X(04) | Alphanumeric | Merchant category code (MCC) | — |
| 05 | PA-ACQR-COUNTRY-CODE | PIC X(03) | Alphanumeric | Acquirer country code | — |
| 05 | PA-POS-ENTRY-MODE | PIC 9(02) | Numeric | POS entry mode | — |
| 05 | PA-MERCHANT-ID | PIC X(15) | Alphanumeric | Merchant ID | — |
| 05 | PA-MERCHANT-NAME | PIC X(22) | Alphanumeric | Merchant name | — |
| 05 | PA-MERCHANT-CITY | PIC X(13) | Alphanumeric | Merchant city | — |
| 05 | PA-MERCHANT-STATE | PIC X(02) | Alphanumeric | Merchant state | — |
| 05 | PA-MERCHANT-ZIP | PIC X(09) | Alphanumeric | Merchant ZIP | — |
| 05 | PA-TRANSACTION-ID | PIC X(15) | Alphanumeric | Transaction reference ID | — |
| 05 | PA-MATCH-STATUS | PIC X(01) | Alphanumeric | Matching status | 88: P=pending, D=declined, E=expired, M=matched |
| 05 | PA-AUTH-FRAUD | PIC X(01) | Alphanumeric | Fraud flag | 88: F=confirmed fraud, R=removed |
| 05 | PA-FRAUD-RPT-DATE | PIC X(08) | Alphanumeric | Fraud report date | YYYYMMDD |
| 05 | FILLER | PIC X(17) | Filler | Reserved | — |

### CIPAUSMY.cpy — Pending Authorization Summary Record
IMS hierarchical database root segment for authorization summaries by account.

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|------------------|
| 05 | PA-SUMMARY-KEY | — | Group | Root segment key |
| 10 | PA-SUMM-ACCT-ID | PIC 9(11) | Numeric | Account ID (IMS root key) |
| 05 | PA-SUMM-CARD-NUM | PIC X(16) | Alphanumeric | Card number |
| 05 | PA-SUMM-APPROVED-CNT | PIC 9(06) | Numeric | Count of approved auths |
| 05 | PA-SUMM-APPROVED-AMT | PIC S9(10)V99 COMP-3 | Packed decimal | Total approved amount |
| 05 | PA-SUMM-DECLINED-CNT | PIC 9(06) | Numeric | Count of declined auths |
| 05 | PA-SUMM-DECLINED-AMT | PIC S9(10)V99 COMP-3 | Packed decimal | Total declined amount |
| 05 | PA-SUMM-PENDING-CNT | PIC 9(06) | Numeric | Count of pending auths |
| 05 | PA-SUMM-PENDING-AMT | PIC S9(10)V99 COMP-3 | Packed decimal | Total pending amount |
| 05 | PA-SUMM-LAST-AUTH-DATE | PIC X(06) | Alphanumeric | Last auth date |
| 05 | PA-SUMM-LAST-AUTH-TIME | PIC X(06) | Alphanumeric | Last auth time |

### CCPAURQY.cpy — Authorization Request Message (MQ)
Layout for inbound MQ authorization request messages.

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|------------------|
| 05 | PA-RQ-AUTH-DATE | PIC X(06) | Alphanumeric | Request date |
| 05 | PA-RQ-AUTH-TIME | PIC X(06) | Alphanumeric | Request time |
| 05 | PA-RQ-CARD-NUM | PIC X(16) | Alphanumeric | Card number |
| 05 | PA-RQ-AUTH-TYPE | PIC X(04) | Alphanumeric | Auth type |
| 05 | PA-RQ-CARD-EXPIRY-DATE | PIC X(04) | Alphanumeric | Card expiry |
| 05 | PA-RQ-MESSAGE-TYPE | PIC X(06) | Alphanumeric | ISO message type |
| 05 | PA-RQ-MESSAGE-SOURCE | PIC X(06) | Alphanumeric | Message source |
| 05 | PA-RQ-PROCESSING-CODE | PIC 9(06) | Numeric | Processing code |
| 05 | PA-RQ-TRANSACTION-AMT | PIC +9(10).99 | Edited numeric | Transaction amount |
| 05 | PA-RQ-MERCHANT-CATAGORY-CODE | PIC X(04) | Alphanumeric | MCC |
| 05 | PA-RQ-ACQR-COUNTRY-CODE | PIC X(03) | Alphanumeric | Acquirer country |
| 05 | PA-RQ-POS-ENTRY-MODE | PIC 9(02) | Numeric | POS entry mode |
| 05 | PA-RQ-MERCHANT-ID | PIC X(15) | Alphanumeric | Merchant ID |
| 05 | PA-RQ-MERCHANT-NAME | PIC X(22) | Alphanumeric | Merchant name |
| 05 | PA-RQ-MERCHANT-CITY | PIC X(13) | Alphanumeric | Merchant city |
| 05 | PA-RQ-MERCHANT-STATE | PIC X(02) | Alphanumeric | Merchant state |
| 05 | PA-RQ-MERCHANT-ZIP | PIC X(09) | Alphanumeric | Merchant ZIP |
| 05 | PA-RQ-TRANSACTION-ID | PIC X(15) | Alphanumeric | Transaction reference |

### CCPAURLY.cpy — Authorization Reply Message (MQ)
Layout for outbound MQ authorization reply messages.

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|------------------|
| 05 | PA-RL-CARD-NUM | PIC X(16) | Alphanumeric | Card number |
| 05 | PA-RL-TRANSACTION-ID | PIC X(15) | Alphanumeric | Transaction reference |
| 05 | PA-RL-AUTH-ID-CODE | PIC X(06) | Alphanumeric | Approval/auth code |
| 05 | PA-RL-AUTH-RESP-CODE | PIC X(02) | Alphanumeric | Response code |
| 05 | PA-RL-AUTH-RESP-REASON | PIC X(04) | Alphanumeric | Decline reason |
| 05 | PA-RL-APPROVED-AMT | PIC +9(10).99 | Edited numeric | Approved amount |

### CCPAUERY.cpy — Authorization Error Log Record

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|------------------|
| 05 | ERR-DATE | PIC X(06) | Alphanumeric | Error date |
| 05 | ERR-TIME | PIC X(06) | Alphanumeric | Error time |
| 05 | ERR-APPLICATION | PIC X(08) | Alphanumeric | Application name |
| 05 | ERR-PROGRAM | PIC X(08) | Alphanumeric | Program name |
| 05 | ERR-LOCATION | PIC X(04) | Alphanumeric | Error location code |
| 05 | ERR-LEVEL | PIC X(01) | Alphanumeric | Severity (88: L=log, I=info, W=warn, C=critical) |
| 05 | ERR-SUBSYSTEM | PIC X(01) | Alphanumeric | Subsystem (88: A=app, C=CICS, I=IMS, D=DB2, M=MQ, F=file) |
| 05 | ERR-CODE-1 | PIC X(09) | Alphanumeric | Primary error code |
| 05 | ERR-CODE-2 | PIC X(09) | Alphanumeric | Secondary error code |
| 05 | ERR-MESSAGE | PIC X(50) | Alphanumeric | Error message text |
| 05 | ERR-EVENT-KEY | PIC X(20) | Alphanumeric | Event correlation key |

---

## 9. Export/Migration Entity

### CVEXPORT.cpy — Consolidated Export Record
Multi-format record for data migration export/import (CBEXPORT/CBIMPORT).

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|------------------|
| 01 | EXPORT-RECORD | — | Group | Export wrapper record |
| 05 | EXPORT-REC-TYPE | PIC X(1) | Alphanumeric | Record type discriminator |
| 05 | EXPORT-TIMESTAMP | PIC X(26) | Alphanumeric | Export timestamp |
| 05 | EXPORT-SEQUENCE-NUM | PIC 9(9) COMP | Binary | Record sequence number |
| 05 | EXPORT-BRANCH-ID | PIC X(4) | Alphanumeric | Originating branch |
| 05 | EXPORT-REGION-CODE | PIC X(5) | Alphanumeric | Region code |
| 05 | EXPORT-RECORD-DATA | PIC X(460) | Alphanumeric | Polymorphic payload (see REDEFINES) |
| — | EXPORT-CUSTOMER-DATA | (REDEFINES) | Group | Customer data variant |
| — | EXPORT-ACCOUNT-DATA | (REDEFINES) | Group | Account data variant |
| — | EXPORT-TRANSACTION-DATA | (REDEFINES) | Group | Transaction data variant |

---

## 10. IMS PCB (Program Communication Block) Layouts

### PAUTBPCB.CPY — Authorization Database PCB

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|------------------|
| 05 | PAUT-DBDNAME | PIC X(08) | Alphanumeric | Database description name |
| 05 | PAUT-SEG-LEVEL | PIC X(02) | Alphanumeric | Segment level |
| 05 | PAUT-PCB-STATUS | PIC X(02) | Alphanumeric | IMS status code ('  '=OK, 'GE'=not found) |
| 05 | PAUT-PCB-PROCOPT | PIC X(04) | Alphanumeric | Processing option |
| 05 | PAUT-SEG-NAME | PIC X(08) | Alphanumeric | Current segment name |
| 05 | PAUT-KEYFB | PIC X(255) | Alphanumeric | Key feedback area |

### PASFLPCB.CPY / PADFLPCB.CPY — Summary/Detail PCBs
Same structure as PAUTBPCB for summary and detail segments respectively.

---

## 11. Application Infrastructure Copybooks

### COCOM01Y.cpy — Application Communication Area (COMMAREA)
Passed between all online CICS programs via XCTL/LINK.

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|------------------|
| 05/10 | CDEMO-FROM-TRANID | PIC X(04) | Alphanumeric | Source transaction ID |
| 05/10 | CDEMO-FROM-PROGRAM | PIC X(08) | Alphanumeric | Source program name |
| 05/10 | CDEMO-TO-TRANID | PIC X(04) | Alphanumeric | Target transaction ID |
| 05/10 | CDEMO-TO-PROGRAM | PIC X(08) | Alphanumeric | Target program name |
| 05/10 | CDEMO-USER-ID | PIC X(08) | Alphanumeric | Authenticated user ID |
| 05/10 | CDEMO-USER-TYPE | PIC X(01) | Alphanumeric | User type (88: A=admin, U=user) |
| 05/10 | CDEMO-PGM-CONTEXT | PIC 9(01) | Numeric | Program context (88: 0=enter, 1=reenter) |
| 05/10 | CDEMO-CUST-ID | PIC 9(09) | Numeric | Selected customer ID |
| 05/10 | CDEMO-ACCT-ID | PIC 9(11) | Numeric | Selected account ID |
| 05/10 | CDEMO-CARD-NUM | PIC 9(16) | Numeric | Selected card number |
| 10 | CDEMO-LAST-MAP | PIC X(7) | Alphanumeric | Last displayed map name |
| 10 | CDEMO-LAST-MAPSET | PIC X(7) | Alphanumeric | Last displayed mapset |

### CVCRD01Y.cpy — Online Working Storage Areas
Common working storage for all online CICS programs.

| Level | Field Name | PIC Clause | Data Type | Business Meaning |
|-------|-----------|------------|-----------|------------------|
| 10 | CCARD-AID | PIC X(5) | Alphanumeric | Decoded AID key (88: ENTER, CLEAR, PA1, PA2, PFK01-12) |
| 10 | CCARD-NEXT-PROG | PIC X(8) | Alphanumeric | Next program to XCTL to |
| 10 | CCARD-NEXT-MAPSET | PIC X(7) | Alphanumeric | Next mapset to display |
| 10 | CCARD-NEXT-MAP | PIC X(7) | Alphanumeric | Next map to display |
| 10 | CCARD-ERROR-MSG | PIC X(75) | Alphanumeric | Error message for screen |
| 10 | CCARD-RETURN-MSG | PIC X(75) | Alphanumeric | Return message for screen |
| 10 | CC-ACCT-ID | PIC X(11) | Alphanumeric | Current account ID (working) |
| 10 | CC-CARD-NUM | PIC X(16) | Alphanumeric | Current card number (working) |
| 10 | CC-CUST-ID | PIC X(09) | Alphanumeric | Current customer ID (working) |

### COADM02Y.cpy / COMEN02Y.cpy — Menu Option Tables
Admin and main user menu option arrays, mapping option numbers to program names.

### CSDAT01Y.cpy — Date/Time Working Storage
Common date/time fields used across programs.

### CSMSG01Y.cpy — Common Messages
Shared message constants ('Thank you...', 'Invalid key pressed...').

### CSMSG02Y.cpy — Abend Data Areas
Working storage for abnormal termination handling.

### COTTL01Y.cpy — Screen Title Constants
Common screen title lines ('AWS Mainframe Modernization', 'CardDemo').

### CSLKPCDY.cpy — Lookup Code Repository
Extensive validation tables:
- North American phone area codes (NANPA list — 300+ entries)
- US state codes (50 states + territories)
- US state-to-ZIP prefix mappings

### CSUTLDWY.cpy — Date Validation Working Storage
Date field definitions with 88-level validation rules for century, year, month, day including leap year logic.

### CSUTLDPY.cpy — Date Validation Procedure Division
PERFORM-able paragraphs for date validation (EDIT-DATE-CCYYMMDD, EDIT-YEAR-CCYY, EDIT-MONTH, EDIT-DAY, EDIT-DATE-OF-BIRTH).

### CSSTRPFY.cpy — PF Key Storage Procedure
PERFORM-able paragraph that decodes EIBAID into CCARD-AID 88-level values.

### CSSETATY.cpy — Screen Attribute Setting Template
Copybook with substitution tokens `(TESTVAR1)`, `(SCRNVAR2)`, `(MAPNAME3)` — used as a code generation template for setting screen field attributes (red highlight on error).

### CODATECN.cpy — Date Conversion Record
Working storage for date format conversions (YYYYMMDD ↔ YYYY-MM-DD).

### CSDB2RWY.cpy — DB2 Working Storage
DB2-specific working storage fields including SQLCODE display area and dummy query for connectivity testing.

### CSDB2RPY.cpy — DB2 Procedure Division
Priming query paragraph (SELECT 1 FROM SYSIBM.SYSDUMMY1) to verify DB2 connectivity.

### IMSFUNCS.cpy — IMS Function Constants
Constants for IMS DLI function codes (GU, GN, GNP, GHNP, REPL, ISRT, DLET).
