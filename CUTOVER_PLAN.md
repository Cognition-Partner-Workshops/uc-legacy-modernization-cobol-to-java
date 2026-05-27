# CardDemo Cutover Plan

## 1. Overview

This plan sequences the migration of CardDemo from mainframe COBOL to Java/Spring Boot across six phases, ordered from **lowest-risk to highest-risk**. Each phase has defined entry/exit criteria, rollback procedures, and validation gates. The total estimated timeline is 18-24 months.

---

## 2. Migration Principles

1. **Parallel Run Always:** Legacy and modern systems run simultaneously during each phase. Traffic is gradually shifted.
2. **Data Synchronization First:** Establish bidirectional data sync before migrating any workload.
3. **Validate Before Cutover:** Every phase requires output parity testing (legacy vs. modern) before traffic routing.
4. **Rollback by Default:** Every cutover is reversible within 4 hours. If parity fails, traffic routes back to legacy automatically.
5. **Feature Flags:** Use feature flags to control traffic routing at the functional area level.
6. **No Big Bang:** No single cutover event. Each functional area migrates independently.

---

## 3. Pre-Migration Foundation (Phase 0)

**Duration:** 6-8 weeks
**Risk Level:** Minimal

### Objectives
- Establish infrastructure, CI/CD pipelines, and data synchronization
- Create the API gateway and service mesh
- Set up monitoring and observability

### Tasks

| # | Task | Duration | Dependencies |
|---|---|---|---|
| 0.1 | Provision Kubernetes cluster (EKS) and base infrastructure | 2 weeks | None |
| 0.2 | Set up PostgreSQL instances per service schema | 1 week | 0.1 |
| 0.3 | Deploy API Gateway (Spring Cloud Gateway) | 1 week | 0.1 |
| 0.4 | Establish Kafka cluster for domain events | 1 week | 0.1 |
| 0.5 | Build CI/CD pipelines (GitHub Actions → ArgoCD) | 2 weeks | 0.1 |
| 0.6 | Implement VSAM-to-PostgreSQL data sync (CDC bridge) | 3 weeks | 0.2 |
| 0.7 | Deploy monitoring stack (Prometheus, Grafana, ELK) | 1 week | 0.1 |
| 0.8 | Create integration test harness for parity testing | 2 weeks | 0.2 |
| 0.9 | Extract and document all VSAM record layouts to SQL DDL | 2 weeks | 0.2 |

### Data Migration Schema Mapping

| VSAM File | Record Layout | Target Table | Record Size |
|---|---|---|---|
| USRSEC | CSUSR01Y | `identity.users` | 80B |
| CUSTDAT | CVCUS01Y | `customer.customers` | 500B |
| ACCTDAT | CVACT01Y | `account.accounts` | 300B |
| CARDDAT | CVACT02Y | `card.cards` | 150B |
| CARDXREF | CVACT03Y | `card.card_xref` | 50B |
| TRANSACT | CVTRA05Y | `transaction.transactions` | 350B |
| DALYTRAN | CVTRA06Y | `transaction.daily_transactions` | 350B |
| TCATBALF | CVTRA01Y | `billing.category_balances` | 50B |
| DISCGRP | CVTRA02Y | `billing.disclosure_groups` | 50B |
| TRANTYPE | CVTRA03Y | `reference.transaction_types` | 60B |
| TRANCATG | CVTRA04Y | `reference.transaction_categories` | 60B |

### Exit Criteria
- [ ] All infrastructure provisioned and accessible
- [ ] CDC bridge replicating all VSAM files to PostgreSQL in real-time
- [ ] Parity test harness operational
- [ ] All record layouts converted to SQL DDL with test data loaded

---

## 4. Phase 1 -- Identity & Navigation (Lowest Risk)

**Duration:** 4-6 weeks
**Risk Level:** Low
**Functional Areas:** FA-1 (Authentication), FA-2 (Menu/Routing), FA-9 (User Admin)
**Strategy:** Rewrite
**Programs Migrated:** 6 programs, ~2,623 LOC

### Why First
- Self-contained: USRSEC file has no upstream dependencies
- Simple CRUD: No complex business logic or financial calculations
- Foundation: All subsequent services depend on authentication
- Reversible: Legacy sign-on can remain active alongside modern auth

### Implementation Steps

