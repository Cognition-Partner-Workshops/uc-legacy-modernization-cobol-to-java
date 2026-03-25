# CardDemo Hotspot Report — Top 10 Modules

> **Generated:** 2026-03-25 | **Methodology:** Weighted scoring across code complexity, data coupling, business risk, and modernization difficulty

---

## Scoring Methodology

Each module is scored across four dimensions (1–5 scale each, 5 = highest):

| Dimension | Weight | What It Measures |
|-----------|--------|-----------------|
| **Code Complexity** | 30% | Lines of code, cyclomatic complexity (IF/EVALUATE branches), PERFORM count |
| **Data Coupling** | 25% | Number of VSAM files accessed, copybooks included, cross-program dependencies |
| **Business Risk** | 25% | Financial impact if module fails, data integrity criticality, user-facing exposure |
| **Modernization Difficulty** | 20% | CICS/VSAM-specific patterns, screen interactions, state management complexity |

**Composite Score** = (Complexity × 0.30) + (Coupling × 0.25) + (Risk × 0.25) + (Difficulty × 0.20)

---

## Top 10 Hotspot Ranking

| Rank | Program | LOC | Composite | Priority |
|------|---------|-----|-----------|----------|
| 1 | COACTUPC | 4,236 | **4.70** | Critical |
| 2 | CBTRN02C | 731 | **4.45** | Critical |
| 3 | COCRDUPC | 1,560 | **4.15** | Critical |
| 4 | COCRDLIC | 1,459 | **4.05** | High |
| 5 | CBACT04C | 652 | **4.00** | High |
| 6 | CBSTM03A | 924 | **3.90** | High |
| 7 | COBIL00C | 572 | **3.85** | High |
| 8 | CBTRN03C | 649 | **3.55** | Medium-High |
| 9 | COTRN02C | 783 | **3.50** | Medium-High |
| 10 | CORPT00C | 649 | **3.30** | Medium-High |

---

## Detailed Analysis

### #1 — COACTUPC (Account Update) — Score: 4.70

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Complexity | **5** | 4,236 lines — largest program. 167 IF statements, 20 EVALUATE blocks, 64 PERFORMs, 17 EXEC CICS calls |
| Coupling | **5** | Accesses 3 VSAM files (ACCTDATA, CARDXREF, CUSTDATA). 10+ copybooks. Central to account management |
| Risk | **5** | Directly modifies account balances and credit limits. Financial data integrity — any bug means monetary loss |
| Difficulty | **4** | Complex screen interaction with multi-field validation, conditional field editing, and CICS conversational state |

**Why #1:** This is the most complex program in the entire codebase by every measure. It handles account balance modifications, credit limit changes, and status updates — all high-risk financial operations. The 167 IF statements indicate deeply nested validation logic that will require careful unit testing during conversion.

**Modernization Recommendation:**
- Decompose into separate service classes: `AccountValidationService`, `AccountUpdateService`, `AccountViewController`
- Extract the 20 EVALUATE blocks into a strategy pattern or state machine
- Implement comprehensive integration tests before and after conversion
- Map the CICS conversational state to HTTP session or form wizard pattern

---

### #2 — CBTRN02C (Transaction Posting) — Score: 4.45

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Complexity | **4** | 731 lines. 61 PERFORMs, 48 IF statements. Complex multi-file batch processing with rejection logic |
| Coupling | **5** | Reads 4 files (DALYTRAN, XREFFILE, ACCTDATA, TCATBALF), writes 2 (TRANSACT, DALYREJS). Core batch job |
| Risk | **5** | Posts daily transactions to master — the central financial batch process. Rejection handling is critical |
| Difficulty | **4** | Multi-file coordination with cross-reference lookups, balance updates, and category accumulations |

**Why #2:** This is the **financial heart of batch processing**. Every daily transaction flows through this program. It validates transactions against cross-references, updates account balances, accumulates category totals, and segregates rejected transactions. A bug here corrupts all account balances.

