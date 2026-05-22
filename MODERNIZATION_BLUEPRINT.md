# CardDemo Modernization Blueprint

## Executive Summary

This blueprint evaluates four modernization strategies — **Strangler Fig**, **Replatform**, **Refactor**, and **Rewrite** — for each functional area of the CardDemo mainframe credit card management system. The system comprises ~20,650 lines of COBOL across 31 programs, 29 copybooks, 24+ JCL jobs, and optional modules for IMS/DB2/MQ integration. The target platform is Java on cloud infrastructure.

---

## System Inventory Summary

| Metric | Count |
|:-------|------:|
| COBOL programs (core) | 31 |
| Copybooks | 29 |
| BMS screen maps | 18 |
| JCL batch jobs | 24+ |
| CICS online transactions | 18 |
| VSAM datasets | 12+ |
| Optional module programs (IMS/DB2/MQ) | 8 |
| Total COBOL LOC | ~20,650 |
| Scheduler workflows (Control-M) | 4 folders |

---

## Functional Area Inventory

### FA-1: Authentication & User Security

| Attribute | Detail |
|:----------|:-------|
| Programs | `COSGN00C` (260 LOC) |
| Data stores | USRSEC VSAM KSDS |
| Copybooks | `CSUSR01Y`, `COCOM01Y` |
| CICS transactions | CC00 (Sign-on) |
| Complexity | Low |

**Description**: Handles user sign-on with userid/password validation against the USRSEC VSAM file. Supports two roles: Admin (`A`) and Regular User (`U`). Password stored in plaintext in the VSAM record.

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|:---------|:---:|:----------|
| **Strangler Fig** | **Recommended** | Wrap the authentication behind a new REST/JWT auth service. The COMMAREA contract (`COCOM01Y`) provides a clean seam — new auth service populates `CDEMO-USER-ID` and `CDEMO-USER-TYPE`. Other programs only check the COMMAREA, so they are unaffected. |
| Replatform | Fair | Automated translation would work but perpetuates plaintext password storage and VSAM-based auth — both security anti-patterns. |
| Refactor | Good | Small enough to refactor incrementally but a greenfield auth module is simpler than refactoring CICS RECEIVE/SEND patterns. |
| Rewrite | Good | Only 260 LOC; easy to rewrite. However, Strangler Fig is preferred because it allows parallel operation during migration. |

---

### FA-2: Menu Navigation & Routing

| Attribute | Detail |
|:----------|:-------|
| Programs | `COMEN01C` (308 LOC), `COADM01C` (288 LOC) |
| Data stores | None (routing only) |
| Copybooks | `COMEN02Y` (menu options table), `COCOM01Y` |
| CICS transactions | CM00 (User Menu), CA00 (Admin Menu) |
| Complexity | Low |

**Description**: Table-driven menu routing. `COMEN02Y` defines 11 menu options mapping option numbers to program names and user-type restrictions. Admin menu provides user management and optional DB2 transaction type management.

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|:---------|:---:|:----------|
| **Rewrite** | **Recommended** | Menu routing maps directly to a modern SPA router or API gateway. The table in `COMEN02Y` is the complete specification. No complex business logic to preserve — just a routing table. |
| Strangler Fig | Fair | Possible but over-engineered for simple routing logic. |
| Replatform | Poor | BMS maps and CICS pseudo-conversational patterns don't translate well to modern UIs via automated tools. |
| Refactor | Poor | The 3270 terminal interaction model has no meaningful equivalent to refactor toward. |

---

### FA-3: Account Management

| Attribute | Detail |
|:----------|:-------|
| Programs | `COACTVWC` (941 LOC), `COACTUPC` (4,236 LOC) |
| Data stores | ACCTDATA VSAM KSDS, CARDXREF VSAM KSDS, CUSTDATA VSAM KSDS |
| Copybooks | `CVACT01Y` (Account), `CVACT03Y` (XREF), `CVCUS01Y` (Customer), `CVCRD01Y` |
| BMS maps | COACTVW, COACTUP |
| CICS transactions | CAVW (View), CAUP (Update) |
| Complexity | **High** — `COACTUPC` is the largest online program (4,236 LOC) |

