# CardDemo Modernization Blueprint

## Executive Summary

The CardDemo application is a mainframe-hosted credit card management system built with COBOL/CICS/VSAM/BMS. It comprises 31 COBOL programs (online + batch), 30 copybooks, 17 BMS screen maps, and 38 JCL batch jobs. This blueprint evaluates four modernization strategies for each functional area: **Strangler Fig**, **Replatform**, **Refactor**, and **Rewrite**.

---

## System Inventory

| Category | Count | Technology |
|----------|-------|------------|
| Online CICS Programs | 17 | COBOL / CICS |
| Batch Programs | 10 | COBOL / JCL |
| Copybooks | 30 | COBOL |
| BMS Screen Maps | 17 | BMS / 3270 |
| JCL Batch Jobs | 38 | JCL |
| VSAM Data Files | 7+ | KSDS / ESDS / RRDS |
| Optional Modules | 3 | IMS-DB2-MQ, DB2, VSAM-MQ |

**Total Lines of COBOL**: ~15,000+ across online and batch programs.

---

## Functional Areas

### 1. Authentication & User Security

| Artifact | Description |
|----------|-------------|
| `COSGN00C` | Sign-on screen, credential validation against USRSEC VSAM file |
| `CSUSR01Y` | User security record layout (80 bytes) |
| USRSEC VSAM | User credentials store (ID, name, password, type) |

**Complexity**: Low (261 lines, simple READ + compare logic)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Stand up a new authentication service (Spring Security / JWT) alongside the mainframe. Route new clients to the modern auth service while legacy 3270 sessions continue using USRSEC. Simplest seam to cut. |
| Replatform | Good | Direct translation of VSAM lookups to SQL queries is straightforward. Password storage must be upgraded to bcrypt/scrypt. |
| Refactor | Moderate | The program is small enough to refactor in-place, but the CICS EXEC commands would need complete replacement. |
| Rewrite | Overkill | Low complexity makes rewrite unnecessary; strangler or replatform is faster. |

---

### 2. Menu Navigation & Routing

| Artifact | Description |
|----------|-------------|
| `COMEN01C` | Regular user main menu (309 lines) |
| `COADM01C` | Admin user menu (289 lines) |
| `COMEN02Y` | Menu options configuration (11 options) |
| `COADM02Y` | Admin menu options configuration (6 options) |

**Complexity**: Low (menu dispatch via XCTL, role-based filtering)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Replace with a modern web UI routing layer (React/Angular). Menu configurations in `COMEN02Y` / `COADM02Y` map directly to REST endpoint routes. The strangler facade routes requests to either legacy or modernized backends. |
| Replatform | Good | Menus can be directly translated to a web controller + navigation config. |
| Refactor | Low value | Menu programs are thin dispatchers; refactoring gains little. |
| Rewrite | Acceptable | Web UIs are inherently a rewrite of 3270 BMS maps. This is the natural outcome of any strategy. |

---

### 3. Account Management

| Artifact | Description |
|----------|-------------|
| `COACTVWC` | Account view (942 lines) - reads ACCTDAT, CUSTDAT, CARDXREF |
| `COACTUPC` | Account update (4,237 lines) - complex validation, multi-file updates |
| `CVACT01Y` | Account record (300 bytes, 16 fields) |
| `CVCUS01Y` | Customer record (500 bytes, 19 fields) |
| `CVACT03Y` | Card cross-reference record (50 bytes) |

**Complexity**: High (`COACTUPC` is the largest program at 4,237 lines with extensive field-level validation including SSN, phone, dates, credit limits)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Moderate | High coupling between account, customer, and card data makes partial extraction complex. Need anti-corruption layer. |
| Replatform | Good | VSAM-to-RDBMS migration is well-understood. Record layouts in copybooks map cleanly to table schemas. |
| **Refactor** | **Recommended** | The extensive validation logic in `COACTUPC` (SSN, phone, dates, credit limits, status flags) represents significant business value. Refactor to Java POJOs + Bean Validation annotations, preserving every validation rule. Extract the 4,237-line monolith into Account, Customer, and Card service classes. |
| Rewrite | Risky | Volume of validation logic makes rewrite error-prone. High risk of losing edge-case business rules. |

---

### 4. Card Management

| Artifact | Description |
|----------|-------------|
| `COCRDLIC` | Card list with pagination (1,460 lines) |
| `COCRDSLC` | Card detail view |
| `COCRDUPC` | Card update |
| `CVACT02Y` | Card record (150 bytes) |
| `CVCRD01Y` | Card working storage variables |
| CARDDAT VSAM | Card master file |
| CARDAIX | Card alternate index (by account) |

**Complexity**: Medium (pagination logic, alternate index access, role-based filtering)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Card management has a clean boundary through the CARDDAT/CARDAIX files. A new Card microservice can expose REST APIs while reading from the same data source during transition. VSAM alternate index patterns map to SQL secondary indexes. |
| Replatform | Good | VSAM STARTBR/READNEXT pagination maps to SQL OFFSET/LIMIT or cursor-based pagination. |
| Refactor | Moderate | Pagination logic is CICS-specific (STARTBR/READNEXT/ENDBR). Refactoring requires rethinking the data access pattern entirely. |
| Rewrite | Acceptable | Card CRUD is a well-understood domain; rewrite risk is manageable if copybook record layouts are used as the contract. |

