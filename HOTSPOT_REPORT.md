# Hotspot Report - CardDemo COBOL Codebase

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management)
> **Purpose:** Identify the top 10 modules prioritized by complexity, risk, and business impact for modernization planning

---

## Scoring Methodology

Each module is scored across three dimensions (1-10 scale each):

| Dimension          | Weight | Criteria                                                                                   |
|--------------------|-------:|--------------------------------------------------------------------------------------------|
| **Complexity**     |   40%  | Lines of code, number of copybooks, file I/O operations, validation logic depth, PERFORM nesting |
| **Risk**           |   30%  | Data sensitivity (PII/PCI), write operations, cross-file updates, error handling complexity  |
| **Business Impact**|   30%  | User-facing criticality, financial calculations, transaction volume, downstream dependencies  |

**Composite Score** = (Complexity × 0.4) + (Risk × 0.3) + (Business Impact × 0.3)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC — Account Update

| Metric             | Value / Detail                                                              |
|--------------------|-----------------------------------------------------------------------------|
| **LOC**            | 4,236 (largest program in codebase — 2.7× the next largest)                |
| **Copybooks**      | 15 (COCOM01Y, COTTL01Y, COACTUPC, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, DFHBMSCA, DFHAID, CSUTLDWY, CSUTLDPY, CSLKPCDY) |
| **VSAM Files**     | 3 (ACCTDAT RW, CUSTDAT RW, XREFDAT R)                                      |
| **Complexity**     | **10/10** — Extensive input validation: date (year, month, day, leap year, DOB), SSN, phone area code, state code, ZIP code cross-validation. 1,318-line lookup table (CSLKPCDY). Multiple REWRITE operations. |
| **Risk**           | **9/10** — Writes to both Account AND Customer master files. Handles SSN, DOB, phone (PII). Complex error recovery with multiple edit flags. |
| **Business Impact**| **8/10** — Core account maintenance function. Data integrity errors here cascade to billing, statements, and reporting. |
| **Composite Score**| **9.1** |

**Modernization Notes:**
- Break into separate Account and Customer update services
- Extract validation logic into reusable validation framework
- Replace 1,318-line lookup copybook (CSLKPCDY) with database-driven reference data
- Implement proper PII encryption for SSN, DOB, phone numbers
- The inline date validation (CSUTLDWY/CSUTLDPY) should become a shared Java utility class

---

### Rank 2: CBTRN02C — Transaction Posting (Batch)

| Metric             | Value / Detail                                                              |
|--------------------|-----------------------------------------------------------------------------|
| **LOC**            | 731                                                                         |
| **Copybooks**      | 6 (CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y)                      |
| **Files**          | 6 (DALYTRAN R, XREFFILE R, ACCTFILE R, TRANFILE W, DALYREJS W, TCATBALF RW)|
| **Complexity**     | **8/10** — Multi-file coordination: reads daily transactions, validates against cross-reference, posts to master, updates category balances, writes rejects. Complex file status handling. |
| **Risk**           | **10/10** — Core financial posting engine. Incorrect posting = incorrect balances. Writes to 3 different files atomically. Rejected transaction handling is critical for audit. |
| **Business Impact**| **10/10** — Central to the entire batch cycle. All downstream processes (interest calc, statements, reports) depend on correct posting. |
| **Composite Score**| **9.2** |

**Modernization Notes:**
- Convert to Spring Batch job with chunk-oriented processing
- Implement database transactions for atomicity (replace multi-file coordination)
- Add comprehensive logging and audit trail
- Consider event-driven architecture for real-time posting

---

### Rank 3: CBACT04C — Interest Calculation (Batch)

| Metric             | Value / Detail                                                              |
|--------------------|-----------------------------------------------------------------------------|
| **LOC**            | 652                                                                         |
| **Copybooks**      | 5 (CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y)                      |
| **Files**          | 5 (TCATBALF R, XREFFILE R, ACCTFILE R, DISCGRP R, TRANSACT R → TCATBALF W) |
| **Complexity**     | **8/10** — Financial calculation logic: reads disclosure group rates, applies to category balances per account. Multiple file lookups per account. Date-parameterized processing. |
| **Risk**           | **10/10** — Interest calculation errors directly impact customer billing. Financial regulatory compliance implications. Rate lookup from DISCGRP must be precise. |
| **Business Impact**| **9/10** — Directly affects customer statements and account balances. Regulatory reporting depends on accurate interest calculations. |
| **Composite Score**| **8.9** |

