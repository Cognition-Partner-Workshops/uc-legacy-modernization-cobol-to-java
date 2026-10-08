# CardDemo COBOL Estate — Hotspot Report

Ranks all 44 COBOL programs on the five requested dimensions and recommends a modernization order for the COBOL → Java migration. Metrics are produced by static analysis of the source (see *Method*). Companion documents: [APPLICATION_INVENTORY.md](APPLICATION_INVENTORY.md), [DATA_DICTIONARY.md](DATA_DICTIONARY.md), [DEPENDENCY_MAP.md](DEPENDENCY_MAP.md).

## 1. Method

| Dimension | Metric | Weight in composite |
|---|---|---|
| Lines of code | Code lines (physical − comment − blank) | 20% |
| Copybooks | Distinct `COPY` / `EXEC SQL INCLUDE` members (application, BMS map, DCLGEN and system) | 20% |
| I/O operations | 70% distinct *(target, operation)* pairs + 30% raw I/O statement count (file `READ/WRITE/REWRITE/DELETE/START`, CICS file commands, SQL statements, DL/I calls, MQ calls). Distinct pairs dampen programs such as CBSTM03A that repeat `WRITE` to the same report file 95 times | 20% |
| Business-logic density | 60% decision points (`IF` + `WHEN`) + 40% maximum `IF`/`EVALUATE` nesting depth. Density per 100 code lines is shown for reference | 20% |
| Inter-program dependencies | Fan-out (static CALL, XCTL, LINK, menu dispatch) + fan-in + number of JCL jobs executing the program | 20% |

Each dimension is normalised to the estate maximum (0–1); the composite score is the mean × 100.

## 2. Top 10 programs (composite)

| Rank | Program | Type | Score | Code lines | Copybooks | I/O ops (distinct) | IF+WHEN / nest / per-100 | Deps (out / in / jobs) | External coupling |
|---|---|---|---|---|---|---|---|---|---|
| 1 | **COACTUPC** | Online (CICS) | 64.8 | 3,368 | 18 | 7 (5) | 291 / 3 / 8.64 | 1 / 1 / 0 | CICS, VSAM/QSAM |
| 2 | **COTRTLIC** | Online (CICS) | 47.3 | 1,597 | 15 | 12 (4) | 149 / 4 / 9.33 | 2 / 1 / 0 | CICS, DB2 |
| 3 | **COPAUS0C** | Online (CICS) | 44.3 | 792 | 14 | 9 (9) | 71 / 4 / 8.96 | 3 / 2 / 0 | CICS, VSAM/QSAM, IMS |
| 4 | **COTRTUPC** | Online (CICS) | 43.9 | 1,241 | 16 | 4 (4) | 153 / 3 / 12.33 | 1 / 2 / 0 | CICS, DB2 |
| 5 | **COPAUA0C** | Online (CICS, MQ-triggered) | 43.1 | 771 | 14 | 15 (15) | 48 / 3 / 6.23 | 0 / 0 / 0 | CICS, VSAM/QSAM, IMS, MQ |
| 6 | **COCRDLIC** | Online (CICS) | 38.5 | 1,093 | 11 | 8 (4) | 102 / 4 / 9.33 | 3 / 1 / 0 | CICS, VSAM/QSAM |
| 7 | **COMEN01C** | Online (CICS) | 38.3 | 213 | 9 | 0 (0) | 26 / 3 / 12.21 | 12 / 12 / 0 | CICS |
| 8 | **COCRDUPC** | Online (CICS) | 37.5 | 1,194 | 13 | 3 (2) | 131 / 3 / 10.97 | 1 / 2 / 0 | CICS, VSAM/QSAM |
| 9 | **COTRN02C** | Online (CICS) | 35.2 | 614 | 10 | 6 (6) | 77 / 4 / 12.54 | 3 / 1 / 0 | CICS, VSAM/QSAM |
| 10 | **COBIL00C** | Online (CICS) | 32.8 | 420 | 10 | 7 (7) | 42 / 4 / 10.0 | 2 / 1 / 0 | CICS, VSAM/QSAM |

## 3. Top 10 by individual dimension

