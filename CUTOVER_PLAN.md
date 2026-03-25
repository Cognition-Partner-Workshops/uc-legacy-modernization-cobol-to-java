# CardDemo Cutover Plan

## Overview

This document defines a phased migration sequence for the CardDemo mainframe application, ordered from **lowest-risk to highest-risk**. Each phase includes entry criteria, specific migration steps, validation checkpoints, rollback procedures, and exit criteria. The overall approach uses a **Strangler Fig envelope** -- a new web frontend progressively routes traffic to Java microservices while the mainframe continues operating until each functional area is fully validated.

**Total estimated timeline:** 5 phases over 18-24 months

---

## Phase 0: Foundation (Weeks 1-6) -- Risk: MINIMAL

### Objective
Stand up the target platform infrastructure, CI/CD pipelines, and data migration tooling before any functional migration begins.

### Entry Criteria
- Modernization blueprint approved by stakeholders
- Target cloud environment provisioned
- Development team onboarded with Java/Spring Boot skills

### Steps

| # | Task | Owner | Duration |
|---|------|-------|----------|
| 0.1 | Provision target infrastructure (Kubernetes cluster, PostgreSQL instances, message broker) | Platform | Week 1-2 |
| 0.2 | Set up CI/CD pipelines (build, test, deploy for Spring Boot services) | DevOps | Week 1-2 |
| 0.3 | Create shared libraries: EBCDIC-to-ASCII converter, COBOL-to-Java data type mapper (PIC S9(n)V99 to BigDecimal), date utilities | Dev | Week 2-4 |
| 0.4 | Build VSAM-to-RDBMS data migration toolkit using `app/data/ASCII/` sample data | Dev | Week 2-4 |
| 0.5 | Design and create target database schemas from copybook analysis (CVACT01Y, CVACT02Y, CVCUS01Y, CVACT03Y, CVTRA05Y, CSUSR01Y, CVTRA01Y, CVTRA02Y, CVTRA06Y) | Dev | Week 3-4 |
| 0.6 | Set up API gateway (Spring Cloud Gateway) as the Strangler facade entry point | Dev | Week 4-5 |
| 0.7 | Build observability stack (logging, metrics, distributed tracing) | DevOps | Week 4-5 |
| 0.8 | Create automated regression test suite from BMS screen flows and batch job outputs | QA | Week 3-6 |
| 0.9 | Establish dual-write infrastructure for transition period (CDC connectors) | Dev | Week 5-6 |

### Database Schema Mapping (from Copybooks)

```
USRSEC   (CSUSR01Y)  --> users (user_id VARCHAR(8) PK, first_name, last_name, password_hash, user_type)
ACCTDAT  (CVACT01Y)  --> accounts (acct_id BIGINT PK, active_status, curr_bal DECIMAL(12,2), credit_limit, ...)
CARDDAT  (CVACT02Y)  --> cards (card_num VARCHAR(16) PK, acct_id BIGINT FK, cvv, embossed_name, ...)
CUSTDAT  (CVCUS01Y)  --> customers (cust_id BIGINT PK, first_name, last_name, ssn, fico_score, ...)
CCXREF   (CVACT03Y)  --> card_xref (card_num VARCHAR(16) PK, cust_id BIGINT FK, acct_id BIGINT FK)
TRANSACT (CVTRA05Y)  --> transactions (tran_id VARCHAR(16) PK, type_cd, cat_cd, amount DECIMAL(11,2), ...)
TCATBALF (CVTRA01Y)  --> tran_category_balances (acct_id, type_cd, cat_cd, balance DECIMAL(11,2))
DISCGRP  (CVTRA02Y)  --> disclosure_groups (group_id, tran_type_cd, tran_cat_cd, int_rate DECIMAL(6,2))
```

### Validation Checkpoint
- [ ] All database schemas created and verified against copybook field layouts
- [ ] Sample data from `app/data/ASCII/` successfully loaded into target RDBMS
- [ ] CI/CD pipeline deploys a hello-world Spring Boot service end-to-end
- [ ] API gateway routes test traffic correctly