**Description**: Full CRUD for account data including balance, credit limits, dates, and group assignment. The update program has extensive field-level validation (phone number, state code, yes/no fields, signed numbers, mandatory checks) with complex CICS pseudo-conversational state management.

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|:---------|:---:|:----------|
| **Strangler Fig** | **Recommended** | Expose account data via a new Account REST API backed by a relational DB. The VSAM record layout (`CVACT01Y`, 300-byte record) maps cleanly to a JPA entity. Gradually redirect CICS programs to call the new API. The `CVACT03Y` cross-reference provides the join logic. |
| Replatform | Fair | Automated COBOL-to-Java would preserve the 4,236-LOC validation logic but produce unmaintainable generated code with CICS API stubs. |
| Refactor | Good | Validation logic in `COACTUPC` is valuable business logic worth preserving. Extract validation rules as a service. |
| Rewrite | Risky | 4,236 LOC of validation logic risks losing subtle business rules. Requires exhaustive test coverage first. |

---

### FA-4: Credit Card Management

| Attribute | Detail |
|:----------|:-------|
| Programs | `COCRDLIC` (1,459 LOC), `COCRDSLC` (887 LOC), `COCRDUPC` (1,560 LOC) |
| Data stores | CARDDATA VSAM KSDS, CARDXREF VSAM KSDS, ACCTDATA VSAM KSDS |
| Copybooks | `CVACT02Y` (Card), `CVACT03Y` (XREF), `CVCRD01Y` |
| BMS maps | COCRDLI, COCRDSL, COCRDUP |
| CICS transactions | CCLI (List), CCDL (View), CCUP (Update) |
| Complexity | Medium-High |

**Description**: List, view, and update credit card records. Card list supports pagination via STARTBR/READNEXT. Card update includes account cross-reference validation and status management. Card records are 150 bytes with embedded name, CVV, expiration, and active status.

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|:---------|:---:|:----------|
| **Strangler Fig** | **Recommended** | Build a Card Service API alongside the Account Service. The `CVACT02Y` and `CVACT03Y` copybooks define clean entity boundaries. Pagination logic maps to standard offset/cursor-based REST pagination. |
| Replatform | Fair | Card management is less complex than accounts; automated translation is feasible but produces lower-quality code than a targeted rewrite. |
| Refactor | Good | Card update logic (1,560 LOC) has reusable validation patterns similar to account update. |
| Rewrite | Good | Moderate size; rewrite is feasible with good test coverage. |

---

### FA-5: Transaction Processing (Online)

| Attribute | Detail |
|:----------|:-------|
| Programs | `COTRN00C` (699 LOC), `COTRN01C` (330 LOC), `COTRN02C` (783 LOC) |
| Data stores | TRANSACT VSAM KSDS, CARDXREF VSAM KSDS |
| Copybooks | `CVTRA05Y` (Transaction, 350-byte record), `CVTRA03Y`, `CVTRA04Y` |
| BMS maps | COTRN00, COTRN01, COTRN02 |
| CICS transactions | CT00 (List), CT01 (View), CT02 (Add) |
| Complexity | Medium |

**Description**: Online transaction list with pagination, view details, and add new transactions. Transaction records are 350 bytes with merchant data, timestamps, type/category codes, and card number linkage. The add function generates sequential transaction IDs.

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|:---------|:---:|:----------|
| **Strangler Fig** | **Recommended** | Create a Transaction Service API. The 350-byte record (`CVTRA05Y`) maps to a well-defined entity. Transaction add can be fronted by an event-driven architecture for future scalability. |
| Replatform | Fair | Straightforward CRUD; automated translation works but misses the opportunity to adopt event-driven patterns. |
| Refactor | Good | Transaction list pagination pattern could be refactored to cursor-based API. |
| Rewrite | Good | Medium complexity; feasible. |

---

### FA-6: Transaction Processing (Batch — Core)

