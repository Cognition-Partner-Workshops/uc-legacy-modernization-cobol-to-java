# CardDemo Modernization Blueprint

## Executive Summary

This blueprint evaluates four modernization strategies — **Strangler Fig**, **Replatform**, **Refactor**, and **Rewrite** — for each functional area of the AWS CardDemo mainframe application. CardDemo is a COBOL/CICS/VSAM credit card management system comprising 31 online and batch programs, 30 copybooks, 17 BMS screen maps, and 38 JCL batch jobs. The application manages credit card accounts, transactions, customer data, billing, reporting, and user administration across two user roles (Regular and Admin).

---

## Inventory Summary

| Asset Type | Count | Key Examples |
|---|---|---|
| Online CICS Programs | 17 | COSGN00C, COMEN01C, COACTVWC, COBIL00C |
| Batch COBOL Programs | 14 | CBTRN02C, CBACT04C, CBSTM03A/B, CBTRN03C |
| Copybooks | 30 | CVACT01Y, CVACT02Y, CVCUS01Y, CVTRA05Y |
| BMS Screen Maps | 17 | COSGN00, COMEN01, COACTUP, COBIL00 |
| JCL Batch Jobs | 38 | POSTTRAN, INTCALC, CREASTMT, COMBTRAN |
| VSAM Data Files | 7+ | USRSEC, ACCTDAT, CARDDAT, CUSTDAT, TRANSACT, CARDXREF, TCATBAL |
| Optional Modules | 3 | IMS/DB2/MQ Authorization, DB2 Transaction Types, VSAM-MQ |

---

## Functional Area Analysis

### 1. Authentication & Session Management

**Programs:** COSGN00C (Signon), COADM01C (Admin Menu), COMEN01C (User Menu)
**Data:** USRSEC VSAM file (SEC-USER-DATA: user ID, password, type)
**Screens:** COSGN00, COADM01, COMEN01
**Complexity:** Low (261 lines signon, simple password comparison, role-based routing)

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | **Recommended** | Wrap existing auth with a modern identity provider (e.g., Spring Security + JWT). New services authenticate against the new IdP while legacy CICS programs continue to use USRSEC during transition. |
| Replatform | Moderate | Lift VSAM USRSEC to a relational table; run COBOL on a managed runtime. Preserves weak password model. |
| Refactor | Low | The plaintext password comparison (SEC-USR-PWD = WS-USER-PWD) and 8-char limits make refactoring the existing logic counterproductive. |
| Rewrite | High | Small codebase, well-understood logic, and modern security requirements (hashing, MFA, token-based auth) justify a clean rewrite in Java/Spring Security. |

**Recommendation:** **Strangler Fig** into a new Spring Security module. Deploy early as the gateway for all subsequent migrated services. The existing USRSEC data can be migrated to a `users` table with bcrypt-hashed passwords.

---

### 2. Account Management

**Programs:** COACTVWC (Account View, 942 lines), COACTUPC (Account Update, 4237 lines)
**Data:** ACCTDAT VSAM file (ACCOUNT-RECORD: 300 bytes — ID, status, balances, credit limits, dates, group ID)
**Copybooks:** CVACT01Y, COCOM01Y
**Screens:** COACTVW, COACTUP
**Complexity:** High (COACTUPC is the largest program — extensive field-level validation, date editing, SSN/phone validation, multi-file reads)

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | **Recommended** | Expose account view/update as REST APIs behind an API gateway. Route traffic gradually from CICS to the new service. The existing COMMAREA-based navigation can be preserved during transition. |
| Replatform | Moderate | Move ACCTDAT to a relational table; recompile COBOL for a managed runtime. Preserves the monolithic validation logic. |
| Refactor | Low | COACTUPC's 4237 lines of tightly coupled validation, screen I/O, and file access resist incremental refactoring without a clear domain model. |
| Rewrite | High | The complex validation logic in COACTUPC (dates, FICO scores, SSN, phone numbers, credit limits) maps naturally to Java Bean Validation annotations and a clean service layer. |

