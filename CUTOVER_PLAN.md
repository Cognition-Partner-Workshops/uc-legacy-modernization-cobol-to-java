# CardDemo Phased Cutover Plan

## Overview

This plan sequences the migration of CardDemo from COBOL/CICS/VSAM to Java/Spring Boot/PostgreSQL in six phases, ordered from **lowest risk to highest risk**. Each phase has defined entry criteria, deliverables, validation gates, and rollback procedures.

**Total Estimated Duration**: 9-12 months (overlapping phases)

---

## Phase 0: Foundation (Weeks 1-4) -- Risk: Minimal

### Objective
Establish the target platform, CI/CD pipeline, database schema, and anti-corruption layer (ACL) infrastructure before migrating any business logic.

### Deliverables

| # | Deliverable | Description |
|---|------------|-------------|
| 0.1 | Target database schema | PostgreSQL tables derived from VSAM copybook analysis (see MODERNIZATION_BLUEPRINT.md Section 6) |
| 0.2 | Data migration scripts | ETL scripts to load ASCII sample data (`app/data/ASCII/`) into PostgreSQL |
| 0.3 | Spring Boot project scaffold | Multi-module Maven/Gradle project with shared libraries, common DTOs, exception handling |
| 0.4 | CI/CD pipeline | Build, test, deploy pipeline for Java services |
| 0.5 | ACL framework | Anti-corruption layer stubs for CICS Transaction Gateway integration |
| 0.6 | Observability stack | Logging, metrics, and tracing infrastructure |
| 0.7 | Dual-run reconciliation framework | Tooling to compare outputs from legacy and new systems in parallel |

### Database Schema (derived from copybooks)

```sql
-- From CSUSR01Y (80 bytes)
CREATE TABLE users (
    user_id         VARCHAR(8) PRIMARY KEY,
    first_name      VARCHAR(20),
    last_name       VARCHAR(20),
    password_hash   VARCHAR(256),  -- upgraded from plaintext
    user_type       CHAR(1),       -- 'A' or 'U'
    created_at      TIMESTAMP DEFAULT NOW()
);

-- From CVACT01Y (300 bytes)
CREATE TABLE accounts (
    account_id          BIGINT PRIMARY KEY,     -- was PIC 9(11)
    active_status       CHAR(1),
    current_balance     DECIMAL(12,2),
    credit_limit        DECIMAL(12,2),
    cash_credit_limit   DECIMAL(12,2),
    open_date           DATE,
    expiration_date     DATE,
    reissue_date        DATE,
    current_cycle_credit DECIMAL(12,2),
    current_cycle_debit  DECIMAL(12,2),
    address_zip         VARCHAR(10),
    group_id            VARCHAR(10),
    customer_id         BIGINT REFERENCES customers(customer_id)
);

-- From CVCUS01Y (500 bytes)
CREATE TABLE customers (
    customer_id         BIGINT PRIMARY KEY,     -- was PIC 9(09)
    first_name          VARCHAR(25),
    middle_name         VARCHAR(25),
    last_name           VARCHAR(25),
    address_line_1      VARCHAR(50),
    address_line_2      VARCHAR(50),
    address_line_3      VARCHAR(50),
    state_code          CHAR(2),
    country_code        CHAR(3),
    zip_code            VARCHAR(10),
    phone_1             VARCHAR(15),
    phone_2             VARCHAR(15),
    ssn                 BIGINT,
    govt_issued_id      VARCHAR(20),
    date_of_birth       DATE,
    eft_account_id      VARCHAR(10),
    primary_cardholder  CHAR(1),
    fico_score          SMALLINT
);

-- From CVACT02Y (150 bytes)
CREATE TABLE cards (
    card_number         VARCHAR(16) PRIMARY KEY,
    account_id          BIGINT REFERENCES accounts(account_id),
    cvv_code            SMALLINT,
    embossed_name       VARCHAR(50),
    expiration_date     DATE,
    active_status       CHAR(1)
);

-- From CVTRA05Y (350 bytes)
CREATE TABLE transactions (
    transaction_id      VARCHAR(16) PRIMARY KEY,
    type_code           CHAR(2),
    category_code       SMALLINT,
    source              VARCHAR(10),
    description         VARCHAR(100),
    amount              DECIMAL(11,2),
    merchant_id         BIGINT,
    merchant_name       VARCHAR(50),
    merchant_city       VARCHAR(50),
    merchant_zip        VARCHAR(10),
    card_number         VARCHAR(16) REFERENCES cards(card_number),
    originated_ts       TIMESTAMP,
    processed_ts        TIMESTAMP
);

-- CARDXREF eliminated -- replaced by cards.account_id FK
-- and accounts.customer_id FK
```

