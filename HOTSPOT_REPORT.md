# CardDemo Hotspot Report — Top 10 Modernization Priorities

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo
>
> This report identifies the 10 highest-priority modules for modernization, ranked by a composite score of code complexity, migration risk, and business impact.

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Executive Summary](#executive-summary)
3. [Top 10 Hotspot Rankings](#top-10-hotspot-rankings)
4. [Detailed Module Analysis](#detailed-module-analysis)
5. [Risk Mitigation Recommendations](#risk-mitigation-recommendations)
6. [Migration Wave Plan](#migration-wave-plan)

---

## Scoring Methodology

Each module is scored on three dimensions (1–10 scale each), producing a **composite score (max 30)**:

| Dimension          | Weight | Scoring Criteria                                                         |
|-------------------|--------|--------------------------------------------------------------------------|
| **Complexity**     | Equal  | Lines of code, cyclomatic complexity (IF/EVALUATE counts), PERFORM count, copybook dependencies, CICS command count |
| **Migration Risk** | Equal  | COMP-3/COMP fields, CICS API calls, VSAM I/O operations, REDEFINES usage, external CALLs, date handling, COPY REPLACING |
| **Business Impact**| Equal  | Revenue criticality, user-facing visibility, data integrity role, downstream dependencies, frequency of execution |

---

## Executive Summary

| Rank | Module     | Type   | Lines  | Composite Score | Primary Concern                          |
|------|-----------|--------|--------|----------------|------------------------------------------|
| 1    | COACTUPC  | Online | 4,236  | 28/30          | Largest program; 18 copybooks; heavy CICS |
| 2    | CBTRN02C  | Batch  | 731    | 26/30          | Core posting engine; financial integrity  |
| 3    | CBACT04C  | Batch  | 652    | 25/30          | Interest calculation; COMP-3 arithmetic   |
| 4    | COCRDLIC  | Online | 1,459  | 24/30          | Complex list UI; 18 CICS commands         |
| 5    | COCRDUPC  | Online | 1,560  | 24/30          | Card update; 16 EVALUATE blocks           |
| 6    | CBSTM03A  | Batch  | 924    | 23/30          | Statement gen; dual output; calls sub     |
| 7    | CBTRN03C  | Batch  | 649    | 22/30          | Report engine; 5 reference files          |
| 8    | COTRN02C  | Online | 783    | 22/30          | Transaction add; date validation; VSAM writes |
| 9    | COBIL00C  | Online | 572    | 21/30          | Bill payment; balance updates; financial  |
| 10   | CBEXPORT  | Batch  | 582    | 20/30          | Data migration; REDEFINES polymorphism    |

---

## Top 10 Hotspot Rankings

### Visual Ranking

```
Module      Complexity  Risk  Impact  Total   Bar
─────────── ──────────  ────  ──────  ─────   ───────────────────────────
COACTUPC    10          9     9       28      ████████████████████████████
CBTRN02C     8          9     9       26      ██████████████████████████
CBACT04C     7          9     9       25      █████████████████████████
COCRDLIC     9          8     7       24      ████████████████████████
COCRDUPC     9          8     7       24      ████████████████████████
CBSTM03A     8          8     7       23      ███████████████████████
CBTRN03C     7          7     8       22      ██████████████████████
COTRN02C     7          7     8       22      ██████████████████████
COBIL00C     6          7     8       21      █████████████████████
CBEXPORT     6          8     6       20      ████████████████████
```

---

## Detailed Module Analysis

### #1 — COACTUPC (Account Update) — Score: 28/30

| Metric              | Value    | Concern Level |
|--------------------|----------|---------------|
| Lines of Code      | 4,236    | 🔴 Critical   |
| COPY Statements    | 56       | 🔴 Critical   |
| Unique Copybooks   | 18       | 🔴 Critical   |
| EXEC CICS Commands | 17       | 🔴 Critical   |
| EVALUATE Blocks    | 10       | 🟡 High       |
| IF Statements      | 168      | 🔴 Critical   |
| PERFORM Statements | 64       | 🟡 High       |

**Why It's #1**: This is the single largest and most complex program in the entire application. At 4,236 lines with 168 IF statements, it has the highest cyclomatic complexity. It uses COPY REPLACING 38 times (CSSETATY) for field attribute management — a pattern that must be carefully translated to Java reflection or annotation-based approaches. It touches 5 VSAM files (Account, Card, Customer, Cross-Reference, plus lookups) and includes the full date validation suite (CSUTLDPY/CSUTLDWY).

**Complexity Drivers**:
- 38x COPY REPLACING for screen attribute management
- Inline COPY of CSUTLDPY (375-line date validation procedure)
- Inline COPY of CSSTRPFY (85-line string utility)
- Reads/writes Account, Card, Customer, and Cross-Reference VSAM files
- Full field-level validation with individual error messaging

**Migration Strategy**: Decompose into multiple Java classes — AccountUpdateService, AccountValidator, FieldAttributeManager. Extract the COPY REPLACING pattern into a reusable annotation processor or decorator. Consider splitting into view-model, validation, and persistence layers.

---

### #2 — CBTRN02C (Transaction Posting) — Score: 26/30

| Metric              | Value    | Concern Level |
|--------------------|----------|---------------|
| Lines of Code      | 731      | 🟡 High       |
| Unique Copybooks   | 5        | 🟢 Medium     |
| PERFORM Statements | 61       | 🟡 High       |
| IF Statements      | 48       | 🟡 High       |
| File I/O           | 5 files  | 🟡 High       |

**Why It's #2**: This is the **core financial posting engine** — the single most business-critical batch program. It reads from DALYTRAN (daily transactions), validates against CARDXREF, and posts to TRANSACT (master). Any bug in the migration directly impacts financial accuracy. It also updates ACCTDATA (account balances) and TCATBALF (category balances), making it a multi-file update with implicit transactional requirements.

**Complexity Drivers**:
- Multi-file coordinated updates (5 VSAM files)
- Financial arithmetic with COMP-3 packed decimal
- No explicit transaction boundaries (VSAM doesn't have them)
- Complex validation logic against cross-reference data
- Abend handling via CEE3ABD

**Migration Strategy**: Map to a Spring Batch job with chunk-oriented processing. Wrap multi-table updates in database transactions. Implement idempotency via transaction ID deduplication. Add comprehensive audit logging.

---

### #3 — CBACT04C (Interest Calculation) — Score: 25/30

| Metric              | Value    | Concern Level |
|--------------------|----------|---------------|
| Lines of Code      | 652      | 🟡 High       |
| Unique Copybooks   | 5        | 🟢 Medium     |
| PERFORM Statements | 56       | 🟡 High       |
| IF Statements      | 43       | 🟡 High       |
| File I/O           | 5 files  | 🟡 High       |

**Why It's #3**: Interest calculation is the most financially sensitive computation in the system. It reads TCATBALF (category balances) and DISCGRP (disclosure/rate tables) to compute interest charges, then updates TRANSACT and account records. COMP-3 packed decimal arithmetic must be precisely replicated — even a rounding difference of $0.01 per account multiplied across thousands of accounts creates material financial discrepancies.

**Complexity Drivers**:
- COMP-3 packed decimal financial arithmetic
- Interest rate lookup across Disclosure Group table
- Rate × Balance calculation with precise decimal handling
- Multi-file reads: TCATBALF, DISCGRP, ACCTDATA, TRANSACT, XREF
- Regulatory implications of incorrect interest charges

**Migration Strategy**: Use `java.math.BigDecimal` with explicit `RoundingMode.HALF_UP` (or whatever the COBOL ROUNDED directive specifies). Create an InterestCalculationService with thorough unit tests comparing results against COBOL output. Implement a parallel-run validation period.

---

### #4 — COCRDLIC (Credit Card List) — Score: 24/30

| Metric              | Value    | Concern Level |
|--------------------|----------|---------------|
| Lines of Code      | 1,459    | 🟡 High       |
| Unique Copybooks   | 13       | 🟡 High       |
| EXEC CICS Commands | 18       | 🔴 Critical   |
| EVALUATE Blocks    | 18       | 🔴 Critical   |
| IF Statements      | 61       | 🟡 High       |
| PERFORM Statements | 34       | 🟢 Medium     |

**Why It's #4**: Most CICS-intensive program (18 EXEC CICS commands) with the highest EVALUATE count (18 blocks). Implements scrollable list with forward/backward paging over VSAM BROWSE operations. The selection logic (view/update routing) adds UI state management complexity. Contains XCTL transfers to COCRDSLC and COCRDUPC based on user selection.

**Complexity Drivers**:
- 18 EXEC CICS commands (READ, BROWSE, STARTBR, READNEXT, READPREV, ENDBR, SEND MAP, RECEIVE MAP)
- Scrollable paging with cursor position tracking
- Multi-row selection with error validation (max 1 row)
- Dynamic routing to View or Update programs
- Role-based access (admin sees all cards, user sees own only)

**Migration Strategy**: Map to a Spring MVC controller with paginated REST endpoint. Replace VSAM BROWSE with SQL `SELECT ... ORDER BY ... LIMIT/OFFSET`. Implement client-side pagination or server-side cursor. Replace XCTL routing with HTTP redirects or SPA navigation.

---

### #5 — COCRDUPC (Credit Card Update) — Score: 24/30

| Metric              | Value    | Concern Level |
|--------------------|----------|---------------|
| Lines of Code      | 1,560    | 🟡 High       |
| Unique Copybooks   | 15       | 🟡 High       |
| EXEC CICS Commands | 12       | 🟡 High       |
| EVALUATE Blocks    | 16       | 🔴 Critical   |
| IF Statements      | 74       | 🔴 Critical   |

**Why It's #5**: Second-largest online program. High cyclomatic complexity from 74 IF statements and 16 EVALUATE blocks implementing field-level validation for card updates. Reads and writes Card (CVACT02Y) and Customer (CVCUS01Y) data with cross-reference lookups.

**Complexity Drivers**:
- 74 IF statements (field-level validation branching)
- 16 EVALUATE blocks (state machine for edit/confirm/error flow)
- Multi-entity read/write (Card + Customer + Account VSAM)
- CSSTRPFY inline copy for field stripping/padding

**Migration Strategy**: Decompose into CardUpdateController, CardValidationService, and CardRepository. Extract validation rules into a declarative validation framework (Bean Validation / JSR 380). Separate the confirm/edit state machine into explicit workflow states.

---

### #6 — CBSTM03A (Statement Generation) — Score: 23/30

| Metric              | Value    | Concern Level |
|--------------------|----------|---------------|
| Lines of Code      | 924      | 🟡 High       |
| Unique Copybooks   | 4        | 🟢 Medium     |
| CALL Statements    | 14       | 🔴 Critical   |
| EVALUATE Blocks    | 5        | 🟢 Medium     |
| File I/O           | 7 files  | 🔴 Critical   |

**Why It's #6**: Generates account statements in **two formats** (plain text and HTML) simultaneously, reading from Transaction, Account, Customer, and Cross-Reference VSAM files. Makes 14 calls to CBSTM03B subroutine for line formatting. Uses mainframe control block addressing — a low-level technique that doesn't translate directly to Java.

**Complexity Drivers**:
- Dual output format (text + HTML) in single pass
- 14 CALL CBSTM03B invocations with shared working storage
- 7 file I/O streams (reads + writes)
- Mainframe control block addressing (noted in source comments)
- Multi-account loop with page/account/grand totals

**Migration Strategy**: Map to Spring Batch with a Thymeleaf or FreeMarker template engine for HTML output and a text formatter for plain text. Replace CBSTM03B calls with a StatementLineFormatter utility class. Consider generating PDF directly instead of text→PDF conversion.

---

### #7 — CBTRN03C (Transaction Report) — Score: 22/30

| Metric              | Value    | Concern Level |
|--------------------|----------|---------------|
| Lines of Code      | 649      | 🟡 High       |
| Unique Copybooks   | 5        | 🟢 Medium     |
| PERFORM Statements | 72       | 🔴 Critical   |
| EVALUATE Blocks    | 4        | 🟢 Medium     |
| IF Statements      | 38       | 🟡 High       |

**Why It's #7**: Highest PERFORM count (72) of any program, indicating deeply nested procedural logic. Reads from 5 reference files (TRANSACT, CARDXREF, TRANTYPE, TRANCATG, report output) to produce the Daily Transaction Report with page/account/grand totals. Uses CVTRA07Y report layout copybook.

**Complexity Drivers**:
- 72 PERFORM statements (deeply structured procedural flow)
- 5-file coordinated read with cross-reference lookups
- Report pagination with running totals at 3 levels
- Sort integration (JCL SORT step feeds sorted input)
- GDG dataset input (requires understanding of generation data groups)

**Migration Strategy**: Map to Spring Batch with JasperReports or a custom report builder. Replace SORT step with SQL `ORDER BY`. Implement running totals as accumulator pattern in the ItemProcessor. Replace GDG with versioned file naming or database-driven audit trail.

---

### #8 — COTRN02C (Transaction Add) — Score: 22/30

| Metric              | Value    | Concern Level |
|--------------------|----------|---------------|
| Lines of Code      | 783      | 🟡 High       |
| Unique Copybooks   | 10       | 🟡 High       |
| EXEC CICS Commands | 11       | 🟡 High       |
| EVALUATE Blocks    | 13       | 🟡 High       |
| CALL Statements    | 2        | 🟢 Medium     |

**Why It's #8**: Creates new transactions online and writes to DALYTRAN VSAM. Calls CSUTLDTC for date validation. Updates both the daily transaction file and the account balance, making it a multi-resource write with data integrity implications. This is one of the few online programs that also creates data (not just reads/updates).

**Complexity Drivers**:
- Writes to DALYTRAN VSAM (creates new records)
- Updates ACCTDATA (account balance adjustment)
- Cross-reference validation via CARDXREF
- Date validation via CSUTLDTC CALL
- 13 EVALUATE blocks for multi-step add workflow

**Migration Strategy**: Map to a TransactionAddController + TransactionService with `@Transactional` database writes. Replace CSUTLDTC call with Java date validation. Implement optimistic locking for concurrent balance updates.

---

### #9 — COBIL00C (Bill Payment) — Score: 21/30

| Metric              | Value    | Concern Level |
|--------------------|----------|---------------|
| Lines of Code      | 572      | 🟢 Medium     |
| Unique Copybooks   | 10       | 🟡 High       |
| EXEC CICS Commands | 13       | 🟡 High       |
| EVALUATE Blocks    | 9        | 🟡 High       |
| IF Statements      | 10       | 🟢 Medium     |

**Why It's #9**: Processes bill payments by creating a payment transaction in DALYTRAN and updating the account balance in ACCTDATA. Despite moderate line count, it has high business impact because it directly affects customer balances and generates financial transactions. Payment processing bugs are high-visibility customer issues.

**Complexity Drivers**:
- Financial balance update (ACCTDAT current balance)
- Transaction creation (DALYTRAN write)
- Cross-reference lookup for card-to-account mapping
- 13 CICS commands for screen I/O and file access
- Must handle payment amounts, partial payments, overpayments

**Migration Strategy**: Map to a BillPaymentService with `@Transactional`. Implement idempotency keys to prevent duplicate payments. Add payment amount validation (min/max, account standing). Consider event-driven architecture for payment notifications.

---

### #10 — CBEXPORT (Data Export) — Score: 20/30

| Metric              | Value    | Concern Level |
|--------------------|----------|---------------|
| Lines of Code      | 582      | 🟢 Medium     |
| Unique Copybooks   | 6        | 🟢 Medium     |
| PERFORM Statements | 45       | 🟡 High       |
| IF Statements      | 16       | 🟢 Medium     |
| File I/O           | 5 files  | 🟡 High       |

**Why It's #10**: Uses the CVEXPORT copybook with its multi-type REDEFINES structure — a union type that carries Customer, Account, Transaction, Card, and Cross-Reference records in a single file format. This REDEFINES pattern is one of the hardest COBOL constructs to translate to Java. Reads from all major VSAM files and produces a consolidated export.

**Complexity Drivers**:
- REDEFINES union type (5 different record structures in one)
- Reads from 5 VSAM files simultaneously
- Sequence numbering and branch/region tagging
- Statistics generation and export report
- Must maintain exact record alignment for CBIMPORT compatibility

**Migration Strategy**: Implement using Java sealed classes or a discriminated union pattern. Consider replacing the flat-file export with JSON/CSV format. If maintaining mainframe compatibility, use a RecordTypeFactory with explicit serialization. Pair with CBIMPORT migration.

---

## Risk Mitigation Recommendations

### Critical Risks (Address Before Migration)

| Risk                                   | Affected Modules          | Mitigation                                       |
|---------------------------------------|--------------------------|--------------------------------------------------|
| **COMP-3 decimal precision**          | CBTRN02C, CBACT04C, COBIL00C | Build a COMP-3 test harness; validate every arithmetic operation against COBOL output |
| **Plaintext passwords**               | CSUSR01Y, COSGN00C       | Implement bcrypt hashing in migration; create data migration script for existing passwords |
| **No transaction boundaries**         | CBTRN02C, COBIL00C, COTRN02C | Wrap multi-file updates in DB transactions; design rollback strategy |
| **CVV stored in plaintext**           | CVACT02Y, COCRDSLC       | Implement tokenization/encryption service; consider PCI-DSS compliance |

### High Risks (Monitor During Migration)

| Risk                                   | Affected Modules          | Mitigation                                       |
|---------------------------------------|--------------------------|--------------------------------------------------|
| **COPY REPLACING pattern**            | COACTUPC (38 instances)  | Create reusable Java utility; validate all field mappings |
| **REDEFINES union types**             | CBEXPORT, CBIMPORT       | Use sealed classes / factory pattern; exhaustive tests |
| **CICS pseudo-conversational model**  | All online programs       | Map to HTTP session state; handle back-button scenarios |
| **Date handling (CEEDAYS)**           | CSUTLDTC, COACTUPC       | Replace with `java.time`; validate edge cases (leap years, Y2K) |
| **GDG (Generation Data Groups)**      | TRANBKP, TRANREPT        | Replace with versioned files or database audit trail |

---

## Migration Wave Plan

Based on the hotspot analysis, here is the recommended migration wave ordering:

### Wave 1 — Foundation (Low Risk, High Reuse)
> Establish base services and utilities

| Module    | Reason                                    | Dependencies |
|----------|-------------------------------------------|-------------|
| CSUTLDTC | Date utility — reused by multiple programs | None        |
| CSUSR01Y | User entity — foundation for auth         | None        |
| COSGN00C | Sign-on — enables testing of other modules | CSUTLDTC    |

### Wave 2 — Read-Only Screens (Medium Complexity)
> Build out read paths before write paths

| Module    | Reason                                    | Dependencies |
|----------|-------------------------------------------|-------------|
| COMEN01C | Menu — navigation backbone               | Wave 1      |
| COACTVWC | Account view — read-only, tests data layer | Wave 1     |
| COCRDSLC | Card view — read-only                     | Wave 1      |
| COTRN01C | Transaction view — read-only              | Wave 1      |

### Wave 3 — Write Operations (High Complexity)
> Implement data modification with full validation

| Module    | Reason                                    | Dependencies |
|----------|-------------------------------------------|-------------|
| COACTUPC | Account update — most complex, do early   | Wave 2      |
| COCRDUPC | Card update — similar patterns            | Wave 2      |
| COTRN02C | Transaction add — creates new data        | Wave 2      |
| COBIL00C | Bill payment — financial write            | Wave 2      |

### Wave 4 — Batch Processing (Highest Risk)
> Migrate batch after online is stable

| Module    | Reason                                    | Dependencies |
|----------|-------------------------------------------|-------------|
| CBTRN02C | Transaction posting — core batch          | Wave 3      |
| CBACT04C | Interest calculation — financial critical | Wave 3      |
| CBTRN01C | Transaction combination                   | Wave 3      |
| CBSTM03A | Statement generation                      | Wave 3      |
| CBTRN03C | Transaction reporting                     | Wave 3      |

### Wave 5 — List Screens & Admin
> Lower priority, can coexist with legacy longer

| Module    | Reason                                    | Dependencies |
|----------|-------------------------------------------|-------------|
| COCRDLIC | Card list — complex but lower risk        | Wave 3      |
| COTRN00C | Transaction list                          | Wave 3      |
| COUSR00C-03C | User admin — lower frequency          | Wave 1      |
| COADM01C | Admin menu                                | Wave 1      |
| CORPT00C | Report request                            | Wave 4      |

### Wave 6 — Data Migration & Optional
> Final wave for export/import and extensions

| Module    | Reason                                    | Dependencies |
|----------|-------------------------------------------|-------------|
| CBEXPORT | Export utility — REDEFINES complexity     | Wave 4      |
| CBIMPORT | Import utility — paired with CBEXPORT     | Wave 4      |
| Optional modules | IMS/DB2/MQ extensions              | All waves   |
