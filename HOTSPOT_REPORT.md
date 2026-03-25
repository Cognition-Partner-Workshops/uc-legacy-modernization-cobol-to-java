# CardDemo Hotspot Report — Top 10 Modernization Priorities

> **Generated:** 2026-03-25 | **Methodology:** Weighted scoring across code complexity, data risk, business impact, and coupling
> **Scope:** All 44 COBOL programs in the CardDemo application

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Rankings](#top-10-hotspot-rankings)
3. [Detailed Hotspot Analysis](#detailed-hotspot-analysis)
4. [Risk Heat Map](#risk-heat-map)
5. [Recommended Migration Waves](#recommended-migration-waves)
6. [Key Observations](#key-observations)

---

## Scoring Methodology

Each module is scored on four dimensions (1-10 scale), then weighted to produce a composite priority score.

| Dimension | Weight | What It Measures |
|---|---|---|
| **Code Complexity** | 30% | Lines of code, control flow density (PERFORM/EVALUATE/IF per LOC), number of COPY inclusions, REDEFINES/88-level conditions |
| **Data Risk** | 25% | Number of VSAM files accessed, read/write operations, PII exposure, data integrity implications |
| **Business Impact** | 25% | Criticality to core business operations, user-facing visibility, financial calculation involvement |
| **Coupling** | 20% | Number of programs that call/are called by this module, copybook fan-out, COMMAREA dependencies |

**Composite Score** = (Complexity x 0.30) + (Data Risk x 0.25) + (Business Impact x 0.25) + (Coupling x 0.20)

---

## Top 10 Hotspot Rankings

| Rank | Program | LOC | Complexity | Data Risk | Biz Impact | Coupling | **Score** | Domain |
|---|---|---|---|---|---|---|---|---|
| **1** | **COACTUPC** | 4,237 | 10 | 9 | 9 | 8 | **9.10** | Account Update |
| **2** | **CBTRN02C** | 731 | 8 | 10 | 10 | 7 | **8.80** | Transaction Posting |
| **3** | **CBACT04C** | 652 | 8 | 9 | 10 | 6 | **8.35** | Interest Calculation |
| **4** | **CBSTM03A** | 924 | 8 | 8 | 8 | 7 | **7.80** | Statement Generation |
| **5** | **COCRDLIC** | 1,460 | 9 | 6 | 7 | 8 | **7.50** | Card List |
| **6** | **COCRDUPC** | 1,560 | 9 | 7 | 7 | 6 | **7.40** | Card Update |
| **7** | **COSGN00C** | 261 | 4 | 7 | 10 | 9 | **7.30** | Sign-on / Auth |
| **8** | **COTRN02C** | 783 | 7 | 7 | 8 | 6 | **7.10** | Transaction Add |
| **9** | **CBTRN03C** | 649 | 7 | 7 | 7 | 5 | **6.60** | Transaction Report |
| **10** | **COMEN01C** | 309 | 4 | 3 | 9 | 10 | **6.30** | Main Menu / Router |

---

## Detailed Hotspot Analysis

### #1 — COACTUPC (Account Update) — Score: 9.10

**Location:** `app/cbl/COACTUPC.cbl` (4,237 lines)

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 4,237 | **Largest program in the codebase by 2.7x** |
| Control Flow Density | 269 statements (PERFORM/EVALUATE/IF) | Extremely high branching |
| VSAM Files Accessed | 3 (ACCTDAT R/W, CUSTDAT R/W, CXACAIX R) | Multi-file transactional writes |
| Copybooks Included | 14 (COCOM01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVCRD01Y, CSUTLDWY, etc.) | Highest copybook fan-out |
| PII Exposure | SSN, DOB, Phone, Address (via CVCUS01Y) | **High regulatory risk** |
| Validation Logic | Phone, SSN, date, numeric, alpha-only, yes/no edits | Complex inline validation |

**Why It's #1:**
- At 4,237 lines, this is the single largest and most complex program — nearly 3x the next largest.
- It performs multi-file REWRITE operations across accounts AND customers in a single transaction, creating data integrity risk.
- Contains deeply nested validation logic for SSN (parts 1/2/3 with specific invalid ranges like 666, 900-999), US phone numbers, dates (with leap year checks), and financial fields.
- Uses extensive REDEFINES and 88-level conditions (100+ condition names) for field-level validation.
- Any bug in this program can corrupt account balances and customer PII.

**Migration Recommendation:**
- Decompose into separate Account Update Service and Customer Update Service.
- Extract validation logic into reusable validator classes.
- Implement database transactions (ACID) to replace VSAM multi-file writes.
- PII fields require encryption at rest in the target platform.

---

### #2 — CBTRN02C (Transaction Posting) — Score: 8.80

**Location:** `app/cbl/CBTRN02C.cbl` (731 lines)

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 731 | Medium-large batch program |
| Control Flow Density | 154 statements | High for a batch program |
| VSAM Files Accessed | 6 (DALYTRAN R, TRANSACT W, XREFFILE R, DALYREJS W, ACCTFILE R/W, TCATBALF R/W) | **Highest file fan-out in codebase** |
| Business Logic | Cross-reference validation, balance updates, reject handling | Core financial processing |

**Why It's #2:**
- This is the **financial heart** of the batch cycle. Every daily transaction flows through this program.
- Reads daily transactions (DALYTRAN), validates card numbers against cross-reference (XREFFILE), posts valid transactions to the master file (TRANSACT), updates account balances (ACCTFILE), updates category balances (TCATBALF), and writes rejects (DALYREJS).
- Touches 6 different VSAM files in a single execution — the highest file I/O fan-out.
- A defect here means incorrect account balances, lost transactions, or undetected fraud.
- No explicit error recovery/restart logic — if it fails mid-run, manual intervention is required.

**Migration Recommendation:**
- Convert to a Spring Batch job with chunk-based processing and restart capability.
- Implement database transactions for atomicity across account and category balance updates.
- Add comprehensive audit logging for every posted/rejected transaction.
- Build idempotent processing to allow safe re-runs.

---

### #3 — CBACT04C (Interest Calculation) — Score: 8.35

**Location:** `app/cbl/CBACT04C.cbl` (652 lines)

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 652 | Medium batch program |
| Control Flow Density | 142 statements | High complexity |
| VSAM Files Accessed | 5 (ACCTDAT R/W, DISCGRP R, TRANTYPE R, TRANCATG R, TCATBALF R/W) | High file fan-out |
| Financial Logic | Interest rate lookup, compound calculation, balance updates | **Core financial calculation** |

**Why It's #3:**
- Implements the **interest calculation engine** — a core financial function with regulatory implications.
- Looks up disclosure group rates (DISCGRP), cross-references transaction types and categories, then applies interest to account balances.
- Updates both account master (ACCTDAT) and category balances (TCATBALF) with calculated interest.
- Rate calculation logic is embedded in procedural COBOL — difficult to audit or unit test.
- Any error directly impacts customer billing statements and reported balances.

**Migration Recommendation:**
- Extract interest calculation into a dedicated, unit-testable service with clear inputs/outputs.
- Externalize rate tables into a configuration database (replacing DISCGRP VSAM).
- Implement decimal arithmetic with explicit rounding rules (COBOL's implicit decimal handling must be preserved exactly).
- Add audit trail for every interest charge applied.

---

### #4 — CBSTM03A (Statement Generation) — Score: 7.80

**Location:** `app/cbl/CBSTM03A.CBL` (924 lines)

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 924 | Large batch program |
| Control Flow Density | 53 statements + 12 CALL sites to CBSTM03B | Distributed complexity |
| VSAM Files Accessed | 4 (TRNXFILE R, XREFFILE R, CUSTFILE R, ACCTFILE R) + 2 output (STMTFILE W, HTMLFILE W) | Multi-file reads |
| Sub-Program Calls | 12 calls to CBSTM03B | **Highest inter-program coupling** |
| Output | Generates both text and HTML statements | Dual-format output |

**Why It's #4:**
- Generates customer-facing statements — the primary customer communication artifact.
- Uses ALTER statement to dynamically change program flow at runtime (lines 300-309), which is a deprecated and notoriously difficult-to-trace COBOL feature.
- Delegates all file I/O to CBSTM03B via 12 different CALL sites with a shared work area, creating tight coupling.
- Reads across 4 different VSAM files to assemble complete statement data.
- Produces both plain-text and HTML output formats.

**Migration Recommendation:**
- Replace ALTER-based flow control with explicit conditionals or strategy pattern.
- Merge CBSTM03A and CBSTM03B into a single statement service (the separation is an artifact of COBOL-era memory constraints).
- Use a template engine (Thymeleaf, FreeMarker) for statement formatting.
- Consider generating PDF statements directly.

---

### #5 — COCRDLIC (Credit Card List) — Score: 7.50

**Location:** `app/cbl/COCRDLIC.cbl` (1,460 lines)

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 1,460 | Third-largest online program |
| Control Flow Density | 192 statements | Very high |
| VSAM Files Accessed | 2 (CARDDAT R, CARDAIX R) | Moderate |
| Screen Interactions | Pagination (PF7/PF8), row selection (S/U), filtering | Complex UI logic |
| Navigation | XCTL to COCRDSLC (view) and COCRDUPC (update) | Hub program |

**Why It's #5:**
- Implements paginated list display with forward/backward scrolling — complex screen state management.
- Serves as a navigation hub: users select cards for viewing or updating, transferring to COCRDSLC or COCRDUPC.
- Contains 7-row screen array processing with row-level action codes (S=Select, U=Update).
- Maintains page state across CICS pseudo-conversational boundaries via COMMAREA.
- First/last page tracking, next-page indicators, and browse cursor management.

**Migration Recommendation:**
- Convert to a paginated REST API endpoint with standard pagination parameters.
- Screen state management becomes server-side session or client-side state.
- The row-selection pattern maps to a standard list-with-actions UI component.

---

### #6 — COCRDUPC (Credit Card Update) — Score: 7.40

**Location:** `app/cbl/COCRDUPC.cbl` (1,560 lines)

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 1,560 | Second-largest online program |
| Control Flow Density | 202 statements | Very high |
| VSAM Files Accessed | 3 (CARDDAT R/W, CARDAIX R, CUSTDAT R) | Write operations on card data |
| Validation | Card number, CVV, expiration date, embossed name, status | Complex field validation |

**Why It's #6:**
- Second-largest online program with extensive field validation similar to COACTUPC.
- Performs REWRITE on CARDDAT — any bug can corrupt card records.
- Contains date validation for card expiration with year/month/day component checks.
- Reads customer data for display context but doesn't modify it (lower risk than COACTUPC).
- Uses CSSTRPFY for PF-key remapping — non-trivial control flow.

**Migration Recommendation:**
- Extract into a Card Update REST service with DTO validation.
- Reuse validation components from the Account Update decomposition.
- Implement optimistic locking to replace VSAM record-level locks.

---

### #7 — COSGN00C (Sign-on) — Score: 7.30

**Location:** `app/cbl/COSGN00C.cbl` (261 lines)

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 261 | Small program |
| Control Flow Density | 31 statements | Low |
| VSAM Files Accessed | 1 (USRSEC R) | Single file |
| Security | Plain-text password comparison, no encryption | **Critical security gap** |
| Coupling | Entry point for ALL application access; XCTL to COMEN01C or COADM01C | **Highest upstream coupling** |

**Why It's #7:**
- Despite its small size, this is the **single entry point** for the entire application — every user session starts here.
- Passwords are stored and compared in **plain text** (line 223: `IF SEC-USR-PWD = WS-USER-PWD`).
- No session management, no login attempt limiting, no account lockout.
- Determines admin vs. regular user routing — a role-based access control decision point.
- A security vulnerability here compromises the entire application.

**Migration Recommendation:**
- Replace with proper authentication framework (Spring Security, OAuth2/OIDC).
- Implement password hashing (bcrypt/scrypt), session management, CSRF protection.
- Add login attempt limiting and account lockout.
- This should be one of the **first modules migrated** to eliminate the security gap.

---

### #8 — COTRN02C (Transaction Add) — Score: 7.10

**Location:** `app/cbl/COTRN02C.cbl` (783 lines)

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 783 | Medium-large online program |
| Control Flow Density | 112 statements | Moderate-high |
| VSAM Files Accessed | 4 (ACCTDAT R, CARDXREF R, CXACAIX R, TRANSACT R/W) | Multi-file with writes |
| External Calls | CSUTLDTC (date validation) x 2 | Utility dependency |
| Business Logic | Transaction creation with card/account validation | Core business function |

**Why It's #8:**
- The primary online entry point for creating new transactions.
- Validates card numbers against cross-reference, verifies account exists, then writes to TRANSACT.
- Calls CSUTLDTC date validation utility twice (for start/end dates).
- Creates financial records — any bug results in incorrect transaction postings.

**Migration Recommendation:**
- Convert to a Transaction Creation REST endpoint with request validation.
- Implement proper transaction isolation for the VSAM write.
- Add idempotency keys to prevent duplicate transaction creation.

---

### #9 — CBTRN03C (Daily Transaction Report) — Score: 6.60

**Location:** `app/cbl/CBTRN03C.cbl` (649 lines)

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 649 | Medium batch program |
| Control Flow Density | 151 statements | High for its size |
| VSAM Files Accessed | 4 (DALYTRAN R, TRANSACT R, XREFFILE R, TRANTYPE R, TRANCATG R) | High read fan-out |
| Report Logic | Page breaks, subtotals, grand totals, date-range filtering | Complex report formatting |

**Why It's #9:**
- Generates the Daily Transaction Report — a key operational and audit artifact.
- Reads across 5 files to assemble report data with type and category descriptions.
- Contains complex report formatting: page headers, account subtotals, page totals, grand totals.
- Uses CVTRA07Y report structures for formatted output.
- Report accuracy is critical for reconciliation and audit compliance.

**Migration Recommendation:**
- Replace with a reporting service using JasperReports or similar.
- Implement the report as a database query with aggregation rather than procedural file reads.
- Consider generating reports in multiple formats (PDF, CSV, HTML).

---

### #10 — COMEN01C (Main Menu / Router) — Score: 6.30

**Location:** `app/cbl/COMEN01C.cbl` (309 lines)

| Metric | Value | Assessment |
|---|---|---|
| Lines of Code | 309 | Small program |
| Control Flow Density | 33 statements | Low |
| VSAM Files Accessed | 0 | None (pure navigation) |
| Coupling | Dispatches to ALL user-facing programs via menu table | **Highest downstream coupling** |
| Menu Definition | Driven by COMEN02Y copybook (data-driven menu) | Configurable |

**Why It's #10:**
- The central **routing hub** for all regular-user functionality.
- Uses a data-driven menu system (COMEN02Y defines option names, program names, and access levels).
- Performs EXEC CICS INQUIRE to check if target programs are installed before transfer.
- Has graceful handling for uninstalled optional modules ("not installed" message).
- Role-based filtering: blocks regular users from admin-only menu options.

**Migration Recommendation:**
- Replace with a navigation controller / API gateway routing layer.
- The menu-option table pattern maps well to a configuration-driven route registry.
- Role-based access becomes middleware/filter-based authorization.

---

## Risk Heat Map

Visual summary of risk distribution across the application domains.

```
                    Low Risk    Medium Risk    High Risk    Critical
                    (1-3)       (4-6)          (7-8)        (9-10)
                    ─────────── ────────────── ──────────── ──────────
Security           │           │              │            │ COSGN00C │
                   │           │              │            │(plain pwd)│
                    ─────────── ────────────── ──────────── ──────────
Account Mgmt       │           │              │ COACTVWC   │ COACTUPC │
                   │           │              │            │(4237 LOC)│
                    ─────────── ────────────── ──────────── ──────────
Card Mgmt          │           │              │ COCRDLIC   │          │
                   │           │              │ COCRDUPC   │          │
                    ─────────── ────────────── ──────────── ──────────
Transaction Mgmt   │           │              │ COTRN02C   │ CBTRN02C │
                   │           │              │ CBTRN03C   │(6 files) │
                    ─────────── ────────────── ──────────── ──────────
Financial Calc     │           │              │            │ CBACT04C │
                   │           │              │            │(interest)│
                    ─────────── ────────────── ──────────── ──────────
Reporting          │           │              │ CBSTM03A   │          │
                   │           │              │(ALTER stmt)│          │
                    ─────────── ────────────── ──────────── ──────────
Navigation         │           │ COMEN01C     │            │          │
                   │           │ COADM01C     │            │          │
                    ─────────── ────────────── ──────────── ──────────
User Admin         │ COUSR01C  │ COUSR00C     │            │          │
                   │ COUSR03C  │ COUSR02C     │            │          │
                    ─────────── ────────────── ──────────── ──────────
Batch Utilities    │ COBSWAIT  │ CBEXPORT     │            │          │
                   │ CBACT01-03│ CBIMPORT     │            │          │
                    ─────────── ────────────── ──────────── ──────────
```

---

## Recommended Migration Waves

Based on the hotspot analysis, risk profiles, and dependency constraints, here is a recommended phased migration approach:

### Wave 1 — Foundation & Security (Weeks 1-4)

| Program | Rationale |
|---|---|
| **COSGN00C** | Eliminate plain-text password vulnerability immediately |
| **COCOM01Y** | COMMAREA → shared session/context model (enables all other migrations) |
| **CSUSR01Y** | User entity with proper authentication model |
| **COMEN01C / COADM01C** | Navigation framework → API gateway / controller layer |

**Deliverable:** Secure authentication, session management, and routing framework.

### Wave 2 — Core Business Logic (Weeks 5-10)

| Program | Rationale |
|---|---|
| **CBTRN02C** | Highest business risk — transaction posting engine |
| **CBACT04C** | Interest calculation — financial accuracy critical |
| **COTRN02C** | Online transaction creation — user-facing financial writes |
| **COBIL00C** | Bill payment — customer financial operations |

**Deliverable:** Core financial processing pipeline migrated with full test coverage.

### Wave 3 — Account & Card Management (Weeks 11-16)

| Program | Rationale |
|---|---|
| **COACTUPC** | Largest, most complex program — needs dedicated effort |
| **COACTVWC** | Account view — simpler companion to COACTUPC |
| **COCRDUPC** | Card update — second-largest online program |
| **COCRDLIC / COCRDSLC** | Card list and detail — navigation pair |

**Deliverable:** Full account and card management CRUD operations.

### Wave 4 — Reporting & Statements (Weeks 17-20)

| Program | Rationale |
|---|---|
| **CBSTM03A / CBSTM03B** | Statement generation — can use new data layer |
| **CBTRN03C** | Transaction reporting |
| **CORPT00C** | Report request UI |
| **COTRN00C / COTRN01C** | Transaction list/view |

**Deliverable:** Reporting and statement generation on modern platform.

### Wave 5 — Admin, Utilities & Cleanup (Weeks 21-24)

| Program | Rationale |
|---|---|
| **COUSR00-03C** | User CRUD (admin) |
| **CBEXPORT / CBIMPORT** | Data utilities |
| **CBACT01-03C, CBCUS01C, CBTRN01C** | File listing/print utilities |
| **CSUTLDTC, COBSWAIT** | Shared utilities |

**Deliverable:** Complete application migration. Decommission mainframe.

---

## Key Observations

### Critical Findings

1. **COACTUPC is 2.7x larger than any other program** (4,237 lines vs. next-largest COTRTLIC at 2,098). It should be decomposed during migration rather than converted 1:1.

2. **Plain-text passwords** in COSGN00C/CSUSR01Y represent an immediate security risk that should be the first migration target regardless of other priorities.

3. **CBTRN02C touches 6 VSAM files** in a single batch run with no transactional integrity guarantees. A failure mid-run leaves data in an inconsistent state.

4. **CBSTM03A uses the ALTER statement** (lines 300-309) — a deprecated COBOL feature that dynamically changes program control flow at runtime. This is a well-known anti-pattern that makes the program extremely difficult to analyze and test.

5. **No error recovery/restart** in batch programs. If POSTTRAN fails after processing 50% of transactions, there is no checkpoint/restart mechanism.

### Complexity Distribution

| Complexity Tier | Programs | % of Total LOC |
|---|---|---|
| Very High (>1000 LOC) | COACTUPC, COCRDUPC, COCRDLIC, COTRTLIC, COTRTUPC, COPAUS0C, COPAUA0C | 40% |
| High (500-999 LOC) | COACTVWC, CBSTM03A, COCRDSLC, COTRN02C, CBTRN02C, COTRN00C, COUSR00C, CBACT04C, CORPT00C, CBTRN03C, COACCT01, CBEXPORT, COBIL00C, COPAUS1C, CODATE01 | 42% |
| Medium (200-499 LOC) | CBTRN01C, CBIMPORT, CBACT01C, COUSR02C, COUSR03C, CBPAUP0C, PAUDBLOD, DBUNLDGS, COTRN01C, COMEN01C, COUSR01C, COADM01C, COSGN00C, PAUDBUNL, COPAUS2C, COBTUPDT, CBSTM03B | 15% |
| Low (<200 LOC) | CBCUS01C, CBACT02C, CBACT03C, CSUTLDTC, COBSWAIT | 3% |

### Data Sensitivity Assessment

| Sensitivity Level | Data Elements | Programs Handling |
|---|---|---|
| **PII — Critical** | SSN, DOB, Government ID | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC |
| **PII — High** | Name, Address, Phone | COACTUPC, COACTVWC, COCRDSLC, COCRDUPC, CBSTM03A |
| **Financial — Critical** | Account Balance, Credit Limit, Interest Rate | COACTUPC, CBTRN02C, CBACT04C, COBIL00C |
| **Financial — High** | Transaction Amounts, Card Numbers | COTRN02C, CBTRN02C, CBTRN03C, CBSTM03A |
| **Security — Critical** | User Password (plain text) | COSGN00C, COUSR01C, COUSR02C |
