# CardDemo Modernization Blueprint

## Executive Summary

This blueprint evaluates four modernization strategies — **Strangler Fig**, **Replatform**, **Refactor**, and **Rewrite** — for each functional area of the AWS CardDemo mainframe application. CardDemo is a COBOL/CICS/VSAM credit card management system comprising 31 online and batch programs, 30 copybooks, 17 BMS screen maps, and 38 JCL batch jobs. The application manages accounts, cards, transactions, bill payments, reporting, user security, and interest calculation.

---

## System Inventory

| Metric | Count |
|---|---|
| Online CICS programs | 17 |
| Batch COBOL programs | 8 |
| Copybooks (data structures) | 30 |
| BMS screen maps (3270 UI) | 17 |
| JCL batch jobs | 38 |
| VSAM data files | 7+ |
| CICS transactions | 12 |
| Optional extension modules | 3 (IMS/DB2/MQ) |
| Total COBOL LOC (approx.) | ~15,000 |

---

## Functional Areas

### 1. Authentication & User Security

**Programs:** `COSGN00C` (signon), `COUSR00C` (user list), `COUSR01C` (user add), `COUSR02C` (user update), `COUSR03C` (user delete)
**Data:** USRSEC VSAM file, `CSUSR01Y` copybook (80-byte user record: ID, name, password, type)
**BMS Maps:** `COSGN00`, `COUSR00`–`COUSR03`
**Complexity:** Low–Medium. Simple CRUD operations on a flat VSAM file. Plaintext password storage.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | Good | Auth can be extracted early behind an API gateway; legacy signon redirected to new service |
| **Replatform** | Poor | Plaintext passwords and flat-file security model have no modern equivalent worth preserving |
| **Refactor** | Fair | Structure is simple enough to refactor, but security model needs redesign regardless |
| **Rewrite** | **Recommended** | Security must be redesigned (hashed passwords, RBAC, JWT/OAuth). Clean-room rewrite is safest |

**Recommendation:** **Rewrite** as a Spring Security / Spring Boot authentication microservice with BCrypt password hashing, JWT tokens, and role-based access control. The current plaintext password model is a liability that cannot be preserved.

---

### 2. Account Management

**Programs:** `COACTVWC` (account view, 942 LOC), `COACTUPC` (account update, 4,237 LOC — the largest program)
**Data:** ACCTDAT VSAM file, `CVACT01Y` copybook (300-byte record: balance, credit limits, dates, cycle credits/debits, group ID)
**Dependencies:** Card cross-reference (`CVACT03Y`), Customer master (`CVCUS01Y`), Card data (`CVACT02Y`)
**BMS Maps:** `COACTVW`, `COACTUP`
**Complexity:** High. `COACTUPC` is 4,237 lines with extensive field-level validation (dates, SSN, phone numbers, FICO scores, credit limits), multi-file reads (account + customer + card + xref), and complex update logic.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | **Recommended** | Expose account read/write behind REST APIs; route new consumers to Java while legacy CICS still serves existing 3270 users during transition |
| **Replatform** | Fair | Automated translation could handle the volume, but 4,237-line monolithic program needs restructuring |
| **Refactor** | Good | Extract validation logic into reusable services; split view and update into separate bounded operations |
| **Rewrite** | Fair | High LOC count increases rewrite risk; business rules embedded in 4K+ lines are easy to miss |

**Recommendation:** **Strangler Fig** with incremental extraction. Start by exposing read-only account view as a REST API (lower risk), then progressively migrate update logic. The validation rules in `COACTUPC` should be extracted into a dedicated validation service during migration.

---

### 3. Card Management

