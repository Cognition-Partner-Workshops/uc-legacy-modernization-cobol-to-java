# CardDemo Cutover Plan

## Executive Summary

This document defines a phased migration sequence for the CardDemo mainframe application, ordered from **lowest-risk to highest-risk**. Each phase includes specific programs and modules to migrate, entry and exit criteria, rollback strategies, and estimated timelines. The plan assumes a hybrid modernization approach as defined in the Modernization Blueprint, using Strangler Fig, Replatform, Refactor, and Rewrite strategies as appropriate per functional area.

---

## 1. Migration Principles

1. **Lowest-risk first** -- Start with isolated, well-bounded contexts that have minimal cross-system dependencies.
2. **Read before write** -- Migrate read-only operations before write operations within each domain.
3. **Online before batch** -- Migrate interactive CICS programs before batch processing, since batch jobs have tighter coupling and higher financial risk.
4. **Parallel run** -- For financial processing (transaction posting, interest calculation), run legacy and modern systems in parallel and reconcile before cutover.
5. **Reversibility** -- Every phase must have a documented rollback path that restores the previous state within the agreed Recovery Time Objective (RTO).
6. **Data consistency** -- VSAM-to-RDBMS data synchronization must be validated at each phase boundary before proceeding.

---

## 2. Prerequisites (Phase 0)

**Duration:** 4-6 weeks
**Risk Level:** Low
**Strategy:** Foundation setup -- no legacy code changes

### 2.1 Deliverables

| # | Deliverable | Description |
|---|-------------|-------------|
| P0-1 | Target RDBMS provisioned | PostgreSQL database with schema matching VSAM data model (see Domain Decomposition, Section 6) |
| P0-2 | Data migration pipeline | ETL pipeline to load VSAM data (from `app/data/ASCII/` format) into RDBMS |
| P0-3 | API Gateway deployed | Spring Cloud Gateway (or equivalent) to serve as the Strangler Fig facade |
| P0-4 | CI/CD pipeline | Build, test, deploy pipeline for Java/Spring Boot services |
| P0-5 | Monitoring & observability | Logging, metrics, and alerting for new services (ELK/Prometheus/Grafana) |
| P0-6 | Test data loaded | Production-representative data loaded into RDBMS from `app/data/ASCII/` samples |
| P0-7 | Change Data Capture (CDC) | Bidirectional sync mechanism between VSAM and RDBMS for dual-run period |

### 2.2 Entry Criteria
- Project charter approved
- Target architecture reviewed and signed off
- Infrastructure provisioned (cloud accounts, CI/CD, database)

### 2.3 Exit Criteria
- RDBMS schema created and validated against copybook definitions
- Sample data loaded and record counts verified against VSAM sources
- API Gateway routing traffic to a health-check endpoint
- CI/CD pipeline deploying a "hello world" Spring Boot service
- CDC mechanism tested with sample VSAM updates

### 2.4 Rollback
- No legacy changes; rollback is simply decommissioning new infrastructure

---

## 3. Phase 1 -- Peripheral Services (Lowest Risk)

**Duration:** 4-6 weeks
**Risk Level:** Low
**Bounded Contexts:** BC-1 (Identity), BC-9 (Reference Data); **Functional Area:** FA-12 (Utilities)
**Strategy:** Rewrite (Identity) + Replatform (Reference Data, Utilities)

### 3.1 Scope

| Program | Current Function | Target | Strategy |
|---------|-----------------|--------|----------|
| COSGN00C | User signon (USRSEC VSAM read, plaintext password compare) | Spring Security + JWT authentication service | Rewrite |
| COUSR00C | User list (admin) | Spring Boot REST CRUD | Rewrite |
| COUSR01C | User add (admin) | Spring Boot REST CRUD | Rewrite |
| COUSR02C | User update (admin) | Spring Boot REST CRUD | Rewrite |
| COUSR03C | User delete (admin) | Spring Boot REST CRUD | Rewrite |
| COTRTLIC | Transaction type list/delete (DB2) | Spring Data JPA CRUD | Replatform |
| COTRTUPC | Transaction type add/edit (DB2) | Spring Data JPA CRUD | Replatform |
| COBTUPDT | Batch transaction type update | Spring Batch job | Replatform |
| CSUTLDTC | Date validation utility | Java utility class (java.time) | Rewrite |