### Rollback
No production impact -- all work is on the new platform. Rollback is simply not proceeding.

### Exit Criteria
- Target platform operational with all infrastructure components
- Data migration toolkit validated against sample data
- Shared libraries unit-tested with >90% coverage

---

## Phase 1: Identity & Presentation Tier (Weeks 7-14) -- Risk: LOW

### Objective
Replace the authentication system and 3270 terminal UI with a modern web frontend and JWT-based security. This establishes the Strangler facade.

### Entry Criteria
- Phase 0 complete
- API gateway operational
- Database schemas for USRSEC created

### Scope

| Component | Current | Target | Strategy |
|-----------|---------|--------|----------|
| Authentication | COSGN00C + USRSEC VSAM | Spring Security + JWT + BCrypt | Rewrite |
| User Management | COUSR00C-03C (admin CRUD) | Spring Boot REST + JPA | Rewrite |
| Main Menu | COMEN01C + BMS maps | React SPA with routing | Rewrite |
| Admin Menu | COADM01C + BMS maps | React SPA with admin routes | Rewrite |
| Session State | COMMAREA (COCOM01Y) | JWT claims + URL params | Rewrite |

### Steps

| # | Task | Duration |
|---|------|----------|
| 1.1 | Implement Identity Service: Spring Security + JWT token issuance, BCrypt password hashing | Week 7-8 |
| 1.2 | Migrate USRSEC data to `users` table (hash all existing plaintext passwords) | Week 8 |
| 1.3 | Implement User Management REST API (CRUD replacing COUSR00C-03C) | Week 8-9 |
| 1.4 | Build React SPA shell: login page, main menu, admin menu, routing | Week 9-11 |
| 1.5 | Map BMS screen field definitions to React form components (17 maps) | Week 10-12 |
| 1.6 | Configure API gateway to route: new web UI -> Identity Service for auth, mainframe proxy for all other functions | Week 12 |
| 1.7 | Implement COMMAREA-to-JWT translation layer for hybrid operation | Week 12-13 |
| 1.8 | Integration testing: login via web UI, navigate menu, verify PF-key equivalents work | Week 13-14 |

### Traffic Routing During Phase 1

```
User Browser --> [API Gateway]
                    |
                    +--> /auth/*     --> Identity Service (NEW)
                    +--> /admin/*    --> Identity Service (NEW)
                    +--> /accounts/* --> Mainframe Proxy (LEGACY)
                    +--> /cards/*    --> Mainframe Proxy (LEGACY)
                    +--> /trans/*    --> Mainframe Proxy (LEGACY)
                    +--> /reports/*  --> Mainframe Proxy (LEGACY)
```

### Validation Checkpoint
- [ ] Users can log in via web UI and receive JWT token
- [ ] Admin users can perform all user CRUD operations
- [ ] Menu navigation matches the 11 regular + 6 admin options from COMEN02Y/COADM02Y
- [ ] JWT token contains user_id and user_type claims matching COMMAREA contract
- [ ] Mainframe auth (COSGN00C) still works in parallel for non-migrated functions
- [ ] Load test: 100 concurrent logins with <200ms p95 latency

### Rollback Procedure
1. Route all traffic back to mainframe COSGN00C via API gateway config change
2. No data loss -- USRSEC file remains intact as source of truth until Phase 1 exit
3. Estimated rollback time: <5 minutes (gateway config change)

### Exit Criteria
- All authentication flows pass through Identity Service
- User management fully operational via REST API
- Web UI shell deployed and accessible
- Mainframe USRSEC writes stopped; Identity Service is the system of record for users

---

## Phase 2: Card Management (Weeks 15-22) -- Risk: LOW-MEDIUM

### Objective
Migrate the Card Management bounded context, establishing the first data-owning service outside the mainframe.

### Entry Criteria
- Phase 1 complete (web UI + auth operational)
- Card Management database schema created and validated
- CCXREF lookup API contract agreed upon by all consuming teams

