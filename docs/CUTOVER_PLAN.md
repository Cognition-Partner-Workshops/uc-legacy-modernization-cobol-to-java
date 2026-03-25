# CardDemo Cutover Plan

> **Purpose:** Define a phased migration sequence from lowest-risk to highest-risk, with entry/exit criteria, rollback procedures, and parallel-run validation gates
> **Principle:** Each phase delivers a self-contained, production-ready increment. No phase depends on future phases for correctness.

---

## 1. Migration Phasing Overview

```
Timeline (indicative)
──────────────────────────────────────────────────────────────────────────►

Phase 0        Phase 1        Phase 2         Phase 3         Phase 4
Foundation     Low-Risk       Medium-Risk     High-Risk       Decommission
(4 weeks)      (6 weeks)      (8 weeks)       (12 weeks)      (4 weeks)
                                                               
Database +     IAM +          Card Mgmt +     Online Txn +    Mainframe
Auth +         User CRUD +    Account Mgmt    Batch Posting + shutdown
Ref Data       Reporting +                    Interest Calc
               Export/Import
```

---

## 2. Phase 0 -- Foundation (Weeks 1-4)

### Objective
Stand up the target platform, migrate the database schema, seed reference data, and establish the integration bridge between mainframe and Java.

### Scope

| Work Item | Description |
|-----------|-------------|
| Database provisioning | Create PostgreSQL/Aurora instance with all 12 target tables |
| Schema migration | Flyway scripts for accounts, customers, cards, card_xref, transactions, users, category_balances, disclosure_groups, tran_types, tran_categories, daily_transactions, rejected_transactions |
| Data seeding | Initial data load from VSAM files via CBEXPORT → ETL → database INSERT |
| Reference data | Load disclosure_groups, tran_types, tran_categories as static lookup tables |
| API Gateway | Deploy Spring Cloud Gateway with routing stubs for future services |
| CI/CD pipeline | Build pipeline for Java services (compile, test, deploy) |
| Monitoring | Logging (ELK/CloudWatch), metrics (Micrometer/Prometheus), alerting |

### Entry Criteria
- [ ] Target cloud environment provisioned (VPC, subnets, security groups)
- [ ] Database instance available with network access from application tier
- [ ] Source VSAM data files available in ASCII format (from `app/data/ASCII/`)

### Exit Criteria
- [ ] All 12 tables created with correct column types mapping PIC clauses to SQL types
- [ ] Reference data loaded and queryable
- [ ] Sample data loaded; row counts match VSAM record counts
- [ ] API Gateway deployed and returning 200 on health endpoint
- [ ] CI/CD pipeline building and deploying a hello-world Spring Boot service

### Rollback
Phase 0 has no mainframe impact. Rollback = tear down cloud infrastructure.

---

## 3. Phase 1 -- Low-Risk Modules (Weeks 5-10)

### Objective
Migrate modules with clean extraction seams and no write contention: IAM, User CRUD, Reporting, and Data Export/Import.

### 3.1 IAM & User Administration

| Source | Target | Strategy |
|--------|--------|----------|
| COSGN00C (260 LOC) | `POST /auth/login` → JWT | Rewrite |
| COMEN01C (308 LOC) | Frontend routing (SPA) | Rewrite |
| COADM01C (288 LOC) | Frontend routing (SPA) | Rewrite |
| COUSR00C (695 LOC) | `GET /users` (paginated) | Rewrite |
| COUSR01C (299 LOC) | `POST /users` | Rewrite |
| COUSR02C (414 LOC) | `PUT /users/{id}` | Rewrite |
| COUSR03C (359 LOC) | `DELETE /users/{id}` | Rewrite |
| CSUSR01Y copybook | `User` JPA entity | Rewrite |
| USRSEC VSAM | `users` table | Schema migration |

**Implementation Steps:**
1. Create `iam-service` Spring Boot application
2. Implement `User` entity with BCrypt password hashing (replacing plaintext)
3. Implement Spring Security configuration with JWT token issuance
4. Implement UserController with CRUD endpoints
5. Migrate USRSEC data → `users` table (hash passwords during migration)
6. Deploy behind API Gateway

**Validation:**
- [ ] Login with ADMIN001 credentials returns valid JWT
- [ ] User CRUD operations match COBOL behavior (create, list, update, delete)
- [ ] Invalid credentials return 401
- [ ] Unauthorized access returns 403

