# CardDemo Cutover Plan

## Guiding Principles

1. **Lowest-risk first**: Begin with the most decoupled, lowest-complexity components.
2. **Preserve the batch cycle**: The nightly batch pipeline (CLOSEFIL → ... → OPENFIL) is the riskiest component and is migrated last.
3. **Parallel-run everything**: Every phase includes a parallel-run period where legacy and modern systems operate simultaneously with output comparison.
4. **Rollback-ready**: Each phase has a defined rollback procedure that restores the previous state within the batch window.
5. **Data integrity over speed**: No phase completes until data reconciliation confirms zero discrepancies.

---

## Phase Overview

| Phase | Name | Duration | Risk | Bounded Contexts |
|-------|------|----------|------|-------------------|
| 0 | Foundation | 4-6 weeks | None | Infrastructure only |
| 1 | Identity & Access | 3-4 weeks | Low | BC-1 |
| 2 | Reporting | 3-4 weeks | Low | BC-7 |
| 3 | Card Management | 4-6 weeks | Medium | BC-3 |
| 4 | Transaction Inquiry (Online) | 4-6 weeks | Medium | BC-4 (read path) |
| 5 | Bill Payment | 3-4 weeks | Medium | BC-6 |
| 6 | Account Management | 8-10 weeks | High | BC-2 |
| 7 | Transaction Write Path & Optional Modules | 6-8 weeks | High | BC-4 (write), BC-8 |
| 8 | Batch Processing & Settlement | 10-12 weeks | Very High | BC-5 |
| 9 | Decommission | 4-6 weeks | Medium | All |

**Total estimated duration**: 50-66 weeks (12-16 months)

---

## Phase 0: Foundation (Weeks 1-6)

### Objective
Stand up target infrastructure, establish the data pipeline, and create the testing framework.

### Activities

| # | Activity | Details |
|---|----------|---------|
| 0.1 | Provision target environment | Java 17+, Spring Boot, PostgreSQL, Kafka/RabbitMQ, CI/CD pipeline |
| 0.2 | Design target database schema | Map all VSAM copybooks (`CVACT01Y`, `CVACT02Y`, `CVACT03Y`, `CVCUS01Y`, `CVTRA05Y`, `CSUSR01Y`) to relational tables |
| 0.3 | Build data migration tooling | ETL scripts to convert ASCII data files in `app/data/ASCII/` to SQL INSERT statements |
| 0.4 | Set up CDC pipeline | Change Data Capture from VSAM to PostgreSQL for parallel-run reconciliation |
| 0.5 | Create reconciliation framework | Automated comparison of VSAM record counts, checksums, and key business totals vs. PostgreSQL |
| 0.6 | Load test data | Seed PostgreSQL with data from `app/data/ASCII/` files |
| 0.7 | Establish strangler facade | API gateway that routes requests to either legacy (mainframe) or modern (Java) backends |

### Exit Criteria
- [ ] Target database schema created and validated against all copybook layouts
- [ ] ASCII test data loaded into PostgreSQL with zero conversion errors
- [ ] CDC pipeline operational with < 5 second replication lag
- [ ] Strangler facade routing confirmed with health-check endpoints
- [ ] CI/CD pipeline deploying to staging environment

### Rollback
Not applicable (no production traffic affected).

---

## Phase 1: Identity & Access Management (Weeks 5-8)

### Objective
Replace COSGN00C sign-on and COUSR00C-03C user management with a modern auth service.

### Programs Migrated
| Program | Function | LOC | Target |
|---------|----------|-----|--------|
| `COSGN00C` | Sign-on | 261 | Spring Security + JWT |
| `COUSR00C` | User list | 696 | REST API + React UI |
| `COUSR01C` | User add | ~400 | REST API + React UI |
| `COUSR02C` | User update | ~400 | REST API + React UI |
| `COUSR03C` | User delete | ~300 | REST API + React UI |

