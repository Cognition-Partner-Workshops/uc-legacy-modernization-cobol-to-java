# CardDemo Cutover Plan

## 1. Overview

This document defines a phased migration sequence for the CardDemo mainframe application, ordered from lowest-risk to highest-risk. Each phase has defined entry/exit criteria, rollback procedures, and validation checkpoints.

**Total estimated timeline:** 30–40 weeks across 7 phases (with parallelization).

---

## 2. Phase Summary

| Phase | Name | Risk | Duration | Parallel? | Strategy |
|-------|------|------|----------|-----------|----------|
| 0 | Foundation & Infrastructure | ★☆☆☆☆ | 3–4 weeks | — | Setup |
| 1 | Identity & Access Management | ★★☆☆☆ | 3–4 weeks | — | Rewrite |
| 2 | Read-Only Account & Card Views | ★★☆☆☆ | 4–5 weeks | — | Strangler Fig |
| 3 | Credit Card Management (Full CRUD) | ★★★☆☆ | 4–5 weeks | ∥ Phase 4 | Strangler Fig |
| 4 | Account Update & Reference Data | ★★★☆☆ | 5–6 weeks | ∥ Phase 3 | Strangler Fig |
| 5 | Transaction Processing & Batch | ★★★★☆ | 8–10 weeks | ∥ Phase 6 | Strangler + Replatform |
| 6 | Authorization & Fraud Module | ★★★★☆ | 6–8 weeks | ∥ Phase 5 | Rewrite |
| 7 | Billing, Reporting & Final Cutover | ★★★★★ | 4–6 weeks | — | Replatform |

---

## 3. Detailed Phase Descriptions

### Phase 0: Foundation & Infrastructure (Weeks 1–4)

**Objective:** Establish the target platform, CI/CD pipelines, observability, and data migration infrastructure before any functional migration begins.

**Deliverables:**

| # | Task | Owner | Duration |
|---|------|-------|----------|
| 0.1 | Provision target environment (AWS/Azure/GCP) — VPC, subnets, security groups | Platform | 1 week |
| 0.2 | Deploy PostgreSQL (Aurora) with schemas for each bounded context | Data | 1 week |
| 0.3 | Set up CI/CD pipeline (GitHub Actions / Jenkins) for Java microservices | DevOps | 1 week |
| 0.4 | Deploy API Gateway (Kong / AWS API Gateway) | Platform | 3 days |
| 0.5 | Set up observability stack — logging, metrics, tracing (ELK/Datadog/Grafana) | Platform | 1 week |
| 0.6 | Build EBCDIC → UTF-8 → PostgreSQL data migration pipeline | Data | 2 weeks |
| 0.7 | Validate data migration using ASCII sample data (`app/data/ASCII/`) as baseline | Data | 1 week |
| 0.8 | Set up CDC (Change Data Capture) infrastructure for VSAM ↔ PostgreSQL sync | Data | 1 week |
| 0.9 | Create integration test framework with contract tests for each service boundary | QA | 1 week |

**Entry criteria:** Project approved, team staffed, mainframe access available.

**Exit criteria:**
- [ ] Target environment provisioned and accessible
- [ ] PostgreSQL schemas created for all 6 bounded contexts
- [ ] Data migration pipeline successfully loads all 9 ASCII sample files
- [ ] CDC sync verified for at least one VSAM file (USRSEC)
- [ ] CI/CD pipeline deploys a hello-world Spring Boot service end-to-end

**Rollback:** N/A — no production changes in this phase.

---

### Phase 1: Identity & Access Management (Weeks 4–8)

**Objective:** Replace the USRSEC VSAM-based authentication with a modern JWT/OAuth2 auth service. This is the lowest-risk functional migration because:
- USRSEC is exclusively owned by IAM programs (no cross-domain writes)
- Only 6 COBOL programs involved (~2,330 LOC)
- No complex business logic — just CRUD + password validation
- Failure mode is clear: users can't log in (easy to detect and rollback)

**Migration Sequence:**

