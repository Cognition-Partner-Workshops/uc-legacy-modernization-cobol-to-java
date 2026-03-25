# CardDemo Hotspot Report — Top 10 Modules

> **Generated**: 2026-03-25 | **Methodology**: Weighted scoring across code complexity, dependency fan-out, data risk, and business criticality

---

## Scoring Methodology

Each module is scored on four dimensions (1-5 scale each, 5 = highest risk):

| Dimension | Weight | What It Measures |
|-----------|--------|------------------|
| **Complexity** | 30% | Lines of code, cyclomatic complexity indicators (EVALUATE/IF nesting, PERFORM branches), number of COPY inclusions |
| **Dependency Risk** | 25% | Fan-out (files touched, programs called), fan-in (how many other modules depend on it), coupling to shared data structures |
| **Business Impact** | 25% | Revenue impact if this module fails, regulatory exposure (PCI-DSS, PII), customer-facing criticality |
| **Migration Difficulty** | 20% | CICS API usage density, VSAM I/O patterns, screen map complexity, use of assembler calls or platform-specific features |

**Composite Score** = (Complexity × 0.30) + (Dependency Risk × 0.25) + (Business Impact × 0.25) + (Migration Difficulty × 0.20)

---

## Top 10 Hotspot Ranking

| Rank | Program | Type | LOC | Composite Score | Priority |
|------|---------|------|-----|----------------|----------|
| 1 | **COACTUPC** | Online CICS | 4,236 | **4.55** | CRITICAL |
| 2 | **CBTRN02C** | Batch | 731 | **4.30** | CRITICAL |
| 3 | **CBACT04C** | Batch | 652 | **4.20** | CRITICAL |
| 4 | **CBSTM03A** | Batch | 924 | **4.00** | HIGH |
| 5 | **COCRDLIC** | Online CICS | 1,459 | **3.85** | HIGH |
| 6 | **COCRDUPC** | Online CICS | 1,560 | **3.80** | HIGH |
| 7 | **COTRN02C** | Online CICS | 783 | **3.70** | HIGH |
| 8 | **COBIL00C** | Online CICS | 572 | **3.65** | HIGH |
| 9 | **COSGN00C** | Online CICS | 260 | **3.50** | MEDIUM |
| 10 | **CBTRN03C** | Batch | 649 | **3.40** | MEDIUM |

---

## Detailed Analysis

### #1 — COACTUPC (Account Update) — Score: 4.55 CRITICAL

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 5 | **4,236 lines** — by far the largest program (3x next largest). Deep EVALUATE/IF nesting for field validation. 15 copybook inclusions. Complex edit-map-process-send cycle with multiple screen states. |
| Dependency Risk | 5 | Touches **6 VSAM files** (ACCTDATA, CARDXREF, CUSTDATA + lookups). Uses CSLKPCDY (lookup codes), CSUTLDWY (date utility). 14 copybooks — highest fan-out of any program. |
| Business Impact | 4 | Account updates directly affect balances, credit limits, and account status. Errors could cause incorrect billing or block legitimate customers. |
| Migration Difficulty | 5 | Dense CICS API usage (HANDLE ABEND, RECEIVE MAP, SEND MAP, XCTL, READ/REWRITE VSAM). Complex BMS map (COACTUP, 245 lines). Multi-phase screen flow with edit validation. |

**Modernization Recommendations**:
- Decompose into multiple Java service classes: `AccountValidationService`, `AccountUpdateService`, `AccountScreenController`
- Extract field validation logic into a reusable validation framework
- Map the multi-phase screen flow to a REST API with proper state management
- Priority: Migrate first to establish patterns for other CICS programs

---

