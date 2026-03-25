# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo | **Purpose:** Prioritize modules for modernization by complexity, risk, and business impact

---

## Methodology

Each module is scored across three dimensions on a 1-10 scale:

| Dimension | Weight | Scoring Criteria |
|-----------|--------|-----------------|
| **Complexity** | 35% | Lines of code, cyclomatic complexity (IF/EVALUATE/PERFORM counts), number of copybook dependencies, CICS command variety, COPY REPLACING usage |
| **Risk** | 35% | Data mutation (WRITE/REWRITE/DELETE), number of VSAM files accessed, financial calculations, error handling patterns, cross-program coupling |
| **Business Impact** | 30% | Revenue criticality, user-facing frequency, regulatory/compliance relevance, downstream dependencies |

**Composite Score** = (Complexity x 0.35) + (Risk x 0.35) + (Business Impact x 0.30)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC - Account Update

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 4,236 | Largest program in the entire codebase |
| **IF Statements** | 164 | Highest conditional complexity |
| **EVALUATE Blocks** | 10 | Multiple decision branches |
| **PERFORM Calls** | 61 | High procedural decomposition |
| **Copybook Dependencies** | 18 (incl. 38 COPY REPLACING) | Most complex include structure |
| **VSAM Files Accessed** | 4 (ACCTDATA, CARDXREF, CARDDATA, CUSTDATA) | Reads + Rewrites |
| **CICS Commands** | HANDLE ABEND, XCTL, RETURN, RECEIVE, SEND, READ(x5), REWRITE, ABEND | Full CICS command palette |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **10** | 4,236 lines, 164 IFs, 38 COPY REPLACING macros for field attribute setting, most copybook deps |
| Risk | **9** | Mutates account master data (REWRITE), reads 4 VSAM files, HANDLE ABEND pattern, field-level validation |
| Business Impact | **9** | Core account management - directly affects customer balances and credit limits |
| **Composite** | **9.4** | |

**Modernization Notes:**
- The 38 COPY REPLACING instances for CSSETATY (screen attribute setting) represent a macro pattern that should become a reusable UI component
- Field-level validation logic (164 IFs) maps to Java Bean Validation annotations
- HANDLE ABEND / ABEND pattern needs structured exception handling
- Highest priority for unit test coverage due to financial data mutation

---

### Rank 2: CBTRN02C - Transaction Posting (Batch)

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 731 | Medium-large batch program |
| **IF Statements** | 48 | Heavy conditional logic |
| **PERFORM Calls** | 61 | Most PERFORM statements of any program |
| **Copybook Dependencies** | 5 (CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y) | Core financial copybooks |
| **Files Accessed** | 6 (DALYTRAN, TRANSACT, CARDXREF, DALYREJS, ACCTDATA, TCATBALF) | Most file I/O of any batch |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **8** | 731 lines, 61 PERFORMs, 48 IFs, 6 files with mixed read/write, complex posting logic |
| Risk | **10** | Core financial posting - writes to TRANSACT master, updates TCATBALF balances, produces rejection GDG. Data integrity is critical |
| Business Impact | **10** | Heart of the batch cycle - if this fails, no transactions post, downstream reporting and interest calc break |
| **Composite** | **9.3** | |

**Modernization Notes:**
- Transaction posting is the most critical batch job - requires idempotency in modern design
- Rejection handling (DALYREJS GDG) should become an event-driven error queue
- Category balance updates (TCATBALF) represent an aggregation pattern → consider materialized views or CQRS
- Must maintain exact decimal arithmetic (COMP-3 → Java BigDecimal)

---

### Rank 3: CBACT04C - Interest Calculation (Batch)

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 652 | Complex financial logic |
| **IF Statements** | 43 | Many business rule branches |
| **PERFORM Calls** | 56 | High procedural depth |
| **Copybook Dependencies** | 5 (CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y) | Financial data structures |
| **Files Accessed** | 5 (TCATBALF, CARDXREF, ACCTDATA, DISCGRP, TRANSACT) | Multi-file financial calc |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **8** | 652 lines, 56 PERFORMs, 43 IFs, multi-file joins via sequential reads, rate lookup logic |
| Risk | **10** | Directly computes interest charges on customer accounts. Incorrect calculation = financial loss or regulatory violation |
| Business Impact | **9** | Revenue-generating process. Interest income is core business. Subject to regulatory audit |
| **Composite** | **9.0** | |

