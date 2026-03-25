# CardDemo Risk Register

## Overview

This document catalogues the top risks associated with modernizing the CardDemo mainframe application from COBOL/CICS/VSAM to Java/Spring Boot/RDBMS. Each risk is assessed for probability, impact, and overall severity, with specific mitigation strategies tied to the phased cutover plan.

### Risk Scoring

| Score | Probability | Impact |
|-------|------------|--------|
| 1 | Unlikely (< 10%) | Minimal -- workaround available |
| 2 | Possible (10-30%) | Minor -- schedule delay < 2 weeks |
| 3 | Likely (30-60%) | Moderate -- schedule delay 2-6 weeks |
| 4 | Very Likely (60-90%) | Major -- scope reduction or significant rework |
| 5 | Almost Certain (> 90%) | Critical -- project failure or financial loss |

**Severity = Probability x Impact**

| Severity Range | Rating | Action |
|---------------|--------|--------|
| 1-4 | Low | Monitor |
| 5-9 | Medium | Mitigate actively |
| 10-15 | High | Escalate and mitigate immediately |
| 16-25 | Critical | Stop and resolve before proceeding |

---

## Risk Register

### R-01: Financial Calculation Precision Loss

| Attribute | Detail |
|-----------|--------|
| **Category** | Technical |
| **Phase** | Phase 4 (Financial Processing) |
| **Probability** | 4 (Very Likely) |
| **Impact** | 5 (Critical) |
| **Severity** | **20 (Critical)** |
| **Description** | COBOL uses fixed-point packed decimal arithmetic (COMP-3, PIC S9(n)V99) with implicit truncation behavior. Java floating-point types (float, double) will produce different results. Even with BigDecimal, rounding mode differences between COBOL TRUNCATE and Java RoundingMode options can cause penny-level discrepancies that compound across thousands of accounts. |
| **Affected Programs** | CBACT04C (interest calculation), CBTRN02C (transaction posting), COBIL00C (bill payment) |
| **Affected Data** | ACCTDAT (account balances), TRANSACT (transaction amounts), TCATBALF (category balances) |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M-01a | Use `java.math.BigDecimal` exclusively for all monetary amounts. Never use float/double. | Dev Team | Planned |
| M-01b | Map COBOL `PIC S9(10)V99` to `BigDecimal` with scale=2 and `RoundingMode.DOWN` (matches COBOL truncation). | Dev Team | Planned |
| M-01c | Create a comprehensive arithmetic test suite using production data: extract 10,000+ accounts from VSAM, run both COBOL and Java calculations, compare every output field to the penny. | QA Team | Planned |
| M-01d | Mandatory 3-cycle parallel run (see Cutover Plan Phase 4) with automated reconciliation before cutover approval. | Project Lead | Planned |
| M-01e | Implement automated daily reconciliation reports during dual-run period comparing VSAM and RDBMS balances. | Dev Team | Planned |

**Contingency:** If precision discrepancies cannot be resolved, replatform the interest calculation module using automated COBOL-to-Java conversion tools that preserve COMP-3 semantics, accepting lower code quality for higher arithmetic fidelity.

---

### R-02: Data Inconsistency During Dual-Write Period

| Attribute | Detail |
|-----------|--------|
| **Category** | Data |
| **Phase** | Phase 3-4 (Core Domain, Financial Processing) |
| **Probability** | 4 (Very Likely) |
| **Impact** | 4 (Major) |
| **Severity** | **16 (Critical)** |
| **Description** | During the Strangler Fig transition, both VSAM and RDBMS must stay synchronized via Change Data Capture (CDC). Network latency, CDC failures, or race conditions between legacy writes and new service writes can create data divergence. ACCTDAT is accessed by 5 bounded contexts, making it the highest-contention dataset. |
| **Affected Programs** | All programs that write to ACCTDAT, TRANSACT, CARDDAT |
| **Affected Data** | Account balances, transaction records, card status |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M-02a | Implement CDC with exactly-once delivery guarantees (e.g., Debezium with Kafka transactional producers). | Infra Team | Planned |
| M-02b | During dual-write phases, designate one system as "source of truth" per dataset (VSAM for accounts until Phase 4 cutover). | Architect | Planned |
| M-02c | Implement automated hourly consistency checks: count records, sum key numeric fields, hash-compare random samples. | Dev Team | Planned |
| M-02d | Build a reconciliation dashboard showing real-time sync status per dataset. | Dev Team | Planned |
| M-02e | Define maximum acceptable sync lag (< 30 seconds for online, < 5 minutes for batch) with alerts on breach. | Ops Team | Planned |

