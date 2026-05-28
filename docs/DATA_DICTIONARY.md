# CardDemo Data Dictionary

> **System**: CardDemo – Mainframe Credit Card Management System  
> **Generated**: 2026-05-28  
> **Entities Cataloged**: 15 business entities across 30+ copybooks

---

## 1 — Customer (`CVCUS01Y`)

The primary cardholder or applicant. Record length: 500 bytes.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Customer ID | CUST-ID | `PIC 9(09)` | Numeric | 9 | Unique customer identifier |
| First Name | CUST-FIRST-NAME | `PIC X(25)` | Alpha | 25 | Customer first name |
| Middle Name | CUST-MIDDLE-NAME | `PIC X(25)` | Alpha | 25 | Customer middle name |
| Last Name | CUST-LAST-NAME | `PIC X(25)` | Alpha | 25 | Customer last name |
| Address Line 1 | CUST-ADDR-LINE-1 | `PIC X(50)` | Alpha | 50 | Primary address |
| Address Line 2 | CUST-ADDR-LINE-2 | `PIC X(50)` | Alpha | 50 | Secondary address |
| Address Line 3 | CUST-ADDR-LINE-3 | `PIC X(50)` | Alpha | 50 | Tertiary address |
| State Code | CUST-ADDR-STATE-CD | `PIC X(02)` | Alpha | 2 | US state abbreviation |
| Country Code | CUST-ADDR-COUNTRY-CD | `PIC X(03)` | Alpha | 3 | ISO country code |
| ZIP Code | CUST-ADDR-ZIP | `PIC X(10)` | Alpha | 10 | Postal code (ZIP+4 capable) |
| Phone Number 1 | CUST-PHONE-NUM-1 | `PIC X(15)` | Alpha | 15 | Primary phone |
| Phone Number 2 | CUST-PHONE-NUM-2 | `PIC X(15)` | Alpha | 15 | Secondary phone |
| SSN | CUST-SSN | `PIC 9(09)` | Numeric | 9 | Social Security Number (PII) |
| Government ID | CUST-GOVT-ISSUED-ID | `PIC X(20)` | Alpha | 20 | Government-issued identification |
| Date of Birth | CUST-DOB-YYYY-MM-DD | `PIC X(10)` | Date | 10 | Date of birth (YYYY-MM-DD) |
| EFT Account ID | CUST-EFT-ACCOUNT-ID | `PIC X(10)` | Alpha | 10 | Electronic Funds Transfer account |
| Primary Cardholder | CUST-PRI-CARD-HOLDER-IND | `PIC X(01)` | Flag | 1 | Primary cardholder indicator (Y/N) |
| FICO Score | CUST-FICO-CREDIT-SCORE | `PIC 9(03)` | Numeric | 3 | Credit score (300–850) |

**Key**: CUST-ID  
**Storage**: VSAM KSDS (`AWS.M2.CARDDEMO.CUSTDATA`)  
**PII Fields**: CUST-SSN, CUST-DOB-YYYY-MM-DD, CUST-GOVT-ISSUED-ID, CUST-PHONE-NUM-1/2

---

## 2 — Account (`CVACT01Y`)

A credit card account with balance and limit tracking. Record length: 300 bytes.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Account ID | ACCT-ID | `PIC 9(11)` | Numeric | 11 | Unique account identifier |
| Active Status | ACCT-ACTIVE-STATUS | `PIC X(01)` | Flag | 1 | Account status (Y=Active) |
| Current Balance | ACCT-CURR-BAL | `PIC S9(10)V99` | Signed Decimal | 12+2 | Current outstanding balance |
| Credit Limit | ACCT-CREDIT-LIMIT | `PIC S9(10)V99` | Signed Decimal | 12+2 | Maximum credit line |
| Cash Credit Limit | ACCT-CASH-CREDIT-LIMIT | `PIC S9(10)V99` | Signed Decimal | 12+2 | Cash advance limit |
| Open Date | ACCT-OPEN-DATE | `PIC X(10)` | Date | 10 | Account opening date |
| Expiration Date | ACCT-EXPIRAION-DATE | `PIC X(10)` | Date | 10 | Account expiration date |
| Reissue Date | ACCT-REISSUE-DATE | `PIC X(10)` | Date | 10 | Last card reissue date |
| Cycle Credits | ACCT-CURR-CYC-CREDIT | `PIC S9(10)V99` | Signed Decimal | 12+2 | Current billing cycle credits |
| Cycle Debits | ACCT-CURR-CYC-DEBIT | `PIC S9(10)V99` | Signed Decimal | 12+2 | Current billing cycle debits |
| ZIP Code | ACCT-ADDR-ZIP | `PIC X(10)` | Alpha | 10 | Account holder ZIP code |
| Group ID | ACCT-GROUP-ID | `PIC X(10)` | Alpha | 10 | Disclosure / rate group |

