# CardDemo Hotspot Report

> Top 10 modules prioritized by complexity, risk, and business impact for modernization planning.

---

## Scoring Methodology

Each module is scored on three dimensions (1–5 scale):

| Dimension | Criteria | Weight |
|-----------|----------|--------|
| **Complexity** | Lines of code, number of COPY dependencies, nested logic depth, CICS commands, VSAM file access patterns | 35% |
| **Risk** | Data modification scope, concurrent access (online+batch), error handling maturity, external dependencies | 35% |
| **Business Impact** | Transaction volume criticality, financial data handling, user-facing frequency, downstream dependencies | 30% |

**Composite Score** = (Complexity × 0.35) + (Risk × 0.35) + (Business Impact × 0.30) — scaled to 10.

---

## Top 10 Hotspot Modules

### #1 — COACTUPC (Account Update) — Score: 9.5/10

| Dimension | Score | Justification |
|-----------|-------|---------------|
| Complexity | 5/5 | **4,236 lines** — largest program by 3×. 16+ copybook dependencies. 30+ COPY REPLACING statements for BMS attribute control. Deep EVALUATE/PERFORM nesting. Multiple VSAM READ/REWRITE operations with error handling. Uses CSSETATY, CSSTRPFY, CSUTLDPY, CSUTLDWY utility copybooks. |
| Risk | 5/5 | **Writes to ACCTFILE** (financial data). Concurrent access conflict with batch posting (CBTRN01C/02C). Field-level validation logic embedded in 3,000+ lines of procedural code. HANDLE ABEND for error recovery. Any bug directly corrupts account balances. |
| Business Impact | 5/5 | Core account management function. Every account modification flows through this program. Impacts credit limits, balances, and account status — all financially critical. |

**Modernization Priority:** CRITICAL — Extract first as a standalone Account Service with proper transaction management and validation framework.

---

### #2 — CBTRN02C (Transaction Posting v2) — Score: 9.0/10

| Dimension | Score | Justification |
|-----------|-------|---------------|
| Complexity | 4/5 | **731 lines**. Reads DALYTRAN sequential file, writes to TRANSACT VSAM, updates CARDXREF, ACCTFILE, and TCATBALF. 5 copybook data structures. Multi-file coordination logic. |
| Risk | 5/5 | **Core financial posting** — transfers daily transactions to master file and updates account balances. Requires exclusive file locks (CLOSEFIL). Failure mid-run leaves files inconsistent (no two-phase commit). Category balance tracking adds complexity. |
| Business Impact | 5/5 | Every daily transaction flows through this program. Batch cycle cannot proceed without successful posting. Feeds interest calculation, statements, and reports downstream. |

**Modernization Priority:** CRITICAL — Replace with event-driven transaction processing service with proper ACID guarantees.

---

### #3 — CBSTM03A (Statement Generator) — Score: 8.5/10

| Dimension | Score | Justification |
|-----------|-------|---------------|
| Complexity | 5/5 | **924 lines**. Calls CBSTM03B subroutine **13 times** for file I/O. Produces dual-format output (plain text + HTML). Joins 4 VSAM files (TRANSACT, CARDXREF, CUSTFILE, ACCTFILE). Mainframe control block addressing. Contains deliberate complexity patterns for modernization testing. |
| Risk | 4/5 | Read-only on master files (lower write risk), but complex multi-file join logic. Failure affects customer-facing statements. HTML generation embeds business logic in formatting code. |
| Business Impact | 4/5 | Customer-facing statements are a regulatory requirement. Feeds TXT2PDF1 for PDF conversion. Must maintain exact formatting for compliance. |

**Modernization Priority:** HIGH — Decompose into data extraction service + template-based rendering engine.

---

### #4 — COCRDLIC (Card List) — Score: 8.0/10

