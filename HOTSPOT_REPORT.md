# CardDemo Hotspot Report

> **Generated from**: Static complexity analysis, dependency coupling metrics, and business impact assessment
>
> **Purpose**: Identifies the top 10 highest-risk modules for modernization planning, prioritized by a weighted composite score

---

## Scoring Methodology

Each module is scored on three dimensions (1-10 scale each):

| Dimension | Weight | Factors Considered |
|---|---|---|
| **Complexity** | 40% | Lines of code, EVALUATE/IF branch count, PERFORM count, number of COPY includes, CICS command variety |
| **Risk** | 30% | Number of VSAM files accessed (R/W), data coupling with other programs, REWRITE/DELETE operations, multi-file consistency requirements |
| **Business Impact** | 30% | Revenue criticality, user-facing visibility, data integrity implications, frequency of execution |

**Composite Score** = (Complexity × 0.4) + (Risk × 0.3) + (Business Impact × 0.3)

---

## Top 10 Hotspot Modules

### Rank 1: `COACTUPC.cbl` — Account Update

| Metric | Value | Score |
|---|---|---|
| **Lines of Code** | 4,236 | — |
| **IF Statements** | 164 | — |
| **EVALUATE Blocks** | 20 | — |
| **PERFORM Calls** | 64 | — |
| **COPY Includes** | 15 unique + 38 COPY REPLACING | — |
| **CICS File Ops** | READ ×5, REWRITE ×2 (ACCTDAT + CUSTDAT) | — |
| **VSAM Files Touched** | ACCTDAT (R/W), CUSTDAT (R/W), CARDDAT (R) | — |
| **Complexity Score** | **10** | — |
| **Risk Score** | **10** | — |
| **Business Impact** | **9** | — |
| **Composite Score** | **9.7** | — |

**Why #1**: By far the largest program at 4,236 lines — more than double the next largest. Contains 164 IF statements creating deeply nested conditional logic. Performs REWRITE operations on two separate VSAM files (account and customer), requiring transactional consistency that CICS does not natively guarantee. Uses 38 instances of COPY REPLACING for field-level attribute setting — a pattern with no direct Java equivalent. Includes dedicated date utility code (CSUTLDWY, CSUTLDPY), lookup tables (CSLKPCDY), and string manipulation (CSSTRPFY).

**Modernization Risks**:
- Multi-file REWRITE without true transaction boundaries
- 38 COPY REPLACING instances need a generalized attribute-setting pattern
- Complex field-level validation logic spread across 164 conditional branches
- Tight coupling to 3 VSAM files makes unit testing difficult

**Recommended Approach**: Decompose into AccountService and CustomerService with explicit transaction management (@Transactional). Extract validation into a dedicated AccountValidator class. Replace COPY REPLACING with a generic field-attribute utility.

---

### Rank 2: `CBTRN02C.cbl` — Transaction Posting

| Metric | Value | Score |
|---|---|---|
| **Lines of Code** | 731 | — |
| **IF Statements** | 48 | — |
| **EVALUATE Blocks** | 0 | — |
| **PERFORM Calls** | 61 | — |
| **VSAM Files** | DALYTRAN (R), TRANSACT (W), DALYREJS (W), ACCOUNT (R/W), TCATBAL (R/W) | — |
| **Complexity Score** | **8** | — |
| **Risk Score** | **10** | — |
| **Business Impact** | **10** | — |
| **Composite Score** | **9.2** | — |

**Why #2**: Core batch posting engine that moves money. Reads daily transactions, validates against cross-reference, posts to master transaction file, updates account balances, and updates category balances — touching 6 different files in a single run. A bug here directly causes financial data corruption. The multi-file update pattern (ACCOUNT I-O + TCATBAL I-O + TRANSACT OUTPUT) requires careful transaction boundary design in Java.

