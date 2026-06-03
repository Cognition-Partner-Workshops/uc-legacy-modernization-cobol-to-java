# CardDemo — Data Dictionary

> Business-entity reference derived directly from COBOL copybook `PIC` clauses, DB2 DCLGENs, and
> IMS segment layouts. Field types are translated from COBOL storage notation into business-friendly
> terms for modernization (e.g. Java/SQL target typing).

## How to read this document

| COBOL `PIC` | Meaning | Suggested target type |
|:------------|:--------|:----------------------|
| `9(n)` | Unsigned integer, n digits, zoned decimal | integer / `NUMERIC(n)` |
| `S9(n)` | Signed integer | signed integer |
| `S9(n)V99` | Signed decimal, implied 2 dp | `DECIMAL(n+2,2)` (money) |
| `X(n)` | Fixed-length text, n chars | `CHAR(n)` / `VARCHAR(n)` |
| `COMP` | Binary halfword/fullword | small/int |
| `COMP-3` | Packed decimal | `DECIMAL` (compact) |
| `REDEFINES` | Overlay / union of same storage | discriminated union |
| `OCCURS n` | Fixed array of n elements | list / array |

Money fields use an **implied decimal** (`V`): the value is stored without a decimal point and the
last two digits are cents.

---

## 1. Customer  *(CVCUS01Y → CUSTOMER-RECORD, file CUSTDAT)*

The person who owns one or more accounts. 500-byte VSAM KSDS keyed on Customer ID.

| Field | PIC | Business meaning | Type | Notes |
|:------|:----|:-----------------|:-----|:------|
| CUST-ID | 9(09) | Customer identifier | integer | **Primary key** |
| CUST-FIRST-NAME | X(25) | First name | text | |
| CUST-MIDDLE-NAME | X(25) | Middle name | text | |
| CUST-LAST-NAME | X(25) | Last name | text | |
| CUST-ADDR-LINE-1/2/3 | X(50) ×3 | Mailing address lines | text | |
| CUST-ADDR-STATE-CD | X(02) | State code | text | validated via CSLKPCDY |
| CUST-ADDR-COUNTRY-CD | X(03) | Country code | text | |
| CUST-ADDR-ZIP | X(10) | ZIP / postal code | text | |
| CUST-PHONE-NUM-1/2 | X(15) ×2 | Phone numbers | text | area code validated |
| CUST-SSN | 9(09) | Social Security Number | integer | **PII / sensitive** |
| CUST-GOVT-ISSUED-ID | X(20) | Government ID | text | **PII** |
| CUST-DOB-YYYY-MM-DD | X(10) | Date of birth | date (text) | |
| CUST-EFT-ACCOUNT-ID | X(10) | EFT/bank account for payments | text | |
| CUST-PRI-CARD-HOLDER-IND | X(01) | Primary cardholder flag | boolean (Y/N) | |
| CUST-FICO-CREDIT-SCORE | 9(03) | FICO credit score | integer | 300–850 |
| FILLER | X(168) | Reserved/expansion | — | |

> `CUSTREC.cpy` defines an alternate copy of the same layout (field `CUST-DOB-YYYYMMDD` named
> slightly differently) — a duplication worth consolidating during modernization.

---

## 2. Account  *(CVACT01Y → ACCOUNT-RECORD, file ACCTDAT)*

A credit account belonging to a customer. 300-byte VSAM KSDS keyed on Account ID. Holds balances
and limits — the financial core of the system.

