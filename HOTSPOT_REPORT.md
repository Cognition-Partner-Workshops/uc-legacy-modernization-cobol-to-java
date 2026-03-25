# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Application:** CardDemo (Credit Card Management System)
> **Methodology:** Weighted scoring across Code Complexity, Data Coupling, Business Risk, and Modernization Effort

---

## Executive Summary

This report identifies the **Top 10 highest-risk modules** in the CardDemo COBOL codebase, prioritized for modernization planning. Each module is scored on four dimensions and ranked by a composite weighted score. The single largest hotspot is **COACTUPC** (Account Update) at 4,237 lines -- nearly as large as the next 5 programs combined -- which handles the most complex business logic including multi-entity updates with extensive field validation.

---

## Scoring Methodology

Each module is scored 1-10 on four dimensions:

| Dimension | Weight | What It Measures |
|-----------|--------|-----------------|
| **Code Complexity** | 30% | Lines of code, cyclomatic complexity (EVALUATE/IF nesting), number of PERFORMs, COPY statements |
| **Data Coupling** | 25% | Number of VSAM files accessed, copybooks included, cross-entity dependencies |
| **Business Risk** | 25% | Financial impact, data sensitivity (PII/PCI), regulatory exposure, user-facing criticality |
| **Modernization Effort** | 20% | Estimated conversion difficulty, CICS-specific constructs, screen complexity, testing surface |

**Composite Score** = (Complexity x 0.30) + (Coupling x 0.25) + (Risk x 0.25) + (Effort x 0.20)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC -- Account Update

| Metric | Value | Score |
|--------|-------|-------|
| **Lines of Code** | 4,237 | -- |
| **Type** | Online CICS | -- |
| **CICS Transaction** | CAUP | -- |
| **Code Complexity** | Deepest nesting, 50+ validation rules, date/phone/SSN editing, 5x CSSETATY REPLACING macros | **10** |
| **Data Coupling** | 5 VSAM files (ACCTDAT RW, CUSTDAT R, CARDDAT R, CARDAIX R, CXACAIX R), 15 copybooks | **9** |
| **Business Risk** | Modifies account balances, credit limits, customer PII (SSN, DOB, address, phone). Financial data integrity critical. | **10** |
| **Modernization Effort** | Most complex BMS map (512 lines), extensive field-level attribute manipulation, multi-entity read-modify-write, date validation logic | **10** |
| **Composite Score** | | **9.75** |

**Why #1:** This is by far the largest and most complex program in the codebase. It touches more data entities than any other online program, performs extensive input validation (US phone numbers, SSN, dates, financial amounts), and directly modifies financial data. A bug here could corrupt account balances or expose PII. It alone represents ~20% of all online COBOL code.

**Modernization Recommendations:**
- Decompose into separate services: AccountService, CustomerService, ValidationService
- Extract the 50+ validation rules into a reusable validation framework
- Use Bean Validation (JSR 380) annotations to replace manual field edits
- Implement optimistic locking for concurrent account updates
- Add audit logging for all financial field changes

---

### Rank 2: CBTRN02C -- Transaction Posting (Core)

| Metric | Value | Score |
|--------|-------|-------|
| **Lines of Code** | 731 | -- |
| **Type** | Batch | -- |
| **Code Complexity** | Complex transaction matching, cross-reference lookups, reject handling, file status error paths | **7** |
| **Data Coupling** | 5 files (DALYTRAN R, TRANFILE RW, XREFFILE R, DALYREJS W, ACCTFILE R), 5 copybooks | **8** |
| **Business Risk** | Core financial posting -- creates permanent transaction records, updates master file. Rejects must be tracked. Data loss = financial loss. | **10** |
| **Modernization Effort** | Sequential file processing, multi-file coordination, reject file generation, must maintain exact posting semantics | **8** |
| **Composite Score** | | **8.25** |

**Why #2:** This is the financial heart of the batch cycle. Every daily transaction flows through this program. Incorrect posting means incorrect balances, incorrect statements, and incorrect interest calculations downstream. The reject handling logic is critical for auditability.

**Modernization Recommendations:**
- Implement as Spring Batch job with `ItemReader`/`ItemProcessor`/`ItemWriter`
- Use database transactions (ACID) instead of sequential file updates
- Implement dead-letter queue pattern for rejects instead of flat file
- Add comprehensive logging and reconciliation reports
- Consider event sourcing for full transaction audit trail

---

### Rank 3: CBACT04C -- Interest Calculation