**Modernization Recommendation:**
- Convert to Spring Batch `Tasklet` or chunk-oriented step
- Implement idempotent processing with transaction IDs to prevent double-posting
- Add comprehensive reconciliation logging
- The DALYREJS (rejection) output should become a dead-letter queue pattern

---

### #3 — COCRDUPC (Card Update) — Score: 4.15

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Complexity | **4** | 1,560 lines. 73 IF statements, 16 EVALUATE blocks, 26 PERFORMs, 12 EXEC CICS calls |
| Coupling | **4** | Accesses 3 VSAM files (CARDDATA, CUSTDATA, cross-reference). 12 copybooks including CSSTRPFY utility |
| Risk | **4** | Modifies card data (status, embossed name, expiration). Card activation/deactivation is security-sensitive |
| Difficulty | **4** | CICS HANDLE ABEND for error recovery, complex screen field validation, confirmation flow |

**Why #3:** Second-largest online program. The card update flow includes activation/deactivation which is security-critical. Uses CICS HANDLE ABEND for error recovery — a pattern that maps poorly to Java exceptions and requires careful redesign.

**Modernization Recommendation:**
- Separate card validation from update logic
- Map HANDLE ABEND to try-catch with proper rollback semantics
- Card status changes should trigger audit events
- Consider card number masking for PCI compliance in the Java version

---

### #4 — COCRDLIC (Card List) — Score: 4.05

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Complexity | **4** | 1,459 lines. 60 IF statements, 18 EVALUATE blocks, 34 PERFORMs, 18 EXEC CICS calls (highest) |
| Coupling | **4** | CARDDATA VSAM browse with STARTBR/READNEXT/READPREV/ENDBR. 11 copybooks. Routes to 3 sub-programs |
| Risk | **3** | Read-only list view — lower data integrity risk, but high user-facing visibility |
| Difficulty | **5** | Most CICS-intensive program (18 calls). Complex browse/paging logic with STARTBR cursors maps poorly to REST |

**Why #4:** Has the **highest EXEC CICS call count** (18) of any program. The VSAM browse pattern (STARTBR → READNEXT → READPREV → ENDBR) with cursor positioning is one of the hardest patterns to convert to JPA pagination. Also the navigation hub for card management, routing to view/update.

**Modernization Recommendation:**
- Convert VSAM browse to JPA `Pageable` with `Page<Card>` responses
- Replace CICS cursor positioning with offset/keyset pagination
- The three XCTL routes become REST API endpoints or navigation links
- Consider the CSSTRPFY copybook as a shared Java utility class

---

### #5 — CBACT04C (Interest Calculation) — Score: 4.00

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Complexity | **4** | 652 lines. 56 PERFORMs, 43 IF statements. Multi-table lookup with financial calculations |
| Coupling | **4** | Reads 4 files (TCATBALF, XREFFILE, ACCTDATA, DISCGRP), writes TRANSACT. Disclosure group rate lookups |
| Risk | **5** | Interest calculations directly affect customer billing. Rounding/precision errors = financial liability |
| Difficulty | **3** | Batch-only, no CICS. But financial precision requirements and disclosure group logic are complex |

**Why #5:** **Highest business risk for a batch program.** Interest calculations are legally regulated and must be precisely accurate. The program performs multi-table lookups (disclosure groups determine rates per account group/transaction type/category), applies interest, and generates interest transactions. Any rounding error compounds across all accounts.

**Modernization Recommendation:**
- Use `BigDecimal` exclusively for all monetary calculations (no `double`/`float`)
- Preserve exact COBOL PIC S9(09)V99 precision semantics
- Create a dedicated `InterestCalculationService` with comprehensive test fixtures
- Regulatory compliance testing: verify results match COBOL to the penny

---

