# CardDemo Data Dictionary

> Business-friendly extraction of all data entities from COBOL copybook PIC clauses, organized by business domain.

---

## 1. Account Master (`CVACT01Y` — 300 bytes)

The central account record stored in `ACCTFILE` VSAM KSDS.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|------|---------------------|
| Account ID | `ACCT-ID` | `PIC 9(11)` | Numeric | 11 digits | Unique account identifier |
| Active Status | `ACCT-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 char | Account status flag (Y=Active) |
| Current Balance | `ACCT-CURR-BAL` | `PIC S9(10)V99` | Signed Decimal | 12 digits + 2 dec | Current outstanding balance |
| Credit Limit | `ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | Signed Decimal | 12 digits + 2 dec | Maximum credit allowed |
| Cash Advance Limit | `ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | Signed Decimal | 12 digits + 2 dec | Maximum cash advance allowed |
| Open Date | `ACCT-OPEN-DATE` | `PIC X(10)` | Alpha | 10 chars | Date account was opened |
| Expiration Date | `ACCT-EXPIRAION-DATE` | `PIC X(10)` | Alpha | 10 chars | Account expiration date |
| Reissue Date | `ACCT-REISSUE-DATE` | `PIC X(10)` | Alpha | 10 chars | Last card reissue date |
| Current Cycle Credit | `ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | Signed Decimal | 12 digits + 2 dec | Credits applied in current cycle |
| Current Cycle Debit | `ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | Signed Decimal | 12 digits + 2 dec | Debits applied in current cycle |
| Account Group ID | `ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 chars | Account group/portfolio identifier |
| Filler | `FILLER` | `PIC X(178)` | Alpha | 178 chars | Reserved for future use |

**VSAM File:** `ACCTFILE` — KSDS, keyed on `ACCT-ID`

---

## 2. Card Data (`CVACT02Y` — 150 bytes)