| # | Step | Duration | Validation |
|---|---|---|---|
| 1.1 | Build `identity-service` with Spring Security + OAuth2/OIDC | 2 weeks | Unit tests |
| 1.2 | Migrate user data from USRSEC VSAM to PostgreSQL | 1 week | Row count + field parity |
| 1.3 | Build user management REST API (CRUD for admin) | 1 week | API contract tests |
| 1.4 | Build SPA login page and navigation shell | 1 week | E2E tests |
| 1.5 | Parallel run: dual authentication (legacy 3270 + modern web) | 1 week | Login success rates |
| 1.6 | Route traffic: new users → modern, existing → either | 1 week | Monitoring |

### Cutover Sequence
```
Day 1-7:   Both systems active. Modern auth issues JWT; legacy issues COMMAREA.
Day 8-14:  50% of logins routed to modern system via feature flag.
Day 15-21: 100% routed to modern. Legacy auth remains warm standby.
Day 22+:   Legacy sign-on decommissioned after 7-day observation.
```

### Rollback Trigger
- Login failure rate exceeds 1%
- JWT validation errors in downstream services

### Exit Criteria
- [ ] All users can authenticate via modern system
- [ ] Admin CRUD operations functional
- [ ] JWT tokens accepted by API Gateway
- [ ] Zero authentication-related incidents for 7 days

---

## 5. Phase 2 -- Customer & Reference Data (Low Risk)

**Duration:** 4-6 weeks
**Risk Level:** Low
**Functional Areas:** FA-10 (Customer), FA-13 (Tran Type Mgmt), FA-14 (MQ Integration)
**Strategy:** Strangler Fig (Customer), Refactor (Tran Types), Rewrite (MQ)
**Programs Migrated:** 9 programs, ~6,145 LOC

### Why Second
- Customer data is read-heavy with minimal write operations
- Reference data (transaction types, categories) is stable and low-volume
- No financial calculations or balance mutations
- High value as shared services consumed by later phases

### Implementation Steps

| # | Step | Duration | Validation |
|---|---|---|---|
| 2.1 | Build `customer-service` REST API | 2 weeks | API contract tests |
| 2.2 | Build `reference-data-service` (tran types, categories, disclosure groups) | 2 weeks | Data parity vs DB2/VSAM |
| 2.3 | Migrate customer data from CUSTDAT VSAM to PostgreSQL | 1 week | Full record comparison |
| 2.4 | Port DB2 SQL for transaction type management to JPA | 1 week | SQL output parity |
| 2.5 | Replace MQ inquiry endpoints with REST APIs | 1 week | Response parity |
| 2.6 | Redirect legacy programs to read from new services (via API shim) | 1 week | Integration tests |

### Data Parity Validation
```
For each customer record in VSAM:
  1. Read via legacy COBOL program (CBCUS01C)
  2. Read via new customer-service REST API
  3. Compare all 500 bytes field-by-field
  4. Report any mismatches
  
Expected: 0 mismatches across all records.
```

### Cutover Sequence
```
Week 1-2:  Modern services deployed, reading from PostgreSQL (CDC-synced from VSAM).
Week 3:    Legacy programs shimmed to call modern APIs for reads.
Week 4:    Writes redirected to modern service (modern → PostgreSQL → reverse-sync → VSAM).
Week 5:    VSAM sync disabled. Modern service is source of truth.
Week 6:    Legacy customer programs decommissioned.
```

### Exit Criteria
- [ ] Customer Service serving 100% of reads
- [ ] Reference Data Service replacing DB2 direct access
- [ ] Weekly Control-M jobs (TRANEXTR, DISCGRP refresh) replaced by direct queries
- [ ] All downstream consumers using new APIs

---

## 6. Phase 3 -- Reporting & Branch Migration (Low-Medium Risk)

**Duration:** 4-6 weeks
**Risk Level:** Low-Medium
**Functional Areas:** FA-8 (Statements & Reporting), FA-11 (Branch Migration)
**Strategy:** Rewrite (Reporting), Refactor (Migration)
**Programs Migrated:** 6 programs, ~3,521 LOC

### Why Third
- Reporting is output-only -- no writes to core data
- Branch migration is an operational tool, not customer-facing
- Output can be diff-tested (text/HTML statements) against legacy
- Exercises batch processing patterns needed for Phase 5

### Implementation Steps

| # | Step | Duration | Validation |
|---|---|---|---|
| 3.1 | Build `report-service` with JasperReports/PDF generation | 2 weeks | Output diff vs legacy |
| 3.2 | Port statement generation (CBSTM03A/B) to Spring Batch | 2 weeks | Statement-level parity |
| 3.3 | Build `migration-service` for export/import | 2 weeks | Export file byte-level comparison |
| 3.4 | Replace TDQ-based report submission (CORPT00C) with REST trigger | 1 week | Report generation end-to-end |
| 3.5 | Convert CREASTMT JCL to Spring Batch + K8s CronJob | 1 week | Monthly run validation |

