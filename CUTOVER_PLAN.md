# CardDemo Phased Cutover Plan

## Overview

This plan defines a phased migration sequence for the CardDemo mainframe application, ordered from **lowest-risk** to **highest-risk** functional areas. Each phase includes entry criteria, migration steps, validation gates, and rollback procedures. The plan assumes a **Strangler Fig** approach where the legacy CICS/VSAM system runs in parallel with the new Java/Spring Boot services until full cutover.

---

## Migration Architecture

```
Phase 1-2: Dual-Run with Shared Database
┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   3270 UI    │    │  API Gateway │    │   Web UI     │
│  (Legacy)    │    │  (New)       │    │  (New)       │
└──────┬───────┘    └──────┬───────┘    └──────┬───────┘
       │                   │                   │
       ▼                   ▼                   │
┌──────────────┐    ┌──────────────┐           │
│  CICS Region │    │ Spring Boot  │◄──────────┘
│  (Legacy)    │    │ Services     │
└──────┬───────┘    └──────┬───────┘
       │                   │
       ▼                   ▼
┌──────────────┐    ┌──────────────┐
│  VSAM Files  │◄──►│  PostgreSQL  │  ← Data sync layer
│  (Legacy)    │    │  (New)       │
└──────────────┘    └──────────────┘

Phase 3-5: Progressive Traffic Shift
┌──────────────┐    ┌──────────────┐
│  API Gateway │    │   Web UI     │
│  (Routes %)  │    │  (SPA)       │
└──────┬───────┘    └──────┬───────┘
       │                   │
       ▼                   ▼
┌──────────────┐    ┌──────────────┐
│  CICS Region │    │ Spring Boot  │
│  (Declining) │    │ Services     │
└──────┬───────┘    └──────┬───────┘
       │                   │
       ▼                   ▼
┌──────────────┐    ┌──────────────┐
│  VSAM Files  │    │  PostgreSQL  │  ← Source of truth shifts
│  (Read-only) │    │  (Primary)   │
└──────────────┘    └──────────────┘
```

---

## Phase 0: Foundation (Weeks 1–4)

**Risk Level:** None (no production traffic changes)
**Objective:** Establish infrastructure, CI/CD, and data migration pipeline

### 0.1 Infrastructure Setup
- [ ] Provision target environment (Kubernetes/ECS, PostgreSQL, message broker)
- [ ] Set up CI/CD pipeline (GitHub Actions / Jenkins) with build, test, deploy stages
- [ ] Configure API gateway (Kong / Spring Cloud Gateway)
- [ ] Establish monitoring and alerting (Prometheus, Grafana, ELK)
- [ ] Set up feature flags (LaunchDarkly / Spring Cloud Config)

### 0.2 Database Schema Creation
- [ ] Create relational schema from copybook analysis:

| VSAM File | Target Table | Source Copybook | Record Size |
|---|---|---|---|
| USRSEC | `users` | CSUSR01Y | 80 bytes |
| ACCTDAT | `accounts` | CVACT01Y | 300 bytes |
| CARDDAT | `cards` | CVACT02Y | 150 bytes |
| CUSTDAT | `customers` | CVCUS01Y | 500 bytes |
| TRANSACT | `transactions` | CVTRA05Y | 350 bytes |
| CARDXREF | `card_account_xref` | CVACT03Y | 50 bytes |
| TCATBAL | `tran_category_balances` | CVTRA01Y | 50 bytes |

- [ ] Create indexes matching VSAM alternate indexes (CARDAIX → `idx_cards_acct_id`, CXACAIX → `idx_xref_acct_id`)
- [ ] Validate schema against sample data files in `app/data/ASCII/`

### 0.3 Data Migration Pipeline
- [ ] Build ETL jobs to sync VSAM → PostgreSQL (initial load from `app/data/ASCII/` files)
- [ ] Implement change data capture (CDC) for ongoing sync during dual-run
- [ ] Validate data integrity: row counts, checksums, key relationships
- [ ] Test rollback: PostgreSQL → VSAM reverse sync capability

### 0.4 Shared Libraries
- [ ] Create Java DTOs/entities from copybook record layouts
- [ ] Build common validation utilities (date formatting, COMP-3 conversion, EBCDIC/ASCII)
- [ ] Implement COMMAREA-to-JWT mapping logic
- [ ] Create Spring Boot parent POM with shared dependencies

**Exit Criteria:** Database populated, CI/CD pipeline operational, data sync validated with <1s latency

---

## Phase 1: Read-Only Services (Weeks 5–8)

**Risk Level:** ★☆☆☆☆ (Lowest)
**Objective:** Deploy read-only APIs alongside legacy system. Zero impact on existing write paths.

