# Hotspot Report - CardDemo

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo - Credit Card Management System
> **Methodology:** Modules ranked by composite score of code complexity, migration risk, and business impact

---

## Scoring Methodology

Each module is scored on three dimensions (1-10 scale):

| Dimension | Weight | Criteria |
|-----------|--------|----------|
| **Complexity** | 35% | Lines of code, cyclomatic complexity (EVALUATE/IF nesting depth), number of PERFORMs, number of copybooks included, CICS command count |
| **Risk** | 35% | Data mutation (WRITE/REWRITE/DELETE), financial calculation logic, multi-file I/O, error handling density, external CALL dependencies |
| **Business Impact** | 30% | Revenue criticality, user-facing functionality, data integrity consequences of failure, downstream dependencies |

**Composite Score** = (Complexity × 0.35) + (Risk × 0.35) + (Business Impact × 0.30)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC — Account Update

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 4,236 | Largest program in the entire codebase (2× the next largest) |
| **Copybooks Included** | 13 | COCOM01Y, CSUTLDWY, CVCRD01Y, CSLKPCDY, DFHBMSCA, DFHAID, COTTL01Y, COACTUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVCUS01Y |
| **CICS Commands** | ~20 | SEND MAP, RECEIVE MAP, READ, REWRITE, XCTL, HANDLE ABEND, RETURN |
| **VSAM Files Accessed** | 4 | ACCTFILE (R/W), CARDFILE (R), CUSTFILE (R), XREFFILE (R) |
| **Validation Routines** | 18+ | Edit-Account, Edit-YesNo, Edit-Date, Edit-Signed-9V2, Edit-US-SSN, Edit-US-Phone, Edit-US-State, Edit-FICO, Edit-Alpha, Edit-Num, Edit-State-Zip |
| **Complexity Score** | **10** | |
| **Risk Score** | **9** | Writes to account master (financial data), extensive validation with many edge cases |
| **Business Impact Score** | **9** | Core account management — errors affect balances, credit limits, customer data |
| **Composite Score** | **9.35** | |

**Migration Concerns:**
- Massive monolithic program — should be decomposed into 4-5 Java service classes (AccountService, ValidationService, CustomerService, ScreenController)
- Contains inline validation logic for SSN, phone, state, ZIP codes via lookup tables (CSLKPCDY) — extract to reusable validators
- Uses CSSETATY macro for dynamic screen attribute setting — complex BMS interaction to replicate in web UI
- REWRITE to ACCTFILE changes financial data — requires transaction management and audit logging in Java

---

### Rank 2: CBTRN02C — Transaction Posting

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 731 | |
| **Copybooks Included** | 6 | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |
| **VSAM Files Accessed** | 6 | DALYTRAN (R), TRANSACT (W), XREFFILE (R), DALYREJS (W), ACCTFILE (R/W), TCATBALF (R/W) |
| **Financial Logic** | Yes | Updates account balance, category balances; generates reject records |
| **Complexity Score** | **8** | |
| **Risk Score** | **10** | Writes to 4 files including financial master; generates DB2-format timestamps; reject processing |
| **Business Impact Score** | **10** | Core batch posting — if this fails, no transactions are recorded; direct revenue impact |
| **Composite Score** | **9.30** | |

**Migration Concerns:**
- Highest-risk batch program: mutates ACCTFILE, TCATBALF, writes TRANSACT and DALYREJS in a single run
- Must maintain ACID properties — implement as a Spring Batch job with chunk-based processing and rollback support
- Reject handling (DALYREJS) needs careful mapping to exception/error handling framework
- Timestamp generation (`Z-GET-DB2-FORMAT-TIMESTAMP`) must match DB2 format exactly for data compatibility
- Cross-reference lookup (card → account) is the critical validation step — must be atomic

---

### Rank 3: CBACT04C — Interest Calculation

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 652 | |
| **Copybooks Included** | 5 | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |
| **VSAM Files Accessed** | 5 | TCATBALF (R), XREFFILE (R), DISCGRP (R), ACCTFILE (R/W), TRANSACT (W) |
| **Financial Logic** | Yes | Computes interest charges, applies fees, updates account balance |
| **Complexity Score** | **7** | |
| **Risk Score** | **10** | Directly computes and applies financial charges to customer accounts |
| **Business Impact Score** | **10** | Interest revenue — incorrect calculation means financial loss or regulatory issues |
| **Composite Score** | **8.95** | |

