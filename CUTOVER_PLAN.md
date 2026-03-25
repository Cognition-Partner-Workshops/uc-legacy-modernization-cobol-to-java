# CardDemo Cutover Plan

## Overview

This document defines a phased migration sequence for the CardDemo mainframe application, ordered from **lowest-risk to highest-risk**. Each phase specifies the programs being migrated, prerequisites, success criteria, estimated effort, and rollback strategy.

**Total Estimated Duration**: 9--12 months across 6 phases

**Parallel Run Principle**: Every phase includes a parallel-run period where both mainframe and Java implementations process the same inputs and outputs are reconciled before cutover.

---

## Phase Sequence Summary

| Phase | Name | Risk | Duration | Programs | Strategy |
|-------|------|------|----------|----------|----------|
| 0 | Foundation & Infrastructure | Lowest | 4--6 weeks | N/A (platform setup) | N/A |
| 1 | Identity & Access + Utilities | Low | 4--6 weeks | COSGN00C, COMEN01C, COADM01C, COUSR00-03C, CSUTLDTC | Strangler Fig + Rewrite |
| 2 | Data Export/Import + Reference Data | Low | 3--4 weeks | CBEXPORT, CBIMPORT, COTRTUPC, COTRTLIC, COBTUPDT | Rewrite + Refactor |
| 3 | Account & Card Management | Medium | 8--10 weeks | COACTVWC, COACTUPC, COCRDLIC, COCRDSLC, COCRDUPC | Refactor |
| 4 | Transaction Management + Bill Payment + Reporting | Medium-High | 8--10 weeks | COTRN00-02C, COBIL00C, CORPT00C, CBTRN03C | Strangler Fig + Rewrite |
| 5 | Batch Processing (Posting + Interest) | High | 8--10 weeks | CBTRN02C, CBACT04C, CBTRN01C | Refactor |
| 6 | Statements + Optional Modules + Decommission | Highest | 6--8 weeks | CBSTM03A/B, COPAU*, COBSWAIT | Rewrite |

---

## Phase 0: Foundation & Infrastructure

**Risk Level**: Lowest
**Duration**: 4--6 weeks
**Objective**: Establish the target platform, CI/CD pipeline, database schema, and anti-corruption layer (ACL) framework.

### Deliverables

| # | Deliverable | Details |
|---|------------|---------|
| 0.1 | Target platform provisioning | Spring Boot 3.x project structure, Maven multi-module build, PostgreSQL database, message broker (Kafka/RabbitMQ) |
| 0.2 | Database schema creation | Migrate VSAM record layouts to relational tables: `users`, `accounts`, `customers`, `cards`, `card_xref`, `transactions`, `daily_transactions`, `transaction_category_balances`, `discount_groups` |
| 0.3 | Data migration scripts | Load ASCII data files from `app/data/ASCII/` into PostgreSQL tables. Validate record counts and checksums. |
| 0.4 | CI/CD pipeline | GitHub Actions / Jenkins pipeline for build, test, and deploy. Include COBOL-to-Java integration test harness. |
| 0.5 | API Gateway setup | Kong or Spring Cloud Gateway configured to route between legacy CICS and new services |
| 0.6 | ACL framework | Anti-corruption layer skeleton: COMMAREA-to-JWT translator, VSAM proxy interface, event bridge |
| 0.7 | Monitoring & observability | Prometheus + Grafana dashboards for both legacy and new services. Distributed tracing (Jaeger/Zipkin). |
| 0.8 | `carddemo-common` module | Shared DTOs, validation utilities (date, SSN, phone), exception hierarchy, message constants |

### Prerequisites
- Cloud infrastructure provisioned (AWS/Azure/GCP)
- Database instance available
- Network connectivity between mainframe and cloud (if running parallel)
- Development team onboarded with Java/Spring Boot training

### Success Criteria
- [ ] All database tables created with correct column types matching COBOL PIC clauses
- [ ] All ASCII data files loaded; record counts match source files
- [ ] API Gateway routes traffic; ACL skeleton returns mock responses
- [ ] CI/CD pipeline runs green on empty Spring Boot application
- [ ] Monitoring dashboards active

