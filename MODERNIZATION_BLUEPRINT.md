# CardDemo Modernization Blueprint

## Executive Summary

This blueprint evaluates four modernization strategies -- **Strangler Fig**, **Replatform**, **Refactor**, and **Rewrite** -- for each functional area of the AWS CardDemo mainframe application. CardDemo is a COBOL/CICS/VSAM credit card management system comprising 31 online and batch programs, 30 copybooks, 17 BMS screen maps, 38 JCL batch jobs, and three optional extension modules (IMS/DB2/MQ authorization, DB2 transaction-type management, VSAM-MQ account extraction).

The recommended overall approach is a **Strangler Fig pattern with selective Rewrite**, migrating to a Java/Spring Boot + relational database target architecture. This balances risk reduction with the opportunity to shed accumulated technical debt.

---

## 1. Functional Area Inventory

| # | Functional Area | Programs | Complexity | Data Stores | Integration Points |
|---|----------------|----------|-----------|-------------|-------------------|
| FA-1 | Authentication & User Security | COSGN00C | Low | USRSEC (VSAM KSDS) | CICS XCTL to menus |
| FA-2 | Menu & Navigation | COMEN01C, COADM01C | Low | None (stateless routing) | COMMAREA-based control transfer |
| FA-3 | Account Management | COACTVWC, COACTUPC | High | ACCTDAT, CARDDAT, CUSTDAT, CXACAIX (VSAM) | Cross-reference lookups |
| FA-4 | Card Management | COCRDLIC, COCRDSLC, COCRDUPC | Medium | CARDDAT, CARDAIX, CXACAIX (VSAM) | Account/customer cross-refs |
| FA-5 | Transaction Processing (Online) | COTRN00C, COTRN01C, COTRN02C | Medium | TRANSACT (VSAM KSDS) | Card cross-reference |
| FA-6 | Bill Payment | COBIL00C | Medium | ACCTDAT, TRANSACT, CXACAIX (VSAM) | Writes transactions, updates accounts |
| FA-7 | Transaction Reporting (Online) | CORPT00C | Medium | TRANSACT (VSAM) | Submits JCL via internal reader TDQ |
| FA-8 | Batch Transaction Posting | CBTRN02C | High | DALYTRAN, TRANSACT, XREFFILE, ACCTDAT, TCATBALF (VSAM) | Multi-file coordination |
| FA-9 | Batch Interest Calculation | CBACT04C | High | TCATBALF, XREFFILE, ACCTDAT, DISCGRP (VSAM) | Discount group lookups |
| FA-10 | Batch Statement Generation | CBSTM03A, CBSTM03B | High | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE (VSAM) | Subroutine CALL, PSA/TCB/TIOT addressing, ALTER/GO TO |
| FA-11 | Batch Data Management | CBACT01C-03C, CBCUS01C, CBTRN01C, CBTRN03C, CBEXPORT, CBIMPORT | Medium | Various VSAM files | Sequential file I/O |
| FA-12 | Authorization Processing (IMS/DB2/MQ) | COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, CBPAUP0C | Very High | IMS HIDAM DB, DB2 AUTHFRDS table, MQ queues, VSAM | MQ trigger, IMS DL/I, embedded SQL, two-phase commit |
| FA-13 | Transaction Type Management (DB2) | COTRTUPC, COTRTLIC, COBTUPDT | Medium | DB2 TRANSACTION_TYPE, TRANSACTION_TYPE_CATEGORY | Embedded static SQL, DB2 cursors |
| FA-14 | VSAM-MQ Account Extraction | CODATE01, COACCT01 | Low | VSAM (read), MQ queues | MQ request/response pattern |
| FA-15 | Utility & Infrastructure | CSUTLDTC, COBSWAIT, assembler (MVSWAIT, COBDATFT) | Low | None | Date validation, wait routines |

---

## 2. Strategy Definitions

