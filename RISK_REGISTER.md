# CardDemo Risk Register

## Overview

This register documents the top risks for modernizing the CardDemo COBOL/CICS/VSAM mainframe application to a Java/Spring Boot target architecture. Each risk is assessed for likelihood, impact, and overall severity, with specific mitigations tied to the CardDemo codebase.

---

## Risk Scoring

| Score | Likelihood | Impact |
|---|---|---|
| 1 | Rare | Negligible |
| 2 | Unlikely | Minor |
| 3 | Possible | Moderate |
| 4 | Likely | Major |
| 5 | Almost Certain | Critical |

**Severity = Likelihood × Impact** (1–25 scale)

| Severity Range | Classification |
|---|---|
| 1–4 | Low |
| 5–9 | Medium |
| 10–15 | High |
| 16–25 | Critical |

---

## Risk Register

### R01: VSAM-to-Relational Data Migration Errors

| Attribute | Value |
|---|---|
| **Category** | Data |
| **Likelihood** | 4 (Likely) |
| **Impact** | 5 (Critical) |
| **Severity** | **20 (Critical)** |
| **Phase Affected** | Phase 0 (Foundation) |

**Description:**
VSAM files use fixed-length records with COBOL-specific data types (COMP, COMP-3, signed packed decimal PIC S9(10)V99) that require precise conversion. Seven core VSAM files (USRSEC 80B, ACCTDAT 300B, CARDDAT 150B, CUSTDAT 500B, TRANSACT 350B, CARDXREF 50B, TCATBAL 50B) must be migrated with zero data loss.

**Specific Concerns:**
- COMP-3 (packed decimal) fields like `ACCT-CURR-BAL PIC S9(10)V99` require exact conversion to `DECIMAL(12,2)` — rounding errors affect financial balances
- EBCDIC-to-ASCII character conversion for name fields (CUST-FIRST-NAME, CARD-EMBOSSED-NAME) may corrupt special characters
- VSAM KSDS key ordering (binary collation) differs from PostgreSQL default text collation — affects pagination behavior
- FILLER bytes in records (e.g., CVTRA06Y has 20-byte FILLER) may contain undocumented data

**Mitigations:**
1. Use sample data from `app/data/ASCII/` and `app/data/EBCDIC/` to build and validate conversion routines
2. Implement automated row-by-row comparison: read VSAM record → convert → write to DB → read back → compare with original
3. For COMP-3 fields, use a validated COBOL-to-Java decimal converter (e.g., JRecord or custom BigDecimal mapping)
4. Preserve VSAM key ordering in PostgreSQL by using appropriate collation or binary sort columns
5. Document and inspect all FILLER fields before migration — check for hidden data patterns

**Owner:** Data Migration Team
**Status:** Open

---

### R02: Financial Calculation Precision Loss

| Attribute | Value |
|---|---|
| **Category** | Financial |
| **Likelihood** | 4 (Likely) |
| **Impact** | 5 (Critical) |
| **Severity** | **20 (Critical)** |
| **Phase Affected** | Phase 4 (Financial Operations) |

**Description:**
CBACT04C (Interest Calculation, 653 lines) computes interest using COBOL arithmetic with implicit decimal scaling (PIC S9(09)V99). The COMPUTE statement in COBOL uses different rounding rules than Java's floating-point or even BigDecimal arithmetic. CBTRN02C (Transaction Posting) updates account balances by accumulating transaction amounts.

**Specific Concerns:**
- COBOL `COMPUTE` with PIC S9(09)V99 truncates (not rounds) by default — Java BigDecimal rounds HALF_UP by default
- Interest rate lookup in CBACT04C (1200-GET-INTEREST-RATE paragraph) uses DISCGRP file — rate precision and application method must be preserved exactly
- Transaction category balance accumulation in TCATBAL (PIC S9(09)V99) — cumulative rounding differences across thousands of transactions
- Bill payment (COBIL00C) updates ACCT-CURR-BAL — must match COBOL's signed decimal behavior

**Mitigations:**
1. Use `BigDecimal` with explicit `RoundingMode.DOWN` (matching COBOL truncation) for all financial calculations
2. Run parallel batch cycles: execute legacy CBACT04C and new Spring Batch interest calculator on identical data; compare account balances to the penny
3. Create a comprehensive test suite from `app/data/ASCII/` transaction data — compute expected interest manually and validate both systems
4. Document all COBOL COMPUTE statements and their Java equivalents with rounding mode annotations
5. Implement automated reconciliation: after every batch run, compare sum of all account balances between VSAM and PostgreSQL

