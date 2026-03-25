# CardDemo Hotspot Report — Top 10 Modernization-Priority Modules

> **Generated**: 2026-03-25 | **Application**: CardDemo — Mainframe Credit Card Management System
> **Purpose**: Identify the highest-priority modules for modernization based on complexity, risk, and business impact

---

## Scoring Methodology

Each module is scored on three dimensions (1–10 scale each), then ranked by **composite score** (weighted sum):

| Dimension | Weight | Criteria |
|-----------|--------|----------|
| **Complexity** | 35% | Lines of code, cyclomatic indicators (EVALUATE/IF branches), PERFORM count, GO TO usage, COPY REPLACING, number of copybook dependencies, CICS commands, file I/O operations |
| **Risk** | 30% | Data mutation (REWRITE/WRITE/DELETE), financial calculations, cross-file updates, abend handling, GO TO spaghetti, number of VSAM files accessed simultaneously |
| **Business Impact** | 35% | Revenue criticality, user-facing importance, batch cycle criticality, data integrity role, regulatory relevance |

**Composite Score** = (Complexity × 0.35) + (Risk × 0.30) + (Business Impact × 0.35)

---

## Top 10 Hotspot Modules

### #1 — COACTUPC (Account Update) — Score: 9.4

| Metric | Value | Notes |
|--------|-------|-------|
| **Lines of Code** | 4,236 | Largest program in the entire codebase |
| **EVALUATE Statements** | 20 | Complex multi-branch decision logic |
| **IF Statements** | 167 | Highest conditional count of any program |
| **PERFORM Statements** | 64 | Many paragraph-level calls |
| **GO TO Statements** | 51 | Significant spaghetti code risk |
| **EXEC CICS Commands** | 17 | Heavy CICS interaction |
| **Copybooks Included** | 56 | Includes 39 COPY REPLACING (CSSETATY) |
| **VSAM Files Accessed** | 4 | ACCTDATA, CARDXREF, CUSTDATA, CARDDATA |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **10** | Largest program, most IF statements, 51 GO TOs, 39 COPY REPLACING macros, handles ABEND |
| Risk | **9** | Mutates account master (REWRITE), reads 4 VSAM files, financial data updates, abend handling |
| Business Impact | **9** | Core account update — every account change flows through this program |
| **Composite** | **9.4** | |

**Modernization Concerns**:
- 51 GO TO statements create non-linear control flow that is extremely difficult to translate to structured Java
- 39 COPY REPLACING invocations for attribute setting will need a fundamentally different UI approach
- Handles ABEND with EXEC CICS HANDLE ABEND — needs Java exception handling strategy
- Cross-file reads (account + customer + card + xref) suggest this should decompose into multiple services

---

### #2 — CBTRN02C (Transaction Posting — Batch) — Score: 8.5

| Metric | Value | Notes |
|--------|-------|-------|
| **Lines of Code** | 731 | Medium-large batch program |
| **IF Statements** | 48 | Complex validation and branching |
| **PERFORM Statements** | 62 | High paragraph reuse |
| **VSAM Files Accessed** | 6 | DALYTRAN, TRANSACT, XREFFILE, DALYREJS, ACCTFILE, TCATBALF |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **8** | 6 file I/O operations, 48 conditionals, complex posting logic |
| Risk | **10** | Core financial posting — writes to TRANSACT, updates ACCTDATA and TCATBAL, creates rejects |
| Business Impact | **9** | Heart of nightly batch cycle; incorrect posting = financial loss |
| **Composite** | **8.5** | |

**Modernization Concerns**:
- Reads daily transactions sequentially, validates against XREF, posts to master, updates account balances, writes rejects
- Multi-file transactional consistency (no DB commit/rollback in VSAM) — needs careful Spring Batch design
- Reject handling logic must be preserved exactly for audit compliance

---

### #3 — COCRDLIC (Credit Card List) — Score: 8.0

| Metric | Value | Notes |
|--------|-------|-------|
| **Lines of Code** | 1,459 | Second-largest online program |
| **EVALUATE Statements** | 18 | Multi-way branching |
| **IF Statements** | 60 | Heavy conditional logic |
| **GO TO Statements** | 16 | Moderate spaghetti |
| **EXEC CICS Commands** | 18 | Highest CICS interaction count |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **9** | STARTBR/READNEXT/READPREV browsing, 18 CICS commands, pagination logic, XCTL navigation |
| Risk | **7** | Read-heavy but controls navigation to update screens |
| Business Impact | **8** | Primary card lookup — gateway to card detail and update functions |
| **Composite** | **8.0** | |

**Modernization Concerns**:
- CICS browse operations (STARTBR/READNEXT/READPREV/ENDBR) for pagination must map to database cursor or paginated queries
- XCTL to COCRDSLC and COCRDUPC means screen flow state management is critical
- Admin vs. regular user filtering logic embedded in program