**Modernization Notes:**
- Interest rate lookup via Disclosure Group (CVTRA02Y) is a rate engine pattern → consider a configurable rules engine
- COMP-3 packed decimal arithmetic must preserve exact precision in Java (BigDecimal, not double)
- Needs comprehensive regression testing with known-good calculation results
- Audit trail (SYSTRAN GDG) should become an immutable event log

---

### Rank 4: COCRDLIC - Card List (Online)

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 1,459 | Large online program |
| **IF Statements** | 59 | Complex browse/pagination logic |
| **EVALUATE Blocks** | 9 | Multiple state handling paths |
| **PERFORM Calls** | 30 | Moderate procedural depth |
| **Copybook Dependencies** | 11 | Broad include set |
| **CICS Commands** | XCTL(x3), RETURN, SEND, RECEIVE, STARTBR, READNEXT(x2), ENDBR, READPREV(x2), SEND TEXT | Full browse command set |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **8** | 1,459 lines, 59 IFs, STARTBR/READNEXT/READPREV pagination is complex stateful browse logic |
| Risk | **7** | Read-only data access but complex navigation state; 3 XCTL targets means tight coupling |
| Business Impact | **8** | Primary card lookup screen - used by every customer service interaction |
| **Composite** | **7.7** | |

**Modernization Notes:**
- STARTBR/READNEXT/READPREV pagination pattern → paginated REST API with cursor-based pagination
- Three XCTL targets (card view, card update, account view) represent navigation coupling → REST links / HATEOAS
- 59 IF statements mostly handle edge cases in pagination → simplifies significantly with SQL OFFSET/LIMIT

---

### Rank 5: COCRDUPC - Card Update (Online)

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 1,560 | Second-largest online program |
| **IF Statements** | 72 | Heavy validation logic |
| **EVALUATE Blocks** | 8 | Multiple state handling |
| **PERFORM Calls** | 26 | Moderate depth |
| **Copybook Dependencies** | 13 | Many includes |
| **CICS Commands** | HANDLE ABEND, XCTL, RETURN, RECEIVE, SEND, READ(x2), REWRITE, ABEND | Full CRUD command set |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **8** | 1,560 lines, 72 IFs, 8 EVALUATEs, HANDLE ABEND pattern, COPY REPLACING |
| Risk | **8** | Mutates card data (REWRITE), reads customer and account data for cross-validation |
| Business Impact | **7** | Card detail changes affect transaction processing and customer-facing card info |
| **Composite** | **7.7** | |

**Modernization Notes:**
- Card update validation (72 IFs) should become declarative validation rules
- HANDLE ABEND pattern needs structured try/catch in Java
- REWRITE to CARDDATA VSAM → JPA/Hibernate UPDATE with optimistic locking

---

### Rank 6: CBSTM03A - Statement Generation (Batch)

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 924 | Large batch with dual output |
| **IF Statements** | 15 | Moderate branching |
| **PERFORM Calls** | 29 | Good decomposition |
| **CALL Statements** | 14 (13 to CBSTM03B, 1 to CEE3ABD) | Heavy subroutine delegation |
| **Files Accessed** | 6 (TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE → STMTFILE, HTMLFILE) | Multi-file read, dual write |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **8** | 924 lines, dual-format output (text + HTML), 13 calls to subroutine, multi-file joins |
| Risk | **7** | Read-only from source files, but output quality affects customer communications |
| Business Impact | **8** | Customer-facing statements - regulatory requirement (Truth in Lending). PDF generation downstream |
| **Composite** | **7.7** | |

