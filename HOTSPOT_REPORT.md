# CardDemo Hotspot Report — Top 10 Modules

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo — Mainframe Credit Card Management System
> **Methodology**: Composite scoring based on code complexity, migration risk, and business impact

---

## Scoring Methodology

Each module is scored 1–5 across three dimensions:

| Dimension         | Weight | Criteria                                                                 |
|-------------------|--------|--------------------------------------------------------------------------|
| **Complexity**    | 40%    | Lines of code, PERFORM count, EVALUATE nesting, number of copybook includes, CICS commands, file I/O operations |
| **Risk**          | 30%    | Data mutation (REWRITE/WRITE), financial calculations, security logic, cross-entity dependencies, error-handling gaps |
| **Business Impact** | 30%  | Revenue criticality, user-facing vs. batch, data integrity dependencies, downstream consumers |

**Composite Score** = (Complexity × 0.4) + (Risk × 0.3) + (Business Impact × 0.3)

---

## Top 10 Hotspot Ranking

| Rank | Module     | Complexity | Risk | Impact | **Composite** | Classification          |
|------|------------|:----------:|:----:|:------:|:---------:|-------------------------|
| 1    | COACTUPC   | 5          | 5    | 5      | **5.00**  | Online — Account Update |
| 2    | CBTRN02C   | 5          | 5    | 5      | **5.00**  | Batch — Transaction Posting |
| 3    | CBACT04C   | 4          | 5    | 5      | **4.60**  | Batch — Interest Calculation |
| 4    | COCRDLIC   | 5          | 3    | 4      | **4.10**  | Online — Card List      |
| 5    | COCRDUPC   | 5          | 4    | 3      | **4.10**  | Online — Card Update    |
| 6    | CBSTM03A   | 4          | 3    | 5      | **4.00**  | Batch — Statement Gen   |
| 7    | COACTVWC   | 4          | 3    | 4      | **3.70**  | Online — Account View   |
| 8    | COBIL00C   | 3          | 5    | 4      | **3.90**  | Online — Bill Payment   |
| 9    | CBTRN03C   | 4          | 2    | 4      | **3.40**  | Batch — Transaction Report |
| 10   | COTRN02C   | 4          | 4    | 3      | **3.70**  | Online — Transaction Add |

---

## Detailed Analysis

### #1 — COACTUPC (Account Update) — Score: 5.00

| Metric               | Value                      |
|-----------------------|----------------------------|
| Lines of Code         | **4,236** (largest program) |
| PERFORM Count         | 64                         |
| EVALUATE Blocks       | 8+ (nested)                |
| Copybook Includes     | 14 + 37× CSSETATY REPLACING|
| CICS Commands         | 15 (READ, REWRITE, SEND, RECEIVE, XCTL, HANDLE ABEND) |
| Files Accessed        | ACCTDATA, CUSTDATA, CARDXREF (READ + REWRITE) |

**Why it's #1**:
- **Complexity**: At 4,236 lines, this is the largest program in the entire codebase — over 2.5× the next largest. It uses 37 instances of `COPY CSSETATY REPLACING` for field-level attribute manipulation, deeply nested EVALUATE blocks, and extensive screen field validation logic.
- **Risk**: Performs REWRITE on both ACCTDATA (balances, limits, dates) and CUSTDATA (customer info). A bug here can corrupt financial records across the system. Uses `EXEC CICS HANDLE ABEND` for error recovery — complex to translate correctly.
- **Business Impact**: Account update is the central administrative operation. All balance adjustments, credit limit changes, and account status modifications flow through this program. Downstream batch processes (interest calculation, statement generation) depend on the data it writes.

**Migration Recommendations**:
1. Decompose into multiple Java services: AccountUpdateService, AccountValidationService, CustomerUpdateService
2. Extract the 37 CSSETATY REPLACING instances into a shared field-attribute utility
3. Replace CICS HANDLE ABEND with try-catch exception handling
4. Add database transaction boundaries (the original has no explicit commit — CICS handles it)
5. Implement comprehensive input validation (the COBOL version validates field-by-field across 2,000+ lines)

