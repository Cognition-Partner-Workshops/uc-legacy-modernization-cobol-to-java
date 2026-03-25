# CardDemo Modernization Blueprint

## Executive Summary

This blueprint evaluates four modernization strategies -- **Strangler Fig**, **Replatform**, **Refactor**, and **Rewrite** -- for each functional area of the AWS CardDemo mainframe application. CardDemo is a COBOL/CICS/VSAM credit card management system comprising 31 online and batch programs, 30 copybooks, 17 BMS screen maps, and 38 JCL batch jobs. The recommended approach is a **domain-driven hybrid strategy** that applies the most appropriate pattern per functional area, executed through a phased Strangler Fig envelope.

---

## 1. Application Inventory

### 1.1 Online CICS Programs (14 programs)

| Program    | Transaction | Function                        | LOC   | Complexity |
|------------|-------------|---------------------------------|-------|------------|
| COSGN00C   | CC00        | User sign-on / authentication   | 261   | Low        |
| COMEN01C   | CM00        | Main menu (regular users)       | 309   | Low        |
| COADM01C   | CA00        | Admin menu                      | 289   | Low        |
| COACTVWC   | CAVW        | Account view                    | 942   | Medium     |
| COACTUPC   | CAUP        | Account update                  | 4,237 | High       |
| COCRDLIC   | CCLI        | Credit card list                | 1,460 | Medium     |
| COCRDSLC   | CCDL        | Credit card view                | ~800  | Medium     |
| COCRDUPC   | CCUP        | Credit card update              | ~1,200| Medium     |
| COTRN00C   | CT00        | Transaction list                | ~900  | Medium     |
| COTRN01C   | CT01        | Transaction view                | ~700  | Medium     |
| COTRN02C   | CT02        | Transaction add                 | 784   | Medium     |
| CORPT00C   | CR00        | Transaction reports (JCL submit) | 650   | Medium     |
| COBIL00C   | CB00        | Bill payment                    | 573   | Medium     |
| COUSR00C-03C | CU00-CU03 | User CRUD (admin only)         | ~2,400| Medium     |

### 1.2 Batch Programs (10 programs)

| Program    | Function                                      | LOC   | Complexity |
|------------|-----------------------------------------------|-------|------------|
| CBTRN02C   | Daily transaction posting                     | 732   | High       |
| CBACT04C   | Interest calculation                          | 653   | High       |
| CBSTM03A   | Statement generation (text + HTML)             | 924   | High       |
| CBSTM03B   | Statement sub-routine (VSAM I/O helper)       | ~400  | Medium     |
| CBTRN01C   | Transaction file processing                   | ~500  | Medium     |
| CBTRN03C   | Transaction report generation                 | ~500  | Medium     |
| CBACT01C   | Account file processing                       | ~300  | Low        |
| CBACT02C   | Account data refresh                          | ~300  | Low        |
| CBACT03C   | Account data maintenance                      | ~300  | Low        |
| CBEXPORT/CBIMPORT | Data export/import utilities            | ~600  | Low        |

### 1.3 Data Stores (VSAM KSDS Files)

| VSAM File  | Copybook  | Record Length | Key             | Description              |
|------------|-----------|---------------|-----------------|--------------------------|
| ACCTDAT    | CVACT01Y  | 300 bytes     | ACCT-ID (9(11)) | Account master           |
| CARDDAT    | CVACT02Y  | 150 bytes     | CARD-NUM (X(16))| Card master              |
| CUSTDAT    | CVCUS01Y  | 500 bytes     | CUST-ID (9(09)) | Customer master          |
| CCXREF     | CVACT03Y  | 50 bytes      | XREF-CARD-NUM   | Card-account cross-ref   |
| TRANSACT   | CVTRA05Y  | 350 bytes     | TRAN-ID (X(16)) | Transaction master       |
| USRSEC     | CSUSR01Y  | 80 bytes      | SEC-USR-ID      | User security            |
| DALYTRAN   | CVTRA06Y  | 350 bytes     | Sequential       | Daily transaction input  |
| TCATBALF   | CVTRA01Y  | 50 bytes      | Composite key   | Transaction category bal |
| DISCGRP    | CVTRA02Y  | 50 bytes      | Composite key   | Disclosure/interest rate |

### 1.4 Optional Modules

| Module                          | Technology           | Programs | Description                     |
|---------------------------------|----------------------|----------|---------------------------------|
| Authorization (IMS/DB2/MQ)      | IMS DB, DB2, MQ      | 8        | Fraud detection, auth summaries |
| Transaction Type (DB2)          | DB2, embedded SQL    | 3        | Transaction type CRUD           |
| VSAM-MQ                        | VSAM, MQ             | 2        | MQ request/response services    |