**Modernization Notes:**
- Dual output (text + HTML) → modern template engine (Thymeleaf, FreeMarker)
- CBSTM03B subroutine is called 13 times → extract as a service/repository layer
- SORT step in JCL pre-processes data → SQL ORDER BY in modern approach
- PDF conversion (TXT2PDF1 JCL) → direct PDF generation library (iText, Apache PDFBox)

---

### Rank 7: CBTRN03C - Transaction Report (Batch)

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 649 | Medium-large batch |
| **IF Statements** | 38 | Complex report logic |
| **PERFORM Calls** | 72 | Highest PERFORM count of any program |
| **EVALUATE Blocks** | 2 | |
| **Files Accessed** | 6 (TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM → TRANREPT) | Multi-file report |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **7** | 649 lines, 72 PERFORMs (highest), 38 IFs, 6 files, complex report formatting (CVTRA07Y) |
| Risk | **6** | Read-only source files, output-only report; risk is in report accuracy |
| Business Impact | **8** | Daily transaction report used for reconciliation and audit. Date-parameterized for flexibility |
| **Composite** | **7.0** | |

**Modernization Notes:**
- 72 PERFORM statements indicate deep procedural decomposition → refactor into report service methods
- Report formatting (CVTRA07Y header/detail/total structures) → JasperReports or similar
- Date parameter file (DATEPARM) → REST API query parameters
- GDG output → versioned file storage or report archive service

---

### Rank 8: COTRN02C - Transaction Add (Online)

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 783 | Medium online program |
| **IF Statements** | 14 | Moderate validation |
| **EVALUATE Blocks** | 13 | Highest EVALUATE count |
| **PERFORM Calls** | 61 | High procedural depth |
| **CALL Statements** | 2 (CSUTLDTC for date validation) | |
| **CICS Commands** | RETURN, SEND, RECEIVE, READ(x2), STARTBR, READPREV, ENDBR, WRITE | Full transaction lifecycle |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **7** | 783 lines, 13 EVALUATEs (highest), 61 PERFORMs, date validation via CALL, auto-ID generation |
| Risk | **8** | WRITE to TRANSACT master - creates financial records. Transaction ID generation must be unique |
| Business Impact | **7** | Online transaction entry - less critical than posting but directly creates financial data |
| **Composite** | **7.3** | |

**Modernization Notes:**
- 13 EVALUATE blocks represent a state-machine pattern → consider State design pattern
- Transaction ID generation (STARTBR/READPREV to find last ID) → database sequence or UUID
- Date validation via CALL to CSUTLDTC → java.time validation
- WRITE to TRANSACT → JPA persist with transaction management

---

### Rank 9: COBIL00C - Bill Payment (Online)

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 572 | Medium online program |
| **IF Statements** | 10 | Light validation |
| **EVALUATE Blocks** | 9 | Multiple payment paths |
| **PERFORM Calls** | 38 | Moderate depth |
| **CICS Commands** | RETURN, ASKTIME, FORMATTIME, SEND, RECEIVE, READ, REWRITE, STARTBR, READPREV, ENDBR, WRITE | Comprehensive command set |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **6** | 572 lines, moderate branching, ASKTIME/FORMATTIME for timestamps, balance update logic |
| Risk | **9** | Directly modifies account balances (REWRITE ACCTDATA) and creates transactions (WRITE TRANSACT). Financial integrity critical |
| Business Impact | **8** | Customer-facing payment processing - revenue impact, customer satisfaction |
| **Composite** | **7.6** | |

**Modernization Notes:**
- Payment processing must be atomic (update balance + create transaction) → database transaction with rollback
- ASKTIME/FORMATTIME → java.time.Instant for timestamps
- Balance update (REWRITE) needs optimistic locking to prevent concurrent modification
- Consider idempotency key to prevent duplicate payments

---