### Data Migration
| Source | Target | Notes |
|--------|--------|-------|
| USRSEC VSAM (80-byte records) | `users` table | Hash passwords (bcrypt), add audit columns, map `SEC-USR-TYPE` to roles table |

### Cutover Sequence
1. **Deploy** auth-service to production (no traffic).
2. **Migrate** USRSEC data to `users` table. Passwords must be reset (USRSEC stores plaintext).
3. **Parallel-run**: New web users authenticate via auth-service; 3270 users continue via COSGN00C.
4. **Validate**: Compare user counts, login success rates, role assignments.
5. **Cutover**: Route all authentication to auth-service. COSGN00C becomes a thin redirect.
6. **Decommission COSGN00C** (after Phase 9 full decommission).

### Rollback
- Re-enable COSGN00C as primary authenticator.
- Sync any new users created in PostgreSQL back to USRSEC.

### Risk: Password Reset
USRSEC stores passwords in plaintext (`SEC-USR-PWD PIC X(08)`). All users must receive new credentials when moving to bcrypt-hashed storage. Coordinate with business stakeholders for user communication.

---

## Phase 2: Reporting (Weeks 7-10)

### Objective
Replace TDQ-based report submission (CORPT00C) and batch report generation with modern reporting.

### Programs Migrated
| Program | Function | LOC | Target |
|---------|----------|-----|--------|
| `CORPT00C` | Report submission | 650 | REST API (date range → async job) |
| `CBTRN03C` | Transaction report | ~500 | JasperReports / SQL query |
| `CBSTM03A/B` | Statement generation | ~600 | PDF generation service |
| `CSUTLDTC` | Date validation utility | ~200 | `java.time` validation |

### Cutover Sequence
1. **Deploy** reporting-service reading from PostgreSQL (CDC-replicated data).
2. **Parallel-run**: Generate reports from both legacy (JCL batch) and modern systems. Compare outputs line-by-line.
3. **Validate**: 100% report output match for monthly and yearly reports across 3 billing cycles.
4. **Cutover**: Disable TDQ submission in CORPT00C; route to reporting-service.
5. **Remove** TRANREPT and CREASTMT JCL jobs from batch schedule.

### Rollback
- Re-enable TDQ submission path in CORPT00C.
- Restore TRANREPT/CREASTMT JCL jobs to batch schedule.

---

## Phase 3: Card Management (Weeks 9-14)

### Objective
Migrate card list/view/update (COCRDLIC, COCRDSLC, COCRDUPC) to a Card microservice.

### Programs Migrated
| Program | Function | LOC | Target |
|---------|----------|-----|--------|
| `COCRDLIC` | Card list + pagination | 1,460 | REST API (paginated) + React UI |
| `COCRDSLC` | Card detail view | ~500 | REST API + React UI |
| `COCRDUPC` | Card update | ~800 | REST API + React UI |

### Data Migration
| Source | Target | Notes |
|--------|--------|-------|
| CARDDAT VSAM (150-byte records) | `cards` table | Map `CARD-ACCT-ID` to FK, `CARD-ACTIVE-STATUS` to boolean |
| CARDAIX (alternate index) | SQL index on `cards.account_id` | Replaces VSAM alternate key path |
| CARDXREF (50-byte records) | FK relationships: `cards.account_id`, `cards.customer_id` | Transitional: keep CARDXREF synced until BC-2 and BC-5 are migrated |

### Cutover Sequence
1. **Deploy** card-service with read APIs backed by CDC-replicated `cards` table.
2. **Strangler route**: Web users → card-service; 3270 users → COCRDLIC/COCRDSLC/COCRDUPC.
3. **Parallel-run**: Compare card list output, pagination behavior, update results.
4. **Enable writes**: card-service writes to PostgreSQL + dual-write back to CARDDAT VSAM (for batch programs that still read VSAM).
5. **Validate**: Zero data discrepancy across 2-week period.
6. **Cutover**: All card operations through card-service. Maintain VSAM dual-write for batch consumers.

### Rollback
- Disable card-service write path.
- Restore COCRDLIC/COCRDSLC/COCRDUPC as primary.
- VSAM remains authoritative during rollback.

