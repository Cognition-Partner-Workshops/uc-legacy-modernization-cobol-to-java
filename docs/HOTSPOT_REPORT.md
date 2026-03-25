# HOTSPOT REPORT -- Top 10 Modules by Complexity, Risk & Business Impact

> **Generated:** 2026-03-25  
> **Methodology:** Each module scored on three axes (1-10 each, max composite = 30):  
> - **Complexity** -- LOC, cyclomatic density, copybook count, file I/O breadth, nesting depth  
> - **Risk** -- Data sensitivity (PII/PCI), write operations, multi-file updates, error-handling gaps  
> - **Business Impact** -- Revenue criticality, user-facing surface, downstream dependencies, batch-cycle position

---

## Executive Summary

| Rank | Module | LOC | Complexity | Risk | Impact | **Score** | Primary Concern |
|------|--------|-----|------------|------|--------|-----------|-----------------|
| 1 | **COACTUPC** | 4,237 | 10 | 9 | 9 | **28** | Largest program; multi-file VSAM updates with complex field validation |
| 2 | **CBTRN02C** | 731 | 8 | 10 | 10 | **28** | Core transaction posting; financial writes to 4 VSAM files |
| 3 | **CBACT04C** | 652 | 8 | 9 | 9 | **26** | Interest calculation; reads discount rules, writes financial transactions |
| 4 | **COCRDUPC** | 1,560 | 9 | 8 | 8 | **25** | Card update with PCI-scoped data; multi-field validation |
| 5 | **CBSTM03A** | 924 | 8 | 7 | 9 | **24** | Statement generation driver; 13 CALL dispatches to CBSTM03B |
| 6 | **COCRDLIC** | 1,460 | 8 | 6 | 8 | **22** | Card list with pagination; AIX browsing complexity |
| 7 | **COTRTLIC** | 2,098 | 9 | 7 | 6 | **22** | DB2 transaction type list; embedded SQL cursors |
| 8 | **COTRN02C** | 783 | 7 | 8 | 7 | **22** | Online transaction add; writes to TRANSACT, reads 3 files |
| 9 | **COPAUA0C** | 1,026 | 8 | 8 | 6 | **22** | MQ-triggered auth decision; IMS + MQ + CICS integration |
| 10 | **CBTRN03C** | 649 | 7 | 6 | 8 | **21** | Transaction report; reads 6 files, complex formatting |

---

## Detailed Module Assessments

### 1. COACTUPC -- Account Update (Score: 28/30)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **10** | 4,237 lines -- largest program in the codebase. Deep EVALUATE/IF nesting for field-by-field validation of all account attributes. 11 copybooks included. Multiple VSAM READ-FOR-UPDATE + REWRITE patterns. |
| Risk | **9** | Updates ACCTDAT (financial balances, credit limits). Multi-file reads from CARDAIX, CXACAIX, CUSTDAT. Any bug in validation or update logic directly corrupts account data. Monetary fields (ACCT-CURR-BAL, ACCT-CREDIT-LIMIT) are S9(10)V99 signed decimals prone to truncation. |
| Impact | **9** | Core user-facing function for account management. Admin and regular users both access via menu option 2. Downstream: account balance changes affect interest calculation (CBACT04C) and statement generation (CBSTM03A). |

**Modernization Recommendations:**
- Decompose into smaller service methods (AccountValidator, AccountUpdater, AccountReader)
- Extract validation rules into a rules engine or bean validation annotations
- Replace VSAM READ-FOR-UPDATE with JPA optimistic locking (`@Version`)
- Add comprehensive unit tests for each validation branch

---