| Strategy | Description | Best For | Risk Profile |
|----------|-------------|----------|-------------|
| **Strangler Fig** | Incrementally replace legacy components behind an API facade; old and new run in parallel until cutover | Read-only screens, list/view operations, low-coupling modules | Low |
| **Replatform** | Lift COBOL to run on a cloud-compatible runtime (e.g., Micro Focus, AWS Mainframe Modernization) with minimal code changes | Complex batch with tricky runtime behavior (PSA/TIOT, ALTER/GO TO) that is expensive to rewrite | Low-Medium |
| **Refactor** | Convert COBOL to Java semi-automatically using tooling, preserving business logic structure but replacing runtime | Medium-complexity programs with clear structure and few platform dependencies | Medium |
| **Rewrite** | Build new Java/Spring Boot services from scratch using COBOL as specification | Core transactional logic where modernization also improves design; areas needing new capabilities | Medium-High |

---

## 3. Strategy Evaluation by Functional Area

### FA-1: Authentication & User Security

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Expose a new Spring Security / JWT authentication service. Route login requests to new service while legacy COSGN00C continues for unmodified screens. |
| Replatform | Poor | Security model (plaintext passwords in VSAM) should not be preserved. |
| Refactor | Fair | Simple enough to auto-convert, but the security model needs redesign. |
| Rewrite | Good | Opportunity to implement modern auth (OAuth2/JWT, hashed passwords, RBAC). |

**Recommendation**: Strangler Fig into a new Auth microservice, then retire COSGN00C. Replace USRSEC VSAM with a users table + Spring Security.

---

### FA-2: Menu & Navigation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Navigation is purely presentational; replace with a web UI as screens migrate. |
| Replatform | Poor | BMS maps have no value in a modern UI. |
| Refactor | Poor | No business logic worth preserving. |
| Rewrite | Good | Build a modern SPA/web frontend. |

**Recommendation**: Strangler Fig -- build a new web frontend that routes to both legacy (via 3270 bridge) and new services. Retire BMS maps incrementally.

---

### FA-3: Account Management

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Good | Account View (read-only) can be strangled first. |
| Replatform | Fair | COACTUPC is 4,237 lines with extensive validation -- high conversion cost. |
| Refactor | Fair | Structured but very large; auto-conversion tools handle this reasonably. |
| **Rewrite** | **Recommended** | Core domain logic; opportunity to build clean Account service with proper validation, REST API, and JPA entities. |

**Recommendation**: Strangle Account View first (read path), then Rewrite Account Update as a new Account Service. Map VSAM copybooks (CVACT01Y, CVACT02Y, CVCUS01Y, CVACT03Y) to JPA entities.

---

### FA-4: Card Management

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Card List and Card View are read-only and can be strangled early. Card Update follows. |
| Replatform | Fair | Moderate complexity, but VSAM alternate-index patterns need rethinking. |
| Refactor | Good | Well-structured CRUD operations. |
| Rewrite | Good | Clean domain, natural REST resource. |

**Recommendation**: Strangler Fig for Card List/View, then Rewrite Card Update as part of the Card Service. Unify card-cross-reference logic into the relational model.

---

### FA-5: Transaction Processing (Online)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Good | Transaction List/View are read paths. |
| Replatform | Fair | Moderate complexity. |
| Refactor | Good | Straightforward VSAM BROWSE/READ patterns. |
| **Rewrite** | **Recommended** | Transaction Add (COTRN02C) creates records -- needs modern validation, audit trails, and API integration. |

**Recommendation**: Strangler Fig for list/view, Rewrite for transaction add as part of a Transaction Service.

---

### FA-6: Bill Payment

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Fair | Tightly coupled -- reads account, writes transaction, updates balance in one flow. |
| Replatform | Poor | Business flow needs redesign for modern payment patterns. |
| Refactor | Fair | Single program, but cross-cutting concerns. |
| **Rewrite** | **Recommended** | Payment flows benefit from saga/event patterns, idempotency, and audit logging that COBOL lacks. |