| Metric | Value | Score |
|--------|-------|-------|
| **Lines of Code** | 652 | -- |
| **Type** | Batch | -- |
| **Code Complexity** | Financial arithmetic (S9(10)V99), category-based rate lookup, account balance updates, disclosure group matching | **8** |
| **Data Coupling** | 5 files (TCATBALF R, XREFFILE R, ACCTFILE RW, DISCGRP R, TRANSACT RW) | **8** |
| **Business Risk** | Directly calculates and applies interest to accounts. Incorrect calculation = financial liability. Regulatory compliance (Truth in Lending Act). | **10** |
| **Modernization Effort** | Decimal precision critical (COMP-3 arithmetic), disclosure group lookup logic, must match penny-for-penny with mainframe output | **9** |
| **Composite Score** | | **8.75** |

**Why #3:** Interest calculation is the most financially sensitive batch program. Errors here compound -- every account is affected, and incorrect interest rates could trigger regulatory violations. The arithmetic must be preserved exactly during conversion (COBOL COMP-3 to Java BigDecimal).

**Modernization Recommendations:**
- Use `BigDecimal` exclusively (never `double`/`float`) for all monetary calculations
- Implement comprehensive unit tests with penny-level precision validation
- Run parallel processing (old + new) during transition period to verify results match
- Externalize interest rate configuration (disclosure groups) to database/config
- Document all rounding rules explicitly

---

### Rank 4: COCRDLIC -- Credit Card List

| Metric | Value | Score |
|--------|-------|-------|
| **Lines of Code** | 1,460 | -- |
| **Type** | Online CICS | -- |
| **Code Complexity** | Pagination logic (PF7/PF8), VSAM browse (STARTBR/READNEXT/ENDBR), 7-row selection array, forward/backward navigation | **8** |
| **Data Coupling** | 2 files (CARDDAT R, CARDAIX R), 10 copybooks, navigates to COCRDSLC and COCRDUPC | **6** |
| **Business Risk** | Displays card numbers (PCI-DSS scope). List is primary navigation for card operations. | **7** |
| **Modernization Effort** | VSAM browse/pagination maps to SQL cursor or paginated query. Row-level selection (S/U) maps to REST endpoints. Complex state management across pages. | **8** |
| **Composite Score** | | **7.30** |

**Why #4:** Second-largest online program. The pagination logic with forward/backward browse using VSAM STARTBR/READNEXT is the most complex data retrieval pattern in the online system. Card numbers displayed here are PCI-DSS sensitive.

**Modernization Recommendations:**
- Implement server-side pagination with Spring Data JPA `Pageable`
- Mask card numbers in display (show only last 4 digits)
- Replace row-level S/U selection with hyperlinks or action buttons
- Add card number search/filter capability

---

### Rank 5: COCRDUPC -- Credit Card Update

| Metric | Value | Score |
|--------|-------|-------|
| **Lines of Code** | 1,560 | -- |
| **Type** | Online CICS | -- |
| **Code Complexity** | Multi-field validation, REWRITE operations, date validation, status changes | **7** |
| **Data Coupling** | 3 files (CARDDAT RW, CARDAIX R, CUSTDAT R), 13 copybooks | **7** |
| **Business Risk** | Modifies card status and details. Incorrect update could activate/deactivate cards improperly. | **8** |
| **Modernization Effort** | Read-modify-write pattern on VSAM, field-level validation, BMS attribute manipulation | **7** |
| **Composite Score** | | **7.25** |

**Why #5:** Third-largest program overall. Card updates are sensitive operations (activating/deactivating cards, changing embossed names). The read-modify-write pattern on VSAM requires careful transaction handling in the modernized version.

**Modernization Recommendations:**
- Implement with JPA `@Transactional` for atomicity
- Add card status change audit trail
- Implement field-level change detection
- Add authorization checks for sensitive operations (status changes)

---

### Rank 6: CBSTM03A -- Statement Generation (Main)

| Metric | Value | Score |
|--------|-------|-------|
| **Lines of Code** | 924 | -- |
| **Type** | Batch | -- |
| **Code Complexity** | Multi-format output (text + HTML), sub-program calls (CBSTM03B x10), cross-reference traversal, customer/account lookups | **7** |
| **Data Coupling** | 4 input files + 2 output files, calls CBSTM03B sub-program repeatedly, 4 copybooks | **7** |
| **Business Risk** | Customer-facing output. Statement errors cause customer complaints and potential regulatory issues. | **8** |
| **Modernization Effort** | Dual output format (text + HTML), sub-program CALL interface, sequential file generation, print formatting | **7** |
| **Composite Score** | | **7.25** |

**Why #6:** Statement generation is the primary customer-facing batch output. It uses a sub-program architecture (CBSTM03A calls CBSTM03B for each card's transactions), produces both text and HTML output, and traverses the entire cross-reference file to generate per-card statements.

