# CardDemo Cutover Plan

## 1. Overview

This plan defines the phased migration sequence for the CardDemo mainframe application, ordered from **lowest-risk to highest-risk**. Each phase includes entry criteria, migration steps, validation gates, and rollback procedures. The plan assumes a hybrid Strangler Fig (online) + Rewrite (batch) strategy as recommended in the Modernization Blueprint.

---

## 2. Guiding Principles

1. **Read before write**: Migrate read-only paths first; write paths follow once data integrity is proven.
2. **Reference data before transactional data**: Seed lookup tables and master data before migrating workflows.
3. **Simple before complex**: Low-coupling modules first; high-coupling modules last.
4. **Always reversible**: Every phase has a documented rollback to the previous state.
5. **Dual-run validation**: Legacy and modern systems run in parallel until functional equivalence is confirmed.
6. **Feature flags**: All traffic routing through the Strangler facade is controlled by feature flags — not deployments.

---

## 3. Phase Summary

| Phase | Name | Duration | Risk | Functional Areas |
|:-----:|------|:--------:|:----:|-----------------|
| 0 | Foundation & Infrastructure | 4 weeks | Minimal | DevOps, database, API gateway |
| 1 | Identity & Access Management | 3 weeks | Low | FA-01, FA-02, FA-08 |
| 2 | Read-Only Data Services | 4 weeks | Low | FA-04 (cards), FA-05 (txn list/view) |
| 3 | Account Management | 5 weeks | Medium | FA-03 (view then update) |
| 4 | Transaction Write Path & Bill Payment | 5 weeks | Medium-High | FA-05 (txn add), FA-06 |
| 5 | Reporting & Statements | 4 weeks | Medium | FA-07, FA-11 |
| 6 | Batch Processing | 6 weeks | High | FA-09, FA-10, FA-12 |
| 7 | Optional Modules & Decommission | 4 weeks | Medium | FA-13, legacy shutdown |

**Total estimated duration**: 35 weeks (~9 months)

---

## 4. Detailed Phase Plans

### Phase 0: Foundation & Infrastructure
**Risk: MINIMAL** | **Duration: 4 weeks**

#### Objectives
- Establish the target runtime environment
- Deploy database with migrated schema
- Set up API gateway / Strangler facade
- Seed reference and master data

#### Steps

| Step | Action | Owner | Duration |
|:----:|--------|-------|:--------:|
| 0.1 | Provision PostgreSQL instance (RDS or equivalent) | DevOps | 2 days |
| 0.2 | Create Flyway migration scripts from VSAM copybook definitions (CVACT01Y → `accounts`, CVACT02Y → `cards`, CVCUS01Y → `customers`, CVTRA05Y → `transactions`, CSUSR01Y → `users`, CVTRA01Y → `transaction_category_balances`, CVTRA02Y → `discount_groups`) | Backend | 1 week |
| 0.3 | Load initial data from `app/data/ASCII/` files (acctdata.txt, carddata.txt, custdata.txt, cardxref.txt, trantype.txt, discgrp.txt, tcatbal.txt, trancatg.txt) | Backend | 3 days |
| 0.4 | Deploy Spring Cloud Gateway as Strangler facade | DevOps | 3 days |
| 0.5 | Set up CI/CD pipeline (GitHub Actions) | DevOps | 2 days |
| 0.6 | Deploy monitoring stack (Micrometer + Prometheus + Grafana) | DevOps | 2 days |
| 0.7 | Create React SPA scaffold with routing skeleton | Frontend | 3 days |
| 0.8 | Validate data migration: record counts, checksums, sample verification | QA | 2 days |

#### Entry Criteria
- Cloud environment provisioned
- Source COBOL codebase analyzed (this document set)
- Team onboarded

#### Exit Criteria
- Database populated with production-equivalent data
- API gateway deployed and routing 100% traffic to legacy (passthrough mode)
- CI/CD pipeline green
- Monitoring dashboards operational

#### Rollback
- Remove API gateway from traffic path; direct traffic to legacy system

---

