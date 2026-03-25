# CardDemo Modernization Risk Register

## Risk Scoring

| Dimension | Scale | Description |
|-----------|-------|-------------|
| **Probability** | 1 (Rare) – 5 (Almost Certain) | Likelihood the risk materializes |
| **Impact** | 1 (Negligible) – 5 (Critical) | Severity if the risk materializes |
| **Risk Score** | P x I | 1-8 Low, 9-15 Medium, 16-25 High |

---

## Risk Summary Heat Map

| Risk ID | Risk | Probability | Impact | Score | Category |
|---------|------|-------------|--------|-------|----------|
| R-01 | Business rule loss during COACTUPC migration | 4 | 5 | **20 High** | Technical |
| R-02 | Batch cycle ordering failure | 3 | 5 | **15 Medium** | Technical |
| R-03 | Data integrity loss during VSAM-to-RDBMS migration | 3 | 5 | **15 Medium** | Technical |
| R-04 | Transaction ID collision (COBIL00C sequence replacement) | 3 | 5 | **15 Medium** | Technical |
| R-05 | Dual-write inconsistency during parallel-run | 4 | 4 | **16 High** | Technical |
| R-06 | CDC replication lag causing stale reads | 3 | 4 | **12 Medium** | Technical |
| R-07 | Password migration (plaintext to hashed) | 2 | 4 | **8 Low** | Operational |
| R-08 | COBOL numeric precision loss (COMP-3 / packed decimal) | 4 | 4 | **16 High** | Technical |
| R-09 | EBCDIC-to-ASCII data conversion errors | 3 | 3 | **9 Medium** | Technical |
| R-10 | Interest calculation discrepancy | 4 | 5 | **20 High** | Financial |
| R-11 | Batch window overrun in Spring Batch | 3 | 4 | **12 Medium** | Operational |
| R-12 | CICS file locking semantics not replicated | 3 | 4 | **12 Medium** | Technical |
| R-13 | Loss of mainframe expertise during project | 3 | 4 | **12 Medium** | Organizational |
| R-14 | Scope creep from optional modules (IMS/DB2/MQ) | 4 | 3 | **12 Medium** | Organizational |
| R-15 | Rollback failure during cutover | 2 | 5 | **10 Medium** | Operational |
| R-16 | Duplicate logic across migrated services | 3 | 3 | **9 Medium** | Technical |
| R-17 | Performance degradation under production load | 3 | 4 | **12 Medium** | Technical |
| R-18 | Incomplete BMS-to-web UI mapping | 3 | 3 | **9 Medium** | Technical |
| R-19 | Regulatory/audit compliance gap during transition | 2 | 5 | **10 Medium** | Compliance |
| R-20 | Team skill gap (COBOL-to-Java knowledge transfer) | 4 | 3 | **12 Medium** | Organizational |

---

## Detailed Risk Analysis

### R-01: Business Rule Loss During COACTUPC Migration

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Probability** | 4 (Likely) |
| **Impact** | 5 (Critical) |
| **Score** | **20 High** |
| **Affected Phase** | Phase 6 (Account Management) |
| **Affected Artifacts** | `COACTUPC` (4,237 lines), `CVACT01Y`, `CVCUS01Y` |

**Description**: `COACTUPC` is the largest program at 4,237 lines containing extensive field-level validation: SSN format validation (area/group/serial with exclusions for 000, 666, 900-999 ranges), US phone validation, date validation with leap-year handling, credit limit bounds checking, FICO score validation, and account status transitions. These rules are embedded in deeply nested EVALUATE/IF blocks without external documentation.

**Mitigation**:
1. **Extract every validation rule** from COACTUPC into a formal test specification document before writing any Java code. Each EVALUATE branch and IF condition becomes a test case.
2. **Build a validation test harness** that runs identical inputs through both COBOL and Java validation logic, comparing outputs field-by-field.
3. **Use Bean Validation annotations** (JSR 380) in Java to make validation rules declarative and auditable.
4. **Code review gate**: Require COBOL-literate reviewer sign-off on every validation rule migration.
5. **Proactively consolidate duplicate validation logic** — the same SSN and date validation patterns appear in multiple programs; extract into shared utility classes during migration.