**Programs:** `COCRDLIC` (card list, 1,460 LOC), `COCRDSLC` (card detail view), `COCRDUPC` (card update)
**Data:** CARDDAT VSAM file, CARDAIX alternate index, `CVACT02Y` copybook (150-byte card record: number, account ID, CVV, embossed name, expiration, status)
**Dependencies:** Card cross-reference (`CVACT03Y`), Account master for account-path browsing
**BMS Maps:** `COCRDLI`, `COCRDSL`, `COCRDUP`
**Complexity:** Medium. Paginated list with browse/filter, detail view, and update. Uses alternate index for account-based card lookup.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | **Recommended** | Card list/view are read-heavy and can be fronted by a new API early; update follows |
| **Replatform** | Fair | Straightforward VSAM-to-RDBMS mapping, but CICS browse semantics need redesign |
| **Refactor** | Good | Clean separation of list/view/update maps well to REST resources |
| **Rewrite** | Fair | Moderate complexity; rewrite is viable but strangler approach is lower risk |

**Recommendation:** **Strangler Fig**. Card management has clear read/write separation and maps naturally to RESTful resources (`GET /cards`, `GET /cards/{id}`, `PUT /cards/{id}`). Migrate read operations first, then writes.

---

### 4. Transaction Processing (Online)

**Programs:** `COTRN00C` (transaction list, 700 LOC), `COTRN01C` (transaction view), `COTRN02C` (transaction add)
**Data:** TRANSACT VSAM file, `CVTRA05Y` copybook (350-byte record: ID, type, category, amount, merchant info, card number, timestamps)
**Dependencies:** Account master for balance updates, Card cross-reference for card validation
**BMS Maps:** `COTRN00`, `COTRN01`, `COTRN02`
**Complexity:** Medium. List with pagination, view, and add. Transaction add creates records in TRANSACT and updates ACCTDAT balance.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | **Recommended** | Transaction list/view are read-only and safe to extract first; add involves cross-file writes requiring careful orchestration |
| **Replatform** | Fair | Transaction records map cleanly to relational tables |
| **Refactor** | Good | Clear separation of read vs. write operations |
| **Rewrite** | Fair | Business logic is moderate; rewrite is feasible but riskier for the write path |

**Recommendation:** **Strangler Fig**. Extract read operations first (list/view). Transaction add requires coordinated writes to TRANSACT and ACCTDAT — implement this as a transactional service with proper ACID guarantees (Spring `@Transactional`).

---

### 5. Transaction Processing (Batch)

**Programs:** `CBTRN02C` (daily transaction posting, 732 LOC), `CBTRN01C`, `CBTRN03C` (transaction report)
**Data:** DALYTRAN (daily input), TRANSACT (master), DALYREJS (rejects), ACCTDAT (account), TCATBALF (category balances), XREFFILE (cross-reference)
**JCL:** `POSTTRAN.jcl`, `COMBTRAN.jcl`, `TRANFILE.jcl`, `TRANBKP.jcl`, `TRANIDX.jcl`
**Complexity:** High. `CBTRN02C` is the core batch posting engine: reads daily transactions, validates against cross-reference and account files, posts to transaction master, updates account balances and category balances, writes rejects. Multi-file coordinated I/O with validation and error handling.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | Fair | Batch jobs are harder to strangle incrementally; they run as atomic cycles |
| **Replatform** | Good | JCL-to-scheduler mapping is well-understood; VSAM-to-RDBMS conversion is straightforward |
| **Refactor** | **Recommended** | Convert to Spring Batch with chunk-oriented processing; preserve validation and posting logic as Java steps |
| **Rewrite** | Fair | Batch logic is well-structured and can be directly translated |

**Recommendation:** **Refactor** into Spring Batch jobs. The sequential read-validate-post-reject pattern maps directly to Spring Batch's chunk-oriented architecture (ItemReader → ItemProcessor → ItemWriter). Preserve the validation rules from `1500-VALIDATE-TRAN` as a processor step. The JCL batch cycle (CLOSEFIL → data refresh → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → OPENFIL) becomes a Spring Batch job flow with step dependencies.

---

### 6. Interest Calculation (Batch)

