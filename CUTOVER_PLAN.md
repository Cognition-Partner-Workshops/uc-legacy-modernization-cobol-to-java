# CardDemo Phased Cutover Plan

## 1. Overview

This plan sequences the migration of CardDemo from mainframe COBOL/CICS to Java/Spring Boot services, ordered from lowest-risk to highest-risk. Each phase has defined entry criteria, migration activities, validation gates, and rollback procedures.

**Total estimated timeline:** 18–24 months across 6 phases.

---

## 2. Guiding Principles

1. **Lowest risk first** — Start with isolated, leaf-node contexts that have no downstream write dependencies.
2. **Parallel run mandatory** — Every migrated component runs in parallel with legacy for a defined period before cutover.
3. **Rollback always available** — Traffic routing is toggle-based; legacy remains operational until phase validation passes.
4. **Data parity before cutover** — Automated reconciliation must show zero discrepancies for a defined period.
5. **No big bang** — Each phase delivers a working increment; the system is always fully operational.
6. **Feature freeze during cutover** — No new feature development on the migrating component during the active cutover window.

---

## 3. Phase Sequence

```
Phase 1 ─ Identity & Access Management + Reference Data     [Months 1-3]
Phase 2 ─ Card Management + Reporting/Statements             [Months 3-6]
Phase 3 ─ Account Management + Bill Payment                  [Months 6-10]
Phase 4 ─ Transaction Processing (Online + Batch)            [Months 10-15]
Phase 5 ─ Interest Calculation + Authorization/Fraud         [Months 15-20]
Phase 6 ─ Data Migration + Decommissioning                   [Months 20-24]
```

---

## 4. Phase Details

### Phase 1: Identity & Access Management + Reference Data
**Timeline:** Months 1–3
**Risk Level:** ⬤○○○○ (Lowest)

#### Scope
| Component | Programs | LOC | Strategy |
|---|---|---|---|
| Authentication | COSGN00C | 260 | Strangler → Rewrite |
| User Admin | COUSR00-03C | 1,767 | Strangler → Rewrite |
| Menu Navigation | COMEN01C, COADM01C | 596 | Strangler (absorb) |
| Transaction Type Mgmt | COTRTLIC, COTRTUPC, COBTUPDT | ~700 | Strangler → Rewrite |
| MQ Account Extraction | CODATE01, COACCT01 | ~400 | Strangler → Rewrite |

#### Entry Criteria
- [ ] Target infrastructure provisioned (Kubernetes cluster, PostgreSQL, API Gateway)
- [ ] CI/CD pipeline established with automated testing
- [ ] Anti-Corruption Layer (ACL) framework deployed with COMMAREA↔JWT translation
- [ ] CDC pipeline from USRSEC VSAM to PostgreSQL operational
- [ ] Modern UI scaffold (React/Angular) deployed with routing to both legacy and new backend

#### Migration Activities
1. **IAM Service (Weeks 1–4)**
   - Implement Spring Security OAuth2 service with user CRUD
   - Migrate USRSEC VSAM data to `users` table in PostgreSQL
   - Implement JWT token issuance replacing COMMAREA session propagation
   - Deploy ACL to translate JWT tokens into COMMAREA for legacy programs still in use

2. **Reference Data Service (Weeks 3–6)**
   - Migrate DB2 TRNTYPE/TRNTYCAT tables to PostgreSQL
   - Implement REST API for transaction type CRUD
   - Eliminate the TRANEXTR (DB2→VSAM extract) pattern — new services read from PostgreSQL directly
   - Maintain VSAM extract job during transition for legacy consumers

3. **MQ Extraction Replacement (Weeks 5–8)**
   - Replace CODATE01/COACCT01 MQ programs with REST endpoints
   - Implement adapter to bridge MQ messages to REST calls during transition

