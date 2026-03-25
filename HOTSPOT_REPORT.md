# CardDemo Hotspot Report — Top 10 Modernization Priority Modules

> **Generated:** 2026-03-25 | **Methodology:** Weighted scoring across code complexity, data coupling, business criticality, and migration risk  
> **Purpose:** Prioritize modules for modernization by identifying the highest-risk, highest-impact components.

---

## Scoring Methodology

Each module is scored on four dimensions (1–5 scale, 5 = highest):

| Dimension | Weight | What It Measures |
|-----------|--------|-----------------|
| **Code Complexity** | 30% | Lines of code, cyclomatic complexity (EVALUATE/IF nesting), number of PERFORM sections, input validation depth |
| **Data Coupling** | 25% | Number of VSAM files accessed, copybooks included, cross-file operations, COMMAREA usage |
| **Business Impact** | 25% | Revenue criticality, regulatory exposure (PCI, SOX), user-facing frequency, financial calculation involvement |
| **Migration Risk** | 20% | CICS-specific constructs, screen handling complexity, concurrency concerns, state management complexity |

**Composite Score** = (Complexity × 0.30) + (Data Coupling × 0.25) + (Business Impact × 0.25) + (Migration Risk × 0.20)

---

## Top 10 Hotspot Modules

### #1 — COACTUPC (Account Update) — Score: 4.75

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Code Complexity | **5** | 4,237 lines — largest program in the codebase. Extensive input validation (SSN, phone, dates, credit limits, FICO score). Deep EVALUATE nesting. 40+ flag variables. Multiple date format validations. |
| Data Coupling | **5** | Accesses 4 VSAM files (ACCTDAT, CUSTDAT, CARDXREF via CXACAIX). Includes 11 copybooks. Uses REWRITE for updates. Complex cross-file referential integrity checks. |
| Business Impact | **5** | Modifies account credit limits, balances, customer data. **PCI-DSS sensitive** (SSN, credit data). High audit trail importance. Used by both admin and regular users. |
| Migration Risk | **4** | Heavy BMS screen interaction with field-level attribute manipulation. Complex COMMAREA state passing. Cursor positioning logic. Multi-screen confirmation flow. |

**Recommendation:** Decompose into separate Account Update and Customer Update services. Extract validation logic into a shared validation library. Implement as separate REST endpoints with Spring Validation.

---

### #2 — CBTRN02C (Transaction Posting) — Score: 4.50

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Code Complexity | **4** | 731 lines. Core posting logic with cross-reference validation, account balance updates, category balance tracking, reject handling. |
| Data Coupling | **5** | Reads/writes 6 files simultaneously: DALYTRAN (input), TRANSACT (output), XREFFILE (lookup), DALYREJS (rejects), ACCOUNT-FILE (I-O), TCATBAL-FILE (I-O). Most file-intensive program. |
| Business Impact | **5** | **Mission-critical financial processing.** Posts all daily transactions. Incorrect posting = financial discrepancy. Drives account balances, category totals, and downstream statement generation. SOX audit implications. |
| Migration Risk | **4** | Batch sequential processing with implicit file positioning. SORT dependencies. Error handling writes to reject file. Must preserve exact decimal arithmetic (COMP-3). |

**Recommendation:** Convert to Spring Batch with chunk-oriented processing. Implement transactional boundaries with database rollback. Add idempotency keys. Critical to maintain exact `BigDecimal` arithmetic.

---

### #3 — CBSTM03A (Statement Generation) — Score: 4.35

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Code Complexity | **4** | 924 lines. Multi-step processing: sort transactions by card, aggregate by account, generate both text and HTML output. Calls subroutine CBSTM03B. Control-break reporting pattern. |
| Data Coupling | **5** | Reads 4 files (TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE). Produces 2 output files (STMTFILE text, HTMLFILE HTML). Uses re-keyed COSTM01 transaction layout. Depends on SORT pre-step in JCL. |
| Business Impact | **5** | **Customer-facing output.** Account statements are regulatory documents (TILA compliance). Errors visible to customers. Drives monthly billing cycle. |
| Migration Risk | **3** | Batch-only, no CICS complexity. But report formatting logic is tightly coupled to fixed-width output. HTML generation uses string concatenation. Requires careful layout preservation. |