**Contingency:** If CDC proves unreliable, fall back to periodic batch synchronization (every 15 minutes) with a write-lock on the non-primary system during sync windows.

---

### R-03: COACTUPC Validation Rule Loss

| Attribute | Detail |
|-----------|--------|
| **Category** | Functional |
| **Phase** | Phase 3 (Core Domain) |
| **Probability** | 3 (Likely) |
| **Impact** | 4 (Major) |
| **Severity** | **12 (High)** |
| **Description** | COACTUPC is the largest program (4,237 lines) with 40+ field-level validation rules for account updates (SSN format, phone format, date validation, credit limit ranges, FICO score bounds, zip code format). These rules are deeply interleaved with BMS screen-handling logic and copybook-based date utilities (CSUTLDWY). Missing or incorrectly migrated validation rules could allow invalid data into the system. |
| **Affected Programs** | COACTUPC |
| **Affected Data** | ACCTDAT, CUSTDAT |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M-03a | Extract and document every validation rule from COACTUPC into a validation rules specification before coding begins. | Business Analyst | Planned |
| M-03b | Create a validation rule test matrix with valid/invalid/boundary test cases for each of the 40+ rules. | QA Team | Planned |
| M-03c | Implement validation rules as a separate, testable Java validation layer (Bean Validation / custom validators) isolated from UI and persistence logic. | Dev Team | Planned |
| M-03d | Run shadow-mode comparison: submit identical update requests to both legacy and new service, compare accept/reject decisions for 100% agreement. | QA Team | Planned |
| M-03e | Include edge cases: max-length fields, special characters, boundary dates, zero/negative amounts. | QA Team | Planned |

**Contingency:** If rule extraction is incomplete, use automated COBOL-to-Java conversion for the validation paragraphs only, then manually refactor the converted code.

---

### R-04: Batch Cycle Timing and Sequencing Failure

| Attribute | Detail |
|-----------|--------|
| **Category** | Operational |
| **Phase** | Phase 4 (Financial Processing) |
| **Probability** | 3 (Likely) |
| **Impact** | 4 (Major) |
| **Severity** | **12 (High)** |
| **Description** | The legacy batch cycle has a strict execution sequence: CLOSEFIL -> data refresh -> POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT -> TRANIDX -> OPENFIL. JCL job dependencies, condition codes, and restart/recovery procedures are managed by the job scheduler (CA7/Control-M). Migrating to Spring Batch requires replicating this orchestration, including failure handling and restart from the point of failure. |
| **Affected Programs** | CBTRN02C, CBACT04C, CBSTM03A/B, all JCL jobs in app/jcl/ |
| **Affected Data** | All batch-processed datasets |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M-04a | Map every JCL job dependency, condition code, and restart point into a Spring Batch job flow definition. | Dev Team | Planned |
| M-04b | Implement Spring Batch step listeners that replicate JCL condition code behavior (step success/failure routing). | Dev Team | Planned |
| M-04c | Design restart/recovery: Spring Batch's built-in restartability (job repository tracking) replaces JCL restart procedures. Test restart from every step. | Dev Team | Planned |
| M-04d | Performance benchmark: the new batch cycle must complete within the existing batch window. Test with production-volume data. | QA Team | Planned |
| M-04e | CLOSEFIL/OPENFIL (CICS file close/open for batch access) is eliminated in the modern architecture (RDBMS handles concurrent access). Verify no contention issues. | Architect | Planned |

**Contingency:** If Spring Batch orchestration is unreliable, use Apache Airflow for batch job scheduling with explicit step dependencies and alerting.

---

### R-05: CBSTM03A Conversion Failure (ALTER/GO TO, PSA/TCB/TIOT)

