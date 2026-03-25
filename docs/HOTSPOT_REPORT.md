# CardDemo Hotspot Report

> **Generated for**: Mainframe-to-Java Modernization Assessment
> **Application**: CardDemo - Credit Card Management System
> **Purpose**: Identify the top 10 highest-risk, highest-complexity modules to prioritize modernization effort

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Rankings](#top-10-hotspot-rankings)
3. [Detailed Module Assessments](#detailed-module-assessments)
4. [Risk Heat Map](#risk-heat-map)
5. [Recommended Modernization Sequence](#recommended-modernization-sequence)
6. [Complexity Metrics Summary (All Programs)](#complexity-metrics-summary-all-programs)

---

## Scoring Methodology

Each module is scored across three dimensions on a 1–10 scale:

| Dimension | Weight | Factors Considered |
|-----------|--------|-------------------|
| **Complexity** | 35% | Lines of code, PERFORM count, EVALUATE/IF branching depth, number of copybooks included, CICS command count, number of VSAM files accessed, CALL dependencies |
| **Risk** | 35% | Data sensitivity (PII/financial), write operations to critical files, error handling patterns, ABEND handling, cross-program dependencies, state management complexity |
| **Business Impact** | 30% | Revenue criticality, user-facing frequency, data integrity role, downstream dependencies, regulatory compliance implications |

**Composite Score** = (Complexity × 0.35) + (Risk × 0.35) + (Business Impact × 0.30)

---

## Top 10 Hotspot Rankings

| Rank | Program | Type | LOC | Composite Score | Complexity | Risk | Business Impact | Primary Concern |
|------|---------|------|-----|:-:|:-:|:-:|:-:|-----------------|
| **1** | **COACTUPC** | Online CICS | 4,236 | **9.4** | 10 | 9 | 9 | Largest program; updates accounts + customers; 36 copybooks; complex validation |
| **2** | **CBTRN02C** | Batch | 731 | **8.8** | 8 | 10 | 9 | Core transaction posting; updates 4 VSAM files; financial integrity critical |
| **3** | **COCRDUPC** | Online CICS | 1,560 | **8.3** | 9 | 8 | 8 | Card update with sensitive data (CVV); ABEND handling; complex validation |
| **4** | **COCRDLIC** | Online CICS | 1,459 | **7.9** | 9 | 7 | 8 | Card listing with pagination; 3 XCTL targets; complex browse logic |
| **5** | **CBACT04C** | Batch | 652 | **7.8** | 7 | 9 | 8 | Interest calculation; reads disclosure rates; creates financial transactions |
| **6** | **CBSTM03A** | Batch | 924 | **7.5** | 8 | 7 | 8 | Statement generation; ALTER GOTO; calls subroutine 10×; dual output (text+HTML) |
| **7** | **COACTVWC** | Online CICS | 941 | **7.2** | 7 | 7 | 8 | Account view; reads 3 VSAM files; ABEND handling; gateway to update |
| **8** | **COTRN02C** | Online CICS | 783 | **7.1** | 7 | 8 | 7 | Add transaction online; writes to TRANSACT; date validation via CALL |
| **9** | **CBTRN03C** | Batch | 649 | **6.8** | 7 | 6 | 8 | Daily transaction report; reads 4 reference files; complex formatting |
| **10** | **COTRN00C** | Online CICS | 699 | **6.5** | 7 | 6 | 7 | Transaction list with browse/pagination; STARTBR/READNEXT/READPREV logic |

---

## Detailed Module Assessments

### #1 — COACTUPC (Account Update) — Score: 9.4

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 4,236 | Largest program in the entire codebase (24% of all online LOC) |
| **PERFORM Statements** | 64 | High procedural complexity |
| **EVALUATE Blocks** | 20 | Extensive multi-way branching |
| **IF Statements** | 168 | Extremely high conditional density |
| **Copybooks Included** | 18 unique + 36 CSSETATY replacements | Most copybook dependencies in the system |
| **CICS Commands** | 15+ | READ (5×), REWRITE (2×), SEND, RECEIVE, XCTL, HANDLE ABEND |
| **VSAM Files Accessed** | 3 (ACCTDATA, CUSTDATA, CARDXREF) | Multi-file update = transaction consistency risk |
| **CALLs** | None (but includes CSSTRPFY, CSUTLDPY inline) | Complex inline utilities |

**Why It's #1**:
- **Size**: At 4,236 lines, it is 2.7× larger than the next biggest online program. This alone makes it the hardest to convert.
- **Data Scope**: Updates both account AND customer records in a single transaction — requires careful transaction boundary design in Java.
- **Validation Complexity**: 168 IF statements handle field-level validation for 30+ screen fields including account status, credit limits, customer addresses, and dates.
- **UI Complexity**: Uses 36 COPY CSSETATY REPLACING statements for dynamic screen attribute control — each must map to frontend validation/styling logic.
- **ABEND Handling**: Has explicit HANDLE ABEND and ABEND paragraphs — error recovery must be carefully mapped to Java exception handling.

**Modernization Risks**:
- Multi-file REWRITE without CICS unit-of-work → needs `@Transactional` in Java
- Screen attribute manipulation → complex frontend state management
- Inline string processing (CSSTRPFY) → Java String utilities
- Date utility (CSUTLDPY) → Java `java.time` API

**Recommended Approach**: Break into multiple Java service classes: `AccountUpdateService`, `CustomerUpdateService`, `AccountUpdateValidator`. Map BMS screen to a multi-tab web form.

---

### #2 — CBTRN02C (Transaction Posting) — Score: 8.8

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 731 | Medium-large batch program |
| **PERFORM Statements** | 61 | High procedural complexity |
| **IF Statements** | 48 | Significant branching |
| **Files Accessed** | 6 (DALYTRAN, TRANSACT, CARDXREF, ACCTDATA, TCATBAL, DALYREJS) | Most file dependencies of any batch program |
| **File Operations** | READ, WRITE, REWRITE across multiple files | Multi-file update integrity |

**Why It's #2**:
- **Financial Core**: This is THE program that posts transactions. Every dollar that moves through CardDemo flows through CBTRN02C.
- **Multi-File Updates**: Reads daily transactions, validates via cross-reference, writes to master TRANSACT, updates account balances in ACCTDATA, updates category balances in TCATBAL, and writes rejects to DALYREJS.
- **Data Integrity**: A bug here means incorrect balances, lost transactions, or phantom charges.
- **No Rollback**: Batch COBOL has no built-in transaction rollback — if it fails mid-run, partial updates exist. Java conversion needs `@Transactional` with proper rollback.

**Modernization Risks**:
- Sequential file processing model → Spring Batch with chunk-oriented processing
- Implicit commit-per-record → need explicit transaction boundaries
- Cross-reference validation → JOIN queries in SQL
- Reject file handling → error queue or dead-letter table

**Recommended Approach**: Convert to Spring Batch job with `ItemReader` (daily transactions), `ItemProcessor` (validation + calculation), `ItemWriter` (multi-table update). Use database transactions for atomicity.

---

### #3 — COCRDUPC (Card Update) — Score: 8.3

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 1,560 | Second-largest online program |
| **PERFORM Statements** | 26 | Moderate |
| **EVALUATE Blocks** | 16 | Multi-way branching |
| **IF Statements** | 74 | High conditional density |
| **CICS Commands** | 10+ | READ (2×), REWRITE, SEND, RECEIVE, HANDLE ABEND, ABEND |
| **Sensitive Data** | CVV code, card number, expiration date | PCI-DSS compliance required |

**Why It's #3**:
- **PCI-DSS Sensitivity**: Handles card numbers, CVV codes, and expiration dates. The modernized version MUST implement tokenization and encryption.
- **ABEND Handling**: Includes both HANDLE ABEND and explicit ABEND paragraphs — complex error scenarios.
- **Inline String Processing**: Includes CSSTRPFY for card number formatting/validation.
- **REWRITE Complexity**: Updates card records with validation against account and customer data.

**Modernization Risks**:
- Card data handling → PCI-DSS compliant tokenization service
- Plaintext CVV in VSAM → must never be stored in modernized system
- ABEND handling → Java exception hierarchy with proper logging

**Recommended Approach**: Implement as `CardUpdateService` with `@Transactional`. Integrate with a card tokenization service. Never expose CVV beyond initial validation.

---

### #4 — COCRDLIC (Card Listing) — Score: 7.9

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 1,459 | Third-largest online program |
| **PERFORM Statements** | 34 | Moderate-high |
| **EVALUATE Blocks** | 18 | Extensive navigation logic |
| **IF Statements** | 61 | Heavy conditional logic |
| **CICS Browse Ops** | STARTBR, READNEXT (2×), READPREV (2×), ENDBR (2×) | Complex pagination |
| **XCTL Targets** | 3 (COCRDSLC, COCRDUPC, COMEN01C) | Hub program |

**Why It's #4**:
- **Pagination Complexity**: Implements forward/backward scrolling through VSAM records using CICS browse — this is the most complex browse pattern in the system.
- **Hub Role**: Acts as navigation hub to card view and card update programs.
- **Multiple Browse Sessions**: Manages browse with direction reversal (READNEXT → READPREV on page up).

**Modernization Risks**:
- CICS STARTBR/READNEXT/READPREV → JPA `Pageable` with cursor-based pagination
- VSAM key-range browsing → SQL `WHERE` with indexed columns
- Stateful browse position → stateless REST with offset/cursor parameters

**Recommended Approach**: REST endpoint with paginated query (`/api/cards?accountId=X&page=0&size=10`). Use Spring Data JPA `Pageable`.

---

### #5 — CBACT04C (Interest Calculation) — Score: 7.8

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 652 | Medium batch program |
| **PERFORM Statements** | 56 | High for its size |
| **IF Statements** | 43 | Complex business rules |
| **Files Accessed** | 5 (TCATBAL, CARDXREF, ACCTDATA, DISCGRP, TRANSACT) | Reads 4, writes 2 |
| **Business Logic** | Interest rate lookup, compound calculation, balance updates | Core financial algorithm |

**Why It's #5**:
- **Financial Algorithm**: Implements the interest calculation engine. Reads disclosure group rates (DISCGRP), applies them to category balances (TCATBAL), creates interest charge transactions in TRANSACT, and updates account balances.
- **Regulatory Risk**: Interest calculation errors have compliance and legal implications.
- **Date Sensitivity**: Takes a date parameter (PARM='2022071800') for calculation cutoff — must handle month boundaries, leap years, etc.

**Modernization Risks**:
- Fixed-point COBOL arithmetic (PIC S9(09)V99) → Java `BigDecimal` with `RoundingMode.HALF_UP`
- Compound interest logic → must validate identical results between COBOL and Java
- Disclosure rate lookup → database JOIN or in-memory cache

**Recommended Approach**: Implement as `InterestCalculationService` using `BigDecimal` throughout. Create exhaustive parallel-run tests comparing COBOL and Java output for identical inputs.

---

### #6 — CBSTM03A (Statement Generation) — Score: 7.5

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 924 | Large batch program |
| **PERFORM Statements** | 29 | Moderate |
| **EVALUATE Blocks** | 9 | Multi-way control flow |
| **IF Statements** | 15 | Relatively low for its size |
| **CALL to CBSTM03B** | 10 calls | Heavy subroutine usage |
| **Output Formats** | 2 (plain text + HTML) | Dual output complexity |
| **Special Constructs** | ALTER GOTO | Legacy flow control — hardest to convert |

**Why It's #6**:
- **ALTER GOTO**: Uses the `ALTER` statement to modify GOTO targets at runtime — one of the most difficult COBOL constructs to convert to structured Java code.
- **Dual Output**: Generates both plain text and HTML statements simultaneously, writing to two output files in parallel.
- **Subroutine Coupling**: Calls CBSTM03B 10 times for various file I/O operations (open, read, write, close) with a control flag parameter.
- **Multi-File Reads**: Joins data from TRANSACT, CARDXREF, CUSTDATA, and ACCTDATA to build complete statements.

**Modernization Risks**:
- `ALTER GOTO` → requires control flow refactoring; use state machine or strategy pattern
- Dual output format → template engine (Thymeleaf for HTML, text template for plain)
- CBSTM03B coupling → inline into main class or use Repository pattern
- Fixed-format text layout → maintain for legacy compatibility, add PDF option

**Recommended Approach**: Spring Batch job with `FlatFileItemWriter` (text) and template-based HTML. Replace ALTER GOTO with state enum. Merge CBSTM03B logic into the main service.

---

### #7 — COACTVWC (Account View) — Score: 7.2

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 941 | Medium-large online program |
| **PERFORM Statements** | 21 | Moderate |
| **EVALUATE Blocks** | 10 | Standard navigation |
| **IF Statements** | 29 | Moderate |
| **CICS Commands** | 10+ | READ (3×), SEND, RECEIVE, XCTL, HANDLE ABEND, ABEND |
| **VSAM Files Read** | 3 (ACCTDATA, CARDXREF, CUSTDATA) | Multi-entity display |

**Why It's #7**:
- **Gateway Program**: Account view is the entry point to the most complex program (COACTUPC). Bugs here affect the entire account management flow.
- **Multi-Entity JOIN**: Reads from 3 VSAM files to compose a single view — equivalent to a 3-table SQL JOIN.
- **ABEND Handling**: Full HANDLE ABEND / explicit ABEND paragraphs like COACTUPC.

**Modernization Risks**:
- 3-file read composition → single JPA query with eager/lazy fetch strategy
- Display-only but with XCTL to update → REST GET endpoint with HATEOAS links to update
- ABEND handling → global `@ExceptionHandler` in Spring

**Recommended Approach**: `AccountViewController` REST endpoint returning DTO that combines Account, Customer, and Card data. Use `@Query` with JOINs.

---

### #8 — COTRN02C (Transaction Add Online) — Score: 7.1

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 783 | Medium online program |
| **PERFORM Statements** | 61 | High for its size |
| **EVALUATE Blocks** | 26 | Most EVALUATE blocks of any program |
| **CICS Commands** | 10+ | READ (2×), WRITE, STARTBR, READPREV, ENDBR, SEND, RECEIVE |
| **CALL** | CSUTLDTC (2×) | Date validation dependency |
| **VSAM Writes** | TRANSACT (new transaction) | Financial write operation |

**Why It's #8**:
- **Financial Write**: This is the online counterpart to CBTRN02C — it adds transactions directly from the user interface.
- **Complex Validation**: 26 EVALUATE blocks handle input validation for transaction type, category, amount, date, and merchant fields.
- **ID Generation**: Uses STARTBR/READPREV to find the last transaction ID and generate the next one — custom sequence generation.
- **Date Validation**: Calls CSUTLDTC twice to validate origination and processing dates.

**Modernization Risks**:
- VSAM-based ID generation → database sequence or UUID
- Date validation via CALL → `java.time.LocalDate.parse()` with `DateTimeParseException`
- 26 EVALUATE blocks → Java validation framework (`@Valid` / `@Validated`)

**Recommended Approach**: `TransactionService.addTransaction()` with `@Valid` DTO. Use database auto-increment or UUID for IDs. Input validation via Bean Validation annotations.

---

### #9 — CBTRN03C (Transaction Report) — Score: 6.8

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 649 | Medium batch program |
| **PERFORM Statements** | 72 | Highest PERFORM count of any program |
| **EVALUATE Blocks** | 4 | Moderate |
| **IF Statements** | 38 | Moderate-high |
| **Files Accessed** | 5 (TRANFILE, CARDXREF, TRANTYPE, TRANCATG, TRANREPT) | Read 4, write 1 |
| **Report Structure** | CVTRA07Y | Complex formatted output |

**Why It's #9**:
- **Highest PERFORM Count**: 72 PERFORMs indicate deeply modular code with many small paragraphs — hardest to trace execution flow.
- **Multi-File Lookups**: For each transaction, looks up type description (TRANTYPE), category description (TRANCATG), and account via cross-reference (CARDXREF).
- **Formatted Output**: Produces page headers, detail lines, page totals, account totals, and grand totals — classic control-break report logic.

**Modernization Risks**:
- Control-break reporting logic → Spring Batch with custom `ItemWriter` and aggregation
- 4-file lookup per transaction → SQL JOINs in a single query
- Fixed-format report → JasperReports or similar report engine

**Recommended Approach**: Spring Batch job with multi-table query. Use JasperReports for formatted output. Maintain text format for backward compatibility.

---

### #10 — COTRN00C (Transaction List) — Score: 6.5

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 699 | Medium online program |
| **PERFORM Statements** | 43 | Moderate-high |
| **EVALUATE Blocks** | 16 | Multi-way navigation |
| **IF Statements** | 26 | Moderate |
| **CICS Browse** | STARTBR, READNEXT, READPREV, ENDBR | Pagination complexity |
| **XCTL Targets** | 2 (COTRN01C, COTRN02C) | Navigation hub |

**Why It's #10**:
- **Pagination Pattern**: Same CICS browse pagination as COCRDLIC (#4) but for the transaction file, which is the largest VSAM dataset.
- **Performance Sensitivity**: Browsing the full TRANSACT file can be slow on large datasets — the modernized version needs proper indexing and pagination.
- **Hub Role**: Routes to transaction view and transaction add — central to the transaction management flow.

**Modernization Risks**:
- Full VSAM browse on large file → must use database indexes and LIMIT/OFFSET
- CICS stateful browse → stateless REST pagination with keyset/cursor
- Performance on transaction table (largest dataset) → query optimization critical

**Recommended Approach**: REST endpoint with cursor-based pagination. Create database indexes on transaction date and card number. Use `@Query` with optimized WHERE clauses.

---

## Risk Heat Map

```
                    LOW Business Impact    MEDIUM Business Impact    HIGH Business Impact
                  ┌──────────────────────┬──────────────────────┬──────────────────────┐
 HIGH Complexity  │                      │ COCRDLIC (#4)        │ COACTUPC (#1)        │
                  │                      │ CBSTM03A (#6)        │ COCRDUPC (#3)        │
                  ├──────────────────────┼──────────────────────┼──────────────────────┤
 MED Complexity   │ CBACT01C             │ COTRN00C (#10)       │ CBTRN02C (#2)        │
                  │ CBACT02C             │ COACTVWC (#7)        │ CBACT04C (#5)        │
                  │ CBACT03C             │ CBTRN03C (#9)        │ COTRN02C (#8)        │
                  ├──────────────────────┼──────────────────────┼──────────────────────┤
 LOW Complexity   │ COBSWAIT             │ COUSR01C             │ COSGN00C             │
                  │ UNUSED1Y             │ COUSR02C             │ COBIL00C             │
                  │ CSUTLDTC             │ COUSR03C             │ COMEN01C             │
                  │ CBCUS01C             │ COTRN01C             │                      │
                  │ CBSTM03B             │ COUSR00C             │                      │
                  │ CBIMPORT             │ CORPT00C             │                      │
                  │ CBEXPORT             │ COADM01C             │                      │
                  │ CBTRN01C             │ COCRDSLC             │                      │
                  └──────────────────────┴──────────────────────┴──────────────────────┘

  ■ Top-right quadrant = Highest priority for modernization attention
  ■ Bottom-left quadrant = Can be converted with standard patterns
```

---

## Recommended Modernization Sequence

Based on the hotspot analysis, here is the recommended conversion order organized into waves:

### Wave 1: Foundation & High-Risk Batch (Weeks 1-4)
| Order | Module | Rationale |
|-------|--------|-----------|
| 1.1 | **Data Layer** (all copybooks → JPA entities) | Every program depends on data structures; convert these first |
| 1.2 | **CSUTLDTC** (Date utility) | Shared dependency; trivial to replace with `java.time` |
| 1.3 | **CBTRN02C** (#2 - Transaction Posting) | Core financial integrity; needs thorough parallel testing |
| 1.4 | **CBACT04C** (#5 - Interest Calculation) | Financial calculations; parallel-run validation essential |

### Wave 2: Core Online Programs (Weeks 5-8)
| Order | Module | Rationale |
|-------|--------|-----------|
| 2.1 | **COSGN00C** (Sign-on) | Entry point; enables all other online testing |
| 2.2 | **COMEN01C + COADM01C** (Menus) | Navigation infrastructure |
| 2.3 | **COACTUPC** (#1 - Account Update) | Highest complexity; longest lead time; start early |
| 2.4 | **COACTVWC** (#7 - Account View) | Paired with Account Update |

### Wave 3: Card & Transaction Online (Weeks 9-12)
| Order | Module | Rationale |
|-------|--------|-----------|
| 3.1 | **COCRDLIC** (#4 - Card List) | Pagination pattern reusable for other list screens |
| 3.2 | **COCRDSLC + COCRDUPC** (#3 - Card View/Update) | Sensitive data; PCI-DSS design needed |
| 3.3 | **COTRN00C** (#10) + **COTRN01C** + **COTRN02C** (#8) | Transaction management suite |
| 3.4 | **COBIL00C** (Bill Payment) | Financial write; test after transaction infrastructure |

### Wave 4: Reporting & Remaining (Weeks 13-16)
| Order | Module | Rationale |
|-------|--------|-----------|
| 4.1 | **CBSTM03A/B** (#6 - Statements) | Complex but isolated; ALTER GOTO refactoring |
| 4.2 | **CBTRN03C** (#9 - Transaction Report) | Report engine replacement |
| 4.3 | **CORPT00C** (Online Report Request) | Simple after batch report exists |
| 4.4 | **COUSR00C-03C** (User Admin suite) | Straightforward CRUD; low risk |
| 4.5 | **CBEXPORT/CBIMPORT + utilities** | Data migration tools; convert last |

### Wave 5: Optional Modules (If In Scope)
| Module | Programs | Complexity |
|--------|----------|------------|
| Auth IMS/DB2/MQ | 8 programs | HIGH — requires IMS/DB2/MQ replacement design |
| Tran Type DB2 | 3 programs | MEDIUM — DB2 SQL maps directly to JPA |
| VSAM-MQ | 2 programs | MEDIUM — MQ → Spring JMS/Kafka |

---

## Complexity Metrics Summary (All Programs)

| Program | LOC | PERFORMs | EVALUATEs | IFs | COPY Stmts | CICS Cmds | CALLs | Files | Composite |
|---------|-----|----------|-----------|-----|------------|-----------|-------|-------|-----------|
| COACTUPC | 4,236 | 64 | 20 | 168 | 18+ | 15+ | 0 | 3 | **9.4** |
| CBTRN02C | 731 | 61 | 0 | 48 | 2 | 0 | 1 | 6 | **8.8** |
| COCRDUPC | 1,560 | 26 | 16 | 74 | 10 | 10+ | 0 | 2 | **8.3** |
| COCRDLIC | 1,459 | 34 | 18 | 61 | 8 | 12+ | 0 | 1 | **7.9** |
| CBACT04C | 652 | 56 | 0 | 43 | 2 | 0 | 1 | 5 | **7.8** |
| CBSTM03A | 924 | 29 | 9 | 15 | 4 | 0 | 11 | 4 | **7.5** |
| COACTVWC | 941 | 21 | 10 | 29 | 8 | 10+ | 0 | 3 | **7.2** |
| COTRN02C | 783 | 61 | 26 | 14 | 8 | 10+ | 2 | 3 | **7.1** |
| CBTRN03C | 649 | 72 | 4 | 38 | 5 | 0 | 1 | 5 | **6.8** |
| COTRN00C | 699 | 43 | 16 | 26 | 6 | 8 | 0 | 1 | **6.5** |
| COCRDSLC | 887 | 19 | 8 | 33 | 7 | 10+ | 0 | 2 | 6.3 |
| COUSR00C | 695 | 41 | 16 | 25 | 6 | 8 | 0 | 1 | 5.9 |
| CORPT00C | 649 | 34 | 10 | 20 | 6 | 7 | 2 | 0 | 5.8 |
| COBIL00C | 572 | 38 | 18 | 10 | 6 | 12 | 0 | 3 | 5.7 |
| CBEXPORT | 582 | 45 | 0 | 16 | 6 | 0 | 1 | 6 | 5.5 |
| CBTRN01C | 494 | 42 | 0 | 33 | 2 | 0 | 1 | 6 | 5.3 |
| CBIMPORT | 487 | 29 | 2 | 14 | 1 | 0 | 1 | 6 | 5.1 |
| CBACT01C | 430 | 35 | 0 | 22 | 1 | 0 | 2 | 1 | 4.8 |
| COUSR02C | 414 | 31 | 10 | 13 | 6 | 6 | 0 | 1 | 4.5 |
| COUSR03C | 359 | 26 | 10 | 8 | 6 | 5 | 0 | 1 | 4.3 |
| COTRN01C | 330 | 17 | 6 | 7 | 6 | 5 | 0 | 1 | 4.1 |
| COMEN01C | 308 | 13 | 6 | 7 | 8 | 7 | 0 | 0 | 4.0 |
| COUSR01C | 299 | 20 | 6 | 4 | 6 | 5 | 0 | 1 | 3.8 |
| COADM01C | 288 | 14 | 4 | 6 | 5 | 6 | 0 | 0 | 3.7 |
| COSGN00C | 260 | 11 | 6 | 4 | 4 | 8 | 0 | 1 | 3.6 |
| CBSTM03B | 230 | 4 | 1 | 12 | 0 | 0 | 0 | 4 | 3.5 |
| CBACT02C | 178 | 10 | 0 | 11 | 1 | 0 | 1 | 1 | 3.0 |
| CBACT03C | 178 | 10 | 0 | 11 | 1 | 0 | 1 | 1 | 3.0 |
| CBCUS01C | 178 | 10 | 0 | 11 | 1 | 0 | 1 | 1 | 3.0 |
| CSUTLDTC | 157 | 1 | 2 | 0 | 0 | 0 | 1 | 0 | 2.5 |
| COBSWAIT | 41 | 0 | 0 | 0 | 0 | 0 | 1 | 0 | 1.0 |