### Entry Criteria
- Project funding and team allocated
- Access to mainframe source code (this repository)
- Target cloud environment provisioned

### Validation Gate
- [ ] All copybook fields accounted for in relational schema
- [ ] ASCII sample data loads successfully into PostgreSQL
- [ ] Spring Boot app starts and connects to database
- [ ] CI/CD pipeline runs green
- [ ] Reconciliation framework can compare a VSAM dump vs. database query

### Rollback
No business impact -- foundation work only.

---

## Phase 1: Read-Only Services & Reference Data (Weeks 3-8) -- Risk: Low

### Objective
Migrate read-only operations and admin reference data management. These have zero write contention and minimal business risk.

### Components Migrated

| Component | Legacy | Target | Strategy |
|-----------|--------|--------|----------|
| Transaction Type CRUD | COTRTUPC, COTRTLIC (DB2) | Spring Data JPA REST API | Refactor |
| Account View | COACTVWC | Account Service (GET endpoint) | Strangler Fig |
| Card List | COCRDLIC | Card Service (GET /cards?accountId=) | Strangler Fig |
| Card View | COCRDSLC | Card Service (GET /cards/{id}) | Strangler Fig |
| Transaction List | COTRN00C | Transaction Service (GET /transactions) | Strangler Fig |
| Transaction View | COTRN01C | Transaction Service (GET /transactions/{id}) | Strangler Fig |
| VSAM-MQ Date Inquiry | CODATE01 | System utility REST endpoint | Strangler Fig |
| VSAM-MQ Account Inquiry | COACCT01 | Account Service GET endpoint | Strangler Fig |

### Sequence

```
Week 3-4: Reference Data Service (COTRTUPC/COTRTLIC -> Spring Data JPA)
          - Convert DB2 embedded SQL to JPA repositories
          - Implement pagination (replaces DB2 cursor forward/backward)
          - Admin UI for transaction type management

Week 4-5: Account View Service
          - Implement GET /api/accounts/{id} endpoint
          - Map CVACT01Y + CVCUS01Y to AccountDto + CustomerDto
          - Read from PostgreSQL (fed by data sync from VSAM)

Week 5-6: Card Service (read operations)
          - Implement GET /api/cards?accountId= and GET /api/cards/{number}
          - Replace VSAM alternate index (CARDAIX) with SQL query

Week 6-8: Transaction Service (read operations)
          - Implement GET /api/transactions with pagination
          - Replace VSAM BROWSE with paginated SQL query
          - Implement GET /api/transactions/{id}
```

### Data Strategy
- One-way sync: VSAM -> PostgreSQL (nightly batch or CDC)
- Legacy remains the system of record for write operations
- Read services point at PostgreSQL

### Entry Criteria
- Phase 0 complete
- Database populated with production-equivalent data
- ACL framework operational

### Validation Gate
- [ ] All read endpoints return data matching CICS screen output
- [ ] Pagination matches COBOL BROWSE behavior (verified with sample data)
- [ ] Response times within SLA (< 200ms p95)
- [ ] Reference data CRUD passes full regression (add, edit, list, delete)
- [ ] Data sync verified: VSAM changes reflected in PostgreSQL within SLA

### Rollback
- Route API gateway back to CICS Transaction Gateway
- No data loss risk (read-only operations)
- Rollback time: minutes (traffic routing change)

---

## Phase 2: Authentication & Web Frontend (Weeks 6-12) -- Risk: Low-Medium