### 3.2 Data Migration

| Source | Target | Records | Validation |
|--------|--------|---------|------------|
| USRSEC (VSAM) | `users` table | ~50 | Count match, password hash migration |
| DB2 transaction types | `transaction_types` table | ~100 | Count match, field-level comparison |

### 3.3 Cutover Steps

1. Deploy Identity Service with JWT token issuance
2. Deploy Reference Data Service with transaction type CRUD
3. Configure API Gateway to route `/auth/*` and `/api/transaction-types/*` to new services
4. Migrate USRSEC data to `users` table (hash passwords during migration)
5. Migrate DB2 transaction types to PostgreSQL
6. Enable new Identity Service -- API Gateway validates JWT for all downstream requests
7. Update CICS COSGN00C to issue JWT tokens alongside COMMAREA (dual-auth period)
8. After validation, disable COSGN00C and route all auth through new service

### 3.4 Rollback Strategy

- **Trigger:** Authentication failure rate > 5% or user-reported login failures
- **Action:** API Gateway reverts auth routing to legacy COSGN00C within 5 minutes
- **Data:** USRSEC VSAM file remains unchanged; new `users` table is supplementary
- **RTO:** < 15 minutes

### 3.5 Exit Criteria

- [ ] All users can authenticate via new Identity Service
- [ ] JWT tokens accepted by API Gateway for downstream routing
- [ ] Admin CRUD for users operational via new service
- [ ] Transaction type CRUD operational via new service
- [ ] Legacy COSGN00C and COUSR* programs decommissioned
- [ ] Zero authentication errors in production for 48 hours

---

## 4. Phase 2 -- Read-Only Services (Low Risk)

**Duration:** 6-8 weeks
**Risk Level:** Low-Medium
**Bounded Contexts:** BC-7 (Reporting); **Functional Area:** FA-15 (VSAM-MQ Integration)
**Strategy:** Rewrite (Reporting, VSAM-MQ)

### 4.1 Scope

| Program | Current Function | Target | Strategy |
|---------|-----------------|--------|----------|
| CORPT00C | Report request UI (submits JCL via TDQ) | REST API to trigger Spring Batch report jobs | Rewrite |
| CBTRN03C | Batch transaction report generation | Spring Batch job with JasperReports/CSV output | Rewrite |
| CODATE01 | MQ date service | REST endpoint `GET /api/system-date` | Rewrite |
| COACCT01 | MQ account inquiry | REST endpoint `GET /api/accounts/{id}` (preview of Phase 3) | Rewrite |
| COMEN01C | Main menu (regular user) | React/Angular SPA routing | Rewrite |
| COADM01C | Admin menu | React/Angular SPA admin routing | Rewrite |

### 4.2 Data Migration

| Source | Target | Notes |
|--------|--------|-------|
| TRANSACT (VSAM) | `transactions` table | Read-only replica for reporting; CDC-synced from legacy |
| MQ queues (CDRA, CDRD) | REST endpoints | No data migration; protocol change only |

### 4.3 Cutover Steps

1. Deploy Reporting Service with Spring Batch report jobs
2. Deploy web frontend (SPA) with menu routing
3. Configure API Gateway to route `/api/reports/*` to Reporting Service
4. Sync TRANSACT data to RDBMS via CDC
5. Validate report output matches legacy reports (byte-level comparison for 3 months of data)
6. Replace MQ-based date and account inquiry with REST endpoints
7. Enable new reporting UI; users access reports via web instead of 3270 BMS screens
8. Decommission CORPT00C BMS map and TDQ-based JCL submission

### 4.4 Rollback Strategy

- **Trigger:** Report output discrepancies > 0.1% or report generation failures
- **Action:** Re-enable CORPT00C and restore TDQ-based job submission
- **Data:** TRANSACT VSAM remains primary; RDBMS replica is supplementary
- **RTO:** < 30 minutes