---

### #4 — COCRDUPC (Credit Card Update) — Score: 7.9

| Metric | Value | Notes |
|--------|-------|-------|
| **Lines of Code** | 1,560 | Third-largest program |
| **EVALUATE Statements** | 16 | Complex branching |
| **IF Statements** | 73 | Second-highest conditional count |
| **GO TO Statements** | 21 | Significant non-linear flow |
| **EXEC CICS Commands** | 12 | Multiple CICS operations |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **9** | 73 IFs, 21 GO TOs, ABEND handling, multiple CICS operations |
| Risk | **8** | Mutates card data (REWRITE), cross-references customer data |
| Business Impact | **7** | Card updates affect active cards — status changes, name changes |
| **Composite** | **7.9** | |

**Modernization Concerns**:
- Similar structure to COACTUPC but for card entity
- GO TO statements complicate Java translation
- ABEND handling requires exception strategy

---

### #5 — CBACT04C (Interest Calculation) — Score: 7.8

| Metric | Value | Notes |
|--------|-------|-------|
| **Lines of Code** | 652 | Medium batch program |
| **IF Statements** | 43 | Complex business rule logic |
| **PERFORM Statements** | 57 | High paragraph count |
| **VSAM Files Accessed** | 5 | TCATBAL, XREFFILE, ACCTFILE, DISCGRP, TRANSACT |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **7** | 5 VSAM files, complex interest rate lookups via disclosure groups |
| Risk | **10** | Financial calculation — incorrect interest = regulatory/legal exposure |
| Business Impact | **8** | Interest revenue generation; must be penny-accurate |
| **Composite** | **7.8** | |

**Modernization Concerns**:
- Interest calculation logic with disclosure group lookups is core business IP
- COMP fields and implied decimal arithmetic (`PIC S9(09)V99`) require exact `BigDecimal` translation
- Must preserve rounding behavior exactly — even off-by-one-cent errors are compliance issues
- Reads TCATBAL (category balances) and DISCGRP (interest rates) to compute per-category interest

---

### #6 — CBSTM03A (Statement Generation) — Score: 7.5

| Metric | Value | Notes |
|--------|-------|-------|
| **Lines of Code** | 924 | Large batch program |
| **EVALUATE Statements** | 9 | Moderate branching |
| **GO TO Statements** | 15 | Legacy control flow |
| **CALL Statements** | 14 | Calls CBSTM03B 13 times, CEE3ABD once |
| **File I/O** | Reads 4 VSAM files, writes 2 output files |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **8** | Dual-format output (text + HTML), mainframe control block addressing, subroutine calls |
| Risk | **7** | Customer-facing output — statement errors affect customer trust |
| Business Impact | **7** | Generates customer-visible account statements |
| **Composite** | **7.5** | |

**Modernization Concerns**:
- Generates both plaintext and HTML statements — dual output path
- Calls CBSTM03B subroutine 13 times for file I/O — tight coupling
- Uses mainframe-specific control block addressing (noted in comments)
- GO TO statements (15) in batch context create complex loop patterns
- Statement formatting logic (headers, detail lines, totals) maps well to a template engine in Java

---

### #7 — COBIL00C (Bill Payment) — Score: 7.3

| Metric | Value | Notes |
|--------|-------|-------|
| **Lines of Code** | 572 | Medium online program |
| **EVALUATE Statements** | 18 | High for its size |
| **EXEC CICS Commands** | 13 | Heavy CICS usage |
| **VSAM Files Accessed** | 3 | ACCTDATA, CARDXREF, TRANSACT |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **7** | 18 EVALUATEs in 572 lines = high decision density, ASKTIME/FORMATTIME for timestamps |
| Risk | **8** | Creates financial transactions (WRITE to TRANSACT), updates account balances (REWRITE ACCTDATA) |
| Business Impact | **7** | Customer-facing payment function; errors = incorrect balances |
| **Composite** | **7.3** | |

**Modernization Concerns**:
- Uses EXEC CICS ASKTIME/FORMATTIME for transaction timestamps — needs Java time API equivalent
- Writes new transaction records AND updates account balance in same operation — atomicity concern
- Browse operations on CARDXREF for account lookup via card number

---

### #8 — COTRN02C (Transaction Add — Online) — Score: 7.1

| Metric | Value | Notes |
|--------|-------|-------|
| **Lines of Code** | 783 | Medium-large online program |
| **EVALUATE Statements** | 26 | Highest EVALUATE count of any program |
| **PERFORM Statements** | 61 | High reuse |
| **EXEC CICS Commands** | 11 | Moderate CICS usage |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **7** | 26 EVALUATEs = complex state machine, field validation, screen state management |
| Risk | **8** | Creates new transaction records; validation errors = bad data in master file |
| Business Impact | **7** | Online transaction entry — used by operators to add transactions |
| **Composite** | **7.1** | |

