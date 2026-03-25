# CardDemo Domain Decomposition

## 1. Overview

This document identifies bounded contexts within the CardDemo COBOL/CICS/VSAM application, maps the coupling between legacy components, and defines extraction seams -- the natural boundaries where the monolith can be split into independently deployable microservices.

---

## 2. Bounded Context Map

```
+------------------------------------------------------------------+
|                        CardDemo Monolith                          |
|                                                                   |
|  +------------------+    +------------------+    +--------------+ |
|  |   Identity &     |    |    Account       |    |    Card      | |
|  |   Access Mgmt    |--->|    Management    |<---|  Management  | |
|  |                  |    |                  |    |              | |
|  | COSGN00C         |    | COACTVWC         |    | COCRDLIC     | |
|  | COADM01C         |    | COACTUPC         |    | COCRDSLC     | |
|  | COMEN01C         |    |                  |    | COCRDUPC     | |
|  | COUSR00C-03C     |    | VSAM: ACCTDAT    |    |              | |
|  |                  |    | VSAM: CUSTDAT    |    | VSAM: CARDDAT| |
|  | VSAM: USRSEC     |    +--------+---------+    +------+-------+ |
|  +------------------+             |                      |         |
|                                   |   +------------------+         |
|                                   v   v                            |
|                          +--------+---+------+                     |
|                          |  Cross-Reference  |                     |
|                          |  (Shared Kernel)   |                     |
|                          |                    |                     |
|                          | VSAM: CARDXREF     |                     |
|                          | VSAM: CXACAIX      |                     |
|                          +--------+-----------+                     |
|                                   |                                |
|            +----------------------+----------------------+         |
|            v                      v                      v         |
|  +---------+--------+  +---------+--------+  +----------+-------+ |
|  |   Transaction     |  |    Payment       |  |   Reporting      | |
|  |   Processing      |  |    Processing    |  |                  | |
|  |                   |  |                  |  | CORPT00C         | |
|  | COTRN00C (list)   |  | COBIL00C         |  | CBTRN03C (batch) | |
|  | COTRN01C (view)   |  |                  |  | CBSTM03A/B       | |
|  | COTRN02C (add)    |  | VSAM: TRANSACT   |  |                  | |
|  |                   |  | VSAM: ACCTDAT    |  | VSAM: TRANSACT   | |
|  | VSAM: TRANSACT    |  +------------------+  +------------------+ |
|  +-------------------+                                             |
|            |                                                       |
|            v                                                       |
|  +---------+--------+  +---------+--------+  +------------------+ |
|  | Batch Processing  |  | Authorization    |  | Reference Data   | |
|  |                   |  | (Optional Ext.)  |  | (Optional Ext.)  | |
|  | CBTRN02C (post)   |  |                  |  |                  | |
|  | CBACT04C (interest)|  | COPAUA0C (MQ)    |  | COTRTUPC (DB2)   | |
|  | CBSTM03A/B (stmt) |  | COPAUS0C/1C/2C   |  | COTRTLIC (DB2)   | |
|  | CBACT01C-03C      |  | CBPAUP0C (purge) |  | COBTUPDT (batch) | |
|  | CBCUS01C          |  |                  |  |                  | |
|  | CBEXPORT/IMPORT   |  | IMS DB, DB2, MQ  |  | DB2 tables       | |
|  +-------------------+  +------------------+  +------------------+ |
+------------------------------------------------------------------+
```

---

## 3. Bounded Contexts Detail

### BC-1: Identity & Access Management

**Responsibility**: User authentication, authorization (admin vs. regular), session management, user CRUD operations.

| Component | Type | Role |
|-----------|------|------|
| COSGN00C | CICS Online | Sign-on screen, credential validation |
| COMEN01C | CICS Online | Regular user main menu (routing) |
| COADM01C | CICS Online | Admin menu (routing) |
| COUSR00C | CICS Online | User list |
| COUSR01C | CICS Online | User add |
| COUSR02C | CICS Online | User update |
| COUSR03C | CICS Online | User delete |
| CSUSR01Y | Copybook | User security record layout (80 bytes) |
| USRSEC | VSAM KSDS | User security file |

**Data Ownership**: USRSEC file (user credentials and role type).

**Upstream Dependencies**: None -- this is the entry point.

