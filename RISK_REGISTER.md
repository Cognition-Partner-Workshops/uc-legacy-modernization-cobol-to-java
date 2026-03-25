# CardDemo Modernization Risk Register

## Overview

This register identifies and assesses the top risks associated with modernizing the CardDemo mainframe COBOL/CICS/VSAM application to a Java/Spring Boot/PostgreSQL target architecture. Each risk is scored on **Impact** (1–5) and **Probability** (1–5), yielding a **Risk Score** (Impact x Probability). Risks scoring 15+ are Critical, 10–14 are High, 5–9 are Medium, and 1–4 are Low.

---

## Risk Summary Heat Map

```
Impact ▲
  5    │  R-09       R-01  R-03
  4    │  R-10  R-07 R-04  R-02
  3    │  R-12  R-08 R-06  R-05
  2    │        R-13 R-14
  1    │  R-15  R-11
       └──────────────────────────►
         1     2     3     4     5
                              Probability
```

---

## Critical Risks (Score 15+)

### R-01: Financial Data Integrity Loss During Migration
| Attribute | Value |
|---|---|
| **Category** | Technical / Data |
| **Impact** | 5 — Incorrect account balances or transaction amounts directly affect customers |
| **Probability** | 4 — Multiple writers to ACCTDAT and complex COBOL decimal arithmetic increase error likelihood |
| **Risk Score** | **20 (Critical)** |
| **Affected Phase** | Phase 4 (Account Management), Phase 6 (Batch Posting), Phase 7 (Interest Calculation) |

**Description:**
COBOL uses fixed-point decimal arithmetic (`PIC S9(10)V99`) while Java uses IEEE 754 floating-point by default. The account balance field `ACCT-CURR-BAL` is written by four different program groups (account update, bill payment, batch posting, interest calculation). Any precision mismatch, rounding difference, or lost update during the dual-run period could result in incorrect customer balances.

