# CardDemo Hotspot Report

> **Purpose:** Identify the top 10 modules by complexity, risk, and business impact for modernization prioritization
> **Methodology:** Static analysis of LOC, cyclomatic indicators (PERFORM/EVALUATE/IF nesting), CICS coupling, file I/O breadth, copybook fan-in, and business criticality

---

## Scoring Methodology

Each module is scored on three axes (1-10 scale):

| Axis | Weight | Factors |
|------|-------:|---------|
| **Complexity** | 40% | Lines of code, number of PERFORMs, EVALUATE/IF depth, number of copybooks, number of CICS commands, CALL dependencies |
| **Risk** | 30% | Data mutation scope (WRITE/REWRITE/DELETE), number of VSAM files accessed, error handling quality, security sensitivity, coupling to other programs |
| **Business Impact** | 30% | Revenue criticality, user-facing frequency, downstream dependencies, data volume processed, regulatory relevance |

**Composite Score** = (Complexity x 0.4) + (Risk x 0.3) + (Business Impact x 0.3)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC.cbl -- Account Update

| Metric | Value | Notes |
|--------|------:|-------|
| **Lines of Code** | 4,236 | Largest program in the entire codebase (20% of all COBOL LOC) |
| **PERFORM Count** | ~85 | Deeply nested validation logic |
| **COPY Statements** | 12 | CVACT01Y, CVACT03Y, CVCUS01Y, CVCRD01Y, CSLKPCDY, COCOM01Y, COACTUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y |
| **EXEC CICS Commands** | ~20 | READ, REWRITE, SEND MAP, RECEIVE MAP, HANDLE ABEND, RETURN |
| **VSAM Files Accessed** | 4 | ACCTFILE (R/W), CARDXREF (R), CUSTFILE (R), CARDFILE (R) |
| **Complexity Score** | **10** | Extreme: 60+ field-level validations (SSN, phone, state, zip, FICO, dates, amounts), multiple REDEFINES |
| **Risk Score** | **9** | Writes to ACCTFILE (financial data), handles PII (SSN, DOB), complex edit-compare logic |
| **Business Impact Score** | **9** | Core account maintenance; balance/credit limit changes affect all downstream processing |
| **Composite Score** | **9.4** |

**Modernization Challenges:**
- Massive monolithic program -- should be decomposed into 5+ microservices (account edit, customer edit, card management, validation, persistence)
- Embedded validation rules (CSLKPCDY has 800+ phone area codes, 50+ state codes, 100+ state-zip combos) need externalization
- Contains inline `CSUTLDWY` date utility working storage
- Complex CICS pseudo-conversational flow with multi-step confirmation
- Field-level attribute manipulation for 3270 screen highlights

---

### Rank 2: CBTRN02C.cbl -- Transaction Posting

| Metric | Value | Notes |
|--------|------:|-------|
| **Lines of Code** | 731 | Core batch posting engine |
| **PERFORM Count** | ~35 | Sequential processing with branching |
| **COPY Statements** | 5 | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |
| **VSAM Files Accessed** | 6 | DALYTRAN (R), TRANSACT (W), CARDXREF (R), DALYREJS (W), ACCTFILE (R/W), TCATBALF (R/W) |
| **Complexity Score** | **8** | Multi-file I-O with complex validation and cross-referencing |
| **Risk Score** | **10** | Writes to 4 files simultaneously; financial posting -- errors cause balance discrepancies |
| **Business Impact Score** | **10** | THE core batch process; all daily transactions flow through this program |
| **Composite Score** | **9.2** |

**Modernization Challenges:**
- Transactional integrity across 4 output files (no built-in 2-phase commit in VSAM)
- Reject handling logic creates separate output stream
- Category balance maintenance (TCATBALF) is tightly coupled
- Must maintain exact decimal precision for financial calculations
- Timestamp generation logic (`Z-GET-DB2-FORMAT-TIMESTAMP`) embedded inline

---

### Rank 3: CBACT04C.cbl -- Interest Calculation

| Metric | Value | Notes |
|--------|------:|-------|
| **Lines of Code** | 652 | Financial calculation engine |
| **PERFORM Count** | ~30 | Iterative processing with nested lookups |
| **COPY Statements** | 5 | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |
| **VSAM Files Accessed** | 5 | TCATBALF (R), CARDXREF (R), DISCGRP (R), ACCTFILE (R/W), TRANSACT (W) |
| **Complexity Score** | **8** | Multi-tier interest rate lookup, fee computation, account updates |
| **Risk Score** | **9** | Financial calculations -- rounding errors or rate misapplication cause monetary loss |
| **Business Impact Score** | **9** | Revenue-generating: interest and fees are the business's income stream |
| **Composite Score** | **8.7** |

