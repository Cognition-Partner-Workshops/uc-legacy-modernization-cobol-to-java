# Risk Register — CardDemo COBOL-to-Java Migration

> Top 10 migration risks with likelihood, impact, mitigation strategies, and early warning indicators.

---

## Table of Contents

1. [Risk Scoring Framework](#risk-scoring-framework)
2. [Risk Summary Matrix](#risk-summary-matrix)
3. [Risk Details](#risk-details)
   - [RISK-01: Hidden Business Rules in COACTUPC](#risk-01-hidden-business-rules-in-coactupc)
   - [RISK-02: VSAM-to-RDBMS Data Fidelity Loss](#risk-02-vsam-to-rdbms-data-fidelity-loss)
   - [RISK-03: Cross-Reference Coupling via cardxref.txt (CCXREF)](#risk-03-cross-reference-coupling-via-cardxreftxt-ccxref)
   - [RISK-04: CICS Runtime Dependency Gap](#risk-04-cics-runtime-dependency-gap)
   - [RISK-05: IMS Database Migration (Authorization Sub-App)](#risk-05-ims-database-migration-authorization-sub-app)
   - [RISK-06: Financial Calculation Precision (Interest Calculation)](#risk-06-financial-calculation-precision-interest-calculation)
   - [RISK-07: Test Data Representativeness](#risk-07-test-data-representativeness)
   - [RISK-08: Shared Copybook Divergence During Migration](#risk-08-shared-copybook-divergence-during-migration)
   - [RISK-09: Batch Window and Performance Regression](#risk-09-batch-window-and-performance-regression)
   - [RISK-10: Mainframe-Specific Runtime Dependencies](#risk-10-mainframe-specific-runtime-dependencies)
4. [Risk Heat Map](#risk-heat-map)
5. [Risk Monitoring Schedule](#risk-monitoring-schedule)

---

## Risk Scoring Framework

| Dimension | Low (1) | Medium (2) | High (3) | Critical (4) |
|-----------|---------|------------|----------|---------------|
| **Likelihood** | Unlikely — requires multiple failures | Possible — has occurred in similar projects | Likely — expected based on codebase analysis | Almost certain — inherent in the approach |
| **Impact** | Minor — delays < 1 week, no data loss | Moderate — delays 2–4 weeks, rework needed | Major — delays > 1 month, data integrity concerns | Severe — production data loss, regulatory exposure, project cancellation |

**Risk Score** = Likelihood x Impact (range: 1–16)

| Score Range | Classification | Action |
|-------------|---------------|--------|
| 1–3 | **Low** | Monitor; address opportunistically |
| 4–6 | **Medium** | Plan mitigation; allocate contingency |
| 8–9 | **High** | Active mitigation required; escalate to project sponsor |
| 12–16 | **Critical** | Immediate action; risk threatens project success |

---

## Risk Summary Matrix

| ID | Risk | Likelihood | Impact | Score | Phase(s) Affected |
|----|------|-----------|--------|-------|-------------------|
| RISK-01 | Hidden business rules in COACTUPC (4,236 LOC, 188 decision points) | High (3) | Major (3) | **9** | Phase 4 |
| RISK-02 | VSAM-to-RDBMS data fidelity loss | Medium (2) | Critical (4) | **8** | Phases 3–5 |
| RISK-03 | Cross-reference coupling via cardxref.txt (CCXREF) | High (3) | Major (3) | **9** | Phases 3–5 |
| RISK-04 | CICS runtime dependency gap | High (3) | Moderate (2) | **6** | Phases 2–6 |
| RISK-05 | IMS database migration (Authorization sub-app) | Medium (2) | Major (3) | **6** | Phase 7 |
| RISK-06 | Financial calculation precision (interest) | Medium (2) | Critical (4) | **8** | Phase 4 |
| RISK-07 | Test data representativeness | High (3) | Major (3) | **9** | All phases |
| RISK-08 | Shared copybook divergence during migration | Medium (2) | Moderate (2) | **4** | Phases 3–6 |
| RISK-09 | Batch window and performance regression | Medium (2) | Major (3) | **6** | Phase 5 |
| RISK-10 | Mainframe-specific runtime dependencies | Medium (2) | Moderate (2) | **4** | All phases |

---

## Risk Details

### RISK-01: Hidden Business Rules in COACTUPC

| Attribute | Detail |
|-----------|--------|
| **Description** | COACTUPC.cbl contains 4,236 lines of code with 188 decision points (168 IF + 20 EVALUATE statements), 56 copybook references, and 7 I/O operations. This single program encodes the majority of account management business rules — credit limit validation, balance boundary checks, status transition logic, date format validation, multi-field cross-validation, and error messaging. Many of these rules are implicit in nested IF/EVALUATE blocks and are not documented externally. The risk is that the Java rewrite will miss rules, introduce incorrect behavior, or silently change edge case handling. |
| **Likelihood** | **High (3)** — At 188 decision points, it is statistically near-certain that some rules will be missed or misunderstood during manual rewrite. COBOL's nested paragraph structure and GO TO/PERFORM flow make it easy to miss conditional branches. |
| **Impact** | **Major (3)** — Incorrect account updates could corrupt customer balances, credit limits, or account statuses. This affects every customer in the system. |
| **Risk Score** | **9 (High)** |
| **Affected Phase** | Phase 4 (Account Management) |
| **Root Cause** | COACTUPC was written incrementally over many years. Business rules were added as nested conditionals without refactoring or documentation. The 56 copybook dependencies mean the program interacts with nearly every data structure in the system. |

#### Mitigation Strategy

| Action | Owner | When |
|--------|-------|------|
| **Rule extraction sprint** — Before coding the Java replacement, conduct a dedicated 2-week analysis sprint where two developers independently document every business rule in COACTUPC by tracing every IF/EVALUATE branch. Compare and reconcile the two analyses. | Tech Lead | Pre-Phase 4 |
| **Automated rule comparison** — Build a test harness that sends identical inputs to both COACTUPC (via CICS) and the Java replacement, then compares all output fields and VSAM file changes. Run against the full account dataset. | QA Lead | Phase 4 parallel-run |
| **Fuzz testing** — Generate 10,000+ randomized account update requests with boundary values (zero balances, max credit limits, expired dates, invalid statuses). Run against both COBOL and Java. Flag any divergence. | QA Lead | Phase 4 |
| **Business stakeholder review** — Present the extracted rule catalog to business stakeholders who understand the credit card domain. They can identify rules that are intentional vs. accidental artifacts of COBOL coding. | Product Owner | Pre-Phase 4 |

#### Early Warning Indicators

- Rule extraction sprint identifies > 200 distinct business rules (indicates even higher complexity than estimated).
- Automated comparison reveals divergent behavior in > 1% of test cases during first parallel-run week.
- Development velocity on COACTUPC Java replacement falls below 50 LOC/day (indicates developers are struggling with logic comprehension).
- More than 5 "undocumented" rules are discovered after parallel-run begins (indicates pre-analysis was insufficient).

---

### RISK-02: VSAM-to-RDBMS Data Fidelity Loss

| Attribute | Detail |
|-----------|--------|
| **Description** | VSAM files use fixed-length records with COBOL PIC clauses that define exact byte layouts, including COMP (binary), COMP-3 (packed decimal), and S9(n)V99 (signed decimal with implied decimal point) data types. Converting these to PostgreSQL column types risks precision loss, sign handling errors, and character encoding issues (EBCDIC → UTF-8). Key files at risk: ACCTDAT (300 bytes, S9(10)V99 balance fields), TCATBALF (50 bytes, S9(09)V99 balance), TRANSACT (350 bytes, S9(09)V99 amounts), and DISCGRP (50 bytes, S9(04)V99 interest rates). |
| **Likelihood** | **Medium (2)** — COMP-3 and signed decimal conversions are well-understood but error-prone. The CardDemo data files include both ASCII and EBCDIC versions, and the data seed files (`app/data/ASCII/*.txt`) may not exercise all edge cases (negative balances, maximum values, packed decimal high-nibble variations). |
| **Impact** | **Critical (4)** — Data fidelity loss in financial fields directly impacts customer balances and interest calculations. Even a 1-cent discrepancy, compounded across accounts and billing cycles, can trigger regulatory scrutiny. |
| **Risk Score** | **8 (High)** |
| **Affected Phases** | Phases 3 (CARDDAT, CCXREF), 4 (ACCTDAT, TCATBALF, DISCGRP), 5 (TRANSACT, DALYTRAN) |

#### Mitigation Strategy

| Action | Owner | When |
|--------|-------|------|
| **Field-by-field mapping document** — For every VSAM copybook field, document the exact COBOL PIC clause, the target PostgreSQL column type (using NUMERIC(p,s) for all monetary fields), and the conversion formula. Verify with test data. | Data Architect | Pre-Phase 3 |
| **Use BigDecimal exclusively** — In Java code, all monetary and rate fields must use `java.math.BigDecimal` with explicit scale and rounding mode (HALF_EVEN for financial). Never use `double` or `float`. | All Developers | All phases |
| **Byte-level comparison tool** — Build a utility that reads VSAM records, converts to PostgreSQL rows, then converts back to VSAM format. Compare the round-trip bytes. Any non-filler byte differences are flagged. | Data Engineer | Pre-Phase 3 |
| **EBCDIC test dataset** — Use the `app/data/EBCDIC/` files to verify that EBCDIC-to-UTF-8 conversion handles all character code points correctly, especially for signed overpunch digits in zoned decimal fields. | Data Engineer | Pre-Phase 3 |

#### Early Warning Indicators

- Round-trip byte comparison tool reports differences in monetary fields for any record.
- BigDecimal arithmetic produces results that differ from COBOL COMPUTE results by more than 0.001.
- EBCDIC-to-UTF-8 conversion produces garbled characters in name or address fields.
- Database migration record count does not match VSAM file record count.

---

### RISK-03: Cross-Reference Coupling via cardxref.txt (CCXREF)

| Attribute | Detail |
|-----------|--------|
| **Description** | The card cross-reference file (`cardxref.txt` → CCXREF VSAM KSDS + CXACAIX alternate index) is the most widely shared data file in the estate. It is read by **12 programs across 5 bounded contexts**: Account Management (COACTUPC, COACTVWC, CBACT04C), Card Management (COCRDLIC, COCRDUPC), Transaction Processing (CBTRN01C, CBTRN02C, CBTRN03C, COTRN02C), Reporting (CBSTM03A), and Data Exchange (CBEXPORT, CBIMPORT). During the phased migration, some programs will read CCXREF from VSAM while others read from PostgreSQL. Any inconsistency between the VSAM and PostgreSQL copies — even a brief window during sync — could cause programs to resolve card-to-account mappings differently. |
| **Likelihood** | **High (3)** — With 12 consumers across 5 phases, some inconsistency during the multi-month migration is almost certain. The CXACAIX alternate index (lookup by account ID rather than card number) adds complexity because PostgreSQL requires a secondary index rather than a native VSAM AIX. |
| **Impact** | **Major (3)** — Incorrect card-to-account resolution means transactions could be posted to wrong accounts, statements could include wrong cards, and balance updates could affect wrong accounts. |
| **Risk Score** | **9 (High)** |
| **Affected Phases** | Phases 3 (CCXREF migrated), 4 (Account programs read PostgreSQL copy), 5 (Transaction programs cut over) |

#### Mitigation Strategy

| Action | Owner | When |
|--------|-------|------|
| **Single source of truth during transition** — Designate PostgreSQL as the authoritative store from Phase 3. VSAM CCXREF becomes a read replica, refreshed via CDC (Change Data Capture) from PostgreSQL. All writes go to PostgreSQL. | Data Architect | Phase 3 |
| **Automated consistency check** — Run a nightly job that compares every record in VSAM CCXREF with PostgreSQL `card_cross_references`. Alert on any discrepancy. | DevOps | Phases 3–5 |
| **CXACAIX replacement validation** — Verify that the PostgreSQL secondary index on `acct_id` returns identical result sets to the VSAM alternate index for all account IDs in the test dataset. | QA Lead | Phase 3 |
| **Freeze cross-ref writes during cutover** — During each phase cutover, temporarily halt any process that modifies CCXREF (primarily CBIMPORT). This creates a clean sync window. | Operations | Each phase cutover |

#### Early Warning Indicators

- Nightly consistency check reports any record difference between VSAM and PostgreSQL.
- CDC sync lag exceeds 5 minutes during peak processing.
- Any program reports "card not found" or "account not found" errors that did not occur before migration.
- CXACAIX queries return different result counts than PostgreSQL index queries.

---

### RISK-04: CICS Runtime Dependency Gap

| Attribute | Detail |
|-----------|--------|
| **Description** | 20 online programs depend on CICS services: EXEC CICS SEND/RECEIVE MAP (BMS screen I/O), EXEC CICS READ/WRITE/REWRITE/DELETE (VSAM access), EXEC CICS STARTBR/READNEXT/READPREV/ENDBR (browse), EXEC CICS XCTL/LINK/RETURN (program transfer), EXEC CICS WRITEQ TD (transient data queues), and EXEC CICS INQUIRE PROGRAM (dynamic program check). These CICS APIs have no direct equivalent in Java — they must be replaced with HTTP request/response, JPA operations, and REST routing respectively. The risk is that CICS-specific behaviors (pseudo-conversational programming model, COMMAREA state management, BMS map field attribute handling) are not fully replicated. |
| **Likelihood** | **High (3)** — CICS pseudo-conversational programming (SEND MAP → RETURN → RECEIVE MAP) is fundamentally different from HTTP request/response. Every online program uses this pattern. |
| **Impact** | **Moderate (2)** — The Java replacement will not attempt to replicate CICS behavior — it will use standard HTTP patterns. The risk is in the transition period where some programs run in CICS and others in Java, requiring session state translation between the two paradigms. |
| **Risk Score** | **6 (Medium)** |
| **Affected Phases** | Phases 2–6 (all online program migrations) |

#### Mitigation Strategy

| Action | Owner | When |
|--------|-------|------|
| **CICS-to-REST mapping document** — For each CICS API used, document the equivalent Spring Boot pattern. Map COMMAREA fields to JWT claims and/or session objects. Map BMS map fields to DTO fields. | Architect | Pre-Phase 2 |
| **API Gateway session bridge** — During transition, the API Gateway translates between CICS COMMAREA sessions and JWT tokens. Migrated programs use JWT; unmigrated programs use COMMAREA. The gateway maintains a mapping table. | Platform Team | Phase 2 |
| **BMS map → DTO test coverage** — For each BMS map copybook, verify that all map fields appear in the corresponding Java DTO. Automated test compares field counts and names. | QA Lead | Each phase |

#### Early Warning Indicators

- Users experience session drops when navigating between migrated (JWT) and unmigrated (CICS) screens.
- BMS map fields missing from Java DTOs discovered during integration testing.
- COMMAREA data corruption when translating between JWT claims and COCOM01Y fields.

---

### RISK-05: IMS Database Migration (Authorization Sub-App)

| Attribute | Detail |
|-----------|--------|
| **Description** | The Authorization IMS-DB2-MQ sub-application uses IMS hierarchical databases (DBPAUTP0, DBPAUTX0) accessed via DL/I calls (GU, GN, GHN, GHU, ISRT, REPL, DLET). IMS databases have a hierarchical (parent-child segment) structure that does not map directly to relational tables. The PCB (Program Communication Block) copybooks (PAUTBPCB, PADFLPCB, PASFLPCB) define segment layouts and access paths, but the actual database schema (DBD — Database Description) is defined externally in the IMS control blocks. The programs PAUDBLOD (369 LOC), PAUDBUNL (317 LOC), and DBUNLDGS (366 LOC) are the only programs that directly access IMS, and they use function codes defined in IMSFUNCS.cpy. |
| **Likelihood** | **Medium (2)** — IMS-to-relational migration is a well-studied problem with known tooling and patterns. However, the specific segment hierarchy of the CardDemo IMS databases must be reverse-engineered from the PCB and DBD definitions, which may have undocumented relationships. |
| **Impact** | **Major (3)** — Incorrect migration of payment authorization data could cause transaction approvals/declines to be incorrect, which has immediate financial impact on cardholders and merchants. |
| **Risk Score** | **6 (Medium)** |
| **Affected Phase** | Phase 7 (Sub-Application Migration) |

#### Mitigation Strategy

| Action | Owner | When |
|--------|-------|------|
| **DBD/PSB reverse-engineering** — Analyze the IMS DBD and PSB definitions in `app/app-authorization-ims-db2-mq/ims/` to produce a complete segment hierarchy diagram. Map each segment to a relational table with foreign keys. | Data Architect | Pre-Phase 7 |
| **PAUDBUNL extraction as source data** — Use the existing PAUDBUNL program to extract all IMS data to flat files. Parse and transform these flat files into PostgreSQL rows. This avoids direct IMS-to-PostgreSQL tooling. | Data Engineer | Phase 7 |
| **Dual-consumer validation** — During parallel-run, process MQ authorization messages through both COBOL (IMS-backed) and Java (PostgreSQL-backed) consumers. Compare authorization decisions and response codes for 100% of messages. | QA Lead | Phase 7 |

#### Early Warning Indicators

- DBD reverse-engineering reveals segment relationships not represented in the PCB copybooks.
- PAUDBUNL extraction produces records that cannot be parsed by the documented copybook layouts.
- Dual-consumer validation shows authorization decision divergence for any message.
- IMS segment data contains values outside the expected ranges defined in the copybook field validation rules.

---

### RISK-06: Financial Calculation Precision (Interest Calculation)

| Attribute | Detail |
|-----------|--------|
| **Description** | CBACT04C.cbl (652 LOC, 86 decision points) computes monthly interest using the formula: `interest = (category_balance * disclosure_rate) / 1200`. The COBOL implementation uses `S9(10)V99` for balances (12-digit signed with 2 decimal places) and `S9(04)V99` for interest rates (6-digit signed with 2 decimal places). COBOL arithmetic has specific truncation and rounding behavior that differs from Java's `BigDecimal` defaults. The interest calculation reads TCATBALF (category balances by account+type+category), CCXREF (cross-reference), DISCGRP (disclosure group rates), and ACCTDAT (account records), then writes interest transactions to TRANSACT. |
| **Likelihood** | **Medium (2)** — Java `BigDecimal` can match COBOL precision, but the developer must explicitly set the correct scale, precision, and rounding mode for every arithmetic operation. COBOL's default truncation behavior (TRUNC(STD)) silently drops excess digits, which developers may not replicate. |
| **Impact** | **Critical (4)** — Even a 1-cent discrepancy in interest calculation, when compounded across thousands of accounts and multiple billing cycles, can result in material financial reporting errors. Credit card interest calculations are subject to regulatory audit (Truth in Lending Act, Regulation Z). |
| **Risk Score** | **8 (High)** |
| **Affected Phase** | Phase 4 (Account Management — CBACT04C migration) |

#### Mitigation Strategy

| Action | Owner | When |
|--------|-------|------|
| **COBOL arithmetic tracing** — Instrument CBACT04C to log every intermediate calculation result (pre-division, post-division, pre-rounding, post-rounding) for a representative set of accounts. Use these traces as the "golden" expected values for the Java implementation. | COBOL Developer | Pre-Phase 4 |
| **BigDecimal configuration standard** — Establish a project-wide standard: all monetary calculations use `BigDecimal` with `MathContext.DECIMAL128`, scale of 2, and `RoundingMode.HALF_EVEN` (banker's rounding). Document deviations where COBOL uses truncation instead of rounding. | Architect | Pre-Phase 4 |
| **Historical replay test** — Re-run interest calculation for 6 months of historical data through both COBOL and Java. Compare to-the-penny for every account. Zero tolerance for discrepancies. | QA Lead | Phase 4 |
| **Regulatory review** — Have the compliance team verify that the Java interest calculation meets Truth in Lending Act / Regulation Z requirements. | Compliance | Phase 4 |

#### Early Warning Indicators

- COBOL arithmetic tracing reveals intermediate results that require scale > 2 (indicating hidden precision requirements).
- Historical replay shows any discrepancy exceeding $0.00 for any account in any month.
- DISCGRP interest rates contain values with more than 2 decimal places of precision (the PIC S9(04)V99 allows 2, but manual data entry could encode rates differently).
- COBOL COMPUTE statements use ON SIZE ERROR handlers, indicating the original developers expected overflow conditions.

---

### RISK-07: Test Data Representativeness

| Attribute | Detail |
|-----------|--------|
| **Description** | The CardDemo system includes seed data files in `app/data/ASCII/` (acctdata.txt, carddata.txt, cardxref.txt, custdata.txt, dailytran.txt, tcatbal.txt, trancatg.txt, trantype.txt, discgrp.txt). These files represent a small, synthetic dataset for demonstration purposes. They may not cover edge cases present in a production environment: accounts with zero or negative balances, cards in suspended/closed status, transactions with maximum amounts, customers with international addresses, cross-references with one-to-many card-to-account relationships, and transaction categories with zero-rate interest groups. If the migration is validated only against this seed data, defects that appear in production-representative data will not be caught until after cutover. |
| **Likelihood** | **High (3)** — Seed/demo data is almost never representative of production edge cases. The CardDemo data files are clearly synthetic (sequential IDs, uniform distribution, no outliers). |
| **Impact** | **Major (3)** — Defects discovered post-cutover require emergency rollback, which is disruptive and erodes stakeholder confidence in the migration. |
| **Risk Score** | **9 (High)** |
| **Affected Phases** | All phases |

#### Mitigation Strategy

| Action | Owner | When |
|--------|-------|------|
| **Production-representative test dataset** — Create or obtain a sanitized production dataset that includes edge cases: overlimit accounts, expired cards, international customers, high-value transactions, accounts with many cards, and every transaction type/category combination. If production data is unavailable, generate synthetic data with statistical properties matching production distributions. | QA Lead | Pre-Phase 1 |
| **Edge case catalog** — From the DATA_DICTIONARY.md validation rules, compile a list of every boundary value, every status flag value, and every nullable/optional field. Generate test records that exercise each boundary. | QA Lead | Pre-Phase 1 |
| **Negative testing** — For each migrated program, test with: invalid inputs (wrong data types, too-long values), missing cross-references (card with no account, account with no customer), and concurrent access (simultaneous reads and writes to the same record). | QA Lead | Each phase |
| **Chaos data injection** — After parallel-run stabilizes, inject deliberately malformed records into the test dataset. Verify that both COBOL and Java handle them identically (both reject or both accept). | QA Lead | Each phase |

#### Early Warning Indicators

- Seed data covers fewer than 50% of the distinct transaction type/category combinations defined in TRANTYPE and TRANCATG.
- Seed data contains no negative balance accounts, no expired cards, or no inactive accounts.
- Edge case catalog identifies more than 100 boundary conditions not covered by seed data.
- First production deployment reveals defects in data patterns not present in test data.

---

### RISK-08: Shared Copybook Divergence During Migration

| Attribute | Detail |
|-----------|--------|
| **Description** | During the phased migration, COBOL programs and Java services will coexist. The COBOL programs rely on copybooks (especially COCOM01Y used by 20 programs, CVACT03Y used by 12 programs, and CVACT01Y used by 12 programs) to define data structures. The Java services define equivalent structures as JPA entities and DTOs. If a field is added, renamed, or retyped in the Java entity (e.g., to fix a misspelling like `ACCT-EXPIRAION-DATE` → `accountExpirationDate`), the COBOL copybook does not automatically reflect this change. Conversely, if a COBOL program is patched during migration (bug fix, regulatory change), the Java entity may not be updated. This divergence can cause data interpretation mismatches between COBOL and Java during the transition period. |
| **Likelihood** | **Medium (2)** — The migration spans 10–14 months. It is likely that at least one COBOL program will require a patch (regulatory or bug fix) during this period, and it is likely that at least one Java entity will evolve beyond the original COBOL field layout. |
| **Impact** | **Moderate (2)** — Divergence causes subtle data interpretation errors (e.g., a field is read at the wrong offset), but these are typically caught by automated comparison tools during parallel-run. |
| **Risk Score** | **4 (Medium)** |
| **Affected Phases** | Phases 3–6 (multi-phase coexistence period) |

#### Mitigation Strategy

| Action | Owner | When |
|--------|-------|------|
| **Change freeze on copybooks** — During the migration, institute a change control process for any copybook modification. All copybook changes must be reflected in the corresponding Java entity within the same sprint. | Tech Lead | All phases |
| **Automated schema comparison** — Build a CI check that compares COBOL copybook field definitions with Java entity field definitions. Flag any mismatches in field count, field order, or field size. | DevOps | Pre-Phase 3 |
| **Semantic mapping registry** — Maintain a living document that maps each COBOL copybook field to its Java entity property, including known intentional differences (e.g., fixing the `EXPIRAION` typo). | Architect | All phases |

#### Early Warning Indicators

- A COBOL copybook is modified without a corresponding Java entity update (detected by CI check).
- A Java entity is modified without verifying compatibility with the corresponding COBOL copybook.
- Parallel-run comparison detects a field that COBOL and Java interpret differently.

---

### RISK-09: Batch Window and Performance Regression

| Attribute | Detail |
|-----------|--------|
| **Description** | The daily batch processing pipeline (POSTTRAN.jcl: JCL SORT → CBTRN01C → CBTRN02C) must complete within its batch window (typically 2–4 hours overnight). The COBOL pipeline benefits from mainframe-optimized I/O: VSAM buffered reads, JCL SORT utility (hardware-accelerated on z/OS), and in-memory WORKING-STORAGE. The Spring Batch replacement must achieve comparable throughput using JDBC/JPA writes to PostgreSQL, Java-based sorting, and chunk-oriented processing. Additionally, CBSTM03A (statement generation, 117 I/O operations) and CBACT04C (interest calculation, 19 I/O operations reading 4 files) must also complete within their monthly batch windows. |
| **Likelihood** | **Medium (2)** — Spring Batch is a mature framework with excellent throughput for chunk-oriented processing. However, the I/O pattern changes fundamentally (sequential VSAM reads → random-access JDBC queries), and network latency between the application server and database adds overhead not present in the mainframe's tightly coupled I/O subsystem. |
| **Impact** | **Major (3)** — If the batch window is exceeded, daily transaction processing cannot complete before the next business day begins. This delays account balance updates, transaction reports, and downstream feeds. |
| **Risk Score** | **6 (Medium)** |
| **Affected Phase** | Phase 5 (Transaction Processing Pipeline), Phase 4 (Interest Calculation), Phase 6 (Statement Generation) |

#### Mitigation Strategy

| Action | Owner | When |
|--------|-------|------|
| **Performance benchmark early** — Before full migration, build a Spring Batch proof-of-concept for CBTRN01C/02C using production-scale data volumes. Measure throughput and compare to COBOL batch execution time. | Performance Engineer | Pre-Phase 5 |
| **Chunk size tuning** — Experiment with Spring Batch chunk sizes (100, 500, 1000, 5000) to find the optimal balance between commit frequency and throughput. | Performance Engineer | Phase 5 |
| **Database optimization** — Use PostgreSQL `COPY` for bulk inserts instead of row-by-row JPA inserts. Use batch JDBC `executeBatch()` for updates. Partition transaction tables by date. | DBA | Phase 5 |
| **Parallel step execution** — Spring Batch supports parallel step execution and partitioned steps. CBTRN01C (validation) and CBTRN02C (posting) could be parallelized by card number range. | Developer | Phase 5 |

#### Early Warning Indicators

- POC batch job takes more than 150% of the COBOL batch execution time.
- PostgreSQL write throughput for transaction inserts falls below 10,000 rows/second.
- Spring Batch chunk commits cause database lock contention.
- Memory usage exceeds 4GB for the batch JVM (indicates inefficient chunking or unbounded reads).

---

### RISK-10: Mainframe-Specific Runtime Dependencies

| Attribute | Detail |
|-----------|--------|
| **Description** | Several COBOL programs depend on mainframe-specific runtime services that have no direct cloud equivalent: (1) **LE (Language Environment) APIs** — CBACT01C calls COBDATFT (assembler date formatting), CSUTLDTC calls CEEDAYS (LE date validation returning Lillian date), all programs call CEE3ABD (LE abend routine). (2) **Assembler routines** — COBSWAIT calls MVSWAIT (assembler wait). (3) **JCL utilities** — IEBGENER (file copy), IDCAMS (VSAM management), SORT (hardware-accelerated sort), DSNTIAUL (DB2 unload). (4) **CICS services** — EXEC CICS ASKTIME/FORMATTIME (CODATE01), EXEC CICS WRITEQ TD (transient data queues in CORPT00C), EXEC CICS INQUIRE PROGRAM (dynamic program existence check in COMEN01C). (5) **TXT2PDF** — TXT2PDF1.JCL uses IKJEFT1B with TXT2PDF REXX exec (TSO/ISPF utility) to convert text to PDF. |
| **Likelihood** | **Medium (2)** — Most of these have well-known Java equivalents (java.time for dates, Thread.sleep for waits, Spring Batch for file processing, Apache FOP for PDF). However, the exact behavior of CEEDAYS (Lillian date format, specific error codes) and COBDATFT (assembler date formatting) may have subtle differences from Java date libraries. |
| **Impact** | **Moderate (2)** — These are utility-level dependencies, not core business logic. Behavioral differences are typically caught by unit testing. |
| **Risk Score** | **4 (Medium)** |
| **Affected Phases** | All phases |

#### Mitigation Strategy

| Action | Owner | When |
|--------|-------|------|
| **Runtime dependency inventory** — Catalog every non-COBOL runtime call (LE APIs, assembler routines, JCL utilities, CICS services) with its Java equivalent and any behavioral differences. | Architect | Pre-Phase 1 |
| **CEEDAYS compatibility test** — Build a test suite that passes every date format combination through both CEEDAYS (on the mainframe) and `java.time.LocalDate.parse()`. Verify that valid/invalid date classification is identical. | Developer | Pre-Phase 2 |
| **JCL SORT replacement** — Verify that Java in-process sorting (Collections.sort or database ORDER BY) produces identical ordering to the JCL SORT control cards specified in POSTTRAN.jcl and TRANREPT.jcl. Pay attention to EBCDIC vs. ASCII collation sequence differences. | Developer | Pre-Phase 5 |
| **PDF generation validation** — Compare PDF output from Apache FOP / iText with TXT2PDF REXX output for a sample statement. Verify formatting, page breaks, and character rendering. | QA Lead | Phase 6 |

#### Early Warning Indicators

- CEEDAYS and java.time disagree on the validity of any date string.
- JCL SORT produces different record ordering than Java sort for records with special characters or mixed-case keys.
- CEE3ABD abend handling reveals error recovery paths in COBOL programs that are not replicated in Java exception handlers.
- TXT2PDF produces materially different page layout than Apache FOP for the same input text.

---

## Risk Heat Map

```
                         IMPACT
              Low(1)   Med(2)   Major(3)  Critical(4)
           ┌────────┬────────┬─────────┬──────────┐
Critical(4)│        │        │         │          │
           ├────────┼────────┼─────────┼──────────┤
  High  (3)│        │ RISK-04│ RISK-01 │          │
           │        │        │ RISK-03 │          │
           │        │        │ RISK-07 │          │
L ─────────┼────────┼────────┼─────────┼──────────┤
I Medium(2)│        │ RISK-08│ RISK-05 │ RISK-02  │
K          │        │ RISK-10│ RISK-09 │ RISK-06  │
E ─────────┼────────┼────────┼─────────┼──────────┤
L  Low  (1)│        │        │         │          │
I          │        │        │         │          │
H ─────────┴────────┴────────┴─────────┴──────────┘
O
O
D
```

### Highest Priority Risks (Score >= 8)

1. **RISK-01 (Score 9):** Hidden business rules in COACTUPC — requires dedicated rule extraction sprint.
2. **RISK-03 (Score 9):** Cross-reference coupling — requires single source of truth strategy and automated consistency checks.
3. **RISK-07 (Score 9):** Test data representativeness — requires production-representative dataset creation.
4. **RISK-02 (Score 8):** VSAM-to-RDBMS data fidelity — requires field-level mapping and round-trip verification.
5. **RISK-06 (Score 8):** Financial calculation precision — requires arithmetic tracing and historical replay with zero tolerance.

---

## Risk Monitoring Schedule

| Frequency | Activity | Risks Monitored |
|-----------|----------|----------------|
| **Daily** (during active phase) | Parallel-run comparison — check COBOL vs. Java output divergence | RISK-01, RISK-02, RISK-06 |
| **Daily** (during active phase) | CCXREF consistency check — VSAM vs. PostgreSQL | RISK-03 |
| **Weekly** | Batch performance benchmark — measure Spring Batch execution times | RISK-09 |
| **Weekly** | Copybook-to-entity schema comparison (CI check) | RISK-08 |
| **Per-sprint** | Test coverage review — verify edge cases covered | RISK-07 |
| **Per-phase** | Runtime dependency compatibility test suite | RISK-04, RISK-10 |
| **Pre-phase** | Risk score reassessment — update likelihood/impact based on new information | All risks |
| **Monthly** | Stakeholder risk review — present updated risk register to project sponsor | All risks |