**Downstream Consumers**: All other contexts depend on the authenticated user identity (user-id, user-type) passed via CARDDEMO-COMMAREA.

**Extraction Seam Analysis**:
- **Seam Type**: Clean API boundary
- **Seam Location**: The COMMAREA fields `CDEMO-USER-ID` and `CDEMO-USER-TYPE` are the only data flowing downstream
- **Extraction Difficulty**: Low
- **Notes**: Replace VSAM lookup with a database-backed auth service. The COMMAREA user context becomes a JWT token. XCTL transfers to menu programs become redirects in a web frontend.

---

### BC-2: Account Management

**Responsibility**: Account lifecycle -- viewing account details, updating account attributes (status, credit limits, balances, dates).

| Component | Type | Role |
|-----------|------|------|
| COACTVWC | CICS Online | Account view (942 lines) |
| COACTUPC | CICS Online | Account update (4,237 lines -- largest program) |
| CVACT01Y | Copybook | Account record (300 bytes) |
| CVCUS01Y | Copybook | Customer record (500 bytes) |
| CVACT03Y | Copybook | Card cross-reference record (50 bytes) |
| ACCTDAT | VSAM KSDS | Account master file |
| CUSTDAT | VSAM KSDS | Customer master file |
| CXACAIX | VSAM AIX | Card xref by account alternate index |

**Data Ownership**: ACCTDAT (accounts), CUSTDAT (customers) -- shared read with other contexts.

**Upstream Dependencies**: BC-1 (authenticated user context).

**Downstream Consumers**: BC-3 (Card Mgmt reads account data), BC-4 (Transaction Processing), BC-5 (Payment), BC-7 (Batch).

**Extraction Seam Analysis**:
- **Seam Type**: Data seam + API boundary
- **Seam Location**: VSAM READ/REWRITE operations on ACCTDAT and CUSTDAT are the data seam. The COMMAREA + XCTL transfer is the control seam.
- **Extraction Difficulty**: Medium-High
- **Coupling Points**:
  - COACTUPC directly reads CUSTDAT, CARDDAT, and CARDXREF -- cross-context data access
  - Account balance is updated by both this context AND by Batch Processing (CBACT04C) and Payment (COBIL00C)
  - Customer data is shared with Statement Generation (CBSTM03A)
- **Notes**: Account and Customer should be in the same bounded context since COACTUPC updates both. The cross-reference file (CARDXREF) is a shared kernel that becomes FK relationships in the relational model.

---

### BC-3: Card Management

**Responsibility**: Credit card lifecycle -- listing, viewing, and updating card details.

| Component | Type | Role |
|-----------|------|------|
| COCRDLIC | CICS Online | Card list (browsing by account) |
| COCRDSLC | CICS Online | Card detail view |
| COCRDUPC | CICS Online | Card update |
| CVACT02Y | Copybook | Card record (150 bytes) |
| CVACT03Y | Copybook | Card cross-reference record (50 bytes) |
| CARDDAT | VSAM KSDS | Card master file |
| CARDAIX | VSAM AIX | Card by account alternate index |

**Data Ownership**: CARDDAT (cards).

**Upstream Dependencies**: BC-1 (user context), BC-2 (account/customer data for display).

**Downstream Consumers**: BC-4 (Transaction Processing uses card number), BC-5 (Payment looks up card via xref).

**Extraction Seam Analysis**:
- **Seam Type**: API boundary with shared data
- **Seam Location**: VSAM READ operations on CARDDAT. Navigation from Account View to Card List via XCTL.
- **Extraction Difficulty**: Medium
- **Coupling Points**:
  - Card List (COCRDLIC) is invoked from Account View (COACTVWC) via XCTL -- tight navigation coupling
  - Cross-reference (CARDXREF) is read by multiple contexts
- **Notes**: Consider merging with BC-2 (Account Management) into a single "Account & Card Service" if the team is small. The card-to-account relationship is the strongest coupling in the system.

---

### BC-4: Transaction Processing

**Responsibility**: Online transaction lifecycle -- listing, viewing, and adding transactions.

