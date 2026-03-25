# CardDemo Hotspot Report — Top 10 Modernization Priorities

> **Source:** Static analysis of `app/cbl/`, `app/cpy/`, `app/jcl/`
> **Application:** AWS CardDemo — Mainframe Credit Card Management System
> **Purpose:** Identify the highest-risk, highest-complexity modules to prioritize for modernization

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Ranking](#top-10-hotspot-ranking)
3. [Detailed Hotspot Analysis](#detailed-hotspot-analysis)
4. [Risk Heatmap](#risk-heatmap)
5. [Modernization Recommendations](#modernization-recommendations)

---

## Scoring Methodology

Each module is scored on three dimensions (1–10 scale each, 30 max):

| Dimension | Weight | Criteria |
|-----------|--------|----------|
| **Complexity** | 10 pts | Lines of code, IF/EVALUATE branching density, PERFORM count, copybook dependencies, CICS commands, file I/O operations |
| **Risk** | 10 pts | Data sensitivity (PII/financial), error handling gaps, cross-file update scope, external dependencies (MQ/DB2/IMS), plaintext credentials |
| **Business Impact** | 10 pts | Business-criticality of function, user-facing vs. background, downstream dependencies, failure blast radius, regulatory relevance |

**Composite Score** = Complexity + Risk + Business Impact (max 30)

---

## Top 10 Hotspot Ranking

| Rank | Program | LOC | Composite Score | Complexity | Risk | Business Impact | Domain |
|------|---------|-----|:---------------:|:----------:|:----:|:---------------:|--------|
| **1** | **COACTUPC.cbl** | 4,236 | **28** | 10 | 9 | 9 | Account Update |
| **2** | **CBTRN02C.cbl** | 731 | **26** | 8 | 9 | 9 | Transaction Posting |
| **3** | **CBACT04C.cbl** | 652 | **25** | 7 | 9 | 9 | Interest Calculation |
| **4** | **COCRDLIC.cbl** | 1,459 | **24** | 9 | 7 | 8 | Card List |
| **5** | **COCRDUPC.cbl** | 1,560 | **24** | 9 | 8 | 7 | Card Update |
| **6** | **CBSTM03A.CBL** | 924 | **23** | 8 | 7 | 8 | Statement Generation |
| **7** | **COTRN02C.cbl** | 783 | **22** | 7 | 8 | 7 | Transaction Add |
| **8** | **COSGN00C.cbl** | 260 | **22** | 4 | 10 | 8 | Sign-On / Auth |
| **9** | **CBTRN03C.cbl** | 649 | **21** | 8 | 6 | 7 | Transaction Report |
| **10** | **COBIL00C.cbl** | 572 | **21** | 7 | 8 | 6 | Bill Payment |

---

## Detailed Hotspot Analysis

### #1 — COACTUPC.cbl (Account Update) — Score: 28/30

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 4,236 | **Largest program in the codebase** — 3× bigger than next largest |
| IF Statements | 164 | Extremely high branching — difficult to test exhaustively |
| EVALUATE Statements | 10 | Complex multi-way decision logic |
| PERFORM Statements | 64 | Deep call tree within the program |
| Copybook Dependencies | 16+ | COCOM01Y, CVACT01Y, CVACT03Y, CVCUS01Y, CVCRD01Y, CSLKPCDY, CSSETATY (×37 REPLACING), CSSTRPFY, CSUTLDPY, CSUTLDWY, etc. |
| CICS File Operations | 5 READ | Reads Account, Card Xref, Customer, Card data, Account (for update) |
| External CALLs | CSUTLDTC | Date validation subroutine |
| BMS Map | COACTUP | Complex form with 30+ input fields |

**Why it's #1:**
- The single largest and most complex program — a "God module" anti-pattern
- Handles account balance updates, credit limit changes, address changes, and status changes in one monolithic program
- 37 uses of `COPY CSSETATY REPLACING` for dynamic field attribute setting — extremely hard to translate
- Reads from 4 different VSAM files in a single transaction
- Any bug here directly corrupts account financial data

**Modernization Strategy:** Decompose into separate services: `AccountBalanceService`, `AccountProfileService`, `AccountStatusService`. Each gets its own REST endpoint and database transaction boundary.

---

### #2 — CBTRN02C.cbl (Transaction Posting) — Score: 26/30

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 731 | Medium-high |
| IF Statements | 48 | High branching for validation logic |
| PERFORM Statements | 61 | Complex flow control |
| Input Files | 4 | DALYTRAN, XREFFILE, ACCTFILE, TCATBALF |
| Output Files | 4 | TRANFILE, DALYREJS, ACCTFILE (update), TCATBALF (update) |

**Why it's #2:**
- **Core financial pipeline** — posts daily transactions to the master file
- Multi-file update: reads daily transactions, validates against cross-reference, updates account balances AND category balances, writes to master transaction file
- Rejected transactions go to DALYREJS — error handling is critical
- Failure halts the entire daily batch cycle (Steps 7–13)
- No explicit transaction/rollback semantics — partial failure leaves data inconsistent

**Modernization Strategy:** Implement as a Spring Batch job with chunk-oriented processing, database transactions with rollback on failure, and a dead-letter queue for rejected transactions.

---

### #3 — CBACT04C.cbl (Interest Calculation) — Score: 25/30

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 652 | Medium |
| IF Statements | 43 | Significant validation/calculation branching |
| PERFORM Statements | 56 | Complex calculation loops |
| Input Files | 5 | TCATBALF, XREFFILE, XREFFIL1, ACCTFILE, DISCGRP |
| Output Files | 2 | TRANSACT (new interest transactions), ACCTFILE (balance updates) |
| PARM Input | `'2022071800'` | Date parameter passed via JCL PARM |

**Why it's #3:**
- **Financial calculation engine** — computes interest charges per account per category
- Reads 5 input files including disclosure group (interest rate tables)
- Creates new transactions for interest charges — directly affects customer balances
- Precision errors in decimal arithmetic (COBOL `S9(09)V99` → Java `BigDecimal`) are a major risk
- Uses JCL PARM for date — requires careful parameterization in modernized version

**Modernization Strategy:** Extract to a dedicated `InterestCalculationService` with comprehensive unit tests for decimal precision. Use `BigDecimal` exclusively. Implement as an idempotent batch job that can be safely re-run.

---

### #4 — COCRDLIC.cbl (Credit Card List) — Score: 24/30

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 1,459 | Large |
| IF Statements | 59 | High branching |
| EVALUATE Statements | 18 | **Highest EVALUATE count** in the codebase |
| PERFORM Statements | 34 | Moderate |
| CICS Browse | STARTBR, READNEXT, READPREV, ENDBR | Full VSAM browse cycle |
| XCTL Targets | COCRDSLC, COCRDUPC | Transfers to view/update |

**Why it's #4:**
- Implements paginated list browsing over VSAM — forward/backward navigation
- 18 EVALUATE statements create complex state machine for handling PF keys, selections, page navigation
- Multiple XCTL transfers based on user action — complex control flow
- VSAM browse operations (STARTBR/READNEXT/READPREV/ENDBR) require careful translation to SQL pagination

**Modernization Strategy:** Replace with a paginated REST API (`GET /cards?page=1&size=10`) backed by SQL `LIMIT/OFFSET`. The VSAM browse state machine becomes a stateless query.

---

### #5 — COCRDUPC.cbl (Credit Card Update) — Score: 24/30

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 1,560 | Large |
| IF Statements | 72 | High branching |
| EVALUATE Statements | 16 | Complex state handling |
| PERFORM Statements | 26 | Moderate |
| Copybooks | 12+ | Includes CVCRD01Y, CVACT02Y, CVCUS01Y, CSSTRPFY |
| CICS File Ops | 2 READ | Card data, Customer data |
| External CALLs | CSUTLDTC | Date validation |

**Why it's #5:**
- Second largest online program — handles card status changes, expiration updates, embossed name changes
- Input validation logic spans 72 IF statements — each field has multiple validation rules
- Uses `COPY CSSTRPFY` for string formatting — custom utility adds complexity
- Card updates affect downstream transaction processing and statement generation

**Modernization Strategy:** Decompose card update into field-specific validators. Implement as `PUT /cards/{cardNum}` with JSON Patch semantics and Bean Validation annotations.

---

### #6 — CBSTM03A.CBL (Statement Generation) — Score: 23/30

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 924 | Large for batch |
| IF Statements | 15 | Moderate |
| EVALUATE Statements | 5 | Moderate |
| PERFORM Statements | 29 | Moderate |
| Input Files | 4 | TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE |
| Output Files | 2 | STMTFILE (text), HTMLFILE (HTML) |
| Subroutine Call | CBSTM03B | Delegates file I/O |

**Why it's #6:**
- **Dual-format output** — generates both plain text and HTML statements
- Reads 4 input files and cross-references card→account→customer
- Calls subroutine CBSTM03B for file operations — split logic across 2 programs
- HTML generation embedded in COBOL — a modernization nightmare
- Statement output is customer-facing and potentially regulatory (PCI-DSS billing statements)

**Modernization Strategy:** Replace with a templating engine (Thymeleaf/FreeMarker). Database queries replace the 4 file reads. PDF generation via a library. Implement as a Spring Batch job with an `ItemWriter` that produces PDF/HTML.

---

### #7 — COTRN02C.cbl (Transaction Add — Online) — Score: 22/30

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 783 | Medium-high |
| IF Statements | 14 | Moderate |
| EVALUATE Statements | 13 | Complex state handling |
| CICS File Ops | READ (×2), STARTBR, READPREV, ENDBR, WRITE | Full CRUD cycle |
| Input Validation | Account + Card + Type + Amount | Multi-field validation |

**Why it's #7:**
- User-facing transaction entry — directly creates financial records
- Reads Account and Card Xref for validation before writing to TRANSACT
- STARTBR/READPREV used to generate next transaction ID (max+1 pattern) — concurrency risk
- WRITE to TRANSACT creates permanent financial records — no undo
- 13 EVALUATE statements for PF key handling and validation states

**Modernization Strategy:** `POST /transactions` REST endpoint with request validation, optimistic locking, and UUID-based IDs instead of sequential max+1.

---

### #8 — COSGN00C.cbl (Sign-On) — Score: 22/30

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 260 | Small |
| IF Statements | 4 | Low complexity |
| EVALUATE Statements | 3 | Simple flow |
| CICS File Ops | 1 READ | Reads USRSEC file |
| Security Risk | **CRITICAL** | Plaintext password comparison |

**Why it's #8 (despite small size):**
- **Gateway to the entire application** — every user passes through this module
- Passwords stored and compared in **plaintext** (`SEC-USR-PWD`) — critical security vulnerability
- No login attempt throttling, no account lockout, no session timeout management
- User type (`A`/`U`) determines which menu is displayed — authorization is coarse-grained
- XCTL to COMEN01C or COADM01C based on user type — single point of failure for access control

**Modernization Strategy:** Replace with Spring Security + BCrypt password hashing + JWT tokens. Implement RBAC with fine-grained permissions. Add rate limiting, account lockout, and audit logging.

---

### #9 — CBTRN03C.cbl (Transaction Report) — Score: 21/30

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 649 | Medium |
| IF Statements | 38 | High for a report program |
| EVALUATE Statements | 4 | Moderate |
| PERFORM Statements | 72 | **Highest PERFORM count** — complex control flow |
| Input Files | 5 | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM |
| Output Files | 1 | TRANREPT (report) |
| Error Handling | `CALL 'CEE3ABD'` | Abnormal termination on critical errors |

**Why it's #9:**
- 72 PERFORM statements — highest in the codebase — indicates deeply nested procedural logic
- Cross-references 5 input files to produce a denormalized report
- Report formatting with page breaks, subtotals, grand totals — complex print logic
- Used by online CORPT00C via TDQ submission — bridges online and batch worlds
- `CEE3ABD` call for error handling means unrecoverable failures

**Modernization Strategy:** Replace with a reporting service using JasperReports or SQL-based reporting. The 5-file join becomes a single SQL query with JOINs. Implement as an async job triggered via REST API.

---

### #10 — COBIL00C.cbl (Bill Payment) — Score: 21/30

| Metric | Value | Assessment |
|--------|-------|------------|
| Lines of Code | 572 | Medium |
| IF Statements | 10 | Moderate |
| EVALUATE Statements | 9 | Complex state handling |
| CICS File Ops | READ, REWRITE, READ, STARTBR, READPREV, ENDBR, WRITE | **7 file operations** — highest for online |
| Files Accessed | ACCTDAT (R/U), CARDXREF (R), TRANSACT (browse + W) | Multi-file update |

**Why it's #10:**
- **Financial transaction** — creates payment records and updates account balances
- 7 CICS file operations in a single transaction — highest I/O density for online programs
- REWRITE to ACCTDAT updates the account balance — directly modifies financial data
- STARTBR/READPREV for transaction ID generation — same concurrency risk as COTRN02C
- Payment amount validation must handle edge cases (overpayment, zero payment, negative)

**Modernization Strategy:** `POST /payments` endpoint with atomic database transaction. Account balance update and transaction creation in a single DB transaction with pessimistic locking.

---

## Risk Heatmap

Visual summary of all 31 core programs across three dimensions:

```
                            BUSINESS IMPACT
                    Low ◄────────────────────► High
                    
         High  │  CBTRN01C    CBTRN03C    CBTRN02C
               │  CBACT01C    CBSTM03A    CBACT04C
    C          │  CBEXPORT    CORPT00C    COACTUPC
    O   Medium │  CBIMPORT    COCRDLIC    COTRN02C
    M          │  CBACT02C    COCRDUPC    COBIL00C
    P          │  CBACT03C    COCRDSLC    COSGN00C
    L   Low    │  COBSWAIT    COUSR01C    COMEN01C
    E          │  CSUTLDTC    COUSR02C    COADM01C
    X          │  CBSTM03B    COUSR03C    
    I          │  CBCUS01C    COUSR00C    COTRN00C
    T   Min    │  UNUSED1Y    COACTVWC    COTRN01C
    Y          │
               └──────────────────────────────────
```

### Color-Coded Priority Zones

| Priority | Programs | Action |
|----------|----------|--------|
| **CRITICAL (Score 25-30)** | COACTUPC, CBTRN02C, CBACT04C | Modernize first — highest risk and complexity. Requires decomposition and extensive testing. |
| **HIGH (Score 22-24)** | COCRDLIC, COCRDUPC, CBSTM03A, COTRN02C, COSGN00C | Modernize in Wave 2 — significant complexity but more contained scope. |
| **MEDIUM (Score 19-21)** | CBTRN03C, COBIL00C, COACTVWC, COTRN00C, CORPT00C | Wave 3 — moderate complexity, can leverage patterns from Wave 1-2. |
| **LOW (Score < 19)** | COUSR00C-03C, COADM01C, COMEN01C, COTRN01C, CSUTLDTC, COBSWAIT, CBACT01C-03C, CBCUS01C | Wave 4 — simpler programs, many are straightforward CRUD or read-only. |

---

## Modernization Recommendations

### Wave 1 — Foundation (Months 1-3)

| Program | Target Architecture | Key Risk |
|---------|-------------------|----------|
| **COSGN00C** | Spring Security + JWT + BCrypt | Plaintext passwords → must hash before go-live |
| **COMEN01C / COADM01C** | React/Angular SPA Router | Menu-driven → route-based navigation |
| **COCOM01Y (COMMAREA)** | HTTP Session / JWT claims | Session state container redesign |

*Rationale:* Authentication is the gateway. Get this right first to enable testing of all other modules.

### Wave 2 — Core Financial Engine (Months 3-6)

| Program | Target Architecture | Key Risk |
|---------|-------------------|----------|
| **CBTRN02C** | Spring Batch + DB transactions | Multi-file consistency → ACID transactions |
| **CBACT04C** | Spring Batch + BigDecimal | Decimal precision in interest calc |
| **COACTUPC** | Decomposed REST services | Monolith → microservices decomposition |
| **COBIL00C** | `POST /payments` endpoint | Multi-file update atomicity |

*Rationale:* These are the financial core. Precision, consistency, and atomicity are paramount.

### Wave 3 — Card & Transaction Management (Months 6-9)

| Program | Target Architecture | Key Risk |
|---------|-------------------|----------|
| **COCRDLIC / COCRDSLC / COCRDUPC** | REST CRUD + paginated queries | VSAM browse → SQL pagination |
| **COTRN00C / COTRN01C / COTRN02C** | REST CRUD endpoints | Sequential ID → UUID |
| **CORPT00C** | Async report API | TDQ submission → message queue |

### Wave 4 — Reporting & Utilities (Months 9-12)

| Program | Target Architecture | Key Risk |
|---------|-------------------|----------|
| **CBSTM03A / CBSTM03B** | Templating engine + PDF | Embedded HTML → template engine |
| **CBTRN03C** | JasperReports / SQL reporting | 5-file join → SQL JOIN |
| **COUSR00C-03C** | User management REST API | Simple CRUD — low risk |
| **CBEXPORT / CBIMPORT** | Spring Batch ETL | File format → database migration |

### Cross-Cutting Concerns

| Concern | Current State | Target State |
|---------|--------------|--------------|
| **Security** | Plaintext passwords, no encryption | BCrypt + TLS + field-level encryption for PII |
| **Data Persistence** | VSAM KSDS files | PostgreSQL / MySQL with JPA |
| **Session Management** | CICS COMMAREA (200 bytes) | Stateless JWT or server-side session |
| **Error Handling** | CICS RESP codes + CEE3ABD | Exception handling + circuit breakers |
| **Logging** | SYSPRINT DD SYSOUT=* | SLF4J + structured logging + ELK |
| **Scheduling** | CA7 / Control-M JCL submission | Spring Batch + cron or Kubernetes CronJobs |
| **Screen UI** | BMS 3270 maps (24×80 chars) | React/Angular responsive web UI |
| **Inter-program Comm** | CICS XCTL / LINK / COMMAREA | REST API / Message Queue |