**Owner:** Financial Engineering Team
**Status:** Open

---

### R03: CBSTM03A Non-Portable Constructs

| Attribute | Value |
|---|---|
| **Category** | Technical |
| **Likelihood** | 5 (Almost Certain) |
| **Impact** | 4 (Major) |
| **Severity** | **20 (Critical)** |
| **Phase Affected** | Phase 5 (Reporting & Decommission) |

**Description:**
CBSTM03A (Statement Generation, 924 lines) is the most technically complex program in the codebase. It uses mainframe-specific constructs that have no direct Java equivalent.

**Specific Constructs:**
- **ALTER/GO TO statements** — Dynamic flow control where `ALTER` changes the target of a `GO TO` at runtime. This is equivalent to self-modifying code and has no Java analogue.
- **PSA/TCB/TIOT control block addressing** — Directly reads mainframe system control blocks to obtain DD name information. These are z/OS internal data structures.
- **2D array processing** — `WS-TRNX-TABLE` with 51 cards × 10 transactions per card. Each entry is 334 bytes (16 + 10×(16+318)). Total working storage for this table alone is ~17KB.
- **COMP/COMP-3 variables** — Binary and packed decimal fields requiring exact conversion.
- **CBSTM03B subroutine** — Called via CALL with LINKAGE SECTION (LK-M03B-AREA) using operation codes (O/C/R/K/W/Z). This is a file I/O dispatcher that must be replaced with repository calls.

**Mitigations:**
1. Treat CBSTM03A as a **specification document**, not code to be converted — extract the business logic (what data goes where on the statement) and reimplement in Java
2. Map the 2D array (51×10) to a SQL GROUP BY query: `SELECT card_num, ... FROM transactions WHERE acct_id = ? GROUP BY card_num ORDER BY card_num LIMIT 51`
3. Replace ALTER/GO TO with a state machine or standard if/else control flow
4. Replace PSA/TCB/TIOT addressing with configuration-driven file paths or Spring resource resolution
5. Replace CBSTM03B's operation-code dispatch with Spring Data JPA repositories
6. Run parallel statement generation: compare legacy text/HTML output with new output field-by-field for 100+ accounts
7. Allocate 50% additional development time for this program (it is the single hardest conversion)

**Owner:** Reporting Team
**Status:** Open

---

### R04: Dual-Write Consistency During Migration

| Attribute | Value |
|---|---|
| **Category** | Data Integrity |
| **Likelihood** | 4 (Likely) |
| **Impact** | 4 (Major) |
| **Severity** | **16 (Critical)** |
| **Phase Affected** | Phase 3–4 (Write Operations, Financial) |

**Description:**
During Phases 3–4, write operations execute against both PostgreSQL (new) and VSAM (legacy) simultaneously. Any inconsistency between the two systems could cause data divergence, especially for financial records.

**Specific Concerns:**
- Network failures between Java service and VSAM sync layer could leave one system updated and the other stale
- COACTUPC (Account Update, 4237 lines) and batch programs (CBTRN02C, CBACT04C) may write to ACCTDAT concurrently — the dual-write path doubles the contention window
- Transaction ID generation: VSAM uses key-ordered sequential IDs; PostgreSQL uses database sequences — IDs may diverge
- CICS READ UPDATE / REWRITE is an atomic operation; dual-write makes it non-atomic

**Mitigations:**
1. Use **Change Data Capture (CDC)** rather than application-level dual-write where possible — let the database sync layer handle consistency
2. Implement a **reconciliation job** that runs every 15 minutes during dual-run: compare record counts and checksums between VSAM and PostgreSQL
3. For financial fields (balances, amounts), implement **write-through with verification**: write to PostgreSQL → sync to VSAM → read back from VSAM → compare
4. Use a **single source of truth** designation for each phase: during Phase 3, VSAM remains authoritative; during Phase 4, PostgreSQL becomes authoritative
5. Implement **compensating transactions** for any detected inconsistency: log the discrepancy, alert operations, and auto-correct from the authoritative source
6. Keep the dual-write window as short as possible per functional area (target: 2–4 weeks per service)

**Owner:** Platform Engineering Team
**Status:** Open

---

### R05: Batch Cycle Ordering and Dependency Breakage

