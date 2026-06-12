# CardDemo Hotspot Report

> Top 10 modules prioritized by code complexity, migration risk, and business impact — guiding modernization sequencing.

---

## Scoring Methodology

Each module is scored across three dimensions (1–10 scale):

| Dimension        | Weight | Factors Considered                                                                    |
|:-----------------|-------:|:--------------------------------------------------------------------------------------|
| **Complexity**   |   40%  | LOC, cyclomatic indicators (IF/EVALUATE/GO TO), PERFORM count, copybook fanout, technology mix |
| **Risk**         |   35%  | Data writes (REWRITE/DELETE), multi-technology stack (CICS+DB2+IMS+MQ), PII/PCI handling, GO TO usage, COMP-3/binary fields |
| **Business Impact** | 25% | Transaction volume (core vs. utility), business criticality (payments, auth), downstream dependencies, user-facing scope |

**Composite Score** = (Complexity × 0.40) + (Risk × 0.35) + (Impact × 0.25)

---

## Top 10 Hotspot Modules

### #1 — COACTUPC (Account Update) — Score: 9.3

| Metric          | Value  | Detail                                                    |
|:----------------|-------:|:----------------------------------------------------------|
| Lines of Code   | 4,236  | Largest program in the entire codebase                    |
| IF Statements   |   165  | Extremely high branching density                          |
| PERFORM Stmts   |    64  | Many paragraphs                                           |
| GO TO Stmts     |    51  | Heavy use of unstructured flow                            |
| EVALUATE Stmts  |    10  | Multi-way branching                                       |
| Copybook Fanout |    58  | Highest — 15+ unique copybooks including CSLKPCDY (1,318 lines of lookup tables), CSUTLDWY, CSUTLDPY, 3× CSSETATY REPLACING |
| Technologies    |        | CICS (SEND/RECEIVE/READ/REWRITE), VSAM I-O               |
| Data Access     |        | Reads + Rewrites ACCTDATA; reads CARDXREF, CUSTDATA, CARDDATA |

**Why #1**: This is the single most complex program — highest LOC, deepest branching, most GO TO statements, and the widest copybook fanout. It contains extensive field-level validation using embedded lookup tables (1,300+ lines), date validation procedures copied into the PROCEDURE DIVISION, and BMS attribute manipulation via COPY REPLACING. It writes to the critical ACCTDATA file. Modernization will require decomposing validation logic into reusable services and eliminating GO TO spaghetti.

| Dimension        | Score |
|:-----------------|------:|
| Complexity       |  10   |
| Risk             |   9   |
| Business Impact  |   8   |
| **Composite**    | **9.3** |

---

### #2 — COTRTLIC (Transaction Type List — DB2) — Score: 8.5

| Metric          | Value  | Detail                                                    |
|:----------------|-------:|:----------------------------------------------------------|
| Lines of Code   | 2,098  | Second largest                                            |
| IF Statements   |    87  | High branching                                            |
| PERFORM Stmts   |    63  | Many paragraphs                                           |
| GO TO Stmts     |    28  | Significant unstructured flow                             |
| EVALUATE Stmts  |    16  | Multi-way branching                                       |
| Technologies    |        | CICS + DB2 (cursor, SELECT, DELETE, UPDATE) + VSAM        |
| Data Access     |        | DB2 TRTYP table via cursor-based paging; XCTL navigation  |

**Why #2**: Dual-technology program (CICS + DB2) with cursor-based pagination — a pattern that requires careful translation to modern REST APIs with pagination. Contains GO TO for flow control around DB2 error handling. The DB2 integration adds migration complexity beyond pure VSAM programs.

| Dimension        | Score |
|:-----------------|------:|
| Complexity       |   9   |
| Risk             |   9   |
| Business Impact  |   7   |
| **Composite**    | **8.5** |

---

### #3 — COTRTUPC (Transaction Type Update — DB2) — Score: 8.2

| Metric          | Value  | Detail                                                    |
|:----------------|-------:|:----------------------------------------------------------|
| Lines of Code   | 1,702  | Third largest                                             |
| IF Statements   |    98  | Very high branching                                       |
| EVALUATE Stmts  |    26  | Highest EVALUATE count in codebase                        |
| GO TO Stmts     |    23  | Significant                                               |
| Technologies    |        | CICS + DB2 (INSERT/UPDATE) + BMS                          |
| Data Access     |        | DB2 TRTYP table (read/insert/update)                      |

**Why #3**: Similar dual-technology complexity to COTRTLIC. The highest EVALUATE count signals complex state machine logic for screen handling. DB2 INSERT/UPDATE operations need transaction management translation.

| Dimension        | Score |
|:-----------------|------:|
| Complexity       |   9   |
| Risk             |   8   |
| Business Impact  |   7   |
| **Composite**    | **8.2** |