### 3.2 Reporting & Statements

| Source | Target | Strategy |
|--------|--------|----------|
| CORPT00C (649 LOC) | `POST /reports/generate` | Rewrite |
| CBTRN03C (649 LOC) | Spring Batch report job | Rewrite |
| CBSTM03A/B (924 LOC) | Spring Batch + Thymeleaf template | Rewrite |

**Implementation Steps:**
1. Create `reporting-service` Spring Boot application
2. Implement transaction report as SQL query with GROUP BY rollup
3. Implement statement generation with Thymeleaf HTML template
4. Replace CICS internal reader submission (CORPT00C) with REST trigger
5. Schedule via Quartz (replacing JCL: TRANREPT, CREASTMT)

**Validation:**
- [ ] Transaction report output matches CBTRN03C output for same date range
- [ ] Statement HTML matches CBSTM03A output for same accounts
- [ ] Report totals (grand total, account subtotals) match to the cent

### 3.3 Data Export/Import

| Source | Target | Strategy |
|--------|--------|----------|
| CBEXPORT (582 LOC) | Spring Batch multi-entity export job | Rewrite |
| CBIMPORT (487 LOC) | Spring Batch multi-entity import job | Rewrite |

**Implementation Steps:**
1. Add export/import jobs to `data-migration-job` Spring Batch application
2. Replace multiplexed single-file format with per-entity CSV or JSON
3. Implement error handling with reject file (replacing CBIMPORT error output)

**Validation:**
- [ ] Export from database → re-import produces identical data
- [ ] Record counts match original VSAM file counts
- [ ] Error records written to reject file with correct error codes

### Phase 1 Entry Criteria
- [ ] Phase 0 complete: database provisioned, schema deployed, data seeded
- [ ] API Gateway operational
- [ ] USRSEC data exported and available for migration

### Phase 1 Exit Criteria
- [ ] IAM service handling all authentication (mainframe COSGN00C disabled)
- [ ] User CRUD functional with all validation rules
- [ ] Reports generate identical output to mainframe (verified by diff)
- [ ] Export/Import round-trip validated
- [ ] 1 week of parallel operation with no authentication failures

### Phase 1 Rollback
- Re-enable COSGN00C on CICS
- Restore USRSEC VSAM from backup
- Redirect API Gateway to return 503 for IAM endpoints
- Reporting can fall back to mainframe JCL submission

---

## 4. Phase 2 -- Medium-Risk Modules (Weeks 11-18)

### Objective
Migrate Card Management and Account Management, resolving the CUSTFILE cross-context read dependency and establishing the Account Balance API that Transaction Processing will depend on.

### 4.1 Card Management

| Source | Target | Strategy |
|--------|--------|----------|
| COCRDLIC (1,459 LOC) | `GET /cards?page=N&size=M` | Strangler |
| COCRDSLC (887 LOC) | `GET /cards/{cardNumber}` | Strangler |
| COCRDUPC (1,560 LOC) | `PUT /cards/{cardNumber}` | Strangler |
| CBACT02C (178 LOC) | JPA repository read | Strangler |

**Implementation Steps:**
1. Create `card-service` Spring Boot application
2. Implement `Card` and `CardXref` JPA entities
3. Implement paginated card search (replacing STARTBR/READNEXT/READPREV)
4. Implement card update with JPA `@Version` optimistic locking (replacing read-compare-rewrite)
5. Implement customer name lookup via `account-service` API (replacing direct CUSTFILE read)
6. Deploy behind API Gateway; route `/cards/**` to card-service
7. **Strangler bridge:** During transition, card-service writes to both database and VSAM via adapter

**Validation:**
- [ ] Card list pagination matches COCRDLIC browse results
- [ ] Card update with concurrent access correctly handles optimistic locking
- [ ] Card status change (active↔inactive) persists correctly
- [ ] Customer name displays correctly (cross-service call to account-service)

### 4.2 Account Management

| Source | Target | Strategy |
|--------|--------|----------|
| COACTVWC (941 LOC) | `GET /accounts/{id}` | Strangler |
| COACTUPC (4,236 LOC) | `PUT /accounts/{id}` (decomposed) | Refactor + Strangler |
| CBACT01C (430 LOC) | JPA repository read | Strangler |