| Attribute | Value |
|---|---|
| **Category** | Operational |
| **Likelihood** | 3 (Possible) |
| **Impact** | 5 (Critical) |
| **Severity** | **15 (High)** |
| **Phase Affected** | Phase 4 (Financial Operations) |

**Description:**
The CardDemo batch cycle has a strict execution order documented in the JCL:
```
CLOSEFIL → Data Refresh → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL
```
Breaking this ordering can cause data corruption (e.g., interest calculated on unposted transactions) or stale data (statements generated before transactions posted).

**Specific Dependencies:**
- POSTTRAN (CBTRN02C) must complete before INTCALC (CBACT04C) — interest calculation reads TCATBAL records written by posting
- INTCALC must complete before CREASTMT (CBSTM03A) — statements must include interest charges
- CLOSEFIL must run before any batch processing — prevents CICS online users from writing to files being processed by batch
- OPENFIL must run after all batch processing — re-enables CICS access

**Mitigations:**
1. Implement batch orchestration using **Spring Batch job flows** with explicit step dependencies matching the JCL sequence
2. Replace CLOSEFIL/OPENFIL with database-level coordination: use a `batch_lock` table or advisory locks to signal batch windows
3. Add **pre-condition checks** to each batch step: INTCALC verifies POSTTRAN completed successfully before starting
4. Implement **circuit breakers**: if POSTTRAN fails, automatically skip INTCALC and CREASTMT; alert operations
5. Log batch job execution order and timing; alert on any out-of-order execution
6. During parallel-run phase, run the Spring Batch cycle immediately after the legacy JCL cycle and compare results

**Owner:** Batch Operations Team
**Status:** Open

---

### R06: CICS Transaction Semantics Loss

| Attribute | Value |
|---|---|
| **Category** | Technical |
| **Likelihood** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Severity** | **12 (High)** |
| **Phase Affected** | Phase 3 (Write Operations) |

**Description:**
CICS provides pseudo-conversational transaction semantics that don't directly map to HTTP request/response. Programs like COACTUPC use `EXEC CICS RETURN TRANSID(...) COMMAREA(...)` to maintain state between screen interactions. The COMMAREA (COCOM01Y, 300+ bytes) carries session state including user identity, selected account/card, and program navigation context.

**Specific Concerns:**
- **Pseudo-conversational state**: CICS releases the task between user interactions; state is preserved only in COMMAREA. REST APIs are stateless — state must be managed differently.
- **EXEC CICS READ UPDATE**: acquires a record-level lock that is held until REWRITE or end of task. This lock prevents concurrent updates. In Spring Boot, the equivalent requires explicit optimistic or pessimistic locking.
- **EXEC CICS SYNCPOINT**: provides two-phase commit across multiple VSAM files. Spring's `@Transactional` provides single-database atomicity but not cross-resource transactions.
- **HANDLE CONDITION / ABEND**: CICS exception handling semantics differ from Java try/catch; some programs use HANDLE CONDITION to redirect control flow.

**Mitigations:**
1. Replace COMMAREA with **JWT claims** (user identity, role) and **API request parameters** (entity IDs) — eliminate server-side session state
2. Use **optimistic locking** (`@Version` annotation) to replace CICS READ UPDATE record locks — detect concurrent modifications at write time
3. For multi-file updates (e.g., COBIL00C updates both TRANSACT and ACCTDAT), use **database transactions** (`@Transactional`) to ensure atomicity within a single database
4. Map all HANDLE CONDITION paths to Java exceptions with appropriate HTTP status codes
5. Document every EXEC CICS command used in each program and its Java equivalent:

| CICS Command | Java Equivalent |
|---|---|
| READ FILE(...) INTO(...) RIDFLD(...) | `repository.findById(id)` |
| READ FILE(...) UPDATE | `repository.findById(id)` + `@Lock(PESSIMISTIC_WRITE)` |
| REWRITE FILE(...) FROM(...) | `repository.save(entity)` |
| STARTBR / READNEXT / ENDBR | `repository.findAll(Pageable)` |
| RETURN TRANSID(...) COMMAREA(...) | HTTP response + JWT + client-side state |
| XCTL PROGRAM(...) COMMAREA(...) | Internal service call or redirect |
| SEND MAP / RECEIVE MAP | REST API request/response |
| SYNCPOINT | `@Transactional` commit |

