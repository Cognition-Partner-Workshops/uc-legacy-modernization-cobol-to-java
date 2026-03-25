# CardDemo Modernization Blueprint

> **Generated:** 2026-03-25 | **Scope:** Strategy evaluation for migrating CardDemo from COBOL/CICS/VSAM to Java/Spring Boot

---

## 1. Strategy Definitions

| Strategy | Definition | When to Use | Typical Cost | Typical Risk |
|----------|-----------|-------------|-------------|-------------|
| **Strangler Fig** | Incrementally replace modules behind a facade while the legacy system continues to run. New Java services handle new traffic; legacy handles remainder. | Large systems that must stay live during migration. Modules with clear API boundaries. | Medium-High | Low |
| **Replatform** | Move COBOL source to a cloud-hosted mainframe emulator or automated code converter (e.g., AWS Blu Age, Micro Focus). Minimal code rewrite. | When time-to-cloud is critical and code fidelity is paramount. Acceptable for interim states. | Low-Medium | Medium |
| **Refactor** | Restructure existing logic into Java while preserving business rules. Manual translation with architectural improvements. | Moderate-complexity modules where behavior is well-understood and tests exist. | Medium | Medium |
| **Rewrite** | Build new Java implementation from requirements/specs, not from COBOL source. | Simple CRUD modules, or modules so tangled that line-by-line translation is harder than starting fresh. | High | High |

---

## 2. Functional Area Assessment

### 2.1 Security & Authentication

| Attribute | Detail |
|-----------|--------|
| **Programs** | COSGN00C (sign-on), CSUSR01Y (user record copybook) |
| **LOC** | 260 |
| **Current State** | Plaintext password storage in USRSEC VSAM. 8-char passwords. Two roles: Admin (A), Regular (U). CICS ASSIGN for terminal ID. |
| **Data** | USRSEC.VSAM.KSDS — 80-byte records (user ID, name, password, type) |
| **Complexity** | Low — simple READ against VSAM, compare password, XCTL to menu |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ★★★★☆ | Can stand up Spring Security independently and route auth through it before migrating any other module. The auth boundary is clean. |
| Replatform | ★★☆☆☆ | Preserves plaintext passwords and weak auth model — defeats the purpose. |
| Refactor | ★★★☆☆ | Straightforward to translate, but the model is so simple that refactoring gains little. |
| **Rewrite** | **★★★★★** | **Recommended.** Replace with Spring Security + BCrypt. Add JWT/OAuth2. The existing model is a security liability, not an asset to preserve. |

**Recommended Strategy: Rewrite**
- Replace USRSEC VSAM with a `users` table + Spring Security `UserDetailsService`
- Add password hashing (BCrypt), JWT tokens, role-based access control
- This is the **first module to migrate** — all other modules depend on authentication

---

### 2.2 Navigation & Menu System

| Attribute | Detail |
|-----------|--------|
| **Programs** | COMEN01C (main menu), COADM01C (admin menu) |
| **LOC** | 596 combined |
| **Current State** | BMS screen with numbered options. COMEN02Y copybook holds routing table (option → transaction ID → program). CICS XCTL transfers control. |
| **Complexity** | Low — menu display and dispatch only |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ★★★★★ | **Recommended.** The menu IS the facade. Replace it with a web UI shell that can route to either legacy (via 3270 bridge) or new Java services. |
| Replatform | ★★☆☆☆ | Preserves 3270 terminal paradigm — no value for web migration. |
| Refactor | ★★★☆☆ | Could translate the routing table, but the concept changes entirely in a web app. |
| Rewrite | ★★★★☆ | Simple enough to rewrite, but the strangler approach gives incremental migration benefits. |

**Recommended Strategy: Strangler Fig**
- Build a web UI shell (React/Angular/Thymeleaf) that acts as the new menu
- Each menu option routes to either a new Java endpoint or a legacy 3270 bridge
- As modules are migrated, update the route — zero big-bang cutover

---

### 2.3 Account Management

