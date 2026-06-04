# Cutover Plan

## Overview

This document defines a phased migration sequence for the CardDemo mainframe application, ordered from **lowest-risk to highest-risk**. Each phase has entry criteria, deliverables, validation gates, and rollback procedures.

---

## Phasing Principles

1. **Start at the edges** — migrate components with fewest upstream/downstream dependencies first.
2. **Read-before-write** — migrate read-only consumers before write-path owners.
3. **Prove the platform** — Phase 1 establishes the target platform, CI/CD, and observability baseline.
4. **Parallel-run everything financial** — any component touching monetary values runs old+new simultaneously.
5. **One context at a time** — never migrate two tightly-coupled contexts in the same phase.

---

## Phase 0: Foundation (Pre-Migration)

**Duration:** 4–6 weeks
**Risk Level:** None (no production changes)

### Deliverables

| # | Deliverable | Purpose |
|---|-------------|---------|
| 0.1 | Target platform provisioned (AWS/Azure/GCP) | Cloud infrastructure |
| 0.2 | CI/CD pipeline for Java/Spring Boot services | Build/deploy automation |
| 0.3 | Database provisioned (PostgreSQL/Aurora) | Target data store |
| 0.4 | Schema design for all VSAM → relational mappings | Data migration foundation |
| 0.5 | Observability stack (metrics, logs, traces) | Operational readiness |
| 0.6 | Anti-corruption layer (ACL) framework | Legacy ↔ modern bridge |
| 0.7 | Test harness: COBOL output capture framework | Regression baseline |
| 0.8 | Data migration tooling (VSAM extract → SQL load) | Reusable ETL pipeline |

### Exit Criteria
- [ ] Can deploy a "hello world" Spring Boot service end-to-end
- [ ] Can extract VSAM data to relational tables with full fidelity
- [ ] Automated regression test captures output of all 33 COBOL programs

---

## Phase 1: Low-Risk Leaf Nodes

**Duration:** 6–8 weeks
**Risk Level:** Low
**Components:** Identity & Access, Reporting & Statements, Branch Migration (Export/Import)

### 1A: Identity & Access Service

**Source:** `COSGN00C`, `COUSR00C–03C` (~1,900 LOC)
**Target:** Spring Security + OAuth2/JWT auth service

| Step | Action | Validation |
|------|--------|------------|
| 1 | Migrate USRSEC VSAM → `users` table | Row count + field-level comparison |
| 2 | Implement auth service (login, user CRUD) | Unit + integration tests |
| 3 | Deploy ACL: intercept CICS CC00 transaction | Both old and new auth succeed for same credentials |
| 4 | Route new UI to auth service | Login flow works end-to-end |
| 5 | Decommission COSGN00C/COUSR* from CICS | Verify no 3270 sessions use old path |

**Rollback:** Re-enable CICS sign-on program; ACL falls back to legacy.

### 1B: Reporting & Statements Service

**Source:** `CORPT00C`, `CBTRN03C`, `CBSTM03A/B` (~2,450 LOC)
**Target:** Reporting microservice (JasperReports or custom PDF/HTML generator)

| Step | Action | Validation |
|------|--------|------------|
| 1 | Extract TRANSACT + ACCTDAT data to reporting DB | Data parity check |
| 2 | Implement statement generator (text + HTML + PDF) | Output diff vs. COBOL-generated statements |
| 3 | Implement transaction report generator | Column-for-column output comparison |
| 4 | Deploy as scheduled job (replaces CORPT00C TDQ submission) | Reports generated on schedule |
| 5 | Decommission batch reporting JCL | Monitor for missing report complaints |

**Rollback:** Re-enable batch JCL reporting jobs in Control-M.

### 1C: Branch Migration (Export/Import) Service

**Source:** `CBEXPORT`, `CBIMPORT` (~1,070 LOC)
**Target:** Spring Batch job or lightweight ETL service

| Step | Action | Validation |
|------|--------|------------|
| 1 | Implement export: read all master tables → JSON/CSV | Byte-level comparison of exported data |
| 2 | Implement import: ingest file → validate → write to DB | Round-trip: export → import → compare |
| 3 | Retire CBEXPORT/CBIMPORT JCL | Functional test of branch migration |

**Rollback:** JCL jobs remain available; no production dependency.

### Phase 1 Exit Criteria
- [ ] Three services deployed to production and handling live traffic
- [ ] Zero auth failures for 2 consecutive weeks
- [ ] Report output matches legacy output with < 0.01% variance
- [ ] Platform operational maturity proven (alerting, on-call, runbooks)

---

## Phase 2: Medium-Risk Components

**Duration:** 8–10 weeks
**Risk Level:** Medium
**Components:** Customer Management, Card Management, Billing & Payments, New UI Shell

### 2A: Customer Management Service

**Source:** `CBCUS01C`, customer data operations
**Target:** Spring Boot CRUD service + PostgreSQL