Credit card records stored in `CARDFILE` VSAM KSDS.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|------|---------------------|
| Card Number | `CARD-NUM` | `PIC X(16)` | Alpha | 16 chars | Full credit card number |
| Account ID | `CARD-ACCT-ID` | `PIC 9(11)` | Numeric | 11 digits | Owning account identifier |
| CVV Code | `CARD-CVV-CD` | `PIC 9(03)` | Numeric | 3 digits | Card verification value |
| Embossed Name | `CARD-EMBOSSED-NAME` | `PIC X(50)` | Alpha | 50 chars | Name printed on card |
| Expiration Date | `CARD-EXPIRAION-DATE` | `PIC X(10)` | Alpha | 10 chars | Card expiration date |
| Active Status | `CARD-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 char | Card status (Y=Active, N=Inactive) |
| Filler | `FILLER` | `PIC X(59)` | Alpha | 59 chars | Reserved for future use |

**VSAM File:** `CARDFILE` — KSDS, keyed on `CARD-NUM`

---

## 3. Card Cross-Reference (`CVACT03Y` — 50 bytes)

Maps card numbers to account IDs for lookup. Stored in `CARDXREF` VSAM KSDS.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|------|---------------------|
| Card Number | `XREF-CARD-NUM` | `PIC X(16)` | Alpha | 16 chars | Credit card number (key) |
| Account ID | `XREF-ACCT-ID` | `PIC 9(11)` | Numeric | 11 digits | Associated account |
| Filler | `FILLER` | `PIC X(23)` | Alpha | 23 chars | Reserved |

**VSAM File:** `CARDXREF` — KSDS, keyed on `XREF-CARD-NUM`

---

## 4. Customer Master (`CVCUS01Y` — 500 bytes)

Customer personal information stored in `CUSTFILE` VSAM KSDS.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|------|---------------------|
| Customer ID | `CUST-ID` | `PIC 9(09)` | Numeric | 9 digits | Unique customer identifier |
| First Name | `CUST-FIRST-NAME` | `PIC X(25)` | Alpha | 25 chars | Customer first name |
| Middle Name | `CUST-MIDDLE-NAME` | `PIC X(25)` | Alpha | 25 chars | Customer middle name |
| Last Name | `CUST-LAST-NAME` | `PIC X(25)` | Alpha | 25 chars | Customer last name |
| Address Line 1 | `CUST-ADDR-LINE-1` | `PIC X(50)` | Alpha | 50 chars | Street address line 1 |
| Address Line 2 | `CUST-ADDR-LINE-2` | `PIC X(50)` | Alpha | 50 chars | Street address line 2 |
| Address Line 3 | `CUST-ADDR-LINE-3` | `PIC X(50)` | Alpha | 50 chars | Street address line 3 |
| State/Code | `CUST-ADDR-STATE-CD` | `PIC X(02)` | Alpha | 2 chars | US state code |
| Country | `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | Alpha | 3 chars | Country code |
| Zip Code | `CUST-ADDR-ZIP` | `PIC X(10)` | Alpha | 10 chars | Postal/zip code |
| Phone 1 | `CUST-PHONE-NUM-1` | `PIC X(15)` | Alpha | 15 chars | Primary phone number |
| Phone 2 | `CUST-PHONE-NUM-2` | `PIC X(15)` | Alpha | 15 chars | Secondary phone number |
| SSN | `CUST-SSN` | `PIC 9(09)` | Numeric | 9 digits | Social Security Number |
| Government ID | `CUST-GOVT-ISSUED-ID` | `PIC X(20)` | Alpha | 20 chars | Government-issued ID number |
| Date of Birth | `CUST-DOB-YYYYMMDD` | `PIC X(10)` | Alpha | 10 chars | Date of birth |
| EFT Account | `CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | Alpha | 10 chars | Electronic funds transfer account |
| PRI Card Holder | `CUST-PRI-CARD-HOLDER-IND` | `PIC X(01)` | Alpha | 1 char | Primary card holder indicator |
| FICO Score | `CUST-FICO-CREDIT-SCORE` | `PIC 9(03)` | Numeric | 3 digits | Credit score (300-850) |
| Filler | `FILLER` | `PIC X(168)` | Alpha | 168 chars | Reserved for future use |

**VSAM File:** `CUSTFILE` — KSDS, keyed on `CUST-ID`

---

## 5. Transaction Record (`CVTRA05Y` — 350 bytes)

Transaction master records stored in `TRANSACT` VSAM KSDS.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|------|---------------------|
| Transaction ID | `TRAN-ID` | `PIC X(16)` | Alpha | 16 chars | Unique transaction identifier |
| Type Code | `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | Transaction type (e.g., SA=Sale, CR=Credit) |
| Category Code | `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 digits | Transaction category code |
| Source | `TRAN-SOURCE` | `PIC X(10)` | Alpha | 10 chars | Transaction origination source |
| Description | `TRAN-DESC` | `PIC X(100)` | Alpha | 100 chars | Free-text transaction description |
| Amount | `TRAN-AMT` | `PIC S9(09)V99` | Signed Decimal | 9 digits + 2 dec | Transaction amount |
| Merchant ID | `TRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 digits | Merchant identifier |
| Merchant Name | `TRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 chars | Merchant business name |
| Merchant City | `TRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 chars | Merchant city |
| Merchant Zip | `TRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 chars | Merchant postal code |
| Card Number | `TRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 chars | Card used for transaction |
| Origination Timestamp | `TRAN-ORIG-TS` | `PIC X(26)` | Alpha | 26 chars | When transaction was initiated |
| Processing Timestamp | `TRAN-PROC-TS` | `PIC X(26)` | Alpha | 26 chars | When transaction was processed |
| Filler | `FILLER` | `PIC X(20)` | Alpha | 20 chars | Reserved |

**VSAM File:** `TRANSACT` — KSDS, keyed on `TRAN-ID`

---

## 6. Daily Transaction Record (`CVTRA06Y` — 350 bytes)

Inbound daily transactions for batch posting. Same layout as CVTRA05Y with `DALYTRAN-` prefix.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|------|---------------------|
| Transaction ID | `DALYTRAN-ID` | `PIC X(16)` | Alpha | 16 chars | Daily transaction identifier |
| Type Code | `DALYTRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | Transaction type code |
| Category Code | `DALYTRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 digits | Transaction category |
| Source | `DALYTRAN-SOURCE` | `PIC X(10)` | Alpha | 10 chars | Origination source |
| Description | `DALYTRAN-DESC` | `PIC X(100)` | Alpha | 100 chars | Transaction description |
| Amount | `DALYTRAN-AMT` | `PIC S9(09)V99` | Signed Decimal | 9 digits + 2 dec | Transaction amount |
| Merchant ID | `DALYTRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 digits | Merchant ID |
| Merchant Name | `DALYTRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 chars | Merchant name |
| Merchant City | `DALYTRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 chars | Merchant city |
| Merchant Zip | `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 chars | Merchant zip |
| Card Number | `DALYTRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 chars | Card number |
| Origination Timestamp | `DALYTRAN-ORIG-TS` | `PIC X(26)` | Alpha | 26 chars | When initiated |
| Processing Timestamp | `DALYTRAN-PROC-TS` | `PIC X(26)` | Alpha | 26 chars | When processed |
| Filler | `FILLER` | `PIC X(20)` | Alpha | 20 chars | Reserved |

**VSAM File:** `DALYTRAN` — Sequential input file for batch posting

---

## 7. Transaction Category Balance (`CVTRA01Y` — 50 bytes)

Running balance per account per transaction type/category. Stored in `TCATBALF` VSAM KSDS.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|------|---------------------|
| Account ID | `TRANCAT-ACCT-ID` | `PIC 9(11)` | Numeric | 11 digits | Account identifier |
| Type Code | `TRANCAT-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | Transaction type |
| Category Code | `TRANCAT-CD` | `PIC 9(04)` | Numeric | 4 digits | Transaction category |
| Balance | `TRAN-CAT-BAL` | `PIC S9(09)V99` | Signed Decimal | 9 digits + 2 dec | Running balance for this category |
| Filler | `FILLER` | `PIC X(22)` | Alpha | 22 chars | Reserved |