### 1.1 Customer Service
**Source:** CUSTDAT (CVCUS01Y, 500 bytes, no dedicated COBOL programs)
**Rationale:** Lowest risk — no existing online programs, read-only access, no write contention

- [ ] Implement `customer-service` with GET /customers/{id} endpoint
- [ ] Map CVCUS01Y fields to JPA entity:
  ```
  CUST-ID (9) → customer_id BIGINT PK
  CUST-FIRST-NAME (25) → first_name VARCHAR(25)
  CUST-LAST-NAME (25) → last_name VARCHAR(25)
  CUST-SSN (9) → ssn_encrypted VARCHAR(256) -- encrypt at rest
  CUST-FICO-CREDIT-SCORE (3) → fico_score SMALLINT
  ... (all 22 fields)
  ```
- [ ] Add read-through cache (Redis) for frequently accessed customers
- [ ] Validate response parity: compare API output vs. COACTVWC screen data for 100 accounts

**Validation Gate:**
- API returns identical data to legacy CICS screen for all test accounts
- Response time < 100ms (p95)
- No impact on CICS region performance

**Rollback:** Decommission service; no production dependencies exist

### 1.2 Account View Service
**Source:** COACTVWC (942 lines), ACCTDAT (CVACT01Y, 300 bytes)
**Rationale:** Read-only view; no write contention with batch or other online programs

- [ ] Implement GET /accounts/{id} endpoint
- [ ] Implement GET /accounts/{id}/cards endpoint (replacing CXACAIX alternate index browse)
- [ ] Map CVACT01Y to JPA entity with proper numeric types:
  ```
  ACCT-CURR-BAL PIC S9(10)V99 → curr_balance DECIMAL(12,2)
  ACCT-CREDIT-LIMIT PIC S9(10)V99 → credit_limit DECIMAL(12,2)
  ```
- [ ] Validate against COACTVWC screen output for all accounts in test dataset

**Validation Gate:**
- 100% data parity with COACTVWC screen for all test accounts
- Cross-reference lookups (CARDXREF) return consistent results
- No degradation of CICS COACTVWC response times

**Rollback:** Remove API gateway route; legacy screen continues serving

### 1.3 Transaction Browse Service
**Source:** COTRN00C (700 lines), COTRN01C
**Rationale:** Read-only list/view; the STARTBR/READNEXT pagination pattern maps cleanly to SQL

- [ ] Implement GET /transactions?page=&size=&cardNum= endpoint
- [ ] Implement GET /transactions/{id} endpoint
- [ ] Replicate VSAM browse behavior: forward (READNEXT) and backward (READPREV) pagination
- [ ] Validate paginated results match COTRN00C screen output

**Validation Gate:**
- Pagination produces identical record ordering to VSAM KSDS key sequence
- Transaction detail matches COTRN01C field-by-field
- Performance: < 200ms for paginated list (p95)

**Rollback:** Remove API gateway route

**Phase 1 Exit Criteria:**
- All read-only APIs operational with ≥99.9% availability over 2 weeks
- Data sync latency < 5s from VSAM write to API visibility
- Zero errors in comparison testing (legacy vs. new API responses)
- Monitoring dashboards and alerts operational

---

## Phase 2: Authentication & User Administration (Weeks 9–12)

**Risk Level:** ★★☆☆☆ (Low)
**Objective:** Stand up new IAM system; begin routing authentication through new service

### 2.1 IAM Service
**Source:** COSGN00C (261 lines), USRSEC (CSUSR01Y, 80 bytes)
**Rationale:** Self-contained context with no business data dependencies. Must be migrated before write-path services.

- [ ] Implement `iam-service` with Spring Security:
  - POST /auth/login → validate credentials, return JWT
  - POST /auth/logout → invalidate token
  - GET /auth/me → return current user profile
- [ ] Migrate USRSEC data to `users` table:
  ```
  SEC-USR-ID (8) → username VARCHAR(8)
  SEC-USR-PWD (8) → password_hash VARCHAR(60) -- bcrypt
  SEC-USR-TYPE (1) → role ENUM('ADMIN','USER')
  SEC-USR-FNAME (20) → first_name VARCHAR(20)
  SEC-USR-LNAME (20) → last_name VARCHAR(20)
  ```
- [ ] Hash all existing passwords during migration (bcrypt)
- [ ] Configure JWT token issuance with role claims matching CDEMO-USER-TYPE values
- [ ] Set up dual authentication: new API gateway validates JWT; legacy CICS still reads USRSEC
- [ ] Implement bidirectional password sync: password changes in either system update both