| Attribute | Detail |
|:----------|:-------|
| Programs | `CBTRN02C` (731 LOC — Post Transactions), `CBTRN01C` (494 LOC — Transaction Backup) |
| JCL | POSTTRAN, TRANBKP, COMBTRAN, TRANIDX |
| Data stores | DALYTRAN (sequential), TRANSACT VSAM KSDS, CARDXREF VSAM KSDS, ACCTDATA VSAM KSDS, TCATBALF VSAM KSDS, DALYREJS GDG |
| Copybooks | `CVTRA05Y`, `CVTRA06Y`, `CVTRA01Y`, `CVACT01Y`, `CVACT03Y` |
| Scheduler | Control-M `DAILY-TransactionBackup` folder (CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL) |
| Complexity | **High** — core financial processing with multi-file updates |

**Description**: The heart of the batch cycle. `CBTRN02C` reads daily transactions, validates card numbers against the cross-reference, updates account balances, writes to the transaction master VSAM, and maintains transaction category balances. Rejected transactions go to a GDG. Control-M orchestrates the daily sequence: close CICS files → backup → wait → reopen.

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|:---------|:---:|:----------|
| **Refactor** | **Recommended** | Core financial logic with subtle balance-update and rejection rules. Refactor to Spring Batch with the same sequential processing semantics. Preserve the exact validation/rejection logic. The VSAM-to-RDBMS migration is handled separately; this refactoring focuses on the processing pipeline. |
| Strangler Fig | Good | Could introduce a new batch processor alongside the old one with dual-write reconciliation, but the file-based I/O makes parallel running complex. |
| Replatform | Risky | Automated translation of batch COBOL with complex file handling (GDGs, VSAM sequential+random access) produces brittle code. |
| Rewrite | Risky | 731 LOC of financial balance-update logic. Subtle rounding and category-balance rules could be lost. Requires extensive reconciliation testing. |

---

### FA-7: Interest Calculation & Financial Processing

| Attribute | Detail |
|:----------|:-------|
| Programs | `CBACT04C` (652 LOC) |
| JCL | INTCALC |
| Data stores | TCATBALF VSAM KSDS, CARDXREF VSAM KSDS (+ AIX), ACCTDATA VSAM KSDS, DISCGRP VSAM KSDS, TRANSACT (sequential output) |
| Copybooks | `CVTRA01Y`, `CVTRA02Y`, `CVACT01Y`, `CVACT03Y` |
| Scheduler | Control-M `MONTHLY-InterestCalculation` folder (CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL) |
| Complexity | **High** — financial calculation with disclosure group rules |

**Description**: Monthly interest calculator. Reads transaction category balances, looks up disclosure group rates, computes interest per account using cross-reference AIX (alternate index), and updates account balances. Uses VSAM alternate index path for account-to-card lookups. Generates system transactions for computed interest.

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|:---------|:---:|:----------|
| **Refactor** | **Recommended** | Critical financial logic with disclosure-group-based rate lookup. Refactor to a Spring Batch job with the exact same calculation semantics. The COMP-3 packed decimal arithmetic must be carefully mapped to Java `BigDecimal`. VSAM AIX pattern maps to SQL JOINs. |
| Strangler Fig | Fair | Could run old and new calculations in parallel for reconciliation, but the monthly batch window is tight. |
| Replatform | Risky | Packed decimal (COMP-3) and VSAM AIX access patterns are error-prone in automated translation. |
| Rewrite | Risky | Financial calculations are high-risk for rewrite. Interest calculation errors have direct monetary impact. |

---

### FA-8: Reporting & Statements

| Attribute | Detail |
|:----------|:-------|
| Programs | `CORPT00C` (649 LOC — online report request), `CBTRN03C` (649 LOC — batch report), `CBSTM03A` (924 LOC — statement generation), `CBSTM03B` (230 LOC — I/O subroutine) |
| JCL | TRANREPT, CREASTMT |
| Data stores | TRANSACT, CARDXREF, TRANTYPE, TRANCATG, CUSTDATA, ACCTDATA |
| Complexity | Medium |