4. **Menu/Navigation (Weeks 6–8)**
   - Deploy modern SPA with routing logic replacing BMS menus
   - SPA routes to new IAM and Reference Data services
   - SPA proxies remaining functions to legacy CICS (via ACL)

#### Validation Gates
- [ ] All USRSEC records migrated with zero data discrepancies
- [ ] Login flow works end-to-end through new IAM service
- [ ] Legacy programs still authenticate via ACL-translated JWT
- [ ] Transaction type CRUD functional parity verified (automated test suite)
- [ ] 2-week parallel run with zero authentication failures
- [ ] Performance: New IAM login latency ≤ legacy 3270 sign-on time

#### Rollback Procedure
1. Route all traffic back to CICS CC00 sign-on transaction
2. Disable JWT issuance in API Gateway
3. Re-enable COMMAREA-only session propagation
4. Restart VSAM extract jobs for reference data

---

### Phase 2: Card Management + Reporting/Statements
**Timeline:** Months 3–6
**Risk Level:** ⬤⬤○○○ (Low)

#### Scope
| Component | Programs | LOC | Strategy |
|---|---|---|---|
| Card Management | COCRDLIC, COCRDSLC, COCRDUPC | 3,906 | Strangler |
| Reporting | CORPT00C, CBSTM03A/B, CBTRN03C | 1,803 | Rewrite |

#### Entry Criteria
- [ ] Phase 1 cutover complete and stable for 2+ weeks
- [ ] Account domain APIs available in read-only mode (shadow API from VSAM via CDC)
- [ ] CARDDAT VSAM → PostgreSQL CDC pipeline operational
- [ ] Report output requirements documented (format, delivery, retention)

#### Migration Activities
1. **Card Service (Weeks 1–6)**
   - Implement Card CRUD service backed by PostgreSQL
   - Migrate CARDDAT VSAM to `cards` table
   - Card→Account resolution via Account shadow API (reads CARDXREF)
   - Deploy in Strangler mode: new UI calls Card Service; legacy CICS programs still available

2. **Reporting Service (Weeks 4–10)**
   - Rewrite CBSTM03A statement generation using JasperReports or Apache POI
   - Replace TDQ/Internal Reader submission (CORPT00C) with async job queue (SQS/Kafka)
   - Implement transaction report (replacing CBTRN03C) reading from Transaction shadow database
   - Generate PDF/HTML statements (replacing STMTFILE/HTMLFILE sequential files)
   - Deploy scheduled report generation via Kubernetes CronJob (replacing Control-M CREASTMT)

#### Validation Gates
- [ ] All CARDDAT records migrated with zero discrepancies
- [ ] Card list/view/update functional parity (automated regression suite)
- [ ] Statement output matches legacy output for 100 sample accounts (content parity, format may differ)
- [ ] Transaction report totals match within ±$0.01 of legacy
- [ ] 2-week parallel run for Card operations with zero failures
- [ ] Report generation completes within SLA (legacy batch window timeframe)

#### Rollback Procedure
1. Route card operations back to CICS CCLI/CCDL/CCUP transactions
2. Re-enable CORPT00C for report submission
3. Restart legacy CBSTM03A batch jobs in Control-M

---

### Phase 3: Account Management + Bill Payment
**Timeline:** Months 6–10
**Risk Level:** ⬤⬤⬤○○ (Medium)

#### Scope
| Component | Programs | LOC | Strategy |
|---|---|---|---|
| Account Management | COACTVWC, COACTUPC, CBACT01-03C, CBCUS01C | 6,245 | Strangler |
| Bill Payment | COBIL00C | 572 | Strangler → Rewrite |

#### Entry Criteria
- [ ] Phase 2 cutover complete and stable for 2+ weeks
- [ ] ACCTDAT and CUSTDATA VSAM → PostgreSQL CDC pipelines operational with <1 second lag
- [ ] Account Service write path designed and reviewed (optimistic locking strategy)
- [ ] CARDXREF migration complete (cross-reference available in PostgreSQL)
- [ ] Dual-write framework tested for ACCTDAT (write to both VSAM and PostgreSQL during transition)