**Implementation Steps:**
1. Create `account-service` Spring Boot application
2. Implement `Account` and `Customer` JPA entities
3. Implement account view as simple GET endpoint
4. **Decompose COACTUPC** (4,236 LOC):
   a. Extract validation rules from CSLKPCDY → `validation-rules.json` or database lookup
   b. Implement `AccountUpdateService` for account field updates
   c. Implement `CustomerUpdateService` for customer field updates
   d. Implement shared `FieldValidationService` (phone, SSN, state, zip, date)
5. **Expose Balance API** (`GET/PUT /accounts/{id}/balance`) for Transaction Processing to use in Phase 3
6. Deploy behind API Gateway
7. **Strangler bridge:** Write to both database and VSAM during transition

**Validation:**
- [ ] Account view returns all fields matching COACTVWC display
- [ ] Account update validates phone numbers against area code table (800+ codes)
- [ ] Account update validates state codes (50+ states)
- [ ] Account update validates state-zip combinations
- [ ] Credit limit changes persist correctly
- [ ] FICO score handling matches COBOL behavior
- [ ] Balance API returns correct current balance
- [ ] 2 weeks of parallel operation: Java writes shadowed against mainframe; diffs reconciled daily

### Phase 2 Entry Criteria
- [ ] Phase 1 complete: IAM service live, reporting functional
- [ ] Account and Card data synchronized from VSAM to database (via Phase 0 ETL)
- [ ] Validation lookup data (area codes, state codes, zip ranges) loaded into reference tables

### Phase 2 Exit Criteria
- [ ] Card CRUD functional; mainframe COCRDLIC/COCRDSLC/COCRDUPC disabled
- [ ] Account view/update functional; mainframe COACTVWC/COACTUPC disabled
- [ ] Balance API operational and tested under load
- [ ] All field validations match mainframe behavior (verified by test suite)
- [ ] 2 weeks of parallel operation with <0.1% discrepancy rate

### Phase 2 Rollback
- Re-enable CICS programs (COCRDLIC, COCRDSLC, COCRDUPC, COACTVWC, COACTUPC)
- Restore VSAM files from last backup
- Disable API Gateway routes for `/cards/**` and `/accounts/**`
- Balance API remains available (read-only) for Phase 3 preparation

---

## 5. Phase 3 -- High-Risk Modules (Weeks 19-30)

### Objective
Migrate the financial core: online transaction processing, batch posting, and interest calculation. This phase carries the highest risk because it involves real-time and batch financial writes.

### 5.1 Online Transaction Processing

| Source | Target | Strategy |
|--------|--------|----------|
| COTRN00C (699 LOC) | `GET /transactions?page=N` | Strangler |
| COTRN01C (330 LOC) | `GET /transactions/{id}` | Strangler |
| COTRN02C (783 LOC) | `POST /transactions` | Strangler |
| COBIL00C (572 LOC) | `POST /payments` | Strangler |
| CSUTLDTC (157 LOC) | `java.time` validation | Rewrite |

**Implementation Steps:**
1. Create `transaction-service` Spring Boot application
2. Implement `Transaction` JPA entity with `BigDecimal` amounts
3. Implement transaction list with paginated search
4. Implement transaction add with:
   - Card→Account cross-reference lookup (via `card-service` API)
   - Date validation (replacing CSUTLDTC/CEEDAYS with `java.time`)
   - Sequential transaction ID generation (UUID or database sequence)
   - Amount validation
5. Implement bill payment with:
   - Balance read via `account-service` Balance API
   - Transaction write (`@Transactional`)
   - Balance update via `account-service` Balance API
6. Deploy behind API Gateway

**Critical: Financial Accuracy Testing**
```
For every transaction type, verify:
  1. Transaction record matches COBOL output field-by-field
  2. Balance after payment = BigDecimal(oldBalance).subtract(BigDecimal(payment))
     with scale(2, RoundingMode.HALF_UP)
  3. Transaction ID generation is gap-free (or document gap policy)
```

### 5.2 Batch Transaction Posting

| Source | Target | Strategy |
|--------|--------|----------|
| CBTRN02C (731 LOC) | Spring Batch `PostingJob` | Refactor |
| CBTRN01C (494 LOC) | Spring Batch pre-validation step | Refactor |
| POSTTRAN JCL | Quartz-triggered Spring Batch job | Replace |

