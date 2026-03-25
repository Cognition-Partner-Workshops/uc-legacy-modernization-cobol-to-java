# CardDemo Hotspot Report — Top 10 Modernization Priority Modules

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Purpose:** Identify highest-risk, highest-complexity modules to prioritize during modernization

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Summary](#top-10-hotspot-summary)
3. [Detailed Module Assessments](#detailed-module-assessments)
4. [Risk Heat Map](#risk-heat-map)
5. [Recommended Migration Wave Plan](#recommended-migration-wave-plan)
6. [Key Observations & Recommendations](#key-observations--recommendations)

---

## Scoring Methodology

Each module is scored across three dimensions on a 1–10 scale:

| Dimension | Weight | Factors Measured |
|-----------|--------|-----------------|
| **Complexity** | 35% | Lines of code, IF/EVALUATE branching density, PERFORM count, number of copybooks included, CICS command count, file I/O operations |
| **Risk** | 35% | Number of VSAM files accessed (R/W), data mutation operations (WRITE/REWRITE/DELETE), cross-program dependencies (CALL/XCTL fan-out), shared copybook sensitivity, concurrency exposure |
| **Business Impact** | 30% | Domain criticality (financial calculations, security, data integrity), frequency of execution, downstream dependency count, regulatory/audit implications |

**Composite Score** = (Complexity × 0.35) + (Risk × 0.35) + (Business Impact × 0.30)

---

## Top 10 Hotspot Summary

| Rank | Module | LOC | Complexity | Risk | Biz Impact | Composite | Domain |
|------|--------|-----|-----------|------|-----------|-----------|--------|
| **1** | **COACTUPC.cbl** | 4,236 | 10 | 9 | 9 | **9.35** | Account Update |
| **2** | **CBTRN02C.cbl** | 731 | 8 | 10 | 10 | **9.30** | Transaction Posting |
| **3** | **CBACT04C.cbl** | 652 | 7 | 9 | 10 | **8.60** | Interest Calculation |
| **4** | **CBSTM03A.CBL** | 924 | 8 | 7 | 8 | **7.65** | Statement Generation |
| **5** | **COCRDLIC.cbl** | 1,459 | 8 | 6 | 6 | **6.70** | Card List Browse |
| **6** | **COCRDUPC.cbl** | 1,560 | 8 | 7 | 6 | **7.05** | Card Update |
| **7** | **COSGN00C.cbl** | 260 | 3 | 7 | 10 | **6.50** | Sign-On / Auth |
| **8** | **CBTRN03C.cbl** | 649 | 7 | 5 | 7 | **6.30** | Transaction Report |
| **9** | **COTRN02C.cbl** | 783 | 7 | 7 | 7 | **7.00** | Transaction Add |
| **10** | **COBIL00C.cbl** | 572 | 6 | 7 | 8 | **6.95** | Bill Payment |

---

## Detailed Module Assessments

### #1 — COACTUPC.cbl (Account Update)

| Metric | Value | Assessment |
|--------|-------|-----------|
| **Lines of Code** | 4,236 | Largest program in codebase — 2.7x next largest |
| **IF statements** | 164 | Extremely high branching density |
| **EVALUATE blocks** | 10 | Complex state machine logic |
| **PERFORM calls** | 64 | High internal complexity |
| **Copybooks included** | 14 | Most copybook dependencies of any program |
| **VSAM files accessed** | 3 (ACCTDATA R/W, CARDXREF R, CUSTDATA R/W) | Reads and writes core financial data |
| **CICS operations** | 5 (READ, REWRITE) | Mutates account AND customer records |
| **XCTL targets** | 2 (COMEN01C, COACTVWC) | Moderate navigation complexity |
| **Special copybooks** | CSLKPCDY (1,318-line lookup), CSSETATY (COPY REPLACING), CSUTLDPY | Most complex copybook usage pattern |

**Why it's #1:** This is the single most complex module in the entire application. At 4,236 lines with 164 IF branches, it handles both account and customer record updates in a single monolithic program. It accesses 3 VSAM files with write operations, includes 14 copybooks (including the 1,318-line lookup table), and uses advanced COBOL features like `COPY REPLACING`. Any error in this module directly corrupts financial records.

**Modernization Risks:**
- Should be decomposed into separate Account Update and Customer Update services
- Field-level validation logic is deeply intertwined with screen handling
- COPY REPLACING for CSSETATY needs careful translation (no direct Java equivalent)
- The 1,318-line CSLKPCDY lookup table should become a database table or enum

---

### #2 — CBTRN02C.cbl (Transaction Posting — Batch)

| Metric | Value | Assessment |
|--------|-------|-----------|
| **Lines of Code** | 731 | Moderate size |
| **IF statements** | 48 | High branching for validation |
| **PERFORM calls** | 61 | Complex processing loop |
| **VSAM files accessed** | 6 (DALYTRAN R, TRANSACT W, XREFFILE R, ACCTFILE R/W, TCATBALF R/W, DALYREJS W) | Highest file fan-out in codebase |
| **Data mutations** | WRITE (TRANSACT, DALYREJS), REWRITE (ACCTFILE, TCATBALF) | Multi-file transactional updates |
| **Error handling** | CEE3ABD (abnormal termination) | Fatal error = batch abort |

**Why it's #2:** This is the core financial engine of the batch cycle. It reads daily transactions, validates them against the cross-reference and account files, posts valid transactions to the master file, updates account balances and category balances, and rejects invalid transactions. It touches 6 VSAM files — more than any other program. A bug here directly impacts every customer's account balance.

**Modernization Risks:**
- Must preserve exact decimal arithmetic (COBOL packed decimal → Java BigDecimal)
- Multi-file update consistency — no ACID transactions in VSAM; modernized version needs proper transaction management
- Reject file handling (DALYREJS) needs equivalent error queue
- Processing sequence within the batch cycle is critical — cannot be reordered

---

### #3 — CBACT04C.cbl (Interest Calculation)

| Metric | Value | Assessment |
|--------|-------|-----------|
| **Lines of Code** | 652 | Moderate size |
| **IF statements** | 43 | Complex conditional logic |
| **PERFORM calls** | 56 | High iteration count |
| **VSAM files accessed** | 6 (ACCTFILE R/W, DISCGRP R, TCATBALF R/W, XREFFILE R, DALYTRAN R, TRANFILE W) | Multi-file financial computation |
| **Business logic** | Interest rate lookup, daily accrual calculation, balance tier computation | Core financial algorithm |
| **Regulatory exposure** | Interest calculations must comply with TILA, Reg Z | Audit-critical |

**Why it's #3:** Interest calculation is the most financially sensitive batch process. It reads disclosure group rates, applies them to category balances per account, and generates interest charge transactions. Any arithmetic error directly impacts customer billing and has regulatory implications. The interest rate lookup logic (DISCGRP → account group → type → category) is non-trivial.

**Modernization Risks:**
- Decimal precision MUST be preserved exactly (rounding rules matter for compliance)
- Interest rate lookup chain (DISCGRP + TCATBALF + ACCTFILE) needs careful mapping to relational model
- Generated interest transactions must maintain the exact same format as posted transactions
- Must be thoroughly regression-tested with known-good test data

---

### #4 — CBSTM03A.CBL (Statement Generation)

| Metric | Value | Assessment |
|--------|-------|-----------|
| **Lines of Code** | 924 | Large program |
| **CALL statements** | 13 (to CBSTM03B) | Heaviest inter-program call pattern |
| **WRITE statements** | 80+ | Extremely output-intensive (text + HTML) |
| **Output formats** | 2 (plain text PS, HTML) | Dual-format report generation |
| **Special features** | ALTER statement, mainframe control block addressing | Uses deprecated/advanced COBOL features |
| **Subroutine coupling** | Tightly coupled to CBSTM03B via shared area | Must be migrated as a pair |

**Why it's #4:** This program generates customer-facing account statements in two formats. It uses the `ALTER` statement (a deprecated COBOL feature that modifies GO TO targets at runtime), which has no equivalent in modern languages and requires careful analysis. It makes 13 calls to CBSTM03B for file I/O, creating tight coupling. The 80+ WRITE statements for HTML generation are essentially a template engine embedded in COBOL.

**Modernization Risks:**
- `ALTER` statement must be refactored into conditional logic before conversion
- Mainframe control block addressing (pointer manipulation) needs special handling
- HTML generation should be replaced with a proper template engine (Thymeleaf, FreeMarker)
- CBSTM03A and CBSTM03B must be migrated together as a unit
- Statement formatting rules are likely business-specified and must be preserved exactly

---

### #5 — COCRDLIC.cbl (Card List Browse)

| Metric | Value | Assessment |
|--------|-------|-----------|
| **Lines of Code** | 1,459 | Third-largest program |
| **IF statements** | 59 | Complex pagination logic |
| **EVALUATE blocks** | 18 | Highest EVALUATE count — complex state machine |
| **PERFORM calls** | 34 | Moderate |
| **CICS browse operations** | 7 (STARTBR, READNEXT, READPREV, ENDBR) | Full browse pattern |
| **XCTL targets** | 3 (COMEN01C, COCRDSLC, COCRDUPC) | Dispatches to view/update |

**Why it's #5:** The card list program implements the most complex UI interaction pattern in the application: paginated browse with forward/backward navigation, selection routing to detail or update screens, and dynamic screen refresh. The 18 EVALUATE blocks form a state machine that manages the browse context. This pattern appears in several programs (COTRN00C, COUSR00C) and establishes a reusable pattern for modernization.

**Modernization Risks:**
- CICS browse (STARTBR/READNEXT/READPREV/ENDBR) must be replaced with paginated queries
- State machine logic needs careful mapping to a web session or API pagination pattern
- Selection routing to child screens maps to navigation/routing in modern UI framework
- Test data must cover edge cases: empty list, single record, boundary pages

---

### #6 — COCRDUPC.cbl (Card Update)

| Metric | Value | Assessment |
|--------|-------|-----------|
| **Lines of Code** | 1,560 | Second-largest program |
| **IF statements** | 72 | High validation complexity |
| **EVALUATE blocks** | 16 | Complex state handling |
| **PERFORM calls** | 26 | Moderate |
| **VSAM files accessed** | 2 (CARDDATA R/W, CUSTDATA R) | Mutates card records |
| **CICS REWRITE** | 1 (CARDDATA) | Writes to card master |

**Why it's #6:** Card update handles modification of credit card records, including sensitive fields like expiration dates and active status. The 72 IF statements primarily handle field validation and error messaging. It's the second-largest program by LOC and directly mutates the card master file.

**Modernization Risks:**
- Field validation logic should be extracted into a reusable validation service
- Card data updates have PCI-DSS implications in the modernized system
- Screen-to-data binding logic is interleaved with business rules

---

### #7 — COSGN00C.cbl (Sign-On / Authentication)

| Metric | Value | Assessment |
|--------|-------|-----------|
| **Lines of Code** | 260 | Small but critical |
| **VSAM files accessed** | 1 (USRSEC R) | Reads security credentials |
| **XCTL targets** | 2 (COMEN01C, COADM01C) | Role-based dispatch |
| **Security concerns** | Plaintext password comparison, no lockout, no session timeout | Multiple security vulnerabilities |

**Why it's #7:** Despite its small size, this is the security gateway for the entire application. It reads plaintext passwords from USRSEC and performs a simple string comparison — no hashing, salting, or encryption. There is no account lockout mechanism, no password complexity enforcement, and no session timeout. The role-based dispatch (Regular → COMEN01C, Admin → COADM01C) is the sole authorization check.

**Modernization Risks:**
- **Must** implement proper authentication (bcrypt/scrypt hashing, OAuth2/OIDC, MFA)
- Password storage must be completely redesigned
- Session management needs implementation (no CICS pseudo-conversational equivalent)
- Authorization model should be upgraded from binary (R/A) to RBAC
- This should be one of the first modules migrated to establish the security foundation

---

### #8 — CBTRN03C.cbl (Transaction Report)

| Metric | Value | Assessment |
|--------|-------|-----------|
| **Lines of Code** | 649 | Moderate |
| **IF statements** | 38 | Report break logic |
| **PERFORM calls** | 72 | Highest PERFORM count — complex iteration |
| **Files accessed** | 6 (TRANSACT, XREFFILE, TRANTYPE, TRANCATG, DATEPARM, TRANREPT) | Heavy read + report write |
| **Report features** | Page breaks, control breaks, subtotals, grand totals | Classic report writer pattern |

**Why it's #8:** The daily transaction report program has the highest PERFORM count (72) of any program, reflecting its complex nested iteration: by account, by transaction type, by category, with running totals at each break level. It reads 5 input files and produces formatted output. The report break logic (page totals, account totals, grand totals) is a classic COBOL pattern that maps to Spring Batch or reporting frameworks.

**Modernization Risks:**
- Control break logic should map to a reporting framework (JasperReports, BIRT)
- Date parameter file (DATEPARM) input should become API parameters
- Report output format likely needs modernization (PDF instead of fixed-width text)

---

### #9 — COTRN02C.cbl (Transaction Add — Online)

| Metric | Value | Assessment |
|--------|-------|-----------|
| **Lines of Code** | 783 | Moderate |
| **EVALUATE blocks** | 13 | Complex validation state machine |
| **PERFORM calls** | 61 | High for an online program |
| **VSAM files accessed** | 3 (ACCTDATA R, CARDXREF R, TRANSACT R/W) | Writes new transactions |
| **CALL statements** | 2 (CSUTLDTC for date validation) | External dependency |
| **CICS operations** | 6 (READ, WRITE, STARTBR, READPREV, ENDBR) | Full create workflow |

**Why it's #9:** This is the online transaction entry point — where new transactions are created interactively. It validates the account and card via cross-reference lookups, validates dates using the CSUTLDTC utility, generates a unique transaction ID (via READPREV to find last ID), and writes the new transaction. It's a complete CRUD create operation touching 3 VSAM files.

**Modernization Risks:**
- Transaction ID generation (READPREV for last ID + increment) needs a proper sequence generator
- Multi-file validation (XREF → ACCT → TRANSACT) should become a service layer with proper error handling
- Date validation call to CSUTLDTC should use java.time in modernized version

---

### #10 — COBIL00C.cbl (Bill Payment)

| Metric | Value | Assessment |
|--------|-------|-----------|
| **Lines of Code** | 572 | Moderate |
| **EVALUATE blocks** | 9 | Payment workflow states |
| **PERFORM calls** | 38 | Moderate |
| **VSAM files accessed** | 3 (ACCTDATA R/W, CARDXREF R, TRANSACT W) | Writes financial records |
| **CICS operations** | 7 (READ, REWRITE, WRITE, STARTBR, READPREV, ENDBR) | Full payment workflow |
| **Financial sensitivity** | Updates account balance, creates payment transaction | Direct balance impact |

**Why it's #10:** Bill payment is a financial transaction that debits the account balance and creates a payment record. It accesses 3 VSAM files with write operations (account rewrite + transaction write). The payment workflow includes account lookup, balance verification, payment posting, and transaction record creation. Any error directly impacts customer balances.

**Modernization Risks:**
- Payment processing must be atomic (account update + transaction write)
- Balance verification and update must prevent race conditions
- Should integrate with payment gateway in modernized version
- Audit trail requirements for financial transactions

---

## Risk Heat Map

```
                    Low Business Impact    Medium Business Impact    High Business Impact
                  ┌─────────────────────┬────────────────────────┬───────────────────────┐
  High Complexity │                     │  COCRDLIC (#5)         │  COACTUPC (#1)        │
                  │                     │  COCRDUPC (#6)         │  CBSTM03A (#4)        │
                  │                     │                        │                       │
                  ├─────────────────────┼────────────────────────┼───────────────────────┤
  Med Complexity  │  CBACT01C           │  CBTRN03C (#8)         │  CBTRN02C (#2)        │
                  │  CBACT02C           │  COTRN00C              │  CBACT04C (#3)        │
                  │  CBACT03C           │  COUSR00C              │  COTRN02C (#9)        │
                  │  CBCUS01C           │                        │  COBIL00C (#10)       │
                  ├─────────────────────┼────────────────────────┼───────────────────────┤
  Low Complexity  │  COBSWAIT           │  COUSR01C              │  COSGN00C (#7)        │
                  │  UNUSED1Y           │  COUSR02C              │                       │
                  │  CBSTM03B (sub)     │  COUSR03C              │                       │
                  │                     │  COTRN01C              │                       │
                  └─────────────────────┴────────────────────────┴───────────────────────┘
```

---

## Recommended Migration Wave Plan

Based on the hotspot analysis, here is the recommended migration sequence:

### Wave 0 — Foundation (Weeks 1–3)
**Goal:** Establish core infrastructure and security

| Module | Priority | Rationale |
|--------|----------|-----------|
| **COSGN00C** (#7) | Security gateway | Must be migrated first to establish auth framework |
| **COMEN01C** / **COADM01C** | Navigation | Menu routing → Spring MVC controllers / API gateway |
| **COCOM01Y** copybook | Shared context | Defines inter-program communication → Session/context management |

### Wave 1 — Read-Only Functions (Weeks 4–6)
**Goal:** Migrate low-risk, high-visibility read operations

| Module | Priority | Rationale |
|--------|----------|-----------|
| **COACTVWC** | Account view | Read-only, validates data layer works |
| **COCRDSLC** | Card view | Read-only, validates card data model |
| **COTRN00C** / **COTRN01C** | Transaction list/view | Read-only, validates transaction model |
| **COUSR00C** | User list | Read-only, validates security model |

### Wave 2 — CRUD Operations (Weeks 7–10)
**Goal:** Add write capabilities, carefully

| Module | Priority | Rationale |
|--------|----------|-----------|
| **COUSR01C–03C** | User CRUD | Simple CRUD, low financial risk |
| **COCRDUPC** (#6) | Card update | Single-file write, moderate complexity |
| **COTRN02C** (#9) | Transaction add | Validates write path for transactions |

### Wave 3 — Financial Core (Weeks 11–15)
**Goal:** Migrate the most critical financial programs with extensive testing

| Module | Priority | Rationale |
|--------|----------|-----------|
| **COACTUPC** (#1) | Account update | Highest complexity — decompose into Account + Customer services |
| **COBIL00C** (#10) | Bill payment | Financial transaction, needs atomicity |
| **CORPT00C** | Online reports | Report submission → async job |

### Wave 4 — Batch Processing (Weeks 16–20)
**Goal:** Migrate batch cycle to Spring Batch / scheduled jobs

| Module | Priority | Rationale |
|--------|----------|-----------|
| **CBTRN02C** (#2) | Transaction posting | Core batch engine — needs Spring Batch job with step-by-step validation |
| **CBACT04C** (#3) | Interest calculation | Financially critical — requires exact decimal regression testing |
| **CBSTM03A/B** (#4) | Statement generation | Template engine replacement, paired migration |
| **CBTRN03C** (#8) | Transaction report | Report framework migration |
| **CBEXPORT / CBIMPORT** | Data migration | Utility functions — may become ETL jobs or APIs |

### Wave 5 — Optional Modules (Weeks 21+)
**Goal:** Migrate extension modules if in scope

| Module | Rationale |
|--------|-----------|
| Authorization (IMS/DB2/MQ) | Complex middleware integration — may need full redesign |
| Transaction Type DB2 | DB2 embedded SQL → JPA/Hibernate |
| VSAM-MQ | MQ request/response → REST API or message broker |

---

## Key Observations & Recommendations

### Top 5 Modernization Risks

1. **COACTUPC monolith (4,236 lines):** Must be decomposed — too large and complex for a 1:1 conversion. Split into AccountUpdateService and CustomerUpdateService minimum.

2. **Decimal arithmetic precision:** COBOL packed decimal (COMP-3) and display numeric arithmetic must be mapped to Java `BigDecimal` with explicit rounding modes. Interest calculations (CBACT04C) and transaction posting (CBTRN02C) are particularly sensitive.

3. **Plaintext passwords (COSGN00C):** The current authentication model has zero security controls. The modernized system must implement proper credential storage, session management, and authorization before any user-facing features go live.

4. **Batch file concurrency:** The current batch cycle requires CICS files to be closed (CLOSEFIL → OPENFIL pattern). The modernized system needs proper database transaction isolation rather than exclusive file locks.

5. **ALTER statement (CBSTM03A):** The `ALTER` statement dynamically changes GO TO destinations at runtime. This must be refactored into conditional logic before any automated conversion tool can process it.

### Top 5 Quick Wins

1. **Lookup tables (CSLKPCDY, CVTRA03Y, CVTRA04Y):** Move to database reference tables immediately — these are pure data with no logic.

2. **User CRUD (COUSR01C–03C):** Simple, isolated programs with minimal dependencies — good first migration candidates.

3. **Read-only screens (COACTVWC, COCRDSLC, COTRN01C):** Low risk, high visibility — demonstrates progress early.

4. **Export/Import (CBEXPORT/CBIMPORT):** Self-contained data migration utilities — can become REST APIs or ETL scripts.

5. **Date utility (CSUTLDTC):** Replace with `java.time` — eliminates a cross-cutting dependency.

### Architecture Recommendations for Target State

| COBOL Pattern | Recommended Java Pattern |
|--------------|------------------------|
| CICS XCTL navigation | Spring MVC controllers + Thymeleaf / React SPA routing |
| BMS maps (3270 screens) | HTML forms / React components |
| VSAM KSDS files | PostgreSQL / MySQL tables with JPA entities |
| Copybook record layouts | Java POJOs / Records with Jakarta Validation |
| Batch JCL jobs | Spring Batch jobs with ItemReader/Processor/Writer |
| COCOM01Y (comm area) | Spring Session / SecurityContext / RequestScope beans |
| CICS STARTBR/READNEXT | Spring Data JPA paginated queries (Pageable) |
| EXEC CICS READ/WRITE | JPA Repository methods (findById, save) |
| CEE3ABD (abend) | Exception handling with @ControllerAdvice |
| COBDATFT / CSUTLDTC | java.time.LocalDate / DateTimeFormatter |
