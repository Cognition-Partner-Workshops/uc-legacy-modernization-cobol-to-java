# HOTSPOT REPORT — CardDemo COBOL Codebase

> **Generated:** 2026-03-26 | **Scope:** `uc-legacy-modernization-cobol-to-java`
>
> The top 10 modules ranked by a weighted composite of **code complexity**,
> **migration risk**, and **business impact**. Use this report to sequence
> modernization waves — start with high-impact / high-risk modules to
> de-risk the migration early.

---

## Scoring Methodology

Each module is scored on three dimensions (1–10 scale):

| Dimension | Weight | Factors Considered |
|-----------|--------|-------------------|
| **Complexity** | 35% | LOC, # PERFORM statements, # EVALUATE branches, # IF conditions, # copybook inclusions, # VSAM file accesses, nested logic depth |
| **Migration Risk** | 35% | CICS dependency depth, VSAM I/O patterns (STARTBR/READNEXT pagination), BMS map coupling, cross-program COMMAREA usage, assembler calls, MQ/IMS/DB2 integration, data format transformations |
| **Business Impact** | 30% | Revenue criticality, user-facing frequency, data integrity responsibility, downstream dependencies (how many other programs/jobs depend on this module's output) |

**Composite Score** = (Complexity × 0.35) + (Risk × 0.35) + (Impact × 0.30)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC — Account Update

| Metric | Value | Detail |
|--------|-------|--------|
| **LOC** | **4,236** | Largest program in the entire codebase |
| **PERFORMs** | 64 | High procedural complexity |
| **EVALUATEs** | 20 | Multiple branching paths |
| **IF conditions** | 164 | Extremely high conditional density |
| **Copybooks** | 18 | Highest inclusion count — touches every entity |
| **VSAM files** | ACCTDAT, CARDDAT, CUSTDAT (R/W) | Full CRUD on 3 VSAM files |
| **BMS Map** | COACTUP (31 KB — largest map) | Complex multi-section screen |

| Complexity | Risk | Impact | **Composite** |
|------------|------|--------|---------------|
| 10 | 10 | 9 | **9.7** |

**Why #1:** This is the most complex program by every metric. It performs full CRUD across
accounts, cards, and customers in a single screen with extensive field-level validation.
The 164 IF conditions create deeply nested logic that is extremely difficult to migrate.
The BMS map is the largest in the system. Any bug here directly affects account data integrity.

**Migration Strategy:** Decompose into 3+ microservices (AccountService, CardService,
CustomerService). Map each VSAM operation to a JPA repository method. The BMS validation
logic maps to Jakarta Bean Validation annotations.

---

### Rank 2: COCRDUPC — Credit Card Update

| Metric | Value | Detail |
|--------|-------|--------|
| **LOC** | **1,560** | 4th largest online program |
| **PERFORMs** | 26 | Moderate procedural count |
| **EVALUATEs** | 16 | Many status-based branches |
| **IF conditions** | 72 | High validation complexity |
| **Copybooks** | 12 | Card + customer + common |
| **VSAM files** | CARDDAT (READ/REWRITE) | Card update operations |
| **BMS Map** | COCRDUP | Update form with validation |

| Complexity | Risk | Impact | **Composite** |
|------------|------|--------|---------------|
| 8 | 8 | 8 | **8.0** |

**Why #2:** Card data updates are PCI-sensitive. The 72 IF conditions implement card number
validation, expiration date checks, and status transition rules. Incorrect migration could
expose card data or allow invalid updates.

**Migration Strategy:** Implement as CardUpdateService with PCI-DSS compliant field handling.
Replace BMS validation with server-side validation + client-side form validation.

---

### Rank 3: COCRDLIC — Credit Card List

| Metric | Value | Detail |
|--------|-------|--------|
| **LOC** | **1,459** | Complex pagination logic |
| **PERFORMs** | 34 | Moderate |
| **EVALUATEs** | 18 | Pagination state machine |
| **IF conditions** | 59 | Boundary condition handling |
| **Copybooks** | 11 | Card + common |
| **VSAM files** | CARDDAT (STARTBR/READNEXT/READPREV) | Browse/pagination pattern |
| **BMS Map** | COCRDLI (21 KB — large list screen) | Multi-row list display |

| Complexity | Risk | Impact | **Composite** |
|------------|------|--------|---------------|
| 8 | 9 | 7 | **8.1** |

**Why #3:** The STARTBR/READNEXT/READPREV pagination pattern is one of the hardest CICS
patterns to migrate. The pseudo-conversational state management (remembering browse position
across CICS RETURN/re-entry cycles) requires careful translation to stateless REST pagination.
The multi-row BMS map with dynamic field coloring adds UI complexity.

**Migration Strategy:** Replace VSAM browse with Spring Data JPA `Pageable` queries.
The BMS list maps to a paginated REST endpoint + React/Angular table component.

---

### Rank 4: CBTRN02C — Transaction Posting (Batch)

| Metric | Value | Detail |
|--------|-------|--------|
| **LOC** | **731** | Core batch logic |
| **PERFORMs** | 62 | Highest PERFORM count among batch programs |
| **EVALUATEs** | 0 | Uses IF-based branching instead |
| **IF conditions** | 48 | Complex validation rules |
| **Copybooks** | 5 | Transaction + account entities |
| **Files** | DALYTRAN (in), TRANFILE, XREFFILE, ACCTFILE, TCATBALF (R/W), DALYREJS (out) | Reads 4 files, writes 3 |

| Complexity | Risk | Impact | **Composite** |
|------------|------|--------|---------------|
| 7 | 8 | 10 | **8.3** |

**Why #4:** This is the **most business-critical batch program** — it posts all daily
transactions. A bug here means incorrect account balances, lost transactions, or
financial discrepancies. It reads from 4 input files and writes to 3 outputs,
implementing cross-file validation (card→account lookup via XREF, balance checks).
The reject file handling adds error-recovery complexity.

**Migration Strategy:** Implement as a Spring Batch job with `ItemReader` (daily file),
`ItemProcessor` (validation + XREF lookup), and `ItemWriter` (transaction + balance update).
Use database transactions for atomicity instead of sequential file I/O.

---

### Rank 5: CBACT04C — Interest Calculation (Batch)

| Metric | Value | Detail |
|--------|-------|--------|
| **LOC** | **652** | Moderate size with financial logic |
| **PERFORMs** | 57 | High procedural count |
| **EVALUATEs** | 0 | IF-based logic |
| **IF conditions** | 43 | Rate lookup + calculation branches |
| **Copybooks** | 5 | Account, XREF, transaction entities |
| **Files** | ACCTFILE, XREFFILE, DISCGRP (in), TCATBALF, SYSTRAN (out) | Rate lookup + interest posting |

| Complexity | Risk | Impact | **Composite** |
|------------|------|--------|---------------|
| 7 | 7 | 10 | **7.9** |

**Why #5:** Interest calculation is financially critical — errors directly impact
customer billing. The program implements rate-lookup logic (account group → disclosure
group → interest rate) and compound interest calculations using COBOL packed-decimal
arithmetic. COBOL `PIC S9(09)V99` arithmetic has different rounding behavior than Java
`BigDecimal`, which is a significant risk.

**Migration Strategy:** Implement as a Spring Batch job. Use `BigDecimal` with explicit
`RoundingMode.HALF_EVEN` (banker's rounding) to match COBOL behavior. Create comprehensive
test cases comparing COBOL output vs Java output for decimal edge cases.

---

### Rank 6: CBSTM03A — Statement Generation (Batch)

| Metric | Value | Detail |
|--------|-------|--------|
| **LOC** | **924** | Largest batch program |
| **PERFORMs** | 33 | Moderate |
| **EVALUATEs** | 9 | Format selection branches |
| **IF conditions** | 15 | Lower conditional density |
| **Copybooks** | 4 | Account, XREF, customer, statement layout |
| **Files** | ACCTFILE, XREFFILE, CUSTFILE, TRANSACT (in), STMTFILE, HTMLFILE (out) | Multi-file join → formatted output |
| **Sub-call** | CBSTM03B | Only program that CALLs a sub-program |

| Complexity | Risk | Impact | **Composite** |
|------------|------|--------|---------------|
| 7 | 7 | 8 | **7.3** |

**Why #6:** Statement generation joins 4 VSAM files and produces both plain-text and
HTML output. It's the only batch program that uses a CALL to a sub-program (CBSTM03B),
creating a two-program dependency. The HTML generation logic embedded in COBOL STRING
statements is fragile and hard to migrate. Customer-facing output — errors visible to
cardholders.

**Migration Strategy:** Replace with a Spring Batch job using JPA queries for data
assembly and a template engine (Thymeleaf/FreeMarker) for HTML statement rendering.
Eliminate the CBSTM03B sub-program by inlining its logic into the statement service.

---

### Rank 7: COACTVWC — Account View

| Metric | Value | Detail |
|--------|-------|--------|
| **LOC** | **941** | Moderate online program |
| **PERFORMs** | 21 | Lower procedural count |
| **EVALUATEs** | 10 | Display-mode branches |
| **IF conditions** | 28 | Moderate |
| **Copybooks** | 15 | Second-highest inclusion count |
| **VSAM files** | ACCTDAT, CARDDAT, CUSTDAT (READ-only) | Multi-file read |
| **BMS Map** | COACTVW (23 KB) | Dense display screen |

| Complexity | Risk | Impact | **Composite** |
|------------|------|--------|---------------|
| 6 | 7 | 8 | **7.0** |

**Why #7:** While read-only, this program assembles data from 3 VSAM files and
displays it in a complex BMS screen. It includes 15 copybooks (second only to
COACTUPC). The multi-file join pattern needs to be replaced with JPA entity
relationships. High user frequency — this is the most-visited screen after the menu.

**Migration Strategy:** Implement as a read-only REST endpoint returning a composite
`AccountDetailDTO`. Use JPA `@ManyToOne` / `@OneToMany` relationships to replace the
manual VSAM joins.

---

### Rank 8: CBTRN03C — Transaction Report (Batch)

| Metric | Value | Detail |
|--------|-------|--------|
| **LOC** | **649** | Moderate batch program |
| **PERFORMs** | 73 | **Highest PERFORM count in entire codebase** |
| **EVALUATEs** | 4 | Few top-level branches |
| **IF conditions** | 38 | Moderate |
| **Copybooks** | 5 | Transaction + type + category + report layout |
| **Files** | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM (in), TRANREPT (out) | 5-file input → print report |

| Complexity | Risk | Impact | **Composite** |
|------------|------|--------|---------------|
| 7 | 6 | 7 | **6.6** |

**Why #8:** The 73 PERFORM statements (highest in the codebase) indicate deeply
modularized procedural logic that must be carefully untangled. The 5-file join with
control-break reporting (page totals, account totals, grand totals) is a classic
COBOL pattern that requires careful translation to a report-generation framework.

**Migration Strategy:** Implement as a Spring Batch job with JasperReports or a custom
report writer. Replace COBOL control-break logic with SQL GROUP BY aggregation.
The DATEPARM file becomes application configuration / command-line arguments.

---

### Rank 9: COTRN02C — Transaction Add (Online)

| Metric | Value | Detail |
|--------|-------|--------|
| **LOC** | **783** | Moderate online program |
| **PERFORMs** | 61 | High procedural count |
| **EVALUATEs** | 26 | **Highest EVALUATE count among online programs** |
| **IF conditions** | 14 | Lower — uses EVALUATE instead |
| **Copybooks** | 10 | Transaction + account entities |
| **VSAM files** | ACCTDAT (READ), CARDXREF (STARTBR/READPREV), TRANSACT (READ/WRITE) | Multi-file validation + write |
| **Sub-call** | CSUTLDTC | Date/time utility |

| Complexity | Risk | Impact | **Composite** |
|------------|------|--------|---------------|
| 7 | 7 | 8 | **7.3** |

**Why #9:** This program adds new transactions to the system — a write-path operation
with financial implications. The 26 EVALUATE statements implement a complex state
machine for the transaction entry workflow. It validates the card against XREF,
checks account status, and writes to TRANSACT VSAM. The CSUTLDTC sub-call for
timestamp generation adds a dependency.

**Migration Strategy:** Implement as a `POST /api/transactions` REST endpoint.
Replace EVALUATE state machine with a service method with clear validation steps.
Use `@Transactional` for atomicity.

---

### Rank 10: COTRN00C — Transaction List (Online)

| Metric | Value | Detail |
|--------|-------|--------|
| **LOC** | **699** | Moderate online program |
| **PERFORMs** | 47 | Moderate |
| **EVALUATEs** | 16 | Pagination state management |
| **IF conditions** | 26 | Boundary handling |
| **Copybooks** | 8 | Transaction + common |
| **VSAM files** | TRANSACT (STARTBR/READNEXT/READPREV) | Browse/pagination pattern |
| **BMS Map** | COTRN00 (29 KB — large list) | Multi-row transaction list |

| Complexity | Risk | Impact | **Composite** |
|------------|------|--------|---------------|
| 6 | 7 | 7 | **6.7** |

**Why #10:** Similar to COCRDLIC (#3), this implements the STARTBR/READNEXT pagination
pattern for transactions. The large BMS map (29 KB) and pseudo-conversational state
management make migration non-trivial. High user-facing frequency — core workflow screen.

**Migration Strategy:** Replace with a paginated `GET /api/transactions` endpoint.
Use Spring Data JPA `Pageable` with cursor-based pagination for performance.

---

## Complexity Metrics — All Programs (Sorted by Composite Score)

| Rank | Program | LOC | PERFORMs | EVALUATEs | IFs | Copybooks | Composite |
|------|---------|-----|----------|-----------|-----|-----------|-----------|
| 1 | **COACTUPC** | 4,236 | 64 | 20 | 164 | 18 | 9.7 |
| 2 | **COCRDUPC** | 1,560 | 26 | 16 | 72 | 12 | 8.0 |
| 3 | **COCRDLIC** | 1,459 | 34 | 18 | 59 | 11 | 8.1 |
| 4 | **CBTRN02C** | 731 | 62 | 0 | 48 | 5 | 8.3 |
| 5 | **CBACT04C** | 652 | 57 | 0 | 43 | 5 | 7.9 |
| 6 | **CBSTM03A** | 924 | 33 | 9 | 15 | 4 | 7.3 |
| 7 | **COACTVWC** | 941 | 21 | 10 | 28 | 15 | 7.0 |
| 8 | **CBTRN03C** | 649 | 73 | 4 | 38 | 5 | 6.6 |
| 9 | **COTRN02C** | 783 | 61 | 26 | 14 | 10 | 7.3 |
| 10 | **COTRN00C** | 699 | 47 | 16 | 26 | 8 | 6.7 |
| — | COTRTLIC* | 2,098 | 69 | 32 | 0 | — | 6.5 |
| — | COTRTUPC* | 1,702 | 40 | 26 | 0 | — | 6.3 |
| — | COPAUA0C* | 1,026 | 38 | 10 | 26 | 14 | 6.2 |
| — | COPAUS0C* | 1,032 | 48 | 22 | 25 | 14 | 6.0 |
| — | COUSR00C | 695 | 45 | 16 | 25 | 8 | 5.5 |
| — | COCRDSLC | 887 | 19 | 8 | 33 | 13 | 5.5 |
| — | CORPT00C | 649 | 35 | 10 | 20 | 8 | 5.3 |
| — | COBIL00C | 572 | 38 | 18 | 10 | 10 | 5.8 |
| — | CBTRN01C | 494 | 43 | 0 | 33 | 6 | 4.5 |
| — | CBEXPORT | 582 | 50 | 0 | 16 | 6 | 4.2 |

*\* Optional module programs — scored but not ranked in the core top 10.*

---

## Modernization Wave Recommendations

Based on the hotspot analysis, we recommend the following migration sequence:

### Wave 1 — Foundation (De-risk Core Data Layer)
**Programs:** CBTRN02C (#4), CBACT04C (#5)
**Rationale:** These batch programs own the most critical data-integrity logic
(transaction posting and interest calculation). Migrating them first establishes
the core Java data model and validates decimal-arithmetic compatibility.

### Wave 2 — Core Online CRUD
**Programs:** COACTUPC (#1), COCRDUPC (#2), COTRN02C (#9)
**Rationale:** The three highest-complexity online programs. COACTUPC is the single
riskiest module. Migrating these proves the CICS→Spring MVC/REST pattern and
establishes the validation framework.

### Wave 3 — List/Browse Screens
**Programs:** COCRDLIC (#3), COTRN00C (#10), COUSR00C
**Rationale:** These share the STARTBR/READNEXT pagination pattern. Once one is
migrated, the pattern can be templated for the others.

### Wave 4 — Reporting & Statements
**Programs:** CBSTM03A (#6), CBTRN03C (#8), CORPT00C
**Rationale:** Report generation has lower real-time risk (not user-facing OLTP).
Migrating these replaces COBOL print files with modern reporting (PDF/HTML).

### Wave 5 — View-Only & Navigation
**Programs:** COACTVWC (#7), COCRDSLC, COTRN01C, COMEN01C, COSGN00C, COADM01C
**Rationale:** Read-only and navigation programs are lowest risk. The sign-on
screen migrates to Spring Security. Menus become a frontend router.

### Wave 6 — Optional Modules
**Programs:** COTRTLIC, COTRTUPC, COPAUA0C, COPAUS0C–2C, CODATE01, COACCT01
**Rationale:** IMS/DB2/MQ integrations are specialized and can be migrated last
(or rewritten entirely with modern equivalents like Kafka, PostgreSQL).

---

## Key Migration Risks (Cross-Cutting)

| Risk | Affected Modules | Mitigation |
|------|-----------------|------------|
| **Decimal arithmetic rounding** | CBACT04C, CBTRN02C, COBIL00C | Use `BigDecimal` with `RoundingMode.HALF_EVEN`; create parallel-run test suite comparing COBOL vs Java output |
| **VSAM pagination → SQL pagination** | COCRDLIC, COTRN00C, COUSR00C | Replace STARTBR/READNEXT with keyset pagination (`WHERE key > :lastKey ORDER BY key LIMIT :pageSize`) |
| **BMS screen → Web UI** | All 17 online programs | Map BMS fields to REST DTOs; build React/Angular forms from BMS field definitions |
| **COMMAREA session state** | All online programs | Replace with HTTP session or JWT tokens; map COCOM01Y fields to session attributes |
| **Pseudo-conversational pattern** | All online programs | Replace with stateless REST + client-side state management |
| **Multi-file joins (no FK constraints)** | COACTUPC, COACTVWC, CBSTM03A, CBTRN01C | Define proper JPA entity relationships with foreign keys |
| **GDG (Generation Data Groups)** | TRANBKP, COMBTRAN, DEFGDGB | Replace with timestamped backup tables or S3 versioned objects |
| **Assembler dependencies** | CBACT01C (COBDATFT), COBSWAIT (MVSWAIT) | Replace COBDATFT with `java.time`; replace MVSWAIT with `Thread.sleep()` |
| **Duplicate data structures** | CVTRA05Y ≡ CVTRA06Y, CVCUS01Y ≡ CUSTREC | Consolidate into single Java entity; eliminate redundancy |

---

## Duplicate Logic Identified

| Pattern | Programs | Recommendation |
|---------|----------|---------------|
| **CVTRA05Y ≡ CVTRA06Y** (identical layouts) | CBTRN01C, CBTRN02C use both | Merge into single `Transaction` entity with `status` field |
| **CVCUS01Y ≡ CUSTREC** (identical layouts) | CBSTM03A uses CUSTREC; others use CVCUS01Y | Eliminate CUSTREC; use single `Customer` entity |
| **Menu dispatch pattern** (COMEN01C ≡ COADM01C) | Both use table-driven XCTL | Consolidate into single `MenuRouter` with role-based filtering |
| **VSAM browse pattern** (COCRDLIC ≡ COTRN00C ≡ COUSR00C) | All 3 implement identical STARTBR/READNEXT/READPREV pagination | Create generic `PaginationService<T>` |
| **User CRUD pattern** (COUSR00C–03C) | 4 programs for List/Add/Update/Delete | Consolidate into single `UserController` with REST verbs |
| **Common error handling** (CEE3ABD calls in all batch programs) | 13 batch programs | Replace with Spring `@ControllerAdvice` / `@ExceptionHandler` |
| **Date/time handling** (CSDAT01Y included everywhere) | All 17 online programs | Replace with `java.time.LocalDateTime.now()` |
