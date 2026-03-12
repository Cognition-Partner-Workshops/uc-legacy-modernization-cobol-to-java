# Hotspot Report — CardDemo COBOL Estate

> Identifies the most complex programs and recommends a modernization priority order.

---

## Table of Contents

1. [Methodology](#methodology)
2. [Top 10 by Lines of Code](#1-top-10-by-lines-of-code)
3. [Top 10 by Copybook References](#2-top-10-by-copybook-references)
4. [Top 10 by I/O Operations](#3-top-10-by-io-operations)
5. [Top 10 by Business Logic Density](#4-top-10-by-business-logic-density)
6. [Top 10 by Inter-Program Dependencies](#5-top-10-by-inter-program-dependencies)
7. [Composite Hotspot Score](#6-composite-hotspot-score)
8. [Modernization Recommendations](#7-modernization-recommendations)

---

## Methodology

Each program was scored across five dimensions:

| Dimension | How Measured | Weight |
|-----------|-------------|--------|
| **Lines of Code (LOC)** | `wc -l` on source file | 20% |
| **Copybook References** | Count of `COPY` statements | 20% |
| **I/O Operations** | Count of READ, WRITE, OPEN, CLOSE, EXEC CICS READ/WRITE/REWRITE/STARTBR/READNEXT/READPREV/ENDBR, EXEC SQL statements | 20% |
| **Business Logic Density** | Count of IF + EVALUATE statements (proxy for decision complexity and nesting) | 20% |
| **Inter-Program Dependencies** | Count of programs that call/transfer-to this program + programs this program calls/transfers-to + VSAM files accessed | 20% |

Scores are normalized to 0–100 within each dimension, then combined into a weighted composite.

---

## 1. Top 10 by Lines of Code

| Rank | Program | LOC | Classification | Purpose |
|------|---------|-----|---------------|---------|
| 1 | **COACTUPC.cbl** | 4,236 | Online (CICS) | Account update with full field validation |
| 2 | **COTRTLIC.cbl** | 2,098 | Online (CICS) | Transaction type list (DB2) |
| 3 | **COTRTUPC.cbl** | 1,702 | Online (CICS) | Transaction type update (DB2) |
| 4 | **COCRDUPC.cbl** | 1,560 | Online (CICS) | Credit card update |
| 5 | **COCRDLIC.cbl** | 1,459 | Online (CICS) | Credit card list with pagination |
| 6 | **COPAUS0C.cbl** | 1,032 | Online (CICS) | Authorization summary (DB2) |
| 7 | **COPAUA0C.cbl** | 1,026 | Online (CICS) | Payment authorization admin (DB2/MQ) |
| 8 | **COACTVWC.cbl** | 941 | Online (CICS) | Account view |
| 9 | **CBSTM03A.CBL** | 924 | Batch | Statement generation |
| 10 | **COCRDSLC.cbl** | 887 | Online (CICS) | Credit card detail view |

---

## 2. Top 10 by Copybook References

| Rank | Program | COPY Count | Key Copybooks |
|------|---------|-----------|---------------|
| 1 | **COACTUPC.cbl** | 56 | COCOM01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCRD01Y, COACTUP, COTTL01Y, CSDAT01Y, CSMSG01Y, CSMSG02Y, DFHAID, DFHBMSCA, and many BMS map copies |
| 2 | **COPAUA0C.cbl** | 16 | COCOM01Y, COPAU00, COPAU01, CIPAUDTY, CIPAUSMY, CCPAUERY, CCPAURQY, CCPAURLY, CSDB2RWY, CSDB2RPY, + standard |
| 3 | **COACTVWC.cbl** | 15 | COCOM01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCRD01Y, COACTVW, + standard |
| 4 | **COCRDSLC.cbl** | 15 | COCOM01Y, CVCRD01Y, CVACT01Y, CVACT02Y, CVACT03Y, COCRDSL, + standard |
| 5 | **COCRDUPC.cbl** | 15 | COCOM01Y, CVCRD01Y, CVACT01Y, CVACT02Y, CVACT03Y, COCRDUP, + standard |
| 6 | **COPAUS0C.cbl** | 14 | COCOM01Y, CIPAUDTY, CIPAUSMY, CSDB2RWY, CSDB2RPY, + standard |
| 7 | **COCRDLIC.cbl** | 13 | COCOM01Y, CVCRD01Y, CVACT01Y, CVACT03Y, COCRDLI, CSLKPCDY, + standard |
| 8 | **COTRTUPC.cbl** | 13 | COCOM01Y, COTRTUP, CSDB2RWY, CSDB2RPY, CSMSG02Y, + standard |
| 9 | **COTRTLIC.cbl** | 11 | COCOM01Y, COTRTLI, CSDB2RWY, CSDB2RPY, + standard |
| 10 | **COBIL00C.cbl** | 10 | COCOM01Y, COBIL00, CVACT01Y, CVACT03Y, CVTRA05Y, + standard |

---

## 3. Top 10 by I/O Operations

| Rank | Program | I/O Count | Types of I/O |
|------|---------|----------|--------------|
| 1 | **CBSTM03A.CBL** | 117 | WRITE (statement lines × 2 formats), CALL CBSTM03B for READ; OPEN/CLOSE files |
| 2 | **COTRTLIC.cbl** | 35 | EXEC SQL SELECT/FETCH (multiple cursors), EXEC CICS SEND/RECEIVE |
| 3 | **CBTRN02C.cbl** | 23 | READ/REWRITE ACCTDAT, TCATBALF; READ XREF; WRITE TRANSACT |
| 4 | **COPAUA0C.cbl** | 23 | EXEC SQL SELECT/INSERT/UPDATE, EXEC CICS SEND/RECEIVE, MQ operations |
| 5 | **COACCT01.cbl** | 23 | MQ MQOPEN/MQGET/MQPUT/MQCLOSE, EXEC CICS READ (VSAM files) |
| 6 | **CODATE01.cbl** | 23 | MQ MQOPEN/MQGET/MQPUT/MQCLOSE, EXEC CICS ASKTIME/FORMATTIME |
| 7 | **CBEXPORT.cbl** | 22 | READ 5 VSAM files, WRITE export output |
| 8 | **CBIMPORT.cbl** | 22 | READ export input, WRITE to 6 output files (incl. error file) |
| 9 | **CBTRN03C.cbl** | 21 | READ TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM; WRITE TRANREPT |
| 10 | **CBACT04C.cbl** | 19 | READ TCATBALF, XREF, DISCGRP, ACCTDAT; WRITE TRANSACT |

---

## 4. Top 10 by Business Logic Density

Business logic density = IF statements + EVALUATE statements (higher = more decision branching).

| Rank | Program | IF Count | EVALUATE Count | Total | Key Logic Areas |
|------|---------|---------|---------------|-------|----------------|
| 1 | **COACTUPC.cbl** | 168 | 20 | **188** | Field-level validation for all account fields, date checks, limit checks, status transitions |
| 2 | **COCRDUPC.cbl** | 148 | 16 | **164** | Card field validation, status transitions, expiry checks |
| 3 | **COCRDLIC.cbl** | 122 | 18 | **140** | Pagination logic, search criteria, card status filtering |
| 4 | **COTRTUPC.cbl** | 102 | 26 | **128** | DB2 CRUD validation, type code uniqueness, cascade checks |
| 5 | **COTRTLIC.cbl** | 89 | 32 | **121** | DB2 cursor management, pagination, multi-table joins |
| 6 | **CBTRN02C.cbl** | 93 | 0 | **93** | Transaction posting validation, balance checks, cross-ref |
| 7 | **CBTRN03C.cbl** | 75 | 4 | **79** | Report formatting logic, type/category lookups, totaling |
| 8 | **CBACT04C.cbl** | 86 | 0 | **86** | Interest calculation, rate lookups, category matching |
| 9 | **COCRDSLC.cbl** | 68 | 8 | **76** | Card detail display, cross-ref resolution, error handling |
| 10 | **COACTVWC.cbl** | 57 | 10 | **67** | Account view with card lookup, cross-ref navigation |

---

## 5. Top 10 by Inter-Program Dependencies

Dependencies include: programs called/transferred to, programs that call/transfer to this one, and VSAM/DB2 resources accessed.

| Rank | Program | Dependency Score | Breakdown |
|------|---------|-----------------|-----------|
| 1 | **COMEN01C.cbl** | **12** | Transfers to 7+ programs (COACTVWC, COCRDLIC, COTRN00C, CORPT00C, COBIL00C, COADM01C, etc.); called from COSGN00C; all programs return to it |
| 2 | **COACTUPC.cbl** | **10** | Accesses 3 VSAM files (ACCTDAT, CCXREF, CXACAIX); called from COACTVWC; many copybook dependencies |
| 3 | **COTRN02C.cbl** | **9** | Accesses 4 VSAM files (TRANSACT, CCXREF, CXACAIX, ACCTDAT); calls CSUTLDTC; called from COTRN00C |
| 4 | **COBIL00C.cbl** | **9** | Accesses 3 VSAM files (TRANSACT, ACCTDAT, CXACAIX); called from COMEN01C |
| 5 | **CBSTM03A.CBL** | **8** | Calls CBSTM03B; CBSTM03B accesses 4 files (TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE); writes 2 outputs |
| 6 | **COCRDLIC.cbl** | **8** | Accesses 3 VSAM files; calls CSUTLDTC, COBDATFT; transfers to COCRDSLC; called from COMEN01C |
| 7 | **CBTRN02C.cbl** | **8** | Accesses 4 VSAM files (DALYTRAN, ACCTDAT, XREF, TCATBALF); writes TRANSACT |
| 8 | **CBTRN03C.cbl** | **7** | Reads 5 files (TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM); writes TRANREPT |
| 9 | **COPAUA0C.cbl** | **7** | Accesses DB2 tables, MQ queues; transfers to COPAUS0C, COPAUS1C |
| 10 | **COADM01C.cbl** | **7** | Transfers to 4 user management programs; called from COMEN01C |

---

## 6. Composite Hotspot Score

All dimensions normalized to 0–100, then weighted equally (20% each).

| Rank | Program | LOC Score | Copybook Score | I/O Score | Logic Score | Dependency Score | **Composite** |
|------|---------|----------|---------------|----------|------------|-----------------|-------------|
| 1 | **COACTUPC.cbl** | 100.0 | 100.0 | 6.0 | 100.0 | 83.3 | **77.9** |
| 2 | **COCRDLIC.cbl** | 33.5 | 23.2 | 8.5 | 74.5 | 66.7 | **41.3** |
| 3 | **COCRDUPC.cbl** | 35.9 | 26.8 | 2.6 | 87.2 | 50.0 | **40.5** |
| 4 | **COTRTLIC.cbl** | 48.6 | 19.6 | 29.9 | 64.4 | 41.7 | **40.8** |
| 5 | **COTRTUPC.cbl** | 39.2 | 23.2 | 16.2 | 68.1 | 41.7 | **37.7** |
| 6 | **COBIL00C.cbl** | 12.5 | 17.9 | 6.0 | 14.9 | 75.0 | **25.3** |
| 7 | **CBSTM03A.CBL** | 20.8 | 7.1 | 100.0 | 12.8 | 66.7 | **41.5** |
| 8 | **COPAUA0C.cbl** | 23.2 | 28.6 | 19.7 | 32.4 | 58.3 | **32.4** |
| 9 | **COACTVWC.cbl** | 21.2 | 26.8 | 2.6 | 35.6 | 50.0 | **27.2** |
| 10 | **COTRN02C.cbl** | 17.5 | 17.9 | 5.1 | 21.3 | 75.0 | **27.4** |

---

## 7. Modernization Recommendations

### Priority 1: COACTUPC.cbl (Account Update) — **Modernize First**

| Factor | Detail |
|--------|--------|
| **Composite Score** | 77.9 (highest by significant margin) |
| **Why** | At 4,236 LOC with 188 decision points, this is the most complex program in the estate. It contains the core business logic for account management — credit limits, balance updates, status transitions, date validation, and multi-file cross-referencing. Its 56 COPY statements make it tightly coupled to nearly every data structure. |
| **Risk** | Highest risk of regression during modernization due to extensive field-level validation logic. |
| **Approach** | Decompose into microservices: account validation service, account persistence service, account view model. Extract validation rules into a rules engine. |

### Priority 2: CBSTM03A.CBL + CBSTM03B.CBL (Statement Generation) — **High Value**

| Factor | Detail |
|--------|--------|
| **Composite Score** | 41.5 |
| **Why** | This batch program uses legacy COBOL patterns (ALTER/GO TO, mainframe control block addressing, COMP-3 variables) that are hardest for automated tools to convert. It calls a subroutine (CBSTM03B) and produces dual-format output (text + HTML). The ALTER statement is notoriously difficult to translate. |
| **Risk** | Medium — isolated batch program with well-defined inputs/outputs. |
| **Approach** | Replace with modern template engine (e.g., Thymeleaf/FreeMarker for Java). The subroutine pattern maps well to service decomposition. |

### Priority 3: COTRTLIC.cbl + COTRTUPC.cbl (Transaction Type DB2 Programs) — **Quick Win**

| Factor | Detail |
|--------|--------|
| **Composite Score** | 40.8 / 37.7 |
| **Why** | These programs already use DB2 SQL, making them the closest to a modern data access pattern. They are relatively self-contained within the transaction-type sub-application. Converting DB2-based COBOL to JPA/JDBC is more straightforward than converting VSAM-based programs. |
| **Risk** | Low — isolated sub-application with clear DB2 interface. |
| **Approach** | Direct conversion to Spring Data JPA with REST API endpoints. Use existing SQL as a starting point for repository queries. |

### Priority 4: COCRDLIC.cbl + COCRDUPC.cbl (Card List & Update) — **UI Modernization**

| Factor | Detail |
|--------|--------|
| **Composite Score** | 41.3 / 40.5 |
| **Why** | These programs represent the card management UI with complex pagination, search, and CRUD logic. They access multiple VSAM files. Modernizing them demonstrates a full online workflow conversion. |
| **Risk** | Medium — tight coupling between BMS maps and business logic. |
| **Approach** | Separate presentation (React/Angular) from business logic (Spring Boot). Convert VSAM access to JPA repositories. |

### Priority 5: Batch Pipeline (CBTRN01C → CBTRN02C → CBTRN03C) — **Core Batch**

| Factor | Detail |
|--------|--------|
| **Composite Score** | Varies (27–35 range) |
| **Why** | This is the core daily transaction processing pipeline. It touches the most datasets and has the highest operational impact. However, the programs are moderately sized (494–731 LOC) and have clear input/output contracts making them good candidates for Spring Batch conversion. |
| **Risk** | High operational risk — these run daily and affect all account balances. |
| **Approach** | Convert to Spring Batch with chunk-oriented processing. Implement thorough regression testing against historical data before cutover. |

### Priority 6: CBACT04C.cbl (Interest Calculation) — **Business-Critical**

| Factor | Detail |
|--------|--------|
| **Composite Score** | ~30 |
| **Why** | Contains financial calculation logic (interest rate × balance ÷ 1200) that is highly regulated. Relatively moderate in size (652 LOC) but high in business importance. |
| **Risk** | High — financial accuracy is paramount. Requires extensive validation. |
| **Approach** | Extract calculation logic into a dedicated financial service with BigDecimal precision. Implement parallel-run testing before cutover. |

### Suggested Modernization Sequence

```
Phase 1 (Quick Wins):
  ├── COTRTLIC + COTRTUPC + COBTUPDT  (DB2 sub-app, lowest risk)
  └── CBSTM03A + CBSTM03B             (isolated batch, high legacy complexity)

Phase 2 (Core Online):
  ├── COACTUPC                          (highest complexity, most business value)
  ├── COCRDLIC + COCRDSLC + COCRDUPC   (card management workflow)
  └── COACTVWC                          (account view, simpler than update)

Phase 3 (Core Batch):
  ├── CBTRN01C + CBTRN02C + CBTRN03C  (daily transaction pipeline)
  ├── CBACT04C                          (interest calculation)
  └── CBEXPORT + CBIMPORT              (data migration utilities)

Phase 4 (Supporting Programs):
  ├── COSGN00C + COUSR00C-03C          (authentication & user management)
  ├── COMEN01C + COADM01C              (navigation menus)
  ├── COBIL00C                          (bill payment)
  └── COTRN00C-02C + CORPT00C         (transaction screens & reports)

Phase 5 (Sub-Applications):
  ├── Authorization IMS-DB2-MQ programs (requires IMS migration strategy)
  └── VSAM-MQ programs                  (requires MQ migration strategy)
```

---

## Appendix: Raw Metrics (All Programs)

### Main Application (`app/cbl/`)

| Program | LOC | COPY | I/O | IF | EVAL | CALL | Logic Total |
|---------|-----|------|-----|----|----|------|------------|
| COACTUPC.cbl | 4236 | 56 | 7 | 168 | 20 | 0 | 188 |
| COCRDUPC.cbl | 1560 | 15 | 3 | 148 | 16 | 0 | 164 |
| COCRDLIC.cbl | 1459 | 13 | 10 | 122 | 18 | 3 | 140 |
| CBSTM03A.CBL | 924 | 4 | 117 | 15 | 9 | 14 | 24 |
| COACTVWC.cbl | 941 | 15 | 3 | 57 | 10 | 0 | 67 |
| COCRDSLC.cbl | 887 | 15 | 2 | 68 | 8 | 0 | 76 |
| COTRN02C.cbl | 783 | 10 | 6 | 14 | 26 | 2 | 40 |
| CBTRN02C.cbl | 731 | 5 | 23 | 93 | 0 | 1 | 93 |
| COTRN00C.cbl | 699 | 8 | 4 | 26 | 16 | 0 | 42 |
| COUSR00C.cbl | 695 | 8 | 4 | 25 | 16 | 0 | 41 |
| CBACT04C.cbl | 652 | 5 | 19 | 86 | 0 | 1 | 86 |
| CBTRN03C.cbl | 649 | 5 | 21 | 75 | 4 | 1 | 79 |
| CORPT00C.cbl | 649 | 8 | 1 | 20 | 10 | 2 | 30 |
| CBEXPORT.cbl | 582 | 6 | 22 | 16 | 0 | 1 | 16 |
| COBIL00C.cbl | 572 | 10 | 7 | 10 | 18 | 0 | 28 |
| CBTRN01C.cbl | 494 | 6 | 17 | 33 | 0 | 1 | 33 |
| CBIMPORT.cbl | 487 | 6 | 22 | 14 | 2 | 1 | 16 |
| CBACT01C.cbl | 430 | 2 | 15 | 22 | 0 | 3 | 22 |
| COUSR02C.cbl | 414 | 8 | 2 | 13 | 10 | 0 | 23 |
| COUSR03C.cbl | 359 | 8 | 1 | 8 | 10 | 0 | 18 |
| COTRN01C.cbl | 330 | 8 | 1 | 7 | 6 | 0 | 13 |
| COMEN01C.cbl | 308 | 9 | 0 | 7 | 6 | 0 | 13 |
| COUSR01C.cbl | 299 | 9 | 1 | 4 | 6 | 0 | 10 |
| COADM01C.cbl | 288 | 9 | 0 | 11 | 4 | 0 | 15 |
| COSGN00C.cbl | 260 | 9 | 1 | 4 | 6 | 0 | 10 |
| CBSTM03B.CBL | 230 | 0 | 17 | 12 | 1 | 0 | 13 |
| CBACT02C.cbl | 178 | 1 | 3 | 22 | 0 | 1 | 22 |
| CBACT03C.cbl | 178 | 1 | 3 | 22 | 0 | 1 | 22 |
| CBCUS01C.cbl | 178 | 1 | 3 | 11 | 0 | 1 | 11 |
| CSUTLDTC.cbl | 157 | 0 | 0 | 0 | 2 | 2 | 2 |
| COBSWAIT.cbl | 41 | 0 | 0 | 0 | 0 | 1 | 0 |

### Sub-Applications

| Program | Sub-App | LOC | COPY | I/O | IF | EVAL | CALL | Logic Total |
|---------|---------|-----|------|-----|----|----|------|------------|
| COTRTLIC.cbl | tran-type-db2 | 2098 | 11 | 35 | 89 | 32 | 1 | 121 |
| COTRTUPC.cbl | tran-type-db2 | 1702 | 13 | 19 | 102 | 26 | 0 | 128 |
| COPAUS0C.cbl | auth-ims-db2-mq | 1032 | 14 | 10 | 25 | 22 | 0 | 47 |
| COPAUA0C.cbl | auth-ims-db2-mq | 1026 | 16 | 23 | 51 | 10 | 8 | 61 |
| COACCT01.cbl | vsam-mq | 620 | 9 | 23 | 13 | 18 | 9 | 31 |
| COPAUS1C.cbl | auth-ims-db2-mq | 604 | 10 | 8 | 17 | 10 | 0 | 27 |
| CODATE01.cbl | vsam-mq | 524 | 8 | 23 | 11 | 16 | 9 | 27 |
| CBPAUP0C.cbl | auth-ims-db2-mq | 386 | 2 | 10 | 17 | 4 | 0 | 21 |
| PAUDBLOD.CBL | auth-ims-db2-mq | 369 | 4 | 11 | 26 | 0 | 9 | 26 |
| DBUNLDGS.CBL | auth-ims-db2-mq | 366 | 6 | 13 | 19 | 0 | 7 | 19 |
| PAUDBUNL.CBL | auth-ims-db2-mq | 317 | 4 | 13 | 17 | 0 | 5 | 17 |
| COPAUS2C.cbl | auth-ims-db2-mq | 244 | 2 | 7 | 6 | 0 | 0 | 6 |
| COBTUPDT.cbl | tran-type-db2 | 237 | 0 | 10 | 3 | 8 | 0 | 11 |
