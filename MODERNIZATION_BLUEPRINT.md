# CardDemo Modernization Blueprint

## Executive Summary

This blueprint evaluates four modernization strategies — **Strangler Fig**, **Replatform**, **Refactor**, and **Rewrite** — for each functional area of the AWS CardDemo mainframe application. CardDemo is a COBOL/CICS/VSAM credit card management system comprising 31 COBOL programs (17 online CICS, 14 batch), 30 copybooks, 17 BMS screen maps, and 38 JCL batch jobs. The system manages credit card accounts, transactions, customer data, bill payments, reporting, user administration, and interest calculation.

The recommended strategy is a **hybrid approach**: Strangler Fig for online CICS modules (incremental, low-risk replacement) combined with Rewrite for batch processing (to leverage Spring Batch's superior scheduling and error-handling capabilities).

---

## 1. Functional Area Inventory

| ID | Functional Area | Programs | Type | Complexity | LOC (approx) |
|----|----------------|----------|------|------------|---------------|
| FA-01 | Authentication & Session Management | COSGN00C | Online CICS | Low | 261 |
| FA-02 | Menu & Navigation | COMEN01C, COADM01C | Online CICS | Low | 598 |
| FA-03 | Account Management | COACTVWC, COACTUPC | Online CICS | High | 5,179 |
| FA-04 | Card Management | COCRDLIC, COCRDSLC, COCRDUPC | Online CICS | Medium | 3,500+ |
| FA-05 | Transaction Management | COTRN00C, COTRN01C, COTRN02C | Online CICS | Medium | 1,800+ |
| FA-06 | Bill Payment | COBIL00C | Online CICS | Medium | 573 |
| FA-07 | Reporting | CORPT00C, CBTRN03C | Online + Batch | Medium | 1,350+ |
| FA-08 | User Administration | COUSR00C, COUSR01C, COUSR02C, COUSR03C | Online CICS | Medium | 2,400+ |
| FA-09 | Batch Transaction Posting | CBTRN02C | Batch | High | 732 |
| FA-10 | Interest Calculation | CBACT04C | Batch | High | 653 |
| FA-11 | Statement Generation | CBSTM03A, CBSTM03B | Batch | High | 1,850+ |
| FA-12 | Data Management (Refresh/Export/Import) | CBACT01C-03C, CBCUS01C, CBEXPORT, CBIMPORT | Batch | Medium | 2,000+ |
| FA-13 | Optional Modules (IMS/DB2/MQ) | Authorization, TranType DB2, VSAM-MQ | Mixed | High | 1,500+ |

---

## 2. Strategy Definitions

### Strangler Fig
Incrementally replace legacy components by routing traffic through a facade layer. New Java services coexist with the existing COBOL/CICS system until full replacement. Best for online transaction processing where incremental switchover reduces risk.

### Replatform (Lift-and-Shift with Modernization)
Move COBOL programs to a cloud-compatible runtime (e.g., AWS Mainframe Modernization with Micro Focus or Blu Age) with minimal code changes. Preserves existing logic but changes the hosting platform.

### Refactor
Restructure existing COBOL code to improve maintainability (e.g., eliminating GOTOs, ALTER statements, dead code) while keeping the programs in COBOL. Typically a precursor to other strategies.

### Rewrite
Build entirely new Java/Spring Boot services from scratch using the COBOL programs as functional specifications. Highest effort but delivers the cleanest target architecture.

---

## 3. Strategy Evaluation by Functional Area

### FA-01: Authentication & Session Management

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Small, self-contained module (261 LOC). VSAM USRSEC file lookup maps directly to a Spring Security + JWT service. Route authentication through an API gateway; legacy and modern can coexist via shared session tokens. |
| Replatform | Acceptable | Works but retains plaintext password comparison (SEC-USR-PWD = WS-USER-PWD) and VSAM-based session management — both are security anti-patterns. |
| Refactor | Not Recommended | The COBOL logic is too simple to benefit from refactoring; the real value is replacing the insecure authentication model. |
| Rewrite | Acceptable | Viable but the Strangler Fig approach achieves the same result with lower risk by allowing parallel operation. |

**Target**: Spring Security with BCrypt password hashing, JWT tokens, and a relational user store.

---

### FA-02: Menu & Navigation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Navigation logic (COMEN01C, COADM01C) is UI-layer code that maps BMS screens to program transfers via XCTL. This translates directly to a React/Angular SPA router with role-based route guards. The menu definition copybooks (COMEN02Y, COADM02Y) become JSON configuration. |
| Replatform | Not Recommended | 3270-style menus have no value on a modern platform. |
| Refactor | Not Recommended | No business logic to preserve. |
| Rewrite | Acceptable | Effectively the same outcome as Strangler Fig for pure UI components. |

**Target**: SPA with role-based routing; admin vs. regular user menus driven by configuration.

---

### FA-03: Account Management

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | The most complex online module (COACTUPC alone is 4,237 LOC) with deep VSAM I/O (ACCTDAT, CARDDAT, CUSTDAT, CXACAIX files), extensive field-level validation (SSN, phone, date, credit limit), and multi-file update coordination. Strangler Fig allows incremental migration: start with read-only account view (COACTVWC, lower risk), then tackle account update (COACTUPC) once the data layer is stable. |
| Replatform | Acceptable | Preserves the complex validation logic but retains VSAM coupling and fixed-length record formats. |
| Refactor | Partial Value | Could benefit from extracting validation routines (currently inline in COACTUPC) into separate paragraphs, but this is better done during the rewrite. |
| Rewrite | High Risk | The sheer volume of validation rules and edge cases makes a big-bang rewrite risky without comprehensive regression testing. |

**Target**: Account REST API (Spring Boot + JPA) with a shared validation library. VSAM files migrated to PostgreSQL tables. Dual-write during transition.

---

### FA-04: Card Management

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Three programs (list/view/update) with clear CRUD semantics. COCRDLIC uses VSAM BROWSE (STARTBR/READNEXT/ENDBR) with pagination — maps cleanly to paginated REST endpoints. Card data (CVACT02Y, 150-byte records) is simple: card number, account ID, CVV, embossed name, expiration, status. |
| Replatform | Acceptable | Straightforward but misses the opportunity to add modern card security (tokenization, PCI-DSS controls). |
| Refactor | Not Recommended | Logic is already reasonably structured. |
| Rewrite | Acceptable | The moderate complexity makes rewrite feasible, but Strangler Fig is preferred for risk management. |

**Target**: Card REST API with pagination, backed by a `cards` table with FK to `accounts`. Add card tokenization in the modern layer.

---

### FA-05: Transaction Management

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Three online programs: list (COTRN00C), view (COTRN01C), add (COTRN02C). The list/view are read-only VSAM browse operations with pagination. Transaction add (COTRN02C) writes to the TRANSACT VSAM file with a 350-byte record layout (CVTRA05Y). The sequential transaction ID generation (MAX+1) is a concurrency concern that benefits from database sequences. |
| Replatform | Acceptable | Works but retains the MAX+1 ID generation anti-pattern. |
| Refactor | Not Recommended | Limited value; the ID generation issue is architectural. |
| Rewrite | Acceptable | Transaction add could be rewritten standalone, but the read-only views should use Strangler Fig for safety. |

**Target**: Transaction REST API with database-generated IDs (sequences), indexed queries replacing VSAM BROWSE.

---

### FA-06: Bill Payment

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Single program (COBIL00C, 573 LOC) with well-defined workflow: validate account → confirm payment → create transaction record → update account balance. Reads ACCTDAT (with UPDATE lock), CXACAIX (xref), writes TRANSACT, rewrites ACCTDAT. The confirmation flow (Y/N prompt) maps to a two-step API (preview + confirm). |
| Replatform | Acceptable | Preserves logic but retains VSAM record locking semantics. |
| Refactor | Not Recommended | Already compact and well-structured. |
| Rewrite | Acceptable | Small enough to rewrite safely, but Strangler Fig provides the safety net of parallel operation. |

**Target**: Bill Payment service with idempotent payment API, optimistic locking via JPA `@Version`, and event-driven transaction creation.

---

### FA-07: Reporting

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **Recommended** | CORPT00C submits JCL batch jobs via CICS extra-partition TDQ (Internal Reader) — this pattern has no modern equivalent. The JCL embeds inline parameters (PARM-START-DATE, PARM-END-DATE) and references a TRANREPT procedure. CBTRN03C generates reports from VSAM data. Modern reporting should use SQL-based query engines, scheduled jobs, or on-demand PDF generation. |
| Strangler Fig | Partial Fit | The online report submission (CORPT00C) can be strangled, but the batch report generation needs full replacement. |
| Replatform | Not Recommended | JCL job submission and TDQ are deeply mainframe-specific. |
| Refactor | Not Recommended | The JCL/TDQ pattern cannot be refactored into something modern within COBOL. |

**Target**: Spring Batch report jobs triggered via REST API or scheduler. Reports generated as PDF/CSV from SQL queries against the transaction table.

---

### FA-08: User Administration

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Four programs (COUSR00C list, COUSR01C add, COUSR02C update, COUSR03C delete) providing full CRUD on the USRSEC VSAM file. Standard list/paginate/select pattern identical to transaction and card list screens. User record (CSUSR01Y) is 80 bytes: ID, first/last name, password, type, filler. |
| Replatform | Acceptable | Works but retains plaintext password storage. |
| Refactor | Not Recommended | Logic is straightforward CRUD. |
| Rewrite | Acceptable | Simple enough for rewrite but Strangler Fig preferred for consistency with the overall approach. |

**Target**: User management REST API integrated with Spring Security. Passwords hashed with BCrypt. Role model expanded beyond the current binary Admin/User type.

---

### FA-09: Batch Transaction Posting

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **Recommended** | CBTRN02C is a complex batch program that reads daily transactions (DALYTRAN), validates against cross-reference (XREF) and account master (ACCTDAT) files, posts valid transactions to TRANSACT and TCATBAL, writes rejects to DALYREJS, and updates account balances. It uses 6 VSAM files simultaneously. The sequential file processing with validation/reject/post pattern maps perfectly to Spring Batch's Reader → Processor → Writer paradigm with skip/retry policies. |
| Strangler Fig | Not Applicable | Batch programs don't have interactive traffic to "strangle." |
| Replatform | Acceptable | Could run on Micro Focus/Blu Age but misses the opportunity to leverage database transactions for atomicity (currently relies on VSAM file status checks). |
| Refactor | Partial Value | Could improve error handling structure but doesn't address the fundamental VSAM-to-RDBMS transition. |

**Target**: Spring Batch job with chunk-oriented processing. ItemReader for daily transaction files, ItemProcessor for validation (xref lookup, account verification), ItemWriter for posting. Database transactions replace VSAM file operations.

---

### FA-10: Interest Calculation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **Recommended** | CBACT04C reads transaction category balances (TCATBAL), looks up discount/interest rates (DISCGRP), computes monthly interest, and updates account balances. The break-logic (detecting account number changes) and multi-file coordination require careful reimplementation. The interest computation formula and fee calculation are core business logic that must be precisely preserved through equivalence testing. |
| Strangler Fig | Not Applicable | Batch program. |
| Replatform | Acceptable | Preserves the exact computation logic but ties it to VSAM. |
| Refactor | Not Recommended | The computation logic itself is sound; the issue is the platform. |

**Target**: Spring Batch interest calculation job. Discount group rates stored in a database lookup table. Account balance updates via JPA with transaction isolation.

---

### FA-11: Statement Generation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **Recommended** | CBSTM03A/B are the most complex batch programs: 924 LOC with ALTER/GO TO control flow, COMP/COMP-3 variables, 2D arrays, subroutine calls (CBSTM03B), and mainframe control block addressing (PSA/TCB/TIOT). They generate statements in both plain text and HTML format. The mainframe-specific patterns (ALTER, PSA addressing) have no modern equivalent. |
| Strangler Fig | Not Applicable | Batch program. |
| Replatform | Challenging | The ALTER statements and control block addressing may not be supported by all replatforming tools. |
| Refactor | High Effort | Eliminating ALTER/GO TO would require significant restructuring of the control flow. |

**Target**: Spring Batch + template engine (Thymeleaf/FreeMarker) for statement generation. PDF output via iText or OpenPDF. Customer/account/transaction data joined via SQL.

---

### FA-12: Data Management (Refresh/Export/Import)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **Recommended** | CBACT01C-03C refresh account/card/customer master files. CBEXPORT/CBIMPORT handle data export/import. These are essentially ETL programs that read sequential files and load VSAM datasets. In a modern architecture, these become database migration scripts, data import APIs, or Spring Batch ETL jobs. |
| Strangler Fig | Not Applicable | Batch programs. |
| Replatform | Low Value | The entire concept of "refreshing VSAM files from sequential input" disappears with RDBMS adoption. |
| Refactor | Not Recommended | Programs are straightforward file-to-file operations. |

**Target**: Database seed/migration scripts (Flyway/Liquibase) for initial load. REST-based data import APIs for ongoing feeds. Spring Batch for bulk ETL.

---

### FA-13: Optional Modules (IMS/DB2/MQ)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **Recommended** | These modules demonstrate alternative mainframe patterns: IMS DB segments, DB2 embedded SQL with cursors, MQ request/reply messaging. Each addresses a different integration pattern: authorization via MQ triggers, transaction type CRUD via DB2, and account inquiry via MQ. These should be rewritten as microservices or modules within the monolith, using modern equivalents: JPA for DB2, Spring AMQP/Kafka for MQ, REST for IMS. |
| Strangler Fig | Partial Fit | The MQ-based modules could be strangled by replacing MQ queues with modern message brokers. |
| Replatform | Not Recommended | IMS/DB2/MQ dependencies are deeply mainframe-specific. |
| Refactor | Not Recommended | Platform dependencies cannot be refactored away. |

**Target**: Spring Boot modules with JPA (replacing DB2), Spring AMQP or Kafka (replacing MQ), and REST APIs (replacing IMS transactions).

---

## 4. Strategy Summary Matrix

| Functional Area | Strangler Fig | Replatform | Refactor | Rewrite | **Recommended** |
|----------------|:---:|:---:|:---:|:---:|:---:|
| FA-01 Authentication | **Yes** | ~ | No | ~ | Strangler Fig |
| FA-02 Menu & Navigation | **Yes** | No | No | ~ | Strangler Fig |
| FA-03 Account Management | **Yes** | ~ | ~ | Risk | Strangler Fig |
| FA-04 Card Management | **Yes** | ~ | No | ~ | Strangler Fig |
| FA-05 Transaction Management | **Yes** | ~ | No | ~ | Strangler Fig |
| FA-06 Bill Payment | **Yes** | ~ | No | ~ | Strangler Fig |
| FA-07 Reporting | ~ | No | No | **Yes** | Rewrite |
| FA-08 User Administration | **Yes** | ~ | No | ~ | Strangler Fig |
| FA-09 Batch Transaction Posting | N/A | ~ | ~ | **Yes** | Rewrite |
| FA-10 Interest Calculation | N/A | ~ | No | **Yes** | Rewrite |
| FA-11 Statement Generation | N/A | Risk | Risk | **Yes** | Rewrite |
| FA-12 Data Management | N/A | Low | No | **Yes** | Rewrite |
| FA-13 Optional Modules | ~ | No | No | **Yes** | Rewrite |

**Legend**: **Yes** = Recommended, ~ = Acceptable, No = Not Recommended, Risk = High Risk, N/A = Not Applicable

---

## 5. Target Architecture Overview

```
                    +-----------------------+
                    |   React/Angular SPA   |
                    |   (FA-02 Navigation)  |
                    +-----------+-----------+
                                |
                    +-----------v-----------+
                    |    API Gateway /       |
                    |    Strangler Facade    |
                    +-----------+-----------+
                                |
          +---------------------+---------------------+
          |                     |                     |
+---------v--------+  +--------v--------+  +---------v--------+
| Auth Service     |  | Account Service |  | Card Service     |
| (FA-01)          |  | (FA-03)         |  | (FA-04)          |
| Spring Security  |  | Spring Boot     |  | Spring Boot      |
| JWT + BCrypt     |  | JPA + Postgres  |  | JPA + Postgres   |
+------------------+  +-----------------+  +------------------+

+---------v--------+  +--------v--------+  +---------v--------+
| Transaction Svc  |  | Bill Payment    |  | User Admin Svc   |
| (FA-05)          |  | (FA-06)         |  | (FA-08)          |
| Spring Boot      |  | Spring Boot     |  | Spring Security  |
| JPA + Postgres   |  | Event-driven    |  | JPA + Postgres   |
+------------------+  +-----------------+  +------------------+

+---------v--------+  +--------v--------+  +---------v--------+
| Batch: Posting   |  | Batch: Interest |  | Batch: Statements|
| (FA-09)          |  | (FA-10)         |  | (FA-11)          |
| Spring Batch     |  | Spring Batch    |  | Spring Batch +   |
| Chunk Processing |  | Scheduled       |  | Template Engine  |
+------------------+  +-----------------+  +------------------+

+---------v--------+  +--------v--------+
| Reporting Svc    |  | Data Management |
| (FA-07)          |  | (FA-12)         |
| Spring Batch +   |  | Flyway +        |
| REST trigger     |  | Spring Batch    |
+------------------+  +-----------------+
```

---

## 6. Data Migration Strategy

### VSAM-to-RDBMS Mapping

| VSAM File | Key | Record Size | Target Table | Notes |
|-----------|-----|-------------|-------------|-------|
| USRSEC | SEC-USR-ID (8) | 80 bytes | `users` | Add BCrypt password column |
| ACCTDAT | ACCT-ID (11) | 300 bytes | `accounts` | 178 bytes of filler become nullable columns or dropped |
| CARDDAT | CARD-NUM (16) | 150 bytes | `cards` | FK to accounts |
| CUSTDAT | CUST-ID (9) | 500 bytes | `customers` | 168 bytes of filler |
| CARDXREF | XREF-CARD-NUM (16) | 50 bytes | Resolved via FKs | Card→Customer→Account relationships via JPA |
| TRANSACT | TRAN-ID (16) | 350 bytes | `transactions` | Largest dataset; needs partitioning strategy |
| DALYTRAN | Sequential | 350 bytes | `daily_transactions` (staging) | Temporary staging table for batch posting |
| TCATBALF | Composite key (17) | 50 bytes | `transaction_category_balances` | Composite key: acct_id + type_cd + cat_cd |
| DISCGRP | Composite key (16) | 50 bytes | `discount_groups` | Interest rate lookup |
| DALYREJS | Sequential | 430 bytes | `rejected_transactions` | Audit trail |

### Migration Approach
1. **Schema-first**: Define target PostgreSQL schema using Flyway migrations
2. **Bulk load**: Use ASCII data files in `app/data/ASCII/` for initial data seeding
3. **Dual-write**: During Strangler Fig transition, write to both VSAM and RDBMS
4. **Validation**: Use record counts and checksums to verify data integrity
5. **Cutover**: Switch reads to RDBMS once validated; decommission VSAM writes

---

## 7. Technology Stack Recommendation

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| Frontend | React + TypeScript | Replaces BMS 3270 screens; component-based UI |
| API Gateway | Spring Cloud Gateway | Strangler Fig facade; traffic routing |
| Backend | Spring Boot 3.x (Java 17+) | Industry standard for enterprise Java |
| Batch | Spring Batch 5.x | Direct mapping from JCL/COBOL batch patterns |
| Database | PostgreSQL | Open-source, ACID-compliant, JSON support |
| Auth | Spring Security + JWT | Replaces VSAM-based session management |
| Messaging | Apache Kafka or RabbitMQ | Replaces MQ Series for optional modules |
| ORM | Spring Data JPA / Hibernate | Replaces VSAM CRUD operations |
| Migration | Flyway | Schema versioning and data migration |
| Testing | JUnit 5 + equivalence harness | Functional equivalence validation |
| CI/CD | GitHub Actions | Automated build, test, and deployment |
| Monitoring | Micrometer + Prometheus + Grafana | Replaces mainframe SMF/RMF monitoring |

---

## 8. Effort Estimates

| Functional Area | Strategy | Effort (Sprints) | Team Size | Confidence |
|----------------|----------|:---------:|:---------:|:----------:|
| FA-01 Authentication | Strangler Fig | 2 | 2 | High |
| FA-02 Menu & Navigation | Strangler Fig | 2 | 2 | High |
| FA-03 Account Management | Strangler Fig | 5 | 3 | Medium |
| FA-04 Card Management | Strangler Fig | 3 | 2 | High |
| FA-05 Transaction Management | Strangler Fig | 3 | 2 | High |
| FA-06 Bill Payment | Strangler Fig | 2 | 2 | High |
| FA-07 Reporting | Rewrite | 3 | 2 | Medium |
| FA-08 User Administration | Strangler Fig | 3 | 2 | High |
| FA-09 Batch Transaction Posting | Rewrite | 4 | 3 | Medium |
| FA-10 Interest Calculation | Rewrite | 3 | 2 | Medium |
| FA-11 Statement Generation | Rewrite | 4 | 3 | Low |
| FA-12 Data Management | Rewrite | 2 | 2 | High |
| FA-13 Optional Modules | Rewrite | 4 | 3 | Medium |
| **Infrastructure & DevOps** | — | 3 | 2 | High |
| **Integration Testing** | — | 3 | 3 | Medium |
| **Total** | — | **~46 sprints** | **3-4 devs** | — |

> Note: Sprints are 2-week iterations. Many functional areas can be parallelized, reducing wall-clock time to approximately 6-9 months with a team of 3-4 developers.