### Statement Parity Testing
```
For each account with transactions:
  1. Run legacy CBSTM03A to produce plain-text statement
  2. Run modern report-service for same account/period
  3. Diff output line-by-line (ignoring formatting differences)
  4. Verify: amounts match, transaction counts match, totals match
```

### Cutover Sequence
```
Week 1-3:  Modern reporting runs in shadow mode alongside legacy.
Week 4:    Compare all monthly statements. Fix any discrepancies.
Week 5:    Route report requests to modern service.
Week 6:    Legacy report programs decommissioned.
```

### Exit Criteria
- [ ] Statements match legacy output within formatting tolerance
- [ ] Export/import produces identical data when round-tripped
- [ ] Monthly batch report job running on Kubernetes schedule
- [ ] CREASTMT JCL decommissioned

---

## 7. Phase 4 -- Card & Account Management (Medium Risk)

**Duration:** 6-8 weeks
**Risk Level:** Medium
**Functional Areas:** FA-3 (Account Management), FA-4 (Credit Card Management)
**Strategy:** Strangler Fig
**Programs Migrated:** 5 programs, ~9,083 LOC (includes COACTUPC at 4,236 LOC)

### Why Fourth
- Core entity management with write operations
- Complex validation logic (especially COACTUPC) requires thorough testing
- Account data is consumed by almost every other context
- Phase 2 services (Customer, Reference) must be stable first

### Implementation Steps

| # | Step | Duration | Validation |
|---|---|---|---|
| 4.1 | Build `account-service` REST API with full validation | 3 weeks | Validation parity tests |
| 4.2 | Build `card-service` REST API with XREF management | 2 weeks | CRUD parity tests |
| 4.3 | Extract COACTUPC validation rules into a validation library | 2 weeks | Field-level test suite |
| 4.4 | Implement card-to-account XREF as service-owned relationship | 1 week | Referential integrity tests |
| 4.5 | Strangler: route reads to modern, writes dual-write | 1 week | Data consistency checks |
| 4.6 | Strangler: route all traffic to modern | 1 week | Monitoring |

### COACTUPC Validation Extraction

The `COACTUPC` program (4,236 LOC) contains these validation patterns that must be preserved:

| Validation | COBOL Pattern | Java Equivalent |
|---|---|---|
| US Phone Number | REDEFINES with area code parsing | Regex + Bean Validation |
| Signed Number (9V2) | COMP-3 with sign check | BigDecimal validation |
| Alpha-Only | Character-by-character scan | Pattern `[a-zA-Z]+` |
| Alphanumeric | Character scan with length check | Pattern `[a-zA-Z0-9]+` |
| Mandatory Field | LOW-VALUES/SPACES check | `@NotBlank` |
| Yes/No Flag | 88-level conditions | Enum validation |
| Date Format | CODATECN copybook routines | `LocalDate.parse()` |

### XREF Decomposition

```
Current: Single CARDXREF VSAM file (Card Num → Cust ID + Acct ID)

Target:
  card-service owns:     cards table (card_num, account_id, ...)
  customer-service owns: customer_accounts table (customer_id, account_id)
  
  XREF lookups become:
    Card → Account: card-service.getAccountByCard(cardNum)
    Card → Customer: card-service.getCard(cardNum) → customer-service.getByAccount(acctId)
```

### Cutover Sequence
```
Week 1-4:  Modern services deployed, reading from PostgreSQL (CDC-synced).
Week 5:    Reads routed to modern services via API Gateway.
Week 6:    Writes dual-written (modern + VSAM).
Week 7:    VSAM write disabled. Modern is source of truth.
Week 8:    Legacy programs decommissioned after observation period.
```

### Rollback Trigger
- Any account balance discrepancy detected
- Validation rule producing different accept/reject decision than legacy
- XREF lookup returning different results

### Exit Criteria
- [ ] Account and Card services handling 100% of traffic
- [ ] XREF fully decomposed into service-owned relationships
- [ ] All validation rules producing identical outcomes to COACTUPC
- [ ] Zero data integrity incidents for 14 days

---

## 8. Phase 5 -- Transaction Processing & Financial Operations (High Risk)

**Duration:** 8-10 weeks
**Risk Level:** High
**Functional Areas:** FA-5 (Online Transactions), FA-6 (Batch Processing), FA-7 (Interest/Billing)
**Strategy:** Strangler Fig (Online), Refactor (Batch), Refactor (Financial)
**Programs Migrated:** 8 programs, ~4,261 LOC