**Recommendation:** Convert to Spring Batch job producing PDF via Jasper/iText. Replace SORT pre-step with SQL ORDER BY. Modernize HTML output to a proper template engine (Thymeleaf).

---

### #4 — COCRDLIC (Credit Card List) — Score: 4.10

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Code Complexity | **4** | 1,460 lines. Complex browse logic with STARTBR/READNEXT pagination, forward/backward scrolling, multi-row selection (S=view, U=update). Alternate index browsing. |
| Data Coupling | **4** | Accesses CARDDAT and CARDAIX (alternate index by account). 7-row screen array with selection tracking. Maintains page state across COMMAREA round-trips. |
| Business Impact | **4** | Primary card discovery screen. Gateway to card detail and card update. Used frequently by both user types. |
| Migration Risk | **5** | Most complex BMS pagination pattern in the codebase. CICS browse (STARTBR/READNEXT/READPREV/ENDBR) maps poorly to REST. Screen array logic with per-row selection flags. Page-up/page-down state tracking via COMMAREA. |

**Recommendation:** Convert to paginated REST endpoint with Spring Data `Pageable`. Replace BMS list with React/Angular data table. Pagination state moves from COMMAREA to URL query parameters.

---

### #5 — COCRDUPC (Credit Card Update) — Score: 4.05

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Code Complexity | **4** | 1,560 lines. Full CRUD screen with field-level validation, confirmation flow, embossed-name editing, date validation, status change logic. |
| Data Coupling | **4** | Accesses CARDDAT, CUSTDAT, ACCTDAT. Cross-validates card against account and customer. REWRITE on update. |
| Business Impact | **4** | Modifies card data including status, expiration, embossed name. **PCI-DSS sensitive** (card numbers, CVV display). |
| Migration Risk | **4** | Multi-step screen flow (display → edit → confirm → save). BMS attribute manipulation. Complex COMMAREA state. |

**Recommendation:** Implement as card management REST API + web form. Mask card numbers (PCI requirement). Add audit logging for all card modifications.

---

### #6 — CBACT04C (Interest Calculation) — Score: 4.00

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Code Complexity | **4** | 652 lines. Financial calculation logic: reads disclosure group rates, applies to category balances, compounds interest, updates account balances. Date-range-based processing via PARM. |
| Data Coupling | **4** | Reads DISCGRP and TCATBALF. Updates ACCTDAT and TCATBALF (I-O). Cross-references group IDs to apply correct rates. |
| Business Impact | **5** | **Revenue-generating process.** Interest is the primary revenue driver for a credit card business. Errors directly impact P&L and customer billing. Regulatory scrutiny (TILA, CARD Act). |
| Migration Risk | **3** | Batch-only, straightforward sequential processing. But decimal precision is critical — COMP-3 arithmetic must map exactly to `BigDecimal`. |

**Recommendation:** Convert to Spring Batch. Implement interest engine as a testable service with parameterized rates. Extensive unit testing with known-good calculation scenarios. Use `BigDecimal` with `HALF_EVEN` rounding.

---

### #7 — COTRN00C (Transaction List) — Score: 3.85

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Code Complexity | **3** | 699 lines. Browse with STARTBR/READNEXT/READPREV. Filter by transaction ID range. Page-up/page-down navigation. |
| Data Coupling | **4** | Accesses TRANSACT file. Heavy COMMAREA state (first/last keys per page, page indicators). Screen array for 7-row display. |
| Business Impact | **4** | Primary transaction discovery screen. High-frequency use by cardholders and customer service. |
| Migration Risk | **4** | Same CICS browse complexity as COCRDLIC. Bidirectional scrolling with key tracking. |

