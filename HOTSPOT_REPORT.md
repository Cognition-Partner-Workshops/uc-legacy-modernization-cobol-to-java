# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Application:** CardDemo - Credit Card Management System
> **Methodology:** Weighted scoring across code complexity, data coupling, business risk, and modernization effort

---

## Scoring Methodology

Each module is scored on four dimensions (1-10 scale):

| Dimension | Weight | Criteria |
|-----------|--------|----------|
| **Code Complexity** | 30% | Lines of code, nested IF/EVALUATE depth, PERFORM count, CICS command density |
| **Data Coupling** | 25% | Number of VSAM files accessed, copybooks included, cross-file operations |
| **Business Risk** | 25% | Financial impact, data integrity criticality, user-facing importance |
| **Modernization Effort** | 20% | Conversion difficulty, pattern complexity, testing surface area |

**Composite Score** = (Complexity x 0.30) + (Coupling x 0.25) + (Risk x 0.25) + (Effort x 0.20)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC — Account Update

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **4,236** | |
| IF Statements | 168 | |
| EVALUATE Statements | 20 | |
| PERFORM Statements | 64 | |
| VSAM Files Accessed | 5 (ACCTDATA, CARDDATA, CARDXREF, CUSTDATA, TCATBALF) | |
| Copybooks Included | 18 | |
| **Code Complexity** | | **10/10** |
| **Data Coupling** | | **9/10** |
| **Business Risk** | | **10/10** |
| **Modernization Effort** | | **10/10** |
| **Composite Score** | | **9.75** |

**Why #1:** This is the largest program in the entire codebase at 4,236 lines — nearly 3x the next-largest module. It performs read-modify-write operations across 5 VSAM files with complex validation logic, field-level attribute manipulation (CSSETATY), and CICS HANDLE ABEND error recovery. The account update function directly modifies financial balances and credit limits, making any conversion error a potential financial data integrity issue.

**Modernization Risks:**
- Complex multi-file transactional updates require careful JPA transaction boundary design
- BMS map field-by-field attribute control (color, protection, highlight) needs UI framework mapping
- CICS pseudo-conversational pattern with COMMAREA state must become stateful session or SPA state
- HANDLE ABEND logic needs equivalent Java exception handling strategy

**Recommended Approach:** Decompose into multiple service methods (AccountService, CardService). Implement as a multi-step wizard with Spring MVC form backing. Use `@Transactional` for atomic updates.

---

### Rank 2: COCRDUPC — Card Update

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **1,560** | |
| IF Statements | 148 | |
| EVALUATE Statements | 16 | |
| PERFORM Statements | 26 | |
| VSAM Files Accessed | 2 (CARDDATA + reads from others) | |
| Copybooks Included | 14 | |
| **Code Complexity** | | **8/10** |
| **Data Coupling** | | **7/10** |
| **Business Risk** | | **9/10** |
| **Modernization Effort** | | **8/10** |
| **Composite Score** | | **8.05** |

**Why #2:** Second-highest IF statement count (148) indicates dense validation logic for card data modifications. Shares the same HANDLE ABEND / XCTL navigation pattern as COACTUPC. Card status changes (activate/deactivate) have downstream impact on transaction processing.

**Modernization Risks:**
- Card number and CVV handling requires PCI-DSS compliance in the target system
- Complex field validation logic needs systematic extraction into a validation service
- Navigation state machine (XCTL to COCRDLIC, COCRDSLC, COMEN01C) needs REST endpoint mapping

**Recommended Approach:** Extract validation into `CardValidationService`. Implement card update as a REST endpoint with Spring Validation annotations. Add PCI-DSS field-level encryption.

---

### Rank 3: COCRDLIC — Card List

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **1,459** | |
| IF Statements | 122 | |
| EVALUATE Statements | 18 | |
| PERFORM Statements | 34 | |
| VSAM Files Accessed | 2 (CARDXREF, CARDDATA) | |
| XCTL Targets | 3 (COCRDSLC, COCRDUPC, COMEN01C) | |
| **Code Complexity** | | **8/10** |
| **Data Coupling** | | **6/10** |
| **Business Risk** | | **7/10** |
| **Modernization Effort** | | **8/10** |
| **Composite Score** | | **7.35** |

**Why #3:** Complex browse/pagination logic using CICS STARTBR/READNEXT/READPREV/ENDBR across two VSAM files with alternate index navigation. The 3270 list screen with 7 selectable rows, forward/backward paging, and row-level selection (view/update/delete) is one of the most complex UI patterns to convert.

**Modernization Risks:**
- VSAM sequential browse with page-up/page-down maps to paginated REST API with cursor-based pagination
- Row selection model (select one of 7 visible rows) needs frontend list/table component
- Multiple XCTL exit points complicate REST API design