**Contingency**: If validation discrepancies are found post-cutover, route affected account updates back to COACTUPC via the strangler facade while fixes are applied.

---

### R-02: Batch Cycle Ordering Failure

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Probability** | 3 (Possible) |
| **Impact** | 5 (Critical) |
| **Score** | **15 Medium** |
| **Affected Phase** | Phase 8 (Batch Processing) |
| **Affected Artifacts** | JCL: CLOSEFIL, POSTTRAN, INTCALC, TRANBKP, COMBTRAN, CREASTMT, TRANIDX, OPENFIL |

**Description**: The batch cycle has implicit ordering dependencies encoded in JCL job scheduling: CLOSEFIL must run before any data refresh; POSTTRAN must complete before INTCALC; INTCALC before TRANBKP; all before CREASTMT. These dependencies are not documented — they are embedded in JCL job streams and CA7/Control-M schedules in `app/scheduler/`. Incorrect ordering causes data corruption (e.g., calculating interest on unposted transactions).

**Mitigation**:
1. **Document the dependency graph** explicitly from JCL analysis. The confirmed order is: CLOSEFIL → Data Refresh → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL.
2. **Model as a Spring Batch flow** with explicit step dependencies (not just sequential ordering). Use `FlowBuilder` with `.next()` chains and conditional transitions.
3. **Add step-completion verification**: Each step validates its preconditions (e.g., INTCALC verifies POSTTRAN completed successfully).
4. **Integration test** the full batch flow with production-scale data before cutover.

**Contingency**: If a step fails mid-cycle, the Spring Batch restart mechanism resumes from the failed step (leveraging `JobRepository` checkpoints). VSAM batch cycle remains available as fallback during parallel-run period.

---

### R-03: Data Integrity Loss During VSAM-to-RDBMS Migration

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Probability** | 3 (Possible) |
| **Impact** | 5 (Critical) |
| **Score** | **15 Medium** |
| **Affected Phase** | Phase 0 (Foundation) |
| **Affected Artifacts** | All VSAM files, all copybook record layouts |

**Description**: VSAM records use fixed-length fields with FILLER bytes (e.g., ACCTDAT has 178 bytes of FILLER in a 300-byte record). Data may exist in FILLER areas from previous program versions. EBCDIC-encoded data in `app/data/EBCDIC/` may contain packed decimal (COMP-3) fields that lose precision during conversion. Signed fields (`PIC S9(10)V99`) use trailing overpunch signs in EBCDIC.

**Mitigation**:
1. **Full record dump and comparison**: Convert every VSAM record to both ASCII and RDBMS formats; compare byte-by-byte.
2. **FILLER audit**: Inspect FILLER areas in production data for non-space content. Document any legacy data in FILLER regions.
3. **Use ASCII test data** (`app/data/ASCII/`) as the primary migration source — these files are already in a parseable format.
4. **Decimal precision**: Map all `PIC S9(n)V99` fields to `DECIMAL(n+2, 2)` in PostgreSQL. Never use floating-point types for financial data.
5. **Reconciliation checkpoints** at every phase (record counts, financial totals, checksums).

**Contingency**: Maintain VSAM as the authoritative source until reconciliation confirms zero discrepancy. Reload RDBMS from VSAM if corruption is detected.

---

### R-04: Transaction ID Collision

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Probability** | 3 (Possible) |
| **Impact** | 5 (Critical) |
| **Score** | **15 Medium** |
| **Affected Phase** | Phase 5 (Bill Payment) |
| **Affected Artifacts** | `COBIL00C` (lines 212-219), TRANSACT VSAM |

**Description**: `COBIL00C` generates transaction IDs by reading the last transaction (READPREV on TRANSACT file with HIGH-VALUES key) and incrementing by 1. This works on CICS because file-level locking prevents concurrent access. In a multi-threaded Java environment, this pattern causes race conditions and duplicate IDs.

**Mitigation**:
1. **Replace with database sequence**: `CREATE SEQUENCE transaction_id_seq START WITH {max_existing_id + 1}`.
2. **Alternative**: Use UUID v7 (time-ordered) for new transaction IDs if downstream systems can handle 128-bit IDs.
3. **During dual-write period**: Modern system uses sequence; legacy uses READPREV. Allocate non-overlapping ID ranges to prevent collision.

