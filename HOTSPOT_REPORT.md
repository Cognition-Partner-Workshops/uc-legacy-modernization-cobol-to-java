# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Application:** CardDemo | **Method:** Static analysis of LOC, cyclomatic complexity indicators, file coupling, business criticality, and modernization risk

---

## Scoring Methodology

Each module is scored on three dimensions (1-10 scale):

| Dimension | Weight | Criteria |
|-----------|--------|----------|
| **Complexity** | 40% | Lines of code, nested logic depth, number of COPY/REPLACING, EVALUATE branches, date validation, PERFORM THRU chains |
| **Risk** | 30% | Number of VSAM files touched, read/write operations, cross-program dependencies (XCTL/CALL targets), error handling patterns |
| **Business Impact** | 30% | Revenue criticality, data integrity exposure, user-facing frequency, regulatory implications |

**Hotspot Score** = (Complexity x 0.4) + (Risk x 0.3) + (Business Impact x 0.3)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC -- Account Update

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **4,237** | -- |
| Copybooks Used | 15 (+ 30 COPY REPLACING) | -- |
| VSAM Files | 3 (ACCTDAT R/W, CUSTDAT R, CXACAIX R) | -- |
| Complexity | **10/10** | -- |
| Risk | **9/10** | -- |
| Business Impact | **10/10** | -- |
| **Hotspot Score** | | **9.7** |

**Why #1:** This is the largest program in the entire codebase at 4,237 lines -- more than double any other program. It contains:
- Extensive field-level validation for credit limits, cash limits, balances, dates, ZIP codes, phone numbers, and state codes
- 30 instances of `COPY CSSETATY REPLACING` for dynamic BMS attribute manipulation
- Embedded date validation via `CSUTLDWY`/`CSUTLDPY` copybooks (376 lines of date logic)
- Lookup table validation via `CSLKPCDY` (1,318 lines of phone area codes, state codes, ZIP prefixes)
- Direct account balance modifications (financial data integrity)

**Modernization Recommendation:** Decompose into multiple Java service classes: `AccountValidationService`, `AccountUpdateService`, `AddressValidationService`, `DateValidationService`. Extract lookup tables to database reference tables. Priority: **CRITICAL** -- convert early with extensive unit test coverage.

---

### Rank 2: CBTRN02C -- Transaction Posting

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **731** | -- |
| VSAM Files | 6 (DALYTRAN R, TRANFILE R/W, XREFFILE R, DALYREJS W, ACCTFILE R/W, TCATBALF R/W) | -- |
| Complexity | **8/10** | -- |
| Risk | **10/10** | -- |
| Business Impact | **10/10** | -- |
| **Hotspot Score** | | **9.2** |

**Why #2:** This is the core financial transaction posting engine. It:
- Touches 6 different VSAM files (highest file coupling in the codebase)
- Updates account balances (ACCTFILE) and category balances (TCATBALF) -- direct financial data mutation
- Writes rejected transactions to DALYREJS for audit
- Validates card cross-references before posting
- Any bug here directly impacts account balances and financial integrity

**Modernization Recommendation:** Implement as a Spring Batch job with transactional boundaries (database transactions replacing VSAM I/O). Add idempotency keys. Priority: **CRITICAL** -- requires extensive integration testing and parallel-run validation.

---

### Rank 3: CBACT04C -- Interest Calculation

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **652** | -- |
| VSAM Files | 4 (ACCTFILE R/W, DISCGRP R, TRANFILE W, TCATBALF R) | -- |
| Complexity | **7/10** | -- |
| Risk | **8/10** | -- |
| Business Impact | **9/10** | -- |
| **Hotspot Score** | | **7.9** |

**Why #3:** Core financial calculation with:
- Interest rate computation based on disclosure groups
- Account balance updates (financial mutation)
- Creates interest charge transactions
- Regulatory implications (Truth in Lending Act compliance)
- Any calculation error affects all customer statements
- Despite moderate LOC, highest risk/impact ratio in the codebase

**Modernization Recommendation:** Implement as isolated Spring Batch step with `BigDecimal` arithmetic (not floating point). Add reconciliation reporting. Requires parallel-run validation against legacy calculations. Priority: **CRITICAL**.

---

### Rank 4: COCRDUPC -- Credit Card Update

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **1,560** | -- |
| VSAM Files | 2 (CARDDAT R/W, CARDAIX R) | -- |
| Complexity | **8/10** | -- |
| Risk | **7/10** | -- |
| Business Impact | **8/10** | -- |
| **Hotspot Score** | | **7.7** |

**Why #4:** Second-largest online program with:
- Complex card field validation (card number, CVV, embossed name, expiration date)
- BMS screen attribute manipulation similar to COACTUPC
- Date validation for card expiration
- XCTL navigation with dynamic return

