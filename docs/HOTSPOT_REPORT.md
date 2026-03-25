# CardDemo Hotspot Report

> **Generated**: 2026-03-25 | **Application**: CardDemo (AWS Mainframe Credit Card Management)
> **Methodology**: Static analysis of LOC, cyclomatic complexity proxies (IF/EVALUATE/PERFORM counts), data coupling, CICS operations, and business criticality

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Modules](#top-10-hotspot-modules)
3. [Detailed Analysis Per Module](#detailed-analysis-per-module)
4. [Risk Heat Map](#risk-heat-map)
5. [Recommended Modernization Sequence](#recommended-modernization-sequence)

---

## Scoring Methodology

Each module is scored across three dimensions on a 1–10 scale:

| Dimension | Weight | Factors |
|-----------|--------|---------|
| **Complexity** | 40% | Lines of code, PERFORM count, IF/EVALUATE branching, COPY includes, CALL depth, CICS operations |
| **Risk** | 30% | Data files written, shared data contention, error handling patterns, security concerns, COPY REPLACING usage |
| **Business Impact** | 30% | Revenue criticality, user-facing exposure, downstream dependencies, data volume processed |

**Composite Score** = (Complexity x 0.4) + (Risk x 0.3) + (Business Impact x 0.3)

### Complexity Metrics Collected

| Metric | Description |
|--------|-------------|
| LOC | Lines of code (including comments) |
| PERFORM | Number of PERFORM statements (subroutine calls) |
| IF | Number of conditional branches |
| EVALUATE | Number of EVALUATE (switch/case) statements |
| COPY | Number of copybook includes |
| CICS Ops | Number of EXEC CICS commands |
| CALL | Number of external program calls |

---

## Top 10 Hotspot Modules

| Rank | Program | LOC | Complexity | Risk | Business Impact | **Composite** | Domain |
|------|---------|-----|-----------|------|-----------------|---------------|--------|
| **1** | **COACTUPC** | 4,236 | **10** | **9** | **9** | **9.4** | Account Update |
| **2** | **CBTRN02C** | 731 | **8** | **9** | **10** | **8.9** | Transaction Posting |
| **3** | **CBACT04C** | 652 | **8** | **8** | **10** | **8.6** | Interest Calculation |
| **4** | **COCRDUPC** | 1,560 | **9** | **7** | **7** | **7.8** | Card Update |
| **5** | **COCRDLIC** | 1,459 | **8** | **6** | **7** | **7.1** | Card List |
| **6** | **CBSTM03A** | 924 | **7** | **7** | **8** | **7.3** | Statement Generation |
| **7** | **CBTRN03C** | 649 | **7** | **6** | **7** | **6.7** | Transaction Report |
| **8** | **COTRN02C** | 783 | **7** | **7** | **6** | **6.7** | Transaction Add |
| **9** | **COBIL00C** | 572 | **6** | **7** | **8** | **6.9** | Bill Payment |
| **10** | **COSGN00C** | 260 | **4** | **8** | **9** | **6.7** | Signon/Auth |

---

## Detailed Analysis Per Module

### #1 — COACTUPC (Account Update) — Score: 9.4

| Metric | Value | Assessment |
|--------|-------|------------|
| **LOC** | 4,236 | Largest program by far (2x the next biggest) |
| **PERFORM** | 64 | High procedural complexity |
| **IF** | 164 | Extremely high branching — most conditional program |
| **EVALUATE** | 10 | Multiple state machines |
| **COPY** | 17 unique + 39 REPLACING | Heaviest copybook usage in entire codebase |
| **CICS Ops** | READ(5), SEND(2), RECEIVE(1), XCTL(1), RETURN(1) | Multi-file read with update |
| **Files Accessed** | ACCTDATA (R/U), CUSTDATA (R/U), CARDXREF (R) | Writes to 2 critical VSAM files |

**Why It's #1**:
- **Complexity**: At 4,236 LOC it is over 2x larger than any other program. Contains 164 IF statements — the highest conditional density in the codebase. Uses `COPY CSSETATY REPLACING` 39 times for dynamic screen attribute manipulation, creating massive macro expansion.
- **Risk**: Directly updates Account Master and Customer Master — the two most critical data stores. A bug here could corrupt financial balances. Uses the most copybooks (17+) creating broad coupling.
- **Business Impact**: Account update is a core business function. Every account modification flows through this program. Changes to account balances, credit limits, and customer data all route here.

**Modernization Recommendations**:
1. Decompose into separate services: AccountUpdateService, CustomerUpdateService, ScreenValidationService
2. Extract the 39 CSSETATY REPLACING blocks into a generic attribute-setting utility
3. Replace CICS screen attribute logic with CSS/frontend framework styling
4. Add unit tests for each of the 164 conditional paths
5. Estimated effort: **High** (3-4 weeks)

---

### #2 — CBTRN02C (Transaction Posting) — Score: 8.9

| Metric | Value | Assessment |
|--------|-------|------------|
| **LOC** | 731 | Medium-large for a batch program |
| **PERFORM** | 61 | High procedural count |
| **IF** | 48 | Significant branching |
| **EVALUATE** | 0 | Procedural rather than state-based |
| **Files Accessed** | DALYTRAN(R), TRANSACT(RW), ACCTDATA(RU), CARDXREF(R), TCATBALF(RU) | 5 files, 3 with write access |
| **JCL** | POSTTRAN | Core daily batch job |

**Why It's #2**:
- **Complexity**: Reads from 5 different VSAM files and writes/updates 3. Complex validation and posting logic with 48 conditional branches.
- **Risk**: The heart of the daily batch cycle. Updates Account Master balances, Transaction Master, and Category Balance files simultaneously. A failure mid-run leaves data in an inconsistent state with no built-in rollback mechanism. Rejects written to GDG for audit trail.
- **Business Impact**: Every daily transaction flows through this program. Financial accuracy of the entire system depends on CBTRN02C working correctly. Downstream programs (INTCALC, CREASTMT, TRANREPT) all depend on its output.

**Modernization Recommendations**:
1. Convert to Spring Batch job with chunk-oriented processing and database transactions
2. Implement proper rollback/compensation logic (COBOL has no transaction support across VSAM files)
3. Add idempotency support (safe to re-run)
4. Break into: TransactionValidator, TransactionPoster, BalanceUpdater services
5. Estimated effort: **High** (3-4 weeks)

---

### #3 — CBACT04C (Interest Calculation) — Score: 8.6

| Metric | Value | Assessment |
|--------|-------|------------|
| **LOC** | 652 | Medium-large |
| **PERFORM** | 56 | High — iterative processing |
| **IF** | 43 | Complex rate determination logic |
| **EVALUATE** | 0 | Procedural |
| **Files Accessed** | ACCTDATA(RU), CARDXREF+AIX(R), DISCGRP(R), TCATBALF(RU), SYSTRAN(W) | 5 files including alt index |
| **JCL** | INTCALC | Daily interest calculation job |

**Why It's #3**:
- **Complexity**: Iterates through accounts, looks up interest rates by category from DISCGRP, calculates compound interest, and generates interest transaction entries. Uses the CARDXREF alternate index path for reverse lookups (account -> cards).
- **Risk**: Financial calculation accuracy is paramount. Interest rate lookup involves a 3-way join (Account -> Cross-Ref -> Disclosure Group). Updates account balances directly. Generated interest transactions feed into SYSTRAN GDG which is combined with regular transactions.
- **Business Impact**: Directly affects revenue (interest income) and customer billing. Regulatory compliance requires accurate interest calculations. Errors compound over time and are difficult to detect.

**Modernization Recommendations**:
1. Implement as a dedicated InterestCalculationService with BigDecimal arithmetic
2. Make interest rate rules configurable (currently hardcoded via DISCGRP VSAM)
3. Add comprehensive audit logging for every calculation
4. Implement validation/reconciliation reports
5. Estimated effort: **High** (2-3 weeks)

---

### #4 — COCRDUPC (Card Update) — Score: 7.8

| Metric | Value | Assessment |
|--------|-------|------------|
| **LOC** | 1,560 | Second-largest online program |
| **PERFORM** | 26 | Moderate |
| **IF** | 72 | High branching for field validation |
| **EVALUATE** | 16 | Multiple state transitions |
| **COPY** | 12 | Heavy copybook coupling |
| **CICS Ops** | READ(2), SEND(2), RECEIVE(1), REWRITE, XCTL, RETURN | Full CRUD cycle |
| **Files Accessed** | CARDDATA(R/U), CUSTDATA(R) | Card master update |

**Why It's #4**:
- **Complexity**: 72 IF statements for extensive field-level validation (card number format, CVV, expiration dates, embossed name). 16 EVALUATE blocks managing screen state transitions.
- **Risk**: Updates card data including CVV — security-sensitive field. Validation logic is complex and interleaved with screen management.
- **Business Impact**: Card updates affect active card status, expiration dates, and embossed names. Errors could render cards unusable.

**Modernization Recommendations**:
1. Separate validation logic from screen management
2. Implement card data encryption at rest
3. Add PCI-DSS compliant audit trail for card modifications
4. Estimated effort: **Medium** (2 weeks)

---

### #5 — COCRDLIC (Card List) — Score: 7.1

| Metric | Value | Assessment |
|--------|-------|------------|
| **LOC** | 1,459 | Third-largest program |
| **PERFORM** | 34 | Moderate-high |
| **IF** | 59 | High branching |
| **EVALUATE** | 18 | Highest EVALUATE count in codebase |
| **CICS Ops** | READ(4), STARTBR(2), XCTL(3), SEND(3), RETURN(3), RECEIVE(1), ENDBR(1) | Complex browse operations |
| **Files Accessed** | CARDDATA (R/Browse) | VSAM browse with pagination |

**Why It's #5**:
- **Complexity**: Implements VSAM browse (STARTBR/READNEXT/READPREV/ENDBR) with forward and backward pagination — one of the most complex CICS patterns. 18 EVALUATE blocks handle multiple screen states and navigation options. 3 XCTL calls route to card view and card update programs.
- **Risk**: Browse logic is stateful — relies on COMMAREA to maintain cursor position between screen interactions. Pagination edge cases (first page, last page, empty results) require careful handling.
- **Business Impact**: Primary card search/list interface. All card operations begin here. Gateway to COCRDSLC (view) and COCRDUPC (update).

**Modernization Recommendations**:
1. Replace VSAM browse with SQL pagination (OFFSET/LIMIT or cursor-based)
2. Extract navigation routing into a separate controller
3. Implement as a paginated REST endpoint with query parameters
4. Estimated effort: **Medium** (2 weeks)

---

### #6 — CBSTM03A (Statement Generation) — Score: 7.3

| Metric | Value | Assessment |
|--------|-------|------------|
| **LOC** | 924 | Large batch program |
| **PERFORM** | 29 | Moderate |
| **IF** | 15 | Low branching |
| **EVALUATE** | 5 | Moderate |
| **CALL** | CBSTM03B x13 | Heavy subroutine usage — calls CBSTM03B 13 times |
| **Files Accessed** | TRXFL(R), CARDXREF(R), ACCTDATA(R), CUSTDATA(R), STATEMNT(W), HTML(W) | 4 input, 2 output |
| **JCL** | CREASTMT | Multi-step job with SORT + IDCAMS pre-processing |

**Why It's #6**:
- **Complexity**: Joins 4 VSAM files to produce statements. Calls CBSTM03B (file I/O subroutine) 13 times with different operation modes (READ/WRITE/REWRITE). Generates both plain text and HTML output simultaneously. Uses mainframe control block addressing.
- **Risk**: Depends on SORT pre-processing to re-key transactions by card number (CREASTMT JCL Step010). If the SORT step fails or re-keys incorrectly, statements will be wrong. Dual output format (text + HTML) doubles the surface area for formatting bugs.
- **Business Impact**: Customer-facing output — statements are sent to cardholders. Accuracy and formatting directly affect customer experience and regulatory compliance.

**Modernization Recommendations**:
1. Replace CBSTM03A + CBSTM03B with a single StatementGenerationService
2. Use a template engine (Thymeleaf/FreeMarker) for HTML generation
3. Replace SORT pre-processing with SQL ORDER BY
4. Add PDF generation natively (currently requires separate TXT2PDF1 job)
5. Estimated effort: **Medium-High** (2-3 weeks)

---

### #7 — CBTRN03C (Transaction Report) — Score: 6.7

| Metric | Value | Assessment |
|--------|-------|------------|
| **LOC** | 649 | Medium |
| **PERFORM** | 72 | Highest PERFORM count in entire codebase |
| **IF** | 38 | Moderate-high |
| **EVALUATE** | 4 | Low |
| **COPY** | 5 (CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y) | Multiple entity types |
| **Files Accessed** | TRANSACT(R), CARDXREF(R), TRANTYPE(R), TRANCATG(R), DATEPARM(R), TRANREPT(W), TRANSACT.DALY(W) | 5 input, 2 output |
| **JCL** | TRANREPT | Multi-step with SORT pre-processing |

**Why It's #7**:
- **Complexity**: 72 PERFORM statements — highest in the codebase — indicate deeply nested procedural logic. Joins 5 input sources and produces 2 output files. Uses report layout copybook (CVTRA07Y) with page/account/grand totals.
- **Risk**: Date parameter input controls which transactions are selected. Incorrect date parsing could produce incomplete or empty reports. Report totals must reconcile with transaction posting.
- **Business Impact**: Daily operational report used for reconciliation and audit. Management relies on this for business metrics.

**Modernization Recommendations**:
1. Convert to a reporting service using JasperReports or similar
2. Replace file-based date parameters with API request parameters
3. Store report data in a reporting database for ad-hoc queries
4. Estimated effort: **Medium** (2 weeks)

---

### #8 — COTRN02C (Transaction Add) — Score: 6.7

| Metric | Value | Assessment |
|--------|-------|------------|
| **LOC** | 783 | Medium-large |
| **PERFORM** | 61 | High |
| **IF** | 14 | Low branching (validation delegated) |
| **EVALUATE** | 13 | High state management |
| **CALL** | CSUTLDTC x2 | Date validation calls |
| **CICS Ops** | READ(3), WRITE(1), STARTBR(1), RETURN(2), SEND(1), RECEIVE(1), ENDBR(1) | Multi-file transaction creation |
| **Files Accessed** | TRANSACT(RW), ACCTDATA(R), CARDXREF(R) | Writes new transactions |

**Why It's #8**:
- **Complexity**: 13 EVALUATE blocks manage complex screen state transitions. Validates input by reading Account and Cross-Reference files before writing to Transaction Master. Uses STARTBR/READPREV to check for duplicate transaction IDs.
- **Risk**: Writes to the Transaction Master (TRANSACT.VSAM.KSDS) — the highest-contention file. Must validate card-to-account mapping via cross-reference. Date validation via CSUTLDTC call adds external dependency.
- **Business Impact**: Primary mechanism for adding transactions online. Every manual transaction entry flows through this program.

**Modernization Recommendations**:
1. Implement as a TransactionService.createTransaction() with validation pipeline
2. Replace VSAM duplicate check with database unique constraint
3. Use Java date libraries instead of CSUTLDTC/CEEDAYS chain
4. Estimated effort: **Medium** (1-2 weeks)

---

### #9 — COBIL00C (Bill Payment) — Score: 6.9

| Metric | Value | Assessment |
|--------|-------|------------|
| **LOC** | 572 | Medium |
| **PERFORM** | 38 | Moderate |
| **IF** | 10 | Low |
| **EVALUATE** | 9 | Moderate state management |
| **CICS Ops** | READ(3), WRITE(1), REWRITE(1), STARTBR(1), SEND(1), RETURN(1), RECEIVE(1), ENDBR(1) | Full payment cycle |
| **Files Accessed** | TRANSACT(RW), ACCTDATA(R/U), CARDXREF(R) | Creates transaction + updates balance |

**Why It's #9**:
- **Complexity**: Moderate overall, but performs a complete financial operation in a single program: reads card cross-reference, validates account, creates a transaction record, and updates account balance.
- **Risk**: **Dual-write pattern** — writes to TRANSACT and updates ACCTDATA in the same logical operation without CICS Unit of Work. If the REWRITE to ACCTDATA fails after the WRITE to TRANSACT succeeds, balances become inconsistent.
- **Business Impact**: Direct revenue impact — bill payments are a core business function. Payment errors immediately affect customer accounts and require manual correction.

**Modernization Recommendations**:
1. Wrap payment logic in a database transaction (ACID guarantees)
2. Implement as PaymentService with proper compensation/rollback
3. Add payment confirmation/receipt generation
4. Estimated effort: **Medium** (1-2 weeks)

---

### #10 — COSGN00C (Signon/Authentication) — Score: 6.7

| Metric | Value | Assessment |
|--------|-------|------------|
| **LOC** | 260 | Small |
| **PERFORM** | 11 | Low |
| **IF** | 4 | Very low branching |
| **EVALUATE** | 3 | Low |
| **CICS Ops** | READ(1), SEND(2), XCTL(2), RETURN(2), RECEIVE(1) | Simple authentication |
| **Files Accessed** | USRSEC (R) | Reads user credentials |

**Why It's #10**:
- **Complexity**: Low — simple read-and-compare authentication logic. Small program with minimal branching.
- **Risk**: **Critical security vulnerability** — compares plain-text passwords stored in USRSEC.VSAM. No encryption, no hashing, no account lockout, no session timeout management. The COMMAREA carries user type (A/U) in plain text, which controls all downstream authorization. A compromised COMMAREA means full admin access.
- **Business Impact**: **Gateway to the entire application**. Every user interaction starts here. Authentication bypass would expose all functions and data. Despite low complexity, its security role makes it a top-10 hotspot.

**Modernization Recommendations**:
1. Implement proper authentication (Spring Security / OAuth 2.0 / OIDC)
2. Password hashing with BCrypt and salt
3. Add account lockout after failed attempts
4. Implement session management with JWT tokens
5. Add multi-factor authentication support
6. Estimated effort: **Medium** (1-2 weeks, but critical path)

---

## Risk Heat Map

### Complexity vs. Business Impact Matrix

```
Business Impact
     HIGH │  COSGN00C    CBTRN02C  CBACT04C
          │  (security)  (posting) (interest)
          │
          │  COBIL00C    COACTUPC  CBSTM03A
          │  (payment)   (acct upd)(stmts)
          │
     MED  │  COTRN02C    COCRDUPC  CBTRN03C
          │  (tran add)  (card upd)(reports)
          │
          │               COCRDLIC
          │               (card list)
          │
     LOW  │  COUSR01C    COACTVWC  
          │  (add user)  (acct view)
          │
          └──────────────────────────────────
              LOW         MEDIUM       HIGH
                      Complexity
```

### Data Contention Risk

| Risk Level | Files | Affected Programs |
|------------|-------|-------------------|
| **CRITICAL** | TRANSACT.VSAM.KSDS | COTRN02C, COBIL00C, CBTRN02C (writers) + 6 readers |
| **HIGH** | ACCTDATA.VSAM.KSDS | COACTUPC, CBTRN02C, CBACT04C (writers) + 6 readers |
| **MEDIUM** | TCATBALF.VSAM.KSDS | CBTRN02C, CBACT04C (both read and update) |
| **LOW** | CARDDATA, CARDXREF, CUSTDATA, USRSEC | Limited writers, mostly read-only |

### Security Risk Summary

| Risk | Location | Severity | Remediation |
|------|----------|----------|-------------|
| Plain-text passwords | CSUSR01Y / COSGN00C | **CRITICAL** | Hash with BCrypt |
| CVV stored in clear | CVACT02Y / COCRDUPC | **HIGH** | Encrypt at rest, tokenize |
| SSN stored in clear | CVCUS01Y / COACTUPC | **HIGH** | AES-256 encryption |
| No session management | COMMAREA-based auth | **HIGH** | JWT/session tokens |
| No input sanitization | All online programs | **MEDIUM** | Validation framework |
| No audit logging | All programs | **MEDIUM** | Add audit trail |

---

## Recommended Modernization Sequence

### Phase 1: Foundation (Weeks 1-4)

| Order | Module | Rationale |
|-------|--------|-----------|
| 1.1 | **COSGN00C** + **CSUSR01Y** | Security foundation — must be modernized first. All other modules depend on authentication. |
| 1.2 | **COCOM01Y** (COMMAREA) | Convert session state to JWT/database sessions. Required before any other online module. |
| 1.3 | **COMEN01C** + **COADM01C** | Navigation framework — simple programs that establish the routing pattern for all other screens. |

### Phase 2: Core Business Logic (Weeks 5-10)

| Order | Module | Rationale |
|-------|--------|-----------|
| 2.1 | **CBTRN02C** (Transaction Posting) | Highest business impact batch program. Establishes the pattern for all batch modernization. |
| 2.2 | **CBACT04C** (Interest Calculation) | Revenue-critical financial logic. Must be modernized with extreme accuracy testing. |
| 2.3 | **COBIL00C** (Bill Payment) | High business impact with dual-write risk. Benefits from database transaction support. |
| 2.4 | **COTRN02C** (Transaction Add) | Online transaction entry — pairs with CBTRN02C for complete transaction lifecycle. |

### Phase 3: Account & Card Management (Weeks 11-16)

| Order | Module | Rationale |
|-------|--------|-----------|
| 3.1 | **COACTUPC** (Account Update) | Largest and most complex program. Requires the most decomposition work. |
| 3.2 | **COCRDUPC** (Card Update) | Second-most complex online program. Security-sensitive (CVV handling). |
| 3.3 | **COCRDLIC** + **COCRDSLC** | Card list/view — establishes pagination pattern. |
| 3.4 | **COACTVWC** | Account view — simpler, read-only version of COACTUPC. |

### Phase 4: Reporting & Data Management (Weeks 17-20)

| Order | Module | Rationale |
|-------|--------|-----------|
| 4.1 | **CBSTM03A** + **CBSTM03B** | Statement generation — customer-facing output. |
| 4.2 | **CBTRN03C** + **CORPT00C** | Transaction reporting pipeline. |
| 4.3 | **CBEXPORT** + **CBIMPORT** | Data migration utilities — useful for parallel-run validation. |

### Phase 5: Administration & Utilities (Weeks 21-22)

| Order | Module | Rationale |
|-------|--------|-----------|
| 5.1 | **COUSR00C-03C** | User management — straightforward CRUD, low complexity. |
| 5.2 | **COTRN00C** + **COTRN01C** | Transaction list/view — read-only, low risk. |
| 5.3 | **Remaining utilities** | CSUTLDTC, COBSWAIT, CBACT01-03C, CBCUS01C, CBTRN01C |

### Estimated Total Effort

| Phase | Weeks | Programs | LOC |
|-------|-------|----------|-----|
| Phase 1: Foundation | 4 | 5 | ~1,200 |
| Phase 2: Core Business | 6 | 4 | ~2,700 |
| Phase 3: Account & Card | 6 | 5 | ~9,700 |
| Phase 4: Reporting | 4 | 5 | ~2,800 |
| Phase 5: Admin & Utility | 2 | 12 | ~4,250 |
| **Total** | **22** | **31** | **20,650** |

> **Note**: Estimates assume a team of 2-3 experienced Java developers familiar with mainframe concepts. Add 20-30% buffer for testing, parallel-run validation, and unexpected issues. JCL jobs, BMS maps, and copybooks are implicitly covered by program modernization.