**Key**: ACCT-ID  
**Storage**: VSAM KSDS (`AWS.M2.CARDDEMO.ACCTDATA`)

---

## 3 — Card (`CVACT02Y`)

A physical or virtual credit card linked to an account. Record length: 150 bytes.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Card Number | CARD-NUM | `PIC X(16)` | Alpha | 16 | 16-digit card number (PAN) |
| Account ID | CARD-ACCT-ID | `PIC 9(11)` | Numeric | 11 | Owning account reference |
| CVV Code | CARD-CVV-CD | `PIC 9(03)` | Numeric | 3 | Card verification value |
| Embossed Name | CARD-EMBOSSED-NAME | `PIC X(50)` | Alpha | 50 | Name printed on card |
| Expiration Date | CARD-EXPIRAION-DATE | `PIC X(10)` | Date | 10 | Card expiration date |
| Active Status | CARD-ACTIVE-STATUS | `PIC X(01)` | Flag | 1 | Card status (Y=Active) |

**Key**: CARD-NUM  
**Storage**: VSAM KSDS (`AWS.M2.CARDDEMO.CARDDATA`)  
**PII Fields**: CARD-NUM, CARD-CVV-CD

---

## 4 — Card Cross-Reference (`CVACT03Y`)

Links a card to its customer and account. Record length: 50 bytes.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Card Number | XREF-CARD-NUM | `PIC X(16)` | Alpha | 16 | Card number (FK to Card) |
| Customer ID | XREF-CUST-ID | `PIC 9(09)` | Numeric | 9 | Customer ID (FK to Customer) |
| Account ID | XREF-ACCT-ID | `PIC 9(11)` | Numeric | 11 | Account ID (FK to Account) |

**Key**: XREF-CARD-NUM  
**Storage**: VSAM KSDS (`AWS.M2.CARDDEMO.CARDXREF`)  
**Relationships**: Joins Customer ↔ Account ↔ Card

---

## 5 — Transaction (`CVTRA05Y`)

An online or posted credit card transaction. Record length: 350 bytes.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Transaction ID | TRAN-ID | `PIC X(16)` | Alpha | 16 | Unique transaction reference |
| Type Code | TRAN-TYPE-CD | `PIC X(02)` | Alpha | 2 | Transaction type (FK to Tran Type) |
| Category Code | TRAN-CAT-CD | `PIC 9(04)` | Numeric | 4 | Category classification |
| Source | TRAN-SOURCE | `PIC X(10)` | Alpha | 10 | Origination channel |
| Description | TRAN-DESC | `PIC X(100)` | Alpha | 100 | Free-text transaction narrative |
| Amount | TRAN-AMT | `PIC S9(09)V99` | Signed Decimal | 11+2 | Transaction monetary value |
| Merchant ID | TRAN-MERCHANT-ID | `PIC 9(09)` | Numeric | 9 | Merchant identifier |
| Merchant Name | TRAN-MERCHANT-NAME | `PIC X(50)` | Alpha | 50 | Merchant business name |
| Merchant City | TRAN-MERCHANT-CITY | `PIC X(50)` | Alpha | 50 | Merchant city |
| Merchant ZIP | TRAN-MERCHANT-ZIP | `PIC X(10)` | Alpha | 10 | Merchant postal code |
| Card Number | TRAN-CARD-NUM | `PIC X(16)` | Alpha | 16 | Card used (FK to Card) |
| Origination TS | TRAN-ORIG-TS | `PIC X(26)` | Timestamp | 26 | When transaction originated |
| Processing TS | TRAN-PROC-TS | `PIC X(26)` | Timestamp | 26 | When transaction was processed |

**Key**: TRAN-ID  
**Storage**: VSAM KSDS (`AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`)

---

## 6 — Daily Transaction (`CVTRA06Y`)

