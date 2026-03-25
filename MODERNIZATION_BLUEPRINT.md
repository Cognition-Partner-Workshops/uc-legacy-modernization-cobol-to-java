# CardDemo Modernization Blueprint

## Executive Summary

This document evaluates four modernization strategies -- **Strangler Fig**, **Replatform**, **Refactor**, and **Rewrite** -- for each functional area of the AWS CardDemo mainframe application. CardDemo is a COBOL/CICS/VSAM credit card management system comprising 31 online and batch programs, 30 copybooks, 17 BMS screen maps, and 38 JCL batch jobs. The recommended approach is a **hybrid strategy** that applies the most appropriate technique per functional area, prioritizing business continuity and risk reduction.

---

## 1. Application Landscape

### 1.1 Technology Stack

| Layer | Technology | Count |
|-------|-----------|-------|
| Online Programs | COBOL / CICS | 16 programs (CO\* prefix) |
| Batch Programs | COBOL / Batch | 15 programs (CB\* prefix) |
| Data Structures | COBOL Copybooks | 30 copybooks (CV\*, CS\*, CO\* prefix) |
| Screen Maps | BMS (3270 Terminal) | 17 maps |
| Job Control | JCL | 38 jobs |
| Data Storage | VSAM (KSDS, ESDS, RRDS) | 7+ datasets |
| Optional Modules | IMS DB, DB2, MQ | 3 modules (13 additional programs) |

### 1.2 Functional Areas Identified

| ID | Functional Area | Programs | Type | Complexity |
|----|----------------|----------|------|------------|
| FA-01 | Authentication & Security | COSGN00C, COUSR00C-03C | Online (CICS) | Medium |
| FA-02 | Menu Navigation | COMEN01C, COADM01C | Online (CICS) | Low |
| FA-03 | Account Management | COACTVWC, COACTUPC | Online (CICS) | High |
| FA-04 | Card Management | COCRDLIC, COCRDSLC, COCRDUPC | Online (CICS) | High |
| FA-05 | Transaction Management | COTRN00C, COTRN01C, COTRN02C | Online (CICS) | High |
| FA-06 | Bill Payment | COBIL00C | Online (CICS) | Medium |
| FA-07 | Reporting | CORPT00C, CBTRN03C | Online + Batch | Medium |
| FA-08 | Transaction Posting (Batch) | CBTRN02C | Batch | High |
| FA-09 | Interest Calculation (Batch) | CBACT04C | Batch | High |
| FA-10 | Statement Generation (Batch) | CBSTM03A, CBSTM03B | Batch | High |
| FA-11 | Data Export/Import (Batch) | CBEXPORT, CBIMPORT | Batch | Low |
| FA-12 | Data Refresh & Utilities | CBACT01C-03C, CBCUS01C, COBSWAIT, CSUTLDTC | Batch | Low |
| FA-13 | Authorization Module (Optional) | COPAUA0C, COPAUS0C-2C, CBPAUP0C | IMS/DB2/MQ | High |
| FA-14 | Transaction Type DB2 (Optional) | COTRTLIC, COTRTUPC, COBTUPDT | DB2 | Medium |
| FA-15 | VSAM-MQ Integration (Optional) | CODATE01, COACCT01 | VSAM/MQ | Medium |

### 1.3 Data Model (VSAM Datasets)

| Dataset | Copybook | Record Length | Key | Description |
|---------|----------|--------------|-----|-------------|
| USRSEC | CSUSR01Y | 80 bytes | User ID (8) | User security credentials |
| ACCTDAT | CVACT01Y | 300 bytes | Account ID (11) | Account master |
| CARDDAT | CVACT02Y | 150 bytes | Card Number (16) | Card master |
| CARDXREF | CVACT03Y | 50 bytes | Card Number (16) | Card-to-Account-to-Customer cross-reference |
| CUSTDAT | CVCUS01Y | 500 bytes | Customer ID (9) | Customer master |
| TRANSACT | CVTRA05Y | 350 bytes | Transaction ID (16) | Transaction master |
| DALYTRAN | CVTRA06Y | 350 bytes | Sequential | Daily transactions for batch posting |
| TCATBALF | CVTRA01Y | 50 bytes | Acct+Type+Cat (17) | Transaction category balances |
| DISCGRP | CVTRA02Y | 50 bytes | Group+Type+Cat (16) | Discount/interest rate groups |