| Step | Action | Validation |
|------|--------|------------|
| 1 | Migrate CUSTDAT VSAM → `customers` table | 500-byte record → normalized columns |
| 2 | Implement Customer API (CRUD + search) | Field-level comparison with VSAM reads |
| 3 | Deploy ACL: CICS customer reads proxy to new service | Transparent to online programs |
| 4 | Dual-write: VSAM + DB for transition period | Consistency monitor catches drift |
| 5 | Cut over: disable VSAM writes, service is system of record | Customer lookups stable for 1 week |

**Rollback:** Disable dual-write; VSAM reverts to primary.

### 2B: Card Management Service

**Source:** `COCRDLIC`, `COCRDSLC`, `COCRDUPC` (~4,500 LOC combined)
**Target:** Card Service with CCXREF ownership

| Step | Action | Validation |
|------|--------|------------|
| 1 | Migrate CARDDAT + CCXREF + alternate indexes → relational tables | Index coverage, row counts |
| 2 | Implement Card API (list, detail, update) | Admin/user role parity with legacy |
| 3 | Implement XREF lookup API (card→account, account→cards) | Response matches VSAM AIX browse |
| 4 | Deploy ACL: intercept CICS card transactions | Parallel execution comparison |
| 5 | Cut over card operations to new service | Monitor for orphaned XREF records |

**Rollback:** ACL routes back to CICS programs; VSAM remains consistent.

### 2C: Billing & Payments Service

**Source:** `COBIL00C` (572 LOC)
**Target:** Payment orchestration service (saga pattern)

| Step | Action | Validation |
|------|--------|------------|
| 1 | Implement payment command (creates txn + updates balance) | Amount accuracy to 2 decimal places |
| 2 | Integrate with Account Service (balance update) and Txn Service (payment record) | End-to-end payment flow |
| 3 | Deploy behind ACL; bill payment routes to new service | A/B comparison on test accounts |
| 4 | Full cutover after 1-week parallel run | Balance reconciliation passes |

**Rollback:** Route payments back to COBIL00C; reconcile any diverged balances manually.

### 2D: Modern UI Shell

**Target:** React/Angular SPA consuming new service APIs

| Step | Action | Validation |
|------|--------|------------|
| 1 | Build auth-integrated UI shell with navigation | Login → menu → logout flow |
| 2 | Integrate customer, card, and billing views | Feature parity with BMS maps |
| 3 | Soft launch to subset of users | User acceptance testing |
| 4 | Full rollout; BMS maps deprecated | Zero 3270 terminal sessions |

**Rollback:** BMS maps remain available via CICS; users can fall back to terminals.

### Phase 2 Exit Criteria
- [ ] Customer, Card, and Billing services in production
- [ ] CCXREF integrity maintained (zero orphans)
- [ ] Billing produces correct balances for 30 consecutive days
- [ ] Modern UI handles 100% of user/admin workflows for these contexts
- [ ] VSAM dual-write disabled; database is system of record for Customer + Card

---

## Phase 3: High-Risk Core

**Duration:** 10–14 weeks
**Risk Level:** High
**Components:** Account Management, Transaction Processing, Batch Operations

### 3A: Account Management Service

**Source:** `COACTUPC` (4,236 LOC), `COACTVWC`, `CBACT01C–04C`
**Target:** Account domain service with full validation rules

| Step | Action | Validation |
|------|--------|------------|
| 1 | Migrate ACCTDAT VSAM → `accounts` table | All 16 fields preserved |
| 2 | Extract and codify all validation rules from COACTUPC | Rule-by-rule unit tests |
| 3 | Implement Account API (view, update, status change) | Parallel-run: same inputs → same outputs |
| 4 | Deploy with dual-write (DB + VSAM shadow) | Reconciliation job runs hourly |
| 5 | Redirect all balance-update callers (Txn, Billing) to Account API | Integration tests pass |
| 6 | Disable VSAM writes; Account Service is authoritative | 2-week stability period |

**Rollback:** Re-enable VSAM as primary; replay any committed DB transactions to VSAM via reconciliation.

### 3B: Transaction Processing Service

**Source:** `COTRN00C–02C`, `CBTRN01C–02C` (~3,150 LOC)
**Target:** Event-driven transaction service

| Step | Action | Validation |
|------|--------|------------|
| 1 | Migrate TRANSACT VSAM + TCATBALF → relational tables | Full data parity |
| 2 | Implement online transaction operations (list, detail, add) | Output comparison |
| 3 | Implement daily posting logic as scheduled service | **Parallel run: old batch + new service, compare results** |
| 4 | Implement category balance accumulation | TCATBALF reconciliation (every category, every account) |
| 5 | Introduce event stream (Kafka/SQS) for transaction capture | Events match VSAM writes |
| 6 | Disable DALYTRAN sequential file; events are the source | 1-month parallel run passes |
| 7 | Decommission CBTRN01C/02C batch jobs | Monitor for balance drift |

**Rollback:** Re-enable batch posting JCL; event stream continues as secondary (no data loss).