### Rollback Strategy
Phase 0 has no production impact. Rollback is simply deleting cloud resources.

---

## Phase 1: Identity & Access Management + Utilities

**Risk Level**: Low
**Duration**: 4--6 weeks
**Objective**: Replace mainframe authentication with Spring Security + JWT. Migrate user CRUD operations. Replace utility programs with Java standard library equivalents.

### Programs Migrated

| Program | Function | Target Java Component | LOC |
|---------|----------|----------------------|-----|
| COSGN00C | User signon | `AuthController.login()` + Spring Security | 261 |
| COMEN01C | User main menu | React/Angular SPA routing | 300 |
| COADM01C | Admin main menu | React/Angular SPA routing (admin role) | 300 |
| COUSR00C | User list (paginated) | `UserController.listUsers()` + Spring Data `Pageable` | 696 |
| COUSR01C | User add | `UserController.createUser()` | 350 |
| COUSR02C | User update | `UserController.updateUser()` | 400 |
| COUSR03C | User delete | `UserController.deleteUser()` | 350 |
| CSUTLDTC | Date validation utility | `java.time.LocalDate.parse()` + custom validator | 200 |

### Data Migration

| VSAM File | Target Table | Record Count (sample) | Migration Method |
|-----------|-------------|----------------------|------------------|
| USRSEC | `users` | ~10 records | Direct load from `app/data/ASCII/USRSEC.txt` |

### Implementation Steps

1. **Week 1--2**: Implement `identity-service`
   - Spring Security configuration with JWT token issuance
   - `User` JPA entity from CSUSR01Y copybook (SEC-USR-ID, SEC-USR-FNAME, SEC-USR-LNAME, SEC-USR-PWD -> bcrypt hash, SEC-USR-TYPE -> ROLE_ADMIN / ROLE_USER)
   - REST endpoints: POST /auth/login, GET /users, POST /users, PUT /users/{id}, DELETE /users/{id}
   - Password migration: hash existing plaintext passwords on first load

2. **Week 2--3**: Implement web frontend shell
   - Login page replacing COSGN0A BMS map
   - Main menu replacing COMEN01 BMS map (11 menu options as navigation links)
   - Admin menu replacing COADM01 BMS map
   - User management screens replacing COUSR00-03 BMS maps