Incoming daily transactions awaiting posting. Record length: 350 bytes.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Transaction ID | DALYTRAN-ID | `PIC X(16)` | Alpha | 16 | Daily transaction reference |
| Type Code | DALYTRAN-TYPE-CD | `PIC X(02)` | Alpha | 2 | Transaction type code |
| Category Code | DALYTRAN-CAT-CD | `PIC 9(04)` | Numeric | 4 | Category code |
| Source | DALYTRAN-SOURCE | `PIC X(10)` | Alpha | 10 | Origination channel |
| Description | DALYTRAN-DESC | `PIC X(100)` | Alpha | 100 | Transaction narrative |
| Amount | DALYTRAN-AMT | `PIC S9(09)V99` | Signed Decimal | 11+2 | Monetary value |
| Merchant ID | DALYTRAN-MERCHANT-ID | `PIC 9(09)` | Numeric | 9 | Merchant identifier |
| Merchant Name | DALYTRAN-MERCHANT-NAME | `PIC X(50)` | Alpha | 50 | Merchant name |
| Merchant City | DALYTRAN-MERCHANT-CITY | `PIC X(50)` | Alpha | 50 | Merchant city |
| Merchant ZIP | DALYTRAN-MERCHANT-ZIP | `PIC X(10)` | Alpha | 10 | Merchant postal code |
| Card Number | DALYTRAN-CARD-NUM | `PIC X(16)` | Alpha | 16 | Card number |
| Origination TS | DALYTRAN-ORIG-TS | `PIC X(26)` | Timestamp | 26 | When originated |
| Processing TS | DALYTRAN-PROC-TS | `PIC X(26)` | Timestamp | 26 | When processed |

**Key**: DALYTRAN-ID  
**Storage**: Sequential file (`AWS.M2.CARDDEMO.DALYTRAN.PS`)

---

## 7 — Transaction Type (`CVTRA03Y`)

Reference table of transaction classifications. Record length: 60 bytes.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Type Code | TRAN-TYPE | `PIC X(02)` | Alpha | 2 | Two-character type code |
| Description | TRAN-TYPE-DESC | `PIC X(50)` | Alpha | 50 | Human-readable description |

**Key**: TRAN-TYPE  
**Storage**: VSAM KSDS (`AWS.M2.CARDDEMO.TRANTYPE`) or DB2 table `TRTYP`

---

## 8 — Transaction Category (`CVTRA04Y`)

Sub-classification under a transaction type. Record length: 60 bytes.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Type Code | TRAN-TYPE-CD | `PIC X(02)` | Alpha | 2 | Parent type code (FK) |
| Category Code | TRAN-CAT-CD | `PIC 9(04)` | Numeric | 4 | Category numeric code |
| Description | TRAN-CAT-TYPE-DESC | `PIC X(50)` | Alpha | 50 | Category description |

**Key**: TRAN-TYPE-CD + TRAN-CAT-CD (composite)  
**Storage**: VSAM KSDS (`AWS.M2.CARDDEMO.TRANCATG`) or DB2 table `TRCAT`

---

## 9 — Transaction Category Balance (`CVTRA01Y`)

Running balance per account per transaction category. Record length: 50 bytes.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Account ID | TRANCAT-ACCT-ID | `PIC 9(11)` | Numeric | 11 | Account FK |
| Type Code | TRANCAT-TYPE-CD | `PIC X(02)` | Alpha | 2 | Transaction type FK |
| Category Code | TRANCAT-CD | `PIC 9(04)` | Numeric | 4 | Category FK |
| Balance | TRAN-CAT-BAL | `PIC S9(09)V99` | Signed Decimal | 11+2 | Accumulated category balance |

**Key**: TRANCAT-ACCT-ID + TRANCAT-TYPE-CD + TRANCAT-CD (composite)  
**Storage**: VSAM KSDS (`AWS.M2.CARDDEMO.TCATBALF`)

---

## 10 — Disclosure Group (`CVTRA02Y`)

Interest rate rules per account group and transaction category. Record length: 50 bytes.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Account Group ID | DIS-ACCT-GROUP-ID | `PIC X(10)` | Alpha | 10 | Account group classification |
| Transaction Type | DIS-TRAN-TYPE-CD | `PIC X(02)` | Alpha | 2 | Transaction type FK |
| Category Code | DIS-TRAN-CAT-CD | `PIC 9(04)` | Numeric | 4 | Category FK |
| Interest Rate | DIS-INT-RATE | `PIC S9(04)V99` | Signed Decimal | 6+2 | Applicable interest rate (%) |

