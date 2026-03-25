# CardDemo Cutover Plan

## Overview

This document defines a phased migration sequence for the CardDemo mainframe application, ordered from **lowest-risk to highest-risk** functional areas. Each phase specifies the programs, data, and infrastructure to migrate, along with acceptance criteria, rollback strategies, and estimated timelines.

### Guiding Principles

1. **Start with read-only, zero-write-coupling contexts** — minimize blast radius.
2. **Establish foundational services first** — IAM and database schema before business logic.
3. **Dual-run legacy and modern systems in parallel** during transition — fallback to legacy at any point.
4. **Migrate batch after online** — batch jobs have wider blast radius and tighter coupling.
5. **Preserve data integrity above all** — every phase includes data reconciliation checks.

---

## Pre-Migration Phase: Foundation (Weeks 1–6)

### Objective
Stand up the target platform, database schema, data migration pipeline, and CI/CD infrastructure before any COBOL program migration begins.

### Tasks

| # | Task | Details | Duration |
|---|---|---|---|
| F-1 | Provision target infrastructure | Java 17+, Spring Boot 3.x, PostgreSQL, API Gateway, CI/CD pipeline | 2 weeks |
| F-2 | Design and create database schema | Translate VSAM copybooks to relational tables (see mapping below) | 2 weeks |
| F-3 | Build data migration pipeline | ETL from VSAM files to PostgreSQL; use `app/data/ASCII/` sample data for validation | 2 weeks |
| F-4 | Set up dual-run infrastructure | API Gateway routes requests to legacy or modern system based on feature flags | 1 week |
| F-5 | Establish monitoring and alerting | Application metrics, error rates, latency dashboards | 1 week |

### VSAM-to-PostgreSQL Schema Mapping

| VSAM File | Copybook | PostgreSQL Table | Record Size | Estimated Rows |
|---|---|---|---|---|
| USRSEC | CSUSR01Y | `users` | 80 bytes | ~100 |
| ACCTDAT | CVACT01Y | `accounts` | 300 bytes | ~10,000 |
| CARDDAT | CVACT02Y | `cards` | 150 bytes | ~20,000 |
| CUSTDAT | CVCUS01Y | `customers` | 500 bytes | ~10,000 |
| CARDXREF | CVACT03Y | `card_xref` | 50 bytes | ~20,000 |
| TRANSACT | CVTRA05Y | `transactions` | 350 bytes | ~1,000,000 |
| DALYTRAN | CVTRA06Y | `daily_transactions` | 350 bytes | ~10,000/day |
| TCATBALF | CVTRA01Y | `tran_category_balances` | 50 bytes | ~50,000 |
| DISCGRP | CVTRA02Y | `disclosure_groups` | 50 bytes | ~500 |

### Acceptance Criteria
- [ ] All PostgreSQL tables created with correct column types and constraints
- [ ] Sample data from `app/data/ASCII/` loaded and validated (row counts, checksums)
- [ ] CI/CD pipeline builds and deploys Spring Boot application
- [ ] API Gateway configured with feature flag routing
- [ ] Monitoring dashboards operational

### Rollback
No rollback needed — legacy system is untouched. Foundation work is additive only.

---

## Phase 1: Identity & Access Management (Weeks 7–10)

### Risk Level: LOW

### Rationale
Authentication is a clean boundary with no cross-context data writes. USRSEC is exclusively owned by IAM programs. Migrating first establishes the security foundation for all subsequent phases.

### Programs to Migrate

| Legacy Program | Target Component | Strategy |
|---|---|---|
| `COSGN00C` (signon) | `AuthController` + `AuthService` + Spring Security | Rewrite |
| `COUSR00C` (user list) | `UserController.listUsers()` | Rewrite |
| `COUSR01C` (user add) | `UserController.createUser()` | Rewrite |
| `COUSR02C` (user update) | `UserController.updateUser()` | Rewrite |
| `COUSR03C` (user delete) | `UserController.deleteUser()` | Rewrite |

### Data Migration
- Migrate USRSEC → `users` table
- **Hash all passwords** during migration (BCrypt)
- Map `SEC-USR-TYPE` ('A'/'U') to role-based access control (RBAC) roles

