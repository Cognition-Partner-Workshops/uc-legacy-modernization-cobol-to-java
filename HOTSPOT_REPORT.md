# CardDemo Hotspot Report

> **Generated**: 2026-03-25 | **Purpose**: Identify the top 10 modules by complexity, risk, and business impact for modernization prioritization

---

## Scoring Methodology

Each module is scored across three dimensions (1-10 scale):

| Dimension | Weight | Criteria |
|-----------|--------|----------|
| **Complexity** | 40% | Lines of code, nested logic depth, number of EVALUATE/IF branches, file I/O operations, copybook dependencies, external calls |
| **Risk** | 30% | Data integrity exposure (writes to multiple files), error handling gaps, security concerns, business-critical path, coupling to other modules |
| **Business Impact** | 30% | User-facing frequency, financial data handling, regulatory/compliance relevance, downstream dependencies, modernization effort |

**Composite Score** = (Complexity x 0.4) + (Risk x 0.3) + (Business Impact x 0.3)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC -- Account Update

| Metric | Value |
|--------|-------|
| **Lines of Code** | 4,237 |
| **Complexity Score** | 10/10 |
| **Risk Score** | 10/10 |
| **Business Impact Score** | 9/10 |
| **Composite Score** | **9.7** |
| **Location** | `app/cbl/COACTUPC.cbl` |
| **Transaction ID** | CAUP |

**Why it's #1:**
- **Largest program** in the entire codebase at 4,237 lines -- more than 2.5x the next largest
- **Writes to 2 master files**: ACCTDAT (account) and CUSTDAT (customer) via CICS REWRITE
- **Extensive field-level validation**: SSN format, phone number area codes (validated against 1,300-line lookup table in CSLKPCDY), date validation (calls CSUTLDTC), ZIP code cross-checks against state codes, credit limit ranges, balance consistency
- **13+ copybooks** included, creating high coupling
- **Complex screen interaction**: multi-step workflow with context preservation across re-entries
- **PII handling**: Directly modifies SSN, DOB, address, phone -- regulatory/compliance critical
- **Modernization challenge**: Must preserve all validation logic precisely; any regression risks financial data integrity

**Recommendation**: Decompose into smaller services: AccountUpdateService, CustomerUpdateService, ValidationService. Extract validation rules into a configurable rules engine.

---

### Rank 2: CBTRN02C -- Transaction Posting (Batch)

| Metric | Value |
|--------|-------|
| **Lines of Code** | 731 |
| **Complexity Score** | 9/10 |
| **Risk Score** | 10/10 |
| **Business Impact Score** | 10/10 |
| **Composite Score** | **9.6** |
| **Location** | `app/cbl/CBTRN02C.cbl` |
| **Executed By** | POSTTRAN.jcl (Step 2) |

**Why it's #2:**
- **Core financial processing**: Posts daily transactions to the master transaction file
- **Writes/updates 3 files**: TRANSACT (write), TCATBAL (write/rewrite), ACCTDAT (rewrite balance)
- **Reads 4 files**: DALYTRAN (daily input), CARDXREF (cross-reference), ACCTDAT, TCATBAL
- **Balance update logic**: Modifies account current balance -- any bug directly impacts financial accuracy
- **Reject handling**: Writes rejected transactions to a separate reject file with reason codes
- **Category balance tracking**: Maintains running balances per account/type/category combination
- **Abend handling**: Calls CEE3ABD on fatal errors; incomplete postings risk data inconsistency

**Recommendation**: Implement as a Spring Batch job with chunk-oriented processing. Add transaction management (database transactions) to ensure atomicity. Implement idempotency keys to prevent double-posting.

---

### Rank 3: COCRDUPC -- Card Update

| Metric | Value |
|--------|-------|
| **Lines of Code** | 1,560 |
| **Complexity Score** | 8/10 |
| **Risk Score** | 8/10 |
| **Business Impact Score** | 8/10 |
| **Composite Score** | **8.0** |
| **Location** | `app/cbl/COCRDUPC.cbl` |
| **Transaction ID** | CCUP |