**Migration Concerns:**
- Contains `1300-COMPUTE-INTEREST` and `1400-COMPUTE-FEES` paragraphs — must preserve exact decimal arithmetic (COBOL `PIC S9(09)V99` → Java `BigDecimal`)
- Reads disclosure group for interest rates — ensure rate lookup logic is preserved exactly
- REWRITE to ACCTFILE with updated balance — requires idempotency checks to prevent double-charging
- Default interest rate fallback (`1200-A-GET-DEFAULT-INT-RATE`) must be preserved
- Regulatory compliance: interest calculation logic must be auditable

---

### Rank 4: CBSTM03A — Statement Generation (Main Driver)

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 924 | |
| **Copybooks Included** | 4 | COSTM01, CVACT03Y, CUSTREC, CVACT01Y |
| **VSAM Files Accessed** | 4 (via CBSTM03B) | TRANSACT, XREFFILE, CUSTFILE, ACCTFILE |
| **Output Files** | 2 | STMT-FILE (text), HTML-FILE (HTML) |
| **CALL Dependencies** | 1 | CBSTM03B (file I/O subroutine) |
| **Complexity Score** | **8** | |
| **Risk Score** | **7** | Read-only data access but complex output formatting; dual-format output |
| **Business Impact Score** | **9** | Customer-facing statements — errors visible to cardholders |
| **Composite Score** | **8.05** | |

**Migration Concerns:**
- Uses `ALTER ... TO PROCEED TO` (computed GO TO) — rare and difficult COBOL construct, must be carefully refactored to Java control flow
- Tight coupling with CBSTM03B via CALL — these two must be migrated together as a unit
- Generates both plain-text and HTML statements — map to modern template engine (Thymeleaf, Freemarker)
- Complex record-level iteration: loops over cross-references, then cards, then transactions per card
- Statement formatting logic is deeply embedded in WRITE statements with complex FILLER layouts

---

### Rank 5: COCRDLIC — Card List (Browse)

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 1,459 | |
| **Copybooks Included** | 10 | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDLI, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y |
| **CICS Commands** | ~15 | SEND MAP, RECEIVE MAP, STARTBR, READNEXT, READPREV, ENDBR, XCTL, RETURN |
| **Navigation Logic** | Forward/backward paging, card selection, transfer to detail/update |
| **Complexity Score** | **8** | |
| **Risk Score** | **6** | Read-only browsing with XCTL to other programs |
| **Business Impact Score** | **7** | Primary card lookup interface — entry point for card operations |
| **Composite Score** | **7.00** | |

**Migration Concerns:**
- Implements full CICS browse (STARTBR/READNEXT/READPREV/ENDBR) — must be converted to paginated REST API or database query with LIMIT/OFFSET
- 7-row scrollable array with dynamic attributes per row — complex BMS array handling
- Multiple XCTL exits to COCRDSLC (view) and COCRDUPC (update) based on user selection
- Screen attribute manipulation per array element (color coding based on card status)
- Context management across pages via COMMAREA fields

---

### Rank 6: COCRDUPC — Card Update

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 1,560 | |
| **Copybooks Included** | 10 | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y |
| **CICS Commands** | ~15 | SEND MAP, RECEIVE MAP, READ, REWRITE, XCTL, HANDLE ABEND |
| **Validation Routines** | 6 | Edit-Account, Edit-Card, Edit-Name, Edit-CardStatus, Edit-Expiry-Mon, Edit-Expiry-Year |
| **Complexity Score** | **8** | |
| **Risk Score** | **8** | REWRITE to CARDFILE — changes card status, expiry, embossed name |
| **Business Impact Score** | **7** | Card data changes affect transaction authorization |
| **Composite Score** | **7.70** | |

**Migration Concerns:**
- Multi-step update flow: read-for-update → display → validate → confirm → write
- REWRITE with CICS update token — must implement optimistic locking in Java (version column or ETag)
- Card status change has downstream impact on transaction authorization
- Expiry date validation (month 01-12, year logic) must be preserved
- ABEND handling and recovery logic needs mapping to Java exception handling

---

