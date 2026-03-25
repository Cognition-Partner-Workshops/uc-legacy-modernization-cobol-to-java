# CardDemo Risk Register

## Overview

This register identifies the top risks for the CardDemo COBOL-to-Java modernization initiative, organized by impact and likelihood. Each risk includes specific mitigation strategies, early warning indicators, and contingency plans. Risks are mapped to the cutover phases defined in `CUTOVER_PLAN.md`.

---

## Risk Scoring Matrix

| | **Low Impact** | **Medium Impact** | **High Impact** | **Critical Impact** |
|---|---|---|---|---|
| **Very Likely** | Medium | High | Critical | Critical |
| **Likely** | Low | Medium | High | Critical |
| **Possible** | Low | Medium | High | High |
| **Unlikely** | Low | Low | Medium | High |

---

## Critical Risks

### R01: Financial Calculation Discrepancies

| Attribute | Value |
|-----------|-------|
| **Risk ID** | R01 |
| **Category** | Data Integrity |
| **Phase** | Phase 4 (Transaction Processing & Financial Operations) |
| **Likelihood** | Likely |
| **Impact** | Critical |
| **Risk Score** | **CRITICAL** |

**Description:**
COBOL uses fixed-point decimal arithmetic (PIC S9(09)V99) with implicit rounding rules that differ from Java floating-point. The transaction posting engine (CBTRN02C), interest calculator (CBACT04C), and bill payment processor (COBIL00C) all perform financial calculations. Any discrepancy -- even by one cent -- across millions of transactions compounds into material financial errors.

**Specific Concern:**
- COBOL `COMPUTE` with `PIC S9(09)V99` truncates (not rounds) by default
- Java `double`/`float` introduces IEEE 754 floating-point errors
- COMP-3 (packed decimal) in CBSTM03A has specific binary representations
- Interest rate lookups in DISCGRP use `PIC S9(04)V99` -- 6-digit precision

**Mitigations:**
1. **Mandatory:** Use `java.math.BigDecimal` with `scale=2` and `RoundingMode.DOWN` (matching COBOL truncation) for all financial fields
2. **Mandatory:** Implement penny-level parallel run comparison for 2+ complete batch cycles before cutover
3. Create a COBOL-arithmetic compatibility library that replicates `PIC S9(n)Vnn` behavior exactly
4. Write >500 unit tests with known COBOL output values covering edge cases: zero amounts, maximum amounts (9,999,999,999.99), negative amounts, boundary rounding scenarios
5. Engage external auditor to verify financial calculation parity

**Early Warning Indicators:**
- Any non-zero difference in parallel run comparison reports
- Rounding behavior differences detected in unit tests
- Balance drift between VSAM and RDBMS during bidirectional sync

**Contingency:**
Revert to mainframe batch processing. DALYTRAN input files are idempotent; reprocessing through CBTRN02C produces correct results. Estimated recovery: 2-4 hours.

---

### R02: Data Loss During Bidirectional Sync

| Attribute | Value |
|-----------|-------|
| **Risk ID** | R02 |
| **Category** | Data Integrity |
| **Phase** | Phase 3-4 (Account Management, Transaction Processing) |
| **Likelihood** | Possible |
| **Impact** | Critical |
| **Risk Score** | **HIGH** |

**Description:**
During the transition period (Phases 3-4), ACCTDAT is written by both the new Java Account Service and legacy mainframe programs (CBTRN02C for posting, COBIL00C for bill payment, CBACT04C for interest). The bidirectional sync between VSAM and RDBMS must handle concurrent writes from both systems without data loss or corruption.

**Specific Concern:**
- ACCTDAT balance field (ACCT-CURR-BAL) is updated by 4 different programs across 2 systems
- CICS pseudo-conversational locking and database optimistic locking use different consistency models
- Batch window timing: mainframe batch (CLOSEFIL/OPENFIL) quiesces online access, but Java services don't pause
- Network latency between mainframe and cloud could cause sync delays

