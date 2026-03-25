# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management System)
> **Methodology:** Static analysis of LOC, cyclomatic complexity proxies, coupling, data sensitivity, and business criticality

---

## Executive Summary

This report identifies the **Top 10 highest-risk modules** in the CardDemo COBOL codebase, prioritized by a weighted score combining code complexity, integration risk, data sensitivity, and business impact. These modules should be the focus of modernization planning, test coverage, and architectural decisions.

---

## Scoring Methodology

Each module is scored on four dimensions (1-10 scale):

| Dimension | Weight | Measured By |
|-----------|--------|------------|
| **Code Complexity** | 30% | Lines of code, IF/EVALUATE branches, PERFORM count, nested logic depth |
| **Integration Risk** | 25% | Number of VSAM files accessed, copybooks included, CICS commands, external calls |
| **Data Sensitivity** | 20% | PII fields touched, financial data mutations, security-critical operations |
| **Business Impact** | 25% | Revenue-affecting logic, regulatory implications, user-facing criticality |

**Composite Score** = (Complexity × 0.30) + (Integration × 0.25) + (Data Sensitivity × 0.20) + (Business Impact × 0.25)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC -- Account Update (Online CICS)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **4,236** | -- |
| IF Statements | 168 | -- |
| EVALUATE Blocks | 10 | -- |
| PERFORM Calls | 64 | -- |
| EXEC CICS Commands | 17 | -- |
| Copybooks Included | 56 | -- |
| VSAM Files Accessed | 4 (ACCTDATA, CARDXREF, CUSTDATA, CARDDATA) | -- |
| **Code Complexity** | -- | **10** |
| **Integration Risk** | -- | **9** |
| **Data Sensitivity** | -- | **9** |
| **Business Impact** | -- | **9** |
| **COMPOSITE SCORE** | -- | **9.30** |

**Why #1:** The largest program in the entire codebase at 4,236 lines. Handles credit limit changes, account status updates, and customer data modifications -- all directly impacting financial exposure and regulatory compliance. Touches 4 VSAM files and includes 56 copybooks, making it the most tightly coupled module. Contains extensive field-level validation, attribute management, and error handling for the BMS screen. Any defect here directly affects account balances and credit decisions.

**Modernization Recommendations:**
- Decompose into separate services: AccountUpdateService, CreditLimitService, AccountStatusService
- Extract validation logic into reusable validator classes
- Implement unit tests for every EVALUATE/IF branch (estimated 178 test cases)
- Add audit logging for all financial field changes

---

### Rank 2: CBTRN02C -- Transaction Posting (Batch)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **731** | -- |
| IF Statements | 93 | -- |
| EVALUATE Blocks | 0 | -- |
| PERFORM Calls | 61 | -- |
| VSAM Files Accessed | 6 (TRANSACT, DALYTRAN, CARDXREF, ACCTDATA, TCATBALF, DALYREJS) | -- |
| **Code Complexity** | -- | **8** |
| **Integration Risk** | -- | **10** |
| **Data Sensitivity** | -- | **10** |
| **Business Impact** | -- | **10** |
| **COMPOSITE SCORE** | -- | **9.50** |

**Why #2 (highest integration + business score):** The core financial engine of the application. Posts daily transactions to the master file, updates account balances, updates category balances, and writes rejected transactions. Touches 6 VSAM files with read-write operations -- the highest data coupling in the system. A bug here causes financial discrepancies across all accounts. The 93 IF statements reflect extensive business rule validation (card validity, account status, credit limit checks, transaction categorization).

**Modernization Recommendations:**
- Convert to Spring Batch job with chunk-oriented processing
- Implement idempotent posting with transaction IDs to prevent double-posting
- Add comprehensive reconciliation step (input count vs. posted + rejected)
- Build parallel-safe design to handle high transaction volumes

---

### Rank 3: CBACT04C -- Interest Calculation (Batch)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **652** | -- |
| IF Statements | 86 | -- |
| EVALUATE Blocks | 0 | -- |
| PERFORM Calls | 56 | -- |
| Copybooks Included | 5 | -- |
| VSAM Files Accessed | 5 (TCATBALF, CARDXREF+AIX, ACCTDATA, DISCGRP, SYSTRAN) | -- |
| **Code Complexity** | -- | **8** |
| **Integration Risk** | -- | **9** |
| **Data Sensitivity** | -- | **10** |
| **Business Impact** | -- | **10** |
| **COMPOSITE SCORE** | -- | **9.25** |