#### Migration Activities
1. **Account Service — Read Path (Weeks 1–4)**
   - Implement account view and customer lookup APIs
   - Validate against legacy COACTVWC for data parity on 100% of accounts
   - Route read traffic to new service while writes remain on legacy

2. **Account Service — Write Path (Weeks 4–10)**
   - Implement account update logic (porting COACTUPC's 4,236 lines of validation)
   - Key validations to port: status transitions, credit limit changes, date validations, balance constraints
   - Implement dual-write: new service writes to PostgreSQL AND VSAM (via legacy adapter) during transition
   - Gradually shift write traffic with percentage-based rollout (10% → 25% → 50% → 100%)

3. **Payment Service (Weeks 8–12)**
   - Implement bill payment as a saga: get balance → create payment transaction → update balance
   - Uses Account Service API (not direct VSAM access)
   - Uses Transaction Service API (not direct VSAM write)
   - Atomic consistency via saga with compensating transactions

4. **Data File Load Jobs (Weeks 10–14)**
   - Replace CBACT01-03C and CBCUS01C batch VSAM load jobs
   - Implement as database migration scripts or bulk-load endpoints
   - Data initialization becomes a CI/CD pipeline step

#### Validation Gates
- [ ] Account view data matches legacy for 100% of accounts
- [ ] Account update preserves all field-level validations from COACTUPC
- [ ] Bill payment creates identical transaction records (amount, type, timestamp)
- [ ] Balance reconciliation: all accounts balance to the penny after 30-day parallel run
- [ ] CARDXREF lookups functional from both Card and Transaction services
- [ ] Dual-write consistency verified: zero VSAM↔PostgreSQL divergence
- [ ] Performance: Account update P99 latency within 2x of legacy

#### Rollback Procedure
1. Revert traffic routing to CICS CAVW/CAUP/CB00 transactions
2. Disable dual-write; VSAM becomes sole source of truth again
3. Re-sync PostgreSQL from VSAM via full reload
4. Validate balance consistency post-rollback

---

### Phase 4: Transaction Processing (Online + Batch)
**Timeline:** Months 10–15
**Risk Level:** ⬤⬤⬤⬤○ (High)

#### Scope
| Component | Programs | LOC | Strategy |
|---|---|---|---|
| Online Transactions | COTRN00C, COTRN01C, COTRN02C | 1,812 | Strangler |
| Batch Transaction Posting | CBTRN02C | 731 | Replatform → Refactor |
| Batch Transaction Ops | CBTRN01C, CBTRN03C | 1,143 | Replatform → Refactor |
| Batch File Operations | CLOSEFIL, OPENFIL, TRANBKP, COMBTRAN, TRANIDX | N/A (JCL) | Replace |

#### Entry Criteria
- [ ] Phase 3 cutover complete and stable for 4+ weeks
- [ ] Account Service write path proven in production (handles balance updates at scale)
- [ ] TRANSACT VSAM → PostgreSQL CDC pipeline operational
- [ ] DALYTRAN feed mechanism redesigned (file drop → message queue or API)
- [ ] Spring Batch framework set up with parallel run capability
- [ ] Comprehensive regression test suite covering all transaction posting edge cases
- [ ] TCATBALF and DALYREJS data models migrated to PostgreSQL

#### Migration Activities
1. **Online Transaction Service (Weeks 1–6)**
   - Implement transaction list, view, and add APIs
   - Transaction add writes to PostgreSQL + VSAM (dual-write)
   - Validate against legacy for transaction creation parity

2. **Batch Transaction Posting — Replatform (Weeks 4–10)**
   - Run CBTRN02C on Micro Focus or Blu Age (Linux runtime)
   - Validate: output files match legacy execution for same input data
   - This de-risks mainframe exit while preserving COBOL logic

3. **Batch Transaction Posting — Refactor to Spring Batch (Weeks 8–18)**
   - Convert CBTRN02C posting logic to Spring Batch job:
     - Reader: Process daily transactions (replaces DALYTRAN file read)
     - Processor: Validate card via Card Service, resolve account via Account Service
     - Writer: Write to TRANSACT, update ACCTDAT balance, update TCATBALF, write DALYREJS
   - Implement as idempotent, restartable batch job
   - Parallel run: execute both legacy and new posting, compare outputs

4. **Replace JCL Infrastructure (Weeks 12–18)**
   - Replace CLOSEFIL/OPENFIL bookend pattern (becomes unnecessary without CICS file control)
   - Replace TRANBKP with database backup mechanism
   - Replace COMBTRAN (SORT utility) with SQL-based merge
   - Replace TRANIDX (AIX definition) with database indexes
   - Replace Control-M DAILY workflow with Kubernetes CronJob + event triggers

#### Validation Gates
- [ ] Online transaction add produces identical TRANSACT records
- [ ] Batch posting: outputs (TRANSACT, ACCTDAT balances, TCATBALF, DALYREJS) match legacy for 5 production-equivalent daily cycles
- [ ] Reject file (DALYREJS) contents match legacy exactly
- [ ] TCATBALF category balance totals reconcile to the penny
- [ ] ACCTDAT balance updates match legacy posting results
- [ ] End-to-end batch window completes within SLA (legacy batch window ± 10%)
- [ ] 30-day parallel run with zero financial discrepancies
- [ ] Reconciliation report automated and running daily

#### Rollback Procedure
1. Stop new Spring Batch jobs
2. Re-enable legacy CBTRN02C batch job in Control-M
3. Switch online transaction traffic back to CICS CT00/CT01/CT02
4. Full TRANSACT VSAM reload from backup
5. Reconcile ACCTDAT balances against last known-good state

---

### Phase 5: Interest Calculation + Authorization/Fraud
**Timeline:** Months 15–20
**Risk Level:** ⬤⬤⬤⬤⬤ (Highest)

#### Scope
| Component | Programs | LOC | Strategy |
|---|---|---|---|
| Interest Calculation | CBACT04C | 652 | Replatform → Refactor |
| Authorization Processing | COPAUA0C, COPAUS0-2C | ~1,200 | Rewrite |
| Authorization Batch | CBPAUP0C, PAUDBLOD, PAUDBUNL | ~800 | Rewrite |
| IMS DB Components | DBDs, PSBs | N/A | Eliminate |
| DB2 Fraud Table | AUTHFRDS | N/A | Migrate to PostgreSQL |

#### Entry Criteria
- [ ] Phase 4 cutover complete and stable for 4+ weeks
- [ ] TCATBALF data owned by Transaction Service with API access
- [ ] Account Service balance update API proven at scale (handles Interest + Posting loads)
- [ ] Authorization Service architecture reviewed (event-driven design)
- [ ] DISCGRP rate data migrated to PostgreSQL
- [ ] IMS decommission plan approved by operations team
- [ ] MQ message format documented and versioned for backward compatibility

#### Migration Activities
1. **Interest Calculation — Replatform (Weeks 1–4)**
   - Run CBACT04C on Micro Focus / Blu Age Linux runtime
   - Validate against legacy for a full monthly interest cycle
   - Confirm packed decimal (COMP-3) arithmetic produces identical results

2. **Interest Calculation — Refactor to Java (Weeks 4–12)**
   - Convert to Spring Batch job using `java.math.BigDecimal` with `HALF_EVEN` rounding
   - Interest Service owns DISCGRP rate data (reads from PostgreSQL)
   - Calls Transaction Service for category balances (TCATBALF)
   - Calls Account Service to post interest charges (balance update)
   - Parallel run for 3 monthly cycles minimum
   - Automated reconciliation: per-account interest amounts must match to the penny

3. **Authorization Service — Rewrite (Weeks 6–16)**
   - Implement event-driven Authorization Service:
     - Kafka/SQS consumer replaces MQ-triggered COPAUA0C
     - PostgreSQL replaces IMS HIDAM for authorization storage
     - PostgreSQL replaces DB2 AUTHFRDS for fraud records
   - Authorization request/response maintains legacy MQ message format via adapter
   - MQ adapter translates between legacy MQ and new Kafka/SQS topics
   - Online screens (COPAUS0-2C) replaced by new UI pages
   - Batch purge (CBPAUP0C) replaced by scheduled database cleanup job

4. **IMS Decommission (Weeks 14–18)**
   - Migrate all IMS HIDAM data to PostgreSQL `authorizations` table
   - Verify data integrity (all authorization records accounted for)
   - Decommission IMS databases (DBPAUTP0, DBPAUTX0)
   - Remove IMS PSBs and DBDs from operational inventory

#### Validation Gates
- [ ] Interest calculation: per-account amounts match legacy for 3 consecutive monthly cycles
- [ ] BigDecimal rounding produces identical results to COMP-3 arithmetic for all test cases
- [ ] Authorization request processing: approve/decline decisions match legacy logic for test transaction set
- [ ] Fraud flagging workflow operational in new UI
- [ ] Authorization purge produces identical results (same records purged)
- [ ] IMS data fully migrated to PostgreSQL with zero record loss
- [ ] MQ message compatibility: legacy POS systems work with new authorization service via MQ adapter
- [ ] 30-day parallel run for interest; 60-day for authorization

#### Rollback Procedure
1. Re-enable CBACT04C in Control-M (interest)
2. Re-enable COPAUA0C MQ trigger in CICS (authorization)
3. Re-enable IMS database access
4. Route MQ authorization messages back to legacy program
5. Reconcile any authorization records created during new service operation

---

### Phase 6: Data Migration + Decommissioning
**Timeline:** Months 20–24
**Risk Level:** ⬤⬤○○○ (Low — cleanup phase)

#### Scope
| Component | Programs | LOC | Strategy |
|---|---|---|---|
| Data Export/Import | CBEXPORT, CBIMPORT | 1,069 | Refactor |
| Utilities | CSUTLDTC, COBSWAIT, ASM modules | ~200 | Retire |
| Anti-Corruption Layer | ACL components | N/A | Remove |
| VSAM Files | All | N/A | Decommission |
| CICS Region | All transactions | N/A | Decommission |
| Control-M Jobs | All CardDemo workflows | N/A | Decommission |

#### Entry Criteria
- [ ] Phases 1–5 cutover complete and stable for 8+ weeks
- [ ] All VSAM files no longer receiving writes from legacy programs
- [ ] All Control-M jobs disabled for 4+ weeks with no business impact
- [ ] CICS region receiving zero transactions for 4+ weeks
- [ ] Export/Import requirements confirmed with business (still needed? new format?)

#### Migration Activities
1. **Export/Import Modernization (Weeks 1–6)**
   - Refactor CBEXPORT to Java: reads from all domain PostgreSQL databases
   - Output format: JSON or Parquet (replacing 500-byte EBCDIC REDEFINES records)
   - Refactor CBIMPORT to Java: bulk insert into PostgreSQL from JSON/Parquet
   - If branch migration is no longer needed, retire entirely

2. **Anti-Corruption Layer Removal (Weeks 4–8)**
   - Remove COMMAREA↔JWT translation layer
   - Remove VSAM↔PostgreSQL CDC pipelines
   - Remove dual-write adapters
   - Remove MQ↔Kafka/SQS adapters
   - Simplify API Gateway routing (remove legacy proxy rules)

3. **Legacy Decommission (Weeks 6–12)**
   - Archive VSAM files to cold storage (retain for compliance/audit)
   - Decommission CICS region
   - Remove Control-M CardDemo workflow definitions
   - Archive COBOL source and JCL (retain in repository for reference)
   - Decommission mainframe LPAR (if CardDemo is the last workload)

4. **Utility Retirement (Weeks 8–10)**
   - CSUTLDTC (date utility) → absorbed into `java.time` usage
   - COBSWAIT → replaced by `Thread.sleep()` or scheduler delays
   - ASM MVSWAIT/COBDATFT → no longer needed

#### Validation Gates
- [ ] Export produces equivalent data to legacy CBEXPORT (reconciled at record level)
- [ ] No CICS transactions executing for 30+ days
- [ ] No Control-M jobs executing for 30+ days
- [ ] No VSAM file access for 30+ days
- [ ] All compliance/audit data archived per retention policy
- [ ] Mainframe cost savings realized (MIPS reduction confirmed)

#### Rollback Procedure
- Phase 6 rollback is restoration from archive. This is a one-way door — once CICS region is decommissioned, restoring requires significant effort. Ensure all validation gates pass before decommission.
- Maintain mainframe environment in standby for 90 days post-decommission as a safety net.

---

## 5. Cross-Phase Dependencies

```
Phase 1 ──▶ Phase 2 ──▶ Phase 3 ──▶ Phase 4 ──▶ Phase 5 ──▶ Phase 6
IAM+RefData  Card+Report  Account+Pay  Transaction  Interest+Auth  Decommission
                │              │              │              │
                │              └──────────────┘              │
                │         Account API needed for             │
                │         Transaction batch posting          │
                │                                            │
                └────────────────────────────────────────────┘
                       Card Service used by Authorization
```

**Critical path:** Phase 3 (Account) must complete before Phase 4 (Transaction) can begin batch refactoring, because the batch posting job (CBTRN02C) writes to ACCTDAT.

---

## 6. Parallel Run Strategy

Each phase includes a parallel run where both legacy and new components process the same workload:

| Phase | Parallel Run Duration | Reconciliation Method |
|---|---|---|
| 1 — IAM + Reference Data | 2 weeks | Login success rate comparison; reference data record count |
| 2 — Card + Reporting | 2 weeks | Record-level card data comparison; report output diff |
| 3 — Account + Payment | 30 days | Per-account balance reconciliation (daily automated) |
| 4 — Transaction | 30 days | Daily posting output comparison (TRANSACT, ACCTDAT, TCATBALF) |
| 5 — Interest | 3 monthly cycles | Per-account interest amount comparison to the penny |
| 5 — Authorization | 60 days | Authorization decision comparison per request |

---

## 7. Go/No-Go Criteria

Before each phase cutover, the following must be satisfied:

| Criterion | Threshold |
|---|---|
| Data parity | Zero discrepancies in automated reconciliation |
| Error rate | New service error rate ≤ legacy error rate |
| Latency | P99 latency within 2x of legacy response time |
| Parallel run | Required duration completed with no blocking issues |
| Rollback tested | Rollback procedure executed successfully in staging |
| Stakeholder sign-off | Business owner and operations team approval |
| Security review | New service passed security scan (SAST + DAST) |
| Load test | New service handles 2x peak legacy throughput |

---

## 8. Communication Plan

| Event | Audience | Channel | Timing |
|---|---|---|---|
| Phase kickoff | All stakeholders | Email + meeting | 2 weeks before |
| Cutover window | Operations + Business | War room / Slack channel | During cutover |
| Parallel run start | Operations | Dashboard + email | Day of |
| Reconciliation report | Technical lead | Automated daily email | Daily during parallel run |
| Go/No-Go decision | All stakeholders | Meeting | 1 day before cutover |
| Cutover complete | All stakeholders | Email | Immediately after |
| Phase retrospective | Engineering team | Meeting | 1 week after |