---

## 2. Strategy Evaluation by Functional Area

### 2.1 Strategy Definitions

| Strategy | Description | When to Use |
|----------|-------------|-------------|
| **Strangler Fig** | Incrementally replace components behind a facade; old and new run side-by-side | When gradual migration reduces risk; loose coupling exists |
| **Replatform** | Move code to a new runtime with minimal changes (e.g., COBOL-to-Java automated conversion) | When logic is stable, well-understood, and not changing |
| **Refactor** | Restructure code while preserving behavior; modernize architecture incrementally | When business logic is sound but architecture is outdated |
| **Rewrite** | Build from scratch using modern frameworks | When existing code is unmaintainable or requirements have fundamentally changed |

### 2.2 FA-01: Authentication & Security

| Criterion | Assessment |
|-----------|-----------|
| **Current State** | COSGN00C reads USRSEC VSAM file, compares plaintext passwords, routes to admin or user menu. COUSR00C-03C provide admin CRUD for user records. |
| **Complexity** | Medium (261 lines signon + 4 user-management programs) |
| **Data Coupling** | USRSEC VSAM file only; minimal coupling to other areas |
| **Business Rule Density** | Low -- straightforward credential lookup and role routing |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | **Low** | Security is a cross-cutting concern; partial migration creates two auth systems |
| Replatform | **Low** | Plaintext password storage must be replaced, not preserved |
| Refactor | **Low** | Fundamental security model is inadequate for modern standards |
| **Rewrite** | **Recommended** | Replace with Spring Security / OAuth2 / JWT. Modern identity management requires fundamentally different architecture (hashed passwords, token-based auth, RBAC) |

### 2.3 FA-02: Menu Navigation

| Criterion | Assessment |
|-----------|-----------|
| **Current State** | COMEN01C (regular user) and COADM01C (admin) render BMS menus and route to sub-programs via XCTL. Menu options defined in copybooks COMEN02Y and COADM02Y. |
| **Complexity** | Low (309 + 289 lines) |
| **Data Coupling** | None -- pure navigation logic reading menu-option arrays |
| **Business Rule Density** | Minimal -- option validation and role-based filtering |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | **Low** | Navigation is tightly coupled to the 3270 UI paradigm |
| Replatform | **Low** | BMS maps have no equivalent in web UIs |
| Refactor | **Low** | Navigation patterns are fundamentally different in web/REST |
| **Rewrite** | **Recommended** | Replace with a modern web frontend (React/Angular) or REST API routing layer. Menu navigation is purely a UI concern that maps naturally to modern SPA routing or API gateway patterns |

### 2.4 FA-03: Account Management

| Criterion | Assessment |
|-----------|-----------|
| **Current State** | COACTVWC (942 lines) handles account view with lookups across ACCTDAT, CUSTDAT, CARDDAT, and CARDXREF. COACTUPC (4,237 lines) handles account update with extensive field-level validation (SSN, phone, dates, credit limits, FICO scores). |
| **Complexity** | High -- COACTUPC is the largest program in the system |
| **Data Coupling** | High -- reads/writes ACCTDAT, CARDDAT, CARDXREF (via alternate index CXACAIX), CUSTDAT |
| **Business Rule Density** | Very High -- 40+ validation rules for account fields |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Build new Account microservice behind API gateway; route traffic gradually. View operations can migrate first, then updates |
| Replatform | Medium | Automated conversion preserves validation logic but produces hard-to-maintain Java |
| Refactor | Medium | Validation rules are valuable but deeply embedded in screen-handling logic |
| Rewrite | Medium | Risk of losing nuanced validation rules (date logic, SSN validation, etc.) |

### 2.5 FA-04: Card Management

