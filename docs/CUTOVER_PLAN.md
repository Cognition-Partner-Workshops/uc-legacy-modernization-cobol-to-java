# CardDemo Cutover Plan

> **Generated:** 2026-03-25 | **Application:** AWS CardDemo
> **Purpose:** Phased migration sequence from lowest-risk to highest-risk, with rollback criteria and success gates.

---

## Table of Contents

1. [Cutover Principles](#1-cutover-principles)
2. [Phase Overview](#2-phase-overview)
3. [Phase 1 — Foundation](#3-phase-1--foundation)
4. [Phase 2 — Read-Only Services](#4-phase-2--read-only-services)
5. [Phase 3 — Write Services (Online)](#5-phase-3--write-services-online)
6. [Phase 4 — Batch Processing](#6-phase-4--batch-processing)
7. [Phase 5 — Final Cutover & Decommission](#7-phase-5--final-cutover--decommission)
8. [Rollback Strategy](#8-rollback-strategy)
9. [Success Gates](#9-success-gates)
10. [Timeline Estimate](#10-timeline-estimate)

---

## 1. Cutover Principles

| Principle                       | Description                                                                                       |
|--------------------------------|---------------------------------------------------------------------------------------------------|
| **Lowest-risk first**          | Begin with components that have clean extraction seams and no cross-context write violations.     |
| **Read before write**          | Migrate read-only operations before write operations to prove the data layer without risk.        |
| **Online before batch**        | Online programs have simpler transaction boundaries; batch has multi-file atomicity challenges.   |
| **Coexistence is mandatory**   | Legacy and modern systems must run in parallel during each phase via the Anti-Corruption Layer.   |
| **Feature flags over big-bang**| Route traffic gradually using feature flags — not instant cutovers.                                |
| **Rollback always available**  | Every phase must support instant rollback to legacy within minutes.                                |
| **Data integrity verified**    | Reconciliation checks must pass at 100% before advancing to the next phase.                       |

---

## 2. Phase Overview

```
Timeline (estimated weeks):

Phase 1: Foundation                    ████████░░░░░░░░░░░░░░░░░░░░░░  Weeks 1-4
Phase 2: Read-Only Services            ░░░░░░░░████████░░░░░░░░░░░░░░  Weeks 5-8
Phase 3: Write Services (Online)       ░░░░░░░░░░░░░░░░████████░░░░░░  Weeks 9-14
Phase 4: Batch Processing              ░░░░░░░░░░░░░░░░░░░░░░░░██████  Weeks 15-22
Phase 5: Final Cutover & Decommission  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░██  Weeks 23-26

Risk Level:   LOW ─────────────────────────────────── HIGH
```

| Phase | Name                     | Risk  | Programs Migrated                                    | Success Criteria                     |
|-------|--------------------------|-------|------------------------------------------------------|--------------------------------------|
| 1     | Foundation               | Low   | COSGN00C, COUSR00C–03C, COMEN01C, COADM01C         | Auth works, admin CRUD passes        |
| 2     | Read-Only Services       | Low   | COACTVWC, COCRDLIC, COCRDSLC, COTRN00C, COTRN01C   | 100% data parity on reads            |
| 3     | Write Services (Online)  | Medium| COACTUPC, COCRDUPC, COTRN02C, COBIL00C, CORPT00C   | Write reconciliation passes          |
| 4     | Batch Processing         | High  | CBTRN01C–02C, CBACT04C, CBSTM03A/B, CBTRN03C       | Batch cycle parity verified          |
| 5     | Final Cutover            | Low   | CBEXPORT, CBIMPORT, cleanup, decommission           | All traffic on new platform          |

---

## 3. Phase 1 — Foundation

**Duration:** 4 weeks | **Risk:** Low

### 3.1 Scope

| Component               | Legacy Program(s) | Modern Replacement                          |
|-------------------------|--------------------|---------------------------------------------|
| Authentication          | COSGN00C           | Spring Security + OAuth2/OIDC + bcrypt      |
| User Administration     | COUSR00C–03C       | UserAdminController + UserRepository        |
| Regular Menu            | COMEN01C           | Web UI routing (React/Angular)              |
| Admin Menu              | COADM01C           | Web UI routing (React/Angular)              |

### 3.2 Prerequisites

- [ ] PostgreSQL provisioned with `users` table schema
- [ ] Spring Boot application skeleton deployed
- [ ] Spring Security configured with OAuth2 provider
- [ ] User data migrated from USRSEC VSAM → `users` table (password hashing migration)
- [ ] Web UI skeleton with login page, regular menu, admin menu
- [ ] Feature flag infrastructure (LaunchDarkly, Unleash, or Spring Cloud Config)

### 3.3 Cutover Steps

1. **Deploy modern auth service** alongside legacy CICS
2. **Migrate user data:** Extract USRSEC records → hash passwords with bcrypt → insert into `users` table
3. **Enable feature flag:** `auth.modern.enabled = true` for pilot group (internal users)
4. **Validate:** Login with pilot users through modern flow
5. **Expand:** Gradually increase traffic percentage (10% → 25% → 50% → 100%)
6. **Verify:** Admin CRUD operations pass on modern stack
7. **Gate:** All auth traffic on modern stack for 1 week with zero incidents

### 3.4 Rollback Trigger

- Authentication failure rate > 1%
- User unable to login within 5 seconds
- Any privilege escalation detected

### 3.5 Rollback Procedure

1. Set feature flag `auth.modern.enabled = false`
2. All traffic routes back to COSGN00C within 30 seconds
3. No data loss — USRSEC VSAM remains the source of truth during Phase 1

---

## 4. Phase 2 — Read-Only Services

**Duration:** 4 weeks | **Risk:** Low

### 4.1 Scope

| Component               | Legacy Program(s) | Modern Replacement                          |
|-------------------------|--------------------|---------------------------------------------|
| Account View            | COACTVWC           | `GET /api/accounts/{id}`                    |
| Card List               | COCRDLIC           | `GET /api/cards?accountId={id}&page={n}`    |
| Card Detail             | COCRDSLC           | `GET /api/cards/{cardNumber}`               |
| Transaction List        | COTRN00C           | `GET /api/transactions?page={n}`            |
| Transaction Detail      | COTRN01C           | `GET /api/transactions/{id}`                |

### 4.2 Prerequisites

- [ ] VSAM → PostgreSQL data migration completed for: ACCTDATA, CARDDATA, CARDXREF, CUSTDATA, TRANSACT
- [ ] VSAM-DB sync operational (bidirectional during coexistence)
- [ ] JPA entities and repositories for Account, Card, CardXref, Customer, Transaction
- [ ] Pagination support (Spring Data Pageable)
- [ ] API gateway routing rules configured

### 4.3 Data Migration Strategy

```
VSAM Files              →    PostgreSQL Tables
─────────────────────────────────────────────────
ACCTDATA (CVACT01Y)     →    accounts
CUSTDATA (CVCUS01Y)     →    customers
CARDDATA (CVACT02Y)     →    cards
CARDXREF (CVACT03Y)     →    card_xref
TRANSACT (CVTRA05Y)     →    transactions
TCATBAL  (CVTRA01Y)     →    category_balances
DISCGRP  (CVTRA02Y)     →    disclosure_groups
TRANTYPE (CVTRA03Y)     →    transaction_types
TRANCATG (CVTRA04Y)     →    transaction_categories
USRSEC   (CSUSR01Y)     →    users (already done in Phase 1)
```

### 4.4 Cutover Steps

1. **Deploy read-only APIs** behind feature flags
2. **Run differential tests:** Compare API responses vs. legacy screen data for 1,000+ records
3. **Enable feature flag** for pilot group on each endpoint
4. **Monitor:** Track response times, data accuracy, error rates
5. **Expand** to 100% traffic over 2 weeks
6. **Gate:** 100% data parity verified across all read endpoints

### 4.5 Reconciliation Checks

| Check                         | Method                                                         | Threshold |
|-------------------------------|----------------------------------------------------------------|-----------|
| Account record count          | `SELECT COUNT(*) FROM accounts` = VSAM ACCTDATA record count  | 100%      |
| Field-level data match        | Compare all fields for random 10% sample                      | 100%      |
| Card cross-reference integrity| Every card maps to a valid account                            | 100%      |
| Transaction count             | DB transaction count = VSAM TRANSACT count                    | 100%      |
| Response time                 | Modern API p99 < 200ms                                        | Pass      |

### 4.6 Rollback

- Set feature flags to `false` → traffic routes to legacy BMS screens
- No data risk — reads don't modify data

---

## 5. Phase 3 — Write Services (Online)

**Duration:** 6 weeks | **Risk:** Medium

### 5.1 Scope

| Component               | Legacy Program(s) | Modern Replacement                          |
|-------------------------|--------------------|---------------------------------------------|
| Account Update          | COACTUPC           | `PUT /api/accounts/{id}` — AccountUpdateService |
| Card Update             | COCRDUPC           | `PUT /api/cards/{cardNumber}` — CardUpdateService |
| Transaction Add         | COTRN02C           | `POST /api/transactions` — TransactionService |
| Bill Payment            | COBIL00C           | `POST /api/payments` — PaymentService       |
| Report Trigger          | CORPT00C           | `POST /api/reports` — ReportService         |

### 5.2 Prerequisites

- [ ] Read-only services fully cut over (Phase 2 gate passed)
- [ ] Write-path Spring `@Transactional` boundaries verified
- [ ] Audit logging implemented for all write operations
- [ ] Idempotency keys implemented for Payment and Transaction creation
- [ ] Validation services ported (AccountValidator, CardValidator, TransactionValidator)
- [ ] VSAM-DB sync switched to **DB-primary** mode (DB is source of truth, sync to VSAM for batch)

### 5.3 Critical: Source of Truth Switchover

During Phase 3, the **database becomes the source of truth** for online operations:

```
Phase 2:  VSAM ──sync──▶ DB (VSAM primary)
Phase 3:  DB ──sync──▶ VSAM (DB primary — VSAM kept for batch until Phase 4)
```

This is the highest-risk transition in the entire migration. The VSAM-DB sync must be reversed.

### 5.4 Cutover Steps

1. **Deploy write services** behind feature flags (disabled)
2. **Shadow-write testing:** For 2 weeks, run modern writes in shadow mode — capture the result but don't commit; compare against what legacy would have produced
3. **Enable for pilot group:** Internal users perform account/card/transaction updates via modern UI
4. **Dual-write period (1 week):** Both modern DB and legacy VSAM receive writes via sync
5. **Switch to DB-primary:** Disable legacy VSAM writes; sync DB→VSAM for batch compatibility
6. **Expand** to 100% over 2 weeks with careful monitoring
7. **Gate:** Zero data discrepancies for 1 week

### 5.5 Account Update (COACTUPC) — Special Handling

COACTUPC is the #1 complexity hotspot (4,236 LOC). Special measures:

| Measure                    | Detail                                                           |
|---------------------------|------------------------------------------------------------------|
| **Decomposition testing** | Validate each extracted service independently before integration |
| **Field-by-field parity** | Compare every field of Account + Customer record after update    |
| **Validation rule parity**| Port all validation rules and verify identical accept/reject behavior |
| **Performance baseline**  | Measure p95 latency; modern must be within 2× of legacy         |
| **Canary deployment**     | 1% traffic for 48 hours before expansion                        |

### 5.6 Rollback

- Set feature flags to `false`
- Switch VSAM back to primary (sync DB→VSAM disabled, VSAM→DB re-enabled)
- Reconcile any writes that occurred during the modern-primary window
- **RTO:** 15 minutes for flag switch; up to 2 hours for full data reconciliation

---

## 6. Phase 4 — Batch Processing

**Duration:** 8 weeks | **Risk:** High

### 6.1 Scope

| Component                  | Legacy Program(s)    | Modern Replacement                              |
|---------------------------|---------------------|-------------------------------------------------|
| Transaction Validation     | CBTRN01C            | DailyTransactionValidationJob (Spring Batch)    |
| Transaction Posting        | CBTRN02C            | TransactionPostingJob (Spring Batch)            |
| Interest Calculation       | CBACT04C            | InterestCalculationJob (Spring Batch)           |
| Statement Generation       | CBSTM03A/B          | StatementGenerationJob (Spring Batch)           |
| Daily Report               | CBTRN03C            | DailyTransactionReportJob (Spring Batch)        |
| Batch Account Processing   | CBACT01C–03C        | Spring Batch equivalents                        |
| Customer Read              | CBCUS01C            | Spring Batch equivalent                         |

### 6.2 Prerequisites

- [ ] All online write services fully cut over (Phase 3 gate passed)
- [ ] Spring Batch infrastructure deployed (JobLauncher, JobRepository, scheduling)
- [ ] VSAM-DB sync decommissioned (DB is sole data store)
- [ ] Batch job scheduling configured (replaces JCL job scheduling)
- [ ] Financial precision tests passing (BigDecimal with correct rounding modes)
- [ ] Golden-file test suite for all batch jobs (known inputs → expected outputs)

### 6.3 Nightly Cycle Migration

The legacy nightly cycle (JCL jobs) maps to a Spring Batch job chain:

| Step | Legacy JCL Job  | Legacy Program | Modern Spring Batch Job              | Risk    |
|------|----------------|----------------|--------------------------------------|---------|
| 1    | CLOSEFIL       | (JCL only)     | **Eliminated** — DB handles concurrency| None    |
| 2    | POSTTRAN       | CBTRN02C       | TransactionPostingJob                | **Critical** |
| 3    | INTCALC        | CBACT04C       | InterestCalculationJob               | **Critical** |
| 4    | TRANBKP        | (JCL only)     | Database backup / pg_dump            | Low     |
| 5    | COMBTRAN       | (JCL only)     | `INSERT INTO transactions SELECT ... FROM daily_transactions` | Low |
| 6    | CREASTMT       | CBSTM03A/B     | StatementGenerationJob               | Medium  |
| 7    | DAILYTRAN      | CBTRN03C       | DailyTransactionReportJob            | Low     |
| 8    | TRANIDX        | (JCL only)     | **Eliminated** — DB indexes are automatic | None |
| 9    | OPENFIL        | (JCL only)     | **Eliminated** — DB handles concurrency| None    |

### 6.4 Cutover Steps

1. **Deploy Spring Batch jobs** in shadow mode (run both legacy and modern, compare outputs)
2. **Run parallel for 2 weeks:** Both JCL batch cycle and Spring Batch jobs execute nightly
3. **Reconcile outputs:** Compare transaction counts, balance totals, statement content
4. **Financial verification:** Auditor validates interest calculations match to the cent
5. **Switch to modern batch:** Disable JCL scheduling; Spring Batch becomes primary
6. **Monitor for 2 weeks:** Daily reconciliation checks
7. **Gate:** 14 consecutive days of zero discrepancies

### 6.5 Interest Calculation — Special Handling

| Measure                     | Detail                                                           |
|----------------------------|------------------------------------------------------------------|
| **Precision testing**       | Verify interest amounts match to 2 decimal places across 10,000+ accounts |
| **Rounding mode alignment** | Confirm `BigDecimal.ROUND_HALF_UP` matches COBOL `COMPUTE ROUNDED` |
| **Edge cases**              | Test zero-balance accounts, negative balances, max credit limits  |
| **Regulatory review**       | Have compliance team verify calculations meet TILA/Reg Z requirements |

### 6.6 Rollback

- Re-enable JCL batch scheduling on mainframe
- Spring Batch jobs set to inactive
- **RTO:** 4 hours (must wait for current batch cycle to complete)
- **Data recovery:** If modern batch corrupted data, restore from pre-batch database backup

---

## 7. Phase 5 — Final Cutover & Decommission

**Duration:** 4 weeks | **Risk:** Low (cleanup only)

### 7.1 Scope

| Task                          | Detail                                                         |
|-------------------------------|----------------------------------------------------------------|
| Data Migration Utilities      | Convert CBEXPORT/CBIMPORT to Spring Batch import/export jobs   |
| ACL Decommission              | Remove VSAM-DB sync, API gateway legacy routing rules          |
| CICS Region Shutdown          | Decommission CICS transaction definitions (CC00, etc.)         |
| VSAM File Archival            | Archive VSAM KSDS clusters to cold storage                     |
| JCL Cleanup                   | Archive all 38 JCL jobs                                        |
| Feature Flag Cleanup          | Remove all migration feature flags from code                   |
| Documentation Update          | Update operational runbooks for new stack                       |

### 7.2 Final Verification

- [ ] All 150+ legacy artifacts accounted for (migrated, eliminated, or archived)
- [ ] No traffic flowing to legacy CICS region for 7+ days
- [ ] All batch jobs running exclusively on Spring Batch for 14+ days
- [ ] Performance baselines met or exceeded
- [ ] Penetration test passed on modern stack
- [ ] Disaster recovery test passed

---

## 8. Rollback Strategy

### Rollback Hierarchy

| Level          | Scope                        | Method                              | RTO       |
|---------------|------------------------------|-------------------------------------|-----------|
| **Feature**   | Single endpoint/service      | Feature flag toggle                 | < 1 min   |
| **Phase**     | Entire phase's services      | Bulk feature flag group toggle      | < 5 min   |
| **Full**      | Return to 100% legacy        | Re-enable CICS + JCL; VSAM primary | < 4 hours |

### Rollback Data Recovery

| Phase | Data Recovery Approach                                                      |
|-------|-----------------------------------------------------------------------------|
| 1     | USRSEC remains authoritative; no recovery needed                            |
| 2     | Read-only; no data modified; no recovery needed                             |
| 3     | Replay unsynced DB writes to VSAM via reconciliation tool                   |
| 4     | Restore pre-batch DB backup + re-run legacy JCL batch cycle                 |
| 5     | N/A — legacy already decommissioned; forward-fix only                       |

---

## 9. Success Gates

Each phase must pass ALL gates before the next phase can begin:

### Phase 1 → Phase 2 Gate

- [ ] 100% of pilot users can authenticate via modern stack
- [ ] Admin CRUD operations pass functional tests
- [ ] Zero authentication failures in production for 7 days
- [ ] Security scan passed (no plaintext passwords, no OWASP Top 10 issues)

### Phase 2 → Phase 3 Gate

- [ ] 100% data parity between API responses and VSAM data (sampled verification)
- [ ] Read API p99 latency < 200ms
- [ ] All reconciliation checks passing for 14 days
- [ ] Load test: modern APIs handle 2× peak legacy throughput

### Phase 3 → Phase 4 Gate

- [ ] Zero data discrepancies between DB and VSAM for 14 days
- [ ] All write operations produce audit trail entries
- [ ] Idempotency verified for payment and transaction creation
- [ ] Validation rule parity: identical accept/reject on 1,000+ test cases
- [ ] Account update (COACTUPC replacement) handles all field combinations

### Phase 4 → Phase 5 Gate

- [ ] 14 consecutive nightly batch cycles with zero discrepancies
- [ ] Interest calculations match to the cent across entire portfolio
- [ ] Statements generated match legacy format and content
- [ ] Batch job completion time within 1.5× of legacy
- [ ] Regulatory/compliance team sign-off on financial calculations

---

## 10. Timeline Estimate

| Phase | Duration     | Start  | End    | Key Milestone                          |
|-------|-------------|--------|--------|----------------------------------------|
| 1     | 4 weeks     | Week 1 | Week 4 | Modern auth live for all users         |
| 2     | 4 weeks     | Week 5 | Week 8 | All read APIs serving 100% traffic     |
| 3     | 6 weeks     | Week 9 | Week 14| DB is source of truth for online ops   |
| 4     | 8 weeks     | Week 15| Week 22| Spring Batch runs nightly cycle alone  |
| 5     | 4 weeks     | Week 23| Week 26| Legacy decommissioned                  |
| **Total** | **26 weeks** | | | **Full migration complete** |

### Risk-Adjusted Timeline

| Scenario       | Duration  | Assumption                                                    |
|---------------|-----------|---------------------------------------------------------------|
| Optimistic    | 20 weeks  | No rollbacks, clean data migration, all tests pass first try  |
| **Expected**  | **26 weeks** | **1–2 minor rollbacks, some data cleanup, normal debugging** |
| Pessimistic   | 36 weeks  | Major rollback in Phase 3 or 4, data integrity issues, re-work |

### Team Composition (Recommended)

| Role                      | Count | Phases    |
|--------------------------|-------|-----------|
| Java/Spring Backend Dev  | 3     | All       |
| Frontend Dev             | 1     | 1–3       |
| COBOL SME                | 1     | All (advisory, part-time after Phase 2) |
| QA/Test Engineer         | 2     | All       |
| DBA                      | 1     | 2–4       |
| DevOps/Platform          | 1     | All       |
| Business Analyst         | 1     | 3–4 (validation of business rules) |