---

## Phase 4: Transaction Inquiry - Read Path (Weeks 13-18)

### Objective
Migrate transaction list/view (COTRN00C, COTRN01C) to a Transaction query service.

### Programs Migrated
| Program | Function | LOC | Target |
|---------|----------|-----|--------|
| `COTRN00C` | Transaction list + pagination | 700 | REST API (paginated) + React UI |
| `COTRN01C` | Transaction detail view | ~400 | REST API + React UI |

### Data Migration
| Source | Target | Notes |
|--------|--------|-------|
| TRANSACT VSAM (350-byte records) | `transactions` table | Partition by `TRAN-ORIG-TS` date; index on `TRAN-ID`, `TRAN-CARD-NUM` |

### Cutover Sequence
1. **Deploy** transaction-service (read-only) backed by CDC-replicated `transactions` table.
2. **Strangler route**: Web users → transaction-service; 3270 → COTRN00C/COTRN01C.
3. **Parallel-run**: Compare list outputs, pagination, detail views.
4. **Validate**: Transaction counts match, amounts reconcile to the penny.
5. **Cutover**: All read traffic to transaction-service.

### Rollback
- Restore routing to COTRN00C/COTRN01C.

**Note**: Write path (COTRN02C - transaction add) is deferred to Phase 7 due to dependency on account validation.

---

## Phase 5: Bill Payment (Weeks 17-20)

### Objective
Migrate bill payment (COBIL00C) to a modern payment orchestration service.

### Programs Migrated
| Program | Function | LOC | Target |
|---------|----------|-----|--------|
| `COBIL00C` | Bill payment | 573 | Payment service (saga orchestrator) |

### Dependencies
- **account-service** (Phase 6) is not yet live → payment-service reads ACCTDAT via CDC-replicated data and writes back via dual-write.
- **transaction-service** (Phase 4) is live for reads → payment-service creates transactions directly in PostgreSQL.
- **Transaction ID generation**: Replace READPREV + increment with database sequence.

### Cutover Sequence
1. **Deploy** payment-service with saga: read balance → confirm → create transaction → update balance.
2. **Parallel-run**: Process identical payments through both COBIL00C and payment-service. Compare resulting balances and transaction records.
3. **Validate**: Zero discrepancy in balance updates and transaction amounts over 4-week period.
4. **Cutover**: Route bill payment to payment-service. Dual-write to VSAM for batch consumers.

### Rollback
- Disable payment-service.
- Restore COBIL00C routing.

---

## Phase 6: Account Management (Weeks 19-28)

### Objective
Migrate account view/update (COACTVWC, COACTUPC) — the most complex component.

### Programs Migrated
| Program | Function | LOC | Target |
|---------|----------|-----|--------|
| `COACTVWC` | Account view | 942 | REST API + React UI |
| `COACTUPC` | Account update | 4,237 | REST API + React UI + Bean Validation |

### Critical Validation Rules to Preserve (from COACTUPC)
- SSN validation (3-part: area, group, serial; exclusions for 000, 666, 900-999)
- US phone number validation (area code + exchange + subscriber)
- Date validation (open date, expiration, reissue date, date of birth)
- Credit limit validation (signed decimal S9(10)V99)
- Cash credit limit validation
- Current balance validation
- FICO score validation
- Account status (Y/N active flag)
- Leap year handling for date validation

### Data Migration
| Source | Target | Notes |
|--------|--------|-------|
| ACCTDAT (300-byte records) | `accounts` table | Normalize `ACCT-GROUP-ID` to lookup table |
| CUSTDAT (500-byte records) | `customers` table | Normalize address fields, split phone numbers |

