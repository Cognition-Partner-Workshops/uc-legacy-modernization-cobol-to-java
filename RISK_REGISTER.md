# CardDemo Modernization Risk Register

## 1. Overview

This register catalogs the top risks for the CardDemo COBOL-to-Java modernization, scored by likelihood and impact, with concrete mitigations tied to the codebase.

**Risk scoring:**
- **Likelihood:** 1 (Rare) → 5 (Almost Certain)
- **Impact:** 1 (Negligible) → 5 (Critical)
- **Risk Score** = Likelihood × Impact (max 25)

---

## 2. Risk Summary Heat Map

| Risk Score | Category | Count |
|-----------|----------|-------|
| 🔴 16–25 (Critical) | Must address before starting | 3 |
| 🟠 10–15 (High) | Must address before relevant phase | 5 |
| 🟡 5–9 (Medium) | Monitor and mitigate as encountered | 5 |
| 🟢 1–4 (Low) | Accept with contingency plan | 2 |

---

## 3. Detailed Risk Register

### R01: COMP-3 / Packed Decimal Precision Loss

| Attribute | Value |
|-----------|-------|
| **ID** | R01 |
| **Category** | Data Integrity |
| **Risk Score** | 🔴 **20** (Likelihood: 4, Impact: 5) |
| **Phase** | Phase 0 (Data Migration), Phase 5 (Transaction Posting) |
| **Description** | COBOL uses COMP-3 (packed decimal) and COMP (binary) numeric formats for financial calculations. Java's `double`/`float` types introduce floating-point rounding errors. Key fields at risk: `ACCT-CURR-BAL` (PIC S9(10)V99), `TRAN-AMT` (PIC S9(09)V99), `ACCT-CREDIT-LIMIT` (PIC S9(10)V99), and `TRAN-CAT-BAL` (PIC S9(09)V99). |
| **Affected Programs** | COACTUPC, COBIL00C, CBTRN02C, CBACT04C (interest calculation) |
| **Affected Copybooks** | CVACT01Y, CVTRA05Y, CVTRA06Y, CVTRA01Y |
| **Impact** | Financial discrepancies — even a 1-cent variance per transaction compounds to material misstatement across thousands of accounts. Regulatory and audit exposure. |
| **Mitigation** | 1. Use `java.math.BigDecimal` exclusively for all monetary fields. Never use `double`/`float` for financial data.<br>2. Define column types as `NUMERIC(12,2)` in PostgreSQL (matches COBOL PIC S9(10)V99 precision).<br>3. Implement penny-level reconciliation: nightly comparison of sum-of-balances between VSAM and PostgreSQL during dual-run phases.<br>4. Validate the EBCDIC→PostgreSQL conversion pipeline against all 9 ASCII sample files in `app/data/ASCII/`, comparing field-by-field. |
| **Owner** | Data Engineering Lead |
| **Status** | Open |

---

### R02: COACTUPC Validation Logic Parity

| Attribute | Value |
|-----------|-------|
| **ID** | R02 |
| **Category** | Functional Correctness |
| **Risk Score** | 🔴 **16** (Likelihood: 4, Impact: 4) |
| **Phase** | Phase 4 (Account Update) |
| **Description** | COACTUPC is the largest program (4,236 LOC) with 40+ field-level validation rules including US phone format `(XXX)XXX-XXXX`, signed number parsing, mandatory field checks, alpha-only validation, alphanumeric validation, and yes/no flag validation. These rules are embedded inline rather than in reusable routines, making them hard to extract comprehensively. |
| **Affected Programs** | COACTUPC (lines 50–150 define validation flag structures) |
| **Impact** | Missing or incorrect validation allows bad data into the account table, corrupting downstream processes (interest calculation, statement generation). |
| **Mitigation** | 1. Create a validation rule extraction spreadsheet: document every `EVALUATE`, `IF`, and `88-level` condition in COACTUPC that produces an error message.<br>2. Build a test harness that submits the same inputs to both COACTUPC (via CICS) and the new REST API, comparing accept/reject decisions and error messages.<br>3. Achieve 100% path coverage for validation logic before cutover.<br>4. Phase 4 includes a 3-week parallel validation period (longer than other phases) specifically for this risk. |
| **Owner** | QA Lead + COBOL SME |
| **Status** | Open |