**Recommendation:** Convert to paginated transaction search REST endpoint. Consider cursor-based pagination for high-volume tables.

---

### #8 — COTRN02C (Transaction Add) — Score: 3.80

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Code Complexity | **3** | 783 lines. Multi-field input form with validation (amount, card number, merchant, source, type/category). Cross-reference validation. |
| Data Coupling | **4** | Reads CARDXREF and CARDDAT for validation. Writes to TRANSACT. Generates unique transaction ID. |
| Business Impact | **4** | Creates new financial transactions. Data integrity critical — invalid transactions corrupt downstream processing (posting, statements, interest). |
| Migration Risk | **4** | BMS form handling. COMMAREA-based confirmation flow. Transaction ID generation logic must be thread-safe in Java. |

**Recommendation:** Convert to POST REST endpoint with Bean Validation. Replace sequential ID generation with UUID or database sequence. Add optimistic locking.

---

### #9 — COBIL00C (Bill Payment) — Score: 3.75

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Code Complexity | **3** | 572 lines. Payment processing: reads account balance, accepts payment amount (full or partial), creates payment transaction, updates account balance. |
| Data Coupling | **4** | Accesses ACCTDAT (read + rewrite), TRANSACT (write), CXACAIX (cross-reference lookup). Multi-file atomic update. |
| Business Impact | **5** | **Direct financial operation.** Customer bill payments. Incorrect processing = financial loss. Dual-entry bookkeeping (transaction + balance update). |
| Migration Risk | **3** | Standard CICS screen interaction. But atomicity of the multi-file update (account balance + new transaction) requires careful transaction management in Java. |

**Recommendation:** Implement as payment service with database transaction ensuring atomicity. Add payment idempotency. Consider event-driven architecture (payment event → balance update).

---

### #10 — CBTRN03C (Transaction Report) — Score: 3.55

| Dimension | Score | Evidence |
|-----------|-------|----------|
| Code Complexity | **3** | 649 lines. Control-break report: by account, with page totals, account totals, grand totals. Date-range parameterization. Lookup joins to TRANTYPE and TRANCATG for descriptions. |
| Data Coupling | **4** | Reads 5 files: TRANSACT, XREFFILE, TRANTYPE, TRANCATG, DATEPARM. Writes TRANREPT report file. Multiple file cross-references. |
| Business Impact | **3** | Internal reporting. Used for audit trails and operational monitoring. Not directly customer-facing. |
| Migration Risk | **3** | Batch-only with SORT pre-step. Fixed-width report output. Date parameter file input. |

**Recommendation:** Convert to Spring Batch report job or scheduled query-based report. Output as CSV/PDF. Replace SORT with SQL ORDER BY. Replace date parameter file with REST API parameters.

---

## Summary Ranking

| Rank | Program | Score | Lines | Domain | Key Risk Factor |
|------|---------|-------|-------|--------|----------------|
| **1** | COACTUPC | **4.75** | 4,237 | Account Update | Largest codebase, most complex validation |
| **2** | CBTRN02C | **4.50** | 731 | Transaction Posting | Mission-critical financial posting, 6 files |
| **3** | CBSTM03A | **4.35** | 924 | Statement Generation | Customer-facing regulatory document |
| **4** | COCRDLIC | **4.10** | 1,460 | Card List | Most complex CICS pagination |
| **5** | COCRDUPC | **4.05** | 1,560 | Card Update | PCI-sensitive data modification |
| **6** | CBACT04C | **4.00** | 652 | Interest Calculation | Revenue-generating, decimal precision critical |
| **7** | COTRN00C | **3.85** | 699 | Transaction List | CICS browse complexity |
| **8** | COTRN02C | **3.80** | 783 | Transaction Add | Data integrity gateway |
| **9** | COBIL00C | **3.75** | 572 | Bill Payment | Direct financial operation |
| **10** | CBTRN03C | **3.55** | 649 | Transaction Report | Multi-file report generation |