**Implementation Steps:**
1. Create `batch-posting-job` Spring Batch application
2. Implement chunk-oriented reader/processor/writer:
   - Reader: Read from `daily_transactions` table (replaces DALYTRAN VSAM read)
   - Processor: Validate card xref, check account status, apply business rules
   - Writer: Write to `transactions` table, update `accounts.balance`, update `category_balances`, write rejects
3. Replace multi-file VSAM writes with single database transaction per chunk
4. Implement reject handling (replaces DALYREJS VSAM write)

**Parallel Run Protocol:**
```
Week 1-2: Run Java batch in shadow mode (read-only, compare output)
Week 3-4: Run Java batch writing to shadow tables; reconcile against COBOL
Week 5-6: Run Java batch as primary; COBOL batch as verification
Week 7+:  Java batch only; COBOL decommissioned
```

### 5.3 Interest Calculation

| Source | Target | Strategy |
|--------|--------|----------|
| CBACT04C (652 LOC) | Spring Batch `InterestCalcJob` | Refactor |
| INTCALC JCL | Quartz-triggered Spring Batch job | Replace |

**Implementation Steps:**
1. Create `interest-calc-job` Spring Batch application
2. Implement tiered interest rate lookup (DISCGRP → `disclosure_groups` table)
3. Implement `BigDecimal` arithmetic with exact COBOL rounding behavior:
   ```java
   // COBOL: COMPUTE WS-INTEREST = WS-BALANCE * WS-RATE / 12 ROUNDED
   BigDecimal interest = balance.multiply(rate)
       .divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
   ```
4. Implement fee assessment logic
5. Write interest charge transactions and update account balances

**Critical: Arithmetic Parity Testing**
```
For 100+ test accounts with known balances and rates:
  1. Run COBOL CBACT04C → capture output transactions and updated balances
  2. Run Java InterestCalcJob on same input data
  3. Compare every output field to 2 decimal places
  4. ZERO tolerance for discrepancies (any mismatch = blocker)
```

### Phase 3 Entry Criteria
- [ ] Phase 2 complete: Account and Card services live; Balance API operational
- [ ] All VSAM data migrated to database and verified
- [ ] BigDecimal arithmetic test suite passing (100+ test cases)
- [ ] Parallel run infrastructure ready (shadow tables, reconciliation scripts)

### Phase 3 Exit Criteria
- [ ] Online transaction add/payment functional with correct balance updates
- [ ] Batch posting produces identical output to CBTRN02C (verified over 4+ cycles)
- [ ] Interest calculation matches CBACT04C to the cent (verified over 2+ cycles)
- [ ] All batch JCL jobs decommissioned (POSTTRAN, INTCALC, COMBTRAN, TRANBKP)
- [ ] 4 weeks of production operation with zero financial discrepancies
- [ ] Reconciliation reports archived for audit

### Phase 3 Rollback
**This is the most critical rollback scenario.**

| Severity | Trigger | Action |
|----------|---------|--------|
| Minor | Non-financial bug (UI, formatting) | Hotfix in Java; no rollback needed |
| Moderate | Single transaction discrepancy | Investigate root cause; apply corrective transaction; pause Java batch |
| Major | Systematic balance errors | Halt Java processing; re-enable COBOL batch; restore balances from last reconciliation checkpoint |
| Critical | Data corruption | Full stop; restore database from point-in-time backup; re-enable entire mainframe stack |

**Rollback preparation:**
- Database point-in-time recovery enabled (5-minute RPO)
- COBOL batch JCL retained on mainframe (not deleted) for 90 days post-cutover
- VSAM files maintained as read-only backups for 90 days
- Daily reconciliation checkpoints stored for 180 days

---

## 6. Phase 4 -- Decommission (Weeks 31-34)

### Objective
Shut down mainframe components, archive COBOL source, and finalize the migration.

### Steps

| Step | Action | Criteria |
|------|--------|----------|
| 1 | Disable CICS transactions (CC00, CM00, CA00, etc.) | All users on Java UI |
| 2 | Close VSAM files permanently | No batch jobs accessing VSAM |
| 3 | Archive COBOL source to Git (already done) | Source preserved |
| 4 | Archive VSAM data to cold storage (S3 Glacier / tape) | 7-year retention for audit |
| 5 | Decommission mainframe LPAR or region | Cost savings begin |
| 6 | Remove strangler bridges and VSAM adapters | Clean architecture |
| 7 | Update API Gateway to remove legacy routing rules | Simplified routing |
| 8 | Final documentation update | Runbooks, architecture diagrams |