**Modernization Risks**:
- 6-file coordination without explicit commit/rollback
- Reject handling logic must be preserved exactly
- Balance update arithmetic (TRAN-AMT accumulation) is financially sensitive
- Sequential file processing pattern maps to Spring Batch but requires careful chunk sizing

**Recommended Approach**: Map to Spring Batch job with chunk-oriented processing. Implement compensating transactions for partial failures. Create a TransactionPostingService with explicit @Transactional boundaries per daily transaction.

---

### Rank 3: `COCRDLIC.cbl` — Credit Card List

| Metric | Value | Score |
|---|---|---|
| **Lines of Code** | 1,459 | — |
| **IF Statements** | 59 | — |
| **EVALUATE Blocks** | 18 | — |
| **PERFORM Calls** | 34 | — |
| **CICS Ops** | STARTBR, READNEXT, READPREV, ENDBR, XCTL ×3 | — |
| **Complexity Score** | **9** | — |
| **Risk Score** | **7** | — |
| **Business Impact** | **7** | — |
| **Composite Score** | **7.8** | — |

**Why #3**: Second-largest online program. Implements full bi-directional VSAM browse with forward/backward pagination — a pattern that has no direct equivalent in REST APIs. Contains 3 different XCTL targets (return to menu, card detail, card update) making it a navigation hub for the card management subsystem. The STARTBR/READNEXT/READPREV/ENDBR browse pattern requires careful translation to paginated database queries.

**Modernization Risks**:
- Bi-directional browse with cursor positioning has no direct REST equivalent
- VSAM key-based positioning (STARTBR with partial key) maps to complex SQL WHERE clauses
- Screen-at-a-time pagination differs from modern offset/cursor pagination

**Recommended Approach**: Implement cursor-based pagination API. Use Spring Data's `Pageable` with keyset pagination for efficient forward/backward navigation.

---

### Rank 4: `COCRDUPC.cbl` — Credit Card Update

| Metric | Value | Score |
|---|---|---|
| **Lines of Code** | 1,560 | — |
| **IF Statements** | 72 | — |
| **EVALUATE Blocks** | 16 | — |
| **PERFORM Calls** | 26 | — |
| **CICS Ops** | READ ×2, REWRITE, XCTL | — |
| **Complexity Score** | **8** | — |
| **Risk Score** | **8** | — |
| **Business Impact** | **7** | — |
| **Composite Score** | **7.7** | — |

**Why #4**: Third-largest program with 72 IF statements for field-level validation of card attributes. The REWRITE to CARDDAT changes card status which affects downstream transaction processing. Contains complex navigation logic via CCARD-NEXT-PROG and CDEMO-TO-PROGRAM for inter-screen flow.

**Modernization Risks**:
- Card status validation rules are embedded in conditional logic (not externalized)
- REWRITE semantics require optimistic locking in Java
- Navigation state management via COMMAREA fields

**Recommended Approach**: Extract to CardUpdateService with Bean Validation annotations. Implement optimistic locking via JPA @Version.

---

### Rank 5: `CBACT04C.cbl` — Interest Calculation

| Metric | Value | Score |
|---|---|---|
| **Lines of Code** | 652 | — |
| **IF Statements** | 43 | — |
| **EVALUATE Blocks** | 0 | — |
| **PERFORM Calls** | 56 | — |
| **Files** | TCATBAL (R), XREF (R), DISCGRP (R), ACCOUNT (R/W), TRANSACT (W) | — |
| **Complexity Score** | **7** | — |
| **Risk Score** | **9** | — |
| **Business Impact** | **10** | — |
| **Composite Score** | **8.5** | — |

**Why #5**: Computes interest charges — directly affects customer billing. Reads category balances, looks up applicable interest rates from disclosure groups, calculates interest amounts, creates interest transactions, and updates account balances. Touches 5 files. The financial calculation logic must be preserved with exact decimal precision (PIC S9(09)V99).

