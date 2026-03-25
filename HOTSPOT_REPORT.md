# CardDemo Hotspot Report — Top 10 Modules by Complexity, Risk & Business Impact

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo | **Method:** Static analysis of LOC, cyclomatic indicators, file dependencies, COPY depth, and business criticality

---

## Scoring Methodology

Each module is scored on three axes (1-5 scale each):

| Axis | Criteria | Weight |
|------|----------|--------|
| **Complexity** | Lines of code, number of COPY includes, PERFORM/EVALUATE nesting, number of CICS commands, CALL depth, number of file I/O operations | 40% |
| **Risk** | Data mutation (WRITE/REWRITE/DELETE), number of VSAM files touched, error handling gaps, financial calculations, security-sensitive operations | 35% |
| **Business Impact** | Revenue criticality, data integrity dependence, user-facing frequency, downstream dependencies, regulatory relevance | 25% |

**Weighted Score** = (Complexity × 0.40) + (Risk × 0.35) + (Impact × 0.25)

---

## Top 10 Hotspot Modules

### Rank 1: COACTUPC — Account Update (Online)

| Metric | Value | Detail |
|--------|-------|--------|
| **Lines of Code** | 4,236 | Largest program in the entire codebase |
| **Copybooks Included** | 16 unique (38 CSSETATY expansions) | Highest copybook count |
| **CICS Commands** | 15+ (READ, REWRITE, SEND, RECEIVE, HANDLE ABEND, XCTL) | Complex CICS interaction |
| **Files Accessed** | 3 (ACCTDATA, CUSTDATA, CARDXREF) | R/W on Account + Customer |
| **COPY REPLACING** | 38 instances of CSSETATY REPLACING | Macro-style attribute setting |

| Axis | Score | Rationale |
|------|-------|-----------|
| Complexity | **5** | Largest LOC, deepest copybook nesting, 38 COPY REPLACING expansions, extensive field validation |
| Risk | **5** | REWRITE on both Account and Customer masters — corrupted writes affect all downstream |
| Impact | **5** | Core account modification: balance, limits, customer data. Financial and regulatory impact |
| **Weighted Score** | **5.00** | |

**Modernization Priority:** CRITICAL
**Recommendation:** Decompose into AccountUpdateService and CustomerUpdateService. Extract the 38 CSSETATY expansions into a reusable field-attribute utility. Implement optimistic locking for concurrent REWRITE operations.

---

### Rank 2: CBTRN02C — Transaction Posting (Batch)

| Metric | Value | Detail |
|--------|-------|--------|
| **Lines of Code** | 731 | Core batch processing logic |
| **Copybooks Included** | 5 (CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y) | Multiple entity types |
| **Files Accessed** | 6 (DALYTRAN, TRANSACT, CARDXREF, ACCTDATA, TCATBALF, DALYREJS) | Highest file count in batch |
| **Data Mutations** | WRITE (2 files), REWRITE (2 files) | Multi-file transactional updates |

| Axis | Score | Rationale |
|------|-------|-----------|
| Complexity | **4** | 6-file I/O, cross-reference lookups, balance calculations, reject handling |
| Risk | **5** | Updates Account balance + Category balance + writes Transactions. No SYNCPOINT — failure mid-run leaves partial state |
| Impact | **5** | Core nightly batch: if posting fails, no transactions are recorded, balances are wrong, statements are incorrect |
| **Weighted Score** | **4.60** | |

**Modernization Priority:** CRITICAL
**Recommendation:** Implement as Spring Batch job with chunk-oriented processing and DB transactions. Add idempotency via transaction ID deduplication. Implement compensating transactions for partial failures.

---

### Rank 3: CBACT04C — Interest Calculation (Batch)

| Metric | Value | Detail |
|--------|-------|--------|
| **Lines of Code** | 652 | Financial calculation logic |
| **Copybooks Included** | 5 (CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y) | Multiple reference tables |
| **Files Accessed** | 5 (TCATBALF, CARDXREF, DISCGRP, ACCTDATA, TRANSACT) | Complex joins across files |
| **Data Mutations** | REWRITE (ACCTDATA), WRITE (TRANSACT) | Balance updates + interest entries |