### 2. CBTRN02C -- Transaction Posting (Score: 28/30)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **8** | 731 lines but operates on 6 different files simultaneously: DALYTRAN (input), TRANSACT (output), XREFFILE (lookup), ACCTFILE (update), TCATBALF (update), DALYREJS (error output). Complex matching logic to validate transactions against cross-reference and account data. |
| Risk | **10** | This is the **financial posting engine**. Writes debits/credits to account balances (ACCTFILE) and category balances (TCATBALF). Creates the permanent transaction record in TRANSACT. Invalid postings = financial data corruption. Rejected transactions written to DALYREJS -- must not lose data. |
| Impact | **10** | Anchors the nightly batch cycle (Step 3 in POSTTRAN.jcl). Every downstream job depends on correct posting: INTCALC, COMBTRAN, CREASTMT, TRANREPT. Failure halts the entire batch pipeline. |

**Modernization Recommendations:**
- Convert to Spring Batch `ItemProcessor` with chunk-oriented processing
- Implement two-phase commit or saga pattern for multi-file updates
- Add dead-letter queue for rejected transactions (replace DALYREJS)
- Implement idempotency keys to enable safe batch restarts

---

### 3. CBACT04C -- Interest Calculation (Score: 26/30)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **8** | 652 lines with nested loops: iterates through TCATBALF category balances, looks up discount rates from DISCGRP, applies rate calculations per account. Uses XREFFILE for card-to-account resolution. Date-driven logic with PARM input for processing date. |
| Risk | **9** | Generates interest charges that become real financial transactions in TRANSACT. Incorrect rate lookup from DISCGRP or rounding errors in S9(10)V99 arithmetic directly affect customer bills. Updates ACCTFILE balances. |
| Impact | **9** | Runs nightly as Step 4 (INTCALC.jcl). Interest charges flow into statements (CREASTMT) and reports (TRANREPT). Regulatory compliance: interest must be calculated accurately per contractual terms. |

**Modernization Recommendations:**
- Extract interest rate rules into a configuration-driven engine
- Use `BigDecimal` with explicit `RoundingMode.HALF_UP` to match COBOL behavior
- Implement comprehensive audit trail for rate applications
- Add reconciliation step comparing calculated vs. expected totals

---

### 4. COCRDUPC -- Credit Card Update (Score: 25/30)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **9** | 1,560 lines. Similar structure to COACTUPC but for card data. Extensive field-level validation, CSSTRPFY string manipulation, CSSETATY attribute setting. 13 copybooks. CARDDAT READ-FOR-UPDATE + REWRITE, CARDAIX alternate index access. |
| Risk | **8** | Updates PCI-scoped data: CARD-NUM (16-digit), CARD-CVV-CD (3-digit), CARD-EXPIRAION-DATE. Card number changes require cross-reference integrity. CARD-EMBOSSED-NAME changes could affect physical card production. |
| Impact | **8** | Critical card management function. Changes here affect all downstream programs that read CARDDAT. PCI-DSS compliance requirements make this a regulatory hotspot. |

**Modernization Recommendations:**
- Implement PCI-DSS tokenization for card numbers (never store raw PAN)
- Separate card-number changes into an audited workflow with approval
- Extract validation into `CardValidator` service with @Valid annotations
- Add field-level audit logging for all card data changes

---

### 5. CBSTM03A -- Statement Generation Driver (Score: 24/30)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **8** | 924 lines. Driver program that CALLs CBSTM03B 13 times for different file operations (open, read, close for TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE). Produces two output formats: text (STMTFILE) and HTML (HTMLFILE). Complex formatting logic for statement line items. |
| Risk | **7** | Read-only access to master files (no write risk to source data). However, incorrect statement generation could lead to customer disputes and compliance issues. HTML output has potential for rendering bugs. |
| Impact | **9** | Customer-facing deliverable. Statements are the primary communication to cardholders about their balance and transactions. Runs as Step 7 in CREASTMT.JCL after all financial processing is complete. Depends on correct output from Steps 3-6. |

**Modernization Recommendations:**
- Replace CBSTM03A/B coupling with a single Spring Batch job reading JPA entities
- Use a templating engine (Thymeleaf/FreeMarker) for HTML statement generation
- Add PDF output using a library (iText/Apache PDFBox) to replace TXT2PDF1.JCL
- Implement parallel statement generation for performance

---