**Recommendation**: Rewrite as a Payment Service with proper transaction management. Implement saga pattern for the read-validate-pay-update flow. This is a critical business function requiring careful parallel testing.

---

### FA-7: Transaction Reporting (Online)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Report submission can be replaced with a new reporting API/queue. |
| Replatform | Poor | JCL submission via TDQ is a mainframe-specific pattern. |
| Refactor | Poor | Heavy mainframe coupling (INTRDR, TDQ). |
| Rewrite | Good | Modern reporting frameworks (Jasper, PDF generation) are superior. |

**Recommendation**: Strangler Fig -- replace with a new Report Service that accepts requests via REST/messaging and generates reports using modern tools. Retire JCL-based reporting.

---

### FA-8: Batch Transaction Posting

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Poor | Core batch logic with multi-file updates; hard to incrementally replace. |
| Replatform | Fair | Could run under Micro Focus batch runtime initially. |
| Refactor | Fair | Well-structured sequential processing. |
| **Rewrite** | **Recommended** | Convert to Spring Batch job. The read-validate-post-reject pattern maps cleanly to Spring Batch reader/processor/writer. |

**Recommendation**: Rewrite as a Spring Batch job. Map VSAM files to database tables. Maintain the validation/rejection pipeline. Run legacy and new in parallel with reconciliation during cutover.

---

### FA-9: Batch Interest Calculation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Poor | Tightly coupled to account and transaction data. |
| Replatform | Fair | Complex financial logic benefits from careful migration. |
| **Refactor** | **Recommended** | Business rules are well-defined (discount group lookup, rate application, balance update). Auto-conversion preserves exact logic. |
| Rewrite | Good | But risk of introducing calculation discrepancies in financial logic. |

**Recommendation**: Refactor (auto-convert) to preserve exact financial calculation logic, with extensive reconciliation testing. Wrap in Spring Batch job infrastructure.

---

### FA-10: Batch Statement Generation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Poor | Monolithic batch, hard to decompose incrementally. |
| **Replatform** | **Recommended** | Uses ALTER/GO TO, PSA/TCB/TIOT addressing, POINTER manipulation, subroutine CALL -- all very difficult to auto-convert or rewrite correctly. |
| Refactor | Poor | Non-standard constructs (ALTER, control-block addressing) defeat most auto-conversion tools. |
| Rewrite | Fair | High effort, but eventually needed. |

**Recommendation**: Replatform initially (run under Micro Focus or AWS M2 managed runtime). Plan a future Rewrite once all upstream data is migrated to the relational database. Statement generation ultimately becomes a PDF/email service.

---

### FA-11: Batch Data Management

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Refactor** | **Recommended** | Simple sequential file I/O programs (load, export, import). Straightforward to auto-convert. |
| Strangler Fig | Fair | These are standalone utilities. |
| Replatform | Fair | Low complexity, not worth keeping on legacy runtime. |
| Rewrite | Fair | Simple enough but unnecessary when auto-conversion works. |

**Recommendation**: Refactor using auto-conversion tools. Convert to Spring Batch jobs that load/export data between files and database tables. These become database migration/ETL scripts.

---

### FA-12: Authorization Processing (IMS/DB2/MQ)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Fair | Can route MQ traffic to new service. |
| Replatform | Poor | IMS HIDAM DB is the hardest technology to replatform. |
| Refactor | Poor | IMS DL/I calls, embedded SQL, and MQ triggers have no direct Java equivalent in auto-conversion. |
| **Rewrite** | **Recommended** | Replace IMS with relational DB, MQ with modern messaging (Kafka/SQS/RabbitMQ), DB2 fraud table with JPA entity. Build a clean Authorization Service. |

**Recommendation**: Rewrite as an Authorization Service. This is the most complex module but also the one most constrained by legacy technologies (IMS HIDAM). Replace: IMS DB -> PostgreSQL/Oracle tables, MQ triggers -> message listeners (Spring JMS/Kafka), DB2 fraud table -> JPA entity. Implement as a separate bounded context.