**Recommendation:** **Strangler Fig** — build a Java `AccountService` with JPA entities mirroring CVACT01Y. Introduce a REST API for account CRUD. During transition, dual-write to both VSAM and the new database.

---

### 3. Card Management

**Programs:** COCRDLIC (Card List, 1460 lines), COCRDSLC (Card View), COCRDUPC (Card Update)
**Data:** CARDDAT VSAM (CARD-RECORD: 150 bytes — card number, account ID, CVV, embossed name, expiration, status), CARDAIX (alternate index by account)
**Copybooks:** CVACT02Y, CVCRD01Y
**Screens:** COCRDLI, COCRDSL, COCRDUP
**Complexity:** Medium-High (paginated list with select/update routing, alternate index browsing, multi-program navigation via COMMAREA)

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | **Recommended** | Card list/view/update map cleanly to REST endpoints. The paginated browse (STARTBR/READNEXT/ENDBR pattern) translates to Spring Data pagination. |
| Replatform | Moderate | VSAM CARDDAT migrates to a `cards` table. Alternate index (CARDAIX) becomes a database index. |
| Refactor | Moderate | The three programs follow a consistent pattern and share copybooks, making them amenable to incremental extraction. |
| Rewrite | High | Card management is a well-bounded domain; rewriting provides cleaner separation from account management. |

**Recommendation:** **Strangler Fig** — extract as a `CardService` with endpoints for list (paginated), view, and update. CARDDAT and CARDAIX map to a `cards` table with indexes on `card_num` and `acct_id`.

---

### 4. Transaction Processing (Online)

**Programs:** COTRN00C (Transaction List, 700 lines), COTRN01C (Transaction View), COTRN02C (Transaction Add)
**Data:** TRANSACT VSAM (TRAN-RECORD: 350 bytes — ID, type, category, source, description, amount, merchant info, card number, timestamps)
**Copybooks:** CVTRA05Y
**Screens:** COTRN00, COTRN01, COTRN02
**Complexity:** Medium (paginated list with forward/backward browsing, selection routing, transaction creation with auto-generated IDs via HIGH-VALUES STARTBR)

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | **Recommended** | Transaction list/view/add are standard CRUD operations. The ID generation pattern (read last key, increment) should be replaced with database sequences. |
| Replatform | Moderate | TRANSACT VSAM becomes a `transactions` table. The READNEXT/READPREV pagination maps to SQL OFFSET/LIMIT. |
| Refactor | Moderate | Clean program boundaries; however, the tight coupling to VSAM browse operations limits in-place refactoring. |
| Rewrite | High | Modern transaction processing benefits from event sourcing, audit trails, and database-backed concurrency control that VSAM cannot provide. |

**Recommendation:** **Strangler Fig** — build a `TransactionService` with paginated list, detail view, and creation endpoints. Replace the manual ID generation with database sequences or UUIDs.

---

### 5. Transaction Processing (Batch)

**Programs:** CBTRN02C (Transaction Posting, 732 lines), CBACT04C (Interest Calculation, 653 lines), CBTRN03C (Transaction Report)
**Data:** DALYTRAN (daily transactions), TRANSACT, XREFFILE, ACCTDAT, TCATBAL (transaction category balances), DISCGRP (discount groups)
**JCL:** POSTTRAN, INTCALC, COMBTRAN, TRANBKP, TRANIDX
**Complexity:** High (multi-file processing with validation, rejection handling, cross-reference lookups, interest computation with discount group rates, account balance updates)

| Strategy | Fit | Rationale |
|---|---|---|
| Strangler Fig | Low | Batch jobs run as a complete cycle (CLOSEFIL -> refresh -> POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT -> TRANIDX -> OPENFIL). Partial strangling risks data inconsistency. |
| **Replatform** | **Recommended** | Convert to Spring Batch jobs reading from the new relational database. Preserve the sequential processing logic and validation rules. JCL step dependencies map to Spring Batch job flows. |
| Refactor | Moderate | Extract validation (1500-VALIDATE-TRAN) and posting (2000-POST-TRANSACTION) as reusable components. |
| Rewrite | High | Enables modern scheduling (Quartz/Spring Scheduler), parallel processing, and elimination of the CLOSEFIL/OPENFIL CICS file synchronization. |