**Contingency**: If collisions occur during parallel-run, halt the modern write path and investigate. The legacy READPREV approach remains safe under CICS file control.

---

### R-05: Dual-Write Inconsistency During Parallel-Run

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Probability** | 4 (Likely) |
| **Impact** | 4 (Major) |
| **Score** | **16 High** |
| **Affected Phase** | Phases 3-8 (all dual-write phases) |

**Description**: During parallel-run, writes go to both PostgreSQL and VSAM. Network failures, timing differences, or transaction rollbacks can cause the two stores to diverge. VSAM has no concept of distributed transactions (2PC) — writes are committed file-by-file.

**Mitigation**:
1. **Designate a single authoritative source** per phase. During forward migration, PostgreSQL is authoritative; VSAM receives best-effort sync.
2. **Use an outbox pattern**: Write to PostgreSQL first, then asynchronously publish changes to a sync process that updates VSAM.
3. **Reconciliation job**: Run every batch cycle to compare PostgreSQL and VSAM totals. Alert on any discrepancy.
4. **Idempotent sync**: All VSAM sync operations must be idempotent (REWRITE with same key).

**Contingency**: If divergence is detected, halt the modern write path and resync VSAM from PostgreSQL (or vice versa depending on which is authoritative).

---

### R-06: CDC Replication Lag Causing Stale Reads

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Probability** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Score** | **12 Medium** |
| **Affected Phase** | Phases 2-7 |

**Description**: Modern read services (reporting, transaction inquiry) read from CDC-replicated PostgreSQL tables. If VSAM changes replicate with > 5 second lag, users may see stale data. During high-volume batch posting, lag could spike to minutes.

**Mitigation**:
1. **Monitor CDC lag** continuously with alerting at 5-second threshold.
2. **Add `Last-Updated` headers** to API responses so clients know data freshness.
3. **For critical reads** (e.g., account balance before bill payment), read directly from the authoritative source rather than the replica.
4. **Throttle batch posting** if CDC lag exceeds 30 seconds.

**Contingency**: If CDC lag becomes unacceptable, switch affected read services to query the authoritative source directly (VSAM via adapter or PostgreSQL depending on phase).

---

### R-07: Password Migration (Plaintext to Hashed)

| Attribute | Value |
|-----------|-------|
| **Category** | Operational |
| **Probability** | 2 (Unlikely to cause technical failure) |
| **Impact** | 4 (Major user disruption) |
| **Score** | **8 Low** |
| **Affected Phase** | Phase 1 (Identity & Access) |
| **Affected Artifacts** | `CSUSR01Y` (`SEC-USR-PWD PIC X(08)`) |

**Description**: USRSEC stores passwords in plaintext (8-character `PIC X` field). Modern auth service must use bcrypt/scrypt hashing. Passwords cannot be reverse-migrated; all users must reset passwords.

**Mitigation**:
1. **Coordinate password reset campaign** with business stakeholders before Phase 1 cutover.
2. **Provide self-service password reset** via email/SMS verification in the new auth service.
3. **Temporary dual-auth**: During transition, if a user hasn't reset their password, fall back to comparing against the migrated plaintext (over TLS only, with forced reset on next login).
4. **Enforce password policy upgrade**: New passwords must meet modern standards (min 12 chars, no reuse).

**Contingency**: Extend the parallel-run period for Phase 1 if password reset adoption is below 80%.

---

### R-08: COBOL Numeric Precision Loss (COMP-3 / Packed Decimal)

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Probability** | 4 (Likely) |
| **Impact** | 4 (Major) |
| **Score** | **16 High** |
| **Affected Phase** | All phases with financial data |
| **Affected Artifacts** | All `PIC S9(n)V99` fields in copybooks |

**Description**: COBOL uses fixed-point decimal arithmetic (`PIC S9(10)V99` = 10 integer digits, 2 decimal places). Java `double` and `float` types introduce floating-point rounding errors. Interest calculation (`CBACT04C`) compounds these errors across millions of records. Even a 0.01-cent difference per record compounds to significant totals.

