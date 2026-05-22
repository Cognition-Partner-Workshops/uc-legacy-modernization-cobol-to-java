# CardDemo Modernization Risk Register

## Overview

This register identifies the top risks associated with modernizing the CardDemo mainframe credit card management system from COBOL/CICS/VSAM to Java/Spring Boot/RDBMS. Each risk is categorized, scored, and paired with mitigation strategies.

**Risk Scoring**: Likelihood (1–5) x Impact (1–5) = Risk Score (1–25)

| Score Range | Level |
|:------------|:------|
| 1–5 | Low |
| 6–12 | Medium |
| 13–19 | High |
| 20–25 | Critical |

---

## Risk Summary Matrix

| ID | Risk | Category | L | I | Score | Level | Phase |
|:---|:-----|:---------|:-:|:-:|:-----:|:-----:|:-----:|
| R-01 | COMP-3 / Packed Decimal Arithmetic Fidelity | Financial | 4 | 5 | **20** | Critical | 4 |
| R-02 | ACCTDATA Balance Write Contention | Data Integrity | 3 | 5 | **15** | High | 3–4 |
| R-03 | XREF Cross-Reference Migration Cascading Failures | Integration | 3 | 5 | **15** | High | 2 |
| R-04 | COACTUPC Validation Logic Loss (4,236 LOC) | Business Logic | 3 | 4 | **12** | Medium | 3 |
| R-05 | EBCDIC-to-UTF-8 Data Conversion Errors | Data Migration | 3 | 4 | **12** | Medium | 0 |
| R-06 | Batch Window Overrun During Parallel Run | Operational | 3 | 3 | **9** | Medium | 4 |
| R-07 | Mainframe Skills Attrition | People | 4 | 3 | **12** | Medium | All |
| R-08 | GDG-to-Modern Storage Mapping Gaps | Technical | 2 | 3 | **6** | Medium | 4 |
| R-09 | CICS Pseudo-Conversational State Loss | Technical | 3 | 3 | **9** | Medium | 3 |
| R-10 | IMS DL/I to Relational Impedance Mismatch | Technical | 3 | 4 | **12** | Medium | 5 |
| R-11 | Control-M to Modern Scheduler Feature Gap | Operational | 2 | 3 | **6** | Medium | 4 |
| R-12 | MQ-to-Kafka Message Ordering Guarantees | Integration | 3 | 4 | **12** | Medium | 5 |
| R-13 | Dual-Write Consistency During Transition | Data Integrity | 4 | 4 | **16** | High | 2–4 |
| R-14 | PII Exposure During Data Migration | Security | 2 | 5 | **10** | Medium | 0–2 |
| R-15 | Incomplete Test Coverage for Legacy Logic | Quality | 4 | 4 | **16** | High | All |
| R-16 | Vendor Lock-in on Cloud Platform | Strategic | 2 | 3 | **6** | Medium | 0 |
| R-17 | Scope Creep from Optional Module Dependencies | Project | 3 | 3 | **9** | Medium | All |
| R-18 | Performance Regression in Batch Processing | Performance | 3 | 4 | **12** | Medium | 4 |

---

## Detailed Risk Analysis

### R-01: COMP-3 / Packed Decimal Arithmetic Fidelity

**Category**: Financial Accuracy
**Likelihood**: 4 (High) | **Impact**: 5 (Critical) | **Score**: 20 — Critical
**Phase**: 4 (Interest Calculation)

**Description**: The interest calculation program (`CBACT04C`, 652 LOC) and transaction posting (`CBTRN02C`, 731 LOC) use COMP-3 (packed decimal) arithmetic for financial calculations. COBOL packed decimal arithmetic uses different rounding rules than Java floating-point. Even `BigDecimal` requires explicit rounding mode configuration. A single rounding discrepancy at scale could produce material financial variances.

**Affected Components**:
- `CBACT04C`: Interest calculation with `PIC S9(9)V99 COMP-3` variables
- `CBTRN02C`: Balance updates with `PIC S9(10)V99` fields
- `CVEXPORT.cpy`: Export uses mixed COMP/COMP-3 fields (`EXP-ACCT-CURR-BAL PIC S9(10)V99 COMP-3`)