**Modernization Challenges:**
- Interest rate tier lookup (DISCGRP) uses composite key (group + type + category)
- Default interest rate fallback logic when disclosure group not found
- `COMPUTE` statements with `ROUNDED` -- must preserve exact COBOL decimal arithmetic behavior
- Writes interest charge as new transaction to TRANSACT file
- Account balance update must match posting program's arithmetic exactly

---

### Rank 4: CBSTM03A.CBL -- Statement Generation (Driver)

| Metric | Value | Notes |
|--------|------:|-------|
| **Lines of Code** | 924 | Statement generation with HTML output |
| **PERFORM Count** | ~40 | Complex report formatting with nested loops |
| **COPY Statements** | 3 | COSTM01, CVACT03Y, CUSTREC, CVACT01Y |
| **CALL Targets** | 1 | CBSTM03B (file I/O sub-program) |
| **Files Accessed** | 6+ | TRANSACT, CARDXREF, CUSTFILE, ACCTFILE (via CBSTM03B), STMT-FILE (W), HTML-FILE (W) |
| **Complexity Score** | **8** | Dual output (text + HTML), complex formatting, `ALTER` statement usage |
| **Risk Score** | **7** | Read-only on master files but statement errors affect customer communications |
| **Business Impact Score** | **8** | Customer-facing deliverable; regulatory requirement for periodic statements |
| **Composite Score** | **7.7** |

**Modernization Challenges:**
- Uses `ALTER` statement (deprecated COBOL feature) to switch file processing modes
- Dual output format (text + HTML) with inline HTML generation
- Calls CBSTM03B as sub-program with shared working storage area
- Complex report pagination logic
- TIOT (Task I/O Table) processing for dynamic file allocation

---

### Rank 5: COCRDUPC.cbl -- Credit Card Update

| Metric | Value | Notes |
|--------|------:|-------|
| **Lines of Code** | 1,560 | Card maintenance with full edit/confirm/write cycle |
| **PERFORM Count** | ~50 | Multi-phase: display, edit, validate, compare, write |
| **COPY Statements** | 10 | CVCRD01Y, COCOM01Y, COCRDUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y |
| **EXEC CICS Commands** | ~15 | READ, REWRITE, SEND MAP, RECEIVE MAP |
| **VSAM Files Accessed** | 3 | CARDFILE (R/W), CUSTFILE (R), (CARDXREF via COACTVWC) |
| **Complexity Score** | **8** | Change detection (old vs new), multi-field validation, CICS pseudo-conversational |
| **Risk Score** | **7** | Card data mutations affect transaction routing |
| **Business Impact Score** | **7** | Card status changes (activate/deactivate) directly impact cardholder operations |
| **Composite Score** | **7.3** |

**Modernization Challenges:**
- Optimistic locking pattern (read-compare-rewrite) needs careful translation to DB locking
- Card status change triggers need to be event-driven in modern architecture
- Expiry date validation with month/year components
- Copy of CSSTRPFY inline for PF-key string processing

---

### Rank 6: COCRDLIC.cbl -- Credit Card List/Search

| Metric | Value | Notes |
|--------|------:|-------|
| **Lines of Code** | 1,459 | Complex browse/search with array display |
| **PERFORM Count** | ~40 | Forward/backward paging with filter logic |
| **COPY Statements** | 9 | CVCRD01Y, COCOM01Y, COCRDLI, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, DFHBMSCA |
| **EXEC CICS Commands** | ~15 | STARTBR, READNEXT, READPREV, ENDBR, SEND MAP, RECEIVE MAP, XCTL |
| **Complexity Score** | **7** | Bi-directional browsing, record filtering, array population |
| **Risk Score** | **5** | Read-only; main risk is incorrect filtering hiding valid records |
| **Business Impact Score** | **7** | Primary card discovery interface; gateway to card view/update |
| **Composite Score** | **6.4** |

**Modernization Challenges:**
- CICS browse (STARTBR/READNEXT/READPREV/ENDBR) maps to paginated REST API or cursor-based query
- 7-row display array with per-cell attribute management
- Context passed via COMMAREA for drill-down to COCRDSLC/COCRDUPC
- Filter logic operates at VSAM browse level (no SQL WHERE equivalent)

---

### Rank 7: CBTRN03C.cbl -- Transaction Report