### Objective
Replace COSGN00C with a modern authentication service and build the web frontend that replaces BMS 3270 screens.

### Components Migrated

| Component | Legacy | Target | Strategy |
|-----------|--------|--------|----------|
| Sign-on | COSGN00C | Spring Security + JWT Auth Service | Strangler Fig |
| Regular Menu | COMEN01C | Web frontend (React/Angular) router | Rewrite |
| Admin Menu | COADM01C | Web frontend admin routes | Rewrite |
| User CRUD | COUSR00C-03C | User Management REST API | Rewrite |
| BMS Screens | 17 BMS maps | React/Angular components | Rewrite |

### Sequence

```
Week 6-8:  Auth Service
           - Spring Security with JWT token issuance
           - Password migration: hash existing plaintext passwords on first login
           - RBAC: map CDEMO-USER-TYPE ('A'/'U') to Spring Security roles
           - Token replaces COMMAREA user context

Week 8-10: Web Frontend Shell
           - Application shell with routing
           - Login page (replaces COSGN00 BMS map)
           - Main menu (replaces COMEN01 BMS map) 
           - Admin menu (replaces COADM01 BMS map)
           - Integration with Phase 1 read-only APIs

Week 10-12: Remaining screens
            - Account view screen (replaces COACTVW BMS map)
            - Card list/view screens (replaces COCRDLI/COCRDSL BMS maps)
            - Transaction list/view screens (replaces COTRN00/COTRN01 BMS maps)
            - User management screens (replaces COUSR00-03 BMS maps)
```

### Authentication Migration

```
Legacy Flow:
  3270 Terminal -> COSGN00C -> VSAM USRSEC (plaintext pwd) -> COMMAREA user context

Target Flow:
  Browser -> Auth Service -> PostgreSQL users table (bcrypt hash) -> JWT token
```

**Password Migration Strategy**:
1. Pre-migration: Hash all existing passwords and store in `users.password_hash`
2. First login: Accept legacy password, verify against hash, issue JWT
3. Force password reset after first successful login (optional, recommended)

### Entry Criteria
- Phase 1 read-only services operational
- Web frontend framework selected and scaffolded

### Validation Gate
- [ ] Login with ADMIN001/USER0001 credentials succeeds
- [ ] JWT token contains correct user-id and role claims
- [ ] All 17 BMS screen equivalents render correctly in browser
- [ ] Menu navigation matches legacy flow (including admin-only restrictions)
- [ ] PF-key equivalents implemented (F3=Back, F7/F8=Page)

### Rollback
- Dual-run: Keep CICS sign-on active alongside new auth service
- Users can fall back to 3270 terminal access
- Rollback time: minutes (DNS/routing change)

---

## Phase 3: Write Operations & Transaction Processing (Weeks 10-18) -- Risk: Medium

### Objective
Migrate write operations -- account update, card update, transaction add. This is where data ownership begins transferring from VSAM to PostgreSQL.

### Components Migrated

| Component | Legacy | Target | Strategy |
|-----------|--------|--------|----------|
| Account Update | COACTUPC (4,237 lines) | Account Service (PUT endpoint) | Rewrite |
| Card Update | COCRDUPC | Card Service (PUT endpoint) | Rewrite |
| Transaction Add | COTRN02C | Transaction Service (POST endpoint) | Rewrite |
| Date Validation | CSUTLDTC | java.time validation | Rewrite |

### Sequence

```
Week 10-12: Account Update Service
            - Port all validation rules from COACTUPC
            - Validate: account status, credit limits, dates, FICO score,
              SSN format, phone number format, expiry dates
            - Map CICS REWRITE to JPA save()
            - Implement optimistic locking (replaces CICS record-level locking)

Week 12-14: Card Update Service
            - Port validation from COCRDUPC
            - Card status, embossed name, expiry date updates
            - Maintain card-account relationship integrity

Week 14-16: Transaction Add Service
            - Port from COTRN02C
            - Transaction ID generation (replace VSAM key sequencing
              with database sequence)
            - Validate card number, amounts, merchant data

Week 16-18: Integration testing & dual-write period
            - Enable dual-write: writes go to both VSAM and PostgreSQL
            - Reconciliation reports comparing both data stores
            - Gradually shift read traffic to PostgreSQL
```

