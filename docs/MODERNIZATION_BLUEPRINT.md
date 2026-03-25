# CardDemo Modernization Blueprint

> **Application:** AWS CardDemo -- Credit Card Management System
> **Target Platform:** Java / Spring Boot / Spring Batch / RDBMS
> **Source:** Static analysis of 29 COBOL programs (~20,650 LOC), 29 copybooks, 38 JCL jobs, 17 BMS maps

---

## 1. Strategy Definitions

| Strategy | Description | When to Use | Risk Level |
|----------|-------------|-------------|:----------:|
| **Strangler Fig** | Incrementally replace individual functions behind a facade/API gateway while the mainframe continues to run | Mixed online/batch workloads where modules can be isolated behind service interfaces | Low-Med |
| **Replatform** | Move code to a new runtime (e.g., COBOL on cloud, Micro Focus, AWS Blu Age) with minimal logic changes | Tight timelines; need to exit mainframe quickly without rewriting business logic | Low |
| **Refactor** | Restructure COBOL into cleaner modules, then auto-convert or manually translate to Java | Complex programs with embedded business rules that must be preserved exactly | Medium |
| **Rewrite** | Build equivalent functionality from scratch in Java/Spring using the COBOL as a specification | Simple programs or areas where the COBOL logic is outdated/non-performant | High |

---

## 2. Functional Area Assessment

### 2.1 Authentication & Session Management

| Component | Programs | LOC | Current Technology |
|-----------|----------|----:|-------------------|
| Sign-on | COSGN00C | 260 | CICS XCTL, USRSEC VSAM |
| Menu routing | COMEN01C, COADM01C | 596 | CICS XCTL with COMMAREA |
| User security record | CSUSR01Y copybook | -- | Plaintext passwords in VSAM |

**Recommended Strategy: Rewrite**

| Factor | Assessment |
|--------|------------|
| Complexity | Low (260 + 596 LOC, simple credential check + menu dispatch) |
| Business rules | Minimal -- username/password lookup, role-based menu routing |
| Security debt | Critical -- plaintext passwords must be replaced with bcrypt/scrypt |
| Reuse value | None -- CICS XCTL/COMMAREA patterns have no Java equivalent worth preserving |
| Target | Spring Security with JWT or session-based auth, BCrypt password encoder |
| Effort | 2-3 weeks |

**Rationale:** The sign-on and menu modules are small, have no complex business logic, and carry critical security debt (plaintext passwords). A clean rewrite with Spring Security provides immediate security improvement and establishes the authentication foundation for all other migrated modules.

---

### 2.2 Account Management

| Component | Programs | LOC | Current Technology |
|-----------|----------|----:|-------------------|
| Account View | COACTVWC | 941 | CICS READ, 4 VSAM files |
| Account Update | COACTUPC | 4,236 | CICS READ/REWRITE, complex validation |
| Account Read (batch) | CBACT01C | 430 | Batch sequential read |

**Recommended Strategy: Refactor + Strangler Fig**

| Factor | Assessment |
|--------|------------|
| Complexity | Extreme -- COACTUPC is 4,236 LOC with 800+ area code validations, 50+ state codes, 100+ state-zip combos |
| Business rules | Dense -- credit limit rules, FICO score handling, multi-field cross-validation |
| Data coupling | High -- reads/writes ACCTFILE, CUSTFILE, CARDXREF, CARDFILE |
| Target | Decompose into AccountService, CustomerService, ValidationService; expose as REST APIs |
| Effort | 8-12 weeks |

**Rationale:** COACTUPC is the single largest program (20% of total LOC). A direct rewrite risks losing embedded validation rules. Refactor first to extract validation tables (area codes, state codes, zip ranges) into configuration, then decompose into services behind a strangler facade. COACTVWC (view-only) can be rewritten directly as a simple REST GET endpoint.

**Decomposition Plan:**
1. Extract CSLKPCDY validation tables → reference data service or database lookup tables
2. Extract account update logic → AccountUpdateService
3. Extract customer update logic → CustomerUpdateService
4. Extract field validation → shared ValidationService
5. Wrap behind API gateway; CICS program calls new REST endpoints via adapter during transition

---

### 2.3 Card Management

| Component | Programs | LOC | Current Technology |
|-----------|----------|----:|-------------------|
| Card List | COCRDLIC | 1,459 | CICS STARTBR/READNEXT/READPREV |
| Card View | COCRDSLC | 887 | CICS READ |
| Card Update | COCRDUPC | 1,560 | CICS READ/REWRITE with change detection |
| Card Read (batch) | CBACT02C | 178 | Batch sequential read |

