# CardDemo Modernization Blueprint

## Executive Summary

This blueprint evaluates four modernization strategies -- **Strangler Fig**, **Replatform**, **Refactor**, and **Rewrite** -- for the AWS CardDemo mainframe application. CardDemo is a COBOL/CICS/VSAM credit card management system comprising 31 online and batch COBOL programs, 30 copybooks, 17 BMS screen maps, and 38 JCL batch jobs. The target platform is Java (Spring Boot / Spring Batch) with a relational database backend.

---

## Functional Area Inventory

| # | Functional Area | Programs | Type | LOC (approx.) | Complexity |
|---|----------------|----------|------|---------------|------------|
| 1 | Authentication & Session | COSGN00C | Online CICS | 261 | Low |
| 2 | Menu & Navigation | COMEN01C, COADM01C | Online CICS | 600 | Low |
| 3 | Account Management | COACTVWC, COACTUPC | Online CICS | 5,179 | High |
| 4 | Card Management | COCRDLIC, COCRDSLC, COCRDUPC | Online CICS | 3,500+ | Medium-High |
| 5 | Transaction Management (Online) | COTRN00C, COTRN01C, COTRN02C | Online CICS | 2,200+ | Medium |
| 6 | Bill Payment | COBIL00C | Online CICS | 573 | Medium |
| 7 | Reporting | CORPT00C, CBTRN03C | Online + Batch | 1,350+ | Medium |
| 8 | Batch Transaction Posting | CBTRN02C, CBTRN01C | Batch | 1,400+ | High |
| 9 | Batch Interest Calculation | CBACT04C | Batch | 653 | High |
| 10 | Batch Statement Generation | CBSTM03A, CBSTM03B | Batch | 800+ | Medium |
| 11 | Data Export/Import | CBEXPORT, CBIMPORT | Batch | 600+ | Low |
| 12 | User Administration (CRUD) | COUSR00C, COUSR01C, COUSR02C, COUSR03C | Online CICS | 2,400+ | Medium |
| 13 | Authorization Module (Optional) | COPAUA0C, COPAUS0C/1C/2C, CBPAUP0C | Online + Batch | 1,500+ | High |
| 14 | Transaction Type DB2 (Optional) | COTRTUPC, COTRTLIC, COBTUPDT | Online + Batch | 900+ | Medium |
| 15 | VSAM-MQ Integration (Optional) | CODATE01, COACCT01 | Online MQ | 600+ | Medium |
| 16 | Utility Programs | CSUTLDTC, COBSWAIT | Utility | 400+ | Low |

---

## Strategy Definitions

### Strangler Fig
Incrementally replace legacy components by routing traffic through a facade. New Java services replace COBOL programs one at a time while the mainframe continues to run. Co-existence via API gateway or anti-corruption layer.

### Replatform (Lift & Shift + Adapt)
Move COBOL code to a cloud-hosted mainframe emulator (e.g., AWS Mainframe Modernization with Micro Focus or Blu Age), then incrementally modernize. Minimal code changes initially.

### Refactor
Automatically or semi-automatically convert COBOL to Java using tooling (e.g., Blu Age, TSRI, Modernization Workbench). Preserves existing logic structure but generates Java code that mirrors COBOL patterns.

### Rewrite
Build entirely new Java services from scratch using the COBOL code as functional specification. Full redesign of data model, business logic, and UI.

---

## Strategy Evaluation by Functional Area

### 1. Authentication & Session Management

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Small, self-contained entry point. Easy to replace with Spring Security behind an API gateway. First candidate for extraction. |
| Replatform | Acceptable | Works but retains VSAM-based plaintext password storage. |
| Refactor | Acceptable | Simple enough to auto-convert but yields poor-quality auth code. |
| Rewrite | Good | Opportunity to implement modern auth (OAuth2/JWT) from scratch. |

**Recommendation**: **Strangler Fig** into a Spring Security module with JWT tokens. This is the natural first extraction point since it gates all other interactions.

**Key Considerations**:
- Current auth stores plaintext passwords in USRSEC VSAM file (CSUSR01Y copybook: 80-byte records)
- Two user types: Admin (`A`) and Regular (`U`) -- map to Spring Security roles
- CICS COMMAREA carries session state -- replace with stateless JWT claims

---

