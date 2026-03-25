# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Source:** `uc-legacy-modernization-cobol-to-java`
> **Application:** CardDemo -- Mainframe Credit Card Management System

---

## Scoring Methodology

Each module is scored on three dimensions (1-10 scale):

| Dimension         | Weight | Criteria                                                                 |
|-------------------|--------|--------------------------------------------------------------------------|
| **Complexity**    | 35%    | Lines of code, cyclomatic complexity indicators (EVALUATE/IF nesting), number of copybook includes, CICS commands, file I/O operations, CALL depth |
| **Risk**          | 35%    | Data mutation (WRITE/REWRITE/DELETE), financial calculation logic, multi-file coordination, error handling gaps, PII exposure, concurrent access patterns |
| **Business Impact** | 30% | Revenue criticality, user-facing frequency, downstream dependencies, regulatory implications, batch cycle position |

**Composite Score** = (Complexity x 0.35) + (Risk x 0.35) + (Business Impact x 0.30)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC -- Account Update

| Metric            | Score | Details                                                    |
|-------------------|-------|------------------------------------------------------------|
| **Complexity**    | 10/10 | **4,236 lines** -- largest program in the codebase. 38 COPY CSSETATY REPLACING directives, 15+ copybook includes, 5 CICS READ operations, REWRITE, SEND MAP, RECEIVE MAP. Deep nested EVALUATE/IF logic for field-level validation of 30+ account fields. |
| **Risk**          | 10/10 | Mutates Account master (REWRITE), reads Card XREF + Customer + Card files. Handles financial fields (balance, credit limit, cash limit). Contains full address validation with state/country lookup (CSLKPCDY -- 1,318-line lookup table). Any bug directly corrupts financial records. |
| **Business Impact** | 9/10 | Core account maintenance function used daily by all users. Account data feeds into interest calculation, statement generation, and transaction posting. Changes ripple through entire batch cycle. |
| **Composite**     | **9.7** | |

**Modernization Notes:**
- Highest-priority candidate for decomposition -- split into Account Validation Service, Account Persistence Service, and Account UI Controller
- The 38 CSSETATY COPY REPLACING blocks map to a reusable field-attribute utility in Java
- CSLKPCDY lookup table should be externalized to a database reference table or configuration
- Consider splitting the monolithic screen into multiple UI panels (personal info, financial info, address)

---

### Rank 2: CBTRN02C -- Transaction Posting (Batch)

| Metric            | Score | Details                                                    |
|-------------------|-------|------------------------------------------------------------|
| **Complexity**    | 8/10  | **731 lines**. Reads 6 files (DALYTRAN, TRANFILE, XREFFILE, DALYREJS, ACCTFILE, TCATBALF). Complex validation logic: card existence, account status, credit limit checks, category balance updates. Multi-step PERFORM structure. |
| **Risk**          | 10/10 | **Core financial posting engine.** Writes to 4 files simultaneously (TRANSACT, DALYREJS, ACCTDATA, TCATBALF). Updates account balances and category balances. Rejected transactions go to DALYREJS -- any validation bug means lost or double-posted transactions. No rollback mechanism (batch sequential). |
| **Business Impact** | 10/10 | **Heart of the nightly batch cycle.** Every posted transaction flows through this program. Failure halts the entire batch chain (INTCALC, CREASTMT, TRANREPT all depend on its output). Direct revenue and regulatory impact. |
| **Composite**     | **9.3** | |

**Modernization Notes:**
- Map to a Spring Batch job with chunk-oriented processing and database transactions
- Implement proper rollback/retry semantics (COBOL version has none)
- Critical to implement idempotency checks to prevent double-posting
- Add comprehensive audit logging (COBOL version only writes rejects)

---

### Rank 3: COCRDLIC -- Card List / Browse