### Scope

| Component | Current | Target | Strategy |
|-----------|---------|--------|----------|
| Card List | COCRDLIC (STARTBR/READNEXT/ENDBR) | Spring Data JPA + Pageable | Strangler |
| Card View | COCRDSLC | REST GET endpoint | Strangler |
| Card Update | COCRDUPC | REST PUT endpoint | Strangler |
| Card Xref Lookup | CCXREF VSAM reads (7 consumers) | Card Lookup API + Redis cache | Strangler |

### Steps

| # | Task | Duration |
|---|------|----------|
| 2.1 | Implement Card Management Service: CRUD REST endpoints with Spring Data JPA | Week 15-17 |
| 2.2 | Migrate CARDDAT and CCXREF data to `cards` and `card_xref` tables | Week 17 |
| 2.3 | Implement Card Lookup API (replaces CCXREF direct reads) with Redis caching | Week 17-18 |
| 2.4 | Build React UI components: card list (paginated), card detail, card edit forms | Week 18-20 |
| 2.5 | Set up bidirectional data sync between VSAM and RDBMS for transition period | Week 19-20 |
| 2.6 | Update API gateway routing: /cards/* -> Card Management Service | Week 20 |
| 2.7 | Validate CARDAIX (alternate index by account ID) behavior via API | Week 20-21 |
| 2.8 | Shadow testing: run both mainframe and Java service, compare outputs for 1000+ card records | Week 21-22 |
| 2.9 | Cut over: stop VSAM sync, Card Management Service is system of record | Week 22 |

### CCXREF Migration -- Critical Path

Since CCXREF is read by 7 programs across 5 bounded contexts, the Card Lookup API must be operational before other contexts migrate:

```
Phase 2 (NOW):   Card Mgmt owns CCXREF -> exposes API
Phase 3 (later): Account Mgmt calls Card Lookup API (replaces CXACAIX reads)
Phase 4 (later): Transaction Processing calls Card Lookup API (replaces CCXREF reads)
Phase 4 (later): Financial Operations calls Card Lookup API
Phase 5 (later): Reporting calls Card Lookup API
```

During Phase 2 transition, non-migrated mainframe programs still read CCXREF directly. The bidirectional sync ensures consistency.

### Validation Checkpoint
- [ ] Card list pagination matches COCRDLIC browse behavior (7 rows per page)
- [ ] Card detail displays all fields from CVACT02Y (card_num, acct_id, cvv, embossed_name, expiry, status)
- [ ] Card update persists changes and triggers cache invalidation
- [ ] Card Lookup API returns correct results for both card-num and account-id queries
- [ ] Shadow test: 100% output match between mainframe and Java service
- [ ] VSAM-RDBMS sync operates with <1 second latency

### Rollback Procedure
1. Route /cards/* back to mainframe proxy in API gateway
2. VSAM files remain consistent via bidirectional sync
3. Disable Card Management Service
4. Estimated rollback time: <10 minutes

### Exit Criteria
- Card Management Service handles all card operations
- Card Lookup API operational with <50ms p95 latency
- CCXREF data sync stopped; Card Management Service is system of record
- Mainframe COCRDLIC, COCRDSLC, COCRDUPC decommissioned

---

## Phase 3: Account Management (Weeks 23-34) -- Risk: MEDIUM

### Objective
Migrate account and customer management, the largest and most complex online functional area.

### Entry Criteria
- Phase 2 complete (Card Lookup API operational)
- Account and Customer database schemas created
- COACTUPC business rules documented and unit tests written from COBOL analysis

### Scope

| Component | Current | Target | Strategy |
|-----------|---------|--------|----------|
| Account View | COACTVWC (942 LOC) | REST endpoint + React UI | Refactor |
| Account Update | COACTUPC (4,237 LOC) | REST endpoint + validation service | Refactor |
| Batch Account Processing | CBACT01C-03C | Spring Batch jobs | Refactor |
| Customer Data | Embedded in Account programs | Separate Customer sub-resource | Refactor |

### Steps

| # | Task | Duration |
|---|------|----------|
| 3.1 | Extract and document all validation rules from COACTUPC (SSN format, date validation, credit limit rules, phone format, FICO score ranges) | Week 23-24 |
| 3.2 | Implement Account domain model: JPA entities for Account and Customer with Bean Validation annotations | Week 24-26 |
| 3.3 | Implement Account Service REST API: view, update, with full validation parity | Week 26-28 |
| 3.4 | Replace CXACAIX reads with Card Lookup API calls (cross-context dependency) | Week 27 |
| 3.5 | Build React UI: account view, account update form with client-side validation | Week 28-30 |
| 3.6 | Migrate ACCTDAT and CUSTDAT data to `accounts` and `customers` tables | Week 29-30 |
| 3.7 | Implement Balance Adjustment API (POST /accounts/{id}/balance-adjustment) for use by Transaction and Financial contexts | Week 30-31 |
| 3.8 | Set up ACCTDAT bidirectional sync (critical: Financial Operations and Transaction Processing still write to ACCTDAT) | Week 30-31 |
| 3.9 | Convert CBACT01C-03C to Spring Batch jobs | Week 31-32 |
| 3.10 | Integration and shadow testing | Week 32-34 |
| 3.11 | Cut over: Account Management Service is system of record | Week 34 |

### ACCTDAT Shared-Write Transition

ACCTDAT is written by three contexts during transition:
1. **Account Management** (COACTUPC) -- migrating now
2. **Transaction Processing** (CBTRN02C) -- still on mainframe
3. **Financial Operations** (COBIL00C, CBACT04C) -- still on mainframe

During Phase 3 transition:
```
Account Updates:    Web UI -> Account Service -> accounts table -> sync -> ACCTDAT
Balance Updates:    CBTRN02C -> ACCTDAT -> sync -> accounts table
Bill Payments:      COBIL00C -> ACCTDAT -> sync -> accounts table
Interest Calc:      CBACT04C -> ACCTDAT -> sync -> accounts table
```

This bidirectional sync is the **highest-risk component** of Phase 3.

### Validation Checkpoint
- [ ] All COACTUPC validation rules reproduced: SSN (9 digits), phone (15 chars), dates (YYYY-MM-DD), credit limit (positive decimal), FICO (000-999)
- [ ] Account view displays all CVACT01Y fields including derived data (customer name from CUSTDAT)
- [ ] Balance Adjustment API correctly handles concurrent updates (optimistic locking)
- [ ] Bidirectional sync maintains <500ms consistency window
- [ ] Shadow test: 100% match for account view and update operations
- [ ] Batch jobs (CBACT01C-03C) produce identical output files

### Rollback Procedure
1. Route /accounts/* back to mainframe proxy
2. Bidirectional sync ensures RDBMS and VSAM are consistent
3. Stop Account Service, resume mainframe operations
4. Estimated rollback time: <15 minutes (must verify sync is current)

### Exit Criteria
- Account Management Service handles all account and customer operations
- Balance Adjustment API ready for Transaction and Financial contexts
- ACCTDAT sync continues for non-migrated batch writers
- Mainframe COACTVWC, COACTUPC, CBACT01C-03C decommissioned

---

## Phase 4: Transaction Processing & Financial Operations (Weeks 35-50) -- Risk: HIGH

### Objective
Migrate the financial core -- transaction posting, bill payment, and interest calculation. This is the **highest-risk phase** due to multi-context writes and financial accuracy requirements.

### Entry Criteria
- Phase 3 complete (Account Balance Adjustment API operational)
- Card Lookup API operational (Phase 2)
- Financial calculation test suite with known-good outputs prepared
- Parallel run infrastructure ready (run both systems, compare results)

### Scope

| Component | Current | Target | Strategy |
|-----------|---------|--------|----------|
| Transaction List/View | COTRN00C/01C | REST endpoints + React UI | Refactor |
| Transaction Add | COTRN02C (784 LOC) | REST endpoint with validation | Refactor |
| Transaction Posting | CBTRN02C (732 LOC) | Spring Batch job | Refactor |
| Bill Payment | COBIL00C (573 LOC) | Spring service with saga | Refactor |
| Interest Calculation | CBACT04C (653 LOC) | Spring Batch job | Refactor |

### Steps -- Sub-Phase 4A: Online Transaction Management (Weeks 35-42)

| # | Task | Duration |
|---|------|----------|
| 4A.1 | Implement Transaction domain model: JPA entity for Transaction (CVTRA05Y mapping) | Week 35-36 |
| 4A.2 | Implement Transaction Service: list (paginated), view, add endpoints | Week 36-38 |
| 4A.3 | Port COTRN02C validation rules (type code, category code, amount format, date format, merchant fields) | Week 37-38 |
| 4A.4 | Replace CCXREF reads with Card Lookup API calls | Week 38 |
| 4A.5 | Build React UI: transaction list, detail view, add form | Week 38-40 |
| 4A.6 | Migrate TRANSACT data to `transactions` table | Week 39-40 |
| 4A.7 | Integration testing and shadow comparison | Week 40-42 |

### Steps -- Sub-Phase 4B: Batch Transaction Posting (Weeks 41-46)

| # | Task | Duration |
|---|------|----------|
| 4B.1 | Design Spring Batch job for transaction posting (replacing CBTRN02C) | Week 41 |
| 4B.2 | Implement batch steps: (1) Read DALYTRAN, (2) Validate via Card Lookup API, (3) Post to transactions table, (4) Call Account Balance Adjustment API, (5) Update tran_category_balances, (6) Write rejects | Week 41-44 |
| 4B.3 | Implement reject handling: DALYREJS equivalent with detailed reject reason codes | Week 43-44 |
| 4B.4 | Parallel run: execute both CBTRN02C (mainframe) and Spring Batch job with identical input, compare all outputs | Week 44-45 |
| 4B.5 | Validate financial accuracy: every posted transaction amount, every balance update, every category balance must match to the penny | Week 45-46 |
| 4B.6 | Performance test: process 100K+ daily transactions within batch window | Week 46 |

### Steps -- Sub-Phase 4C: Financial Operations (Weeks 45-50)

| # | Task | Duration |
|---|------|----------|
| 4C.1 | Implement Bill Payment service as a saga: verify balance -> resolve card -> create transaction -> adjust balance | Week 45-47 |
| 4C.2 | Implement compensating transactions for saga failure scenarios | Week 47 |
| 4C.3 | Implement Interest Calculation Spring Batch job (replacing CBACT04C): read tran_category_balances -> lookup disclosure_groups -> compute interest -> call Balance Adjustment API | Week 47-49 |
| 4C.4 | Migrate DISCGRP data to `disclosure_groups` table | Week 48 |
| 4C.5 | Parallel run for interest calculation: compare output to penny-level accuracy | Week 49-50 |
| 4C.6 | Cut over: stop ACCTDAT bidirectional sync, Java services are system of record for all financial data | Week 50 |

### Parallel Run Protocol

For financial operations, a **mandatory parallel run** of at least 2 complete batch cycles:

```
Cycle N:
  1. Same DALYTRAN input fed to both systems
  2. CBTRN02C runs on mainframe -> output A
  3. Spring Batch job runs on Java -> output B
  4. Automated comparison:
     - Every posted transaction: tran_id, amount, timestamps
     - Every account balance: acct_id, curr_bal to 2 decimal places
     - Every category balance: composite key + balance
     - Every reject: tran_id, reject reason
  5. ZERO discrepancies required to proceed

Cycle N+1:
  Repeat with production-volume data
```

### Validation Checkpoint
- [ ] Transaction list pagination matches COTRN00C browse behavior
- [ ] Transaction add validates all fields per COTRN02C rules
- [ ] Batch posting: 100% output match in parallel run (2+ cycles)
- [ ] Bill payment saga handles: success, insufficient balance, invalid card, system failure
- [ ] Interest calculation: penny-level accuracy match in parallel run
- [ ] BigDecimal arithmetic preserves COBOL PIC S9(09)V99 precision
- [ ] Batch job completes within existing batch window (performance parity)
- [ ] No orphaned transactions or double-postings during cutover

### Rollback Procedure
1. **Sub-Phase 4A (online):** Route /transactions/* back to mainframe proxy. Bidirectional sync ensures consistency.
2. **Sub-Phase 4B (batch posting):** Revert to mainframe CBTRN02C. DALYTRAN input is idempotent if re-processed.
3. **Sub-Phase 4C (financial):** Revert to mainframe COBIL00C and CBACT04C. Restore ACCTDAT sync.
4. **Critical:** If rollback needed during 4C cutover, must reconcile any transactions posted by Java services back to VSAM.
5. Estimated rollback time: 30-60 minutes (includes data reconciliation verification)

### Exit Criteria
- All transaction operations handled by Java services
- Batch posting job produces identical results to CBTRN02C
- Bill payment saga handles all edge cases
- Interest calculation matches to penny-level accuracy
- ACCTDAT bidirectional sync stopped; Java database is system of record
- Mainframe batch cycle (CLOSEFIL->...->OPENFIL) replaced by Spring Batch orchestration

---

## Phase 5: Reporting, Statements & Decommission (Weeks 51-62) -- Risk: MEDIUM

### Objective
Replace the reporting/statement generation system and decommission the mainframe entirely.

### Entry Criteria
- Phases 1-4 complete (all data-owning services operational)
- All VSAM data fully migrated to RDBMS
- No remaining mainframe data writes

### Scope

| Component | Current | Target | Strategy |
|-----------|---------|--------|----------|
| Report Request | CORPT00C (JCL submission via TDQ) | REST API + scheduled job trigger | Rewrite |
| Transaction Report | CBTRN03C | Spring Batch + Thymeleaf/Jasper | Rewrite |
| Statement Generation | CBSTM03A/B (924 LOC, ALTER/GOTO) | Spring Batch + templating engine | Rewrite |
| Optional: Auth Module | COPAUA0C, COPAUS0C-2C, CBPAUP0C | Spring services (if in scope) | Rewrite |
| Optional: Tran Type DB2 | COTRTLIC, COTRTUPC, COBTUPDT | Spring Boot + JPA (replaces DB2) | Rewrite |
| Optional: VSAM-MQ | CDRD/CODATE01, CDRA/COACCT01 | Spring AMQP services | Rewrite |

### Steps

| # | Task | Duration |
|---|------|----------|
| 5.1 | Implement Reporting Service: REST API to request reports with date range parameters | Week 51-52 |
| 5.2 | Build Transaction Report job: Spring Batch reader from transactions table, Thymeleaf template output | Week 52-54 |
| 5.3 | Build Statement Generation job: aggregate by card/account, generate PDF and HTML output | Week 54-57 |
| 5.4 | Replace JCL submission (CORPT00C's TDQ/INTRDR pattern) with Spring Batch job launcher | Week 55-56 |
| 5.5 | Build React UI: report request page, statement download page | Week 56-58 |
| 5.6 | (Optional) Migrate authorization module programs if in scope | Week 57-59 |
| 5.7 | (Optional) Migrate transaction type DB2 programs if in scope | Week 58-60 |
| 5.8 | Validate statement output: format and content match (allow format differences for PDF vs text) | Week 58-60 |
| 5.9 | Final data reconciliation: verify all VSAM data exists in RDBMS | Week 60-61 |
| 5.10 | Decommission mainframe: remove CICS resource definitions, delete VSAM files, archive JCL | Week 61-62 |

### Batch Cycle Replacement

The mainframe batch cycle is replaced by a Spring Batch orchestration:

```
Mainframe (Current)                    Java (Target)
--------------------                   --------------------------
CLOSEFIL (quiesce CICS)               (not needed - no file locking)
ACCTFILE/CARDFILE/CUSTFILE/XREFFILE    (not needed - RDBMS is live)
POSTTRAN (CBTRN02C)                   --> Transaction Posting Job
INTCALC  (CBACT04C)                   --> Interest Calculation Job
TRANBKP  (backup)                     --> Database backup (pg_dump)
COMBTRAN (combine)                    --> (handled by database)
CREASTMT (CBSTM03A/B)                --> Statement Generation Job
TRANIDX  (rebuild indexes)           --> (handled by database)
OPENFIL  (resume CICS)               (not needed - no file locking)
```

Spring Batch job orchestration preserves the dependency ordering:
```java
// Posting must complete before Interest Calculation
// Interest Calculation must complete before Statements
JobExecution posting = jobLauncher.run(transactionPostingJob, params);
if (posting.getStatus() == BatchStatus.COMPLETED) {
    JobExecution interest = jobLauncher.run(interestCalcJob, params);
    if (interest.getStatus() == BatchStatus.COMPLETED) {
        jobLauncher.run(statementGenJob, params);
    }
}
```

### Validation Checkpoint
- [ ] Transaction reports contain all fields from CBTRN03C output
- [ ] Statements include: customer name/address, account details, transaction summary, totals
- [ ] Statement totals match to the penny vs CBSTM03A output
- [ ] Report request via web UI triggers batch job and delivers downloadable output
- [ ] All VSAM data verified present in RDBMS (record count + checksum comparison)
- [ ] No mainframe dependencies remain

### Rollback Procedure
1. Reporting is read-only; rollback simply re-enables mainframe report jobs
2. For decommission step: maintain mainframe in standby for 30 days after cutover
3. Keep VSAM file backups for 90 days post-decommission

### Exit Criteria
- All reporting and statement generation handled by Java services
- Mainframe fully decommissioned
- 30-day monitoring period with no issues
- All JCL, CICS resource definitions, and VSAM files archived

---

## Phase Summary Timeline

```
Week:  1    6   7   14  15  22  23  34  35       50  51      62
       |----|----|----|----|----|----|----|---------|----|-------|
       Phase 0   Phase 1   Phase 2   Phase 3   Phase 4   Phase 5
       Foundation Auth+UI   Cards     Accounts  Transactions Reporting
                                                +Financial   +Decommission
       
Risk:  MINIMAL   LOW       LOW-MED   MEDIUM    HIGH         MEDIUM
```

---

## Cutover Decision Gates

Each phase requires explicit sign-off before proceeding:

| Gate | Criteria | Decision Maker |
|------|----------|----------------|
| G0 -> G1 | Infrastructure operational, data toolkit validated | Technical Lead |
| G1 -> G2 | Auth service live, web UI accessible, zero auth failures for 48 hours | Product Owner + Tech Lead |
| G2 -> G3 | Card service shadow test 100% match, Card Lookup API <50ms p95 | Tech Lead |
| G3 -> G4 | Account service validated, bidirectional sync proven stable for 1 week | Product Owner + Tech Lead + DBA |
| G4 -> G5 | Parallel run: ZERO financial discrepancies for 2+ batch cycles | Product Owner + Tech Lead + Finance + Compliance |
| G5 -> Done | All reporting validated, 30-day monitoring clear | Full Steering Committee |

---

## Resource Requirements

| Role | Phase 0 | Phase 1 | Phase 2 | Phase 3 | Phase 4 | Phase 5 |
|------|---------|---------|---------|---------|---------|---------|
| Java Developers | 2 | 3 | 3 | 4 | 5 | 3 |
| Frontend Developers | 0 | 2 | 1 | 1 | 2 | 1 |
| COBOL SME (knowledge transfer) | 1 | 1 | 1 | 1 | 2 | 1 |
| QA Engineers | 1 | 2 | 2 | 2 | 3 | 2 |
| DevOps/Platform | 2 | 1 | 1 | 1 | 1 | 1 |
| DBA | 1 | 0.5 | 0.5 | 1 | 1 | 0.5 |
