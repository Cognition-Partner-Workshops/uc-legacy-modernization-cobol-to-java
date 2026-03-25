# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Methodology:** Programs ranked by weighted score across complexity, risk, and business impact dimensions

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Modules](#top-10-hotspot-modules)
3. [Detailed Analysis per Module](#detailed-analysis-per-module)
4. [Complexity Metrics — All Programs](#complexity-metrics--all-programs)
5. [Risk Assessment Summary](#risk-assessment-summary)
6. [Recommended Modernization Order](#recommended-modernization-order)

---

## Scoring Methodology

Each program is scored on three dimensions (1–10 scale each):

| Dimension | Weight | Factors |
|---|---:|---|
| **Complexity** | 40% | Lines of code, PERFORM count, EVALUATE count, COPY count, number of CICS commands, copybook dependencies |
| **Risk** | 30% | VSAM file access count, REWRITE/DELETE operations, CICS ABEND handling, error paths, data sensitivity (PII) |
| **Business Impact** | 30% | Core revenue function, user-facing, batch-critical, data volume, downstream dependencies |

**Weighted Score** = (Complexity × 0.4) + (Risk × 0.3) + (Business Impact × 0.3)

---

## Top 10 Hotspot Modules

| Rank | Program | Lines | PERFORMs | EVALUATEs | Complexity | Risk | Impact | **Score** | Domain |
|---:|---|---:|---:|---:|---:|---:|---:|---:|---|
| **1** | COACTUPC.cbl | 4,236 | 72 | 26 | 10 | 9 | 9 | **9.4** | Account Update |
| **2** | CBTRN02C.cbl | 731 | 27 | 8 | 7 | 10 | 10 | **8.8** | Transaction Posting |
| **3** | COCRDUPC.cbl | 1,560 | 42 | 18 | 8 | 8 | 8 | **8.0** | Card Update |
| **4** | COCRDLIC.cbl | 1,459 | 36 | 12 | 8 | 7 | 8 | **7.7** | Card List |
| **5** | CBACT04C.cbl | 652 | 24 | 4 | 6 | 9 | 9 | **7.8** | Interest Calc |
| **6** | CBSTM03A.CBL | 924 | 36 | 6 | 7 | 7 | 8 | **7.3** | Statement Gen |
| **7** | COACTVWC.cbl | 941 | 26 | 8 | 7 | 7 | 7 | **7.0** | Account View |
| **8** | COTRN02C.cbl | 783 | 21 | 3 | 6 | 7 | 8 | **6.9** | Transaction Add |
| **9** | COCRDSLC.cbl | 887 | 24 | 8 | 6 | 7 | 7 | **6.6** | Card Select |
| **10** | CBEXPORT.cbl | 582 | 21 | 0 | 5 | 8 | 7 | **6.5** | Data Export |

---

## Detailed Analysis per Module

### #1 — COACTUPC.cbl (Account Update)

| Metric | Value |
|---|---|
| **Lines of Code** | 4,236 (largest program in codebase — 2.7× next largest) |
| **PERFORM Statements** | 72 |
| **EVALUATE Statements** | 26 |
| **Copybook Dependencies** | 19 (COCOM01Y, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT03Y, CVCUS01Y, CVCRD01Y, CSLKPCDY, CSSETATY ×35, CSSTRPFY, CSUTLDWY, CSUTLDPY, DFHAID, DFHBMSCA, COACTUP) |
| **CICS Commands** | SEND MAP, RECEIVE MAP, READ ×5, REWRITE ×2, XCTL, RETURN, HANDLE ABEND, ABEND |
| **VSAM Files Accessed** | ACCTDATA (RW), CARDDATA (R), CARDXREF (R), CUSTDATA (RW) — 4 files |
| **BMS Map** | COACTUP.bms (488 lines — largest map) |

**Why #1:** This is the most complex module by every metric — largest code, most PERFORMs, most EVALUATEs, most copybook dependencies. It performs full CRUD on account data with extensive field-level validation using 35 COPY REPLACING blocks for attribute setting. It touches 4 VSAM files with both read and rewrite operations. The 51KB CSLKPCDY lookup table is embedded. Modernization will require careful decomposition into smaller services.

**Modernization Risks:**
- COPY REPLACING pattern (35 instances of CSSETATY) needs refactoring into reusable validation logic
- CSLKPCDY (51KB lookup table) should become a database table
- Complex screen state management across multiple CICS SEND/RECEIVE cycles
- Multi-file transactional integrity (account + customer updates)

---

### #2 — CBTRN02C.cbl (Transaction Posting)

| Metric | Value |
|---|---|
| **Lines of Code** | 731 |
| **PERFORM Statements** | 27 |
| **EVALUATE Statements** | 8 |
| **Copybook Dependencies** | 6 (CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y) |
| **VSAM Files Accessed** | DALYTRAN (R), TRANSACT (RW), ACCTDATA (RW), TCATBALF (RW), CARDXREF (R), DALYREJS (W) — 6 files |
| **JCL Job** | POSTTRAN.jcl |

**Why #2:** This is the **most critical batch program** — it processes every daily transaction through the system. It reads the daily transaction file, validates each record, posts to the transaction master, updates account balances, and updates category balances. It touches 6 datasets with rewrite operations on 3 of them. A defect here would corrupt financial data across the entire system.

**Modernization Risks:**
- Multi-file transactional consistency (no CICS/DB2 transaction scope — relies on sequential processing)
- Reject handling to GDG — must preserve audit trail
- Balance update logic is core to financial accuracy
- High data volume — performance-critical

---

### #3 — COCRDUPC.cbl (Card Update)

| Metric | Value |
|---|---|
| **Lines of Code** | 1,560 |
| **PERFORM Statements** | 42 |
| **EVALUATE Statements** | 18 |
| **Copybook Dependencies** | 13 (CVCRD01Y, COCOM01Y, DFHBMSCA, DFHAID, COTTL01Y, COCRDUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, CSSTRPFY) |
| **CICS Commands** | SEND MAP, RECEIVE MAP, READ, REWRITE, XCTL, RETURN, HANDLE ABEND |
| **VSAM Files Accessed** | CARDDATA (RW), CUSTDATA (R), CARDXREF (R) — 3 files |

**Why #3:** Second-largest online program with extensive field validation for card maintenance. Contains complex screen navigation with card selection, display, and update modes. The REWRITE on CARDDATA makes it a write-path program for card management.

**Modernization Risks:**
- Complex state machine for screen flow (view → edit → confirm → save)
- Card number handling requires PCI-DSS compliance in modernized system
- Integration with cross-reference for account lookup

---

### #4 — COCRDLIC.cbl (Card List)

| Metric | Value |
|---|---|
| **Lines of Code** | 1,459 |
| **PERFORM Statements** | 36 |
| **EVALUATE Statements** | 12 |
| **CICS Commands** | STARTBR, READNEXT, READPREV, ENDBR, SEND MAP, RECEIVE MAP, XCTL |
| **VSAM Files Accessed** | CARDDATA (Browse), CARDXREF (R) — 2 files |

**Why #4:** Complex browsing logic with forward/backward pagination through VSAM browse operations (STARTBR/READNEXT/READPREV/ENDBR). The paginated list pattern is the most complex UI interaction in the application, managing cursor position and page boundaries.

**Modernization Risks:**
- VSAM browse → SQL pagination with cursors/offsets
- Bidirectional scrolling state management
- Dynamic navigation to card detail/update screens

---

### #5 — CBACT04C.cbl (Interest Calculation)

| Metric | Value |
|---|---|
| **Lines of Code** | 652 |
| **PERFORM Statements** | 24 |
| **EVALUATE Statements** | 4 |
| **Copybook Dependencies** | 5 (CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y, CVTRA01Y) |
| **VSAM Files Accessed** | ACCTDATA (R), CARDXREF (R + AIX), DISCGRP (R), TCATBALF (R), SYSTRAN (W) — 5 files |
| **JCL Job** | INTCALC.jcl (with date parameter) |

**Why #5:** Core financial calculation engine. Reads every account, looks up disclosure rates by account group, calculates interest per category balance, and generates system transactions. Uses an alternate index path on CARDXREF. The parameterized date input (PARM='2022071800') drives calculation periods.

**Modernization Risks:**
- Financial calculation accuracy must be preserved exactly (rounding rules)
- Alternate index access patterns → SQL JOINs
- Generated system transactions must match existing format
- Date-parameterized execution model

---

### #6 — CBSTM03A.CBL (Statement Generation)

| Metric | Value |
|---|---|
| **Lines of Code** | 924 |
| **PERFORM Statements** | 36 |
| **EVALUATE Statements** | 6 |
| **Subroutine Calls** | CBSTM03B (×10 — file I/O delegation) |
| **VSAM Files Accessed** | TRANSACT (R), CARDXREF (R), ACCTDATA (R), CUSTDATA (R), STATEMNT (W) |
| **JCL Job** | CREASTMT.JCL (multi-step with SORT) |

**Why #6:** Generates customer-facing account statements in both text and HTML formats. Calls CBSTM03B subroutine 10 times for file I/O operations. Part of a multi-step JCL job that first sorts transactions by card number, then generates statements. The HTML output generation is unique in this COBOL codebase.

**Modernization Risks:**
- CALL-based subroutine pattern (CBSTM03A → CBSTM03B) needs decomposition
- HTML generation in COBOL → modern template engine
- Multi-step JCL dependency (SORT prerequisite)
- Customer PII in output (name, address)

---

### #7 — COACTVWC.cbl (Account View)

| Metric | Value |
|---|---|
| **Lines of Code** | 941 |
| **PERFORM Statements** | 26 |
| **EVALUATE Statements** | 8 |
| **CICS Commands** | SEND MAP, RECEIVE MAP, READ ×3, SEND TEXT, XCTL, RETURN, HANDLE ABEND, ABEND |
| **VSAM Files Accessed** | ACCTDATA (R), CARDDATA (R), CARDXREF (R), CUSTDATA (R) — 4 files |

**Why #7:** Read-only but touches 4 VSAM files to compose a complete account view. Contains error handling with SEND TEXT for diagnostic messages. Shares significant logic patterns with COACTUPC but without write operations.

**Modernization Risks:**
- 4-file join logic → SQL with proper joins
- CSSTRPFY string processing functions
- Shared patterns with COACTUPC — opportunity for code reuse

---

### #8 — COTRN02C.cbl (Transaction Add)

| Metric | Value |
|---|---|
| **Lines of Code** | 783 |
| **PERFORM Statements** | 21 |
| **EVALUATE Statements** | 3 |
| **CALL Statements** | CSUTLDTC (×2 — date validation) |
| **VSAM Files Accessed** | ACCTDATA (R), CARDXREF (R), TRANSACT (W) — 3 files |

**Why #8:** Online entry point for new transactions. Validates date input via CSUTLDTC subroutine calls and writes directly to the transaction VSAM file. Represents the primary online data creation path alongside batch posting.

**Modernization Risks:**
- Dual entry paths (online + batch) for transactions must be unified
- Date validation via external CALL → inline validation library
- Real-time write to TRANSACT must maintain consistency with batch posting

---

### #9 — COCRDSLC.cbl (Card Select/View)

| Metric | Value |
|---|---|
| **Lines of Code** | 887 |
| **PERFORM Statements** | 24 |
| **EVALUATE Statements** | 8 |
| **CICS Commands** | SEND MAP, RECEIVE MAP, READ ×2, SEND TEXT, XCTL, RETURN, HANDLE ABEND |
| **VSAM Files Accessed** | CARDDATA (R), CUSTDATA (R), CARDXREF (R) — 3 files |

**Why #9:** Card detail view with navigation to card update. Contains the card selection logic that bridges the card list and card update flows. Complex screen state management.

**Modernization Risks:**
- Navigation state management between list → detail → update
- Card number display (PCI-DSS masking considerations)
- Shared CSSTRPFY string processing

---

### #10 — CBEXPORT.cbl (Data Export)

| Metric | Value |
|---|---|
| **Lines of Code** | 582 |
| **PERFORM Statements** | 21 |
| **EVALUATE Statements** | 0 |
| **VSAM Files Accessed** | ACCTDATA (R), CARDDATA (R), CARDXREF (R), CUSTDATA (R), TRANSACT (R), EXPORT.DATA (W) — 6 files |
| **JCL Job** | CBEXPORT.jcl |

**Why #10:** Reads all 5 core VSAM files and writes them to a single combined export file. While structurally simple (no EVALUATEs), the breadth of file access (6 datasets) and its role as the data migration path make it high-risk. Paired with CBIMPORT for round-trip data transfer.

**Modernization Risks:**
- Touches all core data — any format change cascades here
- Export format is the only documented interchange format
- Must maintain backward compatibility during phased migration
- PII data in export (SSN, addresses)

---

## Complexity Metrics — All Programs

### Core Programs (sorted by line count)

| Program | Lines | PERFORMs | EVALUATEs | COPYs | CALLs | CICS Cmds | Type |
|---|---:|---:|---:|---:|---:|---:|---|
| COACTUPC.cbl | 4,236 | 72 | 26 | 55 | 0 | 15 | Online |
| COCRDUPC.cbl | 1,560 | 42 | 18 | 13 | 0 | 8 | Online |
| COCRDLIC.cbl | 1,459 | 36 | 12 | 10 | 0 | 14 | Online |
| COACTVWC.cbl | 941 | 26 | 8 | 15 | 0 | 12 | Online |
| CBSTM03A.CBL | 924 | 36 | 6 | 3 | 10 | 0 | Batch |
| COCRDSLC.cbl | 887 | 24 | 8 | 13 | 0 | 9 | Online |
| COTRN02C.cbl | 783 | 21 | 3 | 10 | 2 | 0 | Online |
| CBTRN02C.cbl | 731 | 27 | 8 | 6 | 1 | 0 | Batch |
| COTRN00C.cbl | 699 | 26 | 5 | 8 | 0 | 8 | Online |
| COUSR00C.cbl | 695 | 23 | 2 | 7 | 0 | 6 | Online |
| CBACT04C.cbl | 652 | 24 | 4 | 5 | 1 | 0 | Batch |
| CBTRN03C.cbl | 649 | 18 | 0 | 5 | 1 | 0 | Batch |
| CORPT00C.cbl | 649 | 21 | 4 | 8 | 2 | 6 | Online |
| CBEXPORT.cbl | 582 | 21 | 0 | 6 | 1 | 0 | Batch |
| COBIL00C.cbl | 572 | 15 | 3 | 9 | 0 | 12 | Online |
| CBTRN01C.cbl | 494 | 18 | 8 | 6 | 1 | 0 | Batch |
| CBIMPORT.cbl | 487 | 15 | 0 | 6 | 1 | 0 | Batch |
| CBACT01C.cbl | 430 | 16 | 0 | 2 | 2 | 0 | Batch |
| COUSR02C.cbl | 414 | 12 | 4 | 8 | 0 | 6 | Online |
| COUSR03C.cbl | 359 | 10 | 3 | 8 | 0 | 6 | Online |
| COTRN01C.cbl | 330 | 10 | 3 | 7 | 0 | 5 | Online |
| COMEN01C.cbl | 308 | 10 | 5 | 8 | 0 | 5 | Online |
| COUSR01C.cbl | 299 | 9 | 2 | 7 | 0 | 6 | Online |
| COADM01C.cbl | 288 | 9 | 4 | 8 | 0 | 5 | Online |
| COSGN00C.cbl | 260 | 10 | 3 | 7 | 0 | 5 | Online |
| CBSTM03B.CBL | 230 | 4 | 0 | 0 | 0 | 0 | Batch |
| CBACT02C.cbl | 178 | 6 | 0 | 1 | 1 | 0 | Batch |
| CBACT03C.cbl | 178 | 6 | 0 | 1 | 1 | 0 | Batch |
| CBCUS01C.cbl | 178 | 6 | 0 | 1 | 1 | 0 | Batch |
| CSUTLDTC.cbl | 157 | 3 | 0 | 0 | 1 | 0 | Utility |
| COBSWAIT.cbl | 41 | 0 | 0 | 0 | 1 | 0 | Utility |

### Optional Module Programs

| Program | Lines | Module | Type |
|---|---:|---|---|
| COTRTLIC.cbl | 2,098 | Transaction Type DB2 | Online (CICS+DB2) |
| COTRTUPC.cbl | 1,702 | Transaction Type DB2 | Online (CICS+DB2) |
| COPAUS0C.cbl | 1,032 | Authorization IMS | Online (CICS) |
| COPAUA0C.cbl | 1,026 | Authorization IMS | Online (CICS+MQ) |
| COACCT01.cbl | 620 | VSAM-MQ | Batch (MQ) |
| COPAUS1C.cbl | 604 | Authorization IMS | Online (CICS) |
| CODATE01.cbl | 524 | VSAM-MQ | Batch (MQ) |
| CBPAUP0C.cbl | 386 | Authorization IMS | Batch |
| COPAUS2C.cbl | 244 | Authorization IMS | Online (CICS+DB2) |
| COBTUPDT.cbl | 237 | Transaction Type DB2 | Batch (DB2) |

---

## Risk Assessment Summary

### High-Risk Factors

| Risk Factor | Programs Affected | Mitigation |
|---|---|---|
| **Multi-file writes** | COACTUPC, CBTRN02C, CBACT04C, COBIL00C | Implement database transactions with proper rollback |
| **PII data handling** | COACTUPC, COACTVWC, CBEXPORT, CBSTM03A | Encrypt at rest, mask in UI, audit access |
| **Financial calculations** | CBTRN02C, CBACT04C, COBIL00C | Parallel-run validation during migration |
| **Plain-text passwords** | COSGN00C, COUSR01C, COUSR02C | Hash with bcrypt/Argon2 immediately |
| **No transaction scope** | All batch programs | Add database transaction boundaries |
| **Hard-coded lookups** | COACTUPC (CSLKPCDY — 51KB) | Externalize to database reference tables |
| **GDG dependencies** | CBTRN02C, CBACT04C, COMBTRAN, TRANBKP | Replace with versioned file storage or database |
| **Alternate index access** | CBACT04C (CARDXREF AIX) | Replace with SQL JOINs |

### Coupling Analysis

Programs with the highest dependency fan-out (most copybooks + files):

| Program | Copybooks | VSAM Files | Total Deps | Risk Level |
|---|---:|---:|---:|---|
| COACTUPC | 19 | 4 | 23 | **Critical** |
| COACTVWC | 15 | 4 | 19 | High |
| CBEXPORT | 6 | 6 | 12 | High |
| COCRDLIC | 10 | 2 | 12 | High |
| COCRDUPC | 13 | 3 | 16 | High |
| CBTRN02C | 6 | 6 | 12 | **Critical** |
| CBACT04C | 5 | 5 | 10 | High |

---

## Recommended Modernization Order

### Phase 1: Foundation & Security (Weeks 1–4)

| Priority | Module | Rationale |
|---|---|---|
| 1a | COSGN00C + CSUSR01Y | Fix password hashing first — security foundation |
| 1b | COCOM01Y (COMMAREA) | Define session/request model — all programs depend on this |
| 1c | CSUTLDTC | Utility — small, self-contained, validates approach |

### Phase 2: Read-Only Screens (Weeks 5–8)

| Priority | Module | Rationale |
|---|---|---|
| 2a | COACTVWC | Account View — read-only, tests 4-file join pattern |
| 2b | COCRDLIC + COCRDSLC | Card List/View — tests pagination pattern |
| 2c | COTRN00C + COTRN01C | Transaction List/View — tests browse pattern |
| 2d | COUSR00C | User List — simpler browse, admin-only |

### Phase 3: Write Screens (Weeks 9–14)

| Priority | Module | Rationale |
|---|---|---|
| 3a | COUSR01C, COUSR02C, COUSR03C | User CRUD — simpler write patterns, admin-only |
| 3b | COCRDUPC | Card Update — single-file write |
| 3c | COTRN02C (online) | Transaction Add — validates write + date logic |
| 3d | COBIL00C | Bill Payment — validates balance updates |
| 3e | **COACTUPC** | Account Update — **most complex, do last** in write phase |

### Phase 4: Batch Processing (Weeks 15–20)

| Priority | Module | Rationale |
|---|---|---|
| 4a | CBACT01C–03C, CBCUS01C | File readers — simple, validate batch I/O |
| 4b | **CBTRN02C** (batch) | Transaction Posting — **most critical batch** |
| 4c | **CBACT04C** | Interest Calculation — financial accuracy critical |
| 4d | CBSTM03A + CBSTM03B | Statement Generation — CALL pattern |
| 4e | CBTRN03C | Transaction Report |
| 4f | CBEXPORT + CBIMPORT | Data Migration — needed for parallel-run |

### Phase 5: Optional Modules (Weeks 21–26)

| Priority | Module | Rationale |
|---|---|---|
| 5a | Transaction Type DB2 | Already uses DB2 — easier transition |
| 5b | VSAM-MQ module | MQ → message broker (Kafka/RabbitMQ) |
| 5c | Authorization IMS module | Most complex — IMS + DB2 + MQ |

### Migration Risk Mitigations

1. **Parallel Run:** Keep COBOL and Java running side-by-side for Phase 4 (batch), comparing outputs record-by-record
2. **Data Validation:** Use CBEXPORT/CBIMPORT to create snapshot comparisons between old and new systems
3. **Incremental Cutover:** Migrate read paths first, then write paths, maintaining VSAM as source of truth until full validation
4. **Regression Suite:** Capture current BMS screen flows as automated test cases before migration
5. **Financial Reconciliation:** Run CBACT04C interest calculations in both systems and compare to zero tolerance
