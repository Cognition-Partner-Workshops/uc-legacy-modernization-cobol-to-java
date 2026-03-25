# CardDemo Hotspot Report

> **Generated**: 2026-03-25 | **Application**: CardDemo - Mainframe Credit Card Management System
> **Purpose**: Identify the top 10 modules prioritized by code complexity, migration risk, and business impact to guide modernization sequencing.

---

## Table of Contents

- [1. Scoring Methodology](#1-scoring-methodology)
- [2. Top 10 Hotspot Modules](#2-top-10-hotspot-modules)
- [3. Detailed Analysis](#3-detailed-analysis)
  - [Rank 1: COACTUPC - Account Update](#rank-1-coactupc---account-update)
  - [Rank 2: CBTRN02C - Post Daily Transactions](#rank-2-cbtrn02c---post-daily-transactions)
  - [Rank 3: COTRTLIC - Transaction Type List (DB2)](#rank-3-cotrtlic---transaction-type-list-db2)
  - [Rank 4: COTRTUPC - Transaction Type Add/Edit (DB2)](#rank-4-cotrtupc---transaction-type-addedit-db2)
  - [Rank 5: COCRDLIC - Credit Card List](#rank-5-cocrdlic---credit-card-list)
  - [Rank 6: COCRDUPC - Credit Card Update](#rank-6-cocrdupc---credit-card-update)
  - [Rank 7: COPAUA0C - Authorization Request Processing (IMS/MQ)](#rank-7-copaua0c---authorization-request-processing-imsmq)
  - [Rank 8: COPAUS0C - Pending Authorization Summary (IMS)](#rank-8-copaus0c---pending-authorization-summary-ims)
  - [Rank 9: CBSTM03A - Account Statement Generation](#rank-9-cbstm03a---account-statement-generation)
  - [Rank 10: CBACT04C - Interest Calculation](#rank-10-cbact04c---interest-calculation)
- [4. Risk Heat Map](#4-risk-heat-map)
- [5. Modernization Sequencing Recommendation](#5-modernization-sequencing-recommendation)
- [6. Technical Debt Observations](#6-technical-debt-observations)

---

## 1. Scoring Methodology

Each module is scored on three dimensions (1-10 scale each), producing a weighted composite score:

| Dimension | Weight | Criteria |
|-----------|-------:|---------|
| **Code Complexity** | 35% | Lines of code, cyclomatic complexity indicators (nested IF/EVALUATE, PERFORM VARYING, paragraph count), number of COPY/CALL dependencies, data structures used |
| **Migration Risk** | 35% | Technology diversity (CICS, DB2, IMS, MQ, ASM), VSAM access patterns (browse, AIX, I-O), platform-specific features (ALTER, COMP-3, REDEFINES), external system coupling |
| **Business Impact** | 30% | Financial data handling, user-facing frequency, downstream dependencies, regulatory sensitivity (PII, payment data) |

**Composite Score** = (Complexity x 0.35) + (Risk x 0.35) + (Impact x 0.30)

---

## 2. Top 10 Hotspot Modules

| Rank | Program | LOC | Type | Complexity | Risk | Impact | **Score** | Primary Concern |
|-----:|---------|----:|------|:----------:|:----:|:------:|:---------:|----------------|
| 1 | **COACTUPC** | 4,236 | CICS Online | 10 | 8 | 9 | **9.0** | Largest program; complex account update with multi-file VSAM I/O |
| 2 | **CBTRN02C** | 731 | Batch | 8 | 8 | 10 | **8.6** | Core transaction posting; 6 files, balance updates, reject handling |
| 3 | **COTRTLIC** | 2,098 | CICS+DB2 | 9 | 9 | 6 | **8.1** | DB2 cursor operations, CICS-DB2 integration complexity |
| 4 | **COTRTUPC** | 1,702 | CICS+DB2 | 8 | 9 | 6 | **7.7** | DB2 INSERT/UPDATE from CICS, DSNTIAC error handling |
| 5 | **COCRDLIC** | 1,459 | CICS Online | 8 | 7 | 8 | **7.7** | VSAM browse with AIX, forward/backward pagination |
| 6 | **COCRDUPC** | 1,560 | CICS Online | 8 | 7 | 8 | **7.7** | Card data updates, multi-file validation |
| 7 | **COPAUA0C** | 1,026 | CICS+IMS+MQ | 7 | 10 | 7 | **8.0** | Triple technology: CICS + IMS DL/I + MQ messaging |
| 8 | **COPAUS0C** | 1,032 | CICS+IMS | 7 | 9 | 6 | **7.4** | IMS segment retrieval from CICS, BMS screen |
| 9 | **CBSTM03A** | 924 | Batch | 9 | 7 | 8 | **8.0** | ALTER verb, called subroutine, dual output (text+HTML) |
| 10 | **CBACT04C** | 652 | Batch | 7 | 6 | 10 | **7.6** | Interest calculation on all accounts; 5 files, financial precision |

---

## 3. Detailed Analysis

### Rank 1: COACTUPC - Account Update

**File**: `app/cbl/COACTUPC.cbl` | **Lines**: 4,236 | **Type**: CICS Online | **Transaction**: CAUP

| Dimension | Score | Justification |
|-----------|------:|--------------|
| Complexity | 10 | By far the largest program (4,236 LOC - 2.7x the next largest). Extensive field-level validation, screen handling, and error processing. Multiple VSAM READ operations across different files. |
| Risk | 8 | 5 distinct VSAM file accesses (ACCTDAT, CARDDAT, CUSTDAT, CARDXREF, USRSEC). Complex BMS map handling with COACTUP copybook. XCTL navigation to/from COMEN01C. |
| Impact | 9 | Account updates directly affect financial records. Any migration bug could corrupt account balances or credit limits. High user-facing frequency. |

**Complexity Indicators**:
- 5 VSAM READ operations across different datasets
- Extensive EVALUATE/WHEN blocks for field validation
- Multiple EXEC CICS SEND MAP / RECEIVE MAP cycles
- COPY statements: COCOM01Y, CSLKPCDY, CSSETATY, COACTUP (BMS), COTTL01Y, CSDAT01Y, CSMSG01Y, CSUSR01Y, CVACT01Y, CVACT02Y, CVACT03Y, CVCUS01Y, DFHAID, DFHBMSCA
- XCTL to COMEN01C for navigation

**Migration Considerations**:
- Decompose into smaller services (account read, account validate, account write)
- Field-level validation logic should become a shared validation service
- BMS screen logic maps to a modern form UI with client-side validation

---

### Rank 2: CBTRN02C - Post Daily Transactions

**File**: `app/cbl/CBTRN02C.cbl` | **Lines**: 731 | **Type**: Batch | **JCL**: POSTTRAN

| Dimension | Score | Justification |
|-----------|------:|--------------|
| Complexity | 8 | 6 file descriptors (DALYTRAN, TRANSACT, XREFFILE, DALYREJS, ACCTFILE, TCATBALF). Complex posting logic with cross-reference validation and balance updates. |
| Risk | 8 | Multi-file I-O mode (read-write on ACCTFILE and TCATBALF). Reject handling with separate output file. CEE3ABD abort on failure. |
| Impact | 10 | **Core business process**: Posts all daily transactions to master files. Failure means no transactions get processed. Updates account balances and category balances. |

**Complexity Indicators**:
- 6 SELECT/FD file definitions
- I-O mode on ACCTFILE and TCATBALF (read-modify-write)
- Cross-reference lookup for card-to-account mapping
- Rejected transaction routing to DALYREJS
- Account balance updates during posting
- COPY: CVTRA05Y, CVTRA06Y, CVACT01Y, CVACT03Y, CVTRA01Y

**Migration Considerations**:
- Map to a transactional batch service with database transactions
- Reject handling becomes exception/dead-letter queue pattern
- Balance updates should use optimistic locking in the target platform
- Critical path for end-of-day processing - needs comprehensive regression testing

---

### Rank 3: COTRTLIC - Transaction Type List (DB2)

**File**: `app/app-transaction-type-db2/cbl/COTRTLIC.cbl` | **Lines**: 2,098 | **Type**: CICS+DB2 | **Transaction**: CTLI

| Dimension | Score | Justification |
|-----------|------:|--------------|
| Complexity | 9 | Second largest program. DB2 cursor operations (DECLARE, OPEN, FETCH, CLOSE). Complex scrollable list with pagination. Delete operations with referential integrity checks. |
| Risk | 9 | Dual technology: CICS + DB2. Uses SQLCA, DSNTIAC for error formatting. DB2 cursor lifecycle management. CICS pseudo-conversational pattern with DB2 state. |
| Impact | 6 | Admin-only function for maintaining reference data. Lower frequency but data integrity is important for downstream transaction processing. |

**Complexity Indicators**:
- EXEC SQL with DECLARE CURSOR, OPEN, FETCH, CLOSE
- DSNTIAC error message formatting (CSDB2RPY copybook)
- Scrollable list with forward/backward navigation
- DELETE with validation
- COPY: CSDB2RPY, CSDB2RWY, COTRTLI (BMS), COCOM01Y, etc.

**Migration Considerations**:
- DB2 cursor patterns map directly to JPA/JDBC ResultSet
- DSNTIAC error handling replaced by standard SQL exception handling
- Scrollable list becomes paginated REST API or UI table component

---

### Rank 4: COTRTUPC - Transaction Type Add/Edit (DB2)

**File**: `app/app-transaction-type-db2/cbl/COTRTUPC.cbl` | **Lines**: 1,702 | **Type**: CICS+DB2 | **Transaction**: CTTU

| Dimension | Score | Justification |
|-----------|------:|--------------|
| Complexity | 8 | Large program with INSERT/UPDATE SQL operations. Input validation, duplicate checking, and error handling. CICS pseudo-conversational with DB2. |
| Risk | 9 | Same CICS+DB2 dual-technology risk as COTRTLIC. SQL INSERT and UPDATE operations with SQLCODE checking. |
| Impact | 6 | Admin function for reference data maintenance. Changes affect transaction categorization downstream. |

**Complexity Indicators**:
- EXEC SQL INSERT, UPDATE operations
- SQLCODE evaluation with detailed error paths
- DSNTIAC utility integration
- Input validation for type codes and descriptions
- COPY: CSDB2RPY, CSDB2RWY, COTRTUP (BMS)

**Migration Considerations**:
- Maps to CRUD REST endpoints with JPA entities
- Validation logic becomes Bean Validation annotations
- Consider combining with COTRTLIC into a single Transaction Type microservice

---

### Rank 5: COCRDLIC - Credit Card List

**File**: `app/cbl/COCRDLIC.cbl` | **Lines**: 1,459 | **Type**: CICS Online | **Transaction**: CCLI

| Dimension | Score | Justification |
|-----------|------:|--------------|
| Complexity | 8 | Complex VSAM browse logic with STARTBR/READNEXT/READPREV. Alternate index (AIX) access on card file. Forward and backward scrolling. Navigation to detail/update. |
| Risk | 7 | VSAM browse with AIX requires careful mapping to SQL. ENDBR management. Screen pagination state across pseudo-conversational boundaries. String padding utility (CSSTRPFY). |
| Impact | 8 | Primary card management screen. High user frequency. Security-sensitive (card numbers displayed). |

**Complexity Indicators**:
- EXEC CICS STARTBR/READNEXT/READPREV/ENDBR on LIT-CARD-FILE
- Alternate index browsing for account-based card lookup
- Forward/backward page scrolling logic
- XCTL to COCRDSLC (detail) and COCRDUPC (update)
- COPY: CVCRD01Y, CVACT02Y, COCOM01Y, COCRDLI (BMS), CSSTRPFY

**Migration Considerations**:
- Browse/pagination maps to SQL OFFSET/LIMIT or keyset pagination
- AIX access becomes a secondary index query
- Card number masking should be added during modernization (PCI-DSS)

---

### Rank 6: COCRDUPC - Credit Card Update

**File**: `app/cbl/COCRDUPC.cbl` | **Lines**: 1,560 | **Type**: CICS Online | **Transaction**: CCUP

| Dimension | Score | Justification |
|-----------|------:|--------------|
| Complexity | 8 | Multi-file reads for validation (Card, Customer data). Field-level update processing. BMS map send/receive cycles. |
| Risk | 7 | Multiple VSAM READ operations. String processing utility. Navigation back to card list. |
| Impact | 8 | Card data updates are security-sensitive. Embossed name, status, expiration date changes. PCI-relevant data. |

**Complexity Indicators**:
- 2 EXEC CICS READ operations (CARDDAT, CUSTDAT)
- BMS SEND MAP / RECEIVE MAP handling
- XCTL to COCRDLIC for return navigation
- COPY: CVCRD01Y, CVACT02Y, CVCUS01Y, COCRDUP (BMS), CSSTRPFY

**Migration Considerations**:
- Card update operations need audit logging in modernized system
- PCI-DSS compliance: encrypt card data at rest
- Combine with COCRDSLC into a unified Card management service

---

### Rank 7: COPAUA0C - Authorization Request Processing (IMS/MQ)

**File**: `app/app-authorization-ims-db2-mq/cbl/COPAUA0C.cbl` | **Lines**: 1,026 | **Type**: CICS+IMS+MQ | **Transaction**: CP00

| Dimension | Score | Justification |
|-----------|------:|--------------|
| Complexity | 7 | MQ message processing (GET/PUT), IMS DL/I calls (ISRT, GU), CICS integration. Request/response message parsing. |
| Risk | 10 | **Highest technology risk**: Three middleware technologies (CICS + IMS + MQ). Each requires separate migration strategy. MQ trigger-based invocation. IMS segment manipulation. |
| Impact | 7 | Authorization decisions are business-critical but this is an optional module. Real-time fraud/auth decision path. |

**Complexity Indicators**:
- MQGET / MQPUT for request/response processing
- IMS DL/I calls: GU (Get Unique), ISRT (Insert) via CBLTDLI
- CICS pseudo-conversational processing
- Multiple PCB references (PAUTBPCB)
- IMS function codes from IMSFUNCS copybook

**Migration Considerations**:
- MQ messaging maps to JMS, Kafka, or cloud messaging (SQS/SNS)
- IMS database maps to relational DB or document store
- Consider event-driven architecture for authorization flow
- Highest risk module - recommend proof-of-concept migration first

---

### Rank 8: COPAUS0C - Pending Authorization Summary (IMS)

**File**: `app/app-authorization-ims-db2-mq/cbl/COPAUS0C.cbl` | **Lines**: 1,032 | **Type**: CICS+IMS | **Transaction**: CPVS

| Dimension | Score | Justification |
|-----------|------:|--------------|
| Complexity | 7 | IMS segment browsing (GU, GN calls). Summary data aggregation from hierarchical database. BMS screen display. |
| Risk | 9 | IMS DL/I integration from CICS. Hierarchical data model requires relational mapping. PCB status checking. |
| Impact | 6 | View-only summary screen. Optional module. Important for authorization monitoring but no data modification. |

**Complexity Indicators**:
- IMS GU (Get Unique), GN (Get Next) calls
- PCB status code evaluation
- BMS map COPAU00 for summary display
- Hierarchical to flat data transformation for screen display

**Migration Considerations**:
- IMS hierarchical model needs relational schema design
- Summary view becomes a simple query with aggregation
- Can be combined with COPAUS1C and COPAUA0C into an Authorization service

---

### Rank 9: CBSTM03A - Account Statement Generation

**File**: `app/cbl/CBSTM03A.CBL` | **Lines**: 924 | **Type**: Batch | **JCL**: CREASTMT

| Dimension | Score | Justification |
|-----------|------:|--------------|
| Complexity | 9 | Uses the **ALTER** verb (self-modifying code). Dual output format (plain text + HTML). Calls CBSTM03B subroutine for file I/O. Mainframe control block addressing. |
| Risk | 7 | ALTER verb is a legacy COBOL feature rarely supported in modern tooling. CALL to subroutine CBSTM03B for all file operations. Two output streams (text + HTML). |
| Impact | 8 | Generates customer-facing account statements. Financial accuracy is critical. Output formats need to match exactly during migration. |

**Complexity Indicators**:
- **ALTER** verb (self-modifying GO TO targets) - rare and complex
- CALL 'CBSTM03B' for file open/close/read operations (9 CALL sites)
- Dual output: STMTFILE (text) and HTMLFILE (HTML)
- Per-card statement aggregation from TRXFL.VSAM
- COPY: COSTM01 (statement layout)
- File processing through called subroutine (separation of concerns)

**Migration Considerations**:
- ALTER verb must be refactored to structured control flow before/during migration
- Dual output maps to template engine (e.g., Thymeleaf, FreeMarker)
- Statement generation becomes a report service with PDF/HTML output
- CBSTM03A + CBSTM03B should be migrated together as a unit

---

### Rank 10: CBACT04C - Interest Calculation

**File**: `app/cbl/CBACT04C.cbl` | **Lines**: 652 | **Type**: Batch | **JCL**: INTCALC

| Dimension | Score | Justification |
|-----------|------:|--------------|
| Complexity | 7 | 5 file descriptors. Complex financial calculation logic. Per-account, per-category interest computation using disclosure group rates. |
| Risk | 6 | Standard batch COBOL patterns. VSAM sequential processing. I-O mode on account file for balance updates. |
| Impact | 10 | **Highest business impact**: Directly computes interest charges on all customer accounts. Financial calculation precision is critical. Any error affects revenue and customer billing. |

**Complexity Indicators**:
- 5 files: TCATBALF (input), XREFFILE (input), DISCGRP (input), ACCTFILE (I-O), TRANSACT (output)
- Per-account interest calculation loop
- Disclosure group rate lookup per transaction category
- Account balance update with calculated interest
- New interest transactions written to TRANSACT file
- COPY: CVACT01Y, CVACT03Y, CVTRA01Y, CVTRA02Y

**Migration Considerations**:
- Financial calculations must use BigDecimal (not floating point)
- Comprehensive regression testing with known-good results
- Consider regulatory audit trail requirements
- Map to a scheduled batch service with idempotent processing

---

## 4. Risk Heat Map

```
                    Business Impact
                Low ◄──────────────────► High
           ┌────────┬──────────┬──────────┐
     High  │        │ COTRTLIC │ COPAUA0C │
           │        │ COTRTUPC │          │
  Migration│        │ COPAUS0C │          │
    Risk   ├────────┼──────────┼──────────┤
           │        │ COCRDLIC │ COACTUPC │
     Med   │        │ COCRDUPC │ CBSTM03A │
           │        │          │ CBTRN02C │
           ├────────┼──────────┼──────────┤
     Low   │        │          │ CBACT04C │
           │        │          │          │
           └────────┴──────────┴──────────┘

Legend:
  Top-right = Highest priority (high impact + high risk)
  Bottom-right = Quick wins (high impact + low risk)
  Top-left = Technology challenges (high risk + low impact)
```

---

## 5. Modernization Sequencing Recommendation

### Wave 1: Foundation & Quick Wins (Weeks 1-4)

| Priority | Module | Rationale |
|----------|--------|-----------|
| 1 | **CBACT04C** (Interest Calc) | High impact, low risk. Standard batch pattern. Establishes financial calculation baseline. |
| 2 | **CBTRN02C** (Post Transactions) | Core business process. Standard batch pattern with file I/O. |
| 3 | **CBSTM03A + CBSTM03B** (Statements) | Self-contained unit. ALTER verb needs refactoring. Good early test of report generation. |

**Rationale**: Start with batch programs that have standard patterns, establish data access layers, and validate financial precision.

### Wave 2: Core Online Screens (Weeks 5-10)

| Priority | Module | Rationale |
|----------|--------|-----------|
| 4 | **COCRDLIC** (Card List) | Complex browse/pagination. Validates VSAM-to-SQL migration for AIX patterns. |
| 5 | **COCRDUPC** (Card Update) | Builds on card data model from COCRDLIC. Tests update operations. |
| 6 | **COACTUPC** (Account Update) | Largest program. Should be decomposed during migration. Critical to get right. |

**Rationale**: Migrate user-facing screens that exercise the core data model. COACTUPC last in this wave because it's the largest and benefits from lessons learned.

### Wave 3: Optional Modules (Weeks 11-16)

| Priority | Module | Rationale |
|----------|--------|-----------|
| 7 | **COTRTLIC + COTRTUPC** (DB2 Tran Types) | DB2 programs - easier to migrate than IMS. Validates DB2-to-JPA mapping. |
| 8 | **COPAUA0C** (Auth Processing) | Highest technology risk. CICS+IMS+MQ. Recommend proof-of-concept first. |
| 9 | **COPAUS0C** (Auth Summary) | Depends on IMS migration decisions from COPAUA0C. |

**Rationale**: Optional modules have the highest technology diversity. Tackle DB2 first (more straightforward), then IMS+MQ (requires architectural decisions).

---

## 6. Technical Debt Observations

### Critical Issues

| # | Issue | Affected Programs | Severity | Modernization Action |
|---|-------|------------------|----------|---------------------|
| 1 | **Plain-text passwords** | COSGN00C, CSUSR01Y | Critical | Hash passwords with bcrypt/scrypt in target system |
| 2 | **ALTER verb (self-modifying code)** | CBSTM03A | High | Refactor to structured IF/EVALUATE before migration |
| 3 | **Sensitive data unmasked** | COCRDLIC, COCRDSLC, COCRDUPC | High | Add card number masking (PCI-DSS compliance) |
| 4 | **No audit trail** | COACTUPC, COCRDUPC, COUSR02C | Medium | Add audit logging for data modifications |
| 5 | **CEE3ABD hard aborts** | All batch programs | Medium | Replace with structured error handling and graceful recovery |

### Code Quality Observations

| # | Observation | Examples | Impact |
|---|------------|---------|--------|
| 1 | **Inconsistent file naming** | `.cbl` vs `.CBL`, `.cpy` vs `.CPY` | Build/tooling confusion |
| 2 | **Large monolithic programs** | COACTUPC (4,236 LOC) | Difficult to test and maintain |
| 3 | **FILLER bytes in records** | All copybooks have FILLER | Wasted storage; remove in modernized schema |
| 4 | **Date fields as strings** | `X(10)` and `X(26)` for dates | Use proper date/timestamp types in target |
| 5 | **Unused copybook** | UNUSED1Y.cpy | Dead code to be removed |
| 6 | **Spelling errors in field names** | `EXPIRAION-DATE` (missing 'T') | Carry forward or fix during migration |
| 7 | **Tight coupling via COMMAREA** | All CICS programs share COCOM01Y | Decouple using API contracts in microservices |
| 8 | **Hardcoded literals** | Various programs | Extract to configuration |

### Positive Patterns (Leverage During Migration)

| # | Pattern | Where | Benefit |
|---|---------|-------|---------|
| 1 | Clean separation of batch I/O | CBSTM03A calls CBSTM03B | Already follows repository pattern |
| 2 | Shared utility modules | CSUTLDTC (date validation) | Maps to shared library/service |
| 3 | Consistent COMMAREA navigation | All CICS programs | Clear API contract for screen flow |
| 4 | Reference data separation | CVTRA03Y, CVTRA04Y | Clean lookup table pattern |
| 5 | Cross-reference junction table | CVACT03Y | Good relational design already |