**Modernization Risks**:
- Financial arithmetic precision (COBOL packed decimal → Java BigDecimal)
- Multi-file lookup chain: TCATBAL → XREF → DISCGRP → ACCOUNT
- Interest calculation formula must be validated against regulatory requirements
- Rounding behavior differences between COBOL and Java

**Recommended Approach**: Implement as Spring Batch job with BigDecimal arithmetic. Create InterestCalculationService with explicit rate lookup and calculation methods. Add extensive unit tests comparing COBOL and Java calculation results.

---

### Rank 6: `COTRN02C.cbl` — Transaction Add

| Metric | Value | Score |
|---|---|---|
| **Lines of Code** | 783 | — |
| **IF Statements** | 14 | — |
| **EVALUATE Blocks** | 26 | — |
| **PERFORM Calls** | 61 | — |
| **CICS Ops** | READ ×3, STARTBR, READPREV, ENDBR, WRITE | — |
| **VSAM Files** | TRANSACT (R/W), ACCTDAT (R), CCXREF (R), CXACAIX (R) | — |
| **Complexity Score** | **7** | — |
| **Risk Score** | **8** | — |
| **Business Impact** | **9** | — |
| **Composite Score** | **7.6** | — |

**Why #6**: The online transaction entry point — writes new transactions to the master file. Has 26 EVALUATE blocks (highest of any program) for handling multiple transaction types and validation scenarios. Accesses 4 VSAM files including the alternate index (CXACAIX) for reverse account-to-card lookups. Generates unique transaction IDs by reading the last transaction and incrementing.

**Modernization Risks**:
- Transaction ID generation via STARTBR/READPREV is a concurrency risk
- 26 EVALUATE blocks map to complex switch/strategy patterns
- Alternate index access (CXACAIX) needs database secondary index

**Recommended Approach**: Replace ID generation with UUID or database sequence. Decompose EVALUATE blocks into Strategy pattern. Map CXACAIX to a database index.

---

### Rank 7: `COACTVWC.cbl` — Account View

| Metric | Value | Score |
|---|---|---|
| **Lines of Code** | 941 | — |
| **IF Statements** | 28 | — |
| **EVALUATE Blocks** | 10 | — |
| **PERFORM Calls** | 21 | — |
| **CICS Ops** | READ ×3, XCTL | — |
| **COPY Includes** | 14 unique copybooks | — |
| **Complexity Score** | **7** | — |
| **Risk Score** | **6** | — |
| **Business Impact** | **8** | — |
| **Composite Score** | **7.0** | — |

**Why #7**: Primary account information display screen. Reads from 3 VSAM files (ACCTDAT, CUSTDAT, CARDDAT via cross-reference) to compose a unified account view. Includes 14 copybooks — the highest copybook fan-out of any online program. Uses CSSTRPFY for string manipulation and CSMSG02Y for extended messaging. Though read-only, it's the most frequently accessed screen.

**Modernization Risks**:
- 3-file join logic must be replicated in JPA/SQL
- 14 copybook dependencies create a wide compilation surface
- Screen field mapping from BMS to HTML/React requires careful layout translation

**Recommended Approach**: Create AccountViewDTO that aggregates Account, Customer, and Card entities via JPA relationships. Replace 3-file reads with a single JPA query using @EntityGraph.

---

### Rank 8: `CBSTM03A.CBL` — Statement Generation (Driver)

| Metric | Value | Score |
|---|---|---|
| **Lines of Code** | 924 | — |
| **IF Statements** | 15 | — |
| **EVALUATE Blocks** | 9 | — |
| **PERFORM Calls** | 29 | — |
| **Files** | TRNXFILE, XREFFILE, CUSTFILE, ACCTFILE (via CBSTM03B), STMTFILE, HTMLFILE (output) | — |
| **Calls** | CBSTM03B (subroutine) | — |
| **Complexity Score** | **7** | — |
| **Risk Score** | **6** | — |
| **Business Impact** | **8** | — |
| **Composite Score** | **7.0** | — |