**Mitigation**:
1. **Mandatory**: Use `java.math.BigDecimal` for ALL financial fields. Never use `double` or `float`.
2. **Set rounding mode** explicitly: `RoundingMode.HALF_UP` (matches COBOL default rounding behavior).
3. **Map `PIC S9(10)V99`** to `BigDecimal` with scale=2 in Java and `DECIMAL(12,2)` in PostgreSQL.
4. **Interest rate fields** (`PIC S9(4)V99` in `CVTRA02Y`) map to `BigDecimal` with scale=2 or higher depending on computation needs.
5. **Penny-level reconciliation**: Compare every financial total to the penny between COBOL and Java outputs.

**Contingency**: If precision discrepancies are found, analyze the specific COBOL arithmetic statements (`COMPUTE`, `MULTIPLY`, `DIVIDE`) and match the exact intermediate rounding behavior in Java.

---

### R-09: EBCDIC-to-ASCII Data Conversion Errors

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Probability** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Score** | **9 Medium** |
| **Affected Phase** | Phase 0 (Foundation) |

**Description**: Production data on the mainframe is in EBCDIC encoding. Special characters, accented names, and non-printable bytes in FILLER regions may convert incorrectly. The `app/data/ASCII/` files are pre-converted but may not represent all production edge cases.

**Mitigation**:
1. **Use IBM's EBCDIC code page tables** (CP037 for US English) for conversion.
2. **Character-by-character audit** of all customer names (`CUST-FIRST-NAME`, `CUST-LAST-NAME`) and merchant names (`TRAN-MERCHANT-NAME`).
3. **Validate** using the pre-converted ASCII files as the baseline.
4. **Handle LOW-VALUES/HIGH-VALUES** explicitly: COBOL uses `X'00'` and `X'FF'` as sentinel values. Map to appropriate SQL representations (NULL, MAX_VALUE).

**Contingency**: Maintain an EBCDIC-to-ASCII lookup table for manual correction of edge cases discovered post-migration.

---

### R-10: Interest Calculation Discrepancy

| Attribute | Value |
|-----------|-------|
| **Category** | Financial |
| **Probability** | 4 (Likely) |
| **Impact** | 5 (Critical — regulatory/financial) |
| **Score** | **20 High** |
| **Affected Phase** | Phase 8 (Batch Processing) |
| **Affected Artifacts** | `CBACT04C` (653 lines), `CVTRA01Y`, `CVTRA02Y` |

**Description**: `CBACT04C` computes interest using: category balance (`TRAN-CAT-BAL`) x interest rate (`DIS-INT-RATE`) from the disclosure group (`CVTRA02Y`). The rate is `PIC S9(4)V99`, and computation involves multiplication of large balances by small rates. Any difference in rounding, intermediate precision, or rate lookup logic produces financial discrepancies that may violate Truth in Lending Act (TILA) requirements.

**Mitigation**:
1. **Line-by-line translation** of `1300-COMPUTE-INTEREST` paragraph in CBACT04C.
2. **Use `BigDecimal`** with explicit scale and rounding at every arithmetic step.
3. **Test with production-scale data**: Run interest calculation on 100,000+ accounts and compare every individual account's interest charge to the penny.
4. **Regulatory review**: Have compliance team verify that the modern calculation produces results within acceptable regulatory tolerance.
5. **Identify and eliminate duplicate interest logic** — ensure a single, authoritative interest calculation service is used across all contexts.

**Contingency**: If discrepancies exceed regulatory tolerance, halt Phase 8 cutover. Run COBOL interest calculation as a verification step alongside the Java calculation until root cause is resolved.

---

### R-11: Batch Window Overrun in Spring Batch

| Attribute | Value |
|-----------|-------|
| **Category** | Operational |
| **Probability** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Score** | **12 Medium** |
| **Affected Phase** | Phase 8 |

**Description**: Mainframe batch jobs are optimized for sequential I/O on high-speed DASD. Spring Batch running on commodity hardware with RDBMS may be slower for large-volume sequential processing. If the batch window overruns, it impacts online availability.