### Integration Points
- New IAM service issues JWT tokens on successful authentication
- During dual-run: legacy CICS signon (`CC00` transaction) continues to work for 3270 users
- API Gateway validates JWT for all modern API calls

### BMS Maps Retired
- `COSGN00` (signon screen) — replaced by web login form
- `COUSR00`–`COUSR03` (user management screens) — replaced by admin web UI

### Acceptance Criteria
- [ ] Users can authenticate via REST API and receive JWT tokens
- [ ] Admin users can perform CRUD on user accounts via REST API
- [ ] Passwords are stored as BCrypt hashes (no plaintext)
- [ ] Legacy 3270 signon still works (dual-run)
- [ ] JWT tokens are validated by API Gateway for subsequent API calls
- [ ] All five COBOL programs have functional Java equivalents with unit tests

### Rollback Strategy
- Revert API Gateway routing to bypass IAM service
- Legacy USRSEC file remains untouched during dual-run
- Users fall back to legacy CICS signon

### Estimated Duration: 4 weeks

---

## Phase 2: Reporting & Statements (Weeks 11–14)

### Risk Level: LOW

### Rationale
Reporting is read-only — no writes to any shared data files. Reports consume transaction and account data without modifying it. The mainframe-specific JCL submission mechanism (TDQ) needs replacement but has no impact on core transactional data.

### Programs to Migrate

| Legacy Program | Target Component | Strategy |
|---|---|---|
| `CORPT00C` (report request) | `ReportController` + `ReportService` | Replatform |
| `CBSTM03A` (statement gen. A) | `StatementGenerationJob` (Spring Batch) | Replatform |
| `CBSTM03B` (statement gen. B) | `StatementGenerationJob` (Spring Batch) | Replatform |
| `CBTRN03C` (transaction report) | `TransactionReportJob` (Spring Batch) | Replatform |

### Data Migration
- No additional data migration — reports read from `transactions` and `accounts` tables (migrated in Foundation phase)

### Key Changes
- Replace TDQ-based JCL submission (`CORPT00C` writes JCL to internal reader) with REST API trigger for batch jobs
- Replace line-printer report output with PDF/CSV generation
- Add date range validation (monthly/yearly/custom) in report request API

### JCL Jobs Retired
- `CREASTMT.JCL` → `StatementGenerationJob`
- `TRANREPT.jcl` → `TransactionReportJob`
- `REPTFILE.jcl` → incorporated into report service

### BMS Maps Retired
- `CORPT00` (report request screen) — replaced by web report UI

### Acceptance Criteria
- [ ] Monthly, yearly, and custom date range reports generate correctly
- [ ] Report output available as PDF and CSV
- [ ] Statement generation batch job produces correct output matching legacy
- [ ] Report scheduling works via REST API or cron trigger
- [ ] Legacy report submission still works during dual-run

### Rollback Strategy
- Re-enable legacy TDQ-based report submission
- Reports revert to mainframe batch processing
- No data impact — reports are read-only

### Estimated Duration: 4 weeks

---

## Phase 3: Card Management (Weeks 15–20)

### Risk Level: MEDIUM-LOW

### Rationale
Card management has clear CRUD boundaries and primarily involves the CARDDAT file, which is owned by the Account & Card context. Card operations are mostly independent of balance-affecting transactions, making them a lower-risk extraction than Account Management.

### Programs to Migrate

| Legacy Program | Target Component | Strategy |
|---|---|---|
| `COCRDLIC` (card list) | `CardController.listCards()` | Strangler Fig |
| `COCRDSLC` (card detail) | `CardController.getCard()` | Strangler Fig |
| `COCRDUPC` (card update) | `CardController.updateCard()` | Strangler Fig |

### Data Migration
- CARDDAT and CARDXREF already migrated to PostgreSQL in Foundation phase
- Validate alternate index behavior: `CARDAIX` (browse by account) → SQL query with `WHERE card_acct_id = ?`