**Why #8**: Produces customer-facing statements in both text and HTML formats. Has a driver/subroutine architecture (calls CBSTM03B for file I/O) that is relatively clean but requires coordinated modernization of both programs. The HTML generation logic embeds HTML tags directly in COBOL WRITE statements — a pattern that should be replaced with a template engine. Uses ALTER GO TO for dynamic dispatch (an obsolete COBOL feature).

**Modernization Risks**:
- ALTER GO TO is obsolete and has no Java equivalent — requires refactoring to conditional logic
- Dual-format output (text + HTML) should be unified with a template engine (Thymeleaf/FreeMarker)
- Driver/subroutine split (CBSTM03A/B) should be consolidated into a single service

**Recommended Approach**: Consolidate into StatementGenerationService. Replace ALTER GO TO with polymorphic dispatch. Use a template engine for output formatting.

---

### Rank 9: `CBTRN03C.cbl` — Transaction Report Generator

| Metric | Value | Score |
|---|---|---|
| **Lines of Code** | 649 | — |
| **IF Statements** | 38 | — |
| **EVALUATE Blocks** | 4 | — |
| **PERFORM Calls** | 72 | — |
| **Files** | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM (input), TRANREPT (output) | — |
| **Complexity Score** | **7** | — |
| **Risk Score** | **5** | — |
| **Business Impact** | **7** | — |
| **Composite Score** | **6.4** | — |

**Why #9**: Has the highest PERFORM count (72) of any program, indicating highly modular but deeply nested procedural flow. Reads from 5 input files to produce a formatted report with page/account/grand totals. The report formatting logic (headers, detail lines, totals) with page-break control is non-trivial to replicate in Java.

**Modernization Risks**:
- 72 PERFORMs create a complex call graph within a single program
- Page-oriented report formatting with control breaks
- 5-file join for report enrichment
- GDG output requires equivalent versioning strategy

**Recommended Approach**: Map to Spring Batch with JasperReports or custom report service. Replace GDG versioning with timestamped file storage.

---

### Rank 10: `COBIL00C.cbl` — Bill Payment

| Metric | Value | Score |
|---|---|---|
| **Lines of Code** | 572 | — |
| **IF Statements** | 10 | — |
| **EVALUATE Blocks** | 18 | — |
| **PERFORM Calls** | 38 | — |
| **CICS Ops** | READ ×2, REWRITE, STARTBR, READPREV, ENDBR, WRITE | — |
| **VSAM Files** | TRANSACT (R/W), ACCTDAT (R/W), CXACAIX (R) | — |
| **Complexity Score** | **6** | — |
| **Risk Score** | **8** | — |
| **Business Impact** | **9** | — |
| **Composite Score** | **7.1** | — |

**Why #10**: Processes bill payments — a revenue-critical operation that creates transactions and updates account balances. Performs both READ and WRITE/REWRITE on TRANSACT and ACCTDAT in a single CICS transaction, creating a multi-file consistency requirement. Uses the alternate index (CXACAIX) for account-to-card resolution. The 18 EVALUATE blocks handle various payment scenarios and error conditions.

**Modernization Risks**:
- Payment processing requires atomicity across TRANSACT and ACCTDAT updates
- Account balance update must be idempotent to prevent double-payment
- Alternate index usage requires database secondary index mapping

**Recommended Approach**: Implement BillPaymentService with @Transactional and optimistic locking. Add idempotency key to prevent duplicate payments.

---

## Consolidated Ranking Table

