# CardDemo Hotspot Report — Top 10 Modernization Priorities

> **Generated**: 2026-03-25 | **Methodology**: Weighted scoring across Complexity, Risk, and Business Impact dimensions.

---

## Scoring Methodology

Each module is scored on three dimensions (1–10 scale):

| Dimension | Weight | Criteria |
|-----------|--------|----------|
| **Complexity** | 35% | LOC, cyclomatic complexity (EVALUATE/IF nesting), number of PERFORM paragraphs, copybook count, CICS command diversity |
| **Risk** | 35% | File contention (shared VSAM writes), data integrity exposure (REWRITE operations), PII handling, error-handling gaps, security concerns |
| **Business Impact** | 30% | Revenue criticality, user-facing frequency, downstream dependencies, batch cycle position |

**Composite Score** = (Complexity × 0.35) + (Risk × 0.35) + (Business Impact × 0.30)

---

## Top 10 Hotspot Modules

### #1 — COACTUPC (Account Update) — Score: 9.15

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **10** | 4,236 LOC — largest program in the codebase. 64 PERFORMs, 10 EVALUATEs, 39 COPY statements (including 32 CSSETATY REPLACING macros). Complex field-level validation with date parsing, attribute manipulation, and multi-file reads. |
| Risk | **9** | REWRITE operations on both ACCTDATA and CUSTDATA VSAM files. Reads CARDDATA via alternate index. Any bug here corrupts account master and customer records. Handles sensitive financial data (balances, credit limits, dates). |
| Business Impact | **8** | Core account maintenance function. Every account modification flows through this program. Directly affects credit limits, balances, and customer data. |

**Modernization Notes**:
- Refactor into Account Update Service with separate validation, persistence, and UI layers
- The 32 CSSETATY COPY REPLACING blocks should become a single attribute-setting utility method
- Split screen-handling logic from business rules (currently interleaved)
- Field-level validation logic maps well to Bean Validation annotations in Java

---

### #2 — CBTRN02C (Transaction Posting Engine) — Score: 8.60

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **8** | 731 LOC, 61 PERFORMs. Reads 2 input files, writes 2 output files, updates 2 files in I-O mode. Complex posting logic with cross-reference lookups, account balance updates, and category balance maintenance. |
| Risk | **10** | **Highest risk program in the system**. Updates ACCTDATA (balances) and TCATBALF (category balances) while writing to TRANSACT master. A bug here causes financial discrepancies across the entire ledger. Processes every daily transaction — no room for error. Rejected transactions go to DALYREJS but error handling is minimal. |
| Business Impact | **9** | The heart of the batch cycle. If this fails, no transactions post, statements are wrong, and interest calculations cascade errors. Every dollar flows through CBTRN02C. |

**Modernization Notes**:
- Highest-priority candidate for comprehensive unit testing before any migration
- Should become a Spring Batch step with chunk-oriented processing and automatic rollback
- Add transaction-level commit points (currently processes entire file or nothing)
- Implement proper dead-letter queue for rejects instead of flat file

---

### #3 — COCRDUPC (Card Update) — Score: 8.00

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **9** | 1,560 LOC, 26 PERFORMs, 16 EVALUATEs (highest EVALUATE count). Heavy validation logic for card status changes, expiration dates, embossed name. Multiple CICS READ/REWRITE patterns with alternate index access. |
| Risk | **8** | REWRITE on CARDDATA VSAM. Card number is PCI-DSS scope — any exposure or corruption is a compliance violation. Card status changes directly affect transaction authorization. |
| Business Impact | **7** | Card lifecycle management — activations, status changes, renewals. Lower frequency than account operations but critical for card issuance workflows. |

**Modernization Notes**:
- PCI-DSS compliance must be built into the modernized service from day one
- Card number should be tokenized; CVV should never be stored (currently stored in CVACT02Y)
- 16 EVALUATE blocks suggest complex state machine — document all card status transitions before migrating

---

### #4 — CBACT04C (Interest Calculation) — Score: 7.95

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **8** | 652 LOC, 56 PERFORMs. Reads 3 reference files (TCATBALF, XREFFILE, DISCGRP), writes interest transactions, updates account balances. Multi-level nested processing: for each account → for each category → lookup rate → calculate → post. |
| Risk | **9** | Directly modifies account balances (REWRITE ACCTDATA). Interest calculation errors compound over time and are difficult to detect. Writes synthetic transactions to TRANSACT that are indistinguishable from real ones. |
| Business Impact | **7** | Revenue-generating function — interest charges are a primary income source. Regulatory requirement to calculate correctly. Errors here trigger customer disputes and potential regulatory action. |

**Modernization Notes**:
- Needs comprehensive audit trail (currently no logging of calculation details)
- Interest rate lookup from DISCGRP should become a configurable rules engine
- Must implement idempotency — re-running should not double-charge interest
- Add reconciliation step to verify balance changes match posted transactions

---