### #2 — CBTRN02C (Transaction Posting) — Score: 4.30 CRITICAL

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 4 | **731 lines**. Multi-step validation pipeline: read daily transaction → validate against XREF → check account → post to master → update category balance → write rejects. |
| Dependency Risk | 5 | Reads/writes **6 files**: DALYTRAN (in), TRANSACT (out), CARDXREF, ACCTDATA, TCATBALF, DALYREJS. Central hub of the nightly batch cycle. |
| Business Impact | 5 | **Core financial posting engine**. Every transaction in the system flows through this program. Posting errors directly impact customer balances, statements, and interest calculations downstream. |
| Migration Difficulty | 4 | Sequential file I/O mixed with VSAM KSDS. Multi-file update logic with error handling. Reject file management. Must maintain transactional integrity in modernized version. |

**Modernization Recommendations**:
- Map to a Spring Batch job with chunk-oriented processing
- Implement database transactions to replace multi-file VSAM updates
- Create a `TransactionValidationService` for the validation pipeline
- Add proper error handling/retry vs. current reject-file approach
- Priority: Migrate early — all downstream batch depends on this

---

### #3 — CBACT04C (Interest Calculation) — Score: 4.20 CRITICAL

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 4 | **652 lines**. Complex business logic: iterate category balances → lookup disclosure group rates → compute interest per category → compute fees → update account. Financial arithmetic with S9(09)V99 precision. |
| Dependency Risk | 4 | Reads **5 files**: TCATBALF, CARDXREF, DISCGRP, ACCTDATA, TRANSACT. Updates ACCTDATA (balance) and writes to SYSTRAN GDG (interest transactions). |
| Business Impact | 5 | **Direct revenue impact** — interest calculation is the primary revenue driver for a credit card operation. Errors compound over time and affect every customer. Regulatory exposure for incorrect interest rates. |
| Migration Difficulty | 4 | COBOL decimal arithmetic (COMPUTE with S9V99) must map precisely to Java BigDecimal. Disclosure group rate lookup logic is intricate. Must preserve exact rounding behavior. |

**Modernization Recommendations**:
- Use `BigDecimal` exclusively — never `double` or `float` for financial calculations
- Implement `InterestCalculationService` with configurable rate lookup
- Add comprehensive unit tests with penny-precise assertions
- Consider making rates table-driven (DB) rather than file-driven
- Priority: Migrate with extreme care — financial accuracy is non-negotiable

---

### #4 — CBSTM03A (Statement Generation) — Score: 4.00 HIGH

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 5 | **924 lines**. Generates both plain text and HTML statements. Complex report formatting with headers, detail lines, page breaks, account totals. Uses mainframe control block addressing and calls subroutine CBSTM03B. |
| Dependency Risk | 4 | Reads 4 VSAM files (TRANSACT, CARDXREF, CUSTDATA, ACCTDATA) via CBSTM03B. Writes HTML and text output files. Calls CBSTM03B subroutine (tight coupling). |
| Business Impact | 3 | Customer-facing statements. Errors are visible but not financially damaging (read-only operation). Statement delivery is a regulatory requirement. |
| Migration Difficulty | 5 | Mainframe-specific features: TIOT control block addressing, dual-format output (text + HTML), CALL to subroutine with shared USING area. JCL SORT pre-processing step. |

**Modernization Recommendations**:
- Replace with a modern templating engine (Thymeleaf, JasperReports)
- Merge CBSTM03A and CBSTM03B into a single `StatementGenerationService`
- Generate PDF directly instead of text + HTML
- Map to Spring Batch with `ItemReader` (sorted transactions) and `ItemWriter` (statement output)

---

### #5 — COCRDLIC (Card List) — Score: 3.85 HIGH

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 4 | **1,459 lines**. Implements forward/backward paging through VSAM BROWSE operations (STARTBR, READNEXT, READPREV, ENDBR). Different behavior for admin vs. regular users. 11 copybook inclusions. |
| Dependency Risk | 3 | Reads CARDDATA.VSAM. XCTL to COCRDSLC (view) and COCRDUPC (update). Fan-in from COMEN01C menu. |
| Business Impact | 3 | Card listing is a core navigation screen. If broken, users cannot access card details or updates. |
| Migration Difficulty | 5 | VSAM BROWSE operations (STARTBR/READNEXT/READPREV) are the hardest CICS patterns to modernize. Cursor-based paging must map to SQL LIMIT/OFFSET or keyset pagination. Role-based filtering logic. |

