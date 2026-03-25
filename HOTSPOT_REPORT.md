# CardDemo Hotspot Report — Top 10 Modules

> **Generated**: 2026-03-25 | **Purpose**: Prioritize modules for modernization by complexity, risk, and business impact
> **Methodology**: Weighted scoring across Lines of Code, cyclomatic complexity indicators, data coupling, business criticality, and change risk

---

## Scoring Methodology

Each module is scored 1–5 on four dimensions:

| Dimension           | Weight | 1 (Low)                | 5 (High)                           |
|--------------------|:------:|------------------------|-------------------------------------|
| **Code Complexity** | 30%    | < 200 LOC, simple flow | > 1000 LOC, deep nesting, many branches |
| **Data Coupling**   | 20%    | 0–1 files accessed     | 5+ files, cross-file updates        |
| **Business Impact** | 30%    | Utility/display only   | Financial calculations, money movement |
| **Migration Risk**  | 20%    | Stateless, no I/O      | CICS-specific, complex VSAM patterns |

**Composite Score** = (Complexity × 0.30) + (Data Coupling × 0.20) + (Business Impact × 0.30) + (Migration Risk × 0.20)

---

## Top 10 Hotspot Ranking

| Rank | Module     | Lines | Composite | Complexity | Data Coupling | Business Impact | Migration Risk | Domain |
|:----:|-----------|------:|:---------:|:----------:|:-------------:|:---------------:|:--------------:|--------|
| 1    | **COACTUPC** | 4,237 | **4.70** | 5 | 5 | 5 | 4 | Account Update |
| 2    | **CBTRN02C** | 731   | **4.40** | 4 | 5 | 5 | 4 | Transaction Posting |
| 3    | **CBACT04C** | 652   | **4.30** | 4 | 5 | 5 | 3 | Interest Calculation |
| 4    | **COCRDLIC** | 1,460 | **4.00** | 5 | 3 | 4 | 4 | Card List |
| 5    | **COCRDUPC** | 1,560 | **3.90** | 5 | 3 | 4 | 4 | Card Update |
| 6    | **COBIL00C** | 572   | **3.90** | 3 | 5 | 5 | 3 | Bill Payment |
| 7    | **CBSTM03A** | 924   | **3.80** | 4 | 4 | 4 | 3 | Statement Generation |
| 8    | **COACTVWC** | 942   | **3.70** | 4 | 4 | 3 | 4 | Account View |
| 9    | **COTRN02C** | 783   | **3.60** | 4 | 4 | 4 | 3 | Transaction Add |
| 10   | **CBTRN03C** | 649   | **3.40** | 3 | 4 | 4 | 3 | Transaction Report |

---

## Detailed Analysis

### #1 — COACTUPC (Account Update) — Score: 4.70

**Why it's #1**: The largest program in the codebase at 4,237 lines. Contains the most complex business logic with full CRUD operations on account data, extensive field-level validation (SSN, phone, dates, credit limits), and multi-file updates.

| Metric                | Detail                                                   |
|-----------------------|----------------------------------------------------------|
| **Lines of Code**     | 4,237 (largest in codebase by 2.7×)                      |
| **CICS Operations**   | 5 READ, REWRITE, multiple SEND/RECEIVE MAP               |
| **Files Accessed**    | ACCTDAT, CARDXREF/CXACAIX, CUSTDAT, CARDDAT (4 VSAM files) |
| **Copybooks Used**    | 17 (most in codebase)                                    |
| **Key Risk Factors**  | Complex input validation (SSN, phone, dates, currency), field-level attribute protection, multi-record updates |

**Modernization Concerns**:
- Extensive inline validation logic → Extract to validation service layer
- BMS attribute manipulation (color, protection) → Map to HTML form validation
- Multiple REDEFINES for numeric/alpha conversion → Java type parsing
- EVALUATE/PERFORM nesting depth is significant

**Recommended Approach**: Decompose into multiple Java services — `AccountService`, `AccountValidator`, `CustomerValidator`. Map BMS form to REST API + web form.