### 6. COCRDLIC -- Credit Card List (Score: 22/30)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **8** | 1,460 lines. Manual pagination logic: 7 rows per screen, forward/backward browse via STARTBR/READNEXT/READPREV on CARDAIX alternate index. Complex cursor positioning to maintain scroll state across CICS pseudo-conversational returns. |
| Risk | **6** | Read-only operations. Risk is primarily in data leakage: full card numbers displayed on screen. Incorrect pagination could skip or duplicate records. |
| Impact | **8** | Primary card lookup screen for both regular and admin users. Gateway to COCRDSLC (view) and COCRDUPC (update). High user interaction frequency. |

**Modernization Recommendations:**
- Replace VSAM browse with Spring Data JPA `Pageable` queries
- Mask card numbers in list view (show only last 4 digits)
- Implement server-side pagination with page/offset parameters
- Convert BMS list layout to a responsive data table component

---

### 7. COTRTLIC -- Transaction Type List / DB2 (Score: 22/30)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **9** | 2,098 lines -- second largest program. Embedded DB2 SQL with cursors (DECLARE/OPEN/FETCH/CLOSE). Complex list/search/delete UI. Mixed CICS BMS + DB2 programming model. 11 copybooks including CSSTRPFY string utility. |
| Risk | **7** | DB2 transaction type changes affect reference data used by CBTRN03C (reporting) and CBACT04C (interest calc). DELETE operations on reference data could orphan transaction records. |
| Impact | **6** | Admin-only function in the optional DB2 module. Not part of core batch cycle. However, transaction type integrity is a prerequisite for correct reporting. |

**Modernization Recommendations:**
- Convert DB2 embedded SQL to Spring Data JPA repository with `@Query` annotations
- Replace cursor-based iteration with `Page<TransactionType>` pagination
- Add soft-delete with referential integrity checks before deletion
- Consolidate with COTRTUPC into a single CRUD controller

---

### 8. COTRN02C -- Online Transaction Add (Score: 22/30)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **7** | 783 lines. Generates new TRAN-ID by reading last transaction (STARTBR/READPREV) and incrementing. Cross-references card to account via CXACAIX and CCXREF. Date validation via CALL to CSUTLDTC. 10 copybooks. |
| Risk | **8** | Creates permanent financial transactions in TRANSACT. TRAN-ID generation via sequential read-and-increment is a concurrency risk (duplicate IDs under load). Writes to TRANSACT affect all downstream batch processing. Amount field (S9(09)V99) validation critical. |
| Impact | **7** | User-facing transaction entry. New transactions flow through the entire batch pipeline: POSTTRAN → INTCALC → COMBTRAN → CREASTMT → TRANREPT. |

**Modernization Recommendations:**
- Replace READPREV+INCREMENT ID generation with UUID or database sequence
- Add concurrent access protection (optimistic locking or unique constraint)
- Implement input sanitization for amount and merchant fields
- Add real-time balance check before transaction creation

---

### 9. COPAUA0C -- Card Authorization Decision (Score: 22/30)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **8** | 1,026 lines. Three-technology integration: CICS (transaction control) + MQ (request/reply messaging) + IMS DB (authorization data). MQOPEN/MQGET/MQPUT1/MQCLOSE patterns with error handling for each. 15 copybooks including MQ and IMS structures. |
| Risk | **8** | Real-time authorization decision affects whether a card transaction is approved or declined. MQ message loss = missed authorizations. IMS DB integrity critical. Integration point for fraud detection (COPAUS2C marks fraud to DB2). |
| Impact | **6** | Optional module -- only activated if IMS/DB2/MQ infrastructure is available. However, when active, it's on the critical path for real-time card authorization. Latency-sensitive. |

**Modernization Recommendations:**
- Replace MQ request/reply with REST API or gRPC service
- Convert IMS DB to relational database (JPA entities)
- Implement circuit breaker pattern for external service calls
- Add async processing with message acknowledgment for reliability

---

