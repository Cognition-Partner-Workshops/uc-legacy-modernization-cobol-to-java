# CardDemo Cutover Plan -- Phased Migration Sequence

> **Generated**: 2026-03-25 | **Approach**: Lowest-risk to highest-risk, validated by coupling analysis and business impact

## Guiding Principles

1. **Migrate the least-coupled modules first** -- reduce blast radius and build confidence
2. **Establish the data layer early** -- schema from copybooks, seeded from VSAM exports
3. **Parallel-run everything** -- COBOL and Java run side-by-side with output comparison before cutover
4. **Read before write** -- migrate read-only paths first, then writes, for each functional area
5. **Batch last** -- the nightly pipeline has the highest coupling and financial precision requirements
6. **Rollback ready** -- every phase has a documented rollback procedure

---

## Pre-Migration (Phase 0): Foundation & Infrastructure

**Duration:** Weeks 1-3
**Risk Level:** Very Low
**Strategy:** Infrastructure setup, no functional changes

### Activities

| # | Activity | Owner | Duration | Dependencies |
|---|---|---|---|---|
| 0.1 | Provision target infrastructure (AWS ECS/EKS, RDS PostgreSQL/Aurora, VPC) | Platform | Week 1 | None |
| 0.2 | Set up CI/CD pipeline (GitHub Actions → ECR → ECS) | Platform | Week 1 | 0.1 |
| 0.3 | Create database schema from copybook layouts (CVACT01Y → accounts, CVACT02Y → cards, CVCUS01Y → customers, CVTRA05Y → transactions, etc.) | Data | Week 1-2 | 0.1 |
| 0.4 | Build data migration pipeline: CBEXPORT → ASCII → ETL → PostgreSQL | Data | Week 2-3 | 0.3 |
| 0.5 | Load reference data (TRANTYPE, TRANCATG, DISCGRP) into target database | Data | Week 2 | 0.3 |
| 0.6 | Seed target database with full VSAM data export | Data | Week 3 | 0.4 |
| 0.7 | Set up data synchronization (VSAM → PostgreSQL) for dual-run period | Data | Week 3 | 0.4 |
| 0.8 | Deploy API Gateway (Spring Cloud Gateway) with routing rules | Platform | Week 2 | 0.1 |
| 0.9 | Set up monitoring and alerting (CloudWatch/Datadog) | Platform | Week 2 | 0.1 |
| 0.10 | Establish parallel-run comparison framework (capture COBOL output, compare with Java output) | QA | Week 2-3 | None |

### Entry Criteria
- Target cloud environment provisioned
- Network connectivity between mainframe and cloud established

### Exit Criteria
- Database schema created and validated against copybook definitions
- Full data load completed with record count reconciliation
- Data sync pipeline running with < 5 minute lag
- CI/CD pipeline deploying to staging environment
- Monitoring dashboards active

### Rollback
- No production changes; rollback = tear down cloud infrastructure

---

## Phase 1: Identity & Access Management

**Duration:** Weeks 4-6
**Risk Level:** Low
**Strategy:** Rewrite
**Programs:** COSGN00C, COUSR00C-03C (5 programs, 2,027 lines)
**Bounded Context:** BC-1 (Identity & Access)

### Rationale for Going First
- **Zero coupling** to other bounded contexts (see DOMAIN_DECOMPOSITION.md coupling matrix)
- Authentication gateway -- must exist before any other service can be accessed
- Smallest functional area with clear boundaries
- Security improvement (plaintext → BCrypt) provides immediate value

### Activities

| # | Activity | Duration | Dependencies |
|---|---|---|---|
| 1.1 | Develop `identity-service`: Spring Security + Spring Data JPA | Week 4-5 | Phase 0 |
| 1.2 | Implement user CRUD endpoints (GET/POST/PUT/DELETE /users) | Week 4-5 | 0.3 |
| 1.3 | Implement authentication endpoint (POST /auth/login) with JWT | Week 4-5 | 0.3 |
| 1.4 | Migrate user data: USRSEC VSAM → users table (hash passwords during migration) | Week 5 | 1.1, 0.6 |
| 1.5 | Integration test: verify login for all existing users (ADMIN001, USER0001, etc.) | Week 5 | 1.4 |
| 1.6 | Deploy to staging, parallel-run against COBOL signon | Week 5-6 | 1.5 |
| 1.7 | Route authentication traffic to new service via API Gateway | Week 6 | 1.6 |

