# CardDemo Cutover Plan

> **Generated:** 2026-03-25 | **Scope:** Phased migration sequence from lowest-risk to highest-risk

---

## 1. Executive Summary

This plan defines a **5-phase migration** from the CardDemo COBOL/CICS/VSAM system to Java/Spring Boot, ordered from lowest risk to highest risk. Each phase includes scope, duration estimate, team sizing, success criteria, rollback strategy, and data migration approach.

**Total estimated duration:** 9–14 months (excluding optional modules)
**Parallel run period:** Minimum 2 billing cycles per phase before cutover

### Migration Sequence Overview

```
Phase 1          Phase 2          Phase 3          Phase 4          Phase 5
Foundation       Read-Only        Write            Batch            Admin &
& Auth           Screens          Operations       Processing       Utilities
─────────────►  ─────────────►  ─────────────►  ─────────────►  ─────────────►
Weeks 1-6        Weeks 7-14       Weeks 15-26      Weeks 27-40      Weeks 41-52

Risk: LOW        Risk: LOW-MED    Risk: MEDIUM     Risk: HIGH       Risk: LOW
LOC: 2,623       LOC: 3,929      LOC: 6,267       LOC: 3,530       LOC: 2,836
Programs: 7      Programs: 4      Programs: 5      Programs: 5      Programs: 8+
```

---

## 2. Pre-Migration Foundation (Week 0)

Before Phase 1 begins, establish the infrastructure and tooling foundation.

### 2.1 Infrastructure Setup

| Task | Detail | Duration |
|------|--------|----------|
| Database provisioning | PostgreSQL instance with schemas: `identity`, `account`, `card`, `transaction`, `financial`, `reporting` | 2 days |
| Spring Boot scaffold | Multi-module Maven/Gradle project with shared kernel | 3 days |
| CI/CD pipeline | Build → test → deploy pipeline (GitHub Actions / Jenkins) | 2 days |
| 3270 bridge | Terminal emulator gateway for legacy access during parallel run (e.g., IBM Host Access Client) | 3 days |
| Monitoring | Prometheus + Grafana dashboards for both legacy and new systems | 2 days |
| Data sync tooling | Change Data Capture (CDC) pipeline: VSAM → PostgreSQL (for parallel run) | 5 days |

### 2.2 Test Infrastructure

| Task | Detail |
|------|--------|
| Regression test suite | Capture current COBOL behavior as golden-master tests (input → expected output) |
| Performance baseline | Record current transaction throughput, batch completion times, response latencies |
| Reconciliation framework | Automated comparison tool: COBOL output vs. Java output for identical inputs |

### 2.3 Entry Criteria for Phase 1

- [ ] PostgreSQL provisioned and accessible
- [ ] Spring Boot project compiles and deploys to staging
- [ ] CDC pipeline proven with USRSEC VSAM → `users` table sync
- [ ] Golden-master regression tests captured for all online screens
- [ ] Monitoring dashboards live for both legacy and new systems
- [ ] Rollback runbook documented and rehearsed

---

## 3. Phase 1 — Foundation & Authentication (Weeks 1–6)

### 3.1 Scope

| Component | Strategy | LOC | Programs |
|-----------|----------|-----|----------|
| Security & Auth | Rewrite | 260 | COSGN00C |
| Navigation/Menu | Strangler Fig | 596 | COMEN01C, COADM01C |
| Shared Utilities | Rewrite | 1,516 | CSUTLDTC, COBSWAIT + copybooks |
| Date Utility | Rewrite | 157 | CSUTLDTC |
| Lookup Codes | Rewrite | 1,318 | CSLKPCDY (copybook) |

**Total:** 2,623 LOC across 7 programs/copybooks

### 3.2 Rationale — Why First?

1. **Authentication is a prerequisite** — every other module depends on user identity
2. **Navigation is the strangler facade** — it's the entry point that routes to all other modules
3. **Shared utilities are consumed everywhere** — `java.time`, `StringUtils`, and enum lookups must exist before any module can be migrated
4. **Risk is minimal** — these are the simplest programs (COSGN00C: 260 LOC) with clear replacements