**Modernization Notes:**
- Implement as a dedicated financial calculation microservice
- Use BigDecimal for all monetary calculations (avoid floating-point)
- Add comprehensive unit tests for interest calculation edge cases
- Externalize interest rate configuration (database or config service)
- Implement audit logging for all rate applications

---

### Rank 4: COCRDUPC — Credit Card Update

| Metric             | Value / Detail                                                              |
|--------------------|-----------------------------------------------------------------------------|
| **LOC**            | 1,560                                                                       |
| **Copybooks**      | 13 (CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY) |
| **VSAM Files**     | 1 (CARDDAT RW)                                                              |
| **Complexity**     | **8/10** — Card data validation, expiration date logic, status management. Includes PF-key handling (CSSTRPFY) and abend processing (CSMSG02Y). |
| **Risk**           | **8/10** — Writes to card master file. Card numbers are PCI-DSS scope. CVV handling present. |
| **Business Impact**| **7/10** — Card status changes affect transaction authorization. Card data integrity is critical for payment processing. |
| **Composite Score**| **7.7** |

**Modernization Notes:**
- Implement PCI-DSS compliant card data handling (tokenization)
- Never store CVV post-authorization in modernized system
- Add card number validation (Luhn algorithm) if not already present
- Separate card lifecycle management into its own bounded context

---

### Rank 5: COCRDLIC — Credit Card List

| Metric             | Value / Detail                                                              |
|--------------------|-----------------------------------------------------------------------------|
| **LOC**            | 1,459                                                                       |
| **Copybooks**      | 11 (CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDLI, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, CSSTRPFY) |
| **VSAM Files**     | 1 (CARDDAT R — browse/STARTBR/READNEXT/READPREV/ENDBR)                     |
| **Complexity**     | **8/10** — Implements server-side pagination with VSAM browse operations (STARTBR, READNEXT, READPREV, ENDBR, RESETBR). 7-row display with forward/backward scrolling. Filter by account and card number. Complex cursor positioning logic. |
| **Risk**           | **5/10** — Read-only access. Card numbers displayed (PCI consideration for masking). |
| **Business Impact**| **6/10** — Primary card lookup interface. High usage frequency for customer service. |
| **Composite Score**| **6.5** |

**Modernization Notes:**
- Replace VSAM browse with SQL pagination (OFFSET/FETCH or keyset pagination)
- Implement card number masking in display (show only last 4 digits)
- Convert to REST API with standard pagination parameters
- Consider search/filter as query parameters

---

### Rank 6: CBSTM03A — Statement Generation (Batch)

| Metric             | Value / Detail                                                              |
|--------------------|-----------------------------------------------------------------------------|
| **LOC**            | 924                                                                         |
| **Copybooks**      | 4 (COSTM01, CVACT03Y, CUSTREC, CVACT01Y)                                  |
| **Files**          | 4 inputs (sorted TRNX, XREFFILE, CUSTFILE, ACCTFILE) + 2 outputs (STMTFILE, HTMLFILE) |
| **Complexity**     | **7/10** — Reads sorted transaction file, joins with customer and account data via cross-reference, generates formatted print output and HTML statements. Control break logic for account-level subtotals. |
| **Risk**           | **7/10** — Statement accuracy is customer-facing. Contains all PII (customer name, address, transactions). Generates both print and HTML output formats. |
| **Business Impact**| **8/10** — Customer-facing output. Regulatory requirement for periodic statements. Errors are directly visible to customers. |
| **Composite Score**| **7.2** |

**Modernization Notes:**
- Convert to a reporting/document generation service
- Use a templating engine (Thymeleaf, Jasper) for statement formatting
- Implement PDF generation (replacing mainframe print + TXT2PDF1)
- Add email delivery capability for digital statements
- Ensure PII masking in statement archives

---

### Rank 7: COACTVWC — Account View