---

### #4 — COCRDUPC (Credit Card Update) — Score: 8.1

| Metric          | Value  | Detail                                                    |
|:----------------|-------:|:----------------------------------------------------------|
| Lines of Code   | 1,560  | Fourth largest                                            |
| IF Statements   |   147  | Second highest IF count in codebase                       |
| GO TO Stmts     |    21  | Significant unstructured flow                             |
| EVALUATE Stmts  |    16  | Multi-way branching                                       |
| Technologies    |        | CICS (SEND/RECEIVE/READ/REWRITE), VSAM                   |
| Data Access     |        | Reads + Rewrites CARDDATA; reads CUSTDATA                 |

**Why #4**: Very high branching density (147 IFs in 1,560 lines = 1 IF per 10.6 lines). Writes to the PCI-sensitive CARDDATA file containing card numbers and CVVs. GO TO usage complicates refactoring. Card update logic is a regulatory compliance hotspot.

| Dimension        | Score |
|:-----------------|------:|
| Complexity       |   8   |
| Risk             |   9   |
| Business Impact  |   7   |
| **Composite**    | **8.1** |

---

### #5 — COCRDLIC (Credit Card List) — Score: 7.8

| Metric          | Value  | Detail                                                    |
|:----------------|-------:|:----------------------------------------------------------|
| Lines of Code   | 1,459  | Fifth largest                                             |
| IF Statements   |   120  | Very high                                                 |
| EVALUATE Stmts  |    18  | Significant                                               |
| GO TO Stmts     |    16  | Moderate unstructured flow                                |
| Technologies    |        | CICS (STARTBR/READNEXT/READPREV/ENDBR), VSAM browse      |
| Data Access     |        | Browses CARDDATA; XCTL to COCRDSLC and COCRDUPC           |

**Why #5**: Implements complex VSAM browse logic with forward/backward paging — a pattern that maps poorly to simple SQL queries. The combination of browse state management, 120 IF branches, and downstream XCTL navigation makes this a significant migration challenge. Acts as the central hub for the card management subsystem.

| Dimension        | Score |
|:-----------------|------:|
| Complexity       |   8   |
| Risk             |   7   |
| Business Impact  |   8   |
| **Composite**    | **7.8** |

---

### #6 — COPAUA0C (Authorization Decision — IMS/MQ) — Score: 7.7

| Metric          | Value  | Detail                                                    |
|:----------------|-------:|:----------------------------------------------------------|
| Lines of Code   | 1,026  |                                                           |
| IF Statements   |    51  | Moderate                                                  |
| EVALUATE Stmts  |    10  |                                                           |
| Technologies    |        | CICS + IMS DL/I + MQ (MQOPEN/MQGET/MQPUT1/MQCLOSE) + VSAM|
| Data Access     |        | MQ queues (request/response); IMS PAUT DB (SCHD/GU/ISRT/REPL); VSAM reads (ACCT, CARD, XREF) |

**Why #6**: Triple-technology program (CICS + IMS + MQ) — the highest technology diversity in the codebase. Implements the real-time credit card authorization decision engine. MQ message handling (MQOPEN → MQGET → process → MQPUT1 → MQCLOSE) requires mapping to modern messaging (Kafka, SQS). IMS DL/I calls need complete database paradigm migration. The authorization decision logic is business-critical and regulatory-sensitive.

| Dimension        | Score |
|:-----------------|------:|
| Complexity       |   7   |
| Risk             |  10   |
| Business Impact  |   6   |
| **Composite**    | **7.7** |

---

### #7 — COPAUS0C (Pending Auth Summary — IMS) — Score: 7.4

| Metric          | Value  | Detail                                                    |
|:----------------|-------:|:----------------------------------------------------------|
| Lines of Code   | 1,032  |                                                           |
| IF Statements   |    25  | Moderate                                                  |
| EVALUATE Stmts  |    11  |                                                           |
| Technologies    |        | CICS + IMS DL/I (GN/GNP browse) + VSAM + BMS             |
| Data Access     |        | IMS PAUT & PASFL databases (segment browsing); VSAM reads for account/card/xref |

**Why #7**: IMS DL/I segment browsing (GN/GNP) with CICS BMS screen display. The hierarchical database access pattern (parent-child segment navigation) is fundamentally different from relational access and requires complete redesign. Combined with VSAM cross-reference lookups for enrichment.

| Dimension        | Score |
|:-----------------|------:|
| Complexity       |   7   |
| Risk             |   9   |
| Business Impact  |   6   |
| **Composite**    | **7.4** |

---

### #8 — CBTRN02C (Transaction Posting — Batch) — Score: 7.2