| Axis | Score | Rationale |
|------|-------|-----------|
| Complexity | **4** | Multi-file reads with business logic joining categories to rates to accounts. Sequential file processing with state |
| Risk | **5** | Financial calculations: incorrect interest rates or rounding errors have direct monetary impact. Regulatory exposure |
| Impact | **5** | Revenue-generating: interest is income. Incorrect calculation = financial loss + compliance violation |
| **Weighted Score** | **4.60** | |

**Modernization Priority:** CRITICAL
**Recommendation:** Implement using BigDecimal with explicit rounding mode (HALF_UP). Create an InterestCalculationService with unit tests covering edge cases (zero balance, negative balance, rate boundaries). Parallel-testable with legacy for reconciliation.

---

### Rank 4: CBSTM03A / CBSTM03B — Statement Generation (Batch)

| Metric | Value | Detail |
|--------|-------|--------|
| **Lines of Code** | 924 + 230 = 1,154 | Two programs (caller + subroutine) |
| **Copybooks Included** | 4 (COSTM01, CVACT03Y, CUSTREC, CVACT01Y) | Multiple entity lookups |
| **Files Accessed** | 6 (TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE, STMTFILE, HTMLFILE) | Most complex I/O pattern |
| **Special Features** | CALL subroutine, ALTER verb, HTML generation, dual-format output | Uncommon COBOL patterns |

| Axis | Score | Rationale |
|------|-------|-----------|
| Complexity | **5** | Dual-output (text+HTML), CALL to subroutine, ALTER verb (self-modifying code), inline HTML generation |
| Risk | **4** | Read-only on master files, but ALTER verb makes control flow hard to trace. HTML output could have injection issues |
| Impact | **4** | Customer-facing statements: errors visible to customers. Regulatory requirement for accurate statements |
| **Weighted Score** | **4.40** | |

**Modernization Priority:** HIGH
**Recommendation:** Replace ALTER verb with explicit control flow. Use a template engine (Thymeleaf/FreeMarker) for HTML generation. Separate statement data assembly from rendering. The CALL to CBSTM03B maps naturally to a FileService dependency injection.

---

### Rank 5: COCRDUPC — Card Update (Online)

| Metric | Value | Detail |
|--------|-------|--------|
| **Lines of Code** | 1,560 | Second-largest online program |
| **Copybooks Included** | 10+ including CSUTLDWY, CSSETATY | Date validation + attribute setting |
| **CICS Commands** | 12+ (READ, STARTBR, READPREV, ENDBR, SEND, RECEIVE, REWRITE) | Browse + update pattern |
| **Files Accessed** | 2+ (CARDXREF browse, card data) | Card lifecycle management |

| Axis | Score | Rationale |
|------|-------|-----------|
| Complexity | **4** | Browse-and-select UI pattern, date validation, field-level attribute management |
| Risk | **4** | Card data modification: status changes, expiry updates. PCI-DSS scope |
| Impact | **4** | Card lifecycle operations affect cardholder access. Status change errors block card usage |
| **Weighted Score** | **4.00** | |

**Modernization Priority:** HIGH
**Recommendation:** Implement as CardUpdateService with validation annotations. Card status changes should be event-driven for audit trail. PCI-DSS tokenization for card numbers in transit.

---

### Rank 6: COCRDLIC — Card List (Online)

| Metric | Value | Detail |
|--------|-------|--------|
| **Lines of Code** | 1,459 | Complex browse/pagination logic |
| **CICS Commands** | 10+ (STARTBR, READNEXT, READPREV, ENDBR, SEND, RECEIVE) | Full browse pattern |
| **Files Accessed** | 2 (CARDXREF + CXREF AIX path) | Uses alternate index for browsing |
| **UI Complexity** | Multi-page scrollable list with selection | Most complex UI pattern |

| Axis | Score | Rationale |
|------|-------|-----------|
| Complexity | **4** | VSAM browse with forward/backward pagination, alternate index access, selection handling |
| Risk | **3** | Read-only, but incorrect browse logic could show wrong cards to wrong accounts |
| Impact | **4** | Primary card discovery screen: all card operations start here |
| **Weighted Score** | **3.65** | |

**Modernization Priority:** HIGH
**Recommendation:** Replace VSAM browse with SQL pagination (LIMIT/OFFSET or keyset). The forward/backward STARTBR pattern maps to cursor-based pagination in REST API.

