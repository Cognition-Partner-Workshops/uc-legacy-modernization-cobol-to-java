# CardDemo Hotspot Report -- Top 10 Modernization Priority Modules

> **Generated**: 2026-03-25 | **Methodology**: Weighted scoring across code complexity, integration risk, data sensitivity, and business impact

## Scoring Methodology

Each module is scored on four dimensions (1-5 scale, 5 = highest risk/priority):

| Dimension | Weight | What It Measures |
|---|---|---|
| **Code Complexity** | 30% | Lines of code, cyclomatic complexity indicators (EVALUATE/IF nesting, PERFORM loops), number of COPY includes, REDEFINES usage |
| **Integration Risk** | 25% | Number of VSAM files accessed, CICS commands, external system calls (MQ/DB2/IMS), inter-program dependencies |
| **Data Sensitivity** | 20% | PII fields handled, financial data mutations, security-critical operations |
| **Business Impact** | 25% | Revenue criticality, user-facing importance, batch pipeline centrality |

**Composite Score** = (Complexity × 0.30) + (Integration × 0.25) + (Data Sensitivity × 0.20) + (Business Impact × 0.25)

---

## Top 10 Hotspot Modules

### #1: COACTUPC -- Account Update (Online)

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **5** | 4,236 lines -- largest program in the codebase. Heavy EVALUATE/IF nesting for field-level validation. 56 COPY includes. Complex screen interaction with multiple SEND/RECEIVE MAP cycles. |
| Integration Risk | **5** | 17 EXEC CICS commands (5 READ, 1 REWRITE, 1 SEND MAP, 1 RECEIVE MAP, 1 XCTL). Accesses ACCTDATA + CUSTDATA VSAM files with READ and REWRITE. 14 copybook dependencies. |
| Data Sensitivity | **5** | Directly mutates account balances, credit limits, and customer PII. Financial data changes with no audit trail in code. |
| Business Impact | **5** | Core account management function. Any defect directly impacts customer accounts and financial records. |
| **Composite Score** | **5.00** | |

**Modernization Risks:**
- Largest single program -- requires careful decomposition into multiple Java services/controllers
- Field-by-field validation logic should map to Bean Validation annotations
- Screen state management (pseudo-conversational CICS) maps to stateful web forms or SPA state
- Must implement optimistic locking to replace CICS single-user VSAM record locking

**Recommended Approach:** Decompose into `AccountController` + `AccountService` + `AccountValidator`. Extract validation rules into a separate service. Implement `@Transactional` with optimistic locking.

---

### #2: CBTRN02C -- Daily Transaction Posting (Batch)

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **4** | 731 lines. Multiple file operations across 6 VSAM files. Complex posting logic with reject handling. Category balance updates. |
| Integration Risk | **5** | Reads DALYTRAN (sequential) + CARDXREF + ACCTDATA (KSDS). Writes to TRANSACT + updates ACCTDATA + TCATBALF. Writes rejects to DALYREJS. 6 file dependencies. |
| Data Sensitivity | **5** | Posts financial transactions to master files. Updates account balances. Creates permanent financial records. A bug here means incorrect account balances across the system. |
| Business Impact | **5** | Central to the nightly batch cycle. POSTTRAN job must succeed or the entire downstream pipeline (interest calc, statements, reports) fails. |
| **Composite Score** | **4.70** | |

**Modernization Risks:**
- Must preserve exact decimal arithmetic (COMP-3 packed decimal → Java BigDecimal)
- Reject handling logic must be preserved -- creates DALYREJS for manual review
- Atomicity: all-or-nothing posting per transaction (map to Spring Batch with chunk-oriented processing)
- Sequential file processing order matters for balance calculations

**Recommended Approach:** Spring Batch job with `ItemReader` (daily file) → `ItemProcessor` (validation + posting logic) → `ItemWriter` (database). Use `@Transactional` for each chunk.

---