| Dimension | Score | Justification |
|-----------|-------|---------------|
| Complexity | 5/5 | **1,459 lines**. Complex browse logic with forward/backward paging (STARTBR/READNEXT/READPREV/ENDBR). Role-based filtering (admin sees all, regular user sees own account cards only). 10 copybook dependencies. XCTL navigation to detail/update screens. CSSTRPFY string parsing. |
| Risk | 3/5 | Read-only VSAM access (CARDFILE). Navigation branching — incorrect XCTL could lose COMMAREA context. Role-based access control logic embedded in browse. |
| Business Impact | 4/5 | Primary card management screen. Gateway to card detail and update functions. Used in every user session. |

**Modernization Priority:** HIGH — Extract as Card List API with pagination and role-based filtering.

---

### #5 — COCRDUPC (Card Update) — Score: 7.8/10

| Dimension | Score | Justification |
|-----------|-------|---------------|
| Complexity | 4/5 | **1,560 lines**. 12 copybook dependencies. Field-level validation, BMS attribute control, VSAM READ/REWRITE. CSSTRPFY string parsing. HANDLE ABEND error recovery. |
| Risk | 4/5 | **Writes to CARDFILE** — modifies card status, embossed name, expiration. Incorrect updates could deactivate valid cards or activate invalid ones. Shared file access with batch. |
| Business Impact | 4/5 | Card modifications directly affect cardholder ability to transact. Card activation/deactivation is time-sensitive. |

**Modernization Priority:** HIGH — Combine with COCRDSLC into a Card Management Service.

---

### #6 — CBACT04C (Interest Calculator) — Score: 7.5/10

| Dimension | Score | Justification |
|-----------|-------|---------------|
| Complexity | 4/5 | **652 lines**. Reads 5 VSAM files (TCATBALF, CARDXREF, DISCGRP, ACCTFILE, TRANSACT). Complex financial calculation logic — interest rate lookup by account group + transaction category. Updates TCATBALF category balances. |
| Risk | 5/5 | **Financial calculation accuracy is critical** — incorrect interest rates directly impact customer billing. No unit test framework in COBOL. Batch-only (runs nightly), so errors compound until detected. |
| Business Impact | 4/5 | Regulatory requirement for accurate interest computation. Feeds into statement generation. Errors compound daily if undetected. |

**Modernization Priority:** HIGH — Extract as Interest Calculation Service with comprehensive unit testing and audit trail.

---

### #7 — COBIL00C (Bill Payment) — Score: 7.3/10

| Dimension | Score | Justification |
|-----------|-------|---------------|
| Complexity | 3/5 | **572 lines**. Reads ACCTFILE and CARDXREF, writes payment transaction to TRANSACT, updates account balance via REWRITE. Uses CICS ASKTIME/FORMATTIME for timestamp generation. Browse logic for transaction ID generation. |
| Risk | 5/5 | **Financial transaction creation + balance modification** in a single operation. No compensating transaction on partial failure. Writes to both TRANSACT and ACCTFILE — if REWRITE fails after WRITE, data is inconsistent. |
| Business Impact | 4/5 | Direct money movement — customer pays account balance. Incorrect posting means customer charged/credited wrong amount. |

**Modernization Priority:** HIGH — Implement as Payment Service with saga pattern for consistency.

---

### #8 — COTRN02C (Transaction Add) — Score: 7.0/10

| Dimension | Score | Justification |
|-----------|-------|---------------|
| Complexity | 4/5 | **783 lines**. Card/account validation via CARDXREF and ACCTFILE reads. Auto-generates transaction ID using STARTBR/READPREV pattern. CALL to CSUTLDTC for date validation. WRITE to TRANSACT VSAM. |
| Risk | 4/5 | Creates new financial transactions. ID generation via READPREV could produce duplicates under concurrent access. Date validation dependency on external subroutine. |
| Business Impact | 4/5 | Every manual transaction entry flows through this program. Feeds into batch posting cycle. |