### Phase 1: Identity & Access Management
**Risk: LOW** | **Duration: 3 weeks**

#### Objectives
- Replace COBOL authentication (COSGN00C) with Spring Security + JWT
- Replace user administration (COUSR00C-03C) with REST CRUD API
- Replace admin/user menus (COADM01C, COMEN01C) with SPA routing

#### Steps

| Step | Action | Owner | Duration |
|:----:|--------|-------|:--------:|
| 1.1 | Implement Spring Security auth service with JWT issuance | Backend | 1 week |
| 1.2 | Migrate USRSEC data to `users` table with BCrypt password hashing | Backend | 2 days |
| 1.3 | Implement user CRUD REST API (list with pagination, add, update, delete) | Backend | 1 week |
| 1.4 | Build login page and user admin UI in React SPA | Frontend | 1 week |
| 1.5 | Configure API gateway to intercept auth requests and route to new service | DevOps | 2 days |
| 1.6 | Dual-run validation: authenticate same users against both systems | QA | 3 days |
| 1.7 | Switch auth traffic to new service via feature flag | DevOps | 1 day |

#### Validation Gates
- [ ] All existing users can authenticate with correct passwords
- [ ] Admin users see admin menu options; regular users see user menu options
- [ ] User CRUD operations (add/update/delete) work and survive restart
- [ ] JWT tokens are validated by the API gateway for downstream requests
- [ ] Legacy COSGN00C still works in parallel (feature flag off = legacy)

#### Rollback
- Feature flag OFF → gateway routes auth requests to legacy COSGN00C
- USRSEC file remains authoritative until Phase 1 is validated

---

### Phase 2: Read-Only Data Services
**Risk: LOW** | **Duration: 4 weeks**

#### Objectives
- Extract card list/view (COCRDLIC/COCRDSLC) to REST API
- Extract transaction list/view (COTRN00C/COTRN01C) to REST API
- Establish the VSAM → RDBMS data sync pipeline

#### Steps

| Step | Action | Owner | Duration |
|:----:|--------|-------|:--------:|
| 2.1 | Implement Card REST API (list with pagination, view, search) | Backend | 1 week |
| 2.2 | Implement Transaction list/view REST API (pagination, filtering) | Backend | 1 week |
| 2.3 | Build card list and transaction list UI screens in React | Frontend | 1.5 weeks |
| 2.4 | Set up VSAM → database CDC sync for TRANSACT and CARDDAT files | Backend | 1 week |
| 2.5 | Dual-run: compare query results between legacy screens and new API | QA | 3 days |
| 2.6 | Route card and transaction read traffic to new services | DevOps | 1 day |

#### Validation Gates
- [ ] Card list pagination matches legacy COCRDLIC output (same records, same order)
- [ ] Transaction list pagination matches legacy COTRN00C output
- [ ] Transaction detail view matches legacy COTRN01C output
- [ ] CDC sync latency < 5 seconds for new VSAM records appearing in database
- [ ] No data discrepancies in 72-hour parallel run

#### Rollback
- Feature flag OFF → gateway routes card/transaction reads to legacy CICS programs
- Database remains a read replica; VSAM remains the system of record

---

### Phase 3: Account Management
**Risk: MEDIUM** | **Duration: 5 weeks**

#### Objectives
- Extract account view (COACTVWC) to REST API — read-only, lower risk
- Extract account update (COACTUPC) to REST API — write path with complex validation
- Establish account service as the system of record for ACCTDAT

#### Steps

| Step | Action | Owner | Duration |
|:----:|--------|-------|:--------:|
| 3.1 | Implement Account view REST API (account + linked cards + customer info) | Backend | 1 week |
| 3.2 | Implement Account validation library (SSN, phone, date, credit limit rules extracted from COACTUPC) | Backend | 1.5 weeks |
| 3.3 | Implement Account update REST API with full validation | Backend | 1 week |
| 3.4 | Build account view and update UI screens in React | Frontend | 1.5 weeks |
| 3.5 | Implement dual-write: account updates write to both database and VSAM | Backend | 3 days |
| 3.6 | Dual-run: compare account view/update results | QA | 3 days |
| 3.7 | Switch account service to system of record; VSAM becomes the replica | DevOps | 1 day |

