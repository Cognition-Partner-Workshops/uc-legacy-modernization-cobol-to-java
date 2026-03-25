# CardDemo Cutover Plan

> Phased migration sequence from lowest-risk to highest-risk, with entry/exit criteria and rollback strategies.

---

## Migration Principles

1. **Lowest-risk first** — Start with read-only, isolated modules; end with financial write operations
2. **Strangler pattern** — Old and new systems coexist during each phase via API facade
3. **No big-bang** — Each phase is independently deployable and rollback-safe
4. **Dual-run validation** — Critical phases run old and new in parallel with output comparison
5. **Eliminate duplicates** — Each phase consolidates duplicate logic identified in the Domain Decomposition

---

## Phase Overview

```
Phase 1 (Weeks 1–6)     Phase 2 (Weeks 5–12)    Phase 3 (Weeks 10–18)
─────────────────────    ───────────────────────   ──────────────────────
Identity & Access        Card Management           Account Management
Reporting (read-only)    Transaction Queries        Bill Payment
Data Migration Utils     Transaction Add            Data Export/Import v2

Phase 4 (Weeks 16–24)   Phase 5 (Weeks 22–30)    Phase 6 (Weeks 28–32)
─────────────────────    ───────────────────────   ──────────────────────
Statement Generation     Batch Transaction Post     Decommission Legacy
Batch Reporting          Interest Calculation       CICS/VSAM Shutdown
                         CLOSEFIL/OPENFIL removal   Final Data Migration
```

---

## Phase 1: Foundation & Low-Risk Extraction (Weeks 1–6)

### 1A: Identity & Access Management (Weeks 1–3)

| Item | Detail |
|------|--------|
| **Programs Replaced** | COSGN00C, COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| **Data Migrated** | USRSEC VSAM → `users` table (PostgreSQL) |
| **Duplicate Logic Eliminated** | COUSR01C/02C/03C CRUD boilerplate merged into single User Service (~200 lines saved) |
| **Risk Level** | ★☆☆☆☆ LOW |
| **Strategy** | Rewrite |

**Entry Criteria:**
- [ ] Target database provisioned (PostgreSQL)
- [ ] Spring Boot project scaffolded with Spring Security
- [ ] CI/CD pipeline configured

**Implementation Steps:**
1. Create `users` table with BCrypt password hashes (migrate plaintext → hashed)
2. Implement Spring Security + JWT authentication
3. Build User CRUD REST API (consolidating 4 programs into 1 service)
4. Deploy API facade that routes `/auth/*` and `/users/*` to new service
5. COSGN00C login → `POST /auth/login` returns JWT; redirects to new UI or legacy CICS based on feature flag
6. Test: verify all user operations (create, read, update, delete, login)

**Exit Criteria:**
- [ ] All 5 user/auth programs replaced by REST API
- [ ] JWT authentication working for both new UI and legacy bridge
- [ ] Plaintext passwords eliminated — BCrypt hashes only
- [ ] Load test: 100 concurrent logins with <200ms response time

**Rollback Plan:** Feature flag reverts authentication to legacy COSGN00C via CICS. USRSEC VSAM remains populated via bidirectional sync until Phase 6.

---

### 1B: Reporting (Read-Only) (Weeks 2–5)

| Item | Detail |
|------|--------|
| **Programs Replaced** | CORPT00C (online trigger), CBTRN03C (batch report) |
| **JCL Replaced** | TRANREPT |
| **Duplicate Logic Eliminated** | CBTRN03C and CBSTM03A share transaction data retrieval — extract shared query service (~200 lines saved) |
| **Risk Level** | ★☆☆☆☆ LOW |
| **Strategy** | Strangler → Rewrite |

**Implementation Steps:**
1. Create Transaction Data Query Service (shared by reporting and future statement generation)
2. Replace CBTRN03C report generation with database query + template engine
3. Replace CORPT00C's TDQ/internal reader pattern with REST API trigger
4. Replace SORT steps with SQL ORDER BY
5. Deploy new report generation as async job triggered via REST

**Exit Criteria:**
- [ ] Transaction report generates identical output to CBTRN03C
- [ ] Report trigger works via REST API (no TDQ dependency)
- [ ] Dual-run comparison: old vs. new report output matches 100%

**Rollback Plan:** Re-enable CORPT00C TDQ trigger and TRANREPT JCL.

---

### 1C: Data Migration Utilities (Weeks 3–6)

