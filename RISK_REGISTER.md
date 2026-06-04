# Risk Register

## Overview

This document identifies the top risks associated with modernizing the CardDemo mainframe credit card management system, along with their likelihood, impact, and mitigation strategies.

---

## Risk Scoring

| Level | Likelihood | Impact |
|-------|-----------|--------|
| **Critical** | Very likely to occur | System outage, financial loss, data corruption |
| **High** | Likely | Service degradation, delayed timeline, significant rework |
| **Medium** | Possible | Manageable delay, workaround available |
| **Low** | Unlikely | Minor inconvenience, easily resolved |

**Risk Score = Likelihood × Impact** (Critical=5, High=4, Medium=3, Low=2, Negligible=1)

---

## Risk Register

### R-01: Financial Calculation Divergence

| Attribute | Value |
|-----------|-------|
| **Category** | Data Integrity |
| **Likelihood** | High (4) |
| **Impact** | Critical (5) |
| **Risk Score** | **20 — Critical** |
| **Phase Affected** | Phase 3 (Transaction Processing, Interest Calculation) |

**Description:**
COBOL COMP-3 (packed decimal) arithmetic and Java `BigDecimal` may produce different rounding results for interest calculations, balance updates, and category accumulations. The COBOL programs use `PIC S9(10)V99` (12-digit precision with 2 decimal places) which has implicit rounding behavior that may not match Java defaults.

**Mitigations:**
1. **Parallel-run validation** — run legacy and new calculations side-by-side for minimum 3 billing cycles (90 days) before cutover.
2. **Golden dataset** — create a reference dataset of 10,000+ account calculations with known correct outputs; automate regression.
3. **Rounding policy codification** — document and unit-test every rounding decision (ROUNDED, TRUNCATION, intermediate precision).
4. **Zero-tolerance gate** — no cutover until parallel run shows $0.00 variance across all accounts.

**Contingency:** If divergence cannot be resolved, fall back to replatform (Micro Focus/Blu Age) for financial calculations only, preserving COBOL arithmetic semantics.

---

### R-02: CCXREF Data Integrity During Dual-Write

| Attribute | Value |
|-----------|-------|
| **Category** | Data Integrity |
| **Likelihood** | High (4) |
| **Impact** | High (4) |
| **Risk Score** | **16 — High** |
| **Phase Affected** | Phase 2 (Card Management) |

**Description:**
The Card-to-Account Cross-Reference (CCXREF) is the central join entity linking Customers, Accounts, and Cards. During dual-write transition, a write failure on either the VSAM or relational side creates an orphaned reference that cascades errors to Transaction Processing (cannot resolve card → account) and Billing (cannot apply payment to correct account).

**Mitigations:**
1. **Transactional dual-write** — use a distributed transaction or outbox pattern to ensure atomicity.
2. **Hourly reconciliation job** — compare VSAM CCXREF with relational table; alert on any orphan.
3. **XREF as last-to-migrate** — keep XREF on VSAM until all consumers (Card, Txn, Billing) are migrated, then do a single atomic cutover.
4. **Immutable XREF records** — during transition, new XREFs written only to DB; legacy reads from VSAM remain stable.

**Contingency:** If orphans are detected, halt new writes and replay from the last consistent snapshot.

---

### R-03: CICS COMMAREA Contract Breakage

| Attribute | Value |
|-----------|-------|
| **Category** | Integration |
| **Likelihood** | Medium (3) |
| **Impact** | High (4) |
| **Risk Score** | **12 — High** |
| **Phase Affected** | Phase 1–3 (all online programs) |

**Description:**
All CICS programs share a fixed 120-byte COMMAREA (`COCOM01Y.cpy`) containing cross-context data (user, customer, account, card info). As contexts are extracted to independent services, the COMMAREA contract becomes stale. If a legacy program expects data that a new service no longer populates, the program crashes or produces incorrect results.

**Mitigations:**
1. **ACL owns COMMAREA translation** — the Anti-Corruption Layer always populates the full COMMAREA before routing to any legacy program.
2. **Contract versioning** — maintain a COMMAREA v1 (legacy) and map to/from service DTOs at the ACL boundary.
3. **Integration test matrix** — test every CICS program with COMMAREA populated by the new service stack.
4. **Deprecation tracking** — mark each COMMAREA field as "still needed by program X" and track until all consumers are migrated.