**Recommended Strategy: Strangler Fig**

| Factor | Assessment |
|--------|------------|
| Complexity | Medium-High -- COCRDLIC has bi-directional VSAM browse; COCRDUPC has optimistic locking |
| Business rules | Moderate -- card status management, expiry validation, embossed name rules |
| Data coupling | Medium -- CARDFILE + CUSTFILE (read), CARDXREF for lookup |
| Target | CardService with paginated search (Spring Data JPA), card CRUD REST API |
| Effort | 4-6 weeks |

**Rationale:** Card management is a cohesive bounded context with clear VSAM-to-table mapping. The STARTBR/READNEXT browse pattern maps cleanly to JPA paginated queries. Optimistic locking (read-compare-rewrite) maps to JPA `@Version`. The strangler approach lets the mainframe continue handling cards during transition while new REST APIs are validated.

---

### 2.4 Transaction Processing (Online)

| Component | Programs | LOC | Current Technology |
|-----------|----------|----:|-------------------|
| Transaction List | COTRN00C | 699 | CICS STARTBR/READNEXT |
| Transaction View | COTRN01C | 330 | CICS READ |
| Transaction Add | COTRN02C | 783 | CICS WRITE + CALL CSUTLDTC |
| Bill Payment | COBIL00C | 572 | CICS READ/WRITE/REWRITE |

**Recommended Strategy: Strangler Fig**

| Factor | Assessment |
|--------|------------|
| Complexity | Medium -- COTRN02C has multi-field validation and date checking; COBIL00C updates balances in real time |
| Business rules | Significant -- transaction ID generation (sequential), amount validation, date validation, real-time balance update |
| Data coupling | High -- TRANSACT (R/W), ACCTFILE (R/W), CARDXREF (R) |
| Financial risk | Critical -- incorrect posting affects customer balances |
| Target | TransactionService with @Transactional; payment REST API |
| Effort | 6-8 weeks |

**Rationale:** Online transaction processing has tight coupling to the account balance (COBIL00C does read-compute-rewrite on ACCTFILE). A strangler approach allows the new Java service to write to a shared database while the mainframe batch cycle is still operational. The date validation utility (CSUTLDTC → CEEDAYS) can be replaced with `java.time` APIs.

---

### 2.5 Transaction Processing (Batch)

| Component | Programs | LOC | Current Technology |
|-----------|----------|----:|-------------------|
| Daily posting | CBTRN02C | 731 | Batch, 6 VSAM files |
| Interest calculation | CBACT04C | 652 | Batch, 5 VSAM files |
| Combined transactions | COMBTRAN JCL | -- | SORT/MERGE utility |
| Transaction backup | TRANBKP JCL | -- | IDCAMS REPRO |

**Recommended Strategy: Refactor → Spring Batch**

| Factor | Assessment |
|--------|------------|
| Complexity | High -- CBTRN02C writes to 4 files simultaneously; CBACT04C has tiered interest rate lookups |
| Business rules | Critical -- posting validation, reject handling, interest tier computation, fee assessment |
| Financial risk | Highest -- this is the core financial engine; errors cascade through all downstream reporting |
| Data coupling | Very High -- reads/writes across 6 VSAM files per run |
| Target | Spring Batch jobs with chunk-oriented processing, JPA repositories, @Transactional |
| Effort | 10-14 weeks |

**Rationale:** The batch posting engine is the highest-risk component. COBOL's exact decimal arithmetic (`COMPUTE ROUNDED`, `PIC S9(10)V99`) must be preserved with `BigDecimal`. A refactor-first approach extracts the business rules into testable units, then translates them to Spring Batch steps with database transactions replacing multi-file VSAM writes. This eliminates the need for CLOSEFIL/OPENFIL file-level locking.

**Migration Sequence:**
1. Create parallel Spring Batch job writing to shadow database tables
2. Run COBOL and Java in parallel for 2+ cycles; reconcile outputs
3. Once outputs match to the cent, cut over to Java batch
4. Decommission JCL jobs: POSTTRAN, INTCALC, COMBTRAN, TRANBKP

---

### 2.6 Reporting & Statements

| Component | Programs | LOC | Current Technology |
|-----------|----------|----:|-------------------|
| Report submission | CORPT00C | 649 | CICS WRITEQ TD (internal reader) |
| Transaction report | CBTRN03C | 649 | Batch control-break reporting |
| Statement generation | CBSTM03A/B | 924+N | Batch, CALL sub-program, ALTER stmt |

**Recommended Strategy: Rewrite**