**Modernization Concerns**:
- 26 EVALUATE statements suggest a complex state machine for form validation and submission
- Calls CSUTLDTC for date validation
- Must validate transaction type/category codes against reference files

---

### #9 — CBTRN03C (Transaction Detail Report) — Score: 6.8

| Metric | Value | Notes |
|--------|-------|-------|
| **Lines of Code** | 649 | Medium batch program |
| **EVALUATE Statements** | 4 | Moderate |
| **IF Statements** | 38 | Complex conditionals |
| **PERFORM Statements** | 73 | Highest PERFORM count |
| **VSAM Files Accessed** | 6 | TRANSACT, CARDXREF, TRANTYPE, TRANCATG, TRANREPT, DATEPARM |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **7** | 6 files, 73 PERFORMs, date range filtering, page-break logic, multi-level totals |
| Risk | **6** | Read-only reporting — lower mutation risk, but inaccurate reports affect decisions |
| Business Impact | **7** | Regulatory reporting — transaction detail reports may be audited |
| **Composite** | **6.8** | |

**Modernization Concerns**:
- Complex report formatting with page headers, column headers, detail lines, page/account/grand totals
- Date parameter file (DATEPARM) for date range filtering
- Joins 4 reference files (XREF, TRANTYPE, TRANCATG) with transaction data
- 73 PERFORM statements = many small paragraphs — maps well to Java methods

---

### #10 — COACTVWC (Account View) — Score: 6.6

| Metric | Value | Notes |
|--------|-------|-------|
| **Lines of Code** | 941 | Medium-large online program |
| **EVALUATE Statements** | 10 | Moderate branching |
| **IF Statements** | 29 | Moderate conditionals |
| **GO TO Statements** | 9 | Some non-linear flow |
| **EXEC CICS Commands** | 15 | Heavy CICS usage |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **7** | 15 CICS commands, reads 3 VSAM files, SEND TEXT for error display, ABEND handling |
| Risk | **5** | Read-only — no data mutation, but gateway to account update |
| Business Impact | **8** | Primary account inquiry — most-used screen for customer service |
| **Composite** | **6.6** | |

**Modernization Concerns**:
- Read-only but loads data from 3 VSAM files (account, customer, card xref) — natural candidate for a read service/API
- XCTL to COACTUPC means view-to-edit transition must be preserved
- SEND TEXT for error handling uses a different BMS path than normal SEND MAP
- String parsing (CSSTRPFY copybook) for data formatting

---

## Complexity Metrics Summary (All Core Programs)

| Program | Lines | EVALUATE | IF | PERFORM | GO TO | CALL | CICS Cmds | Copybooks | Type |
|---------|-------|----------|-----|---------|-------|------|-----------|-----------|------|
| **COACTUPC** | **4,236** | **20** | **167** | **64** | **51** | 0 | **17** | **56** | Online |
| **COCRDUPC** | **1,560** | 16 | **73** | 26 | **21** | 0 | 12 | 15 | Online |
| **COCRDLIC** | **1,459** | **18** | **60** | 34 | 16 | 3 | **18** | 13 | Online |
| COACTVWC | 941 | 10 | 29 | 21 | 9 | 0 | 15 | 15 | Online |
| **CBSTM03A** | **924** | 9 | 15 | 33 | **15** | **14** | 0 | 4 | Batch |
| COCRDSLC | 887 | 8 | 33 | 19 | 9 | 0 | 14 | 15 | Online |
| COTRN02C | 783 | **26** | 14 | **61** | 0 | 2 | 11 | 10 | Online |
| **CBTRN02C** | **731** | 0 | **48** | **62** | 0 | 1 | 0 | 5 | Batch |
| COTRN00C | 699 | 16 | 26 | 47 | 0 | 0 | 10 | 8 | Online |
| COUSR00C | 695 | 16 | 25 | 45 | 0 | 0 | 11 | 8 | Online |
| **CBACT04C** | **652** | 0 | **43** | **57** | 0 | 1 | 0 | 5 | Batch |
| CORPT00C | 649 | 10 | 20 | 35 | 1 | 2 | 7 | 8 | Online |
| **CBTRN03C** | **649** | 4 | **38** | **73** | 0 | 1 | 0 | 5 | Batch |
| CBEXPORT | 582 | 0 | 16 | 50 | 0 | 1 | 0 | 6 | Batch |
| COBIL00C | 572 | **18** | 10 | 38 | 0 | 0 | **13** | 10 | Online |
| CBTRN01C | 494 | 0 | 33 | 43 | 0 | 1 | 0 | 6 | Batch |
| CBIMPORT | 487 | 2 | 14 | 30 | 0 | 1 | 0 | 6 | Batch |
| CBACT01C | 430 | 0 | 22 | 36 | 0 | 3 | 0 | 2 | Batch |
| COUSR02C | 414 | 10 | 13 | 31 | 0 | 0 | 6 | 8 | Online |
| COUSR03C | 359 | 10 | 8 | 26 | 0 | 0 | 6 | 8 | Online |
| COTRN01C | 330 | 6 | 7 | 17 | 0 | 0 | 5 | 8 | Online |
| COMEN01C | 308 | 6 | 7 | 15 | 0 | 0 | 7 | 9 | Online |
| COUSR01C | 299 | 6 | 4 | 20 | 0 | 0 | 5 | 9 | Online |
| COADM01C | 288 | 4 | 6 | 15 | 0 | 0 | 7 | 9 | Online |
| COSGN00C | 260 | 6 | 4 | 11 | 0 | 0 | 10 | 9 | Online |
| CBSTM03B | 230 | 1 | 12 | 4 | 13 | 0 | 0 | 0 | Batch |
| CBACT02C | 178 | 0 | 11 | 11 | 0 | 1 | 0 | 1 | Batch |
| CBACT03C | 178 | 0 | 11 | 11 | 0 | 1 | 0 | 1 | Batch |
| CBCUS01C | 178 | 0 | 11 | 11 | 0 | 1 | 0 | 1 | Batch |
| CSUTLDTC | 157 | 2 | 0 | 1 | 0 | 2 | 0 | 0 | Utility |
| COBSWAIT | 41 | 0 | 0 | 0 | 0 | 1 | 0 | 0 | Utility |

