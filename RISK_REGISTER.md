# CardDemo Migration Risk Register

## Overview

This register documents the top 10 migration risks identified through analysis of the CardDemo COBOL application. Each risk includes likelihood, impact, mitigation strategy, and early warning indicators.

---

## Risk Summary

| # | Risk | Likelihood | Impact | Risk Score |
|---|---|---|---|---|
| R1 | Hidden business rules in validation logic | High | High | **Critical** |
| R2 | VSAM-to-RDBMS data coupling complexity | High | High | **Critical** |
| R3 | EBCDIC/COMP-3 data conversion errors | Medium | High | **High** |
| R4 | Batch pipeline timing and sequencing gaps | Medium | High | **High** |
| R5 | Mainframe-specific runtime behavior differences | Medium | High | **High** |
| R6 | Test data representativeness | High | Medium | **High** |
| R7 | CICS transaction semantics mismatch | Medium | Medium | **Medium** |
| R8 | Team COBOL skill availability | High | Medium | **High** |
| R9 | Cross-domain data consistency during migration | Medium | High | **High** |
| R10 | Legacy pattern complexity in statement generation | Low | Medium | **Medium** |

---

## Detailed Risk Analysis

### R1: Hidden Business Rules in Validation Logic

**Likelihood:** HIGH | **Impact:** HIGH

**Description:**
`COACTUPC` (4,236 lines) contains deeply embedded business rules that are not documented anywhere outside the code:
- SSN validation rejects specific prefixes (666, 900-999 range) — an IRS rule that must be preserved.
- Phone number parsing handles US format with specific area-code/exchange validations.
- Date validation includes leap-year calculations via the `CSUTLDWY` copybook (375 lines of date utility code).
- Account status transitions are guarded by conditions spread across multiple paragraphs.
- Credit limit changes have implicit ceiling/floor constraints baked into EVALUATE statements.

Similar tribal knowledge exists in:
- `CBTRN02C` (POSTTRAN): Transaction rejection rules based on card status + account status + limit checks.
- `CBACT04C`: Interest calculation rates selected by disclosure group + transaction category combination.
- `COBIL00C`: Bill payment amount constraints relative to account balance.

**Mitigation Strategy:**
1. **Code-level extraction:** Before rewriting, create a formal business rule catalog by systematically walking every EVALUATE, IF, and 88-level condition in the top-5 programs by LOC (`COACTUPC`, `COCRDUPC`, `COCRDLIC`, `COPAUS0C`, `CBTRN02C`).
2. **Characterization testing:** Run the legacy system with a comprehensive input matrix (boundary values, error paths) and capture actual outputs. Use these as golden-file tests for the new implementation.
3. **SME interviews:** If original developers or business analysts are available, validate the extracted rules.
4. **Parallel-run verification:** Run old and new systems side-by-side on production traffic (read-only for new system initially).

**Early Warning Indicators:**
- Rewritten service produces different outputs for the same inputs during characterization testing.
- Edge-case inputs (SSN=666000000, date=02/29/2025, negative balance) behave differently.
- Business users report "that's not how it used to work" during UAT.

---

### R2: VSAM-to-RDBMS Data Coupling Complexity

**Likelihood:** HIGH | **Impact:** HIGH

**Description:**
The CardDemo application uses 12+ VSAM KSDS files with alternate indexes (AIX). Several critical data coupling patterns make migration complex:

1. **Three-way XREF coupling:** `CVACT03Y` (Card XREF) is read by Account View (`COACTVWC`), Card List (`COCRDLIC`), Transaction Posting (`CBTRN02C`), and Interest Calculation (`CBACT04C`). Any change to XREF structure or ownership affects four domains.

2. **ACCTDAT dual-write:** Both Account Update (`COACTUPC`) and Transaction Posting (`CBTRN02C`) write to the same VSAM file. In the legacy system, this is safe because batch runs during the nightly window when CICS files are closed. In a microservice architecture, concurrent writes require distributed transactions or saga patterns.

3. **AIX dependencies:** `CARDAIX` and `CXACAIX` are alternate index paths over CARDDAT and CARDXREF. PostgreSQL indexes replicate this, but the VSAM STARTBR/READNEXT browse semantics (used in `COCRDLIC` for pagination) behave differently from SQL cursor pagination — VSAM browsing is position-based while SQL is value-based.

4. **File status codes:** VSAM returns specific status codes (00, 02, 10, 23, etc.) that programs branch on. The exact error-handling behavior must be replicated.