| Factor | Assessment |
|--------|------------|
| Complexity | Medium -- control-break reporting, page formatting, HTML generation |
| Business rules | Low -- formatting and aggregation; no financial calculations |
| Legacy debt | CBSTM03A uses deprecated `ALTER` statement |
| Target | SQL GROUP BY with JasperReports/Thymeleaf templates; Spring Batch for scheduling |
| Effort | 4-6 weeks |

**Rationale:** Reports are read-only consumers of transaction data. Once transactions are in an RDBMS, control-break reporting becomes trivial SQL (GROUP BY with ROLLUP). The deprecated `ALTER` statement in CBSTM03A makes refactoring more expensive than rewriting. HTML statement generation maps naturally to a template engine.

---

### 2.7 User Administration (CRUD)

| Component | Programs | LOC | Current Technology |
|-----------|----------|----:|-------------------|
| User List | COUSR00C | 695 | CICS STARTBR/READNEXT |
| User Add | COUSR01C | 299 | CICS WRITE |
| User Update | COUSR02C | 414 | CICS READ/REWRITE |
| User Delete | COUSR03C | 359 | CICS DELETE |

**Recommended Strategy: Rewrite**

| Factor | Assessment |
|--------|------------|
| Complexity | Low-Medium -- standard CRUD with browse |
| Business rules | Minimal -- user type validation (A/U), duplicate check |
| Target | Spring Data JPA UserRepository + REST controller |
| Effort | 2-3 weeks |

**Rationale:** This is textbook CRUD with a VSAM browse. Spring Data JPA + a REST controller replaces all four programs in a fraction of the code. The STARTBR/READNEXT pattern maps to paginated `findAll()`. Combine with the auth rewrite (2.1) to establish the user/security domain first.

---

### 2.8 Data Export/Import

| Component | Programs | LOC | Current Technology |
|-----------|----------|----:|-------------------|
| Export | CBEXPORT | 582 | Batch, 5 VSAM input → 1 output |
| Import | CBIMPORT | 487 | Batch, 1 input → 5 VSAM output |

**Recommended Strategy: Rewrite → Spring Batch**

| Factor | Assessment |
|--------|------------|
| Complexity | Medium -- multi-entity ETL with record type discrimination |
| Business rules | Low -- data marshaling/unmarshaling |
| Target | Spring Batch multi-step job with FlatFileItemReader/Writer or JSON/CSV |
| Effort | 2-3 weeks |

**Rationale:** Export/import are data migration utilities. Once VSAM files are replaced by database tables, these become Spring Batch jobs reading from/writing to the database. The multiplexed single-file format (record type in position 1) should be replaced with per-entity files or a modern format (JSON, CSV, Parquet).

---

### 2.9 File Management & Scheduling

| Component | JCL Jobs | Current Technology |
|-----------|----------|-------------------|
| File open/close | CLOSEFIL, OPENFIL | CICS CEMT commands |
| Data refresh | ACCTFILE, CARDFILE, CUSTFILE, XREFFILE, TRANFILE, DUSRSECJ | IDCAMS REPRO |
| Index management | TRANIDX | IDCAMS DEFINE AIX |
| GDG management | DEFGDGB, DEFGDGD | IDCAMS DEFINE GDG |
| Scheduling | CA7/Control-M configs | Mainframe scheduler |

**Recommended Strategy: Eliminate / Replatform**

| Factor | Assessment |
|--------|------------|
| Complexity | Low -- utility JCL, no business logic |
| Business rules | None -- infrastructure management |
| Target | RDBMS eliminates file-level locking; Spring Cloud Task or Quartz for scheduling |
| Effort | 1-2 weeks (absorbed into database migration) |

**Rationale:** CLOSEFIL/OPENFIL exist solely because VSAM files require exclusive access for batch. With an RDBMS providing row-level locking, these jobs are eliminated entirely. Data refresh jobs become database seed scripts or Flyway/Liquibase migrations. GDG management is replaced by database backup/retention policies.

---

### 2.10 Optional Modules

| Module | Programs | LOC | Current Technology |
|--------|----------|----:|-------------------|
| Authorization (IMS/DB2/MQ) | 8 programs | ~2,000 | IMS DB, DB2, MQ Series |
| Transaction Type DB2 | 3 programs | ~800 | Embedded SQL, DB2 cursors |
| VSAM-MQ | 2 programs | ~400 | MQ request/response |

**Recommended Strategy: Conditional (per module)**

