# CardDemo Hotspot Report &mdash; Top 10 Modernization Priorities

> **Methodology:** Modules ranked by a weighted composite of **Complexity** (40%), **Risk** (30%), and **Business Impact** (30%).
> Metrics derived from static analysis of line count, cyclomatic indicators (IF/EVALUATE/GO TO), CICS operations, file I/O density, copybook fan-out, and business domain criticality.

---

## Scoring Methodology

| Dimension | Weight | Indicators |
|-----------|-------:|------------|
| **Complexity** | 40% | Lines of code, IF/EVALUATE count, GO TO count, PERFORM count, COPY count, CSSETATY usage (attribute-heavy UI logic) |
| **Risk** | 30% | Number of VSAM files accessed, CICS commands, CALL/XCTL dependencies, data mutation operations (WRITE/REWRITE/DELETE), error handling patterns |
| **Business Impact** | 30% | Business domain criticality, user-facing importance, batch processing centrality, data breadth |

Each dimension scored 1&ndash;10, then weighted to produce a composite score (max 10.0).

---

## Top 10 Hotspot Modules

### #1 &mdash; COACTUPC (Account Update)
| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | **4,236** | Largest program in the codebase by 2.7x |
| IF Statements | 164 | Highest conditional density |
| EVALUATE | 10 | Complex state machine |
| GO TO | 51 | High unstructured control flow |
| PERFORM | 64 | Deep procedure nesting |
| COPY Statements | 27+ | Includes CSSETATY (24 times), CSSTRPFY, CSUTLDPY |
| CICS Operations | 15+ | READ, SEND, RECEIVE, REWRITE, XCTL |
| VSAM Files Accessed | 4 | ACCTDATA, CARDDATA, CARDXREF, CUSTDATA |
| **Complexity** | **10.0** | |
| **Risk** | **9.5** | Multi-file updates, ABEND handler, heavy attribute manipulation |
| **Business Impact** | **9.0** | Core account modification &mdash; financial data at stake |
| **Composite Score** | **9.55** | |

**Modernization Notes:** This is the single most complex module. Heavy use of COPY REPLACING for field attributes (CSSETATY 24+ times) creates expansion that must be carefully preserved. Contains ABEND handling. Candidates for decomposition: separate validation, I/O, and screen-handling into distinct services.

---

### #2 &mdash; CBTRN02C (Transaction Posting &mdash; Batch)
| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | **731** | |
| IF Statements | 48 | Dense validation logic |
| PERFORM | 61 | Complex paragraph flow |
| VSAM Files | 5 | TRANSACT, DALYTRAN, CARDXREF, ACCTDATA, TCATBALF |
| Outputs | 2 | DALYREJS (rejects GDG), TCATBALF updates |
| **Complexity** | **8.0** | |
| **Risk** | **9.5** | Central batch posting &mdash; drives all downstream processing; data integrity critical |
| **Business Impact** | **10.0** | Financial transaction posting &mdash; highest business criticality |
| **Composite Score** | **9.05** | |

**Modernization Notes:** Keystone batch program. All downstream jobs (INTCALC, CREASTMT, TRANREPT) depend on its output. Must preserve exact posting logic, reject handling, and category balance updates. Prime candidate for Spring Batch job with transactional integrity.

---

### #3 &mdash; COCRDUPC (Credit Card Update)
| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | **1,560** | |
| IF Statements | 72 | |
| EVALUATE | 16 | |
| GO TO | 21 | |
| PERFORM | 26 | |
| CICS Operations | 12+ | READ, SEND, RECEIVE, REWRITE, ABEND handler |
| VSAM Files Accessed | 3 | CARDDATA, CARDXREF, CUSTDATA |
| **Complexity** | **9.0** | |
| **Risk** | **8.5** | Card data mutation, ABEND handling |
| **Business Impact** | **8.0** | Credit card management &mdash; sensitive PCI data |
| **Composite Score** | **8.55** | |

**Modernization Notes:** Second-largest online program. Similar architecture to COACTUPC with ABEND handler and attribute manipulation. PCI-sensitive card data modifications require careful security mapping.

---

### #4 &mdash; COCRDLIC (Credit Card List)
| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | **1,459** | |
| IF Statements | 59 | |
| EVALUATE | 18 | Highest EVALUATE count |
| GO TO | 16 | |
| PERFORM | 34 | |
| CICS Operations | 14+ | STARTBR, READNEXT, READPREV, ENDBR, XCTL |
| VSAM Files Accessed | 3 | CARDDATA, CARDXREF, ACCTDATA |
| **Complexity** | **8.5** | |
| **Risk** | **7.5** | Browse cursor management, forward/backward pagination |
| **Business Impact** | **7.5** | Primary card discovery screen |
| **Composite Score** | **7.90** | |