---

### #2 — CBTRN02C (Transaction Posting — Batch) — Score: 4.40

**Why it's #2**: Core financial processing program that posts daily transactions to the master file. Touches 6 files simultaneously and performs balance updates — any bug here means incorrect account balances.

| Metric                | Detail                                                   |
|-----------------------|----------------------------------------------------------|
| **Lines of Code**     | 731                                                      |
| **Files Accessed**    | DALYTRAN, TRANFILE, XREFFILE, DALYREJS, ACCTFILE, TCATBALF (6 files) |
| **Copybooks Used**    | CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA05Y, CVTRA06Y        |
| **Key Risk Factors**  | Financial accuracy (balance updates), reject handling, multi-file consistency |

**Modernization Concerns**:
- Must maintain transactional consistency across 6 files → Use database transactions
- Reject file handling → Map to exception/dead-letter queue
- Category balance accumulation logic is business-critical
- Sequential file processing pattern → Spring Batch ItemReader/ItemWriter

**Recommended Approach**: Spring Batch job with chunk-oriented processing. Use `@Transactional` to ensure atomicity of account balance updates.

---

### #3 — CBACT04C (Interest Calculation) — Score: 4.30

**Why it's #3**: Financial calculation engine that computes interest per account based on disclosure group rates. Mathematical precision is critical — rounding errors would directly impact customer statements.

| Metric                | Detail                                                   |
|-----------------------|----------------------------------------------------------|
| **Lines of Code**     | 652                                                      |
| **Files Accessed**    | TCATBALF, XREFFILE, ACCTFILE, DISCGRP, TRANSACT (5 files) |
| **Copybooks Used**    | CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA02Y, CVTRA05Y        |
| **Key Risk Factors**  | Financial precision (S9(09)V99 arithmetic), interest rate lookups, balance updates |

**Modernization Concerns**:
- COBOL COMPUTE with ROUNDED → Java `BigDecimal` with explicit `RoundingMode`
- Disclosure group rate lookups → JPA repository queries
- Generated interest transactions must match original COBOL output exactly
- Must validate with parallel-run testing against mainframe output

**Recommended Approach**: Dedicated `InterestCalculationService` using `BigDecimal` exclusively. Implement parallel-run comparison framework for validation.

---

### #4 — COCRDLIC (Credit Card List) — Score: 4.00

**Why it's #4**: Complex browse/paging logic with CICS STARTBR/READNEXT/ENDBR patterns, row selection handling (S for view, U for update), and conditional filtering based on user type.

| Metric                | Detail                                                   |
|-----------------------|----------------------------------------------------------|
| **Lines of Code**     | 1,460                                                    |
| **CICS Operations**   | READ ×4, STARTBR ×2, READNEXT (loop), ENDBR             |
| **Files Accessed**    | CARDDAT, CARDAIX (alternate index)                       |
| **Key Risk Factors**  | VSAM browse → SQL pagination, row selection state management, alternate index access |

**Modernization Concerns**:
- CICS STARTBR/READNEXT/ENDBR pattern → JPA `Pageable` with offset/limit
- Alternate index (CARDAIX) → SQL secondary index or JOIN
- Row selection state (7 rows × S/U flags) → Frontend selection + API call
- Screen-level data caching in COMMAREA → Server-side session or stateless design

**Recommended Approach**: REST API with pagination parameters. Replace browse loop with `SELECT ... ORDER BY ... LIMIT/OFFSET`.

---

### #5 — COCRDUPC (Credit Card Update) — Score: 3.90

**Why it's #5**: Similar complexity to COCRDSLC but adds write operations. Contains card-level validation, cross-reference lookups, and REWRITE operations.

| Metric                | Detail                                                   |
|-----------------------|----------------------------------------------------------|
| **Lines of Code**     | 1,560                                                    |
| **CICS Operations**   | READ ×2, REWRITE                                         |
| **Files Accessed**    | CARDDAT, CARDAIX, CUSTDAT, ACCTDAT, CARDXREF             |
| **Copybooks Used**    | 15 copybooks                                             |
| **Key Risk Factors**  | Card data modification, expiry date validation, cross-reference integrity |