---

### #2 — CBTRN02C (Transaction Posting) — Score: 5.00

| Metric               | Value                      |
|-----------------------|----------------------------|
| Lines of Code         | 731                        |
| PERFORM Count         | 62                         |
| File I/O Operations   | 6 files (R/W across all)   |
| CALL Targets          | CSUTLDTC (date validation) |
| Data Mutations        | TRANSACT (WRITE), ACCTDATA (REWRITE), TCATBALF (WRITE+REWRITE), DALYREJS (WRITE) |

**Why it's #2**:
- **Complexity**: Accesses 6 VSAM files simultaneously with complex read-validate-update-write logic. Uses sequential file processing with multiple error paths and rejection handling. Contains 62 PERFORMs managing the full transaction lifecycle.
- **Risk**: This is the **core financial posting engine**. Every transaction in the system passes through this program. It updates account balances (ACCTDATA), writes to the transaction master (TRANSACT), updates category balances (TCATBALF), and writes rejected transactions (DALYREJS). A posting error has cascading financial impact.
- **Business Impact**: Transaction posting is the single most critical batch operation. Statement generation, interest calculation, and all reporting depend on correctly posted transactions. This program is the heart of the batch cycle (`POSTTRAN` job).

**Migration Recommendations**:
1. Implement as a Spring Batch job with chunk-oriented processing
2. Add explicit database transactions with rollback capability (the original relies on batch restart)
3. Implement a dead-letter queue pattern for rejected transactions (replacing DALYREJS flat file)
4. Add idempotency checks to prevent double-posting on restart
5. Create comprehensive audit logging (the original has minimal logging)

---

### #3 — CBACT04C (Interest Calculation) — Score: 4.60

| Metric               | Value                      |
|-----------------------|----------------------------|
| Lines of Code         | 652                        |
| PERFORM Count         | 57                         |
| File I/O Operations   | 5 files                    |
| CALL Targets          | CSUTLDTC (date validation) |
| Data Mutations        | ACCTDATA (REWRITE), TCATBALF (REWRITE) |

**Why it's #3**:
- **Complexity**: Reads from 5 VSAM files to calculate interest across transaction categories. Uses disclosure group rates (DISCGRP) with category-specific logic. Accepts a date parameter via JCL PARM for calculation cutoff. Contains packed decimal arithmetic (COMP-3) requiring precise conversion.
- **Risk**: **Financial calculation engine** — any error in interest computation directly impacts account balances and customer statements. Uses `COMP-3` (packed decimal) variables that require careful BigDecimal translation. Rate lookup across DISCGRP + TCATBAL + TRANSACT creates complex join logic.
- **Business Impact**: Interest calculation is a core financial function regulated by compliance requirements. Incorrect interest posting creates legal liability. This runs as part of the nightly batch cycle (`INTCALC` job) and its output feeds into statement generation.

**Migration Recommendations**:
1. Implement as a dedicated Spring Batch step with configurable rate tables
2. Use BigDecimal exclusively for all financial calculations (no floating-point)
3. Create a rate-lookup service that replaces the DISCGRP file scan
4. Add calculation audit trail for regulatory compliance
5. Implement reconciliation checks comparing pre/post calculation balances

---

### #4 — COCRDLIC (Card List) — Score: 4.10

| Metric               | Value                      |
|-----------------------|----------------------------|
| Lines of Code         | 1,459                      |
| PERFORM Count         | 34                         |
| CICS Commands         | 14 (STARTBR, READNEXT, READPREV, XCTL, SEND, RECEIVE) |
| Copybook Includes     | 10                         |

**Why it's #4**:
- **Complexity**: Implements paginated VSAM browsing with forward/backward navigation using STARTBR/READNEXT/READPREV — a pattern that requires careful cursor management. Contains 3 XCTL transfer targets. At 1,459 lines, it's a substantial UI program with complex screen handling.
- **Risk**: The pagination logic using CICS browse commands is notoriously difficult to translate to relational database cursors or offset-based pagination. Multiple XCTL transfers to detail/edit screens create complex navigation state management.
- **Business Impact**: Card list is the entry point for all card management operations. Users must browse cards before viewing, editing, or selecting them.