---

## 2. Functional Area Identification

Based on code analysis, the application decomposes into **seven functional areas**:

| # | Functional Area              | Programs                                                    | Data Stores                           |
|---|------------------------------|-------------------------------------------------------------|---------------------------------------|
| 1 | **Authentication & Security** | COSGN00C, COUSR00C-03C                                     | USRSEC                                |
| 2 | **Account Management**       | COACTVWC, COACTUPC, CBACT01C-03C                           | ACCTDAT, CUSTDAT, CCXREF              |
| 3 | **Card Management**          | COCRDLIC, COCRDSLC, COCRDUPC                               | CARDDAT, CCXREF, CARDAIX              |
| 4 | **Transaction Processing**   | COTRN00C-02C, CBTRN01C-02C                                 | TRANSACT, DALYTRAN, TCATBALF, CCXREF  |
| 5 | **Financial Operations**     | COBIL00C, CBACT04C                                         | ACCTDAT, TRANSACT, DISCGRP, TCATBALF  |
| 6 | **Reporting & Statements**   | CORPT00C, CBTRN03C, CBSTM03A/B                            | TRANSACT, ACCTDAT, CUSTDAT, CCXREF    |
| 7 | **Navigation & UI Framework**| COMEN01C, COADM01C                                         | (COMMAREA only)                       |

---

## 3. Strategy Evaluation per Functional Area

### 3.1 Authentication & Security

**Current State:** Simple VSAM KSDS lookup (USRSEC file), plaintext password comparison, role-based routing (Admin vs User). 80-byte fixed-length user records. 4 CRUD programs for user management.

| Strategy       | Fit   | Rationale |
|----------------|-------|-----------|
| **Strangler**  | Fair  | Could wrap existing auth behind a facade, but the security model is too primitive to preserve. |
| **Replatform** | Poor  | The plaintext password storage and simplistic auth model cannot be lifted as-is. |
| **Refactor**   | Poor  | The existing COBOL is too simple to justify incremental restructuring. |
| **Rewrite**    | **Best** | Replace with Spring Security + JWT/OAuth2. Modern security standards require a clean break. |

**Recommendation: REWRITE** -- Replace with Spring Security using BCrypt password hashing, JWT tokens, and role-based access control. This is non-negotiable for production security compliance.

---

### 3.2 Account Management

**Current State:** COACTVWC (942 LOC) provides read-only account view. COACTUPC (4,237 LOC) is the largest and most complex program -- handles account updates with extensive field validation (SSN, phone, dates, credit limits, FICO scores), reads across ACCTDAT, CUSTDAT, and CCXREF files. Multiple REDEFINES for data manipulation.

| Strategy       | Fit   | Rationale |
|----------------|-------|-----------|
| **Strangler**  | Good  | Can intercept CICS calls and route to new service while legacy runs in parallel. |
| **Replatform** | Fair  | VSAM-to-RDBMS mapping is straightforward but validation logic in COACTUPC is deeply coupled to BMS screen handling. |
| **Refactor**   | **Best** | The business rules (validation, cross-file lookups) are valuable and well-structured. Extract domain logic from CICS coupling. |
| **Rewrite**    | Fair  | High risk of losing embedded business rules in 4,237 LOC of COACTUPC. |

**Recommendation: REFACTOR** -- Extract the validation and business rules from COACTUPC into a Java domain service layer, systematically translating COBOL data validation logic (SSN format, date checks, credit limit rules) into Java Bean Validation annotations and service methods. Map VSAM records to JPA entities.

---

### 3.3 Card Management

**Current State:** COCRDLIC handles paginated card listing with browse/select functionality (STARTBR/READNEXT/ENDBR patterns). COCRDSLC provides card detail view. COCRDUPC handles card updates. All share CARDDAT and CCXREF via alternate index paths (CARDAIX, CXACAIX).

| Strategy       | Fit   | Rationale |
|----------------|-------|-----------|
| **Strangler**  | **Best** | Card management is a clean bounded context with well-defined VSAM I/O boundaries. Can be intercepted at the CICS transaction level. |
| **Replatform** | Good  | VSAM browse patterns map well to SQL cursor/pagination. |
| **Refactor**   | Good  | Moderate complexity, clear separation from other areas. |
| **Rewrite**    | Fair  | Unnecessary risk; existing logic is well-structured. |

**Recommendation: STRANGLER FIG** -- Implement a new Card Management microservice (Spring Boot + JPA) behind an API gateway. Route CICS transactions to the new service while maintaining the VSAM data sync during transition. The paginated browse pattern maps naturally to Spring Data pagination.

---

### 3.4 Transaction Processing