---

### FA-13: Transaction Type Management (DB2)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Refactor** | **Recommended** | Already uses SQL (DB2 embedded SQL); refactoring to JPA/JDBC is mechanical. |
| Strangler Fig | Good | Admin-only CRUD, easy to strangle behind API. |
| Replatform | Poor | DB2 dependency means it won't run without DB2. |
| Rewrite | Fair | Simple enough but refactoring preserves tested cursor logic. |

**Recommendation**: Refactor -- convert DB2 embedded SQL to Spring Data JPA repositories. Map DB2 tables to JPA entities. Preserve cursor-based pagination logic as paginated JPA queries.

---

### FA-14: VSAM-MQ Account Extraction

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Replace MQ request/response with REST API endpoints for date and account inquiry. |
| Replatform | Poor | MQ-specific patterns. |
| Refactor | Fair | Small programs, but MQ API calls need manual mapping. |
| Rewrite | Good | Very small scope. |

**Recommendation**: Strangler Fig -- expose Account and System Date as REST API endpoints. Deprecate MQ-based inquiry pattern. Simplest area to migrate.

---

### FA-15: Utility & Infrastructure

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **Recommended** | Date validation (CSUTLDTC) becomes java.time. Wait routines (COBSWAIT) are unnecessary. Assembler routines have no equivalent. |
| Strangler Fig | N/A | Utilities are internal, not independently strangulable. |
| Replatform | Poor | Assembler has no cloud equivalent. |
| Refactor | Poor | Assembler cannot be auto-converted. |

**Recommendation**: Rewrite utilities using Java standard library. Date validation -> java.time. Wait routines -> Thread.sleep or ScheduledExecutorService. Assembler programs are retired entirely (their functions are handled by the Java runtime/OS).

---

## 4. Strategy Summary Matrix

| Functional Area | Strangler Fig | Replatform | Refactor | Rewrite | **Selected** |
|----------------|:---:|:---:|:---:|:---:|:---:|
| FA-1 Authentication | **X** | | | | Strangler Fig |
| FA-2 Menu/Navigation | **X** | | | | Strangler Fig |
| FA-3 Account Management | X | | | **X** | Strangler + Rewrite |
| FA-4 Card Management | **X** | | | X | Strangler + Rewrite |
| FA-5 Transaction Processing | X | | | **X** | Strangler + Rewrite |
| FA-6 Bill Payment | | | | **X** | Rewrite |
| FA-7 Transaction Reporting | **X** | | | | Strangler Fig |
| FA-8 Batch Transaction Posting | | | | **X** | Rewrite |
| FA-9 Batch Interest Calc | | | **X** | | Refactor |
| FA-10 Batch Statement Gen | | **X** | | | Replatform (interim) |
| FA-11 Batch Data Management | | | **X** | | Refactor |
| FA-12 Authorization (IMS/DB2/MQ) | | | | **X** | Rewrite |
| FA-13 Transaction Type Mgmt (DB2) | | | **X** | | Refactor |
| FA-14 VSAM-MQ Extraction | **X** | | | | Strangler Fig |
| FA-15 Utilities | | | | **X** | Rewrite |

---

## 5. Target Architecture

```
                    +------------------+
                    |   Web Frontend   |
                    |  (React / Angular)|
                    +--------+---------+
                             |
                    +--------+---------+
                    |   API Gateway    |
                    +--------+---------+
                             |
        +--------------------+--------------------+
        |           |           |           |      |
   +----+----+ +----+----+ +----+----+ +----+----+ |
   |  Auth   | | Account | |  Card   | |  Txn    | |
   | Service | | Service | | Service | | Service | |
   +---------+ +---------+ +---------+ +---------+ |
        |           |           |           |      |
   +----+----+ +----+----+ +----+----+ +----+----+ |
   | Payment | | Report  | |  Auth   | | Batch   | |
   | Service | | Service | | (Authz) | | Jobs    | |
   +---------+ +---------+ +---------+ +---------+ |
        |           |           |           |      |
        +--------------------+--------------------+
                             |
                    +--------+---------+
                    | PostgreSQL / RDS |
                    | (replaces VSAM,  |
                    |  IMS DB, DB2)    |
                    +------------------+
```