### 3.3 Implementation Steps

| Week | Tasks |
|------|-------|
| 1 | Implement Spring Security with JWT. Create `UserDetailsService`, `AuthController`, BCrypt password encoding. Migrate USRSEC data to `users` table. |
| 2 | Build web UI shell (main menu + admin menu). Implement strangler routing: each menu option routes to either new Java endpoint or 3270 bridge URL. |
| 3 | Rewrite shared utilities: `java.time` for CSUTLDTC, `enum` types for CSLKPCDY lookup codes, `StringUtils` for CSSTRPFY. Package as shared library module. |
| 4 | Integration testing: login → menu → 3270 bridge for all unmigrated screens. Verify JWT propagation. |
| 5 | Parallel run: both CICS sign-on and new web login active. Users can access either. Compare auth success/failure rates. |
| 6 | UAT and cutover gate. |

### 3.4 Data Migration

| Source | Target | Method | Volume |
|--------|--------|--------|--------|
| USRSEC.VSAM.KSDS | `identity.users` table | One-time ETL + CDC sync | ~100 records |

**Steps:**
1. Export USRSEC records via CBEXPORT or IDCAMS REPRO
2. Transform: hash all passwords with BCrypt (plaintext → hashed)
3. Load into PostgreSQL `users` table
4. Verify: all users can authenticate via new system
5. During parallel run: CDC syncs new user creations from VSAM to PostgreSQL

### 3.5 Success Criteria

- [ ] All existing users can log in via web UI with correct role assignment
- [ ] Menu shell displays all options; unmigrated options route to 3270 bridge successfully
- [ ] Shared utility unit tests pass with 100% parity to COBOL behavior
- [ ] Response time ≤ 200ms for authentication (P95)
- [ ] Zero authentication failures that succeed in legacy (parity test)

### 3.6 Rollback Plan

**Trigger:** Authentication failure rate > 1% OR menu routing failure to legacy screens.
**Action:**
1. DNS/load balancer switch: route all traffic back to CICS region
2. CICS sign-on screen becomes primary entry point again
3. No data rollback needed — USRSEC VSAM was never modified

**Recovery time:** < 5 minutes (DNS TTL or load balancer switch)

---

## 4. Phase 2 — Read-Only Screens (Weeks 7–14)

### 4.1 Scope

| Component | Strategy | LOC | Programs |
|-----------|----------|-----|----------|
| Account View | Refactor | 941 | COACTVWC |
| Card List | Strangler Fig | 1,459 | COCRDLIC |
| Card View | Strangler Fig | 887 | COCRDSLC |
| Transaction List | Refactor | 699 | COTRN00C |

**Total:** 3,929 LOC across 4 programs (all read-only)

### 4.2 Rationale — Why Second?

1. **Read-only operations carry no data corruption risk** — if the Java version returns wrong data, no harm done to the system of record
2. **Validates the data migration** — if views display correctly, the data sync is working
3. **High user visibility** — these are the most-used screens, building confidence in the new system
4. **No dual-write complexity** — VSAM remains the system of record; Java reads from PostgreSQL replica

### 4.3 Implementation Steps

| Week | Tasks |
|------|-------|
| 7 | Implement Account View: Spring MVC controller + JPA `AccountRepository`. Replace VSAM READ with `findById()`. Map all COACTVWC display fields to HTML template. |
| 8 | Implement Card List: Replace VSAM STARTBR/READNEXT/READPREV with JPA `Pageable`. Implement card number masking in the Java layer. |
| 9 | Implement Card View + Transaction List: Card detail page with `findByCardNumber()`. Transaction list with paginated query. |
| 10 | Data sync validation: Compare every screen field between COBOL BMS output and Java HTML output for 100 sample accounts. |
| 11–12 | Parallel run: A/B testing. 10% of users get Java screens, 90% get CICS. Compare response times and accuracy. |
| 13–14 | Ramp to 50%, then 100%. UAT gate. |

### 4.4 Data Migration