### Integration Points
- Card list API supports pagination (replacing CICS STARTBR/READNEXT/ENDBR browse pattern)
- Card cross-reference lookups exposed as internal API for other services
- Account ID passed as query parameter (replaces COMMAREA `CDEMO-ACCT-ID`)

### BMS Maps Retired
- `COCRDLI` (card list screen)
- `COCRDSL` (card detail screen)
- `COCRDUP` (card update screen)

### Acceptance Criteria
- [ ] Card list with pagination returns correct results
- [ ] Card detail view displays all fields from CVACT02Y copybook
- [ ] Card update validates and persists all editable fields
- [ ] Cross-reference lookups work for both card-path and account-path browsing
- [ ] Legacy 3270 card screens still function during dual-run

### Rollback Strategy
- API Gateway routes card API traffic back to legacy CICS
- CARDDAT VSAM file remains authoritative during dual-run (sync changes back if needed)

### Estimated Duration: 6 weeks

---

## Phase 4: Account Management & Bill Payment (Weeks 21–30)

### Risk Level: MEDIUM-HIGH

### Rationale
Account Management is the most complex online context (`COACTUPC` is 4,237 LOC) with multiple inbound writers to `ACCTDAT`. Bill Payment is included in this phase because it directly writes to `ACCTDAT` and `TRANSACT`, and migrating it alongside Account Management allows the `AccountService.adjustBalance()` pattern to be established once.

### Programs to Migrate

| Legacy Program | Target Component | Strategy |
|---|---|---|
| `COACTVWC` (account view) | `AccountController.getAccount()` | Strangler Fig |
| `COACTUPC` (account update) | `AccountController.updateAccount()` | Strangler Fig |
| `COBIL00C` (bill payment) | `PaymentController.payBill()` | Refactor |

### Data Migration
- ACCTDAT and CUSTDAT already in PostgreSQL
- Establish `AccountService.adjustBalance()` as the single entry point for all balance modifications
- Implement optimistic locking on `accounts.curr_bal` column

### Key Complexity Areas

**Account Update (`COACTUPC` — 4,237 LOC):**
- Field-level validation for 20+ fields (dates, SSN format, phone format, FICO score range, credit limits)
- Multi-file reads: ACCTDAT + CUSTDAT + CARDDAT + CARDXREF
- Extract validation rules into `AccountValidationService`
- Migrate in sub-phases:
  1. Account View (read-only) — 2 weeks
  2. Account Update (write) — 4 weeks
  3. Bill Payment — 2 weeks

**Bill Payment (`COBIL00C`):**
- Requires transaction creation + balance update (cross-context)
- Implement as: `PaymentService.payBill()` → calls `AccountService.adjustBalance()` + `TransactionService.create()`
- Use `@Transactional` for ACID guarantees (shared database) or saga pattern (separate databases)

### Customer Data
- Customer fields currently displayed on Account screens
- Extract `CustomerService` as internal component within Account service
- Expose `GET /customers/{id}` API for future separation

### BMS Maps Retired
- `COACTVW` (account view screen)
- `COACTUP` (account update screen)
- `COBIL00` (bill payment screen)

### Acceptance Criteria
- [ ] Account view returns all fields from CVACT01Y + customer name/address from CVCUS01Y
- [ ] Account update validates all 20+ fields correctly (match legacy validation rules)
- [ ] Bill payment creates transaction record and updates balance atomically
- [ ] Balance modifications are serialized (no lost updates under concurrent access)
- [ ] Data reconciliation: ACCTDAT legacy balances match PostgreSQL accounts within $0.01
- [ ] All three programs have functional Java equivalents with comprehensive unit tests

### Rollback Strategy
- API Gateway reverts account/payment traffic to legacy CICS
- During dual-run, balance synchronization runs bidirectionally (legacy↔modern)
- If balance discrepancy detected: halt modern writes, reconcile, then resume
- **Critical:** Bill payment rollback requires reversing any in-flight payments — use compensating transactions

### Estimated Duration: 10 weeks

---

## Phase 5: Online Transaction Management (Weeks 31–36)

### Risk Level: MEDIUM-HIGH