### #5 — COCRDLIC (Card List) — Score: 7.50

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **8** | 1,459 LOC, 34 PERFORMs, 18 EVALUATEs. Complex browse logic with STARTBR/READNEXT/READPREV for forward and backward paging. Role-based filtering (admin sees all, regular user sees own cards only). Three XCTL targets. |
| Risk | **7** | Read-only VSAM access (low data corruption risk). However, browse cursor management is error-prone — incorrect ENDBR can leave cursors open and lock files. Role-based access check is critical for security. |
| Business Impact | **7** | Primary card discovery screen — gateway to card detail and update. High user interaction frequency. If this breaks, users cannot manage their cards at all. |

**Modernization Notes**:
- Browse/paging logic maps to paginated REST API with cursor-based pagination
- Role-based filtering should leverage Spring Security rather than inline checks
- The three XCTL targets (list, view, update) suggest a card management microservice boundary

---

### #6 — CBSTM03A (Statement Generation) — Score: 7.45

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **9** | 924 LOC, 29 PERFORMs, 5 EVALUATEs. Generates both plain text AND HTML output simultaneously. Uses ALTER verb (deprecated, hard to trace). Calls CBSTM03B subroutine 13 times for file I/O. Complex formatting with headers, addresses, transaction details, and totals. |
| Risk | **6** | Read-only from source files (low corruption risk). Output-only to STMTFILE/HTMLFILE. Risk is primarily in correctness — wrong statements cause customer complaints. ALTER verb makes control flow unpredictable. |
| Business Impact | **8** | Customer-facing output — statements are the primary communication touchpoint. Regulatory requirement to produce accurate statements. HTML generation suggests early attempt at digital delivery. |

**Modernization Notes**:
- **ALTER verb must be eliminated** — this is the only program using it, and it's untraceable in static analysis
- Dual output (text + HTML) should become a template engine (Thymeleaf/FreeMarker)
- CBSTM03B subroutine coupling is tight — merge into single service or use proper dependency injection
- Statement generation is a strong candidate for event-driven architecture (generate on demand)

---

### #7 — COACTVWC (Account View) — Score: 7.15

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **7** | 941 LOC, 21 PERFORMs, 10 EVALUATEs. Reads from 3 VSAM files (CARDDATA, ACCTDATA, CUSTDATA) plus cross-reference. Complex screen population with multiple data sources. |
| Risk | **7** | Read-only operations (lower risk than update programs). However, it reads sensitive data (account balances, customer details, card numbers) and displays them on screen — information disclosure risk if role checks fail. |
| Business Impact | **7** | Most frequently used screen after login. Gateway to account update (COACTUPC). If view is broken, users cannot see their account information. |

**Modernization Notes**:
- Natural REST GET endpoint: `GET /accounts/{id}`
- Currently joins data from 3 files in application code — in Java, this becomes a JPA query or service composition
- Consider read-through cache since this is the highest-traffic read path

---

### #8 — COTRN02C (Transaction Add) — Score: 7.00

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **7** | 783 LOC, 61 PERFORMs, 13 EVALUATEs. Date validation via CSUTLDTC calls. Reads card cross-reference and account data for validation. Generates transaction ID using STARTBR/READPREV pattern (find max ID). |
| Risk | **8** | WRITE to TRANSACT VSAM (creates new financial records). Account balance not updated here (deferred to batch) — creates temporal inconsistency. Transaction ID generation via READPREV is not atomic — race condition risk under concurrent access. |
| Business Impact | **6** | Online transaction entry. Used for manual transaction creation (less frequent than batch posting). Important for adjustments and corrections. |

**Modernization Notes**:
- Transaction ID generation must be replaced with proper sequence/UUID — current READPREV approach has concurrency issues
- Consider whether online transaction add should immediately update balances (real-time) vs. current deferred approach
- Date validation logic (CSUTLDTC calls) should use Java's date/time API

---

### #9 — CBTRN03C (Transaction Report) — Score: 6.80

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **7** | 649 LOC, 72 PERFORMs (highest PERFORM count relative to LOC — very procedural). 4 EVALUATEs. Reads 6 input files. Complex break logic for page/account/grand totals. Multiple reference file lookups per transaction. |
| Risk | **5** | Read-only from all inputs, write-only to report file. Low data corruption risk. Primary risk is report accuracy — incorrect totals or missing transactions. |
| Business Impact | **8** | Regulatory and audit reporting. Daily transaction report is a compliance requirement. Downstream consumers (auditors, management) depend on accuracy. |

**Modernization Notes**:
- 72 PERFORMs in 649 LOC means extremely granular paragraph structure — good candidate for method extraction
- Break logic (page/account/grand totals) maps well to JasperReports or similar reporting framework
- Consider whether this should remain a batch report or become an on-demand API with export capability
- 6 input files suggest this should be a database view/query in the modernized system

---

### #10 — COSGN00C (Signon) — Score: 6.65