### 10. CBTRN03C -- Transaction Detail Report (Score: 21/30)

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **7** | 649 lines. Reads 6 input files: TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM, TRANREPT (output). Complex report formatting using CVTRA07Y report layout copybook with column headers, detail lines, page breaks. Date range filtering from DATEPARM. |
| Risk | **6** | Read-only from source files. Risk is in report accuracy: incorrect joins between transactions and reference data (type/category descriptions) produce misleading reports. Date filtering bugs could omit or duplicate transactions. |
| Impact | **8** | Primary operational report for transaction activity. Used by business users and auditors. Runs as Step 8 in batch cycle (TRANREPT.jcl). Regulatory: must accurately reflect all posted transactions for the period. |

**Modernization Recommendations:**
- Convert to JasperReports or Spring Batch `FlatFileItemWriter` with formatting
- Replace file-based date parameters with API request parameters
- Add summary totals and cross-foot validation
- Implement PDF/Excel export in addition to text

---

## Complexity Heatmap by Domain

```
Domain                  Programs    Total LOC   Avg LOC   Max LOC    Hotspots
──────────────────────  ──────────  ──────────  ────────  ─────────  ────────
Account Management      2           5,179       2,590     4,237      ████████████  COACTUPC
Card Management         3           3,908       1,303     1,560      █████████     COCRDUPC
Transaction (Online)    4           2,511       628       783        ██████        COTRN02C
Transaction (Batch)     3           2,032       677       731        ████████      CBTRN02C
Statement Generation    2           1,154       577       924        ███████       CBSTM03A
Interest Calculation    1           652         652       652        ████████      CBACT04C
User Administration     4           1,767       442       695        ████          (lower risk)
Report Generation       2           1,298       649       649        ██████        CBTRN03C
Authentication          1           261         261       261        ██            (lower risk)
Navigation/Menu         2           597         299       309        █             (lower risk)
Data Migration          2           1,069       535       582        ████          (moderate)
Utility/Debug           5           1,025       205       430        ██            (low risk)
Optional: Auth IMS      5           3,577       715       1,032      ███████       COPAUA0C
Optional: DB2 TranType  3           4,037       1,346     2,098      █████████     COTRTLIC
Optional: VSAM-MQ       2           1,144       572       620        ████          (moderate)
```

---

## Migration Priority Recommendation

Based on the hotspot analysis, recommended modernization wave order:

### Wave 1 -- Financial Core (Highest Risk + Impact)
1. **CBTRN02C** (transaction posting) -- batch backbone
2. **CBACT04C** (interest calculation) -- regulatory compliance
3. **COTRN02C** (transaction add) -- online entry point to batch pipeline
4. Copybooks: CVTRA05Y, CVTRA06Y, CVTRA01Y, CVTRA02Y, CVACT01Y, CVACT03Y

### Wave 2 -- Account & Card Management (Highest Complexity)
5. **COACTUPC** (account update) -- largest, most complex program
6. **COCRDUPC** (card update) -- PCI compliance critical
7. **COCRDLIC** (card list) -- high interaction frequency
8. Copybooks: CVACT02Y, CVCUS01Y, CVCRD01Y

### Wave 3 -- Reporting & Statements (Read-Heavy, Lower Risk)
9. **CBSTM03A/B** (statements) -- customer-facing output
10. **CBTRN03C** (transaction report) -- operational reporting
11. Copybooks: CVTRA07Y, COSTM01, CUSTREC

### Wave 4 -- Supporting Functions
12. Authentication (COSGN00C) + User admin (COUSR00C-03C)
13. Menu/navigation (COMEN01C, COADM01C)
14. Utilities (CSUTLDTC, CBACT01C-03C, CBCUS01C)
15. Data migration (CBEXPORT, CBIMPORT)

### Wave 5 -- Optional Modules
16. DB2 transaction type module (COTRTLIC, COTRTUPC, COBTUPDT)
17. IMS/MQ authorization module (COPAUA0C, COPAUS0C-2C, CBPAUP0C)
18. VSAM-MQ module (COACCT01, CODATE01)