| Metric             | Value / Detail                                                              |
|--------------------|-----------------------------------------------------------------------------|
| **LOC**            | 941                                                                         |
| **Copybooks**      | 13 (CVCRD01Y, COCOM01Y, COTTL01Y, COACTVW, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY) |
| **VSAM Files**     | 4 (ACCTDAT R, CARDDAT R, CUSTDAT R, XREFDAT R)                             |
| **Complexity**     | **7/10** — Reads from 4 different VSAM files to compose a single view. Cross-reference lookup pattern. File error handling for each file. PF-key handling. |
| **Risk**           | **4/10** — Read-only operations. No data mutation risk. Displays PII (customer data). |
| **Business Impact**| **7/10** — Primary account inquiry screen. High usage for customer service operations. Foundation for account-level workflows. |
| **Composite Score**| **6.1** |

**Modernization Notes:**
- Convert to a read-only API endpoint that joins Account + Customer + Card data
- Implement a materialized view or denormalized read model for performance
- Add response caching for frequently accessed accounts

---

### Rank 8: CBTRN03C — Transaction Report (Batch)

| Metric             | Value / Detail                                                              |
|--------------------|-----------------------------------------------------------------------------|
| **LOC**            | 649                                                                         |
| **Copybooks**      | 5 (CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y)                      |
| **Files**          | 6 (TRANFILE R, CARDXREF R, TRANTYPE R, TRANCATG R, DATEPARM R → TRANREPT W)|
| **Complexity**     | **7/10** — Multi-file join for report generation. Date range filtering via parameter file. Report formatting with headers, detail lines, page totals, account totals, and grand totals. Type and category description lookups. |
| **Risk**           | **6/10** — Report accuracy for management and regulatory review. Contains transaction amounts and card references. |
| **Business Impact**| **7/10** — Daily management reporting. Used for reconciliation and audit purposes. |
| **Composite Score**| **6.7** |

**Modernization Notes:**
- Convert to a reporting service with parameterized queries
- Use a reporting framework (JasperReports, BIRT) for output formatting
- Support multiple output formats (PDF, CSV, Excel)
- Implement date range and filter parameters as API query parameters

---

### Rank 9: COTRN02C — Transaction Add (Online)

| Metric             | Value / Detail                                                              |
|--------------------|-----------------------------------------------------------------------------|
| **LOC**            | 783                                                                         |
| **Copybooks**      | 10 (COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y, DFHAID, DFHBMSCA) |
| **VSAM Files**     | 3 (TRANSACT RW, XREFDAT R via 2 paths)                                     |
| **Complexity**     | **7/10** — Online transaction creation with card-to-account cross-reference validation. Timestamp generation. Multiple VSAM access paths (alternate index for account lookup). |
| **Risk**           | **8/10** — Creates financial transactions. Write to transaction master. Card cross-reference validation is critical for correct account posting. |
| **Business Impact**| **8/10** — Direct transaction entry point. Financial data creation. |
| **Composite Score**| **7.5** |

**Modernization Notes:**
- Convert to a transaction creation REST API endpoint
- Implement idempotency keys to prevent duplicate transactions
- Add real-time validation against account status and card status
- Consider event sourcing pattern for transaction creation audit trail

---

### Rank 10: COBIL00C — Bill Payment

| Metric             | Value / Detail                                                              |
|--------------------|-----------------------------------------------------------------------------|
| **LOC**            | 572                                                                         |
| **Copybooks**      | 9 (COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y, DFHAID, DFHBMSCA) |
| **VSAM Files**     | 3 (ACCTDAT R, TRANSACT RW, XREFDAT R)                                      |
| **Complexity**     | **6/10** — Payment processing: validates account, creates transaction record, cross-reference lookup. Moderate validation logic. |
| **Risk**           | **9/10** — Financial payment processing. Creates transaction records and affects account balances. Incorrect payments have direct customer impact. |
| **Business Impact**| **8/10** — Core payment function. Customer-facing financial operation. |
| **Composite Score**| **7.5** |

**Modernization Notes:**
- Convert to a payment processing microservice
- Implement payment confirmation and receipt generation
- Add payment amount validation (minimum, maximum, account balance check)
- Consider integration with payment gateway for external payments
- Implement proper transaction isolation for concurrent payments

---

## Summary Ranking Table