**Modernization Notes:** Complex browse logic with CICS STARTBR/READNEXT/READPREV/ENDBR for pagination. The browse cursor pattern maps to paginated REST API or database cursor. Multiple XCTL exits to COCRDSLC and COCRDUPC.

---

### #5 &mdash; CBSTM03A (Statement Generation &mdash; Batch)
| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | **924** | |
| IF Statements | 15 | |
| GO TO | 15 | |
| PERFORM | 29 | |
| CALL to CBSTM03B | 13 | Heavy subroutine invocation |
| VSAM Files | 4 | TRXFL, CARDXREF, ACCTDATA, CUSTDATA |
| Output Files | 2 | STATEMNT.PS (text), STATEMNT.HTML |
| **Complexity** | **8.0** | |
| **Risk** | **8.0** | Dual-format output, mainframe control block addressing, subroutine coupling |
| **Business Impact** | **8.5** | Customer-facing statements &mdash; regulatory/compliance artifact |
| **Composite Score** | **8.13** | |

**Modernization Notes:** Generates statements in both text and HTML &mdash; demonstrates mainframe control block addressing patterns. Tightly coupled with CBSTM03B (13 CALL sites). These two programs should be migrated together as a unit.

---

### #6 &mdash; CBACT04C (Interest Calculation &mdash; Batch)
| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | **652** | |
| IF Statements | 43 | Heavy conditional logic |
| PERFORM | 56 | Complex control flow |
| VSAM Files | 5 | TCATBALF, CARDXREF (+AIX), ACCTDATA, DISCGRP |
| Output | SYSTRAN GDG | Interest transactions |
| **Complexity** | **7.5** | |
| **Risk** | **9.0** | Financial calculation &mdash; rounding, rate lookup, account balance updates |
| **Business Impact** | **9.0** | Interest revenue &mdash; directly affects P&L |
| **Composite Score** | **8.40** | |

**Modernization Notes:** Business-critical financial calculation. Uses alternate index (AIX) on CARDXREF for account-based lookups. Interest rate logic sourced from DISCGRP disclosure table. Requires exact decimal precision preservation.

---

### #7 &mdash; COACTVWC (Account View)
| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | **941** | |
| IF Statements | 28 | |
| EVALUATE | 10 | |
| GO TO | 9 | |
| PERFORM | 21 | |
| CICS Operations | 12+ | READ (3 files), SEND, RECEIVE, XCTL, ABEND handler |
| VSAM Files Accessed | 5 | ACCTDATA, CARDDATA, CARDXREF, CUSTDATA, (view only) |
| **Complexity** | **7.0** | |
| **Risk** | **7.0** | Read-only but joins 4 entities; ABEND handling |
| **Business Impact** | **7.5** | Primary account inquiry &mdash; high usage frequency |
| **Composite Score** | **7.15** | |

**Modernization Notes:** Read-only but accesses 4 VSAM files in a single screen. The join pattern (XREF &rarr; Account &rarr; Card &rarr; Customer) should map to a SQL JOIN or service aggregation layer. Has ABEND handler and CSSTRPFY string processing.

---

### #8 &mdash; CBTRN03C (Transaction Report &mdash; Batch)
| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | **649** | |
| IF Statements | 38 | |
| EVALUATE | 4 | |
| PERFORM | 72 | Highest PERFORM count |
| VSAM Files | 4 | TRANSACT.DALY, CARDXREF, TRANTYPE, TRANCATG |
| Output | TRANREPT GDG | Daily transaction report |
| **Complexity** | **7.5** | |
| **Risk** | **6.5** | Report-only, no data mutation |
| **Business Impact** | **7.5** | Daily operational reporting &mdash; audit/compliance |
| **Composite Score** | **7.20** | |

**Modernization Notes:** Highest PERFORM count (72) indicates deeply structured procedural logic. Uses report layout copybook CVTRA07Y with formatted print lines. Maps to a reporting service or scheduled report generation task.

---

### #9 &mdash; COTRN02C (Transaction Add &mdash; Online)
| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | **783** | |
| IF Statements | 14 | |
| EVALUATE | 13 | |
| PERFORM | 61 | |
| CALL to CSUTLDTC | 2 | Date validation |
| CICS Operations | 10+ | READ, WRITE, STARTBR, READPREV, ENDBR, SEND, RECEIVE |
| VSAM Files | 3 | TRANSACT, ACCTDATA, CARDXREF |
| **Complexity** | **7.0** | |
| **Risk** | **7.5** | Transaction creation &mdash; WRITE to TRANSACT file, sequence generation |
| **Business Impact** | **8.0** | Only online path for new transaction entry |
| **Composite Score** | **7.45** | |

