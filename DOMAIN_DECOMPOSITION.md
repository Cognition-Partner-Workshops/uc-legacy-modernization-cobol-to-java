# CardDemo Domain Decomposition

## 1. Overview

This document identifies bounded contexts within the CardDemo COBOL/CICS/VSAM application using Domain-Driven Design (DDD) principles. For each bounded context, we analyze extraction seams -- the natural boundaries where the monolithic system can be split into independent services with minimal cross-cutting impact.

---

## 2. Bounded Context Map

```
+------------------------------------------------------------------+
|                        CardDemo Monolith                          |
|                                                                   |
|  +------------------+    +-------------------+                    |
|  |   Identity &     |    |   Account         |                    |
|  |   Access Mgmt    |    |   Management      |                    |
|  |                  |    |                   |                    |
|  |  COSGN00C        |--->|  COACTVWC         |                    |
|  |  COUSR00C-03C    |    |  COACTUPC         |                    |
|  |  [USRSEC]        |    |  CBACT01C-03C     |                    |
|  +------------------+    |  [ACCTDAT]        |                    |
|          |               |  [CUSTDAT]        |                    |
|          | user-type     +--------+----------+                    |
|          v                        |                               |
|  +------------------+             | acct-id                       |
|  |   Navigation &   |             v                               |
|  |   Presentation   |    +-------------------+                    |
|  |                  |    |   Card             |                    |
|  |  COMEN01C        |    |   Management      |                    |
|  |  COADM01C        |    |                   |                    |
|  |  [17 BMS Maps]   |    |  COCRDLIC         |                    |
|  |  [COCOM01Y]      |    |  COCRDSLC         |                    |
|  +------------------+    |  COCRDUPC         |                    |
|                          |  [CARDDAT]        |                    |
|                          |  [CCXREF/CARDAIX] |                    |
|                          +--------+----------+                    |
|                                   |                               |
|                                   | card-num                      |
|                                   v                               |
|  +--------------------------------------------------+            |
|  |          Transaction Processing                    |            |
|  |                                                    |            |
|  |  COTRN00C (list) COTRN01C (view) COTRN02C (add)  |            |
|  |  CBTRN02C (posting) CBTRN01C (file proc)          |            |
|  |  [TRANSACT] [DALYTRAN] [TCATBALF] [DALYREJS]     |            |
|  +-------------------------+-------------------------+            |
|                             |                                     |
|              +--------------+--------------+                      |
|              |                             |                      |
|              v                             v                      |
|  +--------------------+     +------------------------+            |
|  | Financial           |     |  Reporting &            |            |
|  | Operations          |     |  Statements             |            |
|  |                    |     |                        |            |
|  | COBIL00C (payment) |     | CORPT00C (job submit)  |            |
|  | CBACT04C (interest)|     | CBTRN03C (tran report) |            |
|  | [DISCGRP]          |     | CBSTM03A/B (stmts)     |            |
|  +--------------------+     +------------------------+            |
|                                                                   |
|  +--------------------------------------------------+            |
|  |    Optional Extensions (separate bounded contexts)|            |
|  |  [Authorization IMS/DB2/MQ] [Tran Type DB2]      |            |
|  |  [VSAM-MQ Services]                               |            |
|  +--------------------------------------------------+            |
+------------------------------------------------------------------+
```

---

## 3. Bounded Contexts -- Detailed Analysis

### 3.1 Identity & Access Management Context

**Domain Purpose:** Authenticate users, manage user lifecycle, enforce role-based access.

**Aggregate Root:** `User` (SEC-USER-DATA / CSUSR01Y)

**Entities:**
| Entity      | Source Copybook | Key Fields                              |
|-------------|----------------|-----------------------------------------|
| User        | CSUSR01Y       | SEC-USR-ID (PK), SEC-USR-FNAME, SEC-USR-LNAME, SEC-USR-PWD, SEC-USR-TYPE |

**Programs Owned:**
- `COSGN00C` -- Sign-on (authenticates against USRSEC, routes by user type)
- `COUSR00C` -- User list (admin: browse USRSEC file)
- `COUSR01C` -- User add (admin: write new USRSEC record)
- `COUSR02C` -- User update (admin: rewrite USRSEC record)
- `COUSR03C` -- User delete (admin: delete USRSEC record)

**Data Ownership:** USRSEC (exclusive -- no other context reads/writes this file)