---

### R03: CBTRN02C Multi-File Atomicity

| Attribute | Value |
|-----------|-------|
| **ID** | R03 |
| **Category** | Data Integrity |
| **Risk Score** | 🔴 **20** (Likelihood: 4, Impact: 5) |
| **Phase** | Phase 5B (Batch Transaction Posting) |
| **Description** | CBTRN02C (post-tran, 731 LOC) reads from DALYTRAN and writes to 4 files simultaneously: TRANSACT, ACCTDATA, TCATBALF, and DALYREJS. On the mainframe, these writes occur within a single batch step with implicit VSAM file-level locking. In the microservice world, posting a transaction requires updating 3 separate services (Transaction, Account, Category Balance) — a distributed transaction. |
| **Affected Programs** | CBTRN02C (FILE-CONTROL: 6 file declarations at lines 29–61) |
| **Impact** | Partial posting — transaction recorded but account balance not updated, or vice versa. Financial books won't balance. |
| **Mitigation** | 1. Implement the Saga pattern with compensating transactions for the post-tran pipeline.<br>2. Design the Spring Batch job with chunk-based processing and explicit savepoints.<br>3. Build a reconciliation job that runs after each batch: compare count of posted transactions vs. sum of balance changes vs. TCATBALF updates.<br>4. Maintain a dead-letter table (replacing DALYREJS) with full context for manual review.<br>5. During the 4-week parallel batch run (Phase 5B.5), achieve zero discrepancies for 30 consecutive days before cutover. |
| **Owner** | Backend Lead |
| **Status** | Open |

---

### R04: IMS Hierarchical-to-Relational Schema Mapping

| Attribute | Value |
|-----------|-------|
| **ID** | R04 |
| **Category** | Technical Complexity |
| **Risk Score** | 🟠 **15** (Likelihood: 3, Impact: 5) |
| **Phase** | Phase 6 (Authorization & Fraud) |
| **Description** | The authorization module uses IMS DB with hierarchical segments (DBPAUTP0, DBPAUTX0) defined via DBD/PSB files. IMS segment hierarchies don't map directly to relational tables. The PSB definitions (PSBPAUTB, DLIGSAMP, PAUTBUNL) specify different views of the same data for different programs, adding complexity. |
| **Affected Files** | `app/app-authorization-ims-db2-mq/ims/*.dbd`, `*.psb` |
| **Impact** | Incorrect schema design loses data relationships or query performance. Authorization decisions may differ if segment traversal logic isn't correctly translated. |
| **Mitigation** | 1. Engage an IMS DB specialist to analyze DBD segment relationships and translate to an ER diagram before coding begins.<br>2. Start with the existing DB2 AUTHFRDS table (already relational, 27 columns) as the anchor — it captures the final authorization state.<br>3. Design new tables by analyzing the DL/I calls in COPAUA0C, COPAUS0C/1C, COPAUS2C to understand access patterns.<br>4. Validate by loading IMS unload data (PAUTBUNL PSB) into new schema and comparing query results. |
| **Owner** | Data Architect + IMS SME |
| **Status** | Open |

---

### R05: EBCDIC Data Encoding Corruption