**Contingency:** If a program fails due to COMMAREA issues, route that specific transaction back to the legacy path via ACL configuration change (< 5 min).

---

### R-04: Batch Window Elimination Causes Data Corruption

| Attribute | Value |
|-----------|-------|
| **Category** | Concurrency |
| **Likelihood** | Medium (3) |
| **Impact** | Critical (5) |
| **Risk Score** | **15 — High** |
| **Phase Affected** | Phase 3 (Batch Operations) |

**Description:**
Legacy batch operations require exclusive file access (CLOSEFIL/OPENFIL cycle ensures no online contention). When batch logic moves to services accessing a shared database, concurrent online transactions during what was previously the "batch window" can cause dirty reads, lost updates, or phantom balances during interest calculation or transaction consolidation.

**Mitigations:**
1. **Database-level isolation** — use SERIALIZABLE isolation for batch operations that require point-in-time consistency.
2. **Snapshot reads** — interest calculation reads from a database snapshot (MVCC), not live data.
3. **Batch reservation** — implement a "batch lock" flag that queues online writes to affected accounts during batch processing (mimics CLOSEFIL semantics without mainframe).
4. **Gradual elimination** — keep the batch window for the first 3 months post-migration; only eliminate it after concurrency testing validates correctness.

**Contingency:** Re-introduce a maintenance window (short, e.g., 5 min) during batch if concurrent corruption is detected.

---

### R-05: Knowledge Loss of Undocumented Business Rules

| Attribute | Value |
|-----------|-------|
| **Category** | Knowledge |
| **Likelihood** | High (4) |
| **Impact** | High (4) |
| **Risk Score** | **16 — High** |
| **Phase Affected** | Phase 2–3 (Account Update, Transaction Processing) |

**Description:**
`COACTUPC.cbl` (4,236 LOC) contains complex validation logic including 88-level condition checks, signed-number edits, and state machine transitions that are not documented anywhere. Original developers are likely unavailable. Rewriting without understanding these rules risks silent behavioral differences that surface as production bugs months later.

**Mitigations:**
1. **Automated rule extraction** — use AI-assisted code analysis to catalog every conditional branch and validation rule in the top-5 largest programs.
2. **Test generation from production data** — replay 6 months of production inputs through legacy; capture all outputs as golden test cases.
3. **SME interviews** — schedule knowledge transfer sessions with any available mainframe staff before migration begins.
4. **Incremental validation** — migrate one validation rule at a time; verify each against the golden test suite before proceeding.

**Contingency:** For rules that cannot be decoded, keep the COBOL program running via rehosting (UniKix/Micro Focus) and expose it as a validation oracle until the rule is understood.

---

### R-06: VSAM-to-Relational Data Migration Fidelity

| Attribute | Value |
|-----------|-------|
| **Category** | Data Migration |
| **Likelihood** | Medium (3) |
| **Impact** | High (4) |
| **Risk Score** | **12 — High** |
| **Phase Affected** | Phase 0–1 (Foundation + all contexts) |

**Description:**
VSAM KSDS files use EBCDIC encoding, packed decimal (COMP-3) numeric storage, and fixed-length records with FILLER bytes. Migration to relational tables requires exact conversion of every data type. Risks include: sign bit misinterpretation in packed decimals, EBCDIC special characters lost in translation, FILLER bytes containing meaningful legacy data.

**Mitigations:**
1. **Round-trip testing** — export VSAM → relational → re-export in VSAM format → binary diff against original.
2. **FILLER analysis** — scan all FILLER fields across production data for non-space/non-zero content before declaring them unused.
3. **COMP-3 test suite** — create boundary-value tests for every packed decimal field (max positive, max negative, zero, near-overflow).
4. **Character set mapping** — define explicit EBCDIC → UTF-8 mapping table; test with production data containing special characters.

**Contingency:** If data loss is detected, revert to VSAM as primary and re-run extraction with corrected mapping.

---

### R-07: Performance Degradation Under Load

| Attribute | Value |
|-----------|-------|
| **Category** | Performance |
| **Likelihood** | Medium (3) |
| **Impact** | Medium (3) |
| **Risk Score** | **9 — Medium** |
| **Phase Affected** | Phase 2–3 |