### Rank 7: CBTRN03C — Transaction Report Generation

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 649 | |
| **Copybooks Included** | 5 | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| **VSAM Files Accessed** | 5 | TRANSACT (R), REPORT-FILE (W), XREFFILE (R), TRANTYPE (R), TRANCATG (R) |
| **Report Features** | Page totals, account totals, grand totals, date range filtering |
| **Complexity Score** | **7** | |
| **Risk Score** | **6** | Read-only data access; write to report file only |
| **Business Impact Score** | **8** | Operational reporting — used for daily reconciliation and audit |
| **Composite Score** | **7.05** | |

**Migration Concerns:**
- Control-break reporting logic (account changes trigger subtotals) — must preserve in Java report engine
- Date range parameter file (`DATEPARM`) — convert to command-line args or config properties
- Print-oriented output (133 columns) with headers, detail lines, and totals — map to PDF generation or web report
- Multiple reference table lookups (TRANTYPE, TRANCATG) per transaction — optimize with in-memory caching in Java
- Report layout defined in CVTRA07Y copybook with edited PIC fields (`PIC -ZZZ,ZZZ,ZZZ.ZZ`) — format in Java with `DecimalFormat`

---

### Rank 8: COACTVWC — Account View

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 941 | |
| **Copybooks Included** | 13 | CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COACTVW, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y |
| **CICS Commands** | ~12 | SEND MAP, RECEIVE MAP, READ (×3 files), SEND TEXT, RETURN, HANDLE ABEND |
| **VSAM Files Accessed** | 4 | ACCTFILE (R), CARDFILE (R), CUSTFILE (R), XREFFILE (R) |
| **Complexity Score** | **7** | |
| **Risk Score** | **5** | Read-only — no data mutation |
| **Business Impact Score** | **8** | Primary account inquiry screen — most frequently used by operators |
| **Composite Score** | **6.60** | |

**Migration Concerns:**
- Joins data from 4 VSAM files (Account + Card + Customer + Xref) into a single screen — natural fit for SQL JOIN or JPA entity graph
- Contains SEND TEXT fallback for error display — map to error page/toast in web UI
- Uses CSSTRPFY (PF-key mapper) inline COPY — shared behavior with 4 other programs
- Screen layout maps to a read-only account detail page with related card and customer info

---

### Rank 9: CORPT00C — Transaction Report Request

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 649 | |
| **Copybooks Included** | 6 | COCOM01Y, CORPT00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVTRA05Y, DFHAID, DFHBMSCA |
| **CICS Commands** | ~10 | SEND MAP, RECEIVE MAP, WRITEQ TD, RETURN |
| **CALL Dependencies** | 1 | CSUTLDTC (date validation) |
| **Special Feature** | Submits JCL via internal reader (WRITEQ TD to INTRDR) |
| **Complexity Score** | **7** | |
| **Risk Score** | **7** | Triggers batch job submission — indirect but significant side effect |
| **Business Impact Score** | **7** | Report generation gateway — controls what reports are produced |
| **Composite Score** | **7.00** | |

**Migration Concerns:**
- **Critical pattern:** Submits JCL to internal reader via `CICS WRITEQ TD` — this is the bridge between online and batch
- Must be replaced with REST API call to Spring Batch job launcher or message queue submission
- Date validation via CALL to CSUTLDTC — reuse shared date validation service
- Builds JCL dynamically with date parameters — replace with batch job parameters
- Loop constructs JCL line-by-line (PERFORM VARYING ... WRITE-JOBSUB-TDQ) — complex string assembly

---

### Rank 10: COBIL00C — Bill Payment

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 572 | |
| **Copybooks Included** | 8 | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y, DFHAID, DFHBMSCA |
| **CICS Commands** | ~15 | SEND MAP, RECEIVE MAP, READ, REWRITE, WRITE, STARTBR, READPREV, ENDBR, ASKTIME, FORMATTIME |
| **VSAM Files Accessed** | 3 | ACCTFILE (R/W), XREFFILE (R), TRANSACT (R/W) |
| **Complexity Score** | **6** | |
| **Risk Score** | **9** | Writes payment transaction AND updates account balance — dual financial mutation |
| **Business Impact Score** | **8** | Payment processing — errors mean incorrect balances or lost payments |
| **Composite Score** | **7.60** | |

**Migration Concerns:**
- **Dual write:** Creates transaction record AND updates account balance in same logical unit — must be wrapped in database transaction
- Uses ASKTIME/FORMATTIME for timestamp — replace with `java.time.Instant`
- STARTBR/READPREV on TRANSACT to get last transaction ID for sequencing — replace with database sequence or UUID
- Confirmation flow (display → confirm → execute) maps well to REST API with optimistic locking
- Balance update arithmetic must use `BigDecimal` to avoid floating-point errors