### Validation Checklist
- [ ] All existing users can authenticate with original passwords
- [ ] Admin users receive admin role; regular users receive user role
- [ ] JWT tokens are issued and validated correctly
- [ ] User CRUD operations match COBOL behavior (add, update, delete, list)
- [ ] Failed login attempts return appropriate error messages
- [ ] Session timeout behavior matches CICS session timeout

### Rollback Procedure
1. Route authentication traffic back to COBOL COSGN00C via API Gateway
2. No data rollback needed -- USRSEC VSAM remains unchanged during dual-run

---

## Phase 2: Reference Data & Navigation

**Duration:** Weeks 6-8
**Risk Level:** Low
**Strategy:** Rewrite (navigation), Shared DB (reference data)
**Programs:** COMEN01C, COADM01C (2 programs, 596 lines)
**Bounded Contexts:** BC-7 (Reference Data), Navigation

### Rationale
- Reference data has **only inbound weak coupling** -- all other contexts consume it read-only
- Navigation is the application shell -- must exist for users to reach other functions
- Menu definitions in COMEN02Y/COADM02Y translate directly to route configuration

### Activities

| # | Activity | Duration | Dependencies |
|---|---|---|---|
| 2.1 | Verify reference data loaded in Phase 0 (transaction types, categories, disclosure groups) | Week 6 | 0.5 |
| 2.2 | Create reference data REST endpoints (GET /reference/transaction-types, etc.) | Week 6 | 2.1 |
| 2.3 | Build frontend application shell (React/Angular) with role-based navigation | Week 6-7 | 1.7 |
| 2.4 | Implement main menu (from COMEN02Y program list) and admin menu (from COADM02Y) | Week 7 | 2.3 |
| 2.5 | Route menu selections to legacy CICS programs (via API Gateway) for functions not yet migrated | Week 7-8 | 2.4 |
| 2.6 | Deploy frontend to staging, test all navigation paths | Week 8 | 2.5 |

### Validation Checklist
- [ ] All menu options from COMEN02Y accessible to regular users
- [ ] All admin menu options from COADM02Y accessible to admin users only
- [ ] Non-migrated functions correctly route to legacy system
- [ ] Reference data endpoints return correct type/category/disclosure data
- [ ] Menu renders correctly on modern browsers

### Rollback Procedure
1. Route all traffic back to CICS BMS screens
2. No data changes to roll back

---

## Phase 3: Credit Card Management (Read Path)

**Duration:** Weeks 8-11
**Risk Level:** Low-Medium
**Strategy:** Strangler Fig (reads first)
**Programs:** COCRDLIC (1,459 lines), COCRDSLC (887 lines), CBACT02C (178 lines), CBACT03C (178 lines)
**Bounded Context:** BC-3 (Credit Card -- read operations)

### Rationale
- Card list and view are **read-only** -- no risk of data corruption
- STARTBR/READNEXT pagination pattern maps cleanly to database pagination
- Establishes the card-service and CARDXREF ownership before Account migration
- Validates data sync pipeline under real read traffic

### Activities

| # | Activity | Duration | Dependencies |
|---|---|---|---|
| 3.1 | Develop `card-service`: Spring Data JPA with card and cross-reference entities | Week 8-9 | Phase 0 |
| 3.2 | Implement card list endpoint (GET /cards?account_id=X with pagination) | Week 9 | 3.1 |
| 3.3 | Implement card detail endpoint (GET /cards/{number}) | Week 9 | 3.1 |
| 3.4 | Implement PCI-compliant field masking (card number, CVV in API responses) | Week 9-10 | 3.2 |
| 3.5 | Parallel-run: compare Java API output with COBOL screen data for 100+ cards | Week 10 | 3.4 |
| 3.6 | Route card list/view traffic to new service | Week 10-11 | 3.5 |

### Validation Checklist
- [ ] Card list pagination matches COBOL browse (same cards, same order)
- [ ] Card detail displays all fields from CVACT02Y
- [ ] Cross-reference resolution (card → account → customer) matches COBOL
- [ ] Card numbers are masked in API responses (PCI compliance)
- [ ] Performance: card list responds in < 200ms for typical account (< 10 cards)

### Rollback Procedure
1. Route card list/view traffic back to COCRDLIC/COCRDSLC via API Gateway
2. No data changes to roll back