**Mitigations:**
1. Implement Change Data Capture (CDC) with exactly-once delivery guarantees
2. Use a conflict resolution strategy: **mainframe wins** during transition (mainframe is source of truth until explicit cutover)
3. Add reconciliation job that runs every 15 minutes comparing VSAM and RDBMS record counts and balance checksums
4. During batch window, temporarily pause Java writes to ACCTDAT-equivalent tables
5. Maintain a detailed audit log of every balance change with source system identifier
6. Pre-cutover: run a full data reconciliation and resolve any discrepancies before switching source of truth

**Early Warning Indicators:**
- Reconciliation job detects record count mismatch
- Balance checksum divergence > 0
- CDC lag exceeds 5 seconds
- Concurrent update conflicts in audit log

**Contingency:**
Halt migration, restore VSAM from most recent backup, replay missed transactions from audit log. Estimated recovery: 4-8 hours.

---

### R03: COBOL Business Rule Loss

| Attribute | Value |
|-----------|-------|
| **Risk ID** | R03 |
| **Category** | Functional |
| **Phase** | Phase 3-4 (Account Management, Transaction Processing) |
| **Likelihood** | Likely |
| **Impact** | High |
| **Risk Score** | **HIGH** |

**Description:**
The CardDemo COBOL programs embed critical business rules in procedural code that may be overlooked during migration. COACTUPC alone contains 4,237 lines with extensive validation logic for SSN, phone numbers, dates, credit limits, and FICO scores. Implicit COBOL behaviors (e.g., space-filling, zero-filling, implicit type conversions, EVALUATE TRUE fall-through) have no direct Java equivalent.

**Specific Concern:**
- COACTUPC defines ~50 edit flags (lines 183-274) for field-level validation
- COBOL `EVALUATE TRUE` with multiple `WHEN` conditions has specific fall-through behavior
- Implicit space/zero padding in PIC X and PIC 9 fields (e.g., PIC X(16) right-pads with spaces)
- COBOL comparison semantics differ from Java (alphabetic comparison, numeric comparison with different PIC types)
- Undocumented business rules embedded in conditional logic

**Mitigations:**
1. Conduct line-by-line code review of all programs >500 LOC with COBOL SME present
2. Extract every EVALUATE, IF, and PERFORM UNTIL block into a decision table
3. Write behavior-driven tests (BDD) from the COBOL code before writing any Java code
4. Create a "COBOL Behavior Compatibility Checklist" covering: space padding, zero filling, sign handling (leading vs trailing), decimal alignment, alphanumeric comparison rules
5. Implement integration tests that exercise the same input data through both systems and compare outputs
6. Retain COBOL SME on the team through Phase 4 completion

**Early Warning Indicators:**
- Shadow testing reveals behavioral differences
- QA finds edge cases not covered by test suite
- Users report unexpected behavior vs mainframe system

**Contingency:**
For any discovered rule gap: immediately implement the missing rule in Java, add regression test, and re-run shadow comparison. No data impact since rules affect validation/processing logic.

---

## High Risks

### R04: CCXREF Cross-Context Dependency Bottleneck

| Attribute | Value |
|-----------|-------|
| **Risk ID** | R04 |
| **Category** | Architecture |
| **Phase** | Phase 2-4 (Card Management through Financial Operations) |
| **Likelihood** | Possible |
| **Impact** | High |
| **Risk Score** | **HIGH** |

**Description:**
The Card Cross-Reference file (CCXREF/CVACT03Y) is read by 7 programs across 5 bounded contexts. When Card Management migrates in Phase 2 and exposes the Card Lookup API, all remaining mainframe programs still reading CCXREF directly must either continue using the VSAM file or be updated to call the API. Any latency or availability issue with the Card Lookup API cascades across the entire system.

**Mitigations:**
1. Implement Redis cache in front of Card Lookup API with TTL-based invalidation
2. Maintain VSAM CCXREF as read-only replica during transition (write-through from Card Service)
3. Design API with batch lookup endpoint for batch programs (avoid N+1 calls)
4. Set SLA: Card Lookup API must sustain <10ms p99 latency at 1000 req/sec
5. Implement circuit breaker pattern -- if API is unavailable, fall back to cached data

**Early Warning Indicators:**
- Card Lookup API latency exceeds 50ms p95
- Cache hit ratio drops below 90%
- Batch job duration increases due to API call overhead

**Contingency:**
Re-enable direct VSAM CCXREF reads for affected programs. The bidirectional sync ensures VSAM data is current.