**Owner:** Application Architecture Team
**Status:** Open

---

### R07: Authentication Security Downgrade/Upgrade Risks

| Attribute | Value |
|---|---|
| **Category** | Security |
| **Likelihood** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Severity** | **12 (High)** |
| **Phase Affected** | Phase 2 (Authentication) |

**Description:**
The current USRSEC system stores plaintext passwords (SEC-USR-PWD PIC X(08)) with no hashing, no password complexity requirements, and an 8-character limit. Migration must upgrade security without disrupting existing users.

**Specific Concerns:**
- **Bidirectional password sync**: During dual-run, password changes must propagate between USRSEC (plaintext) and PostgreSQL (bcrypt). Syncing bcrypt → plaintext requires storing the plaintext temporarily.
- **8-character password limit**: Existing users have passwords like "PASSWORD" — new system must initially accept these but enforce stronger requirements for new passwords
- **Single-character role model**: CDEMO-USER-TYPE is 'A' (admin) or 'U' (user). New RBAC model must maintain backward compatibility with this binary role during transition.
- **No account lockout**: COSGN00C has no failed login attempt tracking — migration should add this without locking out users unexpectedly

**Mitigations:**
1. **One-way hash migration**: During initial data load, hash all existing plaintext passwords with bcrypt. Store only hashes in PostgreSQL. USRSEC retains plaintext during dual-run only.
2. **Password policy transition**: Accept legacy 8-char passwords for existing users; enforce 12+ char minimum for new users. Prompt password change on first login to new system.
3. **Role mapping**: Map 'A' → ROLE_ADMIN, 'U' → ROLE_USER in JWT claims. Add finer-grained roles post-migration.
4. **Gradual lockout**: Implement account lockout (5 failed attempts, 15-min cooldown) but only activate after notifying all users and providing a 2-week grace period.
5. **Eliminate bidirectional sync ASAP**: Minimize the dual-run window for authentication (target: 2 weeks). Once all clients authenticate via JWT, decommission USRSEC reads.
6. **Security audit**: Conduct a penetration test of the new IAM service before Phase 3 begins.

**Owner:** Security Team
**Status:** Open

---

### R08: Performance Degradation Under Load

| Attribute | Value |
|---|---|
| **Category** | Performance |
| **Likelihood** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Severity** | **9 (Medium)** |
| **Phase Affected** | Phase 3–4 (Write Operations, Financial) |

**Description:**
Mainframe CICS/VSAM provides extremely fast in-memory record access (~1ms reads). Java/Spring Boot with PostgreSQL introduces network latency, ORM overhead, and connection pool management that may degrade response times for high-throughput operations.

**Specific Concerns:**
- COACTUPC (4237 lines) performs multiple VSAM reads/writes per screen interaction — each becomes a database round-trip
- Batch processing (CBTRN02C, CBACT04C) reads/writes thousands of records sequentially — ORM overhead per record may extend batch windows
- Paginated browse (STARTBR/READNEXT in COCRDLIC, COTRN00C) must scan indexes — database pagination performance depends on index design
- The data sync layer (CDC) during dual-run adds write latency

**Mitigations:**
1. **Database indexing**: Create indexes matching all VSAM alternate indexes (CARDAIX, CXACAIX) plus additional indexes for pagination queries
2. **Batch processing optimization**: Use Spring Batch chunk-based processing with configurable chunk sizes (start with 100); use batch INSERT/UPDATE statements instead of individual calls
3. **Connection pooling**: Configure HikariCP with appropriate pool size (min 10, max 50) based on load testing
4. **Caching**: Use Redis for read-heavy data (customer records, card cross-references) with short TTL during dual-run
5. **Load testing**: Conduct performance tests with production-equivalent data volumes before each phase go-live
6. **Performance budget**: Set response time targets (p95 < 200ms for online, batch within 150% of legacy window) and alert on breaches

**Owner:** Performance Engineering Team
**Status:** Open

---

### R09: Knowledge and Skill Gap

| Attribute | Value |
|---|---|
| **Category** | Organizational |
| **Likelihood** | 4 (Likely) |
| **Impact** | 3 (Moderate) |
| **Severity** | **12 (High)** |
| **Phase Affected** | All Phases |

**Description:**
The modernization requires expertise in both legacy COBOL/CICS/VSAM systems and modern Java/Spring Boot architecture. Understanding programs like CBSTM03A (ALTER/GO TO, control blocks) and CBACT04C (interest calculation logic) requires deep mainframe knowledge that is increasingly scarce.