**Recommendation:** **Replatform** to Spring Batch. The batch cycle order is critical: preserve the dependency chain while replacing VSAM file I/O with JPA repositories. The reject file (DALYREJS) becomes a database table for audit/retry.

---

### 6. Bill Payment

**Programs:** COBIL00C (573 lines)
**Data:** TRANSACT, ACCTDAT, CXACAIX (card cross-reference alternate index)
**Screens:** COBIL00
**Complexity:** Medium (account lookup, balance check, confirmation flow, transaction creation, account balance update — all within a single CICS program)

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | **Recommended** | Bill payment is a self-contained workflow: validate account -> confirm -> create transaction -> update balance. Clean extraction seam. |
| Replatform | Moderate | The CICS READ UPDATE / REWRITE pattern maps to database transactions with row-level locking. |
| Refactor | Low | The program mixes UI, validation, and data access; refactoring in COBOL yields limited benefit. |
| Rewrite | High | Modern payment processing needs idempotency, retry logic, and event-driven notifications that justify a rewrite. |

**Recommendation:** **Strangler Fig** — extract as a `BillPaymentService`. The confirmation flow (Y/N prompt) becomes a two-step API (preview + confirm). The atomic read-update of ACCTDAT maps to a database transaction with optimistic locking.

---

### 7. Reporting & Statements

**Programs:** CORPT00C (Report Submission, 650 lines), CBSTM03A (Statement Generation, 924 lines), CBSTM03B (File I/O Subroutine, 230 lines), CBTRN03C (Transaction Report)
**Data:** TRANSACT, XREFFILE, CUSTDAT, ACCTDAT
**JCL:** CREASTMT, TRANREPT, TXT2PDF1
**Complexity:** High (CBSTM03A uses ALTER/GO TO, mainframe control block addressing via PSA/TCB/TIOT pointers, COMP/COMP-3 variables, 2D arrays, subroutine calls, dual-format output in plain text and HTML)

| Strategy | Fit | Rationale |
|---|---|---|
| Strangler Fig | Low | CORPT00C submits JCL via CICS extra-partition TDQ (transient data queue). This tight CICS/JES coupling makes incremental strangling impractical. |
| Replatform | Low | CBSTM03A's use of PSA/TCB/TIOT control block addressing and ALTER statements is inherently non-portable. |
| Refactor | Low | The ALTER/GO TO patterns and mainframe control block access in CBSTM03A resist automated refactoring tools. |
| **Rewrite** | **Recommended** | Modern reporting (JasperReports, Apache POI, or HTML templating) is more maintainable. The statement layout in CBSTM03A (lines 86-224) provides a complete specification for the new implementation. |