| Metric            | Score | Details                                                    |
|-------------------|-------|------------------------------------------------------------|
| **Complexity**    | 9/10  | **1,459 lines**. Complex browse/pagination logic using CICS STARTBR/READNEXT/READPREV/ENDBR. Handles forward and backward scrolling, selection processing, and navigation to detail/update screens. 11 copybook includes. |
| **Risk**          | 7/10  | Read-only for card data, but navigates to COCRDSLC (view) and COCRDUPC (update) via XCTL with context passing. Incorrect context propagation could lead to wrong card being updated. Browse positioning logic is error-prone. |
| **Business Impact** | 8/10 | Primary card lookup screen -- entry point to all card operations. Used heavily by both users and admins. |
| **Composite**     | **8.0** | |

**Modernization Notes:**
- CICS browse pattern (STARTBR/READNEXT/READPREV) maps to paginated REST API with cursor-based pagination
- Selection + navigation pattern maps to a list view with row-click handlers
- Test thoroughly: off-by-one errors in browse positioning are common migration bugs

---

### Rank 4: COCRDUPC -- Card Update

| Metric            | Score | Details                                                    |
|-------------------|-------|------------------------------------------------------------|
| **Complexity**    | 8/10  | **1,560 lines**. 14 copybook includes. HANDLE ABEND, multiple CICS READ operations across Card + Account + Customer files. Field-level validation and CSSETATY attribute manipulation. |
| **Risk**          | 9/10  | Mutates Card master record (REWRITE). Reads across 3 VSAM files for validation. Contains ABEND handling with forced abend on unrecoverable errors. Embossed name, expiration date, status changes are sensitive operations. |
| **Business Impact** | 7/10 | Card maintenance is a core operational function. Status changes (active/inactive) directly affect transaction authorization. |
| **Composite**     | **8.1** | |

**Modernization Notes:**
- Similar structure to COACTUPC but simpler -- good candidate for second migration wave
- Card status changes need business event publishing in the modernized version
- CVV handling requires PCI-DSS compliance review

---

### Rank 5: CBACT04C -- Interest Calculation (Batch)

| Metric            | Score | Details                                                    |
|-------------------|-------|------------------------------------------------------------|
| **Complexity**    | 8/10  | **652 lines**. Reads 5 files (TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT). Complex interest rate lookup by account group + transaction type + category. Decimal arithmetic with S9(09)V99 precision. |
| **Risk**          | 10/10 | **Financial calculation engine.** Writes updated balances to ACCTDATA and TCATBALF. Interest rate miscalculation directly impacts customer billing. Regulatory exposure (Truth in Lending Act compliance). |
| **Business Impact** | 9/10 | Runs nightly after transaction posting. Interest charges are a primary revenue source. Calculation errors have legal and financial consequences. |
| **Composite**     | **9.0** | |

**Modernization Notes:**
- Requires precise decimal arithmetic preservation (use Java BigDecimal, never double/float)
- Interest rate lookup logic (DISCGRP by group+type+category) needs thorough unit testing
- Consider implementing as a separate microservice with full audit trail
- Regulatory compliance testing is mandatory before go-live

---

### Rank 6: CBSTM03A -- Statement Generation (Batch)

| Metric            | Score | Details                                                    |
|-------------------|-------|------------------------------------------------------------|
| **Complexity**    | 8/10  | **924 lines**. CALLs CBSTM03B subroutine 13 times for different I/O operations. Produces both text (PS) and HTML output formats. Complex report formatting with page breaks, headers, totals. Reads 4 VSAM files via subroutine. |
| **Risk**          | 7/10  | Read-only for data files, but generates customer-facing statements. Incorrect formatting or calculation errors appear on official documents. Dual-format output (text + HTML) doubles the testing surface. |
| **Business Impact** | 8/10 | Customer-facing deliverable -- statements are a regulatory requirement. HTML format suggests modernization path to PDF/email delivery. |
| **Composite**     | **7.7** | |

**Modernization Notes:**
- CALL to CBSTM03B is a tight coupling -- merge into a single service or use dependency injection
- HTML generation is rudimentary -- replace with a templating engine (Thymeleaf, Freemarker)
- Consider generating PDFs directly (the TXT2PDF1 JCL job already does text-to-PDF conversion)
- Statement generation is a good candidate for parallel processing (per-card)

---

### Rank 7: COACTVWC -- Account View