**Published Events (outbound):**
- `UserAuthenticated { userId, userType }` -- consumed by Navigation context
- `UserCreated / UserUpdated / UserDeleted` -- audit trail

**Consumed Events (inbound):** None

**Extraction Seam Analysis:**
- **Seam Type:** Clean boundary -- USRSEC is exclusively owned
- **Coupling Point:** COSGN00C sets CDEMO-USER-ID and CDEMO-USER-TYPE in the COMMAREA, which is consumed by every downstream program. This is the **single shared contract**.
- **Extraction Approach:** Replace with an Authentication Service that issues JWT tokens containing `userId` and `userType` claims. All downstream services validate the token instead of reading COMMAREA fields.
- **Seam Difficulty:** LOW -- No shared data stores, no bidirectional dependencies.

---

### 3.2 Account Management Context

**Domain Purpose:** Maintain customer accounts including balances, credit limits, and customer demographics.

**Aggregate Roots:** `Account` (CVACT01Y), `Customer` (CVCUS01Y)

**Entities:**
| Entity      | Source Copybook | Key Fields                              |
|-------------|----------------|-----------------------------------------|
| Account     | CVACT01Y       | ACCT-ID (PK), ACCT-ACTIVE-STATUS, ACCT-CURR-BAL, ACCT-CREDIT-LIMIT, ACCT-CASH-CREDIT-LIMIT, ACCT-OPEN-DATE, ACCT-EXPIRAION-DATE, ACCT-GROUP-ID |
| Customer    | CVCUS01Y       | CUST-ID (PK), CUST-FIRST-NAME, CUST-LAST-NAME, CUST-ADDR-*, CUST-SSN, CUST-DOB, CUST-FICO-CREDIT-SCORE |

**Programs Owned:**
- `COACTVWC` -- Account view (reads ACCTDAT, CUSTDAT, CCXREF)
- `COACTUPC` -- Account update (validates and rewrites ACCTDAT, CUSTDAT)
- `CBACT01C-03C` -- Batch account file processing and maintenance

**Data Ownership:** ACCTDAT (shared-write with Financial Operations), CUSTDAT (exclusive)

**Published Events (outbound):**
- `AccountUpdated { acctId, newBalance, newStatus }` -- consumed by Card, Transaction contexts
- `CustomerUpdated { custId, ... }` -- consumed by Reporting context

**Consumed Events (inbound):**
- `TransactionPosted { acctId, amount }` -- from Transaction Processing (balance updates)
- `InterestApplied { acctId, amount }` -- from Financial Operations
- `BillPaymentProcessed { acctId, amount }` -- from Financial Operations

**Extraction Seam Analysis:**
- **Seam Type:** Shared data boundary -- ACCTDAT is written by COBIL00C (bill payment) and CBACT04C (interest calculation)
- **Coupling Points:**
  1. COACTVWC reads CCXREF to find cards for an account (cross-context join)
  2. COACTUPC validates customer data inline (Customer is a sub-aggregate)
  3. ACCT-CURR-BAL is updated by Transaction Processing and Financial Operations
- **Extraction Approach:** Account Management becomes the system of record for account and customer data. Other contexts update balances through a published API (e.g., `POST /accounts/{id}/balance-adjustment`) rather than direct file writes. Cross-reference lookups become API calls to Card Management.
- **Seam Difficulty:** MEDIUM -- The shared write to ACCTDAT requires an anti-corruption layer during transition.

---

### 3.3 Card Management Context

**Domain Purpose:** Manage credit card lifecycle -- issuance, status, and card-account-customer cross-references.

**Aggregate Root:** `Card` (CVACT02Y)

**Entities:**
| Entity         | Source Copybook | Key Fields                              |
|----------------|----------------|-----------------------------------------|
| Card           | CVACT02Y       | CARD-NUM (PK), CARD-ACCT-ID, CARD-CVV-CD, CARD-EMBOSSED-NAME, CARD-EXPIRAION-DATE, CARD-ACTIVE-STATUS |
| CardXref       | CVACT03Y       | XREF-CARD-NUM (PK), XREF-CUST-ID, XREF-ACCT-ID |

**Programs Owned:**
- `COCRDLIC` -- Card list with pagination (STARTBR/READNEXT/ENDBR on CARDDAT)
- `COCRDSLC` -- Card detail view
- `COCRDUPC` -- Card update

**Data Ownership:** CARDDAT (exclusive), CCXREF (shared-read -- used by Account, Transaction, Financial, Reporting)