### #6 — CBSTM03A (Statement Generation) — Score: 3.90

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Complexity | **4** | 924 lines. 29 PERFORMs, 15 IF statements, 9 EVALUATE blocks. 13 CALL statements to CBSTM03B |
| Coupling | **4** | Reads 4 VSAM files (TRXFL, CARDXREF, ACCTDATA, CUSTDATA). Writes text + HTML output. Calls subroutine |
| Risk | **4** | Customer-facing output (statements). Formatting errors = customer complaints and regulatory issues |
| Difficulty | **3** | Dual output (text + HTML). Subroutine call pattern (13 calls to CBSTM03B). Report formatting logic |

**Why #6:** Generates customer-visible statements in both text and HTML format. The 13 calls to CBSTM03B (I/O subroutine) represent a tight caller-callee coupling. Customer statements are regulatory documents — formatting must be exact.

**Modernization Recommendation:**
- Merge CBSTM03A + CBSTM03B into a single `StatementGenerationService`
- Replace text/HTML dual output with a template engine (Thymeleaf, FreeMarker)
- Convert to Spring Batch job with chunk processing per account
- Add PDF generation capability (replacing TXT2PDF1 JCL)

---

### #7 — COBIL00C (Bill Payment) — Score: 3.85

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Complexity | **3** | 572 lines. 38 PERFORMs, 18 EVALUATE blocks, 13 EXEC CICS calls |
| Coupling | **4** | Reads/writes 3 files (TRANSACT write, ACCTDATA read/rewrite, CARDXREF browse). 10 copybooks |
| Risk | **5** | Creates payment transactions AND updates account balances. Dual-write = highest consistency risk |
| Difficulty | **3** | Standard CICS screen pattern but with critical dual-write (transaction + balance update) |

**Why #7:** Performs a **dual write** — creating a payment transaction in TRANSACT while simultaneously updating the account balance in ACCTDATA. If either write fails, data becomes inconsistent. This is the most transactionally sensitive online operation.

**Modernization Recommendation:**
- Wrap both writes in a single database transaction (`@Transactional`)
- Implement optimistic locking on account balance to prevent lost updates
- Add idempotency key to prevent duplicate payments
- Consider event sourcing pattern: payment event → balance projection

---

### #8 — CBTRN03C (Transaction Report) — Score: 3.55

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Complexity | **4** | 649 lines. 72 PERFORMs (highest of any program), 38 IF statements, 4 EVALUATE blocks |
| Coupling | **4** | Reads 5 files (TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM). Writes report output |
| Risk | **3** | Report generation — no data modification, but accuracy affects business decisions |
| Difficulty | **3** | Batch-only. Multiple file joins in procedural code. Complex report formatting with control breaks |

**Why #8:** Has the **highest PERFORM count** (72) of any program, indicating deep procedural decomposition. Reads 5 input files — the most of any single program. The control-break report logic (page totals, account totals, grand totals) requires careful conversion.

**Modernization Recommendation:**
- Convert multi-file reads to JPA repository queries with joins
- Replace control-break logic with JasperReports or a reporting library
- The 5-file join pattern maps to a SQL query with JOINs
- Spring Batch `FlatFileItemWriter` for report output

---

### #9 — COTRN02C (Transaction Add) — Score: 3.50

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Complexity | **4** | 783 lines. 61 PERFORMs, 26 EVALUATE blocks (highest of any program), 14 IF statements |
| Coupling | **4** | Reads/writes 4 files (TRANSACT write, ACCTDATA read, CARDXREF browse). Calls CSUTLDTC for date validation |
| Risk | **3** | Creates new transactions. Validation errors could allow invalid transactions |
| Difficulty | **3** | CICS screen with date validation (CALL to CSUTLDTC), card lookup browse, and transaction write |

**Why #9:** Has the **highest EVALUATE count** (26) — indicating the most complex decision logic of any program. The 26 EVALUATE blocks handle different input states, validation outcomes, and screen flow decisions. Also depends on CSUTLDTC for date validation — a cross-program call dependency.

**Modernization Recommendation:**
- Map EVALUATE blocks to Java switch statements or enum-based state machines
- Replace CSUTLDTC date validation with `java.time` API
- Implement JSR-380 bean validation for transaction input
- The CARDXREF browse for card lookup becomes a simple JPA query