**Description:**
CICS transactions complete in milliseconds due to in-memory VSAM access and efficient COBOL execution. Java services with database calls, network hops, and serialization overhead may introduce latency. Transaction listing (`COTRN00C`) pages through VSAM with sub-millisecond sequential reads; equivalent SQL pagination may be significantly slower under high concurrency.

**Mitigations:**
1. **Performance benchmarks** — establish baseline latency for each CICS transaction; new services must meet or beat.
2. **Caching layer** — Redis/Memcached for frequently-accessed account and card data.
3. **Connection pooling** — HikariCP with optimized pool sizes per service.
4. **Database indexing** — mirror VSAM KSDS key structure and AIX patterns as database indexes.
5. **Load testing** — simulate peak transaction volumes before each phase cutover.

**Contingency:** If a service cannot meet latency SLA, implement read-through cache or consider in-memory data grid (Hazelcast/Redis) for hot-path data.

---

### R-08: Mainframe Skills Shortage for Parallel Run

| Attribute | Value |
|-----------|-------|
| **Category** | People |
| **Likelihood** | High (4) |
| **Impact** | Medium (3) |
| **Risk Score** | **12 — High** |
| **Phase Affected** | All phases |

**Description:**
Parallel-run validation requires ongoing mainframe operation plus staff who understand COBOL, CICS, VSAM, JCL, and Control-M. Mainframe expertise is scarce and expensive. If key staff leave during the 9–11 month migration, the ability to debug legacy discrepancies is lost.

**Mitigations:**
1. **Document everything upfront** — Phase 0 produces comprehensive documentation of all programs, data flows, and business rules.
2. **Automated regression** — minimize human intervention in parallel-run comparison; automated alerts reduce dependency on SMEs.
3. **Staff retention plan** — budget for mainframe consultant availability through Phase 4 completion.
4. **AI-assisted analysis** — use LLM-based COBOL comprehension tools to reduce dependency on human COBOL expertise.
5. **Cross-training** — Java developers pair with mainframe staff in Phase 0–1.

**Contingency:** Contract with a mainframe services firm (e.g., Infosys, TCS, DXC) for on-demand COBOL expertise.

---

### R-09: Regulatory/Audit Compliance During Transition

| Attribute | Value |
|-----------|-------|
| **Category** | Compliance |
| **Likelihood** | Medium (3) |
| **Impact** | High (4) |
| **Risk Score** | **12 — High** |
| **Phase Affected** | Phase 2–4 |

**Description:**
Credit card management systems are subject to PCI-DSS, SOX, and potentially banking regulations. During transition, card numbers (PIC 9(16)) flow through both legacy and new systems. Audit trails must prove data integrity end-to-end. Running two systems simultaneously doubles the attack surface and complicates compliance certification.

**Mitigations:**
1. **PCI-DSS scope reduction** — tokenize card numbers at the ACL boundary; new services never see raw PANs.
2. **Unified audit log** — centralized audit trail capturing all read/write operations across both legacy and new systems.
3. **Compliance review at each phase gate** — security team signs off before each cutover.
4. **Encryption in transit and at rest** — new platform encrypts all financial data (AES-256, TLS 1.3).
5. **Penetration testing** — at Phase 2 and Phase 3 completion.

**Contingency:** If compliance cannot be achieved for both systems simultaneously, migrate PCI-scoped data first (Phase 2 Card Management) and decommission legacy card access before audit period.

---

### R-10: Scope Creep from Optional Modules

| Attribute | Value |
|-----------|-------|
| **Category** | Project Management |
| **Likelihood** | Medium (3) |
| **Impact** | Medium (3) |
| **Risk Score** | **9 — Medium** |
| **Phase Affected** | Phase 3–4 |

**Description:**
The optional modules (IMS/DB2/MQ authorization, VSAM-MQ account extraction, DB2 transaction type management) add significant complexity (IMS hierarchical DB, DB2 relational, MQ messaging). If these are brought into scope without explicit planning, they inflate timeline and complexity disproportionately.

