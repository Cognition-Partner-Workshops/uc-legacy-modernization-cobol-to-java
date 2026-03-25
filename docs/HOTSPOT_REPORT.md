# CardDemo Hotspot Report — Top 10 Modules for Modernization

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Purpose:** Prioritize modules by complexity, migration risk, and business impact for Java modernization planning

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Summary](#top-10-hotspot-summary)
3. [Detailed Analysis per Module](#detailed-analysis-per-module)
4. [Risk Heat Map](#risk-heat-map)
5. [Recommended Migration Sequence](#recommended-migration-sequence)
6. [Quick Wins (Low Complexity / High Value)](#quick-wins-low-complexity--high-value)

---

## Scoring Methodology

Each module is scored on three dimensions (1–10 scale):

| Dimension | Weight | Criteria |
|---|---|---|
| **Complexity** | 35% | Lines of code, cyclomatic complexity indicators (EVALUATE/IF nesting), number of PERFORM paragraphs, number of COPY dependencies, external CALLs, VSAM file operations |
| **Risk** | 35% | Data sensitivity (PII, financial), multi-file update scope (data integrity), CICS transaction complexity (STARTBR/READNEXT/REWRITE patterns), error handling sophistication, tight coupling to other modules |
| **Business Impact** | 30% | Revenue-critical functionality, user-facing frequency, regulatory/audit relevance, batch cycle criticality, impact if unavailable |

**Composite Score** = (Complexity × 0.35) + (Risk × 0.35) + (Business Impact × 0.30)

---

## Top 10 Hotspot Summary

| Rank | Module | Lines | Type | Complexity | Risk | Business Impact | **Composite** | Domain |
|---|---|---|---|---|---|---|---|---|
| **1** | **COACTUPC** | 4,236 | Online CICS | 10 | 10 | 9 | **9.70** | Account Update |
| **2** | **CBTRN02C** | 731 | Batch | 8 | 9 | 10 | **8.95** | Transaction Posting |
| **3** | **COCRDLIC** | 1,459 | Online CICS | 9 | 7 | 8 | **8.00** | Card List/Browse |
| **4** | **COCRDUPC** | 1,560 | Online CICS | 9 | 8 | 7 | **8.05** | Card Update |
| **5** | **CBACT04C** | 652 | Batch | 8 | 9 | 9 | **8.65** | Interest Calculation |
| **6** | **CBSTM03A** | 924 | Batch | 8 | 7 | 8 | **7.65** | Statement Generation |
| **7** | **COTRN02C** | 783 | Online CICS | 7 | 8 | 8 | **7.65** | Transaction Add |
| **8** | **CBTRN03C** | 649 | Batch | 7 | 6 | 8 | **6.95** | Transaction Report |
| **9** | **COSGN00C** | 260 | Online CICS | 4 | 9 | 10 | **7.55** | Sign-On / Authentication |
| **10** | **COBIL00C** | 572 | Online CICS | 6 | 8 | 9 | **7.60** | Bill Payment |

---

## Detailed Analysis per Module

### #1 — COACTUPC (Account Update) — Score: 9.70

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | 4,236 | Largest program in the entire codebase by 4×+ |
| **Copybook Dependencies** | 16 | CSUTLDWY, CVCRD01Y, CSLKPCDY (51KB lookup table), DFHBMSCA, DFHAID, COTTL01Y, COACTUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVCUS01Y, COCOM01Y, CSSETATY |
| **VSAM Files Accessed** | 4 | ACCTDATA (R/RW), CARDXREF (R), CUSTDATA (R), CARDDATA (R) |
| **BMS Map** | COACTUP (512 lines — largest map) | Extensive field-level input/output |
| **CICS Operations** | READ ×5, REWRITE, SEND MAP, RECEIVE MAP, HANDLE ABEND | Full CRUD on accounts |
| **COPY REPLACING** | 5 instances of CSSETATY REPLACING | Dynamic attribute setting per field |

**Why it's #1:**
- Most complex program by every metric: LOC, copybook count, VSAM file count, and BMS map size
- Performs multi-file updates affecting account balances (financial data integrity risk)
- Embeds a 51 KB lookup code table (CSLKPCDY) — must be externalized to a database table in Java
- Contains extensive field-level validation logic with attribute manipulation via COPY REPLACING
- Any bug in this module directly corrupts financial records

**Migration Considerations:**
- Extract the 51 KB lookup table into a database reference table
- Split into multiple Java service classes (AccountService, ValidationService, etc.)
- Field-level validation logic maps to Bean Validation annotations or a dedicated validator
- COPY REPLACING pattern requires careful translation (no direct Java equivalent)
- Requires comprehensive regression testing due to financial data mutation

---

### #2 — CBTRN02C (Transaction Posting v2) — Score: 8.95

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | 731 | Moderate size but highest business criticality |
| **Copybook Dependencies** | 5 | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |
| **VSAM Files Accessed** | 6 | DALYTRAN (R), TRANSACT (RW), CARDXREF (R), DALYREJS (W), ACCTDATA (R), TCATBALF (RW) |
| **Business Logic** | Transaction validation, posting, reject handling, balance updates |

**Why it's #2:**
- Core of the nightly batch cycle — if this fails, no transactions are processed
- Touches 6 different VSAM files in a single run with coordinated updates
- Updates both transaction master AND account balances AND category balances in one pass
- Generates reject file for failed transactions (audit trail)
- Any defect causes financial discrepancies across multiple data stores

**Migration Considerations:**
- Map to a Spring Batch job with chunk-oriented processing
- Transaction atomicity must be preserved (database transactions replacing VSAM multi-file updates)
- Reject handling maps to a Spring Batch skip/retry policy with error logging
- Consider using database transactions to ensure consistency across the 4 tables being updated
- Critical candidate for parallel integration testing with original system

---

### #3 — COCRDLIC (Card List / Browse) — Score: 8.00

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | 1,459 | Second largest online program |
| **Copybook Dependencies** | 8+ | CVCRD01Y, CVACT02Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDLI, CSDAT01Y, CSMSG01Y, CSUSR01Y, COCOM01Y |
| **CICS Browse Operations** | STARTBR, READNEXT, READPREV, ENDBR | Full forward/backward paging |
| **BMS Map** | COCRDLI (344 lines) | Multi-row list display with selection |

**Why it's #3:**
- Implements complex browse/paging pattern (STARTBR/READNEXT/READPREV/ENDBR)
- This is the most complex UI interaction pattern to translate to REST/web
- Pagination state management across CICS pseudo-conversational interactions
- Gateway to card detail and card update — central to card management workflow

**Migration Considerations:**
- CICS browse pattern maps to JPA paginated queries (`Pageable` / `Page<T>`)
- Pseudo-conversational state maps to server-side session or stateless pagination tokens
- Forward/backward paging via cursor position maps to keyset pagination
- Multi-row display with per-row selection maps to a standard data table component

---

### #4 — COCRDUPC (Card Update) — Score: 8.05

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | 1,560 | Third largest online program |
| **Copybook Dependencies** | 10+ | CVCRD01Y, CVACT02Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, COCOM01Y |
| **CICS Operations** | READ (for update), REWRITE | Modifies card records |
| **Validation** | Extensive field-level validation with attribute manipulation |

**Why it's #4:**
- Modifies sensitive card data (card numbers, CVV, expiration dates, active status)
- PCI-DSS compliance implications — card data must be handled securely in Java
- Complex edit/validate/save cycle with optimistic locking via CICS READ FOR UPDATE
- Tight coupling with COCRDLIC (card list) for navigation flow

**Migration Considerations:**
- PCI-DSS compliance: card numbers must be masked/tokenized in the Java layer
- READ FOR UPDATE pattern maps to JPA optimistic locking (`@Version`)
- Field validation maps to Bean Validation or a card-specific validator service
- Consider separating sensitive card data into a secured microservice

---

### #5 — CBACT04C (Interest Calculation) — Score: 8.65

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | 652 | Core financial calculation engine |
| **Copybook Dependencies** | 5 | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |
| **VSAM Files Accessed** | 5 | TCATBALF (R), CARDXREF (R), DISCGRP (R), ACCTDATA (R/RW), TRANSACT (R) |
| **Business Logic** | Interest rate lookup, interest computation, fee calculation, account balance update |

**Why it's #5:**
- Core financial calculation — directly impacts customer billing
- Joins data across 5 VSAM files: category balances → disclosure groups → accounts
- Interest and fee calculations must produce identical results to penny precision
- COMP-3 packed decimal arithmetic requires careful BigDecimal translation
- Regulatory/audit requirement: interest calculations must be reproducible

**Migration Considerations:**
- All arithmetic must use `BigDecimal` with explicit `RoundingMode` matching COBOL behavior
- COMP-3 packed decimal → BigDecimal conversion requires precision testing
- Map to a Spring Batch job; the 5-file join becomes SQL JOINs or sequential service calls
- **Critical:** Must run parallel testing (COBOL vs. Java) to verify penny-for-penny accuracy
- Consider extracting interest rate rules into a rules engine for maintainability

---

### #6 — CBSTM03A / CBSTM03B (Statement Generation) — Score: 7.65

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | 924 + 230 = 1,154 (combined) | Two-program unit |
| **Copybook Dependencies** | 4 | COSTM01, CVACT03Y, CUSTREC, CVACT01Y |
| **Output Formats** | Plain text AND HTML | Dual-format statement generation |
| **CALL Frequency** | CBSTM03A calls CBSTM03B 11 times | Tight subroutine coupling |
| **VSAM Files Accessed** | 4 | TRXFL (sorted txns), CARDXREF, ACCTDATA, CUSTDATA |

**Why it's #6:**
- Customer-facing output — statements are the primary customer communication
- Generates dual formats (text + HTML) requiring two rendering paths
- Complex page-break logic, multi-account grouping, and address formatting
- The CBSTM03A/CBSTM03B split is an unusual subroutine pattern (11 CALL round-trips)
- Mainframe control block addressing (TIOT) used for unique features

**Migration Considerations:**
- Map to a Spring Batch job with an ItemWriter producing both text and HTML
- Replace text formatting with a template engine (Thymeleaf, FreeMarker)
- Replace HTML generation with proper HTML templates
- The CBSTM03A/CBSTM03B split can be merged into a single Java class
- Consider PDF generation (via TXT2PDF1.JCL successor) as part of the same job

---

### #7 — COTRN02C (Transaction Add) — Score: 7.65

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | 783 | Moderate complexity |
| **Copybook Dependencies** | 8+ | CVCRD01Y, CVTRA05Y, DFHBMSCA, DFHAID, COTTL01Y, COTRN02, CSDAT01Y, CSMSG01Y, CSUSR01Y, COCOM01Y |
| **External CALLs** | CSUTLDTC ×2 | Date validation for transaction dates |
| **CICS Operations** | READ ×2, STARTBR, READPREV, ENDBR, WRITE | Complex read-validate-write flow |

**Why it's #7:**
- Creates new financial transactions — directly impacts account balances
- Date validation via external CALL to CSUTLDTC (which calls LE CEEDAYS)
- Generates transaction IDs using CICS browse to find the last ID and increment
- Read-before-write pattern with cross-reference validation

**Migration Considerations:**
- Transaction ID generation: replace VSAM browse-for-max with database sequence
- Date validation: replace CSUTLDTC/CEEDAYS with `java.time` API
- VSAM WRITE maps to JPA `save()` within a `@Transactional` method
- Cross-reference validation becomes a foreign key constraint or service-level check

---

### #8 — CBTRN03C (Transaction Report) — Score: 6.95

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | 649 | Report generation |
| **Copybook Dependencies** | 5 | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| **VSAM Files Accessed** | 5 | TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM + REPTFILE (output) |
| **Report Features** | Column headers, page breaks, account/page/grand totals, date range filtering |

**Why it's #8:**
- Operational report used for daily reconciliation and audit
- Joins 5 files: transactions → cross-reference → type descriptions → category descriptions
- Complex report layout with multi-level totals (page, account, grand)
- Date range parameter-driven (reads DATEPARM file)

**Migration Considerations:**
- Map to a Spring Batch job or reporting service (JasperReports, BIRT)
- The 5-file join becomes a SQL query with JOINs
- Report layout maps to a report template
- Date range parameters map to REST query parameters or Spring Batch job parameters
- GDG output becomes file system or object storage with versioning

---

### #9 — COSGN00C (Sign-On / Authentication) — Score: 7.55

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | 260 | Small but critical |
| **Copybook Dependencies** | 7 | COCOM01Y, COSGN00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID |
| **VSAM Files Accessed** | 1 | USRSEC (READ) |
| **Security** | Plaintext password comparison, user type routing |

**Why it's #9:**
- **Security gateway** — every user interaction starts here
- Passwords stored and compared in plaintext (CSUSR01Y: `SEC-USR-PWD PIC X(08)`)
- Routes users to different menus based on type (Admin vs. Regular)
- Sets COMMAREA fields used by all downstream programs
- Small code but highest blast radius if broken — locks out all users

**Migration Considerations:**
- **Must not** migrate plaintext password pattern — implement proper hashing (bcrypt/scrypt)
- Map to Spring Security with form-based or JWT authentication
- User type routing maps to role-based access control (`@PreAuthorize`, `hasRole()`)
- COMMAREA session state maps to HTTP session or JWT claims
- Consider adding MFA, account lockout, and password complexity rules
- **First module to migrate** as the security foundation for everything else

---

### #10 — COBIL00C (Bill Payment) — Score: 7.60

| Metric | Value | Notes |
|---|---|---|
| **Lines of Code** | 572 | Moderate |
| **Copybook Dependencies** | 8 | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, DFHAID, DFHBMSCA |
| **CICS Operations** | READ, REWRITE, STARTBR, READPREV, ENDBR, WRITE | Full financial transaction flow |
| **VSAM Files Accessed** | 2+ | ACCTDATA (R/RW), TRANSACT (R/W) |

**Why it's #10:**
- Revenue-critical: processes customer bill payments
- Directly modifies account balances (REWRITE to ACCTDATA)
- Creates transaction records (WRITE to TRANSACT)
- Uses browse to generate transaction IDs (like COTRN02C)
- Financial integrity: balance update + transaction write must be atomic

**Migration Considerations:**
- Must be wrapped in a database transaction for atomicity
- Balance update + transaction creation in a single `@Transactional` method
- Payment validation (sufficient balance, active account) maps to service-layer checks
- Consider adding idempotency keys to prevent duplicate payments
- Audit logging is essential for financial compliance

---

## Risk Heat Map

```
                    LOW Business Impact ←──────────────→ HIGH Business Impact
                    │                                                        │
HIGH Complexity     │  COCRDLIC (#3)        COACTUPC (#1)                   │
                    │  COCRDUPC (#4)        CBTRN02C (#2)                   │
                    │                       CBACT04C (#5)                   │
                    │                                                        │
                    │  CBSTM03A (#6)        COTRN02C (#7)                   │
MEDIUM Complexity   │  CBTRN03C (#8)        COBIL00C (#10)                  │
                    │                                                        │
                    │                                                        │
LOW Complexity      │  UNUSED1Y (dead code) COSGN00C (#9) ← small but      │
                    │  COBSWAIT (utility)   critical security gateway        │
                    │                                                        │
```

---

## Recommended Migration Sequence

Based on the hotspot analysis, we recommend the following migration waves:

### Wave 0: Foundation (Pre-requisite)
| Module | Rationale |
|---|---|
| **COSGN00C** (Authentication) | Security foundation — must be in place before any other module |
| **CSUSR01Y** → User entity | User data model with proper password hashing |
| **COCOM01Y** → Session/Auth context | Replace COMMAREA with HTTP session or JWT |
| **COTTL01Y, CSDAT01Y, CSMSG01Y** → Shared UI components | Common header, date, message infrastructure |

### Wave 1: Read-Only Screens (Low Risk)
| Module | Rationale |
|---|---|
| **COACTVWC** (Account View) | Read-only, validates data access patterns |
| **COCRDSLC** (Card Detail View) | Read-only card display |
| **COTRN01C** (Transaction View) | Read-only transaction display |
| **COMEN01C** / **COADM01C** (Menus) | Simple navigation, establishes routing |

### Wave 2: List/Browse Screens (Medium Risk)
| Module | Rationale |
|---|---|
| **COCRDLIC** (#3 — Card List) | Establishes pagination pattern for all list screens |
| **COTRN00C** (Transaction List) | Reuses pagination pattern from COCRDLIC |
| **COUSR00C–03C** (User CRUD) | Complete CRUD cycle, simpler than account/card |

### Wave 3: Financial Write Operations (High Risk)
| Module | Rationale |
|---|---|
| **COTRN02C** (#7 — Transaction Add) | First write operation, validates transaction creation |
| **COBIL00C** (#10 — Bill Payment) | Financial writes with balance updates |
| **COCRDUPC** (#4 — Card Update) | Card data modification with PCI implications |
| **COACTUPC** (#1 — Account Update) | Most complex — migrate last among online programs |

### Wave 4: Batch Processing (Highest Risk)
| Module | Rationale |
|---|---|
| **CBTRN02C** (#2 — Transaction Posting) | Core batch — requires parallel run testing |
| **CBACT04C** (#5 — Interest Calculation) | Financial precision — penny-for-penny validation |
| **CBSTM03A/B** (#6 — Statement Generation) | Customer-facing output — visual comparison testing |
| **CBTRN03C** (#8 — Transaction Report) | Operational reporting — can run in parallel |
| **CBEXPORT / CBIMPORT** (Data Migration) | Support tools — migrate when needed |

### Wave 5: Optional Modules
| Module | Rationale |
|---|---|
| **Authorization (IMS/DB2/MQ)** | Requires IMS-to-JPA and MQ-to-JMS/Kafka migration |
| **Transaction Type (DB2)** | DB2 SQL can map relatively directly to JPA |
| **VSAM-MQ** | MQ integration maps to Spring JMS or Spring Cloud Stream |

---

## Quick Wins (Low Complexity / High Value)

These modules offer the best return on investment for early migration:

| Module | Lines | Why It's a Quick Win |
|---|---|---|
| **COSGN00C** | 260 | Small, well-defined scope. Establishes security foundation. Maps cleanly to Spring Security. |
| **COMEN01C / COADM01C** | 308/288 | Pure navigation — trivial to implement as REST routing or a web menu. |
| **COTRN01C** (Txn View) | 330 | Read-only single-record display. Simple REST GET endpoint. |
| **COUSR01C** (User Add) | 299 | Single VSAM WRITE. Clean CRUD operation → JPA `save()`. |
| **CSUTLDTC** (Date Utility) | 157 | Replace entirely with `java.time` API — eliminates LE dependency. |
| **COBSWAIT** | 41 | Replace with `Thread.sleep()` or `ScheduledExecutorService`. |
| **Batch Reader Programs** (CBACT01C/02C/03C, CBCUS01C) | 178–430 | Simple read-and-print utilities — trivial Spring Batch reader/writer jobs. |