| Module | Strategy | Rationale |
|--------|----------|-----------|
| Authorization | Replatform or Defer | Already uses DB2/MQ; if IMS/DB2 remain available, replatform. If decommissioning mainframe entirely, rewrite as microservice |
| Transaction Type DB2 | Refactor | Already SQL-based; embedded SQL → Spring Data JPA is straightforward |
| VSAM-MQ | Rewrite | Replace MQ request/response with REST APIs or event streaming (Kafka) |

---

## 3. Strategy Summary Matrix

| Functional Area | Strategy | Programs | LOC | Effort (weeks) | Risk | Priority |
|----------------|----------|:--------:|----:|:--------------:|:----:|:--------:|
| Authentication & Session | **Rewrite** | 3 | 856 | 2-3 | Low | P1 |
| User Administration | **Rewrite** | 4 | 1,767 | 2-3 | Low | P1 |
| Reporting & Statements | **Rewrite** | 3 | 2,222 | 4-6 | Low | P2 |
| Data Export/Import | **Rewrite** | 2 | 1,069 | 2-3 | Low | P2 |
| Card Management | **Strangler** | 4 | 4,084 | 4-6 | Med | P2 |
| Transaction (Online) | **Strangler** | 4 | 2,384 | 6-8 | Med-High | P3 |
| Account Management | **Refactor + Strangler** | 3 | 5,607 | 8-12 | High | P3 |
| Transaction (Batch) | **Refactor** | 2 | 1,383 | 10-14 | Highest | P4 |
| File Mgmt & Scheduling | **Eliminate** | 0 (JCL) | -- | 1-2 | Low | P4 |
| Optional Modules | **Varies** | 13 | ~3,200 | 4-8 | Med | P5 |
| **TOTAL** | | **38+** | **~22,572** | **44-66** | | |

---

## 4. Technology Target Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     API Gateway (Spring Cloud Gateway)       │
│            JWT Authentication + Rate Limiting                │
└────────────┬────────────┬────────────┬──────────────────────┘
             │            │            │
    ┌────────▼───┐  ┌─────▼─────┐  ┌──▼──────────┐
    │ Account    │  │ Card      │  │ Transaction │
    │ Service    │  │ Service   │  │ Service     │
    │ (Spring)   │  │ (Spring)  │  │ (Spring)    │
    └────────┬───┘  └─────┬─────┘  └──┬──────────┘
             │            │            │
    ┌────────▼────────────▼────────────▼──────────┐
    │              Shared Database                 │
    │   PostgreSQL / Aurora (replaces VSAM)        │
    │   Tables: accounts, cards, customers,        │
    │   transactions, card_xref, users             │
    └─────────────────────┬───────────────────────┘
                          │
    ┌─────────────────────▼───────────────────────┐
    │           Spring Batch Engine                │
    │   Jobs: posting, interest_calc, statements,  │
    │   reports, export/import                     │
    │   Scheduler: Quartz / Spring Cloud Task      │
    └─────────────────────────────────────────────┘
```

### Key Technology Mappings

| COBOL/Mainframe | Java/Spring Target |
|----------------|-------------------|
| CICS transactions | REST API endpoints (Spring MVC) |
| CICS COMMAREA | Request/response DTOs or session state |
| CICS XCTL/LINK | Service method calls or API gateway routing |
| CICS pseudo-conversational | Stateless REST (state in JWT/session) |
| BMS 3270 maps | React/Angular SPA or Thymeleaf server-side |
| VSAM KSDS files | PostgreSQL/Aurora tables with primary keys |
| VSAM STARTBR/READNEXT | JPA paginated queries (`Pageable`) |
| Copybook record layouts | Java POJOs/DTOs with JPA `@Entity` |
| PIC S9(10)V99 | `BigDecimal` with `RoundingMode.HALF_UP` |
| JCL batch jobs | Spring Batch jobs with `@Scheduled` or Quartz |
| IDCAMS utilities | Flyway/Liquibase migrations + DB admin |
| SORT/MERGE | SQL ORDER BY / application-level sort |
| GDG backups | Database backup policies (RDS snapshots) |
| CA7/Control-M | Quartz / Spring Cloud Task / Airflow |
| COBOL PERFORM | Java method calls |
| COBOL EVALUATE | Java switch expressions |
| COBOL COMPUTE ROUNDED | `BigDecimal.multiply().setScale(2, HALF_UP)` |
| CEE3ABD (abend) | `throw new RuntimeException()` + global handler |
| Assembler (COBDATFT) | `java.time.format.DateTimeFormatter` |
| Assembler (MVSWAIT) | `Thread.sleep()` or `ScheduledExecutorService` |