| Attribute | Detail |
|-----------|--------|
| **Programs** | COACTVWC (view, 941 LOC), COACTUPC (update, 4,236 LOC) |
| **LOC** | 5,177 combined |
| **Current State** | COACTUPC is the #1 hotspot — 167 IF statements, 20 EVALUATE blocks. Reads/writes ACCTDATA, CARDXREF, CUSTDATA. COACTVWC is read-only browse. |
| **Data** | ACCTDATA.VSAM.KSDS (300 bytes) — balances, credit limits, dates, status |
| **Complexity** | Very High (COACTUPC), Moderate (COACTVWC) |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **★★★★★** | **Recommended.** Migrate view first (low risk), then update. Run old and new in parallel with data sync. |
| Replatform | ★★★☆☆ | Automated conversion would handle the LOC volume, but the 167 IF branches need human review regardless. |
| Refactor | ★★★★☆ | Strong option for COACTVWC (straightforward). COACTUPC's complexity makes pure refactor slow. |
| Rewrite | ★★☆☆☆ | Too risky — the 4,236-line update program embeds critical business rules that could be lost. |

**Recommended Strategy: Strangler Fig**
- Phase A: Migrate COACTVWC (read-only) to a Spring MVC controller + JPA queries
- Phase B: Decompose COACTUPC into `AccountValidationService` + `AccountUpdateService`
- Use the strangler facade to route reads to Java, writes to legacy until validation is complete
- Dual-write verification period before cutting over writes

---

### 2.4 Card Management

| Attribute | Detail |
|-----------|--------|
| **Programs** | COCRDLIC (list, 1,459 LOC), COCRDSLC (view, 887 LOC), COCRDUPC (update, 1,560 LOC) |
| **LOC** | 3,906 combined |
| **Current State** | COCRDLIC has the highest CICS call count (18). Complex VSAM browse pagination (STARTBR/READNEXT/READPREV/ENDBR). COCRDUPC handles card activation/deactivation. HANDLE ABEND for error recovery. |
| **Data** | CARDDATA.VSAM.KSDS (150 bytes), CARDXREF.VSAM.KSDS (50 bytes) |
| **Complexity** | High across all three programs |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **★★★★★** | **Recommended.** List → View → Update progression mirrors account management. |
| Replatform | ★★★☆☆ | The VSAM browse pattern is the hardest part; replatform preserves it but doesn't modernize it. |
| Refactor | ★★★★☆ | Good for COCRDSLC (read-only). COCRDLIC's browse pattern needs architectural change regardless. |
| Rewrite | ★★☆☆☆ | Card activation/deactivation rules in COCRDUPC are subtle — rewrite risks missing edge cases. |

**Recommended Strategy: Strangler Fig**
- Replace VSAM browse with JPA `Pageable` pagination (eliminates STARTBR/READNEXT complexity)
- Card view is a simple JPA `findByCardNum` — migrate first
- Card update requires careful mapping of HANDLE ABEND → try-catch with rollback
- PCI considerations: add card number masking in the Java layer

---

### 2.5 Transaction Management (Online)

| Attribute | Detail |
|-----------|--------|
| **Programs** | COTRN00C (list, 699 LOC), COTRN01C (view, 330 LOC), COTRN02C (add, 783 LOC) |
| **LOC** | 1,812 combined |
| **Current State** | COTRN02C has 26 EVALUATE blocks (highest of any program). Calls CSUTLDTC for date validation. Writes to TRANSACT VSAM. COTRN00C browses with STARTBR. |
| **Data** | TRANSACT.VSAM.KSDS (350 bytes) |
| **Complexity** | Moderate (list/view), High (add) |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Refactor** | **★★★★★** | **Recommended.** Well-defined CRUD operations. The 26 EVALUATE blocks map to Java switch/enum patterns. Date validation replaces cleanly with java.time. |
| Strangler Fig | ★★★★☆ | Also viable, but the module is self-contained enough for a clean refactor. |
| Replatform | ★★★☆☆ | Preserves the complex EVALUATE logic without simplifying it. |
| Rewrite | ★★★☆☆ | Unnecessary — the logic is translatable. |

**Recommended Strategy: Refactor**
- Map EVALUATE blocks to enum-based state handling
- Replace CSUTLDTC date calls with `java.time.LocalDate` validation
- Transaction add → REST POST endpoint with JSR-380 bean validation
- VSAM browse → Spring Data JPA with `Pageable`

---

### 2.6 Bill Payment

