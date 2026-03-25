# CardDemo Data Dictionary

> **Application:** AWS CardDemo &mdash; Mainframe Credit Card Management System  
> **Generated:** 2026-03-25 | **Source Repo:** `uc-legacy-modernization-cobol-to-java`

---

## Overview

This document extracts all business data entities from the CardDemo copybook PIC clauses and presents them in a business-friendly format. Each entity maps to a VSAM KSDS file on the mainframe and will map to a relational database table in the modernized Java application.

---

## 1. Account Master

> **Copybook:** `CVACT01Y.cpy` | **Record Length:** 300 bytes  
> **VSAM Dataset:** `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`  
> **Used By:** COACTVWC, COACTUPC, CBACT01C, CBACT04C, CBTRN01C, CBTRN02C, CBSTM03A, CBEXPORT, CBIMPORT

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|---|---|---|---|---|
| 1 | `ACCT-ID` | `PIC 9(11)` | Numeric | 11 | **Primary Key.** Unique account identifier |
| 2 | `ACCT-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 | Account status flag (Y=Active, N=Inactive) |
| 3 | `ACCT-CURR-BAL` | `PIC S9(10)V99` | Signed Decimal | 12.2 | Current account balance |
| 4 | `ACCT-CREDIT-LIMIT` | `PIC S9(10)V99` | Signed Decimal | 12.2 | Maximum credit limit |
| 5 | `ACCT-CASH-CREDIT-LIMIT` | `PIC S9(10)V99` | Signed Decimal | 12.2 | Cash advance credit limit |
| 6 | `ACCT-OPEN-DATE` | `PIC X(10)` | Alpha | 10 | Account opening date (YYYY-MM-DD) |
| 7 | `ACCT-EXPIRAION-DATE` | `PIC X(10)` | Alpha | 10 | Account expiration date |
| 8 | `ACCT-REISSUE-DATE` | `PIC X(10)` | Alpha | 10 | Last card reissue date |
| 9 | `ACCT-CURR-CYC-CREDIT` | `PIC S9(10)V99` | Signed Decimal | 12.2 | Current billing cycle credits |
| 10 | `ACCT-CURR-CYC-DEBIT` | `PIC S9(10)V99` | Signed Decimal | 12.2 | Current billing cycle debits |
| 11 | `ACCT-ADDR-ZIP` | `PIC X(10)` | Alpha | 10 | Account billing ZIP code |
| 12 | `ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 | Account group / disclosure group ID |
| 13 | `FILLER` | `PIC X(178)` | Filler | 178 | Reserved space |

**Business Rules:**
- Balance = Prior Balance + Debits - Credits + Interest + Fees
- Interest calculated by CBACT04C using disclosure group rates
- Credit limit enforced during transaction posting (CBTRN02C)

---

## 2. Card Master

