# CardDemo Cutover Plan

## Overview

This document defines a phased migration sequence for the CardDemo mainframe application, ordered from **lowest risk to highest risk**. Each phase has defined entry criteria, exit criteria, rollback strategy, and estimated duration. The plan assumes parallel-run capability where the mainframe and Java systems coexist during transition.

---

## Migration Principles

1. **Lowest risk first** -- Start with isolated, low-complexity modules before tackling shared-data, high-complexity areas.
2. **Parallel run** -- Both mainframe and Java systems operate simultaneously during each phase. Traffic is gradually shifted.
3. **Data synchronization** -- During parallel run, a CDC (Change Data Capture) or dual-write mechanism keeps mainframe VSAM and PostgreSQL in sync.
4. **Rollback always available** -- Each phase can be rolled back by routing traffic back to the mainframe.
5. **No big bang** -- Never cut over the entire system at once. Each functional area migrates independently.
6. **Feature parity validation** -- Every migrated function must pass functional equivalence tests before traffic shift.

---

## Phase Summary

| Phase | Scope | Risk Level | Strategy | Duration (est.) |
|---|---|---|---|---|
| **Phase 0** | Infrastructure & Data Foundation | Minimal | Setup | 4-6 weeks |
| **Phase 1** | Authentication & User Admin | Low | Rewrite | 4-6 weeks |
| **Phase 2** | Account & Card Management (Read) | Low-Medium | Strangler (read path) | 6-8 weeks |
| **Phase 3** | Transaction Processing (Read) | Medium | Strangler (read path) | 4-6 weeks |
| **Phase 4** | Account & Card Management (Write) | Medium-High | Strangler (write path) | 8-10 weeks |
| **Phase 5** | Transaction Processing (Write) & Bill Payment | High | Strangler + Rewrite | 8-10 weeks |
| **Phase 6** | Batch Processing | High | Replatform + Rewrite | 10-12 weeks |
| **Phase 7** | Reporting & Statements | Medium | Rewrite | 6-8 weeks |
| **Phase 8** | Optional Integrations & Decommission | Low | Rewrite + Cleanup | 6-8 weeks |

**Total estimated duration:** 56-74 weeks (14-18 months)

---

## Phase 0: Infrastructure & Data Foundation

**Risk:** Minimal
**Duration:** 4-6 weeks

### Scope
- Provision target infrastructure (Kubernetes cluster, PostgreSQL, API gateway, CI/CD pipelines)
- Design and create PostgreSQL schema based on VSAM copybook analysis
- Build data migration tooling (VSAM -> PostgreSQL ETL pipeline)
- Execute initial data load using existing `CBEXPORT` batch program
- Validate data integrity (record counts, field checksums, referential integrity)
- Set up CDC mechanism for ongoing synchronization

### Database Schema (from copybook analysis)