### Why Fifth
- Highest financial risk: balance mutations, interest calculations, payment processing
- Requires all upstream services (Account, Card, Customer, Reference) to be stable
- Batch processing pattern (POSTTRAN) is the most complex migration
- Monthly billing cycle provides natural testing boundaries

### Implementation Steps

| # | Step | Duration | Validation |
|---|---|---|---|
| 5.1 | Build `transaction-service` for online CRUD | 2 weeks | Transaction parity |
| 5.2 | Port CBTRN02C (POSTTRAN) to Spring Batch | 3 weeks | Posting parity (see below) |
| 5.3 | Port CBACT04C (interest calc) to Spring Batch | 2 weeks | Interest amount parity |
| 5.4 | Port COBIL00C (bill payment) to REST endpoint | 1 week | Payment parity |
| 5.5 | Replace COMBTRAN (SORT utility) with SQL merge | 1 week | Record count parity |
| 5.6 | Replace Control-M schedules with K8s CronJobs | 1 week | Schedule parity |
| 5.7 | Shadow run: modern batch alongside legacy for 2 cycles | 4 weeks | Full output comparison |

### POSTTRAN Parity Testing Protocol

```
Given: Same DALYTRAN input file
  
Legacy run produces:
  - Updated TRANSACT file (posted transactions)
  - Updated ACCTDAT (account balances)
  - Updated TCATBALF (category balances)
  - DALYREJS file (rejected transactions)

Modern run produces:
  - Updated transactions table
  - Updated accounts table (balances)
  - Updated category_balances table
  - Rejected transactions queue/table

Validation:
  1. Count: posted records match
  2. Count: rejected records match  
  3. For each posted transaction: amount, timestamps, card mapping match
  4. For each account: new balance = old balance + sum(posted amounts)
  5. For each category balance: new total matches
  6. Decimal precision: all amounts match to the cent (COMP-3 ↔ BigDecimal)
```

### Interest Calculation Parity

```
For each account:
  Legacy: CBACT04C reads TCATBALF → DISCGRP → calculates interest
  Modern: billing-service reads category_balances → disclosure_groups → calculates

Validation:
  1. Interest amount per account per category must match exactly
  2. Total interest across all accounts must match
  3. Special attention to COMP-3 → BigDecimal rounding behavior
  4. Validate against disclosure group rate precision (S9(04)V99)
```

### Control-M to Kubernetes Schedule Mapping

| Control-M Folder | Schedule | K8s CronJob |
|---|---|---|
| DAILY-TransactionBackup | Daily, all days | `0 1 * * *` (1:00 AM) |
| WEEKLY-DisclosureGroupsRefresh | Saturdays | `0 2 * * 6` (2:00 AM Sat) -- *eliminated (direct DB)* |
| WEEKLY-TransactionTypesDBRefresh | Saturdays | `0 3 * * 6` (3:00 AM Sat) -- *eliminated (direct DB)* |
| MONTHLY-InterestCalculation | Monthly | `0 0 1 * *` (midnight, 1st of month) |

### Cutover Sequence
```
Month 1:     Modern batch runs in shadow mode. Compare outputs daily.
Month 2:     Fix any discrepancies. Re-run shadow comparison.
Month 3/W1:  Online transactions routed to modern (reads + writes).
Month 3/W2:  Daily batch (POSTTRAN) switched to modern. Legacy warm standby.
Month 3/W3:  Monthly interest calculation run on modern.
Month 3/W4:  Bill payment routed to modern.
Month 4:     Full observation period. Legacy decommissioned after 30 days clean.
```

### Rollback Trigger
- Any account balance discrepancy after batch run
- Interest calculation delta > $0.01 for any account
- Transaction posting producing different accept/reject outcomes
- Batch job failure without successful compensation

### Exit Criteria
- [ ] Two consecutive monthly cycles with zero financial discrepancies
- [ ] Daily batch posting producing identical results for 30 days
- [ ] Interest calculations matching to the cent for all accounts
- [ ] Bill payment producing correct balance adjustments
- [ ] All Control-M jobs replaced by K8s CronJobs
- [ ] Legacy batch decommissioned

---

## 9. Phase 6 -- Authorization Processing (Highest Risk)

**Duration:** 6-8 weeks
**Risk Level:** High
**Functional Areas:** FA-12 (Authorization Processing -- IMS/DB2/MQ)
**Strategy:** Rewrite
**Programs Migrated:** 8 programs, ~4,344 LOC

### Why Last
- Most complex integration surface: IMS + DB2 + MQ + VSAM
- IMS hierarchical database requires complete schema redesign
- MQ trigger processing requires architectural change to event-driven
- Optional module -- can be deferred if core migration encounters delays
- Depends on all core services being stable

