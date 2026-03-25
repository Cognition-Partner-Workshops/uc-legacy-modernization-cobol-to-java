# CardDemo Hotspot Report

> **Generated**: 2026-03-25 | **Application**: AWS CardDemo — Mainframe Credit Card Management System
>
> This report identifies the **top 10 modules** prioritized by code complexity, migration risk,
> and business impact to guide modernization sequencing and resource allocation.

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Ranking](#top-10-hotspot-ranking)
3. [Detailed Module Assessments](#detailed-module-assessments)
4. [Risk Heat Map](#risk-heat-map)
5. [Recommended Migration Order](#recommended-migration-order)
6. [Key Risk Factors Summary](#key-risk-factors-summary)

---

## Scoring Methodology

Each module is scored on three dimensions (1–10 scale each):

| Dimension         | Weight | Criteria                                                                                  |
|-------------------|--------|-------------------------------------------------------------------------------------------|
| **Complexity**    | 35%    | Lines of code, cyclomatic complexity (IF/EVALUATE count), CICS commands, file I/O, CALLs  |
| **Risk**          | 35%    | Data mutation (REWRITE/DELETE), multi-file coordination, PII handling, error paths, ABEND  |
| **Business Impact** | 30%  | Revenue criticality, user-facing, regulatory exposure, batch cycle dependency, data volume |

**Composite Score** = (Complexity × 0.35) + (Risk × 0.35) + (Business Impact × 0.30)

### Complexity Metrics Used

| Metric              | Low (1-3)        | Medium (4-6)       | High (7-10)        |
|---------------------|------------------|--------------------|--------------------|
| Lines of Code       | < 300            | 300–700            | > 700              |
| IF Statements       | < 10             | 10–50              | > 50               |
| EVALUATE Statements | < 3              | 3–10               | > 10               |
| PERFORM Statements  | < 15             | 15–30              | > 30               |
| CICS Commands       | < 5              | 5–12               | > 12               |
| File Operations     | 0–1 files        | 2–3 files          | 4+ files           |

---

## Top 10 Hotspot Ranking

| Rank | Program    | LOC   | Complexity | Risk | Biz Impact | **Composite** | Domain             | Type   |
|------|------------|------:|-----------:|-----:|-----------:|---------------:|--------------------|--------|
| 1    | COACTUPC   | 4,236 |       10   |  10  |        9   |       **9.7**  | Account Mgmt       | Online |
| 2    | CBTRN02C   |   731 |        8   |   9  |       10   |       **9.0**  | Transaction Posting| Batch  |
| 3    | COCRDUPC   | 1,560 |        9   |   8  |        8   |       **8.4**  | Card Mgmt          | Online |
| 4    | COCRDLIC   | 1,459 |        8   |   6  |        8   |       **7.3**  | Card Mgmt          | Online |
| 5    | CBACT04C   |   652 |        7   |   8  |        9   |       **7.9**  | Interest Calc      | Batch  |
| 6    | CBSTM03A   |   924 |        8   |   6  |        8   |       **7.3**  | Statement Gen      | Batch  |
| 7    | COACTVWC   |   941 |        7   |   6  |        8   |       **6.9**  | Account View       | Online |
| 8    | COTRN02C   |   783 |        7   |   7  |        7   |       **7.0**  | Transaction Add    | Online |
| 9    | CBTRN03C   |   649 |        6   |   5  |        7   |       **6.0**  | Transaction Report | Batch  |
|10    | COTRN00C   |   699 |        6   |   5  |        7   |       **6.0**  | Transaction List   | Online |

---

## Detailed Module Assessments

### #1 — COACTUPC (Account Update) — Score: 9.7

| Metric               | Value                     | Assessment       |
|----------------------|---------------------------|------------------|
| Lines of Code        | 4,236                     | **Highest in codebase** — 2.7× larger than next |
| IF Statements        | 360                       | Extremely high branching |
| EVALUATE Statements  | 31                        | Complex state machine |
| PERFORM Statements   | 63                        | Deep call nesting |
| CICS Commands        | 18+ (READ, REWRITE, SEND, RECEIVE, XCTL, HANDLE ABEND) | Full CICS stack |
| Files Accessed       | ACCTDATA (R/RW), CUSTDATA (R/RW), CARDXREF (R) | Multi-file mutations |
| Copybooks Used       | 14 (COCOM01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CSSTRPFY, ...) | Heavy data coupling |

**Why it's #1**:
- **Complexity**: The largest program in the entire codebase by a significant margin. Contains field-level validation for every account attribute, complex edit/confirmation screen flow, and multi-step update logic with rollback consideration.
- **Risk**: Performs REWRITE operations on both Account and Customer VSAM files in a single transaction. An error mid-update could leave data inconsistent. Uses HANDLE ABEND for crash recovery.
- **Business Impact**: Account updates directly affect credit limits, balances, and customer data — core financial records. Any bug could result in incorrect balances or regulatory violations.
- **Migration Concern**: Must be decomposed into multiple Java service methods. The 4,236-line monolith should map to separate AccountService and CustomerService classes with proper transaction boundaries.

---

### #2 — CBTRN02C (Transaction Posting) — Score: 9.0

| Metric               | Value                     | Assessment       |
|----------------------|---------------------------|------------------|
| Lines of Code        | 731                       | Medium-high |
| IF Statements        | 53                        | High branching |
| EVALUATE Statements  | 6                         | Multiple state checks |
| PERFORM Statements   | 30                        | Complex flow |
| Files Accessed       | 6 files (DALYTRAN, TRANSACT, XREFFILE, DALYREJS, ACCTFILE, TCATBALF) | **Most files of any batch program** |
| Operations           | READ, WRITE, REWRITE on multiple files | Multi-file mutation |

**Why it's #2**:
- **Complexity**: Coordinates 6 different files — reads daily transactions, validates via cross-reference, posts to transaction master, updates account balances and category balances, writes rejections.
- **Risk**: **Financial mutation hub** — simultaneously updates account balances (REWRITE ACCTFILE) and category balances (REWRITE/WRITE TCATBALF). A failure partway through creates balance inconsistencies that are extremely difficult to reconcile.
- **Business Impact**: This is the **core batch engine** — every daily transaction flows through it. Downtime or errors directly affect all account balances and transaction records. Must maintain exact decimal precision for financial calculations.
- **Migration Concern**: Requires careful Spring Batch step design with proper chunk-based commit points. Must implement compensating transactions for failure scenarios.

---

### #3 — COCRDUPC (Card Update) — Score: 8.4

| Metric               | Value                     | Assessment       |
|----------------------|---------------------------|------------------|
| Lines of Code        | 1,560                     | High |
| IF Statements        | 148                       | Very high branching |
| EVALUATE Statements  | 16                        | Complex state handling |
| PERFORM Statements   | 26                        | Moderate nesting |
| CICS Commands        | 12 (READ, REWRITE, SEND, RECEIVE, XCTL, HANDLE ABEND) | Full CICS stack |
| Files Accessed       | CARDDATA (R/RW), CARDXREF (R), CUSTDATA (R) | Card data mutation |

**Why it's #3**:
- **Complexity**: Second-largest online program. Extensive field validation for card attributes, edit/display/confirm workflow, and complex screen-state management across multiple map sends/receives.
- **Risk**: REWRITE on card master file. Uses HANDLE ABEND for recovery. Reads from 3 different VSAM files for cross-validation.
- **Business Impact**: Card updates affect active card status, expiration dates, and embossed names — directly impacts cardholders' ability to transact.
- **Migration Concern**: Complex BMS screen flow must map to multi-step web form with client-side and server-side validation.

---

### #4 — COCRDLIC (Card List) — Score: 7.3

| Metric               | Value                     | Assessment       |
|----------------------|---------------------------|------------------|
| Lines of Code        | 1,459                     | High |
| IF Statements        | 122                       | Very high branching |
| EVALUATE Statements  | 18                        | Complex state handling |
| PERFORM Statements   | 34                        | Deep iteration |
| CICS Commands        | 18 (STARTBR, READNEXT, READPREV, ENDBR, SEND, RECEIVE, XCTL) | Browsing pattern |
| Files Accessed       | CARDDATA (Browse), USRSEC (R), CVACT02Y | Scrollable list with auth |

**Why it's #4**:
- **Complexity**: Implements forward/backward paging through VSAM KSDS using STARTBR/READNEXT/READPREV/ENDBR pattern. Complex page-state management including "fresh start" vs. "continuation" logic. Inline CSSTRPFY string formatting.
- **Risk**: Browse operations must handle end-of-file, empty results, and concurrent updates gracefully. Multiple EVALUATE blocks for key-press handling (PF7/PF8 paging, selection, navigation).
- **Business Impact**: Primary card lookup screen — used by every user session that involves card operations.
- **Migration Concern**: VSAM browse pattern → paginated JPA query with cursor-based pagination. PF-key navigation → REST API pagination with offset/limit or keyset pagination.

---

### #5 — CBACT04C (Interest Calculation) — Score: 7.9

| Metric               | Value                     | Assessment       |
|----------------------|---------------------------|------------------|
| Lines of Code        | 652                       | Medium-high |
| IF Statements        | 36                        | Moderate branching |
| EVALUATE Statements  | 4                         | Some state logic |
| PERFORM Statements   | 28                        | Iterative processing |
| Files Accessed       | 5 files (TCATBALF, XREFFILE, DISCGRP, ACCTFILE, TRANSACT) | Multi-file reads + writes |
| Operations           | READ, REWRITE (ACCTFILE), WRITE (TRANSACT) | Financial mutations |

**Why it's #5**:
- **Complexity**: Iterates through category balances, looks up interest rates from disclosure groups (joining on account group + transaction type + category), calculates interest, and creates interest transaction records.
- **Risk**: **Financial calculation engine** — any precision error in interest calculation directly affects account balances. Joins across 3 reference files. Creates new transaction records that permanently affect the ledger. REWRITE to account master updates the running balance.
- **Business Impact**: Interest revenue is a core business metric. Regulatory compliance requires exact calculation methods. Errors compound over time and are difficult to reverse.
- **Migration Concern**: Must preserve exact COBOL decimal arithmetic behavior (S9(09)V99). Java BigDecimal with proper rounding mode (HALF_EVEN for banking). Needs thorough parallel-run testing against COBOL output.

---

### #6 — CBSTM03A (Statement Generation — Main) — Score: 7.3

| Metric               | Value                     | Assessment       |
|----------------------|---------------------------|------------------|
| Lines of Code        | 924                       | High |
| IF Statements        | 15                        | Moderate |
| EVALUATE Statements  | 5                         | Some branching |
| PERFORM Statements   | 29                        | Iterative processing |
| CALL Statements      | 14 (all to CBSTM03B)     | **Highest call count** |
| Files Accessed       | STMTFILE (W), HTMLFILE (W), + via CBSTM03B: TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE |
| Special Features     | ALTER statement, dual output (text + HTML) | Uncommon COBOL feature |

**Why it's #6**:
- **Complexity**: Generates statements in **two formats simultaneously** (plain text and HTML). Uses COBOL ALTER statement (a rare, hard-to-trace flow-control mechanism). Heavy use of CALL to CBSTM03B subroutine for all file I/O. Complex report page-break and totaling logic.
- **Risk**: ALTER statement modifies paragraph destinations at runtime — extremely difficult to trace statically and a known anti-pattern. Must coordinate with subroutine for file open/read/close states.
- **Business Impact**: Customer-facing statements — regulatory requirement for credit card issuers. Accuracy of balances, transactions, and formatting is critical.
- **Migration Concern**: ALTER statement has no Java equivalent — must be refactored to explicit method calls or strategy pattern. Dual-format output → template engine (Thymeleaf/FreeMarker for HTML, text formatter for plain text).

---

### #7 — COACTVWC (Account View) — Score: 6.9

| Metric               | Value                     | Assessment       |
|----------------------|---------------------------|------------------|
| Lines of Code        | 941                       | High |
| IF Statements        | 68                        | High branching |
| EVALUATE Statements  | 8                         | State machine pattern |
| PERFORM Statements   | 23                        | Moderate |
| CICS Commands        | 14 (READ ×3, SEND, RECEIVE, XCTL, HANDLE ABEND) | Multi-file reads |
| Files Accessed       | ACCTDATA (R), CARDXREF (R), CUSTDATA (R) | 3-file join for display |

**Why it's #7**:
- **Complexity**: Joins data from 3 VSAM files to compose the account detail screen. Complex field formatting and screen-state management. Handles ABEND for error recovery.
- **Risk**: Read-only for data files, reducing mutation risk. However, displays sensitive financial data (balances, limits) — must enforce access controls.
- **Business Impact**: Primary account inquiry screen — used in every customer service interaction. Must display accurate, real-time data.
- **Migration Concern**: Three separate VSAM reads → single JPA query with joins or DTO projection. BMS screen → REST API response / web page.

---

### #8 — COTRN02C (Transaction Add) — Score: 7.0

| Metric               | Value                     | Assessment       |
|----------------------|---------------------------|------------------|
| Lines of Code        | 783                       | Medium-high |
| IF Statements        | 14                        | Low-moderate |
| EVALUATE Statements  | 13                        | Complex state logic |
| PERFORM Statements   | 61                        | **Highest PERFORM count** |
| CICS Commands        | 11 (READ, WRITE, STARTBR, READPREV, ENDBR, SEND, RECEIVE) | Full CRUD + browse |
| Files Accessed       | TRANSACT (W), ACCTDATA (R), CARDXREF (R/Browse) | Transaction creation |
| CALL Statements      | 2 (CSUTLDTC for date conversion) | Date utility dependency |

**Why it's #8**:
- **Complexity**: Highest PERFORM count in the codebase (61). Implements transaction ID generation via STARTBR/READPREV to find the last ID, then increments. Multi-step validation against account and cross-reference data. Calls date utility for timestamp conversion.
- **Risk**: WRITE to TRANSACT file creates permanent financial records. Transaction ID generation via browse-last-and-increment could have concurrency issues under load.
- **Business Impact**: Online transaction entry — allows manual transaction creation. Must validate amount, card, and account before writing.
- **Migration Concern**: Transaction ID generation → database sequence or UUID. Date conversion subroutine → java.time API. Must ensure atomicity of write.

---

### #9 — CBTRN03C (Transaction Report) — Score: 6.0

| Metric               | Value                     | Assessment       |
|----------------------|---------------------------|------------------|
| Lines of Code        | 649                       | Medium-high |
| IF Statements        | 36                        | Moderate |
| EVALUATE Statements  | 6                         | Some branching |
| PERFORM Statements   | 33                        | Iterative |
| Files Accessed       | 6 files (TRANSACT, XREFFILE, TRANTYPE, TRANCATG, DATEPARM, TRANREPT) | **Widest read span** |
| Operations           | All READs + WRITE (report) | Read-heavy, report output |

**Why it's #9**:
- **Complexity**: Reads from 6 different input files to compose a comprehensive daily transaction report. Complex report formatting with page breaks, subtotals per account, and grand totals. Date range filtering from parameter file.
- **Risk**: Read-only for business data (low mutation risk), but report accuracy is critical for audit. Reads TRANTYPE and TRANCATG for code-to-description lookups.
- **Business Impact**: Daily operational report used for reconciliation and audit. Regulatory requirement for transaction record-keeping.
- **Migration Concern**: 6-file read coordination → SQL joins in a single query. Report formatting → JasperReports or similar. Date parameter file → command-line args or Spring Batch job parameters.

---

### #10 — COTRN00C (Transaction List) — Score: 6.0

| Metric               | Value                     | Assessment       |
|----------------------|---------------------------|------------------|
| Lines of Code        | 699                       | Medium-high |
| IF Statements        | 26                        | Moderate |
| EVALUATE Statements  | 8                         | Screen state handling |
| PERFORM Statements   | 43                        | High iteration |
| CICS Commands        | 10 (STARTBR, READNEXT, READPREV, ENDBR, SEND, RECEIVE) | Browse pattern |
| Files Accessed       | TRANSACT (Browse) | Scrollable VSAM browse |

**Why it's #10**:
- **Complexity**: VSAM browse with forward/backward paging (PF7/PF8). Complex page-state tracking for continuation keys. Similar pattern to COCRDLIC but for transactions.
- **Risk**: Browse-only (no mutations). Must handle concurrent updates during browse gracefully.
- **Business Impact**: Primary transaction inquiry screen — used for customer support and dispute investigation.
- **Migration Concern**: VSAM browse → paginated database query. PF-key paging → REST pagination API.

---

## Risk Heat Map

Visual representation of risk dimensions across all 31 core programs.

```
                    Business Impact →
                 Low        Medium       High
            ┌──────────┬──────────┬──────────┐
    High    │          │ CBSTM03A │ COACTUPC │  ← Complexity
            │          │ COCRDLIC │ CBTRN02C │
            │          │ COCRDUPC │          │
            ├──────────┼──────────┼──────────┤
    Medium  │ CBACT01C │ COTRN00C │ CBACT04C │
            │ CBACT02C │ COACTVWC │ COTRN02C │
            │ CBACT03C │ COUSR00C │          │
            │ CBCUS01C │ COBIL00C │          │
            │          │ CBTRN03C │          │
            ├──────────┼──────────┼──────────┤
    Low     │ COBSWAIT │ COUSR01C │          │
            │ CSUTLDTC │ COUSR02C │          │
            │ CBSTM03B │ COUSR03C │          │
            │ UNUSED1Y │ COSGN00C │          │
            │          │ COMEN01C │          │
            │          │ COADM01C │          │
            └──────────┴──────────┴──────────┘
```

**Critical Zone** (top-right): COACTUPC, CBTRN02C — highest priority for migration attention.

---

## Recommended Migration Order

Based on the hotspot analysis, we recommend a phased migration approach:

### Phase 1 — Foundation (Low Risk, High Reuse)

Migrate shared infrastructure and utilities first to establish the Java foundation.

| Order | Module     | Rationale                                                   |
|-------|------------|-------------------------------------------------------------|
| 1.1   | CSUTLDTC   | Date utility → `java.time` API; depended on by other modules |
| 1.2   | COCOM01Y   | COMMAREA → Java session/DTO; used by all online programs     |
| 1.3   | Copybooks  | All CV* copybooks → JPA Entity classes + DTOs                |
| 1.4   | CSUSR01Y   | User security → Spring Security UserDetails                  |
| 1.5   | COSGN00C   | Sign-on → Spring Security authentication controller          |

### Phase 2 — Read-Only Screens (Medium Risk)

Migrate inquiry programs that don't modify data.

| Order | Module     | Rationale                                                   |
|-------|------------|-------------------------------------------------------------|
| 2.1   | COACTVWC   | Account View — read-only, validates entity mapping           |
| 2.2   | COCRDSLC   | Card Detail View — read-only                                 |
| 2.3   | COTRN01C   | Transaction View — read-only                                 |
| 2.4   | COTRN00C   | Transaction List — browse → pagination pattern               |
| 2.5   | COCRDLIC   | Card List — browse → pagination pattern                      |
| 2.6   | COUSR00C   | User List — browse → pagination pattern                      |

### Phase 3 — Batch Read/Report Programs (Medium Risk)

Migrate batch programs that read data and produce output.

| Order | Module     | Rationale                                                   |
|-------|------------|-------------------------------------------------------------|
| 3.1   | CBTRN03C   | Transaction Report — Spring Batch job, read-only + output    |
| 3.2   | CBSTM03A/B | Statement Generation — most complex report, dual-format      |
| 3.3   | CBEXPORT   | Data Export — validates all entity reads work correctly       |
| 3.4   | CBIMPORT   | Data Import — validates all entity writes work correctly      |

### Phase 4 — Maintenance Screens (High Risk)

Migrate programs that modify data.

| Order | Module     | Rationale                                                   |
|-------|------------|-------------------------------------------------------------|
| 4.1   | COUSR01C   | User Add — simplest write operation                          |
| 4.2   | COUSR02C   | User Update — simple REWRITE                                 |
| 4.3   | COUSR03C   | User Delete — simple DELETE                                  |
| 4.4   | COTRN02C   | Transaction Add — online WRITE + validation                  |
| 4.5   | COBIL00C   | Bill Payment — financial write                               |
| 4.6   | COCRDUPC   | Card Update — complex REWRITE with validation                |
| 4.7   | COACTUPC   | Account Update — **most complex; migrate last among online** |

### Phase 5 — Core Batch Processing (Highest Risk)

Migrate the financial batch engine last, with extensive parallel-run testing.

| Order | Module     | Rationale                                                   |
|-------|------------|-------------------------------------------------------------|
| 5.1   | CBTRN01C   | Transaction Validation — prerequisite for posting            |
| 5.2   | CBTRN02C   | Transaction Posting — **financial core, highest risk**       |
| 5.3   | CBACT04C   | Interest Calculation — **financial precision critical**      |

### Phase 6 — Optional Modules

Migrate if needed; may be replaced by modern alternatives.

| Order | Module         | Rationale                                               |
|-------|----------------|---------------------------------------------------------|
| 6.1   | VSAM-MQ module | COACCT01, CODATE01 → REST API microservices             |
| 6.2   | DB2 module     | COTRTLIC, COTRTUPC, COBTUPDT → already near SQL; easiest|
| 6.3   | IMS/MQ module  | Most complex middleware; consider redesign vs. migrate   |

---

## Key Risk Factors Summary

| Risk Factor                          | Affected Modules                      | Mitigation Strategy                           |
|--------------------------------------|---------------------------------------|-----------------------------------------------|
| **Decimal precision drift**          | CBTRN02C, CBACT04C, COACTUPC         | Use `BigDecimal` with `HALF_EVEN` rounding; parallel-run validation |
| **Multi-file atomicity**             | CBTRN02C, CBACT04C, COACTUPC         | Database transactions with proper isolation levels |
| **ALTER statement**                  | CBSTM03A                             | Refactor to explicit method dispatch / strategy pattern |
| **VSAM browse → pagination**         | COCRDLIC, COTRN00C, COUSR00C         | Keyset pagination with indexed queries |
| **PII exposure (SSN, Gov ID)**       | CVCUS01Y, COACTVWC, COACTUPC         | Field-level encryption, audit logging, access controls |
| **Plaintext passwords**              | CSUSR01Y, COSGN00C                   | bcrypt/Argon2 hashing in Java |
| **Transaction ID generation**        | COTRN02C (online)                    | Database sequence or UUID; eliminate browse-and-increment |
| **COMMAREA session state**           | All online programs                  | HTTP session or JWT with proper timeout/invalidation |
| **TDQ-based job submission**         | CORPT00C                             | REST API trigger or message queue for async batch |
| **80-byte record padding (FILLER)**  | All copybooks                        | Drop FILLER fields; use proper column types in RDBMS |

---

*End of Hotspot Report*
