# CardDemo Domain Decomposition

## Executive Summary

This document identifies bounded contexts within the CardDemo mainframe application using Domain-Driven Design (DDD) principles. For each bounded context, we analyze extraction seams -- the natural boundaries where the legacy monolith can be split into independent services with minimal cross-cutting disruption. The analysis is grounded in the actual COBOL source code, copybook data structures, VSAM file dependencies, and CICS program call chains.

---

## 1. Bounded Context Map

### 1.1 Identified Bounded Contexts

```
+------------------------------------------------------------------+
|                     CardDemo Application                          |
|                                                                   |
|  +------------------+    +-------------------+                    |
|  | BC-1: Identity   |    | BC-2: Account     |                    |
|  | & Access Mgmt    |--->| Management        |                    |
|  |                  |    |                   |                    |
|  | COSGN00C         |    | COACTVWC          |                    |
|  | COUSR00C-03C     |    | COACTUPC          |                    |
|  | USRSEC (VSAM)    |    | ACCTDAT (VSAM)    |                    |
|  +------------------+    | CUSTDAT (VSAM)    |                    |
|                          +------|------------+                    |
|                                 |                                 |
|                          +------v------------+                    |
|                          | BC-3: Card        |                    |
|                          | Management        |                    |
|                          |                   |                    |
|                          | COCRDLIC          |                    |
|                          | COCRDSLC          |                    |
|                          | COCRDUPC          |                    |
|                          | CARDDAT (VSAM)    |                    |
|                          | CARDXREF (VSAM)   |                    |
|                          +------|------------+                    |
|                                 |                                 |
|  +------------------+    +------v------------+                    |
|  | BC-5: Bill       |    | BC-4: Transaction |                    |
|  | Payment          |--->| Processing        |                    |
|  |                  |    |                   |                    |
|  | COBIL00C         |    | COTRN00C-02C      |                    |
|  |                  |    | CBTRN02C (batch)  |                    |
|  +------------------+    | TRANSACT (VSAM)   |                    |
|                          | DALYTRAN (VSAM)   |                    |
|                          | TCATBALF (VSAM)   |                    |
|                          +------|------------+                    |
|                                 |                                 |
|  +------------------+    +------v------------+                    |
|  | BC-7: Reporting  |    | BC-6: Financial   |                    |
|  | & Statements     |<---| Calculations      |                    |
|  |                  |    |                   |                    |
|  | CORPT00C         |    | CBACT04C (batch)  |                    |
|  | CBTRN03C         |    | DISCGRP (VSAM)    |                    |
|  | CBSTM03A/B       |    |                   |                    |
|  +------------------+    +-------------------+                    |
|                                                                   |
|  +------------------+    +-------------------+                    |
|  | BC-8: Auth'zation|    | BC-9: Reference   |                    |
|  | (Optional)       |    | Data              |                    |
|  |                  |    |                   |                    |
|  | COPAUA0C         |    | COTRTLIC          |                    |
|  | COPAUS0C-2C      |    | COTRTUPC          |                    |
|  | CBPAUP0C         |    | COBTUPDT          |                    |
|  | IMS/DB2/MQ       |    | DB2 tables        |                    |
|  +------------------+    +-------------------+                    |
+------------------------------------------------------------------+
```

---

## 2. Bounded Context Detail

### 2.1 BC-1: Identity & Access Management

**Domain Purpose:** Authenticate users, manage user credentials, and enforce role-based access control (Admin vs. Regular User).

#### Programs

| Program | Function | Lines | CICS Trans ID |
|---------|----------|-------|---------------|
| COSGN00C | Sign-on screen, credential validation, role routing | 261 | CC00 |
| COUSR00C | User list (admin) | ~350 | CU00 |
| COUSR01C | User add (admin) | ~400 | CU01 |
| COUSR02C | User update (admin) | ~400 | CU02 |
| COUSR03C | User delete (admin) | ~300 | CU03 |

#### Data Ownership

| Dataset | Copybook | Ownership | Access Pattern |
|---------|----------|-----------|----------------|
| USRSEC | CSUSR01Y | **Owns** | READ (signon), CRUD (admin user mgmt) |

#### Ubiquitous Language

