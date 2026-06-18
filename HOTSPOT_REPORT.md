# Hotspot Report — CardDemo COBOL Estate

## Scoring Methodology

Each program is scored across five dimensions. Within each dimension, programs are ranked 1-10 (10 = highest complexity). The **composite score** is the sum of all five ranks, with a maximum of 50.

| Dimension | Metric | How Measured |
|-----------|--------|-------------|
| **Size** | Lines of code | `wc -l` |
| **Copybook Fan-Out** | Number of COPY statements | `grep -ci "COPY "` |
| **I/O Breadth** | Count of I/O operations (EXEC SQL/CICS/DLI, READ, WRITE, OPEN, CLOSE, REWRITE, DELETE, STARTBR, ENDBR, READNEXT, READPREV) | `grep -ciE` pattern match |
| **Logic Density** | Count of EVALUATE + IF statements (proxy for conditional complexity) | `grep -ciE "EVALUATE\|IF "` |
| **Inter-Program Dependencies** | CALL, XCTL, LINK statements + number of distinct programs referenced | `grep -ciE "CALL \|XCTL\|LINK "` + manual analysis |

---

## Top 10 Programs by Composite Score

| Rank | Program | LOC | Copybooks | I/O Ops | Logic (IF/EVAL) | Dependencies | Composite | Classification |
|------|---------|----:|----------:|--------:|----------------:|-------------:|----------:|----------------|
| **1** | **COACTUPC.cbl** | 4,236 | 58 | 35 | 194 | 6 (XCTL + CICS file) | **48** | Online (CICS) |
| **2** | **COTRTLIC.cbl** | 2,098 | 12 | 57 | 131 | 3 (XCTL) | **40** | Online (CICS+DB2) |
| **3** | **COTRTUPC.cbl** | 1,702 | 15 | 50 | 134 | 2 (SYNCPOINT) | **39** | Online (CICS+DB2) |
| **4** | **COCRDUPC.cbl** | 1,560 | 16 | 18 | 167 | 2 (XCTL) | **36** | Online (CICS) |
| **5** | **COCRDLIC.cbl** | 1,459 | 14 | 25 | 149 | 6 (XCTL + browse) | **35** | Online (CICS) |
| **6** | **COPAUA0C.cbl** | 1,026 | 17 | 31 | 62 | 8 (MQ+DLI+CICS) | **34** | Online (CICS+MQ+IMS) |
| **7** | **CBSTM03A.CBL** | 924 | 4 | 119 | 30 | 15 (CALL CBSTM03B ×13) | **33** | Batch |
| **8** | **CBTRN02C.cbl** | 731 | 8 | 29 | 93 | 1 (CEE3ABD) | **30** | Batch |
| **9** | **COPAUS0C.cbl** | 1,032 | 15 | 22 | 47 | 4 (DLI+CICS) | **29** | Online (CICS+IMS) |
| **10** | **CBACT04C.cbl** | 652 | 5 | 24 | 86 | 1 (CEE3ABD) | **28** | Batch |

### Detailed Dimension Rankings

| Program | LOC Rank | Copy Rank | I/O Rank | Logic Rank | Deps Rank |
|---------|---------|----------|---------|-----------|----------|
| COACTUPC | 10 | 10 | 8 | 10 | 10 |
| COTRTLIC | 9 | 5 | 10 | 7 | 9 |
| COTRTUPC | 8 | 7 | 9 | 8 | 7 |
| COCRDUPC | 7 | 8 | 5 | 9 | 7 |
| COCRDLIC | 6 | 6 | 6 | 8 | 9 |
| COPAUA0C | 4 | 9 | 7 | 5 | 9 |
| CBSTM03A | 3 | 2 | 10 | 3 | 10 |
| CBTRN02C | 2 | 4 | 7 | 6 | 4 |
| COPAUS0C | 5 | 7 | 5 | 4 | 8 |
| CBACT04C | 1 | 3 | 6 | 6 | 4 |

