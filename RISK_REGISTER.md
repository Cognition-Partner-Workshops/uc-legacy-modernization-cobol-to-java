# CardDemo Risk Register

## Overview

This register identifies the top risks associated with modernizing the CardDemo COBOL/CICS/VSAM mainframe application to Java/Spring Boot/PostgreSQL. Each risk is assessed for likelihood, impact, and overall severity, with concrete mitigation strategies tied to the codebase.

---

## Risk Severity Matrix

| | **Low Impact** | **Medium Impact** | **High Impact** |
|---|---|---|---|
| **High Likelihood** | Medium | High | Critical |
| **Medium Likelihood** | Low | Medium | High |
| **Low Likelihood** | Low | Low | Medium |

---

## Technical Risks

### T-1: Financial Calculation Precision Loss

| Attribute | Value |
|---|---|
| **Category** | Technical |
| **Likelihood** | High |
| **Impact** | Critical |
| **Severity** | **Critical** |
| **Phase affected** | Phase 4-6 (write paths, batch) |

**Description:** COBOL uses fixed-point packed decimal arithmetic (`PIC S9(10)V99`, `PIC S9(9)V99 COMP-3`) with deterministic rounding behavior. Java floating-point types (`double`, `float`) introduce rounding errors. Even `BigDecimal` requires explicit scale and rounding mode configuration to match COBOL behavior.

**Codebase evidence:**
- `CVACT01Y`: `ACCT-CURR-BAL PIC S9(10)V99` -- 12-digit balance with 2 decimal places
- `CVTRA05Y`: `TRAN-AMT PIC S9(09)V99` -- 11-digit transaction amount
- `CBACT04C` (interest calculation): `WS-MONTHLY-INT PIC S9(09)V99`, `WS-TOTAL-INT PIC S9(09)V99` -- intermediate interest values
- `CBSTM03A`: `WS-TOTAL-AMT PIC S9(9)V99 COMP-3` -- packed decimal for statement totals
- `COBIL00C`: Balance update after bill payment uses direct ADD/SUBTRACT on packed decimal fields