- **User ID**: 8-character identifier (e.g., ADMIN001, USER0001)
- **User Type**: Single character -- `A` (Admin) or `U` (Regular User)
- **Security Record**: 80-byte VSAM record containing credentials and role

#### Extraction Seam Analysis

| Seam Characteristic | Assessment |
|--------------------|-----------|
| **Data isolation** | **Excellent** -- USRSEC is only accessed by programs in this context. No other bounded context reads or writes user security data. |
| **Inbound dependencies** | None -- this is the entry point of the application |
| **Outbound dependencies** | Passes `CDEMO-USER-ID` and `CDEMO-USER-TYPE` via COMMAREA to downstream contexts (COMEN01C, COADM01C). This is the only coupling point. |
| **Extraction difficulty** | **Low** -- Clean data boundary. The COMMAREA fields (`CDEMO-USER-ID`, `CDEMO-USER-TYPE`) become JWT claims or session attributes in the modernized system. |
| **Recommended seam** | Replace USRSEC VSAM reads with a REST call to an Identity Service. Return a JWT token containing user ID, role, and session metadata. All downstream programs validate the token instead of reading COMMAREA user fields. |

#### Anti-Corruption Layer (ACL)

During transition, the ACL translates between:
- **Legacy side**: COMMAREA fields `CDEMO-USER-ID` (PIC X(08)) and `CDEMO-USER-TYPE` (PIC X(01))
- **Modern side**: JWT token with claims `{ "sub": "USER0001", "role": "USER", "iat": ... }`

---

### 2.2 BC-2: Account Management

**Domain Purpose:** Maintain account master data including balances, credit limits, status, and associated customer information.

#### Programs

| Program | Function | Lines | CICS Trans ID |
|---------|----------|-------|---------------|
| COACTVWC | Account view -- displays account, customer, and card details | 942 | CAVW |
| COACTUPC | Account update -- extensive field validation and update | 4,237 | CAUP |

#### Data Ownership

| Dataset | Copybook | Ownership | Access Pattern |
|---------|----------|-----------|----------------|
| ACCTDAT | CVACT01Y | **Owns** | READ, REWRITE |
| CUSTDAT | CVCUS01Y | **Owns** | READ, REWRITE (via account update) |
| CARDXREF | CVACT03Y | Reads (shared) | READ via alternate index CXACAIX |
| CARDDAT | CVACT02Y | Reads (shared) | READ via alternate index CARDAIX |

#### Ubiquitous Language

- **Account**: 300-byte record keyed by 11-digit Account ID
- **Customer**: 500-byte record keyed by 9-digit Customer ID
- **Account Status**: Active (`Y`) or Inactive (`N`)
- **Credit Limit / Cash Credit Limit**: PIC S9(10)V99 (signed, 2 decimal places)
- **FICO Score**: 3-digit numeric credit score
- **Account Group ID**: 10-character grouping for interest rate lookup

#### Extraction Seam Analysis

| Seam Characteristic | Assessment |
|--------------------|-----------|
| **Data isolation** | **Moderate** -- Owns ACCTDAT and CUSTDAT but these are also read by batch programs (CBTRN02C, CBACT04C, CBSTM03A). Customer data could be a separate context but is tightly coupled to account view/update screens. |
| **Inbound dependencies** | Card Management (COCRDLIC navigates back to account view). Transaction Management reads account for validation. Bill Payment reads/updates account balance. Batch programs read/update account records. |
| **Outbound dependencies** | Reads CARDDAT and CARDXREF for display (account view shows associated cards). |
| **Extraction difficulty** | **Medium-High** -- Multiple external readers of ACCTDAT. The customer sub-domain is embedded in account update (COACTUPC validates SSN, phone, DOB for the customer). |
| **Recommended seam** | Extract as an **Account Service** exposing: `GET /accounts/{id}`, `PUT /accounts/{id}`, `GET /accounts/{id}/customer`. Batch programs transition to calling the Account Service API instead of direct VSAM reads. Consider splitting Customer into a sub-service later, but keep it unified initially to reduce extraction risk. |

#### Shared Data Boundary

The cross-reference file (CARDXREF / CVACT03Y) is the key shared data structure:
```
XREF-CARD-NUM    PIC X(16)   -- links to CARDDAT
XREF-CUST-ID     PIC 9(09)   -- links to CUSTDAT
XREF-ACCT-ID     PIC 9(11)   -- links to ACCTDAT
```
This cross-reference table becomes a **shared lookup** in the relational schema, owned by the Card context but queryable by Account and Transaction contexts.