| Item | Detail |
|------|--------|
| **Programs Replaced** | CBEXPORT, CBIMPORT |
| **JCL Replaced** | CBEXPORT.jcl, CBIMPORT.jcl |
| **Duplicate Logic Eliminated** | CBEXPORT/CBIMPORT mirror logic merged into single service (~200 lines saved) |
| **Risk Level** | ★☆☆☆☆ LOW |
| **Strategy** | Rewrite |

**Implementation Steps:**
1. Create Data Migration Service with export (JSON/CSV) and import (file upload + validation) endpoints
2. Support bulk export of customer, account, card, and transaction data
3. Add validation layer (CBIMPORT's record validation logic preserved)
4. Deploy REST API: `POST /migrate/export`, `POST /migrate/import`

**Exit Criteria:**
- [ ] Export produces valid JSON/CSV matching CBEXPORT output data
- [ ] Import validates and loads data with error reporting
- [ ] Round-trip test: export → import → verify data integrity

---

## Phase 2: Read-Heavy Online Services (Weeks 5–12)

### 2A: Card Management (Weeks 5–9)

| Item | Detail |
|------|--------|
| **Programs Replaced** | COCRDLIC, COCRDSLC, COCRDUPC |
| **Data Migrated** | CARDFILE VSAM → `cards` table, CARDXREF VSAM → `card_xref` table (or FK relationship) |
| **Duplicate Logic Eliminated** | COCRDSLC/COCRDUPC shared card+customer read (~150 lines); all 3 programs' BMS patterns (~200 lines); CARDXREF lookup consolidated from 12 programs |
| **Risk Level** | ★★☆☆☆ LOW-MEDIUM |
| **Strategy** | Strangler |

**Implementation Steps:**
1. Migrate CARDFILE and CARDXREF data to PostgreSQL tables
2. Implement Card Service with list (pagination), detail, and update endpoints
3. Expose `GET /cards?accountId=X` API for cross-context card→account resolution
4. Replace CICS browse (STARTBR/READNEXT/READPREV) with SQL OFFSET/LIMIT
5. Implement role-based filtering (admin vs. regular user) via JWT claims
6. Deploy API facade routing card operations to new service

**Exit Criteria:**
- [ ] Card list pagination works with same data as COCRDLIC
- [ ] Card detail displays same data as COCRDSLC
- [ ] Card update modifies same fields as COCRDUPC
- [ ] CARDXREF API endpoint used by other contexts (replacing direct VSAM reads)

**Rollback Plan:** Feature flag reverts to CICS card programs. Bidirectional sync keeps VSAM and database in sync.

---

### 2B: Transaction Queries (Weeks 6–9)

| Item | Detail |
|------|--------|
| **Programs Replaced** | COTRN00C, COTRN01C |
| **Data Migrated** | TRANSACT VSAM → `transactions` table (read replica initially) |
| **Duplicate Logic Eliminated** | COTRN00C/01C duplicate read patterns (~100 lines saved) |
| **Risk Level** | ★★☆☆☆ LOW-MEDIUM |
| **Strategy** | Strangler |

**Implementation Steps:**
1. Create read replica of TRANSACT data in PostgreSQL
2. Implement Transaction Query Service: list (pagination) and detail endpoints
3. Sync TRANSACT VSAM → database via CDC or periodic batch

**Exit Criteria:**
- [ ] Transaction list displays same data as COTRN00C
- [ ] Transaction detail matches COTRN01C output
- [ ] Pagination handles same data volume as CICS browse

---

### 2C: Transaction Add (Weeks 8–12)

| Item | Detail |
|------|--------|
| **Programs Replaced** | COTRN02C |
| **Risk Level** | ★★★☆☆ MEDIUM |
| **Strategy** | Refactor |

**Implementation Steps:**
1. Implement Transaction Creation Service with UUID-based ID generation (replacing READPREV anti-pattern)
2. Validate card and account via Card Management and Account APIs
3. Date validation via java.time (replacing CSUTLDTC CALL)
4. Write to database with proper constraints
5. Publish `TransactionCreated` event for downstream consumers
6. Bidirectional sync to TRANSACT VSAM during transition

**Exit Criteria:**
- [ ] New transactions appear in both database and VSAM
- [ ] ID uniqueness guaranteed (no duplicates under concurrent load)
- [ ] Card/account validation equivalent to COTRN02C checks

---

## Phase 3: Write Operations (Weeks 10–18)

### 3A: Account Management (Weeks 10–15)

| Item | Detail |
|------|--------|
| **Programs Replaced** | COACTVWC, COACTUPC |
| **Data Migrated** | ACCTFILE VSAM → `accounts` table, CUSTFILE VSAM → `customers` table |
| **Duplicate Logic Eliminated** | COACTVWC/COACTUPC shared read/join pattern (~200 lines saved); CSSETATY attribute control eliminated entirely |
| **Risk Level** | ★★★★☆ HIGH |
| **Strategy** | Strangler (View) + Refactor (Update) |

**Implementation Steps:**
1. Migrate ACCTFILE and CUSTFILE to PostgreSQL
2. Account View: REST endpoint with JOIN (replaces 3-file VSAM read pattern)
3. Account Update: Extract 30+ field validations from COACTUPC into validation framework
4. Implement audit trail (not present in COBOL)
5. Handle concurrent access with optimistic locking (replacing CLOSEFIL exclusive access)

**Exit Criteria:**
- [ ] Account view displays same data as COACTVWC
- [ ] All 30+ field validations from COACTUPC preserved in new validation layer
- [ ] Audit trail records all account modifications
- [ ] Concurrent update handling verified under load

**Rollback Plan:** Feature flag per operation (view vs. update). View can be rolled back independently of update.

---

### 3B: Bill Payment (Weeks 13–17)

| Item | Detail |
|------|--------|
| **Programs Replaced** | COBIL00C |
| **Duplicate Logic Eliminated** | COBIL00C's TRANSACT write pattern shared with COTRN02C — consolidated into Transaction Service |
| **Risk Level** | ★★★★☆ HIGH |
| **Strategy** | Refactor |

**Implementation Steps:**
1. Implement Payment Service with database transaction wrapping:
   - Create payment transaction record
   - Update account balance
   - Both in single DB transaction (fixing the COBOL dual-write inconsistency risk)
2. Add idempotency key to prevent double payments
3. Publish `PaymentProcessed` event
4. Dual-run: process payments in both old and new systems, compare results

**Exit Criteria:**
- [ ] Payment creates transaction AND updates balance atomically
- [ ] Idempotency prevents duplicate payments
- [ ] Dual-run comparison shows 100% match for 30-day period

---

## Phase 4: Batch Processing — Reports & Statements (Weeks 16–24)

### 4A: Statement Generation (Weeks 16–21)

| Item | Detail |
|------|--------|
| **Programs Replaced** | CBSTM03A, CBSTM03B, TXT2PDF1 |
| **JCL Replaced** | CREASTMT, TXT2PDF1 |
| **Duplicate Logic Eliminated** | CBSTM03A data retrieval shares logic with CBTRN03C (already extracted in Phase 1B as shared query service) |
| **Risk Level** | ★★★☆☆ MEDIUM |
| **Strategy** | Rewrite |

**Implementation Steps:**
1. Statement Data Service queries database (reuses Phase 1B shared query service)
2. HTML template engine replaces COBOL HTML string concatenation
3. PDF generation via OpenPDF/iText replaces TXT2PDF1 REXX script
4. Schedule via Spring Scheduler (replaces JCL CREASTMT)
5. Dual-run: generate statements from both systems, compare

**Exit Criteria:**
- [ ] Statements match CBSTM03A output format
- [ ] PDF generation replaces TXT2PDF1
- [ ] Automated scheduling replaces CREASTMT JCL

---

### 4B: Batch Report Enhancement (Weeks 18–22)

| Item | Detail |
|------|--------|
| **JCL Replaced** | Remaining utility JCL (READACCT, READCARD, READCUST, READXREF, PRTCATBL) |
| **Risk Level** | ★☆☆☆☆ LOW |

**Implementation Steps:**
1. Replace IDCAMS PRINT utility JCL with database query reports
2. Replace remaining setup/utility JCL with admin API endpoints or scripts

---

## Phase 5: Core Batch — Financial Processing (Weeks 22–30)

### 5A: Batch Transaction Posting (Weeks 22–27)

| Item | Detail |
|------|--------|
| **Programs Replaced** | CBTRN01C, CBTRN02C |
| **JCL Replaced** | POSTTRAN, COMBTRAN, TRANBKP |
| **Duplicate Logic Eliminated** | CBTRN01C/CBTRN02C merged into single posting service (~300 lines saved) |
| **Risk Level** | ★★★★★ CRITICAL |
| **Strategy** | Replatform → Refactor |

**Implementation Steps:**
1. Replatform CBTRN02C logic to Spring Batch with JDBC (preserving exact behavior)
2. Build reconciliation suite: run old and new posting, compare every output file
3. Run parallel for 30+ days with automated comparison
4. Once validated, refactor to event-driven processing
5. Eliminate CLOSEFIL/OPENFIL — database handles concurrent access via transactions

**Exit Criteria:**
- [ ] Reconciliation shows 0 discrepancies over 30-day parallel run
- [ ] Account balances match to the penny
- [ ] Category balances (TCATBALF) match exactly
- [ ] CLOSEFIL/OPENFIL no longer required

**Rollback Plan:** Revert to JCL batch cycle. VSAM files maintained via bidirectional sync throughout Phase 5.

---

### 5B: Interest Calculation (Weeks 25–30)

| Item | Detail |
|------|--------|
| **Programs Replaced** | CBACT04C |
| **JCL Replaced** | INTCALC |
| **Risk Level** | ★★★★★ CRITICAL |
| **Strategy** | Replatform → Refactor |

**Implementation Steps:**
1. Replatform CBACT04C to Spring Batch with BigDecimal (preserving COBOL decimal arithmetic)
2. Build interest calculation reconciliation suite
3. Run parallel for 60+ days (two billing cycles minimum)
4. Validate every account's interest matches to the penny
5. Refactor rate lookup into configurable rules engine

**Exit Criteria:**
- [ ] Interest calculations match CBACT04C to 0.00 tolerance over 60 days
- [ ] Rate configuration externalized (no hardcoded rates)
- [ ] Audit trail for all interest computations

---

## Phase 6: Decommission Legacy (Weeks 28–32)

### 6A: CICS Shutdown

| Step | Action |
|------|--------|
| 1 | Verify all online programs have feature flags pointing to new services |
| 2 | Disable CICS transaction routing for all CC/CM/CA/CT/CR/CB/CU transactions |
| 3 | Keep CICS available in read-only mode for 30 days (emergency rollback) |
| 4 | Decommission CICS region |

### 6B: VSAM Decommission

| Step | Action |
|------|--------|
| 1 | Stop all bidirectional sync jobs |
| 2 | Take final VSAM snapshots as archive |
| 3 | Verify database is source of truth for all data |
| 4 | Deallocate VSAM files |

### 6C: JCL Decommission

| Step | Action |
|------|--------|
| 1 | Remove all JCL jobs from scheduler (CA7/Control-M) |
| 2 | Archive JCL source for reference |
| 3 | Verify Spring Batch/Scheduler jobs cover all batch functions |

---

## Milestone Summary

| Milestone | Week | Key Deliverable | Risk |
|-----------|------|----------------|------|
| M1: Auth Service Live | 3 | JWT auth replacing COSGN00C | Low |
| M2: Reports Modernized | 5 | REST-triggered reports | Low |
| M3: Card Service Live | 9 | Full card CRUD via REST | Low-Med |
| M4: Transaction Queries Live | 9 | Transaction list/view via REST | Low-Med |
| M5: Transaction Add Live | 12 | Online transaction creation via REST | Medium |
| M6: Account Service Live | 15 | Account view + update via REST | High |
| M7: Payments Live | 17 | Bill payment with ACID guarantees | High |
| M8: Statements Modernized | 21 | Template-based statement generation | Medium |
| M9: Batch Posting Live | 27 | Spring Batch transaction posting | Critical |
| M10: Interest Calc Live | 30 | Replatformed interest calculation | Critical |
| M11: Legacy Decommissioned | 32 | CICS + VSAM fully retired | — |

---

## Resource Requirements

| Phase | Team Composition | Duration |
|-------|-----------------|----------|
| Phase 1 | 2 Java devs + 1 COBOL SME | 6 weeks |
| Phase 2 | 3 Java devs + 1 COBOL SME + 1 QA | 8 weeks |
| Phase 3 | 3 Java devs + 1 COBOL SME + 2 QA | 8 weeks |
| Phase 4 | 2 Java devs + 1 QA | 8 weeks |
| Phase 5 | 3 Java devs + 2 COBOL SMEs + 2 QA | 8 weeks |
| Phase 6 | 1 Java dev + 1 ops + 1 COBOL SME | 4 weeks |

**Note:** Phases overlap by 2–4 weeks. Total calendar time: ~32 weeks (8 months) with parallel tracks.