**Specific Concerns:**
- 31 COBOL programs with varying complexity (261 to 4237 lines) require business logic extraction by developers who understand COBOL semantics
- COMP-3 (packed decimal) and COMP (binary) data types have subtle conversion behaviors
- CICS pseudo-conversational programming model is unfamiliar to most Java developers
- JCL batch job dependencies and GDG concepts have no direct Spring Boot equivalent
- BMS screen map layouts (17 maps) need UI/UX redesign expertise

**Mitigations:**
1. **Pair programming**: Pair COBOL-experienced developers with Java developers for each functional area extraction
2. **Knowledge capture**: Document all COBOL programs with line-by-line business logic annotations before development begins
3. **Training program**: Provide Java team with a COBOL/CICS/VSAM fundamentals course (1 week) before Phase 1
4. **Reference implementation**: Build Account View (COACTVWC, 942 lines) as the reference conversion — it covers read operations, pagination, cross-reference lookups, and BMS screen mapping
5. **Automated conversion aids**: Use tools like JRecord for COBOL copybook → Java class generation; validate output against `app/data/ASCII/` sample data
6. **Retain COBOL expertise**: Ensure at least 2 COBOL-experienced developers are available through Phase 5

**Owner:** Program Management
**Status:** Open

---

### R10: Incomplete Business Rule Extraction

| Attribute | Value |
|---|---|
| **Category** | Functional |
| **Likelihood** | 3 (Possible) |
| **Impact** | 4 (Major) |
| **Severity** | **12 (High)** |
| **Phase Affected** | Phase 3–4 |

**Description:**
COBOL programs embed business rules in procedural code without clear separation. COACTUPC (4237 lines) contains hundreds of field-level validation rules, date calculations, and business logic that must be extracted completely.

**Specific Concerns:**
- **Implicit rules in EVALUATE/IF chains**: COACTUPC validates account status transitions, credit limit ranges, and date relationships in deeply nested IF statements
- **Copybook-embedded constants**: Values like transaction type codes (PIC X(02)) and category codes (PIC 9(04)) may have business meaning not documented in code
- **88-level conditions**: Boolean conditions like `CDEMO-USRTYP-ADMIN VALUE 'A'` define business rules compactly — easy to miss during extraction
- **Cross-program rules**: Some validations span programs (e.g., COBIL00C checks account status before allowing bill payment — same status values checked by COACTUPC)
- **Undocumented edge cases**: COBOL programs may handle edge cases (zero amounts, boundary dates, maximum values) differently than Java defaults

**Mitigations:**
1. **Systematic extraction**: For each COBOL program, create a rules spreadsheet listing every IF/EVALUATE condition, the fields involved, and the business meaning
2. **Test case generation**: For COACTUPC, generate test cases from `app/data/ASCII/` that exercise every validation path; run against both legacy and new system
3. **88-level audit**: Extract all 88-level conditions from all copybooks and programs; ensure each is mapped to a Java enum or constant
4. **Cross-reference validation**: Build a matrix of which programs validate which fields; ensure validation is consistent in the new system
5. **Code coverage**: Achieve >95% branch coverage in Java tests for any program with financial impact (COBIL00C, CBTRN02C, CBACT04C)
6. **Business stakeholder review**: Have business analysts review the extracted rules for each domain before implementation

**Owner:** Business Analysis Team
**Status:** Open

---

### R11: Rollback Complexity Escalation

| Attribute | Value |
|---|---|
| **Category** | Operational |
| **Likelihood** | 2 (Unlikely) |
| **Impact** | 5 (Critical) |
| **Severity** | **10 (High)** |
| **Phase Affected** | Phase 4–5 |

**Description:**
As the migration progresses through phases, rollback becomes increasingly complex. By Phase 4, financial transactions may exist only in PostgreSQL. A full rollback to CICS/VSAM would require reverse-migrating all data created during the new system's operation.

**Specific Concerns:**
- Transactions created via new API (Phase 3.3) may use database-sequence IDs that don't map to VSAM key space
- Bill payments (Phase 4.1) processed in PostgreSQL would need compensating entries in VSAM
- Statement generation (Phase 5.1) may have produced outputs in a new format that legacy code cannot reproduce
- Batch job state (Spring Batch job repository) has no VSAM equivalent