**Mitigation**:
1. **Performance benchmark** early in Phase 8 with production-equivalent data volumes.
2. **Use Spring Batch partitioning** to parallelize work across multiple threads/nodes.
3. **Optimize database**: Batch-mode SQL, bulk inserts, reduced index maintenance during batch, partition tables by date.
4. **Eliminate unnecessary steps**: CLOSEFIL/OPENFIL, TRANIDX, and data refresh steps are eliminated in the modern architecture, saving significant time.

**Contingency**: If batch overruns occur, scale up the batch infrastructure (vertical scaling) or add partitioning (horizontal scaling). Fall back to JCL batch cycle if modern batch cannot complete within the window.

---

### R-12: CICS File Locking Semantics Not Replicated

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Probability** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Score** | **12 Medium** |
| **Affected Phase** | Phases 5-8 |

**Description**: CICS provides file-level and record-level locking for VSAM operations. The CLOSEFIL/OPENFIL pattern prevents online access during batch processing. In the modern architecture, PostgreSQL uses row-level locking and MVCC. Concurrent reads during batch writes behave differently (dirty reads vs. snapshot isolation).

**Mitigation**:
1. **Use PostgreSQL transaction isolation levels** appropriate to each operation (READ COMMITTED for online, SERIALIZABLE for batch posting if needed).
2. **Replace CLOSEFIL/OPENFIL** with a batch-in-progress flag that the online system can query to show a "batch processing in progress" notice.
3. **Test concurrent access scenarios**: Online account view during batch posting, bill payment during interest calculation.

**Contingency**: If concurrency issues arise, introduce a maintenance window for batch processing (replicating the CLOSEFIL/OPENFIL pattern in a less disruptive way).

---

### R-13: Loss of Mainframe Expertise During Project

| Attribute | Value |
|-----------|-------|
| **Category** | Organizational |
| **Probability** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Score** | **12 Medium** |
| **Affected Phase** | All phases |

**Description**: COBOL/CICS/JCL expertise is concentrated in a small number of team members. If these experts leave during the 12-16 month project, critical knowledge about business rules, batch cycle behavior, and data formats is lost.

**Mitigation**:
1. **Knowledge capture sessions**: Record video walkthroughs of every COBOL program with the COBOL experts.
2. **Pair programming**: Pair COBOL experts with Java developers during Phases 6 and 8 (the most complex phases).
3. **Document everything**: Every business rule in COACTUPC, every batch cycle dependency, every data format quirk.
4. **Retention incentives**: Ensure COBOL experts are retained through at least Phase 8 completion.

**Contingency**: If experts depart, engage external COBOL consulting firms. The well-documented codebase (naming conventions, copybook comments) reduces but does not eliminate this risk.

---

### R-14: Scope Creep from Optional Modules

| Attribute | Value |
|-----------|-------|
| **Category** | Organizational |
| **Probability** | 4 (Likely) |
| **Impact** | 3 (Moderate) |
| **Score** | **12 Medium** |
| **Affected Phase** | Phase 7 |

**Description**: The three optional modules (IMS-DB2-MQ authorization, DB2 transaction types, VSAM-MQ inquiry) introduce additional middleware technologies (IMS DB, DB2, MQ) beyond the core VSAM/CICS stack. Each module requires its own migration strategy, testing, and expertise.

**Mitigation**:
1. **Treat optional modules as separate work streams** with independent timelines.
2. **Prioritize**: If not all modules are in production use, defer or eliminate unused modules.
3. **DB2 modules are lower risk** — SQL translates to modern RDBMS. Prioritize these over IMS DB modules.
4. **IMS DB module**: Consider replacing entirely rather than migrating the hierarchical data model.

**Contingency**: Defer optional modules to a Phase 7b if they threaten the critical path. Core CardDemo functionality (Phases 1-6, 8) is independent of these modules.

---

### R-15: Rollback Failure During Cutover

| Attribute | Value |
|-----------|-------|
| **Category** | Operational |
| **Probability** | 2 (Unlikely) |
| **Impact** | 5 (Critical) |
| **Score** | **10 Medium** |
| **Affected Phase** | All cutover phases |