**Validation Gate:**
- Login with all existing users (ADMIN001, USER0001, etc.) succeeds
- JWT tokens correctly encode role (A → ADMIN, U → USER)
- Password change on new system is reflected in USRSEC within 5s
- Legacy CICS login continues to work unmodified

### 2.2 User Administration Service
**Source:** COUSR00C-03C (List/Add/Update/Delete, ~696+ lines each)
**Rationale:** Admin-only functions with limited user base; low blast radius

- [ ] Implement user CRUD endpoints:
  - GET /users (paginated, replacing COUSR00C)
  - POST /users (replacing COUSR01C)
  - PUT /users/{id} (replacing COUSR02C)
  - DELETE /users/{id} (replacing COUSR03C)
- [ ] Add audit logging for all user changes
- [ ] Implement RBAC: only ADMIN role can access user management
- [ ] Test with admin user (ADMIN001): create, list, update, delete a user

**Validation Gate:**
- All CRUD operations produce identical USRSEC state changes
- Audit log captures all mutations
- New admin UI (or API) provides same functionality as COUSR00-03 screens
- Legacy admin screens still functional for CICS users

**Rollback:** Revert API gateway routing; disable dual-write; legacy auth resumes as sole system

**Phase 2 Exit Criteria:**
- All new API users authenticate via JWT
- Password sync bidirectional and verified
- Admin CRUD operations tested by admin users for 2 weeks
- No authentication failures in production

---

## Phase 3: Core Write Operations (Weeks 13–20)

**Risk Level:** ★★★☆☆ (Medium)
**Objective:** Migrate write paths for account, card, and transaction management. Begin dual-write.

### 3.1 Card Management Service
**Source:** COCRDLIC (1460 lines), COCRDSLC, COCRDUPC
**Rationale:** Card writes (COCRDUPC) affect only CARDDAT; lower coupling than account writes

- [ ] Implement card CRUD endpoints:
  - GET /cards?acctId=&page=&size= (replacing COCRDLIC paginated browse)
  - GET /cards/{cardNum} (replacing COCRDSLC)
  - PUT /cards/{cardNum} (replacing COCRDUPC)
- [ ] Implement dual-write: new service writes to PostgreSQL AND VSAM (via data sync)
- [ ] Validate: card update via API produces identical CARDDAT record to COCRDUPC
- [ ] Shift traffic: 10% → 25% → 50% → 100% over 4 weeks using feature flags

**Validation Gate:**
- Dual-write consistency: PostgreSQL and VSAM records match after every operation
- Card list pagination identical to COCRDLIC STARTBR/READNEXT ordering
- No data loss or corruption during traffic shift

### 3.2 Account Update Service
**Source:** COACTUPC (4237 lines — largest program)
**Rationale:** Medium-high risk due to extensive validation and concurrent batch writes

- [ ] Implement PUT /accounts/{id} with full validation:
  - Port all field validations from COACTUPC (dates, credit limits, status transitions, FICO range, SSN format, phone format)
  - Use Java Bean Validation annotations + custom validators
- [ ] Implement optimistic locking (version field) to handle concurrent batch updates
- [ ] Dual-write to PostgreSQL and VSAM
- [ ] Run shadow-mode comparison: execute both COACTUPC and new API, compare results for 2 weeks
- [ ] Gradual traffic shift: 10% → 25% → 50% → 100%

**Validation Gate:**
- Shadow-mode comparison shows 100% parity for 2 consecutive weeks
- All 4237 lines of validation logic covered by automated tests
- Concurrent batch updates (CBTRN02C, CBACT04C) do not cause data inconsistency
- Optimistic locking correctly handles contention

### 3.3 Transaction Add Service
**Source:** COTRN02C (online transaction add)
**Rationale:** Creates TRANSACT records; the ID generation pattern (HIGH-VALUES STARTBR) must be replaced

- [ ] Implement POST /transactions with:
  - Database sequence for transaction ID generation (replacing VSAM HIGH-VALUES trick)
  - Input validation matching COTRN02C field checks
  - Merchant information capture
- [ ] Dual-write to PostgreSQL and VSAM
- [ ] Validate: new transaction appears in both COTRN00C browse and new API list

**Validation Gate:**
- Transaction IDs are unique and sequential
- New transactions visible in legacy COTRN00C screen within data sync latency
- No duplicate transactions from dual-write

**Rollback:** Disable feature flag → traffic reverts to CICS; reverse-sync any PostgreSQL-only records to VSAM

