# CardDemo Modernization Risk Register

## 1. Overview

This register catalogs the top risks associated with modernizing the CardDemo mainframe application, assessed by likelihood, impact, and overall severity. Each risk includes specific mitigations tied to the CardDemo codebase, affected components, and owner accountability.

### Risk Scoring

| Dimension | 1 (Low) | 2 (Medium) | 3 (High) | 4 (Critical) |
|---|---|---|---|---|
| **Likelihood** | Unlikely (<10%) | Possible (10–40%) | Likely (40–70%) | Almost Certain (>70%) |
| **Impact** | Minor inconvenience | Moderate delay/cost | Major outage or data loss | Catastrophic financial/regulatory |
| **Severity** | = Likelihood × Impact | 1–4 Low | 5–8 Medium | 9–16 High |

---

## 2. Risk Register

### R-01: Financial Calculation Discrepancy (COMP-3 / Packed Decimal)

| Attribute | Value |
|---|---|
| **ID** | R-01 |
| **Category** | Technical — Data Fidelity |
| **Likelihood** | 3 (Likely) |
| **Impact** | 4 (Critical) |
| **Severity** | **12 (High)** |
| **Affected Components** | CBACT04C (interest calc), CBTRN02C (transaction posting), COBIL00C (bill payment) |
| **Affected Phase** | Phase 4 (Transaction), Phase 5 (Interest) |

**Description:**
COBOL uses packed decimal (COMP-3) and zoned decimal arithmetic with implicit truncation rules. Java's `double`/`float` types have floating-point precision issues. Even `BigDecimal` requires explicit rounding mode specification. CBACT04C uses `PIC S9(04)V99` for interest rates and `PIC S9(09)V99` for amounts. Incorrect rounding in Java could cause per-transaction discrepancies that compound across millions of transactions.

**Specific Code Risk:**
- `CBACT04C.cbl`: Interest rate from DISCGRP (`DIS-INT-RATE PIC S9(04)V99`) applied to category balances (`TRAN-CAT-BAL PIC S9(09)V99`)
- `CBTRN02C.cbl`: Balance updates use VSAM REWRITE with COBOL truncation semantics
- `CBSTM03A.CBL`: Uses COMP-3 variable `WS-TOTAL-AMT PIC S9(9)V99 COMP-3`