> **Copybook:** `CVACT02Y.cpy` | **Record Length:** 150 bytes  
> **VSAM Dataset:** `AWS.M2.CARDDEMO.CARDDATA.VSAM.KSDS`  
> **Used By:** COCRDLIC, COCRDSLC, COCRDUPC, CBACT02C, CBTRN01C, CBEXPORT, CBIMPORT

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|---|---|---|---|---|
| 1 | `CARD-NUM` | `PIC X(16)` | Alpha | 16 | **Primary Key.** 16-digit credit card number |
| 2 | `CARD-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | **FK to Account.** Owning account ID |
| 3 | `CARD-CVV-CD` | `PIC 9(03)` | Numeric | 3 | Card verification value (CVV) |
| 4 | `CARD-EMBOSSED-NAME` | `PIC X(50)` | Alpha | 50 | Cardholder name as embossed |
| 5 | `CARD-EXPIRAION-DATE` | `PIC X(10)` | Alpha | 10 | Card expiration date |
| 6 | `CARD-ACTIVE-STATUS` | `PIC X(01)` | Alpha | 1 | Card status (Y=Active, N=Inactive) |
| 7 | `FILLER` | `PIC X(59)` | Filler | 59 | Reserved space |

**Business Rules:**
- Multiple cards can be linked to one account
- Card number is the key for cross-reference lookups
- Card status must be Active for transaction processing

---

## 3. Customer Master

> **Copybook:** `CVCUS01Y.cpy` | **Record Length:** 500 bytes  
> **VSAM Dataset:** `AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS`  
> **Used By:** CBCUS01C, CBTRN01C, CBSTM03A, CBEXPORT, CBIMPORT

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|---|---|---|---|---|
| 1 | `CUST-ID` | `PIC 9(09)` | Numeric | 9 | **Primary Key.** Unique customer identifier |
| 2 | `CUST-FIRST-NAME` | `PIC X(25)` | Alpha | 25 | Customer first name |
| 3 | `CUST-MIDDLE-NAME` | `PIC X(25)` | Alpha | 25 | Customer middle name |
| 4 | `CUST-LAST-NAME` | `PIC X(25)` | Alpha | 25 | Customer last name |
| 5 | `CUST-ADDR-LINE-1` | `PIC X(50)` | Alpha | 50 | Street address line 1 |
| 6 | `CUST-ADDR-LINE-2` | `PIC X(50)` | Alpha | 50 | Street address line 2 |
| 7 | `CUST-ADDR-LINE-3` | `PIC X(50)` | Alpha | 50 | Street address line 3 |
| 8 | `CUST-ADDR-STATE-CD` | `PIC X(02)` | Alpha | 2 | State code |
| 9 | `CUST-ADDR-COUNTRY-CD` | `PIC X(03)` | Alpha | 3 | Country code |
| 10 | `CUST-ADDR-ZIP` | `PIC X(10)` | Alpha | 10 | ZIP / postal code |
| 11 | `CUST-PHONE-NUM-1` | `PIC X(15)` | Alpha | 15 | Primary phone number |
| 12 | `CUST-PHONE-NUM-2` | `PIC X(15)` | Alpha | 15 | Secondary phone number |
| 13 | `CUST-SSN` | `PIC 9(09)` | Numeric | 9 | Social Security Number |
| 14 | `CUST-GOVT-ISSUED-ID` | `PIC X(20)` | Alpha | 20 | Government-issued ID |
| 15 | `CUST-DOB-YYYY-MM-DD` | `PIC X(10)` | Alpha | 10 | Date of birth |
| 16 | `CUST-EFT-ACCOUNT-ID` | `PIC X(10)` | Alpha | 10 | Electronic fund transfer account |
| 17 | `CUST-PRI-CARD-HOLDER-IND` | `PIC X(01)` | Alpha | 1 | Primary cardholder indicator |
| 18 | `CUST-FICO-CREDIT-SCORE` | `PIC 9(03)` | Numeric | 3 | FICO credit score |
| 19 | `FILLER` | `PIC X(168)` | Filler | 168 | Reserved space |

**Business Rules:**
- Customer is linked to cards via the cross-reference file
- SSN is sensitive PII &mdash; must be protected in modernized app
- FICO score used for credit decisions

---

## 4. Card Cross-Reference

> **Copybook:** `CVACT03Y.cpy` | **Record Length:** 50 bytes  
> **VSAM Dataset:** `AWS.M2.CARDDEMO.CARDXREF.VSAM.KSDS` (+ Alternate Index on ACCT-ID)  
> **Used By:** CBACT03C, CBACT04C, CBTRN01C, CBTRN02C, CBTRN03C, CBSTM03A, COTRN02C, CBEXPORT, CBIMPORT

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|---|---|---|---|---|
| 1 | `XREF-CARD-NUM` | `PIC X(16)` | Alpha | 16 | **Primary Key.** Card number |
| 2 | `XREF-CUST-ID` | `PIC 9(09)` | Numeric | 9 | **FK to Customer.** Customer ID |
| 3 | `XREF-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | **FK to Account.** Account ID |
| 4 | `FILLER` | `PIC X(14)` | Filler | 14 | Reserved space |

**Business Rules:**
- Central junction table linking Card &rarr; Customer &rarr; Account
- Alternate index on ACCT-ID allows lookup by account
- Used by nearly every transaction processing program

---

## 5. Transaction Master

