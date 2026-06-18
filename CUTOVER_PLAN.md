# CardDemo Cutover Plan

## Overview

This plan sequences the CardDemo modernization into five phases. Each phase specifies which programs migrate, affected data stores, required integration bridges, rollback procedures, and acceptance criteria for phase advancement. The first phase is the lowest-risk, highest-value extraction.

---

## Phase Sequencing Rationale

The phases are ordered by:
1. **Risk (ascending):** Start with isolated, low-coupling domains; finish with the most tightly coupled.
2. **Value (descending within risk tier):** Prioritize areas where modernization yields immediate operational improvement.
3. **Dependency chain:** Each phase produces the API surface needed by subsequent phases.

```
Phase 1          Phase 2                Phase 3                  Phase 4            Phase 5
Identity ──→ Reference Data ──→ Account + Card (Strangler) ──→ Transactions ──→ Reporting +
& Auth        & Transaction          + XREF                   + Batch Jobs       Optional
              Type Admin                                       + Interest         Modules
                                                               + Bill Pay
```

---

## Phase 1: Identity & Access Management

**Timeline:** Weeks 1-4

**Rationale:** Lowest-risk extraction. USRSEC is exclusively owned by this domain. The only outbound dependency is a user-type flag. Replaces plaintext passwords with modern auth. High immediate security value.

### Programs Migrating

| Program | Function | New Component |
|---|---|---|
| `COSGN00C` | Sign-on | `POST /auth/login` (Spring Security + JWT) |
| `COADM01C` | Admin menu | Admin dashboard SPA route |
| `COUSR00C` | User list | `GET /users` |
| `COUSR01C` | Add user | `POST /users` |
| `COUSR02C` | Update user | `PUT /users/{id}` |
| `COUSR03C` | Delete user | `DELETE /users/{id}` |

### Data Stores Affected

| Store | Action | Notes |
|---|---|---|
| `USRSEC` (VSAM KSDS) | Migrate → PostgreSQL `users` table | One-time ETL. Hash existing passwords (or force reset). |
| Copybook `CSUSR01Y` | Map → JPA `User` entity | 80-byte record → relational schema |
| BMS maps `COSGN00`, `COUSR00-03`, `COADM01` | Retire | Replaced by API + frontend |

### Integration Bridges

| Bridge | Description | Duration |
|---|---|---|
| **Auth Token Bridge** | CICS programs that read `CDEMO-USER-TYPE` from the commarea will instead receive it from a new CICS bridge program that calls the identity-service `/auth/validate` endpoint and populates the commarea. | Until Phase 3 completes |
| **VSAM USRSEC Sync** | During transition, write-through to both PostgreSQL and USRSEC so legacy programs still function. | Phase 1 only |

### Rollback Plan

- **Trigger:** Auth service fails health checks, or bridge program produces incorrect user-type flags.
- **Steps:**
  1. Route all traffic back to legacy `COSGN00C`.
  2. Disable write-through; USRSEC becomes authoritative again.
  3. No data loss: USRSEC was kept in sync during the phase.
- **Recovery Time:** < 1 hour (CICS program swap via PCT table update).

### Acceptance Criteria

- [ ] All existing users authenticate via the new identity-service.
- [ ] JWT tokens contain correct `user_type` (admin/user) claim.
- [ ] Bridge program correctly populates commarea for all legacy CICS programs.
- [ ] Write-through keeps USRSEC and PostgreSQL in sync (reconciliation job passes 100%).
- [ ] No increase in CICS transaction failure rates (ABENDs).
- [ ] User CRUD operations function correctly (add/update/delete verified).

---

## Phase 2: Reference Data & Transaction Type Administration

**Timeline:** Weeks 5-8

**Rationale:** Transaction type and category data are small lookup tables with low coupling. The DB2-based transaction type module is already SQL-native, making rewrite straightforward. This phase establishes the reference data APIs that Phase 3 and 4 depend on.

### Programs Migrating

| Program | Function | New Component |
|---|---|---|
| `COTRTLIC` | Transaction type list (DB2) | `GET /reference/transaction-types` |
| `COTRTUPC` | Transaction type edit (DB2) | `PUT /reference/transaction-types/{code}` |
| `COBTUPDT` | Batch type maintenance | Spring Batch job |

### Data Stores Affected

| Store | Action | Notes |
|---|---|---|
| DB2 `TRNTYPE` table | Migrate → PostgreSQL `transaction_types` table | Schema nearly identical |
| DB2 `TRNTYCAT` table | Migrate → PostgreSQL `transaction_categories` table | Schema nearly identical |
| `TRANTYPE` VSAM | Retire after Phase 4 | Maintained via sync bridge until then |
| `TRANCATG` VSAM | Retire after Phase 4 | Maintained via sync bridge until then |
| `DISCGRP` VSAM | Migrate → PostgreSQL `disclosure_groups` table | Used by interest calc in Phase 4 |