**Recommended Approach**: `CreditCardService.update()` with JPA entity and Bean Validation annotations.

---

### #6 — COBIL00C (Bill Payment) — Score: 3.90

**Why it's #6**: Moves money — pays account balance in full. Involves reading cross-references, computing payment amount, writing a transaction record, and updating the account balance. Financial operations are the highest-risk category.

| Metric                | Detail                                                   |
|-----------------------|----------------------------------------------------------|
| **Lines of Code**     | 572                                                      |
| **CICS Operations**   | READ ×3, WRITE ×1, REWRITE ×1, STARTBR, ENDBR           |
| **Files Accessed**    | ACCTDAT, CXACAIX, TRANSACT (3 files, all with writes)    |
| **Key Risk Factors**  | Balance-in-full calculation, atomic write + rewrite, cross-reference lookup |

**Modernization Concerns**:
- Must be atomic: create transaction + update balance in one unit of work
- Full-balance payment logic → Need to handle concurrent access (optimistic locking)
- CICS STARTBR for card lookup → JPA query

**Recommended Approach**: `BillPaymentService` with `@Transactional`. Use optimistic locking (`@Version`) on Account entity.

---

### #7 — CBSTM03A (Statement Generation) — Score: 3.80

**Why it's #7**: Complex batch report generation with a subroutine call to CBSTM03B. Produces both text and HTML statement output from sorted transaction data.

| Metric                | Detail                                                   |
|-----------------------|----------------------------------------------------------|
| **Lines of Code**     | 924 (+ 230 in CBSTM03B = 1,154 total)                   |
| **Files Accessed**    | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE → STMTFILE, HTMLFILE |
| **Subroutine**        | CALL 'CBSTM03B' for file processing                     |
| **Key Risk Factors**  | Report formatting, page breaks, account totals, HTML generation |

**Modernization Concerns**:
- COBOL report writer patterns (headers, footers, page breaks) → Template engine (Thymeleaf/JasperReports)
- Subroutine CALL → Method call within same service
- Dual output (text + HTML) → Single template with multiple renderers

**Recommended Approach**: Spring Batch job using `FlatFileItemReader` → `StatementProcessor` → Template-based output.

---

### #8 — COACTVWC (Account View) — Score: 3.70

**Why it's #8**: Read-only but touches 4 VSAM files (account, card, customer, cross-reference) and has complex screen population logic. The first screen most users see after login.

| Metric                | Detail                                                   |
|-----------------------|----------------------------------------------------------|
| **Lines of Code**     | 942                                                      |
| **CICS Operations**   | READ ×3 (ACCTDAT, CARDDAT/CARDAIX, CUSTDAT)              |
| **Files Accessed**    | ACCTDAT, CARDDAT, CARDAIX, CXACAIX, CUSTDAT              |
| **Key Risk Factors**  | Multi-file join logic, screen formatting, currency display |

**Recommended Approach**: REST `GET /api/accounts/{id}` with JPA `@ManyToOne`/`@OneToMany` relationships. Single query with JOIN fetching.

---

### #9 — COTRN02C (Transaction Add — Online) — Score: 3.60

**Why it's #9**: Adds new transactions via the online CICS interface. Validates card/account existence via cross-reference, validates amounts, and writes to TRANSACT.

| Metric                | Detail                                                   |
|-----------------------|----------------------------------------------------------|
| **Lines of Code**     | 783                                                      |
| **CICS Operations**   | READ ×3, WRITE ×1, STARTBR, ENDBR                       |
| **Files Accessed**    | TRANSACT, ACCTDAT, CCXREF/CXACAIX (3 files)             |
| **Utility Call**      | CALL 'CSUTLDTC' (date validation)                        |
| **Key Risk Factors**  | Transaction creation, card-to-account validation, amount validation |

**Recommended Approach**: REST `POST /api/transactions` with request validation. The `CSUTLDTC` date utility maps to Java's `LocalDate` parsing.