```
Step 1.1: Migrate USRSEC data → PostgreSQL users table
          ┌─────────────────────────────────────────────┐
          │ USRSEC (80 bytes)  →  users table           │
          │ SEC-USR-ID         →  user_id (VARCHAR 8)   │
          │ SEC-USR-FNAME      →  first_name (VARCHAR 20)│
          │ SEC-USR-LNAME      →  last_name (VARCHAR 20) │
          │ SEC-USR-PWD        →  password_hash (bcrypt) │
          │ SEC-USR-TYPE       →  role (ADMIN/USER)      │
          └─────────────────────────────────────────────┘

Step 1.2: Build Auth Service (Spring Boot + Spring Security)
          - POST /api/auth/login → returns JWT
          - POST /api/auth/register
          - GET/PUT/DELETE /api/users/{id}

Step 1.3: Dual-run period (2 weeks)
          - New auth service validates credentials
          - COBOL COSGN00C continues to validate against USRSEC
          - CDC keeps USRSEC and PostgreSQL in sync
          - Compare: login success/failure rates must match within 0.1%

Step 1.4: Cutover — redirect CC00 transaction to new auth service
          - CICS transaction CC00 intercepted at API Gateway
          - JWT token issued on successful login
          - COMMAREA USER-ID/USER-TYPE populated from JWT claims

Step 1.5: Decommission COSGN00C, COUSR00-03C
```

**Validation checkpoints:**
- [ ] All 2 seeded users (ADMIN001, USER0001) can authenticate via new service
- [ ] Admin user gets ADMIN role; regular user gets USER role
- [ ] Password hashes are bcrypt (not plaintext as in USRSEC)
- [ ] JWT tokens expire correctly; refresh mechanism works
- [ ] CICS programs downstream still receive correct user context

**Rollback procedure:**
1. Revert API Gateway routing to direct CICS CC00 transaction
2. USRSEC VSAM file is unchanged (CDC was one-way during dual-run)
3. Estimated rollback time: < 15 minutes

---

### Phase 2: Read-Only Account & Card Views (Weeks 8–13)

**Objective:** Expose Account View and Card View as read-only REST APIs, proving the Strangler Fig pattern on the simplest read paths before attempting writes.

**Migration Sequence:**

```
Step 2.1: Migrate ACCTDATA → PostgreSQL accounts table (300-byte records)
Step 2.2: Migrate CARDDATA → PostgreSQL cards table (150-byte records)
Step 2.3: Migrate CUSTDATA → PostgreSQL customers table (500-byte records)
Step 2.4: Migrate CARDXREF → PostgreSQL card_xref table (50-byte records)

Step 2.5: Build Account View Service
          - GET /api/accounts/{id} → replaces COACTVWC
          - GET /api/accounts/{id}/cards → card list via XREF join
          - GET /api/accounts/{id}/customer → customer details

Step 2.6: Build Card View Service
          - GET /api/cards?accountId={id} → replaces COCRDLIC (list)
          - GET /api/cards/{cardNum} → replaces COCRDSLC (detail)

Step 2.7: Deploy new React/Angular UI for account & card views

Step 2.8: Dual-run (2 weeks) — compare CICS screen output vs REST responses

Step 2.9: Cutover — route CAVW and CCLI/CCDL transactions to new UI
```

**Validation checkpoints:**
- [ ] Account view shows identical data to COACTVWC BMS screen for all test accounts
- [ ] Card list pagination matches COCRDLIC browse behavior
- [ ] Card detail matches COCRDSLC display fields
- [ ] Customer name resolution matches across both systems
- [ ] Response times ≤ 200ms (p99) for account/card queries

**Rollback procedure:**
1. Revert API Gateway routing to CICS transactions CAVW, CCLI, CCDL
2. VSAM files unchanged (read-only phase)
3. Estimated rollback time: < 10 minutes

---

### Phase 3: Credit Card Management — Full CRUD (Weeks 13–18)

*Can run in parallel with Phase 4.*

**Objective:** Add write operations for Credit Card domain — card update functionality.

**Migration Sequence:**