**Recommendation:** **Rewrite** in Java. Replace the online report submission (CORPT00C's TDQ-to-JES flow) with a Spring Batch job triggered via REST API. Statement generation (CBSTM03A/B) becomes a reporting service using a templating engine. This is the highest-risk area due to CBSTM03A's non-standard constructs.

---

### 8. User Administration (Admin Only)

**Programs:** COUSR00C (User List, 696 lines), COUSR01C (User Add), COUSR02C (User Update), COUSR03C (User Delete)
**Data:** USRSEC VSAM file
**Screens:** COUSR00, COUSR01, COUSR02, COUSR03
**Complexity:** Medium (standard CRUD with paginated list, same patterns as transaction list)

| Strategy | Fit | Rationale |
|---|---|---|
| **Rewrite** | **Recommended** | User administration should be rebuilt as part of the new identity/access management system. The existing USRSEC structure (8-char IDs, plaintext passwords, single-char type) is inadequate for modern requirements. |
| Strangler Fig | High | Could wrap behind an admin API, but the data model needs fundamental changes. |
| Replatform | Low | The security model is too primitive to preserve. |
| Refactor | Low | Same reasoning — the data model must change. |

**Recommendation:** **Rewrite** as part of the new IAM module. Implement role-based access control (RBAC), password policies, audit logging, and self-service capabilities that the current USRSEC model cannot support.

---

### 9. Data Import/Export & Utilities

**Programs:** CBEXPORT (data export), CBIMPORT (data import), COBSWAIT (wait utility), CSUTLDTC (date validation utility)
**JCL:** CBEXPORT, CBIMPORT, DEFGDGB, DEFGDGD, ESDSRRDS, and various file-definition JCL
**Complexity:** Low-Medium

| Strategy | Fit | Rationale |
|---|---|---|
| Strangler Fig | Low | These are infrastructure utilities, not business logic. |
| **Replatform** | **Recommended** | Export/import become database ETL jobs. Date validation (CSUTLDTC) becomes a Java utility class. GDG definitions become file versioning or database snapshots. |
| Refactor | Low | Minimal business value in refactoring utilities. |
| Rewrite | Moderate | Simple enough to rewrite but not a priority. |

**Recommendation:** **Replatform** — replace with Spring Batch import/export jobs and Java utility classes. These should be migrated as part of the batch processing infrastructure.

---

### 10. Optional Modules (IMS/DB2/MQ)

**Programs:** COPAUA0C, COPAUS0C/1C/2C, CBPAUP0C (Authorization), COTRTUPC, COTRTLIC, COBTUPDT (Transaction Type DB2), CDRD/CODATE01, CDRA/COACCT01 (VSAM-MQ)
**Data:** IMS DB, DB2 tables, MQ queues
**Complexity:** High (multi-middleware integration)

| Strategy | Fit | Rationale |
|---|---|---|
| **Rewrite** | **Recommended** | IMS DB -> JPA, DB2 embedded SQL -> JPA/JPQL, MQ -> Spring JMS or Kafka. The middleware dependencies (IMS, MQ, DB2) are the primary reason to rewrite rather than replatform. |
| Strangler Fig | Moderate | Could introduce an API gateway in front of MQ interfaces. |
| Replatform | Low | IMS DB is not available on modern platforms; DB2 could be preserved but MQ topology changes. |
| Refactor | Low | Middleware dependencies prevent meaningful in-place refactoring. |

**Recommendation:** **Rewrite** — these modules demonstrate integration patterns that should be rebuilt using modern middleware (REST APIs, message brokers, relational DB).

---

## Strategy Summary Matrix

| Functional Area | Recommended Strategy | Complexity | Priority |
|---|---|---|---|
| Authentication & Session | Strangler Fig | Low | P0 (prerequisite) |
| Account Management | Strangler Fig | High | P1 |
| Card Management | Strangler Fig | Medium-High | P1 |
| Transaction Processing (Online) | Strangler Fig | Medium | P1 |
| Transaction Processing (Batch) | Replatform | High | P2 |
| Bill Payment | Strangler Fig | Medium | P2 |
| Reporting & Statements | Rewrite | High | P3 |
| User Administration | Rewrite | Medium | P1 |
| Data Import/Export & Utilities | Replatform | Low-Medium | P3 |
| Optional Modules (IMS/DB2/MQ) | Rewrite | High | P3 |

---

## Technology Target Stack

| Legacy Component | Target Technology |
|---|---|
| COBOL Programs | Java 17+ / Spring Boot |
| CICS Transactions | REST APIs (Spring MVC) |
| BMS Screen Maps | React/Angular SPA or Thymeleaf |
| VSAM KSDS Files | PostgreSQL / Oracle RDBMS |
| Copybooks (Record Layouts) | JPA Entities / DTOs |
| JCL Batch Jobs | Spring Batch |
| COMMAREA | HTTP Session / JWT Claims |
| CICS STARTBR/READNEXT | Spring Data JPA Pagination |
| CICS READ UPDATE/REWRITE | @Transactional with Optimistic Locking |
| Extra-partition TDQ | Message Queue (RabbitMQ/Kafka) |
| IMS DB | JPA with relational DB |
| MQ Series | Spring JMS / Kafka |
| GDG (Generation Data Groups) | Database snapshots / file versioning |