```sql
-- From CSUSR01Y (USRSEC, 80 bytes)
CREATE TABLE users (
    user_id       VARCHAR(8) PRIMARY KEY,
    first_name    VARCHAR(20),
    last_name     VARCHAR(20),
    password_hash VARCHAR(255),  -- upgraded from PIC X(8) plaintext
    user_type     CHAR(1),       -- 'A' or 'U'
    email         VARCHAR(255),  -- new field
    created_at    TIMESTAMP DEFAULT NOW()
);

-- From CVACT01Y (ACCTDAT, 300 bytes)
CREATE TABLE accounts (
    account_id          BIGINT PRIMARY KEY,   -- from PIC 9(11)
    active_status       CHAR(1),
    current_balance     DECIMAL(12,2),        -- from PIC S9(10)V99
    credit_limit        DECIMAL(12,2),
    cash_credit_limit   DECIMAL(12,2),
    open_date           DATE,
    expiration_date     DATE,
    reissue_date        DATE,
    current_cycle_credit DECIMAL(12,2),
    current_cycle_debit  DECIMAL(12,2),
    address_zip         VARCHAR(10),
    group_id            VARCHAR(10)
);

-- From CVCUS01Y (CUSTDAT, 500 bytes)
CREATE TABLE customers (
    customer_id         BIGINT PRIMARY KEY,   -- from PIC 9(9)
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
    ssn                 VARCHAR(9),           -- encrypted at rest
    govt_issued_id      VARCHAR(20),
    date_of_birth       DATE,
    eft_account_id      VARCHAR(10),
    primary_cardholder  CHAR(1),
    fico_score          SMALLINT
);

-- From CVACT02Y (CARDDAT, 150 bytes)
CREATE TABLE cards (
    card_number         VARCHAR(16) PRIMARY KEY,
    account_id          BIGINT REFERENCES accounts(account_id),
    cvv_code            SMALLINT,
    embossed_name       VARCHAR(50),
    expiration_date     DATE,
    active_status       CHAR(1),
    customer_id         BIGINT REFERENCES customers(customer_id)
    -- replaces CCXREF: CVACT03Y cross-reference is now FKs here
);

-- From CVTRA05Y (TRANSACT, 350 bytes)
CREATE TABLE transactions (
    transaction_id      VARCHAR(16) PRIMARY KEY,
    type_code           CHAR(2),
    category_code       SMALLINT,
    source              VARCHAR(10),
    description         VARCHAR(100),
    amount              DECIMAL(11,2),        -- from PIC S9(9)V99
    merchant_id         BIGINT,
    merchant_name       VARCHAR(50),
    merchant_city       VARCHAR(50),
    merchant_zip        VARCHAR(10),
    card_number         VARCHAR(16) REFERENCES cards(card_number),
    origination_ts      TIMESTAMP,
    processing_ts       TIMESTAMP
);

-- From CVTRA01Y (TCATBAL, 50 bytes)
CREATE TABLE transaction_category_balances (
    account_id          BIGINT REFERENCES accounts(account_id),
    type_code           CHAR(2),
    category_code       SMALLINT,
    balance             DECIMAL(12,2),
    PRIMARY KEY (account_id, type_code, category_code)
);

-- From CVTRA02Y (DISCGRP, 50 bytes)
CREATE TABLE discount_groups (
    group_id            VARCHAR(10),
    transaction_type    CHAR(2),
    category_code       SMALLINT,
    interest_rate       DECIMAL(8,4),
    PRIMARY KEY (group_id, transaction_type, category_code)
);
```

### Entry Criteria
- Project approved and funded
- Target infrastructure budget allocated
- Development team onboarded with COBOL reading proficiency

### Exit Criteria
- [ ] PostgreSQL schema created and validated
- [ ] Initial data load complete with zero data loss
- [ ] Record counts match between VSAM and PostgreSQL
- [ ] CDC pipeline operational with < 5s latency
- [ ] CI/CD pipelines functional (build, test, deploy)
- [ ] API gateway configured with routing rules
- [ ] Monitoring and alerting operational (Prometheus/Grafana or equivalent)

### Rollback
Not applicable -- mainframe is untouched.

---

## Phase 1: Authentication & User Administration

**Risk:** Low
**Duration:** 4-6 weeks
**Strategy:** Rewrite

### Scope
- Implement `auth-service` (Spring Boot + Spring Security)
- Replace COSGN00C sign-on with JWT-based authentication
- Implement user CRUD (replaces COUSR00C-03C)
- Migrate USRSEC data to `users` table with password rehashing
- Deploy admin UI for user management (replaces BMS maps COSGN0A/B, COUSR0A/B)

### Programs Replaced

| COBOL Program | Java Replacement |
|---|---|
| `COSGN00C` | `AuthController.login()` + Spring Security |
| `COUSR00C` | `UserController.listUsers()` with Pageable |
| `COUSR01C` | `UserController.createUser()` |
| `COUSR02C` | `UserController.updateUser()` |
| `COUSR03C` | `UserController.deleteUser()` |

### Why Low Risk
- USRSEC file is isolated -- no other context writes to it
- Small codebase (~2,000 LOC)
- Authentication is the first thing users interact with, so early migration forces validation of the entire Java stack
- Failure only affects login, which has a clear rollback (revert to CICS sign-on)

### Entry Criteria
- Phase 0 complete
- `users` table populated from USRSEC VSAM
- API gateway configured for auth endpoints

### Exit Criteria
- [ ] Users can log in via Java auth-service and receive JWT
- [ ] Admin can list/add/update/delete users via REST API
- [ ] Passwords rehashed using bcrypt (migrated from plaintext)
- [ ] Role-based access control (ADMIN/USER) functional
- [ ] Response time < 200ms for login
- [ ] Functional equivalence tests pass for all COUSR00-03C flows
- [ ] 2 weeks of parallel-run with zero authentication failures