---

## Migration Wave Recommendations

### Wave 1 — Foundation (Low Risk, Build Patterns)
**Modules:** COSGN00C, COMEN01C, COADM01C, COUSR00C–03C  
**Rationale:** Simple CRUD on user security file. Establishes authentication patterns, menu navigation framework, and BMS-to-web-UI conversion patterns.  
**Effort:** 2–3 weeks

### Wave 2 — Core Read Operations (Medium Risk)
**Modules:** COACTVWC, COCRDSLC, COTRN01C, CBACT01C–03C, CBCUS01C  
**Rationale:** Read-only screens and batch print programs. Low data integrity risk. Establishes VSAM-to-JPA query patterns.  
**Effort:** 3–4 weeks

### Wave 3 — Core Write Operations (High Risk)
**Modules:** COACTUPC (#1), COCRDUPC (#5), COTRN02C (#8), COBIL00C (#9)  
**Rationale:** Data modification programs. Requires careful validation migration and atomicity testing. PCI-DSS review needed for card data handling.  
**Effort:** 4–6 weeks

### Wave 4 — Batch Processing (Critical Risk)
**Modules:** CBTRN02C (#2), CBACT04C (#6), CBSTM03A (#3), CBTRN03C (#10)  
**Rationale:** Mission-critical batch cycle. Requires Spring Batch framework, exact decimal arithmetic, parallel testing with mainframe output for validation period.  
**Effort:** 6–8 weeks

### Wave 5 — Complex Browsing & Reports
**Modules:** COCRDLIC (#4), COTRN00C (#7), CORPT00C, CBEXPORT, CBIMPORT  
**Rationale:** CICS browse patterns require pagination redesign. Export/import requires API redesign. Can be done in parallel with Wave 4.  
**Effort:** 3–4 weeks

### Wave 6 — Optional Modules
**Modules:** Authorization (IMS/DB2/MQ), Transaction Type (DB2), VSAM-MQ  
**Rationale:** These introduce IMS, DB2, and MQ dependencies. Migrate after core patterns are established. May require architectural decisions (keep MQ vs. migrate to Kafka/SQS).  
**Effort:** 4–6 weeks

---

## Key Technical Risks Across All Modules

| Risk | Impact | Mitigation |
|------|--------|------------|
| **Decimal precision loss** | Financial discrepancies | Use `BigDecimal` with `HALF_EVEN` rounding. Parallel run with mainframe comparison. |
| **COMMAREA state → HTTP stateless** | Navigation/flow bugs | Map COMMAREA fields to session or JWT claims. Use PRG (Post-Redirect-Get) pattern. |
| **CICS browse → SQL pagination** | Performance, correctness | Implement keyset pagination. Index optimization. Load test with production volumes. |
| **VSAM key semantics** | Data integrity | Map KSDS keys to primary keys. Map alternate indexes to secondary indexes/unique constraints. |
| **Packed decimal (COMP-3)** | Silent data corruption | Use `BigDecimal` for all monetary fields. Test with boundary values. |
| **Fixed-width record I/O** | Data migration errors | Validate record parsing with checksum comparison. Test FILLER handling. |
| **BMS attribute bytes** | UI inconsistency | Design web forms independently. Map field protection/highlighting to CSS/JS validation. |
| **Batch SORT steps** | Processing order | Replace with SQL `ORDER BY`. Validate sort key equivalence. |
| **PCI-DSS compliance** | Regulatory exposure | Never store full card numbers in logs. Mask in UI. Encrypt at rest. Audit all access. |
| **Concurrency (CICS single-thread → multi-thread)** | Race conditions | Add optimistic locking (version columns). Database-level constraints. |
