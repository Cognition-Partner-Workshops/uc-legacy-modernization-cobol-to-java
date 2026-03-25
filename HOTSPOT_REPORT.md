# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Methodology:** Weighted scoring across Code Complexity, Dependency Risk, Business Impact, and Modernization Effort

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Modules](#top-10-hotspot-modules)
3. [Detailed Hotspot Analysis](#detailed-hotspot-analysis)
4. [Risk Heat Map](#risk-heat-map)
5. [Recommended Migration Order](#recommended-migration-order)
6. [Quick Wins](#quick-wins)

---

## Scoring Methodology

Each module is scored on four dimensions (1–5 scale, 5 = highest concern):

| Dimension | Weight | What It Measures |
|-----------|--------|-----------------|
| **Code Complexity** | 30% | Lines of code, PERFORM count, branching density, number of paragraphs |
| **Dependency Risk** | 25% | Fan-in (how many modules depend on it), fan-out (how many files/programs it touches), copybook count |
| **Business Impact** | 30% | Financial processing, data integrity, user-facing criticality, regulatory exposure |
| **Modernization Effort** | 15% | CICS commands, BMS maps, VSAM I/O patterns, special constructs (COMP-3, REDEFINES, etc.) |

**Composite Score** = (Complexity × 0.30) + (Dependency × 0.25) + (Business Impact × 0.30) + (Effort × 0.15)

---

## Top 10 Hotspot Modules

| Rank | Program | Type | LOC | Composite Score | Primary Risk |
|------|---------|------|-----|----------------|-------------|
| **1** | **COACTUPC** | Online CICS | 4,236 | **4.60** | Largest program; updates accounts + customers with complex validation |
| **2** | **CBTRN02C** | Batch | 731 | **4.45** | Core transaction posting — writes to 3 VSAM files, reject handling |
| **3** | **CBACT04C** | Batch | 652 | **4.35** | Interest calculator — financial math, 5-file I/O, LINKAGE SECTION parms |
| **4** | **COCRDUPC** | Online CICS | 1,560 | **4.15** | Card update with ABEND handling, REWRITE operations |
| **5** | **COCRDLIC** | Online CICS | 1,459 | **3.95** | Card list — complex pagination with STARTBR/READNEXT/READPREV |
| **6** | **CBSTM03A** | Batch | 924 | **3.85** | Statement generation — calls CBSTM03B subroutine, HTML output |
| **7** | **CBTRN03C** | Batch | 649 | **3.70** | Transaction report — 5-file joins, formatted report output |
| **8** | **CBEXPORT** | Batch | 582 | **3.55** | Data migration export — reads 5 VSAM files, multi-record output |
| **9** | **COTRN02C** | Online CICS | 783 | **3.50** | Transaction add — validates and writes with STARTBR/READPREV for ID gen |
| **10** | **COBIL00C** | Online CICS | 572 | **3.45** | Bill payment — financial writes with ASKTIME/FORMATTIME |

---

## Detailed Hotspot Analysis

### #1 — COACTUPC (Account Update) — Score: 4.60

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **5** | 4,236 lines — largest program in codebase. 64 PERFORMs, deep paragraph nesting, extensive EVALUATE/IF chains for field-level validation |
| Dependency | **5** | Reads/writes 4 VSAM files (ACCTDATA, CARDDATA, CARDXREF, CUSTDATA). Uses COMMAREA for navigation. BMS map COACTUP. 5+ copybooks |
| Business Impact | **5** | Updates financial account records (balances, limits). Customer data modification. Data integrity critical — incorrect updates cause financial discrepancy |
| Effort | **3** | CICS commands (READ, REWRITE, RECEIVE, SEND, XCTL, HANDLE ABEND). BMS map conversion needed. Program-specific COMMAREA extension |

**Key Risks:**
- Monolithic structure — single program handles all account update scenarios
- Mixed concerns — account update AND customer update in same program
- Complex screen flow with program-specific COMMAREA extension (WS-THIS-PROGCOMMAREA)
- HANDLE ABEND logic needs careful mapping to Java exception handling

**Migration Recommendation:** Decompose into separate Account and Customer update services. Map EVALUATE chains to strategy pattern or validation framework.

---

### #2 — CBTRN02C (Transaction Posting) — Score: 4.45

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 731 lines, 61 PERFORMs. Multi-file processing loop with cross-reference lookups and conditional reject routing |
| Dependency | **5** | Central to batch cycle. Reads DALYTRAN + CARDXREF + ACCTDATA. Writes TRANSACT + TCATBALF + DALYREJS. 6 file references |
| Business Impact | **5** | **Core financial processing.** Posts all daily transactions. Errors here cause missing/duplicate transactions. Reject handling affects reconciliation |
| Effort | **3** | Standard batch I/O patterns. No CICS. Multiple VSAM access modes (sequential + random). COMP-3 fields in category balance |

**Key Risks:**
- Single point of failure for daily transaction processing
- Complex reject logic — rejected transactions written to GDG with reason codes
- Transaction category balance accumulation must be exactly correct for interest calculation downstream
- TCATBALF writes feed directly into CBACT04C (interest calc) — tight coupling

**Migration Recommendation:** Implement as Spring Batch job with chunk-oriented processing. Use database transactions for atomicity. Add comprehensive audit logging.

---

### #3 — CBACT04C (Interest Calculator) — Score: 4.35

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 652 lines, 56 PERFORMs. Financial computation logic, multi-file cross-referencing, account group rate lookups |
| Dependency | **5** | Reads 4 files (TCATBALF, CARDXREF, DISCGRP, ACCTDATA). Updates ACCTDATA (REWRITE). Writes interest transactions to SYSTRAN. LINKAGE SECTION for date parameter |
| Business Impact | **5** | **Financial accuracy critical.** Computes interest and fees. Errors directly affect customer bills. Regulatory implications for incorrect interest rates |
| Effort | **3** | LINKAGE SECTION parameter passing (JCL PARM). REWRITE on I-O opened file. COMP-3 decimal arithmetic. DB2-style timestamp formatting |

**Key Risks:**
- Financial math precision — COBOL decimal arithmetic must map exactly to Java BigDecimal
- Interest rate lookup logic chains: Account → Group ID → Disclosure Group → Rate
- REWRITE of account master with accumulated interest — must handle concurrent access in Java
- Date parameter passed via JCL PARM through LINKAGE SECTION

**Migration Recommendation:** Extract interest calculation as a pure business logic service with BigDecimal. Wrap in Spring Batch step. Add extensive unit tests for financial precision. Consider regulatory compliance testing.

---

### #4 — COCRDUPC (Card Update) — Score: 4.15

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 1,560 lines, 26 PERFORMs. HANDLE ABEND, field validation, card status management |
| Dependency | **4** | Reads CARDDATA + CARDXREF. Writes CARDDATA (REWRITE). BMS map COCRDUP. COMMAREA navigation |
| Business Impact | **4** | Card data updates (status, embossed name, expiration). Incorrect updates can disable valid cards or enable expired ones |
| Effort | **4** | CICS HANDLE ABEND, ABEND command for error scenarios. BMS map with field-level attribute control. XCTL navigation |

**Key Risks:**
- HANDLE ABEND / EXEC CICS ABEND pattern — unusual error handling needs careful mapping
- Card status changes have downstream effects on transaction processing
- BMS field-level attribute manipulation for edit/display mode switching

**Migration Recommendation:** Map to REST endpoint with card update DTO. Convert ABEND handling to structured exception handling. Separate validation logic into reusable validators.

---

### #5 — COCRDLIC (Card List) — Score: 3.95

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 1,459 lines, 34 PERFORMs. Complex pagination with forward/backward browsing using STARTBR/READNEXT/READPREV/ENDBR |
| Dependency | **4** | Reads CARDDATA via browse. Multiple XCTL targets (menu, card detail, card update). BMS map COCRDLI (344 lines — largest BMS map) |
| Business Impact | **3** | Read-only listing — lower financial risk. But primary navigation point for card management |
| Effort | **5** | Most complex CICS browse pattern in the codebase. STARTBR/READNEXT/READPREV/ENDBR cycle. Dynamic XCTL dispatch. Largest BMS map (344 lines with repeating row groups) |

**Key Risks:**
- VSAM browse cursor management (STARTBR/ENDBR) has no direct Java equivalent
- Repeating row group in BMS map — needs mapping to paginated list UI
- Forward/backward pagination state maintained across pseudo-conversational CICS returns

**Migration Recommendation:** Replace with paginated REST API + frontend table component. Use Spring Data pagination. This is the most complex CICS browse pattern to convert.

---

### #6 — CBSTM03A (Statement Generation) — Score: 3.85

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 924 lines, 29 PERFORMs. Multi-step processing: sort input, group by card/account, format output. Calls subroutine CBSTM03B |
| Dependency | **4** | Depends on CBSTM03B (subroutine CALL). Reads sorted transaction file + account + customer + xref (via subroutine). Produces both text and HTML output |
| Business Impact | **4** | Customer-facing statements. Statement errors create customer complaints and regulatory issues. Dual output format (text + HTML) |
| Effort | **3** | CALL to subroutine (program linkage). File I/O via called program. Report formatting logic. HTML generation in COBOL |

**Key Risks:**
- Cross-program CALL with shared data areas — CBSTM03B accesses 4 files on behalf of CBSTM03A
- HTML output generation in COBOL — unusual pattern
- Statement formatting must match regulatory requirements
- Depends on upstream SORT step in JCL (CREASTMT.JCL) for input ordering

**Migration Recommendation:** Implement as Spring Batch job with HTML template engine (Thymeleaf/FreeMarker). Merge CBSTM03A and CBSTM03B into single service since the separation was a COBOL modularity pattern.

---

### #7 — CBTRN03C (Transaction Report) — Score: 3.70

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 649 lines, 72 PERFORMs (highest PERFORM density in codebase). Report formatting with page/account/grand totals. 5-file cross-reference joins |
| Dependency | **4** | Reads 5 files: TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM. Writes formatted report. Uses CVTRA07Y report layout copybook |
| Business Impact | **3** | Reporting — no write to master files. But used for daily reconciliation and audit |
| Effort | **3** | Standard batch. Report formatting with page breaks, running totals. Date range parameter input |

**Key Risks:**
- Highest PERFORM count (72) — most internally complex control flow
- 5-file cross-reference lookup pattern (transaction → card → account, type → description, category → description)
- Report pagination logic with page totals and grand totals

**Migration Recommendation:** Replace with JasperReports or similar. Map 5-file lookups to SQL JOINs.

---

### #8 — CBEXPORT (Data Export) — Score: 3.55

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **3** | 582 lines, 45 PERFORMs. Reads 5 input files sequentially, writes single multi-type output. Clear structure |
| Dependency | **4** | Reads ALL 5 core VSAM files (Customer, Account, Xref, Transaction, Card). Single output file with type discriminator |
| Business Impact | **4** | Data migration — must export complete and accurate data. Any missing records break the migration |
| Effort | **2** | Clean batch structure. No CICS. Straightforward sequential reads. Modern timestamp handling (ACCEPT FROM DATE) |

**Key Risks:**
- Multi-record type output file with discriminator — needs careful parsing
- Must read ALL master files completely — performance at scale
- Data completeness validation needed

**Migration Recommendation:** Replace with database export utility or ETL tool. Good candidate for early migration since it has clean interfaces.

---

### #9 — COTRN02C (Transaction Add) — Score: 3.50

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **3** | 783 lines, 61 PERFORMs. Transaction ID generation using STARTBR/READPREV for last-used ID. Validation + write |
| Dependency | **4** | Reads ACCTDATA, CARDXREF, TRANSACT. Writes TRANSACT. BMS map COTRN02. COMMAREA navigation |
| Business Impact | **4** | Creates financial transactions. Duplicate or invalid transactions cause reconciliation issues |
| Effort | **4** | CICS WRITE (new record). STARTBR/READPREV for ID generation. BMS map. Pseudo-conversational state management |

**Key Risks:**
- Transaction ID generation via STARTBR/READPREV on TRANSACT — race condition potential in Java (needs sequence generator)
- Financial validation: account active, card valid, amount within limits
- Pseudo-conversational state — map screen shows data, user enters, program re-invoked to process

**Migration Recommendation:** Replace ID generation with database sequence. Map to REST POST endpoint with request validation. Use optimistic locking for concurrent writes.

---

### #10 — COBIL00C (Bill Payment) — Score: 3.45

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **3** | 572 lines, 38 PERFORMs. Balance calculation, payment processing, transaction creation |
| Dependency | **3** | Reads ACCTDATA, CARDXREF. Writes ACCTDATA (REWRITE) + TRANSACT (WRITE). CICS ASKTIME/FORMATTIME for timestamps |
| Business Impact | **5** | **Directly moves money.** Full and partial balance payments. Incorrect processing causes financial loss to customer or institution |
| Effort | **3** | CICS ASKTIME + FORMATTIME (timestamp generation). STARTBR/READPREV for browsing. REWRITE + WRITE in same transaction |

**Key Risks:**
- Financial transaction — payment processing must be atomic (update balance + write transaction)
- CICS ASKTIME/FORMATTIME — timestamp generation needs mapping to Java Instant/LocalDateTime
- Balance validation: payment cannot exceed current balance (or can it? — business rule to verify)
- No explicit CICS SYNCPOINT — relies on implicit task-level commit

**Migration Recommendation:** Implement with @Transactional for atomicity. Use Java time APIs for timestamps. Add idempotency key to prevent double payments. This module needs the most thorough testing due to financial impact.

---

## Risk Heat Map

Visual summary of risk across all dimensions:

```
                    Code         Dependency    Business     Modernization
Program             Complexity   Risk          Impact       Effort
──────────────────  ───────────  ────────────  ───────────  ─────────────
COACTUPC  (4236L)   █████        █████         █████        ███░░
CBTRN02C  ( 731L)   ████░        █████         █████        ███░░
CBACT04C  ( 652L)   ████░        █████         █████        ███░░
COCRDUPC  (1560L)   ████░        ████░         ████░        ████░
COCRDLIC  (1459L)   ████░        ████░         ███░░        █████
CBSTM03A  ( 924L)   ████░        ████░         ████░        ███░░
CBTRN03C  ( 649L)   ████░        ████░         ███░░        ███░░
CBEXPORT  ( 582L)   ███░░        ████░         ████░        ██░░░
COTRN02C  ( 783L)   ███░░        ████░         ████░        ████░
COBIL00C  ( 572L)   ███░░        ███░░         █████        ███░░

█ = 1 point on 5-point scale, ░ = empty
```

---

## Recommended Migration Order

Based on dependency analysis and risk scoring, the recommended migration order is:

### Phase 1: Foundation (Weeks 1–4)
**Migrate data layer and utilities first — zero business logic risk**

| Order | Component | Rationale |
|-------|-----------|-----------|
| 1.1 | Copybooks → Java POJOs/DTOs | All programs depend on these; must exist first |
| 1.2 | VSAM Files → Database Tables | DDL from copybook analysis; load sample data |
| 1.3 | COBSWAIT, CSUTLDTC | Simple utilities, no business logic |
| 1.4 | COCOM01Y → Session/Context object | Shared communication area → Java session state |

### Phase 2: Read-Only Operations (Weeks 5–8)
**Low risk — no data modification**

| Order | Component | Rationale |
|-------|-----------|-----------|
| 2.1 | CBACT02C, CBACT03C, CBCUS01C | Simple file readers — validate data migration |
| 2.2 | COACTVWC | Account view — first online screen (read-only) |
| 2.3 | COCRDSLC | Card view — read-only |
| 2.4 | COTRN01C | Transaction view — read-only |
| 2.5 | COTRN00C, COCRDLIC, COUSR00C | List screens — pagination logic |

### Phase 3: CRUD Operations (Weeks 9–14)
**Moderate risk — data modification with validation**

| Order | Component | Rationale |
|-------|-----------|-----------|
| 3.1 | COUSR01C, COUSR02C, COUSR03C | User CRUD — isolated, low financial risk |
| 3.2 | COCRDUPC | Card update — moderate complexity |
| 3.3 | COTRN02C (online) | Transaction add — financial write |
| 3.4 | COACTUPC | **Highest complexity.** Decompose into Account + Customer update services |

### Phase 4: Financial Processing (Weeks 15–20)
**High risk — core business logic, financial accuracy**

| Order | Component | Rationale |
|-------|-----------|-----------|
| 4.1 | CBTRN01C (batch) | Transaction validation — prereq for posting |
| 4.2 | CBTRN02C (batch) | **Core posting logic.** Extensive parallel testing required |
| 4.3 | CBACT04C | **Interest calculator.** Financial precision testing critical |
| 4.4 | COBIL00C | **Bill payment.** Atomic financial operations |

### Phase 5: Reporting & Migration (Weeks 21–24)
**Moderate risk — output-focused, replaceable**

| Order | Component | Rationale |
|-------|-----------|-----------|
| 5.1 | CBTRN03C | Transaction report — replace with modern reporting |
| 5.2 | CBSTM03A + CBSTM03B | Statement generation — template-based replacement |
| 5.3 | CORPT00C | Report trigger — convert to scheduled job trigger |
| 5.4 | CBEXPORT + CBIMPORT | Data migration — ETL replacement |
| 5.5 | CBACT01C | Account extract — utility replacement |

### Phase 6: Navigation & Security (Weeks 25–26)
**Complete the shell — authentication and menu routing**

| Order | Component | Rationale |
|-------|-----------|-----------|
| 6.1 | COSGN00C | Authentication — replace with Spring Security |
| 6.2 | COMEN01C, COADM01C | Menu routing — replace with frontend routing |

---

## Quick Wins

Low-effort, high-value modules to migrate first for early confidence:

| Module | LOC | Why It's a Quick Win |
|--------|-----|---------------------|
| COBSWAIT | 41 | Trivial utility — `Thread.sleep()` in Java |
| CBACT02C | 178 | Simple file reader — validates card data migration |
| CBACT03C | 178 | Simple file reader — validates xref data migration |
| CBCUS01C | 178 | Simple file reader — validates customer data migration |
| COUSR01C | 299 | Simple CRUD — add user. Isolated from financial data |
| COSGN00C | 260 | Login screen — well-understood pattern, Spring Security |
| COADM01C | 288 | Menu router — becomes frontend route configuration |
| COMEN01C | 308 | Menu router — becomes frontend route configuration |

These 8 programs (1,730 lines combined) can be migrated in the first 2–3 weeks, covering **8.4% of total LOC** while building team confidence and validating the migration framework.