**Published Events (outbound):**
- `CardStatusChanged { cardNum, newStatus }` -- consumed by Transaction Processing
- Provides lookup service: `GetCardByAccount`, `GetAccountByCard`

**Consumed Events (inbound):** None directly (reads ACCTDAT for display enrichment only)

**Extraction Seam Analysis:**
- **Seam Type:** Clean boundary with shared-read reference data
- **Coupling Points:**
  1. CCXREF (Card Cross-Reference) is the **most shared data structure** in the system -- read by Account View, Transaction Processing, Bill Payment, Interest Calculation, and Statement Generation
  2. CARDAIX (alternate index on CARDDAT by account ID) used for account-scoped card lookups
- **Extraction Approach:** Card Management owns the cross-reference data and exposes it via a lookup API:
  - `GET /cards?accountId={id}` -- replaces CARDAIX browse
  - `GET /cards/{cardNum}/xref` -- replaces CCXREF direct reads
  - Consider a shared read-replica or cache (Redis) for high-frequency lookups during transition
- **Seam Difficulty:** LOW-MEDIUM -- CCXREF is read-only for other contexts; only Card Management writes it.

---

### 3.4 Transaction Processing Context

**Domain Purpose:** Capture, validate, post, and store all card transactions. This is the financial core of the system.

**Aggregate Root:** `Transaction` (CVTRA05Y)

**Entities:**
| Entity               | Source Copybook | Key Fields                              |
|----------------------|----------------|-----------------------------------------|
| Transaction          | CVTRA05Y       | TRAN-ID (PK), TRAN-TYPE-CD, TRAN-CAT-CD, TRAN-AMT, TRAN-CARD-NUM, TRAN-MERCHANT-*, TRAN-ORIG-TS, TRAN-PROC-TS |
| DailyTransaction     | CVTRA06Y       | DALYTRAN-ID, same structure as TRAN-RECORD (input staging) |
| TransactionCatBal    | CVTRA01Y       | Composite key (ACCT-ID + TYPE-CD + CAT-CD), TRAN-CAT-BAL |

**Programs Owned:**
- `COTRN00C` -- Transaction list (online, paginated browse of TRANSACT)
- `COTRN01C` -- Transaction view (online, detail display)
- `COTRN02C` -- Transaction add (online, validates and writes to TRANSACT)
- `CBTRN02C` -- **Core posting engine** (batch: DALYTRAN -> validate -> TRANSACT + ACCTDAT + TCATBALF)
- `CBTRN01C` -- Transaction file processing

**Data Ownership:** TRANSACT (shared-read with Reporting), DALYTRAN (exclusive input), TCATBALF (shared-read with Financial Operations), DALYREJS (exclusive output)

**Published Events (outbound):**
- `TransactionPosted { tranId, acctId, amount, type }` -- consumed by Account Management (balance update)
- `TransactionRejected { tranId, reason }` -- consumed by monitoring/alerting
- `CategoryBalanceUpdated { acctId, typeCd, catCd, balance }` -- consumed by Financial Operations

**Consumed Events (inbound):**
- `BillPaymentCreated { tranId, ... }` -- from Financial Operations (bill payment creates a transaction record)

**Extraction Seam Analysis:**
- **Seam Type:** Complex shared-write boundary -- CBTRN02C writes to both TRANSACT and ACCTDAT
- **Coupling Points:**
  1. CBTRN02C reads CCXREF (Card context) and ACCTDAT (Account context) during validation
  2. CBTRN02C writes to ACCTDAT to update balances after posting -- this is the **tightest coupling** in the system
  3. TCATBALF is maintained by CBTRN02C and consumed by CBACT04C (interest calculation)
  4. TRANSACT is read by Reporting context for statement generation
  5. COTRN02C (online add) creates transaction records that bypass the batch posting validation
- **Extraction Approach:**
  1. Introduce an **Event-Driven Architecture**: Transaction Processing publishes `TransactionPosted` events
  2. Account Management subscribes and updates its own balances (replacing the direct ACCTDAT write)
  3. During transition, use a **Change Data Capture (CDC)** pattern to sync TRANSACT to the new database while the mainframe still runs
  4. The cross-reference lookup becomes an API call to Card Management
- **Seam Difficulty:** HIGH -- This is the most coupled context. The batch posting engine's multi-file atomic updates require careful decomposition into eventual consistency or distributed transactions.

---

### 3.5 Financial Operations Context

**Domain Purpose:** Bill payment processing and interest calculation -- the financial computation engine.

