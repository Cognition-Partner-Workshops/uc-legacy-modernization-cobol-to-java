# Cutover Plan — CardDemo COBOL-to-Java Migration

> Phased migration sequence with program-level detail, data store impacts, integration bridges, rollback plans, and acceptance criteria.

---

## Table of Contents

1. [Migration Principles](#migration-principles)
2. [Phase Overview](#phase-overview)
3. [Phase 1 — Foundation & Quick Wins](#phase-1--foundation--quick-wins)
4. [Phase 2 — User Security & Authentication](#phase-2--user-security--authentication)
5. [Phase 3 — Card Management & Bill Payment](#phase-3--card-management--bill-payment)
6. [Phase 4 — Account Management](#phase-4--account-management)
7. [Phase 5 — Transaction Processing Pipeline](#phase-5--transaction-processing-pipeline)
8. [Phase 6 — Statement, Reporting & Utilities](#phase-6--statement-reporting--utilities)
9. [Phase 7 — Sub-Application Migration (Authorization IMS-DB2-MQ, VSAM-MQ)](#phase-7--sub-application-migration)
10. [Phase 8 — Decommission & Cleanup](#phase-8--decommission--cleanup)
11. [Cross-Phase Dependencies](#cross-phase-dependencies)
12. [Timeline Estimate](#timeline-estimate)

---

## Migration Principles

1. **Lowest risk first.** Each phase starts with the most isolated, least coupled component. (References: HOTSPOT_REPORT.md composite scores and DOMAIN_DECOMPOSITION.md extraction difficulty ratings.)
2. **Parallel-run before cutover.** Every phase includes a parallel-run period where both COBOL and Java execute, and outputs are compared.
3. **Strangler facade for online programs.** CICS online programs are wrapped with REST APIs before being replaced. The facade routes traffic to COBOL or Java based on a feature flag.
4. **Data migration follows code migration.** VSAM files are migrated to PostgreSQL only after the Java code that replaces the COBOL programs is proven in parallel-run.
5. **Rollback is always possible.** Every phase defines a rollback plan that restores the previous COBOL state within one business day.

---

## Phase Overview

```
Phase 1 ─── Transaction Reference Data (DB2 sub-app) + VSAM-MQ services
  │          Lowest risk, proof-of-concept for Java architecture
  │          HOTSPOT_REPORT Priority 3: COTRTLIC (40.8), COTRTUPC (37.7)
  ▼
Phase 2 ─── User Security & Authentication
  │          Clean extraction, security improvement
  │          HOTSPOT_REPORT Priority 6+: COUSR/COSGN programs (low composite)
  ▼
Phase 3 ─── Card Management & Bill Payment
  │          First VSAM-to-PostgreSQL migration
  │          HOTSPOT_REPORT Priority 4: COCRDLIC (41.3), COCRDUPC (40.5)
  ▼
Phase 4 ─── Account Management
  │          Highest complexity, highest value
  │          HOTSPOT_REPORT Priority 1: COACTUPC (77.9)
  ▼
Phase 5 ─── Transaction Processing Pipeline
  │          Core batch, highest operational risk
  │          HOTSPOT_REPORT Priority 5: CBTRN pipeline (27-35 range)
  ▼
Phase 6 ─── Statement, Reporting & Utilities
  │          Read-only consumers, data exchange
  │          HOTSPOT_REPORT Priority 2: CBSTM03A (41.5)
  ▼
Phase 7 ─── Sub-Application Migration (Authorization IMS-DB2-MQ)
  │          IMS decommission, most complex infrastructure
  │          HOTSPOT_REPORT: COPAUA0C (32.4), COPAUS0C
  ▼
Phase 8 ─── Decommission COBOL runtime, VSAM files, JCL jobs
```

---

## Phase 1 — Foundation & Quick Wins

**Duration estimate:** 4–6 weeks
**Risk level:** Low
**Hotspot reference:** COTRTLIC composite 40.8, COTRTUPC composite 37.7 (HOTSPOT_REPORT.md §Priority 3)

### Objective

Establish the Java/Spring Boot architecture, CI/CD pipeline, and shared libraries. Migrate the Transaction Type DB2 sub-application and VSAM-MQ services as proof-of-concept.

### Programs Migrating

| Program | LOC | New Java Component | Notes |
|---------|-----|--------------------|-------|
| COTRTLIC (2,098) | Online | `TransactionTypeController` + `TransactionTypeRepository` (Spring Data JPA) | DB2 cursors → Spring Data `Pageable` |
| COTRTUPC (1,702) | Online | `TransactionTypeUpdateController` + Bean Validation | DB2 CRUD → JPA `save()`/`delete()` |
| COBTUPDT (237) | Batch | `TransactionTypeBatchJob` (Spring Batch) | Action-code file processing (A/D/U) |
| COACCT01 (620) | Service | `AccountInquiryService` (REST endpoint) | MQ consumer → REST API |
| CODATE01 (524) | Service | `DateService` utility (`java.time`) | MQ consumer → REST API or utility class |

### Data Stores Affected

| Data Store | Current | Target | Migration |
|-----------|---------|--------|-----------|
| DB2: TRANSACTION_TYPE | DB2 table | PostgreSQL `transaction_types` table | SQL schema conversion, data dump/load |
| DB2: TRANSACTION_TYPE_CATEGORY | DB2 table | PostgreSQL `transaction_categories` table | SQL schema conversion, data dump/load |
| TRANTYPE (VSAM) | VSAM mirror of DB2 | Eliminated — batch programs will read from PostgreSQL | Remove TRANEXTR.jcl → TRANTYPE.jcl pipeline |
| TRANCATG (VSAM) | VSAM mirror of DB2 | Eliminated — same as above | Remove TRANCATG.jcl pipeline |

### Integration Bridges

| Bridge | Purpose | Duration |
|--------|---------|----------|
| DB2-to-PostgreSQL sync | During parallel-run, sync DB2 writes to PostgreSQL via CDC or periodic ETL | Until Phase 1 cutover |
| VSAM mirror maintenance | Keep TRANTYPE/TRANCATG VSAM files populated for COBOL programs not yet migrated (CBTRN01C, CBTRN03C) | Until Phase 5 cutover |
| MQ-to-REST adapter | For COACCT01/CODATE01, maintain MQ listeners that forward to new REST endpoints | Until all MQ producers are migrated |

### Rollback Plan

1. Restore DB2 as primary data store by re-enabling CICS COBOL programs.
2. Re-run TRANEXTR.jcl to refresh VSAM mirrors from DB2.
3. Disable Java services and remove API Gateway routes.
4. **Rollback time:** < 4 hours (no VSAM data changes involved).

### Acceptance Criteria

- [ ] All transaction types and categories display correctly in new UI with pagination matching COTRTLIC behavior.
- [ ] CRUD operations (add, update, delete) pass validation rules matching COTRTUPC behavior.
- [ ] Batch maintenance (COBTUPDT equivalent) processes action-code files with identical results.
- [ ] VSAM mirror files contain identical data to PostgreSQL tables (verified by automated comparison).
- [ ] AccountInquiryService returns identical results to COACCT01 MQ responses for 100% of test cases.
- [ ] DateService produces identical formatted dates to CODATE01 for all date format combinations.
- [ ] Response times for REST APIs are within 200ms p99 (comparable to CICS response times).
- [ ] CI/CD pipeline deploys successfully to staging and production environments.

### Deliverables

- Spring Boot application scaffold with shared libraries (JPA entities, error handling, logging).
- CI/CD pipeline (build, test, deploy).
- API Gateway configuration with routing rules.
- Automated regression test suite comparing COBOL and Java outputs.

---

## Phase 2 — User Security & Authentication

**Duration estimate:** 3–4 weeks
**Risk level:** Low
**Hotspot reference:** COSGN00C, COUSR00-03C (low composite scores, 10–41 logic points)

### Objective

Replace the COBOL authentication system with Spring Security + JWT. Migrate USRSEC VSAM to a PostgreSQL users table with bcrypt password hashing.

### Programs Migrating

| Program | LOC | New Java Component | Notes |
|---------|-----|--------------------|-------|
| COSGN00C (260) | Online | `AuthController.login()` + Spring Security `UserDetailsService` | CICS READ USRSEC → JPA query |
| COUSR00C (695) | Online | `UserController.listUsers()` | CICS STARTBR/READNEXT → Spring Data `findAll(Pageable)` |
| COUSR01C (299) | Online | `UserController.createUser()` | CICS WRITE USRSEC → JPA `save()` |
| COUSR02C (414) | Online | `UserController.updateUser()` | CICS READ/REWRITE → JPA `save()` |
| COUSR03C (359) | Online | `UserController.deleteUser()` | CICS READ/DELETE → JPA `deleteById()` |

### Data Stores Affected

| Data Store | Current | Target | Migration |
|-----------|---------|--------|-----------|
| USRSEC (VSAM KSDS) | 80-byte records, plaintext passwords | PostgreSQL `users` table, bcrypt hashed passwords | One-time migration: read USRSEC, hash passwords, load into PostgreSQL |

### Integration Bridges

| Bridge | Purpose | Duration |
|--------|---------|----------|
| JWT validation middleware | All remaining COBOL programs (still running in CICS) must accept JWT tokens. Add a CICS wrapper that validates JWT and populates COCOM01Y fields from JWT claims. | Until all online programs are migrated |
| Dual-auth during transition | Support both CICS COMMAREA sessions (for unmigrated programs) and JWT tokens (for migrated programs). API Gateway handles token translation. | Until Phase 4 cutover |

### Rollback Plan

1. Disable JWT authentication at API Gateway; revert to CICS sign-on flow.
2. USRSEC VSAM file is preserved unchanged (passwords were only hashed in PostgreSQL, not in VSAM).
3. **Rollback time:** < 2 hours.

### Acceptance Criteria

- [ ] All existing users can log in with current credentials (passwords verified against bcrypt hashes).
- [ ] User CRUD operations produce identical results to COUSR programs.
- [ ] Admin users (SEC-USR-TYPE = 'A') receive admin role in JWT claims.
- [ ] JWT tokens are validated by all remaining CICS programs via the translation wrapper.
- [ ] Password storage passes security audit (no plaintext passwords in any data store).
- [ ] Session timeout behavior matches CICS session timeout.

---

## Phase 3 — Card Management & Bill Payment

**Duration estimate:** 6–8 weeks
**Risk level:** Medium
**Hotspot reference:** COCRDLIC composite 41.3, COCRDUPC composite 40.5, COBIL00C composite 25.3 (HOTSPOT_REPORT.md §Priority 4)

### Objective

Migrate the card management online screens and bill payment to Spring Boot. Migrate CARDDAT and CCXREF VSAM files to PostgreSQL. This is the first phase involving VSAM-to-RDBMS data migration for core entity data.

### Programs Migrating

| Program | LOC | New Java Component | Notes |
|---------|-----|--------------------|-------|
| COCRDLIC (1,459) | Online | `CardController.listCards()` + pagination | 140 decision points — complex search/filter logic |
| COCRDSLC (887) | Online | `CardController.getCardDetail()` | Cross-reference resolution to account |
| COCRDUPC (1,560) | Online | `CardController.updateCard()` + `CardValidationService` | 164 decision points — extensive validation |
| COBIL00C (572) | Online | `PaymentController.processPayment()` | Writes to TRANSACT and updates ACCTDAT balance |

### Data Stores Affected

| Data Store | Current | Target | Migration |
|-----------|---------|--------|-----------|
| CARDDAT (VSAM KSDS) | `carddata.txt` → VSAM | PostgreSQL `cards` table | Bulk load from VSAM export |
| CCXREF (VSAM KSDS + AIX) | `cardxref.txt` → VSAM + alternate index | PostgreSQL `card_cross_references` table with indexes on both card_num and acct_id | Bulk load; create indexes to replace CXACAIX |
| ACCTDAT | Unchanged (still VSAM) | Read via temporary VSAM adapter | Account service not yet migrated; card service reads via bridge |
| TRANSACT | Unchanged (still VSAM) | Write via temporary VSAM adapter | COBIL00C payment transactions written via bridge |

### Integration Bridges

| Bridge | Purpose | Duration |
|--------|---------|----------|
| CCXREF dual-read | Programs not yet migrated (COACTUPC, CBTRN01C, etc.) still read CCXREF from VSAM. Maintain VSAM file as read replica, sync from PostgreSQL. | Until Phase 5 cutover |
| ACCTDAT read adapter | Card service reads account data via REST call to temporary VSAM-backed endpoint (or direct VSAM read via adapter library). | Until Phase 4 deploys account-service |
| TRANSACT write adapter | COBIL00C replacement writes payment transactions to both VSAM TRANSACT (for COBOL consumers) and PostgreSQL (for future transaction-service). | Until Phase 5 cutover |

### Rollback Plan

1. Disable card-service routes in API Gateway; re-enable CICS card programs.
2. VSAM CARDDAT and CCXREF files are preserved as read replicas throughout the phase.
3. If PostgreSQL `cards` or `card_cross_references` tables are corrupted, restore from VSAM export.
4. **Rollback time:** < 4 hours.

### Acceptance Criteria

- [ ] Card list pagination produces identical results to COCRDLIC for all search criteria combinations.
- [ ] Card update validates all fields identically to COCRDUPC (status transitions, expiry dates, embossed name).
- [ ] Bill payment produces identical account balance updates and transaction records to COBIL00C.
- [ ] CCXREF data in PostgreSQL matches VSAM file content (automated nightly comparison).
- [ ] Cross-reference lookups by both card number and account ID return correct results.
- [ ] All COBOL programs still reading CCXREF VSAM continue to function correctly.
- [ ] Payment transactions are visible in both VSAM TRANSACT and PostgreSQL.

---

## Phase 4 — Account Management

**Duration estimate:** 8–10 weeks
**Risk level:** High
**Hotspot reference:** COACTUPC composite 77.9 — highest in estate (HOTSPOT_REPORT.md §Priority 1)

### Objective

Migrate the most complex program in the estate (COACTUPC, 4,236 LOC, 188 decision points) and the account batch programs to Spring Boot / Spring Batch. Migrate ACCTDAT VSAM to PostgreSQL.

### Programs Migrating

| Program | LOC | New Java Component | Notes |
|---------|-----|--------------------|-------|
| COACTUPC (4,236) | Online | `AccountUpdateController` + `AccountValidationService` (188 validation rules) | Highest complexity — decompose into validation, persistence, and view layers |
| COACTVWC (941) | Online | `AccountViewController` | Read-only; simpler entry point |
| CBACT01C (430) | Batch | `AccountFileReaderJob` (Spring Batch) | VSAM reader with date conversion |
| CBACT02C (178) | Batch | `AccountBatchReaderStep` | Stub — trivial |
| CBACT03C (178) | Batch | `CrossRefBatchReaderStep` | Stub — trivial |
| CBACT04C (652) | Batch | `InterestCalculationJob` (Spring Batch) + `InterestCalculationService` (BigDecimal) | 86 decision points — financial calculation logic |

### Data Stores Affected

| Data Store | Current | Target | Migration |
|-----------|---------|--------|-----------|
| ACCTDAT (VSAM KSDS) | `acctdata.txt` → VSAM | PostgreSQL `accounts` table | Bulk load; field-by-field mapping from CVACT01Y |
| CXACAIX (VSAM AIX) | Alternate index on ACCTDAT | PostgreSQL index on `account_id` in `card_cross_references` | Index already created in Phase 3 |
| TCATBALF (VSAM KSDS) | `tcatbal.txt` → VSAM | PostgreSQL `transaction_category_balances` table | Bulk load; used by CBACT04C and CBTRN02C |
| DISCGRP (VSAM KSDS) | `discgrp.txt` → VSAM | PostgreSQL `disclosure_groups` table | Bulk load; read-only reference data |

### Integration Bridges

| Bridge | Purpose | Duration |
|--------|---------|----------|
| ACCTDAT dual-write | During parallel-run, account-service writes to both PostgreSQL and VSAM ACCTDAT. COBOL programs (CBTRN02C, COBIL00C replacement) can still read VSAM. | Until Phase 5 cutover |
| TCATBALF sync | CBTRN02C (not yet migrated) writes to TCATBALF VSAM. Sync VSAM changes to PostgreSQL for CBACT04C replacement. | Until Phase 5 cutover |
| Interest transaction bridge | CBACT04C replacement writes interest transactions to both PostgreSQL `transactions` table and VSAM TRANSACT file. | Until Phase 5 cutover |

### Rollback Plan

1. Disable account-service routes; re-enable CICS account programs.
2. VSAM ACCTDAT is maintained as primary during parallel-run. Rollback = stop dual-write, revert to VSAM-only.
3. For CBACT04C (interest calculation): run COBOL version from JCL (INTCALC.jcl) as backup. Compare outputs.
4. **Rollback time:** < 8 hours (interest calculation must complete within monthly cycle).

### Acceptance Criteria

- [ ] COACTUPC's 188 validation rules are documented and implemented with 100% parity.
- [ ] Account update produces identical field values for all test cases (automated comparison of VSAM and PostgreSQL records after each update).
- [ ] Interest calculation (CBACT04C) produces identical transaction amounts to 2 decimal places for all accounts over a 3-month historical dataset.
- [ ] Account view displays identical data to COACTVWC for all account records.
- [ ] Credit limit, cash credit limit, and balance fields maintain S9(10)V99 precision via BigDecimal.
- [ ] Date fields (open date, expiration date, reissue date) are correctly converted and validated.
- [ ] ACCTDAT VSAM file and PostgreSQL `accounts` table are byte-equivalent (after field mapping) for 100% of records during parallel-run.
- [ ] CBTRN02C (still COBOL) continues to update VSAM ACCTDAT correctly via the bridge.

---

## Phase 5 — Transaction Processing Pipeline

**Duration estimate:** 8–10 weeks
**Risk level:** High
**Hotspot reference:** CBTRN02C composite 27.4 (93 decision points), COTRN02C (40 logic points) (HOTSPOT_REPORT.md §Priority 5)

### Objective

Migrate the daily transaction processing pipeline (POSTTRAN.jcl: SORT → CBTRN01C → CBTRN02C) and online transaction screens to Spring Batch / Spring Boot. This is the most operationally critical migration.

### Programs Migrating

| Program | LOC | New Java Component | Notes |
|---------|-----|--------------------|-------|
| CBTRN01C (494) | Batch | `TransactionValidationStep` (Spring Batch ItemProcessor) | Validates daily transactions against cross-ref, enriches with type/category |
| CBTRN02C (731) | Batch | `TransactionPostingStep` (Spring Batch) + `AccountBalanceUpdater` | 93 decision points — updates ACCTDAT, TCATBALF, writes TRANSACT |
| COTRN00C (699) | Online | `TransactionListController` | Browse/paginate transactions |
| COTRN01C (330) | Online | `TransactionViewController` | Read-only transaction detail |
| COTRN02C (783) | Online | `TransactionCreateController` + `TransactionValidationService` | Add new transaction with validation |

### Data Stores Affected

| Data Store | Current | Target | Migration |
|-----------|---------|--------|-----------|
| DALYTRAN (flat file) | `dailytran.txt` → PS flat file | PostgreSQL `daily_transactions` staging table or message queue | Replace external feed format or keep flat file with Spring Batch `FlatFileItemReader` |
| TRANSACT (VSAM KSDS) | VSAM | PostgreSQL `transactions` table | Bulk load from VSAM backup |
| ACCTDAT | Already in PostgreSQL (Phase 4) | Direct JPA access | Remove VSAM dual-write |
| TCATBALF | Already in PostgreSQL (Phase 4) | Direct JPA access | Remove VSAM sync |

### JCL Jobs Replaced

| JCL Job | Replacement |
|---------|-------------|
| POSTTRAN.jcl (SORT + CBTRN01C + CBTRN02C) | `DailyTransactionPostingJob` (Spring Batch with 3 steps) |
| TRANBKP.jcl (IEBGENER backup) | Database backup job (pg_dump or cloud snapshot) |
| COMBTRAN.jcl (combine daily→master) | Eliminated — transactions stored directly in PostgreSQL |
| TRANIDX.jcl (build transaction index) | Eliminated — PostgreSQL indexes replace VSAM index building |
| DALYREJS.jcl (rejection report) | `RejectionReportStep` in Spring Batch job |

### Integration Bridges

| Bridge | Purpose | Duration |
|--------|---------|----------|
| Daily feed adapter | If external systems still produce DALYTRAN.PS flat files, implement a `FlatFileItemReader` or file watcher that loads into staging table. | Permanent (or until feed format changes) |
| Transaction read adapter | During parallel-run, COBOL statement generation (CBSTM03A, not yet migrated) still reads VSAM TRANSACT. Maintain VSAM as read replica. | Until Phase 6 cutover |

### Rollback Plan

1. Re-enable POSTTRAN.jcl batch schedule.
2. Restore VSAM TRANSACT, ACCTDAT, TCATBALF from most recent backup GDG (TRANSACT.BKUP).
3. Re-process the day's DALYTRAN.PS through COBOL pipeline.
4. **Rollback time:** < 12 hours (must complete before next day's batch window).
5. **Critical:** Maintain daily VSAM backups (TRANBKP.jcl) throughout parallel-run as insurance.

### Acceptance Criteria

- [ ] Daily transaction posting produces identical account balances (ACCTDAT) for a 30-day historical replay.
- [ ] Transaction category balances (TCATBALF) match COBOL output for all accounts.
- [ ] TRANSACT records match COBOL output field-by-field (including timestamps and generated IDs).
- [ ] JCL SORT step is replicated: transactions sorted by card number + timestamp before processing.
- [ ] Rejection handling produces identical rejection counts and records.
- [ ] Spring Batch job completes within the existing batch window (< 2 hours for full daily cycle).
- [ ] Online transaction list/view/add produce identical results to COTRN programs.
- [ ] Transaction add (COTRN02C) validates account, card, amounts, and dates identically.
- [ ] CBSTM03A (still COBOL) continues to read transactions from VSAM replica correctly.

---

## Phase 6 — Statement, Reporting & Utilities

**Duration estimate:** 5–7 weeks
**Risk level:** Medium
**Hotspot reference:** CBSTM03A composite 41.5 (117 I/O ops, ALTER/GO TO pattern) (HOTSPOT_REPORT.md §Priority 2)

### Objective

Replace statement generation, reporting, data export/import, and navigation programs. Eliminate VSAM read replicas — all data now comes from PostgreSQL.

### Programs Migrating

| Program | LOC | New Java Component | Notes |
|---------|-----|--------------------|-------|
| CBSTM03A (924) | Batch | `StatementGenerationJob` + Thymeleaf templates | ALTER/GO TO → structured Java code |
| CBSTM03B (230) | Batch | Eliminated (I/O handled by JPA) | Subroutine absorbed into main job |
| CBTRN03C (649) | Batch | `DailyTransactionReportJob` | Report formatting with type/category lookups |
| CORPT00C (649) | Online | `ReportController.submitReport()` | TD queue → message queue (RabbitMQ/SQS) |
| CBEXPORT (582) | Batch | `DataExportJob` (Spring Batch) — JSON/CSV output | Replaces COBOL record-type-prefixed format |
| CBIMPORT (487) | Batch | `DataImportJob` (Spring Batch) — orchestrates entity service calls | Writes via JPA to each entity table |
| COMEN01C (308) | Online | React/Angular SPA routing | Navigation hub → frontend routes |
| COADM01C (288) | Online | React/Angular admin section | Admin menu → role-gated admin routes |
| CSUTLDTC (157) | Utility | `java.time.LocalDate.parse()` | Date validation utility |
| COBSWAIT (41) | Utility | `Thread.sleep()` or eliminated | Wait utility |

### Data Stores Affected

| Data Store | Current | Target | Migration |
|-----------|---------|--------|-----------|
| STATEMNT.PS / STATEMNT.HTML | Flat file + HTML file outputs | Generated files stored in cloud storage (S3/Azure Blob) | New format; old format preserved for comparison |
| TRANREPT (GDG) | Generation Data Group output | Report files in cloud storage | New format |
| EXPORT.DATA.PS | COBOL record-type-prefixed flat file | JSON or CSV export file | New format (provide backward-compatible exporter if needed) |
| DATEPARM | Configuration flat file | Application properties / environment variable | Inline into Spring configuration |

### JCL Jobs Replaced

| JCL Job | Replacement |
|---------|-------------|
| CREASTMT.JCL | `StatementGenerationJob` (Spring Batch) |
| TRANREPT.jcl | `DailyTransactionReportJob` (Spring Batch) |
| TXT2PDF1.JCL | Apache FOP or iText PDF generation (integrated into statement job) |
| CBEXPORT.jcl | `DataExportJob` (Spring Batch) |
| CBIMPORT.jcl | `DataImportJob` (Spring Batch) |
| REPTFILE.jcl | Eliminated (cloud storage handles allocation) |

### Integration Bridges

| Bridge | Purpose | Duration |
|--------|---------|----------|
| Statement format comparison | Generate statements in both old (text/HTML) and new (PDF/HTML) formats during parallel-run. Automated diff on content. | 2 billing cycles |
| Export format adapter | If downstream systems consume EXPORT.DATA.PS format, provide a format conversion adapter. | Until downstream systems migrate |

### Rollback Plan

1. Re-enable JCL batch jobs (CREASTMT.JCL, TRANREPT.jcl) in the batch schedule.
2. Statements and reports are generated from the same underlying data — no data corruption risk.
3. For export/import: COBOL programs can still read VSAM files (maintained as read replicas until this phase is fully cut over).
4. **Rollback time:** < 4 hours.

### Acceptance Criteria

- [ ] Generated statements contain identical transaction lists, balances, and customer information compared to CBSTM03A output.
- [ ] HTML statements render correctly in modern browsers.
- [ ] PDF generation replaces TXT2PDF1.JCL REXX-based conversion.
- [ ] Daily transaction report contains identical totals, account breakdowns, and formatting.
- [ ] Data export produces a file that can be re-imported with zero data loss.
- [ ] Navigation and menu routing works for all user roles (admin, regular user).
- [ ] All VSAM read replicas can be removed — no COBOL program still depends on VSAM files.

---

## Phase 7 — Sub-Application Migration

**Duration estimate:** 8–10 weeks
**Risk level:** High
**Hotspot reference:** COPAUA0C composite 32.4, COPAUS0C (47 logic points) (HOTSPOT_REPORT.md §Priority 7+)

### Objective

Migrate the Authorization IMS-DB2-MQ sub-application. Replace IMS databases with PostgreSQL. Replace MQ-based message processing with REST APIs or modern message broker.

### Programs Migrating

| Program | LOC | New Java Component | Notes |
|---------|-----|--------------------|-------|
| CBPAUP0C (386) | Batch | `PaymentAuthorizationBatchProcessor` (Spring Batch / message listener) | MQ GET/PUT → Spring JMS or Kafka consumer |
| COPAUA0C (1,026) | Online | `AuthorizationAdminController` + `FraudDetectionService` | DB2 AUTHFRDS → JPA entity |
| COPAUS0C (1,032) | Online | `AuthorizationSummaryController` | DB2 queries → JPA queries |
| COPAUS1C (604) | Online | `AuthorizationDetailController` | DB2 query → JPA findById |
| COPAUS2C (244) | Online | `AuthorizationHistoryController` | Simple display |
| PAUDBLOD (369) | Batch | `AuthorizationDataLoadJob` (Spring Batch) | IMS ISRT → JPA batch insert |
| PAUDBUNL (317) | Batch | `AuthorizationDataExportJob` | IMS GN/GHN → JPA query + file write |
| DBUNLDGS (366) | Batch | `GSAMExportJob` | GSAM → JPA query + file write |

### Data Stores Affected

| Data Store | Current | Target | Migration |
|-----------|---------|--------|-----------|
| IMS: DBPAUTP0 (Primary DB) | IMS hierarchical database | PostgreSQL `payment_authorizations` table | Schema reverse-engineering from IMS DBD/PSB definitions; data export via PAUDBUNL, transform, load |
| IMS: DBPAUTX0 (Index DB) | IMS secondary index | PostgreSQL index on `payment_authorizations` | Automatic with table creation |
| DB2: AUTHFRDS | DB2 fraud detection table | PostgreSQL `fraud_rules` table | SQL schema conversion, data dump/load |
| MQ queues | IBM MQ request/reply queues | RabbitMQ/Kafka topics or REST API | Message format preserved during transition |

### Integration Bridges

| Bridge | Purpose | Duration |
|--------|---------|----------|
| MQ dual-consumer | During parallel-run, both COBOL and Java consumers process MQ authorization messages. Compare responses for accuracy. | 4 weeks parallel-run |
| IMS-to-PostgreSQL migration tool | Custom ETL to extract IMS segments, transform to relational rows, load into PostgreSQL. | One-time migration |

### Rollback Plan

1. Re-enable COBOL MQ consumer; disable Java consumer.
2. IMS databases are preserved unchanged during parallel-run.
3. Re-enable CICS online programs for authorization admin screens.
4. **Rollback time:** < 6 hours.

### Acceptance Criteria

- [ ] Authorization request/reply processing produces identical authorization codes and response codes to COBOL.
- [ ] Fraud detection queries return identical results from PostgreSQL as from DB2 AUTHFRDS.
- [ ] IMS data is fully migrated with zero record loss (verified by record count and checksum comparison).
- [ ] Authorization summary statistics match COPAUS0C output.
- [ ] Batch authorization processing throughput meets or exceeds COBOL performance.
- [ ] MQ message processing latency is within 50ms p99.

---

## Phase 8 — Decommission & Cleanup

**Duration estimate:** 3–4 weeks
**Risk level:** Low (all traffic already on Java)

### Objective

Remove all COBOL runtime infrastructure, VSAM files, JCL jobs, and CICS configurations. Close the mainframe environment.

### Activities

| Activity | Detail |
|----------|--------|
| Disable CICS region | Remove all CICS program definitions (CSD entries) |
| Archive VSAM files | Export all VSAM files to cloud storage as historical archives |
| Remove JCL jobs | Archive all 44 JCL jobs; remove from batch scheduler |
| Decommission DB2 | Archive DB2 tables; remove DB2 subsystem |
| Decommission IMS | Archive IMS databases; remove IMS subsystem |
| Remove MQ infrastructure | Remove IBM MQ queues and channel definitions (if fully replaced) |
| Archive COBOL source | Tag the final COBOL source in version control as `legacy/final-cobol-state` |
| Remove VSAM read replicas | Drop all VSAM sync jobs and adapter code |
| Documentation | Update all runbooks, operational procedures, and architecture diagrams |

### Acceptance Criteria

- [ ] No COBOL program executes in any environment (dev, staging, production).
- [ ] No VSAM file is read or written by any running process.
- [ ] No JCL job is scheduled or executable.
- [ ] All data is accessible via PostgreSQL and the Java service APIs.
- [ ] Operational monitoring dashboards show zero mainframe dependencies.
- [ ] COBOL source code is archived with full git history.

---

## Cross-Phase Dependencies

```
Phase 1 ──────────────────────────────────────►
         DB2 sub-app, VSAM-MQ services
         Establishes: Java architecture, CI/CD, shared libs

Phase 2 ──────────────────────────────────────►
         User Security
         Depends on: Phase 1 (architecture, CI/CD)
         Produces: JWT auth for all subsequent phases

Phase 3 ──────────────────────────────────────►
         Card Management
         Depends on: Phase 2 (JWT auth)
         Produces: cards + cross-ref tables in PostgreSQL

Phase 4 ──────────────────────────────────────►
         Account Management
         Depends on: Phase 3 (cross-ref in PostgreSQL)
         Produces: accounts table in PostgreSQL

Phase 5 ──────────────────────────────────────►
         Transaction Processing
         Depends on: Phase 4 (accounts in PostgreSQL)
         Produces: transactions + category balances in PostgreSQL

Phase 6 ──────────────────────────────────────►
         Reporting & Utilities
         Depends on: Phase 5 (all entity data in PostgreSQL)
         Produces: VSAM read replicas decommissioned

Phase 7 ──────────────────────────────────────►
         Authorization Sub-App
         Depends on: Phase 2 (JWT auth), Phase 4 (account data)
         Can run in parallel with Phases 5-6

Phase 8 ──────────────────────────────────────►
         Decommission
         Depends on: ALL previous phases complete
```

---

## Timeline Estimate

| Phase | Duration | Cumulative | Parallel? |
|-------|----------|-----------|-----------|
| Phase 1: Foundation & Quick Wins | 4–6 weeks | 4–6 weeks | — |
| Phase 2: User Security | 3–4 weeks | 7–10 weeks | — |
| Phase 3: Card Management | 6–8 weeks | 13–18 weeks | — |
| Phase 4: Account Management | 8–10 weeks | 21–28 weeks | — |
| Phase 5: Transaction Processing | 8–10 weeks | 29–38 weeks | — |
| Phase 6: Reporting & Utilities | 5–7 weeks | 34–45 weeks | Partially overlaps Phase 5 |
| Phase 7: Authorization Sub-App | 8–10 weeks | 42–55 weeks | Partially overlaps Phases 5–6 |
| Phase 8: Decommission | 3–4 weeks | 45–59 weeks | After all phases |

**Total estimated duration: 10–14 months** (with parallelization of Phases 5–7)

### Critical Path

The critical path runs through: Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5 → Phase 8.

Phases 6 and 7 can be parallelized with Phases 5 once the prerequisite data stores are migrated. This parallelization can reduce the overall timeline by 8–12 weeks.