| Attribute | Detail |
|-----------|--------|
| **Programs** | COBIL00C (572 LOC) |
| **LOC** | 572 |
| **Current State** | Performs a dual-write: creates payment transaction in TRANSACT AND updates balance in ACCTDATA. 18 EVALUATE blocks. Browses CARDXREF for card lookup. |
| **Data** | TRANSACT (write), ACCTDATA (read/rewrite), CARDXREF (browse) |
| **Complexity** | High — the dual-write is the most transactionally sensitive online operation |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Refactor** | **★★★★★** | **Recommended.** The dual-write maps perfectly to a single `@Transactional` method. Well-scoped, moderate LOC. |
| Strangler Fig | ★★★★☆ | Could work, but the dual-write makes parallel running complex (which system of record?). |
| Replatform | ★★☆☆☆ | Preserves the CICS pseudo-conversational pattern — misses the opportunity to use real ACID transactions. |
| Rewrite | ★★★☆☆ | The logic is clear enough to refactor; rewrite gains little. |

**Recommended Strategy: Refactor**
- Wrap transaction creation + balance update in a single `@Transactional` method
- Add optimistic locking (`@Version`) on Account entity to prevent lost updates
- Implement idempotency key to prevent duplicate payments
- Consider event sourcing: payment event → balance projection

---

### 2.7 Transaction Posting (Batch)

| Attribute | Detail |
|-----------|--------|
| **Programs** | CBTRN02C (731 LOC) |
| **LOC** | 731 |
| **Current State** | The financial heart of batch processing. Reads DALYTRAN, cross-references against XREFFILE, validates, posts to TRANSACT, updates TCATBALF, writes rejections to DALYREJS. 61 PERFORMs, 48 IF statements. |
| **Data** | Reads 4 files, writes 2. Central to nightly batch cycle. |
| **Complexity** | Very High — multi-file coordination with financial implications |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Refactor** | **★★★★★** | **Recommended.** The batch pattern maps directly to Spring Batch with ItemReader/ItemProcessor/ItemWriter. |
| Strangler Fig | ★★★☆☆ | Batch jobs are harder to strangle — they run in isolation, not behind a facade. |
| Replatform | ★★★★☆ | Viable interim step if migration timeline is compressed. |
| Rewrite | ★★☆☆☆ | Too risky — rejection logic and cross-reference validation encode critical business rules. |

**Recommended Strategy: Refactor**
- Spring Batch chunk-oriented step: `FlatFileItemReader` → `TransactionProcessor` → `JpaItemWriter`
- Rejection handling → Spring Batch skip/retry policies + dead-letter output
- Reconciliation job to compare Java output vs. COBOL output during parallel run
- Must preserve exact COBOL arithmetic (use `BigDecimal` everywhere)

---

### 2.8 Interest Calculation (Batch)

| Attribute | Detail |
|-----------|--------|
| **Programs** | CBACT04C (652 LOC) |
| **LOC** | 652 |
| **Current State** | Multi-table lookup: TCATBALF (category balances) + DISCGRP (interest rates per group/type/category) + ACCTDATA + XREFFILE. Generates interest transactions written to TRANSACT. 56 PERFORMs, 43 IF statements. |
| **Complexity** | High — financial precision is legally regulated |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Refactor** | **★★★★★** | **Recommended.** The calculation logic is valuable and must be preserved exactly. Line-by-line refactor ensures fidelity. |
| Strangler Fig | ★★☆☆☆ | Batch — no facade opportunity. |
| Replatform | ★★★★☆ | Good interim option — preserves exact COBOL arithmetic automatically. |
| Rewrite | ★☆☆☆☆ | Extremely risky. Interest calculation errors = regulatory liability. |

**Recommended Strategy: Refactor**
- `BigDecimal` with explicit `RoundingMode` matching COBOL COMP-3 behavior
- Create `InterestCalculationService` with parameterized rate lookups
- Parallel run: execute both COBOL and Java for N cycles, compare penny-for-penny
- Regulatory sign-off required before cutover

---

### 2.9 Statement Generation (Batch)