**Mitigation Strategy:**
1. **Schema design review:** Design the PostgreSQL schema before coding. Validate that all VSAM key structures and AIX paths have equivalent indexes.
2. **Data access layer abstraction:** Build a repository layer that abstracts VSAM-specific semantics (browse, key positioning) into standard query patterns.
3. **Shadow write pattern:** During Phase 3, write to both PostgreSQL and VSAM. Run nightly reconciliation to detect drift.
4. **Saga pattern for cross-service writes:** Replace ACCTDAT dual-write with API calls + compensating transactions.

**Early Warning Indicators:**
- Shadow write reconciliation reports non-zero discrepancies.
- Card list pagination returns different results or different ordering than legacy.
- Batch programs fail with unexpected file status codes when reading shadow-written VSAM files.
- Performance degradation when replacing direct VSAM reads with API calls.

---

### R3: EBCDIC/COMP-3 Data Conversion Errors

**Likelihood:** MEDIUM | **Impact:** HIGH

**Description:**
The application uses multiple numeric storage formats:
- **COMP (binary):** Used in `CVEXPORT` (e.g., `EXP-ACCT-ID PIC 9(11) COMP`, `EXP-TRAN-MERCHANT-ID PIC 9(09) COMP`).
- **COMP-3 (packed decimal):** Used in `CVEXPORT` (e.g., `EXP-ACCT-CURR-BAL PIC S9(10)V99 COMP-3`, `EXP-CUST-FICO-CREDIT-SCORE PIC 9(03) COMP-3`).
- **DISPLAY (zoned decimal):** Standard format in most copybooks (e.g., `ACCT-CURR-BAL PIC S9(10)V99`).

The EBCDIC data files in `app/data/EBCDIC/` must be converted correctly. Specific risks:
- Packed decimal sign nibble handling (C=positive, D=negative, F=unsigned).
- Half-byte boundary alignment in COMP-3 fields.
- FILLER bytes may contain non-space values in EBCDIC that corrupt if treated as ASCII.
- Implied decimal points (`V99`) have no physical representation — conversion must handle precision correctly.

**Mitigation Strategy:**
1. **Use proven conversion libraries:** JRecord, EBCDIC4J, or the AWS M2 conversion tools — not custom code.
2. **Round-trip validation:** Export all VSAM files, convert to ASCII/relational, convert back to EBCDIC, and binary-compare with originals.
3. **Sample data validation:** The repo includes both `app/data/ASCII/` and `app/data/EBCDIC/` directories. Use these as test fixtures for conversion validation.
4. **Decimal precision testing:** Verify that `PIC S9(10)V99` values survive conversion with exact cent-level precision.

**Early Warning Indicators:**
- Balance fields show incorrect values after conversion (especially negative balances).
- FICO scores (COMP-3 PIC 9(03)) decode to values outside 300-850 range.
- Export→Import round-trip produces different byte sequences.
- Hash/checksum mismatches in `CBIMPORT` validation logic.

---

### R4: Batch Pipeline Timing and Sequencing Gaps

**Likelihood:** MEDIUM | **Impact:** HIGH

**Description:**
The Control-M scheduler defines three critical job chains:

1. **DAILY-TransactionBackup:** `CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL`
2. **MONTHLY-InterestCalculation:** `CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL`
3. **WEEKLY-DisclosureGroupsRefresh:** `CLOSEFIL → DISCGRP → WAITSTEP → OPENFIL`

The CLOSEFIL/OPENFIL pattern is a mainframe-specific requirement: CICS VSAM files must be closed before batch programs can access them for update. This creates an implicit mutual exclusion that prevents concurrent online and batch access.

In a modern architecture, there is no equivalent of CLOSEFIL/OPENFIL. The new batch pipeline must handle concurrent read/write access to the database, which the legacy system never had to deal with.

Additional risks:
- The GDG (Generation Data Group) backup strategy creates versioned datasets automatically. Replacing this with S3 versioning requires explicit implementation.
- `WAITSTEP` calls an assembler program (`MVSWAIT`) that pauses execution — this is a mainframe-specific timing mechanism.
- Job dependencies in Control-M use condition variables (INCOND/OUTCOND) that must be mapped to Spring Batch flow control or Kubernetes job dependencies.