---

## Dimension Deep Dives

### Lines of Code (Top 10)

| # | Program | LOC |
|---|---------|----:|
| 1 | COACTUPC.cbl | 4,236 |
| 2 | COTRTLIC.cbl | 2,098 |
| 3 | COTRTUPC.cbl | 1,702 |
| 4 | COCRDUPC.cbl | 1,560 |
| 5 | COCRDLIC.cbl | 1,459 |
| 6 | COPAUS0C.cbl | 1,032 |
| 7 | COPAUA0C.cbl | 1,026 |
| 8 | COACTVWC.cbl | 941 |
| 9 | CBSTM03A.CBL | 924 |
| 10 | COCRDSLC.cbl | 887 |

### Copybook References (Top 10)

| # | Program | COPY Count |
|---|---------|----------:|
| 1 | COACTUPC.cbl | 58 |
| 2 | COPAUA0C.cbl | 17 |
| 3 | COCRDUPC.cbl | 16 |
| 4 | COCRDSLC.cbl | 16 |
| 5 | COACTVWC.cbl | 16 |
| 6 | COTRTUPC.cbl | 15 |
| 7 | COPAUS0C.cbl | 15 |
| 8 | COCRDLIC.cbl | 14 |
| 9 | COTRTLIC.cbl | 12 |
| 10 | COPAUS1C.cbl | 11 |

### I/O Operations (Top 10)

| # | Program | I/O Count | Primary I/O Type |
|---|---------|----------:|-----------------|
| 1 | CBSTM03A.CBL | 119 | CALL CBSTM03B (file ops delegated to subroutine) |
| 2 | COTRTLIC.cbl | 57 | EXEC SQL (DB2 cursor operations) |
| 3 | COTRTUPC.cbl | 50 | EXEC SQL (DB2 CRUD) |
| 4 | CBIMPORT.cbl | 36 | WRITE (multiple output files) |
| 5 | COACTUPC.cbl | 35 | EXEC CICS READ/REWRITE |
| 6 | CBEXPORT.cbl | 35 | READ (5 input files) + WRITE (1 output) |
| 7 | COPAUA0C.cbl | 31 | MQ (MQOPEN/GET/PUT/CLOSE) + DLI + CICS READ |
| 8 | COUSR00C.cbl | 29 | EXEC CICS STARTBR/READNEXT/READPREV |
| 9 | CBTRN02C.cbl | 29 | READ/WRITE/REWRITE (VSAM files) |
| 10 | COTRN00C.cbl | 28 | EXEC CICS STARTBR/READNEXT/READPREV |

### Business Logic Density — EVALUATE/IF Count (Top 10)

| # | Program | IF + EVALUATE Count | Max Nesting Depth |
|---|---------|-------------------:|------------------:|
| 1 | COACTUPC.cbl | 194 | 10 |
| 2 | COCRDUPC.cbl | 167 | 10 |
| 3 | COCRDLIC.cbl | 149 | 17 |
| 4 | COTRTUPC.cbl | 134 | 8 |
| 5 | COTRTLIC.cbl | 131 | 18 |
| 6 | CBTRN02C.cbl | 93 | 3 |
| 7 | CBACT04C.cbl | 86 | 4 |
| 8 | COCRDSLC.cbl | 80 | 5 |
| 9 | CBTRN03C.cbl | 79 | 3 |
| 10 | COACTVWC.cbl | 70 | 5 |

### Inter-Program Dependencies (Top 10)