**Key**: DIS-ACCT-GROUP-ID + DIS-TRAN-TYPE-CD + DIS-TRAN-CAT-CD (composite)  
**Storage**: VSAM KSDS (`AWS.M2.CARDDEMO.DISCGRP`)

---

## 11 — User Security (`CSUSR01Y`)

Application authentication and authorization records. Record length: 80 bytes.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| User ID | SEC-USR-ID | `PIC X(08)` | Alpha | 8 | Login identifier |
| First Name | SEC-USR-FNAME | `PIC X(20)` | Alpha | 20 | User first name |
| Last Name | SEC-USR-LNAME | `PIC X(20)` | Alpha | 20 | User last name |
| Password | SEC-USR-PWD | `PIC X(08)` | Alpha | 8 | Login password (plaintext) |
| User Type | SEC-USR-TYPE | `PIC X(01)` | Flag | 1 | Role: A=Admin, U=User |

**Key**: SEC-USR-ID  
**Storage**: VSAM KSDS (`AWS.M2.CARDDEMO.USRSEC`)  
**Security Note**: Passwords stored in plaintext — modernization should add hashing

---

## 12 — Pending Authorization Summary (IMS — `CIPAUSMY`)

Aggregate authorization statistics per account. IMS hierarchical segment.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Account ID | PA-ACCT-ID | `PIC S9(11) COMP-3` | Packed Decimal | 6 | Account reference |
| Customer ID | PA-CUST-ID | `PIC 9(09)` | Numeric | 9 | Customer reference |
| Auth Status | PA-AUTH-STATUS | `PIC X(01)` | Flag | 1 | Overall authorization status |
| Account Statuses | PA-ACCOUNT-STATUS | `PIC X(02) OCCURS 5` | Alpha Array | 10 | Status history (5 entries) |
| Credit Limit | PA-CREDIT-LIMIT | `PIC S9(09)V99 COMP-3` | Packed Decimal | 6 | Credit limit |
| Cash Limit | PA-CASH-LIMIT | `PIC S9(09)V99 COMP-3` | Packed Decimal | 6 | Cash advance limit |
| Credit Balance | PA-CREDIT-BALANCE | `PIC S9(09)V99 COMP-3` | Packed Decimal | 6 | Outstanding credit balance |
| Cash Balance | PA-CASH-BALANCE | `PIC S9(09)V99 COMP-3` | Packed Decimal | 6 | Outstanding cash balance |
| Approved Count | PA-APPROVED-AUTH-CNT | `PIC S9(04) COMP` | Binary | 2 | Count of approved authorizations |
| Declined Count | PA-DECLINED-AUTH-CNT | `PIC S9(04) COMP` | Binary | 2 | Count of declined authorizations |
| Approved Amount | PA-APPROVED-AUTH-AMT | `PIC S9(09)V99 COMP-3` | Packed Decimal | 6 | Total approved amount |
| Declined Amount | PA-DECLINED-AUTH-AMT | `PIC S9(09)V99 COMP-3` | Packed Decimal | 6 | Total declined amount |

**Storage**: IMS DB (`DBPAUTP0`)

---

## 13 — Pending Authorization Detail (IMS — `CIPAUDTY`)