---

## Phase 4: Transaction Management (Read Path) & Reporting

**Duration:** Weeks 11-14
**Risk Level:** Low-Medium
**Strategy:** Strangler Fig (reads) + Rewrite (reporting)
**Programs:** COTRN00C (699 lines), COTRN01C (330 lines), CORPT00C (649 lines), CBTRN03C (649 lines)
**Bounded Contexts:** BC-4 (Transaction -- read path), BC-8 (Reporting)

### Rationale
- Transaction list/view are **read-only** like card list/view
- Reporting is a pure **read-only consumer** of other contexts' data
- Can be migrated together as they share the TRANSACT data source
- Low coupling to other contexts

### Activities

| # | Activity | Duration | Dependencies |
|---|---|---|---|
| 4.1 | Develop `transaction-service`: transaction list and detail endpoints | Week 11-12 | Phase 0 |
| 4.2 | Implement transaction list with browse pagination (GET /transactions?account_id=X) | Week 11-12 | 4.1 |
| 4.3 | Implement transaction detail endpoint (GET /transactions/{id}) | Week 12 | 4.1 |
| 4.4 | Develop `reporting-service`: Spring Batch + JasperReports | Week 12-13 | Phase 0 |
| 4.5 | Implement transaction detail report (POST /reports/transaction-detail) | Week 12-13 | 4.4 |
| 4.6 | Implement report request UI (replacing CORPT00C BMS screen) | Week 13 | 4.5 |
| 4.7 | Parallel-run: compare Java report output with COBOL TRANREPT output | Week 13-14 | 4.5 |
| 4.8 | Route transaction list/view and report requests to new services | Week 14 | 4.7 |

### Validation Checklist
- [ ] Transaction list matches COBOL browse (same transactions, same order)
- [ ] Transaction detail shows all fields from CVTRA05Y
- [ ] Transaction report matches COBOL output (amounts, totals, formatting)
- [ ] Date range filtering works correctly
- [ ] Report subtotals (per-account, per-type, grand total) match COBOL

### Rollback Procedure
1. Route transaction and report traffic back to COBOL programs
2. No data changes to roll back

---

## Phase 5: Credit Card Management (Write Path)

**Duration:** Weeks 14-17
**Risk Level:** Medium
**Strategy:** Strangler Fig (writes)
**Programs:** COCRDUPC (1,560 lines)
**Bounded Context:** BC-3 (Credit Card -- write operations)

### Rationale
- Read path already validated in Phase 3 -- write path builds on stable foundation
- **First write operation** to be migrated -- establishes patterns for subsequent phases
- PCI compliance requires careful handling of card data writes

### Activities

| # | Activity | Duration | Dependencies |
|---|---|---|---|
| 5.1 | Implement card update endpoint (PUT /cards/{number}) in card-service | Week 14-15 | Phase 3 |
| 5.2 | Implement card field validation (CVV, expiration, embossed name, status) | Week 15 | 5.1 |
| 5.3 | Implement PCI-compliant field encryption at rest | Week 15-16 | 5.1 |
| 5.4 | Dual-write: card updates write to both PostgreSQL and VSAM during transition | Week 16 | 5.2 |
| 5.5 | Parallel-run: update 50+ cards via Java, verify VSAM matches | Week 16-17 | 5.4 |
| 5.6 | Cut over card writes to Java-only (stop VSAM sync for card data) | Week 17 | 5.5 |

### Validation Checklist
- [ ] Card update validation matches COBOL rules exactly (field-by-field)
- [ ] Card status changes (active/inactive) propagate correctly
- [ ] CVV and card number encrypted at rest in database
- [ ] Dual-write produces identical records in both PostgreSQL and VSAM
- [ ] Error handling matches COBOL error messages

### Rollback Procedure
1. Route card update traffic back to COCRDUPC
2. Restore VSAM CARDDATA from most recent backup if dual-write caused inconsistency
3. Re-enable VSAM → PostgreSQL sync

---

## Phase 6: Transaction Add & Bill Payment

**Duration:** Weeks 17-21
**Risk Level:** Medium-High
**Strategy:** Strangler Fig
**Programs:** COTRN02C (783 lines), COBIL00C (572 lines)
**Bounded Contexts:** BC-4 (Transaction -- write path), BC-5 (Bill Payment)