| Metric            | Score | Details                                                    |
|-------------------|-------|------------------------------------------------------------|
| **Complexity**    | 7/10  | **941 lines**. 14 copybook includes. Reads Account, Card XREF, Card, and Customer VSAM files. HANDLE ABEND with forced ABEND on errors. Complex screen population logic. |
| **Risk**          | 6/10  | Read-only -- no data mutation. However, displays sensitive financial data (balance, credit limit) and PII (customer info). ABEND handling could mask errors. |
| **Business Impact** | 8/10 | Most frequently used screen -- every user interaction starts with account lookup. High availability requirement. |
| **Composite**     | **7.0** | |

**Modernization Notes:**
- Good candidate for early migration -- read-only, well-defined scope
- Can serve as a template/pattern for migrating other view screens
- Consider API-first approach: Account View API can serve both web UI and mobile

---

### Rank 8: CBTRN03C -- Transaction Report (Batch)

| Metric            | Score | Details                                                    |
|-------------------|-------|------------------------------------------------------------|
| **Complexity**    | 8/10  | **649 lines**. Reads 6 files (TRANSACT, CARDXREF, TRANTYPE, TRANCATG, REPORT-FILE, DATE-PARMS). Complex report formatting with CVTRA07Y layout (headers, detail, page/account/grand totals). Date range filtering from parameter file. |
| **Risk**          | 6/10  | Read-only reporting, but produces official daily transaction reports. Incorrect totals or missing transactions have audit implications. |
| **Business Impact** | 7/10 | Daily operational report used for reconciliation. Regulatory audit trail requirement. |
| **Composite**     | **7.0** | |

**Modernization Notes:**
- Map to a Spring Batch reporting job or a reporting service (JasperReports, etc.)
- Date parameter file pattern maps to job parameters in Spring Batch
- Report output format should be modernized to PDF/CSV with email delivery

---

### Rank 9: COTRN02C -- Transaction Add (Online)

| Metric            | Score | Details                                                    |
|-------------------|-------|------------------------------------------------------------|
| **Complexity**    | 7/10  | **783 lines**. 11 copybook includes. CALLs CSUTLDTC for date conversion. Reads Account + Card XREF VSAM files for validation. STARTBR/READPREV for auto-generating transaction ID. WRITE to TRANSACT file. |
| **Risk**          | 8/10  | Creates new financial transactions (CICS WRITE). Validates card, account status, and generates unique transaction IDs. Any bypass of validation allows fraudulent transactions. |
| **Business Impact** | 8/10 | Direct transaction creation -- core revenue operation. Every manually entered transaction passes through this screen. |
| **Composite**     | **7.6** | |

**Modernization Notes:**
- Transaction ID generation (READPREV to find last ID, then increment) must be made thread-safe in Java
- Consider using database sequences or UUIDs for transaction IDs
- Validation logic should be extracted into a shared Transaction Validation Service (reused by batch posting)

---

### Rank 10: COBIL00C -- Bill Payment (Online)

| Metric            | Score | Details                                                    |
|-------------------|-------|------------------------------------------------------------|
| **Complexity**    | 7/10  | **572 lines**. 11 copybook includes. ASKTIME/FORMATTIME for timestamp generation. Reads Account + Card XREF + Transaction files. REWRITE Account (balance update) + WRITE Transaction (payment record). STARTBR/READPREV for ID generation. |
| **Risk**          | 9/10  | **Financial mutation**: updates account balance (REWRITE) and creates a payment transaction (WRITE). Incorrect amount handling or duplicate payments directly affect customer accounts. No duplicate payment detection. |
| **Business Impact** | 8/10 | Customer payment channel -- directly affects receivables. Payment processing failures have immediate customer impact. |
| **Composite**     | **7.9** | |

**Modernization Notes:**
- Must implement idempotency (duplicate payment detection) in the modernized version
- Account balance update + transaction write should be in a single database transaction
- Consider integrating with a payment gateway for real payment processing
- ASKTIME/FORMATTIME maps to Java `Instant.now()` / `DateTimeFormatter`

---