| Field | PIC | Business meaning | Type | Notes |
|:------|:----|:-----------------|:-----|:------|
| ACCT-ID | 9(11) | Account identifier | integer | **Primary key** |
| ACCT-ACTIVE-STATUS | X(01) | Active flag | boolean (Y/N) | |
| ACCT-CURR-BAL | S9(10)V99 | Current balance | money | signed |
| ACCT-CREDIT-LIMIT | S9(10)V99 | Credit limit | money | |
| ACCT-CASH-CREDIT-LIMIT | S9(10)V99 | Cash advance limit | money | |
| ACCT-OPEN-DATE | X(10) | Account open date | date (text) | |
| ACCT-EXPIRAION-DATE | X(10) | Expiration date | date (text) | *(sic: typo in source)* |
| ACCT-REISSUE-DATE | X(10) | Reissue date | date (text) | |
| ACCT-CURR-CYC-CREDIT | S9(10)V99 | Current-cycle credits | money | payments/credits this cycle |
| ACCT-CURR-CYC-DEBIT | S9(10)V99 | Current-cycle debits | money | charges this cycle |
| ACCT-ADDR-ZIP | X(10) | Account ZIP | text | |
| ACCT-GROUP-ID | X(10) | Disclosure group ID | text | **FK → Disclosure Group** (interest rates) |
| FILLER | X(178) | Reserved | — | |

---

## 3. Card  *(CVACT02Y → CARD-RECORD, file CARDDAT)*

A physical/virtual card tied to an account. 150-byte VSAM KSDS keyed on 16-digit card number, with
alternate index `CARDAIX` on account ID.

| Field | PIC | Business meaning | Type | Notes |
|:------|:----|:-----------------|:-----|:------|
| CARD-NUM | X(16) | Card number (PAN) | text | **Primary key**, **sensitive** |
| CARD-ACCT-ID | 9(11) | Owning account | integer | **FK → Account**; AIX key |
| CARD-CVV-CD | 9(03) | Card verification value | integer | **sensitive** |
| CARD-EMBOSSED-NAME | X(50) | Name embossed on card | text | |
| CARD-EXPIRAION-DATE | X(10) | Expiry date | date (text) | *(sic)* |
| CARD-ACTIVE-STATUS | X(01) | Active flag | boolean (Y/N) | |
| FILLER | X(59) | Reserved | — | |

---

## 4. Card Cross-Reference  *(CVACT03Y → CARD-XREF-RECORD, file CCXREF / CXACAIX)*

Join entity linking card ↔ customer ↔ account. Enables lookups from any of the three keys.

| Field | PIC | Business meaning | Type | Notes |
|:------|:----|:-----------------|:-----|:------|
| XREF-CARD-NUM | X(16) | Card number | text | **Primary key** |
| XREF-CUST-ID | 9(09) | Customer | integer | **FK → Customer** |
| XREF-ACCT-ID | 9(11) | Account | integer | **FK → Account**; AIX key (CXACAIX) |
| FILLER | X(14) | Reserved | — | |

---

## 5. Transaction (posted)  *(CVTRA05Y → TRAN-RECORD, file TRANSACT)*

A financial transaction that has been posted to an account. 350-byte VSAM KSDS keyed on Transaction ID.

| Field | PIC | Business meaning | Type | Notes |
|:------|:----|:-----------------|:-----|:------|
| TRAN-ID | X(16) | Transaction identifier | text | **Primary key** |
| TRAN-TYPE-CD | X(02) | Transaction type | text | **FK → Transaction Type** |
| TRAN-CAT-CD | 9(04) | Transaction category | integer | **FK → Transaction Category** |
| TRAN-SOURCE | X(10) | Origin channel | text | e.g. POS, online |
| TRAN-DESC | X(100) | Description | text | |
| TRAN-AMT | S9(09)V99 | Amount | money | signed |
| TRAN-MERCHANT-ID | 9(09) | Merchant identifier | integer | |
| TRAN-MERCHANT-NAME | X(50) | Merchant name | text | |
| TRAN-MERCHANT-CITY | X(50) | Merchant city | text | |
| TRAN-MERCHANT-ZIP | X(10) | Merchant ZIP | text | |
| TRAN-CARD-NUM | X(16) | Card used | text | **FK → Card** |
| TRAN-ORIG-TS | X(26) | Origination timestamp | timestamp (text) | |
| TRAN-PROC-TS | X(26) | Processing timestamp | timestamp (text) | |
| FILLER | X(20) | Reserved | — | |

