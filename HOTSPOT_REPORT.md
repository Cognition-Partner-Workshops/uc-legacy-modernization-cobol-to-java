# CardDemo Hotspot Report — Top 10 Modules by Complexity, Risk & Business Impact

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Methodology:** Weighted scoring of Lines of Code, cyclomatic complexity indicators, file I/O breadth, validation density, copybook coupling, and business criticality.

---

## Scoring Methodology

Each module is scored across three dimensions (1–10 scale each):

| Dimension | Weight | Factors |
|-----------|--------|---------|
| **Complexity** | 40% | Lines of code, nesting depth, number of EVALUATE/IF branches, validation routines, number of copybooks included, PERFORM/paragraph count |
| **Risk** | 30% | Number of VSAM files accessed, write/rewrite/delete operations, external dependencies (MQ, DB2, IMS), error handling patterns, data sensitivity (SSN, passwords) |
| **Business Impact** | 30% | Centrality to business workflows, number of callers/dependents, financial data handling, user-facing frequency, batch cycle criticality |

**Composite Score** = (Complexity × 0.4) + (Risk × 0.3) + (Business Impact × 0.3)

---

## Top 10 Hotspot Ranking

| Rank | Program | LOC | Composite | Complexity | Risk | Impact | Domain |
|------|---------|-----|-----------|-----------|------|--------|--------|
| **1** | COACTUPC | 4,237 | **9.4** | 10 | 9 | 9 | Account Update |
| **2** | CBTRN02C | 731 | **8.5** | 8 | 9 | 9 | Transaction Posting |
| **3** | COCRDUPC | 1,560 | **8.0** | 9 | 8 | 7 | Card Update |
| **4** | COCRDLIC | 1,460 | **7.5** | 8 | 7 | 8 | Card Listing |
| **5** | CBACT04C | 652 | **7.4** | 7 | 8 | 8 | Interest Calc |
| **6** | CBSTM03A | 924 | **7.1** | 8 | 7 | 7 | Statement Gen |
| **7** | COTRN02C | 783 | **7.0** | 7 | 8 | 7 | Transaction Add |
| **8** | CBTRN03C | 649 | **6.8** | 7 | 7 | 7 | Transaction Report |
| **9** | COTRN00C | 699 | **6.5** | 7 | 6 | 7 | Transaction List |
| **10** | COBIL00C | 572 | **6.4** | 6 | 7 | 7 | Bill Payment |

---

## Detailed Analysis

### #1 — COACTUPC (Account Update) — Score: 9.4

| Metric | Value | Detail |
|--------|-------|--------|
| Lines of Code | **4,237** | Largest program in the entire codebase |
| Copybooks Included | **17** | Highest coupling of any program |
| VSAM Files Accessed | **4** | CXACAIX, ACCTDAT (R/W), CUSTDAT, CARDDAT |
| Validation Routines | **15+** | SSN, phone, dates, credit limit, FICO, ZIP, state, status |
| Nesting Depth | **6+ levels** | Deeply nested IF/EVALUATE with flag-based flow control |

**Complexity Drivers:**
- Extensive field-level validation: SSN format (3-part), phone numbers, date of birth (not in future), credit limits, FICO score range (300–850), state codes, account status
- Date validation via `CSUTLDPY` copybook which itself contains ~400 lines of date logic including leap year checks
- Multiple edit flags (`FLG-*-NOT-OK`) controlling screen attribute highlighting
- Screen attribute manipulation via `CSSETATY` copybook
- PF key handling via `CSSTRPFY` copybook
- Rewrite operations on ACCTDAT (financial data mutation)

**Risk Factors:**
- Writes to account master (financial balance data)
- Handles sensitive PII: SSN, date of birth, credit scores
- Complex state machine with `CDEMO-PGM-CONTEXT` (enter vs. re-enter)
- Largest blast radius for bugs — affects account balances

**Modernization Recommendation:** Decompose into separate services: AccountValidationService, AccountPersistenceService, ScreenPresentationController. Extract validation rules into a rules engine or annotation-based validators.

---

### #2 — CBTRN02C (Transaction Posting) — Score: 8.5

| Metric | Value | Detail |
|--------|-------|--------|
| Lines of Code | **731** | Medium-large batch program |
| Input Files | **3** | DALYTRAN, XREFFILE, ACCTFILE |
| Output Files | **3** | TRANSACT, DALYREJS, TCATBALF |
| Copybooks | **6** | CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA05Y, CVTRA06Y |

**Complexity Drivers:**
- Cross-reference validation (card → account lookup)
- Balance calculations with signed decimal arithmetic
- Category balance accumulation (TCATBALF updates)
- Rejected transaction handling (DALYREJS output)
- Multi-file coordination with sequential read/write

