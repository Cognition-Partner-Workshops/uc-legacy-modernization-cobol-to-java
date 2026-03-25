# CardDemo Hotspot Report — Top 10 Modules

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
>
> This report identifies the 10 highest-risk modules in the CardDemo codebase, prioritized by a composite score of **code complexity**, **migration risk**, and **business impact**. Use this to guide modernization sequencing, testing investment, and team allocation.

---

## 1. Scoring Methodology

Each module is scored on three dimensions (1–10 scale):

| Dimension | Weight | What It Measures |
|---|---|---|
| **Complexity** | 35% | Lines of code, cyclomatic complexity (IF/EVALUATE/PERFORM counts), number of copybooks, number of CICS commands, VSAM files accessed |
| **Migration Risk** | 35% | Coupling to other programs, use of advanced CICS features (XCTL, dynamic calls), pointer arithmetic, REDEFINES usage, COMP fields, embedded business rules |
| **Business Impact** | 30% | Criticality to daily operations, financial data handling, user-facing visibility, downstream dependencies, data integrity responsibility |

**Composite Score** = (Complexity × 0.35) + (Risk × 0.35) + (Impact × 0.30)

---

## 2. Top 10 Hotspot Ranking

| Rank | Program | LOC | Composite | Complexity | Risk | Impact | Domain |
|---|---|---|---|---|---|---|---|
| **1** | COACTUPC | 4,236 | **9.35** | 10 | 9 | 9 | Account Update |
| **2** | CBTRN02C | 731 | **8.60** | 7 | 9 | 10 | Transaction Posting |
| **3** | COCRDLIC | 1,459 | **8.35** | 9 | 8 | 8 | Card List |
| **4** | COCRDUPC | 1,560 | **8.35** | 9 | 8 | 8 | Card Update |
| **5** | CBACT04C | 652 | **8.25** | 7 | 8 | 10 | Interest Calculation |
| **6** | CBSTM03A | 924 | **8.05** | 8 | 9 | 7 | Statement Generation |
| **7** | COCRDSLC | 887 | **7.70** | 8 | 8 | 7 | Card View |
| **8** | COACTVWC | 941 | **7.65** | 8 | 7 | 8 | Account View |
| **9** | COTRN02C | 783 | **7.35** | 7 | 8 | 7 | Transaction Add |
| **10** | CBTRN03C | 649 | **7.05** | 8 | 7 | 6 | Transaction Report |

---

## 3. Detailed Hotspot Analysis

### Rank #1 — COACTUPC (Account Update) · Score: 9.35

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 4,236 | **Largest program in codebase** — 2.7× the next largest |
| IF statements | 164 | Extremely high cyclomatic complexity |
| EVALUATE statements | 20 | Complex branching logic |
| PERFORM statements | 64 | Deep procedure hierarchy |
| COMPUTE statements | 5 | Financial calculations inline |
| Copybooks included | 16 | High coupling surface |
| VSAM files accessed | 4 | ACCTDATA (RW), CARDXREF (RW), CUSTDATA (RW), CARDDATA |
| CICS commands | ~40 | Heavy CICS interaction (SEND/RECEIVE/READ/REWRITE/XCTL) |

**Why It's #1:**
- This is the single most complex program in the entire application — a monolith within a monolith
- Handles account update with multi-file transactional writes (account + customer + cross-ref)
- Contains embedded validation rules for credit limits, account status transitions, and date logic
- Uses dynamic XCTL for navigation, making control flow hard to trace
- Any bug here directly corrupts financial account data

**Migration Recommendation:**
- **Break into microservices:** Separate account validation, account persistence, and screen logic
- **Extract business rules** into a dedicated rules engine or service layer
- **Target:** Spring MVC controller + AccountService + AccountRepository
- **Test investment:** High — needs comprehensive integration tests for all field validations
- **Estimated effort:** 3–4 weeks for one experienced developer

---

### Rank #2 — CBTRN02C (Transaction Posting) · Score: 8.60

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 731 | Moderate size but extreme business criticality |
| IF statements | 93 | Extensive validation logic |
| PERFORM statements | 62 | Complex flow control |
| Files accessed | 6 | DALYTRAN (R), TRANSACT (W), XREFFILE (R), DALYREJS (W), ACCTFILE (R), TCATBALF (W) |
| CALL targets | CSUTLDTC | Date utility dependency |