| Attribute | Detail |
|-----------|--------|
| **Category** | Technical |
| **Phase** | Phase 4 (Financial Processing) |
| **Probability** | 4 (Very Likely) |
| **Impact** | 3 (Moderate) |
| **Severity** | **12 (High)** |
| **Description** | CBSTM03A (924 lines) is the most technically complex program. It uses ALTER/GO TO for dynamic control flow, accesses mainframe control blocks (PSA at address 0, TCB, TIOT) for dataset name resolution, uses 2D arrays with REDEFINES, and CALLs CBSTM03B for file I/O. These constructs have no Java equivalent and defeat automated conversion tools. |
| **Affected Programs** | CBSTM03A, CBSTM03B |
| **Affected Data** | Statement output files (text and HTML) |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M-05a | Rewrite from scratch using Spring Batch + modern templating (Thymeleaf for HTML, plain text formatter). Do not attempt automated conversion. | Dev Team | Planned |
| M-05b | Extract statement format requirements from CBSTM03A's output record definitions (lines 85-146 for text, lines 148-223 for HTML) as the specification for the new template. | Business Analyst | Planned |
| M-05c | Use the existing ASCII sample data (app/data/ASCII/) to generate reference statements from the legacy system for comparison. | QA Team | Planned |
| M-05d | The PSA/TCB/TIOT control block access (lines 235-260) is used only for DD name resolution -- this is a mainframe-specific pattern that becomes a simple configuration property in Java. | Dev Team | Planned |

**Contingency:** If the rewrite takes longer than expected, generate statements using SQL queries directly against the RDBMS with a simple report formatter, bypassing the Spring Batch approach.

---

### R-06: Authentication Security Gap During Transition

| Attribute | Detail |
|-----------|--------|
| **Category** | Security |
| **Phase** | Phase 1 (Peripheral Services) |
| **Probability** | 2 (Possible) |
| **Impact** | 5 (Critical) |
| **Severity** | **10 (High)** |
| **Description** | The legacy system stores passwords in plaintext (USRSEC: `SEC-USR-PWD PIC X(08)`). During the Phase 1 transition from COSGN00C to Spring Security, there is a window where the new system must authenticate against migrated credentials. Password hashing during migration means users cannot use their old plaintext passwords unless a one-time migration flow is implemented. Additionally, the dual-auth period (JWT + COMMAREA) creates a potential bypass if not carefully implemented. |
| **Affected Programs** | COSGN00C, new Identity Service |
| **Affected Data** | USRSEC |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M-06a | During data migration, hash all plaintext passwords using bcrypt. Force password reset for all users on first login to the new system. | Dev Team | Planned |
| M-06b | Implement a "legacy password bridge": on first login, accept the plaintext password, verify against stored hash of the original plaintext, then prompt for a new password that meets modern complexity requirements. | Dev Team | Planned |
| M-06c | During dual-auth period, ensure the API Gateway validates JWT tokens and does NOT fall back to legacy auth without explicit routing rules. | Security Team | Planned |
| M-06d | Conduct penetration testing of the authentication transition, specifically targeting the dual-auth window. | Security Team | Planned |
| M-06e | Implement session timeout and token expiry (e.g., 30-minute JWT TTL) from day one. Legacy COSGN00C has no session timeout. | Dev Team | Planned |

**Contingency:** If the dual-auth period proves too risky, perform a "big bang" authentication cutover over a maintenance window (all users switch to new auth simultaneously).

---

### R-07: VSAM Alternate Index Semantic Mismatch

| Attribute | Detail |
|-----------|--------|
| **Category** | Technical |
| **Phase** | Phase 3 (Core Domain) |
| **Probability** | 3 (Likely) |
| **Impact** | 3 (Moderate) |
| **Severity** | **9 (Medium)** |
| **Description** | CardDemo uses VSAM alternate indexes extensively: CARDAIX (cards by account), CXACAIX (cross-reference by account). CICS STARTBR/READNEXT/READPREV with RIDFLD positioning on alternate indexes has specific behavior for duplicate keys, generic keys, and end-of-file handling that differs from SQL query semantics. COCRDLIC's pagination logic (7 rows per page with PF7/PF8 forward/backward browsing) depends on this VSAM browse behavior. |
| **Affected Programs** | COCRDLIC, COACTVWC, COBIL00C |
| **Affected Data** | CARDDAT (via CARDAIX), CARDXREF (via CXACAIX) |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M-07a | Document the exact VSAM browse behavior for each alternate index use: key positioning, duplicate handling, forward/backward traversal, end-of-browse conditions. | Dev Team | Planned |
| M-07b | Replace VSAM alternate index browsing with SQL queries using appropriate WHERE clauses, ORDER BY, and LIMIT/OFFSET (or keyset pagination). | Dev Team | Planned |
| M-07c | Test pagination edge cases: first page, last page, single-record pages, empty result sets, accounts with maximum cards (stress test). | QA Team | Planned |
| M-07d | Implement cursor-based pagination (using card number as cursor) rather than offset-based, to match VSAM's key-positioning semantics more closely. | Dev Team | Planned |

