# CardDemo Modernization Blueprint

> **Generated**: 2026-03-25 | **Source System**: CardDemo COBOL/CICS/VSAM Credit Card Management Application

## Executive Summary

CardDemo is a mainframe credit card management application comprising **31 core COBOL programs**, **30 copybooks**, **17 BMS screen maps**, and **38 JCL batch jobs**, plus **3 optional modules** (13 additional programs) integrating with IMS DB, DB2, and MQ. This blueprint evaluates four modernization strategies for each functional area and recommends the optimal approach.

### Strategy Definitions

| Strategy | Description | When to Use | Risk | Cost |
|---|---|---|---|---|
| **Strangler Fig** | Incrementally replace modules behind an API facade while the legacy system continues to run | Low-coupling modules with clear interfaces; read-heavy or stateless functions | Low | Medium |
| **Replatform** | Move COBOL to a cloud-compatible runtime (e.g., Micro Focus, AWS M2) with minimal code changes | Stable, low-change modules where preserving existing logic reduces risk | Low | Low |
| **Refactor** | Restructure COBOL into cleaner modules, then convert to Java preserving business logic | Complex modules with deep business rules that must be preserved exactly | Medium | Medium-High |
| **Rewrite** | Build from scratch in Java/Spring using COBOL only as a specification | Modules with poor code quality, obsolete patterns, or where modern equivalents exist | High | High |

---

## Functional Area Analysis

### 1. Authentication & Session Management

**Programs:** COSGN00C (260 lines)
**Copybooks:** CSUSR01Y (user security record), COCOM01Y (communication area)
**BMS Maps:** COSGN00 (signon screen)
**VSAM Files:** USRSEC (user security KSDS)

| Aspect | Detail |
|---|---|
| Complexity | Low (260 lines, simple read-compare-route logic) |
| Current State | Plaintext password storage, CICS pseudo-sign-on, COMMAREA-based session |
| Business Rules | User type routing (admin vs. regular), login validation |
| External Dependencies | None beyond VSAM |

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|---|---|---|
| Strangler Fig | **Poor** | Authentication cannot run in parallel -- users must authenticate against one system |
| Replatform | **Poor** | Plaintext passwords are a security liability that must be eliminated |
| Refactor | **Fair** | Simple enough that refactoring adds unnecessary intermediate steps |
| **Rewrite** | **Recommended** | Replace with Spring Security + BCrypt. Modern auth patterns (JWT, OAuth2) have no COBOL equivalent. This module is simple enough that a rewrite is faster and more secure than any other approach. |

**Recommendation:** **Rewrite** as Spring Security module. Build first as the gateway for all other migrated services.

---

### 2. Navigation & Menu System

**Programs:** COMEN01C (308 lines), COADM01C (288 lines)
**Copybooks:** COMEN02Y (menu definitions), COADM02Y (admin menu definitions), COCOM01Y
**BMS Maps:** COMEN01 (main menu), COADM01 (admin menu)

| Aspect | Detail |
|---|---|
| Complexity | Low (table-driven XCTL routing from copybook arrays) |
| Current State | COBOL table lookup → XCTL to target program |
| Business Rules | Menu-to-program mapping, role-based menu filtering |
| External Dependencies | All downstream programs (XCTL targets) |

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|---|---|---|
| Strangler Fig | **Fair** | Could serve as API gateway routing to both legacy and new services |
| Replatform | **Poor** | BMS 3270 menus have no modern equivalent |
| Refactor | **Poor** | Too simple to justify intermediate restructuring |
| **Rewrite** | **Recommended** | Replace with web UI navigation (React/Angular) + API routing layer. Menu definitions in COMEN02Y/COADM02Y translate directly to route configuration. |

**Recommendation:** **Rewrite** as frontend navigation + API gateway. Build alongside authentication as the application shell.

---

### 3. Account Management

**Programs:** COACTVWC (941 lines, view), COACTUPC (4,236 lines, update)
**Copybooks:** CVACT01Y (account record, 300 bytes), CVACT03Y (card cross-ref), CVCUS01Y (customer), CVCRD01Y
**BMS Maps:** COACTVW (account view), COACTUP (account update)
**VSAM Files:** ACCTDATA, CUSTDATA, CARDXREF
**Batch:** CBACT01C (read accounts, 430 lines), CBACT04C (interest calc, 652 lines)
**JCL:** ACCTFILE, READACCT, INTCALC