---

### R05: Batch Window Timing Violation

| Attribute | Value |
|-----------|-------|
| **Risk ID** | R05 |
| **Category** | Performance |
| **Phase** | Phase 4-5 (Transaction Processing, Reporting) |
| **Likelihood** | Possible |
| **Impact** | High |
| **Risk Score** | **HIGH** |

**Description:**
The mainframe batch cycle (CLOSEFIL -> POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT -> TRANIDX -> OPENFIL) must complete within a fixed overnight window. The replacement Spring Batch jobs must meet the same performance requirements. CBSTM03A processes up to 51 cards x 10 transactions per statement with 2D array manipulation -- the Java equivalent must handle equivalent throughput.

**Mitigations:**
1. Performance-test Spring Batch jobs with production-volume data (100K+ transactions) during Phase 4
2. Implement chunk-oriented processing with optimal chunk sizes (tune via experiment)
3. Use database connection pooling (HikariCP) with appropriate pool sizes
4. For statement generation: use parallel step execution (partition by account range)
5. Profile and optimize: identify bottlenecks in data access patterns (N+1 queries, missing indexes)
6. Establish performance baseline: measure current mainframe batch execution time as the target

**Early Warning Indicators:**
- Spring Batch job duration exceeds 80% of mainframe equivalent
- Database CPU/IO saturation during batch execution
- Increasing job duration trend over successive runs

**Contingency:**
Scale database instance vertically or add read replicas. If timing cannot be met, consider running critical batch jobs (posting, interest) on mainframe while non-critical jobs (statements, reports) run on Java.

---

### R06: COBOL Knowledge Drain

| Attribute | Value |
|-----------|-------|
| **Risk ID** | R06 |
| **Category** | Organizational |
| **Phase** | All Phases |
| **Likelihood** | Likely |
| **Impact** | High |
| **Risk Score** | **HIGH** |

**Description:**
Understanding the COBOL codebase requires specialized skills. Key programs use archaic patterns: CBSTM03A uses ALTER/GO TO (obsolete since COBOL-85), pointer-based PSA/TCB/TIOT addressing, and COMP/COMP-3 packed decimal variables. If the COBOL SME leaves or becomes unavailable during the migration, critical knowledge about implicit behaviors and undocumented business rules may be lost.

**Mitigations:**
1. **Immediate:** Conduct knowledge extraction sessions with COBOL SME, recording all sessions
2. Document every implicit COBOL behavior discovered during migration in a shared knowledge base
3. Create a "COBOL Patterns Dictionary" mapping every COBOL construct used in CardDemo to its Java equivalent
4. Cross-train at least 2 Java developers in COBOL fundamentals
5. Contract with COBOL SME through Phase 5 with retention bonus at project completion
6. Prioritize migration of programs with archaic patterns (CBSTM03A) to rewrite rather than refactor -- reducing dependency on deep COBOL expertise

**Early Warning Indicators:**
- COBOL SME availability drops below 50% of committed time
- Questions about COBOL behavior go unanswered for >2 days
- Migration velocity decreases in Phase 3-4

**Contingency:**
Engage external COBOL consulting firm. The CardDemo codebase (31 programs) is small enough for external analysis within 2-4 weeks.

---

### R07: Distributed Transaction Consistency (Bill Payment Saga)

| Attribute | Value |
|-----------|-------|
| **Risk ID** | R07 |
| **Category** | Architecture |
| **Phase** | Phase 4C (Financial Operations) |
| **Likelihood** | Possible |
| **Impact** | High |
| **Risk Score** | **HIGH** |

**Description:**
COBIL00C currently executes bill payment as a pseudo-atomic CICS operation: read account, create transaction record, update balance -- all within a single CICS unit of work. The microservice equivalent requires a saga pattern spanning Account Service, Card Service, and Transaction Service. Partial failures (e.g., transaction created but balance not updated) could leave the system in an inconsistent state.

**Mitigations:**
1. Implement the Orchestrator Saga pattern (not Choreography) for bill payment -- a central coordinator manages the flow
2. Define compensating transactions for each step:
   - Transaction created -> compensate: void/reverse the transaction
   - Balance debited -> compensate: credit the balance back