| Attribute | Detail |
|-----------|--------|
| **Programs** | CBSTM03A (924 LOC), CBSTM03B (230 LOC) |
| **LOC** | 1,154 combined |
| **Current State** | Main driver (CBSTM03A) calls I/O subroutine (CBSTM03B) 13 times. Reads 4 VSAM files. Produces dual output: text statement + HTML statement. Multi-step JCL: SORT → IDCAMS → CBSTM03A. |
| **Complexity** | High — dual output, subroutine coupling, multi-step JCL |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Refactor** | **★★★★☆** | Merge A+B into single service. Replace text/HTML with template engine. |
| Strangler Fig | ★★☆☆☆ | Batch — no facade. |
| Replatform | ★★★☆☆ | Preserves the two-program structure unnecessarily. |
| **Rewrite** | **★★★★★** | **Recommended.** Statement formatting is presentation logic. Modern template engines (Thymeleaf, JasperReports) produce far better output. |

**Recommended Strategy: Rewrite**
- Replace text+HTML dual output with a template engine producing HTML → PDF
- Merge CBSTM03A + CBSTM03B into a single `StatementGenerationService`
- Spring Batch job with chunk processing per account
- Output to cloud storage (S3) instead of sequential files

---

### 2.10 Transaction Reporting (Batch)

| Attribute | Detail |
|-----------|--------|
| **Programs** | CBTRN03C (649 LOC), CORPT00C (649 LOC — online report request) |
| **LOC** | 1,298 combined |
| **Current State** | CBTRN03C reads 5 files — most of any program. 72 PERFORMs. Control-break report logic. CORPT00C submits JCL via TDQ/INTRDR — unique CICS-to-batch bridge with no Java equivalent. |
| **Complexity** | High — multi-file joins, control breaks, TDQ/INTRDR pattern |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **★★★★★** | **Recommended.** The report formatting and the TDQ/INTRDR submission pattern both need complete architectural redesign. |
| Refactor | ★★★☆☆ | The 5-file join translates to SQL JOINs, but the report formatting and job submission need redesign regardless. |
| Strangler Fig | ★★★☆☆ | The online report request (CORPT00C) could use a strangler facade. |
| Replatform | ★★☆☆☆ | Preserves the TDQ/INTRDR anti-pattern. |

**Recommended Strategy: Rewrite**
- Replace 5-file procedural joins with SQL/JPA query
- Replace control-break logic with JasperReports or Spring Batch report writer
- Replace TDQ/INTRDR with Spring Batch `JobLauncher` (async job submission)
- REST endpoint for report request → async job → notification on completion

---

### 2.11 User Administration (Admin)

| Attribute | Detail |
|-----------|--------|
| **Programs** | COUSR00C (list, 695 LOC), COUSR01C (add, 299 LOC), COUSR02C (update, 414 LOC), COUSR03C (delete, 359 LOC) |
| **LOC** | 1,767 combined |
| **Current State** | Standard CRUD on USRSEC VSAM. Admin-only access. Browse + Add/Update/Delete pattern. |
| **Complexity** | Low-Moderate — straightforward CRUD with VSAM browse |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ★★★☆☆ | Overhead of facade not justified for simple CRUD. |
| Replatform | ★★☆☆☆ | Preserves weak security model. |
| Refactor | ★★★★☆ | Clean translation possible, but the data model changes significantly (password hashing, roles). |
| **Rewrite** | **★★★★★** | **Recommended.** The user model needs fundamental security upgrades. Simple enough to rebuild cleanly. |

**Recommended Strategy: Rewrite**
- Spring Data JPA CRUD repository + Spring Security admin endpoints
- Add password hashing, account lockout, audit logging
- Role model expansion beyond binary Admin/Regular
- Admin UI with Spring MVC or React admin panel

---

### 2.12 Data Export/Import

| Attribute | Detail |
|-----------|--------|
| **Programs** | CBEXPORT (582 LOC), CBIMPORT (487 LOC) |
| **LOC** | 1,069 combined |
| **Current State** | CBEXPORT reads all 5 VSAM files and writes a single tagged sequential file. CBIMPORT reverses the process with error handling. EVALUATE-based record type routing in CBIMPORT. |
| **Complexity** | Moderate — multi-file I/O but straightforward logic |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | ★★☆☆☆ | Utility programs — no user-facing facade. |
| Replatform | ★★★☆☆ | Could auto-convert, but the tagged-record format is obsolete. |
| Refactor | ★★★★☆ | The logic is clear and translatable. |
| **Rewrite** | **★★★★★** | **Recommended.** Replace tagged sequential format with JSON/CSV. Modern format enables integration with other systems. |