**Migration Recommendations**:
1. Replace CICS STARTBR/READNEXT/READPREV with SQL LIMIT/OFFSET or keyset pagination
2. Implement as a paginated REST endpoint returning card DTOs
3. Extract navigation state from COMMAREA into session/request parameters
4. Add search/filter capability (the COBOL version only supports sequential browsing)

---

### #5 — COCRDUPC (Card Update) — Score: 4.10

| Metric               | Value                      |
|-----------------------|----------------------------|
| Lines of Code         | 1,560                      |
| PERFORM Count         | 26                         |
| CICS Commands         | 12 (READ, REWRITE, SEND, RECEIVE, XCTL, HANDLE ABEND) |
| Data Mutations        | CARDDATA (REWRITE)         |

**Why it's #5**:
- **Complexity**: At 1,560 lines, it's the third-largest program. Uses HANDLE ABEND for error recovery. Contains extensive screen field validation and attribute manipulation logic similar to COACTUPC.
- **Risk**: REWRITE to CARDDATA affects card status, expiration dates, and embossed names. Uses CICS HANDLE ABEND which is difficult to translate. Card data integrity impacts transaction processing.
- **Business Impact**: Card update manages card lifecycle (activation, deactivation, reissue). Card status directly affects whether transactions are approved or rejected by the posting engine.

**Migration Recommendations**:
1. Extract validation logic into a CardValidationService
2. Implement card lifecycle state machine (Active → Suspended → Closed)
3. Add audit logging for all card status changes (compliance requirement)
4. Replace HANDLE ABEND with exception handling

---

### #6 — CBSTM03A (Statement Generation) — Score: 4.00

| Metric               | Value                      |
|-----------------------|----------------------------|
| Lines of Code         | 924                        |
| PERFORM Count         | 33                         |
| CALL Targets          | CBSTM03B (file I/O sub)   |
| Output Formats        | Plain text + HTML          |
| Special Constructs    | ALTER + GO TO, COMP/COMP-3, 2D arrays, mainframe control blocks |

**Why it's #6**:
- **Complexity**: Deliberately designed to exercise difficult modernization patterns: `ALTER` + `GO TO` statements (dynamic control flow), `COMP` and `COMP-3` variables, 2-dimensional arrays, and mainframe control block addressing. The program header explicitly lists these as modernization challenges. Calls CBSTM03B subroutine for all file I/O.
- **Risk**: The `ALTER` statement dynamically changes `GO TO` targets at runtime — one of the most difficult COBOL constructs to translate to structured Java. The COMP/COMP-3 arithmetic requires precise BigDecimal conversion for financial accuracy.
- **Business Impact**: Statement generation is a core customer-facing deliverable. Every card account receives periodic statements. Incorrect statements create customer service burden and regulatory issues.

**Migration Recommendations**:
1. **Critical**: Refactor ALTER/GO TO into structured control flow BEFORE migration
2. Replace 2D arrays with Java collections (List<List<T>> or Map structures)
3. Generate statements using a template engine (Thymeleaf/FreeMarker) instead of line-by-line WRITE
4. Implement CBSTM03B file operations as a Spring Batch reader/writer
5. Add PDF generation natively (replacing the separate TXT2PDF1 utility)

---

### #7 — COACTVWC (Account View) — Score: 3.70

| Metric               | Value                      |
|-----------------------|----------------------------|
| Lines of Code         | 941                        |
| PERFORM Count         | 21                         |
| CICS Commands         | 14                         |
| Files Accessed        | ACCTDATA, CARDDATA, CARDXREF, CUSTDATA (4 files, READ only) |

**Why it's #7**:
- **Complexity**: Reads from 4 VSAM files to compose a denormalized account view. Uses HANDLE ABEND, string parsing (CSSTRPFY), and complex screen rendering with multiple map areas.
- **Risk**: Read-only, so lower data-mutation risk. However, the 4-file join logic must be correctly translated or the view will show incorrect/incomplete data.
- **Business Impact**: Account view is the most frequently accessed screen. Every customer inquiry starts here.