| Metric | Value | Notes |
|--------|------:|-------|
| **Lines of Code** | 649 | Daily transaction report generator |
| **PERFORM Count** | ~30 | Control-break reporting with page/account/grand totals |
| **COPY Statements** | 5 | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| **Files Accessed** | 6 | TRANSACT (R), CARDXREF (R), TRANTYPE (R), TRANCATG (R), DATEPARM (R), REPTFILE (W) |
| **Complexity Score** | **7** | Multi-level control breaks, running totals, formatted output |
| **Risk Score** | **5** | Read-only on master files; report errors visible but non-destructive |
| **Business Impact Score** | **8** | Daily operational report; basis for reconciliation and audit |
| **Composite Score** | **6.6** |

**Modernization Challenges:**
- Control-break reporting pattern (account changes trigger subtotals) maps to SQL GROUP BY with rollup
- Date range parameter file input
- Report header/detail/footer formatting with fixed-width columns
- Cross-reference lookups for type/category descriptions
- Page overflow logic with header reprinting

---

### Rank 8: COBIL00C.cbl -- Bill Payment

| Metric | Value | Notes |
|--------|------:|-------|
| **Lines of Code** | 572 | Online bill payment with real-time posting |
| **PERFORM Count** | ~25 | Validate, lookup, write transaction, update balance |
| **COPY Statements** | 8 | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y |
| **EXEC CICS Commands** | ~15 | READ, REWRITE, WRITE, STARTBR, READPREV, ENDBR, ASKTIME, FORMATTIME |
| **VSAM Files Accessed** | 3 | ACCTFILE (R/W), CARDXREF (R), TRANSACT (R/W) |
| **Complexity Score** | **6** | Moderate logic, but combines inquiry + transaction in one screen |
| **Risk Score** | **9** | Real-time financial write; payment errors directly impact customer balance |
| **Business Impact Score** | **8** | Customer-facing payment channel; revenue collection |
| **Composite Score** | **7.5** |

**Modernization Challenges:**
- Real-time balance update (read-compute-rewrite) requires transactional integrity
- Transaction ID generation (READPREV to find last, increment) needs sequence/UUID replacement
- CICS ASKTIME/FORMATTIME for timestamps must map to Java `Instant`/`LocalDateTime`
- Combines multiple operations (validate card, lookup account, post payment, update balance) in single pseudo-conversational flow

---

### Rank 9: COTRN02C.cbl -- Transaction Add (Online)

| Metric | Value | Notes |
|--------|------:|-------|
| **Lines of Code** | 783 | Online transaction entry with extensive validation |
| **PERFORM Count** | ~35 | Multi-step validation, date checking, amount verification |
| **COPY Statements** | 8 | COCOM01Y, COTRN02, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, CVACT01Y, CVACT03Y |
| **CALL Targets** | 1 | CSUTLDTC (date validation) |
| **EXEC CICS Commands** | ~12 | READ, WRITE, STARTBR, READPREV, ENDBR, SEND MAP, RECEIVE MAP |
| **VSAM Files Accessed** | 3 | TRANSACT (R/W), CARDXREF (R), ACCTFILE (R) |
| **Complexity Score** | **7** | Heavy validation, date parsing, amount formatting |
| **Risk Score** | **7** | Creates new transaction records; validation gaps allow bad data |
| **Business Impact Score** | **7** | Manual transaction entry (corrections, adjustments) |
| **Composite Score** | **7.0** |

**Modernization Challenges:**
- Date validation via CSUTLDTC sub-program call
- Transaction ID auto-generation (browse to find max, increment)
- Multiple validation passes with per-field error messaging
- "Copy last transaction" feature for rapid data entry
- Merchant data validation with multiple optional fields

---

### Rank 10: CBEXPORT.cbl -- Data Export

| Metric | Value | Notes |
|--------|------:|-------|
| **Lines of Code** | 582 | Multi-entity data export utility |
| **PERFORM Count** | ~25 | Sequential read-transform-write for 5 entity types |
| **COPY Statements** | 6 | CVCUS01Y, CVACT01Y, CVACT03Y, CVTRA05Y, CVACT02Y, CVEXPORT |
| **Files Accessed** | 6 | CUSTFILE (R), ACCTFILE (R), CARDXREF (R), TRANSACT (R), CARDFILE (R), EXPORT-OUTPUT (W) |
| **Complexity Score** | **6** | Straightforward ETL but covers all entity types |
| **Risk Score** | **6** | Read-only on master files; export errors affect migration |
| **Business Impact Score** | **7** | Critical for data migration and environment provisioning |
| **Composite Score** | **6.3** |

**Modernization Challenges:**
- Multi-entity export in single sequential pass -- maps to multiple Spring Batch steps
- Record type discriminator pattern (position 1) for multiplexed output
- Paired with CBIMPORT for round-trip data migration
- Timestamp generation for export metadata
- Must preserve exact field positions and lengths for compatibility