### Rationale
Transaction list/view are read-only and low-risk, but Transaction Add writes to both TRANSACT and ACCTDAT. This phase builds on the `AccountService.adjustBalance()` infrastructure established in Phase 4.

### Programs to Migrate

| Legacy Program | Target Component | Strategy |
|---|---|---|
| `COTRN00C` (transaction list) | `TransactionController.listTransactions()` | Strangler Fig |
| `COTRN01C` (transaction view) | `TransactionController.getTransaction()` | Strangler Fig |
| `COTRN02C` (transaction add) | `TransactionController.addTransaction()` | Strangler Fig |

### Data Migration
- TRANSACT already in PostgreSQL
- Transaction ID generation: replace VSAM MAX+1 browse with PostgreSQL sequence

### Integration Points
- Transaction list supports date range filtering and pagination
- Transaction add calls `AccountService.adjustBalance()` for balance updates
- Card cross-reference lookups via `CardXrefService`

### Sub-Phases
1. Transaction List + View (read-only) — 2 weeks
2. Transaction Add (write) — 4 weeks

### BMS Maps Retired
- `COTRN00` (transaction list screen)
- `COTRN01` (transaction view screen)
- `COTRN02` (transaction add screen)

### Acceptance Criteria
- [ ] Transaction list with pagination and date filtering returns correct results
- [ ] Transaction view displays all fields from CVTRA05Y copybook
- [ ] Transaction add creates record and updates account balance atomically
- [ ] Transaction IDs are unique and sequential
- [ ] Legacy 3270 transaction screens still function during dual-run

### Rollback Strategy
- API Gateway reverts transaction API traffic to legacy CICS
- Any transactions created in modern system are synced back to TRANSACT VSAM if rollback needed

### Estimated Duration: 6 weeks

---

## Phase 6: Batch Transaction Posting (Weeks 37–44)

### Risk Level: HIGH

### Rationale
Batch transaction posting (`CBTRN02C`) is the core of the nightly batch cycle. It performs multi-file coordinated I/O (DALYTRAN → TRANSACT, ACCTDAT, TCATBALF, XREFFILE) with validation and reject handling. Failure in batch posting affects all downstream processes (interest calculation, statements, backups).

### Programs to Migrate

| Legacy Program | Target Component | Strategy |
|---|---|---|
| `CBTRN02C` (transaction posting) | `TransactionPostingJob` (Spring Batch) | Refactor |
| `CBTRN01C` (batch transaction processing) | `TransactionProcessingJob` | Refactor |

### Spring Batch Job Design

```
TransactionPostingJob
├── Step 1: Read daily transactions (ItemReader → daily_transactions table)
├── Step 2: Validate & post (ItemProcessor)
│   ├── Validate card via card_xref
│   ├── Validate account status via accounts
│   ├── Post to transactions table
│   ├── Update account balance via AccountService
│   └── Update category balances in tran_category_balances
├── Step 3: Write rejects (ItemWriter → daily_rejects table)
└── Step 4: Generate summary counts (StepExecutionListener)
```

### JCL Jobs Retired
- `POSTTRAN.jcl` → `TransactionPostingJob`
- `COMBTRAN.jcl` → combined into job flow
- `TRANFILE.jcl` → database load replaces VSAM refresh
- `TRANBKP.jcl` → database backup replaces VSAM backup
- `CLOSEFIL.jcl` / `OPENFIL.jcl` → no equivalent needed (no CICS file open/close)

### Batch Cycle Migration
Legacy batch cycle order:
```
CLOSEFIL → Data Refresh → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL
```

Modern batch flow:
```
TransactionPostingJob → InterestCalculationJob → BackupJob → StatementGenerationJob
```
- CLOSEFIL/OPENFIL are eliminated (no CICS file locking needed)
- TRANIDX (alternate index rebuild) is eliminated (database indexes are maintained automatically)
- COMBTRAN is absorbed into the posting job

### Acceptance Criteria
- [ ] Daily transactions posted correctly: match legacy posting counts ±0
- [ ] Reject records generated for same invalid transactions as legacy
- [ ] Account balances after posting match legacy within $0.01
- [ ] Category balances updated correctly
- [ ] Spring Batch job completes within acceptable time window (< 2x legacy duration)
- [ ] Job restart/recovery works correctly after mid-batch failure