**Modernization Recommendations**:
- Implement as a paginated REST endpoint with Spring Data JPA
- Replace VSAM BROWSE with SQL `ORDER BY ... LIMIT ... OFFSET`
- Separate admin vs. regular user logic into role-based query scoping
- Consider this a template for all list screens (COTRN00C, COUSR00C)

---

### #6 — COCRDUPC (Card Update) — Score: 3.80 HIGH

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 4 | **1,560 lines**. Multi-phase edit-validate-update cycle similar to COACTUPC but for card data. Field-by-field validation with error messaging. |
| Dependency Risk | 3 | Reads/writes CARDDATA.VSAM. Reads CUSTDATA for customer info display. 13 copybook inclusions. |
| Business Impact | 4 | Card updates affect active status, embossed name, expiration — directly impacts card usability. PCI-DSS data (card numbers) involved. |
| Migration Difficulty | 4 | Same CICS MAP/RECEIVE pattern as COACTUPC. Must handle PCI-DSS compliance for card data in modernized system. |

**Modernization Recommendations**:
- Follow same decomposition pattern as COACTUPC
- Implement PCI-DSS compliant card data handling (tokenization, field-level encryption)
- Add audit logging for all card modifications

---

### #7 — COTRN02C (Transaction Add) — Score: 3.70 HIGH

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 4 | **783 lines**. Online transaction entry with field validation, account lookup, card verification, and VSAM write. Generates unique transaction IDs. |
| Dependency Risk | 3 | Writes to TRANSACT.VSAM. Reads CARDXREF, ACCTDATA for validation. |
| Business Impact | 5 | **New transaction creation** — this is how money moves. Validation errors could allow invalid transactions or block legitimate ones. |
| Migration Difficulty | 3 | Standard CICS MAP/SEND/RECEIVE pattern. VSAM WRITE maps well to database INSERT. |

**Modernization Recommendations**:
- Implement as a REST POST endpoint with request validation
- Add idempotency key to prevent duplicate transactions
- Implement optimistic locking for concurrent access

---

### #8 — COBIL00C (Bill Payment) — Score: 3.65 HIGH

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 3 | **572 lines**. Pay account balance — reads account, validates, creates payment transaction, updates balance. Uses STARTBR/READPREV for transaction history lookup. |
| Dependency Risk | 3 | Reads/writes ACCTDATA, TRANSACT, CARDXREF. Uses BROWSE operations on TRANSACT. |
| Business Impact | 5 | **Direct financial operation** — bill payment affects account balances. Errors could result in incorrect payments or double charges. Customer trust impact. |
| Migration Difficulty | 3 | VSAM BROWSE for transaction lookup is complex. Balance update requires transaction integrity. |

**Modernization Recommendations**:
- Implement as an atomic database transaction (payment record + balance update)
- Add payment confirmation/receipt generation
- Consider async processing for large payments

---

### #9 — COSGN00C (Sign-on) — Score: 3.50 MEDIUM

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 2 | **260 lines**. Relatively simple: receive credentials → read USRSEC → validate → route to appropriate menu. |
| Dependency Risk | 3 | Gateway to entire application. Every user session starts here. Routes to COMEN01C or COADM01C. |
| Business Impact | 4 | Security gate. If bypassed or broken, entire application is exposed. Plaintext password comparison is a major security risk. |
| Migration Difficulty | 4 | Must be completely reimplemented with modern auth (OAuth2/JWT, password hashing). Session management changes from CICS pseudo-conversational to stateless tokens. |

**Modernization Recommendations**:
- Replace with Spring Security + JWT token-based authentication
- Implement bcrypt/scrypt password hashing (never store plaintext)
- Add session management, CSRF protection, rate limiting
- Consider integration with enterprise SSO/LDAP

---