### Cutover Sequence
1. **Deploy** account-service with full CRUD APIs.
2. **Comprehensive validation testing**: Run every validation scenario from COACTUPC against the new service. Test matrix must cover all 88-level condition names.
3. **Strangler route**: Web → account-service; 3270 → COACTVWC/COACTUPC.
4. **Parallel-run**: Every account update through both paths. Compare field-by-field.
5. **Validate**: 100% match on all 16 account fields and all 19 customer fields. Zero validation false positives or false negatives.
6. **Cutover**: All account operations through account-service. Dual-write to ACCTDAT VSAM for batch consumers.
7. **Disable VSAM dual-write** only after Phase 8 (Batch) completes.

### Rollback
- Disable account-service write path.
- Restore COACTVWC/COACTUPC routing.
- Sync PostgreSQL changes back to VSAM.

---

## Phase 7: Transaction Write Path & Optional Modules (Weeks 27-34)

### Objective
Migrate transaction add (COTRN02C) and the optional integration modules.

### Programs Migrated
| Program | Function | LOC | Target |
|---------|----------|-----|--------|
| `COTRN02C` | Transaction add (online) | ~600 | REST API |
| `COTRTUPC` | Transaction type add/edit (DB2) | ~500 | JPA-based CRUD |
| `COTRTLIC` | Transaction type list/delete (DB2) | ~400 | JPA-based CRUD |
| `COBTUPDT` | Transaction type batch update (DB2) | ~300 | Spring Batch step |
| `COPAUA0C` + auth programs | MQ-triggered authorization | ~1,200 | Event-driven service (Kafka) |
| `CDRD/CODATE01`, `CDRA/COACCT01` | VSAM-MQ inquiry | ~400 | REST API |

### Cutover Sequence
1. **Deploy** transaction write endpoint in transaction-service.
2. **Deploy** transaction-type CRUD within account-service (or as separate admin service).
3. **Deploy** authorization-service replacing IMS/DB2/MQ with Kafka event processing.
4. **Parallel-run**: All write operations through both legacy and modern paths.
5. **Validate**: Transaction records match, authorization decisions match.
6. **Cutover**: Disable legacy write paths.

### Rollback
- Re-enable COTRN02C, DB2 programs, and MQ triggers.

---

## Phase 8: Batch Processing & Settlement (Weeks 33-44)

### Objective
Replace the entire JCL batch cycle with Spring Batch jobs. This is the highest-risk phase.

### Programs Migrated
| Program | Function | LOC | Target |
|---------|----------|-----|--------|
| `CBTRN02C` | Transaction posting | 732 | Spring Batch job: `PostTransactionsJob` |
| `CBACT04C` | Interest calculation | 653 | Spring Batch job: `InterestCalculationJob` |
| `CBSTM03A/B` | Statement generation | ~600 | Spring Batch job: `StatementGenerationJob` |
| `CBTRN01C` | Combine transactions | ~400 | Spring Batch job: `CombineTransactionsJob` |
| `CBACT01C-03C` | Account/card/customer utilities | ~600 | Spring Batch steps |
| `CBCUS01C` | Customer utility | ~200 | Spring Batch step |
| `CBEXPORT/CBIMPORT` | Data export/import | ~400 | Spring Batch jobs |

### Batch Cycle Mapping
| JCL Step | Spring Batch Job | Notes |
|----------|-----------------|-------|
| CLOSEFIL | Eliminated | No longer needed — modern DB handles concurrent access |
| ACCTFILE/CARDFILE/CUSTFILE/XREFFILE/TRANFILE | Eliminated | Data is already in PostgreSQL |
| POSTTRAN (`CBTRN02C`) | `PostTransactionsJob` | ItemReader: daily file/queue → ItemProcessor: validate (XREF lookup, account lookup) → ItemWriter: insert transaction + update account balance |
| INTCALC (`CBACT04C`) | `InterestCalculationJob` | Read category balances → lookup disclosure group rates → compute interest → update account → create interest transactions |
| TRANBKP | `BackupTransactionsJob` | Database backup/archive instead of VSAM copy |
| COMBTRAN | `CombineTransactionsJob` | Merge daily into master (may be simplified with single transaction table) |
| CREASTMT | `StatementGenerationJob` | Already migrated in Phase 2; verify integration |
| TRANIDX | Eliminated | Database indexes are maintained automatically |
| OPENFIL | Eliminated | No CICS file management needed |