**Aggregate Root:** `BillPayment` (derived from COBIL00C flow), `InterestCalculation` (derived from CBACT04C flow)

**Entities:**
| Entity           | Source Copybook | Key Fields                              |
|------------------|----------------|-----------------------------------------|
| DisclosureGroup  | CVTRA02Y       | Composite key (GROUP-ID + TYPE-CD + CAT-CD), DIS-INT-RATE |

**Programs Owned:**
- `COBIL00C` -- Bill payment (online: reads account, creates payment transaction, updates balance)
- `CBACT04C` -- Interest calculation (batch: reads TCATBALF, looks up DISCGRP rates, computes interest, updates ACCTDAT)

**Data Ownership:** DISCGRP (exclusive)

**Published Events (outbound):**
- `BillPaymentProcessed { acctId, amount, tranId }` -- consumed by Account Management and Transaction Processing
- `InterestApplied { acctId, amount }` -- consumed by Account Management

**Consumed Events (inbound):**
- `CategoryBalanceUpdated` -- from Transaction Processing (TCATBALF data)
- Account data (ACCTDAT) read for balance checks and updates

**Extraction Seam Analysis:**
- **Seam Type:** Complex boundary -- writes to both TRANSACT and ACCTDAT
- **Coupling Points:**
  1. COBIL00C reads ACCTDAT (Account context) to check balance
  2. COBIL00C reads CXACAIX (Card context) to find card number for the payment transaction
  3. COBIL00C reads TRANSACT to find the highest transaction ID (Transaction context)
  4. COBIL00C writes a new transaction record to TRANSACT (cross-context write)
  5. COBIL00C updates ACCTDAT balance (cross-context write)
  6. CBACT04C reads TCATBALF (Transaction context) for category balances
  7. CBACT04C reads DISCGRP (owned) for interest rates
  8. CBACT04C reads CCXREF (Card context) for cross-reference data
  9. CBACT04C rewrites ACCTDAT (Account context) with updated balances
- **Extraction Approach:**
  1. Bill Payment becomes a **Saga/Orchestrator** that:
     - Calls Account Management to verify balance
     - Calls Card Management to resolve card number
     - Calls Transaction Processing to create the payment transaction
     - Calls Account Management to adjust the balance
  2. Interest Calculation becomes a scheduled Spring Batch job that:
     - Queries Transaction Processing for category balances
     - Looks up its own Disclosure Group rates
     - Publishes `InterestApplied` events to Account Management
- **Seam Difficulty:** HIGH -- COBIL00C performs a multi-resource pseudo-atomic operation that must be decomposed into a saga with compensating transactions.

---

### 3.6 Reporting & Statements Context

**Domain Purpose:** Generate transaction reports and account statements in multiple formats.

**Aggregate Root:** None (read-only, no state mutations)

**Programs Owned:**
- `CORPT00C` -- Report request handler (online: validates date range, submits JCL to internal reader)
- `CBTRN03C` -- Transaction report generator (batch)
- `CBSTM03A` -- Statement generator (batch: text + HTML output)
- `CBSTM03B` -- Statement I/O subroutine (batch: called by CBSTM03A)

**Data Ownership:** None -- purely reads from other contexts

**Consumed Events (inbound):**
- Reads TRANSACT (Transaction Processing) for transaction data
- Reads ACCTDAT (Account Management) for account details
- Reads CUSTDAT (Account Management) for customer names/addresses
- Reads CCXREF (Card Management) for card-account mapping

**Extraction Seam Analysis:**
- **Seam Type:** Read-only boundary -- no data mutations
- **Coupling Points:**
  1. CBSTM03A reads from 4 different contexts (TRANSACT, ACCTDAT, CUSTDAT, CCXREF)
  2. CORPT00C submits JCL via CICS TDQ to the internal reader -- tightly coupled to JES2
  3. CBSTM03A uses ALTER/GO TO and pointer-based addressing (PSA/TCB/TIOT) -- cannot be mechanically translated
  4. CBSTM03A calls CBSTM03B as a subroutine for VSAM I/O operations
- **Extraction Approach:**
  1. Create a read-only Reporting Service that queries other contexts via APIs or maintains a denormalized read model (CQRS pattern)
  2. Replace JCL submission with scheduled Spring Batch jobs triggered by API request
  3. Complete rewrite of CBSTM03A using modern templating (the ALTER/GOTO patterns are untranslatable)
  4. Statement output in PDF/HTML instead of spool files