**Why It's #2:**
- **Core of the nightly batch cycle** — posts all daily transactions to the master file
- Validates each transaction against cross-reference, account status, and card validity
- Writes to THREE output files: transaction master, rejection file, and category balances
- A failure here blocks the entire downstream pipeline (interest calc, statements, reports)
- Contains implicit transaction-like behavior without CICS transaction management

**Migration Recommendation:**
- **Target:** Spring Batch job with chunk-oriented processing (read → validate → write)
- Implement proper database transactions with rollback capability
- Add dead-letter queue for rejected transactions (replacing DALYREJS GDG)
- **Estimated effort:** 2–3 weeks

---

### Rank #3 — COCRDLIC (Card List) · Score: 8.35

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 1,459 | Third largest program |
| IF statements | 120 | Very high — paging logic, selection handling |
| EVALUATE statements | 18 | Complex state management |
| PERFORM statements | 34 | Multi-level browsing logic |
| CICS commands | ~25 | STARTBR, READNEXT, READPREV, ENDBR, XCTL |
| VSAM files | CARDDATA (browse), CARDXREF | |

**Why It's #3:**
- Implements full forward/backward paging through VSAM browse operations
- Contains the most complex CICS browse logic (STARTBR → READNEXT/READPREV → ENDBR)
- Dynamic program transfer to view (COCRDSLC) or update (COCRDUPC)
- Selection handling for multiple display rows with validation
- CSSTRPFY copybook inline for PF key stripping

**Migration Recommendation:**
- **Target:** Paginated REST API endpoint + React/Angular list component
- Replace VSAM browse with SQL `SELECT ... LIMIT ... OFFSET` or cursor-based pagination
- **Estimated effort:** 2 weeks

---

### Rank #4 — COCRDUPC (Card Update) · Score: 8.35

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 1,560 | Second largest program |
| IF statements | 146 | Extremely high — field-by-field validation |
| EVALUATE statements | 16 | State machine behavior |
| PERFORM statements | 26 | |
| Copybooks | 13 | High coupling |
| VSAM files | CARDDATA (RW), CUSTDATA (R) | |

**Why It's #4:**
- Extensive field-level validation for card data (expiration, CVV, embossed name, status)
- Handles card status transitions (active/inactive) with business rules
- REWRITE operation on CARDDATA — direct financial data mutation
- Uses CSSTRPFY for PF key handling
- Tightly coupled to CVACT02Y, CVCRD01Y, and CVCUS01Y copybooks

**Migration Recommendation:**
- **Target:** Card update REST endpoint with Bean Validation (JSR 380)
- Extract validation rules into `CardValidator` service
- **Estimated effort:** 2 weeks

---

### Rank #5 — CBACT04C (Interest Calculation) · Score: 8.25

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 652 | Moderate size but **highest business impact** |
| IF statements | 86 | Complex financial logic |
| PERFORM statements | 57 | Deep calculation loops |
| COMPUTE statements | 5 | Core interest calculations |
| Files accessed | 5 | TCATBALF, XREFFILE(+AIX), ACCTFILE, DISCGRP, TRANSACT |

**Why It's #5:**
- **Directly affects customer balances** — calculates and applies interest charges
- Reads disclosure group rates and applies them per transaction category per account
- Uses alternate index path on CARDXREF for account-based lookups
- Writes system-generated transactions (interest charges) to SYSTRAN
- Updates both category balances (TCATBALF) and account data (ACCTDATA)
- Financial calculation errors have direct monetary impact on customers

**Migration Recommendation:**
- **Target:** Spring Batch job with precise `BigDecimal` arithmetic
- Extract rate lookup into `InterestRateService`
- Implement audit trail for all balance modifications
- **CRITICAL:** Parallel testing against COBOL output required — penny-for-penny reconciliation
- **Estimated effort:** 3 weeks (including extensive reconciliation testing)

---