3. Use the Outbox pattern: write saga events to a local outbox table, then publish -- ensures at-least-once delivery
4. Implement idempotency keys on all financial operations to handle retries safely
5. Add a reconciliation job that detects orphaned saga instances and alerts operations
6. Extensive failure injection testing (Chaos Engineering): network partitions, service timeouts, database deadlocks

**Early Warning Indicators:**
- Orphaned saga instances detected by reconciliation job
- Compensating transaction rate exceeds 1% of total transactions
- Saga completion time exceeds 5 seconds p95

**Contingency:**
For irreconcilable saga failures: manual intervention queue with operations dashboard. Alert on any saga stuck for >10 minutes.

---

## Medium Risks

### R08: EBCDIC-to-ASCII Data Corruption

| Attribute | Value |
|-----------|-------|
| **Risk ID** | R08 |
| **Category** | Data Integrity |
| **Phase** | Phase 0-1 (Foundation, Data Migration) |
| **Likelihood** | Possible |
| **Impact** | Medium |
| **Risk Score** | **MEDIUM** |

**Description:**
VSAM files on the mainframe use EBCDIC encoding. Data migration to the target RDBMS requires EBCDIC-to-ASCII conversion. Packed decimal fields (COMP-3), zoned decimal fields, and special characters may not convert correctly. The sample data in `app/data/EBCDIC/` vs `app/data/ASCII/` provides a reference, but production data may contain values not represented in samples.

**Mitigations:**
1. Use the `app/data/ASCII/` and `app/data/EBCDIC/` directories as conversion validation test data
2. Build a conversion test suite that round-trips: EBCDIC -> ASCII -> EBCDIC and verifies byte-level equality
3. Handle special cases: packed decimal unpacking, signed fields (trailing overpunch), FILLER bytes
4. Validate every field post-conversion: numeric fields are valid numbers, dates are valid dates, status codes are in expected value sets
5. Run conversion on a full copy of production data and spot-check 1% of records manually

**Early Warning Indicators:**
- Conversion validation failures on sample data
- Invalid numeric values post-conversion
- Character encoding artifacts (garbled names, addresses)

**Contingency:**
Re-run conversion with corrected mapping tables. Source VSAM data is never modified -- conversion is repeatable.

---

### R09: API Gateway as Single Point of Failure

| Attribute | Value |
|-----------|-------|
| **Risk ID** | R09 |
| **Category** | Infrastructure |
| **Phase** | Phase 1 onwards |
| **Likelihood** | Unlikely |
| **Impact** | Critical |
| **Risk Score** | **MEDIUM** |

**Description:**
The Strangler Fig pattern routes all traffic through an API gateway. During the transition period, this gateway must route to both the new Java services and the mainframe proxy. A gateway failure would make the entire application unavailable.

**Mitigations:**
1. Deploy API gateway in high-availability configuration (minimum 3 instances across availability zones)
2. Implement health checks with automatic failover
3. Use infrastructure-as-code for rapid redeployment
4. Gateway config must be version-controlled with instant rollback capability
5. Rate limiting and circuit breakers to prevent cascade failures
6. Load test gateway at 2x expected peak traffic

**Early Warning Indicators:**
- Gateway error rate exceeds 0.1%
- Gateway latency p99 exceeds 200ms
- Instance health check failures

**Contingency:**
Bypass gateway and route directly to services via DNS change. Maintain a "break glass" DNS configuration that routes to mainframe directly.

---

### R10: Incomplete BMS-to-Web UI Mapping

| Attribute | Value |
|-----------|-------|
| **Risk ID** | R10 |
| **Category** | Functional |
| **Phase** | Phase 1-2 (Presentation Tier) |
| **Likelihood** | Possible |
| **Impact** | Medium |
| **Risk Score** | **MEDIUM** |

**Description:**
The 17 BMS maps define 3270 terminal screens with specific field attributes (protected, unprotected, bright, dark, numeric-only). The PF key navigation model (PF3=Back, PF4=Clear, PF5=Copy, PF7/PF8=Page) has no direct web equivalent. Users accustomed to the 3270 workflow may find the web UI disorienting.