---

## Recommended Modernization Waves

Based on the hotspot analysis, we recommend the following migration waves:

### Wave 1 — Foundation (Low Risk, High Reuse)
**Target**: Reference data services, authentication, navigation
- COSGN00C (Sign-on) → Spring Security authentication service
- COMEN01C / COADM01C (Menus) → UI navigation framework
- CSUTLDTC (Date utility) → Java `java.time` utility class
- COUSR00C–03C (User CRUD) → Spring Data JPA user management

### Wave 2 — Read Services (Low Risk, High Impact)
**Target**: Read-only screens, inquiry APIs
- COACTVWC (Account View) → Account inquiry REST API
- COCRDSLC (Card Detail) → Card inquiry REST API
- COTRN00C / COTRN01C (Transaction List/View) → Transaction query API
- CBACT01C–03C, CBCUS01C (File readers) → Data access layer tests

### Wave 3 — Core Mutations (High Risk, High Impact)
**Target**: The core business logic hotspots
- COACTUPC (Account Update) → Account update service (decompose the 4,236-line monolith)
- COCRDUPC (Card Update) → Card update service
- COCRDLIC (Card List) → Paginated card query service
- COBIL00C (Bill Payment) → Payment processing service
- COTRN02C (Transaction Add) → Transaction entry service

### Wave 4 — Batch Processing (Highest Risk)
**Target**: Nightly batch cycle — Spring Batch jobs
- CBTRN02C (Transaction Posting) → Spring Batch posting job with DB transactions
- CBACT04C (Interest Calculation) → Spring Batch interest job (penny-exact BigDecimal)
- CBSTM03A/B (Statement Generation) → Template-based statement generator
- CBTRN03C (Transaction Report) → Reporting service with JasperReports or similar
- CBEXPORT/CBIMPORT (Data Migration) → Spring Batch import/export jobs

### Wave 5 — Optional Modules
**Target**: IMS/DB2/MQ extensions (only if in scope)
- Authorization module → Microservice with JMS/Kafka replacing MQ
- Transaction Type DB2 → JPA entities replacing embedded SQL
- VSAM-MQ module → REST API replacing MQ request/response

---

## Key Risks for Modernization

| Risk | Impact | Mitigation |
|------|--------|-----------|
| GO TO spaghetti in COACTUPC (51) and COCRDUPC (21) | Extremely difficult to translate to structured Java | Use control flow analysis tools; may need manual rewrite |
| Implied decimal arithmetic (`PIC S9(n)V99`) | Rounding differences between COBOL and Java | Use `BigDecimal` with explicit `HALF_EVEN` rounding; create comprehensive numeric test suite |
| CICS COMMAREA state management | No direct Java equivalent | Map to HTTP session or JWT token with state |
| COPY REPLACING macro expansion | 39 instances in COACTUPC alone | Requires build-time or compile-time resolution before translation |
| Multi-file VSAM consistency | No ACID transactions in VSAM | Leverage database transactions in Java; design compensating transactions |
| Plaintext passwords in USRSEC | Security vulnerability | Migrate to bcrypt/scrypt hashing immediately |
| BMS 3270 terminal UI | No modern equivalent | Redesign as web UI forms; use BMS field definitions as requirements |
| EBCDIC character encoding | Data conversion needed | Apply EBCDIC-to-UTF8 conversion during data migration |
