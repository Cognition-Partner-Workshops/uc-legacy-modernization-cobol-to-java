# CardDemo Hotspot Report

> **Generated**: 2026-03-25 | **Application**: CardDemo (Credit Card Management System)
> **Methodology**: Static analysis of LOC, file I/O operations, decision points, copybook dependencies, and business criticality

---

## Table of Contents

1. [Scoring Methodology](#scoring-methodology)
2. [Top 10 Hotspot Modules](#top-10-hotspot-modules)
3. [Detailed Analysis per Module](#detailed-analysis-per-module)
4. [Risk Heat Map](#risk-heat-map)
5. [Recommended Migration Order](#recommended-migration-order)
6. [Modernization Patterns per Hotspot](#modernization-patterns-per-hotspot)

---

## Scoring Methodology

Each module is scored across three weighted dimensions:

| Dimension | Weight | Metrics Used |
|-----------|--------|-------------|
| **Complexity** | 40% | Lines of code, number of VSAM file operations, number of copybook dependencies, CALL/XCTL statements, field validation logic |
| **Risk** | 30% | Data mutation operations (WRITE/REWRITE/DELETE), error handling paths, multi-file coordination, financial calculations, PII handling |
| **Business Impact** | 30% | Core transaction path (yes/no), user-facing (yes/no), data integrity responsibility, downstream dependencies, batch cycle criticality |

**Score Range**: 1-10 per dimension. **Composite** = (Complexity x 0.4) + (Risk x 0.3) + (Impact x 0.3)

---

## Top 10 Hotspot Modules

| Rank | Module | Lines | Type | Complexity | Risk | Impact | **Composite** | Primary Concern |
|------|--------|-------|------|:----------:|:----:|:------:|:-------------:|-----------------|
| **1** | **COACTUPC** | 4,237 | Online CICS | 10 | 9 | 9 | **9.4** | Largest program; account update with extensive field validation |
| **2** | **CBTRN02C** | 731 | Batch | 8 | 10 | 10 | **9.2** | Core transaction posting; multi-file writes with reject handling |
| **3** | **CBACT04C** | 652 | Batch | 7 | 9 | 9 | **8.2** | Interest calculation; financial precision, 5-file coordination |
| **4** | **CBSTM03A** | 924 | Batch | 8 | 7 | 8 | **7.7** | Statement generation; calls subroutine, 4-file reads, dual output |
| **5** | **COCRDLIC** | 1,460 | Online CICS | 8 | 6 | 7 | **7.1** | Card list with VSAM browse pagination; complex screen handling |
| **6** | **COCRDUPC** | 1,560 | Online CICS | 8 | 7 | 6 | **7.1** | Card update; REWRITE operations, field validation |
| **7** | **CBTRN03C** | 649 | Batch | 7 | 5 | 8 | **6.7** | Transaction report; 6-file coordination, report formatting |
| **8** | **COACTVWC** | 942 | Online CICS | 7 | 5 | 7 | **6.4** | Account view; 4-file reads, error handling for each |
| **9** | **COBIL00C** | 572 | Online CICS | 6 | 8 | 7 | **6.9** | Bill payment; financial writes, balance updates |
| **10** | **COTRN02C** | 783 | Online CICS | 7 | 7 | 6 | **6.7** | Transaction add; multi-file validation and writes |

---

## Detailed Analysis per Module

### #1 -- COACTUPC (Account Update) -- Composite: 9.4

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 4,237 | Largest program in the entire codebase (2.7x larger than #2 core program) |
| **VSAM Files** | 4 | ACCTDAT (R/W), CARDAIX (R), CXACAIX (R), CUSTDAT (R/W) |
| **Copybooks** | 13 | CSUTLDWY, COCOM01Y, COTTL01Y, COACTUPC, CSDAT01Y, CSMSG01Y, CSMSG02Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, DFHBMSCA |
| **BMS Map** | COACTUP | Account update screen with 20+ editable fields |
| **Validation Logic** | Extensive | Phone number formats, SSN validation, date validation (open date, expiry, reissue), credit limit validation, FICO score range checks, state code validation |
| **Data Mutations** | REWRITE | Updates both account (ACCTDAT) and customer (CUSTDAT) records |

**Why #1**: This program combines extreme size (4,237 LOC), complex multi-field validation (the bulk of the code), dual-file REWRITE operations, and direct account/customer data mutation. A bug here corrupts core financial data. Migration requires decomposing the monolithic validation into a service layer with proper input sanitization.

**Migration Recommendations**:
- Decompose into `AccountUpdateService` with separate validation, persistence, and presentation layers
- Extract field validators into reusable `@Valid` annotations (phone, SSN, date, credit limit)
- Replace BMS map with a reactive form with client-side + server-side validation
- Replace VSAM REWRITE with JPA `@Transactional` update

---

### #2 -- CBTRN02C (Transaction Posting) -- Composite: 9.2

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 731 | Core batch transaction processing |
| **Files Read** | 3 | DALYTRAN, XREFFILE, ACCTFILE |
| **Files Written** | 3 | TRANFILE, DALYREJS, TCATBALF |
| **Copybooks** | 5 | CVTRA06Y, CVTRA05Y, CVACT03Y, CVACT01Y, CVTRA01Y |
| **JCL Job** | POSTTRAN | Critical batch step — must complete for cycle to proceed |
| **Error Handling** | Reject file | Invalid transactions written to DALYREJS with error codes |

**Why #2**: This is the heart of the batch cycle. It reads daily transactions, validates them against cross-reference and account data, posts valid ones to the master transaction file, updates category balances, and routes rejects to a separate file. Failure here halts the entire nightly batch cycle and corrupts financial totals.

**Migration Recommendations**:
- Implement as a Spring Batch `ItemProcessor` + `ItemWriter` with chunk-based processing
- Use database transactions with rollback capability (VSAM has no rollback)
- Implement dead-letter queue pattern for rejected transactions
- Add idempotency keys to prevent double-posting on restart

---

### #3 -- CBACT04C (Interest Calculation) -- Composite: 8.2

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 652 | Financial calculation engine |
| **Files Read** | 4 | TCATBALF, XREFFILE, ACCTFILE, DISCGRP |
| **Files Written** | 1 | TRANSACT (interest transactions) |
| **Copybooks** | 5 | CVTRA01Y, CVACT03Y, CVTRA02Y, CVACT01Y, CVTRA05Y |
| **JCL Job** | INTCALC | Runs after POSTTRAN; depends on correct balances |
| **Financial Precision** | S9(09)V99 | Must maintain exact decimal precision |

**Why #3**: Financial calculations with decimal precision requirements. Reads category balances and disclosure group interest rates, then generates interest charge transactions. Any rounding error or incorrect rate application directly impacts customer billing. The `S9(09)V99` packed decimal format must map exactly to `BigDecimal` in Java.

**Migration Recommendations**:
- Use `BigDecimal` with `RoundingMode.HALF_UP` for all monetary calculations
- Implement as a Spring Batch step with audit logging for every interest charge
- Parameterize calculation date (currently passed via JCL PARM)
- Add reconciliation checks: sum of interest charges = expected totals

---

### #4 -- CBSTM03A (Statement Generation) -- Composite: 7.7

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 924 | Statement generation main program |
| **Files Read** | 4 | TRNXFILE, XREFFILE, ACCTFILE, CUSTFILE |
| **Files Written** | 2 | STMTFILE (text), HTMLFILE (HTML) |
| **Subroutine** | CBSTM03B | Called 13+ times for file I/O operations |
| **Copybooks** | 4 | COSTM01, CVACT03Y, CUSTREC, CVACT01Y |
| **JCL Job** | CREASTMT | Runs at end of batch cycle |

**Why #4**: Complex multi-file joins to produce customer statements in both text and HTML formats. The CALL to CBSTM03B for every file operation creates an unusual co-routine pattern. Uses ALTER statements (modifying GO TO targets at runtime), which is one of the most difficult COBOL patterns to convert.

**Migration Recommendations**:
- Replace ALTER/GO TO with standard Java control flow
- Implement as a template-based report generator (Thymeleaf or JasperReports)
- Merge CBSTM03A and CBSTM03B into a single service class
- Consider generating PDF directly instead of text + HTML

---

### #5 -- COCRDLIC (Credit Card List) -- Composite: 7.1

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 1,460 | Paginated card browse |
| **VSAM Files** | 2 | CARDDAT (browse), CARDAIX (alternate index browse) |
| **BMS Map** | COCRDLI | List screen with selection, pagination (F7/F8) |
| **Copybooks** | 10 | Including CVCRD01Y, CVACT02Y |
| **XCTL Targets** | COCRDSLC, COCRDUPC, COMEN01C | Drill-down to detail/update |

**Why #5**: VSAM BROWSE operations with forward/backward pagination are one of the most complex CICS patterns to convert. The program manages an array of displayed records, handles selection of individual rows, and dispatches to detail/update programs. The pagination state must be maintained across pseudo-conversational CICS interactions.

**Migration Recommendations**:
- Replace VSAM BROWSE with SQL `SELECT ... LIMIT/OFFSET` or cursor-based pagination
- Implement as a paginated REST endpoint returning JSON
- Use a frontend data table component (React Table, AG Grid) for the list UI
- Selection/drill-down becomes simple URL navigation

---

### #6 -- COCRDUPC (Credit Card Update) -- Composite: 7.1

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 1,560 | Card data modification |
| **VSAM Files** | 2 | CARDDAT (R/REWRITE), CUSTDAT (R) |
| **BMS Map** | COCRDUP | Card edit screen |
| **Copybooks** | 14 | Including CSSTRPFY (string formatting) |
| **Data Mutations** | REWRITE | Direct card record modification |

**Why #6**: Second-largest online program. Similar complexity pattern to COACTUPC but focused on card data. The REWRITE operation modifies card records directly. Field validation includes card number format, expiration date, CVV, embossed name, and active status.

**Migration Recommendations**:
- Implement as `CreditCardUpdateService` with `@Transactional` JPA update
- Extract card validation into a shared `CardValidator` (reusable for COCRDSLC)
- Replace BMS edit screen with a web form with masked card number display

---

### #7 -- CBTRN03C (Transaction Report) -- Composite: 6.7

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 649 | Transaction detail report generator |
| **Files Read** | 5 | TRANFILE, CARDXREF, TRANTYPE, TRANCATG, DATEPARM |
| **Files Written** | 1 | TRANREPT (report output) |
| **Copybooks** | 5 | CVTRA05Y, CVACT03Y, CVTRA03Y, CVTRA04Y, CVTRA07Y |
| **JCL Job** | TRANREPT | Preceded by SORT step for date filtering |

**Why #7**: Reads from 5 different files to produce a formatted report with page totals, account totals, and grand totals. The JCL SORT step pre-filters by date range, so the COBOL program depends on correctly sorted input. Report formatting logic (column alignment, page breaks, subtotals) is brittle and spread across the program.

**Migration Recommendations**:
- Replace SORT pre-step + COBOL with a single SQL query joining relevant tables
- Use a reporting library (JasperReports, Apache POI) for formatted output
- Parameterize date range via API rather than JCL DATEPARM file

---

### #8 -- COACTVWC (Account View) -- Composite: 6.4

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 942 | Account detail display |
| **VSAM Files** | 4 | ACCTDAT (R), CARDAIX (R), CXACAIX (R), CUSTDAT (R) |
| **BMS Map** | COACTVW | Read-only account detail screen |
| **Copybooks** | 13 | Full set of entity copybooks |
| **Error Handling** | Extensive | Separate error messages for each file read failure |

**Why #8**: Although read-only, this program performs 4 separate VSAM reads to assemble a single account view. Each read has its own error handling path. This is a common pattern that maps well to a single SQL JOIN in a relational database, but the COBOL implementation requires significant code for each file access.

**Migration Recommendations**:
- Replace 4 VSAM reads with a single SQL JOIN query
- Implement as a REST `GET /accounts/{id}` endpoint returning a composite DTO
- Error handling simplifies to standard exception handling

---

### #9 -- COBIL00C (Bill Payment) -- Composite: 6.9

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 572 | Bill payment processing |
| **VSAM Files** | 3 | ACCTDAT (R/U), CXACAIX (R), TRANSACT (R/W) |
| **BMS Map** | COBIL00 | Payment amount entry screen |
| **Copybooks** | 10 | CVACT01Y, CVACT03Y, CVTRA05Y |
| **Financial Operations** | Balance update + transaction creation | Must be atomic |

**Why #9**: Financial transaction that creates a payment transaction record AND updates the account balance. These two operations must be atomic — if one succeeds and the other fails, the data becomes inconsistent. VSAM has no built-in transaction support, so the program relies on careful ordering and error checking. This is a critical data integrity concern.

**Migration Recommendations**:
- Wrap in `@Transactional` with database-level atomicity
- Implement as `BillPaymentService` with idempotency protection
- Add audit trail for all payment operations
- Consider event-driven architecture (payment event triggers balance update)

---

### #10 -- COTRN02C (Transaction Add) -- Composite: 6.7

| Metric | Value | Details |
|--------|-------|---------|
| **Lines of Code** | 783 | New transaction entry |
| **VSAM Files** | 4 | CXACAIX (R), CARDXREF (R), TRANSACT (R/W) |
| **BMS Map** | COTRN02 | Transaction entry screen |
| **Copybooks** | 10 | CVTRA05Y, CVACT01Y, CVACT03Y |
| **Subroutine Calls** | CSUTLDTC | Date validation (2 calls) |

**Why #10**: User-facing transaction entry point. Validates card number against cross-reference, validates dates via CSUTLDTC subroutine call, generates a new transaction ID, and writes to the TRANSACT file. Involves multi-file reads for validation before the write, and date handling requires the external CSUTLDTC utility.

**Migration Recommendations**:
- Implement as `POST /transactions` REST endpoint
- Replace CSUTLDTC date validation with `java.time` API
- Auto-generate transaction IDs with UUID or sequence
- Add server-side validation annotations (`@Valid`, `@NotNull`, `@Pattern`)

---

## Risk Heat Map

```
                    LOW Business Impact ◄──────────────────► HIGH Business Impact
                    │                                                           │
HIGH Complexity     │  COCRDLIC (#5)        COACTUPC (#1)                      │
                    │  COCRDUPC (#6)        CBSTM03A (#4)                      │
                    │                       COTRN02C (#10)                     │
                    │                                                           │
                    │                                                           │
MEDIUM Complexity   │  COACTVWC (#8)        CBTRN02C (#2)                      │
                    │  CBTRN03C (#7)        CBACT04C (#3)                      │
                    │                       COBIL00C (#9)                      │
                    │                                                           │
LOW Complexity      │  CBACT01C             CSUTLDTC                           │
                    │  CBACT02C             COBSWAIT                           │
                    │  CBACT03C                                                │
                    │  CBCUS01C                                                │
                    │                                                           │
                    └───────────────────────────────────────────────────────────┘

Legend: Programs in the upper-right quadrant require the most careful migration planning.
```

---

## Recommended Migration Order

Based on the hotspot analysis, dependencies, and modernization best practices:

### Phase 1: Foundation (Low risk, high reuse)
| Order | Module | Rationale |
|-------|--------|-----------|
| 1 | CSUTLDTC | Utility — zero file dependencies, maps to `java.time` |
| 2 | COBSWAIT | Utility — trivial replacement with `Thread.sleep()` |
| 3 | Copybook entities | Convert all copybooks to JPA entities first (foundation for everything) |
| 4 | COCOM01Y | Convert COMMAREA to session/context object |

### Phase 2: Read-Only Operations (Low risk, validates data layer)
| Order | Module | Rationale |
|-------|--------|-----------|
| 5 | COACTVWC | Read-only; validates 4-table JOIN works correctly |
| 6 | COCRDSLC | Read-only card detail; validates card entity |
| 7 | COTRN01C | Read-only transaction view; validates transaction entity |
| 8 | CBACT01C-03C, CBCUS01C | Batch data dumps; validates batch framework + file reads |

### Phase 3: List/Browse Operations (Medium risk)
| Order | Module | Rationale |
|-------|--------|-----------|
| 9 | COUSR00C | User list — simpler pagination (smaller dataset) |
| 10 | COTRN00C | Transaction list — validates VSAM browse → SQL pagination |
| 11 | COCRDLIC (#5) | Card list — complex pagination, validates browse pattern |

### Phase 4: CRUD Operations (High risk, careful testing)
| Order | Module | Rationale |
|-------|--------|-----------|
| 12 | COUSR01C-03C | User CRUD — simpler entity, validates create/update/delete patterns |
| 13 | COCRDUPC (#6) | Card update — validates REWRITE → JPA update pattern |
| 14 | COTRN02C (#10) | Transaction add — validates multi-file validation + write |
| 15 | COACTUPC (#1) | Account update — MOST COMPLEX, do last in CRUD phase |

### Phase 5: Financial Operations (Highest risk)
| Order | Module | Rationale |
|-------|--------|-----------|
| 16 | COBIL00C (#9) | Bill payment — atomic financial transaction |
| 17 | CBTRN02C (#2) | Transaction posting — core batch, must be bulletproof |
| 18 | CBACT04C (#3) | Interest calculation — financial precision critical |

### Phase 6: Reporting and ETL
| Order | Module | Rationale |
|-------|--------|-----------|
| 19 | CBTRN03C (#7) | Transaction report — replace with SQL + report library |
| 20 | CBSTM03A/B (#4) | Statement generation — complex but isolated |
| 21 | CBEXPORT/CBIMPORT | Data migration — may not be needed post-modernization |
| 22 | CORPT00C | Report trigger — becomes simple API call |

### Phase 7: Optional Modules
| Order | Module | Rationale |
|-------|--------|-----------|
| 23 | Transaction Type DB2 | Already uses SQL; most natural migration |
| 24 | VSAM-MQ | Replace MQ with REST APIs or message broker |
| 25 | Authorization IMS/DB2/MQ | Most complex; requires IMS DB → relational migration |

---

## Modernization Patterns per Hotspot

| Hotspot | COBOL Pattern | Java Target Pattern | Key Risk |
|---------|--------------|--------------------:|----------|
| COACTUPC (#1) | Monolithic validation + REWRITE | Service + `@Valid` annotations + JPA | Validation logic completeness |
| CBTRN02C (#2) | Sequential file processing + reject file | Spring Batch `Tasklet` + dead-letter queue | Transaction atomicity |
| CBACT04C (#3) | Packed decimal math + multi-file reads | `BigDecimal` + SQL JOINs | Rounding precision |
| CBSTM03A (#4) | ALTER/GO TO + subroutine CALL | Template engine (Thymeleaf/JasperReports) | ALTER statement conversion |
| COCRDLIC (#5) | VSAM BROWSE + pseudo-conversational | SQL pagination + REST API | Session state management |
| COCRDUPC (#6) | REWRITE + field validation | JPA `save()` + Bean Validation | Field mapping completeness |
| CBTRN03C (#7) | SORT pre-step + report formatting | SQL ORDER BY + report library | Date filtering accuracy |
| COACTVWC (#8) | 4 sequential VSAM READs | Single SQL JOIN | N+1 query avoidance |
| COBIL00C (#9) | Non-atomic dual writes | `@Transactional` | Atomicity guarantee |
| COTRN02C (#10) | Multi-file validation + WRITE | Service validation + JPA persist | Validation completeness |