---

## Composite Score Summary

| Rank | Program | Complexity | Risk | Business Impact | **Composite** | Category |
|------|---------|:---:|:---:|:---:|:---:|----------|
| 1 | **COACTUPC** | 10 | 9 | 9 | **9.35** | Online - Account |
| 2 | **CBTRN02C** | 8 | 10 | 10 | **9.30** | Batch - Posting |
| 3 | **CBACT04C** | 7 | 10 | 10 | **8.95** | Batch - Financial |
| 4 | **CBSTM03A** | 8 | 7 | 9 | **7.95** | Batch - Reporting |
| 5 | **COCRDUPC** | 8 | 8 | 7 | **7.70** | Online - Card |
| 6 | **COBIL00C** | 6 | 9 | 8 | **7.65** | Online - Payment |
| 7 | **CBTRN03C** | 7 | 6 | 8 | **6.95** | Batch - Reporting |
| 8 | **COCRDLIC** | 8 | 6 | 7 | **7.00** | Online - Card |
| 9 | **CORPT00C** | 7 | 7 | 7 | **7.00** | Online - Reporting |
| 10 | **COACTVWC** | 7 | 5 | 8 | **6.60** | Online - Account |

---

## Migration Priority Recommendations

### Phase 1 — High Risk / High Impact (Migrate First with Maximum Testing)

| Program | Rationale |
|---------|-----------|
| **CBTRN02C** | Core posting engine — all financial data flows through here. Migrate early to establish batch framework patterns. |
| **CBACT04C** | Interest calculation — regulatory/financial risk. Requires exact arithmetic validation against COBOL results. |
| **COBIL00C** | Payment processing — customer-facing financial mutation. Good candidate for proving transactional integrity. |

### Phase 2 — High Complexity (Decompose and Modernize)

| Program | Rationale |
|---------|-----------|
| **COACTUPC** | Largest program — decompose into multiple Java services. Establish validation framework patterns here. |
| **CBSTM03A/B** | Statement generation pair — migrate together. Replace with modern template engine. |
| **COCRDUPC** | Card update — establishes update/validation patterns reusable across all CRUD screens. |

### Phase 3 — Medium Complexity (Leverage Patterns from Phase 1-2)

| Program | Rationale |
|---------|-----------|
| **COCRDLIC** | Browse/pagination pattern — once solved, reusable for COTRN00C and COUSR00C. |
| **CBTRN03C** | Batch reporting — leverage report generation patterns from CBSTM03A migration. |
| **CORPT00C** | Online-to-batch bridge — replace JCL submission with REST/message-based job triggering. |
| **COACTVWC** | Read-only view — lowest risk, good for validating data access layer. |

### Phase 4 — Lower Complexity (Rapid Migration)

All remaining programs (menus, simple CRUD, utilities, read-only displays) can leverage frameworks and patterns established in Phases 1-3 for accelerated migration.

---

## Key Technical Risks Across All Hotspots

| Risk Area | Programs Affected | Mitigation |
|-----------|------------------|------------|
| **Decimal Arithmetic Precision** | CBTRN02C, CBACT04C, COBIL00C | Use `BigDecimal` exclusively; compare results against COBOL output for regression |
| **VSAM Browse → SQL Pagination** | COCRDLIC, COTRN00C, COUSR00C | Implement cursor-based pagination; test boundary conditions |
| **CICS COMMAREA → Session State** | All 17 online programs | Design session management strategy (JWT, HTTP session, Redis) before migration |
| **Batch Job Orchestration** | CBTRN02C, CBACT04C, CBSTM03A | Implement Spring Batch with proper restart/recovery; preserve job dependency chain |
| **Internal Reader Submission** | CORPT00C | Replace WRITEQ TD to INTRDR with REST API or message queue |
| **ALTER/Computed GO TO** | CBSTM03A | Refactor to standard control flow before or during migration |
| **Plaintext Passwords** | COSGN00C, CSUSR01Y | Implement bcrypt/scrypt hashing in Java — no backward compatibility needed |
| **PII Data (SSN, DOB)** | COACTUPC, CVCUS01Y | Implement field-level encryption in Java; add data masking for display |