**Risk Factors:**
- **Core batch cycle program** — failure halts the entire nightly batch
- Writes to 3 different files simultaneously
- Financial calculation accuracy (balance updates)
- No transaction rollback capability — partial failures leave inconsistent state
- Rejection logic must be comprehensive to prevent bad data

**Modernization Recommendation:** Convert to Spring Batch with chunk-based processing. Add database transactions for atomicity. Implement dead-letter queue for rejected transactions.

---

### #3 — COCRDUPC (Credit Card Update) — Score: 8.0

| Metric | Value | Detail |
|--------|-------|--------|
| Lines of Code | **1,560** | Second largest online program |
| Copybooks | **12** | Heavy coupling |
| VSAM Files | **2** | CARDDAT (R/W), CUSTDAT |
| Validation | Multiple | Card number, expiry date, CVV, status, embossed name |

**Complexity Drivers:**
- Complex screen state management (view → edit → confirm)
- Card number validation
- Expiry date validation and business rules
- PF key routing with multiple paths
- Screen attribute manipulation for field highlighting

**Risk Factors:**
- Writes to card master data (sensitive: card numbers, CVV)
- PCI-DSS implications for card data handling
- State transition bugs could expose card data

**Modernization Recommendation:** Implement as a secured REST API with PCI-DSS compliant field encryption. Separate read and update operations.

---

### #4 — COCRDLIC (Credit Card List) — Score: 7.5

| Metric | Value | Detail |
|--------|-------|--------|
| Lines of Code | **1,460** | Third largest online program |
| Copybooks | **10** | |
| VSAM Files | **1** | CARDDAT (via CARDAIX alt index) |
| Browse Operations | Forward + Backward | STARTBR, READNEXT, READPREV |

**Complexity Drivers:**
- Paginated browse with bidirectional scrolling (PF7/PF8)
- 7 rows per screen with dynamic formatting
- Alternate index browsing (CARDAIX by account)
- Row selection routing (S → detail view, U → update)
- Admin vs. user access filtering

**Risk Factors:**
- Displays card numbers on screen (PCI concern)
- Browse positioning logic is error-prone
- Multiple exit points to different programs

**Modernization Recommendation:** Convert to paginated REST API with server-side cursor management. Mask card numbers in display (show last 4 digits only).

---

### #5 — CBACT04C (Interest Calculation) — Score: 7.4

| Metric | Value | Detail |
|--------|-------|--------|
| Lines of Code | **652** | Medium batch program |
| Input Files | **5** | TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT |
| Output Updates | **2** | TCATBALF, ACCTFILE |
| Copybooks | **5** | CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA02Y, CVTRA05Y |

**Complexity Drivers:**
- Multi-file lookup chain: Account → Group → Discount Rate → Category Balance
- Interest rate calculation with decimal precision requirements
- Accumulation logic across transaction categories
- Multi-file update coordination

**Risk Factors:**
- **Financial calculation accuracy** — incorrect interest = regulatory/legal risk
- Updates account balances (ACCTFILE) — directly affects customer bills
- Reads from 5 separate files — data consistency dependency
- No audit trail for calculated interest

**Modernization Recommendation:** Implement as a dedicated financial calculation service with BigDecimal precision. Add audit logging for all interest calculations. Unit test every rate scenario.

---

### #6 — CBSTM03A (Statement Generation) — Score: 7.1

| Metric | Value | Detail |
|--------|-------|--------|
| Lines of Code | **924** | Large batch program |
| Calls to CBSTM03B | **13** | Heavy subroutine coupling |
| Output Formats | **2** | Plain text (STMTFILE) + HTML (HTMLFILE) |
| Copybooks | **4** | COSTM01, CUSTREC, CVACT01Y, CVACT03Y |

**Complexity Drivers:**
- Dual-format output generation (text + HTML)
- 13 separate calls to CBSTM03B subroutine for file I/O
- Account-level aggregation with page breaks
- HTML generation with embedded formatting
- Customer and transaction data correlation

**Risk Factors:**
- Statement accuracy directly customer-facing
- HTML generation may have injection concerns
- Subroutine interface coupling (WS-M03B-AREA)
- Large output volume during batch runs

**Modernization Recommendation:** Use a template engine (Thymeleaf/Razor) for statement generation. Separate data gathering from formatting. Consider PDF generation.

---

### #7 — COTRN02C (Transaction Add) — Score: 7.0

| Metric | Value | Detail |
|--------|-------|--------|
| Lines of Code | **783** | Medium online program |
| VSAM Files | **3** | CXACAIX (R), CARDXREF (R), TRANSACT (R/W) |
| Calls | **2** | CSUTLDTC (date validation) |
| Copybooks | **10** | |

**Complexity Drivers:**
- Transaction ID generation (max ID + 1 logic using READPREV)
- Cross-reference validation (card → account)
- Date validation via CSUTLDTC
- Amount validation and formatting
- Write to TRANSACT with generated key