| Criterion | Assessment |
|-----------|-----------|
| **Current State** | COCRDLIC (1,460 lines) lists cards with pagination, filtering by account, and selection for view/update. COCRDSLC views card details. COCRDUPC updates card records. All operate on CARDDAT with CARDAIX alternate index. |
| **Complexity** | High -- pagination logic, alternate index browsing |
| **Data Coupling** | CARDDAT, CARDAIX (alternate index by account) |
| **Business Rule Density** | Medium -- card status validation, CVV handling, embossed name rules |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Extract as Card Service behind API; reads can migrate before writes. Natural bounded context with clear data ownership |
| Replatform | Medium | VSAM browse/pagination logic maps awkwardly to RDBMS queries |
| Refactor | Medium | Pagination and browse patterns need fundamental redesign for REST |
| Rewrite | Medium | Business rules are moderate; extraction seam is cleaner than rewrite |

### 2.6 FA-05: Transaction Management

| Criterion | Assessment |
|-----------|-----------|
| **Current State** | COTRN00C (700 lines) lists transactions with pagination via TRANSACT VSAM browse. COTRN01C views transaction detail. COTRN02C adds new transactions. Transactions are the core data entity linking to accounts, cards, and merchants. |
| **Complexity** | High -- central to the business; cross-references accounts, cards |
| **Data Coupling** | Very High -- TRANSACT, ACCTDAT, CARDXREF, CXACAIX |
| **Business Rule Density** | High -- transaction creation, ID generation, timestamp handling |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Core domain; needs careful incremental migration. Read path (list/view) migrates first. Write path (add) migrates after account and card services are stable |
| Replatform | Low | Transaction ID generation and timestamp logic require manual rework |
| Refactor | Medium | Business logic is sound but interleaved with CICS I/O |
| Rewrite | Medium | Risk of data inconsistency during dual-write period |

### 2.7 FA-06: Bill Payment

| Criterion | Assessment |
|-----------|-----------|
| **Current State** | COBIL00C (573 lines) handles full-balance payment by creating a transaction record and updating account balance atomically (CICS READ UPDATE + REWRITE). Generates unique transaction IDs by reading the last TRANSACT key. |
| **Complexity** | Medium -- but involves multi-file atomic updates |
| **Data Coupling** | TRANSACT, ACCTDAT, CXACAIX -- writes to both transaction and account files |
| **Business Rule Density** | Medium -- balance check, payment confirmation, transaction creation |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Medium | Depends on transaction and account services being available |
| Replatform | Low | Atomic cross-file updates don't map to automated conversion |
| **Refactor** | **Recommended** | Extract payment logic into a Payment Service that orchestrates Transaction and Account services. The business rules (balance check, confirmation flow) are sound and should be preserved |
| Rewrite | Medium | Payment flows are well-understood; refactoring is safer |

### 2.8 FA-07: Reporting

| Criterion | Assessment |
|-----------|-----------|
| **Current State** | CORPT00C (650 lines) is an online CICS program that submits batch JCL jobs to the internal reader for transaction reports (monthly, yearly, custom date range). CBTRN03C generates the actual batch report. |
| **Complexity** | Medium -- JCL submission from CICS via TDQ, date validation |
| **Data Coupling** | TRANSACT (read-only), internal reader (TDQ for JCL submission) |
| **Business Rule Density** | Low -- date range validation, report parameter construction |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Medium | Reports can run independently of transactional systems |
| Replatform | Low | JCL submission via TDQ has no modern equivalent |
| Refactor | Low | Report triggering mechanism is mainframe-specific |
| **Rewrite** | **Recommended** | Replace with modern reporting (Spring Batch + JasperReports / database views + BI tools). Reporting requirements are well-defined and the mainframe-specific JCL submission pattern must be completely replaced |

### 2.9 FA-08: Transaction Posting (Batch)

| Criterion | Assessment |
|-----------|-----------|
| **Current State** | CBTRN02C (732 lines) reads daily transactions (DALYTRAN), validates against XREF and ACCTDAT, posts valid transactions to TRANSACT, writes rejects to DALYREJS, and updates TCATBALF category balances. Core batch cycle step. |
| **Complexity** | High -- multi-file I/O, validation pipeline, reject handling |
| **Data Coupling** | Very High -- DALYTRAN, TRANSACT, XREFFILE, ACCTFILE, TCATBALF, DALYREJS |
| **Business Rule Density** | High -- transaction validation rules, balance updates, reject tracking |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Low | Batch processing is all-or-nothing per cycle |
| **Replatform** | **Recommended** | Convert to Spring Batch with minimal logic changes. The sequential read-validate-post pattern maps directly to Spring Batch's chunk-oriented processing (ItemReader/ItemProcessor/ItemWriter). Validation and reject logic can be preserved nearly 1:1 |
| Refactor | Medium | Good fit but more effort than replatform for same outcome |
| Rewrite | Low | High risk of introducing bugs in critical financial posting |