### 4.5 Exit Criteria

- [ ] All report types (monthly, yearly, custom) generate identical output to legacy
- [ ] MQ services replaced by REST endpoints with equivalent response payloads
- [ ] Web frontend serves main menu and admin menu
- [ ] Legacy menu programs (COMEN01C, COADM01C) decommissioned
- [ ] Report generation SLA met (< 5 minutes for monthly report)

---

## 5. Phase 3 -- Core Domain Services (Medium Risk)

**Duration:** 10-14 weeks
**Risk Level:** Medium
**Bounded Contexts:** BC-3 (Card), BC-2 (Account), BC-5 (Bill Payment)
**Strategy:** Strangler Fig (Card, Account) + Refactor (Bill Payment)

### 5.1 Scope

#### 5.1.1 Card Management (Strangler Fig)

| Program | Current Function | Target | Migration Order |
|---------|-----------------|--------|-----------------|
| COCRDLIC | Card list with pagination (VSAM BROWSE) | `GET /api/cards?accountId={id}&page={n}` | 1st (read-only) |
| COCRDSLC | Card detail view | `GET /api/cards/{cardNumber}` | 2nd (read-only) |
| COCRDUPC | Card update | `PUT /api/cards/{cardNumber}` | 3rd (write) |

#### 5.1.2 Account Management (Strangler Fig)

| Program | Current Function | Target | Migration Order |
|---------|-----------------|--------|-----------------|
| COACTVWC | Account view (reads ACCTDAT, CUSTDAT, CARDDAT, CARDXREF) | `GET /api/accounts/{id}` (includes customer + cards) | 4th (read-only) |
| COACTUPC | Account update (4,237 lines, 40+ validation rules) | `PUT /api/accounts/{id}` with validation middleware | 5th (write -- highest risk in this phase) |

#### 5.1.3 Bill Payment (Refactor)

| Program | Current Function | Target | Migration Order |
|---------|-----------------|--------|-----------------|
| COBIL00C | Pay full balance, create transaction, update account | Payment Service orchestrating Account + Transaction APIs | 6th (after Account + Transaction APIs available) |

### 5.2 Data Migration

| Source | Target | Strategy |
|--------|--------|----------|
| CARDDAT (VSAM) | `cards` table | Full migration + CDC sync during dual-run |
| CARDXREF (VSAM) | `card_xref` table | Full migration + CDC sync |
| ACCTDAT (VSAM) | `accounts` table | Full migration + CDC sync |
| CUSTDAT (VSAM) | `customers` table | Full migration + CDC sync |

### 5.3 Strangler Fig Implementation Detail

```
                    +-----------------+
                    |  API Gateway    |
                    | (Spring Cloud)  |
                    +--------+--------+
                             |
              +--------------+--------------+
              |                             |
     +--------v--------+          +--------v--------+
     |  New Card        |          |  Legacy CICS    |
     |  Service (Java)  |          |  (COBOL)        |
     +--------+---------+          +--------+--------+
              |                             |
     +--------v--------+          +--------v--------+
     |  PostgreSQL      |   CDC   |  VSAM Files     |
     |  (cards, xref)   |<------->|  (CARDDAT, etc) |
     +-----------------+          +-----------------+
```

**Traffic routing progression:**
1. Week 1-2: 0% new / 100% legacy (shadow mode -- new service processes requests but responses are discarded; compare with legacy responses)
2. Week 3-4: 10% new / 90% legacy (canary release for card list read-only)
3. Week 5-6: 50% new / 50% legacy (card list + card view)
4. Week 7-8: 90% new / 10% legacy (add card update; legacy handles fallback)
5. Week 9-10: 100% new / 0% legacy (full cutover for card management)
6. Week 11-14: Repeat for Account Management (view first, then update)

### 5.4 Validation Rules Migration (COACTUPC)

The 4,237-line COACTUPC program contains 40+ validation rules that must be preserved exactly. Key validations to test:

| Field | Validation Rule | COBOL Reference |
|-------|----------------|-----------------|
| Account ID | 11-digit numeric, must exist in ACCTDAT | COACTUPC lines 400-420 |
| SSN | 9-digit numeric, format validation | CSUTLDWY copybook |
| Phone Number | 15-character, format validation | CSUTLDWY copybook |
| Date of Birth | Valid date (YYYY-MM-DD), age check | CSUTLDWY copybook |
| Credit Limit | Signed numeric S9(10)V99, range check | COACTUPC lines 500-520 |
| FICO Score | 3-digit numeric (300-850 range) | COACTUPC lines 540-560 |
| Zip Code | 10-character, format validation | COACTUPC lines 580-600 |
| Account Status | Y/N only | COACTUPC lines 620-640 |

### 5.5 Cutover Steps

1. Deploy Card Service with read endpoints (list, view)
2. Enable shadow mode -- Card Service processes requests alongside legacy
3. Compare responses for 1 week; fix discrepancies
4. Begin canary rollout for card list (10% -> 50% -> 100%)
5. Enable card update endpoint after read path is stable
6. Deploy Account Service with view endpoint
7. Shadow mode for account view; compare responses
8. Canary rollout for account view
9. Deploy account update with full validation rule set
10. Extensive testing of all 40+ validation rules against legacy behavior
11. Canary rollout for account update (10% -> 50% -> 100%)
12. Deploy Payment Service calling Account + Transaction APIs
13. Test bill payment end-to-end with test accounts
14. Enable bill payment in production

### 5.6 Rollback Strategy

- **Trigger:** Data inconsistency between RDBMS and VSAM, validation rule discrepancy, or transaction failure rate > 1%
- **Action:** API Gateway reverts routing to legacy CICS programs per-service (Card, Account, or Payment independently)
- **Data:** CDC ensures both VSAM and RDBMS are synchronized; rollback simply changes routing
- **RTO:** < 10 minutes per service (API Gateway route change)
- **Special:** COACTUPC validation rule failures trigger immediate rollback of account update only; read operations remain on new service

### 5.7 Exit Criteria

- [ ] Card list pagination produces identical results to COCRDLIC for all test accounts
- [ ] Card view displays all fields matching COCRDSLC output
- [ ] Card update persists changes correctly (verified in both RDBMS and VSAM via CDC)
- [ ] Account view matches COACTVWC output including associated customer and card data
- [ ] All 40+ COACTUPC validation rules produce identical accept/reject decisions
- [ ] Bill payment creates correct transaction record and updates account balance
- [ ] Zero data inconsistencies between RDBMS and VSAM for 2 weeks
- [ ] Legacy CICS programs for Card, Account, and Bill Payment decommissioned

---

## 6. Phase 4 -- Financial Processing (High Risk)

**Duration:** 12-16 weeks
**Risk Level:** High
**Bounded Contexts:** BC-4 (Transaction Processing), BC-6 (Financial Calculations), BC-7 (Reporting & Statements -- statement generation subset)
**Strategy:** Strangler Fig (Transaction online), Replatform (Batch posting, Interest calc), Rewrite (Statements)

### 6.1 Scope

#### 6.1.1 Transaction Online Programs

| Program | Current Function | Target | Migration Order |
|---------|-----------------|--------|-----------------|
| COTRN00C | Transaction list with pagination | `GET /api/transactions?accountId={id}&page={n}` | 1st (read-only) |
| COTRN01C | Transaction detail view | `GET /api/transactions/{id}` | 2nd (read-only) |
| COTRN02C | Transaction add | `POST /api/transactions` | 3rd (write) |

#### 6.1.2 Batch Processing

| Program | Current Function | Target | Migration Order |
|---------|-----------------|--------|-----------------|
| CBTRN02C | Daily transaction posting (DALYTRAN -> TRANSACT + ACCTDAT) | Spring Batch: ItemReader(DALYTRAN) -> ItemProcessor(validate) -> ItemWriter(TRANSACT + Account API) | 4th |
| CBACT04C | Interest calculation (TCATBALF -> DISCGRP -> ACCTDAT) | Spring Batch: Read TCATBALF -> Calculate interest per BigDecimal -> Update Account API | 5th |
| CBSTM03A/B | Statement generation (text + HTML) | Spring Batch + Thymeleaf/FreeMarker template engine | 6th |