**Composite Key:** Account ID + Type Code + Category Code

---

## 8. Disclosure Group (`CVTRA02Y` — 50 bytes)

Interest rate assignments by account group and transaction category.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|------|---------------------|
| Account Group ID | `DIS-ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 chars | Account portfolio group |
| Transaction Type | `DIS-TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | Transaction type code |
| Category Code | `DIS-TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 digits | Transaction category |
| Interest Rate | `DIS-INT-RATE` | `PIC S9(04)V99` | Signed Decimal | 4 digits + 2 dec | Annual interest rate percentage |
| Filler | `FILLER` | `PIC X(28)` | Alpha | 28 chars | Reserved |

---

## 9. Transaction Type (`CVTRA03Y` — 60 bytes)

Reference table for transaction type codes.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|------|---------------------|
| Type Code | `TRAN-TYPE` | `PIC X(02)` | Alpha | 2 chars | Transaction type code (e.g., SA, CR, PR) |
| Description | `TRAN-TYPE-DESC` | `PIC X(50)` | Alpha | 50 chars | Human-readable type name |
| Filler | `FILLER` | `PIC X(08)` | Alpha | 8 chars | Reserved |

**VSAM File:** `TRANTYPE` — KSDS, keyed on `TRAN-TYPE`

---

## 10. Transaction Category (`CVTRA04Y` — 60 bytes)

Reference table for transaction category codes within each type.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|------|---------------------|
| Type Code | `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | Parent transaction type |
| Category Code | `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 digits | Category code within type |
| Description | `TRAN-CAT-TYPE-DESC` | `PIC X(50)` | Alpha | 50 chars | Category description |
| Filler | `FILLER` | `PIC X(04)` | Alpha | 4 chars | Reserved |

**Composite Key:** Type Code + Category Code

---

## 11. User Security (`CSUSR01Y` — 80 bytes)

User authentication records stored in `USRSEC` VSAM KSDS.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|------|---------------------|
| User ID | `SEC-USR-ID` | `PIC X(08)` | Alpha | 8 chars | Login user identifier |
| First Name | `SEC-USR-FNAME` | `PIC X(20)` | Alpha | 20 chars | User first name |
| Last Name | `SEC-USR-LNAME` | `PIC X(20)` | Alpha | 20 chars | User last name |
| Password | `SEC-USR-PWD` | `PIC X(08)` | Alpha | 8 chars | Login password (plaintext) |
| User Type | `SEC-USR-TYPE` | `PIC X(01)` | Alpha | 1 char | A=Admin, R=Regular |
| Filler | `FILLER` | `PIC X(23)` | Alpha | 23 chars | Reserved |

**VSAM File:** `USRSEC` — KSDS, keyed on `SEC-USR-ID`

---

## 12. Statement Transaction Layout (`COSTM01` — 350 bytes)

Re-keyed transaction layout for statement generation (keyed by card + transaction ID).

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|------|---------------------|
| Card Number | `TRNX-CARD-NUM` | `PIC X(16)` | Alpha | 16 chars | Card number (primary sort key) |
| Transaction ID | `TRNX-ID` | `PIC X(16)` | Alpha | 16 chars | Transaction ID (secondary key) |
| Type Code | `TRNX-TYPE-CD` | `PIC X(02)` | Alpha | 2 chars | Transaction type |
| Category Code | `TRNX-CAT-CD` | `PIC 9(04)` | Numeric | 4 digits | Transaction category |
| Source | `TRNX-SOURCE` | `PIC X(10)` | Alpha | 10 chars | Transaction source |
| Description | `TRNX-DESC` | `PIC X(100)` | Alpha | 100 chars | Transaction description |
| Amount | `TRNX-AMT` | `PIC S9(09)V99` | Signed Decimal | 9 digits + 2 dec | Transaction amount |
| Merchant ID | `TRNX-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 digits | Merchant ID |
| Merchant Name | `TRNX-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 chars | Merchant name |
| Merchant City | `TRNX-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 chars | Merchant city |
| Merchant Zip | `TRNX-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 chars | Merchant zip |
| Origination Timestamp | `TRNX-ORIG-TS` | `PIC X(26)` | Alpha | 26 chars | Origination time |
| Processing Timestamp | `TRNX-PROC-TS` | `PIC X(26)` | Alpha | 26 chars | Processing time |
| Filler | `FILLER` | `PIC X(20)` | Alpha | 20 chars | Reserved |

**Composite Key:** Card Number + Transaction ID

---

## 13. Export Record (`CVEXPORT`)

Multi-record export layout for branch data migration.

| Field | COBOL Name | PIC Clause | Type | Size | Business Description |
|-------|-----------|------------|------|------|---------------------|
| Record Type | `EXP-RECORD-TYPE` | `PIC X(01)` | Alpha | 1 char | H=Header, C=Customer, A=Account, X=Card Xref, T=Transaction |
| Customer ID | `EXP-CUST-ID` | `PIC 9(09)` | Numeric | 9 digits | Customer identifier |
| Customer Name Fields | `EXP-CUST-FIRST-NAME` etc. | `PIC X(25)` | Alpha | 25 chars each | First/Middle/Last name |
| Account Fields | `EXP-ACCT-ID` etc. | Various | Various | Various | Account data subset |
| Card Fields | `EXP-CARD-NUM` etc. | Various | Various | Various | Card data subset |

---

## VSAM File Summary

| Logical Name | Physical Dataset Pattern | Record Layout | Key | Record Length |
|-------------|------------------------|---------------|-----|-------------|
| ACCTFILE | AWS.M2.CARDDEMO.ACCTDATA.PS → VSAM | CVACT01Y | ACCT-ID | 300 |
| CARDFILE | AWS.M2.CARDDEMO.CARDDATA.PS → VSAM | CVACT02Y | CARD-NUM | 150 |
| CUSTFILE | AWS.M2.CARDDEMO.CUSTDATA.PS → VSAM | CVCUS01Y | CUST-ID | 500 |
| CARDXREF | AWS.M2.CARDDEMO.CARDXREF.PS → VSAM | CVACT03Y | XREF-CARD-NUM | 50 |
| TRANSACT | AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS | CVTRA05Y | TRAN-ID | 350 |
| DALYTRAN | AWS.M2.CARDDEMO.DALYTRAN.PS | CVTRA06Y | Sequential | 350 |
| USRSEC | AWS.M2.CARDDEMO.USRSEC.PS → VSAM | CSUSR01Y | SEC-USR-ID | 80 |
| TCATBALF | AWS.M2.CARDDEMO.TCATBAL.PS → VSAM | CVTRA01Y | Composite | 50 |
| DISCGRP | AWS.M2.CARDDEMO.DISCGRP.PS → VSAM | CVTRA02Y | Composite | 50 |
| TRANTYPE | AWS.M2.CARDDEMO.TRANTYPE.PS → VSAM | CVTRA03Y | TRAN-TYPE | 60 |
| TRANCATG | AWS.M2.CARDDEMO.TRANCATG.PS → VSAM | CVTRA04Y | Composite | 60 |

---

## Entity Relationship Summary

```
CUSTOMER (1) ──── (N) ACCOUNT ──── (N) CARD
     │                    │                │
     │                    │                │
     │                    ▼                ▼
     │            TRAN-CAT-BALANCE    CARD-XREF
     │                    │
     │                    ▼
     └───────────── TRANSACTION ◄─── DAILY-TRANSACTION
                          │
                          ▼
                    TRAN-TYPE / TRAN-CATEGORY
                          │
                          ▼
                    DISCLOSURE-GROUP (interest rates)
```

## Data Type Reference

| COBOL PIC | Java Equivalent | Description |
|-----------|----------------|-------------|
| `PIC X(n)` | `String` | Fixed-length alphanumeric |
| `PIC 9(n)` | `long` / `int` | Unsigned integer |
| `PIC S9(n)V99` | `BigDecimal` | Signed decimal with 2 decimal places |
| `PIC S9(n)` | `long` | Signed integer |
| `FILLER` | — | Unused padding bytes |
