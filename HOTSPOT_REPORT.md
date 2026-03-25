# CardDemo Hotspot Report — Top 10 Modernization Priorities

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Methodology:** Weighted scoring across code complexity, integration risk, business impact, and data sensitivity

---

## Scoring Methodology

Each module is scored on four dimensions (1–5 scale), then weighted to produce a composite priority score:

| Dimension | Weight | Description |
|-----------|--------|-------------|
| **Code Complexity** | 30% | Lines of code, cyclomatic branching (EVALUATE/IF nesting), use of ALTER/GO TO, COMP variables, multi-format I/O, subroutine calls |
| **Integration Risk** | 25% | Number of VSAM files accessed, cross-program dependencies (XCTL/CALL), CICS API surface, external system interfaces (MQ, DB2, IMS) |
| **Business Impact** | 30% | Criticality to daily operations, financial calculation involvement, customer-facing functionality, regulatory exposure |
| **Data Sensitivity** | 15% | PII fields, financial data, authentication credentials, audit trail data |

**Composite Score = (Complexity × 0.30) + (Risk × 0.25) + (Impact × 0.30) + (Sensitivity × 0.15)**

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC — Account Update

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **5** | 4,236 lines — largest program in the system. Heavy EVALUATE/IF nesting for field-level validation. Accesses 5 VSAM files. Complex screen interaction with multi-step update workflow. Uses CSSETATY for dynamic attribute manipulation. |
| Integration Risk | **5** | Reads/writes ACCTDATA and CUSTDATA. Reads CARDDATA, CARDXREF. XCTL integration with COACTVWC. Uses COCOM01Y COMMAREA extensively. 12+ copybook dependencies. |
| Business Impact | **5** | Core account modification — credit limits, balances, customer data. Any defect directly impacts financial accuracy. |
| Data Sensitivity | **5** | Modifies account balances, credit limits, customer PII (name, address, SSN via linked customer record). |
| **Composite** | **5.00** | |

**Modernization Concerns:**
- Largest single program — candidate for decomposition into separate services (account update, customer update, validation)
- Field-level validation logic should map to Java Bean Validation annotations
- Multi-step update with CICS pseudo-conversational flow needs careful state management in REST/web equivalent
- REWRITE operations on ACCTDATA and CUSTDATA need transactional consistency (database transaction in Java)

**Recommended Approach:** Decompose into 3+ Java services. Extract validation into reusable validators. Map to Spring MVC controller + JPA entities with `@Transactional`.

---

### Rank 2: CBTRN02C — Transaction Posting (Batch)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 731 lines. Reads daily transactions, validates against type/category, resolves card→account via cross-reference, updates master file and category balances. Multi-file coordination. |
| Integration Risk | **5** | Reads 4 VSAM files (DALYTRAN, CARDXREF, TRANTYPE, TRANCATG). Writes 3 files (TRANSACT, TCATBAL, DALYREJS). Central to the daily batch cycle — all downstream jobs depend on its output. |
| Business Impact | **5** | Core financial posting engine. Incorrect posting means wrong balances, wrong statements, wrong interest calculations. Single point of failure for the entire batch cycle. |
| Data Sensitivity | **4** | Processes all transaction amounts. Creates the audit trail (TRANSACT master). Generates rejection records. |
| **Composite** | **4.60** | |

**Modernization Concerns:**
- Critical path in batch cycle — must maintain exact posting semantics
- Multi-file update atomicity (no native transaction support in VSAM — relies on job restart/recovery)
- Category balance accumulation logic is the foundation for interest calculation
- Reject handling must be preserved for reconciliation

**Recommended Approach:** Map to Spring Batch job with chunk-oriented processing. Use database transactions for atomicity. Implement ItemReader/ItemProcessor/ItemWriter pattern.

---