### 2. Menu & Navigation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Navigation is purely presentational. Replace with web UI routing. |
| Replatform | Poor | BMS maps are 3270-specific; no value in preserving terminal UI. |
| Refactor | Poor | Auto-converting BMS maps produces unusable UI code. |
| Rewrite | Good | Clean opportunity for modern web frontend (React/Angular). |

**Recommendation**: **Strangler Fig** -- replace with a modern web frontend. Menu definitions in COMEN02Y copybook (11 options) map directly to REST endpoint routes. The BMS maps (COMEN01.bms) are eliminated entirely.

---

### 3. Account Management (View & Update)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Good | Can be extracted as a standalone Account Service. |
| Replatform | Acceptable | Complex validation logic (COACTUPC: 4,237 lines) works on emulator. |
| **Refactor** | **Recommended** | High LOC with straightforward CRUD patterns. Auto-conversion preserves business rules. Manual cleanup of generated code is tractable. |
| Rewrite | Risky | 4,237 lines of validation logic in COACTUPC is error-prone to rewrite. Risk of missing edge cases (SSN validation, phone number formatting, date checks, credit limit rules). |

**Recommendation**: **Refactor** (auto-convert then clean up). The account update program (COACTUPC) is the largest single program at 4,237 lines with extensive field-level validation. Auto-conversion preserves these rules while allowing cleanup into a Spring MVC service layer.

**Key Considerations**:
- VSAM files: ACCTDAT (300-byte records per CVACT01Y), CARDDAT (150-byte), CUSTDAT (500-byte), CXACAIX (cross-reference)
- Complex cross-file reads: Account -> Card XREF -> Customer master
- Field validations: SSN (3-part), phone numbers, dates (open, expiry, reissue), credit limits, FICO scores
- Map to JPA entities: Account, Card, Customer with proper foreign key relationships

---

### 4. Card Management (List, View, Update)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Good | Naturally pairs with Account Management extraction. |
| **Refactor** | **Recommended** | Three programs with consistent CRUD patterns. Card list (COCRDLIC: 1,460 lines) has pagination logic that auto-converts well. |
| Replatform | Acceptable | Works but retains 3270 UI limitations. |
| Rewrite | Acceptable | Moderate risk; well-understood domain. |

**Recommendation**: **Refactor**, extracted alongside Account Management since they share VSAM files (CARDDAT, CARDAIX alternate index). The card record (CVACT02Y: card number, account ID, CVV, embossed name, expiration, status) maps cleanly to a JPA entity.

**Key Considerations**:
- COCRDLIC uses VSAM alternate index (CARDAIX) for account-based card lookups -- replace with SQL JOIN
- Pagination logic (7 rows per screen, PF7/PF8 keys) maps to Spring Data pageable queries
- Select actions: `S` for view, `U` for update -- map to REST GET/PUT endpoints

---

### 5. Transaction Management (Online)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Core business function that benefits from incremental extraction behind a transaction API. |
| Refactor | Good | List/View are simple; Add (COTRN02C) has moderate validation. |
| Replatform | Acceptable | Works but locks in VSAM I/O patterns. |
| Rewrite | Risky | Transaction add (784 lines) has date validation, cross-reference lookups, and auto-ID generation that must be precisely replicated. |

**Recommendation**: **Strangler Fig** -- extract as a Transaction Service behind a REST API. Transaction list (COTRN00C) reads TRANSACT VSAM file sequentially with pagination. Transaction add (COTRN02C) performs: card/account cross-reference validation, date format validation (YYYY-MM-DD), amount validation, auto-incrementing transaction ID generation, and VSAM WRITE.

**Key Considerations**:
- Transaction record (CVTRA05Y): 350 bytes with 17 fields including merchant info, timestamps, amounts
- Auto-ID generation: reads HIGH-VALUES to find last ID, increments by 1 -- replace with database sequence
- Cross-reference validation through CCXREF and CXACAIX files
- Date validation calls utility program CSUTLDTC

---

### 6. Bill Payment

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Self-contained payment flow. Natural microservice boundary. |
| Refactor | Good | 573 lines, manageable complexity. |
| Replatform | Acceptable | Works but misses opportunity to integrate modern payment rails. |
| Rewrite | Good | Small enough to rewrite safely with enhanced payment features. |

