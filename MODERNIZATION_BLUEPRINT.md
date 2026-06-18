# CardDemo Modernization Blueprint

## Executive Summary

CardDemo is a ~25,000-line COBOL/CICS/JCL mainframe credit card management system with six major functional areas. This blueprint evaluates four modernization strategies for each area and recommends the optimal approach based on business logic complexity, data coupling, team skill availability, and risk tolerance.

---

## Application Inventory Summary

| Metric | Count |
|---|---|
| Online COBOL Programs (CICS) | 22 |
| Batch COBOL Programs | 14 |
| Copybooks (shared data structures) | 31 |
| BMS Screen Maps | 18 |
| JCL Job Definitions | 31 |
| VSAM Data Files (KSDS/AIX) | 12 |
| Optional Modules (IMS/DB2/MQ) | 3 |
| Total COBOL LOC (approx.) | ~25,000 |

---

## Functional Area Analysis

### 1. Authentication & User Management

**Programs:** `COSGN00C` (260 LOC), `COADM01C` (288 LOC), `COUSR00C` (695 LOC), `COUSR01C` (299 LOC), `COUSR02C` (414 LOC), `COUSR03C` (359 LOC)

**Copybooks:** `CSUSR01Y` (user security record), `COCOM01Y` (commarea), `COADM02Y` (admin menu options)

**Data Stores:** `USRSEC` VSAM KSDS (80-byte records: user ID, name, password, type)

**Business Logic Complexity:** LOW. Simple CRUD on a flat user record. Authentication is a direct VSAM key lookup comparing plaintext passwords. The admin menu is a static table-driven dispatcher (COADM02Y). No complex branching or business rules.

**Data Coupling:** LOW. USRSEC is accessed only by these six programs. The sole outbound dependency is the commarea field `CDEMO-USER-TYPE` (A/U flag) consumed by the menu dispatcher.

#### Strategy Evaluation

| Strategy | Fit | Justification |
|---|---|---|
| **(a) Strangler** | Fair | Could wrap with an auth API, but the logic is too simple to justify incremental wrapping overhead. |
| **(b) Replatform** | Poor | Keeping plaintext password logic on a cloud runtime perpetuates a security liability. |
| **(c) Refactor** | Poor | The code is already simple. Refactoring COBOL here yields no meaningful benefit. |
| **(d) Rewrite** | **RECOMMENDED** | Replace with a Spring Security / JWT module. The logic maps 1:1 to standard auth patterns. Eliminates plaintext passwords. Lowest-risk rewrite target due to minimal business logic and isolated data. |

**Recommended Strategy: REWRITE to Java/Spring Security + JWT**

---

### 2. Account Management

**Programs:** `COACTVWC` (941 LOC), `COACTUPC` (4,236 LOC)

**Copybooks:** `CVACT01Y` (account record, 300 bytes), `CVACT02Y` (card record, 150 bytes), `CVACT03Y` (card XREF, 50 bytes), `CVCUS01Y` (customer record, 500 bytes), `CVCRD01Y`, `CSUTLDWY` (date validation), `CSUTLDPY` (utility)

**Data Stores:** `ACCTDAT` (account master), `CARDDAT`/`CARDAIX` (card data + AIX), `CUSTDAT` (customer master), `CXACAIX` (cross-reference AIX)

**Business Logic Complexity:** HIGH. `COACTUPC` alone is 4,236 lines. It implements:
- Multi-field input validation (SSN parts with specific invalid-value checks like 666/900-999, US phone number format parsing, date range validation including leap year checks via `CSUTLDWY`)
- Cross-file lookups: account -> card XREF -> customer
- Field-by-field change detection against prior values
- Account status transitions (active/inactive)
- Credit limit and balance constraint enforcement

**Data Coupling:** HIGH. Account View/Update reads from 4 VSAM files (ACCTDAT, CARDDAT, CUSTDAT, CXACAIX). Account data is the central entity consumed by Transaction Processing, Bill Payment, Interest Calculation, and Statement Generation.

#### Strategy Evaluation