| # | Program | CALL/XCTL/LINK Count | External Programs Called |
|---|---------|--------------------:|------------------------|
| 1 | CBSTM03A.CBL | 15 | CBSTM03B (13 calls with different function codes) |
| 2 | COACCT01.cbl | 9 | MQ APIs (MQOPEN, MQGET, MQPUT, MQCLOSE) |
| 3 | CODATE01.cbl | 9 | MQ APIs |
| 4 | PAUDBLOD.CBL | 9 | IMS CBLTDLI (DLI calls) |
| 5 | COPAUA0C.cbl | 8 | MQ APIs + IMS CBLTDLI |
| 6 | DBUNLDGS.CBL | 7 | IMS CBLTDLI + GSAM |
| 7 | COCRDLIC.cbl | 6 | COCRDSLC, COCRDUPC (via XCTL) |
| 8 | PAUDBUNL.CBL | 5 | IMS CBLTDLI |
| 9 | COTRN02C.cbl | 3 | CSUTLDTC (date utility) |
| 10 | CORPT00C.cbl | 3 | CSUTLDTC (date utility) |

---

## Modernization Priority Recommendations

### Tier 1 — Modernize First (Highest Impact + Highest Risk)

#### 1. COACTUPC.cbl (Account Update) — Priority: CRITICAL
- **Why first:** Largest program (4,236 LOC), highest copybook fan-out (58), highest logic density (194 IF/EVALUATE), deepest CICS integration. This is the most complex single program in the estate and a primary touchpoint for account modifications.
- **Risk factors:** 58 copybook dependencies create a wide blast radius. Extensive field-level validation (dates, SSN, state codes, ZIP codes via CSLKPCDY lookup tables). The CSSETATY template copybook uses non-standard substitution tokens for screen attribute setting.
- **Approach:** Decompose into domain services: AccountValidationService, AccountPersistenceService, AccountScreenController. Extract CSLKPCDY validation tables to a shared lookup microservice.

#### 2. CBTRN02C.cbl (Transaction Posting) — Priority: CRITICAL
- **Why early:** Core batch pipeline — posts daily transactions, updates account balances, and maintains category balance accumulators. Directly impacts financial accuracy. Used daily by POSTTRAN.jcl.
- **Risk factors:** Modifies 3 VSAM files in a single batch run (TRANSACT write, ACCTFILE rewrite, TCATBALF write/rewrite). Transaction integrity depends on sequential processing order. Rejection logic writes to GDG — must preserve audit trail.
- **Approach:** Convert to transactional service with ACID guarantees. Implement idempotent transaction posting with deduplication.

#### 3. CBACT04C.cbl (Interest Calculation) — Priority: CRITICAL
- **Why early:** Monthly financial calculation — computes interest and fees based on category balances and disclosure group rates. Errors here directly impact billing.
- **Risk factors:** Complex multi-file join logic (TCATBALF → XREFFILE → DISCGRP → ACCTFILE). Rate calculation with COMP-3 packed decimal arithmetic.
- **Approach:** Convert to a scheduled calculation service. Replace VSAM joins with SQL queries against normalized tables.

### Tier 2 — Modernize Second (High Complexity, User-Facing)

#### 4. COTRTLIC.cbl + COTRTUPC.cbl (Transaction Type List/Update) — Priority: HIGH
- **Why:** Already using DB2 — these are the most "modernization-ready" programs. Combined 3,800 LOC with DB2 cursor-based pagination. Good candidates for demonstrating the migration pattern.
- **Risk factors:** Complex DB2 cursor management with SENSITIVE DYNAMIC cursors. COTRTLIC has nesting depth of 18 — highest in the estate.
- **Approach:** Replace CICS screens with REST API + web UI. DB2 tables can be migrated to PostgreSQL/MySQL with minimal schema changes.

#### 5. COCRDLIC.cbl + COCRDUPC.cbl (Card List/Update) — Priority: HIGH
- **Why:** Combined 3,019 LOC of card management with high logic density (316 IF/EVALUATE combined). COCRDLIC has VSAM browse operations with forward/backward paging.
- **Risk factors:** Alternate index browsing via CICS STARTBR — paging logic must be re-implemented. COCRDUPC has extensive validation.
- **Approach:** API-first card management service with cursor-based pagination.