**Description**: Report generation in two modes: (1) `CORPT00C` collects report parameters via CICS and submits batch JCL via internal reader; (2) `CBSTM03A` generates account statements in both plain text and HTML formats. `CBSTM03A` exercises legacy patterns: `ALTER/GO TO`, `COMP/COMP-3`, 2D arrays, and subroutine calls (to `CBSTM03B`).

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|:---------|:---:|:----------|
| **Rewrite** | **Recommended** | Statement/report generation maps naturally to modern templating engines (Thymeleaf, JasperReports). The legacy patterns (ALTER/GO TO, 2D arrays) are intentionally complex for testing tooling — they add no business value. HTML output already exists, confirming the intent for modern rendering. |
| Strangler Fig | Good | Could route report requests to a new reporting service while keeping old batch reports operational. |
| Replatform | Poor | ALTER/GO TO patterns and subroutine I/O delegation produce nearly unmaintainable translated code. |
| Refactor | Fair | The I/O subroutine pattern (`CBSTM03B`) is worth understanding but not preserving. |

---

### FA-9: Bill Payment

| Attribute | Detail |
|:----------|:-------|
| Programs | `COBIL00C` (572 LOC) |
| Data stores | TRANSACT VSAM KSDS, ACCTDATA VSAM KSDS, CXACAIX (XREF AIX) |
| Copybooks | `CVACT01Y`, `CVACT03Y`, `CVTRA05Y` |
| BMS maps | COBIL00 |
| CICS transactions | CB00 |
| Complexity | Medium |

**Description**: Online bill payment. Allows paying the full account balance. Creates a payment transaction record, updates the account balance, and requires user confirmation before posting. Uses the XREF alternate index to look up account by account ID.

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|:---------|:---:|:----------|
| **Strangler Fig** | **Recommended** | Build a Payment Service API that coordinates with the Account and Transaction services. The confirmation workflow maps to a two-step API (initiate → confirm). Once Account and Transaction services exist, bill payment becomes an orchestrating service. |
| Replatform | Fair | Simple enough for automated translation but misses modern payment patterns (idempotency, event sourcing). |
| Refactor | Good | Confirmation logic is a good candidate for refactoring into a stateless service with optimistic locking. |
| Rewrite | Good | 572 LOC; manageable rewrite. |

---

### FA-10: User Administration

| Attribute | Detail |
|:----------|:-------|
| Programs | `COUSR00C` (695 LOC), `COUSR01C` (299 LOC), `COUSR02C` (414 LOC), `COUSR03C` (359 LOC) |
| Data stores | USRSEC VSAM KSDS |
| Copybooks | `CSUSR01Y` |
| BMS maps | COUSR00, COUSR01, COUSR02, COUSR03 |
| CICS transactions | CU00 (List), CU01 (Add), CU02 (Update), CU03 (Delete) |
| Complexity | Low-Medium |

**Description**: Admin-only CRUD for user records. List supports pagination (10 records per page). 80-byte user records with ID, name, password, and type. Straightforward VSAM browse/read/write/delete operations.

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|:---------|:---:|:----------|
| **Rewrite** | **Recommended** | Standard CRUD with pagination. Modern identity management (Keycloak, AWS Cognito, or Spring Security) replaces this entirely. No complex business logic to preserve — the 80-byte record layout is the complete specification. |
| Strangler Fig | Fair | Could wrap behind a new user API, but the VSAM-backed auth model should be retired, not preserved. |
| Replatform | Poor | Translating CICS browse operations for user lists produces poor-quality code when modern IAM solutions exist. |
| Refactor | Poor | The entire authentication model should be replaced, not incrementally refactored. |

---

### FA-11: Branch Migration (Export/Import)

| Attribute | Detail |
|:----------|:-------|
| Programs | `CBEXPORT` (582 LOC), `CBIMPORT` (487 LOC) |
| Data stores | All master files (CUSTDATA, ACCTDATA, CARDXREF, TRANSACT, CARDDATA) + Export file |
| Copybooks | `CVEXPORT` (multi-record layout with REDEFINES, COMP, COMP-3) |
| JCL | CBEXPORT, CBIMPORT |
| Complexity | Medium |