> **Copybook:** `CVTRA05Y.cpy` | **Record Length:** 350 bytes  
> **VSAM Dataset:** `AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS`  
> **Used By:** COTRN00C, COTRN01C, COTRN02C, CBTRN02C, CBTRN03C, CBACT04C, CBSTM03A, CBEXPORT, CBIMPORT

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|---|---|---|---|---|
| 1 | `TRAN-ID` | `PIC X(16)` | Alpha | 16 | **Primary Key.** Unique transaction ID |
| 2 | `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type code (e.g., SA=Sale, CR=Credit) |
| 3 | `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category code |
| 4 | `TRAN-SOURCE` | `PIC X(10)` | Alpha | 10 | Transaction source (POS, ATM, Online, etc.) |
| 5 | `TRAN-DESC` | `PIC X(100)` | Alpha | 100 | Transaction description |
| 6 | `TRAN-AMT` | `PIC S9(09)V99` | Signed Decimal | 11.2 | Transaction amount |
| 7 | `TRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant identifier |
| 8 | `TRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant name |
| 9 | `TRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant city |
| 10 | `TRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP code |
| 11 | `TRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 | **FK to Card.** Card used for transaction |
| 12 | `TRAN-ORIG-TS` | `PIC X(26)` | Alpha | 26 | Original transaction timestamp |
| 13 | `TRAN-PROC-TS` | `PIC X(26)` | Alpha | 26 | Processing timestamp |
| 14 | `FILLER` | `PIC X(20)` | Filler | 20 | Reserved space |

**Business Rules:**
- Transactions are posted from daily transaction file by CBTRN02C
- Transaction amounts are signed (negative = credit/refund)
- Card number links to cross-reference for account resolution

---

## 6. Daily Transaction (Input)

> **Copybook:** `CVTRA06Y.cpy` | **Record Length:** 350 bytes  
> **VSAM Dataset:** `AWS.M2.CARDDEMO.DALYTRAN.PS` (sequential)  
> **Used By:** CBTRN01C, CBTRN02C

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|---|---|---|---|---|
| 1 | `DALYTRAN-ID` | `PIC X(16)` | Alpha | 16 | Daily transaction ID |
| 2 | `DALYTRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type code |
| 3 | `DALYTRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category code |
| 4 | `DALYTRAN-SOURCE` | `PIC X(10)` | Alpha | 10 | Transaction source |
| 5 | `DALYTRAN-DESC` | `PIC X(100)` | Alpha | 100 | Transaction description |
| 6 | `DALYTRAN-AMT` | `PIC S9(09)V99` | Signed Decimal | 11.2 | Transaction amount |
| 7 | `DALYTRAN-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant ID |
| 8 | `DALYTRAN-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant name |
| 9 | `DALYTRAN-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant city |
| 10 | `DALYTRAN-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP |
| 11 | `DALYTRAN-CARD-NUM` | `PIC X(16)` | Alpha | 16 | Card number |
| 12 | `DALYTRAN-ORIG-TS` | `PIC X(26)` | Alpha | 26 | Original timestamp |
| 13 | `DALYTRAN-PROC-TS` | `PIC X(26)` | Alpha | 26 | Processing timestamp |
| 14 | `FILLER` | `PIC X(20)` | Filler | 20 | Reserved space |

**Business Rules:**
- Input file for the nightly batch posting cycle
- Identical layout to TRAN-RECORD but separate entity for staging
- Records validated and either posted to TRANSACT or written to DALYREJS (rejects)

---

## 7. Transaction Category Balance

> **Copybook:** `CVTRA01Y.cpy` | **Record Length:** 50 bytes  
> **VSAM Dataset:** `AWS.M2.CARDDEMO.TCATBALF.VSAM.KSDS`  
> **Used By:** CBACT04C, CBTRN02C

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|---|---|---|---|---|
| 1 | `TRANCAT-ACCT-ID` | `PIC 9(11)` | Numeric | 11 | **Composite Key Part 1.** Account ID |
| 2 | `TRANCAT-TYPE-CD` | `PIC X(02)` | Alpha | 2 | **Composite Key Part 2.** Transaction type code |
| 3 | `TRANCAT-CD` | `PIC 9(04)` | Numeric | 4 | **Composite Key Part 3.** Transaction category code |
| 4 | `TRAN-CAT-BAL` | `PIC S9(09)V99` | Signed Decimal | 11.2 | Running balance for this category |
| 5 | `FILLER` | `PIC X(22)` | Filler | 22 | Reserved space |

**Business Rules:**
- Tracks accumulated balance per account per transaction type/category
- Updated during transaction posting (CBTRN02C)
- Read by interest calculator (CBACT04C) to compute category-specific interest

---

## 8. Disclosure Group (Interest Rates)

> **Copybook:** `CVTRA02Y.cpy` | **Record Length:** 50 bytes  
> **VSAM Dataset:** `AWS.M2.CARDDEMO.DISCGRP.VSAM.KSDS`  
> **Used By:** CBACT04C

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|---|---|---|---|---|
| 1 | `DIS-ACCT-GROUP-ID` | `PIC X(10)` | Alpha | 10 | **Composite Key Part 1.** Account group ID |
| 2 | `DIS-TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | **Composite Key Part 2.** Transaction type |
| 3 | `DIS-TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | **Composite Key Part 3.** Transaction category |
| 4 | `DIS-INT-RATE` | `PIC S9(04)V99` | Signed Decimal | 6.2 | Interest rate (APR) |
| 5 | `FILLER` | `PIC X(28)` | Filler | 28 | Reserved space |

**Business Rules:**
- Defines interest rates per account group / transaction category combination
- Linked to accounts via ACCT-GROUP-ID field
- Used exclusively by CBACT04C for interest computation

---

## 9. Transaction Type (Reference)

> **Copybook:** `CVTRA03Y.cpy` | **Record Length:** 60 bytes  
> **VSAM Dataset:** `AWS.M2.CARDDEMO.TRANTYPE.VSAM.KSDS`  
> **Used By:** CBTRN03C

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|---|---|---|---|---|
| 1 | `TRAN-TYPE` | `PIC X(02)` | Alpha | 2 | **Primary Key.** Transaction type code |
| 2 | `TRAN-TYPE-DESC` | `PIC X(50)` | Alpha | 50 | Human-readable description |
| 3 | `FILLER` | `PIC X(08)` | Filler | 8 | Reserved space |

**Business Rules:**
- Reference/lookup table for transaction type codes
- Examples: SA=Sale, CR=Credit, CA=Cash Advance, BA=Balance Adjustment

---

## 10. Transaction Category Type (Reference)

> **Copybook:** `CVTRA04Y.cpy` | **Record Length:** 60 bytes  
> **VSAM Dataset:** `AWS.M2.CARDDEMO.TRANCATG.VSAM.KSDS`  
> **Used By:** CBTRN03C

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|---|---|---|---|---|
| 1 | `TRAN-TYPE-CD` | `PIC X(02)` | Alpha | 2 | **Composite Key Part 1.** Transaction type |
| 2 | `TRAN-CAT-CD` | `PIC 9(04)` | Numeric | 4 | **Composite Key Part 2.** Category code |
| 3 | `TRAN-CAT-TYPE-DESC` | `PIC X(50)` | Alpha | 50 | Category description |
| 4 | `FILLER` | `PIC X(04)` | Filler | 4 | Reserved space |

**Business Rules:**
- Subcategory within each transaction type
- Used for detailed reporting and interest rate determination

---

## 11. User Security Record

> **Copybook:** `CSUSR01Y.cpy` | **Record Length:** 80 bytes  
> **VSAM Dataset:** `AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS`  
> **Used By:** COSGN00C, COUSR00C-03C

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|---|---|---|---|---|
| 1 | `SEC-USR-ID` | `PIC X(08)` | Alpha | 8 | **Primary Key.** User login ID |
| 2 | `SEC-USR-FNAME` | `PIC X(20)` | Alpha | 20 | User first name |
| 3 | `SEC-USR-LNAME` | `PIC X(20)` | Alpha | 20 | User last name |
| 4 | `SEC-USR-PWD` | `PIC X(08)` | Alpha | 8 | User password (plaintext) |
| 5 | `SEC-USR-TYPE` | `PIC X(01)` | Alpha | 1 | User type (A=Admin, U=Regular User) |
| 6 | `SEC-USR-FILLER` | `PIC X(23)` | Filler | 23 | Reserved space |

**Business Rules:**
- ADMIN001/PASSWORD is the default admin account
- USER0001/PASSWORD is the default regular user
- Password stored in plaintext &mdash; must be hashed in modernized app
- User type determines menu routing (Admin Menu vs. Main Menu)

---

## 12. Transaction Report Layout

> **Copybook:** `CVTRA07Y.cpy` | **Record Length:** Variable (formatting)  
> **Used By:** CBTRN03C

| # | Structure | Fields | Business Description |
|---|---|---|---|
| 1 | `REPORT-NAME-HEADER` | REPT-SHORT-NAME, REPT-LONG-NAME, date range | Report title block |
| 2 | `TRANSACTION-DETAIL-REPORT` | Trans ID, Account ID, Type, Category, Source, Amount | Detail line |
| 3 | `TRANSACTION-HEADER-1/2` | Column headers, separator line | Header formatting |
| 4 | `REPORT-PAGE-TOTALS` | REPT-PAGE-TOTAL (`+ZZZ,ZZZ,ZZZ.ZZ`) | Page subtotal |
| 5 | `REPORT-ACCOUNT-TOTALS` | REPT-ACCOUNT-TOTAL | Account subtotal |
| 6 | `REPORT-GRAND-TOTALS` | REPT-GRAND-TOTAL | Grand total |

---

## 13. Statement Transaction Layout

> **Copybook:** `COSTM01.CPY` | **Record Length:** 350 bytes  
> **Used By:** CBSTM03A

| # | Field Name | COBOL PIC | Type | Size | Business Description |
|---|---|---|---|---|---|
| 1 | `TRNX-CARD-NUM` | `PIC X(16)` | Alpha | 16 | **Composite Key Part 1.** Card number |
| 2 | `TRNX-ID` | `PIC X(16)` | Alpha | 16 | **Composite Key Part 2.** Transaction ID |
| 3 | `TRNX-TYPE-CD` | `PIC X(02)` | Alpha | 2 | Transaction type |
| 4 | `TRNX-CAT-CD` | `PIC 9(04)` | Numeric | 4 | Transaction category |
| 5 | `TRNX-SOURCE` | `PIC X(10)` | Alpha | 10 | Source |
| 6 | `TRNX-DESC` | `PIC X(100)` | Alpha | 100 | Description |
| 7 | `TRNX-AMT` | `PIC S9(09)V99` | Signed Decimal | 11.2 | Amount |
| 8 | `TRNX-MERCHANT-ID` | `PIC 9(09)` | Numeric | 9 | Merchant ID |
| 9 | `TRNX-MERCHANT-NAME` | `PIC X(50)` | Alpha | 50 | Merchant name |
| 10 | `TRNX-MERCHANT-CITY` | `PIC X(50)` | Alpha | 50 | Merchant city |
| 11 | `TRNX-MERCHANT-ZIP` | `PIC X(10)` | Alpha | 10 | Merchant ZIP |
| 12 | `TRNX-ORIG-TS` | `PIC X(26)` | Alpha | 26 | Original timestamp |
| 13 | `TRNX-PROC-TS` | `PIC X(26)` | Alpha | 26 | Processing timestamp |
| 14 | `FILLER` | `PIC X(20)` | Filler | 20 | Reserved |

**Note:** This is a re-keyed version of TRAN-RECORD with Card Number + Tran ID as the composite key, used for statement generation sorted by card.

---

## 14. Export/Import Multi-Record Layout

> **Copybook:** `CVEXPORT.cpy` | **Record Length:** 500 bytes  
> **Used By:** CBEXPORT, CBIMPORT

| Record Type | Prefix | Key Fields | Business Description |
|---|---|---|---|
| `CUST` | `EXP-CUST-*` | CUST-ID, Name, Address, SSN, DOB, FICO | Customer profile export |
| `ACCT` | `EXP-ACCT-*` | ACCT-ID, Status, Balance, Limits, Dates | Account data export |
| `TRAN` | `EXP-TRAN-*` | TRAN-ID, Type, Amount, Merchant, Timestamps | Transaction export |
| `XREF` | `EXP-XREF-*` | Card Num, Customer ID, Account ID | Cross-reference export |
| `CARD` | `EXP-CARD-*` | Card Num, Account ID, CVV, Name, Expiry, Status | Card data export |

**Business Rules:**
- Record type discriminator in first field determines layout
- Each record padded to 500 bytes with FILLER
- Used for branch migration / data portability scenarios
- CBIMPORT validates records and writes errors to ERROR-OUTPUT

---

## 15. Inter-Program Communication Area

> **Copybook:** `COCOM01Y.cpy` | **Record Length:** Variable  
> **Used By:** All online CICS programs

| Section | Key Fields | Business Description |
|---|---|---|
| Program Control | `CDEMO-FROM-PROGRAM`, `CDEMO-TO-PROGRAM`, `CDEMO-FROM-TRANID` | Navigation state between screens |
| User Context | `CDEMO-USER-ID`, `CDEMO-USER-TYPE` | Authenticated user session data |
| Account Context | `CDEMO-ACCT-ID`, `CDEMO-ACCT-STATUS` | Currently selected account |
| Card Context | `CDEMO-CARD-NUM` | Currently selected card |
| Transaction Context | `CDEMO-TRAN-ID` | Currently selected transaction |
| Menu Options | `CDEMO-MENU-OPT-PGMNAME(n)` | Menu-to-program mapping table |
| PF Key State | `CDEMO-PFK-*` | Function key pressed |

**Business Rules:**
- Passed between programs via CICS XCTL COMMAREA
- Maintains user session state across screen navigations
- Contains the menu option to program name mapping table

---

## Entity Relationship Summary

```
                    +-----------+
                    | CUSTOMER  |
                    | (CVCUS01Y)|
                    +-----+-----+
                          |
                          | CUST-ID
                          v