**Modernization Priority:** MEDIUM-HIGH — Extract as Transaction Entry Service with proper ID generation (UUID/sequence).

---

### #9 — COACTVWC (Account View) — Score: 6.8/10

| Dimension | Score | Justification |
|-----------|-------|---------------|
| Complexity | 4/5 | **941 lines**. 13 copybook dependencies — most for a read-only program. Joins ACCTFILE + CARDXREF + CUSTFILE. CSSTRPFY string parsing. HANDLE ABEND. Multiple CICS READ operations with error branching. |
| Risk | 2/5 | Read-only — no data modification risk. However, incorrect display of account data could lead to wrong business decisions. |
| Business Impact | 4/5 | Most frequently accessed screen after menu. Every account inquiry starts here. |

**Modernization Priority:** MEDIUM — Extract as Account Query Service. Low risk makes it a good early candidate for strangler pattern.

---

### #10 — CBTRN01C (Transaction Poster v1) — Score: 6.5/10

| Dimension | Score | Justification |
|-----------|-------|---------------|
| Complexity | 3/5 | **494 lines**. Reads DALYTRAN, writes to TRANSACT, updates CUSTFILE/CARDXREF/CARDFILE/ACCTFILE. 6 copybook dependencies. Simpler than CBTRN02C (no category balance tracking). |
| Risk | 5/5 | Same financial posting risk as CBTRN02C — multi-file updates without transactional integrity. Legacy version that may still be in production alongside v2. |
| Business Impact | 3/5 | May be superseded by CBTRN02C in newer configurations, but still present and potentially active. |

**Modernization Priority:** MEDIUM — Consolidate with CBTRN02C into unified Transaction Posting Service.

---

## Hotspot Summary Matrix

| Rank | Module | Lines | Complexity | Risk | Impact | Score | Priority |
|------|--------|-------|-----------|------|--------|-------|----------|
| 1 | COACTUPC | 4,236 | 5 | 5 | 5 | **9.5** | CRITICAL |
| 2 | CBTRN02C | 731 | 4 | 5 | 5 | **9.0** | CRITICAL |
| 3 | CBSTM03A | 924 | 5 | 4 | 4 | **8.5** | HIGH |
| 4 | COCRDLIC | 1,459 | 5 | 3 | 4 | **8.0** | HIGH |
| 5 | COCRDUPC | 1,560 | 4 | 4 | 4 | **7.8** | HIGH |
| 6 | CBACT04C | 652 | 4 | 5 | 4 | **7.5** | HIGH |
| 7 | COBIL00C | 572 | 3 | 5 | 4 | **7.3** | HIGH |
| 8 | COTRN02C | 783 | 4 | 4 | 4 | **7.0** | MEDIUM-HIGH |
| 9 | COACTVWC | 941 | 4 | 2 | 4 | **6.8** | MEDIUM |
| 10 | CBTRN01C | 494 | 3 | 5 | 3 | **6.5** | MEDIUM |

---

## Recommendations

### Immediate Actions
1. **COACTUPC** — Refactor into Account Update Service with input validation framework, proper error handling, and unit tests. This single program represents ~25% of the online codebase complexity.
2. **CBTRN02C** — Replace batch posting with event-driven processing and ACID transaction support.
3. **CBACT04C** — Extract interest calculation into a testable, auditable service with configurable rate tables.

### Quick Wins (Lower Risk, High Value)
4. **COACTVWC** — Read-only account view is an ideal strangler pattern candidate. Extract as REST API with minimal risk.
5. **CORPT00C** — Reporting trigger can be replaced with modern job scheduling without touching batch logic.

### Consolidation Opportunities
6. Merge **CBTRN01C + CBTRN02C** into a single Transaction Posting Service.
7. Merge **COCRDLIC + COCRDSLC + COCRDUPC** into a Card Management Service.
8. Merge **COUSR00C + COUSR01C + COUSR02C + COUSR03C** into a User Management Service.