---

### Rank 7: COSGN00C — Sign-on / Login (Online)

| Metric | Value | Detail |
|--------|-------|--------|
| **Lines of Code** | 260 | Compact but critical |
| **CICS Commands** | 8 (READ, SEND, RECEIVE, RETURN, XCTL, ASSIGN) | Session establishment |
| **Files Accessed** | 1 (USRSEC) | User credential lookup |
| **Security** | Plaintext password comparison, role-based XCTL routing | Authentication gateway |

| Axis | Score | Rationale |
|------|-------|-----------|
| Complexity | **2** | Simple READ + compare logic, small program |
| Risk | **5** | Plaintext passwords, no lockout, no encryption. Single point of entry for all users |
| Impact | **5** | Authentication gateway: compromise here = full system access. Every user session starts here |
| **Weighted Score** | **3.85** | |

**Modernization Priority:** CRITICAL (Security)
**Recommendation:** Replace with Spring Security + bcrypt/argon2 password hashing. Add account lockout, MFA, session management, and audit logging. Despite low complexity, the security risk makes this a day-one migration priority.

---

### Rank 8: CBTRN03C — Transaction Report (Batch)

| Metric | Value | Detail |
|--------|-------|--------|
| **Lines of Code** | 649 | Report generation logic |
| **Copybooks Included** | 5 (CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y) | Many reference tables |
| **Files Accessed** | 6 (TRANSACT, XREFFILE, TRANTYPE, TRANCATG, DATE-PARMS, REPORT-FILE) | Most reference data lookups |
| **Output** | Formatted report with headers, detail lines, page/account/grand totals | Complex report formatting |

| Axis | Score | Rationale |
|------|-------|-----------|
| Complexity | **4** | 6-file joins, date range filtering, multi-level totaling (page, account, grand), formatted output |
| Risk | **3** | Read-only on masters, output to report file only. Risk is incorrect reporting, not data corruption |
| Impact | **4** | Daily operational report: used for reconciliation and audit. Errors delay close-of-day |
| **Weighted Score** | **3.65** | |

**Modernization Priority:** MEDIUM
**Recommendation:** Implement with JasperReports or similar. The multi-level break logic (page/account/grand totals) maps well to report group constructs. Date-range parameter handling moves to a report API endpoint.

---

### Rank 9: COBIL00C — Bill Payment (Online)

| Metric | Value | Detail |
|--------|-------|--------|
| **Lines of Code** | 572 | Financial transaction entry |
| **CICS Commands** | 10+ (READ, REWRITE, STARTBR, READPREV, ENDBR, ASKTIME, FORMATTIME) | Time-sensitive operations |
| **Files Accessed** | 3 (TRANSACT, ACCTDATA, CARDXREF) | R/W on Transaction + Account |
| **CALL** | CSUTLDTC (date validation) | Date dependency |

| Axis | Score | Rationale |
|------|-------|-----------|
| Complexity | **3** | Moderate: date validation, balance check, transaction write, account update |
| Risk | **4** | Creates transactions and updates account balance in real-time. Financial impact of double-payment |
| Impact | **4** | Customer-facing payment function. Failed payments = customer complaints + financial exposure |
| **Weighted Score** | **3.60** | |

**Modernization Priority:** HIGH
**Recommendation:** Implement as a transactional service with idempotency keys to prevent duplicate payments. Use database transactions to atomically update balance + insert transaction. Add payment confirmation workflow.

---

### Rank 10: COTRN02C — Transaction Add (Online)

| Metric | Value | Detail |
|--------|-------|--------|
| **Lines of Code** | 783 | Online transaction entry |
| **CICS Commands** | 11 (READ, WRITE, STARTBR, READPREV, ENDBR, SEND, RECEIVE, RETURN) | Full CRUD pattern |
| **Files Accessed** | 3 (TRANSACT, ACCTDATA, CARDXREF) | Write to Transaction master |
| **CALL** | CSUTLDTC (2 calls for date validation) | Dual date validation |

