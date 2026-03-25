# CardDemo Modernization Risk Register

> **Generated:** 2026-03-25 | **Scope:** Top risks identified from codebase analysis with mitigation strategies

---

## 1. Risk Scoring Methodology

| Dimension | 1 (Very Low) | 2 (Low) | 3 (Medium) | 4 (High) | 5 (Very High) |
|-----------|-------------|---------|-----------|----------|---------------|
| **Probability** | < 5% | 5–20% | 20–50% | 50–80% | > 80% |
| **Impact** | Cosmetic issue | Minor delay (< 1 week) | Moderate delay (1–4 weeks) | Major delay (1–3 months) | Project failure |

**Risk Score = Probability × Impact** (range: 1–25)

| Score Range | Level | Action |
|-------------|-------|--------|
| 1–4 | Low | Monitor |
| 5–9 | Medium | Mitigate |
| 10–15 | High | Active management required |
| 16–25 | Critical | Escalate immediately |

---

## 2. Risk Register

### R01 — COACTUPC Business Rule Loss

| Attribute | Detail |
|-----------|--------|
| **Category** | Technical |
| **Description** | COACTUPC (4,236 LOC, 167 IF statements, 20 EVALUATE blocks) contains deeply embedded business rules for account updates. During migration, subtle validation conditions may be missed or incorrectly translated, leading to accounts being updated in ways the original system would have rejected. |
| **Probability** | 4 (High) |
| **Impact** | 5 (Very High) |
| **Risk Score** | **20 — Critical** |
| **Phase Affected** | Phase 3 (Write Operations) |
| **Evidence** | Hotspot score 4.70 (highest). 167 IF statements = 167 decision paths. Many are nested 3-4 levels deep with compound conditions. |

**Mitigation Strategy:**
1. **Golden-master testing:** Capture every COACTUPC execution path by running the COBOL program with 500+ test scenarios and recording input/output pairs
2. **Branch-by-branch translation:** Map each IF/EVALUATE to a named Java method with unit test. Track coverage: 167/167 branches must be tested
3. **Code review by COBOL SME:** Every translated method reviewed by someone who understands the original COBOL intent
4. **Parallel run with field-level comparison:** During dual-write period, compare every account update field-by-field between COBOL and Java output

**Owner:** Tech Lead + COBOL SME
**Success Metric:** 100% branch coverage in Java translation; zero field-level discrepancies in parallel run over 2 billing cycles
**Escalation:** If > 5% of test scenarios fail after initial translation, escalate to project sponsor for timeline extension

---

### R02 — Interest Calculation Precision Errors