| Component | Type | Role |
|-----------|------|------|
| COTRN00C | CICS Online | Transaction list (paginated browse) |
| COTRN01C | CICS Online | Transaction detail view |
| COTRN02C | CICS Online | Transaction add (creates new records) |
| CVTRA05Y | Copybook | Transaction record (350 bytes) |
| TRANSACT | VSAM KSDS | Transaction master file |

**Data Ownership**: TRANSACT (transactions) -- shared with BC-5, BC-6, BC-7.

**Upstream Dependencies**: BC-1 (user context), BC-3 (card number for transaction add).

**Downstream Consumers**: BC-6 (Reporting), BC-7 (Batch reads transaction data).

**Extraction Seam Analysis**:
- **Seam Type**: Data seam
- **Seam Location**: VSAM BROWSE/READ/WRITE on TRANSACT file. The TRAN-ID is a monotonically increasing key used for sequencing.
- **Extraction Difficulty**: Medium
- **Coupling Points**:
  - Transaction List allows drill-down to Transaction View via selection code + XCTL
  - Bill Payment (BC-5) writes to the same TRANSACT file
  - Batch posting (BC-7) reads TRANSACT for statement generation
  - Transaction ID generation is based on reading the last record (READPREV with HIGH-VALUES) -- shared sequence
- **Notes**: The shared write access to TRANSACT from both online (COTRN02C, COBIL00C) and batch (CBTRN02C) is the most critical data contention point. In the target architecture, this becomes a single `transactions` table with proper isolation levels.

---

### BC-5: Payment Processing

**Responsibility**: Bill payment -- validate account, record payment transaction, update account balance.

| Component | Type | Role |
|-----------|------|------|
| COBIL00C | CICS Online | Bill payment (573 lines) |
| CVACT01Y | Copybook | Account record |
| CVTRA05Y | Copybook | Transaction record |
| CVACT03Y | Copybook | Card cross-reference |
| ACCTDAT | VSAM KSDS | Account master (read + update) |
| TRANSACT | VSAM KSDS | Transaction master (write) |
| CXACAIX | VSAM AIX | Card xref by account |

**Data Ownership**: None exclusively -- this is a coordinator/orchestrator context.

**Upstream Dependencies**: BC-1 (user context), BC-2 (account data), BC-3 (card data via xref).

**Downstream Consumers**: BC-4 (newly written transactions appear in lists), BC-7 (batch processes posted transactions).

**Extraction Seam Analysis**:
- **Seam Type**: Orchestration seam (saga candidate)
- **Seam Location**: The payment flow is: READ ACCTDAT -> validate balance -> READ CXACAIX -> generate tran ID -> WRITE TRANSACT -> REWRITE ACCTDAT. This is a multi-step transaction.
- **Extraction Difficulty**: High
- **Coupling Points**:
  - Reads and updates ACCTDAT (owned by BC-2)
  - Writes to TRANSACT (owned by BC-4)
  - Reads CXACAIX (shared kernel)
  - Transaction ID generation shares the same monotonic sequence as BC-4
- **Notes**: This is the strongest candidate for a saga pattern in the target architecture. The payment operation spans Account and Transaction aggregates. In CICS, this is handled by CICS task-level recovery; in Java, it needs explicit saga/compensating transaction design.

---

### BC-6: Reporting

**Responsibility**: Generate transaction reports (online submission and batch execution), account statements.

| Component | Type | Role |
|-----------|------|------|
| CORPT00C | CICS Online | Report request screen, JCL submission via TDQ |
| CBTRN03C | Batch | Transaction report generation |
| CBSTM03A | Batch | Statement generation (text + HTML) -- 924 lines |
| CBSTM03B | Batch | Statement subroutine (file I/O via CALL) |
| CSUTLDTC | Utility | Date validation |
| COSTM01 | Copybook | Statement control structure |

**Data Ownership**: None -- reads from TRANSACT, ACCTDAT, CUSTDAT, XREFFILE.

**Upstream Dependencies**: BC-2 (account/customer data), BC-4 (transaction data).

**Downstream Consumers**: End users (printed reports, HTML statements).

**Extraction Seam Analysis**:
- **Seam Type**: Read-only boundary (ideal for extraction)
- **Seam Location**: All data access is read-only. Reports are generated from snapshots of account/transaction data.
- **Extraction Difficulty**: Low-Medium (read-only), but CBSTM03A has complex constructs
- **Coupling Points**:
  - CORPT00C submits JCL via extra-partition TDQ (mainframe-specific)
  - CBSTM03A uses ALTER/GO TO and PSA/TCB/TIOT addressing (platform-specific)
  - CBSTM03B is called as a subroutine for file I/O