### 6.2 Data Migration

| Source | Target | Strategy |
|--------|--------|----------|
| TRANSACT (VSAM) | `transactions` table | Full migration (already CDC-synced from Phase 2) |
| DALYTRAN (sequential) | `daily_transactions` staging table | Batch input file -> staging table |
| DALYREJS (sequential) | `rejected_transactions` table | New table for reject tracking |
| TCATBALF (VSAM) | `transaction_category_balances` table | Full migration + CDC |
| DISCGRP (VSAM) | `discount_groups` table | Full migration (reference data) |

### 6.3 Parallel Run Strategy (Critical)

For financial batch processing, the parallel run is **mandatory** before cutover:

```
              +-------------------+     +-------------------+
              | Legacy Batch      |     | New Spring Batch  |
              | (COBOL/JCL)       |     | (Java)            |
              +--------+----------+     +--------+----------+
                       |                         |
              +--------v----------+     +--------v----------+
              | VSAM Files        |     | RDBMS Tables      |
              | (TRANSACT, etc)   |     | (transactions)    |
              +--------+----------+     +--------+----------+
                       |                         |
                       +----------+--------------+
                                  |
                       +----------v----------+
                       | Reconciliation      |
                       | Engine              |
                       | (compare outputs)   |
                       +---------------------+
```

**Parallel run procedure:**
1. Run legacy batch cycle (CLOSEFIL -> POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT -> OPENFIL)
2. Run new Spring Batch jobs with same input data
3. Compare outputs:
   - Transaction records: field-by-field comparison
   - Account balances: exact decimal match (BigDecimal vs. COMP-3)
   - Interest calculations: exact decimal match
   - Statement content: text comparison (ignoring formatting differences)
   - Reject records: same records rejected with same reason codes
4. Investigate and resolve any discrepancies
5. Repeat for 3 consecutive batch cycles with zero discrepancies before cutover approval

### 6.4 Arithmetic Precision Validation

COBOL uses fixed-point decimal arithmetic with `COMP-3` (packed decimal) and `PIC S9(n)V99` (display numeric with implied decimal). Java must use `BigDecimal` with matching precision:

| COBOL Type | Java Equivalent | Example |
|-----------|----------------|---------|
| PIC S9(10)V99 | BigDecimal(12,2) | Account balance: -9999999999.99 to 9999999999.99 |
| PIC S9(09)V99 | BigDecimal(11,2) | Transaction amount |
| PIC 9(04) | int | Category code |
| COMP-3 (interest rate) | BigDecimal(7,4) | Interest rate with 4 decimal places |

**Critical test cases:**
- Rounding: COBOL truncates; Java `BigDecimal` must use `RoundingMode.DOWN` to match
- Negative balances: Verify sign handling for credits vs. debits
- Zero-balance accounts: Edge case for interest calculation (divide by zero prevention)
- Maximum values: Test with PIC S9(10)V99 maximum (9,999,999,999.99)

### 6.5 Cutover Steps

1. Deploy Transaction Query Service (list, view -- read-only)
2. Shadow mode for transaction list; compare with COTRN00C output
3. Canary rollout for transaction queries (10% -> 100%)
4. Deploy Transaction Write Service (add)
5. Enable transaction add with dual-write to both VSAM and RDBMS
6. Validate dual-write consistency for 1 week
7. Deploy Spring Batch transaction posting job
8. **Parallel run #1:** Run legacy POSTTRAN alongside new posting job; reconcile
9. **Parallel run #2:** Fix discrepancies; run again
10. **Parallel run #3:** Confirm zero discrepancies
11. Deploy Spring Batch interest calculation job
12. **Parallel run #4:** Run legacy INTCALC alongside new interest calc; reconcile
13. **Parallel run #5-6:** Fix and re-validate interest calculations
14. Deploy Spring Batch statement generation job
15. Compare generated statements (text + HTML) with legacy output
16. Cutover approval: 3 consecutive parallel runs with zero financial discrepancies
17. Switch batch cycle to new Spring Batch jobs
18. Monitor for 2 weeks before decommissioning legacy batch