| Metric          | Value  | Detail                                                    |
|:----------------|-------:|:----------------------------------------------------------|
| Lines of Code   |   731  |                                                           |
| IF Statements   |    93  | Very high for its size (1 IF per 7.9 lines)              |
| PERFORM Stmts   |    61  | High                                                      |
| Technologies    |        | Batch COBOL, Sequential + VSAM I-O                        |
| Data Access     |        | Reads DALYTRAN, XREF; Writes TRANSACT, DALYREJS; Updates ACCTDATA, TCATBALF |

**Why #8**: Core batch posting engine — the most critical batch program. Highest IF density of any program (93 IFs in 731 lines). Performs multi-file I-O with validation, cross-reference lookups, balance updates, and reject handling in a single program. This is the backbone of daily transaction processing. Writes to 4 different files simultaneously.

| Dimension        | Score |
|:-----------------|------:|
| Complexity       |   7   |
| Risk             |   7   |
| Business Impact  |   8   |
| **Composite**    | **7.2** |

---

### #9 — CBSTM03A (Account Statement Generation) — Score: 6.8

| Metric          | Value  | Detail                                                    |
|:----------------|-------:|:----------------------------------------------------------|
| Lines of Code   |   924  |                                                           |
| PERFORM Stmts   |    29  |                                                           |
| GO TO Stmts     |    15  | Uses ALTER ... PROCEED TO (obsolete dynamic GO TO)        |
| CALL Stmts      |    13  | 13 calls to CBSTM03B subroutine                           |
| Technologies    |        | Batch COBOL, Sequential I-O, CALL, ALTER                  |
| Data Access     |        | Reads TRANSACT, XREF, CUSTDATA, ACCTDATA; Writes statement + HTML files |

**Why #9**: Uses the obsolete `ALTER ... PROCEED TO` statement — a dynamic GO TO that changes branch targets at runtime. This is one of the most difficult COBOL patterns to analyze and translate. Generates both plain-text and HTML output, requiring dual output stream management. The CALL-based delegation to CBSTM03B adds inter-program coupling. Four input files make the data dependency footprint large.

| Dimension        | Score |
|:-----------------|------:|
| Complexity       |   7   |
| Risk             |   8   |
| Business Impact  |   5   |
| **Composite**    | **6.8** |

---

### #10 — COACTVWC (Account View) — Score: 6.6

| Metric          | Value  | Detail                                                    |
|:----------------|-------:|:----------------------------------------------------------|
| Lines of Code   |   941  |                                                           |
| IF Statements   |    56  | Moderate-high                                             |
| EVALUATE Stmts  |    10  |                                                           |
| GO TO Stmts     |     9  |                                                           |
| Copybook Fanout |    16  | High — includes CSSTRPFY string utility                   |
| Technologies    |        | CICS (SEND/RECEIVE/READ/SEND TEXT/RETURN), VSAM           |
| Data Access     |        | Reads ACCTDATA, CARDDATA, CARDXREF, CUSTDATA (4 files)    |

**Why #10**: Read-only but accesses 4 VSAM files to compose a single account view — Customer + Account + Card + XREF. This cross-file join pattern is a key candidate for consolidation into a single relational query in the target system. Uses SEND TEXT for overflow display, GO TO for error handling, and string manipulation utilities. High copybook fanout (16 includes) means broad data structure coupling.

| Dimension        | Score |
|:-----------------|------:|
| Complexity       |   6   |
| Risk             |   6   |
| Business Impact  |   8   |
| **Composite**    | **6.6** |

---

## Full Complexity Metrics (All Programs)

Sorted by composite complexity score (LOC + branching density + GO TO penalty).

| Rank | Program    | LOC   | IF  | EVAL | GO TO | PERFORM | Copies | Module        |
|-----:|:-----------|------:|----:|-----:|------:|--------:|-------:|:--------------|
|    1 | COACTUPC   | 4,236 | 165 |   10 |    51 |      64 |     58 | Core          |
|    2 | COTRTLIC   | 2,098 |  87 |   16 |    28 |      63 |     12 | Tran Type/DB2 |
|    3 | COTRTUPC   | 1,702 |  98 |   26 |    23 |      40 |     15 | Tran Type/DB2 |
|    4 | COCRDUPC   | 1,560 | 147 |   16 |    21 |      26 |     16 | Core          |
|    5 | COCRDLIC   | 1,459 | 120 |   18 |    16 |      34 |     14 | Core          |
|    6 | COPAUS0C   | 1,032 |  25 |   11 |     0 |      46 |     15 | Auth/IMS/MQ   |
|    7 | COPAUA0C   | 1,026 |  51 |   10 |     0 |      38 |     17 | Auth/IMS/MQ   |
|    8 | COACTVWC   |   941 |  56 |   10 |     9 |      21 |     16 | Core          |
|    9 | CBSTM03A   |   924 |  15 |    5 |    15 |      29 |      5 | Core          |
|   10 | COCRDSLC   |   887 |  69 |    8 |     9 |      19 |     16 | Core          |
|   11 | COTRN02C   |   783 |  14 |   13 |     0 |      61 |     11 | Core          |
|   12 | CBTRN02C   |   731 |  93 |    0 |     0 |      61 |      6 | Core          |
|   13 | COTRN00C   |   699 |  26 |    8 |     0 |      43 |      9 | Core          |
|   14 | COUSR00C   |   695 |  25 |    8 |     0 |      41 |      9 | Core          |
|   15 | CBACT04C   |   652 |  86 |    0 |     0 |      56 |      6 | Core          |
|   16 | CBTRN03C   |   649 |  75 |    4 |     0 |      72 |      6 | Core          |
|   17 | CORPT00C   |   649 |  20 |    5 |     1 |      34 |      9 | Core          |
|   18 | COACCT01   |   620 |  13 |   10 |     0 |      33 |      9 | VSAM-MQ       |
|   19 | COPAUS1C   |   604 |  17 |    5 |     0 |      34 |     11 | Auth/IMS/MQ   |
|   20 | CBEXPORT   |   582 |  16 |    0 |     0 |      45 |      7 | Core          |