| Strategy | Fit | Justification |
|---|---|---|
| **(a) Strangler** | **RECOMMENDED** | Wrap with REST APIs (GET/PUT `/accounts/{id}`). The CICS screens become consumers of the new API. High data coupling means this must be migrated carefully; the strangler pattern allows coexistence during migration. |
| **(b) Replatform** | Fair | Could run on UniKix/Microfocus, but 4,200 LOC of validation logic is a maintenance burden in COBOL. |
| **(c) Refactor** | Fair | Could reduce complexity by extracting validation into shared paragraphs, but doesn't solve the platform dependency. |
| **(d) Rewrite** | Risky | 4,236 LOC of intricate validation makes a big-bang rewrite high-risk. Easy to introduce subtle defects in SSN/phone/date parsing. |

**Recommended Strategy: STRANGLER PATTERN (API-first extraction)**

---

### 3. Credit Card Management

**Programs:** `COCRDLIC` (1,459 LOC), `COCRDSLC` (887 LOC), `COCRDUPC` (1,560 LOC)

**Copybooks:** `CVACT02Y` (card record), `CVCRD01Y` (card working storage), `CVACT03Y` (XREF), `CVCUS01Y` (customer)

**Data Stores:** `CARDDAT` (card master VSAM KSDS), `CARDAIX` (alternate index by account), `CXACAIX` (XREF AIX), `CUSTDAT`

**Business Logic Complexity:** MEDIUM. Card list supports browsing with forward/backward pagination via VSAM STARTBR/READNEXT/READPREV. Card detail view does multi-file joins (card -> XREF -> customer). Card update validates expiry dates, embossed name, status transitions, and CVV. Uses OCCURS arrays for screen row selection (7-row page).

**Data Coupling:** MEDIUM. Reads from CARDDAT/CARDAIX (shared with Account View). The XREF file is the central coupling point between cards, customers, and accounts.

#### Strategy Evaluation

| Strategy | Fit | Justification |
|---|---|---|
| **(a) Strangler** | **RECOMMENDED** | Expose card CRUD via REST (`/cards`, `/cards/{num}`). Pagination translates naturally to query parameters. Co-exists with account API migration. |
| **(b) Replatform** | Poor | The VSAM browse/pagination logic is the primary complexity; this doesn't improve on cloud. |
| **(c) Refactor** | Fair | Could extract pagination into a reusable module, but still COBOL-bound. |
| **(d) Rewrite** | Fair | Moderate complexity makes rewrite feasible, but the shared XREF coupling with Account means it should be synchronized with Account migration. |

**Recommended Strategy: STRANGLER PATTERN (co-migrate with Account domain)**

---

### 4. Transaction Processing (Online + Batch)

**Online Programs:** `COTRN00C` (699 LOC), `COTRN01C` (330 LOC), `COTRN02C` (783 LOC), `CORPT00C` (649 LOC), `COBIL00C` (572 LOC)

**Batch Programs:** `CBTRN01C` (494 LOC), `CBTRN02C` (731 LOC — core POSTTRAN), `CBTRN03C` (649 LOC — report generator), `CBACT04C` (652 LOC — interest calculator), `CBSTM03A` (924 LOC — statement printer)

**Copybooks:** `CVTRA05Y` (transaction record, 350 bytes), `CVTRA06Y` (daily transaction), `CVTRA01Y` (category balance), `CVTRA02Y` (disclosure group), `CVTRA03Y` (transaction type), `CVTRA04Y` (transaction category), `CVTRA07Y`

**Data Stores:** `TRANSACT` (transaction VSAM KSDS), `DALYTRAN` (daily input sequential), `DALYREJS` (rejects sequential), `TCATBALF` (category balance VSAM), `DISCGRP` (disclosure groups VSAM), `TRANTYPE` (type lookup VSAM), `TRANCATG` (category lookup VSAM), GDG for backups

**Business Logic Complexity:** HIGH. This is the most complex domain:
- **POSTTRAN (CBTRN02C):** Reads daily transactions, validates card number against XREF, looks up account, validates transaction against account status/limits, writes to TRANSACT file, updates category balances in TCATBALF, writes rejects to DALYREJS with validation trailers
- **Interest Calculator (CBACT04C):** Iterates TCATBALF sequentially, looks up disclosure group interest rates, calculates interest per category per account, creates interest transactions
- **Statement Generator (CBSTM03A):** Multi-format output (plain text + HTML), uses ALTER/GO TO statements, calls subroutine CBSTM03B for file I/O, exercises COMP/COMP-3 arithmetic and 2D arrays
- **Bill Payment (COBIL00C):** Online balance payment that creates a transaction record and updates account balance
- **Report (CORPT00C):** Submits batch JCL via internal reader (TDQ) from online CICS