---

## 6. Daily Transaction (unposted)  *(CVTRA06Y → DALYTRAN-RECORD, file DALYTRAN)*

Incoming daily transaction feed before posting. Field-for-field identical to TRAN-RECORD with a
`DALYTRAN-` prefix; consumed by posting (CBTRN02C) and validation (CBTRN01C).

| Field | PIC | Business meaning | Type |
|:------|:----|:-----------------|:-----|
| DALYTRAN-ID | X(16) | Transaction identifier | text |
| DALYTRAN-TYPE-CD | X(02) | Transaction type | text |
| DALYTRAN-CAT-CD | 9(04) | Transaction category | integer |
| DALYTRAN-SOURCE | X(10) | Origin channel | text |
| DALYTRAN-DESC | X(100) | Description | text |
| DALYTRAN-AMT | S9(09)V99 | Amount | money |
| DALYTRAN-MERCHANT-ID | 9(09) | Merchant identifier | integer |
| DALYTRAN-MERCHANT-NAME / -CITY / -ZIP | X(50)/X(50)/X(10) | Merchant details | text |
| DALYTRAN-CARD-NUM | X(16) | Card used | text |
| DALYTRAN-ORIG-TS / -PROC-TS | X(26) ×2 | Timestamps | timestamp (text) |
| FILLER | X(20) | Reserved | — |

---

## 7. Transaction-Category Balance  *(CVTRA01Y → TRAN-CAT-BAL-RECORD, file TCATBAL)*

Running balance per account × transaction type × category. Updated by posting and read by interest
calculation.

| Field | PIC | Business meaning | Type | Notes |
|:------|:----|:-----------------|:-----|:------|
| TRANCAT-ACCT-ID | 9(11) | Account | integer | **Composite key** part 1 (FK → Account) |
| TRANCAT-TYPE-CD | X(02) | Transaction type | text | key part 2 |
| TRANCAT-CD | 9(04) | Category | integer | key part 3 |
| TRAN-CAT-BAL | S9(09)V99 | Category balance | money | |
| FILLER | X(22) | Reserved | — | |

---

## 8. Disclosure Group  *(CVTRA02Y → DIS-GROUP-RECORD, file DISCGRP)*

Interest-rate disclosure rules. Joined to accounts via `ACCT-GROUP-ID`; drives interest calc.

| Field | PIC | Business meaning | Type | Notes |
|:------|:----|:-----------------|:-----|:------|
| DIS-ACCT-GROUP-ID | X(10) | Disclosure group | text | **Composite key** part 1 |
| DIS-TRAN-TYPE-CD | X(02) | Transaction type | text | key part 2 |
| DIS-TRAN-CAT-CD | 9(04) | Category | integer | key part 3 |
| DIS-INT-RATE | S9(04)V99 | Interest rate (%) | decimal | applied per type/category |
| FILLER | X(28) | Reserved | — | |

---

## 9. Transaction Type  *(CVTRA03Y → TRAN-TYPE-RECORD, file TRANTYPE)*

Reference data describing transaction types. Also maintained as DB2 table in the Tran-Type module.

| Field | PIC | Business meaning | Type | Notes |
|:------|:----|:-----------------|:-----|:------|
| TRAN-TYPE | X(02) | Type code | text | **Primary key** |
| TRAN-TYPE-DESC | X(50) | Description | text | |
| FILLER | X(08) | Reserved | — | |

---

## 10. Transaction Category  *(CVTRA04Y → TRAN-CAT-RECORD, file TRANCATG)*

Reference data describing transaction categories within a type.

| Field | PIC | Business meaning | Type | Notes |
|:------|:----|:-----------------|:-----|:------|
| TRAN-TYPE-CD | X(02) | Type code | text | **Composite key** part 1 (FK → Tran Type) |
| TRAN-CAT-CD | 9(04) | Category code | integer | key part 2 |
| TRAN-CAT-TYPE-DESC | X(50) | Description | text | |
| FILLER | X(04) | Reserved | — | |