- **Seam Difficulty:** LOW -- No data mutations, but requires API access to 4 other contexts. Best implemented after all data-owning contexts are migrated.

---

### 3.7 Navigation & Presentation Context

**Domain Purpose:** UI rendering, menu navigation, and session management.

**Aggregate Root:** `UserSession` (derived from CARDDEMO-COMMAREA / COCOM01Y)

**Session State (COMMAREA fields):**
| Field                   | Purpose                                  |
|-------------------------|------------------------------------------|
| CDEMO-FROM-TRANID       | Source transaction ID (navigation trail)  |
| CDEMO-FROM-PROGRAM      | Source program name                       |
| CDEMO-TO-TRANID         | Target transaction ID                     |
| CDEMO-TO-PROGRAM        | Target program name                      |
| CDEMO-USER-ID           | Authenticated user                        |
| CDEMO-USER-TYPE         | Admin (A) or User (U)                    |
| CDEMO-PGM-CONTEXT       | Enter (0) or Reenter (1) state           |
| CDEMO-CUST-ID           | Current customer context                 |
| CDEMO-ACCT-ID           | Current account context                  |
| CDEMO-CARD-NUM          | Current card context                     |
| CDEMO-LAST-MAP/MAPSET   | Last displayed screen                    |

**Programs Owned:**
- `COMEN01C` -- Main menu (11 options defined in COMEN02Y)
- `COADM01C` -- Admin menu (6 options defined in COADM02Y)
- All 17 BMS maps in `app/bms/`
- All 17 BMS-generated copybooks in `app/cpy-bms/`

**Extraction Seam Analysis:**
- **Seam Type:** Presentation-layer boundary
- **Coupling Points:**
  1. Every online program receives and returns CARDDEMO-COMMAREA -- the universal session contract
  2. Menu programs use EXEC CICS XCTL to transfer control to functional programs
  3. BMS MAP SEND/RECEIVE is tightly coupled to 3270 terminal protocol
  4. PF key handling (DFHAID) drives navigation flow
- **Extraction Approach:**
  1. Replace with a web frontend (React SPA) that calls REST APIs
  2. COMMAREA session state maps to: JWT claims (user info) + URL parameters (entity context) + client-side routing (navigation)
  3. BMS map field definitions become form specifications for the new UI
  4. This is the **Strangler facade** -- the new UI layer is the entry point for progressive migration
- **Seam Difficulty:** LOW -- This context has no business logic; it's pure routing and rendering.

---

## 4. Cross-Reference File (CCXREF) -- The Critical Shared Seam

The Card Cross-Reference file (CCXREF/CVACT03Y) is the **most referenced data structure** across bounded contexts:

| Context                   | Program    | Access Pattern | Operation |
|---------------------------|------------|----------------|-----------|
| Account Management        | COACTVWC   | Read by ACCT-ID (CXACAIX) | View cards for account |
| Card Management           | COCRDLIC   | Read/Browse by CARD-NUM | List/paginate cards |
| Transaction Processing    | CBTRN02C   | Read by CARD-NUM | Validate card during posting |
| Transaction Processing    | COTRN02C   | Read by CARD-NUM and ACCT-ID | Validate during online add |
| Financial Operations      | COBIL00C   | Read by ACCT-ID (CXACAIX) | Find card for bill payment |
| Financial Operations      | CBACT04C   | Read by ACCT-ID (alternate key) | Interest calculation xref |
| Reporting & Statements    | CBSTM03A   | Read by CARD-NUM | Statement card lookup |

**Extraction Strategy for CCXREF:**
1. **Card Management** becomes the owner and system of record
2. Expose a **Card Lookup API** with both access patterns:
   - `GET /api/cards/xref?cardNum={num}` -- returns accountId, custId
   - `GET /api/cards/xref?accountId={id}` -- returns cardNum, custId
3. During transition, maintain a **read-through cache** (Redis) to avoid N+1 API calls from batch programs
4. For batch contexts (CBTRN02C, CBACT04C, CBSTM03A), pre-load the full cross-reference into memory at job start to avoid per-record API calls

---

## 5. Data Flow Diagram -- Batch Cycle

The batch cycle represents the most complex cross-context data flow:

```
Step 1: CLOSEFIL    -- Quiesce CICS file access
           |
Step 2: Data Refresh (ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE)
           |           [Account Mgmt]  [Card Mgmt]  [Transaction Processing]
           |
Step 3: POSTTRAN    -- CBTRN02C
           |           Reads: DALYTRAN, CCXREF [Card], ACCTDAT [Account]
           |           Writes: TRANSACT [Transaction], ACCTDAT [Account],
           |                   TCATBALF [Transaction], DALYREJS [Transaction]
           |
Step 4: INTCALC     -- CBACT04C
           |           Reads: TCATBALF [Transaction], CCXREF [Card],
           |                  DISCGRP [Financial], ACCTDAT [Account]
           |           Writes: ACCTDAT [Account], TRANSACT [Transaction]
           |
Step 5: TRANBKP     -- Backup transaction file
           |
Step 6: COMBTRAN    -- Combine daily + master transactions
           |
Step 7: CREASTMT    -- CBSTM03A/B
           |           Reads: TRANSACT [Transaction], CCXREF [Card],
           |                  CUSTDAT [Account], ACCTDAT [Account]
           |           Writes: Statement files (text + HTML)
           |
Step 8: TRANIDX     -- Rebuild alternate indexes
           |
Step 9: OPENFIL     -- Resume CICS file access
```

**Key Insight:** Steps 3 and 4 are the most cross-cutting -- they read from 3-4 contexts and write to 2 contexts each. In the target architecture, these become **orchestrated sagas** or **Spring Batch jobs with explicit API boundaries**.

---

## 6. Bounded Context Extraction Priority

Based on seam difficulty, coupling analysis, and business risk:

| Priority | Bounded Context            | Seam Difficulty | Reason                                    |
|----------|---------------------------|-----------------|-------------------------------------------|
| 1        | Identity & Access Mgmt    | LOW             | Exclusive data, no inbound deps            |
| 2        | Navigation & Presentation | LOW             | No business logic, facade for strangler    |
| 3        | Card Management           | LOW-MEDIUM      | Clean data ownership, shared reads manageable |
| 4        | Account Management        | MEDIUM          | Shared-write to ACCTDAT requires ACL       |
| 5        | Reporting & Statements    | LOW (but late)  | Read-only, but depends on all other contexts |
| 6        | Transaction Processing    | HIGH            | Core financial flow, multi-context writes  |
| 7        | Financial Operations      | HIGH            | Saga pattern needed for bill payment       |

---

## 7. Anti-Corruption Layer (ACL) Requirements

During the migration transition period, these ACLs bridge the legacy mainframe and new Java services:

| ACL                        | Purpose                                           | Pattern                    |
|----------------------------|---------------------------------------------------|----------------------------|
| VSAM-to-API Adapter        | Translate VSAM file reads into REST API calls      | Adapter / Gateway          |
| COMMAREA-to-JWT Translator | Map CICS session state to JWT token claims         | Token Exchange             |
| Batch File Sync            | Bidirectional sync between VSAM files and RDBMS    | CDC / Change Data Capture  |
| Transaction Coordinator    | Ensure consistency during dual-write transition    | Saga / Outbox Pattern      |
| BMS-to-REST Facade         | New web UI calls Java services while mainframe runs| API Gateway / BFF          |

---

## 8. Target Microservice Architecture

```
                    +------------------+
                    |   API Gateway    |
                    |   (Spring Cloud) |
                    +--------+---------+
                             |
         +-------------------+-------------------+
         |         |         |         |         |
    +----v---+ +---v----+ +-v------+ +v-------+ +v---------+
    |Identity| |Account | |Card    | |Transact| |Financial |
    |Service | |Service | |Service | |Service | |Service   |
    +--------+ +--------+ +--------+ +--------+ +----------+
    |Spring  | |Spring  | |Spring  | |Spring  | |Spring    |
    |Security| |Boot+JPA| |Boot+JPA| |Boot+JPA| |Boot+JPA  |
    |JWT/OAuth| |        | |        | |Batch   | |Batch     |
    +----+---+ +---+----+ +---+----+ +---+----+ +----+-----+
         |         |           |          |           |
         v         v           v          v           v
    [User DB] [Account DB] [Card DB] [Trans DB] [Financial DB]
                                                      |
                                          +-----------+
                                          v
                                   +-------------+
                                   |  Reporting  |
                                   |  Service    |
                                   | (read model)|
                                   +-------------+
```

Each service owns its database (Database-per-Service pattern). Cross-service communication uses:
- **Synchronous:** REST APIs for queries (e.g., card lookup)
- **Asynchronous:** Events (Kafka/RabbitMQ) for state changes (e.g., TransactionPosted, BalanceUpdated)