### Integration Bridges

| Bridge | Description | Duration |
|---|---|---|
| **VSAM Sync Job** | Replace the existing `TRANEXTR` (DB2→VSAM extract) with a new sync job that writes from PostgreSQL → VSAM, maintaining compatibility for legacy batch programs. | Until Phase 4 completes |
| **Control-M Wrapper** | Keep the WEEKLY-TransactionTypesDBRefresh and WEEKLY-DisclosureGroupsRefresh Control-M folders, but redirect them to call the new reference-data-service API. | Until Phase 4 completes |

### Rollback Plan

- **Trigger:** Reference data API returns incorrect type/category data, or VSAM sync produces data mismatches.
- **Steps:**
  1. Restore DB2 tables from backup (point-in-time recovery).
  2. Re-enable original `TRANEXTR` DB2→VSAM extract job.
  3. Revert CICS PCT entries for `COTRTLIC`/`COTRTUPC` to legacy programs.
- **Recovery Time:** < 2 hours.

### Acceptance Criteria

- [ ] All transaction types and categories accessible via REST API.
- [ ] VSAM sync job produces byte-identical records to legacy `TRANEXTR`.
- [ ] Disclosure group interest rates match exactly (decimal precision verified).
- [ ] Admin UI for type management functions correctly (add/update/delete).
- [ ] Batch programs (`CBTRN02C`, `CBTRN03C`, `CBACT04C`) still read VSAM files correctly.
- [ ] Weekly Control-M schedule executes without errors.

---

## Phase 3: Account & Credit Card Management (Strangler)

**Timeline:** Weeks 9-18

**Rationale:** This is the core domain with the highest data coupling. The strangler pattern allows incremental extraction while the legacy system remains operational. Account and Card are co-extracted because they share the XREF junction file.

### Programs Migrating

| Program | Function | New Component |
|---|---|---|
| `COACTVWC` | Account view | `GET /accounts/{id}` |
| `COACTUPC` | Account update | `PUT /accounts/{id}` |
| `COCRDLIC` | Card list | `GET /accounts/{id}/cards` |
| `COCRDSLC` | Card detail | `GET /cards/{number}` |
| `COCRDUPC` | Card update | `PUT /cards/{number}` |

### Data Stores Affected

| Store | Action | Notes |
|---|---|---|
| `ACCTDAT` VSAM | Migrate → PostgreSQL `accounts` table | 300-byte records. Must maintain backward compat for batch (Phase 4). |
| `CUSTDAT` VSAM | Migrate → PostgreSQL `customers` table | 500-byte records. |
| `CARDDAT` VSAM + `CARDAIX` | Migrate → PostgreSQL `cards` table with indexes | 150-byte records. |
| `CARDXREF` + `CXACAIX` | Migrate → PostgreSQL `card_xref` table | 50-byte junction records. Foreign keys enforced in DB. |
| BMS maps `COACTVW`, `COACTUP`, `COCRDLI`, `COCRDSL`, `COCRDUP` | Retire | Replaced by API + frontend |

### Integration Bridges

| Bridge | Description | Duration |
|---|---|---|
| **VSAM Shadow Write** | Every write to the new PostgreSQL tables is also written to VSAM files, keeping them current for batch programs that haven't migrated yet (CBTRN02C, CBACT04C, CBSTM03A). | Until Phase 4 completes |
| **CICS API Adapter** | A thin CICS COBOL program that translates EXEC CICS calls to HTTP REST calls to account-service and card-service. Wired into the menu dispatcher. | Until CICS is fully decommissioned |
| **XREF API** | `GET /xref?card={num}` returns account + customer IDs. Used by batch bridge for transaction posting. | Permanent (becomes part of account-service) |

### Rollback Plan

- **Trigger:** Shadow write latency exceeds 100ms or produces data drift; account/card API returns incorrect data.
- **Steps:**
  1. Disable API adapter in CICS menu; re-enable legacy programs.
  2. Stop shadow writes; VSAM becomes authoritative.
  3. Run reconciliation job to identify any PostgreSQL→VSAM drift.
  4. If PostgreSQL has newer data: run reverse sync to bring VSAM current.
- **Recovery Time:** 2-4 hours (reconciliation may take time for large datasets).

### Acceptance Criteria