**Phase 3 Exit Criteria:**
- All write operations dual-writing successfully
- 100% traffic on new services for card, account update, and transaction add
- Zero data inconsistencies between PostgreSQL and VSAM over 2-week validation period
- Performance within 20% of legacy response times

---

## Phase 4: Financial Operations (Weeks 21–28)

**Risk Level:** ★★★★☆ (High)
**Objective:** Migrate bill payment and batch transaction processing — the financial core

### 4.1 Bill Payment Service
**Source:** COBIL00C (573 lines)
**Rationale:** Financial transaction that updates account balance and creates a transaction record atomically

- [ ] Implement POST /payments with:
  - Two-step flow: POST /payments/preview (returns amount) → POST /payments/confirm
  - Atomic database transaction: create transaction + update account balance
  - Idempotency key to prevent duplicate payments
- [ ] Port COBIL00C logic:
  ```
  1. Look up account via CXACAIX (card → account cross-reference)
  2. Read account balance (ACCTDAT)
  3. Confirm payment amount (full balance)
  4. Create transaction record (TRANSACT)
  5. Update account balance to zero (ACCTDAT REWRITE)
  ```
- [ ] Implement compensating transaction for failures (reverse payment if balance update fails)
- [ ] Run shadow-mode: compare bill payment results between legacy and new for 2 weeks
- [ ] Gradual rollout: 5% → 10% → 25% → 50% → 100%

**Validation Gate:**
- Account balances are correct after payment (balance reduced by payment amount)
- Transaction record created with correct amount, timestamp, and type code
- No double payments from dual-write or retry scenarios
- Compensating transactions work correctly for failure cases

### 4.2 Batch Transaction Posting
**Source:** CBTRN02C (732 lines), JCL POSTTRAN
**Rationale:** Highest-risk batch operation — reads daily transactions, validates, posts, rejects, updates balances

- [ ] Implement as Spring Batch job with steps:
  ```
  Step 1: Read daily transactions (DALYTRAN → daily_transactions table)
  Step 2: Validate each transaction:
    - Card exists in CARDXREF (card_account_xref table)
    - Account is active (accounts table)
    - Amount within limits
  Step 3: Post valid transactions (→ transactions table)
  Step 4: Write rejected transactions (→ rejected_transactions table)
  Step 5: Update transaction category balances (→ tran_category_balances table)
  ```
- [ ] Preserve batch cycle ordering: posting must complete before interest calculation
- [ ] Implement restart/recovery: Spring Batch checkpointing for interrupted jobs
- [ ] Run parallel: execute both legacy JCL and new Spring Batch; compare results
- [ ] Validate: identical records in TRANSACT vs. transactions, identical rejects in DALYREJS vs. rejected_transactions

**Validation Gate:**
- Parallel execution produces identical results for 4 consecutive batch cycles
- Restart after mid-batch failure produces correct results
- Processing time within 150% of legacy batch window

### 4.3 Interest Calculation Batch
**Source:** CBACT04C (653 lines), JCL INTCALC
**Rationale:** Computes interest per account using discount group rates; updates account balances

- [ ] Implement as Spring Batch job:
  ```
  Step 1: Read transaction category balances by account
  Step 2: Look up discount group and interest rate
  Step 3: Compute interest for each account
  Step 4: Create interest transaction records
  Step 5: Update account balances
  ```
- [ ] Validate interest calculation precision: COMP-3 / PIC S9(09)V99 → BigDecimal(12,2)
- [ ] Run parallel with legacy INTCALC for 4 batch cycles
- [ ] Compare: account balances and interest transaction amounts

**Validation Gate:**
- Interest amounts match to the penny (COMP-3 precision preserved)
- Account balance updates match legacy exactly
- Batch completes within the scheduled window

**Rollback:** Re-enable legacy JCL jobs; reverse any PostgreSQL-only batch updates via compensating entries

**Phase 4 Exit Criteria:**
- Bill payment processing 100% on new service for 4 weeks
- Batch posting and interest calculation running exclusively on Spring Batch for 2 consecutive cycles
- Zero financial discrepancies
- Batch window met consistently

---

## Phase 5: Reporting & Decommission (Weeks 29–36)

**Risk Level:** ★★★★★ (Highest)
**Objective:** Migrate reporting/statements (most complex code), decommission legacy

### 5.1 Reporting Service
**Source:** CORPT00C (650 lines), CBSTM03A (924 lines), CBSTM03B (230 lines), CBTRN03C
**Rationale:** CBSTM03A contains non-portable constructs (ALTER/GO TO, PSA/TCB/TIOT control blocks). Complete rewrite required.

- [ ] Implement report generation service:
  - POST /reports/transactions — replaces CORPT00C's TDQ-to-JES flow
  - POST /reports/statements — replaces CBSTM03A/B batch job