**Modernization Recommendations:**
- Implement with Spring Batch multi-step job
- Use a templating engine (Thymeleaf/FreeMarker) for HTML statements
- Replace sub-program CALL with service method invocation
- Consider PDF generation (replacing TXT2PDF1 JCL utility)
- Add email delivery capability

---

### Rank 7: COACTVWC -- Account View

| Metric | Value | Score |
|--------|-------|-------|
| **Lines of Code** | 942 | -- |
| **Type** | Online CICS | -- |
| **Code Complexity** | Multi-entity data assembly (account + customer + cards via xref), abend handling, complex screen population | **6** |
| **Data Coupling** | 4 files (ACCTDAT R, CUSTDAT R, CXACAIX R, CARDAIX R), 14 copybooks | **7** |
| **Business Risk** | Displays financial data and customer PII. Read-only but information exposure risk. | **6** |
| **Modernization Effort** | Multi-entity join pattern, complex BMS map (378 lines), abend handling, string formatting | **6** |
| **Composite Score** | | **6.25** |

**Why #7:** This is the read-only counterpart to COACTUPC. While simpler (no writes), it assembles data from 4 different VSAM files to present a unified account view -- a pattern that maps naturally to a SQL JOIN but requires careful query optimization.

**Modernization Recommendations:**
- Implement as a single REST endpoint returning a composite DTO
- Use JPA `@EntityGraph` or custom query for efficient multi-entity fetch
- Apply field-level access control for PII fields
- Cache frequently accessed account views

---

### Rank 8: COTRN02C -- Transaction Add

| Metric | Value | Score |
|--------|-------|-------|
| **Lines of Code** | 783 | -- |
| **Type** | Online CICS | -- |
| **Code Complexity** | Date validation (CALL CSUTLDTC), cross-reference lookup, transaction ID generation, multi-file write | **7** |
| **Data Coupling** | 4 files (TRANSACT RW, CXACAIX R, CCXREF R, ACCTDAT R), 10 copybooks | **7** |
| **Business Risk** | Creates financial transactions. Duplicate or incorrect transactions = financial loss. | **8** |
| **Modernization Effort** | Transaction ID generation, date validation subroutine call, multi-step write (validate → generate ID → write), balance validation | **6** |
| **Composite Score** | | **7.00** |

**Why #8:** This is the online transaction entry point where new transactions are created. It validates card numbers against the cross-reference, generates unique transaction IDs, and writes to the transaction master. Duplicate prevention and amount validation are critical.

**Modernization Recommendations:**
- Implement idempotency (prevent duplicate transaction creation)
- Use database sequences for transaction ID generation
- Add real-time balance checking before transaction approval
- Implement transaction amount limits and velocity checks

---

### Rank 9: CBTRN03C -- Transaction Detail Report

| Metric | Value | Score |
|--------|-------|-------|
| **Lines of Code** | 649 | -- |
| **Type** | Batch | -- |
| **Code Complexity** | Report formatting with headers/footers, page/account/grand totals, cross-reference lookups for type/category descriptions, date range filtering | **6** |
| **Data Coupling** | 5 input files (TRANFILE, CARDXREF, TRANTYPE, TRANCATG) + 1 output (TRANREPT), 5 copybooks | **7** |
| **Business Risk** | Audit/compliance report. Incorrect totals could mask financial discrepancies. Used for reconciliation. | **7** |
| **Modernization Effort** | Report layout with page breaks, running totals, control breaks on account, print formatting to fixed-width output | **6** |
| **Composite Score** | | **6.50** |

**Why #9:** This is the primary financial reconciliation report. It reads from 4 input files and produces a detailed transaction report with page totals, account totals, and grand totals. The control-break reporting pattern is common in COBOL but requires careful reimplementation.

**Modernization Recommendations:**
- Implement with JasperReports or Spring Batch with `FlatFileItemWriter`
- Use SQL aggregation for totals instead of programmatic accumulation
- Add report parameter support (date range, account filter)
- Generate PDF output directly
- Consider a reporting dashboard (e.g., Grafana, custom UI)

---

### Rank 10: COBIL00C -- Bill Payment

| Metric | Value | Score |
|--------|-------|-------|
| **Lines of Code** | 572 | -- |
| **Type** | Online CICS | -- |
| **Code Complexity** | Payment processing, balance calculation, transaction creation, account update | **6** |
| **Data Coupling** | 3 files (ACCTDAT RW, TRANSACT RW, CXACAIX R), 10 copybooks | **6** |
| **Business Risk** | Directly debits account balance and creates payment transaction. Financial integrity critical. | **9** |
| **Modernization Effort** | Combined read-modify-write on two files (ACCTDAT + TRANSACT), balance verification, payment confirmation flow | **6** |
| **Composite Score** | | **6.75** |

