# HOTSPOT REPORT — CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Methodology:** Weighted scoring across Complexity, Risk, and Business Impact dimensions

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Modules](#top-10-hotspot-modules)
3. [Detailed Module Assessments](#detailed-module-assessments)
4. [Modernization Priority Matrix](#modernization-priority-matrix)
5. [Risk Summary](#risk-summary)

---

## Scoring Methodology

Each module is scored on three dimensions (1–5 scale), then combined with weights:

| Dimension | Weight | Criteria |
|-----------|--------|----------|
| **Complexity** | 35% | Lines of code, cyclomatic complexity indicators (EVALUATE/IF nesting depth), number of CICS commands, file I/O operations, COPY dependencies, PERFORM paragraphs |
| **Risk** | 35% | Data integrity impact (financial writes), error handling quality, coupling to other modules, ABEND handling, security sensitivity |
| **Business Impact** | 30% | Revenue/regulatory criticality, user-facing importance, batch cycle centrality, downstream dependencies |

**Score Formula:** `Total = (Complexity × 0.35) + (Risk × 0.35) + (Business Impact × 0.30)`

---

## Top 10 Hotspot Modules

| Rank | Module | LOC | Complexity | Risk | Biz Impact | **Total** | Primary Concern |
|:----:|--------|----:|:----------:|:----:|:----------:|:---------:|-----------------|
| **1** | **COACTUPC** | 4,236 | 5 | 5 | 5 | **5.00** | Largest program; financial account updates with multi-file writes |
| **2** | **CBTRN02C** | 731 | 4 | 5 | 5 | **4.65** | Core batch posting; writes to 4 files; balance mutations |
| **3** | **CBACT04C** | 652 | 4 | 5 | 5 | **4.65** | Interest calculation; financial math; balance rewrites |
| **4** | **COCRDUPC** | 1,560 | 5 | 4 | 4 | **4.35** | Card update with ABEND handling; complex screen flow |
| **5** | **CBSTM03A** | 924 | 4 | 3 | 5 | **3.95** | Statement gen; calls subroutine; HTML+text dual output |
| **6** | **COCRDLIC** | 1,459 | 4 | 3 | 4 | **3.65** | Large browse program; complex pagination logic |
| **7** | **COBIL00C** | 572 | 3 | 5 | 4 | **3.90** | Bill payment; financial writes to account + transaction |
| **8** | **COTRN02C** | 783 | 4 | 4 | 3 | **3.70** | Transaction add; writes to TRANSACT; multi-file validation |
| **9** | **COTRN00C** | 699 | 4 | 3 | 3 | **3.35** | Transaction list browse; complex STARTBR/READNEXT logic |
| **10** | **CBTRN03C** | 649 | 3 | 3 | 4 | **3.30** | Report generation; joins 5 reference files; report layout |

---

## Detailed Module Assessments

### #1 — COACTUPC (Account Update) — Score: 5.00

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 4,236 — **largest program in the entire codebase** |
| **Type** | Online CICS |
| **VSAM Files Accessed** | ACCTDATA (R/W), CUSTDATA (R), CARDXREF via AIX (R) |
| **COPY Dependencies** | 16 copybooks including COCOM01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y |
| **CICS Commands** | SEND MAP, RECEIVE, READ, REWRITE, XCTL, HANDLE ABEND, ABEND |
| **BMS Map** | COACTUP (460 lines — largest map) |

**Why It's #1:**
- Highest LOC in the codebase by a factor of 2.7x over the next largest core program
- Performs **financial writes** (REWRITE on ACCTDATA) — a balance mutation
- Complex multi-screen workflow with field-level validation
- Has explicit ABEND handling (both HANDLE ABEND and forced ABEND)
- Reads from 3 VSAM files across account, customer, and cross-reference domains
- Any bug in this module can directly corrupt account balances

**Modernization Recommendations:**
- Decompose into smaller service methods (validation, read, update, screen handling)
- Extract financial update logic into a separate transactional service
- Add unit test coverage for all balance-mutation paths
- Map to a REST API with ACID transaction guarantees

---

### #2 — CBTRN02C (Transaction Posting) — Score: 4.65

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 731 |
| **Type** | Batch |
| **Files Accessed** | DAILYTRAN (R), TRANSACT (W), ACCTDATA (R/W), CARDXREF (R), TCATBAL (R/W), DALYREJS (W) |
| **CALL Dependencies** | CSUTLDTC (date utility) |

**Why It's #2:**
- **Heart of the nightly batch cycle** — processes all daily transactions
- Writes to **4 different files** in a single run (TRANSACT, ACCTDATA, TCATBAL, DALYREJS)
- Mutates account balances (REWRITE on ACCTDATA) and category balances (REWRITE on TCATBAL)
- Handles rejected transactions with a separate output file
- Any failure here blocks the entire downstream batch cycle (interest calc, statements, indexes)
- Complex file status checking and error routing logic

**Modernization Recommendations:**
- Convert to Spring Batch job with chunk-oriented processing
- Implement proper transaction management (commit intervals)
- Add idempotency checks to prevent double-posting
- Create comprehensive integration tests with sample daily transaction data

---

### #3 — CBACT04C (Interest Calculation) — Score: 4.65

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 652 |
| **Type** | Batch |
| **Files Accessed** | TCATBAL (R/W), ACCTDATA (R/W), DISCGRP (R), CARDXREF (R), TRANSACT (R) |
| **CALL Dependencies** | CSUTLDTC (date utility) |

**Why It's #3:**
- **Critical financial calculation** — computes interest charges on all accounts
- Reads disclosure group rates and applies them to category balances
- Writes interest transactions to TRANSACT and updates ACCTDATA balances
- Date-sensitive logic (interest accrual periods) via CSUTLDTC calls
- Regulatory implications — incorrect interest calculation = compliance violation
- Complex business rules joining 5 different files

**Modernization Recommendations:**
- Extract interest calculation rules into a testable business-rule engine
- Parameterize calculation dates (currently hardcoded in JCL PARM)
- Add audit logging for every interest charge generated
- Implement decimal-precision testing (COBOL `PIC S9(10)V99` → Java `BigDecimal`)

---

### #4 — COCRDUPC (Credit Card Update) — Score: 4.35

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 1,560 |
| **Type** | Online CICS |
| **VSAM Files Accessed** | CARDDATA (R/W), CUSTDATA (R) |
| **CICS Commands** | SEND MAP, RECEIVE, READ, REWRITE, HANDLE ABEND, ABEND |
| **BMS Map** | COCRDUP (323 lines) |

**Why It's #4:**
- Second-largest online program
- **Writes to card master file** — card status changes, embossed name updates
- Has explicit ABEND handling (both defensive HANDLE ABEND and forced ABEND escalation)
- Complex multi-screen workflow similar to COACTUPC
- PCI-DSS implications — card data modifications require audit trail

**Modernization Recommendations:**
- Implement field-level audit logging for all card data changes
- Add PCI-DSS compliant masking for card numbers in logs/screens
- Separate read-only card view from mutable card update logic
- Add role-based authorization checks

---

### #5 — CBSTM03A (Statement Generation) — Score: 3.95

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 924 |
| **Type** | Batch |
| **Files Accessed** | TRXFL (R, sorted copy of TRANSACT), CARDXREF (R), ACCTDATA (R), CUSTDATA (R) |
| **CALL Dependencies** | CBSTM03B (file processing subroutine) |

**Why It's #5:**
- **Customer-facing output** — generates the actual account statements
- Dual output format: plain text (.PS) and HTML
- Calls subroutine CBSTM03B for file processing — the only CALL-based modular design in batch
- Joins 4 files to produce per-card, per-account statement detail
- Statement errors directly impact customer experience and regulatory compliance

**Modernization Recommendations:**
- Convert to a template-based report engine (e.g., JasperReports or Thymeleaf)
- Replace file-based join with SQL queries against modernized database
- Add PDF generation capability (currently requires separate TXT2PDF1 JCL)
- Implement statement archival and retrieval service

---

### #6 — COCRDLIC (Credit Card List) — Score: 3.65

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 1,459 |
| **Type** | Online CICS |
| **VSAM Files Accessed** | CARDDATA (R, browse) |
| **BMS Map** | COCRDLI (394 lines) |

**Why It's #6:**
- Third-largest program overall — complex browse/pagination logic
- CICS STARTBR/READNEXT/READPREV/ENDBR browse operations
- Manages a 10-item scrollable list with forward/backward navigation
- Screen selection logic routes to detail view or update screens
- Complex state management across pseudo-conversational CICS interactions

**Modernization Recommendations:**
- Replace browse logic with paginated REST API (offset/limit or cursor-based)
- Implement server-side sorting and filtering
- Map BMS list layout to a modern data grid component

---

### #7 — COBIL00C (Bill Payment) — Score: 3.90

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 572 |
| **Type** | Online CICS |
| **VSAM Files Accessed** | ACCTDATA (R/W), TRANSACT (R/W), CXACAIX (R) |

**Why It's #7:**
- **Financial write path** — processes bill payments in real time
- Writes to both ACCTDATA (balance update) and TRANSACT (payment record)
- Reads account via card cross-reference alternate index (CXACAIX)
- Uses STARTBR/READPREV to find the last transaction for sequence numbering
- Mid-ranked LOC but very high risk due to dual financial writes

**Modernization Recommendations:**
- Wrap payment processing in an ACID transaction
- Add payment amount validation (negative, zero, exceeds-balance checks)
- Implement payment confirmation workflow
- Add real-time fraud screening integration point

---

### #8 — COTRN02C (Transaction Add) — Score: 3.70

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 783 |
| **Type** | Online CICS |
| **VSAM Files Accessed** | TRANSACT (R/W), CARDXREF (R), CXACAIX (R) |
| **BMS Map** | COTRN02 (239 lines) |

**Why It's #8:**
- Adds new transactions to the master file in real time
- Multi-file validation: checks card exists in XREF before allowing transaction
- Uses STARTBR/READPREV + ENDBR for transaction ID sequence generation
- Complex screen flow with field validation and error messaging

**Modernization Recommendations:**
- Convert to transaction creation REST endpoint with validation service
- Implement optimistic locking for concurrent transaction creation
- Add merchant validation and transaction limits

---

### #9 — COTRN00C (Transaction List) — Score: 3.35

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 699 |
| **Type** | Online CICS |
| **VSAM Files Accessed** | TRANSACT (R, browse) |
| **BMS Map** | COTRN00 (378 lines) |

**Why It's #9:**
- Complex browse program with 10-item scrollable list
- Bidirectional pagination (READNEXT + READPREV)
- Selection routing to transaction view (COTRN01C) or transaction add (COTRN02C)
- State management across pseudo-conversational interactions

**Modernization Recommendations:**
- Replace with paginated transaction search API
- Add date range, amount range, and merchant filtering
- Implement server-side sorting

---

### #10 — CBTRN03C (Transaction Report) — Score: 3.30

| Metric | Detail |
|--------|--------|
| **Lines of Code** | 649 |
| **Type** | Batch |
| **Files Accessed** | TRANSACT (R), CARDXREF (R), TRANTYPE (R), TRANCATG (R) |
| **Report Copybook** | CVTRA07Y (73 lines — detailed report layout) |

**Why It's #10:**
- Joins 5 reference files to produce the Daily Transaction Report
- Complex report formatting with page totals, account totals, grand totals
- Uses CVTRA07Y for structured report output layout
- Triggered online via CORPT00C (TDQ submission) or directly via TRANREPT JCL

**Modernization Recommendations:**
- Convert to a reporting service with parameterized date ranges
- Replace file joins with SQL queries
- Output as PDF/CSV instead of fixed-width text
- Add email/notification delivery capability

---

## Modernization Priority Matrix

Based on the hotspot analysis, here is the recommended modernization sequencing:

### Wave 1 — Critical Financial Core (Highest Risk)

| Priority | Module | Rationale |
|----------|--------|-----------|
| 1.1 | CBTRN02C | Batch posting is the foundation — modernize first to validate data pipeline |
| 1.2 | CBACT04C | Interest calculation has regulatory implications |
| 1.3 | COBIL00C | Real-time payment processing — customer-facing financial writes |

### Wave 2 — Account & Card Management (Highest Complexity)

| Priority | Module | Rationale |
|----------|--------|-----------|
| 2.1 | COACTUPC | Largest program; requires decomposition before migration |
| 2.2 | COCRDUPC | Card updates with PCI-DSS implications |
| 2.3 | COTRN02C | Transaction creation — integrates with posting pipeline |

### Wave 3 — Reporting & Statements (Customer-Facing Output)

| Priority | Module | Rationale |
|----------|--------|-----------|
| 3.1 | CBSTM03A + CBSTM03B | Statement generation — direct customer impact |
| 3.2 | CBTRN03C | Transaction reporting — operational visibility |

### Wave 4 — Browse & Navigation (High LOC, Lower Risk)

| Priority | Module | Rationale |
|----------|--------|-----------|
| 4.1 | COCRDLIC | Large but read-only — good candidate for API-first migration |
| 4.2 | COTRN00C | Read-only browse — straightforward REST API conversion |

### Wave 5 — Security, Menus & Utilities (Foundation)

| Priority | Module | Rationale |
|----------|--------|-----------|
| 5.1 | COSGN00C | Authentication — replace with modern IAM |
| 5.2 | COMEN01C + COADM01C | Menu navigation — replaced by web UI routing |
| 5.3 | COUSR00C–03C | User CRUD — straightforward Spring Security + JPA migration |

---

## Risk Summary

### Top Risks for Modernization

| # | Risk | Affected Modules | Mitigation |
|---|------|------------------|------------|
| 1 | **Financial data integrity** — COBOL packed decimal (PIC S9V99) → Java BigDecimal precision loss | COACTUPC, CBTRN02C, CBACT04C, COBIL00C | Comprehensive decimal comparison tests; use `BigDecimal` exclusively |
| 2 | **Batch ordering dependency** — strict sequence required (CLOSE → POST → INTEREST → BACKUP → COMBINE → STATEMENT → INDEX → OPEN) | All batch programs | Implement job orchestration (Spring Batch + scheduler) with dependency checks |
| 3 | **Pseudo-conversational state** — CICS COMMAREA state management has no direct equivalent in stateless REST | All online programs | Convert to session tokens or stateless request patterns |
| 4 | **VSAM key structure** — composite keys and alternate indexes need careful relational mapping | CARDXREF, TCATBAL, DISCGRP, TRANCATG | Design normalized schema with proper foreign keys and indexes |
| 5 | **Implicit file locking** — CICS VSAM provides record-level locking; REST APIs need explicit concurrency control | COACTUPC, COCRDUPC, COBIL00C | Implement optimistic locking (version columns) or pessimistic locking |
| 6 | **Report formatting** — Fixed-width COBOL report layouts need re-implementation | CBSTM03A, CBTRN03C | Adopt a template engine; verify totals match exactly |
| 7 | **Date handling** — COBOL uses CEEDAYS/CEEDATM LE routines and various date formats | CSUTLDTC, CBACT04C, CBTRN02C | Centralize on `java.time` API; comprehensive date conversion tests |
| 8 | **Security model** — Plaintext passwords in USRSEC VSAM file | COSGN00C, COUSR01C–03C | Replace with bcrypt/argon2 hashing and modern IAM |
| 9 | **Dual-format output** — Statement generation produces both text and HTML | CBSTM03A | Unify on HTML/PDF template; verify formatting parity |
| 10 | **Dead code** — UNUSED1Y copybook and commented COPY statements suggest code drift | Various | Audit and remove dead code before migration |