**Mitigations:**
1. Map every BMS field attribute to an HTML form equivalent (protected -> readonly, numeric-only -> type="number", bright -> highlight CSS)
2. Implement keyboard shortcuts that mirror PF key behavior (Ctrl+B=Back, Ctrl+L=Clear)
3. Conduct user acceptance testing (UAT) with actual 3270 users during Phase 1
4. Provide a "classic view" option that mimics the 3270 layout for users in transition
5. Document the BMS-to-Web field mapping for all 17 screens

**Early Warning Indicators:**
- UAT feedback indicates missing functionality
- Users report workflow disruptions
- Support ticket volume increases post-Phase 1

**Contingency:**
Maintain 3270 terminal access in parallel until web UI achieves user acceptance. No data impact -- both UIs share the same backend.

---

### R11: Regression in Batch Job Orchestration Order

| Attribute | Value |
|-----------|-------|
| **Risk ID** | R11 |
| **Category** | Operational |
| **Phase** | Phase 4-5 (Batch Migration) |
| **Likelihood** | Possible |
| **Impact** | Medium |
| **Risk Score** | **MEDIUM** |

**Description:**
The mainframe batch cycle has strict ordering dependencies enforced by JCL JOB/STEP sequencing and the CA-7/Control-M scheduler. The replacement Spring Batch orchestration must preserve: POSTTRAN before INTCALC, INTCALC before CREASTMT, and the CLOSEFIL/OPENFIL quiesce pattern. Misordering could cause interest calculation on unposted transactions or statements with missing data.

**Mitigations:**
1. Model batch dependencies as a directed acyclic graph (DAG) in the Spring Batch orchestrator
2. Each job checks preconditions: "posting job completed successfully today" before interest calculation starts
3. Implement job execution metadata table tracking: job name, start time, end time, status, record counts
4. Alert on any out-of-order execution or missing prerequisite job
5. Replicate the CLOSEFIL/OPENFIL pattern: during batch window, set a "batch-in-progress" flag that online services check before allowing writes

**Early Warning Indicators:**
- Job execution metadata shows out-of-order start times
- Interest calculation runs on stale data
- Statement record counts don't match posting output

**Contingency:**
Abort the batch cycle, restore database to pre-batch-window snapshot, re-run in correct order.

---

### R12: Security Regression (Plaintext Password Migration)

| Attribute | Value |
|-----------|-------|
| **Risk ID** | R12 |
| **Category** | Security |
| **Phase** | Phase 1 (Identity & Access Management) |
| **Likelihood** | Unlikely |
| **Impact** | High |
| **Risk Score** | **MEDIUM** |

**Description:**
The current USRSEC VSAM file stores passwords in plaintext (SEC-USR-PWD PIC X(08)). During migration to the Identity Service, all passwords must be hashed with BCrypt. If the migration process exposes plaintext passwords (in logs, migration scripts, or temporary files) or if the hashing is implemented incorrectly, it creates a security vulnerability.

**Mitigations:**
1. **Never** log, display, or write plaintext passwords during migration
2. Migration script: read USRSEC -> BCrypt hash each password -> write to users table -> immediately discard plaintext
3. Run migration script in a secure, isolated environment
4. Enforce password rotation: require all users to change passwords within 30 days of migration
5. Implement password complexity requirements in the new system (the old 8-character limit is inadequate)
6. Penetration test the Identity Service before Phase 1 exit

**Early Warning Indicators:**
- Plaintext passwords found in any log file or temporary storage
- BCrypt hash verification fails for migrated passwords
- Security scan detects vulnerabilities in Identity Service

**Contingency:**
If plaintext passwords are exposed: force immediate password reset for all users, notify security team, conduct incident review.

---

## Low Risks

### R13: Spring Batch Learning Curve

| Attribute | Value |
|-----------|-------|
| **Risk ID** | R13 |
| **Category** | Organizational |
| **Phase** | Phase 4 |
| **Likelihood** | Possible |
| **Impact** | Low |
| **Risk Score** | **LOW** |

**Description:**
The development team may lack experience with Spring Batch chunk-oriented processing, skip/retry policies, and job restartability. This could slow Phase 4 delivery.