## Summary Heat Map

```
                    Complexity  Risk  Business Impact  Composite
                    ──────────  ────  ───────────────  ─────────
 1. COACTUPC        ██████████  ██████████  █████████   9.7  ■■■ CRITICAL
 2. CBTRN02C        ████████░░  ██████████  ██████████  9.3  ■■■ CRITICAL
 3. CBACT04C        ████████░░  ██████████  █████████░  9.0  ■■■ CRITICAL
 4. COCRDUPC        ████████░░  █████████░  ███████░░░  8.1  ■■  HIGH
 5. COCRDLIC        █████████░  ███████░░░  ████████░░  8.0  ■■  HIGH
 6. COBIL00C        ███████░░░  █████████░  ████████░░  7.9  ■■  HIGH
 7. CBSTM03A        ████████░░  ███████░░░  ████████░░  7.7  ■■  HIGH
 8. COTRN02C        ███████░░░  ████████░░  ████████░░  7.6  ■■  HIGH
 9. COACTVWC        ███████░░░  ██████░░░░  ████████░░  7.0  ■   MEDIUM
10. CBTRN03C        ████████░░  ██████░░░░  ███████░░░  7.0  ■   MEDIUM
```

---

## Recommended Migration Waves

### Wave 1: Foundation & Low-Risk (Weeks 1-4)
- **COACTVWC** (Account View) -- Read-only, establishes the migration pattern
- **COSGN00C** (Sign-On) -- Simple, establishes security infrastructure
- **COMEN01C / COADM01C** (Menus) -- Navigation framework
- **COUSR00C-03C** (User CRUD) -- Simple CRUD, establishes admin patterns

### Wave 2: Card Management (Weeks 5-8)
- **COCRDLIC** (Card List) -- Establishes browse/pagination pattern
- **COCRDSLC** (Card View) -- Read-only detail
- **COCRDUPC** (Card Update) -- First mutation with validation
- **COTRN00C / COTRN01C** (Transaction List/View) -- Read-only browsing

### Wave 3: Financial Operations -- HIGH RISK (Weeks 9-14)
- **COACTUPC** (Account Update) -- Largest program, critical financial data
- **COTRN02C** (Transaction Add) -- Online transaction creation
- **COBIL00C** (Bill Payment) -- Financial mutations
- **CORPT00C** (Reports) -- Online report request

### Wave 4: Batch Processing -- HIGHEST RISK (Weeks 15-22)
- **CBTRN02C** (Transaction Posting) -- Core batch engine
- **CBACT04C** (Interest Calculation) -- Financial calculations
- **CBTRN01C** (Transaction Validation) -- Pre-posting validation
- **CBTRN03C** (Transaction Report) -- Batch reporting
- **CBSTM03A/B** (Statement Generation) -- Customer-facing output
- **CBEXPORT / CBIMPORT** (Data Migration) -- Bulk data operations

### Wave 5: Optional Modules (Weeks 23-28)
- Authorization Module (IMS/DB2/MQ integration)
- Transaction Type DB2 Module
- VSAM-MQ Module

---

## Key Risk Mitigations

| Risk Area                     | Mitigation Strategy                                              |
|-------------------------------|------------------------------------------------------------------|
| Financial calculation drift   | Parallel-run: execute both COBOL and Java for 2+ billing cycles and compare results field-by-field |
| Transaction double-posting    | Implement idempotency keys and exactly-once processing semantics |
| Data type precision loss      | Use `BigDecimal` exclusively for all monetary fields; never use `double` or `float` |
| Browse/pagination bugs        | Comprehensive cursor-based pagination tests with boundary conditions (empty, single-page, multi-page, last-page) |
| PII data exposure             | Encrypt SSN, DOB at rest; implement field-level access control in Java layer |
| Batch sequence dependencies   | Implement workflow orchestration (Spring Batch Flow) to enforce job ordering and error handling |
| Password plain-text storage   | Hash all passwords (bcrypt/Argon2) during migration; implement proper authentication |
| VSAM key generation           | Replace sequential key generation with database sequences or UUID to ensure thread safety |