### Rank 3: CBACT04C — Interest Calculation (Batch)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 652 lines. Reads category balances and disclosure group rates. Performs interest computation per account per category. Writes interest transactions to master. COMP/COMP-3 arithmetic for precision. |
| Integration Risk | **4** | Reads 4 VSAM files (TCATBAL, DISCGRP, ACCTDATA, CARDXREF). Writes to TRANSACT. Depends on CBTRN02C output (TCATBAL). |
| Business Impact | **5** | Direct financial impact — calculates interest charges applied to customer accounts. Regulatory compliance requirement (Truth in Lending). Errors mean incorrect billing. |
| Data Sensitivity | **5** | Financial calculations affecting customer balances. Interest rate application. Must maintain decimal precision (COMP-3). |
| **Composite** | **4.55** | |

**Modernization Concerns:**
- COMP-3 (packed decimal) arithmetic requires Java `BigDecimal` for exact precision
- Interest rate lookup logic (group → type → category) must be preserved exactly
- Regulatory requirement: interest calculation methodology must be auditable
- Date-based calculation (parameter-driven) needs careful calendar handling

**Recommended Approach:** Implement as Spring Batch job. Use `BigDecimal` throughout. Create dedicated `InterestCalculationService` with comprehensive unit tests validating against known COBOL outputs. Consider keeping a parallel-run phase during migration.

---

### Rank 4: COCRDUPC — Card Update

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **5** | 1,560 lines. Second-largest online program. Complex validation for card number, expiration, CVV, status changes. Dynamic screen attribute manipulation (CSSETATY). XCTL flow from card list/view. |
| Integration Risk | **4** | Reads/writes CARDDATA. Reads CARDXREF. XCTL integration with COCRDLIC and COCRDSLC. Multiple copybook dependencies including CSSETATY, CSSTRPFY. |
| Business Impact | **4** | Card data modifications — activation, deactivation, expiration updates. Affects whether cards can process transactions. |
| Data Sensitivity | **5** | Card numbers, CVV codes, expiration dates — PCI-DSS scope. Card activation status directly affects transaction authorization. |
| **Composite** | **4.45** | |

**Modernization Concerns:**
- PCI-DSS compliance: card numbers and CVV must be encrypted at rest and in transit
- Card status changes have immediate impact on transaction authorization
- Screen attribute manipulation (BRT/NORM/DRK for field highlighting) maps to CSS/frontend validation
- Consider tokenization strategy for card numbers in modernized system

**Recommended Approach:** Implement with PCI-DSS controls from day one. Use field-level encryption. Map to secure REST endpoint with input validation. Frontend validation mirrors BMS attribute logic.

---

### Rank 5: COCRDLIC — Card List (Browse)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 1,459 lines. Complex STARTBR/READNEXT/READPREV browse logic with pagination. Forward and backward scrolling. Selection for view or update. CSSTRPFY string formatting. |
| Integration Risk | **4** | Browses CARDDATA VSAM. XCTL to COCRDSLC (view) and COCRDUPC (update). Complex navigation state management via COMMAREA. |
| Business Impact | **3** | Card inquiry — important for customer service but read-only (no data modification risk). |
| Data Sensitivity | **4** | Displays card numbers in list format — PCI scope. Must mask card numbers in modernized display. |
| **Composite** | **3.75** | |

**Modernization Concerns:**
- VSAM browse with STARTBR/READNEXT/READPREV maps to paginated SQL queries
- Forward/backward scrolling is non-trivial — VSAM allows bidirectional browse natively
- Card number masking required in modernized UI (show only last 4 digits)
- Selection mechanism (line selection → XCTL) maps to list-detail navigation pattern

**Recommended Approach:** Implement as paginated REST endpoint with Spring Data JPA. Use `Pageable` for forward/backward navigation. Mask card numbers in DTO layer.

---