**Contingency:** If pagination behavior diverges, implement a VSAM-compatible browse abstraction layer in Java that replicates STARTBR/READNEXT semantics using database cursors.

---

### R-08: Transaction ID Generation Collision

| Attribute | Detail |
|-----------|--------|
| **Category** | Data Integrity |
| **Phase** | Phase 3-4 (Core Domain, Financial Processing) |
| **Probability** | 2 (Possible) |
| **Impact** | 4 (Major) |
| **Severity** | **8 (Medium)** |
| **Description** | COBIL00C generates transaction IDs by reading the last key from the TRANSACT VSAM file (STARTBR with HIGH-VALUES, READPREV) and incrementing by 1. During the dual-write period, both legacy and new systems may attempt to generate transaction IDs simultaneously, causing collisions. Additionally, the 16-character PIC X(16) transaction ID format may not accommodate modern distributed ID generation. |
| **Affected Programs** | COBIL00C, COTRN02C, CBTRN02C |
| **Affected Data** | TRANSACT |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M-08a | During dual-write period, partition the ID space: legacy uses IDs in range 0000000000000001 to 4999999999999999; new system uses 5000000000000001 to 9999999999999999. | Architect | Planned |
| M-08b | In the fully modern system, use a database sequence (PostgreSQL BIGSERIAL) or distributed ID generator (Snowflake/ULID) for transaction IDs. | Dev Team | Planned |
| M-08c | Implement an ID generation service that both legacy and modern systems call during the transition period. | Dev Team | Planned |
| M-08d | Add a UNIQUE constraint on the transactions table `transaction_id` column with conflict detection. | Dev Team | Planned |

**Contingency:** If collisions occur, implement a retry mechanism with exponential backoff and ID re-generation.

---

### R-09: Performance Degradation in API-Based Architecture

| Attribute | Detail |
|-----------|--------|
| **Category** | Performance |
| **Phase** | Phase 3-5 (All service phases) |
| **Probability** | 3 (Likely) |
| **Impact** | 3 (Moderate) |
| **Severity** | **9 (Medium)** |
| **Description** | The legacy system performs all operations in-process: CICS programs read VSAM files with microsecond latency, pass data via COMMAREA (in-memory), and use XCTL for zero-cost program transfers. The modern architecture introduces network hops (REST API calls between services), database query overhead (SQL parsing, network round-trips to PostgreSQL), and serialization/deserialization costs (JSON). Account view (COACTVWC) currently makes 4 VSAM reads in-process; the modern equivalent makes 3-4 REST API calls across network boundaries. |
| **Affected Programs** | All online CICS programs (response time), all batch programs (throughput) |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M-09a | Establish performance baselines for all CICS transactions before migration (response time P50, P95, P99). | QA Team | Planned |
| M-09b | Use database connection pooling (HikariCP) and HTTP connection pooling (Apache HttpClient / OkHttp). | Dev Team | Planned |
| M-09c | Implement caching for frequently read, rarely changed data: customer records, card cross-references, discount group rates (Redis/Caffeine). | Dev Team | Planned |
| M-09d | For account view, use a composite API pattern or GraphQL to fetch account + customer + cards in a single request instead of 3-4 separate calls. | Dev Team | Planned |
| M-09e | For batch processing, use bulk operations (batch inserts, bulk updates) instead of record-by-record processing. Spring Batch chunk-oriented processing with configurable chunk sizes. | Dev Team | Planned |
| M-09f | Set performance SLA: online response time < 200ms P95; batch throughput > 10,000 transactions/minute. | Architect | Planned |