- **Notes**: Reporting is a natural extraction candidate because it is read-only. However, CBSTM03A's use of ALTER/GO TO and control block addressing means it should be replatformed initially rather than auto-converted.

---

### BC-7: Batch Processing

**Responsibility**: End-of-day and periodic batch operations -- transaction posting, interest calculation, data refresh, backups.

| Component | Type | Role |
|-----------|------|------|
| CBTRN02C | Batch | Transaction posting (732 lines) |
| CBACT04C | Batch | Interest calculation (653 lines) |
| CBACT01C-03C | Batch | Account data load/refresh |
| CBCUS01C | Batch | Customer data load |
| CBTRN01C | Batch | Transaction data load |
| CBEXPORT | Batch | Data export |
| CBIMPORT | Batch | Data import |
| CVTRA06Y | Copybook | Daily transaction record (350 bytes) |
| CVTRA01Y | Copybook | Transaction category balance (50 bytes) |
| CVTRA02Y | Copybook | Discount group (50 bytes) |

**Data Ownership**: DALYTRAN (daily transactions), TCATBALF (category balances), DISCGRP (discount groups).

**Upstream Dependencies**: BC-4 (transaction data), BC-2 (account data).

**Downstream Consumers**: BC-2 (account balances updated by interest calc), BC-6 (reporting reads posted transactions).

**Extraction Seam Analysis**:
- **Seam Type**: Temporal seam (batch window)
- **Seam Location**: The batch cycle has a strict ordering: CLOSEFIL -> data refresh -> POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT -> TRANIDX -> OPENFIL. This is orchestrated by JCL job dependencies.
- **Extraction Difficulty**: Medium-High
- **Coupling Points**:
  - Transaction posting (CBTRN02C) reads DALYTRAN, writes TRANSACT, updates ACCTDAT, writes DALYREJS, updates TCATBALF -- spans multiple data stores
  - Interest calculation (CBACT04C) reads TCATBALF, DISCGRP, XREFFILE, updates ACCTDAT, writes TRANSACT
  - CLOSEFIL/OPENFIL jobs coordinate with CICS to close/open files for batch access
- **Notes**: The CLOSEFIL/OPENFIL pattern is the clearest indicator of batch-online contention. In the target architecture, this is eliminated by using a shared relational database with proper transaction isolation. Batch jobs become Spring Batch jobs with chunk-oriented processing.

---

### BC-8: Authorization Processing (Optional Extension)

**Responsibility**: Real-time credit card authorization, fraud detection, authorization lifecycle management.

| Component | Type | Role |
|-----------|------|------|
| COPAUA0C | CICS Online | MQ-triggered authorization processor |
| COPAUS0C | CICS Online | Authorization summary view |
| COPAUS1C | CICS Online | Authorization detail view |
| COPAUS2C | CICS Online | Fraud marking (DB2 write) |
| CBPAUP0C | Batch | Expired authorization purge |

**Data Ownership**: IMS HIDAM DB (authorizations), DB2 AUTHFRDS table (fraud records).

**Upstream Dependencies**: BC-2 (account data via VSAM), BC-3 (card data via xref).

**Downstream Consumers**: External systems via MQ reply queue.

**Extraction Seam Analysis**:
- **Seam Type**: Messaging seam (already loosely coupled via MQ)
- **Seam Location**: MQ request/response queues provide a natural API boundary. The authorization processor is already event-driven.
- **Extraction Difficulty**: High (due to IMS/DB2/MQ technology stack), but architecturally well-suited
- **Coupling Points**:
  - Reads VSAM account/customer/xref data for authorization decisions
  - Writes to IMS HIDAM for authorization storage
  - Writes to DB2 for fraud tracking
  - MQ trigger initiation model
- **Notes**: Despite using three different data technologies (IMS, DB2, VSAM), this context is already loosely coupled through messaging. It is the best candidate for a clean microservice extraction. Replace IMS with PostgreSQL tables, DB2 fraud table with a JPA entity, MQ with Kafka/SQS.

