# HOTSPOT REPORT — CardDemo COBOL Application

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Purpose:** Top 10 modules prioritized by complexity, migration risk, and business impact

---

## Table of Contents

1. [Scoring Methodology](#1-scoring-methodology)
2. [Top 10 Hotspot Modules](#2-top-10-hotspot-modules)
3. [Detailed Module Assessments](#3-detailed-module-assessments)
4. [Risk Heat Map](#4-risk-heat-map)
5. [Recommended Migration Wave Plan](#5-recommended-migration-wave-plan)
6. [Key Risk Factors and Mitigations](#6-key-risk-factors-and-mitigations)

---

## 1. Scoring Methodology

Each module is scored on three dimensions (1–10 scale):

| Dimension | Weight | Criteria |
|-----------|--------|----------|
| **Complexity** | 35% | Lines of code, cyclomatic indicators (IF/EVALUATE/PERFORM count), number of CICS commands, copybook dependencies, CALL depth |
| **Risk** | 35% | Number of VSAM files accessed, data coupling (shared files with other programs), CICS complexity (XCTL chains, screen flows), financial data mutation, error handling patterns |
| **Business Impact** | 30% | Revenue criticality, user-facing functionality, downstream dependencies, data integrity responsibility, regulatory implications |

**Composite Score** = (Complexity × 0.35) + (Risk × 0.35) + (Business Impact × 0.30)

### Metric Benchmarks (from codebase analysis)

| Metric | Low (1–3) | Medium (4–6) | High (7–10) |
|--------|-----------|--------------|-------------|
| Lines of Code | < 300 | 300–700 | > 700 |
| IF + EVALUATE | < 15 | 15–50 | > 50 |
| PERFORM count | < 20 | 20–40 | > 40 |
| CICS commands | 0–5 | 6–10 | > 10 |
| Copybooks used | < 5 | 5–10 | > 10 |
| VSAM files | 0–1 | 2–3 | > 3 |

---

## 2. Top 10 Hotspot Modules

| Rank | Module | LOC | Complexity | Risk | Business Impact | **Composite** | Primary Concern |
|------|--------|-----|-----------|------|----------------|---------------|-----------------|
| **1** | **COACTUPC** | 4,236 | **10** | **10** | **9** | **9.70** | Largest program; mutates account data across 5 VSAM files |
| **2** | **CBTRN02C** | 731 | **8** | **9** | **10** | **8.95** | Core transaction posting; financial integrity critical |
| **3** | **CBACT04C** | 652 | **8** | **9** | **9** | **8.65** | Interest calculation; complex financial logic |
| **4** | **COCRDUPC** | 1,560 | **9** | **8** | **7** | **8.05** | Card update; 5 VSAM files, 148 IF statements |
| **5** | **COCRDLIC** | 1,459 | **9** | **7** | **7** | **7.70** | Card list; complex browse/scroll with 122 IF statements |
| **6** | **CBSTM03A** | 924 | **8** | **7** | **8** | **7.65** | Statement generation; dual-format output (text + HTML) |
| **7** | **COBIL00C** | 572 | **6** | **8** | **9** | **7.60** | Bill payment; writes to TRANSACT + updates ACCTDATA |
| **8** | **COTRN02C** | 783 | **7** | **7** | **8** | **7.30** | Transaction add; validation + WRITE to TRANSACT |
| **9** | **CBTRN03C** | 649 | **8** | **6** | **7** | **7.00** | Daily report; 4 EVALUATE, complex report formatting |
| **10** | **COACTVWC** | 941 | **7** | **7** | **6** | **6.70** | Account view; reads from 5 VSAM files, 57 IF branches |

---

## 3. Detailed Module Assessments

### 3.1 🔴 #1 — COACTUPC (Account Update) — Score: 9.70

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 4,236 | Largest program in the codebase by 2.7× |
| **IF statements** | 168 | Highest in codebase — extensive field validation |
| **EVALUATE** | 10 | Multi-branch control flow |
| **PERFORM** | 64 | High internal decomposition |
| **CICS commands** | 17 | Heavy CICS integration (READ, REWRITE, SEND, RECEIVE, XCTL, ABEND, HANDLE) |
| **Copybooks** | 16 | Most copybook dependencies |
| **VSAM files** | 5 | ACCTDATA, CARDXREF, CUSTDATA, CARDDATA, USRSEC |
| **CICS operations** | READ, REWRITE, SEND, RECEIVE, XCTL, ABEND, HANDLE CONDITION | Full CRUD with error handling |

**Why it's #1:**
- At 4,236 lines, this is the single largest and most complex program. It implements full account update with cross-entity validation across 5 VSAM files.
- Contains 168 IF statements indicating extensive business rule validation that must be preserved exactly during migration.
- Uses EXEC CICS HANDLE CONDITION for error flow — must be converted to Java exception handling.
- Mutates financial data (account balances, credit limits) — any regression is revenue-impacting.

**Migration Recommendations:**
- Decompose into multiple Java service classes: `AccountValidationService`, `AccountUpdateService`, `AccountScreenController`
- Extract the 168 IF-based validation rules into a dedicated `AccountBusinessRules` class
- Implement comprehensive unit tests for every validation path before migration
- Use database transactions to replace CICS implicit file integrity

---

### 3.2 🔴 #2 — CBTRN02C (Transaction Posting) — Score: 8.95

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 731 | Medium-large |
| **IF statements** | 93 | Very high — extensive validation |
| **PERFORM** | 61 | Many internal paragraphs |
| **Copybooks** | 5 | CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA05Y, CVTRA06Y |
| **VSAM files** | 6 | DALYTRAN, TRANSACT, ACCTDATA, CARDXREF, TCATBALF, DALYREJS |
| **CALL** | CEE3ABD | Abend on critical failure |

**Why it's #2:**
- This is the **core financial engine** — posts daily transactions to master files and updates account balances.
- Reads from daily transaction staging (DALYTRAN), validates against cross-reference and account data, then writes to 3 VSAM files simultaneously.
- Any bug in this module directly causes incorrect account balances — the highest-severity business defect possible.
- Must handle partial failures: what if TRANSACT writes but ACCTDATA update fails?

**Migration Recommendations:**
- Map to a Spring Batch `Tasklet` or `ItemProcessor` with database transaction boundaries
- Implement the posting logic as an atomic database transaction (all-or-nothing)
- Build reconciliation reports to validate migrated posting results against COBOL output
- Create comprehensive test datasets covering edge cases: zero amounts, negative amounts, expired cards, over-limit scenarios

---

### 3.3 🔴 #3 — CBACT04C (Interest Calculation) — Score: 8.65

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 652 | Medium-large |
| **IF statements** | 86 | Very high — conditional interest logic |
| **PERFORM** | 56 | Many calculation loops |
| **Copybooks** | 5 | CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA02Y, CVTRA05Y |
| **VSAM files** | 5 | ACCTDATA, CARDXREF, DISCGRP, TCATBALF, SYSTRAN |
| **Output** | SYSTRAN GDG | System-generated interest transactions |

**Why it's #3:**
- Implements the **interest calculation engine** — applies rates from disclosure groups to category balances.
- Reads interest rates per account group / transaction type / category (3-level composite key lookup).
- Generates system transactions (SYSTRAN) that represent interest charges — these flow into future posting cycles.
- 86 IF statements indicate complex conditional logic for rate tiers, minimum balances, grace periods, etc.
- Financial accuracy is regulatory-critical — even rounding differences can cause compliance issues.

**Migration Recommendations:**
- Implement as a dedicated `InterestCalculationService` with `BigDecimal` arithmetic (never `double`)
- Specify explicit `RoundingMode` (likely `HALF_UP` to match COBOL ROUNDED behavior)
- Parallel-run COBOL and Java calculation for at least 3 billing cycles to validate penny-perfect accuracy
- Extract rate lookup logic into a configurable `RateScheduleRepository`

---

### 3.4 🟠 #4 — COCRDUPC (Card Update) — Score: 8.05

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 1,560 | Large |
| **IF statements** | 148 | Second-highest in codebase |
| **EVALUATE** | 16 | Complex multi-branch logic |
| **PERFORM** | 26 | Moderate decomposition |
| **CICS commands** | 12 | READ, REWRITE, SEND, RECEIVE, XCTL, ABEND, HANDLE |
| **Copybooks** | 15 | Heavy dependency |
| **VSAM files** | 5 | ACCTDATA, CARDDATA, CARDXREF, CUSTDATA, USRSEC |

**Why it's #4:**
- Second-largest online program with 148 IF branches for card field validation (number format, expiry dates, CVV, embossed name).
- Reads from 5 VSAM files and writes back to CARDDATA — cross-entity updates.
- 16 EVALUATE statements handle AID key processing (PF keys, ENTER, CLEAR).

**Migration Recommendations:**
- Split into `CardValidationService` and `CardUpdateController`
- Map EVALUATE/AID-key handling to REST API endpoints or UI event handlers
- Implement card number validation using Luhn algorithm in Java

---

### 3.5 🟠 #5 — COCRDLIC (Card List) — Score: 7.70

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 1,459 | Large |
| **IF statements** | 122 | Very high |
| **EVALUATE** | 18 | Highest EVALUATE count in codebase |
| **CICS browse** | STARTBR, READNEXT, READPREV, ENDBR | Full browse logic |
| **Copybooks** | 13 | Heavy dependency |
| **VSAM files** | 3 | CARDDATA, CARDXREF, USRSEC |

**Why it's #5:**
- Implements CICS browse/scroll paradigm (STARTBR/READNEXT/READPREV/ENDBR) — no direct equivalent in Java.
- 18 EVALUATE statements handle page-up, page-down, selection, and function key routing.
- References two BMS maps (COCRDLI + COCRDSL) for dual-screen card selection workflow.
- Complex cursor positioning and page-state management.

**Migration Recommendations:**
- Replace CICS browse with paginated SQL queries (`LIMIT`/`OFFSET` or cursor-based)
- Map to a REST API with pagination parameters
- The dual-map workflow becomes a single-page app with list + detail views

---

### 3.6 🟠 #6 — CBSTM03A (Statement Generation) — Score: 7.65

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 924 | Large |
| **PERFORM** | 29 | Moderate |
| **CALL** | CBSTM03B × 14 calls | Heavy subroutine coupling |
| **Copybooks** | 4 | COSTM01, CUSTREC, CVACT01Y, CVACT03Y |
| **Output files** | 2 | STMTFILE (text), HTMLFILE (HTML) |
| **Input files** | 4 | TRXFL, XREFFILE, CUSTFILE, ACCTFILE |

**Why it's #6:**
- Generates customer-facing account statements in **two formats** (plain text and HTML).
- Calls CBSTM03B 14 times for file I/O operations — tightly coupled subroutine pair.
- Uses `ALTER ... TO PROCEED TO` (GOTO modification) — rare, dangerous COBOL construct.
- HTML generation is embedded in COBOL WRITE statements — needs complete rewrite with a template engine.
- Reads from 4 master files and cross-references data across customer, account, and transaction entities.

**Migration Recommendations:**
- Replace with a Java report generation library (JasperReports, Apache FOP, or HTML template engine like Thymeleaf)
- Merge CBSTM03A + CBSTM03B into a single `StatementGenerationService`
- Eliminate the `ALTER` construct — convert to standard conditional logic
- Map to a Spring Batch job with `ItemReader` (transactions) → `ItemProcessor` (aggregate) → `ItemWriter` (output)

---

### 3.7 🟠 #7 — COBIL00C (Bill Payment) — Score: 7.60

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 572 | Medium |
| **IF statements** | 10 | Low branching |
| **PERFORM** | 38 | Moderate |
| **CICS commands** | 13 | Full transaction set including WRITE, REWRITE, ASKTIME, FORMATTIME |
| **Copybooks** | 10 | COCOM01Y, COBIL00, COTTL01Y, CSDAT01Y, CSMSG01Y, CVACT01Y, CVACT03Y, CVTRA05Y |
| **VSAM files** | 3 | TRANSACT (Write), ACCTDATA (Rewrite), CARDXREF (Read) |

**Why it's #7:**
- Directly processes credit card payments — **revenue-critical** functionality.
- WRITEs new payment transactions to TRANSACT and REWRITEs account balances in ACCTDATA.
- Uses EXEC CICS ASKTIME/FORMATTIME for transaction timestamps.
- Uses EXEC CICS STARTBR/READPREV for finding the latest transaction for the card.
- Lower code complexity but very high business impact — payment errors directly affect customers.

**Migration Recommendations:**
- Map to a `PaymentService` with `@Transactional` annotation for atomicity
- Implement idempotency (payment deduplication) — critical for distributed systems
- Add payment audit logging that doesn't exist in the COBOL version
- Consider event-driven architecture for payment notifications

---

### 3.8 🟡 #8 — COTRN02C (Transaction Add) — Score: 7.30

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 783 | Medium-large |
| **IF statements** | 14 | Low-moderate |
| **EVALUATE** | 13 | High multi-branch |
| **PERFORM** | 61 | High decomposition |
| **CICS commands** | 11 | READ, WRITE, STARTBR, READPREV, ENDBR, SEND, RECEIVE |
| **CALL** | CSUTLDTC | Date conversion dependency |
| **VSAM files** | 3 | TRANSACT (Write), ACCTDATA (Read), CARDXREF (Read) |

**Why it's #8:**
- Creates new transactions — the primary data entry point for the application.
- Validates card existence via CARDXREF, account status via ACCTDATA before writing.
- Generates transaction IDs and timestamps using CEEDAYS (via CSUTLDTC).
- 13 EVALUATE statements handle AID key processing.

**Migration Recommendations:**
- Map to a `TransactionService.createTransaction()` method
- Implement as a REST POST endpoint with request validation
- Replace CEEDAYS date logic with `java.time` API
- Add input sanitization for transaction descriptions (100 chars)

---

### 3.9 🟡 #9 — CBTRN03C (Daily Transaction Report) — Score: 7.00

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 649 | Medium |
| **IF statements** | 75 | High |
| **EVALUATE** | 4 | Moderate |
| **PERFORM** | 72 | Very high internal decomposition |
| **Copybooks** | 5 | CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA05Y, CVTRA07Y |
| **Input files** | 4 | TRANFILE, CARDXREF, TRANTYPE, TRANCATG |
| **Output** | TRANREPT (GDG) | Daily report to print/archive |

**Why it's #9:**
- Generates the daily transaction report with page totals, account totals, and grand totals.
- Reads from 4 reference files and produces formatted print output.
- 72 PERFORM statements indicate highly decomposed report-building logic (headers, detail lines, subtotals, page breaks).
- Uses report layout copybook CVTRA07Y with complex formatted output fields.

**Migration Recommendations:**
- Replace with JasperReports or a CSV/PDF export service
- The 3-level control break logic (page → account → grand total) maps to SQL GROUP BY with ROLLUP
- Map the GDG output to a versioned file store or database report archive

---

### 3.10 🟡 #10 — COACTVWC (Account View) — Score: 6.70

| Metric | Value | Assessment |
|--------|-------|------------|
| **Lines of Code** | 941 | Medium-large |
| **IF statements** | 57 | Moderate-high |
| **EVALUATE** | 10 | High multi-branch |
| **PERFORM** | 21 | Moderate |
| **CICS commands** | 15 | Full set: READ, SEND, RECEIVE, XCTL, ABEND, HANDLE |
| **Copybooks** | 15 | Heavy dependency |
| **VSAM files** | 5 | ACCTDATA, CARDDATA, CARDXREF, CUSTDATA, USRSEC |

**Why it's #10:**
- Read-only account view, but reads from 5 VSAM files to compose a complete account picture.
- 57 IF statements handle display formatting and conditional field rendering.
- Gateway to COACTUPC (Account Update) — navigation chain dependency.
- Uses EXEC CICS HANDLE CONDITION for file-not-found error handling.

**Migration Recommendations:**
- Map to a `AccountViewController` with a `AccountDetailsDTO` assembler
- Replace 5 VSAM reads with a single SQL JOIN query
- The HANDLE CONDITION pattern maps to try/catch with specific exception types

---

## 4. Risk Heat Map

```
Business Impact ──►
         LOW            MEDIUM           HIGH
    ┌──────────────┬──────────────┬──────────────┐
 H  │              │  COCRDLIC    │  COACTUPC    │
 I  │              │  COCRDUPC    │  CBTRN02C    │
 G  │              │              │  CBACT04C    │
 H  │              │              │  COBIL00C    │
    ├──────────────┼──────────────┼──────────────┤
 C  │  CBTRN03C    │  COACTVWC    │  CBSTM03A   │
 o  │              │  COTRN02C    │              │
 m  │              │              │              │
 p  │              │              │              │
 l  ├──────────────┼──────────────┼──────────────┤
 e  │  COUSR00-03C │  COTRN00C    │  COSGN00C   │
 x  │  CSUTLDTC    │  COTRN01C    │  COMEN01C   │
 i  │  COBSWAIT    │  CB read pgms│              │
 t  │              │              │              │
 y  └──────────────┴──────────────┴──────────────┘
```

---

## 5. Recommended Migration Wave Plan

### Wave 1 — Foundation (Low Risk, High Value)
**Goal:** Establish the Java platform with simple, well-understood modules.

| Module | Rationale |
|--------|-----------|
| CSUTLDTC | Simple utility → `java.time` replacement |
| COSGN00C | Signon → Spring Security |
| COMEN01C / COADM01C | Menus → Router/Controller layer |
| COUSR00C–03C | User CRUD → Spring Data JPA |
| Copybook data models | VSAM records → JPA Entity classes |

**Estimated Effort:** 2–3 weeks | **Risk:** Low

### Wave 2 — Read-Only Modules (Medium Risk)
**Goal:** Migrate read-heavy programs to validate data access patterns.

| Module | Rationale |
|--------|-----------|
| COACTVWC | Account view — validates JOIN queries |
| COCRDSLC | Card view — validates card data model |
| COTRN00C / COTRN01C | Transaction list/view — validates pagination |
| CBACT01C–03C, CBCUS01C | Batch readers — validate file-to-DB migration |

**Estimated Effort:** 3–4 weeks | **Risk:** Medium

### Wave 3 — Update Modules (High Risk)
**Goal:** Migrate write operations with comprehensive validation testing.

| Module | Rationale |
|--------|-----------|
| COCRDUPC | Card update — validates write patterns |
| COCRDLIC | Card list — validates browse-to-pagination |
| COTRN02C | Transaction add — validates write + ID generation |
| COBIL00C | Bill payment — validates financial writes |
| COACTUPC | Account update — largest/most complex program |

**Estimated Effort:** 4–6 weeks | **Risk:** High — requires parallel testing

### Wave 4 — Batch Financial Core (Critical Risk)
**Goal:** Migrate batch financial processing with penny-perfect validation.

| Module | Rationale |
|--------|-----------|
| CBTRN02C | Transaction posting — core financial engine |
| CBACT04C | Interest calculation — regulatory-critical |
| CBTRN01C | Transaction validation — prerequisite for posting |
| CBEXPORT / CBIMPORT | Data migration utilities |

**Estimated Effort:** 4–6 weeks | **Risk:** Critical — requires parallel-run validation

### Wave 5 — Reporting (Medium Risk)
**Goal:** Migrate reporting with output comparison.

| Module | Rationale |
|--------|-----------|
| CBSTM03A / CBSTM03B | Statement generation — replace with template engine |
| CBTRN03C | Daily report — replace with report framework |
| CORPT00C | Report submission — replace with scheduled jobs |
| JCL batch cycle | Nightly cycle → Spring Batch + scheduler |

**Estimated Effort:** 3–4 weeks | **Risk:** Medium — output format comparison

---

## 6. Key Risk Factors and Mitigations

### 6.1 Financial Accuracy

| Risk | Severity | Mitigation |
|------|----------|------------|
| Decimal rounding differences between COBOL `COMP-3` and Java `BigDecimal` | **Critical** | Use `BigDecimal` with explicit `RoundingMode.HALF_UP`; penny-perfect parallel-run testing |
| Interest calculation rate tier logic | **Critical** | Extract all rate conditions as test cases; run 3+ billing cycles in parallel |
| Transaction posting partial failures | **High** | Implement database transactions; add reconciliation reports |

### 6.2 Data Integrity

| Risk | Severity | Mitigation |
|------|----------|------------|
| VSAM → RDBMS key mapping errors | **High** | Validate all composite keys map correctly; test with production-volume data |
| Cross-reference (CARDXREF) integrity | **High** | Implement foreign key constraints + referential integrity checks |
| GDG versioning → backup strategy | **Medium** | Design versioned backup tables or use database audit trails |

### 6.3 CICS-Specific Patterns

| Risk | Severity | Mitigation |
|------|----------|------------|
| CICS pseudo-conversational model | **High** | Map COMMAREA to HTTP session or JWT state; ensure no data loss between requests |
| EXEC CICS HANDLE CONDITION | **Medium** | Convert to Java exception handling with equivalent error routing |
| BMS map field attributes (BRT/DARK/NUM) | **Medium** | Map to HTML form input types and CSS classes |
| XCTL transfer control | **Medium** | Map to Spring MVC controller redirects or React Router navigation |

### 6.4 Batch Processing

| Risk | Severity | Mitigation |
|------|----------|------------|
| Nightly batch window timing | **Medium** | Spring Batch parallel step execution; database-level locking |
| JCL condition code chaining | **Medium** | Map COND parameters to Spring Batch step exit statuses |
| `ALTER ... TO PROCEED TO` in CBSTM03A | **High** | Refactor to standard conditional logic before migration |
| SORT utility replacement | **Low** | Use SQL ORDER BY or Java Collections.sort() |

### 6.5 Security

| Risk | Severity | Mitigation |
|------|----------|------------|
| Plaintext passwords in USRSEC | **Critical** | Implement bcrypt/scrypt hashing; one-time password migration |
| PII fields (SSN, DOB) in CUSTDATA | **High** | Implement column-level encryption; access audit logging |
| No role-based access control beyond R/A | **Medium** | Implement Spring Security roles with fine-grained permissions |
