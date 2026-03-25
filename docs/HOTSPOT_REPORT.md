# CardDemo Hotspot Report — Top 10 Modernization Priorities

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Methodology:** Static analysis of code complexity, data coupling, business criticality, and migration risk

---

## Table of Contents

- [1. Scoring Methodology](#1-scoring-methodology)
- [2. Executive Summary](#2-executive-summary)
- [3. Top 10 Hotspot Rankings](#3-top-10-hotspot-rankings)
- [4. Detailed Hotspot Analysis](#4-detailed-hotspot-analysis)
  - [#1 COACTUPC — Account Update](#1-coactupc--account-update)
  - [#2 CBTRN02C — Transaction Posting](#2-cbtrn02c--transaction-posting)
  - [#3 COCRDLIC — Card List](#3-cocrdlic--card-list)
  - [#4 CBSTM03A — Statement Generation](#4-cbstm03a--statement-generation)
  - [#5 COCRDUPC — Card Update](#5-cocrdupc--card-update)
  - [#6 CBACT04C — Interest Calculation](#6-cbact04c--interest-calculation)
  - [#7 CBTRN03C — Transaction Report](#7-cbtrn03c--transaction-report)
  - [#8 COTRN02C — Transaction Add](#8-cotrn02c--transaction-add)
  - [#9 COBIL00C — Bill Payment](#9-cobil00c--bill-payment)
  - [#10 COTRN00C — Transaction List](#10-cotrn00c--transaction-list)
- [5. Risk Heat Map](#5-risk-heat-map)
- [6. Recommended Migration Sequence](#6-recommended-migration-sequence)
- [7. Key Observations](#7-key-observations)

---

## 1. Scoring Methodology

Each module is scored on four dimensions (1–10 scale each, max total = 40):

| Dimension | Weight | Measures | Scoring Criteria |
|-----------|--------|----------|-----------------|
| **Code Complexity** | 25% | Lines of code, EVALUATE branches, IF statements, PERFORM calls, paragraph count | >1000 LOC=8+, >100 IFs=10, >20 EVALUATEs=8 |
| **Data Coupling** | 25% | Number of VSAM files accessed, copybooks included, I-O vs read-only | >4 files=8+, I-O operations=+2, cross-entity joins=+2 |
| **Business Impact** | 30% | Revenue criticality, user-facing, financial calculations, regulatory | Financial calc=9+, payment=9, auth=8, read-only=4 |
| **Migration Risk** | 20% | CICS dependencies, BMS maps, ALTER/GO TO, assembler calls, LE calls | CICS+BMS=6+, ALTER=+3, ASM calls=+2, DB2/IMS=+3 |

**Composite Score** = (Complexity × 0.25) + (Coupling × 0.25) + (Impact × 0.30) + (Risk × 0.20)

---

## 2. Executive Summary

| Finding | Detail |
|---------|--------|
| **Highest-risk module** | COACTUPC (Account Update) — 4,236 lines, 164 IF branches, 20 EVALUATE blocks |
| **Most critical for business** | CBTRN02C (Transaction Posting) — core revenue path, updates 3 files |
| **Largest batch program** | CBSTM03A (Statement Gen) — 924 lines, calls subroutine, uses ALTER verb |
| **Most complex screen** | COCRDLIC (Card List) — 1,459 lines, multi-page browse with forward/backward |
| **Security hotspot** | COSGN00C — plaintext password storage, no encryption |
| **Total LOC in top 10** | 12,364 lines (60% of total 20,650 core lines) |

---

## 3. Top 10 Hotspot Rankings

| Rank | Program | LOC | Complexity | Coupling | Business Impact | Migration Risk | **Composite** |
|------|---------|-----|:----------:|:--------:|:---------------:|:--------------:|:-------------:|
| **1** | **COACTUPC** | 4,236 | 10 | 9 | 9 | 8 | **9.05** |
| **2** | **CBTRN02C** | 731 | 8 | 9 | 10 | 6 | **8.45** |
| **3** | **COCRDLIC** | 1,459 | 9 | 7 | 7 | 8 | **7.70** |
| **4** | **CBSTM03A** | 924 | 8 | 8 | 8 | 9 | **8.20** |
| **5** | **COCRDUPC** | 1,560 | 9 | 7 | 7 | 7 | **7.50** |
| **6** | **CBACT04C** | 652 | 7 | 9 | 9 | 5 | **7.70** |
| **7** | **CBTRN03C** | 649 | 7 | 8 | 7 | 5 | **6.85** |
| **8** | **COTRN02C** | 783 | 8 | 8 | 8 | 7 | **7.80** |
| **9** | **COBIL00C** | 572 | 7 | 8 | 9 | 7 | **7.90** |
| **10** | **COTRN00C** | 699 | 7 | 6 | 6 | 7 | **6.45** |

---

## 4. Detailed Hotspot Analysis

### #1 COACTUPC — Account Update

**File:** `app/cbl/COACTUPC.cbl` | **Lines:** 4,236 | **Type:** CICS Online | **Composite: 9.05**

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 4,236 | **Extreme** — largest program by 2.7×; nearly 3× the next largest |
| IF Statements | 164 | **Extreme** — highest in codebase by wide margin |
| EVALUATE Blocks | 20 | **Very High** — complex branching logic |
| PERFORM Calls | 64 | High |
| Paragraphs | 88 | **Extreme** — most paragraphs in any program |
| VSAM Files | 3 (ACCTDATA, CARDDATA, CUSTDATA) | High — cross-entity reads |
| CICS Operations | READ ×5 | Multiple file reads for validation |
| Copybooks | 12+ | High coupling |

**Why It's #1:**
- Largest and most complex single program in the entire application
- Handles account field validation with deeply nested IF/EVALUATE logic
- Touches three core VSAM files (Account, Card, Customer)
- Every field on the account update screen has individual validation rules
- High business impact — incorrect account updates affect balances and credit limits

**Migration Challenges:**
- 164 IF branches must be carefully mapped to Java validation logic
- Screen field-by-field validation needs to become form validation / DTO validation
- CICS SEND/RECEIVE MAP must become REST request/response or web form
- Cross-file reads for card/customer data suggest need for service layer

**Recommendation:** Decompose into multiple Java classes: `AccountUpdateController`, `AccountValidator`, `AccountService`. Extract validation rules into a separate rules engine or Bean Validation annotations.

---

### #2 CBTRN02C — Transaction Posting

**File:** `app/cbl/CBTRN02C.cbl` | **Lines:** 731 | **Type:** Batch | **Composite: 8.45**

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 731 | Medium-High |
| IF Statements | 48 | High |
| PERFORM Calls | 61 | High |
| Paragraphs | 29 | Moderate |
| Input Files | 2 (DALYTRAN, XREFFILE) | |
| Output Files | 2 (TRANFILE, DALYREJS) | |
| I-O Files | 2 (ACCTFILE, TCATBALF) | **Critical** — in-place updates |

**Why It's #2:**
- **Core revenue path** — every financial transaction flows through this program
- Updates account balances (ACCTFILE I-O) and category balances (TCATBALF I-O)
- Validates transactions against card cross-reference before posting
- Rejected transactions written to separate file for reconciliation
- Financial accuracy is paramount — rounding errors have real business impact

**Migration Challenges:**
- Batch sequential file processing → Spring Batch ItemReader/ItemProcessor/ItemWriter
- In-place file updates (I-O mode) → database transactions with proper isolation
- Rejection handling → exception flow with dead-letter queue pattern
- `S9(09)V99` arithmetic → `BigDecimal` with explicit rounding modes

**Recommendation:** Map to Spring Batch job with chunk-oriented processing. Use database transactions instead of VSAM I-O updates. Implement comprehensive audit logging for financial traceability.

---

### #3 COCRDLIC — Card List

**File:** `app/cbl/COCRDLIC.cbl` | **Lines:** 1,459 | **Type:** CICS Online | **Composite: 7.70**

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 1,459 | High |
| IF Statements | 59 | High |
| EVALUATE Blocks | 18 | High |
| PERFORM Calls | 34 | Moderate |
| Paragraphs | 42 | High |
| CICS Browse Ops | STARTBR, READNEXT, READPREV, ENDBR | Complex cursor management |
| XCTL Targets | COMEN01C, COCRDSLC, COCRDUPC | 3 navigation targets |
| Copybooks | CSSTRPFY (string formatting) | Extra utility dependency |

**Why It's #3:**
- Implements multi-page browsable list with forward/backward scrolling
- Complex VSAM browse logic (STARTBR/READNEXT/READPREV/ENDBR) for pagination
- Three XCTL targets for navigation (menu, view, update)
- PCI-sensitive — displays card numbers on screen
- 42 paragraphs indicate highly procedural control flow

**Migration Challenges:**
- VSAM browse with cursor → SQL pagination (OFFSET/LIMIT or keyset pagination)
- BMS multi-row display → paginated REST API + frontend table/grid
- READNEXT/READPREV logic → bidirectional cursor or page number tracking
- Card number masking needed in modern UI (PCI DSS compliance)

**Recommendation:** Implement as paginated REST endpoint with Spring Data JPA. Use keyset pagination for performance. Add card number masking (show last 4 digits only). Frontend: paginated data table with view/edit actions.

---

### #4 CBSTM03A — Statement Generation

**File:** `app/cbl/CBSTM03A.CBL` | **Lines:** 924 | **Type:** Batch | **Composite: 8.20**

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 924 | High |
| IF Statements | 15 | Moderate |
| EVALUATE Blocks | 9 | Moderate |
| PERFORM Calls | 29 | Moderate |
| Paragraphs | 28 | Moderate |
| CALL Targets | CBSTM03B (14 calls), CEE3ABD | Heavy subroutine usage |
| Output Formats | 2 (plain text + HTML) | Dual output |
| **ALTER Verb** | Yes (4 instances) | **Critical migration risk** |

**Why It's #4:**
- Uses the **ALTER verb** — dynamically changes GO TO targets at runtime
- This is one of the most difficult COBOL constructs to migrate to Java
- Calls CBSTM03B subroutine 14 times for file I/O operations
- Produces dual output (text + HTML) — complex formatting logic
- Reads 4 VSAM files through the subroutine (transactions, cross-ref, customer, account)
- Uses `CEE3ABD` for abnormal termination — LE dependency

**Migration Challenges:**
- **ALTER verb** — must be refactored to state machine or strategy pattern; no Java equivalent
- CBSTM03A/B call relationship → merge into single service or use dependency injection
- Fixed-width text formatting → template engine (Thymeleaf, FreeMarker)
- HTML generation → modern templating (not inline string building)
- CEE3ABD → Java exception handling

**Recommendation:** Highest technical risk due to ALTER verb. Refactor ALTER/GO TO into explicit state machine before migration. Implement as Spring Batch job producing PDF statements via a template engine. Merge CBSTM03A/B into a single `StatementGenerationService`.

---

### #5 COCRDUPC — Card Update

**File:** `app/cbl/COCRDUPC.cbl` | **Lines:** 1,560 | **Type:** CICS Online | **Composite: 7.50**

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 1,560 | High |
| IF Statements | 72 | High |
| EVALUATE Blocks | 16 | High |
| Paragraphs | 48 | High |
| VSAM Files | 2 (CARDDATA, ACCTDATA) | |
| CICS Operations | READ ×2, XCTL | |
| Copybooks | 13+ (includes CSMSG02Y, CSSTRPFY) | |

**Why It's #5:**
- Second-largest online program after COACTUPC
- 72 IF statements for card field validation
- PCI-sensitive — handles card numbers, CVV codes, expiration dates
- Complex screen interaction with confirmation flows
- HANDLE ABEND for error recovery

**Migration Challenges:**
- Card data handling needs PCI DSS compliance (encryption at rest, masking)
- Field validation → Java Bean Validation with custom validators
- HANDLE ABEND → try/catch with proper rollback
- Multi-step confirmation → REST API with optimistic locking

**Recommendation:** Implement strict PCI DSS controls in Java. Use Spring Security for access control. Encrypt card data at rest. Field validation via Bean Validation annotations.

---

### #6 CBACT04C — Interest Calculation

**File:** `app/cbl/CBACT04C.cbl` | **Lines:** 652 | **Type:** Batch | **Composite: 7.70**

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 652 | Medium |
| IF Statements | 43 | High |
| PERFORM Calls | 56 | High |
| Paragraphs | 25 | Moderate |
| Input Files | 3 (TCATBALF, XREFFILE, DISCGRP) | |
| I-O Files | 1 (ACCTFILE) | **Critical** — updates balances |
| Output Files | 1 (TRANSACT) | Creates interest transaction records |

**Why It's #6:**
- **Financial calculation engine** — computes interest on account balances
- Reads disclosure group rates and applies them per transaction category
- Updates account balances in-place (I-O mode)
- Creates new transaction records for interest charges
- 43 IF statements indicate complex business rules for rate application
- Any calculation error directly impacts customer billing

**Migration Challenges:**
- COBOL fixed-point arithmetic (`S9(10)V99`) → Java `BigDecimal` with explicit `RoundingMode`
- Interest rate lookup by group/type/category → complex join query or lookup service
- In-place account updates → database transaction with row-level locking
- Must preserve exact cent-level calculation behavior during migration

**Recommendation:** Implement as Spring Batch job. Use `BigDecimal` throughout with `HALF_EVEN` rounding (banker's rounding). Write comprehensive test cases comparing COBOL and Java calculation results. Consider a parallel-run period.

---

### #7 CBTRN03C — Transaction Report

**File:** `app/cbl/CBTRN03C.cbl` | **Lines:** 649 | **Type:** Batch | **Composite: 6.85**

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 649 | Medium |
| IF Statements | 38 | Moderate-High |
| EVALUATE Blocks | 4 | Low-Moderate |
| PERFORM Calls | 72 | **Highest in codebase** |
| Paragraphs | 29 | Moderate |
| Input Files | 5 (TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM) | **High** — most inputs |
| Output Files | 1 (TRANREPT) | Report output |

**Why It's #7:**
- Highest PERFORM count (72) — indicates highly modular but procedural structure
- Reads from 5 different input files — complex data assembly
- Generates formatted report with page totals, account totals, grand totals
- Date-range filtering from parameter file
- Control break logic for account-level subtotals

**Migration Challenges:**
- 5-file join → SQL query with JOINs or multi-table read in Spring Batch
- Control break report logic → grouped stream processing or JasperReports
- Fixed-width report output → PDF/Excel generation
- 72 PERFORM calls → many small methods; good for refactoring

**Recommendation:** Implement as Spring Batch job with JasperReports or Apache POI for output. Replace 5-file reads with a single SQL query joining the equivalent tables. The high PERFORM count actually aids migration — each paragraph maps to a Java method.

---

### #8 COTRN02C — Transaction Add

**File:** `app/cbl/COTRN02C.cbl` | **Lines:** 783 | **Type:** CICS Online | **Composite: 7.80**

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 783 | Medium-High |
| EVALUATE Blocks | 26 | **Very High** — most EVALUATEs after COACTUPC |
| IF Statements | 14 | Moderate |
| PERFORM Calls | 61 | High |
| Paragraphs | 20 | Moderate |
| VSAM Files | 3 (TRANSACT, ACCTDATA, CARDXREF) | |
| CICS Operations | READ ×2, STARTBR, READPREV, ENDBR, WRITE | Complex |
| CALL Targets | CSUTLDTC | Date validation |

**Why It's #8:**
- Entry point for new financial transactions — directly impacts revenue
- 26 EVALUATE blocks for complex screen state management
- Validates transaction against account and card cross-reference
- Generates transaction IDs using READPREV to find last ID
- Date validation via CSUTLDTC/CEEDAYS

**Migration Challenges:**
- Transaction ID generation (READPREV for max ID) → database sequence or UUID
- 26 EVALUATE blocks → switch statements or state machine
- Multi-file validation → service layer with repository pattern
- CEEDAYS date validation → `java.time` API

**Recommendation:** Implement as REST POST endpoint. Use database sequences for ID generation. Replace EVALUATE-based state management with proper controller flow. Date validation via `java.time.LocalDate`.

---

### #9 COBIL00C — Bill Payment

**File:** `app/cbl/COBIL00C.cbl` | **Lines:** 572 | **Type:** CICS Online | **Composite: 7.90**

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 572 | Medium |
| EVALUATE Blocks | 18 | High |
| IF Statements | 10 | Low |
| PERFORM Calls | 38 | Moderate |
| Paragraphs | 18 | Moderate |
| VSAM Files | 3 (TRANSACT, ACCTDATA, CARDXREF) | |
| CICS Operations | READ, READPREV, REWRITE, WRITE, STARTBR, ENDBR | **Full CRUD** |

**Why It's #9:**
- **Payment processing** — highest business criticality per line of code
- Full CRUD on VSAM: reads account, rewrites balance, writes transaction
- Directly modifies account balances (REWRITE on ACCTDATA)
- Creates payment transaction records
- 18 EVALUATE blocks for payment validation and processing states

**Migration Challenges:**
- Payment processing requires ACID transactions — must use database transactions
- REWRITE (in-place update) → optimistic locking with version column
- Balance update must be atomic — race condition risk in concurrent environment
- Regulatory compliance for payment processing (audit trail, reversibility)

**Recommendation:** Implement as transactional REST endpoint with `@Transactional`. Use pessimistic or optimistic locking for balance updates. Add comprehensive audit logging. Consider event sourcing for payment traceability.

---

### #10 COTRN00C — Transaction List

**File:** `app/cbl/COTRN00C.cbl` | **Lines:** 699 | **Type:** CICS Online | **Composite: 6.45**

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 699 | Medium |
| EVALUATE Blocks | 16 | High |
| IF Statements | 26 | Moderate |
| PERFORM Calls | 43 | Moderate |
| Paragraphs | 18 | Moderate |
| VSAM Files | 1 (TRANSACT) | Low coupling |
| CICS Operations | STARTBR, READNEXT, READPREV, ENDBR | Browse operations |

**Why It's #10:**
- High-traffic screen — transaction list is the most frequently accessed view
- Complex browse logic similar to COCRDLIC
- 16 EVALUATE blocks for pagination state management
- Gateway to transaction detail (COTRN01C) and transaction add (COTRN02C)

**Migration Challenges:**
- VSAM browse → SQL pagination
- Pagination state management → stateless REST with page parameters
- BMS multi-row display → frontend data table

**Recommendation:** Implement as paginated REST GET endpoint. Use Spring Data JPA with `Pageable`. Frontend: sortable, filterable data table with infinite scroll or pagination controls.

---

## 5. Risk Heat Map

```
                    LOW Impact ◄──────────────────────► HIGH Impact
                    │                                            │
HIGH Complexity ────┤  COCRDLIC(3)          COACTUPC(1)         │
                    │  COCRDUPC(5)                               │
                    │                                            │
                    │  COTRN00C(10)        COTRN02C(8)          │
                    │                      CBSTM03A(4)          │
                    │                                            │
LOW Complexity  ────┤  CBTRN03C(7)         CBTRN02C(2)          │
                    │                      CBACT04C(6)          │
                    │                      COBIL00C(9)          │
                    │                                            │
                    └────────────────────────────────────────────┘

Legend: Number in parentheses = rank in top 10
```

### Risk Categories

| Risk Level | Programs | Key Concern |
|------------|----------|-------------|
| **Critical** | COACTUPC, CBTRN02C, COBIL00C | Financial data modification, complex validation |
| **High** | CBSTM03A, CBACT04C, COTRN02C | ALTER verb, interest calc precision, transaction entry |
| **Medium** | COCRDLIC, COCRDUPC, CBTRN03C | PCI data, complex browse, multi-file joins |
| **Standard** | COTRN00C | Pagination complexity, high traffic |

---

## 6. Recommended Migration Sequence

Based on dependencies, risk, and business value, the recommended migration order is:

### Wave 1: Foundation (Weeks 1–4)
| Order | Module | Rationale |
|-------|--------|-----------|
| 1.1 | **CSUSR01Y → User Security table** | Foundation — all auth depends on this |
| 1.2 | **COSGN00C → Auth Service** | Must authenticate before any other screen |
| 1.3 | **COCOM01Y → Session/COMMAREA DTO** | Shared state used by all programs |
| 1.4 | **COMEN01C / COADM01C → Navigation** | Menu routing to all other screens |

### Wave 2: Read-Only Screens (Weeks 5–8)
| Order | Module | Rationale |
|-------|--------|-----------|
| 2.1 | **COTRN00C → Transaction List API** | High traffic, read-only, low risk |
| 2.2 | **COTRN01C → Transaction Detail API** | Read-only, simple |
| 2.3 | **COACTVWC → Account View API** | Read-only, validates data layer |
| 2.4 | **COCRDLIC → Card List API** | Read-only browse, validates pagination |

### Wave 3: Write Operations (Weeks 9–14)
| Order | Module | Rationale |
|-------|--------|-----------|
| 3.1 | **COUSR00C–03C → User CRUD API** | Self-contained, low financial risk |
| 3.2 | **COTRN02C → Transaction Add API** | Core write path, enables testing |
| 3.3 | **COBIL00C → Bill Payment API** | Payment processing, needs thorough testing |
| 3.4 | **COCRDUPC → Card Update API** | PCI compliance needed |
| 3.5 | **COACTUPC → Account Update API** | Most complex — do last in write wave |

### Wave 4: Batch Processing (Weeks 15–20)
| Order | Module | Rationale |
|-------|--------|-----------|
| 4.1 | **CBTRN02C → Transaction Posting Job** | Core batch — most critical |
| 4.2 | **CBACT04C → Interest Calculation Job** | Financial precision critical |
| 4.3 | **CBTRN03C → Transaction Report Job** | Reporting, can run in parallel |
| 4.4 | **CBSTM03A/B → Statement Generation Job** | ALTER verb requires special handling |
| 4.5 | **CBEXPORT/CBIMPORT → Data Migration** | Utility, lowest priority |

---

## 7. Key Observations

### Technical Debt Findings

| # | Finding | Severity | Affected Programs | Recommendation |
|---|---------|----------|-------------------|----------------|
| 1 | **Plaintext passwords** in USRSEC | Critical | COSGN00C, COUSR00C–03C | BCrypt/Argon2 hashing |
| 2 | **ALTER verb** in CBSTM03A | High | CBSTM03A | Refactor to state machine before migration |
| 3 | **No input sanitization** | Medium | All online programs | Add input validation layer |
| 4 | **Hardcoded lookup tables** (CSLKPCDY, 1,318 lines) | Medium | Multiple programs | Externalize to database reference tables |
| 5 | **Card numbers unmasked** on screen | High | COCRDLIC, COCRDSLC | PCI DSS — mask all but last 4 digits |
| 6 | **No audit trail** for data changes | Medium | All write programs | Add audit logging |
| 7 | **FILLER fields** consuming significant space | Low | All copybooks | Right-size record layouts |
| 8 | **Duplicate record layouts** (CVCUS01Y vs CUSTREC) | Low | CBSTM03A | Consolidate to single Customer class |
| 9 | **UNUSED1Y copybook** still in codebase | Low | None | Remove dead code |
| 10 | **Fixed-point arithmetic** (`S9(n)V99`) | Medium | CBACT04C, CBTRN02C, COBIL00C | Mandate `BigDecimal` with explicit rounding |

### Complexity Distribution

```
Lines of Code Distribution (Core 31 programs):

  >2000 LOC:  ██ 1 program   (COACTUPC: 4,236)
  1000-2000:  ████ 2 programs (COCRDLIC: 1,459, COCRDUPC: 1,560)
  500-1000:   ████████████ 10 programs
  200-500:    ██████████████ 12 programs
  <200:       ████████████ 6 programs

Top 10 programs contain 60% of all code but 85% of all complexity.
```

### Modernization Effort Estimates

| Program | Estimated Java LOC | Effort (Person-Days) | Confidence |
|---------|-------------------|---------------------|------------|
| COACTUPC | ~3,000 | 15–20 | Medium (high complexity) |
| CBTRN02C | ~600 | 8–12 | Medium (financial precision) |
| COCRDLIC | ~800 | 8–10 | High (pagination well understood) |
| CBSTM03A/B | ~700 | 12–15 | Low (ALTER verb risk) |
| COCRDUPC | ~900 | 8–10 | High |
| CBACT04C | ~500 | 10–14 | Medium (financial precision) |
| CBTRN03C | ~400 | 6–8 | High (report generation) |
| COTRN02C | ~500 | 6–8 | High |
| COBIL00C | ~400 | 8–10 | Medium (payment compliance) |
| COTRN00C | ~400 | 5–6 | High (pagination) |
| **Total Top 10** | **~8,200** | **86–113** | |