**Modernization Notes:** Generates new transaction IDs using STARTBR/READPREV to find last ID. This auto-increment pattern needs careful conversion to a database sequence. Validates dates via CSUTLDTC CALL. Writes to TRANSACT VSAM.

---

### #10 &mdash; COBIL00C (Bill Payment)
| Metric | Value | Notes |
|--------|------:|-------|
| Lines of Code | **572** | |
| IF Statements | 10 | |
| EVALUATE | 9 | |
| PERFORM | 38 | |
| CICS Operations | 12+ | READ, REWRITE, WRITE, STARTBR, READPREV, ENDBR |
| VSAM Files | 3 | ACCTDATA, TRANSACT, CARDXREF |
| **Complexity** | **6.5** | |
| **Risk** | **8.0** | Financial mutation &mdash; account REWRITE + transaction WRITE in single flow |
| **Business Impact** | **8.5** | Customer-facing payment processing |
| **Composite Score** | **7.55** | |

**Modernization Notes:** Performs both account balance update (REWRITE) and transaction creation (WRITE) in a single CICS pseudo-conversation. This dual-write pattern requires transactional atomicity in the target platform. Uses STARTBR/READPREV for transaction ID generation (same pattern as COTRN02C).

---

## Summary Ranking Table

| Rank | Program | Type | Lines | Composite Score | Primary Risk Factor |
|-----:|---------|------|------:|----------------:|---------------------|
| 1 | **COACTUPC** | Online | 4,236 | **9.55** | Size, complexity, multi-file mutation |
| 2 | **CBTRN02C** | Batch | 731 | **9.05** | Central batch posting, downstream dependencies |
| 3 | **COCRDUPC** | Online | 1,560 | **8.55** | PCI-sensitive card mutations, ABEND handling |
| 4 | **CBACT04C** | Batch | 652 | **8.40** | Financial calculations, decimal precision |
| 5 | **CBSTM03A** | Batch | 924 | **8.13** | Dual-format output, subroutine coupling |
| 6 | **COCRDLIC** | Online | 1,459 | **7.90** | Browse cursor pagination complexity |
| 7 | **COBIL00C** | Online | 572 | **7.55** | Dual-write atomicity (payment + transaction) |
| 8 | **COTRN02C** | Online | 783 | **7.45** | Transaction creation, ID generation |
| 9 | **CBTRN03C** | Batch | 649 | **7.20** | Deep PERFORM nesting, report formatting |
| 10 | **COACTVWC** | Online | 941 | **7.15** | 4-file join pattern, ABEND handling |

---

## Recommended Modernization Waves

### Wave 1 &mdash; Foundation (Low Risk, High Learning)
- **COSGN00C** (Sign On) &mdash; Simple, isolated, maps to auth service
- **COMEN01C** / **COADM01C** (Menus) &mdash; Navigation layer, maps to routing
- **COUSR00C-03C** (User CRUD) &mdash; Standard CRUD, single VSAM file

### Wave 2 &mdash; Core Read Paths
- **COACTVWC** (#10) &mdash; Read-only account view, exercises multi-file join
- **COCRDLIC** (#6) / **COCRDSLC** &mdash; Card list/view, exercises browse pattern
- **COTRN00C** / **COTRN01C** &mdash; Transaction list/view

### Wave 3 &mdash; Write Paths (High Risk)
- **COTRN02C** (#8) &mdash; Transaction add, ID generation
- **COBIL00C** (#7) &mdash; Bill payment, dual-write atomicity
- **COCRDUPC** (#3) &mdash; Card update, PCI-sensitive
- **COACTUPC** (#1) &mdash; Account update, largest and most complex

### Wave 4 &mdash; Batch Processing (Highest Business Impact)
- **CBTRN02C** (#2) &mdash; Transaction posting (most critical)
- **CBACT04C** (#4) &mdash; Interest calculation
- **CBSTM03A** + **CBSTM03B** (#5) &mdash; Statement generation (pair)
- **CBTRN03C** (#9) &mdash; Daily transaction report
- **CORPT00C** &mdash; Online transaction reports

### Wave 5 &mdash; Utilities & Data Migration
- **CBEXPORT** / **CBIMPORT** &mdash; Data migration tools
- **CBACT01C-03C**, **CBCUS01C**, **CBTRN01C** &mdash; File read utilities
- **CSUTLDTC**, **COBSWAIT** &mdash; Utility programs
- Optional modules (IMS/DB2/MQ, VSAM-MQ)