**Description**: Data migration subsystem. Export reads all master files and creates a multi-record sequential export file with different record types (Customer=`C`, Account=`A`, Transaction=`T`, Card XREF=`X`). Import reverses the process with validation checksums. The `CVEXPORT` copybook uses REDEFINES for polymorphic record types and mixed COMP/COMP-3 storage.

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|:---------|:---:|:----------|
| **Rewrite** | **Recommended** | The export/import pattern is a data migration utility. Replace with modern ETL (Spring Batch, AWS Glue, or database-native export). The REDEFINES/COMP-3 layout is a data format, not business logic. Once data is in an RDBMS, standard SQL export/import replaces this entirely. |
| Strangler Fig | Fair | Could create a new export API alongside the old one during transition. |
| Replatform | Poor | REDEFINES with COMP-3 fields is notoriously error-prone in automated translation. |
| Refactor | Poor | The entire paradigm (multi-record sequential files) is obsolete once data is in a relational database. |

---

### FA-12: Authorization Processing (Optional Module — IMS/DB2/MQ)

| Attribute | Detail |
|:----------|:-------|
| Programs | `COPAUA0C`, `COPAUS0C`, `COPAUS1C`, `COPAUS2C`, `CBPAUP0C`, `PAUDBLOD`, `PAUDBUNL`, `DBUNLDGS` (8 programs) |
| Data stores | IMS DB (DBPAUTP0, DBPAUTX0), DB2 tables (AUTHFRDS), MQ queues |
| Copybooks | `CIPAUDTY`, `CCPAURQY`, `CCPAURLY`, `CCPAUERY`, `CIPAUSMY`, `IMSFUNCS` |
| BMS maps | COPAU00, COPAU01 |
| CICS transactions | CPVS (Summary), CPVD (Details), CP00 (Process Auth) |
| Complexity | **Very High** — IMS + DB2 + MQ + CICS integration |

**Description**: Credit card authorization with MQ-triggered processing. Authorization requests arrive via MQ, are processed against IMS databases for customer data, and logged to DB2. CICS screens display pending authorizations. Batch job purges expired authorizations. This module demonstrates the most complex integration patterns in the system.

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|:---------|:---:|:----------|
| **Rewrite** | **Recommended** | The IMS/DB2/MQ integration patterns have no direct modern equivalent worth preserving. Rewrite as an event-driven authorization microservice using Kafka/SQS for messaging, a relational DB for persistence, and REST/gRPC for synchronous lookups. The business rules (authorization decision, expiry purge) are well-documented in the copybooks. |
| Strangler Fig | Good | MQ provides a natural integration seam — route auth requests to a new service while keeping IMS/DB2 operational during transition. |
| Replatform | Poor | IMS DL/I calls and MQ trigger patterns cannot be meaningfully auto-translated. |
| Refactor | Poor | The technology stack (IMS+MQ+DB2+CICS) is the problem, not the code structure. |

---

### FA-13: Reference Data Management

| Attribute | Detail |
|:----------|:-------|
| Programs | (IDCAMS utilities via JCL; optional DB2 programs COTRTUPC, COTRTLIC) |
| JCL | TRANTYPE, TRANCATG, DISCGRP, TCATBALF |
| Data stores | TRANTYPE VSAM, TRANCATG VSAM, DISCGRP VSAM, TCATBALF VSAM; optional DB2 tables |
| Copybooks | `CVTRA01Y`–`CVTRA04Y` |
| Scheduler | Control-M `WEEKLY-TransactionTypesDBRefresh`, `WEEKLY-DisclosureGroupsRefresh` |
| Complexity | Low |