| Rank | Program | LOC | IFs | EVALs | PERFORMs | Files | Complexity | Risk | Impact | **Score** |
|---|---|---|---|---|---|---|---|---|---|---|
| 1 | **COACTUPC** | 4,236 | 164 | 20 | 64 | 3 R/W | 10 | 10 | 9 | **9.7** |
| 2 | **CBTRN02C** | 731 | 48 | 0 | 61 | 6 | 8 | 10 | 10 | **9.2** |
| 3 | **CBACT04C** | 652 | 43 | 0 | 56 | 5 | 7 | 9 | 10 | **8.5** |
| 4 | **COCRDLIC** | 1,459 | 59 | 18 | 34 | 1 | 9 | 7 | 7 | **7.8** |
| 5 | **COCRDUPC** | 1,560 | 72 | 16 | 26 | 2 R/W | 8 | 8 | 7 | **7.7** |
| 6 | **COTRN02C** | 783 | 14 | 26 | 61 | 4 | 7 | 8 | 9 | **7.6** |
| 7 | **COBIL00C** | 572 | 10 | 18 | 38 | 3 R/W | 6 | 8 | 9 | **7.1** |
| 8 | **COACTVWC** | 941 | 28 | 10 | 21 | 3 R | 7 | 6 | 8 | **7.0** |
| 9 | **CBSTM03A** | 924 | 15 | 9 | 29 | 6 | 7 | 6 | 8 | **7.0** |
| 10 | **CBTRN03C** | 649 | 38 | 4 | 72 | 6 | 7 | 5 | 7 | **6.4** |

---

## Modernization Wave Planning

Based on the hotspot analysis and dependency clustering (see DEPENDENCY_MAP.md), the recommended modernization wave order is:

### Wave 1: Security & Navigation (Low Risk, Quick Win)
- **COSGN00C** (Sign-on) → Spring Security authentication
- **COMEN01C** / **COADM01C** (Menus) → React/Angular routing
- **COUSR00C-03C** (User CRUD) → Spring Data JPA + REST API
- **Rationale**: Isolated to USRSEC file, no financial data, establishes auth foundation

### Wave 2: Read-Only Screens (Medium Complexity, High Visibility)
- **COACTVWC** (#8) → Account view REST endpoint
- **COCRDLIC** (#4) → Paginated card list API
- **COCRDSLC** → Card detail API
- **COTRN00C** / **COTRN01C** → Transaction list/detail APIs
- **Rationale**: Read-only operations reduce risk; delivers visible value quickly

### Wave 3: Write Operations (High Risk, Core Business Logic)
- **COACTUPC** (#1) → Account/customer update service
- **COCRDUPC** (#5) → Card update service
- **COTRN02C** (#6) → Transaction entry service
- **COBIL00C** (#7) → Bill payment service
- **Rationale**: Write operations require careful transaction management; build on Wave 2 read infrastructure

### Wave 4: Batch Processing (Highest Risk, Financial Core)
- **CBTRN02C** (#2) → Spring Batch transaction posting job
- **CBACT04C** (#3) → Spring Batch interest calculation job
- **Rationale**: Core financial processing; must validate calculation parity with COBOL

### Wave 5: Reporting & Utilities
- **CBSTM03A/B** (#9) → Statement generation service with template engine
- **CBTRN03C** (#10) → Report generation service
- **CORPT00C** → Report request API
- **CBEXPORT/CBIMPORT** → Data migration utilities
- **Rationale**: Read-only consumers; can run in parallel with legacy during transition

---

## Key Risk Mitigations

| Risk | Mitigation |
|---|---|
| **Financial calculation precision** | Use `BigDecimal` exclusively; create COBOL-to-Java calculation comparison test suite |
| **Multi-file consistency** | Replace VSAM multi-file writes with JPA @Transactional on relational database |
| **VSAM browse pagination** | Implement keyset pagination instead of STARTBR/READNEXT offset pattern |
| **Plain-text passwords** | Implement bcrypt/scrypt hashing during migration (CSUSR01Y stores plain text) |
| **ALTER GO TO (CBSTM03A)** | Refactor to conditional/polymorphic dispatch before or during conversion |
| **COPY REPLACING (COACTUPC)** | Create generic attribute-setting utility class |
| **Concurrent access** | Add optimistic locking (@Version) to replace CICS single-threading model |
| **GDG versioning** | Replace with timestamped file naming or S3 versioning |