**Current State:** The most interconnected area. Online programs (COTRN00C-02C) handle transaction list/view/add via CICS. Batch program CBTRN02C (732 LOC) is the core posting engine -- reads daily transactions (DALYTRAN), validates against CCXREF and ACCTDAT, posts to TRANSACT, updates account balances, writes rejects to DALYREJS, and maintains category balances (TCATBALF). This is the **financial heart of the system**.

| Strategy       | Fit   | Rationale |
|----------------|-------|-----------|
| **Strangler**  | Fair  | Complex data dependencies across 6 files make parallel running risky for financial accuracy. |
| **Replatform** | Fair  | Batch posting logic relies on sequential file processing patterns specific to mainframe. |
| **Refactor**   | **Best** | The posting logic, validation rules, and reject handling are critical business IP that must be preserved precisely. |
| **Rewrite**    | Poor  | Highest risk area. Financial posting errors could cause monetary loss. |

**Recommendation: REFACTOR** -- Carefully extract CBTRN02C posting logic into a Spring Batch job with explicit step definitions: (1) Read daily transactions, (2) Validate via cross-reference lookup, (3) Post to transaction store, (4) Update account balances, (5) Write rejects. Each COBOL paragraph maps to a discrete Spring Batch ItemProcessor/ItemWriter. Online transaction screens refactor to REST endpoints.

---

### 3.5 Financial Operations (Bill Payment & Interest Calculation)

**Current State:** COBIL00C (573 LOC) handles online bill payment -- reads account, finds highest transaction ID, creates payment transaction record, and updates account balance in a pseudo-atomic CICS operation. CBACT04C (653 LOC) is the interest calculator -- iterates TCATBALF sequentially, looks up discount group interest rates (DISCGRP), computes monthly interest, and rewrites account balances.

| Strategy       | Fit   | Rationale |
|----------------|-------|-----------|
| **Strangler**  | Fair  | Bill payment touches TRANSACT + ACCTDAT atomically; hard to intercept mid-flow. |
| **Replatform** | Fair  | Interest calculation has mainframe-specific sequential file processing patterns. |
| **Refactor**   | **Best** | Financial algorithms (interest computation, balance updates) are precise business logic worth preserving. |
| **Rewrite**    | Poor  | Risk of introducing rounding errors or calculation discrepancies. |

**Recommendation: REFACTOR** -- Translate COBIL00C bill payment flow into a transactional Spring service with database-level atomicity (replacing CICS pseudo-conversational pattern). Convert CBACT04C interest calculation to a Spring Batch job with BigDecimal arithmetic to preserve COBOL's fixed-point precision (PIC S9(09)V99 maps to BigDecimal with scale 2).

---

### 3.6 Reporting & Statements

**Current State:** CORPT00C submits batch report jobs via CICS TDQ (extra-partition transient data queue) to the internal reader. CBSTM03A (924 LOC) is the most technically complex program -- uses ALTER/GO TO, pointer-based PSA/TCB/TIOT addressing, 2D arrays (51 cards x 10 transactions), COMP/COMP-3 variables, and calls CBSTM03B as a subroutine. Generates both plain text and HTML statements.

| Strategy       | Fit   | Rationale |
|----------------|-------|-----------|
| **Strangler**  | Good  | Reports are read-only; can run new reporting in parallel without data conflicts. |
| **Replatform** | Fair  | The ALTER/GO TO and pointer-based addressing in CBSTM03A cannot be automatically converted. |
| **Refactor**   | Fair  | The program deliberately uses archaic patterns for modernization testing. |
| **Rewrite**    | **Best** | CBSTM03A's ALTER statements, pointer arithmetic, and 2D arrays are anti-patterns that resist automated conversion. Modern reporting frameworks (JasperReports, HTML templating) are vastly superior. |

**Recommendation: REWRITE** -- Replace with a modern reporting service using Spring Batch for data aggregation and a templating engine (Thymeleaf/JasperReports) for output. The archaic COBOL patterns (ALTER, pointer-based addressing) have no Java equivalent and were deliberately included as modernization challenges. The JCL submission via TDQ replaces with scheduled Spring Batch jobs or event-driven triggers.

---

### 3.7 Navigation & UI Framework

**Current State:** COMEN01C and COADM01C are menu-driven navigation programs using BMS maps. They route to functional programs via XCTL with COMMAREA passing. 17 BMS maps define 3270 terminal screens. The COMMAREA (COCOM01Y) carries session state including user context, navigation history, and entity identifiers.

| Strategy       | Fit   | Rationale |
|----------------|-------|-----------|
| **Strangler**  | Good  | The UI layer is the natural Strangler entry point -- intercept at the presentation tier. |
| **Replatform** | Poor  | BMS/3270 terminal UI has no meaningful equivalent in modern web frameworks. |
| **Refactor**   | Poor  | BMS maps are declarative screen definitions, not business logic. |
| **Rewrite**    | **Best** | Replace 3270 terminal UI with a modern web frontend. No business logic resides here. |