**Lines of code:** 1. COACTUPC (3,368) · 2. COTRTLIC (1,597) · 3. COTRTUPC (1,241) · 4. COCRDUPC (1,194) · 5. COCRDLIC (1,093) · 6. COPAUS0C (792) · 7. CBSTM03A (784) · 8. COPAUA0C (771) · 9. COACTVWC (703) · 10. COCRDSLC (642)

**Copybooks referenced:** 1. COACTUPC (18) · 2. COTRTUPC (16) · 3. COTRTLIC (15) · 4. COACTVWC (15) · 5. COPAUS0C (14) · 6. COPAUA0C (14) · 7. COCRDUPC (13) · 8. COCRDSLC (13) · 9. COCRDLIC (11) · 10. COTRN02C (10)

**I/O operations (distinct pairs):** 1. COPAUA0C (15) · 2. COPAUS0C (9) · 3. CBTRN02C (9) · 4. COBIL00C (7) · 5. COPAUS1C (7) · 6. CBIMPORT (7) · 7. COTRN02C (6) · 8. CBACT04C (6) · 9. CBTRN03C (6) · 10. CBEXPORT (6)

**I/O statements (raw):** 1. CBSTM03A (95) · 2. COPAUA0C (15) · 3. COTRTLIC (12) · 4. COACCT01 (10) · 5. CBEXPORT (10) · 6. COPAUS0C (9) · 7. CBTRN02C (9) · 8. CODATE01 (9) · 9. COCRDLIC (8) · 10. COACTUPC (7)

**Decision points (IF + WHEN):** 1. COACTUPC (291) · 2. COTRTUPC (153) · 3. COTRTLIC (149) · 4. COCRDUPC (131) · 5. COCRDLIC (102) · 6. COTRN02C (77) · 7. COUSR00C (77) · 8. COTRN00C (76) · 9. COPAUS0C (71) · 10. COCRDSLC (49)

**Max IF/EVALUATE nesting:** 1. COTRTLIC (4) · 2. COPAUS0C (4) · 3. COCRDLIC (4) · 4. COTRN02C (4) · 5. COBIL00C (4) · 6. COUSR00C (4) · 7. COTRN00C (4) · 8. CBACT04C (4) · 9. CBTRN03C (4) · 10. COUSR02C (4)

**Inter-program dependencies:** 1. COMEN01C (24) · 2. COADM01C (14) · 3. COSGN00C (14) · 4. COPAUS0C (5) · 5. COUSR00C (5) · 6. COTRN00C (5) · 7. COTRN01C (5) · 8. COCRDLIC (4) · 9. COTRN02C (4) · 10. COUSR02C (4)

## 4. Recommended modernization order

The composite score measures *size and complexity*; the order below also weighs **business criticality**, **position in the batch critical path**, **external coupling** (CICS/IMS/MQ/DB2 add runtime dependencies Java must replace), and **testability** (batch programs with file-in/file-out contracts allow byte-level parity tests).

### Wave 1 — Core batch posting & interest (start here)

1. **CBTRN02C** — daily posting (619 code lines, 5 copybooks, 48 decision points, nesting 3, composite rank 18). The single write-gate for `TRANSACT`, `ACCTDATA` and `TCATBALF`; encodes the reject rules 100–103 and balance/cycle arithmetic. Pure QSAM/VSAM I/O with no CICS/DB2/IMS → clean Java Spring Batch (or plain Java 17) port, testable by replaying `DALYTRAN.PS` and diffing the KSDS images and `DALYREJS`. Highest business value per line of code.
2. **CBACT04C** — interest calculation (552 code lines, 5 copybooks, 43 decision points, nesting 4, composite rank 19). Next node on the critical path; money arithmetic (`S9(n)V99`, rate/1200) is where `BigDecimal` scale/rounding parity matters most. Nesting depth 4 with a stubbed fee routine (`1400-COMPUTE-FEES`) — capture current behaviour before extending.
3. **CSUTLDTC** + shared copybooks (`CVACT01Y`, `CVTRA05Y`, `CVTRA01Y`, `CVACT03Y`, `CVTRA02Y`) — generate Java records/DTOs and a date-validation utility first; they are prerequisites for every other wave (CVACT01Y alone is used by 14 programs).

### Wave 2 — Downstream batch outputs