| Attribute | Value |
|-----------|-------|
| **ID** | R05 |
| **Category** | Data Migration |
| **Risk Score** | 🟠 **12** (Likelihood: 3, Impact: 4) |
| **Phase** | Phase 0 (Data Migration) |
| **Description** | Production data is stored in EBCDIC encoding (`app/data/EBCDIC/`). Packed decimal (COMP-3) and binary (COMP) fields are bit-encoded, not character-encoded. Incorrect byte interpretation during migration silently produces wrong values (e.g., a COMP-3 balance of $1,234.56 stored as hex `01234C` could be misread). |
| **Affected Files** | All 13 EBCDIC data files in `app/data/EBCDIC/` |
| **Impact** | Silent data corruption — financial values appear valid but are wrong. May not be detected until end-of-month reconciliation. |
| **Mitigation** | 1. Use a COBOL-aware ETL tool (AWS Mainframe Modernization Data Replication, Micro Focus Data Express, or custom Java with `jRecord` library) that understands copybook layouts.<br>2. Validate every migration by comparing against the ASCII equivalents in `app/data/ASCII/` — these 9 files serve as a Rosetta Stone.<br>3. Build automated comparison: for each record in EBCDIC output, decode using copybook and compare field-by-field to the ASCII version.<br>4. Pay special attention to CVEXPORT.cpy which uses COMP, COMP-3, and REDEFINES — the export record layout has 5 overlapping record types in 500 bytes. |
| **Owner** | Data Engineering Lead |
| **Status** | Open |

---

### R06: Batch Window Timing Constraints

| Attribute | Value |
|-----------|-------|
| **ID** | R06 |
| **Category** | Operational |
| **Risk Score** | 🟠 **12** (Likelihood: 3, Impact: 4) |
| **Phase** | Phase 5B (Batch Processing) |
| **Description** | The current batch window requires CICS file close (CLOSEFIL) before batch jobs can run, and file open (OPENFIL) after batch completes — creating an application outage window. The Control-M schedule defines three workflows: DAILY (4 jobs), WEEKLY (up to 8 jobs), and MONTHLY (5 jobs). Spring Batch jobs must complete within the same window or the migration extends outage duration. |
| **Affected Files** | `app/scheduler/CardDemo.controlm`, CLOSEFIL.jcl, OPENFIL.jcl |
| **Impact** | If Spring Batch jobs run slower than COBOL batch, the outage window expands, impacting SLAs. |
| **Mitigation** | 1. Profile mainframe batch execution times as a baseline before migration.<br>2. Run Spring Batch performance tests with production-volume data (scale ASCII samples to production record counts).<br>3. Optimize with parallel chunk processing — Spring Batch supports multi-threaded steps.<br>4. Long-term: eliminate the batch window entirely by replacing CLOSEFIL/OPENFIL with database transactions that don't require exclusive file access. This is an architectural improvement over the mainframe approach.<br>5. Design the new system to support online processing during batch runs (no CICS file close needed). |
| **Owner** | Platform Engineering |
| **Status** | Open |

---

### R07: COMMAREA State Management During Hybrid Phase

| Attribute | Value |
|-----------|-------|
| **ID** | R07 |
| **Category** | Integration |
| **Risk Score** | 🟠 **10** (Likelihood: 5, Impact: 2) |
| **Phase** | Phases 1–5 (entire hybrid period) |
| **Description** | The CARDDEMO-COMMAREA (COCOM01Y, 200+ bytes) is passed between ALL CICS programs via RETURN TRANSID. It carries user context, customer ID, account ID, card number, and navigation state. During the hybrid phase, some transactions run on the new platform while others remain on CICS. The COMMAREA must be faithfully maintained across platform boundaries. |
| **Affected Copybooks** | COCOM01Y (47 lines, 7 data groups) |
| **Impact** | Lost session state — user gets blank screens or wrong account data when navigating between legacy and modernized screens. |
| **Mitigation** | 1. Build a COMMAREA↔Session bridge service that serializes/deserializes COMMAREA fields to/from a server-side session store (Redis).<br>2. Map each COMMAREA field to a typed DTO: `CDEMO-USER-ID` → JWT claim, `CDEMO-ACCT-ID` → URL path parameter, `CDEMO-CARD-NUM` → URL path parameter, `CDEMO-PGM-CONTEXT` → frontend routing state.<br>3. Test every cross-platform navigation path: e.g., user logs in (new auth) → views account (new) → updates card (legacy CICS) → returns to account view (new).<br>4. Phase-specific: retire COMMAREA fields as their owning programs are migrated. Track in a field-retirement matrix. |
| **Owner** | Integration Architect |
| **Status** | Open |