---

### 2.3 BC-3: Card Management

**Domain Purpose:** Manage credit card lifecycle -- listing, viewing, and updating card records associated with accounts.

#### Programs

| Program | Function | Lines | CICS Trans ID |
|---------|----------|-------|---------------|
| COCRDLIC | Card list with pagination, filtering by account, select for view/update | 1,460 | CCLI |
| COCRDSLC | Card detail view | ~600 | CCDL |
| COCRDUPC | Card update (status, embossed name, expiry) | ~800 | CCUP |

#### Data Ownership

| Dataset | Copybook | Ownership | Access Pattern |
|---------|----------|-----------|----------------|
| CARDDAT | CVACT02Y | **Owns** | BROWSE, READ, REWRITE |
| CARDAIX | (alternate index on CARDDAT) | **Owns** | BROWSE by account ID |
| CARDXREF | CVACT03Y | **Shared** (co-owns with Account) | READ by card number and account |
| CXACAIX | (alternate index on CARDXREF) | **Shared** | READ by account ID |

#### Ubiquitous Language

- **Card Number**: 16-character primary key
- **Card Status**: Active (`Y`) or Inactive (`N`)
- **CVV Code**: 3-digit Card Verification Value
- **Embossed Name**: 50-character name printed on physical card
- **Card Expiration Date**: 10-character date string

#### Extraction Seam Analysis

| Seam Characteristic | Assessment |
|--------------------|-----------|
| **Data isolation** | **Good** -- CARDDAT is primarily managed here. Other contexts read cards through CARDXREF (cross-reference), not CARDDAT directly. |
| **Inbound dependencies** | Account View (COACTVWC) lists cards associated with an account. Bill Payment (COBIL00C) reads CARDXREF to find the card for payment transactions. |
| **Outbound dependencies** | Navigation back to Account View (COACTVWC) and Main Menu (COMEN01C). |
| **Extraction difficulty** | **Medium** -- Clean primary data ownership. The CARDXREF shared table requires a clear ownership decision. |
| **Recommended seam** | Extract as **Card Service** exposing: `GET /cards?accountId={id}`, `GET /cards/{cardNumber}`, `PUT /cards/{cardNumber}`. Expose a `/cards/xref?accountId={id}` endpoint for cross-reference lookups used by Bill Payment and Transaction contexts. The alternate index (CARDAIX) becomes a database secondary index on `account_id`. |

#### Pagination Seam

COCRDLIC implements cursor-based pagination using CICS STARTBR/READNEXT/READPREV with 7 rows per page. The extraction seam converts this to:
- **Legacy**: VSAM BROWSE with RIDFLD positioning
- **Modern**: `GET /cards?accountId={id}&page={n}&size=7` using Spring Data's `Pageable`

The program-local commarea (`WS-THIS-PROGCOMMAREA`) stores pagination state (last card key, first card key, screen number, next-page indicator). In the modern system, this becomes stateless cursor-based pagination using the card number as the cursor.

---

### 2.4 BC-4: Transaction Processing

**Domain Purpose:** Record, list, view, and process credit card transactions. This is the core business domain encompassing both online transaction entry and batch posting.

#### Programs

| Program | Function | Lines | Type |
|---------|----------|-------|------|
| COTRN00C | Transaction list with pagination | 700 | Online (CICS) |
| COTRN01C | Transaction detail view | ~400 | Online (CICS) |
| COTRN02C | Transaction add (manual entry) | ~500 | Online (CICS) |
| CBTRN02C | Daily transaction posting (batch) | 732 | Batch |
| CBTRN01C | Transaction file processing | ~300 | Batch |

#### Data Ownership

| Dataset | Copybook | Ownership | Access Pattern |
|---------|----------|-----------|----------------|
| TRANSACT | CVTRA05Y | **Owns** | BROWSE, READ, WRITE |
| DALYTRAN | CVTRA06Y | **Owns** | READ (batch input) |
| DALYREJS | (reject output) | **Owns** | WRITE (batch rejects) |
| TCATBALF | CVTRA01Y | **Owns** | READ, WRITE, REWRITE |
| XREFFILE | CVACT03Y | Reads (shared) | READ for card validation |
| ACCTDAT | CVACT01Y | Reads/Writes (shared) | READ for validation, REWRITE for balance updates |