Individual authorization request detail. IMS child segment.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Auth Date Key | PA-AUTH-DATE-9C | `PIC S9(05) COMP-3` | Packed Decimal | 3 | Compressed auth date key |
| Auth Time Key | PA-AUTH-TIME-9C | `PIC S9(09) COMP-3` | Packed Decimal | 5 | Compressed auth time key |
| Orig Date | PA-AUTH-ORIG-DATE | `PIC X(06)` | Alpha | 6 | Original authorization date |
| Orig Time | PA-AUTH-ORIG-TIME | `PIC X(06)` | Alpha | 6 | Original authorization time |
| Card Number | PA-CARD-NUM | `PIC X(16)` | Alpha | 16 | Card used for authorization |
| Auth Type | PA-AUTH-TYPE | `PIC X(04)` | Alpha | 4 | Authorization type code |
| Card Expiry | PA-CARD-EXPIRY-DATE | `PIC X(04)` | Alpha | 4 | Card expiry (MMYY) |
| Message Type | PA-MESSAGE-TYPE | `PIC X(06)` | Alpha | 6 | Message type (ISO 8583) |
| Message Source | PA-MESSAGE-SOURCE | `PIC X(06)` | Alpha | 6 | Originating system |
| Auth ID Code | PA-AUTH-ID-CODE | `PIC X(06)` | Alpha | 6 | Authorization ID assigned |
| Response Code | PA-AUTH-RESP-CODE | `PIC X(02)` | Alpha | 2 | Response code (00=Approved) |
| Response Reason | PA-AUTH-RESP-REASON | `PIC X(04)` | Alpha | 4 | Decline reason code |
| Processing Code | PA-PROCESSING-CODE | `PIC 9(06)` | Numeric | 6 | ISO processing code |
| Transaction Amt | PA-TRANSACTION-AMT | `PIC S9(10)V99 COMP-3` | Packed Decimal | 7 | Requested amount |
| Approved Amount | PA-APPROVED-AMT | `PIC S9(10)V99 COMP-3` | Packed Decimal | 7 | Amount approved |
| Merchant Category | PA-MERCHANT-CATAGORY-CODE | `PIC X(04)` | Alpha | 4 | MCC code |
| Country Code | PA-ACQR-COUNTRY-CODE | `PIC X(03)` | Alpha | 3 | Acquirer country |
| POS Entry Mode | PA-POS-ENTRY-MODE | `PIC 9(02)` | Numeric | 2 | Point-of-sale entry mode |
| Merchant ID | PA-MERCHANT-ID | `PIC X(15)` | Alpha | 15 | Merchant identifier |
| Merchant Name | PA-MERCHANT-NAME | `PIC X(22)` | Alpha | 22 | Merchant business name |
| Merchant City | PA-MERCHANT-CITY | `PIC X(13)` | Alpha | 13 | Merchant city |

**Storage**: IMS DB (`DBPAUTP0`, child of Summary segment)

---

## 14 — Authorization MQ Messages

### 14.1 Authorization Request (`CCPAURQY`)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Card Number | CC-AURQ-CARD-NUM | `PIC X(16)` | Alpha | 16 | Card for authorization |
| Card Expiry | CC-AURQ-CARD-EXP | `PIC X(04)` | Alpha | 4 | Card expiration date |
| Transaction Amount | CC-AURQ-TRAN-AMT | `PIC S9(10)V99` | Signed Decimal | 12+2 | Amount to authorize |
| Processing Code | CC-AURQ-PROC-CD | `PIC 9(06)` | Numeric | 6 | Processing code |
| Merchant Category | CC-AURQ-MER-CAT | `PIC X(04)` | Alpha | 4 | Merchant category code |
| Merchant ID | CC-AURQ-MER-ID | `PIC X(15)` | Alpha | 15 | Merchant identifier |
| Merchant Name | CC-AURQ-MER-NAME | `PIC X(22)` | Alpha | 22 | Merchant name |

**Transport**: IBM MQ (request queue)

### 14.2 Authorization Reply (`CCPAURLY`)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Card Number | CC-AURP-CARD-NUM | `PIC X(16)` | Alpha | 16 | Card authorized |
| Response Code | CC-AURP-RESP-CD | `PIC X(02)` | Alpha | 2 | Authorization result |
| Approved Amount | CC-AURP-APPR-AMT | `PIC S9(10)V99` | Signed Decimal | 12+2 | Approved monetary amount |
| Auth ID | CC-AURP-AUTH-ID | `PIC X(06)` | Alpha | 6 | Authorization reference |

**Transport**: IBM MQ (reply queue)

---

## 15 — Export Composite Record (`CVEXPORT`)

Multi-format record for branch migration. Uses REDEFINES for polymorphism. Record length: 500 bytes.

### 15.1 Header Fields (common to all record types)

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|-----:|----------------------|
| Record Type | EXPORT-REC-TYPE | `PIC X(1)` | Flag | 1 | C=Customer, A=Account, X=Xref, T=Transaction, R=Card |
| Timestamp | EXPORT-TIMESTAMP | `PIC X(26)` | Timestamp | 26 | Export datetime |
| Sequence Number | EXPORT-SEQUENCE-NUM | `PIC 9(9) COMP` | Binary | 4 | Sequential record counter |
| Branch ID | EXPORT-BRANCH-ID | `PIC X(4)` | Alpha | 4 | Originating branch |
| Region Code | EXPORT-REGION-CODE | `PIC X(5)` | Alpha | 5 | Geographic region |