**Recommendation**: **Strangler Fig** -- extract as a Payment Service. COBIL00C performs: account lookup (ACCTDAT READ FOR UPDATE), cross-reference lookup (CXACAIX), transaction creation (TRANSACT WRITE), and account balance update (ACCTDAT REWRITE). This is a natural transactional unit.

**Key Considerations**:
- Uses CICS READ with UPDATE option for pessimistic locking -- replace with database transactions
- Creates a transaction record with type `02`, category `2`, source `POS TERM`
- Zero-balance check prevents unnecessary payments
- Confirmation flow (Y/N) maps to a two-step REST API (preview + confirm)

---

### 7. Reporting

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| Strangler Fig | Acceptable | Can extract report generation independently. |
| **Rewrite** | **Recommended** | Current approach submits JCL batch jobs via CICS extra-partition TDQ (internal reader). This mainframe-specific pattern has no cloud equivalent. |
| Refactor | Poor | JCL submission logic and internal reader I/O cannot be auto-converted meaningfully. |
| Replatform | Poor | TDQ-based job submission is deeply mainframe-specific. |

**Recommendation**: **Rewrite** as a Spring Batch reporting module or async report generation service. CORPT00C constructs JCL dynamically (hardcoded JOB card, PROC references, SYSIN parameters) and writes it to an internal reader TDQ. This pattern must be replaced entirely.

**Key Considerations**:
- Three report types: Monthly, Yearly, Custom date range
- Date validation uses CSUTLDTC utility
- Report parameters: start date, end date in YYYY-MM-DD format
- JCL references PROC TRANREPT and SORT control statements -- replace with Spring Batch ItemReader/ItemProcessor/ItemWriter
- Output: currently mainframe spool (MSGCLASS=0) -- replace with PDF/CSV generation

---

### 8. Batch Transaction Posting

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Refactor** | **Recommended** | Core batch processing with clear file I/O patterns that map well to Spring Batch. |
| Strangler Fig | Good | Can run in parallel with mainframe during transition. |
| Replatform | Acceptable | Batch programs run well on emulators. |
| Rewrite | Risky | Complex validation pipeline (XREF lookup, account lookup, reject processing) with precise error handling. |

**Recommendation**: **Refactor** into Spring Batch jobs. CBTRN02C reads daily transactions (DALYTRAN), validates each against cross-reference (XREF) and account master (ACCTFILE), posts valid transactions to TRANSACT file, writes rejects to DALYREJS, and updates account balances and transaction category balances (TCATBAL).

**Key Considerations**:
- Six file I/O operations: DALYTRAN (input), TRANSACT (output), XREF (lookup), DALYREJS (rejects), ACCTFILE (update), TCATBAL (update)
- Validation pipeline: 1500-A-LOOKUP-XREF -> 1500-B-LOOKUP-ACCT -> extensible ("ADD MORE VALIDATIONS HERE")
- Reject tracking with reason codes and descriptions
- Return code 4 if any rejects -- map to Spring Batch exit status
- Maps to Spring Batch: FlatFileItemReader -> ValidatingItemProcessor -> CompositeItemWriter

---

### 9. Batch Interest Calculation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Refactor** | **Recommended** | Financial calculation logic must be preserved exactly. Auto-conversion minimizes arithmetic errors. |
| Rewrite | Risky | Interest calculation (COMPUTE statements with COBOL packed decimal) is error-prone to rewrite. Rounding differences can cause regulatory issues. |
| Strangler Fig | Good | Can run in parallel for reconciliation. |
| Replatform | Acceptable | Preserves exact COBOL arithmetic behavior. |

**Recommendation**: **Refactor** with extensive numeric validation. CBACT04C reads transaction category balances (TCATBAL), looks up discount/interest rates (DISCGRP file), computes monthly interest, and updates account records.

**Key Considerations**:
- Uses COBOL packed decimal arithmetic (PIC S9(09)V99) -- must use Java BigDecimal
- Account grouping logic: processes all categories for an account before updating
- External parameter: PARM-DATE passed via JCL EXEC PARM
- Cross-file lookups: TCATBAL -> XREF -> DISCGRP -> ACCTFILE
- Generates interest transactions written to TRANSACT file
- **Critical**: Parallel run with mainframe for reconciliation before cutover

---