- [ ] All 11 account record fields survive round-trip (write via API, read from VSAM shadow, compare).
- [ ] All 6 card record fields survive round-trip.
- [ ] XREF lookups return correct account and customer for every card number.
- [ ] Pagination on card list returns identical results to legacy `COCRDLIC` STARTBR/READNEXT.
- [ ] Account update validation rules (SSN, phone, date) behave identically (test with known edge cases: SSN starting with 666, Feb 29 dates, etc.).
- [ ] Shadow write lag < 50ms (P99).
- [ ] Batch programs (`CBTRN02C`, `CBACT04C`) complete successfully using VSAM shadow files.
- [ ] Credit limit enforcement matches legacy behavior exactly.

---

## Phase 4: Transaction Processing & Batch Pipeline

**Timeline:** Weeks 19-30

**Rationale:** This is the highest-risk, highest-complexity phase. The batch pipeline (POSTTRAN → INTCALC → COMBTRAN → CREASTMT) is a tightly sequenced daily/monthly process. A parallel-run approach validates the new pipeline against the legacy one before cutover.

### Programs Migrating

| Program | Function | New Component |
|---|---|---|
| `COTRN00C` | Transaction list | `GET /transactions` |
| `COTRN01C` | Transaction view | `GET /transactions/{id}` |
| `COTRN02C` | Transaction add (CICS) | `POST /transactions` |
| `COBIL00C` | Bill payment | `POST /payments` |
| `CBTRN01C` | Daily posting (alternate) | Spring Batch `DailyPostingJob` |
| `CBTRN02C` | Daily posting (primary) | Spring Batch `DailyPostingJob` |
| `CBACT04C` | Interest calculation | Spring Batch `InterestCalcJob` |

### Data Stores Affected

| Store | Action | Notes |
|---|---|---|
| `TRANSACT` VSAM | Migrate → PostgreSQL `transactions` table | 350-byte records, ~millions of rows. |
| `DALYTRAN` sequential | Replace with Kafka topic `daily-transactions` or API ingestion | External feed interface. |
| `DALYREJS` sequential | Replace with `rejects` table + dead letter queue | Error handling. |
| `TCATBALF` VSAM | Migrate → PostgreSQL `category_balances` table | Composite key: account + type + category. |
| GDG datasets | Replace with S3 versioned bucket for backups | Daily/monthly snapshots. |

### Integration Bridges

| Bridge | Description | Duration |
|---|---|---|
| **Parallel Run Pipeline** | Run both legacy batch (JCL) and new Spring Batch jobs on the same day's input. Compare outputs record-by-record. Legacy remains authoritative until 30 consecutive days of zero discrepancies. | 4-8 weeks |
| **CLOSEFIL/OPENFIL Bridge** | Legacy batch requires CICS VSAM files to be closed. During parallel run, a script orchestrates: close VSAM → run legacy batch → reopen VSAM → run new batch → compare. | Until legacy batch is decommissioned |
| **Balance Update Bridge** | New transaction-service calls account-service API to update balances (replaces direct ACCTDAT WRITE in CBTRN02C). | Permanent (correct architecture) |

### Rollback Plan

- **Trigger:** Parallel run shows discrepancies in transaction posting, interest calculation amounts, or balance updates.
- **Steps:**
  1. Stop new batch jobs; legacy batch remains authoritative (it never stopped during parallel run).
  2. Revert online transaction screens to legacy CICS programs.
  3. Investigate discrepancies using the parallel-run comparison reports.
  4. Fix, re-deploy, and restart parallel run from day 1.
- **Recovery Time:** < 1 hour for online; batch reverts to next-day run.
- **Data Safety:** Legacy batch output is always preserved in GDG (or S3). No data loss possible during parallel run.

### Acceptance Criteria

- [ ] Parallel run: 30 consecutive days of zero discrepancies between legacy and new batch outputs.
- [ ] Transaction posting produces identical records (field-by-field comparison including timestamps).
- [ ] Interest calculation amounts match to the cent across all accounts.
- [ ] Category balance totals match after each daily/monthly run.
- [ ] Reject file records match (same transactions rejected, same reason codes).
- [ ] Bill payment updates account balance correctly and creates matching transaction record.
- [ ] Daily throughput: new pipeline processes same volume as legacy within the batch window.
- [ ] Control-M schedule replaced by equivalent Kubernetes CronJob or Spring Scheduler configuration.
- [ ] GDG backup strategy replaced by S3 versioned bucket with equivalent retention.

---

## Phase 5: Reporting, Data Migration, & Optional Modules

**Timeline:** Weeks 31-40

**Rationale:** Reporting is read-only and can be migrated with low risk now that all source data is in PostgreSQL. Optional modules (IMS/DB2/MQ) are migrated last because they have the most infrastructure-specific dependencies and may not be needed in all deployments.

### Sub-Phase 5A: Reporting & Statements (Weeks 31-34)