**Recommended Approach:** Implement as a paginated `GET /api/cards` endpoint with Spring Data JPA `Pageable`. Frontend table component with action buttons per row.

---

### Rank 4: CBSTM03A — Statement Generation (Main)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **924** | |
| IF Statements | 15 | |
| EVALUATE Statements | 9 | |
| PERFORM Statements | 33 | |
| File I/O Operations | **101** (highest in codebase) | |
| CALL Statements | 14 (calls CBSTM03B) | |
| Files Accessed | 4 VSAM + 2 output files | |
| **Code Complexity** | | **7/10** |
| **Data Coupling** | | **9/10** |
| **Business Risk** | | **8/10** |
| **Modernization Effort** | | **9/10** |
| **Composite Score** | | **8.10** |

**Why #4:** Highest file I/O operation count (101) in the entire codebase. Reads from 4 VSAM files (TRXFL, XREFFILE, ACCTFILE, CUSTFILE), generates both text (STATEMNT.PS) and HTML (STATEMNT.HTML) output via the CBSTM03B subroutine. This is a multi-format report generator that crosses customer, account, card, and transaction domains.

**Modernization Risks:**
- SORT utility pre-processing (re-keying transactions by card number) needs SQL ORDER BY or application-level sort
- Dual output format (text + HTML) needs a templating engine (Thymeleaf, JasperReports)
- Control break logic (per-card, per-account totals) requires careful business logic preservation
- Called subroutine pattern (CALL CBSTM03B) maps to method extraction

**Recommended Approach:** Implement as a Spring Batch job with `ItemReader` (JPA query), `ItemProcessor` (control break logic), and `ItemWriter` (PDF/HTML template). Use JasperReports or Apache PDFBox.

---

### Rank 5: CBTRN02C — Transaction Posting (Full)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **731** | |
| IF Statements | 93 | |
| EVALUATE Statements | 0 | |
| PERFORM Statements | 62 | |
| Files Accessed | 6 (DALYTRAN, TRANSACT, XREFFILE, DALYREJS, ACCTFILE, TCATBALF) | |
| **Code Complexity** | | **7/10** |
| **Data Coupling** | | **10/10** |
| **Business Risk** | | **10/10** |
| **Modernization Effort** | | **8/10** |
| **Composite Score** | | **8.75** |

**Why #5:** The core batch transaction posting engine that touches the most files of any single program (6 files). It reads daily transactions, validates against cross-references, posts to the master transaction file, updates account balances and category balances, and writes rejects. This is the financial heart of the batch cycle — any posting error directly affects account balances.

**Modernization Risks:**
- Six-file transactional consistency requires careful database transaction design
- Reject handling (DALYREJS GDG) needs error queue or dead-letter table
- Account balance and category balance updates must be atomic with transaction posting
- Sequential file processing pattern maps to Spring Batch chunk-oriented processing

**Recommended Approach:** Implement as a Spring Batch job with multi-step tasklet: validate → post → update balances → write rejects. Use database transactions for atomicity.

---

### Rank 6: CBACT04C — Interest Calculation

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **652** | |
| IF Statements | 86 | |
| EVALUATE Statements | 0 | |
| PERFORM Statements | 57 | |
| Files Accessed | 5 (TCATBALF, CARDXREF, CARDXREF.AIX, ACCTDATA, DISCGRP) | |
| **Code Complexity** | | **7/10** |
| **Data Coupling** | | **8/10** |
| **Business Risk** | | **10/10** |
| **Modernization Effort** | | **8/10** |
| **Composite Score** | | **8.25** |

**Why #6:** Financial calculation engine with the second-highest IF density (86 IFs in 652 lines = 13.2%). Computes interest using disclosure group rates applied to category balances per account. Uses VSAM alternate index path (CARDXREF.AIX.PATH) for account-to-card lookups — a pattern that requires special handling in relational databases.

**Modernization Risks:**
- Financial rounding and precision rules (COMP-3, signed decimals) must be preserved exactly
- Alternate index path access pattern needs JPA `@ManyToOne` relationship or custom query
- Interest calculation business rules are embedded in procedural code — need extraction to a calculation service
- COBOL `COMPUTE` with implicit rounding differs from Java `BigDecimal` arithmetic

**Recommended Approach:** Extract into `InterestCalculationService` using `BigDecimal` with explicit `RoundingMode`. Implement as a scheduled Spring Batch job. Add comprehensive unit tests comparing output to COBOL results.

---

### Rank 7: CBTRN03C — Transaction Detail Report

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **649** | |
| IF Statements | 75 | |
| EVALUATE Statements | 4 | |
| PERFORM Statements | 73 | |
| Files Accessed | 5 (TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM) | |
| **Code Complexity** | | **7/10** |
| **Data Coupling** | | **7/10** |
| **Business Risk** | | **7/10** |
| **Modernization Effort** | | **7/10** |
| **Composite Score** | | **7.00** |