### Rank #6 — CBSTM03A (Statement Generation) · Score: 8.05

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 924 | Complex batch with subroutine |
| IF statements | 15 | Moderate |
| EVALUATE statements | 9 | Format switching (text vs HTML) |
| PERFORM statements | 33 | Iterative processing |
| COMPUTE statements | 4 | Running totals |
| Subroutine | CBSTM03B (CALL) | Delegated file I/O |
| Output formats | 2 | Plain text + HTML |

**Why It's #6:**
- Dual-format output generation (text + HTML) — unusual complexity
- CALL to CBSTM03B subroutine for file handling — two programs must be migrated together
- Processes sorted transaction file (TRXFL) keyed by card number
- Joins across 4 VSAM files (transactions, cross-ref, accounts, customers)
- Report formatting with page breaks, running totals, and control breaks
- Comments indicate intentional complexity for "modernization tooling exercise"

**Migration Recommendation:**
- **Target:** Spring Batch job with JasperReports or Thymeleaf templates
- Merge CBSTM03A + CBSTM03B into a single service
- Replace dual-format with a template engine that generates both outputs
- **Estimated effort:** 2 weeks

---

### Rank #7 — COCRDSLC (Card View) · Score: 7.70

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 887 | |
| IF statements | 68 | Multi-field display logic |
| EVALUATE statements | 8 | |
| PERFORM statements | 19 | |
| CICS commands | ~20 | READ, SEND MAP, RECEIVE MAP, XCTL |
| VSAM files | CARDDATA (R), CARDXREF (R), CUSTDATA (R) |
| Copybooks | 12 | |

**Why It's #7:**
- Read-only but joins 3 VSAM files for a single card detail view
- Uses CSSTRPFY for PF key handling
- Dynamic XCTL back to caller via CDEMO-TO-PROGRAM
- Error handling with CICS ABEND/HANDLE ABEND
- Contains CICS SEND TEXT for error display (unusual pattern)

**Migration Recommendation:**
- **Target:** REST GET endpoint returning card detail DTO with joined data
- **Estimated effort:** 1 week

---

### Rank #8 — COACTVWC (Account View) · Score: 7.65

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 941 | |
| IF statements | 56 | |
| EVALUATE statements | 10 | |
| PERFORM statements | 21 | |
| CICS commands | ~22 | |
| VSAM files | ACCTDATA (R), CARDDATA (R), CARDXREF (R), CUSTDATA (R) | 4 files for one view |
| Copybooks | 14 | |

**Why It's #8:**
- Joins the most VSAM files of any online program (4 files)
- Uses CICS HANDLE ABEND for error recovery
- Dynamic XCTL for navigation
- Uses CSSTRPFY for PF key processing
- Error display via CICS SEND TEXT (non-standard pattern)

**Migration Recommendation:**
- **Target:** REST GET endpoint with database JOIN query
- **Estimated effort:** 1 week

---

### Rank #9 — COTRN02C (Transaction Add) · Score: 7.35

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 783 | |
| IF statements | 14 | Lower than expected — validation in batch instead |
| EVALUATE statements | 26 | Complex state management for multi-step entry |
| PERFORM statements | 61 | |
| COMPUTE statements | 4 | Amount calculations |
| VSAM files | TRANSACT (RW), CARDXREF (R) | |

**Why It's #9:**
- Multi-step data entry screen with state management (context = enter/reenter)
- Generates unique transaction IDs (READPREV to find last ID, then increment)
- Validates card number against cross-reference before writing
- Writes directly to TRANSACT VSAM — financial data creation
- 26 EVALUATE statements suggest complex form state machine

**Migration Recommendation:**
- **Target:** REST POST endpoint with form validation + UUID generation
- Replace sequential ID generation with UUID or database sequence
- **Estimated effort:** 1.5 weeks

---

### Rank #10 — CBTRN03C (Daily Transaction Report) · Score: 7.05

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 649 | |
| IF statements | 75 | High — complex report logic |
| PERFORM statements | 73 | Very high — iterative processing |
| EVALUATE statements | 4 | |
| Files accessed | 6 | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM, TRANREPT |
| Report copybook | CVTRA07Y | Custom print layout |