### 2.10 FA-09: Interest Calculation (Batch)

| Criterion | Assessment |
|-----------|-----------|
| **Current State** | CBACT04C (653 lines) reads TCATBALF sequentially, groups by account, looks up interest rates from DISCGRP, computes monthly interest, updates account balances, and writes interest transactions. Accepts date parameter. |
| **Complexity** | High -- financial calculation logic, multi-file lookups, account grouping |
| **Data Coupling** | Very High -- TCATBALF, XREFFILE, DISCGRP, ACCTFILE, TRANSACT |
| **Business Rule Density** | Very High -- interest rate lookup, monthly computation, fee calculation, balance updates |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Low | Batch calculation is atomic per cycle |
| **Replatform** | **Recommended** | Convert to Spring Batch. Financial calculation logic must be preserved exactly -- automated conversion with careful testing is safer than rewrite. COMP and COMP-3 arithmetic maps to Java BigDecimal |
| Refactor | Medium | Risk of introducing rounding differences |
| Rewrite | Low | Very high risk for financial calculations; COBOL decimal arithmetic has subtle behaviors |

### 2.11 FA-10: Statement Generation (Batch)

| Criterion | Assessment |
|-----------|-----------|
| **Current State** | CBSTM03A (924 lines) generates account statements in both plain text and HTML formats. Uses 2D arrays, ALTER/GO TO, mainframe control block addressing (PSA/TCB/TIOT), and CALL to subroutine CBSTM03B for file I/O. |
| **Complexity** | Very High -- most complex batch program; uses ALTER statements, pointer manipulation, mainframe control blocks |
| **Data Coupling** | TRANSACT (via CBSTM03B), XREFFILE, CUSTDAT, ACCTDAT |
| **Business Rule Density** | Medium -- statement formatting, aggregation by card |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Low | Standalone batch output generation |
| Replatform | Low | ALTER/GO TO, PSA/TCB/TIOT addressing, and pointer manipulation make automated conversion unreliable |
| Refactor | Low | Code structure (ALTER, GO TO) is not refactorable in place |
| **Rewrite** | **Recommended** | Replace with Spring Batch + modern templating (Thymeleaf/FreeMarker for HTML, plain text formatter). The ALTER/GO TO patterns and mainframe control block access make this the hardest program to convert automatically. Statement format requirements are well-documented in the existing output |

### 2.12 FA-11: Data Export/Import (Batch)

| Criterion | Assessment |
|-----------|-----------|
| **Current State** | CBEXPORT and CBIMPORT handle bulk data extraction and loading across VSAM files. Straightforward sequential file processing. |
| **Complexity** | Low -- sequential read/write operations |
| **Data Coupling** | All VSAM datasets (read for export, write for import) |
| **Business Rule Density** | Minimal -- data movement only |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Low | Utility function, not incremental |
| **Replatform** | **Recommended** | Convert to Spring Batch readers/writers targeting RDBMS. Simple I/O patterns convert cleanly |
| Refactor | Low | Nothing to refactor; logic is trivial |
| Rewrite | Low | Unnecessary effort for simple data movement |

### 2.13 FA-12: Data Refresh & Utilities

| Criterion | Assessment |
|-----------|-----------|
| **Current State** | CBACT01C-03C refresh account, card, and cross-reference files. CBCUS01C refreshes customer data. COBSWAIT is a wait utility. CSUTLDTC validates dates. Driven by JCL jobs (ACCTFILE, CARDFILE, CUSTFILE, etc.). |
| **Complexity** | Low -- simple file operations |
| **Data Coupling** | Individual VSAM datasets per program |
| **Business Rule Density** | Minimal -- data loading, date validation |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Low | Utility programs |
| **Replatform** | **Recommended** | Convert to database migration scripts / Spring Batch jobs. Date utility becomes a Java utility class |
| Refactor | Low | Too simple to warrant refactoring |
| Rewrite | Low | Unnecessary effort |