**Why #7:** Highest PERFORM count (73) indicates deeply modular internal structure. Joins data from 5 files to produce a formatted report with control breaks (page totals, account totals, grand total). Uses the CVTRA07Y report layout copybook for 133-character print lines.

**Modernization Risks:**
- Five-file join for report data needs a database VIEW or complex JPA query
- Print-oriented formatting (fixed-width columns, page breaks) needs report framework
- Date parameter file (DATEPARM) needs externalized configuration
- Control break logic (page/account/grand totals) requires stateful processing

**Recommended Approach:** Implement as a Spring Batch reporting job. Use SQL `JOIN` queries to replace multi-file reads. Output via JasperReports or CSV export.

---

### Rank 8: COACTVWC — Account View

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **941** | |
| IF Statements | 57 | |
| EVALUATE Statements | 10 | |
| PERFORM Statements | 21 | |
| VSAM Files Accessed | 4 (ACCTDATA, CARDDATA, CARDXREF, CUSTDATA) | |
| **Code Complexity** | | **6/10** |
| **Data Coupling** | | **8/10** |
| **Business Risk** | | **7/10** |
| **Modernization Effort** | | **6/10** |
| **Composite Score** | | **6.75** |

**Why #8:** Reads across 4 VSAM files to compose a single account view screen, including customer details, card list, and balance information. Uses CSSTRPFY string parsing functions for data formatting. Multiple XCTL exit points (5 possible targets) create complex navigation state.

**Modernization Risks:**
- Four-file composite view maps to a DTO aggregating multiple JPA entities
- Navigation to 5 different targets needs REST HATEOAS links or frontend routing
- Read-only display with embedded sub-lists (cards for account) needs careful API design

**Recommended Approach:** Implement as `GET /api/accounts/{id}/details` returning an `AccountDetailDTO` that aggregates Account, Customer, and Card data via JPA entity graph.

---

### Rank 9: COTRN02C — Transaction Add (Online)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **783** | |
| IF Statements | 14 | |
| EVALUATE Statements | 26 | |
| PERFORM Statements | 61 | |
| VSAM Files Accessed | 3 (TRANSACT, CARDXREF, ACCTDATA) | |
| CALL Statements | 2 (CSUTLDTC for dates) | |
| **Code Complexity** | | **7/10** |
| **Data Coupling** | | **6/10** |
| **Business Risk** | | **9/10** |
| **Modernization Effort** | | **7/10** |
| **Composite Score** | | **7.30** |

**Why #9:** Highest EVALUATE count (26) indicating complex state machine / input processing logic. Creates new transaction records with card validation, date validation (via CSUTLDTC CALL), and unique ID generation (STARTBR/READPREV to find last ID). Writes to the master TRANSACT file — direct financial data creation.

**Modernization Risks:**
- Transaction ID generation (VSAM READPREV to find max ID) needs database sequence or UUID strategy
- Date validation via CSUTLDTC CALL needs Java `LocalDate` parsing with equivalent business rules
- EVALUATE state machine for screen processing needs clean REST controller action mapping
- CICS WRITE to TRANSACT needs `@Transactional` JPA save with optimistic locking

**Recommended Approach:** Implement as `POST /api/transactions` endpoint. Use database sequence for ID generation. Extract validation into `TransactionValidationService`.

---

### Rank 10: CORPT00C — Transaction Report Submit

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **649** | |
| IF Statements | 20 | |
| EVALUATE Statements | 10 | |
| PERFORM Statements | 35 | |
| CICS Operations | WRITEQ TD (trigger batch), SEND/RECEIVE MAP | |
| CALL Statements | 2 (CSUTLDTC for date validation) | |
| **Code Complexity** | | **6/10** |
| **Data Coupling** | | **5/10** |
| **Business Risk** | | **7/10** |
| **Modernization Effort** | | **7/10** |
| **Composite Score** | | **6.25** |

**Why #10:** Bridges online and batch worlds by accepting report parameters via BMS screen and triggering batch report generation via CICS WRITEQ TD (Transient Data Queue). This online-to-batch trigger pattern has no direct equivalent in standard Java web applications and requires an async job submission mechanism.

**Modernization Risks:**
- CICS WRITEQ TD pattern needs async job submission (Spring Batch `JobLauncher`, message queue, or REST API)
- Date range validation with CSUTLDTC CALL needs equivalent Java validation
- Report parameter screen maps to a form with date pickers and dropdowns
- Report status feedback (submitted vs. completed) needs async polling or WebSocket