**Description**: If a cutover fails and rollback is needed, data written to PostgreSQL during the modern system's operation must be synced back to VSAM. VSAM has no built-in import mechanism for bulk data — loading requires JCL REPRO or custom programs.

**Mitigation**:
1. **VSAM backup** before every cutover (TRANBKP JCL job pattern).
2. **Rollback scripts** pre-written and tested for every phase.
3. **Time-limited cutover windows**: If issues aren't resolved within the rollback window, execute rollback immediately.
4. **Rehearse rollbacks** in staging environment before each production cutover.

**Contingency**: If rollback data sync fails, take a system outage to perform a full VSAM restore from backup.

---

### R-16: Duplicate Logic Across Migrated Services

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Probability** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Score** | **9 Medium** |
| **Affected Phase** | All phases |

**Description**: The legacy codebase contains duplicate validation and data access logic across programs (e.g., date validation in `CSUTLDTC`, `COACTUPC`, and `COBIL00C`; account balance checks in `COBIL00C`, `COACTUPC`, and `CBTRN02C`; CARDXREF lookups in 5+ programs). If each program is migrated independently, these duplications persist in the modern codebase, increasing maintenance cost and divergence risk.

**Mitigation**:
1. **Proactively identify duplicate logic during migration** — catalog all shared patterns before writing any Java code.
2. **Extract shared libraries**: Date validation → `DateValidationUtil`, SSN validation → `SsnValidator`, account balance checks → `AccountBalanceService`, CARDXREF lookups → `CardXrefRepository`.
3. **Centralize in the owning service**: Account validation logic lives exclusively in `account-service`. Other services call the account-service API rather than reimplementing validation.
4. **Code review gate**: Flag any PR that introduces validation logic already present in another service.

**Contingency**: If duplicate logic ships to production, refactor in the next sprint. Track duplication debt in a shared technical debt backlog.

---

### R-17: Performance Degradation Under Production Load

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Probability** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Score** | **12 Medium** |
| **Affected Phase** | All phases |

**Description**: CICS online programs benefit from mainframe I/O optimization and keep-alive connections. The modern microservices architecture introduces network hops, serialization overhead, and database connection pooling. Pagination queries (COCRDLIC reads 10 records at a time via STARTBR/READNEXT) may perform differently with SQL.

**Mitigation**:
1. **Load test** each service at 2x expected production volume before cutover.
2. **Database indexing strategy**: Create indexes matching every VSAM key and alternate key path.
3. **Connection pooling**: Configure HikariCP with appropriate pool sizes.
4. **Caching**: Cache static data (disclosure groups, transaction types, menu configurations).
5. **Pagination**: Use keyset pagination (WHERE id > :last_id LIMIT 10) instead of OFFSET for large tables.

**Contingency**: Scale up infrastructure or optimize hot paths. Specific programs to watch: COCRDLIC (card list — 1,460 lines of pagination logic), COTRN00C (transaction list — high-volume table).

---

### R-18: Incomplete BMS-to-Web UI Mapping

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Probability** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Score** | **9 Medium** |
| **Affected Phase** | Phases 1-7 |

**Description**: 17 BMS maps define the 3270 terminal UI. Each map has specific field positions, attributes (protected, numeric, bright/dark), and PF key handling. Modern web UI must replicate the workflow without requiring pixel-perfect layout. Users accustomed to 3270 keyboard shortcuts (PF3=Back, PF7=Up, PF8=Down, ENTER=Submit) may struggle with a mouse-based web UI.

**Mitigation**:
1. **Map each BMS field** to a form field with equivalent validation attributes.
2. **Keyboard shortcuts**: Implement Escape=Back, PageUp/PageDown for pagination, Enter=Submit to ease transition.
3. **User acceptance testing**: Include mainframe-experienced users in UAT for every phase.
4. **Training materials**: Create side-by-side comparison guides (3270 screen vs. web form).

**Contingency**: Provide a "classic" mode in the web UI that mimics 3270 layout for users who resist the new interface.

---

### R-19: Regulatory/Audit Compliance Gap During Transition

| Attribute | Value |
|-----------|-------|
| **Category** | Compliance |
| **Probability** | 2 (Unlikely) |
| **Impact** | 5 (Critical) |
| **Score** | **10 Medium** |
| **Affected Phase** | All phases |