| Rank | Program    | LOC   | Type    | Complexity | Risk | Impact | **Composite** | Primary Concern                        |
|-----:|------------|------:|---------|:----------:|:----:|:------:|:-------------:|----------------------------------------|
|    1 | COACTUPC   | 4,236 | Online  |    10      |   9  |    8   |    **9.1**    | Massive size, dual-file writes, PII    |
|    2 | CBTRN02C   |   731 | Batch   |     8      |  10  |   10   |    **9.2**    | Core posting engine, multi-file atomic |
|    3 | CBACT04C   |   652 | Batch   |     8      |  10  |    9   |    **8.9**    | Financial calculations, regulatory     |
|    4 | COCRDUPC   | 1,560 | Online  |     8      |   8  |    7   |    **7.7**    | PCI-DSS scope, card data writes        |
|    5 | COCRDLIC   | 1,459 | Online  |     8      |   5  |    6   |    **6.5**    | Complex pagination, high usage         |
|    6 | CBSTM03A   |   924 | Batch   |     7      |   7  |    8   |    **7.2**    | Customer-facing output, PII exposure   |
|    7 | COACTVWC   |   941 | Online  |     7      |   4  |    7   |    **6.1**    | 4-file join, foundation for workflows  |
|    8 | CBTRN03C   |   649 | Batch   |     7      |   6  |    7   |    **6.7**    | 6-file join, management reporting      |
|    9 | COTRN02C   |   783 | Online  |     7      |   8  |    8   |    **7.5**    | Transaction creation, financial write  |
|   10 | COBIL00C   |   572 | Online  |     6      |   9  |    8   |    **7.5**    | Payment processing, financial risk     |

---

## Modernization Priority Recommendations

### Phase 1 — High Risk / High Impact (Immediate)
1. **CBTRN02C** (Transaction Posting) — Core financial engine; convert to Spring Batch with DB transactions
2. **CBACT04C** (Interest Calculation) — Regulatory-sensitive financial logic; needs thorough test coverage
3. **COBIL00C** (Bill Payment) — Customer-facing financial operation; needs payment gateway integration

### Phase 2 — High Complexity / Data Sensitivity
4. **COACTUPC** (Account Update) — Largest program; decompose into Account + Customer services
5. **COCRDUPC** (Card Update) — PCI-DSS scope; implement tokenization early
6. **COTRN02C** (Transaction Add) — Online transaction creation; add idempotency and validation

### Phase 3 — Reporting & Read Operations
7. **CBSTM03A** (Statement Generation) — Convert to modern reporting framework with PDF/email
8. **CBTRN03C** (Transaction Report) — Convert to parameterized reporting service
9. **COACTVWC** (Account View) — Read-only; straightforward API conversion
10. **COCRDLIC** (Card List) — Pagination logic; convert to SQL-backed paginated API

### Cross-Cutting Concerns (All Phases)
- **Security:** Replace plain-text passwords (CSUSR01Y) with hashed credentials
- **PII:** Implement field-level encryption for SSN, DOB, Government ID
- **PCI-DSS:** Tokenize card numbers, never persist CVV
- **Date Handling:** Consolidate CSUTLDTC/CSUTLDWY/CSUTLDPY into `java.time` utilities
- **Lookup Data:** Move CSLKPCDY (1,318 lines of area codes/states/ZIPs) to database reference tables
- **COMMAREA:** Replace with HTTP session or JWT token-based state management
- **Error Handling:** Replace CICS abend handling with structured exception handling

---

## Complexity Metrics Summary

| Metric                          | Value    |
|---------------------------------|----------|
| Total COBOL LOC (core)          | 20,650   |
| Total COBOL LOC (optional)      | 9,525    |
| Total Copybook LOC              | 2,786    |
| Total BMS Map LOC               | 4,472    |
| Largest Program                 | COACTUPC (4,236 LOC) |
| Average Program LOC (core)      | 666      |
| Programs > 1,000 LOC            | 3 (COACTUPC, COCRDUPC, COCRDLIC) |
| Programs accessing 3+ files     | 7        |
| Programs with write operations  | 11       |
| Total VSAM files                | 12       |
| Total JCL jobs                  | 38       |
| Total copybooks                 | 30       |
| Total BMS maps                  | 17       |
| Copybook with most consumers    | COCOM01Y (17 programs) |
| Most-accessed VSAM file         | CARDXREF (10+ programs) |
