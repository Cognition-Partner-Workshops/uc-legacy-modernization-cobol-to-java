# Cutover Plan — CardDemo Modernization

## Overview

This plan sequences the migration of CardDemo functional areas from lowest-risk to highest-risk. Each phase can be delivered and validated independently. The plan assumes a **Strangler Fig** approach at the system level — legacy and modern services coexist behind a routing layer until the legacy component is fully decommissioned.

---

## Phasing Principles

1. **Foundation First**: Build shared infrastructure (API gateway, event bus, observability) before domain services.
2. **Leaf Nodes Before Roots**: Migrate contexts with no downstream dependents first (they can't break others).
3. **Read Before Write**: Extract read-only consumers before touching write paths.
4. **Low Coupling → High Coupling**: Self-contained modules before those with cross-domain write dependencies.
5. **Revenue-Critical Last**: Transaction posting and interest calculation carry the highest financial risk and migrate only after extensive dual-run validation.

---

## Phase Overview

| Phase | Name | Duration | Risk | Contexts |
|:-----:|------|:--------:|:----:|----------|
| 0 | Foundation & Infrastructure | 4 weeks | None | Platform setup |
| 1 | Identity & Reference Data | 4 weeks | Low | Identity & Access, Reference Data |
| 2 | Customer & Card (Read Path) | 4 weeks | Low | Customer, Card Management |
| 3 | Account Service & Card (Write Path) | 6 weeks | Medium | Account Management, Card writes |
| 4 | Transaction Entry & Bill Payment | 6 weeks | Medium | Transaction (Online), Bill Payment |
| 5 | Statement & Reporting | 4 weeks | Low | Statement & Reporting |
| 6 | Authorization & Fraud | 6 weeks | Medium | Authorization & Fraud, MQ Integration |
| 7 | Transaction Posting & Interest (Batch) | 12 weeks | High | Transaction Posting, Interest Calc |
| 8 | Branch Migration & Decommission | 4 weeks | Low | Branch Migration, legacy teardown |

**Total Estimated Timeline:** 50 weeks (12–13 months)

---

## Detailed Phase Breakdown

### Phase 0: Foundation & Infrastructure (Weeks 1–4)

**Objective:** Establish the target platform, CI/CD pipelines, and coexistence routing layer.

| Task | Deliverable | Owner |
|------|-------------|-------|
| Provision cloud infrastructure (Kubernetes, PostgreSQL, Kafka) | IaC templates (Terraform/CDK) | Platform team |
| Deploy API Gateway with legacy passthrough | Gateway routes all traffic to legacy initially | Platform team |
| Set up CI/CD pipelines (GitHub Actions / Jenkins) | Build, test, deploy automation | DevOps |
| Establish observability stack (metrics, logs, traces) | Grafana/Prometheus/OpenTelemetry | Platform team |
| Create shared library: `carddemo-common` | BigDecimal utilities, date conversion, error handling | Dev team |
| Define API contract standards (OpenAPI 3.0) | API style guide document | Architecture |
| Set up dual-run test framework | Parity comparison harness | QA team |

**Exit Criteria:**
- [ ] Gateway routes traffic to legacy with zero degradation
- [ ] CI/CD can build and deploy a Spring Boot service to target environment
- [ ] Observability confirms baseline metrics from legacy system

---

### Phase 1: Identity & Reference Data (Weeks 5–8)

**Objective:** Extract the two lowest-risk, highest-independence contexts.

#### 1A: Identity & Access Service

| Task | Deliverable |
|------|-------------|
| Build `carddemo-auth` Spring Boot service | OAuth2 / JWT authentication |
| Migrate USRSEC data to PostgreSQL `users` table | Data migration script |
| Implement user CRUD (replaces `COUSR00C`–`COUSR03C`) | REST API + admin UI |
| Configure API Gateway to route CC00 sign-on to new service | Gateway rule |
| Implement legacy sync adapter (write-back to USRSEC during coexistence) | Bidirectional sync |
| Validate: existing CICS sessions continue to work | Integration test |

#### 1B: Reference Data Service

| Task | Deliverable |
|------|-------------|
| Build `carddemo-refdata` Spring Boot service | CRUD for types/categories/disclosure groups |
| Migrate DB2 tables + VSAM reference files to PostgreSQL | Data migration |
| Expose REST API: `/reference/types`, `/reference/categories`, `/reference/disclosure-groups` | OpenAPI spec |
| Implement VSAM file extract bridge (for legacy consumers) | Batch sync job |
| Validate: CICS programs still read correct reference data | Functional test |

**Rollback Plan:** Re-enable direct VSAM/DB2 access; gateway routes back to legacy.

**Exit Criteria:**
- [ ] All user logins route through new auth service
- [ ] Reference data API serves correct values matching legacy DB2/VSAM content
- [ ] Legacy batch jobs that read VSAM reference files continue working via sync

---

### Phase 2: Customer & Card — Read Path (Weeks 9–12)

**Objective:** Extract Customer and Card as read-only services; legacy retains write ownership temporarily.

| Task | Deliverable |
|------|-------------|
| Build `carddemo-customer` service | REST API for customer profile reads |
| Set up CDC (Change Data Capture) from CUSTDATA VSAM → PostgreSQL | Near-real-time sync |
| Build `carddemo-card` service (read-only initially) | REST API for card lookups |
| Set up CDC from CARDDATA VSAM → PostgreSQL | Near-real-time sync |
| Migrate XREF data into Account/Card relationship tables | Data model |
| Update Statement and Reporting programs to read from APIs (or cache) | Integration |
| Validate: data consistency between VSAM and PostgreSQL at 99.99% | Reconciliation report |

**Rollback Plan:** Revert API consumers to direct VSAM reads; CDC can be paused without data loss.

**Exit Criteria:**
- [ ] Customer and Card APIs serve data consistent with VSAM source
- [ ] CDC lag is < 5 seconds under normal load
- [ ] No legacy program is broken by the introduction of read APIs

---

### Phase 3: Account Service & Card Write Path (Weeks 13–18)

**Objective:** Make Account the system of record. This is the first phase with significant write-path changes.

| Task | Deliverable |
|------|-------------|
| Build `carddemo-account` service with full CRUD | REST + Command API |
| Implement Account domain model (balance, limits, status transitions) | Java domain entity |
| Migrate Account writes from `COACTUPC` to Account Service | Strangler route |
| Enable Card write path in `carddemo-card` service | Card update/create API |
| Implement anti-corruption layer: Account Service writes back to ACCTFILE VSAM | Dual-write bridge |
| Validate: Account balance matches after every operation | Parity tests |
| Performance test: Account Service handles peak CICS equivalent TPS | Load test |

**Rollback Plan:** Gateway routes account updates back to `COACTUPC` CICS program. Dual-write bridge ensures VSAM is always current.

**Exit Criteria:**
- [ ] Account Service is the primary writer for account updates
- [ ] VSAM ACCTFILE stays synchronized for legacy batch consumers
- [ ] Balance parity confirmed over 2 weeks of dual-write operation

---

### Phase 4: Transaction Entry & Bill Payment (Weeks 19–24)

**Objective:** Modernize the online transaction creation path and payment processing.

| Task | Deliverable |
|------|-------------|
| Build `carddemo-transaction` service (entry only — not posting) | Event-sourced transaction creation |
| Route CICS CT02 (transaction add) to new service | Gateway rule |
| Implement event publication (TransactionCreated event → Kafka) | Event schema |
| Build VSAM bridge: write new transactions back to TRANSACT file for legacy posting | Bridge adapter |
| Build `carddemo-payment` service | Payment orchestration (Saga) |
| Route CICS CB00 (bill payment) to new service | Gateway rule |
| Validate: transactions created via new service are correctly posted by legacy batch | End-to-end test |

**Rollback Plan:** Route CT02 and CB00 back to CICS. Bridge ensures no data loss — transactions exist in both systems.

**Exit Criteria:**
- [ ] Online transaction entry runs on new service
- [ ] Bill payments processed correctly with balance updates
- [ ] Legacy POSTTRAN batch job successfully processes transactions from the bridge

---

### Phase 5: Statement & Reporting (Weeks 25–28)

**Objective:** Replace legacy batch statement generation with modern report engine.

| Task | Deliverable |
|------|-------------|
| Build `carddemo-statement` service | HTML/PDF statement generation |
| Consume from Transaction, Account, Customer APIs | API integration |
| Implement report scheduling (replaces JCL CREASTMT) | Cron / Spring Scheduler |
| Validate: generated statements match legacy output field-by-field | Diff comparison |
| Route CICS CR00 (report request) to new service | Gateway rule |
| Deploy self-service statement portal (web UI) | Frontend component |

**Rollback Plan:** Legacy CREASTMT JCL remains available; can be re-enabled immediately.

**Exit Criteria:**
- [ ] Modern statements match legacy output format for all test accounts
- [ ] Statements generated within SLA (same-day for daily, T+1 for monthly)

---

### Phase 6: Authorization & Fraud (Weeks 29–34)

**Objective:** Replace IMS/DB2/MQ authorization engine with event-driven service.

| Task | Deliverable |
|------|-------------|
| Build `carddemo-authorization` service | Kafka/MQ consumer + PostgreSQL |
| Implement authorization business rules (approval/decline logic) | Domain service |
| Replace IMS HIDAM with PostgreSQL (or DynamoDB) auth store | Data migration |
| Replace DB2 fraud table with dedicated fraud analytics | Fraud service |
| Connect to existing MQ queues (same message format — backward compatible) | MQ adapter |
| Implement batch purge (replaces `CBPAUP0C`) | Scheduled job |
| Validate: authorization decisions match legacy for 10,000+ test cases | Decision parity |

**Rollback Plan:** Reconnect MQ trigger to legacy `COPAUA0C` program. IMS database remains available for 90 days post-cutover.

**Exit Criteria:**
- [ ] Authorization decisions are consistent with legacy (>99.9% match rate)
- [ ] Fraud flagging works correctly
- [ ] MQ response times are within SLA (< 500ms p99)

---

### Phase 7: Transaction Posting & Interest Calculation (Weeks 35–46)

**Objective:** The highest-risk migration — replace the financial calculation engine. Extended dual-run validation is mandatory.

| Sub-Phase | Duration | Activity |
|-----------|----------|----------|
| 7A: Build | 4 weeks | Implement Spring Batch posting job and interest calculator |
| 7B: Shadow Run | 4 weeks | Run new batch in parallel with legacy; compare every output field |
| 7C: Primary Cutover | 2 weeks | New batch becomes primary; legacy runs as shadow for verification |
| 7D: Decommission | 2 weeks | Legacy batch disabled; monitoring only |

| Task | Deliverable |
|------|-------------|
| Port `CBTRN02C` posting logic to Spring Batch | `carddemo-posting` job |
| Port `CBACT04C` interest calc to Spring Batch | `carddemo-interest` job |
| Implement exact decimal arithmetic (BigDecimal, RoundingMode.HALF_EVEN) | Arithmetic library |
| Build comprehensive parity test suite (1M+ synthetic transactions) | Test harness |
| Dual-run: both systems process same input; compare outputs | Reconciliation report |
| Validate over 3 complete billing cycles | Cycle-by-cycle comparison |
| Implement TCATBALF (category balance) in new posting job | Category balance service |
| Eliminate CLOSEFIL/OPENFIL dependency (no file locking in PostgreSQL) | Architecture change |

**Rollback Plan:** Immediately re-enable legacy batch jobs. VSAM files remain synchronized throughout 7A-7C via bridge adapters.

**Exit Criteria:**
- [ ] Zero discrepancies in 3 consecutive billing cycle comparisons
- [ ] Interest calculations match to the penny for all account groups
- [ ] Posting throughput meets SLA (process full daily volume in < 2 hours)
- [ ] Category balance reconciliation passes

---

### Phase 8: Branch Migration & Decommission (Weeks 47–50)

**Objective:** Replace file-based branch migration with API-based data sync; decommission legacy.

| Task | Deliverable |
|------|-------------|
| Replace CBEXPORT/CBIMPORT with API-to-API data transfer | ETL job or API sync |
| Final data migration: all remaining VSAM data → PostgreSQL | Migration script |
| Remove legacy bridge adapters and dual-write mechanisms | Code cleanup |
| Decommission CICS region | Infrastructure teardown |
| Decommission VSAM datasets | Storage cleanup |
| Archive legacy source code | Git tag + documentation |
| Final validation: all business functions operational without legacy | Full regression |

**Exit Criteria:**
- [ ] No traffic routes to legacy mainframe components
- [ ] All data resides in PostgreSQL/Kafka (no VSAM dependency)
- [ ] Monitoring confirms stable operation for 30 days post-decommission

---

## Coexistence Architecture (During Migration)

```
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway / Router                      │
│         (Routes by TransID to legacy OR modern service)       │
├──────────────────────────────┬──────────────────────────────┤
│       Modern Services        │        Legacy (CICS)          │
│                              │                               │
│  ┌──────────┐ ┌──────────┐  │  ┌──────────┐ ┌──────────┐  │
│  │Auth Svc  │ │Acct Svc  │  │  │COACTUPC  │ │CBTRN02C  │  │
│  │(Phase 1) │ │(Phase 3) │  │  │(Phase 3) │ │(Phase 7) │  │
│  └──────────┘ └──────────┘  │  └──────────┘ └──────────┘  │
│                              │                               │
├──────────────────────────────┼──────────────────────────────┤
│       PostgreSQL / Kafka     │     VSAM / DB2 / IMS         │
│       (Target state)         │     (Legacy state)           │
├──────────────────────────────┴──────────────────────────────┤
│                    Bridge Adapters                            │
│     (Bidirectional sync between modern DB and VSAM)          │
└─────────────────────────────────────────────────────────────┘
```

---

## Go/No-Go Decision Gates

Each phase has a decision gate before proceeding to production cutover:

| Gate | Criteria |
|------|----------|
| **Data Parity** | ≥ 99.99% field-level match between legacy and modern for 7 consecutive days |
| **Performance** | P99 latency within 20% of legacy baseline; throughput meets peak load |
| **Rollback Tested** | Rollback procedure executed successfully in staging environment |
| **Security Review** | SAST/DAST scans pass; no critical/high findings |
| **Business Sign-off** | Product owner approves functional validation results |
| **Operational Readiness** | Runbooks documented; on-call team trained; alerting configured |

---

## Timeline Summary (Gantt)

```
Week:  1    5    9    13   17   21   25   29   33   37   41   45   49
       |    |    |    |    |    |    |    |    |    |    |    |    |
P0:    ████                                                         Foundation
P1:         ████                                                    Identity + RefData
P2:              ████                                               Customer + Card (Read)
P3:                   ██████                                        Account + Card (Write)
P4:                             ██████                              Txn Entry + Payment
P5:                                   ████                          Statement
P6:                                        ██████                   Authorization
P7:                                              ████████████       Posting + Interest
P8:                                                          ████   Decommission
```

---

## Staffing Model

| Phase | Dev Team Size | Key Roles |
|-------|:------------:|-----------|
| 0 | 4 | 2 Platform Eng, 1 Architect, 1 DevOps |
| 1–2 | 6 | 3 Backend, 1 DBA, 1 QA, 1 DevOps |
| 3–4 | 8 | 4 Backend, 1 Frontend, 1 DBA, 1 QA, 1 Architect |
| 5 | 4 | 2 Backend, 1 Frontend, 1 QA |
| 6 | 6 | 3 Backend, 1 DBA, 1 QA, 1 Architect |
| 7 | 8 | 4 Backend, 1 DBA, 2 QA, 1 Architect |
| 8 | 4 | 2 Backend, 1 DevOps, 1 QA |