### Data Ownership Transfer

```
Phase 3a (dual-write):
  Write -> Java Service -> PostgreSQL (primary)
                        -> ACL -> CICS -> VSAM (shadow)
  
Phase 3b (cutover):
  Write -> Java Service -> PostgreSQL (sole)
  Read  -> Java Service -> PostgreSQL (sole)
  Legacy VSAM -> read-only archive
```

### Critical Validation Rules to Port

From COACTUPC (4,237 lines of validation logic):
- Account status: 'Y' or 'N' only
- Credit limit: signed numeric, non-negative
- Cash credit limit: signed numeric, <= credit limit
- Date fields: valid calendar dates (CCYYMMDD format)
- FICO score: numeric, 300-850 range
- SSN: valid format (not 000, not 666, not 900-999 prefix)
- Phone number: US format (NNN)NNN-NNNN

### Entry Criteria
- Phase 2 auth service operational
- All read-only services validated
- Dual-write infrastructure tested

### Validation Gate
- [ ] Every validation rule from COACTUPC ported and unit tested
- [ ] Dual-write reconciliation shows 100% match for 2 weeks
- [ ] No data loss during write operations (verified by reconciliation)
- [ ] Optimistic locking prevents lost updates
- [ ] Transaction ID sequence is monotonic and gap-free
- [ ] All original COBOL test scenarios pass against new services

### Rollback
- Disable dual-write, route all traffic back to CICS
- PostgreSQL data discarded (VSAM was shadow-written)
- Rollback time: minutes (routing) + data reconciliation

---

## Phase 4: Payment & Reporting (Weeks 16-22) -- Risk: Medium-High

### Objective
Migrate the bill payment workflow (cross-aggregate transaction) and reporting subsystem.

### Components Migrated

| Component | Legacy | Target | Strategy |
|-----------|--------|--------|----------|
| Bill Payment | COBIL00C | Payment Service (saga pattern) | Rewrite |
| Report Request | CORPT00C | Report Service (REST + async) | Strangler Fig |
| Batch Txn Report | CBTRN03C | Report generation job | Rewrite |
| Statement Gen | CBSTM03A/B | Replatformed initially, then PDF service | Replatform |

### Payment Service Design

The bill payment flow in COBIL00C is:

```
Legacy (single CICS task):
  1. READ ACCTDAT (with UPDATE lock)
  2. Validate balance > 0
  3. READ CXACAIX (get card number)
  4. READPREV TRANSACT (get last tran ID)
  5. WRITE TRANSACT (new payment record)
  6. REWRITE ACCTDAT (subtract balance)
```

```
Target (saga pattern):
  1. Payment Service receives POST /api/payments
  2. Query Account Service: GET /api/accounts/{id}
  3. Validate balance > 0
  4. Create transaction: POST /api/transactions (idempotency key)
  5. Update account balance: PUT /api/accounts/{id}/balance
  6. If step 5 fails: compensate by voiding transaction
  7. Return payment confirmation
```

### Sequence

```
Week 16-18: Payment Service
            - Implement saga orchestrator
            - Idempotency key to prevent duplicate payments
            - Compensating transactions for failure scenarios
            - Integration with Account and Transaction services

Week 18-20: Report Service
            - REST API to accept report requests (replaces TDQ/JCL submission)
            - Async report generation using Spring Async or message queue
            - PDF/HTML output (replaces CBSTM03A text/HTML generation)
            - Monthly, yearly, and custom date range reports

Week 20-22: Statement Generation
            - Replatform CBSTM03A/B on Micro Focus runtime (interim)
            - Plan future rewrite as PDF generation service
            - Connect to PostgreSQL for data (via ODBC/JDBC bridge)
```

### Entry Criteria
- Phase 3 write operations stable
- Account and Transaction services handling all CRUD
- Data ownership fully transferred to PostgreSQL