| Aspect | Detail |
|---|---|
| Complexity | Very High (COACTUPC is the largest program at 4,236 lines with 17 CICS commands, field-level validation, multi-file updates) |
| Current State | CICS pseudo-conversational, direct VSAM READ/REWRITE, no audit trail |
| Business Rules | Balance management, credit limit enforcement, cycle tracking, interest calculation (CBACT04C), cross-reference validation |
| External Dependencies | Card cross-reference (CVACT03Y), customer data (CVCUS01Y), interest disclosure groups (CVTRA02Y) |

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|---|---|---|
| Strangler Fig | **Fair** | Read operations (COACTVWC) could be strangled first; writes are tightly coupled |
| Replatform | **Poor** | COACTUPC's complexity and tight CICS coupling make replatforming high-risk |
| **Refactor** | **Recommended** | COACTUPC must be decomposed into smaller services. Refactor extracts validation rules, separates read/write paths, preserves exact business logic. Interest calculation (CBACT04C) requires precise decimal arithmetic preservation. |
| Rewrite | **Fair** | Risky due to complex validation and financial precision requirements |

**Recommendation:** **Refactor** with decomposition. Split COACTUPC into AccountViewController, AccountUpdateService, and AccountValidationService. Refactor CBACT04C into InterestCalculationService with BigDecimal arithmetic and extensive comparison testing against COBOL output.

---

### 4. Credit Card Management

**Programs:** COCRDLIC (1,459 lines, list), COCRDSLC (887 lines, view), COCRDUPC (1,560 lines, update)
**Copybooks:** CVACT02Y (card record, 150 bytes), CVCRD01Y (card detail), CVACT03Y (cross-ref)
**BMS Maps:** COCRDLI (card list), COCRDSL (card detail), COCRDUP (card update)
**VSAM Files:** CARDDATA, CARDXREF, ACCTDATA, CUSTDATA
**Batch:** CBACT02C (read cards, 178 lines), CBACT03C (read cross-ref, 178 lines)
**JCL:** CARDFILE, READCARD, READXREF, XREFFILE

| Aspect | Detail |
|---|---|
| Complexity | High (COCRDUPC 1,560 lines with PCI-sensitive field handling; COCRDLIC 1,459 lines with CICS browse pagination) |
| Current State | STARTBR/READNEXT pagination, card number displayed, CVV in plaintext |
| Business Rules | Card validation (number, CVV, expiration), card-account linking, status management |
| External Dependencies | Account cross-reference, customer data |

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | **Recommended** | Card list/view are read-only -- strangle behind a REST API while legacy handles writes. Migrate writes once reads are stable. |
| Replatform | **Poor** | PCI compliance requires new encryption patterns incompatible with VSAM |
| Refactor | **Fair** | Complex but the core logic is straightforward CRUD |
| Rewrite | **Fair** | PCI requirements may favor clean-room implementation |

**Recommendation:** **Strangler Fig**. Phase 1: REST API for card list/view (read-only) with data from new database + sync from VSAM. Phase 2: Migrate card update with PCI-compliant field encryption. The pagination pattern (STARTBR/READNEXT) maps cleanly to database pagination.

---

### 5. Transaction Management (Online)

**Programs:** COTRN00C (699 lines, list), COTRN01C (330 lines, view), COTRN02C (783 lines, add)
**Copybooks:** CVTRA05Y (transaction record, 350 bytes), CVACT03Y (cross-ref)
**BMS Maps:** COTRN00 (transaction list), COTRN01 (transaction detail), COTRN02 (transaction add)
**VSAM Files:** TRANSACT, ACCTDATA, CARDXREF

| Aspect | Detail |
|---|---|
| Complexity | Medium (COTRN02C 783 lines with write + validation, list/view are simpler) |
| Current State | Sequential VSAM key generation, date validation via CSUTLDTC/CEEDAYS, CICS browse for lists |
| Business Rules | Transaction creation with type/category validation, merchant data capture, timestamp management |
| External Dependencies | Account validation, card cross-reference, date utility (CSUTLDTC) |

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | **Recommended** | Transaction list/view are read-only and high-volume. Strangle reads first, then migrate writes. New API can coexist with batch posting. |
| Replatform | **Fair** | Simpler programs could replatform, but VSAM key generation is problematic |
| Refactor | **Fair** | Business rules are moderate complexity |
| Rewrite | **Fair** | Transaction ID generation must change anyway (VSAM → database sequence) |

**Recommendation:** **Strangler Fig**. Read operations first (list/view), then add. Transaction ID generation naturally switches to database sequences. Date validation moves to `java.time`.

---

### 6. Transaction Processing (Batch Pipeline)