### Entry Criteria
- [ ] Phase 3 complete: all financial processing on Java for 4+ weeks
- [ ] Zero rollback events in last 4 weeks
- [ ] All users migrated to web UI
- [ ] Audit/compliance sign-off obtained

### Exit Criteria
- [ ] Mainframe LPAR/region decommissioned (or COBOL programs deleted)
- [ ] VSAM data archived with documented retention policy
- [ ] All JCL jobs removed from scheduler
- [ ] Cost savings validated (mainframe MIPS reduction confirmed)

---

## 7. Parallel-Run Reconciliation Framework

### 7.1 Reconciliation Points

| Check | Frequency | Tolerance | Method |
|-------|-----------|-----------|--------|
| Account balances | Daily | $0.00 (exact) | Compare Java `accounts.balance` vs VSAM ACCTFILE `ACCT-CURR-BAL` |
| Transaction counts | Daily | 0 (exact) | Count records in Java `transactions` vs VSAM TRANSACT |
| Transaction amounts | Daily | $0.00 (exact) | Sum `TRAN-AMT` in both systems |
| Category balances | After batch | $0.00 (exact) | Compare Java `category_balances` vs VSAM TCATBALF |
| Interest charges | After batch | $0.00 (exact) | Compare interest transactions written by both systems |
| User count | Weekly | 0 (exact) | Compare Java `users` vs VSAM USRSEC |
| Card status | Weekly | 0 (exact) | Compare Java `cards.status` vs VSAM CARDFILE `CARD-ACTIVE-STATUS` |

### 7.2 Reconciliation Process

```
┌─────────────┐     ┌─────────────┐
│  Mainframe  │     │    Java     │
│  COBOL/VSAM │     │  Services   │
└──────┬──────┘     └──────┬──────┘
       │                   │
       ▼                   ▼
  ┌─────────┐         ┌─────────┐
  │ VSAM    │         │Database │
  │ Extract │         │ Export  │
  └────┬────┘         └────┬────┘
       │                   │
       └─────────┬─────────┘
                 ▼
          ┌──────────────┐
          │ Reconciliation│
          │    Engine     │
          │  (compares    │
          │   row-by-row) │
          └──────┬───────┘
                 │
        ┌────────┼────────┐
        ▼        ▼        ▼
   ┌────────┐ ┌──────┐ ┌──────┐
   │ MATCH  │ │DIFFER│ │MISSING│
   │ Report │ │Report│ │Report │
   └────────┘ └──────┘ └──────┘
```

### 7.3 Go/No-Go Decision Matrix

| Metric | Green (Go) | Yellow (Investigate) | Red (No-Go) |
|--------|:----------:|:-------------------:|:-----------:|
| Balance discrepancies | 0 | 1-5 (with root cause) | >5 or unexplained |
| Transaction count mismatch | 0 | 1-3 (timing-related) | >3 |
| Interest calc variance | $0.00 | ≤$0.01 (rounding) | >$0.01 |
| Authentication failures | 0% | <0.1% | ≥0.1% |
| Response time (P99) | <500ms | 500ms-2s | >2s |
| Error rate | <0.01% | 0.01%-0.1% | >0.1% |

---

## 8. Timeline Summary

| Phase | Weeks | Risk Level | Key Deliverable |
|-------|------:|:----------:|-----------------|
| **Phase 0: Foundation** | 1-4 | None | Database, schema, API Gateway, CI/CD |
| **Phase 1: Low-Risk** | 5-10 | Low | IAM, User CRUD, Reporting, Export/Import |
| **Phase 2: Medium-Risk** | 11-18 | Medium | Card Management, Account Management, Balance API |
| **Phase 3: High-Risk** | 19-30 | High | Online Transactions, Batch Posting, Interest Calc |
| **Phase 4: Decommission** | 31-34 | Low | Mainframe shutdown, archival |
| **TOTAL** | **34 weeks** | | **Full migration complete** |

### Critical Path
```
Database Schema → IAM Service → Account Service (Balance API) → Transaction Service → Batch Posting → Interest Calc
```
The critical path runs through the Balance API: Transaction Processing cannot go live until Account Management exposes a stable Balance API. This dependency must be prioritized in Phase 2.