```
Step 3.1: Extend Card Service with write operations
          - PUT /api/cards/{cardNum} → replaces COCRDUPC
          - Validate: card status, expiration date, embossed name

Step 3.2: Implement dual-write pattern
          - New Card Service writes to PostgreSQL
          - CDC replicates to VSAM CARDDATA for legacy consumers

Step 3.3: Dual-run (2 weeks) — compare update outcomes

Step 3.4: Cutover — route CCUP transaction to new service

Step 3.5: Decommission COCRDUPC
```

**Validation checkpoints:**
- [ ] Card update persists identical data to COCRDUPC VSAM rewrite
- [ ] Card status transitions (Active ↔ Inactive) work correctly
- [ ] Expiration date validation matches COBOL edit logic
- [ ] CICS programs that read CARDDATA still see updates (via CDC)

**Rollback:** Revert routing; CDC ensures VSAM is the source of truth during rollback.

---

### Phase 4: Account Update & Reference Data (Weeks 13–19)

*Can run in parallel with Phase 3.*

**Objective:** Migrate the most complex single program — COACTUPC (4,236 LOC) — and the reference data tables (DISCGRP, TRANCATG, TRANTYPE, TCATBALF).

**Migration Sequence:**

```
Step 4.1: Extract and unit-test all 40+ field validation rules from COACTUPC
          - Phone number format (US: (XXX)XXX-XXXX)
          - Signed number validation (credit limits, balances)
          - Mandatory field checks
          - Yes/No flag validation
          - State code, ZIP code validation

Step 4.2: Build Account Update Service
          - PUT /api/accounts/{id} → replaces COACTUPC
          - Implement all validation rules as Bean Validation annotations + custom validators
          - Comprehensive test suite: 1 test per validation rule minimum

Step 4.3: Migrate reference data
          - DISCGRP → disclosure_groups table
          - TRANCATG → transaction_categories table
          - TRANTYPE → transaction_types table
          - TCATBALF → transaction_category_balances table

Step 4.4: Dual-write with CDC sync

Step 4.5: Parallel validation (3 weeks — extra time due to complexity)
          - Submit identical updates through CICS COACTUPC and REST API
          - Compare resulting VSAM records byte-for-byte

Step 4.6: Cutover — route CAUP transaction to new service
```

**Validation checkpoints:**
- [ ] All 40+ field validations produce identical accept/reject decisions
- [ ] Credit limit and balance calculations match to the penny (COMP-3 precision)
- [ ] Account group ID changes correctly cascade to DISCGRP lookups
- [ ] Error messages match COBOL originals for user familiarity

**Rollback:** Revert routing + restore VSAM from CDC snapshot. Extended rollback window (30 minutes) due to dual-write complexity.

---

### Phase 5: Transaction Processing & Batch (Weeks 18–28)

*Can run in parallel with Phase 6.*

**Objective:** Migrate the highest-risk functional area — online transaction entry and the daily batch posting pipeline.

**Sub-Phase 5A: Online Transaction Screens (Weeks 18–22)**

```
Step 5A.1: Build Transaction Service
           - GET /api/transactions?cardId={id}&page={n} → replaces COTRN00C
           - GET /api/transactions/{id} → replaces COTRN01C
           - POST /api/transactions → replaces COTRN02C

Step 5A.2: Implement transaction ID generation
           - Current: CICS READPREV to find last TRAN-ID, increment
           - Target: Database sequence or UUID-based generation

Step 5A.3: Dual-run for transaction add (3 weeks)
           - Critical: verify transaction IDs don't collide between systems
           - Verify card validation via Card Service matches XREF VSAM read

Step 5A.4: Cutover online transactions
```

**Sub-Phase 5B: Batch Transaction Posting (Weeks 22–28)**