### #10 — CBTRN03C (Transaction Report) — Score: 3.40 MEDIUM

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | 4 | **649 lines**. Multi-file report with page headers, detail lines, page/account/grand totals. Lookup joins across 5 files (transactions, XREF, types, categories, date parameters). |
| Dependency Risk | 3 | Reads 5 files. Writes report output. Triggered by CORPT00C via internal reader (online-to-batch bridge). |
| Business Impact | 3 | Operational reporting. Required for reconciliation and audit but not customer-facing. |
| Migration Difficulty | 3 | Multi-file lookup logic maps to SQL JOINs. Report formatting maps to modern reporting tools. |

**Modernization Recommendations**:
- Replace with SQL-based reporting (JOINs replace multi-file lookups)
- Use JasperReports or similar for formatted output
- Map to Spring Batch job triggered via REST API (replace TDQ/internal reader)

---

## Migration Priority Matrix

```
                    HIGH Business Impact
                         │
            ┌────────────┼────────────┐
            │  COBIL00C  │ COACTUPC   │
            │  COTRN02C  │ CBTRN02C   │
            │            │ CBACT04C   │
            │            │            │
  LOW  ─────┼────────────┼────────────┼───── HIGH
  Complexity│            │            │  Complexity
            │  COSGN00C  │ COCRDLIC   │
            │            │ COCRDUPC   │
            │            │ CBSTM03A   │
            │  CBTRN03C  │            │
            └────────────┼────────────┘
                         │
                    LOW Business Impact
```

---

## Recommended Migration Waves

### Wave 1 — Foundation (Weeks 1-4)
| Module | Rationale |
|--------|-----------|
| **COSGN00C** | Security gate — must be modernized first. Establishes auth patterns. |
| **COMEN01C / COADM01C** | Navigation hub — establishes routing and menu patterns. |
| **CSUSR01Y / COCOM01Y** | Shared data structures — must be converted to Java POJOs first. |

### Wave 2 — Core Financial (Weeks 5-10)
| Module | Rationale |
|--------|-----------|
| **CBTRN02C** | Transaction posting — core batch pipeline. Highest data dependency. |
| **CBACT04C** | Interest calculation — revenue-critical, requires financial precision testing. |
| **COBIL00C** | Bill payment — customer-facing financial operation. |
| **COTRN02C** | Transaction add — how transactions enter the system. |

### Wave 3 — Account & Card Management (Weeks 11-16)
| Module | Rationale |
|--------|-----------|
| **COACTUPC** | Largest program — most complex but patterns established in Wave 2. |
| **COCRDLIC / COCRDUPC** | Card management — follows account patterns. |
| **COACTVWC / COCRDSLC** | View screens — simpler read-only versions of update screens. |

### Wave 4 — Reporting & Utilities (Weeks 17-20)
| Module | Rationale |
|--------|-----------|
| **CBSTM03A / CBSTM03B** | Statement generation — modernize to PDF/template engine. |
| **CBTRN03C** | Transaction report — replace with SQL-based reporting. |
| **CBEXPORT / CBIMPORT** | Data migration utilities — may not be needed post-modernization. |
| **COUSR00C-03C** | User management CRUD — straightforward once patterns exist. |

---

## Key Risk Mitigations

| Risk | Mitigation |
|------|-----------|
| Financial calculation precision loss | Use `BigDecimal` everywhere. Create regression test suite with penny-precise expected values from current mainframe output. |
| VSAM BROWSE → SQL paging mismatch | Implement keyset pagination (not OFFSET) to match VSAM sequential access semantics. |
| Plaintext passwords in USRSEC | Hash existing passwords during migration. Force password reset for all users. |
| PCI-DSS card data exposure | Implement tokenization layer. Never log or display full card numbers. |
| Batch cycle ordering dependencies | Implement job orchestration (Spring Batch + Spring Cloud Data Flow) to enforce step dependencies. |
| CICS pseudo-conversational → REST | Map COMMAREA state to server-side session or JWT claims. Ensure idempotent operations. |