4. **CBTRN03C** — transaction report (545 code lines, 5 copybooks, 44 decision points, nesting 4, composite rank 20); read-only, deterministic output, invoked on demand from CORPT00C — ideal golden-file test.
5. **CBSTM03A / CBSTM03B** — statements (784 code lines, 4 copybooks, 31 decision points, nesting 2, composite rank 24). High raw I/O count (95 statements) but only 2 distinct I/O targets; uses `ALTER`/`GO TO` and a 2-D in-memory table. Migrate as a pair, replacing the CBSTM03B function-code dispatcher with repository classes and the text/HTML writers with templates.
6. **CBEXPORT / CBIMPORT, CBACT01C–03C, CBCUS01C** — low complexity utilities; batch them together to finish the batch estate (CBACT01C requires replacing the assembler `COBDATFT` date routine).

### Wave 3 — Online account & card maintenance (largest risk)

7. **COACTUPC** — #1 hotspot (3,368 code lines, 18 copybooks, 291 decision points, nesting 3, composite rank 1). Largest program in the estate, densest validation logic (SSN, FICO, phone/area code, state/ZIP lookups via 1,300-line `CSLKPCDY`), and optimistic-locking REWRITE of two masters. Migrate only after Wave 1 entities/validators exist; extract the edit routines into a reusable Java validation layer first, then rebuild the screen as REST/UI.
8. **COCRDUPC, COCRDLIC, COACTVWC, COCRDSLC** — card/account screens (1,194 code lines, 13 copybooks, 131 decision points, nesting 3, composite rank 8 for COCRDUPC); share the same entities and AIX browse patterns, so they follow COACTUPC as one CICS→REST wave.
9. **COTRN02C, COBIL00C, COTRN00C/01C, COUSR0xC, COSGN00C, COMEN01C, COADM01C** — remaining online functions; menus/sign-on become routing and authentication in the target framework rather than literal ports.

### Wave 4 — Optional modules with heavy middleware coupling

10. **COTRTLIC / COTRTUPC / COBTUPDT** (DB2) — high decision density (149 decision points, nesting 4 in COTRTLIC) but DB2 maps naturally to JPA/JDBC; can be parallelised with Wave 3.
11. **COPAUA0C, COPAUS0C–2C, CBPAUP0C, PAUDBLOD/PAUDBUNL/DBUNLDGS** (IMS + MQ + DB2) — modernize last: requires replacing IMS hierarchical DB and MQ triggering with a relational store and messaging (e.g. JMS/Kafka). COPAUA0C's approve/decline rules are compact and should be captured as executable specifications early, even though the port is deferred.

### Why not start with the #1 hotspot?

COACTUPC scores highest on size and logic but is an online CICS program whose behaviour depends on BMS screens, pseudo-conversational COMMAREA state and concurrent-update checks. Starting with CBTRN02C/CBACT04C delivers the core financial logic, establishes the Java data model for Account/Card/Transaction, and provides automated parity tests — all of which de-risk the later COACTUPC migration.

## 5. Full metrics (all programs)