**Contingency:** If inter-service latency is unacceptable, consolidate related services (e.g., merge Account + Card into a single service) to reduce network hops, accepting lower domain isolation.

---

### R-10: Knowledge Loss and Skill Gap

| Attribute | Detail |
|-----------|--------|
| **Category** | Organizational |
| **Phase** | All phases |
| **Probability** | 3 (Likely) |
| **Impact** | 4 (Major) |
| **Severity** | **12 (High)** |
| **Description** | COBOL business rules are often undocumented and understood only by subject matter experts (SMEs) who may be unavailable during the migration. The CardDemo application has 31 programs totaling approximately 15,000+ lines of COBOL. Critical business logic -- especially in COACTUPC (account validation), CBACT04C (interest calculation), and CBTRN02C (transaction posting) -- may have undocumented edge cases, implicit business rules embedded in conditional logic, or workarounds for historical data issues. |
| **Affected Programs** | All, especially COACTUPC, CBACT04C, CBTRN02C |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M-10a | Conduct knowledge extraction workshops with COBOL SMEs for each bounded context before development begins. Record sessions. | Project Lead | Planned |
| M-10b | Create a business rules catalog: for each program, document every EVALUATE, IF/ELSE, and 88-level condition with its business meaning. | Business Analyst | Planned |
| M-10c | Use code analysis tools (e.g., SonarQube COBOL plugin, Micro Focus Enterprise Analyzer) to generate program flow diagrams and data flow maps. | Dev Team | Planned |
| M-10d | Implement comprehensive regression test suites BEFORE migration begins, using production data to capture current behavior as the specification. | QA Team | Planned |
| M-10e | Pair COBOL SMEs with Java developers during each phase to ensure knowledge transfer. | Project Lead | Planned |

**Contingency:** If SMEs are unavailable, use the COBOL source code as the single source of truth and implement "characterization tests" -- tests that capture current behavior without understanding the intent.

---

### R-11: CICS-Specific Behavior Loss

| Attribute | Detail |
|-----------|--------|
| **Category** | Technical |
| **Phase** | Phase 3-4 |
| **Probability** | 3 (Likely) |
| **Impact** | 3 (Moderate) |
| **Severity** | **9 (Medium)** |
| **Description** | CICS provides implicit behaviors that COBOL programs rely on without explicit code: pseudo-conversational mode (RETURN TRANSID with COMMAREA), automatic transaction rollback on ABEND, SYNCPOINT for distributed commits, ASKTIME/FORMATTIME for timestamps, and HANDLE CONDITION for error routing. These implicit behaviors must be explicitly implemented in Java. |
| **Affected Programs** | All online CICS programs |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M-11a | Document all CICS API calls used across programs: EXEC CICS READ, WRITE, REWRITE, DELETE, STARTBR, READNEXT, READPREV, ENDBR, XCTL, LINK, RETURN, SEND MAP, RECEIVE MAP, ASKTIME, FORMATTIME, SYNCPOINT, HANDLE CONDITION, HANDLE AID. | Dev Team | Planned |
| M-11b | Map each CICS API to its Spring equivalent: READ -> JPA findById, STARTBR/READNEXT -> JPA pagination query, XCTL -> REST redirect, COMMAREA -> HTTP session/JWT, SYNCPOINT -> @Transactional, HANDLE CONDITION -> try/catch. | Architect | Planned |
| M-11c | Implement a Spring `@Transactional` boundary around each service method that corresponds to a CICS transaction to preserve atomicity semantics. | Dev Team | Planned |
| M-11d | Test ABEND scenarios: verify that partial updates are rolled back when exceptions occur mid-transaction. | QA Team | Planned |

**Contingency:** Implement a CICS compatibility layer that wraps Spring services with CICS-like transaction semantics for critical paths.

---

### R-12: Regulatory and Compliance Risk

