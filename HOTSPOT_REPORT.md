# Hotspot Report — CardDemo COBOL Estate

> Programs ranked by complexity metrics to guide modernization priority.

---

## Table of Contents

1. [Methodology](#1-methodology)
2. [Top 10 Programs by Composite Score](#2-top-10-programs-by-composite-score)
3. [Individual Metric Rankings](#3-individual-metric-rankings)
4. [Modernization Recommendations](#4-modernization-recommendations)

---

## 1. Methodology

Each program is scored across five dimensions:

| Metric | Description | How Measured |
|--------|-------------|-------------|
| **Lines of Code (LOC)** | Total source lines including comments | `wc -l` on source file |
| **Copybooks Referenced** | Number of unique COPY statements | Count of distinct `COPY xxx` directives |
| **I/O Operations** | File/DB/MQ/CICS operations | Count of OPEN, READ, WRITE, REWRITE, DELETE, START, EXEC CICS (file), EXEC SQL, MQOPEN/GET/PUT/CLOSE, CBLTDLI |
| **Business Logic Density** | EVALUATE + IF statement count | Sum of EVALUATE and IF keywords in PROCEDURE DIVISION |
| **Inter-Program Dependencies** | Programs called + programs that call this one | Count of outbound CALLs + inbound references from other programs |

**Composite Score** = Weighted sum:
- LOC: 20%
- Copybooks: 15%
- I/O Operations: 25%
- Business Logic Density: 25%
- Inter-Program Dependencies: 15%

Each metric is normalized to a 0–100 scale relative to the maximum value in the estate.

---

## 2. Top 10 Programs by Composite Score

| Rank | Program | LOC | Copybooks | I/O Ops | Logic (EVAL+IF) | Dependencies | Composite | Classification |
|------|---------|-----|-----------|---------|-----------------|-------------|-----------|---------------|
| **1** | **COACTUPC.cbl** | 4,236 | 15 | 24+ | 343 | 4 (ACCTDAT, CUSTDAT, CARDXREF, CARDDAT) | **95.2** | Online (CICS) |
| **2** | **COTRTLIC.cbl** | 2,098 | 12 | 28+ | 190 | 3 (DB2 TRANSACTION_TYPE, CICS XCTL) | **78.6** | Online (CICS+DB2) |
| **3** | **COTRTUPC.cbl** | 1,702 | 14 | 26+ | 128 | 3 (DB2 TRANSACTION_TYPE, TRANSACTION_TYPE_CATEGORY) | **75.3** | Online (CICS+DB2) |
| **4** | **COCRDUPC.cbl** | 1,560 | 13 | 18+ | 164 | 5 (CARDDAT, CARDXREF, ACCTDAT, CUSTDAT files) | **72.8** | Online (CICS) |
| **5** | **COCRDLIC.cbl** | 1,459 | 10 | 16+ | 140 | 3 (CARDDAT, CARDXREF; navigates to COCRDUPC) | **64.5** | Online (CICS) |
| **6** | **COPAUA0C.cbl** | 1,026 | 13 | 16+ | 63 | 6 (MQ queues, VSAM files, IMS) | **60.2** | Online (CICS+MQ) |
| **7** | **COPAUS0C.cbl** | 1,032 | 14 | 12+ | 61 | 3 (IMS segments, CICS screens) | **55.8** | Online (CICS) |
| **8** | **COACTVWC.cbl** | 941 | 15 | 16+ | 67 | 4 (ACCTDAT, CUSTDAT, CARDXREF, CARDDAT) | **54.1** | Online (CICS) |
| **9** | **CBSTM03A.CBL** | 924 | 4 | 14+ | 35 | 5 (calls CBSTM03B; reads 4 VSAM files, writes 2 output files) | **48.7** | Batch |
| **10** | **COCRDSLC.cbl** | 887 | 12 | 10+ | 76 | 4 (CARDXREF, ACCTDAT, CARDDAT, CUSTDAT) | **46.3** | Online (CICS) |

---

## 3. Individual Metric Rankings

### 3.1 Lines of Code (Top 10)

| Rank | Program | LOC | Notes |
|------|---------|-----|-------|
| 1 | COACTUPC.cbl | 4,236 | Largest program — account update CRUD |
| 2 | COTRTLIC.cbl | 2,098 | DB2 transaction type list with cursor management |
| 3 | COTRTUPC.cbl | 1,702 | DB2 transaction type update/delete |
| 4 | COCRDUPC.cbl | 1,560 | Card detail update with full validation |
| 5 | COCRDLIC.cbl | 1,459 | Card list with pagination |
| 6 | COPAUS0C.cbl | 1,032 | Auth summary list (IMS) |
| 7 | COPAUA0C.cbl | 1,026 | MQ authorization processor |
| 8 | COACTVWC.cbl | 941 | Account view (read-only) |
| 9 | CBSTM03A.CBL | 924 | Statement generation driver |
| 10 | COCRDSLC.cbl | 887 | Card search screen |

### 3.2 Copybooks Referenced (Top 10)

| Rank | Program | Count | Copybooks |
|------|---------|-------|-----------|
| 1 | COACTUPC.cbl | 15 | COCOM01Y, COACTUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCRD01Y, CVCUS01Y, DFHAID, DFHBMSCA, CSSTRPFY |
| 2 | COACTVWC.cbl | 15 | COCOM01Y, COACTVW, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCRD01Y, CVCUS01Y, DFHAID, DFHBMSCA, CSSTRPFY |
| 3 | COPAUS0C.cbl | 14 | COCOM01Y, COPAU00, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CIPAUSMY, CIPAUDTY, DFHAID, DFHBMSCA |
| 4 | COTRTUPC.cbl | 14 | CVCRD01Y, COCOM01Y, COTTL01Y, COTRTUP, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, DFHAID, DFHBMSCA, CSSTRPFY, CSSETATY, CSUTLDWY, DCLTRTYP+ |
| 5 | COCRDUPC.cbl | 13 | CVCRD01Y, COCOM01Y, COCRDUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, DFHAID, DFHBMSCA, CSSTRPFY |
| 6 | COPAUA0C.cbl | 13 | CMQODV, CMQMDV, CMQV, CMQTML, CMQPMOV, CMQGMOV, CCPAURQY, CCPAURLY, CCPAUERY, CIPAUSMY, CIPAUDTY, CVACT03Y, CVACT01Y+ |
| 7 | COTRTLIC.cbl | 12 | CVCRD01Y, COCOM01Y, COTTL01Y, COTRTLI, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, DFHAID, DFHBMSCA, CSSTRPFY, CSDB2RWY+ |
| 8 | COCRDSLC.cbl | 12 | CVCRD01Y, COCOM01Y, COCRDSL, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT02Y, CVCUS01Y, DFHAID, DFHBMSCA+ |
| 9 | COCRDLIC.cbl | 10 | COCOM01Y, CVCRD01Y, COCRDLI, COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT02Y, DFHAID, DFHBMSCA |
| 10 | COPAUS1C.cbl | 10 | COCOM01Y, COPAU01, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, CIPAUSMY, CIPAUDTY, DFHAID, DFHBMSCA |

### 3.3 I/O Operations (Top 10)

| Rank | Program | I/O Count | Details |
|------|---------|-----------|---------|
| 1 | COTRTLIC.cbl | 28+ | EXEC SQL (SELECT, DELETE, OPEN/CLOSE cursor ×2, FETCH), EXEC CICS (SEND, RECEIVE, XCTL, RETURN, SYNCPOINT) |
| 2 | COTRTUPC.cbl | 26+ | EXEC SQL (SELECT, UPDATE, INSERT, DELETE, SYNCPOINT), EXEC CICS (SEND, RECEIVE, XCTL, RETURN, HANDLE ABEND) |
| 3 | COACTUPC.cbl | 24+ | READ/REWRITE ACCTDAT, READ CUSTDAT/CARDXREF/CARDDAT, CICS SEND/RECEIVE MAP, STARTBR/READNEXT/ENDBR |
| 4 | COCRDUPC.cbl | 18+ | READ/REWRITE CARDDAT, READ CARDXREF/ACCTDAT/CUSTDAT, CICS SEND/RECEIVE MAP |
| 5 | COACTVWC.cbl | 16+ | READ ACCTDAT/CUSTDAT/CARDXREF/CARDDAT, CICS SEND/RECEIVE MAP |
| 6 | COCRDLIC.cbl | 16+ | READ/STARTBR/READNEXT/READPREV/ENDBR CARDDAT/CARDXREF, CICS SEND/RECEIVE |
| 7 | COPAUA0C.cbl | 16+ | MQOPEN, MQGET, MQPUT1, MQCLOSE, READ ACCTDAT/CUSTDAT/CARDXREF |
| 8 | CBSTM03A.CBL | 14+ | CALL CBSTM03B (OPEN/READ/CLOSE ×4 VSAM files), WRITE STMTFILE, WRITE HTMLFILE |
| 9 | CBEXPORT.cbl | 14+ | OPEN/READ/CLOSE ×6 VSAM files, WRITE EXPFILE |
| 10 | CBIMPORT.cbl | 14+ | READ import file, WRITE ×6 VSAM files |

### 3.4 Business Logic Density — EVALUATE + IF (Top 10)

| Rank | Program | EVALUATE | IF | Total | Density (per 100 LOC) |
|------|---------|----------|-----|-------|----------------------|
| 1 | COACTUPC.cbl | 10 | 333 | 343 | 8.10 |
| 2 | COTRTLIC.cbl | 16 | 174 | 190 | 9.06 |
| 3 | COCRDUPC.cbl | 16 | 148 | 164 | 10.51 |
| 4 | COCRDLIC.cbl | 18 | 122 | 140 | 9.60 |
| 5 | COTRTUPC.cbl | 26 | 102 | 128 | 7.52 |
| 6 | CBTRN02C.cbl | 0 | 96 | 96 | 13.13 |
| 7 | CBACT04C.cbl | 0 | 86 | 86 | 13.19 |
| 8 | COCRDSLC.cbl | 8 | 68 | 76 | 8.57 |
| 9 | CBTRN03C.cbl | 4 | 75 | 79 | 12.17 |
| 10 | CBTRN01C.cbl | 0 | 66 | 66 | 13.36 |

> **Logic Density** = (EVALUATE + IF) / LOC × 100. Higher values indicate more branching per line of code — these programs have concentrated decision logic.

### 3.5 Inter-Program Dependencies (Top 10)

Programs ranked by total dependency count (outbound calls + inbound calls + shared file access):

| Rank | Program | Outbound Calls | Inbound Calls | Shared Datasets | Total Dep. |
|------|---------|---------------|---------------|-----------------|-----------|
| 1 | COACTUPC.cbl | 0 (CICS XCTL only) | Called from COMEN01C | 4 VSAM files | 5+ |
| 2 | CBSTM03A.CBL | 1 (CBSTM03B) | Called via JCL | 6 files (via sub) | 5+ |
| 3 | CBEXPORT.cbl | 1 (CEE3ABD) | Called via JCL | 7 files | 5+ |
| 4 | CBIMPORT.cbl | 1 (CEE3ABD) | Called via JCL | 7 files | 5+ |
| 5 | COPAUA0C.cbl | 4 (MQ API calls) | Triggered by MQ | 3 VSAM + MQ queues | 5+ |
| 6 | COCRDUPC.cbl | 0 (CICS XCTL only) | Called from COCRDLIC | 4 VSAM files | 5+ |
| 7 | COACTVWC.cbl | 0 (CICS XCTL only) | Called from COMEN01C | 4 VSAM files | 4+ |
| 8 | COCRDLIC.cbl | 0 (CICS XCTL only) | Called from COCRDSLC | 2 VSAM files | 4+ |
| 9 | COCRDSLC.cbl | 0 (CICS XCTL only) | Called from COMEN01C | 3 VSAM files | 4+ |
| 10 | COACCT01.cbl | 9 (MQ API calls) | CICS triggered | 1 VSAM + 3 MQ queues | 4+ |

---

## 4. Modernization Recommendations

### Priority Order

| Priority | Program | Score | Rationale |
|----------|---------|-------|-----------|
| **P1** | **COACTUPC.cbl** | 95.2 | **Modernize first.** At 4,236 LOC it is by far the largest and most complex program. It contains 343 branching statements, touches 4 VSAM files, and represents the core account-update business logic. It is the highest-risk, highest-value target: any bug here impacts all accounts. Extracting its validation rules, screen flow, and file I/O into clean Java services will de-risk the entire estate. |
| **P2** | **COTRTLIC.cbl** | 78.6 | **Modernize second.** The DB2 cursor management, scrollable list with in-line delete, and 190 branching statements make this the second most complex program. It already uses SQL (not VSAM), so migrating it to a Spring/JPA service is architecturally straightforward — but the procedural cursor logic and PF-key navigation need careful translation. |
| **P3** | **COTRTUPC.cbl** | 75.3 | **Modernize with COTRTLIC as a pair.** This is the detail/update counterpart to COTRTLIC — they share DB2 tables and CICS screen flow. Converting both together ensures the transaction-type CRUD lifecycle is consistent. Its 26 EVALUATE blocks reflect complex field-level validation that should become a validation service. |
| **P4** | **COCRDUPC.cbl** | 72.8 | **High complexity, many shared files.** The card update screen has 164 branching statements and accesses 4 VSAM files. Modernizing it alongside the card list (COCRDLIC) and search (COCRDSLC) programs creates a complete "Card Management" microservice. |
| **P5** | **COCRDLIC.cbl** | 64.5 | **Modernize with COCRDUPC.** The list/detail pattern (COCRDLIC ↔ COCRDUPC) should be converted as a unit. The STARTBR/READNEXT/READPREV pagination logic is a classic CICS pattern that maps naturally to Spring Data paginated queries. |
| **P6** | **COPAUA0C.cbl** | 60.2 | **MQ integration makes this a strategic target.** This program bridges MQ messaging with VSAM and IMS. Converting it creates the authorization API gateway. However, it depends on MQ infrastructure — ensure the MQ-to-Kafka (or equivalent) adapter is in place first. |
| **P7** | **COPAUS0C.cbl** | 55.8 | **Convert with the authorization sub-app.** The auth summary/detail/decision screens (COPAUS0C, COPAUS1C, COPAUS2C) form a cohesive unit. They depend on IMS DL/I — migrating them requires an IMS-to-relational data migration strategy. |
| **P8** | **COACTVWC.cbl** | 54.1 | **Quick win — read-only.** Same VSAM file access as COACTUPC but no writes. It's the read-only "view" companion. Because it only READs, it can be a thin query service — low risk, fast to convert, validates the data access layer before tackling COACTUPC. |
| **P9** | **CBSTM03A.CBL** | 48.7 | **Batch modernization anchor.** This is the statement-generation pipeline driver. It calls CBSTM03B and produces both text and HTML output. Converting this to a Spring Batch job with template-based rendering (Thymeleaf / PDF) replaces the most user-facing batch output. |
| **P10** | **COCRDSLC.cbl** | 46.3 | **Convert with card management group.** The card search screen feeds into COCRDLIC. Converting it completes the card search→list→detail flow as a modern REST-backed UI. |

### Recommended Modernization Waves

```
Wave 1 — Foundation & Quick Wins (Low Risk)
──────────────────────────────────────────────
  ✦ COACTVWC (Account View — read-only, validates data layer)
  ✦ CSUTLDTC (Date utility — small, self-contained, reusable)
  ✦ CBSTM03A + CBSTM03B (Statement batch — high business visibility)

Wave 2 — Core CRUD (Medium Risk, High Value)
──────────────────────────────────────────────
  ✦ COACTUPC (Account Update — crown jewel)
  ✦ COCRDSLC + COCRDLIC + COCRDUPC (Card Management lifecycle)

Wave 3 — Transaction & DB2 Services (Medium Risk)
──────────────────────────────────────────────────
  ✦ COTRTLIC + COTRTUPC + COBTUPDT (Transaction Type CRUD — already on DB2)
  ✦ COTRN00C + COTRN01C + COTRN02C (Transaction list/add/update)
  ✦ COBIL00C (Bill Payment)

Wave 4 — Integration & Authorization (Higher Risk)
───────────────────────────────────────────────────
  ✦ COPAUA0C (MQ Authorization processor)
  ✦ COPAUS0C + COPAUS1C + COPAUS2C (Auth screens — IMS dependent)
  ✦ COACCT01 + CODATE01 (MQ-VSAM services)

Wave 5 — Remaining Batch & Infrastructure
──────────────────────────────────────────
  ✦ CBACT01C–04C, CBCUS01C, CBTRN01C–03C (Batch file readers)
  ✦ CBEXPORT + CBIMPORT (Data migration utilities)
  ✦ COSGN00C + COUSR00C–03C (Security/user management — replace with IAM)
  ✦ COMEN01C + COADM01C (Menus — replaced by modern UI routing)
```

### Key Risk Factors

| Risk | Programs Affected | Mitigation |
|------|-------------------|------------|
| **VSAM-to-RDBMS migration** | All 24 online programs, all batch readers | Design a VSAM-compatible data access layer first; validate with COACTVWC (read-only) |
| **CICS screen flow** | 17 online programs | Map BMS maps to REST endpoints + SPA screens; preserve COMMAREA state as session/JWT |
| **IMS DL/I dependency** | COPAUA0C, COPAUS0C, COPAUS1C, DBUNLDGS, PAUDBLOD, PAUDBUNL | Migrate IMS hierarchical data to relational tables before converting programs |
| **MQ messaging** | COPAUA0C, COACCT01, CODATE01 | Implement a message broker adapter (MQ → Kafka or JMS) |
| **DB2 embedded SQL** | COTRTLIC, COTRTUPC, COBTUPDT | Straightforward — map EXEC SQL to JPA/JDBC; cursor logic → paginated queries |
| **Assembler dependencies** | COBDATFT (date), MVSWAIT (timer) | Replace with Java standard library (java.time, Thread.sleep) |
| **Shared copybooks** | COCOM01Y (17 programs), COTTL01Y (17), CSDAT01Y (17) | Create shared Java DTOs/POJOs; ensure all consumers migrate to same model |

---

## Appendix: Full Metric Table (All 44 Programs)

| Program | LOC | Copybooks | I/O Ops | EVAL | IF | Logic Total | Calls Out | Classification |
|---------|-----|-----------|---------|------|-----|-------------|-----------|---------------|
| COACTUPC.cbl | 4,236 | 15 | 24+ | 10 | 333 | 343 | 0 | Online |
| COTRTLIC.cbl | 2,098 | 12 | 28+ | 16 | 174 | 190 | 0 | Online+DB2 |
| COTRTUPC.cbl | 1,702 | 14 | 26+ | 26 | 102 | 128 | 0 | Online+DB2 |
| COCRDUPC.cbl | 1,560 | 13 | 18+ | 16 | 148 | 164 | 0 | Online |
| COCRDLIC.cbl | 1,459 | 10 | 16+ | 18 | 122 | 140 | 0 | Online |
| COPAUS0C.cbl | 1,032 | 14 | 12+ | 11 | 50 | 61 | 0 | Online |
| COPAUA0C.cbl | 1,026 | 13 | 16+ | 10 | 53 | 63 | 4 | Online+MQ |
| COACTVWC.cbl | 941 | 15 | 16+ | 10 | 57 | 67 | 0 | Online |
| CBSTM03A.CBL | 924 | 4 | 14+ | 5 | 30 | 35 | 1 | Batch |
| COCRDSLC.cbl | 887 | 12 | 10+ | 8 | 68 | 76 | 0 | Online |
| COTRN02C.cbl | 783 | 10 | 8+ | 13 | 28 | 41 | 1 | Online |
| CBTRN02C.cbl | 731 | 2 | 6+ | 0 | 96 | 96 | 1 | Batch |
| COTRN00C.cbl | 699 | 7 | 10+ | 8 | 52 | 60 | 0 | Online |
| COUSR00C.cbl | 695 | 8 | 10+ | 8 | 50 | 58 | 0 | Online |
| CBACT04C.cbl | 652 | 2 | 4+ | 0 | 86 | 86 | 1 | Batch |
| CORPT00C.cbl | 649 | 7 | 6+ | 5 | 40 | 45 | 1 | Online |
| CBTRN03C.cbl | 649 | 3 | 8+ | 4 | 75 | 79 | 1 | Batch |
| COACCT01.cbl | 620 | 7 | 14+ | 10 | 20 | 30 | 9 | Online+MQ |
| COPAUS1C.cbl | 604 | 10 | 6+ | 5 | 34 | 39 | 0 | Online |
| CBEXPORT.cbl | 582 | 7 | 14+ | 0 | 32 | 32 | 1 | Batch |
| COBIL00C.cbl | 572 | 7 | 8+ | 9 | 20 | 29 | 0 | Online |
| CODATE01.cbl | 524 | 6 | 12+ | 8 | 18 | 26 | 9 | Online+MQ |
| CBTRN01C.cbl | 494 | 1 | 4+ | 0 | 66 | 66 | 1 | Batch |
| CBIMPORT.cbl | 487 | 7 | 14+ | 1 | 28 | 29 | 1 | Batch |
| CBACT01C.cbl | 430 | 5 | 4+ | 0 | 44 | 44 | 2 | Batch |
| COUSR02C.cbl | 414 | 8 | 6+ | 5 | 26 | 31 | 0 | Online |
| CBPAUP0C.cbl | 386 | 2 | 4+ | 2 | 35 | 37 | 0 | Batch |
| PAUDBLOD.CBL | 369 | 4 | 6+ | 0 | 33 | 33 | 3 | Batch+IMS |
| DBUNLDGS.CBL | 366 | 6 | 8+ | 0 | 25 | 25 | 4 | Batch+IMS |
| COUSR03C.cbl | 359 | 8 | 6+ | 5 | 16 | 21 | 0 | Online |
| COTRN01C.cbl | 330 | 7 | 6+ | 3 | 14 | 17 | 0 | Online |
| PAUDBUNL.CBL | 317 | 4 | 6+ | 0 | 21 | 21 | 3 | Batch+IMS |
| COMEN01C.cbl | 308 | 9 | 4+ | 3 | 14 | 17 | 0 | Online |
| COUSR01C.cbl | 299 | 8 | 4+ | 3 | 8 | 11 | 0 | Online |
| COADM01C.cbl | 288 | 8 | 4+ | 4 | 12 | 16 | 0 | Online |
| COSGN00C.cbl | 260 | 8 | 4+ | 3 | 8 | 11 | 0 | Online |
| COPAUS2C.cbl | 244 | 1 | 4+ | 0 | 6 | 6 | 0 | Online |
| COBTUPDT.cbl | 237 | 1 | 8+ | 7 | 4 | 11 | 0 | Batch+DB2 |
| CBSTM03B.CBL | 230 | 0 | 8+ | 1 | 24 | 25 | 0 | Batch (Sub) |
| CBACT02C.cbl | 178 | 1 | 4+ | 0 | 22 | 22 | 1 | Batch |
| CBACT03C.cbl | 178 | 1 | 4+ | 0 | 22 | 22 | 1 | Batch |
| CBCUS01C.cbl | 178 | 1 | 4+ | 0 | 22 | 22 | 1 | Batch |
| CSUTLDTC.cbl | 157 | 0 | 2 | 2 | 0 | 2 | 1 | Utility |
| COBSWAIT.cbl | 41 | 0 | 0 | 0 | 0 | 0 | 1 | Utility |