| Program | Type | Score | Code lines | Total lines | Copybooks | Raw I/O | Distinct I/O | IF | WHEN | EVALUATE | Max nest | Per-100 | Fan-out | Fan-in | Jobs | Paragraphs |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| COACTUPC | Online (CICS) | 64.8 | 3,368 | 4,236 | 18 | 7 | 5 | 164 | 127 | 10 | 3 | 8.64 | 1 | 1 | 0 | 85 |
| COTRTLIC | Online (CICS) | 47.3 | 1,597 | 2,098 | 15 | 12 | 4 | 86 | 63 | 16 | 4 | 9.33 | 2 | 1 | 0 | 57 |
| COPAUS0C | Online (CICS) | 44.3 | 792 | 1,032 | 14 | 9 | 9 | 25 | 46 | 11 | 4 | 8.96 | 3 | 2 | 0 | 20 |
| COTRTUPC | Online (CICS) | 43.9 | 1,241 | 1,702 | 16 | 4 | 4 | 49 | 104 | 13 | 3 | 12.33 | 1 | 2 | 0 | 63 |
| COPAUA0C | Online (CICS, MQ-triggered) | 43.1 | 771 | 1,026 | 14 | 15 | 15 | 27 | 21 | 5 | 3 | 6.23 | 0 | 0 | 0 | 43 |
| COCRDLIC | Online (CICS) | 38.5 | 1,093 | 1,459 | 11 | 8 | 4 | 59 | 43 | 9 | 4 | 9.33 | 3 | 1 | 0 | 39 |
| COMEN01C | Online (CICS) | 38.3 | 213 | 308 | 9 | 0 | 0 | 7 | 19 | 3 | 3 | 12.21 | 12 | 12 | 0 | 7 |
| COCRDUPC | Online (CICS) | 37.5 | 1,194 | 1,560 | 13 | 3 | 2 | 72 | 59 | 8 | 3 | 10.97 | 1 | 2 | 0 | 45 |
| COTRN02C | Online (CICS) | 35.2 | 614 | 783 | 10 | 6 | 6 | 14 | 63 | 13 | 4 | 12.54 | 3 | 1 | 0 | 18 |
| COBIL00C | Online (CICS) | 32.8 | 420 | 572 | 10 | 7 | 7 | 10 | 32 | 9 | 4 | 10.0 | 2 | 1 | 0 | 16 |
| COUSR00C | Online (CICS) | 31.4 | 531 | 695 | 8 | 4 | 4 | 25 | 52 | 8 | 4 | 14.5 | 4 | 1 | 0 | 16 |
| COACTVWC | Online (CICS) | 31.3 | 703 | 941 | 15 | 3 | 3 | 28 | 16 | 5 | 2 | 6.26 | 1 | 1 | 0 | 34 |
| COTRN00C | Online (CICS) | 31.3 | 529 | 699 | 8 | 4 | 4 | 26 | 50 | 8 | 4 | 14.37 | 3 | 2 | 0 | 16 |
| COPAUS1C | Online (CICS) | 30.8 | 461 | 604 | 10 | 7 | 7 | 17 | 20 | 5 | 3 | 8.03 | 2 | 1 | 0 | 15 |
| COADM01C | Online (CICS) | 29.6 | 189 | 288 | 9 | 0 | 0 | 6 | 14 | 2 | 3 | 10.58 | 7 | 7 | 0 | 8 |
| COSGN00C | Online (CICS) | 29.1 | 172 | 260 | 8 | 1 | 1 | 4 | 9 | 3 | 3 | 7.56 | 2 | 12 | 0 | 6 |
| COCRDSLC | Online (CICS) | 28.8 | 642 | 887 | 13 | 2 | 2 | 33 | 16 | 4 | 2 | 7.63 | 1 | 2 | 0 | 34 |
| CBTRN02C | Batch | 27.0 | 619 | 731 | 5 | 9 | 9 | 48 | 0 | 0 | 3 | 7.75 | 0 | 0 | 1 | 26 |
| CBACT04C | Batch | 25.5 | 552 | 652 | 5 | 7 | 6 | 43 | 0 | 0 | 4 | 7.79 | 0 | 0 | 1 | 22 |
| CBTRN03C | Batch | 25.4 | 545 | 649 | 5 | 6 | 6 | 38 | 6 | 2 | 4 | 8.07 | 0 | 0 | 1 | 26 |
| COUSR02C | Online (CICS) | 25.4 | 303 | 414 | 8 | 2 | 2 | 13 | 20 | 5 | 4 | 10.89 | 2 | 2 | 0 | 11 |
| COUSR03C | Online (CICS) | 24.7 | 251 | 359 | 8 | 2 | 2 | 8 | 16 | 5 | 4 | 9.56 | 2 | 2 | 0 | 11 |
| COTRN01C | Online (CICS) | 24.1 | 231 | 330 | 8 | 1 | 1 | 7 | 10 | 3 | 4 | 7.36 | 3 | 2 | 0 | 9 |
| CBSTM03A | Batch | 23.9 | 784 | 924 | 4 | 95 | 2 | 15 | 16 | 5 | 2 | 3.95 | 1 | 0 | 1 | 25 |
| COACCT01 | Online (CICS, MQ service) | 23.4 | 500 | 620 | 7 | 10 | 5 | 13 | 19 | 9 | 3 | 6.4 | 0 | 0 | 0 | 13 |
| CORPT00C | Online (CICS) | 22.8 | 498 | 649 | 8 | 0 | 0 | 20 | 19 | 5 | 3 | 7.83 | 3 | 1 | 0 | 10 |
| CODATE01 | Online (CICS, MQ service) | 20.6 | 409 | 524 | 6 | 9 | 4 | 12 | 16 | 8 | 3 | 6.85 | 0 | 0 | 0 | 13 |
| COUSR01C | Online (CICS) | 20.3 | 198 | 299 | 8 | 1 | 1 | 4 | 14 | 3 | 3 | 9.09 | 2 | 1 | 0 | 9 |
| CBTRN01C | Batch | 19.5 | 415 | 494 | 6 | 3 | 3 | 33 | 0 | 0 | 3 | 7.95 | 0 | 0 | 0 | 18 |
| CBIMPORT | Batch | 19.3 | 337 | 487 | 6 | 7 | 7 | 14 | 6 | 1 | 1 | 5.93 | 0 | 0 | 1 | 16 |
| CBEXPORT | Batch | 18.7 | 396 | 582 | 6 | 10 | 6 | 16 | 0 | 0 | 1 | 4.04 | 0 | 0 | 1 | 21 |
| CBACT01C | Batch | 15.0 | 358 | 430 | 2 | 5 | 4 | 22 | 0 | 0 | 2 | 6.15 | 1 | 0 | 1 | 16 |
| CBPAUP0C | Batch (IMS BMP) | 14.7 | 266 | 386 | 2 | 5 | 5 | 19 | 7 | 2 | 2 | 9.77 | 0 | 0 | 1 | 17 |
| PAUDBLOD | Batch (IMS BMP) | 13.7 | 251 | 369 | 4 | 5 | 2 | 17 | 0 | 0 | 2 | 6.77 | 0 | 0 | 1 | 17 |
| DBUNLDGS | Batch (IMS DLI) | 13.3 | 198 | 366 | 6 | 4 | 0 | 9 | 0 | 0 | 2 | 4.55 | 0 | 0 | 1 | 15 |
| PAUDBUNL | Batch (IMS DLI) | 13.1 | 207 | 317 | 4 | 4 | 2 | 11 | 0 | 0 | 2 | 5.31 | 0 | 0 | 1 | 11 |
| COPAUS2C | Online (CICS) subroutine | 11.5 | 201 | 244 | 3 | 2 | 2 | 3 | 0 | 0 | 2 | 1.49 | 0 | 1 | 0 | 2 |
| COBTUPDT | Batch (DB2) | 10.7 | 177 | 237 | 2 | 4 | 4 | 2 | 13 | 4 | 1 | 8.47 | 0 | 0 | 1 | 9 |
| CBSTM03B | Subroutine | 8.5 | 162 | 230 | 0 | 4 | 4 | 12 | 5 | 1 | 1 | 10.49 | 0 | 1 | 0 | 14 |
| CBACT02C | Batch | 8.2 | 129 | 178 | 1 | 1 | 1 | 11 | 0 | 0 | 2 | 8.53 | 0 | 0 | 1 | 5 |
| CBACT03C | Batch | 8.2 | 130 | 178 | 1 | 1 | 1 | 11 | 0 | 0 | 2 | 8.46 | 0 | 0 | 1 | 5 |
| CBCUS01C | Batch | 8.2 | 130 | 178 | 1 | 1 | 1 | 11 | 0 | 0 | 2 | 8.46 | 0 | 0 | 1 | 5 |
| CSUTLDTC | Subroutine | 4.8 | 114 | 157 | 0 | 0 | 0 | 0 | 10 | 1 | 1 | 8.77 | 0 | 2 | 0 | 2 |
| COBSWAIT | Batch | 1.7 | 13 | 41 | 0 | 0 | 0 | 0 | 0 | 0 | 0 | 0.0 | 1 | 0 | 1 | 0 |

## 6. Metric limitations

- Static analysis only: dynamic `XCTL PROGRAM(variable)` returns are resolved through menu tables where possible; COMMAREA-driven back-navigation is counted only when the target is a literal.
- Nesting depth counts `IF`/`EVALUATE` blocks closed by explicit scope terminators or periods; deeply nested paragraphs chained by `PERFORM` are not aggregated.
- I/O counts are statement occurrences, not runtime executions; PROCEDURE-division copybooks (`CSUTLDPY`, `CSSTRPFY`, `CSDB2RPY`) are not expanded into the including program's metrics.