### Implementation Steps

| # | Step | Duration | Validation |
|---|---|---|---|
| 6.1 | Design relational schema to replace IMS hierarchical DB | 2 weeks | Schema review |
| 6.2 | Build `authorization-service` with event-driven processing | 3 weeks | Authorization flow tests |
| 6.3 | Migrate IMS data to PostgreSQL | 1 week | Data parity |
| 6.4 | Replace MQ triggers with Kafka consumers | 1 week | Message flow parity |
| 6.5 | Port authorization purge batch (CBPAUP0C) | 1 week | Purge logic parity |
| 6.6 | Parallel run and cutover | 2 weeks | Authorization outcome parity |

### IMS to Relational Mapping

```
IMS Hierarchical:
  DBPAUTP0 (Primary segments)
    └── DBPAUTX0 (Extension segments)

PostgreSQL Relational:
  authorizations (
    auth_id         BIGINT PRIMARY KEY,
    card_number     VARCHAR(16),
    auth_amount     DECIMAL(11,2),
    auth_status     VARCHAR(1),
    auth_timestamp  TIMESTAMP,
    merchant_id     BIGINT,
    ...
  )
  
  authorization_details (
    detail_id       BIGINT PRIMARY KEY,
    auth_id         BIGINT REFERENCES authorizations(auth_id),
    detail_type     VARCHAR(2),
    detail_data     TEXT,
    ...
  )
```

### Exit Criteria
- [ ] Authorization requests processed correctly via event-driven architecture
- [ ] Pending authorization screens functional in modern UI
- [ ] Purge batch running on schedule
- [ ] IMS and MQ fully decommissioned

---

## 10. Post-Migration (Phase 7)

**Duration:** 4-6 weeks
**Risk Level:** Low

### Tasks
| # | Task | Duration |
|---|---|---|
| 7.1 | Decommission all VSAM files after 30-day retention | 2 weeks |
| 7.2 | Remove CDC bridge and VSAM sync infrastructure | 1 week |
| 7.3 | Archive legacy COBOL source code | 1 week |
| 7.4 | Decommission mainframe CICS region | 1 week |
| 7.5 | Remove Control-M job definitions | 1 week |
| 7.6 | Final performance tuning and optimization | 2 weeks |
| 7.7 | Update operational runbooks and on-call procedures | 1 week |
| 7.8 | Knowledge transfer to operations team | 2 weeks |

---

## 11. Timeline Summary

```
Month:   1    2    3    4    5    6    7    8    9   10   11   12   ...
Phase 0: ████████
Phase 1:      ████████
Phase 2:           ████████
Phase 3:                ████████
Phase 4:                     ████████████
Phase 5:                               ████████████████
Phase 6:                                         ████████████
Phase 7:                                                   ████████

Legend: █ = Active development and testing
```

| Phase | Duration | Cumulative | Risk | Programs | LOC |
|---|---|---|---|---|---|
| Phase 0: Foundation | 6-8 wks | 6-8 wks | Minimal | 0 | 0 |
| Phase 1: Identity | 4-6 wks | 10-14 wks | Low | 6 | 2,623 |
| Phase 2: Customer/Reference | 4-6 wks | 14-20 wks | Low | 9 | 6,145 |
| Phase 3: Reporting/Migration | 4-6 wks | 18-26 wks | Low-Med | 6 | 3,521 |
| Phase 4: Account/Card | 6-8 wks | 24-34 wks | Medium | 5 | 9,083 |
| Phase 5: Transactions/Billing | 8-10 wks | 32-44 wks | High | 8 | 4,261 |
| Phase 6: Authorization | 6-8 wks | 38-52 wks | High | 8 | 4,344 |
| Phase 7: Decommission | 4-6 wks | 42-58 wks | Low | -- | -- |
| **Total** | | **42-58 weeks** | | **42** | **~30,000** |

---

## 12. Go/No-Go Decision Framework

Before each phase cutover, the following checklist must be satisfied:

| # | Criterion | Threshold |
|---|---|---|
| 1 | Data parity test | 100% field-level match |
| 2 | Functional parity test | 100% use cases pass |
| 3 | Performance benchmark | Response time within 20% of legacy |
| 4 | Financial accuracy | Zero discrepancy ($0.00 tolerance) |
| 5 | Error rate | < 0.1% in 7-day observation |
| 6 | Rollback tested | Successful rollback drill completed |
| 7 | Stakeholder sign-off | Product owner + Tech lead approval |
| 8 | Monitoring coverage | All services have alerts configured |
