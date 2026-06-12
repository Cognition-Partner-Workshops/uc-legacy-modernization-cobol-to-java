# Risk Register — CardDemo Modernization

## Overview

This register identifies the top risks associated with modernizing the CardDemo mainframe system, organized by impact severity. Each risk includes likelihood, impact assessment, affected phases, and concrete mitigation strategies.

---

## Risk Severity Matrix

| | Low Impact | Medium Impact | High Impact | Critical Impact |
|---|---|---|---|---|
| **High Likelihood** | Monitor | Mitigate | Mitigate Urgently | Prevent |
| **Medium Likelihood** | Accept | Monitor | Mitigate | Mitigate Urgently |
| **Low Likelihood** | Accept | Accept | Monitor | Mitigate |

---

## Top Risks

### R-01: Financial Calculation Discrepancy

| Attribute | Value |
|-----------|-------|
| **Category** | Data Integrity |
| **Likelihood** | High |
| **Impact** | Critical |
| **Affected Phases** | Phase 7 (Posting & Interest) |
| **Risk Owner** | Technical Architect |

**Description:**  
COBOL packed-decimal arithmetic (`COMP-3`, `PIC S9(10)V99`) behaves differently from Java floating-point. The posting engine (`CBTRN02C`) and interest calculator (`CBACT04C`) perform running balance calculations across thousands of transactions. Even sub-cent rounding differences compound over billing cycles, potentially causing regulatory non-compliance (incorrect statements, wrong interest charges).