**Recommendation: REWRITE** -- Replace with a React/Angular SPA or server-side rendered web application. BMS map field definitions serve as specifications for form layouts. COMMAREA session state replaces with server-side session or JWT claims. Menu routing replaces with client-side routing. This is the **Strangler Fig facade** -- the new web UI calls new Java services while the mainframe is progressively decommissioned.

---

## 4. Strategy Summary Matrix

| Functional Area             | Strategy        | Risk  | Effort | Priority |
|-----------------------------|-----------------|-------|--------|----------|
| Authentication & Security   | **Rewrite**     | Low   | Low    | Phase 1  |
| Navigation & UI Framework   | **Rewrite**     | Low   | Medium | Phase 1  |
| Card Management             | **Strangler**   | Low   | Medium | Phase 2  |
| Account Management          | **Refactor**    | Medium| High   | Phase 3  |
| Transaction Processing      | **Refactor**    | High  | High   | Phase 4  |
| Financial Operations        | **Refactor**    | High  | High   | Phase 4  |
| Reporting & Statements      | **Rewrite**     | Medium| Medium | Phase 5  |

---

## 5. Cross-Cutting Concerns

### 5.1 Data Migration (VSAM to RDBMS)

- **VSAM KSDS files** map to relational tables with primary keys matching VSAM record keys
- **Alternate index paths** (CARDAIX, CXACAIX) map to database indexes and foreign keys
- **Fixed-length records** with FILLER fields -- strip filler during migration
- **EBCDIC to ASCII** conversion required for data values
- **PIC S9(n)V99** fixed-point fields must use `BigDecimal` in Java, not `double`/`float`

### 5.2 Transaction Management

- CICS pseudo-conversational pattern (RETURN TRANSID with COMMAREA) replaces with stateless REST + server-side session
- CICS file control (READ UPDATE / REWRITE) replaces with JPA optimistic locking (`@Version`)
- Batch file I/O (OPEN/READ/WRITE/CLOSE) replaces with Spring Batch chunk-oriented processing

### 5.3 Error Handling

- COBOL RESP/RESP2 codes map to Java exceptions with specific handlers
- CICS HANDLE ABEND replaces with `@ControllerAdvice` / global exception handlers
- Batch ABEND codes replace with Spring Batch step execution listeners and skip policies

### 5.4 Character Set and Encoding

- EBCDIC packed decimal (COMP-3) fields require conversion utilities
- PIC X fields with EBCDIC collation may affect sort order in migrated data
- Date formats (PIC X(10) as YYYY-MM-DD strings) map to `java.time.LocalDate`

---

## 6. Technology Stack Recommendation

| Layer              | Current (Mainframe)         | Target (Java)                        |
|--------------------|-----------------------------|--------------------------------------|
| Runtime            | z/OS, CICS                  | JVM 17+, Spring Boot 3.x            |
| UI                 | BMS / 3270 Terminal         | React SPA or Thymeleaf SSR          |
| API                | CICS Transactions           | REST (Spring MVC) + OpenAPI          |
| Business Logic     | COBOL programs              | Spring Services + Bean Validation    |
| Batch Processing   | JCL + COBOL batch           | Spring Batch                         |
| Data Access        | VSAM KSDS / ESDS            | Spring Data JPA + Hibernate          |
| Database           | VSAM files                  | PostgreSQL / Oracle                  |
| Security           | USRSEC VSAM file            | Spring Security + JWT + BCrypt       |
| Messaging          | CICS TDQ / MQ (optional)    | Spring AMQP or Kafka                 |
| Scheduling         | JES2 / CA-7 / Control-M     | Spring Scheduler or Quartz           |
| Reporting          | COBOL batch + spool output  | JasperReports / Thymeleaf templates  |
| Testing            | Mainframe unit testing       | JUnit 5 + Mockito + Testcontainers  |

---

## 7. Key Assumptions and Constraints

1. The target platform is Java 17+ with Spring Boot 3.x
2. VSAM files will be migrated to a relational database (PostgreSQL or Oracle)
3. The 3270 terminal UI will be replaced entirely (no terminal emulation)
4. Optional modules (IMS/DB2/MQ) are out of initial scope but addressed in Phase 5+
5. Data migration will use the ASCII sample data in `app/data/ASCII/` for validation
6. Existing batch cycle ordering (CLOSEFIL -> refresh -> POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT -> TRANIDX -> OPENFIL) must be preserved in the Spring Batch job orchestration
7. All financial calculations must preserve COBOL fixed-point precision using `BigDecimal`