---

## 11. User Security  *(CSUSR01Y → SEC-USER-DATA, file USRSEC)*

Application sign-on credentials and role. 80-byte VSAM KSDS keyed on user ID.

| Field | PIC | Business meaning | Type | Notes |
|:------|:----|:-----------------|:-----|:------|
| SEC-USR-ID | X(08) | User ID | text | **Primary key** |
| SEC-USR-FNAME | X(20) | First name | text | |
| SEC-USR-LNAME | X(20) | Last name | text | |
| SEC-USR-PWD | X(08) | Password | text | **plaintext — security concern** |
| SEC-USR-TYPE | X(01) | Role: `A`=admin, `U`=user | enum | drives menu routing |
| SEC-USR-FILLER | X(23) | Reserved | — | |

---

## 12. Branch-Migration Export  *(CVEXPORT → EXPORT-RECORD, file EXPORT.DATA)*

Variable-content export record used by `CBEXPORT`/`CBIMPORT`. A single fixed envelope whose
`EXPORT-RECORD-DATA` (460 bytes) is **REDEFINED** per record type (customer/account/card/etc.).
Demonstrates `REDEFINES`, `OCCURS`, `COMP`, and `COMP-3`.

| Field | PIC | Business meaning | Type | Notes |
|:------|:----|:-----------------|:-----|:------|
| EXPORT-REC-TYPE | X(1) | Record-type discriminator | enum | selects REDEFINES overlay |
| EXPORT-TIMESTAMP | X(26) | Export timestamp | timestamp | REDEFINED into date/time parts |
| EXPORT-SEQUENCE-NUM | 9(9) COMP | Sequence number | binary int | |
| EXPORT-BRANCH-ID | X(4) | Branch identifier | text | |
| EXPORT-REGION-CODE | X(5) | Region code | text | |
| EXPORT-RECORD-DATA | X(460) | Payload | union | REDEFINED per type |
| ↳ EXPORT-CUSTOMER-DATA | (redefine) | Customer payload | struct | `EXP-CUST-*` fields |
| ↳↳ EXP-CUST-ID | 9(09) COMP | Customer ID | binary int | |
| ↳↳ EXP-CUST-ADDR-LINES | X(50) OCCURS 3 | Address lines | array | |
| ↳↳ EXP-CUST-PHONE-NUMS | X(15) OCCURS 2 | Phone numbers | array | |
| ↳↳ EXP-CUST-FICO-CREDIT-SCORE | 9(03) COMP-3 | FICO score | packed decimal | |

---

## 13. Inter-program Commarea  *(COCOM01Y → CARDDEMO-COMMAREA)*

Not a stored entity — the in-memory context passed between CICS programs on every `XCTL`. Central to
the navigation/state model.

| Field | PIC | Business meaning |
|:------|:----|:-----------------|
| CDEMO-FROM-TRANID / -PROGRAM | X(04)/X(08) | Caller transaction & program |
| CDEMO-TO-TRANID / -PROGRAM | X(04)/X(08) | Target transaction & program |
| CDEMO-USER-ID | X(08) | Signed-on user |
| CDEMO-USER-TYPE | X(01) | Role (A/U) |
| CDEMO-PGM-CONTEXT | 9(01) | Re-entry / first-pass flag |
| CDEMO-CUST-ID / -ACCT-ID / -CARD-NUM | 9(09)/9(11)/9(16) | Currently selected keys |
| CDEMO-LAST-MAP / -MAPSET | X(7) ×2 | Last screen shown |

---

## 14. Optional-Module Entities

### 14.1 Pending Authorization — IMS segments

**Detail segment** *(CIPAUDTY, IMS DB DBPAUTP0)* — one row per authorization attempt:

| Field | PIC | Business meaning | Notes |
|:------|:----|:-----------------|:------|
| PA-AUTH-DATE-9C | S9(05) COMP-3 | Authorization date (key) | packed |
| PA-AUTH-TIME-9C | S9(09) COMP-3 | Authorization time (key) | packed |
| PA-AUTH-ORIG-DATE / -TIME | X(06) ×2 | Original date/time | |
| PA-CARD-NUM | X(16) | Card number | sensitive |
| PA-AUTH-TYPE | X(04) | Authorization type | |

**Summary segment** *(CIPAUSMY)* — per-account rollups (parent segment):

| Field | PIC | Business meaning |
|:------|:----|:-----------------|
| PA-ACCT-ID | S9(11) COMP-3 | Account (key) |
| PA-CUST-ID | 9(09) | Customer |
| PA-AUTH-STATUS | X(01) | Status |
| PA-ACCOUNT-STATUS | X(02) OCCURS 5 | Status history (array) |
| PA-CREDIT-LIMIT / -CASH-LIMIT | S9(09)V99 COMP-3 | Limits (packed money) |
| PA-CREDIT-BALANCE / -CASH-BALANCE | S9(09)V99 COMP-3 | Balances |
| PA-APPROVED/DECLINED-AUTH-CNT | S9(04) COMP | Approved/declined counts |
| PA-APPROVED/DECLINED-AUTH-AMT | S9(09)V99 COMP-3 | Approved/declined amounts |

### 14.2 Authorization MQ messages

**Request** *(CCPAURQY)* — inbound authorization request from acquirer: `PA-RQ-CARD-NUM X(16)`,
`PA-RQ-TRANSACTION-AMT +9(10).99` (display-edited money), `PA-RQ-MERCHANT-*`, `PA-RQ-PROCESSING-CODE
9(06)`, `PA-RQ-POS-ENTRY-MODE 9(02)`, `PA-RQ-TRANSACTION-ID X(15)`.

**Reply** *(CCPAURLY)* — decision returned to acquirer: `PA-RL-CARD-NUM X(16)`,
`PA-RL-AUTH-ID-CODE X(06)`, `PA-RL-AUTH-RESP-CODE X(02)`, `PA-RL-AUTH-RESP-REASON X(04)`,
`PA-RL-APPROVED-AMT +9(10).99`.

**Error** *(CCPAUERY)* — error/exception envelope for failed authorizations.

### 14.3 Transaction Type — DB2 tables (DCLGEN)

`DCLTRTYP` → `CARDDEMO.TRANSACTION_TYPE` (`DCL-TR-TYPE CHAR(2)`, `DCL-TR-DESCRIPTION VARCHAR`).
`DCLTRCAT` → transaction-category table. `AUTHFRDS.dcl` → authorization-fraud table. These mirror the
VSAM reference files (TRANTYPE/TRANCATG) for the DB2-based optional module.

---

## 15. Entity-Relationship Overview

```
Customer (CUSTDAT) ──1:N── Account (ACCTDAT) ──1:N── Card (CARDDAT)
     │                          │                        │
     └──────── Card Cross-Reference (CCXREF) ────────────┘
                                │
        Account ──1:N── Transaction (TRANSACT) ──N:1── Transaction Type (TRANTYPE)
                                │                              │
                                └── N:1 ── Transaction Category (TRANCATG)
        Account.ACCT-GROUP-ID ──N:1── Disclosure Group (DISCGRP)  [interest rates]
        Account × Type × Category ──── TCATBAL  [running category balances]
        DALYTRAN (feed) ──posted by CBTRN02C──▶ TRANSACT
        User (USRSEC) — application sign-on / role
```

**Sensitive data (PII / PCI) requiring special handling in migration:** CARD-NUM, CARD-CVV-CD,
CUST-SSN, CUST-GOVT-ISSUED-ID, and SEC-USR-PWD (currently stored in plaintext).