**Mitigations:**
1. **Mandatory `BigDecimal`:** Enforce via code review and static analysis that all monetary fields use `java.math.BigDecimal`, never `double`/`float`. Add a custom lint rule.
2. **Rounding mode:** Use `RoundingMode.HALF_EVEN` (banker's rounding) to match COBOL `ROUNDED` clause behavior.
3. **Scale enforcement:** Set scale to 2 for currency, 4 for interest rates (`CVTRA02Y`: `DIS-INT-RATE`).
4. **Penny-test harness:** Build an automated test suite that runs identical calculations in COBOL and Java, comparing results for 10,000+ accounts. Any discrepancy fails the build.
5. **Parallel-run reconciliation:** During Phase 5-6, run daily balance reconciliation between COBOL and Java outputs.

---

### T-2: VSAM-to-PostgreSQL Data Migration Data Loss

| Attribute | Value |
|---|---|
| **Category** | Technical |
| **Likelihood** | Medium |
| **Impact** | High |
| **Severity** | **High** |
| **Phase affected** | Phase 0 (data foundation) |

**Description:** VSAM files use EBCDIC encoding with fixed-length records, packed decimal fields, and zone-decimal representations. Conversion to PostgreSQL requires character encoding translation (EBCDIC -> UTF-8), packed decimal unpacking, and handling of FILLER bytes and REDEFINES clauses.

**Codebase evidence:**
- `app/data/EBCDIC/` contains raw EBCDIC data files for mainframe upload
- `app/data/ASCII/` contains ASCII equivalents (sample data, not production)
- `CVACT01Y`: `FILLER PIC X(178)` -- 178 bytes of padding that must be ignored
- `CVACT02Y`: `FILLER PIC X(59)` -- padding in card records
- `CVCUS01Y`: `FILLER PIC X(168)` -- padding in customer records
- `CSUSR01Y`: `SEC-USR-FILLER PIC X(23)` -- padding in user records
- `CVTRA05Y`: `FILLER PIC X(20)` -- padding in transaction records

**Mitigations:**
1. **Use existing export program:** `CBEXPORT` already converts VSAM to flat files. Use this as the extraction step.
2. **Field-level validation:** For each copybook, build a mapping specification that documents the PIC clause, byte offset, and target PostgreSQL column type. Validate every field during migration.
3. **Record count reconciliation:** Compare VSAM record counts (from `IDCAMS LISTCAT`) with PostgreSQL `SELECT COUNT(*)` after load.
4. **Checksum validation:** Compute checksums on key numeric fields (balances, amounts) in both systems and compare.
5. **Test with EBCDIC data:** Use the `app/data/EBCDIC/` files to validate the conversion pipeline handles encoding correctly, especially for special characters in names and addresses.
6. **Handle REDEFINES:** `CBSTM03A` uses `REDEFINES` on `DB2-FORMAT-TS` and `TWO-BYTES-BINARY`. Document all REDEFINES in the codebase and ensure the correct interpretation is used during migration.

---

### T-3: Transaction ID Format Change Breaks Backward Compatibility

| Attribute | Value |
|---|---|
| **Category** | Technical |
| **Likelihood** | High |
| **Impact** | High |
| **Severity** | **Critical** |
| **Phase affected** | Phase 5 (transaction write) |

**Description:** COBOL generates sequential transaction IDs by reading the last record in the TRANSACT VSAM file and incrementing. The ID is `PIC X(16)` -- a 16-character alphanumeric string. Moving to UUID or database sequence changes the format. All downstream consumers (batch posting, reports, statements, cross-reference lookups) must handle the new format.

**Codebase evidence:**
- `COTRN02C` lines 193-230: Validates input and generates new transaction ID
- `COBIL00C`: Generates payment transaction ID using same pattern
- `CBTRN02C`: Batch posting reads TRANSACT by key (`FD-TRANS-ID PIC X(16)`)
- `CBSTM03A`: Statement generation reads transactions by key
- `CORPT00C`: Report generation references transaction IDs

**Mitigations:**
1. **Keep PIC X(16) format initially:** Generate IDs as zero-padded sequential numbers from a PostgreSQL sequence (`LPAD(nextval('tran_id_seq')::text, 16, '0')`). This maintains format compatibility during parallel run.
2. **ID mapping table:** If UUID is desired long-term, maintain a `transaction_id_mapping` table during transition that maps old-format IDs to new UUIDs.
3. **Deferred UUID migration:** Only switch to UUID format after all consumers are on Java (Phase 8). Run a one-time migration at decommission.
4. **API contract:** The transaction-service API should accept both formats during the transition period.

---

### T-4: CICS COMMAREA Session State Cannot Be Directly Mapped

| Attribute | Value |
|---|---|
| **Category** | Technical |
| **Likelihood** | Medium |
| **Impact** | Medium |
| **Severity** | **Medium** |
| **Phase affected** | Phase 1-5 (all online programs) |

**Description:** CardDemo uses a COMMAREA (`COCOM01Y`, 48 fields) to pass state between CICS programs via `EXEC CICS XCTL` and `EXEC CICS RETURN TRANSID`. This includes user identity, current account/card/customer context, last map displayed, and program flow control. HTTP is stateless, so this state must be managed differently.

**Codebase evidence:**
- `COCOM01Y`: 48-byte COMMAREA with `CDEMO-FROM-TRANID`, `CDEMO-TO-PROGRAM`, `CDEMO-USER-ID`, `CDEMO-USER-TYPE`, `CDEMO-ACCT-ID`, `CDEMO-CARD-NUM`, `CDEMO-LAST-MAP`, `CDEMO-LAST-MAPSET`
- Every online program reads and writes the COMMAREA
- `COSGN00C`: Sets `CDEMO-USER-TYPE` based on USRSEC lookup
- `COMEN01C`: Uses `CDEMO-PGM-CONTEXT` (0=enter, 1=reenter) to manage screen flow

**Mitigations:**
1. **JWT claims for identity:** `CDEMO-USER-ID` and `CDEMO-USER-TYPE` become JWT claims. Eliminates the largest shared state.
2. **URL/query parameters for context:** `CDEMO-ACCT-ID` and `CDEMO-CARD-NUM` become URL path parameters or query parameters in REST calls.
3. **Eliminate program flow state:** `CDEMO-FROM-PROGRAM`, `CDEMO-TO-PROGRAM`, `CDEMO-PGM-CONTEXT` are artifacts of the CICS pseudo-conversational model. In a SPA, navigation state is managed by the frontend router (React Router, etc.).
4. **Session store for multi-step flows:** For flows like account update (confirm screen), use a short-lived server-side session or a draft/pending state in the database.

---

### T-5: Batch Processing Timing and Ordering Dependencies

| Attribute | Value |
|---|---|
| **Category** | Technical |
| **Likelihood** | Medium |
| **Impact** | High |
| **Severity** | **High** |
| **Phase affected** | Phase 6 (batch processing) |

**Description:** The CardDemo batch cycle has strict ordering dependencies (CLOSEFIL -> posting -> interest -> backup -> combine -> statements -> OPENFIL). The CLOSEFIL/OPENFIL jobs exist because VSAM cannot support concurrent online and batch access. While PostgreSQL eliminates this limitation, the processing order of posting before interest calculation is a business rule that must be preserved.

**Codebase evidence:**
- `app/jcl/CLOSEFIL.jcl` and `OPENFIL.jcl`: Close/open CICS file entries for batch window
- `app/jcl/POSTTRAN.jcl` -> `app/jcl/INTCALC.jcl` -> `app/jcl/CREASTMT.jcl`: Sequential dependency
- `CBACT04C`: Interest calculation reads TCATBAL records that are produced by `CBTRN02C` (posting)
- `CBSTM03A`: Statement generation reads TRANSACT records that include posted transactions

**Mitigations:**
1. **Spring Batch job orchestration:** Define a `BatchCycleJob` with sequential steps that enforce the correct order: posting -> interest -> statements.
2. **Eliminate CLOSEFIL/OPENFIL:** PostgreSQL supports MVCC (concurrent reads and writes). No need for file-level locking. Validate that batch jobs use appropriate transaction isolation levels (`READ COMMITTED`).
3. **Job restart capability:** Spring Batch provides built-in restart from the last checkpoint. Configure `chunk`-oriented processing with commit intervals.
4. **Monitoring:** Implement batch job monitoring with alerting for: job failure, job exceeding SLA window, unexpected record counts.
5. **Idempotency:** Ensure batch jobs can be safely re-run. Use a processing date marker to prevent double-posting.

---

### T-6: Complex Control Flow in Statement Generation (CBSTM03A)

| Attribute | Value |
|---|---|
| **Category** | Technical |
| **Likelihood** | Medium |
| **Impact** | Medium |
| **Severity** | **Medium** |
| **Phase affected** | Phase 6 (batch) |

**Description:** `CBSTM03A` (924 LOC) uses several mainframe-specific COBOL patterns that have no direct Java equivalent: `ALTER`/`GO TO` for dynamic control flow, PSA/TCB/TIOT addressing for z/OS control block enumeration, `CALL` to subroutine `CBSTM03B` for file I/O, COMP and COMP-3 variables, and 2-dimensional arrays.

**Codebase evidence:**
- Lines 296-314: `ALTER 8100-FILE-OPEN TO PROCEED TO 8100-TRNXFILE-OPEN` -- dynamically changes GO TO target based on `WS-FL-DD` value
- Lines 262-291: PSA -> TCB -> TIOT pointer chain to enumerate DD names from z/OS control blocks
- Lines 225-233: `WS-TRNX-TABLE` with `WS-CARD-TBL OCCURS 51 TIMES` containing `WS-TRAN-TBL OCCURS 10 TIMES` -- 2D array (51 cards x 10 transactions)
- Lines 59-63: `COMP` and `COMP-3` variables for counters and totals

**Mitigations:**
1. **ALTER/GO TO -> Strategy pattern or switch:** Replace the ALTER-based file-open dispatcher with a Java `Map<String, Runnable>` or strategy pattern. The ALTER simply changes which file-open method is called based on a DD name.
2. **PSA/TCB/TIOT -> Configuration:** The TIOT enumeration is used to display JCL DD names. In Java, replace with application configuration (e.g., `application.yml` file paths) and logging.
3. **2D array -> nested Map/List:** `WS-TRNX-TABLE` (51 cards x 10 transactions) becomes `Map<String, List<Transaction>>`.
4. **COMP/COMP-3 -> Java primitives:** `COMP` (binary) maps to `int`/`long`; `COMP-3` (packed decimal) for monetary values maps to `BigDecimal`.
5. **Subroutine CALL -> method invocation:** `CALL 'CBSTM03B'` becomes a method call or a separate service class injection.
6. **Dedicated test suite:** Create a test suite that compares Java-generated statements against COBOL-generated statements field-by-field for 100+ accounts.

---

## Operational Risks

### O-1: Parallel-Run Data Synchronization Failures

| Attribute | Value |
|---|---|
| **Category** | Operational |
| **Likelihood** | Medium |
| **Impact** | High |
| **Severity** | **High** |
| **Phase affected** | Phase 2-7 (entire parallel run) |

**Description:** During parallel run, data must be synchronized between VSAM (mainframe) and PostgreSQL (Java). CDC (Change Data Capture) latency, conflicts from dual writes, and network failures can cause the two systems to diverge.

**Mitigations:**
1. **Single source of truth:** At any given time, only one system is the master for each data entity. Never allow both systems to write to the same entity simultaneously.
2. **CDC monitoring:** Implement real-time monitoring of CDC lag. Alert if lag exceeds 5 seconds.
3. **Reconciliation jobs:** Run hourly reconciliation that compares record counts and key field checksums between VSAM and PostgreSQL.
4. **Conflict resolution:** Define a clear conflict resolution policy: master system always wins. Log all conflicts for investigation.
5. **Circuit breaker:** If CDC fails, automatically route all traffic back to the master system.

---

### O-2: Extended Batch Window During Migration

| Attribute | Value |
|---|---|
| **Category** | Operational |
| **Likelihood** | Medium |
| **Impact** | Medium |
| **Severity** | **Medium** |
| **Phase affected** | Phase 6 (batch processing) |

**Description:** During the batch migration phase, both COBOL and Java batch jobs may need to run (parallel verification). This doubles the batch processing time and may exceed the available batch window.

**Mitigations:**
1. **Run Java batch on a separate schedule:** Run Java batch jobs 1 hour after COBOL batch completes, using a copy of the data.
2. **Optimize Java batch performance:** Use Spring Batch chunk processing with larger commit intervals (1000 records) and parallel step execution where possible.
3. **Benchmark early:** In Phase 0, run performance benchmarks on the Java batch jobs to establish baseline timing.
4. **Temporary window extension:** Negotiate a temporary batch window extension (e.g., additional 2 hours) during the parallel verification period.

---

### O-3: Loss of Mainframe Operational Expertise

| Attribute | Value |
|---|---|
| **Category** | Operational |
| **Likelihood** | High |
| **Impact** | Medium |
| **Severity** | **High** |
| **Phase affected** | All phases |

**Description:** As the migration progresses, mainframe COBOL expertise becomes less valued and team members may leave or be reassigned. However, the mainframe must remain operational until Phase 8 (decommission), and COBOL knowledge is critical for debugging data issues during parallel run.

**Mitigations:**
1. **Knowledge capture:** Document all COBOL business rules, file layouts, JCL parameters, and operational procedures before migration begins. The four documents in this blueprint set (including this risk register) are a start.
2. **Cross-training:** Ensure at least 2 team members retain COBOL/JCL skills throughout the migration.
3. **Retain key personnel:** Identify critical mainframe staff and provide retention incentives through Phase 8.
4. **Automated COBOL analysis:** Use tools like COBOL-IT Compiler or Micro Focus Visual COBOL to generate documentation from the source code.

---

### O-4: Rollback Complexity Increases Over Time

| Attribute | Value |
|---|---|
| **Category** | Operational |
| **Likelihood** | Medium |
| **Impact** | High |
| **Severity** | **High** |
| **Phase affected** | Phase 4+ (write paths) |

**Description:** As more write operations move to Java, the cost and complexity of rolling back to mainframe increases. Once PostgreSQL becomes the source of truth (Phase 4+), rollback requires reverse-migrating data back to VSAM.

**Mitigations:**
1. **Maintain reverse CDC:** Keep a PostgreSQL -> VSAM sync pipeline operational throughout the migration, even after PostgreSQL becomes master.
2. **Regular rollback testing:** Quarterly rollback drill: route traffic back to mainframe and verify data consistency.
3. **Point of no return threshold:** Define a clear "point of no return" (end of Phase 6). After this, rollback is no longer practical and the team commits to forward-fixing issues.
4. **Incremental commitment:** Each phase's go/no-go checkpoint explicitly evaluates rollback feasibility.

---

## Business Risks

### B-1: Project Timeline Overrun

| Attribute | Value |
|---|---|
| **Category** | Business |
| **Likelihood** | High |
| **Impact** | Medium |
| **Severity** | **High** |
| **Phase affected** | All phases |

**Description:** The estimated 14-18 month timeline is aggressive. Legacy modernization projects historically overrun by 30-50% due to undiscovered business rules, edge cases, and integration complexity.

**Mitigations:**
1. **Phase-level scope control:** Each phase is independently valuable. If timeline pressure mounts, defer lower-priority phases (Phase 7-8) while still delivering value from earlier phases.
2. **Buffer per phase:** Include 20% buffer in each phase estimate (already included in the ranges above).
3. **MVP per phase:** Define minimum viable scope for each phase. If behind schedule, deliver MVP and iterate.
4. **Regular checkpoint reviews:** Bi-weekly progress reviews against the cutover plan milestones.
5. **Dedicated discovery sprints:** Allocate the first 2 weeks of each phase to discovery/analysis before committing to the implementation timeline.

---

### B-2: Dual Operating Costs During Migration

| Attribute | Value |
|---|---|
| **Category** | Business |
| **Likelihood** | High |
| **Impact** | Medium |
| **Severity** | **High** |
| **Phase affected** | Phase 0-7 (12-16 months) |

**Description:** During the parallel-run period, both mainframe (MIPS charges, storage, licenses) and cloud (compute, database, networking) costs are incurred simultaneously. Mainframe costs do not decrease until Phase 8 (decommission).

**Mitigations:**
1. **MIPS reduction tracking:** Monitor mainframe MIPS utilization as traffic shifts to Java. Negotiate MIPS contract adjustments at each phase completion.
2. **Cloud cost optimization:** Use auto-scaling, reserved instances, and spot instances for non-production environments.
3. **Phase-gated funding:** Fund one phase at a time. Each phase's go/no-go checkpoint includes a cost/benefit review.
4. **Early decommission of unused components:** After Phase 1, the USRSEC-related CICS definitions can be removed, providing a small MIPS reduction. After Phase 6, batch JCL and CLOSEFIL/OPENFIL overhead is eliminated.

---

### B-3: Regulatory and Compliance Impact

| Attribute | Value |
|---|---|
| **Category** | Business |
| **Likelihood** | Low |
| **Impact** | High |
| **Severity** | **Medium** |
| **Phase affected** | Phase 0, 4-5 (data migration, financial operations) |

**Description:** Credit card processing is subject to PCI-DSS, SOX, and potentially other financial regulations. The migration must maintain compliance throughout. Data at rest and in transit must be encrypted. Audit trails must be preserved.

**Codebase evidence:**
- `CVCUS01Y`: `CUST-SSN PIC 9(09)` -- Social Security Number stored as plain numeric. Must be encrypted in PostgreSQL.
- `CVACT02Y`: `CARD-CVV-CD PIC 9(03)` -- CVV code stored in card record. PCI-DSS prohibits CVV storage post-authorization.
- `CSUSR01Y`: `SEC-USR-PWD PIC X(08)` -- Plaintext 8-character password. Must be hashed.

**Mitigations:**
1. **PCI-DSS compliance review:** Engage compliance team before Phase 0 to define requirements for the target architecture.
2. **Encrypt SSN at rest:** Use PostgreSQL column-level encryption (`pgcrypto`) or application-level encryption for `CUST-SSN`.
3. **CVV handling:** Do not migrate CVV codes to PostgreSQL storage. If needed for authorization, use a tokenization service.
4. **Password hashing:** Phase 1 (auth rewrite) replaces plaintext passwords with bcrypt hashes. Plan a forced password reset for all users.
5. **Audit trail:** Implement database audit logging (PostgreSQL `pgaudit` extension) from day one.
6. **Data classification:** Tag all columns with sensitivity levels (PII, financial, public) in the data dictionary.

---

### B-4: User Experience Disruption During Transition

| Attribute | Value |
|---|---|
| **Category** | Business |
| **Likelihood** | Medium |
| **Impact** | Medium |
| **Severity** | **Medium** |
| **Phase affected** | Phase 1-5 |

**Description:** Users will transition from 3270 terminal screens to a web-based UI. During the migration, some functions will be on the new UI while others remain on 3270. This split experience may confuse users and reduce productivity.

**Mitigations:**
1. **3270 web emulator bridge:** Deploy a TN3270 web emulator alongside the new UI so users can access both from a browser.
2. **Unified navigation:** The new web UI should include links/redirects to 3270 functions not yet migrated.
3. **Training program:** Develop role-specific training (admin, regular user) for each phase. Train before cutover.
4. **User acceptance testing:** Include end-users in UAT for each phase. Their feedback drives the go/no-go decision.
5. **Phased UI rollout:** Release new UI pages in the same order as backend migration (auth -> account view -> transaction view -> etc.).

---

## Risk Summary Dashboard

| ID | Risk | Severity | Phase | Status |
|---|---|---|---|---|
| **T-1** | Financial calculation precision loss | **Critical** | 4-6 | Open |
| **T-2** | VSAM-to-PostgreSQL data migration data loss | **High** | 0 | Open |
| **T-3** | Transaction ID format change breaks compatibility | **Critical** | 5 | Open |
| **T-4** | CICS COMMAREA session state mapping | **Medium** | 1-5 | Open |
| **T-5** | Batch processing timing and ordering | **High** | 6 | Open |
| **T-6** | Complex control flow in CBSTM03A | **Medium** | 6 | Open |
| **O-1** | Parallel-run data synchronization failures | **High** | 2-7 | Open |
| **O-2** | Extended batch window during migration | **Medium** | 6 | Open |
| **O-3** | Loss of mainframe operational expertise | **High** | All | Open |
| **O-4** | Rollback complexity increases over time | **High** | 4+ | Open |
| **B-1** | Project timeline overrun | **High** | All | Open |
| **B-2** | Dual operating costs during migration | **High** | 0-7 | Open |
| **B-3** | Regulatory and compliance impact | **Medium** | 0, 4-5 | Open |
| **B-4** | User experience disruption | **Medium** | 1-5 | Open |

**Critical risks (2):** T-1 (precision), T-3 (transaction ID)
**High risks (7):** T-2, T-5, O-1, O-3, O-4, B-1, B-2
**Medium risks (5):** T-4, T-6, O-2, B-3, B-4

---

## Risk Review Cadence

- **Weekly:** Review critical and high risks during project standup
- **Bi-weekly:** Full risk register review at phase checkpoint meetings
- **Per-phase:** Risk assessment update before each go/no-go decision
- **Ad-hoc:** Any team member can escalate a new risk at any time

## Risk Ownership

| Role | Risks Owned |
|---|---|
| Technical Lead | T-1, T-2, T-3, T-4, T-5, T-6 |
| Operations Lead | O-1, O-2, O-3, O-4 |
| Project Manager | B-1, B-2 |
| Compliance Officer | B-3 |
| Product Owner | B-4 |