### Rollback
Route login traffic back to CICS COSGN00C. JWT tokens are self-contained, so no session state to migrate back.

---

## Phase 2: Account & Card Management (Read Path)

**Risk:** Low-Medium
**Duration:** 6-8 weeks
**Strategy:** Strangler Fig (read path only)

### Scope
- Implement `account-service` (Spring Boot + Spring Data JPA)
- Migrate read-only operations:
  - Account view (replaces `COACTVWC`)
  - Card list (replaces `COCRDLIC`)
  - Card detail view (replaces `COCRDSLC`)
- Deploy corresponding UI pages (replaces BMS maps)
- VSAM data continues to be the source of truth; CDC syncs to PostgreSQL

### Programs Replaced (Read Only)

| COBOL Program | Java Replacement |
|---|---|
| `COACTVWC` | `AccountController.getAccount()` |
| `COCRDLIC` | `CardController.listCards()` |
| `COCRDSLC` | `CardController.getCard()` |

### Why Low-Medium Risk
- Read-only operations cannot corrupt data
- VSAM remains the master; PostgreSQL is a read replica
- Account/card data is the most accessed data in the system -- high validation volume
- Medium risk comes from the multiple file reads per screen (ACCTDAT, CARDDAT, CUSTDAT, CCXREF) that must all be consistent

### Entry Criteria
- Phase 1 complete (JWT auth available)
- `accounts`, `cards`, `customers` tables populated
- CDC sync operational for these tables

### Exit Criteria
- [ ] Account detail page displays same data as CICS COACTVWC
- [ ] Card list paginated identically to COCRDLIC (page size, sort order)
- [ ] Card detail displays same fields as COCRDSLC
- [ ] Data consistency: PostgreSQL matches VSAM within CDC latency window
- [ ] Response time < 200ms for account/card reads
- [ ] 2 weeks of shadow-mode comparison (Java result vs. CICS result logged and compared)

### Rollback
Stop routing read traffic to Java; revert to CICS screens. No data impact.

---

## Phase 3: Transaction Processing (Read Path)

**Risk:** Medium
**Duration:** 4-6 weeks
**Strategy:** Strangler Fig (read path only)

### Scope
- Implement `transaction-service` (Spring Boot + Spring Data JPA)
- Migrate read-only operations:
  - Transaction list with pagination (replaces `COTRN00C`)
  - Transaction view (replaces `COTRN01C`)
- Deploy corresponding UI pages

### Programs Replaced (Read Only)

| COBOL Program | Java Replacement |
|---|---|
| `COTRN00C` | `TransactionController.listTransactions()` |
| `COTRN01C` | `TransactionController.getTransaction()` |

### Why Medium Risk
- Transaction data is high-volume and continuously updated by batch and online processes
- Pagination logic must exactly match COBOL STARTBR/READNEXT/ENDBR semantics
- Transaction timestamps include sub-second precision that must be preserved

### Entry Criteria
- Phase 2 complete
- `transactions` table populated with CDC sync
- Transaction list pagination logic verified against COBOL output

### Exit Criteria
- [ ] Transaction list matches COTRN00C output for same account/card filters
- [ ] Pagination behavior identical (PF7/PF8 equivalent)
- [ ] Transaction detail matches COTRN01C for all fields
- [ ] Performance: < 200ms for list queries (indexed properly)
- [ ] Shadow comparison: 100% field-level match for sampled transactions

### Rollback
Revert to CICS transaction list/view screens. No data impact.

---

## Phase 4: Account & Card Management (Write Path)

**Risk:** Medium-High
**Duration:** 8-10 weeks
**Strategy:** Strangler Fig (write path)

### Scope
- Enable write operations on `account-service`:
  - Account update (replaces `COACTUPC` -- 4,237 LOC, largest program)
  - Card update (replaces `COCRDUPC`)
- PostgreSQL becomes source of truth for accounts, cards, customers
- Reverse CDC: PostgreSQL -> VSAM for programs still on mainframe
- Migrate all field-level validation logic:
  - SSN format validation
  - Phone number format validation
  - Date range validation (open date, expiration, reissue)
  - Credit limit validation (positive amounts, reasonable ranges)
  - State code validation (valid US states)