**Description**: Credit card processing is subject to PCI-DSS, SOX, and TILA regulations. During the transition period, data flows through both legacy and modern systems. Audit trails must be maintained in both systems. Card numbers (`PIC X(16)` in multiple copybooks) must be encrypted at rest in the modern system.

**Mitigation**:
1. **Encrypt card numbers** (TRAN-CARD-NUM, CARD-NUM, XREF-CARD-NUM) at rest in PostgreSQL using column-level encryption or tokenization.
2. **Audit logging**: Every data mutation in the modern system must be logged with timestamp, user, and before/after values.
3. **Compliance review** at Phase 0: Engage compliance team to define requirements for the modern architecture.
4. **PCI-DSS scope**: Ensure the modern environment is PCI-DSS compliant before any card data is stored.

**Contingency**: If compliance gaps are discovered, halt migration of card-number-bearing data until gaps are closed. Card data remains on the mainframe (which is already PCI-compliant) during remediation.

---

### R-20: Team Skill Gap (COBOL-to-Java Knowledge Transfer)

| Attribute | Value |
|-----------|-------|
| **Category** | Organizational |
| **Probability** | 4 (Likely) |
| **Impact** | 3 (Moderate) |
| **Score** | **12 Medium** |
| **Affected Phase** | All phases |

**Description**: Java developers may misunderstand COBOL semantics: level-88 condition names, REDEFINES overlays, COMP-3 packed decimal, OCCURS DEPENDING ON, EVALUATE TRUE patterns, CICS pseudo-conversational programming model, and the implicit PERFORM...THRU fall-through behavior.

**Mitigation**:
1. **COBOL literacy training**: 2-week crash course for Java developers covering COBOL data types, control flow, and CICS concepts.
2. **Create a COBOL-to-Java pattern guide** mapping: EVALUATE → switch, PERFORM → method call, COPY → import, Working-Storage → class fields, COMMAREA → DTO/session.
3. **Pair programming** during Phases 6 and 8 (most complex migrations).
4. **Automated COBOL-to-pseudocode tools**: Use tools like IBM Watsonx Code Assistant for Z to generate initial Java translations for review.

**Contingency**: Engage COBOL modernization consultants for the highest-risk phases (6 and 8).

---

## Risk Ownership Matrix

| Risk | Owner | Reviewer | Escalation |
|------|-------|----------|------------|
| R-01 | Account Service Lead | COBOL SME | Technical Director |
| R-02 | Batch Processing Lead | Operations | Technical Director |
| R-03 | Data Migration Lead | DBA | Technical Director |
| R-04 | Payment Service Lead | DBA | Technical Director |
| R-05 | Data Migration Lead | Architecture | CTO |
| R-06 | Infrastructure Lead | Architecture | Technical Director |
| R-07 | Identity Service Lead | Business Owner | Product Manager |
| R-08 | All Service Leads | DBA / COBOL SME | Technical Director |
| R-09 | Data Migration Lead | COBOL SME | Technical Director |
| R-10 | Batch Processing Lead | Compliance | CTO |
| R-11 | Batch Processing Lead | Operations | Technical Director |
| R-12 | Architecture Lead | DBA | Technical Director |
| R-13 | Project Manager | HR | CTO |
| R-14 | Project Manager | Architecture | Product Manager |
| R-15 | Operations Lead | Architecture | CTO |
| R-16 | Architecture Lead | All Service Leads | Technical Director |
| R-17 | Performance Lead | Infrastructure | Technical Director |
| R-18 | Frontend Lead | UX / Business Users | Product Manager |
| R-19 | Compliance Lead | Legal / Security | CTO |
| R-20 | Project Manager | COBOL SME | Technical Director |

---

## Risk Review Schedule

| Frequency | Activity |
|-----------|----------|
| Weekly | Review all High risks (R-01, R-05, R-08, R-10) in project standup |
| Bi-weekly | Review all Medium risks; update probability/impact based on project progress |
| Per-phase gate | Full risk register review before each phase cutover go/no-go decision |
| Monthly | Executive risk summary report to steering committee |