---

## Summary Ranking Table

| Rank | Program | LOC | Complexity | Risk | Business Impact | Composite | Recommended Action |
|------|---------|----:|:----------:|:----:|:---------------:|:---------:|---------------------|
| 1 | **COACTUPC** | 4,236 | 10 | 9 | 9 | **9.4** | Decompose into 5+ services; externalize validation |
| 2 | **CBTRN02C** | 731 | 8 | 10 | 10 | **9.2** | Convert to Spring Batch with transactional DB writes |
| 3 | **CBACT04C** | 652 | 8 | 9 | 9 | **8.7** | Isolate calculation engine; preserve decimal precision |
| 4 | **CBSTM03A** | 924 | 8 | 7 | 8 | **7.7** | Replace with template engine (Thymeleaf/Jasper) |
| 5 | **COBIL00C** | 572 | 6 | 9 | 8 | **7.5** | Convert to REST API with DB transaction |
| 6 | **COCRDUPC** | 1,560 | 8 | 7 | 7 | **7.3** | Split view/edit/save into REST endpoints |
| 7 | **COTRN02C** | 783 | 7 | 7 | 7 | **7.0** | Map to transaction entry REST API |
| 8 | **CBTRN03C** | 649 | 7 | 5 | 8 | **6.6** | Replace with SQL report query + PDF generator |
| 9 | **COCRDLIC** | 1,459 | 7 | 5 | 7 | **6.4** | Convert to paginated REST API |
| 10 | **CBEXPORT** | 582 | 6 | 6 | 7 | **6.3** | Replace with Spring Batch multi-entity export |

---

## Modernization Priority Waves

### Wave 1 -- Foundation (Highest Risk + Impact)
- **CBTRN02C** (Transaction Posting) -- core batch; all financial data flows through here
- **CBACT04C** (Interest Calculation) -- revenue-critical; requires exact arithmetic parity
- **COBIL00C** (Bill Payment) -- real-time customer-facing financial writes

### Wave 2 -- Core Online (Highest Complexity)
- **COACTUPC** (Account Update) -- decompose the 4,236-line monolith
- **COCRDUPC** (Credit Card Update) -- card maintenance with change detection
- **COTRN02C** (Transaction Add) -- online transaction entry with validation

### Wave 3 -- Reporting & ETL
- **CBSTM03A/B** (Statements) -- replace with modern template engine
- **CBTRN03C** (Transaction Report) -- replace with SQL + reporting framework
- **CBEXPORT/CBIMPORT** (Data Migration) -- convert to Spring Batch ETL

### Wave 4 -- Supporting Modules
- **COCRDLIC** (Card List) -- paginated browse to REST API
- **COACTVWC** (Account View) -- inquiry to GET endpoint
- **COTRN00C/01C** (Transaction List/View) -- browse to paginated API
- **COSGN00C** (Sign-on) -- replace with Spring Security
- **COUSR00C-03C** (User CRUD) -- standard Spring Data JPA CRUD
- **COMEN01C/COADM01C** (Menus) -- replace with web UI routing

### Wave 5 -- Optional Modules
- Authorization (IMS/DB2/MQ) -- modernize if in scope
- Transaction Type DB2 -- already SQL-based, straightforward migration
- VSAM-MQ integration -- replace MQ with REST or event streaming

---

## Key Technical Risks for Modernization

| Risk | Affected Programs | Mitigation |
|------|-------------------|------------|
| **Decimal arithmetic parity** | CBTRN02C, CBACT04C, COBIL00C | Use `BigDecimal` with explicit rounding mode; create arithmetic test suite |
| **VSAM to RDBMS mapping** | All programs | Map KSDS keys to primary keys; compound keys to composite PKs |
| **CICS pseudo-conversational** | All CO* online programs | Map to stateless REST with session tokens |
| **COMMAREA state passing** | All CO* online programs | Replace with session/JWT claims or request context |
| **3270 BMS maps to Web UI** | All 17 BMS maps | Map field-by-field to HTML forms; preserve validation rules |
| **Batch job orchestration** | 10+ JCL jobs | Replace with Spring Batch + scheduler (Quartz/Spring Cloud Task) |
| **File-level locking** | CLOSEFIL/OPENFIL | Unnecessary with RDBMS row-level locking |
| **Plaintext passwords** | CSUSR01Y/USRSEC | Implement bcrypt/scrypt hashing immediately |
| **ALTER statement** | CBSTM03A | Refactor to standard control flow (IF/EVALUATE) |
| **Assembler dependencies** | COBDATFT, MVSWAIT | Replace with Java `DateTimeFormatter` and `Thread.sleep()` |