| Dimension | Score | Rationale |
|-----------|-------|-----------|
| Complexity | **4** | 260 LOC, 11 PERFORMs, 3 EVALUATEs. Relatively simple program. Reads USRSEC file, validates credentials, determines user type, XCTLs to appropriate menu. |
| Risk | **9** | **Security gateway for the entire application**. Passwords stored and compared in plaintext (CSUSR01Y). No lockout after failed attempts. No session timeout management. CICS ASSIGN USERID used for terminal identification. Single point of failure — if compromised, all functions are exposed. |
| Business Impact | **7** | Every user session starts here. Authentication failure = total application unavailability. User type determination (Admin vs. Regular) controls all downstream access. |

**Modernization Notes**:
- **CRITICAL**: Must implement proper password hashing (bcrypt/scrypt) — current plaintext storage is unacceptable
- Add failed-login lockout, session management, and MFA support
- Replace with Spring Security or OAuth2/OIDC integration
- This should be the FIRST program modernized — security posture of the entire application depends on it
- Consider separating authentication from authorization (currently combined)

---

## Priority Matrix

```
                    HIGH BUSINESS IMPACT
                         │
         ┌───────────────┼───────────────┐
         │   QUADRANT 2  │  QUADRANT 1   │
         │  (Modernize   │  (Modernize   │
         │   carefully)  │   FIRST)      │
         │               │               │
         │  CBSTM03A (#6)│  COACTUPC (#1)│
         │  CBTRN03C (#9)│  CBTRN02C (#2)│
         │               │  CBACT04C (#4)│
         │               │  COSGN00C(#10)│
HIGH     │               │               │     LOW
COMPLEXITY├───────────────┼───────────────┤  COMPLEXITY
         │   QUADRANT 3  │  QUADRANT 4   │
         │  (Refactor    │  (Quick wins) │
         │   or rewrite) │               │
         │               │               │
         │  COCRDUPC (#3)│  COACTVWC (#7)│
         │  COCRDLIC (#5)│  COTRN02C (#8)│
         │               │               │
         └───────────────┼───────────────┘
                         │
                    LOW BUSINESS IMPACT
```

---

## Recommended Modernization Waves

### Wave 1 — Security & Foundation (Weeks 1–4)
| Program | Action | Rationale |
|---------|--------|-----------|
| COSGN00C | Replace with Spring Security / OAuth2 | Security foundation for everything else |
| CSUSR01Y | Redesign with password hashing | PII/security compliance |
| COUSR00-03C | Migrate to User Management Service | Admin function, enables IAM |

### Wave 2 — Core Financial Engine (Weeks 5–12)
| Program | Action | Rationale |
|---------|--------|-----------|
| CBTRN02C | Migrate to Spring Batch with proper tx management | Highest-risk batch program |
| CBACT04C | Migrate to Spring Batch with audit trail | Revenue-critical calculation |
| COTRN02C | Migrate to Transaction Service (REST) | Online transaction creation |
| CVTRA05Y/06Y | Design JPA entities, database schema | Foundation for all transaction programs |

### Wave 3 — Account & Card Management (Weeks 13–20)
| Program | Action | Rationale |
|---------|--------|-----------|
| COACTUPC | Decompose into Account Update Service | Largest, most complex program |
| COACTVWC | Migrate to Account View API | High-traffic read path |
| COCRDUPC | Migrate to Card Management Service | PCI-DSS compliance required |
| COCRDLIC/SLC | Migrate to Card Query APIs | Card discovery and detail |

### Wave 4 — Reporting & Statements (Weeks 21–28)
| Program | Action | Rationale |
|---------|--------|-----------|
| CBSTM03A/B | Replace with template-based statement engine | Customer-facing output |
| CBTRN03C | Replace with reporting framework | Regulatory compliance |
| CORPT00C | Migrate online report trigger | Online-batch bridge |

### Wave 5 — Data Migration & Utilities (Weeks 29–32)
| Program | Action | Rationale |
|---------|--------|-----------|
| CBEXPORT/CBIMPORT | Migrate to ETL pipeline | Data migration tooling |
| CBACT01-03C, CBCUS01C | Migrate to data extract utilities | Operational support |
| COBSWAIT, CSUTLDTC | Replace with Java equivalents | Utility functions |
| COMEN01C, COADM01C | Migrate to web UI navigation | Menu/navigation layer |

---

## Key Risk Mitigations

1. **Plaintext Passwords** (COSGN00C + CSUSR01Y) — Immediate remediation required regardless of migration timeline
2. **PCI-DSS Violations** (CVACT02Y stores CVV) — Must not persist CVV in modernized system
3. **PII Exposure** (CVCUS01Y SSN field) — Encrypt at rest, mask in transit
4. **No Audit Trail** (CBACT04C interest calc) — Add before migration to enable reconciliation
5. **ALTER Verb** (CBSTM03A) — Eliminate before attempting automated code conversion
6. **Race Condition** (COTRN02C transaction ID) — Replace with database sequence immediately
7. **Batch/Online Contention** (ACCTDATA, TRANSACT) — Current CLOSEFIL/OPENFIL pattern must be replaced with proper locking or event-driven architecture