**Risk Factors:**
- Creates financial transactions — data integrity critical
- Transaction ID generation has race condition potential
- Writes to master transaction file
- No duplicate transaction detection

**Modernization Recommendation:** Use database-generated IDs (sequences). Implement idempotency keys for duplicate prevention. Add validation service layer.

---

### #8 — CBTRN03C (Transaction Detail Report) — Score: 6.8

| Metric | Value | Detail |
|--------|-------|--------|
| Lines of Code | **649** | Medium batch program |
| Input Files | **6** | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM, REPORT |
| Copybooks | **5** | CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA05Y, CVTRA07Y |

**Complexity Drivers:**
- Multi-file join: transactions + cross-ref + type lookup + category lookup
- Date range filtering from parameter file
- Page-level, account-level, and grand totals
- Report formatting with headers, detail lines, and summary
- Complex CVTRA07Y report layout copybook (73 lines of formatting)

**Risk Factors:**
- 6-file input dependency chain
- Incorrect totals directly visible to stakeholders
- Date parameter parsing
- Report layout changes require CVTRA07Y copybook modification

**Modernization Recommendation:** Replace with a reporting framework (JasperReports, SSRS). Use SQL aggregation instead of procedural accumulation.

---

### #9 — COTRN00C (Transaction List) — Score: 6.5

| Metric | Value | Detail |
|--------|-------|--------|
| Lines of Code | **699** | Medium online program |
| VSAM Files | **1** | TRANSACT (browse) |
| Browse Operations | Bidirectional | STARTBR, READNEXT, READPREV |
| Copybooks | **8** | |

**Complexity Drivers:**
- Paginated browse with forward/backward navigation
- Filter logic for transactions by account
- Screen state management across page transitions
- Row selection routing to detail view

**Risk Factors:**
- Browse positioning bugs can skip or duplicate records
- Performance concern with large transaction volumes
- No server-side filtering (full file scan)

**Modernization Recommendation:** Implement as paginated REST API with server-side filtering and indexing. Add date range and amount filters.

---

### #10 — COBIL00C (Bill Payment) — Score: 6.4

| Metric | Value | Detail |
|--------|-------|--------|
| Lines of Code | **572** | Medium online program |
| VSAM Files | **3** | ACCTDAT (R/W), CXACAIX (R), TRANSACT (browse + W) |
| Copybooks | **10** | |

**Complexity Drivers:**
- Full balance payment calculation
- Transaction creation for payment record
- Account balance update (rewrite)
- Cross-reference lookup for account validation
- Transaction ID generation (READPREV for max ID)

**Risk Factors:**
- **Direct financial mutation** — updates account balance AND creates transaction
- Dual-write to ACCTDAT and TRANSACT without atomic guarantee
- Balance calculation precision
- No partial payment support (full balance only)

**Modernization Recommendation:** Implement as a payment service with database transactions for atomicity. Add partial payment support. Integrate with payment gateway.

---

## Summary Heat Map

```
              Complexity    Risk    Business Impact
              ──────────    ────    ───────────────
COACTUPC      ██████████    █████████   █████████     ← HIGHEST PRIORITY
CBTRN02C      ████████      █████████   █████████
COCRDUPC      █████████     ████████    ███████
COCRDLIC      ████████      ███████     ████████
CBACT04C      ███████       ████████    ████████
CBSTM03A      ████████      ███████     ███████
COTRN02C      ███████       ████████    ███████
CBTRN03C      ███████       ███████     ███████
COTRN00C      ███████       ██████      ███████
COBIL00C      ██████        ███████     ███████
```

---

## Migration Priority Recommendations

### Phase 1 — High Priority (Months 1–3)
1. **COACTUPC** → Decompose first; highest complexity and risk
2. **CBTRN02C** → Core batch engine; must be reliable before go-live
3. **CBACT04C** → Financial calculations need rigorous testing

### Phase 2 — Medium Priority (Months 3–5)
4. **COCRDUPC** → Card data handling (PCI compliance driver)
5. **COCRDLIC** → Card listing with PCI masking requirements
6. **COBIL00C** → Payment processing with atomicity needs

### Phase 3 — Standard Priority (Months 5–7)
7. **COTRN02C** → Transaction add with ID generation redesign
8. **CBSTM03A/B** → Statement generation (template engine migration)
9. **CBTRN03C** → Reporting (framework migration)
10. **COTRN00C** → Transaction list (straightforward API conversion)

### Cross-Cutting Concerns for All Phases
- Replace VSAM KSDS files with relational database tables
- Replace COMMAREA with session/JWT-based state management
- Replace BMS screens with REST API + web frontend
- Replace CEE3ABD abend handling with structured exception handling
- Add comprehensive logging and audit trails
- Implement automated testing (unit + integration)
