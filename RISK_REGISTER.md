# CardDemo Modernization Risk Register

## 1. Overview

This register catalogs the top risks associated with modernizing the CardDemo mainframe application from COBOL/CICS/VSAM to Java/Spring Boot/PostgreSQL. Each risk is scored on **Likelihood** (1-5) and **Impact** (1-5), producing a **Risk Score** (L x I, max 25). Risks are ordered by score descending.

### Risk Severity Bands

| Score Range | Severity | Action Required |
|---|---|---|
| 20-25 | **Critical** | Immediate mitigation; executive sponsor escalation |
| 15-19 | **High** | Active mitigation plan required before phase start |
| 10-14 | **Medium** | Mitigation plan documented; monitor weekly |
| 5-9 | **Low** | Accept with monitoring |
| 1-4 | **Minimal** | Accept |

---

## 2. Risk Register

### R-01: Financial Calculation Precision Loss

| Attribute | Detail |
|---|---|
| **Category** | Data Integrity |
| **Likelihood** | 4 (Likely) |
| **Impact** | 5 (Critical) |
| **Risk Score** | **20 -- Critical** |
| **Phase Affected** | Phase 5 (Transaction Processing & Financial Operations) |
| **Description** | COBOL uses packed decimal (COMP-3) and fixed-point arithmetic (PIC S9(10)V99) which has exact decimal semantics. Java `double`/`float` types introduce floating-point rounding errors. Interest calculations (CBACT04C) and transaction posting (CBTRN02C) accumulate amounts across thousands of records, where even sub-cent differences can compound. |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M-01a | Use `java.math.BigDecimal` exclusively for all monetary amounts. Prohibit `double`/`float` in financial code paths via static analysis rule. | Dev Lead | Planned |
| M-01b | Implement penny-exact parity test: run legacy and modern interest calculation on same data, compare every account total. Threshold: $0.00 variance. | QA Lead | Planned |
| M-01c | Map COBOL COMP-3 fields (e.g., `PIC S9(10)V99 COMP-3`) to `BigDecimal` with explicit scale of 2 and `RoundingMode.HALF_EVEN` (banker's rounding). | Dev Lead | Planned |
| M-01d | Create a dedicated decimal conversion library with unit tests for every COMP-3 field in the codebase (CVEXPORT, CVTRA01Y, etc.). | Dev Lead | Planned |

**Contingency:** If parity cannot be achieved, revert to legacy batch processing and investigate COBOL-to-Java transpiler for the specific calculation routines.

---

### R-02: POSTTRAN Batch Atomicity Failure

| Attribute | Detail |
|---|---|
| **Category** | Data Integrity |
| **Likelihood** | 4 (Likely) |
| **Impact** | 5 (Critical) |
| **Risk Score** | **20 -- Critical** |
| **Phase Affected** | Phase 5 (Transaction Processing) |
| **Description** | The POSTTRAN batch job (`CBTRN02C`, 731 LOC) atomically updates 4 VSAM files (TRANSACT, ACCTDAT, TCATBALF) and produces a reject file (DALYREJS) in a single sequential pass. In the microservice architecture, these updates span multiple services (Transaction, Account, Billing). A failure mid-batch could leave data inconsistent -- e.g., a transaction posted but the account balance not updated. |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M-02a | Implement Spring Batch with chunk-based processing and database-level transactions. Each chunk (e.g., 100 records) commits atomically within a single database. | Dev Lead | Planned |
| M-02b | Use the Saga pattern with compensation: if account balance update fails after transaction posting, enqueue a compensating reversal. | Architect | Planned |
| M-02c | Implement an outbox pattern for cross-service consistency: write to local DB + outbox table in one transaction, then publish events. | Architect | Planned |
| M-02d | Add batch restart capability using Spring Batch's `JobRepository` to resume from last successful chunk after failure. | Dev Lead | Planned |
| M-02e | Run parallel (shadow) batch for 30+ days before cutover. Compare outputs daily. | QA Lead | Planned |

**Contingency:** Fall back to a monolithic batch job that uses a single database transaction across all tables (temporarily violating microservice boundaries) until saga reliability is proven.

---

### R-03: VSAM-to-PostgreSQL Data Migration Corruption

| Attribute | Detail |
|---|---|
| **Category** | Data Integrity |
| **Likelihood** | 3 (Possible) |
| **Impact** | 5 (Critical) |
| **Risk Score** | **15 -- High** |
| **Phase Affected** | Phase 0 (Foundation), all subsequent phases |
| **Description** | VSAM files use EBCDIC encoding, packed decimal (COMP-3), binary (COMP), and fixed-length records with FILLER padding. Conversion to PostgreSQL requires precise character encoding translation, numeric unpacking, and field boundary alignment. The CVEXPORT copybook (103 lines) uses REDEFINES, OCCURS, COMP, and COMP-3 fields -- a single alignment error corrupts all downstream records. |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M-03a | Build record-level validation: for each copybook, create a parser that reads raw VSAM records and verifies field boundaries against known test data. | Dev Lead | Planned |
| M-03b | Use the ASCII sample data files (`app/data/ASCII/`) as ground truth for validating EBCDIC-to-ASCII conversion. | QA Lead | Planned |
| M-03c | Implement bidirectional CDC bridge: VSAM → PostgreSQL and PostgreSQL → VSAM during transition. Verify round-trip data integrity. | Data Engineer | Planned |
| M-03d | Document every FILLER field's position and purpose. Some FILLERs may contain undocumented data from previous code versions. | Analyst | Planned |

**Contingency:** Use AWS Mainframe Modernization (M2) data replication service as a proven VSAM-to-RDS bridge if custom CDC fails.

---

### R-04: CICS Transaction Semantics Lost in Translation

| Attribute | Detail |
|---|---|
| **Category** | Functional |
| **Likelihood** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Risk Score** | **12 -- Medium** |
| **Phase Affected** | Phase 4 (Account/Card), Phase 5 (Transactions) |
| **Description** | CICS provides pseudo-conversational programming, COMMAREA-based state management, and automatic transaction rollback (SYNCPOINT). The modern REST/SPA architecture has fundamentally different session management, state handling, and error recovery patterns. Subtle behavioral differences (e.g., CICS automatic abend handling, DFHCOMMAREA size limits, BMS map cursor positioning) may cause functional regressions. |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M-04a | Document all CICS-specific behaviors per program: SYNCPOINT usage, HANDLE CONDITION, XCTL vs LINK, RETURN TRANSID chains. | Analyst | Planned |
| M-04b | Implement session state management (Redis or JWT claims) that replicates COMMAREA semantics during transition. | Dev Lead | Planned |
| M-04c | Create end-to-end test scenarios that mirror 3270 terminal flows: login → navigate → operate → logout. Automate with Selenium/Playwright. | QA Lead | Planned |
| M-04d | Map each CICS ABEND code to modern exception handling. Ensure no silent failures. | Dev Lead | Planned |

**Contingency:** Maintain a CICS compatibility shim (UniKix or similar) during transition for any programs where behavioral parity cannot be verified.

---

### R-05: Cross-Reference (XREF) Decomposition Data Inconsistency

| Attribute | Detail |
|---|---|
| **Category** | Data Integrity |
| **Likelihood** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Risk Score** | **12 -- Medium** |
| **Phase Affected** | Phase 4 (Account/Card Management) |
| **Description** | The CARDXREF VSAM file is a denormalized join table linking Card Number → Customer ID + Account ID. It's read by 6 bounded contexts. Decomposing it into service-owned relationships introduces eventual consistency risks: if the Card Service creates a new card but the Customer Service hasn't yet received the linkage event, lookups fail. |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M-05a | During transition, maintain a read-only XREF materialized view in PostgreSQL, synced from service events. All consumers read from this view until fully migrated. | Data Engineer | Planned |
| M-05b | Implement event ordering guarantees (Kafka partition key = card number) to prevent out-of-order XREF updates. | Architect | Planned |
| M-05c | Add a reconciliation job that periodically compares service-owned XREF projections against the canonical XREF data. | Dev Lead | Planned |
| M-05d | Define SLA for eventual consistency: XREF updates must propagate within 5 seconds under normal load. | Architect | Planned |

**Contingency:** Keep XREF as a shared read-only database table (violating strict service boundaries) until all consumers can tolerate eventual consistency.

---

### R-06: IMS Hierarchical Database Schema Redesign Failure

| Attribute | Detail |
|---|---|
| **Category** | Technical |
| **Likelihood** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Risk Score** | **12 -- Medium** |
| **Phase Affected** | Phase 6 (Authorization Processing) |
| **Description** | IMS DB uses a hierarchical data model (segments, PCBs, PSBs) with parent-child relationships that have no direct relational equivalent. The authorization module uses two IMS databases (DBPAUTP0, DBPAUTX0) with DL/I calls. Flattening the hierarchy into relational tables may lose implicit ordering, segment-level locking semantics, or hierarchical path dependencies. |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M-06a | Analyze all DL/I calls (GU, GN, ISRT, REPL, DLET) in authorization programs to understand traversal patterns before designing relational schema. | Analyst | Planned |
| M-06b | Use a document database (PostgreSQL JSONB columns) for segments that resist normalization. | Architect | Planned |
| M-06c | Build a comprehensive test suite from existing IMS test data (`AWS.M2.CARDDEMO.IMSDATA.DBPAUTP0.dat`). Verify all query patterns produce identical results. | QA Lead | Planned |
| M-06d | Consider a phased approach: first rehost IMS to PostgreSQL with minimal schema change, then normalize in a subsequent iteration. | Architect | Planned |

**Contingency:** Use AWS M2 managed runtime to keep IMS operational while the rest of the system migrates. Migrate IMS last as an isolated effort.

---

### R-07: Batch Schedule Timing and Dependency Violations

| Attribute | Detail |
|---|---|
| **Category** | Operational |
| **Likelihood** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Risk Score** | **9 -- Low** |
| **Phase Affected** | Phase 5 (Batch Processing) |
| **Description** | Control-M orchestrates job chains with IN/OUT conditions (e.g., CLOSEFIL must complete before TRANBKP). The daily, weekly, and monthly schedules have implicit timing dependencies (e.g., WAITSTEP ensures CICS file reopening before online processing resumes). Kubernetes CronJobs don't natively support job dependency chains. |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M-07a | Use Apache Airflow or Spring Cloud Data Flow for batch orchestration instead of raw CronJobs. | DevOps | Planned |
| M-07b | Implement the CLOSEFIL/OPENFIL pattern as database connection pool recycling in the modern architecture. | Dev Lead | Planned |
| M-07c | Document all timing dependencies from Control-M XML. Map each INCOND/OUTCOND to Airflow task dependencies. | Analyst | Planned |
| M-07d | Eliminate WAITSTEP by using proper job completion signals (K8s Job success condition → trigger next job). | DevOps | Planned |

**Contingency:** Keep Control-M as the orchestrator during transition. Control-M can invoke REST APIs to trigger modern batch jobs while maintaining dependency chains.

---

### R-08: MQ-to-Kafka Message Semantics Mismatch

| Attribute | Detail |
|---|---|
| **Category** | Technical |
| **Likelihood** | 2 (Unlikely) |
| **Impact** | 4 (Major) |
| **Risk Score** | **8 -- Low** |
| **Phase Affected** | Phase 6 (Authorization Processing) |
| **Description** | IBM MQ provides transactional message delivery with exactly-once semantics via SYNCPOINT. The authorization module (`COPAUA0C`) uses MQ triggers to process authorization requests. Kafka provides at-least-once delivery by default. Authorization processing must not double-process requests or lose messages. |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M-08a | Use Kafka's idempotent producer + transactional consumer for exactly-once processing semantics. | Dev Lead | Planned |
| M-08b | Implement idempotency keys on authorization requests to prevent double-processing. | Dev Lead | Planned |
| M-08c | During transition, use a MQ-to-Kafka bridge (IBM MQ Kafka Connector) to maintain MQ producers while migrating consumers. | DevOps | Planned |
| M-08d | Implement a dead letter queue for failed authorization processing with manual review workflow. | Dev Lead | Planned |

**Contingency:** Maintain IBM MQ for the authorization module while migrating other modules to Kafka. Bridge later.

---

### R-09: Knowledge Loss -- COBOL Expertise Gap

| Attribute | Detail |
|---|---|
| **Category** | Organizational |
| **Likelihood** | 4 (Likely) |
| **Impact** | 3 (Moderate) |
| **Risk Score** | **12 -- Medium** |
| **Phase Affected** | All phases |
| **Description** | The CardDemo codebase uses diverse COBOL patterns (88-level conditions, REDEFINES, COMP-3, ALTER/GO TO, BMS maps, COMMAREA). Understanding these patterns requires mainframe expertise that is scarce. If COBOL-skilled staff leave or are unavailable during migration, undocumented business rules may be lost or incorrectly translated. |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M-09a | Before each phase, conduct COBOL code walkthroughs with mainframe SMEs. Record sessions. | Project Lead | Planned |
| M-09b | Extract all business rules from COBOL programs into a machine-readable specification (decision tables, validation matrices). | Analyst | Planned |
| M-09c | Use automated COBOL analysis tools (e.g., SonarQube for COBOL, Micro Focus Enterprise Analyzer) to generate dependency graphs and call trees. | Dev Lead | Planned |
| M-09d | Ensure at least 2 team members have COBOL reading proficiency throughout the project. Budget for mainframe consulting if needed. | Project Lead | Planned |

**Contingency:** Engage a specialized mainframe modernization partner (e.g., Micro Focus, Modern Systems) for knowledge transfer.

---

### R-10: Performance Degradation Under Load

| Attribute | Detail |
|---|---|
| **Category** | Performance |
| **Likelihood** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Risk Score** | **9 -- Low** |
| **Phase Affected** | Phase 4, Phase 5 |
| **Description** | CICS is optimized for high-throughput transaction processing with minimal overhead. VSAM KSDS provides O(1) keyed access with B-tree indexes stored on the same volume as data. The modern architecture introduces network hops (API calls between services), serialization overhead (JSON), and database connection pooling latency that may degrade response times for high-volume operations like card list pagination (COCRDLIC reads thousands of records). |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M-10a | Establish performance baselines from legacy: measure response times for each CICS transaction under typical load. | QA Lead | Planned |
| M-10b | Implement database indexing strategy that mirrors VSAM KSDS key access patterns (primary key + alternate indexes). | Dev Lead | Planned |
| M-10c | Use connection pooling (HikariCP), response caching (Redis), and pagination optimization to match VSAM access performance. | Dev Lead | Planned |
| M-10d | Conduct load testing with production-scale data volumes before each phase cutover. Target: <=20% response time increase. | QA Lead | Planned |

**Contingency:** Implement read replicas and caching layers. For extreme cases, use gRPC instead of REST for inter-service communication.

---

### R-11: EBCDIC/ASCII Encoding Edge Cases

| Attribute | Detail |
|---|---|
| **Category** | Data Integrity |
| **Likelihood** | 2 (Unlikely) |
| **Impact** | 3 (Moderate) |
| **Risk Score** | **6 -- Low** |
| **Phase Affected** | Phase 0 (Foundation) |
| **Description** | EBCDIC-to-ASCII conversion has edge cases with special characters, code pages (e.g., EBCDIC code page 037 vs 1047), and zone-decimal/packed-decimal representations. The `app/data/EBCDIC/` files may contain characters that map differently across code pages. |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M-11a | Identify the EBCDIC code page used (likely IBM-037 for US English). Use the correct code page for all conversions. | Data Engineer | Planned |
| M-11b | Compare every converted ASCII record against the provided `app/data/ASCII/` reference files. | QA Lead | Planned |
| M-11c | Build a field-level EBCDIC-to-UTF-8 conversion library with test coverage for all character ranges encountered in the data. | Dev Lead | Planned |

**Contingency:** Use IBM's ICU (International Components for Unicode) library for industrial-grade EBCDIC conversion.

---

### R-12: Incomplete Test Coverage of Legacy Behavior

| Attribute | Detail |
|---|---|
| **Category** | Quality |
| **Likelihood** | 4 (Likely) |
| **Impact** | 3 (Moderate) |
| **Risk Score** | **12 -- Medium** |
| **Phase Affected** | All phases |
| **Description** | The legacy system has no automated test suite. The various coding styles (documented in README as intentional for testing modernization tooling) include ALTER/GO TO patterns, 2D arrays, subroutine calls, and complex REDEFINES. These patterns create execution paths that are difficult to discover without comprehensive test data. Edge cases in field validation (COACTUPC's 4,236 LOC), batch error handling (CBTRN02C's reject logic), and interest calculation (CBACT04C's multi-file traversal) may go untested. |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M-12a | Generate comprehensive test data covering boundary conditions: zero balances, maximum amounts (PIC S9(10)V99 = +/- 9,999,999,999.99), empty fields, special characters. | QA Lead | Planned |
| M-12b | Record production-like transaction flows on the legacy system to create golden-file test suites. | QA Lead | Planned |
| M-12c | For COACTUPC, create a validation rule matrix documenting every field's acceptable values, then generate test cases from the matrix. | Analyst | Planned |
| M-12d | Implement characterization testing: run legacy program with diverse inputs, capture all outputs, assert modern code produces identical outputs. | Dev Lead | Planned |

**Contingency:** Accept higher risk for untestable paths and implement extensive production monitoring with anomaly detection.

---

### R-13: Dual-Write Consistency During Transition

| Attribute | Detail |
|---|---|
| **Category** | Data Integrity |
| **Likelihood** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Risk Score** | **9 -- Low** |
| **Phase Affected** | Phase 4, Phase 5 |
| **Description** | During the strangler fig transition, both legacy (VSAM) and modern (PostgreSQL) systems must remain synchronized. Dual-writing introduces the risk of one write succeeding and the other failing, creating data drift. This is especially critical for account balance updates where VSAM and PostgreSQL must agree exactly. |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M-13a | Designate one system as authoritative at any given time. During transition, VSAM is authoritative; modern replicates via CDC. | Architect | Planned |
| M-13b | Implement a reconciliation job that runs hourly, comparing record counts and checksums between VSAM and PostgreSQL. | Data Engineer | Planned |
| M-13c | Use event sourcing: all writes go to an event log first, then are applied to both stores. Replay events on discrepancy. | Architect | Planned |
| M-13d | Minimize the dual-write window. Phase cutover should move from "legacy authoritative" to "modern authoritative" in < 1 week per functional area. | Project Lead | Planned |

**Contingency:** If reconciliation finds drift, halt dual-writing and investigate. Resync from the authoritative source.

---

### R-14: Regulatory and Audit Compliance During Transition

| Attribute | Detail |
|---|---|
| **Category** | Compliance |
| **Likelihood** | 2 (Unlikely) |
| **Impact** | 5 (Critical) |
| **Risk Score** | **10 -- Medium** |
| **Phase Affected** | Phase 5, Phase 6 |
| **Description** | Credit card systems are subject to PCI-DSS, SOX, and financial regulations. The modernization must maintain audit trails, data retention policies, and access controls throughout the transition. Changing the technology stack requires re-certification of compliance controls. |

**Mitigations:**

| # | Mitigation | Owner | Status |
|---|---|---|---|
| M-14a | Engage compliance team before Phase 5 to define audit requirements for the modern architecture. | Project Lead | Planned |
| M-14b | Implement comprehensive audit logging from day one. Every data mutation must be traceable. | Dev Lead | Planned |
| M-14c | Maintain full data lineage documentation: which system processed each record, when, and what transformations were applied. | Data Engineer | Planned |
| M-14d | Plan for PCI-DSS re-assessment of the target architecture before processing live card data. | Security Lead | Planned |

**Contingency:** Delay customer-facing cutover until compliance certification is obtained. Process through legacy system in the interim.

---

## 3. Risk Summary Matrix

| ID | Risk | Likelihood | Impact | Score | Severity | Phase |
|---|---|---|---|---|---|---|
| R-01 | Financial Calculation Precision Loss | 4 | 5 | **20** | Critical | 5 |
| R-02 | POSTTRAN Batch Atomicity Failure | 4 | 5 | **20** | Critical | 5 |
| R-03 | VSAM-to-PostgreSQL Data Migration Corruption | 3 | 5 | **15** | High | 0 |
| R-04 | CICS Transaction Semantics Lost | 3 | 4 | **12** | Medium | 4-5 |
| R-05 | XREF Decomposition Inconsistency | 3 | 4 | **12** | Medium | 4 |
| R-06 | IMS Schema Redesign Failure | 3 | 4 | **12** | Medium | 6 |
| R-09 | Knowledge Loss -- COBOL Expertise Gap | 4 | 3 | **12** | Medium | All |
| R-12 | Incomplete Test Coverage | 4 | 3 | **12** | Medium | All |
| R-14 | Regulatory Compliance During Transition | 2 | 5 | **10** | Medium | 5-6 |
| R-07 | Batch Schedule Dependency Violations | 3 | 3 | **9** | Low | 5 |
| R-10 | Performance Degradation Under Load | 3 | 3 | **9** | Low | 4-5 |
| R-13 | Dual-Write Consistency | 3 | 3 | **9** | Low | 4-5 |
| R-08 | MQ-to-Kafka Semantics Mismatch | 2 | 4 | **8** | Low | 6 |
| R-11 | EBCDIC/ASCII Encoding Edge Cases | 2 | 3 | **6** | Low | 0 |

---

## 4. Risk Heat Map

```
Impact ▲
  5    │  R-11       R-03        R-01,R-02
       │                         ▲ CRITICAL
  4    │  R-08    R-04,R-05,R-06
       │
  3    │         R-07,R-10,R-13  R-09,R-12
       │
  2    │                         R-14
       │
  1    │
       └──────────────────────────────────▶ Likelihood
            1        2        3        4        5
```

---

## 5. Risk Review Cadence

| Frequency | Activity | Participants |
|---|---|---|
| Weekly | Review active phase risks; update likelihood/impact | Project Lead, Dev Lead |
| Per Phase Gate | Full risk register review; add/remove risks | All stakeholders |
| Monthly | Executive risk summary; escalate Critical/High risks | Executive Sponsor, Project Lead |
| On Incident | Immediate risk reassessment; update mitigations | Incident team |