### 6.6 Rollback Strategy

- **Trigger:** Any financial discrepancy (balance mismatch, incorrect interest, missing transactions), or batch job failure
- **Action:**
  - Online: API Gateway reverts transaction routing to legacy CICS
  - Batch: Revert JCL job schedule to run legacy COBOL batch programs
  - Data: Restore VSAM files from most recent backup (GDG generation)
- **Data Recovery:** GDG (Generation Data Groups) provide automatic versioning of batch output files; restore previous generation
- **RTO:** < 1 hour for online; < 4 hours for batch (next scheduled cycle)
- **Financial controls:** All financial discrepancies reported immediately to finance team

### 6.7 Exit Criteria

- [ ] Transaction list/view/add produces identical results to legacy
- [ ] 3 consecutive parallel runs with zero financial discrepancies for transaction posting
- [ ] 3 consecutive parallel runs with zero discrepancies for interest calculation
- [ ] Statement generation produces functionally equivalent output (text + HTML)
- [ ] All COMP-3 arithmetic matches BigDecimal calculations to the penny
- [ ] Batch cycle completes within SLA (< 2 hours for full cycle)
- [ ] Reject handling produces identical reject records
- [ ] GDG backup/restore tested and verified
- [ ] Legacy batch JCL jobs decommissioned

---

## 7. Phase 5 -- Optional Modules (High Risk)

**Duration:** 8-12 weeks
**Risk Level:** High (middleware complexity)
**Bounded Contexts:** BC-8 (Authorization -- IMS/DB2/MQ)
**Strategy:** Strangler Fig

### 7.1 Scope

| Program | Current Function | Target | Strategy |
|---------|-----------------|--------|----------|
| COPAUA0C | MQ trigger -- process authorization requests | Kafka/RabbitMQ consumer + Authorization Service | Strangler Fig |
| COPAUS0C | Authorization summary view | REST endpoint `GET /api/authorizations` | Strangler Fig |
| COPAUS1C | Authorization detail view | REST endpoint `GET /api/authorizations/{id}` | Strangler Fig |
| COPAUS2C | Fraud marking (DB2) | REST endpoint `POST /api/authorizations/{id}/fraud` | Strangler Fig |
| CBPAUP0C | Batch purge of old records | Spring Batch scheduled purge job | Replatform |

### 7.2 Middleware Migration

| Legacy Component | Modern Equivalent | Migration Approach |
|-----------------|-------------------|-------------------|
| MQ queues (CDRA, CDRD) | Kafka topics / RabbitMQ queues | Deploy message broker; configure producer/consumer |
| IMS DB segments | JPA entities + PostgreSQL tables | Map IMS hierarchical segments to relational tables |
| DB2 fraud table | PostgreSQL table | Direct SQL DDL migration |
| MQ trigger monitor | Spring Cloud Stream / Spring Kafka listener | Event-driven service |

### 7.3 Cutover Steps

1. Deploy message broker (Kafka or RabbitMQ)
2. Map IMS DB segments to relational schema
3. Migrate IMS data and DB2 fraud table to PostgreSQL
4. Deploy Authorization Service with message consumer
5. Run parallel: Both MQ-triggered COBOL and Kafka-triggered Java process same messages
6. Compare authorization decisions for 2 weeks
7. Deploy authorization UI (summary, detail, fraud marking)
8. Cut over MQ producers to publish to Kafka instead
9. Decommission IMS DB, DB2 fraud table, and legacy MQ infrastructure

### 7.4 Rollback Strategy

- **Trigger:** Authorization processing delays > 5 seconds or incorrect authorization decisions
- **Action:** Re-enable MQ trigger monitor for legacy COPAUA0C; restore IMS DB as primary
- **Data:** IMS DB and DB2 remain active during dual-run; no data loss on rollback
- **RTO:** < 30 minutes

### 7.5 Exit Criteria

- [ ] Authorization requests processed via Kafka within SLA (< 2 seconds)
- [ ] Fraud marking persists correctly to PostgreSQL
- [ ] Batch purge job runs successfully on schedule
- [ ] Legacy IMS/DB2/MQ infrastructure decommissioned
- [ ] Zero authorization processing failures for 2 weeks