**Why it's #3:**
- **Second largest online program** at 1,560 lines
- **Card data modification**: Updates card status, embossed name, expiration -- directly affects card usability
- **Multi-file reads**: CARDDAT (read/rewrite), cross-references via alternate index paths
- **PF-key navigation logic**: Complex EVALUATE statements for function key handling
- **Screen state management**: Tracks input changes, confirmation flows, error re-display
- **Shares pattern with COACTUPC**: Similar validation/update architecture but for card entity

**Recommendation**: Extract card validation into a shared CardValidationService. Implement optimistic locking for concurrent update protection.

---

### Rank 4: COCRDLIC -- Card List

| Metric | Value |
|--------|-------|
| **Lines of Code** | 1,460 |
| **Complexity Score** | 8/10 |
| **Risk Score** | 6/10 |
| **Business Impact Score** | 8/10 |
| **Composite Score** | **7.4** |
| **Location** | `app/cbl/COCRDLIC.cbl` |
| **Transaction ID** | CCLI |

**Why it's #4:**
- **Pagination logic**: Implements forward (F8) and backward (F7) browsing via STARTBR/READNEXT/READPREV
- **Alternate index browsing**: Uses CARDAIX to list cards by account
- **Screen buffer management**: Fills a 10-row display grid, manages page boundaries
- **Navigation hub**: Transfers to COCRDSLC (view) or COCRDUPC (update) based on selection
- **Complex state**: Must track scroll position, current page, total records across CICS pseudo-conversational boundaries

**Recommendation**: Replace browse logic with paginated database queries. Implement cursor-based pagination in the modernized API layer.

---

### Rank 5: CBSTM03A -- Statement Generation

| Metric | Value |
|--------|-------|
| **Lines of Code** | 924 |
| **Complexity Score** | 8/10 |
| **Risk Score** | 7/10 |
| **Business Impact Score** | 8/10 |
| **Composite Score** | **7.7** |
| **Location** | `app/cbl/CBSTM03A.CBL` |
| **Executed By** | CREASTMT.JCL |

**Why it's #5:**
- **Multi-file reader**: ACCTDAT, CUSTDAT, CARDXREF, TRANSACT -- reads 4 master files
- **Calls subroutine 11+ times**: CBSTM03B is called repeatedly for different file operations
- **Report formatting**: Generates formatted account statements with headers, details, totals
- **Control break logic**: Groups transactions by account, calculates subtotals and grand totals
- **Customer-facing output**: Statements are the primary customer communication; errors are visible to cardholders
- **Batch dependency chain**: Requires all upstream batch jobs to complete successfully

**Recommendation**: Implement as a Spring Batch job reading from database views. Generate statements as PDF using a templating engine. Consider event-driven generation triggered by billing cycle close.

---

### Rank 6: COACTVWC -- Account View

| Metric | Value |
|--------|-------|
| **Lines of Code** | 942 |
| **Complexity Score** | 7/10 |
| **Risk Score** | 6/10 |
| **Business Impact Score** | 8/10 |
| **Composite Score** | **7.0** |
| **Location** | `app/cbl/COACTVWC.cbl` |
| **Transaction ID** | CAVW |

**Why it's #6:**
- **Multi-file reader**: Reads ACCTDAT, CARDDAT, CUSTDAT, and browses CARDXREF + CXACAIX
- **5 alternate index paths**: Uses both primary and alternate index access to navigate entity relationships
- **Screen composition**: Assembles data from 4 different entities into a single comprehensive view
- **13 copybooks**: High coupling to multiple data structures
- **Most-used screen**: Account view is typically the most frequently accessed screen in card management

**Recommendation**: Implement as a read-only REST endpoint returning a composite AccountDetailDTO. Use database JOINs to replace multi-file reads. Cache frequently accessed accounts.

---

### Rank 7: CBACT04C -- Interest Calculation