**Modernization Recommendation:** Extract to `CardUpdateService` with Bean Validation annotations. Reuse date validation from Account module. Priority: **HIGH**.

---

### Rank 5: CBSTM03A -- Statement Generation (Driver)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **924** | -- |
| VSAM Files | 4 (ACCTFILE R, XREFFILE R, TRANFILE R, STMTFILE W) | -- |
| CALLs to CBSTM03B | **12 call sites** | -- |
| Complexity | **7/10** | -- |
| Risk | **7/10** | -- |
| Business Impact | **8/10** | -- |
| **Hotspot Score** | | **7.3** |

**Why #5:** The statement generation driver has:
- Tight coupling with CBSTM03B (called 12 times for different print sections)
- Complex report formatting logic (headers, detail lines, subtotals, grand totals)
- Multi-file correlation (accounts -> cross-refs -> transactions)
- Customer-facing output (statements) -- high visibility

**Modernization Recommendation:** Implement as Spring Batch job with `ItemReader`/`ItemProcessor`/`ItemWriter`. Replace print output with PDF generation (e.g., Apache PDFBox or JasperReports). Merge CBSTM03A/B into single service. Priority: **HIGH**.

---

### Rank 6: COTRN02C -- Transaction Add

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **783** | -- |
| VSAM Files | 3 (CXACAIX R, CCXREF R, TRANSACT R/W) | -- |
| External CALLs | CSUTLDTC (date validation, 2 sites) | -- |
| Complexity | **7/10** | -- |
| Risk | **7/10** | -- |
| Business Impact | **6/10** | -- |
| **Hotspot Score** | | **6.7** |

**Why #6:** Online transaction entry with:
- Card cross-reference validation before transaction creation
- Date validation via CSUTLDTC CALL
- VSAM WRITE to TRANSACT file (data creation)
- Generates unique transaction IDs

**Modernization Recommendation:** Convert to POST REST endpoint with request validation. Implement as `TransactionService.createTransaction()`. Priority: **MEDIUM-HIGH** (data creation path).

---

### Rank 7: COCRDLIC -- Credit Card List

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **1,460** | -- |
| VSAM Files | 2 (CARDDAT R, CARDAIX R) | -- |
| Complexity | **7/10** | -- |
| Risk | **6/10** | -- |
| Business Impact | **7/10** | -- |
| **Hotspot Score** | | **6.7** |

**Why #7:** Complex pagination logic with:
- VSAM STARTBR/READNEXT/READPREV browse operations
- Alternate index (CARDAIX) access for account-based card lookups
- Forward/backward page navigation state management
- XCTL to both COCRDSLC (view) and COCRDUPC (update)
- Most frequently used card management screen

**Modernization Recommendation:** Convert to paginated REST endpoint with Spring Data JPA `Pageable`. Replace VSAM browse with SQL `LIMIT`/`OFFSET` or keyset pagination. Priority: **HIGH**.

---

### Rank 8: COACTVWC -- Account View

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **942** | -- |
| VSAM Files | 3 (ACCTDAT R, CUSTDAT R, CARDXREF R) | -- |
| Complexity | **6/10** | -- |
| Risk | **5/10** | -- |
| Business Impact | **8/10** | -- |
| **Hotspot Score** | | **6.3** |

**Why #8:** Primary account inquiry screen with:
- Multi-file data aggregation (account + customer + cross-reference)
- Complex screen layout with formatted display fields
- High usage frequency (most common user action)
- Read-only but critical for customer service workflows

**Modernization Recommendation:** Convert to REST GET endpoint with DTO assembly. Good candidate for early conversion due to read-only nature. Priority: **MEDIUM-HIGH**.

---

### Rank 9: COCRDSLC -- Credit Card Detail View

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **888** | -- |
| VSAM Files | 2 (CARDDAT R, CARDAIX R) | -- |
| Complexity | **6/10** | -- |
| Risk | **5/10** | -- |
| Business Impact | **7/10** | -- |
| **Hotspot Score** | | **6.0** |

**Why #9:**
- Alternate index access pattern (CARDAIX)
- BMS screen handling with field formatting
- Navigation state management via COMMAREA
- Read-only but feeds into COCRDUPC for updates

**Modernization Recommendation:** Convert alongside COCRDLIC as part of card management REST API. Priority: **MEDIUM**.

---

### Rank 10: COTRN00C -- Transaction List

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **699** | -- |
| VSAM Files | 1 (TRANSACT R) | -- |
| Complexity | **6/10** | -- |
| Risk | **4/10** | -- |
| Business Impact | **7/10** | -- |
| **Hotspot Score** | | **5.7** |

**Why #10:** Transaction browse/search with:
- VSAM STARTBR/READNEXT pagination
- Forward/backward navigation
- XCTL to COTRN01C (view) and COTRN02C (add)
- High user interaction frequency