---

### #10 — CBTRN03C (Transaction Report — Batch) — Score: 3.40

**Why it's #10**: Batch report generation reading from 5 files with control-break logic (by account, by type, by category). Produces formatted report with page/account/grand totals.

| Metric                | Detail                                                   |
|-----------------------|----------------------------------------------------------|
| **Lines of Code**     | 649                                                      |
| **Files Accessed**    | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM → TRANREPT |
| **Copybooks Used**    | CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA05Y, CVTRA07Y        |
| **Key Risk Factors**  | Control-break logic, date range filtering, report formatting |

**Recommended Approach**: Spring Batch with `GroupItemReader` pattern. Use JPA queries with date predicates instead of sequential file scanning.

---

## Migration Priority Recommendation

Based on the hotspot analysis, the recommended modernization sequence is:

### Phase 1 — Foundation (Weeks 1–4)
Build the data layer and shared services first.

| Priority | Module     | Rationale                                            |
|:--------:|-----------|------------------------------------------------------|
| 1        | Data Layer | Convert VSAM copybooks → JPA entities + DB schema    |
| 2        | COCOM01Y  | COMMAREA → Session/Context object                     |
| 3        | CSUTLDTC  | Date utility → Java LocalDate utilities               |
| 4        | CSUSR01Y  | Security model → Spring Security UserDetails          |

### Phase 2 — Read-Only Screens (Weeks 5–8)
Lower risk, builds confidence in the converted data layer.

| Priority | Module     | Rationale                                            |
|:--------:|-----------|------------------------------------------------------|
| 5        | COSGN00C  | Login → Spring Security authentication                |
| 6        | COMEN01C  | Menu → Navigation/routing                             |
| 7        | COACTVWC  | Account View — validates multi-table reads            |
| 8        | COCRDSLC  | Card View — simple single-record read                 |
| 9        | COTRN01C  | Transaction View — simple single-record read          |

### Phase 3 — Write Operations (Weeks 9–14)
Higher risk; requires thorough testing.

| Priority | Module     | Rationale                                            |
|:--------:|-----------|------------------------------------------------------|
| 10       | COUSR00C–03C | User CRUD — isolated domain, good test case        |
| 11       | COTRN02C  | Transaction Add — validates write path                |
| 12       | COCRDUPC  | Card Update — moderate complexity                     |
| 13       | COACTUPC  | **Highest complexity** — tackle with full team        |
| 14       | COBIL00C  | Bill Payment — financial, needs extensive validation   |

### Phase 4 — Batch Processing (Weeks 15–20)
Convert batch jobs to Spring Batch, validate with parallel runs.

| Priority | Module     | Rationale                                            |
|:--------:|-----------|------------------------------------------------------|
| 15       | CBTRN02C  | Transaction Posting — core batch, validate balances   |
| 16       | CBACT04C  | Interest Calc — validate financial precision          |
| 17       | CBSTM03A/B| Statements — validate report output                   |
| 18       | CBTRN03C  | Reports — compare output with mainframe               |
| 19       | CBEXPORT/IMPORT | Data migration — needed for cutover              |

---

## Risk Heatmap

```
                    Low Business Impact ◄──────────► High Business Impact
                    
High Complexity  │  COCRDLIC          │  COACTUPC  ★★★★★
                 │  COCRDUPC          │  CBSTM03A
                 │                    │
                 │                    │
Medium           │  COUSR00C          │  CBTRN02C  ★★★★
Complexity       │  COTRN00C          │  CBACT04C
                 │  COACTVWC          │  COBIL00C
                 │                    │  COTRN02C
                 │                    │  CBTRN03C
                 │                    │
Low Complexity   │  COMEN01C          │
                 │  COADM01C          │
                 │  COSGN00C          │
                 │  COTRN01C          │
                 │  COBSWAIT          │
```

**Legend**: ★ = Priority attention required during modernization. Programs in the upper-right quadrant (high complexity + high business impact) should receive the most testing investment and senior developer attention.