**Programs:** CBTRN02C (731 lines, posting), CBACT04C (652 lines, interest), CBTRN01C (494 lines, daily load), CBTRN03C (649 lines, reporting)
**Copybooks:** CVTRA05Y, CVTRA06Y (daily transactions), CVTRA01Y (category balance), CVTRA02Y (disclosure groups), CVTRA03Y, CVTRA04Y, CVTRA07Y (report)
**JCL:** POSTTRAN, INTCALC, TRANBKP, COMBTRAN, TRANIDX, TRANREPT, CLOSEFIL, OPENFIL
**VSAM Files:** TRANSACT, DALYTRAN, ACCTDATA, TCATBALF, DISCGRP, CARDXREF, SYSTRAN, DALYREJS

| Aspect | Detail |
|---|---|
| Complexity | Very High (12-step nightly batch cycle with strict sequencing: CLOSEFIL → data refresh → POSTTRAN → INTCALC → TRANBKP → COMBTRAN → CREASTMT → TRANIDX → OPENFIL) |
| Current State | JCL job chains with IDCAMS/SORT utilities, VSAM sequential/indexed access, packed decimal arithmetic |
| Business Rules | Transaction posting with reject handling, interest calculation with disclosure groups, category balance tracking, backup/recovery |
| External Dependencies | All VSAM master files, JCL SORT, IDCAMS, SDSF for CICS file control |

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|---|---|---|
| Strangler Fig | **Poor** | Batch pipeline is tightly coupled -- cannot run partial pipeline in new system |
| **Replatform** | **Recommended** | AWS M2 or Micro Focus can run the existing batch cycle while online programs are migrated. Batch runs overnight with no user interaction -- replatforming preserves exact behavior with minimal risk. |
| Refactor | **Fair** | Eventually needed but premature while online migration is in progress |
| Rewrite | **Poor** | High risk due to financial precision, complex sequencing, and reject handling |

**Recommendation:** **Replatform** initially on AWS Mainframe Modernization (M2) or equivalent. The batch pipeline runs unattended overnight and has the highest financial precision requirements. Keep it running on the replatformed runtime while online programs are migrated. Plan a Phase 2 refactor to Spring Batch once the online migration is stable and parallel-run testing infrastructure exists.

---

### 7. Statement Generation

**Programs:** CBSTM03A (924 lines, main), CBSTM03B (230 lines, file I/O subroutine)
**Copybooks:** COSTM01 (statement layout), CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVTRA05Y
**JCL:** CREASTMT (with SORT pre-processing)
**VSAM Files:** TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE

| Aspect | Detail |
|---|---|
| Complexity | High (ALTER/GO TO control flow, 2D arrays, dual output: text + HTML, CALL to subroutine) |
| Current State | Mainframe report with text and HTML output, SORT-dependent, ALTER/GO TO flow |
| Business Rules | Statement generation with card grouping, transaction detail, account/customer header |
| External Dependencies | Pre-sorted transaction file (JCL SORT), 4 master files |

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|---|---|---|
| Strangler Fig | **Poor** | Statements are generated in batch, not incrementally replaceable |
| Replatform | **Fair** | Could run on M2 alongside other batch, but ALTER/GO TO makes future maintenance painful |
| Refactor | **Fair** | ALTER/GO TO flow needs untangling before any conversion |
| **Rewrite** | **Recommended** | ALTER/GO TO makes the control flow nearly impossible to automatically convert. Modern reporting frameworks (JasperReports, Thymeleaf) produce better output. The SORT dependency maps to SQL ORDER BY. |

**Recommendation:** **Rewrite** using Spring Batch + reporting framework (JasperReports or Thymeleaf). CBSTM03A's ALTER/GO TO flow is a strong rewrite indicator. Build with parallel-run testing against COBOL output.

---

### 8. Bill Payment

**Programs:** COBIL00C (572 lines)
**Copybooks:** CVACT01Y, CVACT03Y, CVTRA05Y
**BMS Maps:** COBIL00 (bill payment screen)
**VSAM Files:** ACCTDATA, CARDXREF, TRANSACT

| Aspect | Detail |
|---|---|
| Complexity | Medium (full/partial payment, account balance update + transaction creation) |
| Current State | CICS pseudo-conversational, VSAM READ/REWRITE/WRITE |
| Business Rules | Payment validation (amount vs. balance), payment transaction creation, balance update |
| External Dependencies | Account data, card cross-reference, transaction master |

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|---|---|---|
| **Strangler Fig** | **Recommended** | Payment can be exposed as a REST API that writes to both legacy VSAM and new database during transition. Clear transactional boundary. |
| Replatform | **Fair** | Works but misses opportunity to add modern payment features |
| Refactor | **Fair** | Business logic is moderate and well-contained |
| Rewrite | **Fair** | Clean enough for rewrite but strangler is lower risk |