| Attribute | Detail |
|-----------|--------|
| **Category** | Compliance |
| **Phase** | All phases |
| **Probability** | 2 (Possible) |
| **Impact** | 5 (Critical) |
| **Severity** | **10 (High)** |
| **Description** | Credit card processing is subject to PCI-DSS compliance. The migration changes data storage (VSAM to RDBMS), data transmission (COMMAREA to REST/JWT), and security mechanisms (plaintext to hashed passwords). Each change must maintain or improve compliance posture. Card numbers (PIC X(16) in CVACT02Y, CVTRA05Y) must be encrypted at rest and masked in transit. |
| **Affected Programs** | All programs handling card numbers and customer PII |
| **Affected Data** | CARDDAT (card numbers, CVV), CUSTDAT (SSN, DOB), TRANSACT (card numbers), USRSEC (passwords) |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M-12a | Encrypt card numbers and CVV at rest in PostgreSQL using column-level encryption (pgcrypto) or application-level encryption. | Security Team | Planned |
| M-12b | Mask card numbers in API responses (show only last 4 digits) and log output. | Dev Team | Planned |
| M-12c | Encrypt customer PII (SSN, DOB) at rest. | Security Team | Planned |
| M-12d | Implement TLS for all inter-service communication. | Infra Team | Planned |
| M-12e | Conduct PCI-DSS gap assessment after each phase and before go-live. | Compliance Team | Planned |
| M-12f | Implement audit logging for all data access and modifications (who, what, when). | Dev Team | Planned |

**Contingency:** If compliance gaps are identified, halt migration and remediate before proceeding. Engage a Qualified Security Assessor (QSA) early.

---

### R-13: Incomplete Test Coverage for Batch Edge Cases

| Attribute | Detail |
|-----------|--------|
| **Category** | Quality |
| **Phase** | Phase 4 (Financial Processing) |
| **Probability** | 3 (Likely) |
| **Impact** | 3 (Moderate) |
| **Severity** | **9 (Medium)** |
| **Description** | Batch programs handle edge cases that may not be represented in sample data: zero-balance accounts, accounts with no transactions in the current cycle, maximum COMP-3 values, negative balances, accounts in multiple discount groups, reject records with all possible error codes. The sample data in `app/data/ASCII/` may not cover these scenarios. |
| **Affected Programs** | CBTRN02C, CBACT04C, CBSTM03A |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M-13a | Generate synthetic test data covering all edge cases identified in COBOL source code analysis (88-level conditions, IF/EVALUATE branches). | QA Team | Planned |
| M-13b | Achieve branch coverage analysis on COBOL source: ensure every conditional path in CBTRN02C, CBACT04C, and CBSTM03A is exercised. | QA Team | Planned |
| M-13c | Use production-volume data (sanitized) for integration testing, not just the sample data in app/data/ASCII/. | QA Team | Planned |
| M-13d | Test specific edge cases: account with 0 balance, account with maximum balance (S9(10)V99 = 9,999,999,999.99), transaction with 0 amount, account with no XREF entry, DISCGRP with 0% interest rate. | QA Team | Planned |

**Contingency:** If test data is insufficient, use property-based testing (QuickCheck/jqwik) to generate random valid inputs and compare COBOL vs. Java outputs.

---

### R-14: IMS/DB2/MQ Migration Complexity (Optional Module)

| Attribute | Detail |
|-----------|--------|
| **Category** | Technical |
| **Phase** | Phase 5 (Optional Modules) |
| **Probability** | 3 (Likely) |
| **Impact** | 3 (Moderate) |
| **Severity** | **9 (Medium)** |
| **Description** | The optional Authorization module uses three different middleware technologies (IMS DB, DB2, MQ) with different data models (hierarchical, relational, message-based). Migrating from IMS hierarchical segments to relational tables requires schema redesign. MQ trigger monitoring has specific timing and reliability semantics. This is the most middleware-complex module. |
| **Affected Programs** | COPAUA0C, COPAUS0C-2C, CBPAUP0C |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M-14a | Map IMS DB segment hierarchy to relational entity-relationship model before development. | Architect | Planned |
| M-14b | Replace MQ with Kafka or RabbitMQ; implement exactly-once processing semantics. | Dev Team | Planned |
| M-14c | Migrate DB2 tables directly to PostgreSQL (least-effort migration; SQL is mostly compatible). | Dev Team | Planned |
| M-14d | This module is optional; it can be deferred or descoped if it threatens the critical path. | Project Lead | Planned |