### 10. Batch Statement Generation

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **Recommended** | Statement generation output format (mainframe print) is fundamentally different from modern output (PDF/email). |
| Refactor | Poor | Print-oriented logic doesn't translate to modern statement formats. |
| Replatform | Acceptable | Preserves functionality but locks in legacy output. |
| Strangler Fig | Acceptable | Can generate modern statements in parallel. |

**Recommendation**: **Rewrite** as a modern statement generation service producing PDF statements and/or email delivery. CBSTM03A/B currently generate line-printer output.

---

### 11. Data Export/Import

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **Recommended** | Simple utilities that should be replaced with modern ETL or database migration tools. |
| Refactor | Acceptable | Straightforward file I/O but EBCDIC handling is unnecessary on target platform. |
| Replatform | Poor | EBCDIC/ASCII conversion logic is mainframe-specific. |
| Strangler Fig | N/A | One-time migration utilities. |

**Recommendation**: **Rewrite** as database migration scripts or Spring Batch import/export jobs. These are primarily needed during the migration itself and for ongoing data exchange.

---

### 12. User Administration (CRUD)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Strangler Fig** | **Recommended** | Clean extraction alongside Authentication. Four programs (list, add, update, delete) form a complete CRUD service. |
| Refactor | Good | Standard CRUD patterns auto-convert well. |
| Rewrite | Good | Simple enough to rewrite with enhanced user management. |
| Replatform | Acceptable | Works but retains flat-file user store. |

**Recommendation**: **Strangler Fig** -- extract as a User Management Service paired with the Authentication module. COUSR00C (list with pagination), COUSR01C (add), COUSR02C (update), COUSR03C (delete) operate on the USRSEC VSAM file.

**Key Considerations**:
- User record (CSUSR01Y): 80 bytes -- user ID, first/last name, password, type
- Admin-only access enforced at menu level
- Pagination (10 users per page, PF7/PF8) maps to pageable queries
- Selection actions: `U` for update, `D` for delete

---

### 13. Authorization Module (Optional -- IMS/DB2/MQ)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **Recommended** | IMS DB, DB2, and MQ integration is deeply platform-specific. |
| Refactor | Poor | IMS DB calls and MQ trigger programs cannot be auto-converted to standard Java. |
| Replatform | Acceptable | Requires IMS and MQ emulation. |
| Strangler Fig | Good | Can be replaced with a modern event-driven authorization service. |

**Recommendation**: **Rewrite** as an event-driven authorization service using Spring Boot + message broker (Kafka/RabbitMQ). The MQ trigger pattern (COPAUA0C) maps to message listener containers. DB2 fraud marking (COPAUS2C) becomes JPA operations.

---

### 14. Transaction Type Management (Optional -- DB2)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Refactor** | **Recommended** | Already uses DB2 (SQL), which maps directly to JPA/JDBC. |
| Strangler Fig | Good | Can be extracted as a reference data service. |
| Rewrite | Acceptable | Small scope, well-understood domain. |
| Replatform | Acceptable | DB2 SQL works with minor syntax changes. |

**Recommendation**: **Refactor** -- embedded SQL (EXEC SQL) in COTRTUPC/COTRTLIC auto-converts to JPA repositories or JDBC templates with minimal changes.

---

### 15. VSAM-MQ Integration (Optional)

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **Recommended** | MQ request/reply for system date and account inquiry is a synchronous messaging anti-pattern. |
| Refactor | Poor | MQ API calls are platform-specific. |
| Replatform | Acceptable | Requires MQ emulation. |
| Strangler Fig | Good | Replace with REST API calls. |

**Recommendation**: **Rewrite** as REST API endpoints. The MQ request/reply pattern for date retrieval (CODATE01) and account inquiry (COACCT01) should become simple REST service calls.

---

### 16. Utility Programs

| Strategy | Fit | Rationale |
|----------|-----|-----------|
| **Rewrite** | **Recommended** | Utilities are small and should use Java standard libraries. |
| Refactor | Acceptable | Date validation (CSUTLDTC) auto-converts but Java has better date APIs. |
| Replatform | N/A | Utilities are consumed by other programs. |
| Strangler Fig | N/A | No independent deployment. |

**Recommendation**: **Rewrite** -- replace CSUTLDTC date validation with `java.time` API. Replace COBSWAIT with `Thread.sleep()` or scheduled executor.