### #3: CBACT04C -- Interest Calculation Engine (Batch)

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **4** | 652 lines. Complex financial calculations involving disclosure group rates, category balances, and account updates. 5 COPY includes with multiple VSAM file cross-references. |
| Integration Risk | **4** | Accesses 5 VSAM files: ACCTDATA (read/rewrite), CARDXREF (read via AIX), DISCGRP (read), TCATBALF (read/rewrite), SYSTRAN (write). Uses alternate index path. |
| Data Sensitivity | **5** | Calculates and applies interest to customer accounts. Directly impacts statement amounts and customer billing. Financial regulatory implications. |
| Business Impact | **5** | Interest revenue is core to credit card business. Incorrect calculations = financial loss or regulatory violations. |
| **Composite Score** | **4.45** | |

**Modernization Risks:**
- Interest calculation formulas must be validated with precision testing (rounding differences between COBOL COMP-3 and Java BigDecimal)
- Disclosure group lookup logic is complex (account group → type → category → rate)
- Output to SYSTRAN must be preserved for downstream COMBTRAN processing
- Alternate index (AIX) access on CARDXREF maps to database JOIN

**Recommended Approach:** Dedicated `InterestCalculationService` with `BigDecimal` arithmetic. Extensive unit tests comparing COBOL output samples with Java results. Spring Batch job for bulk processing.

---

### #4: COCRDUPC -- Credit Card Update (Online)

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **4** | 1,560 lines. Complex validation for card fields (CVV, expiration, embossed name). 15 copybook dependencies. Multiple screen states. |
| Integration Risk | **4** | 12 EXEC CICS commands. Reads/rewrites CARDDATA VSAM. Cross-references ACCTDATA, CUSTDATA, CARDXREF. 15 copybooks. |
| Data Sensitivity | **5** | Handles PCI-sensitive card data: card numbers, CVV codes, expiration dates. Card activation/deactivation. |
| Business Impact | **4** | Card management is essential for customer operations. Card status changes affect transaction authorization. |
| **Composite Score** | **4.20** | |

**Modernization Risks:**
- PCI DSS compliance requirements for card data handling in Java
- Card number and CVV must be encrypted at rest and in transit
- Validation logic for card fields must be exact (Luhn check, expiration format)
- Card status changes may need event-driven notifications in modern architecture

**Recommended Approach:** `CreditCardController` + `CreditCardService` with PCI-compliant field handling. Use `@JsonIgnore` on sensitive fields, field-level encryption, and tokenization.

---

### #5: COCRDLIC -- Credit Card List (Online)

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **4** | 1,459 lines. Complex browse/pagination logic with STARTBR/READNEXT. Multi-page list with forward/backward navigation. 13 copybooks. |
| Integration Risk | **4** | 18 EXEC CICS commands (4 READ, 3 XCTL, 2 STARTBR, 1 SEND MAP, 1 RECEIVE MAP). Navigates to COCRDSLC for detail view. Accesses CARDDATA + CARDXREF. |
| Data Sensitivity | **4** | Displays card numbers (potentially masked). List view of all cards for an account. |
| Business Impact | **4** | Primary card browsing interface. Entry point to card detail and update functions. |
| **Composite Score** | **4.00** | |

**Modernization Risks:**
- CICS browse (STARTBR/READNEXT/ENDBR) pagination maps to database OFFSET/LIMIT or keyset pagination
- Pseudo-conversational state between pages (card position) needs session or URL state
- XCTL to detail screen maps to REST API call or route navigation

**Recommended Approach:** REST endpoint with paginated response. Spring Data JPA `Pageable` for database pagination. Frontend table component with server-side paging.

---

### #6: CBSTM03A/B -- Statement Generation (Batch)

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **4** | Combined 1,154 lines (924 + 230). CBSTM03A uses ALTER/GO TO, 2D arrays, mainframe control block addressing, COMP/COMP-3 variables. Calls subroutine CBSTM03B 10+ times. Dual output (text + HTML). |
| Integration Risk | **4** | CBSTM03A calls CBSTM03B for all file I/O. CBSTM03B manages 4 VSAM files (TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE). SORT step in JCL pre-processes transactions. |
| Data Sensitivity | **4** | Generates customer-facing statements with account balances, transaction details, and personal information. |
| Business Impact | **4** | Statements are a regulatory requirement. Customers rely on them for billing disputes and record-keeping. |
| **Composite Score** | **4.00** | |