---

### R08: CICS Pseudo-Conversational Pattern Translation

| Attribute | Value |
|-----------|-------|
| **ID** | R08 |
| **Category** | Technical Complexity |
| **Risk Score** | 🟠 **10** (Likelihood: 5, Impact: 2) |
| **Phase** | Phases 2–5 |
| **Description** | All CICS online programs use the pseudo-conversational pattern: process input → send screen → EXEC CICS RETURN TRANSID → wait for next user input. This stateless-per-interaction model is conceptually similar to REST but carries state in COMMAREA between interactions. EIBAID (attention key identification) drives branching: DFHENTER, DFHPF3, DFHPF7, DFHPF8. |
| **Affected Programs** | All 15 online CICS programs |
| **Impact** | Incorrect translation of the pseudo-conversational flow creates state bugs — e.g., data displayed on screen but lost when user submits, or PF-key navigation not working. |
| **Mitigation** | 1. Map each EIBAID handler to a REST endpoint or UI action: DFHENTER → form submit (POST/PUT), DFHPF3 → navigate back, DFHPF7/PF8 → pagination (prev/next page).<br>2. Document the state machine for each transaction: COMEN01C has 11 menu options mapping to 11 XCTL targets; each target has its own EIBAID handlers.<br>3. Build end-to-end UI tests that exercise every PF-key equivalent action. |
| **Owner** | Backend Lead |
| **Status** | Open |

---

### R09: VSAM Alternate Index (AIX) Query Semantics

| Attribute | Value |
|-----------|-------|
| **ID** | R09 |
| **Category** | Data Access |
| **Risk Score** | 🟡 **9** (Likelihood: 3, Impact: 3) |
| **Phase** | Phase 2 (Read Views), Phase 5 (Transactions) |
| **Description** | CARDXREF uses VSAM AIX to allow lookup by XREF-ACCT-ID (alternate key) in addition to XREF-CARD-NUM (primary key). TRANSACT uses AIX for alternate access by card number. CBACT04C (interest calc) explicitly declares `ALTERNATE RECORD KEY IS FD-XREF-ACCT-ID` on XREFFILE. These dual-key access patterns must be preserved in the relational schema. |
| **Affected Programs** | CBACT04C (line 38), COTRN00C (browse by card), COCRDLIC (card list by account) |
| **Impact** | Missing secondary index causes performance degradation or incorrect query results (full table scan instead of index lookup). |
| **Mitigation** | 1. Map each VSAM AIX to a PostgreSQL secondary index: `CREATE INDEX idx_xref_acct ON card_xref(acct_id)` and `CREATE INDEX idx_tran_card ON transactions(card_num)`.<br>2. Verify query plans (EXPLAIN ANALYZE) match the access patterns in COBOL — KSDS sequential browse → indexed range scan; random read by alternate key → index seek.<br>3. Test with production-scale data volumes to ensure index performance. |
| **Owner** | Data Architect |
| **Status** | Open |

---

### R10: Loss of Mainframe Knowledge During Migration