| Program | Function | New Component |
|---|---|---|
| `CORPT00C` | Report request | `POST /reports/generate` |
| `CBTRN03C` | Transaction report | Reporting service job |
| `CBSTM03A` | Statement generation | PDF/HTML generation service |
| `CBSTM03B` | I/O subroutine | Eliminated (inlined) |

**Data Stores:** All source data now in PostgreSQL (from Phases 3-4). Output switches from GDG/sequential files to S3-stored PDF/HTML.

**Integration Bridges:**
- **TDQ Elimination:** `CORPT00C`'s internal reader pattern (write JCL to TDQ) is replaced by a REST call: `POST /reports/generate` with report type and parameters.
- None needed — reporting reads from the same PostgreSQL tables the other services write to.

**Rollback:** Re-enable legacy batch report jobs; they still have access to VSAM shadow files from Phase 3/4 bridges.

**Acceptance Criteria:**
- [ ] Generated statements match legacy output character-by-character (text format) or visually (PDF).
- [ ] Transaction detail reports contain identical data.
- [ ] Report generation time is within acceptable SLA.

### Sub-Phase 5B: Data Migration Toolkit (Weeks 33-36, overlapping)

| Program | Function | New Component |
|---|---|---|
| `CBEXPORT` | Branch data export | Spring Batch export job + JSON/Parquet output |
| `CBIMPORT` | Branch data import | Spring Batch import job + validation |

**Data Stores:** Sequential export file (500-byte REDEFINES) → JSON or Parquet format.

**Integration Bridges:** None — this replaces a one-off migration utility.

**Acceptance Criteria:**
- [ ] Export→Import round-trip produces identical data in PostgreSQL.
- [ ] COMP-3 and COMP fields decode correctly (validated against ASCII sample data in `app/data/ASCII/`).

### Sub-Phase 5C: Optional Modules (Weeks 35-40, if applicable)

| Module | Programs | New Component |
|---|---|---|
| Authorization (IMS/DB2/MQ) | `COPAUA0C`, `COPAUS0C`, `COPAUS1C`, `COPAUS2C`, `CBPAUP0C` | `authorization-service` (Spring Boot + Kafka) |
| Account Extraction (MQ/VSAM) | `COACCT01`, `CODATE01` | REST endpoints on account-service |

**Integration Bridges:**
- **MQ→Kafka Bridge:** Run a MQ-Kafka connector during transition if external systems still send MQ messages.
- **IMS→PostgreSQL:** One-time migration of IMS hierarchical segments to relational tables.

**Rollback:** These modules are optional and can remain on legacy infrastructure if migration proves problematic.

**Acceptance Criteria:**
- [ ] Authorization requests processed end-to-end via new service.
- [ ] MQ message format compatibility verified (or external producers updated).
- [ ] IMS data accessible via JPA queries with correct hierarchical relationships preserved.

---

## Phase 6: Decommission Legacy Infrastructure

**Timeline:** Weeks 41-44

This is not a migration phase but a cleanup phase.

### Steps
1. Stop VSAM shadow writes (all data now in PostgreSQL).
2. Disable CICS API adapter bridge programs.
3. Archive VSAM files to cold storage (compliance retention).
4. Decommission CICS region and associated resources.
5. Decommission Control-M schedules (replaced by Kubernetes CronJobs).
6. Decommission DB2 instance (if applicable).
7. Decommission IMS and MQ infrastructure (if applicable).
8. Remove JCL job definitions from production libraries.

### Acceptance Criteria
- [ ] No CICS transactions processed for 30 days.
- [ ] No batch jobs executed from JCL for 30 days.
- [ ] All data retrievable from PostgreSQL (spot-check 100 random records per entity type against archived VSAM).
- [ ] Monitoring shows zero traffic to legacy endpoints.

---

## Timeline Summary

```
Weeks:    1-4      5-8      9-18       19-30        31-40      41-44
          │        │        │          │            │          │
Phase 1:  ████                                                         Identity
Phase 2:           ████                                                Reference Data
Phase 3:                    ██████████                                 Account + Card
Phase 4:                               ████████████                    Transactions
Phase 5:                                            ██████████         Reporting + Optional
Phase 6:                                                       ████   Decommission
```

**Total Duration:** ~44 weeks (11 months)

---

## Cross-Phase Dependencies

| Dependency | Producer Phase | Consumer Phase | Bridge Required |
|---|---|---|---|
| JWT tokens for auth | Phase 1 | All subsequent | Auth Token Bridge |
| Reference data API | Phase 2 | Phases 3, 4, 5 | VSAM Sync Job |
| Account/Card/XREF API | Phase 3 | Phase 4 | VSAM Shadow Write |
| Transaction data in PostgreSQL | Phase 4 | Phase 5 | None (direct DB access) |
| CLOSEFIL/OPENFIL coordination | Legacy | Phase 4 parallel run | Script wrapper |