**Mitigations:**
1. **Use `java.math.BigDecimal` exclusively** for all monetary calculations — never `double` or `float`. Configure scale=2 and `RoundingMode.HALF_EVEN` (banker's rounding) to match COBOL behavior.
2. **Implement `AccountService.adjustBalance()` as a single serialized entry point** for all balance modifications, using pessimistic locking (`SELECT ... FOR UPDATE`) or optimistic locking with version columns.
3. **Run parallel financial reconciliation** for a minimum of 3 monthly billing cycles: compare every account's modern balance against legacy VSAM balance nightly. Zero-tolerance threshold for discrepancies.
4. **Automated penny-test suite**: Generate test transactions with known expected balances covering edge cases (max balance, negative balance, very small amounts, rounding boundaries).

**Owner:** Technical Lead
**Status:** Open

---

### R-02: Batch Posting Failure Cascades Through Nightly Cycle
| Attribute | Value |
|---|---|
| **Category** | Technical / Operational |
| **Impact** | 4 — Failed posting blocks interest calculation, statements, and next-day processing |
| **Probability** | 4 — `CBTRN02C` performs multi-file coordinated writes; Spring Batch translation is complex |
| **Risk Score** | **16 (Critical)** |
| **Affected Phase** | Phase 6 (Batch Transaction Posting) |

**Description:**
The batch posting program `CBTRN02C` reads daily transactions, validates against cross-reference and account files, posts to the transaction master, updates account balances, and updates category balances — all in a single sequential pass. If the Spring Batch equivalent fails mid-batch, it could leave the database in an inconsistent state (some transactions posted, some not), blocking the entire downstream batch cycle (interest calculation, statement generation, backups).

**Mitigations:**
1. **Chunk-based transactions**: Configure Spring Batch with commit intervals (e.g., 100 records per chunk). Each chunk is an atomic database transaction — on failure, only the current chunk rolls back.
2. **Restartability**: Implement Spring Batch restart capability using `JobRepository`. A failed job can be restarted from the last successful chunk, not from the beginning.
3. **Maintain legacy batch in warm standby** for at least 3 successful modern cycles. If modern batch fails, the legacy JCL (`POSTTRAN.jcl`) can be submitted as a fallback.
4. **Pre-flight validation**: Add a validation-only pass before posting (read all daily transactions, validate all cross-references and accounts, report errors) before committing any writes.
5. **Circuit breaker**: If error rate exceeds 5% in a single batch run, halt and alert operations team rather than posting potentially corrupted data.

**Owner:** Batch Infrastructure Lead
**Status:** Open

---

### R-03: Interest Calculation Discrepancy Due to Arithmetic Differences
| Attribute | Value |
|---|---|
| **Category** | Financial / Compliance |
| **Impact** | 5 — Incorrect interest charges are a regulatory compliance issue and directly affect customers |
| **Probability** | 3 — COBOL arithmetic semantics differ from Java in subtle ways (truncation vs. rounding, intermediate precision) |
| **Risk Score** | **15 (Critical)** |
| **Affected Phase** | Phase 7 (Interest Calculation) |

**Description:**
`CBACT04C` computes monthly interest by reading disclosure group rates (`DIS-INT-RATE`, `PIC S9(04)V99`) and multiplying by category balances. COBOL's `COMPUTE` statement uses intermediate results with specific precision rules that may differ from Java's `BigDecimal` operations. Even a $0.01 difference per account multiplied across thousands of accounts represents a material financial discrepancy.

**Mitigations:**
1. **Exact COBOL arithmetic replication**: Document every COMPUTE statement's intermediate precision and replicate using `BigDecimal` with explicit `MathContext` and scale at each step.
2. **Golden test dataset**: Create a reference dataset with known interest calculation outputs from the legacy system. The modern system must produce identical results on this dataset before any production run.
3. **Three-cycle parallel run** (mandatory): Run legacy and modern interest calculation in parallel for 3 consecutive monthly cycles. Compare results per account — any discrepancy blocks cutover.
4. **Regulatory review**: Engage compliance team to review the modern interest calculation logic before production deployment.

**Owner:** Financial Systems Lead
**Status:** Open

---

## High Risks (Score 10–14)

### R-04: Dual-Run Data Synchronization Conflicts
| Attribute | Value |
|---|---|
| **Category** | Technical / Data |
| **Impact** | 4 — Data conflicts can cause lost transactions or duplicate entries |
| **Probability** | 3 — Dual-run requires bidirectional sync between VSAM and PostgreSQL |
| **Risk Score** | **12 (High)** |
| **Affected Phase** | All phases during dual-run (Phases 1–7) |

**Description:**
During the dual-run period, the same data entity may be written by both the legacy CICS system and the modern Java system. The data synchronization layer must propagate changes bidirectionally without creating conflicts, duplicates, or lost updates. The single-writer principle (each entity has one authoritative source) is the designed mitigation, but routing errors in the API Gateway could direct writes to both systems simultaneously.

**Mitigations:**
1. **Strict single-writer enforcement**: API Gateway routing rules must guarantee that writes for a given entity type go to exactly one system. Log all routing decisions for audit.
2. **Conflict detection**: Nightly reconciliation compares record counts and checksums between VSAM and PostgreSQL. Any mismatch triggers an alert and blocks the next day's processing until resolved.
3. **Idempotent sync operations**: Design sync messages with unique IDs so that replaying a sync event produces the same result (no duplicates).
4. **Feature flag granularity**: Use per-entity-type feature flags (not per-user or per-request) to ensure clean cutover boundaries.

**Owner:** Integration Lead
**Status:** Open

---

### R-05: CICS Transaction Semantics Lost in REST API Translation
| Attribute | Value |
|---|---|
| **Category** | Technical |
| **Impact** | 3 — Behavioral differences cause subtle bugs visible to users |
| **Probability** | 4 — CICS pseudo-conversational model has no direct REST equivalent |
| **Risk Score** | **12 (High)** |
| **Affected Phase** | Phases 3–5 (Card, Account, Transaction online programs) |

**Description:**
CICS online programs use a pseudo-conversational model: the program sends a screen (BMS map), terminates, and re-enters when the user presses a key. State is preserved in COMMAREA across these program invocations. This model has specific behaviors:
- Record-level locking during REWRITE operations
- XCTL (transfer control) vs. LINK (call/return) semantics
- SEND/RECEIVE MAP with cursor positioning and error highlighting
- PF key handling (PF3=exit, PF7/PF8=page up/down)

Translating these to stateless REST APIs may lose behavioral nuances that users and integration tests depend on.

**Mitigations:**
1. **Document all CICS-specific behaviors** per program before migration: record locks, cursor positioning, PF key handling, COMMAREA state transitions.
2. **Stateless API design with explicit state**: Replace COMMAREA with JWT tokens for identity and URL parameters for context. Pagination replaces PF7/PF8 with offset/limit parameters.
3. **Optimistic concurrency control**: Replace CICS record-level locking with ETag/version-based concurrency on REST resources.
4. **Comprehensive integration tests**: Write API-level tests that verify the same user workflows as the legacy 3270 screens (e.g., "search card → view detail → update → verify change").

**Owner:** API Design Lead
**Status:** Open

---

### R-06: Knowledge Loss of Undocumented Business Rules
| Attribute | Value |
|---|---|
| **Category** | Business / Knowledge |
| **Impact** | 3 — Missing rules cause incorrect processing that may not be caught until production |
| **Probability** | 3 — COBOL programs embed business rules in procedural code without documentation |
| **Risk Score** | **9 (Medium–High)** |
| **Affected Phase** | All phases, especially Phase 4 (Account Update — 4,237 LOC) |

**Description:**
The largest program, `COACTUPC` (Account Update, 4,237 LOC), contains extensive field-level validation rules embedded in procedural COBOL code: date format validation, SSN format checking, credit limit calculations, FICO score ranges, and cross-field dependencies. These rules are not documented outside the code. During migration, some rules may be missed, misunderstood, or incorrectly translated, leading to:
- Accepting invalid data that legacy would reject
- Rejecting valid data that legacy would accept
- Subtle calculation differences

**Mitigations:**
1. **Business rule extraction workshops**: Before migrating each program, conduct workshops with domain experts to catalog every validation rule, edge case, and exception.
2. **Automated COBOL analysis**: Use COBOL analysis tools to extract condition trees from `EVALUATE` and `IF` statements, generating a decision table per program.
3. **Regression test suites from production data**: Replay anonymized production transactions through both legacy and modern systems; compare outcomes (accepted/rejected, field values).
4. **Incremental validation**: For `COACTUPC`, migrate and validate one field group at a time (dates, then names, then financial fields, then addresses).

**Owner:** Business Analyst Lead
**Status:** Open

---

### R-07: Performance Regression in Database Access Patterns
| Attribute | Value |
|---|---|
| **Category** | Technical / Performance |
| **Impact** | 4 — Slow response times affect user experience; slow batch affects processing windows |
| **Probability** | 2 — PostgreSQL is generally faster than VSAM, but incorrect indexing or query patterns can cause regressions |
| **Risk Score** | **8 (Medium)** |
| **Affected Phase** | All phases |

**Description:**
VSAM KSDS files are optimized for key-sequential access — the access patterns used by all CardDemo programs. PostgreSQL provides equivalent functionality but requires proper indexing, connection pooling, and query optimization. Specific risks:
- VSAM browse (STARTBR/READNEXT) translates to cursor-based pagination, which can be slower without proper indexing
- CARDAIX alternate index access requires a secondary index in PostgreSQL
- Batch programs processing millions of records may be slower if N+1 query patterns emerge
- Connection pool exhaustion during peak batch + online concurrent access

**Mitigations:**
1. **Create indexes matching VSAM access patterns**: Primary key indexes for all key-sequential access, plus secondary indexes for alternate index paths (`card_acct_id` on cards table, `tran_card_num` on transactions table).
2. **Performance testing with production-scale data**: Load PostgreSQL with production-equivalent data volumes before migrating any context. Benchmark key queries against legacy response times.
3. **Connection pool tuning**: Configure HikariCP with appropriate pool sizes for concurrent online + batch access. Separate connection pools for batch and online if needed.
4. **Batch optimization**: Use Spring Batch chunk-oriented processing with JDBC batch inserts/updates. Avoid N+1 patterns by using IN clauses or JOIN queries.

**Owner:** Database Lead
**Status:** Open

---

### R-08: VSAM Alternate Index Behavior Not Replicated Correctly
| Attribute | Value |
|---|---|
| **Category** | Technical |
| **Impact** | 3 — Incorrect card/account lookups return wrong data |
| **Probability** | 2 — Alternate indexes have specific ordering and duplicate-handling semantics |
| **Risk Score** | **6 (Medium)** |
| **Affected Phase** | Phase 3 (Card Management), Phase 4 (Account Management) |

**Description:**
CardDemo uses two alternate indexes: `CARDAIX` (cards by account ID) and `CXACAIX` (cross-reference by account). VSAM alternate indexes support browsing (STARTBR/READNEXT) over the alternate key, with records returned in alternate key + primary key order. PostgreSQL secondary indexes have different ordering semantics, and the application may depend on specific ordering (e.g., card list displays cards for an account in card-number order).

**Mitigations:**
1. **Document alternate index usage**: Catalog every STARTBR/READNEXT sequence that uses alternate indexes, noting the expected sort order.
2. **SQL ORDER BY clauses**: Explicitly replicate VSAM ordering with `ORDER BY` clauses (e.g., `ORDER BY card_acct_id, card_num` to match CARDAIX browse order).
3. **Comparison tests**: For each alternate index path, compare query results (row order, row content) between VSAM browse and SQL query.

**Owner:** Database Lead
**Status:** Open

---

## Medium Risks (Score 5–9)

### R-09: Mainframe Operational Staff Knowledge Gap for Modern Stack
| Attribute | Value |
|---|---|
| **Category** | Organizational / People |
| **Impact** | 5 — Operations team unable to support modern system causes outages |
| **Probability** | 1 — Mitigated by training, but risk exists |
| **Risk Score** | **5 (Medium)** |
| **Affected Phase** | All phases, especially Phase 8 (Decommission) |

**Description:**
Current operations staff are skilled in mainframe technologies (CICS, VSAM, JCL, CA7/Control-M). The modern Java/Spring Boot/PostgreSQL stack requires different skills: container orchestration, database administration, REST API monitoring, Spring Batch operations, and cloud infrastructure management.

**Mitigations:**
1. **Phased training program**: Begin training operations staff on Java/Spring Boot/PostgreSQL from Phase 1, not Phase 8.
2. **Runbook creation**: Create operations runbooks for every modern batch job and service before each phase goes live.
3. **Shadow operations**: Have mainframe ops team shadow the modern system operations for 2+ weeks before taking ownership.
4. **24/7 developer support**: During the first 4 weeks after each phase cutover, development team provides on-call support to operations.

**Owner:** Operations Manager
**Status:** Open

---

### R-10: Scope Creep from Optional Modules (IMS/DB2/MQ)
| Attribute | Value |
|---|---|
| **Category** | Project Management |
| **Impact** | 4 — Expanding scope delays core migration and introduces additional complexity |
| **Probability** | 2 — Optional modules are in scope but may pull in unexpected dependencies |
| **Risk Score** | **8 (Medium)** |
| **Affected Phase** | Phase 5+ (if optional modules are included) |

**Description:**
Three optional modules exist: IMS/DB2/MQ authorization (8 programs), DB2 transaction types (3 programs), and VSAM-MQ integration (2 programs). These introduce additional technology dependencies (IMS, DB2, MQ) that are not present in the core VSAM-based application. Including them in the migration scope adds complexity and may delay core functional area migration.

**Mitigations:**
1. **Defer optional modules to post-Phase 7**: Complete core VSAM-based migration before addressing IMS/DB2/MQ modules.
2. **Stub optional module interfaces**: Where core programs call optional modules (e.g., `COPAUS0C` for pending authorization view), create stub implementations that return default responses.
3. **Separate project stream**: If optional modules must be migrated, run as a parallel workstream with its own timeline and team.

**Owner:** Project Manager
**Status:** Open

---

### R-11: JCL Job Scheduling Dependencies Not Fully Captured
| Attribute | Value |
|---|---|
| **Category** | Technical / Operational |
| **Impact** | 1 — Incorrect job ordering causes batch failures that are detectable and recoverable |
| **Probability** | 3 — 38 JCL jobs with implicit ordering dependencies |
| **Risk Score** | **3 (Low)** |
| **Affected Phase** | Phase 6–7 (Batch Migration) |

**Description:**
The 38 JCL batch jobs have a specific execution order: `CLOSEFIL → data refresh → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL`. Some dependencies are implicit (e.g., `POSTTRAN` must complete before `INTCALC` because it populates `TCATBALF`). Scheduler configurations (CA7, Control-M in `app/scheduler/`) define these dependencies, but they may not cover all implicit data dependencies.

**Mitigations:**
1. **Map all data dependencies**: For each JCL job, document input files (must exist before job runs) and output files (created/modified by job). Use this to construct a complete dependency graph.
2. **Spring Batch job flow**: Encode dependencies as Spring Batch step dependencies with explicit preconditions (e.g., `InterestCalculationJob` cannot start until `TransactionPostingJob` completes successfully).
3. **Review scheduler configs**: Analyze `app/scheduler/` CA7 and Control-M configurations to capture all defined dependencies.

**Owner:** Batch Infrastructure Lead
**Status:** Open

---

### R-12: Security Vulnerabilities Introduced During Migration
| Attribute | Value |
|---|---|
| **Category** | Security |
| **Impact** | 3 — Security gaps could be exploited during the transition period |
| **Probability** | 2 — New API surface area increases attack surface; dual-run adds complexity |
| **Risk Score** | **6 (Medium)** |
| **Affected Phase** | Phase 1 (IAM) and all subsequent phases |

**Description:**
The legacy system's security model is simple (USRSEC file with plaintext passwords, user type 'A'/'U'). The modern system introduces a larger attack surface: REST APIs, JWT tokens, database credentials, and network exposure. During the dual-run period, both the legacy 3270 interface and modern web API are active, doubling the attack surface.

**Mitigations:**
1. **Security review at each phase gate**: Penetration testing before each phase goes to production.
2. **API security**: OAuth 2.0 / JWT for authentication, HTTPS everywhere, input validation on all API endpoints, rate limiting.
3. **Database security**: Encrypted connections, least-privilege database accounts per service, encrypted at rest.
4. **Audit logging**: Log all authentication events, data modifications, and administrative actions in both legacy and modern systems during dual-run.
5. **Retire legacy signon promptly**: Once Phase 1 (IAM) is validated, force all new authentication through the modern service to reduce attack surface.

**Owner:** Security Lead
**Status:** Open

---

### R-13: Testing Coverage Gaps Due to Lack of Existing Test Suites
| Attribute | Value |
|---|---|
| **Category** | Technical / Quality |
| **Impact** | 2 — Bugs escape to production; detected by users rather than tests |
| **Probability** | 3 — Legacy COBOL codebase has no automated test suites |
| **Risk Score** | **6 (Medium)** |
| **Affected Phase** | All phases |

**Description:**
The CardDemo COBOL codebase has no automated unit tests, integration tests, or regression tests. All testing has been manual via 3270 terminal interaction. The modern system must build its test suites from scratch, without a reference test suite to validate against. Risk is that edge cases and error paths are not covered.

**Mitigations:**
1. **Test-first migration**: For each migrated program, write comprehensive unit tests and integration tests before declaring the migration complete. Target 80%+ code coverage.
2. **Production data replay**: Capture anonymized production transactions and replay through both legacy and modern systems to generate a de facto regression test suite.
3. **BMS map walkthroughs**: Use BMS map definitions to enumerate all possible user interactions (every input field, every PF key, every error condition) and write test cases for each.
4. **Contract tests**: For API boundaries between services, write contract tests (e.g., Spring Cloud Contract) to ensure interface compatibility.

**Owner:** QA Lead
**Status:** Open

---

### R-14: Vendor Lock-in or Technology Obsolescence
| Attribute | Value |
|---|---|
| **Category** | Strategic |
| **Impact** | 2 — Technology choices made today may need revision in 3–5 years |
| **Probability** | 2 — Spring Boot and PostgreSQL are mature, widely-supported technologies |
| **Risk Score** | **4 (Low)** |
| **Affected Phase** | Foundation and all phases |

**Description:**
The target architecture (Spring Boot 3.x, PostgreSQL, potentially AWS-managed services) creates dependencies on specific vendors and frameworks. While these are mainstream choices, the modernization should not create a new form of vendor lock-in that is as constraining as the mainframe.

**Mitigations:**
1. **Portable abstractions**: Use Spring Data JPA (not vendor-specific SQL), standard JMS/AMQP (not vendor-specific messaging), and container-based deployment (Docker/Kubernetes, not cloud-specific).
2. **Database abstraction**: Use JPA/Hibernate with standard SQL where possible. Avoid PostgreSQL-specific extensions unless necessary.
3. **Cloud-agnostic deployment**: Containerize all services for Kubernetes deployment; avoid hard dependencies on AWS-specific services (or abstract them behind interfaces).

**Owner:** Architecture Lead
**Status:** Open

---

### R-15: Project Timeline Exceeds Budget or Stakeholder Patience
| Attribute | Value |
|---|---|
| **Category** | Project Management |
| **Impact** | 1 — Project may be cancelled or descoped, leaving a partially migrated system |
| **Probability** | 3 — 58-week timeline is ambitious; mainframe modernization projects commonly overrun |
| **Risk Score** | **3 (Low)** |
| **Affected Phase** | All phases |

**Description:**
The estimated 58-week (14-month) timeline assumes sequential phase execution with no major blockers. Mainframe modernization projects historically overrun by 30–50%. If the project exceeds budget or stakeholder patience, it may be paused or cancelled, leaving the organization with a partially migrated system that is more complex to operate than either the legacy or fully modern system.

**Mitigations:**
1. **Value delivery at each phase**: Each phase delivers independently usable functionality. Even if the project is paused after Phase 4, the organization has modern IAM, reporting, card management, and account management.
2. **Phase gates with go/no-go decisions**: Each phase boundary is a decision point where scope, timeline, and budget can be reassessed.
3. **Quick wins first**: Phases 1–2 (IAM + Reporting) deliver visible results within 14 weeks, building stakeholder confidence.
4. **Parallel workstreams**: If budget allows, Phases 3 and 4 can partially overlap (card management team and account management team working in parallel), compressing the timeline.

**Owner:** Project Sponsor
**Status:** Open

---

## Risk Register Summary

| ID | Risk | Impact | Prob. | Score | Category | Priority |
|---|---|---|---|---|---|---|
| R-01 | Financial data integrity loss | 5 | 4 | **20** | Technical/Data | Critical |
| R-02 | Batch posting failure cascade | 4 | 4 | **16** | Technical/Ops | Critical |
| R-03 | Interest calculation discrepancy | 5 | 3 | **15** | Financial | Critical |
| R-04 | Dual-run sync conflicts | 4 | 3 | **12** | Technical/Data | High |
| R-05 | CICS semantics lost in REST | 3 | 4 | **12** | Technical | High |
| R-06 | Undocumented business rules | 3 | 3 | **9** | Business | Medium |
| R-07 | Performance regression | 4 | 2 | **8** | Technical/Perf | Medium |
| R-08 | Alternate index behavior | 3 | 2 | **6** | Technical | Medium |
| R-09 | Ops staff knowledge gap | 5 | 1 | **5** | Organizational | Medium |
| R-10 | Scope creep from optional modules | 4 | 2 | **8** | PM | Medium |
| R-11 | JCL scheduling dependencies | 1 | 3 | **3** | Technical/Ops | Low |
| R-12 | Security vulnerabilities | 3 | 2 | **6** | Security | Medium |
| R-13 | Testing coverage gaps | 2 | 3 | **6** | Quality | Medium |
| R-14 | Vendor lock-in | 2 | 2 | **4** | Strategic | Low |
| R-15 | Timeline overrun | 1 | 3 | **3** | PM | Low |

---

## Risk Review Cadence

| Frequency | Activity |
|---|---|
| Weekly | Review Critical and High risks; update status and mitigations |
| Bi-weekly | Review Medium risks; identify any escalating trends |
| Phase gate | Full risk register review; re-score all risks based on current phase context |
| Monthly | Executive summary of top 5 risks to steering committee |

---

## Appendix: Risk Response Strategy Key

| Strategy | Description |
|---|---|
| **Avoid** | Change the plan to eliminate the risk entirely |
| **Mitigate** | Reduce probability or impact through proactive actions |
| **Transfer** | Shift risk to a third party (e.g., managed services, insurance) |
| **Accept** | Acknowledge the risk and prepare contingency plans |

All risks in this register use the **Mitigate** strategy with specific, actionable mitigations. No risks are accepted without mitigation.