### Validation Gate
- [ ] Payment creates correct transaction record and updates balance
- [ ] Payment is idempotent (duplicate requests handled)
- [ ] Saga compensates correctly on partial failure
- [ ] Reports match legacy output format (field-by-field comparison)
- [ ] Statement generation produces correct customer/account/transaction data
- [ ] Concurrent payment + batch scenarios tested (no lost updates)

### Rollback
- Payment: route back to COBIL00C via ACL
- Reporting: continue using JCL-based reporting
- Rollback time: minutes (routing) but may require data reconciliation

---

## Phase 5: Batch Processing (Weeks 20-28) -- Risk: High

### Objective
Migrate the core batch cycle from JCL/COBOL to Spring Batch. This is high-risk because batch jobs mutate account balances and process financial transactions.

### Components Migrated

| Component | Legacy | Target | Strategy |
|-----------|--------|--------|----------|
| Transaction Posting | CBTRN02C + POSTTRAN.jcl | Spring Batch job | Rewrite |
| Interest Calculation | CBACT04C + INTCALC.jcl | Spring Batch job | Refactor |
| Data Load/Refresh | CBACT01C-03C, CBCUS01C, CBTRN01C | Spring Batch ETL jobs | Refactor |
| Data Export/Import | CBEXPORT, CBIMPORT | Spring Batch jobs | Refactor |
| Transaction Backup | TRANBKP.jcl | Database backup strategy | Rewrite |
| Alternate Index | TRANIDX.jcl | Database indexes (automatic) | Eliminated |
| File Close/Open | CLOSEFIL/OPENFIL.jcl | Eliminated (shared DB) | Eliminated |

### Batch Cycle Transformation

```
Legacy JCL Sequence:
  CLOSEFIL -> ACCTFILE -> CARDFILE -> CUSTFILE -> XREFFILE -> TRANFILE
  -> POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT -> TRANIDX
  -> OPENFIL

Target Spring Batch Orchestration:
  1. Transaction Posting Job (replaces POSTTRAN)
     - ItemReader: daily_transactions table
     - ItemProcessor: validate card xref, validate account
     - ItemWriter: write to transactions table, update account balance
     - Error handling: write rejects to rejected_transactions table
  
  2. Interest Calculation Job (replaces INTCALC)
     - ItemReader: transaction_category_balances table (sequential)
     - ItemProcessor: lookup discount group, compute interest
     - ItemWriter: update account balance, write interest transactions
  
  3. Statement Generation Job (replaces CREASTMT)
     - Uses replatformed CBSTM03A or new PDF service
  
  Eliminated:
  - CLOSEFIL/OPENFIL: No longer needed (shared database, no file locking)
  - TRANIDX: Database indexes are maintained automatically
  - TRANBKP: Replaced by database backup/point-in-time recovery
```

### Transaction Posting Detail (CBTRN02C replacement)

The posting job processes daily transactions with validation:

```java
// Spring Batch equivalent of CBTRN02C logic
@Bean
public Job transactionPostingJob() {
    return jobBuilder.get("transactionPostingJob")
        .start(postingStep())
        .build();
}

@Bean
public Step postingStep() {
    return stepBuilder.get("postingStep")
        .<DailyTransaction, PostedTransaction>chunk(100)
        .reader(dailyTransactionReader())     // replaces 1000-DALYTRAN-GET-NEXT
        .processor(transactionValidator())     // replaces 1500-VALIDATE-TRAN
        .writer(transactionPoster())           // replaces 2000-POST-TRANSACTION
        .faultTolerant()
        .skipPolicy(rejectPolicy())            // replaces 2500-WRITE-REJECT-REC
        .listener(reconciliationListener())
        .build();
}
```

### Sequence

