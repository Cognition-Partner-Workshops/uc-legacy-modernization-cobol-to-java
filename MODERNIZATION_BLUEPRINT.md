# Modernization Blueprint — CardDemo COBOL Estate

> Strategy evaluation and recommendation for each major functional area of the CardDemo credit card management system.

---

## Table of Contents

1. [Strategy Definitions](#strategy-definitions)
2. [Evaluation Criteria](#evaluation-criteria)
3. [Account Management (CBACT\*, COACTUPC, COACTVWC)](#1-account-management)
4. [Transaction Processing (CBTRN\*, COTRN\*)](#2-transaction-processing)
5. [Customer Management (CBCUS\*, CBSTM03A/B)](#3-customer-management)
6. [Statement & Reporting (CBSTM\*, CORPT\*, CBTRN03C)](#4-statement--reporting)
7. [User Security (COUSR\*, COSGN00C)](#5-user-security)
8. [Card Management (COCRD\*, COBIL00C)](#6-card-management)
9. [Sub-App: Authorization IMS-DB2-MQ](#7-sub-app-authorization-ims-db2-mq)
10. [Sub-App: Transaction Type DB2](#8-sub-app-transaction-type-db2)
11. [Sub-App: VSAM-MQ](#9-sub-app-vsam-mq)
12. [Navigation & Menu (COMEN01C, COADM01C)](#10-navigation--menu)
13. [Utility Programs (CBEXPORT, CBIMPORT, CSUTLDTC, COBSWAIT)](#11-utility-programs)
14. [Strategy Summary Matrix](#strategy-summary-matrix)

---

## Strategy Definitions

| Strategy | Description | Best When |
|----------|-------------|-----------|
| **(a) Strangler Pattern** | Wrap existing COBOL programs with modern APIs (REST/gRPC). Route traffic through a facade. Incrementally replace back-end implementations while the API contract remains stable. | Business logic is stable, uptime requirements are high, and a gradual migration with zero-downtime is needed. |
| **(b) Replatform** | Keep the COBOL source code largely unchanged but move execution from the mainframe to a cloud-hosted COBOL runtime (e.g., Micro Focus, AWS Mainframe Modernization). | Time-to-cloud is critical, budget is limited, and the COBOL code is well-structured and maintainable. |
| **(c) Refactor** | Restructure the COBOL code for better maintainability — modularize monolithic paragraphs, extract copybook logic, improve naming — without rewriting in another language. | COBOL skills are available, the programs work correctly, and the goal is to extend the system's life rather than replace it. |
| **(d) Rewrite** | Translate the COBOL programs to Java (Spring Boot / Spring Batch), replacing VSAM with RDBMS (PostgreSQL), CICS with REST APIs, and BMS maps with a modern UI framework. | Long-term strategic investment, COBOL skills are declining, and the target architecture is Java/cloud-native. |

---

## Evaluation Criteria

Each functional area is evaluated on four axes:

| Criterion | Description |
|-----------|-------------|
| **Business Logic Complexity** | Volume and density of decision points (IF/EVALUATE), validation rules, and calculation logic. Higher complexity increases rewrite risk but also increases the long-term benefit of rewriting. |
| **Data Coupling** | Number of VSAM files, DB2 tables, copybooks, and cross-program data flows. Tighter coupling means harder extraction and higher risk of regressions. |
| **Team Skill Availability** | Assumes the modernization team has strong Java/Spring skills and moderate COBOL reading ability. Pure COBOL maintenance requires scarce COBOL expertise. |
| **Risk Tolerance** | Operational impact of failure. Financial calculations and daily batch processing require lower risk tolerance; administrative screens tolerate more risk. |

---

## 1. Account Management

**Programs:** CBACT01C (430 LOC), CBACT02C (178 LOC), CBACT03C (178 LOC), CBACT04C (652 LOC), COACTUPC (4,236 LOC), COACTVWC (941 LOC)

**Function:** Core account lifecycle — batch file readers, interest calculation, online CRUD with extensive field validation.

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **(a) Strangler** | **Medium** | COACTUPC's 188 decision points make it risky to replace all at once. A strangler facade could expose account read/update as REST APIs while COBOL continues executing behind it. However, the tight CICS coupling (EXEC CICS READ/REWRITE on ACCTDAT, CCXREF, CXACAIX) makes the facade layer complex. |
| **(b) Replatform** | **Low** | COACTUPC at 4,236 LOC with 56 copybook references represents the most complex program in the estate. Keeping it in COBOL on a cloud runtime preserves the maintenance burden and does not reduce the COBOL skill dependency. |
| **(c) Refactor** | **Low** | Refactoring 4,236 lines of COBOL with 188 decision points would be a massive effort that still leaves the team dependent on COBOL skills. The batch programs (CBACT01-04) are small enough to refactor, but there is limited value in doing so if the target is Java. |
| **(d) Rewrite** | **High** | The account domain is the highest-value rewrite target. COACTUPC's validation logic maps well to Java Bean Validation annotations and a Spring MVC controller. CBACT04C's interest calculation (rate x balance / 1200) maps to a Java BigDecimal service. The batch readers (CBACT01-03) are trivially small. |

### Recommendation: **(d) Rewrite** with **(a) Strangler** as transition mechanism

- **Phase 1:** Wrap COACTVWC (read-only, 941 LOC) behind a REST API using a strangler facade. This is the low-risk entry point.
- **Phase 2:** Rewrite COACTUPC as a Spring Boot service with JPA entities, extracting validation rules into a dedicated `AccountValidationService`. Run both old and new in parallel with output comparison.
- **Phase 3:** Rewrite CBACT04C (interest calculation) as a Spring Batch job with BigDecimal arithmetic. Parallel-run against historical data for at least two billing cycles.
- **Justification:** The account domain's 188 decision points represent hidden business rules that must be captured in the target system. A rewrite forces the team to understand and document every rule, which is a strategic asset. The strangler pattern provides a safe incremental path.

---

## 2. Transaction Processing

**Programs:** CBTRN01C (494 LOC), CBTRN02C (731 LOC), CBTRN03C (649 LOC), COTRN00C (699 LOC), COTRN01C (330 LOC), COTRN02C (783 LOC)

**Function:** Daily batch transaction posting pipeline (validate, enrich, post to accounts, report) and online transaction browse/view/add screens.

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **(a) Strangler** | **Medium** | The online programs (COTRN00C-02C) can be wrapped with REST APIs relatively easily. The batch pipeline (CBTRN01-03) is harder to strangle because it operates on sequential file flows managed by JCL. |
| **(b) Replatform** | **Medium** | The batch pipeline has well-defined JCL-driven steps (SORT → CBTRN01C → CBTRN02C). This linear flow could run on a cloud COBOL runtime with minimal changes. However, CBTRN02C updates ACCTDATA and TCATBALF in place, creating a tight coupling to VSAM. |
| **(c) Refactor** | **Low** | CBTRN02C has 93 decision points with complex balance update logic. Refactoring COBOL doesn't reduce operational risk or skill dependency. |
| **(d) Rewrite** | **High** | The batch pipeline maps cleanly to Spring Batch: CBTRN01C = ItemReader + ItemProcessor (validation), CBTRN02C = ItemProcessor + ItemWriter (posting), CBTRN03C = report generation step. The online screens map to Spring MVC REST controllers. |

### Recommendation: **(d) Rewrite**

- Rewrite the batch pipeline as a Spring Batch job with chunk-oriented processing.
- CBTRN01C (validation/enrichment) → `TransactionValidationProcessor`
- CBTRN02C (account posting) → `TransactionPostingProcessor` + `AccountBalanceWriter`
- CBTRN03C (report) → `TransactionReportTasklet`
- Rewrite online screens (COTRN00-02) as REST endpoints with a modern frontend.
- **Justification:** The daily transaction pipeline is the operational heart of the system. It touches ACCTDATA, TCATBALF, CARDXREF, TRANSACT, DALYTRAN, TRANTYPE, and TRANCATG — seven data stores. Converting to Spring Batch provides modern monitoring, retry logic, and cloud-native scheduling. The 93 decision points in CBTRN02C require careful extraction but are well-bounded.

---

## 3. Customer Management

**Programs:** CBCUS01C (178 LOC)

**Function:** Batch sequential reader for customer data file. Minimal logic — essentially a file iteration stub.

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **(a) Strangler** | **Low** | Too simple to warrant a facade. |
| **(b) Replatform** | **Low** | Not worth maintaining as COBOL. |
| **(c) Refactor** | **Low** | Already trivially simple (178 LOC, 11 decision points). |
| **(d) Rewrite** | **High** | This is a trivial Spring Batch `FlatFileItemReader` or JPA repository. |

### Recommendation: **(d) Rewrite**

- Replace with a JPA `CustomerRepository` and a Spring Batch reader.
- Customer data model (CVCUS01Y, 500-byte record) maps directly to a `Customer` JPA entity.
- **Justification:** At 178 LOC with minimal logic, this is an easy win. The customer entity is needed by the statement generation and export/import functions anyway.

---

## 4. Statement & Reporting

**Programs:** CBSTM03A (924 LOC), CBSTM03B (230 LOC), CORPT00C (649 LOC), CBTRN03C (649 LOC)

**Function:** Account statement generation (text + HTML), report submission via CICS transient data queues, daily transaction report formatting.

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **(a) Strangler** | **Medium** | CORPT00C (online report submission) could be wrapped with a REST API that triggers batch jobs. CBSTM03A is batch-only and harder to strangle. |
| **(b) Replatform** | **Low** | CBSTM03A uses ALTER/GO TO control flow, COMP-3 variables, and 2D arrays — all legacy patterns that complicate cloud COBOL runtimes and limit maintainability. |
| **(c) Refactor** | **Low** | The ALTER/GO TO pattern in CBSTM03A is notoriously difficult to refactor within COBOL. This is the strongest argument against keeping the code in COBOL. |
| **(d) Rewrite** | **High** | Statement generation maps perfectly to modern template engines (Thymeleaf for HTML, JasperReports or Apache FOP for PDF). The subroutine pattern (CBSTM03A calling CBSTM03B for file I/O) maps to service decomposition. |

### Recommendation: **(d) Rewrite**

- Replace CBSTM03A/B with a Spring Batch job using Thymeleaf templates for HTML and Apache FOP for PDF generation.
- Replace CORPT00C with a REST endpoint that enqueues report generation jobs to a message queue (RabbitMQ/SQS replacing CICS TD queues).
- CBTRN03C → Spring Batch report step (already covered under Transaction Processing).
- **Justification:** CBSTM03A's ALTER/GO TO is the single hardest pattern for automated COBOL tools to handle. Manual rewrite to a template engine eliminates this technical debt entirely. The 117 I/O operations (highest in the estate) reduce to simple JPA queries.

---

## 5. User Security

**Programs:** COSGN00C (260 LOC), COUSR00C (695 LOC), COUSR01C (299 LOC), COUSR02C (414 LOC), COUSR03C (359 LOC)

**Function:** User authentication (sign-on screen), CRUD for user security records (list, add, update, delete).

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **(a) Strangler** | **Medium** | Could wrap with an API gateway + OAuth2/JWT layer. However, the USRSEC VSAM file stores plaintext passwords (PIC X(08)), which is a security liability regardless of the wrapper. |
| **(b) Replatform** | **Low** | Plaintext password storage on any platform is a compliance risk. Replatforming perpetuates this vulnerability. |
| **(c) Refactor** | **Low** | Cannot meaningfully improve security posture within the COBOL paradigm without rewriting the authentication model. |
| **(d) Rewrite** | **High** | Replace with Spring Security + JWT/OAuth2. Migrate USRSEC to a proper identity store with bcrypt password hashing. The programs are small (260–695 LOC) with low decision complexity (10–41 logic points). |

### Recommendation: **(d) Rewrite**

- Replace the entire authentication subsystem with Spring Security.
- Migrate USRSEC data to a `users` table with bcrypt-hashed passwords and role-based access control.
- Implement JWT token-based session management replacing CICS COMMAREA session state.
- **Justification:** Security is non-negotiable. The current plaintext password storage (SEC-USR-PWD PIC X(08)) is a compliance risk. The programs are small and self-contained — USRSEC is the only VSAM file accessed, and no other programs write to it. This is a clean extraction boundary.

---

## 6. Card Management

**Programs:** COCRDLIC (1,459 LOC), COCRDSLC (887 LOC), COCRDUPC (1,560 LOC), COBIL00C (572 LOC)

**Function:** Credit card browsing/search with pagination, card detail view, card update with validation, bill payment processing.

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **(a) Strangler** | **High** | These are CICS online screens with well-defined user interactions. A strangler facade can expose card operations as REST APIs while COBOL handles the back-end. COBIL00C (bill payment) is a good candidate for early API wrapping because it has a clear input/output contract. |
| **(b) Replatform** | **Medium** | The programs work correctly and could run on a cloud COBOL runtime. However, COCRDUPC at 1,560 LOC with 164 decision points still represents a maintenance burden. |
| **(c) Refactor** | **Low** | High decision density (140–164 points) makes COBOL refactoring expensive. |
| **(d) Rewrite** | **High** | Card CRUD maps to a standard Spring Boot REST API with JPA. Pagination logic (CICS STARTBR/READNEXT/READPREV/ENDBR) maps to Spring Data `Pageable`. COBIL00C's bill payment logic (read account, create transaction, update balance) is a clean service method. |

### Recommendation: **(d) Rewrite** with **(a) Strangler** as transition mechanism

- **Phase 1:** Expose COBIL00C (bill payment, 572 LOC) as a REST API using strangler pattern. It has a clean input (account ID, payment amount) → output (updated balance, transaction record) contract.
- **Phase 2:** Rewrite card list/view/update as Spring Boot REST endpoints with a React/Angular frontend replacing BMS maps.
- **Justification:** Card management accesses CARDDAT, ACCTDAT, and CCXREF — three files shared with the Account domain. The strangler pattern allows the card API to coexist with account programs during the transition. COBIL00C's 28 logic points make it the easiest entry point.

---

## 7. Sub-App: Authorization IMS-DB2-MQ

**Programs:** CBPAUP0C (386 LOC), COPAUA0C (1,026 LOC), COPAUS0C (1,032 LOC), COPAUS1C (604 LOC), COPAUS2C (244 LOC), PAUDBLOD (369 LOC), PAUDBUNL (317 LOC), DBUNLDGS (366 LOC)

**Function:** Payment authorization with IMS databases, DB2 fraud detection tables (AUTHFRDS), and MQ message queuing.

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **(a) Strangler** | **High** | The MQ-based request/reply pattern (CCPAURQY/CCPAURLY) already defines a message contract. This is naturally suited to a strangler approach — replace the COBOL MQ consumer with a new Java consumer while keeping the message format. |
| **(b) Replatform** | **Low** | IMS dependencies (DL/I calls, PCBs, GSAM) are the hardest mainframe artifacts to replatform. Cloud COBOL runtimes have limited IMS support. |
| **(c) Refactor** | **Low** | IMS DL/I calls cannot be meaningfully refactored within COBOL to reduce the IMS dependency. |
| **(d) Rewrite** | **High** | Replace IMS databases with PostgreSQL tables. Replace MQ with a modern message broker (RabbitMQ, Kafka, or SQS). Replace DB2 fraud detection queries with JPA queries. The COPAUA0C online screens map to Spring Boot REST controllers. |

### Recommendation: **(a) Strangler** transitioning to **(d) Rewrite**

- **Phase 1:** Introduce a new Java MQ consumer that reads the existing authorization request messages (CCPAURQY format). Process authorization logic in Java. Write replies in CCPAURLY format. This runs in parallel with the COBOL consumer for validation.
- **Phase 2:** Migrate IMS database content to PostgreSQL tables. Replace PAUDBLOD/PAUDBUNL/DBUNLDGS with JPA-based data management.
- **Phase 3:** Rewrite CICS screens (COPAUA0C, COPAUS0C/1C/2C) as a modern authorization admin UI.
- **Justification:** The MQ message contract (request/reply) is a natural seam for the strangler pattern. IMS is the highest-risk dependency in the estate — no cloud COBOL runtime fully supports IMS DL/I. This sub-application must be rewritten; the question is how to sequence it safely.

---

## 8. Sub-App: Transaction Type DB2

**Programs:** COTRTLIC (2,098 LOC), COTRTUPC (1,702 LOC), COBTUPDT (237 LOC)

**Function:** DB2-based CRUD for transaction type and category reference data. Online browse/update screens and batch maintenance.

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **(a) Strangler** | **Medium** | Could wrap DB2 CRUD with a REST API, but the programs already use SQL, making a direct rewrite more efficient. |
| **(b) Replatform** | **Medium** | These programs use standard DB2 SQL (SELECT, INSERT, UPDATE, DELETE) which runs on any DB2-compatible cloud database. However, the CICS UI coupling remains. |
| **(c) Refactor** | **Low** | The SQL is already clean. The CICS UI is the main issue, and that cannot be meaningfully refactored within COBOL. |
| **(d) Rewrite** | **High** | The existing SQL maps directly to Spring Data JPA repositories. COTRTLIC's DB2 cursor pagination maps to Spring Data `Pageable`. COTRTUPC's CRUD maps to standard JPA `save()`/`delete()`. COBTUPDT (batch maintenance) maps to a Spring Batch job reading an action-code file. |

### Recommendation: **(d) Rewrite** — Quick Win

- This is the **recommended first migration target** (see also HOTSPOT_REPORT.md Priority 3).
- Convert DB2 tables (`TRANSACTION_TYPE`, `TRANSACTION_TYPE_CATEGORY`) to JPA entities.
- Convert COTRTLIC → Spring Data REST or Spring MVC with paginated list endpoint.
- Convert COTRTUPC → Spring MVC CRUD controller with Bean Validation.
- Convert COBTUPDT → Spring Batch `FlatFileItemReader` with action-code processor.
- Eliminate the TRANEXTR.jcl → VSAM reload pipeline (DB2 extract → flat file → VSAM REPRO). In the new architecture, batch programs read directly from the shared database.
- **Justification:** Lowest risk in the entire estate. Isolated sub-application with clear DB2 interface. No VSAM dependencies. No IMS/MQ dependencies. The SQL can be reused as JPA query templates. This is the ideal proof-of-concept for the modernization approach.

---

## 9. Sub-App: VSAM-MQ

**Programs:** COACCT01 (620 LOC), CODATE01 (524 LOC)

**Function:** MQ-based services for account inquiry (VSAM lookup) and date formatting/validation.

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **(a) Strangler** | **High** | These are already designed as message-driven services. Replace the COBOL MQ consumer with a Java MQ consumer (or migrate to REST). |
| **(b) Replatform** | **Medium** | Small programs that could run on a cloud COBOL runtime, but MQ infrastructure must still be maintained. |
| **(c) Refactor** | **Low** | The programs are already well-structured. No benefit from COBOL refactoring. |
| **(d) Rewrite** | **High** | COACCT01 → Spring Boot REST endpoint backed by JPA (replacing VSAM reads). CODATE01 → Java `java.time` API (trivial replacement for CICS ASKTIME/FORMATTIME). |

### Recommendation: **(d) Rewrite**

- COACCT01 → `AccountInquiryService` REST endpoint.
- CODATE01 → `DateService` utility class using `java.time.LocalDate` and `DateTimeFormatter`.
- If MQ integration must be preserved during transition, use Spring JMS/AMQP to consume existing MQ messages and delegate to the new Java services.
- **Justification:** Both programs are small (524–620 LOC) and self-contained. CODATE01 is particularly trivial — it replaces CICS date formatting with Java's built-in date library. COACCT01 reads the same VSAM files as the account domain, so it should be converted as part of the account migration.

---

## 10. Navigation & Menu

**Programs:** COMEN01C (308 LOC), COADM01C (288 LOC)

**Function:** Main menu (central navigation hub) and admin menu. Route users to functional sub-screens via EXEC CICS XCTL.

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **(a) Strangler** | **Low** | These are purely UI navigation programs. Wrapping them with APIs adds no value. |
| **(b) Replatform** | **Low** | Menu screens are the first thing to replace in any UI modernization. |
| **(c) Refactor** | **Low** | No business logic to refactor. |
| **(d) Rewrite** | **High** | Replace with a modern SPA (React/Angular) application shell with route-based navigation. |

### Recommendation: **(d) Rewrite**

- The navigation layer is replaced by the modern frontend application's routing.
- COMEN01C's menu options map to a sidebar/navigation component.
- COADM01C's admin menu maps to a role-gated admin section.
- The COMMAREA session state (COCOM01Y) is replaced by JWT claims and frontend state management.
- **Justification:** These programs have no business logic — they are pure UI routing. They disappear entirely when the frontend is replaced with a modern SPA.

---

## 11. Utility Programs

**Programs:** CBEXPORT (582 LOC), CBIMPORT (487 LOC), CSUTLDTC (157 LOC), COBSWAIT (41 LOC)

**Function:** Data export/import utilities, date validation, and wait/delay.

### Strategy Evaluation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **(a) Strangler** | **Low** | Utilities are called by other programs; they are not standalone services. |
| **(b) Replatform** | **Low** | Trivially small programs not worth maintaining in COBOL. |
| **(c) Refactor** | **Low** | Already simple and single-purpose. |
| **(d) Rewrite** | **High** | CBEXPORT/CBIMPORT → Spring Batch export/import jobs. CSUTLDTC → `java.time` validation. COBSWAIT → `Thread.sleep()` or scheduled executor. |

### Recommendation: **(d) Rewrite**

- CBEXPORT/CBIMPORT: Replace with Spring Batch jobs that read/write JSON or CSV instead of the custom COBOL record-type-prefixed flat file format.
- CSUTLDTC: Replace with `java.time.LocalDate.parse()` with `DateTimeFormatter`.
- COBSWAIT: Replace with `Thread.sleep()` or eliminate entirely (modern schedulers handle delays natively).
- **Justification:** These are infrastructure utilities, not business logic. They are rewritten as part of the framework migration, not as standalone efforts.

---

## Strategy Summary Matrix

| Functional Area | Programs | Total LOC | Recommended Strategy | Risk | Priority |
|----------------|----------|-----------|---------------------|------|----------|
| **Transaction Type DB2** | COTRTLIC, COTRTUPC, COBTUPDT | 4,037 | **(d) Rewrite** | Low | 1 — Quick Win |
| **Statement & Reporting** | CBSTM03A/B, CORPT00C, CBTRN03C | 2,452 | **(d) Rewrite** | Medium | 2 — High Value |
| **User Security** | COSGN00C, COUSR00-03C | 2,027 | **(d) Rewrite** | Low | 3 — Security |
| **VSAM-MQ** | COACCT01, CODATE01 | 1,144 | **(d) Rewrite** | Low | 4 — Easy |
| **Card Management** | COCRDLIC, COCRDSLC, COCRDUPC, COBIL00C | 4,478 | **(d) Rewrite** via **(a) Strangler** | Medium | 5 — Core Online |
| **Account Management** | CBACT01-04, COACTUPC, COACTVWC | 6,615 | **(d) Rewrite** via **(a) Strangler** | High | 6 — Highest Complexity |
| **Transaction Processing** | CBTRN01-03, COTRN00-02 | 4,355 | **(d) Rewrite** | High | 7 — Core Batch |
| **Authorization IMS-DB2-MQ** | CBPAUP0C, COPAUA0C, COPAUS0-2C, PAUDBLOD, PAUDBUNL, DBUNLDGS | 4,344 | **(a) Strangler** → **(d) Rewrite** | High | 8 — IMS Migration |
| **Navigation & Menu** | COMEN01C, COADM01C | 596 | **(d) Rewrite** | Low | 9 — UI Shell |
| **Utilities** | CBEXPORT, CBIMPORT, CSUTLDTC, COBSWAIT | 1,267 | **(d) Rewrite** | Low | 10 — Infrastructure |
| **Customer Management** | CBCUS01C | 178 | **(d) Rewrite** | Low | 11 — Trivial |

### Key Observations

1. **Rewrite dominates.** Every functional area is recommended for eventual rewrite to Java. The CardDemo estate is moderately sized (~28,000 LOC) — well within the scope of a full rewrite when properly sequenced.

2. **Strangler pattern is the transition mechanism**, not the end state. For high-complexity areas (Account Management, Card Management, Authorization), the strangler pattern provides safe incremental migration. For simpler areas, direct rewrite is faster.

3. **Replatform is not recommended** for any area. The estate contains too many mainframe-specific patterns (CICS, VSAM, IMS, BMS maps, ALTER/GO TO) for a cloud COBOL runtime to provide meaningful value. The maintenance burden and COBOL skill dependency would persist.

4. **Refactor is not recommended** for any area. The strategic goal is to move to Java; refactoring COBOL extends the life of a codebase the organization intends to retire.

5. **The Transaction Type DB2 sub-application is the ideal starting point.** It is isolated, already uses SQL, has no VSAM/IMS/MQ dependencies, and serves as a proof-of-concept for the JPA-based architecture.