| Metric | Value |
|--------|-------|
| **Lines of Code** | 652 |
| **Complexity Score** | 7/10 |
| **Risk Score** | 9/10 |
| **Business Impact Score** | 8/10 |
| **Composite Score** | **7.9** |
| **Location** | `app/cbl/CBACT04C.cbl` |
| **Executed By** | INTCALC.jcl |

**Why it's #7 (high risk despite moderate size):**
- **Financial calculation**: Computes interest on account balances -- directly affects billing amounts
- **Multi-file access**: Reads ACCTDAT, TCATBAL, DISCGRP; Updates ACCTDAT
- **Rate lookup logic**: Joins account group to disclosure group to determine interest rate per transaction category
- **Balance modification**: Updates account balance with accrued interest
- **Regulatory exposure**: Interest calculations must comply with Truth in Lending Act (TILA) regulations
- **Precision requirements**: Rounding errors in decimal arithmetic can accumulate across thousands of accounts

**Recommendation**: Implement with BigDecimal arithmetic. Create a dedicated InterestCalculationService with comprehensive unit tests for rounding behavior. Add audit logging for all balance modifications.

---

### Rank 8: COTRN02C -- Transaction Add

| Metric | Value |
|--------|-------|
| **Lines of Code** | 783 |
| **Complexity Score** | 7/10 |
| **Risk Score** | 7/10 |
| **Business Impact Score** | 7/10 |
| **Composite Score** | **7.0** |
| **Location** | `app/cbl/COTRN02C.cbl` |
| **Transaction ID** | CT02 |

**Why it's #8:**
- **Transaction creation**: Writes new records to TRANSACT VSAM file
- **Date validation**: Calls CSUTLDTC utility for date fields
- **Cross-reference validation**: Reads ACCTDAT and CARDXREF to validate card-account relationships
- **Amount handling**: Signed decimal amounts require careful validation
- **Generates unique IDs**: Must ensure transaction ID uniqueness

**Recommendation**: Implement as a REST POST endpoint with request validation. Use database sequences for ID generation. Add idempotency support via request IDs.

---

### Rank 9: COCRDSLC -- Card Detail View

| Metric | Value |
|--------|-------|
| **Lines of Code** | 888 |
| **Complexity Score** | 6/10 |
| **Risk Score** | 6/10 |
| **Business Impact Score** | 7/10 |
| **Composite Score** | **6.3** |
| **Location** | `app/cbl/COCRDSLC.cbl` |
| **Transaction ID** | CCDL |

**Why it's #9:**
- **Multi-entity display**: Shows card details plus associated customer information
- **Reads CARDDAT and CUSTDAT**: Joins card and customer data for display
- **PII display**: Shows card number, CVV, and customer details on screen
- **Navigation**: Returns to COMEN01C menu
- **Screen formatting**: Complex BMS map with multiple field groups

**Recommendation**: Implement PCI-DSS compliant card display (mask card number, never show CVV). Add role-based field visibility.

---

### Rank 10: COBIL00C -- Bill Payment

| Metric | Value |
|--------|-------|
| **Lines of Code** | 572 |
| **Complexity Score** | 7/10 |
| **Risk Score** | 8/10 |
| **Business Impact Score** | 7/10 |
| **Composite Score** | **7.3** |
| **Location** | `app/cbl/COBIL00C.cbl` |
| **Transaction ID** | CB00 |

**Why it's #10:**
- **Financial transaction**: Processes bill payments that modify account balances
- **Multi-file I/O**: Reads ACCTDAT, browses TRANSACT and CARDXREF; writes to TRANSACT, updates ACCTDAT
- **Balance calculation**: Reads most recent transactions to compute outstanding balance
- **READPREV browsing**: Uses reverse browse to find latest transactions
- **Payment posting**: Creates a new credit transaction and updates account balance atomically
- **User-facing**: Directly handles customer money -- errors are immediately visible

**Recommendation**: Implement with database transaction management for atomicity. Add payment confirmation workflow. Implement daily payment limits and fraud checks.

---

## Summary Table