```
Week 20-22: Data Load/Refresh Jobs
            - Convert CBACT01C-03C, CBCUS01C to Spring Batch ETL
            - These are simple file-to-table loaders
            - Validate against legacy loads

Week 22-24: Transaction Posting Job
            - Port CBTRN02C validation logic (1500-VALIDATE-TRAN)
            - Port posting logic (2000-POST-TRANSACTION)
            - Port reject handling (2500-WRITE-REJECT-REC)
            - Run in parallel with legacy for reconciliation

Week 24-26: Interest Calculation Job
            - Port CBACT04C calculation logic
            - Preserve exact decimal arithmetic (BigDecimal)
            - Port discount group lookup (1200-GET-INTEREST-RATE)
            - Port interest computation (1300-COMPUTE-INTEREST)
            - Reconcile to the penny with legacy output

Week 26-28: End-to-end batch cycle validation
            - Run full cycle: posting -> interest -> statements
            - Compare every account balance with legacy
            - Stress test with production-volume data
            - Validate reject counts match
```

### Entry Criteria
- Phase 4 payment service stable
- All data in PostgreSQL (no VSAM dependency for batch input)
- Reconciliation framework proven

### Validation Gate
- [ ] Transaction posting: reject count matches legacy for same input
- [ ] Transaction posting: all accepted transactions match legacy output
- [ ] Interest calculation: every account balance matches legacy to the penny
- [ ] Full batch cycle completes within acceptable time window
- [ ] CLOSEFIL/OPENFIL eliminated without online service disruption
- [ ] 3 consecutive successful parallel runs with zero reconciliation differences

### Rollback
- Resume JCL batch cycle on mainframe
- Restore VSAM files from backup if needed
- Rollback time: hours (requires job restart and data reconciliation)

---

## Phase 6: Authorization Module & Decommission (Weeks 26-36) -- Risk: Highest

### Objective
Migrate the most complex module (IMS/DB2/MQ authorization processing) and decommission the mainframe.

### Components Migrated

| Component | Legacy | Target | Strategy |
|-----------|--------|--------|----------|
| Auth Request Processor | COPAUA0C (MQ trigger) | Kafka/SQS listener service | Rewrite |
| Auth Summary View | COPAUS0C | Authorization Service REST API | Rewrite |
| Auth Detail View | COPAUS1C | Authorization Service REST API | Rewrite |
| Fraud Marking | COPAUS2C (DB2) | Fraud Service (JPA) | Rewrite |
| Auth Purge | CBPAUP0C | Scheduled Spring Batch job | Rewrite |
| IMS HIDAM DB | DBPAUTP0/DBPAUTX0 | PostgreSQL tables | Schema redesign |

### IMS-to-Relational Schema Mapping

```
IMS HIDAM Hierarchy:
  PAUTSUM0 (root: Authorization Summary)
    |
    +-- PAUTDTL1 (child: Authorization Details)

PostgreSQL Tables:
  CREATE TABLE authorizations (
      authorization_id   BIGSERIAL PRIMARY KEY,
      card_number        VARCHAR(16) REFERENCES cards(card_number),
      auth_timestamp     TIMESTAMP NOT NULL,
      auth_type          VARCHAR(4),
      status             VARCHAR(2),      -- approved/declined
      approved_amount    DECIMAL(12,2),
      account_id         BIGINT REFERENCES accounts(account_id),
      customer_id        BIGINT REFERENCES customers(customer_id),
      created_at         TIMESTAMP DEFAULT NOW()
  );

  CREATE TABLE authorization_details (
      detail_id          BIGSERIAL PRIMARY KEY,
      authorization_id   BIGINT REFERENCES authorizations(authorization_id),
      merchant_id        VARCHAR(15),
      merchant_name      VARCHAR(22),
      merchant_city      VARCHAR(13),
      merchant_state     CHAR(2),
      merchant_zip       VARCHAR(9),
      transaction_amount DECIMAL(12,2),
      processing_code    VARCHAR(6),
      pos_entry_mode     SMALLINT,
      transaction_id     VARCHAR(15)
  );

  CREATE TABLE fraud_reports (
      card_number        VARCHAR(16),
      auth_timestamp     TIMESTAMP,
      fraud_reported_date DATE,
      -- all fields from DB2 AUTHFRDS table
      PRIMARY KEY (card_number, auth_timestamp)
  );
```

### Messaging Migration