---

## Recommended Migration Sequence

Based on dependency analysis and hotspot scores, the following phased approach minimizes risk:

### Phase 0 — Shared Utilities (Low Risk, No Business Logic)
- CSUTLDTC (date validation), COBSWAIT/MVSWAIT (timer), COBDATFT (date format)
- Establish Java date/time utilities as foundation

### Phase 1 — Reference Data & Security (Low Complexity, Foundation)
- COUSR00C–03C (User CRUD) — simple VSAM CRUD → Spring Data JPA
- COSGN00C (Sign-on) — authentication → Spring Security
- Transaction types/categories — reference data tables

### Phase 2 — Read-Only Views (Medium Complexity, No Write Risk)
- COACTVWC (Account View) — 4-file join pattern → SQL JOIN
- COCRDSLC (Card Detail) — single-file read → REST GET
- COTRN00C/COTRN01C (Transaction List/View) — browse → paginated query

### Phase 3 — Core CRUD Operations (High Complexity, Business Critical)
- COACTUPC (#1 hotspot) — requires validation decomposition
- COCRDUPC (#4) — PCI-sensitive card updates
- COCRDLIC (#5) — browse pattern → pagination
- COTRN02C (Transaction Add) — VSAM write → JPA persist

### Phase 4 — Batch Processing (High Impact, Multi-File I/O)
- CBTRN02C (#8) — transaction posting engine
- CBACT04C — interest calculator
- CBSTM03A/CBSTM03B (#9) — statement generation (eliminate ALTER)
- CBTRN03C — transaction report

### Phase 5 — Optional Modules (Highest Technology Risk)
- COPAUA0C (#6) — IMS + MQ + CICS → Spring + Kafka/SQS
- COPAUS0C (#7) — IMS browse → JPA query
- COTRTLIC (#2) / COTRTUPC (#3) — DB2 → JPA (already relational)

---

## Key Migration Risks

| Risk                                | Affected Programs                    | Mitigation                                           |
|:------------------------------------|:-------------------------------------|:-----------------------------------------------------|
| GO TO / ALTER spaghetti flow        | COACTUPC (51), COTRTLIC (28), CBSTM03A (15 + ALTER) | Refactor to structured control flow before translation |
| IMS DL/I hierarchical access        | COPAUA0C, COPAUS0C, COPAUS1C, CBPAUP0C | Redesign data model to relational; no 1:1 mapping    |
| MQ message integration              | COPAUA0C, CODATE01, COACCT01         | Map to modern async messaging (Kafka, SQS, RabbitMQ) |
| COMP-3 / packed decimal fields      | CVEXPORT, CIPAUDTY, CIPAUSMY         | Binary format handling in Java (use BigDecimal)       |
| COPY REPLACING macros               | COACTUPC (3× CSSETATY REPLACING)     | Expand macros before translation; no direct Java analog |
| VSAM browse (STARTBR/READNEXT/PREV) | COCRDLIC, COTRN00C, COUSR00C         | Replace with cursor-based pagination queries          |
| CICS COMMAREA state management      | All 17+ online programs              | Replace with session/JWT tokens + REST statelessness  |
| Plaintext passwords                 | CSUSR01Y (SEC-USR-PWD)              | Implement bcrypt/scrypt hashing in target system      |
| PCI-DSS card data                   | CVACT02Y, CVACT03Y                   | Tokenization; never store CVV post-auth               |
| TDQ / Internal Reader job submit    | CORPT00C                             | Replace with message queue or async job trigger       |

---

*Generated from static analysis of the CardDemo COBOL codebase — line counts, branching metrics, technology usage, and data access patterns.*