### Programs Replaced

| COBOL Program | Java Replacement |
|---|---|
| `COACTUPC` | `AccountController.updateAccount()` + validation service |
| `COCRDUPC` | `CardController.updateCard()` + validation service |

### Why Medium-High Risk
- `COACTUPC` is the largest and most complex program (4,237 LOC)
- Write operations can corrupt data if validation logic is not perfectly replicated
- Reverse CDC (PostgreSQL -> VSAM) adds complexity and latency
- Multiple other programs still on mainframe read this data (bill payment, batch posting, interest calculation)
- Field-level validation differences could cause data quality issues

### Critical Validation Rules to Migrate (from COACTUPC analysis)

| Field | Validation | COBOL Location |
|---|---|---|
| Account Status | Must be 'Y' or 'N' | Working-Storage flags |
| Credit Limit | Must be positive, max 9999999999.99 | PIC S9(10)V99 range |
| Expiration Date | Must be future date, valid calendar date | CSUTLDTC subroutine call |
| SSN | 9 numeric digits | PIC 9(09) with NUMERIC check |
| Phone | Non-empty when provided | SPACES check |
| State Code | Valid 2-letter code | Table lookup |
| Zip Code | Valid format | Pattern check |

### Entry Criteria
- Phases 2-3 complete (read paths stable for 4+ weeks)
- Reverse CDC (PostgreSQL -> VSAM) tested and operational
- All validation rules documented and unit tested
- Integration test suite covering every validation scenario in COACTUPC

### Exit Criteria
- [ ] All 50+ validation scenarios from COACTUPC replicated in Java
- [ ] Account update produces identical VSAM record content (byte-level comparison)
- [ ] Card update produces identical VSAM record content
- [ ] Reverse CDC latency < 2 seconds
- [ ] Concurrent access tested (multiple users updating same account)
- [ ] 4 weeks parallel-run with dual-write verification
- [ ] Zero data discrepancy incidents in parallel-run period

### Rollback
- Stop routing write traffic to Java
- Reverse CDC stops; VSAM becomes master again
- Data reconciliation job ensures VSAM has latest state

---

## Phase 5: Transaction Write & Bill Payment

**Risk:** High
**Duration:** 8-10 weeks
**Strategy:** Strangler Fig (transaction add) + Rewrite (bill payment)

### Scope
- Enable transaction add on `transaction-service` (replaces `COTRN02C`)
- Implement `payment-service` (replaces `COBIL00C`)
- PostgreSQL becomes source of truth for transactions
- Transaction ID generation moves from sequential VSAM key to UUID or database sequence

### Programs Replaced

| COBOL Program | Java Replacement |
|---|---|
| `COTRN02C` | `TransactionController.createTransaction()` |
| `COBIL00C` | `PaymentController.processPayment()` |

### Why High Risk
- **Transaction ID generation change:** COBOL generates sequential IDs by reading the last VSAM record. Moving to UUID/sequence changes the ID format -- all consumers must be updated.
- **Bill payment is a multi-step transaction:** Read account balance -> validate sufficient funds -> create transaction -> update account balance. Must be atomic.
- **Cross-service orchestration:** Payment service calls both account-service and transaction-service. Saga pattern needed for distributed transaction.
- **Financial accuracy:** Amounts use PIC S9(9)V99 (COBOL packed decimal). Java `BigDecimal` must be used -- never `double`/`float`.

### Key Design Decisions

1. **Transaction ID format:** Migrate from sequential `PIC X(16)` to UUID. Provide a mapping table for the transition period.
2. **Payment atomicity:** Implement saga with compensation:
   - Step 1: Reserve account balance (account-service)
   - Step 2: Create transaction (transaction-service)
   - Step 3: Confirm balance deduction (account-service)
   - Compensation: If step 2 or 3 fails, release reservation