**Mitigations:**
| # | Mitigation | Effectiveness |
|---|-----------|:---:|
| 1 | Use `BigDecimal` with `RoundingMode.HALF_EVEN` (banker's rounding) matching COBOL default | High |
| 2 | Build parity test harness: run legacy + modern on identical input, compare field-by-field | High |
| 3 | Mandate 3 full billing cycles of dual-run with zero-discrepancy exit gate | High |
| 4 | Create synthetic test data covering edge cases: negative balances, overlimit, zero-amount, max-value amounts | Medium |
| 5 | Engage COBOL subject-matter expert to document exact rounding/truncation behavior per program | Medium |

**Residual Risk:** Low (after mitigations 1–3 applied)

---

### R-02: Data Synchronization Failure During Coexistence

| Attribute | Value |
|-----------|-------|
| **Category** | Integration |
| **Likelihood** | High |
| **Impact** | High |
| **Affected Phases** | Phases 2–7 (entire coexistence period) |
| **Risk Owner** | Integration Lead |

**Description:**  
During coexistence, bridge adapters sync data bidirectionally between VSAM files and PostgreSQL. If sync fails or lags under load, legacy batch jobs may process stale data, or modern services may display incorrect balances. The CLOSEFIL/OPENFIL JCL ceremony (which locks files from CICS during batch) adds complexity — sync must respect these windows.

**Mitigations:**
| # | Mitigation | Effectiveness |
|---|-----------|:---:|
| 1 | Implement CDC with guaranteed delivery (Kafka Connect or Debezium-like for VSAM changes) | High |
| 2 | Add reconciliation job: compare record counts and checksums between VSAM and PostgreSQL every hour | High |
| 3 | Alert on sync lag > configurable threshold (e.g., 30 seconds) | Medium |
| 4 | Design bridge adapters as idempotent — safe to replay without duplicates | High |
| 5 | Coordinate bridge with CLOSEFIL/OPENFIL schedule — pause sync during batch window | Medium |

**Residual Risk:** Medium (sync lag during peak load remains possible)

---

### R-03: VSAM File Locking Conflicts

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Likelihood** | Medium |
| **Impact** | High |
| **Affected Phases** | Phases 3–7 |
| **Risk Owner** | DevOps Lead |

**Description:**  
VSAM KSDS files have exclusive access constraints — CICS holds files open for online use, and batch jobs require CLOSEFIL to release them. Modern services writing back to VSAM (via bridge adapters) may conflict with CICS file control or batch locking. This could cause `FILE STATUS 93` (file locked) errors or data corruption.

**Mitigations:**
| # | Mitigation | Effectiveness |
|---|-----------|:---:|
| 1 | Bridge adapters write to VSAM only during defined maintenance windows (aligned with existing CLOSEFIL/OPENFIL) | High |
| 2 | Make PostgreSQL the system of record as early as possible — minimize the coexistence window | High |
| 3 | Implement retry with exponential backoff for bridge writes encountering file locks | Medium |
| 4 | Monitor file status codes from bridge adapter; alert on any non-zero status | Medium |

**Residual Risk:** Low (eliminated once PostgreSQL becomes system of record)

---

### R-04: Loss of Tribal Knowledge

| Attribute | Value |
|-----------|-------|
| **Category** | Organizational |
| **Likelihood** | High |
| **Impact** | High |
| **Affected Phases** | All phases |
| **Risk Owner** | Project Manager |

**Description:**  
The CardDemo system incorporates intentionally varied coding patterns (ALTER/GO TO, mainframe control-block addressing, COMP-3 arithmetic, 2D arrays). Undocumented business rules may be embedded in program logic (e.g., `COACTUPC` at 4,236 LOC has complex conditional paths). If developers who understand the COBOL cannot explain intent, modern implementations may miss edge cases.

**Mitigations:**
| # | Mitigation | Effectiveness |
|---|-----------|:---:|
| 1 | Conduct structured knowledge-extraction workshops with COBOL developers before migration | High |
| 2 | Document every business rule discovered in `COACTUPC`, `CBTRN02C`, `CBACT04C` as executable test cases | High |
| 3 | Use automated COBOL analysis tools (e.g., AWS Mainframe Modernization, Micro Focus COBOL Analyzer) to generate control-flow diagrams | Medium |
| 4 | Maintain COBOL environment until Phase 8 is complete — enables verification at any time | High |
| 5 | Create a "business rules catalog" mapping COBOL paragraph names to business intentions | Medium |

**Residual Risk:** Medium (some undocumented rules may still be missed)

---

### R-05: Performance Regression in Batch Processing

| Attribute | Value |
|-----------|-------|
| **Category** | Performance |
| **Likelihood** | Medium |
| **Impact** | High |
| **Affected Phases** | Phase 7 |
| **Risk Owner** | Performance Engineer |

**Description:**  
COBOL batch jobs on mainframe hardware achieve high throughput via sequential file I/O, VSAM buffering, and in-memory COMP-3 arithmetic. The modernized Spring Batch equivalent using PostgreSQL, JPA, and network I/O may not achieve the same throughput within the batch window. The POSTTRAN job currently processes the entire daily transaction volume in a constrained window between CLOSEFIL and OPENFIL.

**Mitigations:**
| # | Mitigation | Effectiveness |
|---|-----------|:---:|
| 1 | Benchmark early: Port a subset of `CBTRN02C` in Phase 0 and measure throughput on target hardware | High |
| 2 | Design posting job for parallel chunk processing (Spring Batch partitioning) | High |
| 3 | Use batch-optimized database access patterns (JDBC batch inserts, stored procedures for hot paths) | High |
| 4 | Size target infrastructure to exceed mainframe MIPS equivalent for batch workload | Medium |
| 5 | Eliminate file locking overhead — PostgreSQL MVCC allows concurrent read/write without CLOSEFIL | High |

**Residual Risk:** Low (modern hardware with parallel processing typically exceeds mainframe batch throughput)

---

### R-06: IMS Database Migration Complexity

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Likelihood** | Medium |
| **Impact** | Medium |
| **Affected Phases** | Phase 6 (Authorization) |
| **Risk Owner** | Database Architect |

**Description:**  
The Authorization extension stores authorization records in a HIDAM IMS database (hierarchical model). Mapping hierarchical parent-child relationships to relational tables requires careful schema design. The IMS segment hierarchy (root → authorization → detail) and the navigational access patterns (GU, GN, ISRT, DLET) don't map 1:1 to SQL.

**Mitigations:**
| # | Mitigation | Effectiveness |
|---|-----------|:---:|
| 1 | Map IMS hierarchy to a relational model with explicit parent FK relationships | High |
| 2 | Alternatively, use PostgreSQL JSONB for the authorization tree (preserves hierarchical nature) | Medium |
| 3 | Document all DL/I call patterns in `COPAUA0C` and `COPAUS0C`/`1C` | High |
| 4 | Build comprehensive test dataset from IMS DB UNLOAD output (`PAUDBUNL`) | High |
| 5 | Consider DynamoDB if authorization access patterns are primarily key-lookup (fits hierarchical model) | Medium |

**Residual Risk:** Low (hierarchical → relational mapping is well-understood)

---

### R-07: EBCDIC/ASCII Data Conversion Errors

| Attribute | Value |
|-----------|-------|
| **Category** | Data Integrity |
| **Likelihood** | Medium |
| **Impact** | Medium |
| **Affected Phases** | Phases 2–3 (data migration) |
| **Risk Owner** | Data Migration Lead |

**Description:**  
CardDemo data files are stored in EBCDIC encoding (`app/data/EBCDIC/`). Packed decimal (COMP-3) fields, signed zoned decimals, and binary (COMP) fields require byte-level conversion. Incorrect conversion produces corrupted amounts, invalid dates, or garbled text — particularly for special characters, negative values (sign overpunch), and high-value packed fields.

**Mitigations:**
| # | Mitigation | Effectiveness |
|---|-----------|:---:|
| 1 | Use proven conversion tooling (e.g., AWS M2, Micro Focus File Handler, custom COBOL UNLOAD to CSV) | High |
| 2 | Validate every field post-conversion: known checksums, record counts, boundary values | High |
| 3 | Create test records with edge cases: max PIC S9(10)V99, negative COMP-3, high-bit characters | High |
| 4 | Preserve original EBCDIC files as archive — enables re-extraction if errors found | Medium |
| 5 | Document copybook field types/offsets as the conversion specification | Medium |

**Residual Risk:** Low (with proper tooling and validation)

---

### R-08: Scope Creep — "Modernize Everything" Pressure

| Attribute | Value |
|-----------|-------|
| **Category** | Organizational / Governance |
| **Likelihood** | High |
| **Impact** | Medium |
| **Affected Phases** | All phases |
| **Risk Owner** | Product Owner |

**Description:**  
Stakeholders may push to add new features (mobile app, real-time analytics, ML fraud detection) during migration phases. Adding functionality on top of a modernization effort increases risk, extends timelines, and creates integration complexity with half-migrated systems.

**Mitigations:**
| # | Mitigation | Effectiveness |
|---|-----------|:---:|
| 1 | Enforce "parity first" rule: each phase achieves functional equivalence before new features are considered | High |
| 2 | Maintain a separate "enhancement backlog" — features are scheduled only AFTER their context is fully migrated | High |
| 3 | Phase gate reviews explicitly check for scope additions | Medium |
| 4 | Communicate that modernization enables new features faster — but only after a stable foundation | Medium |

**Residual Risk:** Low (governance process)

---

### R-09: MQ Message Format Backward Compatibility

| Attribute | Value |
|-----------|-------|
| **Category** | Integration |
| **Likelihood** | Medium |
| **Impact** | Medium |
| **Affected Phases** | Phase 6 |
| **Risk Owner** | Integration Architect |

**Description:**  
The Authorization system uses IBM MQ with a specific fixed-format message layout. External POS emulators and clients send authorization requests in this format. If the new Authorization Service changes the message contract, all external integrators must update simultaneously — creating a coordination bottleneck.

**Mitigations:**
| # | Mitigation | Effectiveness |
|---|-----------|:---:|
| 1 | Keep existing MQ message format as the external contract — translate internally | High |
| 2 | New service consumes from same MQ queue (transparent replacement) | High |
| 3 | Add versioning to message format for future evolution (header field) | Medium |
| 4 | Publish new REST/gRPC API alongside MQ — let integrators migrate at their own pace | High |

**Residual Risk:** Low (backward-compatible approach eliminates breaking changes)

---

### R-10: Insufficient Test Coverage for Legacy Behavior

| Attribute | Value |
|-----------|-------|
| **Category** | Quality |
| **Likelihood** | High |
| **Impact** | High |
| **Affected Phases** | All phases |
| **Risk Owner** | QA Lead |

**Description:**  
The legacy system has no automated test suite. Modernized services must replicate behavior exactly, but without tests, "correct" behavior is defined only by the running legacy code. Edge cases (overlimit transactions, expired cards, concurrent updates) may not be exercised during migration validation, leading to production defects.

**Mitigations:**
| # | Mitigation | Effectiveness |
|---|-----------|:---:|
| 1 | Before Phase 1, build a "golden dataset" — run all batch jobs and CICS transactions, capture inputs and outputs as test fixtures | Critical |
| 2 | Implement characterization tests: record legacy system I/O and replay against modern services | High |
| 3 | Use property-based testing for financial calculations (generate random valid inputs, compare legacy vs. modern output) | High |
| 4 | Leverage production traffic replay in staging: mirror real MQ messages and CICS inputs to new services | Medium |
| 5 | Define minimum test coverage gate: 95% line coverage on ported business logic | Medium |

**Residual Risk:** Medium (unknown unknowns will exist until production exposure)

---

### R-11: Team COBOL Skills Gap

| Attribute | Value |
|-----------|-------|
| **Category** | Organizational |
| **Likelihood** | Medium |
| **Impact** | Medium |
| **Affected Phases** | Phases 3–7 |
| **Risk Owner** | Engineering Manager |

**Description:**  
Modern Java developers may lack COBOL literacy, making it difficult to understand legacy business rules, debug parity failures, or interpret COMP-3/VSAM access patterns. Conversely, COBOL developers may struggle with Spring Boot / microservices patterns.

**Mitigations:**
| # | Mitigation | Effectiveness |
|---|-----------|:---:|
| 1 | Pair programming: COBOL expert + Java developer on each migration story | High |
| 2 | Provide COBOL literacy training for Java team (2-day workshop on reading COBOL, copybooks, JCL) | Medium |
| 3 | Use AI-assisted code analysis to annotate COBOL programs with plain-English explanations | Medium |
| 4 | Retain COBOL contractor on retainer through Phase 7 completion | High |

**Residual Risk:** Low (paired expertise addresses the gap)

---

### R-12: Rollback Failure Under Pressure

| Attribute | Value |
|-----------|-------|
| **Category** | Operational |
| **Likelihood** | Low |
| **Impact** | Critical |
| **Affected Phases** | Phases 3–7 (write-path changes) |
| **Risk Owner** | Operations Manager |

**Description:**  
If a modern service fails in production and rollback is needed, the bridge adapters, gateway routing rules, and data sync mechanisms must all revert cleanly. Under incident pressure, manual rollback steps may be skipped or executed incorrectly, causing data inconsistency between VSAM and PostgreSQL.

**Mitigations:**
| # | Mitigation | Effectiveness |
|---|-----------|:---:|
| 1 | Automate rollback as a single-command runbook (script that reverts gateway rules, pauses bridge, re-enables legacy) | Critical |
| 2 | Practice rollback in staging monthly — treat it as a fire drill | High |
| 3 | Design bridge adapters to handle "split-brain" — reconcile diverged data after rollback | High |
| 4 | Maintain legacy in hot-standby (receiving sync'd data) at all times during coexistence | High |
| 5 | Define RTO (Recovery Time Objective) for each phase: rollback must complete within 15 minutes | Medium |

**Residual Risk:** Low (automated rollback with regular drills)

---

## Risk Summary Dashboard

| ID | Risk | Likelihood | Impact | Phase | Status |
|----|------|:----------:|:------:|:-----:|:------:|
| R-01 | Financial calculation discrepancy | High | Critical | 7 | 🔴 Prevent |
| R-02 | Data sync failure during coexistence | High | High | 2–7 | 🟠 Mitigate |
| R-03 | VSAM file locking conflicts | Medium | High | 3–7 | 🟠 Mitigate |
| R-04 | Loss of tribal knowledge | High | High | All | 🟠 Mitigate |
| R-05 | Batch performance regression | Medium | High | 7 | 🟠 Mitigate |
| R-06 | IMS database migration complexity | Medium | Medium | 6 | 🟡 Monitor |
| R-07 | EBCDIC/ASCII conversion errors | Medium | Medium | 2–3 | 🟡 Monitor |
| R-08 | Scope creep | High | Medium | All | 🟡 Monitor |
| R-09 | MQ message compatibility | Medium | Medium | 6 | 🟡 Monitor |
| R-10 | Insufficient test coverage | High | High | All | 🟠 Mitigate |
| R-11 | Team COBOL skills gap | Medium | Medium | 3–7 | 🟡 Monitor |
| R-12 | Rollback failure under pressure | Low | Critical | 3–7 | 🟠 Mitigate |

---

## Risk Review Cadence

| Frequency | Activity |
|-----------|----------|
| Weekly | Project team reviews active risks, updates status |
| Per Phase Gate | Full risk register review; add/retire risks |
| Monthly | Executive steering committee reviews top-5 risks |
| Post-Incident | Any production issue triggers risk register update |

---

## Escalation Thresholds

| Condition | Action |
|-----------|--------|
| Any Critical-impact risk becomes "Realized" | Immediate executive escalation; halt affected phase |
| Dual-run parity fails > 0.01% discrepancy rate | Extend validation period; investigate root cause |
| Sync lag exceeds 5 minutes | Trigger incident; consider rollback |
| Rollback required > 2 times in same phase | Phase re-planning required; architecture review |