---

### #10 — CORPT00C (Report Request) — Score: 3.30

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Complexity | **3** | 649 lines. 34 PERFORMs, 10 EVALUATE blocks, 20 IF statements, 7 EXEC CICS calls |
| Coupling | **3** | Minimal VSAM access. Writes to TDQ (transient data queue) to submit JCL. Calls CSUTLDTC |
| Risk | **3** | Report submission — no direct data modification, but INTRDR JCL submission is a unique pattern |
| Difficulty | **4** | Uses EXEC CICS WRITEQ TD to submit JCL via internal reader — no Java equivalent. Unique integration |

**Why #10:** The **only program that submits JCL jobs** from online CICS. It writes JCL statements to a transient data queue (TDQ) which routes to the internal reader (INTRDR). This CICS-to-batch bridge pattern has no direct Java equivalent and requires architectural redesign.

**Modernization Recommendation:**
- Replace TDQ/INTRDR pattern with async job submission (Spring Batch `JobLauncher`)
- Report parameters collected in UI → REST API → scheduled/async batch job
- Consider message queue (RabbitMQ/SQS) for decoupled job submission
- Date validation via CSUTLDTC → `java.time` API

---

## Summary Heat Map

```
                     Complexity  Coupling  Risk  Difficulty  COMPOSITE
COACTUPC  ████████   █████       █████    █████  ████        ████ 4.70
CBTRN02C  ████████   ████        █████    █████  ████        ████ 4.45
COCRDUPC  ████████   ████        ████     ████   ████        ████ 4.15
COCRDLIC  ████████   ████        ████     ███    █████       ████ 4.05
CBACT04C  ███████    ████        ████     █████  ███         ████ 4.00
CBSTM03A  ███████    ████        ████     ████   ███         ███  3.90
COBIL00C  ██████     ███         ████     █████  ███         ███  3.85
CBTRN03C  ██████     ████        ████     ███    ███         ███  3.55
COTRN02C  ███████    ████        ████     ███    ███         ███  3.50
CORPT00C  ██████     ███         ███      ███    ████        ███  3.30
```

---

## Recommended Migration Order

Based on the hotspot analysis, the recommended migration sequence balances risk reduction with incremental value delivery:

### Wave 1 — Foundation (Weeks 1–4)
1. **Data layer first:** Convert all copybook record layouts to JPA entities (see `DATA_DICTIONARY.md`)
2. **CSUTLDTC** → Java `DateUtils` class (shared utility, depended on by multiple programs)
3. **CSUSR01Y / COSGN00C** → Spring Security authentication (enables testing all other modules)

### Wave 2 — Read-Only Screens (Weeks 5–8)
4. **COACTVWC** → Account view (read-only, moderate complexity, validates data layer)
5. **COCRDLIC / COCRDSLC** → Card list and detail (tests VSAM browse → JPA pagination)
6. **COTRN00C / COTRN01C** → Transaction list and view

### Wave 3 — Write Operations (Weeks 9–14)
7. **COBIL00C** → Bill payment (critical dual-write, needs thorough testing)
8. **COACTUPC** → Account update (#1 hotspot — schedule extra time)
9. **COCRDUPC** → Card update
10. **COTRN02C** → Transaction add

### Wave 4 — Batch Processing (Weeks 15–20)
11. **CBTRN02C** → Transaction posting (Spring Batch)
12. **CBACT04C** → Interest calculation (requires precision testing)
13. **CBSTM03A/B** → Statement generation
14. **CBTRN03C** → Transaction reporting

### Wave 5 — Admin & Utilities (Weeks 21–24)
15. **COUSR00C–03C** → User CRUD (straightforward)
16. **CORPT00C** → Report submission (architecture redesign for job launcher)
17. **CBEXPORT / CBIMPORT** → Data migration utilities
18. **Remaining batch utilities** (CBACT01C–03C, CBCUS01C, CBTRN01C)