+----------+      +-----------+      +---------+
| ACCOUNT  |<---->| CARD-XREF |<---->|  CARD   |
| (CVACT01Y)|     | (CVACT03Y)|      |(CVACT02Y)|
+-----+----+      +-----------+      +---------+
      |                                    |
      | ACCT-ID                            | CARD-NUM
      v                                    v
+------------+                      +-------------+
| TRAN-CAT-  |                      | TRANSACTION |
| BALANCE    |                      | (CVTRA05Y)  |
| (CVTRA01Y) |                      +------+------+
+-----+------+                             |
      |                                    | TRAN-TYPE-CD + TRAN-CAT-CD
      | ACCT-GROUP-ID                      v
      v                          +----+-------+----+
+-----------+                    |TRAN-TYPE   |TRAN-CAT  |
| DISCLOSURE|                    |(CVTRA03Y)  |(CVTRA04Y)|
| GROUP     |                    +------------+---------+
| (CVTRA02Y)|
+-----------+

+-----------+
| USER      |
| SECURITY  |
| (CSUSR01Y)|
+-----------+
```

---

## VSAM-to-Table Mapping (Modernization Reference)

| VSAM Dataset | Suggested Table Name | Primary Key | Est. Rows |
|---|---|---|---|
| `ACCTDATA.VSAM.KSDS` | `account` | `acct_id` | ~10K |
| `CARDDATA.VSAM.KSDS` | `card` | `card_num` | ~20K |
| `CUSTDATA.VSAM.KSDS` | `customer` | `cust_id` | ~10K |
| `CARDXREF.VSAM.KSDS` | `card_xref` | `xref_card_num` | ~20K |
| `TRANSACT.VSAM.KSDS` | `transaction` | `tran_id` | ~100K+ |
| `DALYTRAN.PS` | `daily_transaction` (staging) | `dalytran_id` | ~1K/day |
| `TCATBALF.VSAM.KSDS` | `tran_category_balance` | `(acct_id, type_cd, cat_cd)` | ~50K |
| `DISCGRP.VSAM.KSDS` | `disclosure_group` | `(group_id, type_cd, cat_cd)` | ~500 |
| `TRANTYPE.VSAM.KSDS` | `transaction_type` | `tran_type` | ~20 |
| `TRANCATG.VSAM.KSDS` | `transaction_category` | `(type_cd, cat_cd)` | ~200 |
| `USRSEC.VSAM.KSDS` | `app_user` | `usr_id` | ~100 |