**Data Coupling:** VERY HIGH. Transactions touch nearly every data store: TRANSACT, ACCTDAT, CARDXREF, TCATBALF, DISCGRP, TRANTYPE, TRANCATG, and GDGs.

#### Strategy Evaluation

| Strategy | Fit | Justification |
|---|---|---|
| **(a) Strangler** | Fair | The batch jobs cannot easily be incrementally strangled because they are tightly sequenced (CLOSEFIL -> POSTTRAN -> INTCALC -> COMBTRAN -> CREASTMT -> OPENFIL). |
| **(b) Replatform** | Fair | Could move batch to cloud COBOL runtime (e.g., Micro Focus on AWS). Preserves correctness of complex calculations. |
| **(c) Refactor** | Fair | Could modernize the ALTER/GO TO in CBSTM03A, but doesn't reduce platform lock-in. |
| **(d) Rewrite** | **RECOMMENDED (with parallel-run validation)** | The batch processing logic — while complex — is deterministic and file-based, making it ideal for parallel-run testing. Rewrite as Spring Batch jobs with JPA. The online transaction screens (list/view/add) are straightforward CRUD and can be rewritten as REST endpoints. Bill Payment becomes a service method. Interest calculation becomes a scheduled job. Statement generation becomes a reporting microservice. Parallel-run the old and new batch pipelines with reconciliation until parity is proven. |

**Recommended Strategy: REWRITE with parallel-run validation**

---

### 5. Reporting & Statement Generation

**Programs:** `CORPT00C` (649 LOC — online report request), `CBTRN03C` (649 LOC — batch report), `CBSTM03A` (924 LOC — statement printer), `CBSTM03B` (230 LOC — file I/O subroutine)

**Copybooks:** `COSTM01` (statement format), `CUSTREC` (customer for statements), `CVTRA05Y`, `CVACT01Y`, `CVACT03Y`

**Data Stores:** `TRANSACT`, `CARDXREF`, `TRANTYPE`, `TRANCATG` (all read-only), GDG output files, `STMTFILE`/`HTMLFILE` (statement output)

**Business Logic Complexity:** MEDIUM-HIGH. CBSTM03A is intentionally written with legacy patterns (ALTER/GO TO, mainframe control block addressing, CALL to subroutine) to exercise modernization tooling. CORPT00C submits batch jobs from CICS via internal reader (TDQ write), which is a mainframe-specific integration pattern.

**Data Coupling:** MEDIUM. Read-only access to transaction and reference data. Output is self-contained (report/statement files).

#### Strategy Evaluation

| Strategy | Fit | Justification |
|---|---|---|
| **(a) Strangler** | Fair | Could route report requests to a new service while keeping legacy running. |
| **(b) Replatform** | Poor | ALTER/GO TO and internal reader are platform-specific. Replatforming adds little value. |
| **(c) Refactor** | Poor | The programs exist specifically to exercise legacy patterns; refactoring defeats the purpose of the demo. |
| **(d) Rewrite** | **RECOMMENDED** | Replace with a modern reporting service (JasperReports or similar). The internal reader pattern (TDQ) is replaced by a simple REST endpoint that triggers a Spring Batch job. Statement output becomes PDF generation. The read-only data access makes this a safe rewrite target. |

**Recommended Strategy: REWRITE as a modern reporting service**

---

### 6. Data Migration & Branch Operations

**Programs:** `CBEXPORT` (582 LOC), `CBIMPORT` (487 LOC)

**Copybooks:** `CVEXPORT` (103 LOC — multi-record export layout with REDEFINES, COMP, COMP-3 fields, OCCURS)

**Data Stores:** All core VSAM files (read for export, write for import), sequential export file (500-byte records with record-type discrimination)

**Business Logic Complexity:** MEDIUM. Multi-record type handling with REDEFINES structure. Export reads all customer, account, card, transaction, and XREF files and packs them into a denormalized 500-byte export record. Import does the reverse with checksum validation. Uses COMP and COMP-3 packed decimal fields for storage optimization.

**Data Coupling:** VERY HIGH. These programs touch every VSAM file in the system.

#### Strategy Evaluation