### Rationale
- **First financial write operations** -- transactions and payments directly affect account balances
- Both create transaction records in TRANSACT
- Bill payment also updates account balance (cross-context write → saga pattern)

### Activities

| # | Activity | Duration | Dependencies |
|---|---|---|---|
| 6.1 | Implement transaction add endpoint (POST /transactions) in transaction-service | Week 17-18 | Phase 4 |
| 6.2 | Implement transaction ID generation (database sequence replacing VSAM key) | Week 17 | 6.1 |
| 6.3 | Implement date validation (java.time replacing CSUTLDTC/CEEDAYS) | Week 18 | 6.1 |
| 6.4 | Develop `payment-service`: POST /payments endpoint | Week 18-19 | Phase 0 |
| 6.5 | Implement payment saga: reserve balance → create transaction → confirm | Week 19 | 6.4 |
| 6.6 | Implement idempotency keys for payment deduplication | Week 19-20 | 6.5 |
| 6.7 | Dual-write: transactions and payments write to both PostgreSQL and VSAM | Week 20 | 6.3, 6.6 |
| 6.8 | Parallel-run: process 200+ transactions and 50+ payments, compare results | Week 20-21 | 6.7 |
| 6.9 | Cut over transaction writes and payments to Java-only | Week 21 | 6.8 |

### Validation Checklist
- [ ] Transaction IDs are unique and sequential (no gaps in normal operation)
- [ ] Transaction amounts stored with correct decimal precision (COMP-3 → BigDecimal)
- [ ] Date validation matches COBOL behavior (valid/invalid date detection)
- [ ] Bill payment reduces account balance by exact payment amount
- [ ] Partial and full-balance payments work correctly
- [ ] Payment creates corresponding transaction record
- [ ] Idempotency: duplicate payment requests produce single payment
- [ ] Concurrent payment requests for same account are serialized correctly

### Rollback Procedure
1. Route transaction add and payment traffic back to COBOL programs
2. Reconcile any dual-write discrepancies using VSAM as source of truth
3. Re-enable VSAM → PostgreSQL sync for transaction and account data

---

## Phase 7: Account Management

**Duration:** Weeks 21-26
**Risk Level:** High
**Strategy:** Refactor
**Programs:** COACTVWC (941 lines), COACTUPC (4,236 lines), CBACT01C (430 lines)
**Bounded Context:** BC-2 (Account Management)

### Rationale
- **COACTUPC is the largest and most complex program** (4,236 lines, 17 CICS commands)
- Must be decomposed into multiple services/controllers during refactoring
- Depends on Card context (CARDXREF) which is already migrated in Phases 3/5
- Customer data tightly coupled -- migrated together

### Activities

| # | Activity | Duration | Dependencies |
|---|---|---|---|
| 7.1 | Decompose COACTUPC into AccountController + AccountService + AccountValidator | Week 21-23 | Phases 3, 5 |
| 7.2 | Implement account view endpoint (GET /accounts/{id}) from COACTVWC | Week 21-22 | 7.1 |
| 7.3 | Implement account update endpoint (PUT /accounts/{id}) with field-level validation | Week 22-24 | 7.1 |
| 7.4 | Implement customer endpoints (GET /accounts/{id}/customer) | Week 22-23 | 7.1 |
| 7.5 | Implement optimistic locking (replacing CICS VSAM record locking) | Week 23-24 | 7.3 |
| 7.6 | Extract validation rules from COACTUPC into Bean Validation annotations | Week 23-24 | 7.3 |
| 7.7 | Dual-write: account updates write to both PostgreSQL and VSAM | Week 24-25 | 7.6 |
| 7.8 | Parallel-run: update 100+ accounts, compare field-by-field | Week 25 | 7.7 |
| 7.9 | Cut over account management to Java-only | Week 26 | 7.8 |

### Validation Checklist
- [ ] All COACTUPC validation rules reproduced in Java (field-by-field comparison)
- [ ] Account balance updates preserve exact decimal precision
- [ ] Credit limit enforcement matches COBOL behavior
- [ ] Optimistic locking prevents concurrent update conflicts
- [ ] Customer data display matches COBOL screen output
- [ ] Account status transitions (active/closed/suspended) work correctly
- [ ] Account cycle dates calculated correctly