| Source | Target | Method | Volume |
|--------|--------|--------|--------|
| ACCTDATA.VSAM.KSDS | `account.accounts` table | Initial ETL + continuous CDC | ~10,000 records |
| CARDDATA.VSAM.KSDS | `card.cards` table | Initial ETL + continuous CDC | ~50,000 records |
| CARDXREF.VSAM.KSDS | `card.card_xref` table | Initial ETL + continuous CDC | ~50,000 records |
| TRANSACT.VSAM.KSDS | `transaction.transactions` table | Initial ETL + continuous CDC | ~500,000 records |

**Steps:**
1. Run CBEXPORT for initial bulk load of all 4 VSAM files
2. Transform EBCDIC → UTF-8, COMP-3 → BigDecimal, packed dates → ISO dates
3. Load into PostgreSQL with foreign key validation
4. Start CDC pipeline: VSAM changes → PostgreSQL within 30 seconds
5. Reconciliation: hourly count/sum checks between VSAM and PostgreSQL

### 4.5 Success Criteria

- [ ] 100% field-level parity between COBOL screens and Java pages for 1,000 sample records
- [ ] Page load time ≤ 500ms (P95) for list screens with pagination
- [ ] Zero data discrepancies between VSAM and PostgreSQL during parallel run
- [ ] Card numbers properly masked (show last 4 digits only)
- [ ] User satisfaction survey: ≥ 80% prefer new UI

### 4.6 Rollback Plan

**Trigger:** Field-level data mismatch rate > 0.1% OR page load time > 2s (P95).
**Action:**
1. Update strangler menu routing to direct read-only screens back to 3270 bridge
2. No data rollback needed — reads don't modify data
3. CDC pipeline continues running (no disruption to sync)

**Recovery time:** < 2 minutes (routing config change)

---

## 5. Phase 3 — Write Operations (Weeks 15–26)

### 5.1 Scope