**Why #3:** Calculates interest charges on every account -- directly affecting customer billing and revenue. Uses 86 conditional branches to handle different rate tiers, account groups, transaction categories, and edge cases (zero balance, over-limit, etc.). Reads disclosure groups to determine applicable rates and generates system transactions for calculated interest. Any calculation error has regulatory (Truth-in-Lending) and financial reporting implications.

**Modernization Recommendations:**
- Implement as a dedicated FinancialCalculationService with BigDecimal arithmetic
- Parameterize rate tables (currently hard-linked to DISCGRP VSAM)
- Add comprehensive audit trail with calculation breakdown per account
- Build regression test suite comparing legacy vs. modernized calculations to penny precision

---

### Rank 4: COCRDUPC -- Card Update (Online CICS)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **1,560** | -- |
| IF Statements | 148 | -- |
| EVALUATE Blocks | 16 | -- |
| PERFORM Calls | 26 | -- |
| EXEC CICS Commands | 12 | -- |
| VSAM Files Accessed | 3 (CARDDATA, CARDXREF, ACCTDATA) | -- |
| **Code Complexity** | -- | **9** |
| **Integration Risk** | -- | **7** |
| **Data Sensitivity** | -- | **8** |
| **Business Impact** | -- | **8** |
| **COMPOSITE SCORE** | -- | **8.05** |

**Why #4:** Second-largest online program with 148 IF branches -- the highest conditional density of any online module. Manages card status changes (activate/deactivate), embossed name updates, and expiration date management. Card status changes directly affect whether transactions are authorized. The 16 EVALUATE blocks handle multi-state card lifecycle transitions.

**Modernization Recommendations:**
- Implement Card entity with state machine pattern for lifecycle management
- Separate read (view) from write (update) operations
- Add card status change event publishing for downstream consumers

---

### Rank 5: COCRDLIC -- Card Listing (Online CICS)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **1,459** | -- |
| IF Statements | 122 | -- |
| EVALUATE Blocks | 18 | -- |
| PERFORM Calls | 34 | -- |
| EXEC CICS Commands | 18 | -- |
| VSAM Files Accessed | 3 (CARDDATA, CARDXREF, ACCTDATA) | -- |
| **Code Complexity** | -- | **9** |
| **Integration Risk** | -- | **7** |
| **Data Sensitivity** | -- | **6** |
| **Business Impact** | -- | **6** |
| **COMPOSITE SCORE** | -- | **7.15** |

**Why #5:** High complexity driven by paginated VSAM browsing logic with 18 EVALUATE blocks -- the most in any single program. Implements forward/backward scrolling through card records using CICS STARTBR/READNEXT/READPREV operations. The 122 IF statements handle edge cases: empty results, end-of-file, beginning-of-file, filter criteria, and screen field validation. This pattern repeats in COTRN00C and COUSR00C but is most complex here due to multi-file joins.

**Modernization Recommendations:**
- Replace VSAM browse with paginated SQL queries (LIMIT/OFFSET or keyset pagination)
- Implement as REST endpoint returning JSON (card list with account context)
- Extract pagination logic into a reusable service pattern

---

### Rank 6: CBSTM03A -- Statement Generation (Batch)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **924** | -- |
| IF Statements | 15 | -- |
| EVALUATE Blocks | 5 | -- |
| PERFORM Calls | 29 | -- |
| CALL Statements | 14 (calls CBSTM03B for all I/O) | -- |
| VSAM Files Accessed | 4 (TRXFL, CARDXREF, ACCTDATA, CUSTDATA) | -- |
| Output Files | 2 (STATEMNT.PS, STATEMNT.HTML) | -- |
| **Code Complexity** | -- | **7** |
| **Integration Risk** | -- | **8** |
| **Data Sensitivity** | -- | **8** |
| **Business Impact** | -- | **8** |
| **COMPOSITE SCORE** | -- | **7.70** |

**Why #6:** Generates customer-facing account statements in both text and HTML formats. Reads across 4 VSAM files to assemble complete statement data (customer name/address, account details, card transactions). The 14 CALL statements to CBSTM03B represent a tight caller-callee coupling pattern. Produces regulated financial documents (monthly statements) that must be accurate. Uses advanced COBOL features: mainframe control block addressing, TIOT navigation, and dual-format output.

**Modernization Recommendations:**
- Convert to template-based report generator (e.g., Jasper Reports or HTML/PDF library)
- Merge CBSTM03A + CBSTM03B into a single StatementGenerationService
- Replace flat file output with PDF generation and optional email delivery
- Implement statement archival in database for customer self-service