### 2.14 FA-13: Authorization Module (Optional -- IMS/DB2/MQ)

| Criterion | Assessment |
|-----------|-----------|
| **Current State** | MQ-triggered authorization with IMS DB storage and DB2 fraud marking. COPAUA0C processes MQ messages, COPAUS0C/1C display summaries/details, COPAUS2C marks fraud to DB2, CBPAUP0C purges old records. |
| **Complexity** | High -- multi-middleware integration (IMS + DB2 + MQ) |
| **Data Coupling** | IMS DB, DB2 tables, MQ queues |
| **Business Rule Density** | High -- fraud detection workflow |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Deploy as a standalone Authorization microservice. MQ triggers map to event-driven architecture (Kafka/RabbitMQ). DB2 tables migrate to RDBMS. IMS segments map to JPA entities |
| Replatform | Low | IMS DB has no direct modern equivalent |
| Refactor | Medium | Business rules are valuable but middleware is obsolete |
| Rewrite | Medium | Complex middleware interactions warrant careful incremental approach |

### 2.15 FA-14: Transaction Type DB2 (Optional)

| Criterion | Assessment |
|-----------|-----------|
| **Current State** | COTRTLIC lists/deletes transaction types from DB2. COTRTUPC adds/edits types. COBTUPDT batch-updates types. Uses embedded SQL with cursors. |
| **Complexity** | Medium -- standard DB2 CRUD |
| **Data Coupling** | DB2 transaction type table |
| **Business Rule Density** | Low -- straightforward CRUD with cursor-based pagination |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Low | Simple CRUD, not worth incremental migration |
| **Replatform** | **Recommended** | Embedded SQL maps directly to JPA/JDBC. DB2 tables migrate to PostgreSQL/MySQL with minimal schema changes. Cursor patterns become JPA pagination |
| Refactor | Low | Already well-structured CRUD |
| Rewrite | Low | Unnecessary effort for standard CRUD |

### 2.16 FA-15: VSAM-MQ Integration (Optional)

| Criterion | Assessment |
|-----------|-----------|
| **Current State** | CODATE01 responds to MQ requests with system date. COACCT01 responds with account inquiry data. Request/response over MQ queues. |
| **Complexity** | Medium -- MQ request/response pattern |
| **Data Coupling** | MQ queues, ACCTDAT (for account inquiry) |
| **Business Rule Density** | Low -- data retrieval and formatting |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Medium | Can be replaced with REST endpoints |
| Replatform | Low | MQ-specific patterns need redesign |
| **Rewrite** | **Recommended** | Replace with REST API endpoints (Spring Boot). System date becomes a trivial endpoint. Account inquiry integrates with the new Account Service |
| Refactor | Low | MQ patterns don't refactor to REST |

---

## 3. Strategy Summary Matrix

| Functional Area | Recommended Strategy | Risk Level | Effort | Priority |
|----------------|---------------------|------------|--------|----------|
| FA-01 Authentication & Security | **Rewrite** | Low | Medium | Phase 1 |
| FA-02 Menu Navigation | **Rewrite** | Low | Low | Phase 1 |
| FA-12 Data Refresh & Utilities | **Replatform** | Low | Low | Phase 1 |
| FA-11 Data Export/Import | **Replatform** | Low | Low | Phase 2 |
| FA-14 Transaction Type DB2 | **Replatform** | Low | Low | Phase 2 |
| FA-07 Reporting | **Rewrite** | Medium | Medium | Phase 2 |
| FA-15 VSAM-MQ Integration | **Rewrite** | Low | Low | Phase 2 |
| FA-04 Card Management | **Strangler Fig** | Medium | Medium | Phase 3 |
| FA-03 Account Management | **Strangler Fig** | Medium | High | Phase 3 |
| FA-06 Bill Payment | **Refactor** | Medium | Medium | Phase 3 |
| FA-05 Transaction Management | **Strangler Fig** | High | High | Phase 4 |
| FA-08 Transaction Posting | **Replatform** | High | High | Phase 4 |
| FA-09 Interest Calculation | **Replatform** | High | High | Phase 4 |
| FA-10 Statement Generation | **Rewrite** | High | High | Phase 4 |
| FA-13 Authorization Module | **Strangler Fig** | High | High | Phase 5 |