| Component | Strategy | LOC | Programs | Hotspot Score |
|-----------|----------|-----|----------|---------------|
| Account Update | Strangler Fig | 4,236 | COACTUPC | 4.70 (#1) |
| Card Update | Strangler Fig | 1,560 | COCRDUPC | 4.15 (#3) |
| Transaction Add | Refactor | 783 | COTRN02C | 3.50 (#9) |
| Transaction View | Refactor | 330 | COTRN01C | — |
| Bill Payment | Refactor | 572 | COBIL00C | 3.85 (#7) |

**Total:** 6,267 LOC across 5 programs (includes top 3 hotspots)

### 5.2 Rationale — Why Third?

1. **Write operations are the highest-risk online change** — data corruption is possible
2. **Read-only screens (Phase 2) validated the data model** — if reads are correct, writes have a solid foundation
3. **COACTUPC is the #1 hotspot** — 4,236 LOC with 167 IF statements. It needs the most careful migration.
4. **Bill payment is the most transactionally complex online operation** — dual-write requires `@Transactional`

### 5.3 Implementation Steps

| Week | Tasks |
|------|-------|
| 15–16 | COTRN01C (Transaction View) and COTRN02C (Transaction Add): Refactor EVALUATE blocks to enum-based validation. Replace CSUTLDTC calls with `java.time`. REST POST with JSR-380 validation. |
| 17–19 | COCRDUPC (Card Update): Map card activation/deactivation logic. Replace HANDLE ABEND with try-catch + `@Transactional` rollback. PCI masking for card numbers. |
| 20–23 | COACTUPC (Account Update): Decompose 4,236-line program into `AccountValidationService` + `AccountUpdateService`. Map all 167 IF conditions. Create comprehensive test suite. |
| 24 | COBIL00C (Bill Payment): Implement `@Transactional` dual-write. Add optimistic locking. Implement idempotency key. |
| 25 | Parallel run: dual-write mode. Both COBOL and Java process writes. Reconciliation compares results nightly. |
| 26 | UAT gate with financial reconciliation sign-off. |

### 5.4 Dual-Write Strategy

During Phase 3 parallel run, writes are processed by **both** systems:

```
User Request ──► Java Service ──► PostgreSQL (primary)
                    │
                    └──► CDC reverse sync ──► VSAM (shadow)

Reconciliation Job (nightly):
  Compare PostgreSQL records vs. VSAM records
  Flag any discrepancies for manual review
```

**Cutover decision:** When reconciliation shows zero discrepancies for 2 consecutive billing cycles, stop VSAM shadow writes.

### 5.5 Data Migration

Phase 2 CDC pipeline continues. Additional considerations for writes:
- PostgreSQL becomes **system of record** for migrated entities
- CDC direction reverses: PostgreSQL → VSAM (for legacy programs not yet migrated)
- Conflict resolution: Java write wins (PostgreSQL is primary)

### 5.6 Success Criteria

- [ ] All 167 COACTUPC validation rules pass golden-master regression tests
- [ ] Bill payment dual-write: zero balance discrepancies over 2 billing cycles
- [ ] Write response time ≤ 300ms (P95)
- [ ] Transaction add: all EVALUATE branch conditions tested
- [ ] Idempotency: duplicate payment requests produce single transaction
- [ ] Optimistic lock: concurrent account updates handled gracefully (retry or conflict error)

### 5.7 Rollback Plan

**Trigger:** Balance discrepancy detected OR data corruption in any VSAM file.
**Action:**
1. Immediately route all write traffic back to CICS (menu routing update)
2. Read traffic can remain on Java (Phase 2 rollback not needed)
3. Reconcile: identify divergent records between PostgreSQL and VSAM
4. Restore VSAM from last known-good backup if corruption detected
5. Re-sync PostgreSQL from VSAM

**Recovery time:** < 15 minutes for traffic routing. Up to 4 hours for data reconciliation if corruption occurred.

**Critical:** VSAM backups must run daily during Phase 3. Retain 14 days of backups.

---

## 6. Phase 4 — Batch Processing (Weeks 27–40)

### 6.1 Scope

| Component | Strategy | LOC | Programs | Hotspot Score |
|-----------|----------|-----|----------|---------------|
| Transaction Posting | Refactor | 731 | CBTRN02C | 4.45 (#2) |
| Interest Calculation | Refactor | 652 | CBACT04C | 4.00 (#5) |
| Statement Generation | Rewrite | 1,154 | CBSTM03A/B | 3.90 (#6) |
| Transaction Reporting | Rewrite | 1,298 | CBTRN03C, CORPT00C | 3.55 (#8), 3.30 (#10) |
| Daily Transaction Prep | Refactor | 429 | CBTRN01C | — |

**Total:** 3,530 LOC across 5 batch programs (includes hotspots #2, #5, #6, #8, #10)

### 6.2 Rationale — Why Fourth?

1. **Batch is the highest financial risk** — transaction posting and interest calculation directly affect account balances and statements
2. **Online operations (Phase 3) must be stable first** — batch processes consume data produced by online operations
3. **Batch allows the longest parallel run** — run both COBOL and Java batch for N cycles, compare output penny-for-penny
4. **Regulatory implications** — interest calculation changes require audit trail and potentially regulatory sign-off

### 6.3 Implementation Steps

| Week | Tasks |
|------|-------|
| 27–28 | CBTRN01C (Daily Transaction Prep): Spring Batch `FlatFileItemReader` for DALYTRAN. Simple validation and staging. |
| 29–31 | CBTRN02C (Transaction Posting): Spring Batch chunk processing. `ItemProcessor` with cross-reference validation. Rejection handling via skip/retry policies. Dead-letter output for DALYREJS equivalent. |
| 32–34 | CBACT04C (Interest Calculation): `BigDecimal` with explicit `RoundingMode`. Multi-table rate lookup. **Penny-for-penny reconciliation** against COBOL output for every account. |
| 35–37 | CBSTM03A/B (Statement Generation): Thymeleaf template → HTML → PDF. Spring Batch per-account chunking. Output to cloud storage. |
| 38 | CBTRN03C + CORPT00C (Transaction Reporting): JPA queries replacing 5-file reads. JasperReports or custom Spring Batch report writer. REST endpoint for async report request. |
| 39–40 | Parallel batch run: execute COBOL and Java batch nightly for 2 billing cycles. Automated reconciliation of every output file. |

### 6.4 Batch Parallel Run Strategy

```
Nightly Batch Schedule (During Parallel Run):

22:00  CLOSEFIL (COBOL) — close VSAM files for batch
22:15  COBOL Batch Cycle: POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX
02:00  COBOL batch complete
02:15  Java Batch Cycle: PostingJob → InterestJob → BackupJob → StatementJob → IndexJob
06:00  Java batch complete
06:15  Reconciliation Job:
       - Compare TRANSACT record counts and sums
       - Compare TCATBALF balances per category
       - Compare interest amounts per account (penny-for-penny)
       - Compare statement output (field-level diff)
07:00  OPENFIL (COBOL) — reopen VSAM for online
07:15  Reconciliation report emailed to migration team
```

### 6.5 Data Migration

- By Phase 4, PostgreSQL is the system of record for accounts, cards, and transactions
- Batch jobs read from and write to PostgreSQL directly
- VSAM batch cycle continues as shadow for reconciliation
- After 2 clean billing cycles, decommission VSAM batch JCL

### 6.6 Success Criteria

- [ ] Transaction posting: zero rejected records that COBOL would have accepted (and vice versa)
- [ ] Interest calculation: **penny-for-penny match** with COBOL output for every account over 2 billing cycles
- [ ] Statement generation: field-level match with COBOL text output (formatting differences acceptable)
- [ ] Batch completion time ≤ COBOL batch time × 1.5 (allow initial overhead; optimize later)
- [ ] Reporting: all report totals match COBOL output
- [ ] Regulatory sign-off on interest calculation methodology documentation

### 6.7 Rollback Plan

**Trigger:** Penny-level discrepancy in interest calculation OR posting rejection mismatch.
**Action:**
1. Revert to COBOL-only batch cycle (Java batch disabled, COBOL batch promoted to primary)
2. If Java batch produced incorrect data: restore PostgreSQL from pre-batch snapshot
3. Re-run COBOL batch to restore correct state
4. Root-cause analysis before reattempting Java batch

**Recovery time:** Same-night if caught by reconciliation. Next business day if caught by users.

**Critical:** Database snapshots must be taken immediately before each Java batch run during parallel period.

---

## 7. Phase 5 — Admin, Utilities & Cleanup (Weeks 41–52)

### 7.1 Scope

| Component | Strategy | LOC | Programs |
|-----------|----------|-----|----------|
| User Admin CRUD | Rewrite | 1,767 | COUSR00C, COUSR01C, COUSR02C, COUSR03C |
| Data Export/Import | Rewrite | 1,069 | CBEXPORT, CBIMPORT |
| Remaining JCL Jobs | Eliminate | — | CLOSEFIL, OPENFIL, IDCAMS, SORT, etc. |

**Total:** 2,836 LOC across 8 programs + JCL elimination

### 7.2 Rationale — Why Last?

1. **User admin is low-usage** — only administrators access these screens
2. **Export/Import is migration tooling** — may not even be needed post-migration
3. **JCL jobs become obsolete** — VSAM file operations (OPEN/CLOSE/BACKUP/DEFINE) have no PostgreSQL equivalent
4. **Risk is low** — no financial impact, limited user base

### 7.3 Implementation Steps

| Week | Tasks |
|------|-------|
| 41–43 | User Admin CRUD: Spring Data JPA repository + Spring MVC controllers. Admin-only access via `@PreAuthorize("hasRole('ADMIN')")`. Add audit logging for all user changes. |
| 44–45 | Data Export/Import: Replace tagged sequential format with JSON/CSV export. REST API endpoints: `GET /admin/export`, `POST /admin/import`. Spring Batch for bulk operations. |
| 46–48 | JCL elimination: Document which JCL jobs are replaced by Java equivalents, which are eliminated. Database backup replaces VSAM REPRO. Scheduled tasks replace JCL schedulers. |
| 49–50 | Full system integration testing. End-to-end regression suite on Java-only system. Performance testing under load. |
| 51–52 | Legacy decommissioning preparation. Final reconciliation. Documentation. |

### 7.4 Data Migration

- No new data migration — all data is already in PostgreSQL from previous phases
- CBEXPORT/CBIMPORT replaced with database-native tools (`pg_dump`/`pg_restore` or API endpoints)
- VSAM files archived for regulatory retention (7 years)

### 7.5 Success Criteria

- [ ] Admin CRUD: all user management operations functional with audit trail
- [ ] Export: JSON/CSV output contains all fields from original tagged format
- [ ] Import: round-trip test (export → import → verify) succeeds with zero data loss
- [ ] Full regression suite: 100% pass rate on Java-only system
- [ ] Performance: all SLAs met without legacy system running
- [ ] No remaining CICS transactions active
- [ ] VSAM files archived and retention policy documented

### 7.6 Rollback Plan

**Trigger:** Critical admin functionality failure.
**Action:**
1. Re-enable CICS admin transactions (COUSR00-03) via 3270 bridge
2. Admin users use legacy screens while Java admin is fixed
3. No data rollback needed — admin operations are low-volume

**Recovery time:** < 5 minutes (3270 bridge already exists from Phase 1)

---

## 8. Optional Modules — Phase 6 (Post-Core)

### 8.1 Scope

| Module | Strategy | LOC | Programs | Duration |
|--------|----------|-----|----------|----------|
| Authorization (IMS/DB2/MQ) | Rewrite | 4,345 | 8 programs | 8–10 weeks |
| Transaction Type (DB2) | Refactor | 4,037 | 3 programs | 6–8 weeks |
| VSAM-MQ Integration | Rewrite | 1,144 | 2 programs | 3–4 weeks |

**Total:** 9,526 LOC across 13 programs

### 8.2 Approach

Optional modules use different middleware (IMS, DB2, MQ) and can be migrated independently after the core VSAM-based system is fully on Java. These should be scoped as separate projects with their own cutover plans.

---

## 9. Legacy Decommissioning Checklist

After all phases complete successfully:

| Step | Action | Criteria |
|------|--------|----------|
| 1 | Stop CDC pipeline | All data in PostgreSQL, no VSAM dependency |
| 2 | Disable CICS region | All transactions routed to Java for 30+ days |
| 3 | Archive VSAM files | Copy to tape/cloud archive for regulatory retention |
| 4 | Remove 3270 bridge | No legacy screen access needed |
| 5 | Decommission JCL scheduler | All batch jobs running as Spring Batch |
| 6 | Cancel mainframe contract | After 90-day observation period with zero fallback |
| 7 | Retain COBOL source | Archive in version control for reference (never delete) |

---

## 10. Timeline Summary

```
Month:  1      2      3      4      5      6      7      8      9      10     11     12
        ├──────┼──────┼──────┼──────┼──────┼──────┼──────┼──────┼──────┼──────┼──────┤
Phase 0 █░░░░░░│      │      │      │      │      │      │      │      │      │      │
Phase 1 │██████│      │      │      │      │      │      │      │      │      │      │
Phase 2 │      │██████│██████│      │      │      │      │      │      │      │      │
Phase 3 │      │      │      │██████│██████│██████│      │      │      │      │      │
Phase 4 │      │      │      │      │      │      │██████│██████│██████│██    │      │
Phase 5 │      │      │      │      │      │      │      │      │      │  ████│██████│
Decomm  │      │      │      │      │      │      │      │      │      │      │    ██│

Risk:   LOW    LOW    LOW    LOW-MED MED    MED    HIGH   HIGH   HIGH   MED    LOW
Team:   3-4    4-5    4-5    5-6    6-7    6-7    6-7    6-7    6-7    4-5    3-4
```

### Team Composition

| Role | Phase 1–2 | Phase 3 | Phase 4 | Phase 5 |
|------|-----------|---------|---------|---------|
| Tech Lead (COBOL + Java) | 1 | 1 | 1 | 1 |
| Java/Spring Developer | 2 | 3 | 3 | 2 |
| COBOL SME | 1 | 1 | 2 | 0.5 |
| QA / Test Engineer | 0.5 | 1 | 1 | 1 |
| DBA | 0.5 | 0.5 | 1 | 0.5 |
| **Total** | **5** | **6.5** | **8** | **5** |