| Attribute | Value |
|-----------|-------|
| **ID** | R10 |
| **Category** | Organizational |
| **Risk Score** | 🟡 **9** (Likelihood: 3, Impact: 3) |
| **Phase** | All phases |
| **Description** | COBOL/CICS/VSAM expertise is scarce and aging. The migration may take 30–40 weeks. Key mainframe SMEs may leave or become unavailable during the project. Undocumented business rules embedded in COBOL programs (especially COACTUPC's 4,236 LOC of validation logic) may be lost. |
| **Impact** | Migration stalls when COBOL interpretation questions arise. Business rules are incorrectly translated. |
| **Mitigation** | 1. Conduct a knowledge extraction sprint (2 weeks) before Phase 1: record screen-share sessions of COBOL SMEs walking through each program.<br>2. Create a COBOL Decision Table for each program documenting every business rule, referencing source line numbers.<br>3. Retain at least one COBOL SME through Phase 7 completion; negotiate retention bonus if needed.<br>4. Use these modernization documents (Blueprint, Domain Decomposition, Cutover Plan) as living documentation — update as rules are discovered. |
| **Owner** | Project Manager |
| **Status** | Open |

---

### R11: Control-M Job Dependency Chain Breakage

| Attribute | Value |
|-----------|-------|
| **ID** | R11 |
| **Category** | Operational |
| **Risk Score** | 🟡 **8** (Likelihood: 2, Impact: 4) |
| **Phase** | Phase 5B |
| **Description** | Control-M orchestrates three workflows with explicit job dependencies (INCOND/OUTCOND): DAILY (CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL), WEEKLY (MNTTRDB2 → CLOSEFIL → DISCGRP → WAITSTEP → OPENFIL + TRANEXTR), and MONTHLY (CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL). Breaking any dependency causes downstream jobs to not execute. |
| **Affected Files** | `app/scheduler/CardDemo.controlm` (93 lines, 4 folders, 2 smart folders) |
| **Impact** | Missed batch runs → stale data → incorrect balances → financial misstatement. |
| **Mitigation** | 1. Map every INCOND/OUTCOND pair from the Control-M XML to the new scheduler's dependency model.<br>2. The WEEKLY workflow has a SMART_FOLDER (DisclosureGroupsRefresh) that depends on TransactionTypesDBRefresh completing — preserve this cross-folder dependency.<br>3. Implement health checks and alerting for each step in the new batch pipeline.<br>4. Run parallel schedules for 30 days: both Control-M and new scheduler trigger jobs; compare execution order and outcomes. |
| **Owner** | Operations Lead |
| **Status** | Open |

---

### R12: MQ Message Format Compatibility

| Attribute | Value |
|-----------|-------|
| **ID** | R12 |
| **Category** | Integration |
| **Risk Score** | 🟡 **6** (Likelihood: 2, Impact: 3) |
| **Phase** | Phase 6 |
| **Description** | COPAUA0C processes authorization requests from MQ with a specific message format. External systems producing these messages may not be under the migration team's control. Changing the message format breaks upstream producers. |
| **Affected Programs** | COPAUA0C (MQ GET/PUT), CODATE01 (MQ date inquiry), COACCT01 (MQ account inquiry) |
| **Impact** | Authorization request processing fails; transactions are not authorized; customer-facing impact. |
| **Mitigation** | 1. Maintain the existing MQ message format as a public contract during the transition.<br>2. Build a message transformer in the MQ-to-event-broker bridge that converts legacy MQ format to the new service's internal format.<br>3. Only deprecate the legacy MQ format after all upstream producers have migrated (coordinate with external teams).<br>4. Schema registry for new event format to prevent drift. |
| **Owner** | Integration Architect |
| **Status** | Open |

---

### R13: REDEFINES / OCCURS DEPENDING ON Mapping

| Attribute | Value |
|-----------|-------|
| **ID** | R13 |
| **Category** | Data Structure |
| **Risk Score** | 🟡 **6** (Likelihood: 2, Impact: 3) |
| **Phase** | Phase 0 (Data Migration) |
| **Description** | CVEXPORT.cpy uses REDEFINES to overlay 5 different record types (Customer, Account, Transaction, Card XREF, Card) onto the same 460-byte `EXPORT-RECORD-DATA` area, selected by `EXPORT-REC-TYPE`. COADM02Y uses REDEFINES for admin menu option arrays. These union-type patterns have no direct Java equivalent. |
| **Affected Copybooks** | CVEXPORT.cpy (103 lines), COADM02Y.cpy, COMEN02Y.cpy |
| **Impact** | Incorrect deserialization of export records → data corruption during branch migration. |
| **Mitigation** | 1. Map each REDEFINES to a Java type hierarchy: abstract `ExportRecord` base class with `CustomerExportRecord`, `AccountExportRecord`, etc. subclasses. Use a factory pattern keyed by `EXPORT-REC-TYPE`.<br>2. For OCCURS DEPENDING ON patterns (COSGN00C LINKAGE SECTION), use Java `List` with runtime size validation.<br>3. Build round-trip tests: serialize Java objects to the exact byte layout defined in CVEXPORT.cpy and compare with COBOL-produced export files. |
| **Owner** | Backend Lead |
| **Status** | Open |

---

### R14: GDG (Generation Data Group) Versioning Replacement

| Attribute | Value |
|-----------|-------|
| **ID** | R14 |
| **Category** | Operational |
| **Risk Score** | 🟢 **4** (Likelihood: 2, Impact: 2) |
| **Phase** | Phase 5B |
| **Description** | DEFGDGB and DEFGDGD JCL jobs define GDG bases for versioned backup datasets. GDG provides automatic rotation of historical data generations. The TRANBKP job creates new generations of transaction backup files. |
| **Affected JCL** | DEFGDGB.jcl, DEFGDGD.jcl, TRANBKP.jcl |
| **Impact** | Loss of data versioning → inability to rollback to previous data state after a bad batch run. |
| **Mitigation** | 1. Replace GDG with S3 versioning for backup files (automatic rotation via lifecycle policies).<br>2. For database-level versioning, implement point-in-time recovery (PostgreSQL WAL archiving).<br>3. Maintain 30-day retention for all backup generations. |
| **Owner** | Platform Engineering |
| **Status** | Open |

---

### R15: Regulatory / Audit Trail Continuity

| Attribute | Value |
|-----------|-------|
| **ID** | R15 |
| **Category** | Compliance |
| **Risk Score** | 🟢 **4** (Likelihood: 1, Impact: 4) |
| **Phase** | Phase 7 (Final Cutover) |
| **Description** | Financial applications require unbroken audit trails. During migration, transactions may be processed by either the legacy or new system. Auditors need a single view of all transactions regardless of which system processed them. |
| **Impact** | Audit finding → regulatory action if transaction history is incomplete or inconsistent. |
| **Mitigation** | 1. Assign a `processing_system` tag to every transaction record during the hybrid phase (LEGACY or MODERN).<br>2. Build a unified audit view that joins legacy VSAM-migrated data with new system data.<br>3. Maintain the complete VSAM dataset archive for 7 years post-migration (regulatory retention).<br>4. Include audit trail continuity as a sign-off criterion at Gate G7 (Final Cutover). |
| **Owner** | Compliance Officer |
| **Status** | Open |

---

## 4. Risk Response Summary

| Response Type | Count | Examples |
|--------------|-------|---------|
| **Mitigate** | 13 | R01–R13: reduce likelihood or impact through specific actions |
| **Accept** | 2 | R14, R15: low likelihood; contingency plans in place |
| **Transfer** | 0 | — |
| **Avoid** | 0 | — |

---

## 5. Risk Monitoring Schedule

| Frequency | Action | Owner |
|-----------|--------|-------|
| Weekly | Review open risks, update likelihood scores based on phase progress | Project Manager |
| Per phase gate | Verify all phase-specific risks are mitigated before go/no-go decision | Steering Committee |
| Monthly | Financial reconciliation between systems during dual-run phases | Finance + Data Engineering |
| Quarterly | External audit of migration data integrity | Compliance Officer |
