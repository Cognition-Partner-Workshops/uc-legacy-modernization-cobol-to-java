# CardDemo Modernization Risk Register

## Overview

This register identifies, categorizes, and prioritizes risks for the CardDemo COBOL-to-Java modernization. Each risk is scored using a **Likelihood x Impact** matrix (1-5 scale) and has defined mitigation strategies, owners, and trigger indicators.

### Scoring Guide

| Score | Likelihood | Impact |
|-------|-----------|--------|
| 1 | Rare | Negligible |
| 2 | Unlikely | Minor |
| 3 | Possible | Moderate |
| 4 | Likely | Major |
| 5 | Almost Certain | Severe |

**Risk Rating**: Likelihood x Impact. Critical >= 16, High = 10-15, Medium = 5-9, Low = 1-4.

---

## Risk Summary Heat Map

```
Impact
  5 |     |     | R07 | R01 | R03 |
  4 |     | R13 | R04 | R02 | R06 |
  3 |     | R14 | R09 | R05 |     |
  2 |     | R15 | R11 | R10 |     |
  1 |     |     | R12 |     |     |
    +-----+-----+-----+-----+-----+
      1     2     3     4     5
                Likelihood
```

---

## Risk Detail

### R01: Financial Calculation Discrepancy (CRITICAL)

| Attribute | Value |
|-----------|-------|
| **Category** | Data Integrity |
| **Likelihood** | 4 (Likely) |
| **Impact** | 5 (Severe) |
| **Risk Rating** | **20 (Critical)** |
| **Phase** | Phase 5 (Batch Processing) |
| **Affected Components** | CBACT04C (interest calc), CBTRN02C (posting), COBIL00C (payment) |

**Description**: COBOL uses packed decimal (COMP-3) arithmetic with specific truncation and rounding behavior (e.g., `PIC S9(10)V99`). Java `BigDecimal` has different default rounding modes. Interest calculations in CBACT04C and balance updates across multiple programs could produce penny-level discrepancies that compound over thousands of accounts.

**Root Cause**: COBOL `COMPUTE` statements with intermediate results use different precision rules than Java's `BigDecimal.multiply()` and `BigDecimal.divide()`.

**Trigger Indicators**:
- Reconciliation reports show non-zero differences in account balances
- Interest calculation output differs from legacy for the same input
- Category balance totals (TCATBALF) diverge after batch posting