### 3C: Batch Operations Migration

**Source:** Control-M schedules, CLOSEFIL/OPENFIL, INTCALC, COMBTRAN, DISCGRP
**Target:** Cloud-native batch (AWS Step Functions / Spring Batch + scheduler)

| Step | Action | Validation |
|------|--------|------------|
| 1 | Replace CLOSEFIL/OPENFIL with no-ops (DB doesn't need file close) | — |
| 2 | Implement interest calculation as scheduled service | Penny-level reconciliation |
| 3 | Implement transaction consolidation | COMBTRAN output comparison |
| 4 | Implement disclosure group refresh | DISCGRP output comparison |
| 5 | Migrate Control-M → cloud scheduler (EventBridge/Airflow) | Schedule parity |
| 6 | Decommission all JCL and Control-M definitions | No batch failures for 1 cycle |

**Rollback:** Control-M definitions retained; can re-enable any individual job.

### Phase 3 Exit Criteria
- [ ] Account balance accurate to the penny for all accounts over 30 days
- [ ] Daily transaction posting produces identical results to legacy for 30 days
- [ ] Monthly interest calculation matches COBOL output (parallel run for 3 months)
- [ ] All batch schedules operating on cloud scheduler
- [ ] VSAM files in read-only mode (no writes from any source)

---

## Phase 4: Decommission & Cleanup

**Duration:** 4–6 weeks
**Risk Level:** Low (validation complete)
**Components:** MQ/Authorization module, final VSAM decommission, mainframe shutdown

### 4A: MQ Authorization Migration

| Step | Action | Validation |
|------|--------|------------|
| 1 | Implement authorization service consuming from cloud messaging | Auth decisions match legacy |
| 2 | Migrate IBM MQ → Amazon MQ or Kafka | Message delivery parity |
| 3 | Migrate IMS DB data → relational tables | Full data comparison |
| 4 | Decommission IMS/MQ components | Zero pending authorizations lost |

### 4B: Final Decommission

| Step | Action | Validation |
|------|--------|------------|
| 1 | VSAM files archived to cold storage | Backup verified |
| 2 | CICS region quiesced | Zero active sessions |
| 3 | Control-M schedules deleted | No orphan triggers |
| 4 | Mainframe LPAR released | Cost savings realized |
| 5 | 90-day retention of mainframe backups | Compliance requirement |

### Phase 4 Exit Criteria
- [ ] All traffic served by modern platform
- [ ] Mainframe costs reduced to zero (or archive-only)
- [ ] 90-day post-migration stability period passed
- [ ] Legacy source archived in version control (this repository)

---

## Timeline Summary

```
Week:  0    4    8   12   16   20   24   28   32   36   40
       ├────┤
       Phase 0: Foundation
            ├─────────┤
            Phase 1: Low-Risk (Auth, Reporting, ETL)
                      ├───────────────┤
                      Phase 2: Medium-Risk (Customer, Card, Billing, UI)
                                       ├────────────────────┤
                                       Phase 3: High-Risk (Account, Txn, Batch)
                                                            ├────────┤
                                                            Phase 4: Decommission
```

**Total estimated duration:** 36–44 weeks (9–11 months)

---

## Parallel-Run Architecture

For Phases 2–3, the parallel-run infrastructure is critical:

```
┌─────────────┐     ┌─────────────┐
│   Legacy    │     │    New      │
│  (COBOL)   │     │  (Java)    │
└──────┬──────┘     └──────┬──────┘
       │                    │
       ▼                    ▼
┌─────────────────────────────────┐
│     Comparison Engine           │
│  (input replay + output diff)   │
└─────────────┬───────────────────┘
              │
              ▼
┌─────────────────────────────────┐
│   Reconciliation Dashboard      │
│   - Field-level diffs           │
│   - Tolerance thresholds        │
│   - Alert on divergence         │
└─────────────────────────────────┘
```

### Validation Thresholds

| Data Type | Tolerance | Action on Breach |
|-----------|-----------|------------------|
| Monetary amounts | 0 (exact match) | Block cutover; investigate |
| Dates | 0 (exact match) | Block cutover |
| String fields | Case-insensitive trim match | Warning; review |
| Record counts | 0 (exact match) | Block cutover |
| Report output | Whitespace-normalized diff | Warning; manual review |

---

## Rollback Strategy

Every phase maintains a rollback path:

1. **ACL routing** — traffic can be redirected back to CICS programs within minutes.
2. **Dual-write windows** — during transition, both VSAM and DB are kept in sync.
3. **Data reconciliation** — hourly jobs detect drift between old and new stores.
4. **Control-M preservation** — all batch schedules retained (disabled, not deleted) until Phase 4.
5. **BMS maps available** — 3270 terminal access remains until final decommission.

**Rollback decision criteria:**
- Any monetary discrepancy > $0.00 → immediate rollback
- Error rate > 1% of transactions → rollback within 1 hour
- Availability < 99.9% for 4 consecutive hours → rollback