---

### 5. Transaction Processing (Online)

| Artifact | Description |
|----------|-------------|
| `COTRN00C` | Transaction list with pagination (700 lines) |
| `COTRN01C` | Transaction view |
| `COTRN02C` | Transaction add (online) |
| `CVTRA05Y` | Transaction record (350 bytes, 14 fields) |
| TRANSACT VSAM | Transaction master file |

**Complexity**: Medium (pagination, selection dispatch, numeric validation)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Online transaction screens are read-heavy. A new transaction query service can be stood up alongside the legacy system, reading from the same TRANSACT data (replicated to a modern DB). Write paths (COTRN02C) can be migrated later. |
| Replatform | Good | Transaction records have a well-defined layout. Direct migration to a relational model is clean. |
| Refactor | Moderate | Online programs are CICS-dependent; refactoring requires replacing the entire I/O and screen handling model. |
| Rewrite | Acceptable | Transaction list/view/add is standard CRUD. Rewrite is viable if test data from `app/data/ASCII/` is used for validation. |

---

### 6. Transaction Processing (Batch)

| Artifact | Description |
|----------|-------------|
| `CBTRN02C` | Daily transaction posting (732 lines) - reads DALYTRAN, validates via XREF, posts to TRANSACT, updates ACCTDAT, maintains TCATBAL |
| `CBACT04C` | Interest calculation (653 lines) - reads TCATBAL, computes interest per disclosure group, updates ACCTDAT, creates interest transactions |
| `CBSTM03A/B` | Statement generation |
| `CBTRN03C` | Transaction reporting |
| `CVTRA06Y` | Daily transaction record (350 bytes) |
| `CVTRA01Y` | Transaction category balance (50 bytes) |
| `CVTRA02Y` | Disclosure group / interest rate (50 bytes) |

**Complexity**: High (multi-file orchestration, cross-reference validation, balance updates, reject handling, interest calculation with disclosure groups)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Difficult | Batch jobs are deeply coupled to the file-based batch cycle (CLOSEFIL -> refresh -> POST -> INTEREST -> BACKUP -> COMBINE -> STATEMENT -> OPENFIL). Partial extraction breaks the cycle. |
| **Replatform** | **Recommended** | Map JCL batch jobs to Spring Batch jobs. VSAM sequential reads map to ItemReaders; validation + posting logic maps to ItemProcessors; file writes map to ItemWriters. The batch cycle order is preserved as a Spring Batch flow. JCL DD card mappings become Spring Batch resource configurations. |
| Refactor | Good | Business logic (validation in `1500-VALIDATE-TRAN`, interest computation in `1300-COMPUTE-INTEREST`) can be extracted and unit-tested independently. |
| Rewrite | Risky | The batch cycle has implicit ordering dependencies and file-locking semantics (CLOSEFIL/OPENFIL). Rewrite risks breaking the end-of-day processing pipeline. |

---

### 7. Bill Payment

| Artifact | Description |
|----------|-------------|
| `COBIL00C` | Bill payment processing (573 lines) - reads ACCTDAT, creates payment transaction in TRANSACT, updates account balance |
| ACCTDAT, TRANSACT, CXACAIX VSAM files |

**Complexity**: Medium (reads account, looks up card via XREF alternate index, generates transaction ID by reading last ID, creates payment record, updates balance)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Bill payment is a self-contained business operation with clear inputs (account ID, confirmation) and outputs (transaction record, updated balance). A modern payment service can be introduced alongside the legacy path. The transaction ID generation (READPREV on TRANSACT to get last ID + 1) must be replaced with a database sequence. |
| Replatform | Good | Straightforward CICS-to-REST mapping. The ASKTIME/FORMATTIME timestamp generation maps to `java.time`. |
| Refactor | Moderate | Business logic is clean but tightly coupled to CICS I/O commands. |
| Rewrite | Acceptable | Well-bounded scope makes rewrite manageable. |

---

### 8. Reporting

| Artifact | Description |
|----------|-------------|
| `CORPT00C` | Report submission (650 lines) - submits batch JCL via CICS TDQ (transient data queue) to internal reader |
| `CBTRN03C` | Batch transaction report generator |
| `CBSTM03A/B` | Statement generation programs |
| JCL: TRANREPT, CREASTMT | Report/statement job definitions |

**Complexity**: Medium-High (online-to-batch bridge via TDQ, JCL generation with parameterized date ranges, SYMNAMES for sort/filter)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Moderate | Reports can be generated from a modern data store while legacy batch continues running in parallel. |
| **Rewrite** | **Recommended** | The reporting subsystem is the most mainframe-specific component (TDQ submission, JCL internal reader, SYMNAMES sort specifications). Modern reporting tools (JasperReports, Apache POI, or a BI tool) provide far superior functionality. The parameterized date-range logic in `CORPT00C` maps to a simple REST endpoint with query parameters. |
| Replatform | Low fit | JCL-based report submission has no direct modern equivalent. Translation would produce an awkward hybrid. |
| Refactor | Low fit | TDQ/internal reader patterns are inherently mainframe-specific. |