### Rollback Procedure
1. Route account traffic back to COACTVWC/COACTUPC
2. Full account data reconciliation (PostgreSQL vs. VSAM)
3. Re-enable VSAM → PostgreSQL sync for account and customer data

---

## Phase 8: Batch Processing Pipeline

**Duration:** Weeks 26-34
**Risk Level:** Very High
**Strategy:** Replatform (initially) → Refactor to Spring Batch
**Programs:** CBTRN01C, CBTRN02C, CBACT04C, CBSTM03A/B, CBTRN03C (6 programs, 3,680 lines)
**Bounded Context:** BC-6 (Batch Processing)

### Rationale
- **Highest coupling** in the entire application (strong coupling to Account, Transaction, Card)
- **Financial precision critical** -- interest calculations, balance updates, statement generation
- **Strict sequencing** -- 12-step nightly cycle with dependencies
- Migrate last when all other contexts are stable and well-tested

### Sub-Phase 8A: Replatform Batch on AWS M2 (Weeks 26-28)

| # | Activity | Duration | Dependencies |
|---|---|---|---|
| 8A.1 | Configure AWS Mainframe Modernization (M2) managed runtime | Week 26 | None |
| 8A.2 | Deploy batch COBOL programs to M2 runtime | Week 26-27 | 8A.1 |
| 8A.3 | Reconfigure JCL to read/write from new database instead of VSAM | Week 27 | 8A.2, Phase 7 |
| 8A.4 | Run full nightly batch cycle on M2, compare output with mainframe | Week 27-28 | 8A.3 |
| 8A.5 | Cut over nightly batch to M2 runtime | Week 28 | 8A.4 |

### Sub-Phase 8B: Refactor to Spring Batch (Weeks 28-34)

| # | Activity | Duration | Dependencies |
|---|---|---|---|
| 8B.1 | Implement CBTRN02C as Spring Batch job: DailyTransactionPostingJob | Week 28-30 | 8A.5 |
| 8B.2 | Implement CBACT04C as Spring Batch job: InterestCalculationJob | Week 29-31 | 8A.5 |
| 8B.3 | Implement CBSTM03A/B as Spring Batch job: StatementGenerationJob | Week 30-32 | 8A.5 |
| 8B.4 | Implement CBTRN03C as Spring Batch job: TransactionReportJob | Week 31-32 | 8A.5 |
| 8B.5 | Implement batch orchestration (Step Functions or Spring Batch flow) | Week 32-33 | 8B.1-8B.4 |
| 8B.6 | Parallel-run: run both M2 and Spring Batch nightly, compare all outputs | Week 33-34 | 8B.5 |
| 8B.7 | Cut over to Spring Batch | Week 34 | 8B.6 |

### Validation Checklist
- [ ] Transaction posting produces identical account balances (COMP-3 → BigDecimal precision)
- [ ] Reject handling: same transactions rejected for same reasons
- [ ] Interest calculation matches to the penny (disclosure group rate lookup, category balance)
- [ ] Statement output matches COBOL output (amounts, formatting, card grouping)
- [ ] Transaction report totals (per-account, per-type, grand) match exactly
- [ ] Batch cycle completes within the same time window as mainframe batch
- [ ] CLOSEFIL/OPENFIL behavior handled by database transaction isolation

### Rollback Procedure
- **8A rollback:** Revert batch execution to mainframe
- **8B rollback:** Revert batch execution to M2 replatformed COBOL
- Full data reconciliation required after any batch rollback

---

## Phase 9: Optional Modules

**Duration:** Weeks 34-40
**Risk Level:** Medium
**Strategy:** Rewrite (IMS/MQ modules), Refactor (DB2 module)

### 9A: Authorization Module -- IMS/DB2/MQ (Weeks 34-37)
**Programs:** COPAUA0C, COPAUS0C, COPAUS1C, COPAUS2C, CBPAUP0C

| # | Activity | Duration |
|---|---|---|
| 9A.1 | Rewrite MQ trigger processing (COPAUA0C) as SQS/Kafka consumer | Week 34-35 |
| 9A.2 | Rewrite authorization summary/detail (COPAUS0C/1C) as REST endpoints | Week 35-36 |
| 9A.3 | Rewrite fraud marking (COPAUS2C) as database service | Week 36 |
| 9A.4 | Rewrite batch purge (CBPAUP0C) as scheduled job | Week 36-37 |
| 9A.5 | Integration test with message queue and database | Week 37 |

