# CardDemo Modernization Blueprint

> Strategy evaluation for each functional area: Strangler, Replatform, Refactor, and Rewrite approaches.

---

## Strategy Definitions

| Strategy | Description | When to Use |
|----------|-------------|-------------|
| **Strangler Fig** | Incrementally replace components behind an API facade. Old and new coexist during transition. | Low-risk modules with clear boundaries. Read-heavy or isolated functions. |
| **Replatform** | Move existing logic to new runtime (e.g., COBOL on JVM via automated conversion) with minimal changes. | High-risk modules where preserving exact behavior is critical. |
| **Refactor** | Restructure code into modern patterns (services, APIs) while preserving business logic intent. | Medium-complexity modules where the logic is sound but the structure needs modernization. |
| **Rewrite** | Build from scratch using modern frameworks, guided by existing behavior as specification. | Small utilities, deprecated modules, or code with fundamental architectural issues. |

---

## Functional Area Analysis

### 1. Authentication & User Management

**Programs:** COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C
**Data:** USRSEC VSAM (80-byte records, plaintext passwords)
**BMS Maps:** COSGN00, COUSR00–03

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler | ★★★★★ | Fully isolated subsystem — USRSEC is only accessed by these 5 programs. No batch dependencies. Clear API boundary. |
| Replatform | ★★☆☆☆ | Plaintext password storage must be replaced regardless; replatforming preserves the security flaw. |
| Refactor | ★★★☆☆ | Logic is simple enough that refactoring offers little benefit over a clean rewrite. |
| **Rewrite** | ★★★★★ | **RECOMMENDED.** Simple CRUD + auth logic. Must replace plaintext passwords with bcrypt/OAuth. Spring Security + JWT is the natural target. ~1,600 total lines across 5 programs. |

**Recommended Strategy: Rewrite**
- Replace USRSEC VSAM with `users` database table
- Implement Spring Security with BCrypt password hashing
- Add JWT token-based authentication
- Expose REST endpoints: `POST /auth/login`, `GET/POST/PUT/DELETE /users`
- Estimated effort: 2–3 weeks

---

### 2. Account Management

**Programs:** COACTVWC (941 lines), COACTUPC (4,236 lines)
**Data:** ACCTFILE, CARDXREF, CUSTFILE VSAM
**BMS Maps:** COACTVW, COACTUP

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler** | ★★★★☆ | Account View (read-only) is an ideal early strangler target. Account Update is higher risk but can follow. |
| Replatform | ★★★☆☆ | COACTUPC's 4,236 lines of validation logic could be auto-converted, but the generated code would be unmaintainable. |
| **Refactor** | ★★★★★ | **RECOMMENDED for COACTUPC.** Extract validation rules into a rules engine. Decompose the monolithic program into Account Service with separate validation, persistence, and presentation layers. |
| Rewrite | ★★☆☆☆ | Risky — 4,236 lines contain embedded business rules that could be lost in translation. |

**Recommended Strategy: Strangler (View) + Refactor (Update)**
- Phase 1: Strangler — Extract COACTVWC as read-only Account Query REST API
- Phase 2: Refactor — Decompose COACTUPC into Account Update Service with:
  - Validation layer (extract 30+ field validations from procedural code)
  - Persistence layer (replace VSAM REWRITE with JPA/repository pattern)
  - Audit trail (COACTUPC has no audit logging today)
- Estimated effort: 6–8 weeks

---

### 3. Card Management

**Programs:** COCRDLIC (1,459), COCRDSLC (887), COCRDUPC (1,560)
**Data:** CARDFILE, CARDXREF, CUSTFILE VSAM
**BMS Maps:** COCRDLI, COCRDSL, COCRDUP

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler** | ★★★★★ | **RECOMMENDED.** Card List and Card Detail are read-only — perfect early targets. Card Update follows once read path is proven. |
| Replatform | ★★☆☆☆ | Browse logic (STARTBR/READNEXT/READPREV) is deeply CICS-specific; replatforming would produce awkward code. |
| Refactor | ★★★★☆ | Good fit for Card Update — validation logic is complex but well-structured. |
| Rewrite | ★★★☆☆ | Feasible but 3,900+ total lines means significant effort to capture all edge cases. |

**Recommended Strategy: Strangler**
- Phase 1: Card List API with pagination (replace CICS browse with SQL OFFSET/LIMIT)
- Phase 2: Card Detail API (simple VSAM READ → SQL SELECT)
- Phase 3: Card Update API with validation (VSAM REWRITE → JPA save)
- Replace CARDXREF lookup with database JOIN
- Estimated effort: 4–6 weeks

---

### 4. Transaction Management (Online)