```
Step 5B.1: Convert CBTRN02C (post-tran) to Spring Batch job
           - ItemReader: read DALYTRAN sequential file (or event stream)
           - ItemProcessor: validate against Card + Account services
           - ItemWriter: write to Transaction DB + call Account Service for balance updates
           - SkipPolicy: write rejects to dead-letter table (replaces DALYREJS)

Step 5B.2: Convert CBTRN03C (report) to Spring Batch job
           - Read from Transaction DB instead of VSAM

Step 5B.3: Replace Control-M DAILY-TransactionBackup workflow
           - Current: CLOSEFIL → TRANBKP → WAITSTEP → OPENFIL
           - Target: Spring Batch job with pre/post steps, no CICS file close needed

Step 5B.4: Replace MONTHLY-InterestCalculation workflow
           - Current: CLOSEFIL → INTCALC → COMBTRAN → WAITSTEP → OPENFIL
           - Target: CBACT04C → Spring Batch interest calculation job
           - COMBTRAN (SORT utility) → SQL merge query

Step 5B.5: Parallel batch run (4 weeks)
           - Run both COBOL and Spring Batch jobs nightly
           - Compare: account balances, transaction counts, reject counts
           - Tolerance: zero discrepancies for financial data

Step 5B.6: Cutover batch processing
```

**Validation checkpoints:**
- [ ] Transaction posting produces identical account balance updates (to the cent)
- [ ] Reject counts match between DALYREJS and dead-letter table
- [ ] Interest calculation matches CBACT04C output for all accounts
- [ ] COMBTRAN sort merge produces identical combined transaction files
- [ ] Control-M job dependencies preserved in new scheduler
- [ ] Batch window completion time ≤ mainframe batch window

**Rollback procedure:**
1. Stop Spring Batch jobs
2. Re-enable Control-M schedules
3. Restore VSAM files from GDG backup (previous generation)
4. Estimated rollback time: 1–2 hours (batch window required)

---

### Phase 6: Authorization & Fraud Module (Weeks 18–26)

*Can run in parallel with Phase 5. Independent data stores enable parallel development.*

**Objective:** Rewrite the IMS/DB2/MQ authorization subsystem as a standalone microservice.

**Migration Sequence:**

```
Step 6.1: Design relational schema to replace IMS DB segments
          - DBPAUTP0 segments → authorization_messages table
          - DBPAUTX0 index segments → database indexes
          - AUTHFRDS (DB2) → fraud_records table (direct migration)

Step 6.2: Build Authorization Service
          - POST /api/authorizations/process → replaces COPAUA0C (MQ trigger)
          - GET /api/authorizations/pending → replaces COPAUS0C (summary)
          - GET /api/authorizations/{id} → replaces COPAUS1C (detail)
          - POST /api/authorizations/{id}/fraud → replaces COPAUS2C (flag fraud)
          - DELETE /api/authorizations/expired → replaces CBPAUP0C (purge)

Step 6.3: Deploy MQ-to-Kafka/SQS bridge
          - Legacy MQ producers publish to existing queues
          - Bridge consumer republishes to event broker
          - New Authorization Service consumes from event broker

Step 6.4: Dual-run (3 weeks)
          - Both IMS-based and new service process same authorization requests
          - Compare decisions and fraud flags

Step 6.5: Cutover
          - Route MQ messages to new service
          - Redirect CICS transactions CPVS, CPVD to new UI

Step 6.6: Decommission IMS DB, MQ bridge
```

**Validation checkpoints:**
- [ ] Authorization decisions match for identical request messages
- [ ] Fraud flagging produces identical records in new DB vs. DB2 AUTHFRDS
- [ ] Purge job removes same set of expired authorizations
- [ ] MQ bridge introduces < 50ms additional latency

**Rollback:** Redirect MQ routing back to CICS COPAUA0C. IMS DB unmodified during dual-run.

---

### Phase 7: Billing, Reporting & Final Cutover (Weeks 28–34)

**Objective:** Complete the migration with billing, reporting, branch migration utilities, and final mainframe decommissioning.

**Migration Sequence:**