**Programs:** `CBACT04C` (interest calculator, 653 LOC)
**Data:** TCATBALF (category balances), XREFFILE, DISCGRP (disclosure/interest rates), ACCTDAT, TRANSACT (output)
**JCL:** `INTCALC.jcl`, `DISCGRP.jcl`
**Complexity:** High. Reads transaction category balances, looks up disclosure group interest rates, computes monthly interest per category, aggregates per account, updates account balances, and generates interest transaction records.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | Poor | Tightly coupled to batch cycle; cannot run independently during transition |
| **Replatform** | Fair | Calculation logic is pure business rules that translate directly |
| **Refactor** | **Recommended** | Convert to Spring Batch job; extract interest rate lookup and computation as testable service classes |
| **Rewrite** | Good | Calculation logic is self-contained and well-defined |

**Recommendation:** **Refactor** into a Spring Batch job with a dedicated `InterestCalculationService`. The disclosure group lookup (`DISCGRP` file → `DIS-INT-RATE`) becomes a database-backed rate table. This is a high-value target for unit testing — the pure calculation logic can be thoroughly tested once extracted from file I/O.

---

### 7. Bill Payment

**Programs:** `COBIL00C` (bill payment, 573 LOC)
**Data:** TRANSACT (write), ACCTDAT (read/update), CXACAIX (card cross-reference alternate index)
**BMS Maps:** `COBIL00`
**Complexity:** Medium. Reads account balance, creates a payment transaction record (type '02', category 2), deducts from account balance. Cross-file writes with confirmation flow.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | Good | Payment can be exposed as an API endpoint while legacy screen remains active |
| **Replatform** | Fair | Logic is straightforward but involves multi-file updates |
| **Refactor** | **Recommended** | Clean extraction into a PaymentService with transactional integrity |
| **Rewrite** | Good | Simple enough for clean-room implementation |

**Recommendation:** **Refactor**. Extract as a `BillPaymentService` with `@Transactional` semantics. The payment flow (read account → validate balance → create transaction → update balance) maps to a single service method. This is a good early candidate because the business logic is self-contained and the confirmation flow (Y/N) maps to a simple API request.

---

### 8. Reporting

**Programs:** `CORPT00C` (report submission, 650 LOC), `CBSTM03A`/`CBSTM03B` (statement generation), `CBTRN03C` (transaction report)
**Data:** TRANSACT (read), JCL submission via CICS TDQ (extra-partition transient data queue)
**JCL:** `CREASTMT.JCL`, `TRANREPT.jcl`, `REPTFILE.jcl`
**Complexity:** Medium. Online report request screen with date range validation submits JCL to internal reader. Batch statement generation reads transactions and produces formatted output.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | Good | Reports can be generated by new system while legacy batch continues |
| **Replatform** | **Recommended** | Report generation maps directly to modern reporting tools (JasperReports, database queries) |
| **Refactor** | Fair | JCL submission via TDQ is a mainframe-specific pattern that doesn't refactor cleanly |
| **Rewrite** | Good | Report logic is well-defined; modern tools offer better output formats (PDF, CSV, dashboards) |

**Recommendation:** **Replatform** to modern reporting. Replace JCL-submitted batch reports with Spring Batch report jobs or a dedicated reporting service. The TDQ-based JCL submission pattern (`CORPT00C` writes JCL cards to an internal reader queue) is fundamentally mainframe-specific and should be replaced with a job scheduling mechanism (Spring Batch, Quartz, or cloud-native scheduler).

---

### 9. Menu & Navigation Framework

**Programs:** `COMEN01C` (user main menu), `COADM01C` (admin menu)
**Data:** `COMEN02Y` (11 menu options), `COADM02Y` (6 admin menu options)
**BMS Maps:** `COMEN01`, `COADM01`
**Complexity:** Low. Menu-driven navigation using CICS XCTL (transfer control). Menus are data-driven via copybook arrays.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | N/A | Navigation framework is replaced entirely by web UI |
| **Replatform** | Poor | 3270 menu paradigm has no modern equivalent |
| **Refactor** | Poor | CICS XCTL navigation doesn't map to web frameworks |
| **Rewrite** | **Recommended** | Replace with modern web UI (React/Angular) with REST API backend |

**Recommendation:** **Rewrite** as a modern web application. The 3270 terminal menu paradigm (BMS maps, CICS SEND/RECEIVE, XCTL program transfer) is completely replaced by a web frontend with API-driven navigation. This is not code migration — it is UI/UX redesign.

