# CardDemo Risk Register

> Top risks for the COBOL-to-Java modernization, scored by likelihood and impact, with mitigations and contingencies.

---

## Risk Scoring

| Dimension | 1 | 2 | 3 | 4 | 5 |
|-----------|---|---|---|---|---|
| **Likelihood** | Rare | Unlikely | Possible | Likely | Almost Certain |
| **Impact** | Negligible | Minor | Moderate | Major | Critical |

**Risk Score** = Likelihood × Impact (max 25)

| Score Range | Rating | Action Required |
|-------------|--------|----------------|
| 1–4 | LOW | Monitor |
| 5–9 | MEDIUM | Mitigation plan required |
| 10–15 | HIGH | Active mitigation + contingency |
| 16–25 | CRITICAL | Escalation + dedicated mitigation track |

---

## Risk Summary

| # | Risk | Score | Rating |
|---|------|-------|--------|
| R01 | Financial calculation precision loss | 20 | CRITICAL |
| R02 | Data inconsistency during dual-run | 16 | CRITICAL |
| R03 | CLOSEFIL/OPENFIL elimination breaks batch integrity | 15 | HIGH |
| R04 | COACTUPC validation logic loss | 15 | HIGH |
| R05 | Transaction ID collision during migration | 12 | HIGH |
| R06 | COMMAREA state management gaps | 12 | HIGH |
| R07 | Plaintext password exposure during auth migration | 10 | HIGH |
| R08 | CARDXREF coupling causes cascading failures | 9 | MEDIUM |
| R09 | Batch cycle ordering violated | 9 | MEDIUM |
| R10 | BMS screen parity gaps in web UI | 6 | MEDIUM |
| R11 | COBOL SME availability | 8 | MEDIUM |
| R12 | Performance regression in high-volume batch | 10 | HIGH |

---

## Detailed Risk Analysis

### R01: Financial Calculation Precision Loss

| Attribute | Detail |
|-----------|--------|
| **Risk Score** | Likelihood: 4 × Impact: 5 = **20 (CRITICAL)** |
| **Affected Phase** | Phase 5 — Interest Calculation (CBACT04C) |
| **Description** | COBOL uses fixed-point decimal arithmetic (`PIC S9(09)V99`) with implicit rounding rules. Java BigDecimal uses different rounding modes by default. Subtle differences in interest calculations could compound across thousands of accounts over billing cycles, leading to balance discrepancies. |

**Root Cause:** COBOL `COMPUTE` with `ROUNDED` uses a specific truncation/rounding behavior that may not match Java's `RoundingMode.HALF_EVEN` or `HALF_UP` defaults.

**Mitigations:**
1. **Replatform first** — Auto-convert CBACT04C to Java preserving exact COBOL arithmetic semantics before refactoring
2. **Reconciliation suite** — Compare interest calculations account-by-account between old and new systems for 60+ days (two full billing cycles)
3. **Zero-tolerance threshold** — Any discrepancy >$0.00 blocks go-live
4. **BigDecimal configuration** — Explicitly set `RoundingMode.HALF_UP` with scale matching COBOL `V99` (2 decimal places)
5. **Test with edge cases** — Zero balances, maximum balances (`S9(10)V99` = ±9,999,999,999.99), negative balances, small fractions

**Contingency:** If precision cannot be matched, keep CBACT04C running on mainframe as a calculation service called via API bridge until exact behavioral match is proven.

**Owner:** Lead Java Developer + COBOL SME

---

### R02: Data Inconsistency During Dual-Run

| Attribute | Detail |
|-----------|--------|
| **Risk Score** | Likelihood: 4 × Impact: 4 = **16 (CRITICAL)** |
| **Affected Phase** | Phases 2–5 (any phase with bidirectional sync) |
| **Description** | During strangler migration, data is written to both VSAM and PostgreSQL. Sync lag, failed sync jobs, or race conditions between online and batch writes could cause the two stores to diverge, leading to incorrect balances, missing transactions, or phantom records. |

**Root Cause:** VSAM has no native change data capture (CDC). Bidirectional sync relies on polling or custom triggers, introducing latency windows where data can diverge.

**Mitigations:**
1. **Single writer per entity per phase** — During each phase, only one system (old or new) writes to each entity type. The other system is read-only from sync.
2. **Sync monitoring dashboard** — Real-time comparison of record counts and checksums between VSAM and database
3. **Idempotent sync** — Sync operations use upsert semantics with version/timestamp conflict resolution
4. **Sync lag SLA** — Maximum 5-minute lag between VSAM write and database reflection
5. **Daily reconciliation job** — Automated full-table comparison with alert on any discrepancy