#### Ubiquitous Language

- **Transaction ID**: 16-character unique identifier (sequential, auto-generated)
- **Transaction Type Code**: 2-character code (e.g., `02` for bill payment)
- **Transaction Category Code**: 4-digit numeric category
- **Transaction Source**: 10-character origin (e.g., `POS TERM`)
- **Transaction Amount**: PIC S9(09)V99 (signed, 2 decimal places)
- **Daily Transaction**: Incoming transaction pending validation and posting
- **Reject Record**: Failed validation with reason code and description
- **Transaction Category Balance**: Running balance per account + type + category

#### Extraction Seam Analysis

| Seam Characteristic | Assessment |
|--------------------|-----------|
| **Data isolation** | **Moderate** -- Owns TRANSACT and related files, but updates ACCTDAT (balance) during batch posting. |
| **Inbound dependencies** | Bill Payment creates transactions. Reporting reads transactions. Statement Generation reads transactions. |
| **Outbound dependencies** | Batch posting (CBTRN02C) validates against XREFFILE and updates ACCTDAT. This is the tightest cross-context coupling in the system. |
| **Extraction difficulty** | **High** -- The batch posting process (CBTRN02C) spans Transaction, Account, and Card contexts. The online transaction add (COTRN02C) has simpler boundaries. |
| **Recommended seam** | Split into two sub-services: (1) **Transaction Query Service** (list, view -- read-only, easy extraction) and (2) **Transaction Processing Service** (add, batch post -- requires saga/orchestration with Account Service for balance updates). The batch posting process becomes an orchestrated saga: validate card (Card Service) -> validate account (Account Service) -> post transaction (Transaction Service) -> update balance (Account Service). |

#### Critical Coupling: Batch Posting

The `CBTRN02C` batch program is the most tightly coupled process in the system:

```
DALYTRAN (read) --> Validate against XREFFILE (Card context)
                --> Validate against ACCTDAT (Account context)
                --> Write to TRANSACT (Transaction context)
                --> Update ACCTDAT balance (Account context)
                --> Update TCATBALF (Transaction context)
                --> Write rejects to DALYREJS (Transaction context)
```

This process must be decomposed into a **saga pattern** or kept as a single orchestrating batch service that calls Account and Card APIs.

---

### 2.5 BC-5: Bill Payment

**Domain Purpose:** Process full-balance bill payments by creating payment transactions and updating account balances.

#### Programs

| Program | Function | Lines | CICS Trans ID |
|---------|----------|-------|---------------|
| COBIL00C | Bill payment -- pay full balance, create transaction | 573 | CB00 |

#### Data Ownership

| Dataset | Copybook | Ownership | Access Pattern |
|---------|----------|-----------|----------------|
| (none exclusively) | -- | **No exclusive data** | -- |
| TRANSACT | CVTRA05Y | Writes (shared) | STARTBR/READPREV (get last ID), WRITE (new payment) |
| ACCTDAT | CVACT01Y | Reads/Writes (shared) | READ UPDATE, REWRITE (deduct balance) |
| CXACAIX | CVACT03Y | Reads (shared) | READ (find card for account) |

#### Ubiquitous Language

- **Bill Payment**: Full-balance payment that zeros account balance
- **Payment Confirmation**: Y/N flag required before processing
- **Payment Transaction**: Type code `02`, category `2`, source `POS TERM`, description `BILL PAYMENT - ONLINE`

#### Extraction Seam Analysis

| Seam Characteristic | Assessment |
|--------------------|-----------|
| **Data isolation** | **Poor** -- Owns no data; orchestrates writes across Transaction and Account datasets |
| **Inbound dependencies** | Accessed from Main Menu via COMEN01C |
| **Outbound dependencies** | Reads ACCTDAT and CXACAIX, writes TRANSACT, updates ACCTDAT |
| **Extraction difficulty** | **Medium** -- No data ownership simplifies extraction; it becomes a pure orchestration service. But it depends on Transaction and Account services being available. |
| **Recommended seam** | Extract as a **Payment Service** that orchestrates calls to Account Service (get balance, update balance) and Transaction Service (create payment transaction). Uses a distributed transaction pattern (saga with compensating actions). Natural fit for an Application Service in DDD terms -- it has no domain state, only orchestration logic. |