```
Legacy:
  MQ Queue (CSV) -> CICS MQ Trigger -> COPAUA0C -> IMS DL/I + MQ Reply

Target:
  Kafka/SQS Topic (JSON) -> Spring Kafka Listener -> Auth Service
                          -> PostgreSQL + Kafka Reply Topic
```

### Sequence

```
Week 26-28: Authorization data migration
            - Map IMS HIDAM segments to PostgreSQL tables
            - Migrate historical authorization data
            - Migrate DB2 AUTHFRDS fraud data

Week 28-30: Authorization Service
            - Kafka/SQS listener (replaces MQ trigger)
            - Authorization decision logic
            - JPA persistence (replaces IMS DL/I calls)
            - Fraud marking (replaces COPAUS2C DB2 writes)

Week 30-32: Authorization UI & purge
            - Authorization summary/detail views
            - Expired authorization purge job (replaces CBPAUP0C)

Week 32-34: Integration testing
            - End-to-end authorization flow testing
            - Message format migration (CSV -> JSON)
            - Load testing with production volumes

Week 34-36: Mainframe decommission
            - Final data reconciliation
            - DNS/routing cutover
            - Legacy system read-only mode (30-day archive period)
            - Mainframe shutdown
```

### Entry Criteria
- Phase 5 batch processing stable
- All non-authorization components decommissioned from mainframe
- Message broker (Kafka/SQS) infrastructure ready

### Validation Gate
- [ ] Authorization response times meet SLA (< 500ms p99)
- [ ] Fraud marking persists correctly in PostgreSQL
- [ ] Purge job handles expired authorizations correctly
- [ ] Message format migration validated (CSV -> JSON)
- [ ] 30-day parallel run with zero discrepancies
- [ ] All legacy CICS transactions disabled with no business impact
- [ ] Zero open incidents related to migrated functionality

### Rollback
- Re-enable CICS transactions and MQ triggers
- Restore IMS database from backup
- Rollback time: hours to days (most complex rollback)

---

## Phase Summary Timeline

```
Month:    1    2    3    4    5    6    7    8    9    10   11   12
          |----|----|----|----|----|----|----|----|----|----|----|----|

Phase 0:  ████
Phase 1:  ░░████████
Phase 2:      ░░░░████████████
Phase 3:              ░░░░░░████████████████
Phase 4:                          ░░░░████████████
Phase 5:                              ░░░░░░████████████████
Phase 6:                                        ░░░░████████████████

████ = Active development and testing
░░░░ = Planning and preparation overlap
```

---

## Cutover Decision Matrix

Each phase cutover requires sign-off on the following criteria:

| Criterion | Phase 1 | Phase 2 | Phase 3 | Phase 4 | Phase 5 | Phase 6 |
|-----------|:---:|:---:|:---:|:---:|:---:|:---:|
| Unit test coverage > 80% | X | X | X | X | X | X |
| Integration tests pass | X | X | X | X | X | X |
| Performance within SLA | X | X | X | X | X | X |
| Reconciliation: zero diff | X | X | X | X | X | X |
| Parallel run: 1 week min | | X | X | X | X | X |
| Parallel run: 2 week min | | | | X | X | X |
| Rollback tested | X | X | X | X | X | X |
| Stakeholder sign-off | X | X | X | X | X | X |
| On-call runbook published | | X | X | X | X | X |
| Data backup verified | | | X | X | X | X |

---

## Decommission Checklist (Post-Phase 6)

- [ ] All CICS transactions disabled (CC00, CM00, CA00, etc.)
- [ ] All JCL batch jobs removed from scheduler (CA7/Control-M)
- [ ] VSAM files set to read-only, then archived
- [ ] IMS databases archived and taken offline
- [ ] DB2 tables archived (fraud data migrated)
- [ ] MQ queues drained and deleted
- [ ] Mainframe LPAR reclaimed or decommissioned
- [ ] Source code archived in version control (this repository)
- [ ] 90-day post-migration monitoring period completed
- [ ] Final cost comparison: mainframe MIPS vs. cloud compute