**Mitigations**:

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M1.1 | Use `BigDecimal` with `RoundingMode.HALF_EVEN` (banker's rounding) to match COBOL behavior | Dev Team | Planned |
| M1.2 | Create a "COBOL arithmetic emulator" utility class that replicates COMP-3 precision for critical calculations | Dev Team | Planned |
| M1.3 | Run parallel batch cycles (COBOL and Java) for 90 days with automated penny-level reconciliation | QA Team | Planned |
| M1.4 | Document every COMPUTE statement in CBACT04C and CBTRN02C with expected intermediate precision | Architect | Planned |
| M1.5 | Establish a financial reconciliation gate: zero tolerance for balance differences before cutover | Program Mgr | Planned |

**Contingency**: If discrepancies cannot be resolved, replatform CBACT04C under Micro Focus runtime to preserve exact COBOL arithmetic, and wrap with a Java API.

---

### R02: Data Migration Loss or Corruption (HIGH)

| Attribute | Value |
|-----------|-------|
| **Category** | Data Integrity |
| **Likelihood** | 4 (Likely) |
| **Impact** | 4 (Major) |
| **Risk Rating** | **16 (Critical)** |
| **Phase** | Phase 0-3 |
| **Affected Components** | All VSAM files, IMS HIDAM DB, DB2 tables |

**Description**: Migration of data from VSAM KSDS files (ACCTDAT, CARDDAT, CUSTDAT, TRANSACT, etc.) to PostgreSQL involves EBCDIC-to-ASCII conversion, packed decimal unpacking, fixed-length field parsing, and FILLER elimination. Any mapping error corrupts production data.

**Root Cause**: COBOL copybook record layouts use packed decimal (COMP-3), zoned decimal, and fixed-length fields with implicit decimal points. Mapping errors in any of the 9 VSAM files could silently corrupt data.

**Trigger Indicators**:
- Record counts differ between VSAM and PostgreSQL after migration
- Numeric field values are incorrect (sign handling, decimal placement)
- Character fields have trailing spaces or encoding issues
- Cross-reference integrity violations (orphaned cards, missing accounts)

**Mitigations**:

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M2.1 | Use ASCII sample data in `app/data/ASCII/` for initial validation before tackling EBCDIC | Dev Team | Planned |
| M2.2 | Build automated copybook-to-schema mapping tool with field-by-field assertions | Dev Team | Planned |
| M2.3 | Implement record count, checksum, and min/max range validation for every migrated table | Data Team | Planned |
| M2.4 | Test with production-volume data (not just samples) in pre-production environment | QA Team | Planned |
| M2.5 | Verify referential integrity post-migration: every card -> account, every account -> customer | Data Team | Planned |
| M2.6 | Maintain VSAM files in read-only mode for 90 days post-cutover as recovery source | Ops Team | Planned |

**Contingency**: Restore from VSAM backup and re-run migration with corrected mappings. Rollback to legacy system if data integrity cannot be verified within 48 hours.

---

### R03: Batch-Online Contention During Dual-Run (CRITICAL)

| Attribute | Value |
|-----------|-------|
| **Category** | Operational |
| **Likelihood** | 5 (Almost Certain) |
| **Impact** | 5 (Severe) |
| **Risk Rating** | **25 (Critical)** |
| **Phase** | Phase 3-5 |
| **Affected Components** | CLOSEFIL/OPENFIL JCL, CBTRN02C, COBIL00C, COACTUPC |

**Description**: The legacy batch cycle requires CLOSEFIL to close VSAM files for CICS before batch can access them, and OPENFIL to reopen them. During dual-run, the new Java services use PostgreSQL (always available) while legacy batch still needs exclusive VSAM access. This creates a window where VSAM and PostgreSQL are out of sync, and any writes to either system during the batch window create split-brain inconsistencies.

**Root Cause**: VSAM does not support concurrent online and batch access to the same files. The CLOSEFIL/OPENFIL pattern is a mainframe constraint with no cloud equivalent.

**Trigger Indicators**:
- Dual-write reconciliation shows divergence during batch windows
- Online transactions fail during batch CLOSEFIL window
- Account balances differ between VSAM and PostgreSQL after batch cycle

**Mitigations**:

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M3.1 | During dual-run, quiesce Java write operations during the legacy batch window | Ops Team | Planned |
| M3.2 | Implement a "batch lock" flag in the Java services that pauses writes during CLOSEFIL/OPENFIL | Dev Team | Planned |
| M3.3 | Run reconciliation immediately after each batch cycle to detect divergence | Data Team | Planned |
| M3.4 | Shorten dual-run period: migrate batch jobs as soon as possible (Phase 5) to eliminate the CLOSEFIL dependency | Architect | Planned |
| M3.5 | Design batch jobs to read from PostgreSQL as early as Phase 4, eliminating VSAM dependency | Dev Team | Planned |

**Contingency**: If dual-run contention is unmanageable, accelerate Phase 5 (batch migration) or accept a brief cutover outage window instead of zero-downtime migration.

---

### R04: IMS HIDAM Migration Complexity (HIGH)

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Likelihood** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Risk Rating** | **12 (High)** |
| **Phase** | Phase 6 |
| **Affected Components** | COPAUA0C, COPAUS0C/1C/2C, CBPAUP0C, IMS DBPAUTP0/DBPAUTX0 |

**Description**: The authorization module uses IMS HIDAM (Hierarchical Indexed Direct Access Method) with a root segment (PAUTSUM0) and child segment (PAUTDTL1). IMS DL/I calls (GU, GN, GNP, ISRT, DLET) have no direct relational equivalent. The hierarchical parent-child relationship, segment search arguments (SSAs), and secondary indexing must be carefully mapped to relational JOINs and queries.

**Root Cause**: Hierarchical-to-relational data model transformation is inherently lossy. IMS segment retrieval semantics (positioning, parentage) differ fundamentally from SQL set-based queries.

**Trigger Indicators**:
- Authorization queries return incorrect or missing child records
- Performance degradation when hierarchical traversal becomes multiple SQL queries
- Data migration produces orphaned detail records

**Mitigations**:

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M4.1 | Map IMS segments to normalized PostgreSQL tables with FK relationships | Architect | Planned |
| M4.2 | Create a DL/I-to-SQL mapping guide for each call type (GU, GN, GNP, ISRT, DLET) | Dev Team | Planned |
| M4.3 | Use database-level cascading deletes to replicate IMS DLET with segment propagation | Dev Team | Planned |
| M4.4 | Load test with production-volume authorization data (potentially millions of records) | QA Team | Planned |
| M4.5 | Schedule Phase 6 last to allow maximum team learning from earlier phases | Program Mgr | Planned |

**Contingency**: If IMS migration is blocked, run the authorization module on a managed mainframe runtime (AWS M2) as a long-term replatform while the rest of the system runs on Java.

---

### R05: CICS Pseudo-Conversational State Management (HIGH)

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Likelihood** | 4 (Likely) |
| **Impact** | 3 (Moderate) |
| **Risk Rating** | **12 (High)** |
| **Phase** | Phase 2-3 |
| **Affected Components** | All CICS online programs (CO* prefix) |

**Description**: CICS pseudo-conversational programs use RETURN TRANSID with COMMAREA to suspend between user interactions. Each SEND MAP / RECEIVE MAP cycle ends the task and restarts it. This stateful-but-not pattern has no direct equivalent in stateless REST services. Programs like COACTUPC maintain complex state across multiple screen interactions (enter -> validate -> confirm -> save).

**Root Cause**: CICS pseudo-conversational programming model conflates session state, transaction state, and UI state in a single COMMAREA.

**Trigger Indicators**:
- Users lose context when navigating between screens in the new UI
- Multi-step forms (account update) lose data between steps
- Concurrent users see incorrect data (state leakage)

**Mitigations**:

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M5.1 | Design new services as stateless REST; move all state to frontend (React/Angular) or session store (Redis) | Architect | Planned |
| M5.2 | Map COMMAREA fields to JWT claims (user context) and request parameters (entity context) | Dev Team | Planned |
| M5.3 | For multi-step operations (e.g., account update), use a frontend wizard pattern with client-side state | Dev Team | Planned |
| M5.4 | Implement optimistic locking (version column) to replace CICS record-level locking | Dev Team | Planned |
| M5.5 | Document every RETURN TRANSID flow and its stateless equivalent | Architect | Planned |

**Contingency**: For complex multi-step flows that resist stateless redesign, use server-side session state (Spring Session + Redis) as an interim solution.

---

### R06: MQ-to-Modern-Messaging Migration (HIGH)

| Attribute | Value |
|-----------|-------|
| **Category** | Integration |
| **Likelihood** | 5 (Almost Certain) |
| **Impact** | 4 (Major) |
| **Risk Rating** | **20 (Critical)** |
| **Phase** | Phase 6 |
| **Affected Components** | COPAUA0C (MQ trigger), CODATE01, COACCT01, all MQ queues |

**Description**: IBM MQ is used for authorization request/response (CSV format), system date inquiry, and account extraction. External systems (POS emulators, cloud clients) send messages to MQ queues. Migrating to Kafka/SQS requires: (1) message format change (CSV to JSON), (2) delivery semantics change (MQ exactly-once vs. Kafka at-least-once), (3) external client coordination, and (4) MQ trigger-based initiation replaced with consumer polling/listener.

**Root Cause**: External systems depend on specific MQ queue names, message formats, and delivery guarantees that change during migration.

**Trigger Indicators**:
- Authorization requests from POS systems fail after migration
- Message loss or duplication during format transition
- External clients cannot connect to new messaging infrastructure

**Mitigations**:

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M6.1 | Deploy a message bridge: MQ -> Kafka/SQS adapter that translates CSV to JSON and vice versa | Dev Team | Planned |
| M6.2 | Maintain MQ queues during transition with bridge forwarding to new messaging | Ops Team | Planned |
| M6.3 | Implement idempotency keys in authorization processing to handle at-least-once delivery | Dev Team | Planned |
| M6.4 | Coordinate with external POS/client teams on migration timeline and test windows | Program Mgr | Planned |
| M6.5 | Use dead-letter queues on both MQ and Kafka sides to capture failed messages | Dev Team | Planned |

**Contingency**: Keep MQ as the external-facing interface permanently. Use an internal MQ-to-Kafka bridge as a long-term architecture component.

---

### R07: Statement Generation Replatform Failure (HIGH)

| Attribute | Value |
|-----------|-------|
| **Category** | Technical |
| **Likelihood** | 3 (Possible) |
| **Impact** | 5 (Severe) |
| **Risk Rating** | **15 (High)** |
| **Phase** | Phase 4 |
| **Affected Components** | CBSTM03A, CBSTM03B |

**Description**: CBSTM03A (924 lines) uses `ALTER ... GO TO` statements, PSA/TCB/TIOT control block addressing, POINTER manipulation, and subroutine CALLs. These z/OS-specific constructs defeat auto-conversion tools and are difficult to reproduce on managed COBOL runtimes (e.g., Micro Focus). If replatform fails, statement generation is blocked.

**Root Cause**: `ALTER` modifies GO TO targets at runtime (self-modifying code), and PSA/TCB/TIOT addressing reads z/OS control blocks that only exist on a real mainframe.

**Trigger Indicators**:
- CBSTM03A fails to compile on Micro Focus runtime
- Statement output differs from z/OS output
- Control block addressing produces garbage data on non-z/OS runtime

**Mitigations**:

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M7.1 | Test CBSTM03A compilation on Micro Focus COBOL early (Phase 0 POC) | Dev Team | Planned |
| M7.2 | Identify and catalog all ALTER/GO TO statements and control block references | Architect | Planned |
| M7.3 | If replatform fails, plan early rewrite as a Java PDF generation service using iText or JasperReports | Dev Team | Planned |
| M7.4 | Keep z/OS runtime available as fallback until statement generation is fully replaced | Ops Team | Planned |
| M7.5 | Use the HTML output mode of CBSTM03A as the specification for the Java rewrite | Dev Team | Planned |

**Contingency**: If replatform fails entirely, run CBSTM03A on AWS Mainframe Modernization managed runtime, or accelerate the rewrite using the HTML output as a visual specification.

---

### R08: Team Skill Gap -- COBOL Expertise (HIGH)

| Attribute | Value |
|-----------|-------|
| **Category** | Organizational |
| **Likelihood** | 4 (Likely) |
| **Impact** | 3 (Moderate) |
| **Risk Rating** | **12 (High)** |
| **Phase** | All phases |
| **Affected Components** | All 31 COBOL programs |

**Description**: The modernization team needs developers who can read and understand COBOL/CICS/VSAM patterns to correctly port business logic. COBOL expertise is scarce and expensive. Without adequate COBOL reading skills, the team may misinterpret business rules (especially in COACTUPC's 4,237 lines of validation or CBACT04C's interest calculation).

**Trigger Indicators**:
- Ported validation rules fail to match legacy behavior
- Business logic misinterpretation found during UAT
- COBOL subject matter experts are unavailable for clarification

**Mitigations**:

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M8.1 | Train Java developers on COBOL reading using this CardDemo codebase as training material | Program Mgr | Planned |
| M8.2 | Create a COBOL-to-Java pattern mapping guide (PERFORM -> method call, EVALUATE -> switch, etc.) | Architect | Planned |
| M8.3 | Pair program: one COBOL reader + one Java developer on each porting task | Dev Lead | Planned |
| M8.4 | Use AI-assisted code analysis tools to generate Java equivalent pseudocode from COBOL | Dev Team | Planned |
| M8.5 | Retain at least one COBOL SME on contract through Phase 6 | Program Mgr | Planned |

**Contingency**: Engage a mainframe modernization consultancy (e.g., Cognizant, TCS, Infosys) for COBOL analysis support.

---

### R09: Performance Degradation -- Batch Window Overrun (MEDIUM)

| Attribute | Value |
|-----------|-------|
| **Category** | Performance |
| **Likelihood** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Risk Rating** | **9 (Medium)** |
| **Phase** | Phase 5 |
| **Affected Components** | CBTRN02C, CBACT04C, CBSTM03A (Spring Batch equivalents) |

**Description**: Mainframe batch jobs benefit from high I/O throughput, channel-attached storage, and optimized sequential file access. Spring Batch running on cloud infrastructure may not match mainframe batch performance, especially for transaction posting (CBTRN02C processes the entire daily transaction file) and interest calculation (CBACT04C touches every account).

**Trigger Indicators**:
- Batch cycle exceeds the available overnight window
- Database lock contention during batch processing
- Memory pressure from large chunk sizes

**Mitigations**:

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M9.1 | Profile batch jobs with production-volume data during Phase 5 testing | QA Team | Planned |
| M9.2 | Use partitioned steps in Spring Batch for parallel processing (partition by account range) | Dev Team | Planned |
| M9.3 | Optimize database: batch-appropriate indexes, connection pooling, prepared statements | DBA | Planned |
| M9.4 | Consider database bulk operations (COPY, batch INSERT) instead of row-by-row | Dev Team | Planned |
| M9.5 | Right-size compute: use dedicated batch instances with more CPU/memory | Ops Team | Planned |

**Contingency**: If batch window is insufficient, split batch into micro-batches that run throughout the day (enabled by eliminating CLOSEFIL/OPENFIL contention).

---

### R10: Cross-Reference Elimination Breaks Queries (MEDIUM)

| Attribute | Value |
|-----------|-------|
| **Category** | Data Integrity |
| **Likelihood** | 4 (Likely) |
| **Impact** | 2 (Minor) |
| **Risk Rating** | **8 (Medium)** |
| **Phase** | Phase 0-1 |
| **Affected Components** | CARDXREF (CVACT03Y), CXACAIX, CARDAIX |

**Description**: The CARDXREF VSAM file and its alternate indexes (CXACAIX, CARDAIX) provide the card-number-to-account-to-customer lookup chain used by 6 bounded contexts. Eliminating this file in favor of relational FKs changes query patterns. Programs that currently do sequential BROWSE on alternate indexes need different query strategies.

**Trigger Indicators**:
- Queries that used to return results via alternate index return empty sets
- Performance differs for queries that relied on VSAM alternate index ordering
- Edge cases where a card has no account or an account has no customer

**Mitigations**:

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M10.1 | Create database indexes on `cards.account_id` and `accounts.customer_id` to match VSAM AIX performance | DBA | Planned |
| M10.2 | Map every VSAM BROWSE by alternate index to an equivalent SQL ORDER BY query | Dev Team | Planned |
| M10.3 | Verify referential integrity constraints catch all edge cases (orphaned records) | Data Team | Planned |
| M10.4 | Test all lookup patterns with production data before cutover | QA Team | Planned |

**Contingency**: Maintain a `card_xref` table as a denormalized view if relational JOIN performance is insufficient.

---

### R11: Security Regression -- Plaintext Password Migration (MEDIUM)

| Attribute | Value |
|-----------|-------|
| **Category** | Security |
| **Likelihood** | 3 (Possible) |
| **Impact** | 2 (Minor -- if mitigated) |
| **Risk Rating** | **6 (Medium)** |
| **Phase** | Phase 2 |
| **Affected Components** | COSGN00C, USRSEC VSAM, CSUSR01Y |

**Description**: The legacy USRSEC VSAM file stores passwords in plaintext (`SEC-USR-PWD PIC X(08)`). During migration, these must be hashed before loading into PostgreSQL. If the hashing step is missed or incorrectly implemented, plaintext passwords could be stored in the new system, or users could be locked out.

**Trigger Indicators**:
- Users cannot log in after migration
- Password field contains plaintext in `users` table
- Hash algorithm does not match Spring Security's expected format

**Mitigations**:

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M11.1 | Hash all passwords with bcrypt during data migration (never store plaintext) | Security Team | Planned |
| M11.2 | Implement "first login" password reset flow for migrated users | Dev Team | Planned |
| M11.3 | Security audit of `users` table after migration (no plaintext, no weak hashes) | Security Team | Planned |
| M11.4 | Enforce password complexity rules in the new system (min 12 chars, mixed case, etc.) | Dev Team | Planned |

**Contingency**: Force all users to reset passwords if any security concern arises during migration.

---

### R12: JCL Job Dependency Chain Breaks (LOW)

| Attribute | Value |
|-----------|-------|
| **Category** | Operational |
| **Likelihood** | 3 (Possible) |
| **Impact** | 1 (Negligible) |
| **Risk Rating** | **3 (Low)** |
| **Phase** | Phase 5 |
| **Affected Components** | 38 JCL jobs |

**Description**: The 38 JCL jobs have implicit dependencies (e.g., CLOSEFIL must run before POSTTRAN, OPENFIL must run after CREASTMT). These dependencies are encoded in the job scheduler (CA7/Control-M), not in the JCL itself. If Spring Batch jobs are not orchestrated with the same dependencies, batch processing may fail or produce incorrect results.

**Trigger Indicators**:
- Batch jobs run out of order
- Spring Batch job fails because prerequisite data is not ready
- Missing step in the orchestration chain

**Mitigations**:

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M12.1 | Document the complete JCL dependency chain from the job scheduler | Ops Team | Planned |
| M12.2 | Implement Spring Batch job orchestration with explicit step ordering | Dev Team | Planned |
| M12.3 | Use Spring Cloud Data Flow or AWS Step Functions for job dependency management | Architect | Planned |
| M12.4 | Eliminate unnecessary jobs (CLOSEFIL, OPENFIL, TRANIDX) that have no cloud equivalent | Architect | Planned |

**Contingency**: Run jobs manually in correct order until orchestration is automated.

---

### R13: Vendor Lock-In with Managed COBOL Runtime (MEDIUM)

| Attribute | Value |
|-----------|-------|
| **Category** | Strategic |
| **Likelihood** | 2 (Unlikely) |
| **Impact** | 4 (Major) |
| **Risk Rating** | **8 (Medium)** |
| **Phase** | Phase 4 (Statement Generation replatform) |
| **Affected Components** | CBSTM03A/B (replatformed on Micro Focus or AWS M2) |

**Description**: Replatforming CBSTM03A on a managed COBOL runtime (Micro Focus, AWS Mainframe Modernization) creates a dependency on that vendor. If the vendor raises prices, discontinues the product, or the runtime has limitations, the organization is locked in.

**Trigger Indicators**:
- Vendor price increase makes replatform cost-prohibitive
- Runtime limitation prevents needed feature enhancement
- Vendor announces end-of-life for managed runtime

**Mitigations**:

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M13.1 | Treat replatform as temporary (maximum 12 months) with a planned rewrite | Architect | Planned |
| M13.2 | Include "exit clause" in vendor contract for managed runtime | Program Mgr | Planned |
| M13.3 | Begin rewrite of statement generation in parallel with replatform deployment | Dev Team | Planned |
| M13.4 | Use the replatform period to create comprehensive test cases for the eventual rewrite | QA Team | Planned |

**Contingency**: Accelerate the Java rewrite of statement generation if vendor terms become unfavorable.

---

### R14: Scope Creep -- "While We're At It" Enhancements (MEDIUM)

| Attribute | Value |
|-----------|-------|
| **Category** | Organizational |
| **Likelihood** | 2 (Unlikely) |
| **Impact** | 3 (Moderate) |
| **Risk Rating** | **6 (Medium)** |
| **Phase** | All phases |

**Description**: During modernization, stakeholders may request functional enhancements ("while we're rewriting account management, let's add multi-currency support"). Each enhancement increases risk, timeline, and testing burden.

**Trigger Indicators**:
- New requirements appear during phase execution
- Timeline estimates grow beyond original projections
- Testing scope expands beyond legacy-parity validation

**Mitigations**:

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M14.1 | Strict "parity first" policy: new features only after legacy parity is achieved and validated | Program Mgr | Planned |
| M14.2 | Maintain a separate enhancement backlog; do not mix with migration tasks | Program Mgr | Planned |
| M14.3 | Each phase gate includes scope review: only planned items cut over | Program Mgr | Planned |

**Contingency**: Defer all enhancements to a post-migration release cycle.

---

### R15: Rollback Failure During Cutover (MEDIUM)

| Attribute | Value |
|-----------|-------|
| **Category** | Operational |
| **Likelihood** | 2 (Unlikely) |
| **Impact** | 3 (Moderate) |
| **Risk Rating** | **6 (Medium)** |
| **Phase** | Phase 3-6 |

**Description**: Each phase has a defined rollback procedure. If rollback is not tested or the rollback window is exceeded (e.g., too many transactions processed on the new system to replay on legacy), the organization may be unable to revert to the mainframe.

**Trigger Indicators**:
- Rollback drill fails during pre-cutover testing
- Transaction volume during cutover exceeds replay capacity
- VSAM files are out of sync and cannot be restored quickly

**Mitigations**:

| # | Mitigation | Owner | Status |
|---|-----------|-------|--------|
| M15.1 | Test rollback procedures in pre-production before every phase cutover | Ops Team | Planned |
| M15.2 | Maintain bi-directional data sync during dual-write phases | Data Team | Planned |
| M15.3 | Define a "point of no return" for each phase; until that point, rollback is guaranteed | Program Mgr | Planned |
| M15.4 | Keep mainframe LPAR available for 90 days post-final cutover | Ops Team | Planned |

**Contingency**: If rollback is needed after the "point of no return," perform forward recovery: fix the issue in the new system rather than reverting.

---

## Risk Summary Table

| ID | Risk | Category | L | I | Rating | Phase | Status |
|----|------|----------|---|---|--------|-------|--------|
| R01 | Financial calculation discrepancy | Data Integrity | 4 | 5 | **20 Critical** | 5 | Open |
| R02 | Data migration loss/corruption | Data Integrity | 4 | 4 | **16 Critical** | 0-3 | Open |
| R03 | Batch-online contention (dual-run) | Operational | 5 | 5 | **25 Critical** | 3-5 | Open |
| R04 | IMS HIDAM migration complexity | Technical | 3 | 4 | **12 High** | 6 | Open |
| R05 | CICS pseudo-conversational state | Technical | 4 | 3 | **12 High** | 2-3 | Open |
| R06 | MQ-to-modern-messaging migration | Integration | 5 | 4 | **20 Critical** | 6 | Open |
| R07 | Statement gen replatform failure | Technical | 3 | 5 | **15 High** | 4 | Open |
| R08 | Team skill gap (COBOL expertise) | Organizational | 4 | 3 | **12 High** | All | Open |
| R09 | Batch window overrun | Performance | 3 | 3 | **9 Medium** | 5 | Open |
| R10 | Cross-reference elimination | Data Integrity | 4 | 2 | **8 Medium** | 0-1 | Open |
| R11 | Plaintext password migration | Security | 3 | 2 | **6 Medium** | 2 | Open |
| R12 | JCL dependency chain breaks | Operational | 3 | 1 | **3 Low** | 5 | Open |
| R13 | Vendor lock-in (managed COBOL) | Strategic | 2 | 4 | **8 Medium** | 4 | Open |
| R14 | Scope creep | Organizational | 2 | 3 | **6 Medium** | All | Open |
| R15 | Rollback failure | Operational | 2 | 3 | **6 Medium** | 3-6 | Open |

---

## Risk Review Cadence

| Activity | Frequency | Participants |
|----------|-----------|-------------|
| Risk review meeting | Bi-weekly | Program Manager, Architect, Dev Leads |
| Risk rating re-assessment | Each phase gate | Full team |
| New risk identification | Continuous (any team member) | Logged in this register |
| Mitigation status update | Weekly | Mitigation owners |
| Post-incident risk update | After any incident | Program Manager + affected team |