**Mitigations:**
1. **Point of no return declaration**: Explicitly define the Phase 4 exit as the "point of no return" — after Phase 4 completion, forward-only (no rollback to VSAM)
2. **Data archival**: Before declaring point of no return, take a full VSAM backup that could be restored to a clean mainframe environment
3. **Incremental rollback design**: Each phase's rollback only needs to cover that phase's changes, not the entire migration
4. **Dry-run rollback**: Test rollback procedures for Phases 1–3 in a staging environment before go-live
5. **Runbook documentation**: Create step-by-step rollback runbooks for each phase; practice them quarterly
6. **Feature flag infrastructure**: Ensure feature flags can disable new services and reroute to legacy within minutes (Phases 1–3)

**Owner:** Operations Team
**Status:** Open

---

### R12: CARDXREF Cross-Context Coupling

| Attribute | Value |
|---|---|
| **Category** | Architecture |
| **Likelihood** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Severity** | **9 (Medium)** |
| **Phase Affected** | Phase 3 (Write Operations) |

**Description:**
The CARDXREF file (CVACT03Y: XREF-CARD-NUM → XREF-CUST-ID → XREF-ACCT-ID) is accessed by programs across 5 different bounded contexts (Account, Card, Transaction, Bill Payment, Reporting). In the target microservices architecture, ownership of this cross-reference data must be clearly assigned.

**Specific Programs Accessing CARDXREF:**
- COACTVWC (Account View) — reads via CXACAIX alternate index
- COCRDLIC (Card List) — filters by account
- CBTRN02C (Transaction Posting) — validates card against account
- COBIL00C (Bill Payment) — looks up account from card
- CBSTM03A (Statement Generation) — cross-references all entities

**Mitigations:**
1. **Assign ownership**: Account Service owns the `card_account_xref` table (it is the natural owner of the card-account-customer relationship)
2. **API-based access**: Other services query cross-reference data via Account Service API, not direct database access
3. **Materialized view**: For performance-critical consumers (batch processing), provide a read-only materialized view or cache of cross-reference data
4. **Event-driven sync**: When cross-reference data changes, publish a domain event for downstream consumers to update their local caches
5. **Migration order**: Migrate Account Service (Phase 3.2) before Card Service (Phase 3.1) to ensure cross-reference ownership is established first

**Owner:** Application Architecture Team
**Status:** Open

---

### R13: EBCDIC/ASCII Data Encoding Issues

| Attribute | Value |
|---|---|
| **Category** | Data |
| **Likelihood** | 3 (Possible) |
| **Impact** | 2 (Minor) |
| **Severity** | **6 (Medium)** |
| **Phase Affected** | Phase 0 (Foundation) |

**Description:**
Mainframe data is stored in EBCDIC encoding. The `app/data/EBCDIC/` directory contains production-format data, while `app/data/ASCII/` contains converted data. Character fields (customer names, merchant names, addresses) may contain characters that convert differently between code pages.

**Mitigations:**
1. Use `app/data/ASCII/` files as the primary migration source — they are pre-converted
2. For EBCDIC sources, use IBM code page 037 (US English) as the default conversion table
3. Build a validation report comparing character-by-character conversion for all name/address fields
4. Test with edge cases: accented characters, special characters in merchant names, maximum-length fields
5. Implement a character validation filter that flags non-ASCII characters in converted data

**Owner:** Data Migration Team
**Status:** Open

---

### R14: Batch Window Overrun

| Attribute | Value |
|---|---|
| **Category** | Operational |
| **Likelihood** | 3 (Possible) |
| **Impact** | 3 (Moderate) |
| **Severity** | **9 (Medium)** |
| **Phase Affected** | Phase 4 (Financial Operations) |

**Description:**
The legacy batch cycle (CLOSEFIL → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL) runs within a defined maintenance window. Spring Batch processing may exceed this window due to ORM overhead, database round-trips, and the elimination of VSAM's in-memory performance.

**Mitigations:**
1. **Parallel processing**: Use Spring Batch partitioning to process accounts in parallel (the legacy batch is sequential)
2. **Chunk optimization**: Tune chunk sizes (commit intervals) for each batch step based on load testing
3. **Eliminate CLOSEFIL/OPENFIL**: With a relational database, online and batch can run concurrently — this alone may recover enough time to absorb other overhead
4. **Database tuning**: Use bulk INSERT/UPDATE statements, tune PostgreSQL shared_buffers and work_mem for batch workloads
5. **Monitoring**: Implement batch step duration tracking; alert if any step exceeds 120% of expected duration
6. **Fallback**: If Spring Batch cannot meet the window, run critical financial steps (POSTTRAN, INTCALC) as optimized SQL procedures