**Description**: Static reference data: transaction types (10-byte records), transaction categories (60-byte), disclosure groups (50-byte), and category balances (50-byte). Weekly Control-M jobs refresh VSAM files from DB2 extracts. Optional DB2 module provides CICS screens for managing transaction types.

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|:---------|:---:|:----------|
| **Rewrite** | **Recommended** | Simple reference data tables. Migrate to relational DB tables with a Spring Data JPA CRUD interface or admin UI. The IDCAMS REPRO/DEFINE operations become SQL DDL/DML. Weekly refresh cycle can be replaced by direct DB access. |
| Strangler Fig | Fair | Possible but over-engineered for simple lookup tables. |
| Replatform | Fair | Straightforward data, but IDCAMS JCL doesn't translate. |
| Refactor | Fair | DB2 module already demonstrates the target pattern. |

---

### FA-14: Batch Infrastructure (File Management & Scheduling)

| Attribute | Detail |
|:----------|:-------|
| Programs | `COBSWAIT` (41 LOC — assembler wait wrapper) |
| JCL | CLOSEFIL, OPENFIL, WAITSTEP, ESDSRRDS, DEFGDGB, DEFGDGD |
| Scheduler | Control-M (all folders use CLOSEFIL/OPENFIL/WAITSTEP patterns) |
| Complexity | Low (infrastructure, not business logic) |

**Description**: CICS file open/close coordination, GDG management, VSAM dataset lifecycle (IDCAMS DEFINE/DELETE/REPRO), and batch wait steps. These are infrastructure patterns required by the mainframe architecture — they have no business logic.

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|:---------|:---:|:----------|
| **Retire** | **Recommended** | These programs exist solely due to mainframe constraints (CICS file exclusivity, VSAM lifecycle management). On a modern platform, database connections are managed by connection pools, file locking is handled by the OS/DB, and GDGs are replaced by timestamped storage. Replace Control-M with Spring Cloud Data Flow, Airflow, or AWS Step Functions. |
| All other strategies | N/A | No business logic to preserve, translate, or refactor. |

---

## Strategy Summary Matrix

| Functional Area | Recommended Strategy | LOC | Risk Level | Priority |
|:----------------|:---------------------|----:|:----------:|:--------:|
| FA-14: Batch Infrastructure | Retire | 41 | Very Low | Phase 0 |
| FA-13: Reference Data | Rewrite | ~200 | Low | Phase 1 |
| FA-10: User Admin | Rewrite | 1,767 | Low | Phase 1 |
| FA-1: Authentication | Strangler Fig | 260 | Low | Phase 1 |
| FA-2: Menu/Routing | Rewrite | 596 | Low | Phase 1 |
| FA-8: Reporting | Rewrite | 2,452 | Medium | Phase 2 |
| FA-11: Branch Migration | Rewrite | 1,069 | Medium | Phase 2 |
| FA-5: Transactions (Online) | Strangler Fig | 1,812 | Medium | Phase 2 |
| FA-9: Bill Payment | Strangler Fig | 572 | Medium | Phase 3 |
| FA-4: Credit Card Mgmt | Strangler Fig | 3,906 | Medium-High | Phase 3 |
| FA-3: Account Management | Strangler Fig | 5,177 | High | Phase 3 |
| FA-6: Transaction Batch | Refactor | 1,225 | High | Phase 4 |
| FA-7: Interest Calculation | Refactor | 652 | High | Phase 4 |
| FA-12: Authorization (IMS/DB2/MQ) | Rewrite | ~2,000 | Very High | Phase 5 |

---

## Technology Target State

| Current | Target |
|:--------|:-------|
| COBOL | Java 17+ / Spring Boot 3 |
| CICS | Spring MVC REST APIs + SPA frontend |
| VSAM KSDS | PostgreSQL / Amazon RDS |
| BMS Maps / 3270 | React or Angular SPA |
| JCL Batch | Spring Batch |
| Control-M | Spring Cloud Data Flow / Apache Airflow |
| VSAM AIX | SQL JOINs / indexes |
| GDG | Timestamped S3 objects or DB partitions |
| COMP-3 / Packed Decimal | Java `BigDecimal` |
| IMS DB | PostgreSQL (relational mapping) |
| IBM MQ | Apache Kafka / Amazon SQS |
| DB2 | PostgreSQL (or retain DB2 on cloud) |
| RACF | Spring Security + OAuth2/OIDC |
| EBCDIC | UTF-8 |