### Rollback Strategy
- **Critical:** Batch posting is all-or-nothing per daily cycle.
- If modern batch fails: revert to legacy JCL submission for that cycle
- Maintain legacy batch infrastructure in warm standby for at least 3 cycles after cutover
- Database transaction rollback ensures no partial posting on failure

### Estimated Duration: 8 weeks

---

## Phase 7: Interest Calculation & Batch Finalization (Weeks 45–52)

### Risk Level: HIGH

### Rationale
Interest calculation is the most financially sensitive batch process. Incorrect interest computation directly affects customer billing. This is the highest-risk migration item due to the financial precision requirements and the complex multi-file processing pattern.

### Programs to Migrate

| Legacy Program | Target Component | Strategy |
|---|---|---|
| `CBACT04C` (interest calculation) | `InterestCalculationJob` (Spring Batch) | Refactor |
| Remaining batch utilities | Various Spring Batch jobs | Replatform |

### Spring Batch Job Design

```
InterestCalculationJob
├── Step 1: Read category balances (ItemReader → tran_category_balances)
├── Step 2: Calculate interest (ItemProcessor)
│   ├── Lookup disclosure group interest rate
│   ├── Compute interest: balance × rate / 12
│   ├── Compute fees (if applicable)
│   └── Aggregate per account
├── Step 3: Update accounts (ItemWriter)
│   ├── Adjust account balance via AccountService
│   ├── Generate interest transaction records
│   └── Reset cycle credit/debit amounts
└── Step 4: Reconciliation report
```

### Data Dependencies
- DISCGRP (disclosure group rates) → `disclosure_groups` table — must be migrated and validated
- TCATBALF (category balances) → `tran_category_balances` — populated by batch posting (Phase 6)
- Interest rates must match legacy DISCGRP values exactly (decimal precision)

### Financial Validation
- **Parallel run required:** Run both legacy and modern interest calculation for at least 3 monthly cycles
- Compare results per account: modern vs. legacy interest amounts
- Tolerance: $0.00 (exact match required for financial calculations)
- Any discrepancy triggers investigation before modern system becomes authoritative

### JCL Jobs Retired
- `INTCALC.jcl` → `InterestCalculationJob`
- `DISCGRP.jcl` → database seed/update script
- `DEFGDGB.jcl` / `DEFGDGD.jcl` → eliminated (no GDG in modern system)

### Acceptance Criteria
- [ ] Interest calculated for every account with non-zero category balances
- [ ] Interest amounts match legacy calculation exactly ($0.00 tolerance)
- [ ] Interest transaction records generated with correct type/category codes
- [ ] Account balances updated correctly after interest posting
- [ ] Cycle credit/debit amounts reset after interest calculation
- [ ] Disclosure group rates loaded correctly from database
- [ ] 3 monthly parallel-run cycles completed with zero discrepancies

### Rollback Strategy
- Revert to legacy interest calculation JCL
- If modern calculation produced incorrect results: reverse interest transactions and rerun legacy
- Maintain legacy batch infrastructure until 3 clean cycles have run on modern system

### Estimated Duration: 8 weeks

---

## Phase 8: Legacy Decommission (Weeks 53–58)

### Risk Level: MEDIUM (operational, not technical)

### Rationale
After all functional areas are migrated and validated, decommission the legacy mainframe components. This phase focuses on operational cutover, not code migration.

### Tasks

| # | Task | Duration |
|---|---|---|
| D-1 | Final data reconciliation across all tables | 1 week |
| D-2 | Remove API Gateway dual-run routing (all traffic to modern) | 1 week |
| D-3 | Disable legacy CICS transactions (read-only mode first, then offline) | 1 week |
| D-4 | Archive VSAM files and JCL jobs | 1 week |
| D-5 | Retire mainframe LPAR or reduce capacity | 2 weeks |
| D-6 | Final documentation and knowledge transfer | 1 week |