| Rank | Program | Lines | Type | Composite | Complexity | Risk | Impact | Key Concern |
|------|---------|-------|------|-----------|-----------|------|--------|-------------|
| 1 | **COACTUPC** | 4,237 | Online | **9.7** | 10 | 10 | 9 | Massive size, multi-file writes, PII |
| 2 | **CBTRN02C** | 731 | Batch | **9.6** | 9 | 10 | 10 | Core financial posting, balance updates |
| 3 | **COCRDUPC** | 1,560 | Online | **8.0** | 8 | 8 | 8 | Card data modification, validation |
| 4 | **COCRDLIC** | 1,460 | Online | **7.4** | 8 | 6 | 8 | Pagination logic, browse state |
| 5 | **CBSTM03A** | 924 | Batch | **7.7** | 8 | 7 | 8 | Multi-file report, customer-facing |
| 6 | **COACTVWC** | 942 | Online | **7.0** | 7 | 6 | 8 | Multi-entity composite view |
| 7 | **CBACT04C** | 652 | Batch | **7.9** | 7 | 9 | 8 | Interest calculation, regulatory |
| 8 | **COTRN02C** | 783 | Online | **7.0** | 7 | 7 | 7 | Transaction creation, validation |
| 9 | **COCRDSLC** | 888 | Online | **6.3** | 6 | 6 | 7 | PII display, PCI concerns |
| 10 | **COBIL00C** | 572 | Online | **7.3** | 7 | 8 | 7 | Payment processing, balance update |

---

## Modernization Priority Waves

Based on the hotspot analysis, we recommend the following modernization sequence:

### Wave 1 -- Core Financial Engine (Highest Risk)
> **Target**: De-risk the most critical financial processing first

| Program | Reason |
|---------|--------|
| CBTRN02C | Core transaction posting -- financial accuracy is paramount |
| CBACT04C | Interest calculation -- regulatory compliance |
| COBIL00C | Bill payment -- customer money handling |

### Wave 2 -- Account & Card Management (Highest Complexity)
> **Target**: Modernize the largest, most complex modules

| Program | Reason |
|---------|--------|
| COACTUPC | Largest program, needs decomposition into services |
| COCRDUPC | Second largest online program, shares patterns with COACTUPC |
| COACTVWC | High-traffic read path, good candidate for API + caching |

### Wave 3 -- Browse & Reporting (UI-Heavy)
> **Target**: Replace CICS pseudo-conversational patterns with modern UX

| Program | Reason |
|---------|--------|
| COCRDLIC | Pagination logic maps to modern paginated APIs |
| COTRN02C | Transaction entry maps to a web form |
| COCRDSLC | Card detail maps to a detail page |
| CBSTM03A | Statement generation maps to a batch reporting service |

### Wave 4 -- Administration & Utilities
> **Target**: Lower risk, straightforward modernization

| Programs | Reason |
|----------|--------|
| COUSR00C-03C | User CRUD maps directly to a REST API |
| COSGN00C | Authentication maps to Spring Security/OAuth2 |
| COMEN01C/COADM01C | Menus become frontend routing |
| Batch utilities | Simple file readers become database queries |

---

## Key Risk Areas for Modernization

| Risk Area | Affected Programs | Mitigation |
|-----------|-------------------|------------|
| **Decimal Precision** | CBTRN02C, CBACT04C, COBIL00C | Use `BigDecimal` everywhere; never use `float`/`double` for money |
| **Data Integrity** | COACTUPC, CBTRN02C | Implement database transactions; add optimistic locking |
| **PII/PCI Compliance** | COACTUPC, COCRDSLC, COSGN00C | Encrypt SSN/card data at rest; mask in display; hash passwords |
| **Pseudo-Conversational State** | All online programs | Replace COMMAREA state with server-side sessions or JWT tokens |
| **VSAM to RDBMS Migration** | All programs | Map copybook layouts to JPA entities; preserve key structures |
| **Batch Sequencing** | POSTTRAN, INTCALC, CREASTMT | Implement Spring Batch job orchestration with proper error recovery |
| **Alternate Index Queries** | COCRDLIC, COACTVWC, COBIL00C | Replace with database indexes and JOIN queries |