**Owner:** Batch Operations Team
**Status:** Open

---

### R15: Third-Party and Optional Module Complexity

| Attribute | Value |
|---|---|
| **Category** | Technical |
| **Likelihood** | 2 (Unlikely) |
| **Impact** | 3 (Moderate) |
| **Severity** | **6 (Medium)** |
| **Phase Affected** | Phase 5 |

**Description:**
Three optional modules extend CardDemo with middleware dependencies:
1. **Authorization (IMS/DB2/MQ)**: 8 programs using IMS DB, DB2, and MQ Series
2. **Transaction Type DB2**: 3 programs with embedded SQL (EXEC SQL cursors)
3. **VSAM-MQ**: 2 programs using MQ request/response patterns

These modules introduce middleware dependencies (IMS DB, DB2, MQ Series) that are not present in the core application.

**Mitigations:**
1. **Defer to Phase 5**: Treat optional modules as post-core-migration scope
2. **IMS DB → JPA**: Map IMS segments to JPA entities with hierarchical relationships
3. **DB2 embedded SQL → JPQL**: Convert EXEC SQL / DECLARE CURSOR to Spring Data JPA queries
4. **MQ → Spring JMS/Kafka**: Replace MQ request/response with REST APIs or async messaging
5. **Feature toggle**: Deploy optional module replacements behind feature flags for gradual rollout
6. **Evaluate necessity**: Some optional modules may be obsolete — confirm with stakeholders before investing in migration

**Owner:** Integration Team
**Status:** Open

---

## Risk Heat Map

```
Impact →    1         2         3         4         5
         Negligible  Minor   Moderate   Major    Critical
    5    │         │         │         │  R03    │         │  Likelihood:
Almost   │         │         │         │         │         │  Almost Certain
Certain  │─────────│─────────│─────────│─────────│─────────│
    4    │         │         │  R09    │  R04    │ R01,R02 │
Likely   │         │         │         │         │         │
         │─────────│─────────│─────────│─────────│─────────│
    3    │         │  R13    │ R08,R12 │R06,R07  │  R05    │
Possible │         │         │  R14    │  R10    │         │
         │─────────│─────────│─────────│─────────│─────────│
    2    │         │         │  R15    │         │  R11    │
Unlikely │         │         │         │         │         │
         │─────────│─────────│─────────│─────────│─────────│
    1    │         │         │         │         │         │
Rare     │         │         │         │         │         │
         └─────────┴─────────┴─────────┴─────────┴─────────┘
```

---

## Risk Summary (Sorted by Severity)

| ID | Risk | Severity | Category | Phase |
|---|---|---|---|---|
| **R01** | VSAM-to-Relational Data Migration Errors | **20 Critical** | Data | 0 |
| **R02** | Financial Calculation Precision Loss | **20 Critical** | Financial | 4 |
| **R03** | CBSTM03A Non-Portable Constructs | **20 Critical** | Technical | 5 |
| **R04** | Dual-Write Consistency During Migration | **16 Critical** | Data Integrity | 3–4 |
| **R05** | Batch Cycle Ordering and Dependency Breakage | **15 High** | Operational | 4 |
| **R06** | CICS Transaction Semantics Loss | **12 High** | Technical | 3 |
| **R07** | Authentication Security Downgrade/Upgrade | **12 High** | Security | 2 |
| **R09** | Knowledge and Skill Gap | **12 High** | Organizational | All |
| **R10** | Incomplete Business Rule Extraction | **12 High** | Functional | 3–4 |
| **R11** | Rollback Complexity Escalation | **10 High** | Operational | 4–5 |
| **R08** | Performance Degradation Under Load | **9 Medium** | Performance | 3–4 |
| **R12** | CARDXREF Cross-Context Coupling | **9 Medium** | Architecture | 3 |
| **R14** | Batch Window Overrun | **9 Medium** | Operational | 4 |
| **R13** | EBCDIC/ASCII Data Encoding Issues | **6 Medium** | Data | 0 |
| **R15** | Third-Party and Optional Module Complexity | **6 Medium** | Technical | 5 |