### Technology Stack

| Layer | Technology |
|-------|-----------|
| Frontend | React or Angular (replacing BMS/3270 screens) |
| API Gateway | Spring Cloud Gateway or AWS API Gateway |
| Services | Spring Boot 3.x, Java 17+ |
| Batch | Spring Batch (replacing JCL/batch COBOL) |
| Messaging | Apache Kafka or Amazon SQS (replacing MQ) |
| Database | PostgreSQL or Amazon RDS (replacing VSAM, IMS DB, DB2) |
| Security | Spring Security + OAuth2/JWT (replacing USRSEC VSAM) |
| Observability | Micrometer + Prometheus + Grafana |

---

## 6. Data Migration Strategy

### VSAM-to-Relational Mapping

| VSAM File | Record Layout (Copybook) | Target Table | Key |
|-----------|------------------------|--------------|-----|
| USRSEC | CSUSR01Y (80 bytes) | `users` | USR-ID |
| ACCTDAT | CVACT01Y (300 bytes) | `accounts` | ACCT-ID |
| CARDDAT | CVACT02Y (150 bytes) | `cards` | CARD-NUM |
| CUSTDAT | CVCUS01Y (500 bytes) | `customers` | CUST-ID |
| CARDXREF | CVACT03Y (50 bytes) | Eliminated -- use FK relationships | XREF-CARD-NUM |
| TRANSACT | CVTRA05Y (350 bytes) | `transactions` | TRAN-ID |
| DALYTRAN | CVTRA06Y (350 bytes) | `daily_transactions` (staging) | DALYTRAN-ID |
| TCATBALF | CVTRA01Y (50 bytes) | `transaction_category_balances` | ACCT-ID + TYPE-CD + CAT-CD |
| DISCGRP | CVTRA02Y (50 bytes) | `discount_groups` | GROUP-ID + TYPE-CD + CAT-CD |

### Key Data Migration Decisions

1. **Cross-reference elimination**: The CARDXREF file becomes unnecessary -- relational foreign keys replace the card-to-account-to-customer cross-reference.
2. **FILLER fields**: All FILLER bytes in copybooks are dropped during migration.
3. **Packed decimal**: COBOL PIC S9(10)V99 fields map to `DECIMAL(12,2)` in PostgreSQL.
4. **EBCDIC/ASCII**: Use the ASCII sample data in `app/data/ASCII/` for initial test loads.

---

## 7. Key Technical Challenges

| Challenge | Impact | Mitigation |
|-----------|--------|-----------|
| COBOL packed decimal (COMP-3) precision | Financial calculation discrepancies | Use `BigDecimal` in Java; reconciliation testing |
| ALTER/GO TO in CBSTM03A | Defeats auto-conversion tools | Replatform initially; rewrite later |
| PSA/TCB/TIOT control block addressing in CBSTM03A | z/OS-specific, no Java equivalent | Replatform on managed runtime; replace with Java config |
| CICS pseudo-conversational model | COMMAREA state management has no direct Spring equivalent | Redesign as stateless REST + session/token state |
| IMS HIDAM hierarchical data model | No relational equivalent without schema redesign | Flatten to relational tables during rewrite |
| MQ trigger-initiated CICS transactions | Different activation model than REST/messaging | Replace with message listener containers |
| BMS map-to-UI conversion | 17 screens need new UI equivalents | Generate React/Angular components from BMS analysis |
| Batch job scheduling (CA7/Control-M) | JCL dependencies need orchestration replacement | Spring Batch + Spring Cloud Data Flow or AWS Step Functions |