| Attribute | Detail |
|-----------|--------|
| **Category** | Financial / Regulatory |
| **Description** | CBACT04C (interest calculation) uses COBOL COMP-3 (packed decimal) arithmetic with implicit rounding rules. Java `BigDecimal` uses explicit `RoundingMode`. Any mismatch in rounding behavior produces penny-level discrepancies that accumulate across thousands of accounts and may violate regulatory requirements. |
| **Probability** | 4 (High) |
| **Impact** | 5 (Very High) |
| **Risk Score** | **20 — Critical** |
| **Phase Affected** | Phase 4 (Batch Processing) |
| **Evidence** | CBACT04C score 4.00 (#5 hotspot). Multi-table rate lookup (DISCGRP + TCATBALF) with 56 PERFORMs. COBOL implicit rounding differs from Java defaults. |

**Mitigation Strategy:**
1. **COBOL arithmetic analysis:** Document every COMPUTE, ADD, SUBTRACT, MULTIPLY, DIVIDE statement and its implicit rounding behavior (ROUNDED keyword, COMP-3 truncation)
2. **Explicit RoundingMode mapping:** Create a `COBOLArithmetic` utility class that replicates COBOL rounding for each operation type
3. **Penny-for-penny reconciliation:** Run both COBOL and Java interest calculations for every account for 3+ billing cycles. Zero tolerance for discrepancies.
4. **Regulatory documentation:** Prepare methodology document showing Java calculation is mathematically equivalent to COBOL

**Owner:** Tech Lead + DBA + Compliance Officer
**Success Metric:** Zero penny-level discrepancies across all accounts for 3 consecutive billing cycles
**Escalation:** Any single penny discrepancy triggers root-cause analysis and halts Java batch cutover

---

### R03 — Bill Payment Dual-Write Data Inconsistency

| Attribute | Detail |
|-----------|--------|
| **Category** | Technical / Data |
| **Description** | COBIL00C performs an atomic dual-write: creates a payment transaction in TRANSACT AND updates the account balance in ACCTDATA. In CICS, both are within a single Logical Unit of Work (LUW). In Java, this spans two domain operations. If the transaction creation succeeds but the balance update fails, the system enters an inconsistent state. |
| **Probability** | 3 (Medium) |
| **Impact** | 5 (Very High) |
| **Risk Score** | **15 — High** |
| **Phase Affected** | Phase 3 (Write Operations) |
| **Evidence** | COBIL00C score 3.85 (#7 hotspot). 18 EVALUATE blocks. Dual-write to TRANSACT + ACCTDATA. |

**Mitigation Strategy:**
1. **Modular monolith first:** Keep billing and transaction in the same deployment unit sharing a PostgreSQL database. Use `@Transactional` to maintain atomicity.
2. **Optimistic locking:** Add `@Version` column to accounts table. Concurrent updates detected and retried.
3. **Idempotency key:** Every payment request includes a unique key. Duplicate submissions produce the same result (no double-charge).
4. **Reconciliation job:** Nightly job compares transaction sum against account balance changes. Any mismatch triggers alert.

**Owner:** Senior Java Developer + DBA
**Success Metric:** Zero balance discrepancies during parallel run; idempotency test passes with 100% duplicate rejection
**Escalation:** Any balance discrepancy in production triggers immediate rollback to COBOL bill payment

---

### R04 — VSAM-to-PostgreSQL Data Migration Fidelity

| Attribute | Detail |
|-----------|--------|
| **Category** | Data |
| **Description** | 11 VSAM datasets totaling ~600K+ records must be migrated from EBCDIC/packed-decimal/zoned-decimal to UTF-8/BigDecimal/ISO dates. Character encoding issues, packed decimal conversion errors, and date format mismatches can corrupt data silently. |
| **Probability** | 4 (High) |
| **Impact** | 4 (High) |
| **Risk Score** | **16 — Critical** |
| **Phase Affected** | Phase 2–4 (all data migration phases) |
| **Evidence** | DATA_DICTIONARY shows 11 VSAM files with COMP-3, PIC 9, and packed date fields. ACCTDATA has 300-byte records with 20+ fields requiring type conversion. |

**Mitigation Strategy:**
1. **Field-level conversion tests:** For each copybook PIC clause, create a unit test: COBOL value → conversion → PostgreSQL value → verify
2. **Round-trip validation:** Export VSAM → PostgreSQL → export back to flat file → compare with original VSAM export byte-for-byte
3. **Incremental migration:** Migrate one VSAM file at a time with full validation before proceeding
4. **CDC validation:** Continuous record count + checksum comparison between VSAM and PostgreSQL during parallel run

**Owner:** DBA + Data Engineer
**Success Metric:** Zero conversion errors across all 11 datasets; CDC lag < 30 seconds
**Escalation:** > 10 conversion errors per dataset triggers conversion logic review and re-migration

---

### R05 — COBOL SME Availability

| Attribute | Detail |
|-----------|--------|
| **Category** | Organizational |
| **Description** | The migration requires deep COBOL expertise to interpret business rules embedded in 30+ programs. COBOL developers are aging out of the workforce. If key SMEs leave, become unavailable, or their knowledge isn't captured, the migration loses its primary source of truth. |
| **Probability** | 3 (Medium) |
| **Impact** | 5 (Very High) |
| **Risk Score** | **15 — High** |
| **Phase Affected** | All phases, especially Phase 3–4 |

**Mitigation Strategy:**
1. **Knowledge capture sprints:** Before each phase, dedicate 1 week to recorded video walkthroughs of every COBOL program with the SME explaining the business logic
2. **Pair programming:** COBOL SME + Java developer pair on every translation to transfer knowledge in real-time
3. **Documentation-first:** Every business rule must be documented in a shared wiki BEFORE translation begins
4. **Backup SME:** Identify at least 2 people with COBOL knowledge; never rely on a single individual
5. **Contract retention bonus:** If SME is a contractor, negotiate retention through Phase 4 completion

**Owner:** Project Manager + HR
**Success Metric:** 100% of programs have recorded walkthroughs; 2+ people can explain each program's business logic
**Escalation:** If COBOL SME gives notice, immediately accelerate knowledge capture for remaining programs

---

### R06 — Batch Cycle Timing Window

| Attribute | Detail |
|-----------|--------|
| **Category** | Technical / Schedule |
| **Description** | The nightly COBOL batch cycle (POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX) runs in a fixed window between CLOSEFIL and OPENFIL. During parallel run, both COBOL and Java batch must complete within this window. If Java batch is slower, it delays OPENFIL and impacts next-day online operations. |
| **Probability** | 3 (Medium) |
| **Impact** | 3 (Medium) |
| **Risk Score** | **9 — Medium** |
| **Phase Affected** | Phase 4 (Batch Processing) |
| **Evidence** | DEPENDENCY_MAP shows 9-step nightly cycle. CLOSEFIL/OPENFIL bracket the window. |

**Mitigation Strategy:**
1. **Performance benchmarking:** Run Java batch against production-volume data in staging and measure wall-clock time
2. **Parallel execution:** Run Java batch concurrently with COBOL batch (on PostgreSQL copy, not VSAM) to avoid extending the window
3. **Chunk size tuning:** Optimize Spring Batch chunk sizes for throughput
4. **Fallback:** If Java batch consistently exceeds the window, extend the window or skip Java batch on high-volume nights

**Owner:** Tech Lead + Operations
**Success Metric:** Java batch completes within 120% of COBOL batch time for equivalent data volume
**Escalation:** If Java batch exceeds 150% of COBOL time after optimization, re-evaluate batch architecture

---

### R07 — 3270 Bridge Reliability During Parallel Run

| Attribute | Detail |
|-----------|--------|
| **Category** | Technical |
| **Description** | During Phases 1–4, unmigrated modules are accessed via a 3270 terminal bridge from the web UI. If the bridge drops connections, times out, or misrenders BMS maps, users lose access to unmigrated functionality. |
| **Probability** | 3 (Medium) |
| **Impact** | 3 (Medium) |
| **Risk Score** | **9 — Medium** |
| **Phase Affected** | Phases 1–4 (entire parallel run period) |

**Mitigation Strategy:**
1. **Commercial bridge product:** Use a proven product (IBM Host Access Client, Rocket BlueZone) rather than building custom
2. **Connection pooling:** Maintain warm connections to CICS to avoid cold-start latency
3. **Health checks:** Monitor bridge connectivity every 30 seconds; auto-restart on failure
4. **Fallback:** Provide direct TN3270 client access as backup if web bridge fails

**Owner:** Infrastructure Team
**Success Metric:** Bridge uptime ≥ 99.5%; average connection latency < 500ms
**Escalation:** Bridge downtime > 30 minutes triggers direct 3270 client distribution to all users

---

### R08 — Transaction ID / Key Collision During Parallel Run

| Attribute | Detail |
|-----------|--------|
| **Category** | Data |
| **Description** | During dual-write periods, both COBOL and Java generate transaction IDs. If both systems use sequential numbering, IDs will collide. Additionally, VSAM key generation (which may depend on record count or last-key-used logic) may conflict with PostgreSQL sequence generation. |
| **Probability** | 4 (High) |
| **Impact** | 3 (Medium) |
| **Risk Score** | **12 — High** |
| **Phase Affected** | Phase 3 (Write Operations) |

**Mitigation Strategy:**
1. **Key space partitioning:** COBOL uses even transaction IDs, Java uses odd (or use a prefix: "L-" for legacy, "J-" for Java)
2. **UUID adoption:** Java services use UUIDs for new transaction IDs; maintain a mapping table for cross-reference with legacy numeric IDs
3. **Centralized sequence service:** A single sequence generator (PostgreSQL sequence) serves both systems via API
4. **Post-migration cleanup:** After cutover, renumber or normalize IDs if needed

**Owner:** DBA + Tech Lead
**Success Metric:** Zero key collisions during parallel run; all cross-references resolvable
**Escalation:** First key collision triggers immediate review of key generation strategy

---

### R09 — CDC Pipeline Lag or Failure

| Attribute | Detail |
|-----------|--------|
| **Category** | Technical / Data |
| **Description** | The Change Data Capture pipeline synchronizing VSAM changes to PostgreSQL (and reverse during Phase 3+) is a critical dependency. If CDC falls behind or fails silently, read-only screens show stale data, and dual-write reconciliation produces false positives. |
| **Probability** | 3 (Medium) |
| **Impact** | 4 (High) |
| **Risk Score** | **12 — High** |
| **Phase Affected** | Phases 2–4 |

**Mitigation Strategy:**
1. **Lag monitoring:** Track CDC lag metric in Grafana with alert at > 60 seconds
2. **Heartbeat records:** Insert a timestamp record into VSAM every minute; verify it appears in PostgreSQL within 30 seconds
3. **Checkpoint and recovery:** CDC pipeline checkpoints progress; on failure, resumes from last checkpoint
4. **Full resync capability:** Ability to trigger a full VSAM → PostgreSQL resync if CDC loses its place
5. **Read-from-source fallback:** If CDC is down, read-only screens can temporarily query VSAM via the 3270 bridge

**Owner:** Data Engineer + DBA
**Success Metric:** CDC lag < 30 seconds (P99); zero silent failures (all failures trigger alerts)
**Escalation:** CDC outage > 5 minutes triggers read-traffic fallback to CICS screens

---

### R10 — Scope Creep from Optional Modules

| Attribute | Detail |
|-----------|--------|
| **Category** | Schedule |
| **Description** | The 13 optional module programs (IMS/DB2/MQ — 9,526 LOC) may be pulled into the core migration scope if stakeholders discover dependencies or business requirements that demand their inclusion. This could extend the timeline by 4–6 months. |
| **Probability** | 3 (Medium) |
| **Impact** | 3 (Medium) |
| **Risk Score** | **9 — Medium** |
| **Phase Affected** | All phases, especially Phase 4–5 |

**Mitigation Strategy:**
1. **Strict scope boundary:** Optional modules documented as out-of-scope in project charter with explicit change control process
2. **Dependency analysis:** Verify core modules have no runtime dependency on optional modules (currently they don't — optional modules use separate CICS transactions)
3. **Stub services:** If a dependency is discovered, create a stub that delegates to the legacy system rather than migrating the optional module
4. **Separate project:** Optional modules scoped as a follow-on project with its own budget and timeline

**Owner:** Project Manager + Product Owner
**Success Metric:** Zero unplanned scope additions to core migration
**Escalation:** Any scope addition request goes through formal change control with impact assessment

---

### R11 — Performance Regression in Online Transactions

| Attribute | Detail |
|-----------|--------|
| **Category** | Technical |
| **Description** | CICS transactions are highly optimized for 3270 terminal interactions (sub-100ms response times). The new Java/Spring system adds HTTP overhead, JPA query generation, connection pooling, and web rendering. Response times may exceed user expectations, especially for high-volume screens like card list (COCRDLIC) and transaction list (COTRN00C). |
| **Probability** | 3 (Medium) |
| **Impact** | 2 (Low) |
| **Risk Score** | **6 — Medium** |
| **Phase Affected** | Phases 2–3 |

**Mitigation Strategy:**
1. **Performance baseline:** Record CICS response times for all screens before migration begins
2. **SLA definition:** Define acceptable response times per screen (e.g., list: 500ms, detail: 200ms, write: 300ms)
3. **Database indexing:** Ensure PostgreSQL indexes match VSAM KSDS key structures
4. **Connection pooling:** HikariCP with tuned pool sizes based on concurrent user count
5. **Caching:** Redis/Caffeine cache for frequently accessed, slowly changing data (card cross-references, lookup codes)
6. **Load testing:** JMeter/Gatling test with production-equivalent traffic patterns before each phase cutover

**Owner:** Tech Lead + Performance Engineer
**Success Metric:** All screens meet defined SLAs under load test
**Escalation:** If any screen exceeds SLA by > 200% after optimization, consider query rewrite or caching strategy review

---

### R12 — Security Vulnerability Introduction

| Attribute | Detail |
|-----------|--------|
| **Category** | Technical / Security |
| **Description** | The legacy system's security weaknesses (plaintext passwords, no encryption) are being replaced. However, the new system introduces new attack surfaces: REST APIs, JWT tokens, web UI (XSS/CSRF), SQL injection via JPA misuse, and network exposure beyond the mainframe's isolated environment. |
| **Probability** | 3 (Medium) |
| **Impact** | 4 (High) |
| **Risk Score** | **12 — High** |
| **Phase Affected** | Phase 1 (Security rewrite) and ongoing |

**Mitigation Strategy:**
1. **OWASP Top 10 review:** Security checklist review for every REST endpoint before go-live
2. **Dependency scanning:** Dependabot / Snyk on all Java dependencies
3. **Penetration testing:** External pen test before Phase 2 (first user-facing screens) and before Phase 3 (first write operations)
4. **Spring Security hardening:** CSRF protection, CORS configuration, rate limiting, input validation on all endpoints
5. **PCI DSS compliance:** Card numbers encrypted at rest and masked in display (last 4 only)
6. **Audit logging:** All authentication events, admin actions, and data modifications logged with timestamps

**Owner:** Security Engineer + Tech Lead
**Success Metric:** Zero critical/high vulnerabilities in pen test; PCI DSS self-assessment passed
**Escalation:** Any critical vulnerability discovered in pen test blocks phase cutover until remediated

---

### R13 — Organizational Resistance to Change

| Attribute | Detail |
|-----------|--------|
| **Category** | Organizational |
| **Description** | Users accustomed to 3270 terminal screens may resist the new web UI. Operations staff familiar with JCL scheduling may resist Spring Batch. Resistance can manifest as low adoption, workaround usage (accessing legacy directly), or inflated defect reports. |
| **Probability** | 3 (Medium) |
| **Impact** | 2 (Low) |
| **Risk Score** | **6 — Medium** |
| **Phase Affected** | Phases 2–5 |

**Mitigation Strategy:**
1. **Early involvement:** Include power users in UAT for each phase; incorporate their feedback
2. **Training plan:** Hands-on training sessions before each phase cutover
3. **Champion network:** Identify 2–3 power users per department to be early adopters and peer trainers
4. **Feedback loop:** Weekly feedback surveys during first month of each phase; address top issues within 1 week
5. **Parallel access:** During parallel run, users can always fall back to legacy if they prefer — track which system they choose to measure adoption

**Owner:** Project Manager + Change Management Lead
**Success Metric:** ≥ 80% user preference for new system in satisfaction surveys
**Escalation:** < 50% adoption after 4 weeks triggers focused training and UI improvement sprint

---

### R14 — Incomplete Test Coverage of Edge Cases

| Attribute | Detail |
|-----------|--------|
| **Category** | Technical |
| **Description** | The COBOL programs handle edge cases that may not be documented or obvious: negative balances, zero-length strings, date boundary conditions (leap years, century boundaries), maximum field lengths, and VSAM record-not-found conditions. These edge cases may not appear in golden-master test data but can cause production failures. |
| **Probability** | 4 (High) |
| **Impact** | 3 (Medium) |
| **Risk Score** | **12 — High** |
| **Phase Affected** | All phases |

**Mitigation Strategy:**
1. **COBOL branch analysis:** Use COBOL code coverage tools to identify untested branches in legacy programs
2. **Boundary value testing:** For every numeric field, test: zero, max, max+1, negative, and boundary values
3. **Date edge cases:** Test Feb 29, Dec 31, Jan 1, year-end/month-end boundaries
4. **Error path testing:** Test every VSAM status code handler, every EVALUATE OTHER clause, every HANDLE ABEND path
5. **Production traffic replay:** Capture 30 days of production transactions and replay against Java system; compare results
6. **Mutation testing:** Use PIT or similar to verify test suite catches introduced bugs

**Owner:** QA Lead + COBOL SME
**Success Metric:** ≥ 95% branch coverage for all migrated programs; production replay produces zero unexpected results
**Escalation:** < 90% branch coverage blocks phase cutover

---

### R15 — Budget Overrun Due to Extended Parallel Run

| Attribute | Detail |
|-----------|--------|
| **Category** | Financial |
| **Description** | During parallel run, the organization pays for both mainframe (CICS/VSAM) and cloud (Java/PostgreSQL) infrastructure. If any phase's parallel run extends beyond planned duration due to reconciliation issues, the dual-infrastructure cost accumulates. Mainframe costs are typically the largest IT line item. |
| **Probability** | 3 (Medium) |
| **Impact** | 3 (Medium) |
| **Risk Score** | **9 — Medium** |
| **Phase Affected** | Phases 2–5 |

**Mitigation Strategy:**
1. **Fixed parallel run windows:** Each phase has a maximum parallel run duration (defined in CUTOVER_PLAN). If issues aren't resolved by deadline, either fix forward or rollback — don't extend indefinitely.
2. **Mainframe cost tracking:** Monthly mainframe MIPS/MSU reporting to track whether migration phases actually reduce consumption
3. **Early CICS region consolidation:** As modules are migrated, reduce CICS region resources to cut costs even before full decommission
4. **Cloud cost optimization:** Right-size cloud infrastructure; use reserved instances once usage patterns are known
5. **Contingency budget:** 20% buffer built into each phase budget for parallel run extension

**Owner:** Project Sponsor + Finance
**Success Metric:** Total migration cost within 120% of initial estimate; mainframe costs decrease measurably after Phase 3
**Escalation:** Cumulative overrun > 15% triggers executive review of remaining phase budgets

---

## 3. Risk Heat Map

```
Impact
  5 │  R05        R01,R02
    │             R03
  4 │  R09        R04
    │  R12
  3 │  R10,R06    R08,R14
    │  R15
  2 │  R13,R11
    │
  1 │
    └──────────────────────
      1    2    3    4    5
              Probability
```

### Top 5 Risks by Score

| Rank | ID | Risk | Score | Level |
|------|----|------|-------|-------|
| 1 | R01 | COACTUPC business rule loss | 20 | Critical |
| 2 | R02 | Interest calculation precision | 20 | Critical |
| 3 | R04 | VSAM data migration fidelity | 16 | Critical |
| 4 | R03 | Bill payment dual-write inconsistency | 15 | High |
| 5 | R05 | COBOL SME availability | 15 | High |

---

## 4. Risk Monitoring Schedule

| Frequency | Action | Responsible |
|-----------|--------|-------------|
| Daily | Check CDC lag, reconciliation results, bridge health | Data Engineer, Operations |
| Weekly | Review open risk items, update probability/impact scores | Tech Lead, Project Manager |
| Per-phase | Full risk register review before each phase cutover gate | Steering Committee |
| Monthly | Financial tracking: mainframe costs, cloud costs, budget burn rate | Finance, Project Sponsor |
| Quarterly | External security assessment | Security Engineer |

---

## 5. Escalation Matrix

| Risk Level | Response Time | Escalation To | Action |
|------------|--------------|---------------|--------|
| Low (1–4) | Next weekly review | Tech Lead | Monitor; no immediate action |
| Medium (5–9) | Within 48 hours | Project Manager | Assign mitigation owner; track in sprint |
| High (10–15) | Within 24 hours | Steering Committee | Active management; weekly status updates |
| Critical (16–25) | Immediate | Project Sponsor / CTO | Emergency meeting; potential phase halt |