---

### Rank 7: CBTRN03C -- Transaction Report (Batch)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **649** | -- |
| IF Statements | 75 | -- |
| EVALUATE Blocks | 4 | -- |
| PERFORM Calls | 72 | -- |
| VSAM Files Accessed | 5 (TRANSACT.DALY, CARDXREF, TRANTYPE, TRANCATG, DATEPARM) | -- |
| **Code Complexity** | -- | **7** |
| **Integration Risk** | -- | **8** |
| **Data Sensitivity** | -- | **6** |
| **Business Impact** | -- | **7** |
| **COMPOSITE SCORE** | -- | **7.00** |

**Why #7:** Generates the daily transaction report used for management review and audit. High PERFORM count (72) indicates deeply modular internal structure with many subroutines. Joins transaction data with type and category lookup tables to produce enriched reports. The 75 IF branches handle date range filtering, page breaks, subtotals, grand totals, and edge cases. Date parameter handling via DATEPARM file adds operational complexity.

**Modernization Recommendations:**
- Convert to scheduled reporting service with configurable date ranges
- Replace GDG output with database-backed report storage
- Add export to CSV/Excel format for business users
- Implement as parameterized query with aggregation in SQL

---

### Rank 8: COACTVWC -- Account View (Online CICS)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **941** | -- |
| IF Statements | 57 | -- |
| EVALUATE Blocks | 10 | -- |
| PERFORM Calls | 21 | -- |
| EXEC CICS Commands | 15 | -- |
| VSAM Files Accessed | 3 (ACCTDATA, CARDXREF, CUSTDATA) | -- |
| **Code Complexity** | -- | **6** |
| **Integration Risk** | -- | **7** |
| **Data Sensitivity** | -- | **8** |
| **Business Impact** | -- | **7** |
| **COMPOSITE SCORE** | -- | **6.95** |

**Why #8:** Read-only account viewer but still complex due to multi-file assembly (account + customer + card cross-reference). Displays sensitive financial data (balances, credit limits, SSN) on screen. The 10 EVALUATE blocks handle screen state transitions. While read-only, it's a critical customer service tool -- incorrect display could lead to wrong decisions by operators.

**Modernization Recommendations:**
- Convert to read-only REST endpoint (GET /accounts/{id})
- Mask sensitive fields (SSN, full card numbers) in UI
- Implement caching for frequently accessed accounts

---

### Rank 9: COCRDSLC -- Card Selection/View (Online CICS)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **887** | -- |
| IF Statements | 68 | -- |
| EVALUATE Blocks | 8 | -- |
| PERFORM Calls | 19 | -- |
| EXEC CICS Commands | 14 | -- |
| VSAM Files Accessed | 3 (CARDDATA, CARDXREF, ACCTDATA) | -- |
| **Code Complexity** | -- | **6** |
| **Integration Risk** | -- | **7** |
| **Data Sensitivity** | -- | **7** |
| **Business Impact** | -- | **6** |
| **COMPOSITE SCORE** | -- | **6.45** |

**Why #9:** Card detail view with navigation to card update. Displays full card number, CVV, expiration -- all PCI-DSS sensitive data. The 68 IF branches handle card record retrieval, cross-reference validation, and BMS field population. Acts as the gateway to card update operations (COCRDUPC), so any issue here blocks the card management workflow.

**Modernization Recommendations:**
- Implement PCI-DSS compliant card data masking (show last 4 digits only)
- Convert to REST endpoint with role-based field visibility
- Consider tokenization of card numbers in the modernized system

---

### Rank 10: COTRN02C -- Transaction Add (Online CICS)

| Metric | Value | Score |
|--------|-------|-------|
| Lines of Code | **783** | -- |
| IF Statements | 14 | -- |
| EVALUATE Blocks | 13 | -- |
| PERFORM Calls | 61 | -- |
| EXEC CICS Commands | 11 | -- |
| CALL Statements | 2 (CSUTLDTC for date validation) | -- |
| VSAM Files Accessed | 3 (TRANSACT, CARDXREF, ACCTDATA) | -- |
| **Code Complexity** | -- | **6** |
| **Integration Risk** | -- | **7** |
| **Data Sensitivity** | -- | **8** |
| **Business Impact** | -- | **7** |
| **COMPOSITE SCORE** | -- | **6.95** |

**Why #10:** The online entry point for new transactions. Writes directly to the TRANSACT VSAM file, making it a critical data-mutation path. The 13 EVALUATE blocks handle transaction type selection, validation, and confirmation flows. Calls CSUTLDTC for date validation, adding an external dependency. Transaction creation affects downstream batch processing (posting, interest calc, reporting). A validation gap here could allow invalid transactions into the pipeline.