| Strategy | Fit | Justification |
|---|---|---|
| **(a) Strangler** | Poor | This is a batch-only ETL process; strangling makes little sense. |
| **(b) Replatform** | Fair | Could run on cloud COBOL to preserve EBCDIC/COMP-3 handling, but this is a migration-era utility. |
| **(c) Refactor** | Poor | No benefit in restructuring a utility program. |
| **(d) Rewrite** | **RECOMMENDED** | Replace with a modern ETL pipeline (Spring Batch or Apache Spark). The EBCDIC-to-ASCII conversion and COMP-3 unpacking are handled by standard libraries. This is a migration-era utility that should be replaced with a cloud-native data pipeline as part of the cutover. |

**Recommended Strategy: REWRITE as cloud-native ETL pipeline**

---

### 7. Optional Modules

#### 7a. Credit Card Authorization (IMS + DB2 + MQ)

**Programs:** `COPAUA0C` (1,026 LOC), `COPAUS0C` (1,032 LOC), `COPAUS1C` (604 LOC), `COPAUS2C` (244 LOC), `CBPAUP0C` (386 LOC)

**Business Logic Complexity:** HIGH. MQ-triggered authorization processing. IMS hierarchical database reads/updates. DB2 inserts for audit. CICS BMS screens for pending authorization summary/details.

**Data Coupling:** HIGH. IMS database (DBPAUTP0), DB2 table (AUTHFRDS), MQ queue, VSAM files.

**Recommended Strategy: REWRITE.** IMS and MQ are the most infrastructure-heavy dependencies. Replace with: Spring Boot + JPA for persistence, Spring Integration or Apache Kafka for async messaging, REST APIs for authorization requests. The IMS hierarchical model maps to a relational schema with parent-child joins.

#### 7b. Transaction Type Management (DB2)

**Programs:** `COTRTLIC` (2,098 LOC), `COTRTUPC` (1,702 LOC), `COBTUPDT` (237 LOC)

**Business Logic Complexity:** MEDIUM. DB2 cursor-based list/paginate, SQL INSERT/UPDATE/DELETE. Standard CRUD with DB2 instead of VSAM.

**Recommended Strategy: REWRITE.** Already uses SQL, making translation to JPA/Spring Data straightforward. The DB2 SQL operations map directly to JPA repository methods.

#### 7c. Account Extraction (MQ + VSAM)

**Programs:** `COACCT01` (620 LOC), `CODATE01` (524 LOC)

**Business Logic Complexity:** LOW-MEDIUM. MQ request/response patterns for date inquiry and account details.

**Recommended Strategy: REWRITE.** Replace MQ with REST or Kafka messaging. Simple request/response patterns.

---

## Strategy Summary Matrix

| Functional Area | Strategy | Risk | Effort | Priority |
|---|---|---|---|---|
| Authentication & User Mgmt | Rewrite | Low | Low | P1 (first) |
| Account Management | Strangler | Medium | High | P2 |
| Credit Card Management | Strangler | Medium | Medium | P2 (with Account) |
| Transaction Processing | Rewrite + parallel-run | High | Very High | P3 |
| Reporting & Statements | Rewrite | Medium | Medium | P3 (with Transactions) |
| Data Migration/Branch Ops | Rewrite | Low | Medium | P4 (cutover utility) |
| Authorization (IMS/DB2/MQ) | Rewrite | High | High | P4 (optional) |
| Transaction Types (DB2) | Rewrite | Low | Low | P2 |
| Account Extraction (MQ) | Rewrite | Low | Low | P4 (optional) |

---

## Technology Target Stack

| Layer | Technology |
|---|---|
| Language | Java 21 / Kotlin |
| Framework | Spring Boot 3.x |
| Persistence | Spring Data JPA + PostgreSQL (replaces VSAM/DB2) |
| Batch Processing | Spring Batch (replaces JCL/Control-M) |
| Messaging | Apache Kafka or Spring Integration (replaces MQ) |
| Security | Spring Security + JWT (replaces RACF/USRSEC) |
| API | REST + OpenAPI 3.0 (replaces BMS/3270) |
| Reporting | JasperReports or Spring + PDF generation |
| Scheduling | Spring Scheduler or Kubernetes CronJobs (replaces Control-M) |
| Data Migration | Spring Batch + custom EBCDIC/COMP-3 readers |
| CI/CD | GitHub Actions |
| Containerization | Docker + Kubernetes |
