# CardDemo Hotspot Report

> **Generated:** 2026-03-25 | **Application:** CardDemo — Mainframe Credit Card Management System
> **Purpose:** Identify the top 10 modules requiring the most modernization effort, ranked by complexity, risk, and business impact.

---

## Table of Contents

1. [Scoring Methodology](#1-scoring-methodology)
2. [Top 10 Hotspot Summary](#2-top-10-hotspot-summary)
3. [Detailed Hotspot Analysis](#3-detailed-hotspot-analysis)
4. [Duplicate Logic Findings](#4-duplicate-logic-findings)
5. [Risk Heat Map](#5-risk-heat-map)
6. [Recommended Migration Order](#6-recommended-migration-order)

---

## 1. Scoring Methodology

Each module is scored across three dimensions on a 1–10 scale:

| Dimension         | Weight | Criteria                                                                                     |
|-------------------|--------|----------------------------------------------------------------------------------------------|
| **Complexity**    | 40%    | Lines of code, number of copybooks, VSAM files accessed, control flow branches, REDEFINES usage, validation logic density |
| **Risk**          | 30%    | Data mutation (WRITE/REWRITE/DELETE), PII handling, financial calculations, error-handling depth, external dependencies |
| **Business Impact** | 30%  | Transaction volume, user-facing criticality, downstream dependencies, revenue/compliance relevance |

**Composite Score** = (Complexity × 0.4) + (Risk × 0.3) + (Business Impact × 0.3)

---

## 2. Top 10 Hotspot Summary

| Rank | Module     | Lines | Type          | Composite | Complexity | Risk | Biz Impact | Primary Domain          |
|------|------------|-------|---------------|-----------|------------|------|------------|-------------------------|
| 1    | COACTUPC   | 4,236 | Online CICS   | **9.4**   | 10         | 9    | 9          | Account & Customer Update |
| 2    | CBTRN02C   | 731   | Batch         | **8.5**   | 8          | 10   | 8          | Transaction Posting      |
| 3    | COTRTLIC   | 2,098 | Online DB2    | **8.1**   | 9          | 7    | 8          | Transaction Type Mgmt    |
| 4    | COCRDUPC   | 1,560 | Online CICS   | **7.9**   | 9          | 8    | 7          | Card Update              |
| 5    | COCRDLIC   | 1,459 | Online CICS   | **7.5**   | 8          | 6    | 8          | Card List / Browse       |
| 6    | COTRTUPC   | 1,702 | Online DB2    | **7.4**   | 8          | 7    | 7          | Transaction Type Update  |
| 7    | COPAUS0C   | 1,032 | Online IMS/MQ | **7.3**   | 8          | 7    | 7          | Authorization Summary    |
| 8    | CBACT04C   | 652   | Batch         | **7.2**   | 7          | 9    | 6          | Interest Calculation     |
| 9    | COACTVWC   | 941   | Online CICS   | **7.0**   | 7          | 5    | 9          | Account View             |
| 10   | CBSTM03A   | 924   | Batch         | **6.9**   | 7          | 6    | 8          | Statement Generation     |

---

## 3. Detailed Hotspot Analysis

### Rank 1 — COACTUPC (Account Update) — Score: 9.4

| Metric              | Value                                                   |
|----------------------|---------------------------------------------------------|
| **Lines of Code**    | 4,236 (largest program in the entire application)       |
| **Copybooks Used**   | 15 unique (+ 39 CSSETATY COPY REPLACING invocations)    |
| **VSAM Files**       | 5 (ACCTDAT R/W, CUSTDAT R/W, CARDAIX R, CXACAIX R, CARDDAT R) |
| **BMS Map**          | COACTUP (CACTUPA)                                       |

**Complexity Drivers:**
- 39 separate `COPY CSSETATY REPLACING` invocations for field-level attribute setting — a code generation pattern that inflates working storage and makes maintenance error-prone
- Extensive inline validation: phone numbers, SSN format, date ranges (open, expiry, reissue), credit limits (cash vs. total), state/country codes against 1,318-line lookup table (CSLKPCDY)
- Multiple edit-flag variables (`FLG-*-NOT-OK`, `FLG-*-BLANK`) for each editable field creating a complex state machine
- Date validation via CSUTLDWY (working-storage date editing) and CSUTLDPY (CEEDAYS linkage)
- Dual entity update: modifies both Account (ACCTDAT) and Customer (CUSTDAT) in a single transaction

**Risk Factors:**
- Writes to 2 master files (ACCTDAT, CUSTDAT) — data corruption here affects the entire system
- Handles PII: SSN, address, phone numbers, date of birth
- Financial fields: credit limit changes, cash advance limit adjustments
- No explicit transaction rollback — if account update succeeds but customer update fails, data inconsistency results

**Modernization Recommendation:**
- Split into `AccountUpdateService` and `CustomerUpdateService` with proper JPA transactions
- Extract validation into a shared `ValidationService` (reusable for all update screens)
- Replace 39 CSSETATY invocations with a generic field-error-rendering mechanism (e.g., Bean Validation + Thymeleaf error tags)
- Wrap both entity updates in a single `@Transactional` boundary

---

### Rank 2 — CBTRN02C (Transaction Posting) — Score: 8.5

| Metric              | Value                                                   |
|----------------------|---------------------------------------------------------|
| **Lines of Code**    | 731                                                     |
| **Copybooks Used**   | 6 (CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y)  |
| **VSAM Files**       | 6 (DALYTRAN R, TRANSACT R, XREFFILE R, ACCTFILE R/W, TCATBALF R/W, DALYREJS W) |

**Complexity Drivers:**
- Core batch posting engine — reads daily transactions and posts them to master files
- Cross-reference lookups (card → account) for every transaction
- Updates account balances (ACCTDAT) and category running totals (TCATBALF)
- Rejected transactions written to separate reject file (DALYREJS) with error codes

**Risk Factors:**
- **Highest data mutation risk** in the system: writes to 3 VSAM files per transaction
- Financial accuracy critical: balance updates must be exact (no rounding errors)
- Reject handling: incorrect rejection logic = lost revenue or double-posting
- No idempotency: re-running without cleanup could double-post transactions

**Modernization Recommendation:**
- Implement as a Spring Batch `ItemReader`/`ItemProcessor`/`ItemWriter` pipeline
- Add idempotency checks (transaction ID deduplication)
- Use database transactions with proper isolation levels
- Implement a dead-letter queue pattern for rejects instead of flat file

---

### Rank 3 — COTRTLIC (Transaction Type List — DB2) — Score: 8.1

| Metric              | Value                                                   |
|----------------------|---------------------------------------------------------|
| **Lines of Code**    | 2,098 (second-largest program)                          |
| **Module**           | Optional — Transaction Type DB2                         |
| **Technology**       | Embedded SQL (DB2 cursors), CICS BMS                    |

**Complexity Drivers:**
- DB2 cursor management for paginated browsing of transaction types
- Complex screen navigation: list, select for update, select for delete
- Dual-mode operation (list + CRUD dispatch)
- Embedded SQL with host variables and SQLCA error checking

**Risk Factors:**
- DB2 dependency introduces a different data access pattern from VSAM programs
- Cursor state management across CICS pseudo-conversational interactions
- Error handling for SQL errors alongside CICS errors

**Modernization Recommendation:**
- Map to Spring Data JPA `@Repository` with pagination (`Pageable`)
- Replace DB2 cursors with JPA `findAll` or `@Query` with pagination
- Separate list and CRUD into distinct REST endpoints / service methods

---

### Rank 4 — COCRDUPC (Card Update) — Score: 7.9

| Metric              | Value                                                   |
|----------------------|---------------------------------------------------------|
| **Lines of Code**    | 1,560                                                   |
| **Copybooks Used**   | 11                                                      |
| **VSAM Files**       | 2 (CARDDAT R/W, CARDAIX R)                              |

**Complexity Drivers:**
- Card number validation, CVV validation, expiration date logic
- Account path lookup via alternate index (CARDAIX)
- CSSTRPFY PF-key mapping for navigation
- Shares significant structural overlap with COCRDSLC (card detail view) — see [Duplicate Logic Findings](#4-duplicate-logic-findings)

**Risk Factors:**
- Writes to CARDDAT (card master) — incorrect updates affect card usability
- CVV code handling (security-sensitive)
- Status change logic (activate/deactivate cards)

**Modernization Recommendation:**
- Merge view and update logic into a single `CardService` with `findById` and `update` methods
- Extract shared card-loading logic from COCRDSLC and COCRDUPC into one reusable service method
- Implement proper input validation with Bean Validation annotations

---

### Rank 5 — COCRDLIC (Card List) — Score: 7.5

| Metric              | Value                                                   |
|----------------------|---------------------------------------------------------|
| **Lines of Code**    | 1,459                                                   |
| **Copybooks Used**   | 9                                                       |
| **VSAM Files**       | 1 (CARDDAT R/Browse)                                    |

**Complexity Drivers:**
- VSAM browse (STARTBR / READNEXT / READPREV) for paginated card listing
- Selection processing: dispatches to card detail (COCRDSLC) or card update (COCRDUPC)
- Page-forward and page-backward logic with cursor repositioning
- Filtering by account ID via alternate index

**Risk Factors:**
- Read-only for data, but incorrect browse logic causes missed or duplicate cards in display
- Navigation hub: if broken, card detail and card update become unreachable

**Modernization Recommendation:**
- Replace VSAM browse with JPA `findByAccountId` with `Pageable`
- Implement as a paginated REST endpoint or Spring MVC controller
- Use `Page<Card>` return type for clean pagination

---

### Rank 6 — COTRTUPC (Transaction Type Update — DB2) — Score: 7.4

| Metric              | Value                                                   |
|----------------------|---------------------------------------------------------|
| **Lines of Code**    | 1,702                                                   |
| **Module**           | Optional — Transaction Type DB2                         |
| **Technology**       | Embedded SQL, CICS BMS                                  |

**Complexity Drivers:**
- Full CRUD operations (INSERT, UPDATE, DELETE) via embedded SQL
- Complex screen flow: add mode vs. edit mode vs. delete confirmation
- SQL error handling with SQLCODE checking
- Heavy validation on transaction type codes and descriptions

**Risk Factors:**
- Direct DB2 DML (INSERT/UPDATE/DELETE) — data mutation risk
- Shares structural patterns with COTRTLIC — see [Duplicate Logic Findings](#4-duplicate-logic-findings)
- No optimistic locking — concurrent updates could overwrite each other

**Modernization Recommendation:**
- Implement as a Spring Data JPA `CrudRepository` with `@Transactional` methods
- Add `@Version` field for optimistic locking
- Consolidate with COTRTLIC into a single `TransactionTypeService`

---

### Rank 7 — COPAUS0C (Authorization Summary — IMS/MQ) — Score: 7.3

| Metric              | Value                                                   |
|----------------------|---------------------------------------------------------|
| **Lines of Code**    | 1,032                                                   |
| **Module**           | Optional — Authorization IMS/DB2/MQ                     |
| **Technology**       | IMS DL/I calls, MQ messaging, CICS BMS                  |

**Complexity Drivers:**
- Three different middleware technologies (IMS + MQ + CICS)
- IMS segment retrieval with PCB status checking
- Paginated browsing of IMS hierarchical data
- MQ message correlation for authorization decisions

**Risk Factors:**
- IMS dependency is the most exotic technology in the codebase — hardest to find modernization expertise
- MQ integration for real-time authorization decisions is latency-sensitive
- IMS PCB error handling is fundamentally different from VSAM error handling

**Modernization Recommendation:**
- Replace IMS with a relational database (JPA entity for authorization messages)
- Replace MQ trigger with a message-driven bean or Spring Cloud Stream consumer
- This module represents the highest **technology migration risk** due to IMS

---

### Rank 8 — CBACT04C (Interest Calculation) — Score: 7.2

| Metric              | Value                                                   |
|----------------------|---------------------------------------------------------|
| **Lines of Code**    | 652                                                     |
| **Copybooks Used**   | 5 (CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y)  |
| **VSAM Files**       | 4 (TCATBALF R/W, XREFFILE R, ACCTFILE R/W, DISCGRP R)  |

**Complexity Drivers:**
- Financial calculation logic: interest rate lookup by account group, transaction type, and category
- Multi-file join: Account → Group → Discount Rate → Category Balance
- Date-based parameter processing (PARM field for calculation date)
- Generates new transaction records for interest charges

**Risk Factors:**
- **Financial accuracy is paramount** — rounding errors in interest calculations have direct revenue and compliance impact
- Reads discount group rates and applies them to category balances — complex business rule
- Writes interest charge transactions back to TRANSACT — circular data flow
- Regulatory compliance: interest calculation methods may be subject to audit

**Modernization Recommendation:**
- Implement using `BigDecimal` exclusively (never `double` or `float`)
- Create a dedicated `InterestCalculationService` with comprehensive unit tests
- Implement as a Spring Batch job with chunk-based processing
- Add audit logging for every interest charge generated

---

### Rank 9 — COACTVWC (Account View) — Score: 7.0

| Metric              | Value                                                   |
|----------------------|---------------------------------------------------------|
| **Lines of Code**    | 941                                                     |
| **Copybooks Used**   | 13                                                      |
| **VSAM Files**       | 4 (ACCTDAT R, CUSTDAT R, CARDAIX R, CXACAIX R)         |

**Complexity Drivers:**
- Multi-file read joining Account + Customer + Card data for a single display
- Alternate index navigation (CXACAIX for account→card lookup)
- Complex screen layout assembling data from 3 different entities
- Shares significant read logic with COACTUPC — see [Duplicate Logic Findings](#4-duplicate-logic-findings)

**Risk Factors:**
- Read-only (low data risk), but it is the **most-used screen** in the application
- If broken, users cannot view any account information
- Displays PII (customer name, address) — needs access control in modernized version

**Modernization Recommendation:**
- Implement as a read-only `AccountViewController` / REST endpoint
- Reuse the same `AccountService.findById()` method used by Account Update
- Extract the account+customer+card join into a shared `AccountDetailDTO` assembler

---

### Rank 10 — CBSTM03A (Statement Generation) — Score: 6.9

| Metric              | Value                                                   |
|----------------------|---------------------------------------------------------|
| **Lines of Code**    | 924                                                     |
| **Copybooks Used**   | 4 (COSTM01, CVACT03Y, CUSTREC, CVACT01Y)               |
| **VSAM Files**       | 4 (TRNXFILE R, XREFFILE R, ACCTFILE R, CUSTFILE R)      |
| **Sub-program**      | CBSTM03B (230 lines — report writer)                    |

**Complexity Drivers:**
- Multi-step pipeline: sort transactions → read sorted file → join with xref/account/customer → generate statement
- Calls sub-program CBSTM03B (report writer) multiple times for different report sections
- Produces both text (STMTFILE) and HTML (HTMLFILE) output formats
- Page-break logic, running totals, account-level subtotals, grand totals

**Risk Factors:**
- Customer-facing output — statement errors are highly visible
- Must accurately aggregate transactions per account per statement period
- HTML generation in COBOL is fragile and hard to maintain

**Modernization Recommendation:**
- Implement as a Spring Batch job with a reporting library (JasperReports or Apache POI)
- Replace COBOL HTML generation with a proper templating engine (Thymeleaf)
- Consolidate CBSTM03A and CBSTM03B into a single `StatementGenerationService`

---

## 4. Duplicate Logic Findings

During analysis, the following instances of **duplicate or near-duplicate logic** were identified. These should be consolidated during modernization to reduce maintenance burden and defect risk.

| # | Pattern                            | Programs Involved                    | Lines Affected | Recommendation                                         |
|---|-------------------------------------|--------------------------------------|----------------|--------------------------------------------------------|
| 1 | Account + Customer read/join logic | COACTVWC, COACTUPC                   | ~200 lines each| Extract into shared `AccountDetailService.load()`      |
| 2 | Card read via primary key + AIX    | COCRDSLC, COCRDUPC                   | ~150 lines each| Extract into shared `CardService.findByNumber()`       |
| 3 | VSAM browse with pagination        | COCRDLIC, COTRN00C, COUSR00C         | ~300 lines each| Create generic `PaginatedBrowseService<T>`             |
| 4 | Transaction record structure       | CVTRA05Y, CVTRA06Y, COSTM01         | 3 copybooks    | Unify into single `Transaction` entity with status flag|
| 5 | Customer record layout             | CVCUS01Y, CUSTREC                    | 2 copybooks    | Merge into single `Customer` class                     |
| 6 | PF-key handling / AID processing   | All 17 online programs               | ~30 lines each | Extract into shared `KeyHandler` utility               |
| 7 | Screen title / date display        | All 17 online programs (COTTL01Y, CSDAT01Y) | ~20 lines each | Handle via page layout template / base controller |
| 8 | Abend / error handling             | All batch programs (CEE3ABD calls)   | ~15 lines each | Replace with global exception handler                  |
| 9 | COMMAREA initialization + XCTL return | All online programs (COCOM01Y)    | ~25 lines each | Replace with HTTP session / JWT + Spring Security       |
| 10| Export record REDEFINES structure  | CBEXPORT, CBIMPORT (CVEXPORT)        | ~100 lines each| Replace with polymorphic serialization (Jackson)       |
| 11| DB2 cursor pagination              | COTRTLIC, COTRTUPC                   | ~200 lines each| Replace with Spring Data JPA `Pageable`                |

**Estimated Savings:** Consolidating these patterns eliminates approximately **2,500–3,000 lines** of duplicate logic, reducing the modernized codebase by ~15%.

---

## 5. Risk Heat Map

```
                    Low Business Impact    Medium Business Impact    High Business Impact
                 ┌──────────────────────┬─────────────────────────┬────────────────────────┐
 High Complexity │                      │  COTRTLIC (2,098 LOC)   │  COACTUPC (4,236 LOC)  │
                 │                      │  COTRTUPC (1,702 LOC)   │                        │
                 │                      │  COCRDUPC (1,560 LOC)   │                        │
                 │                      │  COCRDLIC (1,459 LOC)   │                        │
                 ├──────────────────────┼─────────────────────────┼────────────────────────┤
 Med Complexity  │  COPAUS0C (1,032)    │  CBACT04C (652 LOC)     │  CBTRN02C (731 LOC)    │
                 │  COPAUS1C (604)      │                         │  COACTVWC (941 LOC)    │
                 │                      │                         │  CBSTM03A (924 LOC)    │
                 ├──────────────────────┼─────────────────────────┼────────────────────────┤
 Low Complexity  │  COBSWAIT (41 LOC)   │  CBACT02C (178 LOC)     │  COSGN00C (260 LOC)    │
                 │  CSUTLDTC (157 LOC)  │  CBACT03C (178 LOC)     │  COMEN01C (308 LOC)    │
                 │  UNUSED1Y            │  CBCUS01C (178 LOC)     │  COTRN00C (699 LOC)    │
                 └──────────────────────┴─────────────────────────┴────────────────────────┘

 ■ Top-right quadrant = highest priority for modernization attention
 ■ Bottom-left quadrant = lowest priority / utility programs
```

---

## 6. Recommended Migration Order

Based on the hotspot analysis, dependencies, and risk assessment, the recommended migration order is:

### Phase 1 — Foundation (Weeks 1–3)
Establish shared infrastructure and low-risk, high-reuse components.

| Order | Module(s)                          | Rationale                                              |
|-------|------------------------------------|--------------------------------------------------------|
| 1     | Copybook data structures           | Define JPA entities from CVACT01Y, CVCUS01Y, etc.     |
| 2     | COCOM01Y (Commarea)                | Design session/auth model (Spring Security + JWT)      |
| 3     | CSUSR01Y + COSGN00C                | Auth & login — enables all other testing               |
| 4     | CSUTLDTC                           | Date utility — dependency for multiple programs        |
| 5     | COBSWAIT / Assembler programs      | Simple utilities — quick wins                          |

### Phase 2 — Read-Only Screens (Weeks 4–6)
Migrate inquiry/view programs to validate data access patterns.

| Order | Module(s)                          | Rationale                                              |
|-------|------------------------------------|--------------------------------------------------------|
| 6     | COMEN01C / COADM01C                | Menu navigation — enables screen flow                  |
| 7     | COACTVWC (Account View)            | High-use, read-only — validates account data access    |
| 8     | COCRDSLC (Card Detail)             | Read-only — validates card data access                 |
| 9     | COTRN01C (Transaction View)        | Read-only — validates transaction data access          |

### Phase 3 — List/Browse Screens (Weeks 7–9)
Migrate paginated list views, extracting shared pagination patterns.

| Order | Module(s)                          | Rationale                                              |
|-------|------------------------------------|--------------------------------------------------------|
| 10    | COCRDLIC (Card List)               | First pagination implementation — creates reusable pattern |
| 11    | COTRN00C (Transaction List)        | Reuse pagination pattern from Card List                |
| 12    | COUSR00C (User List)               | Reuse pagination pattern; admin-only                   |

### Phase 4 — Update/Mutation Screens (Weeks 10–14)
Migrate the highest-complexity, highest-risk modules.

| Order | Module(s)                          | Rationale                                              |
|-------|------------------------------------|--------------------------------------------------------|
| 13    | COUSR01C–03C (User CRUD)           | Simpler CRUD — warm up for complex updates             |
| 14    | COCRDUPC (Card Update)             | Card update — moderate complexity                      |
| 15    | COTRN02C (Transaction Add)         | Validates transaction creation flow                    |
| 16    | COBIL00C (Bill Payment)            | Financial mutation — needs careful testing              |
| 17    | COACTUPC (Account Update)          | **#1 hotspot** — migrate last among online programs    |
| 18    | CORPT00C (Reports)                 | Report submission — triggers batch jobs                |

### Phase 5 — Batch Programs (Weeks 15–19)
Migrate batch processing to Spring Batch.

| Order | Module(s)                          | Rationale                                              |
|-------|------------------------------------|--------------------------------------------------------|
| 19    | CBACT01C–03C (Read/Print)          | Simple batch readers — establishes Spring Batch patterns|
| 20    | CBTRN02C (Transaction Posting)     | **#2 hotspot** — core batch engine                     |
| 21    | CBACT04C (Interest Calculation)    | **#8 hotspot** — financial accuracy critical           |
| 22    | CBTRN03C (Transaction Report)      | Reporting batch                                        |
| 23    | CBSTM03A/B (Statement Generation)  | **#10 hotspot** — customer-facing output               |
| 24    | CBEXPORT / CBIMPORT                | Data migration utilities                               |

### Phase 6 — Optional Modules (Weeks 20–22)
Migrate optional modules if in scope.

| Order | Module(s)                          | Rationale                                              |
|-------|------------------------------------|--------------------------------------------------------|
| 25    | Transaction Type DB2 module        | DB2 → JPA migration                                    |
| 26    | Authorization IMS/DB2/MQ module    | Most complex technology stack — highest migration risk  |
| 27    | VSAM-MQ module                     | MQ → messaging framework (Spring Cloud Stream / JMS)   |

---

*End of Hotspot Report*