---

### BC-9: Reference Data Management (Optional Extension)

**Responsibility**: CRUD operations for transaction type reference data.

| Component | Type | Role |
|-----------|------|------|
| COTRTUPC | CICS Online | Transaction type add/edit |
| COTRTLIC | CICS Online | Transaction type list/delete |
| COBTUPDT | Batch | Batch transaction type update |

**Data Ownership**: DB2 TRANSACTION_TYPE and TRANSACTION_TYPE_CATEGORY tables.

**Upstream Dependencies**: BC-1 (admin user context).

**Downstream Consumers**: BC-7 (batch processing uses transaction types for categorization).

**Extraction Seam Analysis**:
- **Seam Type**: Clean data boundary (already uses SQL)
- **Seam Location**: DB2 tables are the data seam. Admin-only access reduces contention.
- **Extraction Difficulty**: Low
- **Notes**: Already SQL-based; straightforward conversion to Spring Data JPA. Can be extracted as a standalone Reference Data Service or folded into the Transaction Service as a configuration API.

---

## 4. Shared Kernel: Cross-Reference Data

The card cross-reference file (CARDXREF / CVACT03Y) is the most pervasive shared data structure in CardDemo. It maps:

```
Card Number (16) --> Customer ID (9) --> Account ID (11)
```

This is accessed by:
- BC-2 (Account Management) -- account-to-card lookup
- BC-3 (Card Management) -- card-to-account lookup
- BC-5 (Payment Processing) -- card lookup for payment
- BC-6 (Reporting) -- statement generation
- BC-7 (Batch Processing) -- transaction posting validation, interest calculation
- BC-8 (Authorization) -- card validation

**Target Architecture Resolution**: Eliminate the cross-reference file. In the relational model:
- `cards.account_id` FK replaces the card-to-account lookup
- `accounts.customer_id` FK replaces the account-to-customer lookup
- Database JOINs replace the multi-file lookup chain

---

## 5. Communication Area (COMMAREA) Analysis

The CARDDEMO-COMMAREA (copybook COCOM01Y) is the primary inter-program communication mechanism:

```
CARDDEMO-COMMAREA (total ~190 bytes)
  CDEMO-GENERAL-INFO
    CDEMO-FROM-TRANID      PIC X(04)    -- caller transaction
    CDEMO-FROM-PROGRAM     PIC X(08)    -- caller program
    CDEMO-TO-TRANID        PIC X(04)    -- target transaction
    CDEMO-TO-PROGRAM       PIC X(08)    -- target program
    CDEMO-USER-ID          PIC X(08)    -- authenticated user
    CDEMO-USER-TYPE        PIC X(01)    -- 'A'dmin or 'U'ser
    CDEMO-PGM-CONTEXT      PIC 9(01)    -- 0=enter, 1=reenter
  CDEMO-CUSTOMER-INFO
    CDEMO-CUST-ID          PIC 9(09)
    CDEMO-CUST-FNAME/MNAME/LNAME
  CDEMO-ACCOUNT-INFO
    CDEMO-ACCT-ID          PIC 9(11)
    CDEMO-ACCT-STATUS      PIC X(01)
  CDEMO-CARD-INFO
    CDEMO-CARD-NUM         PIC 9(16)
  CDEMO-MORE-INFO
    CDEMO-LAST-MAP/MAPSET  PIC X(7)
```

**Target Architecture Mapping**:

| COMMAREA Field | Target Equivalent |
|---------------|------------------|
| CDEMO-USER-ID, CDEMO-USER-TYPE | JWT token claims |
| CDEMO-FROM/TO-PROGRAM | HTTP routing / API gateway |
| CDEMO-PGM-CONTEXT | Stateless REST (no reenter concept) |
| CDEMO-CUST-ID, ACCT-ID, CARD-NUM | URL path parameters or query params |
| CDEMO-LAST-MAP/MAPSET | Frontend router state |

Some programs extend the COMMAREA with context-specific data (e.g., COTRN00C adds `CDEMO-CT00-INFO` for pagination state, COBIL00C adds `CDEMO-CB00-INFO` for bill payment state). These become:
- Pagination: query parameters (`?page=2&after=<last-tran-id>`)
- Payment state: request body or frontend state