**Mitigations:**
1. **Explicit scope boundary** — optional modules are Phase 4 or post-migration; not in critical path.
2. **Feature flags** — if authorization module is needed, implement with feature toggle for gradual rollout.
3. **Technology containment** — IMS → relational migration is a separate workstream with its own validation.
4. **Decision point at Phase 3 exit** — evaluate whether optional modules justify migration or should be retired.

**Contingency:** Keep optional modules running on mainframe indefinitely if business value doesn't justify migration cost.

---

### R-11: Anti-Corruption Layer Becomes Permanent

| Attribute | Value |
|-----------|-------|
| **Category** | Architecture |
| **Likelihood** | Medium (3) |
| **Impact** | Medium (3) |
| **Risk Score** | **9 — Medium** |
| **Phase Affected** | Phase 2–4 |

**Description:**
The ACL bridging legacy CICS and new services is meant to be temporary. If migration stalls (budget cuts, priority shifts, team changes), the ACL becomes permanent infrastructure — adding latency, maintenance burden, and a single point of failure that's neither fully legacy nor fully modern.

**Mitigations:**
1. **Time-boxed phases** — each phase has a hard deadline; if not complete, escalate rather than extend ACL lifetime.
2. **ACL health metrics** — monitor ACL throughput, latency, and error rate; degradation triggers priority review.
3. **Incremental ACL removal** — as each context is fully migrated, remove its ACL route (don't wait for full decommission).
4. **Architecture review at Phase 2 exit** — if > 50% of traffic still routes through ACL, reassess strategy.

**Contingency:** If ACL must persist, invest in making it production-grade (HA, monitoring, auto-scaling) rather than treating it as throwaway.

---

### R-12: Rollback Data Reconciliation Failure

| Attribute | Value |
|-----------|-------|
| **Category** | Operations |
| **Likelihood** | Low (2) |
| **Impact** | Critical (5) |
| **Risk Score** | **10 — Medium** |
| **Phase Affected** | Phase 2–3 |

**Description:**
If a rollback is triggered after the new service has been processing live transactions, data written to the new database must be reconciled back to VSAM. If the reconciliation process has bugs or the dual-write lag exceeded the checkpoint interval, data loss occurs on rollback.

**Mitigations:**
1. **Continuous sync** — during dual-write, both stores are updated synchronously (not async replication).
2. **Rollback drills** — practice rollback procedures in staging at least twice per phase.
3. **Point-in-time recovery** — maintain database snapshots every 15 minutes during cutover windows.
4. **Transaction log replay** — capture all service inputs in an immutable log; can replay to either target.

**Contingency:** If rollback is needed and reconciliation fails, enter "read-only emergency mode" — freeze all writes, perform manual reconciliation, then resume.

---

## Risk Heat Map

```
Impact →        Low(2)    Medium(3)    High(4)     Critical(5)
Likelihood ↓
─────────────────────────────────────────────────────────────────
Very High(5)     |           |            |             |
─────────────────────────────────────────────────────────────────
High(4)          |           | R-08       | R-02, R-05  | R-01
─────────────────────────────────────────────────────────────────
Medium(3)        |           | R-07,R-10  | R-03,R-06   | R-04
                 |           | R-11       | R-09        |
─────────────────────────────────────────────────────────────────
Low(2)           |           |            |             | R-12
─────────────────────────────────────────────────────────────────
```

---

## Top 5 Risks by Score

| Rank | ID | Risk | Score | Primary Mitigation |
|------|----|------|-------|--------------------|
| 1 | R-01 | Financial Calculation Divergence | 20 | 90-day parallel run with zero-tolerance gate |
| 2 | R-02 | CCXREF Data Integrity | 16 | Transactional dual-write + hourly reconciliation |
| 3 | R-05 | Undocumented Business Rules | 16 | AI-assisted extraction + golden test suite |
| 4 | R-04 | Batch Window Elimination | 15 | SERIALIZABLE isolation + gradual elimination |
| 5 | R-03 | COMMAREA Contract Breakage | 12 | ACL owns translation + integration test matrix |

---

## Risk Monitoring Cadence

| Frequency | Action |
|-----------|--------|
| Daily | Automated reconciliation reports reviewed |
| Weekly | Risk register reviewed in migration standup |
| Per-phase gate | Full risk reassessment; new risks added |
| Monthly | Executive risk dashboard updated |
| On-incident | Post-mortem → new risk or mitigation update |