### Cutover Sequence
1. **Build** all Spring Batch jobs with comprehensive unit and integration tests.
2. **Load** production-equivalent data volume into staging PostgreSQL.
3. **Parallel-run (critical)**: Run both JCL batch cycle and Spring Batch flow on the same day's data. Compare:
   - Transaction counts (posted vs. rejected)
   - Reject reasons (match `WS-VALIDATION-FAIL-REASON` codes)
   - Account balances after posting (to the penny)
   - Interest amounts calculated (to the penny)
   - Statement output (line-by-line)
4. **Extended parallel-run**: Minimum 4 complete billing cycles (4 months) with zero discrepancies.
5. **Cutover**: Disable JCL batch schedule. Spring Batch becomes the sole batch processor.
6. **Disable all VSAM dual-writes** from Phases 3-6.

### Rollback
- Re-enable JCL batch schedule.
- Re-enable VSAM dual-writes.
- Resync PostgreSQL from VSAM if needed.

### Performance Validation
| Metric | Legacy Target | Modern Target |
|--------|--------------|---------------|
| Daily posting throughput | Baseline | >= Legacy |
| Interest calculation time | Baseline | >= Legacy |
| Statement generation time | Baseline | >= Legacy |
| Batch window | Current window | <= 80% of current window |

---

## Phase 9: Decommission (Weeks 43-48)

### Objective
Remove all legacy mainframe dependencies.

### Activities
| # | Activity | Prerequisite |
|---|----------|-------------|
| 9.1 | Disable CICS transactions (CC00, CM00, CA00, etc.) | All online users on modern UI |
| 9.2 | Remove JCL batch schedule | Phase 8 complete |
| 9.3 | Decommission VSAM files | All dual-writes disabled |
| 9.4 | Remove CDC pipeline | No consumers reading VSAM |
| 9.5 | Decommission strangler facade | All routes pointing to modern services |
| 9.6 | Archive COBOL source | Retain for audit/reference |
| 9.7 | Terminate mainframe LPAR | Final step |

### Exit Criteria
- [ ] Zero transactions processed by legacy system for 30 consecutive days
- [ ] All BMS screens deactivated with no user complaints
- [ ] VSAM files archived to cold storage
- [ ] Mainframe costs at zero

---

## Parallel-Run Reconciliation Checkpoints

Every phase requires these reconciliation checks before cutover approval:

| Check | Method | Threshold |
|-------|--------|-----------|
| Record counts | COUNT(*) vs. VSAM LISTCAT | Exact match |
| Financial totals | SUM(balance) across all accounts | Zero difference |
| Transaction amounts | SUM(amount) by type and date | Zero difference |
| Reject counts | Daily reject counts comparison | Exact match |
| User access | Login success/failure rates | Within 1% |
| Response times | P95 latency comparison | Modern <= 120% of legacy |
| Data freshness | CDC lag measurement | < 5 seconds |

---

## Milestone Summary

```
Week  0         6        10        14        18        20        28        34        44        48
  |-----------|---------|---------|---------|---------|---------|---------|---------|---------|
  Phase 0     Phase 1   Phase 2   Phase 3   Phase 4   Phase 5   Phase 6   Phase 7   Phase 8   Phase 9
  Foundation  Auth      Reports   Cards     Txn Read  Payment   Accounts  Txn Write Batch     Decommission
  [NONE]      [LOW]     [LOW]     [MED]     [MED]     [MED]     [HIGH]    [HIGH]    [V.HIGH]  [MED]
```

---

## Go/No-Go Decision Gates

Each phase transition requires sign-off from:
1. **Technical Lead**: All automated tests pass, reconciliation at zero discrepancy
2. **Business Owner**: Business validation scenarios approved
3. **Operations**: Monitoring, alerting, and runbooks in place
4. **Security**: Penetration testing and access controls validated (especially Phase 1)