#### Transaction ID Generation Seam

COBIL00C generates transaction IDs by:
1. STARTBR TRANSACT with HIGH-VALUES
2. READPREV to get the last transaction ID
3. ADD 1 to generate the next ID

This becomes a **sequence generator** in the modern system (database sequence or distributed ID generator like Snowflake).

---

### 2.6 BC-6: Financial Calculations

**Domain Purpose:** Calculate interest charges and fees based on transaction category balances and discount group rates, then update account balances.

#### Programs

| Program | Function | Lines | Type |
|---------|----------|-------|------|
| CBACT04C | Interest calculation -- compute monthly interest per account per category | 653 | Batch |

#### Data Ownership

| Dataset | Copybook | Ownership | Access Pattern |
|---------|----------|-----------|----------------|
| DISCGRP | CVTRA02Y | **Owns** | READ (interest rate lookup) |
| TCATBALF | CVTRA01Y | Reads (shared with BC-4) | Sequential READ |
| XREFFILE | CVACT03Y | Reads (shared) | READ by alternate key (account ID) |
| ACCTDAT | CVACT01Y | Reads/Writes (shared) | READ, REWRITE (update balances) |
| TRANSACT | CVTRA05Y | Writes (shared) | Sequential WRITE (interest transactions) |

#### Ubiquitous Language

- **Discount Group**: Rate table keyed by Account Group ID + Transaction Type + Category
- **Interest Rate**: Rate from DISCGRP applied to category balances
- **Monthly Interest**: Calculated per transaction category per account
- **Total Interest**: Aggregate interest across all categories for an account

#### Extraction Seam Analysis

| Seam Characteristic | Assessment |
|--------------------|-----------|
| **Data isolation** | **Low** -- Only exclusively owns DISCGRP (rate tables). Heavy read/write coupling with Account and Transaction contexts. |
| **Inbound dependencies** | Triggered by JCL job (INTCALC) in the batch cycle, after transaction posting |
| **Outbound dependencies** | Reads TCATBALF (Transaction context), reads/updates ACCTDAT (Account context), writes TRANSACT (Transaction context) |
| **Extraction difficulty** | **High** -- Financial calculation precision is critical. Cross-context writes require saga coordination. COMP-3 arithmetic must map exactly to Java BigDecimal. |
| **Recommended seam** | Extract as a **Financial Calculation Service** (Spring Batch job). Reads TCATBALF and DISCGRP from its own database views. Calls Account Service API to update balances. Calls Transaction Service API to write interest transactions. Run parallel with legacy and reconcile before cutover. |

---

### 2.7 BC-7: Reporting & Statements

**Domain Purpose:** Generate transaction reports (monthly, yearly, custom) and produce account statements in text and HTML formats.

#### Programs

| Program | Function | Lines | Type |
|---------|----------|-------|------|
| CORPT00C | Report request UI -- submit batch report jobs via TDQ | 650 | Online (CICS) |
| CBTRN03C | Batch transaction report generation | ~500 | Batch |
| CBSTM03A | Statement generation (text + HTML) -- uses ALTER/GO TO, PSA/TCB/TIOT | 924 | Batch |
| CBSTM03B | Statement sub-routine -- file I/O for CBSTM03A | ~400 | Batch |

#### Data Ownership

| Dataset | Copybook | Ownership | Access Pattern |
|---------|----------|-----------|----------------|
| (output files) | -- | **Owns** (output) | WRITE (statement/report files) |
| TRANSACT | CVTRA05Y | Reads (shared) | READ / BROWSE |
| ACCTDAT | CVACT01Y | Reads (shared) | READ |
| CUSTDAT | CVCUS01Y | Reads (shared) | READ |
| CARDXREF | CVACT03Y | Reads (shared) | READ |

#### Extraction Seam Analysis