**Modernization Recommendations:**
- Convert to REST POST endpoint with request validation (Bean Validation)
- Implement async transaction creation with event-driven processing
- Add duplicate detection based on transaction attributes
- Integrate with fraud detection service pre-commit

---

## Summary Ranking Table

| Rank | Module | LOC | Complexity | Integration | Data Sensitivity | Business Impact | **Score** |
|------|--------|-----|-----------|-------------|-----------------|-----------------|-----------|
| 1 | COACTUPC | 4,236 | 10 | 9 | 9 | 9 | **9.30** |
| 2 | CBTRN02C | 731 | 8 | 10 | 10 | 10 | **9.50** |
| 3 | CBACT04C | 652 | 8 | 9 | 10 | 10 | **9.25** |
| 4 | COCRDUPC | 1,560 | 9 | 7 | 8 | 8 | **8.05** |
| 5 | COCRDLIC | 1,459 | 9 | 7 | 6 | 6 | **7.15** |
| 6 | CBSTM03A | 924 | 7 | 8 | 8 | 8 | **7.70** |
| 7 | CBTRN03C | 649 | 7 | 8 | 6 | 7 | **7.00** |
| 8 | COACTVWC | 941 | 6 | 7 | 8 | 7 | **6.95** |
| 9 | COCRDSLC | 887 | 6 | 7 | 7 | 6 | **6.45** |
| 10 | COTRN02C | 783 | 6 | 7 | 8 | 7 | **6.95** |

---

## Migration Wave Recommendation

Based on the hotspot analysis, we recommend the following migration wave strategy:

### Wave 1: Foundation (Low Risk, High Reuse)
- **CSUTLDTC** (Date utility) -- small, shared, no CICS dependency
- **COCOM01Y** structure -- design the Java session/context equivalent
- All copybook-to-POJO conversions (DATA_DICTIONARY.md entities)

### Wave 2: Read-Only Online (Medium Risk)
- **COACTVWC** (Account View) -- read-only, good first online conversion
- **COCRDSLC** (Card View) -- read-only card display
- **COTRN01C** (Transaction View) -- read-only transaction display
- **COSGN00C** (Sign-on) -- convert to modern auth

### Wave 3: CRUD Online (Higher Risk)
- **COUSR00C-03C** (User CRUD) -- self-contained admin functions
- **COTRN02C** (Transaction Add) -- online data entry
- **COCRDUPC** (Card Update) -- card status management
- **COACTUPC** (Account Update) -- the largest, do last in this wave

### Wave 4: Batch Financial Core (Highest Risk)
- **CBTRN02C** (Transaction Posting) -- core financial engine
- **CBACT04C** (Interest Calculation) -- regulated financial logic
- **CBSTM03A/B** (Statement Generation) -- customer-facing output
- **CBTRN03C** (Transaction Report) -- management reporting

### Wave 5: Navigation and Integration
- **COMEN01C / COADM01C** (Menus) -- replace with web navigation
- **COCRDLIC / COTRN00C / COUSR00C** (List screens) -- pagination patterns
- **CORPT00C / COBIL00C** (Reports, Billing) -- remaining online functions

### Wave 6: Optional Modules
- Authorization Module (IMS/DB2/MQ) -- if in scope
- Transaction Type DB2 Module -- if in scope
- VSAM-MQ Module -- if in scope

---

## Key Risk Factors for Modernization

| Risk | Affected Modules | Mitigation |
|------|-----------------|------------|
| Financial calculation precision | CBTRN02C, CBACT04C | Use BigDecimal; build penny-level regression tests |
| PII/PCI data handling | COACTUPC, COCRDSLC, COACTVWC | Implement encryption, tokenization, masking |
| Plaintext passwords | COSGN00C, CSUSR01Y | Hash + salt immediately; add MFA |
| COMMAREA coupling | All 18 online programs | Replace with stateless HTTP context / JWT |
| VSAM browse pagination | COCRDLIC, COTRN00C, COUSR00C | Replace with SQL pagination (keyset or offset) |
| Batch-online file contention | CLOSEFIL/OPENFIL cycle | Eliminate with database (concurrent read/write) |
| GDG-based backups | TRANBKP, COMBTRAN, TRANREPT | Replace with database point-in-time recovery |
| Dual-format statement output | CBSTM03A | Use template engine (HTML→PDF) |
