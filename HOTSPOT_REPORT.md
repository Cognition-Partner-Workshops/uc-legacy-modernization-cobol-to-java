# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo
> **Purpose:** Identify the top 10 modules by complexity, migration risk, and business impact to prioritize modernization effort

---

## Scoring Methodology

Each module is scored on three dimensions (1–10 scale):

| Dimension          | Weight | Criteria                                                                                      |
|--------------------|:------:|-----------------------------------------------------------------------------------------------|
| **Complexity**     |  40%   | Lines of code, cyclomatic indicators (IF/EVALUATE/PERFORM density), number of VSAM files accessed, copybook count, cross-program calls |
| **Risk**           |  30%   | Financial data writes, multi-file transactions, error handling gaps, security sensitivity, tight coupling to platform (CICS, VSAM, assembler) |
| **Business Impact**|  30%   | Revenue criticality, user-facing frequency, downstream dependencies (other programs/jobs that depend on this module's output) |

**Composite Score** = (Complexity × 0.40) + (Risk × 0.30) + (Business Impact × 0.30)

---

## Top 10 Hotspot Modules

### Summary Table

| Rank | Module     | LOC   | Type   | Domain                 | Complexity | Risk | Business Impact | **Composite** |
|-----:|------------|------:|--------|------------------------|:----------:|:----:|:---------------:|:-------------:|
|    1 | COACTUPC   | 4,236 | Online | Account Update         |    10      |  10  |       9         |  **9.7**      |
|    2 | CBTRN02C   |   731 | Batch  | Transaction Posting    |     8      |  10  |      10         |  **9.2**      |
|    3 | COCRDUPC   | 1,560 | Online | Card Update            |     9      |   9  |       8         |  **8.7**      |
|    4 | CBACT04C   |   652 | Batch  | Interest Calculation   |     7      |   9  |       9         |  **8.2**      |
|    5 | COCRDLIC   | 1,459 | Online | Card List              |     8      |   7  |       8         |  **7.7**      |
|    6 | CBSTM03A   |   924 | Batch  | Statement Generation   |     8      |   8  |       7         |  **7.7**      |
|    7 | COTRN02C   |   783 | Online | Transaction Add        |     7      |   8  |       8         |  **7.6**      |
|    8 | CBTRN03C   |   649 | Batch  | Transaction Report     |     7      |   6  |       7         |  **6.7**      |
|    9 | COTRN00C   |   699 | Online | Transaction List       |     6      |   6  |       8         |  **6.6**      |
|   10 | COACTVWC   |   941 | Online | Account View           |     6      |   7  |       7         |  **6.6**      |

---

## Detailed Module Analysis

### 1. COACTUPC — Account Update (Composite: 9.7)

**Why #1:** The largest and most complex program in the entire codebase. Handles account master updates with cascading writes to customer and account files, validates dozens of fields using hard-coded lookup tables (1,318-line CSLKPCDY), and manages multi-step CICS conversational state.

| Metric                  | Value                                          |
|-------------------------|------------------------------------------------|
| Lines of Code           | 4,236 (20.5% of entire codebase)               |
| IF Statements           | 168 (highest in codebase)                      |
| EVALUATE Statements     | 20                                             |
| PERFORM Statements      | 64                                             |
| Paragraphs              | 88                                             |
| VSAM Files Accessed     | 3 (ACCTDAT, CUSTDAT, CARDXREF) — Read + Update |
| Copybooks Included      | 8 (including CSLKPCDY, CVCRD01Y, CSUTLDWY)    |
| BMS Map                 | COACTUP                                        |

**Key Risks:**
- Direct REWRITE to ACCTDAT and CUSTDAT (financial data mutation)
- Embeds state/country validation in 1,318-line lookup copybook — fragile and hard to test
- Multi-step conversational flow with complex PF-key handling
- No explicit transaction rollback — partial updates possible on CICS ABEND

**Migration Recommendations:**
- Decompose into AccountUpdateService + AccountValidator + LookupService
- Replace CSLKPCDY hard-coded lookups with database reference tables
- Wrap updates in a database transaction with proper rollback
- Extract field validation into Jakarta Bean Validation annotations

---

### 2. CBTRN02C — Transaction Posting (Composite: 9.2)

**Why #2:** The core batch posting engine that moves daily transactions into the master ledger. Touches the most VSAM files of any single program and performs financial balance updates.

| Metric                  | Value                                          |
|-------------------------|------------------------------------------------|
| Lines of Code           | 731                                            |
| IF Statements           | 48                                             |
| EVALUATE Statements     | 0                                              |
| PERFORM Statements      | 61                                             |
| VSAM Files Read         | DALYTRAN, CARDXREF, ACCTDAT, TRANSACT, TCATBAL |
| VSAM Files Written      | TRANSACT, ACCTDAT, TCATBAL, DALYREJS           |
| JCL Invocation          | POSTTRAN.jcl                                   |

**Key Risks:**
- Writes to 4 VSAM files in a single batch run — no two-phase commit
- Failed posting leaves inconsistent state across files
- Rejected transactions go to DALYREJS but no alerting mechanism
- Central to the nightly batch chain — failure blocks downstream jobs

**Migration Recommendations:**
- Implement as a Spring Batch job with chunk-oriented processing
- Use database transactions to ensure atomic posting across tables
- Add retry/skip policies and dead-letter queue for rejected transactions
- Implement monitoring and alerting for posting failures

---

### 3. COCRDUPC — Card Update (Composite: 8.7)

**Why #3:** Second-largest online program. Manages credit card record updates with multi-step confirmation flow and complex field validation.

| Metric                  | Value                                          |
|-------------------------|------------------------------------------------|
| Lines of Code           | 1,560                                          |
| IF Statements           | 74                                             |
| EVALUATE Statements     | 16                                             |
| PERFORM Statements      | 26                                             |
| VSAM Files Accessed     | CARDDAT (Read + REWRITE)                       |
| BMS Map                 | COCRDUP                                        |

**Key Risks:**
- Direct REWRITE to card master file (sensitive PCI data: card number, CVV, expiration)
- Multi-step confirmation flow vulnerable to state inconsistency
- PCI-DSS compliance: card data in working storage, no masking

**Migration Recommendations:**
- Encrypt card data at rest and in transit (PCI-DSS)
- Implement optimistic locking to prevent concurrent update conflicts
- Separate card data handling into a PCI-scoped microservice boundary
- Add audit logging for all card data modifications

---

### 4. CBACT04C — Interest Calculation (Composite: 8.2)

**Why #4:** Financial calculation engine with direct account balance mutations. High business impact despite moderate code size.

| Metric                  | Value                                          |
|-------------------------|------------------------------------------------|
| Lines of Code           | 652                                            |
| IF Statements           | 43                                             |
| PERFORM Statements      | 56                                             |
| Sections                | 4                                              |
| Files Read              | ACCTDAT, DISCGRP                               |
| Files Written           | ACCTDAT (balance update with interest)         |
| JCL Invocation          | INTCALC.jcl (PARM='2022071800')                |

**Key Risks:**
- **Highest financial risk:** Directly modifies account balances with interest charges
- Parameterized date via JCL PARM — incorrect date causes wrong calculations
- No audit trail for interest postings
- Rounding logic must exactly match legacy behavior during migration

**Migration Recommendations:**
- Use `BigDecimal` with explicit `RoundingMode` matching COBOL `COMP-3` arithmetic
- Implement comprehensive audit logging for every interest calculation
- Add reconciliation checks comparing old vs. new calculation results
- Run in parallel with legacy during cutover (shadow mode)

---

### 5. COCRDLIC — Card List (Composite: 7.7)

**Why #5:** Complex browsing logic with forward/backward pagination over VSAM BROWSE operations and drill-down navigation to view/update screens.

| Metric                  | Value                                          |
|-------------------------|------------------------------------------------|
| Lines of Code           | 1,459                                          |
| IF Statements           | 61                                             |
| EVALUATE Statements     | 18                                             |
| PERFORM Statements      | 34                                             |
| VSAM Files Accessed     | CARDDAT (STARTBR, READNEXT, READPREV, ENDBR)  |
| XCTL Targets            | COMEN01C, COCRDSLC, COCRDUPC                   |
| BMS Map                 | COCRDLI                                        |

**Key Risks:**
- Complex VSAM browse with manual cursor management — edge cases in pagination
- Multiple XCTL exits to different programs based on user selection
- Card numbers visible on list screen (PCI masking needed)

**Migration Recommendations:**
- Replace VSAM browse with paginated JPA query (Spring Data Page<Card>)
- Mask card numbers in list view (show last 4 digits only)
- Implement as a single REST endpoint with pagination parameters

---

### 6. CBSTM03A — Statement Generation (Composite: 7.7)

**Why #6:** The most architecturally complex batch program. Generates dual-format output (plain text + HTML), calls a subroutine (CBSTM03B) for all file I/O, and uses mainframe-specific control block addressing.

| Metric                  | Value                                          |
|-------------------------|------------------------------------------------|
| Lines of Code           | 924 (+ 230 for CBSTM03B = 1,154 combined)     |
| CALL Statements         | 12 (all to CBSTM03B)                           |
| EVALUATE Statements     | 9                                              |
| PERFORM Statements      | 29                                             |
| Sections                | 4                                              |
| Files Read              | Sorted transactions (seq), ACCTDAT             |
| Files Written           | Text statement, HTML statement                 |

**Key Risks:**
- Tight coupling between CBSTM03A and CBSTM03B via shared working storage area
- Mainframe control block addressing (`ADDRESS OF` patterns) — non-portable
- Dual-format output logic interleaved — hard to maintain independently
- Called `CEE3ABD` for abend — needs proper error handling

**Migration Recommendations:**
- Implement as a Spring Batch Tasklet with separate text and HTML template engines
- Replace CBSTM03B file I/O with Spring Resource/BufferedWriter
- Use Thymeleaf or similar for HTML statement templates
- Add proper exception handling with Spring Batch's skip/retry policies

---

### 7. COTRN02C — Transaction Add (Composite: 7.6)

**Why #7:** Creates new transaction records with cross-file lookups, date validation via external program call, and multi-file writes.

| Metric                  | Value                                          |
|-------------------------|------------------------------------------------|
| Lines of Code           | 783                                            |
| EVALUATE Statements     | 26 (highest EVALUATE density)                  |
| PERFORM Statements      | 61                                             |
| CALL Statements         | 2 (CSUTLDTC for date validation)               |
| VSAM Files Accessed     | TRANSACT (R/W), CARDXREF, CXACAIX              |
| BMS Map                 | COTRN02                                        |

**Key Risks:**
- Writes to TRANSACT file — core financial data
- External CALL to CSUTLDTC for date validation — adds a dependency
- Card-to-account lookup via cross-reference — integrity depends on XREF consistency

**Migration Recommendations:**
- Implement as a transactional REST endpoint (POST /transactions)
- Use JPA cascading with foreign key constraints for referential integrity
- Replace CSUTLDTC with Java `LocalDate` validation
- Add idempotency keys to prevent duplicate transaction creation

---

### 8. CBTRN03C — Transaction Report (Composite: 6.7)

**Why #8:** Batch reporting program with multi-file joins and formatted output generation. Complex report layout logic.

| Metric                  | Value                                          |
|-------------------------|------------------------------------------------|
| Lines of Code           | 649                                            |
| EVALUATE Statements     | 4                                              |
| IF Statements           | 38                                             |
| PERFORM Statements      | 72 (highest PERFORM density)                   |
| Files Read              | TRANSACT (seq), TRANTYPE, TRANCATG             |
| Files Written           | Report output (text)                           |
| JCL Invocation          | TRANREPT.jcl (via REPROC procedure)            |

**Key Risks:**
- Joins 3 reference files — data inconsistency causes report errors
- Report formatting tightly coupled to 132-column print layout
- Uses CVTRA07Y for complex report header/detail/total structures

**Migration Recommendations:**
- Implement as a Spring Batch job with JasperReports or similar
- Replace fixed-column formatting with PDF/HTML templates
- Pre-join reference data via SQL queries instead of manual file merging

---

### 9. COTRN00C — Transaction List (Composite: 6.6)

**Why #9:** High user-traffic list screen with VSAM browse pagination, the primary entry point for transaction inquiries.

| Metric                  | Value                                          |
|-------------------------|------------------------------------------------|
| Lines of Code           | 699                                            |
| EVALUATE Statements     | 16                                             |
| IF Statements           | 26                                             |
| PERFORM Statements      | 43                                             |
| Paragraphs              | 16                                             |
| VSAM Files Accessed     | TRANSACT (STARTBR, READNEXT, READPREV, ENDBR) |
| BMS Map                 | COTRN00                                        |

**Key Risks:**
- VSAM browse pagination logic with manual cursor — edge cases on empty results or boundary conditions
- High frequency of use — performance-sensitive
- Navigates to COTRN01C (view) or other programs via dynamic XCTL

**Migration Recommendations:**
- Replace with paginated JPA query (Spring Data `Pageable`)
- Add search/filter capabilities not available in legacy 3270 interface
- Implement as GET /transactions with query parameters

---

### 10. COACTVWC — Account View (Composite: 6.6)

**Why #10:** Read-only but touches 3 VSAM files and performs cross-file joins. High user traffic as the primary account inquiry screen.

| Metric                  | Value                                          |
|-------------------------|------------------------------------------------|
| Lines of Code           | 941                                            |
| EVALUATE Statements     | 10                                             |
| IF Statements           | 29                                             |
| PERFORM Statements      | 21                                             |
| VSAM Files Accessed     | ACCTDAT, CUSTDAT, CARDXREF                     |
| BMS Map                 | COACTVW                                        |

**Key Risks:**
- Multi-file join logic in COBOL — must preserve exact data assembly logic
- Displays sensitive customer PII (SSN, address) — needs access controls
- Navigation target from multiple programs — heavily depended upon

**Migration Recommendations:**
- Implement as GET /accounts/{id} REST endpoint with DTO projection
- Add field-level access control for PII fields
- Create a reusable AccountDetailDTO used across view/update screens

---

## Migration Priority Matrix

```
                    HIGH BUSINESS IMPACT
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                  │
         │  CBACT04C (4)   │  CBTRN02C (2)   │
         │  Interest Calc  │  Tran Posting    │
         │                 │                  │
         │  COTRN02C (7)   │  COACTUPC (1)    │
         │  Tran Add       │  Account Update  │
         │                 │                  │
LOW ─────┼─────────────────┼──────────────────┼───── HIGH
COMPLEXITY│                │                  │    COMPLEXITY
         │  COTRN00C (9)   │  COCRDLIC (5)    │
         │  Tran List      │  Card List       │
         │                 │                  │
         │  COACTVWC (10)  │  COCRDUPC (3)    │
         │  Account View   │  Card Update     │
         │                 │                  │
         │  CBTRN03C (8)   │  CBSTM03A (6)    │
         │  Tran Report    │  Statement Gen   │
         │                 │                  │
         └─────────────────┼─────────────────┘
                           │
                    LOW BUSINESS IMPACT
```

---

## Recommended Migration Waves

### Wave 1 — Read-Only Screens (Low Risk, High Confidence)
Quick wins that prove the migration approach without touching financial data.

| Module   | Effort | Risk  | Rationale                                    |
|----------|:------:|:-----:|----------------------------------------------|
| COACTVWC | Medium | Low   | Read-only, builds account view DTO           |
| COTRN00C | Medium | Low   | Read-only, establishes pagination pattern    |
| COTRN01C | Low    | Low   | Simple single-record read                    |
| COCRDLIC | Medium | Low   | Establishes card list + browse pattern       |
| COCRDSLC | Low    | Low   | Simple single-record read                    |

### Wave 2 — Update Screens (Medium Risk)
Builds on Wave 1 DTOs, adds write operations with proper transactions.

| Module   | Effort | Risk   | Rationale                                    |
|----------|:------:|:------:|----------------------------------------------|
| COACTUPC | High   | High   | Largest program; needs decomposition first   |
| COCRDUPC | High   | Medium | PCI-scoped card updates                      |
| COTRN02C | Medium | Medium | Transaction creation with cross-file lookups |
| COBIL00C | Medium | Medium | Payment processing with balance updates      |

### Wave 3 — Batch Processing (Highest Risk, Highest Impact)
Requires Spring Batch framework and equivalence testing.

| Module   | Effort | Risk   | Rationale                                    |
|----------|:------:|:------:|----------------------------------------------|
| CBTRN02C | High   | High   | Core posting engine — needs shadow testing   |
| CBACT04C | High   | Critical| Interest calculation — financial precision   |
| CBSTM03A | High   | Medium | Statement generation — dual output formats   |
| CBTRN03C | Medium | Medium | Report generation — format translation       |

### Wave 4 — Admin & Security
| Module   | Effort | Risk  | Rationale                                    |
|----------|:------:|:-----:|----------------------------------------------|
| COSGN00C | Low    | Low   | Replace with Spring Security / JWT           |
| COUSR00C–03C | Medium | Low | Standard CRUD — map to REST API          |
| COADM01C | Low    | Low   | Menu → React admin dashboard                 |
| COMEN01C | Low    | Low   | Menu → React main navigation                 |

---

## Key Metrics Summary

| Metric                          | Value                      |
|---------------------------------|----------------------------|
| Total Core COBOL LOC            | 20,650                     |
| Top 10 Hotspot LOC              | 12,634 (61.2% of total)    |
| Top 3 Hotspots LOC              | 6,527 (31.6% of total)     |
| Programs with financial writes  | 5 (COACTUPC, CBTRN02C, CBACT04C, COTRN02C, COBIL00C) |
| Programs with PCI-sensitive data| 3 (COCRDUPC, COCRDLIC, COCRDSLC)                      |
| Max cyclomatic complexity proxy | COACTUPC (168 IFs + 20 EVALUATEs)                     |
| Most-connected program          | CBTRN02C (reads 5 files, writes 4)                    |
| Largest single dependency       | CSLKPCDY (1,318 LOC lookup table, used only by COACTUPC) |