#### Validation Gates
- [ ] Account view displays same data as legacy COACTVWC for all test accounts
- [ ] All COACTUPC validation rules are replicated (test with invalid SSN, phone, dates, credit limits)
- [ ] Account update persists correctly and is visible in both database and VSAM
- [ ] Concurrent updates handled correctly (optimistic locking)
- [ ] Dual-write consistency verified for 72-hour parallel run

#### Rollback
- Feature flag OFF → gateway routes account requests to legacy CICS programs
- Dual-write ensures VSAM has all recent changes if rollback is needed
- If dual-write is causing issues, disable database writes and revert to VSAM-only

---

### Phase 4: Transaction Write Path & Bill Payment
**Risk: MEDIUM-HIGH** | **Duration: 5 weeks**

#### Objectives
- Extract transaction add (COTRN02C) to REST API
- Extract bill payment (COBIL00C) to REST API
- Migrate card update (COCRDUPC) to REST API
- Replace VSAM TRAN-ID generation (MAX+1) with database sequences

#### Steps

| Step | Action | Owner | Duration |
|:----:|--------|-------|:--------:|
| 4.1 | Implement Transaction add REST API with database sequence for TRAN-ID | Backend | 1 week |
| 4.2 | Implement Bill Payment REST API (preview + confirm pattern) with idempotency | Backend | 1.5 weeks |
| 4.3 | Implement Card update REST API | Backend | 1 week |
| 4.4 | Build transaction add, bill payment, and card update UI screens | Frontend | 1.5 weeks |
| 4.5 | Implement cross-context coordination: bill payment → transaction create + account balance update | Backend | 3 days |
| 4.6 | Dual-run: create transactions and bill payments via both systems; compare results | QA | 3 days |
| 4.7 | Switch write traffic to new services; disable legacy VSAM writes | DevOps | 1 day |

#### Validation Gates
- [ ] Transaction IDs are unique and sequential (no gaps or collisions)
- [ ] Bill payment correctly deducts balance and creates transaction record
- [ ] Bill payment idempotency: duplicate requests do not create duplicate transactions
- [ ] Card updates persist correctly
- [ ] Account balance consistency after bill payments (verified against legacy)
- [ ] No orphaned transactions or phantom balance changes in 72-hour parallel run

#### Rollback
- Feature flag OFF → gateway routes writes to legacy CICS programs
- Dual-write ensures VSAM has all recent changes
- If database sequences have advanced past legacy MAX+1, resync by setting legacy starting ID

---

### Phase 5: Reporting & Statements
**Risk: MEDIUM** | **Duration: 4 weeks**

#### Objectives
- Replace online report submission (CORPT00C) with REST API + React form
- Rewrite batch statement generation (CBSTM03A/B) as Spring Batch job
- Rewrite transaction report (CBTRN03C) as database query + PDF generation

#### Steps

| Step | Action | Owner | Duration |
|:----:|--------|-------|:--------:|
| 5.1 | Implement report submission REST API (monthly, yearly, custom date range) | Backend | 3 days |
| 5.2 | Implement Spring Batch report job (SQL-based query, PDF/CSV output) | Backend | 1.5 weeks |
| 5.3 | Rewrite statement generation as Spring Batch job with Thymeleaf templates | Backend | 1.5 weeks |
| 5.4 | Build report submission UI in React | Frontend | 1 week |
| 5.5 | Comparison testing: generate reports from both systems for same date ranges | QA | 3 days |
| 5.6 | Decommission CORPT00C JCL submission and CBSTM03A/B batch | DevOps | 1 day |

#### Validation Gates
- [ ] Monthly/yearly/custom reports produce same transaction totals as legacy
- [ ] Statement format is readable and includes all required sections (header, basic details, transaction summary, totals)
- [ ] Reports handle edge cases: no transactions in range, single transaction, large volumes
- [ ] Report generation completes within acceptable time (< 5 minutes for yearly)