**Modernization Recommendation:** Convert to paginated REST endpoint. Similar pattern to COCRDLIC. Priority: **MEDIUM**.

---

## Summary Ranking Table

| Rank | Program | LOC | Type | Complexity | Risk | Biz Impact | Score | Priority |
|------|---------|-----|------|-----------|------|-----------|-------|----------|
| 1 | COACTUPC | 4,237 | Online | 10 | 9 | 10 | **9.7** | CRITICAL |
| 2 | CBTRN02C | 731 | Batch | 8 | 10 | 10 | **9.2** | CRITICAL |
| 3 | CBACT04C | 652 | Batch | 7 | 8 | 9 | **7.9** | CRITICAL |
| 4 | COCRDUPC | 1,560 | Online | 8 | 7 | 8 | **7.7** | HIGH |
| 5 | CBSTM03A | 924 | Batch | 7 | 7 | 8 | **7.3** | HIGH |
| 6 | COTRN02C | 783 | Online | 7 | 7 | 6 | **6.7** | MEDIUM-HIGH |
| 7 | COCRDLIC | 1,460 | Online | 7 | 6 | 7 | **6.7** | HIGH |
| 8 | COACTVWC | 942 | Online | 6 | 5 | 8 | **6.3** | MEDIUM-HIGH |
| 9 | COCRDSLC | 888 | Online | 6 | 5 | 7 | **6.0** | MEDIUM |
| 10 | COTRN00C | 699 | Online | 6 | 4 | 7 | **5.7** | MEDIUM |

---

## Risk Themes Across Hotspots

### 1. Financial Data Integrity (CRITICAL)
- **Programs:** COACTUPC, CBTRN02C, CBACT04C, COBIL00C
- **Risk:** Direct mutation of account balances and transaction records
- **Mitigation:** Implement database transactions, add reconciliation checks, parallel-run validation

### 2. Complex Validation Logic (HIGH)
- **Programs:** COACTUPC, COCRDUPC, COTRN02C
- **Risk:** 1,700+ lines of date, phone, ZIP, and state validation embedded in programs
- **Mitigation:** Extract to shared validation service, use Bean Validation annotations, externalize lookup tables

### 3. VSAM File Coupling (HIGH)
- **Programs:** CBTRN02C (6 files), CBACT04C (4 files), CBSTM03A (4 files)
- **Risk:** Multi-file updates without transactional boundaries
- **Mitigation:** Map VSAM files to relational tables with proper foreign keys and transaction management

### 4. Tight Program Coupling (MEDIUM)
- **Programs:** CBSTM03A/CBSTM03B (12 CALL sites), COTRN02C/CSUTLDTC, CORPT00C/CSUTLDTC
- **Risk:** Subroutine interfaces via WORKING-STORAGE areas
- **Mitigation:** Convert to method calls with typed parameters

### 5. Hardcoded Reference Data (MEDIUM)
- **Programs:** COACTUPC (via CSLKPCDY -- 1,318 lines of area codes, state codes, ZIP prefixes)
- **Risk:** Reference data changes require recompilation
- **Mitigation:** Move to database reference tables with admin UI for maintenance

---

## Recommended Modernization Waves

### Wave 1: Foundation (Weeks 1-4)
- Data layer: Convert all VSAM files to relational database tables
- Shared services: Date validation, lookup tables, authentication
- Programs: COSGN00C, COMEN01C, COADM01C (navigation framework)

### Wave 2: Read-Only Screens (Weeks 5-8)
- Programs: COACTVWC, COCRDSLC, COTRN01C, COTRN00C, COCRDLIC, COUSR00C
- Lower risk due to read-only access patterns
- Validates data layer correctness

### Wave 3: Data Mutation (Weeks 9-14)
- Programs: COACTUPC, COCRDUPC, COTRN02C, COBIL00C, COUSR01C-03C
- Higher risk -- requires thorough testing
- COACTUPC alone may need 2-3 weeks due to complexity

### Wave 4: Batch Processing (Weeks 15-20)
- Programs: CBTRN01C, CBTRN02C, CBACT04C, CBSTM03A/B, CBTRN03C
- Spring Batch implementation
- Parallel-run validation against legacy batch
- CBACT04C (interest calculation) requires mathematical validation

### Wave 5: Data Migration & Utilities (Weeks 21-22)
- Programs: CBEXPORT, CBIMPORT, CBACT01C-03C, CBCUS01C
- May be replaced entirely by database migration scripts
- Export/import may become REST API endpoints

### Wave 6: Optional Modules (Weeks 23-26)
- Authorization module (IMS/DB2/MQ -> Spring + JPA + JMS)
- Transaction Type DB2 module (already SQL-based -- easier conversion)
- VSAM-MQ module (-> REST + JMS)