### Decommission Sequence
1. **Week 53:** Final reconciliation — verify all data matches between VSAM and PostgreSQL
2. **Week 54:** Put legacy CICS in read-only mode — all writes go to modern system
3. **Week 55:** Disable legacy CICS transactions — 3270 terminals redirected to web UI
4. **Week 56:** Archive all VSAM files, JCL, and COBOL source to cold storage
5. **Week 57–58:** Reduce mainframe capacity; negotiate LPAR retirement with operations

### Acceptance Criteria
- [ ] Zero traffic flowing to legacy CICS for 7 consecutive days
- [ ] All VSAM data archived with checksums
- [ ] Modern system handles full production load within SLA
- [ ] Monitoring confirms no legacy dependencies remain
- [ ] Operations team trained on modern system runbooks

### Rollback Strategy
- Maintain archived VSAM files for 12 months
- Keep mainframe LPAR in cold standby for 6 months (emergency fallback)
- Document emergency re-activation procedure

---

## Timeline Summary

```
Weeks:  1─────6  7────10  11───14  15───20  21──────30  31───36  37──────44  45──────52  53───58
        ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐ ┌────────┐ ┌──────┐ ┌────────┐ ┌────────┐ ┌──────┐
        │Found-│ │Phase1│ │Phase2│ │Phase3│ │ Phase 4│ │Phase5│ │ Phase 6│ │ Phase 7│ │Phase8│
        │ation │ │ IAM  │ │Report│ │ Card │ │Acct+Pay│ │Trans │ │ Batch  │ │Interest│ │Decom │
        │      │ │      │ │      │ │ Mgmt │ │  ment  │ │Online│ │Posting │ │ Calc.  │ │      │
        └──────┘ └──────┘ └──────┘ └──────┘ └────────┘ └──────┘ └────────┘ └────────┘ └──────┘
Risk:   NONE     LOW      LOW      MED-LOW   MED-HIGH  MED-HIGH   HIGH       HIGH      MEDIUM

Programs:  0      5        4        3         3          3         2          1+        0
           ─      ─        ─        ─         ─          ─         ─          ─         ─
Total:     0      5        9       12        15         18        20         21+       ALL
```

**Total Estimated Duration: 58 weeks (~14 months)**

---

## Data Synchronization Strategy (Dual-Run Period)

During Phases 1–7, the legacy mainframe and modern Java system run in parallel. Data synchronization is critical:

### Approach: Change Data Capture (CDC)

```
┌─────────┐         ┌─────────────┐         ┌──────────┐
│ Legacy  │──CDC──► │   Sync      │──────►  │  Modern  │
│  VSAM   │         │   Service   │         │PostgreSQL│
│  Files  │◄──CDC── │             │ ◄────── │  Tables  │
└─────────┘         └─────────────┘         └──────────┘
```

1. **VSAM → PostgreSQL:** Capture changes from VSAM files and replay to PostgreSQL (primary direction during early phases)
2. **PostgreSQL → VSAM:** When modern system becomes authoritative for a context, sync writes back to VSAM for legacy consumers still running
3. **Reconciliation:** Nightly batch job compares record counts and checksums between VSAM and PostgreSQL; alert on any discrepancy

### Conflict Resolution
- **Single-writer principle:** During any phase, each data entity has exactly one authoritative source (either VSAM or PostgreSQL)
- **Phase-by-phase ownership transfer:** As each context is migrated, PostgreSQL becomes authoritative for that context's data
- **Conflict = bug:** Any conflicting writes indicate a routing error in the API Gateway and must be investigated immediately

---

## Go/No-Go Decision Criteria

Each phase transition requires a Go/No-Go decision based on:

| Criterion | Threshold |
|---|---|
| Functional test pass rate | 100% (no failures) |
| Data reconciliation discrepancies | 0 for financial data; < 0.01% for other data |
| Performance (response time) | Within 120% of legacy response time |
| Batch job duration | Within 200% of legacy duration (database I/O is faster, but overhead exists) |
| Error rate (production) | < 0.1% for 7 consecutive days |
| Rollback tested | Successfully demonstrated within 15 minutes |
| Stakeholder sign-off | Product owner + Technical lead + Operations |