| Axis | Score | Rationale |
|------|-------|-----------|
| Complexity | **3** | Cross-reference lookup, date validation (2 calls), card/account validation, transaction ID generation |
| Risk | **4** | Writes directly to TRANSACT master and reads account for validation. Bad transaction = financial impact |
| Impact | **4** | Online transaction entry: real-time business operations depend on this |
| **Weighted Score** | **3.55** | |

**Modernization Priority:** HIGH
**Recommendation:** Implement as a TransactionService with validation pipeline (card exists → account active → amount valid → date valid). Use event sourcing for transaction creation audit trail.

---

## Summary Ranking Table

| Rank | Program | LOC | Complexity | Risk | Impact | **Weighted Score** | Priority |
|------|---------|-----|-----------|------|--------|-------------------|----------|
| 1 | **COACTUPC** | 4,236 | 5 | 5 | 5 | **5.00** | CRITICAL |
| 2 | **CBTRN02C** | 731 | 4 | 5 | 5 | **4.60** | CRITICAL |
| 3 | **CBACT04C** | 652 | 4 | 5 | 5 | **4.60** | CRITICAL |
| 4 | **CBSTM03A/B** | 1,154 | 5 | 4 | 4 | **4.40** | HIGH |
| 5 | **COCRDUPC** | 1,560 | 4 | 4 | 4 | **4.00** | HIGH |
| 6 | **COSGN00C** | 260 | 2 | 5 | 5 | **3.85** | CRITICAL* |
| 7 | **COCRDLIC** | 1,459 | 4 | 3 | 4 | **3.65** | HIGH |
| 8 | **CBTRN03C** | 649 | 4 | 3 | 4 | **3.65** | MEDIUM |
| 9 | **COBIL00C** | 572 | 3 | 4 | 4 | **3.60** | HIGH |
| 10 | **COTRN02C** | 783 | 3 | 4 | 4 | **3.55** | HIGH |

*COSGN00C ranked 6th by weighted score but flagged CRITICAL due to security risk (plaintext passwords).*

---

## Migration Wave Recommendation

### Wave 1 — Foundation + Security (Weeks 1-4)
- **COSGN00C** — Authentication (security-critical, low complexity = quick win)
- **COUSR00C-03C** — User CRUD (depends on auth, straightforward)
- **COCOM01Y → Session Management** — Replace COMMAREA with HTTP sessions/JWT

### Wave 2 — Core Business Logic (Weeks 5-12)
- **CBTRN02C** — Transaction posting (highest business risk)
- **CBACT04C** — Interest calculation (financial accuracy critical)
- **COBIL00C** — Bill payment (customer-facing financial)
- **COTRN02C** — Transaction add (online entry point for transactions)

### Wave 3 — Account & Card Management (Weeks 13-20)
- **COACTUPC** — Account update (largest, most complex — needs decomposition)
- **COACTVWC** — Account view (simpler, read-only)
- **COCRDUPC** — Card update
- **COCRDLIC / COCRDSLC** — Card list/view

### Wave 4 — Reporting & Data Migration (Weeks 21-28)
- **CBSTM03A/B** — Statement generation
- **CBTRN03C** — Transaction report
- **CORPT00C** — Report request screen
- **CBEXPORT / CBIMPORT** — Data migration utilities

### Wave 5 — Optional Modules (Weeks 29+)
- Authorization module (IMS/DB2/MQ — most complex integration)
- Transaction Type DB2 module
- VSAM-MQ module

---

## Key Risk Factors Across All Modules

| Risk Factor | Affected Programs | Mitigation |
|-------------|-------------------|------------|
| **Plaintext passwords** | COSGN00C, CSUSR01Y | Bcrypt/argon2 hashing on day one |
| **No transaction isolation** | CBTRN02C, CBACT04C, COBIL00C | DB transactions with rollback |
| **PII in flat files** | CBEXPORT, CBIMPORT, CVCUS01Y | Encryption at rest, tokenization |
| **ALTER verb (self-modifying code)** | CBSTM03A | Refactor to explicit control flow |
| **No concurrent access control** | All online CICS REWRITE programs | Optimistic locking (version columns) |
| **FILLER fields** | All copybooks | Document/remove or reserve for extensibility |
| **Hard-coded lookups** | CSLKPCDY (1,318 lines of codes) | Externalize to config/database |
| **No input sanitization** | All BMS map programs | Add validation layer in Java |