| Seam Characteristic | Assessment |
|--------------------|-----------|
| **Data isolation** | **Good for extraction** -- Pure read-only consumer of other contexts' data. Owns only output files. |
| **Inbound dependencies** | CORPT00C triggered from Main Menu. Batch jobs triggered by JCL schedule. |
| **Outbound dependencies** | Reads from Transaction, Account, Customer, and Card contexts (all read-only). |
| **Extraction difficulty** | **Low-Medium** -- Read-only access makes this the cleanest extraction candidate. CBSTM03A's ALTER/GO TO patterns make it hard to convert but easy to replace. |
| **Recommended seam** | Extract as a **Reporting Service**. Replace JCL-based job submission (TDQ) with REST API-triggered Spring Batch jobs. Read from database views or call other service APIs. Statement generation becomes a template-based renderer. CBSTM03A is rewritten from scratch using modern templating. |

---

### 2.8 BC-8: Authorization (Optional Module)

**Domain Purpose:** Process pending credit card authorizations via MQ-triggered workflows, with fraud marking capabilities via DB2.

#### Programs

| Program | Function | Lines | Type |
|---------|----------|-------|------|
| COPAUA0C | MQ trigger handler -- process authorization requests | ~400 | MQ Trigger |
| COPAUS0C | Authorization summary view | ~500 | Online (CICS) |
| COPAUS1C | Authorization detail view | ~400 | Online (CICS) |
| COPAUS2C | Fraud marking to DB2 | ~300 | Online (CICS) |
| CBPAUP0C | Batch purge of old authorization records | ~300 | Batch |

#### Data Ownership

| Dataset | Copybook | Ownership | Access Pattern |
|---------|----------|-----------|----------------|
| IMS DB segments | (IMS-specific) | **Owns** | IMS DL/I calls |
| DB2 fraud table | (SQL DDL) | **Owns** | Embedded SQL INSERT/UPDATE |
| MQ queues (CDRA, CDRD) | (MQ definitions) | **Owns** | MQ GET/PUT |

#### Extraction Seam Analysis

| Seam Characteristic | Assessment |
|--------------------|-----------|
| **Data isolation** | **Excellent** -- Uses completely separate data stores (IMS DB, DB2, MQ) from the core VSAM-based system |
| **Inbound dependencies** | MQ queue triggers; Menu navigation (COMEN01C option 11 for COPAUS0C) |
| **Outbound dependencies** | None to core VSAM datasets |
| **Extraction difficulty** | **Medium** -- Excellent data isolation but complex middleware (IMS + DB2 + MQ). IMS DB segments need schema mapping to RDBMS. |
| **Recommended seam** | Extract as an **Authorization Service**. MQ triggers become Kafka/RabbitMQ consumers. IMS DB segments become JPA entities. DB2 tables migrate directly to PostgreSQL. This is the most naturally isolated optional module. |

---

### 2.9 BC-9: Reference Data Management

**Domain Purpose:** Manage transaction type reference data (CRUD) stored in DB2.

#### Programs

| Program | Function | Lines | Type |
|---------|----------|-------|------|
| COTRTLIC | Transaction type list/delete (DB2 cursors) | ~600 | Online (CICS) |
| COTRTUPC | Transaction type add/edit (embedded SQL) | ~500 | Online (CICS) |
| COBTUPDT | Batch transaction type update | ~300 | Batch |

#### Data Ownership

| Dataset | Copybook | Ownership | Access Pattern |
|---------|----------|-----------|----------------|
| DB2 transaction type table | (SQL DDL) | **Owns** | Embedded SQL CRUD |

#### Extraction Seam Analysis

| Seam Characteristic | Assessment |
|--------------------|-----------|
| **Data isolation** | **Excellent** -- DB2 table with no VSAM dependencies |
| **Inbound dependencies** | Admin menu (COADM01C options 5-6) |
| **Outbound dependencies** | Transaction type codes referenced by TRANSACT records (PIC X(02) `TRAN-TYPE-CD`) |
| **Extraction difficulty** | **Low** -- Clean DB2 CRUD; embedded SQL maps directly to JPA/JDBC |
| **Recommended seam** | Extract as a **Reference Data Service** or fold into Transaction Service as a sub-module. DB2 tables migrate to PostgreSQL. Cursor-based pagination becomes Spring Data Pageable. |

---

## 3. Cross-Context Dependency Matrix

This matrix shows data access patterns between bounded contexts:

| | USRSEC | ACCTDAT | CUSTDAT | CARDDAT | CARDXREF | TRANSACT | DALYTRAN | TCATBALF | DISCGRP |
|---|---|---|---|---|---|---|---|---|---|
| **BC-1 Identity** | **OWN** | | | | | | | | |
| **BC-2 Account** | | **OWN** | **OWN** | read | read | | | | |
| **BC-3 Card** | | | | **OWN** | **SHARED** | | | | |
| **BC-4 Transaction** | | r/w | | | read | **OWN** | **OWN** | **OWN** | |
| **BC-5 Bill Payment** | | r/w | | | read | write | | | |
| **BC-6 Financial Calc** | | r/w | | | read | write | | read | **OWN** |
| **BC-7 Reporting** | | read | read | | read | read | | | |
| **BC-8 Authorization** | | | | | | | | | |
| **BC-9 Reference Data** | | | | | | | | | |

**Legend:** OWN = owns the data, SHARED = co-owns, r/w = reads and writes, read = read-only, write = write-only

### 3.1 Key Coupling Hotspots

1. **ACCTDAT** (Account Master) -- Accessed by 5 bounded contexts (BC-2, BC-4, BC-5, BC-6, BC-7). This is the most contended dataset and must be extracted carefully with a clear API boundary.

2. **CARDXREF** (Cross-Reference) -- Shared between Card Management (BC-3) and used by Transaction Processing (BC-4), Bill Payment (BC-5), and Financial Calculations (BC-6) for card-to-account lookups. Recommend owning this in Card Service with a read-only API for others.

3. **TRANSACT** (Transaction Master) -- Written by Transaction Processing (BC-4) and Bill Payment (BC-5). Read by Reporting (BC-7). Transaction Processing should own this with Bill Payment calling its API to create records.

---

## 4. Extraction Seam Priority

Ordered by extraction cleanliness (cleanest first):

| Priority | Bounded Context | Seam Quality | Rationale |
|----------|----------------|-------------|-----------|
| 1 | BC-1: Identity & Access | **Excellent** | No shared data. Single outbound dependency (COMMAREA -> JWT). |
| 2 | BC-9: Reference Data | **Excellent** | Isolated DB2 tables. No VSAM coupling. |
| 3 | BC-8: Authorization | **Excellent** | Separate middleware stack (IMS/DB2/MQ). Zero VSAM overlap. |
| 4 | BC-7: Reporting | **Good** | Read-only consumer. No writes to shared datasets. |
| 5 | BC-3: Card Management | **Good** | Clear primary data ownership. CARDXREF sharing is manageable. |
| 6 | BC-2: Account Management | **Moderate** | Owns critical shared data (ACCTDAT). Many external readers/writers. |
| 7 | BC-5: Bill Payment | **Moderate** | No owned data; pure orchestration. Depends on BC-2 and BC-4. |
| 8 | BC-4: Transaction Processing | **Challenging** | Multi-context writes in batch posting. Saga pattern required. |
| 9 | BC-6: Financial Calculations | **Challenging** | Cross-context writes. Precision-critical. Must parallel-run. |

---

## 5. Context Communication Patterns

### 5.1 Synchronous (REST API)

| Caller | Callee | Operation | Pattern |
|--------|--------|-----------|---------|
| Bill Payment | Account Service | Get balance, Update balance | Request/Response |
| Bill Payment | Transaction Service | Create payment transaction | Request/Response |
| Card List | Card Service | Get cards by account | Request/Response |
| Account View | Card Service | Get cards for account | Request/Response |
| Financial Calc | Account Service | Update balance | Request/Response |

### 5.2 Asynchronous (Events)

| Producer | Event | Consumer(s) | Pattern |
|----------|-------|-------------|---------|
| Transaction Service | TransactionPosted | Account Service (balance update) | Event-Driven (Saga) |
| Transaction Service | TransactionRejected | Reject Handler | Event Notification |
| Authorization Service | AuthorizationRequested | Authorization Processor | Event-Driven |
| Financial Calc Service | InterestCalculated | Account Service | Event-Driven (Saga) |

### 5.3 Batch Integration

| Batch Job | Input Context | Output Context | Pattern |
|-----------|--------------|----------------|---------|
| Transaction Posting | Transaction (DALYTRAN) | Transaction (TRANSACT), Account (ACCTDAT) | Choreographed Saga |
| Interest Calculation | Transaction (TCATBALF) | Account (ACCTDAT), Transaction (TRANSACT) | Choreographed Saga |
| Statement Generation | Transaction, Account, Customer, Card | Reporting (output files) | Aggregation Query |