**Recommendation:** **Strangler Fig**. Expose payment as a REST endpoint. During dual-run, write to both VSAM and new database. Validate amounts match before cutting over.

---

### 9. Reporting

**Programs:** CORPT00C (649 lines, online report request), CBTRN03C (649 lines, batch report)
**Copybooks:** CVTRA05Y, CVTRA03Y, CVTRA04Y, CVTRA07Y
**BMS Maps:** CORPT00 (report request screen)
**JCL:** TRANREPT (with SORT), PRTCATBL

| Aspect | Detail |
|---|---|
| Complexity | Medium-High (CBTRN03C has complex formatting with headers, subtotals, page breaks; CORPT00C is an online trigger) |
| Current State | JCL SORT + COBOL formatting, fixed-width report output |
| Business Rules | Date range filtering, transaction type/category grouping, multi-level subtotals |
| External Dependencies | Pre-sorted transaction file, reference tables (type/category) |

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|---|---|---|
| **Rewrite** | **Recommended** | Report formatting is inherently different in modern systems (PDF, web, dashboard). The SORT + fixed-width output model has no modern equivalent worth preserving. |
| Strangler Fig | **Fair** | Could route report requests to new system while batch reports continue |
| Replatform | **Poor** | Fixed-width reports provide poor user experience |
| Refactor | **Poor** | Formatting logic doesn't translate well |

**Recommendation:** **Rewrite** using a modern reporting framework. Online report requests (CORPT00C) become REST API calls. Batch reports (CBTRN03C) become Spring Batch jobs with JasperReports or PDF generation.

---

### 10. User Administration

**Programs:** COUSR00C (695 lines, list), COUSR01C (299 lines, add), COUSR02C (414 lines, update), COUSR03C (359 lines, delete)
**Copybooks:** CSUSR01Y (user security record)
**BMS Maps:** COUSR00-03 (user CRUD screens)
**VSAM Files:** USRSEC

| Aspect | Detail |
|---|---|
| Complexity | Low-Medium (standard CRUD operations on user security records) |
| Current State | CICS CRUD with plaintext passwords |
| Business Rules | User creation/update/deletion, password management, user type assignment |
| External Dependencies | USRSEC VSAM only |

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|---|---|---|
| Strangler Fig | **Fair** | Could run alongside legacy admin |
| Replatform | **Poor** | Plaintext passwords must be eliminated |
| Refactor | **Poor** | Too simple for intermediate refactoring |
| **Rewrite** | **Recommended** | Standard CRUD that maps directly to Spring Data JPA + REST. Plaintext passwords must be replaced with hashed storage. Modern admin UI is a better experience. |

**Recommendation:** **Rewrite** as part of the authentication module. Spring Data JPA + REST API for user CRUD. Admin UI with role-based access.

---

### 11. Data Export/Import

**Programs:** CBEXPORT (582 lines), CBIMPORT (487 lines)
**Copybooks:** CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, CVTRA05Y, CVEXPORT
**JCL:** CBEXPORT, CBIMPORT

| Aspect | Detail |
|---|---|
| Complexity | Medium (multi-file sequential read/write with combined export record format) |
| Current State | VSAM-to-sequential export, sequential-to-VSAM import |
| Business Rules | Data migration utility, combined record format (CVEXPORT) |
| External Dependencies | All master VSAM files |

#### Strategy Evaluation

| Strategy | Fit | Rationale |
|---|---|---|
| **Replatform** | **Recommended** | Export/import is a migration utility, not a business function. Keep running on replatformed environment during migration. Replace with database-native export tools post-migration. |
| Strangler Fig | **Poor** | Not user-facing, no benefit to incremental replacement |
| Refactor | **Poor** | Utility code, not worth refactoring |
| Rewrite | **Fair** | Eventually replaced by database export tools |

**Recommendation:** **Replatform** during migration. Post-migration, replace with database-native tools (pg_dump, mysqldump, or application-level data export API).

---

### 12. Optional Modules

#### 12a. Authorization Module (IMS/DB2/MQ)
**Programs:** COPAUA0C (1,026 lines), COPAUS0C (1,032 lines), COPAUS1C (604 lines), COPAUS2C (244 lines), CBPAUP0C (386 lines)
**Technology:** IMS DB, DB2, MQ Series