### 9B: Transaction Type DB2 Module (Weeks 37-39)
**Programs:** COTRTUPC, COTRTLIC, COBTUPDT

| # | Activity | Duration |
|---|---|---|
| 9B.1 | Refactor DB2 embedded SQL to JPA repositories | Week 37-38 |
| 9B.2 | Implement transaction type CRUD and list endpoints | Week 38 |
| 9B.3 | Implement batch update job (COBTUPDT) | Week 38-39 |
| 9B.4 | Verify cursor-based list pagination matches DB2 cursor behavior | Week 39 |

### 9C: VSAM-MQ Module (Weeks 39-40)
**Programs:** CODATE01, COACCT01

| # | Activity | Duration |
|---|---|---|
| 9C.1 | Rewrite system date service (CODATE01) as REST endpoint | Week 39 |
| 9C.2 | Rewrite account inquiry (COACCT01) as REST endpoint | Week 39-40 |
| 9C.3 | Replace MQ request/response with REST API calls | Week 40 |

---

## Phase 10: Decommission Legacy

**Duration:** Weeks 40-44
**Risk Level:** Low (if all previous phases validated)

### Activities

| # | Activity | Duration | Dependencies |
|---|---|---|---|
| 10.1 | Stop VSAM → PostgreSQL data sync | Week 40 | All phases |
| 10.2 | Run final data reconciliation (VSAM vs. PostgreSQL) | Week 40-41 | 10.1 |
| 10.3 | Redirect all remaining mainframe traffic to Java services | Week 41 | 10.2 |
| 10.4 | Monitor Java-only operation for 2 weeks | Week 41-42 | 10.3 |
| 10.5 | Archive VSAM files and JCL jobs | Week 43 | 10.4 |
| 10.6 | Decommission mainframe CICS region | Week 43-44 | 10.5 |
| 10.7 | Decommission AWS M2 runtime (if used in Phase 8A) | Week 44 | 10.6 |

### Exit Criteria
- All traffic routed to Java services for 14+ days with no incidents
- No data discrepancies between final VSAM snapshot and PostgreSQL
- Mainframe resources released

---

## Timeline Summary

```
Week:  1   4   6   8   11  14  17  21  26  28  34  40  44
       │   │   │   │   │   │   │   │   │   │   │   │   │
Ph 0:  ████                                                 Foundation
Ph 1:      ████                                             Identity & Access
Ph 2:          ████                                         Reference Data & Nav
Ph 3:              ████                                     Card Reads
Ph 4:                  ████                                 Txn Reads & Reports
Ph 5:                      ████                             Card Writes
Ph 6:                          █████                        Txn Writes & Payment
Ph 7:                               ██████                  Account Mgmt
Ph 8A:                                     ███              Batch Replatform
Ph 8B:                                     ███████          Batch → Spring Batch
Ph 9:                                             ███████   Optional Modules
Ph 10:                                                  ████ Decommission
       │   │   │   │   │   │   │   │   │   │   │   │   │
       Low ───────────────────────────────────────── High
                        Risk Gradient →
```

### Phase Risk Summary

| Phase | Duration | Risk | Programs | Strategy | Key Milestone |
|---|---|---|---|---|---|
| 0 | Weeks 1-3 | Very Low | 0 | Infrastructure | Database schema + data load |
| 1 | Weeks 4-6 | Low | 5 | Rewrite | JWT authentication live |
| 2 | Weeks 6-8 | Low | 2 | Rewrite | Frontend shell + reference data |
| 3 | Weeks 8-11 | Low-Medium | 4 | Strangler | Card reads via REST API |
| 4 | Weeks 11-14 | Low-Medium | 4 | Strangler + Rewrite | Transaction reads + reports |
| 5 | Weeks 14-17 | Medium | 1 | Strangler | First write cutover (cards) |
| 6 | Weeks 17-21 | Medium-High | 2 | Strangler | Financial writes (transactions, payments) |
| 7 | Weeks 21-26 | High | 3 | Refactor | Largest program decomposed |
| 8 | Weeks 26-34 | Very High | 6 | Replatform → Refactor | Nightly batch on Spring Batch |
| 9 | Weeks 34-40 | Medium | 10 | Rewrite + Refactor | Optional modules migrated |
| 10 | Weeks 40-44 | Low | 0 | Decommission | Mainframe decommissioned |