**Mitigation Strategy:**
1. **Database isolation levels:** Use PostgreSQL serializable isolation for balance-update transactions during batch runs.
2. **Batch window scheduling:** Initially, maintain a batch window where online writes are paused (feature flag). Relax this as confidence grows.
3. **Explicit backup steps:** Replace GDG with S3 versioned bucket; add explicit backup step at start of each batch job.
4. **Spring Batch flow:** Map Control-M INCOND/OUTCOND chains to Spring Batch step flows with conditional execution.
5. **Parallel run:** Run legacy and new batch concurrently for 30+ days before cutover.

**Early Warning Indicators:**
- Deadlocks or lock contention during batch runs.
- Missing backup versions when rollback is needed.
- Batch jobs completing in different order than expected.
- WAITSTEP equivalent (Thread.sleep or scheduler delay) causes downstream timing issues.

---

### R5: Mainframe-Specific Runtime Behavior Differences

**Likelihood:** MEDIUM | **Impact:** HIGH

**Description:**
Several COBOL constructs behave differently than their Java equivalents:

1. **COBOL arithmetic:** Fixed-point decimal with specific truncation/rounding rules. `PIC S9(10)V99` is NOT the same as `BigDecimal` without explicit scale/rounding configuration. Interest calculations in `CBACT04C` are sensitive to rounding order.

2. **ALTER/GO TO in CBSTM03A:** The statement generator uses `ALTER` to modify GO TO targets at runtime — a self-modifying code pattern with no direct equivalent in modern languages. The control flow must be reverse-engineered into standard conditionals.

3. **CICS pseudo-conversational model:** Programs use `EXEC CICS RETURN TRANSID(...)` to end processing and resume when the user presses Enter. This creates a stateless request-response pattern that maps well to REST, but the commarea state management is implicit.

4. **EXEC CICS XCTL (transfer control):** Programs hand off to each other without return. This is not a subroutine call — it's a full program replacement. In a web app, this maps to navigation/routing, but the data passing semantics (commarea) differ from HTTP session or JWT.

5. **VSAM STARTBR/READNEXT/READPREV:** Browse operations position a cursor in the key-ordered file. The cursor state is implicit and maintained by CICS. SQL pagination uses OFFSET/LIMIT or keyset pagination — different semantics for concurrent access.

6. **Internal reader (TDQ):** `CORPT00C` submits JCL to the internal reader to trigger batch jobs from online. This is a CICS→JES2 integration with no cloud equivalent; must be replaced with an API call or message.