#### Rollback
- Legacy JCL jobs remain available; re-enable CICS TDQ submission
- Reports generated by the new system are in addition to (not replacing) legacy reports until validated

---

### Phase 6: Batch Processing
**Risk: HIGH** | **Duration: 6 weeks**

#### Objectives
- Rewrite batch transaction posting (CBTRN02C) as Spring Batch job
- Rewrite interest calculation (CBACT04C) as Spring Batch job
- Replace data management programs (CBACT01C-03C, CBCUS01C) with database operations
- Decommission all JCL batch jobs

#### Steps

| Step | Action | Owner | Duration |
|:----:|--------|-------|:--------:|
| 6.1 | Implement Spring Batch transaction posting (Reader: daily file, Processor: validate xref + account, Writer: post transaction + update category balance + update account) | Backend | 2 weeks |
| 6.2 | Implement Spring Batch interest calculation (Reader: category balances, Processor: lookup discount group + compute interest, Writer: create interest transaction + update account balance) | Backend | 2 weeks |
| 6.3 | Build equivalence test harness: process identical input files, compare outputs byte-for-byte | QA | 1 week |
| 6.4 | Replace data refresh programs with Flyway migrations and Spring Batch import jobs | Backend | 1 week |
| 6.5 | Run parallel batch cycles (3 consecutive end-of-day and end-of-month cycles) | QA | 3 days |
| 6.6 | Switch batch scheduling to new Spring Batch jobs | DevOps | 1 day |

#### Validation Gates
- [ ] Transaction posting: same number of posted and rejected records as legacy for identical input
- [ ] Transaction posting: reject reasons match legacy DALYREJS output
- [ ] Transaction posting: account balances match after posting
- [ ] Interest calculation: computed interest matches legacy to the penny (COMP-3 precision)
- [ ] Interest calculation: account balance after interest matches legacy
- [ ] Category balances (TCATBALF) match between systems after full batch cycle
- [ ] End-of-month cycle produces identical financial outcomes across 3 consecutive months

#### Rollback
- Revert batch scheduling to legacy JCL jobs
- VSAM files remain available for legacy batch programs
- If financial discrepancies are found, halt modern batch and run legacy batch to maintain integrity

**Critical Note**: This phase must be carefully timed around business cycles. Avoid cutover during month-end, quarter-end, or year-end processing. Ideal cutover: beginning of a new billing cycle after successful parallel runs.

---

### Phase 7: Optional Modules & Decommission
**Risk: MEDIUM** | **Duration: 4 weeks**

#### Objectives
- Migrate optional modules (IMS/DB2/MQ patterns) to Spring Boot equivalents
- Decommission all legacy CICS programs
- Decommission VSAM files
- Archive legacy codebase

#### Steps

| Step | Action | Owner | Duration |
|:----:|--------|-------|:--------:|
| 7.1 | Implement authorization module using Spring Security (replaces IMS/DB2/MQ authorization) | Backend | 1 week |
| 7.2 | Implement transaction type service using JPA (replaces DB2 module) | Backend | 1 week |
| 7.3 | Implement message-driven account inquiry using Kafka/RabbitMQ (replaces VSAM-MQ module) | Backend | 1 week |
| 7.4 | Remove VSAM → database CDC sync (no longer needed) | DevOps | 1 day |
| 7.5 | Disable legacy CICS region | DevOps | 1 day |
| 7.6 | Archive VSAM files to cold storage | DevOps | 1 day |
| 7.7 | Final regression test of complete modern system | QA | 3 days |
| 7.8 | Archive legacy COBOL source code | DevOps | 1 day |

#### Validation Gates
- [ ] All functional areas operational without legacy system
- [ ] No traffic reaching legacy CICS programs (monitoring confirms zero hits for 7 days)
- [ ] Authorization module correctly approves/denies transactions
- [ ] Message-driven inquiry responds within SLA
- [ ] Full regression suite passes

#### Rollback
- Legacy CICS region can be restarted from archived VSAM files
- Keep legacy system available (powered off) for 90 days post-decommission