**Why It's #10:**
- Joins 4 reference files for enriched transaction reporting
- Uses CVTRA07Y report layout with page headers, detail lines, and multiple total levels
- Date parameter file (DATEPARM) for report date range — unusual input mechanism
- Writes to GDG output (TRANREPT(+1)) — generational versioning
- Control break logic with account totals, page totals, and grand total

**Migration Recommendation:**
- **Target:** Spring Batch job with report generation (JasperReports / CSV export)
- Replace DATEPARM file with command-line arguments or API parameters
- **Estimated effort:** 1.5 weeks

---

## 4. Complexity Heatmap — All Programs

```
LOC     0    500   1000   1500   2000   2500   3000   3500   4000   4500
        ├─────┼─────┼──────┼──────┼──────┼──────┼──────┼──────┼──────┤
COACTUPC ████████████████████████████████████████████████████████████████ 4236
COCRDUPC ██████████████████████████████████████░░░░░░░░░░░░░░░░░░░░░░░░ 1560
COCRDLIC ████████████████████████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░ 1459
COACTVWC ██████████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  941
CBSTM03A ██████████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  924
COCRDSLC █████████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  887
COTRN02C ████████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  783
CBTRN02C ██████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  731
COTRN00C █████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  699
COUSR00C █████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  695
CBACT04C ████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  652
CORPT00C ████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  649
CBTRN03C ████████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  649
CBEXPORT ██████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  582
COBIL00C █████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  572
CBTRN01C ████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  494
CBIMPORT ████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  487
CBACT01C ███████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  430
COUSR02C ██████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  414
COUSR03C █████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  359
COTRN01C █████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  330
COMEN01C ████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  308
COUSR01C ████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  299
COADM01C ████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  288
COSGN00C ███████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  260
CBSTM03B ██████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  230
CBACT02C █████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  178
CBACT03C █████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  178
CBCUS01C █████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  178
CSUTLDTC █████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  157
COBSWAIT ██░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░   41
```

---

## 5. Risk Matrix

```
                          B U S I N E S S   I M P A C T
                     Low (1-3)      Medium (4-6)     High (7-10)
                  ┌──────────────┬───────────────┬───────────────┐
     High (7-10)  │              │  CBSTM03A     │  COACTUPC     │
                  │              │  CBTRN03C     │  CBTRN02C     │
  C               │              │               │  CBACT04C     │
  O               │              │               │  COCRDLIC     │
  M  Med (4-6)    │  CBACT01C    │  COUSR00C     │  COCRDUPC     │
  P               │  CBACT02C    │  CORPT00C     │  COCRDSLC     │
  L               │  CBACT03C    │               │  COACTVWC     │
  E               │  CBCUS01C    │               │  COTRN02C     │
  X  Low (1-3)    │  COBSWAIT    │  COTRN01C     │  COSGN00C     │
  I               │  CSUTLDTC    │  COUSR01C     │  COBIL00C     │
  T               │              │  COUSR02C     │               │
  Y               │              │  COUSR03C     │               │
                  └──────────────┴───────────────┴───────────────┘
```

**Red Zone (top-right):** Highest priority — complex AND business-critical
**Yellow Zone (middle):** Medium priority — moderate complexity or moderate impact
**Green Zone (bottom-left):** Lowest priority — simple and low-impact

---

## 6. Recommended Migration Sequence

Based on the hotspot analysis, dependency clusters, and risk assessment:

### Phase 1: Foundation (Weeks 1–2)
| Priority | Module | Rationale |
|---|---|---|
| 1.1 | COSGN00C + CSUSR01Y | Authentication gateway — blocks everything else |
| 1.2 | COMEN01C + COADM01C | Menu routing — defines application navigation |
| 1.3 | COCOM01Y (COMMAREA) | Shared session context → Spring session beans |

### Phase 2: User Administration (Weeks 3–4)
| Priority | Module | Rationale |
|---|---|---|
| 2.1 | COUSR00C–COUSR03C | Self-contained CRUD — good first migration exercise |
| 2.2 | DUSRSECJ | User data load → database seed/migration script |