### Rank 6: CBSTM03A — Statement Generation (Batch)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **5** | 924 lines. Most complex batch program architecturally. Uses ALTER and GO TO (deliberately — for modernization exercise). COMP/COMP-3 variables. 2D arrays. Calls subroutine CBSTM03B 12 times. Generates both text and HTML output simultaneously. Mainframe control block addressing. |
| Integration Risk | **4** | Calls CBSTM03B (file I/O subroutine). Reads 4 VSAM files via subroutine (TRXFL, XREFFILE, CUSTFILE, ACCTFILE). Writes 2 output files (STMTFILE, HTMLFILE). |
| Business Impact | **4** | Customer-facing output — account statements. Directly visible to cardholders. Errors damage customer trust. |
| Data Sensitivity | **4** | Contains full customer PII (name, address), account balances, and transaction history. |
| **Composite** | **4.30** | |

**Modernization Concerns:**
- **ALTER and GO TO** — deliberately included to test modernization tooling. These create dynamic control flow that is extremely difficult to translate to structured Java
- Subroutine CBSTM03B handles all file I/O — tightly coupled via shared working storage area
- Dual output (text + HTML) should become a template-based generation in Java (e.g., Thymeleaf, FreeMarker)
- 2D array usage for statement line accumulation needs careful mapping

**Recommended Approach:** Refactor ALTER/GO TO into structured control flow first. Implement as Spring Batch job with dedicated statement template engine. Separate the file I/O (CBSTM03B) into a repository layer.

---

### Rank 7: COTRN02C — Add Transaction (Online)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 783 lines. Input validation with CSUTLDTC date calls. Reads account/xref for validation. Generates transaction ID. Writes to both TRANSACT and DALYTRAN. STARTBR/READPREV for ID generation. |
| Integration Risk | **4** | Reads ACCTDATA, CARDXREF. Writes TRANSACT and DALYTRAN. Calls CSUTLDTC twice (date validation). Complex COMMAREA interaction. |
| Business Impact | **4** | Creates new financial transactions. Direct financial impact — incorrect transactions affect balances. |
| Data Sensitivity | **4** | Transaction amounts, card numbers, merchant data. Audit trail creation. |
| **Composite** | **4.00** | |

**Modernization Concerns:**
- Transaction ID generation via STARTBR/READPREV (gets last ID, increments) needs database sequence in Java
- Dual write to TRANSACT and DALYTRAN must be atomic
- Date validation via CSUTLDTC → CEEDAYS needs Java `LocalDate` equivalent
- Input validation logic for amount, date, type should map to Bean Validation

**Recommended Approach:** REST POST endpoint with `@Valid` request body. Database sequence for ID generation. `@Transactional` for dual insert. Java 8+ date/time API.

---

### Rank 8: COTRN00C — Transaction List (Browse)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **3** | 699 lines. STARTBR/READNEXT/READPREV browse with pagination. Similar pattern to COCRDLIC but simpler. |
| Integration Risk | **3** | Browses TRANSACT VSAM. XCTL to COTRN01C (view) and COTRN02C (add). Standard COMMAREA flow. |
| Business Impact | **4** | Transaction inquiry — critical for customer service, dispute resolution, and audit. High-volume access pattern. |
| Data Sensitivity | **3** | Displays transaction details including amounts and merchant info. |
| **Composite** | **3.30** | |

**Modernization Concerns:**
- High-volume browse — performance critical in modernized system
- Alternate index browse (by account) needs efficient SQL query with proper indexing
- Consider implementing search/filter capabilities not present in original BMS screen

**Recommended Approach:** Paginated REST endpoint with search filters. Database indexing strategy critical. Consider read replica for query load.

---

### Rank 9: CBTRN03C — Transaction Report (Batch)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 649 lines. Multi-file read with lookup joins (transaction + type + category). Report formatting with headers, detail lines, page/account/grand totals. Control break logic. |
| Integration Risk | **3** | Reads 5 files (TRANSACT sorted, CARDXREF, TRANTYPE, TRANCATG, date parms). Writes report output. Dependent on COMBTRAN for sorted input. |
| Business Impact | **4** | Daily operational report — used for reconciliation and management review. Regulatory reporting potential. |
| Data Sensitivity | **3** | Aggregated transaction data. Account-level totals. |
| **Composite** | **3.55** | |