**Mitigations:**
1. **M-01a:** Use `java.math.BigDecimal` with `RoundingMode.HALF_EVEN` (banker's rounding) for all financial calculations. Prohibit `double`/`float` in financial code paths via static analysis rule.
2. **M-01b:** Create a comprehensive test suite of 10,000+ COBOL-computed reference values covering edge cases (max values, negative balances, zero rates, boundary conditions). Automated comparison pipeline.
3. **M-01c:** Parallel run interest calculation for 3 monthly cycles (Phase 5) with per-account penny-level reconciliation before cutover.
4. **M-01d:** Replatform CBACT04C to Micro Focus first (Phase 5) to validate that COBOL semantics produce identical results on Linux before converting to Java.

**Owner:** Technical Lead — Financial Domain
**Status:** Open

---

### R-02: Batch Transaction Posting Data Integrity

| Attribute | Value |
|---|---|
| **ID** | R-02 |
| **Category** | Technical — Data Integrity |
| **Likelihood** | 3 (Likely) |
| **Impact** | 4 (Critical) |
| **Severity** | **12 (High)** |
| **Affected Components** | CBTRN02C (posting), ACCTDAT, TRANSACT, TCATBALF, DALYREJS |
| **Affected Phase** | Phase 4 |

**Description:**
CBTRN02C is the central batch posting program (731 lines) that reads daily transactions, validates against XREF, writes to TRANSACT, updates ACCTDAT balances, updates TCATBALF category balances, and writes rejects. It touches 6 VSAM files in a single batch run. Any discrepancy in posting logic (missed validation, incorrect balance update, lost transaction) directly impacts customer balances and financial reporting.

**Specific Code Risk:**
- CBTRN02C opens 6 files: DALYTRAN, TRANSACT, XREFFILE, DALYREJS, ACCTFILE, TCATBALF
- Account balance update: `ACCT-CURR-BAL`, `ACCT-CURR-CYC-DEBIT`, `ACCT-CURR-CYC-CREDIT` — multiple fields updated atomically in COBOL
- VSAM REWRITE is atomic per record; PostgreSQL UPDATE requires explicit transaction boundaries

**Mitigations:**
1. **M-02a:** Replatform CBTRN02C to Linux COBOL runtime first. Validate byte-for-byte output parity with mainframe execution for 30 days of production data.
2. **M-02b:** Spring Batch implementation must be idempotent and restartable. Use chunk-based processing with ACID transactions per chunk.
3. **M-02c:** Automated daily reconciliation script comparing TRANSACT record counts, ACCTDAT balance sums, TCATBALF totals, and DALYREJS counts between legacy and new.
4. **M-02d:** 30-day parallel run (Phase 4) with automated discrepancy alerting. Zero tolerance — any discrepancy blocks cutover.
5. **M-02e:** Implement compensating transaction mechanism for partial failures in the distributed service architecture.

**Owner:** Technical Lead — Transaction Domain
**Status:** Open

---

### R-03: COMMAREA Session State Migration

| Attribute | Value |
|---|---|
| **ID** | R-03 |
| **Category** | Technical — Architecture |
| **Likelihood** | 2 (Possible) |
| **Impact** | 3 (High) |
| **Severity** | **6 (Medium)** |
| **Affected Components** | COCOM01Y (COMMAREA), all 20 online CICS programs |
| **Affected Phase** | Phase 1 (Foundation), all subsequent phases |

**Description:**
The CARDDEMO-COMMAREA (COCOM01Y.cpy, 47 lines) carries session state across all CICS programs: user identity, navigation context (from/to program), customer/account/card IDs, and UI state. During the strangler migration, some programs run on CICS while others run as new services. The Anti-Corruption Layer must translate COMMAREA↔JWT bidirectionally without losing session context, especially during mid-flow navigation (e.g., user starts on new menu, navigates to legacy account update, returns to new card view).

**Specific Code Risk:**
- COMMAREA fields: `CDEMO-FROM-TRANID`, `CDEMO-TO-PROGRAM`, `CDEMO-PGM-CONTEXT` (enter/reenter state machine)
- `CDEMO-ACCT-ID`, `CDEMO-CARD-NUM`, `CDEMO-CUST-ID` — context passed between screens
- Every online program does `MOVE DFHCOMMAREA TO CARDDEMO-COMMAREA` — ordering matters

**Mitigations:**
1. **M-03a:** Design ACL to carry full COMMAREA payload as an opaque JWT claim during transition. New services extract only what they need; legacy programs receive the full COMMAREA.
2. **M-03b:** Implement session state service (Redis) as a bridge — new services write context to Redis, ACL reads Redis to populate COMMAREA for legacy programs.
3. **M-03c:** Exhaustively test navigation flows that cross the legacy/modern boundary: new→legacy→new and legacy→new→legacy paths.
4. **M-03d:** Phase 1 deploys ACL early and validates with IAM+Menu programs before any data-carrying programs are migrated.

**Owner:** Architecture Lead
**Status:** Open

---

### R-04: IMS Database Migration (Authorization Module)

| Attribute | Value |
|---|---|
| **ID** | R-04 |
| **Category** | Technical — Data Migration |
| **Likelihood** | 2 (Possible) |
| **Impact** | 3 (High) |
| **Severity** | **6 (Medium)** |
| **Affected Components** | DBPAUTP0 (IMS HIDAM), COPAUA0C, COPAUS0-2C, CBPAUP0C |
| **Affected Phase** | Phase 5 |

**Description:**
The Authorization module uses IMS hierarchical database (HIDAM with index), which has no direct equivalent in the relational world. IMS segment hierarchy, parent-child navigation (GU, GN, GNP calls), and DL/I status codes must be mapped to SQL queries. The two-phase commit between IMS and DB2 (fraud table) must be replicated with distributed transaction management.

**Specific Code Risk:**
- IMS DBDs: DBPAUTP0 (primary), DBPAUTX0 (index) — hierarchical schema
- PSBs: PSBPAUTB, PSBPAUTL — program specification blocks defining access paths
- Two-phase commit: IMS + DB2 AUTHFRDS in COPAUA0C
- DL/I calls in COBOL programs with status code handling

**Mitigations:**
1. **M-04a:** Document IMS hierarchical schema as an ER diagram first. Map parent-child segments to relational tables with foreign keys.
2. **M-04b:** Use PAUDBUNL (IMS unload) to extract all authorization data to sequential file, then load into PostgreSQL via ETL.
3. **M-04c:** Replace two-phase commit with saga pattern or outbox pattern in the new Authorization Service. Accept eventual consistency for fraud flagging (DB2 writes can be async).
4. **M-04d:** This module is optional and isolated. If IMS migration proves too complex, it can be deferred or the module can be replaced with a simplified implementation.

**Owner:** Technical Lead — Authorization Domain
**Status:** Open

---

### R-05: VSAM-to-RDBMS Data Migration Integrity

| Attribute | Value |
|---|---|
| **ID** | R-05 |
| **Category** | Technical — Data Migration |
| **Likelihood** | 2 (Possible) |
| **Impact** | 4 (Critical) |
| **Severity** | **8 (Medium-High)** |
| **Affected Components** | All 12 VSAM KSDS files, CDC pipelines |
| **Affected Phase** | Phases 1–5 |

**Description:**
12 VSAM files with EBCDIC encoding, packed decimal fields, and fixed-length records must be migrated to PostgreSQL. Key risks include: EBCDIC↔UTF-8 character mapping errors (especially for special characters in customer names/addresses), packed decimal (COMP-3) conversion errors, VSAM alternate index (AIX) semantic differences from SQL indexes, and CDC pipeline lag during high-volume batch windows.

**Specific Code Risk:**
- CVACT01Y: `ACCT-CURR-BAL PIC S9(10)V99` — 12-digit signed decimal in VSAM display format
- CVEXPORT: `EXP-ACCT-CURR-BAL PIC S9(10)V99 COMP-3` — same field in packed format in export file
- CXACAIX: Alternate index on CARDXREF for account-based card lookup
- Customer data (CVCUS01Y): 500-byte records with addresses containing potential EBCDIC special characters

**Mitigations:**
1. **M-05a:** Build a comprehensive data migration validation suite that compares every record, every field between VSAM and PostgreSQL. Run after initial migration and after every CDC sync.
2. **M-05b:** Use EBCDIC codepage tables specific to the mainframe region (CCSID). Test with known special characters (accented names, symbols in addresses).
3. **M-05c:** For packed decimal fields, convert using dedicated COBOL-aware libraries (e.g., JRecord, Cobrix) rather than custom parsing code.
4. **M-05d:** Design CDC pipeline with guaranteed delivery (at-least-once) and idempotent writes. Monitor lag with alerting threshold < 5 seconds.
5. **M-05e:** For VSAM AIX (CXACAIX), create equivalent PostgreSQL indexes and validate query plans produce correct results.

**Owner:** Data Engineering Lead
**Status:** Open

---

### R-06: Dual-Write Consistency During Transition

| Attribute | Value |
|---|---|
| **ID** | R-06 |
| **Category** | Technical — Architecture |
| **Likelihood** | 3 (Likely) |
| **Impact** | 3 (High) |
| **Severity** | **9 (High)** |
| **Affected Components** | ACCTDAT, TRANSACT, CARDDAT during Phases 3–4 |
| **Affected Phase** | Phases 3–4 |

**Description:**
During the strangler migration, some services write to both VSAM and PostgreSQL (dual-write) to maintain data consistency between legacy and new systems. Dual-writes are inherently unreliable — if one write succeeds and the other fails, data diverges. Network latency, VSAM file locks, and PostgreSQL transaction timeouts can all cause partial writes.

**Mitigations:**
1. **M-06a:** Use a transactional outbox pattern: write to PostgreSQL first (source of truth), then an async process replicates to VSAM for legacy consumers.
2. **M-06b:** Implement automated reconciliation that runs every hour during dual-write periods, comparing record counts and checksums between VSAM and PostgreSQL.
3. **M-06c:** Build a conflict resolution strategy: PostgreSQL is always the authoritative source. If VSAM write fails, log the failure and retry; if VSAM has stale data, overwrite from PostgreSQL.
4. **M-06d:** Minimize the dual-write window. For each phase, aim to cut over all consumers within 4 weeks of starting dual-write.

**Owner:** Architecture Lead
**Status:** Open

---

### R-07: Batch Window SLA Breach

| Attribute | Value |
|---|---|
| **ID** | R-07 |
| **Category** | Operational — Performance |
| **Likelihood** | 2 (Possible) |
| **Impact** | 3 (High) |
| **Severity** | **6 (Medium)** |
| **Affected Components** | CBTRN02C (posting), CBACT04C (interest), CBSTM03A (statements), Control-M workflows |
| **Affected Phase** | Phases 4–5 |

**Description:**
CardDemo batch jobs execute within defined time windows (daily, weekly, monthly — defined in Control-M). The new Spring Batch implementations must complete within the same windows. Java overhead (JVM startup, garbage collection, database round-trips replacing direct VSAM I/O) could extend batch processing time. The monthly interest calculation + statement generation pipeline is the longest batch chain.

**Specific Code Risk:**
- Control-M MONTHLY flow: CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL — sequential chain
- CBTRN02C reads DALYTRAN sequentially, does random VSAM reads on 5 other files — I/O-intensive
- CBSTM03A generates statements for all accounts — scales with account count

**Mitigations:**
1. **M-07a:** Benchmark legacy batch execution times before migration. Set SLA at legacy time + 20% buffer.
2. **M-07b:** Design Spring Batch jobs with parallel chunk processing. Use partitioned steps for account-level parallelism.
3. **M-07c:** PostgreSQL performance: proper indexing, connection pooling, bulk operations (COPY instead of row-by-row INSERT).
4. **M-07d:** Use read replicas for reporting batch jobs to avoid contention with online services.
5. **M-07e:** Load test with production-equivalent data volumes (number of accounts, daily transaction count) before cutover.

**Owner:** Operations Lead
**Status:** Open

---

### R-08: Knowledge Loss — COBOL/Mainframe Expertise

| Attribute | Value |
|---|---|
| **ID** | R-08 |
| **Category** | Organizational — People |
| **Likelihood** | 3 (Likely) |
| **Impact** | 3 (High) |
| **Severity** | **9 (High)** |
| **Affected Components** | All — especially COACTUPC (4,236 lines), CBSTM03A (ALTER/GO-TO patterns), ASM modules |
| **Affected Phase** | All phases |

**Description:**
The COBOL developer pool is shrinking. COACTUPC (4,236 lines) contains complex validation logic that requires deep COBOL and CICS understanding to correctly port. CBSTM03A deliberately uses legacy patterns (ALTER, GO-TO, mainframe control block addressing) that are difficult to understand even for experienced COBOL developers. ASM modules (MVSWAIT, COBDATFT) require assembler expertise.

**Mitigations:**
1. **M-08a:** Document all business rules extracted from COBOL as prose specifications BEFORE starting code migration. For COACTUPC, create a validation rule matrix mapping each IF/EVALUATE to a business rule.
2. **M-08b:** Retain at least 2 COBOL-experienced developers through the entire migration (not just Phase 1). Budget for mainframe skills contractors if needed.
3. **M-08c:** Use AI-assisted code analysis tools to generate documentation from COBOL source (flow diagrams, data flow analysis).
4. **M-08d:** For CBSTM03A's ALTER/GO-TO patterns: trace execution paths manually before attempting any conversion. Create a control flow diagram.
5. **M-08e:** Pair COBOL experts with Java developers during each phase to facilitate knowledge transfer.

**Owner:** Engineering Manager
**Status:** Open

---

### R-09: BMS/3270 Screen Parity in Modern UI

| Attribute | Value |
|---|---|
| **ID** | R-09 |
| **Category** | Business — User Experience |
| **Likelihood** | 2 (Possible) |
| **Impact** | 2 (Medium) |
| **Severity** | **4 (Low)** |
| **Affected Components** | 17 BMS maps, all CO* online programs |
| **Affected Phase** | Phases 1–3 |

**Description:**
Users accustomed to 3270 terminal workflows (PF key navigation, field-level tabbing, screen-at-a-time interaction) may resist a modern web UI. Keyboard shortcuts, field ordering, and error message positioning may differ, causing productivity loss during transition.

**Mitigations:**
1. **M-09a:** Involve end users in UI design sprints early (Phase 1). Map each BMS screen to a modern equivalent with user sign-off.
2. **M-09b:** Implement keyboard shortcuts in the new UI that mirror PF key functions (e.g., F3=Back, Enter=Submit).
3. **M-09c:** Provide training sessions and quick-reference cards for each phase rollout.
4. **M-09d:** Run the modern UI in parallel with 3270 access for 4 weeks per phase, allowing users to choose.

**Owner:** Product Owner
**Status:** Open

---

### R-10: Regulatory and Audit Compliance During Migration

| Attribute | Value |
|---|---|
| **ID** | R-10 |
| **Category** | Business — Compliance |
| **Likelihood** | 2 (Possible) |
| **Impact** | 4 (Critical) |
| **Severity** | **8 (Medium-High)** |
| **Affected Components** | All financial data (ACCTDAT, TRANSACT, CUSTDATA), AUTHFRDS (fraud), USRSEC |
| **Affected Phase** | All phases |

**Description:**
Credit card systems are subject to PCI-DSS, SOX, and potentially GDPR. During migration, data traverses new paths (CDC pipelines, dual-write, new databases). Audit trails must be maintained. Card numbers (PAN) stored in CARDDAT and TRANSACT must be protected. Customer SSN in CUSTDATA requires encryption at rest.

**Specific Code Risk:**
- CVACT02Y: `CARD-NUM PIC X(16)` — PAN stored in cleartext in VSAM
- CVCUS01Y: `CUST-SSN PIC 9(09)` — SSN stored in cleartext
- CSUSR01Y: `SEC-USR-PWD PIC X(08)` — password stored in cleartext

**Mitigations:**
1. **M-10a:** Encrypt PAN and SSN at rest in PostgreSQL using column-level encryption or tokenization. This is an improvement over legacy cleartext VSAM storage.
2. **M-10b:** Hash passwords with bcrypt in the new IAM service (replacing cleartext SEC-USR-PWD). Force password reset for all users during Phase 1 cutover.
3. **M-10c:** Implement audit logging on all new services from day one. Log who changed what, when, with before/after values.
4. **M-10d:** Engage compliance/audit team before Phase 1 kickoff. Get sign-off on the migration approach and data handling procedures.
5. **M-10e:** Ensure CDC pipelines and dual-write paths do not expose PAN/SSN in logs, metrics, or error messages.

**Owner:** Security / Compliance Lead
**Status:** Open

---

### R-11: MQ Message Contract Breakage (Authorization Module)

| Attribute | Value |
|---|---|
| **ID** | R-11 |
| **Category** | Technical — Integration |
| **Likelihood** | 2 (Possible) |
| **Impact** | 3 (High) |
| **Severity** | **6 (Medium)** |
| **Affected Components** | COPAUA0C (MQ trigger), MQ request/reply queues, POS emulator clients |
| **Affected Phase** | Phase 5 |

**Description:**
External POS systems send authorization requests via MQ in a specific message format (defined in CCPAURQY.cpy). Changing the new authorization service to use Kafka/SQS could break existing MQ integrations. Message format (field positions, lengths, encoding) must be preserved during transition.

**Mitigations:**
1. **M-11a:** Implement an MQ-to-Kafka/SQS bridge adapter that translates message formats bidirectionally. Legacy POS systems continue using MQ; new service consumes from Kafka/SQS.
2. **M-11b:** Version the authorization message contract. New format (JSON) available alongside legacy format (fixed-length EBCDIC) during transition.
3. **M-11c:** Test with production-equivalent MQ message traffic in staging before cutover.
4. **M-11d:** Coordinate with external POS system teams to schedule their migration from MQ to new API (can be post-Phase 5).

**Owner:** Integration Lead
**Status:** Open

---

### R-12: Scope Creep and Timeline Overrun

| Attribute | Value |
|---|---|
| **ID** | R-12 |
| **Category** | Project Management |
| **Likelihood** | 3 (Likely) |
| **Impact** | 2 (Medium) |
| **Severity** | **6 (Medium)** |
| **Affected Components** | All |
| **Affected Phase** | All phases |

**Description:**
Mainframe modernization projects historically overrun by 30–50%. Undiscovered complexity in COBOL programs (especially COACTUPC's 4,236 lines of validation logic), unexpected data quality issues in VSAM files, and mid-project requirement changes can extend timelines.

**Mitigations:**
1. **M-12a:** Each phase has a defined scope and hard boundaries. Features not in scope are logged for future phases, not added to the current one.
2. **M-12b:** Build 20% buffer into each phase timeline. Use the buffer for unexpected issues, not additional scope.
3. **M-12c:** Weekly progress reviews against the cutover plan checklist. Escalate blockers within 48 hours.
4. **M-12d:** If a phase runs >2 weeks behind, trigger a re-planning session with options: absorb delay, reduce scope, or add resources.

**Owner:** Project Manager
**Status:** Open

---

## 3. Risk Summary Matrix

| ID | Risk | Severity | Phase | Status |
|---|---|---|---|---|
| R-01 | Financial Calculation Discrepancy (COMP-3) | **12 High** | 4–5 | Open |
| R-02 | Batch Posting Data Integrity | **12 High** | 4 | Open |
| R-08 | Knowledge Loss — COBOL Expertise | **9 High** | All | Open |
| R-06 | Dual-Write Consistency | **9 High** | 3–4 | Open |
| R-05 | VSAM-to-RDBMS Data Migration | **8 Med-High** | 1–5 | Open |
| R-10 | Regulatory/Audit Compliance | **8 Med-High** | All | Open |
| R-03 | COMMAREA Session State Migration | **6 Medium** | 1+ | Open |
| R-04 | IMS Database Migration | **6 Medium** | 5 | Open |
| R-07 | Batch Window SLA Breach | **6 Medium** | 4–5 | Open |
| R-11 | MQ Message Contract Breakage | **6 Medium** | 5 | Open |
| R-12 | Scope Creep / Timeline Overrun | **6 Medium** | All | Open |
| R-09 | BMS/3270 Screen Parity | **4 Low** | 1–3 | Open |

---

## 4. Risk Monitoring Cadence

| Frequency | Activity | Owner |
|---|---|---|
| Weekly | Review open risks, update likelihood/impact based on progress | Project Manager |
| Per-phase | Reassess all risks at phase start; add new risks discovered | Technical Lead |
| Monthly | Risk report to steering committee | Project Manager |
| On-event | Immediate reassessment when a risk materializes | Risk Owner |

---

## 5. Escalation Thresholds

| Severity | Escalation Path | Response Time |
|---|---|---|
| 9–16 (High) | Engineering Director + Steering Committee | 24 hours |
| 5–8 (Medium) | Engineering Manager + Technical Leads | 48 hours |
| 1–4 (Low) | Team standup discussion | Next sprint |