**Migration Recommendations**:
1. Implement as a read-only REST endpoint with a composite AccountDetailDTO
2. Replace 4-file VSAM reads with a single SQL JOIN query
3. Consider a materialized view or read model for performance

---

### #8 — COBIL00C (Bill Payment) — Score: 3.90

| Metric               | Value                      |
|-----------------------|----------------------------|
| Lines of Code         | 572                        |
| PERFORM Count         | 38                         |
| CICS Commands         | 12 (READ, REWRITE, WRITE, STARTBR, READPREV) |
| Data Mutations        | TRANSACT (WRITE), ACCTDATA (REWRITE) |

**Why it's #8**:
- **Complexity**: Moderate line count but dense logic — 38 PERFORMs in 572 lines. Reads card cross-reference, validates payment, writes a new transaction record, and updates the account balance in a single operation.
- **Risk**: **High financial risk** — this program directly debits account balances and creates payment transaction records. It updates ACCTDATA (balance) and writes to TRANSACT (new payment transaction). A bill payment bug has direct monetary impact.
- **Business Impact**: Bill payment is a core revenue-generating function. Payment processing correctness is essential for customer trust and regulatory compliance.

**Migration Recommendations**:
1. Implement with explicit transaction management (debit + record must be atomic)
2. Add payment idempotency (prevent duplicate payments)
3. Implement payment amount validation (max limits, minimum payments)
4. Add integration point for external payment gateway in modernized version

---

### #9 — CBTRN03C (Daily Transaction Report) — Score: 3.40

| Metric               | Value                      |
|-----------------------|----------------------------|
| Lines of Code         | 649                        |
| PERFORM Count         | 73 (highest PERFORM density)|
| File I/O Operations   | 4 files (TRANSACT, CARDXREF, TRANTYPE, TRANCATG) |
| Report Layout         | CVTRA07Y (multi-level totals)|

**Why it's #9**:
- **Complexity**: Has the highest PERFORM-to-LOC ratio in the codebase (73 PERFORMs in 649 lines = 1 per 8.9 lines), indicating highly modular but dense procedural logic. Reads from 4 files, joins transaction + cross-reference + type + category data, and produces a multi-level report with page/account/grand totals.
- **Risk**: Read-only reporting, so low data-mutation risk. However, the complex multi-level total accumulation (page totals, account totals, grand totals) must be correctly replicated.
- **Business Impact**: Daily transaction reporting is essential for operations monitoring, reconciliation, and audit. Incorrect reports can mask fraud or processing errors.

**Migration Recommendations**:
1. Implement as a Spring Batch reporting job using JasperReports or similar
2. Replace 4-file sequential scan with SQL GROUP BY queries for totals
3. Add export formats (CSV, Excel) beyond the current text output
4. Consider a real-time dashboard replacement for the batch report

---

### #10 — COTRN02C (Transaction Add) — Score: 3.70

| Metric               | Value                      |
|-----------------------|----------------------------|
| Lines of Code         | 783                        |
| PERFORM Count         | 61                         |
| CICS Commands         | 11 (READ, WRITE, STARTBR, READPREV, SEND, RECEIVE) |
| Data Mutations        | TRANSACT (WRITE)           |
| Files Accessed        | TRANSACT, CARDXREF         |

**Why it's #10**:
- **Complexity**: At 783 lines with 61 PERFORMs, this is a dense online program. Uses STARTBR/READPREV on TRANSACT to generate sequential transaction IDs. Contains extensive input validation for transaction amounts, merchant data, and card verification.
- **Risk**: WRITE to TRANSACT creates new financial records. The transaction ID generation logic (reading the last ID and incrementing) is a concurrency risk in a multi-user environment.
- **Business Impact**: Transaction add is used for manual transaction entry (e.g., phone orders, adjustments). While less frequent than batch posting, it creates permanent financial records.