**Contingency:** If IMS migration proves too complex, implement the authorization module as a greenfield service based on business requirements rather than attempting to replicate IMS behavior.

---

### R-15: Rollback Data Corruption

| Attribute | Detail |
|-----------|--------|
| **Category** | Operational |
| **Phase** | Phase 3-4 |
| **Probability** | 2 (Possible) |
| **Impact** | 5 (Critical) |
| **Severity** | **10 (High)** |
| **Description** | If a rollback is triggered after the new system has been processing writes, data written by the new system must be either synchronized back to VSAM or discarded. For financial data (transactions, account balances), discarding writes means lost transactions. The CDC mechanism must support bidirectional sync, and the rollback procedure must handle partial writes that occurred during the failure window. |
| **Affected Programs** | All write operations during Phase 3-4 |
| **Affected Data** | TRANSACT, ACCTDAT (most critical) |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M-15a | Implement bidirectional CDC: RDBMS changes replicate to VSAM during the transition period. | Infra Team | Planned |
| M-15b | Maintain a write-ahead log of all new system writes with enough detail to replay them on the legacy system if needed. | Dev Team | Planned |
| M-15c | Test rollback scenarios explicitly in staging: simulate failure at various points, execute rollback, verify data integrity. | QA Team | Planned |
| M-15d | For batch processing, leverage GDG (Generation Data Groups) to maintain previous versions of output files for instant rollback. | Ops Team | Planned |
| M-15e | Define a maximum rollback window (e.g., 4 hours for online, 1 batch cycle for batch). Beyond this window, forward-fix is required instead of rollback. | Architect | Planned |

**Contingency:** If bidirectional CDC is not feasible, implement a "read-only legacy" mode where the legacy system continues to serve reads but all writes go through the new system, eliminating the need to sync back.

---

## Risk Summary Matrix

| Risk ID | Risk | Probability | Impact | Severity | Phase |
|---------|------|-------------|--------|----------|-------|
| R-01 | Financial Calculation Precision Loss | 4 | 5 | **20** | Phase 4 |
| R-02 | Data Inconsistency During Dual-Write | 4 | 4 | **16** | Phase 3-4 |
| R-03 | COACTUPC Validation Rule Loss | 3 | 4 | **12** | Phase 3 |
| R-04 | Batch Cycle Timing/Sequencing Failure | 3 | 4 | **12** | Phase 4 |
| R-05 | CBSTM03A Conversion Failure | 4 | 3 | **12** | Phase 4 |
| R-06 | Authentication Security Gap | 2 | 5 | **10** | Phase 1 |
| R-10 | Knowledge Loss and Skill Gap | 3 | 4 | **12** | All |
| R-12 | Regulatory/Compliance Risk | 2 | 5 | **10** | All |
| R-15 | Rollback Data Corruption | 2 | 5 | **10** | Phase 3-4 |
| R-07 | VSAM Alternate Index Mismatch | 3 | 3 | **9** | Phase 3 |
| R-08 | Transaction ID Generation Collision | 2 | 4 | **8** | Phase 3-4 |
| R-09 | Performance Degradation | 3 | 3 | **9** | Phase 3-5 |
| R-11 | CICS-Specific Behavior Loss | 3 | 3 | **9** | Phase 3-4 |
| R-13 | Incomplete Batch Test Coverage | 3 | 3 | **9** | Phase 4 |
| R-14 | IMS/DB2/MQ Migration Complexity | 3 | 3 | **9** | Phase 5 |

### Risk Heat Map

```
Impact  5 |        R-06    |  R-01         |
        4 |        R-08    |  R-03,R-04    |  R-02
          |                |  R-10         |
        3 |                |  R-07,R-09    |  R-05
          |                |  R-11,R-13    |
          |                |  R-14         |
        2 |                |               |
        1 |                |               |
          +----+-----+----+-----+----+----+
             1     2     3     4     5
                    Probability
```

---

## Risk Review Schedule

| Activity | Frequency | Owner |
|----------|-----------|-------|
| Risk register review | Bi-weekly | Project Lead |
| Risk status update | Weekly (during standup) | All teams |
| New risk identification | Continuous | All teams |
| Mitigation effectiveness review | End of each phase | Project Lead + Architect |
| Escalation of Critical/High risks | Immediate | Project Lead |