---

## Strategy Summary Matrix

| Functional Area | Recommended Strategy | Risk Level | Effort (T-shirt) | Priority |
|----------------|---------------------|------------|-------------------|----------|
| Authentication & Session | Strangler Fig | Low | S | P0 |
| Menu & Navigation | Strangler Fig | Low | S | P0 |
| User Administration | Strangler Fig | Low | M | P1 |
| Bill Payment | Strangler Fig | Medium | M | P2 |
| Transaction Mgmt (Online) | Strangler Fig | Medium | L | P2 |
| Account Management | Refactor | Medium-High | XL | P3 |
| Card Management | Refactor | Medium | L | P3 |
| Batch Transaction Posting | Refactor | High | L | P4 |
| Batch Interest Calculation | Refactor | High | L | P4 |
| Transaction Type DB2 | Refactor | Low | S | P3 |
| Reporting | Rewrite | Medium | M | P2 |
| Statement Generation | Rewrite | Medium | M | P5 |
| Data Export/Import | Rewrite | Low | S | P1 |
| Authorization Module | Rewrite | High | L | P5 |
| VSAM-MQ Integration | Rewrite | Medium | S | P5 |
| Utility Programs | Rewrite | Low | S | P0 |

---

## Data Store Migration Map

| VSAM File | Record Layout (Copybook) | Record Size | Target Table | Access Pattern |
|-----------|-------------------------|-------------|-------------|----------------|
| USRSEC | CSUSR01Y | 80 bytes | `users` | KSDS by user ID |
| ACCTDAT | CVACT01Y | 300 bytes | `accounts` | KSDS by account ID |
| CARDDAT | CVACT02Y | 150 bytes | `cards` | KSDS by card number |
| CUSTDAT | CVCUS01Y | 500 bytes | `customers` | KSDS by customer ID |
| CCXREF / CXACAIX | CVACT03Y | 50 bytes | `card_xref` (or FK relationships) | KSDS + AIX |
| TRANSACT | CVTRA05Y | 350 bytes | `transactions` | KSDS by transaction ID |
| DALYTRAN | CVTRA06Y | 350 bytes | `daily_transactions` (staging) | Sequential |
| TCATBAL | CVTRA01Y | 50 bytes | `transaction_category_balances` | KSDS by composite key |
| DISCGRP | CVTRA02Y | 50 bytes | `discount_groups` | KSDS by composite key |

---

## Technology Target Stack

| Layer | Current (Mainframe) | Target (Java) |
|-------|-------------------|---------------|
| Runtime | CICS TS | Spring Boot (embedded Tomcat) |
| Batch | JCL + COBOL batch | Spring Batch |
| UI | BMS 3270 Maps | React / Angular SPA |
| Data | VSAM KSDS/ESDS | PostgreSQL / Oracle |
| Messaging | MQ Series | Apache Kafka / RabbitMQ |
| Security | VSAM flat file | Spring Security + OAuth2/JWT |
| Scheduling | CA-7 / Control-M | Spring Scheduler / Kubernetes CronJob |
| Reporting | COBOL print + SORT | JasperReports / Apache POI |

---

## Cross-Cutting Concerns

### COMMAREA Replacement
The CARDDEMO-COMMAREA (COCOM01Y) carries session state between CICS programs (user ID, user type, navigation context, customer/account/card info). Replace with:
- **Stateless JWT tokens** for user identity and role
- **Request-scoped DTOs** for navigation context
- **Database lookups** for entity relationships (no longer need to pass IDs through a communication area)

### BMS Map Elimination
All 17 BMS maps define 3270 terminal screen layouts. These are entirely replaced by the modern web frontend. No conversion needed -- use the BMS field definitions only as UI wireframe references.

### JCL Batch Orchestration
The 38 JCL jobs define batch execution sequences. The critical batch cycle (CLOSEFIL -> data refresh -> POSTTRAN -> INTCALC -> TRANBKP -> COMBTRAN -> CREASTMT -> TRANIDX -> OPENFIL) maps to a Spring Batch job flow with step dependencies.

### Error Handling
COBOL error handling uses RESP/RESP2 codes and ABEND routines. Replace with Java exception hierarchy and Spring's `@ExceptionHandler` / `@ControllerAdvice` patterns.