**Modernization Risks:**
- ALTER/GO TO statements make control flow difficult to follow -- need careful flowchart analysis
- 2D array for card/transaction grouping maps to Java collections with groupBy
- Dual output (text + HTML) maps to template engine (Thymeleaf/FreeMarker)
- SORT step in JCL must be replicated (ORDER BY in SQL or Java Comparator)

**Recommended Approach:** Spring Batch job with `JasperReports` or `Thymeleaf` for statement rendering. `StatementService` with separate `TextStatementWriter` and `HtmlStatementWriter` implementations.

---

### #7: COTRN02C -- Transaction Add (Online)

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **3** | 783 lines. Input validation, cross-reference lookup, transaction ID generation. Calls CSUTLDTC for date validation. |
| Integration Risk | **4** | 11 EXEC CICS commands (3 READ, 1 WRITE, 1 STARTBR). Accesses TRANSACT (write), ACCTDATA (read), CARDXREF (read). Calls CSUTLDTC subroutine. |
| Data Sensitivity | **5** | Creates new financial transaction records. Directly affects account balances when posted. |
| Business Impact | **4** | Transaction entry is a core business function. Used for manual transaction adjustments. |
| **Composite Score** | **3.90** | |

**Modernization Risks:**
- Transaction ID generation (sequential within VSAM) must be replaced with database sequence or UUID
- Date validation via CSUTLDTC/CEEDAYS must be replaced with `java.time` validation
- WRITE to TRANSACT with duplicate-key check maps to INSERT with unique constraint

**Recommended Approach:** `TransactionController` with `@PostMapping`. Use database-generated IDs. Bean Validation for input. `@Transactional` for atomicity.

---

### #8: COBIL00C -- Bill Payment (Online)

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **3** | 572 lines. Bill payment logic with full-balance and partial payment options. |
| Integration Risk | **3** | 13 EXEC CICS commands (3 READ, 1 WRITE, 1 REWRITE, 1 STARTBR). Accesses ACCTDATA, CARDXREF, TRANSACT. |
| Data Sensitivity | **5** | Processes financial payments. Reduces account balances. Creates payment transaction records. |
| Business Impact | **4** | Direct revenue impact -- bill payments reduce outstanding balances. Customer-facing payment function. |
| **Composite Score** | **3.65** | |

**Modernization Risks:**
- Payment amount validation (cannot exceed balance, minimum payment rules)
- Atomicity: balance update + transaction creation must be in same transaction
- Payment confirmation should generate receipt/acknowledgment in modern system

**Recommended Approach:** `BillPaymentService` with `@Transactional`. Idempotency keys for payment deduplication. Event publishing for payment confirmation notifications.

---

### #9: COSGN00C -- Signon / Authentication (Online)

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **2** | 260 lines. Relatively simple logic: read user record, compare password, route to menu. |
| Integration Risk | **3** | 10 EXEC CICS commands (1 READ, 2 XCTL, 2 RETURN). Reads USRSEC VSAM. Routes to COMEN01C or COADM01C based on user type. |
| Data Sensitivity | **5** | Authentication gateway for the entire application. Stores passwords in plaintext. Determines admin vs. user access. |
| Business Impact | **5** | Single point of entry. If broken, entire application is inaccessible. Security vulnerability with plaintext passwords. |
| **Composite Score** | **3.60** | |

**Modernization Risks:**
- Plaintext password storage must be replaced with BCrypt hashing
- CICS pseudo-sign-on must map to Spring Security authentication
- User type routing (admin/regular) maps to role-based access control
- Session management moves from CICS COMMAREA to HTTP session/JWT

**Recommended Approach:** Spring Security with `UserDetailsService`, BCrypt `PasswordEncoder`, role-based `@PreAuthorize`. JWT tokens for API access. This should be one of the **first modules migrated** as it gates all other functionality.