---

## 5. Parallel Run Strategy

During each phase, the legacy and modern systems run in parallel:

```
                    Phase N Parallel Run
    ┌────────────────────────────────────────────┐
    │                                            │
    │  ┌──────────┐    Feature     ┌──────────┐  │
    │  │  Legacy   │◄── Flag OFF ──┤  API     │  │
    │  │  CICS     │               │  Gateway │  │
    │  │  Program  │── Flag ON ──►│          │  │
    │  └──────────┘               └─────┬────┘  │
    │                                   │       │
    │                              ┌────v────┐  │
    │                              │  Modern  │  │
    │                              │  Service │  │
    │                              └─────────┘  │
    │                                            │
    │  ┌──────────────────────────────────────┐  │
    │  │  Comparison Tool                      │  │
    │  │  - Send same request to both systems  │  │
    │  │  - Compare responses                  │  │
    │  │  - Log discrepancies                  │  │
    │  │  - Alert on mismatches               │  │
    │  └──────────────────────────────────────┘  │
    └────────────────────────────────────────────┘
```

**Parallel run duration per phase**: Minimum 72 hours of clean operation before cutover.

**Discrepancy handling**:
- **Severity 1** (data corruption/financial impact): Immediate rollback, incident review
- **Severity 2** (functional difference, no data impact): Log and fix, extend parallel run
- **Severity 3** (cosmetic/formatting): Log and fix in next sprint

---

## 6. Data Cutover Sequence

```
Phase 0: Initial bulk load (all ASCII data files → PostgreSQL)
    │
Phase 1: USRSEC → users table (one-time migration + BCrypt rehash)
    │    Stop dual-write to USRSEC after validation
    │
Phase 2: Enable CDC: TRANSACT, CARDDAT → database (read-only sync)
    │
Phase 3: ACCTDAT → accounts table (dual-write enabled)
    │    Account service becomes system of record
    │    Stop CDC for ACCTDAT; database is now authoritative
    │
Phase 4: TRANSACT → transactions table (dual-write for new records)
    │    Database sequences replace VSAM MAX+1
    │    Stop CDC for TRANSACT; database is now authoritative
    │
Phase 5: No new data migration (reporting reads from database)
    │
Phase 6: DALYTRAN → staging table (batch input)
    │    TCATBALF → transaction_category_balances (batch output)
    │    DISCGRP already loaded in Phase 0
    │    All batch processes use database exclusively
    │
Phase 7: Decommission CDC pipeline
         Archive VSAM files
         Database is sole system of record
```

---

## 7. Go/No-Go Decision Matrix

Before proceeding from one phase to the next, the following criteria must be met:

| Criterion | Threshold | Measurement |
|-----------|-----------|-------------|
| Functional equivalence | 100% for critical paths | Automated comparison tests |
| Data integrity | Zero discrepancies | Checksum and record count validation |
| Performance | Within 120% of legacy response time | APM metrics (P95 latency) |
| Error rate | < 0.1% of requests | Monitoring dashboard |
| Parallel run duration | Minimum 72 hours clean | Zero Sev-1 or Sev-2 issues |
| Rollback tested | Successfully demonstrated | Runbook execution during parallel run |
| Stakeholder sign-off | Product owner + tech lead | Documented approval |

---

## 8. Communication Plan

| Event | Audience | Channel | Timing |
|-------|----------|---------|--------|
| Phase kickoff | All stakeholders | Meeting + email | Start of each phase |
| Daily status | Dev team | Standup | Daily during phase |
| Parallel run results | Tech leads + PO | Dashboard + report | End of each parallel run |
| Go/No-Go decision | All stakeholders | Meeting | End of each phase |
| Incident (Sev-1) | All stakeholders | Immediate page + war room | Within 15 minutes |
| Rollback executed | All stakeholders | Email + meeting | Within 1 hour |
| Phase completion | All stakeholders | Email + demo | End of each phase |
| Final decommission | All stakeholders + management | Meeting + email | Phase 7 completion |