### 15.2 Data Variants (via REDEFINES on EXPORT-RECORD-DATA)

| Variant | Redefines Name | Content |
|---------|---------------|---------|
| Customer | EXPORT-CUSTOMER-DATA | Full customer fields (mirrors CVCUS01Y) with COMP/COMP-3 storage |
| Account | EXPORT-ACCOUNT-DATA | Full account fields (mirrors CVACT01Y) with COMP-3 for balances |
| Cross-Reference | EXPORT-XREF-DATA | Card-Account-Customer link |
| Transaction | EXPORT-TRANSACTION-DATA | Full transaction fields with COMP-3 amounts and OCCURS DEPENDING ON |
| Card | EXPORT-CARD-DATA | Card number, CVV, embossed name, status |

**Advanced COBOL features**: REDEFINES, OCCURS, OCCURS DEPENDING ON, COMP, COMP-3

---

## Entity Relationship Summary

```
┌──────────┐     ┌──────────────┐     ┌──────────┐
│ Customer │────▶│ Cross-Ref    │◀────│ Account  │
│ CVCUS01Y │  1:N│ CVACT03Y     │N:1  │ CVACT01Y │
└──────────┘     │ (Card↔Cust↔  │     └────┬─────┘
                 │   Acct)       │          │
                 └──────┬───────┘          │ 1:N
                        │                  ▼
                   1:1  │          ┌──────────────┐
                        ▼          │ Tran Cat Bal  │
                 ┌──────────┐     │ CVTRA01Y      │
                 │   Card    │     └──────────────┘
                 │ CVACT02Y  │            ▲
                 └────┬─────┘            │
                      │ 1:N              │ N:1
                      ▼            ┌─────┴────────┐
               ┌──────────────┐    │ Disclosure   │
               │ Transaction  │    │ CVTRA02Y     │
               │ CVTRA05Y     │    └──────────────┘
               └──────┬───────┘
                      │ N:1         ┌──────────────┐
                      ├────────────▶│ Tran Type    │
                      │             │ CVTRA03Y     │
                      │ N:1         └──────────────┘
                      └────────────▶┌──────────────┐
                                    │ Tran Category│
                                    │ CVTRA04Y     │
                                    └──────────────┘

┌──────────────┐     ┌──────────────────┐
│ Daily Tran   │────▶│ Transaction      │  (via CBTRN01C/02C posting)
│ CVTRA06Y     │     │ CVTRA05Y         │
└──────────────┘     └──────────────────┘

┌──────────────┐
│ User Security│  (standalone – linked by session, not by FK)
│ CSUSR01Y     │
└──────────────┘

┌──────────────────────────────────────────┐
│ Pending Authorization (IMS Hierarchy)    │
│ ┌────────────────────┐                   │
│ │ Summary (CIPAUSMY) │◀── Account FK     │
│ │   PA-ACCT-ID       │                   │
│ └────────┬───────────┘                   │
│          │ 1:N                           │
│          ▼                               │
│ ┌────────────────────┐                   │
│ │ Detail (CIPAUDTY)  │◀── Card FK        │
│ │  PA-CARD-NUM       │                   │
│ └────────────────────┘                   │
└──────────────────────────────────────────┘
```

---

## Data Format Reference

| PIC Notation | Business Type | Example |
|-------------|---------------|---------|
| `PIC X(n)` | Alphanumeric string | Names, codes, addresses |
| `PIC 9(n)` | Unsigned integer | IDs, counts |
| `PIC S9(n)V99` | Signed decimal (zoned) | Monetary amounts — sign + n digits + 2 decimal |
| `PIC S9(n)V99 COMP-3` | Packed decimal | Space-optimized monetary amounts |
| `PIC S9(n) COMP` | Binary integer | Lengths, counters |
| `PIC 9(n) COMP` | Unsigned binary | Sequence numbers |
| `OCCURS n TIMES` | Fixed-length array | Repeating fields |
| `OCCURS DEPENDING ON` | Variable-length array | Dynamic repeat count |
| `REDEFINES` | Union / variant record | Multi-type export record |