### Phase 3: Read-Only Views (Weeks 4–6)
| Priority | Module | Rationale |
|---|---|---|
| 3.1 | COACTVWC | Account view — read-only, lower risk |
| 3.2 | COCRDSLC | Card view — read-only |
| 3.3 | COTRN00C + COTRN01C | Transaction list/view — read-only |

### Phase 4: Write Operations — Cards (Weeks 6–8)
| Priority | Module | Rationale |
|---|---|---|
| 4.1 | COCRDLIC | Card list with paging → paginated API |
| 4.2 | COCRDUPC | Card update — write operations |

### Phase 5: Write Operations — Accounts & Transactions (Weeks 8–11) ⚠️ HIGHEST RISK
| Priority | Module | Rationale |
|---|---|---|
| 5.1 | **COACTUPC** | **#1 hotspot** — account update monolith |
| 5.2 | COTRN02C | Transaction add — financial data creation |
| 5.3 | COBIL00C | Bill payment — financial workflow |
| 5.4 | CORPT00C | Online reporting |

### Phase 6: Batch Processing (Weeks 11–16) ⚠️ FINANCIAL CORE
| Priority | Module | Rationale |
|---|---|---|
| 6.1 | **CBTRN02C** | **#2 hotspot** — transaction posting (nightly core) |
| 6.2 | **CBACT04C** | **#5 hotspot** — interest calculation (financial) |
| 6.3 | CBSTM03A + CBSTM03B | Statement generation |
| 6.4 | CBTRN03C | Transaction reporting |
| 6.5 | CBEXPORT + CBIMPORT | Data migration utilities |
| 6.6 | Remaining CBACT* | File read utilities |

### Phase 7: JCL & Infrastructure (Weeks 16–18)
| Priority | Module | Rationale |
|---|---|---|
| 7.1 | All data refresh JCL | → Database migration scripts |
| 7.2 | CLOSEFIL/OPENFIL | → Eliminated (no CICS file locking needed) |
| 7.3 | Backup/GDG JCL | → Database backup procedures |
| 7.4 | Scheduler configs | → Spring Scheduler / cron jobs |

---

## 7. Key Migration Risks & Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| **COACTUPC monolith** (4,236 LOC) | Single program failure breaks account management | Decompose into 3+ services; extensive unit tests |
| **Implicit decimal arithmetic** | S9(n)V99 → `BigDecimal` precision loss | Use `BigDecimal` everywhere; penny-level reconciliation testing |
| **VSAM browse → SQL pagination** | Different paging semantics | Implement cursor-based pagination; test edge cases |
| **COMMAREA session state** | In-memory state → distributed state | Spring Session with Redis; design for statelessness where possible |
| **Dynamic XCTL targets** | Control flow determined at runtime | Map all possible targets; implement routing service |
| **Plaintext passwords** (CSUSR01Y) | Security vulnerability | Hash with BCrypt on migration; force password reset |
| **PCI data in VSAM** (card numbers) | Compliance risk during migration | Tokenize during migration; never store raw PAN in Java |
| **Batch timing dependencies** | CLOSEFIL→process→OPENFIL sequence | Eliminate with database transactions; no file locking needed |
| **GDG versioning** | Generational datasets → ??? | Database versioning + timestamp partitions |
| **Date handling** | Multiple date formats, CEEDAYS calls | Standardize on `java.time.LocalDate`; central DateService |

---

## 8. Testing Priority Matrix

| Module | Unit Tests | Integration Tests | Regression / Reconciliation | UAT |
|---|---|---|---|---|
| COACTUPC | **Critical** | **Critical** | **Critical** | **Critical** |
| CBTRN02C | Critical | **Critical** | **Critical** (penny-level) | High |
| CBACT04C | Critical | Critical | **Critical** (penny-level) | High |
| COCRDLIC | High | High | Medium | High |
| COCRDUPC | High | High | High | High |
| CBSTM03A/B | Medium | High | High (output comparison) | Medium |
| COTRN02C | High | High | High | Medium |
| COACTVWC | Medium | Medium | Low | Medium |
| COCRDSLC | Medium | Medium | Low | Medium |
| CBTRN03C | Medium | High | High (report comparison) | Low |