**Recommended Strategy: Rewrite**
- Export: JPA queries → JSON/CSV file (or direct database dump)
- Import: JSON/CSV parser → JPA batch insert with validation
- Spring Batch for large-volume processing
- Consider REST API endpoints for programmatic export/import

---

### 2.13 Shared Utilities

| Attribute | Detail |
|-----------|--------|
| **Programs** | CSUTLDTC (date utility, 157 LOC), COBSWAIT (wait, 41 LOC) |
| **Copybooks** | CSSTRPFY (string utility), CSLKPCDY (lookup codes, 1,318 LOC), CSDAT01Y, CSUTLDPY/CSUTLDWY |
| **Complexity** | Low individually, but high impact — used across many programs |

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **★★★★★** | **Recommended.** Java has native equivalents for all of these (java.time, String utilities, enum lookup tables). No value in preserving COBOL wrappers. |

**Recommended Strategy: Rewrite**
- CSUTLDTC → `java.time.LocalDate` / `DateTimeFormatter`
- CSSTRPFY → Apache Commons `StringUtils` or Java `String` methods
- CSLKPCDY → Java `enum` types or database reference tables
- COBSWAIT → `Thread.sleep()` or `ScheduledExecutorService`

---

### 2.14 Optional Modules

#### Authorization (IMS/DB2/MQ) — 8 programs, ~4,345 LOC

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **★★★★★** | **Recommended.** IMS hierarchical DB + MQ + DB2 combo involves three legacy technologies. The authorization business logic is valuable, but the infrastructure is entirely replaced by Spring + JPA + RabbitMQ/SQS. |

#### Transaction Type DB2 — 3 programs, ~4,037 LOC

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Refactor** | **★★★★★** | **Recommended.** Already uses SQL (DB2). Embedded SQL → JPA/JDBC is a well-understood migration path. COTRTLIC (2,098 LOC) is the main complexity. |

#### VSAM-MQ — 2 programs, ~1,144 LOC

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **★★★★★** | **Recommended.** Replace MQ request/response with REST APIs. Account inquiry and date service become trivial Spring endpoints. |

---

## 3. Strategy Summary Matrix

| Functional Area | Strategy | LOC | Risk | Priority |
|----------------|----------|-----|------|----------|
| Security & Auth | Rewrite | 260 | Low | 1 — First |
| Navigation/Menu | Strangler Fig | 596 | Low | 2 — Early |
| User Admin | Rewrite | 1,767 | Low | 3 — Early |
| Shared Utilities | Rewrite | 1,516 | Low | 4 — Foundation |
| Transaction Mgmt (Online) | Refactor | 1,812 | Medium | 5 |
| Card Management | Strangler Fig | 3,906 | Medium | 6 |
| Account Management | Strangler Fig | 5,177 | High | 7 |
| Bill Payment | Refactor | 572 | High | 8 |
| Transaction Posting (Batch) | Refactor | 731 | Very High | 9 |
| Interest Calculation (Batch) | Refactor | 652 | Very High | 10 |
| Statement Generation | Rewrite | 1,154 | Medium | 11 |
| Transaction Reporting | Rewrite | 1,298 | Medium | 12 |
| Data Export/Import | Rewrite | 1,069 | Low | 13 |
| Optional: Auth (IMS/DB2/MQ) | Rewrite | 4,345 | High | 14 |
| Optional: Tran Type DB2 | Refactor | 4,037 | Medium | 15 |
| Optional: VSAM-MQ | Rewrite | 1,144 | Low | 16 |

**Strategy Distribution:**
- Strangler Fig: 3 areas (9,679 LOC) — Account, Card, Navigation
- Refactor: 5 areas (7,804 LOC) — Transactions, Bill Payment, Batch Posting, Interest, Tran Type DB2
- Rewrite: 8 areas (11,553 LOC) — Security, User Admin, Utilities, Statements, Reports, Export/Import, Auth Module, VSAM-MQ
- Replatform: 0 areas recommended (available as fallback for batch if timeline is compressed)