3. **Amount precision:** All monetary fields use `BigDecimal` with `HALF_EVEN` rounding (banker's rounding, matching COBOL ROUNDED).

### Entry Criteria
- Phase 4 complete (account write path stable for 4+ weeks)
- Saga pattern infrastructure tested
- Transaction ID migration plan approved
- Financial calculation tests validated against COBOL output

### Exit Criteria
- [ ] Transaction add produces records with all fields matching COTRN02C output
- [ ] Bill payment atomic: no partial payments on failure
- [ ] Balance calculations match COBOL to the penny (2 decimal places)
- [ ] Transaction ID mapping operational for backward compatibility
- [ ] Performance: < 500ms for bill payment end-to-end
- [ ] 4 weeks parallel-run with financial reconciliation
- [ ] Daily balance reconciliation report shows zero discrepancies

### Rollback
- Revert transaction add and bill payment to CICS
- Reconcile any transactions created in Java back to VSAM
- Transaction ID mapping table used to resolve any cross-references

---

## Phase 6: Batch Processing

**Risk:** High
**Duration:** 10-12 weeks
**Strategy:** Replatform (posting, interest) + Rewrite (statements)

### Scope
- Migrate daily batch cycle to Spring Batch:
  - Transaction posting (`CBTRN02C` -> Spring Batch job)
  - Interest calculation (`CBACT04C` -> Spring Batch job)
  - Transaction combine/backup (`CBTRN01C` -> Spring Batch job)
  - Data load jobs (`CBACT01-03C`, `CBCUS01C` -> Spring Batch jobs or eliminated)
- Rewrite statement generation (`CBSTM03A/B` -> PDF/HTML generator)
- Replace JCL orchestration with Spring Batch job orchestration
- Eliminate VSAM file open/close jobs (CLOSEFIL/OPENFIL -- not needed with database)
- Eliminate GDG management (replaced by database versioning / audit tables)

### Batch Cycle Migration

| JCL Step | Current | Target |
|---|---|---|
| CLOSEFIL | Close VSAM files for batch | **Eliminated** -- database supports concurrent access |
| Data refresh | Reload VSAM from flat files | **Eliminated** -- database is persistent |
| POSTTRAN | Post daily transactions | Spring Batch: `DailyPostingJob` |
| INTCALC | Calculate interest | Spring Batch: `InterestCalculationJob` |
| TRANBKP | Backup transactions | **Replaced** by database backup/pg_dump |
| COMBTRAN | Combine transaction files | **Eliminated** -- single transactions table |
| CREASTMT | Generate statements | Spring Batch: `StatementGenerationJob` |
| TRANIDX | Rebuild alternate indexes | **Eliminated** -- database indexes are automatic |
| OPENFIL | Reopen VSAM files for online | **Eliminated** |

### Why High Risk
- Batch processing touches all data stores (accounts, transactions, balances)
- Interest calculation must produce identical financial results
- Statement generation uses complex mainframe-specific patterns (PSA/TCB/TIOT, ALTER/GO TO, 2D arrays, CALL subroutine)
- Batch window timing is critical -- must complete within SLA
- Failure partway through batch can leave data inconsistent

### Entry Criteria
- Phases 4-5 complete (all online write paths on Java)
- VSAM -> PostgreSQL migration complete (PostgreSQL is source of truth)
- Spring Batch framework configured with job repository
- Interest calculation test suite validated against COBOL output for 1000+ accounts

### Exit Criteria
- [ ] Daily posting processes same transactions with identical results
- [ ] Interest calculation matches COBOL output to the penny for all accounts
- [ ] Statement PDF/HTML contains same data as COBOL print output
- [ ] Batch cycle completes within existing SLA window
- [ ] Job failure recovery: can restart from last checkpoint
- [ ] 4 weeks parallel batch run with daily reconciliation
- [ ] GDG and file management JCL jobs identified as safely eliminated

### Rollback
- Revert to JCL batch cycle
- Reconcile PostgreSQL state back to VSAM using export utility
- Resume CLOSEFIL/OPENFIL cycle

---

## Phase 7: Reporting

**Risk:** Medium
**Duration:** 6-8 weeks
**Strategy:** Rewrite

### Scope
- Replace CORPT00C online report submission with REST API
- Replace CBTRN03C batch report generation with Spring Batch + templating
- Generate PDF/HTML reports instead of print files
- Implement report scheduling (replaces JCL submission via internal reader)
- Add report download/email delivery capabilities

### Programs Replaced

| COBOL Program | Java Replacement |
|---|---|
| `CORPT00C` | `ReportController.submitReport()` |
| `CBTRN03C` | `TransactionReportJob` (Spring Batch) |

### Why Medium Risk
- Report generation is read-only (cannot corrupt data)
- Reports are asynchronous (users expect delay)
- Risk is primarily in format fidelity -- reports must contain the same data
- JCL internal reader submission pattern must be completely replaced

### Entry Criteria
- Phase 6 complete (batch framework operational)
- Report templates designed and approved
- Transaction data accessible via transaction-service or direct DB query

### Exit Criteria
- [ ] Monthly, yearly, and custom date range reports generate correctly
- [ ] Report data matches COBOL batch output line-for-line
- [ ] Report scheduling replaces JCL submission
- [ ] Reports available as downloadable PDF/HTML
- [ ] Report generation completes within 5 minutes for a full year of data

### Rollback
Revert to CORPT00C for report submission and CBTRN03C for generation. Reports are independent outputs with no data mutation.

---

## Phase 8: Optional Integrations & Decommission

**Risk:** Low (most work is cleanup)
**Duration:** 6-8 weeks
**Strategy:** Rewrite remaining modules + mainframe decommission

### Scope
- Migrate optional modules if needed:
  - IMS-DB2-MQ authorization -> Spring-based fraud detection / authorization rules engine
  - DB2 transaction type management -> JPA-based admin UI
  - VSAM-MQ account inquiry -> REST API (already implemented in account-service)
- Decommission mainframe:
  - Verify all traffic routed to Java
  - Archive VSAM data files
  - Retire CICS transaction definitions
  - Archive JCL libraries
  - Cancel mainframe resource allocations

### Entry Criteria
- Phases 1-7 complete
- All online and batch processing running on Java for 4+ weeks
- Zero traffic to mainframe CICS transactions
- Final data reconciliation complete

### Exit Criteria
- [ ] All optional modules migrated or explicitly deferred
- [ ] Mainframe CICS region shut down
- [ ] VSAM files archived (retained for 7 years per compliance)
- [ ] JCL libraries archived
- [ ] Mainframe MIPS allocation reduced/canceled
- [ ] Cost savings validated
- [ ] Post-migration support period defined (90 days)

### Rollback
At this point, rollback requires restoring the mainframe environment from archives. This should only be needed in catastrophic failure scenarios. Maintain archive access for 12 months post-decommission.

---

## Parallel-Run Architecture

During phases 1-7, both systems run simultaneously:

```
                    ┌─────────────────────┐
                    │    API Gateway /     │
                    │    Load Balancer     │
                    │  (traffic routing)   │
                    └──────────┬──────────┘
                               │
                 ┌─────────────┼─────────────┐
                 │                           │
          ┌──────▼──────┐            ┌───────▼───────┐
          │  Java        │            │  Mainframe     │
          │  Services    │            │  CICS/VSAM     │
          │              │            │                │
          │  PostgreSQL  │◄──── CDC ──│  VSAM files    │
          │              │── Reverse ─▶│                │
          └──────────────┘    CDC     └────────────────┘
```

### Traffic Routing Strategy
- **Shadow mode** (initial): 100% traffic to mainframe, Java receives a copy for comparison
- **Canary** (validation): 5% -> 10% -> 25% -> 50% traffic to Java
- **Full cutover**: 100% traffic to Java, mainframe available for fallback
- **Decommission**: Mainframe traffic stopped

---

## Key Milestones & Go/No-Go Checkpoints

| Milestone | Phase | Go/No-Go Criteria |
|---|---|---|
| Infrastructure Ready | 0 | All infrastructure provisioned, data loaded, CDC operational |
| First Java Login | 1 | Users can authenticate via Java with 99.9% success rate |
| Read Path Stable | 2-3 | 4 weeks of shadow comparison with < 0.01% discrepancy |
| Write Path Stable | 4 | 4 weeks of dual-write with zero data discrepancy |
| Financial Operations Live | 5 | Daily balance reconciliation at 100% for 4 weeks |
| Batch Cutover | 6 | 4 parallel batch runs with identical results |
| Full Java Operation | 7 | All functionality on Java, mainframe idle for 4 weeks |
| Mainframe Decommission | 8 | Business sign-off on cost savings and risk acceptance |