#### 6. COPAUA0C.cbl (Authorization Processor) — Priority: HIGH
- **Why:** Most architecturally complex program — spans CICS + MQ + IMS + VSAM in a single execution path. The only program in the estate that orchestrates across all four subsystems.
- **Risk factors:** MQ message correlation, IMS hierarchical data access, and CICS file I/O all in one program. Approval/decline logic is business-critical for real-time card authorization.
- **Approach:** Event-driven microservice consuming from message queue, with separate auth-decision and persistence components.

### Tier 3 — Modernize Third (Lower Risk, Can Follow Patterns)

#### 7. CBSTM03A.CBL + CBSTM03B.CBL (Statement Generation) — Priority: MEDIUM
- **Why:** Highest I/O count (119 ops). Generates both text and HTML statements. Already has a clean subroutine architecture (CBSTM03A orchestrates, CBSTM03B handles file ops via 13 parameterized calls).
- **Approach:** Statement generation batch job → modern templating engine (e.g., Thymeleaf).

#### 8. COSGN00C → COADM01C → COMEN01C (Navigation Shell) — Priority: MEDIUM
- **Why:** These form the application shell — sign-on, admin menu, user menu. Relatively simple programs (260-308 LOC each) but they define the entry point and routing logic.
- **Approach:** Replace with Spring Security + router. Convert COMMAREA to session/JWT.

#### 9. CBEXPORT.cbl + CBIMPORT.cbl (Data Migration) — Priority: LOW
- **Why:** These are already migration utilities. Once the target platform is established, they can be replaced by ETL tooling or database migration scripts.
- **Approach:** Replace with database-native export/import or ETL pipeline.

#### 10. COUSR00-03C (User CRUD) — Priority: LOW
- **Why:** Simple CRUD on USRSEC VSAM file. Passwords stored in plaintext (SEC-USR-PWD PIC X(08)). This is a straightforward replacement with modern auth.
- **Approach:** Replace with Spring Security + JPA. Migrate to hashed passwords.

---

## Key Modernization Risks

| Risk | Impact | Programs Affected | Mitigation |
|------|--------|-------------------|------------|
| **VSAM alternate index semantics** | Browsing via AIX (CXACAIX, CARDAIX) uses different key ordering than base cluster | COCRDLIC, COACTUPC, COACTVWC, COBIL00C, COTRN02C | Map AIX paths to SQL secondary indexes; validate sort order equivalence |
| **IMS hierarchical data model** | Root/child segment navigation (GU/GNP) has no direct SQL equivalent | COPAUA0C, COPAUS0C, COPAUS1C, CBPAUP0C, DBUNLDGS, PAUDBLOD, PAUDBUNL | Flatten IMS hierarchy to relational tables; preserve parent-child relationships via foreign keys |
| **MQ message correlation** | Request/reply pattern with correlation IDs must be preserved | COPAUA0C, COACCT01, CODATE01 | Use JMS or Kafka with correlation headers |
| **Packed decimal arithmetic (COMP-3)** | Financial calculations use COMP-3 for precision | CBACT04C, COPAUA0C, CBTRN02C | Use Java BigDecimal with explicit scale/rounding |
| **Plaintext passwords** | SEC-USR-PWD stores 8-char plaintext passwords | COSGN00C, COUSR01C, COUSR02C | Migrate to bcrypt/scrypt hash on day one |
| **CSLKPCDY validation tables** | 300+ phone area codes, 50+ state codes, ZIP mappings embedded in copybook | COACTUPC | Externalize to database reference table or validation service |
| **COMMAREA state passing** | All online programs share CARDDEMO-COMMAREA structure via XCTL | All 20 online programs | Map to session state or JWT claims; ensure no program modifies unexpected fields |
| **GDG (Generation Data Group)** | Versioned backup datasets with automatic generation management | TRANBKP, TRANREPT, DALYREJS, REPTFILE, DEFGDGB | Replace with timestamped file storage or cloud object versioning |