---

## 4. Target Architecture

### 4.1 Technology Mapping

| COBOL/Mainframe Component | Java/Modern Equivalent |
|--------------------------|----------------------|
| CICS Transactions | Spring Boot REST Controllers |
| BMS Screen Maps (3270) | React/Angular SPA + REST API |
| VSAM KSDS Files | PostgreSQL/MySQL Tables |
| VSAM Alternate Indexes | Database Secondary Indexes |
| COBOL Copybooks | Java POJOs / JPA Entities / DTOs |
| JCL Batch Jobs | Spring Batch Jobs + Cron/Scheduler |
| COBOL PERFORM | Java Method Calls |
| COBOL EVALUATE | Java switch / if-else chains |
| COBOL Working-Storage | Java Class Fields |
| CICS COMMAREA | HTTP Session / JWT Claims / Request DTOs |
| CICS XCTL/LINK | REST API Calls / Service Method Invocations |
| CICS READ/WRITE/REWRITE | JPA Repository Methods |
| CICS STARTBR/READNEXT/ENDBR | JPA Query with Pagination (Pageable) |
| MQ Queues | Kafka Topics / RabbitMQ Queues / REST Webhooks |
| IMS DB Segments | JPA Entities with Relationships |
| DB2 Embedded SQL | Spring Data JPA / JDBC Template |
| JCL SORT/MERGE | SQL ORDER BY / Java Streams |
| GDG (Generation Data Groups) | Timestamped file archives / S3 versioning |

### 4.2 Database Schema (Target)

The 7+ VSAM datasets consolidate into a relational schema:

```
users (user_id PK, first_name, last_name, password_hash, user_type, ...)
accounts (account_id PK, status, current_balance, credit_limit, cash_credit_limit,
          open_date, expiration_date, reissue_date, cycle_credit, cycle_debit,
          zip_code, group_id)
customers (customer_id PK, first_name, middle_name, last_name, address_line_1..3,
           state, country, zip, phone_1, phone_2, ssn, govt_id, dob,
           eft_account_id, primary_card_holder, fico_score)
cards (card_number PK, account_id FK, cvv, embossed_name, expiration_date, status)
card_xref (card_number PK, customer_id FK, account_id FK)
transactions (transaction_id PK, type_code, category_code, source, description,
              amount, merchant_id, merchant_name, merchant_city, merchant_zip,
              card_number FK, originated_ts, processed_ts)
transaction_category_balances (account_id+type+category PK, balance)
discount_groups (group_id+type+category PK, interest_rate, ...)
```

---

## 5. Key Recommendations

1. **Start with authentication rewrite** -- Modern security is non-negotiable and decouples all downstream work from the legacy security model.

2. **Build the API gateway early** -- A facade layer (Spring Cloud Gateway or similar) enables the Strangler Fig pattern for account, card, and transaction services.

3. **Preserve financial calculation precision** -- Use `java.math.BigDecimal` everywhere amounts are handled. COBOL's `PIC S9(10)V99` maps to `BigDecimal` with scale 2. Validate arithmetic parity with the legacy system using production-like test data.

4. **Convert batch jobs to Spring Batch** -- The COBOL batch cycle (CLOSEFIL -> refresh -> POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT -> OPENFIL) maps to a Spring Batch job flow with step dependencies.

5. **Dual-run during transition** -- For financial batch processing (FA-08, FA-09), run both old and new systems in parallel and reconcile outputs before cutover.

6. **Rewrite CBSTM03A (Statement Generation)** -- This program uses ALTER/GO TO, mainframe control block addressing, and pointer manipulation that defy automated conversion. It is the strongest candidate for a clean rewrite.

7. **Migrate VSAM to RDBMS early** -- Many programs share VSAM files. Migrating data to an RDBMS and providing both VSAM and JDBC access during transition is critical to the Strangler Fig approach.