- [ ] Rewrite statement generation:
  - Use CBSTM03A's layout (lines 86-224) as specification for HTML/PDF template
  - Replace 2D array logic (WS-TRNX-TABLE: 51 cards × 10 transactions) with SQL query grouping
  - Replace CBSTM03B subroutine file I/O with JPA repository calls
  - Replace ALTER/GO TO flow control with standard Java control structures
  - Replace PSA/TCB/TIOT control block addressing with standard Java APIs
- [ ] Generate statements in PDF (replacing plain text) and HTML (preserving existing format)
- [ ] Validate: compare generated statements field-by-field against legacy output
- [ ] Run parallel generation for 2 billing cycles

**Validation Gate:**
- Statement content matches legacy output (amounts, dates, customer info, transaction details)
- All accounts receive statements (no missing accounts)
- PDF/HTML formatting matches business requirements
- Generation completes within batch window

### 5.2 Remaining Batch Jobs
**Source:** COMBTRAN, TRANBKP, TRANIDX, CBEXPORT, CBIMPORT, file refresh JCL
**Rationale:** Infrastructure jobs that become unnecessary or simplified with relational database

- [ ] COMBTRAN (combine transactions) → SQL query/view
- [ ] TRANBKP (transaction backup) → database backup job (pg_dump or logical replication)
- [ ] TRANIDX (alternate index rebuild) → handled automatically by database indexes
- [ ] CBEXPORT/CBIMPORT → Spring Batch ETL jobs
- [ ] CLOSEFIL/OPENFIL → eliminated (database handles concurrent access)
- [ ] Data refresh JCL (ACCTFILE, CARDFILE, etc.) → database seed/migration scripts

### 5.3 Legacy Decommission
- [ ] Verify all traffic routed to new services (zero CICS transactions for 30 days)
- [ ] Disable VSAM-to-PostgreSQL sync (PostgreSQL is now sole source of truth)
- [ ] Archive VSAM files and CICS resources
- [ ] Decommission CICS region
- [ ] Remove CLOSEFIL/OPENFIL scheduling (no longer needed)
- [ ] Archive JCL, COBOL source, and BMS maps

**Phase 5 Exit Criteria:**
- All reports and statements generated by new service
- Zero CICS transactions for 30 consecutive days
- VSAM files archived
- CICS region decommissioned
- Full application running on Java/Spring Boot + PostgreSQL

---

## Cutover Risk Summary by Phase

| Phase | Risk Level | Duration | Key Risk | Mitigation |
|---|---|---|---|---|
| **0: Foundation** | None | 4 weeks | Data migration errors | Automated validation, checksums |
| **1: Read-Only** | ★☆☆☆☆ | 4 weeks | Data staleness | CDC with <5s latency |
| **2: Auth & Admin** | ★★☆☆☆ | 4 weeks | Auth failures | Dual auth, bidirectional password sync |
| **3: Write Ops** | ★★★☆☆ | 8 weeks | Dual-write inconsistency | Shadow mode, feature flags, gradual rollout |
| **4: Financial** | ★★★★☆ | 8 weeks | Financial discrepancies | Parallel batch runs, penny-level validation |
| **5: Reports & Decom** | ★★★★★ | 8 weeks | CBSTM03A rewrite errors | Parallel output comparison, phased rollout |

**Total Timeline:** ~36 weeks (9 months)

---

## Rollback Strategy

Each phase has an independent rollback that does not affect earlier phases:

| Phase | Rollback Action | Recovery Time |
|---|---|---|
| 1 | Remove API gateway routes | < 5 minutes |
| 2 | Disable JWT validation, revert to USRSEC-only auth | < 15 minutes |
| 3 | Feature flag disable → traffic reverts to CICS; reverse-sync PostgreSQL-only records | < 30 minutes |
| 4 | Re-enable legacy JCL batch jobs; reverse financial entries | < 1 hour |
| 5 | Re-enable CICS region from archived state | < 4 hours |

---

## Success Metrics

| Metric | Target | Measurement |
|---|---|---|
| Data Parity | 100% match between legacy and new | Automated comparison suite |
| Response Time | Within 20% of legacy | APM monitoring (p50, p95, p99) |
| Availability | ≥99.9% per service | Uptime monitoring |
| Batch Window | Within 150% of legacy duration | Job execution metrics |
| Financial Accuracy | Zero discrepancies | Automated reconciliation |
| Defect Rate | <1 P1 bug per phase | Bug tracking |
| Rollback Time | <30 minutes (phases 1-3) | Runbook testing |