**Recommended Approach:** Implement as a REST endpoint that launches a Spring Batch job asynchronously. Return a job ID for status polling. Use `@Async` or a message queue for decoupling.

---

## Summary Ranking Table

| Rank | Program | LOC | Complexity | Coupling | Risk | Effort | **Score** | Domain |
|------|---------|-----|-----------|---------|------|--------|-----------|--------|
| 1 | **COACTUPC** | 4,236 | 10 | 9 | 10 | 10 | **9.75** | Account Update |
| 2 | **CBTRN02C** | 731 | 7 | 10 | 10 | 8 | **8.75** | Transaction Posting |
| 3 | **CBACT04C** | 652 | 7 | 8 | 10 | 8 | **8.25** | Interest Calc |
| 4 | **CBSTM03A** | 924 | 7 | 9 | 8 | 9 | **8.10** | Statement Gen |
| 5 | **COCRDUPC** | 1,560 | 8 | 7 | 9 | 8 | **8.05** | Card Update |
| 6 | **COCRDLIC** | 1,459 | 8 | 6 | 7 | 8 | **7.35** | Card List |
| 7 | **COTRN02C** | 783 | 7 | 6 | 9 | 7 | **7.30** | Transaction Add |
| 8 | **CBTRN03C** | 649 | 7 | 7 | 7 | 7 | **7.00** | Tran Report |
| 9 | **COACTVWC** | 941 | 6 | 8 | 7 | 6 | **6.75** | Account View |
| 10 | **CORPT00C** | 649 | 6 | 5 | 7 | 7 | **6.25** | Report Submit |

---

## Migration Priority Recommendations

### Wave 1 — Foundation (Low Risk, High Value)
1. **User Administration** (COUSR00-03C, COSGN00C) — Simplest programs, standalone data, establishes auth framework
2. **Reference Data** (TRANTYPE, TRANCATG, DISCGRP loaders) — Small, simple, needed by other modules

### Wave 2 — Core Read Operations
3. **Account View** (COACTVWC) — Read-only, validates data access patterns across 4 tables
4. **Card List & Detail** (COCRDLIC, COCRDSLC) — Read-only browse patterns, pagination
5. **Transaction List & View** (COTRN00C, COTRN01C) — Read-only, establishes transaction query patterns

### Wave 3 — Core Write Operations (High Risk)
6. **Transaction Add** (COTRN02C) — First write operation, establishes transaction creation pattern
7. **Card Update** (COCRDUPC) — Moderate complexity write with validation
8. **Account Update** (COACTUPC) — Most complex program, multi-file update, highest risk
9. **Bill Payment** (COBIL00C) — Financial write with balance updates

### Wave 4 — Batch Processing (Highest Risk)
10. **Transaction Posting** (CBTRN02C) — Core batch engine, 6-file I/O
11. **Interest Calculation** (CBACT04C) — Financial precision-critical
12. **Statement Generation** (CBSTM03A/B) — Multi-format output, called subroutine
13. **Transaction Report** (CBTRN03C, CORPT00C) — Report generation with batch trigger

### Wave 5 — Utilities & Data Migration
14. **Export/Import** (CBEXPORT, CBIMPORT) — Data migration utilities
15. **Print Programs** (CBACT01-03C, CBCUS01C) — Simple sequential reads
16. **Utility Programs** (CSUTLDTC, COBSWAIT) — Shared services

---

## Key Technical Risks Across All Hotspots

| Risk Category | Affected Programs | Mitigation Strategy |
|--------------|------------------|-------------------|
| **Financial precision** | COACTUPC, CBTRN02C, CBACT04C, COBIL00C | Use `BigDecimal` with explicit `RoundingMode.HALF_UP`; parallel-run validation |
| **VSAM to RDBMS** | All programs | Map KSDS to indexed tables; replace alternate indexes with secondary indexes/JPA queries |
| **Pseudo-conversational** | All 17 online programs | Convert CICS COMMAREA state to HTTP session or JWT; use SPA with REST backend |
| **Control break reports** | CBSTM03A, CBTRN03C | Use SQL `GROUP BY` with `ROLLUP`; implement in report framework |
| **Batch atomicity** | CBTRN02C, CBACT04C, COMBTRAN | Use Spring Batch with chunk-oriented processing and database transactions |
| **PII/Security** | COSGN00C, CSUSR01Y, CVCUS01Y | Replace plaintext passwords with bcrypt; encrypt SSN at rest; add Spring Security |
| **ID generation** | COTRN02C | Replace VSAM READPREV (max ID) with database sequence or UUID |
| **Date handling** | CSUTLDTC, CORPT00C, COTRN02C | Replace CEEDAYS with `java.time.LocalDate`; validate equivalent behavior |