---

## 6. Data Migration Strategy

### 6.1 VSAM-to-RDBMS Mapping

Each VSAM dataset maps to one or more relational tables. The cross-reference file (CARDXREF) becomes a join table:

```sql
-- BC-1: Identity
CREATE TABLE users (
    user_id       VARCHAR(8)  PRIMARY KEY,
    first_name    VARCHAR(20),
    last_name     VARCHAR(20),
    password_hash VARCHAR(128),  -- replaces plaintext PIC X(08)
    user_type     CHAR(1) CHECK (user_type IN ('A', 'U'))
);

-- BC-2: Account Management
CREATE TABLE accounts (
    account_id       NUMERIC(11)    PRIMARY KEY,
    active_status    CHAR(1),
    current_balance  NUMERIC(12,2),
    credit_limit     NUMERIC(12,2),
    cash_credit_limit NUMERIC(12,2),
    open_date        DATE,
    expiration_date  DATE,
    reissue_date     DATE,
    cycle_credit     NUMERIC(12,2),
    cycle_debit      NUMERIC(12,2),
    zip_code         VARCHAR(10),
    group_id         VARCHAR(10)
);

CREATE TABLE customers (
    customer_id      NUMERIC(9)    PRIMARY KEY,
    first_name       VARCHAR(25),
    middle_name      VARCHAR(25),
    last_name        VARCHAR(25),
    address_line_1   VARCHAR(50),
    address_line_2   VARCHAR(50),
    address_line_3   VARCHAR(50),
    state_code       CHAR(2),
    country_code     CHAR(3),
    zip_code         VARCHAR(10),
    phone_1          VARCHAR(15),
    phone_2          VARCHAR(15),
    ssn              NUMERIC(9),
    govt_issued_id   VARCHAR(20),
    date_of_birth    DATE,
    eft_account_id   VARCHAR(10),
    primary_cardholder CHAR(1),
    fico_score       NUMERIC(3)
);

-- BC-3: Card Management
CREATE TABLE cards (
    card_number      VARCHAR(16)   PRIMARY KEY,
    account_id       NUMERIC(11)   REFERENCES accounts(account_id),
    cvv_code         NUMERIC(3),
    embossed_name    VARCHAR(50),
    expiration_date  DATE,
    active_status    CHAR(1)
);

CREATE TABLE card_xref (
    card_number  VARCHAR(16) PRIMARY KEY REFERENCES cards(card_number),
    customer_id  NUMERIC(9)  REFERENCES customers(customer_id),
    account_id   NUMERIC(11) REFERENCES accounts(account_id)
);

-- BC-4: Transaction Processing
CREATE TABLE transactions (
    transaction_id   VARCHAR(16)   PRIMARY KEY,
    type_code        CHAR(2),
    category_code    NUMERIC(4),
    source           VARCHAR(10),
    description      VARCHAR(100),
    amount           NUMERIC(11,2),
    merchant_id      NUMERIC(9),
    merchant_name    VARCHAR(50),
    merchant_city    VARCHAR(50),
    merchant_zip     VARCHAR(10),
    card_number      VARCHAR(16)   REFERENCES cards(card_number),
    originated_ts    TIMESTAMP,
    processed_ts     TIMESTAMP
);

CREATE TABLE transaction_category_balances (
    account_id   NUMERIC(11),
    type_code    CHAR(2),
    category_code NUMERIC(4),
    balance      NUMERIC(12,2),
    PRIMARY KEY (account_id, type_code, category_code)
);

-- BC-6: Financial Calculations
CREATE TABLE discount_groups (
    group_id      VARCHAR(10),
    type_code     CHAR(2),
    category_code NUMERIC(4),
    interest_rate NUMERIC(7,4),
    PRIMARY KEY (group_id, type_code, category_code)
);
```

### 6.2 Migration Sequence

1. **Create RDBMS schema** with all tables
2. **Load reference data** (discount_groups, transaction types) first
3. **Load master data** (users, customers, accounts, cards, card_xref)
4. **Load transactional data** (transactions, category balances)
5. **Set up Change Data Capture (CDC)** for dual-write period
6. **Validate record counts and checksums** against VSAM sources