| Strategy | Fit | Rationale |
|---|---|---|
| **Rewrite** | **Recommended** | IMS DB + MQ integration is the most complex external dependency. Modern equivalent is REST API + event-driven architecture (Kafka/SQS). MQ trigger processing (COPAUA0C) maps to message consumer. DB2 fraud marking (COPAUS2C) maps to database service. |

#### 12b. Transaction Type DB2 Module
**Programs:** COTRTUPC (1,702 lines), COTRTLIC (2,098 lines), COBTUPDT (237 lines)
**Technology:** DB2 embedded SQL

| Strategy | Fit | Rationale |
|---|---|---|
| **Refactor** | **Recommended** | DB2 embedded SQL maps to JPA/JDBC with moderate effort. Preserve DB2 cursor logic as JPA repository queries. COTRTUPC and COTRTLIC are large but structurally straightforward CRUD + list operations. |

#### 12c. VSAM-MQ Module
**Programs:** CODATE01 (524 lines), COACCT01 (620 lines)
**Technology:** MQ request/response

| Strategy | Fit | Rationale |
|---|---|---|
| **Rewrite** | **Recommended** | MQ request/response for system date and account inquiry maps to REST API endpoints. Simple request/response pattern. |

---

## Strategy Summary by Functional Area

| # | Functional Area | Strategy | Programs | Lines | Key Driver |
|---|---|---|---|---|---|
| 1 | Authentication | Rewrite | 1 | 260 | Security modernization |
| 2 | Navigation | Rewrite | 2 | 596 | UI paradigm change |
| 3 | Account Management | Refactor | 4 | 6,259 | Complex business rules + financial precision |
| 4 | Card Management | Strangler Fig | 5 | 4,084 | Incremental read-then-write migration |
| 5 | Transaction (Online) | Strangler Fig | 3 | 1,812 | Read-heavy, clear API boundary |
| 6 | Transaction (Batch) | Replatform | 4 | 2,526 | Financial precision, pipeline coupling |
| 7 | Statement Generation | Rewrite | 2 | 1,154 | ALTER/GO TO, obsolete output format |
| 8 | Bill Payment | Strangler Fig | 1 | 572 | Clear transactional boundary |
| 9 | Reporting | Rewrite | 2 | 1,298 | Modern reporting frameworks |
| 10 | User Admin | Rewrite | 4 | 1,767 | Security, standard CRUD |
| 11 | Data Export/Import | Replatform | 2 | 1,069 | Migration utility |
| 12a | Authorization (IMS/DB2/MQ) | Rewrite | 5 | 3,292 | External system dependencies |
| 12b | Transaction Type (DB2) | Refactor | 3 | 4,037 | DB2 → JPA mapping |
| 12c | VSAM-MQ | Rewrite | 2 | 1,144 | Simple REST replacement |

### Strategy Distribution

| Strategy | Functional Areas | Total Programs | Total Lines | % of Codebase |
|---|---|---|---|---|
| **Rewrite** | 7 areas | 18 | 9,511 | 31% |
| **Strangler Fig** | 3 areas | 9 | 6,468 | 21% |
| **Refactor** | 2 areas | 7 | 10,296 | 34% |
| **Replatform** | 2 areas | 6 | 3,595 | 12% |
| **Utility (no migration)** | -- | 2 | 198 | 1% |

---

## Technology Target Architecture

### Runtime Stack
- **Language:** Java 17+ (LTS)
- **Framework:** Spring Boot 3.x + Spring Batch 5.x
- **Data Access:** Spring Data JPA + Hibernate
- **Security:** Spring Security 6.x with BCrypt + JWT
- **Database:** PostgreSQL or Amazon Aurora (replacing VSAM KSDS)
- **Messaging:** Amazon SQS or Apache Kafka (replacing MQ Series)
- **Reporting:** JasperReports or Thymeleaf (replacing COBOL report formatting)
- **Frontend:** React or Angular (replacing BMS 3270 screens)

### Infrastructure
- **Compute:** AWS ECS/EKS or equivalent container orchestration
- **Batch:** Spring Batch on ECS Scheduled Tasks or AWS Step Functions
- **Monitoring:** CloudWatch, Datadog, or equivalent
- **CI/CD:** GitHub Actions or AWS CodePipeline

### Data Migration
- **VSAM → PostgreSQL/Aurora:** Use CBEXPORT to extract data, transform to SQL INSERT, load into target database
- **EBCDIC → UTF-8:** Handle character encoding during export (sample data in `app/data/ASCII/` provides reference)
- **Packed Decimal → BigDecimal:** Map all COMP-3 fields to Java BigDecimal with explicit scale