3. **Week 3--4**: ACL integration
   - JWT-to-COMMAREA translator: extract CDEMO-USER-ID and CDEMO-USER-TYPE from JWT claims, populate COMMAREA for programs still on CICS
   - API Gateway routes /auth/* and /users/* to identity-service, all other routes to ACL -> CICS

4. **Week 4--6**: Testing & parallel run
   - Login via web UI and verify JWT issuance
   - Test user CRUD operations
   - Verify remaining CICS programs still work via ACL with JWT-derived COMMAREA
   - Load test with concurrent sessions

### Dependencies
- Phase 0 complete (infrastructure, database, API gateway)

### Success Criteria
- [ ] Users can log in via web UI and receive JWT tokens
- [ ] Admin users can list, add, update, and delete users
- [ ] JWT tokens contain correct user ID, type, and role claims
- [ ] Remaining CICS programs (account, card, transaction) still function via ACL
- [ ] No authentication-related ABEND codes in CICS logs
- [ ] Response time for login < 500ms (P95)

### Rollback Strategy
- API Gateway routes reverted to bypass identity-service
- CICS signon screen (CC00 transaction) re-enabled
- USRSEC VSAM file remains unchanged (read-only during Phase 1)

---

## Phase 2: Data Export/Import + Reference Data

**Risk Level**: Low
**Duration**: 3--4 weeks
**Objective**: Replace mainframe data utilities with Java-based migration tools. Migrate optional DB2 transaction type management.

### Programs Migrated

| Program | Function | Target Java Component | LOC |
|---------|----------|----------------------|-----|
| CBEXPORT | Data export to sequential files | Spring Batch `FlatFileItemWriter` | 300 |
| CBIMPORT | Data import from sequential files | Spring Batch `FlatFileItemReader` | 300 |
| COTRTUPC | Transaction type add/edit (DB2) | `TransactionTypeController` CRUD | 400 |
| COTRTLIC | Transaction type list/delete (DB2) | `TransactionTypeController` list + delete | 300 |
| COBTUPDT | Batch transaction type update (DB2) | Spring Batch `JdbcBatchItemWriter` | 200 |

### Implementation Steps

1. **Week 1--2**: Data migration tools
   - Spring Batch jobs to export/import data between PostgreSQL and flat files
   - Support both ASCII and CSV formats
   - Validate against original EBCDIC data (character encoding verification)

2. **Week 2--3**: Reference data service (optional module)
   - `reference-data-service` with `TransactionType` JPA entity
   - Migrate DB2 embedded SQL (EXEC SQL) to Spring Data JPA repositories
   - REST endpoints: GET/POST/PUT/DELETE /transaction-types

3. **Week 3--4**: Testing
   - Round-trip test: export from mainframe -> import to PostgreSQL -> export from PostgreSQL -> compare
   - Transaction type CRUD operations tested with existing data

### Dependencies
- Phase 0 complete
- Phase 1 recommended (for authenticated access to reference data service)

### Success Criteria
- [ ] Data export produces files matching mainframe output byte-for-byte (after encoding conversion)
- [ ] Data import loads all records with zero data loss
- [ ] Transaction type CRUD operations work via REST API
- [ ] Round-trip data integrity verified (checksums match)

### Rollback Strategy
- Data export/import utilities are standalone; simply stop using them
- Reference data service can be disabled; CICS transaction type screens re-enabled

---

## Phase 3: Account & Card Management

**Risk Level**: Medium
**Duration**: 8--10 weeks
**Objective**: Migrate the core account and card data management functions. This is the most data-intensive phase, as ACCTDAT is the most widely-shared VSAM file.

### Programs Migrated

| Program | Function | Target Java Component | LOC |
|---------|----------|----------------------|-----|
| COACTVWC | Account view | `AccountController.getAccount()` | 942 |
| COACTUPC | Account update | `AccountController.updateAccount()` + `AccountValidator` | 4,237 |
| COCRDLIC | Card list (paginated) | `CardController.listCards()` + Spring Data `Pageable` | 1,460 |
| COCRDSLC | Card detail view | `CardController.getCard()` | 500 |
| COCRDUPC | Card update | `CardController.updateCard()` + `CardValidator` | 1,540 |

### Data Migration

| VSAM File | Target Table | Record Size | Key Fields |
|-----------|-------------|-------------|------------|
| ACCTDAT | `accounts` | 300 bytes | ACCT-ID (PK) |
| CARDDAT | `cards` | 150 bytes | CARD-NUM (PK) |
| CUSTDAT | `customers` | 500 bytes | CUST-ID (PK) |
| CCXREF / CXACAIX | `card_xref` | 50 bytes | XREF-CARD-NUM (PK) |

### Implementation Steps

1. **Week 1--3**: Account service
   - `Account` JPA entity from CVACT01Y (11 active fields + filler)
   - `Customer` JPA entity from CVCUS01Y (19 active fields + filler)
   - `CardXref` JPA entity from CVACT03Y (3 active fields)
   - Account view: REST GET /accounts/{id} with joined customer and card data
   - Account update: REST PUT /accounts/{id} with full validation port from COACTUPC
   - **Critical**: Port all 4,237 lines of validation logic from COACTUPC
     - SSN validation (3-part: area, group, serial)
     - Phone number formatting (10-digit with area code)
     - Date validation (open date, expiry, reissue -- cross-field checks)
     - Credit limit validation (cash credit <= total credit)
     - State code validation
     - FICO score range check

2. **Week 3--5**: Card service
   - `Card` JPA entity from CVACT02Y (6 active fields)
   - Card list: REST GET /cards?accountId={id}&page={n} replacing COCRDLIC pagination
   - Card detail: REST GET /cards/{num}
   - Card update: REST PUT /cards/{num} with validation (CVV, expiration date, status)

3. **Week 5--7**: Dual-write ACL
   - Account service writes to both PostgreSQL and VSAM (via ACL proxy)
   - Card service writes to both PostgreSQL and VSAM
   - VSAM reads by remaining CICS programs redirected through ACL to database
   - Reconciliation job: nightly comparison of VSAM and database records

4. **Week 7--8**: Frontend integration
   - Account view/update screens replacing COACTVW and COACTUP BMS maps
   - Card list/view/update screens replacing COCRDLI, COCRDSL, COCRDUP BMS maps
   - Navigation from account view to card list (drill-down)

5. **Week 8--10**: Parallel run & validation
   - Both mainframe and Java serving account/card operations
   - Reconcile all account reads/writes daily
   - Performance benchmarking: < 200ms P95 for account view, < 500ms P95 for account update
   - Validation regression suite: 100% field-level validation parity

### Dependencies
- Phase 0 complete (database schema, ACL)
- Phase 1 complete (authentication -- account screens require user identity)

### Success Criteria
- [ ] All account fields displayed correctly (compare BMS screen output vs. web UI)
- [ ] Account update validates all fields identically to COACTUPC
- [ ] Card list pagination matches COCRDLIC behavior (same records per page, same sort order)
- [ ] Dual-write reconciliation shows zero discrepancies for 7 consecutive days
- [ ] No data corruption in ACCTDAT during parallel run
- [ ] Remaining CICS programs (transaction, billing) still read correct account data

### Rollback Strategy
- Disable dual-write; revert API Gateway routes to CICS
- VSAM remains the system of record throughout Phase 3 (database is secondary)
- If validation discrepancy found, investigate and fix before proceeding
- **Hard gate**: Do not proceed to Phase 4 until 7-day zero-discrepancy reconciliation achieved

---

## Phase 4: Transaction Management + Bill Payment + Reporting

**Risk Level**: Medium-High
**Duration**: 8--10 weeks
**Objective**: Migrate online transaction CRUD, bill payment processing, and report generation. This phase touches financial transaction data and includes the first payment processing migration.

### Programs Migrated

| Program | Function | Target Java Component | LOC |
|---------|----------|----------------------|-----|
| COTRN00C | Transaction list | `TransactionController.listTransactions()` | 700 |
| COTRN01C | Transaction view | `TransactionController.getTransaction()` | 500 |
| COTRN02C | Transaction add | `TransactionController.addTransaction()` | 784 |
| COBIL00C | Bill payment | `PaymentController.payBill()` | 573 |
| CORPT00C | Report trigger | `ReportController.submitReport()` | 650 |
| CBTRN03C | Batch report gen | Spring Batch `ReportGenerationJob` | 700 |

### Data Migration

| VSAM File | Target Table | Notes |
|-----------|-------------|-------|
| TRANSACT | `transactions` | 350-byte records, high volume -- migrate incrementally |

### Implementation Steps

1. **Week 1--3**: Transaction service (online CRUD)
   - `Transaction` JPA entity from CVTRA05Y (17 active fields)
   - Transaction list: REST GET /transactions?page={n} with pagination (replacing PF7/PF8)
   - Transaction view: REST GET /transactions/{id}
   - Transaction add: REST POST /transactions with validation
     - Account/card cross-reference validation (CCXREF lookup)
     - Date format validation (YYYY-MM-DD)
     - Amount validation (positive, within limits)
     - Auto-ID generation: replace VSAM HIGH-VALUES trick with database sequence
   - Integration with account-service for balance lookups

2. **Week 3--5**: Payment service
   - Bill payment: REST POST /payments/bill
   - **Transactional flow** (replaces CICS READ FOR UPDATE):
     1. Begin database transaction
     2. Read account (SELECT FOR UPDATE)
     3. Validate balance > 0
     4. Lookup card via xref
     5. Create transaction record (type 02, category 2)
     6. Debit account balance
     7. Commit transaction
   - Confirmation flow: POST /payments/bill/preview (read-only) + POST /payments/bill/confirm (execute)
   - **Critical**: Ensure atomicity -- CICS provides it via single-task updates; Spring provides it via `@Transactional`

3. **Week 5--7**: Reporting service
   - Rewrite CORPT00C report submission as async REST API
   - Replace JCL/INTRDR submission with Spring Batch job launcher
   - Report types: monthly, yearly, custom date range
   - Output format: PDF/CSV (replacing mainframe spool MSGCLASS=0)
   - Batch report generation job (CBTRN03C) as Spring Batch step

4. **Week 7--8**: Frontend integration
   - Transaction list/view/add screens replacing COTRN00-02 BMS maps
   - Bill payment screen replacing COBIL00 BMS map (preview + confirm)
   - Report request screen replacing CORPT00 BMS map (date picker, async status)

5. **Week 8--10**: Parallel run & validation
   - Financial reconciliation: compare transaction totals between mainframe and Java daily
   - Bill payment reconciliation: verify account balances match after payments
   - Report output comparison: mainframe spool vs. PDF content

### Dependencies
- Phase 3 complete (account and card data in database; account-service operational)
- Phase 1 complete (user authentication)

### Success Criteria
- [ ] Transaction list matches mainframe output (same records, same sort)
- [ ] Transaction add creates records identical to CICS COTRN02C
- [ ] Bill payment debits correct amount and creates correct transaction record
- [ ] Bill payment atomicity: no partial updates on failure
- [ ] Reports produce accurate data matching mainframe reports
- [ ] Financial reconciliation shows zero discrepancies for 14 consecutive days
- [ ] Payment processing latency < 1 second P99

### Rollback Strategy
- Transaction CRUD routes reverted to CICS
- Payment processing reverted to COBIL00C
- **Critical**: Any financial discrepancy triggers immediate rollback
- TRANSACT VSAM remains system of record; database is secondary until reconciliation passes
- **Hard gate**: 14-day zero financial discrepancy before proceeding to Phase 5

---

## Phase 5: Batch Processing (Transaction Posting + Interest Calculation)

**Risk Level**: High
**Duration**: 8--10 weeks
**Objective**: Migrate the core batch processing jobs that handle daily transaction posting and interest calculation. These are the highest-risk components because they perform bulk financial calculations affecting all accounts.

### Programs Migrated

| Program | Function | Target Java Component | LOC |
|---------|----------|----------------------|-----|
| CBTRN02C | Batch transaction posting | Spring Batch `TransactionPostingJob` | 732 |
| CBACT04C | Batch interest calculation | Spring Batch `InterestCalculationJob` | 653 |
| CBTRN01C | Transaction combination | Spring Batch `TransactionCombineJob` | 500 |

### JCL Jobs Replaced

| JCL Job | Function | Target |
|---------|----------|--------|
| POSTTRAN | Submit CBTRN02C | Spring Batch job with `JobLauncher` |
| INTCALC | Submit CBACT04C | Spring Batch job with `JobLauncher` |
| COMBTRAN | Combine transactions | Spring Batch step in posting flow |
| TRANBKP | Backup transactions | Database backup / point-in-time recovery |
| CLOSEFIL / OPENFIL | Close/open CICS VSAM files | Not needed (database always available) |

### Implementation Steps

1. **Week 1--3**: Transaction posting job
   - Spring Batch job structure:
     - `FlatFileItemReader` or `JdbcCursorItemReader` for daily transactions (DALYTRAN)
     - `ValidatingItemProcessor` implementing CBTRN02C validation pipeline:
       - 1500-A: Card cross-reference lookup
       - 1500-B: Account existence and status check
       - Extensible validation framework ("ADD MORE VALIDATIONS HERE")
     - `CompositeItemWriter`:
       - Write valid transactions to `transactions` table
       - Write rejected transactions to `rejected_transactions` table
       - Update account balances in `accounts` table
       - Update category balances in `transaction_category_balances` table
   - Exit codes: SUCCESS (0), WARNINGS (4 = some rejects), FAILURE (8+)

2. **Week 3--6**: Interest calculation job
   - Spring Batch job structure:
     - `JdbcCursorItemReader` for transaction category balances (ordered by account)
     - **Account grouping logic**: Process all categories for an account, accumulate interest, then update once
     - `InterestCalculationProcessor`:
       - Lookup discount group rate (DISCGRP)
       - Compute monthly interest: `balance * (annual_rate / 12)`
       - Compute fees (if applicable)
       - **Critical**: Use `java.math.BigDecimal` with `ROUND_HALF_UP` to match COBOL packed decimal arithmetic
     - `CompositeItemWriter`:
       - Update account balance
       - Write interest transaction to `transactions` table
   - PARM-DATE handling: Accept processing date as job parameter (replacing JCL EXEC PARM)

3. **Week 6--7**: Batch orchestration
   - Replace JCL batch cycle sequence with Spring Batch flow:
     ```
     data-refresh-step
       -> transaction-posting-step
       -> interest-calculation-step
       -> transaction-backup-step
       -> transaction-combine-step
       -> statement-generation-step (Phase 6)
     ```
   - Conditional flow: stop on posting failure; skip interest calc if no postings
   - Scheduling: Kubernetes CronJob or Spring Scheduler (replacing CA-7 / Control-M)

4. **Week 7--10**: Parallel run & reconciliation
   - **Shadow mode**: Run Java batch alongside mainframe batch for same input data
   - Record-level reconciliation:
     - Transaction counts: posted, rejected, total must match exactly
     - Account balances after posting must match to the cent
     - Interest calculations must match to the cent (BigDecimal precision)
     - Reject reason codes must match
   - **Duration**: Minimum 30-day parallel run covering a full billing cycle
   - Reconciliation automation: nightly job comparing outputs, alerting on any discrepancy

### Dependencies
- Phase 3 complete (account data in database)
- Phase 4 complete (transaction data in database)
- All VSAM files fully migrated to database
- Database is now system of record (promoted in Phase 4)

### Success Criteria
- [ ] Transaction posting: exact match on posted count, rejected count, and total count vs. mainframe
- [ ] Account balances after posting match mainframe to the cent
- [ ] Interest calculation amounts match mainframe to the cent for all accounts
- [ ] Reject reasons match mainframe for all rejected transactions
- [ ] Batch job completes within acceptable time window (e.g., < 2 hours for full posting cycle)
- [ ] 30-day parallel run with zero financial discrepancies
- [ ] Batch restart/recovery works correctly (Spring Batch restart from last checkpoint)

### Rollback Strategy
- Revert to mainframe batch processing (JCL jobs)
- Database changes from Java batch rolled back via database point-in-time recovery
- **Critical**: Any interest calculation discrepancy > $0.01 triggers immediate investigation
- **Hard gate**: 30-day zero-discrepancy parallel run before mainframe batch decommission

---

## Phase 6: Statements + Optional Modules + Decommission

**Risk Level**: Highest (due to decommission)
**Duration**: 6--8 weeks
**Objective**: Complete remaining migrations (statement generation, optional authorization/MQ modules), decommission the mainframe CICS region, and remove the anti-corruption layer.

### Programs Migrated

| Program | Function | Target Java Component | LOC |
|---------|----------|----------------------|-----|
| CBSTM03A | Statement generation (header) | Spring Batch `StatementGenerationJob` | 400 |
| CBSTM03B | Statement generation (details) | Spring Batch step with PDF writer | 400 |
| COPAUA0C | MQ trigger (authorization) | Spring Kafka `@KafkaListener` | 300 |
| COPAUS0C | Auth summary view | `AuthorizationController.getSummary()` | 400 |
| COPAUS1C | Auth detail view | `AuthorizationController.getDetails()` | 400 |
| COPAUS2C | Fraud marking (DB2) | `AuthorizationService.markFraud()` | 300 |
| CBPAUP0C | Auth batch purge | Spring Batch `AuthPurgeJob` | 200 |
| COBSWAIT | Wait utility | `Thread.sleep()` / `ScheduledExecutorService` | 100 |
| CODATE01 | MQ date service | REST GET /system/date | 200 |
| COACCT01 | MQ account inquiry | REST GET /accounts/{id} (already exists) | 200 |

### Implementation Steps

1. **Week 1--3**: Statement generation
   - Rewrite CBSTM03A/B as Spring Batch job producing PDF statements
   - Template-based PDF generation (JasperReports or Apache PDFBox)
   - Statement content: account header, transaction line items, balance summary
   - Output: PDF files stored in object storage (S3) + optional email delivery

2. **Week 3--5**: Optional modules
   - Authorization service: replace MQ trigger with Kafka consumer
   - Fraud marking: JPA operations on `pending_authorizations` table
   - Batch purge: Spring Batch job to archive/delete old authorizations
   - VSAM-MQ integration: already replaced by REST APIs (account-service, date utility)

3. **Week 5--6**: ACL removal
   - Remove COMMAREA-to-JWT translator (no CICS programs remaining)
   - Remove VSAM proxy (all data in database)
   - Remove event bridge (direct service-to-service calls)
   - Simplify API Gateway routes (no legacy backend)

4. **Week 6--7**: Mainframe decommission preparation
   - Final data extraction from VSAM files (archival)
   - CICS region shutdown procedure documented and tested
   - JCL job scheduler (CA-7 / Control-M) entries removed
   - CICS resource definitions (CSD) archived

5. **Week 7--8**: Mainframe decommission execution
   - Scheduled maintenance window for final cutover
   - Stop CICS region
   - Verify all traffic flowing through Java services
   - Monitor for 72 hours post-decommission
   - Archive mainframe source code and data

### Dependencies
- All previous phases complete and stable
- 30-day zero-discrepancy parallel run in Phase 5 achieved
- Stakeholder sign-off on decommission

### Success Criteria
- [ ] Statements generated in PDF format matching mainframe content
- [ ] Authorization service processes events via Kafka
- [ ] ACL fully removed; no legacy routing in API Gateway
- [ ] CICS region stopped with no impact to users
- [ ] All traffic served by Java services for 72+ hours post-decommission
- [ ] No data loss verified via final VSAM-to-database comparison
- [ ] Performance SLAs met under full production load

### Rollback Strategy
- **Pre-decommission**: CICS region can be restarted; VSAM data is archived but restorable
- **Post-decommission (72-hour window)**: Keep mainframe in cold standby for emergency restart
- **After 72 hours**: Mainframe fully decommissioned; rollback requires full recovery plan
- **Decision gate**: Go/no-go decision at the 72-hour mark with all stakeholders

---

## Timeline Summary

```
Month:  1     2     3     4     5     6     7     8     9     10    11    12
        |-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|-----|

Phase 0 [=====]
        Foundation & Infrastructure

Phase 1      [=====]
             Identity & Access + Utilities

Phase 2           [====]
                  Data Export/Import + Reference Data

Phase 3                [==========]
                       Account & Card Management

Phase 4                            [==========]
                                   Transaction + Payment + Reporting

Phase 5                                        [==========]
                                               Batch Processing

Phase 6                                                    [========]
                                                           Statements + Decommission
```

---

## Go/No-Go Decision Gates

Each phase transition requires explicit stakeholder approval based on the success criteria above.

| Gate | Phase Transition | Critical Criteria | Approvers |
|------|-----------------|-------------------|-----------|
| G1 | Phase 0 -> 1 | Infrastructure operational, database populated | Tech Lead, Ops |
| G2 | Phase 1 -> 2 | Authentication working, ACL functional | Tech Lead, Security |
| G3 | Phase 2 -> 3 | Data migration validated, reference data operational | Tech Lead, Data Team |
| G4 | Phase 3 -> 4 | 7-day zero-discrepancy account reconciliation | Tech Lead, Business, QA |
| G5 | Phase 4 -> 5 | 14-day zero financial discrepancy | Tech Lead, Finance, Risk |
| G6 | Phase 5 -> 6 | 30-day zero-discrepancy batch parallel run | All stakeholders |
| G7 | Decommission | 72-hour post-cutover stability | Executive sponsor, all stakeholders |

---

## Resource Requirements

| Phase | Java Developers | QA Engineers | DBA | Mainframe SME | DevOps |
|-------|----------------|-------------|-----|---------------|--------|
| 0 | 2 | 0 | 1 | 1 | 2 |
| 1 | 3 | 1 | 0 | 1 | 1 |
| 2 | 2 | 1 | 1 | 0 | 0 |
| 3 | 4 | 2 | 1 | 1 | 1 |
| 4 | 4 | 2 | 1 | 1 | 1 |
| 5 | 3 | 2 | 1 | 2 | 1 |
| 6 | 3 | 2 | 1 | 1 | 2 |

**Total team size**: 8--12 people at peak (Phases 3--5)