**Contingency:** If sync divergence exceeds threshold, halt the affected phase and perform a full data reload from the authoritative source (VSAM during early phases, database during later phases).

**Owner:** Data Migration Lead

---

### R03: CLOSEFIL/OPENFIL Elimination Breaks Batch Integrity

| Attribute | Detail |
|-----------|--------|
| **Risk Score** | Likelihood: 3 × Impact: 5 = **15 (HIGH)** |
| **Affected Phase** | Phase 5 — Batch Transaction Posting |
| **Description** | The current batch cycle requires CLOSEFIL to lock CICS files for exclusive batch access. The modernized system must handle concurrent online and batch access to the same data without exclusive locking. Removing this constraint could cause dirty reads, lost updates, or deadlocks. |

**Root Cause:** VSAM provides file-level locking (all-or-nothing). Relational databases provide row-level locking and transactions, but the COBOL code was never designed for concurrent access.

**Mitigations:**
1. **Database transactions** — Wrap batch posting in database transactions with proper isolation levels (READ COMMITTED minimum)
2. **Optimistic locking** — Add version columns to account and transaction tables; batch aborts and retries on version conflict
3. **Batch window preservation** — Initially, keep a "soft" batch window where online writes are throttled (not blocked) during batch processing
4. **Integration testing** — Simulate concurrent online transactions during batch posting; verify no data corruption
5. **Gradual transition** — First phase uses CLOSEFIL equivalent (database advisory locks); second phase removes it

**Contingency:** If concurrent access causes issues, implement a database-level advisory lock that mimics CLOSEFIL behavior during batch runs.

**Owner:** Backend Architecture Lead

---

### R04: COACTUPC Validation Logic Loss

| Attribute | Detail |
|-----------|--------|
| **Risk Score** | Likelihood: 3 × Impact: 5 = **15 (HIGH)** |
| **Affected Phase** | Phase 3 — Account Management |
| **Description** | COACTUPC contains 4,236 lines with 30+ field-level validations embedded in procedural code (via COPY REPLACING of CSSETATY). These validations are not documented externally and are interleaved with BMS attribute control, making them difficult to extract completely. Missing a validation could allow invalid data into accounts. |

**Root Cause:** Validation logic is tightly coupled to presentation logic (BMS attribute highlighting). There's no separation of validation rules from screen formatting.

**Mitigations:**
1. **Validation catalog** — Create an exhaustive catalog of every validation in COACTUPC before writing any Java code. Map each EVALUATE/IF condition to a business rule.
2. **Automated extraction** — Parse COACTUPC's CSSETATY COPY REPLACING statements to identify every field that has validation logic
3. **Rule-by-rule testing** — Write a test case for each identified validation rule. Test must pass against both COBOL and Java implementations.
4. **COBOL SME review** — Dedicated COBOL SME reviews the validation catalog for completeness
5. **Bean Validation framework** — Implement validations as declarative annotations (`@NotNull`, `@Size`, `@Pattern`, custom validators) for maintainability

**Contingency:** If validation catalog is incomplete at go-live, keep COACTUPC available as a validation oracle — new system can call it via API bridge to cross-check critical updates.

**Owner:** Business Analyst + COBOL SME

---

### R05: Transaction ID Collision During Migration

| Attribute | Detail |
|-----------|--------|
| **Risk Score** | Likelihood: 3 × Impact: 4 = **12 (HIGH)** |
| **Affected Phase** | Phase 2C — Transaction Add |
| **Description** | COTRN02C generates transaction IDs using STARTBR/READPREV (finding the highest existing ID and incrementing). The new system uses UUIDs or database sequences. During dual-run, both systems may generate IDs for the same transaction, causing collisions or gaps when synced. |

**Root Cause:** No centralized ID generation authority during the transition period.