---

### 9. User Administration (Admin)

| Artifact | Description |
|----------|-------------|
| `COUSR00C` | User list with pagination (696 lines) |
| `COUSR01C` | User add |
| `COUSR02C` | User update |
| `COUSR03C` | User delete |
| USRSEC VSAM | User security file |

**Complexity**: Medium (standard CRUD with pagination, selection dispatch for update/delete)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | User CRUD is a clean, self-contained domain. A modern User Management service can run in parallel, eventually replacing the VSAM-based USRSEC file with a proper identity store (LDAP, database-backed, or cloud IAM). |
| Replatform | Good | VSAM CRUD maps directly to JPA repository operations. |
| Refactor | Moderate | Programs are CICS-specific but the business logic is simple. |
| Rewrite | Acceptable | Low-risk given the simplicity of user CRUD. |

---

### 10. Optional Modules (IMS-DB2-MQ / DB2 / VSAM-MQ)

| Artifact | Description |
|----------|-------------|
| `app-authorization-ims-db2-mq/` | Authorization module using IMS DB, DB2, and MQ (8 programs) |
| `app-transaction-type-db2/` | Transaction type CRUD using DB2 (3 programs) |
| `app-vsam-mq/` | Account inquiry and system date via MQ request/response (2 programs) |

**Complexity**: High (multi-technology integration: IMS DB + DB2 + MQ)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Good | MQ-based modules already have message-based interfaces that can be replaced with modern event-driven patterns (Kafka, RabbitMQ). |
| **Rewrite** | **Recommended** | These modules involve IMS DB (hierarchical), DB2 (relational), and MQ (messaging) -- three distinct middleware technologies. Each must be replaced with modern equivalents. The authorization module should be rewritten as a microservice with a relational DB and event-driven fraud detection. Transaction type CRUD should be absorbed into the Account Management domain with standard JPA. |
| Replatform | Moderate | DB2 modules can be replatformed to a modern RDBMS. IMS DB requires a more fundamental redesign. |
| Refactor | Low fit | Three different middleware technologies make in-place refactoring impractical. |

---

## Data Migration Strategy

### VSAM-to-RDBMS Mapping

| VSAM File | Key | Record Size | Target Table | Notes |
|-----------|-----|-------------|-------------|-------|
| USRSEC | USR-ID (X8) | 80 bytes | `users` | Add password hashing, audit columns |
| ACCTDAT | ACCT-ID (9-11) | 300 bytes | `accounts` | Normalize address, add timestamps |
| CARDDAT | CARD-NUM (X16) | 150 bytes | `cards` | Foreign key to accounts |
| CUSTDAT | CUST-ID (9-9) | 500 bytes | `customers` | Normalize address, phone |
| TRANSACT | TRAN-ID (X16) | 350 bytes | `transactions` | Partition by date |
| CARDXREF | CARD-NUM (X16) | 50 bytes | Eliminated | Becomes FK relationships |
| TCATBALF | Compound key | 50 bytes | `transaction_category_balances` | Compound PK (acct+type+cat) |
| DISCGRP | Compound key | 50 bytes | `disclosure_groups` | Interest rate configuration |

### ASCII Test Data

Sample data in `app/data/ASCII/` provides the seed for migration testing and validation. Each file corresponds to a VSAM dataset and can be loaded into the target RDBMS for parallel-run verification.

---

## Strategy Summary Matrix

| Functional Area | Recommended Strategy | Complexity | LOC | Risk |
|----------------|---------------------|-----------|-----|------|
| Authentication | Strangler Fig | Low | 261 | Low |
| Menu/Navigation | Strangler Fig | Low | 598 | Low |
| Account Management | Refactor | High | 5,179 | High |
| Card Management | Strangler Fig | Medium | 1,460+ | Medium |
| Transaction (Online) | Strangler Fig | Medium | 700+ | Medium |
| Transaction (Batch) | Replatform | High | 1,385 | High |
| Bill Payment | Strangler Fig | Medium | 573 | Medium |
| Reporting | Rewrite | Medium-High | 650+ | Medium |
| User Administration | Strangler Fig | Medium | 696+ | Low |
| Optional Modules | Rewrite | High | ~1,500 | High |

---

## Technology Target State

| Legacy Component | Target Technology |
|-----------------|-------------------|
| COBOL programs | Java 17+ / Spring Boot |
| CICS transactions | REST APIs (Spring MVC) |
| BMS screen maps | React / Angular SPA |
| VSAM files | PostgreSQL / Oracle |
| JCL batch jobs | Spring Batch |
| Copybook records | Java POJOs / JPA Entities |
| COMMAREA | HTTP session / JWT claims |
| TDQ / Internal Reader | Message queue (Kafka / RabbitMQ) |
| 3270 terminal | Web browser |
| IMS DB | Relational DB (PostgreSQL) |
| MQ | Kafka / RabbitMQ |