### Rank 10: COACTVWC - Account View (Online)

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 941 | Large view-only program |
| **IF Statements** | 28 | Moderate display logic |
| **EVALUATE Blocks** | 5 | Multiple view states |
| **PERFORM Calls** | 18 | Moderate depth |
| **Copybook Dependencies** | 15 | Most copybook deps of any online view |
| **CICS Commands** | HANDLE ABEND, XCTL, RETURN, SEND, RECEIVE, READ(x3), SEND TEXT, ABEND | Full read + error handling |

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **7** | 941 lines, 15 copybooks, 3 READ operations to assemble view, HANDLE ABEND |
| Risk | **5** | Read-only - no data mutation. Risk is in data assembly accuracy |
| Business Impact | **8** | Primary account inquiry screen - every customer interaction starts here |
| **Composite** | **6.7** | |

**Modernization Notes:**
- Assembles data from 4 VSAM files (Account, Card, Xref, Customer) → single SQL JOIN or aggregate query
- 941 lines for a read-only view suggests screen formatting dominance → modern UI framework handles this
- HANDLE ABEND pattern is defensive → global exception handler in Spring
- 15 copybook dependencies show tight data coupling → DTO/ViewModel pattern

---

## Summary Rankings

| Rank | Module | Type | Lines | Complexity | Risk | Impact | **Composite** | Primary Concern |
|------|--------|------|-------|------------|------|--------|---------------|-----------------|
| 1 | **COACTUPC** | Online | 4,236 | 10 | 9 | 9 | **9.4** | Largest program, 164 IFs, financial data mutation |
| 2 | **CBTRN02C** | Batch | 731 | 8 | 10 | 10 | **9.3** | Core transaction posting, data integrity |
| 3 | **CBACT04C** | Batch | 652 | 8 | 10 | 9 | **9.0** | Interest calculation, regulatory compliance |
| 4 | **COCRDLIC** | Online | 1,459 | 8 | 7 | 8 | **7.7** | Complex pagination, navigation coupling |
| 5 | **COCRDUPC** | Online | 1,560 | 8 | 8 | 7 | **7.7** | Card data mutation, validation complexity |
| 6 | **CBSTM03A** | Batch | 924 | 8 | 7 | 8 | **7.7** | Dual-format output, subroutine calls |
| 7 | **CBTRN03C** | Batch | 649 | 7 | 6 | 8 | **7.0** | 72 PERFORMs, multi-file report |
| 8 | **COTRN02C** | Online | 783 | 7 | 8 | 7 | **7.3** | 13 EVALUATEs, transaction creation |
| 9 | **COBIL00C** | Online | 572 | 6 | 9 | 8 | **7.6** | Payment processing, balance mutation |
| 10 | **COACTVWC** | Online | 941 | 7 | 5 | 8 | **6.7** | 4-file data assembly, most copybooks |

---

## Modernization Priority Recommendations

### Phase 1 - High Risk / High Impact (Immediate)
1. **CBTRN02C** (Transaction Posting) - Establish exact-match regression tests first
2. **CBACT04C** (Interest Calculation) - Requires precision arithmetic validation
3. **COACTUPC** (Account Update) - Decompose into smaller services

### Phase 2 - Core Online Functions
4. **COBIL00C** (Bill Payment) - Needs atomic transaction semantics
5. **COTRN02C** (Transaction Add) - State machine refactoring
6. **COCRDUPC** (Card Update) - Validation rule extraction

### Phase 3 - Browse / View / Report
7. **COCRDLIC** (Card List) - Pagination modernization
8. **COACTVWC** (Account View) - SQL JOIN consolidation
9. **CBSTM03A** (Statement Gen) - Template engine migration
10. **CBTRN03C** (Transaction Report) - Report framework migration

### Cross-Cutting Concerns
- **COCOM01Y** (COMMAREA) → Session state / JWT tokens
- **CSUTLDTC** (Date utility) → java.time API
- **CSSETATY** (Screen attributes) → CSS / UI framework
- **BMS maps** → HTML/React/Angular forms
- **VSAM files** → Relational database (PostgreSQL/Oracle)
- **JCL batch cycle** → Spring Batch / scheduled tasks
- **GDG datasets** → Versioned file storage or database audit tables