**Mitigations:**
1. **ID namespace separation** — New system uses UUID format; old system keeps numeric format. No collision possible between namespaces.
2. **Single ID authority** — During transition, new system generates IDs for both old and new transactions (old system calls new system's ID service)
3. **Database unique constraint** — Enforce uniqueness at database level regardless of generation method
4. **ID mapping table** — Maintain a mapping between old numeric IDs and new UUIDs for cross-reference during migration

**Contingency:** If collisions occur, the new system's ID takes precedence (newer format). Re-key any colliding old transactions.

**Owner:** Backend Developer

---

### R06: COMMAREA State Management Gaps

| Attribute | Detail |
|-----------|--------|
| **Risk Score** | Likelihood: 3 × Impact: 4 = **12 (HIGH)** |
| **Affected Phase** | Phases 2–3 (during strangler coexistence) |
| **Description** | CICS programs share state via a COMMAREA structure (COCOM01Y) that carries user identity, selected account, navigation history, and error flags. The new REST services are stateless (JWT + request params). During the transition, a user may navigate from a new service to a legacy CICS screen or vice versa, and the COMMAREA context would be lost or stale. |

**Root Cause:** Fundamental architecture mismatch — stateful CICS sessions vs. stateless REST APIs.

**Mitigations:**
1. **Session bridge service** — Maintain a server-side session store that maps JWT tokens to COMMAREA-equivalent state. When routing to legacy CICS, populate COMMAREA from session store.
2. **URL state encoding** — Carry essential context (account ID, card number) in URL parameters / query strings so state survives transitions
3. **Minimize cross-boundary navigation** — Extract entire functional areas (not individual screens) so users stay within one system per workflow
4. **Feature flags per workflow** — Route entire workflows (e.g., all card operations) to either old or new system, not individual screens

**Contingency:** If state management is too complex, skip the strangler for tightly coupled workflows and batch-extract them (refactor approach instead).

**Owner:** Frontend/Integration Lead

---

### R07: Plaintext Password Exposure During Auth Migration

| Attribute | Detail |
|-----------|--------|
| **Risk Score** | Likelihood: 2 × Impact: 5 = **10 (HIGH)** |
| **Affected Phase** | Phase 1A — Identity & Access Management |
| **Description** | USRSEC stores passwords in plaintext (`PIC X(08)`). During migration to BCrypt hashes, plaintext passwords must be read from VSAM and processed. If this migration is mishandled, passwords could be logged, exposed in transit, or stored unhashed. |

**Root Cause:** Legacy security design with no encryption.

**Mitigations:**
1. **One-time migration script** — Read USRSEC, hash each password with BCrypt, write to new `users` table. Script runs once in secure environment.
2. **No logging of passwords** — Ensure migration script suppresses all password values from logs
3. **Secure transit** — Migration script runs on same network as VSAM and database; no passwords cross network boundaries
4. **Immediate VSAM cleanup** — After migration, consider blanking passwords in USRSEC if still operational (or accepting the existing risk during dual-run)
5. **Force password reset** — Optionally require all users to reset passwords on first login to new system

**Contingency:** If passwords are compromised during migration, force immediate password reset for all users.

**Owner:** Security Lead

---

### R08: CARDXREF Coupling Causes Cascading Failures

| Attribute | Detail |
|-----------|--------|
| **Risk Score** | Likelihood: 3 × Impact: 3 = **9 (MEDIUM)** |
| **Affected Phase** | Phases 2–4 |
| **Description** | CARDXREF is accessed by 12 programs across 4 bounded contexts. When Card Management is extracted to a microservice (Phase 2A), all other contexts that currently read CARDXREF VSAM must be updated to call the Card Service API instead. If the Card Service is unavailable, cascading failures could affect account view, transaction processing, reporting, and statements. |

**Mitigations:**
1. **Circuit breaker** — All API calls to Card Service use circuit breaker pattern (Resilience4j/Hystrix)
2. **Cache layer** — CARDXREF data is relatively static; cache card→account mappings with 5-minute TTL
3. **Fallback** — If Card Service is down, fall back to cached mapping or direct database read (during transition, VSAM read)
4. **Phase ordering** — Extract Card Service early (Phase 2A) to give maximum stabilization time before dependent contexts migrate

**Contingency:** If Card Service instability persists, denormalize card→account mapping into each consuming service's database (trade consistency for availability).

**Owner:** Platform/SRE Lead

---

### R09: Batch Cycle Ordering Violated

| Attribute | Detail |
|-----------|--------|
| **Risk Score** | Likelihood: 3 × Impact: 3 = **9 (MEDIUM)** |
| **Affected Phase** | Phases 4–5 |
| **Description** | The batch cycle must run in strict order: CLOSEFIL → data refresh → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL. As individual jobs are modernized at different times, maintaining the correct ordering across old JCL and new Spring Batch jobs becomes complex. |

**Mitigations:**
1. **Orchestration service** — Implement a batch orchestrator (Spring Cloud Data Flow or Apache Airflow) that manages the execution order regardless of whether each step is JCL or Spring Batch
2. **Dependency graph** — Formalize batch dependencies as a DAG; orchestrator enforces ordering
3. **Step completion signals** — Each batch step (old or new) signals completion to orchestrator before next step starts
4. **Integration testing** — Test full batch cycle end-to-end with mixed old/new steps after each phase

**Contingency:** If orchestration is too complex, modernize the entire batch cycle as a single phase (consolidating Phases 4–5) to avoid mixed-mode ordering issues.

**Owner:** Batch Processing Lead

---

### R10: BMS Screen Parity Gaps in Web UI

| Attribute | Detail |
|-----------|--------|
| **Risk Score** | Likelihood: 3 × Impact: 2 = **6 (MEDIUM)** |
| **Affected Phase** | All online phases |
| **Description** | BMS 3270 screens have specific field positioning, attribute control (protected/unprotected, bright/dark), and PF key navigation that users are accustomed to. The new web UI may not replicate these behaviors exactly, causing user confusion or workflow disruption. |

**Mitigations:**
1. **Screen mapping document** — Catalog every BMS map field, attribute, and PF key action; map to web UI equivalent
2. **User acceptance testing** — Involve end users early in UI design; iterate on feedback
3. **Keyboard shortcuts** — Implement keyboard shortcuts that mirror PF key functions (F3=Exit, F7=PageUp, F8=PageDown)
4. **Training materials** — Create side-by-side comparison guides (3270 screen vs. web page)

**Contingency:** If user resistance is high, provide a "classic" UI theme that mimics 3270 screen layout.

**Owner:** UX Lead

---

### R11: COBOL SME Availability

| Attribute | Detail |
|-----------|--------|
| **Risk Score** | Likelihood: 4 × Impact: 2 = **8 (MEDIUM)** |
| **Affected Phase** | All phases |
| **Description** | COBOL expertise is increasingly scarce. The project requires COBOL SMEs to validate business rules, review auto-converted code, and explain undocumented behavior. Loss of SME availability could stall critical phases. |

**Mitigations:**
1. **Knowledge capture** — Document all COBOL SME explanations as they happen; maintain a living knowledge base
2. **Pair programming** — Java developers work alongside COBOL SMEs to transfer knowledge bidirectionally
3. **Early engagement** — Involve COBOL SMEs from Phase 1 (even though it's low-risk) to build working relationships
4. **Automated analysis tools** — Use this codebase analysis (APPLICATION_INVENTORY, DATA_DICTIONARY, DEPENDENCY_MAP) as a knowledge base to reduce dependency on SMEs for basic questions

**Contingency:** If COBOL SME becomes unavailable, use the analysis artifacts in this repository plus automated COBOL parsing tools to continue. Escalate complex behavioral questions to external COBOL consulting firms.

**Owner:** Project Manager

---

### R12: Performance Regression in High-Volume Batch

| Attribute | Detail |
|-----------|--------|
| **Risk Score** | Likelihood: 2 × Impact: 5 = **10 (HIGH)** |
| **Affected Phase** | Phase 5 — Batch Transaction Posting |
| **Description** | VSAM KSDS with sequential access is highly optimized for batch processing (buffered I/O, CI/CA splits). A naive JDBC implementation may be significantly slower for high-volume transaction posting (100K+ records), causing the batch window to overrun. |

**Mitigations:**
1. **Batch JDBC optimization** — Use Spring Batch chunk processing with JDBC batch inserts (not individual INSERTs)
2. **Performance benchmarking** — Benchmark new batch against old with production-volume data before go-live
3. **Database tuning** — Proper indexing, connection pooling, and transaction batch sizes
4. **Parallel processing** — Spring Batch supports partitioned steps for parallel execution (COBOL batch is single-threaded)
5. **Performance SLA** — Define maximum batch window duration; new system must complete within same window as old

**Contingency:** If batch performance is insufficient, implement multi-threaded partitioned processing or consider in-memory caching for lookup tables (CARDXREF, DISCGRP).

**Owner:** Performance Engineer

---

## Risk Heat Map

```
Impact →      1          2          3          4          5
            Negligible  Minor     Moderate    Major     Critical
Likelihood
    5                                                    R01
    4                   R11                   R02
    3                   R10        R08,R09    R05,R06   R03,R04
    2                                                   R07,R12
    1
```

---

## Risk Response Summary

| Response Type | Risks | Description |
|--------------|-------|-------------|
| **Avoid** | R07 | Force password reset; eliminate plaintext storage immediately |
| **Mitigate** | R01, R02, R03, R04, R05, R06, R08, R09, R12 | Active mitigation plans with monitoring |
| **Accept** | R10, R11 | Managed through training and knowledge capture |
| **Transfer** | R11 (partial) | External COBOL consulting as backup |

---

## Monitoring & Escalation

| Trigger | Action |
|---------|--------|
| Any financial discrepancy >$0.00 during dual-run | Halt affected phase; escalate to project sponsor |
| Sync lag exceeds 15 minutes | Alert on-call; investigate root cause |
| Batch window overruns by >20% | Performance review; consider optimization sprint |
| COBOL SME unavailable for >1 week during critical phase | Engage backup SME or external consultant |
| >3 production incidents in any phase | Phase rollback; root cause analysis before resuming |