**Why #10:** Bill payment combines transaction creation with account balance modification in a single operation. This is a critical financial flow where atomicity is essential -- a payment that creates a transaction but fails to update the balance (or vice versa) would create a financial discrepancy.

**Modernization Recommendations:**
- Implement with `@Transactional` to ensure atomicity of payment + balance update
- Add payment confirmation step (two-phase: preview → confirm)
- Implement payment limits and fraud detection hooks
- Add payment receipt/confirmation number generation
- Consider async payment processing for larger amounts

---

## Composite Ranking Summary

| Rank | Program | LOC | Type | Complexity | Coupling | Risk | Effort | **Composite** |
|------|---------|-----|------|-----------|----------|------|--------|--------------|
| 1 | **COACTUPC** | 4,237 | Online | 10 | 9 | 10 | 10 | **9.75** |
| 2 | **CBACT04C** | 652 | Batch | 8 | 8 | 10 | 9 | **8.75** |
| 3 | **CBTRN02C** | 731 | Batch | 7 | 8 | 10 | 8 | **8.25** |
| 4 | **COCRDLIC** | 1,460 | Online | 8 | 6 | 7 | 8 | **7.30** |
| 5 | **COCRDUPC** | 1,560 | Online | 7 | 7 | 8 | 7 | **7.25** |
| 6 | **CBSTM03A** | 924 | Batch | 7 | 7 | 8 | 7 | **7.25** |
| 7 | **COTRN02C** | 783 | Online | 7 | 7 | 8 | 6 | **7.00** |
| 8 | **COBIL00C** | 572 | Online | 6 | 6 | 9 | 6 | **6.75** |
| 9 | **CBTRN03C** | 649 | Batch | 6 | 7 | 7 | 6 | **6.50** |
| 10 | **COACTVWC** | 942 | Online | 6 | 7 | 6 | 6 | **6.25** |

---

## Risk Heat Map

```
                    Low Coupling          Medium Coupling         High Coupling
                    (1-3 files)           (4-5 files)             (5+ files)
                 ┌─────────────────┬─────────────────────┬─────────────────────┐
 High Business   │                 │  COBIL00C            │  COACTUPC           │
 Risk            │                 │  COTRN02C            │  CBTRN02C           │
 (Financial/PII) │                 │                      │  CBACT04C           │
                 ├─────────────────┼─────────────────────┼─────────────────────┤
 Medium Business │  COUSR00C-03C   │  COCRDLIC            │  CBSTM03A           │
 Risk            │  COTRN00C       │  COCRDUPC            │  CBTRN03C           │
                 │  COTRN01C       │  COACTVWC            │                     │
                 ├─────────────────┼─────────────────────┼─────────────────────┤
 Low Business    │  COBSWAIT       │  CBACT01C            │  CBEXPORT           │
 Risk            │  CSUTLDTC       │  CBACT02C/03C        │  CBIMPORT           │
                 │  COSGN00C       │  CBCUS01C            │                     │
                 └─────────────────┴─────────────────────┴─────────────────────┘
```

---

## Key Findings & Recommendations

### Finding 1: COACTUPC is the Elephant in the Room
At 4,237 lines, COACTUPC is **3x larger** than the next largest online program and represents a disproportionate modernization risk. It should be **decomposed into at least 3-4 separate services** during migration.

### Finding 2: Financial Batch Programs are High-Stakes
CBTRN02C (posting) and CBACT04C (interest) are the two most financially critical programs. They must be migrated with **penny-level precision validation** and parallel-run verification.

### Finding 3: Cross-Reference File is a Bottleneck
CVACT03Y (Card Cross-Reference) is used by 12 programs. In the modernized system, this should become a properly indexed database relationship rather than a separate lookup file.

### Finding 4: COMMAREA is a Universal Coupling Point
COCOM01Y is included in all 17 online programs. The session management architecture must be designed early and carefully, as it affects every online program's conversion.

### Finding 5: PCI-DSS and PII Scope is Broad
Card numbers appear in 8+ programs, SSN in COACTUPC, passwords in plaintext in CSUSR01Y. The modernized system needs:
- Card number tokenization/masking
- SSN encryption at rest
- Password hashing (bcrypt/argon2)
- PCI-DSS compliant data handling throughout

### Recommended Modernization Priority
1. **Foundation:** Auth/Session (COCOM01Y, CSUSR01Y, COSGN00C) -- establishes the security framework
2. **Quick Wins:** Read-only screens (COACTVWC, COTRN00C, COTRN01C) -- low risk, high visibility
3. **Core Business:** COACTUPC (decomposed), COCRDUPC, COTRN02C -- highest value
4. **Financial Engine:** CBTRN02C, CBACT04C -- highest risk, needs parallel run
5. **Reporting:** CBSTM03A/B, CBTRN03C -- can run legacy until end