```
Step 7.1: Build Billing Service
          - POST /api/payments → replaces COBIL00C
          - Orchestrates: Account Service (debit) + Transaction Service (record)
          - Implements Saga pattern for distributed transaction

Step 7.2: Build Reporting Service
          - POST /api/reports/transactions → replaces CORPT00C
          - Generates reports asynchronously (replaces TDQ WRITEQ TD)
          - Converts CBSTM03A statement generation to PDF/CSV export

Step 7.3: Convert branch migration utilities
          - CBEXPORT → Spring Batch export job reading from PostgreSQL
          - CBIMPORT → Spring Batch import job writing to PostgreSQL
          - Preserve multi-record CVEXPORT layout for backward compatibility

Step 7.4: Decommission CICS region
          - Disable all CICS transactions
          - Verify no traffic reaching mainframe

Step 7.5: Decommission batch jobs
          - Disable Control-M schedules
          - Archive JCL and GDG datasets

Step 7.6: Final data reconciliation
          - Full comparison: PostgreSQL vs VSAM for all 9 data files
          - Financial audit: sum of all account balances must match

Step 7.7: Decommission VSAM files, DB2, IMS DB, MQ infrastructure
```

**Validation checkpoints:**
- [ ] Bill payment produces correct account balance and transaction record
- [ ] Saga compensates correctly on partial failure (account debited but transaction write fails)
- [ ] Transaction reports match CBTRN03C output format
- [ ] Statement generation matches CBSTM03A output
- [ ] Branch export/import produces byte-equivalent output for legacy consumers
- [ ] Full financial reconciliation passes: zero balance discrepancy across all accounts

**Rollback procedure:**
Phase 7 is the point of no return. If critical issues are discovered:
1. Re-enable CICS transactions (kept in cold standby for 90 days)
2. Restore VSAM from final backup
3. Restart Control-M schedules
4. Estimated rollback time: 4–6 hours

---

## 4. Cutover Timeline Visualization

```
Week:  1    4    8    13   18   22   26   28   34
       |    |    |    |    |    |    |    |    |
       ├────┤ Phase 0: Foundation
       |    ├────┤ Phase 1: IAM
       |    |    ├─────┤ Phase 2: Read-Only Views
       |    |    |     ├─────┤ Phase 3: Card CRUD ──────┐ (parallel)
       |    |    |     ├──────┤ Phase 4: Account Update ─┘
       |    |    |     |      ├──────────┤ Phase 5: Transactions ─┐ (parallel)
       |    |    |     |      ├────────┤ Phase 6: Auth & Fraud ──┘
       |    |    |     |      |        |         ├──────┤ Phase 7: Billing & Final
       |    |    |     |      |        |         |      |
       ▼    ▼    ▼     ▼      ▼        ▼         ▼      ▼
      Infra Auth  Views  CRUD  Write   Batch    Report  Done
```

---

## 5. Go/No-Go Decision Gates

Each phase transition requires sign-off from the migration steering committee:

| Gate | Criteria | Decision Maker |
|------|----------|---------------|
| G0 → G1 | Infrastructure provisioned, data migration pipeline validated | Tech Lead |
| G1 → G2 | Auth service handles 100% of logins for 7 days with zero errors | Product Owner + Security |
| G2 → G3/G4 | Read-only views serve 100% of traffic for 14 days | Product Owner |
| G3+G4 → G5 | All CRUD operations validated, CDC sync verified | Tech Lead + QA |
| G5 → G7 | Batch processing matches mainframe output for 30 consecutive days | Finance + Tech Lead |
| G6 → G7 | Authorization decisions match for 14 days | Risk + Compliance |
| G7 → Done | Full financial reconciliation passes, 90-day cold standby period | Executive Sponsor |

---

## 6. Staffing Model

| Role | Phase 0 | Phase 1 | Phase 2 | Phase 3–4 | Phase 5 | Phase 6 | Phase 7 |
|------|---------|---------|---------|-----------|---------|---------|---------|
| Platform/DevOps | 2 | 1 | 1 | 1 | 1 | 1 | 1 |
| Java Backend | — | 2 | 2 | 4 | 4 | 3 | 3 |
| Frontend (React/Angular) | — | 1 | 2 | 2 | 2 | 1 | 1 |
| Data Engineer | 2 | 1 | 1 | 1 | 2 | 1 | 2 |
| COBOL/Mainframe SME | 1 | 1 | 1 | 2 | 2 | 1 | 1 |
| QA/Test | 1 | 1 | 2 | 2 | 3 | 2 | 3 |
| **Total** | **6** | **6** | **9** | **12** | **14** | **9** | **11** |

Peak staffing: 14 engineers during Phase 5 (Transaction Processing).