**Mitigations**:
1. **Map all COBOL numeric types to explicit `BigDecimal` configurations**:
   - `PIC S9(n)V99` → `BigDecimal` with scale=2
   - `COMP-3` → `BigDecimal` with `RoundingMode.HALF_EVEN` (banker's rounding, matches mainframe `ROUNDED` default)
   - `COMP` (binary) → `BigDecimal` (avoid Java `int`/`long` for financial values)
2. **Create a COBOL-to-Java arithmetic test harness**: Feed identical inputs to both COBOL and Java programs, compare outputs to the cent for 100% of test accounts.
3. **Run parallel interest calculations for 3 consecutive months** before cutover. Report any discrepancy > $0.00.
4. **Document every COBOL ROUNDED clause** in the codebase; ensure each has an equivalent Java rounding mode.

**Residual Risk**: Medium — after mitigation, edge-case rounding differences may persist for very large balances. Acceptable if discrepancy < $0.01 per account per cycle.

---

### R-02: ACCTDATA Balance Write Contention

**Category**: Data Integrity
**Likelihood**: 3 (Medium) | **Impact**: 5 (Critical) | **Score**: 15 — High
**Phase**: 3–4

**Description**: Four bounded contexts write to `ACCT-CURR-BAL` in the `ACCTDATA` VSAM file: Transaction Posting (`CBTRN02C`), Interest Calculation (`CBACT04C`), Bill Payment (`COBIL00C`), and Account Update (`COACTUPC`). On the mainframe, CICS ENQUEUE and VSAM record-level locking prevent concurrent updates. In the modernized system, replacing VSAM with an RDBMS requires a new concurrency model.

**Mitigations**:
1. **Implement optimistic locking** on the `accounts` table using a `version` column. All balance-update operations must include the version in their UPDATE WHERE clause.
2. **Centralize balance mutations** through the Account Service API `POST /accounts/{id}/adjustments` endpoint. No direct SQL UPDATE to the balance column from outside the Account Service.
3. **Use database transactions** with `SERIALIZABLE` isolation for the batch posting job to prevent phantom reads during balance updates.
4. **Implement retry logic** with exponistic backoff for optimistic lock failures in the Payment Service.
5. **Monitor**: Add alerting for optimistic lock retry rates exceeding 1%.

**Residual Risk**: Low — optimistic locking with centralized mutations is a well-understood pattern.

---

### R-03: XREF Cross-Reference Migration Cascading Failures

**Category**: Integration
**Likelihood**: 3 (Medium) | **Impact**: 5 (Critical) | **Score**: 15 — High
**Phase**: 2

**Description**: The `CARDXREF` VSAM file (copybook `CVACT03Y`) is the most heavily shared data structure in the system. It is read by 6+ programs across 4 bounded contexts. The VSAM file has both a primary key (`XREF-CARD-NUM`) and an alternate index (`XREF-ACCT-ID`). Migration of this file to an API affects every downstream consumer simultaneously. A bug in the XREF API could cascade to transaction posting, interest calculation, bill payment, reporting, and data migration.

**Mitigations**:
1. **Shadow reads**: Before cutover, deploy the XREF API alongside VSAM. Have the ACL call both and compare results for every read. Log discrepancies. Run for 2 weeks minimum.
2. **Feature flag**: Use a feature flag to switch between VSAM XREF and API XREF per-program. Roll out one consumer at a time, not all at once.
3. **Cache**: Implement a read-through cache (Redis) in front of the XREF API. The XREF data is relatively static (cards are not frequently created/deleted), so caching is safe.
4. **Fallback**: Maintain VSAM XREF as read-only fallback for 1 phase after migration. ACL falls back to VSAM if API is unavailable.
5. **Data validation**: After initial XREF migration, run a full comparison of all ~N records in VSAM vs. PostgreSQL. Zero discrepancies required before any consumer cutover.

**Residual Risk**: Medium — even with mitigations, the breadth of impact makes this the riskiest single migration step.

---

### R-04: COACTUPC Validation Logic Loss (4,236 LOC)

**Category**: Business Logic Preservation
**Likelihood**: 3 (Medium) | **Impact**: 4 (High) | **Score**: 12 — Medium
**Phase**: 3

**Description**: `COACTUPC` is the largest online program (4,236 LOC) with extensive field-level validation: US phone number format (parentheses + dashes), state code validation, yes/no field checks, mandatory field enforcement, signed number validation, and alphanumeric-only checks. These validation rules are embedded in COBOL EVALUATE/IF statements with no external specification. Loss of any validation rule could allow invalid data into the account master.

**Mitigations**:
1. **Extract a validation specification document** from the COBOL code before writing any Java. Catalog every validation rule with: field name, rule type, error message, and COBOL line reference.
2. **Create a comprehensive test suite** of valid and invalid inputs for each field before migrating. Target 100% branch coverage of validation paths.
3. **Implement validations using Java Bean Validation (JSR 380)** with custom validators for mainframe-specific patterns (e.g., US phone `(NNN)NNN-NNNN`).
4. **Parallel testing**: Submit identical account update requests to both CICS and the new API. Compare acceptance/rejection decisions.
5. **Assign a dedicated COBOL SME** to review the Java validation implementation line-by-line against the COBOL source.

**Residual Risk**: Low — with comprehensive test suite and parallel testing, validation parity is achievable.

---

### R-05: EBCDIC-to-UTF-8 Data Conversion Errors

**Category**: Data Migration
**Likelihood**: 3 (Medium) | **Impact**: 4 (High) | **Score**: 12 — Medium
**Phase**: 0

**Description**: The `app/data/EBCDIC/` directory contains 12+ production data files in EBCDIC encoding. COMP-3 packed decimal fields, signed zoned decimal fields, and binary COMP fields must be correctly interpreted during conversion. ASCII versions exist in `app/data/ASCII/` for some files, but the canonical data is EBCDIC. Incorrect codepage mapping or packed decimal unpacking can produce corrupted numeric data.

**Mitigations**:
1. **Use a proven EBCDIC conversion library** (e.g., JRecord, IBM JZOS, or Micro Focus). Do not write custom conversion code.
2. **Validate converted data** by comparing against the existing ASCII files in `app/data/ASCII/`. These serve as a ground truth for: `acctdata`, `carddata`, `cardxref`, `custdata`, `dailytran`, `discgrp`, `tcatbal`, `trancatg`, `trantype`.
3. **Special attention to COMP-3 fields** in `CVEXPORT.cpy`:
   - `EXP-CUST-ID PIC 9(09) COMP` → 4-byte binary
   - `EXP-ACCT-CURR-BAL PIC S9(10)V99 COMP-3` → 7-byte packed decimal
   - `EXP-CUST-FICO-CREDIT-SCORE PIC 9(03) COMP-3` → 2-byte packed decimal
4. **Record-level checksums**: After conversion, compute checksums per record and verify against a COBOL-side checksum program.

**Residual Risk**: Low — with ground-truth ASCII files and proven conversion libraries.

---

### R-06: Batch Window Overrun During Parallel Run

**Category**: Operational
**Likelihood**: 3 (Medium) | **Impact**: 3 (Medium) | **Score**: 9 — Medium
**Phase**: 4

**Description**: During Phase 4 reconciliation, both the mainframe batch (POSTTRAN/INTCALC via Control-M) and the new Spring Batch jobs must complete within the daily/monthly batch window. Running both doubles the processing time and I/O load. The current Control-M daily window is CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL, which must complete before CICS files reopen.

**Mitigations**:
1. **Run new batch first**, then mainframe batch, with a comparison step in between. The new batch runs against a snapshot of the RDBMS; the mainframe batch runs against VSAM.
2. **Extend the batch window** during reconciliation months. Coordinate with operations to delay CICS file open if needed.
3. **Optimize new batch**: Use Spring Batch partitioning to parallelize across accounts. The mainframe COBOL is sequential; the new batch can be parallel.
4. **Reduce reconciliation data volume**: Sample-based reconciliation (random 10% of accounts) for the first month; full reconciliation for the final month.

**Residual Risk**: Low — modern hardware + parallel processing should outperform sequential COBOL batch.

---

### R-07: Mainframe Skills Attrition

**Category**: People / Knowledge
**Likelihood**: 4 (High) | **Impact**: 3 (Medium) | **Score**: 12 — Medium
**Phase**: All

**Description**: COBOL/CICS/VSAM expertise is scarce and aging. The migration depends on COBOL SMEs to validate that Java implementations match original business logic. If key mainframe personnel leave or become unavailable during the 18–24 month migration, knowledge gaps could delay or compromise the migration.

**Mitigations**:
1. **Knowledge capture first**: Before any coding begins, have COBOL SMEs document every business rule, especially in the high-complexity programs: `COACTUPC` (4,236 LOC), `CBTRN02C` (731 LOC), `CBACT04C` (652 LOC).
2. **Pair programming**: Pair COBOL SMEs with Java developers during Phases 3–4. Knowledge transfer happens through collaboration, not documentation alone.
3. **Retain COBOL SMEs as consultants** through Phase 5 even if they transition off the full-time team.
4. **Automated code analysis**: Use tools (SonarQube COBOL, Micro Focus Enterprise Analyzer, or AWS Mainframe Modernization) to generate call graphs, data flow diagrams, and dead code reports as supplementary documentation.
5. **Record video walkthroughs** of critical batch jobs and their Control-M orchestration.

**Residual Risk**: Medium — knowledge capture mitigates but cannot fully eliminate dependency on scarce expertise.

---

### R-08: GDG-to-Modern Storage Mapping Gaps

**Category**: Technical
**Likelihood**: 2 (Low) | **Impact**: 3 (Medium) | **Score**: 6 — Medium
**Phase**: 4

**Description**: Generation Data Groups (GDGs) provide automatic versioning of sequential datasets. `DALYREJS` (rejected transactions) uses GDG for daily versioned output (`DSN=AWS.M2.CARDDEMO.DALYREJS(+1)`). The `(+1)` notation automatically creates a new generation. Modern platforms lack a native GDG equivalent.

**Mitigations**:
1. **Replace GDGs with timestamped object storage** (S3 with prefixes: `rejected-transactions/2026/05/22/dalyrejs.dat`).
2. **Implement a retention policy**: GDG base definitions include maximum generation counts (`DEFGDGB` JCL). Map this to S3 lifecycle rules or database partition expiry.
3. **For audit purposes**: store rejection records in a database table with date partitioning, which provides better queryability than sequential files.

**Residual Risk**: Low — straightforward mapping with modern equivalents.

---

### R-09: CICS Pseudo-Conversational State Loss

**Category**: Technical
**Likelihood**: 3 (Medium) | **Impact**: 3 (Medium) | **Score**: 9 — Medium
**Phase**: 3

**Description**: CICS programs use pseudo-conversational design: the program returns control to CICS after each screen interaction, passing state via COMMAREA. The `CARDDEMO-COMMAREA` (copybook `COCOM01Y`) carries user context, program flow (`CDEMO-FROM-PROGRAM`, `CDEMO-TO-PROGRAM`), and business data (`CDEMO-ACCT-ID`, `CDEMO-CARD-NUM`) between interactions. Translating this stateful flow to a stateless REST API requires careful session/state management.

**Mitigations**:
1. **Use client-side state** (SPA holds account/card context in local state/URL parameters). The COMMAREA data is read-only context, not server-side session.
2. **For multi-step operations** (e.g., bill payment confirmation): use server-side state tokens or database-backed workflow state, not HTTP sessions.
3. **Map `CDEMO-PGM-CONTEXT` (ENTER=0, REENTER=1)** to standard HTTP semantics: initial GET = enter, subsequent POST = reenter.
4. **Test each transaction flow** end-to-end: sign-on → menu → function → return to menu → sign-off.

**Residual Risk**: Low — SPA + REST is a well-established replacement for pseudo-conversational patterns.

---

### R-10: IMS DL/I to Relational Impedance Mismatch

**Category**: Technical
**Likelihood**: 3 (Medium) | **Impact**: 4 (High) | **Score**: 12 — Medium
**Phase**: 5

**Description**: The authorization module uses IMS hierarchical databases (`DBPAUTP0`, `DBPAUTX0`) with DL/I segment-based access patterns (GU, GN, GNP, ISRT, REPL, DLET). The hierarchical parent-child relationships in IMS do not map 1:1 to relational tables. The PSB/DBD definitions (`PSBPAUTB`, `DLIGSAMP`, etc.) define the logical view of the data.

**Mitigations**:
1. **Analyze the IMS DBDs** to understand the segment hierarchy. Map parent segments to tables with foreign keys to child segment tables.
2. **Use Spring Data JPA** with `@OneToMany` / `@ManyToOne` relationships to model the hierarchy.
3. **Map DL/I calls to SQL**: GU (Get Unique) → SELECT with WHERE, GN (Get Next) → cursor/pagination, ISRT → INSERT, REPL → UPDATE, DLET → DELETE.
4. **Validate with IMS unload utility** (`PAUDBUNL`/`DBUNLDGS`): unload IMS segments to flat file, load into PostgreSQL, compare record counts and field values.

**Residual Risk**: Medium — IMS hierarchical patterns may have implicit ordering assumptions that are lost in relational mapping.

---

### R-11: Control-M to Modern Scheduler Feature Gap

**Category**: Operational
**Likelihood**: 2 (Low) | **Impact**: 3 (Medium) | **Score**: 6 — Medium
**Phase**: 4

**Description**: Control-M provides INCOND/OUTCOND-based job dependencies, MAXRERUN (5 retries), MAXWAIT (7 days), Smart Folders, and calendar-based scheduling (DAILY, WEEKLY-Saturday, MONTHLY). Modern schedulers (Airflow, Spring Cloud Data Flow) have equivalent capabilities but may require different configuration patterns.

**Mitigations**:
1. **Map Control-M patterns to target scheduler**:
   - `INCOND/OUTCOND` → Airflow task dependencies / Spring Batch step flow
   - `MAXRERUN=5` → retry policy with `maxAttempts=5`
   - Smart Folders → Airflow DAGs or Spring Batch partitioned steps
   - Calendar scheduling → cron expressions
2. **Preserve the exact job dependency chain** from `CardDemo.controlm`:
   - Daily: CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL
   - Weekly: MNTTRDB2 → CLOSEFIL → DISCGRP → WAITSTEP → OPENFIL; TRANEXTR
   - Monthly: CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL
3. **Add monitoring dashboards** that replicate Control-M job status views.

**Residual Risk**: Low — well-understood migration path.

---

### R-12: MQ-to-Kafka Message Ordering Guarantees

**Category**: Integration
**Likelihood**: 3 (Medium) | **Impact**: 4 (High) | **Score**: 12 — Medium
**Phase**: 5

**Description**: The authorization module uses IBM MQ for request/response authorization processing (`COPAUA0C`). MQ provides guaranteed delivery and FIFO ordering. Kafka provides ordering only within a partition. If authorization requests are processed out of order, duplicate or conflicting authorization decisions could result.

**Mitigations**:
1. **Partition Kafka topic by card number**: All authorization requests for the same card go to the same partition, preserving per-card ordering.
2. **Implement idempotency**: Authorization service checks for duplicate request IDs before processing.
3. **Use request-reply pattern**: Kafka Streams or a correlation-ID-based reply topic for synchronous-like authorization decisions.
4. **Alternative**: Use Amazon SQS FIFO queues if strict global ordering is required (simpler than Kafka partitioning for this use case).

**Residual Risk**: Low — with per-card partitioning and idempotency.

---

### R-13: Dual-Write Consistency During Transition

**Category**: Data Integrity
**Likelihood**: 4 (High) | **Impact**: 4 (High) | **Score**: 16 — High
**Phase**: 2–4

**Description**: During the transition period, some programs read/write VSAM while others use the new RDBMS. The ACL must keep both stores in sync. Any failure in the dual-write path creates data divergence that can corrupt balances, transaction records, or customer data.

**Mitigations**:
1. **Write to RDBMS first** (source of truth), then replicate to VSAM via ACL. Never the reverse.
2. **Implement write-ahead logging** in the ACL: log every write before applying to VSAM. If VSAM write fails, retry from log.
3. **Nightly reconciliation job**: Compare all records in RDBMS and VSAM; flag discrepancies. Automatically heal minor discrepancies (e.g., timestamp format differences). Escalate material discrepancies (balance differences) to operations.
4. **Minimize dual-write duration**: Aggressively migrate consumers off VSAM to reduce the window. Target < 3 months per dataset.
5. **Circuit breaker**: If VSAM writes fail repeatedly, stop VSAM writes and operate RDBMS-only with manual VSAM catch-up.

**Residual Risk**: Medium — dual-write is inherently complex; minimize the duration.

---

### R-14: PII Exposure During Data Migration

**Category**: Security / Compliance
**Likelihood**: 2 (Low) | **Impact**: 5 (Critical) | **Score**: 10 — Medium
**Phase**: 0–2

**Description**: The `CUSTDATA` VSAM file (copybook `CVCUS01Y`) contains highly sensitive PII: SSN (`CUST-SSN`), government-issued ID (`CUST-GOVT-ISSUED-ID`), date of birth (`CUST-DOB-YYYY-MM-DD`), phone numbers, and full addresses. The `USRSEC` file contains plaintext passwords. Data migration must handle these fields securely.

**Mitigations**:
1. **Encrypt PII at rest** in the target PostgreSQL database using column-level encryption (pgcrypto) or application-level encryption for SSN, government ID, and DOB.
2. **Hash passwords** during migration: convert plaintext passwords from USRSEC to bcrypt hashes. No plaintext passwords in the new system.
3. **Use encrypted transfer channels** (TLS) for all data movement between mainframe and cloud.
4. **Data masking** in non-production environments. Never use real PII in dev/test.
5. **Audit logging**: Log all access to PII fields in the new system. Comply with applicable data protection regulations (PCI-DSS for card numbers, SOX for financial data).
6. **Tokenize card numbers** (`CARD-NUM`, `XREF-CARD-NUM`) in the new system where possible.

**Residual Risk**: Low — with encryption, hashing, and audit logging.

---

### R-15: Incomplete Test Coverage for Legacy Logic

**Category**: Quality Assurance
**Likelihood**: 4 (High) | **Impact**: 4 (High) | **Score**: 16 — High
**Phase**: All

**Description**: The legacy COBOL system has no automated test suite. All validation of migrated code depends on tests created during the migration. If test coverage is insufficient, subtle bugs in validation logic, rounding, or edge cases may escape to production.

**Mitigations**:
1. **Test-first migration**: For each program, create the test suite **before** writing Java code. Use the COBOL program as a black-box specification.
2. **Generate test data from production VSAM files**: Use anonymized snapshots of ACCTDATA, CARDDATA, CUSTDATA, TRANSACT to create realistic test scenarios.
3. **Target coverage thresholds**:
   - Financial calculations (FA-6, FA-7): 100% line + branch coverage
   - Account/Card update validation (FA-3, FA-4): 95% branch coverage
   - Online CRUD (FA-5, FA-9, FA-10): 90% line coverage
   - Reporting/Utilities: 80% line coverage
4. **Automated regression suite**: Run the full test suite on every commit. Gate PRs on test pass.
5. **Golden file testing**: For batch programs, capture mainframe output as golden files. Assert Java output matches byte-for-byte (for reports) or field-for-field (for data files).

**Residual Risk**: Medium — test coverage is never complete, but the mitigations significantly reduce risk.

---

### R-16: Vendor Lock-in on Cloud Platform

**Category**: Strategic
**Likelihood**: 2 (Low) | **Impact**: 3 (Medium) | **Score**: 6 — Medium
**Phase**: 0

**Description**: Choosing AWS-specific services (RDS, SQS, Step Functions, Glue) for the target platform creates vendor dependency. Future cloud strategy changes would require re-migration.

**Mitigations**:
1. **Use portable abstractions**: Spring Boot + Spring Data JPA (not AWS SDK directly for data access), Spring Cloud Stream (abstracts Kafka/SQS), Flyway (database-agnostic migrations).
2. **Containerize everything**: Docker/Kubernetes-based deployment allows portability across AWS EKS, Azure AKS, GCP GKE.
3. **Avoid proprietary services** where open-source alternatives exist: PostgreSQL over Aurora, Kafka over Kinesis, Airflow over Step Functions.
4. **Document cloud-specific decisions** so they can be evaluated during future architecture reviews.

**Residual Risk**: Low — Spring Boot ecosystem is inherently portable.

---

### R-17: Scope Creep from Optional Module Dependencies

**Category**: Project Management
**Likelihood**: 3 (Medium) | **Impact**: 3 (Medium) | **Score**: 9 — Medium
**Phase**: All

**Description**: The CardDemo system has optional modules (IMS/DB2/MQ authorization, DB2 transaction type management, MQ account extractions) that are not strictly part of the core system. These modules introduce IMS, DB2, and MQ dependencies that significantly increase migration complexity. If included in scope, they could delay core migration.

**Mitigations**:
1. **Defer optional modules to Phase 5**: Core migration (Phases 0–4) does not depend on optional modules. Migrate them last.
2. **Feature-flag optional functionality**: In the new system, authorization and DB2-based transaction type management are behind feature flags. Deploy without them, enable later.
3. **Establish clear scope gates**: Phase exits require sign-off that no optional module work has crept into core migration.
4. **If optional modules are not in production use**: Consider retiring them instead of migrating. Validate usage with stakeholders.

**Residual Risk**: Low — with strict phase gating.

---

### R-18: Performance Regression in Batch Processing

**Category**: Performance
**Likelihood**: 3 (Medium) | **Impact**: 4 (High) | **Score**: 12 — Medium
**Phase**: 4

**Description**: COBOL batch programs are optimized for sequential file I/O on mainframe hardware with dedicated I/O channels. The new Spring Batch jobs run against an RDBMS, which has different I/O characteristics (random access, network latency, connection pooling). Large daily transaction files (DALYTRAN) may process slower via SQL than via VSAM sequential reads.

**Mitigations**:
1. **Use Spring Batch chunk-oriented processing** with optimal chunk sizes (tuned via performance testing; start with 1000).
2. **Bulk operations**: Use JDBC batch inserts/updates instead of row-by-row. Use `@BatchSize` annotations in JPA.
3. **Database indexing**: Ensure all primary/foreign keys and query predicates have indexes. The VSAM AIX pattern maps to database indexes.
4. **Connection pooling**: Use HikariCP with appropriate pool sizes.
5. **Partitioning**: Partition batch processing by account ID range. COBOL is sequential; Java can be parallel.
6. **Performance benchmarking**: Before cutover, run the new batch against production-volume data (or a representative subset). Must complete within 80% of the mainframe batch window.
7. **Fallback**: If performance is insufficient, consider using native SQL (stored procedures) for the most intensive operations instead of JPA.

**Residual Risk**: Low — modern hardware + parallel processing typically outperforms sequential COBOL.

---

## Risk Heat Map

```
Impact
  5 │  R-14          R-01
    │              R-02  R-03
  4 │  R-04  R-05  R-10  R-12  R-13  R-15  R-18
    │
  3 │  R-08  R-11  R-06  R-09  R-07  R-16  R-17
    │
  2 │
    │
  1 │
    └──────────────────────────────────────────────
       1         2         3         4         5
                       Likelihood
```

---

## Risk Review Schedule

| Review Type | Frequency | Participants |
|:------------|:----------|:-------------|
| Risk register review | Bi-weekly during active phases | PM, Tech Lead, COBOL SME |
| Financial reconciliation review | Weekly during Phase 4 | PM, DBA, Finance stakeholder |
| PII handling audit | Monthly during Phases 0–2 | Security team, DBA |
| Performance benchmark review | Weekly during Phase 4 | Tech Lead, DBA, DevOps |
| Scope review | At each phase gate | PM, Stakeholders |

---

## Risk Escalation Thresholds

| Trigger | Action |
|:--------|:-------|
| Any financial reconciliation discrepancy > $0.01 per account | Halt Phase 4 cutover; root cause analysis |
| XREF API error rate > 0.1% | Activate VSAM fallback; investigate |
| Dual-write discrepancy count > 10 records/day | Escalate to architecture review; consider pausing migration |
| Batch overrun > 120% of window | Escalate to operations; extend window or optimize |
| COBOL SME availability < 50% allocated time | Escalate to PM; consider schedule extension |
| Any PII exposure incident | Immediate security review; halt data migration until resolved |