**Programs:** COTRN00C (699), COTRN01C (330), COTRN02C (783)
**Data:** TRANSACT, ACCTFILE, CARDXREF VSAM
**BMS Maps:** COTRN00, COTRN01, COTRN02

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler** | ★★★★☆ | Transaction List and View are read-only. Transaction Add writes to TRANSACT and must coordinate with batch posting. |
| Replatform | ★★☆☆☆ | Transaction ID generation via READPREV is a VSAM-specific anti-pattern that shouldn't be preserved. |
| **Refactor** | ★★★★★ | **RECOMMENDED.** Preserve transaction entry business rules but modernize ID generation (UUID/sequence), validation, and persistence. |
| Rewrite | ★★★☆☆ | Feasible for List/View but risky for Add due to embedded validation and ID generation logic. |

**Recommended Strategy: Strangler (List/View) + Refactor (Add)**
- Phase 1: Transaction List/View as REST APIs
- Phase 2: Refactor Transaction Add with:
  - UUID-based transaction ID generation
  - Database-level uniqueness constraints
  - Async event publishing for batch integration
- Estimated effort: 4–5 weeks

---

### 5. Bill Payment

**Programs:** COBIL00C (572)
**Data:** ACCTFILE, CARDXREF, TRANSACT VSAM
**BMS Maps:** COBIL00

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler | ★★★☆☆ | Can be fronted by an API, but the dual-write (TRANSACT + ACCTFILE) requires careful coordination. |
| Replatform | ★★☆☆☆ | The lack of transactional integrity in the current design shouldn't be preserved. |
| **Refactor** | ★★★★★ | **RECOMMENDED.** Restructure as Payment Service with proper transaction boundaries (database transaction or saga). |
| Rewrite | ★★★★☆ | Also viable — relatively small program with clear business logic. |

**Recommended Strategy: Refactor**
- Implement as Payment Service with:
  - Database transaction wrapping balance update + transaction creation
  - Idempotency key to prevent double payments
  - Event publication for downstream processing
- Estimated effort: 3–4 weeks

---

### 6. Transaction Reporting

**Programs:** CORPT00C (649), CBTRN03C (649)
**Data:** TRANSACT, CARDXREF, TRANTYPE, TRANCATG VSAM
**BMS Maps:** CORPT00

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler** | ★★★★★ | **RECOMMENDED.** Reporting is entirely read-only. Currently triggers batch via TDQ — can be replaced with modern reporting framework. |
| Replatform | ★★☆☆☆ | SORT + COBOL report writing is deeply mainframe-specific. |
| Refactor | ★★★☆☆ | Report layout logic in CVTRA07Y is straightforward but format-specific. |
| Rewrite | ★★★★★ | Also excellent — modern reporting tools (JasperReports, database views) are far superior. |

**Recommended Strategy: Strangler → Rewrite**
- Replace CORPT00C's TDQ-to-internal-reader pattern with REST API trigger
- Replace CBTRN03C report generation with database query + template engine
- Replace SORT steps with SQL ORDER BY
- Estimated effort: 3–4 weeks

---

### 7. Batch Transaction Posting

**Programs:** CBTRN01C (494), CBTRN02C (731)
**Data:** DALYTRAN, TRANSACT, ACCTFILE, CARDXREF, TCATBALF VSAM
**JCL:** POSTTRAN, COMBTRAN

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler | ★★☆☆☆ | Tightly coupled to batch cycle ordering and exclusive file locks. Hard to incrementally replace. |
| **Replatform** | ★★★★☆ | **RECOMMENDED for initial phase.** Auto-convert posting logic to Java/Spring Batch, then incrementally refactor. Preserves exact financial calculation behavior during transition. |
| Refactor | ★★★★☆ | Target state — but only after replatform proves behavioral equivalence. |
| Rewrite | ★★☆☆☆ | Too risky for financial posting — subtle calculation differences could cause balance discrepancies. |

**Recommended Strategy: Replatform → Refactor**
- Phase 1: Replatform CBTRN01C/02C to Spring Batch with JDBC
- Phase 2: Add comprehensive reconciliation tests comparing old vs. new output
- Phase 3: Refactor into event-driven transaction processing
- Consolidate CBTRN01C + CBTRN02C into single service
- Estimated effort: 6–8 weeks

---

### 8. Interest Calculation

**Programs:** CBACT04C (652)
**Data:** TCATBALF, DISCGRP, ACCTFILE, CARDXREF, TRANSACT VSAM
**JCL:** INTCALC

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler | ★★☆☆☆ | Cannot run in parallel with old system — interest must be calculated exactly once. |
| **Replatform** | ★★★★★ | **RECOMMENDED.** Financial calculation precision is critical. Auto-convert to preserve exact arithmetic behavior, then wrap with tests. |
| Refactor | ★★★★☆ | Target state after replatform proves equivalence. Extract rate lookup into configuration. |
| Rewrite | ★☆☆☆☆ | Highest risk — COBOL decimal arithmetic and Java BigDecimal can differ subtly. |