**Mitigations:**
1. Conduct Spring Batch training workshop before Phase 4 begins
2. Start with simpler batch jobs (CBACT01C data refresh) before tackling CBTRN02C posting
3. Use Spring Batch's built-in restart, skip, and retry capabilities from the start
4. Assign experienced Spring Batch developer as tech lead for Phase 4

**Contingency:**
Engage Spring Batch consulting support for 4-6 weeks during Phase 4.

---

### R14: Mainframe License Cost During Transition

| Attribute | Value |
|-----------|-------|
| **Risk ID** | R14 |
| **Category** | Financial |
| **Phase** | All Phases |
| **Likelihood** | Likely |
| **Impact** | Low |
| **Risk Score** | **LOW** |

**Description:**
During the 18-24 month migration, the organization pays for both mainframe and cloud infrastructure. Extended transition increases dual-run costs.

**Mitigations:**
1. Negotiate mainframe license reduction as workload decreases (after each phase cutover)
2. Decommission mainframe CICS regions as programs are migrated (reduce MIPS consumption)
3. Track and report dual-run costs monthly to maintain urgency
4. Set hard deadline for mainframe decommission (end of Phase 5)

**Contingency:**
If budget is exceeded, prioritize Phase 4 completion to decommission the most expensive mainframe batch processing.

---

### R15: Optional Module Scope Creep

| Attribute | Value |
|-----------|-------|
| **Risk ID** | R15 |
| **Category** | Scope |
| **Phase** | Phase 5 |
| **Likelihood** | Possible |
| **Impact** | Low |
| **Risk Score** | **LOW** |

**Description:**
The optional modules (Authorization IMS/DB2/MQ with 8 programs, Transaction Type DB2 with 3 programs, VSAM-MQ with 2 programs) introduce additional technology dependencies (IMS DB, DB2, MQ Series) that are not present in the core CardDemo. Including them without proper planning could delay Phase 5.

**Mitigations:**
1. Treat optional modules as a separate Phase 5b -- do not let them block core decommission
2. Evaluate each module independently: if functionality is not needed, skip migration entirely
3. If IMS/DB2 modules are needed, they require their own database migration plan (DB2-to-PostgreSQL)
4. MQ integration can be replaced with Spring AMQP or Kafka -- evaluate based on message volume and patterns

**Contingency:**
Defer optional modules to a post-migration enhancement phase. Core system operates without them.

---

## Risk Summary Dashboard

| Risk ID | Risk | Score | Phase | Status |
|---------|------|-------|-------|--------|
| R01 | Financial Calculation Discrepancies | **CRITICAL** | 4 | Open |
| R02 | Data Loss During Bidirectional Sync | **HIGH** | 3-4 | Open |
| R03 | COBOL Business Rule Loss | **HIGH** | 3-4 | Open |
| R04 | CCXREF Cross-Context Dependency | **HIGH** | 2-4 | Open |
| R05 | Batch Window Timing Violation | **HIGH** | 4-5 | Open |
| R06 | COBOL Knowledge Drain | **HIGH** | All | Open |
| R07 | Distributed Transaction Consistency | **HIGH** | 4C | Open |
| R08 | EBCDIC-to-ASCII Data Corruption | MEDIUM | 0-1 | Open |
| R09 | API Gateway Single Point of Failure | MEDIUM | 1+ | Open |
| R10 | Incomplete BMS-to-Web UI Mapping | MEDIUM | 1-2 | Open |
| R11 | Batch Job Orchestration Order | MEDIUM | 4-5 | Open |
| R12 | Security Regression (Passwords) | MEDIUM | 1 | Open |
| R13 | Spring Batch Learning Curve | LOW | 4 | Open |
| R14 | Mainframe License Cost | LOW | All | Open |
| R15 | Optional Module Scope Creep | LOW | 5 | Open |

---

## Risk Review Cadence

| Activity | Frequency | Participants |
|----------|-----------|--------------|
| Risk register review | Bi-weekly | Project Manager, Tech Lead, COBOL SME |
| Phase-specific risk assessment | At each phase gate | Full project team |
| Financial calculation audit | Weekly during Phase 4 | Tech Lead, DBA, Finance representative |
| Data reconciliation review | Daily during Phase 3-4 transition | DBA, DevOps |
| Executive risk briefing | Monthly | Steering Committee |