**Modernization Concerns:**
- Control break logic (page totals, account totals, grand totals) maps to SQL GROUP BY with ROLLUP
- Report formatting should use a reporting framework (JasperReports, etc.) rather than line-by-line construction
- Date parameter file input needs configuration management equivalent
- SORT pre-processing step can be replaced by SQL ORDER BY

**Recommended Approach:** Replace with SQL-based reporting. Use JasperReports or similar for formatted output. Spring Batch for orchestration. Database views for pre-joined data.

---

### Rank 10: CORPT00C — Report Request (Online)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **3** | 649 lines. Date range input with CSUTLDTC validation. WRITEQ TD to trigger batch report generation. Moderate EVALUATE/IF logic for input validation. |
| Integration Risk | **4** | Calls CSUTLDTC for date validation. WRITEQ TD creates transient data queue entry that triggers batch job. Bridge between online and batch subsystems. |
| Business Impact | **3** | Report generation trigger — important for operations but not financially critical. |
| Data Sensitivity | **2** | Only handles date range parameters — no sensitive data directly. |
| **Composite** | **3.05** | |

**Modernization Concerns:**
- WRITEQ TD (transient data queue) is CICS-specific mechanism for online-to-batch communication
- In Java, this maps to a message queue (e.g., SQS, RabbitMQ) or async job trigger (Spring Batch launcher)
- Date validation logic should be shared with COTRN02C (both call CSUTLDTC)
- Consider making reports available as on-demand downloads rather than batch-only

**Recommended Approach:** REST endpoint that triggers async report generation via message queue or Spring Batch job launcher. Return job ID for status polling. Serve completed reports as downloadable files.

---

## Priority Summary Matrix

| Rank | Program | Composite | Complexity | Risk | Impact | Sensitivity | LOC | Domain |
|------|---------|-----------|------------|------|--------|-------------|-----|--------|
| 1 | **COACTUPC** | **5.00** | 5 | 5 | 5 | 5 | 4,236 | Account Update |
| 2 | **CBTRN02C** | **4.60** | 4 | 5 | 5 | 4 | 731 | Transaction Posting |
| 3 | **CBACT04C** | **4.55** | 4 | 4 | 5 | 5 | 652 | Interest Calculation |
| 4 | **COCRDUPC** | **4.45** | 5 | 4 | 4 | 5 | 1,560 | Card Update |
| 5 | **COCRDLIC** | **3.75** | 4 | 4 | 3 | 4 | 1,459 | Card List |
| 6 | **CBSTM03A** | **4.30** | 5 | 4 | 4 | 4 | 924 | Statement Generation |
| 7 | **COTRN02C** | **4.00** | 4 | 4 | 4 | 4 | 783 | Add Transaction |
| 8 | **COTRN00C** | **3.30** | 3 | 3 | 4 | 3 | 699 | Transaction List |
| 9 | **CBTRN03C** | **3.55** | 4 | 3 | 4 | 3 | 649 | Transaction Report |
| 10 | **CORPT00C** | **3.05** | 3 | 4 | 3 | 2 | 649 | Report Request |

---

## Risk Heat Map

```
                        Business Impact
                   Low    Medium    High    Critical
              ┌─────────┬─────────┬─────────┬─────────┐
   Critical   │         │         │         │COACTUPC │
              │         │         │         │CBTRN02C │
              │         │         │         │CBACT04C │
              ├─────────┼─────────┼─────────┼─────────┤
   High       │         │         │COCRDUPC │         │
C             │         │         │CBSTM03A │         │
o             │         │         │COTRN02C │         │
m             ├─────────┼─────────┼─────────┼─────────┤
p   Medium    │         │CORPT00C │COTRN00C │         │
l             │         │         │CBTRN03C │         │
e             │         │         │COCRDLIC │         │
x             ├─────────┼─────────┼─────────┼─────────┤
i   Low       │         │         │         │         │
t             │         │         │         │         │
y             └─────────┴─────────┴─────────┴─────────┘
```