---

### 10. Data Export/Import & Utilities

**Programs:** `CBEXPORT` (data export), `CBIMPORT` (data import), `CSUTLDTC` (date validation utility), `COBSWAIT` (wait utility)
**Data:** All VSAM files (export/import), `CSUTLDWY`/`CSUTLDPY` copybooks
**JCL:** `CBEXPORT.jcl`, `CBIMPORT.jcl`
**Complexity:** Low. Bulk read/write operations on VSAM files. Date utility is a reusable validation function.

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | N/A | Utility functions are consumed, not strangled |
| **Replatform** | **Recommended** | Export/import become database ETL scripts or Spring Batch jobs |
| **Refactor** | Fair | Date utility becomes a Java utility class |
| **Rewrite** | Good | Simple enough to rewrite cleanly |

**Recommendation:** **Replatform**. Export/import become standard database migration scripts or ETL jobs. The `CSUTLDTC` date validation utility becomes a `DateValidationUtil` Java class. These are supporting functions that migrate naturally as their consumers are modernized.

---

### 11. Optional Modules (IMS/DB2/MQ)

**Modules:**
- **Authorization** (`app-authorization-ims-db2-mq/`): 8 programs, IMS DB + DB2 + MQ integration for fraud authorization
- **Transaction Type DB2** (`app-transaction-type-db2/`): 3 programs, DB2 CRUD for transaction types
- **VSAM-MQ** (`app-vsam-mq/`): 2 programs, MQ request/response for date and account inquiry

**Complexity:** Medium–High. These modules demonstrate enterprise integration patterns (messaging, relational DB, hierarchical DB).

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | Good | MQ-based modules already have message-based interfaces that can be fronted |
| **Replatform** | Good | DB2 SQL translates directly to JPA/JDBC; MQ maps to JMS/Spring AMQP |
| **Refactor** | **Recommended** | Preserve integration patterns but modernize middleware (MQ→Kafka/RabbitMQ, IMS→JPA, DB2→PostgreSQL) |
| **Rewrite** | Fair | Integration logic is valuable to preserve |

**Recommendation:** **Refactor**. The DB2 programs already use SQL, which translates directly to JPA repositories. MQ-based authorization becomes an event-driven microservice (Spring Cloud Stream / Kafka). IMS hierarchical data is redesigned as relational tables. These modules are optional and can be migrated last.

---

## Strategy Summary Matrix

| Functional Area | Recommended Strategy | Complexity | Priority |
|---|---|---|---|
| Authentication & Security | Rewrite | Low | P1 (Foundation) |
| Account Management | Strangler Fig | High | P2 |
| Card Management | Strangler Fig | Medium | P2 |
| Transaction Processing (Online) | Strangler Fig | Medium | P3 |
| Transaction Processing (Batch) | Refactor | High | P3 |
| Interest Calculation | Refactor | High | P4 |
| Bill Payment | Refactor | Medium | P2 |
| Reporting | Replatform | Medium | P4 |
| Menu & Navigation | Rewrite | Low | P1 (Foundation) |
| Data Export/Import & Utilities | Replatform | Low | P5 |
| Optional Modules (IMS/DB2/MQ) | Refactor | Medium–High | P5 |

---

## Technology Target State

| Legacy Component | Target Technology |
|---|---|
| COBOL programs | Java 17+ / Spring Boot 3.x |
| CICS transactions | REST APIs (Spring MVC / Spring WebFlux) |
| BMS 3270 screens | React or Angular SPA |
| VSAM KSDS files | PostgreSQL / Amazon RDS |
| Copybook record layouts | JPA Entities / DTOs |
| JCL batch jobs | Spring Batch |
| CICS COMMAREA | HTTP session / JWT tokens |
| CICS XCTL/LINK | REST API calls / method invocations |
| MQ messaging | Apache Kafka / RabbitMQ / Amazon SQS |
| IMS DB | PostgreSQL (relational redesign) |
| DB2 | PostgreSQL (direct SQL migration) |
| Job scheduling (CA7/Control-M) | Spring Batch + Quartz / AWS Step Functions |