**Mitigation Strategy:**
1. **Arithmetic precision:** Use `BigDecimal` with `ROUND_HALF_EVEN` (banker's rounding) and explicit scale matching COBOL PIC clauses. Create unit tests with known COBOL outputs.
2. **Control flow reverse-engineering:** For CBSTM03A, manually trace the ALTER/GO TO sequences and rewrite as state machine or strategy pattern.
3. **Commarea→DTO mapping:** Create a `CommareaDTO` that mirrors `COCOM01Y` field-by-field; use as the session context object passed between services.
4. **Cursor-based pagination:** Implement keyset pagination (WHERE key > last_key ORDER BY key LIMIT n) to match VSAM browse semantics.
5. **TDQ replacement:** `POST /reports/generate` endpoint triggers an async job (Spring Batch or message queue).

**Early Warning Indicators:**
- Financial calculations differ by ±$0.01 or more.
- Statement generator produces garbled or incorrectly formatted output.
- Pagination returns duplicate or missing records at page boundaries.
- Menu navigation breaks when commarea state is lost between screens/API calls.

---

### R6: Test Data Representativeness

**Likelihood:** HIGH | **Impact:** MEDIUM

**Description:**
The repository includes sample data files in `app/data/ASCII/` and `app/data/EBCDIC/`. These are small, demo-quality datasets that may not exercise:
- High cardinality (real production may have millions of accounts).
- Edge cases in business rules (dormant accounts, expired cards, negative balances, zero-balance interest calculations).
- Character encoding edge cases (names with accents, addresses with special characters).
- Concurrent access patterns (the sample data is designed for single-user demo).
- Date boundaries (year-end, month-end, leap years).
- All transaction type and category combinations.

If the migration is validated only against sample data, production edge cases will surface post-cutover.

**Mitigation Strategy:**
1. **Synthetic data generation:** Build a data generator that creates production-scale datasets (1M+ accounts, 10M+ transactions) with statistical distributions matching production.
2. **Boundary value injection:** Explicitly create test records for every boundary condition identified in R1 (SSN edge cases, leap year dates, max credit limits, zero balances).
3. **Production data sampling:** If possible, obtain an anonymized subset of production data for testing.
4. **Performance testing:** Load test with production-scale data to validate that pagination, batch processing, and report generation perform adequately.

**Early Warning Indicators:**
- Tests pass with sample data but fail with larger datasets.
- Performance degrades non-linearly with data volume.
- Encoding-related failures with real customer data (non-ASCII characters).
- Batch processing timeout with production-scale transaction volumes.

---

### R7: CICS Transaction Semantics Mismatch

**Likelihood:** MEDIUM | **Impact:** MEDIUM

**Description:**
CICS provides implicit transaction management:
- `EXEC CICS READ UPDATE` locks a record until `EXEC CICS REWRITE` or task end.
- `SYNCPOINT` provides explicit commit points.
- CICS pseudo-conversational model means the record lock is released between user interactions (the program returns to CICS between screen displays).

In a REST/microservice architecture:
- There is no implicit record locking between HTTP requests.
- Optimistic concurrency (version columns) must replace pessimistic VSAM locks.
- Long-running user editing sessions (view → modify → save) require conflict detection.

Specific risk: `COACTUPC` reads an account record, displays it for editing, then updates on the next interaction. In CICS, the record is not locked during the user think time (pseudo-conversational). But the program does a `READ UPDATE` on the save interaction, which fails if the record was modified by another user. This implicit conflict detection must be replicated.

**Mitigation Strategy:**
1. **Optimistic locking:** Add a `version` column to all migrated tables. Use JPA `@Version` annotation.
2. **Conflict detection:** On save, check that the version matches what was displayed. Return HTTP 409 Conflict if it doesn't.
3. **Idempotency:** Ensure all write operations are idempotent (retry-safe).
4. **Session timeout:** Match CICS timeout behavior (typically 30 minutes) with HTTP session or JWT expiry.

**Early Warning Indicators:**
- Lost updates: User A's changes silently overwritten by User B.
- Unexpected 409 Conflict errors in UAT.
- Data inconsistency between account balance and sum of transactions (caused by concurrent update race conditions).

---

### R8: Team COBOL Skill Availability

**Likelihood:** HIGH | **Impact:** MEDIUM

**Description:**
Understanding the existing COBOL code is essential for:
- Extracting business rules (R1).
- Verifying data conversion correctness (R3).
- Validating that the new system behaves identically to the old one.
- Maintaining the legacy system during the 11-month migration.

COBOL developers are increasingly scarce. The CardDemo application uses:
- CICS BMS programming (specialized skill).
- VSAM file handling with alternate indexes.
- IMS DB/DC programming (optional module).
- JCL with IDCAMS/IEBGENER utilities.
- Control-M scheduling.
- Assembler programs (`MVSWAIT`, `COBDATFT`).

If COBOL-skilled resources leave or are unavailable during migration, the project loses the ability to validate correctness.

**Mitigation Strategy:**
1. **Knowledge capture first:** Before any code migration, document all business rules (R1 mitigation) and create characterization tests.
2. **Automated regression suite:** Build a comprehensive test suite against the legacy system so that future validation doesn't require manual COBOL expertise.
3. **Pair programming:** During Phases 1-3, pair a COBOL developer with a Java developer for each program being rewritten.
4. **Cross-training:** Train Java developers on COBOL basics (enough to read code, not write it).
5. **Contract contingency:** Identify COBOL consulting firms as backup resources.

**Early Warning Indicators:**
- COBOL team members give resignation notice.
- Business rule extraction falling behind schedule.
- Characterization tests not keeping pace with rewrite progress.
- Increasing questions from Java team about COBOL behavior that can't be answered.

---

### R9: Cross-Domain Data Consistency During Migration

**Likelihood:** MEDIUM | **Impact:** HIGH

**Description:**
During Phase 3 (Account + Card migration), the system will be in a hybrid state:
- Account and Card data live in PostgreSQL (new) with VSAM shadow writes (legacy).
- Transaction data still lives in VSAM only (migrated in Phase 4).
- Batch programs (`CBTRN02C`) still run against VSAM files.

This creates a consistency window where:
1. An account balance is updated via the new API (PostgreSQL first, then shadow-written to VSAM).
2. Before the shadow write completes, the batch POSTTRAN job reads the stale VSAM balance.
3. The batch job posts a transaction using the stale balance, potentially allowing an over-limit transaction.

Similar risks exist at every phase boundary where some domains are modernized and others are not.

**Mitigation Strategy:**
1. **Synchronous shadow writes:** Make the shadow write synchronous (not async) during the hybrid period. Accept the latency cost.
2. **Batch window coordination:** Schedule batch runs with a guaranteed quiesce period after the last shadow write.
3. **Reconciliation jobs:** Run nightly reconciliation between PostgreSQL and VSAM. Alert on any discrepancy.
4. **Circuit breaker:** If reconciliation finds discrepancies, halt batch processing until resolved.
5. **Minimize hybrid duration:** Keep Phase 3 and Phase 4 as close together as possible. Consider a combined Phase 3+4 if team capacity allows.

**Early Warning Indicators:**
- Reconciliation job reports growing number of mismatches.
- Shadow write latency exceeds SLA (>50ms P99).
- Batch jobs produce different results depending on when they run relative to online activity.
- Account balance in PostgreSQL differs from VSAM by more than the last transaction amount.

---

### R10: Legacy Pattern Complexity in Statement Generation

**Likelihood:** LOW | **Impact:** MEDIUM

**Description:**
`CBSTM03A` (924 lines) was intentionally written to exercise modernization tooling:
- **ALTER/GO TO:** Self-modifying control flow. The `ALTER` statement changes the target of a `GO TO` at runtime. This creates a dynamic dispatch pattern that is difficult for automated translation tools to handle.
- **CALL to subroutine:** `CBSTM03B` is called for file I/O, introducing inter-program coupling.
- **COMP and COMP-3 variables:** Mixed numeric formats in the same program.
- **2D arrays (OCCURS):** Used for statement line items.
- **Multi-format output:** Generates both plain text and HTML.

Automated COBOL-to-Java translation tools (AWS M2, Micro Focus, Blu Age) may produce incorrect or unreadable code for these patterns.

**Mitigation Strategy:**
1. **Manual rewrite:** For this specific program, prefer manual rewrite over automated translation. The ALTER/GO TO pattern must be manually reverse-engineered.
2. **State machine pattern:** Rewrite the dynamic dispatch as an explicit state machine or strategy pattern in Java.
3. **Output comparison:** Generate statements from both legacy and new systems for the same accounts. Diff the output character-by-character.
4. **Modern reporting library:** Use JasperReports or a templating engine (Thymeleaf for HTML) instead of line-by-line string building.

**Early Warning Indicators:**
- Automated translation tool produces code that doesn't compile or produces wrong output for ALTER/GO TO sections.
- Statement output formatting differences (column alignment, page breaks, date formats).
- HTML output structural differences (tag nesting, entity encoding).

---

## Risk Heat Map

```
          │ Low Impact  │ Med Impact  │ High Impact
──────────┼─────────────┼─────────────┼─────────────
High      │             │ R6, R8      │ R1, R2
Likelihood│             │             │
──────────┼─────────────┼─────────────┼─────────────
Medium    │             │ R7, R10     │ R3, R4, R5,
Likelihood│             │             │ R9
──────────┼─────────────┼─────────────┼─────────────
Low       │             │             │
Likelihood│             │             │
```

---

## Risk Monitoring Schedule

| Risk | Review Frequency | Responsible Role | Key Metric |
|---|---|---|---|
| R1 (Hidden rules) | Weekly | Business Analyst + COBOL Dev | % of programs with completed rule catalog |
| R2 (Data coupling) | Weekly | Data Architect | Shadow write reconciliation error count |
| R3 (EBCDIC/COMP-3) | Per conversion batch | Data Engineer | Round-trip conversion error count |
| R4 (Batch timing) | Daily during Phase 4 | DevOps Lead | Parallel-run discrepancy count |
| R5 (Runtime behavior) | Per sprint | Java Dev Lead | Characterization test pass rate |
| R6 (Test data) | Monthly | QA Lead | Code path coverage % |
| R7 (CICS semantics) | Per sprint | Java Dev Lead | Concurrent update conflict count |
| R8 (COBOL skills) | Monthly | Project Manager | COBOL team headcount + availability |
| R9 (Cross-domain consistency) | Daily during Phases 3-4 | Data Architect | PostgreSQL-VSAM reconciliation delta |
| R10 (Statement patterns) | Weekly during Phase 5A | Java Dev Lead | Statement output diff count |