---

## 8. Phase 6 -- Decommission Legacy (Final)

**Duration:** 4-6 weeks
**Risk Level:** Low (cleanup)

### 8.1 Scope

| Activity | Description |
|----------|-------------|
| Remove CDC sync | Disable VSAM-to-RDBMS Change Data Capture |
| Archive VSAM files | Export final VSAM data to archival storage |
| Decommission CICS region | Shut down CardDemo CICS region |
| Decommission batch JCL | Remove JCL jobs from scheduler (CA7/Control-M) |
| Remove API Gateway legacy routes | Clean up any remaining legacy routing rules |
| Archive source code | Tag final state of COBOL source in version control |
| Update documentation | Mark migration as complete; update runbooks |

### 8.2 Pre-Decommission Checklist

- [ ] All bounded contexts running on modern services for > 30 days
- [ ] Zero fallback to legacy in the past 14 days
- [ ] All VSAM data archived with checksums
- [ ] Disaster recovery tested on modern stack
- [ ] Operations team trained on new platform
- [ ] Monitoring and alerting configured for all services
- [ ] Performance benchmarks met or exceeded

---

## 9. Timeline Summary

```
Week  0          6         12         18         24         36         48    54
  |---Phase 0---|
  |  Foundation |
                |---Phase 1----|
                |  Peripheral  |
                               |---Phase 2-------|
                               |  Read-Only      |
                                                  |------Phase 3---------|
                                                  |  Core Domain         |
                                                                         |------Phase 4------------|
                                                                         |  Financial Processing   |
                                                                                                   |---Phase 5----|
                                                                                                   |  Optional    |
                                                                                                                  |--Phase 6--|
                                                                                                                  |  Decommis |
```

| Phase | Duration | Cumulative | Risk |
|-------|----------|------------|------|
| Phase 0: Foundation | 4-6 weeks | Week 0-6 | Low |
| Phase 1: Peripheral Services | 4-6 weeks | Week 6-12 | Low |
| Phase 2: Read-Only Services | 6-8 weeks | Week 12-20 | Low-Medium |
| Phase 3: Core Domain | 10-14 weeks | Week 20-34 | Medium |
| Phase 4: Financial Processing | 12-16 weeks | Week 34-50 | High |
| Phase 5: Optional Modules | 8-12 weeks | Week 50-62 | High |
| Phase 6: Decommission | 4-6 weeks | Week 62-68 | Low |
| **Total** | **48-68 weeks** | **~12-17 months** | |

---

## 10. Key Dependencies

```
Phase 0 (Foundation)
    |
    v
Phase 1 (Identity, Reference Data) ----+
    |                                   |
    v                                   |
Phase 2 (Reporting, Menus, MQ)         |
    |                                   |
    v                                   v
Phase 3 (Card, Account, Payment) <------+
    |
    v
Phase 4 (Transactions, Batch, Statements)
    |
    v
Phase 5 (Authorization -- IMS/DB2/MQ)
    |
    v
Phase 6 (Decommission)
```

**Critical path:** Phase 0 -> Phase 1 -> Phase 3 -> Phase 4

Phase 2 can run partially in parallel with Phase 1 (menu rewrite can start after Identity Service is deployed). Phase 5 can start as soon as Phase 3 is complete (authorization module is independent of financial batch processing).

---

## 11. Go/No-Go Decision Framework

Each phase transition requires a formal Go/No-Go decision:

| Criterion | Threshold | Decision |
|-----------|-----------|----------|
| Functional parity | 100% of test cases pass | Must pass |
| Data consistency | Zero discrepancies between RDBMS and VSAM | Must pass |
| Performance | Response time within 120% of legacy SLA | Must pass |
| Error rate | < 0.1% for online, 0% for financial batch | Must pass |
| Rollback tested | Rollback procedure executed successfully in staging | Must pass |
| Operations readiness | Runbooks updated, team trained, monitoring active | Must pass |
| Business sign-off | Business stakeholder approval | Must pass |