**Recommended Strategy: Replatform → Refactor**
- Phase 1: Replatform to Spring Batch job with BigDecimal arithmetic
- Phase 2: Build reconciliation suite comparing interest calculations
- Phase 3: Refactor rate lookup into configurable rules engine
- Estimated effort: 4–6 weeks

---

### 9. Statement Generation

**Programs:** CBSTM03A (924), CBSTM03B (230)
**Data:** TRANSACT (re-keyed), CARDXREF, CUSTFILE, ACCTFILE VSAM
**JCL:** CREASTMT, TXT2PDF1

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler** | ★★★★☆ | Can run new statement generation alongside old and compare output. |
| Replatform | ★★☆☆☆ | HTML generation in COBOL is an anti-pattern that shouldn't be preserved. |
| Refactor | ★★★☆☆ | Data extraction logic is reusable; formatting should be replaced entirely. |
| **Rewrite** | ★★★★★ | **RECOMMENDED.** Modern template engines (Thymeleaf, FreeMarker) + PDF libraries (iText, OpenPDF) are vastly superior. Separate data extraction from rendering. |

**Recommended Strategy: Rewrite**
- Replace CBSTM03A/B with:
  - Statement Data Service (SQL query joining transactions + accounts + customers)
  - Template engine for HTML rendering
  - PDF generation library
- Preserve CBSTM03A as specification for output format
- Estimated effort: 4–5 weeks

---

### 10. Data Export/Import

**Programs:** CBEXPORT (582), CBIMPORT (487)
**Data:** All master VSAM files, EXPORTFL
**JCL:** CBEXPORT, CBIMPORT

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | ★★★★★ | **RECOMMENDED.** Multi-record fixed-format export is a mainframe artifact. Replace with modern data formats (JSON, CSV, Parquet). |
| Strangler | ★★★★☆ | Can coexist during migration to provide backward compatibility. |
| Refactor | ★★☆☆☆ | The fixed-record-type layout is fundamentally a mainframe pattern. |
| Replatform | ★☆☆☆☆ | No value in preserving EBCDIC multi-record format on JVM. |

**Recommended Strategy: Rewrite**
- Replace with REST-based data export/import APIs
- Support JSON, CSV, and optionally Parquet formats
- Add streaming support for large datasets
- Estimated effort: 3–4 weeks

---

## Strategy Summary by Functional Area

| # | Functional Area | Programs | Strategy | Effort | Phase |
|---|----------------|----------|----------|--------|-------|
| 1 | Authentication & User Mgmt | 5 | Rewrite | 2–3 wk | 1 |
| 2 | Transaction Reporting | 2 | Strangler → Rewrite | 3–4 wk | 1 |
| 3 | Card Management | 3 | Strangler | 4–6 wk | 2 |
| 4 | Transaction Mgmt (Online) | 3 | Strangler + Refactor | 4–5 wk | 2 |
| 5 | Account Management | 2 | Strangler + Refactor | 6–8 wk | 3 |
| 6 | Bill Payment | 1 | Refactor | 3–4 wk | 3 |
| 7 | Data Export/Import | 2 | Rewrite | 3–4 wk | 3 |
| 8 | Statement Generation | 2 | Rewrite | 4–5 wk | 4 |
| 9 | Batch Transaction Posting | 2 | Replatform → Refactor | 6–8 wk | 4 |
| 10 | Interest Calculation | 1 | Replatform → Refactor | 4–6 wk | 5 |

**Total estimated effort: 40–53 weeks** (with parallel tracks, achievable in 6–9 months)

---

## Technology Target Stack

| COBOL Concept | Java/Spring Target |
|--------------|-------------------|
| CICS Transactions | Spring MVC REST Controllers |
| BMS 3270 Screens | React/Angular SPA or Thymeleaf templates |
| VSAM KSDS Files | PostgreSQL/MySQL tables with JPA/Hibernate |
| COMMAREA | Spring Security context + HTTP session/JWT |
| JCL Batch Jobs | Spring Batch jobs with JDBC |
| SORT Utility | SQL ORDER BY / database views |
| COPY/Copybooks | Java POJOs/DTOs/Records |
| CICS XCTL | REST API calls or Spring MVC routing |
| TDQ (Transient Data) | Message queue (RabbitMQ/Kafka) |
| Internal Reader | Job scheduler (Quartz/Spring Scheduler) |
| IDCAMS REPRO | Database backup/restore or pg_dump |
| COBDATFT/CEEDAYS | java.time API |
| CEE3ABD | Exception handling + logging frameworks |