---

### #10: CBTRN03C -- Transaction Detail Report (Batch)

| Dimension | Score | Rationale |
|---|---|---|
| Code Complexity | **4** | 649 lines. Complex report formatting with headers, detail lines, page/account/grand totals. Date range filtering via DATEPARM file. |
| Integration Risk | **3** | Reads 5 files: TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM. Writes to TRANREPT. Pre-processing SORT step in JCL. |
| Data Sensitivity | **3** | Read-only report. Contains transaction amounts and account IDs but no PII mutation. |
| Business Impact | **4** | Daily transaction report used for reconciliation and audit. Required for operational oversight. |
| **Composite Score** | **3.55** | |

**Modernization Risks:**
- Report formatting (column alignment, page breaks, subtotals) maps to reporting framework
- SORT pre-processing in JCL must be handled in SQL ORDER BY
- Date range parameter file maps to API query parameters or job parameters

**Recommended Approach:** Spring Batch with `JasperReports` or SQL-based report generation. REST API endpoint for on-demand reports with date range parameters.

---

## Summary Ranking

| Rank | Module | Type | Lines | Composite Score | Key Risk |
|---|---|---|---|---|---|
| 1 | **COACTUPC** | Online | 4,236 | 5.00 | Largest program, financial mutations |
| 2 | **CBTRN02C** | Batch | 731 | 4.70 | Central pipeline, 6-file dependencies |
| 3 | **CBACT04C** | Batch | 652 | 4.45 | Interest calculations, regulatory |
| 4 | **COCRDUPC** | Online | 1,560 | 4.20 | PCI card data, complex validation |
| 5 | **COCRDLIC** | Online | 1,459 | 4.00 | Browse pagination, navigation hub |
| 6 | **CBSTM03A/B** | Batch | 1,154 | 4.00 | ALTER/GO TO, dual output format |
| 7 | **COTRN02C** | Online | 783 | 3.90 | Transaction creation, ID generation |
| 8 | **COBIL00C** | Online | 572 | 3.65 | Payment processing, atomicity |
| 9 | **COSGN00C** | Online | 260 | 3.60 | Security gateway, plaintext passwords |
| 10 | **CBTRN03C** | Batch | 649 | 3.55 | Complex reporting, multi-file joins |

---

## Recommended Migration Order

Based on the hotspot analysis, the recommended migration sequence is:

### Phase 1: Foundation (Weeks 1-4)
1. **COSGN00C** (Signon) -- Security foundation must come first
2. **COMEN01C / COADM01C** (Menus) -- Navigation framework
3. **Database schema** from copybook layouts (DATA_DICTIONARY.md)

### Phase 2: Core Read Operations (Weeks 5-8)
4. **COACTVWC** (Account View) -- Read-only, lower risk
5. **COCRDLIC / COCRDSLC** (Card List/View) -- Pagination patterns
6. **COTRN00C / COTRN01C** (Transaction List/View) -- Read patterns

### Phase 3: Write Operations (Weeks 9-14)
7. **COACTUPC** (Account Update) -- Highest complexity, needs most testing
8. **COCRDUPC** (Card Update) -- PCI compliance critical
9. **COTRN02C** (Transaction Add) -- Write + validation
10. **COBIL00C** (Bill Payment) -- Financial payment logic
11. **COUSR00C-03C** (User CRUD) -- Admin functions

### Phase 4: Batch Processing (Weeks 15-20)
12. **CBTRN02C** (Transaction Posting) -- Central batch
13. **CBACT04C** (Interest Calculation) -- Financial precision
14. **CBSTM03A/B** (Statement Generation) -- Report output
15. **CBTRN03C** (Transaction Report) -- Reporting
16. **Remaining batch programs** (CBACT01C-03C, CBCUS01C, CBEXPORT/CBIMPORT)

### Phase 5: Optional Modules (Weeks 21+)
17. Authorization module (IMS/DB2/MQ)
18. Transaction Type DB2 module
19. VSAM-MQ module