**Migration Recommendations**:
1. Replace sequential ID generation with database sequences or UUIDs
2. Implement as a REST POST endpoint with request validation
3. Add concurrency control (optimistic locking) for the ID generation
4. Integrate with the same validation rules as the batch posting engine

---

## Summary Heat Map

```
                    COMPLEXITY ──────────────────────────►
                    Low           Medium          High
               ┌─────────────┬───────────────┬──────────────────┐
    High       │             │  COBIL00C(8)  │  COACTUPC(1)     │
               │             │               │  CBTRN02C(2)     │
  RISK         │             │               │  CBACT04C(3)     │
               ├─────────────┼───────────────┼──────────────────┤
    Medium     │             │  COTRN02C(10) │  COCRDUPC(5)     │
               │             │               │  CBSTM03A(6)     │
               ├─────────────┼───────────────┼──────────────────┤
    Low        │             │  CBTRN03C(9)  │  COCRDLIC(4)     │
               │             │  COACTVWC(7)  │                  │
               └─────────────┴───────────────┴──────────────────┘
```

---

## Migration Priority Recommendations

### Wave 1 — High Priority (Months 1–3)
| Module    | Rationale                                                   |
|-----------|-------------------------------------------------------------|
| CBTRN02C  | Core financial engine — must be correct before anything else|
| CBACT04C  | Financial calculation — regulatory compliance critical      |
| COBIL00C  | Payment processing — revenue-critical                       |
| COSGN00C  | Authentication — security foundation (simple, good starter) |

### Wave 2 — Medium Priority (Months 3–5)
| Module    | Rationale                                                   |
|-----------|-------------------------------------------------------------|
| COACTUPC  | Largest program but can be decomposed; account operations depend on Wave 1 |
| COCRDUPC  | Card management — depends on account foundation             |
| COTRN02C  | Online transaction entry — shares validation with CBTRN02C  |
| CBSTM03A  | Statement generation — depends on posted transactions       |

### Wave 3 — Lower Priority (Months 5–7)
| Module    | Rationale                                                   |
|-----------|-------------------------------------------------------------|
| COCRDLIC  | Browsing/navigation — can use temporary data access layer   |
| COACTVWC  | Read-only view — lowest risk, can be built on Wave 2 services|
| CBTRN03C  | Reporting — can run against modernized database             |
| COMEN01C, COADM01C | Navigation — replaced by web UI routing          |

### Wave 4 — Utility & Reference Data (Months 7–8)
| Module    | Rationale                                                   |
|-----------|-------------------------------------------------------------|
| CBEXPORT/CBIMPORT | Data migration utilities — needed for cutover    |
| COUSR00C–03C | User management — can use modern IAM initially           |
| Reference data jobs (TRANTYPE, TRANCATG, DISCGRP, TCATBALF) | Simple VSAM-to-table loads |

---

## Risk Mitigation Strategies

1. **Financial Accuracy**: Use `BigDecimal` for ALL monetary calculations. Never use `float` or `double`. Implement penny-level reconciliation tests comparing COBOL output to Java output using the sample data in `app/data/ASCII/`.

2. **Data Integrity**: Implement database constraints (FK, CHECK, NOT NULL) that enforce the same rules currently embedded in COBOL program logic. The COBOL programs contain validation logic that must become database constraints + application validation.

3. **Batch Restart/Recovery**: COBOL batch programs rely on job-level restart (re-run from beginning or checkpoint). Spring Batch provides chunk-level restart — implement this to improve resilience.

4. **CICS State Management**: The COMMAREA (COCOM01Y) carries navigation state between programs. Replace with HTTP session state or JWT claims in the modernized application. Test navigation flows end-to-end.

5. **Packed Decimal (COMP-3)**: Programs CBACT04C, CBSTM03A, and CBTRN02C use COMP-3 variables. These must be converted to BigDecimal with explicit scale/precision. Test with boundary values (max 9-digit amounts, negative balances).

6. **ALTER/GO TO (CBSTM03A)**: This is the single hardest construct to modernize. Refactor to structured control flow (IF/ELSE, SWITCH) before attempting Java translation.
