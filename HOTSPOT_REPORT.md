# CardDemo Hotspot Report — Top 10 Modernization Priorities

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo (Credit Card Management System)
> **Methodology:** Weighted scoring across code complexity, coupling, business criticality, and modernization risk

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Rankings](#top-10-hotspot-rankings)
3. [Detailed Module Assessments](#detailed-module-assessments)
4. [Risk Heat Map](#risk-heat-map)
5. [Recommended Modernization Sequence](#recommended-modernization-sequence)

---

## Scoring Methodology

Each module is scored on four dimensions (1–10 scale), then combined into a weighted total:

| Dimension | Weight | What It Measures |
|---|---|---|
| **Complexity** | 30% | LOC, control flow (IF/EVALUATE/GO TO), nesting depth, PERFORM count |
| **Coupling** | 20% | Copybook dependencies, VSAM files accessed, inter-program calls |
| **Business Impact** | 30% | Revenue criticality, data sensitivity, user-facing importance |
| **Modernization Risk** | 20% | Legacy patterns (GO TO, ALTER), CICS dependency, data conversion difficulty |

**Total Score** = (Complexity × 0.30) + (Coupling × 0.20) + (Business Impact × 0.30) + (Risk × 0.20)

---

## Top 10 Hotspot Rankings

| Rank | Module | LOC | Total Score | Complexity | Coupling | Business Impact | Risk | Domain |
|---|---|---|---|---|---|---|---|---|
| **1** | COACTUPC | 4,236 | **9.0** | 10 | 10 | 9 | 8 | Account Mgmt |
| **2** | CBTRN02C | 731 | **8.4** | 8 | 9 | 10 | 7 | Transaction Processing |
| **3** | CBACT04C | 652 | **7.8** | 7 | 8 | 9 | 7 | Financial Processing |
| **4** | COCRDLIC | 1,459 | **7.5** | 8 | 7 | 7 | 8 | Card Mgmt |
| **5** | COCRDUPC | 1,560 | **7.4** | 8 | 7 | 7 | 7 | Card Mgmt |
| **6** | CBSTM03A | 924 | **7.2** | 7 | 6 | 8 | 8 | Reporting |
| **7** | COSGN00C | 260 | **7.0** | 3 | 4 | 10 | 8 | Security |
| **8** | COACTVWC | 941 | **6.8** | 6 | 8 | 7 | 5 | Account Mgmt |
| **9** | CBTRN03C | 649 | **6.6** | 7 | 7 | 6 | 6 | Reporting |
| **10** | COBIL00C | 572 | **6.5** | 5 | 6 | 8 | 6 | Bill Payment |

---

## Detailed Module Assessments

### #1 — COACTUPC (Account Update) — Score: 9.0

| Metric | Value | Concern Level |
|---|---|---|
| Lines of Code | 4,236 | CRITICAL — largest program by 3x |
| IF statements | 168 | CRITICAL — extremely high branching |
| GO TO statements | 51 | HIGH — legacy spaghetti flow |
| EVALUATE statements | 10 | Moderate |
| PERFORM statements | 64 | Moderate |
| CICS commands | 17 | HIGH — deep CICS coupling |
| Copybook dependencies | 16+ | CRITICAL — highest in codebase |
| VSAM files accessed | 5 (ACCTDATA, CARDXREF, CUSTDATA, CARDDATA, USRSEC) | HIGH |
| CSSETATY REPLACING | 38 instances | HIGH — dynamic field attribute manipulation |

**Why It's #1:**
- At 4,236 lines, it is **3× larger than the next largest program** and contains more branching logic than any other module
- 51 GO TO statements create non-linear control flow that is extremely difficult to convert to structured Java
- 38 uses of CSSETATY with REPLACING for dynamic BMS field attribute control have no direct Java/web equivalent
- Touches 5 VSAM files (accounts, cards, customers, cross-references, users) — maximum data coupling
- Core business function: modifying account data directly impacts financial records

**Modernization Recommendations:**
1. Decompose into multiple Java service classes (AccountUpdateService, AccountValidationService, AccountFieldMapper)
2. Replace GO TO control flow with structured if/switch and method extraction
3. Map CSSETATY field-level attribute control to frontend form validation logic
4. Consider splitting into sub-screens (account info, card info, customer info) in the modern UI
5. Implement comprehensive unit tests before and after migration — this module has the highest regression risk

---

### #2 — CBTRN02C (Transaction Posting) — Score: 8.4

| Metric | Value | Concern Level |
|---|---|---|
| Lines of Code | 731 | Moderate |
| IF statements | 93 | HIGH — heavy validation logic |
| PERFORM statements | 61 | HIGH |
| VSAM files accessed | 6 (TRANSACT, DALYTRAN, CARDXREF, ACCTDATA, TCATBALF, DALYREJS) | CRITICAL |
| Batch file I/O | Sequential read + VSAM R/W/U | HIGH |

**Why It's #2:**
- **Heart of the batch processing cycle** — posts daily transactions to the master file
- Touches 6 datasets — the most of any batch program
- Updates account balances and category balances as a side effect of posting
- Rejection logic routes invalid transactions to a separate GDG dataset
- Any bug in this program directly corrupts financial data

**Modernization Recommendations:**
1. Convert to Spring Batch job with chunk-oriented processing
2. Implement database transactions (ACID) to replace VSAM file-level locking
3. Add idempotency controls (the current design has no duplicate detection)
4. Separate validation, posting, and balance-update into distinct batch steps
5. Critical path for regression testing — build golden-file test suites from sample data

---

### #3 — CBACT04C (Interest Calculation) — Score: 7.8

| Metric | Value | Concern Level |
|---|---|---|
| Lines of Code | 652 | Moderate |
| IF statements | 86 | HIGH — complex business rules |
| PERFORM statements | 56 | HIGH |
| VSAM files accessed | 5 (TCATBALF, CARDXREF, ACCTDATA, DISCGRP, SYSTRAN) | HIGH |
| Financial calculations | Interest rate × balance | CRITICAL accuracy requirement |

**Why It's #3:**
- Implements **core financial logic** — interest calculation on credit card balances
- Reads disclosure group rates and applies them per transaction category per account
- Rounding and precision errors in decimal conversion (COMP-3 → Java BigDecimal) are a major risk
- Results directly affect customer statements and billing
- Must maintain exact numerical parity with the COBOL version during migration

**Modernization Recommendations:**
1. Use `BigDecimal` exclusively — never `double` or `float` for monetary calculations
2. Document and test every rounding rule (COBOL truncation vs. Java rounding modes)
3. Build parallel-run comparison tests: run both COBOL and Java, diff the outputs
4. Extract interest rate rules into a configurable rules engine
5. Consider this module for early migration to validate financial calculation accuracy

---

### #4 — COCRDLIC (Card List / Browse) — Score: 7.5

| Metric | Value | Concern Level |
|---|---|---|
| Lines of Code | 1,459 | HIGH |
| IF statements | 122 | HIGH |
| GO TO statements | 16 | Moderate |
| EVALUATE statements | 18 | HIGH — complex screen navigation |
| CICS commands | 18 | HIGH — browse operations (STARTBR/READNEXT/READPREV/ENDBR) |
| XCTL transfers | 3 (to COCRDSLC, COCRDUPC, COMEN01C) | Moderate |

**Why It's #4:**
- Implements **scrollable list browsing** — a UI pattern that requires careful re-implementation
- CICS browse operations (STARTBR → READNEXT/READPREV → ENDBR) must map to paginated queries
- 18 EVALUATE statements handle complex keyboard/PF-key navigation logic
- Transfers to Card View and Card Update programs with COMMAREA context passing
- GO TO statements complicate the already complex browse/scroll logic

**Modernization Recommendations:**
1. Convert to paginated REST API with cursor-based pagination (replacing CICS browse)
2. Separate browse logic from screen rendering
3. Map PF-key navigation to standard web UI patterns (pagination controls, search filters)
4. Consider combining list/view/update into a single modern card management page

---

### #5 — COCRDUPC (Card Update) — Score: 7.4

| Metric | Value | Concern Level |
|---|---|---|
| Lines of Code | 1,560 | HIGH |
| IF statements | 148 | CRITICAL — second highest after COACTUPC |
| GO TO statements | 21 | HIGH |
| EVALUATE statements | 16 | HIGH |
| Copybook dependencies | 10+ | HIGH |

**Why It's #5:**
- 148 IF statements — second most in the entire codebase
- Heavy field-level validation for card data updates (expiry, status, embossed name)
- 21 GO TO statements create non-linear flow similar to COACTUPC
- Must handle card status transitions carefully (active/inactive/expired)

**Modernization Recommendations:**
1. Extract validation into a CardValidationService with unit-testable methods
2. Replace GO TO with structured control flow
3. Map to a modern form with client-side + server-side validation
4. Consider shared validation with Card View (COCRDSLC) to reduce duplication

---

### #6 — CBSTM03A (Statement Generation) — Score: 7.2

| Metric | Value | Concern Level |
|---|---|---|
| Lines of Code | 924 | Moderate-High |
| GO TO statements | 15 | HIGH |
| ALTER statements | 4 | CRITICAL — self-modifying code |
| CALL to CBSTM03B | 13 calls | HIGH — tight subroutine coupling |
| Output formats | Text + HTML | Moderate |
| COMP/COMP-3 variables | Extensive | HIGH — binary arithmetic |
| 2D array | Yes | Moderate |

**Why It's #6:**
- Contains **ALTER statements** — the most dangerous legacy COBOL pattern. ALTER dynamically changes GO TO targets at runtime, making static analysis impossible
- Deliberately designed to exercise modernization tooling with challenging patterns
- Tight coupling with CBSTM03B subroutine (13 CALL sites)
- Produces both plaintext and HTML output — dual rendering logic
- Uses mainframe control block addressing and COMP-3 packed decimal arithmetic

**Modernization Recommendations:**
1. **Highest technical difficulty** — ALTER statements require careful manual analysis of all possible GO TO target states
2. Replace ALTER/GO TO with strategy pattern or state machine
3. Convert CBSTM03A + CBSTM03B into a single StatementGenerationService
4. Use a templating engine (Thymeleaf, Freemarker) for HTML output instead of inline HTML generation
5. This module is intentionally complex — prioritize for proof-of-concept modernization testing

---

### #7 — COSGN00C (Sign-On) — Score: 7.0

| Metric | Value | Concern Level |
|---|---|---|
| Lines of Code | 260 | Low |
| CICS commands | 10 | Moderate |
| Security concern | Plaintext password comparison | CRITICAL |
| Gateway function | Entry point for all users | CRITICAL business impact |

**Why It's #7:**
- Despite low complexity, it is the **single entry point** for all application users
- Passwords are stored and compared in **plaintext** (CSUSR01Y: SEC-USR-PWD PIC X(08))
- No session management, password expiry, lockout policy, or audit logging
- Routes to either User Menu or Admin Menu based on user type — critical branching point
- Any modernization must completely replace the authentication model

**Modernization Recommendations:**
1. Replace with Spring Security / OAuth2 / OIDC — do not port the plaintext password model
2. Implement proper password hashing (bcrypt), session management, CSRF protection
3. Add account lockout, password complexity rules, and audit logging
4. Consider this the first module to modernize — it defines the security boundary

---

### #8 — COACTVWC (Account View) — Score: 6.8

| Metric | Value | Concern Level |
|---|---|---|
| Lines of Code | 941 | Moderate |
| IF statements | 57 | Moderate |
| GO TO statements | 9 | Moderate |
| EVALUATE statements | 10 | Moderate |
| Copybook dependencies | 14 | HIGH — second highest |
| VSAM files accessed | 4 (ACCTDATA, CARDDATA, CUSTDATA, CARDXREF) | HIGH |

**Why It's #8:**
- Read-only view but accesses 4 VSAM files to assemble a complete account picture
- 14 copybook dependencies — second highest coupling in the codebase
- Good candidate for early migration as a proof-of-concept (read-only, lower risk)
- Shares many patterns with COACTUPC but without the write complexity

**Modernization Recommendations:**
1. Convert to a REST GET endpoint returning a composite Account DTO
2. Good candidate for **strangler fig pattern** — can run alongside legacy
3. Use as a template/pilot for modernizing the other view programs
4. Implement caching since this is read-only

---

### #9 — CBTRN03C (Transaction Report) — Score: 6.6

| Metric | Value | Concern Level |
|---|---|---|
| Lines of Code | 649 | Moderate |
| IF statements | 75 | HIGH |
| EVALUATE statements | 4 | Low |
| PERFORM statements | 72 | HIGH — many small paragraphs |
| VSAM files accessed | 5 (TRANSACT, CARDXREF, TRANTYPE, TRANCATG, DATEPARM) | HIGH |
| Report output | Fixed-width text with headers/totals | Moderate |

**Why It's #9:**
- Core reporting program — generates the daily transaction report
- Reads 5 files to assemble report data (transactions, cross-refs, type/category descriptions)
- 72 PERFORMs indicate highly paragraphed structure — many small routines
- Fixed-width report layout (CVTRA07Y) needs conversion to modern format (PDF, HTML, CSV)
- Date parameter file (DATEPARM) drives report date range — external configuration

**Modernization Recommendations:**
1. Convert to Spring Batch reader/processor/writer pattern
2. Replace fixed-width output with JasperReports, Apache POI, or HTML/PDF generation
3. Externalize date parameters to application configuration or API parameters
4. Consider real-time reporting dashboard as modern replacement

---

### #10 — COBIL00C (Bill Payment) — Score: 6.5

| Metric | Value | Concern Level |
|---|---|---|
| Lines of Code | 572 | Moderate |
| EVALUATE statements | 9 | Moderate |
| CICS commands | 13 | Moderate-High |
| VSAM operations | READ, REWRITE, WRITE, STARTBR, READPREV, ENDBR | HIGH — full CRUD |
| Financial writes | Updates ACCTDATA, writes TRANSACT | CRITICAL — money movement |

**Why It's #10:**
- **Moves money** — processes bill payments by debiting accounts and creating transaction records
- Performs the most diverse set of VSAM operations of any online program (read, write, rewrite, browse)
- Updates account balance (ACCTDATA REWRITE) and creates transaction records (TRANSACT WRITE)
- Browse operations on cross-reference to find the latest transaction for sequencing
- Financial accuracy and atomicity are critical — partial failures could leave inconsistent data

**Modernization Recommendations:**
1. Implement as a transactional service with database ACID guarantees
2. Add idempotency keys to prevent duplicate payments
3. Implement proper error handling with compensating transactions
4. Add payment amount validation, daily limits, and fraud checks not present in legacy
5. Requires thorough integration testing with account balance verification

---

## Risk Heat Map

```
                    LOW Business Impact ◄──────────► HIGH Business Impact
                    │                                          │
HIGH Complexity     │  COCRDLIC (#4)      COACTUPC (#1)       │
                    │  COCRDUPC (#5)      CBTRN02C (#2)       │
                    │  CBSTM03A (#6)      CBACT04C (#3)       │
                    │                                          │
                    │                                          │
MEDIUM Complexity   │  COACTVWC (#8)      COBIL00C (#10)      │
                    │  CBTRN03C (#9)                           │
                    │                                          │
                    │                                          │
LOW Complexity      │                     COSGN00C (#7)       │
                    │                                          │
                    └──────────────────────────────────────────┘
```

---

## Recommended Modernization Sequence

Based on the hotspot analysis, here is the recommended modernization order balancing risk, value, and learning:

### Phase 1: Foundation & Quick Wins (Weeks 1–4)

| Order | Module | Rationale |
|---|---|---|
| 1 | **COSGN00C** | Replace insecure auth first. Low complexity, high impact. Sets security foundation. |
| 2 | **COACTVWC** | Read-only view. Proves data access patterns. Low risk pilot. |
| 3 | **COTRN01C** | Read-only transaction view. Simple, validates transaction data model. |

### Phase 2: Core Business Logic (Weeks 5–10)

| Order | Module | Rationale |
|---|---|---|
| 4 | **CBTRN02C** | Transaction posting. Core batch process. Validates financial accuracy early. |
| 5 | **CBACT04C** | Interest calculation. Financial precision validation. Parallel-run with legacy. |
| 6 | **COBIL00C** | Bill payment. First online write operation. Tests transactional integrity. |

### Phase 3: Complex UI & CRUD (Weeks 11–16)

| Order | Module | Rationale |
|---|---|---|
| 7 | **COCRDLIC** | Card browsing. Validates pagination pattern for reuse across list screens. |
| 8 | **COCRDUPC** | Card update. Complex validation. Reuses patterns from Phase 2. |
| 9 | **COACTUPC** | Account update. The hardest module — tackle last with accumulated experience. |

### Phase 4: Reporting & Remaining (Weeks 17–20)

| Order | Module | Rationale |
|---|---|---|
| 10 | **CBTRN03C** | Transaction report. Batch reporting modernization. |
| 11 | **CBSTM03A/B** | Statement generation. Most technically challenging (ALTER statements). |
| 12 | Remaining modules | Menu screens, user CRUD, utilities — low complexity, pattern-based. |

### Key Risk Mitigations

1. **Parallel running**: Keep COBOL and Java running side-by-side for financial modules (CBTRN02C, CBACT04C) and compare outputs
2. **Golden-file testing**: Use `app/data/ASCII/` sample data to create regression test suites
3. **Incremental cutover**: Use strangler fig pattern — route traffic to Java services one module at a time
4. **Data migration**: VSAM → RDBMS migration should happen early and be validated independently of program conversion
5. **PII handling**: Customer SSN, DOB, and password data require encryption and access controls in the modern system