---

## Modernization Wave Recommendations

### Wave 1 — Foundation (Highest Risk, Highest Impact)
| Program | Rationale |
|---------|-----------|
| CBTRN02C | Foundation of batch cycle — all other batch jobs depend on correct posting |
| CBACT04C | Financial calculation — requires parallel-run validation before cutover |
| COACTUPC | Largest, most complex online program — sets patterns for all other screens |

**Duration Estimate:** 8–12 weeks
**Key Risk:** Financial accuracy — requires extensive parallel testing against COBOL outputs

### Wave 2 — Card Management (PCI Scope)
| Program | Rationale |
|---------|-----------|
| COCRDUPC | PCI-DSS scope — card data handling needs security-first implementation |
| COCRDLIC | Card browse — high-volume, must perform well |
| COCRDSLC | Card view — read-only but PCI scope |

**Duration Estimate:** 4–6 weeks
**Key Risk:** PCI-DSS compliance — encryption, tokenization, access controls

### Wave 3 — Transaction Management
| Program | Rationale |
|---------|-----------|
| COTRN02C (online) | Transaction creation — financial impact |
| COTRN00C | Transaction browse — high-volume inquiry |
| COTRN01C | Transaction view — simple but frequently used |

**Duration Estimate:** 4–6 weeks
**Key Risk:** Transaction integrity — dual writes must be atomic

### Wave 4 — Reporting & Statements
| Program | Rationale |
|---------|-----------|
| CBSTM03A + CBSTM03B | Statement generation — complex but well-contained |
| CBTRN03C | Transaction report — control break logic |
| CORPT00C | Report trigger — online/batch bridge |

**Duration Estimate:** 4–6 weeks
**Key Risk:** ALTER/GO TO in CBSTM03A requires careful refactoring before translation

### Wave 5 — Remaining Programs
All remaining programs (sign-on, menus, user admin, data utilities, export/import) are lower complexity and can follow patterns established in Waves 1–4.

**Duration Estimate:** 4–6 weeks

---

## Technical Debt & Anti-Patterns Identified

| Finding | Location | Severity | Modernization Impact |
|---------|----------|----------|---------------------|
| **ALTER and GO TO** | CBSTM03A | Critical | Creates dynamic control flow — must be refactored to structured code before translation |
| **Plain-text passwords** | CSUSR01Y / USRSEC VSAM | Critical | Must implement bcrypt/scrypt hashing in Java. Cannot migrate passwords as-is |
| **No input sanitization** | All CICS programs | High | BMS provides basic length checks only. Need comprehensive validation in Java |
| **COMP-3 arithmetic** | CBACT04C, CBTRN02C | High | Must use Java BigDecimal — floating point will introduce rounding errors |
| **Hardcoded literals** | Multiple programs | Medium | File names, constants scattered in programs. Externalize to config in Java |
| **No error recovery** | Batch programs | Medium | CEE3ABD (abend) on any error. Need exception handling and retry logic |
| **Tight coupling** | CBSTM03A ↔ CBSTM03B | Medium | Shared working storage area. Refactor to well-defined interface |
| **Duplicate record layouts** | CVTRA05Y ≈ CVTRA06Y ≈ COSTM01 | Low | Three near-identical 350-byte transaction layouts. Consolidate to single entity |
| **FILLER padding** | All copybooks | Low | VSAM fixed-length records use FILLER. Database columns don't need padding |
| **Commented-out code** | COCRDLIC, COCRDSLC | Low | Commented COPY statements suggest abandoned features. Clean up during migration |