---

## 6. Extraction Priority and Seam Summary

| Bounded Context | Extraction Seam Type | Difficulty | Priority | Rationale |
|----------------|---------------------|-----------|----------|-----------|
| BC-9 Reference Data | Clean data boundary | Low | 1 (First) | Already SQL-based, admin-only, no contention |
| BC-1 Identity & Access | Clean API boundary | Low | 2 | Entry point; enables all other extractions |
| BC-6 Reporting | Read-only boundary | Low-Medium | 3 | No write contention; CBSTM03A replatformed |
| BC-14 VSAM-MQ Extraction | Messaging boundary | Low | 3 | Tiny scope, easily replaced with REST APIs |
| BC-3 Card Management | API + shared data | Medium | 4 | Read-heavy, moderate coupling |
| BC-4 Transaction Processing | Data seam | Medium | 5 | Core data entity, shared writes |
| BC-2 Account Management | Data + API boundary | Medium-High | 6 | Central entity, many consumers |
| BC-5 Payment Processing | Orchestration seam | High | 7 | Saga pattern needed, cross-aggregate writes |
| BC-7 Batch Processing | Temporal seam | Medium-High | 8 | Requires all upstream data migrated |
| BC-8 Authorization | Messaging seam | High (technology) | 9 (Last) | Complex tech stack (IMS/DB2/MQ) |

---

## 7. Anti-Corruption Layer Design

During the migration period, when some contexts are modernized and others remain on the mainframe, an Anti-Corruption Layer (ACL) is required:

```
+------------------+     +------------------+     +------------------+
|  New Java        |     | Anti-Corruption   |     |  Legacy COBOL    |
|  Microservice    |---->| Layer (ACL)       |---->|  Program         |
|                  |     |                   |     |                  |
| REST API         |     | - Protocol xlate  |     | - CICS/VSAM      |
| JPA Entities     |     | - Data mapping    |     | - Copybook layout|
| Domain Events    |     | - EBCDIC/ASCII    |     | - COMMAREA       |
+------------------+     +------------------+     +------------------+
```

### ACL Components

1. **CICS Transaction Gateway (CTG) Adapter**: Enables Java services to invoke CICS transactions during hybrid operation. Maps REST requests to COMMAREA-based CICS calls.

2. **Data Synchronizer**: Bi-directional sync between VSAM files and relational database tables during the dual-write period. Uses CDC (Change Data Capture) or timed batch reconciliation.

3. **Message Bridge**: Translates between legacy MQ message formats (CSV-based authorization requests) and modern event formats (JSON/Avro over Kafka).

4. **Copybook-to-DTO Mapper**: Static mapping layer that converts between COBOL copybook record layouts (fixed-length, EBCDIC) and Java DTOs. Generated from copybook analysis:

| Copybook | Java DTO | Size |
|----------|----------|------|
| CVACT01Y | `AccountDto` | 300 -> ~12 fields |
| CVACT02Y | `CardDto` | 150 -> ~6 fields |
| CVCUS01Y | `CustomerDto` | 500 -> ~15 fields |
| CVTRA05Y | `TransactionDto` | 350 -> ~13 fields |
| CSUSR01Y | `UserSecurityDto` | 80 -> ~5 fields |
| CVACT03Y | `CardXrefDto` | 50 -> ~3 fields (eliminated in target) |

---

## 8. Domain Events (Target Architecture)

In the modernized architecture, bounded contexts communicate via domain events rather than shared VSAM files:

| Event | Producer | Consumer(s) |
|-------|----------|------------|
| `AccountUpdated` | Account Service | Card Service, Reporting, Batch |
| `CardStatusChanged` | Card Service | Account Service |
| `TransactionCreated` | Transaction Service | Reporting, Batch Processing |
| `PaymentProcessed` | Payment Service | Account Service, Transaction Service |
| `AuthorizationDecided` | Authorization Service | Account Service (hold amount) |
| `FraudDetected` | Authorization Service | Account Service (freeze), Notification |
| `StatementGenerated` | Reporting Service | Notification (email/PDF delivery) |
| `InterestCalculated` | Batch Processing | Account Service (balance update) |
| `TransactionTypeUpdated` | Reference Data Service | Transaction Service (cache refresh) |
